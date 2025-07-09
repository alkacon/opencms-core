/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.function.FailableSupplier;
import org.apache.commons.logging.Log;

/**
 * A helper class used by CmsSecurityManager to keep track of modified resources for the 'online folders' feature and 'publish' them when we are done with them.
 *
 * <p>This class is meant to be used with the try-with-resources syntax from Java 7: Use the acquire() method in the try expression (e.g. 'try (CmsModificationContext modContext = CmsModificationContext.acquire(requestContext)) { ... }'.
 * Then use the add() method on the modification context to add any modified resources. Once the close() method is automatically invoked by the try-with-resources statement, *and* we are in the outermost nesting level of such try-with-resources statements,
 * the modified resources are published if they belong to a configured online folder.
 */
public class CmsModificationContext implements AutoCloseable {

    /** A lock used to prevent concurrent execution of the resource publication that happens when closing the context. */
    private static final ReentrantLock LOCK = new ReentrantLock(true);

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsModificationContext.class);

    /** Static admin CmsObject. */
    private static CmsObject m_adminCms;

    /** The configuration. */
    private static CmsOnlineFolderOptions m_options;

    /** The security manager. */
    private static CmsSecurityManager m_securityManager;

    /** The active instance for the current thread. */
    private static final ThreadLocal<CmsModificationContext> threadLocalInstance = new ThreadLocal<>();

    /** The added structure ids (not necessarily for resources in the online folder). */
    private Set<CmsUUID> m_ids = new HashSet<>();

    /** The current 'nesting level' of modification contexts. */
    private int m_nestingLevel = 0;

    /** The current request context for which the modification context was acquired. */
    private CmsRequestContext m_requestContext;

    /** The added resources in the online folder. */
    private Set<CmsResource> m_resources = new HashSet<>();

    /**
     * Creates a new instance.
     *
     * @param context the request context
     */
    private CmsModificationContext(CmsRequestContext context) {

        m_requestContext = context;

    }

    /**
     * Executes the given action and returns the result, wrapping the execution in a modification context.
     *
     * @param <T> the result type
     * @param requestContext the current request context
     * @param runnable the action to execute
     * @return the result of the action
     *
     * @throws CmsException if something goes wrong
     */
    public static <T> T doWithModificationContext(
        CmsRequestContext requestContext,
        FailableSupplier<T, CmsException> runnable)
    throws CmsException {

        try (CmsModificationContext modContext = acquire(requestContext)) {
            return runnable.get();
        }
    }

    public static CmsOnlineFolderOptions getOnlineFolderOptions() {

        return m_options;
    }

    /**
     * Initializes this class.
     *
     * @param securityManager the security manager instance
     * @param adminCms a CmsObject with admin privileges
     * @param onlineFolderPath the online folder path (nay be null)
     */
    public static void initialize(
        CmsSecurityManager securityManager,
        CmsObject adminCms,
        CmsOnlineFolderOptions options) {

        m_securityManager = securityManager;
        m_adminCms = adminCms;
        m_options = options;
    }

    /**
     * Checks if the given path is below the configured online folder.
     *
     * <p>If no online folder is configured, this will return false.
     *
     * @param path the path to check
     * @return true if the given path is below the configured online folder
     */
    public static boolean isInOnlineFolder(String path) {

        return m_options.getPaths().stream().anyMatch(onlineFolder -> CmsStringUtil.isPrefixPath(onlineFolder, path));
    }

    /**
     * Checks if an 'instant publish' operation is currently running.
     *
     * @return true if an 'instant publish' operation is running
     */
    public static boolean isInstantPublishing() {

        return LOCK.isHeldByCurrentThread();
    }

    /**
     * Acquires a modification context.
     *
     * <p>If a modification context was acquired in a higher stack frame, the existing context will be returned, but its level counter will be increased by 1, otherwise a new context
     * will be created with a level counter of 1.
     *
     * @param context the request context for which to get a modification context
     * @return the modification context
     */
    protected static CmsModificationContext acquire(CmsRequestContext context) {

        CmsModificationContext instance = threadLocalInstance.get();
        if (instance == null) {
            instance = new CmsModificationContext(context);
            threadLocalInstance.set(instance);
        }
        instance.m_nestingLevel += 1;
        return instance;
    }

    /**
     * Checks if the given resource is in the online only folder, and if so, adds it to the set of resources that should be instant-published when the modification context is closed.
     *
     * @param resource the resource to add
     */
    public void add(CmsResource resource) {

        if (resource == null) {
            return;
        }
        if (!isInOnlineFolder(resource.getRootPath())) {
            return;
        }
        m_resources.add(resource);
    }

    /**
     * Alternative to add(CmsResource) for methods where the resource is not read.
     *
     *  <p>Avoid using this if possible, in favor of add(CmsResource)
     *
     * @param structureId the structure id of the resource to add
     */
    public void addId(CmsUUID structureId) {

        m_ids.add(structureId);
    }

    /**
     * Decrements the modification context's level counter by 1, and finally closes it if the counter reaches zero.
     * <p>If the counter reaches zero, all resources added with the add() method will be published synchronously, without going through
     * the publish queue.
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws CmsException {

        if (m_nestingLevel <= 0) {
            throw new IllegalStateException(CmsModificationContext.class.getSimpleName() + " closed too often!");
        }
        m_nestingLevel -= 1;
        if (m_nestingLevel == 0) {
            threadLocalInstance.remove();
            Set<CmsResource> resources = m_resources;
            if (resources.size() == 0) {
                return;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("closing modification context with resources " + resources.toString());
                if (m_ids.size() > 0) {
                    LOG.debug("additional ids: " + m_ids);
                }
            }
            LOCK.lock();
            try {
                CmsProject project = m_requestContext.getCurrentProject();
                CmsObject adminCms = OpenCms.initCmsObject(m_adminCms);

                // We use an admin CmsObject to read the resources without any further permission checks

                adminCms.getRequestContext().setCurrentProject(project);
                Set<CmsResource> resources2 = new HashSet<>();
                Set<CmsUUID> idsOfResources = new HashSet<>();
                for (CmsResource resource : resources) {
                    try {
                        idsOfResources.add(resource.getStructureId());
                        resources2.add(adminCms.readResource(resource.getStructureId(), CmsResourceFilter.ALL));
                    } catch (CmsVfsResourceNotFoundException e) {
                        LOG.debug(
                            "Could not find modified resource: "
                                + resource.getRootPath()
                                + " "
                                + resource.getStructureId());
                    }
                }
                for (CmsUUID id : m_ids) {
                    if (idsOfResources.contains(id)) {
                        continue;
                    }
                    try {
                        CmsResource resource = adminCms.readResource(id, CmsResourceFilter.ALL);
                        if (isInOnlineFolder(resource.getRootPath())) {
                            resources2.add(resource);
                        }
                    } catch (CmsVfsResourceNotFoundException e) {
                        LOG.debug("Could not find modified resource for id: " + id);
                    }

                }
                CmsPublishList pubList = new CmsPublishList(true, new ArrayList<>(resources2), false);
                pubList.setUserPublishList(true);
                m_securityManager.fillPublishList(m_requestContext, pubList);
                if (pubList.size() > 0) {
                    CmsDbContext dbc1 = m_securityManager.m_dbContextFactory.getDbContext(m_requestContext);
                    I_CmsReport report = new CmsLogReport(Locale.ENGLISH, CmsModificationContext.class);
                    m_securityManager.publishJob(
                        OpenCms.initCmsObject(m_adminCms, new CmsContextInfo(m_requestContext)),
                        dbc1,
                        pubList,
                        report);
                    if (report.hasError() || report.hasWarning()) {
                        for (Object o : report.getErrors()) {
                            if (o instanceof Throwable) {
                                Throwable t = (Throwable)o;
                                LOG.error("Report error: " + t.getMessage(), t);
                            }
                        }
                        for (Object o : report.getWarnings()) {
                            if (o instanceof Throwable) {
                                Throwable t = (Throwable)o;
                                LOG.warn("Report warning: " + t.getMessage(), t);
                            }
                        }
                    }
                    CmsDbContext dbc2 = m_securityManager.m_dbContextFactory.getDbContext(m_requestContext);
                    try {
                        // fire an event that a project has been published
                        Map<String, Object> eventData = new HashMap<String, Object>();
                        eventData.put(I_CmsEventListener.KEY_REPORT, report);
                        eventData.put(I_CmsEventListener.KEY_PUBLISHID, pubList.getPublishHistoryId().toString());
                        eventData.put(I_CmsEventListener.KEY_PROJECTID, dbc2.currentProject().getUuid());
                        eventData.put(I_CmsEventListener.KEY_INSTANT_PUBLISH, Boolean.TRUE);
                        eventData.put(I_CmsEventListener.KEY_DBCONTEXT, dbc2);
                        CmsEvent afterPublishEvent = new CmsEvent(I_CmsEventListener.EVENT_PUBLISH_PROJECT, eventData);
                        OpenCms.fireCmsEvent(afterPublishEvent);
                    } catch (Throwable t) {
                        LOG.error(t.getLocalizedMessage(), t);
                    } finally {
                        dbc2.clear();
                    }
                }
            } finally {
                LOCK.unlock();
            }
        }
    }

}

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

package org.opencms.file.quota;

import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;

import com.google.common.base.Functions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Maintains folder size information for the system and updates it regularly.
 *
 * <p>The folder size information is updated asynchronously and with a delay, so it is not necessarily 100% exact at any particular time.
 */
public class CmsFolderSizeTracker {

    /** Default interval for the update timer (in ms). */
    public static final long DEFAULT_TIMER_INTERVAL = 30000;

    /** The logger instance for the class. */
    private static final Log LOG = CmsLog.getLog(CmsFolderSizeTracker.class);

    /** The CMS context. */
    private CmsObject m_cms;

    /** True if object has been initialized. */
    private boolean m_initialized;

    /** Used to synchronize access to the internal state. */
    private Object m_lock = new Object();

    /** A read-only copy of the folder size information that is used for normal read operations. */
    private volatile CmsFolderSizeTable m_table;

    /** Set of paths that still need to be processed. */
    private LinkedBlockingQueue<String> m_todo = new LinkedBlockingQueue<>();

    /** Timer interval. */
    private long m_interval;

    /** Just a simple timed cache to save space for commonly used paths in the work queue. */
    private LoadingCache<String, String> m_pathCache;

    private boolean m_online;

    /**
     * Creates a new instance.
     *
     * @param cms the CMS context
     * @param true if we want to track folder sizes in the Online project instead of the Offline project
     */
    public CmsFolderSizeTracker(CmsObject cms, boolean online) {

        try {
            m_cms = OpenCms.initCmsObject(cms);
        } catch (CmsException e) {
            // shouldn't happen
            LOG.error(e.getLocalizedMessage(), e);
        }
        m_table = new CmsFolderSizeTable(m_cms, online);
        m_online = online;
        CacheLoader<String, String> loader = CacheLoader.from(Functions.identity());
        m_pathCache = CacheBuilder.newBuilder().concurrencyLevel(4).expireAfterAccess(30, TimeUnit.SECONDS).build(
            loader);
    }

    /**
     * Prepares a folder report consisting of subtree sizes for a bunch of folders.
     *
     * <p>This is more efficient than querying for folder sizes individually.
     *
     * @param folders the folders (list of root paths)
     * @return the folder report
     */
    public Map<String, CmsFolderReportEntry> getFolderReport(Collection<String> folders) {

        if (!m_initialized) {
            return Collections.emptyMap();
        }

        return m_table.getFolderReport(folders);
    }

    /**
     * Gets the timer interval.
     *
     * @return the timer interval
     */
    public long getTimerInterval() {

        return m_interval;
    }

    /**
     * Gets the total folder size for the complete subtree at the given root path.
     *
     * @param rootPath the root path for which to compute the size
     * @return the total size
     */
    public long getTotalFolderSize(String rootPath) {

        if (!m_initialized) {
            return -1;
        }
        return m_table.getTotalFolderSize(rootPath);
    }

    /**
     * Gets the folder size for the subtree at the given root path, but without including any folder sizes
     * of subtrees at any paths from 'otherPaths' of which rootPath is a proper prefix.
     *
     * @param rootPath the root path for which to calculate the size
     * @param otherPaths the other paths to exclude from the size
     *
     * @return the total size
     */
    public long getTotalFolderSizeExclusive(String rootPath, Collection<String> otherPaths) {

        if (!m_initialized) {
            return -1;
        }
        return m_table.getTotalFolderSizeExclusive(rootPath, otherPaths);
    }

    /**
     * Initializes this object (and then returns it).
     *
     * @return this instance
     */
    public CmsFolderSizeTracker initialize() {

        if (!m_initialized) {
            Object prop = OpenCms.getRuntimeProperty("folderSizeTrackerInterval");
            m_interval = DEFAULT_TIMER_INTERVAL;
            if (prop != null) {
                try {
                    m_interval = Long.parseLong("" + prop);
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            if (m_interval > 0) {
                reload();
                OpenCms.getEventManager().addCmsEventListener(this::handleEvent);
                OpenCms.getExecutor().scheduleWithFixedDelay(
                    this::processUpdates,
                    m_interval,
                    m_interval,
                    TimeUnit.MILLISECONDS);

                // Just in case something gets corrupted - reload every day
                OpenCms.getExecutor().scheduleWithFixedDelay(this::reload, 24, 24, TimeUnit.HOURS);

                m_initialized = true;
            }
        }
        return this;

    }

    /**
     * The scheduled task.
     */
    public void processUpdates() {

        long start = System.currentTimeMillis();
        try {
            synchronized (m_lock) {
                Set<String> paths = new HashSet<>();
                m_todo.drainTo(paths);
                LOG.debug("Processing path update set of size " + paths.size());
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Update set: " + paths);
                }
                if (paths.size() > 0) {
                    CmsFolderSizeTable newTable = new CmsFolderSizeTable(m_table);
                    for (String path : paths) {
                        newTable.updateSingle(path);
                    }
                    newTable.updateSubtreeCache();
                    m_table = newTable;
                }
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        long end = System.currentTimeMillis();
        long duration = end - start;
        if (LOG.isDebugEnabled() && (duration > 250)) {
            LOG.debug("folder size tracker update took " + duration + "ms");
        }
    }

    /**
     * Refreshes the data for a particular subtree.
     *
     * @param rootPath the root path to refresh the data for
     */
    public void refresh(String rootPath) {

        synchronized (m_lock) {
            try {
                CmsFolderSizeTable newTable = new CmsFolderSizeTable(m_table);
                newTable.updateTree(rootPath);
                newTable.updateSubtreeCache();
                m_table = newTable;
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Reloads the complete folder size information (this is expensive!).
     */
    public void reload() {

        synchronized (m_lock) {
            try {
                CmsFolderSizeTable newTable = new CmsFolderSizeTable(m_table);
                newTable.loadAll();
                newTable.updateSubtreeCache();
                m_table = newTable;
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Adds a modified folder path to be processed.
     * @param parentFolder the folder path
     */
    private void addPath(String parentFolder) {

        try {
            m_todo.add(m_pathCache.get(parentFolder));
        } catch (ExecutionException e) {
            // can't happen
            LOG.error(e.getLocalizedMessage(), e);
        }

    }

    /**
     * Adds a resource that needs to be processed.
     *
     * @param resource the resource to add
     */
    private void addUpdate(CmsPublishedResource resource) {

        if (resource.isFile()) {
            addPath(CmsResource.getParentFolder(resource.getRootPath()));
        } else {
            addPath(resource.getRootPath());
        }
    }

    /**
     * Adds a resource that needs to be processed
     *
     * @param resource the resource to add
     */
    private void addUpdate(CmsResource resource) {

        if (resource.isFile()) {
            addPath(CmsResource.getParentFolder(resource.getRootPath()));
        } else {
            addPath(resource.getRootPath());
        }
    }

    /**
     * Handles CMS events.
     *
     * @param event the event to process
     */
    private void handleEvent(CmsEvent event) {

        CmsResource resource = null;
        List<CmsResource> resources = null;
        if (m_online) {
            switch (event.getType()) {
                case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
                    String publishIdStr = (String)event.getData().get(I_CmsEventListener.KEY_PUBLISHID);
                    if (publishIdStr != null) {
                        CmsUUID publishId = new CmsUUID(publishIdStr);
                        try {
                            List<CmsPublishedResource> publishedResources = m_cms.readPublishedResources(publishId);
                            for (CmsPublishedResource res : publishedResources) {
                                addUpdate(res);
                            }
                        } catch (CmsException e) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }
                    break;
                default:
                    // do nothing
                    break;
            }
        } else {
            List<Object> irrelevantChangeTypes = new ArrayList<Object>();
            irrelevantChangeTypes.add(Integer.valueOf(CmsDriverManager.NOTHING_CHANGED));
            irrelevantChangeTypes.add(Integer.valueOf(CmsDriverManager.CHANGED_PROJECT));
            switch (event.getType()) {
                case I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED:
                case I_CmsEventListener.EVENT_RESOURCE_MODIFIED:
                case I_CmsEventListener.EVENT_RESOURCE_CREATED:
                    Object change = event.getData().get(I_CmsEventListener.KEY_CHANGE);
                    if ((change != null) && irrelevantChangeTypes.contains(change)) {
                        return;
                    }
                    resource = (CmsResource)event.getData().get(I_CmsEventListener.KEY_RESOURCE);
                    addUpdate(resource);
                    break;
                case I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED:
                    resources = CmsCollectionsGenericWrapper.list(
                        event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                    for (CmsResource res : resources) {
                        addUpdate(res);
                    }
                    break;

                case I_CmsEventListener.EVENT_RESOURCE_MOVED:
                    resources = CmsCollectionsGenericWrapper.list(
                        event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                    // source, source folder, dest, dest folder
                    // - OR -
                    // source, dest, dest folder
                    addUpdate(resources.get(0));
                    addUpdate(resources.get(resources.size() - 2));
                    break;

                case I_CmsEventListener.EVENT_RESOURCE_DELETED:
                    resources = CmsCollectionsGenericWrapper.list(
                        event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                    for (CmsResource res : resources) {
                        addUpdate(res);
                    }
                    break;
                case I_CmsEventListener.EVENT_RESOURCES_MODIFIED:
                    resources = CmsCollectionsGenericWrapper.list(
                        event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                    for (CmsResource res : resources) {
                        addUpdate(res);
                    }
                    break;
                case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
                    String publishIdStr = (String)event.getData().get(I_CmsEventListener.KEY_PUBLISHID);
                    if (publishIdStr != null) {
                        CmsUUID publishId = new CmsUUID(publishIdStr);
                        try {
                            List<CmsPublishedResource> publishedResources = m_cms.readPublishedResources(publishId);
                            for (CmsPublishedResource res : publishedResources) {
                                addUpdate(res);
                            }
                        } catch (CmsException e) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }
                    break;
                default:
                    // do nothing
                    break;
            }
        }
    }

}

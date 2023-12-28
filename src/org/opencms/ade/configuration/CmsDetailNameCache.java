/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.configuration;

import org.opencms.db.CmsPublishedResource;
import org.opencms.db.urlname.CmsUrlNameMappingEntry;
import org.opencms.db.urlname.CmsUrlNameMappingFilter;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;

/**
 * A cache which stores structure ids for URL names.<p>
 *
 * <p>Note that this cache may in some cases contain outdated structure ids for URL names, if an URL name has been removed for a content but
 * is not yet mapped to a different content.
 */
public class CmsDetailNameCache implements I_CmsGlobalConfigurationCache {

    /** The delay between updates. */
    public static final int DELAY_MILLIS = 3000;

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDetailNameCache.class);

    /** Request attribute for telling the link substitution handler that it shouldn't use this cache. */
    public static final String ATTR_BYPASS = "bypass-detail-name-cache";

    /** Object used to wait on for updates. */
    private Object m_updateLock = new Object();

    /** The CMS context used by this cache. */
    private CmsObject m_cms;

    /** The internal map from URL names to structure ids. */
    private volatile ConcurrentHashMap<String, CmsUUID> m_detailIdCache = new ConcurrentHashMap<>();

    /** The set of structure ids for which the URL names have to be updated. */
    private LinkedBlockingQueue<CmsUUID> m_changes = new LinkedBlockingQueue<>();

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context to use
     */
    public CmsDetailNameCache(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsGlobalConfigurationCache#clear()
     */
    public void clear() {

        markForUpdate(CmsUUID.getNullUUID());

    }

    /**
     * Gets the structure id for a given URL name.<p>
     *
     * @param name the URL name
     * @return the structure id for the URL name
     */
    public CmsUUID getDetailId(String name) {

        return m_detailIdCache.get(name);
    }

    /**
     * Initializes the cache by scheduling the update actions and loading the initial cache contents.<p>
     */
    public void initialize() {

        OpenCms.getExecutor().scheduleWithFixedDelay(new Runnable() {

            public void run() {

                checkForUpdates();
            }
        }, DELAY_MILLIS, DELAY_MILLIS, TimeUnit.MILLISECONDS);
        reload();
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsGlobalConfigurationCache#remove(org.opencms.db.CmsPublishedResource)
     */
    public void remove(CmsPublishedResource pubRes) {

        checkIfUpdateIsNeeded(pubRes.getStructureId(), pubRes.getRootPath(), pubRes.getType());
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsGlobalConfigurationCache#remove(org.opencms.file.CmsResource)
     */
    public void remove(CmsResource resource) {

        checkIfUpdateIsNeeded(resource.getStructureId(), resource.getRootPath(), resource.getTypeId());

    }

    /**
     * @see org.opencms.ade.configuration.I_CmsGlobalConfigurationCache#update(org.opencms.db.CmsPublishedResource)
     */
    public void update(CmsPublishedResource pubRes) {

        checkIfUpdateIsNeeded(pubRes.getStructureId(), pubRes.getRootPath(), pubRes.getType());

    }

    /**
     * @see org.opencms.ade.configuration.I_CmsGlobalConfigurationCache#update(org.opencms.file.CmsResource)
     */
    public void update(CmsResource resource) {

        checkIfUpdateIsNeeded(resource.getStructureId(), resource.getRootPath(), resource.getTypeId());

    }

    /**
     * Waits until the cache is potentially updated for the next time.
     *
     * <p>This does include updates where there is actually nothing to update.
     */
    public void waitForUpdate() {

        synchronized (m_updateLock) {
            try {
                m_updateLock.wait();
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    /**
     * Checks if any updates are necessary and if so, performs them.<p>
     */
    void checkForUpdates() {

        Set<CmsUUID> updateSet = new HashSet<>();
        m_changes.drainTo(updateSet);
        if (!updateSet.isEmpty()) {
            if (updateSet.contains(CmsUUID.getNullUUID())) {
                LOG.info("Updating detail name cache: reloading...");
                reload();
            } else {
                LOG.info("Updating detail name cache. Number of changed files: " + updateSet.size());
                for (CmsUUID id : updateSet) {
                    Set<String> urlNames = getUrlNames(id);
                    for (String urlName : urlNames) {
                        m_detailIdCache.put(urlName, id);
                    }
                }
            }
        }
        synchronized (m_updateLock) {
            m_updateLock.notifyAll();
        }
    }

    /**
     * Checks if the cache needs to be updated for the resource, and if so, marks the structure id for updating.<p>
     *
     * @param structureId the structure id for the resource
     * @param rootPath the path of the resource
     * @param typeId the resource type id
     */
    private void checkIfUpdateIsNeeded(CmsUUID structureId, String rootPath, int typeId) {

        try {
            I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(typeId);
            if ((resType instanceof CmsResourceTypeXmlContent)
                && !OpenCms.getResourceManager().matchResourceType(
                    CmsResourceTypeXmlContainerPage.RESOURCE_TYPE_NAME,
                    typeId)) {
                markForUpdate(structureId);
            }
        } catch (CmsLoaderException e) {
            // resource type not found, just log an error
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Reads the URL names for the id.<p>
     *
     * @param id the structure id of a resource
     * @return the URL names for the resource
     */
    private Set<String> getUrlNames(CmsUUID id) {

        try {
            CmsUrlNameMappingFilter filter = CmsUrlNameMappingFilter.ALL.filterStructureId(id);
            Set<String> result = m_cms.readUrlNameMappings(filter).stream().map(entry -> entry.getName()).collect(
                Collectors.toSet());
            return result;
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return Collections.emptySet();
        }
    }

    /**
     * Marks the structure id for updating.<p>
     *
     * @param id the structure id to update
     */
    private void markForUpdate(CmsUUID id) {

        m_changes.add(id);
    }

    /**
     * Loads the complete URL name data into the cache.<p>
     */
    private void reload() {

        ConcurrentHashMap<String, CmsUUID> newMap = new ConcurrentHashMap<String, CmsUUID>();
        try {
            List<CmsUrlNameMappingEntry> mappings = m_cms.readUrlNameMappings(CmsUrlNameMappingFilter.ALL);
            LOG.info("Initializing detail name cache with " + mappings.size() + " entries");
            for (CmsUrlNameMappingEntry entry : mappings) {
                newMap.put(entry.getName(), entry.getStructureId());
            }
            m_detailIdCache = newMap;
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }
}

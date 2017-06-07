/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.containerpage.inherited;

import org.opencms.ade.configuration.CmsSynchronizedUpdateSet;
import org.opencms.ade.configuration.I_CmsGlobalConfigurationCache;
import org.opencms.db.CmsPublishedResource;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;

/**
 * A cache class for storing inherited container configurations.<p>
 */
public class CmsContainerConfigurationCache implements I_CmsGlobalConfigurationCache {

    /** Interval used to check for changes. */
    public static final long UPDATE_INTERVAL_MILLIS = 500;

    /** The standard file name for inherited container configurations. */
    public static final String INHERITANCE_CONFIG_FILE_NAME = ".inherited";

    /** UUID used to signal a cache clear. */
    public static final CmsUUID UPDATE_ALL = CmsUUID.getNullUUID();

    /** The logger instance for this class. */
    public static final Log LOG = CmsLog.getLog(CmsContainerConfigurationCache.class);

    /** A flag which indicates whether this cache is initialized. */
    protected boolean m_initialized;

    /** The CMS context used for this cache's VFS operations. */
    private CmsObject m_cms;

    /** The name of this cache, used for testing/debugging purposes. */
    private String m_name;

    /** The current cache state. */
    private volatile CmsContainerConfigurationCacheState m_state = new CmsContainerConfigurationCacheState(
        new ArrayList<CmsContainerConfigurationGroup>());

    /** Future used to cancel an already scheduled task if initialize() is called again. */
    private ScheduledFuture<?> m_taskFuture;

    /** The set of IDs to update. */
    private CmsSynchronizedUpdateSet<CmsUUID> m_updateSet = new CmsSynchronizedUpdateSet<CmsUUID>();

    /**
     * Creates a new cache instance for inherited containers.<p>
     *
     * @param cms the CMS context to use for VFS operations.
     * @param name the name of the cache, for debugging/testing purposes
     *
     * @throws CmsException if something goes wrong
     */
    public CmsContainerConfigurationCache(CmsObject cms, String name)
    throws CmsException {

        m_cms = OpenCms.initCmsObject(cms);
        m_name = name;
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsGlobalConfigurationCache#clear()
     */
    public synchronized void clear() {

        m_updateSet.add(UPDATE_ALL);
    }

    /**
     * Processes all enqueued inheritance container updates.<p>
     */
    public synchronized void flushUpdates() {

        Set<CmsUUID> updateIds = m_updateSet.removeAll();
        if (!updateIds.isEmpty()) {
            if (updateIds.contains(UPDATE_ALL)) {
                initialize();
            } else {
                Map<CmsUUID, CmsContainerConfigurationGroup> groups = loadFromIds(updateIds);
                CmsContainerConfigurationCacheState state = m_state.updateWithChangedGroups(groups);
                m_state = state;
            }
        }
    }

    /**
     * Gets the current cache contents.<p>
     *
     * @return the cache contents
     */
    public CmsContainerConfigurationCacheState getState() {

        return m_state;
    }

    /**
     * Initializes the cache.<p>
     */
    public synchronized void initialize() {

        LOG.info("Initializing inheritance group cache: " + m_name);
        if (m_taskFuture != null) {
            // in case initialize has been called before on this object, cancel the existing task
            m_taskFuture.cancel(false);
            m_taskFuture = null;
        }
        try {
            List<CmsResource> resources = m_cms.readResources(
                "/",
                CmsResourceFilter.IGNORE_EXPIRATION.addRequireType(
                    OpenCms.getResourceManager().getResourceType(
                        CmsResourceTypeXmlContainerPage.INHERIT_CONTAINER_CONFIG_TYPE_NAME)),
                true);
            m_state = new CmsContainerConfigurationCacheState(load(resources).values());
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        Runnable updateAction = new Runnable() {

            public void run() {

                try {
                    flushUpdates();
                } catch (Throwable e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        };
        m_taskFuture = OpenCms.getExecutor().scheduleWithFixedDelay(
            updateAction,
            UPDATE_INTERVAL_MILLIS,
            UPDATE_INTERVAL_MILLIS,
            TimeUnit.MILLISECONDS);
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsGlobalConfigurationCache#remove(org.opencms.db.CmsPublishedResource)
     */
    public void remove(CmsPublishedResource resource) {

        remove(resource.getStructureId(), resource.getRootPath(), resource.getType());
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsGlobalConfigurationCache#remove(org.opencms.file.CmsResource)
     */
    public void remove(CmsResource resource) {

        remove(resource.getStructureId(), resource.getRootPath(), resource.getTypeId());
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsGlobalConfigurationCache#update(org.opencms.db.CmsPublishedResource)
     */
    public void update(CmsPublishedResource resource) {

        update(resource.getStructureId(), resource.getRootPath(), resource.getType(), resource.getState());
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsGlobalConfigurationCache#update(org.opencms.file.CmsResource)
     */
    public void update(CmsResource resource) {

        update(resource.getStructureId(), resource.getRootPath(), resource.getTypeId(), resource.getState());
    }

    /**
     * Returns the base path for a given configuration file.
     *
     * E.g. the result for the input '/sites/default/.container-config' will be '/sites/default'.<p>
     *
     * @param rootPath the root path of the configuration file
     *
     * @return the base path for the configuration file
     */
    protected String getBasePath(String rootPath) {

        if (rootPath.endsWith(INHERITANCE_CONFIG_FILE_NAME)) {
            return rootPath.substring(0, rootPath.length() - INHERITANCE_CONFIG_FILE_NAME.length());
        }
        return rootPath;
    }

    /**
     * Gets the cache key for a given base path.<p>
     *
     * @param basePath the base path
     *
     * @return the cache key for the base path
     */
    protected String getCacheKey(String basePath) {

        assert !basePath.endsWith(INHERITANCE_CONFIG_FILE_NAME);
        return CmsFileUtil.addTrailingSeparator(basePath);
    }

    /**
     * Checks whether a given combination of path and resource type belongs to an inherited container configuration file.<p>
     *
     * @param rootPath the root path of the resource
     * @param type the type id of the resource
     *
     * @return true if the given root path / type combination matches an inherited container configuration file
     */
    protected boolean isContainerConfiguration(String rootPath, int type) {

        return OpenCms.getResourceManager().matchResourceType(
            CmsResourceTypeXmlContainerPage.INHERIT_CONTAINER_CONFIG_TYPE_NAME,
            type)
            && !CmsResource.isTemporaryFileName(rootPath)
            && rootPath.endsWith("/" + INHERITANCE_CONFIG_FILE_NAME);
    }

    /**
     * Loads the inheritance groups from a list of resources.<p>
     *
     * If the configuration for a given resource can't be read, the corresponding map entry will be null in the result.
     *
     * @param resources the resources
     * @return the inheritance group configurations, with structure ids of the corresponding resources as keys
     */
    protected Map<CmsUUID, CmsContainerConfigurationGroup> load(Collection<CmsResource> resources) {

        Map<CmsUUID, CmsContainerConfigurationGroup> result = Maps.newHashMap();
        for (CmsResource resource : resources) {
            CmsContainerConfigurationGroup parsedGroup = null;
            try {
                CmsFile file = m_cms.readFile(resource);
                CmsContainerConfigurationParser parser = new CmsContainerConfigurationParser(m_cms);
                parser.parse(file);
                parsedGroup = new CmsContainerConfigurationGroup(parser.getParsedResults());
                parsedGroup.setResource(resource);
            } catch (CmsVfsResourceNotFoundException e) {
                LOG.debug(e.getLocalizedMessage(), e);
            } catch (Throwable e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            // if the group couldn't be read or parsed for some reason, the map value is null
            result.put(resource.getStructureId(), parsedGroup);
        }
        return result;
    }

    /**
     * Loads the inheritance groups from the resources with structure ids from the given list.<p>
     *
     * If the configuration for a given id can't be read, the corresponding map entry will be null in the result.
     *
     * @param structureIds the structure ids
     * @return the inheritance group configurations, with structure ids of the corresponding resources as keys
     */
    protected Map<CmsUUID, CmsContainerConfigurationGroup> loadFromIds(Collection<CmsUUID> structureIds) {

        Map<CmsUUID, CmsContainerConfigurationGroup> result = Maps.newHashMap();
        for (CmsUUID id : structureIds) {
            CmsContainerConfigurationGroup parsedGroup = null;
            try {
                CmsResource resource = m_cms.readResource(id, CmsResourceFilter.IGNORE_EXPIRATION);
                CmsFile file = m_cms.readFile(resource);
                CmsContainerConfigurationParser parser = new CmsContainerConfigurationParser(m_cms);
                parser.parse(file);
                parsedGroup = new CmsContainerConfigurationGroup(parser.getParsedResults());
                parsedGroup.setResource(resource);
            } catch (CmsVfsResourceNotFoundException e) {
                LOG.debug(e.getLocalizedMessage(), e);
            } catch (Throwable e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            // if the group couldn't be read or parsed for some reason, the map value is null
            result.put(id, parsedGroup);
        }
        return result;
    }

    /**
     * Removes a resource from the cache.<p>
     *
     * @param structureId the structure id of the resource
     * @param rootPath the root path of the resource
     *
     * @param type the resource type
     */
    protected void remove(CmsUUID structureId, String rootPath, int type) {

        if (!isContainerConfiguration(rootPath, type)) {
            return;
        }
        m_updateSet.add(structureId);
    }

    /**
     * Updates a resource in the cache.<p>
     *
     * @param structureId the structure id of the resource
     * @param rootPath the root path of the resource
     * @param type the resource type
     * @param state the resource state
     */
    protected void update(CmsUUID structureId, String rootPath, int type, CmsResourceState state) {

        if (!isContainerConfiguration(rootPath, type)) {
            return;
        }
        m_updateSet.add(structureId);
    }
}

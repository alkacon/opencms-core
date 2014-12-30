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

import org.opencms.ade.configuration.I_CmsGlobalConfigurationCache;
import org.opencms.db.CmsPublishedResource;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * A cache class for storing inherited container configurations.<p>
 */
public class CmsContainerConfigurationCache implements I_CmsGlobalConfigurationCache {

    /** The standard file name for inherited container configurations. */
    public static final String INHERITANCE_CONFIG_FILE_NAME = ".inherited";

    /** The logger instance for this class. */
    public static final Log LOG = CmsLog.getLog(CmsContainerConfigurationCache.class);

    /** A flag which indicates whether this cache is initialized. */
    protected boolean m_initialized;

    /** The CMS context used for this cache's VFS operations. */
    private CmsObject m_cms;

    /** The map of cached configurations, with the base paths as keys. */
    private Map<String, CmsContainerConfigurationGroup> m_configurationsByPath = new HashMap<String, CmsContainerConfigurationGroup>();

    /** The name of this cache, used for testing/debugging purposes. */
    private String m_name;

    /** A map which contains paths and structure ids of configuration files which need to be read to bring the cache to an up-to-date state. */
    private Map<String, CmsUUID> m_needToUpdate = new HashMap<String, CmsUUID>();

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

        m_initialized = false;
        m_needToUpdate.clear();
        m_configurationsByPath.clear();
    }

    /**
     * Gets the container configuration for a given root path, name and locale.<p>
     * 
     * @param rootPath the root path 
     * @param name the configuration name 
     * 
     * @return the container configuration for the given combination of parameters 
     */
    public synchronized CmsContainerConfiguration getContainerConfiguration(String rootPath, String name) {

        readRemainingConfigurations();
        String key = getCacheKey(rootPath);
        if (m_configurationsByPath.containsKey(key)) {
            CmsContainerConfigurationGroup group = m_configurationsByPath.get(key);
            CmsContainerConfiguration result = group.getConfiguration(name);
            return result;
        }
        return null;
    }

    /** 
     * Initializes the cache.<p>
     */
    public synchronized void initialize() {

        m_initialized = false;
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsGlobalConfigurationCache#remove(org.opencms.db.CmsPublishedResource)
     */
    public synchronized void remove(CmsPublishedResource resource) {

        remove(resource.getStructureId(), resource.getRootPath(), resource.getType());
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsGlobalConfigurationCache#remove(org.opencms.file.CmsResource)
     */
    public synchronized void remove(CmsResource resource) {

        remove(resource.getStructureId(), resource.getRootPath(), resource.getTypeId());
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsGlobalConfigurationCache#update(org.opencms.db.CmsPublishedResource)
     */
    public synchronized void update(CmsPublishedResource resource) {

        update(resource.getStructureId(), resource.getRootPath(), resource.getType(), resource.getState());
    }

    /**
     * @see org.opencms.ade.configuration.I_CmsGlobalConfigurationCache#update(org.opencms.file.CmsResource)
     */
    public synchronized void update(CmsResource resource) {

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
     * Checks whethet a given combination of path and resource type belongs to an inherited container configuration file.<p>
     * 
     * @param rootPath the root path of the resource 
     * @param type the type id of the resource
     * 
     * @return true if the given root path / type combination matches an inherited container configuration file 
     */
    protected boolean isContainerConfiguration(String rootPath, int type) {

        try {
            int expectedId = OpenCms.getResourceManager().getResourceType(
                CmsResourceTypeXmlContainerPage.INHERIT_CONTAINER_CONFIG_TYPE_NAME).getTypeId();
            return (type == expectedId)
                && !CmsResource.isTemporaryFileName(rootPath)
                && rootPath.endsWith("/" + INHERITANCE_CONFIG_FILE_NAME);
        } catch (CmsLoaderException e) {
            return false;
        }
    }

    /**
     * Loads a single configuration file into the cache.<p>
     * 
     * @param configResource the configuration resource 
     */
    protected void load(CmsResource configResource) {

        if (!isContainerConfiguration(configResource.getRootPath(), configResource.getTypeId())) {
            return;
        }
        String basePath = getBasePath(configResource.getRootPath());
        try {
            CmsFile file = m_cms.readFile(configResource);
            CmsContainerConfigurationParser parser = new CmsContainerConfigurationParser(m_cms);
            // This log message is needed for the test cases. 
            LOG.trace("inherited-container-cache " + m_name + " load");
            parser.parse(file);
            CmsContainerConfigurationGroup group = new CmsContainerConfigurationGroup(parser.getParsedResults());
            m_configurationsByPath.put(getCacheKey(basePath), group);
            m_needToUpdate.remove(configResource.getRootPath());
        } catch (CmsException e) {
            m_configurationsByPath.remove(getCacheKey(basePath));
            LOG.error(e.getLocalizedMessage(), e);
        } catch (RuntimeException e) {
            m_configurationsByPath.remove(getCacheKey(basePath));
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Reads the configurations needed to make the cache up-to-date.<p>
     */
    protected synchronized void readRemainingConfigurations() {

        if (!m_initialized) {
            LOG.trace("inherited-container-cache " + m_name + " initialize");
            m_configurationsByPath.clear();
            try {
                List<CmsResource> configurationResources = m_cms.readResources(
                    "/",
                    CmsResourceFilter.DEFAULT.addRequireType(safeGetType()),
                    true);
                for (CmsResource configResource : configurationResources) {
                    load(configResource);
                }
                m_initialized = true;
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        } else {
            Map<String, CmsUUID> needToUpdate = new HashMap<String, CmsUUID>(m_needToUpdate);
            for (Map.Entry<String, CmsUUID> entry : needToUpdate.entrySet()) {
                String rootPath = entry.getKey();
                CmsUUID structureId = entry.getValue();
                CmsResource resource = null;
                try {
                    // This log message is needed for the unit tests 
                    LOG.trace("inherited-container-cache " + m_name + " readSingleResource");
                    resource = m_cms.readResource(structureId);
                    load(resource);
                } catch (CmsException e) {
                    String cacheKey = getCacheKey(getBasePath(rootPath));
                    m_configurationsByPath.remove(cacheKey);
                } catch (RuntimeException e) {
                    String cacheKey = getCacheKey(getBasePath(rootPath));
                    m_configurationsByPath.remove(cacheKey);
                }
            }
            m_needToUpdate.clear();
        }
    }

    /** 
     * Removes a resource from the cache.<p>
     * 
     * @param structureId the structure id of the resource 
     * @param rootPath the root path of the resource 
     * 
     * @param type the resource type 
     */
    protected synchronized void remove(CmsUUID structureId, String rootPath, int type) {

        if (!isContainerConfiguration(rootPath, type)) {
            return;
        }
        String basePath = getBasePath(rootPath);
        m_configurationsByPath.remove(basePath);
        m_needToUpdate.remove(rootPath);
    }

    /**
     * Either gets the configuration type id, or returns -1 if the type hasn't been loaded yet.<p>
     * 
     * @return the configuration type id or -1 
     */
    protected int safeGetType() {

        try {
            return OpenCms.getResourceManager().getResourceType(
                CmsResourceTypeXmlContainerPage.INHERIT_CONTAINER_CONFIG_TYPE_NAME).getTypeId();
        } catch (CmsLoaderException e) {
            return -1;
        }
    }

    /**
     * Updates a resource in the cache.<p>
     * 
     * @param structureId the structure id of the resource 
     * @param rootPath the root path of the resource 
     * @param type the resource type 
     * @param state the resource state 
     */
    protected synchronized void update(CmsUUID structureId, String rootPath, int type, CmsResourceState state) {

        if (!isContainerConfiguration(rootPath, type)) {
            return;
        }
        String basePath = getBasePath(rootPath);
        m_configurationsByPath.remove(basePath);
        m_needToUpdate.put(rootPath, structureId);
    }
}

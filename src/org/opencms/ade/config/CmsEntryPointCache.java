/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/ade/config/CmsEntryPointCache.java,v $
 * Date   : $Date: 2011/04/12 11:59:14 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.config;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeFolderExtended;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class for keeping track of and caching sitemap entry points.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsEntryPointCache {

    /**
     * A bean representing an entry point folder, which also contains the properties for that folder.<p>
     * 
     */
    public class EntryPointFolder {

        /** The map of properties of the entry point folder. */
        private Map<String, CmsProperty> m_properties;

        /** The entry point folder resource. */
        private CmsResource m_resource;

        /**
         * Creates a new entry point folder bean.<p>
         * 
         * @param res 
         * @param props
         */
        public EntryPointFolder(CmsResource res, Map<String, CmsProperty> props) {

            m_resource = res;
            m_properties = props;
        }

        /**
         * Returns the properties of the entry point as a map.<p>
         * 
         * @return a map from property names to properties 
         */
        public Map<String, CmsProperty> getProperties() {

            return Collections.unmodifiableMap(m_properties);
        }

        /**
         * Returns the entry point folder resource.<p>
         * 
         * @return the resource of the entry point 
         */
        public CmsResource getResource() {

            return m_resource;
        }

        /**
         * Checks whether this bean actually represents a valid sitemap entry point.<p>
         * 
         * @return true if this bean actually represents a valid entry point 
         */
        public boolean isEntryPoint() {

            return true;
        }

    }

    /** The logger instance for this class. */
    private static final org.apache.commons.logging.Log LOG = CmsLog.getLog(CmsEntryPointCache.class);

    /** True if this an online cache, else false. */
    boolean m_online;

    /** The CMS object to use. */
    private CmsObject m_cms;

    /** The actual entry point cache, mapping from root paths to entry point folder beans. */
    private Map<String, EntryPointFolder> m_entryPoints;

    /**
     * Creates a new instance.<p>
     * 
     * @param cms the CMS context to use.
     * 
     * @param online true if this cache is for the online project 
     */
    public CmsEntryPointCache(CmsObject cms, boolean online) {

        m_cms = cms;
        m_online = online;
    }

    /**
     * Flushes the cache given that the resource passed as a parameter has changed, but only if the resource 
     * is a possible sitemap entry point.<p>
     * 
     * @param res a resource which has changed 
     */
    public void checkFlush(CmsResource res) {

        if (isPotentialEntryPoint(res) || containsEntryPoint(res.getRootPath())) {
            flush();
        }
    }

    /**
     * Flushes the entry point cache.<p>
     */
    public synchronized void flush() {

        LOG.debug("flushing entry point cache (online=" + m_online + ")");
        m_entryPoints = null;
    }

    /**
     * Retursn a list of all entry points.<p>
     * 
     * @param cms the CMS context to use
     *  
     * @return the list of entry points
     * 
     * @throws CmsException if something goes wrong 
     */
    public synchronized List<EntryPointFolder> getEntryPoints(CmsObject cms) throws CmsException {

        readEntryPointsIfNecessary(cms);
        List<EntryPointFolder> result = new ArrayList<EntryPointFolder>();
        for (Map.Entry<String, EntryPointFolder> entry : m_entryPoints.entrySet()) {
            EntryPointFolder entryPoint = entry.getValue();
            result.add(entryPoint);
        }
        return result;
    }

    /**
     * Looks up the entry points below which a given root path lies.<p>
     * 
     * @param cms the current CMS context 
     * @param path the root path for which the entry points should be retrieved 
     * 
     * @return the list of entry points, ordered from "lowest" to "highest" position in the VFS tree
     *  
     * @throws CmsException if something goes wrong 
     */
    public synchronized List<EntryPointFolder> lookup(CmsObject cms, String path) throws CmsException {

        readEntryPointsIfNecessary(cms);
        List<EntryPointFolder> result = lookup(path);
        return result;
    }

    /**
     * Reads the entry points if they haven't already been cached.<p>
     * 
     * @param cms the CMS context 
     * 
     * @throws CmsException if something goes wrong 
     */
    protected void readEntryPointsIfNecessary(CmsObject cms) throws CmsException {

        if (m_entryPoints == null) {
            m_entryPoints = readAll(cms);
        }
    }

    /**
     * Helper method for checking the type of a resource by type name.<p>
     * 
     * @param res the resource to check 
     * @param typeName the type name
     *  
     * @return true if the resource has the type with the given name 
     */
    private boolean checkType(CmsResource res, String typeName) {

        try {
            return getTypeId(typeName) == res.getTypeId();
        } catch (CmsException e) {
            // LOG.error
            return false;
        }
    }

    /**
     * Returns true if the entry point cache contains an entry point with a given root path.<p>
     * 
     * @param rootPath a VFS root path 
     * @return true if the entry point cache contains an entry point with the given root path 
     */
    private boolean containsEntryPoint(String rootPath) {

        return (m_entryPoints != null) && m_entryPoints.containsKey(rootPath);
    }

    /**
     * Creates an entry point folder bean.<p>
     * 
     * @param cms the CMS context to use
     * @param res the resource for which an entry point folder bean should be created 
     * @return the entry point folder bean 
     * 
     * @throws CmsException if something goes wrong 
     */
    private EntryPointFolder createEntry(CmsObject cms, CmsResource res) throws CmsException {

        List<CmsProperty> propList = cms.readPropertyObjects(res, false);
        Map<String, CmsProperty> props = new HashMap<String, CmsProperty>();
        for (CmsProperty prop : propList) {
            props.put(prop.getName(), prop);
        }
        return new EntryPointFolder(res, props);
    }

    /**
     * Helper method for getting the type id of a name.<p>
     * 
     * @param typeName a type name 
     * @return the type id for the name
     *  
     * @throws CmsException if something goes wrong 
     */
    private int getTypeId(String typeName) throws CmsException {

        return OpenCms.getResourceManager().getResourceType(typeName).getTypeId();
    }

    /**
     * Initializes a CMS object for internal use, and copies the project from another CMS object.<p> 
     * 
     * @param cms the CMS context 
     * @return the initialized CMS object 
     * @throws CmsException
     */
    private CmsObject initCmsObject(CmsObject cms) throws CmsException {

        CmsObject resultCms = OpenCms.initCmsObject(m_cms);
        resultCms.getRequestContext().setCurrentProject(cms.getRequestContext().getCurrentProject());
        return resultCms;
    }

    /**
     * Checks whether a given resource is a candidate for an entry point.<p>
     * 
     * @param res the resource to check 
     * @return true if the resource is a potential entry point 
     */
    private boolean isPotentialEntryPoint(CmsResource res) {

        return checkType(res, CmsResourceTypeFolderExtended.TYPE_ENTRY_POINT)
            || (checkType(res, CmsResourceTypeFolder.RESOURCE_TYPE_NAME) && OpenCms.getSiteManager().isSiteRoot(
                res.getRootPath()));

    }

    /**
     * Internal helper method for looking up the entry points for a given path.<p>
     * 
     * @param path the path for which to look up the entry points 
     * 
     * @return the list of entry points 
     */
    private List<EntryPointFolder> lookup(String path) {

        List<EntryPointFolder> result = new ArrayList<EntryPointFolder>();
        String currentPath = path;
        while (!CmsStringUtil.isEmptyOrWhitespaceOnly(currentPath)) {
            EntryPointFolder entry = m_entryPoints.get(currentPath);
            if (entry != null) {
                result.add(entry);
            }
            currentPath = CmsResource.getParentFolder(currentPath);
        }
        return result;
    }

    /**
     * Reads all entry point folders from the VFS and returns them.<p>
     * 
     * @param paramCms the CMS context to use 
     * @return a map of entry point folders
     *  
     * @throws CmsException if something goes wrong 
     */
    private Map<String, EntryPointFolder> readAll(CmsObject paramCms) throws CmsException {

        if (m_online != paramCms.getRequestContext().getCurrentProject().isOnlineProject()) {
            throw new IllegalArgumentException();
        }

        Map<String, EntryPointFolder> entryPoints = new HashMap<String, EntryPointFolder>();

        CmsObject cms = initCmsObject(paramCms);
        Set<CmsResource> entryPointSet = new HashSet<CmsResource>();

        List<CmsResource> entryPointResources = readEntryPointResources(cms);
        entryPointSet.addAll(entryPointResources);
        List<CmsResource> siteEntryPoints = readSiteEntryPoints(cms);
        entryPointSet.addAll(siteEntryPoints);

        for (CmsResource res : entryPointSet) {
            EntryPointFolder entry = createEntry(cms, res);
            if (entry.isEntryPoint()) {
                entryPoints.put(res.getRootPath(), entry);
            }
        }
        return entryPoints;
    }

    /**
     * Reads the resources which actually are of type "entrypoint".<p>
     * 
     * @param cms the CMS Context to use 
     * @return a list of the resources of type "entrypoint" 
     * @throws CmsException if something goes wrong 
     */
    private List<CmsResource> readEntryPointResources(CmsObject cms) throws CmsException {

        return cms.readResources(
            "/",
            CmsResourceFilter.DEFAULT.addRequireType(getTypeId(CmsResourceTypeFolderExtended.TYPE_ENTRY_POINT)));
    }

    /**
     * Reads the site roots (which are potential entry points).<p>
     * 
     * @param cms the CMS context to use 
     * @return the site root resources
     *  
     * @throws CmsException if something goes wrong 
     */
    private List<CmsResource> readSiteEntryPoints(CmsObject cms) throws CmsException {

        List<CmsSite> sites = OpenCms.getSiteManager().getAvailableSites(cms, false);
        List<CmsResource> result = new ArrayList<CmsResource>();
        for (CmsSite site : sites) {
            String root = site.getSiteRoot();
            CmsResource siteRootRes = cms.readResource(root);
            result.add(siteRootRes);
        }
        return result;
    }

}

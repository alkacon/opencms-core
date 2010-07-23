/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsPublishSitemapCache.java,v $
 * Date   : $Date: 2010/07/23 11:51:59 $
 * Version: $Revision: 1.2 $
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
 * For further information about OpenCms, please see th
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.sitemap;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * A sitemap cache which is used for checking the links of resources when publishing.<p>
 * 
 * The problem with checking the links of published resources is that the validity of links to sitemap entries in resources
 * now depends on the *contents* of sitemap resources which are in the same publish list. We thus need to build a sitemap cache
 * that reflects the sitemap structure after publishing before we actually publish the resources.<p>
 * 
 * Rather than just store only the offline sitemap or only the online sitemap, this cache, using data from the publish list,
 * tries to build a sitemap structure that reflects the online sitemap after publishing. It uses both data from online and offline
 * sitemaps to construct this.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsPublishSitemapCache extends CmsSitemapStructureCache {

    /** The log to use (static for performance reasons).<p> */
    private static final Log LOG = CmsLog.getLog(CmsPublishSitemapCache.class);

    /** The offline CMS context. */
    private CmsObject m_offlineCms;

    /** The online CMS context. */
    private CmsObject m_onlineCms;

    /** A map of resources to be published, indexed by their root paths. */
    private Map<String, CmsResource> m_publishResources;

    /** A map of resources to be published, indexed by their structure ids. */
    private Map<CmsUUID, CmsResource> m_publishResourcesById;

    /** The original project from which the resources should be published. */
    private CmsProject m_sourceProject;

    /** The online project. */
    private CmsProject m_targetProject;

    /**
     * Creates a new publish sitemap cache.<p>
     * 
     * @param adminCms an admin CMS context 
     * @param name the name of the sitemap cache
     */
    public CmsPublishSitemapCache(CmsObject adminCms, String name) {

        super(adminCms, null, false, false, name);
    }

    /**
     * Checks whether a relation's target (given by id and path) will be accessible after publishing.<p>  
     * 
     * @param targetId the id of the relation target 
     * @param targetPath the path of the relation target 
     * 
     * @return true if the id or the path are available in this cache object 
     * 
     */
    public boolean checkLink(CmsUUID targetId, String targetPath) {

        try {
            getActiveSitemaps(null);
        } catch (CmsException e) {
            // one reason we can get here is because of deleted sub-sitemaps.
            LOG.warn(e.getLocalizedMessage(), e);
            return false;
        }
        boolean containsId = m_byId.containsKey(targetId);
        boolean containsPath = m_pathSet.contains(targetPath);
        return containsId || containsPath;

    }

    /**
     * Initializes the publish sitemap cache.<p>
     * 
     * @param sourceProject the project from which the resources are going to be published
     * @param targetProject the project to which the resources are going to be published   
     * @param publishResources a map from VFS paths to resources which will be published 
     * 
     * @throws CmsException if something goes wrong 
     */
    public void init(CmsProject sourceProject, CmsProject targetProject, Map<String, CmsResource> publishResources)
    throws CmsException {

        initProjects(sourceProject, targetProject);
        initPublishResources(publishResources);
        initCmsObjects();
    }

    /**
     * Internal method for creating either a CMS context for the offline or online project depending on a flag.
     * 
     * @param online if true, an online CMS context is created, else an offline CMS context 
     * 
     * @return the online or offline CMS context 
     * 
     * @throws CmsException if something goes wrong 
     */
    protected CmsObject createCmsObject(boolean online) throws CmsException {

        CmsObject cms = OpenCms.initCmsObject(m_adminCms);
        cms.getRequestContext().setSiteRoot("/");
        if (online) {
            cms.getRequestContext().setCurrentProject(m_targetProject);
        } else {
            cms.getRequestContext().setCurrentProject(m_sourceProject);
        }
        return cms;
    }

    /** 
     * Returns either a CmsObject for the online project, or a CmsObject for an offline project, depending on a flag.<p>
     * 
     * @param online if true, return the online CmsObject, else the offline CmsObject
     *  
     * @return a CmsObject
     */
    protected CmsObject getCmsObject(boolean online) {

        return online ? m_onlineCms : m_offlineCms;
    }

    /**
     * Returns either a CmsObject for the online project, or a CmsObject for an offline project,
     * depending on whether the structure id passed as the parameter belongs to a resource which is going to be published.<p>
     * 
     * @param structureId the structure id of a resource 
     * 
     * @return the offline CmsObject if the resource is going to be published, else the online CmsObject 
     */
    protected CmsObject getCmsObject(CmsUUID structureId) {

        return getCmsObject(!m_publishResourcesById.containsKey(structureId));
    }

    /**
     * @see org.opencms.xml.sitemap.CmsSitemapStructureCache#internalCreateCmsObject(org.opencms.file.CmsObject)
     */
    @Override
    protected CmsObject internalCreateCmsObject(CmsObject cms) {

        // we only use the internal CmsObjects, so we just return null here 
        return null;
    }

    /**
    * @see org.opencms.xml.sitemap.CmsSitemapStructureCache#internalGetEntryPointResources(org.opencms.file.CmsObject)
    */
    @Override
    protected List<CmsResource> internalGetEntryPointResources(CmsObject adminCms) throws CmsException {

        CmsObject onlineCms = getCmsObject(true);
        CmsObject offlineCms = getCmsObject(false);
        List<CmsResource> onlineEntryPoints = readEntryPointResources(onlineCms);
        List<CmsResource> offlineEntryPoints = readEntryPointResources(offlineCms);
        List<CmsResource> result = new ArrayList<CmsResource>(onlineEntryPoints);
        /*
         * First we remove all resources which are going to be deleted from the result, then we add all resources which
         * will be changed/created to the result. 
         */
        Iterator<CmsResource> resourceIterator = result.iterator();
        while (resourceIterator.hasNext()) {
            CmsResource onlineResource = resourceIterator.next();
            if (willBeDeleted(onlineResource.getStructureId())) {
                resourceIterator.remove();
            }
        }
        for (CmsResource res : offlineEntryPoints) {
            if (m_publishResourcesById.containsKey(res.getStructureId())
                && !res.getState().equals(CmsResourceState.STATE_DELETED)
                && !result.contains(res)) {
                result.add(res);
            }
        }
        return result;

    }

    /**
     * @see org.opencms.xml.sitemap.CmsSitemapStructureCache#internalGetSitemap(org.opencms.file.CmsObject, org.opencms.xml.sitemap.CmsXmlSitemap, java.util.Locale)
     */
    @Override
    protected CmsSitemapBean internalGetSitemap(CmsObject adminCms, CmsXmlSitemap xmlSitemap, Locale locale) {

        CmsObject cmsToUse = getCmsObject(xmlSitemap.getFile().getStructureId());
        return super.internalGetSitemap(cmsToUse, xmlSitemap, locale);
    }

    /**
     * @see org.opencms.xml.sitemap.CmsSitemapStructureCache#internalReadResource(org.opencms.file.CmsObject, java.lang.String)
     */
    @Override
    protected CmsResource internalReadResource(CmsObject cms, String subSitemapId) throws CmsException {

        CmsUUID uuid = new CmsUUID(subSitemapId);
        CmsObject cmsToUse = getCmsObject(uuid);
        return super.internalReadResource(cmsToUse, subSitemapId);
    }

    /**
     * @see org.opencms.xml.sitemap.CmsSitemapStructureCache#internalReadSitemapFile(org.opencms.file.CmsObject, org.opencms.file.CmsResource, java.lang.String)
     */
    @Override
    protected CmsFile internalReadSitemapFile(CmsObject cms, CmsResource entryPoint, String sitemapPath)
    throws CmsException {

        CmsObject cmsToUse = getCmsObject(entryPoint.getStructureId());
        return super.internalReadSitemapFile(cmsToUse, entryPoint, sitemapPath);
    }

    /**
     * @see org.opencms.xml.sitemap.CmsSitemapStructureCache#internalReadSitemapProperty(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    @Override
    protected String internalReadSitemapProperty(CmsObject adminCms, CmsResource entryPoint) throws CmsException {

        CmsObject cmsToUse = getCmsObject(entryPoint.getStructureId());
        return super.internalReadSitemapProperty(cmsToUse, entryPoint);
    }

    /**
     * @see org.opencms.xml.sitemap.CmsSitemapStructureCache#internalUnmarshalSitemapFile(org.opencms.file.CmsObject, org.opencms.file.CmsFile)
     */
    @Override
    protected CmsXmlSitemap internalUnmarshalSitemapFile(CmsObject adminCms, CmsFile sitemapFile) throws CmsException {

        CmsObject cmsToUse = getCmsObject(sitemapFile.getStructureId());
        return super.internalUnmarshalSitemapFile(cmsToUse, sitemapFile);
    }

    /**
     * @see org.opencms.xml.sitemap.CmsSitemapStructureCache#internalUnmarshalSitemapFile(org.opencms.file.CmsObject, org.opencms.file.CmsFile)
     */
    @Override
    protected CmsXmlSitemap internalUnmarshalSitemapResource(CmsObject cms, CmsResource subSitemap) throws CmsException {

        CmsObject cmsToUse = getCmsObject(subSitemap.getStructureId());

        CmsXmlSitemap sitemapXml = CmsXmlSitemapFactory.unmarshal(cmsToUse, subSitemap);
        return sitemapXml;
    }

    /**
     * Helper method for reading the folders which are entry points for sitemaps.<p>
     * 
     * @param cms the CMS context
     *  
     * @return the list of entry point folders 
     * 
     * @throws CmsException if something goes wrong 
     */
    protected List<CmsResource> readEntryPointResources(CmsObject cms) throws CmsException {

        return cms.readResourcesWithProperty(
            "/",
            CmsPropertyDefinition.PROPERTY_ADE_SITEMAP,
            null,
            CmsResourceFilter.IGNORE_EXPIRATION.addRequireFolder());
    }

    /**
     * Checks whether the resource with the given structure id will be deleted in the online project by publishing.<p>
     * 
     * @param structureId the structure id to check
     *  
     * @return true if publishing will remove the resource in the online project  
     */
    protected boolean willBeDeleted(CmsUUID structureId) {

        CmsResource resource = m_publishResourcesById.get(structureId);
        return (resource != null) && resource.getState().equals(CmsResourceState.STATE_DELETED);
    }

    /** 
     * Initializes the Cms contexts for this object. 
     * 
     * @throws CmsException if something goes wrong 
     */
    private void initCmsObjects() throws CmsException {

        m_onlineCms = createCmsObject(true);
        m_offlineCms = createCmsObject(false);
    }

    /** 
     * Initializes the projects for this object. 
     * 
     * @param sourceProject the original project
     * @param targetProject the target project  
     * 
     */
    private void initProjects(CmsProject sourceProject, CmsProject targetProject) {

        m_sourceProject = sourceProject;
        m_targetProject = targetProject;
    }

    /**
     * Initializes the internal maps of resources to be published.<p>
     * 
     * @param resources the map of resources to be published indexed by root paths 
     */
    private void initPublishResources(Map<String, CmsResource> resources) {

        m_publishResources = resources;
        Map<CmsUUID, CmsResource> result = new HashMap<CmsUUID, CmsResource>();
        for (Map.Entry<String, CmsResource> entry : m_publishResources.entrySet()) {
            CmsResource value = entry.getValue();
            result.put(value.getStructureId(), value);
        }
        m_publishResourcesById = result;
    }

}

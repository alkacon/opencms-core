/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/Attic/CmsJspSitemapNavBuilder.java,v $
 * Date   : $Date: 2010/02/02 10:06:18 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.sitemap.CmsSiteEntryBean;
import org.opencms.xml.sitemap.CmsSitemapManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Bean to provide a convenient way to build navigation structures based on sitemap files.<p>
 *
 * This class provides the same functionally as {@link CmsJspNavBuilder} but based on the sitemap
 * files instead of the folder structure.<p>
 *
 * @author  Michael Moossen 
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 7.9.2 
 * 
 * @see org.opencms.jsp.CmsJspNavBuilder
 * @see org.opencms.jsp.CmsJspNavElement
 */
public class CmsJspSitemapNavBuilder extends CmsJspNavBuilder {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspSitemapNavBuilder.class);

    /** The sitemap manager. */
    protected CmsSitemapManager m_manager;

    /** The current request. */
    protected HttpServletRequest m_request;

    /**
     * Empty constructor, so that this bean can be initialized from a JSP.<p> 
     */
    public CmsJspSitemapNavBuilder() {

        // empty
    }

    /**
     * Default constructor.<p>
     * 
     * @param cms context provider for the current request
     * @param req the current request
     */
    public CmsJspSitemapNavBuilder(CmsObject cms, HttpServletRequest req) {

        init(cms, req);
    }

    /**
     * @see org.opencms.jsp.CmsJspNavBuilder#getNavigationBreadCrumb(java.lang.String, int, int, boolean)
     */
    @Override
    public List<CmsJspNavElement> getNavigationBreadCrumb(
        String folder,
        int startlevel,
        int endlevel,
        boolean currentFolder) {

        // be sure to remove the current (last) entry from the list
        // since all URIs are 'folders'
        List<CmsJspNavElement> elements = super.getNavigationBreadCrumb(folder, startlevel, endlevel, currentFolder);
        if (!elements.isEmpty()) {
            int lastIndex = elements.size() - 1;
            CmsJspNavElement last = elements.get(lastIndex);
            if (last.getResourceName().equals(m_requestUri)) {
                elements.remove(lastIndex);
            }
        }
        return elements;
    }

    /**
     * @see org.opencms.jsp.CmsJspNavBuilder#getNavigationForFolder(java.lang.String)
     */
    @Override
    public List<CmsJspNavElement> getNavigationForFolder(String folder) {

        folder = CmsResource.getFolderPath(folder);
        List<CmsJspNavElement> result = new ArrayList<CmsJspNavElement>();

        CmsSiteEntryBean folderEntry = null;
        try {
            folderEntry = m_manager.getEntryForUri(m_cms, folder);
        } catch (CmsException e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
        }
        if (folderEntry == null) {
            return result;
        }
        if (folderEntry.isVfs()) {
            return super.getNavigationForFolder(folder);
        }

        List<CmsSiteEntryBean> entries = folderEntry.getSubEntries();
        for (CmsSiteEntryBean entry : entries) {
            try {
                // check permissions
                m_cms.readResource(entry.getResourceId());
                // permissions are fine, add it to the results
                String entryName = folderEntry.getSitePath(m_cms) + entry.getName() + "/";
                CmsJspNavElement element = getNavigationForResource(entryName);
                if ((element != null) && element.isInNavigation()) {
                    result.add(element);
                }
            } catch (Exception e) {
                // not enough permissions
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }

        Collections.sort(result);
        return result;
    }

    /**
     * @see org.opencms.jsp.CmsJspNavBuilder#getNavigationForResource(java.lang.String)
     */
    @Override
    public CmsJspNavElement getNavigationForResource(String resource) {

        CmsSiteEntryBean uriEntry;
        try {
            uriEntry = m_manager.getEntryForUri(m_cms, resource);
        } catch (CmsException e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
        if (uriEntry.isVfs()) {
            return super.getNavigationForResource(resource);
        }
        return getNavigationForSiteEntry(resource, uriEntry);
    }

    /**
     * @see org.opencms.jsp.CmsJspNavBuilder#init(org.opencms.file.CmsObject)
     */
    @Override
    public void init(CmsObject cms) {

        // prevent the usage of this method
        throw new RuntimeException();
    }

    /**
     * Initializes this bean.<p>
     * 
     * @param cms the current cms context
     * @param req the current request
     */
    public void init(CmsObject cms, HttpServletRequest req) {

        m_request = req;
        m_cms = cms;
        m_manager = OpenCms.getSitemapManager();
        CmsSiteEntryBean sitemap = m_manager.getRuntimeInfo(req);
        if (sitemap != null) {
            m_requestUri = sitemap.getSitePath(m_cms);
        }
        // can be null if m_requestUri is the root folder
        m_requestUriFolder = m_requestUri;
    }

    /**
     * Returns a new navigation element for the given URI.<p>
     * 
     * @param uri the actual URI of the sitemap entry
     * @param entry the sitemap entry 
     *              
     * @return a navigation element for the given sitemap entry
     */
    protected CmsJspNavElement getNavigationForSiteEntry(String uri, CmsSiteEntryBean entry) {

        int level = CmsResource.getPathLevel(uri);
        if (uri.endsWith("/")) {
            level--;
        }
        // fill some properties with some meaningful values
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(CmsPropertyDefinition.PROPERTY_TITLE, entry.getTitle());
        // check if the current entry is in the navigation
        // inheriting parents properties
        boolean isInNavigation = Boolean.parseBoolean(entry.getProperties(true).get(
            CmsSitemapManager.Property.navigation.name()));
        if (isInNavigation) {
            // navText and navPos determine if an entry is in the navigation
            properties.put(CmsPropertyDefinition.PROPERTY_NAVTEXT, entry.getTitle());
        }
        // put all the rest
        properties.putAll(entry.getProperties(false));
        if (!isInNavigation) {
            // navPos is always set so we have to remove it here
            properties.remove(CmsPropertyDefinition.PROPERTY_NAVPOS);
        }
        return new CmsJspNavElement(uri, properties, level);
    }
}

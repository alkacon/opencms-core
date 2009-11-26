/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/Attic/CmsJspSitemapNavBuilder.java,v $
 * Date   : $Date: 2009/11/26 11:37:21 $
 * Version: $Revision: 1.2 $
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
import org.opencms.xml.sitemap.CmsSiteEntryBean;
import org.opencms.xml.sitemap.CmsSitemapResourceHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Bean to provide a convenient way to build navigation structures based on sitemap files.<p>
 *
 * This class provides the same functionally as {@link CmsJspNavBuilder} but based on the sitemap
 * files instead of the folder structure.<p>
 *
 * @author  Michael Moossen 
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 7.9.2 
 * 
 * @see org.opencms.jsp.CmsJspNavBuilder
 * @see org.opencms.jsp.CmsJspNavElement
 */
public class CmsJspSitemapNavBuilder extends CmsJspNavBuilder {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspSitemapNavBuilder.class);

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
     */
    public CmsJspSitemapNavBuilder(CmsObject cms) {

        super(cms);
    }

    /**
     * Collect all navigation elements from the files in the given folder.<p>
     *
     * @param folder the selected folder
     * 
     * @return A sorted (ascending to navigation position) list of navigation elements
     */
    @Override
    public List<CmsJspNavElement> getNavigationForFolder(String folder) {

        folder = CmsResource.getFolderPath(folder);
        List<CmsJspNavElement> result = new ArrayList<CmsJspNavElement>();

        CmsSiteEntryBean folderEntry = null;
        try {
            folderEntry = CmsSitemapResourceHandler.getInstance().getUri(m_cms, folder);
        } catch (CmsException e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
        }
        if (folderEntry == null) {
            return result;
        }

        int position = 0;
        List<CmsSiteEntryBean> entries = folderEntry.getSubEntries();
        for (CmsSiteEntryBean entry : entries) {
            try {
                // check permissions
                m_cms.readResource(entry.getResourceId());
                // permissions are fine, add it to the results
                entry.setPosition(position);
                CmsJspNavElement element = getNavigationForSiteEntry(folder + entry.getName(), entry);
                if ((element != null) && element.isInNavigation()) {
                    result.add(element);
                }
            } catch (Exception e) {
                // not enough permissions
                LOG.debug(e.getLocalizedMessage(), e);
            }
            position++;
        }

        Collections.sort(result);
        return result;
    }

    /**
     * Returns a navigation element for the named resource.<p>
     * 
     * @param resource the resource name to get the navigation information for, 
     *              must be a full path name, e.g. "/docs/index.html"
     *              
     * @return a navigation element for the given resource
     */
    @Override
    public CmsJspNavElement getNavigationForResource(String resource) {

        CmsSiteEntryBean uriEntry;
        try {
            uriEntry = CmsSitemapResourceHandler.getInstance().getUri(m_cms, resource);
        } catch (CmsException e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }
        return getNavigationForSiteEntry(resource, uriEntry);
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
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(CmsPropertyDefinition.PROPERTY_NAVTEXT, entry.getTitle());
        properties.put(CmsPropertyDefinition.PROPERTY_TITLE, entry.getTitle());
        properties.putAll(entry.getProperties());
        return new CmsJspNavElement(uri, properties, level);
    }
}

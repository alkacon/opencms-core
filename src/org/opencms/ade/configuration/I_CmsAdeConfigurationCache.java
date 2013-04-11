/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;

import java.util.List;
import java.util.Set;

/**
 * Interface for the ADE configuration cache.<p>
 */
interface I_CmsAdeConfigurationCache extends I_CmsGlobalConfigurationCache {

    /**
     * Gets all detail page info beans which are defined anywhere in the configuration.<p>
     * 
     * @return the list of detail page info beans 
     */
    List<CmsDetailPageInfo> getAllDetailPages();

    /**
     * Gets all the detail pages for a given type.<p>
     * 
     * @param type the name of the type 
     * 
     * @return the detail pages for that type 
     */
    List<String> getDetailPages(String type);

    /**
     * Gets the set of type names for which detail pages are configured in any sitemap configuration.<p>
     * 
     * @return the set of type names with configured detail pages  
     */
    Set<String> getDetailPageTypes();

    /**
     * Gets the merged module configuration.<p>
     * @return the merged module configuration instance
     */
    CmsADEConfigData getModuleConfiguration();

    /**
     * Helper method to retrieve the parent folder type or <code>null</code> if none available.<p>
     * 
     * @param rootPath the path of a resource 
     * @return the parent folder content type 
     */
    String getParentFolderType(String rootPath);

    /**
     * Looks up the root path for a given structure id.<p>
     *
     * This is used for correcting the paths of cached resource objects.<p>
     * 
     * @param structureId the structure id 
     * @return the root path for the structure id
     * 
     * @throws CmsException if the resource with the given id was not found or another error occurred 
     */
    String getPathForStructureId(CmsUUID structureId) throws CmsException;

    /**
     * Helper method for getting the best matching sitemap configuration object for a given root path, ignoring the module 
     * configuration.<p>
     * 
     * For example, if there are configurations available for the paths /a, /a/b/c, /a/b/x and /a/b/c/d/e, then 
     * the method will return the configuration object for /a/b/c when passed the path /a/b/c/d.
     * 
     * If no configuration data is found for the path, null will be returned.<p> 
     * 
     * @param path a root path  
     * @return the configuration data for the given path, or null if none was found 
     */
    CmsADEConfigData getSiteConfigData(String path);

    /**
     * Initializes the cache by reading in all the configuration files.<p>
     */
    void initialize();

    /**
     * Checks whether the given resource is configured as a detail page.<p>
     * 
     * @param cms the current CMS context  
     * @param resource the resource to test 
     * @param cached if true, try to look up the result in a cache first 
     * 
     * @return true if the resource is configured as a detail page 
     */
    boolean isDetailPage(CmsObject cms, CmsResource resource, boolean cached);

}
/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.shared;

import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.util.CmsUUID;

import java.util.Map;

/**
 * The interface to the sitemap controller.<p>
 * 
 * This interface allows classes which are shared between client and server to access client-only functionality without
 * statically depending on client-only code.<p> 
 * 
 * @since 8.0.0
 */
public interface I_CmsSitemapController {

    /**
     * Gets the property map for the given id.<p>
     * 
     * @param id a structure id 
     * 
     * @return the property map for that structure id 
     */
    Map<String, CmsClientProperty> getPropertiesForId(CmsUUID id);

    /**
     * Registers the given entry within the data model.<p>
     * 
     * @param entry the entry to register
     */
    void registerEntry(CmsClientSitemapEntry entry);

    /**
     * Registers the change of the sitepath with the given controller.<p>
     * 
     * @param entry the sitemap entry
     * @param oldPath the old path
     */
    void registerPathChange(CmsClientSitemapEntry entry, String oldPath);

    /**
     * This method is used to establish a unique property map object for each id, but replaces the contents of the 
     * map object with new values for each call.<p>
     * 
     * The first call to the method with a given id will just return the map passed in. The n-th call to the method
     * with a given id will return the map object passed in with the first method call for that id, but with its contents
     * replaced by the contents of the map passed in with the n-th call for that id.<p>
     * 
     * The purpose of this is to avoid multiple redundant copies of the same logical map of properties being stored
     * in multiple places.<p>
     * 
     * @param id the map identifying the resource to which the properties belong 
     * @param properties the new properties for the given id
     *  
     * @return the original properties object for the given id, but with its contents replaced
     */
    Map<String, CmsClientProperty> replaceProperties(CmsUUID id, Map<String, CmsClientProperty> properties);
}

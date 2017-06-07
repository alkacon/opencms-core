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

package org.opencms.ui.components.fileselect;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vaadin.data.Container;
import com.vaadin.data.Item;

/**
 * Filter used to hide folder tree items which are not either navigation items themselves or are required to navigate
 * from the site root to a navigation item.<p>
 */
public class CmsNavigationFilter implements Container.Filter {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Paths included by the filter. */
    private List<String> m_navigationPaths = Lists.newArrayList();

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context
     * @param root the root folder
     * @throws CmsException if something goes wrong
     */
    public CmsNavigationFilter(CmsObject cms, CmsResource root)
    throws CmsException {

        CmsObject rootCms = OpenCms.initCmsObject(cms);
        rootCms.getRequestContext().setSiteRoot("");
        Set<CmsResource> navResources = Sets.newHashSet(
            rootCms.readResourcesWithProperty(CmsPropertyDefinition.PROPERTY_NAVTEXT));
        navResources.addAll(rootCms.readResourcesWithProperty(CmsPropertyDefinition.PROPERTY_NAVPOS));
        for (CmsResource res : navResources) {
            if (!res.getRootPath().startsWith("/system/workplace")) {
                // navigation properties are used for configuration on some workplace resources, so don't include those
                m_navigationPaths.addAll(getAncestorPaths(res.getRootPath()));
            }
        }
    }

    /**
     * @see com.vaadin.data.Container.Filter#appliesToProperty(java.lang.Object)
     */
    public boolean appliesToProperty(Object propertyId) {

        return false;
    }

    /**
     * @see com.vaadin.data.Container.Filter#passesFilter(java.lang.Object, com.vaadin.data.Item)
     */
    public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {

        CmsResource res = (CmsResource)item.getItemProperty(CmsResourceTreeContainer.PROPERTY_RESOURCE).getValue();
        if (res == null) {
            return true;
        }
        return m_navigationPaths.contains(res.getRootPath());
    }

    /**
     * Calculates all ancestor paths for a given path.<p>
     *
     * @param path the path
     * @return the list of ancestor paths
     */
    List<String> getAncestorPaths(String path) {

        List<String> result = Lists.newArrayList();
        while (path != null) {
            result.add(path);
            path = CmsResource.getParentFolder(path);
        }
        return result;
    }

}

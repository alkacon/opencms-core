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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.types;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.file.CmsResource;

import java.util.ArrayList;
import java.util.List;

/**
 * Resource type descriptor for sub site map folder types.<p>
 *
 * This type extends an extended folder with a configurable type id and type name.<p>
 *
 * @since 8.5.0
 */
public class CmsResourceTypeFolderSubSitemap extends CmsResourceTypeFolderExtended {

    /** The registered sub site map resource type id's.    */
    private static List<Integer> m_subSitemapResourceTypeIds = new ArrayList<Integer>();

    /** The type name for subsitemaps. */
    public static final String TYPE_SUBSITEMAP = "subsitemap";

    /**
     * Returns the registered sub site map resource type id's.<p>
     *
     * @return the resource type id's
     */
    public static List<Integer> getSubSitemapResourceTypeIds() {

        return m_subSitemapResourceTypeIds;
    }

    /**
     * Returns <code>true</code> in case the given resource is a sub site map.<p>
     *
     * Internally this checks if the given resource type has an id that is registered as a sub site map resource type.<p>
     *
     * @param resource the resource to check
     *
     * @return <code>true</code> in case the given resource is a sub site map
     *
     * @since 8.0.0
     */
    public static boolean isSubSitemap(CmsResource resource) {

        return resource == null ? false : isSubSitemapTypeId(resource.getTypeId());
    }

    /**
     * Returns <code>true</code> in case the given resource type id is a sub site map type.<p>
     *
     * Internally this checks if the given resource type id is registered as a sub site map resource type.<p>
     *
     * @param typeId the resource type id to check
     *
     * @return <code>true</code> in case the given resource type id is a sub site map type
     *
     * @since 8.0.0
     */
    public static boolean isSubSitemapTypeId(int typeId) {

        return m_subSitemapResourceTypeIds.contains(Integer.valueOf(typeId));
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#initConfiguration(java.lang.String, java.lang.String, String)
     */
    @Override
    public void initConfiguration(String name, String id, String className) throws CmsConfigurationException {

        super.initConfiguration(name, id, className);
        // set static members with values from the configuration
        addTypeId(m_typeId);
    }

    /**
     * Adds another resource type id to the registered sub site map resource type id's.<p>
     *
     * @param typeId the resource type id to add
     */
    private void addTypeId(int typeId) {

        m_subSitemapResourceTypeIds.add(Integer.valueOf(typeId));
    }

}
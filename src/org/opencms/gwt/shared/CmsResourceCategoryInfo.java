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

package org.opencms.gwt.shared;

import org.opencms.util.CmsUUID;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Category information for a specific resource.<p>
 */
public class CmsResourceCategoryInfo implements IsSerializable {

    /** The category tree. */
    private List<CmsCategoryTreeEntry> m_categoryTree;

    /** The current resource categories. */
    private List<String> m_currentCategories;

    /** The resource info bean. */
    private CmsListInfoBean m_resourceInfo;

    /** The resource structure id. */
    private CmsUUID m_structureId;

    /**
     * Constructor.<p>
     *
     * @param structureId the resource structure id
     * @param resourceInfo the resource info bean
     * @param currentCategories the current resource categories
     * @param categoryTree the category tree
     */
    public CmsResourceCategoryInfo(
        CmsUUID structureId,
        CmsListInfoBean resourceInfo,
        List<String> currentCategories,
        List<CmsCategoryTreeEntry> categoryTree) {

        m_structureId = structureId;
        m_resourceInfo = resourceInfo;
        m_currentCategories = currentCategories;
        m_categoryTree = categoryTree;
    }

    /**
     * Constructor, for serialization only.<p>
     */
    protected CmsResourceCategoryInfo() {

        // nothing to do
    }

    /**
     * Returns the category tree.<p>
     *
     * @return the category tree
     */
    public List<CmsCategoryTreeEntry> getCategoryTree() {

        return m_categoryTree;
    }

    /**
     * Returns the current resource categories.<p>
     *
     * @return the current resource categories
     */
    public List<String> getCurrentCategories() {

        return m_currentCategories;
    }

    /**
     * Returns the resource info bean.<p>
     *
     * @return the resource info bean
     */
    public CmsListInfoBean getResourceInfo() {

        return m_resourceInfo;
    }

    /**
     * Returns the resource structure id.<p>
     *
     * @return the resource structure id
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }
}

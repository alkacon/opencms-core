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

package org.opencms.ade.sitemap.client;

import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsUUID;

/**
 * Widget representing a category in the category mode of the sitemap editor.<p>
 */
public class CmsCategoryTreeItem extends CmsTreeItem {

    /** Structure id of the category. */
    private CmsUUID m_structureId;

    /**
     * Creates a new tree item.<p>
     *
     * @param entry the data for the tree item
     */
    public CmsCategoryTreeItem(CmsCategoryTreeEntry entry) {

        super(true, new CmsListItemWidget(createCategoryListInfo(entry)));
        m_structureId = entry.getId();
    }

    /**
     * Creates the list info bean for a tree item from a category bean.<p>
     *
     * @param entry the category data
     *
     * @return the list info bean
     */
    public static CmsListInfoBean createCategoryListInfo(CmsCategoryTreeEntry entry) {

        CmsListInfoBean info = new CmsListInfoBean(entry.getTitle(), entry.getPath(), null);
        info.setResourceType("category");
        return info;
    }

    /**
     * Gets the structure id.<p>
     *
     * @return the structure id
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }
}

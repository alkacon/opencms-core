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

package org.opencms.ade.sitemap.client.ui;

import org.opencms.ade.sitemap.shared.CmsNewResourceInfo;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.util.CmsUUID;

/**
 * A list item widget class which also contains a resource type info bean, for use in creating new sitemap entries.<p>
 *
 * @since 8.0.0
 */
public class CmsCreatableListItem extends CmsListItem {

    /** The types of creatable sitemap entries. */
    public enum NewEntryType {
        /** A detail page. */
        detailpage, /** A redirect entry. */
        redirect, /** A regular entry. */
        regular
    }

    /** The sitemap entry type to create. */
    private NewEntryType m_newEntryType;

    /** The resource type info bean. */
    private CmsNewResourceInfo m_typeInfo;

    /**
     * Creates a new list item with a given resource type info bean.<p>
     *
     * @param content the content for the list item widget
     * @param typeInfo the resource type info bean
     * @param newEntryType the type of the creatable sitemap entry type
     */
    public CmsCreatableListItem(CmsListItemWidget content, CmsNewResourceInfo typeInfo, NewEntryType newEntryType) {

        super(content);
        m_typeInfo = typeInfo;
        m_newEntryType = newEntryType;
    }

    /**
     * Returns the copy resource structure id.<p>
     *
     * @return the copy resource structure id
     */
    public CmsUUID getCopyResourceId() {

        return m_typeInfo.getCopyResourceId();
    }

    /**
     * Returns the new sitemap entry type.<p>
     *
     * @return the new sitemap entry type
     */
    public NewEntryType getNewEntryType() {

        return m_newEntryType;
    }

    /**
     * Returns the resource type information bean.<p>
     *
     * @return the resource type info bean
     */
    public CmsNewResourceInfo getResourceTypeInfo() {

        return m_typeInfo;
    }

    /**
     * Returns the resource type id.<p>
     *
     * @return the resource type id
     */
    public int getTypeId() {

        return m_typeInfo.getId();
    }

    /**
     * Returns the resource type name.<p>
     *
     * @return the resource type name
     */
    public String getTypeName() {

        return m_typeInfo.getTypeName();
    }
}

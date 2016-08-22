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

package org.opencms.widgets.dataview.example;

import org.opencms.widgets.dataview.I_CmsDataViewItem;

/**
 * Simple data item implementation.<p>
 */
public class CmsExampleDataItem implements I_CmsDataViewItem {

    /** The id. */
    private String m_id;

    /** The title. */
    private String m_title;

    /** The description. */
    private String m_description;

    /** The category. */
    private String m_category;

    /** The size. */
    private int m_size;

    /** The isGood flag. */
    private boolean m_isGood;

    /** The image URL. */
    private String m_image;

    /**
     * Creates a new instance.<p>
     *
     * @param id the id
     * @param title initial attribute value
     * @param description initial attribute value
     * @param size initial attribute value
     * @param isGood initial attribute value
     * @param image initial attribute value
     * @param category the category
     */
    public CmsExampleDataItem(
        String id,
        String title,
        String description,
        int size,
        boolean isGood,
        String image,
        String category) {
        m_id = id;
        m_title = title;
        m_description = description;
        m_size = size;
        m_isGood = isGood;
        m_image = image;
        m_category = category;
    }

    /**
     * @see org.opencms.widgets.dataview.I_CmsDataViewItem#getColumnData(java.lang.String)
     */
    public Object getColumnData(String column) {

        switch (column) {
            case "id":
                return getId();
            case "title":
                return getTitle();
            case "description":
                return getDescription();
            case "size":
                return new Double(m_size);
            case "good":
                return new Boolean(m_isGood);
            case "image":
                return m_image;
            case "category":
                return m_category;
            default:
                return null;
        }
    }

    /**
     * @see org.opencms.widgets.dataview.I_CmsDataViewItem#getData()
     */
    public String getData() {

        return "";
    }

    /**
     * @see org.opencms.widgets.dataview.I_CmsDataViewItem#getDescription()
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * @see org.opencms.widgets.dataview.I_CmsDataViewItem#getId()
     */
    public String getId() {

        return m_id;
    }

    /**
     * @see org.opencms.widgets.dataview.I_CmsDataViewItem#getImage()
     */
    public String getImage() {

        return m_image;

    }

    /**
     * @see org.opencms.widgets.dataview.I_CmsDataViewItem#getTitle()
     */
    public String getTitle() {

        return m_title;

    }
}

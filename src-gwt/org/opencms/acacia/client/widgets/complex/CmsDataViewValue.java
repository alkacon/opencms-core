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

package org.opencms.acacia.client.widgets.complex;

/**
 * Represents a data view value to be read from or written to the editor by the data view widget.<p>
 */
public class CmsDataViewValue {

    /** The id. */
    private String m_id;

    /** The title. */
    private String m_title;

    /** The description. */
    private String m_description;

    /** The additional data. */
    private String m_data;

    /**
     * Creates a new instance.<p>
     *
     * @param id the id
     * @param title the title
     * @param description the description
     * @param data the additional data
     */
    public CmsDataViewValue(String id, String title, String description, String data) {
        super();
        m_id = id;
        m_title = title;
        m_description = description;
        m_data = data;
    }

    /**
     * Returns the data.<p>
     *
     * @return the data
     */
    public String getData() {

        return m_data;
    }

    /**
     * Returns the description.<p>
     *
     * @return the description
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the id.<p>
     *
     * @return the id
     */
    public String getId() {

        return m_id;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

}

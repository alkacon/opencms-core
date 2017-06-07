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

package org.opencms.workplace;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an user additional information block.<p>
 *
 * @since 6.5.6
 */
public final class CmsWorkplaceUserInfoBlock {

    /** The list of defined entries in the block. */
    private final List<CmsWorkplaceUserInfoEntry> m_entries;

    /** The block's title. */
    private String m_title;

    /**
     * Default constructor.<p>
     */
    public CmsWorkplaceUserInfoBlock() {

        m_entries = new ArrayList<CmsWorkplaceUserInfoEntry>();
    }

    /**
     * Creates a new entry.<p>
     *
     * @param key the additional information key
     * @param type the class name of the stored data type
     * @param widget the widget class name
     * @param params the widget parameters
     * @param optional if optional
     */
    public void addEntry(String key, String type, String widget, String params, String optional) {

        m_entries.add(new CmsWorkplaceUserInfoEntry(key, type, widget, params, optional));
    }

    /**
     * Returns a list of all configured additional information entries.<p>
     *
     * @return a list of {@link CmsWorkplaceUserInfoEntry} objects
     */
    public List<CmsWorkplaceUserInfoEntry> getEntries() {

        return m_entries;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Sets the title.<p>
     *
     * @param title the title to set
     */
    public void setTitle(String title) {

        m_title = title;
    }
}
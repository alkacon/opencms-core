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

package org.opencms.acacia.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Form tab information bean.<p>
 */
public class CmsTabInfo implements IsSerializable {

    /** Indicates if the first level of left labels should be shown in the editor. */
    private boolean m_collapsed;

    /** The XML element name where this tab starts. */
    private String m_startName;

    /** The name for the tab ID, generated from the start name. */
    private String m_tabId;

    /** The name to display on the tab. */
    private String m_tabName;

    /** The tab description HTML. */
    private String m_description;

    /** The tab localization key. */
    private String m_tabKey;

    /** Localization key for the description. */
    private String m_descriptionKey;

    /** The raw configured tab name string. */
    private String m_tabRaw;

    /** The raw configured description string. */
    private String m_descriptionRaw;

    /**
     * Constructor.<p>
     *
     * @param tabName the tab name
     * @param tabKey the tab localization key
     * @param tabId the tab id
     * @param startName the start element name
     * @param collapsed if the labels should be collapsed
     * @param description the description HTML
     * @param descriptionKey the description key
     */
    public CmsTabInfo(
        String tabName,
        String tabKey,
        String tabRaw,
        String tabId,
        String startName,
        boolean collapsed,
        String description,
        String descriptionKey,
        String descriptionRaw) {

        m_tabName = tabName;
        m_tabKey = tabKey;
        m_tabRaw = tabRaw;
        m_tabId = tabId;
        m_startName = startName;
        m_collapsed = collapsed;
        m_description = description;
        m_descriptionKey = descriptionKey;
        m_descriptionRaw = descriptionRaw;
    }

    /**
     * Constructor for serialization only.<p>
     */
    protected CmsTabInfo() {

        // nothing to do
    }

    /**
     * Gets the description HTML.<p>
     *
     * @return the description HTML
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Gets the localization key for the description.
     *
     * @return the localization key for the description
     */
    public String getDescriptionKey() {

        return m_descriptionKey;
    }

    /**
     * Gets the raw configured description string.
     *
     * @return the raw description
     */
    public String getDescriptionRaw() {

        return m_descriptionRaw;

    }

    /**
     * Returns the startName.<p>
     *
     * @return the startName
     */
    public String getStartName() {

        return m_startName;
    }

    /**
     * Returns the tabId.<p>
     *
     * @return the tabId
     */
    public String getTabId() {

        return m_tabId;
    }

    /**
     * Returns the tabName.<p>
     *
     * @return the tabName
     */
    public String getTabName() {

        return m_tabName;
    }

    /**
     * Gets the localization key for the tab name.
     *
     * @return the tab name localization key
     */
    public String getTabNameKey() {

        return m_tabKey;
    }

    /**
     * Gets the raw configured tab name.
     *
     * @return the raw configured tab name
     */
    public String getTabNameRaw() {

        return m_tabRaw;
    }

    /**
     * Returns the collapsed.<p>
     *
     * @return the collapsed
     */
    public boolean isCollapsed() {

        return m_collapsed;
    }
}

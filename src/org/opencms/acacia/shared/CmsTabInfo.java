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

    /**
     * Constructor.<p>
     * 
     * @param tabName the tab name
     * @param tabId the tab id
     * @param startName the start element name
     * @param collapsed if the labels should be collapsed
     */
    public CmsTabInfo(String tabName, String tabId, String startName, boolean collapsed) {

        m_tabName = tabName;
        m_tabId = tabId;
        m_startName = startName;
        m_collapsed = collapsed;
    }

    /**
     * Constructor for serialization only.<p>
     */
    protected CmsTabInfo() {

        // nothing to do
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
     * Returns the collapsed.<p>
     *
     * @return the collapsed
     */
    public boolean isCollapsed() {

        return m_collapsed;
    }
}

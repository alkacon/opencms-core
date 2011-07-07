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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.content;

import org.opencms.util.CmsStringUtil;

/**
 * Represents a configured tab to be used in the XML content editor for better usability.<p>
 */
public class CmsXmlContentTab {

    /** Indicates if the first level of left labels should be shown in the editor. */
    private boolean m_collapsed;

    /** The name for the tab ID, generated from the start name. */
    private String m_idName;

    /** The XML element name where this tab starts. */
    private String m_startName;

    /** The name to display on the tab. */
    private String m_tabName;

    /**
     * Constructor with the start element name.<p>
     * 
     * The tab name is equal to the element name and the first level should not be shown in the editor.<p>
     * 
     * @param startName the XML element name where this tab starts
     */
    public CmsXmlContentTab(String startName) {

        this(startName, true, startName);
    }

    /**
     * Constructor with all possible tab parameter settings.<p>
     * 
     * @param startName XML element name where this tab starts
     * @param collapsed indicates if the first level of left labels should be shown in the editor
     * @param tabName the name to display on the tab
     */
    public CmsXmlContentTab(String startName, boolean collapsed, String tabName) {

        m_startName = startName;
        m_collapsed = collapsed;
        m_tabName = tabName;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsXmlContentTab) {
            return ((CmsXmlContentTab)obj).getStartName().equals(getStartName());
        }
        return false;
    }

    /**
     * Returns the name for the tab ID, generated from the start name.<p>
     * 
     * @return the name for the tab ID, generated from the start name
     */
    public String getIdName() {

        if (m_idName == null) {
            m_idName = getStartName();
            // remove special characters causing problems in ID attributes
            m_idName = CmsStringUtil.substitute(m_idName, ".", "-");
            m_idName = CmsStringUtil.substitute(m_idName, ":", "-");
            m_idName = CmsStringUtil.substitute(m_idName, "_", "-");

        }
        return m_idName;
    }

    /**
     * Returns the XML element name where this tab starts.<p>
     * 
     * @return the XML element name where this tab starts
     */
    public String getStartName() {

        return m_startName;
    }

    /**
     * Returns the name to display on the tab.<p>
     * 
     * @return the name to display on the tab
     */
    public String getTabName() {

        return m_tabName;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return getStartName().hashCode();
    }

    /**
     * Indicates if the first level of left labels should be shown in the editor.<p>
     * 
     * @return <code>true</code> if the first level of left labels should NOT be shown in the editor
     */
    public boolean isCollapsed() {

        return m_collapsed;
    }

}
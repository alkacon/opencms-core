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

package org.opencms.ade.detailpage;

import org.opencms.util.CmsUUID;
import org.opencms.xml.containerpage.CmsXmlDynamicFunctionHandler;

import java.io.Serializable;

/**
 * Data bean containing the information for a detail page.<p>
 *
 * @since 8.0.0
 */
public class CmsDetailPageInfo implements Serializable {

    /** The prefix for dynamic function detail page types. */
    public static final String FUNCTION_PREFIX = "function@";

    /** ID for serialization. */
    private static final long serialVersionUID = 7714334294682534900L;

    /** The id of the detail page. */
    private CmsUUID m_id;

    /** Flag used to distinguish inherited detail pages from ones defined in the current sitemap config. */
    private boolean m_inherited;

    /** The resource type which the detail page should display. */
    private String m_type;

    /** The original URI of the detail page (for debugging purposes only). */
    private String m_uri;

    /**
     * Creates a new detail page info bean.<p>
     *
     * @param id the id of the detail page
     * @param uri the original URI of the page
     * @param type the resource type for which the detail page is used
     */
    public CmsDetailPageInfo(CmsUUID id, String uri, String type) {

        m_id = id;
        m_type = type;
        m_uri = uri;
    }

    /**
     * Empty default constructor for serialization.<p>
     */
    protected CmsDetailPageInfo() {

        // for serialization
    }

    /**
     * Removes the prefix for dynamic functions from a detail page type name.<p>
     *
     * @param name the detail page type name
     *
     * @return the detail page type name withotu the function prefix
     */
    public static String removeFunctionPrefix(String name) {

        return name.replaceFirst("^" + FUNCTION_PREFIX, "");
    }

    /**
     * Creates a copy of this entry, but sets the 'inherited' flag to true in the copy.<p>
     *
     * @return the copy of this entry
     */
    public CmsDetailPageInfo copyAsInherited() {

        CmsDetailPageInfo result = new CmsDetailPageInfo(m_id, m_uri, m_type);
        result.m_inherited = true;
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        boolean result = false;
        if (obj instanceof CmsDetailPageInfo) {
            CmsDetailPageInfo info = (CmsDetailPageInfo)obj;
            result = toString().equals(info.toString());
        }
        return result;
    }

    /**
     * Gets the type name to display to the user.<p>
     *
     * @return the type name to display
     */
    public String getDisplayType() {

        return m_type != null ? removeFunctionPrefix(m_type) : "";
    }

    /**
     * Returns the resource type name for the icon to display.<p>
     *
     * @return the icon resource type
     */
    public String getIconType() {

        if (m_type.startsWith(FUNCTION_PREFIX)) {
            return CmsXmlDynamicFunctionHandler.TYPE_FUNCTION;
        } else {
            return m_type;
        }
    }

    /**
     * Returns the id of the detail page.<p>
     *
     * @return the id of the detail page
     */
    public CmsUUID getId() {

        return m_id;
    }

    /**
     * Returns the type for which the detail page is used.<p>
     *
     * @return the type for which the detail page is used
     */
    public String getType() {

        return m_type;
    }

    /**
     * The original URI for the detail page.<p>
     *
     * @return the original URI for the detail page
     */
    public String getUri() {

        return m_uri;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return toString().hashCode();
    }

    /**
     * Returns true if the detail page entry is inherited from a parent sitemap.<p>
     *
     * @return true if the detail page entry is inherited from a parent sitemap
     */
    public boolean isInherited() {

        return m_inherited;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "" + m_type + ":" + m_id + m_uri;
    }
}

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

import org.opencms.ade.configuration.CmsADEManager;
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

    /** A string used to separate the type from the qualifier in the sitemap configuration. */
    public static final String QUALIFIER_SEPARATOR = "|";

    /** ID for serialization. */
    private static final long serialVersionUID = 7714334294682534900L;

    /** The resource icon style classes. */
    private String m_iconClasses;

    /** The id of the detail page. */
    private CmsUUID m_id;

    /** Flag used to distinguish inherited detail pages from ones defined in the current sitemap config. */
    private boolean m_inherited;

    /** Optional string that indicates when this detail page should be used. */
    private String m_qualifier;

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
     * @param qualifier an optional string that indicates when the detail page should be used
     * @param iconClasses the resource icon style classes
     */
    public CmsDetailPageInfo(CmsUUID id, String uri, String type, String qualifier, String iconClasses) {

        m_id = id;
        m_type = type;
        if ((m_type != null) && (m_type.indexOf(QUALIFIER_SEPARATOR) != -1)) {
            throw new RuntimeException(
                "Error: Qualifier separator '"
                    + QUALIFIER_SEPARATOR
                    + "' may not be part of detail page type: "
                    + type);
        }
        m_qualifier = qualifier;
        m_uri = uri;
        m_iconClasses = iconClasses;
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

        CmsDetailPageInfo result = new CmsDetailPageInfo(m_id, m_uri, m_type, m_qualifier, m_iconClasses);
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
     * Returns the resource icon style classes.<p>
     *
     * @return the resource icon style classes
     **/
    public String getIconClasses() {

        return m_iconClasses;
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
     * Gets the type including the qualifier (if set).
     *
     * <p>This is the same format as used in the detail page type field in the sitemap configuration.
     *
     * @return the qualified type
     */
    public String getQualifiedType() {

        if (m_qualifier != null) {
            return getType() + CmsDetailPageInfo.QUALIFIER_SEPARATOR + getQualifier();
        } else {
            return getType();
        }
    }

    /**
     * Gets the qualifier string.
     *
     * <p>The qualifier is an optional (i.e. possibly null) string that indicates when this detail page should be used.
     *
     * @return the qualifier string
     */
    public String getQualifier() {

        return m_qualifier;
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
     * Checks if this detail page has the default detail page type.
     *
     * @return true if this is a default detail page
     */
    public boolean isDefaultDetailPage() {

        return CmsADEManager.DEFAULT_DETAILPAGE_TYPE.equals(getType());
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

        return "" + m_type + QUALIFIER_SEPARATOR + m_qualifier + ":" + m_id + m_uri;
    }
}

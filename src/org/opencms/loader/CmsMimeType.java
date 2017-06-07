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

package org.opencms.loader;

import java.util.Locale;

/**
 * Describes a MIME type configured in OpenCms.<p>
 *
 * @since 7.0.0
 */
public class CmsMimeType implements Comparable<CmsMimeType> {

    /** Indicates if this a MIME type read from the OpenCms configuration. */
    private boolean m_configured;

    /** The MIME type file extension. */
    private String m_extension;

    /** The MIME type description. */
    private String m_type;

    /**
     * Default constructor for MIME types.<p>
     *
     * If the extension does not start with a dot '.', then a dot is automatically added
     * as a prefix.<p>
     *
     * @param extension the MIME type extension
     * @param type the MIME type description
     */
    public CmsMimeType(String extension, String type) {

        this(extension, type, true);
    }

    /**
     * Special constructor for "marked" MIME types.<p>
     *
     * If the extension does not start with a dot '.', then a dot is automatically added
     * as a prefix.<p>
     *
     * @param extension the MIME type extension
     * @param type the MIME type description
     * @param configured indicates if this a MIME type read from the OpenCms configuration
     */
    public CmsMimeType(String extension, String type, boolean configured) {

        m_extension = String.valueOf(extension).toLowerCase(Locale.ENGLISH);
        if (!(m_extension.charAt(0) == '.')) {
            m_extension = "." + m_extension;
        }
        m_type = String.valueOf(type).toLowerCase(Locale.ENGLISH);
        m_configured = configured;
    }

    /**
     * MIME-types are compared according to the type first, and to the extension second.<p>
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(CmsMimeType obj) {

        if (obj == this) {
            return 0;
        }
        int result = m_type.compareTo(obj.m_type);
        if (result == 0) {
            result = m_extension.compareTo(obj.m_extension);
        }
        return result;
    }

    /**
     * MIME-types are equal is the extension is equal.<p>
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsMimeType) {
            return ((CmsMimeType)obj).m_extension.equals(m_extension);
        }
        return false;
    }

    /**
     * Returns the MIME type file extension.<p>
     *
     * @return the MIME type file extension
     */
    public String getExtension() {

        return m_extension;
    }

    /**
     * Returns the MIME type description.<p>
     *
     * @return the MIME type description
     */
    public String getType() {

        return m_type;
    }

    /**
     * The hash code of MIME types is build only from the extension.<p>
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_extension.hashCode();
    }

    /**
     * Returns <code>true</code> if this MIME type has been read from the OpenCms configuration.<p>
     *
     * @return <code>true</code> if this MIME type has been read from the OpenCms configuration
     */
    public boolean isConfigured() {

        return m_configured;
    }
}
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

package org.opencms.workplace.explorer;

/**
 * An icon configuration rule for an explorer type.<p>
 *
 * Each rule consists of a file name extension and (at most) two icon file names for icons which
 * should be used for resources with that extension.<p>
 *
 * @since 8.0.0
 */
public class CmsIconRule {

    /** The big icon. */
    private String m_bigIcon;

    /** The file name extension. */
    private String m_extension;

    /** The small icon. */
    private String m_icon;

    /**
     * Creates a new icon rule.<p>
     *
     * @param extension the file name extension
     * @param icon the small icon's file name
     * @param bigIcon the big icon's file name
     */
    public CmsIconRule(String extension, String icon, String bigIcon) {

        m_icon = icon;
        m_bigIcon = bigIcon;
        m_extension = extension;
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {

        return new CmsIconRule(m_extension, m_icon, m_bigIcon);
    }

    /**
     * Returns the big icon's file name.<p>
     *
     * @return the big icon's file name
     */
    public String getBigIcon() {

        return m_bigIcon;
    }

    /**
     * Returns the biggest icon available.<p>
     *
     * @return the biggest icon available
     */
    public String getBigIconIfAvailable() {

        return m_bigIcon != null ? m_bigIcon : m_icon;
    }

    /**
     * Returns the file name extension for this rule.<p>
     *
     * @return the file name extension for this rule
     */
    public String getExtension() {

        return m_extension;

    }

    /**
     * Returns the small icon's file name.<p>
     *
     * @return the small icon's file name
     */
    public String getIcon() {

        return m_icon;
    }
}

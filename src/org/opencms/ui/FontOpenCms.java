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

package org.opencms.ui;

import com.vaadin.server.FontIcon;

/**
 * The available OpenCms workplace icons.<p>
 */
public enum FontOpenCms implements FontIcon {
    /** The sitemap icon. */
    SITEMAP(0x73),

    /** The context menu icon. */
    CONTEXT_MENU(0x63),

    /** The publish icon. */
    PUBLISH(0x70),

    /** The bulls eye edit icon. */
    EDIT_POINT(0x65);

    /** The icon code point. */
    private int m_codepoint;

    /**
     * Constructor.<p>
     *
     * @param codepoint the icon code point
     */
    FontOpenCms(int codepoint) {
        m_codepoint = codepoint;
    }

    /**
     * @see com.vaadin.server.FontIcon#getCodepoint()
     */
    @Override
    public int getCodepoint() {

        return m_codepoint;
    }

    /**
     * @see com.vaadin.server.FontIcon#getFontFamily()
     */
    @Override
    public String getFontFamily() {

        return "opencms-font";
    }

    /**
     * @see com.vaadin.server.FontIcon#getHtml()
     */
    @Override
    public String getHtml() {

        return "<span class=\"v-icon\" style=\"font-family: "
            + getFontFamily()
            + ";\">&#x"
            + Integer.toHexString(m_codepoint)
            + ";</span>";
    }

    /**
     * @see com.vaadin.server.Resource#getMIMEType()
     */
    @Override
    public String getMIMEType() {

        throw new UnsupportedOperationException(
            FontIcon.class.getSimpleName() + " should not be used where a MIME type is needed.");
    }
}

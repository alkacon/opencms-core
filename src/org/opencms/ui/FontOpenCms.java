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

    /** The clipboard icon. */
    CLIPBOARD(0x43),

    /** The context menu icon. */
    CONTEXT_MENU(0x4d),

    /** The copy locale icon. */
    COPY_LOCALE(0x6c),

    /** The bulls eye edit icon. */
    EDIT_POINT(0x45),

    /** The exit icon. */
    EXIT(0x58),

    /** The filter icon. */
    FILTER(0x66),

    /** The gallery icon. */
    GALLERY(0x47),

    /** The help icon. */
    HELP(0x68),

    /** The info icon. */
    INFO(0x49),

    /** The pen/edit icon. */
    PEN(0x70),

    /** The publish icon. */
    PUBLISH(0x50),

    /** The redo icon. */
    REDO(0x72),

    /** The remove locale icon. */
    REMOVE_LOCALE(0x4c),

    /** The save icon. */
    SAVE(0x73),

    /** The save and exit icon. */
    SAVE_EXIT(0x78),

    /** The search icon. */
    SEARCH(0x46),

    /** The sitemap icon. */
    SITEMAP(0x53),

    /** The undo icon. */
    UNDO(0x75),

    /** The upload icon. */
    UPLOAD(0x55),

    /** The wand icon. */
    WAND(0x57);

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

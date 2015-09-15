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

    /** The apps icon. */
    APPS(0xe617),

    /** The clipboard icon. */
    CLIPBOARD(0xe60f),

    /** The context menu icon. */
    CONTEXT_MENU(0xe616),

    /** The copy locale icon. */
    COPY_LOCALE(0xe61a),

    /** The bulls eye edit icon. */
    EDIT_POINT(0xe600),

    /** The disabled bulls eye edit icon. */
    EDIT_POINT_DISABLED(0xe601),

    /** The exit icon. */
    EXIT(0xe615),

    /** The filter icon. */
    FILTER(0xe60c),

    /** The gallery icon. */
    GALLERY(0xe611),

    /** The help icon. */
    HELP(0xe602),

    /** The disabled help icon. */
    HELP_DISABLED(0xe603),

    /** The hide icon. */
    HIDE(0xe609),

    /** The info icon. */
    INFO(0xe612),

    /** The pen/edit icon. */
    PEN(0xe614),

    /** The publish icon. */
    PUBLISH(0xe60e),

    /** The redo icon. */
    REDO(0xe607),

    /** The remove locale icon. */
    REMOVE_LOCALE(0xe619),

    /** The save icon. */
    SAVE(0xe60b),

    /** The save and exit icon. */
    SAVE_EXIT(0xe60a),

    /** The search icon. */
    SEARCH(0xe60d),

    /** The settings icon. */
    SETTINGS(0xe618),

    /** The show icon. */
    SHOW(0xe608),

    /** The sitemap icon. */
    SITEMAP(0xe613),

    /** The undo icon. */
    UNDO(0xe606),

    /** The upload icon. */
    UPLOAD(0xe604),

    /** The wand icon. */
    WAND(0xe610);

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

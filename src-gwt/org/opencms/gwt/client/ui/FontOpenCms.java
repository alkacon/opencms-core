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

package org.opencms.gwt.client.ui;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.HTML;

/**
 * OpenCms font icons.<p>
 */
public enum FontOpenCms {

    /** The apps icon. */
    APPS(0xe617),

    /** The brackets icon. */
    BRACKETS(0xe921),

    /** The circle icon. */
    CIRCLE(0xe62a),

    /** The circle cancel icon. */
    CIRCLE_CANCEL(0x622),

    /** The circle check icon. */
    CIRCLE_CHECK(0xe61e),

    /** The circle info icon. */
    CIRCLE_INFO(0xed6c),

    /** The circle invert icon. */
    CIRCLE_INV(0xe62b),

    /** The circle invert cancel icon. */
    CIRCLE_INV_CANCEL(0xe623),

    /** The circle invert check icon. */
    CIRCLE_INV_CHECK(0xe61f),

    /** The circle invert minus icon. */
    CIRCLE_INV_MINUS(0xe629),

    /** The circle invert pause icon. */
    CIRCLE_INV_PAUSE(0x0621),

    /** The circle invert play icon. */
    CIRCLE_INV_PLAY(0xe625),

    /** The circle invert plus icon. */
    CIRCLE_INV_PLUS(0xe627),

    /** The circle minus icon. */
    CIRCLE_MINUS(0xe628),

    /** The circle pause icon. */
    CIRCLE_PAUSE(0xe620),

    /** The circle play icon. */
    CIRCLE_PLAY(0xe624),

    /** The circle plus icon. */
    CIRCLE_PLUS(0xe626),

    /** The clipboard icon. */
    CLIPBOARD(0xe60f),

    /** The context menu icon. */
    CONTEXT_MENU(0xe616),

    /** The context menu dots icon. */
    CONTEXT_MENU_DOTS(0xe91c),

    /** The copy locale icon. */
    COPY_LOCALE(0xe61a),

    /** The bulls eye edit icon. */
    EDIT_POINT(0xe600),

    /** The disabled bulls eye edit icon. */
    EDIT_POINT_DISABLED(0xe601),

    /** The error icon. */
    ERROR(0xed6b),

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

    /** The highlight icon. */
    HIGHLIGHT(0xe91e),

    /** The info icon. */
    INFO(0xe612),

    /** The invisible chars icon. */
    INVISIBLE_CHARS(0xe91f),

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

    /** The search replace icon. */
    SEARCH_REPLACE(0xe91d),

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
    WAND(0xe610),

    /** The warning icon. */
    WARNING(0xed50),

    /** The wrap lines icon. */
    WRAP_LINES(0xe920);

    /**
     * Font icon widget.<p>
     */
    protected class IconHTML extends HTML {

        /**
         * Constructor.<p>
         *
         * @param codepoint the icon character code point
         */
        IconHTML(int codepoint) {
            super(Document.get().createSpanElement());
            getElement().setInnerHTML("&#x" + Integer.toHexString(codepoint) + ";");
            getElement().setClassName(FONT_ICON_CLASS);
        }

        /**
         * Constructor.<p>
         *
         * @param codepoint the icon character code point
         * @param size the icon size
         */
        IconHTML(int codepoint, int size) {
            this(codepoint);
            getElement().setAttribute("style", "font-size: " + size + "px; line-height: " + size + "px;");
        }

        /**
         * Constructor.<p>
         *
         * @param codepoint the icon character code point
         * @param size the icon size
         * @param color the icon color
         */
        IconHTML(int codepoint, int size, String color) {
            this(codepoint);
            getElement().setAttribute(
                "style",
                "font-size: " + size + "px; line-height: " + size + "px; color: " + color + ";");
        }
    }

    /** The font icon CSS class. */
    public static final String FONT_ICON_CLASS = "opencms-font-icon";

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
     * Returns the font family.<p>
     *
     * @return the font family
     */
    public String getFontFamily() {

        return "opencms-font";
    }

    /**
     * Returns the icon HTML.<p>
     *
     * @param size the icon size
     *
     * @return the icon HTML
     */
    public String getHtml(int size) {

        return "<span class=\""
            + FONT_ICON_CLASS
            + "\" style=\"font-size: "
            + size
            + "px; line-height: "
            + size
            + "px;\">&#x"
            + Integer.toHexString(m_codepoint)
            + ";</span>";
    }

    /**
     * Returns the icon HTML.<p>
     *
     * @param size the icon size
     * @param color the icon color
     *
     * @return the icon HTML
     */
    public String getHtml(int size, String color) {

        return "<span class=\""
            + FONT_ICON_CLASS
            + "\" style=\"font-size: "
            + size
            + "px; line-height: "
            + size
            + "px; color: "
            + color
            + ";\">&#x"
            + Integer.toHexString(m_codepoint)
            + ";</span>";
    }

    /**
     * Returns the icon widget.<p>
     *
     * @param size the icon size
     *
     * @return the icon widget
     */
    public HTML getWidget(int size) {

        return new IconHTML(m_codepoint, size);
    }

    /**
     * Returns the icon widget.<p>
     *
     * @param size the icon size
     * @param color the icon color
     *
     * @return the icon widget
     */
    public HTML getWidget(int size, String color) {

        return new IconHTML(m_codepoint, size, color);
    }

}

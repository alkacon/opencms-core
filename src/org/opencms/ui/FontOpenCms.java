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

package org.opencms.ui;

import com.vaadin.server.FontIcon;
import com.vaadin.server.GenericFontIcon;

/**
 * The available OpenCms workplace icons.<p>
 */
public enum FontOpenCms implements FontIcon {

    /** The add icon. */
    ADD_SMALL(0xe90c),

    /** The apps icon. */
    APPS(0xe617),

    /** The bookmarks icon. */
    BOOKMARKS(0xe654),

    /** The brackets icon. */
    BRACKETS(0xe64f),

    /** The broadcast icon. */
    BROADCAST(0xe650),

    /** The cache icon. */
    CACHE(0xe62f),

    /** The check icon. */
    CHECK_SMALL(0xe917),

    /** The circle icon. */
    CIRCLE(0xe62a),

    /** The circle cancel icon. */
    CIRCLE_CANCEL(0x622),

    /** The circle check icon. */
    CIRCLE_CHECK(0xe61e),

    /** The circle info icon. */
    CIRCLE_INFO(0xe640),

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

    /** The circle help icon. */
    CIRCLE_HELP(0xe900),

    /** The clipboard icon. */
    CLIPBOARD(0xe60f),

    /** The context menu icon. */
    CONTEXT_MENU(0xe616),

    /** The context menu dots icon. */
    CONTEXT_MENU_DOTS(0xe64a),

    /** The context menu icon. */
    CONTEXT_MENU_SMALL(0xe90a),

    /** The copy locale icon. */
    COPY_LOCALE(0xe61a),

    /** The crop icon. */
    CROP(0xe641),

    /** The crop remove icon. */
    CROP_REMOVE(0xe642),

    /** The cut icon. */
    CUT_SMALL(0xe907),

    /** The database icon. */
    DATABASE(0xe62c),

    /** The delete icon. */
    DELETE_SMALL(0xe90d),

    /** The download icon. */
    DOWNLOAD(0xe605),

    /** The edit down icon. */
    EDIT_DOWN_SMALL(0xe910),

    /** The bulls eye edit icon. */
    EDIT_POINT(0xe600),

    /** The disabled bulls eye edit icon. */
    EDIT_POINT_DISABLED(0xe601),

    /** The bulls eye edit icon. */
    EDIT_POINT_SMALL(0xe901),

    /** The edit up icon. */
    EDIT_UP_SMALL(0xe913),

    /** The error icon. */
    ERROR(0xe63f),

    /** The exit icon. */
    EXIT(0xe615),

    /** The favorite icon. */
    FAVORITE_SMALL(0xe906),

    /** The file icon. */
    FILE(0xe63b),

    /** The inverse file icon. */
    FILE_INV(0xe63c),

    /** The filter icon. */
    FILTER(0xe60c),

    /** The folder icon. */
    FOLDER(0xe63d),

    /** The gallery icon. */
    GALLERY(0xe611),

    /** The git icon. */
    GIT(0xe633),

    /** The help icon. */
    HELP(0xe602),

    /** The disabled help icon. */
    HELP_DISABLED(0xe603),

    /** The hide icon. */
    HIDE(0xe609),

    /** The highlight icon. */
    HIGHLIGHT(0xe64c),

    /** The history icon. */
    HISTORY(0xe631),

    /** The image icon. */
    IMAGE(0xe632),

    /** The info icon. */
    INFO(0xe612),

    /** The info icon. */
    INFO_SMALL(0xe904),

    /** The invisible chars icon. */
    INVISIBLE_CHARS(0xe64d),

    /** The link icon. */
    LINK(0xe636),

    /** The list icon. */
    LIST(0xe634),

    /** The lock closed icon. */
    LOCK_CLOSED(0xe644),

    /** The lock opened icon. */
    LOCK_OPEN(0xe645),

    /** The lock icon. */
    LOCK_SMALL(0xe90e),

    /** The log icon. */
    LOG(0xe637),

    /** The log icon. */
    LOGIN(0xe653),

    /** The help icon. */
    HELP_SMALL(0xe919),

    /** The module icon. */
    MODULE(0xe638),

    /** The move icon. */
    MOVE_SMALL(0xe903),

    /** The pen/edit icon. */
    PEN(0xe614),

    /** The pen/edit icon. */
    PEN_SMALL(0xe902),

    /** The preview icon. */
    PREVIEW_SMALL(0xe90f),

    /** The project icon. */
    PROJECT(0xe62e),

    /** The publish icon. */
    PUBLISH(0xe60e),

    /** The quick launch editor icon. */
    QUICKLAUNCH_EDITOR(0xe63a),

    /** The redo icon. */
    REDO(0xe607),

    /** The remove locale icon. */
    REMOVE_LOCALE(0xe619),

    /** The reset icon. */
    RESET(0xe643),

    /** The save icon. */
    SAVE(0xe60b),

    /** The save and exit icon. */
    SAVE_EXIT(0xe60a),

    /** The scheduler icon. */
    SCHEDULER(0xe630),

    /** The search icon. */
    SEARCH(0xe60d),

    /** The search replace icon. */
    SEARCH_REPLACE(0xe64b),

    /** The search icon. */
    SEARCH_SMALL(0xe908),

    /** The settings icon. */
    SETTINGS(0xe618),

    /** The settings icon. */
    SETTINGS_SMALL(0xe905),

    /** The show icon. */
    SHOW(0xe608),

    /** The site icon. */
    SITE(0xe62d),

    /** The sitemap icon. */
    SITEMAP(0xe613),

    /** The sitemap icon. */
    SITEMAP_SMALL(0xe90b),

    /** An empty icon. */
    SPACE(0x0020),

    /** The sphere icon. */
    SPHERE(0xe635),

    /** The trash icon. */
    TRASH_SMALL(0xe916),

    /** 'minus' state icon for tree opener. */
    TREE_MINUS(0xe649),

    /** 'plus' state icon for tree opener. */
    TREE_PLUS(0xe648),

    /** The triangle down icon. */
    TRIANGLE_DOWN(0xe647),

    /** The triangle right icon. */
    TRIANGLE_RIGHT(0xe646),

    /** The undo icon. */
    UNDO(0xe606),

    /** The upload icon. */
    UPLOAD(0xe604),

    /** The upload icon. */
    UPLOAD_SMALL(0xe909),

    /** The users icon. */
    USERS(0xe639),

    /** The wand icon. */
    WAND(0xe610),

    /** The warning icon. */
    WARNING(0xe63e),

    /** The wrap lines icon. */
    WRAP_LINES(0xe64e);

    /** The font family. */
    public static final String FONT_FAMILY = "opencms-font";

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
     * Returns the button overlay style classes for the icon.<p>
     *
     * @return the button overlay style
     */
    public String getButtonOverlayStyle() {

        return "o-button-overlay o-button-overlay-" + name().toLowerCase().replace("_", "-");
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

        return FONT_FAMILY;
    }

    /**
     * @see com.vaadin.server.FontIcon#getHtml()
     */
    @Override
    public String getHtml() {

        return GenericFontIcon.getHtml(getFontFamily(), getCodepoint());
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

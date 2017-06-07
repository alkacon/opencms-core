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

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

/**
 * Interface to hold button related enumerations. To be used with {@link org.opencms.gwt.client.ui.CmsPushButton}
 * and {@link org.opencms.gwt.client.ui.CmsToggleButton}.<p>
 */
public interface I_CmsButton {

    /** Available button colors. */
    public enum ButtonColor {

        /** Button color. */
        BLUE(I_CmsLayoutBundle.INSTANCE.buttonCss().blue()),

        /** Button color. */
        GRAY(I_CmsLayoutBundle.INSTANCE.buttonCss().gray()),

        /** Button color. */
        GREEN(I_CmsLayoutBundle.INSTANCE.buttonCss().green()),

        /** Button color. */
        RED(I_CmsLayoutBundle.INSTANCE.buttonCss().red());

        /** The list of additional style class names for this button style. */
        private String m_className;

        /**
         * Constructor.<p>
         *
         * @param className the additional classes
         */
        private ButtonColor(String className) {

            m_className = className;
        }

        /**
         * Returns the additional classes.<p>
         *
         * @return the additional classes
         */
        public String getClassName() {

            return m_className;
        }
    }

    /** Available button icons. */
    public enum ButtonData {

        /** Toolbar button. */
        WAND_BUTTON(WAND, Messages.get().key(Messages.GUI_TOOLBAR_ADD_0)),

        /** Toolbar button. */
        ADD_TO_FAVORITES("opencms-icon-favorite", Messages.get().key(Messages.GUI_TOOLBAR_ADD_TO_FAVORITES_0)),

        /** Toolbar button. */
        BACK(EXIT, Messages.get().key(Messages.GUI_TOOLBAR_BACK_0)),

        /** Toolbar button. */
        CLIPBOARD_BUTTON(CLIPBOARD, Messages.get().key(Messages.GUI_TOOLBAR_CLIPBOARD_0)),

        /** Toolbar button. */
        CONTEXT(CONTEXT_MENU, Messages.get().key(Messages.GUI_TOOLBAR_CONTEXT_0)),

        /** Toolbar button. */
        COPY_LOCALE_BUTTON(COPY_LOCALE, Messages.get().key(Messages.GUI_TOOLBAR_COPY_LOCALE_0)),

        /** Toolbar button. */
        EDIT(PEN, Messages.get().key(Messages.GUI_TOOLBAR_EDIT_0)),

        /** Toolbar button. */
        ELEMENT_INFO(INFO, Messages.get().key(Messages.GUI_TOOLBAR_ELEMENT_INFO_0)),

        /** Toolbar button. */
        GALLERY_BUTTON(GALLERY, Messages.get().key(Messages.GUI_TOOLBAR_GALLERY_0)),

        /** Toolbar button. */
        INFO_BUTTON(INFO, Messages.get().key(Messages.GUI_TOOLBAR_INFO_0)),

        /** Inherited element button. */
        INHERITED(SITEMAP, Messages.get().key(Messages.GUI_TOOLBAR_INHERITED_0)),

        /** Toolbar button. */
        MOVE("opencms-icon-move", Messages.get().key(Messages.GUI_TOOLBAR_MOVE_IN_0)),

        /** Toolbar button. */
        SETTINGS_BUTTON(SETTINGS, Messages.get().key(Messages.GUI_TOOLBAR_PROPERTIES_0)),

        /** Toolbar button. */
        PUBLISH_BUTTON(PUBLISH, Messages.get().key(Messages.GUI_TOOLBAR_PUBLISH_0)),

        /** Toolbar button. */
        REMOVE("opencms-icon-cut", Messages.get().key(Messages.GUI_TOOLBAR_REMOVE_0)),

        /** Toolbar button. */
        RESET_BUTTON(EXIT, Messages.get().key(Messages.GUI_TOOLBAR_RESET_0)),

        /** Toolbar button. */
        SAVE_BUTTON(SAVE, Messages.get().key(Messages.GUI_TOOLBAR_SAVE_0)),

        /** Toolbar button. */
        SELECTION(EDIT_POINT, Messages.get().key(Messages.GUI_TOOLBAR_SELECTION_0)),

        /** Shows formerly hidden elements. */
        ADD("opencms-icon-add", Messages.get().key(Messages.GUI_TOOLBAR_ADD_0)),

        /** Toolbar button. */
        SITEMAP_BUTTON(SITEMAP, Messages.get().key(Messages.GUI_TOOLBAR_SITEMAP_0)),

        /** Toolbar button. */
        TOGGLE_HELP(HELP, Messages.get().key(Messages.GUI_TOOLBAR_TOGGLE_HELP_0));

        /** The icon class name. */
        private String m_iconClass;

        /** The title. */
        private String m_title;

        /**
         * Constructor.<p>
         *
         * @param iconClass the icon class name
         * @param title the title
         */
        private ButtonData(String iconClass, String title) {

            m_iconClass = iconClass;
            m_title = title;
        }

        /**
         * Returns the CSS class name.<p>
         *
         * @return the CSS class name
         */
        public String getIconClass() {

            return m_iconClass;
        }

        /**
         * Returns the icon class for small icons of 20x20.<p>
         *
         * @return the small icon class
         */
        public String getSmallIconClass() {

            return m_iconClass + "-20";
        }

        /**
         * Returns the title.<p>
         *
         * @return the title
         */
        public String getTitle() {

            return m_title;
        }
    }

    /** Available button styles. */
    public enum ButtonStyle {

        /** Font icon button. */
        FONT_ICON(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsFontIconButton(), I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll(), ICON_FONT),

        /** Menu button. */
        MENU(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsMenuButton(), I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll()),

        /** Default button. */
        TEXT(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsTextButton(), I_CmsLayoutBundle.INSTANCE.generalCss().buttonCornerAll()),

        /** Transparent button. */
        TRANSPARENT(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsTransparentButton(), I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());

        /** The list of additional style class names for this button style. */
        private String[] m_additionalClasses;

        /**
         * Constructor.<p>
         *
         * @param additionalClasses the additional classes
         */
        private ButtonStyle(String... additionalClasses) {

            m_additionalClasses = additionalClasses;
        }

        /**
         * Returns the additional classes.<p>
         *
         * @return the additional classes
         */
        public String[] getAdditionalClasses() {

            return m_additionalClasses;
        }

        /**
         * Returns the classes stored in the array as space separated list.<p>
         *
         * @return the classes stored in the array as space separated list
         */
        public String getCssClassName() {

            StringBuffer sb = new StringBuffer();
            for (String addClass : m_additionalClasses) {
                sb.append(addClass + " ");
            }
            return sb.toString();
        }
    }

    /** CSS style variants. */
    public static enum Size {

        /** Big button style. */
        big(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsButtonBig()),

        /** Medium button style. */
        medium(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsButtonMedium()),

        /** Small button style. */
        small(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsButtonSmall());

        /** The CSS class name. */
        private String m_cssClassName;

        /**
         * Constructor.<p>
         *
         * @param cssClassName the CSS class name
         */
        Size(String cssClassName) {

            m_cssClassName = cssClassName;
        }

        /**
         * Returns the CSS class name of this style.<p>
         *
         * @return the CSS class name
         */
        public String getCssClassName() {

            return m_cssClassName;
        }
    }

    /** Icon font CSS class. */
    public static final String ICON_FONT = "opencms-icon";

    /** Font icon using a 32x32 grid. */
    public static final String FILTER = "opencms-icon-filter";

    /** Font icon using a 32x32 grid. */
    public static final String SEARCH = "opencms-icon-search";

    /** Font icon using a 32x32 grid. */
    public static final String UNDO = "opencms-icon-undo";

    /** Font icon using a 32x32 grid. */
    public static final String REDO = "opencms-icon-redo";

    /** Font icon using a 32x32 grid. */
    public static final String HELP = "opencms-icon-help";

    /** Font icon using a 32x32 grid. */
    public static final String REMOVE_LOCALE = "opencms-icon-remove-locale";

    /** Font icon using a 32x32 grid. */
    public static final String COPY_LOCALE = "opencms-icon-copy-locale";

    /** Font icon using a 32x32 grid. */
    public static final String SAVE_EXIT = "opencms-icon-save-exit";

    /** Font icon using a 32x32 grid. */
    public static final String SAVE = "opencms-icon-save";

    /** Font icon using a 32x32 grid. */
    public static final String TREE_MINUS = "opencms-icon-tree-minus";

    /** Font icon using a 32x32 grid. */
    public static final String TREE_PLUS = "opencms-icon-tree-plus";

    /** Font icon using a 32x32 grid. */
    public static final String EXIT = "opencms-icon-exit";

    /** Font icon using a 32x32 grid. */
    public static final String PEN = "opencms-icon-pen";

    /** Font icon using a 32x32 grid. */
    public static final String UPLOAD = "opencms-icon-upload";

    /** Font icon using a 32x32 grid. */
    public static final String DOWNLOAD = "opencms-icon-download";

    /** Font icon using a 32x32 grid. */
    public static final String PUBLISH = "opencms-icon-publish";

    /** Font icon using a 32x32 grid. */
    public static final String CLIPBOARD = "opencms-icon-clipboard";

    /** Font icon using a 32x32 grid. */
    public static final String WAND = "opencms-icon-wand";

    /** Font icon using a 32x32 grid. */
    public static final String GALLERY = "opencms-icon-gallery";

    /** Font icon using a 32x32 grid. */
    public static final String INFO = "opencms-icon-info";

    /** Font icon using a 32x32 grid. */
    public static final String CONTEXT_MENU = "opencms-icon-context-menu";

    /** Font icon using a 32x32 grid. */
    public static final String APPS = "opencms-icon-apps";

    /** Font icon using a 32x32 grid. */
    public static final String SITEMAP = "opencms-icon-sitemap";

    /** Font icon using a 32x32 grid. */
    public static final String EDIT_POINT = "opencms-icon-edit-point";

    /** Font icon using a 32x32 grid. */
    public static final String SHOW = "opencms-icon-show";

    /** Font icon using a 32x32 grid. */
    public static final String HIDE = "opencms-icon-hide";

    /** Font icon using a 32x32 grid. */
    public static final String SETTINGS = "opencms-icon-settings";

    /** Font icon using a 32x32 grid. */
    public static final String CIRCLE_CHECK = "opencms-icon-circle-check";

    /** Font icon using a 32x32 grid. */
    public static final String CIRCLE_CHECK_INV = "opencms-icon-circle-inv-check";

    /** Font icon using a 32x32 grid. */
    public static final String CIRCLE_PAUSE = "opencms-icon-circle-pause";

    /** Font icon using a 32x32 grid. */
    public static final String CIRCLE_PAUSE_INV = "opencms-icon-circle-inv-pause";

    /** Font icon using a 32x32 grid. */
    public static final String CIRCLE_CANCEL = "opencms-icon-circle-cancel";

    /** Font icon using a 32x32 grid. */
    public static final String CIRCLE_CANCEL_INV = "opencms-icon-circle-inv-cancel";

    /** Font icon using a 32x32 grid. */
    public static final String CIRCLE_PLAY = "opencms-icon-circle-play";

    /** Font icon using a 32x32 grid. */
    public static final String CIRCLE_PLAY_INV = "opencms-icon-circle-inv-play";

    /** Font icon using a 32x32 grid. */
    public static final String CIRCLE_PLUS = "opencms-icon-circle-plus";

    /** Font icon using a 32x32 grid. */
    public static final String CIRCLE_PLUS_INV = "opencms-icon-circle-inv-plus";

    /** Font icon using a 32x32 grid. */
    public static final String CIRCLE_MINUS = "opencms-icon-circle-minus";

    /** Font icon using a 32x32 grid. */
    public static final String CIRCLE_MINUS_INV = "opencms-icon-circle-inv-minus";

    /** Font icon using a 32x32 grid. */
    public static final String CIRCLE = "opencms-icon-circle";

    /** Font icon using a 32x32 grid. */
    public static final String CIRCLE_INV = "opencms-icon-circle-inv";

    /** Font icon using a 32x32 grid. */
    public static final String WARNING = "opencms-icon-warning";

    /** Font icon using a 32x32 grid. */
    public static final String ERROR = "opencms-icon-error";

    /** Font icon using a 32x32 grid. */
    public static final String CIRCLE_INFO = "opencms-icon-circle-info";

    /** Font icon using a 32x32 grid. */
    public static final String CROP = "opencms-icon-crop";

    /** Font icon using a 32x32 grid. */
    public static final String CROP_REMOVE = "opencms-icon-crop-remove";

    /** Font icon using a 32x32 grid. */
    public static final String RESET = "opencms-icon-reset";

    /** Font icon using a 32x32 grid. */
    public static final String LOCK_CLOSED = "opencms-icon-lock-closed";

    /** Font icon using a 32x32 grid. */
    public static final String LOCK_OPEN = "opencms-icon-lock-open";

    /** Font icon using a 32x32 grid. */
    public static final String TRIANGLE_RIGHT = "opencms-icon-triangle-right";

    /** Font icon using a 32x32 grid. */
    public static final String TRIANGLE_DOWN = "opencms-icon-triangle-down";

    /** Small font icon using a 20x20 grid. */
    public static final String EDIT_POINT_SMALL = "opencms-icon-edit-point-20";

    /** Small font icon using a 20x20 grid. */
    public static final String PEN_SMALL = "opencms-icon-pen-20";

    /** Small font icon using a 20x20 grid. */
    public static final String MOVE_SMALL = "opencms-icon-move-20";

    /** Small font icon using a 20x20 grid. */
    public static final String INFO_SMALL = "opencms-icon-info-20";

    /** Small font icon using a 20x20 grid. */
    public static final String SETTINGS_SMALL = "opencms-icon-settings-20";

    /** Small font icon using a 20x20 grid. */
    public static final String FAVORITE_SMALL = "opencms-icon-favorite-20";

    /** Small font icon using a 20x20 grid. */
    public static final String CUT_SMALL = "opencms-icon-cut-20";

    /** Small font icon using a 20x20 grid. */
    public static final String SEARCH_SMALL = "opencms-icon-search-20";

    /** Small font icon using a 20x20 grid. */
    public static final String UPLOAD_SMALL = "opencms-icon-upload-20";

    /** Small font icon using a 20x20 grid. */
    public static final String CONTEXT_MENU_SMALL = "opencms-icon-context-menu-20";

    /** Small font icon using a 20x20 grid. */
    public static final String SITEMAP_SMALL = "opencms-icon-sitemap-20";

    /** Small font icon using a 20x20 grid. */
    public static final String ADD_SMALL = "opencms-icon-add-20";

    /** Small font icon using a 20x20 grid. */
    public static final String DELETE_SMALL = "opencms-icon-delete-20";

    /** Small font icon using a 20x20 grid. */
    public static final String LOCK_SMALL = "opencms-icon-lock-20";

    /** Small font icon using a 20x20 grid. */
    public static final String PREVIEW_SMALL = "opencms-icon-preview-20";

    /** Small font icon using a 20x20 grid. */
    public static final String EDIT_DOWN_SMALL = "opencms-icon-edit-down-20";

    /** Small font icon using a 20x20 grid. */
    public static final String EDIT_UP_SMALL = "opencms-icon-edit-up-20";

    /** Small font icon using a 20x20 grid. */
    public static final String TRASH_SMALL = "opencms-icon-trash-20";

    /** Small font icon using a 20x20 grid. */
    public static final String CHECK_SMALL = "opencms-icon-check-20";
}

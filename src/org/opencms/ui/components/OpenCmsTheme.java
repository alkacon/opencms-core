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

package org.opencms.ui.components;

import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

/**
 * Contains the CSS style name constants used within the theme.<p>
 */
public final class OpenCmsTheme {

    /** CSS style name. */
    public static final String APP_BUTTON = "o-app-button";

    /** CSS style name. */
    public static final String APP_BUTTON_PADDED = "o-app-button-padded";

    /** CSS style name. */
    public static final String APP_CONTENT = "o-app-content";

    /** CSS style name. */
    public static final String APP_INFO = "o-app-info";

    /** CSS style name. */
    public static final String BUTTON_BLUE = "o-button-blue";

    /** CSS style name. */
    public static final String BUTTON_ICON = "o-button-icon";

    /** CSS style name. */
    public static final String BUTTON_INVISIBLE = "o-button-invisible";

    /** CSS style name. */
    public static final String BUTTON_PRESSED = "o-button-pressed";

    /** CSS style name. */
    public static final String BUTTON_TABLE_ICON = "o-button-table-icon";

    /** CSS style name. */
    public static final String BUTTON_UNPADDED = "o-button-unpadded";

    /** CSS style name. */
    public static final String COLOR_BLUE = "o-color-blue";

    /** CSS style name. */
    public static final String COLOR_CYAN = " o-color-cyan";

    /** CSS style name. */
    public static final String COLOR_GRAY = "o-color-gray";

    /** CSS style name. */
    public static final String COLOR_ORANGE = "o-color-orange";

    /** CSS style name. */
    public static final String COLOR_RED = "o-color-red";

    /** CSS style name. */
    public static final String COLOR_TRANSPARENT = "o-color-transparent";

    /** CSS style name. */
    public static final String CRUMB_WRAPPER = "o-crumb-wrapper";

    /** CSS style name. */
    public static final String CRUMBS = "o-crumbs";

    /** CSS style name. */
    public static final String DIALOG = "o-dialog";

    /** CSS style name. */
    public static final String DIALOG_BUTTON_BAR = "o-dialog-button-bar";

    /** CSS style name. */
    public static final String DIALOG_CONTENT = "o-dialog-content";

    /** CSS style name. */
    public static final String DIALOG_FORM = "o-dialog-form";

    /** CSS style name. */
    public static final String DIFF_TYPE_ADDED = "diffTypeAdded";

    /** CSS style name. */
    public static final String DIFF_TYPE_CHANGED = "diffTypeChanged";

    /** CSS style name. */
    public static final String DIFF_TYPE_DELETED = "diffTypeDeleted";

    /** CSS style name. */
    public static final String DIFF_TYPE_UNCHANGED = "diffTypeUnchanged";

    /** CSS style name. */
    public static final String DISABLED = "o-disabled";

    /** CSS style name. */
    public static final String DROPDOWN = "o-dropdown";

    /** CSS style name. */
    public static final String EXPIRED = "o-expired";

    /** CSS style name. */
    public static final String FORMLAYOUT_WORKPLACE_MAIN = "o-formlayout-workplace-main";

    /** CSS style name. */
    public static final String FULL_WIDTH_PADDING = "o-full-width-padding";

    /** CSS style name. */
    public static final String HIDDEN = "hidden";

    /** CSS style name. */
    public static final String HIDDEN_TOOLBAR = "o-hidden-toolbar";

    /** CSS style name. */
    public static final String HOVER_COLUMN = "o-hover-column";

    /** CSS style name. */
    public static final String IN_NAVIGATION = "o-in-navigation";

    /** CSS style name. */
    public static final String INLINE_TEXTFIELD = "o-inline-textfield";

    /** CSS style name. */
    public static final String LABEL_ERROR = "o-label-error";

    /** CSS style name. */
    public static final String LOCK_OTHER = "o-lock-other";

    /** CSS style name. */
    public static final String LOCK_PUBLISH = "o-lock-publish";

    /** CSS style name. */
    public static final String LOCK_SHARED = "o-lock-shared";
    /** CSS style name. */
    public static final String LOCK_USER = "o-lock-user";

    /** CSS style name. */
    public static final String MAIN = "o-main";
    /** CSS style name. */
    public static final String NAVIGATOR_DROPDOWN = "o-navigator-dropdown";

    /** CSS style name. */
    public static final String NO_TRANSLATION_ICON = "o-notranslation";

    /** Path to the OpenCms logo theme resource. */
    public static final String OPENCMS_LOGO_PATH = "img/opencmsLogo.png";

    /** CSS style name. */
    public static final String POINTER = "o-pointer";

    /** Path to the project current theme resource. */
    public static final String PROJECT_CURRENT_PATH = "img/project_current.png";

    /** CSS style name. */
    public static final String PROJECT_OTHER = "o-project-other";

    /** Path to the project other theme resource. */
    public static final String PROJECT_OTHER_PATH = "img/project_other.png";

    /** Path to the project publish theme resource. */
    public static final String PROJECT_PUBLISH_PATH = "img/project_publish.png";

    /** CSS style name. */
    public static final String REDUCED_MARGIN = "o-reduced-margin";

    /** CSS style name. */
    public static final String REDUCED_SPACING = "o-reduced-spacing";

    /** CSS style name. */
    public static final String REQUIRED_BUTTON = "o-required-button";

    /** CSS style name. */
    public static final String RESINFO_HIDDEN_ICON = "o-resinfo-hidden-icon";

    /** CSS style name. */
    public static final String RESINFO_POINTER = "o-resinfo-pointer";

    /** CSS style name. */
    public static final String RESOURCE_ICON = "o-resource-icon";

    /** CSS style name. */
    public static final String RESOURCE_INFO = "o-resource-info";

    /** CSS style name. */
    public static final String RESOURCE_INFO_DIRECTLINK = "o-resourceinfo-directlink";

    /** CSS style name. */
    public static final String RESOURCE_INFO_WEAK = "o-resourceinfo-weak";

    /** CSS style name. */
    public static final String RESPONSIVE = "o-responsive";

    /** CSS style name. */
    public static final String SECURITY = "o-security";

    /** CSS style name. */
    public static final String SECURITY_INVALID = SECURITY + "-invalid";

    /** CSS style name. */
    public static final String SECURITY_STRONG = SECURITY + "-strong";

    /** CSS style name. */
    public static final String SECURITY_WEAK = SECURITY + "-weak";

    /** CSS style name. */
    public static final String SIBLING = "o-sibling";

    /** CSS style name. */
    public static final String SIMPLE_DRAG = "o-simple-drag";

    /** CSS style name. */
    public static final String SITEMAP_LOCALE_BAR = "o-sitemap-locale-bar";

    /** CSS style name. */
    public static final String STATE_CHANGED = "o-state-changed";

    /** CSS style name. */
    public static final String STATE_DELETED = "o-state-deleted";

    /** CSS style name. */
    public static final String STATE_NEW = "o-state-new";

    /** CSS style name. */
    public static final String TABLE_CELL_PADDING = "o-table-cell-padding";

    /** CSS style name. */
    public static final String TOOLABER_APP_INDICATOR = "o-toolbar-app-indicator";

    /** CSS style name. */
    public static final String TOOLABER_APP_INDICATOR_ONLINE = "o-toolbar-app-indicator-online";

    /** CSS style name. */
    public static final String TOOLBAR = "o-toolbar";

    /** CSS style name. */
    public static final String TOOLBAR_BUTTON = "o-toolbar-button";

    /** CSS style name. */
    public static final String TOOLBAR_CENTER = "o-toolbar-center";

    /** CSS style name. */
    public static final String TOOLBAR_FIELD = "o-toolbar-field";

    /** CSS style name. */
    public static final String TOOLBAR_INNER = "o-toolbar-inner";

    /** CSS style name. */
    public static final String TOOLBAR_ITEMS_LEFT = "o-toolbar-items-left";

    /** CSS style name. */
    public static final String TOOLBAR_ITEMS_RIGHT = "o-toolbar-items-right";

    /** CSS style name. */
    public static final String TOOLBAR_LOGO = "o-toolbar-logo";

    /** CSS style name. */
    public static final String TOOLS_BREADCRUMB = "o-tools-breadcrumb";

    /** CSS style name. */
    public static final String USER_IMAGE = "o-user-image";

    /** CSS style name. */
    public static final String USER_INFO = "o-user-info";

    /** CSS style name. */
    public static final String VERTICAL_MENU = "o-verticalmenu";

    /** CSS style name. */
    public static final String VERTICAL_MENU_ITEM = "o-verticalmenu-menuitem";

    /** CSS style name. */
    public static final String WORKPLACE_MAXWIDTH = "o-workplace-maxwidth";

    /** CSS style name. */
    protected static final String QUICK_LAUNCH = "o-quicklaunch";

    /**
     * Hidden default constructor.
     */
    private OpenCmsTheme() {
        // hidden default constructor, do nothing
    }

    /**
     * Gets the link to an image below the img-extra folder.<p>
     *
     * @param imagePath the image path below img-extra
     *
     * @return the complete image link
     */
    public static String getImageLink(String imagePath) {

        return CmsStringUtil.joinPaths(
            OpenCms.getSystemInfo().getContextPath(),
            "VAADIN/themes/opencms/img-extra",
            imagePath);
    }

}

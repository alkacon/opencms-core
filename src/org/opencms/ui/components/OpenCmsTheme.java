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
    public static final String BOOKMARKS_PLACEHOLDER = "o-bookmarks-placeholder";

    /** CSS style name. */
    public static final String BUTTON_BLUE = "o-button-blue";

    /** CSS style name. */
    public static final String BUTTON_ICON = "o-button-icon";

    /** CSS style name. */
    public static final String BUTTON_INVISIBLE = "o-button-invisible";

    /** CSS style name. */
    public static final String BUTTON_OVERLAY = "o-button-overlay";

    /** CSS style name. */
    public static final String BUTTON_OVERLAY_BLUE = "o-button-overlay-blue";

    /** CSS style name. */
    public static final String BUTTON_OVERLAY_BLUE_INVERSE = "o-button-overlay-blue-inv";

    /** CSS style name. */
    public static final String BUTTON_OVERLAY_BLUE_LIGHT = "o-button-overlay-blue-light";

    /** CSS style name. */
    public static final String BUTTON_OVERLAY_CYAN = "o-button-overlay-cyan";

    /** CSS style name. */
    public static final String BUTTON_OVERLAY_CYAN_INVERSE = "o-button-overlay-cyan-inv";

    /** CSS style name. */
    public static final String BUTTON_OVERLAY_GRAY_LIGHT = "o-button-overlay-gray-light";

    /** CSS style name. */
    public static final String BUTTON_OVERLAY_GREEN = "o-button-overlay-green";

    /** CSS style name. */
    public static final String BUTTON_OVERLAY_ORANGE = "o-button-overlay-orange";

    /** CSS style name. */
    public static final String BUTTON_OVERLAY_RED = "o-button-overlay-red";

    /** CSS style name. */
    public static final String BUTTON_PRESSED = "o-button-pressed";

    /** CSS style name. */
    public static final String BUTTON_RED = "o-button-red";

    /** CSS style name. */
    public static final String BUTTON_SITE = "o-button-site";

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
    public static final String CRUMB_WRAPPER = "o-crumb-wrapper";

    /** CSS style name. */
    public static final String CRUMBS = "o-crumbs";

    /** CSS style name.*/
    public static final String DELETE_UNUSED = "oc-icon-32-delete-unused";

    /** CSS style name. */
    public static final String DIALOG = "o-dialog";

    /** CSS style name. */
    public static final String DIALOG_BUTTON_BAR = "o-dialog-button-bar";

    /** CSS style name. */
    public static final String DIALOG_CONTENT = "o-dialog-content";

    /** CSS style name. */
    public static final String DIALOG_CONTENT_PANEL = "o-dialog-content-panel";

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
    public static final String FILE_TREE = "o-file-tree";

    /** CSS style name. */
    public static final String FORMLAYOUT_WORKPLACE_MAIN = "o-formlayout-workplace-main";

    /** CSS style name. */
    public static final String FULL_WIDTH_PADDING = "o-full-width-padding";

    /** CSS style name. */
    public static final String GALLERY_ALERT_IN_USE = "o-gallery-alert-in-use";

    /** CSS style name. */
    public static final String GALLERY_FORM = "o-gallery-form";

    /** CSS style name. */
    public static final String GALLERY_GRID_ROW_ODD = "o-gallery-grid-row-odd";

    /** CSS style name. */
    public static final String GALLERY_PREVIEW_IMAGE = "o-gallery-preview-image";

    /** CSS style name. */
    public static final String HIDDEN = "hidden";

    /** CSS style name. */
    public static final String HIDDEN_TOOLBAR = "o-hidden-toolbar";

    /** CSS style name. */
    public static final String HOVER_COLUMN = "o-hover-column";

    /** The cache icon CSS class.*/
    public static final String ICON_CACHE = "oc-icon-24-cache";

    /** The database icon CSS class. */
    public static final String ICON_DATABASE = "oc-icon-24-database";

    /** CSS class for the date search app icon. */
    public static final String ICON_DATE_SEARCH = "oc-icon-32-date-search";

    /** CSS style name.*/
    public static final String ICON_EXLPORER = "oc-icon-24-folder";

    /** CSS style name.*/
    public static final String ICON_EXLPORER_BIG = "oc-icon-32-explorer";

    /** The group icon CSS class. */
    public static final String ICON_GROUP = "oc-icon-24-group";

    /** The job icon CSS class. */
    public static final String ICON_JOB = "oc-icon-24-scheduler";

    /**The Log icon. */
    public static final String ICON_LOG = "oc-icon-24-log";

    /** CSS class for a newsletter app icon (app is not part of core). */
    public static final String ICON_NEWSLETTER = "oc-icon-32-newsletter";

    /**Icon for OUs. */
    public static final String ICON_OU = "oc-icon-24-orgunit";

    /**Icon for OUs for web user. */
    public static final String ICON_OU_WEB = "oc-icon-24-webuser";

    /** CSS style name.*/
    public static final String ICON_PERSON_DATA = "oc-icon-32-person-data";

    /** CSS class for a redirect app icon (app is not part of core). */
    public static final String ICON_REDIRECT = "oc-icon-32-redirect";

    /** The icon for all principal option (currently used in principal select).*/
    public static final String ICON_PRINCIPAL_ALL = "oc-icon-24-principal-all";

    /** The icon for overwriting principal (currently used in principal select).*/
    public static final String ICON_PRINCIPAL_OVERWRITE = "oc-icon-24-principal-overwrite";

    /** The project icon CSS class. */
    public static final String ICON_PROJECT = "oc-icon-24-project";

    /** Path to the project current theme resource. */
    public static final String ICON_PROJECT_CURRENT = "oc-icon-24-project_yellow";

    /** Path to the project other theme resource. */
    public static final String ICON_PROJECT_OTHER = "oc-icon-24-project_red";

    /** Path to the project publish theme resource. */
    public static final String ICON_PUBLISH = "oc-icon-24-publish";

    /** CSS style name.*/
    public static final String ICON_RESOURCE_TYPES = "oc-icon-32-resource-types";

    /** The role icon CSS class.*/
    public static final String ICON_ROLE = "oc-icon-24-role";

    /** The search icon. */
    public static final String ICON_SEARCH = "oc-icon-32-search";

    /**The session icon.*/
    public static final String ICON_SESSION = "oc-icon-24-session";

    /** The cache icon CSS class. */
    public static final String ICON_SITE = "oc-icon-24-site";

    /** CSS style name.*/
    public static final String ICON_TERMINAL = "oc-icon-32-terminal";

    /** CSS style name.*/
    public static final String ICON_TOOL_1 = "oc-icon-32-tool1";

    /** CSS style name.*/
    public static final String ICON_TOOL_2 = "oc-icon-32-tool2";

    /** CSS style name.*/
    public static final String ICON_TOOL_3 = "oc-icon-32-tool3";

    /** CSS style name.*/
    public static final String ICON_TOOL_4 = "oc-icon-32-tool4";

    /** CSS style name.*/
    public static final String ICON_TOOL_5 = "oc-icon-32-tool5";

    /** The user icon CSS class.*/
    public static final String ICON_USER = "oc-icon-24-user";

    /** CSS style name. */
    public static final String IMAGE_GRADIENT = "o-image-gradient";

    /** CSS style name. */
    public static final String IMAGE_TRANSPARENT = "o-image-transparent";

    /** CSS style name. */
    public static final String IN_NAVIGATION = "o-in-navigation";

    /** CSS style name. */
    public static final String INFO = "o-info-dialog";

    /** CSS style name. */
    public static final String INFO_ELEMENT_NAME = "o-info-dialog-name";

    /** CSS style name. */
    public static final String INFO_ELEMENT_VALUE = "o-info-dialog-value";

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
    public static final String PADDING_HORIZONTAL = "o-padding-horizontal";

    /** CSS style name. */
    public static final String POINTER = "o-pointer";

    /** CSS style name. */
    public static final String PROJECT_OTHER = "o-project-other";

    /** CSS style name. */
    public static final String QUICK_LAUNCH_EDITOR = "o-quicklaunch-editor";

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

    /** CSS style name.*/
    public static final String TABLE_CELL_DISABLED = "o-table-cell-disabled";

    /** CSS style name. */
    public static final String TABLE_CELL_PADDING = "o-table-cell-padding";

    /** CSS style name. */
    public static final String TABLE_COLUMN_BOX_BLACK = " o-box-black";

    /** CSS style name. */
    public static final String TABLE_COLUMN_BOX_BLUE = " o-box-blue";

    /** CSS style name. */
    public static final String TABLE_COLUMN_BOX_BLUE_LIGHT = " o-box-blue-light";

    /** CSS style name. */
    public static final String TABLE_COLUMN_BOX_CYAN = " o-box-cyan";

    /** CSS style name. */
    public static final String TABLE_COLUMN_BOX_DARKGRAY = " o-box-gray-darker";

    /** CSS style name. */
    public static final String TABLE_COLUMN_BOX_DARKRED = " o-box-red-dark";

    /** CSS style name. */
    public static final String TABLE_COLUMN_BOX_GRAY = " o-box-gray";

    /** CSS style name. */
    public static final String TABLE_COLUMN_BOX_GRAY_DARKER = " o-box-gray-darker";

    /** CSS style name. */
    public static final String TABLE_COLUMN_BOX_GREEN = " o-box-green";

    /** CSS style name. */
    public static final String TABLE_COLUMN_BOX_ORANGE = " o-box-orange";

    /** CSS style name. */
    public static final String TABLE_COLUMN_BOX_ORANGE_DARK = " o-box-orange-dark";

    /** CSS style name. */
    public static final String TABLE_COLUMN_BOX_RED = " o-box-red";

    /** CSS style name. */
    public static final String TABLE_CONST_COLOR = "o-table-const-color";

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
    public static final String TYPE_FILTER_BUTTON = "o-type-filter-button";

    /** CSS style name. */
    public static final String TYPE_FILTER_BUTTON_ACTIVE = "o-type-filter-button-active";

    /** CSS style name. */
    public static final String TYPE_SELECT = "o-type-select-box";

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

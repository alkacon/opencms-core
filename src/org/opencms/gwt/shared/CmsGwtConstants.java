/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.shared;

/**
 * Common constants needed for client side code.<p>
 */
public final class CmsGwtConstants {

    /** Parameters for favorite dialog. */
    public static final class Favorites {

        /** Request parameter. */
        public static final String PARAM_DETAIL = "detail";

        /** Request parameter. */
        public static final String PARAM_PAGE = "page";

        /** Request parameter. */
        public static final String PARAM_PROJECT = "project";

        /** Request parameter. */
        public static final String PARAM_SITE = "site";
    }

    /**
    * Quick launcher constants.<p>
    */
    public static final class QuickLaunch {

        /** Context string. */
        public static final String CONTEXT_PAGE = "page";

        /** Context string. */
        public static final String CONTEXT_SITEMAP = "sitemap";

        /** Quick launch id. */
        public static final String Q_ACCOUNTMANAGER = "accountmanager";

        /** Quick launch id. */
        public static final String Q_EXPLORER = "explorer";

        /** Quick launch id. */
        public static final String Q_LAUNCHPAD = "launchpad";

        /** Quick launch id. */
        public static final String Q_PAGEEDITOR = "pageeditor";

        /** Quick launch id. */
        public static final String Q_SITEMAP = "sitemap";

        /** Quick launch id. */
        public static final String Q_WORKPLACETOOLS = "workplacetools";

    }

    public static final class RpcContext {

        public static final String PAGE_ID = "pageId";
    }

    /** Context menu action id. */
    public static final String ACTION_EDITSMALLELEMENTS = "editsmallelements";

    /** Context menu action parameter dialog id. */
    public static final String ACTION_PARAM_DIALOG_ID = "dialogId";

    /** Context menu action id. */
    public static final String ACTION_SELECTELEMENTVIEW = "selectelementview";

    /** Context menu action id. */
    public static final String ACTION_SHOWLOCALE = "showlocale";

    /** Context menu action id. */
    public static final String ACTION_TEMPLATECONTEXTS = "templatecontexts";

    /** Context menu action id. */
    public static final String ACTION_TEMPLATECONTEXTS_ADVANCED = "templatecontexts_advanced";

    /** Context menu action id. */
    public static final String ACTION_VIEW_ONLINE = "viewonline";

    /** Attribute for container id. */
    public static final String ATTR_CONTAINER_ID = "ATTR_CONTAINER_ID";

    /** Collector data attribute name. */
    public static final String ATTR_DATA_COLLECTOR = "data-oc-collector";

    /** Container data attribute name. */
    public static final String ATTR_DATA_CONTAINER = "data-oc-container";

    /** Editable data attribute name. */
    public static final String ATTR_DATA_EDITABLE = "data-oc-editable";

    /** Element data attribute name. */
    public static final String ATTR_DATA_ELEMENT = "data-oc-element";

    /** Content field data attribute name. */
    public static final String ATTR_DATA_FIELD = "data-oc-field";

    /** Entity id data attribute name. */
    public static final String ATTR_DATA_ID = "data-oc-id";

    /** Name for the attribute used to store list-add metadata. */
    public static final String ATTR_DATA_LISTADD = "data-oc-listadd";

    /** Marker attribute for dead links. */
    public static final String ATTR_DEAD_LINK_MARKER = "data-oc-broken-link";

    /** Key for the element id attribute. */
    public static final String ATTR_ELEMENT_ID = "ATTR_ELEMENT_ID";

    public static final String ATTR_EXTENSIONS = "ext";

    /** Element data to enable / disable the favorite button. */
    public static final String ATTR_FAVORITE = "fav";

    /** Key for the page root path attribute. */
    public static final String ATTR_PAGE_ROOT_PATH = "ATTR_PAGE_ROOT_PATH";

    /** Name of the Javascript callback used to handle property changes triggered from the locale compare view. */
    public static final String CALLBACK_HANDLE_CHANGED_PROPERTIES = "cmsHandleChangedProperties";

    /** Javascript function name for showing the locale compare view. */
    public static final String CALLBACK_REFRESH_LOCALE_COMPARISON = "cmsRefreshLocaleComparison";

    /** The CSS class name used for the DOM elements containing collector information. */
    public static final String CLASS_COLLECTOR_INFO = "oc-collector-info";

    /** CSS class for containers inflated due to an element dragged into them that is of lower height than the empty container HTML. */
    public static final String CLASS_CONTAINER_INFLATED = "oc-container-inflated";

    /** CSS class for elements in detail containers which are used to transfer their settings to the detail eleemnt. */
    public static final String CLASS_DETAIL_PREVIEW = "oc-detail-preview";

    /** The CSS class name for data elements which precede the direct editable elements. */
    public static final String CLASS_EDITABLE = "oc-editable";

    /** The CSS class name for elements which end the direct editable elements. */
    public static final String CLASS_EDITABLE_END = CLASS_EDITABLE + "-end";

    /** The CSS class name for elements which mark elements to be skipped during direct edit. */
    public static final String CLASS_EDITABLE_SKIP = CLASS_EDITABLE + "-skip";

    /** Class for dnd placeholders which are too big. */
    public static final String CLASS_PLACEHOLDER_TOO_BIG = "oc-placeholder-too-big";

    /** Name for configuration  setting to limit collector results while computing publish lists for collectors. */
    public static final String COLLECTOR_PUBLISH_LIST_LIMIT = "collectorPublishListLimit";

    /** The context type sitemap toolbar. */
    public static final String CONTEXT_TYPE_APP_TOOLBAR = "appToolbar";

    /** The context type containerpage toolbar. */
    public static final String CONTEXT_TYPE_CONTAINERPAGE_TOOLBAR = "containerpageToolbar";

    /** The context type file table. */
    public static final String CONTEXT_TYPE_FILE_TABLE = "fileTable";

    /** The context type sitemap toolbar. */
    public static final String CONTEXT_TYPE_SITEMAP_TOOLBAR = "sitemapToolbar";

    /** Special 'type' name (that isn't actually a type) used to mark default detail pages in the sitemap configuration. */
    public static final String DEFAULT_DETAILPAGE_TYPE = "##DEFAULT##";

    public static final String EDITOR_PAGE_ID = "pageId";

    /** A HTML comment that will cause the container page editor to reload the page if it is contained in HTML which is reloaded after the element or its settings have been edited. */
    public static final String FORMATTER_RELOAD_MARKER = "<!--FORMATTER_RELOAD_g3jf9o0n-->";

    /** Separator for separating the main part of a formatter key from the sub-key (the part in front of the separator will be used as a key if there is no formatter for the full key). */
    public static final String FORMATTER_SUBKEY_SEPARATOR = "#";

    /** Sub-path for the page unlock service. */
    public static final String HANDLER_UNLOCK_PAGE = "/unlockPage";

    /** Sub-path for the session update handler. */
    public static final String HANDLER_UPDATE_SESSION = "/updateSession";

    /** The settings widget name for hidden entries. */
    public static final String HIDDEN_SETTINGS_WIDGET_NAME = "hidden";

    /** Element id for locale comparison view. */
    public static final String ID_LOCALE_COMPARISON = "cmsLocaleComparison";

    /** Id of the element ussed to  display the sitemap header from Vaadin code. */
    public static final String ID_LOCALE_HEADER_CONTAINER = "locale-header-container";

    /** Json field name for typograf locale passed to input widget. */
    public static final String JSON_INPUT_LOCALE = "locale";

    /** Json field name for enabling typograf, passed to input widget. */
    public static final String JSON_INPUT_TYPOGRAF = "typograf";

    /** Field name for the internal textarea configuration. */
    public static final String JSON_TEXTAREA_CONFIG = "config";

    /** Field name for the internal textarea configuration. */
    public static final String JSON_TEXTAREA_LOCALE = "locale";

    /**
     * Session storage key for memorizing the last opened container page.
     */
    public static final String LAST_CONTAINER_PAGE_ID = "lastContainerPageId";

    /** Name of the Javascript callback used to open the locale comparison view. */
    public static final String LOCALECOMPARE_EDIT_PROPERTIES = "cmsLocaleCompareEditProperties";

    /** The maximum DND placeholder height. */
    public static final int MAX_PLACEHOLDER_HEIGHT = 300;

    /** Name of the meta tag used to set the editor stylesheet. */
    public static final String META_EDITOR_STYLESHEET = "cms-editor-stylesheet";

    /** Parameter to pass detail id to page editor context menu actions. */
    public static final String PARAM_ADE_DETAIL_ID = "adeDetailId";

    /** Parameter for the button left position. */
    public static final String PARAM_BUTTON_LEFT = "__buttonLeft";

    /** Parameter to disable direct edit. */
    public static final String PARAM_DISABLE_DIRECT_EDIT = "__disableDirectEdit";

    /** Name of the request parameter used to store the redirect target after login. */
    public static final String PARAM_LOGIN_REDIRECT = "loginRedirect";

    /** Parameter to force a specific template context. */
    public static final String PARAM_TEMPLATE_CONTEXT = "__templateContext";

    /** Marker for the 'prefill' context menu action. */
    public static final String PREFILL_MENU_PLACEHOLDER = "PREFILL_MENU_PLACEHOLDER";

    /** Name of property containing focal point for images. */
    public static final String PROPERTY_IMAGE_FOCALPOINT = "image.focalpoint";

    /** Tab id for the preference dialog. */
    public static final String TAB_BASIC = "basic";

    /** Tab id for the preference dialog. */
    public static final String TAB_EXTENDED = "extended";

    /** Tab id for the preference dialog. */
    public static final String TAB_HIDDEN = "hidden";

    /** Tag name for the edit button bars injected into the page HTML in the container page editor. */
    public static final String TAG_OC_EDITPOINT = "oc-editpoint";

    /** Tag name for the list-add metadata injected into the page. */
    public static final String TAG_OC_LISTADD = "oc-listadd";

    /** Action placeholder for the template contexts menu option. */
    public static final String TEMPLATECONTEXT_MENU_PLACEHOLDER = "templatecontexts";

    /** Type name for container pages. */
    public static final String TYPE_CONTAINERPAGE = "containerpage";

    /** The resource icon CSS class prefix. */
    public static final String TYPE_ICON_CLASS = "cms_type_icon";

    /** The image resource type name. */
    public static final String TYPE_IMAGE = "image";

    /** Name for the pseudo-type 'modelgroup'. */
    public static final String TYPE_MODELGROUP = "modelgroup";

    /** Name for the pseudo-type 'modelgroupreuse'. */
    public static final String TYPE_MODELGROUP_COPY = "modelgroupcopy";

    /** Name for the pseudo-type 'modelgrouppage'. */
    public static final String TYPE_MODELGROUP_PAGE = "modelgrouppage";

    /** Name for the type 'modelpage'. */
    public static final String TYPE_MODELPAGE = "modelpage";

    /** Name for the pseudo-type 'navlevel'. */
    public static final String TYPE_NAVLEVEL = "navlevel";

    /** Part of the URL used to unlock files. */
    public static final String UNLOCK_FILE_PREFIX = "/unlockFile/";

    /** Name of Javascript variable used to hold the structure id of the currently selected locale's root folder in the sitemap editor's locale comparison mode. */
    public static final String VAR_LOCALE_ROOT = "cmsLocaleCompareRoot";

    /**
     * Hide constructor.<p>
     */
    private CmsGwtConstants() {

        // nop

    }

    /**
     * Maximum DND placeholder height, as a string for stylesheets.
     *
     * @return the maximum DND placeholder height as a string
     */
    public static String getPlaceholderMaxHeight() {

        return MAX_PLACEHOLDER_HEIGHT + "px";
    }
}

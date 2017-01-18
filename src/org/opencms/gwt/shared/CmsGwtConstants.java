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
    public static final String ACTION_VIEW_ONLINE = "viewonline";

    /** Name of the Javascript callback used to handle property changes triggered from the locale compare view. */
    public static final String CALLBACK_HANDLE_CHANGED_PROPERTIES = "cmsHandleChangedProperties";

    /** Javascript function name for showing the locale compare view. */
    public static final String CALLBACK_REFRESH_LOCALE_COMPARISON = "cmsRefreshLocaleComparison";

    /** CSS class name used for the DOM elements containing collector information. */
    public static final String CLASS_COLLECTOR_INFO = "cms-collector-info";

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

    /** A HTML comment that will cause the container page editor to reload the page if it is contained in HTML which is reloaded after the element or its settings have been edited. */
    public static final String FORMATTER_RELOAD_MARKER = "<!--FORMATTER_RELOAD_g3jf9o0n-->";

    /** Element id for locale comparison view. */
    public static final String ID_LOCALE_COMPARISON = "cmsLocaleComparison";

    /** Id of the element ussed to  display the sitemap header from Vaadin code. */
    public static final String ID_LOCALE_HEADER_CONTAINER = "locale-header-container";

    /** Name of the Javascript callback used to open the locale comparison view. */
    public static final String LOCALECOMPARE_EDIT_PROPERTIES = "cmsLocaleCompareEditProperties";

    /** Parameter for the button left position. */
    public static final String PARAM_BUTTON_LEFT = "__buttonLeft";

    /** Parameter to disable direct edit. */
    public static final String PARAM_DISABLE_DIRECT_EDIT = "__disableDirectEdit";

    /** Name of the request parameter used to store the redirect target after login. */
    public static final String PARAM_LOGIN_REDIRECT = "loginRedirect";

    /** Parameter to force a specific template context. */
    public static final String PARAM_TEMPLATE_CONTEXT = "__templateContext";

    /** Tab id for the preference dialog. */
    public static final String TAB_BASIC = "basic";

    /** Tab id for the preference dialog. */
    public static final String TAB_EXTENDED = "extended";

    /** Tab id for the preference dialog. */
    public static final String TAB_HIDDEN = "hidden";

    /** Type name for container pages. */
    public static final String TYPE_CONTAINERPAGE = "containerpage";

    /** The image resource type name. */
    public static final String TYPE_IMAGE = "image";

    /** Name for the pseudo-type 'modelgroup'. */
    public static final String TYPE_MODELGROUP = "modelgroup";

    /** Name for the pseudo-type 'modelgrouppage'. */
    public static final String TYPE_MODELGROUP_PAGE = "modelgrouppage";

    /** Name for the pseudo-type 'modelgroupreuse'. */
    public static final String TYPE_MODELGROUP_REUSE = "modelgroupreuse";

    /** Name for the type 'modelpage'. */
    public static final String TYPE_MODELPAGE = "modelpage";

    /** Name for the pseudo-type 'navlevel'. */
    public static final String TYPE_NAVLEVEL = "navlevel";

    /** Name of Javascript variable used to hold the structure id of the currently selected locale's root folder in the sitemap editor's locale comparison mode. */
    public static final String VAR_LOCALE_ROOT = "cmsLocaleCompareRoot";

    /**
     * Hide constructor.<p>
     */
    private CmsGwtConstants() {

        // nop

    }
}

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

    /** Context menu action id. */
    public static final String ACTION_EDITSMALLELEMENTS = "editsmallelements";

    /** Context menu action id. */
    public static final String ACTION_TEMPLATECONTEXTS = "templatecontexts";

    /** A HTML comment that will cause the container page editor to reload the page if it is contained in HTML which is reloaded after the element or its settings have been edited. */
    public static final String FORMATTER_RELOAD_MARKER = "<!--FORMATTER_RELOAD_g3jf9o0n-->";

    /** Parameter to disable direct edit. */
    public static final String PARAM_DISABLE_DIRECT_EDIT = "__disableDirectEdit";

    /** Parameter to force a specific template context. */
    public static final String PARAM_TEMPLATE_CONTEXT = "__templateContext";
    /** Type name for container pages. */
    public static final String TYPE_CONTAINERPAGE = "containerpage";

    /** CSS class name used for the DOM elements containing collector information. */
    public static final String CLASS_COLLECTOR_INFO = "cms-collector-info";

    /** Tab id for the preference dialog. */
    public static final String TAB_BASIC = "basic";

    /** Tab id for the preference dialog. */
    public static final String TAB_EXTENDED = "extended";

    /** Tab id for the preference dialog. */
    public static final String TAB_HIDDEN = "hidden";

    /** Name for the pseudo-type 'navlevel'. */
    public static final String TYPE_NAVLEVEL = "navlevel";

    /** Name for configuration  setting to limit collector results while computing publish lists for collectors. */
    public static final String COLLECTOR_PUBLISH_LIST_LIMIT = "collectorPublishListLimit";

    /**
     * Hide constructor.<p>
     */
    private CmsGwtConstants() {

        // nop

    }
}

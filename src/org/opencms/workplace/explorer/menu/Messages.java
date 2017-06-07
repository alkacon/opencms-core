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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.explorer.menu;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p>
 *
 * @since 6.0.0
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String ERR_INITIALIZE_MENUITEMRULE_1 = "ERR_INITIALIZE_MENUITEMRULE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_MENURULE_FROZEN_0 = "ERR_MENURULE_FROZEN_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONTEXTMENU_TITLE_INACTIVE_DELETED_0 = "GUI_CONTEXTMENU_TITLE_INACTIVE_DELETED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONTEXTMENU_TITLE_INACTIVE_LOCK_INHERITED_0 = "GUI_CONTEXTMENU_TITLE_INACTIVE_LOCK_INHERITED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONTEXTMENU_TITLE_INACTIVE_MOVED_0 = "GUI_CONTEXTMENU_TITLE_INACTIVE_MOVED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONTEXTMENU_TITLE_INACTIVE_NEW_UNCHANGED_0 = "GUI_CONTEXTMENU_TITLE_INACTIVE_NEW_UNCHANGED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONTEXTMENU_TITLE_INACTIVE_NOSIBLINGS_0 = "GUI_CONTEXTMENU_TITLE_INACTIVE_NOSIBLINGS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONTEXTMENU_TITLE_INACTIVE_NOTDELETED_0 = "GUI_CONTEXTMENU_TITLE_INACTIVE_NOTDELETED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONTEXTMENU_TITLE_INACTIVE_NOTLOCKED_0 = "GUI_CONTEXTMENU_TITLE_INACTIVE_NOTLOCKED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONTEXTMENU_TITLE_INACTIVE_ONLINEPROJECT_0 = "GUI_CONTEXTMENU_TITLE_INACTIVE_ONLINEPROJECT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONTEXTMENU_TITLE_INACTIVE_OTHERPROJECT_0 = "GUI_CONTEXTMENU_TITLE_INACTIVE_OTHERPROJECT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONTEXTMENU_TITLE_INACTIVE_PERM_PUBLISH_0 = "GUI_CONTEXTMENU_TITLE_INACTIVE_PERM_PUBLISH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONTEXTMENU_TITLE_INACTIVE_PERM_WRITE_0 = "GUI_CONTEXTMENU_TITLE_INACTIVE_PERM_WRITE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONTEXTMENU_TITLE_INACTIVE_PUBLISH_INHERITED_0 = "GUI_CONTEXTMENU_TITLE_INACTIVE_PUBLISH_INHERITED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONTEXTMENU_TITLE_INACTIVE_PUBLISH_NOT_LOCKED_0 = "GUI_CONTEXTMENU_TITLE_INACTIVE_PUBLISH_NOT_LOCKED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONTEXTMENU_TITLE_INACTIVE_PUBLISH_OTHERPROJECT_0 = "GUI_CONTEXTMENU_TITLE_INACTIVE_PUBLISH_OTHERPROJECT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONTEXTMENU_TITLE_INACTIVE_PUBLISH_PARENTFOLDER_0 = "GUI_CONTEXTMENU_TITLE_INACTIVE_PUBLISH_PARENTFOLDER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONTEXTMENU_TITLE_INACTIVE_UNCHANGED_0 = "GUI_CONTEXTMENU_TITLE_INACTIVE_UNCHANGED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_CONTEXTMENU_TITLE_OTHER_SITE_INACTIVE_0 = "GUI_CONTEXTMENU_TITLE_OTHER_SITE_INACTIVE_0";

    /** Name of the resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.workplace.explorer.menu.messages";

    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new Messages();

    /**
     * Hides the public constructor for this utility class.<p>
     */
    private Messages() {

        // hide the constructor
    }

    /**
     * Returns an instance of this localized message accessor.<p>
     *
     * @return an instance of this localized message accessor
     */
    public static I_CmsMessageBundle get() {

        return INSTANCE;
    }

    /**
     * Returns the bundle name for this OpenCms package.<p>
     *
     * @return the bundle name for this OpenCms package
     */
    public String getBundleName() {

        return BUNDLE_NAME;
    }
}
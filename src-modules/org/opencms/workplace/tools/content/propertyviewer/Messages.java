/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.content.propertyviewer;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.
 * <p>
 *
 * @since 7.5.1
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LIST_PROPERTYVIEW_COL_ICON_HELP_0 = "GUI_LIST_PROPERTYVIEW_COL_ICON_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LIST_PROPERTYVIEW_COL_ICON_NAME_0 = "GUI_LIST_PROPERTYVIEW_COL_ICON_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LIST_PROPERTYVIEW_COL_ID_HELP_0 = "GUI_LIST_PROPERTYVIEW_COL_ID_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LIST_PROPERTYVIEW_COL_ID_NAME_0 = "GUI_LIST_PROPERTYVIEW_COL_ID_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LIST_PROPERTYVIEW_COL_PATH_HELP_0 = "GUI_LIST_PROPERTYVIEW_COL_PATH_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LIST_PROPERTYVIEW_COL_PATH_NAME_0 = "GUI_LIST_PROPERTYVIEW_COL_PATH_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LIST_PROPERTYVIEW_COL_PROPERTY_HELP_0 = "GUI_LIST_PROPERTYVIEW_COL_PROPERTY_HELP_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LIST_PROPERTYVIEW_COL_PROPERTY_NAME_1 = "GUI_LIST_PROPERTYVIEW_COL_PROPERTY_NAME_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LIST_PROPERTYVIEW_NAME_0 = "GUI_LIST_PROPERTYVIEW_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_LIST_PROPERTYVIEW_NOTFOUND_0 = "GUI_LIST_PROPERTYVIEW_NOTFOUND_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_MODULES_DETAIL_DIALOG_MESSAGE_0 = "GUI_MODULES_DETAIL_DIALOG_MESSAGE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_PROPERTYVIEW_ADMIN_TOOL_BLOCK_0 = "GUI_PROPERTYVIEW_ADMIN_TOOL_BLOCK_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERR_PROPERTYVIEWER_READALLPROPS_0 = "LOG_ERR_PROPERTYVIEWER_READALLPROPS_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERR_PROPERTYVIEWER_READONEPROP_2 = "LOG_ERR_PROPERTYVIEWER_READONEPROP_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERR_PROPERTYVIEWER_READRESOURCES_0 = "LOG_ERR_PROPERTYVIEWER_READRESOURCES_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_ERR_PROPERTYVIEWER_READSIBL_1 = "LOG_ERR_PROPERTYVIEWER_READSIBL_1";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.workplace.tools.content.propertyviewer.messages";

    /** Static instance member. */
    private static final I_CmsMessageBundle INSTANCE = new Messages();

    /**
     * Hides the public constructor for this utility class.
     * <p>
     */
    private Messages() {

        // hide the constructor
    }

    /**
     * Returns an instance of this localized message accessor.
     * <p>
     *
     * @return an instance of this localized message accessor
     */
    public static I_CmsMessageBundle get() {

        return INSTANCE;
    }

    /**
     * Returns the bundle name for this OpenCms package.
     * <p>
     *
     * @return the bundle name for this OpenCms package
     */
    public String getBundleName() {

        return BUNDLE_NAME;
    }
}

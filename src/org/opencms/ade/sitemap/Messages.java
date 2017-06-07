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

package org.opencms.ade.sitemap;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p>
 *
 * @since 8.0.0
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String ERR_ALIAS_DUPLICATE_ALIAS_PATH_0 = "ERR_ALIAS_DUPLICATE_ALIAS_PATH_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_ALIAS_INVALID_ALIAS_PATH_0 = "ERR_ALIAS_INVALID_ALIAS_PATH_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_ALIAS_RESOURCE_NOT_FOUND_0 = "ERR_ALIAS_RESOURCE_NOT_FOUND_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CREATING_SUB_SITEMAP_WRONG_CONFIG_FILE_TYPE_2 = "ERR_CREATING_SUB_SITEMAP_WRONG_CONFIG_FILE_TYPE_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_NO_FUNCTION_DETAIL_CONTAINER_1 = "ERR_NO_FUNCTION_DETAIL_CONTAINER_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_TITLE_MUST_NOT_BE_EMPTY_0 = "ERR_TITLE_MUST_NOT_BE_EMPTY_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_EDITOR_TITLE_1 = "GUI_EDITOR_TITLE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NAVIGATION_LEVEL_SUBTITLE_0 = "GUI_NAVIGATION_LEVEL_SUBTITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_NAVIGATION_LEVEL_TITLE_0 = "GUI_NAVIGATION_LEVEL_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_REDIRECT_SUB_LEVEL_0 = "GUI_REDIRECT_SUB_LEVEL_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_REDIRECT_TARGET_LABEL_0 = "GUI_REDIRECT_TARGET_LABEL_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SITEMAP_NO_EDIT_0 = "GUI_SITEMAP_NO_EDIT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_VFS_PATH_0 = "GUI_VFS_PATH_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_SITEMAP_NO_EDIT_ONLINE_0 = "GUI_SITEMAP_NO_EDIT_ONLINE_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.ade.sitemap.messages";

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

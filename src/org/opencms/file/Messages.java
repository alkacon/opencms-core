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

package org.opencms.file;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p>
 *
 * @since 6.0.0
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String ERR_BAD_ORGUNIT_2 = "ERR_BAD_ORGUNIT_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_BAD_PROPERTYNAME_4 = "ERR_BAD_PROPERTYNAME_4";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_BAD_PROPERTYNAME_EMPTY_0 = "ERR_BAD_PROPERTYNAME_EMPTY_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_BAD_RESOURCENAME_4 = "ERR_BAD_RESOURCENAME_4";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_BAD_RESOURCENAME_DOTS_1 = "ERR_BAD_RESOURCENAME_DOTS_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_BAD_RESOURCENAME_EMPTY_0 = "ERR_BAD_RESOURCENAME_EMPTY_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_DELETE_RESOURCE_1 = "ERR_DELETE_RESOURCE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_EMPTY_SITEROOT_0 = "ERR_EMPTY_SITEROOT_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_HISTORYRESOURCE_2 = "ERR_HISTORYRESOURCE_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_LOCK_RESOURCE_1 = "ERR_LOCK_RESOURCE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_METHOD_NOT_IMPLEMENTED_1 = "ERR_METHOD_NOT_IMPLEMENTED_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_MOVE_SAME_FOLDER_2 = "ERR_MOVE_SAME_FOLDER_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_MOVE_SAME_NAME_1 = "ERR_MOVE_SAME_NAME_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_NONEMPTY_SITEROOT_1 = "ERR_NONEMPTY_SITEROOT_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_NOT_ALLOWED_IN_ONLINE_PROJECT_0 = "ERR_NOT_ALLOWED_IN_ONLINE_PROJECT_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_OVERWRITE_RESOURCE_2 = "ERR_OVERWRITE_RESOURCE_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_PERMALINK_1 = "ERR_PERMALINK_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_PROJECTNAME_VALIDATION_0 = "ERR_PROJECTNAME_VALIDATION_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_PROPERTY_FROZEN_1 = "ERR_PROPERTY_FROZEN_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_REWRITE_LINKS_ROOT_NOT_FOLDER_1 = "ERR_REWRITE_LINKS_ROOT_NOT_FOLDER_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_REWRITE_LINKS_ROOTS_DEPENDENT_2 = "ERR_REWRITE_LINKS_ROOTS_DEPENDENT_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_SECURITY_LOGIN_FAILED_1 = "ERR_SECURITY_LOGIN_FAILED_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_SHOWVERSION_2 = "ERR_SHOWVERSION_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UNKNOWN_RESOURCE_TYPE_1 = "ERR_UNKNOWN_RESOURCE_TYPE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_USER_ADDINFO_KEY_NULL_1 = "ERR_USER_ADDINFO_KEY_NULL_1";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.file.messages";

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

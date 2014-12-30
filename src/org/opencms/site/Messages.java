/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.site;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p> 
 * 
 * @since 6.0.0 
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String ERR_CONFIG_FROZEN_0 = "ERR_CONFIG_FROZEN_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_DUPLICATE_SERVER_NAME_1 = "ERR_DUPLICATE_SERVER_NAME_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_EMPTY_SERVER_URL_0 = "ERR_EMPTY_SERVER_URL_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_INVALID_PATH_FOR_SHARED_FOLDER_1 = "ERR_INVALID_PATH_FOR_SHARED_FOLDER_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_SITE_ALREADY_CONFIGURED_1 = "ERR_SITE_ALREADY_CONFIGURED_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_SITE_NOT_CONFIGURED_1 = "ERR_SITE_NOT_CONFIGURED_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_SECURESERVER_MISSING_1 = "ERR_SECURESERVER_MISSING_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_DEFAULT_SITE_ROOT_0 = "INIT_DEFAULT_SITE_ROOT_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_DEFAULT_SITE_ROOT_1 = "INIT_DEFAULT_SITE_ROOT_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_NO_ROOT_FOLDER_1 = "INIT_NO_ROOT_FOLDER_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_NO_ROOT_FOLDER_DEFAULT_SITE_1 = "INIT_NO_ROOT_FOLDER_DEFAULT_SITE_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_NUM_SITE_ROOTS_CONFIGURED_1 = "INIT_NUM_SITE_ROOTS_CONFIGURED_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_SITE_ROOT_ADDED_1 = "INIT_SITE_ROOT_ADDED_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_START_SITE_CONFIG_0 = "INIT_START_SITE_CONFIG_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_WORKPLACE_SITE_0 = "INIT_WORKPLACE_SITE_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_WORKPLACE_SITE_1 = "INIT_WORKPLACE_SITE_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MATCHING_REQUEST_TO_SITE_2 = "LOG_MATCHING_REQUEST_TO_SITE_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_READ_SITE_PROP_FAILED_0 = "LOG_READ_SITE_PROP_FAILED_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.site.messages";

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
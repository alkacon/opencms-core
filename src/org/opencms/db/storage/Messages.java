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
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db.storage;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p>
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String ERR_STORAGE_ACTIVE_MISSING_1 = "ERR_STORAGE_ACTIVE_MISSING_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_STORAGE_BLOB_LOAD_FAILED_2 = "ERR_STORAGE_BLOB_LOAD_FAILED_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_STORAGE_BLOB_MISSING_2 = "ERR_STORAGE_BLOB_MISSING_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_STORAGE_CONTENT_NULL_0 = "ERR_STORAGE_CONTENT_NULL_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_STORAGE_DB_CLASS_CREATE_1 = "ERR_STORAGE_DB_CLASS_CREATE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_STORAGE_DB_CLASS_INVALID_2 = "ERR_STORAGE_DB_CLASS_INVALID_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_STORAGE_DB_RESERVED_ID_1 = "ERR_STORAGE_DB_RESERVED_ID_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_STORAGE_EXTERNAL_WRITE_2 = "ERR_STORAGE_EXTERNAL_WRITE_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_STORAGE_HASH_INVALID_0 = "ERR_STORAGE_HASH_INVALID_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_STORAGE_HASH_OUTSIDE_REPOSITORY_0 = "ERR_STORAGE_HASH_OUTSIDE_REPOSITORY_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_STORAGE_LEGACY_CONTAINS_ACTIVE_1 = "ERR_STORAGE_LEGACY_CONTAINS_ACTIVE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_STORAGE_MISSING_CONFIG_2 = "ERR_STORAGE_MISSING_CONFIG_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_STORAGE_MISSING_TYPE_1 = "ERR_STORAGE_MISSING_TYPE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_STORAGE_PATH_NOT_REGULAR_FILE_1 = "ERR_STORAGE_PATH_NOT_REGULAR_FILE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_STORAGE_POLICY_CREATE_1 = "ERR_STORAGE_POLICY_CREATE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_STORAGE_POLICY_INIT_1 = "ERR_STORAGE_POLICY_INIT_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_STORAGE_POLICY_INVALID_2 = "ERR_STORAGE_POLICY_INVALID_2";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_STORAGE_REPOSITORY_MISSING_1 = "ERR_STORAGE_REPOSITORY_MISSING_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_STORAGE_REPOSITORY_NOT_DIRECTORY_1 = "ERR_STORAGE_REPOSITORY_NOT_DIRECTORY_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_STORAGE_REPOSITORY_READ_WRITE_1 = "ERR_STORAGE_REPOSITORY_READ_WRITE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_STORAGE_S3_READ_WRITE_1 = "ERR_STORAGE_S3_READ_WRITE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_STORAGE_UNCONFIGURED_1 = "ERR_STORAGE_UNCONFIGURED_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_STORAGE_UNSUPPORTED_TYPE_2 = "ERR_STORAGE_UNSUPPORTED_TYPE_2";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_STORAGE_BACKEND_CHECKING_3 = "INIT_STORAGE_BACKEND_CHECKING_3";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_STORAGE_BACKEND_CONFIGURED_3 = "INIT_STORAGE_BACKEND_CONFIGURED_3";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_STORAGE_BACKEND_FAILED_4 = "INIT_STORAGE_BACKEND_FAILED_4";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_STORAGE_BACKEND_OK_3 = "INIT_STORAGE_BACKEND_OK_3";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_STORAGE_DELETE_BACKEND_MISSING_1 = "LOG_STORAGE_DELETE_BACKEND_MISSING_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_STORAGE_DELETE_FAILED_2 = "LOG_STORAGE_DELETE_FAILED_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_STORAGE_CLOSE_FAILED_1 = "LOG_STORAGE_CLOSE_FAILED_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_STORAGE_REFERENCES_FAILED_1 = "LOG_STORAGE_REFERENCES_FAILED_1";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.db.storage.messages";

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

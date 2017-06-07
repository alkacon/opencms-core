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

package org.opencms.repository;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p>
 *
 * @since 6.5.6
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String ERR_DEST_EXISTS_0 = "ERR_DEST_EXISTS_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_INVALID_FILTER_TYPE_1 = "ERR_INVALID_FILTER_TYPE_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_INVALID_WRAPPER_NAME_1 = "ERR_INVALID_WRAPPER_NAME_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_ITEM_FILTERED_1 = "ERR_ITEM_FILTERED_1";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_NO_CONFIG_AFTER_STARTUP_0 = "ERR_NO_CONFIG_AFTER_STARTUP_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_OVERWRITE_0 = "ERR_OVERWRITE_0";

    /** Message constant for key in the resource bundle. */
    public static final String ERR_UNLOCK_FAILED_0 = "ERR_UNLOCK_FAILED_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_ADD_FILTER_RULE_2 = "INIT_ADD_FILTER_RULE_2";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_ADD_REPOSITORY_2 = "INIT_ADD_REPOSITORY_2";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_ADD_WRAPPER_1 = "INIT_ADD_WRAPPER_1";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_REPOSITORY_CONFIG_FINISHED_0 = "INIT_REPOSITORY_CONFIG_FINISHED_0";

    /** Message constant for key in the resource bundle. */
    public static final String INIT_STARTING_REPOSITORY_CONFIG_0 = "INIT_STARTING_REPOSITORY_CONFIG_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_COPY_ITEM_2 = "LOG_COPY_ITEM_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_CREATE_ITEM_1 = "LOG_CREATE_ITEM_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DELETE_DEST_0 = "LOG_DELETE_DEST_0";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_DELETE_ITEM_1 = "LOG_DELETE_ITEM_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_LIST_ITEMS_1 = "LOG_LIST_ITEMS_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_LIST_ITEMS_SUCESS_1 = "LOG_LIST_ITEMS_SUCESS_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_LOCK_ITEM_1 = "LOG_LOCK_ITEM_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_MOVE_ITEM_2 = "LOG_MOVE_ITEM_2";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_READ_ITEM_1 = "LOG_READ_ITEM_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_UNLOCK_ITEM_1 = "LOG_UNLOCK_ITEM_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_UPDATE_ITEM_1 = "LOG_UPDATE_ITEM_1";

    /** Message constant for key in the resource bundle. */
    public static final String LOG_WRAPPER_CLASS_NOT_FOUND_1 = "LOG_WRAPPER_CLASS_NOT_FOUND_1";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.repository.messages";

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

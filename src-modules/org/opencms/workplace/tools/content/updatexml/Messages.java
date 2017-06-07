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

package org.opencms.workplace.tools.content.updatexml;

import org.opencms.i18n.A_CmsMessageBundle;
import org.opencms.i18n.I_CmsMessageBundle;

/**
 * Convenience class to access the localized messages of this OpenCms package.
 * <p>
 *
 * @since 7.0.5
 */
public final class Messages extends A_CmsMessageBundle {

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPDATEXML_DIALOG_BLOCK_SETTINGS_0 = "GUI_UPDATEXML_DIALOG_BLOCK_SETTINGS_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPDATEXML_THREAD_NAME_0 = "GUI_UPDATEXML_THREAD_NAME_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATETXML_INITIALIZE_CMS_ERROR_0 = "RPT_UPDATETXML_INITIALIZE_CMS_ERROR_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATETXML_UPDATE_ERROR_0 = "RPT_UPDATETXML_UPDATE_ERROR_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATEXML_BEGIN_UPDATE_0 = "RPT_UPDATEXML_BEGIN_UPDATE_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATEXML_BEGIN_UPDATE_THREAD_0 = "RPT_UPDATEXML_BEGIN_UPDATE_THREAD_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATEXML_END_UPDATE_0 = "RPT_UPDATEXML_END_UPDATE_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATEXML_END_UPDATE_THREAD_0 = "RPT_UPDATEXML_END_UPDATE_THREAD_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATEXML_FILES_TO_UPDATE_1 = "RPT_UPDATEXML_FILES_TO_UPDATE_1";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATEXML_LOCKED_FILE_0 = "RPT_UPDATEXML_LOCKED_FILE_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATEXML_LOCKED_FILES_1 = "RPT_UPDATEXML_LOCKED_FILES_1";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATEXML_NO_VFS_FOLDER_0 = "RPT_UPDATEXML_NO_VFS_FOLDER_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATEXML_PARAMETERS_0 = "RPT_UPDATEXML_PARAMETERS_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATEXML_PARAMETERS_INC_SUBFOLDERS_1 = "RPT_UPDATEXML_PARAMETERS_INC_SUBFOLDERS_1";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATEXML_PARAMETERS_RESOURCE_PATH_1 = "RPT_UPDATEXML_PARAMETERS_RESOURCE_PATH_1";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATEXML_PUBLISHING_FILES_0 = "RPT_UPDATEXML_PUBLISHING_FILES_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATEXML_RESULT_0 = "RPT_UPDATEXML_RESULT_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATEXML_SEARCH_ERROR_0 = "RPT_UPDATEXML_SEARCH_ERROR_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATEXML_START_SEARCHING_0 = "RPT_UPDATEXML_START_SEARCHING_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATEXML_UNLOCK_FILE_0 = "RPT_UPDATEXML_UNLOCK_FILE_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATEXML_UPDATE_FAILED_0 = "RPT_UPDATEXML_UPDATE_FAILED_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATEXML_UPDATE_NUMBER_ERRORS_1 = "RPT_UPDATEXML_UPDATE_NUMBER_ERRORS_1";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATEXML_UPDATE_SUCCESS_0 = "RPT_UPDATEXML_UPDATE_SUCCESS_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATEXML_WRITE_ERROR_0 = "RPT_UPDATEXML_WRITE_ERROR_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATEXML_NO_FILES_FOUND_0 = "RPT_UPDATEXML_NO_FILES_FOUND_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATEXML_CURRENT_FILE_0 = "RPT_UPDATEXML_CURRENT_FILE_0";

    /** Message constant for key in the resource bundle. */
    public static final String RPT_UPDATEXML_START_UPDATING_0 = "RPT_UPDATEXML_START_UPDATING_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.workplace.tools.content.updatexml.messages";

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
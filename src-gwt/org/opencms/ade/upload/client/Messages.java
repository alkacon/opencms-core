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

package org.opencms.ade.upload.client;

import org.opencms.gwt.client.util.CmsMessages;

/**
 * Convenience class to access the localized messages of this OpenCms package.<p>
 *
 * @since 8.0.0
 */
public final class Messages {

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_BUTTON_ADD_FILES_0 = "GUI_UPLOAD_BUTTON_ADD_FILES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_BUTTON_NO_TARGET_0 = "GUI_UPLOAD_BUTTON_NO_TARGET_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_BUTTON_OK_DISABLE_CHECKING_0 = "GUI_UPLOAD_BUTTON_OK_DISABLE_CHECKING_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_BUTTON_OK_DISABLE_UPLOADING_0 = "GUI_UPLOAD_BUTTON_OK_DISABLE_UPLOADING_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_CLIENT_LOADING_0 = "GUI_UPLOAD_CLIENT_LOADING_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_DIALOG_TITLE_1 = "GUI_UPLOAD_DIALOG_TITLE_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UNZIP_BUTTON_TEXT_0 = "GUI_UNZIP_BUTTON_TEXT_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_DRAG_AND_DROP_DISABLED_0 = "GUI_UPLOAD_DRAG_AND_DROP_DISABLED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_DRAG_AND_DROP_ENABLED_0 = "GUI_UPLOAD_DRAG_AND_DROP_ENABLED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_FILE_EXISTING_DELETED_1 = "GUI_UPLOAD_FILE_EXISTING_DELETED_1";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_FILE_INVALID_NAME_2 = "GUI_UPLOAD_FILE_INVALID_NAME_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_FILE_MAX_SIZE_REACHED_2 = "GUI_UPLOAD_FILE_MAX_SIZE_REACHED_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_FILE_NOT_SELECTED_0 = "GUI_UPLOAD_FILE_NOT_SELECTED_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_FILE_TOO_LARGE_2 = "GUI_UPLOAD_FILE_TOO_LARGE_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_FOLDER_0 = "GUI_UPLOAD_FOLDER_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_HOOK_DIALOG_TITLE_0 = "GUI_UPLOAD_HOOK_DIALOG_TITLE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_INFO_INVALID_0 = "GUI_UPLOAD_INFO_INVALID_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_INFO_OVERWRITE_0 = "GUI_UPLOAD_INFO_OVERWRITE_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_INFO_SELECTION_0 = "GUI_UPLOAD_INFO_SELECTION_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_MAX_SIZE_REACHED_2 = "GUI_UPLOAD_MAX_SIZE_REACHED_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_NOTIFICATION_NO_FILES_0 = "GUI_UPLOAD_NOTIFICATION_NO_FILES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_NOTIFICATION_RUNNING_0 = "GUI_UPLOAD_NOTIFICATION_RUNNING_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_SUMMARY_FILES_0 = "GUI_UPLOAD_SUMMARY_FILES_0";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_SUMMARY_FILES_VALUE_2 = "GUI_UPLOAD_SUMMARY_FILES_VALUE_2";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_SUMMARY_FILES_VALUE_3 = "GUI_UPLOAD_SUMMARY_FILES_VALUE_3";

    /** Message constant for key in the resource bundle. */
    public static final String GUI_UPLOAD_UNZIP_FILE_0 = "GUI_UPLOAD_UNZIP_FILE_0";

    /** Name of the used resource bundle. */
    private static final String BUNDLE_NAME = "org.opencms.ade.upload.clientmessages";

    /** Static instance member. */
    private static CmsMessages INSTANCE;

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
    public static CmsMessages get() {

        if (INSTANCE == null) {
            INSTANCE = new CmsMessages(BUNDLE_NAME);
        }
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

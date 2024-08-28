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

package org.opencms.gwt.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * An interface that holds some constants for the upload dialog.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsUploadConstants extends IsSerializable {

    /** The request attribute name for the close link. */
    String ATTR_CLOSE_LINK = "closeLink";

    /** The request attribute name for the current folder. */
    String ATTR_CURRENT_FOLDER = "currentFolder";

    /** The request attribute name for the dialog mode. */
    String ATTR_DIALOG_MODE = "dialogMode";

    /** The field name for the virus scanner results. */
    String ATTR_VIRUS_WARNINGS = "upload_virus_warnings";

    /** The explorer URI for the back link. */
    String EXPLORER_URI = "/system/workplace/views/explorer/explorer_files.jsp?mode=explorerview";

    /** If this field is 'true', don'T perform file name translation (used for replace dialog). */
    String KEEP_FILE_NAMES = "keepFileNames";

    /** Key for the JSON object. */
    String KEY_MESSAGE = "message";

    /** Key for the JSON object. */
    String KEY_REQUEST_SIZE = "requestsize";

    /** Key for the JSON object. */
    String KEY_STACKTRACE = "stacktrace";

    /** Key for the JSON object. */
    String KEY_SUCCESS = "success";

    /** Key for the upload hook. */
    String KEY_UPLOAD_HOOK = "uploadHook";

    /** Key for the uploaded file names. */
    String KEY_UPLOADED_FILE_NAMES = "uploadedFileNames";

    /** Key for the uploaded file ids. */
    String KEY_UPLOADED_FILES = "uploadedFiles";

    /** The resources parameter name. */
    String PARAM_RESOURCES = "resources";

    /** The post-create handler parameter. */
    String POST_CREATE_HANDLER = "post_create_handler";

    /** Upload action JSP URI. */
    String UPLOAD_ACTION_JSP_URI = "/system/workplace/commons/uploadAction.jsp";

    /** The encoded file name field name suffix. */
    String UPLOAD_FILENAME_ENCODED_SUFFIX = "_filename_encoded";

    /** Name of the form field which stores whether the target folder is given as a root path. */
    String UPLOAD_IS_ROOT_PATH_FIELD_NAME = "isRootPath";

    /** Upload JSP URI. */
    String UPLOAD_JSP_URI = "/system/workplace/commons/upload.jsp";

    /** The encoded file name field name suffix. */
    String UPLOAD_ORIGINAL_FILENAME_ENCODED_SUFFIX = "_original_filename_encoded";

    /** The name of the form field that stores the target folder for the upload. */
    String UPLOAD_TARGET_FOLDER_FIELD_NAME = "upload_target_folder";

    /** The name of the form field that stores the file names to unzip. */
    String UPLOAD_UNZIP_FILES_FIELD_NAME = "upload_unzip_files";

    /** The javascript variable name for the upload target folder. */
    String VAR_TARGET_FOLDER = "targetFolder";
}

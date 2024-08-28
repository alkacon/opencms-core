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

package org.opencms.gwt.client.ui.input.upload.impl;

import org.opencms.gwt.client.ui.input.upload.CmsFileInfo;
import org.opencms.gwt.client.ui.input.upload.I_CmsUploadDialog;
import org.opencms.gwt.client.ui.input.upload.I_CmsUploader;
import org.opencms.gwt.client.util.CmsClientStringUtil;

import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Form data implementation of the file uploader.<p>
 */
public class CmsUploaderFormData implements I_CmsUploader {

    /**
     * @see org.opencms.gwt.client.ui.input.upload.I_CmsUploader#uploadFiles(java.lang.String, java.lang.String, boolean, java.lang.String, java.util.List, java.util.List, boolean, org.opencms.gwt.client.ui.input.upload.I_CmsUploadDialog)
     */
    public void uploadFiles(
        String uploadUri,
        String targetFolder,
        boolean isRootPath,
        String postCreateHandler,
        List<CmsFileInfo> filesToUpload,
        List<String> filesToUnzip,
        boolean keepFileNames,
        I_CmsUploadDialog dialog) {

        JsArray<CmsFileInfo> filesToUploadArray = JavaScriptObject.createArray().cast();
        for (CmsFileInfo fileInfo : filesToUpload) {
            filesToUploadArray.push(fileInfo);
        }

        // create a array that contains the names of the files that should be unziped
        JavaScriptObject filesToUnzipArray = JavaScriptObject.createArray();
        for (String filename : filesToUnzip) {
            CmsClientStringUtil.pushArray(filesToUnzipArray, filename);
        }
        upload(
            uploadUri,
            targetFolder,
            isRootPath,
            postCreateHandler,
            filesToUploadArray,
            filesToUnzipArray,
            keepFileNames,
            dialog);
    }

    /**
     * Sends a post request to the upload JSP.<p>
     *
     * @param uploadUri the URI of the JSP that performs the upload
     * @param targetFolder the target folder to upload
     * @param isRootPath true if the target folder is given as a root path
     * @param postCreateHandler the post-create handler
     * @param filesToUpload the files to upload
     * @param filesToUnzip the file names to unzip
     * @param dialog this dialog
     */
    protected native void upload(
        String uploadUri,
        String targetFolder,
        boolean isRootPath,
        String postCreateHandler,
        JsArray<CmsFileInfo> filesToUpload,
        JavaScriptObject filesToUnzip,
        boolean keepFileNames,
        I_CmsUploadDialog dialog) /*-{

        var data = new FormData();
        if (keepFileNames) {
            data.append(@org.opencms.gwt.shared.I_CmsUploadConstants::KEEP_FILE_NAMES, "true");
        }
        for (i = 0; i < filesToUpload.length; i++) {
            var file = filesToUpload[i];
            var fieldName = "file_" + i;
            data.append(fieldName, file);
            // get the current file name/override-name
            var fileName = file.overrideFileName ? file.overrideFileName
                    : file.name ? file.name : file.fileName;
            data
                    .append(
                            fieldName
                                    + @org.opencms.gwt.shared.I_CmsUploadConstants::UPLOAD_FILENAME_ENCODED_SUFFIX,
                            encodeURI(fileName));


           data.append(fieldName + @org.opencms.gwt.shared.I_CmsUploadConstants::UPLOAD_ORIGINAL_FILENAME_ENCODED_SUFFIX, file.name || file.fileName);

        }
        data
                .append(
                        @org.opencms.gwt.shared.I_CmsUploadConstants::UPLOAD_TARGET_FOLDER_FIELD_NAME,
                        targetFolder);

        if (postCreateHandler) {
            data
                    .append(
                            @org.opencms.gwt.shared.I_CmsUploadConstants::POST_CREATE_HANDLER,
                            postCreateHandler);
        };
        data
                .append(
                        @org.opencms.gwt.shared.I_CmsUploadConstants::UPLOAD_IS_ROOT_PATH_FIELD_NAME,
                        "" + isRootPath);

        for (var i = 0; i < filesToUnzip.length; ++i) {
            data
                    .append(
                            @org.opencms.gwt.shared.I_CmsUploadConstants::UPLOAD_UNZIP_FILES_FIELD_NAME,
                            encodeURI(filesToUnzip[i]));
        }

        var xhr = new XMLHttpRequest();
        xhr.open("POST", uploadUri, true);
        xhr.onreadystatechange = function() {
            if (xhr.readyState == 4) {
                if (xhr.status == 200) {
                    dialog.@org.opencms.gwt.client.ui.input.upload.I_CmsUploadDialog::parseResponse(Ljava/lang/String;)(xhr.responseText);
                } else {
                    dialog.@org.opencms.gwt.client.ui.input.upload.I_CmsUploadDialog::showErrorReport(Ljava/lang/String;Ljava/lang/String;)(xhr.statusText, null);
                }
            }
        }
        xhr.send(data);

    }-*/;
}

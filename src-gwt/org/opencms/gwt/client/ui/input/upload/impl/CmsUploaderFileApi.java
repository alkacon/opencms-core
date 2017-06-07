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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * File API implementation of the file uploader.<p>
 */
public class CmsUploaderFileApi extends CmsUploaderFormData {

    /**
     * @see org.opencms.gwt.client.ui.input.upload.impl.CmsUploaderFormData#upload(java.lang.String, java.lang.String, boolean, com.google.gwt.core.client.JsArray, com.google.gwt.core.client.JavaScriptObject, org.opencms.gwt.client.ui.input.upload.I_CmsUploadDialog)
     */
    @Override
    protected native void upload(
        String uploadUri,
        String targetFolder,
        boolean isRootPath,
        JsArray<CmsFileInfo> filesToUpload,
        JavaScriptObject filesToUnzip,
        I_CmsUploadDialog dialog) /*-{
                                  var self = this;

                                  function addPlainField(requestBody, fieldName, fieldValue) {
                                  requestBody += "Content-Disposition: form-data; name=" + fieldName
                                  + "\r\n";
                                  requestBody += "Content-Type: text/plain\r\n\r\n";
                                  requestBody += fieldValue + "\r\n";
                                  requestBody += "--" + boundary + "--";
                                  }

                                  // is executed when there was an error during reading the file
                                  function errorHandler(evt) {
                                  self.@org.opencms.gwt.client.ui.input.upload.impl.CmsUploaderFileApi::onBrowserError(Lorg/opencms/gwt/client/ui/input/upload/I_CmsUploadDialog;Ljava/lang/String;)(dialog,evt.target.error.code);
                                  }

                                  // is executed when the current file is read completely
                                  function loaded(evt) {
                                  // get the current file name/override-name and obtain the read file data
                                  var fileName = file.overrideFileName ? file.overrideFileName
                                  : file.name ? file.name : file.fileName;
                                  var fileData = evt.target.result;
                                  if (fileData == null) {
                                  fileData = "";
                                  }
                                  var fileInputName = "file_" + curIndex;
                                  addPlainField(
                                  body,
                                  fileInputName
                                  + @org.opencms.gwt.shared.I_CmsUploadConstants::UPLOAD_FILENAME_ENCODED_SUFFIX,
                                  encodeURI(fileName));
                                  body += "Content-Disposition: form-data; name=\"" + fileInputName
                                  + "\"; filename=\"" + encodeURI(fileName) + "\"\r\n";
                                  body += "Content-Type: application/octet-stream\r\n\r\n";
                                  body += fileData + "\r\n";
                                  body += "--" + boundary + "\r\n";
                                  // are there any more files?, continue reading the next file
                                  if (filesToUpload.length > ++curIndex) {
                                  file = filesToUpload[curIndex];
                                  this.readAsBinaryString(file);
                                  } else {
                                  // there are no more files left append the infos to the request body
                                  appendInfos();
                                  // create the request and post it
                                  var xhr = new XMLHttpRequest();
                                  xhr.open("POST", uri, true);
                                  // simulate a file MIME POST request.
                                  xhr.setRequestHeader("Content-Type",
                                  "multipart/form-data; boundary=" + boundary);
                                  xhr.overrideMimeType('text/plain; charset=x-user-defined');
                                  xhr.onreadystatechange = function() {
                                  if (xhr.readyState == 4) {
                                  if (xhr.status == 200) {
                                  dialog.@org.opencms.gwt.client.ui.input.upload.I_CmsUploadDialog::parseResponse(Ljava/lang/String;)(xhr.responseText);
                                  } else if (xhr.status != 200) {
                                  dialog.@org.opencms.gwt.client.ui.input.upload.I_CmsUploadDialog::showErrorReport(Ljava/lang/String;Ljava/lang/String;)(xhr.statusText, null);
                                  }
                                  }
                                  }
                                  xhr.sendAsBinary(body);
                                  }
                                  }

                                  // appends the infos to the request body
                                  // should be called at end of creating the body because the boundary is closed here
                                  function appendInfos() {
                                  for ( var i = 0; i < filesToUnzip.length; ++i) {
                                  var filename = filesToUnzip[i];
                                  addPlainField(
                                  body,
                                  @org.opencms.gwt.shared.I_CmsUploadConstants::UPLOAD_UNZIP_FILES_FIELD_NAME,
                                  encodeURI(filename));
                                  }

                                  addPlainField(
                                  body,
                                  @org.opencms.gwt.shared.I_CmsUploadConstants::UPLOAD_TARGET_FOLDER_FIELD_NAME,
                                  targetFolder);

                                  addPlainField(
                                  body,
                                  @org.opencms.gwt.shared.I_CmsUploadConstants::UPLOAD_IS_ROOT_PATH_FIELD_NAME,
                                  "" + isRootPath);
                                  }

                                  // the uri to call
                                  var uri = uploadUri;
                                  // the boundary
                                  var boundary = "26924190726270";
                                  // the request body with the starting boundary
                                  var body = "--" + boundary + "\r\n";

                                  // the main procedure
                                  if (filesToUpload) {

                                  var curIndex = 0;
                                  var file = filesToUpload[curIndex];

                                  var reader = new FileReader();
                                  reader.onloadend = loaded;
                                  reader.onerror = errorHandler;
                                  // Read file into memory
                                  reader.readAsBinaryString(file);
                                  }
                                  }-*/;

    /**
     * Switches the error message depending on the given error code.<p>
     *
     * The error codes are defined in the W3C file API.<p>
     *
     * <a href="http://www.w3.org/TR/FileAPI/#dfn-fileerror">http://www.w3.org/TR/FileAPI/#dfn-fileerror</a>
     *
     * @param dialog the upload dialog
     * @param errorCode the error code as String
     */
    private void onBrowserError(I_CmsUploadDialog dialog, String errorCode) {

        int code = new Integer(errorCode).intValue();
        String errMsg = org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.ERR_UPLOAD_BROWSER_0);

        switch (code) {
            case 1: // NOT_FOUND_ERR
                errMsg = org.opencms.gwt.client.Messages.get().key(
                    org.opencms.gwt.client.Messages.ERR_UPLOAD_BROWSER_NOT_FOUND_0);
                break;
            case 2: // SECURITY_ERR
                errMsg = org.opencms.gwt.client.Messages.get().key(
                    org.opencms.gwt.client.Messages.ERR_UPLOAD_BROWSER_SECURITY_0);
                break;
            case 3: // ABORT_ERR
                errMsg = org.opencms.gwt.client.Messages.get().key(
                    org.opencms.gwt.client.Messages.ERR_UPLOAD_BROWSER_ABORT_ERR_0);
                break;
            case 4: // NOT_READABLE_ERR
                errMsg = org.opencms.gwt.client.Messages.get().key(
                    org.opencms.gwt.client.Messages.ERR_UPLOAD_BROWSER_NOT_READABLE_0);
                break;
            case 5: // ENCODING_ERR
                errMsg = org.opencms.gwt.client.Messages.get().key(
                    org.opencms.gwt.client.Messages.ERR_UPLOAD_BROWSER_ENCODING_0);
                break;
            default:
                break;
        }
        dialog.showErrorReport(errMsg, null);
    }
}

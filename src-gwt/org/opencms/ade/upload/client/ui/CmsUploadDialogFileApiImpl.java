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

package org.opencms.ade.upload.client.ui;

import org.opencms.ade.upload.client.Messages;
import org.opencms.ade.upload.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.upload.CmsFileInfo;
import org.opencms.gwt.client.util.CmsClientStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Provides the upload dialog for file API support.<p>
 * 
 * @since 8.0.0
 */
public class CmsUploadDialogFileApiImpl extends CmsUploadDialogFormDataImpl {

    /** The maximum upload size in bytes. (50 MB) */
    private static final long MAX_UPLOAD_SIZE = 51200000;

    /**
     * @see org.opencms.ade.upload.client.ui.CmsUploadDialogFormDataImpl#getFileSizeTooLargeMessage(org.opencms.gwt.client.ui.input.upload.CmsFileInfo)
     */
    @Override
    public String getFileSizeTooLargeMessage(CmsFileInfo file) {

        if ((file.getFileSize() > MAX_UPLOAD_SIZE)) {
            return Messages.get().key(
                Messages.GUI_UPLOAD_FILE_MAX_SIZE_REACHED_2,
                formatBytes(file.getFileSize()),
                formatBytes(new Long(MAX_UPLOAD_SIZE).intValue()));
        }
        return super.getFileSizeTooLargeMessage(file);
    }

    /**
     * @see org.opencms.ade.upload.client.ui.A_CmsUploadDialog#isTooLarge(org.opencms.gwt.client.ui.input.upload.CmsFileInfo)
     */
    @Override
    public boolean isTooLarge(CmsFileInfo cmsFileInfo) {

        if (super.isTooLarge(cmsFileInfo) || (cmsFileInfo.getFileSize() > MAX_UPLOAD_SIZE)) {
            return true;
        }
        return false;
    }

    /**
     * @see org.opencms.ade.upload.client.ui.A_CmsUploadDialog#submit()
     */
    @Override
    public void submit() {

        // create a JsArray containing the files to upload
        List<String> orderedFilenamesToUpload = new ArrayList<String>(getFilesToUpload().keySet());
        Collections.sort(orderedFilenamesToUpload, String.CASE_INSENSITIVE_ORDER);
        JsArray<CmsFileInfo> filesToUpload = JavaScriptObject.createArray().cast();
        for (String filename : orderedFilenamesToUpload) {
            filesToUpload.push(getFilesToUpload().get(filename));
        }

        // create a array that contains the names of the files that should be unziped
        JavaScriptObject filesToUnzip = JavaScriptObject.createArray();
        for (String filename : getFilesToUnzip(false)) {
            CmsClientStringUtil.pushArray(filesToUnzip, filename);
        }
        upload(getUploadUri(), getTargetFolder(), filesToUpload, filesToUnzip, this);
    }

    /**
     * @see org.opencms.ade.upload.client.ui.A_CmsUploadDialog#updateSummary()
     */
    @Override
    public void updateSummary() {

        super.updateSummary();
        if (!getFilesToUpload().isEmpty() && (getContentLength() > MAX_UPLOAD_SIZE)) {
            String message = Messages.get().key(
                Messages.GUI_UPLOAD_MAX_SIZE_REACHED_2,
                formatBytes(new Long(getContentLength()).intValue()),
                formatBytes(new Long(MAX_UPLOAD_SIZE).intValue()));
            disableOKButton(message);
            StringBuffer buffer = new StringBuffer(64);
            buffer.append("<p class=\"");
            buffer.append(I_CmsLayoutBundle.INSTANCE.uploadCss().dialogMessageImportant());
            buffer.append("\">");
            buffer.append(message);
            buffer.append("</p>");
            setSummaryHTML(buffer.toString());
        } else if (!getFilesToUpload().isEmpty()) {
            enableOKButton();
        }
    }

    /**
     * Switches the error message depending on the given error code.<p>
     * 
     * The error codes are defined in the W3C file API.<p>
     * 
     * <a href="http://www.w3.org/TR/FileAPI/#dfn-fileerror">http://www.w3.org/TR/FileAPI/#dfn-fileerror</a>
     * 
     * @param errorCode the error code as String
     */
    private void onBrowserError(String errorCode) {

        int code = new Integer(errorCode).intValue();
        String errMsg = Messages.get().key(Messages.ERR_UPLOAD_BROWSER_0);

        switch (code) {
            case 1: // NOT_FOUND_ERR
                errMsg = Messages.get().key(Messages.ERR_UPLOAD_BROWSER_NOT_FOUND_0);
                break;
            case 2: // SECURITY_ERR
                errMsg = Messages.get().key(Messages.ERR_UPLOAD_BROWSER_SECURITY_0);
                break;
            case 3: // ABORT_ERR
                errMsg = Messages.get().key(Messages.ERR_UPLOAD_BROWSER_ABORT_ERR_0);
                break;
            case 4: // NOT_READABLE_ERR
                errMsg = Messages.get().key(Messages.ERR_UPLOAD_BROWSER_NOT_READABLE_0);
                break;
            case 5: // ENCODING_ERR
                errMsg = Messages.get().key(Messages.ERR_UPLOAD_BROWSER_ENCODING_0);
                break;
            default:
                break;
        }
        showErrorReport(errMsg, null);
    }

    /**
     * Sends a post request to the upload JSP.<p>
     * 
     * @param uploadUri the URI of the JSP that performs the upload
     * @param targetFolder the target folder to upload
     * @param filesToUpload the file names to upload
     * @param filesToUnzip the file names that should be unziped
     * @param dialog this dialog
     */
    private native void upload(
        String uploadUri,
        String targetFolder,
        JsArray<CmsFileInfo> filesToUpload,
        JavaScriptObject filesToUnzip,
        CmsUploadDialogFileApiImpl dialog) /*-{

        function addPlainField(requestBody, fieldName, fieldValue) {
            requestBody += "Content-Disposition: form-data; name=" + fieldName
                    + "\r\n";
            requestBody += "Content-Type: text/plain\r\n\r\n";
            requestBody += fieldValue + "\r\n";
            requestBody += "--" + boundary + "--";
        }

        // is executed when there was an error during reading the file
        function errorHandler(evt) {
            dialog.@org.opencms.ade.upload.client.ui.CmsUploadDialogFileApiImpl::onBrowserError(Ljava/lang/String;)(evt.target.error.code);
        }

        // is executed when the current file is read completely
        function loaded(evt) {
            // get the current file name and obtain the read file data
            var fileName = file.name;
            var fileData = evt.target.result;
            if (fileData == null) {
                fileData = "";
            }
            var fileInputName = "file_" + curIndex;
            addPlainField(
                    body,
                    fileInputName
                            + @org.opencms.ade.upload.shared.I_CmsUploadConstants::UPLOAD_FILENAME_ENCODED_SUFFIX,
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
                            dialog.@org.opencms.ade.upload.client.ui.CmsUploadDialogFileApiImpl::parseResponse(Ljava/lang/String;)(xhr.responseText);
                        } else if (xhr.status != 200) {
                            dialog.@org.opencms.ade.upload.client.ui.CmsUploadDialogFileApiImpl::showErrorReport(Ljava/lang/String;Ljava/lang/String;)(xhr.statusText, null);
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
                        @org.opencms.ade.upload.shared.I_CmsUploadConstants::UPLOAD_UNZIP_FILES_FIELD_NAME,
                        encodeURI(filename));
            }

            addPlainField(
                    body,
                    @org.opencms.ade.upload.shared.I_CmsUploadConstants::UPLOAD_TARGET_FOLDER_FIELD_NAME,
                    targetFolder);
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
}

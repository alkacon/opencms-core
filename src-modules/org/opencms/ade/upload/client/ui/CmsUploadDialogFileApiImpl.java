/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/upload/client/ui/Attic/CmsUploadDialogFileApiImpl.java,v $
 * Date   : $Date: 2011/03/02 14:24:06 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.ade.upload.shared.I_CmsUploadConstants;
import org.opencms.gwt.client.ui.input.upload.CmsFileInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Provides the upload dialog for file API support.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsUploadDialogFileApiImpl extends CmsUploadDialogFormDataImpl {

    /** The maximum upload size in bytes. (50 MB) */
    private static final long MAX_UPLOAD_SIZE = 51200000;

    /**
     * @see org.opencms.ade.upload.client.ui.A_CmsUploadDialog#submit()
     */
    @Override
    public void submit() {

        List<String> orderedFilenamesToUpload = new ArrayList<String>(getFilesToUpload().keySet());
        Collections.sort(orderedFilenamesToUpload, String.CASE_INSENSITIVE_ORDER);

        // create a JsArray containing the files to upload
        JsArray<CmsFileInfo> filesToUpload = JavaScriptObject.createArray().cast();
        for (String filename : orderedFilenamesToUpload) {
            filesToUpload.push(getFilesToUpload().get(filename));
        }
        upload(
            getUploadUri(),
            I_CmsUploadConstants.UPLOAD_FILE_NAME_URL_ENCODED_FLAG,
            I_CmsUploadConstants.UPLOAD_TARGET_FOLDER_FIELD_NAME,
            getTargetFolder(),
            filesToUpload,
            this);
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
     * @param encodedFieldName the field name that stores the encoded flag
     * @param targetFolder the target folder to upload
     * @param dialog this dialog
     * @param filesToUpload the file names to upload
     */
    private native void upload(
        String uploadUri,
        String encodedFieldName,
        String targetFolderFieldName,
        String targetFolder,
        JsArray<CmsFileInfo> filesToUpload,
        CmsUploadDialogFileApiImpl dialog) /*-{

		// is executed when there was an error during reading the file
		function errorHandler(evt) {
			dialog.@org.opencms.ade.upload.client.ui.CmsUploadDialogFileApiImpl::onBrowserError(Ljava/lang/String;)(evt.target.error.code);
		}

		// is executed when the current file is read completely
		function loaded(evt) {
			// get the current file name and obtain the read file data
			var fileName = file.name;
			var fileData = evt.target.result;
			body += "Content-Disposition: form-data; name=\"file_" + curIndex
					+ "\"; filename=\"" + encodeURI(fileName) + "\"\r\n";
			body += "Content-Type: application/octet-stream\r\n\r\n";
			body += fileData + "\r\n";
			body += "--" + boundary + "\r\n";
			// are there any more files?, continue reading the next file
			if (filesToUpload.length > ++curIndex) {
				file = filesToUpload[curIndex];
				this.readAsBinaryString(file);
			} else {
				// there are no more files left
				// append the target folder to the request 
				appendTargetFolder();
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

		// appends the target folder to the request body 
		// should be called at end of creating the body because the boundary is closed here
		function appendTargetFolder() {
			body += "Content-Disposition: form-data; name=" + encodedFieldName
					+ "\r\n";
			body += "Content-Type: text/plain\r\n\r\n";
			body += "true\r\n";
			body += "--" + boundary + "\r\n";

			body += "Content-Disposition: form-data; name="
					+ targetFolderFieldName + "\r\n";
			body += "Content-Type: text/plain\r\n\r\n";
			body += targetFolder + "\r\n";
			body += "--" + boundary + "--";
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
}

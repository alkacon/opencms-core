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
import org.opencms.gwt.shared.I_CmsUploadConstants;

import java.util.List;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * The default uploader implementation. Use if the file API is not available.<p>
 */
public class CmsUploaderDefault implements I_CmsUploader {

    /**
     * Implements the submit handler (Used for browsers that don't support file api).<p>
     */
    private class CmsUploadHandler implements SubmitCompleteHandler {

        /** The upload dialog instance. */
        private I_CmsUploadDialog m_dialog;

        /** The submitted form. */
        private FormPanel m_form;

        /**
         * The default constructor.<p>
         *
         * @param dialog the upload dialog instance
         * @param form the submitted form
         */
        public CmsUploadHandler(I_CmsUploadDialog dialog, FormPanel form) {

            m_dialog = dialog;
            m_form = form;
        }

        /**
         * @see com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler#onSubmitComplete(com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent)
         */
        public void onSubmitComplete(SubmitCompleteEvent event) {

            m_dialog.parseResponse(event.getResults());
            m_form.removeFromParent();
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.upload.I_CmsUploader#uploadFiles(java.lang.String, java.lang.String, boolean, java.util.List, java.util.List, org.opencms.gwt.client.ui.input.upload.I_CmsUploadDialog)
     */
    public void uploadFiles(
        String uploadUri,
        String targetFolder,
        boolean isRootPath,
        List<CmsFileInfo> filesToUpload,
        List<String> filesToUnzip,
        I_CmsUploadDialog dialog) {

        FormPanel form = createForm(uploadUri, targetFolder, isRootPath, filesToUpload, filesToUnzip);
        form.addSubmitCompleteHandler(new CmsUploadHandler(dialog, form));
        form.getElement().getStyle().setDisplay(Display.NONE);
        RootPanel.get().add(form);
        form.submit();
    }

    /**
     * Creates a hidden input field with the given name and value and adds it to the form panel.<p>
     *
     * @param form the form panel
     * @param fieldName the field name
     * @param fieldValue the field value
     */
    private void addHiddenField(Panel form, String fieldName, String fieldValue) {

        Hidden inputField = new Hidden();
        inputField.setName(fieldName);
        inputField.setValue(fieldValue);
        form.add(inputField);
    }

    /**
     * Creates a form to submit the upload files.<p>
     *
     * @param uploadUri the upload URI
     * @param targetFolder the target folder
     * @param isRootPath if the target folder is given as a root path
     * @param filesToUpload the files to upload
     * @param filesToUnzip the files to unzip
     * @return the created form panel
     */
    private FormPanel createForm(
        String uploadUri,
        String targetFolder,
        boolean isRootPath,
        List<CmsFileInfo> filesToUpload,
        List<String> filesToUnzip) {

        // create a form using the POST method and multipart MIME encoding
        FormPanel form = new FormPanel();
        form.setAction(uploadUri);
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setMethod(FormPanel.METHOD_POST);
        // create a panel that contains the file input fields and the target folder
        FlowPanel inputFieldsPanel = new FlowPanel();
        int count = 0;
        for (CmsFileInfo info : filesToUpload) {
            InputElement input = info.getInputElement();
            String fieldName = "file_" + count++;
            input.setName(fieldName);
            inputFieldsPanel.getElement().appendChild(input);
            addHiddenField(
                inputFieldsPanel,
                fieldName + I_CmsUploadConstants.UPLOAD_FILENAME_ENCODED_SUFFIX,
                URL.encode(info.getOverrideFileName()));
        }
        for (String filename : filesToUnzip) {
            addHiddenField(inputFieldsPanel, I_CmsUploadConstants.UPLOAD_UNZIP_FILES_FIELD_NAME, URL.encode(filename));
        }
        addHiddenField(inputFieldsPanel, I_CmsUploadConstants.UPLOAD_TARGET_FOLDER_FIELD_NAME, targetFolder);
        addHiddenField(inputFieldsPanel, I_CmsUploadConstants.UPLOAD_IS_ROOT_PATH_FIELD_NAME, "" + isRootPath);
        form.setWidget(inputFieldsPanel);
        return form;
    }

}

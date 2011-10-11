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
import org.opencms.ade.upload.shared.I_CmsUploadConstants;
import org.opencms.gwt.client.ui.input.upload.CmsFileInfo;
import org.opencms.gwt.client.ui.input.upload.CmsFileInput;
import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Panel;

/**
 * Provides the default upload dialog without multiple file selection.<p>
 * 
 * @since 8.0.0
 */
public class CmsUploadDialogImpl extends A_CmsUploadDialog {

    /**
     * Implements the submit handler (Used for browsers that don't support file api).<p>
     */
    private class CmsUploadHandler implements SubmitCompleteHandler {

        /**
         * The default constructor.<p>
         */
        public CmsUploadHandler() {

            // noop
        }

        /**
         * @see com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler#onSubmitComplete(com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent)
         */
        public void onSubmitComplete(SubmitCompleteEvent event) {

            parseResponse(event.getResults());
        }
    }

    /** The input file input fields. */
    private Map<String, CmsFileInput> m_inputsToUpload = new HashMap<String, CmsFileInput>();

    /**
     * @see org.opencms.ade.upload.client.ui.A_CmsUploadDialog#createInfoBean(org.opencms.gwt.client.ui.input.upload.CmsFileInfo)
     */
    @Override
    public CmsListInfoBean createInfoBean(CmsFileInfo file) {

        return new CmsListInfoBean(file.getFileName(), getResourceType(file), null);
    }

    /**
     * @see org.opencms.ade.upload.client.ui.A_CmsUploadDialog#getFileSizeTooLargeMessage(org.opencms.gwt.client.ui.input.upload.CmsFileInfo)
     */
    @Override
    public String getFileSizeTooLargeMessage(CmsFileInfo file) {

        return "";
    }

    /**
     * @see org.opencms.ade.upload.client.ui.A_CmsUploadDialog#isTooLarge(org.opencms.gwt.client.ui.input.upload.CmsFileInfo)
     */
    @Override
    public boolean isTooLarge(CmsFileInfo cmsFileInfo) {

        return false;
    }

    /**
     * @see org.opencms.ade.upload.client.ui.A_CmsUploadDialog#submit()
     */
    @Override
    public void submit() {

        FormPanel form = createForm();
        form.addSubmitCompleteHandler(new CmsUploadHandler());
        insertUploadForm(form);
        form.submit();
    }

    /**
     * @see org.opencms.ade.upload.client.ui.A_CmsUploadDialog#updateSummary()
     */
    @Override
    public void updateSummary() {

        StringBuffer buffer = new StringBuffer(64);
        buffer.append("<p class=\"").append(I_CmsLayoutBundle.INSTANCE.uploadCss().dialogMessage()).append("\">");
        buffer.append("<b>" + Messages.get().key(Messages.GUI_UPLOAD_SUMMARY_FILES_0) + "</b> ");
        buffer.append(Messages.get().key(
            Messages.GUI_UPLOAD_SUMMARY_FILES_VALUE_2,
            new Integer(getFilesToUpload().size()),
            getFileText()));
        buffer.append("</p>");
        setSummaryHTML(buffer.toString());
    }

    /**
     * Adds the given file input field to this dialog.<p>
     * 
     * @param fileInput the file input field to add
     */
    @Override
    protected void addFileInput(CmsFileInput fileInput) {

        // add the files selected by the user to the list of files to upload
        if (fileInput != null) {
            m_inputsToUpload.put(fileInput.getFiles()[0].getFileName(), fileInput);
        }
        super.addFileInput(fileInput);
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
     * Creates a form that contains the file input fields and the target folder.<p>
     * 
     * @return the form
     */
    private FormPanel createForm() {

        // create a form using the POST method and multipart MIME encoding
        FormPanel form = new FormPanel();
        form.setAction(getUploadUri());
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setMethod(FormPanel.METHOD_POST);
        // create a panel that contains the file input fields and the target folder
        FlowPanel inputFieldsPanel = new FlowPanel();
        int count = 0;
        for (CmsFileInput input : m_inputsToUpload.values()) {
            String filename = input.getFiles()[0].getFileName();
            String fieldName = "file_" + count++;
            input.setName(fieldName);
            if (getFilesToUpload().containsKey(filename)) {
                inputFieldsPanel.add(input);
            }
            addHiddenField(
                inputFieldsPanel,
                fieldName + I_CmsUploadConstants.UPLOAD_FILENAME_ENCODED_SUFFIX,
                URL.encode(filename));
        }
        for (String filename : getFilesToUnzip(false)) {
            addHiddenField(inputFieldsPanel, I_CmsUploadConstants.UPLOAD_UNZIP_FILES_FIELD_NAME, URL.encode(filename));
        }
        addHiddenField(inputFieldsPanel, I_CmsUploadConstants.UPLOAD_TARGET_FOLDER_FIELD_NAME, getTargetFolder());
        form.setWidget(inputFieldsPanel);
        return form;
    }
}

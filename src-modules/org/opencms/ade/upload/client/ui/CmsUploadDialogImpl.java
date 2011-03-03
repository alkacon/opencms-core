/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/upload/client/ui/Attic/CmsUploadDialogImpl.java,v $
 * Date   : $Date: 2011/03/03 18:01:42 $
 * Version: $Revision: 1.2 $
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
import org.opencms.gwt.client.ui.input.upload.CmsFileInput;
import org.opencms.gwt.shared.CmsListInfoBean;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.Hidden;

/**
 * Provides the default upload dialog without multiple file selection.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.2 $
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

    /**
     * @see org.opencms.ade.upload.client.ui.A_CmsUploadDialog#createInfoBean(org.opencms.gwt.client.ui.input.upload.CmsFileInfo)
     */
    @Override
    public CmsListInfoBean createInfoBean(CmsFileInfo file) {

        return new CmsListInfoBean(file.getFileName(), getResourceType(file.getFileName()), null);
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
        for (CmsFileInput input : getInputsToUpload()) {
            String filename = input.getFiles()[0].getFileName();
            input.setName("file_" + count++);
            if (getFilesToUpload().containsKey(filename)) {
                inputFieldsPanel.add(input);
            }
        }

        for (String filename : getFilesToUnzip(false)) {
            final Hidden filesToUnzip = new Hidden();
            filesToUnzip.setName(I_CmsUploadConstants.UPLOAD_UNZIP_FILES_FIELD_NAME);
            filesToUnzip.setValue(filename);
            inputFieldsPanel.add(filesToUnzip);
        }

        final Hidden targetFolder = new Hidden();
        targetFolder.setName(I_CmsUploadConstants.UPLOAD_TARGET_FOLDER_FIELD_NAME);
        targetFolder.setValue(getTargetFolder());
        inputFieldsPanel.add(targetFolder);

        // make the form using the panel as widget
        form.setWidget(inputFieldsPanel);
        return form;
    }
}

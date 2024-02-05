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

package org.opencms.ade.upload.client.ui;

import org.opencms.ade.upload.client.Messages;
import org.opencms.gwt.client.ui.input.upload.CmsFileInfo;
import org.opencms.gwt.client.ui.input.upload.CmsFileInput;
import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides the default upload dialog without multiple file selection.<p>
 *
 * @since 8.0.0
 */
public class CmsUploadDialogImpl extends A_CmsUploadDialog {

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
     * @see org.opencms.ade.upload.client.ui.A_CmsUploadDialog#updateSummary()
     */
    @Override
    public void updateSummary() {

        StringBuffer buffer = new StringBuffer(64);
        buffer.append("<p class=\"").append(
            org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.uploadButton().dialogMessage()).append("\">");
        buffer.append("<b>" + Messages.get().key(Messages.GUI_UPLOAD_SUMMARY_FILES_0) + "</b> ");
        buffer.append(
            Messages.get().key(
                Messages.GUI_UPLOAD_SUMMARY_FILES_VALUE_2,
                Integer.valueOf(getFilesToUpload().size()),
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
}

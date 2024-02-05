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
import org.opencms.gwt.client.ui.input.upload.CmsUploadButton;

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
                CmsUploadButton.formatBytes(file.getFileSize()),
                CmsUploadButton.formatBytes(Long.valueOf(MAX_UPLOAD_SIZE).intValue()));
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
     * @see org.opencms.ade.upload.client.ui.A_CmsUploadDialog#updateSummary()
     */
    @Override
    public void updateSummary() {

        super.updateSummary();
        if (!getFilesToUpload().isEmpty() && (getContentLength() > MAX_UPLOAD_SIZE)) {
            String message = Messages.get().key(
                Messages.GUI_UPLOAD_MAX_SIZE_REACHED_2,
                CmsUploadButton.formatBytes(Long.valueOf(getContentLength()).intValue()),
                CmsUploadButton.formatBytes(Long.valueOf(MAX_UPLOAD_SIZE).intValue()));
            disableOKButton(message);
            StringBuffer buffer = new StringBuffer(64);
            buffer.append("<p class=\"");
            buffer.append(
                org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.uploadButton().dialogMessageImportant());
            buffer.append("\">");
            buffer.append(message);
            buffer.append("</p>");
            setSummaryHTML(buffer.toString());
        } else if (!getFilesToUpload().isEmpty()) {
            enableOKButton();
        }
    }
}

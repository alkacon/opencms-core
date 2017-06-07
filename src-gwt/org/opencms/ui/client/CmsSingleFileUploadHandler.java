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

package org.opencms.ui.client;

import org.opencms.ade.upload.client.I_CmsUploadContext;
import org.opencms.ade.upload.client.Messages;
import org.opencms.gwt.client.ui.input.upload.CmsFileInput;
import org.opencms.gwt.client.ui.input.upload.I_CmsUploadButton;
import org.opencms.gwt.client.ui.input.upload.I_CmsUploadButtonHandler;
import org.opencms.util.CmsStringUtil;

import com.google.common.base.Supplier;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Handles single file uploads. Allows to specify the target file name.<p>
 */
public class CmsSingleFileUploadHandler implements I_CmsUploadButtonHandler {

    /** The dialog close handler. */
    private CloseHandler<PopupPanel> m_closeHandler;

    /** Factory for creating upload contexts. */
    private Supplier<I_CmsUploadContext> m_contextFactory;

    /** The replace dialog. */
    private CmsSingleFileUploadDialog m_dialog;

    /** The target file name. */
    private String m_targetFileName;

    /** The target file name prefix. */
    private String m_targetFileNamePrefix;

    /** The upload folder path. */
    private String m_targetFolderPath;

    /** The upload button. */
    private I_CmsUploadButton m_uploadButton;

    /** The dialog title. */
    private String m_dialogTitle;

    /**
     * Constructor.<p>
     *
     * @param contextFactory the upload context factory
     * @param dialogTitle the dialog title
     */
    public CmsSingleFileUploadHandler(Supplier<I_CmsUploadContext> contextFactory, String dialogTitle) {

        m_contextFactory = contextFactory;
        m_dialogTitle = dialogTitle;
    }

    /**
     * Returns the new file name.<p>
     *
     * @param originalFileName the original file name
     *
     * @return the new file name
     */
    public String getFileName(String originalFileName) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_targetFileName)) {
            return m_targetFileName;
        } else if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_targetFileNamePrefix)) {
            return m_targetFileNamePrefix + originalFileName;
        } else {
            return originalFileName;
        }
    }

    /**
     * Returns the targetFolderPath.<p>
     *
     * @return the targetFolderPath
     */
    public String getTargetFolderPath() {

        return m_targetFolderPath;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.upload.I_CmsUploadButtonHandler#initializeFileInput(org.opencms.gwt.client.ui.input.upload.CmsFileInput)
     */
    public void initializeFileInput(CmsFileInput fileInput) {

        // important to set font-size as inline style, as IE7 and IE8 will not accept it otherwise
        fileInput.getElement().getStyle().setFontSize(200, Unit.PX);
        fileInput.setAllowMultipleFiles(false);
        fileInput.setName("replace");
        fileInput.addStyleName(
            org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.uploadButton().uploadFileInput());
    }

    /**
     * @see org.opencms.gwt.client.ui.input.upload.I_CmsUploadButtonHandler#onChange(org.opencms.gwt.client.ui.input.upload.CmsFileInput)
     */
    public void onChange(CmsFileInput fileInput) {

        if (m_dialog == null) {
            String dialogTitle = CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_dialogTitle)
            ? m_dialogTitle
            : Messages.get().key(Messages.GUI_UPLOAD_DIALOG_TITLE_1, getTargetFolderPath());
            m_dialog = new CmsSingleFileUploadDialog(this, dialogTitle);
            m_dialog.setContext(m_contextFactory.get());
            m_dialog.center();
            if (m_closeHandler != null) {
                m_dialog.addCloseHandler(m_closeHandler);
            }
        } else if (m_uploadButton != null) {
            m_uploadButton.createFileInput();
        }
        if (fileInput.getFiles().length == 1) {
            m_dialog.setFileInput(fileInput);
        }
    }

    /**
     * @see org.opencms.gwt.client.ui.input.upload.I_CmsUploadButtonHandler#setButton(org.opencms.gwt.client.ui.input.upload.I_CmsUploadButton)
     */
    public void setButton(I_CmsUploadButton button) {

        m_uploadButton = button;
    }

    /**
     * Sets the dialog close handler.<p>
     *
     * @param closeHandler the close handler
     */
    public void setCloseHandler(CloseHandler<PopupPanel> closeHandler) {

        m_closeHandler = closeHandler;
        if (m_dialog != null) {
            m_dialog.addCloseHandler(closeHandler);
        }
    }

    /**
     * Sets the target file name.<p>
     *
     * @param fileName the target file name
     */
    public void setTargetFileName(String fileName) {

        m_targetFileName = fileName;
    }

    /**
     * Sets the target file name prefix.<p>
     *
     * @param targetFileNamePrefix the target file name prefix to set
     */
    public void setTargetFileNamePrefix(String targetFileNamePrefix) {

        m_targetFileNamePrefix = targetFileNamePrefix;
    }

    /**
     * Sets the target folder path.<p>
     *
     * @param folderPath the target folder path
     */
    public void setTargetFolderPath(String folderPath) {

        m_targetFolderPath = folderPath;
    }
}
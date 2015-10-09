/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

import org.opencms.ade.upload.client.I_CmsUploadContext;
import org.opencms.gwt.client.ui.CmsErrorDialog;
import org.opencms.gwt.client.ui.input.upload.CmsFileInfo;
import org.opencms.gwt.client.ui.input.upload.CmsFileInput;
import org.opencms.gwt.client.ui.input.upload.I_CmsUploadButton;
import org.opencms.gwt.client.ui.input.upload.I_CmsUploadButtonHandler;

import java.util.List;

import com.google.common.base.Supplier;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Default upload button handler which is used for the upload dialog.<p>
 */
public class CmsDialogUploadButtonHandler implements I_CmsUploadButtonHandler {

    /** The upload button instance. */
    private I_CmsUploadButton m_button;

    /** The close handler for the upload dialog. */
    private CloseHandler<PopupPanel> m_closeHandler;

    /** The handler registration for the close handler of the dialog. */
    private HandlerRegistration m_closeHandlerRegistration;

    /** Factory for creating upload contexts. */
    private Supplier<I_CmsUploadContext> m_contextFactory;

    /** True if the the target folder should be treated as a root path. */
    private boolean m_isTargetRootPath;

    /** The target folder for the upload dialog. */
    private String m_targetFolder;

    /** The upload dialog instance. */
    private A_CmsUploadDialog m_uploadDialog;

    /**
     * Creates a new upload button handler.<p>
     *
     * @param contextFactory the context factory to use for upload contexts
     */
    public CmsDialogUploadButtonHandler(Supplier<I_CmsUploadContext> contextFactory) {

        m_contextFactory = contextFactory;
    }

    /**
     * Creates a new upload button handler.<p>
     *
     * @param contextFactory the context factory to use for upload contexts
     * @param targetFolder the target folder
     * @param isRootPath true fi the target folder is a root path
     */
    public CmsDialogUploadButtonHandler(
        Supplier<I_CmsUploadContext> contextFactory,
        String targetFolder,
        boolean isRootPath) {

        m_contextFactory = contextFactory;
        m_targetFolder = targetFolder;
        m_isTargetRootPath = isRootPath;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.upload.I_CmsUploadButtonHandler#initializeFileInput(org.opencms.gwt.client.ui.input.upload.CmsFileInput)
     */
    public void initializeFileInput(CmsFileInput fileInput) {

        // important to set font-size as inline style, as IE7 and IE8 will not accept it otherwise
        fileInput.getElement().getStyle().setFontSize(200, Unit.PX);
        fileInput.setAllowMultipleFiles(true);
        fileInput.setName("upload");
        fileInput.addStyleName(
            org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.uploadButton().uploadFileInput());
    }

    /**
     * @see org.opencms.gwt.client.ui.input.upload.I_CmsUploadButtonHandler#onChange(org.opencms.gwt.client.ui.input.upload.CmsFileInput)
     */
    public void onChange(CmsFileInput fileInput) {

        if (m_uploadDialog == null) {
            try {
                m_uploadDialog = GWT.create(CmsUploadDialogImpl.class);
                I_CmsUploadContext context = m_contextFactory.get();
                m_uploadDialog.setContext(context);
                updateDialog();
                // the current upload button is located outside the dialog, reinitialize it with a new button handler instance
                m_button.reinitButton(
                    new CmsDialogUploadButtonHandler(m_contextFactory, m_targetFolder, m_isTargetRootPath));
            } catch (Exception e) {
                CmsErrorDialog.handleException(
                    new Exception(
                        "Deserialization of dialog data failed. This may be caused by expired java-script resources, please clear your browser cache and try again.",
                        e));
                return;
            }
        }
        m_uploadDialog.addFileInput(fileInput);
        m_button.createFileInput();
    }

    /**
     * Opens the upload dialog for the given file references.<p>
     *
     * @param files the file references
     */
    public void openDialogWithFiles(List<CmsFileInfo> files) {

        if (m_uploadDialog == null) {
            try {
                m_uploadDialog = GWT.create(CmsUploadDialogImpl.class);
                I_CmsUploadContext context = m_contextFactory.get();
                m_uploadDialog.setContext(context);
                updateDialog();
                // the current upload button is located outside the dialog, reinitialize it with a new button handler instance
                m_button.reinitButton(
                    new CmsDialogUploadButtonHandler(m_contextFactory, m_targetFolder, m_isTargetRootPath));
            } catch (Exception e) {
                CmsErrorDialog.handleException(
                    new Exception(
                        "Deserialization of dialog data failed. This may be caused by expired java-script resources, please clear your browser cache and try again.",
                        e));
                return;
            }
        }
        m_uploadDialog.addFiles(files);
        m_button.createFileInput();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.upload.I_CmsUploadButtonHandler#setButton(org.opencms.gwt.client.ui.input.upload.I_CmsUploadButton)
     */
    public void setButton(I_CmsUploadButton button) {

        m_button = button;
    }

    /**
     * Sets the close handler for the dialog.<p>
     *
     * @param closeHandler the close handler
     */
    public void setCloseHandler(CloseHandler<PopupPanel> closeHandler) {

        m_closeHandler = closeHandler;
        updateDialog();

    }

    /**
     * Chooses whether the target folder should be interpreted as a root path.<p>
     *
     * @param isTargetRootPath true if the target folder should be treated as a root path
     */
    public void setIsTargetRootPath(boolean isTargetRootPath) {

        m_isTargetRootPath = isTargetRootPath;
    }

    /**
     * Sets the upload target folder.<p>
     *
     * @param targetFolder the upload target folder
     */
    public void setTargetFolder(String targetFolder) {

        m_targetFolder = targetFolder;
        updateDialog();
    }

    /**
     * Sets the upload dialog instance.<p>
     *
     * @param uploadDialog the upload dialog instance
     */
    public void setUploadDialog(A_CmsUploadDialog uploadDialog) {

        m_uploadDialog = uploadDialog;
    }

    /**
     * Updates the dialog with the current close handler and target folder.<p>
     */
    protected void updateDialog() {

        if (m_uploadDialog != null) {
            if ((m_closeHandler != null)) {
                if (m_closeHandlerRegistration != null) {
                    m_closeHandlerRegistration.removeHandler();
                }
                m_closeHandlerRegistration = m_uploadDialog.addCloseHandler(m_closeHandler);
            }
            if ((m_targetFolder != null)) {
                m_uploadDialog.setTargetFolder(m_targetFolder);
            }
            m_uploadDialog.setIsTargetRootPath(m_isTargetRootPath);
        }
    }
}

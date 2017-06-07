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
import org.opencms.ade.upload.client.ui.CmsDialogUploadButtonHandler;
import org.opencms.gwt.client.ui.input.upload.I_CmsUploadButtonHandler;
import org.opencms.ui.shared.components.CmsUploadState;
import org.opencms.ui.shared.rpc.I_CmsUploadRpc;

import java.util.List;

import com.google.common.base.Supplier;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.button.ButtonConnector;
import com.vaadin.shared.ui.Connect;

/**
 * The upload button connector.<p>
 */
@Connect(org.opencms.ui.components.CmsUploadButton.class)
public class CmsUploadButtonConnector extends ButtonConnector {

    /**
     * Button context supplier.<p>
     */
    protected class ButtonContextSupplier implements Supplier<I_CmsUploadContext> {

        /**
         * @see com.google.common.base.Supplier#get()
         */
        public I_CmsUploadContext get() {

            return new I_CmsUploadContext() {

                public void onUploadFinished(List<String> uploadedFiles) {

                    CmsUploadButtonConnector.this.onUploadFinished(uploadedFiles);
                }

            };
        }
    }

    /** serial version id. */
    private static final long serialVersionUID = -746097406461678513L;

    /** The RPC proxy. */
    private I_CmsUploadRpc m_rpc;

    /**
     * Constructor.<p>
     */
    public CmsUploadButtonConnector() {
        m_rpc = getRpcProxy(I_CmsUploadRpc.class);
    }

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#getState()
     */
    @Override
    public CmsUploadState getState() {

        return (CmsUploadState)super.getState();
    }

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#getWidget()
     */
    @Override
    public CmsUploadButton getWidget() {

        return (CmsUploadButton)super.getWidget();
    }

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#onStateChanged(com.vaadin.client.communication.StateChangeEvent)
     */
    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {

        super.onStateChanged(stateChangeEvent);
        getWidget().reinitButton(createButtonHandler());
        getWidget().setUploadEnabled(isEnabled());
    }

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#createWidget()
     */
    @Override
    protected Widget createWidget() {

        CmsUploadButton uploadButton = new CmsUploadButton(createButtonHandler());
        uploadButton.setUploadEnabled(isEnabled());
        return uploadButton;
    }

    /**
     * Called once the upload is finished.<p>
     *
     * @param uploadedFiles the uploaded files
     */
    void onUploadFinished(List<String> uploadedFiles) {

        m_rpc.onUploadFinished(uploadedFiles);
        // create a new button handler instance, as the old one is in a messed up state
        getWidget().reinitButton(createButtonHandler());
    }

    /**
     * Creates a button handler according to the component state.<p>
     *
     * @return the button handler
     */
    private I_CmsUploadButtonHandler createButtonHandler() {

        I_CmsUploadButtonHandler buttonHandler = null;
        switch (getState().getUploadType()) {
            case singlefile:
                buttonHandler = new CmsSingleFileUploadHandler(
                    new ButtonContextSupplier(),
                    getState().getDialogTitle());
                ((CmsSingleFileUploadHandler)buttonHandler).setTargetFolderPath(getState().getTargetFolderRootPath());
                ((CmsSingleFileUploadHandler)buttonHandler).setTargetFileName(getState().getTargetFileName());
                ((CmsSingleFileUploadHandler)buttonHandler).setTargetFileNamePrefix(
                    getState().getTargetFileNamePrefix());
                break;
            case multifile:
            default:
                buttonHandler = new CmsDialogUploadButtonHandler(new ButtonContextSupplier());
                ((CmsDialogUploadButtonHandler)buttonHandler).setIsTargetRootPath(true);
                ((CmsDialogUploadButtonHandler)buttonHandler).setTargetFolder(getState().getTargetFolderRootPath());
                break;
        }
        return buttonHandler;
    }
}

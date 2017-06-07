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
import org.opencms.gwt.client.ui.input.upload.CmsFileInfo;
import org.opencms.ui.components.extensions.CmsUploadAreaExtension;
import org.opencms.ui.shared.components.CmsUploadAreaState;
import org.opencms.ui.shared.rpc.I_CmsUploadRpc;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

/**
 * The upload area connector.<p>
 */
@Connect(CmsUploadAreaExtension.class)
public class CmsUploadAreaConnector extends AbstractExtensionConnector {

    /** The serial version id. */
    private static final long serialVersionUID = 190108090241764065L;

    /** The RPC proxy. */
    private I_CmsUploadRpc m_rpc;

    /** The widget to enhance. */
    private Widget m_widget;

    /**
     * Constructor.<p>
     */
    public CmsUploadAreaConnector() {
        m_rpc = getRpcProxy(I_CmsUploadRpc.class);
    }

    /**
     * @see com.vaadin.client.ui.AbstractConnector#getState()
     */
    @Override
    public CmsUploadAreaState getState() {

        return (CmsUploadAreaState)super.getState();
    }

    /**
     * @see com.vaadin.client.extensions.AbstractExtensionConnector#extend(com.vaadin.client.ServerConnector)
     */
    @Override
    protected void extend(ServerConnector target) {

        // Get the extended widget
        m_widget = ((ComponentConnector)target).getWidget();
        initUploadZone(m_widget.getElement());
    }

    /**
     * Called on drag out.<p>
     */
    void dragOut() {

        m_widget.removeStyleName("o-upload-drop");
    }

    /**
     * Called on drag over.<p>
     */
    void dragOver() {

        m_widget.addStyleName("o-upload-drop");
    }

    /**
     * Called once the upload is finished<p>
     *
     * @param files the uploaded files
     */
    void uploadFinished(List<String> files) {

        m_rpc.onUploadFinished(files);
    }

    /**
     * Initializes the upload drop zone event handlers.<p>
     *
     * @param element the drop zone element
     */
    private native void initUploadZone(JavaScriptObject element)/*-{
        // check for file api support
        if ((typeof FileReader == 'function' || typeof FileReader == 'object')
                && (typeof FormData == 'function' || typeof FormData == 'object')) {
            var self = this;

            function isFileDrag(event) {
                var result = true;
                var dt = event.dataTransfer;
                for (var i = 0; i < dt.types.length; i++) {
                    // in case the types list contains text/html, we assume a DOM element is dragged, and no files
                    if (dt.types[i] == "text/html") {
                        result = false;
                        break;
                    }
                }
                return result;
            }

            function dragover(event) {
                if (isFileDrag(event)) {
                    event.stopPropagation();
                    event.preventDefault();
                    self.@org.opencms.ui.client.CmsUploadAreaConnector::dragOver()();
                }
            }

            function dragleave(event) {
                if (isFileDrag(event)) {
                    event.stopPropagation();
                    event.preventDefault();
                    self.@org.opencms.ui.client.CmsUploadAreaConnector::dragOut()();
                }
            }

            function drop(event) {
                if (isFileDrag(event)) {
                    event.stopPropagation();
                    event.preventDefault();
                    self.@org.opencms.ui.client.CmsUploadAreaConnector::dragOut()();
                    var dt = event.dataTransfer;
                    var files = dt.files;
                    self.@org.opencms.ui.client.CmsUploadAreaConnector::openUploadWithFiles(Lcom/google/gwt/core/client/JavaScriptObject;)(files);
                }
            }

            element.addEventListener("dragover", dragover, false);
            element.addEventListener("dragexit", dragleave, false);
            element.addEventListener("dragleave", dragleave, false);
            element.addEventListener("dragend", dragleave, false);
            element.addEventListener("drop", drop, false);
        }
    }-*/;

    /**
     * Opens the upload dialog with the given file references to upload.<p>
     *
     * @param files the file references
     */
    private void openUploadWithFiles(JavaScriptObject files) {

        JsArray<CmsFileInfo> cmsFiles = files.cast();
        List<CmsFileInfo> fileObjects = new ArrayList<CmsFileInfo>();
        for (int i = 0; i < cmsFiles.length(); ++i) {
            fileObjects.add(cmsFiles.get(i));
        }
        CmsDialogUploadButtonHandler buttonHandler = new CmsDialogUploadButtonHandler(
            new Supplier<I_CmsUploadContext>() {

                /**
                 * @see com.google.common.base.Supplier#get()
                 */
                public I_CmsUploadContext get() {

                    return new I_CmsUploadContext() {

                        public void onUploadFinished(List<String> uploadedFiles) {

                            uploadFinished(uploadedFiles);
                        }

                    };
                }
            });
        buttonHandler.setIsTargetRootPath(true);
        buttonHandler.setTargetFolder(getState().getTargetFolderRootPath());
        buttonHandler.openDialogWithFiles(fileObjects);
    }
}

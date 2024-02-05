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
import org.opencms.ade.upload.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.css.I_CmsConstantsBundle;
import org.opencms.gwt.client.ui.input.upload.CmsFileInfo;
import org.opencms.gwt.client.ui.input.upload.CmsUploadButton;
import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.HTML;

/**
 * Provides the upload dialog for form data support.<p>
 *
 * @since 8.0.0
 */
public class CmsUploadDialogFormDataImpl extends A_CmsUploadDialog {

    /** The highlighted state color. */
    private String m_hightLightColor = I_CmsConstantsBundle.INSTANCE.css().backgroundColorHighlight();

    /** The normal state color. */
    private String m_normalColor = I_CmsLayoutBundle.INSTANCE.constants().css().backgroundColorDialog();

    /**
     * Default constructor.<p>
     */
    public CmsUploadDialogFormDataImpl() {

        super();
        addUploadZone(m_scrollPanel.getElement(), this);
    }

    /**
     * @see org.opencms.ade.upload.client.ui.A_CmsUploadDialog#createInfoBean(org.opencms.gwt.client.ui.input.upload.CmsFileInfo)
     */
    @Override
    public CmsListInfoBean createInfoBean(CmsFileInfo file) {

        return new CmsListInfoBean(
            file.getFileName(),
            CmsUploadButton.formatBytes(file.getFileSize()) + " (" + getResourceType(file) + ")",
            null);
    }

    /**
     * @see org.opencms.ade.upload.client.ui.A_CmsUploadDialog#getFileSizeTooLargeMessage(org.opencms.gwt.client.ui.input.upload.CmsFileInfo)
     */
    @Override
    public String getFileSizeTooLargeMessage(CmsFileInfo file) {

        return Messages.get().key(
            Messages.GUI_UPLOAD_FILE_TOO_LARGE_2,
            CmsUploadButton.formatBytes(file.getFileSize()),
            CmsUploadButton.formatBytes(Long.valueOf(CmsCoreProvider.get().getUploadFileSizeLimit()).intValue()));
    }

    /**
     * @see org.opencms.ade.upload.client.ui.A_CmsUploadDialog#isTooLarge(org.opencms.gwt.client.ui.input.upload.CmsFileInfo)
     */
    @Override
    public boolean isTooLarge(CmsFileInfo cmsFileInfo) {

        long maxFileSize = CmsCoreProvider.get().getUploadFileSizeLimit();
        if (maxFileSize < 0) {
            return false;
        }
        return cmsFileInfo.getFileSize() > maxFileSize;
    }

    /**
     * @see org.opencms.ade.upload.client.ui.A_CmsUploadDialog#updateSummary()
     */
    @Override
    public void updateSummary() {

        setContentLength(calculateContentLength());
        StringBuffer buffer = new StringBuffer(64);
        buffer.append("<p class=\"").append(
            org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.uploadButton().dialogMessage()).append("\">");
        buffer.append("<b>" + Messages.get().key(Messages.GUI_UPLOAD_SUMMARY_FILES_0) + "</b> ");
        buffer.append(
            Messages.get().key(
                Messages.GUI_UPLOAD_SUMMARY_FILES_VALUE_3,
                Integer.valueOf(getFilesToUpload().size()),
                getFileText(),
                CmsUploadButton.formatBytes(Long.valueOf(getContentLength()).intValue())));
        buffer.append("</p>");
        setSummaryHTML(buffer.toString());
    }

    /**
     * Adds a javascript file array to the list of files to upload.<p>
     *
     * @param files a javascript file array
     */
    protected void addJsFiles(JavaScriptObject files) {

        JsArray<CmsFileInfo> cmsFiles = files.cast();
        List<CmsFileInfo> fileObjects = new ArrayList<CmsFileInfo>();
        for (int i = 0; i < cmsFiles.length(); ++i) {
            fileObjects.add(cmsFiles.get(i));
        }
        addFiles(fileObjects);
    }

    /**
     * Returns the content length.<p>
     *
     * @return the content length
     */
    protected long calculateContentLength() {

        int result = 0;
        for (CmsFileInfo file : getFilesToUpload().values()) {
            result += file.getFileSize();
        }
        return Long.valueOf(result).longValue();
    }

    /**
     * @see org.opencms.ade.upload.client.ui.A_CmsUploadDialog#removeDragAndDropMessage()
     */
    @Override
    protected void removeDragAndDropMessage() {

        if (m_dragAndDropMessage != null) {
            m_dragAndDropMessage.removeFromParent();
            m_dragAndDropMessage = null;
            m_normalColor = I_CmsLayoutBundle.INSTANCE.constants().css().backgroundColorDialog();
            m_scrollPanel.getElement().getStyle().setBackgroundColor(m_normalColor);
            doResize();
        }
    }

    /**
     * @see org.opencms.ade.upload.client.ui.A_CmsUploadDialog#setDragAndDropMessage()
     */
    @Override
    protected void setDragAndDropMessage() {

        if (m_dragAndDropMessage == null) {
            m_dragAndDropMessage = new HTML();
            m_dragAndDropMessage.setStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().dragAndDropMessage());
            m_dragAndDropMessage.setText(Messages.get().key(Messages.GUI_UPLOAD_DRAG_AND_DROP_ENABLED_0));
        }
        getContentWrapper().add(m_dragAndDropMessage);
        m_normalColor = I_CmsConstantsBundle.INSTANCE.css().notificationNormalBg();
        m_scrollPanel.getElement().getStyle().setBackgroundColor(m_normalColor);
        doResize();
    }

    /**
     * Adds a upload drop zone to the given element.<p>
     *
     * @param element the element to add the upload zone
     * @param dialog this dialog
     */
    private native void addUploadZone(JavaScriptObject element, CmsUploadDialogFormDataImpl dialog)/*-{

        function dragover(event) {
            event.stopPropagation();
            event.preventDefault();
            element.style.backgroundColor = dialog.@org.opencms.ade.upload.client.ui.CmsUploadDialogFormDataImpl::m_hightLightColor;
        }

        function dragleave(event) {
            event.stopPropagation();
            event.preventDefault();
            element.style.backgroundColor = dialog.@org.opencms.ade.upload.client.ui.CmsUploadDialogFormDataImpl::m_normalColor;
        }

        function drop(event) {
            event.preventDefault();
            var dt = event.dataTransfer;
            var files = dt.files;
            element.style.backgroundColor = dialog.@org.opencms.ade.upload.client.ui.CmsUploadDialogFormDataImpl::m_normalColor;
            dialog.@org.opencms.ade.upload.client.ui.CmsUploadDialogFormDataImpl::addJsFiles(Lcom/google/gwt/core/client/JavaScriptObject;)(files);
        }

        element.addEventListener("dragover", dragover, false);
        element.addEventListener("dragexit", dragleave, false);
        element.addEventListener("dragleave", dragleave, false);
        element.addEventListener("dragend", dragleave, false);
        element.addEventListener("drop", drop, false);

    }-*/;
}

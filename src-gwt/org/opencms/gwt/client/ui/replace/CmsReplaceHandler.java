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

package org.opencms.gwt.client.ui.replace;

import org.opencms.gwt.client.ui.contextmenu.CmsReplace;
import org.opencms.gwt.client.ui.input.upload.CmsFileInput;
import org.opencms.gwt.client.ui.input.upload.I_CmsUploadButton;
import org.opencms.gwt.client.ui.input.upload.I_CmsUploadButtonHandler;
import org.opencms.util.CmsUUID;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The replace dialog handler.<p>
 */
public class CmsReplaceHandler implements I_CmsUploadButtonHandler {

    /** The replace dialog. */
    private CmsReplaceDialog m_dialog;

    /** The menu item. */
    private CmsReplace m_menuItem;

    /** The structure id of the resource to replace. */
    private CmsUUID m_structureId;

    /** The upload button. */
    private I_CmsUploadButton m_uploadButton;

    /** The dialog close handler. */
    private CloseHandler<PopupPanel> m_closeHandler;

    /**
     * Constructor.<p>
     *
     * @param structureId the structure id of the resource to replace
     */
    public CmsReplaceHandler(CmsUUID structureId) {

        m_structureId = structureId;
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
            m_dialog = new CmsReplaceDialog(this);
            m_dialog.center();
            m_dialog.initContent(m_structureId);
            if (m_closeHandler != null) {
                m_dialog.addCloseHandler(m_closeHandler);
            }
        } else if (m_uploadButton != null) {
            m_uploadButton.createFileInput();
        }
        if (fileInput.getFiles().length == 1) {
            m_dialog.setFileInput(fileInput);
        }
        if (m_menuItem != null) {
            m_menuItem.getParentMenu().hide();
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
     * Sets the replace menu item.<p>
     *
     * @param menuItem the replace menu item to set
     */
    public void setMenuItem(CmsReplace menuItem) {

        m_menuItem = menuItem;
    }

}

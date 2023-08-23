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

package org.opencms.ade.upload.client.lists;

import org.opencms.ade.upload.client.I_CmsUploadContext;
import org.opencms.ade.upload.client.ui.CmsDialogUploadButtonHandler;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.input.upload.CmsUploadButton;
import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Content of the upload dialog used to upload files in lists.
 */
public class CmsUploadView extends Composite {

    /**
     * The uiBinder interface for this widget.<p>
     */
    interface I_UploadViewUiBinder extends UiBinder<Widget, CmsUploadView> {
        // empty
    }

    /** The uiBinder instance for this widget. */
    private static I_UploadViewUiBinder uiBinder = GWT.create(I_UploadViewUiBinder.class);

    /** The container for the list info item. */
    @UiField
    SimplePanel m_infoBoxContainer;

    /** The text displayed in the middle of the dialog. */
    @UiField
    Label m_mainLabel;

    /** The upload button. */
    @UiField(provided = true)
    CmsUploadButton m_uploadButton;

    /**
     * Creates a new instance.
     *
     * @param uploadFolder the upload folder
     * @param postCreateHandler the post-create handler
     * @param context the upload context
     * @param info the list info bean to display on top (may be null)
     */
    public CmsUploadView(
        String uploadFolder,
        String postCreateHandler,
        I_CmsUploadContext context,
        CmsListInfoBean info) {

        CmsDialogUploadButtonHandler handler = new CmsDialogUploadButtonHandler(() -> context);
        handler.setPostCreateHandler(postCreateHandler);
        handler.setTargetFolder(uploadFolder);
        m_uploadButton = new CmsUploadButton(handler);
        initWidget(uiBinder.createAndBindUi(this));
        if (info != null) {
            CmsListItemWidget infoWidget = new CmsListItemWidget(info);
            m_infoBoxContainer.add(infoWidget);
        }
        m_mainLabel.setText(CmsUploadMessages.innerText(uploadFolder));

    }

    /**
     * Gets the buttons.
     *
     * @return the buttons
     */
    public List<Widget> getButtons() {

        return Arrays.asList(m_uploadButton);
    }

}

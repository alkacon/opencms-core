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
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.shared.CmsListInfoBean;

import com.google.gwt.user.client.ui.Widget;

/**
 * Popup used for the upload dialog.
 */
public class CmsUploadPopup extends CmsPopup {

    /** The popup content. */
    private CmsUploadView m_view;

    /**
     * Creates a new instance.
     *
     * @param data the editable data
     * @param context the upload context
     * @param info the list info bean to display
     */
    public CmsUploadPopup(
        String uploadFolder,
        String postCreateHandler,
        I_CmsUploadContext context,
        CmsListInfoBean info) {

        super(CmsUploadMessages.dialogTitle());
        setModal(true);
        setGlassEnabled(true);
        addDialogClose(() -> {/*do nothing*/});
        m_view = new CmsUploadView(uploadFolder, postCreateHandler, context, info);
        setMainContent(m_view);
        for (Widget widget : m_view.getButtons()) {
            addButton(widget);
        }
    }
}

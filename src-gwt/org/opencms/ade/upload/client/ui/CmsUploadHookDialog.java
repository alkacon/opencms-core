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

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsFrameDialog;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.shared.I_CmsUploadConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * A opens a dialog which contains an IFRAME for displaying the upload hook JSP page.<p>
 */
public final class CmsUploadHookDialog {

    /** The dialog height. */
    public static final int DIALOG_HEIGHT = 300;

    /**
     * Hide public constructor.<p>
     */
    private CmsUploadHookDialog() {

        // noop
    }

    /**
     * Opens a new upload property dialog.<p>
     *
     * @param title the title for the dialog popup
     * @param hookUri the URI of the upload hook page
     * @param uploadedFiles the uploaded files
     * @param closeHandler the dialog close handler
     */
    public static void openDialog(
        String title,
        String hookUri,
        List<String> uploadedFiles,
        CloseHandler<PopupPanel> closeHandler) {

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(I_CmsUploadConstants.PARAM_RESOURCES, Joiner.on(",").join(uploadedFiles));
        CmsPopup popup = CmsFrameDialog.showFrameDialog(
            title,
            CmsCoreProvider.get().link(hookUri),
            parameters,
            closeHandler);
        popup.setHeight(DIALOG_HEIGHT);
        popup.setWidth(CmsPopup.DEFAULT_WIDTH);
        popup.center();
    }
}

/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.upload.client;

import org.opencms.ade.upload.client.ui.A_CmsUploadDialog;
import org.opencms.ade.upload.client.ui.CmsUploadDialogImpl;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsErrorDialog;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

/**
 * Upload dialog entry class.<p>
 * 
 * @since 8.0.0
 */
public class CmsUpload extends A_CmsEntryPoint {

    /** Name of exported dialog close function. */
    private static final String FUNCTION_OPEN_UPLOAD_DIALOG = "cms_ade_openUploadDialog";

    /**
     * Exports the open dialog method.<p>
     */
    public static native void exportOpenUploadDialog() /*-{

                                                       $wnd[@org.opencms.ade.upload.client.CmsUpload::FUNCTION_OPEN_UPLOAD_DIALOG] = function(
                                                       uploadTarget) {
                                                       @org.opencms.ade.upload.client.CmsUpload::openDialog(Ljava/lang/String;)(uploadTarget);
                                                       };

                                                       }-*/;

    /**
     * Opens an empty upload dialog.<p>
     * 
     * @param uploadTarget the target folder
     */
    private static void openDialog(String uploadTarget) {

        try {
            A_CmsUploadDialog dialog = GWT.create(CmsUploadDialogImpl.class);
            dialog.setContext(new I_CmsUploadContext() {

                public void onUploadFinished(List<String> uploadedFiles) {

                    Window.Location.reload();
                }
            });
            dialog.setTargetFolder(uploadTarget);
            dialog.loadAndShow();
        } catch (Exception e) {
            CmsErrorDialog.handleException(new Exception(
                "Deserialization of dialog data failed. This may be caused by expired java-script resources, please clear your browser cache and try again.",
                e));
        }
    }

    /**
     * @see org.opencms.gwt.client.A_CmsEntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        super.onModuleLoad();
        if ((getDialogMode() != null) && getDialogMode().equals("button")) {
            exportOpenUploadDialog();
        } else {
            try {
                A_CmsUploadDialog dialog = GWT.create(CmsUploadDialogImpl.class);
                I_CmsUploadContext context = new I_CmsUploadContext() {

                    public void onUploadFinished(List<String> uploadedFiles) {

                        String closeLink = getCloseLink() + "?resource=";
                        Window.Location.assign(CmsCoreProvider.get().link(closeLink));
                    }
                };
                dialog.setContext(context);
                dialog.setTargetFolder(getTargetFolder());
                dialog.loadAndShow();
            } catch (Exception e) {
                CmsErrorDialog.handleException(new Exception(
                    "Deserialization of dialog data failed. This may be caused by expired java-script resources, please clear your browser cache and try again.",
                    e));
            }
        }
    }

    /**
     * Retrieves the close link global variable as a string.<p>
     * 
     * @return the close link
     */
    protected native String getCloseLink() /*-{

                                           return $wnd[@org.opencms.gwt.shared.I_CmsUploadConstants::ATTR_CLOSE_LINK];

                                           }-*/;

    /**
     * Retrieves the dialog mode global variable as a string.<p>
     * 
     * @return the dialog mode
     */
    protected native String getDialogMode() /*-{

                                            return $wnd[@org.opencms.gwt.shared.I_CmsUploadConstants::ATTR_DIALOG_MODE];

                                            }-*/;

    /**
     * Retrieves the target folder global variable as a string.<p>
     * 
     * @return the target folder
     */
    private native String getTargetFolder() /*-{

                                            return $wnd[@org.opencms.gwt.shared.I_CmsUploadConstants::VAR_TARGET_FOLDER];

                                            }-*/;
}

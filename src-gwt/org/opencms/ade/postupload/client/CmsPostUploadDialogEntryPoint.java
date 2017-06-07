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

package org.opencms.ade.postupload.client;

import org.opencms.ade.postupload.client.ui.CmsUploadPropertyDialog;
import org.opencms.ade.postupload.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.CmsCoreProvider;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

/**
 * Dialog entry class.<p>
 *
 * @since 8.0.0
 */
public class CmsPostUploadDialogEntryPoint extends A_CmsEntryPoint {

    /** The name of the close function. */
    public static final String CLOSE_FUNCTION = "cmsCloseUploadHookDialog";

    /** Name of exported dialog close function. */
    private static final String FUNCTION_OPEN_DIALOG = "cms_ade_openDialog";

    /**
     * @see org.opencms.gwt.client.A_CmsEntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        super.onModuleLoad();
        I_CmsLayoutBundle.INSTANCE.dialogCss().ensureInjected();

        // load and show the dialog
        final CmsUploadPropertyDialog dialog = new CmsUploadPropertyDialog();
        Command onFinish = new Command() {

            /**
             * @see com.google.gwt.user.client.Command#execute()
             */
            public void execute() {

                if (!dialog.isIFrameMode()) {
                    String closeLink = getCloseLink() + "?resource=";
                    Window.Location.assign(CmsCoreProvider.get().link(closeLink));
                }
            }
        };
        dialog.setCloseCmd(onFinish);
        dialog.setTitle(Messages.get().key(Messages.GUI_DIALOG_TITLE_0));
        dialog.setWidth(600); //545
        dialog.loadAndShow();
    }

    /**
     * Retrieves the close link global variable as a string.<p>
     *
     * @return the close link
     */
    protected native String getCloseLink() /*-{

                                           return $wnd[@org.opencms.ade.postupload.shared.I_CmsDialogConstants::ATTR_CLOSE_LINK];
                                           }-*/;
}

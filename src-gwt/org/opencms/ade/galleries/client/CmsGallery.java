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

package org.opencms.ade.galleries.client;

import org.opencms.ade.galleries.client.ui.CmsGalleryDialog;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsToolbarPopup;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Gallery Dialog entry class to be open from the vfs tree.<p>
 *
 * @since 8.0.0
 */
public class CmsGallery extends A_CmsEntryPoint {

    /**
     * Closes the dialog.<p>
     */
    static native void closeDialog()/*-{

        if (typeof $wnd.closeDialog === 'function') {
            $wnd.closeDialog();
        } else if ($wnd[@org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants::ATTR_CLOSE_LINK]) {
            $wnd.location.href = $wnd[@org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants::ATTR_CLOSE_LINK];
        }
    }-*/;

    /**
     * @see org.opencms.gwt.client.A_CmsEntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        super.onModuleLoad();
        int dialogHeight = CmsToolbarPopup.getAvailableHeight();
        int dialogWidth = CmsToolbarPopup.getAvailableWidth();
        CmsPopup popup = new CmsPopup(dialogWidth);
        popup.setGlassEnabled(false);

        popup.removePadding();
        SimplePanel container = new SimplePanel();
        popup.setMainContent(container);
        popup.addDialogClose(new Command() {

            public void execute() {

                closeDialog();
            }
        });
        popup.center();
        popup.catchNotifications();
        CmsGalleryDialog dialog = CmsGalleryFactory.createDialog(popup);
        container.setWidget(dialog);
        dialog.setDialogSize(dialogWidth, dialogHeight);
        popup.center();

    }
}

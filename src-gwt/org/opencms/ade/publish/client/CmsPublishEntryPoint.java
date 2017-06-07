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

package org.opencms.ade.publish.client;

import org.opencms.ade.publish.shared.CmsPublishData;
import org.opencms.gwt.client.A_CmsEntryPoint;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcPrefetcher;
import org.opencms.gwt.client.ui.CmsErrorDialog;
import org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler;
import org.opencms.gwt.client.util.CmsJsUtil;
import org.opencms.util.CmsUUID;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The entry point for the publish module.
 *
 * @since 8.0.0
 */
public class CmsPublishEntryPoint extends A_CmsEntryPoint {

    /**
     * @see org.opencms.gwt.client.A_CmsEntryPoint#onModuleLoad()
     */
    @Override
    public void onModuleLoad() {

        super.onModuleLoad();
        CmsPublishData initData = null;
        try {
            initData = (CmsPublishData)CmsRpcPrefetcher.getSerializedObjectFromDictionary(
                CmsPublishDialog.getService(),
                CmsPublishData.DICT_NAME);
            String closeLink = initData.getCloseLink();
            if (closeLink == null) {
                closeLink = CmsCoreProvider.get().getDefaultWorkplaceLink();
            }
            final String constCloseLink = closeLink;
            final boolean confirm = initData.isShowConfirmation();
            CloseHandler<PopupPanel> closeHandler = new CloseHandler<PopupPanel>() {

                public void onClose(CloseEvent<PopupPanel> event) {

                    CmsPublishDialog dialog = (CmsPublishDialog)(event.getTarget());
                    if (confirm && (dialog.hasSucceeded() || dialog.hasFailed())) {
                        CmsPublishConfirmationDialog confirmation = new CmsPublishConfirmationDialog(
                            dialog,
                            constCloseLink);
                        confirmation.center();
                    } else {
                        // 'cancel' case
                        CmsJsUtil.closeWindow();
                        // in case the window isn't successfully closed, go to the workplace
                        Window.Location.assign(constCloseLink);
                    }
                }
            };

            CmsPublishDialog.showPublishDialog(initData, closeHandler, new Runnable() {

                public void run() {

                    Window.Location.reload();
                }

            }, new I_CmsContentEditorHandler() {

                public void onClose(String sitePath, CmsUUID structureId, boolean isNew) {

                    // nothing to do
                }
            });
        } catch (Exception e) {
            CmsErrorDialog.handleException(e);
        }
    }
}

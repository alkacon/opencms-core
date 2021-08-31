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

package org.opencms.gwt.client.ui.contextmenu;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsPreviewInfo;
import org.opencms.util.CmsUUID;

import com.google.gwt.user.client.Window;

/**
 * Context menu entry to show a container page.<p>
 */
public class CmsShowPage implements I_CmsHasContextMenuCommand, I_CmsContextMenuCommand {

    /**
     * Gets the context menu command instance.<p>
     *
     * @return the context menu command instance
     */
    public static I_CmsContextMenuCommand getContextMenuCommand() {

        return new CmsShowPage();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand#execute(org.opencms.util.CmsUUID, org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler, org.opencms.gwt.shared.CmsContextMenuEntryBean)
     */
    public void execute(final CmsUUID structureId, I_CmsContextMenuHandler handler, CmsContextMenuEntryBean bean) {

        CmsRpcAction<CmsPreviewInfo> previewAction = new CmsRpcAction<CmsPreviewInfo>() {

            @Override
            public void execute() {

                CmsCoreProvider.getVfsService().getPreviewInfo(structureId, CmsCoreProvider.get().getLocale(), this);
                start(0, true);
            }

            @Override
            protected void onResponse(CmsPreviewInfo result) {

                stop(false);
                if (result.getPreviewUrl() != null) {
                    Window.Location.assign(result.getPreviewUrl());
                } else {
                    CmsNotification.get().sendAlert(CmsNotification.Type.ERROR, result.getPreviewContent());
                }

            }
        };
        previewAction.execute();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand#getItemWidget(org.opencms.util.CmsUUID, org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler, org.opencms.gwt.shared.CmsContextMenuEntryBean)
     */
    public A_CmsContextMenuItem getItemWidget(
        CmsUUID structureId,
        I_CmsContextMenuHandler handler,
        CmsContextMenuEntryBean bean) {

        return null;
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand#hasItemWidget()
     */
    public boolean hasItemWidget() {

        return false;
    }

}

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

package org.opencms.gwt.client.ui.contextmenu;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsResourceInfoConfirmDialog;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.gwt.shared.CmsResourceStatusBean;
import org.opencms.util.CmsUUID;

/**
 * ADE context menu option for undeleting a file.<p>
 */
public class CmsUndelete implements I_CmsHasContextMenuCommand, I_CmsContextMenuCommand {

    /**
     * Creates a new context menu command.<p>
     *
     * @return the context menu command
     */
    public static I_CmsContextMenuCommand getContextMenuCommand() {

        return new CmsUndelete();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand#execute(org.opencms.util.CmsUUID, org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler, org.opencms.gwt.shared.CmsContextMenuEntryBean)
     */
    public void execute(
        final CmsUUID structureId,
        final I_CmsContextMenuHandler handler,
        CmsContextMenuEntryBean bean) {

        CmsCoreProvider.get();

        CmsRpcAction<CmsResourceStatusBean> loadStatusAction = new CmsRpcAction<CmsResourceStatusBean>() {

            @Override
            public void execute() {

                start(0, true);
                CmsCoreProvider.getVfsService().getResourceStatus(
                    structureId,
                    CmsCoreProvider.get().getLocale(),
                    false,
                    null,
                    this);

            }

            @Override
            protected void onResponse(CmsResourceStatusBean result) {

                stop(false);
                CmsResourceInfoConfirmDialog dialog = new CmsResourceInfoConfirmDialog(result) {

                    @Override
                    public String getCancelText() {

                        return Messages.get().key(Messages.GUI_CANCEL_0);
                    }

                    @Override
                    public String getCaption() {

                        return Messages.get().key(Messages.GUI_UNDELETE_CAPTION_0);
                    }

                    @Override
                    public String getOkText() {

                        return Messages.get().key(Messages.GUI_OK_0);
                    }

                    @Override
                    public String getText() {

                        return Messages.get().key(Messages.GUI_UNDELETE_TEXT_0);
                    }

                    @Override
                    public void onConfirm() {

                        CmsRpcAction<Void> undeleteAction = new CmsRpcAction<Void>() {

                            @Override
                            public void execute() {

                                start(0, true);
                                CmsCoreProvider.getVfsService().undelete(structureId, this);
                            }

                            @Override
                            protected void onResponse(Void voidResult) {

                                stop(false);
                                handler.refreshResource(structureId);
                            }

                        };
                        undeleteAction.execute();

                    }
                };
                dialog.display();
            }
        };
        loadStatusAction.execute();

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

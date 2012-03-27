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

package org.opencms.gwt.client.ui.contextmenu;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.util.CmsUUID;

/**
 * The class for the "logout" context menu entries.<p>
 * 
 * @since 8.0.0
 */
public final class CmsLogout implements I_CmsHasContextMenuCommand {

    /**
     * Hidden utility class constructor.<p>
     */
    private CmsLogout() {

        // nothing to do
    }

    /**
     * Returns the context menu command according to 
     * {@link org.opencms.gwt.client.ui.contextmenu.I_CmsHasContextMenuCommand}.<p>
     * 
     * @return the context menu command
     */
    public static I_CmsContextMenuCommand getContextMenuCommand() {

        return new I_CmsContextMenuCommand() {

            public void execute(CmsUUID structureId, final I_CmsContextMenuHandler handler, CmsContextMenuEntryBean bean) {

                CmsConfirmDialog dialog = new CmsConfirmDialog(
                    Messages.get().key(Messages.GUI_DIALOG_LOGOUT_TITLE_0),
                    Messages.get().key(Messages.GUI_DIALOG_LOGOUT_TEXT_0));
                dialog.setOkText(Messages.get().key(Messages.GUI_YES_0));
                dialog.setCloseText(Messages.get().key(Messages.GUI_NO_0));
                dialog.setHandler(new I_CmsConfirmDialogHandler() {

                    public void onClose() {

                        // nothing to do
                    }

                    public void onOk() {

                        String logoutTarget = CmsCoreProvider.get().link(CmsCoreProvider.get().getLoginURL())
                            + "?logout=true";
                        handler.leavePage(logoutTarget);
                    }
                });
                dialog.center();
            }

            public String getCommandIconClass() {

                return org.opencms.gwt.client.ui.css.I_CmsImageBundle.INSTANCE.contextMenuIcons().logout();
            }
        };
    }
}

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

import org.opencms.gwt.client.ui.CmsLockReportDialog;
import org.opencms.gwt.client.ui.I_CmsToolbarHandler;
import org.opencms.gwt.shared.CmsContextMenuEntryBean;
import org.opencms.util.CmsUUID;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

/**
 * The lock report context menu command.<p>
 */
public class CmsLockReport implements I_CmsHasContextMenuCommand, I_CmsContextMenuCommand {

    /** The context menu handler for this command instance. */
    protected I_CmsContextMenuHandler m_menuHandler;

    /**
     * Returns the context menu command according to
     * {@link org.opencms.gwt.client.ui.contextmenu.I_CmsHasContextMenuCommand}.<p>
     *
     * @return the context menu command
     */
    public static I_CmsContextMenuCommand getContextMenuCommand() {

        return new CmsLockReport();
    }

    /**
     * @see org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuCommand#execute(org.opencms.util.CmsUUID, org.opencms.gwt.client.ui.contextmenu.I_CmsContextMenuHandler, org.opencms.gwt.shared.CmsContextMenuEntryBean)
     */
    public void execute(
        final CmsUUID structureId,
        final I_CmsContextMenuHandler handler,
        CmsContextMenuEntryBean bean) {

        m_menuHandler = handler;
        if (m_menuHandler instanceof I_CmsToolbarHandler) {
            ((I_CmsToolbarHandler)m_menuHandler).deactivateCurrentButton();
        }
        CmsLockReportDialog.openDialogForResource(null, structureId, new Command() {

            public void execute() {

                onUnlock();
                handler.refreshResource(structureId);

            }
        }, null);
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

    /**
     * Executed on unlock of the current resource to reload the page.<p>
     */
    protected void onUnlock() {

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                String url = Window.Location.getHref();
                m_menuHandler.leavePage(url);
            }
        });
    }

}

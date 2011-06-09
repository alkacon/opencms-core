/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsLockReportDialog.java,v $
 * Date   : $Date: 2011/06/09 12:48:09 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.tree.CmsTree;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsListInfoBean.LockIcon;
import org.opencms.gwt.shared.CmsLockReportInfo;
import org.opencms.util.CmsUUID;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;

/**
 * The lock report dialog.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.1
 */
public final class CmsLockReportDialog extends CmsPopup {

    /** The dialog width. */
    private static int DIALOG_WIDTH = 450;

    /** The close button. */
    private CmsPushButton m_closeButton;

    /** Command executed on resource unlock. */
    private Command m_onUnlock;

    /** The structure id of the resource to report on. */
    private CmsUUID m_structureId;

    /** The unlock button. */
    private CmsPushButton m_unlockButton;

    /**
     * Constructor.<p>
     * 
     * @param structureId the structure id of the resource to unlock
     * @param the command to execute on unlock of the resource
     */
    private CmsLockReportDialog(CmsUUID structureId, Command onUnlock) {

        super("Lock Report", DIALOG_WIDTH);
        m_structureId = structureId;
        m_onUnlock = onUnlock;
        m_closeButton = new CmsPushButton();
        m_closeButton.setText(Messages.get().key(Messages.GUI_CLOSE_0));
        m_closeButton.setUseMinWidth(true);
        m_closeButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.BLUE);
        m_closeButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                hide();
            }
        });
        addButton(m_closeButton);
        addDialogClose(null);
        m_unlockButton = new CmsPushButton();
        m_unlockButton.setText("Unlock all");
        m_unlockButton.setUseMinWidth(true);
        m_unlockButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.RED);
        m_unlockButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                unlock();
            }
        });
        m_unlockButton.setVisible(false);
        addButton(m_unlockButton);
        setGlassEnabled(true);
    }

    /**
     * Opens the lock report dialog for the given resource.<p>
     * 
     * @param structureId the structure id of the resource
     * @param onUnlock the command to execute after the has been unlocked
     */
    public static void openDialogForResource(final CmsUUID structureId, Command onUnlock) {

        final CmsLockReportDialog dialog = new CmsLockReportDialog(structureId, onUnlock);
        CmsRpcAction<CmsLockReportInfo> action = new CmsRpcAction<CmsLockReportInfo>() {

            @Override
            public void execute() {

                CmsCoreProvider.getVfsService().getLockReportInfo(structureId, this);
            }

            @Override
            public void onFailure(Throwable t) {

                dialog.hide();
                super.onFailure(t);
            }

            @Override
            protected void onResponse(CmsLockReportInfo result) {

                dialog.initContent(result);
            }
        };
        dialog.center();
        action.execute();

    }

    /**
     * Returns the structure id of the resource to report on.<p>
     * 
     * @return the structure id
     */
    protected CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Initializes the dialog content with the give report info.<p>
     * 
     * @param reportInfo the report info
     */
    protected void initContent(CmsLockReportInfo reportInfo) {

        CmsTree<CmsTreeItem> content = new CmsTree<CmsTreeItem>();

        CmsListItemWidget resourceListItemWidget = new CmsListItemWidget(reportInfo.getResourceInfo());
        CmsTreeItem treeItem = new CmsTreeItem(false, resourceListItemWidget);
        content.addItem(treeItem);
        for (CmsListInfoBean lockedInfo : reportInfo.getLockedResourceInfos()) {
            CmsListItemWidget listItemWidget = new CmsListItemWidget(lockedInfo);
            treeItem.addChild(new CmsTreeItem(false, listItemWidget));
        }
        treeItem.setOpen(true);
        this.setMainContent(content);

        // only show the unlock button if the resource or a descending resource is locked
        if (!reportInfo.getLockedResourceInfos().isEmpty()
            || ((reportInfo.getResourceInfo().getLockIcon() != null) && (reportInfo.getResourceInfo().getLockIcon() != LockIcon.NONE))) {
            m_unlockButton.setVisible(true);
        }
        if (isShowing()) {
            content.truncate(this.getClass().getName(), DIALOG_WIDTH);
            center();
        }
    }

    /**
     * Executed on unlock.<p>
     */
    protected void onUnlock() {

        if (m_onUnlock != null) {
            m_onUnlock.execute();
        }
        hide();
    }

    /**
     * Unlocks the resource and all descending resources.<p>
     */
    protected void unlock() {

        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            @Override
            public void execute() {

                CmsCoreProvider.getVfsService().forceUnlock(getStructureId(), this);
            }

            @Override
            public void onFailure(Throwable t) {

                hide();
                super.onFailure(t);
            }

            @Override
            protected void onResponse(Void result) {

                onUnlock();
            }
        };
        m_closeButton.disable("Processing...");
        m_unlockButton.disable("Processing...");
        action.execute();
    }

}

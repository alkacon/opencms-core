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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsListInfoBean.LockIcon;
import org.opencms.gwt.shared.CmsLockReportInfo;
import org.opencms.util.CmsUUID;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * The lock report dialog.<p>
 *
 * @since 8.0.1
 */
public final class CmsLockReportDialog extends CmsPopup {

    /**
     * Handles the scroll panel height.<p>
     */
    private class HeightHandler implements CloseHandler<CmsListItemWidget>, OpenHandler<CmsListItemWidget> {

        /**
         * Constructor.<p>
         */
        protected HeightHandler() {

            // nothing to do
        }

        /**
         * @see com.google.gwt.event.logical.shared.CloseHandler#onClose(com.google.gwt.event.logical.shared.CloseEvent)
         */
        public void onClose(CloseEvent<CmsListItemWidget> event) {

            adjustHeight();
        }

        /**
         * @see com.google.gwt.event.logical.shared.OpenHandler#onOpen(com.google.gwt.event.logical.shared.OpenEvent)
         */
        public void onOpen(OpenEvent<CmsListItemWidget> event) {

            adjustHeight();
        }
    }

    /** The text metrics key. */
    private static final String TEXT_METRICS_KEY = "CMS_LOCK_REPORT_DIALOG_METRICS";

    /** The close button. */
    private CmsPushButton m_closeButton;

    /** Command executed on resource unlock. */
    private Command m_onUnlock;

    /** The resource item widget. */
    private CmsListItemWidget m_resourceItem;

    /** The scroll panel. */
    private FlowPanel m_scrollPanel;

    /** The structure id of the resource to report on. */
    private CmsUUID m_structureId;

    /** The unlock button. */
    private CmsPushButton m_unlockButton;

    /**
     * Constructor.<p>
     *
     * @param title the title for the dialog (a default value will be used if this is null)
     * @param structureId the structure id of the resource to unlock
     * @param onUnlock command to execute after unlocking
     * @param optionalOnCloseCommand optional action to execute when the dialog is closed
     */
    private CmsLockReportDialog(
        String title,
        CmsUUID structureId,
        Command onUnlock,
        final Command optionalOnCloseCommand) {

        super(title != null ? title : Messages.get().key(Messages.GUI_LOCK_REPORT_TITLE_0));
        m_structureId = structureId;
        m_onUnlock = onUnlock;
        m_closeButton = new CmsPushButton();
        m_closeButton.setText(Messages.get().key(Messages.GUI_CANCEL_0));
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
        if (optionalOnCloseCommand != null) {
            addCloseHandler(new CloseHandler<PopupPanel>() {

                public void onClose(CloseEvent<PopupPanel> event) {

                    optionalOnCloseCommand.execute();

                }
            });
        }
        m_unlockButton = new CmsPushButton();
        m_unlockButton.setText(Messages.get().key(Messages.GUI_UNLOCK_0));
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
     * @param title the dialog title (will use a default value if null)
     * @param structureId the structure id of the resource
     * @param onUnlock the command to execute after the has been unlocked
     * @param optionalOnCloseCommand the optional command to execute when the lock report dialog is closed
     */
    public static void openDialogForResource(
        String title,
        final CmsUUID structureId,
        Command onUnlock,
        Command optionalOnCloseCommand) {

        final CmsLockReportDialog dialog = new CmsLockReportDialog(
            title,
            structureId,
            onUnlock,
            optionalOnCloseCommand);
        CmsRpcAction<CmsLockReportInfo> action = new CmsRpcAction<CmsLockReportInfo>() {

            @Override
            public void execute() {

                start(0, true);
                CmsCoreProvider.getVfsService().getLockReportInfo(structureId, this);
            }

            @Override
            public void onFailure(Throwable t) {

                stop(false);
                dialog.hide();
                super.onFailure(t);
            }

            @Override
            protected void onResponse(CmsLockReportInfo result) {

                stop(false);
                dialog.initContent(result);
            }
        };
        dialog.center();
        action.execute();

    }

    /**
     * Adjusts the height of the scroll panel.<p>
     */
    protected void adjustHeight() {

        if ((m_scrollPanel != null) && (m_resourceItem != null)) {
            m_scrollPanel.getElement().getStyle().setPropertyPx(
                "maxHeight",
                getAvailableHeight(m_resourceItem.getOffsetHeight()));
        }
        center();
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

        FlowPanel content = new FlowPanel();

        m_resourceItem = new CmsListItemWidget(reportInfo.getResourceInfo());
        HeightHandler heightHandler = new HeightHandler();
        m_resourceItem.addOpenHandler(heightHandler);
        m_resourceItem.addCloseHandler(heightHandler);
        content.add(m_resourceItem);
        m_scrollPanel = new FlowPanel();
        m_scrollPanel.setStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().border());
        m_scrollPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().cornerAll());
        m_scrollPanel.addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().logReportScrollPanel());
        CmsList<CmsListItem> list = null;
        CmsMessageWidget message = new CmsMessageWidget();
        m_scrollPanel.add(message);
        message.setMessageText(
            getMessageForLock(
                reportInfo.getResourceInfo().getLockIcon(),
                !reportInfo.getLockedResourceInfos().isEmpty()));
        if (!reportInfo.getLockedResourceInfos().isEmpty()
            || ((reportInfo.getResourceInfo().getLockIcon() != null)
                && (reportInfo.getResourceInfo().getLockIcon() != LockIcon.NONE))) {
            m_unlockButton.setVisible(true);
        }
        // only show the unlock button if the resource or a descending resource is locked
        if (!reportInfo.getLockedResourceInfos().isEmpty()) {
            m_unlockButton.setText(Messages.get().key(Messages.GUI_UNLOCK_ALL_0));
            list = new CmsList<CmsListItem>();
            for (CmsListInfoBean lockedInfo : reportInfo.getLockedResourceInfos()) {
                CmsListItemWidget listItemWidget = new CmsListItemWidget(lockedInfo);
                listItemWidget.addOpenHandler(heightHandler);
                listItemWidget.addCloseHandler(heightHandler);
                list.addItem(new CmsListItem(listItemWidget));
            }
            m_scrollPanel.add(list);
        }

        content.add(m_scrollPanel);
        setMainContent(content);
        if (isShowing()) {
            m_resourceItem.truncate(TEXT_METRICS_KEY, CmsPopup.DEFAULT_WIDTH - 10);
            if (list != null) {
                list.truncate(TEXT_METRICS_KEY, CmsPopup.DEFAULT_WIDTH - 10);
            }
            adjustHeight();
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
        m_closeButton.disable(Messages.get().key(Messages.GUI_LOADING_0));
        m_unlockButton.disable(Messages.get().key(Messages.GUI_LOADING_0));
        action.execute();
    }

    /**
     * Returns the dialog message for the given lock.<p>
     *
     * @param lockIcon the lock icon
     * @param hasLockedChildren <code>true</code> if the given resource has locked children
     *
     * @return the dialog message
     */
    private String getMessageForLock(LockIcon lockIcon, boolean hasLockedChildren) {

        String result = "";
        if (!hasLockedChildren && ((lockIcon == null) || (lockIcon == LockIcon.NONE))) {
            result = Messages.get().key(Messages.GUI_LOCK_REPORT_NOTHING_LOCKED_0);
        } else if ((lockIcon == LockIcon.OPEN) || (lockIcon == LockIcon.SHARED_OPEN)) {
            if (hasLockedChildren) {
                result = Messages.get().key(Messages.GUI_LOCK_REPORT_UNLOCK_ALL_MESSAGE_0);
            } else {
                result = Messages.get().key(Messages.GUI_LOCK_REPORT_UNLOCK_MESSAGE_0);
            }
        } else {
            if (hasLockedChildren) {
                result = Messages.get().key(Messages.GUI_LOCK_REPORT_STEAL_ALL_LOCKS_MESSAGE_0);
            } else {
                result = Messages.get().key(Messages.GUI_LOCK_REPORT_STEAL_LOCK_MESSAGE_0);
            }
        }
        return result;
    }

}

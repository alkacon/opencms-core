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

package org.opencms.gwt.client.ui.restore;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsRestoreInfoBean;
import org.opencms.gwt.shared.rpc.I_CmsVfsServiceAsync;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The content widget for the restore dialog.<p>
 */
public class CmsRestoreView extends Composite {

    /** The UiBinder class for this widget. */
    interface I_CmsRestoreViewUiBinder extends UiBinder<Widget, CmsRestoreView> {
        //empty
    }

    /** The UiBinder instance for this widget. */
    private static I_CmsRestoreViewUiBinder uiBinder = GWT.create(I_CmsRestoreViewUiBinder.class);

    /** The cancel button. */
    @UiField
    protected CmsPushButton m_cancelButton;

    /** The container for the file info box. */
    @UiField
    protected FlowPanel m_infoBoxContainer;

    /** The label which informs the user that the resource has moved. */
    @UiField
    protected Label m_movedLabel;

    /** The section which is displayed when the resource was moved. */
    @UiField
    protected Panel m_movedSection;

    /** The OK button. */
    @UiField
    protected CmsPushButton m_okButton;

    /** The popup in which this widget is contained. */
    protected CmsPopup m_popup;

    /** The bean containing information about the resource to restore. */
    protected CmsRestoreInfoBean m_restoreInfo;

    /** The check box used for selecting whether a move operation should be undone. */
    @UiField
    protected CmsCheckBox m_undoMoveCheckbox;

    /** The action which is executed after undoing the changes for the resource. */
    Runnable m_afterRestoreAction;

    /**
     * Creates a new widget instance.<p>
     *
     * @param restoreInfo a bean with information about the resource to restore
     * @param afterRestoreAction the action to execute after restoring the resource
     */
    public CmsRestoreView(CmsRestoreInfoBean restoreInfo, Runnable afterRestoreAction) {

        initWidget(uiBinder.createAndBindUi(this));
        m_afterRestoreAction = afterRestoreAction;
        CmsListInfoBean listInfo = restoreInfo.getListInfoBean();
        listInfo.addAdditionalInfo(CmsRestoreMessages.messageDateModified(), restoreInfo.getOfflineDate());
        listInfo.addAdditionalInfo(CmsRestoreMessages.messageDateModifiedOnline(), restoreInfo.getOnlineDate());
        CmsListItemWidget liWidget = new CmsListItemWidget(restoreInfo.getListInfoBean());
        CmsListItem li = new CmsListItem(liWidget);
        m_infoBoxContainer.add(li);
        if (restoreInfo.isMoved() && restoreInfo.canUndoMove()) {
            m_movedLabel.setText(
                CmsRestoreMessages.messageMoved(restoreInfo.getOnlinePath(), restoreInfo.getOfflinePath()));
            m_movedSection.setVisible(true);
        }
        m_restoreInfo = restoreInfo;
    }

    /**
     * Sets the popup in which this widget is displayed.<p>
     *
     * @param popup the popup in which this widget is displayed
     */
    public void setPopup(CmsPopup popup) {

        m_popup = popup;
    }

    /**
     * Handler for the cancel button.<p>
     *
     * @param e the click event
     */
    @UiHandler("m_cancelButton")
    protected void onClickCancel(ClickEvent e) {

        m_popup.hide();
    }

    /**
     * Click handler for the OK button.<p>
     *
     * @param e the click event
     */
    @UiHandler("m_okButton")
    protected void onClickOk(ClickEvent e) {

        final boolean undoMove = m_undoMoveCheckbox.getFormValue().booleanValue();
        m_popup.hide();
        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            @Override
            public void execute() {

                start(200, true);
                I_CmsVfsServiceAsync service = CmsCoreProvider.getVfsService();
                service.undoChanges(m_restoreInfo.getStructureId(), undoMove, this);
            }

            @Override
            protected void onResponse(Void result) {

                stop(false);
                if (m_afterRestoreAction != null) {
                    m_afterRestoreAction.run();
                }
            }

        };
        action.execute();

    }

    /**
     * Gets the list of buttons for the dialog.<p>
     *
     * @return the list of buttons for the dialog
     */
    List<CmsPushButton> getDialogButtons() {

        List<CmsPushButton> result = new ArrayList<CmsPushButton>();
        result.add(m_cancelButton);
        result.add(m_okButton);
        return result;
    }

}

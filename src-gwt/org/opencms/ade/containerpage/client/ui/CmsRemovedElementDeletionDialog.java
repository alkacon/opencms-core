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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.shared.CmsRemovedElementStatus;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Panel;

/**
 * Dialog for asking the user whether elements which were removed from the container page should be deleted.<p>
 */
public class CmsRemovedElementDeletionDialog extends CmsPopup {

    /**
     * Class with message accessors for the UiBinder.
     */
    public static class Messages {

        /**
         * Message accessor.<p>
         *
         * @return the message text
         */
        public static String messageCancel() {

            return org.opencms.ade.containerpage.client.Messages.get().key(
                org.opencms.ade.containerpage.client.Messages.GUI_KEEP_ELEMENT_0);

        }

        /**
         * Message accessor.<p>
         *
         * @return the message text
         */
        public static String messageMainText() {

            return org.opencms.ade.containerpage.client.Messages.get().key(
                org.opencms.ade.containerpage.client.Messages.GUI_ASK_DELETE_REMOVED_ELEMENT_0);
        }

        /**
         * Message accessor.<p>
         *
         * @return the message text
         */
        public static String messageOk() {

            return org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_DELETE_0);
        }

        /**
         * Message accessor.<p>
         *
         * @return the message text
         */
        public static String messageTitle() {

            return org.opencms.ade.containerpage.client.Messages.get().key(
                org.opencms.ade.containerpage.client.Messages.GUI_ASK_DELETE_REMOVED_ELEMENT_TITLE_0);
        }
    }

    /**
     * UiBinder interface for this dialog.<p>
     */
    interface I_UiBinder extends UiBinder<Panel, CmsRemovedElementDeletionDialog> {
        // empty uibinder interface
    }

    /** UiBinder instance for this dialog. */
    private static I_UiBinder uibinder = GWT.create(I_UiBinder.class);

    /** The cancel button. */
    @UiField
    protected CmsPushButton m_cancelButton;

    /** The container for the file info box. */
    @UiField
    protected Panel m_infoBoxContainer;

    /** The ok button. */
    @UiField
    protected CmsPushButton m_okButton;

    /** The status of the removed element. */
    CmsRemovedElementStatus m_status;

    /**
     * Creates a new dialog instance.<p>
     *
     * @param status the status of the removed element
     */
    public CmsRemovedElementDeletionDialog(CmsRemovedElementStatus status) {

        super(
            org.opencms.ade.containerpage.client.Messages.get().key(
                org.opencms.ade.containerpage.client.Messages.GUI_ASK_DELETE_REMOVED_ELEMENT_TITLE_0));
        setModal(true);
        setGlassEnabled(true);
        m_status = status;
        CmsListInfoBean elementInfo = status.getElementInfo();
        Panel panel = uibinder.createAndBindUi(this);
        CmsListItemWidget infoBox = new CmsListItemWidget(elementInfo);
        m_infoBoxContainer.add(infoBox);
        setMainContent(panel);
        m_okButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.RED);
        for (CmsPushButton button : getDialogButtons()) {
            addButton(button);
        }
    }

    /**
     * Click handler for the cancel button.<p>
     *
     * @param e the click event
     */
    @UiHandler("m_cancelButton")
    protected void onClickCancel(ClickEvent e) {

        hide();
    }

    /**
     * Click handler for the ok button.<p>
     *
     * @param e the click event
     */
    @UiHandler("m_okButton")
    protected void onClickOk(ClickEvent e) {

        CmsRpcAction<Void> deleteAction = new CmsRpcAction<Void>() {

            @Override
            public void execute() {

                start(200, true);
                CmsCoreProvider.getVfsService().deleteResource(m_status.getStructureId(), this);
            }

            @Override
            public void onResponse(Void result) {

                stop(true);
                hide();
            }
        };
        deleteAction.execute();
    }

    /**
     * Gets a list of the dialog buttons.<p>
     *
     * @return the list of dialog buttons
     */
    private List<CmsPushButton> getDialogButtons() {

        List<CmsPushButton> result = new ArrayList<CmsPushButton>();
        result.add(m_cancelButton);
        result.add(m_okButton);
        return result;
    }

}

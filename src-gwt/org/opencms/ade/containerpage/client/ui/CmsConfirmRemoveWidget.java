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

import org.opencms.ade.containerpage.client.Messages;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

/**
 * Widget for asking the user for confirmation whether a container page element should be removed.<p>
 */
public class CmsConfirmRemoveWidget extends Composite {

    /** 
     * Interface with message string constants.<p>
     */
    public static class MessageStrings {

        /**
         * Message string provider.<p>
         * 
         * @return a message string 
         */
        public static String cancel() {

            return org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_CANCEL_0);
        }

        /**
         * Message string provider.<p>
         * 
         * @return a message string 
         */
        public static String caption() {

            return Messages.get().key(Messages.GUI_CONFIRM_REMOVAL_CAPTION_0);
        }

        /**
         * Message string provider.<p>
         * 
         * @return a message string 
         */
        public static String checkboxText() {

            return Messages.get().key(Messages.GUI_CONFIRM_REMOVAL_DELETE_CHECKBOX_0);
        }

        /**
         * Message string provider.<p>
         * 
         * @return a message string 
         */
        public static String confirmText() {

            return Messages.get().key(Messages.GUI_CONFIRM_REMOVAL_TEXT_0);
        }

        /**
         * Message string provider.<p>
         * 
         * @return a message string 
         */
        public static String noReferenceText() {

            return Messages.get().key(Messages.GUI_CONFIRM_REMOVAL_CAN_DELETE_0);
        }

        /**
         * Message string provider.<p>
         * 
         * @return a message string 
         */
        public static String ok() {

            return org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_OK_0);
        }
    }

    /**
     * UiBinder interface for this dialog.<p>
     */
    interface I_UiBinder extends UiBinder<Panel, CmsConfirmRemoveWidget> {
        // empty uibinder interface 
    }

    /** UiBinder instance for this dialog. */
    private static I_UiBinder uibinder = GWT.create(I_UiBinder.class);

    /** The Cancel button. */
    @UiField
    protected CmsPushButton m_cancelButton;

    /** Checkbox for selecting whether the resource should be deleted. */
    @UiField
    protected CmsCheckBox m_checkBox;

    /** True if this dialog has a checkbox for deleting the resource. */
    protected boolean m_hasDeleteCheckbox;

    /** The widget containing the element info box. */
    @UiField
    protected Panel m_infoBoxContainer;

    /** The label with the dialog text. */
    @UiField
    protected Label m_mainLabel;

    /** The label which appears when the user is given the option to delete the resource. */
    @UiField
    protected Label m_noReferenceLabel;

    /** The OK button. */
    @UiField
    protected CmsPushButton m_okButton;

    /** 
     * The callback which will be called when the user has confirmed or cancelled the element removal.<p>
     */
    protected AsyncCallback<Boolean> m_removeCallback;

    /** True if the resource should be deleted. */
    boolean m_deleteContent;

    /** The dialog instance which this widget appears in. */
    private CmsPopup m_popup;

    /**
     * Creates a new instance.<p>
     * 
     * @param elementInfo the element info bean 
     * @param canDelete true if the user should be given the option to delete the resource 
     * @param removeCallback the callback to execute after the dialog closes 
     */
    public CmsConfirmRemoveWidget(CmsListInfoBean elementInfo, boolean canDelete, AsyncCallback<Boolean> removeCallback) {

        m_removeCallback = removeCallback;
        initWidget(uibinder.createAndBindUi(this));
        CmsListItemWidget itemWidget = new CmsListItemWidget(elementInfo);
        CmsListItem item = new CmsListItem(itemWidget);
        m_infoBoxContainer.add(item);
        m_okButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.RED);
        if (canDelete) {
            m_checkBox.setVisible(true);
            m_checkBox.setChecked(true);
            m_deleteContent = true;
            m_noReferenceLabel.setVisible(true);
        }
    }

    /** 
     * Gets the buttons for the dialog.<p>
     * 
     * @return the buttons for the dialog 
     */
    public List<CmsPushButton> getButtons() {

        return Arrays.asList(m_cancelButton, m_okButton);
    }

    /**
     * The click handler for the cancel button.<p>
     * 
     * @param event the click event 
     */
    @UiHandler("m_cancelButton")
    public void onClickCancel(ClickEvent event) {

        m_popup.hide();
        m_removeCallback.onFailure(null);
    }

    /**
     * The click handler for the OK button.<p>
     * 
     * @param event the click event 
     */
    @UiHandler("m_okButton")
    public void onClickOk(ClickEvent event) {

        m_removeCallback.onSuccess(Boolean.valueOf(m_deleteContent));
        m_popup.hide();
    }

    /**
     * Value change handler for the 'delete' checkbox.<p>
     * 
     * @param event the value change event 
     */
    @UiHandler("m_checkBox")
    public void onValueChange(ValueChangeEvent<Boolean> event) {

        m_deleteContent = event.getValue().booleanValue();
    }

    /**
     * Sets the popup which this widget is used in.<p>
     * 
     * @param popup the popup 
     */
    public void setPopup(CmsPopup popup) {

        m_popup = popup;
    }

}

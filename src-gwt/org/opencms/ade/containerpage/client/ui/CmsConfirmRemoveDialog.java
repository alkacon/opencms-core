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
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.input.CmsCheckBox;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Dialog used for confirming the removal of an element in the container page editor.<p>
 */
public class CmsConfirmRemoveDialog extends CmsPopup {

    /** 
     * Interface with message string constants.<p>
     */
    public static interface I_MessageStrings {

        /** Message constant. */
        String CANCEL = org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_CANCEL_0);

        /** Message constant. */
        String CAPTION = Messages.get().key(Messages.GUI_CONFIRM_REMOVAL_CAPTION_0);

        /** Message constant. */
        String CHECKBOX_TEXT = Messages.get().key(Messages.GUI_CONFIRM_REMOVAL_DELETE_CHECKBOX_0);

        /** Message constant. */
        String CONFIRM_TEXT = Messages.get().key(Messages.GUI_CONFIRM_REMOVAL_TEXT_0);

        /** Message constant. */
        String OK = org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_OK_0);
    }

    /**
     * Value change handler for the 'delete' checkbox.<p>
     */
    protected ValueChangeHandler<Boolean> m_onCheckboxChange = new ValueChangeHandler<Boolean>() {

        public void onValueChange(ValueChangeEvent<Boolean> event) {

            m_deleteContent = event.getValue().booleanValue();
        }
    };

    /**
     * Click handler for the Cancel button.<p>
     */
    protected ClickHandler m_onClickCancel = new ClickHandler() {

        public void onClick(ClickEvent event) {

            CmsConfirmRemoveDialog.this.hide();
            m_removeCallback.onFailure(null);
        }
    };

    /** 
     * Click handler for the OK button.<p>
     */
    protected ClickHandler m_onClickOk = new ClickHandler() {

        public void onClick(ClickEvent event) {

            m_removeCallback.onSuccess(Boolean.valueOf(m_deleteContent));
            CmsConfirmRemoveDialog.this.hide();
        }

    };

    /** 
     * The callback which will be called when the user has confirmed or cancelled the element removal.<p>
     */
    protected AsyncCallback<Boolean> m_removeCallback;

    /** True if the resource should be deleted. */
    boolean m_deleteContent;

    /** The Cancel button. */
    private CmsPushButton m_cancelButton;

    /** Checkbox for selecting whether the resource should be deleted. */
    private CmsCheckBox m_checkBox;

    /** True if this dialog has a checkbox for deleting the resource. */
    private boolean m_hasDeleteCheckbox;

    /** The label with the dialog text. */
    private Label m_label;

    /** The main content panel for the dialog. */
    private FlowPanel m_mainPanel = new FlowPanel();

    /** The OK button. */
    private CmsPushButton m_okButton;

    /**
     * Creates a new instance.<p>
     * 
     * @param deleteCheckbox true if the checkbox for deleting the resource should be displayed 
     * @param removeCallback the callback which should be called when the user has confirmed or cancelled the element removal 
     */
    public CmsConfirmRemoveDialog(boolean deleteCheckbox, AsyncCallback<Boolean> removeCallback) {

        m_okButton = new CmsPushButton();
        m_okButton.setText(I_MessageStrings.OK);
        m_okButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.RED);
        m_okButton.setUseMinWidth(true);
        m_cancelButton = new CmsPushButton();
        m_cancelButton.setText(I_MessageStrings.CANCEL);
        m_cancelButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.BLUE);
        m_cancelButton.setUseMinWidth(true);
        m_label = new Label(I_MessageStrings.CONFIRM_TEXT);
        m_hasDeleteCheckbox = deleteCheckbox;
        m_removeCallback = removeCallback;
        if (deleteCheckbox) {
            m_checkBox = new CmsCheckBox(I_MessageStrings.CHECKBOX_TEXT);
            m_checkBox.setChecked(true);
            m_deleteContent = true;
            m_checkBox.addValueChangeHandler(m_onCheckboxChange);
        }
        m_cancelButton.addClickHandler(m_onClickCancel);
        m_okButton.addClickHandler(m_onClickOk);
        doLayout();
        setWidth(400);
        setHeight(200);
        setModal(true);
        setGlassEnabled(true);
        setCaption(I_MessageStrings.CAPTION);
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#onPreviewNativeEvent(com.google.gwt.user.client.Event.NativePreviewEvent)
     */
    @Override
    protected void onPreviewNativeEvent(NativePreviewEvent event) {

        super.onPreviewNativeEvent(event);
        switch (event.getTypeInt()) {
            case Event.ONKEYPRESS:
                switch (event.getNativeEvent().getKeyCode()) {
                    case KeyCodes.KEY_ENTER:
                        event.cancel();
                        m_onClickOk.onClick(null);
                        break;
                    case KeyCodes.KEY_ESCAPE:
                        event.cancel();
                        m_onClickCancel.onClick(null);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    /** 
     * Lays out the dialog contents.<p>
     */
    private void doLayout() {

        setMainContent(m_mainPanel);
        m_mainPanel.add(m_label);
        if (m_hasDeleteCheckbox) {
            m_mainPanel.add(m_checkBox);
            m_checkBox.getElement().getStyle().setMarginTop(10, Unit.PX);
        }
        addButton(m_cancelButton);
        addButton(m_okButton);
    }
}

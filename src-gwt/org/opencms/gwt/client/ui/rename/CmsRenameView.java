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

package org.opencms.gwt.client.ui.rename;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.shared.CmsRenameInfoBean;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The dialog contents of the 'Rename' dialog.<p>
 */
public class CmsRenameView extends Composite {

    /** The UiBinder interface for this widget. */
    interface I_CmsRenameViewUiBinder extends UiBinder<Widget, CmsRenameView> {
        // empy UiBinder interface
    }

    /** An enum representing the widget state. */
    enum State {
        /** The validation has just failed. */
        validationFailed,

        /** The validation was triggered but hasn't finished yet. */
        validationRunning,

        /** The validation status is unknown. */
        validationUnknown
    }

    /** The UiBinder instance for this widget. */
    private static I_CmsRenameViewUiBinder uiBinder = GWT.create(I_CmsRenameViewUiBinder.class);

    /** The Cancel button. */
    @UiField
    protected CmsPushButton m_cancelButton;

    /** The container for the resource info box. */
    @UiField
    protected Panel m_infoBoxContainer;

    /** The input field for the new name. */
    @UiField
    protected CmsTextBox m_newNameInput;

    /** The label for the input box. */
    @UiField
    protected HasText m_newNameLabel;

    /** The OK button. */
    @UiField
    protected CmsPushButton m_okButton;

    /** The label for the old name. */
    @UiField
    protected HasText m_oldNameLabel;

    /** The label containing the old name. */
    @UiField
    protected HasText m_oldNameValue;

    /** The callback which is invoked when the resource has been renamed successfully. */
    AsyncCallback<String> m_renameCallback;

    /** The widget state. */
    State m_state;

    /** The structure id of the resource which is being renamed. **/
    CmsUUID m_structureId;

    /** The dialog which contains this view. */
    private CmsPopup m_dialog;

    /**
     * Creates a new instance.<p>
     *
     * @param renameInfo the information for the resource which is being renamed
     * @param renameCallback the callback which should be called when the resource has been successfully renamed
     */
    public CmsRenameView(CmsRenameInfoBean renameInfo, AsyncCallback<String> renameCallback) {

        m_structureId = renameInfo.getStructureId();
        m_renameCallback = renameCallback;
        String sitePath = renameInfo.getSitePath().replaceFirst("/$", "");
        String oldName = sitePath.substring(1 + sitePath.lastIndexOf('/'));

        initWidget(uiBinder.createAndBindUi(this));
        m_oldNameLabel.setText(CmsRenameMessages.messageOldNameLabel());
        m_newNameLabel.setText(CmsRenameMessages.messageNewNameLabel());
        m_okButton.setText(CmsRenameMessages.messageOk());
        m_okButton.setUseMinWidth(true);
        m_cancelButton.setText(CmsRenameMessages.messageCancel());
        m_cancelButton.setUseMinWidth(true);
        m_oldNameValue.setText(oldName);

        CmsListItemWidget listItemWidget = new CmsListItemWidget(renameInfo.getListInfo());
        m_infoBoxContainer.add(listItemWidget);
        m_newNameInput.addKeyPressHandler(new KeyPressHandler() {

            public void onKeyPress(KeyPressEvent event) {

                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    onClickOk(null);
                } else if (m_state == State.validationFailed) {
                    changeState(State.validationUnknown);
                }
            }
        });
    }

    /**
     * Gets the buttons which should be inserted into the parent dialog.<p>
     *
     * @return the list of buttons for the parent dialog
     */
    public List<CmsPushButton> getDialogButtons() {

        List<CmsPushButton> result = new ArrayList<CmsPushButton>();
        result.add(m_cancelButton);
        result.add(m_okButton);
        return result;
    }

    /**
     * Sets the dialog in which this view is contained.<p>
     *
     * @param popup the dialog containing this view
     */
    public void setDialog(CmsPopup popup) {

        m_dialog = popup;
    }

    /**
     * Event handler for the Cancel button.<p>
     *
     * @param e the click event
     */
    @UiHandler("m_cancelButton")
    protected void onClickCancel(ClickEvent e) {

        close();
    }

    /**
     * Event handler for the OK button.<p>
     *
     * @param e the click event
     */
    @UiHandler("m_okButton")
    protected void onClickOk(ClickEvent e) {

        changeState(State.validationRunning);
        final String newName = getValue();
        CmsRpcAction<String> validate = new CmsRpcAction<String>() {

            @Override
            public void execute() {

                start(200, false);
                CmsCoreProvider.getVfsService().renameResource(m_structureId, newName, this);
            }

            @Override
            public void onFailure(Throwable t) {

                super.onFailure(t);
                changeState(State.validationUnknown);
            }

            @Override
            protected void onResponse(String result) {

                stop(false);
                if (result == null) {
                    close();
                    if (m_renameCallback != null) {
                        m_renameCallback.onSuccess(newName);
                    }
                } else {
                    changeState(State.validationFailed);
                    setValidationError(result);
                }
            }

        };
        validate.execute();

    }

    /**
     * Changes the state of the widget.<p>
     *
     * @param state the state to change to
     */
    void changeState(State state) {

        m_state = state;
        switch (state) {
            case validationRunning:
                m_cancelButton.setEnabled(false);
                m_okButton.setEnabled(false);
                clearValidationError();
                break;
            case validationFailed:
                m_okButton.setEnabled(false);
                m_cancelButton.setEnabled(true);
                break;
            case validationUnknown:
            default:
                m_cancelButton.setEnabled(true);
                m_okButton.setEnabled(true);
                clearValidationError();
                break;

        }
    }

    /**
     * Closes the dialog containing this widget.<p>
     */
    void close() {

        m_dialog.hide();
    }

    /**
     * Gets the new name entered by the user.<p>
     *
     * @return the new name
     */
    String getValue() {

        return m_newNameInput.getText();
    }

    /**
     * Sets the validation error in the widget.<p>
     *
     * @param error the validation error message
     */
    void setValidationError(String error) {

        m_newNameInput.setErrorMessage(error);
    }

    /**
     * Clears the validation error message.<p>
     */
    private void clearValidationError() {

        m_newNameInput.setErrorMessage(null);
    }

}

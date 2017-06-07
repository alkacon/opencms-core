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
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.util.CmsMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget used to change the current user's password.<p>
 */
public class CmsChangePasswordWidget extends Composite {

    /** The uiBinder interface for this widget. */
    interface I_CmsPasswordWidgetUiBinder extends UiBinder<Widget, CmsChangePasswordWidget> {
        //empty
    }

    /** Constant for the width of the text box labels. */
    public static final String LABEL_WIDTH = "200px";

    /** The uiBinder instance for this widget. */
    private static I_CmsPasswordWidgetUiBinder uiBinder = GWT.create(I_CmsPasswordWidgetUiBinder.class);

    /** Label for displaying error messages. */
    @UiField
    protected Label m_errorLabel;

    /** Action to execute if the password was successfully changed. */
    protected Runnable m_finishAction;

    /** Input field to the new password. */
    @UiField(provided = true)
    protected CmsTextBox m_newPassword1 = new CmsTextBox(new PasswordTextBox());

    /** Input field for the new password confirmation. */
    @UiField(provided = true)
    protected CmsTextBox m_newPassword2 = new CmsTextBox(new PasswordTextBox());

    /** Label for the new password confirmation. */
    @UiField(provided = true)
    protected Label m_newPasswordConfirmLabel;

    /** Label for the new password. */
    @UiField(provided = true)
    protected Label m_newPasswordLabel;

    /** Input field for the old password. */
    @UiField(provided = true)
    protected CmsTextBox m_oldPassword = new CmsTextBox(new PasswordTextBox());

    /** Label for the old password. */
    @UiField(provided = true)
    protected Label m_oldPasswordLabel;

    /**
     * Creates a new instance.<p>
     *
     * @param finishAction the action to execute when the password is successfully changed
     */
    public CmsChangePasswordWidget(Runnable finishAction) {

        CmsMessages m = Messages.get();
        m_oldPasswordLabel = new Label(m.key(Messages.GUI_PASSWORD_OLD_0));
        m_newPasswordLabel = new Label(m.key(Messages.GUI_PASSWORD_NEW_0));
        m_newPasswordConfirmLabel = new Label(m.key(Messages.GUI_PASSWORD_CONFIRM_0));
        initWidget(uiBinder.createAndBindUi(this));
        addStyleName(CmsTextBox.CSS.highTextBoxes());
        m_finishAction = finishAction;
    }

    /**
     * Opens a popup dialog for changing the password.<p>
     */
    public static void showDialog() {

        final CmsPopup popup = new CmsPopup(
            Messages.get().key(Messages.GUI_PASSWORD_CHANGE_TITLE_1, CmsCoreProvider.get().getUserInfo().getName()));
        popup.setModal(true);
        popup.setGlassEnabled(true);
        CmsPushButton okButton = new CmsPushButton();
        okButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.RED);
        okButton.setText(Messages.get().key(Messages.GUI_OK_0));
        okButton.setUseMinWidth(true);
        CmsPushButton cancelButton = new CmsPushButton();
        cancelButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.BLUE);
        cancelButton.setText(Messages.get().key(Messages.GUI_CANCEL_0));
        cancelButton.setUseMinWidth(true);
        popup.addButton(cancelButton);
        popup.addButton(okButton);

        Runnable finishAction = new Runnable() {

            public void run() {

                popup.hide();
            }
        };
        final CmsChangePasswordWidget passwordWidget = new CmsChangePasswordWidget(finishAction);
        okButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent e) {

                passwordWidget.changePassword();
            }
        });

        cancelButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent e) {

                popup.hide();
            }
        });
        popup.setMainContent(passwordWidget);
        popup.center();

    }

    /**
     * Tries to change the password using the data entered by the user.<p>
     */
    public void changePassword() {

        final String oldPassword = m_oldPassword.getFormValueAsString();
        final String newPassword1 = m_newPassword1.getFormValueAsString();
        final String newPassword2 = m_newPassword2.getFormValueAsString();

        CmsRpcAction<String> action = new CmsRpcAction<String>() {

            @Override
            public void execute() {

                start(0, true);
                CmsCoreProvider.getService().changePassword(oldPassword, newPassword1, newPassword2, this);
            }

            @Override
            public void onResponse(String result) {

                stop(false);
                if (result == null) {
                    m_finishAction.run();
                } else {
                    showError(result);
                }
            }
        };
        action.execute();
    }

    /**
     * Displays an error message.<p>
     *
     * @param error the error message to display
     */
    protected void showError(String error) {

        if (null == error) {
            m_errorLabel.setText("");
        } else {
            m_errorLabel.setText(error);
        }
    }

}

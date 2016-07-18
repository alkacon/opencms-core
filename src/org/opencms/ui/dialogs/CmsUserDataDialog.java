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

package org.opencms.ui.dialogs;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsOkCancelActionHandler;
import org.opencms.ui.login.CmsChangePasswordDialog;
import org.opencms.ui.util.CmsNullToEmptyConverter;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsAccountInfo;
import org.opencms.workplace.CmsAccountInfo.Field;

import java.util.Collections;
import java.util.Locale;

import org.apache.commons.beanutils.PropertyUtilsBean;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * Dialog to edit the user data.<p>
 */
public class CmsUserDataDialog extends CmsBasicDialog implements I_CmsHasTitle {

    /**
     * Validator employing the configured OpenCms validation handler.<p>
     */
    private static class FieldValidator extends AbstractStringValidator {

        /** The serial version id. */
        private static final long serialVersionUID = 4432834072807177046L;

        /** The field to validate. */
        private Field m_field;

        /**
         * Constructor.<p>
         *
         * @param field the field to validate
         */
        public FieldValidator(Field field) {
            super(null);
            m_field = field;
        }

        /**
         * @see com.vaadin.data.validator.AbstractValidator#isValidValue(java.lang.Object)
         */
        @SuppressWarnings("incomplete-switch")
        @Override
        protected boolean isValidValue(String value) {

            boolean result = true;

            try {
                switch (m_field) {
                    case email:
                        OpenCms.getValidationHandler().checkEmail(value);
                        break;
                    case firstname:
                        OpenCms.getValidationHandler().checkFirstname(value);
                        break;
                    case lastname:
                        OpenCms.getValidationHandler().checkLastname(value);
                        break;
                    case zipcode:
                        OpenCms.getValidationHandler().checkZipCode(value);
                        break;

                }
            } catch (CmsIllegalArgumentException e) {
                result = false;
                setErrorMessage(e.getLocalizedMessage(UI.getCurrent().getLocale()));
            }

            return result;
        }
    }

    /** The embedded dialog id. */
    public static final String DIALOG_ID = "edituserdata";

    /** The serial version id. */
    private static final long serialVersionUID = 8907786853232656944L;

    /** The field binder. */
    private FieldGroup m_binder;

    /** The Cancel button. */
    private Button m_cancelButton;

    /** The dialog context. */
    private I_CmsDialogContext m_context;

    /** The form layout. */
    private FormLayout m_form;

    /** The property item. */
    private PropertysetItem m_infos;

    /** The OK  button. */
    private Button m_okButton;

    /** The change password button. */
    private Button m_changePassword;

    /** The edited user. */
    private CmsUser m_user;

    /** Displays the user icon and name. */
    private Label m_userInfo;

    /**
     * Creates a new instance.<p>
     *
     * @param context the dialog context
     */
    public CmsUserDataDialog(I_CmsDialogContext context) {
        m_context = context;
        CmsObject cms = context.getCms();
        m_user = cms.getRequestContext().getCurrentUser();
        if (m_user.isManaged()) {
            throw new CmsRuntimeException(
                Messages.get().container(Messages.ERR_USER_NOT_SELF_MANAGED_1, m_user.getName()));
        }
        CmsVaadinUtils.readAndLocalizeDesign(
            this,
            OpenCms.getWorkplaceManager().getMessages(A_CmsUI.get().getLocale()),
            null);
        m_userInfo.setContentMode(ContentMode.HTML);
        m_userInfo.setValue(
            "<img src=\""
                + OpenCms.getWorkplaceAppManager().getUserIconHelper().getSmallIconPath(cms, m_user)
                + "\" style=\"vertical-align:middle; margin: -4px 10px 0 0;\" />"
                + m_user.getName());

        initFields();

        m_cancelButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                cancel();
            }

        });

        m_okButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                submit();
            }
        });
        m_changePassword.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                openChangePassword();
            }
        });

        setActionHandler(new CmsOkCancelActionHandler() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void cancel() {

                CmsUserDataDialog.this.cancel();
            }

            @Override
            protected void ok() {

                submit();
            }
        });
    }

    /**
     * Creates a new instance.<p>
     *
     * @param context the dialog context
     * @param forcedCheck <code>true</code> in case of a forced user data check after login
     */
    public CmsUserDataDialog(I_CmsDialogContext context, boolean forcedCheck) {
        this(context);
        if (forcedCheck) {
            ((VerticalLayout)m_userInfo.getParent()).addComponent(
                new Label(CmsVaadinUtils.getMessageText(org.opencms.ui.dialogs.Messages.GUI_USER_DATA_CHECK_INFO_0)),
                1);

            m_cancelButton.setVisible(false);
            m_changePassword.setVisible(false);
        }
    }

    /**
     * @see org.opencms.ui.dialogs.I_CmsHasTitle#getTitle(java.util.Locale)
     */
    public String getTitle(Locale locale) {

        return org.opencms.ui.components.Messages.get().getBundle(locale).key(
            org.opencms.ui.components.Messages.GUI_USER_EDIT_0);
    }

    /**
     * Cancels the dialog.<p>
     */
    void cancel() {

        m_context.finish(Collections.<CmsUUID> emptyList());
    }

    /**
     * Opens the change password dialog.<p>
     */
    void openChangePassword() {

        if (m_context instanceof CmsEmbeddedDialogContext) {
            ((CmsEmbeddedDialogContext)m_context).closeWindow(true);
        } else {
            m_context.finish(Collections.<CmsUUID> emptyList());
        }
        m_context.start(
            Messages.get().getBundle(A_CmsUI.get().getLocale()).key(Messages.GUI_PWCHANGE_HEADER_0)
                + m_user.getSimpleName(),
            new CmsChangePasswordDialog(m_context));
    }

    /**
     * Submits the dialog.<p>
     */
    void submit() {

        try {
            // Special user info attributes may have been set since the time the dialog was instantiated,
            // and we don't want to overwrite them, so we read the user again.
            m_user = m_context.getCms().readUser(m_user.getId());
            if (isValid()) {
                m_binder.commit();
                PropertyUtilsBean propUtils = new PropertyUtilsBean();
                for (CmsAccountInfo info : OpenCms.getWorkplaceManager().getAccountInfos()) {
                    if (info.isEditable()) {
                        if (info.isAdditionalInfo()) {
                            m_user.setAdditionalInfo(info.getAddInfoKey(), m_infos.getItemProperty(info).getValue());
                        } else {
                            propUtils.setProperty(
                                m_user,
                                info.getField().name(),
                                m_infos.getItemProperty(info).getValue());
                        }
                    }
                }
                m_context.getCms().writeUser(m_user);
                m_context.finish(Collections.<CmsUUID> emptyList());
            }
        } catch (Exception e) {
            m_context.error(e);
        }
    }

    /**
     * Builds the text field for the given property.<p>
     *
     * @param label the field label
     * @param info the property name
     *
     * @return the field
     */
    private TextField buildField(String label, CmsAccountInfo info) {

        TextField field = (TextField)m_binder.buildAndBind(label, info);
        field.setConverter(new CmsNullToEmptyConverter());
        field.setWidth("100%");
        field.setEnabled(info.isEditable());
        if (info.isEditable()) {
            field.addValidator(new FieldValidator(info.getField()));
        }
        field.setImmediate(true);
        return field;
    }

    /**
     * Returns the field label.<p>
     *
     * @param info the info
     *
     * @return the label
     */
    private String getLabel(CmsAccountInfo info) {

        if (info.isAdditionalInfo()) {
            String label = CmsVaadinUtils.getMessageText("GUI_USER_DATA_" + info.getAddInfoKey().toUpperCase() + "_0");
            if (CmsMessages.isUnknownKey(label)) {
                return info.getAddInfoKey();
            } else {
                return label;
            }
        } else {
            return CmsVaadinUtils.getMessageText("GUI_USER_DATA_" + info.getField().name().toUpperCase() + "_0");
        }
    }

    /**
     * Initializes the form fields.<p>
     */
    private void initFields() {

        m_infos = new PropertysetItem();
        for (CmsAccountInfo info : OpenCms.getWorkplaceManager().getAccountInfos()) {
            String value = info.getValue(m_user);
            if (value == null) {
                value = "";
            }
            m_infos.addItemProperty(info, new ObjectProperty<String>(value));
        }

        m_binder = new FieldGroup(m_infos);
        for (CmsAccountInfo info : OpenCms.getWorkplaceManager().getAccountInfos()) {
            m_form.addComponent(buildField(getLabel(info), info));
        }
    }

    /**
     * Returns whether the form fields are valid.<p>
     *
     * @return <code>true</code> if the form fields are valid
     */
    private boolean isValid() {

        boolean valid = true;
        for (Component comp : m_form) {
            if (comp instanceof TextField) {
                valid = valid && ((TextField)comp).isValid();
            }
        }
        return valid;
    }
}

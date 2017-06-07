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

package org.opencms.ui.login;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsPasswordHandler;
import org.opencms.security.I_CmsPasswordSecurityEvaluator;
import org.opencms.security.I_CmsPasswordSecurityEvaluator.SecurityLevel;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsOkCancelActionHandler;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceLoginHandler;

import java.util.Collections;
import java.util.Locale;

import org.apache.commons.logging.Log;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.UI;

/**
 * Dialog used to change the password.<p>
 */
public class CmsChangePasswordDialog extends CmsBasicDialog {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsChangePasswordDialog.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The password form. */
    protected CmsPasswordForm m_form;

    /** The CMS context. */
    CmsObject m_cms;

    /** The locale. */
    Locale m_locale;

    /** The user. */
    CmsUser m_user;

    /** The cancel button. */
    private Button m_cancelButton;

    /** The dialog context. */
    private I_CmsDialogContext m_context;

    /** The button to change the password. */
    private Button m_passwordChangeButton;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context
     * @param user the user
     * @param locale the locale
     */
    public CmsChangePasswordDialog(CmsObject cms, CmsUser user, Locale locale) {
        super();
        m_locale = locale;
        m_cms = cms;
        m_user = user;
        if (m_user.isManaged()) {
            throw new CmsRuntimeException(
                Messages.get().container(Messages.ERR_USER_NOT_SELF_MANAGED_1, m_user.getName()));
        }

        m_form = new CmsPasswordForm(m_locale);
        setContent(m_form);
        if (OpenCms.getPasswordHandler() instanceof I_CmsPasswordSecurityEvaluator) {
            m_form.setSecurityHint(
                ((I_CmsPasswordSecurityEvaluator)OpenCms.getPasswordHandler()).getPasswordSecurityHint(m_locale));
        }
        m_passwordChangeButton = new Button(CmsVaadinUtils.getMessageText(Messages.GUI_CHANGE_PASSWORD_BUTTON_0));
        addButton(m_passwordChangeButton);
        m_passwordChangeButton.addClickListener(new ClickListener() {

            /** Serial versino id. */
            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                submit();
            }
        });

        m_cancelButton = new Button(CmsVaadinUtils.messageCancel());
        addButton(m_cancelButton);
        m_cancelButton.addClickListener(new ClickListener() {

            /** Serial versino id. */
            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                close();
            }
        });
        m_cancelButton.setVisible(false);
        m_form.getOldPasswordField().setImmediate(true);
        m_form.getPassword1Field().setImmediate(true);
        m_form.getPassword2Field().setImmediate(true);

        m_form.getOldPasswordField().addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                clearOldPasswordError();
            }
        });

        m_form.getPassword1Field().addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            public void textChange(TextChangeEvent event) {

                checkSecurity(event.getText());
            }
        });
        m_form.getPassword2Field().addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            public void textChange(TextChangeEvent event) {

                checkPasswordMatch(event.getText());
            }
        });
    }

    /**
     * Creates a new instance.<p>
     *
     * @param context the dialog context
     */
    public CmsChangePasswordDialog(I_CmsDialogContext context) {
        this(context.getCms(), context.getCms().getRequestContext().getCurrentUser(), UI.getCurrent().getLocale());
        m_context = context;
        m_cancelButton.setVisible(true);
        setActionHandler(new CmsOkCancelActionHandler() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void cancel() {

                close();
            }

            @Override
            protected void ok() {

                submit();
            }
        });
    }

    /**
     * Displays some additional text.<p>
     *
     * @param text the text to display
     */
    public void setAdditionalMessage(String text) {

        m_form.setAdditionalText(text);
    }

    /**
     * Checks whether the passwords match.<p>
     *
     * @param password2 the password 2 value
     */
    void checkPasswordMatch(String password2) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(password2)) {
            showPasswordMatchError(!password2.equals(m_form.getPassword1()));
        }
    }

    /**
     * Checks the security level of the given password.<p>
     *
     * @param password the password
     */
    void checkSecurity(String password) {

        I_CmsPasswordHandler handler = OpenCms.getPasswordHandler();
        try {
            handler.validatePassword(password);
            if (handler instanceof I_CmsPasswordSecurityEvaluator) {
                SecurityLevel level = ((I_CmsPasswordSecurityEvaluator)handler).evaluatePasswordSecurity(password);
                m_form.setErrorPassword1(null, OpenCmsTheme.SECURITY + "-" + level.name());
            } else {
                m_form.setErrorPassword1(null, OpenCmsTheme.SECURITY_STRONG);
            }
        } catch (CmsSecurityException e) {
            m_form.setErrorPassword1(new UserError(e.getLocalizedMessage(m_locale)), OpenCmsTheme.SECURITY_INVALID);
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_form.getPassword2())) {
            showPasswordMatchError(!password.equals(m_form.getPassword2()));
        }
    }

    /**
     * Clears the wrong old password error.<p>
     */
    void clearOldPasswordError() {

        m_form.setErrorOldPassword(null, null);
    }

    /**
     * Closes the dialog.<p>
     */
    void close() {

        if (m_context != null) {
            m_context.finish(Collections.<CmsUUID> emptyList());
        }
    }

    /**
     * Shows or hides the not matching passwords error.<p>
     *
     * @param show <code>true</code> to show the error
     */
    void showPasswordMatchError(boolean show) {

        if (show) {
            m_form.setErrorPassword2(
                new UserError(Messages.get().getBundle(m_locale).key(Messages.GUI_PWCHANGE_PASSWORD_MISMATCH_0)),
                OpenCmsTheme.SECURITY_INVALID);
        } else {
            m_form.setErrorPassword2(null, OpenCmsTheme.SECURITY_STRONG);
        }
    }

    /**
     * Submits the password change.<p>
     */
    void submit() {

        String password1 = m_form.getPassword1();
        String password2 = m_form.getPassword2();

        if (validatePasswords(password1, password2)) {
            String oldPassword = m_form.getOldPassword();
            boolean error = false;

            if (oldPassword.equals(password1)) {
                m_form.setErrorPassword1(
                    new UserError(
                        Messages.get().getBundle(m_locale).key(Messages.GUI_PWCHANGE_DIFFERENT_PASSWORD_REQUIRED_0)),
                    OpenCmsTheme.SECURITY_INVALID);
                error = true;
            } else {
                try {
                    m_cms.setPassword(m_user.getName(), oldPassword, password1);
                } catch (CmsException e) {
                    m_form.setErrorOldPassword(
                        new UserError(e.getLocalizedMessage(m_locale)),
                        OpenCmsTheme.SECURITY_INVALID);
                    error = true;
                    LOG.debug(e.getLocalizedMessage(), e);
                }
            }

            if (!error) {
                if (m_context != null) {
                    close();
                } else {
                    // this will be the case for forced password changes after login
                    CmsVaadinUtils.showAlert(
                        Messages.get().getBundle(m_locale).key(Messages.GUI_PWCHANGE_SUCCESS_HEADER_0),
                        Messages.get().getBundle(m_locale).key(Messages.GUI_PWCHANGE_GUI_PWCHANGE_SUCCESS_CONTENT_0),
                        new Runnable() {

                            public void run() {

                                A_CmsUI.get().getPage().setLocation(
                                    OpenCms.getLinkManager().substituteLinkForUnknownTarget(
                                        CmsLoginUI.m_adminCms,
                                        CmsWorkplaceLoginHandler.LOGIN_HANDLER,
                                        false));
                            }

                        });
                }
            }
        }
    }

    /**
     * Validates the passwords, checking if they match and fulfill the requirements of the password handler.<p>
     * Will show the appropriate errors if necessary.<p>
     *
     * @param password1 password 1
     * @param password2 password 2
     *
     * @return <code>true</code> if valid
     */
    boolean validatePasswords(String password1, String password2) {

        if (!password1.equals(password2)) {
            showPasswordMatchError(true);
            return false;
        }
        showPasswordMatchError(false);
        try {
            OpenCms.getPasswordHandler().validatePassword(password1);
            m_form.getPassword1Field().setComponentError(null);
            return true;
        } catch (CmsException e) {
            m_form.setErrorPassword1(new UserError(e.getLocalizedMessage(m_locale)), OpenCmsTheme.SECURITY_INVALID);
            return false;
        }
    }
}

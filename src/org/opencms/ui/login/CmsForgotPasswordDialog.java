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

package org.opencms.ui.login;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsHasButtons;
import org.opencms.ui.Messages;
import org.opencms.util.CmsStringUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.mail.EmailException;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * Dialog to request a password reset link if you forgot your passsword.<p>
 */
public class CmsForgotPasswordDialog extends VerticalLayout implements I_CmsHasButtons {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsForgotPasswordDialog.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** Field for the email address. */
    protected TextField m_emailField;

    /** The user field. */
    protected TextField m_userField;

    /** The OU selector. */
    protected CmsLoginOuSelector m_ouSelect;

    /** Button to request the mail with the password reset link .*/
    protected Button m_mailButton;

    /** Button to cancel. */
    protected Button m_cancelButton;

    /**
     * Creates a new instance.<p>
     */
    public CmsForgotPasswordDialog() {
        Locale locale = A_CmsUI.get().getLocale();
        CmsVaadinUtils.readAndLocalizeDesign(this, OpenCms.getWorkplaceManager().getMessages(locale), null);
        List<CmsOrganizationalUnit> ouList = CmsLoginHelper.getOrgUnitsForLoginDialog(A_CmsUI.getCmsObject(), null);
        m_ouSelect.initOrgUnits(ouList);
        String notEmptyMessage = CmsVaadinUtils.getMessageText(Messages.GUI_VALIDATION_FIELD_EMPTY_0);
        m_userField.setRequired(true);
        m_userField.setRequiredError(notEmptyMessage);
        m_emailField.setRequired(true);
        m_emailField.setRequiredError(notEmptyMessage);
        m_emailField.addValidator(
            new EmailValidator(
                Messages.get().getBundle(A_CmsUI.get().getLocale()).key(Messages.GUI_PWCHANGE_INVALID_EMAIL_0)));
        m_cancelButton.addClickListener(new Button.ClickListener() {

            /**Serial version id. */
            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                cancel();
            }
        });

        m_mailButton.addClickListener(new Button.ClickListener() {

            /** Serial version id. */
            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                boolean valid = true;
                for (AbstractField<?> field : Arrays.asList(m_userField, m_emailField)) {
                    try {
                        field.validate();
                    } catch (InvalidValueException e) {
                        valid = false;
                    }
                }
                if (!valid) {
                    return;
                }
                String selectedOu = m_ouSelect.getValue();
                selectedOu = (selectedOu != null) ? selectedOu : "";
                String fullName = CmsStringUtil.joinPaths(selectedOu, m_userField.getValue());
                if (sendPasswordResetLink(CmsLoginUI.m_adminCms, fullName, m_emailField.getValue())) {

                    // Since we need to actually go to a different page here, we can't use a Vaadin notification,
                    // because we don't get notified on the server when the user clicks it.
                    CmsVaadinUtils.showAlert(
                        Messages.get().getBundle(A_CmsUI.get().getLocale()).key(
                            Messages.GUI_PWCHANGE_MAILSENT_HEADER_0),
                        Messages.get().getBundle(A_CmsUI.get().getLocale()).key(
                            Messages.GUI_PWCHANGE_MAILSENT_MESSAGE_0),
                        new Runnable() {

                        public void run() {

                            A_CmsUI.get().getPage().setLocation(
                                OpenCms.getLinkManager().substituteLinkForUnknownTarget(
                                    CmsLoginUI.m_adminCms,
                                    "/system/login", //$NON-NLS-1$
                                    false));

                        }

                    });
                }
            }
        });

    }

    /**
     * Tries to find a user with the given email address, and if one is found, sends a mail with the password reset link to them.<p>
     *
     * @param cms the CMS Context
     * @param fullUserName the full user name including OU
     * @param email the email address entered by the user
     * @return true if the mail could be sent
     */
    public static boolean sendPasswordResetLink(CmsObject cms, String fullUserName, String email) {

        LOG.info("Trying to find user for email " + email); //$NON-NLS-1$
        email = email.trim();

        try {
            CmsUser foundUser = null;
            try {
                foundUser = cms.readUser(fullUserName);
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            if ((foundUser == null)
                || CmsStringUtil.isEmptyOrWhitespaceOnly(email)
                || !email.equals(foundUser.getEmail())) {
                Notification.show(
                    CmsVaadinUtils.getMessageText(Messages.GUI_PWCHANGE_EMAIL_MISMATCH_0),
                    Type.ERROR_MESSAGE);
                return false;
            }
            String token = CmsTokenValidator.createToken(cms, foundUser);
            String link = OpenCms.getLinkManager().getWorkplaceLink(cms, "/system/login", false) + "?at=" + token; //$NON-NLS-1$ //$NON-NLS-2$
            LOG.info("Sending password reset link to user " + foundUser.getName() + ": " + link); //$NON-NLS-1$ //$NON-NLS-2$
            CmsPasswordChangeNotification notification = new CmsPasswordChangeNotification(cms, foundUser, link);
            try {
                notification.send();
            } catch (EmailException e) {
                Notification.show(
                    Messages.get().getBundle(A_CmsUI.get().getLocale()).key(Messages.GUI_PWCHANGE_MAIL_SEND_ERROR_0),
                    Type.ERROR_MESSAGE);
                LOG.error(e.getLocalizedMessage(), e);
                return false;
            }

        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            Notification.show(e.getLocalizedMessage(), Type.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Cancels the dialog.<p>
     */
    public void cancel() {

        CmsObject cms = A_CmsUI.getCmsObject();
        String link = OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, "/system/login", false);
        A_CmsUI.get().getPage().setLocation(link);
    }

    /**
     * @see org.opencms.ui.I_CmsHasButtons#getButtons()
     */
    public List<Button> getButtons() {

        return Arrays.asList(m_mailButton, m_cancelButton);
    }

}

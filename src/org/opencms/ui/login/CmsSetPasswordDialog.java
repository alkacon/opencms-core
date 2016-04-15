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
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsPasswordHandler;
import org.opencms.security.I_CmsPasswordSecurityEvaluator;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsSecurityIndicator;
import org.opencms.workplace.CmsWorkplaceLoginHandler;

import java.util.Locale;

import org.apache.commons.logging.Log;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;

/**
 * Dialog used to change the password.<p>
 */
public class CmsSetPasswordDialog extends CmsBasicDialog {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSetPasswordDialog.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The CMS context. */
    protected CmsObject m_cms;

    /** The locale. */
    protected Locale m_locale;

    /** The button to change the password. */
    protected Button m_passwordChangeButton;

    /** First password field. */
    protected PasswordField m_passwordField1;

    /** Second password field. */
    protected PasswordField m_passwordField2;

    /** The user. */
    protected CmsUser m_user;

    /** Label to display the security hint. */
    private Label m_securityHint;

    /** The security level indicator. */
    private CmsSecurityIndicator m_securityIndicator;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context
     * @param user the user
     * @param locale the locale
     */
    public CmsSetPasswordDialog(final CmsObject cms, CmsUser user, Locale locale) {
        CmsVaadinUtils.readAndLocalizeDesign(this, OpenCms.getWorkplaceManager().getMessages(locale), null);
        m_locale = locale;
        m_cms = cms;
        m_user = user;

        if (OpenCms.getPasswordHandler() instanceof I_CmsPasswordSecurityEvaluator) {
            m_securityHint.setValue(
                ((I_CmsPasswordSecurityEvaluator)OpenCms.getPasswordHandler()).getPasswordSecurityHint(m_locale));
        } else {
            m_securityHint.setVisible(false);
        }

        m_passwordChangeButton.addClickListener(new ClickListener() {

            /** Serial versino id. */
            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                submit();
            }
        });

        m_passwordField1.addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            public void textChange(TextChangeEvent event) {

                checkSecurity(event.getText());
            }
        });
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
            m_passwordField1.setComponentError(null);
            if (handler instanceof I_CmsPasswordSecurityEvaluator) {
                float level = ((I_CmsPasswordSecurityEvaluator)handler).evaluatePasswordSecurity(password);
                m_securityIndicator.setValue(Float.valueOf(level));
            } else {
                m_securityIndicator.setValue(Float.valueOf(1));
            }
        } catch (CmsSecurityException e) {
            m_passwordField1.setComponentError(new UserError(e.getLocalizedMessage(m_locale)));
            m_securityIndicator.setValue(Float.valueOf(0));
        }
    }

    /**
     * Submits the password.<p>
     */
    void submit() {

        if ((m_user != null) && m_user.isManaged()) {
            return;
        }
        m_passwordField1.setComponentError(null);
        m_passwordField2.setComponentError(null);
        String password1 = m_passwordField1.getValue();
        String password2 = m_passwordField2.getValue();
        String error = null;
        if (password1.equals(password2)) {
            try {
                m_cms.setPassword(m_user.getName(), password1);
                CmsTokenValidator.clearToken(CmsLoginUI.m_adminCms, m_user);
            } catch (CmsException e) {
                error = e.getLocalizedMessage(m_locale);
                LOG.debug(e.getLocalizedMessage(), e);
            } catch (Exception e) {
                error = e.getLocalizedMessage();
                LOG.error(e.getLocalizedMessage(), e);
            }

        } else {
            error = Messages.get().getBundle(A_CmsUI.get().getLocale()).key(Messages.GUI_PWCHANGE_PASSWORD_MISMATCH_0);
        }
        if (error != null) {
            m_passwordField1.setComponentError(new UserError(error));
        } else {
            CmsVaadinUtils.showAlert(
                Messages.get().getBundle(A_CmsUI.get().getLocale()).key(Messages.GUI_PWCHANGE_SUCCESS_HEADER_0),
                Messages.get().getBundle(A_CmsUI.get().getLocale()).key(
                    Messages.GUI_PWCHANGE_GUI_PWCHANGE_SUCCESS_CONTENT_0),
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

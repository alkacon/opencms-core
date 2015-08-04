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
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.Messages;

import java.util.Locale;

import org.apache.commons.logging.Log;

import com.vaadin.server.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.VerticalLayout;

/**
 * Dialog used to change the password.<p>
 */
public class CmsChangePasswordDialog extends VerticalLayout {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsChangePasswordDialog.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The CMS context. */
    protected CmsObject m_cms;

    /** The locale. */
    protected Locale m_locale;

    /** The main panel. */
    protected Panel m_mainPanel;

    /** Field for the old password. */
    protected PasswordField m_oldPasswordField;

    /** The button to change the password. */
    protected Button m_passwordChangeButton;

    /** First password field. */
    protected PasswordField m_passwordField1;

    /** Second password field. */
    protected PasswordField m_passwordField2;

    /** The user. */
    protected CmsUser m_user;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the CMS context
     * @param user the user
     * @param locale the locale
     */
    public CmsChangePasswordDialog(final CmsObject cms, CmsUser user, Locale locale) {
        CmsVaadinUtils.readAndLocalizeDesign(this, OpenCms.getWorkplaceManager().getMessages(locale), null);
        m_locale = locale;
        m_cms = cms;
        m_user = user;
        m_mainPanel.setCaption(
            Messages.get().getBundle(A_CmsUI.get().getLocale()).key(Messages.GUI_PWCHANGE_HEADER_0) + user.getName());

        m_passwordChangeButton.addClickListener(new ClickListener() {

            /** Serial versino id. */
            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void buttonClick(ClickEvent event) {

                m_oldPasswordField.setComponentError(null);
                m_passwordField1.setComponentError(null);
                m_passwordField2.setComponentError(null);
                String password1 = m_passwordField1.getValue();
                String password2 = m_passwordField2.getValue();
                String oldPassword = m_oldPasswordField.getValue();
                String error = null;

                if (password1.equals(password2)) {

                    if (oldPassword.equals(password1)) {
                        error = Messages.get().getBundle(A_CmsUI.get().getLocale()).key(
                            Messages.GUI_PWCHANGE_DIFFERENT_PASSWORD_REQUIRED_0);
                    } else {
                        try {
                            m_cms.setPassword(m_user.getName(), oldPassword, password1);
                        } catch (CmsException e) {
                            error = e.getLocalizedMessage(m_locale);
                            LOG.debug(e.getLocalizedMessage(), e);
                        } catch (Exception e) {
                            error = e.getLocalizedMessage();
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }

                } else {
                    error = Messages.get().getBundle(A_CmsUI.get().getLocale()).key(
                        Messages.GUI_PWCHANGE_PASSWORD_MISMATCH_0);
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
                                OpenCms.getLinkManager().getWorkplaceLink(
                                    CmsLoginUI.m_adminCms,
                                    "/system/login", //$NON-NLS-1$
                                    false));
                        }

                    });

                }

            }
        });

    }

}

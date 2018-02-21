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

package org.opencms.ui.dialogs;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsResourceBundleLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.Messages;
import org.opencms.ui.apps.user.CmsAccountsApp;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsOkCancelActionHandler;
import org.opencms.ui.components.CmsUserDataFormLayout;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.v7.ui.Label;

/**
 * Dialog to edit the user data.<p>
 */
public class CmsUserDataDialog extends CmsBasicDialog implements I_CmsHasTitle {

    /** The embedded dialog id. */
    public static final String DIALOG_ID = "edituserdata";

    /** The serial version id. */
    private static final long serialVersionUID = 8907786853232656944L;

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUserDataDialog.class);

    /** The Cancel button. */
    private Button m_cancelButton;

    /** The dialog context. */
    I_CmsDialogContext m_context;

    /** The form layout. */
    private CmsUserDataFormLayout m_form;

    /** The OK  button. */
    private Button m_okButton;

    /** The edited user. */
    CmsUser m_user;

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

        displayResourceInfoDirectly(Collections.singletonList(CmsAccountsApp.getPrincipalInfo(m_user)));

        m_form.initFields(m_user);

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
            addComponent(new Label(getUserDataCheckMessage()), 0);

            m_cancelButton.setVisible(false);
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
     * Returns the message to be displayed for the user data check dialog.<p>
     *
     * @return the message to display
     */
    protected String getUserDataCheckMessage() {

        ResourceBundle bundle = null;
        try {
            bundle = CmsResourceBundleLoader.getBundle("org.opencms.userdatacheck.custom", A_CmsUI.get().getLocale());
            return bundle.getString("userdatacheck.text");
        } catch (MissingResourceException e) {
            return CmsVaadinUtils.getMessageText(org.opencms.ui.dialogs.Messages.GUI_USER_DATA_CHECK_INFO_0);
        }
    }

    /**
     * Cancels the dialog.<p>
     */
    void cancel() {

        m_context.finish(Collections.<CmsUUID> emptyList());
        m_context.updateUserInfo();
    }

    /**
     * Submits the dialog.<p>
     */
    void submit() {

        try {
            // Special user info attributes may have been set since the time the dialog was instantiated,
            // and we don't want to overwrite them, so we read the user again.
            m_user = m_context.getCms().readUser(m_user.getId());
            m_form.submit(m_user, m_context.getCms(), new Runnable() {

                public void run() {

                    try {
                        m_context.getCms().writeUser(m_user);
                        m_context.finish(Collections.<CmsUUID> emptyList());
                        m_context.updateUserInfo();
                    } catch (CmsException e) {
                        //
                    }
                }
            });
        } catch (CmsException e) {
            LOG.error("Unable to read user", e);
        }
    }

    /**
     * Updates the user info.<p>
     */
    void updateUserInfo() {

        try {
            m_user = m_context.getCms().readUser(m_user.getId());
            displayResourceInfoDirectly(Collections.singletonList(CmsAccountsApp.getPrincipalInfo(m_user)));
        } catch (CmsException e) {
            LOG.error("Error updating user info.", e);
        }
    }
}

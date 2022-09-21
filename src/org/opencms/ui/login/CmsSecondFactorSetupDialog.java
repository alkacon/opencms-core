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

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.twofactor.CmsSecondFactorInfo;
import org.opencms.security.twofactor.CmsSecondFactorSetupInfo;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.Messages;
import org.opencms.ui.apps.user.CmsAccountsApp;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.ui.login.CmsLoginController.LoginContext;
import org.opencms.ui.login.CmsLoginController.LoginContinuation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

import org.apache.commons.logging.Log;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

/**
 * Dialog used to set up two-factor authentication for a user.
 *
 * <p>The dialog contains both a QR code used to share a secret code with the user that they can scan with an authenticator app,
 * as well as an input field to enter a verification code generated using that app. Having the user enter a verification code in the
 * setup phase proves that they have actually added the secret to their authenticator app, i.e. not just clicked OK on a dialog they do
 * not understand.
 */
public class CmsSecondFactorSetupDialog extends CmsBasicDialog {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSecondFactorSetupDialog.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The login context object. */
    private LoginContext m_context;

    /** The handler to call when we can continue with the login process. */
    private LoginContinuation m_continuation;

    /** The description label. */
    private Label m_description;

    /** The OK button. */
    private Button m_okButton;

    /** The image used to display the QR code. */
    private Image m_qrCodeImage;

    /** The shared secret. */
    private String m_secret;

    /** The label to display the shared secret. */
    private Label m_secretLabel;

    /** The text input for entering the verification code. */
    private TextField m_verification;

    /**
     * Creates a new instance.
     *
     * @param context the login context
     * @param continuation the handler to call to proceed with the login process
     */
    public CmsSecondFactorSetupDialog(
        CmsLoginController.LoginContext context,
        CmsLoginController.LoginContinuation continuation) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), new HashMap<>());
        CmsResourceInfo userInfo = CmsAccountsApp.getPrincipalInfo(context.getUser());
        userInfo.setTopLineText(context.getUser().getFullName());
        displayResourceInfoDirectly(Collections.singletonList(userInfo));
        setWidth("800px");
        m_context = context;
        m_continuation = continuation;
        CmsSecondFactorSetupInfo info = OpenCms.getTwoFactorAuthenticationHandler().generateSetupInfo(
            context.getUser());
        Locale locale = A_CmsUI.get().getLocale();
        String specialDescription = OpenCms.getTwoFactorAuthenticationHandler().getSetupMessage(locale);
        if (specialDescription != null) {
            m_description.setValue(specialDescription);
        }

        m_qrCodeImage.setSource(new ExternalResource(info.getQrCodeImageUrl()));
        m_secret = info.getSecret();
        m_secretLabel.setValue(info.getSecret());
        m_okButton.addClickListener(event -> submit());
        m_verification.addStyleName(CmsSecondFactorDialog.CLASS_VERIFICATION_CODE_FIELD);
        m_verification.addShortcutListener(new ShortcutListener(null, KeyCode.ENTER, null) {

            private static final long serialVersionUID = 1L;

            @Override
            public void handleAction(Object sender, Object target) {

                submit();
            }
        });
        addAttachListener(event -> {
            m_verification.focus();
            CmsSecondFactorDialog.initVerificationField();

        });
    }

    /**
     * Executed when the user clicks OK.
     */
    protected void submit() {

        m_verification.setComponentError(null);
        String verificationCode = m_verification.getValue().trim();
        CmsSecondFactorInfo secondFactorInfo = new CmsSecondFactorInfo(m_secret, verificationCode);
        if (!OpenCms.getTwoFactorAuthenticationHandler().verifySecondFactorSetup(secondFactorInfo)) {
            String message = CmsVaadinUtils.getMessageText(Messages.GUI_LOGIN_2FA_SETUP_INVALID_CODE_0);
            m_verification.setComponentError(new UserError(message));
            return;
        }
        m_context.setSecondFactorInfo(secondFactorInfo);
        CmsVaadinUtils.closeWindow(this);
        try {
            m_continuation.continueLogin(m_context);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

    }
}

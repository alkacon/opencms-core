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

import org.opencms.file.CmsUser;
import org.opencms.main.CmsLog;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.Messages;
import org.opencms.ui.apps.user.CmsAccountsApp;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Consumer;

import org.apache.commons.logging.Log;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.TextField;

/**
 * Dialog used to ask the user for a verification code generated from his second factor, using an authenticator app.
 */
public class CmsSecondFactorDialog extends CmsBasicDialog {

    public static final String CLASS_VERIFICATION_CODE_FIELD = "o-verification-code-field";

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSecondFactorDialog.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The OK button. */
    private Button m_okButton;

    /** The field for entering the code. */
    private TextField m_verification;

    /** The handler to which to pass the code entered by the user. */
    private Consumer<String> m_verificationCodeHandler;

    /**
     * Creates a new instance.
     *
     * @param user the user who should be asked for the second factor
     * @param verificationCodeHandler the handler to which to pass the code entered by the user
     */
    public CmsSecondFactorDialog(CmsUser user, Consumer<String> verificationCodeHandler) {

        CmsResourceInfo userInfo = CmsAccountsApp.getPrincipalInfo(user);
        userInfo.setTopLineText(user.getFullName());
        displayResourceInfoDirectly(Collections.singletonList(userInfo));

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), new HashMap<>());
        m_verificationCodeHandler = verificationCodeHandler;
        m_okButton.addClickListener(event -> submit());

        m_verification.addShortcutListener(new ShortcutListener(null, KeyCode.ENTER, null) {

            private static final long serialVersionUID = 1L;

            @Override
            public void handleAction(Object sender, Object target) {

                submit();
            }
        });
        m_verification.addStyleName(CLASS_VERIFICATION_CODE_FIELD);
        addAttachListener(event -> {
            m_verification.focus();
            initVerificationField();
        });
    }

    /**
     * Gets the caption to use for the dialog window.
     *
     * @param user the user for whom 2FA should be used
     *
     * @return the dialog caption
     */
    public static String getCaption(CmsUser user) {

        return CmsVaadinUtils.getMessageText(Messages.GUI_LOGIN_2FA_VERIFICATION_0);
    }

    /**
     * Executes Javascript code that sets additional attributes on the verification code field.
     */
    public static void initVerificationField() {

        try {
            byte[] jsSnippetBytes = CmsFileUtil.readFully(
                CmsSecondFactorDialog.class.getResourceAsStream("init-verification-field.js"),
                true);
            String jsSnippet = new String(jsSnippetBytes, StandardCharsets.UTF_8);
            JavaScript.getCurrent().execute(jsSnippet);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Executed when the user clicks the OK button or presses Enter.
     */
    protected void submit() {

        String otp = m_verification.getValue().trim();
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(otp)) {
            CmsVaadinUtils.getWindow(this).close();
            m_verificationCodeHandler.accept(otp);
        }
    }
}

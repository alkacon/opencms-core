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

import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.util.CmsStyleVariable;
import org.opencms.util.CmsStringUtil;

import java.util.Locale;

import com.vaadin.server.UserError;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.VerticalLayout;

/**
 * The change password form.<p>
 */
public class CmsPasswordForm extends VerticalLayout {

    /** The serial version id. */
    private static final long serialVersionUID = 773244283339376105L;

    /** Label with additional text. */
    private Label m_additionalText;

    /** Field for the old password. */
    private PasswordField m_oldPasswordField;

    /** Old password style variable. */
    private CmsStyleVariable m_oldPasswordStyle;

    /** Layout wrapping the old password field. */
    private CssLayout m_oldPasswordWrapper;

    /** Password 1 style variable. */
    private CmsStyleVariable m_password1Style;

    /** Layout wrapping the password 1 field. */
    private CssLayout m_password1Wrapper;

    /** Password 2 style variable. */
    private CmsStyleVariable m_password2Style;

    /** Layout wrapping the password 2 field. */
    private CssLayout m_password2Wrapper;

    /** First password field. */
    private PasswordField m_passwordField1;

    /** Second password field. */
    private PasswordField m_passwordField2;

    /** Label to display the security hint. */
    private Label m_securityHint;

    /**
     * Constructor.<p>
     *
     * @param locale the user locale
     */
    public CmsPasswordForm(Locale locale) {
        CmsVaadinUtils.readAndLocalizeDesign(this, OpenCms.getWorkplaceManager().getMessages(locale), null);
        m_securityHint.setVisible(false);
        m_password1Style = new CmsStyleVariable(m_password1Wrapper);
        m_password2Style = new CmsStyleVariable(m_password2Wrapper);
        m_oldPasswordStyle = new CmsStyleVariable(m_oldPasswordWrapper);
    }

    /**
     * Returns the old password value.<p>
     *
     * @return the old password
     */
    public String getOldPassword() {

        return m_oldPasswordField.getValue();
    }

    /**
     * Returns the old password field.<p>
     *
     * @return the old password field
     */
    public PasswordField getOldPasswordField() {

        return m_oldPasswordField;
    }

    /**
     * Returns the password 1 value.<p>
     *
     * @return the password 1
     */
    public String getPassword1() {

        return m_passwordField1.getValue();
    }

    /**
     * Returns the password 1 field.<p>
     *
     * @return the password 1 field
     */
    public PasswordField getPassword1Field() {

        return m_passwordField1;
    }

    /**
     * Returns the password 2 value.<p>
     *
     * @return the password 2
     */
    public String getPassword2() {

        return m_passwordField2.getValue();
    }

    /**
     * Returns the password 2 field.<p>
     *
     * @return the password 2 field
     */
    public PasswordField getPassword2Field() {

        return m_passwordField2;
    }

    /**
     * Hides the old password field
     */
    public void hideOldPassword() {

        m_oldPasswordWrapper.setVisible(false);
    }

    /**
     * Displays an additional message.<p>
     *
     * @param text the message
     */
    public void setAdditionalText(String text) {

        if (CmsStringUtil.isEmpty(text)) {
            m_additionalText.setVisible(false);
        } else {
            m_additionalText.setValue(text);
            m_additionalText.setVisible(true);
        }
    }

    /**
     * Sets the old password error.<p>
     *
     * @param error the error
     * @param style the style class
     */
    public void setErrorOldPassword(UserError error, String style) {

        m_oldPasswordField.setComponentError(error);
        m_oldPasswordStyle.setStyle(style);
    }

    /**
     * Sets the password 1 error.<p>
     *
     * @param error the error
     * @param style the style class
     */
    public void setErrorPassword1(UserError error, String style) {

        m_passwordField1.setComponentError(error);
        m_password1Style.setStyle(style);
    }

    /**
     * Sets the password 2 error.<p>
     *
     * @param error the error
     * @param style the style class
     */
    public void setErrorPassword2(UserError error, String style) {

        m_passwordField2.setComponentError(error);
        m_password2Style.setStyle(style);
    }

    /**
     * Sets the security hint.<p>
     *
     * @param hint the hint to display
     */
    public void setSecurityHint(String hint) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(hint)) {
            m_securityHint.setValue(hint);
            m_securityHint.setVisible(true);
        } else {
            m_securityHint.setVisible(false);
        }
    }
}

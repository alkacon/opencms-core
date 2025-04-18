/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.login;

import org.opencms.db.CmsLoginMessage;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.Messages;
import org.opencms.ui.components.CmsFakeWindow;
import org.opencms.ui.components.OpenCmsTheme;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Maps;
import com.vaadin.annotations.DesignRoot;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.OptionGroup;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Login form.<p>
 */
@DesignRoot
public class CmsLoginForm extends VerticalLayout {

    /** The private PC type constant. */
    public static final String PC_TYPE_PRIVATE = "private";

    /** The public PC type constant. */
    public static final String PC_TYPE_PUBLIC = "public";

    /** Version id. */
    private static final long serialVersionUID = 1L;

    /** The login controller. */
    protected CmsLoginController m_controller;

    /** The label showing the copyright information. */
    private Label m_copyright;

    /** The error label. */
    private Label m_error;

    /** Button for opening the "forgot password" dialog. */
    private Button m_forgotPasswordButton;

    /**Fake window. */
    private CmsFakeWindow m_fakeWindow;

    /** Label showing an optional configurable message.*/
    private Label m_additionalMessage;

    /** Login button. */
    private Button m_loginButton;

    /** Button to show / hide advanced options. */
    private Button m_optionsButton;

    /** Boolean which indicated whether the advanced options are currently visible. */
    private boolean m_optionsVisible;

    /** Widget for OU selection. */
    private CmsLoginOuSelector m_ouSelect;

    /** Widget for entering the password. */
    private CmsLoginPasswordField m_passwordField;

    /** The password visibility toggle. */
    private Button m_showPasswordButton;

    /** The security field, which allows the user to choose between a private or public PC. */
    private OptionGroup m_securityField;

    /** Widget for entering the user name.  */
    private TextField m_userField;

    private boolean m_multipleOus;

    /**
     * Creates a new instance.<p>
     *
     * @param controller the login controller
     * @param locale the locale to use
     */
    public CmsLoginForm(CmsLoginController controller, Locale locale) {

        m_controller = controller;
        final CmsMessages messages = OpenCms.getWorkplaceManager().getMessages(locale);
        Map<String, String> macros = Maps.newHashMap();
        macros.put("showSecure", "" + controller.isShowSecure());
        String pctype = controller.getPcType();
        CmsVaadinUtils.readAndLocalizeDesign(this, messages, macros);
        m_securityField.addItem(PC_TYPE_PUBLIC);
        m_securityField.addItem(PC_TYPE_PRIVATE);
        m_securityField.setValue(pctype);
        m_copyright.setContentMode(ContentMode.HTML);
        m_copyright.setValue(CmsLoginHelper.getCopyrightHtml(locale));
        CmsLoginMessage beforeLoginMessage = OpenCms.getLoginManager().getBeforeLoginMessage();
        if ((beforeLoginMessage != null) && beforeLoginMessage.isEnabled()) {
            m_additionalMessage.setVisible(true);
            m_additionalMessage.setContentMode(ContentMode.HTML);
            m_additionalMessage.setValue(beforeLoginMessage.getMessage());
        }
        m_securityField.setItemCaption(
            PC_TYPE_PRIVATE,
            messages.key(org.opencms.workplace.Messages.GUI_LOGIN_PCTYPE_PRIVATE_0));
        m_securityField.setItemCaption(
            PC_TYPE_PUBLIC,
            messages.key(org.opencms.workplace.Messages.GUI_LOGIN_PCTYPE_PUBLIC_0));
        setWidth("600px");
        m_loginButton.setClickShortcut(KeyCode.ENTER);
        m_loginButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                m_controller.onClickLogin();
            }
        });
        addAttachListener(new AttachListener() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("synthetic-access")
            public void attach(AttachEvent event) {

                m_userField.focus();
            }
        });

        ClickListener forgotPasswordListener = new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                m_controller.onClickForgotPassword();
            }
        };

        m_forgotPasswordButton.addClickListener(forgotPasswordListener);

        m_optionsButton.addClickListener(

            new ClickListener() {

                private static final long serialVersionUID = 1L;

                public void buttonClick(ClickEvent event) {

                    toggleOptionsVisible();
                }

            });
        m_error.setContentMode(ContentMode.HTML);
        m_showPasswordButton.addStyleName("o-login-show-password");
        m_showPasswordButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        m_showPasswordButton.addStyleName(OpenCmsTheme.BUTTON_UNPADDED);
        m_showPasswordButton.setIcon(FontAwesome.EYE_SLASH);
        m_showPasswordButton.addClickListener(evt -> togglePasswordVisible());

    }

    /**
     * Hides the error message.
     */
    public void clearError() {

        m_error.setVisible(false);
    }

    /**
     * Gets the OU.<p>
     *
     * @return the OU
     */
    public String getOrgUnit() {

        return m_ouSelect.getValue();
    }

    /**
     * Gets the password.<p>
     *
     * @return the password
     */
    public String getPassword() {

        return m_passwordField.getValue();
    }

    /**
     * Gets the PC type.<p>
     *
     * @return the PC type
     */
    public String getPcType() {

        return "" + m_securityField.getValue();
    }

    /**
     * Gets the user.<p>
     *
     * @return the user
     */
    public String getUser() {

        return m_userField.getValue();
    }

    /**
     * Resets the password field.<p>
     */
    public void resetPassword() {

        m_passwordField.clear();

    }

    /**
     * Selects a specific org unit.<p>
     *
     * @param preselectedOu the OU to select
     */
    public void selectOrgUnit(String preselectedOu) {

        if (preselectedOu == null) {
            if (OpenCms.getLoginManager().isOrgUnitRequired()) {
                preselectedOu = CmsLoginOuSelector.OU_NONE;
            } else {
                preselectedOu = "/";
            }
        }
        m_ouSelect.setValue(preselectedOu);

    }

    /**
     * Sets visibility of 'advanced' options.<p>
     *
     * @param optionsVisible true if the options should be shown, false if not
     */
    public void setOptionsVisible(boolean optionsVisible) {

        m_optionsVisible = optionsVisible;

        boolean ousVisible = optionsVisible && !m_ouSelect.isAlwaysHidden();
        m_ouSelect.setVisible(ousVisible);
        m_forgotPasswordButton.setVisible(optionsVisible);
        String optionsMessage = CmsVaadinUtils.getMessageText(
            optionsVisible ? Messages.GUI_LOGIN_OPTIONS_HIDE_0 : Messages.GUI_LOGIN_OPTIONS_SHOW_0);
        m_optionsButton.setCaption(optionsMessage);
    }

    /**
     * Sets the org units available for selection.<p>
     *
     * @param ous the ous
     */
    public void setSelectableOrgUnits(List<CmsOrganizationalUnit> ous) {

        boolean addEmptySelection = OpenCms.getLoginManager().isOrgUnitRequired() && (ous.size() > 1);
        m_ouSelect.initOrgUnits(ous, addEmptySelection);
        boolean optionsVisible = addEmptySelection && (ous.size() > 1);
        setOptionsVisible(optionsVisible);
    }

    /**
     * Toggles visibility of 'advanced' options.<p>
     */
    public void toggleOptionsVisible() {

        setOptionsVisible(!m_optionsVisible);
    }

    /**
     * Toggles the password visibility (also changes icon for the password visibility toggle button).
     */
    protected void togglePasswordVisible() {

        boolean visible = !m_passwordField.isPasswordVisible();
        m_showPasswordButton.setIcon(visible ? FontAwesome.EYE : FontAwesome.EYE_SLASH);
        m_passwordField.setPasswordVisible(visible);

    }

    /**
     * Displays the given login error.<p>
     *
     * @param messageHTML the error message
     */
    void displayError(String messageHTML) {

        // m_fakeWindow.addStyleName("waggler");
        m_error.setValue(messageHTML);
        m_error.setVisible(true);
        CmsVaadinUtils.waggleMeOnce(m_fakeWindow);
        //
        //Add JavaScript code, which adds the wiggle class and removes it after a short time.
        //        JavaScript.getCurrent().execute(
        //            "wiggleElement=document.querySelectorAll(\".waggler\")[0];\n"
        //                + "wiggleElement.className=wiggleElement.className + \" waggle\";\n"
        //                + "setTimeout(function () {\n"
        //                + "element=document.querySelectorAll(\".waggle\")[0];\n"
        //                + "element.className=element.className.replace(/\\bwaggle\\b/g, \"\");"
        //                + "    }, 1500);");
    }

}

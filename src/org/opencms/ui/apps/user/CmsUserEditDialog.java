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

package org.opencms.ui.apps.user;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsDefaultValidationHandler;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPasswordInfo;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.CmsUserLog;
import org.opencms.security.I_CmsPasswordHandler;
import org.opencms.security.I_CmsPasswordSecurityEvaluator;
import org.opencms.security.I_CmsPasswordSecurityEvaluator.SecurityLevel;
import org.opencms.security.twofactor.CmsTwoFactorAuthenticationHandler;
import org.opencms.site.CmsSite;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.CmsPageEditorConfiguration;
import org.opencms.ui.apps.CmsSitemapEditorConfiguration;
import org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsUserDataFormLayout;
import org.opencms.ui.components.CmsUserDataFormLayout.EditLevel;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.components.fileselect.CmsPathSelectField;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelect;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelect.WidgetType;
import org.opencms.ui.login.CmsLoginController;
import org.opencms.ui.login.CmsPasswordForm;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.mail.EmailException;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.Window;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.Validator;
import com.vaadin.v7.data.Validator.InvalidValueException;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.TextArea;
import com.vaadin.v7.ui.TextField;

/**
 * Class for the dialog to edit user settings.<p>
 */
@SuppressWarnings("deprecation")
public class CmsUserEditDialog extends CmsBasicDialog implements I_CmsPasswordFetcher {

    /**
     * Validator for the eamil field.<p>
     */
    class EmailValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = 8943898736907290076L;

        /**
         * @see com.vaadin.v7.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if (value == null) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_VALIDATION_EMAIL_EMPTY_0));
            }
            try {
                CmsUser.checkEmail((String)value);
            } catch (CmsIllegalArgumentException e) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_VALIDATION_EMAIL_INVALID_0));
            }
        }
    }

    /**
     * Validator for the login name field.<p>
     */
    class LoginNameValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = -6768717591898665618L;

        /**
         * @see com.vaadin.v7.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if (value == null) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_VALIDATION_LOGINNAME_EMPTY_0));
            }

            try {
                CmsDefaultValidationHandler handler = new CmsDefaultValidationHandler();
                handler.checkUserName((String)value);
            } catch (CmsIllegalArgumentException e) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_VALIDATION_LOGINNAME_INVALID_0));
            }

            if (userAlreadyExists((String)value)) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_VALIDATION_LOGINNAME_DOUBLE_0));
            }
        }
    }

    /**
     * Validator for password fields.<p>
     */
    class PasswordValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = 64216980175982548L;

        /**
         * @see com.vaadin.v7.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if (isPasswordMismatchingConfirm()) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(
                        Messages.GUI_USERMANAGEMENT_USER_VALIDATION_PASSWORD_NOT_EQUAL_CONFIRM_0));
            }

            if (!isNewUser()) {
                if ((value == null) | CmsStringUtil.isEmptyOrWhitespaceOnly((String)value)) {
                    return; //ok, password was not changed for existing user
                }
            }

            if (!isPasswordValid()) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_VALIDATION_PASSWORD_INVALID_0));
            }
        }
    }

    /**
     * Validator for start view and start site field.<p>
     */
    class StartPathValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = -4257155941690487831L;

        /**
         * @see com.vaadin.v7.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if (!m_visSites) {
                return;
            }
            if (!isSiteNull()) {
                if (!isSitePathValid()) {
                    throw new InvalidValueException(
                        CmsVaadinUtils.getMessageText(
                            Messages.GUI_USERMANAGEMENT_USER_VALIDATION_START_PATH_NOT_VALID_0));

                }
            }

        }
    }

    /**
     * Validator for start project.<p>
     */
    class StartProjectValidator implements Validator {

        /** Serialization id. */
        private static final long serialVersionUID = 7117548227591179638L;

        /** The invalid value. */
        private String m_invalidProject;

        /**
         * Creates the validator.
         * @param invalidProject the project to treat as invalid.
         */
        public StartProjectValidator(String invalidProject) {

            m_invalidProject = invalidProject;
        }

        /**
         * @see com.vaadin.v7.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if (Objects.equals(String.valueOf(value), m_invalidProject)) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(
                        Messages.GUI_USERMANAGEMENT_USER_VALIDATION_START_PROJECT_NOT_EXISTING_0));
            }
        }
    }

    /**
     * Validator for start view and start site field.<p>
     */
    class StartSiteValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = -4257155941690487831L;

        /**
         * @see com.vaadin.v7.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if (!m_visSites) {
                return;
            }
            if (isSiteNull()) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_VALIDATION_STARTSITE_EMPTY_0));

            }

        }
    }

    /**
     * Validator for start view and start site field.<p>
     */
    class StartViewValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = -4257155941690487831L;

        /**
         * @see com.vaadin.v7.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if (!isSiteNull()) {

                if (isRootSiteSelected() & !isStartViewAvailableOnRoot()) {
                    throw new InvalidValueException(
                        CmsVaadinUtils.getMessageText(
                            Messages.GUI_USERMANAGEMENT_USER_VALIDATION_STARTVIEW_NOTFORROOT_0));
                }

            }
        }
    }

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUserEditDialog.class);

    /**vaadin serial id.*/
    private static final long serialVersionUID = -5198443053070008413L;

    /**Visible sites? */
    protected boolean m_visSites = true;

    /**Flag indicates is user is in webou. */
    boolean m_isWebOU;

    /**Password form. */
    CmsPasswordForm m_pw;

    /**vaadin component.*/
    ComboBox m_site;

    /**vaadin component.*/
    CmsPathSelectField m_startfolder;

    /** The app instance. */ 
    private CmsAccountsApp m_app;

    /**vaadin component.*/
    private Button m_cancel;

    /**CmsObject. */
    private CmsObject m_cms;

    /**vaadin component.*/
    private TextArea m_description;

    /** Label containing invisible dummy password fields to dissuade Firefox from saving the password *after* the user edit dialog. */
    private Label m_dummyPasswordLabel;

    /**User edit parameter. */
    private CmsUserEditParameters m_editParams = new CmsUserEditParameters();

    /**vaadin component.*/
    private CheckBox m_enabled;

    /**vaadin component. */
    private CheckBox m_forceResetPassword;

    /**Vaadin component. */
    private Button m_generateButton;

    /**Select view for principals.*/
    private CmsPrincipalSelect m_group;

    /**vaadin component.*/
    private ComboBox m_language;

    /**vaadin component.*/
    private TextField m_loginname;

    /**Flag indicates if name was empty. */
    private boolean m_name_was_empty;

    /**vaadin component. */
    private Button m_next;

    /**vaadin component.*/
    private Button m_ok;

    /**vaadin component.*/
    private Label m_ou;

    private PasswordValidator m_passwordValidator = new PasswordValidator();

    /**vaadin component.*/
    private ComboBox m_project;

    /** Check box for resetting 2FA information. */
    private CheckBox m_resetTwoFactorAuthentication;

    /**vaadin component. */
    private ComboBox m_role;

    /**vaadin component.*/
    private CheckBox m_selfmanagement;

    /**vaadin component. */
    private CheckBox m_sendEmail;

    /**vaadin component.*/
    private ComboBox m_startview;

    /**vaadin component.*/
    private TabSheet m_tab;

    private com.vaadin.ui.Label m_twoFactorAuthState;

    private FormLayout m_twoFactorBox;

    /**vaadin component.*/
    private CmsUser m_user;

    /**User data form.<p>*/
    private CmsUserDataFormLayout m_userdata;

    /**
     * public constructor.<p>
     *
     * @param cms CmsObject
     * @param userId id of user
     * @param window to be closed
     * @param app account app instance
     */
    public CmsUserEditDialog(CmsObject cms, CmsUUID userId, final Window window, final CmsAccountsApp app) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        setPasswordFields();
        try {
            m_cms = OpenCms.initCmsObject(cms);
            m_app = app;
            m_startfolder.disableSiteSwitch();
            m_user = m_cms.readUser(userId);
            m_editParams = app.getUserEditParameters(m_user);
            if (m_user.isWebuser()) {
                m_sendEmail.setVisible(false);
                m_sendEmail.setValue(Boolean.FALSE);
                m_forceResetPassword.setVisible(false);
                m_forceResetPassword.setValue(Boolean.FALSE);
                m_selfmanagement.setVisible(false);
                m_selfmanagement.setValue(Boolean.FALSE);
                m_isWebOU = true;
            } else {
                m_selfmanagement.setValue(Boolean.valueOf(true));
            }

            displayResourceInfoDirectly(Collections.singletonList(CmsAccountsApp.getPrincipalInfo(m_user)));
            m_group.setVisible(false);
            m_role.setVisible(false);
            m_loginname.setValue(m_user.getSimpleName());
            m_loginname.setEnabled(false);
            m_ou.setValue(m_user.getOuFqn().isEmpty() ? "/" : m_user.getOuFqn());

            m_description.setValue(m_user.getDescription());
            m_selfmanagement.setValue(Boolean.valueOf(!m_user.isManaged()));
            m_enabled.setValue(Boolean.valueOf(m_user.isEnabled()));
            CmsUserSettings settings = new CmsUserSettings(m_user);
            init(window, app, settings, m_editParams.isEditEnabled());
            m_sendEmail.setEnabled(false);
            m_forceResetPassword.setValue(
                m_user.getAdditionalInfo().get(CmsUserSettings.ADDITIONAL_INFO_PASSWORD_RESET) != null);
            m_next.setVisible(false);
            setupStartFolder(settings.getStartFolder());

            m_loginname.setEnabled(false);

            if (!m_editParams.isEditEnabled()) {
                m_description.setEnabled(false);
            }
            if (!m_editParams.isPasswordChangeEnabled()) {
                m_pw.setVisible(false);
                m_forceResetPassword.setVisible(false);
                m_sendEmail.setVisible(false);
                m_generateButton.setVisible(false);
            }

            CmsTwoFactorAuthenticationHandler twoFactorHandler = OpenCms.getTwoFactorAuthenticationHandler();
            if (twoFactorHandler.needsTwoFactorAuthentication(m_user)) {
                m_twoFactorBox.setVisible(true);
                if (!twoFactorHandler.hasSecondFactor(m_user)) {
                    m_resetTwoFactorAuthentication.setEnabled(false);
                    m_twoFactorAuthState.setValue(
                        CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_2FA_NOT_SET_UP_0));
                } else {
                    m_twoFactorAuthState.setValue(
                        CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_2FA_USED_0));
                    m_resetTwoFactorAuthentication.setEnabled(true);
                }
            } else {
                m_twoFactorBox.setVisible(false);
            }

        } catch (CmsException e) {
            LOG.error("Can't read user", e);
        }
    }

    /**
     * public constructor for new user case.<p>
     *
     * @param cms CmsObject
     * @param window Window
     * @param ou organizational unit
     * @param app accounts app instance
     */
    public CmsUserEditDialog(CmsObject cms, final Window window, String ou, final CmsAccountsApp app) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        CmsOrganizationalUnit myOu = null;
        try {
            m_app = app;
            m_cms = OpenCms.initCmsObject(cms);
            myOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, ou);

            m_isWebOU = false;
            m_sendEmail.setValue(Boolean.TRUE);
            m_forceResetPassword.setValue(Boolean.TRUE);
            if (myOu.hasFlagWebuser()) {
                m_role.setVisible(false);
                m_sendEmail.setVisible(false);
                m_sendEmail.setValue(Boolean.FALSE);
                m_forceResetPassword.setVisible(false);
                m_forceResetPassword.setValue(Boolean.FALSE);
                m_selfmanagement.setVisible(false);
                m_selfmanagement.setValue(Boolean.FALSE);
                m_isWebOU = true;
            } else {
                iniRole(m_cms, ou, m_role, LOG, true);
                m_role.select(CmsRole.EDITOR.forOrgUnit(ou));
                m_selfmanagement.setValue(Boolean.valueOf(true));

            }
        } catch (CmsException e) {
            LOG.error("Unable to read OU", e);
        }
        setPasswordFields();
        m_ou.setValue(ou.isEmpty() ? "/" : ou);
        m_group.setWidgetType(WidgetType.groupwidget);
        try {
            CmsGroup group = m_cms.readGroup(ou + OpenCms.getDefaultUsers().getGroupUsers());
            m_group.setValue(group.getName());
        } catch (CmsException e1) {
            //There is no user group -> ok, keep field empty
        }
        m_group.setRealPrincipalsOnly(true);
        m_group.setOU(m_ou.getValue());
        try {
            m_group.setIncludeWebOus(OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, ou).hasFlagWebuser());
        } catch (CmsException e) {
            //
        }
        m_twoFactorBox.setVisible(false);

        m_enabled.setValue(Boolean.TRUE);

        init(window, app, null, true);
        setupStartFolder(null);

        m_tab.addSelectedTabChangeListener(new SelectedTabChangeListener() {

            private static final long serialVersionUID = -2579639520410382246L;

            public void selectedTabChange(SelectedTabChangeEvent event) {

                setButtonVisibility();

            }
        });
        setButtonVisibility();
    }

    /**
     * Initialized the role ComboBox. (Vaadin 8 version)<p>
     *
     * @param cms CmsObject
     * @param ou to load roles for
     * @param roleComboBox ComboBox
     * @param log LOG
     */
    protected static void iniRole(CmsObject cms, String ou, com.vaadin.ui.ComboBox<CmsRole> roleComboBox, Log log) {

        try {
            List<CmsRole> roles = OpenCms.getRoleManager().getRoles(cms, ou, false);
            CmsRole.applySystemRoleOrder(roles);

            DataProvider provider = new ListDataProvider<CmsRole>(roles);

            roleComboBox.setDataProvider(provider);
            roleComboBox.setItemCaptionGenerator(role -> {
                try {
                    return role.getDisplayName(cms, A_CmsUI.get().getLocale());
                } catch (CmsException e) {
                    return "";
                }
            });
            roleComboBox.setEmptySelectionAllowed(false);

        } catch (CmsException e) {
            if (log != null) {
                log.error("Unable to read roles.", e);
            }
        }
    }

    /**
     * Initialized the role ComboBox (vaadin-v7-version).<p>
     *
     * @param cms CmsObject
     * @param ou to load roles for
     * @param roleComboBox ComboBox
     * @param log LOG
     */
    protected static void iniRole(CmsObject cms, String ou, ComboBox roleComboBox, Log log) {

        iniRole(cms, ou, roleComboBox, log, false);
    }

    /**
     * Initialized the role ComboBox (vaadin-v7-version).<p>
     *
     * @param cms CmsObject
     * @param ou to load roles for
     * @param roleComboBox ComboBox
     * @param log LOG
     * @param includeNoRoleEntry with noRole entry?
     */
    protected static void iniRole(
        CmsObject cms,
        String ou,
        ComboBox roleComboBox,
        Log log,
        boolean includeNoRoleEntry) {

        try {
            List<CmsRole> roles = OpenCms.getRoleManager().getRoles(cms, ou, false);
            CmsRole.applySystemRoleOrder(roles);
            IndexedContainer container = new IndexedContainer();
            container.addContainerProperty("caption", String.class, "");
            for (CmsRole role : roles) {
                Item item = container.addItem(role);
                item.getItemProperty("caption").setValue(role.getDisplayName(cms, A_CmsUI.get().getLocale()));
            }
            if (includeNoRoleEntry) {
                Item item = container.addItem("NoRole");
                item.getItemProperty("caption").setValue(
                    CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_NO_ROLE_0));
            }
            roleComboBox.setContainerDataSource(container);
            roleComboBox.setItemCaptionPropertyId("caption");
            roleComboBox.setNullSelectionAllowed(false);
            roleComboBox.setNewItemsAllowed(false);
        } catch (CmsException e) {
            if (log != null) {
                log.error("Unable to read roles.", e);
            }
        }
    }

    /**
     * Sends an email to the user.<p>
     *
     * @param cms CmsObject
     * @param password of the user
     * @param user user to send mail to
     * @param newUser flag indicates if user is new
     * @param changePassword has the user to change password?
     */
    protected static void sendMail(
        CmsObject cms,
        String password,
        CmsUser user,
        boolean newUser,
        boolean changePassword) {

        sendMail(cms, password, user, user.getOuFqn(), newUser, changePassword);
    }

    /**
     * Sends an email to the user.<p>
     *
     * @param cms CmsObject
     * @param password of the user
     * @param user user to send mail to
     * @param ou name
     * @param newUser flag indicates if user is new
     * @param changePassword has the user to change password?
     */
    protected static void sendMail(
        CmsObject cms,
        String password,
        CmsUser user,
        String ou,
        boolean newUser,
        boolean changePassword) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(user.getEmail())) {
            return;
        }
        CmsSendPasswordNotification notification = new CmsSendPasswordNotification(
            cms,
            password,
            user,
            ou,
            cms.getRequestContext().getCurrentUser(),
            newUser,
            changePassword);
        try {
            notification.send();
        } catch (EmailException e) {
            LOG.error("Unable to send email with password", e);
        }

    }

    /**
     * @see org.opencms.ui.apps.user.I_CmsPasswordFetcher#fetchPassword(java.lang.String)
     */
    public void fetchPassword(String password) {

        m_pw.getPassword1Field().setValue(password);
        m_pw.getPassword2Field().setValue(password);
        m_forceResetPassword.setValue(Boolean.TRUE);
        m_sendEmail.setValue(Boolean.TRUE);
        m_sendEmail.setEnabled(true);

    }

    /**
     * Checks if a new user should be created.<p>
     *
     * @return true, if create user function
     */
    protected boolean isNewUser() {

        return m_user == null;
    }

    /**m_next
     * Is password not matching to confirm field?<p>
     *
     * @return true, if password not equal to confirm
     */
    protected boolean isPasswordMismatchingConfirm() {

        return !m_pw.getPassword1().equals(m_pw.getPassword2());

    }

    /**
     * Validates the password fields.<p>
     *
     * @return true if password is valid (and confirm field matches password field).<p>
     */
    protected boolean isPasswordValid() {

        if ((m_pw.getPassword1() == null) | (m_pw.getPassword2() == null)) {
            return false;
        }
        try {
            CmsPasswordInfo pwdInfo = new CmsPasswordInfo();
            pwdInfo.setNewPwd(m_pw.getPassword1());
            pwdInfo.setConfirmation(m_pw.getPassword2());

            pwdInfo.validate();
            return true;
        } catch (CmsIllegalArgumentException | CmsIllegalStateException e) {
            LOG.error("New password is not ok", e);
            return false;
        }
    }

    /**
     * Checks if currently the root site is chosen as start site.<p>
     *
     * @return true if root site was selected
     */
    protected boolean isRootSiteSelected() {

        return m_site.getValue().equals("");
    }

    /**
     * Checks if the chosen site is valid.<p>
     *
     * @return true if site is null
     */
    protected boolean isSiteNull() {

        return m_site.getValue() == null;
    }

    /**
     * Checks if the given path is valid resource in site.<p>
     *
     * @return true if the resource is valid
     */
    protected boolean isSitePathValid() {

        try {
            CmsObject cmsLocal = OpenCms.initCmsObject(m_cms);
            cmsLocal.getRequestContext().setSiteRoot((String)m_site.getValue());
            if (m_startfolder.getValue().length() <= ((String)m_site.getValue()).length()) {
                return false;
            }
            return cmsLocal.existsResource(m_startfolder.getValue().substring(((String)m_site.getValue()).length()));
        } catch (CmsException e) {
            LOG.error("Unabel to ini CmsObject", e);
            return false;
        }
    }

    /**
     * Checks if the currently chosen start view is visible for root site.<p>
     *
     * @return true if app is available for root site
     */
    protected boolean isStartViewAvailableOnRoot() {

        if (!m_startview.isEnabled()) {
            return false;
        }

        return !m_startview.getValue().equals(CmsPageEditorConfiguration.APP_ID)
            & !m_startview.getValue().equals(CmsSitemapEditorConfiguration.APP_ID);
    }

    /**
     * Checks if all fields are valid. If not the tab of the first invalid field gets chosen.<p>
     *
     * @return true, if everything is ok
     */
    protected boolean isValid() {

        boolean[] ret = new boolean[4];
        ret[0] = m_loginname.isValid();
        ret[1] = m_isWebOU ? true : m_userdata.isValid() | m_name_was_empty;
        ret[2] = m_isWebOU
        ? true
        : m_site.isValid() & m_startview.isValid() & m_startfolder.isValid() & m_project.isValid();
        ret[3] = validatePasswordField1(m_pw.getPassword1Field().getValue());

        for (int i = 0; i < ret.length; i++) {

            if (!ret[i]) {
                m_tab.setSelectedTab(i);
                break;
            }
        }
        return ret[0] & ret[1] & ret[2] & ret[3];
    }

    /**
     * Saves the canged user data.<p>
     */
    protected void save() {

        boolean newUser = false;
        try {
            if (m_user == null) {
                createNewUser();
                newUser = true;
            } else {

                saveUser();
            }
            saveUserSettings();
            if (m_sendEmail.getValue().booleanValue() & m_sendEmail.isEnabled()) {
                sendMail(m_cms, m_pw.getPassword1(), m_user, newUser, m_forceResetPassword.getValue().booleanValue());
            }
        } catch (CmsException e) {
            LOG.error("Unable to save user", e);
        }
    }

    /**
     * Sets the visibility of the buttons.<p>
     */
    protected void setButtonVisibility() {

        Component tab = m_tab.getSelectedTab();
        int pos = m_tab.getTabPosition(m_tab.getTab(tab));
        //TODO: check if necessary (when it was finally known which tabs web ou users should see..
        int maxPos = m_isWebOU ? 3 : 3; //has to be changed if number of tabs is changed for web OU user
        m_next.setVisible(pos < maxPos);
        m_ok.setVisible(pos == maxPos);
    }

    /**
     * En/Diables the email box.<p>
     */
    protected void setEmailBox() {

        m_sendEmail.setEnabled(
            !CmsStringUtil.isEmptyOrWhitespaceOnly(m_pw.getPassword1())
                | !CmsStringUtil.isEmptyOrWhitespaceOnly(m_pw.getPassword2()));
    }

    /**
     * Sets the start folder depending on current set site field.<p>
     *
     * @param startFolder default value or null
     */
    protected void setupStartFolder(String startFolder) {

        try {
            CmsObject cmsCopy = OpenCms.initCmsObject(m_cms);
            if (m_site.getValue() != null) {
                cmsCopy.getRequestContext().setSiteRoot((String)m_site.getValue());
            } else {
                cmsCopy.getRequestContext().setSiteRoot("");
            }
            m_startfolder.requireFolder();
            m_startfolder.disableSiteSwitch();
            String defaultFolder = OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartFolder();
            if ((startFolder == null)
                && (CmsStringUtil.isEmptyOrWhitespaceOnly(defaultFolder) || !cmsCopy.existsResource(defaultFolder))) {
                defaultFolder = "/";
            }
            m_startfolder.setValue(
                cmsCopy.getRequestContext().addSiteRoot(startFolder == null ? defaultFolder : startFolder));
            m_startfolder.setCmsObject(cmsCopy);
            m_startfolder.setUseRootPaths(true);
            if (!m_visSites) {
                try {
                    List<CmsResource> ouResources = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
                        cmsCopy,
                        m_ou.getValue());
                    if (!ouResources.isEmpty()) {
                        m_startfolder.setValue(ouResources.get(0).getRootPath());
                    }
                } catch (CmsException e1) {
                    LOG.error("unable to read resources for ou", e1);
                }
            }
            m_startfolder.setEnabled(m_visSites);
        } catch (CmsException e) {
            LOG.error("Unable to ini CmsObject", e);
        }
    }

    /**
     * Sets up the validators.<p>
     */
    protected void setupValidators() {

        if (m_loginname.getValidators().size() == 0) {
            m_loginname.addValidator(new LoginNameValidator());
            m_pw.getPassword1Field().addValueChangeListener(event -> {
                validatePasswordField1(event.getValue());
            });
            m_site.addValidator(new StartSiteValidator());
            m_startview.addValidator(new StartViewValidator());
            m_startfolder.addValidator(new StartPathValidator());
        }
    }

    /**Switches to the next tab.*/
    protected void switchTab() {

        Component tab = m_tab.getSelectedTab();
        int pos = m_tab.getTabPosition(m_tab.getTab(tab));
        if (m_isWebOU) {
            if (pos == 0) {
                pos = 1;
            }
        }
        m_tab.setSelectedTab(pos + 1);
    }

    /**
     * Checks if given user exists.<p>
     *
     * @param username to check
     * @return boolean
     */
    protected boolean userAlreadyExists(String username) {

        if (m_user != null) {
            return false;
        }
        CmsUser user = null;
        try {
            user = m_cms.readUser(m_ou.getValue() + username);
        } catch (CmsException e) {
            return false;
        }

        return user != null;

    }

    /**
     * Checks whether the passwords match.<p>
     *
     * @param password2 the password 2 value
     */
    void checkPasswordMatch(String password2) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(password2)) {
            showPasswordMatchError(!password2.equals(m_pw.getPassword1()));
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
                m_pw.setErrorPassword1(null, OpenCmsTheme.SECURITY + "-" + level.name());
            } else {
                m_pw.setErrorPassword1(null, OpenCmsTheme.SECURITY_STRONG);
            }
        } catch (CmsSecurityException e) {
            m_pw.setErrorPassword1(
                new UserError(e.getLocalizedMessage(A_CmsUI.get().getLocale())),
                OpenCmsTheme.SECURITY_INVALID);
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_pw.getPassword2())) {
            showPasswordMatchError(!password.equals(m_pw.getPassword2()));
        }
    }

    /**
     * Returns a CmsObject with request set to given site.<p>
     *
     * @param siteRoot to be used
     * @return CmsObject
     */
    CmsObject getCmsObjectWithSite(String siteRoot) {

        if (siteRoot == null) {
            siteRoot = "/";
        }
        try {
            CmsObject res = OpenCms.initCmsObject(m_cms);
            res.getRequestContext().setSiteRoot(siteRoot);
            return res;
        } catch (CmsException e) {
            LOG.error("Unable to initialize CmsObject", e);
            return m_cms;
        }
    }

    /**
     * Initializes the site combo box.<p>
     *
     * @param settings user settings
     */
    void iniSite(CmsUserSettings settings) {

        List<CmsSite> sitesList = OpenCms.getSiteManager().getAvailableSites(m_cms, true, false, m_ou.getValue());

        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty("caption", String.class, "");
        CmsSite firstNoRootSite = null;
        for (CmsSite site : sitesList) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(site.getSiteRoot())) {
                if (hasRole(CmsRole.VFS_MANAGER)
                    | ((m_user == null)
                        && Arrays.asList(
                            CmsRole.ACCOUNT_MANAGER.forOrgUnit(m_ou.getValue()),
                            CmsRole.ADMINISTRATOR.forOrgUnit(m_ou.getValue()),
                            CmsRole.WORKPLACE_MANAGER.forOrgUnit(m_ou.getValue()),
                            CmsRole.DATABASE_MANAGER.forOrgUnit(m_ou.getValue()),
                            CmsRole.PROJECT_MANAGER.forOrgUnit(m_ou.getValue()),
                            CmsRole.VFS_MANAGER.forOrgUnit(m_ou.getValue()),
                            CmsRole.ROOT_ADMIN.forOrgUnit(m_ou.getValue())).contains(
                                m_role.getValue() instanceof CmsRole
                                ? ((CmsRole)(m_role.getValue())).forOrgUnit(m_ou.getValue())
                                : ""))) {
                    Item item = container.addItem(site.getSiteRoot());
                    item.getItemProperty("caption").setValue(site.getTitle());
                }
            } else {
                if (firstNoRootSite == null) {
                    firstNoRootSite = site;
                }
                Item item = container.addItem(site.getSiteRoot());
                item.getItemProperty("caption").setValue(site.getTitle());
            }
        }
        if (container.size() == 0) {
            if (!container.containsId(A_CmsUI.getCmsObject().getRequestContext().getSiteRoot())) {
                Item defaultItem = container.addItem(A_CmsUI.getCmsObject().getRequestContext().getSiteRoot());
                defaultItem.getItemProperty("caption").setValue(
                    A_CmsUI.getCmsObject().getRequestContext().getSiteRoot());
            }
            m_visSites = false;
        }
        m_site.setContainerDataSource(container);
        m_site.setItemCaptionPropertyId("caption");
        m_site.setNewItemsAllowed(false);
        m_site.setNullSelectionAllowed(false);
        if (settings != null) {
            if (settings.getStartSite().length() >= 1) {
                m_site.select(settings.getStartSite().substring(0, settings.getStartSite().length() - 1));
            } else {
                LOG.error("The start site is unvalid configured");
            }
        } else {
            String defaultSite = OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartSite();
            if (container.containsId(defaultSite)) {
                m_site.select(defaultSite);
            } else if (firstNoRootSite != null) {
                m_site.select(firstNoRootSite.getSiteRoot());
            } else {
                m_site.select(container.getItemIds().get(0));
            }
        }
    }

    /**
     * Initializes the start view.<p>
     *
     * @param settings user settings
     */
    void iniStartView(CmsUserSettings settings) {

        IndexedContainer container = getStartViewContainer("caption");
        if (container.size() > 0) {
            m_startview.setEnabled(true);
            m_startview.setContainerDataSource(container);
            m_startview.setItemCaptionPropertyId("caption");
            m_startview.setNullSelectionAllowed(false);
            m_startview.setNewItemsAllowed(false);
            if (container.getItemIds().size() > 0) {

                if ((settings != null) && container.containsId(settings.getStartView())) {
                    m_startview.select(settings.getStartView());
                } else {
                    String defaultView = OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartView();
                    if (container.containsId(defaultView)) {
                        m_startview.select(defaultView);
                    } else {
                        if (container.containsId("pageeditor")) {
                            m_startview.select("pageeditor");
                        } else {
                            m_startview.select(container.getItemIds().get(0));
                        }
                    }
                }
            }
        } else {
            m_startview.setValue(null);
            m_startview.setEnabled(false);
        }
    }

    /**
     * Shows or hides the not matching passwords error.<p>
     *
     * @param show <code>true</code> to show the error
     */
    void showPasswordMatchError(boolean show) {

        if (show) {

            m_pw.setErrorPassword2(
                new UserError(CmsVaadinUtils.getMessageText(org.opencms.ui.Messages.GUI_PWCHANGE_PASSWORD_MISMATCH_0)),
                OpenCmsTheme.SECURITY_INVALID);
        } else {
            m_pw.setErrorPassword2(null, OpenCmsTheme.SECURITY_STRONG);
        }
    }

    /**
     * Creates new user.<p>
     *
     * @throws CmsException exception
     */
    private void createNewUser() throws CmsException {

        //Password was checked by validator before
        String ou = m_ou.getValue();
        if (!ou.endsWith("/")) {
            ou += "/";
        }
        CmsUser user = m_cms.createUser(ou + m_loginname.getValue(), m_pw.getPassword1(), "", null);
        updateUser(user);
        m_cms.writeUser(user);
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_group.getValue())) {
            m_cms.addUserToGroup(user.getName(), m_group.getValue());
        }
        if (m_role.isVisible() && (m_role.getValue() instanceof CmsRole)) {
            OpenCms.getRoleManager().addUserToRole(m_cms, (CmsRole)m_role.getValue(), user.getName());
        }
        m_user = user;

    }

    /**
     * Returns the start view container.<p>
     *
     * @param caption of the container
     * @return indexed container
     */
    private IndexedContainer getStartViewContainer(String caption) {

        List<I_CmsWorkplaceAppConfiguration> apps = OpenCms.getWorkplaceAppManager().getDefaultQuickLaunchConfigurations();

        IndexedContainer res = new IndexedContainer();

        res.addContainerProperty(caption, String.class, "");

        for (I_CmsWorkplaceAppConfiguration app : apps) {
            if (hasRoleForApp(app)) {
                Item item = res.addItem(app.getId());
                item.getItemProperty(caption).setValue(app.getName(A_CmsUI.get().getLocale()));
            }
        }
        return res;
    }

    /**
     * Checks if user, which gets edited, has given role.<p>
     *
     * @param role to be checked
     * @return true if user has role (or a parent role)
     */
    private boolean hasRole(CmsRole role) {

        if (m_user != null) {
            return OpenCms.getRoleManager().hasRole(m_cms, m_user.getName(), CmsRole.VFS_MANAGER);
        }
        return false;
    }

    /**
     * Checks if user, which gets edited, has role for given app.<p>
     *
     * @param app to be checked
     * @return true if user has role
     */
    private boolean hasRoleForApp(I_CmsWorkplaceAppConfiguration app) {

        if (m_user != null) {
            return OpenCms.getRoleManager().hasRole(m_cms, m_user.getName(), app.getRequiredRole());
        }
        if (!(m_role.getValue() instanceof CmsRole)) {
            return false;
        }
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_group.getValue())) {
            try {
                CmsGroup group = m_cms.readGroup(m_group.getValue());
                CmsRole roleFromGroup = CmsRole.valueOf(group);
                CmsRole roleFromField = (CmsRole)m_role.getValue();
                if ((roleFromGroup == null) || !roleFromGroup.getChildren(true).contains(roleFromField)) {
                    roleFromGroup = roleFromField;
                }
                if (roleFromGroup == null) {
                    return false;
                }
                List<CmsRole> groupRoles = roleFromGroup.getChildren(true);
                groupRoles.add(roleFromGroup);
                List<String> roleNames = new ArrayList<String>();
                for (CmsRole gr : groupRoles) {
                    roleNames.add(gr.getRoleName());
                }
                return roleNames.contains(app.getRequiredRole().getRoleName());
            } catch (CmsException e) {
                LOG.error("Unable to read group", e);
            }
        }
        return true;
    }

    /**
     * Initializes the language combo box.<p>
     *
     * @param settings user settings
     */
    private void iniLanguage(CmsUserSettings settings) {

        IndexedContainer container = CmsVaadinUtils.getWorkplaceLanguageContainer("caption");
        m_language.setContainerDataSource(container);
        m_language.setItemCaptionPropertyId("caption");
        m_language.setNewItemsAllowed(false);
        m_language.setNullSelectionAllowed(false);

        if (settings != null) {
            m_language.select(settings.getLocale());
        } else {
            if (container.containsId(OpenCms.getWorkplaceManager().getDefaultUserSettings().getLocale())) {
                m_language.select(OpenCms.getWorkplaceManager().getDefaultUserSettings().getLocale());
            } else {
                m_language.select(m_language.getItemIds().iterator().next());
            }
        }
    }

    /**
     * Initializes the project combo box.<p>
     *
     * @param settings of user
     */
    private void iniProject(CmsUserSettings settings) {

        try {
            List<CmsProject> projects = OpenCms.getOrgUnitManager().getAllAccessibleProjects(
                m_cms,
                m_ou.getValue(),
                false);
            for (CmsProject project : projects) {
                m_project.addItem(project.getName());
            }
            m_project.setNewItemsAllowed(false);
            m_project.setNullSelectionAllowed(false);
            if (settings != null) {
                // Project names may start with "/" when stored via the old workplace, this slash has to be removed, to match the name.
                String projString = settings.getStartProject();
                if (projString.startsWith("/")) {
                    projString = projString.substring(1);
                }
                if (!m_project.containsId(projString)) {
                    m_project.addItem(projString);
                    m_project.addValidator(new StartProjectValidator(projString));
                }
                m_project.select(projString);
            } else {
                String defaultProject = OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartProject();
                if (m_project.containsId(defaultProject)) {
                    m_project.select(defaultProject);
                } else if (m_project.containsId("Offline")) {
                    m_project.select("Offline");
                } else {
                    Iterator<?> it = m_project.getItemIds().iterator();
                    String p = (String)it.next();
                    while (p.equals(CmsProject.ONLINE_PROJECT_NAME) & it.hasNext()) {
                        p = (String)it.next();
                    }
                    m_project.select(p);
                }
            }
        } catch (CmsException e) {
            LOG.error("Unable to read projects", e);
        }
    }

    /**
     * A initialization method.<p>
     *
     * @param window to be closed
     * @param app opening this dialog
     * @param settings user settings, null if new user
     * @param enabled enable edit
     */
    private void init(final Window window, final CmsAccountsApp app, final CmsUserSettings settings, boolean enabled) {

        m_userdata.initFields(m_user, enabled ? EditLevel.all : EditLevel.none);
        if (m_user != null) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_user.getFirstname())
                | CmsStringUtil.isEmptyOrWhitespaceOnly(m_user.getLastname())
                | CmsStringUtil.isEmptyOrWhitespaceOnly(m_user.getEmail())) {
                m_name_was_empty = true;
            }
        }
        iniLanguage(settings);
        iniProject(settings);
        iniSite(settings);
        iniStartView(settings);

        m_site.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 5111762655156037899L;

            public void valueChange(ValueChangeEvent event) {

                setupStartFolder(null);

            }

        });

        m_ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -2579639520410382246L;

            public void buttonClick(ClickEvent event) {

                setupValidators();
                if (isValid()) {
                    save();
                    window.close();
                    app.reload();
                }
            }
        });

        m_next.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -8584899970290349959L;

            public void buttonClick(ClickEvent event) {

                setupValidators();
                switchTab();

            }
        });

        m_cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 5803825104722705175L;

            public void buttonClick(ClickEvent event) {

                window.close();
            }
        });

        if (m_user == null) {
            m_role.addValueChangeListener(new ValueChangeListener() {

                private static final long serialVersionUID = 5697126133686172725L;

                public void valueChange(ValueChangeEvent event) {

                    iniSite(settings);
                    iniStartView(settings);
                }
            });
            m_group.addValueChangeListener(new ValueChangeListener() {

                private static final long serialVersionUID = 1512940002751242094L;

                public void valueChange(ValueChangeEvent event) {

                    iniStartView(settings);
                    iniSite(settings);
                }

            });
        }

        m_site.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = -169973382455098800L;

            public void valueChange(ValueChangeEvent event) {

                m_startfolder.setCmsObject(getCmsObjectWithSite((String)m_site.getValue()));

            }

        });

        m_generateButton.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = 4128513094772586752L;

            public void buttonClick(ClickEvent event) {

                final Window windowDialog = CmsBasicDialog.prepareWindow(CmsBasicDialog.DialogWidth.content);
                windowDialog.setCaption(
                    CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_GEN_PASSWORD_CAPTION_0));
                CmsGeneratePasswordDialog dialog = new CmsGeneratePasswordDialog(
                    CmsUserEditDialog.this,
                    new Runnable() {

                        public void run() {

                            windowDialog.close();

                        }
                    });
                windowDialog.setContent(dialog);
                A_CmsUI.get().addWindow(windowDialog);
            }
        });
    }

    private boolean isPasswordField1Valid() {

        String value = m_pw.getPassword1Field().getValue();
        try {
            m_passwordValidator.validate(value);
            return true;
        } catch (InvalidValueException e) {
            return false;
        }
    }

    /**
     * Saves changes to an existing user.<p>
     *
     * @throws CmsException exception
     */
    private void saveUser() throws CmsException {

        updateUser(m_user);

        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_pw.getPassword1())) {
            if (isPasswordValid()) {
                m_cms.setPassword(m_user.getName(), m_pw.getPassword1());
                CmsUserLog.logPasswordChange(m_cms, m_user.getName());
            }
        }

        m_cms.writeUser(m_user);

    }

    /**
     * Saves the user settings.<p>
     *
     * @throws CmsException exception
     */
    private void saveUserSettings() throws CmsException {

        CmsUserSettings settings = new CmsUserSettings(m_user);
        settings.setLocale((Locale)m_language.getValue());
        settings.setStartSite((String)m_site.getValue() + "/");
        settings.setStartProject((String)m_project.getValue());
        if (m_visSites) {
            settings.setStartFolder(m_startfolder.getValue().substring(((String)m_site.getValue()).length()));
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly((String)m_startview.getValue())) {
                settings.setStartView((String)m_startview.getValue());
            }
        }
        settings.save(m_cms);
    }

    /**
     * Sets the password fields.<p>
     */
    private void setPasswordFields() {

        m_dummyPasswordLabel.setContentMode(com.vaadin.v7.shared.ui.label.ContentMode.HTML);

        // ugly hack to prevent Firefox from asking user to save password on every click which causes the history token to change after editing a user
        String pwd = "<input type=\"password\" value=\"password\">";
        m_dummyPasswordLabel.setValue("<div style=\"display: none;\">" + pwd + pwd + "</div>");

        m_pw.hideOldPassword();
        m_pw.setHeaderVisible(false);
        if (OpenCms.getPasswordHandler() instanceof I_CmsPasswordSecurityEvaluator) {
            m_pw.setSecurityHint(
                ((I_CmsPasswordSecurityEvaluator)OpenCms.getPasswordHandler()).getPasswordSecurityHint(
                    A_CmsUI.get().getLocale()));
        }
        m_pw.getOldPasswordField().setImmediate(true);

        m_pw.getPassword1Field().addValueChangeListener(event -> {
            checkSecurity(event.getValue());
            setEmailBox();
        });
        m_pw.getPassword2Field().addValueChangeListener(event -> {

            checkSecurity(m_pw.getPassword1());
            checkPasswordMatch(event.getValue());
            setEmailBox();

        });

    }

    /**
     * Sets the password status for the user.<p>
     *
     * @param user CmsUser
     * @param reset true or false
     */
    private void setUserPasswordStatus(CmsUser user, boolean reset) {

        if (reset) {
            user.setAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_PASSWORD_RESET, "true");
        } else {
            user.deleteAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_PASSWORD_RESET);
        }
        m_app.getPasswordResetStateCache().put(user.getId(), Boolean.valueOf(reset));
    }

    /**
     *  Read form and updates a given user according to form.<p>
     *
     * @param user to be updated
     */
    private void updateUser(CmsUser user) {

        setUserPasswordStatus(user, m_forceResetPassword.getValue().booleanValue());
        CmsUserLog.logSetForceResetPassword(A_CmsUI.getCmsObject(), user.getName());
        user.setDescription(m_description.getValue());
        user.setManaged(!m_selfmanagement.getValue().booleanValue());
        if (m_resetTwoFactorAuthentication.getValue().booleanValue()) {
            OpenCms.getTwoFactorAuthenticationHandler().resetTwoFactorAuthentication(user);
        }
        boolean enabled = m_enabled.getValue().booleanValue();
        user.setEnabled(enabled);
        if (enabled) {
            user.getAdditionalInfo().remove(CmsLoginController.KEY_ACCOUNT_LOCKED);
        }
        m_userdata.submit(user, m_cms, new Runnable() {

            public void run() {

                //
            }
        }, true);
    }

    private boolean validatePasswordField1(String value) {

        try {
            m_passwordValidator.validate(value);
            m_pw.getPassword1Field().setComponentError(null);
            return true;
        } catch (InvalidValueException e) {
            m_pw.getPassword1Field().setComponentError(e.getErrorMessage());
            return false;
        }

    }
}

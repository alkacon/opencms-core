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
import org.opencms.security.I_CmsPasswordHandler;
import org.opencms.security.I_CmsPasswordSecurityEvaluator;
import org.opencms.security.I_CmsPasswordSecurityEvaluator.SecurityLevel;
import org.opencms.site.CmsSite;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.CmsPageEditorConfiguration;
import org.opencms.ui.apps.CmsSitemapEditorConfiguration;
import org.opencms.ui.apps.I_CmsWorkplaceAppConfiguration;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsUserDataFormLayout;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.components.fileselect.CmsPathSelectField;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelect;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelect.WidgetType;
import org.opencms.ui.login.CmsPasswordForm;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceLoginHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.mail.EmailException;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

/**
 * Class for the dialog to edit user settings.<p>
 */
public class CmsUserEditDialog extends CmsBasicDialog implements I_CmsPasswordFetcher {

    /**
     * Validator for the eamil field.<p>
     */
    class EmailValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = 8943898736907290076L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
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
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
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
        }
    }

    /**
     * Validator for password fields.<p>
     */
    class PasswordValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = 64216980175982548L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
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
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
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
     * Validator for start view and start site field.<p>
     */
    class StartSiteValidator implements Validator {

        /**vaadin serial id.*/
        private static final long serialVersionUID = -4257155941690487831L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
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
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
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

    /**Flag indicates is user is in webou. */
    boolean m_isWebOU;

    /**Password form. */
    CmsPasswordForm m_pw;

    /**vaadin component.*/
    ComboBox m_site;

    /**vaadin component.*/
    CmsPathSelectField m_startfolder;

    /**Holder for authentification fields. */
    //    private VerticalLayout m_authHolder;

    /**vaadin component.*/
    private Button m_cancel;

    /**CmsObject. */
    private CmsObject m_cms;

    /**vaadin component.*/
    private TextArea m_description;

    /**vaadin component.*/
    private CheckBox m_enabled;

    /**vaadin component. */
    private CheckBox m_forceResetPassword;

    /**vaadin component. */
    private CheckBox m_sendEmail;

    /**Vaadin component. */
    private Button m_generateButton;

    /**Visible sites? */
    protected boolean m_visSites = true;

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

    /**vaadin component.*/
    private ComboBox m_project;

    /**vaadin component. */
    private ComboBox m_role;

    /**vaadin component.*/
    private CheckBox m_selfmanagement;

    /**vaadin component.*/
    private ComboBox m_startview;

    /**vaadin component.*/
    private TabSheet m_tab;

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
     */
    public CmsUserEditDialog(CmsObject cms, CmsUUID userId, final Window window) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        setPasswordFields();
        try {
            m_cms = OpenCms.initCmsObject(cms);
            m_startfolder.disableSiteSwitch();
            m_user = m_cms.readUser(userId);
            if (m_user.isWebuser()) {
                m_tab.removeTab(m_tab.getTab(3));
                m_selfmanagement.setValue(new Boolean(false));
                m_isWebOU = true;
            } else {
                m_selfmanagement.setValue(new Boolean(true));
            }

            displayResourceInfoDirectly(Collections.singletonList(CmsAccountsApp.getPrincipalInfo(m_user)));
            m_group.setVisible(false);
            m_role.setVisible(false);
            m_loginname.setValue(m_user.getSimpleName());
            m_loginname.setEnabled(false);
            m_ou.setValue(m_user.getOuFqn().isEmpty() ? "/" : m_user.getOuFqn());

            m_description.setValue(m_user.getDescription());
            m_selfmanagement.setValue(new Boolean(!m_user.isManaged()));
            m_enabled.setValue(new Boolean(m_user.isEnabled()));
            CmsUserSettings settings = new CmsUserSettings(m_user);
            init(window, settings);
            m_sendEmail.setEnabled(false);
            m_forceResetPassword.setValue(
                CmsUserTable.USER_PASSWORD_STATUS.get(m_user.getId()) == null
                ? Boolean.FALSE
                : CmsUserTable.USER_PASSWORD_STATUS.get(m_user.getId()));
            m_next.setVisible(false);
            setupStartFolder(settings.getStartFolder());

            m_loginname.setEnabled(false);

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
     */
    public CmsUserEditDialog(CmsObject cms, final Window window, String ou) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        CmsOrganizationalUnit myOu = null;
        try {
            m_cms = OpenCms.initCmsObject(cms);
            myOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(m_cms, ou);

            m_isWebOU = false;
            if (myOu.hasFlagWebuser()) {
                m_tab.removeTab(m_tab.getTab(2));
                m_role.setVisible(false);
                m_selfmanagement.setValue(new Boolean(false));
                m_isWebOU = true;
            } else {
                iniRole(ou);
                m_role.select(CmsRole.ELEMENT_AUTHOR.forOrgUnit(ou));
                m_selfmanagement.setValue(new Boolean(true));

            }
        } catch (CmsException e) {
            LOG.error("Unable to read OU", e);
        }
        setPasswordFields();
        m_ou.setValue(ou.isEmpty() ? "/" : ou);
        m_group.setWidgetType(WidgetType.groupwidget);
        m_group.setValue(ou + OpenCms.getDefaultUsers().getGroupUsers());
        m_group.setRealPrincipalsOnly(true);
        m_group.setOU(m_ou.getValue());

        m_enabled.setValue(Boolean.TRUE);

        init(window, null);
        setupStartFolder(null);
        m_sendEmail.setValue(Boolean.TRUE);
        m_forceResetPassword.setValue(Boolean.TRUE);
        m_tab.addSelectedTabChangeListener(new SelectedTabChangeListener() {

            private static final long serialVersionUID = -2579639520410382246L;

            public void selectedTabChange(SelectedTabChangeEvent event) {

                setButtonVisibility();

            }
        });
        setButtonVisibility();
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
        ret[2] = m_isWebOU ? true : m_site.isValid() & m_startview.isValid() & m_startfolder.isValid();
        ret[3] = m_pw.getPassword1Field().isValid();

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
                sendMail(newUser);
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
        int maxPos = m_isWebOU ? 2 : 3;
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
            m_startfolder.setValue(cmsCopy.getRequestContext().addSiteRoot(startFolder == null ? "/" : startFolder));
            m_startfolder.setCmsObject(cmsCopy);
            m_startfolder.setUseRootPaths(true);
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
            m_pw.getPassword1Field().addValidator(new PasswordValidator());
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
                        & m_group.getValue().equals(OpenCms.getDefaultUsers().getGroupAdministrators()))) {
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
            if (firstNoRootSite != null) {
                m_site.select(firstNoRootSite.getSiteRoot());
            } else {
                Iterator<?> it = m_site.getItemIds().iterator();
                if (it.hasNext()) {
                    m_site.select(it);
                }
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
                m_startview.select(container.getItemIds().get(0));
                if (settings != null) {
                    m_startview.select(settings.getStartView());
                }
            }
        } else {
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
        OpenCms.getRoleManager().addUserToRole(m_cms, (CmsRole)m_role.getValue(), user.getName());
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

        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_group.getValue())) {
            try {
                CmsGroup group = m_cms.readGroup(m_group.getValue());
                CmsRole roleFromGroup = CmsRole.valueOf(group);
                CmsRole roleFromField = (CmsRole)m_role.getValue();
                if (!roleFromGroup.getChildren(true).contains(roleFromField)) {
                    roleFromGroup = roleFromField;
                }
                if (roleFromGroup == null) {
                    return false;
                }
                List<CmsRole> groupRoles = roleFromGroup.getChildren(true);
                groupRoles.add(CmsRole.valueOf(group));
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

        m_language.setContainerDataSource(CmsVaadinUtils.getLanguageContainer("caption"));
        m_language.setItemCaptionPropertyId("caption");
        m_language.setNewItemsAllowed(false);
        m_language.setNullSelectionAllowed(false);
        if (settings != null) {
            m_language.select(settings.getLocale());
        } else {
            m_language.select(m_language.getItemIds().iterator().next());
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
                m_project.addItem(project.getSimpleName());
            }
            m_project.setNewItemsAllowed(false);
            m_project.setNullSelectionAllowed(false);
            if (settings != null) {
                String projString = settings.getStartProject();
                if (projString.contains("/")) {
                    projString = projString.split("/")[projString.split("/").length - 1];
                }
                m_project.select(projString);
            } else {
                Iterator<?> it = m_project.getItemIds().iterator();
                String p = (String)it.next();
                while (p.equals(CmsProject.ONLINE_PROJECT_NAME) & it.hasNext()) {
                    p = (String)it.next();
                }
                m_project.select(p);
            }
        } catch (CmsException e) {
            LOG.error("Unable to read projects", e);
        }
    }

    /**
     * Initialized the role ComboBox.<p>
     *
     * @param ou to load roles for
     */
    private void iniRole(String ou) {

        try {
            List<CmsRole> roles = OpenCms.getRoleManager().getRoles(m_cms, ou, false);
            CmsRole.applySystemRoleOrder(roles);
            IndexedContainer container = new IndexedContainer();
            container.addContainerProperty("caption", String.class, "");
            for (CmsRole role : roles) {
                Item item = container.addItem(role);
                item.getItemProperty("caption").setValue(role.getDisplayName(m_cms, A_CmsUI.get().getLocale()));
            }

            m_role.setContainerDataSource(container);
            m_role.setItemCaptionPropertyId("caption");
            m_role.setNullSelectionAllowed(false);
            m_role.setNewItemsAllowed(false);
        } catch (CmsException e) {
            //
        }
    }

    /**
     * A initialization method.<p>
     *
     * @param window to be closed
     * @param settings user settings, null if new user
     */
    private void init(final Window window, final CmsUserSettings settings) {

        m_userdata.initFields(m_user, true);
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
                    A_CmsUI.get().reload();
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

        m_tab.setHeight("400px");
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
        settings.setStartProject(m_ou.getValue() + (String)m_project.getValue());
        if (m_visSites) {
            settings.setStartFolder(m_startfolder.getValue().substring(((String)m_site.getValue()).length()));
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly((String)m_startview.getValue())) {
                settings.setStartView((String)m_startview.getValue());
            }
        }
        settings.save(m_cms);
    }

    /**
     * Sends an email to the user.<p>
     *
     * @param newUser is the user new?
     */
    private void sendMail(boolean newUser) {

        CmsSendPasswordNotification notification = new CmsSendPasswordNotification(
            m_cms,
            m_pw.getPassword1(),
            m_user,
            m_cms.getRequestContext().getCurrentUser(),
            OpenCms.getLinkManager().getWorkplaceLink(m_cms, CmsWorkplaceLoginHandler.LOGIN_HANDLER, false),
            newUser);
        try {
            notification.send();
        } catch (EmailException e) {
            LOG.error("Unable to send email with password", e);
        }
    }

    /**
     * Sets the password fields.<p>
     */
    private void setPasswordFields() {

        m_pw.hideOldPassword();
        m_pw.setHeaderVisible(false);
        if (OpenCms.getPasswordHandler() instanceof I_CmsPasswordSecurityEvaluator) {
            m_pw.setSecurityHint(
                ((I_CmsPasswordSecurityEvaluator)OpenCms.getPasswordHandler()).getPasswordSecurityHint(
                    A_CmsUI.get().getLocale()));
        }
        m_pw.getOldPasswordField().setImmediate(true);
        m_pw.getPassword1Field().setImmediate(true);
        m_pw.getPassword2Field().setImmediate(true);

        m_pw.getPassword1Field().addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            public void textChange(TextChangeEvent event) {

                checkSecurity(event.getText());
                setEmailBox();
            }
        });
        m_pw.getPassword2Field().addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            public void textChange(TextChangeEvent event) {

                checkSecurity(m_pw.getPassword1());
                checkPasswordMatch(event.getText());
                setEmailBox();
            }
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
        CmsUserTable.USER_PASSWORD_STATUS.put(user.getId(), new Boolean(reset));
    }

    /**
     *  Read form and updates a given user according to form.<p>
     *
     * @param user to be updated
     */
    private void updateUser(CmsUser user) {

        setUserPasswordStatus(user, m_forceResetPassword.getValue().booleanValue());
        user.setDescription(m_description.getValue());
        user.setManaged(!m_selfmanagement.getValue().booleanValue());
        user.setEnabled(m_enabled.getValue().booleanValue());
        m_userdata.submit(user, m_cms, new Runnable() {

            public void run() {
                //
            }
        }, true);
    }
}

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

import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.db.CmsLoginMessage;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.CmsResourceBundleLoader;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.jsp.CmsJspLoginBean;
import org.opencms.main.CmsBroadcast.ContentMode;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsCustomLoginException;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsUserLog;
import org.opencms.security.twofactor.CmsSecondFactorInfo;
import org.opencms.security.twofactor.CmsTwoFactorAuthenticationHandler;
import org.opencms.ui.A_CmsDialogContext;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.I_CmsDialogContext.ContextType;
import org.opencms.ui.Messages;
import org.opencms.ui.apps.CmsAppHierarchyConfiguration;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.CmsFileExplorerConfiguration;
import org.opencms.ui.apps.CmsSitemapEditorConfiguration;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.dialogs.CmsUserDataDialog;
import org.opencms.ui.login.CmsLoginHelper.LoginParameters;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsLoginUserAgreement;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceLoginHandler;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.logging.Log;

import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.server.VaadinServletResponse;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Controller class which actually handles the login dialog logic.<p>
 */
public class CmsLoginController {

    /**
     * Represents the login target information.<p>
     */
    public static class CmsLoginTargetInfo {

        /** The password. */
        private String m_password;

        /** The login target. */
        private String m_target;

        /** The user. */
        private String m_user;

        /**
         * Creates a new instance.<p>
         *
         * @param target the login target
         * @param user the user name
         * @param password the password
         */
        public CmsLoginTargetInfo(String target, String user, String password) {

            super();
            m_target = target;
            m_user = user;
            m_password = password;
        }

        /**
         * Returns the password.<p>
         *
         * @return the password
         */
        public String getPassword() {

            return m_password;
        }

        /**
         * Returns the target.<p>
         *
         * @return the target
         */
        public String getTarget() {

            return m_target;
        }

        /**
         * Returns the user.<p>
         *
         * @return the user
         */
        public String getUser() {

            return m_user;
        }
    }

    /**
     * The login context.
     */
    public static class LoginContext {

        /** The CMS context. */
        private CmsObject m_cms;

        /** The second factor information. */
        private CmsSecondFactorInfo m_secondFactorInfo;

        /** The user being logged in. */
        private CmsUser m_user;

        /**
         * Gets the CmsObject.
         *
         * @return the CmsObject
         */
        public CmsObject getCms() {

            return m_cms;
        }

        /**
         * The second factor information for 2FA.
         *
         * @return the second factor information
         */
        public CmsSecondFactorInfo getSecondFactorInfo() {

            return m_secondFactorInfo;
        }

        /**
         * Gets the user to be logged in.
         *
         * @return the user
         */
        public CmsUser getUser() {

            return m_user;
        }

        /**
         * Sets the CMS context.
         *
         * @param cms the CMS context
         */
        public void setCms(CmsObject cms) {

            m_cms = cms;
        }

        /**
         * Sets the second factor information for 2FA.
         *
         * @param secondFactorInfo the second factor information
         */
        public void setSecondFactorInfo(CmsSecondFactorInfo secondFactorInfo) {

            m_secondFactorInfo = secondFactorInfo;
        }

        /**
         * Sets the user.
         *
         * @param user the user
         */
        public void setUser(CmsUser user) {

            m_user = user;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    };

    /**
     * Helper subclass of CmsLoginUserAgreement which can be used without a page context.<p>
     *
     * This is only used for detecting whether we need to display the user agreement dialog, not for displaying the dialog itself.<p>
     */
    protected static class UserAgreementHelper extends CmsLoginUserAgreement {

        /** The replacement CMS context. */
        private CmsObject m_cms;

        /** The replacemenet workplace settings. */
        private CmsWorkplaceSettings m_wpSettings;

        /**
         * Creates a new instance.<p>
         *
         * @param cms the replacement CMS context
         * @param wpSettings the replacement workplace settings
         */
        public UserAgreementHelper(CmsObject cms, CmsWorkplaceSettings wpSettings) {

            super(null);
            m_cms = cms;
            m_wpSettings = wpSettings;
            initAcceptData();
        }

        /**
         * @see org.opencms.workplace.CmsWorkplace#getCms()
         */
        @Override
        public CmsObject getCms() {

            return m_cms;
        }

        /**
         * @see org.opencms.workplace.CmsWorkplace#getSettings()
         */
        @Override
        public CmsWorkplaceSettings getSettings() {

            return m_wpSettings;
        }

        /**
         * @see org.opencms.workplace.CmsWorkplace#initWorkplaceMembers(org.opencms.jsp.CmsJspActionElement)
         */
        @Override
        protected void initWorkplaceMembers(CmsJspActionElement jsp) {

            // do nothing
        }
    }

    /**
     * Helper interface for splitting the actual login off into its own object
     * which can be called from multiple places.
     */
    interface LoginContinuation {

        /**
         * Continues with the login process.
         *
         * @param data the login data
         * @throws Exception if something goes wrong
         */
        void continueLogin(LoginContext data) throws Exception;
    }

    /** Additional info key to mark accounts as locked due to inactivity. */
    public static final String KEY_ACCOUNT_LOCKED = "accountLocked";

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLoginController.class);

    /** The two-factor authentication handler. */
    protected CmsTwoFactorAuthenticationHandler m_otpHandler = OpenCms.getTwoFactorAuthenticationHandler();

    /** The UI instance. */
    CmsLoginUI m_ui;

    /** The administrator CMS context. */
    private CmsObject m_adminCms;

    /** The parameters collected when the login app was opened. */
    private LoginParameters m_params;

    /***
     * Creates a new instance.<p>
     *
     * @param adminCms the admin cms context
     * @param params the parameters for the UI
     */
    public CmsLoginController(CmsObject adminCms, LoginParameters params) {

        m_params = params;
        m_adminCms = adminCms;
    }

    /**
     * Returns the link to the login form.<p>
     *
     * @param cms the current cms context
     *
     * @return the login form link
     */
    public static String getFormLink(CmsObject cms) {

        return OpenCms.getLinkManager().substituteLinkForUnknownTarget(
            cms,
            CmsWorkplaceLoginHandler.LOGIN_HANDLER,
            false);
    }

    /**
     * Gets the login target link.<p>
     *
     * @param currentCms the current CMS context
     * @param settings the workplace settings
     * @param requestedResource the requested resource parameter
     *
     * @return the login target
     *
     * @throws CmsException in case the user has insufficient permissions to access the login target
     */
    public static String getLoginTarget(CmsObject currentCms, CmsWorkplaceSettings settings, String requestedResource)
    throws CmsException {

        String directEditPath = CmsLoginHelper.getDirectEditPath(currentCms, settings.getUserSettings(), false);
        String target = "";
        boolean checkRole = false;
        String fragment = UI.getCurrent() != null ? UI.getCurrent().getPage().getUriFragment() : "";
        boolean workplace2 = false;

        if ((requestedResource == null) && (directEditPath != null)) {
            target = directEditPath;
        } else if ((requestedResource != null) && !CmsWorkplace.JSP_WORKPLACE_URI.equals(requestedResource)) {
            target = requestedResource;
        } else {
            workplace2 = true;
            target = CmsVaadinUtils.getWorkplaceLink();
            checkRole = true;
        }

        UserAgreementHelper userAgreementHelper = new UserAgreementHelper(currentCms, settings);
        boolean showUserAgreement = userAgreementHelper.isShowUserAgreement();
        if (showUserAgreement) {
            target = userAgreementHelper.getConfigurationVfsPath()
                + "?"
                + CmsLoginUserAgreement.PARAM_WPRES
                + "="
                + target;
        }
        if (checkRole && !OpenCms.getRoleManager().hasRole(currentCms, CmsRole.WORKPLACE_USER)) {
            workplace2 = false;
            target = CmsLoginHelper.getDirectEditPath(currentCms, settings.getUserSettings(), true);
            if (target == null) {
                throw new CmsCustomLoginException(
                    org.opencms.workplace.Messages.get().container(
                        org.opencms.workplace.Messages.GUI_LOGIN_FAILED_NO_WORKPLACE_PERMISSIONS_0));
            }
        }
        if (!workplace2) {
            target = OpenCms.getLinkManager().substituteLink(currentCms, target);
        }

        if (workplace2 && CmsStringUtil.isEmptyOrWhitespaceOnly(fragment)) {
            if (settings.getUserSettings().getStartView().startsWith("/")) {
                if (CmsWorkplace.VIEW_WORKPLACE.equals(settings.getUserSettings().getStartView())) {
                    fragment = CmsFileExplorerConfiguration.APP_ID;
                } else if (CmsWorkplace.VIEW_ADMIN.equals(settings.getUserSettings().getStartView())) {
                    fragment = CmsAppHierarchyConfiguration.APP_ID;
                }
            } else {
                fragment = settings.getUserSettings().getStartView();
            }
        }

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(fragment)) {
            if (CmsSitemapEditorConfiguration.APP_ID.equals(fragment)) {
                target = OpenCms.getLinkManager().substituteLink(currentCms, CmsADEManager.PATH_SITEMAP_EDITOR_JSP)
                    + "?path="
                    + settings.getUserSettings().getStartFolder();
            } else {
                target += "#" + fragment;
            }
        }
        return target;
    }

    /**
     * Logs the current user out by invalidating the session an reloading the current URI.<p>
     * Important:  This works only within vaadin apps.<p>
     */
    public static void logout() {

        CmsObject cms = A_CmsUI.getCmsObject();
        if (UI.getCurrent() instanceof CmsAppWorkplaceUi) {
            ((CmsAppWorkplaceUi)UI.getCurrent()).onWindowClose();
        }
        String loggedInUser = cms.getRequestContext().getCurrentUser().getName();
        UI.getCurrent().getSession().close();
        String logoutUri = OpenCms.getLoginManager().getLogoutUri();
        if (logoutUri != null) {
            String target = OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, logoutUri, false);
            // open in top frame, so it still works when the Vaadin dialog is embedded
            Page.getCurrent().open(target, "_top", false);
        } else {
            String loginLink = OpenCms.getLinkManager().substituteLinkForUnknownTarget(
                cms,
                CmsWorkplaceLoginHandler.LOGIN_HANDLER,
                false);
            VaadinService.getCurrentRequest().getWrappedSession().invalidate();
            // open in top frame, so it still works when the Vaadin dialog is embedded
            Page.getCurrent().open(loginLink, "_top", false);
            // logout was successful
            if (LOG.isInfoEnabled()) {
                LOG.info(
                    org.opencms.jsp.Messages.get().getBundle().key(
                        org.opencms.jsp.Messages.LOG_LOGOUT_SUCCESFUL_3,
                        loggedInUser,
                        "{workplace logout option}",
                        cms.getRequestContext().getRemoteAddress()));
            }
            CmsUserLog.logLogout(cms);
        }
    }

    /**
     * Logs out the current user redirecting to the login form afterwards.<p>
     *
     * @param cms the cms context
     * @param request the servlet request
     * @param response the servlet response
     *
     * @throws IOException if writing to the response fails
     */
    public static void logout(CmsObject cms, HttpServletRequest request, HttpServletResponse response)
    throws IOException {

        CmsUserLog.logLogout(cms);
        String loggedInUser = cms.getRequestContext().getCurrentUser().getName();
        HttpSession session = request.getSession(false);
        String logoutUri = OpenCms.getLoginManager().getLogoutUri();
        if (logoutUri == null) {
            if (session != null) {
                session.invalidate();
                /* we need this because a new session might be created after this method,
                 but before the session info is updated in OpenCmsCore.showResource. */
                cms.getRequestContext().setUpdateSessionEnabled(false);
            }
            // logout was successful
            if (LOG.isInfoEnabled()) {
                LOG.info(
                    org.opencms.jsp.Messages.get().getBundle().key(
                        org.opencms.jsp.Messages.LOG_LOGOUT_SUCCESFUL_3,
                        loggedInUser,
                        cms.getRequestContext().addSiteRoot(cms.getRequestContext().getUri()),
                        cms.getRequestContext().getRemoteAddress()));
            }
            response.sendRedirect(getFormLink(cms));
        } else {
            response.sendRedirect(OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, logoutUri, false));
        }
    }

    /**
     * Gets the PC type.<p>
     *
     * @return the PC type
     */
    public String getPcType() {

        String result = m_params.getPcType();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(result)) {
            result = "public";
        }
        return result;
    }

    /**
     * Returns the reset password link.<p>
     *
     * @return the reset password link
     */
    public String getResetPasswordLink() {

        String result = OpenCms.getLinkManager().substituteLinkForUnknownTarget(
            CmsLoginUI.m_adminCms,
            CmsWorkplaceLoginHandler.LOGIN_HANDLER,
            false) + "?" + CmsLoginHelper.PARAM_RESET_PASSWORD;
        String ou = m_ui.getOrgUnit();
        result += "&" + CmsLoginHelper.PARAM_OUFQN + "=" + ou;
        return result;
    }

    /**
     * Returns true if the security option should be displayed in the login dialog.<p>
     *
     * @return true if the security option should be displayed in the login dialog
     */
    public boolean isShowSecure() {

        return OpenCms.getLoginManager().isEnableSecurity();
    }

    /**
     * Called when the user clicks on the 'forgot password' button.<p>
     */
    public void onClickForgotPassword() {

        A_CmsUI.get().getPage().setLocation(getResetPasswordLink());
    }

    /**
     * Called when the user clicks on the login button.<p>
     */
    public void onClickLogin() {

        String user = m_ui.getUser();
        String password = m_ui.getPassword();
        String ou = m_ui.getOrgUnit();
        if (CmsLoginOuSelector.OU_NONE.equals(ou)) {
            displayError(
                CmsVaadinUtils.getMessageText(Messages.GUI_LOGIN_NO_OU_SELECTED_WARNING_0) + "\n\n",
                false,
                false);
            return;
        }
        {
            CmsMessageContainer message = CmsLoginHelper.validateUserAndPasswordNotEmpty(user, password);
            if (message != null) {
                String errorMessage = message.key(m_params.getLocale());
                displayError(errorMessage, true, false);
                return;
            }
        }

        String realUser = CmsStringUtil.joinPaths(ou, user);
        String pcType = m_ui.getPcType();
        CmsObject currentCms = A_CmsUI.getCmsObject();
        CmsUser userObj = null;

        try {
            try {
                userObj = currentCms.readUser(realUser);
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
                CmsMessageContainer message = org.opencms.workplace.Messages.get().container(
                    org.opencms.workplace.Messages.GUI_LOGIN_FAILED_0);
                displayError(message.key(m_params.getLocale()), true, true);
                CmsUserLog.logLoginFailure(currentCms, realUser);
                return;
            }
            final CmsUser userNonNull = userObj;
            if (OpenCms.getLoginManager().canLockBecauseOfInactivity(currentCms, userObj)) {
                boolean locked = null != userObj.getAdditionalInfo().get(KEY_ACCOUNT_LOCKED);
                if (locked) {
                    displayError(CmsInactiveUserMessages.getLockoutText(A_CmsUI.get().getLocale()), false, false);
                    CmsUserLog.logLoginFailure(currentCms, realUser);
                    return;
                }
            }

            CmsObject cloneCms = OpenCms.initCmsObject(currentCms);
            cloneCms.checkLoginUser(realUser, password);

            String messageToChange = "";
            if (OpenCms.getLoginManager().isPasswordReset(currentCms, userObj)) {
                messageToChange = CmsVaadinUtils.getMessageText(Messages.GUI_PWCHANGE_RESET_0);
            }
            if (OpenCms.getLoginManager().requiresPasswordChange(currentCms, userObj)) {
                messageToChange = getPasswordChangeMessage();
            }
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(messageToChange)) {
                CmsChangePasswordDialog passwordDialog = new CmsChangePasswordDialog(
                    currentCms,
                    userObj,
                    A_CmsUI.get().getLocale());
                passwordDialog.setAdditionalMessage(messageToChange);
                A_CmsUI.get().setContentToDialog(
                    Messages.get().getBundle(A_CmsUI.get().getLocale()).key(Messages.GUI_PWCHANGE_HEADER_0)
                        + userObj.getSimpleName(),
                    passwordDialog);
                return;
            }

            /*
             * Login branches now into multiple paths: For the non-OTP case we can do it directly (synchronously),
             * and for the OTP cases we need to show additional dialogs first (so it's asynchronous). But at the end,
             * the same things happen. So we put these 'things that happen at the end' in a closure, so we can either
             * execute it directly or pass it as a callback to the dialogs.
             */
            LoginContinuation loginContinuation = (LoginContext details) -> {

                // provisional login successful, now do for real
                // we use another separate CmsObject so we can manually control when it is written to the session info
                CmsObject loginCms = OpenCms.initCmsObject(currentCms);
                CmsSecondFactorInfo secondFactorInfo = details.getSecondFactorInfo();
                loginCms.loginUser(realUser, password, secondFactorInfo);
                CmsUserLog.logLogin(loginCms, realUser);

                if (LOG.isInfoEnabled()) {
                    CmsRequestContext context = loginCms.getRequestContext();
                    LOG.info(
                        org.opencms.jsp.Messages.get().getBundle().key(
                            org.opencms.jsp.Messages.LOG_LOGIN_SUCCESSFUL_3,
                            context.getCurrentUser().getName(),
                            "{workplace login dialog}",
                            context.getRemoteAddress()));
                }
                CmsWorkplaceSettings settings = CmsLoginHelper.initSiteAndProject(loginCms);
                final String loginTarget = getLoginTarget(loginCms, settings, m_params.getRequestedResource());

                CmsLoginHelper.setCookieData(
                    pcType,
                    user,
                    ou,
                    (VaadinServletRequest)(VaadinService.getCurrentRequest()),
                    (VaadinServletResponse)(VaadinService.getCurrentResponse()));
                VaadinService.getCurrentRequest().getWrappedSession().setAttribute(
                    CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS,
                    settings);

                final boolean isPublicPC = CmsLoginForm.PC_TYPE_PUBLIC.equals(pcType);
                if (OpenCms.getLoginManager().requiresUserDataCheck(loginCms, userNonNull)) {
                    I_CmsDialogContext context = new A_CmsDialogContext("", ContextType.appToolbar, null) {

                        @Override
                        public void finish(CmsProject project, String siteRoot) {

                            finish(null);
                        }

                        @Override
                        public void finish(Collection<CmsUUID> result) {

                            initSessionAndSendMessages(currentCms, loginCms);
                            m_ui.openLoginTarget(loginTarget, isPublicPC);

                        }

                        public void focus(CmsUUID structureId) {

                            // nothing to do
                        }

                        public List<CmsUUID> getAllStructureIdsInView() {

                            return null;
                        }

                        @Override
                        public CmsObject getCms() {

                            return loginCms;
                        }

                        @Override
                        public void start(String title, Component dialog, DialogWidth style) {

                            if (dialog != null) {
                                m_window = CmsBasicDialog.prepareWindow(style);
                                m_window.setCaption(title);
                                m_window.setContent(dialog);
                                UI.getCurrent().addWindow(m_window);
                                if (dialog instanceof CmsBasicDialog) {
                                    ((CmsBasicDialog)dialog).initActionHandler(m_window);
                                }
                            }
                        }

                        public void updateUserInfo() {

                            // not supported
                        }
                    };
                    CmsUser u = currentCms.readUser(userNonNull.getId());
                    u.setAdditionalInfo(
                        CmsUserSettings.ADDITIONAL_INFO_LAST_USER_DATA_CHECK,
                        Long.toString(System.currentTimeMillis()));
                    loginCms.writeUser(u);
                    CmsUserDataDialog dialog = new CmsUserDataDialog(context, true);
                    context.start(dialog.getTitle(UI.getCurrent().getLocale()), dialog);
                } else {
                    initSessionAndSendMessages(currentCms, loginCms);
                    m_ui.openLoginTarget(loginTarget, isPublicPC);
                }
            };
            LoginContext context = new LoginContext();
            context.setUser(userNonNull);
            context.setCms(currentCms);
            if (m_otpHandler.needsTwoFactorAuthentication(userNonNull)) {
                m_ui.clearError();
                if (!m_otpHandler.hasSecondFactor(userObj)) {
                    showSecondFactorSetup(context, loginContinuation);
                } else {
                    showSecondFactorDialog(context, loginContinuation);
                }
            } else {
                loginContinuation.continueLogin(context);
            }
        } catch (Exception e) {

            handleError(currentCms, realUser, e);
        }
    }

    /**
     * Called on initialization.<p>
     */
    public void onInit() {

        String authToken = m_params.getAuthToken();
        if (authToken != null) {
            m_ui.showForgotPasswordView(authToken);
        } else if (m_params.isReset()) {
            m_ui.showPasswordResetDialog(m_params.getOufqn());
        } else {
            boolean loggedIn = !A_CmsUI.getCmsObject().getRequestContext().getCurrentUser().isGuestUser();
            m_ui.setSelectableOrgUnits(CmsLoginHelper.getOrgUnitsForLoginDialog(A_CmsUI.getCmsObject(), null));
            if (loggedIn) {
                if (m_params.isLogout()) {
                    logout();
                } else {
                    m_ui.showAlreadyLoggedIn();
                }
            } else {
                m_ui.showLoginView(m_params.getOufqn());
            }
        }

    }

    /**
     * Sets the login ui reference.<p>
     *
     * @param ui the login ui
     */
    public void setUi(CmsLoginUI ui) {

        m_ui = ui;
    }

    /**
     * Returns the message to be displayed for the user data check dialog.<p>
     *
     * @return the message to display
     */
    protected String getPasswordChangeMessage() {

        ResourceBundle bundle = null;
        try {
            bundle = CmsResourceBundleLoader.getBundle("org.opencms.passwordchange.custom", A_CmsUI.get().getLocale());
            return bundle.getString("passwordchange.text");
        } catch (MissingResourceException e) {
            return CmsVaadinUtils.getMessageText(Messages.GUI_PWCHANGE_INTERVAL_EXPIRED_0);
        }
    }

    /**
     * Handles exceptions during the login process and displays appropriate error messages.
     *
     * @param currentCms the CMS context
     * @param user the user being logged in
     * @param e the error
     */
    protected void handleError(CmsObject currentCms, String user, Exception e) {

        CmsMessageContainer message = null;

        // there was an error during login
        if (e instanceof CmsException) {
            CmsMessageContainer exceptionMessage = ((CmsException)e).getMessageContainer();
            if (org.opencms.security.Messages.ERR_LOGIN_FAILED_DISABLED_2 == exceptionMessage.getKey()) {
                // the user account is disabled
                message = org.opencms.workplace.Messages.get().container(
                    org.opencms.workplace.Messages.GUI_LOGIN_FAILED_DISABLED_0);
            } else if (org.opencms.security.Messages.ERR_LOGIN_FAILED_TEMP_DISABLED_4 == exceptionMessage.getKey()) {
                // the user account is temporarily disabled because of too many login failures
                message = org.opencms.workplace.Messages.get().container(
                    org.opencms.workplace.Messages.GUI_LOGIN_FAILED_TEMP_DISABLED_0);
            } else if (org.opencms.security.Messages.ERR_LOGIN_FAILED_WITH_MESSAGE_1 == exceptionMessage.getKey()) {
                // all logins have been disabled be the Administration
                CmsLoginMessage loginMessage2 = OpenCms.getLoginManager().getLoginMessage();
                if (loginMessage2 != null) {
                    message = org.opencms.workplace.Messages.get().container(
                        org.opencms.workplace.Messages.GUI_LOGIN_FAILED_WITH_MESSAGE_1,
                        loginMessage2.getMessage().replace("\n", ""));
                }
            }
        }
        if (message == null) {
            if (e instanceof CmsCustomLoginException) {
                message = ((CmsCustomLoginException)e).getMessageContainer();
            } else {
                // any other error - display default message
                message = org.opencms.workplace.Messages.get().container(
                    org.opencms.workplace.Messages.GUI_LOGIN_FAILED_0);
                LOG.warn(e.getLocalizedMessage(), e);
                displayError(message.key(m_params.getLocale()), true, true);
                CmsUserLog.logLoginFailure(currentCms, user);
                return;
            }
        }

        if (e instanceof CmsException) {
            CmsJspLoginBean.logLoginException(currentCms.getRequestContext(), user, (CmsException)e);
            CmsUserLog.logLoginFailure(currentCms, user);
        } else {
            LOG.error(e.getLocalizedMessage(), e);
        }
        displayError(message.key(m_params.getLocale()), false, false);
    }

    /**
     * Switches the session to a new one with the logged in CmsObject.
     *
     * <p>This needs to be called in the <em>last</em> request to the Vaadin servlet in the login process, because switching the session breaks the Vaadin session state.
     *
     * @param currentCms the CmsObject for the current request from the Vaadin UI
     * @param loginCms the CmsObject which was used for the actual login operation
     */
    protected void initSessionAndSendMessages(CmsObject currentCms, CmsObject loginCms) {

        HttpSession session = ((HttpServletRequest)VaadinService.getCurrentRequest()).getSession(false);
        if (session != null) {
            session.invalidate();
        }
        session = ((HttpServletRequest)VaadinService.getCurrentRequest()).getSession(true);

        // we don't want currentCms to be used to automatically update the session at the end of the request...
        currentCms.getRequestContext().setUpdateSessionEnabled(false);

        // ...instead we manually update the session with loginCms
        loginCms.getRequestContext().setUpdateSessionEnabled(true);
        OpenCms.getSessionManager().updateSessionInfo(loginCms, (HttpServletRequest)VaadinService.getCurrentRequest());

        String storedMessage = null;
        CmsLoginMessage loginMessage = OpenCms.getLoginManager().getLoginMessage();
        if (loginMessage != null) {
            // forbidden implies active
            if (loginMessage.isLoginCurrentlyForbidden()) {
                // we are an administrator, otherwise login would have failed
                if (loginMessage.getTimeEnd() == CmsLoginMessage.DEFAULT_TIME_END) {
                    storedMessage = org.opencms.workplace.Messages.get().container(
                        org.opencms.workplace.Messages.GUI_LOGIN_SUCCESS_WITH_MESSAGE_WITHOUT_TIME_1,
                        loginMessage.getMessage(),
                        new Date(loginMessage.getTimeEnd())).key(A_CmsUI.get().getLocale());
                } else {
                    storedMessage = org.opencms.workplace.Messages.get().container(
                        org.opencms.workplace.Messages.GUI_LOGIN_SUCCESS_WITH_MESSAGE_2,
                        loginMessage.getMessage(),
                        new Date(loginMessage.getTimeEnd())).key(A_CmsUI.get().getLocale());
                }
            } else if (loginMessage.isActive()) {
                storedMessage = loginMessage.getMessage();
            }
        }

        if (storedMessage != null) {
            OpenCms.getSessionManager().sendBroadcast(
                null,
                storedMessage,
                loginCms.getRequestContext().getCurrentUser(),
                ContentMode.html);
        }
    }

    /**
     * Gets the CMS context.<p>
     *
     * @return the CMS context
     */
    CmsObject getCms() {

        return m_adminCms;
    }

    /**
     * Displays the given error message.<p>
     *
     * @param message the message
     * @param showForgotPassword in case the forgot password link should be shown
     * @param showTime show the time
     */
    private void displayError(String message, boolean showForgotPassword, boolean showTime) {

        message = message.replace("\n", "<br />");
        if (showForgotPassword) {
            message += "<br /><br /><a href=\""
                + getResetPasswordLink()
                + "\">"
                + CmsVaadinUtils.getMessageText(Messages.GUI_FORGOT_PASSWORD_0)
                + "</a>";
        }
        if (showTime) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            message += "<div style=\"position:absolute;right:6px;bottom:5px;\">"
                + CmsVaadinUtils.getMessageText(Messages.GUI_TIME_1, sdf.format(new Date()))
                + "</div>";
        }
        m_ui.showLoginError(message);
    }

    /**
     * Shows the verification dialog for 2FA.
     *
     * @param context the login context
     * @param loginContinuation the handler to which we pass the verification code to continue with the login
     */
    private void showSecondFactorDialog(LoginContext context, LoginContinuation loginContinuation) {

        Window window = CmsBasicDialog.prepareWindow(DialogWidth.narrow);
        window.setClosable(false);
        window.setResizable(false);
        window.setCaption(CmsSecondFactorDialog.getCaption(context.getUser()));
        CmsSecondFactorDialog dialog = new CmsSecondFactorDialog(context.getUser(), verificationCode -> {
            context.setSecondFactorInfo(new CmsSecondFactorInfo(verificationCode));
            try {
                loginContinuation.continueLogin(context);
            } catch (Exception e) {
                handleError(context.getCms(), context.getUser().getName(), e);
            }
        });
        A_CmsUI.get().addWindow(window);
        window.setContent(dialog);

    }

    /**
     * Shows a dialog for setting up 2FA.
     *
     * @param context the login context
     * @param loginContinuation the handler we call with the secret and verification code to set up 2FA and proceed with the loin.
     */
    private void showSecondFactorSetup(LoginContext context, LoginContinuation loginContinuation) {

        Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
        window.setClosable(false);
        window.setResizable(false);
        CmsSecondFactorSetupDialog dialog = new CmsSecondFactorSetupDialog(context, context2 -> {
            try {
                loginContinuation.continueLogin(context);
            } catch (Exception e) {
                handleError(context.getCms(), context.getUser().getName(), e);
            }
        });
        window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_LOGIN_2FA_SETUP_0));
        A_CmsUI.get().addWindow(window);
        window.setContent(dialog);

    }
}

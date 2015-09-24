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

import org.opencms.db.CmsLoginMessage;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.jsp.CmsJspLoginBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsCustomLoginException;
import org.opencms.security.CmsRole;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.Messages;
import org.opencms.ui.login.CmsLoginHelper.LoginParameters;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsFrameset;
import org.opencms.workplace.CmsLoginUserAgreement;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceSettings;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.server.VaadinServletResponse;
import com.vaadin.ui.UI;

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
     * Helper subclass of CmsLoginUserAgreement which can be used without a page context.<p>
     *
     * This is only used for detecting whether we need to display the user agreement dialog, not for displaying the dialog itself.<p>
     */
    protected class UserAgreementHelper extends CmsLoginUserAgreement {

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

    /** Additional info key to mark accounts as locked due to inactivity. */
    public static final String KEY_ACCOUNT_LOCKED = "accountLocked";

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLoginController.class);

    /** The administrator CMS context. */
    private CmsObject m_adminCms;

    /** The parameters collected when the login app was opened. */
    private LoginParameters m_params;

    /** The UI instance. */
    private I_CmsLoginUI m_ui;

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
     * Logs the current user out by invalidating the session an reloading the current URI.<p>
     * Important:  This works only within vaadin apps.<p>
     */
    public static void logout() {

        CmsObject cms = A_CmsUI.getCmsObject();
        UI.getCurrent().getSession().close();
        String loginLink = OpenCms.getLinkManager().substituteLinkForUnknownTarget(cms, "/system/login", false);
        VaadinService.getCurrentRequest().getWrappedSession().invalidate();
        Page.getCurrent().setLocation(loginLink);
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

        String link = OpenCms.getLinkManager().substituteLinkForUnknownTarget(
            CmsLoginUI.m_adminCms,
            "/system/login", //$NON-NLS-1$
            false) + "?" + CmsLoginHelper.PARAM_RESET_PASSWORD;
        A_CmsUI.get().getPage().setLocation(link);
    }

    /**
     * Called when the user clicks on the login button.<p>
     */
    public void onClickLogin() {

        String user = m_ui.getUser();
        String password = m_ui.getPassword();
        CmsMessageContainer message = CmsLoginHelper.validateUserAndPasswordNotEmpty(user, password);
        CmsLoginMessage loginMessage = OpenCms.getLoginManager().getLoginMessage();
        String storedMessage = null;
        if ((loginMessage != null) && !loginMessage.isLoginCurrentlyForbidden() && loginMessage.isEnabled()) {
            storedMessage = loginMessage.getMessage();
            // If login is forbidden, we will get an error message anyway, so we don't need to store the message here
        }
        if (message != null) {
            String errorMesssage = message.key(m_params.getLocale());
            m_ui.displayError(errorMesssage);
            return;
        }

        String ou = m_ui.getOrgUnit();
        String realUser = CmsStringUtil.joinPaths(ou, user);
        String pcType = m_ui.getPcType();
        CmsObject currentCms = A_CmsUI.getCmsObject();
        CmsUser userObj = null;

        try {
            try {
                userObj = currentCms.readUser(realUser);
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
                message = org.opencms.workplace.Messages.get().container(
                    org.opencms.workplace.Messages.GUI_LOGIN_FAILED_0);
                m_ui.displayError(message.key(m_params.getLocale()));
                return;
            }
            if (OpenCms.getLoginManager().canLockBecauseOfInactivity(currentCms, userObj)) {
                boolean locked = null != userObj.getAdditionalInfo().get(KEY_ACCOUNT_LOCKED);
                if (locked) {
                    A_CmsUI.get().setError(CmsInactiveUserMessages.getLockoutText(A_CmsUI.get().getLocale()));
                    return;
                }
            }
            if (OpenCms.getLoginManager().requiresPasswordChange(currentCms, userObj)) {
                CmsChangePasswordDialog passwordDialog = new CmsChangePasswordDialog(
                    currentCms,
                    userObj,
                    A_CmsUI.get().getLocale());
                A_CmsUI.get().setContentToDialog(
                    Messages.get().getBundle(A_CmsUI.get().getLocale()).key(Messages.GUI_PWCHANGE_HEADER_0)
                        + userObj.getSimpleName(),
                    passwordDialog);
                return;
            }
            currentCms.loginUser(realUser, password);
            OpenCms.getSessionManager().updateSessionInfo(
                currentCms,
                (HttpServletRequest)VaadinService.getCurrentRequest());

            if (storedMessage != null) {
                OpenCms.getSessionManager().sendBroadcast(
                    null,
                    storedMessage,
                    currentCms.getRequestContext().getCurrentUser());
            }
            CmsWorkplaceSettings settings = CmsLoginHelper.initSiteAndProject(currentCms);

            CmsLoginHelper.setCookieData(
                pcType,
                user,
                ou,
                (VaadinServletRequest)(VaadinService.getCurrentRequest()),
                (VaadinServletResponse)(VaadinService.getCurrentResponse()));
            VaadinService.getCurrentRequest().getWrappedSession().setAttribute(
                CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS,
                settings);

            String loginTarget = getLoginTarget(currentCms, settings);

            CmsLoginTargetInfo targetInfo = new CmsLoginTargetInfo(loginTarget, user, password);
            m_ui.openLoginTarget(targetInfo);

        } catch (Exception e) {

            // there was an error during login
            if (e instanceof CmsException) {
                CmsMessageContainer exceptionMessage = ((CmsException)e).getMessageContainer();
                if (org.opencms.security.Messages.ERR_LOGIN_FAILED_DISABLED_2 == exceptionMessage.getKey()) {
                    // the user account is disabled
                    message = org.opencms.workplace.Messages.get().container(
                        org.opencms.workplace.Messages.GUI_LOGIN_FAILED_DISABLED_0);
                } else
                    if (org.opencms.security.Messages.ERR_LOGIN_FAILED_TEMP_DISABLED_4 == exceptionMessage.getKey()) {
                    // the user account is temporarily disabled because of too many login failures
                    message = org.opencms.workplace.Messages.get().container(
                        org.opencms.workplace.Messages.GUI_LOGIN_FAILED_TEMP_DISABLED_0);
                } else if (org.opencms.security.Messages.ERR_LOGIN_FAILED_WITH_MESSAGE_1 == exceptionMessage.getKey()) {
                    // all logins have been disabled be the Administration
                    CmsLoginMessage loginMessage2 = OpenCms.getLoginManager().getLoginMessage();
                    if (loginMessage2 != null) {
                        message = org.opencms.workplace.Messages.get().container(
                            org.opencms.workplace.Messages.GUI_LOGIN_FAILED_WITH_MESSAGE_1,
                            loginMessage2.getMessage());
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
                }
            }

            m_ui.displayError(message.key(m_params.getLocale()));
            if (e instanceof CmsException) {
                CmsJspLoginBean.logLoginException(currentCms.getRequestContext(), user, (CmsException)e);
            } else {
                LOG.error(e.getLocalizedMessage(), e);
            }

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
            m_ui.showPasswordResetDialog();
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
    public void setUi(I_CmsLoginUI ui) {

        m_ui = ui;
    }

    /**
     * Gets the login target link.<p>
     *
     * @param currentCms the current CMS context
     * @param settings the workplace settings
     * @return the login target
     */
    protected String getLoginTarget(CmsObject currentCms, CmsWorkplaceSettings settings) throws CmsException {

        m_params.getLocale();
        String directEditPath = CmsLoginHelper.getDirectEditPath(currentCms, settings.getUserSettings());
        String target = "";
        boolean checkRole = false;
        if (m_params.getRequestedWorkplaceApp() != null) {

            target = m_params.getRequestedWorkplaceApp();
            // we need to read the URI fragment from the current page, as it is not available in the servlet request
            String fragment = UI.getCurrent().getPage().getUriFragment();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(fragment)) {
                target += "#" + UI.getCurrent().getPage().getUriFragment();
            }
            checkRole = true;
        } else {
            boolean workplace2 = false;

            if (CmsFrameset.JSP_WORKPLACE_URI.equals(m_params.getRequestedResource())
                && settings.getUserSettings().getStartView().equals(CmsWorkplace.VIEW_WORKPLACE_2)) {
                workplace2 = true;
                checkRole = true;
            } else if ((m_params.getRequestedResource() != null)
                && !CmsFrameset.JSP_WORKPLACE_URI.equals(m_params.getRequestedResource())) {
                target = m_params.getRequestedResource();
            } else if (directEditPath != null) {
                target = directEditPath;
            } else {
                target = CmsFrameset.JSP_WORKPLACE_URI;
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
            if (workplace2) {
                target = CmsVaadinUtils.getWorkplaceLink();
            } else {
                target = OpenCms.getLinkManager().substituteLink(currentCms, target);
            }
        }
        if (checkRole && !OpenCms.getRoleManager().hasRole(currentCms, CmsRole.WORKPLACE_USER)) {
            throw new CmsCustomLoginException(
                org.opencms.workplace.Messages.get().container(
                    org.opencms.workplace.Messages.GUI_LOGIN_FAILED_NO_WORKPLACE_PERMISSIONS_0));
        }
        return target;
    }

    /**
     * Gets the CMS context.<p>
     *
     * @return the CMS context
     */
    CmsObject getCms() {

        return m_adminCms;
    }
}

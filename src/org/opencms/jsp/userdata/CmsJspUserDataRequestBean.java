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

package org.opencms.jsp.userdata;

import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAuthentificationException;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsStringUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.mail.internet.AddressException;

import org.apache.commons.logging.Log;
import org.apache.commons.mail.EmailException;

/**
 * Bean used by the dynamic function JSP for user data requests.<p>
 *
 * The dynamic function calls the action() method on this bean, which returns the new state for the JSP to render.
 * Error/status messages are also available to the JSP via getters.
 */
public class CmsJspUserDataRequestBean {

    /**
     * Represents the UI state which should be shown by the dynamic function JSP.
     */
    enum State {
        /** Generic error occurred that can't be meaningfully displayed in another state. */
        error,

        /** State showing the data request form with user / email / password fields, and possibly an error message. */
        form,
        /** Success state after submitting the form. */
        formOk,

        /** State which is shown when the link from the email is opened. */
        view,

        /** State which is shown when the user confirms their credentials after clicking on the email link. */
        viewOk
    }

    /** Action id. */
    public static final String ACTION_REQUEST = "request";

    /** Action id. */
    public static final String ACTION_VIEW = "view";

    /** Action id. */
    public static final String ACTION_VIEWAUTH = "viewauth";

    /** Request parameter for the action. */
    public static final String PARAM_ACTION = "action";

    /** Request parameter for the authorization code. */
    public static final String PARAM_AUTH = "auth";

    /** Request parameter. */
    public static final String PARAM_EMAIL = "email";

    /** Request parameter. */
    public static final String PARAM_PASSWORD = "password";

    /** Request parameter. */
    public static final String PARAM_ROOTPATH = "rootpath";

    /** Request parameter. */
    public static final String PARAM_UDRID = "udrid";

    /** Request parameter. */
    public static final String PARAM_USER = "user";

    /** The logger instance for the class. */
    private static final Log LOG = CmsLog.getLog(CmsJspUserDataRequestBean.class);

    /** The configuration. */
    private CmsUserDataRequestConfig m_config;

    /** The download link (only available if credentials have been confirmed). */
    private String m_downloadLink;

    /** Error HTML (only shown directly in error state). */
    private String m_errorHtml;

    /** The user data request info. */
    private CmsUserDataRequestInfo m_info;

    /** The user data HTML (only available if credentials have been confirmed). */
    private String m_infoHtml;

    /** The request parameters (duplicate parameters are removed). */
    private Map<String, String> m_params;

    /** Lazy map to access messages. */
    private Map<String, String> m_texts;

    /**
     * Creates a new instance.
     */
    public CmsJspUserDataRequestBean() {

        LOG.debug("Creating user data request bean.");
    }

    /**
     * Called by the user data request function JSP to handle the user data request logic.
     *
     *  Returns the next state which should be shown by the JSP.
     *
     * @param cms the CMS context
     * @param reqParameters the request parameters
     * @return the next state to render
     *
     * @throws CmsException if something goes wrong
     */
    public String action(CmsObject cms, Map<String, String[]> reqParameters) throws CmsException {

        init(reqParameters);

        CmsResource page = cms.readResource(cms.getRequestContext().getUri());
        List<CmsProperty> props = cms.readPropertyObjects(page, true);

        CmsProperty defaultOuProp = CmsProperty.get(CmsPropertyDefinition.PROPERTY_UDR_DEFAULTOU, props);
        String configuredOu = null;
        if (defaultOuProp != null) {

            configuredOu = defaultOuProp.getValue();
        }
        CmsProperty configPathProp = CmsProperty.get(CmsPropertyDefinition.PROPERTY_UDR_CONFIG, props);
        String configPath = configPathProp.getValue();

        CmsUserDataRequestManager manager = OpenCms.getUserDataRequestManager();
        CmsMessages messages = Messages.get().getBundle(cms.getRequestContext().getLocale());
        m_config = manager.loadConfig(cms, configPath).orElse(null);
        if (m_config == null) {
            m_errorHtml = messages.key(Messages.ERR_CONFIG_NOT_SET_0);
            return State.error.toString();
        }
        String functionDetail = CmsJspStandardContextBean.getFunctionDetailLink(
            cms,
            CmsDetailPageInfo.FUNCTION_PREFIX,
            CmsUserDataRequestManager.FUNCTION_NAME,
            true);
        if (functionDetail.contains("[")) {
            m_errorHtml = messages.key(Messages.ERR_FUNCTION_DETAIL_PAGE_NOT_SET_0);
            return State.error.toString();
        }

        if (!CmsUserDataResourceHandler.isInitialized()) {
            m_errorHtml = messages.key(Messages.ERR_RESOURCE_INIT_HANDLER_NOT_CONFIGURED_0);
            return State.error.toString();
        }
        String email = m_params.get(PARAM_EMAIL);
        String user = m_params.get(PARAM_USER);
        String password = m_params.get(PARAM_PASSWORD);
        String path = m_params.get(PARAM_ROOTPATH);
        String udrid = CmsEncoder.escapeXml(m_params.get(PARAM_UDRID));
        boolean hasEmail = !CmsStringUtil.isEmptyOrWhitespaceOnly(email);
        boolean hasUser = !CmsStringUtil.isEmptyOrWhitespaceOnly(user);
        boolean hasPassword = !CmsStringUtil.isEmptyOrWhitespaceOnly(password);
        String action = CmsEncoder.escapeXml(m_params.get(PARAM_ACTION));
        if (!CmsStringUtil.isEmpty(udrid)) {
            m_info = manager.getRequestStore().load(udrid).orElse(null);
        }
        if (action == null) {
            return State.form.toString();
        } else if (ACTION_REQUEST.equals(action)) {
            try {
                if (hasEmail && !hasUser && !hasPassword) {
                    manager.startUserDataRequest(cms, m_config, email);
                    return State.formOk.toString();
                } else if (!hasEmail && hasUser && hasPassword) {
                    if (CmsStringUtil.isEmpty(path)) {
                        path = cms.getRequestContext().addSiteRoot(cms.getRequestContext().getUri());
                    } else {
                        path = path.trim();
                    }
                    Optional<CmsUser> optUser = lookupUser(cms, configuredOu, path, user, password);
                    if (optUser.isPresent()) {
                        manager.startUserDataRequest(cms, m_config, optUser.get());
                        return State.formOk.toString();
                    } else {
                        throw new CmsUserDataRequestException("Could not find user.");
                    }
                } else {
                    m_errorHtml = "invalid combination of parameters.";
                    return State.form.toString();
                }
            } catch (CmsUserDataRequestException e) {
                m_errorHtml = e.getLocalizedMessage();
                return State.form.toString();
            } catch (EmailException | AddressException e) {
                LOG.warn(e.getLocalizedMessage(), e);
                m_errorHtml = m_config.getText("EmailError");
                return State.error.toString();
            }
        } else if (ACTION_VIEW.equals(action)) {
            CmsUserDataRequestInfo info = manager.getRequestStore().load(udrid).orElse(null);
            if ((info == null) || info.isExpired()) {
                m_errorHtml = m_config.getText("InvalidLink");
                return State.error.toString();
            }
            return State.view.toString();
        } else if (ACTION_VIEWAUTH.equals(action)) {
            CmsUserDataRequestStore store = manager.getRequestStore();
            CmsUserDataRequestInfo info = store.load(udrid).orElse(null);
            if ((info == null) || info.isExpired()) {
                m_errorHtml = m_config.getText("InvalidLink");
                return State.error.toString();
            }
            m_infoHtml = info.getInfoHtml();
            boolean origForceAbsolute = cms.getRequestContext().isForceAbsoluteLinks();
            cms.getRequestContext().setForceAbsoluteLinks(true);
            try {
                m_downloadLink = OpenCms.getLinkManager().substituteLinkForUnknownTarget(
                    cms,
                    CmsUserDataResourceHandler.PREFIX + info.getId() + "?" + PARAM_AUTH + "=" + info.getAuthCode());
            } finally {
                cms.getRequestContext().setForceAbsoluteLinks(origForceAbsolute);
            }
            CmsUserDataRequestType type = info.getType();
            if (CmsUserDataRequestType.email.equals(type)) {
                if (email == null) {
                    return State.view.toString();
                } else if (email.equals(info.getEmail())) {
                    return State.viewOk.toString();
                } else {
                    m_errorHtml = "email address error";
                    return State.view.toString();
                }
            } else if (CmsUserDataRequestType.singleUser.equals(type)) {
                if (hasUser && hasPassword) {
                    try {
                        CmsObject cloneCms = OpenCms.initCmsObject(cms);
                        String fullName = info.getUserName();
                        CmsUser readUser = cms.readUser(fullName);
                        if (!readUser.getSimpleName().equals(user)) {
                            m_errorHtml = "login error";
                            return State.view.toString();
                        }
                        cloneCms.loginUser(info.getUserName(), password);
                        return State.viewOk.toString();
                    } catch (CmsException e) {
                        LOG.info(e.getLocalizedMessage(), e);
                        m_errorHtml = "login error";
                        return State.view.toString();
                    }

                }
                m_errorHtml = "authentication error";
                return State.view.toString();
            }

        } else {
            m_errorHtml = "Invalid action: " + action;
            LOG.info("Invalid action: " + action);
            return State.error.toString();
        }
        return State.form.toString();

    }

    /**
     * Gets the user data download link.
     *
     * @return the user data download link
     */
    public String getDownloadLink() {

        return m_downloadLink;
    }

    /**
     * Gets the error HTML (this is only shown in the error state).
     *
     * @return the error HTML
     */
    public String getErrorHtml() {

        return m_errorHtml;
    }

    /**
     * Gets the user data HTML.<p>
     *
     * @return the user data HTML
     */
    public String getInfoHtml() {

        return m_infoHtml;
    }

    /**
     * Gets the lazy map used to access the configurable texts to display by the user data request dynamic function.
     *
     * @return the lazy map
     */
    public Map<String, String> getTexts() {

        if (m_texts == null) {
            m_texts = CmsCollectionsGenericWrapper.createLazyMap(obj -> {
                String key = (String)obj;
                return m_config.getText(key);

            });
        }
        return m_texts;

    }

    /**
     * Checks if the error HTML has been set.
     *
     * @return true if the error HTML has been set
     */
    public boolean isError() {

        return m_errorHtml != null;
    }

    /**
     * Checks if we need to confirm the user's email rather than their user name and password.
     *
     * @return true if we only need the email address
     */
    public boolean isOnlyEmailRequired() {

        return CmsUserDataRequestType.email == m_info.getType();
    }

    /**
     * Initializes the request parameters by throwing out duplicates.
     *
     * @param requestParams the request parameters
     */
    private void init(Map<String, String[]> requestParams) {

        m_params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            String[] vals = entry.getValue();
            if (vals.length > 0) {
                String val = vals[0];
                m_params.put(entry.getKey(), val);
            }
        }
    }

    /**
     * Looks up the 'best' user for the given user name and password.
     *
     * <p>The 'best' in this case is found by walking up the OUs, from the most specific to the root OU, which contain the current URI
     * and trying to log in the user with the given name and OU in each of them. The first OU+User combination for which the login is successful
     * is returned.
     *
     * @param cms the CMS context
     * @param configuredOu the configured OU
     * @param path the path to use for looking up the OU if the OU is not given
     * @param user the user
     * @param password the password
     *
     * @return the found user
     *
     * @throws CmsException if something goes wrong
     */
    private Optional<CmsUser> lookupUser(CmsObject cms, String configuredOu, String path, String user, String password)
    throws CmsException {

        List<CmsOrganizationalUnit> ous;
        if (configuredOu != null) {
            ous = Collections.singletonList(OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, configuredOu));
        } else {
            ous = OpenCms.getOrgUnitManager().getOrgUnitsForResource(cms, path);
        }
        CmsObject loginCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
        for (CmsOrganizationalUnit ou : ous) {
            String fullName = CmsStringUtil.joinPaths(ou.getName(), user);
            try {
                loginCms.loginUser(fullName, password);
                return Optional.of(loginCms.getRequestContext().getCurrentUser());
            } catch (CmsAuthentificationException e) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }
        return Optional.empty();
    }

}

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

package org.opencms.workplace;

import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsUser;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods to show a configurable user agreement dialog after a successful workplace login.<p>
 *
 * @since 8.0
 */
public class CmsLoginUserAgreement extends CmsDialog {

    /** Value for the action: accept the user agreement. */
    public static final int ACTION_ACCEPT = 100;

    /** The dialog type. */
    public static final String DIALOG_TYPE = "useragreement";

    /** Node name for the element: MessageDeclined. */
    public static final String NODE_MESSAGE_DECLINED = "MessageDeclined";

    /** Node name for the element: Text. */
    public static final String NODE_TEXT = "Text";

    /** Request parameter name for the originally requested resource. */
    public static final String PARAM_WPRES = "wpres";

    /** JSON key name to store the count of the accepted agreement. */
    protected static final String KEY_ACCEPTED_COUNT = "count";

    /** JSON key name to store the version of the accepted agreement. */
    protected static final String KEY_ACCEPTED_VERSION = "version";

    /** Node name for the element: AgreeCount. */
    protected static final String NODE_AGREE_COUNT = "AgreeCount";

    /** Node name for the element: ButtonAccept. */
    protected static final String NODE_BUTTON_ACCEPT = "ButtonAccept";

    /** Node name for the element: ButtonDecline. */
    protected static final String NODE_BUTTON_DECLINE = "ButtonDecline";

    /** Node name for the element: DialogTitle. */
    protected static final String NODE_DIALOG_TITLE = "DialogTitle";

    /** Node name for the element: Version. */
    protected static final String NODE_VERSION = "Version";

    /** The VFS path to the folder containing the user agreement configuration files. */
    protected static final String VFS_PATH_CONFIGFOLDER = CmsWorkplace.VFS_PATH_SYSTEM + "login/useragreement/";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLoginUserAgreement.class);

    /** The number of times the user accepted the agreement. */
    private int m_acceptedCount;

    /** The version of the user accepted agreement. */
    private double m_acceptedVersion;

    /** The user agreement configuration content. */
    private CmsXmlContent m_configurationContent;

    /** The originally requested workplace resource path parameter. */
    private String m_paramWpres;

    /** The required version of the user accepted agreement. */
    private double m_requiredVersion;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsLoginUserAgreement(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsLoginUserAgreement(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Stores the information about the accepted user agreement in the current users additional info.<p>
     */
    public void acceptAgreement() {

        // store accepted flag in current users settings
        getSettings().setUserAgreementAccepted(true);
        // check accepted agreement version
        if (getAcceptedVersion() < getRequiredVersion()) {
            // a new (higher) version was accepted, increase version to store and reset accepted count
            setAcceptedVersion(getRequiredVersion());
            setAcceptedCount(0);
        }
        // increase accepted count
        setAcceptedCount(getAcceptedCount() + 1);
        // create the JSON data structure that is stored in the additional info
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put(KEY_ACCEPTED_VERSION, getRequiredVersion());
            jsonData.put(KEY_ACCEPTED_COUNT, m_acceptedCount);
            // store accepted data in users additional information
            CmsUser user = getCms().getRequestContext().getCurrentUser();
            user.setAdditionalInfo(CmsUserSettings.LOGIN_USERAGREEMENT_ACCEPTED, jsonData.toString());
            // write the changed user
            getCms().writeUser(user);
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    /**
     * Performs the the user agreement accept action, will be called by the JSP page.<p>
     *
     * @throws IOException if problems while redirecting occur
     */
    public void actionAccept() throws IOException {

        acceptAgreement();
        // redirect to the originally requested resource
        getJsp().getResponse().sendRedirect(getJsp().link(getParamWpres()));
    }

    /**
     * Performs the user agreement declined action, will be called by the JSP page.<p>
     *
     * @throws IOException if problems while redirecting occur
     */
    public void actionDecline() throws IOException {

        getJsp().getRequest().getSession().invalidate();
        getJsp().getResponse().sendRedirect(getJsp().link(getParamWpres()));
    }

    /**
     * The standard JavaScript for submitting the dialog is overridden to show an alert in case that
     * an agreement is declined.<p>
     *
     * See also {@link CmsDialog#dialogScriptSubmit()}
     *
     * @return the standard JavaScript for submitting the dialog
     */
    @Override
    public String dialogScriptSubmit() {

        if (useNewStyle()) {
            return super.dialogScriptSubmit();
        }
        StringBuffer result = new StringBuffer(512);
        result.append("function submitAction(actionValue, theForm, formName) {\n");
        result.append("\tif (theForm == null) {\n");
        result.append("\t\ttheForm = document.forms[formName];\n");
        result.append("\t}\n");
        result.append("\ttheForm." + PARAM_FRAMENAME + ".value = window.name;\n");
        result.append("\tif (actionValue == \"" + DIALOG_OK + "\") {\n");
        result.append("\t\treturn true;\n");
        result.append("\t}");
        String declinedMessage = getConfigurationContentStringValue(NODE_MESSAGE_DECLINED);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(declinedMessage)) {
            // add the alert to show the declined message
            result.append(" else if (actionValue == \"" + DIALOG_CANCEL + "\") {\n");
            result.append("\t\talert(\"");
            result.append(CmsStringUtil.escapeJavaScript(declinedMessage));
            result.append("\");\n");
            result.append("\t}\n");
        }
        result.append("\ttheForm." + PARAM_ACTION + ".value = actionValue;\n");
        result.append("\ttheForm.submit();\n");
        result.append("\treturn false;\n");
        result.append("}\n");

        return result.toString();
    }

    /**
     * Returns the number of times the user accepted the agreement.<p>
     *
     * @return the number of times the user accepted the agreement
     */
    public int getAcceptedCount() {

        return m_acceptedCount;
    }

    /**
     * Returns the version of the user accepted agreement.<p>
     *
     * @return the version of the user accepted agreement
     */
    public double getAcceptedVersion() {

        return m_acceptedVersion;
    }

    /**
     * Returns the content value of the given path as String.<p>
     *
     * @param path the path to get the content value for
     *
     * @return the content value of the given path as String
     */
    public String getConfigurationContentStringValue(String path) {

        if (getConfigurationContent() != null) {
            return getConfigurationContent().getStringValue(getCms(), path, getLocale());
        }
        return "";
    }

    /**
     * Returns the absolute path in the OpenCms VFS to the user agreement configuration file.<p>
     *
     * @return the absolute path in the OpenCms VFS to the user agreement configuration file
     */
    public String getConfigurationVfsPath() {

        return VFS_PATH_CONFIGFOLDER + getLocale().toString() + "/configuration.html";
    }

    /**
     * Returns the originally requested workplace resource path parameter.<p>
     *
     * @return the originally requested workplace resource path parameter
     */
    public String getParamWpres() {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_paramWpres) || "null".equals(m_paramWpres)) {
            return CmsWorkplace.JSP_WORKPLACE_URI;
        }
        return m_paramWpres;
    }

    /**
     * Returns the required version of the user accepted agreement.<p>
     *
     * @return the required version of the user accepted agreement
     */
    public double getRequiredVersion() {

        if (m_requiredVersion == 0) {
            String versionStr = getConfigurationContentStringValue(NODE_VERSION);
            try {
                m_requiredVersion = Double.parseDouble(versionStr);
            } catch (Exception e) {
                // the version number is not in the correct format
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.LOG_USERAGREEMENT_WRONG_VERSION_2,
                        versionStr,
                        getConfigurationContent().getFile().getRootPath()));
            }
        }
        return m_requiredVersion;
    }

    /**
     * Returns if the user agreement page should be shown for the current user.<p>
     *
     * @return <code>true</code> if the user agreement page should be shown for the current user, otherwise <code>false</code>
     */
    public boolean isShowUserAgreement() {

        if (!getSettings().isUserAgreementAccepted() && (getConfigurationContent() != null)) {
            CmsXmlContent content = getConfigurationContent();
            boolean enabled = false;
            try {
                // first read the property that contains the information if the agreement is enabled at all
                enabled = Boolean.valueOf(
                    getCms().readPropertyObject(
                        content.getFile(),
                        CmsPropertyDefinition.PROPERTY_LOGIN_FORM,
                        false).getValue()).booleanValue();
                if (enabled) {
                    // user agreement is enabled, now check version and accepted count
                    if (content.hasLocale(getLocale())) {
                        if (getAcceptedVersion() < getRequiredVersion()) {
                            // new user agreement version that has to be shown
                            return true;
                        } else {
                            // check how often the user accepted the user agreement
                            String countStr = content.getStringValue(getCms(), NODE_AGREE_COUNT, getLocale());
                            int count = Integer.parseInt(countStr);
                            if ((count == -1) || (getAcceptedCount() < count)) {
                                // user still has to accept the user agreement
                                return true;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // error when trying to determine if user agreement should be shown
                LOG.error(Messages.get().getBundle().key(
                        Messages.LOG_USERAGREEMENT_SHOW_1,
                    getConfigurationContent().getFile().getRootPath()), e);
            }

        }
        // store information that nothing has to be accepted in users session for better performance
        if (!getSettings().isUserAgreementAccepted()) {
            getSettings().setUserAgreementAccepted(true);
        }
        return false;
    }

    /**
     * Sets the number of times the user accepted the agreement.<p>
     *
     * @param acceptedCount the number of times the user accepted the agreement
     */
    public void setAcceptedCount(int acceptedCount) {

        m_acceptedCount = acceptedCount;
    }

    /**
     * Sets the version of the user accepted agreement.<p>
     *
     * @param acceptedVersion the version of the user accepted agreement
     */
    public void setAcceptedVersion(double acceptedVersion) {

        m_acceptedVersion = acceptedVersion;
    }

    /**
     * Sets the originally requested workplace resource path parameter.<p>
     *
     * @param paramWpres the originally requested workplace resource path parameter
     */
    public void setParamWpres(String paramWpres) {

        m_paramWpres = paramWpres;
    }

    /**
     * Sets the required version of the user accepted agreement.<p>
     *
     * @param requiredVersion the required version of the user accepted agreement
     */
    public void setRequiredVersion(double requiredVersion) {

        m_requiredVersion = requiredVersion;
    }

    /**
     * The standard "OK" and "Cancel" buttons are overridden to show other labels.<p>
     *
     * See also {@link CmsDialog#dialogButtonsHtml(StringBuffer, int, String)}
     *
     * @param result a string buffer where the rendered HTML gets appended to
     * @param button a integer key to identify the button
     * @param attribute an optional string with possible tag attributes, or null
     */
    @Override
    protected void dialogButtonsHtml(StringBuffer result, int button, String attribute) {

        attribute = appendDelimiter(attribute);

        switch (button) {
            case BUTTON_OK:
                result.append("<input name=\"ok\" value=\"");
                result.append(getConfigurationContentStringValue(NODE_BUTTON_ACCEPT));
                result.append("\"");
                if (attribute.toLowerCase().indexOf("onclick") == -1) {
                    result.append(" type=\"submit\"");
                } else {
                    result.append(" type=\"button\"");
                }
                result.append(" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            case BUTTON_CANCEL:
                result.append("<input name=\"cancel\" type=\"button\" value=\"");
                result.append(getConfigurationContentStringValue(NODE_BUTTON_DECLINE));
                result.append("\"");
                if (attribute.toLowerCase().indexOf("onclick") == -1) {
                    result.append(" onclick=\"submitAction('" + DIALOG_CANCEL + "', form);\"");
                }
                result.append(" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            default:
                // other buttons are not overridden, call super implementation
                super.dialogButtonsHtml(result, button, attribute);
        }
    }

    /**
     * Returns the user agreement configuration content.<p>
     *
     * @return the user agreement configuration content
     */
    protected CmsXmlContent getConfigurationContent() {

        if (m_configurationContent == null) {
            String configFileName = getConfigurationVfsPath();
            if (getCms().existsResource(configFileName)) {
                // configuration file found, check VFS cache for unmarshalled content
                CmsVfsMemoryObjectCache vfsCache = CmsVfsMemoryObjectCache.getVfsMemoryObjectCache();
                m_configurationContent = (CmsXmlContent)vfsCache.getCachedObject(getCms(), configFileName);
                if (m_configurationContent == null) {
                    // content not found in cache, read the file and unmarshal it
                    try {
                        CmsFile configFile = getCms().readFile(configFileName);
                        CmsXmlContent content = CmsXmlContentFactory.unmarshal(getCms(), configFile);
                        // put the result content in the cache
                        vfsCache.putCachedObject(getCms(), configFileName, content);
                        m_configurationContent = content;
                    } catch (CmsException e) {
                        // should never happen, because we checked the resource existence before
                    }
                }
            }
        }
        return m_configurationContent;
    }

    /**
     * Initializes the 'accepted' data from the current user.<p>
     * Returns the absolute path in the OpenCms VFS to the user agreement configuration file.<p>
     */
    protected void initAcceptData() {

        // read the current users agreement values
        CmsUser user = getCms().getRequestContext().getCurrentUser();
        String result = (String)user.getAdditionalInfo(CmsUserSettings.LOGIN_USERAGREEMENT_ACCEPTED);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result)) {
            // read JSON data structure that is stored in the user additional info
            try {
                JSONObject jsonData = new JSONObject(result);
                m_acceptedVersion = jsonData.getDouble(KEY_ACCEPTED_VERSION);
                m_acceptedCount = jsonData.getInt(KEY_ACCEPTED_COUNT);
            } catch (JSONException e) {
                LOG.error(e);
            }
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);

        initAcceptData();

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch
        if (DIALOG_TYPE.equals(getParamAction())) {
            setAction(ACTION_ACCEPT);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);
            // build the title for the user agreement dialog
            setParamTitle(getConfigurationContentStringValue(NODE_DIALOG_TITLE));
        }
    }

}

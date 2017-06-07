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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.main;

import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.site.CmsSiteMatcher;

import java.util.Locale;

/**
 * Contains user information for automated creation of a
 * {@link org.opencms.file.CmsRequestContext} during system runtime.<p>
 *
 * @since 6.0.0
 */
public class CmsContextInfo {

    /** Name of the http session attribute the request time is stored in. */
    public static final String ATTRIBUTE_REQUEST_TIME = "__org.opencms.main.CmsContextInfo#m_requestTime";

    /** Indicates the request time should always be the current time. */
    public static final long CURRENT_TIME = -1L;

    /** Localhost ip used in fallback cases. */
    public static final String LOCALHOST = "127.0.0.1";

    /** The detail content resource, if available. */
    private CmsResource m_detailResource;

    /** The encoding to create the context with. */
    private String m_encoding;

    /** Indicates if the configuration if this context info can still be changed or not. */
    private boolean m_frozen;

    /** True if this was determined to be a request to a secure site. */
    private boolean m_isSecureRequest;

    /** The locale to create the context with. */
    private Locale m_locale;

    /** The locale name to create the context with. */
    private String m_localeName;

    /** The organizational unit to create the context with. */
    private String m_ouFqn;

    /** The project to create the context with. */
    private CmsProject m_project;

    /** The user name to create the context with. */
    private String m_projectName;

    /** The remote ip address to create the context with. */
    private String m_remoteAddr;

    /** The request URI to create the context with. */
    private String m_requestedUri;

    /** The time for the request, used for resource publication and expiration dates. */
    private long m_requestTime;

    /** The site root to create the context with. */
    private String m_siteRoot;

    /** The user to create the context with. */
    private CmsUser m_user;

    /** The user name to create the context with. */
    private String m_userName;

    /** the matcher for the current request, that is the host part of the URI from the original http request. */
    private CmsSiteMatcher m_requestMatcher;

    /**
     * Creates a new instance, initializing the variables with some reasonable default values.<p>
     *
     * The default values are:<dl>
     * <dt>User name</dt><dd>(configured default guest user)</dd>
     * <dt>Project name</dt><dd>Online</dd>
     * <dt>Requested URI</dt><dd>/</dd>
     * <dt>Site root</dt><dd>/</dd>
     * <dt>Locale name</dt><dd>(configured default locale name)</dd>
     * <dt>Encoding</dt><dd>(configured default system encoding)</dd>
     * <dt>Remote address</dt><dd>127.0.0.1</dd>
     * <dt>Organizational unit</dt><dd>/</dd>
     * </dl><p>
     */
    public CmsContextInfo() {

        setUserName(OpenCms.getDefaultUsers().getUserGuest());
        setProjectName(CmsProject.ONLINE_PROJECT_NAME);
        setRequestedUri("/");
        setSiteRoot("/");
        setRequestMatcher(OpenCms.getSiteManager() != null
        ? OpenCms.getSiteManager().getWorkplaceSiteMatcher()
        : CmsSiteMatcher.DEFAULT_MATCHER);
        setLocaleName(CmsLocaleManager.getDefaultLocale().toString());
        setEncoding(OpenCms.getSystemInfo().getDefaultEncoding());
        setRemoteAddr(CmsContextInfo.LOCALHOST);
        setRequestTime(CURRENT_TIME);
        setOuFqn("");
    }

    /**
     * Creates a new instance with all context variables initialized from the given request context.<p>
     *
     * @param requestContext the request context to initialize this context info with
     */
    public CmsContextInfo(CmsRequestContext requestContext) {

        setUserName(requestContext.getCurrentUser().getName());
        setProjectName(requestContext.getCurrentProject().getName());
        setRequestedUri(requestContext.getUri());
        setSiteRoot(requestContext.getSiteRoot());
        setRequestMatcher(requestContext.getRequestMatcher());
        setLocale(requestContext.getLocale());
        setEncoding(requestContext.getEncoding());
        setRemoteAddr(requestContext.getRemoteAddress());
        setRequestTime(requestContext.getRequestTime());
        setIsSecureRequest(requestContext.isSecureRequest());
        setOuFqn(requestContext.getOuFqn());
        setDetailResource(requestContext.getDetailResource());
    }

    /**
     * Creates a new instance with all context variables initialized.<p>
     *
     * @param user the user to create the context with
     * @param project the project to create the context with
     * @param requestedUri the request URI to create the context with
     * @param requestMatcher the matcher for the current request, that is the host part of the URI from the original http request
     * @param siteRoot the site root to create the context with
     * @param isSecureRequest if this a secure request
     * @param locale the locale to create the context with
     * @param encoding the encoding to create the context with
     * @param remoteAddr the remote ip address to create the context with
     * @param requestTime the time of the request (used for resource publication / expiration date)
     * @param ouFqn the fully qualified name of the organizational unit to create the context with
     */
    public CmsContextInfo(
        CmsUser user,
        CmsProject project,
        String requestedUri,
        CmsSiteMatcher requestMatcher,
        String siteRoot,
        boolean isSecureRequest,
        Locale locale,
        String encoding,
        String remoteAddr,
        long requestTime,
        String ouFqn) {

        m_user = user;
        setUserName(m_user.getName());
        m_project = project;
        setProjectName(m_project.getName());
        setRequestedUri(requestedUri);
        setRequestMatcher(requestMatcher);
        setSiteRoot(siteRoot);
        setIsSecureRequest(isSecureRequest);
        setLocale(locale);
        setEncoding(encoding);
        setRemoteAddr(remoteAddr);
        setRequestTime(requestTime);
        setOuFqn(ouFqn);
    }

    /**
     * Creates a new instance, initializing the user name as provided and
     * all other vaiables with the same default values as in {@link #CmsContextInfo()}.<p>
     *
     * @param userName the user name to create the context with
     *
     * @see #CmsContextInfo()
     */
    public CmsContextInfo(String userName) {

        this();
        setUserName(userName);
    }

    /**
     * Creates a clone of this context info object.<p>
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {

        CmsContextInfo result = new CmsContextInfo();
        result.m_encoding = m_encoding;
        result.m_frozen = false;
        result.m_locale = m_locale;
        result.m_localeName = m_localeName;
        result.m_project = m_project;
        result.m_projectName = m_projectName;
        result.m_isSecureRequest = m_isSecureRequest;
        result.m_remoteAddr = m_remoteAddr;
        result.m_requestedUri = m_requestedUri;
        result.m_requestTime = m_requestTime;
        result.m_siteRoot = m_siteRoot;
        result.m_user = m_user;
        result.m_userName = m_userName;
        return result;
    }

    /**
     * Finalizes (freezes) the configuration of this context information.<p>
     *
     * After this entry has been frozen, any attempt to change the
     * configuration of this context info with one of the "set..." methods
     * will lead to a <code>RuntimeException</code>.<p>
     */
    public void freeze() {

        m_frozen = true;
    }

    /**
     * Gets the detail content resource.<p>
     *
     * @return the detail content resource
     */
    public CmsResource getDetailResource() {

        return m_detailResource;
    }

    /**
     * Returns the encoding.<p>
     *
     * @return the encoding
     *
     * @see CmsRequestContext#getEncoding()
     */
    public String getEncoding() {

        return m_encoding;
    }

    /**
     * Returns the locale.<p>
     *
     * @return the locale
     *
     * @see CmsRequestContext#getLocale()
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the locale name.<p>
     *
     * @return the locale name
     *
     * @see CmsRequestContext#getLocale()
     */
    public String getLocaleName() {

        return m_localeName;
    }

    /**
     * Returns the fully qualified name of the organizational unit.<p>
     *
     * @return the fully qualified name of the organizational unit
     */
    public String getOuFqn() {

        return m_ouFqn;
    }

    /**
     * Returns the project, or <code>null</code> if the project
     * has not been configured.<p>
     *
     * If the project has not been configured, at last the
     * project name will be available.<p>
     *
     * @return the project
     *
     * @see #getProjectName()
     * @see CmsRequestContext#getCurrentProject()
     */
    public CmsProject getProject() {

        return m_project;
    }

    /**
     * Returns the project name.<p>
     *
     * @return the project name
     *
     * @see #getProject()
     * @see CmsRequestContext#getCurrentProject()
     */
    public String getProjectName() {

        return m_projectName;
    }

    /**
     * Returns the remote ip address.<p>
     *
     * @return the remote ip address
     *
     * @see CmsRequestContext#getRemoteAddress()
     */
    public String getRemoteAddr() {

        return m_remoteAddr;
    }

    /**
     * Returns the requested uri.<p>
     *
     * @return the requested uri
     *
     * @see CmsRequestContext#getUri()
     */
    public String getRequestedUri() {

        return m_requestedUri;
    }

    /**
     * Returns the matcher for the current request, that is the host part of the URI from the original http request.<p>
     *
     * @return the matcher for the current request, that is the host part of the URI from the original http request
     */
    public CmsSiteMatcher getRequestMatcher() {

        return m_requestMatcher;
    }

    /**
     * Returns the request time used for validation of resource publication and expiration dates.<p>
     *
     * @return the request time used for validation of resource publication and expiration dates
     *
     * @see CmsRequestContext#getRequestTime()
     */
    public long getRequestTime() {

        return m_requestTime;
    }

    /**
     * Returns the siteroot.<p>
     *
     * @return the siteroot
     *
     * @see CmsRequestContext#getSiteRoot()
     */
    public String getSiteRoot() {

        return m_siteRoot;
    }

    /**
     * Returns the user, or <code>null</code> if the user
     * has not been configured.<p>
     *
     * If the user has not been configured, at last the
     * user name will be available.<p>
     *
     * @return the user
     *
     * @see #getUserName()
     * @see CmsRequestContext#getCurrentUser()
     */
    public CmsUser getUser() {

        return m_user;
    }

    /**
     * Returns the username.<p>
     *
     * @return the username
     *
     * @see #getUser()
     * @see CmsRequestContext#getCurrentUser()
     */
    public String getUserName() {

        return m_userName;
    }

    /**
     * Returns true if this a secure request.<p>
     *
     * @return true if this is a secure request
     */
    public boolean isSecureRequest() {

        return m_isSecureRequest;
    }

    /**
     * Sets the detail content resource.<p>
     *
     * @param detailResource the detail content resource to set
     */
    public void setDetailResource(CmsResource detailResource) {

        m_detailResource = detailResource;
    }

    /**
     * Sets the encoding.<p>
     *
     * @param encoding the encoding to set
     *
     * @see CmsRequestContext#setEncoding(String)
     */
    public void setEncoding(String encoding) {

        checkFrozen();
        m_encoding = CmsEncoder.lookupEncoding(encoding, OpenCms.getSystemInfo().getDefaultEncoding());
    }

    /**
     * Sets the 'isSecureRequest' attribute.<p>
     *
     * @param isSecureRequest  true if this a secure request
     */
    public void setIsSecureRequest(boolean isSecureRequest) {

        m_isSecureRequest = isSecureRequest;
    }

    /**
     * Sets the locale.<p>
     *
     * Setting the locale name will override the currently selected locale
     * and vice-versa. The locale name and the locale will always match.<p>
     *
     * @param locale the locale to set
     *
     * @see #setLocaleName(String)
     * @see CmsRequestContext#getLocale()
     */
    public void setLocale(Locale locale) {

        checkFrozen();
        m_locale = locale;
        m_localeName = m_locale.toString();
    }

    /**
     * Sets the locale name.<p>
     *
     * Setting the locale name will override the currently selected locale
     * and vice-versa. The locale name and the locale will always match.<p>
     *
     * @param localeName the locale name to set
     *
     * @see #setLocale(Locale)
     * @see CmsRequestContext#getLocale()
     */
    public void setLocaleName(String localeName) {

        checkFrozen();
        m_localeName = localeName;
        m_locale = CmsLocaleManager.getLocale(localeName);
    }

    /**
     * Sets the fully qualified name of the organizational unit.<p>
     *
     * @param ouFqn the fully qualified name of the organizational unit to set
     */
    public void setOuFqn(String ouFqn) {

        checkFrozen();
        m_ouFqn = ouFqn;
    }

    /**
     * Sets the project name.<p>
     *
     * @param projectName the project name to set
     *
     * @see CmsRequestContext#getCurrentProject()
     */
    public void setProjectName(String projectName) {

        checkFrozen();
        m_projectName = projectName;
    }

    /**
     * Sets the remote ip address.<p>
     *
     * @param remoteAddr the remote ip address
     *
     * @see CmsRequestContext#getRemoteAddress()
     */
    public void setRemoteAddr(String remoteAddr) {

        checkFrozen();
        m_remoteAddr = remoteAddr;
    }

    /**
     * Sets the requested uri.<p>
     *
     * @param requestedUri the requested uri to set
     *
     * @see CmsRequestContext#setUri(String)
     */
    public void setRequestedUri(String requestedUri) {

        checkFrozen();
        m_requestedUri = requestedUri;
    }

    /**
     * Sets the matcher for the current request, that is the host part of the URI from the original http request.<p>
     *
     * @param requestMatcher the matcher for the current request
     */
    public void setRequestMatcher(CmsSiteMatcher requestMatcher) {

        m_requestMatcher = requestMatcher;
    }

    /**
     * Sets the request time used for validation of resource publication and expiration dates.<p>
     *
     * @param requestTime the request time to set
     *
     * @see CmsRequestContext#getRequestTime()
     */
    public void setRequestTime(long requestTime) {

        checkFrozen();
        if (requestTime == CURRENT_TIME) {
            m_requestTime = System.currentTimeMillis();
        } else {
            m_requestTime = requestTime;
        }
    }

    /**
     * Sets the siteroot.<p>
     *
     * @param siteRoot the siteroot to set
     *
     * @see CmsRequestContext#setSiteRoot(String)
     */
    public void setSiteRoot(String siteRoot) {

        checkFrozen();
        m_siteRoot = siteRoot;
    }

    /**
     * Sets the username.<p>
     *
     * @param userName the username to set
     *
     * @see CmsRequestContext#getCurrentUser()
     */
    public void setUserName(String userName) {

        checkFrozen();
        m_userName = userName;
        setOuFqn(CmsOrganizationalUnit.getParentFqn(userName));
    }

    /**
     * Checks if this context info configuration is frozen.<p>
     *
     * @throws CmsRuntimeException in case the configuration is already frozen
     */
    protected void checkFrozen() throws CmsRuntimeException {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_CONTEXT_INFO_FROZEN_0));
        }
    }
}
/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsContextInfo.java,v $
 * Date   : $Date: 2005/06/27 23:22:20 $
 * Version: $Revision: 1.11 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;

import java.util.Locale;

/**
 * Contains user information for automated creation of a  
 * {@link org.opencms.file.CmsRequestContext} during system runtime.<p>
 * 
 * @version $Revision: 1.11 $ 
 * 
 * @since 6.0.0 
 */
public class CmsContextInfo {

    /** The encoding to create the context with. */
    private String m_encoding;

    /** Indicates if the configuration if this context info can still be changed or not. */
    private boolean m_frozen;

    /** The locale to create the context with. */
    private Locale m_locale;

    /** The locale name to create the context with. */
    private String m_localeName;

    /** The project to create the context with. */
    private CmsProject m_project;

    /** The user name to create the context with. */
    private String m_projectName;

    /** The remote ip address to create the context with. */
    private String m_remoteAddr;

    /** The request URI to create the context with. */
    private String m_requestedUri;

    /** The site root to create the context with. */
    private String m_siteRoot;

    /** The user to create the context with. */
    private CmsUser m_user;

    /** The user name to create the context with. */
    private String m_userName;

    /** Localhost ip used in fallback cases. */
    public static final String LOCALHOST = "127.0.0.1";

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
     * </dl><p>
     */
    public CmsContextInfo() {

        super();
        setUserName(OpenCms.getDefaultUsers().getUserGuest());
        setProjectName(CmsProject.ONLINE_PROJECT_NAME);
        setRequestedUri("/");
        setSiteRoot("/");
        setLocaleName(CmsLocaleManager.getDefaultLocale().toString());
        setEncoding(OpenCms.getSystemInfo().getDefaultEncoding());
        setRemoteAddr(CmsContextInfo.LOCALHOST);
    }

    /**
     * Creates a new instance with all context variables initialized.<p>
     *
     * @param user the user to create the context with
     * @param project the project to create the context with
     * @param requestedUri the request URI to create the context with
     * @param siteRoot the site root to create the context with
     * @param locale the locale to create the context with
     * @param encoding the encoding to create the context with
     * @param remoteAddr the remote ip address to create the context with
     */
    public CmsContextInfo(
        CmsUser user,
        CmsProject project,
        String requestedUri,
        String siteRoot,
        Locale locale,
        String encoding,
        String remoteAddr) {

        super();
        m_user = user;
        setUserName(m_user.getName());
        m_project = project;
        setProjectName(m_project.getName());
        setRequestedUri(requestedUri);
        setSiteRoot(siteRoot);
        setLocale(locale);
        setEncoding(encoding);
        setRemoteAddr(remoteAddr);
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
     * Creates a new instance with all context variables initialized.<p>
     *
     * @param userName the user name to create the context with
     * @param projectName the project name to create the context with
     * @param requestedUri the request URI to create the context with
     * @param siteRoot the site root to create the context with
     * @param localeName the locale name to create the context with
     * @param encoding the encoding to create the context with
     * @param remoteAddr the remote ip address to create the context with
     */
    public CmsContextInfo(
        String userName,
        String projectName,
        String requestedUri,
        String siteRoot,
        String localeName,
        String encoding,
        String remoteAddr) {

        super();
        setUserName(userName);
        setProjectName(projectName);
        setRequestedUri(requestedUri);
        setSiteRoot(siteRoot);
        setLocaleName(localeName);
        setEncoding(encoding);
        setRemoteAddr(remoteAddr);
    }

    /**
     * Creates a clone of this context info object.<p>
     * 
     * @see java.lang.Object#clone()
     */
    public Object clone() {

        CmsContextInfo result = new CmsContextInfo();
        result.m_encoding = m_encoding;
        result.m_frozen = false;
        result.m_locale = m_locale;
        result.m_localeName = m_localeName;
        result.m_project = m_project;
        result.m_projectName = m_projectName;
        result.m_remoteAddr = m_remoteAddr;
        result.m_requestedUri = m_requestedUri;
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
     * Returns the encoding.<p>
     *
     * @return the encoding
     */
    public String getEncoding() {

        return m_encoding;
    }

    /**
     * Returns the locale.<p>
     *
     * @return the locale
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the locale name.<p>
     *
     * @return the locale name
     */
    public String getLocaleName() {

        return m_localeName;
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
     */
    public String getProjectName() {

        return m_projectName;
    }

    /**
     * Returns the remote ip address.<p>
     *
     * @return the remote ip address
     */
    public String getRemoteAddr() {

        return m_remoteAddr;
    }

    /**
     * Returns the requested uri.<p>
     *
     * @return the requested uri
     */
    public String getRequestedUri() {

        return m_requestedUri;
    }

    /**
     * Returns the siteroot.<p>
     *
     * @return the siteroot
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
     */
    public String getUserName() {

        return m_userName;
    }

    /**
     * Sets the encoding.<p>
     *
     * @param encoding the encoding to set
     */
    public void setEncoding(String encoding) {

        checkFrozen();
        m_encoding = CmsEncoder.lookupEncoding(encoding, OpenCms.getSystemInfo().getDefaultEncoding());
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
     */
    public void setLocaleName(String localeName) {

        checkFrozen();
        m_localeName = localeName;
        m_locale = CmsLocaleManager.getLocale(localeName);
    }

    /**
     * Sets the project name.<p>
     *
     * @param projectName the project name to set
     */
    public void setProjectName(String projectName) {

        checkFrozen();
        m_projectName = projectName;
    }

    /**
     * Sets the remote ip address.<p>
     *
     * @param remoteAddr the remote ip address
     */
    public void setRemoteAddr(String remoteAddr) {

        checkFrozen();
        m_remoteAddr = remoteAddr;
    }

    /**
     * Sets the requested uri.<p>
     *
     * @param requestedUri the requested uri to set
     */
    public void setRequestedUri(String requestedUri) {

        checkFrozen();
        m_requestedUri = requestedUri;
    }

    /**
     * Sets the siteroot.<p>
     *
     * @param siteRoot the siteroot to set
     */
    public void setSiteRoot(String siteRoot) {

        checkFrozen();
        m_siteRoot = siteRoot;
    }

    /**
     * Sets the username.<p>
     *
     * @param userName the username to set
     */
    public void setUserName(String userName) {

        checkFrozen();
        m_userName = userName;
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
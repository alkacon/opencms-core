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

package org.opencms.workplace;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsProject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsSecurityException;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsStringUtil;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

/**
 * Handles front-end login of users to the OpenCms workplace into the given site and project.<p>
 *
 * @since 7.0.3
 */
public class CmsLoginHelper extends CmsWorkplace {

    /** The login exception. */
    private CmsException m_loginException;

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsLoginHelper(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * Returns the loginException.<p>
     *
     * @return the loginException
     */
    public CmsException getLoginException() {

        return m_loginException;
    }

    /**
     * Returns the formatted stack trace.<p>
     *
     * @return the formatted stack trace
     */
    public String getStacktrace() {

        String stacktrace = CmsException.getStackTraceAsString(getLoginException());
        stacktrace = CmsEncoder.escapeXml(stacktrace);
        return stacktrace;
    }

    /**
     * Logs the user into the given project and site.<p>
     *
     * Check the {@link #getLoginException()} for the error message.<p>
     *
     * @param userName the user name
     * @param password the password
     * @param projectName the optional project name, if <code>null</code> the default project is used
     * @param siteRoot the site of the resource, if <code>null</code> the default site is used
     * @param resourceName the resource to display
     *
     * @return <code>true</code> if the login has been successful
     */
    public boolean login(String userName, String password, String projectName, String siteRoot, String resourceName) {

        if (getCms().getRequestContext().getCurrentUser().isGuestUser()) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(userName) || CmsStringUtil.isEmptyOrWhitespaceOnly(password)) {
                return false;
            }
            // login the user
            try {
                getCms().loginUser(userName, password, getCms().getRequestContext().getRemoteAddress());
            } catch (CmsException e) {
                m_loginException = e;
                return false;
            }
        }

        // the user is logged in
        CmsUserSettings userSettings = new CmsUserSettings(getCms());
        // set the project
        try {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(projectName)) {
                // use the default project of the user
                projectName = userSettings.getStartProject();
            }
            // read the project
            CmsProject project = getCms().readProject(projectName);
            if (OpenCms.getOrgUnitManager().getAllAccessibleProjects(getCms(), project.getOuFqn(), false).contains(
                project)) {
                // user has access to the project, set this as current project
                getCms().getRequestContext().setCurrentProject(project);
            } else {
                throw new CmsSecurityException(
                    Messages.get().container(Messages.ERR_PROJECT_NOT_ACCESSIBLE_2, userName, projectName));
            }
        } catch (CmsException e) {
            m_loginException = e;
        }

        if (m_loginException == null) {
            // set the site
            try {
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(siteRoot)) {
                    // set the default site root of the user
                    siteRoot = userSettings.getStartSite();
                }
                // set the site root if accessible
                String oldSite = getCms().getRequestContext().getSiteRoot();
                try {
                    getCms().getRequestContext().setSiteRoot("");
                    getCms().readResource(siteRoot);
                } finally {
                    getCms().getRequestContext().setSiteRoot(oldSite);
                }
                boolean hasAccess = false;
                CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(siteRoot);
                Iterator<CmsSite> accessibles = OpenCms.getSiteManager().getAvailableSites(getCms(), false).iterator();
                while (accessibles.hasNext() && !hasAccess && (site != null)) {
                    CmsSite accessible = accessibles.next();
                    if (accessible.getSiteRoot().equals(site.getSiteRoot())) {
                        hasAccess = true;
                    }
                }
                if (hasAccess) {
                    // user has access to the site, set this as current site
                    getCms().getRequestContext().setSiteRoot(siteRoot);
                } else {
                    throw new CmsSecurityException(
                        Messages.get().container(Messages.ERR_SITE_NOT_ACCESSIBLE_2, userName, siteRoot));
                }
            } catch (CmsException e) {
                m_loginException = e;
            }
        }

        // try to read the resource to display
        try {
            getCms().readResource(resourceName);
        } catch (CmsException e) {
            m_loginException = e;
        }

        if (m_loginException != null) {
            // if an error occurred during login, invalidate the session
            HttpSession session = getJsp().getRequest().getSession(false);
            if (session != null) {
                session.invalidate();
            }
            return false;
        }

        // only for content creators so that direct edit works
        if (OpenCms.getRoleManager().hasRole(getCms(), CmsRole.ELEMENT_AUTHOR)) {
            // get / create the workplace settings
            CmsWorkplaceSettings wpSettings = getSettings();
            if (wpSettings == null) {
                // create the settings object
                wpSettings = new CmsWorkplaceSettings();
                wpSettings = initWorkplaceSettings(getCms(), wpSettings, false);
            }
            // set the settings for the workplace
            wpSettings.setSite(getCms().getRequestContext().getSiteRoot());
            wpSettings.setProject(getCms().getRequestContext().getCurrentProject().getUuid());
            wpSettings.setUser(getCms().getRequestContext().getCurrentUser());
            HttpSession session = getJsp().getRequest().getSession(true);
            storeSettings(session, wpSettings);
        }

        return true;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#checkRole()
     */
    @Override
    protected void checkRole() {

        // do not check
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // empty
    }
}

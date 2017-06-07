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

package org.opencms.jsp;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPersistentLoginAuthorizationHandler;
import org.opencms.security.CmsPersistentLoginTokenHandler;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsRequestUtil;

import java.io.IOException;

import javax.servlet.http.Cookie;

import org.apache.commons.logging.Log;

/**
 * Login bean which sets a cookie that can be used by {@link CmsPersistentLoginAuthorizationHandler} to automatically
 * log the user back in when his session has expired.
 *
 * The cookie's lifetime can be set using the setTokenLifetime method
 */
public class CmsJspLoginPersistingBean extends CmsJspLoginBean {

    /** The token life time. */
    private long m_tokenLifetime = CmsPersistentLoginTokenHandler.DEFAULT_LIFETIME;

    /** The cookie path. */
    private String m_cookiePath = "%(CONTEXT_NAME)%(SERVLET_NAME)";

    /** True if the token has been set. */
    private boolean m_isTokenSet;

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspLoginPersistingBean.class);

    /**
     * Gets the path to use for the authorization cookie, optionally resolving any macros used.<p>
     *
     * @param resolveMacros if true, macros should be resolved
     * @return the authorization cookie path
     */
    public String getCookiePath(boolean resolveMacros) {

        String result = m_cookiePath;
        if (resolveMacros) {
            CmsMacroResolver resolver = new CmsMacroResolver();
            // add special mappings for macros
            resolver.addMacro("CONTEXT_NAME", OpenCms.getSystemInfo().getContextPath());
            resolver.addMacro("SERVLET_NAME", OpenCms.getSystemInfo().getServletPath());
            result = resolver.resolveMacros(result);
        }
        return result;
    }

    /**
     * Returns true if the token has been set.<p>
     *
     * @return true if the token has been set
     */
    public boolean isTokenSet() {

        return m_isTokenSet;
    }

    /**
     * @see org.opencms.jsp.CmsJspLoginBean#login(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void login(String userName, String password, String projectName) {

        super.login(userName, password, projectName);
        if (isLoginSuccess()) {
            CmsObject cms = getCmsObject();
            CmsPersistentLoginTokenHandler tokenHandler = new CmsPersistentLoginTokenHandler();
            tokenHandler.setTokenLifetime(m_tokenLifetime);
            try {
                final String token = tokenHandler.createToken(cms);
                Cookie cookie = new Cookie(CmsPersistentLoginAuthorizationHandler.COOKIE_NAME, token);
                cookie.setMaxAge((int)(m_tokenLifetime / 1000));
                cookie.setPath(getCookiePath(true));
                getResponse().addCookie(cookie);
                m_isTokenSet = true;
            } catch (CmsException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    /**
     * @see org.opencms.jsp.CmsJspLoginBean#logout()
     */
    @Override
    public void logout() throws IOException {

        logout(true);
    }

    /**
     * Logs the user out and optionally invalidates their login token.<p>
     *
     * @param invalidateToken true if the token should be invalidated
     *
     * @throws IOException if something goes wrong
     */
    public void logout(boolean invalidateToken) throws IOException {

        if (isLoggedIn() && invalidateToken) {
            CmsUser user = getCmsObject().getRequestContext().getCurrentUser();
            CmsPersistentLoginTokenHandler tokenHandler = new CmsPersistentLoginTokenHandler();
            try {
                Cookie cookie = new Cookie(CmsPersistentLoginAuthorizationHandler.COOKIE_NAME, "");
                cookie.setMaxAge(0);
                cookie.setPath(getCookiePath(true));
                getResponse().addCookie(cookie);
                tokenHandler.invalidateToken(
                    user,
                    CmsRequestUtil.getCookieValue(
                        getRequest().getCookies(),
                        CmsPersistentLoginAuthorizationHandler.COOKIE_NAME));
            } catch (Exception e) {
                LOG.error("Could not invalidate tokens for user " + user, e);
            }

        }
        super.logout();

    }

    /**
     * Sets the path to use for the login token cookie.<p>
     *
     * You can use the macros %(SERVLET_NAME) and %(CONTEXT_NAME) in the cookie path; the default
     * value is %(CONTEXT_NAME)%(SERVLET_NAME).<p>
     *
     * @param cookiePath the cookie path, possibly including macros
     */
    public void setCookiePath(String cookiePath) {

        m_cookiePath = cookiePath;
    }

    /**
     * Sets the number of milliseconds for which the tokens should be valid.<p>
     *
     * @param lifetime the token life time
     */
    public void setTokenLifetime(long lifetime) {

        m_tokenLifetime = lifetime;
    }

}

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

package org.opencms.security;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor.CacheType;
import org.opencms.util.CmsRequestUtil;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Authorization handler which uses a special cookie sent by the user's browser for authorization.<p>
 *
 * The cookie contains a user's name and a key. It will only log that user in if there is a key matching the key from the cookie
 * in the user's additional info map, and if additional info value, when interpreted as a time, is greater than the current time returned
 * by System.currentTimeMillis().
 */
public class CmsPersistentLoginAuthorizationHandler extends CmsDefaultAuthorizationHandler {

    /** The name of the cookie. */
    public static final String COOKIE_NAME = "ocmsLoginToken";

    /** The logger for this class. */
    @SuppressWarnings("hiding")
    private static final Log LOG = CmsLog.getLog(CmsPersistentLoginAuthorizationHandler.class);

    /**
     * @see org.opencms.security.CmsDefaultAuthorizationHandler#initCmsObject(javax.servlet.http.HttpServletRequest, org.opencms.security.I_CmsAuthorizationHandler.I_PrivilegedLoginAction)
     */
    @Override
    public CmsObject initCmsObject(HttpServletRequest request, I_PrivilegedLoginAction loginAction) {

        CmsObject cms = initCmsObjectFromToken(request, loginAction);
        if (cms == null) {
            cms = super.initCmsObject(request, loginAction);
        }
        return cms;
    }

    /**
     * Tries to initialize the CmsObject from a login token given as a cookie in the request.<p>
     *
     * @param request the request
     * @param loginAction the privileged login action
     *
     * @return the initialized CmsObject, or null if the user couldn't be authenticated using the login token cookie
     */
    public CmsObject initCmsObjectFromToken(HttpServletRequest request, I_PrivilegedLoginAction loginAction) {

        CmsObject cms = null;
        CmsPersistentLoginTokenHandler tokenHandler = new CmsPersistentLoginTokenHandler();
        try {
            CmsUser user = tokenHandler.validateToken(CmsRequestUtil.getCookieValue(request.getCookies(), COOKIE_NAME));
            if (user != null) {
                // clean up some caches to ensure group changes in the LDAP directory take effect
                OpenCms.getMemoryMonitor().uncacheUser(user);
                OpenCms.getMemoryMonitor().flushCache(
                    CacheType.HAS_ROLE,
                    CacheType.USERGROUPS,
                    CacheType.PERMISSION,
                    CacheType.ROLE_LIST);
                loginAction.getCmsObject().getRequestContext().setAttribute("__FORCE_UPDATE_MEMBERSHIP", Boolean.TRUE);
                cms = loginAction.doLogin(request, user.getName());
                OpenCms.getMemoryMonitor().flushCache(
                    CacheType.HAS_ROLE,
                    CacheType.USERGROUPS,
                    CacheType.PERMISSION,
                    CacheType.ROLE_LIST);

                cms = registerSession(request, cms);
                LOG.info("Successfully authenticated user '"
                    + cms.getRequestContext().getCurrentUser().getName()
                    + "' using a login token.");
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return cms;
    }

}

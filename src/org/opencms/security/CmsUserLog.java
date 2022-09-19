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
import org.opencms.file.CmsRequestContext;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsFileUtil;

import java.util.TreeMap;

import org.apache.commons.logging.Log;

/**
 * Class with static methods for logging user-related operations in a centralized manner.
 */
public class CmsUserLog {

    /** The logger to be used. */
    private static final Log LOG = CmsLog.getLog(CmsUserLog.class);

    /**
     * Logs a successful login.
     *
     * @param cms the CMS context
     * @param user the name of the user
     */
    public static void logLogin(CmsObject cms, String user) {

        LOG.info("login successful: " + formatUser(user) + " " + context(cms));
    }

    /**
     * Logs a login failure.
     *
     * @param cms the CMS context
     * @param user the name of the user
     */
    public static void logLoginFailure(CmsObject cms, String user) {

        LOG.info("login failed: " + formatUser(user) + " " + context(cms));
    }

    /**
     * Logs a successful logout.
     *
     * @param cms the CMS context
     */
    public static void logLogout(CmsObject cms) {

        LOG.info("logout: " + formatUser(cms.getRequestContext().getCurrentUser().getName()) + " " + context(cms));

    }

    /**
     * Logs a password change.
     *
     * @param cms the CMS context
     * @param user the user name
     */
    public static void logPasswordChange(CmsObject cms, String user) {

        LOG.info("password changed: " + formatUser(user) + " " + context(cms));
    }

    /**
     * Logs a password change originally requested through the 'reset password' button.
     *
     * @param cms the CMS context
     * @param user the user name
     */
    public static void logPasswordChangeForRequestedReset(CmsObject cms, String user) {

        LOG.info("password changed (reset requested): " + formatUser(user) + " " + context(cms));

    }

    /**
     * Logs a password reset request.
     *
     * @param cms the CMS context
     * @param user the user name
     */
    public static void logPasswordResetRequest(CmsObject cms, String user) {

        LOG.info("password reset request: " + user + " " + context(cms));
    }

    /**
     * Logs when a second factor was added.
     *
     * @param requestContext the request context
     * @param name the user name
     */
    public static void logSecondFactorAdded(CmsRequestContext requestContext, String name) {

        LOG.info("second factor added: " + formatUser(name) + " " + context(requestContext));
    }

    /**
     * Logs when a second factor was modified.
     *
     * @param requestContext the request context
     * @param name the user name
     */
    public static void logSecondFactorInfoModified(CmsRequestContext requestContext, String name) {

        LOG.info("second factor information modified: " + formatUser(name) + " " + context(requestContext));
    }

    /**
     * Logs when a second factor was removed.
     *
     * @param requestContext the request context
     * @param name the user name
     */
    public static void logSecondFactorReset(CmsRequestContext requestContext, String name) {

        LOG.info("second factor reset: " + formatUser(name) + " " + context(requestContext));
    }

    /**
     * Logs that the 'force reset password' status was set on a user.
     *
     * @param cms the CMS context
     * @param user the user name
     */
    public static void logSetForceResetPassword(CmsObject cms, String user) {

        LOG.info("forcing password reset on next login: " + user + " " + context(cms));
    }

    /**
     * Logs a user switch.
     *
     * @param cms the current CMS context
     * @param name the name of the user to switch to
     */
    public static void logSwitchUser(CmsObject cms, String name) {

        LOG.info(
            "user switch: "
                + formatUser(cms.getRequestContext().getCurrentUser().getName())
                + " => "
                + formatUser(name)
                + " "
                + context(cms));

        // TODO Auto-generated method stub
    }

    /**
     * Helper method for formatting context information.
     *
     * @param cms the CMS context
     * @return the context information
     */
    private static TreeMap<String, String> context(CmsObject cms) {

        return context(cms.getRequestContext());
    }

    /**
     * Helper method for formatting context information.
     *
     * @param requestContext the request context
     * @return the context information
     */
    private static TreeMap<String, String> context(CmsRequestContext requestContext) {

        TreeMap<String, String> result = new TreeMap<>();
        result.put("remote_address", requestContext.getRemoteAddress());
        result.put("current_user", requestContext.getCurrentUser().getName());
        return result;
    }

    /**
     * Formats a user name.
     *
     * @param userName the user nam
     * @return the formatted user name
     */
    private static String formatUser(String userName) {

        return CmsFileUtil.removeLeadingSeparator(userName);
    }

}

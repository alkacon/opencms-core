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

package org.opencms.workplace.editors;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.workplace.CmsDialog;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;

/**
 * Base class for all editors that turns of time warp deletion inherited from
 * <code>{@link org.opencms.workplace.CmsWorkplace}</code>.<p>
 *
 * @since 6.0.0
 */
public class CmsEditorBase extends CmsDialog {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsEditorBase.class);

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsEditorBase(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * In addition to the permission check, this will also check if the current user has at least the ELEMENT_AUTHOR role.<p>
     *
     * @see org.opencms.workplace.CmsDialog#checkResourcePermissions(org.opencms.security.CmsPermissionSet, boolean, org.opencms.i18n.CmsMessageContainer)
     */
    @Override
    protected boolean checkResourcePermissions(
        CmsPermissionSet required,
        boolean neededForFolder,
        CmsMessageContainer errorMessage) {

        boolean hasPermissions = false;
        try {
            CmsResource res;
            if (neededForFolder) {
                // check permissions for the folder the resource is in
                res = getCms().readResource(CmsResource.getParentFolder(getParamResource()), CmsResourceFilter.ALL);
            } else {
                res = getCms().readResource(getParamResource(), CmsResourceFilter.ALL);
            }
            hasPermissions = getCms().hasPermissions(res, required, false, CmsResourceFilter.ALL)
                && (OpenCms.getRoleManager().hasRoleForResource(
                    getCms(),
                    CmsRole.ELEMENT_AUTHOR,
                    getCms().getSitePath(res))
                    || OpenCms.getRoleManager().hasRoleForResource(
                        getCms(),
                        CmsRole.PROJECT_MANAGER,
                        getCms().getSitePath(res))
                    || OpenCms.getRoleManager().hasRoleForResource(
                        getCms(),
                        CmsRole.ACCOUNT_MANAGER,
                        getCms().getSitePath(res)));
        } catch (CmsException e) {
            // should usually never happen
            if (LOG.isInfoEnabled()) {
                LOG.info(e);
            }
        }

        if (!hasPermissions) {
            // store the error message in the users session
            getSettings().setErrorMessage(errorMessage);
        }

        return hasPermissions;
    }

    /**
     * Checks that the current user is a workplace user.<p>
     *
     * @throws CmsRoleViolationException if the user does not have the required role
     */
    @Override
    protected void checkRole() throws CmsRoleViolationException {

        OpenCms.getRoleManager().checkRole(getCms(), CmsRole.EDITOR);
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initTimeWarp(org.opencms.db.CmsUserSettings, javax.servlet.http.HttpSession)
     */
    @Override
    protected void initTimeWarp(CmsUserSettings settings, HttpSession session) {

        // overridden to avoid deletion of the configured time warp:
        // this is triggered by editors and in auto time warping a direct edit
        // must not delete a potential auto warped request time
    }
}
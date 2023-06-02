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

package org.opencms.ui.login;

import org.opencms.db.CmsLoginManager;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.security.CmsRole;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Utility for setting the 'force password reset' state for all self-managed non-default users.
 */
public class CmsForcePasswordResetJob implements I_CmsScheduledJob {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsForcePasswordResetJob.class);

    /** Organizational unit parameter. */
    public static final String PARAM_OU = "ou";

    /**
     * Creates a new instance.
     */
    public CmsForcePasswordResetJob() {

    }

    /**
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(org.opencms.file.CmsObject, java.util.Map)
     */
    public String launch(CmsObject cms, Map<String, String> parameters) throws Exception {

        OpenCms.getRoleManager().checkRole(cms, CmsRole.ACCOUNT_MANAGER);
        CmsLoginManager loginManager = OpenCms.getLoginManager();
        String ou = "";
        if ((parameters != null) && (parameters.get(PARAM_OU) != null)) {
            ou = parameters.get(PARAM_OU);
        }
        List<CmsUser> users = OpenCms.getOrgUnitManager().getUsers(cms, ou, true);
        for (CmsUser user : users) {
            if (loginManager.isExcludedFromPasswordReset(cms, user)) {
                LOG.info("Excluded user " + user.getName() + " from password reset.");
                continue;
            }
            try {
                if (user.getAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_PASSWORD_RESET) == null) {
                    LOG.info("Marking user " + user.getName() + " for password reset.");
                    user.setAdditionalInfo(CmsUserSettings.ADDITIONAL_INFO_PASSWORD_RESET, "true");
                    cms.writeUser(user);
                }
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return "";
    }

}

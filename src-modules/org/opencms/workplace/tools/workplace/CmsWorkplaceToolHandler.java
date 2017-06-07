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

package org.opencms.workplace.tools.workplace;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.synchronize.CmsSynchronizeSettings;
import org.opencms.workplace.tools.A_CmsToolHandler;

/**
 * Workplace management tool handler that hides the tools if the current user
 * has not the needed privileges.<p>
 *
 * @since 6.0.0
 */
public class CmsWorkplaceToolHandler extends A_CmsToolHandler {

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#isEnabled(org.opencms.file.CmsObject)
     */
    public boolean isEnabled(CmsObject cms) {

        if (getPath().startsWith("/workplace/broadcast") || getPath().equals("/workplace")) {
            if (OpenCms.getRoleManager().hasRole(cms, CmsRole.ACCOUNT_MANAGER)) {
                return true;
            }
        }
        if (getPath().startsWith("/workplace/synchronize_action")) {
            CmsUserSettings settings = new CmsUserSettings(cms);
            CmsSynchronizeSettings syncSettings = settings.getSynchronizeSettings();
            return (syncSettings != null) && syncSettings.isSyncEnabled();
        }
        return OpenCms.getRoleManager().hasRole(cms, CmsRole.WORKPLACE_MANAGER);
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#isVisible(org.opencms.file.CmsObject)
     */
    public boolean isVisible(CmsObject cms) {

        return isEnabled(cms);
    }
}

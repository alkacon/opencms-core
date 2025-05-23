/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.sites;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.workplace.tools.A_CmsToolHandler;

/**
 * Sites management tool handler that hides the tool if the current user
 * has not the needed privileges.<p>
 *
 * @since 9.0.0
 */
public class CmsSitesToolHandlerWebserver extends A_CmsToolHandler {

    /** Parameter name to enable or disable the configuration. */
    private static final String PARAM_ENABLED = "enableconfig";

    /** The name of this module. */
    private static final String MODULE_NAME = "org.opencms.workplace.tools.sites";

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#isEnabled(org.opencms.file.CmsObject)
     */
    public boolean isEnabled(CmsObject cms) {

        boolean isEnabled = Boolean.valueOf(
            OpenCms.getModuleManager().getModule(MODULE_NAME).getParameter(
                PARAM_ENABLED,
                Boolean.TRUE.toString())).booleanValue();
        return isEnabled && OpenCms.getRoleManager().hasRole(cms, CmsRole.ROOT_ADMIN);
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#isVisible(org.opencms.file.CmsObject)
     */
    public boolean isVisible(CmsObject cms) {

        return isEnabled(cms);
    }
}

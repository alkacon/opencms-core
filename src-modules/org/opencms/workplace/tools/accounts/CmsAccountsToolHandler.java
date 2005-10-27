/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsAccountsToolHandler.java,v $
 * Date   : $Date: 2005/10/27 17:12:06 $
 * Version: $Revision: 1.6.2.2 $
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

package org.opencms.workplace.tools.accounts;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.security.CmsRole;
import org.opencms.workplace.tools.A_CmsToolHandler;

/**
 * Users management tool handler that hides the tool if the current user
 * has not the needed privileges.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.6.2.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsAccountsToolHandler extends A_CmsToolHandler {

    /** Visibility flag module parameter name. */
    private static final String PARAM_VISIBILITY_FLAG = "visibility";

    /** Visibility parameter value constant. */
    private static final String VISIBILITY_ALL = "all";

    /** Visibility parameter value constant. */
    private static final String VISIBILITY_NONE = "none";

    /** Visibility parameter value constant. */
    private static final String VISIBILITY_WEBUSERSONLY = "webusersonly";

    /**
     * @see org.opencms.workplace.tools.I_CmsToolHandler#isEnabled(org.opencms.file.CmsObject)
     */
    public boolean isEnabled(CmsObject cms) {

        return true;
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsToolHandler#isVisible(org.opencms.file.CmsObject)
     */
    public boolean isVisible(CmsObject cms) {

        if (getVisibilityFlag().equals(VISIBILITY_NONE)) {
            return false;
        }
        if (getVisibilityFlag().equals(VISIBILITY_ALL)) {
            return cms.hasRole(CmsRole.ACCOUNT_MANAGER);
        }
        if (getVisibilityFlag().equals(VISIBILITY_WEBUSERSONLY)) {
            boolean visible = cms.hasRole(CmsRole.ACCOUNT_MANAGER);
            visible = visible
                && (getPath().equals("/accounts") || getPath().indexOf("/webusers") > 0 || getPath().indexOf("/groups") > 0);
            return visible;
        }
        return true;
    }

    /**
     * Returns the visibility flag module parameter value.<p>
     * 
     * @return the visibility flag module parameter value
     */
    private String getVisibilityFlag() {

        CmsModule module = OpenCms.getModuleManager().getModule(this.getClass().getPackage().getName());
        if (module == null) {
            return VISIBILITY_ALL;
        }
        return module.getParameter(PARAM_VISIBILITY_FLAG, VISIBILITY_ALL);
    }
}

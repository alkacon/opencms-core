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

package org.opencms.ugc;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsEvent;
import org.opencms.module.CmsModule;
import org.opencms.module.I_CmsModuleAction;
import org.opencms.report.I_CmsReport;

/**
 * Module action class for the user-generated content module.<p>
 */
public class CmsUgcModuleAction implements I_CmsModuleAction {

    /**
     * The admin-level CMS context.<p>
     *
     */
    private static CmsObject m_adminCms;

    /**
     * Returns a CMS context with admin privileges.<p>
     *
     * @return a CMS context with admin privileges
     */
    protected static CmsObject getAdminCms() {

        return m_adminCms;
    }

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        // ignore
    }

    /**
     * @see org.opencms.module.I_CmsModuleAction#initialize(org.opencms.file.CmsObject, org.opencms.configuration.CmsConfigurationManager, org.opencms.module.CmsModule)
     */
    public void initialize(CmsObject adminCms, CmsConfigurationManager configurationManager, CmsModule module) {

        m_adminCms = adminCms;
    }

    /**
     * @see org.opencms.module.I_CmsModuleAction#moduleUninstall(org.opencms.module.CmsModule)
     */
    public void moduleUninstall(CmsModule module) {

        // ignore
    }

    /**
     * @see org.opencms.module.I_CmsModuleAction#moduleUpdate(org.opencms.module.CmsModule)
     */
    public void moduleUpdate(CmsModule module) {

        // ignore
    }

    /**
     * @see org.opencms.module.I_CmsModuleAction#publishProject(org.opencms.file.CmsObject, org.opencms.db.CmsPublishList, int, org.opencms.report.I_CmsReport)
     */
    public void publishProject(CmsObject cms, CmsPublishList publishList, int publishTag, I_CmsReport report) {

        // ignore

    }

    /**
     * @see org.opencms.module.I_CmsModuleAction#shutDown(org.opencms.module.CmsModule)
     */
    public void shutDown(CmsModule module) {

        // ignore

    }

}

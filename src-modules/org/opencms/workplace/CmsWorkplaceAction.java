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

package org.opencms.workplace;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.module.A_CmsModuleAction;
import org.opencms.module.CmsModule;

/**
 * The workplace manager class to get the admin CmsObject.<p>
 *
 * @since 7.5.0
 */
public class CmsWorkplaceAction extends A_CmsModuleAction {

    /** The initialized singleton login manager instance. */
    private static CmsWorkplaceAction m_instance;

    /** The CmsObject initialized as an administrator during startup. */
    private CmsObject m_cmsAdmin;

    /**
     * Constructor for a photo album manager object.<p>
     *
     */
    public CmsWorkplaceAction() {

        // nothing has to be done here
    }

    /**
     * Returns the instance of the login manager to use.<p>
     *
     * @return the instance of the login manager to use
     */
    public static synchronized CmsWorkplaceAction getInstance() {

        if (m_instance == null) {
            m_instance = new CmsWorkplaceAction();
        }
        return m_instance;
    }

    /**
     * Gets the admin cmsObject.<p>
     *
     * @return Admin cmsObject
     *
     * @throws CmsException is something goes wrong
     */
    public CmsObject getCmsAdminObject() throws CmsException {

        return OpenCms.initCmsObject(m_cmsAdmin);
    }

    /**
     * @see org.opencms.module.A_CmsModuleAction#initialize(org.opencms.file.CmsObject, org.opencms.configuration.CmsConfigurationManager, org.opencms.module.CmsModule)
     */
    @Override
    public void initialize(CmsObject adminCms, CmsConfigurationManager configurationManager, CmsModule module) {

        super.initialize(adminCms, configurationManager, module);

        // set the CmsObject initialized as an administrator at the only existing instance
        m_cmsAdmin = adminCms;
        m_instance = this;
    }
}

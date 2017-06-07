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

package org.opencms.module;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;

/**
 * Simple test implementation of the module action interface.<p>
 */
public class TestModuleActionImpl extends A_CmsModuleAction {

    /** Indicates the last event type caught. */
    public static int m_cmsEvent = -1;

    /** Indicates if the initialize() method was called. */
    public static boolean m_initialize;

    /** Indicates if the moduleUninstall() method was called. */
    public static boolean m_moduleUninstall;

    /** Indicates if the moduleUpdate() method was called. */
    public static boolean m_moduleUpdate;

    /** Indicates if the publishProject() method was called. */
    public static boolean m_publishProject;

    /** Indicates if the shutDown() method was called. */
    public static boolean m_shutDown;

    /**
     * Default constructor.<p>
     */
    public TestModuleActionImpl() {

        // noop
    }

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    @Override
    public void cmsEvent(CmsEvent event) {

        super.cmsEvent(event);
        m_cmsEvent = event.getType();
    }

    /**
     * @see org.opencms.module.I_CmsModuleAction#initialize(org.opencms.file.CmsObject, CmsConfigurationManager, CmsModule)
     */
    @Override
    public void initialize(CmsObject adminCms, CmsConfigurationManager configurationManager, CmsModule module) {

        super.initialize(adminCms, configurationManager, module);
        m_initialize = true;
        m_shutDown = false;

        // register as event listener for publish events
        OpenCms.addCmsEventListener(this, new int[] {I_CmsEventListener.EVENT_PUBLISH_PROJECT});
    }

    /**
     * @see org.opencms.module.I_CmsModuleAction#moduleUninstall(CmsModule)
     */
    @Override
    public void moduleUninstall(CmsModule module) {

        super.moduleUninstall(module);
        m_moduleUninstall = true;

        // remove event listener
        OpenCms.removeCmsEventListener(this);
    }

    /**
     * @see org.opencms.module.I_CmsModuleAction#moduleUpdate(org.opencms.module.CmsModule)
     */
    @Override
    public void moduleUpdate(CmsModule module) {

        super.moduleUpdate(module);
        m_moduleUpdate = true;
    }

    /**
     * @see org.opencms.module.I_CmsModuleAction#publishProject(org.opencms.file.CmsObject, org.opencms.db.CmsPublishList, int, org.opencms.report.I_CmsReport)
     */
    @Override
    public void publishProject(CmsObject cms, CmsPublishList publishList, int publishTag, I_CmsReport report) {

        super.publishProject(cms, publishList, publishTag, report);
        m_publishProject = true;
    }

    /**
     * @see org.opencms.module.I_CmsModuleAction#shutDown(CmsModule)
     */
    @Override
    public void shutDown(CmsModule module) {

        super.shutDown(module);
        m_shutDown = true;
    }

}

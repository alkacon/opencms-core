/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/module/A_CmsModuleAction.java,v $
 * Date   : $Date: 2005/02/17 12:44:35 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.module;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsEvent;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;

/**
 * Simple base implementation of the {@link I_CmsModuleAction} interface,
 * extend this class for more sophisticated module action implementations.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3.6
 */
public abstract class A_CmsModuleAction implements I_CmsModuleAction {

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug(this.getClass().getName() + " caugth event: " + event.getType());
        }
    }

    /**
     * @see org.opencms.module.I_CmsModuleAction#initialize(org.opencms.file.CmsObject, CmsConfigurationManager, CmsModule)
     */
    public void initialize(CmsObject adminCms, CmsConfigurationManager configurationManager, CmsModule module) {

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Module '" + module.getName() + "' " + this.getClass().getName() + " initialized");
        }      
    }
    
    /**
     * @see org.opencms.module.I_CmsModuleAction#moduleUninstall(CmsModule)
     */
    public void moduleUninstall(CmsModule module) {

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Module '" + module.getName() + "' " + this.getClass().getName() + " uninstalled");
        }        
    }    
    
    /**
     * @see org.opencms.module.I_CmsModuleAction#moduleUpdate(org.opencms.module.CmsModule)
     */
    public void moduleUpdate(CmsModule module) {

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Module '" + module.getName() + "' " + this.getClass().getName() + " updated");
        }         
    }    
        
    /**
     * @see org.opencms.module.I_CmsModuleAction#publishProject(org.opencms.file.CmsObject, org.opencms.db.CmsPublishList, int, org.opencms.report.I_CmsReport)
     */
    public void publishProject(CmsObject cms, CmsPublishList publishList, int backupTagId, I_CmsReport report) {

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug(this.getClass().getName() + " publishing");
        }
    }

    /**
     * @see org.opencms.module.I_CmsModuleAction#shutDown(CmsModule)
     */
    public void shutDown(CmsModule module) {

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Module '" + module.getName() + "' " + this.getClass().getName() + " shutting down");
        }         
    }
}

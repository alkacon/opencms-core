/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/threads/Attic/CmsPublishThread.java,v $
 * Date   : $Date: 2005/03/03 13:33:12 $
 * Version: $Revision: 1.19 $
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

package org.opencms.threads;

import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsSessionInfo;
import org.opencms.main.CmsSessionInfoManager;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.Iterator;
import java.util.List;

/**
 * Publishes a resource or the users current project.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.19 $
 * @since 5.1.10
 */
public class CmsPublishThread extends A_CmsReportThread {
    
    /** The list of resources to publish. */
    private CmsPublishList m_publishList;
    
    /** The CmsObject used to start this thread. */
    private CmsObject m_cms;
    
    /** The workplace settings of the current user. */
    private CmsWorkplaceSettings m_settings;

    /** Flag for updating the user info. */
    private boolean m_updateUserInfo;
    
    /**
     * Creates a Thread that publishes the Cms resources contained in the specified Cms publish 
     * list.<p>
     * 
     * @param cms the current OpenCms context object
     * @param publishList a Cms publish list
     * @param settings the workplace settings of the current user
     * @see org.opencms.file.CmsObject#getPublishList(org.opencms.file.CmsResource, boolean)
     * @see org.opencms.file.CmsObject#getPublishList()
     */
    public CmsPublishThread(CmsObject cms, CmsPublishList publishList, CmsWorkplaceSettings settings) {
        super(cms, "OpenCms: Publishing of resources in publish list");
        m_cms = cms;
        m_publishList = publishList;
        m_settings = settings;
        
        // if the project to publish is a temporary project, we have to update the
        // user info after publishing
        if (m_cms.getRequestContext().currentProject().getType() == I_CmsConstants.C_PROJECT_TYPE_TEMPORARY) {
            m_updateUserInfo = true;
        } else {
            m_updateUserInfo = false;
        }
        
        initHtmlReport(cms.getRequestContext().getLocale());
    }
    
    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    public String getReportUpdate() {
        return getReport().getReportUpdate();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            getReport().println(getReport().key("report.publish_resource_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            getCms().publishProject(getReport(), m_publishList);
            if (m_updateUserInfo) {
                updateUserSession();
            }
            getReport().println(getReport().key("report.publish_resource_end"), I_CmsReport.C_FORMAT_HEADLINE);
        } catch (Exception e) {
            getReport().println(e);
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error publishing project", e);
            }
        }
    }
    
    
    /**
     * Updates the project information in the user session and the workplace settings 
     * after a temporary project is published and deleted.<p>
     * 
     * This is nescessary to prevent the access to a nonexisting project.
     */
    private void updateUserSession() {
        // get the session menager
        CmsSessionInfoManager manager = OpenCms.getSessionInfoManager();      
         
        // get all user sessions for the current user
        List userSessions = manager.getUserSessions(m_cms.getRequestContext().currentUser().getId());
        Iterator i = userSessions.iterator();
        while (i.hasNext()) {
            CmsSessionInfo sessionInfo = (CmsSessionInfo)i.next();
            // check is the project stored in this session is not existing anymore
            // if so, set it to the online project
            Integer projectId = sessionInfo.getProject();
            try {
                m_cms.readProject(projectId.intValue());
            } catch (CmsException e) {
                // the project does not exist, update the project information
                //sessionInfo.setProject(new Integer(m_cms.getRequestContext().currentProject().getId()));
                sessionInfo.update(m_cms.getRequestContext());
                // update the workplace settings as well
                m_settings.setProject(m_cms.getRequestContext().currentProject().getId());
                getReport().println(getReport().key("report.publish_resource_switch_project")+  m_cms.getRequestContext().currentProject().getName(), I_CmsReport.C_FORMAT_DEFAULT);
            }
        }        
    }
    
    
}

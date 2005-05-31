/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/threads/Attic/CmsPublishThread.java,v $
 * Date   : $Date: 2005/05/31 11:08:23 $
 * Version: $Revision: 1.2 $
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

package org.opencms.workplace.threads;

import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsSessionInfo;
import org.opencms.main.CmsSessionManager;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Publishes a resource or the users current project.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.2 $
 * @since 5.1.10
 */
public class CmsPublishThread extends A_CmsReportThread {
    
    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPublishThread.class); 
    
    /** The list of resources to publish. */
    private CmsPublishList m_publishList;
    
    /** The CmsObject used to start this thread. */
    private CmsObject m_cms;
    
    /** The workplace settings of the current user. */
    private CmsWorkplaceSettings m_settings;

    /** Flag for updating the user info. */
    private boolean m_updateSessionInfo;
    
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
        super(cms, Messages.get().key(
            cms.getRequestContext().getLocale(),
            Messages.GUI_PUBLISH_TRHEAD_NAME_0,
            null));
        m_cms = cms;
        m_publishList = publishList;
        m_settings = settings;
        
        // if the project to publish is a temporary project, we have to update the
        // user info after publishing
        if (m_cms.getRequestContext().currentProject().getType() == I_CmsConstants.C_PROJECT_TYPE_TEMPORARY) {
            m_updateSessionInfo = true;
        } else {
            m_updateSessionInfo = false;
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
            getReport().println(
                Messages.get().container(Messages.RPT_PUBLISH_RESOURCE_BEGIN_0),
                I_CmsReport.C_FORMAT_HEADLINE);
            getCms().publishProject(getReport(), m_publishList);
            if (m_updateSessionInfo) {
                updateSessionInfo();
            }
            getReport().println(
                Messages.get().container(Messages.RPT_PUBLISH_RESOURCE_END_0),
                I_CmsReport.C_FORMAT_HEADLINE);
        } catch (Exception e) {
            getReport().println(e);
            LOG.error(Messages.get().key(Messages.LOG_PUBLISH_PROJECT_FAILED_0), e);
        }
    }
        
    /**
     * Updates the project information in the user session and the workplace settings 
     * after a temporary project is published and deleted.<p>
     * 
     * This is nescessary to prevent the access to a nonexisting project.<p>
     */
    private void updateSessionInfo() {
        // get the session menager
        CmsSessionManager sessionManager = OpenCms.getSessionManager();      
         
        // get all sessions
        List userSessions = sessionManager.getSessionInfos();
        Iterator i = userSessions.iterator();
        while (i.hasNext()) {
            CmsSessionInfo sessionInfo = (CmsSessionInfo)i.next();
            // check is the project stored in this session is not existing anymore
            // if so, set it to the online project
            int projectId = sessionInfo.getProject();
            try {
                m_cms.readProject(projectId);
            } catch (CmsException e) {
                // the project does not longer exist, update the project information with the online project
                sessionInfo.setProject(I_CmsConstants.C_PROJECT_ONLINE_ID);
                // update the workplace settings as well
                m_settings.setProject(I_CmsConstants.C_PROJECT_ONLINE_ID);
                getReport().println(
                    Messages.get().container(
                        Messages.RPT_PUBLISH_RESOURCE_SWITCH_PROJECT_1,
                        m_cms.getRequestContext().currentProject().getName()),
                    I_CmsReport.C_FORMAT_DEFAULT);
            }
        }        
    }
    
    
}

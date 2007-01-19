/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/publish/CmsPublishThread.java,v $
 * Date   : $Date: 2007/01/19 16:53:52 $
 * Version: $Revision: 1.1.2.2 $
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

package org.opencms.publish;

import org.opencms.db.CmsDbContext;
import org.opencms.file.CmsProject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsSessionInfo;
import org.opencms.main.CmsSessionManager;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Publishes a resource or the users current project.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.5.5 
 */
final class CmsPublishThread extends A_CmsReportThread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPublishThread.class);

    /** The publish engine instance. */
    private final CmsPublishEngine m_publishEngine;

    /** The publish job to start this thread for. */
    private final CmsPublishJobInfoBean m_publishJob;

    /** The report to use during the publish process. */
    private I_CmsReport m_report;

    /** Flag for updating the user info. */
    private final boolean m_updateSessionInfo;

    /**
     * Creates a thread that start a new publish job with the given information.<p>
     * 
     * @param publishEngine the publish engine instance
     * @param publishJob the publish job to process
     * 
     * @see org.opencms.publish.CmsPublishManager#getPublishList(org.opencms.file.CmsObject, org.opencms.file.CmsResource, boolean)
     * @see org.opencms.publish.CmsPublishManager#getPublishList(org.opencms.file.CmsObject)
     */
    protected CmsPublishThread(CmsPublishEngine publishEngine, CmsPublishJobInfoBean publishJob) {

        super(publishJob.getCmsObject(), Messages.get().getBundle().key(Messages.GUI_PUBLISH_TRHEAD_NAME_0));
        m_publishJob = publishJob;
        m_publishEngine = publishEngine;

        // if the project to publish is a temporary project
        if (getCms().getRequestContext().currentProject().getType() == CmsProject.PROJECT_TYPE_TEMPORARY) {
            // we have to update the user info after publishing
            m_updateSessionInfo = true;
        } else {
            m_updateSessionInfo = false;
        }
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
            // signalize that the thread has been started
            m_publishEngine.publishThreadStarted();

            // set the report
            m_report = m_publishJob.getPublishReport();

            // start
            m_report.println(Messages.get().container(Messages.RPT_PUBLISH_RESOURCE_BEGIN_0), I_CmsReport.FORMAT_HEADLINE);

            CmsDbContext dbc = m_publishEngine.getDbContextFactory().getDbContext(getCms().getRequestContext());
            try {
                m_publishEngine.getDriverManager().publishJob(getCms(), dbc, m_publishJob.getPublishList(), m_report);
            } catch (Throwable e) {
                // catch every thing including runtime exceptions
                m_report.println(e);
                LOG.error(Messages.get().getBundle().key(Messages.LOG_PUBLISH_PROJECT_FAILED_0), e);
            } finally {
                dbc.clear();
                if (m_updateSessionInfo) {
                    updateSessionInfo();
                }
                m_report.println(
                    Messages.get().container(Messages.RPT_PUBLISH_RESOURCE_END_0),
                    I_CmsReport.FORMAT_HEADLINE);
            }
        } catch (Throwable e) {
            // catch every thing including runtime exceptions
            LOG.error(Messages.get().getBundle().key(Messages.LOG_PUBLISH_PROJECT_FAILED_0), e);
        } finally {
            // signalize that the thread has been finished
            m_publishEngine.publishThreadFinished();
        }
    }

    /**
     * Returns the publish job for this thread.<p>
     * 
     * @return the publish job for this thread
     */
    protected CmsPublishJobInfoBean getPublishJob() {

        return m_publishJob;
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReport()
     */
    protected I_CmsReport getReport() {

        return m_report;
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
                getCms().readProject(projectId);
            } catch (CmsException e) {
                // the project does not longer exist, update the project information with the online project
                sessionInfo.setProject(CmsProject.ONLINE_PROJECT_ID);
                getReport().println(
                    Messages.get().container(
                        Messages.RPT_PUBLISH_RESOURCE_SWITCH_PROJECT_1,
                        getCms().getRequestContext().currentProject().getName()),
                    I_CmsReport.FORMAT_DEFAULT);
            }
        }
    }
}
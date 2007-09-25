/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/publish/CmsPublishThread.java,v $
 * Date   : $Date: 2007/09/25 09:25:45 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2007 Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;

import org.apache.commons.logging.Log;

/**
 * Publishes a resource or the users current project.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.5 $ 
 * 
 * @since 6.5.5 
 */
final class CmsPublishThread extends A_CmsReportThread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPublishThread.class);

    /** the aborted flag. */
    private boolean m_abort;

    /** The publish engine instance. */
    private final CmsPublishEngine m_publishEngine;

    /** The publish job to start this thread for. */
    private CmsPublishJobInfoBean m_publishJob;

    /** The report to use during the publish process. */
    private I_CmsReport m_report;

    /** Flag to indicate that the is no longer possible to abort the current publish job. */
    private boolean m_started;

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
     * Checks if the current publish job has been aborted.<p>
     * 
     * @return <code>true</code> if the current publish job has been aborted
     */
    public boolean isAborted() {

        return m_abort;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {

        try {
            // start the job
            m_publishJob.start(getUUID());
            // set the report
            m_report = m_publishJob.getPublishReport();
            // signalizes that the thread has been started
            m_publishEngine.publishJobStarted(m_publishJob);
            m_started = true;
            if (isAborted()) {
                return;
            }

            m_report.println(
                Messages.get().container(Messages.RPT_PUBLISH_RESOURCE_BEGIN_0),
                I_CmsReport.FORMAT_HEADLINE);

            CmsDbContext dbc = m_publishEngine.getDbContextFactory().getDbContext(getCms().getRequestContext());
            try {
                m_publishEngine.getDriverManager().publishJob(getCms(), dbc, m_publishJob.getPublishList(), m_report);
            } catch (Throwable e) {
                // catch every thing including runtime exceptions
                m_report.println(e);
                LOG.error(Messages.get().getBundle().key(Messages.LOG_PUBLISH_PROJECT_FAILED_0), e);
            } finally {
                dbc.clear();
                dbc = null;
                if (m_updateSessionInfo) {
                    OpenCms.getSessionManager().updateSessionInfos(getCms());
                }
                m_report.println(
                    Messages.get().container(Messages.RPT_PUBLISH_RESOURCE_END_0),
                    I_CmsReport.FORMAT_HEADLINE);
            }
        } catch (Throwable e) {
            // catch every thing including runtime exceptions
            LOG.error(Messages.get().getBundle().key(Messages.LOG_PUBLISH_PROJECT_FAILED_0), e);
        } finally {
            // Signalizes that the thread has been finished
            try {
                m_publishEngine.publishJobFinished(getPublishJob());
            } catch (CmsException exc) {
                LOG.error(exc.getLocalizedMessage(), exc);
            } catch (Throwable e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Aborts the current job.<p>
     * 
     * This can only be done until the publish job started event is fired.<p>
     *  
     * @throws CmsPublishException if the current publish can not be aborted
     */
    protected void abort() throws CmsPublishException {

        if (m_started) {
            throw new CmsPublishException(Messages.get().container(Messages.ERR_PUBLISH_ENGINE_MISSING_PUBLISH_JOB_0));
        }
        m_abort = true;
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
}
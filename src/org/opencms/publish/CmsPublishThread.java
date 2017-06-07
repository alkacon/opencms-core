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

package org.opencms.publish;

import org.opencms.db.CmsDbContext;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.loader.CmsJspLoader;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Publishes a resource or the users current project.<p>
 *
 * @since 6.5.5
 */
/* default */final class CmsPublishThread extends A_CmsReportThread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPublishThread.class);

    /** JSP Loader instance. */
    protected CmsJspLoader m_jspLoader;

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
        if (getCms().getRequestContext().getCurrentProject().getType() == CmsProject.PROJECT_TYPE_TEMPORARY) {
            // we have to update the user info after publishing
            m_updateSessionInfo = true;
        } else {
            m_updateSessionInfo = false;
        }
        try {
            m_jspLoader = (CmsJspLoader)OpenCms.getResourceManager().getLoader(CmsJspLoader.RESOURCE_LOADER_ID);
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore, loader not configured
        }
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    @Override
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
    @Override
    public void run() {

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

        m_report.println(Messages.get().container(Messages.RPT_PUBLISH_RESOURCE_BEGIN_0), I_CmsReport.FORMAT_HEADLINE);

        Set<String> includingFiles = null;
        if (m_jspLoader != null) {
            includingFiles = getStrongLinkReferences();
        }
        try {
            CmsDbContext dbc = m_publishEngine.getDbContext(getCms().getRequestContext());
            try {
                // publish
                m_publishEngine.getDriverManager().publishJob(getCms(), dbc, m_publishJob.getPublishList(), m_report);
            } catch (Throwable e) {
                // catch every thing including runtime exceptions
                dbc.rollback();
                m_report.println(e);
                LOG.error(Messages.get().getBundle().key(Messages.LOG_PUBLISH_PROJECT_FAILED_0), e);
            } finally {
                dbc.clear();
                dbc = null;
                if (m_updateSessionInfo) {
                    OpenCms.getSessionManager().updateSessionInfos(getCms());
                }
                if (m_jspLoader != null) {
                    // update jsp loader cache
                    m_jspLoader.removeFromCache(includingFiles, true);
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
    @Override
    protected I_CmsReport getReport() {

        return m_report;
    }

    /**
     * Returns the set of all strong link references from the files to publish.<p>
     *
     * @return the set of all strong link references
     */
    protected Set<String> getStrongLinkReferences() {

        Set<String> includingFiles = new HashSet<String>();
        CmsDbContext dbc = m_publishEngine.getDbContext(getCms().getRequestContext());
        try {
            // use the online project
            CmsObject jspCms = OpenCms.initCmsObject(m_publishJob.getCmsObject());
            jspCms.getRequestContext().setCurrentProject(jspCms.readProject(CmsProject.ONLINE_PROJECT_ID));
            // collect jsp referencing information
            m_report.println(
                Messages.get().container(Messages.RPT_JSPLOADER_UPDATE_CACHE_BEGIN_0),
                I_CmsReport.FORMAT_HEADLINE);
            int plainId = OpenCms.getResourceManager().getResourceType(
                CmsResourceTypePlain.getStaticTypeName()).getTypeId();
            Iterator<CmsResource> it = m_publishJob.getPublishList().getFileList().iterator();
            while (it.hasNext()) {
                CmsResource resource = it.next();
                if ((CmsResourceTypeJsp.isJsp(resource)) || (resource.getTypeId() == plainId)) {
                    m_report.print(
                        Messages.get().container(Messages.RPT_JSPLOADER_UPDATE_CACHE_0),
                        I_CmsReport.FORMAT_NOTE);
                    m_report.print(
                        org.opencms.report.Messages.get().container(
                            org.opencms.report.Messages.RPT_ARGUMENT_1,
                            dbc.removeSiteRoot(resource.getRootPath())));
                    m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                    m_jspLoader.getReferencingStrongLinks(jspCms, resource, includingFiles);
                    m_report.println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                        I_CmsReport.FORMAT_OK);
                }
            }
        } catch (Throwable e) {
            // catch every thing including runtime exceptions
            m_report.println(e);
        } finally {
            dbc.clear();
            dbc = null;
            m_report.println(
                Messages.get().container(Messages.RPT_JSPLOADER_UPDATE_CACHE_END_0),
                I_CmsReport.FORMAT_HEADLINE);
        }
        return includingFiles;
    }
}
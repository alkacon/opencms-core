/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/publish/CmsPublishJobInfoBean.java,v $
 * Date   : $Date: 2006/12/20 14:01:20 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2006 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsUUID;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Publish job information bean.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.2 $
 * 
 * @since 6.5.5
 */
final class CmsPublishJobInfoBean {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPublishJobInfoBean.class);

    /** The cms context to use for publishing, will be set to <code>null</code> after publishing. */
    private CmsObject m_cms;

    /** If this is a "direct publish" operation. */
    private final boolean m_directPublish;

    /** Time of creation of this object. */
    private long m_enqueueTime;

    /** Time the publish job did end. */
    private long m_finishTime;

    /** The locale to use for publishing. */
    private final Locale m_locale;

    /** Project to use for publishing. */
    private final CmsProject m_project;

    /** Publish history id. */
    private CmsUUID m_publishHistoryId;

    /** List of resources to publish, will be set to <code>null</code> after publishing. */
    private CmsPublishList m_publishList;

    /** The report to use during the publish process, will be set to <code>null</code> after publishing. */
    private I_CmsReport m_publishReport;

    /** Report to log the publish job to, will be set to <code>null</code> after publishing. */
    private I_CmsReport m_report;

    /** Path in the RFS to temporary store the report for the publish job. */
    private String m_reportFilePath;

    /** Number of resources to publish. */
    private final int m_size;

    /** Time the publish job did actually start. */
    private long m_startTime;

    private CmsUUID m_threadUUID;

    /** User to use for publishing. */
    private final CmsUser m_user;

    /**
     * The Default constructor.<p>
     * 
     * @param cms the cms context to use for publishing
     * @param publishList the list of resources to publish
     * @param report the report to write to
     * @param repositoryPath the folder path for the temporary report outputs
     * 
     * @throws CmsException if something goes wrong
     */
    protected CmsPublishJobInfoBean(CmsObject cms, CmsPublishList publishList, I_CmsReport report, String repositoryPath)
    throws CmsException {

        m_cms = OpenCms.initCmsObject(cms);
        m_project = m_cms.getRequestContext().currentProject();
        m_user = m_cms.getRequestContext().currentUser();
        m_locale = m_cms.getRequestContext().getLocale();

        m_publishList = publishList;
        m_publishHistoryId = m_publishList.getPublishHistoryId();

        m_size = m_publishList.size();
        m_directPublish = m_publishList.isDirectPublish();

        m_report = report;
        m_reportFilePath = repositoryPath;
    }

    /**
     * Returns the time this object has been created.<p>
     *
     * @return the time this object has been created
     */
    public long getEnqueueTime() {

        return m_enqueueTime;
    }

    /**
     * Returns the time the publish job ends.<p>
     *
     * @return the time the publish job ends
     */
    public long getFinishTime() {

        return m_finishTime;
    }

    /**
     * Returns the locale for this publish job.<p>
     * 
     * @return the locale for this publish job
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the project for this publish job.<p>
     * 
     * @return the project for this publish job
     */
    public CmsProject getProject() {

        return m_project;
    }

    /**
     * Returns the project name or {@link Messages#GUI_DIRECT_PUBLISH_PROJECT_NAME_0}
     * if it is a direct publish job.<p>
     * 
     * @param locale the locale
     * 
     * @return the project name
     */
    public String getProjectName(Locale locale) {

        if (locale == null) {
            locale = CmsLocaleManager.getDefaultLocale();
        }
        return isDirectPublish() ? Messages.get().getBundle(locale).key(Messages.GUI_DIRECT_PUBLISH_PROJECT_NAME_0)
        : getProject().getName();
    }

    /**
     * Returns the publish history id.<p>
     * 
     * @return the publish history id
     */
    public CmsUUID getPublishHistoryId() {

        return m_publishHistoryId;
    }

    /**
     * Returns the list of resources to publish.<p>
     *
     * @return the list of resources to publish
     */
    public CmsPublishList getPublishList() {

        return m_publishList;
    }

    /**
     * Returns the report for this publish job.<p>
     * 
     * This is not the original report, it is wrapper that 
     * also writes to a temporary file.<p>
     * 
     * It will be <code>null</code> before starting and after finishing.<p>
     * 
     * @return the report for this publish job
     * 
     * @see CmsPublishJobEnqueued#getReport()
     */
    public I_CmsReport getPublishReport() {

        if (m_publishReport == null && m_finishTime == 0 && m_startTime > 0) {
            m_publishReport = getReport();
            try {
                if (m_publishReport == null) {
                    m_publishReport = new CmsPublishReport(
                        getCmsObject().getRequestContext().getLocale(),
                        getReportFilePath());
                } else {
                    m_publishReport = CmsPublishReport.decorate(m_publishReport, getReportFilePath());
                }
            } catch (IOException e) {
                LOG.error(Messages.get().container(
                    Messages.ERR_PUBLISH_ENGINE_CREATE_REPORT_FILE_1,
                    getReportFilePath()));
                if (m_publishReport == null) {
                    m_publishReport = new CmsShellReport(getCmsObject().getRequestContext().getLocale());
                }
            }
        }
        return m_publishReport;
    }

    /**
     * Returns the report for this publish job.<p>
     * 
     * @return the report for this publish job
     */
    public I_CmsReport getReport() {

        return m_report;
    }

    /**
     * Returns the path in the RFS to temporary store the report for the publish job.<p>
     *
     * @return the path in the RFS to temporary store the report for the publish job
     */
    public String getReportFilePath() {

        if (!m_reportFilePath.endsWith(".html")) {
            // initialize the report path
            m_reportFilePath += File.separator + CmsPublishJobBase.REPORT_FILENAME_PREFIX;
            m_reportFilePath += getProjectName(m_locale) + CmsPublishJobBase.REPORT_FILENAME_SEPARATOR;
            m_reportFilePath += getUser().getName() + CmsPublishJobBase.REPORT_FILENAME_SEPARATOR;
            m_reportFilePath += m_enqueueTime + CmsPublishJobBase.REPORT_FILENAME_POSTFIX;
        }
        return m_reportFilePath;
    }

    /**
     * Returns the number of resources in the publish list.<p>
     * 
     * @return the number of resources in the publish list
     */
    public int getSize() {

        return m_size;
    }

    /**
     * Returns the time the publish job did actually start.<p>
     *
     * @return the time the publish job did actually start
     */
    public long getStartTime() {

        return m_startTime;
    }

    /**
     * Returns the UUID of the running publish thread.<p>
     * 
     * @return the UUID of the running publish thread
     */
    public CmsUUID getThreadUUID() {

        return m_threadUUID;
    }

    /**
     * Returns the user for this publish job.<p>
     * 
     * @return the user for this publish job
     */
    public CmsUser getUser() {

        return m_user;
    }

    /**
     * Signalizes that the publish job has been enqueued.<p> 
     * Actually sets the enqueue time and generates the file name to use for the report.<p>
     */
    protected void enqueue() {

        if (m_enqueueTime != 0) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_PUBLISH_JOB_ALREADY_ENQUEUED_0));
        }
        m_enqueueTime = System.currentTimeMillis();
    }

    /**
     * Signalizes the end of the publish job.<p> 
     * Actually only sets the finish time and closes the publish report stream.<p>
     */
    protected void finish() {

        if (m_finishTime != 0) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_PUBLISH_JOB_ALREADY_FINISHED_0));
        }
        m_cms = null;
        m_report = null;
        m_publishList = null;
        if (m_publishReport instanceof CmsPublishReport) {
            ((CmsPublishReport)m_publishReport).finish();
        }
        m_publishReport = null;
        m_threadUUID = null;
        m_finishTime = System.currentTimeMillis();
    }

    /**
     * Returns the cms object, will be set to <code>null</code> after publishing.<p>
     *
     * @return the cms object
     */
    protected CmsObject getCmsObject() {

        return m_cms;
    }

    /**
     * Returns <code>true</code> if this is a "direct publish" operation.<p>
     *
     * @return <code>true</code> if this is a "direct publish" operation
     */
    protected boolean isDirectPublish() {

        return m_directPublish;
    }

    /**
     * Signalizes the start of the publish job.<p> 
     * Actually sets the starting time, writes the report header and sets the running thread uuid.<p>
     * 
     * @param threadUUID the running thread uuid
     */
    protected void start(CmsUUID threadUUID) {

        if (m_startTime != 0) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_PUBLISH_JOB_ALREADY_STARTED_0));
        }
        m_startTime = System.currentTimeMillis();
        m_threadUUID = threadUUID;
        if (getPublishReport() instanceof CmsPublishReport) {
            ((CmsPublishReport)m_publishReport).start();
        }
    }
}

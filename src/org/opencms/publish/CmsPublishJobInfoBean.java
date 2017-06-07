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

import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsUUID;

import java.util.Locale;

/**
 * Publish job information bean.<p>
 *
 * @since 6.5.5
 */
public final class CmsPublishJobInfoBean {

    /** The flag used to indicate a direct publish job. */
    public static final int C_PUBLISH_FLAG = 1;

    /** The cms context to use for publishing, will be set to <code>null</code> after publishing. */
    private CmsObject m_cms;

    /** If this is a "direct publish" operation. */
    private boolean m_directPublish;

    /** Time of creation of this object. */
    private long m_enqueueTime;

    /** Time the publish job did end. */
    private long m_finishTime;

    /** The locale to use for publishing. */
    private Locale m_locale;

    /** Project to use for publishing. */
    private CmsUUID m_projectId;

    /** Name of the project used for publishing. */
    private String m_projectName;

    /** Publish history id. */
    private CmsUUID m_publishHistoryId;

    /** List of resources to publish, will be set to <code>null</code> after publishing. */
    private CmsPublishList m_publishList;

    /** The report to use during the publish process, will be set to <code>null</code> after publishing. */
    private I_CmsReport m_publishReport;

    /** Report to log the publish job to, will be set to <code>null</code> after publishing. */
    private I_CmsReport m_report;

    /** Number of resources to publish. */
    private int m_size;

    /** Time the publish job did actually start. */
    private long m_startTime;

    /** The UUID of the running publish thread. */
    private CmsUUID m_threadUUID;

    /** User to use for publishing. */
    private CmsUUID m_userId;

    /**
     * Constructor used to initialize a job info bean from the database.<p>
     *
     * @param historyId publish history id
     * @param projectId the id of the project
     * @param projectName the name of the project
     * @param userId the id of the user
     * @param localeName the string representation of a locale
     * @param flags flags of the publish job
     * @param resourceCount number of published resources
     * @param enqueueTime time when the job was enqueued
     * @param startTime time when the job was started
     * @param finishTime time when the job was finished
     */
    public CmsPublishJobInfoBean(
        CmsUUID historyId,
        CmsUUID projectId,
        String projectName,
        CmsUUID userId,
        String localeName,
        int flags,
        int resourceCount,
        long enqueueTime,
        long startTime,
        long finishTime) {

        m_publishHistoryId = historyId;
        m_projectId = projectId;

        m_projectName = projectName;
        m_userId = userId;
        m_size = resourceCount;
        m_directPublish = ((flags & C_PUBLISH_FLAG) == C_PUBLISH_FLAG);

        m_enqueueTime = enqueueTime;
        m_startTime = startTime;
        m_finishTime = finishTime;

        m_locale = CmsLocaleManager.getLocale(localeName);
    }

    /**
     * The Default constructor.<p>
     *
     * @param cms the cms context to use for publishing
     * @param publishList the list of resources to publish
     * @param report the report to write to
     *
     * @throws CmsException if something goes wrong
     */
    protected CmsPublishJobInfoBean(CmsObject cms, CmsPublishList publishList, I_CmsReport report)
    throws CmsException {

        m_cms = OpenCms.initCmsObject(cms);
        m_projectId = m_cms.getRequestContext().getCurrentProject().getUuid();
        m_projectName = m_cms.getRequestContext().getCurrentProject().getName();
        m_userId = m_cms.getRequestContext().getCurrentUser().getId();
        m_locale = m_cms.getRequestContext().getLocale();

        m_publishList = publishList;
        m_publishHistoryId = m_publishList.getPublishHistoryId();

        m_size = m_publishList.size();
        m_directPublish = m_publishList.isDirectPublish();

        m_report = report;
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
     * Returns the flags of this publish job.<p>
     *
     * @return the flags of this publish job
     */
    public int getFlags() {

        return (m_directPublish) ? C_PUBLISH_FLAG : 0;
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
     * Returns the project id for this publish job.<p>
     *
     * @return the project id for this publish job
     */
    public CmsUUID getProjectId() {

        return m_projectId;
    }

    /**
     * Returns the originally stored project name.<p>
     *
     * @return the originally stored project name
     */
    public String getProjectName() {

        return m_projectName;
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

        if ((m_publishReport == null) && (m_finishTime == 0) && (m_startTime > 0)) {
            m_publishReport = getReport();
            if (m_publishReport == null) {
                m_publishReport = new CmsPublishReport(getCmsObject().getRequestContext().getLocale());
            } else {
                m_publishReport = CmsPublishReport.decorate(m_publishReport);
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
    public CmsUUID getUserId() {

        return m_userId;
    }

    /**
     * Removes the assigned publish report.<p>
     *
     * @return the removed report
     */
    public I_CmsReport removePublishReport() {

        I_CmsReport report = m_publishReport;
        m_publishReport = null;
        return report;
    }

    /**
     * Revives this publish job.<p>
     *
     * @param adminCms an admin cms object
     * @param publishList a publish list
     *
     * @throws CmsException if something goes wrong
     */
    public void revive(CmsObject adminCms, CmsPublishList publishList) throws CmsException {

        CmsContextInfo context = new CmsContextInfo(adminCms.readUser(m_userId).getName());
        CmsProject project = adminCms.readProject(m_projectId);
        context.setLocale(m_locale);

        m_cms = OpenCms.initCmsObject(adminCms, context);
        m_cms.getRequestContext().setCurrentProject(project);

        m_publishList = publishList;
        m_publishList.revive(m_cms);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer result = new StringBuffer();

        result.append("[");
        result.append(this.getClass().getName());
        result.append(", history id: ");
        result.append(getPublishHistoryId().toString());
        result.append(", project id ");
        result.append(getProjectId().toString());
        result.append(", project name: ");
        result.append(getProjectName());
        result.append(", user id: ");
        result.append(getUserId().toString());
        result.append(", locale: ");
        result.append(getLocale().toString());
        result.append(", flags: ");
        result.append(getFlags());
        result.append(", size: ");
        result.append(getSize());
        result.append(", enqueue time: ");
        result.append(getEnqueueTime());
        result.append(", start time: ");
        result.append(getStartTime());
        result.append(", finish time: ");
        result.append(getFinishTime());
        result.append("]");

        return result.toString();
    }

    /**
     * Signalizes that the publish job has been enqueued.<p>
     * Actually sets the enqueue time only if it is not set already (re-enqueue during startup).<p>
     */
    protected void enqueue() {

        if (m_enqueueTime == 0L) {
            m_enqueueTime = System.currentTimeMillis();
        }
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
        m_size = m_publishList.size();
        m_publishList = null;
        if (m_publishReport instanceof CmsPublishReport) {
            ((CmsPublishReport)m_publishReport).finish();
        }
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
     * Returns if the publish job is already finished.<p>
     *
     * @return <code>true</code> if the publish job is already finished
     */
    protected boolean isFinished() {

        return (m_finishTime != 0L);
    }

    /**
     * Returns if the publish job is already started.<p>
     *
     * @return <code>true</code> if the publish job is already started
     */
    protected boolean isStarted() {

        return (m_startTime != 0L);
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

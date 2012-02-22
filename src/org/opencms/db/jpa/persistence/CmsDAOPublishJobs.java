/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.db.jpa.persistence;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * This data access object represents a publish job entry inside the table "cms_publish_jobs".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_PUBLISH_JOBS")
public class CmsDAOPublishJobs {

    /** The enqueue time. */
    @Basic
    @Column(name = "ENQUEUE_TIME")
    private long m_enqueueTime;

    /** The finish time. */
    @Basic
    @Column(name = "FINISH_TIME")
    private long m_finishTime;

    /** The history id. */
    @Id
    @Column(name = "HISTORY_ID", length = 36)
    private String m_historyId;

    /** The project id. */
    @Basic
    @Column(name = "PROJECT_ID", nullable = false, length = 36)
    private String m_projectId;

    /** The project name. */
    @Basic
    @Column(name = "PROJECT_NAME", nullable = false)
    private String m_projectName;

    /** The publish flags. */
    @Basic
    @Column(name = "PUBLISH_FLAGS")
    private int m_publishFlags;

    /** The publish list. */
    @Basic
    @Lob
    @Column(name = "PUBLISH_LIST")
    private byte[] m_publishList;

    /** The publish locale. */
    @Basic
    @Column(name = "PUBLISH_LOCALE", nullable = false, length = 16)
    private String m_publishLocale;

    /** The publish report. */
    @Basic
    @Lob
    @Column(name = "PUBLISH_REPORT")
    private byte[] m_publishReport;

    /** The resource count. */
    @Basic
    @Column(name = "RESOURCE_COUNT")
    private int m_resourceCount;

    /** The start time. */
    @Basic
    @Column(name = "START_TIME")
    private long m_startTime;

    /** The user id. */
    @Basic
    @Column(name = "USER_ID", nullable = false, length = 36)
    private String m_userId;

    /**
     * The default constructor.<p>
     */
    public CmsDAOPublishJobs() {

        // noop
    }

    /**
     * A public constructor for generating a new publish job object with an unique id.<p>
     * 
     * @param historyId the id
     */
    public CmsDAOPublishJobs(String historyId) {

        m_historyId = historyId;
    }

    /**
     * Returns the enqueueTime.<p>
     *
     * @return the enqueueTime
     */
    public long getEnqueueTime() {

        return m_enqueueTime;
    }

    /**
     * Returns the finishTime.<p>
     *
     * @return the finishTime
     */
    public long getFinishTime() {

        return m_finishTime;
    }

    /**
     * Returns the historyId.<p>
     *
     * @return the historyId
     */
    public String getHistoryId() {

        return m_historyId;
    }

    /**
     * Returns the projectId.<p>
     *
     * @return the projectId
     */
    public String getProjectId() {

        return m_projectId;
    }

    /**
     * Returns the projectName.<p>
     *
     * @return the projectName
     */
    public String getProjectName() {

        return m_projectName;
    }

    /**
     * Returns the publishFlags.<p>
     *
     * @return the publishFlags
     */
    public int getPublishFlags() {

        return m_publishFlags;
    }

    /**
     * Returns the publishList.<p>
     *
     * @return the publishList
     */
    public byte[] getPublishList() {

        return m_publishList;
    }

    /**
     * Returns the publishLocale.<p>
     *
     * @return the publishLocale
     */
    public String getPublishLocale() {

        return m_publishLocale;
    }

    /**
     * Returns the publishReport.<p>
     *
     * @return the publishReport
     */
    public byte[] getPublishReport() {

        return m_publishReport;
    }

    /**
     * Returns the resourceCount.<p>
     *
     * @return the resourceCount
     */
    public int getResourceCount() {

        return m_resourceCount;
    }

    /**
     * Returns the startTime.<p>
     *
     * @return the startTime
     */
    public long getStartTime() {

        return m_startTime;
    }

    /**
     * Returns the userId.<p>
     *
     * @return the userId
     */
    public String getUserId() {

        return m_userId;
    }

    /**
     * Sets the enqueueTime.<p>
     *
     * @param enqueueTime the enqueueTime to set
     */
    public void setEnqueueTime(long enqueueTime) {

        m_enqueueTime = enqueueTime;
    }

    /**
     * Sets the finishTime.<p>
     *
     * @param finishTime the finishTime to set
     */
    public void setFinishTime(long finishTime) {

        m_finishTime = finishTime;
    }

    /**
     * Sets the historyId.<p>
     *
     * @param historyId the historyId to set
     */
    public void setHistoryId(String historyId) {

        m_historyId = historyId;
    }

    /**
     * Sets the projectId.<p>
     *
     * @param projectId the projectId to set
     */
    public void setProjectId(String projectId) {

        m_projectId = projectId;
    }

    /**
     * Sets the projectName.<p>
     *
     * @param projectName the projectName to set
     */
    public void setProjectName(String projectName) {

        m_projectName = projectName;
    }

    /**
     * Sets the publishFlags.<p>
     *
     * @param publishFlags the publishFlags to set
     */
    public void setPublishFlags(int publishFlags) {

        m_publishFlags = publishFlags;
    }

    /**
     * Sets the publishList.<p>
     *
     * @param publishList the publishList to set
     */
    public void setPublishList(byte[] publishList) {

        m_publishList = publishList;
    }

    /**
     * Sets the publishLocale.<p>
     *
     * @param publishLocale the publishLocale to set
     */
    public void setPublishLocale(String publishLocale) {

        m_publishLocale = publishLocale;
    }

    /**
     * Sets the publishReport.<p>
     *
     * @param publishReport the publishReport to set
     */
    public void setPublishReport(byte[] publishReport) {

        m_publishReport = publishReport;
    }

    /**
     * Sets the resourceCount.<p>
     *
     * @param resourceCount the resourceCount to set
     */
    public void setResourceCount(int resourceCount) {

        m_resourceCount = resourceCount;
    }

    /**
     * Sets the startTime.<p>
     *
     * @param startTime the startTime to set
     */
    public void setStartTime(long startTime) {

        m_startTime = startTime;
    }

    /**
     * Sets the userId.<p>
     *
     * @param userId the userId to set
     */
    public void setUserId(String userId) {

        m_userId = userId;
    }

}
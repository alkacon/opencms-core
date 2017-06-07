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

import org.opencms.file.CmsResource;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

/**
 * Test event listener implementation.<p>
 *
 * @since 6.5.5
 */
public class TestPublishEventListener implements I_CmsPublishEventListener {

    private long m_aborted;
    private long m_enqueued;
    private long m_finished;
    private CmsPublishJobInfoBean m_publishJob;
    private long m_removed;
    private CmsResource m_resource;
    private long m_started;

    /** Number of jobs in Queue/History on Enqueue, Start, Finish, Abort, Remove. */
    private int[] m_jobsInQueue = new int[5];
    private int[] m_jobsInHistory = new int[5];

    /**
     * Default constructor.<p>
     *
     * @param resource the resource to track
     */
    public TestPublishEventListener(CmsResource resource) {

        m_resource = resource;
    }

    /**
     * Returns the aborted timestamp.<p>
     *
     * @return the aborted timestamp
     */
    public long getAborted() {

        return m_aborted;
    }

    /**
     * Returns the enqueued timestamp.<p>
     *
     * @return the enqueued timestamp
     */
    public long getEnqueued() {

        return m_enqueued;
    }

    /**
     * Returns the finished timestamp.<p>
     *
     * @return the finished timestamp
     */
    public long getFinished() {

        return m_finished;
    }

    /**
     * Returns the jobs in history counters.<p>
     *
     * @return the jobs in history counters
     */
    public int[] getJobsInHistoryCounter() {

        return m_jobsInHistory;
    }

    /**
     * Returns the jobs in queue counters.<p>
     *
     * @return the jobs in queue counters
     */
    public int[] getJobsInQueueCounter() {

        return m_jobsInQueue;
    }

    /**
     * Returns the removed timestamp.<p>
     *
     * @return the removed timestamp
     */
    public long getRemoved() {

        return m_removed;
    }

    /**
     * Returns the started timestamp.<p>
     *
     * @return the started timestamp
     */
    public long getStarted() {

        return m_started;
    }

    /**
     * @see org.opencms.publish.I_CmsPublishEventListener#onAbort(CmsUUID, org.opencms.publish.CmsPublishJobEnqueued)
     */
    public void onAbort(CmsUUID userId, CmsPublishJobEnqueued publishJob) {

        // track only events for the interesting publish job
        if (m_publishJob == publishJob.m_publishJob) {
            m_aborted = System.currentTimeMillis();
            m_jobsInQueue[3] = OpenCms.getPublishManager().getPublishQueue().size();
            m_jobsInHistory[3] = OpenCms.getPublishManager().getPublishHistory().size();
        }
    }

    /**
     * @see org.opencms.publish.I_CmsPublishEventListener#onEnqueue(org.opencms.publish.CmsPublishJobBase)
     */
    public void onEnqueue(CmsPublishJobBase publishJob) {

        if (m_publishJob != null) {
            // track only one job at the time
            return;
        }
        if (!publishJob.m_publishJob.getPublishList().getFileList().contains(m_resource)) {
            // track only the right publish job
            return;
        }
        // remember the job to track
        m_publishJob = publishJob.m_publishJob;
        // reset all events
        m_enqueued = System.currentTimeMillis();
        m_aborted = 0;
        m_finished = 0;
        m_removed = 0;
        m_started = 0;
        // count jobs in queue and history
        m_jobsInQueue[0] = OpenCms.getPublishManager().getPublishQueue().size();
        m_jobsInHistory[0] = OpenCms.getPublishManager().getPublishHistory().size();
    }

    /**
     * @see org.opencms.publish.I_CmsPublishEventListener#onFinish(org.opencms.publish.CmsPublishJobRunning)
     */
    public void onFinish(CmsPublishJobRunning publishJob) {

        // track only events for the interesting publish job
        if (m_publishJob == publishJob.m_publishJob) {
            m_finished = System.currentTimeMillis();
            // count jobs in queue and history
            m_jobsInQueue[2] = OpenCms.getPublishManager().getPublishQueue().size();
            m_jobsInHistory[2] = OpenCms.getPublishManager().getPublishHistory().size();
        }
    }

    /**
     * @see org.opencms.publish.I_CmsPublishEventListener#onRemove(org.opencms.publish.CmsPublishJobFinished)
     */
    public void onRemove(CmsPublishJobFinished publishJob) {

        // track only events for the interesting publish job
        if (m_publishJob == publishJob.m_publishJob) {
            m_removed = System.currentTimeMillis();
            // count jobs in queue and history
            m_jobsInQueue[4] = OpenCms.getPublishManager().getPublishQueue().size();
            m_jobsInHistory[4] = OpenCms.getPublishManager().getPublishHistory().size();
        }
    }

    /**
     * @see org.opencms.publish.I_CmsPublishEventListener#onStart(org.opencms.publish.CmsPublishJobEnqueued)
     */
    public void onStart(CmsPublishJobEnqueued publishJob) {

        // track only events for the interesting publish job
        if (m_publishJob == publishJob.m_publishJob) {
            m_started = System.currentTimeMillis();
            // count jobs in queue and history
            m_jobsInQueue[1] = OpenCms.getPublishManager().getPublishQueue().size();
            m_jobsInHistory[1] = OpenCms.getPublishManager().getPublishHistory().size();
        }
    }
}

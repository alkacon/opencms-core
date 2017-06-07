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

package org.opencms.ugc;

/**
 * A queue used to enforce a certain wait time between requests for sessions for the same form configuration.<p>
 */
public class CmsUgcSessionQueue {

    /** True if the session queue is enabled. */
    private boolean m_enabled;

    /** The wait time between multiple session requests for the same configuration. */
    private long m_interval;

    /** The maximum number of threads waiting in the queue for the same configuration. */
    private int m_maxLength;

    /** The time at which the next thread should be scheduled to run. */
    private long m_nextScheduleTime;

    /** The number of threads waiting on this queue. */
    private int m_waitCount;

    /**
     * Creates a new instance.<p>
     *
     * @param enabled true if the queue should be enabled
     * @param interval the wait time to use between multiple session requests
     * @param maxLength the maximum length of the queue
     */
    public CmsUgcSessionQueue(boolean enabled, long interval, int maxLength) {

        m_enabled = enabled;
        m_interval = interval;
        m_maxLength = maxLength;
        if (interval < 0) {
            throw new IllegalArgumentException("Queue wait time must not be < 0");
        }

        if (maxLength < 0) {
            throw new IllegalArgumentException("Queue maximum length must not be < 0");
        }
    }

    /**
     * Creates a session queue based on the given configuration.<p>
     *
     * @param config the configuration for which to create the session queue
     * @return the newly created session queue
     */
    public static CmsUgcSessionQueue createQueue(CmsUgcConfiguration config) {

        CmsUgcSessionQueue queue = new CmsUgcSessionQueue(
            config.needsQueue(),
            config.getQueueInterval().isPresent() ? config.getQueueInterval().get().longValue() : 0,
            config.getMaxQueueLength().isPresent() ? config.getMaxQueueLength().get().intValue() : Integer.MAX_VALUE);
        return queue;
    }

    /**
     * Updates the queue parameters from the configuration object.<p>
     *
     * @param config the form configuration
     */
    public synchronized void updateFromConfiguration(CmsUgcConfiguration config) {

        m_enabled = config.needsQueue();
        m_interval = config.getQueueInterval().isPresent() ? config.getQueueInterval().get().longValue() : 0;
        m_maxLength = config.getMaxQueueLength().isPresent()
        ? config.getMaxQueueLength().get().intValue()
        : Integer.MAX_VALUE;
    }

    /**
     * If there are currently any threads waiting on this queue, wait for the interval given on construction after the currenly last thread stops waiting.<p>
     *
     * @return false if the queue was too long to wait, true otherwise
     */
    public synchronized boolean waitForSlot() {

        if (!m_enabled) {
            return true;
        } else {
            long now = System.currentTimeMillis();
            long timeToWait = m_nextScheduleTime - now;
            if (timeToWait <= 0) {
                // This happens either if this method is called for the first time,
                // or if the wait interval for the last thread to enter the queue has
                // already fully elapsed
                m_nextScheduleTime = now + m_interval;
                return true;
            } else if (m_waitCount >= m_maxLength) {
                return false;
            } else {
                m_nextScheduleTime = m_nextScheduleTime + m_interval;
                m_waitCount += 1;
                try {
                    // use wait here instead of sleep because it releases the object monitor

                    // note that timeToWait can't be 0 here, because this case is handled in the first if() branch,
                    // so wait() always returns after the given period
                    wait(timeToWait);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                m_waitCount -= 1;
                return true;
            }
        }
    }
}
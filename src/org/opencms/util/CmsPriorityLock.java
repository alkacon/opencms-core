/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Lock which can be acquired with two different priorities.
 *
 * <p>The lock can be only be held by one thread, but can be locked reentrantly by the owner. All waiting threads who requested the lock with high priority will be granted the lock first if it becomes available, before any threads who requested the lock with low priority.
 */
public class CmsPriorityLock {

    /** The current thread holding the lock. */
    private Thread m_holder = null;

    /** How many times the current lock owner, if any, is holding the lock. */
    private int m_holdCount = 0;

    /** The underlying lock. */
    private ReentrantLock m_lock = new ReentrantLock(true);

    /** Condition object used to signal that high priority waiters can acquire the lock. */
    private Condition m_highCond = m_lock.newCondition();

    /** Condition object used to signal that low priority waiters can acquire the lock. */
    private Condition m_lowCond = m_lock.newCondition();

    /** Number of waiters who want to acquire the lock with high priority. */
    private int m_waitHigh;

    /** Number of waiters who want to acquire the lock with low priority. */
    private int m_waitLow;

    /**
     * Creates a new instance.
     */
    public CmsPriorityLock() {

    }

    /**
     * Acquire the lock, with given priority.
     *
     * @param highPriority true for high-priority acquisition, false for low-priority
     */
    public void lock(boolean highPriority) {

        m_lock.lock();
        try {
            Thread current = Thread.currentThread();
            if (current == m_holder) {
                m_holdCount++;
                return;
            }
            if (highPriority) {
                m_waitHigh += 1;
                while (m_holder != null) {
                    m_highCond.awaitUninterruptibly();
                }
                m_waitHigh -= 1;
                m_holder = current;
                m_holdCount = 1;
            } else {
                m_waitLow += 1;
                while ((m_holder != null) || (m_waitHigh > 0)) {
                    m_lowCond.awaitUninterruptibly();
                }
                m_waitLow -= 1;
                m_holder = current;
                m_holdCount = 1;
            }
        } finally {
            m_lock.unlock();
        }

    }

    /**
     * Releases the lock.
     *
     * @throws IllegalMonitorStateException if called by a thread not owning the lock
     */
    public void unlock() {

        m_lock.lock();
        try {
            Thread current = Thread.currentThread();
            if (current != m_holder) {
                throw new IllegalMonitorStateException();
            }
            m_holdCount -= 1;
            if (m_holdCount == 0) {
                m_holder = null;
                if (m_waitHigh > 0) {
                    m_highCond.signal();
                } else if (m_waitLow > 0) {
                    m_lowCond.signal();
                }
            }
        } finally {
            m_lock.unlock();
        }

    }
}
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

package org.opencms.monitor;

/**
 * Data structure for dealing with memory status information.<p>
 *
 * @since 6.0.0
 */
public class CmsMemoryStatus {

    /** The count used to calculate the average. */
    private int m_count;

    /** The current free memory, in megabytes. */
    private long m_freeMemory;

    /** The maximum available memory, in megabytes. */
    private long m_maxMemory;

    /** The amount of memory currently availble to the JVM, in megabytes. */
    private long m_totalMemory;

    /** The current memory usage, in percent. */
    private long m_usage;

    /** The amount of memory currently used, in megabytes. */
    private long m_usedMemory;

    /**
     * Initializes a new instance of the memory status with the current memory values.<p>
     */
    public CmsMemoryStatus() {

        update();
    }

    /**
     * Calculates the average memory consumption by updating the stored information with
     * the provided current information.<p>
     *
     * @param currentStatus the memory status to update the average with
     */
    public void calculateAverage(CmsMemoryStatus currentStatus) {

        int newCount = m_count + 1;
        m_maxMemory = ((m_count * m_maxMemory) + currentStatus.getMaxMemory()) / newCount;
        m_totalMemory = ((m_count * m_totalMemory) + currentStatus.getTotalMemory()) / newCount;
        m_usedMemory = ((m_count * m_usedMemory) + currentStatus.getUsedMemory()) / newCount;
        m_freeMemory = ((m_count * m_freeMemory) + currentStatus.getFreeMemory()) / newCount;
        m_usage = (m_usedMemory * 100) / m_maxMemory;
        m_count = newCount;
    }

    /**
     * Returns the count used to calculate the average.<p>
     *
     * @return the count used to calculate the average
     */
    public int getCount() {

        return m_count;
    }

    /**
     * Returns the current free memory, in megabytes.<p>
     *
     * @return the current free memory, in megabytes
     */
    public long getFreeMemory() {

        return m_freeMemory;
    }

    /**
     * Returns the maximum available memory, in megabytes.<p>
     *
     * @return the maximum available memory, in megabytes
     */
    public long getMaxMemory() {

        return m_maxMemory;
    }

    /**
     * Returns the amount of memory currently availble to the JVM, in megabytes.<p>
     *
     * @return the amount of memory currently availble to the JVM, in megabytes
     */
    public long getTotalMemory() {

        return m_totalMemory;
    }

    /**
     * Returns the current memory usage, in percent.<p>
     *
     * @return the current memory usage, in percent
     */
    public long getUsage() {

        return m_usage;
    }

    /**
     * Returns the amount of memory currently used, in megabytes.<p>
     *
     * @return the amount of memory currently used, in megabytes
     */
    public long getUsedMemory() {

        return m_usedMemory;
    }

    /**
     * Updates this memory status with the current memory information.<p>
     */
    public void update() {

        m_maxMemory = Runtime.getRuntime().maxMemory() / 1048576;
        m_totalMemory = Runtime.getRuntime().totalMemory() / 1048576;
        m_usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
        m_freeMemory = m_maxMemory - m_usedMemory;
        m_usage = (m_usedMemory * 100) / m_maxMemory;
    }
}

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

package org.opencms.util.benchmark;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages a set of benchmark timers.<p>
 *
 * Each benchmark timer is started with start(name) and stopped with stop(name). When stop() is called,
 * the time between the start and stop calls for that name, in milliseconds, are sent to the configured benchmark
 * receiver instance.
 */
public class CmsBenchmarkTable {

    /**
     * Handler for benchmark samples.
     */
    interface Receiver {

        /**
         * Processes a sample.
         *
         * @param sampleName the sample name
         * @param sampleTime the sample time
         */
        void receiveSample(String sampleName, long sampleTime);
    }

    /** The benchmark sample receiver. */
    private Receiver m_receiver;

    /** Records start times for each benchmark timer. */
    private ConcurrentHashMap<String, Long> m_startTimes = new ConcurrentHashMap<>();

    /**
     * Creates a new instance.
     *
     * @param receiver the benchmark receiver to use
     */
    public CmsBenchmarkTable(Receiver receiver) {

        m_receiver = receiver;
    }

    /**
     * Starts the timer with the given name.<p>
     *
     * The name is just an arbitrary string.
     *
     * @param name the name of the timer
     */
    public void start(String name) {

        if (m_startTimes.containsKey(name)) {
            throw new IllegalStateException("Can't start timer for given key twice: " + name);
        }
        m_startTimes.put(name, Long.valueOf(System.currentTimeMillis()));
    }

    /**
     * Stops the timer with the given name, and sends the value of the timer to the benchmark receiver.
     *
     * @param name the name of the timer
     */
    public void stop(String name) {

        if (!m_startTimes.containsKey(name)) {
            throw new IllegalStateException("Can't stop a timer that wasn't started: " + name);
        }
        long duration = System.currentTimeMillis() - m_startTimes.get(name).longValue();
        m_receiver.receiveSample(name, duration);
        m_startTimes.remove(name);
    }

}

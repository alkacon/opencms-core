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

package org.opencms.util;

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;

import com.google.common.collect.ComparisonChain;

/**
 * Helper class for building diagnostics code that tracks potentially long running operations.<p>
 * Is used with the try-with-resources syntax.  (I_CmsCloseable c = CmsTaskWatcher.get().openTask(description)) { ... }
 */
public class CmsTaskWatcher {

    /**
     * Entry for a single task.
     */
    static class Entry {

        /** Label. */
        private String m_label;
        /** Start time. */
        private long m_startTime;

        /** Id. */
        private String m_id;

        /** Thread name. */
        private String m_threadName;

        /**
         * Creates a new entry.
         *
         * @param threadName the thread name
         * @param label the label
         * @param startTime the start time
         */
        public Entry(String threadName, String label, long startTime) {

            m_id = RandomStringUtils.randomAlphanumeric(10);
            m_threadName = threadName;
            m_label = label;
            m_startTime = startTime;
        }

        /**
         * Gets the age of the entry in milliseconds.
         *
         * @return the age in milliseconds
         */
        public long getAge() {

            return System.currentTimeMillis() - m_startTime;
        }

        /**
         * Gets the id.
         *
         * @return the id
         */
        public String getId() {

            return m_id;
        }

        /**
         * Gets the label.
         *
         * @return the label
         */
        public String getLabel() {

            return m_label;
        }

        /**
         * Gets the start tiem
         *
         * @return the start time
         */
        public long getStartTime() {

            return m_startTime;
        }

        /**
         * Gets the thread name.
         *
         * @return the thread name
         */
        public String getThreadName() {

            return m_threadName;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return "" + (System.currentTimeMillis() - m_startTime) + " " + m_threadName + " / " + m_label;
        }

    }

    /** Task watcher log channel. */
    private static final Log TASKWATCHER = CmsLog.getLog("taskwatcher");

    /** Singleton instance. */
    private static CmsTaskWatcher m_instance = new CmsTaskWatcher();

    /**
     * The map of task entries.
     */
    private ConcurrentHashMap<String, Entry> m_map = new ConcurrentHashMap<>();

    /**
     * Gets the singleton instance.
     * @return the singleton instance
     */
    public static CmsTaskWatcher get() {

        return m_instance;
    }

    /**
     * Initializes the logging job.
     */
    public static void initialize() {

        OpenCms.getExecutor().scheduleWithFixedDelay(() -> {
            try {
                String report = get().getReport(5000);
                if (report.length() > 0) {
                    TASKWATCHER.error("Report:\n");
                    TASKWATCHER.error(report);
                }
            } catch (Exception e) {
                // ignore
            }

        }, 5000, 5000, TimeUnit.MILLISECONDS);
    }

    /**
     * Gets the map of entries.
     *
     * @return the map of entries.
     */
    public Map<String, Entry> getMap() {

        return Collections.unmodifiableMap(m_map);
    }

    /**
     * Gets a report as a string.
     *
     * @param minAge minimum age of entries to show in the report
     *
     * @return the report as a string
     */
    public String getReport(long minAge) {

        List<Entry> entries = new ArrayList<>(m_map.values());
        StringBuilder builder = new StringBuilder();
        Collections.sort(
            entries,
            (
                a,
                b) -> ComparisonChain.start().compare(a.getStartTime(), b.getStartTime()).compare(
                    a.getThreadName(),
                    b.getThreadName()).compare(a.getLabel(), b.getLabel()).result());
        for (Entry entry : entries) {
            if (entry.getAge() > minAge) {
                builder.append(entry.toString());
                builder.append("\n");
            }
        }
        return builder.toString();

    }

    /**
     * Adds a new task and returns a Closeable (to be used in a try-with-resources block) which removes the task entry again when closed.
     *
     * @param label the label for the task entry
     * @return the closeable
     */
    public I_CmsCloseable openTask(String label) {

        Entry entry = new Entry(Thread.currentThread().getName(), label, System.currentTimeMillis());
        final String id = entry.getId();
        m_map.put(id, entry);
        return () -> {
            m_map.remove(id);
        };
    }

}

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

package org.opencms.main;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;

/**
 * Class used to collect log messages and later write them to the actual log but with duplicates removed.
 */
public class CmsDuplicateRemovingLogBuffer {

    /**
     * Entry to write to the log.
     */
    public static class Entry {

        /** The log channel. */
        private String m_channel;

        /** The log level. */
        private String m_level;

        /** The log message. */
        private String m_message;

        /**
         * Creates a new entry.
         *
         * @param channel the log channel
         * @param level the log level
         * @param message the log message
         */
        public Entry(String channel, String level, String message) {

            super();
            m_channel = channel;
            m_level = level;
            m_message = message;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {

            return EqualsBuilder.reflectionEquals(this, obj);
        }

        /**
         * Gets the log channel.
         *
         * @return the log channel
         */
        public String getChannel() {

            return m_channel;
        }

        /**
         * Gets the log level.
         *
         * @return the log level
         */
        public String getLevel() {

            return m_level;
        }

        /**
         * Gets the log message.
         *
         * @return the log message
         */
        public String getMessage() {

            return m_message;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            return HashCodeBuilder.reflectionHashCode(this);
        }

    }

    /** The set of log entries collected so far. */
    private Set<Entry> m_entries = new LinkedHashSet<>();

    /**
     * Adds a new log entry.
     *
     * @param channel the log channel
     * @param level the log level
     * @param message the log message
     */
    public void add(String channel, String level, String message) {

        m_entries.add(new Entry(channel, level, message));
    }

    /**
     * Dumps all collected log entries to their respective logs, with duplicates removed.
     *
     * <p>Also clears the set of collected log entries.
     */
    public void flush() {

        for (Entry entry : m_entries) {
            String level = entry.getLevel();
            Log log = CmsLog.getLog(entry.getChannel());
            if ("warn".equalsIgnoreCase(level)) {
                log.warn(entry.getMessage());
            } else if ("error".equalsIgnoreCase(level)) {
                log.error(entry.getMessage());
            } else if ("info".equalsIgnoreCase(level)) {
                log.info(entry.getMessage());
            } else if ("debug".equalsIgnoreCase(level)) {
                log.debug(entry.getMessage());
            } else {
                log.info(entry.getMessage());
            }
        }
        m_entries.clear();
    }
}

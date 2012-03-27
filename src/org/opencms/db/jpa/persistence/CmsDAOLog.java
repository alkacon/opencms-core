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

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * This data access object represents a log entry inside the table "cms_log".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_LOG")
@IdClass(org.opencms.db.jpa.persistence.CmsDAOLog.CmsDAOLogPK.class)
public class CmsDAOLog {

    /**
     * This class implements the primary key for a log entry in the table "cms_log".<p>
     */
    public static class CmsDAOLogPK implements Serializable {

        /**
         * A tokenizer.<p>
         */
        private static class Tokenizer {

            /** The last index. */
            private int m_last;

            /** The String to tokenize. */
            private final String m_str;

            /**
             * The constructor for this tokenizer.<p>
             * 
             * @param str the String to tokenize.<p>
             */
            public Tokenizer(String str) {

                m_str = str;
            }

            /**
             * Returns the next token.<p>
             * 
             * @return the next token
             */
            public String nextToken() {

                int next = m_str.indexOf("::", m_last);
                String part;
                if (next == -1) {
                    part = m_str.substring(m_last);
                    m_last = m_str.length();
                } else {
                    part = m_str.substring(m_last, next);
                    m_last = next + 2;
                }
                return part;
            }
        }

        /** The serial Version UID. */
        private static final long serialVersionUID = -2970988269297688935L;

        /** The log date. */
        private long m_logDate;

        /** The log type. */
        private int m_logType;

        /** The user id. */
        private String m_userId;

        /**
         * The default constructor.<p>
         */
        public CmsDAOLogPK() {

            // noop
        }

        /**
         * A public constructor to generate a primary key from a given String.<p>
         * 
         * @param str the String to generate the id from
         */
        public CmsDAOLogPK(String str) {

            fromString(str);
        }

        static {
            // register persistent class in JVM
            try {
                Class.forName("org.opencms.db.jpa.persistence.CmsDAOLog");
            } catch (Exception e) {
                // noop
            }
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {

            if (this == obj) {
                return true;
            }
            if ((obj == null) || (obj.getClass() != getClass())) {
                return false;
            }

            CmsDAOLogPK other = (CmsDAOLogPK)obj;
            return (m_logDate == other.m_logDate)
                && (m_logType == other.m_logType)
                && (((m_userId == null) && (other.m_userId == null)) || ((m_userId != null) && m_userId.equals(other.m_userId)));
        }

        /**
         * Returns the logDate.<p>
         *
         * @return the logDate
         */
        public long getLogDate() {

            return m_logDate;
        }

        /**
         * Returns the logType.<p>
         *
         * @return the logType
         */
        public int getLogType() {

            return m_logType;
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
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            int rs = 17;
            rs = rs * 37 + (int)(m_logDate ^ (m_logDate >>> 32));
            rs = rs * 37 + m_logType;
            rs = rs * 37 + ((m_userId == null) ? 0 : m_userId.hashCode());
            return rs;
        }

        /**
         * Sets the logDate.<p>
         *
         * @param logDate the logDate to set
         */
        public void setLogDate(long logDate) {

            m_logDate = logDate;
        }

        /**
         * Sets the logType.<p>
         *
         * @param logType the logType to set
         */
        public void setLogType(int logType) {

            m_logType = logType;
        }

        /**
         * Sets the userId.<p>
         *
         * @param userId the userId to set
         */
        public void setUserId(String userId) {

            m_userId = userId;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return String.valueOf(m_logDate) + "::" + String.valueOf(m_logType) + "::" + m_userId;
        }

        /**
         * Parses the publish tag from and the resource id from a given String.<p>
         *  
         * @param str the String to parse
         */
        private void fromString(String str) {

            Tokenizer toke = new Tokenizer(str);
            str = toke.nextToken();
            m_logDate = Long.parseLong(str);
            str = toke.nextToken();
            m_logType = Integer.parseInt(str);
            str = toke.nextToken();
            if ("null".equals(str)) {
                m_userId = null;
            } else {
                m_userId = str;
            }
        }
    }

    /** The log data. */
    @Basic
    @Column(name = "LOG_DATA", length = 1024)
    private String m_logData;

    /** The log date. */
    @Id
    @Column(name = "LOG_DATE")
    private long m_logDate;

    /** The log type. */
    @Id
    @Column(name = "LOG_TYPE")
    private int m_logType;

    /** The structure id. */
    @Basic
    @Column(name = "STRUCTURE_ID", length = 36)
    private String m_structureId;

    /** The user id. */
    @Id
    @Column(name = "USER_ID", length = 36)
    private String m_userId;

    /**
     * The default constructor.<p>
     */
    public CmsDAOLog() {

        // noop
    }

    /**
     * A public constructor for generating a new log object with an unique id.<p>
     * 
     * @param logDate the log date
     * @param logType the log type
     * @param userId the user id
     */
    public CmsDAOLog(long logDate, int logType, String userId) {

        m_logDate = logDate;
        m_logType = logType;
        m_userId = userId;
    }

    /**
     * Returns the logData.<p>
     *
     * @return the logData
     */
    public String getLogData() {

        return m_logData;
    }

    /**
     * Returns the logDate.<p>
     *
     * @return the logDate
     */
    public long getLogDate() {

        return m_logDate;
    }

    /**
     * Returns the logType.<p>
     *
     * @return the logType
     */
    public int getLogType() {

        return m_logType;
    }

    /**
     * Returns the structureId.<p>
     *
     * @return the structureId
     */
    public String getStructureId() {

        return m_structureId;
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
     * Sets the logData.<p>
     *
     * @param logData the logData to set
     */
    public void setLogData(String logData) {

        m_logData = logData;
    }

    /**
     * Sets the logDate.<p>
     *
     * @param logDate the logDate to set
     */
    public void setLogDate(long logDate) {

        m_logDate = logDate;
    }

    /**
     * Sets the logType.<p>
     *
     * @param logType the logType to set
     */
    public void setLogType(int logType) {

        m_logType = logType;
    }

    /**
     * Sets the structureId.<p>
     *
     * @param structureId the structureId to set
     */
    public void setStructureId(String structureId) {

        m_structureId = structureId;
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
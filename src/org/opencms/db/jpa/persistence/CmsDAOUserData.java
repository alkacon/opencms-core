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
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * This data access object represents a user data entry inside the table "cms_userdata".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_USERDATA")
@IdClass(CmsDAOUserData.CmsDAOUserDataPK.class)
public class CmsDAOUserData {

    /**
     * This class implements the primary key for a user data entry in the table "cms_userdata".<p>
     */
    public static class CmsDAOUserDataPK implements Serializable {

        /** The serial Version UID. */
        private static final long serialVersionUID = 6035468570220023953L;

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

        /** The data key. */
        public String m_dataKey;

        /** The user id. */
        public String m_userId;

        /**
         * The default constructor.<p>
         */
        public CmsDAOUserDataPK() {

            // noop
        }

        /**
         * A public constructor to generate a primary key from a given String.<p>
         * 
         * @param str the String to generate the id from
         */
        public CmsDAOUserDataPK(String str) {

            fromString(str);
        }

        static {
            // register persistent class in JVM
            try {
                Class.forName("org.opencms.db.jpa.persistence.CmsDAOUserData");
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

            CmsDAOUserDataPK other = (CmsDAOUserDataPK)obj;
            return (((m_dataKey == null) && (other.m_dataKey == null)) || ((m_dataKey != null) && m_dataKey.equals(other.m_dataKey)))
                && (((m_userId == null) && (other.m_userId == null)) || ((m_userId != null) && m_userId.equals(other.m_userId)));
        }

        /**
         * Returns the dataKey.<p>
         *
         * @return the dataKey
         */
        public String getDataKey() {

            return m_dataKey;
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
            rs = rs * 37 + ((m_dataKey == null) ? 0 : m_dataKey.hashCode());
            rs = rs * 37 + ((m_userId == null) ? 0 : m_userId.hashCode());
            return rs;
        }

        /**
         * Sets the dataKey.<p>
         *
         * @param dataKey the dataKey to set
         */
        public void setDataKey(String dataKey) {

            m_dataKey = dataKey;
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

            return m_dataKey + "::" + m_userId;
        }

        /**
         * Parses the publish tag from and the resource id from a given String.<p>
         *  
         * @param str the String to parse
         */
        private void fromString(String str) {

            Tokenizer toke = new Tokenizer(str);
            str = toke.nextToken();
            if ("null".equals(str)) {
                m_dataKey = null;
            } else {
                m_dataKey = str;
            }
            str = toke.nextToken();
            if ("null".equals(str)) {
                m_userId = null;
            } else {
                m_userId = str;
            }
        }
    }

    /** The data key. */
    @Id
    @Column(name = "DATA_KEY")
    private String m_dataKey;

    /** The data type. */
    @Basic
    @Column(name = "DATA_TYPE", nullable = false, length = 128)
    private String m_dataType;

    /** The data value. */
    @Basic
    @Lob
    @Column(name = "DATA_VALUE")
    private byte[] m_dataValue;

    /** The user id. */
    @Id
    @Column(name = "USER_ID", length = 36)
    private String m_userId;

    /**
     * The default constructor.<p>
     */
    public CmsDAOUserData() {

        // noop
    }

    /**
     * A public constructor for generating a new user data object with an unique id.<p>
     * 
     * @param dataKey the data key
     * @param userId the user id
     */
    public CmsDAOUserData(String dataKey, String userId) {

        m_dataKey = dataKey;
        m_userId = userId;
    }

    /**
     * Returns the dataKey.<p>
     *
     * @return the dataKey
     */
    public String getDataKey() {

        return m_dataKey;
    }

    /**
     * Returns the dataType.<p>
     *
     * @return the dataType
     */
    public String getDataType() {

        return m_dataType;
    }

    /**
     * Returns the dataValue.<p>
     *
     * @return the dataValue
     */
    public byte[] getDataValue() {

        return m_dataValue;
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
     * Sets the dataKey.<p>
     *
     * @param dataKey the dataKey to set
     */
    public void setDataKey(String dataKey) {

        m_dataKey = dataKey;
    }

    /**
     * Sets the dataType.<p>
     *
     * @param dataType the dataType to set
     */
    public void setDataType(String dataType) {

        m_dataType = dataType;
    }

    /**
     * Sets the dataValue.<p>
     *
     * @param dataValue the dataValue to set
     */
    public void setDataValue(byte[] dataValue) {

        m_dataValue = dataValue;
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
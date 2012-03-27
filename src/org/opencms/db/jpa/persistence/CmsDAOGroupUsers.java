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
 * This data access object represents a group users entry inside the table "cms_groupusers".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_GROUPUSERS")
@IdClass(CmsDAOGroupUsers.CmsDAOGroupUsersPK.class)
public class CmsDAOGroupUsers {

    /**
     * This class implements the primary key for a opencms group users entry in the table "cms_groupusers".<p>
     */
    public static class CmsDAOGroupUsersPK implements Serializable {

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
        private static final long serialVersionUID = 244258933522671742L;

        /** The group id. */
        public String m_groupId;

        /** The user id. */
        public String m_userId;

        /**
         * The default constructor.<p>
         */
        public CmsDAOGroupUsersPK() {

            // noop
        }

        /**
         * A public constructor to generate a primary key from a given String.<p>
         * 
         * @param str the String to generate the id from
         */
        public CmsDAOGroupUsersPK(String str) {

            fromString(str);
        }

        static {
            // register persistent class in JVM
            try {
                Class.forName("org.opencms.db.jpa.persistence.CmsDAOGroupUsers");
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

            CmsDAOGroupUsersPK other = (CmsDAOGroupUsersPK)obj;
            return (((m_groupId == null) && (other.m_groupId == null)) || ((m_groupId != null) && m_groupId.equals(other.m_groupId)))
                && (((m_userId == null) && (other.m_userId == null)) || ((m_userId != null) && m_userId.equals(other.m_userId)));
        }

        /**
         * Returns the groupId.<p>
         *
         * @return the groupId
         */
        public String getGroupId() {

            return m_groupId;
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
            rs = rs * 37 + ((m_groupId == null) ? 0 : m_groupId.hashCode());
            rs = rs * 37 + ((m_userId == null) ? 0 : m_userId.hashCode());
            return rs;
        }

        /**
         * Sets the groupId.<p>
         *
         * @param groupId the groupId to set
         */
        public void setGroupId(String groupId) {

            m_groupId = groupId;
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

            return m_groupId + "::" + m_userId;
        }

        /**
         * Parses the group id and the user id from a given String.<p>
         *  
         * @param str the String to parse
         */
        private void fromString(String str) {

            Tokenizer toke = new Tokenizer(str);
            str = toke.nextToken();
            if ("null".equals(str)) {
                m_groupId = null;
            } else {
                m_groupId = str;
            }
            str = toke.nextToken();
            if ("null".equals(str)) {
                m_userId = null;
            } else {
                m_userId = str;
            }
        }
    }

    /** The group id. */
    @Id
    @Column(name = "GROUP_ID", length = 36)
    private String m_groupId;

    /** The group users flag. */
    @Basic
    @Column(name = "GROUPUSER_FLAGS")
    private int m_groupUserFlags;

    /** The user id. */
    @Id
    @Column(name = "USER_ID", length = 36)
    private String m_userId;

    /**
     * The public constructor.<p>
     */
    public CmsDAOGroupUsers() {

        // noop
    }

    /**
     * A public constructor to create a group users entry.<p>
     * 
     * @param groupId the group id
     * @param userId the user id
     */
    public CmsDAOGroupUsers(String groupId, String userId) {

        m_groupId = groupId;
        m_userId = userId;
    }

    /**
     * Returns the group id.<p>
     * 
     * @return the group id
     */
    public String getGroupId() {

        return m_groupId;
    }

    /**
     * Returns the group users flag.<p>
     * 
     * @return the group users flag
     */
    public int getGroupUserFlags() {

        return m_groupUserFlags;
    }

    /**
     * Returns the user id.<p>
     * 
     * @return the user id
     */
    public String getUserId() {

        return m_userId;
    }

    /**
     * Sets the group id.<p>
     * 
     * @param groupId the group id to set
     */
    public void setGroupId(String groupId) {

        m_groupId = groupId;
    }

    /**
     * Sets the flag.<p>
     * 
     * @param groupuserFlags the flag to set
     */
    public void setGroupUserFlags(int groupuserFlags) {

        m_groupUserFlags = groupuserFlags;
    }

    /**
     * Sets the user id.<p>
     * 
     * @param userId the user id to set
     */
    public void setUserId(String userId) {

        m_userId = userId;
    }
}
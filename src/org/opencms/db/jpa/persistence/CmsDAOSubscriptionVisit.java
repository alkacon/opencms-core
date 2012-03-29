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
 * This data access object represents a subscription visit entry inside the table "cms_subscription_visit".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_SUBSCRIPTION_VISIT")
@IdClass(org.opencms.db.jpa.persistence.CmsDAOSubscriptionVisit.CmsDAOSubscriptionVisitPK.class)
public class CmsDAOSubscriptionVisit {

    /**
     * This class implements the primary key for a subscription visit entry in the table "cms_subscription_visit".<p>
     */
    public static class CmsDAOSubscriptionVisitPK implements Serializable {

        /** The serial Version UID. */
        private static final long serialVersionUID = -6575619743632823038L;

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

        /** The user id. */
        public String m_userId;

        /** The visit date. */
        public long m_visitDate;

        /**
         * The default constructor.<p>
         */
        public CmsDAOSubscriptionVisitPK() {

            // noop
        }

        /**
         * A public constructor to generate a primary key from a given String.<p>
         * 
         * @param str the String to generate the id from
         */
        public CmsDAOSubscriptionVisitPK(String str) {

            fromString(str);
        }

        static {
            // register persistent class in JVM
            try {
                Class.forName("org.opencms.db.jpa.persistence.CmsDAOSubscriptionVisit");
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

            CmsDAOSubscriptionVisitPK other = (CmsDAOSubscriptionVisitPK)obj;
            return (((m_userId == null) && (other.m_userId == null)) || ((m_userId != null) && m_userId.equals(other.m_userId)))
                && (m_visitDate == other.m_visitDate);
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            int rs = 17;
            rs = rs * 37 + ((m_userId == null) ? 0 : m_userId.hashCode());
            rs = rs * 37 + (int)(m_visitDate ^ (m_visitDate >>> 32));
            return rs;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return m_userId + "::" + String.valueOf(m_visitDate);
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
                m_userId = null;
            } else {
                m_userId = str;
            }
            str = toke.nextToken();
            m_visitDate = Long.parseLong(str);
        }
    }

    /** The structure id. */
    @Basic
    @Column(name = "STRUCTURE_ID", length = 36)
    private String m_structureId;

    /** The user id. */
    @Id
    @Column(name = "USER_ID", length = 36)
    private String m_userId;

    /** The visit date. */
    @Id
    @Column(name = "VISIT_DATE")
    private long m_visitDate;

    /**
     * The default constructor.<p>
     */
    public CmsDAOSubscriptionVisit() {

        // noop
    }

    /**
     * A public constructor for generating a new subscription visit object with an unique id.<p>
     * 
     * @param userId the user id
     * @param visitDate the visit date
     */
    public CmsDAOSubscriptionVisit(String userId, long visitDate) {

        m_userId = userId;
        m_visitDate = visitDate;
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
     * Returns the visitDate.<p>
     *
     * @return the visitDate
     */
    public long getVisitDate() {

        return m_visitDate;
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

    /**
     * Sets the visitDate.<p>
     *
     * @param visitDate the visitDate to set
     */
    public void setVisitDate(long visitDate) {

        m_visitDate = visitDate;
    }

}
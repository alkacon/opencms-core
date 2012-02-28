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
 * This data access object represents a subscription entry inside the table "cms_subscription".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_SUBSCRIPTION")
@IdClass(org.opencms.db.jpa.persistence.CmsDAOSubscription.CmsDAOSubscriptionPK.class)
public class CmsDAOSubscription {

    /**
     * This class implements the primary key for a subscription entry in the table "cms_subscription".<p>
     */
    public static class CmsDAOSubscriptionPK implements Serializable {

        /** The serial Version UID. */
        private static final long serialVersionUID = -9211500430127470632L;

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

        /** The principal id. */
        public String m_principalId;

        /** The structure id. */
        public String m_structureId;

        /**
         * The default constructor.<p>
         */
        public CmsDAOSubscriptionPK() {

            // noop
        }

        /**
         * A public constructor to generate a primary key from a given String.<p>
         * 
         * @param str the String to generate the id from
         */
        public CmsDAOSubscriptionPK(String str) {

            fromString(str);
        }

        static {
            // register persistent class in JVM
            try {
                Class.forName("org.opencms.db.jpa.persistence.CmsDAOSubscription");
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

            CmsDAOSubscriptionPK other = (CmsDAOSubscriptionPK)obj;
            return (((m_principalId == null) && (other.m_principalId == null)) || ((m_principalId != null) && m_principalId.equals(other.m_principalId)))
                && (((m_structureId == null) && (other.m_structureId == null)) || ((m_structureId != null) && m_structureId.equals(other.m_structureId)));
        }

        /**
         * Returns the principalId.<p>
         *
         * @return the principalId
         */
        public String getPrincipalId() {

            return m_principalId;
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
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            int rs = 17;
            rs = rs * 37 + ((m_principalId == null) ? 0 : m_principalId.hashCode());
            rs = rs * 37 + ((m_structureId == null) ? 0 : m_structureId.hashCode());
            return rs;
        }

        /**
         * Sets the principalId.<p>
         *
         * @param principalId the principalId to set
         */
        public void setPrincipalId(String principalId) {

            m_principalId = principalId;
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
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return m_principalId + "::" + m_structureId;
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
                m_principalId = null;
            } else {
                m_principalId = str;
            }
            str = toke.nextToken();
            if ("null".equals(str)) {
                m_structureId = null;
            } else {
                m_structureId = str;
            }
        }
    }

    /** The date deleted. */
    @Basic
    @Column(name = "DATE_DELETED")
    private long m_dateDeleted;

    /** The principal id. */
    @Id
    @Column(name = "PRINCIPAL_ID", length = 36)
    private String m_principalId;

    /** The structure id. */
    @Id
    @Column(name = "STRUCTURE_ID", length = 36)
    private String m_structureId;

    /**
     * The default constructor.<p>
     */
    public CmsDAOSubscription() {

        // noop
    }

    /**
     * A public constructor for generating a new subscription object with an unique id.<p>
     * 
     * @param principalId the principal id
     * @param structureId the structure id
     */
    public CmsDAOSubscription(String principalId, String structureId) {

        m_principalId = principalId;
        m_structureId = structureId;
    }

    /**
     * Returns the dateDeleted.<p>
     *
     * @return the dateDeleted
     */
    public long getDateDeleted() {

        return m_dateDeleted;
    }

    /**
     * Returns the principalId.<p>
     *
     * @return the principalId
     */
    public String getPrincipalId() {

        return m_principalId;
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
     * Sets the dateDeleted.<p>
     *
     * @param dateDeleted the dateDeleted to set
     */
    public void setDateDeleted(long dateDeleted) {

        m_dateDeleted = dateDeleted;
    }

    /**
     * Sets the principalId.<p>
     *
     * @param principalId the principalId to set
     */
    public void setPrincipalId(String principalId) {

        m_principalId = principalId;
    }

    /**
     * Sets the structureId.<p>
     *
     * @param structureId the structureId to set
     */
    public void setStructureId(String structureId) {

        m_structureId = structureId;
    }

}
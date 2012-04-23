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
 * This data access object represents a access control entry inside the table "cms_online_accesscontrol".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_ONLINE_ACCESSCONTROL")
@IdClass(CmsDAOOnlineAccessControl.CmsDAOOnlineAccessControlPK.class)
public class CmsDAOOnlineAccessControl implements I_CmsDAOAccessControl {

    /**
     * This class implements the primary key for a access entry in the table "cms_online_accesscontrol".<p>
     */
    public static class CmsDAOOnlineAccessControlPK implements Serializable {

        /** The serial Version UID. */
        private static final long serialVersionUID = 1790569689289360171L;

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

        /** The resource id. */
        public String m_resourceId;

        /**
         * The default constructor.<p>
         */
        public CmsDAOOnlineAccessControlPK() {

            // noop
        }

        /**
         * A public constructor to generate a primary key from a given String.<p>
         * 
         * @param str the String to generate the id from
         */
        public CmsDAOOnlineAccessControlPK(String str) {

            fromString(str);
        }

        static {
            // register persistent class in JVM
            try {
                Class.forName("org.opencms.db.jpa.persistence.CmsDAOOnlineAccessControl");
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

            CmsDAOOnlineAccessControlPK other = (CmsDAOOnlineAccessControlPK)obj;
            return (((m_principalId == null) && (other.m_principalId == null)) || ((m_principalId != null) && m_principalId.equals(other.m_principalId)))
                && (((m_resourceId == null) && (other.m_resourceId == null)) || ((m_resourceId != null) && m_resourceId.equals(other.m_resourceId)));
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
         * Returns the resourceId.<p>
         *
         * @return the resourceId
         */
        public String getResourceId() {

            return m_resourceId;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            int rs = 17;
            rs = rs * 37 + ((m_principalId == null) ? 0 : m_principalId.hashCode());
            rs = rs * 37 + ((m_resourceId == null) ? 0 : m_resourceId.hashCode());
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
         * Sets the resourceId.<p>
         *
         * @param resourceId the resourceId to set
         */
        public void setResourceId(String resourceId) {

            m_resourceId = resourceId;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return m_principalId + "::" + m_resourceId;
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
                m_resourceId = null;
            } else {
                m_resourceId = str;
            }
        }
    }

    /** The access allowed. */
    @Basic
    @Column(name = "ACCESS_ALLOWED")
    private int m_accessAllowed;

    /** The access denied. */
    @Basic
    @Column(name = "ACCESS_DENIED")
    private int m_accessDenied;

    /** The access flag. */
    @Basic
    @Column(name = "ACCESS_FLAGS")
    private int m_accessFlags;

    /** The principal id. */
    @Id
    @Column(name = "PRINCIPAL_ID", length = 36)
    private String m_principalId;

    /** The resource id. */
    @Id
    @Column(name = "RESOURCE_ID", length = 36)
    private String m_resourceId;

    /**
     * The default constructor.<p>
     */
    public CmsDAOOnlineAccessControl() {

        // noop
    }

    /**
     * A public constructor for generating a new contents object with an unique id.<p>
     * 
     * @param principalId the principal id
     * @param resourceId the resource id
     */
    public CmsDAOOnlineAccessControl(String principalId, String resourceId) {

        m_principalId = principalId;
        m_resourceId = resourceId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOAccessControl#getAccessAllowed()
     */
    public int getAccessAllowed() {

        return m_accessAllowed;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOAccessControl#setAccessAllowed(int)
     */
    public void setAccessAllowed(int accessAllowed) {

        m_accessAllowed = accessAllowed;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOAccessControl#getAccessDenied()
     */
    public int getAccessDenied() {

        return m_accessDenied;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOAccessControl#setAccessDenied(int)
     */
    public void setAccessDenied(int accessDenied) {

        m_accessDenied = accessDenied;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOAccessControl#getAccessFlags()
     */
    public int getAccessFlags() {

        return m_accessFlags;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOAccessControl#setAccessFlags(int)
     */
    public void setAccessFlags(int accessFlags) {

        m_accessFlags = accessFlags;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOAccessControl#getPrincipalId()
     */
    public String getPrincipalId() {

        return m_principalId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOAccessControl#setPrincipalId(java.lang.String)
     */
    public void setPrincipalId(String principalId) {

        m_principalId = principalId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOAccessControl#getResourceId()
     */
    public String getResourceId() {

        return m_resourceId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOAccessControl#setResourceId(java.lang.String)
     */
    public void setResourceId(String resourceId) {

        m_resourceId = resourceId;
    }

}
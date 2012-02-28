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
 * This data access object represents a historical structure entry 
 * inside the table "cms_history_structure".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_HISTORY_STRUCTURE")
@IdClass(CmsDAOHistoryStructure.CmsDAOHistoryStructurePK.class)
public class CmsDAOHistoryStructure {

    /**
     * This class implements the primary key for a historical structure entry in the table "cms_history_structure".<p>
     */
    public static class CmsDAOHistoryStructurePK implements Serializable {

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
        private static final long serialVersionUID = -1634835068964793024L;

        /** The publish tag. */
        public int m_publishTag;

        /** The structure id. */
        public String m_structureId;

        /** The version. */
        public int m_version;

        /**
         * The default constructor.<p>
         */
        public CmsDAOHistoryStructurePK() {

            // noop
        }

        /**
         * A public constructor to generate a primary key from a given String.<p>
         * 
         * @param str the String to generate the id from
         */
        public CmsDAOHistoryStructurePK(String str) {

            fromString(str);
        }

        static {
            // register persistent class in JVM
            try {
                Class.forName("org.opencms.db.jpa.persistence.CmsDAOHistoryStructure");
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

            CmsDAOHistoryStructurePK other = (CmsDAOHistoryStructurePK)obj;
            return (m_publishTag == other.m_publishTag)
                && (((m_structureId == null) && (other.m_structureId == null)) || ((m_structureId != null) && m_structureId.equals(other.m_structureId)))
                && (m_version == other.m_version);
        }

        /**
         * Returns the publishTag.<p>
         *
         * @return the publishTag
         */
        public int getPublishTag() {

            return m_publishTag;
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
         * Returns the version.<p>
         *
         * @return the version
         */
        public int getVersion() {

            return m_version;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            int rs = 17;
            rs = rs * 37 + m_publishTag;
            rs = rs * 37 + ((m_structureId == null) ? 0 : m_structureId.hashCode());
            rs = rs * 37 + m_version;
            return rs;
        }

        /**
         * Sets the publishTag.<p>
         *
         * @param publishTag the publishTag to set
         */
        public void setPublishTag(int publishTag) {

            m_publishTag = publishTag;
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
         * Sets the version.<p>
         *
         * @param version the version to set
         */
        public void setVersion(int version) {

            m_version = version;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return String.valueOf(m_publishTag) + "::" + m_structureId + "::" + String.valueOf(m_version);
        }

        /**
         * Parses the publish tag from and the resource id from a given String.<p>
         *  
         * @param str the String to parse
         */
        private void fromString(String str) {

            Tokenizer toke = new Tokenizer(str);
            str = toke.nextToken();
            m_publishTag = Integer.parseInt(str);
            str = toke.nextToken();
            if ("null".equals(str)) {
                m_structureId = null;
            } else {
                m_structureId = str;
            }
            str = toke.nextToken();
            m_version = Integer.parseInt(str);
        }
    }

    /** The date expired. */
    @Basic
    @Column(name = "DATE_EXPIRED")
    private long m_dateExpired;

    /** The date released. */
    @Basic
    @Column(name = "DATE_RELEASED")
    private long m_dateReleased;

    /** The parent id. */
    @Basic
    @Column(name = "PARENT_ID", nullable = false, length = 36)
    private String m_parentId;

    /** The publish tag. */
    @Id
    @Column(name = "PUBLISH_TAG")
    private int m_publishTag;

    /** The resource id. */
    @Basic
    @Column(name = "RESOURCE_ID", nullable = false, length = 36)
    private String m_resourceId;

    /** The resource path. */
    @Basic
    @Column(name = "RESOURCE_PATH", length = 1024)
    private String m_resourcePath;

    /** The structure id. */
    @Id
    @Column(name = "STRUCTURE_ID", length = 36)
    private String m_structureId;

    /** The structure state. */
    @Basic
    @Column(name = "STRUCTURE_STATE")
    private int m_structureState;

    /** The structure version. */
    @Basic
    @Column(name = "STRUCTURE_VERSION")
    private int m_structureVersion;

    /** The structure id. */
    @Id
    @Column(name = "VERSION")
    private int m_version;

    /**
     * The default constructor.<p>
     */
    public CmsDAOHistoryStructure() {

        // noop
    }

    /**
     * A public constructor for generating a new historical structure object with an unique id.<p>
     * 
     * @param publishTag the publish tag
     * @param structureId the structure id
     * @param version the version
     */
    public CmsDAOHistoryStructure(int publishTag, String structureId, int version) {

        m_publishTag = publishTag;
        m_structureId = structureId;
        m_version = version;
    }

    /**
     * Returns the dateExpired.<p>
     *
     * @return the dateExpired
     */
    public long getDateExpired() {

        return m_dateExpired;
    }

    /**
     * Returns the dateReleased.<p>
     *
     * @return the dateReleased
     */
    public long getDateReleased() {

        return m_dateReleased;
    }

    /**
     * Returns the parentId.<p>
     *
     * @return the parentId
     */
    public String getParentId() {

        return m_parentId;
    }

    /**
     * Returns the publishTag.<p>
     *
     * @return the publishTag
     */
    public int getPublishTag() {

        return m_publishTag;
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
     * Returns the resourcePath.<p>
     *
     * @return the resourcePath
     */
    public String getResourcePath() {

        return m_resourcePath;
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
     * Returns the structureState.<p>
     *
     * @return the structureState
     */
    public int getStructureState() {

        return m_structureState;
    }

    /**
     * Returns the structureVersion.<p>
     *
     * @return the structureVersion
     */
    public int getStructureVersion() {

        return m_structureVersion;
    }

    /**
     * Returns the version.<p>
     *
     * @return the version
     */
    public int getVersion() {

        return m_version;
    }

    /**
     * Sets the dateExpired.<p>
     *
     * @param dateExpired the dateExpired to set
     */
    public void setDateExpired(long dateExpired) {

        m_dateExpired = dateExpired;
    }

    /**
     * Sets the dateReleased.<p>
     *
     * @param dateReleased the dateReleased to set
     */
    public void setDateReleased(long dateReleased) {

        m_dateReleased = dateReleased;
    }

    /**
     * Sets the parentId.<p>
     *
     * @param parentId the parentId to set
     */
    public void setParentId(String parentId) {

        m_parentId = parentId;
    }

    /**
     * Sets the publishTag.<p>
     *
     * @param publishTag the publishTag to set
     */
    public void setPublishTag(int publishTag) {

        m_publishTag = publishTag;
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
     * Sets the resourcePath.<p>
     *
     * @param resourcePath the resourcePath to set
     */
    public void setResourcePath(String resourcePath) {

        m_resourcePath = resourcePath;
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
     * Sets the structureState.<p>
     *
     * @param structureState the structureState to set
     */
    public void setStructureState(int structureState) {

        m_structureState = structureState;
    }

    /**
     * Sets the structureVersion.<p>
     *
     * @param structureVersion the structureVersion to set
     */
    public void setStructureVersion(int structureVersion) {

        m_structureVersion = structureVersion;
    }

    /**
     * Sets the version.<p>
     *
     * @param version the version to set
     */
    public void setVersion(int version) {

        m_version = version;
    }
}
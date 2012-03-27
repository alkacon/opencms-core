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
 * This data access object represents a publish history entry inside the table "cms_publish_history".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_PUBLISH_HISTORY")
@IdClass(CmsDAOPublishHistory.CmsDAOPublishHistoryPK.class)
public class CmsDAOPublishHistory {

    /**
     * This class implements the primary key for a publish history entry in the table "cms_publish_history".<p>
     */
    public static class CmsDAOPublishHistoryPK implements Serializable {

        /** The serial Version UID. */
        private static final long serialVersionUID = -1996080800534364421L;

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

        /** The history id. */
        public String m_historyId;

        /** The publish tag. */
        public int m_publishTag;

        /** The resource path. */
        public String m_resourcePath;

        /** The structure id. */
        public String m_structureId;

        /**
         * The default constructor.<p>
         */
        public CmsDAOPublishHistoryPK() {

            // noop
        }

        /**
         * A public constructor to generate a primary key from a given String.<p>
         * 
         * @param str the String to generate the id from
         */
        public CmsDAOPublishHistoryPK(String str) {

            fromString(str);
        }

        static {
            // register persistent class in JVM
            try {
                Class.forName("org.opencms.db.jpa.persistence.CmsDAOPublishHistory");
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

            CmsDAOPublishHistoryPK other = (CmsDAOPublishHistoryPK)obj;
            return (((m_historyId == null) && (other.m_historyId == null)) || ((m_historyId != null) && m_historyId.equals(other.m_historyId)))
                && (m_publishTag == other.m_publishTag)
                && (((m_resourcePath == null) && (other.m_resourcePath == null)) || ((m_resourcePath != null) && m_resourcePath.equals(other.m_resourcePath)))
                && (((m_structureId == null) && (other.m_structureId == null)) || ((m_structureId != null) && m_structureId.equals(other.m_structureId)));
        }

        /**
         * Returns the historyId.<p>
         *
         * @return the historyId
         */
        public String getHistoryId() {

            return m_historyId;
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
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            int rs = 17;
            rs = rs * 37 + ((m_historyId == null) ? 0 : m_historyId.hashCode());
            rs = rs * 37 + m_publishTag;
            rs = rs * 37 + ((m_resourcePath == null) ? 0 : m_resourcePath.hashCode());
            rs = rs * 37 + ((m_structureId == null) ? 0 : m_structureId.hashCode());
            return rs;
        }

        /**
         * Sets the historyId.<p>
         *
         * @param historyId the historyId to set
         */
        public void setHistoryId(String historyId) {

            m_historyId = historyId;
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
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return m_historyId + "::" + String.valueOf(m_publishTag) + "::" + m_resourcePath + "::" + m_structureId;
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
                m_historyId = null;
            } else {
                m_historyId = str;
            }
            str = toke.nextToken();
            m_publishTag = Integer.parseInt(str);
            str = toke.nextToken();
            if ("null".equals(str)) {
                m_resourcePath = null;
            } else {
                m_resourcePath = str;
            }
            str = toke.nextToken();
            if ("null".equals(str)) {
                m_structureId = null;
            } else {
                m_structureId = str;
            }
        }
    }

    /** The history id. */
    @Id
    @Column(name = "HISTORY_ID", length = 36)
    private String m_historyId;

    /** The publish tag. */
    @Id
    @Column(name = "PUBLISH_TAG")
    private int m_publishTag;

    /** The resource id. */
    @Basic
    @Column(name = "RESOURCE_ID", nullable = false, length = 36)
    private String m_resourceId;

    /** The resource path. */
    @Id
    @Column(name = "RESOURCE_PATH", length = 1024)
    private String m_resourcePath;

    /** The resource state. */
    @Basic
    @Column(name = "RESOURCE_STATE")
    private int m_resourceState;

    /** The resource type. */
    @Basic
    @Column(name = "RESOURCE_TYPE")
    private int m_resourceType;

    /** The sibling count. */
    @Basic
    @Column(name = "SIBLING_COUNT")
    private int m_siblingCount;

    /** The structure id. */
    @Id
    @Column(name = "STRUCTURE_ID", length = 36)
    private String m_structureId;

    /**
     * The default constructor.<p>
     */
    public CmsDAOPublishHistory() {

        // noop
    }

    /**
     * A public constructor for generating a new publish history object with an unique id.<p>
     * 
     * @param historyId the history id
     * @param publishTag the publish tag
     * @param resourcePath the resource path
     * @param structureId the structure id
     */
    public CmsDAOPublishHistory(String historyId, int publishTag, String resourcePath, String structureId) {

        m_historyId = historyId;
        m_publishTag = publishTag;
        m_resourcePath = resourcePath;
        m_structureId = structureId;
    }

    /**
     * Returns the historyId.<p>
     *
     * @return the historyId
     */
    public String getHistoryId() {

        return m_historyId;
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
     * Returns the resourceState.<p>
     *
     * @return the resourceState
     */
    public int getResourceState() {

        return m_resourceState;
    }

    /**
     * Returns the resourceType.<p>
     *
     * @return the resourceType
     */
    public int getResourceType() {

        return m_resourceType;
    }

    /**
     * Returns the siblingCount.<p>
     *
     * @return the siblingCount
     */
    public int getSiblingCount() {

        return m_siblingCount;
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
     * Sets the historyId.<p>
     *
     * @param historyId the historyId to set
     */
    public void setHistoryId(String historyId) {

        m_historyId = historyId;
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
     * Sets the resourceState.<p>
     *
     * @param resourceState the resourceState to set
     */
    public void setResourceState(int resourceState) {

        m_resourceState = resourceState;
    }

    /**
     * Sets the resourceType.<p>
     *
     * @param resourceType the resourceType to set
     */
    public void setResourceType(int resourceType) {

        m_resourceType = resourceType;
    }

    /**
     * Sets the siblingCount.<p>
     *
     * @param siblingCount the siblingCount to set
     */
    public void setSiblingCount(int siblingCount) {

        m_siblingCount = siblingCount;
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
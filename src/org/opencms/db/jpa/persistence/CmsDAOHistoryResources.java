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
 * This data access object represents a historical resource entry 
 * inside the table "cms_history_resources".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_HISTORY_RESOURCES")
@IdClass(CmsDAOHistoryResources.CmsDAOHistoryResourcesPK.class)
public class CmsDAOHistoryResources {

    /**
     * This class implements the primary key for a historical resource entry in the table "cms_history_resources".<p>
     */
    public static class CmsDAOHistoryResourcesPK implements Serializable {

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
        private static final long serialVersionUID = 2279429675070947072L;

        /** The publish tag. */
        private int m_publishTag;

        /** The resource id. */
        private String m_resourceId;

        /**
         * The default constructor.<p>
         */
        public CmsDAOHistoryResourcesPK() {

            // noop
        }

        /**
         * A public constructor to generate a primary key from a given String.<p>
         * 
         * @param str the String to generate the id from
         */
        public CmsDAOHistoryResourcesPK(String str) {

            fromString(str);
        }

        static {
            // register persistent class in JVM
            try {
                Class.forName("org.opencms.db.jpa.persistence.CmsDAOHistoryResources");
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

            CmsDAOHistoryResourcesPK other = (CmsDAOHistoryResourcesPK)obj;
            return (m_publishTag == other.m_publishTag)
                && (((m_resourceId == null) && (other.m_resourceId == null)) || ((m_resourceId != null) && m_resourceId.equals(other.m_resourceId)));
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
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            int rs = 17;
            rs = rs * 37 + m_publishTag;
            rs = rs * 37 + ((m_resourceId == null) ? 0 : m_resourceId.hashCode());
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

            return String.valueOf(m_publishTag) + "::" + m_resourceId;
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
                m_resourceId = null;
            } else {
                m_resourceId = str;
            }
        }
    }

    /** The date content. */
    @Basic
    @Column(name = "DATE_CONTENT")
    private long m_dateContent;

    /** The creation date. */
    @Basic
    @Column(name = "DATE_CREATED")
    private long m_dateCreated;

    /** The date last modified. */
    @Basic
    @Column(name = "DATE_LASTMODIFIED")
    private long m_dateLastModified;

    /** The project last modified. */
    @Basic
    @Column(name = "PROJECT_LASTMODIFIED", nullable = false, length = 36)
    private String m_projectLastModified;

    /** The publish tag. */
    @Id
    @Column(name = "PUBLISH_TAG")
    private int m_publishTag;

    /** The resource flags. */
    @Basic
    @Column(name = "RESOURCE_FLAGS")
    private int m_resourceFlags;

    /** The resource id. */
    @Id
    @Column(name = "RESOURCE_ID", length = 36)
    private String m_resourceId;

    /** The resource size. */
    @Basic
    @Column(name = "RESOURCE_SIZE")
    private int m_resourceSize;

    /** The resource state. */
    @Basic
    @Column(name = "RESOURCE_STATE")
    private int m_resourceState;

    /** The resource type. */
    @Basic
    @Column(name = "RESOURCE_TYPE")
    private int m_resourceType;

    /** The version of the resource. */
    @Basic
    @Column(name = "RESOURCE_VERSION")
    private int m_resourceVersion;

    /** The count of siblings. */
    @Basic
    @Column(name = "SIBLING_COUNT")
    private int m_siblingCount;

    /** The user who has created the resource. */
    @Basic
    @Column(name = "USER_CREATED", nullable = false, length = 36)
    private String m_userCreated;

    /** The user last modified. */
    @Basic
    @Column(name = "USER_LASTMODIFIED", nullable = false, length = 36)
    private String m_userLastModified;

    /**
     * The default constructor.<p>
     */
    public CmsDAOHistoryResources() {

        // noop
    }

    /**
     * A public constructor for generating a new contents object with an unique id.<p>
     * 
     * @param publishTag the publish tag
     * @param resourceId the resource id
     */
    public CmsDAOHistoryResources(int publishTag, String resourceId) {

        m_publishTag = publishTag;
        m_resourceId = resourceId;
    }

    /**
     * Returns the dateContent.<p>
     *
     * @return the dateContent
     */
    public long getDateContent() {

        return m_dateContent;
    }

    /**
     * Returns the dateCreated.<p>
     *
     * @return the dateCreated
     */
    public long getDateCreated() {

        return m_dateCreated;
    }

    /**
     * Returns the dateLastModified.<p>
     *
     * @return the dateLastModified
     */
    public long getDateLastModified() {

        return m_dateLastModified;
    }

    /**
     * Returns the projectLastModified.<p>
     *
     * @return the projectLastModified
     */
    public String getProjectLastModified() {

        return m_projectLastModified;
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
     * Returns the resourceFlags.<p>
     *
     * @return the resourceFlags
     */
    public int getResourceFlags() {

        return m_resourceFlags;
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
     * Returns the resourceSize.<p>
     *
     * @return the resourceSize
     */
    public int getResourceSize() {

        return m_resourceSize;
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
     * Returns the resourceVersion.<p>
     *
     * @return the resourceVersion
     */
    public int getResourceVersion() {

        return m_resourceVersion;
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
     * Returns the userCreated.<p>
     *
     * @return the userCreated
     */
    public String getUserCreated() {

        return m_userCreated;
    }

    /**
     * Returns the userLastModified.<p>
     *
     * @return the userLastModified
     */
    public String getUserLastModified() {

        return m_userLastModified;
    }

    /**
     * Sets the dateContent.<p>
     *
     * @param dateContent the dateContent to set
     */
    public void setDateContent(long dateContent) {

        m_dateContent = dateContent;
    }

    /**
     * Sets the dateCreated.<p>
     *
     * @param dateCreated the dateCreated to set
     */
    public void setDateCreated(long dateCreated) {

        m_dateCreated = dateCreated;
    }

    /**
     * Sets the dateLastModified.<p>
     *
     * @param dateLastModified the dateLastModified to set
     */
    public void setDateLastModified(long dateLastModified) {

        m_dateLastModified = dateLastModified;
    }

    /**
     * Sets the projectLastModified.<p>
     *
     * @param projectLastModified the projectLastModified to set
     */
    public void setProjectLastModified(String projectLastModified) {

        m_projectLastModified = projectLastModified;
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
     * Sets the resourceFlags.<p>
     *
     * @param resourceFlags the resourceFlags to set
     */
    public void setResourceFlags(int resourceFlags) {

        m_resourceFlags = resourceFlags;
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
     * Sets the resourceSize.<p>
     *
     * @param resourceSize the resourceSize to set
     */
    public void setResourceSize(int resourceSize) {

        m_resourceSize = resourceSize;
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
     * Sets the resourceVersion.<p>
     *
     * @param resourceVersion the resourceVersion to set
     */
    public void setResourceVersion(int resourceVersion) {

        m_resourceVersion = resourceVersion;
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
     * Sets the userCreated.<p>
     *
     * @param userCreated the userCreated to set
     */
    public void setUserCreated(String userCreated) {

        m_userCreated = userCreated;
    }

    /**
     * Sets the userLastModified.<p>
     *
     * @param userLastModified the userLastModified to set
     */
    public void setUserLastModified(String userLastModified) {

        m_userLastModified = userLastModified;
    }
}
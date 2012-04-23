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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * This data access object represents a project resource entry inside the table "cms_projectresources".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_PROJECTRESOURCES")
@IdClass(CmsDAOProjectResources.CmsDAOProjectResourcesPK.class)
public class CmsDAOProjectResources {

    /**
     * This class implements the primary key for a project resource entry in the table "cms_projectresources".<p>
     */
    public static class CmsDAOProjectResourcesPK implements Serializable {

        /** The serial Version UID. */
        private static final long serialVersionUID = 6018720614639293842L;

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

        /** The project id. */
        public String m_projectId;

        /** The resource path. */
        public String m_resourcePath;

        /**
         * The default constructor.<p>
         */
        public CmsDAOProjectResourcesPK() {

            // noop
        }

        /**
         * A public constructor to generate a primary key from a given String.<p>
         * 
         * @param str the String to generate the id from
         */
        public CmsDAOProjectResourcesPK(String str) {

            fromString(str);
        }

        static {
            // register persistent class in JVM
            try {
                Class.forName("org.opencms.db.jpa.persistence.CmsDAOProjectResources");
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

            CmsDAOProjectResourcesPK other = (CmsDAOProjectResourcesPK)obj;
            return (((m_projectId == null) && (other.m_projectId == null)) || ((m_projectId != null) && m_projectId.equals(other.m_projectId)))
                && (((m_resourcePath == null) && (other.m_resourcePath == null)) || ((m_resourcePath != null) && m_resourcePath.equals(other.m_resourcePath)));
        }

        /**
         * Returns the projectId.<p>
         *
         * @return the projectId
         */
        public String getProjectId() {

            return m_projectId;
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
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            int rs = 17;
            rs = rs * 37 + ((m_projectId == null) ? 0 : m_projectId.hashCode());
            rs = rs * 37 + ((m_resourcePath == null) ? 0 : m_resourcePath.hashCode());
            return rs;
        }

        /**
         * Sets the projectId.<p>
         *
         * @param projectId the projectId to set
         */
        public void setProjectId(String projectId) {

            m_projectId = projectId;
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
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return m_projectId + "::" + m_resourcePath;
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
                m_projectId = null;
            } else {
                m_projectId = str;
            }
            str = toke.nextToken();
            if ("null".equals(str)) {
                m_resourcePath = null;
            } else {
                m_resourcePath = str;
            }
        }
    }

    /** The project id. */
    @Id
    @Column(name = "PROJECT_ID", length = 36)
    private String m_projectId;

    /** The resource path. */
    @Id
    @Column(name = "RESOURCE_PATH", length = 1024)
    private String m_resourcePath;

    /**
     * The default constructor.<p>
     */
    public CmsDAOProjectResources() {

        // noop
    }

    /**
     * A public constructor for generating a new project resource object with an unique id.<p>
     * 
     * @param projectId the project id
     * @param resourcePath the resource path
     */
    public CmsDAOProjectResources(String projectId, String resourcePath) {

        m_projectId = projectId;
        m_resourcePath = resourcePath;
    }

    /**
     * Returns the projectId.<p>
     *
     * @return the projectId
     */
    public String getProjectId() {

        return m_projectId;
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
     * Sets the projectId.<p>
     *
     * @param projectId the projectId to set
     */
    public void setProjectId(String projectId) {

        m_projectId = projectId;
    }

    /**
     * Sets the resourcePath.<p>
     *
     * @param resourcePath the resourcePath to set
     */
    public void setResourcePath(String resourcePath) {

        m_resourcePath = resourcePath;
    }

}
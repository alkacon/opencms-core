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
 * This data access object represents a cms contents entry inside the table "cms_contents".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_CONTENTS")
@IdClass(CmsDAOContents.CmsDAOContentsPK.class)
public class CmsDAOContents {

    /**
     * This class implements the primary key for a opencms content entry in the table "cms_contents".<p>
     */
    public static class CmsDAOContentsPK implements Serializable {

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

        /** Serial version UID. */
        private static final long serialVersionUID = -6613485187813913967L;

        /** The publish tag from. */
        public int m_publishTagFrom;

        /** The resource id. */
        public String m_resourceId;

        /**
         * The public constructor for this Bean.<p>
         */
        public CmsDAOContentsPK() {

            // noop
        }

        /**
         * A public constructor to generate a primary key from a given String.<p>
         * 
         * @param str the String to generate the id from
         */
        public CmsDAOContentsPK(String str) {

            fromString(str);
        }

        static {
            // register persistent class in JVM
            try {
                Class.forName("org.opencms.db.jpa.persistence.CmsDAOContents");
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

            CmsDAOContentsPK other = (CmsDAOContentsPK)obj;
            return (m_publishTagFrom == other.m_publishTagFrom)
                && (((m_resourceId == null) && (other.m_resourceId == null)) || ((m_resourceId != null) && m_resourceId.equals(other.m_resourceId)));
        }

        /**
         * Returns the publish tag from.<p>
         * 
         * @return the publish tag from
         */
        public int getPublishTagFrom() {

            return m_publishTagFrom;
        }

        /**
         * Returns the resource id.<p>
         * 
         * @return the resource id
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
            rs = rs * 37 + m_publishTagFrom;
            rs = rs * 37 + ((m_resourceId == null) ? 0 : m_resourceId.hashCode());
            return rs;
        }

        /**
         * Sets the publish tag from.<p>
         * 
         * @param publishTagFrom the publish tag to set
         */
        public void setPublishTagFrom(int publishTagFrom) {

            m_publishTagFrom = publishTagFrom;
        }

        /**
         * Sets the resource id.<p>
         * 
         * @param resourceId the resource id to set
         */
        public void setResourceId(String resourceId) {

            m_resourceId = resourceId;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return String.valueOf(m_publishTagFrom) + "::" + m_resourceId;
        }

        /**
         * Parses the publish tag from and the resource id from a given String.<p>
         *  
         * @param str the String to parse
         */
        private void fromString(String str) {

            Tokenizer toke = new Tokenizer(str);
            str = toke.nextToken();
            m_publishTagFrom = Integer.parseInt(str);
            str = toke.nextToken();
            if ("null".equals(str)) {
                m_resourceId = null;
            } else {
                m_resourceId = str;
            }
        }
    }

    /** The file contents column. */
    @Basic
    @Lob
    @Column(name = "FILE_CONTENT", nullable = true)
    private byte[] m_fileContent;

    /** The online flag column. */
    @Basic
    @Column(name = "ONLINE_FLAG")
    private int m_onlineFlag;

    /** The publish tag from column. */
    @Id
    @Column(name = "PUBLISH_TAG_FROM")
    private int m_publishTagFrom;

    /** The publish tag to column. */
    @Basic
    @Column(name = "PUBLISH_TAG_TO")
    private int m_publishTagTo;

    /** The resource id column. */
    @Id
    @Column(name = "RESOURCE_ID", length = 36)
    private String m_resourceId;

    /**
     * The public constructor.<p>
     */
    public CmsDAOContents() {

        // noop
    }

    /**
     * A public constructor for generating a new contents object with an unique id.<p>
     *  
     * @param publishTagFrom the publish tag from
     * @param resourceId the resource id from
     */
    public CmsDAOContents(int publishTagFrom, String resourceId) {

        m_publishTagFrom = publishTagFrom;
        m_resourceId = resourceId;
    }

    /**
     * Returns the file contents.<p>
     * 
     * @return the file contents
     */
    public byte[] getFileContent() {

        return m_fileContent;
    }

    /**
     * Returns the online flag.<p>
     * 
     * @return the online flag
     */
    public int getOnlineFlag() {

        return m_onlineFlag;
    }

    /**
     * Returns the publish tag from.<p>
     * 
     * @return the publish tag from
     */
    public int getPublishTagFrom() {

        return m_publishTagFrom;
    }

    /**
     * Returns the publish tag to.<p>
     * 
     * @return the publish flag to
     */
    public int getPublishTagTo() {

        return m_publishTagTo;
    }

    /**
     * Returns the resource id.<p>
     * 
     * @return the resource id
     */
    public String getResourceId() {

        return m_resourceId;
    }

    /**
     * Sets the file contents.<p>
     * 
     * @param fileContent the content to set
     */
    public void setFileContent(byte[] fileContent) {

        m_fileContent = fileContent;
    }

    /**
     * Sets the online flag.<p>
     * 
     * @param onlineFlag the flag to set
     */
    public void setOnlineFlag(int onlineFlag) {

        m_onlineFlag = onlineFlag;
    }

    /**
     * Sets the publish tag from.<p>
     * 
     * @param publishTagFrom the publish tag to set
     */
    public void setPublishTagFrom(int publishTagFrom) {

        m_publishTagFrom = publishTagFrom;
    }

    /**
     * Sets the publish tag to.<p>
     * 
     * @param publishTagTo the flag to set
     */
    public void setPublishTagTo(int publishTagTo) {

        m_publishTagTo = publishTagTo;
    }

    /**
     * Sets the resource id.<p>
     * 
     * @param resourceId the resource id to set
     */
    public void setResourceId(String resourceId) {

        m_resourceId = resourceId;
    }
}
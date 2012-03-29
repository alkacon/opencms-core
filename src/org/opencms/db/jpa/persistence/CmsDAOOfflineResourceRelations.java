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
 * This data access object represents a offline resource relation entry 
 * inside the table "cms_offline_resource_relations".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_OFFLINE_RESOURCE_RELATIONS")
@IdClass(org.opencms.db.jpa.persistence.CmsDAOOfflineResourceRelations.CmsDAOOfflineResourceRelationsPK.class)
public class CmsDAOOfflineResourceRelations implements I_CmsDAOResourceRelations {

    /**
     * This class implements the primary key for a offline resource relation entry 
     * in the table "cms_offline_resource_relations".<p>
     */
    public static class CmsDAOOfflineResourceRelationsPK implements Serializable {

        /** The serial Version UID. */
        private static final long serialVersionUID = 7268452936498382758L;

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

        /** The relation source id. */
        public String m_relationSourceId;

        /** The relation source path. */
        public String m_relationSourcePath;

        /** The relation target id. */
        public String m_relationTargetId;

        /** The relation target path. */
        public String m_relationTargetPath;

        /** The relation type. */
        public int m_relationType;

        /**
         * The default constructor.<p>
         */
        public CmsDAOOfflineResourceRelationsPK() {

            // noop
        }

        /**
         * A public constructor to generate a primary key from a given String.<p>
         * 
         * @param str the String to generate the id from
         */
        public CmsDAOOfflineResourceRelationsPK(String str) {

            fromString(str);
        }

        static {
            // register persistent class in JVM
            try {
                Class.forName("org.opencms.db.jpa.persistence.CmsDAOOfflineResourceRelations");
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

            CmsDAOOfflineResourceRelationsPK other = (CmsDAOOfflineResourceRelationsPK)obj;
            return (((m_relationSourceId == null) && (other.m_relationSourceId == null)) || ((m_relationSourceId != null) && m_relationSourceId.equals(other.m_relationSourceId)))
                && (((m_relationSourcePath == null) && (other.m_relationSourcePath == null)) || ((m_relationSourcePath != null) && m_relationSourcePath.equals(other.m_relationSourcePath)))
                && (((m_relationTargetId == null) && (other.m_relationTargetId == null)) || ((m_relationTargetId != null) && m_relationTargetId.equals(other.m_relationTargetId)))
                && (((m_relationTargetPath == null) && (other.m_relationTargetPath == null)) || ((m_relationTargetPath != null) && m_relationTargetPath.equals(other.m_relationTargetPath)))
                && (m_relationType == other.m_relationType);
        }

        /**
         * Returns the relationSourceId.<p>
         *
         * @return the relationSourceId
         */
        public String getRelationSourceId() {

            return m_relationSourceId;
        }

        /**
         * Returns the relationSourcePath.<p>
         *
         * @return the relationSourcePath
         */
        public String getRelationSourcePath() {

            return m_relationSourcePath;
        }

        /**
         * Returns the relationTargetId.<p>
         *
         * @return the relationTargetId
         */
        public String getRelationTargetId() {

            return m_relationTargetId;
        }

        /**
         * Returns the relationTargetPath.<p>
         *
         * @return the relationTargetPath
         */
        public String getRelationTargetPath() {

            return m_relationTargetPath;
        }

        /**
         * Returns the relationType.<p>
         *
         * @return the relationType
         */
        public int getRelationType() {

            return m_relationType;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            int rs = 17;
            rs = rs * 37 + ((m_relationSourceId == null) ? 0 : m_relationSourceId.hashCode());
            rs = rs * 37 + ((m_relationSourcePath == null) ? 0 : m_relationSourcePath.hashCode());
            rs = rs * 37 + ((m_relationTargetId == null) ? 0 : m_relationTargetId.hashCode());
            rs = rs * 37 + ((m_relationTargetPath == null) ? 0 : m_relationTargetPath.hashCode());
            rs = rs * 37 + m_relationType;
            return rs;
        }

        /**
         * Sets the relationSourceId.<p>
         *
         * @param relationSourceId the relationSourceId to set
         */
        public void setRelationSourceId(String relationSourceId) {

            m_relationSourceId = relationSourceId;
        }

        /**
         * Sets the relationSourcePath.<p>
         *
         * @param relationSourcePath the relationSourcePath to set
         */
        public void setRelationSourcePath(String relationSourcePath) {

            m_relationSourcePath = relationSourcePath;
        }

        /**
         * Sets the relationTargetId.<p>
         *
         * @param relationTargetId the relationTargetId to set
         */
        public void setRelationTargetId(String relationTargetId) {

            m_relationTargetId = relationTargetId;
        }

        /**
         * Sets the relationTargetPath.<p>
         *
         * @param relationTargetPath the relationTargetPath to set
         */
        public void setRelationTargetPath(String relationTargetPath) {

            m_relationTargetPath = relationTargetPath;
        }

        /**
         * Sets the relationType.<p>
         *
         * @param relationType the relationType to set
         */
        public void setRelationType(int relationType) {

            m_relationType = relationType;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return m_relationSourceId
                + "::"
                + m_relationSourcePath
                + "::"
                + m_relationTargetId
                + "::"
                + m_relationTargetPath
                + "::"
                + String.valueOf(m_relationType);
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
                m_relationSourceId = null;
            } else {
                m_relationSourceId = str;
            }
            str = toke.nextToken();
            if ("null".equals(str)) {
                m_relationSourcePath = null;
            } else {
                m_relationSourcePath = str;
            }
            str = toke.nextToken();
            if ("null".equals(str)) {
                m_relationTargetId = null;
            } else {
                m_relationTargetId = str;
            }
            str = toke.nextToken();
            if ("null".equals(str)) {
                m_relationTargetPath = null;
            } else {
                m_relationTargetPath = str;
            }
            str = toke.nextToken();
            m_relationType = Integer.parseInt(str);
        }
    }

    /** The relation source id. */
    @Id
    @Column(name = "RELATION_SOURCE_ID", nullable = false, length = 36)
    private String m_relationSourceId;

    /** The relation source path. */
    @Id
    @Column(name = "RELATION_SOURCE_PATH", nullable = false, length = 1024)
    private String m_relationSourcePath;

    /** The relation target id. */
    @Id
    @Column(name = "RELATION_TARGET_ID", nullable = false, length = 36)
    private String m_relationTargetId;

    /** The relation target path. */
    @Id
    @Column(name = "RELATION_TARGET_PATH", nullable = false, length = 1024)
    private String m_relationTargetPath;

    /** The relation type. */
    @Id
    @Column(name = "RELATION_TYPE")
    private int m_relationType;

    /**
     * The default constructor.<p>
     */
    public CmsDAOOfflineResourceRelations() {

        // noop
    }

    /**
     * A public constructor for generating a new offline resource relation object with an unique id.<p>
     * 
     * @param relationSourceId
     * @param relationSourcePath
     * @param relationTargetId
     * @param relationTargetPath
     * @param relationType
     */
    public CmsDAOOfflineResourceRelations(
        String relationSourceId,
        String relationSourcePath,
        String relationTargetId,
        String relationTargetPath,
        int relationType) {

        m_relationSourceId = relationSourceId;
        m_relationSourcePath = relationSourcePath;
        m_relationTargetId = relationTargetId;
        m_relationTargetPath = relationTargetPath;
        m_relationType = relationType;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResourceRelations#getRelationSourceId()
     */
    public String getRelationSourceId() {

        return m_relationSourceId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResourceRelations#getRelationSourcePath()
     */
    public String getRelationSourcePath() {

        return m_relationSourcePath;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResourceRelations#getRelationTargetId()
     */
    public String getRelationTargetId() {

        return m_relationTargetId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResourceRelations#getRelationTargetPath()
     */
    public String getRelationTargetPath() {

        return m_relationTargetPath;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResourceRelations#getRelationType()
     */
    public int getRelationType() {

        return m_relationType;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResourceRelations#setRelationSourceId(java.lang.String)
     */
    public void setRelationSourceId(String relationSourceId) {

        m_relationSourceId = relationSourceId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResourceRelations#setRelationSourcePath(java.lang.String)
     */
    public void setRelationSourcePath(String relationSourcePath) {

        m_relationSourcePath = relationSourcePath;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResourceRelations#setRelationTargetId(java.lang.String)
     */
    public void setRelationTargetId(String relationTargetId) {

        m_relationTargetId = relationTargetId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResourceRelations#setRelationTargetPath(java.lang.String)
     */
    public void setRelationTargetPath(String relationTargetPath) {

        m_relationTargetPath = relationTargetPath;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOResourceRelations#setRelationType(int)
     */
    public void setRelationType(int relationType) {

        m_relationType = relationType;
    }
}
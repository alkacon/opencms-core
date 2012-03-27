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
 * This data access object represents a history properties entry 
 * inside the table "cms_history_properties".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_HISTORY_PROPERTIES")
@IdClass(CmsDAOHistoryProperties.CmsDAOHistoryPropertiesPK.class)
public class CmsDAOHistoryProperties {

    /**
     * This class implements the primary key for a history properties entry in the table "cms_history_properties".<p>
     */
    public static class CmsDAOHistoryPropertiesPK implements Serializable {

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
        private static final long serialVersionUID = 3076741403580490854L;

        /** The property definition id. */
        public String m_propertyDefId;

        /** The property mapping type. */
        public int m_propertyMappingType;

        /** The publish tag. */
        public int m_publishTag;

        /** The structure id. */
        public String m_structureId;

        /**
         * The default constructor.<p>
         */
        public CmsDAOHistoryPropertiesPK() {

            // noop
        }

        /**
         * A public constructor to generate a primary key from a given String.<p>
         * 
         * @param str the String to generate the id from
         */
        public CmsDAOHistoryPropertiesPK(String str) {

            fromString(str);
        }

        static {
            // register persistent class in JVM
            try {
                Class.forName("org.opencms.db.jpa.persistence.CmsDAOHistoryProperties");
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

            CmsDAOHistoryPropertiesPK other = (CmsDAOHistoryPropertiesPK)obj;
            return (m_propertyMappingType == other.m_propertyMappingType)
                && (((m_propertyDefId == null) && (other.m_propertyDefId == null)) || ((m_propertyDefId != null) && m_propertyDefId.equals(other.m_propertyDefId)))
                && (m_publishTag == other.m_publishTag)
                && (((m_structureId == null) && (other.m_structureId == null)) || ((m_structureId != null) && m_structureId.equals(other.m_structureId)));
        }

        /**
         * Returns the propertyDefId.<p>
         *
         * @return the propertyDefId
         */
        public String getPropertyDefId() {

            return m_propertyDefId;
        }

        /**
         * Returns the propertyMappingType.<p>
         *
         * @return the propertyMappingType
         */
        public int getPropertyMappingType() {

            return m_propertyMappingType;
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
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            int rs = 17;
            rs = rs * 37 + m_propertyMappingType;
            rs = rs * 37 + ((m_propertyDefId == null) ? 0 : m_propertyDefId.hashCode());
            rs = rs * 37 + m_publishTag;
            rs = rs * 37 + ((m_structureId == null) ? 0 : m_structureId.hashCode());
            return rs;
        }

        /**
         * Sets the propertyDefId.<p>
         *
         * @param propertyDefId the propertyDefId to set
         */
        public void setPropertyDefId(String propertyDefId) {

            m_propertyDefId = propertyDefId;
        }

        /**
         * Sets the propertyMappingType.<p>
         *
         * @param propertyMappingType the propertyMappingType to set
         */
        public void setPropertyMappingType(int propertyMappingType) {

            m_propertyMappingType = propertyMappingType;
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
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return String.valueOf(m_propertyMappingType)
                + "::"
                + m_propertyDefId
                + "::"
                + String.valueOf(m_publishTag)
                + "::"
                + m_structureId;
        }

        /**
         * Parses the publish tag from and the resource id from a given String.<p>
         *  
         * @param str the String to parse
         */
        private void fromString(String str) {

            Tokenizer toke = new Tokenizer(str);
            str = toke.nextToken();
            m_propertyMappingType = Integer.parseInt(str);
            str = toke.nextToken();
            if ("null".equals(str)) {
                m_propertyDefId = null;
            } else {
                m_propertyDefId = str;
            }
            str = toke.nextToken();
            m_publishTag = Integer.parseInt(str);
            str = toke.nextToken();
            if ("null".equals(str)) {
                m_structureId = null;
            } else {
                m_structureId = str;
            }
        }
    }

    /** The property definition id. */
    @Id
    @Column(name = "PROPERTYDEF_ID", length = 36)
    private String m_propertyDefId;

    /** The property mapping id. */
    @Basic
    @Column(name = "PROPERTY_MAPPING_ID", nullable = false, length = 36)
    private String m_propertyMappingId;

    /** The property mapping type. */
    @Id
    @Column(name = "PROPERTY_MAPPING_TYPE")
    private int m_propertyMappingType;

    /** The property value. */
    @Basic
    @Column(name = "PROPERTY_VALUE", nullable = false, length = 2048)
    private String m_propertyValue;

    /** The publish tag. */
    @Id
    @Column(name = "PUBLISH_TAG")
    private int m_publishTag;

    /** The structure id. */
    @Id
    @Column(name = "STRUCTURE_ID", length = 36)
    private String m_structureId;

    /**
     * The default constructor.<p>
     */
    public CmsDAOHistoryProperties() {

        // noop
    }

    /**
     * A public constructor for generating a new history property object with an unique id.<p>
     * 
     * @param propertyMappingType the property mapping type 
     * @param propertydefId  the property definition id
     * @param publishTag the publish tag
     * @param structureId the structure id
     */
    public CmsDAOHistoryProperties(int propertyMappingType, String propertydefId, int publishTag, String structureId) {

        m_propertyMappingType = propertyMappingType;
        m_propertyDefId = propertydefId;
        m_publishTag = publishTag;
        m_structureId = structureId;
    }

    /**
     * Returns the propertyDefId.<p>
     *
     * @return the propertyDefId
     */
    public String getPropertyDefId() {

        return m_propertyDefId;
    }

    /**
     * Returns the propertyMappingId.<p>
     *
     * @return the propertyMappingId
     */
    public String getPropertyMappingId() {

        return m_propertyMappingId;
    }

    /**
     * Returns the propertyMappingType.<p>
     *
     * @return the propertyMappingType
     */
    public int getPropertyMappingType() {

        return m_propertyMappingType;
    }

    /**
     * Returns the propertyValue.<p>
     *
     * @return the propertyValue
     */
    public String getPropertyValue() {

        return m_propertyValue;
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
     * Sets the propertyDefId.<p>
     *
     * @param propertyDefId the propertyDefId to set
     */
    public void setPropertyDefId(String propertyDefId) {

        m_propertyDefId = propertyDefId;
    }

    /**
     * Sets the propertyMappingId.<p>
     *
     * @param propertyMappingId the propertyMappingId to set
     */
    public void setPropertyMappingId(String propertyMappingId) {

        m_propertyMappingId = propertyMappingId;
    }

    /**
     * Sets the propertyMappingType.<p>
     *
     * @param propertyMappingType the propertyMappingType to set
     */
    public void setPropertyMappingType(int propertyMappingType) {

        m_propertyMappingType = propertyMappingType;
    }

    /**
     * Sets the propertyValue.<p>
     *
     * @param propertyValue the propertyValue to set
     */
    public void setPropertyValue(String propertyValue) {

        m_propertyValue = propertyValue;
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

}
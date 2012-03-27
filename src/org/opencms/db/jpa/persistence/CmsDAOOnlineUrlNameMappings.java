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
 * This data access object represents a online URL name mapping entry inside the table "cms_online_urlname_mappings".<p>
 * 
 * @since 8.0.0
 */
@Entity
@Table(name = "CMS_ONLINE_URLNAME_MAPPINGS")
@IdClass(org.opencms.db.jpa.persistence.CmsDAOOnlineUrlNameMappings.CmsDAOOnlineUrlNameMappingsPK.class)
public class CmsDAOOnlineUrlNameMappings implements I_CmsDAOUrlNameMappings {

    /**
     * This class implements the primary key for a URL name mapping entry in the table "cms_online_urlname_mappings".<p>
     */
    public static class CmsDAOOnlineUrlNameMappingsPK implements Serializable {

        /**
         * A tokenizer.<p>
         */
        protected static class Tokenizer {

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
        private static final long serialVersionUID = -6569284891719573461L;

        /** The date changed. */
        public long m_dateChanged;

        /** The name. */
        public String m_name;

        /** The state. */
        public int m_state;

        /** The structure id. */
        public String m_structureId;

        /**
         * The public constructor for this DAO.<p>
         */
        public CmsDAOOnlineUrlNameMappingsPK() {

            // noop
        }

        /**
         * A public constructor to generate a primary key from a given String.<p>
         * 
         * @param str the String to generate the id from
         */
        public CmsDAOOnlineUrlNameMappingsPK(String str) {

            fromString(str);
        }

        static {
            // register persistent class in JVM
            try {
                Class.forName("org.opencms.db.jpa.persistence.CmsDAOOnlineUrlNameMappings");
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

            CmsDAOOnlineUrlNameMappingsPK other = (CmsDAOOnlineUrlNameMappingsPK)obj;
            return (m_dateChanged == other.m_dateChanged)
                && (((m_name == null) && (other.m_name == null)) || ((m_name != null) && m_name.equals(other.m_name)))
                && (m_state == other.m_state)
                && (((m_structureId == null) && (other.m_structureId == null)) || ((m_structureId != null) && m_structureId.equals(other.m_structureId)));
        }

        /**
         * Returns the dateChanged.<p>
         *
         * @return the dateChanged
         */
        public long getDateChanged() {

            return m_dateChanged;
        }

        /**
         * Returns the name.<p>
         *
         * @return the name
         */
        public String getName() {

            return m_name;
        }

        /**
         * Returns the state.<p>
         *
         * @return the state
         */
        public int getState() {

            return m_state;
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
            rs = rs * 37 + (int)(m_dateChanged ^ (m_dateChanged >>> 32));
            rs = rs * 37 + ((m_name == null) ? 0 : m_name.hashCode());
            rs = rs * 37 + m_state;
            rs = rs * 37 + ((m_structureId == null) ? 0 : m_structureId.hashCode());
            return rs;
        }

        /**
         * Sets the dateChanged.<p>
         *
         * @param dateChanged the dateChanged to set
         */
        public void setDateChanged(long dateChanged) {

            m_dateChanged = dateChanged;
        }

        /**
         * Sets the name.<p>
         *
         * @param name the name to set
         */
        public void setName(String name) {

            m_name = name;
        }

        /**
         * Sets the state.<p>
         *
         * @param state the state to set
         */
        public void setState(int state) {

            m_state = state;
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

            return String.valueOf(m_dateChanged)
                + "::"
                + m_name
                + "::"
                + String.valueOf(m_state)
                + "::"
                + m_structureId;
        }

        /**
         * Parses the given String into a PK.<p>
         *  
         * @param str the String to parse
         */
        private void fromString(String str) {

            Tokenizer toke = new Tokenizer(str);
            str = toke.nextToken();
            m_dateChanged = Long.parseLong(str);
            str = toke.nextToken();
            if ("null".equals(str)) {
                m_name = null;
            } else {
                m_name = str;
            }
            str = toke.nextToken();
            m_state = Integer.parseInt(str);
            str = toke.nextToken();
            if ("null".equals(str)) {
                m_structureId = null;
            } else {
                m_structureId = str;
            }
        }
    }

    /** The date changed. */
    @Id
    @Column(name = "DATE_CHANGED")
    private long m_dateChanged;

    /** The name. */
    @Id
    @Column(name = "NAME", length = 255)
    private String m_name;

    /** The state. */
    @Id
    @Column(name = "STATE")
    private int m_state;

    /** The structure id. */
    @Id
    @Column(name = "STRUCTURE_ID", length = 36)
    private String m_structureId;

    /** The locale. */
    @Column(name = "LOCALE", length = 10)
    private String m_locale;

    /**
     * The public constructor.<p>
     */
    public CmsDAOOnlineUrlNameMappings() {

        // noop
    }

    /**
     * A public constructor for generating a new contents object with an unique id.<p>
     * 
     * @param dateChanged the date changed
     * @param name the name
     * @param state the state
     * @param structureId the structure id
     */
    public CmsDAOOnlineUrlNameMappings(long dateChanged, String name, int state, String structureId) {

        m_dateChanged = dateChanged;
        m_name = name;
        m_state = state;
        m_structureId = structureId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOUrlNameMappings#getDateChanged()
     */
    public long getDateChanged() {

        return m_dateChanged;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOUrlNameMappings#getLocale()
     */
    public String getLocale() {

        return m_locale;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOUrlNameMappings#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOUrlNameMappings#getState()
     */
    public int getState() {

        return m_state;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOUrlNameMappings#getStructureId()
     */
    public String getStructureId() {

        return m_structureId;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOUrlNameMappings#setDateChanged(long)
     */
    public void setDateChanged(long dateChanged) {

        this.m_dateChanged = dateChanged;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOUrlNameMappings#setLocale(java.lang.String)
     */
    public void setLocale(String locale) {

        this.m_locale = locale;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOUrlNameMappings#setName(java.lang.String)
     */
    public void setName(String name) {

        this.m_name = name;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOUrlNameMappings#setState(int)
     */
    public void setState(int state) {

        this.m_state = state;
    }

    /**
     * @see org.opencms.db.jpa.persistence.I_CmsDAOUrlNameMappings#setStructureId(java.lang.String)
     */
    public void setStructureId(String structureId) {

        this.m_structureId = structureId;
    }
}
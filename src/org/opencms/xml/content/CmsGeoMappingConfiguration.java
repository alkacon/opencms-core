/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.xml.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Geo-coordinate mapping configuration for an XML content schema.
 *
 * <p>Consists of a list of entries, where each entry has a type and a value.
 */
public class CmsGeoMappingConfiguration {

    /**
     * A single configuration entry.
     */
    public static class Entry {

        /** The mapping type. */
        private EntryType m_type;

        /** The parameter value. */
        private String m_value;

        /**
         * Creates a new entry.
         *
         * @param type the entry type
         * @param value the entry parameter value
         */
        public Entry(EntryType type, String value) {

            super();
            m_type = type;
            m_value = value;
            if (m_value == null) {
                m_value = "";
            } else {
                m_value = m_value.trim();
            }
        }

        /**
         * Gets the mapping type.
         *
         * @return the mapping type
         */
        public EntryType getType() {

            return m_type;
        }

        /**
         * Gets the mapping parameter value.
         *
         * @return the mapping parameter value
         *
         */
        public String getValue() {

            return m_value;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return "" + m_type + ":" + m_value;
        }
    }

    /**
     * Enum representing the type of a single configuration entry in a geomapping configuration.
     */
    public enum EntryType {
        /** Take value from a content field. */
        field,

        /** Take value from another resource, the link to which is contained in a content field. */
        link
    }

    /** The list of configuration entries. */
    private List<Entry> m_entries = new ArrayList<>();

    /**
     * Creates a configuration from a list of configuration entries.
     *
     * @param configEntries the configuration entries.
     */
    public CmsGeoMappingConfiguration(List<Entry> configEntries) {

        m_entries = new ArrayList<>(configEntries);
    }

    /**
     * Gets the configuration entries.
     *
     * @return the list of configuration entries
     */
    public List<Entry> getEntries() {

        return Collections.unmodifiableList(m_entries);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "CmsGeoMappingConfiguration:" + m_entries.toString();
    }

}

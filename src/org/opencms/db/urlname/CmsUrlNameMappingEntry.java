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

package org.opencms.db.urlname;

import org.opencms.util.CmsUUID;

import java.util.Comparator;

/**
 * An URL name mapping entry.<p>
 *
 * @since 8.0.0
 */
public class CmsUrlNameMappingEntry {

    /**
     * Class for comparing URL name mapping entries by date.<p>
     **/
    public static class DateComparator implements Comparator<CmsUrlNameMappingEntry> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(CmsUrlNameMappingEntry o1, CmsUrlNameMappingEntry o2) {

            long date1 = o1.m_dateChanged;
            long date2 = o2.m_dateChanged;
            if (date1 < date2) {
                return -1;
            } else if (date2 < date1) {
                return +1;
            } else {
                return o1.m_name.compareTo(o2.m_name);
            }
        }

    }

    /** The state for mapping entries which have not been published. */
    public static final int MAPPING_STATUS_NEW = 0;

    /** The state for mapping entries which have been published. */
    public static final int MAPPING_STATUS_PUBLISHED = 1;

    /** State which indicates that all previous mappings should be replace on publish. */
    public static final int MAPPING_STATUS_REPLACE_ON_PUBLISH = 2;

    /** State which indicates that all previous mappings have been replaced on publish. */
    public static final int MAPPING_STATUS_REPLACE_ON_PUBLISH_PUBLISHED = 3;

    /** The date on which the mapping entry was last changed. */
    protected long m_dateChanged;

    /** The locale of the mapping. */
    protected String m_locale;

    /** The name to which the mapping entry belongs. */
    protected String m_name;

    /** The state of the mapping entry. */
    protected int m_state;

    /** The structure id to which the name is mapped. */
    protected CmsUUID m_structureId;

    /**
     * Creates a new URL name mapping entry.<p>
     *
     * @param name the URL name
     * @param structureId the id to which the name is mapped
     * @param state the state of the entry
     * @param dateChanged the date of the entry's last change
     * @param locale the locale of the mapping
     */
    public CmsUrlNameMappingEntry(String name, CmsUUID structureId, int state, long dateChanged, String locale) {

        m_name = name;
        m_structureId = structureId;
        m_state = state;
        m_dateChanged = dateChanged;
        m_locale = locale;

    }

    /**
     * Returns the date at which the mapping was last changed as a long.<p>
     *
     * @return the date at which the mapping was last changed
     *
     */
    public long getDateChanged() {

        return m_dateChanged;
    }

    /**
     * Returns the locale of the mapping entry.<p>
     *
     * @return the locale of the mapping entry
     */
    public String getLocale() {

        return m_locale;
    }

    /**
     * Returns the name to which the mapping belongs.<p>
     *
     * @return the name to which the mapping belongs
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the state of the mapping entry.<p>
     *
     * @return the state of the mapping entry
     */
    public int getState() {

        return m_state;
    }

    /**
     * Returns the structure id which is mapped to the name.<p>
     *
     * @return the structure id which is mapped to the name
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer buf = new StringBuffer();
        buf.append("[");
        buf.append(CmsUrlNameMappingEntry.class.getSimpleName());
        buf.append(": ");
        buf.append(m_name);
        buf.append(",  ");
        buf.append(m_structureId.toString());
        buf.append(", ");
        buf.append(m_state);
        buf.append(", ");
        buf.append(m_dateChanged);
        buf.append(", ");
        buf.append(m_locale);
        buf.append("]");
        return buf.toString();
    }

}

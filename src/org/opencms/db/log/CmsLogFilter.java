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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db.log;

import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * A filter to retrieve log entries.<p>
 *
 * @since 8.0.0
 */
public final class CmsLogFilter implements Cloneable {

    /** To filter all. */
    public static final CmsLogFilter ALL = new CmsLogFilter();

    /** The starting date to filter. */
    private long m_dateFrom;

    /** The end date to filter. */
    private long m_dateTo = Long.MAX_VALUE;

    /** The types to exclude. */
    private Set<CmsLogEntryType> m_excludeTypes = new HashSet<CmsLogEntryType>();

    /** The types to include. */
    private Set<CmsLogEntryType> m_includeTypes = new HashSet<CmsLogEntryType>();

    /** The structure id of the resource to filter. */
    private CmsUUID m_structureId;

    /** The user id to filter. */
    private CmsUUID m_userId;

    /**
     * Private constructor.<p>
     */
    private CmsLogFilter() {

        // empty
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {

        CmsLogFilter filter = new CmsLogFilter();
        filter.m_structureId = m_structureId;
        filter.m_includeTypes = new HashSet<CmsLogEntryType>(m_includeTypes);
        filter.m_excludeTypes = new HashSet<CmsLogEntryType>(m_excludeTypes);
        filter.m_userId = m_userId;
        filter.m_dateFrom = m_dateFrom;
        filter.m_dateTo = m_dateTo;
        return filter;
    }

    /**
     * Returns an extended filter with the given type restriction.<p>
     *
     * @param type the relation type to exclude
     *
     * @return an extended filter with the given type restriction
     */
    public CmsLogFilter excludeType(CmsLogEntryType type) {

        CmsLogFilter filter = (CmsLogFilter)clone();
        filter.m_excludeTypes.add(type);
        return filter;
    }

    /**
     * Returns an extended filter with the starting date restriction.<p>
     *
     * @param from the starting date to filter
     *
     * @return an extended filter with the starting date restriction
     */
    public CmsLogFilter filterFrom(long from) {

        CmsLogFilter filter = (CmsLogFilter)clone();
        filter.m_dateFrom = from;
        return filter;
    }

    /**
     * Returns an extended filter with the given resource restriction.<p>
     *
     * @param structureId the structure id to filter
     *
     * @return an extended filter with the given resource restriction
     */
    public CmsLogFilter filterResource(CmsUUID structureId) {

        CmsLogFilter filter = (CmsLogFilter)clone();
        filter.m_structureId = structureId;
        return filter;
    }

    /**
     * Returns an extended filter with the end date restriction.<p>
     *
     * @param to the end date to filter
     *
     * @return an extended filter with the end date restriction
     */
    public CmsLogFilter filterTo(long to) {

        CmsLogFilter filter = (CmsLogFilter)clone();
        filter.m_dateTo = to;
        return filter;
    }

    /**
     * Returns an extended filter with the given user ID restriction.<p>
     *
     * @param userId the user ID to filter
     *
     * @return an extended filter with the given user ID restriction
     */
    public CmsLogFilter filterUser(CmsUUID userId) {

        CmsLogFilter filter = (CmsLogFilter)clone();
        filter.m_userId = userId;
        return filter;
    }

    /**
     * Returns the starting date restriction.<p>
     *
     * @return the starting date restriction
     */
    public long getDateFrom() {

        return m_dateFrom;
    }

    /**
     * Returns the end date restriction.<p>
     *
     * @return the end date restriction
     */
    public long getDateTo() {

        return m_dateTo;
    }

    /**
     * Returns the types to exclude.<p>
     *
     * @return the types to exclude
     */
    public Set<CmsLogEntryType> getExcludeTypes() {

        return Collections.unmodifiableSet(m_excludeTypes);
    }

    /**
     * Returns the types to include.<p>
     *
     * @return the types to include
     */
    public Set<CmsLogEntryType> getIncludeTypes() {

        return Collections.unmodifiableSet(m_includeTypes);
    }

    /**
     * Returns the structure Id of the resource to filter.<p>
     *
     * @return the structure Id of the resource to filter
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Returns the user ID restriction.<p>
     *
     * @return the user ID restriction
     */
    public CmsUUID getUserId() {

        return m_userId;
    }

    /**
     * Returns an extended filter with the given type restriction.<p>
     *
     * @param type the relation type to include
     *
     * @return an extended filter with the given type restriction
     */
    public CmsLogFilter includeType(CmsLogEntryType type) {

        CmsLogFilter filter = (CmsLogFilter)clone();
        filter.m_includeTypes.add(type);
        return filter;
    }

    /**
     * Returns <code>true</code> if the given log entry type matches this filter.<p>
     *
     * @param type the log entry type to test
     *
     * @return <code>true</code> if the given log entry type matches this filter
     */
    public boolean matchType(CmsLogEntryType type) {

        if (m_excludeTypes.contains(type)) {
            return false;
        }
        if (m_includeTypes.isEmpty()) {
            return true;
        }
        return m_includeTypes.contains(type);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer str = new StringBuffer(128);
        str.append("[");
        str.append("resource").append("=").append(m_structureId).append(", ");
        str.append("user").append("=").append(m_userId).append(", ");
        str.append("from").append("=").append(new Date(m_dateFrom)).append(", ");
        str.append("to").append("=").append(new Date(m_dateTo)).append(", ");
        str.append("includeTypes").append("=").append(m_includeTypes);
        str.append("excludeTypes").append("=").append(m_excludeTypes);
        str.append("]");
        return str.toString();
    }
}

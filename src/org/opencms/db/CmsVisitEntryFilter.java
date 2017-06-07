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

package org.opencms.db;

import org.opencms.util.CmsUUID;

import java.util.Date;

/**
 * A filter which contains criteria for reading {@link CmsVisitEntry} instances from the database.<p>
 *
 * @since 8.0.0
 */
public final class CmsVisitEntryFilter implements Cloneable {

    /** To filter all. */
    public static final CmsVisitEntryFilter ALL = new CmsVisitEntryFilter();

    /** The starting date to filter. */
    private long m_dateFrom;

    /** The end date to filter. */
    private long m_dateTo = Long.MAX_VALUE;

    /** The structure id of the resource to filter. */
    private CmsUUID m_structureId;

    /** The user id to filter. */
    private CmsUUID m_userId;

    /**
     * Private constructor.<p>
     */
    private CmsVisitEntryFilter() {

        // empty
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {

        CmsVisitEntryFilter filter = new CmsVisitEntryFilter();
        filter.m_structureId = m_structureId;
        filter.m_userId = m_userId;
        filter.m_dateFrom = m_dateFrom;
        filter.m_dateTo = m_dateTo;
        return filter;
    }

    /**
     * Returns an extended filter with the starting date restriction.<p>
     *
     * @param from the starting date to filter
     *
     * @return an extended filter with the starting date restriction
     */
    public CmsVisitEntryFilter filterFrom(long from) {

        CmsVisitEntryFilter filter = (CmsVisitEntryFilter)clone();
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
    public CmsVisitEntryFilter filterResource(CmsUUID structureId) {

        CmsVisitEntryFilter filter = (CmsVisitEntryFilter)clone();
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
    public CmsVisitEntryFilter filterTo(long to) {

        CmsVisitEntryFilter filter = (CmsVisitEntryFilter)clone();
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
    public CmsVisitEntryFilter filterUser(CmsUUID userId) {

        CmsVisitEntryFilter filter = (CmsVisitEntryFilter)clone();
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
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer str = new StringBuffer(128);
        str.append("[");
        str.append("resource").append("=").append(m_structureId).append(", ");
        str.append("user").append("=").append(m_userId).append(", ");
        str.append("from").append("=").append(new Date(m_dateFrom)).append(", ");
        str.append("to").append("=").append(new Date(m_dateTo));
        str.append("]");
        return str.toString();
    }
}

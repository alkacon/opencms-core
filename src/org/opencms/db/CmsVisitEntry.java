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
 * A bean which represents a user having visited a page.<p>
 *
 * @since 8.0.0
 */
public class CmsVisitEntry {

    /** The entry's date in milliseconds. */
    private final long m_date;

    /** The structure id. */
    private final CmsUUID m_structureId;

    /** The user id. */
    private final CmsUUID m_userId;

    /**
     * Public constructor, will use the current time for time stamp.<p>
     *
     * @param dbc the current database context with the current user
     * @param structureId the structure id
     */
    public CmsVisitEntry(CmsDbContext dbc, CmsUUID structureId) {

        m_userId = dbc.currentUser().getId();
        m_date = System.currentTimeMillis();
        m_structureId = structureId;
    }

    /**
     * Public constructor.<p>
     *
     * @param userId the user id
     * @param date the date in milliseconds
     * @param structureId the structure id
     */
    public CmsVisitEntry(CmsUUID userId, long date, CmsUUID structureId) {

        m_userId = userId;
        m_date = date;
        m_structureId = structureId;
    }

    /**
     * Returns the date.<p>
     *
     * @return the date
     */
    public long getDate() {

        return m_date;
    }

    /**
     * Returns the structure id.<p>
     *
     * @return the structure id
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Returns the user id.<p>
     *
     * @return the user id
     */
    public CmsUUID getUserId() {

        return m_userId;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[").append(getClass().getName()).append(":");
        sb.append("user=").append(m_userId).append(",");
        sb.append("date=").append(new Date(m_date)).append(",");
        sb.append("structure=").append(m_structureId);
        return sb.append("]").toString();
    }

}

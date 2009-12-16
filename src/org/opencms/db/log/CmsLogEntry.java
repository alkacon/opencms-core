/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/log/CmsLogEntry.java,v $
 * Date   : $Date: 2009/12/16 15:06:42 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.db.CmsDbContext;
import org.opencms.util.CmsUUID;

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * Wrapper class for a DB log entry.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 7.9.2
 */
public class CmsLogEntry {

    /** DB context attribute name constant. */
    public static final String ATTR_LOG_ENTRY = "ATTR_LOG_ENTRY";

    /** The additional data. */
    private final String[] m_data;

    /** The entry's date in millisecs. */
    private final long m_date;

    /** The resource id. */
    private final CmsUUID m_resourceId;

    /** The type. */
    private final CmsLogEntryType m_type;

    /** The user id. */
    private final CmsUUID m_userId;

    /**
     * Public constructor, will use the current time for time stamp.<p>
     * 
     * @param dbc the current database context with the current user
     * @param resourceId the resource id
     * @param type the entry type
     * @param data the additional data to be parsed as a map
     */
    public CmsLogEntry(CmsDbContext dbc, CmsUUID resourceId, CmsLogEntryType type, String[] data) {

        m_userId = dbc.currentUser().getId();
        m_date = System.currentTimeMillis();
        m_resourceId = resourceId;
        m_data = (data == null ? new String[0] : data);
        m_type = type;
    }

    /**
     * Public constructor.<p>
     * 
     * @param userId the user id
     * @param date the date in millisecs
     * @param resourceId the resource id
     * @param type the entry type
     * @param data the optional additional data
     */
    public CmsLogEntry(CmsUUID userId, long date, CmsUUID resourceId, CmsLogEntryType type, String[] data) {

        m_userId = userId;
        m_date = date;
        m_resourceId = resourceId;
        m_data = (data == null ? new String[0] : data);
        m_type = type;
    }

    /**
     * Returns the additional data.<p>
     *
     * @return the additional data
     */
    public String[] getData() {

        return m_data;
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
     * Returns the formatted details for this log entry.<p>
     * 
     * @param locale the locale
     * 
     * @return the formatted details for this log entry
     */
    public String getDetails(Locale locale) {

        return Messages.get().getBundle(locale).key(m_type.getDetailKey(), m_data);
    }

    /**
     * Returns the resource id.<p>
     *
     * @return the resource id
     */
    public CmsUUID getResourceId() {

        return m_resourceId;
    }

    /**
     * Returns the type.<p>
     *
     * @return the type
     */
    public CmsLogEntryType getType() {

        return m_type;
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
        sb.append("resource=").append(m_resourceId).append(",");
        sb.append("type=").append(m_type.getLocalizedName(Locale.ENGLISH)).append(",");
        sb.append("data=").append(Arrays.toString(m_data)).append(",");
        return sb.append("]").toString();
    }
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workflow/Attic/CmsTaskLog.java,v $
 * Date   : $Date: 2005/06/12 11:18:21 $
 * Version: $Revision: 1.10 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workflow;

import org.opencms.main.I_CmsConstants;
import org.opencms.util.CmsUUID;

/**
 * Describes an OpenCms task log entry.
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.10 $ 
 */
public class CmsTaskLog {

    /** The comment for this task log. */
    private String m_comment;

    /** The id of the task log. */
    private int m_id = I_CmsConstants.C_UNKNOWN_ID;

    /** The start time for this task log. */
    private java.sql.Timestamp m_startTime;

    /** The type for this task log, 0=SystemLog, 1=UserLog. */
    private int m_type;

    /** The id of the corresponding user. */
    private CmsUUID m_userId;

    /**
     * Creates a new CmsTaskLog object.<p>
     * 
     * @param id the id of the task log
     * @param comment the comment for this task log
     * @param userId the id Of the corresponding user
     * @param starttime the start time for this task log
     * @param type the type for this task log
     */
    public CmsTaskLog(int id, String comment, CmsUUID userId, java.sql.Timestamp starttime, int type) {

        m_id = id;
        m_comment = comment;
        m_userId = userId;
        m_startTime = starttime;
        m_type = type;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsTaskLog) {
            return ((CmsTaskLog)obj).m_id == m_id;
        }
        return false;
    }

    /**
     * Returns the comment of this task log entry.<p>
     * 
     * @return the comment of this task log entry
     */
    public String getComment() {

        return m_comment;
    }

    /**
     * Returns the id of this task log entry.<p>
     * 
     * @return the id of this task log entry
     */
    public int getId() {

        return m_id;
    }

    /**
     * Returns the start time of this task log entry.<p>
     * 
     * @return the start time of this task log entry
     */
    public java.sql.Timestamp getStartTime() {

        return m_startTime;
    }

    /**
     * Returns the type of this task log entry.<p>
     *  
     * @return the type of this task log entry
     */
    public int getType() {

        return m_type;
    }

    /**
     * Returns the user id of this task log entry.<p>
     * 
     * @return the user id of this task log entry
     */
    public CmsUUID getUser() {

        return m_userId;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return (new Integer(m_id)).hashCode();
    }

    /**
     * Sets the comment of this task log entry.<p>
     * 
     * @param value the comment to set
     */
    public void setComment(String value) {

        m_comment = value;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer result = new StringBuffer();
        result.append("[TaskLog]");
        result.append(" id:");
        result.append(getId());
        result.append(" comment:");
        result.append(getComment());
        result.append(" starttime:");
        result.append(getStartTime());
        result.append(" user:");
        result.append(getUser());
        if (getType() == I_CmsConstants.C_TASKLOG_SYSTEM) {
            result.append(" type:system");
        } else {
            result.append(" type:user");
        }
        return result.toString();
    }
}

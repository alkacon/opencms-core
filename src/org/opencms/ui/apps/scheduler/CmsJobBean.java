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

package org.opencms.ui.apps.scheduler;

import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.util.CmsUUID;

import java.util.Date;

/**
 * Don't use CmsScheduledJobInfo directly, so we don't need to change it if we want to change how the values are
 * rendered, and having only the fields we want displayed in the table makes it easier to understand.
 */
public class CmsJobBean {

    /** Internal id. */
    private CmsUUID m_id = new CmsUUID();

    /** The wrapped scheduled job info. */
    private CmsScheduledJobInfo m_jobInfo;

    /**
     * Creates a new instance.<p>
     *
     * @param info the scheduled job info to wrap
     */
    public CmsJobBean(CmsScheduledJobInfo info) {
        m_jobInfo = info;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj instanceof CmsJobBean) {
            return ((CmsJobBean)obj).m_id.equals(m_id);
        }
        return false;

    }

    /**
     * Gets the class name for the job.<p>
     *
     * @return the class name
     */
    public String getClassName() {

        return m_jobInfo.getClassName();
    }

    /**
     * Gets the scheduled job.<p>
     *
     * @return the scheduled job
     */
    public CmsScheduledJobInfo getJob() {

        return m_jobInfo;
    }

    /**
     * Gets the last execution date.<p>
     *
     * @return the last execution date
     */
    public Date getLastExecution() {

        return m_jobInfo.getExecutionTimePrevious();
    }

    /**
     * Gets the job name.<p>
     *
     * @return the job name
     */
    public String getName() {

        return m_jobInfo.getJobName();
    }

    /**
     * Gets the next execution date.<p>
     *
     * @return the next execution date
     */
    public Date getNextExecution() {

        return m_jobInfo.getExecutionTimeNext();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_id.hashCode();
    }

}

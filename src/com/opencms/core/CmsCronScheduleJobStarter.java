/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsCronScheduleJobStarter.java,v $
* Date   : $Date: 2003/09/16 12:06:10 $
* Version: $Revision: 1.6 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.core;

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.main.OpenCmsCore;


import java.util.Calendar;

/**
 * This class starts all needed jobs for the current time.
 */
public class CmsCronScheduleJobStarter extends Thread {

    /** The crontable to use */
    private CmsCronTable m_table;

    /** The time of this run */
    private Calendar m_thisRun;

    /** The time of the last run */
    private Calendar m_lastRun;

    /** OpenCms to get access to the system */
    private OpenCmsCore m_opencms;

    /**
     * Creates a new instance of CmsCronScheduleJobStarter.
     * @param opencms to get access to A_OpenCms.
     * @param table the CmsCronTable with all entries.
     * @param thisRun the start time of this run.
     * @param lastRun the last start time.
     */
    public CmsCronScheduleJobStarter(OpenCmsCore opencms, CmsCronTable table, Calendar thisRun, Calendar lastRun) {
        m_table = table;
        m_lastRun = lastRun;
        m_thisRun = thisRun;
        m_opencms = opencms;
    }

    /**
     * The run method of this thread tests all entries, if they should be started.
     * If so it tries to start the entry via A_OpenCms.startScheduleJob()
     */
    public void run() {
        for (int i = 0; i < m_table.size(); i++) {
            if (m_table.get(i).check(m_lastRun, m_thisRun)) {
                // we have to start the job for this entry
                if (OpenCms.isLogging(CmsLog.CHANNEL_CRON, CmsLog.LEVEL_WARN)) {
                    OpenCms.log(CmsLog.CHANNEL_CRON, CmsLog.LEVEL_WARN, "Starting job for " + m_table.get(i));
                }
                m_opencms.startScheduleJob(m_table.get(i));
            }
        }
    }
}

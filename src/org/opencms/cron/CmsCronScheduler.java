/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/cron/Attic/CmsCronScheduler.java,v $
 * Date   : $Date: 2003/10/29 13:00:42 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.cron;

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.main.OpenCmsCore;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * A deamon- thread that automatically executes scheduled Cms cron- jobs.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com) 
 * @version $Revision: 1.1 $ $Date: 2003/10/29 13:00:42 $
 * @since 5.1.12
 */
public class CmsCronScheduler extends Thread {

    /** The crontable to use */
    private CmsCronTable m_table;

    /** The A_OpenCms to get access to the system */
    private OpenCmsCore m_opencms;

    /** Flag to indicate if OpenCms has already been shut down */
    private boolean m_destroyed = false;

    /**
     * Constructs a new scheduler.<p>
     * 
     * @param opencms to get access to A_OpenCms
     * @param table the CmsCronTable with all CmsCronEntry's to launch.
     */
    public CmsCronScheduler(OpenCmsCore opencms, CmsCronTable table) {
        super("OpenCms: CronScheduler");
        // store the crontable
        m_table = table;
        // store the OpenCms to get access to the system
        m_opencms = opencms;

        // set to deamon thread - so java will stop correctly
        setDaemon(true);
        // start the thread
        start();
    }

    /**
     * The run-method of this thread awakes every minute to launch jobs at issue.<p>
     */
    public void run() {
        Calendar lastRun = new GregorianCalendar();
        Calendar thisRun;
        CmsCronScheduleJobStarter jobStarter;
        while (!m_destroyed) { // do this as long as OpenCms runs
            try {
                Calendar tmp = new GregorianCalendar(lastRun.get(Calendar.YEAR), lastRun.get(Calendar.MONTH), lastRun.get(Calendar.DATE), lastRun.get(Calendar.HOUR_OF_DAY), lastRun.get(Calendar.MINUTE) + 1, 0);
                long sleeptime = tmp.getTime().getTime() - new GregorianCalendar().getTime().getTime();
                if (sleeptime > 0) {
                    // sleep til next minute plus ten seconds to get to the next minute
                    sleep(sleeptime + 10000);
                }
            } catch (InterruptedException exc) {
                // ignore this exception - we are interrupted
            }
            if (!m_destroyed) {
                // if not destroyed, read the current values for the crontable from the system
                m_opencms.updateCronTable();
                thisRun = new GregorianCalendar();
                jobStarter = new CmsCronScheduleJobStarter(m_opencms, m_table, thisRun, lastRun);
                jobStarter.start();
                lastRun = thisRun;
            }
        }
    }

    /** 
     * Shuts down this instance of the CronScheduler Thread.<p>
     */
    public void shutDown() {
        m_destroyed = true;
        interrupt();
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info("[" + this.getClass().getName() + "] destroyed!");
        }
    }
}

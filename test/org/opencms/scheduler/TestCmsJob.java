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

package org.opencms.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

/**
 * Class that implements the Job interface.<p>
 */
public class TestCmsJob implements Job {

    /** Count of individual thread. */
    private int m_myCount;

    /** Current count. */
    static int m_count;

    /** Currently running instances. */
    static int m_running;

    /**
     * Default constructor.<p>
     */
    public TestCmsJob() {

        TestCmsJob.m_count++;
        m_myCount = TestCmsJob.m_count;
    }

    /**
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public void execute(JobExecutionContext context) {

        System.out.println(
            getClass().getName() + " " + m_myCount + " is starting (running: " + TestCmsJob.m_running + ").");
        TestCmsJob.m_running++;
        try {
            Thread.sleep(1000 + (long)(4000.0 * Math.random()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TestCmsJob.m_running--;
        System.out.println(
            getClass().getName() + " " + m_myCount + " is finished (running: " + TestCmsJob.m_running + ").");
    }
}
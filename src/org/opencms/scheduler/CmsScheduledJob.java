/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/scheduler/Attic/CmsScheduledJob.java,v $
 * Date   : $Date: 2004/07/05 15:35:12 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.scheduler;

import org.opencms.main.OpenCms;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

/**
 * Launches a single scheduled OpenCms job in a thread of the Scheduler.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 *  
 * @version $Revision: 1.1 $
 * @since 5.3
 */
public class CmsScheduledJob implements Job {

    /** Key for the scheduled entry in the job data map. */
    public static final String C_SCHEDULER_JOB_ENTRY = "org.opencms.scheduler.CmsSchedulerEntry";
    
    /**
     * Empty constructor.<p> 
     */
    public CmsScheduledJob() {
        // noop
    }
    
    /**
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public void execute(JobExecutionContext context) {
        
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        CmsSchedulerEntry entry = (CmsSchedulerEntry)jobData.get(C_SCHEDULER_JOB_ENTRY);
        
        if (entry == null) {
            OpenCms.getLog(this).error("Scheduler: Invalid job data for " + context.getJobDetail().getFullName());
            // can not continue
            return;
        }
        
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Scheduler: Starting job for class " + entry.getClassName());
        }
    
        I_CmsSchedulerJob job = entry.getJobInstance();
        
        if (job != null) {
            try {
                // launch the job
                String result = job.launch(null, entry.getParameters());
                if (result != null) {
                    OpenCms.getLog(this).info(result);
                }
            } catch (Throwable t) {
                OpenCms.getLog(this).error("Scheduler: Error launching job for class " + entry.getClassName(), t);
            }
        }
        
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Scheduler: Finished job for class " + entry.getClassName());
        }
    }
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/scheduler/CmsScheduleManager.java,v $
 * Date   : $Date: 2004/07/08 15:24:16 $
 * Version: $Revision: 1.4 $
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

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Manages the OpenCms scheduled jobs.<p>
 * 
 * Please see the documentation of the class {@link org.opencms.scheduler.CmsScheduledJobInfo} 
 * for a full description of the OpenCms scheduling capabilities.<p>
 * 
 * The OpenCms scheduler implementation internally uses the
 * <a href="http://www.opensymphony.com/quartz/">Quartz scheduler</a> from
 * the <a href="http://www.opensymphony.com/">OpenSymphony project</a>.<p>
 * 
 * This manager class implements the <code>org.quartz.Job</code> interface
 * and wraps all calls to the {@link org.opencms.scheduler.I_CmsScheduledJob} implementing 
 * classes.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 *  
 * @version $Revision: 1.4 $
 * @since 5.3.6
 * 
 * @see org.opencms.scheduler.CmsScheduledJobInfo
 */
public class CmsScheduleManager implements Job {

    /** Key for the scheduled job description in the job data map. */
    public static final String C_SCHEDULER_JOB_INFO = "org.opencms.scheduler.CmsScheduledJobInfo";

    /** The Admin context used for creation of users for the individual jobs. */
    private static CmsObject m_adminCms;

    /** The list of job entries from the configuration. */
    private List m_configuredJobs;

    /** The number of scheduled jobs. */
    private int m_jobCount;

    /** The list of scheduled jobs. */
    private List m_jobs;

    /** The initialized scheduler. */
    private Scheduler m_scheduler;

    /**
     * Default constructor for the scheduler manager, 
     * used only when a new job is scheduled.<p>
     */
    public CmsScheduleManager() {

        // important: this constructor is always called when a new job is 
        // generated, so it _must_ remain empty
    }

    /**
     * Used by the configuration to create a new Scheduler during system startup.<p>
     * 
     * @param configuredJobs the jobs from the configuration
     */
    public CmsScheduleManager(List configuredJobs) {

        m_configuredJobs = configuredJobs;
        int size = 0;
        if (m_configuredJobs != null) {
            size = m_configuredJobs.size();
        }
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                ". Scheduler config     : scheduler created with " + size + " jobs");
        }
    }

    /**
     * Implementation of the Quartz job interface.<p>
     * 
     * The architecture is that this scheduler manager generates
     * a new (empty) instance of itself for every OpenCms job scheduled with Quartz. 
     * When the Quartz job is executed, the configured 
     * implementaion of {@link I_CmsScheduledJob} will be called from this method.<p>
     * 
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public void execute(JobExecutionContext context) {

        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        CmsScheduledJobInfo jobInfo = (CmsScheduledJobInfo)jobData.get(C_SCHEDULER_JOB_INFO);

        if (jobInfo == null) {
            OpenCms.getLog(this).error("Scheduler: Invalid job data for " + context.getJobDetail().getFullName());
            // can not continue
            return;
        }

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Scheduler: Starting job '" + jobInfo.getJobName() + "'");
        }

        I_CmsScheduledJob job = jobInfo.getJobInstance();

        if (job != null) {
            try {
                // launch the job
                CmsObject cms = null;

                // only simple test cases might not have admin cms available
                if (m_adminCms != null) {
                    // generate a CmsObject for the job context                    
                    cms = OpenCms.initCmsObject(m_adminCms, jobInfo.getContextInfo());
                }

                String result = job.launch(cms, jobInfo.getParameters());
                if ((result != null) && OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info(jobInfo.getJobName() + ": " + result);
                }
            } catch (Throwable t) {
                OpenCms.getLog(this).error("Scheduler: Error launching job '" + jobInfo.getJobName() + "'", t);
            }
        }

        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Scheduler: Finished job '" + jobInfo.getJobName() + "'");
        }
    }

    /**
     * Returns the currently scheduled job descriptions in an unmodifiable list.<p>
     *
     * @return the currently scheduled job descriptions in an unmodifiable list
     */
    public List getJobs() {

        return Collections.unmodifiableList(m_jobs);
    }

    /**
     * Initializes the OpenCms scheduler.<p> 
     * 
     * @param adminCms an OpenCms context object that must have been initialized with "Admin" permissions
     * 
     * @throws CmsException if something goes wrong
     */
    public void initialize(CmsObject adminCms) throws CmsException {

        if ((OpenCms.getRunLevel() > 1) || (adminCms != null)) {
            // simple unit tests will have runlevel 1 and no CmsObject
            if ((adminCms == null) || !adminCms.isAdmin()) {
                throw new CmsSecurityException(CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
            }
        }

        // set job counter
        m_jobCount = 0;

        // the list of job entries
        m_jobs = new ArrayList();

        // save the admin cms
        m_adminCms = adminCms;

        // Quartz scheduler settings
        Properties properties = new Properties();
        properties.put(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, "OpenCmsScheduler");
        properties.put(StdSchedulerFactory.PROP_SCHED_THREAD_NAME, "OpenCms: Scheduler");
        properties.put(StdSchedulerFactory.PROP_SCHED_RMI_EXPORT, String.valueOf(false));
        properties.put(StdSchedulerFactory.PROP_SCHED_RMI_PROXY, String.valueOf(false));
        properties.put(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, CmsSchedulerThreadPool.class.getName());
        properties.put(StdSchedulerFactory.PROP_JOB_STORE_CLASS, "org.quartz.simpl.RAMJobStore");

        try {
            // initilize the Quartz scheduler
            SchedulerFactory schedulerFactory = new StdSchedulerFactory(properties);
            m_scheduler = schedulerFactory.getScheduler();
        } catch (Exception e) {
            OpenCms.getLog(this).error("Could not initialize the scheduler", e);
            // can not continue
            m_scheduler = null;
            return;
        }

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Scheduler config     : scheduler initialized");
        }

        if (m_configuredJobs != null) {
            // add all jobs from the system configuration
            for (int i = 0; i < m_configuredJobs.size(); i++) {
                scheduleJob(adminCms, (CmsScheduledJobInfo)m_configuredJobs.get(i));
            }
        }

        try {
            // start the scheduler
            m_scheduler.start();
        } catch (Exception e) {
            OpenCms.getLog(this).error("Could not start the scheduler", e);
            // can not continue
            m_scheduler = null;
            return;
        }

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Scheduler config     : scheduler started");
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Scheduler config     : finished");
        }
    }

    /**
     * Adds a new job to the scheduler.<p>
     *  
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     * @param jobInfo the job info describing the job to schedule
     * 
     * @throws CmsException if something goes wrong
     */
    public void scheduleJob(CmsObject cms, CmsScheduledJobInfo jobInfo) throws CmsException {

        if (OpenCms.getRunLevel() > 1) {
            // simple unit tests will have runlevel 1 and no CmsObject
            if ((cms == null) || !cms.isAdmin()) {
                throw new CmsSecurityException(CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
            }
        }

        if ((jobInfo == null) || (jobInfo.getClassName() == null)) {
            // prevent NPE
            OpenCms.getLog(this).error("Scheduler: Invalid job configuation provided");
            // can not continue
            return;
        }

        if (m_scheduler == null) {
            OpenCms.getLog(this).error(
                "Scheduler: No scheduler available to schedule job '" + jobInfo.getJobName() + "'");
            // can not continue
            return;
        }

        try {
            Class.forName(jobInfo.getClassName());
        } catch (ClassNotFoundException e) {
            OpenCms.getLog(this).error("Scheduler: Class not found '" + jobInfo.getClassName() + "'", e);
            // can not continue
            return;
        }

        // increase job count
        m_jobCount++;

        // generate Quartz job detail
        JobDetail jobDetail = new JobDetail(
            "cmsJob" + m_jobCount, 
            Scheduler.DEFAULT_GROUP, 
            CmsScheduleManager.class);

        // generate Quartz job trigger
        CronTrigger trigger = new CronTrigger(
            "cmsTrigger" + m_jobCount, 
            Scheduler.DEFAULT_GROUP);

        try {
            trigger.setCronExpression(jobInfo.getCronExpression());
        } catch (ParseException e) {
            OpenCms.getLog(this).error(
                "Scheduler: Bad cron expression for job '"
                    + jobInfo.getJobName()
                    + "': ["
                    + jobInfo.getCronExpression()
                    + "]",
                e);
            // can not continue
            return;
        }

        // now set the job data
        JobDataMap jobData = new JobDataMap();
        jobData.put(CmsScheduleManager.C_SCHEDULER_JOB_INFO, jobInfo);
        jobDetail.setJobDataMap(jobData);

        // freeze the scheduled job configuration
        jobInfo.initConfiguration();

        try {
            m_scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            OpenCms.getLog(this).error(
                "Scheduler: Could not schedule job '"
                    + jobInfo.getJobName()
                    + "' for class '"
                    + jobInfo.getClassName()
                    + "'",
                e);
            // can not continue
            return;
        }

        // add the job to the list of configured jobs
        m_jobs.add(jobInfo);

        if (OpenCms.getLog(this).isInfoEnabled()) {
            OpenCms.getLog(this).info(
                "Scheduled job "
                    + m_jobs.size()
                    + " named '"
                    + jobInfo.getJobName()
                    + "' for class '"
                    + jobInfo.getClassName()
                    + "' with user "
                    + jobInfo.getContextInfo().getUserName());
        }
    }

    /** 
     * Shuts down this instance of the OpenCms scheduler manager.<p>
     */
    public void shutDown() {

        m_adminCms = null;

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                ". Shutting down        : " + this.getClass().getName() + " ... ok!");
        }

        if (m_scheduler != null) {
            try {
                m_scheduler.shutdown();
            } catch (SchedulerException e) {
                OpenCms.getLog(this).error("Scheduler: Problems shutting down", e);
            }
        }

        m_scheduler = null;
    }
}
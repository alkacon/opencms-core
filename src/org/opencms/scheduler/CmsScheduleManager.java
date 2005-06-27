/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/scheduler/CmsScheduleManager.java,v $
 * Date   : $Date: 2005/06/27 23:22:10 $
 * Version: $Revision: 1.24 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.scheduler;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;

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
 * @author Alexander Kandzior 
 *  
 * @version $Revision: 1.24 $ 
 * 
 * @since 6.0.0 
 * 
 * @see org.opencms.scheduler.CmsScheduledJobInfo
 */
public class CmsScheduleManager implements Job {

    /** Key for the scheduled job description in the job data map. */
    public static final String SCHEDULER_JOB_INFO = "org.opencms.scheduler.CmsScheduledJobInfo";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsScheduleManager.class);

    /** The Admin context used for creation of users for the individual jobs. */
    private static CmsObject m_adminCms;

    /** The list of job entries from the configuration. */
    private List m_configuredJobs;

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

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_SCHEDULER_CREATED_1, new Integer(size)));
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
        CmsScheduledJobInfo jobInfo = (CmsScheduledJobInfo)jobData.get(SCHEDULER_JOB_INFO);

        if (jobInfo == null) {
            LOG.error(Messages.get().key(Messages.LOG_INVALID_JOB_1, context.getJobDetail().getFullName()));
            // can not continue
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_JOB_STARTING_1, jobInfo.getJobName()));
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
                if (CmsStringUtil.isNotEmpty(result) && LOG.isInfoEnabled()) {
                    LOG.info(Messages.get().key(Messages.LOG_JOB_EXECUTION_OK_2, jobInfo.getJobName(), result));
                }
            } catch (Throwable t) {
                LOG.error(Messages.get().key(Messages.LOG_JOB_EXECUTION_ERROR_1, jobInfo.getJobName()), t);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_JOB_EXECUTED_1, jobInfo.getJobName()));
            Date nextExecution = jobInfo.getExecutionTimeNext();
            if (nextExecution != null) {
                LOG.info(Messages.get().key(Messages.LOG_JOB_NEXT_EXECUTION_2, jobInfo.getJobName(), nextExecution));
            }
        }
    }

    /**
     * Returns the currently scheduled job description identified by the given id.
     * 
     * @param id the job id
     * 
     * @return a job or <code>null</code> if not found
     */
    public CmsScheduledJobInfo getJob(String id) {

        Iterator it = m_jobs.iterator();
        while (it.hasNext()) {
            CmsScheduledJobInfo job = (CmsScheduledJobInfo)it.next();
            if (job.getId().equals(id)) {
                return job;
            }
        }
        // not found
        return null;
    }

    /**
     * Returns the currently scheduled job descriptions in an unmodifiable list.<p>
     *
     * The objects in the List are of type <code>{@link CmsScheduledJobInfo}</code>.<p>
     *
     * @return the currently scheduled job descriptions in an unmodifiable list
     */
    public List getJobs() {

        return Collections.unmodifiableList(m_jobs);
    }

    /**
     * Initializes the OpenCms scheduler.<p> 
     * 
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     * 
     * @throws CmsRoleViolationException if the user has insufficient role permissions
     */
    public synchronized void initialize(CmsObject cms) throws CmsRoleViolationException {

        if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_1_CORE_OBJECT) {
            // simple unit tests will have runlevel 1 and no CmsObject
            cms.checkRole(CmsRole.SCHEDULER_MANAGER);
        }

        // the list of job entries
        m_jobs = new ArrayList();

        // save the admin cms
        m_adminCms = cms;

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
            LOG.error(Messages.get().key(Messages.LOG_NO_SCHEDULER_0), e);
            // can not continue
            m_scheduler = null;
            return;
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_SCHEDULER_INITIALIZED_0));
        }

        if (m_configuredJobs != null) {
            // add all jobs from the system configuration
            for (int i = 0; i < m_configuredJobs.size(); i++) {
                try {
                    CmsScheduledJobInfo job = (CmsScheduledJobInfo)m_configuredJobs.get(i);
                    scheduleJob(cms, job);
                } catch (CmsSchedulerException e) {
                    // ignore this job, but keep scheduling the other jobs
                    // note: the log is has already been written
                }
            }
        }

        try {
            // start the scheduler
            m_scheduler.start();
        } catch (Exception e) {
            LOG.error(Messages.get().key(Messages.LOG_CANNOT_START_SCHEDULER_0), e);
            // can not continue
            m_scheduler = null;
            return;
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_SCHEDULER_STARTED_0));
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_SCHEDULER_CONFIG_FINISHED_0));
        }
    }

    /**
     * Adds a new job to the scheduler.<p>
     *  
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     * @param jobInfo the job info describing the job to schedule
     * 
     * @throws CmsRoleViolationException if the user has insufficient role permissions
     * @throws CmsSchedulerException if the job could not be scheduled for any reason
     */
    public synchronized void scheduleJob(CmsObject cms, CmsScheduledJobInfo jobInfo)
    throws CmsRoleViolationException, CmsSchedulerException {

        if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_1_CORE_OBJECT) {
            // simple unit tests will have runlevel 1 and no CmsObject
            cms.checkRole(CmsRole.SCHEDULER_MANAGER);
        }

        if ((jobInfo == null) || (jobInfo.getClassName() == null)) {
            // prevent NPE
            CmsMessageContainer message = Messages.get().container(Messages.ERR_INVALID_JOB_CONFIGURATION_0);
            LOG.error(message.key());
            // can not continue
            throw new CmsSchedulerException(message);
        }

        if (m_scheduler == null) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_NO_SCHEDULER_1, jobInfo.getJobName());
            LOG.error(message.key());
            // can not continue
            throw new CmsSchedulerException(message);
        }

        Class jobClass;
        try {
            jobClass = Class.forName(jobInfo.getClassName());
        } catch (ClassNotFoundException e) {
            // class does not exist
            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_JOB_CLASS_NOT_FOUND_1,
                jobInfo.getClassName());
            LOG.error(message.key());
            throw new CmsSchedulerException(message);
        }
        if (!I_CmsScheduledJob.class.isAssignableFrom(jobClass)) {
            // class does not implement required interface
            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_JOB_CLASS_BAD_INTERFACE_2,
                jobInfo.getClassName(),
                I_CmsScheduledJob.class.getName());
            LOG.error(message.key());
            throw new CmsSchedulerException(message);
        }

        String jobId = jobInfo.getId();
        boolean idCreated = false;
        if (jobId == null) {
            // generate a new job id
            CmsUUID jobUUID = new CmsUUID();
            jobId = "OpenCmsJob_".concat(jobUUID.toString());
            jobInfo.setId(jobId);
            idCreated = true;
        }

        // generate Quartz job trigger
        CronTrigger trigger = new CronTrigger(jobId, Scheduler.DEFAULT_GROUP);

        try {
            trigger.setCronExpression(jobInfo.getCronExpression());
        } catch (ParseException e) {
            if (idCreated) {
                jobInfo.setId(null);
            }
            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_BAD_CRON_EXPRESSION_2,
                jobInfo.getJobName(),
                jobInfo.getCronExpression());
            LOG.error(message.key());
            // can not continue
            throw new CmsSchedulerException(message);
        }

        CmsScheduledJobInfo oldJob = null;
        if (!idCreated) {
            // this job is already scheduled, remove the currently scheduled instance and keep the id     
            // important: since the new job may have errors, it's required to make sure the old job is only unscheduled 
            // if the new job info is o.k.
            oldJob = unscheduleJob(cms, jobId);
            if (oldJob == null) {
                CmsMessageContainer message = Messages.get().container(Messages.ERR_JOB_WITH_ID_DOES_NOT_EXIST_1, jobId);
                LOG.warn(message.key());
                // can not continue
                throw new CmsSchedulerException(message);
            }
            // open the job configuration (in case it has been frozen)
            jobInfo.setFrozen(false);
        }

        // only schedule jobs when they are marked as active
        if (jobInfo.isActive()) {

            // generate Quartz job detail
            JobDetail jobDetail = new JobDetail(jobInfo.getId(), Scheduler.DEFAULT_GROUP, CmsScheduleManager.class);

            // add the trigger to the job info
            jobInfo.setTrigger(trigger);

            // now set the job data
            JobDataMap jobData = new JobDataMap();
            jobData.put(CmsScheduleManager.SCHEDULER_JOB_INFO, jobInfo);
            jobDetail.setJobDataMap(jobData);

            // finally add the job to the Quartz scheduler
            try {
                m_scheduler.scheduleJob(jobDetail, trigger);
            } catch (Exception e) {
                if (idCreated) {
                    jobInfo.setId(null);
                }
                CmsMessageContainer message = Messages.get().container(
                    Messages.ERR_COULD_NOT_SCHEDULE_JOB_2,
                    jobInfo.getJobName(),
                    jobInfo.getClassName());
                if (oldJob != null) {
                    // make sure an old job is re-scheduled 
                    jobDetail = new JobDetail(oldJob.getId(), Scheduler.DEFAULT_GROUP, CmsScheduleManager.class);
                    try {
                        m_scheduler.scheduleJob(jobDetail, oldJob.getTrigger());
                        m_jobs.add(oldJob);
                    } catch (SchedulerException e2) {
                        // unable to re-schedule original job - not much we can do about this...
                        message = Messages.get().container(
                            Messages.ERR_COULD_NOT_RESCHEDULE_JOB_2,
                            jobInfo.getJobName(),
                            jobInfo.getClassName());
                    }
                }
                LOG.error(message.key());
                throw new CmsSchedulerException(message);
            }
        }

        // freeze the scheduled job configuration
        jobInfo.initConfiguration();

        // add the job to the list of configured jobs
        m_jobs.add(jobInfo);

        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().key(
                Messages.LOG_JOB_SCHEDULED_4,
                new Object[] {
                    new Integer(m_jobs.size()),
                    jobInfo.getJobName(),
                    jobInfo.getClassName(),
                    jobInfo.getContextInfo().getUserName()}));
            Date nextExecution = jobInfo.getExecutionTimeNext();
            if (nextExecution != null) {
                LOG.info(Messages.get().key(Messages.LOG_JOB_NEXT_EXECUTION_2, jobInfo.getJobName(), nextExecution));
            }
        }
    }

    /** 
     * Shuts down this instance of the OpenCms scheduler manager.<p>
     */
    public void shutDown() {

        m_adminCms = null;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_SHUTDOWN_1, this.getClass().getName()));
        }

        if (m_scheduler != null) {
            try {
                m_scheduler.shutdown();
            } catch (SchedulerException e) {
                LOG.error(Messages.get().key(Messages.LOG_SHUTDOWN_ERROR_0));
            }
        }

        m_scheduler = null;
    }

    /**
     * Removes a currently scheduled job from the scheduler.<p>
     *  
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     * @param jobId the id of the job to unschedule, obtained with <code>{@link CmsScheduledJobInfo#getId()}</code>
     * 
     * @return the <code>{@link CmsScheduledJobInfo}</code> of the sucessfully unscheduled job, 
     *      or <code>null</code> if the job could not be unscheduled
     * 
     * @throws CmsRoleViolationException if the user has insufficient role permissions
     */
    public synchronized CmsScheduledJobInfo unscheduleJob(CmsObject cms, String jobId) throws CmsRoleViolationException {

        if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_1_CORE_OBJECT) {
            // simple unit tests will have runlevel 1 and no CmsObject
            cms.checkRole(CmsRole.SCHEDULER_MANAGER);
        }

        CmsScheduledJobInfo jobInfo = null;
        if (m_jobs.size() > 0) {
            // try to remove the job from the OpenCms list of jobs
            for (int i = (m_jobs.size() - 1); i >= 0; i--) {
                CmsScheduledJobInfo job = (CmsScheduledJobInfo)m_jobs.get(i);
                if (jobId.equals(job.getId())) {
                    m_jobs.remove(i);
                    if (jobInfo != null) {
                        LOG.error(Messages.get().key(Messages.LOG_MULTIPLE_JOBS_FOUND_1, jobId));
                    }
                    jobInfo = job;
                }
            }
        }

        if ((jobInfo != null) && jobInfo.isActive()) {
            // job currently active, remove it from the Quartz scheduler
            try {
                // try to remove the job from Quartz
                m_scheduler.unscheduleJob(jobId, Scheduler.DEFAULT_GROUP);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(Messages.LOG_UNSCHEDULED_JOB_1, jobId));
                }
            } catch (SchedulerException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(Messages.LOG_UNSCHEDULING_ERROR_1, jobId));
                }
            }
        }

        return jobInfo;
    }
}
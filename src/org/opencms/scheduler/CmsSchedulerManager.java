/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/scheduler/Attic/CmsSchedulerManager.java,v $
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

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsInitException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Manages the OpenCms scheduler information.<p>
 * 
 * This OpenCms scheduler implementation is based on the 
 * <a href="http://www.opensymphony.com/quartz/">Quartz scheduler</a> from
 * the <a href="http://www.opensymphony.com/">OpenSymphony project</a>.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 *  
 * @version $Revision: 1.1 $
 * @since 5.3
 */
public class CmsSchedulerManager {

    /** The number of configured job entries. */
    private int m_jobCount;
    
    /** The list of configured job entries. */
    private List m_jobEntries;
        
    /** The initialized scheduler. */
    private Scheduler m_scheduler;    
    
    /**
     * Default constructor for the scheduler manager.<p>
     */
    public CmsSchedulerManager() {
        
        m_jobCount = 0;
        m_jobEntries = new ArrayList();
        
        // initialize the scheduler
        m_scheduler = initOpenCmsScheduler();
    }
    
    /**
     * Adds a new entry to the scheduler.<p>
     *  
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     * @param entry the entry to add
     * 
     * @throws CmsException if something goes wrong
     */
    public void addSchedulerEntry(CmsObject cms, CmsSchedulerEntry entry) throws CmsException {

        if (OpenCms.getRunLevel() > 1) {
            // simple unit tests will have runlevel 1 and no CmsObject
            if ((cms == null) || !cms.isAdmin()) {
                throw new CmsInitException(CmsInitException.C_INIT_NO_ADMIN_PERMISSIONS);
            }
        }
        
        // increase job count
        m_jobCount++;
        
        JobDetail jobDetail = new JobDetail(
            "cmsJob" + m_jobCount,
            Scheduler.DEFAULT_GROUP, 
            CmsScheduledJob.class);        
                
        CronTrigger trigger = new CronTrigger(
            "cmsTrigger" + m_jobCount, 
            Scheduler.DEFAULT_GROUP);
        
        try {
            Class.forName(entry.getClassName());
        } catch (ClassNotFoundException e) {
            OpenCms.getLog(this).error("Scheduler: Class not found '" + entry.getClassName() + "'", e);
            // can not continue
            return;
        }
        
        try {
            trigger.setCronExpression(entry.getCronExpression());
        } catch (ParseException e) {
            OpenCms.getLog(this).error("Scheduler: Bad cron expression '" + entry.getCronExpression() + "'", e);
            // can not continue
            return;
        }

        // now set the job data
        JobDataMap jobData = new JobDataMap();
        jobData.put(CmsScheduledJob.C_SCHEDULER_JOB_ENTRY, entry);    
        jobDetail.setJobDataMap(jobData);
        
        try {
            m_scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            OpenCms.getLog(this).error(
                "Scheduler: Could not schedule job for class '" + entry.getClassName() + "'", e);
            // can not continue
            return;
        }
        
        // add the job to the list of configured jobs
        m_jobEntries.add(entry);

        if (OpenCms.getLog(this).isInfoEnabled()) {
            OpenCms.getLog(this).info(
                "Scheduled job "
                    + m_jobEntries.size()
                    + " for class '"
                    + entry.getClassName()
                    + "' with user '"
                    + entry.getUserName());
        }
    }
    
    
    /**
     * Returns the configured job entries in an unmodifiable list.<p>
     *
     * @return the configured job entries in an unmodifiable list
     */
    public List getJobEntries() {

        return Collections.unmodifiableList(m_jobEntries);
    }

    /**
     * Initializes the OpenCms scheduler.<p> 
     * 
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     * 
     * @throws CmsException if something goes wrong
     */
    public void initialize(CmsObject cms) throws CmsException {

        if (OpenCms.getRunLevel() > 1) {
            // simple unit tests will have runlevel 1 and no CmsObject
            if ((cms == null) || !cms.isAdmin()) {
                throw new CmsInitException(CmsInitException.C_INIT_NO_ADMIN_PERMISSIONS);
            }
        }
        
        try {
            // start the scheduler
            m_scheduler.start();
        } catch (Exception e) {
            OpenCms.getLog(this).error("Could not initialize the scheduler", e);
        }
    }
    
    /** 
     * Shuts down this instance of the OpenCms scheduler manager.<p>
     */
    public void shutDown() {

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Shutting down        : " + this.getClass().getName() + " ... ok!");
        }
        
        try {
            m_scheduler.shutdown();
        } catch (SchedulerException e) {
            OpenCms.getLog(this).error("Scheduler: Problems shutting down", e);
        }        
    }    
    
    /**
     * Initializes the Quartz scheduler.<p>
     * 
     * @return the initialized scheduler
     * 
     * @throws SchedulerException in case something goes wrong
     */
    private Scheduler initOpenCmsScheduler() {
        
        Scheduler scheduler = null;
        
        Properties properties = new Properties();
        properties.put(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, "OpenCmsScheduler");
        properties.put(StdSchedulerFactory.PROP_SCHED_THREAD_NAME, "OpenCms: Scheduler");
        properties.put(StdSchedulerFactory.PROP_SCHED_RMI_EXPORT, String.valueOf(false));
        properties.put(StdSchedulerFactory.PROP_SCHED_RMI_PROXY, String.valueOf(false));
        properties.put(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, CmsSchedulerThreadPool.class.getName());
        properties.put(StdSchedulerFactory.PROP_JOB_STORE_CLASS, "org.quartz.simpl.RAMJobStore");

        try {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory(properties);
            scheduler = schedulerFactory.getScheduler();
        } catch (Exception e) {
            OpenCms.getLog(this).error("Could not initialize the scheduler", e);
        }

        return scheduler;
    }    
}

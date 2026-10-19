/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.scheduler;

import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestRunner;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;

/**
 * Test cases for the OpenCms scheduler thread pool.<p>
 *
 * @since 6.0.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestCmsScheduler extends OpenCmsTestRunner {

    /** Cron cadence shared by the scheduler integration tests. */
    static final String CRON_EXPRESSION = "* * * * * ?";

    /** Number of seconds to wait. */
    public static final int SECONDS_TO_WAIT = 30;

    /** Number of threads to run. */
    public static final int THREADS_TO_RUN = 20;

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        initConfiguration();
    }

    /**
     * Tests activating and deactivating of scheduled jobs.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    @Order(5)
    public void testActivateAndDeactivateJob() throws Exception {

        try (CmsScheduledJobTestProbe probe = new CmsScheduledJobTestProbe()) {
            System.out.println("Trying to activate and deactivate an OpenCms job from the OpenCms scheduler.");
            // also make sure CmsUUID is initialized
            CmsUUID.init(CmsUUID.getDummyEthernetAddress());

            CmsScheduledJobInfo jobInfo = new CmsScheduledJobInfo();
            probe.attach(jobInfo);
            CmsContextInfo contextInfo = new CmsContextInfo();
            contextInfo.setUserName(OpenCms.getDefaultUsers().getUserAdmin());
            jobInfo.setContextInfo(contextInfo);
            jobInfo.setClassName(CmsTestScheduledJob.class.getName());
            jobInfo.setCronExpression(CRON_EXPRESSION);

            // set this job as "not active"
            jobInfo.setActive(false);

            List<CmsScheduledJobInfo> jobs = new ArrayList<CmsScheduledJobInfo>();
            jobs.add(jobInfo);
            // create the scheduler with the test job
            CmsScheduleManager scheduler = new CmsScheduleManager(jobs);
            try {
                // initialize the manager, this will start the scheduled jobs
                scheduler.initialize(null);

                // Observe multiple one-second firing opportunities, including dispatch allowance.
                probe.assertNoExecution(3);

                assertEquals(1, scheduler.getJobs().size());
                CmsScheduledJobInfo info = scheduler.getJobs().get(0);
                assertEquals(jobInfo.getId(), info.getId());
                assertEquals(jobInfo.getClassName(), info.getClassName());
                assertEquals(false, info.isActive());
                assertNull(info.getExecutionTimeNext());

                // now set the job active and re-schedule it
                info = info.clone();
                info.setActive(true);
                scheduler.scheduleJob(null, info);

                probe.awaitExecutions(1, 1, 1);

                assertEquals(1, scheduler.getJobs().size());
                info = scheduler.getJobs().get(0);
                assertEquals(jobInfo.getId(), info.getId());
                assertEquals(jobInfo.getClassName(), info.getClassName());
                assertEquals(true, info.isActive());
                assertNotNull(info.getExecutionTimeNext());

                // deactivate the job again and re-schedule it
                info = info.clone();
                info.setActive(false);
                scheduler.scheduleJob(null, info);

                // Observe multiple one-second firing opportunities, including dispatch allowance.
                probe.assertNoExecution(3);

                assertEquals(1, scheduler.getJobs().size());
                info = scheduler.getJobs().get(0);
                assertEquals(jobInfo.getId(), info.getId());
                assertEquals(jobInfo.getClassName(), info.getClassName());
                assertEquals(false, info.isActive());
                assertNull(info.getExecutionTimeNext());
            } finally {
                scheduler.shutDown();
            }
        }
    }

    /**
     * Tests adding and removing a job to the OpenCms schedule manager.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    @Order(2)
    public void testAddAndRemoveJobFromScheduler() throws Exception {

        try (CmsScheduledJobTestProbe probe = new CmsScheduledJobTestProbe()) {
            System.out.println("Trying to add and remove an OpenCms job from the OpenCms scheduler.");
            // also make sure CmsUUID is initialized
            CmsUUID.init(CmsUUID.getDummyEthernetAddress());

            CmsScheduledJobInfo jobInfo = new CmsScheduledJobInfo();
            probe.attach(jobInfo);
            CmsContextInfo contextInfo = new CmsContextInfo();
            contextInfo.setUserName(OpenCms.getDefaultUsers().getUserAdmin());
            jobInfo.setContextInfo(contextInfo);
            jobInfo.setClassName(CmsTestScheduledJob.class.getName());

            jobInfo.setCronExpression(CRON_EXPRESSION);

            List<CmsScheduledJobInfo> jobs = new ArrayList<CmsScheduledJobInfo>();
            jobs.add(jobInfo);
            // create the scheduler with the test job
            CmsScheduleManager scheduler = new CmsScheduleManager(jobs);
            try {
                // initialize the manager, this will start the scheduled jobs
                scheduler.initialize(null);

                probe.awaitExecutions(1, 1, 1);

                CmsScheduledJobInfo result;
                assertEquals(1, scheduler.getJobs().size());
                result = scheduler.unscheduleJob(null, jobInfo.getId());
                assertNotNull(result);
                assertEquals(0, scheduler.getJobs().size());

                result = scheduler.unscheduleJob(null, "iDontExist");
                assertNull(result);
            } finally {
                scheduler.shutDown();
            }
        }
    }

    /**
     * Tests adding an existing job again to the OpenCms scheduler.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    @Order(7)
    public void testAddExistingJobAgainToScheduler() throws Exception {

        try (CmsScheduledJobTestProbe probe = new CmsScheduledJobTestProbe()) {
            System.out.println("Trying to schedule an existing job again with the OpenCms scheduler.");
            // also make sure CmsUUID is initialized
            CmsUUID.init(CmsUUID.getDummyEthernetAddress());

            CmsScheduledJobInfo jobInfo = new CmsScheduledJobInfo();
            probe.attach(jobInfo);
            CmsContextInfo contextInfo = new CmsContextInfo();
            contextInfo.setUserName(OpenCms.getDefaultUsers().getUserAdmin());
            jobInfo.setContextInfo(contextInfo);
            jobInfo.setJobName("My job");
            jobInfo.setClassName(CmsTestScheduledJob.class.getName());

            jobInfo.setCronExpression(CRON_EXPRESSION);

            List<CmsScheduledJobInfo> jobs = new ArrayList<CmsScheduledJobInfo>();
            jobs.add(jobInfo);
            // create the scheduler with the test job
            CmsScheduleManager scheduler = new CmsScheduleManager(jobs);
            try {
                // initialize the manager, this will start the scheduled jobs
                scheduler.initialize(null);

                probe.awaitExecutions(1, 1, 1);

                jobInfo = scheduler.getJob(jobInfo.getId());
                assertEquals("My job", jobInfo.getJobName());

                CmsScheduledJobInfo newInfo = jobInfo.clone();
                newInfo.setJobName("My CHANGED name");
                newInfo.setActive(true);
                assertEquals(1, scheduler.getJobs().size());

                // re-schedule the job with a different name
                scheduler.scheduleJob(null, newInfo);
                // size must still be the same
                assertEquals(jobInfo.getId(), newInfo.getId());
                assertEquals(1, scheduler.getJobs().size());
                jobInfo = scheduler.getJob(newInfo.getId());
                assertEquals("My CHANGED name", jobInfo.getJobName());

                // change cron expression to something invalid
                newInfo = jobInfo.clone();
                newInfo.setActive(true);

                // uncomment because of rolled back quartz due to bugs in version 1.6.x
                CmsIllegalArgumentException ex = null;
                try {
                    newInfo.setCronExpression("* * * * * *");
                    // since Quartz 1.6.x this throws an exception
                } catch (CmsIllegalArgumentException e) {
                    ex = e;
                }
                assertNotNull(ex, "Expected exception not thrown when using invalid CRON expression");
                //FORMAT:Seconds,Minutes,Hours,Day-of-month,Month,Day-of-Week
                //NOTE:"Support for specifying both a day-of-week AND a day-of-month parameter is not implemented."
                newInfo.setCronExpression("* * * * * ?");
                assertEquals(1, scheduler.getJobs().size());
            } finally {
                scheduler.shutDown();
            }
        }
    }

    /**
     * Tests execution of jobs using CmsSchedulerThreadPool.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    @Order(1)
    public void testBasicJobExecution() throws Exception {

        System.out.println("Testing the OpenCms thread pool.");
        Scheduler scheduler = initOpenCmsScheduler();

        CmsTestJob.Probe probe = new CmsTestJob.Probe();
        Set<String> expectedJobs = new HashSet<>();
        try {
            JobDetail[] jobDetail = new JobDetail[THREADS_TO_RUN];
            SimpleTrigger[] trigger = new SimpleTrigger[THREADS_TO_RUN];

            for (int i = 0; i < jobDetail.length; i++) {

                JobDetailImpl tmp = (JobDetailImpl)JobBuilder.newJob(CmsTestJob.class).build();
                tmp.setName("myJob" + i);
                tmp.setGroup(Scheduler.DEFAULT_GROUP);
                tmp.getJobDataMap().put(CmsTestJob.PROBE_KEY, probe);
                expectedJobs.add(tmp.getKey().getName());

                jobDetail[i] = tmp;

                SimpleTriggerImpl tmp2 = (SimpleTriggerImpl)SimpleScheduleBuilder.simpleSchedule().build();
                tmp2.setName("myTrigger" + i);
                tmp2.setGroup(Scheduler.DEFAULT_GROUP);
                tmp2.setStartTime(new Date());
                tmp2.setEndTime(null);
                tmp2.setRepeatCount(0);
                tmp2.setRepeatInterval(0L);
                trigger[i] = tmp2;
            }

            for (int i = 0; i < THREADS_TO_RUN; i++) {
                scheduler.scheduleJob(jobDetail[i], trigger[i]);
            }

            probe.awaitFirstBatch();
            probe.release();
            probe.awaitCompletion();
        } finally {
            // Release blocked workers even when scheduling or an assertion fails.
            probe.release();
            scheduler.shutdown(true);
        }
        probe.assertCompleted(expectedJobs);
    }

    /**
     * Tests launching of an OpenCms job.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    @Order(4)
    public void testCmsJobLaunch() throws Exception {

        try (CmsScheduledJobTestProbe probe = new CmsScheduledJobTestProbe()) {
            System.out.println("Trying to run an OpenCms job 5x.");

            Scheduler scheduler = initOpenCmsScheduler();
            try {
                JobDetailImpl jobDetail = (JobDetailImpl)JobBuilder.newJob(CmsScheduleManager.class).build();
                jobDetail.setName("cmsLaunch");
                jobDetail.setGroup(Scheduler.DEFAULT_GROUP);

                CmsScheduledJobInfo jobInfo = new CmsScheduledJobInfo();
                probe.attach(jobInfo);
                CmsContextInfo contextInfo = new CmsContextInfo(OpenCms.getDefaultUsers().getUserAdmin());
                jobInfo.setContextInfo(contextInfo);
                jobInfo.setClassName(CmsTestScheduledJob.class.getName());

                JobDataMap jobData = new JobDataMap();
                jobData.put(CmsScheduleManager.SCHEDULER_JOB_INFO, jobInfo);

                jobDetail.setJobDataMap(jobData);

                CronTriggerImpl trigger = (CronTriggerImpl)CronScheduleBuilder.cronSchedule(CRON_EXPRESSION).build();
                trigger.setName("cmsLaunchTrigger");
                trigger.setGroup(Scheduler.DEFAULT_GROUP);

                scheduler.scheduleJob(jobDetail, trigger);

                probe.awaitExecutions(1, 1, 1, 1, 1);
            } finally {
                scheduler.shutdown(true);
            }
        }
    }

    /**
     * Tests launching of an OpenCms job with the OpenCms schedule manager.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    @Order(6)
    public void testJobInOpenCmsScheduler() throws Exception {

        try (CmsScheduledJobTestProbe probe = new CmsScheduledJobTestProbe()) {
            System.out.println("Trying to run an OpenCms job 5x with the OpenCms scheduler.");

            CmsScheduledJobInfo jobInfo = new CmsScheduledJobInfo();
            probe.attach(jobInfo);
            CmsContextInfo contextInfo = new CmsContextInfo();
            contextInfo.setUserName(OpenCms.getDefaultUsers().getUserAdmin());
            jobInfo.setContextInfo(contextInfo);
            jobInfo.setClassName(CmsTestScheduledJob.class.getName());

            jobInfo.setCronExpression(CRON_EXPRESSION);

            List<CmsScheduledJobInfo> jobs = new ArrayList<CmsScheduledJobInfo>();
            jobs.add(jobInfo);
            // create the scheduler with the test job
            CmsScheduleManager scheduler = new CmsScheduleManager(jobs);
            try {
                // initialize the manager, this will start the scheduled jobs
                scheduler.initialize(null);

                probe.awaitExecutions(1, 1, 1, 1, 1);
            } finally {
                scheduler.shutDown();
            }
        }
    }

    /**
     * Tests launching of a persistent OpenCms job with the OpenCms schedule manager.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    @Order(3)
    public void testPersitentJobInOpenCmsScheduler() throws Exception {

        try (CmsScheduledJobTestProbe probe = new CmsScheduledJobTestProbe()) {
            System.out.println("Trying to run a persistent OpenCms job 5x with the OpenCms scheduler.");

            CmsScheduledJobInfo jobInfo = new CmsScheduledJobInfo();
            probe.attach(jobInfo);
            CmsContextInfo contextInfo = new CmsContextInfo(OpenCms.getDefaultUsers().getUserAdmin());
            jobInfo.setContextInfo(contextInfo);
            jobInfo.setClassName(CmsTestScheduledJob.class.getName());
            jobInfo.setReuseInstance(true);
            jobInfo.setCronExpression(CRON_EXPRESSION);

            List<CmsScheduledJobInfo> jobs = new ArrayList<CmsScheduledJobInfo>();
            jobs.add(jobInfo);
            // create the scheduler with the test job
            CmsScheduleManager scheduler = new CmsScheduleManager(jobs);
            try {
                // initialize the manager, this will start the scheduled jobs
                scheduler.initialize(null);

                probe.awaitExecutions(1, 2, 3, 4, 5);
            } finally {
                scheduler.shutDown();
            }
        }
    }

    /**
     * Initializes a Quartz scheduler.<p>
     *
     * @return the initialized scheduler
     * @throws Exception in case something goes wrong
     */
    private Scheduler initOpenCmsScheduler() throws Exception {

        Properties properties = new Properties();
        properties.put("org.quartz.scheduler.instanceName", "OpenCmsScheduler");
        properties.put("org.quartz.scheduler.threadName", "OpenCms: Scheduler");
        properties.put("org.quartz.scheduler.rmi.export", CmsStringUtil.FALSE);
        properties.put("org.quartz.scheduler.rmi.proxy", CmsStringUtil.FALSE);
        properties.put("org.quartz.scheduler.xaTransacted", CmsStringUtil.FALSE);
        properties.put("org.quartz.threadPool.class", "org.opencms.scheduler.CmsSchedulerThreadPool");
        properties.put("org.quartz.jobStore.misfireThreshold", "60000");
        properties.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");

        SchedulerFactory schedulerFactory = new StdSchedulerFactory(properties);
        Scheduler scheduler = schedulerFactory.getScheduler();

        scheduler.getMetaData();
        scheduler.start();

        // also make sure CmsUUID is initialized
        CmsUUID.init(CmsUUID.getDummyEthernetAddress());

        return scheduler;
    }
}

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

package org.opencms.publish;

import org.opencms.db.CmsLoginMessage;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.lock.CmsLockException;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for the publish manager.<p>
 */
public class TestPublishManager extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestPublishManager(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestPublishManager.class.getName());

        suite.addTest(new TestPublishManager("testPublishReport"));
        suite.addTest(new TestPublishManager("testAbortJob"));
        suite.addTest(new TestPublishManager("testRunning"));
        suite.addTest(new TestPublishManager("testStop"));
        suite.addTest(new TestPublishManager("testListener"));
        suite.addTest(new TestPublishManager("testInitialization1"));
        suite.addTest(new TestPublishManager("testInitialization2"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Test aborting an enqueued publish job.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testAbortJob() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing aborting an enqueued publish job");

        String source = "/folder2/subfolder21/image1.gif";
        String destination = "/folder1/image1_new"; // + i + ".gif";

        assertFalse(OpenCms.getPublishManager().isRunning());
        // stop the publish engine in order to perform the checks
        OpenCms.getPublishManager().stopPublishing();

        // copy and publish n new resources
        int max = 10;
        for (int i = 0; i < max; i++) {
            cms.copyResource(source, destination + (i + 1) + ".gif", CmsResource.COPY_AS_NEW);
            OpenCms.getPublishManager().publishResource(cms, destination + (i + 1) + ".gif");
        }

        // get the last enqueued publish job
        List queue = OpenCms.getPublishManager().getPublishQueue();
        CmsPublishJobEnqueued publishJob = (CmsPublishJobEnqueued)queue.get(queue.size() - 1);

        // login another user
        cms.loginUser("test1", "test1");
        try {
            // try to abort the last enqueued publish job
            OpenCms.getPublishManager().abortPublishJob(cms, publishJob, true);
            fail("should not be possible to abort a publish job by another user");
        } catch (CmsSecurityException e) {
            // ok, ignore
        }
        // login again as admin, keep online project
        cms.loginUser("Admin", "admin");

        // abort the last enqueued publish job
        OpenCms.getPublishManager().abortPublishJob(cms, publishJob, true);

        // try to abort the publish job again
        try {
            OpenCms.getPublishManager().abortPublishJob(cms, publishJob, true);
            fail("should not be possible to abort a publish job that has been already aborted");
        } catch (CmsPublishException e) {
            // ok, ignore
        }

        // start the background publishing again
        OpenCms.getPublishManager().startPublishing();
        // wait until everything get published
        OpenCms.getPublishManager().waitWhileRunning();

        // check that all files except the last one has been published
        for (int i = 1; i < max; i++) {
            assertState(cms, destination + i + ".gif", CmsResource.STATE_UNCHANGED);
        }
        // the last one should not be published
        assertFalse(cms.existsResource(destination + max + ".gif"));

        // switch to the "Offline" project
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        // check that all files except the last one has been published
        for (int i = 1; i < max; i++) {
            assertState(cms, destination + i + ".gif", CmsResource.STATE_UNCHANGED);
            // and unlocked
            assertLock(cms, destination + i + ".gif", CmsLockType.UNLOCKED);
        }
        // the last one should not be published
        assertState(cms, destination + max + ".gif", CmsResource.STATE_NEW);
        // and unlocked
        assertLock(cms, destination + max + ".gif", CmsLockType.UNLOCKED);

        // clean up
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Tests the reinitialization of the publish manager/engine.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testInitialization1() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing initializing the publish manager/engine");

        // publish engine should not run currently
        assertFalse(OpenCms.getPublishManager().isRunning());
        // stop publishing
        OpenCms.getPublishManager().stopPublishing();

        String source = "/folder2/subfolder21/image1.gif";
        String destination1 = "/testInitialization1_"; // + i + ".gif";

        if (!cms.getLock(source).isNullLock()) {
            cms.unlockResource(source);
        }

        int max = 12; // be sure that it is the same as the system/publishhistory/history-size value + 2
        // copy n new resources
        for (int i = 0; i < max; i++) {
            cms.copyResource(source, destination1 + (i + 1) + ".gif", CmsResource.COPY_AS_NEW);
            OpenCms.getPublishManager().publishResource(cms, destination1 + (i + 1) + ".gif");
        }

        // store current publishQueue
        List oldQueue = OpenCms.getPublishManager().getPublishQueue();
        // store current publish history
        List oldHistory = OpenCms.getPublishManager().getPublishHistory();

        // leads to reloading the queue and history data from the database
        OpenCms.getPublishManager().initialize(cms);

        // get reinitialized queue and history
        List newQueue = OpenCms.getPublishManager().getPublishQueue();
        List newHistory = OpenCms.getPublishManager().getPublishHistory();

        // compare old and new queue
        echo("Checking revived publish queue (" + oldQueue.size() + " items)");
        if (newQueue.size() != oldQueue.size()) {
            fail(
                "Old and new queue have not the same size: Expected <"
                    + oldQueue.size()
                    + ">, was <"
                    + newQueue.size()
                    + ">");
        }
        Iterator n = newQueue.iterator();
        Iterator o = oldQueue.iterator();
        while (n.hasNext() && o.hasNext()) {
            CmsPublishJobEnqueued newJob = (CmsPublishJobEnqueued)n.next();
            CmsPublishJobEnqueued oldJob = (CmsPublishJobEnqueued)o.next();
            assertEquals(newJob, oldJob, true, true);
        }

        // compare old and new history
        echo("Checking revived publish history (" + oldHistory.size() + " items)");
        if (newHistory.size() != oldHistory.size()) {
            fail(
                "Old and new history have not the same size: Expected <"
                    + oldHistory.size()
                    + ">, was <"
                    + newHistory.size()
                    + ">");
        }
        n = newHistory.iterator();
        o = oldHistory.iterator();
        while (n.hasNext() && o.hasNext()) {
            CmsPublishJobFinished newJob = (CmsPublishJobFinished)n.next();
            CmsPublishJobFinished oldJob = (CmsPublishJobFinished)o.next();
            assertEquals(newJob, oldJob, false, true);
        }

        // start the publish engine and wait until all jobs are published
        OpenCms.getPublishManager().startPublishing();
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Tests the reinitialization of the publish manager/engine.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testInitialization2() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing initializing the publish manager/engine");

        // publish engine should not run currently
        assertFalse(OpenCms.getPublishManager().isRunning());
        // stop publishing
        OpenCms.getPublishManager().stopPublishing();

        String source = "/folder2/subfolder21/image1.gif";
        String destination2 = "/testInitialization2_"; // + i + ".gif";

        if (!cms.getLock(source).isNullLock()) {
            cms.unlockResource(source);
        }

        int max = 12; // be sure that it is the same as the system/publishhistory/history-size value + 2
        // create a larger project
        for (int i = 0; i < max; i++) {
            cms.copyResource(source, destination2 + (i + 1) + ".gif", CmsResource.COPY_AS_NEW);
        }
        OpenCms.getPublishManager().publishProject(cms);

        // store current publishQueue
        List oldQueue = OpenCms.getPublishManager().getPublishQueue();

        // add an event listener used to restart the engine when ther job was started
        OpenCms.getPublishManager().addPublishListener(new TestPublishEventListener2(cms));

        // now start publishing again and reinitialize the publish manager and engine while it is running
        OpenCms.getPublishManager().startPublishing();
        OpenCms.getPublishManager().waitWhileRunning();

        // get reinitialized queue and history
        List newQueue = OpenCms.getPublishManager().getPublishQueue();
        List newHistory = OpenCms.getPublishManager().getPublishHistory();

        // the project should not be in the publish queue anymore
        assertEquals(1, oldQueue.size());
        assertEquals(0, newQueue.size());

        // but it should be stored as last entry in the history
        CmsPublishJobEnqueued jobInQueue = (CmsPublishJobEnqueued)oldQueue.get(0);
        CmsPublishJobFinished jobInHistory = (CmsPublishJobFinished)newHistory.get(newHistory.size() - 1);
        assertEquals(jobInQueue, jobInHistory, false, false);

        // and it should be aborted
    }

    /**
     * Test the publish event listener.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testListener() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing the publish event listener");

        String source = "/folder2/subfolder21/image1.gif";
        String destination = "/testListener_"; // + i + ".gif";

        // copy n new resources
        int max = 12; // be sure that it is the same as the system/publishhistory/history-size value + 2
        for (int i = 0; i < max; i++) {
            cms.copyResource(source, destination + (i + 1) + ".gif", CmsResource.COPY_AS_NEW);
        }

        // set the listener for the first job
        TestPublishEventListener firstListener = new TestPublishEventListener(cms.readResource(destination + "1.gif"));
        OpenCms.getPublishManager().addPublishListener(firstListener);

        // set the listener for the last job
        TestPublishEventListener lastListener = new TestPublishEventListener(
            cms.readResource(destination + max + ".gif"));
        OpenCms.getPublishManager().addPublishListener(lastListener);

        // remember the time
        long preEnqueueTime = System.currentTimeMillis();

        assertFalse(OpenCms.getPublishManager().isRunning());
        // stop the publish engine in order to perform the checks
        OpenCms.getPublishManager().stopPublishing();

        // publish(enqueue) n new resources
        for (int i = 0; i < max; i++) {
            OpenCms.getPublishManager().publishResource(cms, destination + (i + 1) + ".gif");
        }
        assertFalse(OpenCms.getPublishManager().isRunning());

        // get the last enqueued publish job
        List queue = OpenCms.getPublishManager().getPublishQueue();
        CmsPublishJobEnqueued publishJob = (CmsPublishJobEnqueued)queue.get(queue.size() - 1);
        // abort it
        OpenCms.getPublishManager().abortPublishJob(cms, publishJob, true);

        // start the background publishing again
        OpenCms.getPublishManager().startPublishing();
        // wait until finished
        OpenCms.getPublishManager().waitWhileRunning();

        // the first job was not aborted
        assertEquals(0, firstListener.getAborted());
        // but it was enqueued
        assertTrue(preEnqueueTime <= firstListener.getEnqueued());
        // and started
        assertTrue(firstListener.getEnqueued() <= firstListener.getStarted());
        // and finished
        assertTrue(firstListener.getStarted() <= firstListener.getFinished());
        // and removed (depending on history size)
        // assertTrue(firstListener.getFinished() <= firstListener.getRemoved());

        // the last job was not started
        assertEquals(0, lastListener.getStarted());
        // nor finished
        assertEquals(0, lastListener.getFinished());
        // but it was enqueued
        assertTrue(preEnqueueTime <= lastListener.getEnqueued());
        // and aborted
        assertTrue(lastListener.getEnqueued() <= lastListener.getAborted());
        // and removed
        assertTrue(lastListener.getAborted() <= lastListener.getRemoved());

        // check the jobs in queue counters
        int[] firstJobsInQueue = firstListener.getJobsInQueueCounter();
        int[] lastJobsInQueue = lastListener.getJobsInQueueCounter();
        // at enqueue time of first job: queue should contain only the job
        assertEquals(1, firstJobsInQueue[0]);
        // at enqueue time of last job: queue should contain  all jobs
        assertEquals(max, lastJobsInQueue[0]);
        // at start time of first job: queue should contain all jobs but not the job itself and not the aborted job
        assertEquals(max - 2, firstJobsInQueue[1]);
        // last job is never started
        assertEquals(0, lastJobsInQueue[1]);
        // at finish time of first job: queue should still contain all jobs but not the job itself and not the aborted job
        assertEquals(max - 2, firstJobsInQueue[2]);
        // last job is never finished
        assertEquals(0, lastJobsInQueue[2]);
        // first job is not aborted
        assertEquals(0, firstJobsInQueue[3]);
        // at abort time of last job: queue should contain all jobs including the aborted job
        assertEquals(max, lastJobsInQueue[3]);
        // first job is not removed since it is still in the job history
        assertEquals(0, firstJobsInQueue[4]);
        // at remove time of the last job: queue should contain all jobs but not the aborted job
        assertEquals(max - 1, lastJobsInQueue[4]);

        // check the jobs in history counters
        int[] firstJobsInHistory = firstListener.getJobsInHistoryCounter();
        int[] lastJobsInHistory = lastListener.getJobsInHistoryCounter();
        // at enqueue/start/finish time of the first job, the history is full (10 Elements)
        assertEquals(10, firstJobsInHistory[0]);
        assertEquals(10, firstJobsInHistory[1]);
        assertEquals(10, firstJobsInHistory[2]);
        assertEquals(0, firstJobsInHistory[3]);
        assertEquals(9, firstJobsInHistory[4]);
        // when the first element is removed, the history queue has one free slot
        // at start/abort/remove time of the last job, the history is unchanged
        assertEquals(10, lastJobsInHistory[0]);
        assertEquals(0, lastJobsInHistory[1]);
        assertEquals(0, lastJobsInHistory[2]);
        assertEquals(10, lastJobsInHistory[3]);
        assertEquals(10, lastJobsInHistory[4]);

        // clean up
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Tests the publish report stored in the database.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testPublishReport() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing the publish report");

        String source = "/folder2/subfolder21/image1.gif";
        String destination = "/testReport_1.gif";

        cms.copyResource(source, destination, CmsResource.COPY_AS_NEW);

        OpenCms.getPublishManager().publishResource(cms, destination);
        OpenCms.getPublishManager().waitWhileRunning();

        List history = OpenCms.getPublishManager().getPublishHistory();
        CmsPublishJobFinished publishJob = (CmsPublishJobFinished)history.get(history.size() - 1);
        String reportContents = new String(OpenCms.getPublishManager().getReportContents(publishJob));

        // check if the report states that the destination was published successfully
        if (!reportContents.matches("(?s)" + "(.*)" + destination + "(.*)" + "o.k." + "(.*)")) {
            System.err.println(reportContents);
            fail("publish report contains errors");
        }
    }

    /**
     * Test publishing process.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testRunning() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publishing process");

        String source = "/folder2/subfolder21/image1.gif";
        String destination1 = "/folder2/image1_new.gif";
        String destination2 = "/folder2/image1_sibling1.gif";
        String destination3 = "/folder2/image1_sibling2.gif";
        String destination4 = "/folder2/image1_sibling3.gif";

        // make four copies of a file to be published later
        cms.copyResource(source, destination1, CmsResource.COPY_AS_NEW);
        cms.copyResource(source, destination2, CmsResource.COPY_AS_SIBLING);
        cms.copyResource(source, destination3, CmsResource.COPY_AS_SIBLING);
        cms.copyResource(source, destination4, CmsResource.COPY_AS_SIBLING);

        assertFalse(OpenCms.getPublishManager().isRunning());
        // stop the publish engine in order to perform the checks
        OpenCms.getPublishManager().stopPublishing();
        // now publish (enqeue) the project
        OpenCms.getPublishManager().publishProject(cms);
        assertFalse(OpenCms.getPublishManager().isRunning());

        assertLock(cms, destination1, CmsLockType.PUBLISH);
        try {
            cms.lockResource(destination1);
            fail("it should not be possible to lock a resource that is waiting to be published");
        } catch (CmsLockException e) {
            // ok, ignore
        }
        assertLock(cms, destination2, CmsLockType.PUBLISH);
        try {
            cms.lockResource(destination2);
            fail("it should not be possible to lock a resource that is waiting to be published");
        } catch (CmsLockException e) {
            // ok, ignore
        }
        assertLock(cms, destination3, CmsLockType.PUBLISH);
        try {
            cms.lockResource(destination3);
            fail("it should not be possible to lock a resource that is waiting to be published");
        } catch (CmsLockException e) {
            // ok, ignore
        }
        assertLock(cms, destination4, CmsLockType.PUBLISH);
        try {
            cms.lockResource(destination4);
            fail("it should not be possible to lock a resource that is waiting to be published");
        } catch (CmsLockException e) {
            // ok, ignore
        }

        // now start the background publishing process
        OpenCms.getPublishManager().startPublishing();
        // wait until the background publishing is finished
        OpenCms.getPublishManager().waitWhileRunning();

        // now check the locks again
        assertLock(cms, destination1, CmsLockType.UNLOCKED);
        assertLock(cms, destination2, CmsLockType.UNLOCKED);
        assertLock(cms, destination3, CmsLockType.UNLOCKED);
        assertLock(cms, destination4, CmsLockType.UNLOCKED);
    }

    /**
     * Test stopping/starting the publish engine.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testStop() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing stopping/starting the publish engine");

        String source = "/folder2/subfolder21/image1.gif";
        String destination1 = "/folder2/subfolder22/image1_new1.gif";
        String destination2 = "/folder2/subfolder22/image1_new2.gif";
        String destination3 = "/folder2/subfolder22/image1_new3.gif";
        String destination4 = "/folder2/subfolder22/image1_new4.gif";

        // copy and publish a new resource
        cms.copyResource(source, destination1, CmsResource.COPY_AS_NEW);
        OpenCms.getPublishManager().publishResource(cms, destination1);

        // create another cms user instance
        CmsContextInfo contextInfo = new CmsContextInfo("test1");
        contextInfo.setProjectName(cms.getRequestContext().getCurrentProject().getName());
        contextInfo.setSiteRoot(cms.getRequestContext().getSiteRoot());
        CmsObject ucms = OpenCms.initCmsObject(cms, contextInfo);

        // stop the publish engine by disabling the login
        OpenCms.getLoginManager().setLoginMessage(cms, new CmsLoginMessage("test", true));

        // copy and publish a new resource
        cms.copyResource(source, destination2, CmsResource.COPY_AS_NEW);
        // should still work since i am the admin
        OpenCms.getPublishManager().publishResource(cms, destination2);
        // wait until publish engine is finished
        OpenCms.getPublishManager().waitWhileRunning();

        // create new resources
        cms.copyResource(source, destination3, CmsResource.COPY_AS_NEW);
        cms.copyResource(source, destination4, CmsResource.COPY_AS_NEW);

        // give the user publish permission
        cms.chacc(
            destination3,
            I_CmsPrincipal.PRINCIPAL_USER,
            "test1",
            new CmsPermissionSet(CmsPermissionSet.PERMISSION_FULL, 0).getPermissionString());
        cms.chacc(
            destination4,
            I_CmsPrincipal.PRINCIPAL_USER,
            "test1",
            new CmsPermissionSet(CmsPermissionSet.PERMISSION_FULL, 0).getPermissionString());

        // and unlock the resources in order to let the other user publish them
        cms.unlockResource(destination3);
        cms.unlockResource(destination4);

        // publish a new resource
        try {
            OpenCms.getPublishManager().publishResource(ucms, destination3);
            fail(
                "a user without administration rights should not be able to publish when the publish engine is disabled");
        } catch (CmsPublishException e) {
            // ok, ignore
        }

        // the resource should not have a publish lock left (was an error)
        assertTrue(ucms.getLock(destination3).isUnlocked());

        // re-enable the login (and implicitly the publish engine)
        OpenCms.getLoginManager().removeLoginMessage(cms);
        // publish engine should not run currently
        assertFalse(OpenCms.getPublishManager().isRunning());

        // try again, it should work now
        OpenCms.getPublishManager().publishResource(ucms, destination4);
        // wait until resource is published
        OpenCms.getPublishManager().waitWhileRunning();

        // third resource was not published, while fourth resource was
        assertState(cms, destination3, CmsResource.STATE_NEW);
        assertState(cms, destination4, CmsResource.STATE_UNCHANGED);

        // clean up
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
    }
}
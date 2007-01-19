/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/publish/TestPublishManager.java,v $
 * Date   : $Date: 2007/01/19 16:54:02 $
 * Version: $Revision: 1.1.2.2 $
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

package org.opencms.publish;

import org.opencms.db.CmsLoginMessage;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.lock.CmsLockException;
import org.opencms.lock.CmsLockType;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for the publish manager.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.2 $
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

        suite.addTest(new TestPublishManager("testAbortJob"));
        suite.addTest(new TestPublishManager("testRunning"));
        suite.addTest(new TestPublishManager("testStop"));
        suite.addTest(new TestPublishManager("testListener"));

        TestSetup wrapper = new TestSetup(suite) {

            protected void setUp() {

                setupOpenCms("simpletest", "/sites/default/");
            }

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
            OpenCms.getPublishManager().abortPublishJob(cms, publishJob);
            fail("should not be possible to abort a publish job by another user");
        } catch (CmsSecurityException e) {
            // ok, ignore
        }
        // login again as admin, keep online project
        cms.loginUser("Admin", "admin");

        // abort the last enqueued publish job
        OpenCms.getPublishManager().abortPublishJob(cms, publishJob);

        // try to abort the publish job again
        try {
            OpenCms.getPublishManager().abortPublishJob(cms, publishJob);
            fail("should not be possible to abort a publish job that has been already aborted");
        } catch (CmsPublishException e) {
            // ok, ignore
        }

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
        String destination = "/image1_new"; // + i + ".gif";

        // copy n new resources
        int max = 12; // be sure that it is the same as the system/publishhistory/history-size value + 2
        for (int i = 0; i < max; i++) {
            cms.copyResource(source, destination + (i + 1) + ".gif", CmsResource.COPY_AS_NEW);
        }

        // set the listener for the first job
        TestPublishEventListener firstListener = new TestPublishEventListener(cms.readResource(destination + "1.gif"));
        OpenCms.getPublishManager().addPublishListener(firstListener);

        // set the listener for the last job
        TestPublishEventListener lastListener = new TestPublishEventListener(cms.readResource(destination
            + max
            + ".gif"));
        OpenCms.getPublishManager().addPublishListener(lastListener);

        // remember the time
        long preEnqueueTime = System.currentTimeMillis();

        // publish n new resources
        for (int i = 0; i < max; i++) {
            OpenCms.getPublishManager().publishResource(cms, destination + (i + 1) + ".gif");
        }

        // get the last enqueued publish job
        List queue = OpenCms.getPublishManager().getPublishQueue();
        CmsPublishJobEnqueued publishJob = (CmsPublishJobEnqueued)queue.get(queue.size() - 1);
        // abort it
        OpenCms.getPublishManager().abortPublishJob(cms, publishJob);

        // wait until finished
        OpenCms.getPublishManager().waitWhileRunning();

        // the first job was not aborted
        assertEquals(0, firstListener.getAborted());
        // but it was enqueued
        assertTrue(preEnqueueTime <= firstListener.getEnqueued());
        // ans started
        assertTrue(firstListener.getEnqueued() <= firstListener.getStarted());
        // and finished
        assertTrue(firstListener.getStarted() <= firstListener.getFinished());
        // and removed
        assertTrue(firstListener.getFinished() <= firstListener.getRemoved());

        // the last job was not started
        assertEquals(0, lastListener.getStarted());
        // nor finsihed
        assertEquals(0, lastListener.getFinished());
        // nor removed
        assertEquals(0, lastListener.getRemoved());
        // but it was enqueued
        assertTrue(preEnqueueTime <= lastListener.getEnqueued());
        // and aborted
        assertTrue(lastListener.getEnqueued() <= lastListener.getAborted());
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
        OpenCms.getPublishManager().publishProject(cms);

        /////////////////////////////////////////////////////////////////////////////
        // README:
        // The assertions in this block may fail if the resources get published first.
        // This is because we can not say the publish engine to wait 1 sec to do the
        // checks before publishing...
        // but it should work 90% of the time
        //        
        assertTrue(OpenCms.getPublishManager().isRunning());

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
        //
        // Here ends the concurrent block, the assertions below should always work
        /////////////////////////////////////////////////////////////////////////////
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

        // stop the publish engine
        long startTime = System.currentTimeMillis() + 1000;
        long endTime = System.currentTimeMillis() + 2000;
        OpenCms.getLoginManager().setLoginMessage(cms, new CmsLoginMessage(startTime, endTime, "test", true));

        // copy and publish a new resource 
        cms.copyResource(source, destination2, CmsResource.COPY_AS_NEW);
        // should still work since i am the admin
        OpenCms.getPublishManager().publishResource(cms, destination2);

        // create new resources
        cms.copyResource(source, destination3, CmsResource.COPY_AS_NEW);
        cms.copyResource(source, destination4, CmsResource.COPY_AS_NEW);

        // give the user publish permission
        cms.chacc(destination3, I_CmsPrincipal.PRINCIPAL_USER, "test1", new CmsPermissionSet(
            CmsPermissionSet.PERMISSION_FULL,
            0).getPermissionString());
        cms.chacc(destination4, I_CmsPrincipal.PRINCIPAL_USER, "test1", new CmsPermissionSet(
            CmsPermissionSet.PERMISSION_FULL,
            0).getPermissionString());

        // login as other user
        cms.loginUser("test1", "test1");
        // switch to the offline project
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        // wait until the publish engine is disabled
        synchronized (this) {
            wait(1000);
        }

        // publish a new resource 
        try {
            OpenCms.getPublishManager().publishResource(cms, destination3);
            fail("a user without administration rights should not be able to publish when the publish engine is disabled");
        } catch (CmsPublishException e) {
            // ok, ignore
        }

        // wait until the publish engine is enabled again
        synchronized (this) {
            wait(1000);
        }

        // try again, it should work now
        OpenCms.getPublishManager().publishResource(cms, destination4);
    }
}

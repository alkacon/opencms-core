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

package org.opencms.file;

import org.opencms.db.CmsPublishList;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishJobFinished;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsThreadedTestCase;
import org.opencms.test.OpenCmsThreadedTestCaseSuite;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for concurrent operations of the CmsObject.<p>
 */
public class TestConcurrentOperations extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestConcurrentOperations(String arg0) {

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
        suite.setName(TestConcurrentOperations.class.getName());

        suite.addTest(new TestConcurrentOperations("testConcurrentPublishResource"));
        suite.addTest(new TestConcurrentOperations("testConcurrentPublishResourceWithRelated"));
        suite.addTest(new TestConcurrentOperations("testConcurrentPublishProject"));
        suite.addTest(new TestConcurrentOperations("testConcurrentCreationIssue"));

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
     * Concurrent creation test method.<p>
     *
     * @param cms the OpenCms user context to use
     * @param count the count for this test
     * @param value used to pass a counter value between the councurrent tests
     *
     * @throws Exception in case the resource could not be created (which is expected)
     */
    public void doConcurrentCreationOperation(CmsObject cms, Integer count, Integer[] value) throws Exception {

        System.out.println("Running doConcurrentCreationOperation() method call - count: " + count.intValue());
        int val = value[0].intValue();
        String name = "/testfolder/sub1/sub2/sub3/subtestfolder" + val;
        CmsResource res = cms.createResource(name, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        value[0] = Integer.valueOf(++val);
        System.out.println(
            "++++++++++++++++++ Finished creation of folder " + res.getRootPath() + " - count: " + count.intValue());
    }

    /**
     * Concurrent publish project test method.<p>
     *
     * @param cms the OpenCms user context to use
     * @param count the count for this test
     * @param value used to pass a counter value between the councurrent tests
     *
     * @throws Exception if something goes wrong, unexpected
     */
    public void doConcurrentPublishProjectOperation(CmsObject cms, Integer count, Integer[] value) throws Exception {

        System.out.println("Running doConcurrentPublishProjectOperation() method call - count: " + count.intValue());
        int val = value[0].intValue();
        OpenCms.getPublishManager().publishProject(cms);
        value[0] = Integer.valueOf(++val);
        System.out.println("++++++++++++++++++ Finished publish project - count: " + count.intValue());
    }

    /**
     * Concurrent publish resource test method.<p>
     *
     * @param cms the OpenCms user context to use
     * @param count the count for this test
     * @param value used to pass a counter value between the councurrent tests
     *
     * @throws Exception if something goes wrong, unexpected
     */
    public void doConcurrentPublishResourceOperation(CmsObject cms, Integer count, Integer[] value) throws Exception {

        System.out.println("Running doConcurrentPublishResourceOperation() method call - count: " + count.intValue());
        int val = value[0].intValue();

        OpenCms.getPublishManager().publishResource(cms, "index.html");

        value[0] = Integer.valueOf(++val);
        System.out.println("++++++++++++++++++ Finished publish resource - count: " + count.intValue());
    }

    /**
     * Concurrent publish resource test method.<p>
     *
     * @param cms the OpenCms user context to use
     * @param count the count for this test
     * @param value used to pass a counter value between the councurrent tests
     *
     * @throws Exception if something goes wrong, unexpected
     */
    public void doConcurrentPublishResourceWithRelatedOperation(CmsObject cms, Integer count, Integer[] value)
    throws Exception {

        System.out.println("thread " + count + ": starting");
        int val = value[0].intValue();

        // create a new publish list
        System.out.println("thread " + count + ": getting publish list");
        CmsPublishList publishList = OpenCms.getPublishManager().getPublishList(
            cms,
            cms.readResource("index.html", CmsResourceFilter.ALL),
            false);
        // get the related resources
        System.out.println("thread " + count + ": getting related resources");
        CmsPublishList relResources = OpenCms.getPublishManager().getRelatedResourcesToPublish(cms, publishList);
        // merge the two publish lists
        System.out.println("thread " + count + ": merging publish lists");
        publishList = OpenCms.getPublishManager().mergePublishLists(cms, publishList, relResources);
        // publish
        System.out.println("thread " + count + ": publishing");
        OpenCms.getPublishManager().publishProject(
            cms,
            new CmsShellReport(cms.getRequestContext().getLocale()),
            publishList);

        value[0] = Integer.valueOf(++val);
        System.out.println("thread " + count + ": finished");
    }

    /**
     * Tests concurrent creation a resource with the same name.<p>
     *
     * @throws Exception if the test fails
     */
    public void testConcurrentCreationIssue() throws Exception {

        int count = 50;
        echo("Concurrent folder creation test: Testing concurrent folder creation with " + count + " threads");

        CmsObject cms = getCmsObject();

        cms.createResource("/testfolder/", CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.createResource("/testfolder/sub1/", CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.createResource("/testfolder/sub1/sub2/", CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.createResource("/testfolder/sub1/sub2/sub3/", CmsResourceTypeFolder.RESOURCE_TYPE_ID);

        String name = "doConcurrentCreationOperation";
        Integer[] value = new Integer[1];
        value[0] = Integer.valueOf(0);

        Object[] parameters = new Object[] {
            OpenCmsThreadedTestCaseSuite.PARAM_CMSOBJECT,
            OpenCmsThreadedTestCaseSuite.PARAM_COUNTER,
            value};
        OpenCmsThreadedTestCaseSuite suite = new OpenCmsThreadedTestCaseSuite(count, this, name, parameters);
        OpenCmsThreadedTestCase[] threads = suite.run();

        if (suite.getThrowable() != null) {
            throw new Exception(suite.getThrowable());
        }

        int c = 0;
        int ec = 0;
        for (int i = 0; i < count; i++) {
            Throwable e = threads[i].getThrowable();
            if (e instanceof CmsVfsException) {
                // an exception was thrown, so this thread can not have created a resource
                c++;
            }
            if (e instanceof CmsVfsResourceAlreadyExistsException) {
                CmsVfsResourceAlreadyExistsException e2 = (CmsVfsResourceAlreadyExistsException)e;
                if (e2.getMessageContainer().getKey() == org.opencms.db.generic.Messages.ERR_RESOURCE_WITH_NAME_CURRENTLY_CREATED_1) {
                    // this is the expected "concurrent creation" exception
                    ec++;
                } else {
                    // also check the cause, may be nested
                    e = e2.getCause();
                    if (e instanceof CmsVfsResourceAlreadyExistsException) {
                        e2 = (CmsVfsResourceAlreadyExistsException)e;
                        if (e2.getMessageContainer().getKey() == org.opencms.db.generic.Messages.ERR_RESOURCE_WITH_NAME_CURRENTLY_CREATED_1) {
                            // this is the expected "concurrent creation" exception
                            ec++;
                        }
                    }
                }
            }
        }

        // now read all resources in the folder and check if duplicates have been created
        List resources = cms.readResources("/testfolder/sub1/sub2/sub3/", CmsResourceFilter.ALL);
        Iterator i = resources.iterator();
        Set names = new HashSet();
        List duplicates = new ArrayList();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            if (names.contains(res.getRootPath())) {
                // found a duplicate name
                duplicates.add(res);
            } else {
                // this is no duplicate
                names.add(res.getRootPath());
            }
        }
        i = duplicates.iterator();
        int c2 = 0;
        while (i.hasNext()) {
            // ouput duplicate list to console
            c2++;
            CmsResource res = (CmsResource)i.next();
            System.err.println("Duplicate resource " + c2 + " : " + res.getRootPath() + " - " + res.getStructureId());
        }
        if (duplicates.size() > 0) {
            // ducplicates where found
            fail("There where " + duplicates.size() + " duplicate resources created");
        }
        if (c != (count - value[0].intValue())) {
            // not the right number of exception where thrown
            fail("Exception count " + c + " id not return the expected result " + (count - value[0].intValue()));
        }
        if (ec != 0) {
            // the "concurrent creation" exception was thrown one or mote times - this must be an error
            fail("Did catch concurrent creation exception at least once - no concurrent exceptions expected!");
        }
        echo("Concurrent folder creation test success: No duplicates created - "
            + ec
            + " concurrent modification exceptions caught");
        echo("Total runtime of concurrent test suite: " + CmsStringUtil.formatRuntime(suite.getRuntime()));
    }

    /**
     * Test concurrently publish same project.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testConcurrentPublishProject() throws Throwable {

        int count = 10;

        CmsObject cms = getCmsObject();
        echo("Testing concurrently publish same project with " + count + " threads");

        String resName = "/";
        cms.lockResource(resName);
        cms.setDateLastModified(resName, System.currentTimeMillis(), true);

        // publish directly with a single resource
        String name = "doConcurrentPublishProjectOperation";
        Integer[] value = new Integer[1];
        value[0] = Integer.valueOf(0);

        Object[] parameters = new Object[] {
            OpenCmsThreadedTestCaseSuite.PARAM_CMSOBJECT,
            OpenCmsThreadedTestCaseSuite.PARAM_COUNTER,
            value};
        OpenCmsThreadedTestCaseSuite suite = new OpenCmsThreadedTestCaseSuite(count, this, name, parameters);
        suite.setAllowedRuntime(20000);
        OpenCmsThreadedTestCase[] threads = suite.run();

        if (suite.getThrowable() != null) {
            throw new Exception(suite.getThrowable());
        }

        OpenCms.getPublishManager().waitWhileRunning();

        for (int i = 0; i < count; i++) {
            Throwable e = threads[i].getThrowable();
            if (e != null) {
                throw new Exception(e);
            }
        }

        List pubHistory = OpenCms.getPublishManager().getPublishHistory();
        assertEquals(10, pubHistory.size());
        CmsPublishJobFinished pubJob = (CmsPublishJobFinished)pubHistory.get(0);
        assertEquals(13 + 51, pubJob.getSize());
        for (int i = 1; i < 10; i++) {
            pubJob = (CmsPublishJobFinished)pubHistory.get(i);
            assertEquals("pubJob: " + i, 0, pubJob.getSize());
        }

        echo("Concurrent publish project test success");
        echo("Total runtime of concurrent test suite: " + CmsStringUtil.formatRuntime(suite.getRuntime()));
    }

    /**
     * Test concurrently publish same resource.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testConcurrentPublishResource() throws Throwable {

        int count = 10;

        CmsObject cms = getCmsObject();
        echo("Testing concurrently publish same resource with " + count + " threads");

        String resName = "index.html";
        cms.lockResource(resName);
        cms.setDateLastModified(resName, System.currentTimeMillis(), true);

        // publish directly with a single resource
        String name = "doConcurrentPublishResourceOperation";
        Integer[] value = new Integer[1];
        value[0] = Integer.valueOf(0);

        Object[] parameters = new Object[] {
            OpenCmsThreadedTestCaseSuite.PARAM_CMSOBJECT,
            OpenCmsThreadedTestCaseSuite.PARAM_COUNTER,
            value};
        OpenCmsThreadedTestCaseSuite suite = new OpenCmsThreadedTestCaseSuite(count, this, name, parameters);
        suite.setAllowedRuntime(10000);
        OpenCmsThreadedTestCase[] threads = suite.run();

        if (suite.getThrowable() != null) {
            throw new Exception(suite.getThrowable());
        }

        OpenCms.getPublishManager().waitWhileRunning();

        for (int i = 0; i < count; i++) {
            Throwable e = threads[i].getThrowable();
            if (e != null) {
                throw new Exception(e);
            }
        }

        List pubHistory = OpenCms.getPublishManager().getPublishHistory();
        assertEquals(10, pubHistory.size());
        CmsPublishJobFinished pubJob = (CmsPublishJobFinished)pubHistory.get(0);
        assertEquals(1, pubJob.getSize());
        for (int i = 1; i < 10; i++) {
            pubJob = (CmsPublishJobFinished)pubHistory.get(i);
            assertEquals("pubJob: " + i, 0, pubJob.getSize());
        }

        echo("Concurrent publish resource test success");
        echo("Total runtime of concurrent test suite: " + CmsStringUtil.formatRuntime(suite.getRuntime()));
    }

    /**
     * Test concurrently publish same resource with related resources, this does the same than the GUI.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testConcurrentPublishResourceWithRelated() throws Throwable {

        int count = 10;

        CmsObject cms = getCmsObject();
        echo("Testing concurrently publish same resource with related resources and with " + count + " threads");

        String resName = "index.html";

        // touch everything, so the involved resources can be published
        cms.lockResource("/");
        cms.setDateLastModified("/", System.currentTimeMillis(), true);

        // get the list of involved files
        CmsPublishList publishList = OpenCms.getPublishManager().getPublishList(cms, cms.readResource(resName), false);
        CmsPublishList relatedList = OpenCms.getPublishManager().getRelatedResourcesToPublish(cms, publishList);
        publishList = OpenCms.getPublishManager().mergePublishLists(cms, publishList, relatedList);

        // publish directly with a single resource
        String name = "doConcurrentPublishResourceWithRelatedOperation";
        Integer[] value = new Integer[1];
        value[0] = Integer.valueOf(0);

        Object[] parameters = new Object[] {
            OpenCmsThreadedTestCaseSuite.PARAM_CMSOBJECT,
            OpenCmsThreadedTestCaseSuite.PARAM_COUNTER,
            value};
        OpenCmsThreadedTestCaseSuite suite = new OpenCmsThreadedTestCaseSuite(count, this, name, parameters);
        suite.setAllowedRuntime(20000);
        OpenCmsThreadedTestCase[] threads = suite.run();

        if (suite.getThrowable() != null) {
            throw new Exception(suite.getThrowable());
        }

        OpenCms.getPublishManager().waitWhileRunning();

        for (int i = 0; i < count; i++) {
            Throwable e = threads[i].getThrowable();
            if (e != null) {
                throw new Exception(e);
            }
        }

        List pubHistory = OpenCms.getPublishManager().getPublishHistory();
        assertEquals(10, pubHistory.size());
        CmsPublishJobFinished pubJob = (CmsPublishJobFinished)pubHistory.get(0);
        assertEquals(publishList.getFileList().size(), pubJob.getSize());
        for (int i = 1; i < 10; i++) {
            pubJob = (CmsPublishJobFinished)pubHistory.get(i);
            assertEquals("pubJob: " + i, 0, pubJob.getSize());
        }

        echo("Concurrent publish resource test success");
        echo("Total runtime of concurrent test suite: " + CmsStringUtil.formatRuntime(suite.getRuntime()));
    }
}
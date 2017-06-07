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

import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSetCustom;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestResourceFilter;

import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for the "undelete" method of the CmsObject.<p>
 */
public class TestUndelete extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestUndelete(String arg0) {

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
        suite.setName(TestUndelete.class.getName());

        suite.addTest(new TestUndelete("testUndeleteFile"));
        suite.addTest(new TestUndelete("testUndeleteFileWrong"));
        suite.addTest(new TestUndelete("testUndeleteSibling"));
        suite.addTest(new TestUndelete("testUndeleteWithACE"));
        suite.addTest(new TestUndelete("testUndeleteFolder"));
        suite.addTest(new TestUndelete("testUndeleteFolderWrong"));
        suite.addTest(new TestUndelete("testUndeleteFolderRecursive"));

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
     * Test the undelete method to undelete a file.<p>
     *
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param file the file to undelete
     *
     * @throws Throwable if something goes wrong
     */
    public static void undeleteFile(OpenCmsTestCase tc, CmsObject cms, String file) throws Throwable {

        tc.storeResources(cms, file);

        long timestamp = System.currentTimeMillis();
        cms.lockResource(file);
        cms.undeleteResource(file, false);
        cms.unlockResource(file);

        // now evaluate the result
        tc.assertFilter(cms, file, OpenCmsTestResourceFilter.FILTER_TOUCH);
        // project must be current project
        tc.assertProject(cms, file, cms.getRequestContext().getCurrentProject());
        // state must be "changed"
        tc.assertState(cms, file, CmsResource.STATE_CHANGED);
        // date last modified must be the date set in the undelete operation
        tc.assertDateLastModifiedAfter(cms, file, timestamp);
        // the user last modified must be the current user
        tc.assertUserLastModified(cms, file, cms.getRequestContext().getCurrentUser());
    }

    /**
     * Test the undelete method to undelete a single folder.<p>
     *
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param folder the folder to undelete
     *
     * @throws Throwable if something goes wrong
     */
    public static void undeleteFolder(OpenCmsTestCase tc, CmsObject cms, String folder) throws Throwable {

        tc.storeResources(cms, folder);

        long timestamp = System.currentTimeMillis();
        cms.lockResource(folder);
        cms.undeleteResource(folder, false);
        cms.unlockResource(folder);

        // now evaluate the result
        tc.assertFilter(cms, folder, OpenCmsTestResourceFilter.FILTER_TOUCH);
        // project must be current project
        tc.assertProject(cms, folder, cms.getRequestContext().getCurrentProject());
        // state must be "changed"
        tc.assertState(cms, folder, CmsResource.STATE_CHANGED);
        // date last modified must be the date set in the tough operation
        tc.assertDateLastModifiedAfter(cms, folder, timestamp);
        // the user last modified must be the current user
        tc.assertUserLastModified(cms, folder, cms.getRequestContext().getCurrentUser());

        // evaluate all subresources
        List subresources = cms.readResources(folder, CmsResourceFilter.ALL);

        // iterate through the subresources
        Iterator i = subresources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            String resName = cms.getSitePath(res);
            tc.assertFilter(cms, resName, OpenCmsTestResourceFilter.FILTER_EQUAL);
        }
    }

    /**
     * Test the undelete method to undelete a complete subtree.<p>
     *
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param folder the folder to undelete
     *
     * @throws Throwable if something goes wrong
     */
    public static void undeleteFolderRecursive(OpenCmsTestCase tc, CmsObject cms, String folder) throws Throwable {

        tc.storeResources(cms, folder);

        long timestamp = System.currentTimeMillis();
        cms.lockResource(folder);
        cms.undeleteResource(folder, true);
        cms.unlockResource(folder);

        // now evaluate the result
        tc.assertFilter(cms, folder, OpenCmsTestResourceFilter.FILTER_TOUCH);
        // project must be current project
        tc.assertProject(cms, folder, cms.getRequestContext().getCurrentProject());
        // state must be "changed"
        tc.assertState(cms, folder, CmsResource.STATE_CHANGED);
        // date last modified must be the date set in the undelete operation
        tc.assertDateLastModifiedAfter(cms, folder, timestamp);
        // the user last modified must be the current user
        tc.assertUserLastModified(cms, folder, cms.getRequestContext().getCurrentUser());

        // evaluate all subresources
        List subresources = cms.readResources(folder, CmsResourceFilter.ALL);

        // iterate through the subresources
        Iterator i = subresources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            String resName = cms.getSitePath(res);
            // now evaluate the result
            tc.assertFilter(cms, resName, OpenCmsTestResourceFilter.FILTER_TOUCH);
            // project must be current project
            tc.assertProject(cms, resName, cms.getRequestContext().getCurrentProject());
            // state must be "changed"
            tc.assertState(cms, resName, CmsResource.STATE_CHANGED);
            // date last modified must be the date set in the undelete operation
            tc.assertDateLastModifiedAfter(cms, resName, timestamp);
            // the user last modified must be the current user
            tc.assertUserLastModified(cms, resName, cms.getRequestContext().getCurrentUser());
        }
    }

    /**
     * Test the undelete method on a file.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndeleteFile() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing undelete on file");

        String resourceName = "/index.html";
        cms.lockResource(resourceName);
        cms.deleteResource(resourceName, CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.unlockResource(resourceName);

        undeleteFile(this, cms, resourceName);
    }

    /**
     * Test the undelete method on a file.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndeleteWithACE() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing undelete on file");

        String resourceName = "/index.html";

        // set some permissions
        cms.lockResource(resourceName);
        cms.chacc(resourceName, I_CmsPrincipal.PRINCIPAL_GROUP, "group2", "+r-w");

        storeResources(cms, resourceName);

        cms.deleteResource(resourceName, CmsResource.DELETE_PRESERVE_SIBLINGS);

        long timestamp = System.currentTimeMillis();
        cms.undeleteResource(resourceName, false);

        // now evaluate the result
        assertFilter(cms, resourceName, OpenCmsTestResourceFilter.FILTER_TOUCH);
        // project must be current project
        assertProject(cms, resourceName, cms.getRequestContext().getCurrentProject());
        // state must be "changed"
        assertState(cms, resourceName, CmsResource.STATE_CHANGED);
        // date last modified must be the date set in the undelete operation
        assertDateLastModifiedAfter(cms, resourceName, timestamp);
        // the user last modified must be the current user
        assertUserLastModified(cms, resourceName, cms.getRequestContext().getCurrentUser());
        // test the acl
        assertAcl(cms, resourceName, cms.readGroup("group2").getId(), new CmsPermissionSetCustom("+r-w"));
    }

    /**
     * Test the undelete method on a not deleted file.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndeleteFileWrong() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing undelete on a not deleted file");

        String resourceName = "/index.html";
        cms.lockResource(resourceName);
        try {
            cms.undeleteResource(resourceName, false);
            fail("it should not be possible to undelete a not deleted file");
        } catch (CmsVfsException e) {
            // ok
        }
    }

    /**
     * Test the undelete method on a folder.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndeleteFolder() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing undelete on a folder (without recursion)");

        String folderName = "/folder1/";
        cms.lockResource(folderName);
        cms.deleteResource(folderName, CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.unlockResource(folderName);

        undeleteFolder(this, cms, folderName);
    }

    /**
     * Test the undelete method on a folder and recusivly on all resources in the folder.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndeleteFolderRecursive() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing undelete on a folder (_with_ recursion)");

        String folderName = "/folder2/";
        cms.lockResource(folderName);
        cms.deleteResource(folderName, CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.unlockResource(folderName);

        undeleteFolderRecursive(this, cms, folderName);
    }

    /**
     * Test the undelete method on a not deleted folder.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndeleteFolderWrong() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing undelete on a not deleted folder (without recursion)");

        String folderName = "/folder1/";
        cms.lockResource(folderName);
        try {
            cms.undeleteResource(folderName, false);
            fail("it should not be possible to undelete a not deleted folder");
        } catch (CmsVfsException e) {
            // okt
        }
        try {
            cms.undeleteResource(folderName, true);
            fail("it should not be possible to undelete a not deleted folder");
        } catch (CmsVfsException e) {
            // ok
        }
    }

    /**
     * Test the undelete method on a sibling.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndeleteSibling() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing undelete on file");

        String resourceName = "/index.html";
        String siblingName = "/index_sibling.html";

        cms.lockResource(resourceName);
        cms.copyResource(resourceName, siblingName, CmsResource.COPY_AS_SIBLING);
        cms.unlockResource(siblingName);

        OpenCms.getPublishManager().publishResource(cms, siblingName);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.lockResource(resourceName);
        cms.deleteResource(resourceName, CmsResource.DELETE_REMOVE_SIBLINGS);
        cms.unlockResource(resourceName);

        storeResources(cms, siblingName);
        long timestamp = System.currentTimeMillis();

        undeleteFile(this, cms, resourceName);

        // now evaluate the result
        assertFilter(cms, siblingName, OpenCmsTestResourceFilter.FILTER_TOUCH);
        // project must be current project
        assertProject(cms, siblingName, cms.getRequestContext().getCurrentProject());
        // state must still be "deleted"
        assertState(cms, siblingName, CmsResource.STATE_DELETED);
        // date last modified must be the date set in the undelete operation
        assertDateLastModifiedAfter(cms, siblingName, timestamp);
        // the user last modified must be the current user
        assertUserLastModified(cms, siblingName, cms.getRequestContext().getCurrentUser());
    }
}

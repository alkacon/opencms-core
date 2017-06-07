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

import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.lock.CmsLockType;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategoryService;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestResourceConfigurableFilter;
import org.opencms.test.OpenCmsTestResourceFilter;

import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for copy operation.<p>
 */
public class TestCopy extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCopy(String arg0) {

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
        suite.setName(TestCopy.class.getName());

        suite.addTest(new TestCopy("testCopyFolderRecursive"));
        suite.addTest(new TestCopy("testCopySingleResourceAsNew"));
        suite.addTest(new TestCopy("testCopyFolderDateIssue"));
        suite.addTest(new TestCopy("testCopyFolderAsNew"));
        suite.addTest(new TestCopy("testCopyOverwriteDeletedFile"));
        suite.addTest(new TestCopy("testCopyOverwriteLockedDeletedFile"));
        suite.addTest(new TestCopy("testCopyFolderWithLockedSibling"));
        suite.addTest(new TestCopy("testCopyCategories"));
        suite.addTest(new TestCopy("testCopySiblings"));

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
     * Tests the copy of a resource with assigned categories.<p>
     *
     * @throws Exception if the test fails
     */
    public void testCopyCategories() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing copy of a resource with assigned categories");

        cms.getRequestContext().setSiteRoot("");
        cms.createResource("/system/categories/", CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.createResource("/system/categories/test/", CmsResourceTypeFolder.RESOURCE_TYPE_ID);

        String resname = "testCopyCategories.txt";
        String category = "test";
        cms.createResource(resname, CmsResourceTypePlain.getStaticTypeId());
        CmsCategoryService.getInstance().addResourceToCategory(cms, resname, category);

        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, category, true, null).contains(
                cms.readResource(resname)));
        assertTrue(
            CmsCategoryService.getInstance().readResourceCategories(cms, resname).contains(
                CmsCategoryService.getInstance().readCategory(cms, category, null)));

        String copyname = "testCopyCategories2.txt";
        cms.copyResource(resname, copyname);

        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, category, true, null).contains(
                cms.readResource(resname)));
        assertTrue(
            CmsCategoryService.getInstance().readResourceCategories(cms, resname).contains(
                CmsCategoryService.getInstance().readCategory(cms, category, null)));

        assertTrue(
            CmsCategoryService.getInstance().readCategoryResources(cms, category, true, null).contains(
                cms.readResource(copyname)));
        assertTrue(
            CmsCategoryService.getInstance().readResourceCategories(cms, copyname).contains(
                CmsCategoryService.getInstance().readCategory(cms, category, null)));
    }

    /**
     * Tests the "copy a folder as new" operation.<p>
     *
     * @throws Exception if the test fails
     */
    public void testCopyFolderAsNew() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing copy of a folder as new (i.e. no siblings)");

        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("testproject"));

        String source = "/folder2/";
        String destination = "/folder2_copy/";
        long timestamp = System.currentTimeMillis();

        storeResources(cms, source);
        cms.copyResource(source, destination, CmsResource.COPY_AS_NEW);

        List subresources;
        Iterator i;

        subresources = cms.readResources(source, CmsResourceFilter.ALL);

        // iterate through the subresources
        i = subresources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            String resName = cms.getSitePath(res);
            assertFilter(cms, resName, OpenCmsTestResourceFilter.FILTER_EQUAL);
        }

        subresources = cms.readResources(destination, CmsResourceFilter.ALL);
        setMapping(destination, source);

        // prepare filter without sibling count
        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter(
            OpenCmsTestResourceFilter.FILTER_COPY_FOLDER);

        filter.disableSiblingCountTest();

        // iterate through the subresources
        i = subresources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            String resName = cms.getSitePath(res);

            // project must be current project
            assertProject(cms, resName, cms.getRequestContext().getCurrentProject());
            // state must be "new"
            assertState(cms, resName, CmsResource.STATE_NEW);
            // date created must be new
            assertDateCreatedAfter(cms, resName, timestamp);
            // user created must be current user
            assertUserCreated(cms, resName, cms.getRequestContext().getCurrentUser());
            // assert lock state
            assertLock(cms, resName);
            // must have sibling count of 1
            assertSiblingCount(cms, resName, 1);
            // now assert the filter for the rest of the attributes
            assertFilter(cms, resName, filter);
        }
    }

    /**
     * Tests the copy operation for a folder, checking if the date of the new folder is the current date.<p>
     *
     * @throws Exception if the test fails
     */
    public void testCopyFolderDateIssue() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing copy operation for a folder, checking if the date of the new folder is the current date");

        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("testproject"));

        String source = "/xmlcontent/";
        String destination = "/xmlcontent_copy/";
        long timestamp = System.currentTimeMillis();

        storeResources(cms, source);
        cms.copyResource(source, destination);

        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_EQUAL);

        // project must be current project
        assertProject(cms, destination, cms.getRequestContext().getCurrentProject());
        // state must be "new"
        assertState(cms, destination, CmsResource.STATE_NEW);
        // date created must be new
        assertDateCreatedAfter(cms, destination, timestamp);
        // for a copied folder, also the date last modified must be new
        assertDateLastModifiedAfter(cms, destination, timestamp);
        // user created must be current user
        assertUserCreated(cms, destination, cms.getRequestContext().getCurrentUser());
        // user last modified must be current user
        assertUserLastModified(cms, destination, cms.getRequestContext().getCurrentUser());
        // assert lock state
        assertLock(cms, destination, CmsLockType.EXCLUSIVE);
        // now assert the filter for the rest of the attributes
        setMapping(destination, source);
        assertFilter(cms, destination, OpenCmsTestResourceFilter.FILTER_COPY_FOLDER);
    }

    /**
     * Tests the "copy a folder recursive" bug.<p>
     *
     * @throws Exception if the test fails
     */
    public void testCopyFolderRecursive() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to copy a folder into itself");

        String source = "/folder1/";
        String destination = "/folder1/subfolder11/";

        String file = "folder1";
        try {
            cms.createResource(file, CmsResourceTypePlain.getStaticTypeId());
            fail("it should not be possible to create a file with the same name as a folder");
            cms.copyResource(file, destination + file);
        } catch (CmsVfsResourceAlreadyExistsException e) {
            // ok
        }

        try {
            cms.copyResource(source, destination);
            fail("it should not be possible to copy a folder into itself");
        } catch (CmsVfsException e) {
            // ok
        }

        String newFolder = "/folder1bla/";
        cms.createResource(newFolder, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        try {
            cms.copyResource(source, newFolder + "test");
        } catch (Exception e) {
            fail("/folder1bla/ should not be considered subfolder of /folder1/");
        }
    }

    /**
     * Tests to copy a folder with a (from other user) locked sibling.<p>
     *
     * @throws Exception if the test fails
     */
    public void testCopyFolderWithLockedSibling() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing coping a folder with a (from other user) locked sibling");

        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("testproject"));

        String sourceFolder = "/folder1";
        String file = "/index.html";

        cms.lockResource(sourceFolder + file);

        // switch user
        CmsUser user = cms.getRequestContext().getCurrentUser();
        cms = getCmsObject();

        assertLock(cms, sourceFolder + file, CmsLockType.EXCLUSIVE, user);

        String destinationFolder = "/folder1_copy";
        cms.copyResource(sourceFolder, destinationFolder, CmsResource.COPY_AS_SIBLING);
        assertLock(cms, sourceFolder + file, CmsLockType.EXCLUSIVE, user);
        assertLock(cms, destinationFolder + file, CmsLockType.SHARED_INHERITED, user);

        String destinationFolder2 = "/folder1_copy2";
        cms.copyResource(sourceFolder, destinationFolder2, CmsResource.COPY_PRESERVE_SIBLING);
        assertLock(cms, sourceFolder + file, CmsLockType.EXCLUSIVE, user);
        assertLock(cms, destinationFolder + file, CmsLockType.SHARED_INHERITED, user);
        assertLock(cms, destinationFolder2 + file, CmsLockType.SHARED_INHERITED, user);

        cms.unlockResource(destinationFolder);
        assertLock(cms, sourceFolder + file, CmsLockType.EXCLUSIVE, user);
        assertLock(cms, destinationFolder + file, CmsLockType.SHARED_EXCLUSIVE, user);
        assertLock(cms, destinationFolder2 + file, CmsLockType.SHARED_INHERITED, user);

        cms.unlockResource(destinationFolder2);
        assertLock(cms, sourceFolder + file, CmsLockType.EXCLUSIVE, user);
        assertLock(cms, destinationFolder + file, CmsLockType.SHARED_EXCLUSIVE, user);
        assertLock(cms, destinationFolder2 + file, CmsLockType.SHARED_EXCLUSIVE, user);
    }

    /**
     * Tests the to copy a single resource to a destination that already exists but is
     * marked as deleted.<p>
     *
     * @throws Exception if the test fails
     */
    public void testCopyOverwriteDeletedFile() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing overwriting a deleted file");

        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("testproject"));

        String source1 = "/folder1/page2.html";
        String source2 = "/folder1/image1.gif";
        String source3 = "/folder1/page3.html";
        String destination = "/folder1/page1.html";

        storeResources(cms, source1);
        storeResources(cms, source2);
        storeResources(cms, source3);
        storeResources(cms, destination);

        cms.lockResource(destination);

        // delete and overwrite with a sibling of source 1
        cms.deleteResource(destination, CmsResource.DELETE_PRESERVE_SIBLINGS);
        assertState(cms, destination, CmsResource.STATE_DELETED);

        cms.copyResource(source1, destination, CmsResource.COPY_AS_SIBLING);

        assertState(cms, destination, CmsResource.STATE_CHANGED);
        assertSiblingCount(cms, destination, 2);
        assertLock(cms, destination, CmsLockType.EXCLUSIVE);

        assertSiblingCountIncremented(cms, source1, 1);
        assertLock(cms, source1, CmsLockType.SHARED_EXCLUSIVE);

        assertFilter(cms, source1, OpenCmsTestResourceFilter.FILTER_EXISTING_SIBLING);
        assertFilter(cms, source1, destination, OpenCmsTestResourceFilter.FILTER_COPY_SOURCE_DESTINATION_AS_SIBLING);

        // delete again and owerwrite with a sibling of source 2
        cms.deleteResource(destination, CmsResource.DELETE_PRESERVE_SIBLINGS);
        assertState(cms, destination, CmsResource.STATE_DELETED);

        cms.copyResource(source2, destination, CmsResource.COPY_AS_SIBLING);

        assertSiblingCountIncremented(cms, source1, 0);
        assertLock(cms, source1, CmsLockType.UNLOCKED);

        assertState(cms, destination, CmsResource.STATE_CHANGED);
        assertSiblingCount(cms, destination, 2);
        assertLock(cms, destination, CmsLockType.EXCLUSIVE);

        assertSiblingCountIncremented(cms, source2, 1);
        assertLock(cms, source2, CmsLockType.SHARED_EXCLUSIVE);

        assertFilter(cms, source1, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
        assertFilter(cms, source2, OpenCmsTestResourceFilter.FILTER_EXISTING_SIBLING);
        assertFilter(cms, source2, destination, OpenCmsTestResourceFilter.FILTER_COPY_SOURCE_DESTINATION_AS_SIBLING);

        cms.deleteResource(destination, CmsResource.DELETE_PRESERVE_SIBLINGS);
        assertState(cms, destination, CmsResource.STATE_DELETED);

        // delete yet again and overwrite with content of source 3 (not a sibling)
        cms.copyResource(source3, destination, CmsResource.COPY_AS_NEW);

        assertSiblingCountIncremented(cms, source1, 0);
        assertLock(cms, source1, CmsLockType.UNLOCKED);
        assertSiblingCountIncremented(cms, source2, 0);
        assertLock(cms, source2, CmsLockType.UNLOCKED);

        assertState(cms, destination, CmsResource.STATE_CHANGED);
        assertSiblingCount(cms, destination, 1);
        assertSiblingCount(cms, source3, 1);
        assertLock(cms, destination, CmsLockType.EXCLUSIVE);

        assertFilter(cms, source1, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
        assertFilter(cms, source2, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
        assertFilter(cms, source3, OpenCmsTestResourceFilter.FILTER_EQUAL);
        assertFilter(cms, source3, destination, OpenCmsTestResourceFilter.FILTER_COPY_FILE_AS_NEW);
    }

    /**
     * Tests the to copy a single resource to a destination that already exists but is
     * marked as deleted and locked by another user.<p>
     *
     * @throws Exception if the test fails
     */
    public void testCopyOverwriteLockedDeletedFile() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to create a file over a deleted one");

        // Creating paths
        String deletedFile = "/folder1/page1.html";
        String sourceFile = "/folder1/page2.html";

        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("testproject"));

        cms.changeLock(deletedFile);
        cms.deleteResource(deletedFile, CmsResource.DELETE_PRESERVE_SIBLINGS);

        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(cms.readProject("testproject"));
        try {
            cms.copyResource(sourceFile, deletedFile);
            fail("should fail to copy a file over a locked deleted one");
        } catch (Exception e) {
            // ok
        }
    }

    /**
     * Tests the copy of siblings.<p>
     *
     * @throws Exception if the test fails
     */
    public void testCopySiblings() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing copy of siblings");

        String folder = "copySiblings/";
        String res1 = folder + "sib1.txt";
        String res2 = folder + "sib2.txt";
        String folder2 = "copySiblings2/";
        byte[] content = "hello world".getBytes();

        cms.createResource(folder, CmsResourceTypeFolder.getStaticTypeId());
        cms.createResource(res1, CmsResourceTypePlain.getStaticTypeId(), content, null);
        cms.createSibling(res1, res2, null);

        OpenCms.getPublishManager().publishResource(cms, folder);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.copyResource(folder, folder2, CmsResource.COPY_PRESERVE_SIBLING);
        OpenCms.getPublishManager().publishResource(cms, folder2);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.lockResource(folder);
        cms.deleteResource(folder, CmsResource.DELETE_PRESERVE_SIBLINGS);
        OpenCms.getPublishManager().publishResource(cms, folder);
        OpenCms.getPublishManager().waitWhileRunning();

        String sib1 = folder2 + "sib1.txt";
        String sib2 = folder2 + "sib2.txt";
        assertContent(cms, sib1, content);
        assertContent(cms, sib2, content);
    }

    /**
     * Tests the "copy single resource as new" operation.<p>
     *
     * @throws Exception if the test fails
     */
    public void testCopySingleResourceAsNew() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing copy of a file as new");

        // create a project for all tests in this suite
        cms.createProject("testproject", "a test project", "Users", "Users", CmsProject.PROJECT_TYPE_NORMAL);
        cms.copyResourceToProject("/");
        cms.addUserToGroup("test1", OpenCms.getDefaultUsers().getGroupAdministrators());

        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("testproject"));

        String source = "/index.html";
        String destination = "/index_copy.html";
        long timestamp = System.currentTimeMillis();

        CmsUser admin = cms.readUser("Admin");
        // some assertions about the original state of the resource
        assertUserCreated(cms, source, admin);
        assertUserLastModified(cms, source, admin);
        CmsResource original = cms.readResource(source);

        storeResources(cms, source);
        cms.copyResource(source, destination);

        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_EQUAL);

        // project must be current project
        assertProject(cms, destination, cms.getRequestContext().getCurrentProject());
        // state must be "new"
        assertState(cms, destination, CmsResource.STATE_NEW);
        // date created must be new
        assertDateCreatedAfter(cms, destination, timestamp);
        // date last modified must be original date
        assertDateLastModified(cms, destination, original.getDateLastModified());
        // user created must be current user
        assertUserCreated(cms, destination, cms.getRequestContext().getCurrentUser());
        // user last modified must be original user
        assertUserLastModified(cms, destination, cms.readUser(original.getUserLastModified()));
        // now assert the filter for the rest of the attributes
        setMapping(destination, source);
        assertFilter(cms, destination, OpenCmsTestResourceFilter.FILTER_COPY_FILE_AS_NEW);
        // assert lock state
        assertLock(cms, destination, CmsLockType.EXCLUSIVE);
    }
}
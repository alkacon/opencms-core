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

import org.opencms.db.CmsVfsOnlineResourceAlreadyExistsException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.lock.CmsLockFilter;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsPermalinkResourceHandler;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionViolationException;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestResourceFilter;

import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for move/rename operation.<p>
 */
public class TestMoveRename extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestMoveRename(String arg0) {

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
        suite.setName(TestMoveRename.class.getName());

        suite.addTest(new TestMoveRename("testMoveToDeletedFolder"));
        suite.addTest(new TestMoveRename("testPublishDeletedFolderWithMovedResource"));
        suite.addTest(new TestMoveRename("testPermaLink"));
        suite.addTest(new TestMoveRename("testMoveSingleResource"));
        suite.addTest(new TestMoveRename("testMoveSingleNewResource"));
        suite.addTest(new TestMoveRename("testMultipleMoveResource"));
        suite.addTest(new TestMoveRename("testMoveFolderToOwnSubfolder"));
        suite.addTest(new TestMoveRename("testOverwriteMovedResource"));
        suite.addTest(new TestMoveRename("testMoveTargetWithoutPermissions"));
        suite.addTest(new TestMoveRename("testMoveDeleted"));
        suite.addTest(new TestMoveRename("testMoveDeletedWithSubfolders"));
        suite.addTest(new TestMoveRename("testMoveSourceWithoutReadPermissions"));
        suite.addTest(new TestMoveRename("testMoveSourceWithoutWritePermissions"));

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
     * Tests to move a folder with deleted subresources.<p>
     *
     * @throws Exception if the test fails
     */
    public void testMoveDeleted() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to move a folder with deleted subresources");

        // Creating paths
        String folder = "/testMoveDeleted/";
        String destinationFolder = "/testMoveDeleted2/";
        String file = "index.html";

        // create the resources
        cms.createResource(folder, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.createResource(folder + file, CmsResourceTypePlain.getStaticTypeId());

        // publish
        OpenCms.getPublishManager().publishResource(cms, folder);
        OpenCms.getPublishManager().waitWhileRunning();

        // delete the file
        cms.lockResource(folder);
        cms.deleteResource(folder + file, CmsResource.DELETE_PRESERVE_SIBLINGS);

        storeResources(cms, folder, true);

        // now move the folder
        cms.moveResource(folder, destinationFolder);

        // assert the folder

        // project must be current project
        assertProject(cms, destinationFolder, cms.getRequestContext().getCurrentProject());
        // state must be "changed"
        assertState(cms, destinationFolder, CmsResource.STATE_CHANGED);
        // assert lock state
        assertLock(cms, destinationFolder, CmsLockType.EXCLUSIVE);

        // set filter mapping
        setMapping(destinationFolder, folder);
        // now assert the filter for the rest of the attributes
        assertFilter(cms, destinationFolder, OpenCmsTestResourceFilter.FILTER_MOVE_DESTINATION);

        // assert the file

        // project must be current project
        assertProject(cms, destinationFolder + file, cms.getRequestContext().getCurrentProject());
        // state must still be "deleted"
        assertState(cms, destinationFolder + file, CmsResource.STATE_DELETED);
        // assert lock state
        assertLock(cms, destinationFolder + file, CmsLockType.INHERITED);
        // now assert the filter for the rest of the attributes
        assertFilter(cms, destinationFolder + file, OpenCmsTestResourceFilter.FILTER_MOVE_DESTINATION);
    }

    /**
     * Tests to move a folder with deleted sub-resources which are non-empty folders.<p>
     *
     * @throws Exception if the test fails
     */
    public void testMoveDeletedWithSubfolders() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to move a folder with deleted sub-resources which are non-empty folders");

        // Creating paths
        String folder1 = "/testMoveDeletedWithSub/";
        String folder2 = "/testMoveDeletedWithSub/folders/";
        String file = "index.html";

        // create the resources
        cms.createResource(folder1, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.createResource(folder1 + file, CmsResourceTypePlain.getStaticTypeId());
        cms.createResource(folder2, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.createResource(folder2 + file, CmsResourceTypePlain.getStaticTypeId());

        // publish
        OpenCms.getPublishManager().publishResource(cms, folder1);
        OpenCms.getPublishManager().waitWhileRunning();

        // delete the file
        cms.lockResource(folder1);
        cms.deleteResource(folder2, CmsResource.DELETE_PRESERVE_SIBLINGS);

        storeResources(cms, folder1, true);

        // now move the folder
        String destinationFolder1 = "/testMoveDeletedWithSubChanged/";
        String destinationFolder2 = "/testMoveDeletedWithSubChanged/folders/";
        cms.moveResource(folder1, destinationFolder1);

        // assert the folder

        // project must be current project
        assertProject(cms, destinationFolder1, cms.getRequestContext().getCurrentProject());
        // state must be "changed"
        assertState(cms, destinationFolder1, CmsResource.STATE_CHANGED);
        // assert lock state
        assertLock(cms, destinationFolder1, CmsLockType.EXCLUSIVE);

        // set filter mapping
        setMapping(destinationFolder1, folder1);
        // now assert the filter for the rest of the attributes
        assertFilter(cms, destinationFolder1, OpenCmsTestResourceFilter.FILTER_MOVE_DESTINATION);

        // assert the file

        // project must be current project
        assertProject(cms, destinationFolder1 + file, cms.getRequestContext().getCurrentProject());
        // state must still be "deleted"
        assertState(cms, destinationFolder1 + file, CmsResource.STATE_CHANGED);
        assertState(cms, destinationFolder1 + file, CmsResource.STATE_CHANGED);
        assertState(cms, destinationFolder2, CmsResource.STATE_DELETED);
        assertState(cms, destinationFolder2 + file, CmsResource.STATE_DELETED);
        // assert lock state
        assertLock(cms, destinationFolder1 + file, CmsLockType.INHERITED);
        assertLock(cms, destinationFolder2 + file, CmsLockType.INHERITED);
        // now assert the filter for the rest of the attributes
        assertFilter(cms, destinationFolder1 + file, OpenCmsTestResourceFilter.FILTER_MOVE_DESTINATION);
        assertFilter(cms, destinationFolder2 + file, OpenCmsTestResourceFilter.FILTER_MOVE_DESTINATION);
    }

    /**
     * Tests to move a folder in its own subfolder.<p>
     *
     * @throws Exception if the test fails
     */
    public void testMoveFolderToOwnSubfolder() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to move a folder in its own subfolder");

        // Creating paths
        String source = "/folder1/";
        String destination = "/folder1/subfolder11/folder1/";

        cms.lockResource(source);
        try {
            // moving a folder to it's own subfolder must cause an exception
            cms.moveResource(source, destination);
            fail("to move a folder in its own subfolder is not allowed");
        } catch (CmsVfsException e) {
            assertSame(e.getMessageContainer().getKey(), Messages.ERR_MOVE_SAME_FOLDER_2);
        }
    }

    /**
     * Tests the "move a single new resource" operation.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testMoveSingleNewResource() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing move of a new file");

        String source = "/folder1/new.html";
        String destination = "/folder1/new_move.html";

        // create a new, plain resource
        cms.createResource(source, CmsResourceTypePlain.getStaticTypeId());
        assertLock(cms, source, CmsLockType.EXCLUSIVE);

        storeResources(cms, source);

        cms.moveResource(source, destination);

        // check lock
        assertFalse(cms.getLockedResources("/folder1", CmsLockFilter.FILTER_ALL).contains(source));

        // source resource must be gone
        try {
            cms.readResource(source, CmsResourceFilter.ALL);
            fail("New resource still available after move operation!");
        } catch (CmsVfsResourceNotFoundException e) {
            // this is expected
        }

        // destination resource

        // project must be current project
        assertProject(cms, destination, cms.getRequestContext().getCurrentProject());
        // state must be "changed"
        assertState(cms, destination, CmsResource.STATE_NEW);
        // assert lock state
        assertLock(cms, destination, CmsLockType.EXCLUSIVE);
        // set filter mapping
        setMapping(destination, source);
        // now assert the filter for the rest of the attributes
        assertFilter(cms, destination, OpenCmsTestResourceFilter.FILTER_MOVE_DESTINATION);
    }

    /**
     * Tests the "move single resource" operation.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testMoveSingleResource() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing move of a file");

        String source = "/folder1/index.html";
        String destination = "/folder1/index_move.html";

        storeResources(cms, source);

        cms.lockResource(source);
        cms.moveResource(source, destination);

        try {
            // source resource must be gone
            cms.readResource(source, CmsResourceFilter.ALL);
            fail("source resource still there");
        } catch (CmsException e) {
            // ok
        }

        // check lock
        assertFalse(cms.getLockedResources("/folder1", CmsLockFilter.FILTER_ALL).contains(source));

        // project must be current project
        assertProject(cms, destination, cms.getRequestContext().getCurrentProject());
        // state must be "changed"
        assertState(cms, destination, CmsResource.STATE_CHANGED);
        // assert lock state
        assertLock(cms, destination, CmsLockType.EXCLUSIVE);
        // set filter mapping
        setMapping(destination, source);
        // now assert the filter for the rest of the attributes
        assertFilter(cms, destination, OpenCmsTestResourceFilter.FILTER_MOVE_DESTINATION);

        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Tests to move a folder with no read permission on a source subresource.<p>
     *
     * @throws Exception if the test fails
     */
    public void testMoveSourceWithoutReadPermissions() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to move a folder with no read permission on a source subresource");

        // Creating paths
        String source = "/folder1/";
        String folder = "/mytestfolder4/";
        String test = "subfolder11/";
        String destinationFolder = "/mytestfolder5/";

        cms.copyResource(source, folder);
        cms.copyResource(source, folder + test);

        List<CmsResource> list = cms.readResources(folder, CmsResourceFilter.ALL, true);
        int files = list.size();

        // remove read permission for test2, this should not be a problem when moving
        cms.chacc(folder, I_CmsPrincipal.PRINCIPAL_USER, "test2", "+r+w+v+i");
        cms.chacc(folder + test, I_CmsPrincipal.PRINCIPAL_USER, "test2", "-r+w+v+i");
        cms.unlockResource(folder);

        // login as test2
        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        cms.lockResource(folder);
        // move the folder
        cms.moveResource(folder, destinationFolder);

        // login as Admin for testing
        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        assertEquals(
            "there missing files after moving",
            files,
            cms.readResources(destinationFolder, CmsResourceFilter.ALL, true).size());
    }

    /**
     * Tests to move a folder with no write permission on a source subresource.<p>
     *
     * @throws Exception if the test fails
     */
    public void testMoveSourceWithoutWritePermissions() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to move a folder with no write permission on a source subresource");

        // Creating paths
        String source = "/folder1/";
        String folder = "/mytestfolder6/";
        String test = "subfolder11/";
        String destinationFolder = "/mytestfolder7/";

        cms.copyResource(source, folder);
        cms.copyResource(source, folder + test);

        List<CmsResource> list = cms.readResources(folder, CmsResourceFilter.ALL, true);
        int files = list.size();

        // remove read permission for test2
        cms.chacc(folder, I_CmsPrincipal.PRINCIPAL_USER, "test2", "+r+w+v+i");
        cms.chacc(folder + test, I_CmsPrincipal.PRINCIPAL_USER, "test2", "+r-w+v+i");
        cms.unlockResource(folder);

        // login as test2
        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        cms.lockResource(folder);
        // try move the folder
        try {
            cms.moveResource(folder, destinationFolder);
            fail("to move a resource with no write permission on a source subresource should fail");
        } catch (CmsPermissionViolationException e) {
            // ok
        }

        // login as Admin for testing
        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        assertEquals(
            "there missing files after moving",
            files,
            cms.readResources(folder, CmsResourceFilter.ALL, true).size());
    }

    /**
     * Tests to move a resource with no write permission on the destination folder.<p>
     *
     * @throws Exception if the test fails
     */
    public void testMoveTargetWithoutPermissions() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to move a resource with no write permission on the destination folder");

        // Creating paths
        String folder = "/mytestfolder3/";
        String destinationFolder = "/folder1/";
        String file = "index3.html";

        // create the new files as test2
        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        cms.createResource(folder, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.createResource(folder + file, CmsResourceTypePlain.getStaticTypeId());

        // login as Admin
        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        // remove write permission for test2
        cms.lockResource(destinationFolder);
        cms.chacc(destinationFolder, I_CmsPrincipal.PRINCIPAL_USER, "test2", "+r-w+v+i");
        cms.unlockResource(destinationFolder);

        // login again as test2
        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        cms.lockResource(folder + file);
        // move the file
        try {
            cms.moveResource(folder + file, destinationFolder + file);
            fail("to move a resource with no write permission on the destination folder should fail");
        } catch (CmsPermissionViolationException e) {
            // ok
        }
    }

    /**
     * Tests to move a file into an as deleted marked folder.<p>
     *
     * @throws Exception if the test fails
     */
    public void testMoveToDeletedFolder() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to move a file into an as deleted marked folder");

        String deletedFolder = "/folder1/subfolder11/";
        String file = "index.html";

        cms.lockResource(deletedFolder);
        cms.deleteResource(deletedFolder, CmsResource.DELETE_PRESERVE_SIBLINGS);

        cms.lockResource(file);
        try {
            // moving a file to a deleted folder must cause an exception
            cms.moveResource(file, deletedFolder + "abc.html");
            fail("moving a file to a deleted folder must cause an exception");
        } catch (CmsVfsResourceNotFoundException e) {
            // ok
        }

        // restore the starting state
        cms.undoChanges(deletedFolder, CmsResource.UNDO_CONTENT_RECURSIVE);
        cms.unlockResource(deletedFolder);
        cms.unlockResource(file);
    }

    /**
     * Tests a "multiple move" on a resource.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testMultipleMoveResource() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing multiple move of a resource");

        String source = "/folder1/page1.html";
        String destination1 = "/folder1/page1_move.html";
        String destination2 = "/page1_move.html";

        storeResources(cms, source);

        cms.lockResource(source);
        cms.moveResource(source, destination1);

        // check lock
        assertFalse(cms.getLockedResources("/folder1", CmsLockFilter.FILTER_ALL).contains(source));

        cms.moveResource(destination1, destination2);

        // check lock
        assertFalse(cms.getLockedResources("/folder1", CmsLockFilter.FILTER_ALL).contains(destination1));

        // source resource:
        try {
            // source resource must be gone
            cms.readResource(source, CmsResourceFilter.ALL);
            fail("source resource still there");
        } catch (CmsException e) {
            // ok
        }

        // destination1 resource:
        try {
            // source resource must be gone
            cms.readResource(destination1, CmsResourceFilter.ALL);
            fail("source resource still there");
        } catch (CmsException e) {
            // ok
        }

        // destination2 resource

        // project must be current project
        assertProject(cms, destination2, cms.getRequestContext().getCurrentProject());
        // state must be "changed"
        assertState(cms, destination2, CmsResource.STATE_CHANGED);
        // assert lock state
        assertLock(cms, destination2, CmsLockType.EXCLUSIVE);
        // set filter mapping
        setMapping(destination2, source);
        // now assert the filter for the rest of the attributes
        assertFilter(cms, destination2, OpenCmsTestResourceFilter.FILTER_MOVE_DESTINATION);

        // just for fun try to undo changes on the source resource
        resetMapping();
        cms.undoChanges(destination2, CmsResource.UNDO_MOVE_CONTENT);
        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);

        // check lock
        assertFalse(cms.getLockedResources("/", CmsLockFilter.FILTER_ALL).contains(destination2));
    }

    /**
     * Tests overwriting a moved resource.<p>
     *
     * @throws Exception if the test fails
     */
    public void testOverwriteMovedResource() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing overwritting a moved resource by other resource");

        String originalResource = "/xmlcontent/article_0001.html";
        String copySource = "/xmlcontent/article_0002.html";
        String intermediaryDestination = "/xmlcontent/article_0001_new.html";
        String finalDestination = "/xmlcontent/article_0001_new2.html";

        // move the resource
        cms.lockResource(originalResource);

        cms.moveResource(originalResource, intermediaryDestination);

        // check lock
        assertFalse(cms.getLockedResources("/xmlcontent", CmsLockFilter.FILTER_ALL).contains(originalResource));

        cms.moveResource(intermediaryDestination, finalDestination);

        // check lock
        assertFalse(cms.getLockedResources("/xmlcontent", CmsLockFilter.FILTER_ALL).contains(intermediaryDestination));

        // try to overwrite by move
        try {
            cms.lockResource(copySource);
            cms.moveResource(copySource, originalResource);
            fail(
                "creating a resource in the position of a moved resource that is not the original resource is not allowed.");
        } catch (CmsVfsOnlineResourceAlreadyExistsException e) {
            // ok
        }

        // try to overwrite by new
        try {
            cms.createResource(originalResource, CmsResourceTypePlain.getStaticTypeId());
            fail(
                "creating a resource in the position of a moved resource that is not the original resource is not allowed.");
        } catch (CmsVfsOnlineResourceAlreadyExistsException e) {
            // ok
        }

        // try to overwrite by new the first target
        try {
            cms.createResource(intermediaryDestination, CmsResourceTypePlain.getStaticTypeId());
        } catch (CmsException e) {
            fail("creating a resource here must allowed.");
        }

        // try to overwrite by copying the new file
        try {
            cms.copyResource(intermediaryDestination, originalResource);
            fail(
                "creating a resource in the position of a moved resource that is not the original resource is not allowed.");
        } catch (CmsVfsOnlineResourceAlreadyExistsException e) {
            // ok
        }

        // try to overwrite by the original file
        try {
            cms.moveResource(finalDestination, originalResource);
        } catch (CmsException e) {
            fail("creating a resource back to its original position is allowed.");
        }

        // check lock
        assertFalse(cms.getLockedResources("/xmlcontent", CmsLockFilter.FILTER_ALL).contains(finalDestination));
    }

    /**
     * Test the perma link.<p>
     *
     * @throws Throwable if the test fails
     */
    public void testPermaLink() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing the perma links");

        String filename = "/folder1/page1.html";
        String filename2 = "/folder1/page1_moved.html";
        String uri;

        CmsResource res = cms.readResource(filename);
        uri = CmsPermalinkResourceHandler.PERMALINK_HANDLER + res.getStructureId() + ".html";
        cms.getRequestContext().setUri(uri);
        CmsResource res2 = new CmsPermalinkResourceHandler().initResource(null, cms, null, null);

        assertEquals(res.getStructureId(), res2.getStructureId());
        assertEquals(res.getResourceId(), res2.getResourceId());
        assertEquals(res.getRootPath(), res2.getRootPath());

        cms.lockResource(filename);
        cms.moveResource(filename, filename2);

        // check lock
        assertFalse(cms.getLockedResources("/folder1", CmsLockFilter.FILTER_ALL).contains(filename));

        cms.getRequestContext().setUri(uri);
        CmsResource res3 = new CmsPermalinkResourceHandler().initResource(null, cms, null, null);

        assertEquals(res.getStructureId(), res3.getStructureId());
        assertEquals(res.getResourceId(), res3.getResourceId());
        assertEquals(filename2, cms.getSitePath(res3));

        cms.undoChanges(filename2, CmsResource.UNDO_MOVE_CONTENT);
    }

    /**
     * Tests to publish a deleted folder with a unpublished moved resource.<p>
     *
     * @throws Exception if the test fails
     */
    public void testPublishDeletedFolderWithMovedResource() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to publish a deleted folder with a unpublished moved resource");

        String deletedFolder = "/folder1/subfolder11/";
        String source = "index.html";
        String destination = "abc.html";

        cms.lockResource(deletedFolder + source);
        cms.moveResource(deletedFolder + source, destination);

        // check lock
        assertFalse(cms.getLockedResources(deletedFolder, CmsLockFilter.FILTER_ALL).contains(deletedFolder + source));

        cms.lockResource(deletedFolder);
        cms.deleteResource(deletedFolder, CmsResource.DELETE_PRESERVE_SIBLINGS);

        cms.unlockResource(deletedFolder);
        OpenCms.getPublishManager().publishResource(cms, deletedFolder);
        OpenCms.getPublishManager().waitWhileRunning();

        try {
            // undoing the changes to a deleted folder must cause an exception
            cms.undoChanges(destination, CmsResource.UNDO_MOVE_CONTENT);
            fail("undoing the changes to a deleted folder must cause an exception");
        } catch (CmsVfsResourceNotFoundException e) {
            // ok
        }

        cms.unlockResource(destination);
        OpenCms.getPublishManager().publishResource(cms, destination);
        OpenCms.getPublishManager().waitWhileRunning();
    }
}

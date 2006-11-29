/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestMoveRename.java,v $
 * Date   : $Date: 2006/11/29 15:04:07 $
 * Version: $Revision: 1.16.8.3 $
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

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for move/rename operation.<p>
 * 
 * @author Alexander Kandzior 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.16.8.3 $
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
        suite.addTest(new TestMoveRename("testMoveWithoutPermissions"));

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
     * Tests to move a resource with no write permission on the destination folder.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testMoveWithoutPermissions() throws Exception {

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
        assertProject(cms, destination, cms.getRequestContext().currentProject());
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
        assertProject(cms, destination, cms.getRequestContext().currentProject());
        // state must be "changed"
        assertState(cms, destination, CmsResource.STATE_CHANGED);
        // assert lock state
        assertLock(cms, destination, CmsLockType.EXCLUSIVE);
        // set filter mapping
        setMapping(destination, source);
        // now assert the filter for the rest of the attributes        
        assertFilter(cms, destination, OpenCmsTestResourceFilter.FILTER_MOVE_DESTINATION);

        cms.unlockProject(cms.getRequestContext().currentProject().getId());
        cms.publishProject();
        OpenCms.getPublishManager().waitWhileRunning();
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
        cms.undeleteResource(deletedFolder);
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

        // switch to the root context
        cms.getRequestContext().setSiteRoot("/");

        String source = "/sites/default/folder1/page1.html";
        String destination1 = "/sites/default/folder1/page1_move.html";
        String destination2 = "/page1_move.html";

        storeResources(cms, source);

        cms.lockResource(source);
        cms.moveResource(source, destination1);

        // check lock
        assertFalse(cms.getLockedResources("/sites/default/folder1", CmsLockFilter.FILTER_ALL).contains(source));

        cms.moveResource(destination1, destination2);

        // check lock
        assertFalse(cms.getLockedResources("/sites/default/folder1", CmsLockFilter.FILTER_ALL).contains(destination1));

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
        assertProject(cms, destination2, cms.getRequestContext().currentProject());
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
            fail("creating a resource in the position of a moved resource that is not the original resource is not allowed.");
        } catch (CmsVfsOnlineResourceAlreadyExistsException e) {
            // ok
        }

        // try to overwrite by new
        try {
            cms.createResource(originalResource, CmsResourceTypePlain.getStaticTypeId());
            fail("creating a resource in the position of a moved resource that is not the original resource is not allowed.");
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
            fail("creating a resource in the position of a moved resource that is not the original resource is not allowed.");
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
        cms.publishResource(deletedFolder);
        OpenCms.getPublishManager().waitWhileRunning();

        try {
            // undoing the changes to a deleted folder must cause an exception
            cms.undoChanges(destination, CmsResource.UNDO_MOVE_CONTENT);
            fail("undoing the changes to a deleted folder must cause an exception");
        } catch (CmsVfsResourceNotFoundException e) {
            // ok
        }

        cms.unlockResource(destination);
        cms.publishResource(destination);
        OpenCms.getPublishManager().waitWhileRunning();
    }
}
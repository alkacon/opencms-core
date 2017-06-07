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

import org.opencms.db.CmsResourceState;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.lock.CmsLockFilter;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestResourceFilter;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.page.CmsXmlPageFactory;

import java.util.Iterator;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for move/rename operation.<p>
 */
public class TestMoveRename2 extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestMoveRename2(String arg0) {

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
        suite.setName(TestMoveRename2.class.getName());

        suite.addTest(new TestMoveRename2("testMoveSibling"));
        suite.addTest(new TestMoveRename2("testRenameNewFolder"));
        suite.addTest(new TestMoveRename2("testRenameFileUpperLowerCase"));
        suite.addTest(new TestMoveRename2("testRenameFolderUpperLowerCase"));
        suite.addTest(new TestMoveRename2("testMoveLostAndFound"));
        suite.addTest(new TestMoveRename2("testMoveOverDeleted"));
        suite.addTest(new TestMoveRename2("testMoveFolderWithNewResource"));
        suite.addTest(new TestMoveRename2("testPublishMovedDeletedFolderWithMovedResource"));
        suite.addTest(new TestMoveRename2("testMoveFolderWithPermissionCheck"));
        suite.addTest(new TestMoveRename2("testMoveFolderWithInvisibleResources"));
        suite.addTest(new TestMoveRename2("testMoveBigFolder"));

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
     * Tests to move a big folder.<p>
     *
     * @throws Exception if the test fails
     */
    public void testMoveBigFolder() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to move a big folder");

        // Creating paths
        cms.getRequestContext().setSiteRoot("");
        String src = "/sites/default/";
        String dest = "/sites/newname/";
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        cms.lockResource(src);
        cms.moveResource(src, dest);

        Iterator<CmsResource> itResources = cms.readResources(dest, CmsResourceFilter.ALL, true).iterator();
        while (itResources.hasNext()) {
            CmsResource res = itResources.next();
            cms.readResource(res.getRootPath());
        }
    }

    /**
     * Tests to move a folder structure with invisible resources inside.<p>
     *
     * @throws Exception if the test fails
     */
    public void testMoveFolderWithInvisibleResources() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to move a folder structure with invisible resources inside");

        // Creating paths
        String folder = "/mytestfolder_moved/";
        String folder_moved = "/mytestfolder_moved2/";
        String file = "index.html";

        cms.lockResource(folder + file);
        cms.chacc(folder + file, I_CmsPrincipal.PRINCIPAL_USER, "test2", "-r+v+i");
        cms.unlockResource(folder + file);

        // lock the whole folder as test2
        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        cms.lockResource(folder);

        // move the folder
        cms.moveResource(folder, folder_moved);

        // check lock
        assertFalse(cms.getLockedResources("/", CmsLockFilter.FILTER_ALL).contains(folder));

        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        assertTrue(cms.existsResource(folder_moved + file));
    }

    /**
     * Tests renaming a folder containing a new resource.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testMoveFolderWithNewResource() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing rename a new folder with content");

        String folder = "/myfolder/";
        String movedFolder = "/myfolder_mov/";
        String file = "file.txt";
        String newFile = "newfile.txt";

        cms.createResource(folder, CmsResourceTypeFolder.getStaticTypeId());

        cms.createResource(folder + file, CmsResourceTypePlain.getStaticTypeId());

        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.createResource(folder + newFile, CmsResourceTypePlain.getStaticTypeId());

        storeResources(cms, folder, true);

        cms.lockResource(folder);
        cms.moveResource(folder, movedFolder);

        // check lock
        assertFalse(cms.getLockedResources("/", CmsLockFilter.FILTER_ALL).contains(folder));

        CmsResource res = cms.readResource(movedFolder + file);
        CmsResource newRes = cms.readResource(movedFolder + newFile);

        setMapping(movedFolder + file, folder + file);
        assertFilter(cms, res, OpenCmsTestResourceFilter.FILTER_MOVE_DESTINATION);
        assertState(cms, movedFolder + file, CmsResource.STATE_CHANGED);
        setMapping(movedFolder + newFile, folder + newFile);
        assertFilter(cms, newRes, OpenCmsTestResourceFilter.FILTER_MOVE_DESTINATION);
        assertState(cms, movedFolder + newFile, CmsResource.STATE_NEW);
    }

    /**
     * Tests to move a deep folder structure with real permission check.<p>
     *
     * @throws Exception if the test fails
     */
    public void testMoveFolderWithPermissionCheck() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to move a deep folder structure with real permission check");

        // Creating paths
        String folder = "/mytestfolder/";
        String folder_moved = "/mytestfolder_moved/";
        String file = "index.html";

        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        cms.createResource(folder, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.createResource(folder + file, CmsResourceTypePlain.getStaticTypeId());

        cms.moveResource(folder, folder_moved);
        cms.unlockResource(folder_moved);

        // check lock
        assertFalse(cms.getLockedResources("/", CmsLockFilter.FILTER_ALL).contains(folder));
    }

    /**
     * Tests the move to lost and found folder operation.<p>
     *
     * @throws Exception if the test fails
     */
    public void testMoveLostAndFound() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing the move to lost and found folder operation");

        String filename = "/file.txt";
        String link = "link.html";
        CmsResource res = cms.createResource(filename, CmsResourceTypePlain.getStaticTypeId());
        CmsResource res1 = cms.createResource(link, CmsResourceTypeXmlPage.getStaticTypeId());
        CmsFile file = cms.readFile(res1);
        CmsXmlPage page = CmsXmlPageFactory.unmarshal(cms, file, true);
        if (!page.hasValue("test", Locale.ENGLISH)) {
            page.addValue("test", Locale.ENGLISH);
        }
        page.setStringValue(cms, "test", Locale.ENGLISH, "<a href='file.txt'>file</a>");
        file.setContents(page.marshal());
        cms.lockResource(link);
        cms.writeFile(file);

        cms.unlockResource(filename);
        OpenCms.getPublishManager().publishResource(cms, filename);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.lockResource(filename);
        String newName = cms.moveToLostAndFound(filename);

        CmsResource newRes = cms.readResource(newName);
        CmsResource oldRes = cms.readResource(filename, CmsResourceFilter.ALL);

        assertSiblingCount(cms, newName, 2);
        assertState(cms, newName, CmsResource.STATE_NEW);

        assertNotSame(res.getStructureId(), newRes.getStructureId());
        assertEquals(res.getStructureId(), oldRes.getStructureId());

        assertSiblingCount(cms, filename, 2);
        assertState(cms, filename, CmsResource.STATE_DELETED);
        // the resource needs to be published first
        OpenCms.getPublishManager().publishResource(cms, filename);
        OpenCms.getPublishManager().waitWhileRunning();
        cms.createResource(filename, CmsResourceTypePlain.getStaticTypeId());

        file = cms.readFile(res1);
        page = CmsXmlPageFactory.unmarshal(cms, file, true);
        CmsLinkTable links = page.getLinkTable("test", Locale.ENGLISH);
        assertEquals(links.size(), 1);
        assertEquals(links.getLink("link0").getSitePath(), filename);
    }

    /**
     * Tests to move a file over a deleted one.<p>
     *
     * @throws Exception if the test fails
     */
    public void testMoveOverDeleted() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to move a file over a deleted one");

        // Creating paths
        String deletedFile = "/folder1/page1.html";
        String sourceFile = "/folder1/page2.html";

        cms.lockResource("/");
        cms.setDateLastModified("/", System.currentTimeMillis(), true);
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.lockResource(deletedFile);
        cms.deleteResource(deletedFile, CmsResource.DELETE_PRESERVE_SIBLINGS);

        try {
            cms.moveResource(sourceFile, deletedFile);
            fail("should fail to move a file over a deleted one");
        } catch (Exception e) {
            // ok
        }
    }

    /**
     * Tests moving a sibling.<p>
     *
     * @throws Exception if the test fails
     */
    public void testMoveSibling() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing moving a sibling");

        String resName = "index.html";
        String sibName = "folder1/sib.html";
        String sib2Name = "folder1/sib2.html";

        // create sibling
        cms.copyResource(resName, sibName, CmsResource.COPY_AS_SIBLING);
        OpenCms.getPublishManager().publishResource(cms, sibName);
        OpenCms.getPublishManager().waitWhileRunning();

        // store state for later
        storeResources(cms, resName);
        storeResources(cms, sibName);

        // check the initial lock state
        assertLock(cms, resName, CmsLockType.UNLOCKED);
        assertLock(cms, sibName, CmsLockType.UNLOCKED);

        // lock the sibling
        cms.lockResource(sibName);

        // check the lock state
        assertLock(cms, resName, CmsLockType.SHARED_EXCLUSIVE);
        assertLock(cms, sibName, CmsLockType.EXCLUSIVE);

        // move the sibling
        cms.moveResource(sibName, sib2Name);

        // check the lock state
        assertLock(cms, resName, CmsLockType.SHARED_EXCLUSIVE);
        assertLock(cms, sib2Name, CmsLockType.EXCLUSIVE);

        // unlock the resource to enable the filter
        cms.unlockResource(sib2Name);
        assertFilter(cms, resName, OpenCmsTestResourceFilter.FILTER_EQUAL);

        setMapping(sib2Name, sibName);
        assertFilter(cms, sib2Name, OpenCmsTestResourceFilter.FILTER_MOVE_DESTINATION);
        assertState(cms, sib2Name, CmsResourceState.STATE_CHANGED);
        assertProject(cms, sib2Name, cms.getRequestContext().getCurrentProject());
    }

    /**
     * Tests to publish a moved deleted folder with a unpublished moved resource.<p>
     *
     * @throws Exception if the test fails
     */
    public void testPublishMovedDeletedFolderWithMovedResource() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to publish a moved deleted folder with a unpublished moved resource");

        String folder = "/folder1/subfolder12/";
        String folderDest = "/moved_subfolder12/";
        String source = "index.html";
        String destination = "def.html";

        // move the resource outside of the folder
        cms.lockResource(folder + source);
        cms.moveResource(folder + source, destination);

        // check lock
        assertFalse(cms.getLockedResources(folder, CmsLockFilter.FILTER_ALL).contains(folder + source));

        // move the folder
        cms.lockResource(folder);
        cms.moveResource(folder, folderDest);

        // check lock
        assertFalse(cms.getLockedResources("/folder1", CmsLockFilter.FILTER_ALL).contains(folder));

        try {
            // undoing the changes to a deleted folder must cause an exception
            cms.undoChanges(destination, CmsResource.UNDO_MOVE_CONTENT);
            fail("undoing the changes to a deleted folder must cause an exception");
        } catch (CmsVfsResourceNotFoundException e) {
            // ok
        }

        // delete it
        cms.deleteResource(folderDest, CmsResource.DELETE_PRESERVE_SIBLINGS);

        // publish the deleted folder
        cms.unlockResource(folderDest);
        OpenCms.getPublishManager().publishResource(cms, folderDest);
        OpenCms.getPublishManager().waitWhileRunning();

        // publish the moved file
        cms.unlockResource(destination);
        OpenCms.getPublishManager().publishResource(cms, destination);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Tests renaming a file to the same name with a different case.<p>
     *
     * @throws Exception if the test fails
     */
    public void testRenameFileUpperLowerCase() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to rename a file to the same name with a different case");

        // Creating paths
        String source = "/folder2/image1.gif";
        String destination = "/folder2/Image1.GIF";

        storeResources(cms, source);

        // now move from the old to the new name
        cms.lockResource(source);
        cms.moveResource(source, destination);

        // check lock
        assertFalse(cms.getLockedResources("/folder2", CmsLockFilter.FILTER_ALL).contains(source));

        try {
            // source resource must be gone
            cms.readResource(source, CmsResourceFilter.ALL);
            fail("New resource still available after move operation!");
        } catch (CmsVfsResourceNotFoundException e) {
            // this is expected
        }

        // destination resource

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
    }

    /**
     * Tests renaming a folder to the same name with a different case.<p>
     *
     * @throws Exception if the test fails
     */
    public void testRenameFolderUpperLowerCase() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to rename a folder to the same name with a different case");

        // Creating paths
        String source = "/folder1/subfolder11/subsubfolder111";
        String destination = "/folder1/subfolder11/SubSubFolder111";

        storeResources(cms, source);

        // now move from the old to the new name
        cms.lockResource(source);
        cms.moveResource(source, destination);

        // check lock
        assertFalse(cms.getLockedResources("/folder1", CmsLockFilter.FILTER_ALL).contains(source));

        // source resource must be gone for default read
        try {
            cms.readResource(source, CmsResourceFilter.ALL);
            fail("New resource still available after move operation!");
        } catch (CmsVfsResourceNotFoundException e) {
            // this is expected
        }

        // try to read the destination folder
        cms.readResource(destination);

        // project must be current project
        assertProject(cms, destination, cms.getRequestContext().getCurrentProject());
        // state must be "changed"
        assertState(cms, destination, CmsResource.STATE_CHANGED);
        // assert lock state
        assertLock(cms, destination, CmsLockType.EXCLUSIVE);
        // folders don't have siblings
        assertSiblingCount(cms, destination, 1);

        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Tests renaming a new folder with content.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testRenameNewFolder() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing rename a new folder with content");

        String source = "/folder1";
        String newFolder = "/newfolder";
        String newFolder2 = "/testfolder";

        cms.createResource(newFolder, CmsResourceTypeFolder.getStaticTypeId());

        cms.lockResource(source);
        cms.moveResource(source, newFolder + source);

        // check lock
        assertFalse(cms.getLockedResources("/", CmsLockFilter.FILTER_ALL).contains(source));

        cms.moveResource(newFolder, newFolder2);

        // check lock
        assertFalse(cms.getLockedResources("/", CmsLockFilter.FILTER_ALL).contains(newFolder));

        try {
            cms.readResource(source, CmsResourceFilter.ALL);
            fail("source folder still there.");
        } catch (CmsException e) {
            // ok
        }

        try {
            cms.readResource(newFolder, CmsResourceFilter.ALL);
            fail("new folder still there.");
        } catch (CmsException e) {
            // ok
        }

        try {
            cms.readResource(newFolder2);
        } catch (CmsVfsResourceNotFoundException e) {
            fail("folder not found.");
        }

        assertState(cms, newFolder2, CmsResource.STATE_NEW);
        assertState(cms, newFolder2 + source, CmsResource.STATE_CHANGED);

        cms.undoChanges(newFolder2 + source, CmsResource.UNDO_MOVE_CONTENT_RECURSIVE);

        // check lock
        assertFalse(cms.getLockedResources(newFolder2, CmsLockFilter.FILTER_ALL).contains(newFolder2 + source));

        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
    }
}
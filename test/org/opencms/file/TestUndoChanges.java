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
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestResourceFilter;
import org.opencms.test.OpenCmsTestResourceStorage;
import org.opencms.util.CmsStringUtil;

import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for the "undoChanges" method of the CmsObject.<p>
 *
 */
public class TestUndoChanges extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestUndoChanges(String arg0) {

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
        suite.setName(TestUndoChanges.class.getName());

        suite.addTest(new TestUndoChanges("testUndoChangesResource"));
        suite.addTest(new TestUndoChanges("testUndoChangesOnNewResource"));
        suite.addTest(new TestUndoChanges("testUndoChangesFolder"));
        suite.addTest(new TestUndoChanges("testUndoChangesFolderRecursive"));
        suite.addTest(new TestUndoChanges("testUndoChangesAfterCopyNewOverDeleted"));
        suite.addTest(new TestUndoChanges("testUndoChangesAfterCopySiblingOverDeleted"));
        suite.addTest(new TestUndoChanges("testUndoChangesWithAce"));
        suite.addTest(new TestUndoChanges("testUndoChangesSharedProperty"));
        suite.addTest(new TestUndoChanges("testUndoChangesMove"));
        suite.addTest(new TestUndoChanges("testUndoChangesMoveContent"));
        suite.addTest(new TestUndoChanges("testUndoChangesMovedFolder"));
        suite.addTest(new TestUndoChanges("testUndoChangesMovedFolderAfterEdit"));
        suite.addTest(new TestUndoChanges("testUndoChangesMovedFolderNewFile"));
        suite.addTest(new TestUndoChanges("testUndoChangesSubfolderAfterMoving"));
        suite.addTest(new TestUndoChanges("testUndoChangesMovedFileAfterEdit"));
        suite.addTest(new TestUndoChanges("testUndoChangesScenario1"));

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
     * Test the touch method to touch a single resource.<p>
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to touch
     * @throws Throwable if something goes wrong
     */
    public static void undoChanges(OpenCmsTestCase tc, CmsObject cms, String resource1) throws Throwable {

        // create a global storage and store the resource
        tc.createStorage("undoChanges");
        tc.switchStorage("undoChanges");
        tc.storeResources(cms, resource1);
        tc.switchStorage(OpenCmsTestResourceStorage.DEFAULT_STORAGE);

        // now do a touch on the resource
        TestTouch.touchResource(tc, cms, resource1);

        // change a property
        CmsProperty property1 = new CmsProperty("Title", "undoChanges2", null);
        TestProperty.writeProperty(tc, cms, resource1, property1);

        // now undo everything
        cms.lockResource(resource1);
        cms.undoChanges(resource1, CmsResource.UNDO_CONTENT);
        cms.unlockResource(resource1);

        tc.switchStorage("undoChanges");

        // now evaluate the result
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
        // project must be current project
        tc.assertProject(cms, resource1, cms.getRequestContext().getCurrentProject());
    }

    /**
     *  Test undoChanges method to a single folder.<p>
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to touch
     * @throws Throwable if something goes wrong
     */
    public static void undoChangesFolder(OpenCmsTestCase tc, CmsObject cms, String resource1) throws Throwable {

        // create a global storage and store the resource
        tc.createStorage("undoChanges");
        tc.switchStorage("undoChanges");
        tc.storeResources(cms, resource1);
        tc.switchStorage(OpenCmsTestResourceStorage.DEFAULT_STORAGE);

        long timestamp = System.currentTimeMillis();

        // change a property
        CmsProperty property1 = new CmsProperty("Title", "undoChanges", null);
        TestProperty.writeProperty(tc, cms, resource1, property1);

        // change the property on all subresources
        List subresources = cms.readResources(resource1, CmsResourceFilter.ALL);
        Iterator i = subresources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            String resName = cms.getSitePath(res);
            TestProperty.writeProperty(tc, cms, resName, property1);
        }

        // now undo everything
        cms.lockResource(resource1);
        cms.undoChanges(resource1, CmsResource.UNDO_CONTENT);
        cms.unlockResource(resource1);

        tc.switchStorage("undoChanges");

        // now evaluate the result, the folder must be unchanged now
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
        // project must be current project
        tc.assertProject(cms, resource1, cms.getRequestContext().getCurrentProject());

        // all resources within the folder must keep their changes
        Iterator j = subresources.iterator();
        while (j.hasNext()) {
            CmsResource res = (CmsResource)j.next();
            String resName = cms.getSitePath(res);
            tc.assertFilter(cms, resName, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);
            // project must be current project
            tc.assertProject(cms, resName, cms.getRequestContext().getCurrentProject());
            // state must be "changed"
            tc.assertState(cms, resName, tc.getPreCalculatedState(resource1));
            // date last modified must be after the test timestamp
            tc.assertDateLastModifiedAfter(cms, resName, timestamp);
            // the user last modified must be the current user
            tc.assertUserLastModified(cms, resName, cms.getRequestContext().getCurrentUser());
            // the property must have the new value
            tc.assertPropertyChanged(cms, resName, property1);
        }
    }

    /**
     * Test undoChanges method to a single folder and all resources within the folder.<p>
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to touch
     * @throws Throwable if something goes wrong
     */
    public static void undoChangesFolderRecursive(OpenCmsTestCase tc, CmsObject cms, String resource1)
    throws Throwable {

        // create a global storage and store the resource
        tc.createStorage("undoChanges");
        tc.switchStorage("undoChanges");
        tc.storeResources(cms, resource1);
        tc.switchStorage(OpenCmsTestResourceStorage.DEFAULT_STORAGE);

        // change a property
        CmsProperty property1 = new CmsProperty("Title", "undoChanges", null);
        TestProperty.writeProperty(tc, cms, resource1, property1);

        // change the property on all subresources
        List subresources = cms.readResources(resource1, CmsResourceFilter.ALL);
        Iterator i = subresources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            String resName = cms.getSitePath(res);
            TestProperty.writeProperty(tc, cms, resName, property1);
        }

        // now undo everything
        cms.lockResource(resource1);
        cms.undoChanges(resource1, CmsResource.UNDO_CONTENT_RECURSIVE);
        cms.unlockResource(resource1);

        tc.switchStorage(OpenCmsTestResourceStorage.GLOBAL_STORAGE);

        // now evaluate the result, the folder must be unchanged now
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
        // project must be current project
        tc.assertProject(cms, resource1, cms.getRequestContext().getCurrentProject());

        // all resources within the folder must  be unchanged now
        Iterator j = subresources.iterator();
        while (j.hasNext()) {
            CmsResource res = (CmsResource)j.next();
            String resName = cms.getSitePath(res);

            // now evaluate the result
            tc.assertFilter(cms, resName, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
            // project must be current project
            tc.assertProject(cms, resName, cms.getRequestContext().getCurrentProject());
        }
    }

    /**
     * Tests undo changes after a resource was deleted and another
     * resource was copied over the deleted file "as new".<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndoChangesAfterCopyNewOverDeleted() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing undo changes after overwriting a deleted file with a new file");

        String source = "/folder1/page2.html";
        String destination = "/folder1/page1.html";

        storeResources(cms, source);
        storeResources(cms, destination);

        cms.lockResource(destination);

        // delete and owerwrite
        cms.deleteResource(destination, CmsResource.DELETE_PRESERVE_SIBLINGS);
        assertState(cms, destination, CmsResource.STATE_DELETED);

        cms.copyResource(source, destination, CmsResource.COPY_AS_NEW);

        // now undo all changes on the resource
        cms.undoChanges(destination, CmsResource.UNDO_CONTENT);

        // now ensure source and destionation are in the original state
        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_EQUAL);
        assertFilter(cms, destination, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);

        // publishing may reveal problems with the id's
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Tests undo changes of move operations as follows:<p>
     *
     * having following start structure:
     * - (black) /folder
     * - (black) /folder/subfolder
     * - (black) /folder/subfolder/subsubfolder
     *
     * a. move /folder/subfolder to /subfolder
     * b. move /subfolder/subsubfolder to /subsubfolder
     * c. move /subfolder back to its starting location: /folder/subfolder
     * d. undo changes of move operation for /subsubfolder
     *
     * to get following scenario:
     * - (black) /folder
     * - (red) /folder/subfolder
     * - (black) /folder/subfolder/subsubfolder
     *
     * e. undo changes of move operation for /folder/subfolder
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndoChangesScenario1() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing undo changes after overwriting a deleted file with a new file");

        String folder = "folder";
        String subfolder = "subfolder";
        String subsubfolder = "subsubfolder";
        String file = "file";

        // create starting point
        cms.createResource(folder, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.createResource(folder + "/" + file, CmsResourceTypePlain.getStaticTypeId());
        cms.createResource(folder + "/" + subfolder, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.createResource(folder + "/" + subfolder + "/" + file, CmsResourceTypePlain.getStaticTypeId());
        cms.createResource(folder + "/" + subfolder + "/" + subsubfolder, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.createResource(
            folder + "/" + subfolder + "/" + subsubfolder + "/" + file,
            CmsResourceTypePlain.getStaticTypeId());
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishResource(cms, folder);
        OpenCms.getPublishManager().waitWhileRunning();

        storeResources(cms, "/" + folder, true);

        // a. move /folder/subfolder to /subfolder
        cms.lockResource(folder + "/" + subfolder);
        cms.moveResource(folder + "/" + subfolder, "/" + subfolder);

        // b. move /subfolder/subsubfolder to /subsubfolder
        cms.lockResource(subfolder + "/" + subsubfolder);
        cms.moveResource(subfolder + "/" + subsubfolder, "/" + subsubfolder);

        // c. move /subfolder back to its starting location: /folder/subfolder
        cms.moveResource("/" + subfolder, folder + "/" + subfolder);

        // d. undo changes of move operation for /subsubfolder
        cms.undoChanges("/" + subsubfolder, CmsResource.UNDO_MOVE_CONTENT);

        // check intermediate state
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        assertFilter(cms, "/" + folder + "/", OpenCmsTestResourceFilter.FILTER_EQUAL);
        assertFilter(cms, "/" + folder + "/" + file, OpenCmsTestResourceFilter.FILTER_EQUAL);
        assertFilter(cms, "/" + folder + "/" + subfolder + "/", OpenCmsTestResourceFilter.FILTER_TOUCH);
        assertFilter(cms, "/" + folder + "/" + subfolder + "/" + file, OpenCmsTestResourceFilter.FILTER_TOUCH);
        assertFilter(
            cms,
            "/" + folder + "/" + subfolder + "/" + subsubfolder + "/",
            OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
        assertFilter(
            cms,
            "/" + folder + "/" + subfolder + "/" + subsubfolder + "/" + file,
            OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);

        // e. undo changes of move operation for /folder/subfolder
        cms.lockResource(folder + "/" + subfolder);
        cms.undoChanges(folder + "/" + subfolder, CmsResource.UNDO_MOVE_CONTENT);

        // check final state, the same starting state
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        assertFilter(cms, "/" + folder + "/", OpenCmsTestResourceFilter.FILTER_EQUAL);
        assertFilter(cms, "/" + folder + "/" + file, OpenCmsTestResourceFilter.FILTER_EQUAL);
        assertFilter(cms, "/" + folder + "/" + subfolder + "/", OpenCmsTestResourceFilter.FILTER_MOVE_DESTINATION);
        assertFilter(
            cms,
            "/" + folder + "/" + subfolder + "/" + file,
            OpenCmsTestResourceFilter.FILTER_MOVE_DESTINATION);
        assertFilter(
            cms,
            "/" + folder + "/" + subfolder + "/" + subsubfolder + "/",
            OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
        assertFilter(
            cms,
            "/" + folder + "/" + subfolder + "/" + subsubfolder + "/" + file,
            OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
    }

    /**
     * Tests undo changes after a resource was deleted and another
     * resource was copied over the deleted file "as sibling".<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndoChangesAfterCopySiblingOverDeleted() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing undo changes after overwriting a deleted file with a sibling");

        String source = "/folder1/page2.html";
        String destination = "/folder1/page1.html";

        storeResources(cms, source);
        storeResources(cms, destination);

        cms.lockResource(destination);

        // delete and owerwrite with a sibling
        cms.deleteResource(destination, CmsResource.DELETE_PRESERVE_SIBLINGS);
        assertState(cms, destination, CmsResource.STATE_DELETED);

        cms.copyResource(source, destination, CmsResource.COPY_AS_SIBLING);

        // now undo all changes on the resource
        cms.undoChanges(destination, CmsResource.UNDO_CONTENT);

        // now ensure source and destionation are in the original state
        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
        assertFilter(cms, destination, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);

        // publishing may reveal problems with the id's
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Test undoChanges method to a single folder.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndoChangesFolder() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing undoChanges on a folder without recursion");
        undoChangesFolder(this, cms, "/folder2/");
    }

    /**
     * Test undoChanges method to a single folder.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndoChangesFolderRecursive() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing undoChanges on a folder _with_ recursion");
        undoChangesFolderRecursive(this, cms, "/folder1/");
    }

    /**
     * Tests undo changes for the move operation only.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndoChangesMove() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing undo changes for the move operation only");

        String source = "/folder1/";
        String destination = "/folder1_undo2/";
        String sourceFile = "page1.html";
        String newFile = "filenew.html";

        storeResources(cms, source, true);

        // move
        cms.lockResource(source);
        cms.moveResource(source, destination);

        // edit
        cms.writePropertyObject(destination + sourceFile, new CmsProperty("test property", "a", "b", true));

        // create new file
        cms.createResource(destination + newFile, CmsResourceTypePlain.getStaticTypeId());
        storeResources(cms, destination + newFile);

        // now undo all changes on the folder
        cms.undoChanges(destination, CmsResource.UNDO_MOVE_CONTENT);

        // now ensure source and destination are in the original state
        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
        // project must be current project
        assertProject(cms, source, cms.getRequestContext().getCurrentProject());

        setMapping(source + newFile, destination + newFile);
        // test recursively
        Iterator subresources = cms.readResources(source, CmsResourceFilter.ALL).iterator();
        while (subresources.hasNext()) {
            CmsResource res = (CmsResource)subresources.next();
            String resName = cms.getSitePath(res);
            if (resName.equals(source + newFile)) {
                assertFilter(cms, resName, OpenCmsTestResourceFilter.FILTER_CREATE_RESOURCE);
            } else {
                assertFilter(cms, resName, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
            }
            // project must be current project
            assertProject(cms, resName, cms.getRequestContext().getCurrentProject());
        }
        assertTrue(cms.existsResource(source, CmsResourceFilter.ALL));
        assertTrue(cms.existsResource(source + sourceFile));
        assertTrue(cms.existsResource(source + newFile));
        assertFalse(cms.existsResource(destination, CmsResourceFilter.ALL));

        cms.deleteResource(source + newFile, CmsResource.DELETE_REMOVE_SIBLINGS);
        // publishing may reveal problems with the id's
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Tests undo changes for the content changes only.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndoChangesMoveContent() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing undo changes for the content changes only");

        String source = "/folder1/";
        String destination = "/folder1_undo2/";
        String sourceFile = "page1.html";
        String newFile = "filenew.html";

        storeResources(cms, source, true);

        // move
        cms.lockResource(source);
        cms.moveResource(source, destination);

        // edit
        cms.writePropertyObject(destination + sourceFile, new CmsProperty("test property", "a", "b", true));

        // create new file
        cms.createResource(destination + newFile, CmsResourceTypePlain.getStaticTypeId());

        // now undo all content changes on the folder
        cms.undoChanges(destination, CmsResource.UNDO_CONTENT_RECURSIVE);

        setMapping(destination, source);
        // now ensure source and destionation are in the original state
        assertFilter(cms, destination, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_CONTENT);
        // project must be current project
        assertProject(cms, destination, cms.getRequestContext().getCurrentProject());
        // test recursively
        Iterator subresources = cms.readResources(destination, CmsResourceFilter.ALL).iterator();
        while (subresources.hasNext()) {
            CmsResource res = (CmsResource)subresources.next();
            String resName = cms.getSitePath(res);
            if (resName.equals(destination + newFile)) {
                assertState(cms, resName, CmsResource.STATE_NEW);
            } else {
                setMapping(resName, CmsStringUtil.substitute(resName, destination, source));
                assertFilter(cms, resName, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_CONTENT);
            }
            // project must be current project
            assertProject(cms, resName, cms.getRequestContext().getCurrentProject());
        }
        assertTrue(cms.existsResource(destination));
        assertTrue(cms.existsResource(destination + sourceFile));
        assertTrue(cms.existsResource(destination + newFile));
        assertFalse(cms.existsResource(source, CmsResourceFilter.ALL));

        // now undo move operation on the folder
        cms.undoChanges(destination, CmsResource.UNDO_MOVE_CONTENT);

        resetMapping();
        // now ensure source and destionation are in the original state
        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
        // project must be current project
        assertProject(cms, source, cms.getRequestContext().getCurrentProject());
        // test recursively
        subresources = cms.readResources(source, CmsResourceFilter.ALL).iterator();
        while (subresources.hasNext()) {
            CmsResource res = (CmsResource)subresources.next();
            String resName = cms.getSitePath(res);
            if (resName.equals(source + newFile)) {
                assertState(cms, resName, CmsResource.STATE_NEW);
            } else {
                assertFilter(cms, resName, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
            }
            // project must be current project
            assertProject(cms, resName, cms.getRequestContext().getCurrentProject());
        }
        assertTrue(cms.existsResource(source));
        assertTrue(cms.existsResource(source + sourceFile));
        assertTrue(cms.existsResource(source + newFile));
        assertFalse(cms.existsResource(destination, CmsResourceFilter.ALL));

        cms.deleteResource(source + newFile, CmsResource.DELETE_REMOVE_SIBLINGS);
        // publishing may reveal problems with the id's
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Tests undo changes after a file was moved and edited.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndoChangesMovedFileAfterEdit() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing undo changes after a file was moved and edited");

        String source = "/folder1/page1.html";
        String destination = "/folder1/page1_undo.html";

        storeResources(cms, source, true);

        // move
        cms.lockResource(source);
        cms.moveResource(source, destination);

        // edit
        cms.writePropertyObject(destination, new CmsProperty("test property", "a", "b", true));

        // now undo content changes
        cms.undoChanges(destination, CmsResource.UNDO_CONTENT_RECURSIVE);

        setMapping(destination, source);
        // now ensure destination is in the original state
        assertFilter(cms, destination, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_CONTENT);
        // project must be current project
        assertProject(cms, destination, cms.getRequestContext().getCurrentProject());
        // file must still be moved
        assertFalse(cms.existsResource(source, CmsResourceFilter.ALL));

        // now undo move operation
        cms.undoChanges(destination, CmsResource.UNDO_MOVE_CONTENT_RECURSIVE);

        // now ensure source is in the original state
        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
        // file must be in source location
        assertFalse(cms.existsResource(destination, CmsResourceFilter.ALL));

        // publishing may reveal problems with the id's
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Tests undo changes after a folder was moved.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndoChangesMovedFolder() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing undo changes after moving a folder");

        String source = "/folder1/";
        String destination = "/folder1_undo3/";

        storeResources(cms, source);

        cms.lockResource(source);
        // move
        cms.moveResource(source, destination);

        // now undo changes on the folder
        cms.undoChanges(destination, CmsResource.UNDO_MOVE_CONTENT_RECURSIVE);

        // now ensure source and destionation are in the original state
        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
        assertFalse(cms.existsResource(destination, CmsResourceFilter.ALL));

        // publishing may reveal problems with the id's
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Tests undo changes after a folder was moved and a file in the folder edited.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndoChangesMovedFolderAfterEdit() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing undo changes after a folder was moved and a file in the folder edited");

        String source = "/folder1/";
        String destination = "/folder1_undo2/";
        String sourceFile = "page1.html";

        storeResources(cms, source, true);

        // move
        cms.lockResource(source);
        cms.moveResource(source, destination);

        // edit
        cms.writePropertyObject(destination + sourceFile, new CmsProperty("test property", "a", "b", true));

        // now undo all changes on the folder
        cms.undoChanges(destination, CmsResource.UNDO_MOVE_CONTENT_RECURSIVE);

        // now ensure source and destionation are in the original state
        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
        // project must be current project
        assertProject(cms, source, cms.getRequestContext().getCurrentProject());
        // test recursively
        Iterator subresources = cms.readResources(source, CmsResourceFilter.ALL).iterator();
        while (subresources.hasNext()) {
            CmsResource res = (CmsResource)subresources.next();
            String resName = cms.getSitePath(res);
            // destination + sourceFile changes get lost
            assertFilter(cms, resName, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
            // project must be current project
            assertProject(cms, resName, cms.getRequestContext().getCurrentProject());
        }
        assertFalse(cms.existsResource(destination, CmsResourceFilter.ALL));

        // publishing may reveal problems with the id's
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Tests undo changes after a folder was moved and a file in the folder edited.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndoChangesMovedFolderNewFile() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing undo changes after a folder was moved and a file in the folder edited");

        String source = "/folder1/";
        String destination = "/folder1_undo2/";
        String newFile = "newfile.html";

        storeResources(cms, source, true);

        // move
        cms.lockResource(source);
        cms.moveResource(source, destination);

        // create new file
        cms.createResource(destination + newFile, CmsResourceTypePlain.getStaticTypeId());

        // now undo all changes on the folder
        cms.undoChanges(destination, CmsResource.UNDO_MOVE_CONTENT_RECURSIVE);

        // now ensure source and destionation are in the original state
        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
        // project must be current project
        assertProject(cms, source, cms.getRequestContext().getCurrentProject());
        // test recursively
        Iterator subresources = cms.readResources(source, CmsResourceFilter.ALL).iterator();
        while (subresources.hasNext()) {
            CmsResource res = (CmsResource)subresources.next();
            String resName = cms.getSitePath(res);
            // newFile should still there
            if (resName.equals(source + newFile)) {
                assertState(cms, source + newFile, CmsResource.STATE_NEW);
            } else {
                assertFilter(cms, resName, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
                // project must be current project
                assertProject(cms, resName, cms.getRequestContext().getCurrentProject());
            }
        }
        assertFalse(cms.existsResource(destination));
        assertTrue(cms.existsResource(source + newFile));

        // publishing may reveal problems with the id's
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Tests undo changes on a new resource, this must lead to an exception!<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndoChangesOnNewResource() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing for exception when trying undo changes on a new resource");

        String source = "/types/new.html";

        // create a new, plain resource
        cms.createResource(source, CmsResourceTypePlain.getStaticTypeId());
        assertLock(cms, source, CmsLockType.EXCLUSIVE);

        try {
            cms.undoChanges(source, CmsResource.UNDO_CONTENT);
        } catch (CmsVfsException e) {
            if (!e.getMessageContainer().getKey().equals(org.opencms.db.Messages.ERR_UNDO_CHANGES_FOR_RESOURCE_1)) {
                fail("Did not catch expected exception trying undo changes on a new resource!");
            }
        }
    }

    /**
     * Test undoChanges method to a single file.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndoChangesResource() throws Throwable {

        CmsObject cms = getCmsObject();

        // this is the first test, so set up the global storage used for all other
        // tests
        createStorage(OpenCmsTestResourceStorage.GLOBAL_STORAGE);
        switchStorage(OpenCmsTestResourceStorage.GLOBAL_STORAGE);
        storeResources(cms, "/");
        switchStorage(OpenCmsTestResourceStorage.DEFAULT_STORAGE);

        echo("Testing undoChanges on a file");
        undoChanges(this, cms, "/index.html");
    }

    /**
     * Tests undo changes after a folder was moved several times.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndoChangesSeveralMovedFolder() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing undo changes after moving a folder several times");

        String source = "/folder1/";
        String destination = "/folder1_undo4/";
        String source2 = "/subfolder11/";
        String destination2 = "/subfolder11_undo/";

        cms.lockResource(source);
        // move
        cms.moveResource(source, destination);
        // move sub folder
        cms.moveResource(destination + source2, destination2);

        // now try to undo changes on the sub folder
        try {
            cms.undoChanges(destination2, CmsResource.UNDO_MOVE_CONTENT_RECURSIVE);
            fail("undo changes should fail, since a parent folder is missing");
        } catch (Exception e) {
            // ok
        }

        // undo changes of parent folder
        cms.undoChanges(destination, CmsResource.UNDO_MOVE_CONTENT_RECURSIVE);
        assertFalse(cms.existsResource(destination, CmsResourceFilter.ALL));
        assertTrue(cms.existsResource(source));
        // subfolder should still be moved
        assertFalse(cms.existsResource(destination + source2, CmsResourceFilter.ALL));
        assertTrue(cms.existsResource(destination2));

        // undo changes of sub folder
        cms.undoChanges(destination2, CmsResource.UNDO_MOVE_CONTENT_RECURSIVE);
        assertTrue(cms.existsResource(source + source2));
        assertFalse(cms.existsResource(destination2, CmsResourceFilter.ALL));

        // publishing may reveal problems with the id's
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Test undoChanges method to a resource with an ace.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndoChangesSharedProperty() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing undoChanges on shared property");

        // create the files
        String file = "/a";
        cms.createResource(file, CmsResourceTypePlain.getStaticTypeId());
        // publish the project
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        String sibling = "/b";
        TestSiblings.createSibling(this, cms, file, sibling);
        // write a persistent no-shared property to test with
        CmsProperty property = new CmsProperty(CmsPropertyDefinition.PROPERTY_NAVTEXT, "undoChanges navText", null);
        cms.writePropertyObject(sibling, property);
        // write a persistent shared property to test with
        CmsProperty property1 = new CmsProperty(
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            null,
            "undoChanges description");
        cms.writePropertyObject(sibling, property1);
        // publish the project
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        // create a global storage and store the resource
        createStorage("undoChanges");
        switchStorage("undoChanges");
        storeResources(cms, file);
        storeResources(cms, sibling);
        switchStorage(OpenCmsTestResourceStorage.DEFAULT_STORAGE);

        // change a shared property
        CmsProperty property2 = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, null, "undoChanges title");
        cms.lockResource(file);
        cms.writePropertyObject(file, property2);
        cms.unlockResource(file);
        //TestProperty.writeProperty(this, cms, file1, property);

        // now undo everything
        cms.lockResource(file);
        cms.undoChanges(file, CmsResource.UNDO_CONTENT);
        cms.unlockResource(file);

        switchStorage("undoChanges");

        // now evaluate the result
        assertFilter(cms, file, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
        // project must be current project
        assertProject(cms, file, cms.getRequestContext().getCurrentProject());

        // publishing may reveal problems with the id's
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Test undoChanges method to a sub folder after moving a whole folder.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndoChangesSubfolderAfterMoving() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing undoChanges on a sub folder after moving a whole folder");

        String source = "/folder1/";
        String destination = "/folder1_undo1/";
        String undo = "subfolder11/";

        cms.lockResource(source);

        // move
        cms.moveResource(source, destination);

        // now undo all changes on the subfolder
        try {
            cms.undoChanges(destination + undo, CmsResource.UNDO_MOVE_CONTENT_RECURSIVE);
            fail("undoing move of subfolder must not be allowed");
        } catch (CmsException e) {
            // expected
        }

        cms.undoChanges(destination, CmsResource.UNDO_MOVE_CONTENT_RECURSIVE);
        // publishing may reveal problems with the id's
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Test undoChanges method to a resource with an ace.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndoChangesWithAce() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing undoChanges on a resource with an ACE");
        undoChanges(this, cms, "/folder2/index.html");
    }

}

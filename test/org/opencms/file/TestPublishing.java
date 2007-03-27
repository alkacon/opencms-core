/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestPublishing.java,v $
 * Date   : $Date: 2007/03/27 14:16:25 $
 * Version: $Revision: 1.21.4.6 $
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

import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsPublishedResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.CmsException;
import org.opencms.main.CmsMultiException;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestLogAppender;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestResourceFilter;

import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for OpenCms publishing.<p>
 * 
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.21.4.6 $
 */
public class TestPublishing extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestPublishing(String arg0) {

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
        suite.setName(TestPublishing.class.getName());

        suite.addTest(new TestPublishing("testPublishStructureProperty"));
        suite.addTest(new TestPublishing("testPublishResourceProperty"));
        suite.addTest(new TestPublishing("testPublishSiblings"));
        suite.addTest(new TestPublishing("testPublishNewFiles"));
        suite.addTest(new TestPublishing("testPublishNewFilesInNewFolder"));
        suite.addTest(new TestPublishing("testPublishChangedFiles"));
        suite.addTest(new TestPublishing("testPublishLockedFiles"));
        suite.addTest(new TestPublishing("testPublishDeletedFiles"));
        suite.addTest(new TestPublishing("testPublishProjectLastmodified"));
        suite.addTest(new TestPublishing("testPublishProjectLastmodifiedFolder"));
        suite.addTest(new TestPublishing("testPublishTemporaryProject"));
        suite.addTest(new TestPublishing("testPublishMovedFiles"));
        suite.addTest(new TestPublishing("testPublishRelatedFiles"));
        suite.addTest(new TestPublishing("testPublishRelatedFilesInFolder"));
        suite.addTest(new TestPublishing("testPublishRelatedFilesInFolder"));

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
     * Test publishing changed files.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testPublishChangedFiles() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publish changed files");

        String resource1 = "/folder2/image1_new.gif";
        String resource2 = "/folder2/image1_sibling1.gif";
        String resource3 = "/folder2/image1_sibling2.gif";
        String resource4 = "/folder2/image1_sibling3.gif";

        CmsProject onlineProject = cms.readProject("Online");
        CmsProject offlineProject = cms.readProject("Offline");

        CmsProperty prop1;
        CmsProperty prop2;
        CmsProperty prop3;
        CmsProperty prop4;

        // make changes to the resources 
        // do not need to make any changes to resource3 and resource4 as they are
        // siblings of resource2!

        cms.lockResource(resource1);
        cms.lockResource(resource2);

        cms.writePropertyObject(resource1, new CmsProperty("Title", resource1, null));
        cms.writePropertyObject(resource2, new CmsProperty("Title", resource2, null));
        cms.writePropertyObject(resource3, new CmsProperty("Title", resource3, null));
        cms.writePropertyObject(resource4, new CmsProperty("Title", resource4, null));

        // unlock all resources
        cms.unlockResource(resource1);
        cms.unlockResource(resource2);

        storeResources(cms, resource1);
        storeResources(cms, resource2);
        storeResources(cms, resource3);
        storeResources(cms, resource4);

        // publish a modified resource without siblings
        //
        OpenCms.getPublishManager().publishResource(cms, resource1);
        OpenCms.getPublishManager().waitWhileRunning();

        // the online file must the offline changes
        cms.getRequestContext().setCurrentProject(onlineProject);
        prop1 = cms.readPropertyObject(resource1, "Title", false);

        if (!prop1.getValue().equals((resource1))) {
            fail("Property not published for " + resource1);
        }
        assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_PUBLISHRESOURCE);

        // check if the file in the offline project is unchancged now
        cms.getRequestContext().setCurrentProject(offlineProject);
        assertState(cms, resource1, CmsResource.STATE_UNCHANGED);

        // publish a modified resource with siblings, but keeping the siblings unpublished
        //
        OpenCms.getPublishManager().publishResource(cms, resource2);
        OpenCms.getPublishManager().waitWhileRunning();

        // the online file must the offline changes
        cms.getRequestContext().setCurrentProject(onlineProject);
        prop2 = cms.readPropertyObject(resource2, "Title", false);
        prop3 = cms.readPropertyObject(resource3, "Title", false);
        prop4 = cms.readPropertyObject(resource4, "Title", false);

        if (!prop2.getValue().equals((resource2))) {
            fail("Property not published for " + resource2);
        }
        if (prop3.getValue().equals((resource3))) {
            fail("Property  published for " + resource3);
        }
        if (prop4.getValue().equals((resource4))) {
            fail("Property  published for " + resource4);
        }

        assertFilter(cms, resource2, OpenCmsTestResourceFilter.FILTER_PUBLISHRESOURCE);

        // check if the file in the offline project is unchancged now
        cms.getRequestContext().setCurrentProject(offlineProject);
        assertState(cms, resource2, CmsResource.STATE_UNCHANGED);
        assertState(cms, resource3, CmsResource.STATE_CHANGED);
        assertState(cms, resource4, CmsResource.STATE_CHANGED);

        // publish a modified resource with siblings, publish the siblings as well
        //
        OpenCms.getPublishManager().publishResource(
            cms,
            resource3,
            true,
            new CmsShellReport(cms.getRequestContext().getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();

        // the online file must the offline changes
        cms.getRequestContext().setCurrentProject(onlineProject);
        prop3 = cms.readPropertyObject(resource3, "Title", false);
        prop4 = cms.readPropertyObject(resource4, "Title", false);

        if (!prop3.getValue().equals((resource3))) {
            fail("Property  not published for " + resource3);
        }
        if (!prop4.getValue().equals((resource4))) {
            fail("Property  not published for " + resource4);
        }

        assertFilter(cms, resource3, OpenCmsTestResourceFilter.FILTER_PUBLISHRESOURCE);
        assertFilter(cms, resource4, OpenCmsTestResourceFilter.FILTER_PUBLISHRESOURCE);

        // check if the file in the offline project is unchancged now
        cms.getRequestContext().setCurrentProject(offlineProject);
        assertState(cms, resource3, CmsResource.STATE_UNCHANGED);
        assertState(cms, resource4, CmsResource.STATE_UNCHANGED);
    }

    /**
     * Test publishing deleted files.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testPublishDeletedFiles() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publish deleted files");

        String resource1 = "/folder2/image1_new.gif";
        String resource2 = "/folder2/image1_sibling1.gif";
        String resource3 = "/folder2/image1_sibling2.gif";
        String resource4 = "/folder2/image1_sibling3.gif";

        CmsProject onlineProject = cms.readProject("Online");
        CmsProject offlineProject = cms.readProject("Offline");

        // publish a deleted resource without siblings
        //

        // delete the resources 
        cms.lockResource(resource1);
        cms.deleteResource(resource1, CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.unlockResource(resource1);

        OpenCms.getPublishManager().publishResource(cms, resource1);
        OpenCms.getPublishManager().waitWhileRunning();

        // the online file must be deleted
        cms.getRequestContext().setCurrentProject(onlineProject);
        try {
            cms.readResource(resource1);
            fail("Resource " + resource1 + " was not deleted online");
        } catch (CmsVfsResourceNotFoundException e) {
            // ok
        } catch (CmsException e) {
            fail("Resource " + resource1 + " error:" + e);
        }

        cms.getRequestContext().setCurrentProject(offlineProject);

        // publish a deleted resource with siblings, 
        // delete the siblings also, but publish only the resource itself

        // delete the resources 
        cms.lockResource(resource2);
        cms.deleteResource(resource2, CmsResource.DELETE_REMOVE_SIBLINGS);
        cms.unlockResource(resource2);

        // this test makes only sense when siblings are published
        OpenCms.getPublishManager().publishResource(
            cms,
            resource2,
            false,
            new CmsShellReport(cms.getRequestContext().getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();

        // the online file must be deleted
        cms.getRequestContext().setCurrentProject(onlineProject);
        try {
            cms.readResource(resource2);
            fail("Resource " + resource2 + " was not deleted online");
        } catch (CmsVfsResourceNotFoundException e) {
            // ok
        } catch (CmsException e) {
            fail("Resource " + resource2 + " error:" + e);
        }
        // the other siblings must still be there
        try {
            cms.readResource(resource3);
        } catch (CmsException e) {
            fail("Resource " + resource3 + " error:" + e);
        }
        try {
            cms.readResource(resource4);
        } catch (CmsException e) {
            fail("Resource " + resource4 + " error:" + e);
        }

        cms.getRequestContext().setCurrentProject(offlineProject);
        // in the offline project, the siblings must be still marked as deleted
        assertState(cms, resource3, CmsResource.STATE_DELETED);
        assertState(cms, resource4, CmsResource.STATE_DELETED);

        // publish a deleted resource with siblings, delete the siblings
        //
        OpenCms.getPublishManager().publishResource(
            cms,
            resource3,
            true,
            new CmsShellReport(cms.getRequestContext().getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();

        // the online files must be deleted
        cms.getRequestContext().setCurrentProject(onlineProject);
        try {
            cms.readResource(resource3);
            fail("Resource " + resource3 + " was not deleted online");
        } catch (CmsVfsResourceNotFoundException e) {
            // ok
        } catch (CmsException e) {
            fail("Resource " + resource3 + " error:" + e);
        }
        try {
            cms.readResource(resource4);
            fail("Resource " + resource4 + " was not deleted online");
        } catch (CmsVfsResourceNotFoundException e) {
            // ok
        } catch (CmsException e) {
            fail("Resource " + resource4 + " error:" + e);
        }

        cms.getRequestContext().setCurrentProject(offlineProject);

    }

    /**
     * Test publishing changed files.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testPublishLockedFiles() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publish locked files");

        // switch user
        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        cms.getRequestContext().setSiteRoot("/sites/default/");

        String source = "/folder2/subfolder21/image1.gif";
        String resource1 = "/folder2/image1_new.gif";
        String resource2 = "/folder2/image1_sibling1.gif";

        // make changes to the resources 
        // do not need to make any changes to resource3 and resource4 as they are
        // siblings of resource2!

        cms.lockResource(source);
        cms.lockResource(resource1);
        cms.lockResource(resource2);

        cms.writePropertyObject(source, new CmsProperty("Title", source + " modified", null));
        cms.writePropertyObject(resource1, new CmsProperty("Title", resource1 + " modified", null));
        cms.writePropertyObject(resource2, new CmsProperty("Title", resource2 + " modified", null));

        // switch user, so the locked resources can not be published until they are explicit unlocked
        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        cms.getRequestContext().setSiteRoot("/sites/default/");

        storeResources(cms, source);
        storeResources(cms, resource1);
        storeResources(cms, resource2);

        // publish a modified resource without siblings
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        // ensure that all changed resources are still changed in the offline project
        assertState(cms, source, CmsResource.STATE_CHANGED);
        assertState(cms, resource1, CmsResource.STATE_CHANGED);
        assertState(cms, resource2, CmsResource.STATE_CHANGED);

        // lock the resources, so the next test can run
        cms.changeLock(source);
        cms.changeLock(resource1);
        cms.changeLock(resource2);

        // ensure that all changed resources are NOT published
        cms.getRequestContext().setCurrentProject(cms.readProject("Online"));
        CmsProperty prop0 = cms.readPropertyObject(source, "Title", false);
        CmsProperty prop1 = cms.readPropertyObject(resource1, "Title", false);
        CmsProperty prop2 = cms.readPropertyObject(resource2, "Title", false);

        if (prop0.getValue().equals((source + " modified"))) {
            fail("Property published for " + source);
        }
        if (prop1.getValue().equals((resource1 + " modified"))) {
            fail("Property published for " + resource1);
        }
        if (prop2.getValue().equals((resource2 + " modified"))) {
            fail("Property published for " + resource2);
        }
    }

    /**
     * Test publishing moved files.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testPublishMovedFiles() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publish moved files");

        String resource1 = "/folder1/subfolder12/subsubfolder121/image1.gif";
        String target1 = "/folder1/subfolder12/subsubfolder121/image1_new.gif";

        CmsProject onlineProject = cms.readProject("Online");
        CmsProject offlineProject = cms.readProject("Offline");

        // move the resource 
        cms.lockResource(resource1);
        cms.moveResource(resource1, target1);
        cms.unlockResource(target1);

        storeResources(cms, target1);

        // publish it
        CmsPublishList publishList = OpenCms.getPublishManager().getPublishList(cms, cms.readResource(target1), false);
        OpenCms.getPublishManager().publishProject(
            cms,
            new CmsShellReport(cms.getRequestContext().getLocale()),
            publishList);
        OpenCms.getPublishManager().waitWhileRunning();

        // check publish resource state
        List pubResources = cms.readPublishedResources(publishList.getPublishHistoryId());
        assertEquals(2, pubResources.size());
        CmsPublishedResource pubRes = (CmsPublishedResource)pubResources.get(0);
        assertEquals(cms.getRequestContext().addSiteRoot(resource1), pubRes.getRootPath());
        assertEquals(CmsPublishedResource.STATE_MOVED_SOURCE, pubRes.getMovedState());
        pubRes = (CmsPublishedResource)pubResources.get(1);
        assertEquals(cms.getRequestContext().addSiteRoot(target1), pubRes.getRootPath());
        assertEquals(CmsPublishedResource.STATE_MOVED_DESTINATION, pubRes.getMovedState());

        // the online original file must be deleted
        cms.getRequestContext().setCurrentProject(onlineProject);
        try {
            cms.readResource(resource1, CmsResourceFilter.ALL);
            fail("Resource " + resource1 + " was not deleted online");
        } catch (CmsVfsResourceNotFoundException e) {
            // ok
        } catch (CmsException e) {
            fail("Resource " + resource1 + " error:" + e);
        }
        try {
            cms.readResource(target1);
        } catch (CmsVfsResourceNotFoundException e) {
            fail("Resource " + target1 + " is not online");
        } catch (CmsException e) {
            fail("Resource " + target1 + " error:" + e);
        }
        assertFilter(cms, target1, OpenCmsTestResourceFilter.FILTER_PUBLISHRESOURCE);

        cms.getRequestContext().setCurrentProject(offlineProject);
        try {
            cms.readResource(resource1, CmsResourceFilter.ALL);
            fail("Resource " + resource1 + " was not deleted offline");
        } catch (CmsVfsResourceNotFoundException e) {
            // ok
        } catch (CmsException e) {
            fail("Resource " + resource1 + " error:" + e);
        }
        try {
            cms.readResource(target1);
        } catch (CmsVfsResourceNotFoundException e) {
            fail("Resource " + target1 + " is not offline");
        } catch (CmsException e) {
            fail("Resource " + target1 + " error:" + e);
        }
        assertState(cms, target1, CmsResource.STATE_UNCHANGED);

    }

    /**
     * Test publishing of related files.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testPublishRelatedFiles() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publishing of related files");

        String resName = "index.html";

        // touch the file to publish        
        cms.lockResource(resName);
        cms.setDateLastModified(resName, System.currentTimeMillis(), false);
        CmsResource resource = cms.readResource(resName, CmsResourceFilter.DEFAULT);

        // get the publish list
        CmsPublishList pubList = OpenCms.getPublishManager().getPublishList(cms, resource, false);
        // just check the publish list
        assertTrue(pubList.getDeletedFolderList().isEmpty());
        assertTrue(pubList.getFolderList().isEmpty());
        assertEquals(1, pubList.getFileList().size());
        assertTrue(pubList.getFileList().contains(resource));

        // get the list of related resources, which should be still empty, since the related resource has unchanged state
        CmsPublishList relatedList = OpenCms.getPublishManager().getRelatedResourcesToPublish(cms, pubList, null);
        // check the publish list has not been touched
        assertTrue(pubList.getDeletedFolderList().isEmpty());
        assertTrue(pubList.getFolderList().isEmpty());
        assertEquals(1, pubList.getFileList().size());
        assertTrue(pubList.getFileList().contains(resource));
        // check the related publish list
        assertTrue(relatedList.getDeletedFolderList().isEmpty());
        assertTrue(relatedList.getFolderList().isEmpty());
        assertTrue(relatedList.getFileList().isEmpty());

        String relResName = "folder1/image2.gif";

        // touch the related resource
        cms.lockResource(relResName);
        cms.setDateLastModified(relResName, System.currentTimeMillis(), false);
        CmsResource relatedRes = cms.readResource(relResName, CmsResourceFilter.DEFAULT);

        // get the list of related resources again
        relatedList = OpenCms.getPublishManager().getRelatedResourcesToPublish(cms, pubList, null);
        // check the publish list has not been touched
        assertTrue(pubList.getDeletedFolderList().isEmpty());
        assertTrue(pubList.getFolderList().isEmpty());
        assertEquals(1, pubList.getFileList().size());
        assertTrue(pubList.getFileList().contains(resource));
        // check the related publish list
        assertTrue(relatedList.getDeletedFolderList().isEmpty());
        assertTrue(relatedList.getFolderList().isEmpty());
        assertEquals(1, relatedList.getFileList().size());
        assertTrue(relatedList.getFileList().contains(relatedRes));

        CmsPublishList mergedList = OpenCms.getPublishManager().mergePublishLists(cms, pubList, relatedList);
        // check the publish list has not been touched
        assertTrue(pubList.getDeletedFolderList().isEmpty());
        assertTrue(pubList.getFolderList().isEmpty());
        assertEquals(1, pubList.getFileList().size());
        assertTrue(pubList.getFileList().contains(resource));
        // check the related publish list has not been touched
        assertTrue(relatedList.getDeletedFolderList().isEmpty());
        assertTrue(relatedList.getFolderList().isEmpty());
        assertEquals(1, relatedList.getFileList().size());
        assertTrue(relatedList.getFileList().contains(relatedRes));
        // check the merged publish list
        assertTrue(mergedList.getDeletedFolderList().isEmpty());
        assertTrue(mergedList.getFolderList().isEmpty());
        assertEquals(2, mergedList.getFileList().size());
        assertTrue(mergedList.getFileList().contains(relatedRes));
        assertTrue(mergedList.getFileList().contains(resource));
    }

    /**
     * Test publishing of related files taken a whole folder structure.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testPublishRelatedFilesInFolder() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publishing of related files taken a whole folder structure");

        String resName = "/folder1/";

        // touch the file to publish        
        cms.lockResource(resName);
        cms.setDateLastModified(resName, System.currentTimeMillis(), true);
        CmsResource resource = cms.readResource(resName, CmsResourceFilter.DEFAULT);

        // get the publish list
        CmsPublishList pubList = OpenCms.getPublishManager().getPublishList(cms, resource, false);
        // just check the publish list
        assertTrue(pubList.getDeletedFolderList().isEmpty());
        assertFalse(pubList.getFolderList().isEmpty());
        assertFalse(pubList.getFileList().isEmpty());

        // get the list of related resources, which should be still empty, since the related resource has unchanged state
        CmsPublishList relatedList = OpenCms.getPublishManager().getRelatedResourcesToPublish(cms, pubList, null);
        // check the publish list has not been touched
        assertTrue(pubList.getDeletedFolderList().isEmpty());
        assertFalse(pubList.getFolderList().isEmpty());
        assertFalse(pubList.getFileList().isEmpty());
        // check the related publish list
        assertTrue(relatedList.getDeletedFolderList().isEmpty());
        assertTrue(relatedList.getFolderList().isEmpty());
        assertTrue(relatedList.getFileList().isEmpty());

        CmsPublishList mergedList = OpenCms.getPublishManager().mergePublishLists(cms, pubList, relatedList);
        // check the publish list has not been touched
        assertTrue(pubList.getDeletedFolderList().isEmpty());
        assertFalse(pubList.getFolderList().isEmpty());
        assertFalse(pubList.getFileList().isEmpty());
        // check the related publish list
        assertTrue(relatedList.getDeletedFolderList().isEmpty());
        assertTrue(relatedList.getFolderList().isEmpty());
        assertTrue(relatedList.getFileList().isEmpty());
        // check the merged publish list
        assertEquals(pubList.getDeletedFolderList(), mergedList.getDeletedFolderList());
        assertEquals(pubList.getFolderList(), mergedList.getFolderList());
        assertEquals(pubList.getFileList(), mergedList.getFileList());
    }

    /**
     * Test publishing new files.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testPublishNewFiles() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publish new files");

        String source = "/folder2/subfolder21/image1.gif";
        String destination1 = "/folder2/image1_new.gif";
        String destination2 = "/folder2/image1_sibling1.gif";
        String destination3 = "/folder2/image1_sibling2.gif";
        String destination4 = "/folder2/image1_sibling3.gif";

        CmsProject onlineProject = cms.readProject("Online");
        CmsProject offlineProject = cms.readProject("Offline");

        // make four copies of a file to be published later
        cms.copyResource(source, destination1, CmsResource.COPY_AS_NEW);
        cms.copyResource(source, destination2, CmsResource.COPY_AS_SIBLING);
        cms.copyResource(source, destination3, CmsResource.COPY_AS_SIBLING);
        cms.copyResource(source, destination4, CmsResource.COPY_AS_SIBLING);

        storeResources(cms, destination1);
        storeResources(cms, destination2);
        storeResources(cms, destination3);
        storeResources(cms, destination4);

        // unlock all new resources
        // do not neet do unlock destination3 as it is a sibling of destination2     
        cms.unlockResource(destination1);
        cms.unlockResource(destination2);

        // publish a new resource
        //
        OpenCms.getPublishManager().publishResource(cms, destination1);
        OpenCms.getPublishManager().waitWhileRunning();

        // the file must be now available in the online project
        cms.getRequestContext().setCurrentProject(onlineProject);
        try {
            cms.readResource(destination1);
        } catch (CmsException e) {
            fail("Resource " + destination1 + " not found in online project:" + e);
        }
        assertFilter(cms, destination1, OpenCmsTestResourceFilter.FILTER_PUBLISHRESOURCE);

        // check if the file in the offline project is unchancged now
        cms.getRequestContext().setCurrentProject(offlineProject);
        assertState(cms, destination1, CmsResource.STATE_UNCHANGED);

        // publish a sibling without publishing other siblings
        //
        OpenCms.getPublishManager().publishResource(cms, destination2);
        OpenCms.getPublishManager().waitWhileRunning();

        // the file must be now available in the online project
        cms.getRequestContext().setCurrentProject(onlineProject);
        try {
            cms.readResource(destination2);
        } catch (CmsException e) {
            fail("Resource " + destination2 + " not found in online project:" + e);
        }
        // the other siblings must not be available in the online project yet
        try {
            cms.readResource(destination3);
            fail("Resource " + destination3 + " should not available online yet");
        } catch (CmsVfsResourceNotFoundException e) {
            // ok
        } catch (CmsException e) {
            fail("Resource " + destination3 + " error:" + e);
        }
        try {
            cms.readResource(destination4);
            fail("Resource " + destination4 + " should not available online yet");
        } catch (CmsVfsResourceNotFoundException e) {
            // ok
        } catch (CmsException e) {
            fail("Resource " + destination4 + " error:" + e);
        }

        assertFilter(cms, destination2, OpenCmsTestResourceFilter.FILTER_PUBLISHRESOURCE);

        // check if the file in the offline project is unchancged now
        cms.getRequestContext().setCurrentProject(offlineProject);
        assertState(cms, destination2, CmsResource.STATE_UNCHANGED);
        // the other siblings in the offline project must still be shown as new
        assertState(cms, destination3, CmsResource.STATE_NEW);
        assertState(cms, destination4, CmsResource.STATE_NEW);

        // publish a sibling and all other siblings of it
        //
        OpenCms.getPublishManager().publishResource(
            cms,
            destination3,
            true,
            new CmsShellReport(cms.getRequestContext().getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();
        // the file and its siblings must be now available in the online project
        cms.getRequestContext().setCurrentProject(onlineProject);
        try {
            cms.readResource(destination3);
        } catch (CmsException e) {
            fail("Resource " + destination3 + " not found in online project:" + e);
        }
        try {
            cms.readResource(destination4);
        } catch (CmsException e) {
            fail("Resource " + destination4 + " not found in online project:" + e);
        }
        assertFilter(cms, destination3, OpenCmsTestResourceFilter.FILTER_PUBLISHRESOURCE);
        assertFilter(cms, destination4, OpenCmsTestResourceFilter.FILTER_PUBLISHRESOURCE);

        // check if the file in the offline project is unchancged now
        cms.getRequestContext().setCurrentProject(offlineProject);
        assertState(cms, destination3, CmsResource.STATE_UNCHANGED);
        assertState(cms, destination4, CmsResource.STATE_UNCHANGED);
    }

    /**
     * Test publishing files within a new unpublished folder.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testPublishNewFilesInNewFolder() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publishing new files in new folder");

        String source = "/folder1/image1.gif";
        String newFolder = "/new_folder/";
        String newFile = newFolder + "new_file";
        String newSibling = newFolder + "new_sibling";

        cms.createResource(newFolder, CmsResourceTypeFolder.getStaticTypeId());
        cms.unlockResource(newFolder);
        storeResources(cms, newFolder);

        // change to test project
        CmsProject project = getTestProject(cms);
        cms.getRequestContext().setCurrentProject(project);

        cms.copyResource(source, newFile, CmsResource.COPY_AS_NEW);
        cms.unlockResource(newFile);

        cms.copyResource(source, newSibling, CmsResource.COPY_AS_SIBLING);
        cms.unlockResource(newSibling);

        // direct publish of the new file will not publish the new file
        echo("Publishing the resource directly");

        storeResources(cms, newFile);

        boolean error = true;
        try {
            // this will generate an error in the log, ensure the test still continues
            OpenCmsTestLogAppender.setBreakOnError(false);
            OpenCms.getPublishManager().publishResource(cms, newFile);
            OpenCms.getPublishManager().waitWhileRunning();
        } catch (CmsMultiException e) {
            CmsVfsException ex = (CmsVfsException)e.getExceptions().get(0);
            if (ex.getMessageContainer().getKey() == org.opencms.db.Messages.ERR_DIRECT_PUBLISH_PARENT_NEW_2) {
                error = false;
            }
        }
        // reset log to stop test on error
        OpenCmsTestLogAppender.setBreakOnError(true);

        if (error) {
            fail("A resource in a new folder could be published without generating an error!");
        }

        assertFilter(cms, newFile, OpenCmsTestResourceFilter.FILTER_EQUAL);

        // direct publish of another sibling will not publish the new sibling
        echo("Publishing another sibling");
        cms.lockResource(source);
        cms.setDateLastModified(source, System.currentTimeMillis(), false);
        cms.unlockResource(source);

        storeResources(cms, newSibling);
        OpenCms.getPublishManager().publishResource(
            cms,
            source,
            true,
            new CmsShellReport(cms.getRequestContext().getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();
        assertFilter(cms, newSibling, OpenCmsTestResourceFilter.FILTER_EQUAL);

        // publishing the test project will not publish the new file or the new sibling
        echo("Publishing the test project");
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        assertFilter(cms, newFile, OpenCmsTestResourceFilter.FILTER_EQUAL);
        assertFilter(cms, newSibling, OpenCmsTestResourceFilter.FILTER_EQUAL);

        // publishing the offline project will publish the folder but not the resources
        echo("Publishing the offline project");

        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        assertFilter(cms, newFolder, OpenCmsTestResourceFilter.FILTER_PUBLISHRESOURCE);
        assertState(cms, newFolder, CmsResource.STATE_UNCHANGED);

        assertFilter(cms, newFile, OpenCmsTestResourceFilter.FILTER_EQUAL);
        assertFilter(cms, newSibling, OpenCmsTestResourceFilter.FILTER_EQUAL);

        // publishing the test proejct again will now publish the resources
        echo("Publishing the test project again");
        cms.getRequestContext().setCurrentProject(project);
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        assertFilter(cms, newFile, OpenCmsTestResourceFilter.FILTER_PUBLISHRESOURCE);
        assertState(cms, newFile, CmsResource.STATE_UNCHANGED);

        assertFilter(cms, newSibling, OpenCmsTestResourceFilter.FILTER_PUBLISHRESOURCE);
        assertState(cms, newSibling, CmsResource.STATE_UNCHANGED);

    }

    /**
     * Tests publishing resources within a distinct project.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testPublishProjectLastmodified() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publishing a distinct project");

        String path = "/folder1";
        String res1 = path + "/page1.html";
        String res2 = path + "/page2.html";

        long timestamp = System.currentTimeMillis();

        cms.getRequestContext().setCurrentProject(cms.readProject("Online"));
        storeResources(cms, res1);

        // change first resource in the offline project
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        cms.lockResource(res1);
        cms.setDateLastModified(res1, timestamp, false);

        cms.unlockProject(cms.getRequestContext().currentProject().getUuid());

        // create a new project
        CmsProject project = getTestProject(cms);
        cms.getRequestContext().setCurrentProject(project);
        cms.copyResourceToProject(path);

        // and change another resource in this project
        cms.lockResource(res2);
        cms.setDateLastModified(res2, timestamp, false);
        cms.unlockProject(cms.getRequestContext().currentProject().getUuid());
        storeResources(cms, res2);

        // when the project is published, only the second resource will be published
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.getRequestContext().setCurrentProject(cms.readProject("Online"));
        assertFilter(cms, res1, OpenCmsTestResourceFilter.FILTER_EQUAL);
        assertFilter(cms, res2, OpenCmsTestResourceFilter.FILTER_PUBLISHRESOURCE);
        assertDateLastModified(cms, res2, timestamp);

        echo("Testing publishing in different projects");

        // change back to the test project, create a new resource
        String res3 = path + "/testPublishProjectLastmodified.txt";
        cms.getRequestContext().setCurrentProject(project);
        cms.createResource(res3, CmsResourceTypePlain.getStaticTypeId());

        // change to offline project, copy resource as sibling
        // this will also change the project in which the source was lastmodified (now "Offline")
        String res4 = "/testPublishProjectLastmodified.txt";
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        cms.copyResource(res3, res4, CmsResource.COPY_AS_SIBLING);

        // check the current state of the resources
        assertState(cms, res3, CmsResource.STATE_NEW);
        assertState(cms, res4, CmsResource.STATE_NEW);

        // publish the test project
        cms.getRequestContext().setCurrentProject(project);
        cms.unlockProject(project.getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        // the resource inside the offline project must not be published (not in test project)
        assertState(cms, res3, CmsResource.STATE_NEW);

        // the sibling outside the offline project must not be published (not in test project)
        assertState(cms, res4, CmsResource.STATE_NEW);

        // publish the root folder of the test project within the test project
        OpenCms.getPublishManager().publishResource(
            cms,
            path,
            true,
            new CmsShellReport(cms.getRequestContext().getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();

        // all resources inside the folder must be published, even if not in test project
        assertState(cms, res3, CmsResource.STATE_UNCHANGED);

        // as well as all siblings, even if not in test project
        assertState(cms, res4, CmsResource.STATE_UNCHANGED);
    }

    /**
     * Tests publishing a folder containing resources modified within a distinct project.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testPublishProjectLastmodifiedFolder() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publishing a folder containing resources modified within a distinct project");

        String path = "/folder1";
        String res1 = path + "/index.html";

        // change first resource in the offline project
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        cms.lockResource(res1);
        cms.setDateLastModified(res1, System.currentTimeMillis(), false);

        cms.unlockProject(cms.getRequestContext().currentProject().getUuid());
        storeResources(cms, res1);

        // create a new project
        CmsProject project = getTestProject(cms);
        cms.getRequestContext().setCurrentProject(project);
        cms.copyResourceToProject(path);

        // check the publish list
        // the resource modified in the Offline project should be there
        CmsPublishList pubList = OpenCms.getPublishManager().getPublishList(cms, cms.readResource(path), false);
        assertTrue(pubList.isPublishSubResources());
        assertTrue(pubList.getDeletedFolderList().isEmpty());
        assertTrue(pubList.getFolderList().isEmpty());
        assertEquals(1, pubList.getFileList().size());
        assertTrue(pubList.getFileList().contains(cms.readResource(res1)));
    }

    /**
     * Tests the publishing of resource property.<p>
     * 
     * take 2 siblings (s1 and s2):
     * - change a shared property of s1
     * - s2 will be marked as changed also
     * - direct publishing s1, should also change s2
     * 
     * @throws Throwable if something goes wrong
     */
    public void testPublishResourceProperty() throws Throwable {

        CmsObject cms = getCmsObject();
        String source = "/folder1/page1.html";
        String sibling = "/folder1/sibling1.html";
        echo("testing the publishing of structure property");

        storeResources(cms, source);
        storeResources(cms, sibling);

        cms.lockResource(sibling);
        cms.writePropertyObject(sibling, new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, null, "test4"));
        cms.unlockResource(sibling);

        assertFilter(cms, sibling, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);
        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);

        storeResources(cms, source);
        storeResources(cms, sibling);

        List pubList = OpenCms.getPublishManager().getPublishList(cms).getFileList();
        assertEquals(pubList.size(), 2);
        assertEquals(((CmsResource)pubList.get(0)).getRootPath(), cms.getRequestContext().addSiteRoot(source));
        assertEquals(((CmsResource)pubList.get(1)).getRootPath(), cms.getRequestContext().addSiteRoot(sibling));

        assertFilter(cms, cms.readResource(source), OpenCmsTestResourceFilter.FILTER_EQUAL);
        assertFilter(cms, cms.readResource(sibling), OpenCmsTestResourceFilter.FILTER_EQUAL);

        OpenCms.getPublishManager().publishResource(cms, source);
        OpenCms.getPublishManager().waitWhileRunning();

        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_PUBLISHRESOURCE);
        assertFilter(cms, sibling, OpenCmsTestResourceFilter.FILTER_PUBLISHRESOURCE);

        storeResources(cms, source);

        assertTrue(OpenCms.getPublishManager().getPublishList(cms).getFileList().isEmpty());
    }

    /**
     * Tests the publishing of structure/shared properties.<p>
     * 
     * take 2 siblings (s1 and s2):
     * - change a individual property of s1 
     * - change a shared property of s2 
     * - direct publishing s2, should not change s1
     * 
     * @throws Throwable if something goes wrong
     */
    public void testPublishSiblings() throws Throwable {

        CmsObject cms = getCmsObject();
        String source = "/folder1/page1.html";
        String sibling = "/folder1/sibling1.html";
        echo("testing the publishing of structure/shared properties");

        storeResources(cms, source);
        storeResources(cms, sibling);

        cms.lockResource(sibling); // individual property
        cms.writePropertyObject(sibling, new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "test2", null));
        cms.unlockResource(sibling);

        assertFilter(cms, sibling, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);

        cms.lockResource(source); // shared property
        cms.writePropertyObject(source, new CmsProperty(CmsPropertyDefinition.PROPERTY_NAVTEXT, null, "test3"));
        cms.unlockResource(source);

        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);

        storeResources(cms, source);
        storeResources(cms, sibling);

        List pubList = OpenCms.getPublishManager().getPublishList(cms).getFileList();
        assertEquals(pubList.size(), 2);
        assertEquals(((CmsResource)pubList.get(0)).getRootPath(), cms.getRequestContext().addSiteRoot(source));
        assertEquals(((CmsResource)pubList.get(1)).getRootPath(), cms.getRequestContext().addSiteRoot(sibling));

        assertFilter(cms, cms.readResource(source), OpenCmsTestResourceFilter.FILTER_EQUAL);
        assertFilter(cms, cms.readResource(sibling), OpenCmsTestResourceFilter.FILTER_EQUAL);

        OpenCms.getPublishManager().publishResource(cms, source);
        OpenCms.getPublishManager().waitWhileRunning();

        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_PUBLISHRESOURCE);
        assertFilter(cms, sibling, OpenCmsTestResourceFilter.FILTER_EQUAL);

        storeResources(cms, source);

        pubList = OpenCms.getPublishManager().getPublishList(cms).getFileList();
        assertEquals(pubList.size(), 1);
        assertEquals(((CmsResource)pubList.get(0)).getRootPath(), cms.getRequestContext().addSiteRoot(sibling));
        assertFilter(cms, cms.readResource(sibling), OpenCmsTestResourceFilter.FILTER_EQUAL);

        OpenCms.getPublishManager().publishResource(cms, sibling);
        OpenCms.getPublishManager().waitWhileRunning();

        assertFilter(cms, sibling, OpenCmsTestResourceFilter.FILTER_PUBLISHRESOURCE);
        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_EQUAL);
    }

    /**
     * Tests the publishing of structure property.<p>
     * 
     * take 2 siblings (s1 and s2):
     * - change a individual property of s1
     * - s2 remains unchanged
     * - direct publishing s1, should not change s2
     * 
     * @throws Throwable if something goes wrong
     */
    public void testPublishStructureProperty() throws Throwable {

        CmsObject cms = getCmsObject();
        String source = "/folder1/page1.html";
        String sibling = "/folder1/sibling1.html";
        echo("testing the publishing of structure property");

        cms.copyResource(source, sibling, CmsResource.COPY_AS_SIBLING);

        // be sure everything is published
        cms.unlockProject(cms.getRequestContext().currentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        storeResources(cms, source);
        storeResources(cms, sibling);

        cms.lockResource(sibling);
        cms.writePropertyObject(sibling, new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "test1", null));
        cms.unlockResource(sibling);

        assertFilter(cms, sibling, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);
        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_SIBLING_PROPERTY);

        storeResources(cms, source);
        storeResources(cms, sibling);

        List pubList = OpenCms.getPublishManager().getPublishList(cms).getFileList();
        assertEquals(pubList.size(), 1);
        assertEquals(((CmsResource)pubList.get(0)).getRootPath(), cms.getRequestContext().addSiteRoot(sibling));
        assertFilter(cms, cms.readResource(sibling), OpenCmsTestResourceFilter.FILTER_EQUAL);

        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        assertFilter(cms, sibling, OpenCmsTestResourceFilter.FILTER_PUBLISHRESOURCE);
        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_EQUAL);
    }

    /**
     * Test publishing a temporary project.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testPublishTemporaryProject() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publish temporary project");

        String source = "/folder1/testfile.txt";
        CmsProject onlineProject = cms.readProject("Online");
        CmsProject offlineProject = cms.readProject("Offline");

        //create a new temp project
        CmsProject tempProject = cms.createProject(
            "deleteme",
            "Temp project to be deleted after publish",
            "Users",
            "Projectmanagers",
            CmsProject.PROJECT_TYPE_TEMPORARY);
        cms.copyResourceToProject("/");
        cms.getRequestContext().setCurrentProject(tempProject);

        // now create a new resource
        cms.createResource(source, CmsResourceTypePlain.getStaticTypeId());
        cms.unlockResource(source);
        storeResources(cms, source);

        cms.readResource(source);

        //publish the project
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        // now try to read the resource
        try {
            cms.readResource(source);
        } catch (CmsException e) {
            fail("Resource "
                + source
                + " not found in project "
                + cms.getRequestContext().currentProject().toString()
                + ":"
                + e);
        }

        // now try to read the resource in the online project
        cms.getRequestContext().setCurrentProject(onlineProject);
        try {
            cms.readResource(source);
        } catch (CmsException e) {
            fail("Resource "
                + source
                + " not found in project "
                + cms.getRequestContext().currentProject().toString()
                + ":"
                + e);
        }

        // check if the state of the resource is ok
        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_PUBLISHRESOURCE);

        // check if the file in the offline project is unchancged now
        cms.getRequestContext().setCurrentProject(offlineProject);
        assertState(cms, source, CmsResource.STATE_UNCHANGED);

        // check if the project is deleted
        try {
            cms.readProject("deleteme");
            fail("Temporary project still existing");
        } catch (CmsException e) {
            // to nothing, this exception must be thrown
        }

    }

    /**
     * Returns a project for testing purposes.<p>
     * 
     * @param cms the cms object
     * @return a project for testing purposes
     */
    CmsProject getTestProject(CmsObject cms) {

        CmsProject project = null;

        try {
            project = cms.readProject("Test");
        } catch (Exception e) {
            try {
                project = cms.createProject("Test", "", "Users", "Administrators");
            } catch (Exception ee) {
                // noop
            }
        }

        return project;
    }
}

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
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for special publish issues.<p>
 */
/**
 * Comment for <code>TestPermissions</code>.<p>
 */
public class TestPublishIssues extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestPublishIssues(String arg0) {

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
        suite.setName(TestPublishIssues.class.getName());

        suite.addTest(new TestPublishIssues("testPublishFolderWithNewFileFromOtherProject"));
        suite.addTest(new TestPublishIssues("testPublishFolderWithChangedFileFromOtherProject"));
        suite.addTest(new TestPublishIssues("testPublishFolderWithDeletedFileFromOtherProject"));
        suite.addTest(new TestPublishIssues("testPublishScenarioA"));
        suite.addTest(new TestPublishIssues("testPublishScenarioB"));
        suite.addTest(new TestPublishIssues("testPublishScenarioC"));
        suite.addTest(new TestPublishIssues("testMultipleProjectCreation"));
        suite.addTest(new TestPublishIssues("testMultipleProjectCreationGalore"));
        suite.addTest(new TestPublishIssues("testDirectPublishWithSiblings"));
        suite.addTest(new TestPublishIssues("testPublishScenarioD"));
        suite.addTest(new TestPublishIssues("testPublishScenarioE"));
        suite.addTest(new TestPublishIssues("testPublishScenarioF"));
        suite.addTest(new TestPublishIssues("testPublishScenarioG"));
        suite.addTest(new TestPublishIssues("testPublishScenarioH"));

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
     * Tests publish scenario "publish all Siblings".<p>
     *
     * This scenario is described as follows:
     * If a direct publish is made, and the option "publish all siblings" is selected,
     * a file that contains siblings will be added multiple times to the publish list,
     * causing a primary key collision in the publish history table.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testDirectPublishWithSiblings() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publish scenario using 'publish all Siblings'");

        cms.lockResource("/folder1/");
        cms.setDateLastModified("/folder1/", System.currentTimeMillis(), true);
        cms.unlockResource("/folder1/");

        // publish the project (this did cause an exception because of primary key violation!)
        CmsUUID publishId = OpenCms.getPublishManager().publishResource(
            cms,
            "/folder1/",
            true,
            new CmsShellReport(cms.getRequestContext().getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();

        // read the published resources from the history
        List publishedResources = cms.readPublishedResources(publishId);

        // make sure the publish history contains the required amount of resources
        assertEquals(35, publishedResources.size());
    }

    /**
     * Tests multiple creation of a project with the same name.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testMultipleProjectCreation() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing multiple creation of a project with the same name");

        String projectName = "projectX";

        // try to read a non-existant project
        try {
            cms.readProject(projectName);
            fail("Required exception was not thrown!");
        } catch (CmsException e) {
            // ok
        }

        // create the project
        cms.createProject(
            projectName,
            "Test project",
            OpenCms.getDefaultUsers().getGroupUsers(),
            OpenCms.getDefaultUsers().getGroupUsers());

        CmsProject project = cms.readProject(projectName);
        cms.getRequestContext().setCurrentProject(project);
        cms.copyResourceToProject("/folder1/");

        // check if the project was created as planned
        List resources = cms.readProjectResources(project);
        assertEquals(1, resources.size());
        assertEquals("/sites/default/folder1/", (String)resources.get(0));

        // copy the root folder of the sito to the project - this must remove the "/folder1/" folder
        cms.copyResourceToProject("/");
        resources = cms.readProjectResources(project);
        assertEquals(1, resources.size());
        assertEquals("/sites/default/", (String)resources.get(0));

        // now create the project again - this must NOT throw an exception since projects may have the same name
        CmsProject newProject = cms.createProject(
            projectName,
            "Test project 2nd time",
            OpenCms.getDefaultUsers().getGroupUsers(),
            OpenCms.getDefaultUsers().getGroupUsers());

        // check if the projects have different ids
        CmsUUID id1 = project.getUuid();
        CmsUUID id2 = newProject.getUuid();
        if (id1 == id2) {
            fail("Two different projects created with same name have the same id!");
        }

        // read the projects again and asserts same name but differnet description
        project = cms.readProject(id1);
        newProject = cms.readProject(id2);
        assertEquals(project.getName(), newProject.getName());
        if (project.getDescription().equals(newProject.getDescription())) {
            fail("Projects should have differnet descriptions!");
        }
    }

    /**
     * Tries to create very many projects with the same name.<p>
     *
     * This displays an issue in the OpenCms v6 project driver, where a project is created with
     * a UNIQUE INDEX based on the given name and the current system time. In rare instances the
     * system time may be identical and an error occurs.<p>
     *
     * The issue was found in the MySQL project driver, but others DBs may be affected as well.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testMultipleProjectCreationGalore() throws Exception {

        CmsObject cms = getCmsObject();

        final String name = "multipleProject";
        final String group = OpenCms.getDefaultUsers().getGroupUsers();
        final String description = "";

        // usually creating the project 20x is enough to replicate the issue
        for (int i = 0; i < 20; i++) {
            cms.createProject(name, description, group, group);
        }
    }

    /**
     * Tests publishing a deleted folder containing a file that was changed in other project.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testPublishFolderWithChangedFileFromOtherProject() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publishing a deleted folder containing a file that was changed in other project");

        // create the initial files
        String folder = "testFolderABC2/";
        cms.createResource(folder, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        String file = folder + "file.txt";
        cms.createResource(file, CmsResourceTypePlain.getStaticTypeId());
        OpenCms.getPublishManager().publishResource(cms, folder);
        OpenCms.getPublishManager().waitWhileRunning();

        CmsProject offline = cms.getRequestContext().getCurrentProject();
        // create a new project and switch to it
        CmsProject prj = cms.createProject(
            "testABC",
            "tets project",
            OpenCms.getDefaultUsers().getGroupAdministrators(),
            OpenCms.getDefaultUsers().getGroupAdministrators());
        cms.getRequestContext().setCurrentProject(prj);
        cms.copyResourceToProject(folder);

        // change the file in the new project
        cms.lockResource(file);
        cms.setDateLastModified(file, System.currentTimeMillis(), false);

        // switch back
        cms.getRequestContext().setCurrentProject(offline);
        // delete folder
        cms.lockResource(folder);
        cms.deleteResource(folder, CmsResource.DELETE_PRESERVE_SIBLINGS);

        // try to publish
        I_CmsReport report = new CmsLogReport(Locale.ENGLISH, getClass());
        OpenCms.getPublishManager().publishResource(cms, folder, true, report);
        OpenCms.getPublishManager().waitWhileRunning();

        // assert
        assertTrue(report.getErrors().isEmpty());
        assertTrue(report.getWarnings().isEmpty());
        assertFalse(cms.existsResource(folder, CmsResourceFilter.ALL));
        assertFalse(cms.existsResource(file, CmsResourceFilter.ALL));
    }

    /**
     * Tests publishing a deleted folder containing a file that was deleted in other project.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testPublishFolderWithDeletedFileFromOtherProject() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publishing a deleted folder containing a file that was deleted in other project");

        // create the initial files
        String folder = "testFolderABC/";
        cms.createResource(folder, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        String file = folder + "file.txt";
        cms.createResource(file, CmsResourceTypePlain.getStaticTypeId());
        OpenCms.getPublishManager().publishResource(cms, folder);
        OpenCms.getPublishManager().waitWhileRunning();

        CmsProject offline = cms.getRequestContext().getCurrentProject();
        // create a new project and switch to it
        CmsProject prj = cms.createProject(
            "testABC",
            "tets project",
            OpenCms.getDefaultUsers().getGroupAdministrators(),
            OpenCms.getDefaultUsers().getGroupAdministrators());
        cms.getRequestContext().setCurrentProject(prj);
        cms.copyResourceToProject(folder);

        // delete the file in the new project
        cms.lockResource(file);
        cms.deleteResource(file, CmsResource.DELETE_PRESERVE_SIBLINGS);

        // switch back
        cms.getRequestContext().setCurrentProject(offline);
        // delete folder
        cms.lockResource(folder);
        cms.deleteResource(folder, CmsResource.DELETE_PRESERVE_SIBLINGS);

        // try to publish
        I_CmsReport report = new CmsLogReport(Locale.ENGLISH, getClass());
        OpenCms.getPublishManager().publishResource(cms, folder, true, report);
        OpenCms.getPublishManager().waitWhileRunning();

        // assert
        assertTrue(report.getErrors().isEmpty());
        assertTrue(report.getWarnings().isEmpty());
        assertFalse(cms.existsResource(folder, CmsResourceFilter.ALL));
        assertFalse(cms.existsResource(file, CmsResourceFilter.ALL));
    }

    /**
     * Tests publishing a deleted folder containing a file that was new created in other project.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testPublishFolderWithNewFileFromOtherProject() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publishing a deleted folder containing a file that was new created in other project");

        // create the initial files
        String folder = "testFolderABC1/";
        cms.createResource(folder, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        OpenCms.getPublishManager().publishResource(cms, folder);
        OpenCms.getPublishManager().waitWhileRunning();

        CmsProject offline = cms.getRequestContext().getCurrentProject();
        // create a new project and switch to it
        CmsProject prj = cms.createProject(
            "testABC",
            "tets project",
            OpenCms.getDefaultUsers().getGroupAdministrators(),
            OpenCms.getDefaultUsers().getGroupAdministrators());
        cms.getRequestContext().setCurrentProject(prj);
        cms.copyResourceToProject(folder);

        // create the file in the new project
        String file = folder + "file.txt";
        cms.createResource(file, CmsResourceTypePlain.getStaticTypeId());

        // switch back
        cms.getRequestContext().setCurrentProject(offline);
        // delete folder
        cms.lockResource(folder);
        cms.deleteResource(folder, CmsResource.DELETE_PRESERVE_SIBLINGS);

        // try to publish
        I_CmsReport report = new CmsLogReport(Locale.ENGLISH, getClass());
        OpenCms.getPublishManager().publishResource(cms, folder, true, report);
        OpenCms.getPublishManager().waitWhileRunning();

        // assert
        assertTrue(report.getErrors().isEmpty());
        assertTrue(report.getWarnings().isEmpty());
        assertFalse(cms.existsResource(folder, CmsResourceFilter.ALL));
        assertFalse(cms.existsResource(file, CmsResourceFilter.ALL));
    }

    /**
     * Tests publish scenario "A".<p>
     *
     * This scenario is described as follows:
     * We have users "test1" and "test2".
     * We have two projects, "project1" and "project2".
     * Project "project1" consists of the folder "/".
     * Project "project2" consists of the folder "/folder1/subfolder11/".
     * User "test2" edits the file "/folder1/subfolder11/index.html".
     * After this, user "test1" locks the folder "/folder1" in "project1", and unlocks it again.
     * User "test2" logs in and now publishes "project2".<p>
     *
     * Wanted result: the changed resource "/folder1/subfolder11/index.html" is published
     * with "project2".<p>
     *
     * The test illustrates a change in the logic from OpenCms 5 to OpenCms 6:
     * In OpenCms 5, locking a file caused it to switch to the current users project.
     * In OpenCms 6, this is no longer true, at last not if you just lock a parent folder.
     * So in OpenCms 5, this test would fail, since the resource would be in "project1", not "project2".<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testPublishScenarioA() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publish scenario A");
        String projectRes1 = "/folder1/subfolder11/";
        String resource1 = projectRes1 + "index.html";
        String resource2 = "/folder1/";
        long timestamp = System.currentTimeMillis();

        // we use the default "Offline" project as "project1"
        CmsProject project1 = cms.readProject("Offline");

        // create "project2" as Admin user
        cms.createProject(
            "project2",
            "Test project 2 for scenario A",
            OpenCms.getDefaultUsers().getGroupUsers(),
            OpenCms.getDefaultUsers().getGroupUsers());

        CmsProject project2 = cms.readProject("project2");
        cms.getRequestContext().setCurrentProject(project2);
        cms.copyResourceToProject(projectRes1);

        // check if the project was created as planned
        List resources = cms.readProjectResources(project2);
        assertEquals(1, resources.size());
        assertEquals("/sites/default" + projectRes1, (String)resources.get(0));

        // login as user "test2"
        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(project2);

        // perform some edit on the file
        cms.lockResource(resource1);
        cms.setDateLastModified(resource1, System.currentTimeMillis(), false);

        // assert some basic status info
        assertDateLastModifiedAfter(cms, resource1, timestamp);
        assertProject(cms, resource1, project2);
        assertLock(cms, resource1, CmsLockType.EXCLUSIVE);
        assertState(cms, resource1, CmsResource.STATE_CHANGED);

        // now login as user "test1" (default will be in the "Offline" project)
        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(project1);

        // lock the folder
        cms.lockResource(resource2);

        // assert some basic status info
        assertProject(cms, resource1, project2);
        assertLock(cms, resource1, CmsLockType.INHERITED);

        // now unlock the folder again
        cms.unlockResource(resource2);

        // back to the user "test2"
        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(project2);

        // get the publish list
        CmsPublishList publishList = OpenCms.getPublishManager().getPublishList(cms);
        assertEquals(1, publishList.getFileList().size());

        // project should have no locked resources
        int resourceProjectCount = cms.countLockedResources(project2.getUuid());
        assertEquals(0, resourceProjectCount);

        // unlock the project
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        // ensure the file was published - state must be "unchanged"
        assertState(cms, resource1, CmsResource.STATE_UNCHANGED);
    }

    /**
     * Tests publish scenario "B".<p>
     *
     * This scenario is described as follows:
     * We have users "test1" and "test2" and projects "projectA" and "projectB".
     * Both projects contain all resources.
     * We have two folders "/foldera/" and "/folderb/".
     * There is a resource "test.txt" that has a sibling in both folders.
     * User "test1" locks folder "/foldera/" and user "test2" locks folder "/folderb".
     * Now both users try to edit the sibling of "test.txt" in their folder.<p>
     *
     * TODO: How are concurrent file modifications avoided on the sibling?
     *
     * @throws Throwable if something goes wrong
     */
    public void testPublishScenarioB() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publish scenario B");

        // set up the test case first
        String folderA = "/foldera/";
        String folderB = "/folderb/";
        String resourceA = folderA + "test.txt";
        String resourceB = folderB + "test.txt";

        // create the resource and the sibling
        cms.createResource(folderA, CmsResourceTypeFolder.getStaticTypeId());
        cms.createResource(folderB, CmsResourceTypeFolder.getStaticTypeId());
        cms.createResource(resourceA, CmsResourceTypePlain.getStaticTypeId());
        cms.createSibling(resourceA, resourceB, Collections.EMPTY_LIST);
        CmsFile cmsFile = cms.readFile(resourceA);
        cmsFile.setContents("Hello, this is a test!".getBytes());
        cms.writeFile(cmsFile);

        // now publish the project
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        // check if the setup was created as planned
        cmsFile = cms.readFile(resourceA);
        assertEquals(2, cmsFile.getSiblingCount());
        assertState(cms, resourceA, CmsResource.STATE_UNCHANGED);
        cmsFile = cms.readFile(resourceB);
        assertEquals(2, cmsFile.getSiblingCount());
        assertState(cms, resourceB, CmsResource.STATE_UNCHANGED);

        // we use the default "Offline" project as "projectA"
        //        CmsProject projectA = cms.readProject("Offline");

        // create "projectB" as Admin user
        cms.createProject(
            "projectB",
            "Test project 2 for scenario B",
            OpenCms.getDefaultUsers().getGroupUsers(),
            OpenCms.getDefaultUsers().getGroupUsers());

        CmsProject projectB = cms.readProject("projectB");
        cms.getRequestContext().setCurrentProject(projectB);
        cms.copyResourceToProject("/");

        // check if the project was created as planned
        List resources = cms.readProjectResources(projectB);
        assertEquals(1, resources.size());
        assertEquals("/sites/default/", (String)resources.get(0));

        // TODO: The wanted behaviour in this case must be defined!
    }

    /**
     * Tests publish scenario "C".<p>
     *
     * This scenario is described as follows:
     * Direct publishing of folders containing subfolders skips all changed subfolders e.g. direct publish of /folder1/
     * publishes /folder1/ and /folder1/index.html/, but not /folder1/subfolder11/.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testPublishScenarioC() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publish scenario C");

        long touchTime = System.currentTimeMillis();

        cms.lockResource("/folder1/");
        cms.setDateLastModified("/folder1/", touchTime, false);
        cms.setDateLastModified("/folder1/index.html", touchTime, false);
        cms.setDateLastModified("/folder1/subfolder11/", touchTime, false);
        cms.setDateLastModified("/folder1/subfolder11/index.html", touchTime, false);

        cms.unlockResource("/folder1/");
        OpenCms.getPublishManager().publishResource(cms, "/folder1/");
        OpenCms.getPublishManager().waitWhileRunning();

        assertState(cms, "/folder1/", CmsResource.STATE_UNCHANGED);
        assertState(cms, "/folder1/index.html", CmsResource.STATE_UNCHANGED);
        assertState(cms, "/folder1/subfolder11/", CmsResource.STATE_UNCHANGED);
        assertState(cms, "/folder1/subfolder11/index.html", CmsResource.STATE_UNCHANGED);

        cms.createResource("/folder_a/", CmsResourceTypeFolder.getStaticTypeId());
        cms.createResource("/folder_a/file_a.txt", CmsResourceTypePlain.getStaticTypeId());
        cms.createResource("/folder_a/folder_b/", CmsResourceTypeFolder.getStaticTypeId());
        cms.createResource("/folder_a/folder_b/file_b.txt", CmsResourceTypePlain.getStaticTypeId());

        cms.unlockResource("/folder_a/");
        OpenCms.getPublishManager().publishResource(cms, "/folder_a/");
        OpenCms.getPublishManager().waitWhileRunning();

        assertState(cms, "/folder_a/", CmsResource.STATE_UNCHANGED);
        assertState(cms, "/folder_a/file_a.txt", CmsResource.STATE_UNCHANGED);
        assertState(cms, "/folder_a/folder_b/", CmsResource.STATE_UNCHANGED);
        assertState(cms, "/folder_a/folder_b/file_b.txt", CmsResource.STATE_UNCHANGED);

    }

    /**
     * Tests publish scenario "D".<p>
     *
     * This scenario is described as follows:
     *
     * Direct publishing of folders containing subfolders skips all (sibling)
     * resources in subfolders.
     *
     * e.g. direct publish of /folder2/folder1/
     * publishes /folder2/folder1/ and /folder2/folder1/index.html/,
     * but not /folder2/folder1/subfolder11/index.html.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testPublishScenarioD() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publish scenario D");

        cms.lockResource("/folder1/");
        // copy the whole folder creating siblings of all resources
        cms.copyResource("/folder1/", "/folder2/folder1", CmsResource.COPY_AS_SIBLING);
        cms.unlockResource("/folder1/");

        // direct publish the new folder
        cms.unlockResource("/folder2/folder1/");
        OpenCms.getPublishManager().publishResource(cms, "/folder2/folder1/");
        OpenCms.getPublishManager().waitWhileRunning();

        // check the state of all resources
        Iterator itResources = cms.readResources("/folder2/folder1/", CmsResourceFilter.ALL, true).iterator();
        while (itResources.hasNext()) {
            CmsResource res = (CmsResource)itResources.next();
            assertEquals(res.getState(), CmsResource.STATE_UNCHANGED);
        }
    }

    /**
     * Tests publish scenario "E".<p>
     *
     * This scenario is described as follows:
     *
     * Deletion of folders containing shared locked siblings,
     * after copying a folder creating siblings into a new folder and publishing. <p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testPublishScenarioE() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publish scenario E");

        // change to the offline project
        CmsProject project = cms.readProject("Offline");
        cms.getRequestContext().setCurrentProject(project);

        // create folder
        cms.createResource("/test", CmsResourceTypeFolder.getStaticTypeId());

        // create siblings
        cms.copyResource("/folder1/subfolder12/subsubfolder121", "/test/subtest", CmsResource.COPY_AS_SIBLING);

        // publish
        cms.unlockResource("/test");
        OpenCms.getPublishManager().publishResource(cms, "/test");
        OpenCms.getPublishManager().waitWhileRunning();

        // lock sibling
        cms.lockResource("/folder1/subfolder12/subsubfolder121/image1.gif");
        CmsUser user = cms.getRequestContext().getCurrentUser();

        // login as user test2
        cms.addUserToGroup("test2", "Projectmanagers");
        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(project);

        // check lock
        assertLock(cms, "/test/subtest/image1.gif", CmsLockType.SHARED_EXCLUSIVE, user);

        // delete the folder
        cms.lockResource("/test/subtest");
        cms.deleteResource("/test/subtest", CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.unlockResource("/test/subtest");

        // publish
        OpenCms.getPublishManager().publishResource(cms, "/test");
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Tests publish scenario "F".<p>
     *
     * This scenario is described as follows:
     *
     * We have 2 siblings: sibA.txt and sibB.txt
     * We set a shared property and we publish
     * just one sibling let's say sibA.txt.
     *
     * After publishing both siblings should be unchanged
     *
     * @throws Throwable if something goes wrong
     */
    public void testPublishScenarioF() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publish scenario F");

        String sibA = "sibA.txt";
        String sibB = "sibB.txt";

        cms.createResource(sibA, CmsResourceTypePlain.getStaticTypeId());
        cms.createSibling(sibA, sibB, null);

        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        CmsProperty prop = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, null, "shared");
        cms.lockResource(sibA);
        cms.writePropertyObject(sibA, prop);

        assertState(cms, sibA, CmsResource.STATE_CHANGED);
        assertState(cms, sibB, CmsResource.STATE_CHANGED);

        cms.unlockResource(sibA);
        OpenCms.getPublishManager().publishResource(
            cms,
            sibA,
            false,
            new CmsShellReport(cms.getRequestContext().getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();

        assertState(cms, sibA, CmsResource.STATE_UNCHANGED);
        assertState(cms, sibB, CmsResource.STATE_UNCHANGED);
    }

    /**
     * Tests publish scenario "G".<p>
     *
     * This scenario is described as follows:
     *
     * We have 2 siblings: sib1.txt and sib2.txt
     * We do a content modification and we publish
     * just one sibling let's say sib1.txt.
     *
     * After publishing both siblings should be unchanged
     *
     * @throws Throwable if something goes wrong
     */
    public void testPublishScenarioG() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publish scenario G");

        String sib1 = "sib1.txt";
        String sib2 = "sib2.txt";

        cms.createResource(sib1, CmsResourceTypePlain.getStaticTypeId());
        cms.createSibling(sib1, sib2, null);

        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        CmsFile file = cms.readFile(sib1);
        file.setContents("abc".getBytes());
        cms.lockResource(sib1);
        cms.writeFile(file);

        assertState(cms, sib1, CmsResource.STATE_CHANGED);
        assertState(cms, sib2, CmsResource.STATE_CHANGED);

        cms.unlockResource(sib1);
        OpenCms.getPublishManager().publishResource(
            cms,
            sib1,
            false,
            new CmsShellReport(cms.getRequestContext().getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();

        assertState(cms, sib1, CmsResource.STATE_UNCHANGED);
        assertState(cms, sib2, CmsResource.STATE_UNCHANGED);
    }

    /**
     * Tests publish scenario "H".<p>
     *
     * This scenario is described as follows:
     *
     * We have 2 unchanged siblings: sibX.txt and sibY.txt
     * Now we set a different individual property on each sibling
     * and we publish just one sibling let's say sibX.txt.
     *
     * After publishing only sibX.txt should be unchanged
     * and sibY.txt should still be changed
     *
     * @throws Throwable if something goes wrong
     */
    public void testPublishScenarioH() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publish scenario H");

        String sibX = "sibX.txt";
        String sibY = "sibY.txt";

        cms.createResource(sibX, CmsResourceTypePlain.getStaticTypeId());
        cms.createSibling(sibX, sibY, null);

        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        CmsProperty propX = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "individual X", null);
        cms.lockResource(sibX);
        cms.writePropertyObject(sibX, propX);

        assertState(cms, sibX, CmsResource.STATE_CHANGED);
        assertState(cms, sibY, CmsResource.STATE_UNCHANGED);

        CmsProperty propY = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "individual Y", null);
        cms.lockResource(sibY);
        cms.writePropertyObject(sibY, propY);

        assertState(cms, sibX, CmsResource.STATE_CHANGED);
        assertState(cms, sibY, CmsResource.STATE_CHANGED);

        cms.unlockResource(sibX);
        OpenCms.getPublishManager().publishResource(
            cms,
            sibX,
            false,
            new CmsShellReport(cms.getRequestContext().getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();

        assertState(cms, sibX, CmsResource.STATE_UNCHANGED);
        assertState(cms, sibY, CmsResource.STATE_CHANGED);

        OpenCms.getPublishManager().publishResource(
            cms,
            sibY,
            false,
            new CmsShellReport(cms.getRequestContext().getLocale()));
        OpenCms.getPublishManager().waitWhileRunning();

        assertState(cms, sibX, CmsResource.STATE_UNCHANGED);
        assertState(cms, sibY, CmsResource.STATE_UNCHANGED);
    }
}

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

import org.opencms.file.CmsResource.CmsResourceDeleteMode;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestResourceFilter;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests OpenCms projects.<p>
 */
public class TestProjects extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestProjects(String arg0) {

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
        suite.setName(TestProjects.class.getName());

        suite.addTest(new TestProjects("testCreateDeleteProject"));
        suite.addTest(new TestProjects("testCopyResourceToProject"));
        suite.addTest(new TestProjects("testDeleteProjectWithResources"));
        suite.addTest(new TestProjects("testReadProjectResources"));
        suite.addTest(new TestProjects("testAccessibleProjects"));
        suite.addTest(new TestProjects("testDeleteNewFolderInProject"));
        suite.addTest(new TestProjects("testDeleteFolderInProject"));
        suite.addTest(new TestProjects("testMoveFolderInProject"));

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
     * Test the "getAllAccessibleProjects" method.<p>
     *
     * @throws Exception if the test fails
     */
    public void testAccessibleProjects() throws Exception {

        CmsObject cms = getCmsObject();

        echo("Testing to read all accessible projects");

        // restore the after setup scenario
        cms.deleteProject(cms.readProject("UnitTest1").getUuid());
        cms.deleteProject(cms.readProject("UnitTest4").getUuid());

        List projects = OpenCms.getOrgUnitManager().getAllAccessibleProjects(cms, "", true);
        assertEquals(2, projects.size());
        assertTrue(projects.contains(cms.readProject(CmsProject.ONLINE_PROJECT_ID)));
        assertTrue(projects.contains(cms.readProject("Offline")));

        OpenCms.getOrgUnitManager().createOrganizationalUnit(cms, "/test/", "test ou", 0, "/folder1");

        projects = OpenCms.getOrgUnitManager().getAllAccessibleProjects(cms, "", true);
        assertEquals(3, projects.size());
        assertTrue(projects.contains(cms.readProject(CmsProject.ONLINE_PROJECT_ID)));
        assertTrue(projects.contains(cms.readProject("Offline")));
        assertTrue(projects.contains(cms.readProject("test/Offline")));

        cms.createUser("/test/user1", "user1", "my test user", null);
        cms.loginUser("/test/user1", "user1");

        projects = OpenCms.getOrgUnitManager().getAllAccessibleProjects(cms, "", true);
        assertEquals(1, projects.size());
        assertTrue(projects.contains(cms.readProject(CmsProject.ONLINE_PROJECT_ID)));

        cms = getCmsObject();
        OpenCms.getRoleManager().addUserToRole(cms, CmsRole.WORKPLACE_USER.forOrgUnit("/test/"), "/test/user1");
        cms.loginUser("/test/user1", "user1");

        projects = OpenCms.getOrgUnitManager().getAllAccessibleProjects(cms, "", true);
        assertEquals(2, projects.size());
        assertTrue(projects.contains(cms.readProject(CmsProject.ONLINE_PROJECT_ID)));
        assertTrue(projects.contains(cms.readProject("test/Offline")));
    }

    /**
     * Test the "copy resource to project" function.<p>
     *
     * @throws Exception if the test fails
     */
    public void testCopyResourceToProject() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing copying a resource to a project");

        String projectName = "UnitTest1";

        String oldSite = cms.getRequestContext().getSiteRoot();
        cms.getRequestContext().setSiteRoot("/");
        try {
            CmsProject project = cms.createProject(
                projectName,
                "Unit test project 1",
                OpenCms.getDefaultUsers().getGroupUsers(),
                OpenCms.getDefaultUsers().getGroupAdministrators(),
                CmsProject.PROJECT_TYPE_NORMAL);
            cms.getRequestContext().setCurrentProject(project);
            cms.copyResourceToProject("/sites/default/index.html");
            cms.copyResourceToProject("/sites/default/folder1/");
        } finally {
            cms.getRequestContext().setSiteRoot(oldSite);
        }

        CmsProject current = cms.readProject(projectName);
        cms.getRequestContext().setCurrentProject(current);

        // some basic project tests
        assertEquals(projectName, current.getName());
        assertFalse(current.isOnlineProject());

        // check the project resources
        List currentResources = cms.readProjectResources(current);
        assertTrue(CmsProject.isInsideProject(currentResources, "/sites/default/index.html"));
        assertTrue(CmsProject.isInsideProject(currentResources, "/sites/default/folder1/"));
        assertTrue(CmsProject.isInsideProject(currentResources, "/sites/default/folder1/subfolder11/index.html"));
        assertFalse(CmsProject.isInsideProject(currentResources, "/sites/default/"));
        assertFalse(CmsProject.isInsideProject(currentResources, "/"));
        assertFalse(CmsProject.isInsideProject(currentResources, "/sites/default/folder2/index.html"));
    }

    /**
     * Test the "createProject" and "deleteProject" methods.<p>
     *
     * @throws Exception if the test fails
     */
    public void testCreateDeleteProject() throws Exception {

        CmsObject cms = getCmsObject();

        echo("Testing creating a project");

        String projectName = "UnitTest2";

        CmsProject project = cms.createProject(
            projectName,
            "Unit test project 2",
            OpenCms.getDefaultUsers().getGroupUsers(),
            OpenCms.getDefaultUsers().getGroupAdministrators(),
            CmsProject.PROJECT_TYPE_NORMAL);

        // some basic project tests
        assertEquals(projectName, project.getName());
        assertFalse(project.isOnlineProject());

        // ensure the project is now accessible
        List projects = OpenCms.getOrgUnitManager().getAllAccessibleProjects(cms, "", true);
        int i;
        for (i = 0; i < projects.size(); i++) {
            if (((CmsProject)projects.get(i)).getUuid().equals(project.getUuid())) {
                break;
            }
        }
        if (i >= projects.size()) {
            fail("Project " + project.getName() + " not accessible");
        }

        // ensure the project is manageable
        projects = OpenCms.getOrgUnitManager().getAllManageableProjects(cms, "", true);
        for (i = 0; i < projects.size(); i++) {
            if (((CmsProject)projects.get(i)).getUuid().equals(project.getUuid())) {
                break;
            }
        }
        if (i >= projects.size()) {
            fail("Project " + project.getName() + "not manageable");
        }

        echo("Testing deleting a project");

        // try to delete the project
        cms.deleteProject(project.getUuid());

        // ensure the project is not accessible anymore
        projects = OpenCms.getOrgUnitManager().getAllAccessibleProjects(cms, "", true);
        for (i = 0; i < projects.size(); i++) {
            if (((CmsProject)projects.get(i)).getUuid().equals(project.getUuid())) {
                fail("Project " + project.getName() + "not deleted");
            }
        }
    }

    /**
     * Test the "delete folder in project" function.<p>
     *
     * @throws Exception if the test fails
     */
    public void testDeleteFolderInProject() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing deleting folder in project");

        String projectName = "testDeleteFolderInProject";

        cms.getRequestContext().setSiteRoot("/sites/default/");
        String folderName = "/types/";

        // create new project
        CmsProject project = cms.createProject(
            projectName,
            projectName,
            OpenCms.getDefaultUsers().getGroupUsers(),
            OpenCms.getDefaultUsers().getGroupAdministrators(),
            CmsProject.PROJECT_TYPE_NORMAL);
        cms.getRequestContext().setCurrentProject(project);
        // add folder to project
        cms.copyResourceToProject(folderName);

        // check the project resources
        List<String> resNames = cms.readProjectResources(project);
        assertEquals(1, resNames.size());
        assertEquals(cms.getRequestContext().addSiteRoot(folderName), resNames.get(0));

        // delete folder
        cms.lockResource(folderName);
        cms.deleteResource(folderName, CmsResourceDeleteMode.MODE_DELETE_REMOVE_SIBLINGS);

        // check the project resources
        resNames = cms.readProjectResources(project);
        assertEquals(1, resNames.size());
        assertEquals(cms.getRequestContext().addSiteRoot(folderName), resNames.get(0));

        // publish folder
        OpenCms.getPublishManager().publishResource(cms, folderName);
        OpenCms.getPublishManager().waitWhileRunning();

        // check the project resources
        resNames = cms.readProjectResources(project);
        assertTrue(resNames.isEmpty());

        // check now delete a parent folder
        folderName = "/folder2/";
        String subfolder = "/folder2/subfolder21/";

        // add folder to project
        cms.copyResourceToProject(subfolder);

        // check the project resources
        resNames = cms.readProjectResources(project);
        assertEquals(1, resNames.size());
        assertEquals(cms.getRequestContext().addSiteRoot(subfolder), resNames.get(0));

        // delete folder
        cms.lockResource(folderName);
        cms.deleteResource(folderName, CmsResourceDeleteMode.MODE_DELETE_REMOVE_SIBLINGS);

        // check the project resources
        resNames = cms.readProjectResources(project);
        assertEquals(1, resNames.size());
        assertEquals(cms.getRequestContext().addSiteRoot(subfolder), resNames.get(0));

        // publish folder
        OpenCms.getPublishManager().publishResource(cms, folderName);
        OpenCms.getPublishManager().waitWhileRunning();

        // check the project resources
        resNames = cms.readProjectResources(project);
        assertTrue(resNames.isEmpty());
    }

    /**
     * Test the "delete new folder in project" function.<p>
     *
     * @throws Exception if the test fails
     */
    public void testDeleteNewFolderInProject() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing deleting new folder in project");

        String projectName = "testDeleteNewFolderInProject";

        cms.getRequestContext().setSiteRoot("/sites/default/");
        String folderName = "/testDeleteNewFolderInProject/";
        // create new folder
        cms.createResource(folderName, CmsResourceTypeFolder.RESOURCE_TYPE_ID);

        // create new project
        CmsProject project = cms.createProject(
            projectName,
            projectName,
            OpenCms.getDefaultUsers().getGroupUsers(),
            OpenCms.getDefaultUsers().getGroupAdministrators(),
            CmsProject.PROJECT_TYPE_NORMAL);
        cms.getRequestContext().setCurrentProject(project);
        // add new folder to project
        cms.copyResourceToProject(folderName);

        // check the project resources
        List<String> resNames = cms.readProjectResources(project);
        assertEquals(1, resNames.size());
        assertEquals(cms.getRequestContext().addSiteRoot(folderName), resNames.get(0));

        // delete folder
        cms.deleteResource(folderName, CmsResourceDeleteMode.MODE_DELETE_REMOVE_SIBLINGS);

        // check the project resources
        resNames = cms.readProjectResources(project);
        assertTrue(resNames.isEmpty());

        // test now deleting a parent folder
        String subfolderName = "/testDeleteNewFolderInProject/test/";
        // create new folder
        cms.createResource(folderName, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.createResource(subfolderName, CmsResourceTypeFolder.RESOURCE_TYPE_ID);

        // add new folder to project
        cms.copyResourceToProject(subfolderName);

        // check the project resources
        resNames = cms.readProjectResources(project);
        assertEquals(1, resNames.size());
        assertEquals(cms.getRequestContext().addSiteRoot(subfolderName), resNames.get(0));

        // delete folder
        cms.deleteResource(folderName, CmsResourceDeleteMode.MODE_DELETE_REMOVE_SIBLINGS);

        // check the project resources
        resNames = cms.readProjectResources(project);
        assertTrue(resNames.isEmpty());
    }

    /**
     * Test the "delete project with resources" function.<p>
     *
     * @throws Exception if the test fails
     */
    public void testDeleteProjectWithResources() throws Exception {

        CmsObject cms = getCmsObject();

        echo("Creating a project for deletion test with resources");

        String projectName = "UnitTest3";

        CmsProject project = cms.createProject(
            projectName,
            "Unit test project 3",
            OpenCms.getDefaultUsers().getGroupUsers(),
            OpenCms.getDefaultUsers().getGroupAdministrators(),
            CmsProject.PROJECT_TYPE_NORMAL);

        // use the main folder as start folder for the project
        String resource = "/";

        // store the resource
        storeResources(cms, resource);

        // switch to the project
        cms.getRequestContext().setCurrentProject(project);

        // copy the main site folder to the project
        cms.copyResourceToProject("/");

        // some basic project tests
        assertEquals(projectName, project.getName());
        assertFalse(project.isOnlineProject());

        // do some changes to the project
        cms.lockResource(resource);
        cms.setDateLastModified("/folder1/", System.currentTimeMillis(), true);
        cms.deleteResource("/folder2/", CmsResource.DELETE_REMOVE_SIBLINGS);
        cms.createResource("/folder3/", CmsResourceTypeFolder.getStaticTypeId(), null, Collections.EMPTY_LIST);
        cms.createResource(
            "/folder3/test.txt",
            CmsResourceTypePlain.getStaticTypeId(),
            "".getBytes(),
            Collections.EMPTY_LIST);
        cms.unlockResource(resource);

        // switch to the offline project
        CmsProject offlineProject = cms.readProject("Offline");
        cms.getRequestContext().setCurrentProject(offlineProject);

        // now delete the project - all changes in the project must be undone
        cms.deleteProject(project.getUuid());

        // ensure that the original resources are unchanged
        assertFilter(cms, resource, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);

        // all resources within the folder must  be unchanged now
        Iterator j = cms.readResources(resource, CmsResourceFilter.ALL, true).iterator();
        while (j.hasNext()) {
            CmsResource res = (CmsResource)j.next();
            String resName = cms.getSitePath(res);

            // now evaluate the result
            assertFilter(cms, resName, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
        }
    }

    /**
     * Test the "move folder in project" function.<p>
     *
     * @throws Exception if the test fails
     */
    public void testMoveFolderInProject() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing moving folder in project");

        String projectName = "testMoveFolderInProject";

        cms.getRequestContext().setSiteRoot("/sites/default/");
        String folderName = "/xmlcontent/";

        // create new project
        CmsProject project = cms.createProject(
            projectName,
            projectName,
            OpenCms.getDefaultUsers().getGroupUsers(),
            OpenCms.getDefaultUsers().getGroupAdministrators(),
            CmsProject.PROJECT_TYPE_NORMAL);
        cms.getRequestContext().setCurrentProject(project);
        // add folder to project
        cms.copyResourceToProject(folderName);

        // check the project resources
        List<String> resNames = cms.readProjectResources(project);
        assertEquals(1, resNames.size());
        assertEquals(cms.getRequestContext().addSiteRoot(folderName), resNames.get(0));

        // move folder
        String destFolder = "/testMoveFolderInProject/";
        cms.lockResource(folderName);
        cms.moveResource(folderName, destFolder);

        // check the project resources
        resNames = cms.readProjectResources(project);
        assertEquals(1, resNames.size());
        assertEquals(cms.getRequestContext().addSiteRoot(destFolder), resNames.get(0));

        // undo changes
        cms.undoChanges(destFolder, CmsResource.UNDO_MOVE_CONTENT);

        // check the project resources
        resNames = cms.readProjectResources(project);
        assertEquals(1, resNames.size());
        assertEquals(cms.getRequestContext().addSiteRoot(folderName), resNames.get(0));
    }

    /**
     * Test the "readProjectResources" method.<p>
     *
     * @throws Exception if the test fails
     */
    public void testReadProjectResources() throws Exception {

        CmsObject cms = getCmsObject();

        echo("Testing to read all project resources");

        String projectName = "UnitTest4";

        String oldSite = cms.getRequestContext().getSiteRoot();
        cms.getRequestContext().setSiteRoot("/");
        try {
            CmsProject project = cms.createProject(
                projectName,
                "Unit test project 4",
                OpenCms.getDefaultUsers().getGroupUsers(),
                OpenCms.getDefaultUsers().getGroupAdministrators(),
                CmsProject.PROJECT_TYPE_NORMAL);
            cms.getRequestContext().setCurrentProject(project);
            cms.copyResourceToProject("/sites/default/index.html");
            cms.copyResourceToProject("/sites/default/folder1/");
        } finally {
            cms.getRequestContext().setSiteRoot(oldSite);
        }

        CmsProject current = cms.readProject(projectName);
        cms.getRequestContext().setCurrentProject(current);

        // some basic project tests
        assertEquals(projectName, current.getName());
        assertFalse(current.isOnlineProject());

        // check the project resources
        List projectResources = cms.readProjectResources(current);

        // check the project resource list
        assertEquals(2, projectResources.size());
        assertTrue(projectResources.contains("/sites/default/index.html"));
        assertTrue(projectResources.contains("/sites/default/folder1/"));
    }
}

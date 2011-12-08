/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workflow;

import org.opencms.ade.publish.shared.CmsWorkflowResponse;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestLogAppender;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import junit.framework.Test;

/**
 * A test for the default workflow manager implementation.<p>
 */
public class TestWorkflow extends OpenCmsTestCase {

    /**
     * Standard test constructor.<p>
     * 
     * @param name the test name 
     */
    public TestWorkflow(String name) {

        super(name);

    }

    /**
     * Creates a test suite instance for this test case.<p>
     * 
     * @return the test suite 
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        return generateSetupTestWrapper(TestWorkflow.class, "systemtest", "/");
    }

    public void testChangeProject() throws Exception {

        CmsObject cms = getCmsObject();
        CmsProject offline = cms.readProject("Offline");
        CmsProject blah = cms.createProject("blah", "blah", "Projectmanagers", "Projectmanagers");
        CmsResource res = cms.createResource("/system/cpr", 1);
        cms.writeProjectLastModified(res, blah);
        assertEquals("blah", getProjectLastModified("/system/cpr").getName());
        publish("/system/cpr");
        CmsProject lastModified = getProjectLastModified("/system/cpr");
        assertEquals("blah", lastModified.getName());
    }

    /**
     * Tests whether the workflow projects are cleaned up when all their resources are published.<p>
     *  
     * @throws CmsException
     */
    public void testCleanupProjects() throws CmsException {

        try {
            OpenCmsTestLogAppender.setBreakOnError(false);
            CmsWorkflowManager wfm = OpenCms.getWorkflowManager();
            CmsObject cms = getCmsObject();
            List<CmsResource> resources = new ArrayList<CmsResource>();
            CmsResource r1 = cms.createResource("/system/r5", 1);
            CmsResource r2 = cms.createResource("/system/r6", 1);
            resources.add(r1);
            resources.add(r2);
            CmsWorkflowResponse response = wfm.executeAction(
                getCmsObject(),
                CmsWorkflowManager.ACTION_RELEASE,
                resources);
            CmsUUID workflowId = response.getWorkflowId();
            assertTrue("Project should exist", existsProject(workflowId));
            publish("/system/r5");
            assertTrue("Project should still exist because it isn't empty yet.", existsProject(workflowId));
            publish("/system/r6");
            assertFalse(
                "The project should have been removed because all the workflow resources have been published.",
                existsProject(workflowId));
        } finally {
            OpenCmsTestLogAppender.setBreakOnError(true);
        }
    }

    /**
     * Tests that the release action fails in the online project.<p>
     * 
     * @throws CmsException
     */
    public void testFailReleaseOnline() throws CmsException {

        CmsWorkflowManager wfm = OpenCms.getWorkflowManager();
        CmsObject cms = OpenCms.initCmsObject(getCmsObject());
        List<CmsResource> resources = new ArrayList<CmsResource>();
        CmsResource r = cms.createResource("/system/r4", 1);
        resources.add(r);
        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_NAME));
        try {
            wfm.executeAction(cms, CmsWorkflowManager.ACTION_RELEASE, resources);
        } catch (CmsException e) {
            return;
        }
        assertTrue("Should have failed", false);
    }

    /**
     * Tests the 'publish' workflow action.<p>
     * 
     * @throws CmsException
     */
    public void testWorkflowForcePublish() throws CmsException {

        CmsWorkflowManager wfm = OpenCms.getWorkflowManager();
        CmsObject cms = getCmsObject();
        List<CmsResource> resources = new ArrayList<CmsResource>();
        CmsResource r = cms.createResource("/system/r2", 1);
        resources.add(r);
        wfm.executeAction(getCmsObject(), CmsWorkflowManager.ACTION_FORCE_PUBLISH, resources);
        OpenCms.getPublishManager().waitWhileRunning();
        CmsResource rRead = cms.readResource("/system/r2");
        assertTrue(rRead.getState().isUnchanged());
    }

    /**
     * Tests the 'publish' workflow action.<p>
     * 
     * @throws CmsException
     */
    public void testWorkflowPublish() throws CmsException {

        CmsWorkflowManager wfm = OpenCms.getWorkflowManager();
        CmsObject cms = getCmsObject();
        List<CmsResource> resources = new ArrayList<CmsResource>();
        CmsResource r = cms.createResource("/system/r1", 1);
        resources.add(r);
        wfm.executeAction(getCmsObject(), CmsWorkflowManager.ACTION_PUBLISH, resources);
        OpenCms.getPublishManager().waitWhileRunning();
        CmsResource rRead = cms.readResource("/system/r1");
        assertTrue(rRead.getState().isUnchanged());
    }

    /**
     * Tests the 'release' workflow action.<p>
     * 
     * @throws CmsException 
     */
    public void testWorkflowRelease() throws CmsException {

        try {
            OpenCmsTestLogAppender.setBreakOnError(false);

            CmsWorkflowManager wfm = OpenCms.getWorkflowManager();
            CmsObject cms = getCmsObject();
            List<CmsResource> resources = new ArrayList<CmsResource>();
            CmsResource r = cms.createResource("/system/r3", 1);
            resources.add(r);
            CmsWorkflowResponse response = wfm.executeAction(
                getCmsObject(),
                CmsWorkflowManager.ACTION_RELEASE,
                resources);
            assertTrue(response.isSuccess());
            CmsUUID workflowId = response.getWorkflowId();
            CmsProject project = cms.readProject(workflowId);
            assertEquals(CmsProject.PROJECT_TYPE_WORKFLOW.getMode(), project.getType().getMode());
            CmsResource rRead = cms.readResource("/system/r3");
            assertEquals(workflowId, rRead.getProjectLastModified());
            assertFalse(workflowId.equals(cms.getRequestContext().getCurrentProject().getUuid()));
        } finally {
            OpenCmsTestLogAppender.setBreakOnError(true);

        }
    }

    /**
     * Tests the 'release' workflow action when executed by another user.<p>
     *  
     * @throws Exception
     */
    public void testWorkflowWithDifferentUser() throws Exception {

        try {
            OpenCmsTestLogAppender.setBreakOnError(false);
            CmsObject cms = getCmsObject();
            CmsUser editor = cms.createUser("Editor", "password", "desc", new HashMap<String, Object>());
            cms.addUserToGroup("Editor", "Users");
            cms.lockResource("/system/");
            cms.chacc("/system/", I_CmsPrincipal.PRINCIPAL_GROUP, "Users", "+w");
            cms.unlockResource("/system/");
            CmsObject editorCms = OpenCms.initCmsObject(cms);
            editorCms.loginUser("Editor", "password");
            editorCms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
            CmsResource first = editorCms.createResource("/system/er1", 1);
            CmsResource second = editorCms.createResource("/system/er2", 1);
            CmsResource third = editorCms.createResource("/system/er3", 1);
            List<CmsResource> resources = new ArrayList<CmsResource>();
            resources.add(first);
            resources.add(second);
            resources.add(third);
            OpenCms.getWorkflowManager().executeAction(editorCms, CmsWorkflowManager.ACTION_RELEASE, resources);
            first = cms.readResource("/system/er1");
            second = cms.readResource("/system/er2");
            third = cms.readResource("/system/er3");
            assertEquals(editor.getId(), first.getUserLastModified());
            assertEquals(editor.getId(), second.getUserLastModified());
            assertEquals(editor.getId(), third.getUserLastModified());
        } finally {
            OpenCmsTestLogAppender.setBreakOnError(false);
        }
    }

    /**
     * Tests that the release action can deal with locks which released resources inherit.<p>
     * 
     * @throws Exception
     */
    public void testWorkflowWithInheritedLock() throws Exception {

        try {
            OpenCmsTestLogAppender.setBreakOnError(false);
            CmsObject cms = getCmsObject();
            CmsUser editor = cms.createUser("Editor2", "password", "desc", new HashMap<String, Object>());
            cms.addUserToGroup("Editor2", "Users");
            cms.lockResource("/system/");
            cms.chacc("/system/", I_CmsPrincipal.PRINCIPAL_GROUP, "Users", "+w");
            cms.unlockResource("/system/");
            CmsObject editorCms = OpenCms.initCmsObject(cms);
            editorCms.loginUser("Editor2", "password");
            editorCms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
            CmsResource first = editorCms.createResource("/system/dir0", 0);
            cms.changeLock("/system/dir0");
            cms.chacc("/system/dir0/", I_CmsPrincipal.PRINCIPAL_GROUP, "Users", "+w");
            cms.unlockResource("/system/dir0");
            editorCms.lockResource("/system/dir0");
            CmsResource second = editorCms.createResource("/system/dir0/er4", 1);
            List<CmsResource> resources = new ArrayList<CmsResource>();
            resources.add(second);
            CmsWorkflowResponse response = OpenCms.getWorkflowManager().executeAction(
                editorCms,
                CmsWorkflowManager.ACTION_RELEASE,
                resources);
            CmsUUID projectId = response.getWorkflowId();
            CmsResource er4 = cms.readResource("/system/dir0/er4");
            assertEquals(projectId, er4.getProjectLastModified());
            assertEquals(editor.getId(), er4.getUserLastModified());
            assertFalse(
                "directory should be still in another project",
                cms.readResource("/system/dir0").getProjectLastModified().equals(projectId));

        } finally {
            OpenCmsTestLogAppender.setBreakOnError(false);
        }
    }

    /**
     * Checks whether a project with a given id exists.<p>
     * 
     * @param projectId a project id 
     * 
     * @return true if the project with the given id exists 
     */
    protected boolean existsProject(CmsUUID projectId) {

        try {
            CmsObject cms = getCmsObject();
            cms.readProject(projectId);
            return true;
        } catch (CmsException e) {
            return false;

        }
    }

    protected CmsProject getProjectLastModified(String path) throws Exception {

        CmsResource resource = getCmsObject().readResource(path);
        CmsProject project = getCmsObject().readProject(resource.getProjectLastModified());
        return project;
    }

    /**
     * Publishes a resources with a given path.<p>
     * 
     * @param path the path of the resource to publish 
     * 
     * @throws CmsException 
     */
    protected void publish(String path) throws CmsException {

        CmsObject cms = getCmsObject();
        CmsResource resource = cms.readResource(path);
        CmsPublishList publishList = OpenCms.getPublishManager().getPublishList(cms, resource, false);
        OpenCms.getPublishManager().publishProject(cms, new CmsShellReport(new Locale("en")), publishList);
        OpenCms.getPublishManager().waitWhileRunning();
    }

}

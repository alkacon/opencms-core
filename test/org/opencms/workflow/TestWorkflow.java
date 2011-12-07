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
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

/**
 * A test for the default workflow manager implementation.<p>
 */
public class TestWorkflow extends OpenCmsTestCase {

    public TestWorkflow(String name) {

        super(name);

    }

    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        return generateSetupTestWrapper(TestWorkflow.class, "systemtest", "/");
    }

    public void testFailReleaseOnline() throws CmsException {

        CmsWorkflowManager wfm = OpenCms.getWorkflowManager();
        CmsObject cms = OpenCms.initCmsObject(getCmsObject());
        List<CmsResource> resources = new ArrayList<CmsResource>();
        CmsResource r = cms.createResource("/system/r4", 1);
        resources.add(r);
        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_NAME));
        try {
            CmsWorkflowResponse response = wfm.executeAction(cms, CmsWorkflowManager.ACTION_RELEASE, resources);
        } catch (CmsException e) {
            return;
        }
        assertTrue("Should have failed", false);
    }

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

    public void testWorkflowRelease() throws CmsException {

        CmsWorkflowManager wfm = OpenCms.getWorkflowManager();
        CmsObject cms = getCmsObject();
        List<CmsResource> resources = new ArrayList<CmsResource>();
        CmsResource r = cms.createResource("/system/r3", 1);
        resources.add(r);
        CmsWorkflowResponse response = wfm.executeAction(getCmsObject(), CmsWorkflowManager.ACTION_RELEASE, resources);
        assertTrue(response.isSuccess());
        CmsUUID workflowId = response.getWorkflowId();
        CmsProject project = cms.readProject(workflowId);
        assertEquals(CmsProject.PROJECT_TYPE_WORKFLOW.getMode(), project.getType().getMode());
        System.out.println("PROJECTNAME>>>" + project.getName());
        CmsResource rRead = cms.readResource("/system/r3");
        assertEquals(workflowId, rRead.getProjectLastModified());
        assertFalse(workflowId.equals(cms.getRequestContext().getCurrentProject().getUuid()));
    }

}

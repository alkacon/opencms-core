/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/workflow/Attic/TestWorkflow.java,v $
 * Date   : $Date: 2006/11/29 15:04:13 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workflow;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.lock.CmsLockException;
import org.opencms.lock.CmsLockType;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/** 
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.1.2.2 $
 * 
 * @since 7.0.0
 */
public class TestWorkflow extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestWorkflow(String arg0) {

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
        suite.setName(TestWorkflow.class.getName());

        suite.addTest(new TestWorkflow("testWorkflow"));
        suite.addTest(new TestWorkflow("testWorkflowWithFileOperations"));
        suite.addTest(new TestWorkflow("testWorkflowWithFolderOperations"));

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
     * Tests a complete workflow sequence.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testWorkflow() throws Throwable {

        String resource = "/index.html";
        String newTitle = "This is the changed title";
        String owner, agent, state;

        CmsObject cms = getCmsObject();
        I_CmsWorkflowManager wfm = OpenCms.getWorkflowManager();

        // create a workflow project
        CmsProject wfp = wfm.createTask(cms, "Task description");

        // modify a resource
        cms.lockResource(resource);
        cms.writePropertyObject(resource, new CmsProperty("Title", newTitle, null));
        cms.unlockResource(resource);

        // add one or more resources to it
        wfm.addResource(getCmsObject(), wfp, resource);
        assertProject(cms, resource, wfp);

        // initiate the workflow (0 - 4 eye review)
        I_CmsWorkflowType wfType = (I_CmsWorkflowType)wfm.getWorkflowTypes().get(0);
        wfm.init(getCmsObject(), wfp, wfType, "This is the initial message.");

        // read workflow data
        owner = wfm.getTaskOwner(wfp).getName();
        agent = wfm.getTaskAgent(wfp).getName();
        state = wfm.getTaskState(wfp, Locale.getDefault());
        // log = wfm.getLog(wfp);
        // entries = log.size();
        echo("Owner: " + owner);
        echo("Agent: " + agent);
        echo("State: " + state);

        assertProject(cms, resource, wfp);

        // now select a transition (0 - Publish)
        I_CmsWorkflowTransition transition = (I_CmsWorkflowTransition)wfm.getTransitions(wfp).get(0);
        wfm.signal(cms, wfp, transition, "This is the transition message.");

        // read workflow data
        owner = wfm.getTaskOwner(wfp).getName();
        agent = wfm.getTaskAgent(wfp).getName();
        state = wfm.getTaskState(wfp, Locale.getDefault());
        // log = wfm.getLog(wfp);
        // entries = log.size();
        echo("Owner: " + owner);
        echo("Agent: " + agent);
        echo("State: " + state);

        // check that the modified resource is published and unlocked, but still changed
        cms.getRequestContext().setCurrentProject(cms.readProject("Online"));
        CmsProperty prop1 = cms.readPropertyObject(resource, "Title", false);
        if (prop1.getValue().equals((newTitle))) {
            fail("Property not changed for " + resource);
        }

        // check if the file in the offline project is unchanged
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        assertState(cms, resource, CmsResource.STATE_UNCHANGED);

        // check if the file in the offline project is unlocked
        assertLock(cms, resource, CmsLockType.UNLOCKED);
    }

    /**
     * Tests a complete workflow sequence trying to do some 'dangerous' folder operations.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testWorkflowWithFolderOperations() throws Throwable {

        String oldFolder = "/folder1";
        String folder = "/newfolder1";
        String resource = "/index.html";
        String owner, agent, state;

        CmsObject cms = getCmsObject();
        echo("Tests a complete workflow sequence trying to do some 'dangerous' folder operations");

        // first move the folder
        cms.lockResource(oldFolder);
        cms.moveResource(oldFolder, folder);

        I_CmsWorkflowManager wfm = OpenCms.getWorkflowManager();

        // create a workflow project
        CmsProject wfp = wfm.createTask(cms, "Task description");

        // modify a resource
        cms.lockResource(resource);
        cms.writePropertyObject(resource, new CmsProperty("Title", resource, null));
        cms.unlockProject(cms.getRequestContext().currentProject().getId());

        // add one or more resources to it
        wfm.addResource(getCmsObject(), wfp, resource);
        cms.lockResource(folder);
        // try to undochanges on the folder
        try {
            cms.undoChanges(folder, CmsResource.UNDO_MOVE_CONTENT_RECURSIVE);
            fail("should not be allowed to undo changes of a folder with resources locked in workflow");
        } catch (CmsLockException e) {
            // ok
        }
        // try to delete the folder
        try {
            cms.deleteResource(folder, CmsResource.DELETE_PRESERVE_SIBLINGS);
            fail("should not be allowed to delete a folder with resources locked in workflow");
        } catch (CmsLockException e) {
            // ok
        }
        // try to move the folder
        try {
            cms.moveResource(folder, oldFolder);
            fail("should not be allowed to move a folder with resources locked in workflow");
        } catch (CmsLockException e) {
            // ok
        }
        // try to direct publish the folder
        try {
            cms.unlockProject(cms.getRequestContext().currentProject().getId());
            cms.publishResource(folder);
            OpenCms.getPublishManager().waitWhileRunning();
            fail("should not be allowed to publish a folder with resources locked in workflow");
        } catch (CmsLockException e) {
            // ok
        }

        // initiate the workflow (0 - 4 eye review)
        I_CmsWorkflowType wfType = (I_CmsWorkflowType)wfm.getWorkflowTypes().get(0);
        wfm.init(getCmsObject(), wfp, wfType, "This is the initial message.");

        // read workflow data
        owner = wfm.getTaskOwner(wfp).getName();
        agent = wfm.getTaskAgent(wfp).getName();
        state = wfm.getTaskState(wfp, Locale.getDefault());
        // log = wfm.getLog(wfp);
        // entries = log.size();
        echo("Owner: " + owner);
        echo("Agent: " + agent);
        echo("State: " + state);

        // now select a transition (0 - Publish)
        I_CmsWorkflowTransition transition = (I_CmsWorkflowTransition)wfm.getTransitions(wfp).get(0);
        wfm.signal(cms, wfp, transition, "This is the transition message.");

        // read workflow data
        owner = wfm.getTaskOwner(wfp).getName();
        agent = wfm.getTaskAgent(wfp).getName();
        state = wfm.getTaskState(wfp, Locale.getDefault());
        // log = wfm.getLog(wfp);
        // entries = log.size();
        echo("Owner: " + owner);
        echo("Agent: " + agent);
        echo("State: " + state);

        // check that the modified resource is published and unlocked, but still changed
        cms.getRequestContext().setCurrentProject(cms.readProject("Online"));
        CmsProperty prop1 = cms.readPropertyObject(resource, "Title", false);
        if (!prop1.getValue().equals((resource))) {
            fail("Property not changed for " + resource);
        }

        // check if the file in the offline project is unchanged
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        assertState(cms, resource, CmsResource.STATE_UNCHANGED);

        // check if the file in the offline project is unlocked
        assertLock(cms, resource, CmsLockType.UNLOCKED);
    }

    /**
     * Tests a complete workflow sequence trying to do some 'dangerous' file operations.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testWorkflowWithFileOperations() throws Throwable {

        String resource = "/index.html";
        String attachment = "/folder1/image2.gif";
        String owner, agent, state;

        CmsObject cms = getCmsObject();
        echo("Tests a complete workflow sequence trying to do some 'dangerous' file operations");

        // first touch the attachment
        cms.lockResource(attachment);
        cms.setDateLastModified(attachment, System.currentTimeMillis(), false);
        cms.unlockResource(attachment);

        I_CmsWorkflowManager wfm = OpenCms.getWorkflowManager();

        // create a workflow project
        CmsProject wfp = wfm.createTask(cms, "Task description");

        // modify a resource
        cms.lockResource(resource);
        cms.writePropertyObject(resource, new CmsProperty("Title", resource, null));
        cms.unlockProject(cms.getRequestContext().currentProject().getId());

        // add one or more resources to it
        wfm.addResource(getCmsObject(), wfp, resource);

        cms.lockResource(attachment);
        assertLock(cms, attachment, CmsLockType.WORKFLOW);
        // try to undochanges on the attachment
        try {
            cms.undoChanges(attachment, CmsResource.UNDO_MOVE_CONTENT_RECURSIVE);
            fail("should not be allowed to undo changes of a resource locked in workflow");
        } catch (CmsLockException e) {
            // ok
        }
        // try to delete the attachment
        try {
            cms.deleteResource(attachment, CmsResource.DELETE_PRESERVE_SIBLINGS);
            fail("should not be allowed to delete a resource locked in workflow");
        } catch (CmsLockException e) {
            // ok
        }
        // try to move the attachment
        try {
            cms.moveResource(attachment, "newimage.gif");
            fail("should not be allowed to move a resource locked in workflow");
        } catch (CmsLockException e) {
            // ok
        }
        // try to direct publish the attachment
        try {
            cms.unlockResource(attachment);
            cms.publishResource(attachment);
            OpenCms.getPublishManager().waitWhileRunning();
            if (!cms.getPublishList(cms.readResource(attachment), false).getFileList().isEmpty()) {
                fail("should not be allowed to publish a resource locked in workflow");
            }
        } catch (CmsLockException e) {
            // ok
        }

        // initiate the workflow (0 - 4 eye review)
        I_CmsWorkflowType wfType = (I_CmsWorkflowType)wfm.getWorkflowTypes().get(0);
        wfm.init(getCmsObject(), wfp, wfType, "This is the initial message.");

        // read workflow data
        owner = wfm.getTaskOwner(wfp).getName();
        agent = wfm.getTaskAgent(wfp).getName();
        state = wfm.getTaskState(wfp, Locale.getDefault());
        // log = wfm.getLog(wfp);
        // entries = log.size();
        echo("Owner: " + owner);
        echo("Agent: " + agent);
        echo("State: " + state);

        // now select a transition (0 - Publish)
        I_CmsWorkflowTransition transition = (I_CmsWorkflowTransition)wfm.getTransitions(wfp).get(0);
        wfm.signal(cms, wfp, transition, "This is the transition message.");

        // read workflow data
        owner = wfm.getTaskOwner(wfp).getName();
        agent = wfm.getTaskAgent(wfp).getName();
        state = wfm.getTaskState(wfp, Locale.getDefault());
        // log = wfm.getLog(wfp);
        // entries = log.size();
        echo("Owner: " + owner);
        echo("Agent: " + agent);
        echo("State: " + state);

        // check that the modified resource is published and unlocked, but still changed
        cms.getRequestContext().setCurrentProject(cms.readProject("Online"));
        CmsProperty prop1 = cms.readPropertyObject(resource, "Title", false);
        if (!prop1.getValue().equals((resource))) {
            fail("Property not changed for " + resource);
        }

        // check if the file in the offline project is unchanged
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        assertState(cms, resource, CmsResource.STATE_UNCHANGED);

        // check if the file in the offline project is unlocked
        assertLock(cms, resource, CmsLockType.UNLOCKED);
    }
}

/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/workflow/Attic/TestWorkflowResources.java,v $
 * Date   : $Date: 2006/08/19 13:40:59 $
 * Version: $Revision: 1.1.2.1 $
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
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/** 
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.1.2.1 $
 * 
 * @since 7.0.0
 */
public class TestWorkflowResources extends OpenCmsTestCase {
    
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestWorkflowResources(String arg0) {

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
        suite.setName(TestWorkflowResources.class.getName());

        suite.addTest(new TestWorkflowResources("testAddToWorkflowProject"));
        suite.addTest(new TestWorkflowResources("testIndirectLock"));
        suite.addTest(new TestWorkflowResources("testLockResourceByOwner1"));
        suite.addTest(new TestWorkflowResources("testInitTask"));
        suite.addTest(new TestWorkflowResources("testLockResourceByOwner2"));
        suite.addTest(new TestWorkflowResources("testLockResourceByAgent"));
        suite.addTest(new TestWorkflowResources("testLockResourceByAdmin"));
        suite.addTest(new TestWorkflowResources("testAbortWorkflowProject"));
        suite.addTest(new TestWorkflowResources("testPublishWorkflowProject"));
        suite.addTest(new TestWorkflowResources("testUndoWorkflowProject"));

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
    
    /** The test project. */
    public static CmsProject testProject;
    
    /**
     * Creates a new workflow project and adds a resource.<p>
     * 
     * @param cms the cms object
     * @param wfName the name of the workflow
     * @param resourcePath path of the resource
     * @return the workflow project
     * @throws Throwable
     */
    public static CmsProject createWorkflowProjectWithResource(CmsObject cms, String wfName, String resourcePath) throws Throwable {
        
        WorkflowTestManager wfm = (WorkflowTestManager)OpenCms.getWorkflowManager();
        
        // create a workflow project
        CmsResource res = cms.readResource(resourcePath);
        testProject = wfm.createWorkflowProject(cms.getRequestContext().currentUser(), wfName, "Workflow project description");
        
        // add resource to the workflow project
        wfm.addResource(cms, testProject, res);
        
        return testProject;
    }
    
    
    /**
     * Tests the resource values and the locking when a resource is added to a workflow project.<p>
     * 
     * @throws Throwable
     */
    public void testAddToWorkflowProject() throws Throwable {
     
        CmsObject cms = getCmsObject();
        
        echo("Test creating a workflow project with a single resource");
        
        // create a sibling of the test resource (used in subsequent tests)
        cms.copyResource("/index.html", "/index_sibling.html", CmsResource.COPY_AS_SIBLING);
        cms.unlockResource("/index_sibling.html");
        
        // test1 will be the task owner, but not the agent
        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        
        createWorkflowProjectWithResource(cms, "Workflow Project #1", "/index.html");
        
        // workflow project is controlled by group project managers as default
        assertTrue("Default workflow project manager group should be Projectmanagers", 
            testProject.getManagerGroupId().equals(cms.readGroup("Projectmanagers").getId()));
        
        // workflow project users are all users as default
        assertTrue("Default workflow project users group should be Users", 
            testProject.getGroupId().equals(cms.readGroup("Users").getId()));
        
        // workflow project owner must be the user who initiated the workflow project
        assertTrue("Workflow project owner should be current user", 
            testProject.getOwnerId().equals(cms.getRequestContext().currentUser().getId()));
            
        // resource must be a project resource of the workflow project
        List wfpResources = cms.readProjectResources(testProject);
        assertTrue(CmsProject.isInsideProject(wfpResources, cms.getRequestContext().addSiteRoot("/index.html")));
        
        // lock manager must now return a workflow lock for the resource
        // user id of the lock must be set to the current user
        assertLock(cms, "/index.html", CmsLockType.WORKFLOW, cms.getRequestContext().currentUser().getId());
        
        // the project of the lock must contain the value of the workflow project
        CmsLock lock = cms.getLock("/index.html");
        assertEquals(testProject.getId(), lock.getProjectId());
    }    

    /**
     * Tests if siblings of resources assigned to a workflow have an indirect lock.<p>
     * 
     * @throws Throwable
     */
    public void testIndirectLock() throws Throwable {
        
        CmsObject cms = getCmsObject();
   
        // siblings lock is of type workflow since index is only locked in workflow but not exclusive
        assertLock(cms, "/index_sibling.html", CmsLockType.WORKFLOW, cms.readUser("test1").getId());
        
        // now lock index.html exclusive
        cms.lockResource("/index.html");
        
        // siblings lock is of type shared exclusive since index is additionally locked exclusive
        assertLock(cms, "/index_sibling.html", CmsLockType.SHARED_EXCLUSIVE, cms.getRequestContext().currentUser().getId());
        
        cms.unlockResource("/index.html");
    }

    /**
     * Tests if resources assigned to a workflow can additionally be locked by a single lock owner exclusively.<p>
     * 
     * @throws Throwable
     */
    public void testLockResourceByOwner1() throws Throwable {
    
        CmsObject cms = getCmsObject();
        
        // resource should be locked in workflow
        assertLock(cms, "/index.html", CmsLockType.WORKFLOW, cms.readUser("test1").getId());
 
        // test1 is the owner but not expected to be in the agent group
        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        
        // as long as the workflow is not initialized, the user can lock the resource
        cms.lockResource("/index.html");
        assertLock(cms, "/index.html", CmsLockType.EXCLUSIVE, cms.getRequestContext().currentUser().getId());
        cms.unlockResource("/index.html");
    }
    
    /**
     * Tests the task initialization.<p>
     * 
     * @throws Throwable
     */
    public void testInitTask() throws Throwable {
        
        CmsObject cms = getCmsObject();
        
        WorkflowTestManager wfm = (WorkflowTestManager)OpenCms.getWorkflowManager();
        wfm.init(cms, testProject, (I_CmsWorkflowType)wfm.getWorkflowTypes().get(0), "Description");
    }
    
    /**
     * Tests if resources assigned to a workflow can additionally be locked by a single lock owner exclusively.<p>
     * 
     * @throws Throwable
     */
    public void testLockResourceByOwner2() throws Throwable {
    
        CmsObject cms = getCmsObject();
        
        // resource should be locked in workflow
        assertLock(cms, "/index.html", CmsLockType.WORKFLOW, cms.readUser("test1").getId());
 
        // test1 is the owner but not expected to be in the agent group
        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
       
        
        // a resource in an initialized workflow cannot be locked by the initiator of the workflow if he is not agent, too
        try {
            cms.lockResource("/index.html");
            fail("Workflow initiator is lock owner but should not be able to lock the resource exclusively");
        } catch (CmsException exc) {
            assertLock(cms, "/index.html", CmsLockType.WORKFLOW);
        }
    }
    
    /**
     * Tests if resources assigned to a workflow can additionally be locked by a member of the agent group.<p>
     * 
     * @throws Throwable
     */
    public void testLockResourceByAgent() throws Throwable {
        
        CmsObject cms = getCmsObject();
        
        // resource should be locked in workflow
        assertLock(cms, "/index.html", CmsLockType.WORKFLOW, cms.readUser("test1").getId());
        
        // after the resource is locked explicitly, it should have an exclusive lock
        // test2 is expected to be in the agent group
        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        
        cms.lockResource("/index.html");
        assertLock(cms, "/index.html", CmsLockType.EXCLUSIVE);
        
        // after the resource is unlocked, it should be locked in workflow again
        cms.unlockResource("/index.html");
        assertLock(cms, "/index.html", CmsLockType.WORKFLOW, cms.readUser("test1").getId());
    }
    
    /**
     * Tests if resources assigned to a workflow can additionally be locked by an administrator.<p>
     * 
     * @throws Throwable
     */
    public void testLockResourceByAdmin() throws Throwable {
        
        CmsObject cms = getCmsObject();
        
        // resource should be locked in workflow
        assertLock(cms, "/index.html", CmsLockType.WORKFLOW, cms.readUser("test1").getId());
        
        // admin should be able to lock the resource anyway
        cms.lockResource("/index.html");
        assertLock(cms, "/index.html", CmsLockType.EXCLUSIVE, cms.getRequestContext().currentUser().getId());
        
        // after the resource is unlocked, it should be locked in workflow again
        cms.unlockResource("/index.html");
        assertLock(cms, "/index.html", CmsLockType.WORKFLOW, cms.readUser("test1").getId());
    }    

    /**
     * Tests if a workflow project is aborted appropriately.<p>
     * 
     * @throws Throwable
     */
    public void testAbortWorkflowProject() throws Throwable {

        CmsObject cms = getCmsObject();
        String resource = "/folder1/page1.html";
        String test = "testAbortWorkflowProject";
        
        // create a workflow project with a resource
        CmsProject wfp = createWorkflowProjectWithResource(cms, "Workflow Project #3", resource);
    
        // modify the resource
        cms.lockResource(resource);
        cms.writePropertyObject(resource, new CmsProperty("Title", test, null));
        cms.unlockResource(resource);
    
        // check if the file is locked in the workflow
        assertLock(cms, resource, CmsLockType.WORKFLOW);
        
        // abort the workflow project
        ((WorkflowTestManager)OpenCms.getWorkflowManager()).abortWorkflowProject(wfp);
        
        // check that the modified resource is unpublished and unlocked, but still changed
        CmsProperty prop1 = cms.readPropertyObject(resource, "Title", false);

        if (!prop1.getValue().equals((test))) {
            fail("Property not changed for " + resource);
        }
        
        // check if the file in the offline project is changed
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        assertState(cms, resource, CmsResource.STATE_CHANGED);
        
        // check if the file in the offline project is unlocked
        assertLock(cms, resource, CmsLockType.UNLOCKED);
    }
    
    /**
     * Tests if resources in a workflow project are published appropriately.<p>
     * 
     * @throws Throwable
     */
    public void testPublishWorkflowProject() throws Throwable {
        
        CmsObject cms = getCmsObject();
        String resource = "/folder1/page1.html";
        String test = "testPublishWorkflowProject";

        // modify the resource
        cms.lockResource(resource);
        cms.writePropertyObject(resource, new CmsProperty("Title", test, null));
        cms.unlockResource(resource);
        
        // create a workflow project with the resource
        CmsProject wfp = createWorkflowProjectWithResource(cms, "Workflow Project #4", resource);
    
        // check if the file is locked in the workflow
        assertLock(cms, resource, CmsLockType.WORKFLOW);
        
        // publish the workflow project
        ((WorkflowTestManager)OpenCms.getWorkflowManager()).publishWorkflowProject(wfp);
        
        // check that the modified resource is published and unlocked, but still changed
        CmsProperty prop1 = cms.readPropertyObject(resource, "Title", false);
        if (!prop1.getValue().equals((test))) {
            fail("Property not changed for " + resource);
        }
        
        // check if the file in the offline project is unchanged
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        assertState(cms, resource, CmsResource.STATE_UNCHANGED);
        
        // check if the file in the offline project is unlocked
        assertLock(cms, resource, CmsLockType.UNLOCKED);   
    }    

    /**
     * Tests if undoing a workflow project works appropriately.<p>
     * 
     * @throws Throwable
     */
    public void testUndoWorkflowProject() throws Throwable {
   
        CmsObject cms = getCmsObject();
        String resource = "/folder1/page1.html";
        String test = "testUndoWorkflowProject";

        // modify the resource
        cms.lockResource(resource);
        cms.writePropertyObject(resource, new CmsProperty("Title", test, null));
        cms.unlockResource(resource);
        
        assertState(cms, resource, CmsResource.STATE_CHANGED);
        
        // create a workflow project with the resource
        CmsProject wfp = createWorkflowProjectWithResource(cms, "Workflow Project #5", resource);
    
        // check if the file is locked in the workflow
        assertLock(cms, resource, CmsLockType.WORKFLOW);
        
        // undo the workflow project
        ((WorkflowTestManager)OpenCms.getWorkflowManager()).undoWorkflowProject(wfp);
        
        // check that the modified resource is unpublished and unlocked and not changed
        CmsProperty prop1 = cms.readPropertyObject(resource, "Title", false);
        if (prop1.getValue().equals((test))) {
            fail("Property is still set for " + resource);
        }
        
        // check if the file in the offline project is unchanged
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        assertState(cms, resource, CmsResource.STATE_UNCHANGED);
        
        // check if the file in the offline project is unlocked
        assertLock(cms, resource, CmsLockType.UNLOCKED);          
    }
}
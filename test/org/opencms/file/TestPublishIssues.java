/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestPublishIssues.java,v $
 * Date   : $Date: 2005/03/29 18:01:40 $
 * Version: $Revision: 1.13 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.file;

import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for special publish issues.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.13 $
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
                
        suite.addTest(new TestPublishIssues("testPublishScenarioA"));
        suite.addTest(new TestPublishIssues("testPublishScenarioB"));
        suite.addTest(new TestPublishIssues("testPublishScenarioC"));        
        suite.addTest(new TestPublishIssues("testMultipleProjectCreation"));
        suite.addTest(new TestPublishIssues("testDirectPublishWithSiblings"));
        suite.addTest(new TestPublishIssues("testPublishScenarioD"));        
        suite.addTest(new TestPublishIssues("testPublishScenarioE"));        
        
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
     * Tests publish scenario "A".<p>
     * 
     * This scenario is described as follows:
     * We have users "test1" and "test2".
     * We have two projects, "project1" and "project2".
     * Project "project1" consists of the folder "/".
     * Project "project2" consists of the folder "/folder1/subfolder11/".
     * User "test2" edits the file "/folder1/subfolder11/index.html".
     * After this, user "test1" locks the folder "/folder1" in "project1".
     * User "test2" now publishes "project2".<p>
     * 
     * TODO: What must happen with the file "/folder1/subfolder11/index.html"?.<p>
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
        cms.touch(resource1, System.currentTimeMillis(), CmsResource.DATE_RELEASED_DEFAULT, CmsResource.DATE_EXPIRED_DEFAULT, false);
        
        // assert some basic status info
        assertDateLastModifiedAfter(cms, resource1, timestamp);
        assertProject(cms, resource1, project2);
        assertLock(cms, resource1, CmsLock.C_TYPE_EXCLUSIVE);
        assertState(cms, resource1, I_CmsConstants.C_STATE_CHANGED);
        
        // now login as user "test1" (default will be in the "Offline" project)
        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(project1);     
        
        // lock the folder
        cms.lockResource(resource2);
        
        // assert some basic status info
        assertProject(cms, resource1, project2);
        assertLock(cms, resource1, CmsLock.C_TYPE_INHERITED);        
        
        // back to the user "test2"
        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(project2);            

        // TODO: The wanted behaviour in this case must be defined!
        
        // project should have one locked resource 
        int resourceProjectCount = cms.countLockedResources(project2.getId());
        // THIS IS WHERE THE TEST CURRENTLY FAILS!        
        assertEquals(1, resourceProjectCount);
        
        // unlock the project
        cms.unlockProject(project2.getId());
        cms.publishProject();
        
        // ensure the file was published - state must be "unchanged" 
        assertState(cms, resource1, I_CmsConstants.C_STATE_UNCHANGED);
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
     * Now both users try to edit the sibling of "test.txt" in their folder.
     * 
     * TODO: How are concurrent file modifications avoided on the sibling?.<p>
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
        cms.unlockProject(cms.getRequestContext().currentProject().getId());
        cms.publishProject();
        
        // check if the setup was created as planned
        cmsFile = cms.readFile(resourceA);
        assertEquals(2, cmsFile.getSiblingCount());
        assertState(cms, resourceA, I_CmsConstants.C_STATE_UNCHANGED);
        cmsFile = cms.readFile(resourceB);
        assertEquals(2, cmsFile.getSiblingCount());        
        assertState(cms, resourceB, I_CmsConstants.C_STATE_UNCHANGED);
        
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
     * Tests multiple creation of a project with the same name.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testMultipleProjectCreation() throws Throwable {
        
        CmsObject cms = getCmsObject();
        echo("Testing multiple creation of a project with the same name");
        
        String projectName = "projectX";
        boolean gotException;
        
        // try to read a non-existant project
        gotException = false;
        try {
            cms.readProject(projectName);
        } catch (CmsException e) {
            gotException = true;
        }        
        if (! gotException) {
            fail("Required exception was not thrown!");
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
        
        // now create the project again - this must throw an exception
        gotException = false;  
        CmsProject newProject = cms.createProject(
            projectName, 
            "Test project 2nd time", 
            OpenCms.getDefaultUsers().getGroupUsers(), 
            OpenCms.getDefaultUsers().getGroupUsers());
        
        // TODO: above create statement fails "sometimes" - check table contraints (?)
        
        // check if the projects have different ids
        int id1 = project.getId();
        int id2 = newProject.getId();
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
        cms.touch("/folder1/", touchTime, I_CmsConstants.C_DATE_UNCHANGED, I_CmsConstants.C_DATE_UNCHANGED, false);
        cms.touch("/folder1/index.html", touchTime, I_CmsConstants.C_DATE_UNCHANGED, I_CmsConstants.C_DATE_UNCHANGED, false);
        cms.touch("/folder1/subfolder11/", touchTime, I_CmsConstants.C_DATE_UNCHANGED, I_CmsConstants.C_DATE_UNCHANGED, false);
        cms.touch("/folder1/subfolder11/index.html", touchTime, I_CmsConstants.C_DATE_UNCHANGED, I_CmsConstants.C_DATE_UNCHANGED, false);
        
        cms.unlockResource("/folder1/");
        cms.publishResource("/folder1/");
        
        assertState(cms, "/folder1/", I_CmsConstants.C_STATE_UNCHANGED);        
        assertState(cms, "/folder1/index.html", I_CmsConstants.C_STATE_UNCHANGED);
        assertState(cms, "/folder1/subfolder11/", I_CmsConstants.C_STATE_UNCHANGED);
        assertState(cms, "/folder1/subfolder11/index.html", I_CmsConstants.C_STATE_UNCHANGED);
        
        cms.createResource("/folder_a/", CmsResourceTypeFolder.getStaticTypeId());
        cms.createResource("/folder_a/file_a.txt", CmsResourceTypePlain.getStaticTypeId());
        cms.createResource("/folder_a/folder_b/", CmsResourceTypeFolder.getStaticTypeId());
        cms.createResource("/folder_a/folder_b/file_b.txt", CmsResourceTypePlain.getStaticTypeId());
        
        cms.unlockResource("/folder_a/");
        cms.publishResource("/folder_a/");
        
        assertState(cms, "/folder_a/", I_CmsConstants.C_STATE_UNCHANGED);        
        assertState(cms, "/folder_a/file_a.txt", I_CmsConstants.C_STATE_UNCHANGED);
        assertState(cms, "/folder_a/folder_b/", I_CmsConstants.C_STATE_UNCHANGED);
        assertState(cms, "/folder_a/folder_b/file_b.txt", I_CmsConstants.C_STATE_UNCHANGED);
        
    }
    
    /**
     * Tests publish scenario "D".<p>
     * 
     * This scenario is described as follows:
     * Direct publishing of folders containing subfolders skips all (sibling)
     * resources in subfolders. 
     * e.g. direct publish of /folder2/folder1/ 
     * publishes /folder2/folder1/ and /folder2/folder1/index.html/, 
     * but not /folder2/folder1/subfolder11/index.html.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testPublishScenarioD() throws Throwable {
        
        CmsObject cms = getCmsObject();
        echo("Testing publish scenario D");
        
        // change to the offline project 
        CmsProject project = cms.readProject("Offline");
        cms.getRequestContext().setCurrentProject(project);

        
        cms.lockResource("/folder1/");
        // copy the whole folder creating siblings of all resources
        cms.copyResource("/folder1/", "/folder2/folder1", I_CmsConstants.C_COPY_AS_SIBLING);
        cms.unlockResource("/folder1/");
        
        // direct publish the new folder
        cms.unlockResource("/folder2/folder1/");
        cms.publishResource("/folder2/folder1/");
        
        assertState(cms, "/folder2/folder1/", I_CmsConstants.C_STATE_UNCHANGED);        
        assertState(cms, "/folder2/folder1/index.html", I_CmsConstants.C_STATE_UNCHANGED);
        assertState(cms, "/folder2/folder1/subfolder11/", I_CmsConstants.C_STATE_UNCHANGED);
        assertState(cms, "/folder2/folder1/subfolder11/index.html", I_CmsConstants.C_STATE_UNCHANGED);
        assertState(cms, "/folder2/folder1/subfolder11/subsubfolder111/", I_CmsConstants.C_STATE_UNCHANGED);
        assertState(cms, "/folder2/folder1/subfolder11/subsubfolder111/jsp.jsp", I_CmsConstants.C_STATE_UNCHANGED);
        assertState(cms, "/folder2/folder1/subfolder12/", I_CmsConstants.C_STATE_UNCHANGED);
        assertState(cms, "/folder2/folder1/subfolder12/index.html", I_CmsConstants.C_STATE_UNCHANGED);
        assertState(cms, "/folder2/folder1/subfolder12/subsubfolder121/", I_CmsConstants.C_STATE_UNCHANGED);
        assertState(cms, "/folder2/folder1/subfolder12/subsubfolder121/index.html", I_CmsConstants.C_STATE_UNCHANGED);
        
    }
    
    /**
     * Tests publish scenario "E".<p>
     * 
     * This scenario is described as follows:
     * Deletion of folders containing shared locked siblings after copying 
     * a folder creating siblings into a new folder and publication. <p>
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
        cms.copyResource("/folder1/subfolder12/subsubfolder121", "/test/subtest", I_CmsConstants.C_COPY_AS_SIBLING);
        cms.unlockResource("/test");
        
        // publish
        cms.publishResource("/test");

        // lock sibling 
        cms.lockResource("/folder1/subfolder12/subsubfolder121/image1.gif");
        
        // login as user test2
        cms.addUserToGroup("test2", "Projectmanagers");
        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(project);
        
        // check lock
        assertEquals(cms.getLock("/test/subtest/image1.gif").getType(), CmsLock.C_TYPE_SHARED_EXCLUSIVE);
        
        // delete the folder
        cms.lockResource("/test/subtest");
        cms.deleteResource("/test/subtest", I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
        cms.unlockResource("/test/subtest");

        // publish
        cms.publishResource("/test");
        
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
        cms.touch("/folder1/", System.currentTimeMillis(), I_CmsConstants.C_DATE_UNCHANGED, I_CmsConstants.C_DATE_UNCHANGED, true);
        cms.unlockResource("/folder1/");
        
        // publish the project (this did cause an exception because of primary key violation!)
        CmsUUID publishId = cms.publishResource("/folder1/", true, new CmsShellReport());
        
        // read the published resources from the history
        List publishedResources = cms.readPublishedResources(publishId);
        
        // make sure the publish history contains the required amount of resources
        assertEquals(35, publishedResources.size());
    }    
}

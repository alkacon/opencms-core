/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestProjects.java,v $
 * Date   : $Date: 2005/06/27 23:22:09 $
 * Version: $Revision: 1.14 $
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

import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.OpenCms;
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
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.14 $
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
            OpenCms.getDefaultUsers().getGroupProjectmanagers(), 
            CmsProject.PROJECT_TYPE_NORMAL
        );
        
        // some basic project tests
        assertEquals(projectName, project.getName());
        assertFalse(project.isOnlineProject());
        
        // ensure the project is now accessible
        List projects = cms.getAllAccessibleProjects();
        int i;
        for (i = 0; i < projects.size(); i++) {
            if (((CmsProject)projects.get(i)).getId() == project.getId()) {
                break;
            }
        }
        if (i >= projects.size()) {
            fail ("Project " + project.getName() + "not accessible");
        }
        
        // ensure the project is manageable
        projects = cms.getAllManageableProjects();
        for (i = 0; i < projects.size(); i++) {
            if (((CmsProject)projects.get(i)).getId() == project.getId()) {
                break;
            }
        }
        if (i >= projects.size()) {
            fail ("Project " + project.getName() + "not manageable");
        }        
        
        echo("Testing deleting a project");
        
        // try to delete the project
        cms.deleteProject(project.getId());
        
        // ensure the project is not accessible anymore
        projects = cms.getAllAccessibleProjects();
        for (i = 0; i < projects.size(); i++) {
            if (((CmsProject)projects.get(i)).getId() == project.getId()) {
                fail ("Project " + project.getName() + "not deleted");
            }
        }
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
            OpenCms.getDefaultUsers().getGroupProjectmanagers(), 
            CmsProject.PROJECT_TYPE_NORMAL
        );
        
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
        cms.touch("/folder1/", System.currentTimeMillis(), CmsResource.TOUCH_DATE_UNCHANGED, CmsResource.TOUCH_DATE_UNCHANGED, true);
        cms.deleteResource("/folder2/", CmsResource.DELETE_REMOVE_SIBLINGS);        
        cms.createResource("/folder3/", CmsResourceTypeFolder.getStaticTypeId(), null, Collections.EMPTY_LIST);
        cms.createResource("/folder3/test.txt", CmsResourceTypePlain.getStaticTypeId(), "".getBytes(), Collections.EMPTY_LIST);
        cms.unlockResource(resource);
                
        // switch to the offline project
        CmsProject offlineProject = cms.readProject("Offline");
        cms.getRequestContext().setCurrentProject(offlineProject);
        
        // now delete the project - all changes in the project must be undone
        cms.deleteProject(project.getId());

        // ensure that the original resources are unchanged
        assertFilter(cms, resource, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES);      
        
        // all resources within the folder must  be unchanged now
        Iterator j = cms.readResources(resource, CmsResourceFilter.ALL, true).iterator();
        while (j.hasNext()) {
            CmsResource res = (CmsResource)j.next();
            String resName = cms.getSitePath(res);
                        
            // now evaluate the result
            assertFilter(cms, resName, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES);
        }            
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
        
        cms.getRequestContext().saveSiteRoot();
        cms.getRequestContext().setSiteRoot("/");
        try {
            CmsProject project = cms.createProject(
                projectName, 
                "Unit test project 1", 
                OpenCms.getDefaultUsers().getGroupUsers(), 
                OpenCms.getDefaultUsers().getGroupProjectmanagers(), 
                CmsProject.PROJECT_TYPE_NORMAL
            );
            cms.getRequestContext().setCurrentProject(project);
            cms.copyResourceToProject("/sites/default/index.html");
            cms.copyResourceToProject("/sites/default/folder1/");
        } finally {
            cms.getRequestContext().restoreSiteRoot();
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
     * Test the "readProjectResources" method.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testReadProjectResources() throws Exception {
        
        CmsObject cms = getCmsObject(); 
        
        echo("Testing to read all project resources");
                
        String projectName = "UnitTest4";
        
        cms.getRequestContext().saveSiteRoot();
        cms.getRequestContext().setSiteRoot("/");
        try {
            CmsProject project = cms.createProject(
                projectName, 
                "Unit test project 4", 
                OpenCms.getDefaultUsers().getGroupUsers(), 
                OpenCms.getDefaultUsers().getGroupProjectmanagers(), 
                CmsProject.PROJECT_TYPE_NORMAL
            );
            cms.getRequestContext().setCurrentProject(project);
            cms.copyResourceToProject("/sites/default/index.html");
            cms.copyResourceToProject("/sites/default/folder1/");
        } finally {
            cms.getRequestContext().restoreSiteRoot();
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

/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestResourceOperations.java,v $
 * Date   : $Date: 2004/11/24 15:57:25 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.main.CmsException;
import org.opencms.test.OpenCmsTestPropertiesSingleton;
import org.opencms.test.OpenCmsTestCase;

import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for basic resource operations without test import.<p>
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * 
 * @version $Revision: 1.6 $
 */
public class TestResourceOperations extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestResourceOperations(String arg0) {
        super(arg0);
    }
    
    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {
        OpenCmsTestPropertiesSingleton.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        
        TestSuite suite = new TestSuite();
        suite.setName(TestResourceOperations.class.getName());

        suite.addTest(new TestResourceOperations("testGetFolderPath"));
        suite.addTest(new TestResourceOperations("testGetName"));
        suite.addTest(new TestResourceOperations("testGetParentFolder"));
        suite.addTest(new TestResourceOperations("testGetPathLevel"));
        suite.addTest(new TestResourceOperations("testGetPathPart"));
        suite.addTest(new TestResourceOperations("testIsFolder"));
        suite.addTest(new TestResourceOperations("testGetFolderPath"));
        suite.addTest(new TestResourceOperations("testResourceNames"));
        suite.addTest(new TestResourceOperations("testCreateResources"));
        suite.addTest(new TestResourceOperations("testCreateReadFile"));
        suite.addTest(new TestResourceOperations("testPublishFile"));
        suite.addTest(new TestResourceOperations("testCreateSibling"));
        suite.addTest(new TestResourceOperations("testCreateAccessFolders"));
        
        TestSetup wrapper = new TestSetup(suite) {
            
            protected void setUp() {
                setupOpenCms(null, null, false);
            }
            
            protected void tearDown() {
                removeOpenCms();
            }
        };
        
        return wrapper;
    }     
    
    /**
     * Tests the static "getFolderPath" method.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testGetFolderPath() throws Throwable {
        
        echo("Testing testGetFolderPath");
        
        assertEquals(CmsResource.getFolderPath("/system/def/file.html"), "/system/def/");
        
        assertEquals(CmsResource.getFolderPath("/system/def/"), "/system/def/");
    }
    
    /**
     * Tests the static "getName" method.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testGetName() throws Throwable {
        
        echo("Testing testGetName");
        
        assertEquals(CmsResource.getName("/system/workplace/"), "workplace/");
    }
    
    /**
     * Tests the static "getParentFolder" method.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testGetParentFolder() throws Throwable {
        
        echo("Testing testGetParentFolder");
        
        assertEquals(CmsResource.getParentFolder("/system/workplace/"), "/system/");
    }
    
    /**
     * Tests the static "getPathLevel" method.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testGetPathLevel() throws Throwable {
        
        echo("Testing testGetPathLevel");
        
        assertEquals(CmsResource.getPathLevel("/"), 0);
        
        assertEquals(CmsResource.getPathLevel("/foo/"), 1);
        
        assertEquals(CmsResource.getPathLevel("/foo/bar/"), 2);
    }
    
    /**
     * Tests the static "getPathPart" method.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testGetPathPart() throws Throwable {
        
        echo("Testing testGetPathPart");
        
        assertEquals(CmsResource.getPathPart("/foo/bar/", 0), "/");
        assertEquals(CmsResource.getPathPart("/foo/bar/", 1), "/foo/");
        assertEquals(CmsResource.getPathPart("/foo/bar/", 2), "/foo/bar/");
        // TODO: CW - unexpected behaviour ???
        assertEquals(CmsResource.getPathPart("/foo/bar/", -1), "/foo/bar/");
        assertEquals(CmsResource.getPathPart("/foo/bar/", -2), "/foo/");
    }
    
    /**
     * Tests the static "isFolder" method.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testIsFolder() throws Throwable {
        
        echo("Testing testIsFolder");
    }

    /**
     * Tests the check for valid resource names.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testResourceNames() throws Throwable {
        
        CmsObject cms = getCmsObject();
        echo("Testing invalid resource names");
        
        CmsException exc;
        
        // resource name must not contain blanks
        exc = null;
        try {
            cms.createResource("/Resource Name", CmsResourceTypePlain.C_RESOURCE_TYPE_ID, null, null);
        } catch (CmsException e) {
            exc = e;
        }

        this.assertEquals(exc, new CmsException(CmsException.C_BAD_NAME));
        
        // resource name must not contain leading blanks
        exc = null;
        try {
            cms.createResource("/ ResourceName", CmsResourceTypePlain.C_RESOURCE_TYPE_ID, null, null);
        } catch (CmsException e) {
            exc = e;
        }

        this.assertEquals(exc, new CmsException(CmsException.C_BAD_NAME));
        
        // resource name must not contain trailing blanks
        exc = null;
        try {
            cms.createResource("/ResourceName ", CmsResourceTypePlain.C_RESOURCE_TYPE_ID, null, null);
        } catch (CmsException e) {
            exc = e;
        }

        this.assertEquals(exc, new CmsException(CmsException.C_BAD_NAME));
        
        // resource name must not contain other characters 
        exc = null;
        try {
            cms.createResource("/Resource#Name", CmsResourceTypePlain.C_RESOURCE_TYPE_ID, null, null);
        } catch (CmsException e) {
            exc = e;
        }

        this.assertEquals(exc, new CmsException(CmsException.C_BAD_NAME)); 
    }
    
    /**
     * Tests the "createResource" operation.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCreateResources() throws Throwable {

        CmsObject cms = getCmsObject();     
        echo("Testing resource creation");
        
        // create a folder in the root directory
        cms.createResource("/folder1", CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);
        
        // create an empty file in the root directory
        cms.createResource("/resource2", CmsResourceTypePlain.C_RESOURCE_TYPE_ID);
        
        // create an empty file in the created folder 
        cms.createResource("/folder1/resource3", CmsResourceTypePlain.C_RESOURCE_TYPE_ID);        
        
        // ensure first created resource is a folder
        assertIsFolder(cms, "/folder1/");
        
        // ensure second created resource is a plain text file
        assertResourceType(cms, "/resource2", CmsResourceTypePlain.C_RESOURCE_TYPE_ID);
        
        // ensure third created resource is a plain text file
        assertResourceType(cms, "/folder1/resource3", CmsResourceTypePlain.C_RESOURCE_TYPE_ID); 

        
    }  
    
    /**
     * Test folder creation and reading with and without trailing "/".<p>
     * 
     * @throws Throwable if something goes wrong
     */    
    public void testCreateAccessFolders() throws Throwable {
        
        CmsObject cms = getCmsObject();     
        echo("Testing folder creation and access");
        
        CmsException exc;
        
        // create a folder without trailing / in the resource name
        cms.createResource("/cafolder1", CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);        

        // create a folder with trailing / in the resource name
        cms.createResource("/cafolder2/", CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);
                
        // access a folder without trailing / in the resource name 
        // and ensure that its root path is a valid folder path (i.e. with trailing /) 
        assertTrue(CmsResource.isFolder(cms.readResource("/cafolder2").getRootPath()));

        // access a folder with trailing / in the resource name 
        // and ensure that its root path is a valid folder path (i.e. with trailing /) 
        assertTrue(CmsResource.isFolder(cms.readResource("/cafolder1/").getRootPath()));

        // check the folder access using another query 
        // and ensure that the root paths are valid folder paths
        List l;
        l = cms.getSubFolders("/");
        for (int i=0; i<l.size(); i++) {
            CmsResource r = (CmsResource)l.get(i);
            if (!(CmsResource.isFolder(r.getRootPath()))) {
                fail("Invalid folder name returned via getRootPath (" + r.getRootPath() + ")");
            }
        }
        
        // try to create another resource with the same name - must fail
        exc = null;
        try { 
            cms.createResource("/cafolder1", CmsResourceTypePlain.C_RESOURCE_TYPE_ID);        
        } catch (CmsException e) {
            exc = e;
        }        
        assertEquals(exc, new CmsException());
    }

    /**
     * Tests the create and read file methods.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCreateReadFile() throws Throwable {
        
        CmsObject cms = getCmsObject();
        echo("Testing file creation"); 
        
        String content = "this is a test content";
        
        // create a file in the root directory
        cms.createResource("/file1", CmsResourceTypePlain.C_RESOURCE_TYPE_ID, content.getBytes(), null);
        
        // read and check the content
        this.assertContent(cms, "/file1" , content.getBytes());        
    }

    /**
     * Tests the publish resource method for file.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testPublishFile() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing file publishing");

        String content = "this is a test content";
                
        // set the site root
        cms.getRequestContext().setSiteRoot("/");

        // create a file in the root directory
        cms.createResource("/file2", CmsResourceTypePlain.C_RESOURCE_TYPE_ID, content.getBytes(), null);
        
        // the reosurce must unlocked, otherwise it will not be published
        cms.unlockResource("/file2");
        
        // now publish the file
        cms.publishResource("/file2");
        
        // change the project to online
        cms.getRequestContext().setCurrentProject(cms.readProject("Online"));
        
        // read and check the content
        this.assertContent(cms, "/file2" , content.getBytes()); 
        
        // switch back to offline project
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline")); 
    }
    
    /**
     * Tests the "createSibling" operation.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCreateSibling() throws Throwable {
        
        CmsObject cms = getCmsObject();     
        echo("Testing sibling creation");
        
        // create an empty file in the root directory
        cms.createResource("/resource4", CmsResourceTypePlain.C_RESOURCE_TYPE_ID);
        
        // ensure that sibling count is zero
        assertSiblingCount(cms, "/resource4", 1);
        
        // create a sibling of res3 in root folder
        cms.createSibling("/resource4", "/sibling1", null);

        // ensure first created resource is a plain text file
        assertResourceType(cms, "/resource4", CmsResourceTypePlain.C_RESOURCE_TYPE_ID);
        
        // ensure sibling is also a plain text file
        assertResourceType(cms, "/sibling1", CmsResourceTypePlain.C_RESOURCE_TYPE_ID);
        
        // check the sibling count
        assertSiblingCount(cms, "/resource4", 2);
        assertSiblingCount(cms, "/sibling1", 2);
    }
 
}
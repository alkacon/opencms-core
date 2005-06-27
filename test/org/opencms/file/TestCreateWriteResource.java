/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestCreateWriteResource.java,v $
 * Date   : $Date: 2005/06/27 23:22:09 $
 * Version: $Revision: 1.18 $
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
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestResourceConfigurableFilter;
import org.opencms.test.OpenCmsTestResourceFilter;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for the create and import methods.<p>
 * 
 * @author Alexander Kandzior 
 * @version $Revision: 1.18 $
 */
public class TestCreateWriteResource extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestCreateWriteResource(String arg0) {
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
        suite.setName(TestCreateWriteResource.class.getName());
        
        suite.addTest(new TestCreateWriteResource("testImportResource"));
        suite.addTest(new TestCreateWriteResource("testImportResourceAgain"));
        suite.addTest(new TestCreateWriteResource("testImportSibling"));        
        suite.addTest(new TestCreateWriteResource("testImportFolder"));
        suite.addTest(new TestCreateWriteResource("testImportFolderAgain"));
        suite.addTest(new TestCreateWriteResource("testCreateResource"));
        suite.addTest(new TestCreateWriteResource("testCreateResourceAgain"));
        suite.addTest(new TestCreateWriteResource("testCreateFolder"));
        suite.addTest(new TestCreateWriteResource("testCreateFolderAgain"));        
        
        TestSetup wrapper = new TestSetup(suite) {
            
            protected void setUp() {
                setupOpenCms("simpletest", "/sites/default/");
            }
            
            protected void tearDown() {
                // removeOpenCms();
            }
        };
        
        return wrapper;
   }
   

   
   /**
    * Test the create resource method for a folder.<p>
    * 
    * @throws Throwable if something goes wrong
    */
   public void testCreateFolder() throws Throwable {

       CmsObject cms = getCmsObject();     
       echo("Testing creating a folder");
       
       String resourcename = "/folder1/test2/";   
       long timestamp = System.currentTimeMillis()-1; 
       
       cms.createResource(resourcename, CmsResourceTypeFolder.getStaticTypeId(), null, null);
       
       // check the created folder
       CmsFolder folder = cms.readFolder(resourcename);
       
       assertEquals(folder.getState(), CmsResource.STATE_NEW);
       assertTrue(folder.getDateLastModified() > timestamp);
       assertTrue(folder.getDateCreated() > timestamp);
       
       // ensure created resource is a folder
       assertIsFolder(cms, resourcename);  
       // project must be current project
       assertProject(cms, resourcename, cms.getRequestContext().currentProject());
       // state must be "new"
       assertState(cms, resourcename, CmsResource.STATE_NEW);
       // date last modified 
       assertDateLastModifiedAfter(cms, resourcename, timestamp);
       // date created
       assertDateCreatedAfter(cms, resourcename, timestamp);
       // the user last modified must be the current user
       assertUserLastModified(cms, resourcename, cms.getRequestContext().currentUser());       
       
       // publish the project
       cms.unlockProject(cms.getRequestContext().currentProject().getId());
       cms.publishProject();    
       
       assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);       
   }
   
   /**
    * Test the create a folder again.<p>
    * 
    * @throws Throwable if something goes wrong
    */
   public void testCreateFolderAgain() throws Throwable {

       CmsObject cms = getCmsObject();     
       echo("Testing to create an existing folder again");

       String resourcename = "/folder1/test2/";
       storeResources(cms, resourcename);
       long timestamp = System.currentTimeMillis();
       
       assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);       
       cms.lockResource(resourcename);       
       
       try {
           // resource exists and is not deleted, creation must thrw exception
           cms.createResource(resourcename, CmsResourceTypeFolder.getStaticTypeId(), null, null);
       } catch (CmsVfsException e) {
           if (!(e instanceof CmsVfsResourceAlreadyExistsException)) {
               fail("Existing resource '" + resourcename + "' was not detected!");
           }
       }
       
       // read resource for comparing id's later
       CmsResource original = cms.readResource(resourcename);
       
       // delete resource and try again
       cms.deleteResource(resourcename, CmsResource.DELETE_PRESERVE_SIBLINGS);
       cms.createResource(resourcename, CmsResourceTypeFolder.getStaticTypeId(), null, null);
       
       // ensure created resource is a folder
       assertIsFolder(cms, resourcename);     
       // project must be current project
       assertProject(cms, resourcename, cms.getRequestContext().currentProject());
       // state must be "changed"
       assertState(cms, resourcename, CmsResource.STATE_CHANGED);
       // date last modified 
       assertDateLastModifiedAfter(cms, resourcename, timestamp);
       // the user last modified must be the current user
       assertUserLastModified(cms, resourcename, cms.getRequestContext().currentUser());
       // date created
       assertDateCreated(cms, resourcename, original.getDateCreated());
       // the user created must be the current user
       assertUserCreated(cms, resourcename, cms.readUser(original.getUserCreated())); 
       
       // compare id's
       CmsResource created = cms.readResource(resourcename);
       if (! created.getResourceId().equals(original.getResourceId())) {
           fail("A created folder that replaced a deleted folder must have the same resource id!");
       }      
       
       // publish the project
       cms.unlockProject(cms.getRequestContext().currentProject().getId());
       cms.publishProject();    
       
       assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);       
   }   
    
   /**
    * Test the create resource method.<p>
    * 
    * @throws Throwable if something goes wrong
    */
   public void testCreateResource() throws Throwable {

       CmsObject cms = getCmsObject();     
       echo("Testing create resource");
       
       String resourcename = "/folder1/test2.html";
       long timestamp = System.currentTimeMillis()-1;        
              
       String contentStr = "Hello this is my other content";
       byte[] content = contentStr.getBytes();      
       
       cms.createResource(resourcename, CmsResourceTypePlain.getStaticTypeId(), content, null);
       
       // ensure created resource type
       assertResourceType(cms, resourcename, CmsResourceTypePlain.getStaticTypeId());  
       // project must be current project
       assertProject(cms, resourcename, cms.getRequestContext().currentProject());
       // state must be "new"
       assertState(cms, resourcename, CmsResource.STATE_NEW);
       // date last modified 
       assertDateLastModifiedAfter(cms, resourcename, timestamp);
       // date created
       assertDateCreatedAfter(cms, resourcename, timestamp);
       // the user last modified must be the current user
       assertUserLastModified(cms, resourcename, cms.getRequestContext().currentUser()); 
       // check the content
       assertContent(cms, resourcename, content);
              
       // publish the project
       cms.unlockProject(cms.getRequestContext().currentProject().getId());
       cms.publishProject();    
       
       assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);       
   }
   
   /**
    * Test the create resource method for an already existing resource.<p>
    * 
    * @throws Throwable if something goes wrong
    */
   public void testCreateResourceAgain() throws Throwable {

       CmsObject cms = getCmsObject();     
       echo("Testing to create an existing resource again");

       String resourcename = "/folder1/test2.html";
       storeResources(cms, resourcename);
       long timestamp = System.currentTimeMillis();
       
       String contentStr = "Hello this is my NEW AND ALSO CHANGED other content";
       byte[] content = contentStr.getBytes();      

       assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);
       cms.lockResource(resourcename);
       
       try {
           // resource exists and is not deleted, creation must throw exception
           cms.createResource(resourcename, CmsResourceTypePlain.getStaticTypeId(), content, null);
       } catch (Throwable e) {
           if (!(e instanceof CmsVfsResourceAlreadyExistsException)) {
               fail("Existing resource '" + resourcename + "' was not detected!");
           }
       }    

       // read resource for comparing id's later
       CmsResource original = cms.readResource(resourcename);
       
       // delete resource and try again
       cms.deleteResource(resourcename, CmsResource.DELETE_PRESERVE_SIBLINGS);
       cms.createResource(resourcename, CmsResourceTypePlain.getStaticTypeId(), content, null);
              
       // project must be current project
       assertProject(cms, resourcename, cms.getRequestContext().currentProject());
       // state must be "changed"
       assertState(cms, resourcename, CmsResource.STATE_CHANGED);
       // date last modified 
       assertDateLastModifiedAfter(cms, resourcename, timestamp);
       // the user last modified must be the current user
       assertUserLastModified(cms, resourcename, cms.getRequestContext().currentUser());
       // date created
       assertDateCreatedAfter(cms, resourcename, timestamp);
       // the user created must be the current user
       assertUserCreated(cms, resourcename, cms.getRequestContext().currentUser());       
       // check the content
       assertContent(cms, resourcename, content);     
       
       // compare id's
       CmsResource created = cms.readResource(resourcename);
       if (created.getResourceId().equals(original.getResourceId())) {
           fail("A created resource that replaced a deleted resource must not have the same resource id!");
       }     
       
       // publish the project
       cms.unlockProject(cms.getRequestContext().currentProject().getId());
       cms.publishProject();    
              
       assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);       
   }

    /**
     * Test the import resource method with a folder.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testImportFolder() throws Throwable {

        CmsObject cms = getCmsObject();     
        echo("Testing import resource for a folder");
        
        String resourcename = "/folder1/test1/";
        
        long timestamp = System.currentTimeMillis() - 87654321;        
        
        // create a new resource
        CmsResource resource = new CmsResource (
            CmsUUID.getNullUUID(),
            CmsUUID.getNullUUID(),
            resourcename,
            CmsResourceTypeFolder.getStaticTypeId(),
            true,
            0,
            cms.getRequestContext().currentProject().getId(),
            CmsResource.STATE_NEW,
            timestamp,
            cms.getRequestContext().currentUser().getId(),
            timestamp, 
            cms.getRequestContext().currentUser().getId(),
            CmsResource.DATE_RELEASED_DEFAULT, 
            CmsResource.DATE_EXPIRED_DEFAULT,
            1, -1
        );
        
        cms.importResource(resourcename, resource, null, null);
        
        // ensure created resource is a folder
        assertIsFolder(cms, resourcename);  
        // project must be current project
        assertProject(cms, resourcename, cms.getRequestContext().currentProject());
        // state must be "new"
        assertState(cms, resourcename, CmsResource.STATE_NEW);
        // date last modified 
        assertDateLastModified(cms, resourcename, timestamp);
        // date created
        assertDateCreated(cms, resourcename, timestamp);
        // the user last modified must be the current user
        assertUserLastModified(cms, resourcename, cms.getRequestContext().currentUser()); 
        
        // publish the project
        cms.unlockProject(cms.getRequestContext().currentProject().getId());
        cms.publishProject();   
        
        assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);
    }  
    
    /**
     * Test the import resource method for an existing folder.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testImportFolderAgain() throws Throwable {

        CmsObject cms = getCmsObject();     
        echo("Testing to import an existing folder again");
        
        String resourcename = "/folder1/test1/";
        
        storeResources(cms, resourcename);
        long timestamp = System.currentTimeMillis() - 12345678;       

        // create a new folder
        CmsResource resource = new CmsResource (
            CmsUUID.getNullUUID(),
            CmsUUID.getNullUUID(),
            resourcename,
            CmsResourceTypeFolder.getStaticTypeId(),
            true,
            0,
            cms.getRequestContext().currentProject().getId(),
            CmsResource.STATE_NEW,
            timestamp,
            cms.getRequestContext().currentUser().getId(),
            timestamp, 
            cms.getRequestContext().currentUser().getId(),
            CmsResource.DATE_RELEASED_DEFAULT, 
            CmsResource.DATE_EXPIRED_DEFAULT,
            1, -1
        );
        
        cms.importResource(resourcename, resource, null, null);
        
        // ensure created resource is a folder
        assertIsFolder(cms, resourcename);  
        // project must be current project
        assertProject(cms, resourcename, cms.getRequestContext().currentProject());
        // state must be "new"
        assertState(cms, resourcename, CmsResource.STATE_CHANGED);
        // date last modified 
        assertDateLastModified(cms, resourcename, timestamp);
        // the user last modified must be the current user
        assertUserLastModified(cms, resourcename, cms.getRequestContext().currentUser());         
        // now evaluate the filter
        assertFilter(cms, resourcename, OpenCmsTestResourceFilter.FILTER_CREATE_RESOURCE);       
        
        // publish the project
        cms.unlockProject(cms.getRequestContext().currentProject().getId());
        cms.publishProject();     
        
        assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);        
    }      
    
    /**
     * Test the import resource method.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testImportResource() throws Throwable {

        CmsObject cms = getCmsObject();     
        echo("Testing import resource");
        
        String resourcename = "/folder1/test1.html";
        
        String contentStr = "Hello this is my content";
        byte[] content = contentStr.getBytes();
        long timestamp = System.currentTimeMillis() - 87654321;        
        
        // create a new resource
        CmsResource resource = new CmsResource (
            CmsUUID.getNullUUID(),
            CmsUUID.getNullUUID(),
            resourcename,
            CmsResourceTypePlain.getStaticTypeId(),
            false,
            0,
            cms.getRequestContext().currentProject().getId(),
            CmsResource.STATE_NEW,
            timestamp,
            cms.getRequestContext().currentUser().getId(),
            timestamp, 
            cms.getRequestContext().currentUser().getId(),
            CmsResource.DATE_RELEASED_DEFAULT, 
            CmsResource.DATE_EXPIRED_DEFAULT,
            1, content.length
        );
        
        cms.importResource(resourcename, resource, content, null);
        
        // ensure created resource type
        assertResourceType(cms, resourcename, CmsResourceTypePlain.getStaticTypeId());  
        // project must be current project
        assertProject(cms, resourcename, cms.getRequestContext().currentProject());
        // state must be "new"
        assertState(cms, resourcename, CmsResource.STATE_NEW);
        // date last modified 
        assertDateLastModified(cms, resourcename, timestamp);
        // date created
        assertDateCreated(cms, resourcename, timestamp);
        // the user last modified must be the current user
        assertUserLastModified(cms, resourcename, cms.getRequestContext().currentUser()); 
        // the content 
        assertContent(cms, resourcename, content);
        
        // publish the project
        cms.unlockProject(cms.getRequestContext().currentProject().getId());
        cms.publishProject();   
        
        assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);
    }  
    
    /**
     * Test the import resource method.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testImportResourceAgain() throws Throwable {

        CmsObject cms = getCmsObject();     
        echo("Testing to import an existing resource again");
        
        String resourcename = "/folder1/test1.html";
        
        storeResources(cms, resourcename);
        long timestamp = System.currentTimeMillis() - 12345678;
        
        String contentStr = "Hello this is my NEW AND CHANGED content";
        byte[] content = contentStr.getBytes();

        // create a new resource
        CmsResource resource = new CmsResource (
            CmsUUID.getNullUUID(),
            CmsUUID.getNullUUID(),
            resourcename,
            CmsResourceTypePlain.getStaticTypeId(),
            false,
            0,
            cms.getRequestContext().currentProject().getId(),
            CmsResource.STATE_NEW,
            timestamp,
            cms.getRequestContext().currentUser().getId(),
            timestamp, 
            cms.getRequestContext().currentUser().getId(),
            CmsResource.DATE_RELEASED_DEFAULT, 
            CmsResource.DATE_EXPIRED_DEFAULT,
            1, content.length
        );
        
        cms.importResource(resourcename, resource, content, null);
        
        // ensure created resource type
        assertResourceType(cms, resourcename, CmsResourceTypePlain.getStaticTypeId()); 
        // project must be current project
        assertProject(cms, resourcename, cms.getRequestContext().currentProject());
        // state must be "new"
        assertState(cms, resourcename, CmsResource.STATE_CHANGED);
        // date last modified 
        assertDateLastModified(cms, resourcename, timestamp);
        // the user last modified must be the current user
        assertUserLastModified(cms, resourcename, cms.getRequestContext().currentUser());         
        // now evaluate the filter
        assertFilter(cms, resourcename, OpenCmsTestResourceFilter.FILTER_CREATE_RESOURCE);        
                
        // publish the project
        cms.unlockProject(cms.getRequestContext().currentProject().getId());
        cms.publishProject();     
        
        assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);        
    }  
        
    /**
     * Test the import of a sibling.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testImportSibling() throws Throwable {

        CmsObject cms = getCmsObject();     
        echo("Testing to import an existing resource as sibling");

        CmsProperty prop1 = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "The title", null);
        CmsProperty prop2 = new CmsProperty(CmsPropertyDefinition.PROPERTY_DESCRIPTION, "The description", null);
        CmsProperty prop3 = new CmsProperty(CmsPropertyDefinition.PROPERTY_KEYWORDS, "The keywords", null);
        
        List properties = new ArrayList();
        properties.add(prop1);
        
        String siblingname = "/folder1/test1.html";
        
        // make sure some non-shared properties are attached to the sibling
        cms.lockResource(siblingname);
        cms.writePropertyObjects(siblingname, properties);
        cms.unlockResource(siblingname);
        
        long timestamp = System.currentTimeMillis() - 12345678;
        
        String resourcename1 = "/folder2/test1_sib1.html";
        String resourcename2 = "/folder1/subfolder11/test1_sib2.html";
        
        // read the existing resource to create siblings for
        CmsFile file = cms.readFile(siblingname);
        byte[] content = file.getContents(); 
        
        assertTrue(file.getLength() > 0);
        assertTrue(content.length > 0);
        
        storeResources(cms, siblingname);

        // create a new resource
        CmsResource resource;

        // cw: Test changed: must now provide correct content size in resource
        resource= new CmsResource (
            file.getStructureId(),
            file.getResourceId(),
            resourcename2,
            CmsResourceTypePlain.getStaticTypeId(),
            false,
            0,
            cms.getRequestContext().currentProject().getId(),
            CmsResource.STATE_NEW,
            timestamp,
            cms.getRequestContext().currentUser().getId(),
            timestamp, 
            cms.getRequestContext().currentUser().getId(),
            CmsResource.DATE_RELEASED_DEFAULT, 
            CmsResource.DATE_EXPIRED_DEFAULT,
            1, content.length
        );
        
        properties.add(prop2);         
        // using null as content must create sibling of existing content
        cms.importResource(resourcename2, resource, null, properties);
        
        // project must be current project
        assertProject(cms, resourcename2, cms.getRequestContext().currentProject());
        // resource type
        assertResourceType(cms, resourcename2, CmsResourceTypePlain.getStaticTypeId());
        assertResourceType(cms, siblingname, CmsResourceTypePlain.getStaticTypeId());
        // state
        assertState(cms, resourcename2, CmsResource.STATE_NEW);
        assertState(cms, siblingname, CmsResource.STATE_CHANGED);
        // date last modified
        assertDateLastModified(cms, resourcename2, file.getDateLastModified());      
        assertDateLastModified(cms, siblingname, file.getDateLastModified());      
        // the user last modified
        assertUserLastModified(cms, resourcename2, cms.getRequestContext().currentUser());
        assertUserLastModified(cms, siblingname, cms.getRequestContext().currentUser());
        // content must be identical to stored content of new resource
        assertContent(cms, resourcename2, content);
        assertContent(cms, siblingname, content);       
        // check the sibling count
        assertSiblingCountIncremented(cms, siblingname, 1);       

        // now evaluate the filter
        OpenCmsTestResourceConfigurableFilter filter =
            new OpenCmsTestResourceConfigurableFilter(OpenCmsTestResourceFilter.FILTER_CREATE_RESOURCE);

        filter.disableSiblingCountTest();
        assertFilter(cms, siblingname, filter);   
        
        String contentStr = "Hello this is my NEW AND CHANGED sibling content";
        content = contentStr.getBytes();        
        
        resource= new CmsResource (
            file.getStructureId(),
            file.getResourceId(),
            resourcename1,
            CmsResourceTypePlain.getStaticTypeId(),
            false,
            0,
            cms.getRequestContext().currentProject().getId(),
            CmsResource.STATE_NEW,
            timestamp,
            cms.getRequestContext().currentUser().getId(),
            timestamp, 
            cms.getRequestContext().currentUser().getId(),
            CmsResource.DATE_RELEASED_DEFAULT, 
            CmsResource.DATE_EXPIRED_DEFAULT,
            1, content.length
        );
        
        properties.add(prop3);
        // using new content must replace existing content
        cms.importResource(resourcename1, resource, content, properties);
                
        // project must be current project
        assertProject(cms, resourcename1, cms.getRequestContext().currentProject());
        assertProject(cms, resourcename2, cms.getRequestContext().currentProject());
        // resource type
        assertResourceType(cms, resourcename1, CmsResourceTypePlain.getStaticTypeId());
        assertResourceType(cms, resourcename2, CmsResourceTypePlain.getStaticTypeId());
        assertResourceType(cms, siblingname, CmsResourceTypePlain.getStaticTypeId());
        // state
        assertState(cms, resourcename1, CmsResource.STATE_NEW);
        assertState(cms, resourcename2, CmsResource.STATE_NEW);
        assertState(cms, siblingname, CmsResource.STATE_CHANGED);
        // date last modified
        assertDateLastModified(cms, resourcename1, timestamp);      
        assertDateLastModified(cms, resourcename2, timestamp);      
        assertDateLastModified(cms, siblingname, timestamp);      
        // the user last modified
        assertUserLastModified(cms, resourcename1, cms.getRequestContext().currentUser());
        assertUserLastModified(cms, resourcename2, cms.getRequestContext().currentUser());
        assertUserLastModified(cms, siblingname, cms.getRequestContext().currentUser());
        // content must be identical to stored content of new resource
        assertContent(cms, resourcename1, content);
        assertContent(cms, resourcename2, content);
        assertContent(cms, siblingname, content);       
        // check the sibling count
        assertSiblingCountIncremented(cms, siblingname, 2);       

        // now evaluate the filter
        assertFilter(cms, siblingname, filter);   
        
        // publish the project
        cms.unlockProject(cms.getRequestContext().currentProject().getId());
        cms.publishProject();     
        
        assertState(cms, resourcename1, CmsResource.STATE_UNCHANGED);  
        assertState(cms, resourcename2, CmsResource.STATE_UNCHANGED);  
    }  
}

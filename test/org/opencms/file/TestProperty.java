/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestProperty.java,v $
 * Date   : $Date: 2005/03/19 13:59:19 $
 * Version: $Revision: 1.12 $
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

import org.opencms.main.I_CmsConstants;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestResourceFilter;

import java.util.ArrayList;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for the "writeProperty" method of the CmsObject.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.12 $
 */
public class TestProperty extends OpenCmsTestCase {
            
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestProperty(String arg0) {
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
        suite.setName(TestProperty.class.getName());
                
        suite.addTest(new TestProperty("testWriteProperty"));
        suite.addTest(new TestProperty("testWriteProperties"));
        suite.addTest(new TestProperty("testRemoveProperty"));
        suite.addTest(new TestProperty("testRemoveProperties"));
        suite.addTest(new TestProperty("testCreateProperty"));
        suite.addTest(new TestProperty("testCreateProperties"));
        suite.addTest(new TestProperty("testWritePropertyOnFolder"));
        suite.addTest(new TestProperty("testDefaultPropertyCreation"));
        
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
     * Test the writeProperty method to create a list of properties.<p>
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to create the properies
     * @param propertyList1 the properties to create
     * @throws Throwable if something goes wrong
     */
    public static void createProperties(OpenCmsTestCase tc, CmsObject cms, String resource1, List propertyList1) throws Throwable {

         tc.storeResources(cms, resource1);

         long timestamp = System.currentTimeMillis();
                   
         cms.lockResource(resource1);         
         cms.writePropertyObjects(resource1, propertyList1);
         cms.unlockResource(resource1);
         
         // now evaluate the result
         tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);
         // project must be current project
         tc.assertProject(cms, resource1, cms.getRequestContext().currentProject());
         // state must be "changed"
         tc.assertState(cms, resource1, tc.getPreCalculatedState(resource1));
         // date last modified must be after the test timestamp
         tc.assertDateLastModifiedAfter(cms, resource1, timestamp);
         // the user last modified must be the current user
         tc.assertUserLastModified(cms, resource1, cms.getRequestContext().currentUser());
         // the properties must be new
         tc.assertPropertyNew(cms, resource1, propertyList1);  
    }
    
    /**
     * Test the writeProperty method to create one property.<p>
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to add a propery
     * @param property1 the property to create
     * @throws Throwable if something goes wrong
     */
    public static void createProperty(OpenCmsTestCase tc, CmsObject cms, String resource1, CmsProperty property1) throws Throwable {

         tc.storeResources(cms, resource1);

         long timestamp = System.currentTimeMillis();

         cms.lockResource(resource1);         
         cms.writePropertyObject(resource1, property1);
         cms.unlockResource(resource1);
         
         // now evaluate the result
         tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);
         // project must be current project
         tc.assertProject(cms, resource1, cms.getRequestContext().currentProject());
         // state must be "changed"
         tc.assertState(cms, resource1, tc.getPreCalculatedState(resource1));
         // date last modified must be after the test timestamp
         tc.assertDateLastModifiedAfter(cms, resource1, timestamp);
         // the user last modified must be the current user
         tc.assertUserLastModified(cms, resource1, cms.getRequestContext().currentUser());
         // the property must be new
         tc.assertPropertyNew(cms, resource1, property1); 
    }
    
    /**
     * Test the writeProperty method to remove a list of properties.<p>
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to remove the properies
     * @param propertyList1 the properties to remove
     * @throws Throwable if something goes wrong
     */
    public static void removeProperties(OpenCmsTestCase tc, CmsObject cms, String resource1, List propertyList1) throws Throwable {     

        tc.storeResources(cms, resource1);

        long timestamp = System.currentTimeMillis();
        
        cms.lockResource(resource1);        
        cms.writePropertyObjects(resource1, propertyList1);        
        cms.unlockResource(resource1);
        
        // now evaluate the result
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);
        // project must be current project
        tc.assertProject(cms, resource1, cms.getRequestContext().currentProject());
        // state must be "changed"
        tc.assertState(cms, resource1, tc.getPreCalculatedState(resource1));
        // date last modified must be after the test timestamp
        tc.assertDateLastModifiedAfter(cms, resource1, timestamp);
        // the user last modified must be the current user
        tc.assertUserLastModified(cms, resource1, cms.getRequestContext().currentUser());
        // the properties must have been removed
        tc.assertPropertyRemoved(cms, resource1, propertyList1);          
    }
    
    /**
     * Test the writeProperty method to remove one property.<p>
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to remove a propery
     * @param property1 the property to remove
     * @throws Throwable if something goes wrong
     */
    public static void removeProperty(OpenCmsTestCase tc, CmsObject cms, String resource1, CmsProperty property1) throws Throwable {  

        tc.storeResources(cms, resource1);
  
         long timestamp = System.currentTimeMillis();
         
         cms.lockResource(resource1);         
         cms.writePropertyObject(resource1, property1);
         cms.unlockResource(resource1);
         
         // now evaluate the result
         tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);
         // project must be current project
         tc.assertProject(cms, resource1, cms.getRequestContext().currentProject());
         // state must be "changed"
         tc.assertState(cms, resource1, tc.getPreCalculatedState(resource1));
         // date last modified must be after the test timestamp
         tc.assertDateLastModifiedAfter(cms, resource1, timestamp);
         // the user last modified must be the current user
         tc.assertUserLastModified(cms, resource1, cms.getRequestContext().currentUser());
         // the property must be removed
         tc.assertPropertyRemoved(cms, resource1, property1);       
    }       
    
    /**
     * Test the writeProperty method with a list of properties.<p>
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to write the properies
     * @param propertyList1 the properties to write
     * @throws Throwable if something goes wrong
     */
    public static void writeProperties(OpenCmsTestCase tc, CmsObject cms, String resource1, List propertyList1) throws Throwable {

         tc.storeResources(cms, resource1);
  
         long timestamp = System.currentTimeMillis();
         
         cms.lockResource(resource1);         
         cms.writePropertyObjects(resource1, propertyList1);
         cms.unlockResource(resource1);
         
         // now evaluate the result
         tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);
         // project must be current project
         tc.assertProject(cms, resource1, cms.getRequestContext().currentProject());
         // state must be "changed"
         tc.assertState(cms, resource1, I_CmsConstants.C_STATE_CHANGED);
         // date last modified must be after the test timestamp
         tc.assertDateLastModifiedAfter(cms, resource1, timestamp);
         // the user last modified must be the current user
         tc.assertUserLastModified(cms, resource1, cms.getRequestContext().currentUser());
         // the property must have the new value
         tc.assertPropertyChanged(cms, resource1, propertyList1);       
    }
    
    /**
     * Test the writeProperty method with one property.<p>
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to write a propery
     * @param property1 the property to write
     * @throws Throwable if something goes wrong
     */
    public static void writeProperty(OpenCmsTestCase tc, CmsObject cms, String resource1, CmsProperty property1) throws Throwable {
          
        tc.storeResources(cms, resource1);
  
         long timestamp = System.currentTimeMillis();
                  
         cms.lockResource(resource1);
         cms.writePropertyObject(resource1, property1);
         cms.unlockResource(resource1);
         
         // now evaluate the result
         tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);
         // project must be current project
         tc.assertProject(cms, resource1, cms.getRequestContext().currentProject());
         // state must be "changed"
         tc.assertState(cms, resource1, tc.getPreCalculatedState(resource1));
         // date last modified must be after the test timestamp
         tc.assertDateLastModifiedAfter(cms, resource1, timestamp);
         // the user last modified must be the current user
         tc.assertUserLastModified(cms, resource1, cms.getRequestContext().currentUser());
         // the property must have the new value
         tc.assertPropertyChanged(cms, resource1, property1);       
    }      
    
    /**
     * Tests the writePropertyObjects method for removing of properties.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCreateProperties() throws Throwable {  
        
        CmsObject cms = getCmsObject(); 
        echo("Testing creating multiple properties on a resource");
        CmsProperty property8 = new CmsProperty("Newproperty", "testvalue1", "testvalue2");
        CmsProperty property9 = new CmsProperty("AnotherNewproperty", "anothervalue", null);  
        List propertyList3 = new ArrayList();
        propertyList3.add(property8);
        propertyList3.add(property9);
        createProperties(this, cms, "/index.html", propertyList3);
    }
        
    /**
     * Tests the writePropertyObject method for removing of properties.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCreateProperty() throws Throwable {  
        
        CmsObject cms = getCmsObject(); 
        echo("Testing creating one property on a resource");
        CmsProperty property7 = new CmsProperty("Newproperty", "testvalue1", "testvalue2");
        createProperty(this, cms, "/folder1/index.html", property7);
    }
    
    /**
     * Tests the writePropertyObjects method for removing of multiple properties.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testRemoveProperties() throws Throwable {  
            
        CmsObject cms = getCmsObject(); 
        echo("Testing removing multiple properties on a resource");
        CmsProperty property5 = new CmsProperty("Title", CmsProperty.C_DELETE_VALUE, CmsProperty.C_DELETE_VALUE);
        CmsProperty property6 = new CmsProperty("NavPos", CmsProperty.C_DELETE_VALUE, CmsProperty.C_DELETE_VALUE);
        List propertyList2 = new ArrayList();
        propertyList2.add(property5);
        propertyList2.add(property6);
        removeProperties(this, cms, "/folder1/page1.html", propertyList2);
    }
    
    /**
     * Tests the writePropertyObject method for removing of a single property.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testRemoveProperty() throws Throwable {  
        
        CmsObject cms = getCmsObject();  
        echo("Testing removing one property on a resource");
        CmsProperty property4 = new CmsProperty("Title", CmsProperty.C_DELETE_VALUE, CmsProperty.C_DELETE_VALUE);                 
        removeProperty(this, cms, "/folder1/page2.html", property4);
    }
        
    /**
     * Tests the writeProperties method.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testWriteProperties() throws Throwable {
                
        CmsObject cms = getCmsObject();         
        echo("Testing writing multiple properties on a resource");
        CmsProperty property2 = new CmsProperty("Title", "OpenCms", null);
        CmsProperty property3 = new CmsProperty("NavPos", "1", null);
        List propertyList1 = new ArrayList();
        propertyList1.add(property2);
        propertyList1.add(property3); 
        writeProperties(this, cms, "/folder1/page3.html", propertyList1);
    }
    
    /**
     * Tests the writePropertyObject method.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testWriteProperty() throws Throwable {
        
        CmsObject cms = getCmsObject();              
        echo("Testing writing one  property on a resource");
        CmsProperty property1 = new CmsProperty("Title", "OpenCms", null);  
        writeProperty(this, cms, "/folder1/image1.gif", property1);
    }
    
    /**
     * Tests the writePropertyObject method for writing of a property on a folder.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testWritePropertyOnFolder() throws Throwable {  
        
        CmsObject cms = getCmsObject(); 
        echo("Testing writing one property on a folder");
        CmsProperty property10 = new CmsProperty("Title", "OpenCms", null);  
        writeProperty(this, cms, "/folder2/", property10);
    }  
    
    /**
     * Test default property creation (from resource type configuration).<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testDefaultPropertyCreation() throws Throwable {
        
        CmsObject cms = getCmsObject(); 
        echo("Testing default property creation");
        
        String resourcename = "/folder1/article_test.html";
        byte[] content = new byte[0];      
        
        // resource 12 is article (xml content) with default properties
        cms.createResource(resourcename, 12, content, null);
        
        // ensure created resource type
        assertResourceType(cms, resourcename, 12);  
        // project must be current project
        assertProject(cms, resourcename, cms.getRequestContext().currentProject());
        // state must be "new"
        assertState(cms, resourcename, I_CmsConstants.C_STATE_NEW);
        // the user last modified must be the current user
        assertUserLastModified(cms, resourcename, cms.getRequestContext().currentUser()); 
               
        CmsProperty property1, property2;
        property1 = new CmsProperty(I_CmsConstants.C_PROPERTY_TITLE, "Test title", null);
        property2 = cms.readPropertyObject(resourcename, I_CmsConstants.C_PROPERTY_TITLE, false);
        assertTrue(property1.isIdentical(property2));        
        
        property1 = new CmsProperty("template-elements", "/system/modules/org.opencms.frontend.templateone.form/pages/form.html", null);
        property2 = cms.readPropertyObject(resourcename, "template-elements", false);
        assertTrue(property1.isIdentical(property2));        

        property1 = new CmsProperty(I_CmsConstants.C_PROPERTY_DESCRIPTION, null, "Admin_/folder1/article_test.html_/sites/default/folder1/article_test.html");
        property2 = cms.readPropertyObject(resourcename, I_CmsConstants.C_PROPERTY_DESCRIPTION, false);
        assertTrue(property1.isIdentical(property2));        
        
        // publish the project
        cms.unlockProject(cms.getRequestContext().currentProject().getId());
        cms.publishProject();    
        
        assertState(cms, resourcename, I_CmsConstants.C_STATE_UNCHANGED);                
    }    
}

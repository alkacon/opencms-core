/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestProperty.java,v $
 * Date   : $Date: 2004/05/28 15:04:59 $
 * Version: $Revision: 1.3 $
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

import org.opencms.main.I_CmsConstants;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestResourceFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for the "writeProperty" method of the CmsObject.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.3 $
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
     * Tests the writeProperty method.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testWritePropertyObject() throws Throwable {
        
        // setup OpenCms
        CmsObject cms = setupOpenCms("simpletest", "/sites/default/");
              
        echo("Testing write property on resource");
        CmsProperty property1 = new CmsProperty("Title", "OpenCms", null);  
        writeProperty(this, cms, "/release/installation.html", property1);
  
        echo("Testing write properties on resource");
        CmsProperty property2 = new CmsProperty("Title", "OpenCms", null);
        CmsProperty property3 = new CmsProperty("NavPos", "1", null);
        List propertyList1 = new ArrayList();
        propertyList1.add(property2);
        propertyList1.add(property3); 
        writeProperties(this, cms, "/release/mailinglist.html", propertyList1);
        
        echo("Testing remove property on resource");
        CmsProperty property4 = new CmsProperty("Title", CmsProperty.C_DELETE_VALUE, CmsProperty.C_DELETE_VALUE);                 
        removeProperty(this, cms, "/release/notes_5.0.0.html", property4);
        
        echo("Testing remove properties on resource");
        CmsProperty property5 = new CmsProperty("Title", CmsProperty.C_DELETE_VALUE, CmsProperty.C_DELETE_VALUE);
        CmsProperty property6 = new CmsProperty("NavPos", CmsProperty.C_DELETE_VALUE, CmsProperty.C_DELETE_VALUE);
        List propertyList2 = new ArrayList();
        propertyList1.add(property5);
        propertyList1.add(property6);
        removeProperties(this, cms, "/release/notes_5.0b2.html", propertyList2);
        
        echo("Testing creating property on resource");
        CmsProperty property7 = new CmsProperty("Newproperty", "testvalue1", "testvalue2");
        createProperty(this, cms, "/release/notes_5.0rc1.html", property7);
        
        echo("Testing creating properties on resource");
        CmsProperty property8 = new CmsProperty("Newproperty", "testvalue1", "testvalue2");
        CmsProperty property9 = new CmsProperty("AnotherNewproperty", "anothervalue", null);  
        List propertyList3 = new ArrayList();
        propertyList1.add(property8);
        propertyList1.add(property9);
        createProperties(this, cms, "/release/notes_5.0rc2.html", propertyList3);
                
        // remove OpenCms
        removeOpenCms();
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
                   
         cms.writePropertyObjects(resource1, propertyList1);
               
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

         cms.writePropertyObject(resource1, property1);
                  
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
        
        cms.writePropertyObjects(resource1, propertyList1);        
        
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
         
         cms.writePropertyObject(resource1, property1);
         
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
         
         cms.writePropertyObjects(resource1, propertyList1);
         
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
                  
         cms.writePropertyObject(resource1, property1);
         
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
    
}

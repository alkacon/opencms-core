/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/Attic/TestCmsObjectWritePropertyObject.java,v $
 * Date   : $Date: 2004/05/27 10:13:02 $
 * Version: $Revision: 1.4 $
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
 * Unit test for the "touch" method of the CmsObject.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.4 $
 */
public class TestCmsObjectWritePropertyObject extends OpenCmsTestCase {
        
    
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestCmsObjectWritePropertyObject(String arg0) {
        super(arg0);
    }
    
    
    /**
     * Tests the touch method in the CmsObject.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCmsObjectWriteProperetyObject() throws Throwable {
        
        // setup OpenCms
        CmsObject cms = setupOpenCms("simpletest", "/sites/default/");
              
        // now do the tests 
        testWriteProperty(cms);
        testWriteProperties(cms);
        testRemoveProperty(cms);
        testRemoveProperties(cms);
        testCreateProperty(cms);
        testCreateProperties(cms);
                
        // remove OpenCms
        removeOpenCms();
    }
    
    
    /**
     * Test the writeProperty method with a list of properties.<p>
     * @param cms the CmsObject
     * @throws Throwable if something goes wrong
     */
    private void testWriteProperties(CmsObject cms) throws Throwable {
         String resource1 = "/release/mailinglist.html";            

         storeResources(cms, resource1);
  
         long timestamp = System.currentTimeMillis();
         
         CmsProperty property1 = new CmsProperty("Title","OpenCms",null);
         CmsProperty property2 = new CmsProperty("NavPos","1",null);
         CmsProperty property3 = new CmsProperty("Title","OpenCms2",null);
         CmsProperty property4 = new CmsProperty("NavPos","2",null);
         List propertyList1 = new ArrayList();
         propertyList1.add(property1);
         propertyList1.add(property2);        
         List propertyList2 = new ArrayList();
         propertyList2.add(property3);
         propertyList2.add(property4);
         
         cms.writePropertyObjects(resource1, propertyList1);
         
         // now evaluate the result
         assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);
         // project must be current project
         assertProject(cms, resource1, cms.getRequestContext().currentProject());
         // state must be "changed"
         assertState(cms, resource1, I_CmsConstants.C_STATE_CHANGED);
         // date last modified must be after the test timestamp
         assertDateLastModifiedAfter(cms, resource1, timestamp);
         // the user last modified must be the current user
         assertUserLastModified(cms, resource1, cms.getRequestContext().currentUser());
         // the property must have the new value
         assertPropertyChanged(cms, resource1, propertyList1);       
    }
    
    /**
     * Test the writeProperty method with one property.<p>
     * @param cms the CmsObject
     * @throws Throwable if something goes wrong
     */
    private void testWriteProperty(CmsObject cms) throws Throwable {
         String resource1 = "/release/installation.html";            

         storeResources(cms, resource1);
  
         long timestamp = System.currentTimeMillis();
         
         CmsProperty property1 = new CmsProperty("Title","OpenCms",null);
         //CmsProperty property2 = new CmsProperty("Title","OpenCms2",null);
         
         cms.writePropertyObject(resource1, property1);
         
         // now evaluate the result
         assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);
         // project must be current project
         assertProject(cms, resource1, cms.getRequestContext().currentProject());
         // state must be "changed"
         assertState(cms, resource1, I_CmsConstants.C_STATE_CHANGED);
         // date last modified must be after the test timestamp
         assertDateLastModifiedAfter(cms, resource1, timestamp);
         // the user last modified must be the current user
         assertUserLastModified(cms, resource1, cms.getRequestContext().currentUser());
         // the property must have the new value
         assertPropertyChanged(cms, resource1, property1);       
    }
    
    /**
     * Test the writeProperty method to remove one property.<p>
     * @param cms the CmsObject
     * @throws Throwable if something goes wrong
     */
    private void testRemoveProperty(CmsObject cms) throws Throwable {
         String resource1 = "/release/notes_5.0.0.html";            

         storeResources(cms, resource1);
  
         long timestamp = System.currentTimeMillis();
         
         CmsProperty property1 = new CmsProperty("Title",CmsProperty.C_DELETE_VALUE,CmsProperty.C_DELETE_VALUE);         
         //CmsProperty property2 = new CmsProperty("Title","OpenCms",null);
         
         cms.writePropertyObject(resource1, property1);
         
         // now evaluate the result
         assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);
         // project must be current project
         assertProject(cms, resource1, cms.getRequestContext().currentProject());
         // state must be "changed"
         assertState(cms, resource1, I_CmsConstants.C_STATE_CHANGED);
         // date last modified must be after the test timestamp
         assertDateLastModifiedAfter(cms, resource1, timestamp);
         // the user last modified must be the current user
         assertUserLastModified(cms, resource1, cms.getRequestContext().currentUser());
         // the property must be removed
         assertPropertyRemoved(cms, resource1, property1);       
    }
    
    /**
     * Test the writeProperty method to remove a list of properties.<p>
     * @param cms the CmsObject
     * @throws Throwable if something goes wrong
     */
    private void testRemoveProperties(CmsObject cms) throws Throwable {
        String resource1 = "/release/notes_5.0b2.html";            

        storeResources(cms, resource1);

        long timestamp = System.currentTimeMillis();
        
        CmsProperty property1 = new CmsProperty("Title",CmsProperty.C_DELETE_VALUE,CmsProperty.C_DELETE_VALUE);
        CmsProperty property2 = new CmsProperty("NavPos",CmsProperty.C_DELETE_VALUE,CmsProperty.C_DELETE_VALUE);

        List propertyList1 = new ArrayList();
        propertyList1.add(property1);
        propertyList1.add(property2);
        
        cms.writePropertyObjects(resource1, propertyList1);        
        
        // now evaluate the result
        assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);
        // project must be current project
        assertProject(cms, resource1, cms.getRequestContext().currentProject());
        // state must be "changed"
        assertState(cms, resource1, I_CmsConstants.C_STATE_CHANGED);
        // date last modified must be after the test timestamp
        assertDateLastModifiedAfter(cms, resource1, timestamp);
        // the user last modified must be the current user
        assertUserLastModified(cms, resource1, cms.getRequestContext().currentUser());
        // the properties must have been removed
        assertPropertyRemoved(cms, resource1, propertyList1);          
    }
    
    /**
     * Test the writeProperty method to create one property.<p>
     * @param cms the CmsObject
     * @throws Throwable if something goes wrong
     */
    private void testCreateProperty(CmsObject cms) throws Throwable {
         String resource1 = "/release/notes_5.0rc1.html";            

         storeResources(cms, resource1);

         long timestamp = System.currentTimeMillis();
         
         CmsProperty property1 = new CmsProperty("Newproperty","testvalue1","testvalue2");
         //CmsProperty property2 = new CmsProperty("Title","test", null);
         
         cms.writePropertyObject(resource1, property1);
                  
         // now evaluate the result
         assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);
         // project must be current project
         assertProject(cms, resource1, cms.getRequestContext().currentProject());
         // state must be "changed"
         assertState(cms, resource1, I_CmsConstants.C_STATE_CHANGED);
         // date last modified must be after the test timestamp
         assertDateLastModifiedAfter(cms, resource1, timestamp);
         // the user last modified must be the current user
         assertUserLastModified(cms, resource1, cms.getRequestContext().currentUser());
         // the property must be new
         assertPropertyNew(cms, resource1, property1); 
    }
    
    /**
     * Test the writeProperty method to create a list of properties.<p>
     * @param cms the CmsObject
     * @throws Throwable if something goes wrong
     */
    private void testCreateProperties(CmsObject cms) throws Throwable {
         String resource1 = "/release/notes_5.0rc2.html";            

         storeResources(cms, resource1);

         long timestamp = System.currentTimeMillis();
          
         CmsProperty property1 = new CmsProperty("Newproperty","testvalue1","testvalue2");
         CmsProperty property2 = new CmsProperty("AnotherNewproperty","anothervalue", null);
         CmsProperty property3 = new CmsProperty("Newproperty","test","test2");
         CmsProperty property4 = new CmsProperty("AnotherNewproperty","test", null);
         //CmsProperty property5 = new CmsProperty("Title","test", null);
         
         List propertyList1 = new ArrayList();
         propertyList1.add(property1);
         propertyList1.add(property2);
         
         List propertyList2 = new ArrayList();
         propertyList2.add(property3);
         propertyList2.add(property4);
         
         cms.writePropertyObjects(resource1, propertyList1);
               
         // now evaluate the result
         assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_WRITEPROPERTY);
         // project must be current project
         assertProject(cms, resource1, cms.getRequestContext().currentProject());
         // state must be "changed"
         assertState(cms, resource1, I_CmsConstants.C_STATE_CHANGED);
         // date last modified must be after the test timestamp
         assertDateLastModifiedAfter(cms, resource1, timestamp);
         // the user last modified must be the current user
         assertUserLastModified(cms, resource1, cms.getRequestContext().currentUser());
         // the properties must be new
         assertPropertyNew(cms, resource1, propertyList1);  
    }
    
}

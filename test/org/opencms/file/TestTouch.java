/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestTouch.java,v $
 * Date   : $Date: 2004/06/04 15:42:06 $
 * Version: $Revision: 1.7 $
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

import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.opencms.main.I_CmsConstants;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestResourceFilter;

/**
 * Unit test for the "touch" method of the CmsObject.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.7 $
 */
public class TestTouch extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestTouch(String arg0) {
        super(arg0);
    }
    
    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {
        
        TestSuite suite = new TestSuite();
        
        suite.addTest(new TestTouch("testTouchFile"));
        suite.addTest(new TestTouch("testTouchFolder"));
        suite.addTest(new TestTouch("testTouchFolderRecursive"));
        
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
     * Test the touch method to touch a single resource.<p>
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to touch
     * @throws Throwable if something goes wrong
     */
    public static void touchResource(OpenCmsTestCase tc, CmsObject cms, String resource1) throws Throwable {            
       
        tc.storeResources(cms, resource1);

        long timestamp = System.currentTimeMillis();
        cms.touch(resource1, timestamp, I_CmsConstants.C_DATE_UNCHANGED, I_CmsConstants.C_DATE_UNCHANGED, false);

        // now evaluate the result
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_TOUCH);
        // project must be current project
        tc.assertProject(cms, resource1, cms.getRequestContext().currentProject());
        // state must be "changed"
        tc.assertState(cms, resource1, tc.getPreCalculatedState(resource1));
        // date last modified must be the date set in the tough operation
        tc.assertDateLastModified(cms, resource1, timestamp);
        // the user last modified must be the current user
        tc.assertUserLastModified(cms, resource1, cms.getRequestContext().currentUser());
    }
    
    /**
     * Test the touch method to touch a single folder.<p>
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to touch
     * @throws Throwable if something goes wrong
     */
    public static void touchResources(OpenCmsTestCase tc, CmsObject cms, String resource1) throws Throwable {

        tc.storeResources(cms, resource1);
         
        long timestamp = System.currentTimeMillis();
        cms.touch(resource1, timestamp, I_CmsConstants.C_DATE_UNCHANGED, I_CmsConstants.C_DATE_UNCHANGED, false);

        // now evaluate the result
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_TOUCH);
        // project must be current project
        tc.assertProject(cms, resource1, cms.getRequestContext().currentProject());
        // state must be "changed"
        tc.assertState(cms, resource1, tc.getPreCalculatedState(resource1));
        // date last modified must be the date set in the tough operation
        tc.assertDateLastModified(cms, resource1, timestamp);
        // the user last modified must be the current user
        tc.assertUserLastModified(cms, resource1, cms.getRequestContext().currentUser());
        
        // evaluate all subresources
        List subresources = tc.getSubtree(cms, resource1);
        
        // iterate through the subresources
        Iterator i = subresources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            String resName = cms.readAbsolutePath(res, CmsResourceFilter.ALL);
            // now evaluate the result
            tc.assertFilter(cms, resName, OpenCmsTestResourceFilter.FILTER_EQUAL);
        }                   
    }
    
    /**
     * Test the touch method to touch a complete subtree.<p>
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to touch
     * @throws Throwable if something goes wrong
     */
    public static void touchResourcesRecursive(OpenCmsTestCase tc, CmsObject cms, String resource1) throws Throwable {
            
        tc.storeResources(cms, resource1);
        
        long timestamp = System.currentTimeMillis();
        cms.touch(resource1, timestamp, I_CmsConstants.C_DATE_UNCHANGED, I_CmsConstants.C_DATE_UNCHANGED, true);

        // now evaluate the result
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_TOUCH);
        // project must be current project
        tc.assertProject(cms, resource1, cms.getRequestContext().currentProject());
        // state must be "changed"
        tc.assertState(cms, resource1, tc.getPreCalculatedState(resource1));
        // date last modified must be the date set in the tough operation
        tc.assertDateLastModified(cms, resource1, timestamp);
        // the user last modified must be the current user
        tc.assertUserLastModified(cms, resource1, cms.getRequestContext().currentUser());
        
        // evaluate all subresources
        List subresources = tc.getSubtree(cms, resource1);
        
        // iterate through the subresources
        Iterator i = subresources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            String resName = cms.readAbsolutePath(res, CmsResourceFilter.ALL);
            // now evaluate the result
            tc.assertFilter(cms, resName, OpenCmsTestResourceFilter.FILTER_TOUCH);
            // project must be current project
            tc.assertProject(cms, resName, cms.getRequestContext().currentProject());
            // state must be "changed"
            tc.assertState(cms, resName, tc.getPreCalculatedState(resName));           
            // date last modified must be the date set in the tough operation
            tc.assertDateLastModified(cms, resName, timestamp);
            // the user last modified must be the current user
            tc.assertUserLastModified(cms, resName, cms.getRequestContext().currentUser());
        }                   
    }
    
    /**
     * Test the touch method on a file.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testTouchFile() throws Throwable {

        CmsObject cms = getCmsObject();     
        echo("Testing touch on file");
        touchResource(this, cms, "/index.html");   
    }  
    
    /**
     * Test the touch method on a folder.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testTouchFolder() throws Throwable {
        
        CmsObject cms = getCmsObject();        
        echo("Testing touch on a folder (without recursion)");
        touchResources(this, cms, "/folder1/");
    }      
    
    /**
     * Test the touch method on a folder and recusivly on all resources in the folder.<p>
     * 
     * @throws Throwable if something goes wrong
     */    
    public void testTouchFolderRecursive() throws Throwable {
        
        CmsObject cms = getCmsObject();        
        echo("Testing touch on a folder (_with_ recursion)");
        touchResourcesRecursive(this, cms, "/folder2/");    
    }  
}

/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestTouch.java,v $
 * Date   : $Date: 2004/05/28 15:04:59 $
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

import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestResourceFilter;

import java.util.Iterator;
import java.util.List;

/**
 * Unit test for the "touch" method of the CmsObject.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.4 $
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
     * Test the touch method.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testTouchResource() throws Throwable {
        CmsObject cms = setupOpenCms("simpletest", "/sites/default/");
        
        echo("Testing touch on file");
        touchResource(this, cms, "/release/installation.html");   

        echo("Testing touch on a folder (without recursion)");
        touchResources(this, cms, "/tree/folder1/");
        
        echo("Testing touch on a folder (_with_ recursion)");
        touchResourcesRecursive(this, cms, "/tree/folder2/");   
        
        removeOpenCms();
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
        cms.touch(resource1, timestamp, false);

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
        cms.touch(resource1, timestamp, false);

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
        cms.touch(resource1, timestamp, true);

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
 
}

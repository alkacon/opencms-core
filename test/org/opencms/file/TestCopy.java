/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestCopy.java,v $
 * Date   : $Date: 2004/06/25 16:36:37 $
 * Version: $Revision: 1.1 $
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

import org.opencms.lock.CmsLock;
import org.opencms.main.I_CmsConstants;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestResourceConfigurableFilter;
import org.opencms.test.OpenCmsTestResourceFilter;

import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for copy operation.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 */
public class TestCopy extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestCopy(String arg0) {
        super(arg0);
    }
    
    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {
        
        TestSuite suite = new TestSuite();
        
        suite.addTest(new TestCopy("testCopySingleResource"));
        suite.addTest(new TestCopy("testCopyFolderNoSiblings"));
        
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
     * Tests the "copy single resource" operation.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCopySingleResource() throws Throwable {

        CmsObject cms = getCmsObject();     
        echo("Testing copy of a file");
        
        String source = "/index.html";
        String destination = "/index_copy.html";
        long timestamp = System.currentTimeMillis();
        
        storeResources(cms, source);        
        cms.copyResource(source, destination);
                
        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_EQUAL);
        
        // project must be current project
        assertProject(cms, destination, cms.getRequestContext().currentProject());
        // state must be "changed"
        assertState(cms, destination, I_CmsConstants.C_STATE_NEW);
        // date last modified must be the date set in the tough operation
        assertDateLastModifiedAfter(cms, destination, timestamp);
        assertDateCreatedAfter(cms, destination, timestamp);
        // the user last modified must be the current user
        assertUserLastModified(cms, destination, cms.getRequestContext().currentUser());
        assertUserCreated(cms, destination, cms.getRequestContext().currentUser());
        // assert lock state
        assertLock(cms, destination, CmsLock.C_TYPE_EXCLUSIVE);
        // now assert the filter for the rest of the attributes        
        setMapping(destination, source);        
        assertFilter(cms, destination, OpenCmsTestResourceFilter.FILTER_COPY);       
    }  
    
    /**
     * Tests the "copy a folder (no siblings)" operation.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCopyFolderNoSiblings() throws Throwable {

        CmsObject cms = getCmsObject();     
        echo("Testing copy of a folder (no siblings)");
        
        String source = "/folder2/";
        String destination = "/folder2_copy/";
        long timestamp = System.currentTimeMillis();
        
        storeResources(cms, source);        
        cms.copyResource(source, destination, I_CmsConstants.C_COPY_AS_NEW);

        List subresources;
        Iterator i;
        
        subresources = getSubtree(cms, source);
        
        // iterate through the subresources
        i = subresources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            String resName = cms.readAbsolutePath(res, CmsResourceFilter.ALL);
            assertFilter(cms, resName, OpenCmsTestResourceFilter.FILTER_EQUAL);
        }     
        
        subresources = getSubtree(cms, destination);
        setMapping(destination, source);    
        
        // prepare filter without sibling count
        OpenCmsTestResourceConfigurableFilter filter =
            new OpenCmsTestResourceConfigurableFilter(OpenCmsTestResourceFilter.FILTER_COPY);

        filter.disableSiblingCountTest();        
        
        // iterate through the subresources
        i = subresources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            String resName = cms.readAbsolutePath(res, CmsResourceFilter.ALL);
            
            // project must be current project
            assertProject(cms, resName, cms.getRequestContext().currentProject());
            // state must be "changed"
            assertState(cms, resName, I_CmsConstants.C_STATE_NEW);
            // date last modified must be the date set in the tough operation
            assertDateLastModifiedAfter(cms, resName, timestamp);
            assertDateCreatedAfter(cms, resName, timestamp);
            // the user last modified must be the current user
            assertUserLastModified(cms, resName, cms.getRequestContext().currentUser());
            assertUserCreated(cms, resName, cms.getRequestContext().currentUser());
            // assert lock state
            assertLock(cms, resName);
            // must have sibling count of 1
            assertSiblingCount(cms, resName, 1);
            // now assert the filter for the rest of the attributes            
            assertFilter(cms, resName, filter);  
        }                        
    }      
}

/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestCopy.java,v $
 * Date   : $Date: 2005/06/23 11:11:44 $
 * Version: $Revision: 1.13 $
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

import org.opencms.lock.CmsLock;
import org.opencms.main.I_CmsConstants;
import org.opencms.test.OpenCmsTestProperties;
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
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.13 $
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
        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        
        TestSuite suite = new TestSuite();
        suite.setName(TestCopy.class.getName());
                
        suite.addTest(new TestCopy("testCopySingleResourceAsNew"));
        suite.addTest(new TestCopy("testCopyFolderAsNew"));
        suite.addTest(new TestCopy("testCopyOverwriteDeletedFile"));
        
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
     * Tests the "copy single resource as new" operation.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCopySingleResourceAsNew() throws Throwable {

        CmsObject cms = getCmsObject();     
        echo("Testing copy of a file as new");
        
        String source = "/index.html";
        String destination = "/index_copy.html";
        long timestamp = System.currentTimeMillis();
        
        storeResources(cms, source);        
        cms.copyResource(source, destination);
                
        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_EQUAL);
        
        // project must be current project
        assertProject(cms, destination, cms.getRequestContext().currentProject());
        // state must be "new"
        assertState(cms, destination, I_CmsConstants.C_STATE_NEW);
        // date created must be new
        assertDateCreatedAfter(cms, destination, timestamp);
        // user created must be current user
        assertUserCreated(cms, source, cms.getRequestContext().currentUser());
        // assert lock state
        assertLock(cms, destination, CmsLock.C_TYPE_EXCLUSIVE);
        // now assert the filter for the rest of the attributes        
        setMapping(destination, source);        
        assertFilter(cms, destination, OpenCmsTestResourceFilter.FILTER_COPY_AS_NEW);       
    }  
    
    /**
     * Tests the to copy a single resource to a destination that already exists but is
     * marked as deleted.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCopyOverwriteDeletedFile() throws Throwable {

        CmsObject cms = getCmsObject();     
        echo("Testing overwriting a deleted file");
        
        String source1 = "/folder1/page2.html";
        String source2 = "/folder1/image1.gif";
        String source3 = "/folder1/page3.html";
        String destination = "/folder1/page1.html";
           
        storeResources(cms, source1);   
        storeResources(cms, source2);   
        storeResources(cms, source3);   
        storeResources(cms, destination);   
        
        cms.lockResource(destination);
        
        // delete and owerwrite with a sibling of source 1
        cms.deleteResource(destination, I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);   
        assertState(cms, destination, I_CmsConstants.C_STATE_DELETED);
        
        cms.copyResource(source1, destination, I_CmsConstants.C_COPY_AS_SIBLING); 
        
        assertState(cms, destination, I_CmsConstants.C_STATE_CHANGED);
        assertSiblingCount(cms, destination, 2);
        assertLock(cms, destination, CmsLock.C_TYPE_EXCLUSIVE);
        
        assertSiblingCountIncremented(cms, source1, 1);
        assertLock(cms, source1, CmsLock.C_TYPE_SHARED_EXCLUSIVE);
        
        assertFilter(cms, source1, OpenCmsTestResourceFilter.FILTER_EXISTING_SIBLING);        
        assertFilter(cms, source1, destination, OpenCmsTestResourceFilter.FILTER_COPY_SOURCE_DESTINATION_AS_SIBLING);
        
        
        // delete again and owerwrite with a sibling of source 2
        cms.deleteResource(destination, I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
        assertState(cms, destination, I_CmsConstants.C_STATE_DELETED);

        cms.copyResource(source2, destination, I_CmsConstants.C_COPY_AS_SIBLING); 

        assertSiblingCountIncremented(cms, source1, 0);
        assertLock(cms, source1, CmsLock.C_TYPE_UNLOCKED);
        
        assertState(cms, destination, I_CmsConstants.C_STATE_CHANGED);
        assertSiblingCount(cms, destination, 2);
        assertLock(cms, destination, CmsLock.C_TYPE_EXCLUSIVE);
        
        assertSiblingCountIncremented(cms, source2, 1);
        assertLock(cms, source2, CmsLock.C_TYPE_SHARED_EXCLUSIVE);
        
        assertFilter(cms, source1, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES);        
        assertFilter(cms, source2, OpenCmsTestResourceFilter.FILTER_EXISTING_SIBLING);        
        assertFilter(cms, source2, destination, OpenCmsTestResourceFilter.FILTER_COPY_SOURCE_DESTINATION_AS_SIBLING);

        cms.deleteResource(destination, I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
        assertState(cms, destination, I_CmsConstants.C_STATE_DELETED);

        // delete yet again and overwrite with content of source 3 (not a sibling)
        cms.copyResource(source3, destination, I_CmsConstants.C_COPY_AS_NEW); 

        assertSiblingCountIncremented(cms, source1, 0);
        assertLock(cms, source1, CmsLock.C_TYPE_UNLOCKED);
        assertSiblingCountIncremented(cms, source2, 0);
        assertLock(cms, source2, CmsLock.C_TYPE_UNLOCKED);        
        
        assertState(cms, destination, I_CmsConstants.C_STATE_CHANGED);
        assertSiblingCount(cms, destination, 1);
        assertSiblingCount(cms, source3, 1);
        assertLock(cms, destination, CmsLock.C_TYPE_EXCLUSIVE);
        
        assertFilter(cms, source1, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES);        
        assertFilter(cms, source2, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES);        
        assertFilter(cms, source3, OpenCmsTestResourceFilter.FILTER_EQUAL);        
        assertFilter(cms, source3, destination, OpenCmsTestResourceFilter.FILTER_COPY_AS_NEW);            
    }  
        
    /**
     * Tests the "copy a folder as new" operation.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCopyFolderAsNew() throws Throwable {

        CmsObject cms = getCmsObject();     
        echo("Testing copy of a folder as new (i.e. no siblings)");
        
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
            String resName = cms.getSitePath(res);
            assertFilter(cms, resName, OpenCmsTestResourceFilter.FILTER_EQUAL);
        }     
        
        subresources = getSubtree(cms, destination);
        setMapping(destination, source);    
        
        // prepare filter without sibling count
        OpenCmsTestResourceConfigurableFilter filter =
            new OpenCmsTestResourceConfigurableFilter(OpenCmsTestResourceFilter.FILTER_COPY_AS_NEW);

        filter.disableSiblingCountTest();        
        
        // iterate through the subresources
        i = subresources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            String resName = cms.getSitePath(res);
            
            // project must be current project
            assertProject(cms, resName, cms.getRequestContext().currentProject());
            // state must be "new"
            assertState(cms, resName, I_CmsConstants.C_STATE_NEW);
            // date created must be new
            assertDateCreatedAfter(cms, destination, timestamp);
            // user created must be current user
            assertUserCreated(cms, source, cms.getRequestContext().currentUser());
            // assert lock state
            assertLock(cms, resName);
            // must have sibling count of 1
            assertSiblingCount(cms, resName, 1);
            // now assert the filter for the rest of the attributes            
            assertFilter(cms, resName, filter);  
        }                        
    }      
}

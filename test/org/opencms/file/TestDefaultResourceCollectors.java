/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestDefaultResourceCollectors.java,v $
 * Date   : $Date: 2005/01/11 15:09:02 $
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

import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.CmsException;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the default resource collectors.<p>
 */
public class TestDefaultResourceCollectors extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestDefaultResourceCollectors(String arg0) {
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
        suite.setName(TestResourceOperations.class.getName());

        suite.addTest(new TestDefaultResourceCollectors("testCollectSingleFile"));
        suite.addTest(new TestDefaultResourceCollectors("testCollectAllInFolder"));
        suite.addTest(new TestDefaultResourceCollectors("testCollectAllInFolderDateReleasedDesc"));
        
        TestSetup wrapper = new TestSetup(suite) {
            
            protected void setUp() {
                CmsObject cms = setupOpenCms(null, null, false);
                try {
                    initResources(cms);
                } catch (CmsException exc) {
                    fail(exc.getMessage());
                }  
            }
            
            protected void tearDown() {
                removeOpenCms();
            }
        };
        
        return wrapper;
    }     

    /**
     * Initializes the resources needed for the tests.<p>
     * 
     * @param cms the cms object
     * @throws CmsException if something goes wrong
     */
    public static void initResources(CmsObject cms) throws CmsException {
    
        // create a file in the root directory
        cms.createResource("/file1", CmsResourceTypePlain.C_RESOURCE_TYPE_ID, null, null);
        
        // create a folder in the root directory
        cms.createResource("/folder1", CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);
        
        // create a file in the folder directory
        cms.createResource("/folder1/file1", CmsResourceTypePlain.C_RESOURCE_TYPE_ID, null, null);

        // create a file in the folder directory
        cms.createResource("/folder1/file2", CmsResourceTypePlain.C_RESOURCE_TYPE_ID, null, null);  
    }
    
    /**
     * Tests the "singleFile" resource collector.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCollectSingleFile() throws Throwable {
        
        CmsObject cms = getCmsObject();     
        echo("Testing singleFile resource collector");
        
        I_CmsResourceCollector collector = new CmsDefaultResourceCollector();
        List resources = collector.getResults(cms, "singleFile", "/file1");
        
        CmsResource res = (CmsResource)resources.get(0);
        assertEquals("/sites/default/file1", res.getRootPath());
    }

    /**
     * Tests the "allInFolder" resource collector.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCollectAllInFolder() throws Throwable {
        
        CmsObject cms = getCmsObject();     
        echo("Testing allInFolder resource collector");
        
        I_CmsResourceCollector collector = new CmsDefaultResourceCollector();
        List resources = collector.getResults(cms, "allInFolder", "/folder1/|3");
        
        CmsResource res;
        
        // order descending determined by root path
        
        res = (CmsResource)resources.get(0);
        assertEquals("/sites/default/folder1/file2", res.getRootPath());
        
        res = (CmsResource)resources.get(1);
        assertEquals("/sites/default/folder1/file1", res.getRootPath());        
    }
    
    /**
     * Tests the "allInFolderDateReleasedDesc" resource collector.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCollectAllInFolderDateReleasedDesc() throws Throwable {
        
        CmsObject cms = getCmsObject();     
        echo("Testing allInFolderDateReleasedDesc resource collector");
        
        I_CmsResourceCollector collector = new CmsDefaultResourceCollector();
        List resources;
        
        CmsResource res;
        
        long day = 1000L * 60L * 60L * 24L;
        long t1 = System.currentTimeMillis()-2*day, t2 = t1+day;
        
        cms.touch("/folder1/file1", t1, t1, t1+3*day, false);
        cms.touch("/folder1/file2", t2, t2, t2+3*day, false);
        
        resources = collector.getResults(cms, "allInFolderDateReleasedDesc", "/folder1/|3");
        
        res = (CmsResource)resources.get(0);
        assertEquals("/sites/default/folder1/file2", res.getRootPath());
        
        res = (CmsResource)resources.get(1);
        assertEquals("/sites/default/folder1/file1", res.getRootPath());

        cms.touch("/folder1/file1", t2, t2, t2+3*day, false);
        cms.touch("/folder1/file2", t1, t1, t1+3*day, false);
        
        resources = collector.getResults(cms, "allInFolderDateReleasedDesc", "/folder1/|3");
        
        res = (CmsResource)resources.get(0);
        assertEquals("/sites/default/folder1/file1", res.getRootPath());
        
        res = (CmsResource)resources.get(1);
        assertEquals("/sites/default/folder1/file2", res.getRootPath());        
    }
}

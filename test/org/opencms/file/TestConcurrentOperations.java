/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestConcurrentOperations.java,v $
 * Date   : $Date: 2006/08/10 15:00:49 $
 * Version: $Revision: 1.1.4.2 $
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
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsThreadedTestCase;
import org.opencms.test.OpenCmsThreadedTestCaseSuite;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for concurrent operations of the CmsObject.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.1.4.2 $
 */
public class TestConcurrentOperations extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestConcurrentOperations(String arg0) {
        
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
        suite.setName(TestConcurrentOperations.class.getName());
                
        suite.addTest(new TestConcurrentOperations("testConcurrentCreationIssue"));
               
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
     * Concurrent creation test method.<p>
     * 
     * @param cms the OpenCms user context to use
     * @param count the count for this test
     * @param value used to pass a counter value between the councurrent tests
     * 
     * @throws Exception in case the resource could not be created (which is expected)
     */
    public void doConcurrentCreationOperation(CmsObject cms, Integer count, Integer[] value) throws Exception {

        System.out.println("Running doConcurrentCreationOperation() method call - count: " + count.intValue());
        int val = value[0].intValue();
        String name = "/testfolder/sub1/sub2/sub3/subtestfolder" + val;
        CmsResource res = cms.createResource(name, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        value[0] = new Integer(++val);
        System.out.println("++++++++++++++++++ Finished creation of folder " + res.getRootPath() + " - count: " + count.intValue());
    }

    /**
     * Tests concurrent creation a resource with the same name.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testConcurrentCreationIssue() throws Exception {

        int count = 50;
        echo("Concurrent folder creation test: Testing concurrent folder creation with " + count + " threads");

        CmsObject cms = getCmsObject();

        cms.createResource("/testfolder/", CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.createResource("/testfolder/sub1/", CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.createResource("/testfolder/sub1/sub2/", CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.createResource("/testfolder/sub1/sub2/sub3/", CmsResourceTypeFolder.RESOURCE_TYPE_ID);

        String name = "doConcurrentCreationOperation";
        Integer[] value = new Integer[1];
        value[0] = new Integer(0);

        Object[] parameters = new Object[] {
            OpenCmsThreadedTestCaseSuite.PARAM_CMSOBJECT,
            OpenCmsThreadedTestCaseSuite.PARAM_COUNTER,
            value};
        OpenCmsThreadedTestCaseSuite suite = new OpenCmsThreadedTestCaseSuite(count, this, name, parameters);                
        OpenCmsThreadedTestCase[] threads = suite.run();
        
        if (suite.getThrowable() != null) {
            throw new Exception(suite.getThrowable());
        }

        int c = 0;
        int ec = 0;
        for (int i = 0; i < count; i++) {
            Throwable e = threads[i].getThrowable();
            if (e instanceof CmsVfsException) {
                // an exception was thrown, so this thread can not have created a resource
                c++;
            }
            if (e instanceof CmsVfsResourceAlreadyExistsException) {
                CmsVfsResourceAlreadyExistsException e2 = (CmsVfsResourceAlreadyExistsException)e;
                if (e2.getMessageContainer().getKey() == org.opencms.db.generic.Messages.ERR_RESOURCE_WITH_NAME_CURRENTLY_CREATED_1) {
                    // this is the expected "concurrent creation" exception
                    ec++;
                } else {
                    // also check the cause, may be nested
                    e = e2.getCause();                
                    if (e instanceof CmsVfsResourceAlreadyExistsException) {
                        e2 = (CmsVfsResourceAlreadyExistsException)e;
                        if (e2.getMessageContainer().getKey() == org.opencms.db.generic.Messages.ERR_RESOURCE_WITH_NAME_CURRENTLY_CREATED_1) {
                            // this is the expected "concurrent creation" exception
                            ec++;
                        }
                    }
                }
            }
        }

        // now read all resources in the folder and check if duplicates have been created
        List resources = cms.readResources("/testfolder/sub1/sub2/sub3/", CmsResourceFilter.ALL);
        Iterator i = resources.iterator();
        Set names = new HashSet();
        List duplicates = new ArrayList();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            if (names.contains(res.getRootPath())) {
                // found a duplicate name
                duplicates.add(res);
            } else {
                // this is no duplicate
                names.add(res.getRootPath());
            }
        }
        i = duplicates.iterator();
        int c2 = 0;
        while (i.hasNext()) {
            // ouput duplicate list to console
            c2++;
            CmsResource res = (CmsResource)i.next();
            System.err.println("Duplicate resource " + c2 + " : " + res.getRootPath() + " - " + res.getStructureId());
        }
        if (duplicates.size() > 0) {
            // ducplicates where found
            fail("There where " + duplicates.size() + " duplicate resources created");
        }
        if (c != (count - value[0].intValue())) {
            // not the right number of exception where thrown
            fail("Exception count " + c + " id not return the expected result " + (count - value[0].intValue()));
        }
        if (ec == 0) {
            // the "concurrent creation" exception was not thrown once - this must be an error
            fail("Did not catch expected concurrent creation exception at least once");
        }
        echo("Concurrent folder creation test success: No duplicates created - "
            + ec
            + " concurrent modification exceptions caught");
        echo("Total runtime of concurrent test suite: " + CmsStringUtil.formatRuntime(suite.getRuntime()));
    }
}
/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/Attic/TestAvailable.java,v $
 * Date   : $Date: 2004/12/14 09:11:15 $
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

import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

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
public class TestAvailable extends OpenCmsTestCase {
  
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestAvailable(String arg0) {
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
        suite.setName(TestAvailable.class.getName());
                
        suite.addTest(new TestAvailable("testAvailableForExistingFile"));
        suite.addTest(new TestAvailable("testAvailableForUnexistingFile"));
        suite.addTest(new TestAvailable("testAvailableForUnauthorizedFile"));
        
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
     * Tests the availability of a file that exists and with proper permissions.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testAvailableForExistingFile() throws Throwable {

        CmsObject cms = getCmsObject();     
        echo("Testing the availability of a file that exists and with proper permissions");
        String filename = "index.html";
        
        assertEquals(true, cms.availableResource(filename));
    }  
    
    /**
     * Tests the availability of a file that does not exist.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testAvailableForUnexistingFile() throws Throwable {

        CmsObject cms = getCmsObject();     
        echo("Testing the availability of a file that does not exist");
        String filename = "xxx.yyy";
        
        assertEquals(false, cms.availableResource(filename));
    }  
    
    /**
     * Tests the availability of a file that exists but with not enough permissions.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testAvailableForUnauthorizedFile() throws Throwable {

        CmsObject cms = getCmsObject();     

        echo("Testing the availability of a file that exists but with not enough permissions");

        cms.createGroup("Testgroup", "A test group", 0, null);
        CmsGroup testGroup = cms.readGroup("Testgroup");
        cms.addUser("testuser", "test", testGroup.getName(), "A test user", null);
        CmsUser testUser = cms.readUser("testuser");

        String resName = "index.html";

        cms.lockResource(resName);
        cms.chacc(resName, I_CmsPrincipal.C_PRINCIPAL_GROUP, testGroup.getName(), "-r-w-v-c-i");
        cms.chacc(resName, I_CmsPrincipal.C_PRINCIPAL_USER, testUser.getName(), "-r-w-v-c-i");
        cms.unlockResource(resName);
        cms.publishProject(); 
        
        cms.loginUser("testuser", "test");
        assertEquals(false, cms.availableResource(resName));
    }  
    
}

/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/security/Attic/TestSetResetPassword.java,v $
 * Date   : $Date: 2005/02/17 12:46:01 $
 * Version: $Revision: 1.5 $
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
 
package org.opencms.security;

import org.opencms.file.CmsObject;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestCase;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Comment for <code>TestSetResetPassword</code>.<p>
 */
public class TestSetResetPassword extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestSetResetPassword(String arg0) {
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
        suite.setName(TestSetResetPassword.class.getName());
        
        suite.addTest(new TestSetResetPassword("testSetResetPassword"));
         
        TestSetup wrapper = new TestSetup(suite) {
            
            protected void setUp() {
                setupOpenCms(null, "/sites/default/", false);
            }
            
            protected void tearDown() {
                removeOpenCms();
            }
        };
        
        return wrapper;
   }
    
    /**
     * Tests the setPassword and resetPassword methods.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testSetResetPassword() throws Throwable {
        
        CmsObject cms = getCmsObject();     
        echo("Testing setting the password as admin");
        
        // change password of admin as guest
        cms.setPassword("Admin", "admin", "password1");
        
        // change password as admin
        cms.loginUser("Admin", "password1");
        cms.setPassword("Admin", "password2");
        
        // change password again, using the old password
        cms.loginUser("Admin", "password2");
        cms.setPassword("Admin", "password2", "admin");
        
        // check if the password was changed
        cms.loginUser("Admin", "admin");           
    }
}

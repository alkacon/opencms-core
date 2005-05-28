/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/security/Attic/TestPasswordHandler.java,v $
 * Date   : $Date: 2005/05/28 17:17:17 $
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
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestCase;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Comment for <code>TestPasswordHandler</code>.<p>
 */
public class TestPasswordHandler extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestPasswordHandler(String arg0) {
        super(arg0);
    }
    
    /**
     * Test suite for this test class.<p>
     * Setup is done without importing vfs data.
     * 
     * @return the test suite
     */
    public static Test suite() {
        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        
        TestSuite suite = new TestSuite();
        suite.setName(TestPasswordHandler.class.getName());

        suite.addTest(new TestPasswordHandler("testPasswordValidation"));
        suite.addTest(new TestPasswordHandler("testLoginUser"));
        
        TestSetup wrapper = new TestSetup(suite) {
            
            protected void setUp() {
                setupOpenCms(null, null, false);
            }
            
            protected void tearDown() {
                removeOpenCms();
            }
        };
        
        return wrapper;
    } 
    
    /**
     * Tests logging in as a user (checking for different kind of exceptions).<p>
     * 
     * @throws Exception if the test fails
     */
    public void testLoginUser() throws Exception {
        
        echo("Testing Exception behaviour during login");        
        
        // this will be initialized as "Admin"
        CmsObject cms = getCmsObject();
        
        String adminUser = OpenCms.getDefaultUsers().getUserAdmin();
        
        // stupid test to just make sure everything is set up correctly
        cms.loginUser(adminUser, "admin");
        
        CmsException error = null;
        try {
            // try to login with a valid username but a wrong password
            cms.loginUser(adminUser, "imamwrong");
        } catch (CmsSecurityException e) {
            error = e;
        }        
        assertNotNull(error);
        assertSame(Messages.ERR_SECURITY_LOGIN_FAILED_1, error.getMessageContainer().getKey());
        
        error = null;
        try {
            // try to login with an invlaid username
            cms.loginUser("idontexist", "imnotimportant");
        } catch (CmsSecurityException e) {
            error = e;
        }        
        assertNotNull(error);        
        assertSame(Messages.ERR_SECURITY_LOGIN_FAILED_1, error.getMessageContainer().getKey());
    }
    
    /**
     * Tests the static "validatePassword" method of the password handler.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testPasswordValidation() throws Throwable {
        
        echo("Testing password validation handler");
        
        I_CmsPasswordHandler passwordHandler = OpenCms.getPasswordHandler();
        boolean failure = false;
        
        // passwords must have a minimal length of 4 charaters
        try {
            passwordHandler.validatePassword("1*3");
            failure = true;    
        } catch (CmsSecurityException exc) {
            // noop
        }
        
        if (failure) {
            fail("Invalid password 1*3 validated.");
        }
        
        // try some valid passwords        
        try {
            passwordHandler.validatePassword("zyz*nowski");
        } catch (Exception exc) {
            echo ("zyznowski invalid:" + exc.getMessage());
        } 

        try {
            passwordHandler.validatePassword("Alfa99");
        } catch (Exception exc) {
            echo ("alfa invalid:" + exc.getMessage());
        }
        
        try {
            passwordHandler.validatePassword("ca%Dill");
        } catch (Exception exc) {
            echo ("ferrar invalid:" + exc.getMessage());
        }
        
        try {
            passwordHandler.validatePassword("#ulary");
        } catch (Exception exc) {
            echo ("ulary invalid:" + exc.getMessage());
        }
    }    
}

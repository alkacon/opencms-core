/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/security/TestLoginAndPasswordHandler.java,v $
 * Date   : $Date: 2005/06/05 14:06:36 $
 * Version: $Revision: 1.2 $
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

import org.opencms.db.CmsLoginMessage;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestCase;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/** 
 * Tests login and password related functions.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.2 $
 * 
 * @since 6.0
 */
public class TestLoginAndPasswordHandler extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestLoginAndPasswordHandler(String arg0) {
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
        suite.setName(TestLoginAndPasswordHandler.class.getName());

        suite.addTest(new TestLoginAndPasswordHandler("testLoginUser"));
        suite.addTest(new TestLoginAndPasswordHandler("testLoginMessage"));
        suite.addTest(new TestLoginAndPasswordHandler("testPasswordValidation"));
        suite.addTest(new TestLoginAndPasswordHandler("testSetResetPassword"));
        
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
     * Tests the login message functions.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testLoginMessage() throws Exception {
        
        echo("Testing login messages");    
        
        // this will be initialized as "Admin"
        CmsObject cms = getCmsObject();
        
        String adminUser = OpenCms.getDefaultUsers().getUserAdmin();
        String test1User = "test1";        
        
        // initial the login message must be null
        assertNull(OpenCms.getLoginManager().getLoginMessage());
        
        String message = "This is the test login message";
        
        // check a "blocking" login message
        CmsLoginMessage loginMessage = new CmsLoginMessage(message, true);        
        OpenCms.getLoginManager().setLoginMessage(cms, loginMessage);
        
        CmsException error = null;
        try {
            cms.loginUser(test1User, "test1");
        } catch (CmsAuthentificationException e) {
            error = e;
        }        
        assertNotNull(error);
        assertSame(Messages.ERR_LOGIN_FAILED_WITH_MESSAGE_1, error.getMessageContainer().getKey());
        assertTrue(error.getMessage().indexOf(message) > 0);
        
        cms.loginUser(adminUser, "admin");
        
        // remove message and try again
        OpenCms.getLoginManager().removeLoginMessage(cms);
        cms.loginUser(test1User, "test1");

        cms.loginUser(adminUser, "admin");
        
        // check a "non blocking" login message
        loginMessage = new CmsLoginMessage(message, false);        
        OpenCms.getLoginManager().setLoginMessage(cms, loginMessage);        
        cms.loginUser(test1User, "test1");
        
        cms.loginUser(adminUser, "admin");

        // check an expired login message
        loginMessage = new CmsLoginMessage(0, System.currentTimeMillis(), message, true);        
        OpenCms.getLoginManager().setLoginMessage(cms, loginMessage);        
        cms.loginUser(test1User, "test1");        
        
        cms.loginUser(adminUser, "admin");
        
        // check a login message in the far future
        loginMessage = new CmsLoginMessage(System.currentTimeMillis() + 100000, Long.MAX_VALUE, message, true);        
        OpenCms.getLoginManager().setLoginMessage(cms, loginMessage);        
        cms.loginUser(test1User, "test1");           
        
        cms.loginUser(adminUser, "admin");
        loginMessage = new CmsLoginMessage(message, true);  
        OpenCms.getLoginManager().setLoginMessage(cms, loginMessage); 
        error = null;
        try {
            cms.loginUser(test1User, "test1");
        } catch (CmsAuthentificationException e) {
            error = e;
        }        
        assertNotNull(error);
        assertSame(Messages.ERR_LOGIN_FAILED_WITH_MESSAGE_1, error.getMessageContainer().getKey());
        assertTrue(error.getMessage().indexOf(message) > 0);
        
        cms.loginUser(adminUser, "admin");
        OpenCms.getLoginManager().removeLoginMessage(cms);        
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
        assertEquals(adminUser, cms.getRequestContext().currentUser().getName());
        
        CmsException error = null;
        try {
            // try to login with a valid username but a wrong password
            cms.loginUser(adminUser, "imamwrong");
        } catch (CmsAuthentificationException e) {
            error = e;
        }        
        assertNotNull(error);
        assertSame(Messages.ERR_LOGIN_FAILED_3, error.getMessageContainer().getKey());
        
        error = null;
        try {
            // try to login with an invlaid username
            cms.loginUser("idontexist", "imnotimportant");
        } catch (CmsAuthentificationException e) {
            error = e;
        }        
        assertNotNull(error);        
        assertSame(Messages.ERR_LOGIN_FAILED_NO_USER_3, error.getMessageContainer().getKey());
        
        String test1User = "test1";
        // now try a different user
        cms.loginUser(test1User, "test1");
        assertEquals(test1User, cms.getRequestContext().currentUser().getName());
        
        // back to admin (to change the test1 user)
        cms.loginUser(adminUser, "admin");
        assertEquals(adminUser, cms.getRequestContext().currentUser().getName());
        
        // disable the test1 user
        CmsUser test1 = cms.readUser(test1User);
        test1.setDisabled();
        cms.writeUser(test1);
        
        error = null;
        try {
            // try to login with an invlaid username
            cms.loginUser(test1User, "test1");
        } catch (CmsAuthentificationException e) {
            error = e;
        }        
        assertNotNull(error);        
        assertSame(Messages.ERR_LOGIN_FAILED_DISABLED_3, error.getMessageContainer().getKey());
        
        // enable the test1 user again
        test1.setEnabled();
        cms.writeUser(test1);
        
        // try again to login
        cms.loginUser(test1User, "test1");
        assertEquals(test1User, cms.getRequestContext().currentUser().getName());        
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
    
    /**
     * Tests the setPassword and resetPassword methods.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testSetResetPassword() throws Throwable {
        
        echo("Testing setting the password as admin");
        CmsObject cms = getCmsObject();     
        
        // change password of admin
        cms.setPassword("Admin", "admin", "password1");
        
        // login with the new password
        cms.loginUser("Admin", "password1");
        
        // change password again        
        cms.setPassword("Admin", "password2");
        
        // login with the new password
        cms.loginUser("Admin", "password2");
        
        // change password again, this time with the old password        
        cms.setPassword("Admin", "password2", "admin");
        
        // check if the password was changed
        cms.loginUser("Admin", "admin");           
    }
}

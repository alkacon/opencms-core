/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import static com.lambdaworks.codec.Base64.decode;
import static org.junit.Assert.assertNotEquals;

import org.opencms.db.CmsLoginMessage;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import com.lambdaworks.crypto.SCryptUtil;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests login and password related functions.<p>
 *
 *
 * @since 6.0
 */
public class TestLoginAndPasswordHandler extends OpenCmsTestCase {

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestLoginAndPasswordHandler.class.getName());

        suite.addTest(new TestLoginAndPasswordHandler("testSCrypt"));
        suite.addTest(new TestLoginAndPasswordHandler("testUserDefaultPasswords"));
        suite.addTest(new TestLoginAndPasswordHandler("testCheckPasswordDigest"));
        suite.addTest(new TestLoginAndPasswordHandler("testPasswordConvesion"));
        suite.addTest(new TestLoginAndPasswordHandler("testLoginUser"));
        suite.addTest(new TestLoginAndPasswordHandler("testLoginMessage"));
        suite.addTest(new TestLoginAndPasswordHandler("testPasswordValidation"));
        suite.addTest(new TestLoginAndPasswordHandler("testSetResetPassword"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestLoginAndPasswordHandler(String arg0) {

        super(arg0);
    }

    /**
     * Tests if the password is digested and stored correctly.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testCheckPasswordDigest() throws Throwable {

        echo("Testing if the password is digested and stored correctly");
        String adminUsername = OpenCms.getDefaultUsers().getUserAdmin();

        CmsObject cms = getCmsObject();

        // change password of admin
        String newPassword = "theNewPassword01";
        String newPasswordDigested = OpenCms.getPasswordHandler().digest(newPassword);
        cms.setPassword("Admin", "admin", newPassword);

        CmsUser adminUser = cms.readUser(adminUsername);
        String adminUserPassword = adminUser.getPassword();

        // change password back, otherwise further tests would fail
        cms.setPassword(adminUsername, newPassword, "admin");

        echo("Digested password: " + newPasswordDigested);
        echo("User password    : " + adminUserPassword);

        assertTrue(
            "Passwords do not validate",
            OpenCms.getPasswordHandler().checkPassword(newPassword, newPasswordDigested, false));
        assertEquals(
            "Password length for Admin user not equal to expected digested password length",
            adminUserPassword.length(),
            newPasswordDigested.length());
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
        if (error != null) {
            assertSame(Messages.ERR_LOGIN_FAILED_WITH_MESSAGE_1, error.getMessageContainer().getKey());
            assertTrue(error.getMessage().indexOf(message) > 0);
        }

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
        if (error != null) {
            assertSame(Messages.ERR_LOGIN_FAILED_WITH_MESSAGE_1, error.getMessageContainer().getKey());
            assertTrue(error.getMessage().indexOf(message) > 0);
        }

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
        assertTrue(OpenCms.getDefaultUsers().isUserAdmin(cms.getRequestContext().getCurrentUser().getName()));

        CmsException error = null;
        try {
            // try to login with a valid username but a wrong password
            cms.loginUser(adminUser, "imamwrong");
        } catch (CmsAuthentificationException e) {
            error = e;
        }
        assertNotNull(error);
        if (error != null) {
            assertSame(Messages.ERR_LOGIN_FAILED_2, error.getMessageContainer().getKey());
        }

        error = null;
        try {
            // try to login with an invlaid username
            cms.loginUser("idontexist", "imnotimportant");
        } catch (CmsAuthentificationException e) {
            error = e;
        }
        assertNotNull(error);
        if (error != null) {
            assertSame(Messages.ERR_LOGIN_FAILED_NO_USER_2, error.getMessageContainer().getKey());
        }

        String test1User = "test1";
        // now try a different user
        cms.loginUser(test1User, "test1");
        assertEquals(test1User, cms.getRequestContext().getCurrentUser().getName());

        // back to admin (to change the test1 user)
        cms.loginUser(adminUser, "admin");
        assertEquals(adminUser, cms.getRequestContext().getCurrentUser().getName());

        // disable the test1 user
        CmsUser test1 = cms.readUser(test1User);
        test1.setEnabled(false);
        cms.writeUser(test1);

        error = null;
        try {
            // try to login with an invalid username
            cms.loginUser(test1User, "test1");
        } catch (CmsAuthentificationException e) {
            error = e;
        }
        assertNotNull(error);
        if (error != null) {
            assertSame(Messages.ERR_LOGIN_FAILED_DISABLED_2, error.getMessageContainer().getKey());
        }

        // enable the test1 user again
        test1.setEnabled(true);
        cms.writeUser(test1);

        // try again to login
        cms.loginUser(test1User, "test1");
        assertEquals(test1User, cms.getRequestContext().getCurrentUser().getName());
    }

    /**
     * Tests if the password is automatically converted from the old to the new hash algorithm.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testPasswordConvesion() throws Throwable {

        echo("Testing if the password is automatically converted from the old to the new hash algorithm");
        String testData = "test1";

        CmsObject cms = getCmsObject();
        CmsUser testUser = cms.readUser(testData);

        // because of old setup data, this should be MD5 encoded but the new standard is SCRYPT
        echo("Old stored password hash: " + testUser.getPassword());
        assertEquals(
            "Password of user 'test1' not as expected",
            testUser.getPassword(),
            OpenCms.getPasswordHandler().digest(
                testData,
                I_CmsPasswordHandler.DIGEST_TYPE_MD5,
                CmsEncoder.ENCODING_UTF_8));

        // now login the user, this should update the password to the new hash algorithm
        cms.loginUser(testData, testData);
        testUser = cms.readUser(testData);

        echo("New stored password hash: " + testUser.getPassword());
        assertTrue(
            "Password validation with new hash algorithm failed",
            OpenCms.getPasswordHandler().checkPassword(testData, testUser.getPassword(), false));
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
            echo("zyznowski invalid:" + exc.getMessage());
        }

        try {
            passwordHandler.validatePassword("Alfa99");
        } catch (Exception exc) {
            echo("alfa invalid:" + exc.getMessage());
        }

        try {
            passwordHandler.validatePassword("ca%Dill");
        } catch (Exception exc) {
            echo("ferrar invalid:" + exc.getMessage());
        }

        try {
            passwordHandler.validatePassword("#ulary");
        } catch (Exception exc) {
            echo("ulary invalid:" + exc.getMessage());
        }
    }

    /**
     * Tests basic functionality and availability of the SCrypt algorithm.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testSCrypt() throws Throwable {

        // Iteration count
        int demos = 5;

        // Iteration count
        int iterations = 5;

        // Password to use for hash tests
        String pwd = "p\r\nassw0Rd!";

        int N = 16384; // CPU cost
        int r = 8; // Memory cost
        int p = 1; // Parallelization parameter

        // Print out some hashes
        echo("\nCreating " + demos + " demo hashes with SCrpyt:");
        for (int i = 0; i < demos; i++) {
            System.out.println(SCryptUtil.scrypt(pwd, N, r, p));
        }

        // Test password validation
        echo("Testing " + iterations + " hashes with SCrpyt");
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            String password = pwd + i;
            String wrongPassword = pwd + (i + 1);
            String hash = SCryptUtil.scrypt(password, N, r, p);
            String secondHash = SCryptUtil.scrypt(password, N, r, p);
            assertNotEquals("FAILURE: TWO HASHES ARE EQUAL!", hash, secondHash);
            assertFalse("FAILURE: WRONG PASSWORD ACCEPTED!", SCryptUtil.check(wrongPassword, hash));
            assertTrue("FAILURE: GOOD PASSWORD NOT ACCEPTED!", SCryptUtil.check(password, hash));
        }
        echo("Test took " + (System.currentTimeMillis() - startTime) + " msec.");

        if ("scrypt".equals(OpenCms.getPasswordHandler().getDigestType())) {
            // OpenCms configuration tests, SCrypt assumed as configured default
            String hashed = OpenCms.getPasswordHandler().digest(pwd);
            String[] parts = hashed.split("\\$");

            if ((parts.length != 5) || !parts[1].equals("s0")) {
                fail("OpenCms produced an invalid hashed SCrypt value");
            }

            long params = Long.parseLong(parts[2], 16);
            byte[] salt = decode(parts[3].toCharArray());
            byte[] derived = decode(parts[4].toCharArray());

            N = (int)Math.pow(2, (params >> 16) & 0xffff);
            r = ((int)params >> 8) & 0xff;
            p = (int)params & 0xff;

            echo("Parsed SCrpyt digest as N:" + N + " r:" + r + " p:" + p + " salt:" + salt + " derived:" + derived);

            assertEquals("Unexpected SCrypt value for N", 8192, N);
            assertEquals("Unexpected SCrypt value for r", 4, r);
            assertEquals("Unexpected SCrypt value for p", 2, p);

        } else {
            fail("Expected SCrypt algorithm not configured as password digester");
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
        String adminUsername = OpenCms.getDefaultUsers().getUserAdmin();

        // change password of admin
        cms.setPassword(adminUsername, "admin", "password1");

        // login with the new password
        cms.loginUser(adminUsername, "password1");

        // change password again
        cms.setPassword(adminUsername, "password2");

        // login with the new password
        cms.loginUser(adminUsername, "password2");

        // change password back, otherwise further tests would fail
        cms.setPassword(adminUsername, "password2", "admin");

        // verify that the password was changed to the expected default
        cms.loginUser(adminUsername, "admin");
    }

    /**
     * Tests if the user passwords are imported / set correctly.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testUserDefaultPasswords() throws Exception {

        CmsObject cms = getCmsObject();

        echo("Testing the user import.");
        I_CmsPasswordHandler passwordHandler = OpenCms.getPasswordHandler();
        CmsUser user;

        // check if passwords will be converted
        echo("Testing passwords of imported users");

        // check the admin user
        user = cms.readUser(OpenCms.getDefaultUsers().getUserAdmin());
        assertTrue(
            "Admin user password does not check",
            passwordHandler.checkPassword("admin", user.getPassword(), false));

        // check the guest user
        user = cms.readUser(OpenCms.getDefaultUsers().getUserGuest());
        assertFalse(
            "Guest user password does check with old default (empty String) but should fail becasue it is now a random UUID",
            passwordHandler.checkPassword("", user.getPassword(), true));
        try {
            SCryptUtil.check("{random-value}", user.getPassword());
        } catch (IllegalArgumentException e) {
            fail("Guest user password not a valid SCrypt password");
        }

        // check the export user
        user = cms.readUser(OpenCms.getDefaultUsers().getUserExport());
        try {
            SCryptUtil.check("{random-value}", user.getPassword());
        } catch (IllegalArgumentException e) {
            fail("Export user password not a valid SCrypt password");
        }

        // check the deleted resource user
        user = cms.readUser(OpenCms.getDefaultUsers().getUserDeletedResource());
        try {
            // check should fail, but if we get an exception then the password does not have the right format
            SCryptUtil.check("{random-value}", user.getPassword());
        } catch (IllegalArgumentException e) {
            fail("Deleted resource user password not a valid SCrypt password");
        }

        // check the test1 user
        user = cms.readUser("test1");
        assertFalse(
            "test1 user password does check with default SCrypt but should fail becasuse it is encoded in MD5",
            passwordHandler.checkPassword("test1", user.getPassword(), false));
        assertTrue(
            "test1 user password does not check with fallback to MD5",
            passwordHandler.checkPassword("test1", user.getPassword(), true));

        // check the test2 user
        user = cms.readUser("test2");
        assertFalse(
            "test2 user password does check with default SCrypt but should fail becasuse it is encoded in MD5",
            passwordHandler.checkPassword("test2", user.getPassword(), false));
        assertTrue(
            "test2 user password does not check with fallback to MD5",
            passwordHandler.checkPassword("test2", user.getPassword(), true));
    }
}

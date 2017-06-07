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

import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.file.history.CmsHistoryPrincipal;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for <code>{@link org.opencms.security.CmsPrincipal}</code> (and it's subclasses).<p>
 */
public class TestCmsPrincipal extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsPrincipal(String arg0) {

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
        suite.setName(TestCmsPrincipal.class.getName());

        suite.addTest(new TestCmsPrincipal("testBasicReadOperation"));
        suite.addTest(new TestCmsPrincipal("testUserHistory"));
        suite.addTest(new TestCmsPrincipal("testGroupHistory"));
        suite.addTest(new TestCmsPrincipal("testPrefixMethods"));

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
     * Tests basic principal read operation.<p>
     *
     * @throws Exception if the test fails
     */
    public void testBasicReadOperation() throws Exception {

        echo("Testing basic principal read operation");
        CmsObject cms = getCmsObject();

        I_CmsPrincipal principal;
        String prefixedName;

        prefixedName = CmsPrincipal.getPrefixedUser(OpenCms.getDefaultUsers().getUserAdmin());
        principal = CmsPrincipal.readPrefixedPrincipal(cms, prefixedName);
        assertTrue(principal.isUser());
        assertFalse(principal.isGroup());
        assertEquals(prefixedName, principal.getPrefixedName());

        prefixedName = CmsPrincipal.getPrefixedGroup(OpenCms.getDefaultUsers().getGroupAdministrators());
        principal = CmsPrincipal.readPrefixedPrincipal(cms, prefixedName);
        assertFalse(principal.isUser());
        assertTrue(principal.isGroup());
        assertEquals(prefixedName, principal.getPrefixedName());

        // negative test
        prefixedName = "kaputt";
        CmsException caught = null;
        try {
            principal = CmsPrincipal.readPrefixedPrincipal(cms, prefixedName);
        } catch (CmsException e) {
            caught = e;
        }
        assertNotNull(caught);
        assertTrue(caught instanceof CmsDbEntryNotFoundException);
        if (caught != null) {
            assertSame(Messages.ERR_INVALID_PRINCIPAL_1, caught.getMessageContainer().getKey());
        }

        // negative test 2
        prefixedName = CmsPrincipal.getPrefixedUser("kaputt");
        caught = null;
        try {
            principal = CmsPrincipal.readPrefixedPrincipal(cms, prefixedName);
        } catch (CmsException e) {
            caught = e;
        }
        assertNotNull(caught);
        assertTrue(caught instanceof CmsDbEntryNotFoundException);
        if (caught != null) {
            assertSame(org.opencms.db.Messages.ERR_READ_USER_FOR_NAME_1, caught.getMessageContainer().getKey());
        }

        // negative test 3
        prefixedName = CmsPrincipal.getPrefixedGroup("kaputt");
        caught = null;
        try {
            principal = CmsPrincipal.readPrefixedPrincipal(cms, prefixedName);
        } catch (CmsException e) {
            caught = e;
        }
        assertNotNull(caught);
        assertTrue(caught instanceof CmsDbEntryNotFoundException);
        if (caught != null) {
            assertSame(org.opencms.db.Messages.ERR_READ_GROUP_FOR_NAME_1, caught.getMessageContainer().getKey());
        }
    }

    /**
     * Test group history.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testGroupHistory() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing group history");

        CmsGroup group = cms.createGroup("groupDelete", "my description", 0, null);
        long before = System.currentTimeMillis();
        cms.deleteGroup(group.getId(), null);
        long after = System.currentTimeMillis();

        CmsHistoryPrincipal histUser = cms.readHistoryPrincipal(group.getId());
        assertEquals(group.getId(), histUser.getId());
        assertEquals(group.getName(), histUser.getName());
        assertEquals(group.getSimpleName(), histUser.getSimpleName());
        assertEquals(group.getOuFqn(), histUser.getOuFqn());
        assertEquals(group.getDescription(), histUser.getDescription());
        assertEquals("-", histUser.getEmail());
        assertEquals(cms.getRequestContext().getCurrentUser().getId(), histUser.getUserDeleted());
        assertTrue(before <= histUser.getDateDeleted());
        assertTrue(histUser.getDateDeleted() <= after);
    }

    /**
     * Tests prefix methods.<p>
     *
     * @throws Exception if the test fails
     */
    public void testPrefixMethods() throws Exception {

        // User checks
        assertTrue("User prefix check with correct user name 1 failed", CmsUser.hasPrefix("USER.hans"));
        assertTrue("User prefix check with correct user name 2 failed", CmsUser.hasPrefix("  USER.hans"));
        assertTrue("User prefix check with correct user name 3 failed", CmsUser.hasPrefix("USER.hans   "));
        assertTrue("User prefix check with correct user name 4 failed", CmsUser.hasPrefix("User.hans   "));
        assertTrue(
            "User prefix removal with correct user name failed",
            CmsUser.removePrefix("USER.hans").equals("hans"));
        assertTrue(
            "User prefix removal with correct user name failed",
            CmsUser.removePrefix("   USER.hans").equals("hans"));
        assertFalse("User prefix check with null failed", CmsUser.hasPrefix(null));
        assertFalse("User prefix check with empty String failed", CmsUser.hasPrefix(""));
        assertFalse("User prefix check with wrong user name 1 failed", CmsUser.hasPrefix("USERhans"));
        assertFalse("User prefix check with wrong user name 2 failed", CmsUser.hasPrefix("USERS.hans"));

        // Group checks
        assertTrue("Group prefix check with correct group name 1 failed", CmsGroup.hasPrefix("GROUP.Users"));
        assertTrue("Group prefix check with correct group name 2 failed", CmsGroup.hasPrefix("  GROUP.Users"));
        assertTrue("Group prefix check with correct group name 3 failed", CmsGroup.hasPrefix("GROUP.Users   "));
        assertTrue("Group prefix check with correct group name 4 failed", CmsGroup.hasPrefix("Group.Users   "));
        assertTrue(
            "Group prefix removal with correct group name failed",
            CmsGroup.removePrefix("GROUP.Users").equals("Users"));
        assertTrue(
            "Group prefix removal with correct group name failed",
            CmsGroup.removePrefix("   GROUP.Users").equals("Users"));
        assertFalse("Group prefix check with null failed", CmsGroup.hasPrefix(null));
        assertFalse("Group prefix check with empty String failed", CmsGroup.hasPrefix(""));
        assertFalse("Group prefix check with wrong group name 1 failed", CmsGroup.hasPrefix("GROUPUsers"));
        assertFalse("Group prefix check with wrong group name 2 failed", CmsGroup.hasPrefix("GROUPS.Users"));
        assertFalse("Group prefix check with wrong group name 3 failed", CmsGroup.hasPrefix("SGROUPS.Users"));

        // Role checks
        assertTrue("Role prefix check with correct role name 1 failed", CmsRole.hasPrefix("ROLE.EDITOR"));
        assertTrue("Role prefix check with correct role name 2 failed", CmsRole.hasPrefix("  ROLE.EDITOR"));
        assertTrue("Role prefix check with correct role name 3 failed", CmsRole.hasPrefix("ROLE.EDITOR   "));
        assertTrue("Role prefix check with correct role name 3 failed", CmsRole.hasPrefix("Role.EDITOR   "));
        assertTrue(
            "Role prefix removal with correct role name failed",
            CmsRole.removePrefix("ROLE.EDITOR").equals("EDITOR"));
        assertTrue(
            "Role prefix removal with correct role name failed",
            CmsRole.removePrefix("   ROLE.EDITOR").equals("EDITOR"));
        assertFalse("Role prefix check with null failed", CmsRole.hasPrefix(null));
        assertFalse("Role prefix check with empty String failed", CmsRole.hasPrefix(""));
        assertFalse("Role prefix check with wrong role name 1 failed", CmsRole.hasPrefix("ROLEEDITOR"));
        assertFalse("Role prefix check with wrong role name 2 failed", CmsRole.hasPrefix("ROLES.EDITOR"));
        assertFalse("Role prefix check with wrong role name 3 failed", CmsRole.hasPrefix("SROLES.EDITOR"));
    }

    /**
     * Test user history.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUserHistory() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing user history");

        CmsUser user = cms.createUser("userDelete", "userDelete", "my description", null);
        user.setEmail("aa@bb.cc");
        cms.writeUser(user);
        long before = System.currentTimeMillis();
        cms.deleteUser(user.getId());
        long after = System.currentTimeMillis();

        CmsHistoryPrincipal histUser = cms.readHistoryPrincipal(user.getId());
        assertEquals(user.getId(), histUser.getId());
        assertEquals(user.getName(), histUser.getName());
        assertEquals(user.getSimpleName(), histUser.getSimpleName());
        assertEquals(user.getOuFqn(), histUser.getOuFqn());
        assertEquals(user.getDescription(), histUser.getDescription());
        assertEquals(user.getEmail(), histUser.getEmail());
        assertEquals(cms.getRequestContext().getCurrentUser().getId(), histUser.getUserDeleted());
        assertTrue(before <= histUser.getDateDeleted());
        assertTrue(histUser.getDateDeleted() <= after);
    }
}

/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
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
import org.opencms.test.OpenCmsTestRunner;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Tests for <code>{@link org.opencms.security.CmsPrincipal}</code> (and it's subclasses).<p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestCmsPrincipal extends OpenCmsTestRunner {

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    /**
     * Tests basic principal read operation.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    @Order(1)
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
    @Test
    @Order(3)
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
    @Test
    @Order(4)
    public void testPrefixMethods() throws Exception {

        // User checks
        assertTrue(CmsUser.hasPrefix("USER.hans"), "User prefix check with correct user name 1 failed");
        assertTrue(CmsUser.hasPrefix("  USER.hans"), "User prefix check with correct user name 2 failed");
        assertTrue(CmsUser.hasPrefix("USER.hans   "), "User prefix check with correct user name 3 failed");
        assertTrue(CmsUser.hasPrefix("User.hans   "), "User prefix check with correct user name 4 failed");
        assertTrue(
            CmsUser.removePrefix("USER.hans").equals("hans"),
            "User prefix removal with correct user name failed");
        assertTrue(
            CmsUser.removePrefix("   USER.hans").equals("hans"),
            "User prefix removal with correct user name failed");
        assertFalse(CmsUser.hasPrefix(null), "User prefix check with null failed");
        assertFalse(CmsUser.hasPrefix(""), "User prefix check with empty String failed");
        assertFalse(CmsUser.hasPrefix("USERhans"), "User prefix check with wrong user name 1 failed");
        assertFalse(CmsUser.hasPrefix("USERS.hans"), "User prefix check with wrong user name 2 failed");

        // Group checks
        assertTrue(CmsGroup.hasPrefix("GROUP.Users"), "Group prefix check with correct group name 1 failed");
        assertTrue(CmsGroup.hasPrefix("  GROUP.Users"), "Group prefix check with correct group name 2 failed");
        assertTrue(CmsGroup.hasPrefix("GROUP.Users   "), "Group prefix check with correct group name 3 failed");
        assertTrue(CmsGroup.hasPrefix("Group.Users   "), "Group prefix check with correct group name 4 failed");
        assertTrue(
            CmsGroup.removePrefix("GROUP.Users").equals("Users"),
            "Group prefix removal with correct group name failed");
        assertTrue(
            CmsGroup.removePrefix("   GROUP.Users").equals("Users"),
            "Group prefix removal with correct group name failed");
        assertFalse(CmsGroup.hasPrefix(null), "Group prefix check with null failed");
        assertFalse(CmsGroup.hasPrefix(""), "Group prefix check with empty String failed");
        assertFalse(CmsGroup.hasPrefix("GROUPUsers"), "Group prefix check with wrong group name 1 failed");
        assertFalse(CmsGroup.hasPrefix("GROUPS.Users"), "Group prefix check with wrong group name 2 failed");
        assertFalse(CmsGroup.hasPrefix("SGROUPS.Users"), "Group prefix check with wrong group name 3 failed");

        // Role checks
        assertTrue(CmsRole.hasPrefix("ROLE.EDITOR"), "Role prefix check with correct role name 1 failed");
        assertTrue(CmsRole.hasPrefix("  ROLE.EDITOR"), "Role prefix check with correct role name 2 failed");
        assertTrue(CmsRole.hasPrefix("ROLE.EDITOR   "), "Role prefix check with correct role name 3 failed");
        assertTrue(CmsRole.hasPrefix("Role.EDITOR   "), "Role prefix check with correct role name 3 failed");
        assertTrue(
            CmsRole.removePrefix("ROLE.EDITOR").equals("EDITOR"),
            "Role prefix removal with correct role name failed");
        assertTrue(
            CmsRole.removePrefix("   ROLE.EDITOR").equals("EDITOR"),
            "Role prefix removal with correct role name failed");
        assertFalse(CmsRole.hasPrefix(null), "Role prefix check with null failed");
        assertFalse(CmsRole.hasPrefix(""), "Role prefix check with empty String failed");
        assertFalse(CmsRole.hasPrefix("ROLEEDITOR"), "Role prefix check with wrong role name 1 failed");
        assertFalse(CmsRole.hasPrefix("ROLES.EDITOR"), "Role prefix check with wrong role name 2 failed");
        assertFalse(CmsRole.hasPrefix("SROLES.EDITOR"), "Role prefix check with wrong role name 3 failed");
    }

    /**
     * Test user history.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    @Order(2)
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

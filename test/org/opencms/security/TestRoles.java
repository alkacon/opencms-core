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

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the OpenCms system roles.<p>
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class TestRoles extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestRoles(String arg0) {

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
        suite.setName(TestRoles.class.getName());

        suite.addTest(new TestRoles("testRoleExceptionMessages"));
        suite.addTest(new TestRoles("testRoleAssignments"));
        suite.addTest(new TestRoles("testSubRoles"));
        suite.addTest(new TestRoles("testVirtualRoleGroups"));
        suite.addTest(new TestRoles("testRoleDelegating"));
        suite.addTest(new TestRoles("testSpecialUserConfirmation"));

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
     * Check the given message.<p>
     *
     * @param message the message to check
     */
    private static void checkMessage(String message) {

        System.out.println(message);
        // check if a key could not be resolved
        assertFalse(message.indexOf(CmsMessages.UNKNOWN_KEY_EXTENSION) >= 0);
        // very simple check if message still containes unresolved '{n}'
        assertFalse(message.indexOf('{') >= 0);
    }

    /**
     * Tests role assignments.<p>
     *
     * @throws Exception if the test fails
     */
    public void testRoleAssignments() throws Exception {

        echo("Testing role assignments");
        CmsObject cms = getCmsObject();

        CmsRoleManager roleMan = OpenCms.getRoleManager();
        // check preconditions for admin, with some roles
        roleMan.checkRoleForResource(cms, CmsRole.ROOT_ADMIN, "/");
        roleMan.checkRole(cms, CmsRole.ROOT_ADMIN);
        roleMan.checkRoleForResource(cms, CmsRole.DEVELOPER, "/");
        roleMan.checkRole(cms, CmsRole.DEVELOPER.forOrgUnit(""));
        roleMan.checkRoleForResource(cms, CmsRole.WORKPLACE_MANAGER, "/");
        roleMan.checkRole(cms, CmsRole.WORKPLACE_MANAGER);

        assertFalse(roleMan.getManageableGroups(cms, "", false).isEmpty());
        assertFalse(roleMan.getManageableUsers(cms, "", false).isEmpty());
        assertFalse(roleMan.getOrgUnitsForRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit(""), false).isEmpty());

        assertFalse(
            roleMan.getRolesOfUser(
                cms,
                cms.getRequestContext().getCurrentUser().getName(),
                "",
                true,
                false,
                false).isEmpty());
        assertTrue(
            roleMan.getUsersOfRole(cms, CmsRole.ROOT_ADMIN, true, false).contains(
                cms.getRequestContext().getCurrentUser()));
        assertTrue(roleMan.getUsersOfRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit(""), true, true).isEmpty());
        assertEquals(1, roleMan.getUsersOfRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit(""), true, false).size());

        // check preconditions for test user, with some roles
        CmsUser user = cms.readUser("test1");
        assertFalse(roleMan.hasRoleForResource(cms, user.getName(), CmsRole.ROOT_ADMIN, "/"));
        assertFalse(roleMan.hasRole(cms, user.getName(), CmsRole.ROOT_ADMIN));
        assertFalse(roleMan.hasRoleForResource(cms, user.getName(), CmsRole.DEVELOPER, "/"));
        assertFalse(roleMan.hasRole(cms, user.getName(), CmsRole.DEVELOPER.forOrgUnit("")));
        assertFalse(roleMan.hasRoleForResource(cms, user.getName(), CmsRole.WORKPLACE_MANAGER, "/"));
        assertFalse(roleMan.hasRole(cms, user.getName(), CmsRole.WORKPLACE_MANAGER));

        assertEquals(5, roleMan.getRolesOfUser(cms, user.getName(), "", true, false, false).size());
        assertFalse(roleMan.getUsersOfRole(cms, CmsRole.ROOT_ADMIN, true, false).contains(user));
        assertTrue(
            roleMan.getUsersOfRole(cms, CmsRole.ROOT_ADMIN, true, false).contains(
                cms.getRequestContext().getCurrentUser()));
        assertTrue(
            roleMan.getUsersOfRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit(""), true, false).contains(
                cms.getRequestContext().getCurrentUser()));

        // login as test user to check if it can create a user
        cms.loginUser(user.getName(), "test1");
        try {
            cms.createUser("mytest", "mytest", "my test", null);
            fail("the user should not have account management permissions");
        } catch (CmsRoleViolationException e) {
            // ok, ignore
        }
        assertTrue(roleMan.getManageableGroups(cms, "", false).isEmpty());
        assertTrue(roleMan.getManageableUsers(cms, "", false).isEmpty());
        assertTrue(roleMan.getOrgUnitsForRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit(""), false).isEmpty());

        // login back as admin
        cms = getCmsObject();
        roleMan.addUserToRole(cms, CmsRole.ADMINISTRATOR, user.getName());

        // login back as test user to check again
        cms.loginUser(user.getName(), "test1");
        // now it should work
        cms.createUser("mytest", "mytest", "my test", null);

        // check post-conditions for test user, with some roles
        assertFalse(roleMan.hasRoleForResource(cms, user.getName(), CmsRole.ROOT_ADMIN, "/"));
        assertFalse(roleMan.hasRole(cms, user.getName(), CmsRole.ROOT_ADMIN));
        assertTrue(roleMan.hasRoleForResource(cms, user.getName(), CmsRole.DEVELOPER, "/"));
        assertTrue(roleMan.hasRole(cms, user.getName(), CmsRole.DEVELOPER.forOrgUnit("")));
        assertFalse(roleMan.hasRoleForResource(cms, user.getName(), CmsRole.WORKPLACE_MANAGER, "/"));
        assertFalse(roleMan.hasRole(cms, user.getName(), CmsRole.WORKPLACE_MANAGER));

        assertFalse(roleMan.getManageableGroups(cms, "", false).isEmpty());
        assertFalse(roleMan.getManageableUsers(cms, "", false).isEmpty());
        assertFalse(roleMan.getOrgUnitsForRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit(""), false).isEmpty());

        assertFalse(roleMan.getRolesOfUser(cms, user.getName(), "", true, false, false).isEmpty());
        assertTrue(
            roleMan.getUsersOfRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit(""), true, false).contains(
                cms.getRequestContext().getCurrentUser()));
        assertTrue(roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(""), true, true).isEmpty());
        assertTrue(roleMan.getUsersOfRole(cms, CmsRole.ROOT_ADMIN, true, false).contains(cms.readUser("Admin")));
        assertFalse(
            roleMan.getUsersOfRole(cms, CmsRole.ROOT_ADMIN, true, false).contains(
                cms.getRequestContext().getCurrentUser()));
    }

    /**
     * Tests role delegating.<p>
     *
     * @throws Exception if the test fails
     */
    public void testRoleDelegating() throws Exception {

        echo("Testing role delegating");
        CmsObject cms = getCmsObject();

        CmsRoleManager roleMan = OpenCms.getRoleManager();

        CmsUser user = cms.createUser("testUser", "testUser", "testUser", null);
        roleMan.addUserToRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(""), user.getName());

        cms.loginUser(user.getName(), "testUser");
        CmsUser u2 = cms.createUser("testUser2", "testUser2", "testUser2", null);

        try {
            roleMan.addUserToRole(cms, CmsRole.DEVELOPER.forOrgUnit(""), u2.getName());
            fail("it should not be possible to delegate a role you do not have");
        } catch (CmsRoleViolationException e) {
            // ok, ignore
        }
        roleMan.addUserToRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(""), u2.getName());
    }

    /**
     * Tests if all keys in the system roles exception messages can be resolved.<p>
     *
     * @throws Exception if the test fails
     */
    public void testRoleExceptionMessages() throws Exception {

        echo("Testing role exception messages");

        // check the system roles
        Iterator i = CmsRole.getSystemRoles().iterator();
        while (i.hasNext()) {
            CmsRole role = (CmsRole)i.next();
            checkMessage(role.getName(Locale.ENGLISH));
            checkMessage(role.getDescription(Locale.ENGLISH));
        }

        // check a user defined role
        String roleName = "MY_VERY_SPECIAL_ROLE";
        CmsRole myRole = new CmsRole(roleName, null, OpenCms.getDefaultUsers().getGroupAdministrators(), true);
        checkMessage(myRole.getName(Locale.ENGLISH));
        checkMessage(myRole.getDescription(Locale.ENGLISH));
    }

    /**
     * Tests special user based role confirmation.<p>
     *
     * @throws Exception if the test fails
     */
    public void testSpecialUserConfirmation() throws Exception {

        echo("Testing special user based role confirmation");
        CmsObject cms = getCmsObject();

        CmsRoleManager roleMan = OpenCms.getRoleManager();

        // check standard: Workplace user has roles CATEGORY_EDITOR, GALLERY_EDITOR
        CmsUser user = cms.createUser("specUser", "specUser", "specUser", null);
        roleMan.addUserToRole(cms, CmsRole.WORKPLACE_USER, user.getName());
        cms.loginUser(user.getName(), "specUser");

        assertTrue(roleMan.hasRole(cms, CmsRole.CATEGORY_EDITOR));
        assertTrue(roleMan.hasRole(cms, CmsRole.GALLERY_EDITOR));

        // configure individual confirmation for CATEGORY_EDITOR
        OpenCms.setRuntimeProperty(CmsRole.CONFIRM_ROLE_PREFIX + "CATEGORY_EDITOR", "true");
        OpenCms.getMemoryMonitor().clearCache();

        // we know user has no individual confirmation by default
        assertFalse(roleMan.hasRole(cms, CmsRole.CATEGORY_EDITOR));
        assertTrue(roleMan.hasRole(cms, CmsRole.GALLERY_EDITOR));

        // configure individual confirmation for GALLERY_EDITOR
        OpenCms.setRuntimeProperty(CmsRole.CONFIRM_ROLE_PREFIX + "GALLERY_EDITOR", "true");
        OpenCms.getMemoryMonitor().clearCache();

        // we know user has no individual confirmation by default still
        assertFalse(roleMan.hasRole(cms, CmsRole.CATEGORY_EDITOR));
        assertFalse(roleMan.hasRole(cms, CmsRole.GALLERY_EDITOR));

        // now add individual confirmation for the user as CATEGORY_EDITOR
        user.setAdditionalInfo(CmsRole.CONFIRM_ROLE_PREFIX + "CATEGORY_EDITOR", "true");
        cms.writeUser(user);
        OpenCms.getMemoryMonitor().clearCache();
        // must login again otherwise additional info will not be updated
        cms.loginUser(user.getName(), "specUser");

        // access to the CATEGORY_EDITOR must be available now
        assertTrue(roleMan.hasRole(cms, CmsRole.CATEGORY_EDITOR));
        assertFalse(roleMan.hasRole(cms, CmsRole.GALLERY_EDITOR));

        // now add individual confirmation for the user as GALLERY_EDITOR
        user.setAdditionalInfo(CmsRole.CONFIRM_ROLE_PREFIX + "GALLERY_EDITOR", "true");
        cms.writeUser(user);
        OpenCms.getMemoryMonitor().clearCache();
        cms.loginUser(user.getName(), "specUser");

        // access to the GALLERY_EDITOR must be available now
        assertTrue(roleMan.hasRole(cms, CmsRole.CATEGORY_EDITOR));
        assertTrue(roleMan.hasRole(cms, CmsRole.GALLERY_EDITOR));

        // now test higher ranking user
        // here access must be available even if no individual confirmation is present
        cms.loginUser("Admin", "admin");
        CmsUser devUser = cms.createUser("devUser", "devUser", "devUser", null);
        roleMan.addUserToRole(cms, CmsRole.DEVELOPER, devUser.getName());
        cms.loginUser(devUser.getName(), "devUser");

        assertTrue("true".equals(OpenCms.getRuntimeProperty(CmsRole.CONFIRM_ROLE_PREFIX + "CATEGORY_EDITOR")));
        assertTrue(devUser.getAdditionalInfo(CmsRole.CONFIRM_ROLE_PREFIX + "CATEGORY_EDITOR") == null);
        assertTrue(roleMan.hasRole(cms, CmsRole.CATEGORY_EDITOR));
        assertTrue("true".equals(OpenCms.getRuntimeProperty(CmsRole.CONFIRM_ROLE_PREFIX + "GALLERY_EDITOR")));
        assertTrue(devUser.getAdditionalInfo(CmsRole.CONFIRM_ROLE_PREFIX + "GALLERY_EDITOR") == null);
        assertTrue(roleMan.hasRole(cms, CmsRole.GALLERY_EDITOR));

        // now test lower ranking user GALLERY_EDITOR
        // if a user has _only_ CATEGORY_EDITOR / GALLERY_EDITOR but not WORKPLACE_USER he should get access
        // otherwise having the role CATEGORY_EDITOR / GALLERY_EDITOR would be pointless
        cms.loginUser("Admin", "admin");
        CmsUser galUser = cms.createUser("galUser", "galUser", "galUser", null);
        roleMan.addUserToRole(cms, CmsRole.GALLERY_EDITOR, galUser.getName());
        cms.loginUser(galUser.getName(), "galUser");

        assertTrue("true".equals(OpenCms.getRuntimeProperty(CmsRole.CONFIRM_ROLE_PREFIX + "CATEGORY_EDITOR")));
        assertTrue(galUser.getAdditionalInfo(CmsRole.CONFIRM_ROLE_PREFIX + "CATEGORY_EDITOR") == null);
        // the user is only GALLERY_EDITOR but not CATEGORY_EDITOR
        assertFalse(roleMan.hasRole(cms, CmsRole.CATEGORY_EDITOR));
        assertTrue("true".equals(OpenCms.getRuntimeProperty(CmsRole.CONFIRM_ROLE_PREFIX + "GALLERY_EDITOR")));
        assertTrue(galUser.getAdditionalInfo(CmsRole.CONFIRM_ROLE_PREFIX + "GALLERY_EDITOR") == null);
        // the user should have the CATEGORY_EDITOR role without individual confirmation
        assertTrue(roleMan.hasRole(cms, CmsRole.GALLERY_EDITOR));

        // now test lower ranking user CATEGORY_EDITOR
        // if a user has _only_ CATEGORY_EDITOR / GALLERY_EDITOR but not WORKPLACE_USER he should get access
        // otherwise having the role CATEGORY_EDITOR / GALLERY_EDITOR would be pointless
        cms.loginUser("Admin", "admin");
        CmsUser catUser = cms.createUser("catUser", "catUser", "catUser", null);
        roleMan.addUserToRole(cms, CmsRole.CATEGORY_EDITOR, catUser.getName());
        cms.loginUser(catUser.getName(), "catUser");

        assertTrue("true".equals(OpenCms.getRuntimeProperty(CmsRole.CONFIRM_ROLE_PREFIX + "CATEGORY_EDITOR")));
        assertTrue(catUser.getAdditionalInfo(CmsRole.CONFIRM_ROLE_PREFIX + "CATEGORY_EDITOR") == null);
        // the user should have the CATEGORY_EDITOR role without individual confirmation
        assertTrue(roleMan.hasRole(cms, CmsRole.CATEGORY_EDITOR));
        assertTrue("true".equals(OpenCms.getRuntimeProperty(CmsRole.CONFIRM_ROLE_PREFIX + "GALLERY_EDITOR")));
        assertTrue(catUser.getAdditionalInfo(CmsRole.CONFIRM_ROLE_PREFIX + "GALLERY_EDITOR") == null);
        // the user is only CATEGORY_EDITOR but not GALLERY_EDITOR
        assertFalse(roleMan.hasRole(cms, CmsRole.GALLERY_EDITOR));
    }

    /**
     * Tests subroles operations.<p>
     *
     * @throws Exception if the test fails
     */
    public void testSubRoles() throws Exception {

        echo("Testing subroles operations");
        CmsObject cms = getCmsObject();

        CmsRoleManager roleMan = OpenCms.getRoleManager();
        // check preconditions for admin
        List adminRoles = roleMan.getRolesOfUser(
            cms,
            cms.getRequestContext().getCurrentUser().getName(),
            "",
            false,
            true,
            false);
        assertEquals(1, adminRoles.size());
        assertTrue(adminRoles.contains(CmsRole.ROOT_ADMIN));

        // should do nothing
        roleMan.addUserToRole(
            cms,
            CmsRole.DEVELOPER.forOrgUnit(""),
            cms.getRequestContext().getCurrentUser().getName());

        // check again
        adminRoles = roleMan.getRolesOfUser(
            cms,
            cms.getRequestContext().getCurrentUser().getName(),
            "",
            false,
            true,
            false);
        assertEquals(1, adminRoles.size());
        assertTrue(adminRoles.contains(CmsRole.ROOT_ADMIN));

        CmsUser user = cms.readUser("test2");
        List roles = roleMan.getRolesOfUser(cms, user.getName(), "", true, true, false);
        assertEquals(1, roles.size());
        assertTrue(roles.contains(CmsRole.WORKPLACE_USER.forOrgUnit(user.getOuFqn())));

        roleMan.addUserToRole(cms, CmsRole.VFS_MANAGER.forOrgUnit(user.getOuFqn()), user.getName());

        roles = roleMan.getRolesOfUser(cms, user.getName(), "", true, true, false);
        assertEquals(1, roles.size());
        assertTrue(roles.contains(CmsRole.VFS_MANAGER.forOrgUnit(user.getOuFqn())));

        roles = roleMan.getRolesOfUser(cms, user.getName(), "", true, false, false);
        List children = CmsRole.VFS_MANAGER.forOrgUnit("").getChildren(true);
        children.add(CmsRole.VFS_MANAGER.forOrgUnit(""));
        assertEquals(children.size(), roles.size());
        Iterator it = roles.iterator();
        while (it.hasNext()) {
            CmsRole role = (CmsRole)it.next();
            assertTrue(children.contains(role));
        }

        // now add a parent role
        roleMan.addUserToRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit(user.getOuFqn()), user.getName());
        // which should have removed the child role
        roles = roleMan.getRolesOfUser(cms, user.getName(), "", true, true, false);
        // wp user is child of administrator, so it will not be set automatically
        assertEquals(1, roles.size());
        assertTrue(roles.contains(CmsRole.ADMINISTRATOR.forOrgUnit(user.getOuFqn())));

        roles = roleMan.getRolesOfUser(cms, user.getName(), "", true, false, false);
        children = CmsRole.ADMINISTRATOR.forOrgUnit("").getChildren(true);
        children.add(CmsRole.ADMINISTRATOR.forOrgUnit(""));
        assertEquals(children.size(), roles.size());
        it = roles.iterator();
        while (it.hasNext()) {
            CmsRole role = (CmsRole)it.next();
            assertTrue(children.contains(role));
        }
    }

    /**
     * Tests virtual role groups.<p>
     *
     * @throws Exception if the test fails
     */
    public void testVirtualRoleGroups() throws Exception {

        echo("Testing virtual role groups");

        CmsObject cms = getCmsObject();
        CmsGroup group = cms.createGroup("mytest", "vfs managers", CmsRole.VFS_MANAGER.getVirtualGroupFlags(), null);

        List roleUsers = OpenCms.getRoleManager().getUsersOfRole(cms, CmsRole.VFS_MANAGER.forOrgUnit(""), true, false);
        List groupUsers = cms.getUsersOfGroup(group.getName());
        assertEquals(new HashSet(roleUsers), new HashSet(groupUsers));

        // try out a child role
        OpenCms.getRoleManager().addUserToRole(cms, CmsRole.DEVELOPER.forOrgUnit(""), "Guest");
        // nothing should change
        assertEquals(new HashSet(roleUsers), new HashSet(cms.getUsersOfGroup(group.getName())));

        // try out a parent role
        OpenCms.getRoleManager().addUserToRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit(""), "Guest");
        assertEquals(groupUsers.size() + 1, cms.getUsersOfGroup(group.getName()).size());
        assertTrue(cms.getUsersOfGroup(group.getName()).contains(cms.readUser("Guest")));

        // everything should be as before
        OpenCms.getRoleManager().removeUserFromRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit(""), "Guest");
        groupUsers = cms.getUsersOfGroup(group.getName());
        assertEquals(new HashSet(roleUsers), new HashSet(groupUsers));

        // remove the virtual group
        cms.deleteGroup(group.getName());
        assertFalse(OpenCms.getOrgUnitManager().getGroups(cms, "", true).contains(group));

        // check the roles for the user
        assertTrue(OpenCms.getRoleManager().getRolesOfUser(cms, "Guest", "", true, true, true).isEmpty());

        // try to add a role by adding a user to the group
        group = cms.createGroup("mytest", "vfs managers", CmsRole.VFS_MANAGER.getVirtualGroupFlags(), null);
        assertEquals(1, cms.getGroupsOfUser("Guest", false).size());
        assertTrue(OpenCms.getRoleManager().getRolesOfUser(cms, "Guest", "", true, true, true).isEmpty());
        cms.addUserToGroup("Guest", group.getName());
        assertEquals(3, cms.getGroupsOfUser("Guest", false).size());
        assertEquals(1, OpenCms.getRoleManager().getRolesOfUser(cms, "Guest", "", true, true, true).size());

        cms.removeUserFromGroup("Guest", group.getName());
        assertEquals(1, cms.getGroupsOfUser("Guest", false).size());
        assertTrue(OpenCms.getRoleManager().getRolesOfUser(cms, "Guest", "", true, true, true).isEmpty());
    }
}

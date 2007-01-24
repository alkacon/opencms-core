/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/security/TestRoles.java,v $
 * Date   : $Date: 2007/01/24 08:30:27 $
 * Version: $Revision: 1.4.8.3 $
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

package org.opencms.security;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the OpenCms system roles.<p>
 */
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
            cms.getRequestContext().currentUser().getName(),
            "/",
            false,
            true,
            false);
        assertEquals(1, adminRoles.size());
        assertTrue(((CmsGroup)adminRoles.get(0)).getName().equals(CmsRole.ROOT_ADMIN.getGroupName()));

        // should do nothing
        roleMan.addUserToRole(cms, CmsRole.DEVELOPER, "/", cms.getRequestContext().currentUser().getName());

        // check again
        adminRoles = roleMan.getRolesOfUser(
            cms,
            cms.getRequestContext().currentUser().getName(),
            "/",
            false,
            true,
            false);
        assertEquals(1, adminRoles.size());
        assertTrue(((CmsGroup)adminRoles.get(0)).getName().equals(CmsRole.ROOT_ADMIN.getGroupName()));

        CmsUser user = cms.readUser("/test2");
        roleMan.addUserToRole(cms, CmsRole.VFS_MANAGER, user.getOuFqn(), user.getName());

        List roles = roleMan.getRolesOfUser(cms, user.getName(), "/", true, true, false);
        assertEquals(1, roles.size());
        assertTrue(roles.contains(cms.readGroup(CmsRole.VFS_MANAGER.getGroupName(user.getOuFqn()))));

        roles = roleMan.getRolesOfUser(cms, user.getName(), "/", true, false, false);
        List childs = CmsRole.VFS_MANAGER.getChilds(true);
        childs.add(CmsRole.VFS_MANAGER);
        assertEquals(childs.size(), roles.size());
        Iterator it = roles.iterator();
        while (it.hasNext()) {
            CmsGroup role = (CmsGroup)it.next();
            assertTrue(childs.contains(CmsRole.valueOf(role.getName())));
        }

        // now add a parent role
        roleMan.addUserToRole(cms, CmsRole.ADMINISTRATOR, user.getOuFqn(), user.getName());
        // which should have removed the child role
        roles = roleMan.getRolesOfUser(cms, user.getName(), "/", true, true, false);
        assertEquals(1, roles.size());
        assertTrue(roles.contains(cms.readGroup(CmsRole.ADMINISTRATOR.getGroupName(user.getOuFqn()))));

        roles = roleMan.getRolesOfUser(cms, user.getName(), "/", true, false, false);
        childs = CmsRole.ADMINISTRATOR.getChilds(true);
        childs.add(CmsRole.ADMINISTRATOR);
        assertEquals(childs.size(), roles.size());
        it = roles.iterator();
        while (it.hasNext()) {
            CmsGroup role = (CmsGroup)it.next();
            assertTrue(childs.contains(CmsRole.valueOf(role.getName())));
        }
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
        roleMan.checkRoleForOrgUnit(cms, CmsRole.ROOT_ADMIN, "/");
        roleMan.checkRoleForResource(cms, CmsRole.DEVELOPER, "/");
        roleMan.checkRoleForOrgUnit(cms, CmsRole.DEVELOPER, "/");
        roleMan.checkRoleForResource(cms, CmsRole.WORKPLACE_MANAGER, "/");
        roleMan.checkRoleForOrgUnit(cms, CmsRole.WORKPLACE_MANAGER, "/");

        assertFalse(roleMan.getManageableGroups(cms, "/", false).isEmpty());
        assertFalse(roleMan.getManageableUsers(cms, "/", false).isEmpty());
        assertFalse(roleMan.getManageableOrgUnits(cms, "/", false).isEmpty());

        assertFalse(roleMan.getRolesOfUser(
            cms,
            cms.getRequestContext().currentUser().getName(),
            "/",
            true,
            false,
            false).isEmpty());
        assertTrue(roleMan.getUsersOfRole(cms, CmsRole.ROOT_ADMIN, "/", true, false).contains(
            cms.getRequestContext().currentUser()));
        assertTrue(roleMan.getUsersOfRole(cms, CmsRole.ADMINISTRATOR, "/", true, false).isEmpty());

        // check preconditions for test user, with some roles
        CmsUser user = cms.readUser("/test1");
        assertFalse(roleMan.hasRoleForResource(cms, user.getName(), CmsRole.ROOT_ADMIN, "/"));
        assertFalse(roleMan.hasRoleForOrgUnit(cms, user.getName(), CmsRole.ROOT_ADMIN, "/"));
        assertFalse(roleMan.hasRoleForResource(cms, user.getName(), CmsRole.DEVELOPER, "/"));
        assertFalse(roleMan.hasRoleForOrgUnit(cms, user.getName(), CmsRole.DEVELOPER, "/"));
        assertFalse(roleMan.hasRoleForResource(cms, user.getName(), CmsRole.WORKPLACE_MANAGER, "/"));
        assertFalse(roleMan.hasRoleForOrgUnit(cms, user.getName(), CmsRole.WORKPLACE_MANAGER, "/"));

        assertTrue(roleMan.getRolesOfUser(cms, user.getName(), "/", true, false, false).isEmpty());
        assertFalse(roleMan.getUsersOfRole(cms, CmsRole.ROOT_ADMIN, "/", true, false).contains(user));
        assertTrue(roleMan.getUsersOfRole(cms, CmsRole.ROOT_ADMIN, "/", true, false).contains(
            cms.getRequestContext().currentUser()));
        assertTrue(roleMan.getUsersOfRole(cms, CmsRole.ADMINISTRATOR, "/", true, false).isEmpty());

        // login as test user to check if it can create a user
        cms.loginUser(user.getName(), "test1");
        try {
            cms.createUser("/mytest", "mytest", "my test", null);
            fail("the user should not have account management permissions");
        } catch (CmsRoleViolationException e) {
            // ok, ignore
        }
        assertTrue(roleMan.getManageableGroups(cms, "/", false).isEmpty());
        assertTrue(roleMan.getManageableUsers(cms, "/", false).isEmpty());
        assertTrue(roleMan.getManageableOrgUnits(cms, "/", false).isEmpty());

        // login back as admin
        cms = getCmsObject();
        roleMan.addUserToRole(cms, CmsRole.ADMINISTRATOR, "/", user.getName());

        // login back as test user to check again
        cms.loginUser(user.getName(), "test1");
        // now it should work
        cms.createUser("/mytest", "mytest", "my test", null);

        // check post-conditions for test user, with some roles
        assertFalse(roleMan.hasRoleForResource(cms, user.getName(), CmsRole.ROOT_ADMIN, "/"));
        assertFalse(roleMan.hasRoleForOrgUnit(cms, user.getName(), CmsRole.ROOT_ADMIN, "/"));
        assertTrue(roleMan.hasRoleForResource(cms, user.getName(), CmsRole.DEVELOPER, "/"));
        assertTrue(roleMan.hasRoleForOrgUnit(cms, user.getName(), CmsRole.DEVELOPER, "/"));
        assertFalse(roleMan.hasRoleForResource(cms, user.getName(), CmsRole.WORKPLACE_MANAGER, "/"));
        assertFalse(roleMan.hasRoleForOrgUnit(cms, user.getName(), CmsRole.WORKPLACE_MANAGER, "/"));

        assertFalse(roleMan.getManageableGroups(cms, "/", false).isEmpty());
        assertFalse(roleMan.getManageableUsers(cms, "/", false).isEmpty());
        assertFalse(roleMan.getManageableOrgUnits(cms, "/", false).isEmpty());

        assertFalse(roleMan.getRolesOfUser(cms, user.getName(), "/", true, false, false).isEmpty());
        assertTrue(roleMan.getUsersOfRole(cms, CmsRole.ADMINISTRATOR, "/", true, false).contains(
            cms.getRequestContext().currentUser()));
        assertTrue(roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER, "/", true, false).isEmpty());
        assertTrue(roleMan.getUsersOfRole(cms, CmsRole.ROOT_ADMIN, "/", true, false).contains(cms.readUser("Admin")));
        assertFalse(roleMan.getUsersOfRole(cms, CmsRole.ROOT_ADMIN, "/", true, false).contains(
            cms.getRequestContext().currentUser()));
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
        CmsRole myRole = new CmsRole(roleName, null, CmsRole.ROOT_ADMIN.getGroupName(), true);
        checkMessage(myRole.getName(Locale.ENGLISH));
        checkMessage(myRole.getDescription(Locale.ENGLISH));
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
}

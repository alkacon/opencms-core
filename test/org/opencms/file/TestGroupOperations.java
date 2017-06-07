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

package org.opencms.file;

import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for basic group operations without test import.<p>
 */
public class TestGroupOperations extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestGroupOperations(String arg0) {

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
        suite.setName(TestGroupOperations.class.getName());

        suite.addTest(new TestGroupOperations("testGetUsersOfGroup"));
        suite.addTest(new TestGroupOperations("testParentGroups"));
        suite.addTest(new TestGroupOperations("testChildGroups"));
        suite.addTest(new TestGroupOperations("testDeleteGroup"));
        suite.addTest(new TestGroupOperations("testDeleteGroupWithChildren"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms(null, null, false);
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Tests the "getChildren" method.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testChildGroups() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing the child group mechanism");

        CmsGroup g1 = cms.readGroup("g1");
        CmsGroup g2 = cms.readGroup("g2");
        CmsGroup g3 = cms.readGroup("g3");
        CmsGroup g4 = cms.readGroup("g4");
        CmsGroup g5 = cms.readGroup("g5");

        List children = cms.getChildren(g1.getName(), false);
        assertEquals(2, children.size());
        assertTrue(children.contains(g2));
        assertTrue(children.contains(g3));
        children = cms.getChildren(g1.getName(), true);
        assertEquals(4, children.size());
        assertTrue(children.contains(g2));
        assertTrue(children.contains(g3));
        assertTrue(children.contains(g4));
        assertTrue(children.contains(g5));
        children = cms.getChildren(g2.getName(), false);
        assertEquals(2, children.size());
        assertTrue(children.contains(g4));
        assertTrue(children.contains(g5));
        children = cms.getChildren(g2.getName(), true);
        assertEquals(2, children.size());
        assertTrue(children.contains(g4));
        assertTrue(children.contains(g5));
        children = cms.getChildren(g3.getName(), false);
        assertTrue(children.isEmpty());
        children = cms.getChildren(g3.getName(), true);
        assertTrue(children.isEmpty());
        children = cms.getChildren(g4.getName(), false);
        assertTrue(children.isEmpty());
        children = cms.getChildren(g4.getName(), true);
        assertTrue(children.isEmpty());
        children = cms.getChildren(g5.getName(), false);
        assertTrue(children.isEmpty());
        children = cms.getChildren(g5.getName(), true);
        assertTrue(children.isEmpty());
    }

    /**
     * Tests group deletion.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testDeleteGroup() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing group deletion");

        // get all groups
        List groups = OpenCms.getOrgUnitManager().getGroups(cms, "", true);

        CmsGroup group = cms.createGroup("testDeleteGroup", "group for deletion", 0, null);
        assertTrue(cms.getUsersOfGroup(group.getName()).isEmpty());
        assertEquals(groups.size() + 1, OpenCms.getOrgUnitManager().getGroups(cms, "", true).size());
        assertTrue(OpenCms.getOrgUnitManager().getGroups(cms, "", true).contains(group));

        CmsUser user = cms.readUser("Guest");
        List userGroups = cms.getGroupsOfUser(user.getName(), true);

        cms.addUserToGroup(user.getName(), group.getName());
        assertEquals(1, cms.getUsersOfGroup(group.getName()).size());
        assertTrue(cms.getUsersOfGroup(group.getName()).contains(user));

        assertEquals(userGroups.size() + 1, cms.getGroupsOfUser(user.getName(), true).size());
        assertTrue(cms.getGroupsOfUser(user.getName(), true).contains(group));

        cms.deleteGroup(group.getName());
        try {
            cms.readGroup(group.getName());
            fail("should not be able to read a deleted group");
        } catch (CmsDbEntryNotFoundException e) {
            // ok, ignore
        }

        assertEquals(groups.size(), OpenCms.getOrgUnitManager().getGroups(cms, "", true).size());
        assertFalse(OpenCms.getOrgUnitManager().getGroups(cms, "", true).contains(group));

        assertEquals(userGroups.size(), cms.getGroupsOfUser(user.getName(), true).size());
        assertFalse(cms.getGroupsOfUser(user.getName(), true).contains(group));
    }

    /**
     * Tests group deletion with children groups.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testDeleteGroupWithChildren() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing group deletion with children");

        // get all groups
        List groups = OpenCms.getOrgUnitManager().getGroups(cms, "", true);

        CmsGroup groupA = cms.createGroup("testDeleteGroupA", "group for deletion", 0, null);
        CmsGroup groupB = cms.createGroup("testDeleteGroupB", "child group for deletion", 0, groupA.getName());

        assertEquals(groups.size() + 2, OpenCms.getOrgUnitManager().getGroups(cms, "", true).size());

        cms.deleteGroup(groupA.getName());
        try {
            cms.readGroup(groupA.getName());
            fail("should not be able to read a deleted group");
        } catch (CmsDbEntryNotFoundException e) {
            // ok, ignore
        }
        cms.readGroup(groupB.getName());

        assertEquals(groups.size() + 1, OpenCms.getOrgUnitManager().getGroups(cms, "", true).size());
        assertFalse(OpenCms.getOrgUnitManager().getGroups(cms, "", true).contains(groupA));
        assertTrue(OpenCms.getOrgUnitManager().getGroups(cms, "", true).contains(groupB));
    }

    /**
     * Tests the "getUsersOfGroup" method.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testGetUsersOfGroup() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing testGetUsersOfGroup");

        List<CmsUser> users = cms.getUsersOfGroup("Guests");
        CmsUser exportUser = cms.readUser("Export");
        CmsUser guestUser = cms.readUser("Guest");
        assertTrue(users.contains(exportUser) || users.contains(guestUser));
    }

    /**
     * Tests the "getParentGroup" method.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testParentGroups() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing the parent group mechanism");

        CmsGroup g1 = cms.createGroup("g1", "g1", 0, null);
        CmsGroup g2 = cms.createGroup("g2", "g2", 0, g1.getName());
        CmsGroup g3 = cms.createGroup("g3", "g3", 0, g1.getName());
        CmsGroup g4 = cms.createGroup("g4", "g4", 0, g2.getName());
        CmsGroup g5 = cms.createGroup("g5", "g5", 0, g2.getName());

        CmsUser u1 = cms.createUser("u1", "password", "u1", null);
        cms.addUserToGroup(u1.getName(), g1.getName());

        List g1Users = cms.getUsersOfGroup(g1.getName());
        assertEquals(1, g1Users.size());
        assertTrue(g1Users.contains(u1));
        List g2Users = cms.getUsersOfGroup(g2.getName());
        assertTrue(g2Users.isEmpty());
        List g3Users = cms.getUsersOfGroup(g3.getName());
        assertTrue(g3Users.isEmpty());
        List g4Users = cms.getUsersOfGroup(g4.getName());
        assertTrue(g4Users.isEmpty());
        List g5Users = cms.getUsersOfGroup(g5.getName());
        assertTrue(g5Users.isEmpty());
        List u1Groups = cms.getGroupsOfUser(u1.getName(), false);
        assertEquals(1, u1Groups.size());
        assertTrue(u1Groups.contains(g1));

        CmsUser u2 = cms.createUser("u2", "password", "u2", null);
        cms.addUserToGroup(u2.getName(), g2.getName());

        g1Users = cms.getUsersOfGroup(g1.getName());
        assertEquals(1, g1Users.size());
        assertTrue(g1Users.contains(u1));
        g2Users = cms.getUsersOfGroup(g2.getName());
        assertEquals(1, g2Users.size());
        assertTrue(g2Users.contains(u2));
        g3Users = cms.getUsersOfGroup(g3.getName());
        assertTrue(g3Users.isEmpty());
        g4Users = cms.getUsersOfGroup(g4.getName());
        assertTrue(g4Users.isEmpty());
        g5Users = cms.getUsersOfGroup(g5.getName());
        assertTrue(g5Users.isEmpty());
        u1Groups = cms.getGroupsOfUser(u1.getName(), false);
        assertEquals(1, u1Groups.size());
        assertTrue(u1Groups.contains(g1));
        List u2Groups = cms.getGroupsOfUser(u2.getName(), false);
        assertEquals(2, u2Groups.size());
        assertTrue(u2Groups.contains(g2));
        assertTrue(u2Groups.contains(g1));

        CmsUser u3 = cms.createUser("u3", "password", "u3", null);
        cms.addUserToGroup(u3.getName(), g3.getName());

        g1Users = cms.getUsersOfGroup(g1.getName());
        assertEquals(1, g1Users.size());
        assertTrue(g1Users.contains(u1));
        g2Users = cms.getUsersOfGroup(g2.getName());
        assertEquals(1, g2Users.size());
        assertTrue(g2Users.contains(u2));
        g3Users = cms.getUsersOfGroup(g3.getName());
        assertEquals(1, g3Users.size());
        assertTrue(g3Users.contains(u3));
        g4Users = cms.getUsersOfGroup(g4.getName());
        assertTrue(g4Users.isEmpty());
        g5Users = cms.getUsersOfGroup(g5.getName());
        assertTrue(g5Users.isEmpty());
        u1Groups = cms.getGroupsOfUser(u1.getName(), false);
        assertEquals(1, u1Groups.size());
        assertTrue(u1Groups.contains(g1));
        u2Groups = cms.getGroupsOfUser(u2.getName(), false);
        assertEquals(2, u2Groups.size());
        assertTrue(u2Groups.contains(g2));
        assertTrue(u2Groups.contains(g1));
        List u3Groups = cms.getGroupsOfUser(u3.getName(), false);
        assertEquals(2, u3Groups.size());
        assertTrue(u3Groups.contains(g3));
        assertTrue(u3Groups.contains(g1));

        CmsUser u4 = cms.createUser("u4", "password", "u4", null);
        cms.addUserToGroup(u4.getName(), g4.getName());

        g1Users = cms.getUsersOfGroup(g1.getName());
        assertEquals(1, g1Users.size());
        assertTrue(g1Users.contains(u1));
        g2Users = cms.getUsersOfGroup(g2.getName());
        assertEquals(1, g2Users.size());
        assertTrue(g2Users.contains(u2));
        g3Users = cms.getUsersOfGroup(g3.getName());
        assertEquals(1, g3Users.size());
        assertTrue(g3Users.contains(u3));
        g4Users = cms.getUsersOfGroup(g4.getName());
        assertEquals(1, g4Users.size());
        assertTrue(g4Users.contains(u4));
        g5Users = cms.getUsersOfGroup(g5.getName());
        assertTrue(g5Users.isEmpty());
        u1Groups = cms.getGroupsOfUser(u1.getName(), false);
        assertEquals(1, u1Groups.size());
        assertTrue(u1Groups.contains(g1));
        u2Groups = cms.getGroupsOfUser(u2.getName(), false);
        assertEquals(2, u2Groups.size());
        assertTrue(u2Groups.contains(g2));
        assertTrue(u2Groups.contains(g1));
        u3Groups = cms.getGroupsOfUser(u3.getName(), false);
        assertEquals(2, u3Groups.size());
        assertTrue(u3Groups.contains(g3));
        assertTrue(u3Groups.contains(g1));
        List u4Groups = cms.getGroupsOfUser(u4.getName(), false);
        assertEquals(3, u4Groups.size());
        assertTrue(u4Groups.contains(g4));
        assertTrue(u4Groups.contains(g2));
        assertTrue(u4Groups.contains(g1));

        CmsUser u5 = cms.createUser("u5", "password", "u5", null);
        cms.addUserToGroup(u5.getName(), g5.getName());

        g1Users = cms.getUsersOfGroup(g1.getName());
        assertEquals(1, g1Users.size());
        assertTrue(g1Users.contains(u1));
        g2Users = cms.getUsersOfGroup(g2.getName());
        assertEquals(1, g2Users.size());
        assertTrue(g2Users.contains(u2));
        g3Users = cms.getUsersOfGroup(g3.getName());
        assertEquals(1, g3Users.size());
        assertTrue(g3Users.contains(u3));
        g4Users = cms.getUsersOfGroup(g4.getName());
        assertEquals(1, g4Users.size());
        assertTrue(g4Users.contains(u4));
        g5Users = cms.getUsersOfGroup(g5.getName());
        assertEquals(1, g5Users.size());
        assertTrue(g5Users.contains(u5));
        u1Groups = cms.getGroupsOfUser(u1.getName(), false);
        assertEquals(1, u1Groups.size());
        assertTrue(u1Groups.contains(g1));
        u2Groups = cms.getGroupsOfUser(u2.getName(), false);
        assertEquals(2, u2Groups.size());
        assertTrue(u2Groups.contains(g2));
        assertTrue(u2Groups.contains(g1));
        u3Groups = cms.getGroupsOfUser(u3.getName(), false);
        assertEquals(2, u3Groups.size());
        assertTrue(u3Groups.contains(g3));
        assertTrue(u3Groups.contains(g1));
        u4Groups = cms.getGroupsOfUser(u4.getName(), false);
        assertEquals(3, u4Groups.size());
        assertTrue(u4Groups.contains(g4));
        assertTrue(u4Groups.contains(g2));
        assertTrue(u4Groups.contains(g1));
        List u5Groups = cms.getGroupsOfUser(u5.getName(), false);
        assertEquals(3, u5Groups.size());
        assertTrue(u5Groups.contains(g5));
        assertTrue(u5Groups.contains(g2));
        assertTrue(u5Groups.contains(g1));
    }
}
/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/security/TestOrganizationalUnits.java,v $
 * Date   : $Date: 2007/01/29 09:44:55 $
 * Version: $Revision: 1.1.2.5 $
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

import org.opencms.db.CmsDbConsistencyException;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for operations with organizational units.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.5 $
 */
public class TestOrganizationalUnits extends OpenCmsTestCase {

    int m_todo1; // change ou name/parent, 1day

    int m_todo2; // test case for addusertorole!?
    int m_todo3; // test case for getresourceforou
    int m_todo4; // test case for writeou
    int m_todo5; // test case virtual groups
    
    int m_todo6; // user additional info clean up (import/export, compatibility, update), 3days
    int m_todo7; // db creation scripts, 0.5day
    int m_todo8; // localization, 0.5day
    int m_todo9; // publishmanager, oumanager and rolemanager methods should be accessible from the shell

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestOrganizationalUnits(String arg0) {

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
        suite.setName(TestOrganizationalUnits.class.getName());

        suite.addTest(new TestOrganizationalUnits("testRootOu"));
        suite.addTest(new TestOrganizationalUnits("testRootRoles"));
        suite.addTest(new TestOrganizationalUnits("testFirstLevelOu"));
        suite.addTest(new TestOrganizationalUnits("testFirstLevelRoles"));
        suite.addTest(new TestOrganizationalUnits("testDeeperLevelOu"));
        suite.addTest(new TestOrganizationalUnits("testDeeperLevelRoles"));
        suite.addTest(new TestOrganizationalUnits("testResourceAssociations"));
        suite.addTest(new TestOrganizationalUnits("testPrincipalAssociations"));
        suite.addTest(new TestOrganizationalUnits("testRoleInheritance"));
        suite.addTest(new TestOrganizationalUnits("testParallelRoles"));
        suite.addTest(new TestOrganizationalUnits("testUserLogin"));
        suite.addTest(new TestOrganizationalUnits("testMembership"));
        suite.addTest(new TestOrganizationalUnits("testPersistence"));
        suite.addTest(new TestOrganizationalUnits("testDelete"));

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
     * Tests handling with a deeper level ou.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testDeeperLevelOu() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing handling with a deeper level ou");

        // check the root ou
        CmsOrganizationalUnit rootOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "");
        CmsOrganizationalUnit ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "/test");

        // create a deeper ou
        CmsOrganizationalUnit ou2 = OpenCms.getOrgUnitManager().createOrganizationalUnit(
            cms,
            ou.getName() + "test2/",
            "my test ou2",
            0,
            "/folder1");

        // check the ou attributes
        assertEquals(0, ou2.getFlags());
        assertEquals("/test/", ou2.getParentFqn());
        assertEquals("/test/test2/", ou2.getName());
        assertEquals(cms.readFolder("/system/orgunits/test/test2").getStructureId(), ou2.getId());

        // check ou resources
        List ou2Resources = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName());
        assertEquals(1, ou2Resources.size());
        assertTrue(ou2Resources.contains(cms.readResource("/folder1")));

        // check the ous
        List rootOus = OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, rootOu.getName(), false);
        assertEquals(1, rootOus.size());
        assertTrue(rootOus.contains(ou));

        List ouOus = OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, ou.getName(), false);
        assertEquals(1, ouOus.size());
        assertTrue(ouOus.contains(ou2));

        List ou2Ous = OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, ou2.getName(), false);
        assertTrue(ou2Ous.isEmpty());

        List rootSubOus = OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, rootOu.getName(), true);
        assertEquals(2, rootSubOus.size());
        assertTrue(rootSubOus.contains(ou));
        assertTrue(rootSubOus.contains(ou2));

        List ouSubOus = OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, ou.getName(), true);
        assertEquals(1, ouSubOus.size());
        assertTrue(ouSubOus.contains(ou2));

        List ou2SubOus = OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, ou2.getName(), true);
        assertTrue(ou2SubOus.isEmpty());

        OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, ou.getName());
        try {
            OpenCms.getOrgUnitManager().deleteOrganizationalUnit(cms, ou.getName());
            fail("should not be possible to delete a ou having sub-ous");
        } catch (CmsDataAccessException e) {
            // ok, ignore
        }
        OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, ou.getName());
    }

    /**
     * Tests handling with a first level ou.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testFirstLevelOu() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing handling with a first level ou");

        // check the root ou
        CmsOrganizationalUnit rootOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "");

        cms.addUserToGroup("/test1", "/Administrators");
        cms.loginUser("/test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        cms.lockResource("/system/");

        cms.loginUser("/Admin", "admin");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        // create a first level ou
        CmsOrganizationalUnit ou = OpenCms.getOrgUnitManager().createOrganizationalUnit(
            cms,
            "/test/",
            "my test ou",
            0,
            CmsOrganizationalUnit.SEPARATOR);

        // check the ou attributes
        assertEquals(0, ou.getFlags());
        assertEquals(CmsOrganizationalUnit.SEPARATOR, ou.getParentFqn());
        assertEquals("my test ou", ou.getDescription());
        assertEquals("/test/", ou.getName());
        assertEquals(cms.readFolder("/system/orgunits/test").getStructureId(), ou.getId());

        // check ou resources
        List ouResources = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou.getName());
        assertEquals(1, ouResources.size());
        assertTrue(ouResources.contains(cms.readResource("/")));

        // check the ous
        List rootOus = OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, rootOu.getName(), false);
        assertEquals(1, rootOus.size());
        assertTrue(rootOus.contains(ou));

        List ouOus = OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, ou.getName(), false);
        assertTrue(ouOus.isEmpty());

        List rootSubOus = OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, rootOu.getName(), true);
        assertEquals(1, rootSubOus.size());
        assertTrue(rootSubOus.contains(ou));

        List ouSubOus = OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, ou.getName(), true);
        assertTrue(ouSubOus.isEmpty());

        // test to update ou attributes
        ou.setDescription("new ou description");
        ou.setFlags(123);
        OpenCms.getOrgUnitManager().writeOrganizationalUnit(cms, ou);

        ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, ou.getName());
        // check the ou attributes again
        assertEquals(123, ou.getFlags());
        assertEquals(CmsOrganizationalUnit.SEPARATOR, ou.getParentFqn());
        assertEquals("new ou description", ou.getDescription());
        assertEquals("/test/", ou.getName());
        assertEquals(cms.readFolder("/system/orgunits/test").getStructureId(), ou.getId());
        assertTrue(cms.readFolder("/system/orgunits/test").isInternal());

        ou.setFlags(0);
        OpenCms.getOrgUnitManager().writeOrganizationalUnit(cms, ou);

        ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, ou.getName());
        assertEquals(0, ou.getFlags());
        assertTrue(cms.readFolder("/system/orgunits/test").isInternal());

        // create a first level ou
        CmsOrganizationalUnit ou2 = OpenCms.getOrgUnitManager().createOrganizationalUnit(
            cms,
            "/test2/",
            "my test ou2 in the root",
            0,
            CmsOrganizationalUnit.SEPARATOR);

        // check the ou attributes
        assertEquals(0, ou2.getFlags());
        assertEquals(CmsOrganizationalUnit.SEPARATOR, ou2.getParentFqn());
        assertEquals("my test ou2 in the root", ou2.getDescription());
        assertEquals("/test2/", ou2.getName());
        assertEquals(cms.readFolder("/system/orgunits/test2").getStructureId(), ou2.getId());

        // check ou resources
        List ou2Resources = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName());
        assertEquals(1, ou2Resources.size());
        assertTrue(ou2Resources.contains(cms.readResource("/")));

        // check the ous
        rootOus = OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, rootOu.getName(), false);
        assertEquals(2, rootOus.size());
        assertTrue(rootOus.contains(ou));
        assertTrue(rootOus.contains(ou2));

        List ou2Ous = OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, ou2.getName(), false);
        assertTrue(ou2Ous.isEmpty());

        rootSubOus = OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, rootOu.getName(), true);
        assertEquals(2, rootSubOus.size());
        assertTrue(rootSubOus.contains(ou));
        assertTrue(rootSubOus.contains(ou2));

        List ou2SubOus = OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, ou2.getName(), true);
        assertTrue(ou2SubOus.isEmpty());

        // delete 2nd ou
        OpenCms.getOrgUnitManager().deleteOrganizationalUnit(cms, ou2.getName());

        try {
            OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, ou2.getName());
            fail("it should not be possible to read the deleted ou");
        } catch (CmsDataAccessException e) {
            // ok, ignore
        }

        // check the ous again
        rootOus = OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, rootOu.getName(), false);
        assertEquals(1, rootOus.size());
        assertTrue(rootOus.contains(ou));

        ouOus = OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, ou.getName(), false);
        assertTrue(ouOus.isEmpty());

        rootSubOus = OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, rootOu.getName(), true);
        assertEquals(1, rootSubOus.size());
        assertTrue(rootSubOus.contains(ou));

        List ouSubous = OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, ou.getName(), true);
        assertTrue(ouSubous.isEmpty());

        // check default users & groups for the new ou
        assertTrue(OpenCms.getOrgUnitManager().getGroups(cms, ou.getName(), false).isEmpty());
        assertTrue(OpenCms.getOrgUnitManager().getGroups(cms, ou.getName(), true).isEmpty());
        assertTrue(OpenCms.getOrgUnitManager().getUsers(cms, ou.getName(), false).isEmpty());
        assertTrue(OpenCms.getOrgUnitManager().getUsers(cms, ou.getName(), true).isEmpty());

        // check the root ou principals again
        List rootGroups = OpenCms.getOrgUnitManager().getGroups(cms, rootOu.getName(), false);
        assertEquals(7, rootGroups.size());
        assertTrue(rootGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupAdministrators())));
        assertTrue(rootGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupProjectmanagers())));
        assertTrue(rootGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupUsers())));
        assertTrue(rootGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupGuests())));
        assertTrue(rootGroups.contains(cms.readGroup("/group1")));
        assertTrue(rootGroups.contains(cms.readGroup("/group2")));
        assertTrue(rootGroups.contains(cms.readGroup("/group3")));

        List rootSubGroups = OpenCms.getOrgUnitManager().getGroups(cms, rootOu.getName(), true);
        assertEquals(7, rootSubGroups.size());
        assertTrue(rootSubGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupAdministrators())));
        assertTrue(rootSubGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupProjectmanagers())));
        assertTrue(rootSubGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupUsers())));
        assertTrue(rootSubGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupGuests())));
        assertTrue(rootSubGroups.contains(cms.readGroup("/group1")));
        assertTrue(rootSubGroups.contains(cms.readGroup("/group2")));
        assertTrue(rootSubGroups.contains(cms.readGroup("/group3")));

        List rootUsers = OpenCms.getOrgUnitManager().getUsers(cms, rootOu.getName(), false);
        assertEquals(5, rootUsers.size());
        assertTrue(rootUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserAdmin())));
        assertTrue(rootUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserExport())));
        assertTrue(rootUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserGuest())));
        assertTrue(rootUsers.contains(cms.readUser("/test1")));
        assertTrue(rootUsers.contains(cms.readUser("/test2")));

        List rootSubUsers = OpenCms.getOrgUnitManager().getUsers(cms, rootOu.getName(), true);
        assertEquals(5, rootSubUsers.size());
        assertTrue(rootSubUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserAdmin())));
        assertTrue(rootSubUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserExport())));
        assertTrue(rootSubUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserGuest())));
        assertTrue(rootSubUsers.contains(cms.readUser("/test1")));
        assertTrue(rootSubUsers.contains(cms.readUser("/test2")));

        cms.removeUserFromGroup("/test1", "/Administrators");
        assertLock(cms, "/system/", CmsLockType.EXCLUSIVE, cms.readUser("/test1"));
        cms.changeLock("/system/");
        cms.unlockResource("/system/");
    }

    /**
     * Tests system roles in first level ou.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testFirstLevelRoles() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing system roles in first level ou");

        // check the ous
        CmsOrganizationalUnit rootOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, CmsOrganizationalUnit.SEPARATOR);
        CmsOrganizationalUnit flOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "/test/");

        // check just the roles of the root ou
        List roleGroups = OpenCms.getRoleManager().getRoles(cms, rootOu.getName(), false);
        assertEquals(CmsRole.getSystemRoles().size(), roleGroups.size());
        List roles = new ArrayList(roleGroups.size());
        Iterator itRoleGroups = roleGroups.iterator();
        while (itRoleGroups.hasNext()) {
            CmsGroup role = (CmsGroup)itRoleGroups.next();
            assertEquals(rootOu.getName(), role.getOuFqn());
            roles.add(CmsRole.valueOf(role.getName()));
        }
        for (int i = 0; i < roles.size(); i++) {
            assertTrue(CmsRole.getSystemRoles().contains(roles.get(i)));
            assertTrue(roles.contains(CmsRole.getSystemRoles().get(i)));
        }
        // check all roles
        roleGroups = OpenCms.getRoleManager().getRoles(cms, rootOu.getName(), true);
        assertEquals(CmsRole.getSystemRoles().size() + getOuRoles().size(), roleGroups.size());
        List rootRoles = new ArrayList(CmsRole.getSystemRoles().size());
        List flRoles = new ArrayList(getOuRoles().size());
        itRoleGroups = roleGroups.iterator();
        while (itRoleGroups.hasNext()) {
            CmsGroup role = (CmsGroup)itRoleGroups.next();
            if (role.getOuFqn().equals(rootOu.getName())) {
                rootRoles.add(CmsRole.valueOf(role.getName()));
            } else {
                assertEquals(flOu.getName(), role.getOuFqn());
                flRoles.add(CmsRole.valueOf(role.getName()));
            }
        }
        for (int i = 0; i < rootRoles.size(); i++) {
            assertTrue(CmsRole.getSystemRoles().contains(rootRoles.get(i)));
            assertTrue(rootRoles.contains(CmsRole.getSystemRoles().get(i)));
        }
        for (int i = 0; i < flRoles.size(); i++) {
            assertTrue(getOuRoles().contains(flRoles.get(i)));
            assertTrue(flRoles.contains(getOuRoles().get(i)));
        }
        // check just the roles of the first level ou
        roleGroups = OpenCms.getRoleManager().getRoles(cms, flOu.getName(), false);
        assertEquals(getOuRoles().size(), roleGroups.size());
        roles = new ArrayList(roleGroups.size());
        itRoleGroups = roleGroups.iterator();
        while (itRoleGroups.hasNext()) {
            CmsGroup role = (CmsGroup)itRoleGroups.next();
            assertEquals(flOu.getName(), role.getOuFqn());
            roles.add(CmsRole.valueOf(role.getName()));
        }
        for (int i = 0; i < roles.size(); i++) {
            assertTrue(getOuRoles().contains(roles.get(i)));
            assertTrue(roles.contains(getOuRoles().get(i)));
        }
        // check again including (missing) sub-ous
        roleGroups = OpenCms.getRoleManager().getRoles(cms, flOu.getName(), true);
        assertEquals(getOuRoles().size(), roleGroups.size());
        roles = new ArrayList(roleGroups.size());
        itRoleGroups = roleGroups.iterator();
        while (itRoleGroups.hasNext()) {
            CmsGroup role = (CmsGroup)itRoleGroups.next();
            assertEquals(flOu.getName(), role.getOuFqn());
            roles.add(CmsRole.valueOf(role.getName()));
        }
        for (int i = 0; i < roles.size(); i++) {
            assertTrue(getOuRoles().contains(roles.get(i)));
            assertTrue(roles.contains(getOuRoles().get(i)));
        }
    }

    /**
     * Tests system roles in a deeper level ou.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testDeeperLevelRoles() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing system roles in a deeper level ou");

        // check the ous
        CmsOrganizationalUnit rootOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, CmsOrganizationalUnit.SEPARATOR);
        CmsOrganizationalUnit flOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "/test/");
        CmsOrganizationalUnit dlOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "/test/test2/");

        // check just the roles of the root ou
        List roleGroups = OpenCms.getRoleManager().getRoles(cms, rootOu.getName(), false);
        assertEquals(CmsRole.getSystemRoles().size(), roleGroups.size());
        List roles = new ArrayList(roleGroups.size());
        Iterator itRoleGroups = roleGroups.iterator();
        while (itRoleGroups.hasNext()) {
            CmsGroup role = (CmsGroup)itRoleGroups.next();
            assertEquals(rootOu.getName(), role.getOuFqn());
            roles.add(CmsRole.valueOf(role.getName()));
        }
        for (int i = 0; i < roles.size(); i++) {
            assertTrue(CmsRole.getSystemRoles().contains(roles.get(i)));
            assertTrue(roles.contains(CmsRole.getSystemRoles().get(i)));
        }
        // check all roles
        roleGroups = OpenCms.getRoleManager().getRoles(cms, rootOu.getName(), true);
        assertEquals(CmsRole.getSystemRoles().size() + getOuRoles().size() * 2, roleGroups.size());
        List rootRoles = new ArrayList(CmsRole.getSystemRoles().size());
        List flRoles = new ArrayList(getOuRoles().size());
        List dlRoles = new ArrayList(getOuRoles().size());
        itRoleGroups = roleGroups.iterator();
        while (itRoleGroups.hasNext()) {
            CmsGroup role = (CmsGroup)itRoleGroups.next();
            if (role.getOuFqn().equals(rootOu.getName())) {
                rootRoles.add(CmsRole.valueOf(role.getName()));
            } else if (role.getOuFqn().equals(flOu.getName())) {
                flRoles.add(CmsRole.valueOf(role.getName()));
            } else {
                assertEquals(dlOu.getName(), role.getOuFqn());
                dlRoles.add(CmsRole.valueOf(role.getName()));
            }
        }
        for (int i = 0; i < rootRoles.size(); i++) {
            assertTrue(CmsRole.getSystemRoles().contains(rootRoles.get(i)));
            assertTrue(rootRoles.contains(CmsRole.getSystemRoles().get(i)));
        }
        for (int i = 0; i < flRoles.size(); i++) {
            assertTrue(getOuRoles().contains(flRoles.get(i)));
            assertTrue(flRoles.contains(getOuRoles().get(i)));
        }
        for (int i = 0; i < dlRoles.size(); i++) {
            assertTrue(getOuRoles().contains(dlRoles.get(i)));
            assertTrue(dlRoles.contains(getOuRoles().get(i)));
        }
        // check just the roles of the first level ou
        roleGroups = OpenCms.getRoleManager().getRoles(cms, flOu.getName(), false);
        assertEquals(getOuRoles().size(), roleGroups.size());
        roles = new ArrayList(roleGroups.size());
        itRoleGroups = roleGroups.iterator();
        while (itRoleGroups.hasNext()) {
            CmsGroup role = (CmsGroup)itRoleGroups.next();
            assertEquals(flOu.getName(), role.getOuFqn());
            roles.add(CmsRole.valueOf(role.getName()));
        }
        for (int i = 0; i < roles.size(); i++) {
            assertTrue(getOuRoles().contains(roles.get(i)));
            assertTrue(roles.contains(getOuRoles().get(i)));
        }
        // check all roles in the first level ou and deeper
        roleGroups = OpenCms.getRoleManager().getRoles(cms, flOu.getName(), true);
        assertEquals(getOuRoles().size() * 2, roleGroups.size());
        flRoles = new ArrayList(getOuRoles().size());
        dlRoles = new ArrayList(getOuRoles().size());
        itRoleGroups = roleGroups.iterator();
        while (itRoleGroups.hasNext()) {
            CmsGroup role = (CmsGroup)itRoleGroups.next();
            if (role.getOuFqn().equals(flOu.getName())) {
                flRoles.add(CmsRole.valueOf(role.getName()));
            } else {
                assertEquals(dlOu.getName(), role.getOuFqn());
                dlRoles.add(CmsRole.valueOf(role.getName()));
            }
        }
        for (int i = 0; i < flRoles.size(); i++) {
            assertTrue(getOuRoles().contains(flRoles.get(i)));
            assertTrue(flRoles.contains(getOuRoles().get(i)));
        }
        for (int i = 0; i < dlRoles.size(); i++) {
            assertTrue(getOuRoles().contains(dlRoles.get(i)));
            assertTrue(dlRoles.contains(getOuRoles().get(i)));
        }
        // check just the roles of the deeper level ou
        roleGroups = OpenCms.getRoleManager().getRoles(cms, dlOu.getName(), false);
        assertEquals(getOuRoles().size(), roleGroups.size());
        roles = new ArrayList(roleGroups.size());
        itRoleGroups = roleGroups.iterator();
        while (itRoleGroups.hasNext()) {
            CmsGroup role = (CmsGroup)itRoleGroups.next();
            assertEquals(dlOu.getName(), role.getOuFqn());
            roles.add(CmsRole.valueOf(role.getName()));
        }
        for (int i = 0; i < roles.size(); i++) {
            assertTrue(getOuRoles().contains(roles.get(i)));
            assertTrue(roles.contains(getOuRoles().get(i)));
        }
        // check again including (missing) sub-ous
        roleGroups = OpenCms.getRoleManager().getRoles(cms, dlOu.getName(), true);
        assertEquals(getOuRoles().size(), roleGroups.size());
        roles = new ArrayList(roleGroups.size());
        itRoleGroups = roleGroups.iterator();
        while (itRoleGroups.hasNext()) {
            CmsGroup role = (CmsGroup)itRoleGroups.next();
            assertEquals(dlOu.getName(), role.getOuFqn());
            roles.add(CmsRole.valueOf(role.getName()));
        }
        for (int i = 0; i < roles.size(); i++) {
            assertTrue(getOuRoles().contains(roles.get(i)));
            assertTrue(roles.contains(getOuRoles().get(i)));
        }
    }

    /**
     * Returns all organizational unit dependent roles.<p>
     * 
     * @return all organizational unit dependent roles
     */
    public static List getOuRoles() {

        List roles = new ArrayList();
        Iterator itRoles = CmsRole.getSystemRoles().iterator();
        while (itRoles.hasNext()) {
            CmsRole role = (CmsRole)itRoles.next();
            if (!role.isOrganizationalUnitIndependent()) {
                roles.add(role);
            }
        }
        return roles;
    }

    /**
     * Tests handling with a deeper level ou.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testPrincipalAssociations() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing handling principal associations");

        // read the ous
        CmsOrganizationalUnit rootOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "");
        CmsOrganizationalUnit ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "/test");
        CmsOrganizationalUnit ou2 = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, ou.getName() + "test2");

        try {
            // try move to move /test1 to /test/test1 
            OpenCms.getOrgUnitManager().setUsersOrganizationalUnit(cms, ou.getName(), "/test1");
            fail("it should not be possible to move the user to other ou (since it is still member of a group)");
        } catch (CmsDbConsistencyException e) {
            // ok, ignore
        }
        cms.removeUserFromGroup("/test1", "/Users");
        cms.removeUserFromGroup("/test1", "/group1");
        // move to move /test1 to /test/test1 
        OpenCms.getOrgUnitManager().setUsersOrganizationalUnit(cms, ou.getName(), "/test1");

        try {
            cms.readUser("/test1");
            fail("it should not be possible to read the user in the old ou");
        } catch (CmsDbEntryNotFoundException e) {
            // ok, ignore
        }
        cms.readUser("/test/test1");
        assertTrue(cms.getGroupsOfUser("/test/test1", false).isEmpty());

        List rootUsers = OpenCms.getOrgUnitManager().getUsers(cms, rootOu.getName(), false);
        assertEquals(4, rootUsers.size());
        assertFalse(rootUsers.contains(cms.readUser("/test/test1")));
        List rootSubusers = OpenCms.getOrgUnitManager().getUsers(cms, rootOu.getName(), true);
        assertEquals(5, rootSubusers.size());
        assertTrue(rootSubusers.contains(cms.readUser("/test/test1")));

        List ouUsers = OpenCms.getOrgUnitManager().getUsers(cms, ou.getName(), false);
        assertEquals(1, ouUsers.size());
        assertTrue(ouUsers.contains(cms.readUser("/test/test1")));
        List ouSubusers = OpenCms.getOrgUnitManager().getUsers(cms, ou.getName(), true);
        assertEquals(1, ouSubusers.size());
        assertTrue(ouSubusers.contains(cms.readUser("/test/test1")));

        cms.removeUserFromGroup("/test2", "/Users");
        cms.removeUserFromGroup("/test2", "/group2");
        // move to move /test2 to /test/test2/test2 
        OpenCms.getOrgUnitManager().setUsersOrganizationalUnit(cms, ou2.getName(), "/test2");

        try {
            cms.readUser("/test2");
            fail("it should not be possible to read the user in the old ou");
        } catch (CmsDbEntryNotFoundException e) {
            // ok, ignore
        }
        cms.readUser("/test/test2/test2");
        assertTrue(cms.getGroupsOfUser("/test/test2/test2", false).isEmpty());

        rootUsers = OpenCms.getOrgUnitManager().getUsers(cms, rootOu.getName(), false);
        assertEquals(3, rootUsers.size());
        assertFalse(rootUsers.contains(cms.readUser("/test/test1")));
        assertFalse(rootUsers.contains(cms.readUser("/test/test2/test2")));

        rootSubusers = OpenCms.getOrgUnitManager().getUsers(cms, rootOu.getName(), true);
        assertEquals(5, rootSubusers.size());
        assertTrue(rootSubusers.contains(cms.readUser("/test/test1")));
        assertTrue(rootSubusers.contains(cms.readUser("/test/test2/test2")));

        List ouGroups = OpenCms.getOrgUnitManager().getGroups(cms, ou.getName(), false);
        assertTrue(ouGroups.isEmpty());

        List ouSubgroups = OpenCms.getOrgUnitManager().getGroups(cms, ou.getName(), true);
        assertTrue(ouSubgroups.isEmpty());

        ouUsers = OpenCms.getOrgUnitManager().getUsers(cms, ou.getName(), false);
        assertEquals(1, ouUsers.size());
        assertTrue(ouUsers.contains(cms.readUser("/test/test1")));

        ouSubusers = OpenCms.getOrgUnitManager().getUsers(cms, ou.getName(), true);
        assertEquals(2, ouSubusers.size());
        assertTrue(ouSubusers.contains(cms.readUser("/test/test1")));
        assertTrue(ouSubusers.contains(cms.readUser("/test/test2/test2")));
    }

    /**
     * Tests handling with resource associations.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testResourceAssociations() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing handling with resource associations");

        // read the ous
        CmsOrganizationalUnit rootOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "");
        CmsOrganizationalUnit ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "/test");
        CmsOrganizationalUnit ou2 = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "/test/test2");

        try {
            OpenCms.getOrgUnitManager().createOrganizationalUnit(cms, "/doesnotexist/test2/", "my test ou2", 0, CmsOrganizationalUnit.SEPARATOR);
            fail("should not be possible to create a new ou with a not valid parent");
        } catch (CmsDataAccessException e) {
            // ok, just be sure the ou has not been created
            assertEquals(2, OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, rootOu.getName(), true).size());
            assertTrue(OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, rootOu.getName(), true).contains(ou));
            assertTrue(OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, rootOu.getName(), true).contains(ou2));
        }

        try {
            OpenCms.getOrgUnitManager().createOrganizationalUnit(
                cms,
                ou2.getName() + "test3/",
                "my test ou3",
                0,
                "/folder2");
            fail("should not be possible to create a new ou with a resource out of the parent scope");
        } catch (CmsDataAccessException e) {
            // ok, just be sure the ou has not been created
            assertEquals(2, OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, rootOu.getName(), true).size());
            assertTrue(OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, rootOu.getName(), true).contains(ou));
            assertTrue(OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, rootOu.getName(), true).contains(ou2));
        }

        // test adding the same resource as a parent ou resource
        assertEquals(1, OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).size());
        assertTrue(OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).contains(
            cms.readResource("/folder1")));
        CmsOrganizationalUnit ou3 = OpenCms.getOrgUnitManager().createOrganizationalUnit(
            cms,
            ou2.getName() + "test3",
            "it will not last too long",
            0,
            "/folder1");
        assertEquals(1, OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou3.getName()).size());
        assertTrue(OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou3.getName()).contains(
            cms.readResource("/folder1")));
        OpenCms.getOrgUnitManager().deleteOrganizationalUnit(cms, ou3.getName());

        // test removing all resources
        assertEquals(1, OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).size());
        assertTrue(OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).contains(
            cms.readResource("/folder1")));
        try {
            // remove a resource
            OpenCms.getOrgUnitManager().removeResourceFromOrgUnit(cms, ou2.getName(), "/folder1");
            fail("it should not be possible to remove all resources");
        } catch (CmsDataAccessException e) {
            assertEquals(
                ((CmsDataAccessException)e.getCause()).getMessageContainer().getKey(),
                org.opencms.db.generic.Messages.ERR_ORGUNIT_REMOVE_LAST_RESOURCE_2);
            // ok, check again just to be sure
            assertEquals(1, OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).size());
            assertTrue(OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).contains(
                cms.readResource("/folder1")));
        }

        // resource addition tests
        try {
            OpenCms.getOrgUnitManager().addResourceToOrgUnit(cms, ou.getName(), "/folder1");
            fail("it should not be possible to add a resource that is contained in another ou resource");
        } catch (CmsDataAccessException e) {
            // ok, just check again to be sure
            assertEquals(1, OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou.getName()).size());
            assertTrue(OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou.getName()).contains(
                cms.readResource("/")));
        }

        try {
            OpenCms.getOrgUnitManager().addResourceToOrgUnit(cms, ou.getName(), "/sites/doesnotexist");
            fail("should not be possible to add an unexistent resource to an ou");
        } catch (CmsDataAccessException e) {
            // ok, just be sure
            assertEquals(1, OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou.getName()).size());
            assertTrue(OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou.getName()).contains(
                cms.readResource("/")));
        }

        // add an additional resource
        OpenCms.getOrgUnitManager().addResourceToOrgUnit(cms, ou2.getName(), "/folder2");
        // check ou resources again
        assertEquals(2, OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).size());
        assertTrue(OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).contains(
            cms.readResource("/folder1")));
        assertTrue(OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).contains(
            cms.readResource("/folder2")));

        // resource remotion tests
        try {
            OpenCms.getOrgUnitManager().removeResourceFromOrgUnit(cms, ou2.getName(), "/folder1/index.html");
            fail("should not be possible to remove an not associated resource from an ou");
        } catch (CmsDataAccessException e) {
            // ok, just be sure
            assertEquals(2, OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).size());
            assertTrue(OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).contains(
                cms.readResource("/folder1")));
            assertTrue(OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).contains(
                cms.readResource("/folder2")));
        }

        // remove a resource
        OpenCms.getOrgUnitManager().removeResourceFromOrgUnit(cms, ou2.getName(), "/folder1");
        // check ou resource
        assertEquals(1, OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).size());
        assertTrue(OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).contains(
            cms.readResource("/folder2")));
    }

    /**
     * Tests the inheritance of role memberships.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testRoleInheritance() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing the inheritance of role memberships");

        cms.getRequestContext().setSiteRoot("");

        CmsRoleManager roleMan = OpenCms.getRoleManager();
        CmsOrgUnitManager ouMan = OpenCms.getOrgUnitManager();
        // check the root ou
        CmsOrganizationalUnit rootOu = ouMan.readOrganizationalUnit(cms, "");
        CmsResource rootRes = (CmsResource)(ouMan.getResourcesForOrganizationalUnit(cms, rootOu.getName()).get(0)); // /
        CmsOrganizationalUnit ou = ouMan.readOrganizationalUnit(cms, "/test");
        CmsResource ouRes = (CmsResource)(ouMan.getResourcesForOrganizationalUnit(cms, ou.getName()).get(0)); // /sites/default/
        CmsOrganizationalUnit ou2 = ouMan.readOrganizationalUnit(cms, ou.getName() + "test2");
        CmsResource ou2Res = (CmsResource)(ouMan.getResourcesForOrganizationalUnit(cms, ou2.getName()).get(0)); // /sites/default/folder2/
        CmsUser user = cms.readUser("/test/test1");
        // check preconditions
        assertFalse(roleMan.hasRoleForResource(
            cms,
            user.getName(),
            CmsRole.ACCOUNT_MANAGER,
            cms.getRequestContext().getSitePath(rootRes)));
        assertFalse(roleMan.hasRoleForResource(
            cms,
            user.getName(),
            CmsRole.ACCOUNT_MANAGER,
            cms.getRequestContext().getSitePath(ouRes)));
        assertFalse(roleMan.hasRoleForResource(
            cms,
            user.getName(),
            CmsRole.ACCOUNT_MANAGER,
            cms.getRequestContext().getSitePath(ou2Res)));

        assertTrue(roleMan.hasRoleForResource(
            cms,
            CmsRole.ACCOUNT_MANAGER,
            cms.getRequestContext().getSitePath(rootRes)));
        assertTrue(roleMan.hasRoleForResource(cms, CmsRole.ACCOUNT_MANAGER, cms.getRequestContext().getSitePath(ouRes)));
        assertTrue(roleMan.hasRoleForResource(cms, CmsRole.ACCOUNT_MANAGER, cms.getRequestContext().getSitePath(ou2Res)));

        assertFalse(roleMan.hasRoleForOrgUnit(cms, user.getName(), CmsRole.ACCOUNT_MANAGER, ou.getName()));
        assertTrue(roleMan.getRolesOfUser(cms, user.getName(), ou.getName(), true, false, false).isEmpty());
        assertTrue(roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER, ou.getName(), true, false).isEmpty());
        // add user to role
        roleMan.addUserToRole(cms, CmsRole.ACCOUNT_MANAGER, ou.getName(), user.getName());
        // check role in the given ou
        assertTrue(roleMan.hasRoleForOrgUnit(cms, user.getName(), CmsRole.ACCOUNT_MANAGER, ou.getName()));
        assertEquals(1, roleMan.getRolesOfUser(cms, user.getName(), ou.getName(), false, false, false).size());
        assertTrue(((CmsGroup)roleMan.getRolesOfUser(cms, user.getName(), ou.getName(), false, false, false).get(0)).getName().equals(
            CmsRole.ACCOUNT_MANAGER.getGroupName(ou.getName())));
        assertEquals(1, roleMan.getRolesOfUser(cms, user.getName(), ou.getName(), false, true, false).size());
        assertTrue(((CmsGroup)roleMan.getRolesOfUser(cms, user.getName(), ou.getName(), false, true, false).get(0)).getName().equals(
            CmsRole.ACCOUNT_MANAGER.getGroupName(ou.getName())));
        assertEquals(1, roleMan.getRolesOfUser(cms, user.getName(), ou.getName(), true, false, false).size());
        assertTrue(((CmsGroup)roleMan.getRolesOfUser(cms, user.getName(), ou.getName(), true, false, false).get(0)).getName().equals(
            CmsRole.ACCOUNT_MANAGER.getGroupName(ou.getName())));
        assertEquals(1, roleMan.getRolesOfUser(cms, user.getName(), ou.getName(), true, true, false).size());
        assertTrue(((CmsGroup)roleMan.getRolesOfUser(cms, user.getName(), ou.getName(), true, true, false).get(0)).getName().equals(
            CmsRole.ACCOUNT_MANAGER.getGroupName(ou.getName())));
        assertEquals(1, roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER, ou.getName(), false, false).size());
        assertTrue(roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER, ou.getName(), false, false).contains(user));
        assertEquals(1, roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER, ou.getName(), false, true).size());
        assertTrue(roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER, ou.getName(), false, true).contains(user));
        assertEquals(1, roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER, ou.getName(), true, false).size());
        assertTrue(roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER, ou.getName(), true, false).contains(user));
        assertEquals(1, roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER, ou.getName(), true, true).size());
        assertTrue(roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER, ou.getName(), true, false).contains(user));
        // check role in deeper ou
        assertTrue(roleMan.hasRoleForOrgUnit(cms, user.getName(), CmsRole.ACCOUNT_MANAGER, ou2.getName()));
        assertTrue(roleMan.getRolesOfUser(cms, user.getName(), ou2.getName(), false, false, false).isEmpty());
        assertTrue(roleMan.getRolesOfUser(cms, user.getName(), ou2.getName(), false, true, false).isEmpty());
        assertEquals(1, roleMan.getRolesOfUser(cms, user.getName(), ou2.getName(), true, false, false).size());
        assertEquals(1, roleMan.getRolesOfUser(cms, user.getName(), ou2.getName(), true, true, false).size());
        assertTrue(roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER, ou2.getName(), false, false).isEmpty());
        assertTrue(roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER, ou2.getName(), false, true).isEmpty());
        assertTrue(roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER, ou2.getName(), true, false).isEmpty());
        assertTrue(roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER, ou2.getName(), true, true).isEmpty());
        // check role in higher ou
        assertFalse(roleMan.hasRoleForOrgUnit(cms, user.getName(), CmsRole.ACCOUNT_MANAGER, rootOu.getName()));
        assertEquals(1, roleMan.getRolesOfUser(cms, user.getName(), rootOu.getName(), true, false, false).size());
        assertTrue(((CmsGroup)roleMan.getRolesOfUser(cms, user.getName(), rootOu.getName(), true, false, false).get(0)).getName().equals(
            CmsRole.ACCOUNT_MANAGER.getGroupName(ou.getName())));
        assertTrue(roleMan.getRolesOfUser(cms, user.getName(), rootOu.getName(), false, false, false).isEmpty());
        assertTrue(roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER, rootOu.getName(), true, false).isEmpty());

        // check resources
        assertFalse(roleMan.hasRoleForResource(
            cms,
            user.getName(),
            CmsRole.ACCOUNT_MANAGER,
            cms.getRequestContext().getSitePath(rootRes)));
        assertTrue(roleMan.hasRoleForResource(
            cms,
            user.getName(),
            CmsRole.ACCOUNT_MANAGER,
            cms.getRequestContext().getSitePath(ouRes)));
        assertTrue(roleMan.hasRoleForResource(
            cms,
            user.getName(),
            CmsRole.ACCOUNT_MANAGER,
            cms.getRequestContext().getSitePath(ou2Res)));

        int todo; // remove the user role
    }

    /**
     * Tests the root ou automatic creation.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testRootOu() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing the automatic creation of the root organizational units");

        // check the root ou
        CmsOrganizationalUnit rootOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "");

        // check all the ous
        assertTrue(OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, rootOu.getName(), false).isEmpty());
        assertTrue(OpenCms.getOrgUnitManager().getOrganizationalUnits(cms, rootOu.getName(), true).isEmpty());

        // check the root ou attributes
        assertEquals(0, rootOu.getFlags());
        assertNull(rootOu.getParentFqn());
        assertEquals("The root organizational unit", rootOu.getDescription());
        assertEquals(CmsOrganizationalUnit.SEPARATOR, rootOu.getName());
        assertEquals(cms.readFolder("/system/orgunits").getStructureId(), rootOu.getId());

        // check root ou resources
        List rootResources = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, rootOu.getName());
        assertEquals(1, rootResources.size());
        String site = cms.getRequestContext().getSiteRoot();
        cms.getRequestContext().setSiteRoot("/");
        assertTrue(rootResources.contains(cms.readResource("/")));
        cms.getRequestContext().setSiteRoot(site);

        // check the root ou principals
        List rootGroups = OpenCms.getOrgUnitManager().getGroups(cms, rootOu.getName(), false);
        assertEquals(7, rootGroups.size());
        assertTrue(rootGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupAdministrators())));
        assertTrue(rootGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupProjectmanagers())));
        assertTrue(rootGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupUsers())));
        assertTrue(rootGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupGuests())));
        assertTrue(rootGroups.contains(cms.readGroup("/group1")));
        assertTrue(rootGroups.contains(cms.readGroup("/group2")));
        assertTrue(rootGroups.contains(cms.readGroup("/group3")));

        List rootSubGroups = OpenCms.getOrgUnitManager().getGroups(cms, rootOu.getName(), true);
        assertEquals(7, rootSubGroups.size());
        assertTrue(rootSubGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupAdministrators())));
        assertTrue(rootSubGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupProjectmanagers())));
        assertTrue(rootSubGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupUsers())));
        assertTrue(rootSubGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupGuests())));
        assertTrue(rootSubGroups.contains(cms.readGroup("/group1")));
        assertTrue(rootSubGroups.contains(cms.readGroup("/group2")));
        assertTrue(rootSubGroups.contains(cms.readGroup("/group3")));

        List rootUsers = OpenCms.getOrgUnitManager().getUsers(cms, rootOu.getName(), false);
        assertEquals(5, rootUsers.size());
        assertTrue(rootUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserAdmin())));
        assertTrue(rootUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserExport())));
        assertTrue(rootUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserGuest())));
        assertTrue(rootUsers.contains(cms.readUser("/test1")));
        assertTrue(rootUsers.contains(cms.readUser("/test2")));

        List rootSubUsers = OpenCms.getOrgUnitManager().getUsers(cms, rootOu.getName(), true);
        assertEquals(5, rootSubUsers.size());
        assertTrue(rootSubUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserAdmin())));
        assertTrue(rootSubUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserExport())));
        assertTrue(rootSubUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserGuest())));
        assertTrue(rootSubUsers.contains(cms.readUser("/test1")));
        assertTrue(rootSubUsers.contains(cms.readUser("/test2")));

        rootOu.setDescription("new description");
        OpenCms.getOrgUnitManager().writeOrganizationalUnit(cms, rootOu);

        try {
            rootOu.setName("abc");
            OpenCms.getOrgUnitManager().writeOrganizationalUnit(cms, rootOu);
            fail("should not be able to edit the root ou");
        } catch (CmsIllegalStateException e) {
            // ok, ignore
        }

        try {
            OpenCms.getOrgUnitManager().deleteOrganizationalUnit(cms, rootOu.getName());
            fail("should not be able to delete the root ou");
        } catch (CmsDbConsistencyException e) {
            // ok, ignore
        }
    }

    /**
     * Tests system roles in root ou.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testRootRoles() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing system roles in root ou");

        // check just the roles of the root ou
        List roleGroups = OpenCms.getRoleManager().getRoles(cms, CmsOrganizationalUnit.SEPARATOR, false);
        assertEquals(CmsRole.getSystemRoles().size(), roleGroups.size());
        List roles = new ArrayList(roleGroups.size());
        Iterator itRoleGroups = roleGroups.iterator();
        while (itRoleGroups.hasNext()) {
            CmsGroup role = (CmsGroup)itRoleGroups.next();
            assertEquals(CmsOrganizationalUnit.SEPARATOR, role.getOuFqn());
            roles.add(CmsRole.valueOf(role.getName()));
        }
        for (int i = 0; i < roles.size(); i++) {
            assertTrue(CmsRole.getSystemRoles().contains(roles.get(i)));
            assertTrue(roles.contains(CmsRole.getSystemRoles().get(i)));
        }
        // check all roles (but since there are no more ous, it is the same as before)
        roleGroups = OpenCms.getRoleManager().getRoles(cms, CmsOrganizationalUnit.SEPARATOR, true);
        assertEquals(CmsRole.getSystemRoles().size(), roleGroups.size());
        roles = new ArrayList(roleGroups.size());
        itRoleGroups = roleGroups.iterator();
        while (itRoleGroups.hasNext()) {
            CmsGroup role = (CmsGroup)itRoleGroups.next();
            assertEquals(CmsOrganizationalUnit.SEPARATOR, role.getOuFqn());
            roles.add(CmsRole.valueOf(role.getName()));
        }
        for (int i = 0; i < roles.size(); i++) {
            assertTrue(CmsRole.getSystemRoles().contains(roles.get(i)));
            assertTrue(roles.contains(CmsRole.getSystemRoles().get(i)));
        }
    }

    /**
     * Tests roles on parallel ous.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testParallelRoles() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing roles on parallel ous");

        CmsUser user = cms.readUser("/test/test1");

        // check preconditions
        assertEquals(3, OpenCms.getRoleManager().getManageableOrgUnits(cms, CmsOrganizationalUnit.SEPARATOR, true).size());

        cms.loginUser(user.getName(), "test1");
        List ous = OpenCms.getRoleManager().getManageableOrgUnits(cms, CmsOrganizationalUnit.SEPARATOR, true);
        assertTrue(ous.isEmpty());

        // create a new ou
        cms = getCmsObject();
        OpenCms.getRoleManager().addUserToRole(cms, CmsRole.ADMINISTRATOR, "/test/", user.getName());

        // check it
        cms.loginUser(user.getName(), "test1");
        ous = OpenCms.getRoleManager().getManageableOrgUnits(cms, CmsOrganizationalUnit.SEPARATOR, true);
        assertEquals(2, ous.size());
        assertTrue(ous.contains(OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "/test")));
        assertTrue(ous.contains(OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "/test/test2")));

        // create a new ou
        cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("");
        OpenCms.getOrgUnitManager().createOrganizationalUnit(cms, "/test3", "test3", 0, "/system/");
        // set it up
        OpenCms.getRoleManager().addUserToRole(cms, CmsRole.ADMINISTRATOR, "/test3/", user.getName());

        // check the result
        assertEquals(4, OpenCms.getRoleManager().getManageableOrgUnits(cms, CmsOrganizationalUnit.SEPARATOR, true).size());

        cms.loginUser(user.getName(), "test1");
        ous = OpenCms.getRoleManager().getManageableOrgUnits(cms, CmsOrganizationalUnit.SEPARATOR, true);
        assertEquals(3, ous.size());
        assertTrue(ous.contains(OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "/test")));
        assertTrue(ous.contains(OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "/test/test2")));
        assertTrue(ous.contains(OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "/test3")));
    }

    /**
     * Tests login.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testUserLogin() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing login");

        // login users in respective ous
        cms.loginUser("/test/test1", "test1");
        assertEquals("/test/", cms.getRequestContext().getOuFqn());
        assertEquals("/test/test1", cms.getRequestContext().currentUser().getName());
        cms.loginUser("/test/test2/test2", "test2");
        assertEquals("/test/test2/", cms.getRequestContext().getOuFqn());
        assertEquals("/test/test2/test2", cms.getRequestContext().currentUser().getName());

        // login user in deeper ou
        cms.loginUser("/test/test2/test1", "test1");
        assertEquals("/test/test2/", cms.getRequestContext().getOuFqn());
        assertEquals("/test/test1", cms.getRequestContext().currentUser().getName());

        try {
            // try login user in higher ou
            cms.loginUser("/test/test2", "test2");
            fail("It should not be possible to login an user in a higher ou");
        } catch (CmsAuthentificationException e) {
            // ok, ignore
        }
    }

    /**
     * Tests deleting organizational units.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testDelete() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing deleting organizational units");

        OpenCms.getRoleManager().addUserToRole(cms, CmsRole.ROOT_ADMIN, CmsOrganizationalUnit.SEPARATOR, "/test/test1");
        cms.loginUser("/test/test2/test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        try {
            OpenCms.getOrgUnitManager().deleteOrganizationalUnit(cms, cms.getRequestContext().getOuFqn());
            fail("it should not be possible to delete an organizational unit that is used in the current request context");
        } catch (CmsDbConsistencyException e) {
            // ok, now check the error message
            assertEquals(
                ((CmsDbConsistencyException)e.getCause()).getMessageContainer().getKey(),
                org.opencms.db.Messages.ERR_ORGUNIT_DELETE_IN_CONTEXT_1);
        }
        try {
            OpenCms.getOrgUnitManager().deleteOrganizationalUnit(cms, cms.getRequestContext().currentUser().getOuFqn());
            fail("it should not be possible to delete an organizational unit that is used by the current user");
        } catch (CmsDbConsistencyException e) {
            // ok, now check the error message
            assertEquals(
                ((CmsDbConsistencyException)e.getCause()).getMessageContainer().getKey(),
                org.opencms.db.Messages.ERR_ORGUNIT_DELETE_CURRENT_USER_1);
        }
        cms.loginUser("/Admin", "admin");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        try {
            OpenCms.getOrgUnitManager().deleteOrganizationalUnit(cms, "/test");
            fail("it should not be possible to delete an organizational unit that has sub-organizational units");
        } catch (CmsDbConsistencyException e) {
            // ok, now check the error message
            assertEquals(
                ((CmsDbConsistencyException)e.getCause()).getMessageContainer().getKey(),
                org.opencms.db.Messages.ERR_ORGUNIT_DELETE_SUB_ORGUNITS_1);
        }

        CmsOrganizationalUnit ou2 = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "/test/test2");
        cms.createGroup(ou2.getName() + "group2", "test group", 0, null);
        try {
            OpenCms.getOrgUnitManager().deleteOrganizationalUnit(cms, ou2.getName());
            fail("it should not be possible to delete an organizational unit that has groups & users units");
        } catch (CmsDbConsistencyException e) {
            // ok, now check the error message
            assertEquals(
                ((CmsDbConsistencyException)e.getCause()).getMessageContainer().getKey(),
                org.opencms.db.Messages.ERR_ORGUNIT_DELETE_GROUPS_1);
        }

        Iterator itGroups = OpenCms.getOrgUnitManager().getGroups(cms, ou2.getName(), false).iterator();
        while (itGroups.hasNext()) {
            CmsGroup group = (CmsGroup)itGroups.next();
            cms.deleteGroup(group.getName());
        }
        assertTrue(OpenCms.getOrgUnitManager().getGroups(cms, ou2.getName(), true).isEmpty());

        try {
            OpenCms.getOrgUnitManager().deleteOrganizationalUnit(cms, ou2.getName());
            fail("it should not be possible to delete an organizational unit that has users units");
        } catch (CmsDbConsistencyException e) {
            // ok, now check the error message
            assertEquals(
                ((CmsDbConsistencyException)e.getCause()).getMessageContainer().getKey(),
                org.opencms.db.Messages.ERR_ORGUNIT_DELETE_USERS_1);
        }

        Iterator itUsers = OpenCms.getOrgUnitManager().getUsers(cms, ou2.getName(), false).iterator();
        while (itUsers.hasNext()) {
            CmsUser user = (CmsUser)itUsers.next();
            cms.deleteUser(user.getName());
        }
        assertTrue(OpenCms.getOrgUnitManager().getUsers(cms, ou2.getName(), true).isEmpty());

        OpenCms.getOrgUnitManager().deleteOrganizationalUnit(cms, ou2.getName());

        // check persistence
        try {
            cms.readFolder("/system/orgunits/root/test/test2", CmsResourceFilter.ALL);
            fail("it should not be possible to read the underlying resource after deleting an organizational unit");
        } catch (CmsVfsResourceNotFoundException e) {
            // ok, ignore
        }
        // check online project
        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
        try {
            cms.readFolder("/system/orgunits/root/test/test2", CmsResourceFilter.ALL);
            fail("it should not be possible to read the underlying resource after deleting an organizational unit");
        } catch (CmsVfsResourceNotFoundException e) {
            // ok, ignore
        }
    }

    /**
     * Tests organizational units persistence.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testPersistence() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing organizational units persistence");

        // check the root ou
        CmsOrganizationalUnit rootOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "");
        CmsOrganizationalUnit ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "/test");
        CmsOrganizationalUnit ou2 = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, ou.getName() + "test2");

        // check offline project
        CmsFolder rootOuFolder = cms.readFolder("/system/orgunits/");
        assertEquals(rootOu.getId(), rootOuFolder.getStructureId());
        assertEquals(rootOu.getFlags(), rootOuFolder.getFlags() & ~CmsResource.FLAG_INTERNAL);
        assertTrue(rootOuFolder.getState().isUnchanged());
        assertTrue(rootOuFolder.isInternal());
        assertEquals(rootOu.getDescription(), cms.readPropertyObject(
            rootOuFolder,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            false).getValue());
        assertOrgUnitResources(cms, rootOu);

        CmsFolder ouFolder = cms.readFolder("/system/orgunits/test");
        assertEquals(ou.getId(), ouFolder.getStructureId());
        assertEquals(ou.getFlags(), ouFolder.getFlags() & ~CmsResource.FLAG_INTERNAL);
        assertTrue(ouFolder.getState().isUnchanged());
        assertTrue(ouFolder.isInternal());
        assertEquals(ou.getDescription(), cms.readPropertyObject(
            ouFolder,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            false).getValue());
        assertOrgUnitResources(cms, ou);

        CmsFolder ou2Folder = cms.readFolder("/system/orgunits/test/test2");
        assertEquals(ou2.getId(), ou2Folder.getStructureId());
        assertEquals(ou2.getFlags(), ou2Folder.getFlags() & ~CmsResource.FLAG_INTERNAL);
        assertTrue(ou2Folder.getState().isUnchanged());
        assertTrue(ou2Folder.isInternal());
        assertEquals(ou2.getDescription(), cms.readPropertyObject(
            ou2Folder,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            false).getValue());
        assertOrgUnitResources(cms, ou2);

        // now check the online project
        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
        rootOuFolder = cms.readFolder("/system/orgunits/");
        assertEquals(rootOu.getId(), rootOuFolder.getStructureId());
        assertEquals(rootOu.getFlags(), rootOuFolder.getFlags() & ~CmsResource.FLAG_INTERNAL);
        assertTrue(rootOuFolder.getState().isUnchanged());
        assertTrue(rootOuFolder.isInternal());
        assertEquals(rootOu.getDescription(), cms.readPropertyObject(
            rootOuFolder,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            false).getValue());
        assertOrgUnitResources(cms, rootOu);

        ouFolder = cms.readFolder("/system/orgunits/test");
        assertEquals(ou.getId(), ouFolder.getStructureId());
        assertEquals(ou.getFlags(), ouFolder.getFlags() & ~CmsResource.FLAG_INTERNAL);
        assertTrue(ouFolder.getState().isUnchanged());
        assertTrue(ouFolder.isInternal());
        assertEquals(ou.getDescription(), cms.readPropertyObject(
            ouFolder,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            false).getValue());
        assertOrgUnitResources(cms, ou);

        ou2Folder = cms.readFolder("/system/orgunits/test/test2");
        assertEquals(ou2.getId(), ou2Folder.getStructureId());
        assertEquals(ou2.getFlags(), ou2Folder.getFlags() & ~CmsResource.FLAG_INTERNAL);
        assertTrue(ou2Folder.getState().isUnchanged());
        assertTrue(ou2Folder.isInternal());
        assertEquals(ou2.getDescription(), cms.readPropertyObject(
            ou2Folder,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            false).getValue());
        assertOrgUnitResources(cms, ou2);
    }

    /**
     * Returns the list of associated resource root paths.<p>
     * 
     * @param cms the cms context
     * @param ou the organizational unit to get the resources for
     * 
     * @throws CmsException if somehting goes wrong
     */
    private void assertOrgUnitResources(CmsObject cms, CmsOrganizationalUnit ou) throws CmsException {

        List resourceList = new ArrayList();
        Iterator itResources = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou.getName()).iterator();
        while (itResources.hasNext()) {
            CmsResource resource = (CmsResource)itResources.next();
            resourceList.add(resource.getRootPath());
        }
        List relations = cms.getRelationsForResource(
            cms.getSitePath(cms.readResource(ou.getId())),
            CmsRelationFilter.TARGETS);
        assertEquals(relations.size(), resourceList.size());
        Iterator itRelations = relations.iterator();
        while (itRelations.hasNext()) {
            CmsRelation relation = (CmsRelation)itRelations.next();
            assertTrue(resourceList.contains(relation.getTargetPath()));
        }
    }

    /**
     * Tests user/group membership.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testMembership() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing user/group membership");

        // this is the current situation:
        // /test/test1 is member of /test/group1 in the /test ou

        assertEquals(3, OpenCms.getOrgUnitManager().getUsers(cms, CmsOrganizationalUnit.SEPARATOR, false).size());
        assertEquals(5, OpenCms.getOrgUnitManager().getUsers(cms, CmsOrganizationalUnit.SEPARATOR, true).size());
        // try to create another user 'test1' in the root ou 
        cms.createUser("/test1", "test1", "test user", null);

        assertEquals(4, OpenCms.getOrgUnitManager().getUsers(cms, CmsOrganizationalUnit.SEPARATOR, false).size());
        assertEquals(6, OpenCms.getOrgUnitManager().getUsers(cms, CmsOrganizationalUnit.SEPARATOR, true).size());

        assertEquals(1, OpenCms.getOrgUnitManager().getUsers(cms, "/test", false).size());
        assertEquals(2, OpenCms.getOrgUnitManager().getUsers(cms, "/test", true).size());
        try {
            // try to create another user 'test1' in the /test ou 
            cms.createUser("/test/test1", "test1", "test user", null);
            fail("it could not be possible to create 2 users with the same name in an ou");
        } catch (CmsVfsException e) {
            // ok, ignore
        }
        assertEquals(1, OpenCms.getOrgUnitManager().getUsers(cms, "/test/", false).size());
        assertEquals(2, OpenCms.getOrgUnitManager().getUsers(cms, "/test/", true).size());

        assertEquals(0, OpenCms.getOrgUnitManager().getGroups(cms, "/test/", false).size());
        assertEquals(0, OpenCms.getOrgUnitManager().getGroups(cms, "/test/", true).size());
        // try to create another group 'group1' in the test ou 
        cms.createGroup("/test/group1", "test group", 0, null);

        assertEquals(7, OpenCms.getOrgUnitManager().getGroups(cms, CmsOrganizationalUnit.SEPARATOR, false).size());
        assertEquals(8, OpenCms.getOrgUnitManager().getGroups(cms, CmsOrganizationalUnit.SEPARATOR, true).size());

        assertEquals(1, OpenCms.getOrgUnitManager().getGroups(cms, "/test", false).size());
        assertEquals(1, OpenCms.getOrgUnitManager().getGroups(cms, "/test", true).size());
        try {
            // try to create another group 'group1' in the /test ou 
            cms.createGroup("/test/group1", "test group", 0, null);
            fail("it could not be possible to create 2 groups with the same name in an ou");
        } catch (CmsVfsException e) {
            // ok, ignore
        }
        assertEquals(1, OpenCms.getOrgUnitManager().getGroups(cms, "/test", false).size());
        assertEquals(1, OpenCms.getOrgUnitManager().getGroups(cms, "/test", true).size());

        cms.addUserToGroup("/test/test1", "/test/group1");

        assertEquals(1, cms.getUsersOfGroup("/test/group1").size());
        assertTrue(cms.getUsersOfGroup("/test/group1").contains(cms.readUser("/test/test1")));
        assertTrue(cms.getGroupsOfUser("/test1", false).isEmpty());
        // add user of root ou to group of 1st level ou
        cms.addUserToGroup("/test1", "/test/group1");
        assertEquals(2, cms.getUsersOfGroup("/test/group1").size());
        assertTrue(cms.getUsersOfGroup("/test/group1").contains(cms.readUser("/test/test1")));
        assertTrue(cms.getUsersOfGroup("/test/group1").contains(cms.readUser("/test1")));
        assertEquals(1, cms.getGroupsOfUser("/test1", false).size());
        assertTrue(cms.getGroupsOfUser("/test1", false).contains(cms.readGroup("/test/group1")));

        assertTrue(cms.getUsersOfGroup("/group1").isEmpty());
        assertEquals(1, cms.getGroupsOfUser("/test/test1", false).size());
        assertTrue(cms.getGroupsOfUser("/test/test1", false).contains(cms.readGroup("/test/group1")));
        // add user of 1st level ou to group of root ou
        cms.addUserToGroup("/test/test1", "/group1");
        assertEquals(1, cms.getUsersOfGroup("/group1").size());
        assertTrue(cms.getUsersOfGroup("/group1").contains(cms.readUser("/test/test1")));
        assertEquals(2, cms.getGroupsOfUser("/test/test1", false).size());
        assertTrue(cms.getGroupsOfUser("/test/test1", false).contains(cms.readGroup("/test/group1")));
        assertTrue(cms.getGroupsOfUser("/test/test1", false).contains(cms.readGroup("/group1")));
    }
}

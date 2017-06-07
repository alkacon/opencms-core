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
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for operations with organizational units.<p>
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class TestOrganizationalUnits extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestOrganizationalUnits(String arg0) {

        super(arg0);
    }

    /**
     * Returns all organizational unit dependent roles.<p>
     *
     * @param ouFqn the organizational unit
     *
     * @return all organizational unit dependent roles
     */
    public static List getOuRoles(String ouFqn) {

        List roles = new ArrayList();
        Iterator itRoles = CmsRole.getSystemRoles().iterator();
        while (itRoles.hasNext()) {
            CmsRole role = (CmsRole)itRoles.next();
            if (!role.isOrganizationalUnitIndependent()) {
                roles.add(role.forOrgUnit(ouFqn));
            }
        }
        return roles;
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

        suite.addTest(new TestOrganizationalUnits("testOuNotFound"));
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
        suite.addTest(new TestOrganizationalUnits("testRoleCacheIssue"));
        suite.addTest(new TestOrganizationalUnits("testBadName"));
        suite.addTest(new TestOrganizationalUnits("testWebuserOU"));

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
     * Tests ou creation with illegal name.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testBadName() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Trying to create an organizational unit with an illegal name");

        String[] badNames = new String[] {"/abc abc", "/\u00E4bc", "/de\u20AC", "/.."};

        for (int i = 0; i < badNames.length; i++) {
            try {
                OpenCms.getOrgUnitManager().createOrganizationalUnit(cms, badNames[i], "bad name test", 0, "/");
                fail("it should not be possible to create an ou with name " + badNames[i]);
            } catch (CmsIllegalArgumentException e) {
                // ok
            }
        }
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
        CmsOrganizationalUnit ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "test");

        // create a deeper ou
        CmsOrganizationalUnit ou2 = OpenCms.getOrgUnitManager().createOrganizationalUnit(
            cms,
            ou.getName() + "test2/",
            "my test ou2",
            0,
            "/folder1");

        // check default project
        CmsProject defProj2 = cms.readProject(ou2.getName() + "Offline");

        // check the ou attributes
        assertEquals(0, ou2.getFlags());
        assertEquals("test/", ou2.getParentFqn());
        assertEquals("test/test2/", ou2.getName());
        assertEquals(cms.readFolder("/system/orgunits/test/test2").getStructureId(), ou2.getId());
        assertEquals(defProj2.getUuid(), ou2.getProjectId());

        // check ou resources
        List ou2Resources = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName());
        assertEquals(1, ou2Resources.size());
        assertTrue(ou2Resources.contains(cms.readResource("/folder1")));

        // check the project resources
        List projRes2 = cms.readProjectResources(defProj2);
        assertEquals(ou2Resources.size(), projRes2.size());
        for (int i = 0; i < projRes2.size(); i++) {
            assertTrue(projRes2.contains(((CmsResource)ou2Resources.get(i)).getRootPath()));
            assertTrue(
                ou2Resources.contains(
                    cms.readResource(cms.getRequestContext().removeSiteRoot((String)projRes2.get(i)))));
        }

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
     * Tests system roles in a deeper level ou.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testDeeperLevelRoles() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing system roles in a deeper level ou");

        // check the ous
        CmsOrganizationalUnit rootOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "");
        CmsOrganizationalUnit flOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "test/");
        CmsOrganizationalUnit dlOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "test/test2/");

        // check just the roles of the root ou
        List myRoles = OpenCms.getRoleManager().getRoles(cms, rootOu.getName(), false);
        assertEquals(CmsRole.getSystemRoles().size(), myRoles.size());
        for (int i = 0; i < myRoles.size(); i++) {
            CmsRole role = (CmsRole)myRoles.get(i);
            assertEquals("", role.getOuFqn());
            assertTrue(myRoles.contains(CmsRole.getSystemRoles().get(i).forOrgUnit("")));
            assertTrue(CmsRole.getSystemRoles().contains(role.forOrgUnit(null)));
        }
        // check all roles
        myRoles = OpenCms.getRoleManager().getRoles(cms, rootOu.getName(), true);
        assertEquals(
            CmsRole.getSystemRoles().size() + getOuRoles(flOu.getName()).size() + getOuRoles(dlOu.getName()).size(),
            myRoles.size());
        List rootRoles = new ArrayList(CmsRole.getSystemRoles().size());
        List flRoles = new ArrayList(getOuRoles(flOu.getName()).size());
        List dlRoles = new ArrayList(getOuRoles(dlOu.getName()).size());
        Iterator itRoles = myRoles.iterator();
        while (itRoles.hasNext()) {
            CmsRole role = (CmsRole)itRoles.next();
            if (role.getOuFqn().equals(rootOu.getName())) {
                rootRoles.add(role);
            } else if (role.getOuFqn().equals(flOu.getName())) {
                flRoles.add(role);
            } else {
                assertEquals(dlOu.getName(), role.getOuFqn());
                dlRoles.add(role);
            }
        }
        for (int i = 0; i < rootRoles.size(); i++) {
            assertTrue(CmsRole.getSystemRoles().contains((((CmsRole)rootRoles.get(i)).forOrgUnit(null))));
            assertTrue(rootRoles.contains(CmsRole.getSystemRoles().get(i).forOrgUnit("")));
        }
        for (int i = 0; i < flRoles.size(); i++) {
            assertTrue(getOuRoles(flOu.getName()).contains(flRoles.get(i)));
            assertTrue(flRoles.contains(getOuRoles(flOu.getName()).get(i)));
        }
        for (int i = 0; i < dlRoles.size(); i++) {
            assertTrue(getOuRoles(dlOu.getName()).contains(dlRoles.get(i)));
            assertTrue(dlRoles.contains(getOuRoles(dlOu.getName()).get(i)));
        }
        // check just the roles of the first level ou
        flRoles = OpenCms.getRoleManager().getRoles(cms, flOu.getName(), false);
        assertEquals(getOuRoles(flOu.getName()).size(), flRoles.size());
        for (int i = 0; i < flRoles.size(); i++) {
            assertTrue(getOuRoles(flOu.getName()).contains(flRoles.get(i)));
            assertTrue(flRoles.contains(getOuRoles(flOu.getName()).get(i)));
        }

        // check all roles in the first level ou and deeper
        myRoles = OpenCms.getRoleManager().getRoles(cms, flOu.getName(), true);
        assertEquals(getOuRoles(flOu.getName()).size() + getOuRoles(dlOu.getName()).size(), myRoles.size());
        flRoles = new ArrayList(getOuRoles(flOu.getName()).size());
        dlRoles = new ArrayList(getOuRoles(dlOu.getName()).size());
        itRoles = myRoles.iterator();
        while (itRoles.hasNext()) {
            CmsRole role = (CmsRole)itRoles.next();
            if (role.getOuFqn().equals(flOu.getName())) {
                flRoles.add(role);
            } else {
                assertEquals(dlOu.getName(), role.getOuFqn());
                dlRoles.add(role);
            }
        }
        for (int i = 0; i < flRoles.size(); i++) {
            assertTrue(getOuRoles(flOu.getName()).contains(flRoles.get(i)));
            assertTrue(flRoles.contains(getOuRoles(flOu.getName()).get(i)));
        }
        for (int i = 0; i < dlRoles.size(); i++) {
            assertTrue(getOuRoles(dlOu.getName()).contains(dlRoles.get(i)));
            assertTrue(dlRoles.contains(getOuRoles(dlOu.getName()).get(i)));
        }
        // check just the roles of the deeper level ou
        dlRoles = OpenCms.getRoleManager().getRoles(cms, dlOu.getName(), false);
        assertEquals(getOuRoles(dlOu.getName()).size(), dlRoles.size());
        for (int i = 0; i < dlRoles.size(); i++) {
            assertTrue(getOuRoles(dlOu.getName()).contains(dlRoles.get(i)));
            assertTrue(dlRoles.contains(getOuRoles(dlOu.getName()).get(i)));
        }
        // check again including (missing) sub-ous
        dlRoles = OpenCms.getRoleManager().getRoles(cms, dlOu.getName(), true);
        assertEquals(getOuRoles(dlOu.getName()).size(), dlRoles.size());
        for (int i = 0; i < dlRoles.size(); i++) {
            assertTrue(getOuRoles(dlOu.getName()).contains(dlRoles.get(i)));
            assertTrue(dlRoles.contains(getOuRoles(dlOu.getName()).get(i)));
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

        OpenCms.getRoleManager().addUserToRole(cms, CmsRole.ROOT_ADMIN, "test/test1");
        cms.loginUser("test/test2/test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        try {
            OpenCms.getOrgUnitManager().deleteOrganizationalUnit(cms, cms.getRequestContext().getOuFqn());
            fail(
                "it should not be possible to delete an organizational unit that is used in the current request context");
        } catch (CmsDbConsistencyException e) {
            // ok, now check the error message
            assertEquals(
                ((CmsDbConsistencyException)e.getCause()).getMessageContainer().getKey(),
                org.opencms.db.Messages.ERR_ORGUNIT_DELETE_IN_CONTEXT_1);
        }
        try {
            OpenCms.getOrgUnitManager().deleteOrganizationalUnit(
                cms,
                cms.getRequestContext().getCurrentUser().getOuFqn());
            fail("it should not be possible to delete an organizational unit that is used by the current user");
        } catch (CmsDbConsistencyException e) {
            // ok, now check the error message
            assertEquals(
                ((CmsDbConsistencyException)e.getCause()).getMessageContainer().getKey(),
                org.opencms.db.Messages.ERR_ORGUNIT_DELETE_IN_CONTEXT_1);
        }
        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        try {
            OpenCms.getOrgUnitManager().deleteOrganizationalUnit(cms, "test");
            fail("it should not be possible to delete an organizational unit that has sub-organizational units");
        } catch (CmsDbConsistencyException e) {
            // ok, now check the error message
            assertEquals(
                ((CmsDbConsistencyException)e.getCause()).getMessageContainer().getKey(),
                org.opencms.db.Messages.ERR_ORGUNIT_DELETE_SUB_ORGUNITS_1);
        }

        CmsOrganizationalUnit ou2 = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "test/test2");
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
            fail("it should not be possible to delete an organizational unit that has users");
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
     * Tests handling with a first level ou.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testFirstLevelOu() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing handling with a first level ou");

        // check the root ou
        CmsOrganizationalUnit rootOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "");

        cms.addUserToGroup("test1", "Administrators");
        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        cms.lockResource("/system/");

        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        // create a first level ou
        CmsOrganizationalUnit ou = OpenCms.getOrgUnitManager().createOrganizationalUnit(
            cms,
            "test/",
            "my test ou",
            0,
            "");

        // check default project
        CmsProject defProj = cms.readProject(ou.getName() + "Offline");

        // check the ou attributes
        assertEquals(0, ou.getFlags());
        assertEquals("", ou.getParentFqn());
        assertEquals("my test ou", ou.getDescription());
        assertEquals("test/", ou.getName());
        assertEquals(cms.readFolder("/system/orgunits/test").getStructureId(), ou.getId());
        assertEquals(defProj.getUuid(), ou.getProjectId());

        // check ou resources
        List ouResources = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou.getName());
        assertEquals(1, ouResources.size());
        assertTrue(ouResources.contains(cms.readResource("/")));

        // check the project resources
        List projRes = cms.readProjectResources(defProj);
        assertEquals(ouResources.size(), projRes.size());
        for (int i = 0; i < projRes.size(); i++) {
            assertTrue(projRes.contains(((CmsResource)ouResources.get(i)).getRootPath()));
            assertTrue(
                ouResources.contains(cms.readResource(cms.getRequestContext().removeSiteRoot((String)projRes.get(i)))));
        }

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
        assertEquals("", ou.getParentFqn());
        assertEquals("new ou description", ou.getDescription());
        assertEquals("test/", ou.getName());
        assertEquals(cms.readFolder("/system/orgunits/test").getStructureId(), ou.getId());
        assertTrue(cms.readFolder("/system/orgunits/test").isInternal());
        assertEquals(defProj.getUuid(), ou.getProjectId());

        ou.setFlags(0);
        OpenCms.getOrgUnitManager().writeOrganizationalUnit(cms, ou);

        ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, ou.getName());
        assertEquals(0, ou.getFlags());
        assertTrue(cms.readFolder("/system/orgunits/test").isInternal());

        // create a first level ou
        CmsOrganizationalUnit ou2 = OpenCms.getOrgUnitManager().createOrganizationalUnit(
            cms,
            "test2/",
            "my test ou2 in the root",
            0,
            "");

        // check default project
        CmsProject defProj2 = cms.readProject(ou2.getName() + "Offline");

        // check the ou attributes
        assertEquals(0, ou2.getFlags());
        assertEquals("", ou2.getParentFqn());
        assertEquals("my test ou2 in the root", ou2.getDescription());
        assertEquals("test2/", ou2.getName());
        assertEquals(cms.readFolder("/system/orgunits/test2").getStructureId(), ou2.getId());
        assertEquals(defProj2.getUuid(), ou2.getProjectId());

        // check ou resources
        List ou2Resources = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName());
        assertEquals(1, ou2Resources.size());
        assertTrue(ou2Resources.contains(cms.readResource("/")));

        // check the project resources
        List projRes2 = cms.readProjectResources(defProj2);
        assertEquals(ou2Resources.size(), projRes2.size());
        for (int i = 0; i < projRes2.size(); i++) {
            assertTrue(projRes2.contains(((CmsResource)ou2Resources.get(i)).getRootPath()));
            assertTrue(
                ou2Resources.contains(
                    cms.readResource(cms.getRequestContext().removeSiteRoot((String)projRes2.get(i)))));
        }

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

        try {
            cms.readProject(defProj2.getUuid());
            fail("it should not be possible to read the default project of a deleted ou");
        } catch (CmsDataAccessException e) {
            // ok, ignore
        }

        try {
            cms.readProject(defProj2.getName());
            fail("it should not be possible to read the default project of a deleted ou");
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
        List groups = OpenCms.getOrgUnitManager().getGroups(cms, ou.getName(), false);
        assertEquals(1, groups.size());
        assertTrue(groups.contains(cms.readGroup(ou.getName() + OpenCms.getDefaultUsers().getGroupUsers())));
        groups = OpenCms.getOrgUnitManager().getGroups(cms, ou.getName(), true);
        assertEquals(1, groups.size());
        assertTrue(groups.contains(cms.readGroup(ou.getName() + OpenCms.getDefaultUsers().getGroupUsers())));
        assertTrue(OpenCms.getOrgUnitManager().getUsers(cms, ou.getName(), false).isEmpty());
        assertTrue(OpenCms.getOrgUnitManager().getUsers(cms, ou.getName(), true).isEmpty());

        // check the root ou principals again
        List rootGroups = OpenCms.getOrgUnitManager().getGroups(cms, rootOu.getName(), false);
        assertEquals(7, rootGroups.size());
        assertTrue(rootGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupAdministrators())));
        assertTrue(rootGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupUsers())));
        assertTrue(rootGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupGuests())));
        assertTrue(rootGroups.contains(cms.readGroup("group1")));
        assertTrue(rootGroups.contains(cms.readGroup("group2")));
        assertTrue(rootGroups.contains(cms.readGroup("group3")));

        List rootSubGroups = OpenCms.getOrgUnitManager().getGroups(cms, rootOu.getName(), true);
        assertEquals(8, rootSubGroups.size());
        assertTrue(rootSubGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupAdministrators())));
        assertTrue(rootSubGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupUsers())));
        assertTrue(rootSubGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupGuests())));
        assertTrue(rootSubGroups.contains(cms.readGroup("group1")));
        assertTrue(rootSubGroups.contains(cms.readGroup("group2")));
        assertTrue(rootSubGroups.contains(cms.readGroup("group3")));
        assertTrue(rootSubGroups.contains(cms.readGroup(ou.getName() + OpenCms.getDefaultUsers().getGroupUsers())));

        List rootUsers = OpenCms.getOrgUnitManager().getUsers(cms, rootOu.getName(), false);
        assertEquals(5, rootUsers.size());
        assertTrue(rootUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserAdmin())));
        assertTrue(rootUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserExport())));
        assertTrue(rootUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserGuest())));
        assertTrue(rootUsers.contains(cms.readUser("test1")));
        assertTrue(rootUsers.contains(cms.readUser("test2")));

        List rootSubUsers = OpenCms.getOrgUnitManager().getUsers(cms, rootOu.getName(), true);
        assertEquals(5, rootSubUsers.size());
        assertTrue(rootSubUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserAdmin())));
        assertTrue(rootSubUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserExport())));
        assertTrue(rootSubUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserGuest())));
        assertTrue(rootSubUsers.contains(cms.readUser("test1")));
        assertTrue(rootSubUsers.contains(cms.readUser("test2")));
        cms.removeUserFromGroup("test1", "Administrators");
        assertLock(cms, "/system/", CmsLockType.EXCLUSIVE, cms.readUser("test1"));
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
        CmsOrganizationalUnit rootOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "");
        CmsOrganizationalUnit flOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "test/");

        // check just the roles of the root ou
        List myRoles = OpenCms.getRoleManager().getRoles(cms, rootOu.getName(), false);
        assertEquals(CmsRole.getSystemRoles().size(), myRoles.size());
        for (int i = 0; i < myRoles.size(); i++) {
            CmsRole role = (CmsRole)myRoles.get(i);
            assertEquals("", role.getOuFqn());
            assertTrue(myRoles.contains(CmsRole.getSystemRoles().get(i).forOrgUnit("")));
            assertTrue(CmsRole.getSystemRoles().contains(role.forOrgUnit(null)));
        }
        // check all roles
        myRoles = OpenCms.getRoleManager().getRoles(cms, rootOu.getName(), true);
        assertEquals(CmsRole.getSystemRoles().size() + getOuRoles(flOu.getName()).size(), myRoles.size());
        for (int i = 0; i < myRoles.size(); i++) {
            CmsRole role = (CmsRole)myRoles.get(i);
            if (!role.getOuFqn().equals(rootOu.getName())) {
                assertEquals(flOu.getName(), role.getOuFqn());
                if (i < CmsRole.getSystemRoles().size()) {
                    assertTrue(myRoles.contains(CmsRole.getSystemRoles().get(i).forOrgUnit(flOu.getName())));
                }
            } else {
                assertEquals(rootOu.getName(), role.getOuFqn());
                if (i < CmsRole.getSystemRoles().size()) {
                    assertTrue(myRoles.contains(CmsRole.getSystemRoles().get(i).forOrgUnit(rootOu.getName())));
                }
            }
            assertTrue(CmsRole.getSystemRoles().contains(role.forOrgUnit(null)));
        }
        // check just the roles of the first level ou
        myRoles = OpenCms.getRoleManager().getRoles(cms, flOu.getName(), false);
        assertEquals(getOuRoles(flOu.getName()).size(), myRoles.size());
        for (int i = 0; i < myRoles.size(); i++) {
            CmsRole role = (CmsRole)myRoles.get(i);
            assertEquals(flOu.getName(), role.getOuFqn());
            assertTrue(myRoles.contains(((CmsRole)getOuRoles(flOu.getName()).get(i)).forOrgUnit(flOu.getName())));
            assertTrue(getOuRoles(flOu.getName()).contains(role.forOrgUnit(flOu.getName())));
        }
        // check again including (missing) sub-ous
        myRoles = OpenCms.getRoleManager().getRoles(cms, flOu.getName(), true);
        assertEquals(getOuRoles(flOu.getName()).size(), myRoles.size());
        for (int i = 0; i < myRoles.size(); i++) {
            CmsRole role = (CmsRole)myRoles.get(i);
            assertEquals(flOu.getName(), role.getOuFqn());
            assertTrue(myRoles.contains(((CmsRole)getOuRoles(flOu.getName()).get(i)).forOrgUnit(flOu.getName())));
            assertTrue(getOuRoles(flOu.getName()).contains(role.forOrgUnit(flOu.getName())));
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

        assertEquals(3, OpenCms.getOrgUnitManager().getUsers(cms, "", false).size());
        assertEquals(5, OpenCms.getOrgUnitManager().getUsers(cms, "", true).size());
        // try to create another user 'test1' in the root ou
        cms.createUser("test1", "test1", "test user", null);

        assertEquals(4, OpenCms.getOrgUnitManager().getUsers(cms, "", false).size());
        assertEquals(6, OpenCms.getOrgUnitManager().getUsers(cms, "", true).size());

        assertEquals(1, OpenCms.getOrgUnitManager().getUsers(cms, "test", false).size());
        assertEquals(2, OpenCms.getOrgUnitManager().getUsers(cms, "test", true).size());
        try {
            // try to create another user 'test1' in the /test ou
            cms.createUser("test/test1", "test1", "test user", null);
            fail("it could not be possible to create 2 users with the same name in an ou");
        } catch (CmsVfsException e) {
            // ok, ignore
        }
        assertEquals(1, OpenCms.getOrgUnitManager().getUsers(cms, "test/", false).size());
        assertEquals(2, OpenCms.getOrgUnitManager().getUsers(cms, "test/", true).size());

        assertEquals(1, OpenCms.getOrgUnitManager().getGroups(cms, "test/", false).size());
        assertEquals(2, OpenCms.getOrgUnitManager().getGroups(cms, "test/", true).size());
        // try to create another group 'group1' in the test ou
        cms.createGroup("test/group1", "test group", 0, null);

        assertEquals(7, OpenCms.getOrgUnitManager().getGroups(cms, "", false).size());
        assertEquals(11, OpenCms.getOrgUnitManager().getGroups(cms, "", true).size());

        assertEquals(2, OpenCms.getOrgUnitManager().getGroups(cms, "test", false).size());
        assertEquals(3, OpenCms.getOrgUnitManager().getGroups(cms, "test", true).size());
        try {
            // try to create another group 'group1' in the /test ou
            cms.createGroup("test/group1", "test group", 0, null);
            fail("it could not be possible to create 2 groups with the same name in an ou");
        } catch (CmsVfsException e) {
            // ok, ignore
        }
        assertEquals(2, OpenCms.getOrgUnitManager().getGroups(cms, "test", false).size());
        assertEquals(3, OpenCms.getOrgUnitManager().getGroups(cms, "test", true).size());

        cms.addUserToGroup("test/test1", "test/group1");

        assertEquals(1, cms.getUsersOfGroup("test/group1").size());
        assertTrue(cms.getUsersOfGroup("test/group1").contains(cms.readUser("test/test1")));
        assertTrue(cms.getGroupsOfUser("test1", false).isEmpty());
        // add user of root ou to group of 1st level ou
        cms.addUserToGroup("test1", "test/group1");
        assertEquals(2, cms.getUsersOfGroup("test/group1").size());
        assertTrue(cms.getUsersOfGroup("test/group1").contains(cms.readUser("test/test1")));
        assertTrue(cms.getUsersOfGroup("test/group1").contains(cms.readUser("test1")));
        assertEquals(1, cms.getGroupsOfUser("test1", false).size());
        assertTrue(cms.getGroupsOfUser("test1", false).contains(cms.readGroup("test/group1")));

        assertTrue(cms.getUsersOfGroup("group1").isEmpty());
        assertEquals(3, cms.getGroupsOfUser("test/test1", true).size());
        assertTrue(cms.getGroupsOfUser("test/test1", true).contains(cms.readGroup("test/group1")));
        // add user of 1st level ou to group of root ou
        cms.addUserToGroup("test/test1", "group1");
        assertEquals(1, cms.getUsersOfGroup("group1").size());
        assertTrue(cms.getUsersOfGroup("group1").contains(cms.readUser("test/test1")));
        assertEquals(4, cms.getGroupsOfUser("test/test1", true).size());
        assertTrue(cms.getGroupsOfUser("test/test1", true).contains(cms.readGroup("test/group1")));
        assertTrue(cms.getGroupsOfUser("test/test1", true).contains(cms.readGroup("group1")));
    }

    /**
     * Tests exception when ou not found.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testOuNotFound() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Trying to read an unexistant ou");

        try {
            OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "/testOuNotFound/");
        } catch (CmsDbEntryNotFoundException e) {
            // ok, this is the expected exception
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

        CmsUser user = cms.readUser("test/test1");

        // check preconditions
        assertEquals(
            3,
            OpenCms.getRoleManager().getOrgUnitsForRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit(""), true).size());

        cms.loginUser(user.getName(), "test1");
        List ous = OpenCms.getRoleManager().getOrgUnitsForRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit(""), true);
        assertTrue(ous.isEmpty());

        // create a new ou
        cms = getCmsObject();
        OpenCms.getRoleManager().addUserToRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit("test/"), user.getName());

        // check it
        cms.loginUser(user.getName(), "test1");
        ous = OpenCms.getRoleManager().getOrgUnitsForRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit(""), true);
        assertEquals(2, ous.size());
        assertTrue(ous.contains(OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "test")));
        assertTrue(ous.contains(OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "test/test2")));

        // create a new ou
        cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("");
        OpenCms.getOrgUnitManager().createOrganizationalUnit(cms, "test3", "test3", 0, "/system/");
        // set it up
        OpenCms.getRoleManager().addUserToRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit("test3/"), user.getName());

        // check the result
        assertEquals(
            4,
            OpenCms.getRoleManager().getOrgUnitsForRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit(""), true).size());

        cms.loginUser(user.getName(), "test1");
        ous = OpenCms.getRoleManager().getOrgUnitsForRole(cms, CmsRole.ADMINISTRATOR.forOrgUnit(""), true);
        assertEquals(3, ous.size());
        assertTrue(ous.contains(OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "test")));
        assertTrue(ous.contains(OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "test/test2")));
        assertTrue(ous.contains(OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "test3")));
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
        CmsOrganizationalUnit ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "test");
        CmsOrganizationalUnit ou2 = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, ou.getName() + "test2");

        // check offline project
        CmsFolder rootOuFolder = cms.readFolder("/system/orgunits/");
        assertEquals(rootOu.getId(), rootOuFolder.getStructureId());
        assertEquals(rootOu.getFlags(), rootOuFolder.getFlags() & ~CmsResource.FLAG_INTERNAL);
        assertTrue(rootOuFolder.getState().isUnchanged());
        assertTrue(rootOuFolder.isInternal());
        assertEquals(
            rootOu.getDescription(),
            cms.readPropertyObject(rootOuFolder, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false).getValue());
        assertOrgUnitResources(cms, rootOu);

        CmsFolder ouFolder = cms.readFolder("/system/orgunits/test");
        assertEquals(ou.getId(), ouFolder.getStructureId());
        assertEquals(ou.getFlags(), ouFolder.getFlags() & ~CmsResource.FLAG_INTERNAL);
        assertTrue(ouFolder.getState().isUnchanged());
        assertTrue(ouFolder.isInternal());
        assertEquals(
            ou.getDescription(),
            cms.readPropertyObject(ouFolder, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false).getValue());
        assertOrgUnitResources(cms, ou);

        CmsFolder ou2Folder = cms.readFolder("/system/orgunits/test/test2");
        assertEquals(ou2.getId(), ou2Folder.getStructureId());
        assertEquals(ou2.getFlags(), ou2Folder.getFlags() & ~CmsResource.FLAG_INTERNAL);
        assertTrue(ou2Folder.getState().isUnchanged());
        assertTrue(ou2Folder.isInternal());
        assertEquals(
            ou2.getDescription(),
            cms.readPropertyObject(ou2Folder, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false).getValue());
        assertOrgUnitResources(cms, ou2);

        // now check the online project
        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
        rootOuFolder = cms.readFolder("/system/orgunits/");
        assertEquals(rootOu.getId(), rootOuFolder.getStructureId());
        assertEquals(rootOu.getFlags(), rootOuFolder.getFlags() & ~CmsResource.FLAG_INTERNAL);
        assertTrue(rootOuFolder.getState().isUnchanged());
        assertTrue(rootOuFolder.isInternal());
        assertEquals(
            rootOu.getDescription(),
            cms.readPropertyObject(rootOuFolder, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false).getValue());
        assertOrgUnitResources(cms, rootOu);

        ouFolder = cms.readFolder("/system/orgunits/test");
        assertEquals(ou.getId(), ouFolder.getStructureId());
        assertEquals(ou.getFlags(), ouFolder.getFlags() & ~CmsResource.FLAG_INTERNAL);
        assertTrue(ouFolder.getState().isUnchanged());
        assertTrue(ouFolder.isInternal());
        assertEquals(
            ou.getDescription(),
            cms.readPropertyObject(ouFolder, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false).getValue());
        assertOrgUnitResources(cms, ou);

        ou2Folder = cms.readFolder("/system/orgunits/test/test2");
        assertEquals(ou2.getId(), ou2Folder.getStructureId());
        assertEquals(ou2.getFlags(), ou2Folder.getFlags() & ~CmsResource.FLAG_INTERNAL);
        assertTrue(ou2Folder.getState().isUnchanged());
        assertTrue(ou2Folder.isInternal());
        assertEquals(
            ou2.getDescription(),
            cms.readPropertyObject(ou2Folder, CmsPropertyDefinition.PROPERTY_DESCRIPTION, false).getValue());
        assertOrgUnitResources(cms, ou2);
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
        CmsOrganizationalUnit ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "test");
        CmsOrganizationalUnit ou2 = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, ou.getName() + "test2");

        try {
            // try move to move /test1 to /test/test1
            OpenCms.getOrgUnitManager().setUsersOrganizationalUnit(cms, ou.getName(), "test1");
            fail("it should not be possible to move the user to other ou (since it is still member of a group)");
        } catch (CmsDbConsistencyException e) {
            // ok, ignore
        }
        cms.removeUserFromGroup("test1", "group1");
        // move to move /test1 to /test/test1
        OpenCms.getOrgUnitManager().setUsersOrganizationalUnit(cms, ou.getName(), "test1");

        try {
            cms.readUser("test1");
            fail("it should not be possible to read the user in the old ou");
        } catch (CmsDbEntryNotFoundException e) {
            // ok, ignore
        }
        cms.readUser("test/test1");
        assertTrue(cms.getGroupsOfUser("test/test1", false).isEmpty());

        List rootUsers = OpenCms.getOrgUnitManager().getUsers(cms, rootOu.getName(), false);
        assertEquals(4, rootUsers.size());
        assertFalse(rootUsers.contains(cms.readUser("test/test1")));
        List rootSubusers = OpenCms.getOrgUnitManager().getUsers(cms, rootOu.getName(), true);
        assertEquals(5, rootSubusers.size());
        assertTrue(rootSubusers.contains(cms.readUser("test/test1")));

        List ouUsers = OpenCms.getOrgUnitManager().getUsers(cms, ou.getName(), false);
        assertEquals(1, ouUsers.size());
        assertTrue(ouUsers.contains(cms.readUser("test/test1")));
        List ouSubusers = OpenCms.getOrgUnitManager().getUsers(cms, ou.getName(), true);
        assertEquals(1, ouSubusers.size());
        assertTrue(ouSubusers.contains(cms.readUser("test/test1")));

        cms.removeUserFromGroup("test2", "Users");
        cms.removeUserFromGroup("test2", "group2");
        // move to move /test2 to /test/test2/test2
        OpenCms.getOrgUnitManager().setUsersOrganizationalUnit(cms, ou2.getName(), "test2");

        try {
            cms.readUser("test2");
            fail("it should not be possible to read the user in the old ou");
        } catch (CmsDbEntryNotFoundException e) {
            // ok, ignore
        }
        cms.readUser("test/test2/test2");
        assertTrue(cms.getGroupsOfUser("test/test2/test2", false).isEmpty());

        rootUsers = OpenCms.getOrgUnitManager().getUsers(cms, rootOu.getName(), false);
        assertEquals(3, rootUsers.size());
        assertFalse(rootUsers.contains(cms.readUser("test/test1")));
        assertFalse(rootUsers.contains(cms.readUser("test/test2/test2")));

        rootSubusers = OpenCms.getOrgUnitManager().getUsers(cms, rootOu.getName(), true);
        assertEquals(5, rootSubusers.size());
        assertTrue(rootSubusers.contains(cms.readUser("test/test1")));
        assertTrue(rootSubusers.contains(cms.readUser("test/test2/test2")));

        List ouGroups = OpenCms.getOrgUnitManager().getGroups(cms, ou.getName(), false);
        assertEquals(1, ouGroups.size());
        assertTrue(ouGroups.contains(cms.readGroup(ou.getName() + OpenCms.getDefaultUsers().getGroupUsers())));

        List ouSubgroups = OpenCms.getOrgUnitManager().getGroups(cms, ou.getName(), true);
        assertEquals(2, ouSubgroups.size());
        assertTrue(ouSubgroups.contains(cms.readGroup(ou.getName() + OpenCms.getDefaultUsers().getGroupUsers())));
        assertTrue(ouSubgroups.contains(cms.readGroup(ou2.getName() + OpenCms.getDefaultUsers().getGroupUsers())));

        ouUsers = OpenCms.getOrgUnitManager().getUsers(cms, ou.getName(), false);
        assertEquals(1, ouUsers.size());
        assertTrue(ouUsers.contains(cms.readUser("test/test1")));

        ouSubusers = OpenCms.getOrgUnitManager().getUsers(cms, ou.getName(), true);
        assertEquals(2, ouSubusers.size());
        assertTrue(ouSubusers.contains(cms.readUser("test/test1")));
        assertTrue(ouSubusers.contains(cms.readUser("test/test2/test2")));
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
        CmsOrganizationalUnit ou = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "test");
        CmsOrganizationalUnit ou2 = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, "test/test2");

        try {
            OpenCms.getOrgUnitManager().createOrganizationalUnit(cms, "/doesnotexist/test2/", "my test ou2", 0, "");
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
        assertTrue(
            OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).contains(
                cms.readResource("/folder1")));
        CmsOrganizationalUnit ou3 = OpenCms.getOrgUnitManager().createOrganizationalUnit(
            cms,
            ou2.getName() + "test3",
            "it will not last too long",
            0,
            "/folder1");
        assertEquals(1, OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou3.getName()).size());
        assertTrue(
            OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou3.getName()).contains(
                cms.readResource("/folder1")));
        OpenCms.getOrgUnitManager().deleteOrganizationalUnit(cms, ou3.getName());

        // test removing all resources
        assertEquals(1, OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).size());
        assertTrue(
            OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).contains(
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
            assertTrue(
                OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).contains(
                    cms.readResource("/folder1")));
        }

        // check default project
        CmsProject defProj = cms.readProject(ou.getName() + "Offline");

        // check the project resources
        List projRes = cms.readProjectResources(defProj);
        List ouResources = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou.getName());
        assertEquals(ouResources.size(), projRes.size());
        for (int i = 0; i < projRes.size(); i++) {
            assertTrue(projRes.contains(((CmsResource)ouResources.get(i)).getRootPath()));
            assertTrue(
                ouResources.contains(cms.readResource(cms.getRequestContext().removeSiteRoot((String)projRes.get(i)))));
        }

        try {
            OpenCms.getOrgUnitManager().addResourceToOrgUnit(cms, ou.getName(), "/sites/doesnotexist");
            fail("should not be possible to add an unexistent resource to an ou");
        } catch (CmsDataAccessException e) {
            // ok, just be sure
            assertEquals(1, OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou.getName()).size());
            assertTrue(
                OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou.getName()).contains(
                    cms.readResource("/")));
        }
        // check the project resources
        projRes = cms.readProjectResources(defProj);
        ouResources = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou.getName());
        assertEquals(ouResources.size(), projRes.size());
        for (int i = 0; i < projRes.size(); i++) {
            assertTrue(projRes.contains(((CmsResource)ouResources.get(i)).getRootPath()));
            assertTrue(
                ouResources.contains(cms.readResource(cms.getRequestContext().removeSiteRoot((String)projRes.get(i)))));
        }

        // add an additional resource
        OpenCms.getOrgUnitManager().addResourceToOrgUnit(cms, ou2.getName(), "/folder2");
        // check ou resources again
        assertEquals(2, OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).size());
        assertTrue(
            OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).contains(
                cms.readResource("/folder1")));
        assertTrue(
            OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).contains(
                cms.readResource("/folder2")));

        // check the project resources
        CmsProject defProj2 = cms.readProject(ou2.getName() + "Offline");
        List projRes2 = cms.readProjectResources(defProj2);
        List ou2Resources = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName());
        assertEquals(ou2Resources.size(), projRes2.size());
        for (int i = 0; i < projRes2.size(); i++) {
            assertTrue(projRes2.contains(((CmsResource)ou2Resources.get(i)).getRootPath()));
            assertTrue(
                ou2Resources.contains(
                    cms.readResource(cms.getRequestContext().removeSiteRoot((String)projRes2.get(i)))));
        }

        // resource remotion tests
        try {
            OpenCms.getOrgUnitManager().removeResourceFromOrgUnit(cms, ou2.getName(), "/folder1/index.html");
            fail("should not be possible to remove an not associated resource from an ou");
        } catch (CmsDataAccessException e) {
            // ok, just be sure
            assertEquals(2, OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).size());
            assertTrue(
                OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).contains(
                    cms.readResource("/folder1")));
            assertTrue(
                OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).contains(
                    cms.readResource("/folder2")));
        }
        // check the project resources
        projRes2 = cms.readProjectResources(defProj2);
        ou2Resources = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName());
        assertEquals(ou2Resources.size(), projRes2.size());
        for (int i = 0; i < projRes2.size(); i++) {
            assertTrue(projRes2.contains(((CmsResource)ou2Resources.get(i)).getRootPath()));
            assertTrue(
                ou2Resources.contains(
                    cms.readResource(cms.getRequestContext().removeSiteRoot((String)projRes2.get(i)))));
        }

        // remove a resource
        OpenCms.getOrgUnitManager().removeResourceFromOrgUnit(cms, ou2.getName(), "/folder1");
        // check ou resource
        assertEquals(1, OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).size());
        assertTrue(
            OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName()).contains(
                cms.readResource("/folder2")));
        // check the project resources
        projRes2 = cms.readProjectResources(defProj2);
        ou2Resources = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou2.getName());
        assertEquals(ou2Resources.size(), projRes2.size());
        for (int i = 0; i < projRes2.size(); i++) {
            assertTrue(projRes2.contains(((CmsResource)ou2Resources.get(i)).getRootPath()));
            assertTrue(
                ou2Resources.contains(
                    cms.readResource(cms.getRequestContext().removeSiteRoot((String)projRes2.get(i)))));
        }
    }

    /**
     * Tests role cache issue after creating a new ou.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testRoleCacheIssue() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Test role cache issue after creating a new ou");

        // ensure the roles are cached
        List roles = OpenCms.getRoleManager().getRolesOfUser(
            cms,
            cms.getRequestContext().getCurrentUser().getName(),
            "",
            true,
            false,
            true);

        // create a new ou
        OpenCms.getOrgUnitManager().createOrganizationalUnit(cms, "/testRoleIssue", "Test role cache issue", 0, "/");

        List newRoles = OpenCms.getRoleManager().getRolesOfUser(
            cms,
            cms.getRequestContext().getCurrentUser().getName(),
            "",
            true,
            false,
            true);

        assertTrue(newRoles.size() > roles.size());
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
        CmsResource rootRes = (ouMan.getResourcesForOrganizationalUnit(cms, rootOu.getName()).get(0)); // /
        CmsOrganizationalUnit ou = ouMan.readOrganizationalUnit(cms, "test");
        CmsResource ouRes = (ouMan.getResourcesForOrganizationalUnit(cms, ou.getName()).get(0)); // /sites/default/
        CmsOrganizationalUnit ou2 = ouMan.readOrganizationalUnit(cms, ou.getName() + "test2");
        CmsResource ou2Res = (ouMan.getResourcesForOrganizationalUnit(cms, ou2.getName()).get(0)); // /sites/default/folder2/
        CmsUser user = cms.readUser("test/test1");
        // check preconditions
        assertFalse(
            roleMan.hasRoleForResource(
                cms,
                user.getName(),
                CmsRole.ACCOUNT_MANAGER,
                cms.getRequestContext().getSitePath(rootRes)));
        assertFalse(
            roleMan.hasRoleForResource(
                cms,
                user.getName(),
                CmsRole.ACCOUNT_MANAGER,
                cms.getRequestContext().getSitePath(ouRes)));
        assertFalse(
            roleMan.hasRoleForResource(
                cms,
                user.getName(),
                CmsRole.ACCOUNT_MANAGER,
                cms.getRequestContext().getSitePath(ou2Res)));

        assertTrue(
            roleMan.hasRoleForResource(cms, CmsRole.ACCOUNT_MANAGER, cms.getRequestContext().getSitePath(rootRes)));
        assertTrue(
            roleMan.hasRoleForResource(cms, CmsRole.ACCOUNT_MANAGER, cms.getRequestContext().getSitePath(ouRes)));
        assertTrue(
            roleMan.hasRoleForResource(cms, CmsRole.ACCOUNT_MANAGER, cms.getRequestContext().getSitePath(ou2Res)));

        assertFalse(roleMan.hasRole(cms, user.getName(), CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou.getName())));
        assertTrue(roleMan.getRolesOfUser(cms, user.getName(), ou.getName(), true, false, false).isEmpty());
        assertEquals(
            1,
            roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou.getName()), true, false).size());
        assertTrue(
            roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou.getName()), true, false).contains(
                cms.readUser(OpenCms.getDefaultUsers().getUserAdmin())));
        // add user to role
        roleMan.addUserToRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou.getName()), user.getName());
        // check role in the given ou
        assertTrue(roleMan.hasRole(cms, user.getName(), CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou.getName())));
        assertEquals(6, roleMan.getRolesOfUser(cms, user.getName(), ou.getName(), false, false, false).size());
        assertTrue(
            roleMan.getRolesOfUser(cms, user.getName(), ou.getName(), false, false, false).contains(
                CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou.getName())));
        assertTrue(
            roleMan.getRolesOfUser(cms, user.getName(), ou.getName(), false, false, false).contains(
                CmsRole.WORKPLACE_USER.forOrgUnit(ou.getName())));
        assertEquals(1, roleMan.getRolesOfUser(cms, user.getName(), ou.getName(), false, true, false).size());
        assertTrue(
            roleMan.getRolesOfUser(cms, user.getName(), ou.getName(), false, true, false).contains(
                CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou.getName())));
        assertFalse(
            roleMan.getRolesOfUser(cms, user.getName(), ou.getName(), false, true, false).contains(
                CmsRole.WORKPLACE_USER.forOrgUnit(ou.getName())));
        assertEquals(12, roleMan.getRolesOfUser(cms, user.getName(), ou.getName(), true, false, false).size());
        assertTrue(
            roleMan.getRolesOfUser(cms, user.getName(), ou.getName(), true, false, false).contains(
                CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou.getName())));
        assertTrue(
            roleMan.getRolesOfUser(cms, user.getName(), ou.getName(), true, false, false).contains(
                CmsRole.WORKPLACE_USER.forOrgUnit(ou.getName())));
        assertEquals(1, roleMan.getRolesOfUser(cms, user.getName(), ou.getName(), true, true, false).size());
        assertTrue(
            roleMan.getRolesOfUser(cms, user.getName(), ou.getName(), true, true, false).contains(
                CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou.getName())));
        assertFalse(
            roleMan.getRolesOfUser(cms, user.getName(), ou.getName(), true, true, false).contains(
                CmsRole.WORKPLACE_USER.forOrgUnit(ou.getName())));
        assertEquals(
            1,
            roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou.getName()), false, false).size());
        assertTrue(
            roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou.getName()), false, false).contains(user));
        assertEquals(
            1,
            roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou.getName()), false, true).size());
        assertTrue(
            roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou.getName()), false, true).contains(user));
        assertEquals(
            2,
            roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou.getName()), true, false).size());
        assertTrue(
            roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou.getName()), true, false).contains(user));
        assertEquals(
            1,
            roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou.getName()), true, true).size());
        assertTrue(
            roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou.getName()), true, false).contains(user));
        // check role in deeper ou
        assertTrue(roleMan.hasRole(cms, user.getName(), CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou2.getName())));
        assertTrue(roleMan.getRolesOfUser(cms, user.getName(), ou2.getName(), false, false, false).isEmpty());
        assertTrue(roleMan.getRolesOfUser(cms, user.getName(), ou2.getName(), false, true, false).isEmpty());
        assertEquals(11, roleMan.getRolesOfUser(cms, user.getName(), ou2.getName(), true, false, false).size());
        assertTrue(roleMan.getRolesOfUser(cms, user.getName(), ou2.getName(), true, true, false).isEmpty());
        assertEquals(
            0,
            roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou2.getName()), false, false).size());
        assertTrue(
            roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou2.getName()), false, true).isEmpty());
        assertEquals(
            2,
            roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou2.getName()), true, false).size());
        assertTrue(
            roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou2.getName()), true, false).contains(
                cms.readUser(OpenCms.getDefaultUsers().getUserAdmin())));
        assertTrue(
            roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou2.getName()), true, true).isEmpty());
        // check role in higher ou
        assertFalse(roleMan.hasRole(cms, user.getName(), CmsRole.ACCOUNT_MANAGER.forOrgUnit(rootOu.getName())));
        assertEquals(12, roleMan.getRolesOfUser(cms, user.getName(), rootOu.getName(), true, false, false).size());
        assertTrue(
            roleMan.getRolesOfUser(cms, user.getName(), rootOu.getName(), true, false, false).contains(
                CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou.getName())));
        assertTrue(roleMan.getRolesOfUser(cms, user.getName(), rootOu.getName(), false, false, false).isEmpty());
        assertEquals(
            1,
            roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(rootOu.getName()), true, false).size());
        assertTrue(
            roleMan.getUsersOfRole(cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(rootOu.getName()), true, false).contains(
                cms.readUser(OpenCms.getDefaultUsers().getUserAdmin())));
        // check resources
        assertFalse(
            roleMan.hasRoleForResource(
                cms,
                user.getName(),
                CmsRole.ACCOUNT_MANAGER,
                cms.getRequestContext().getSitePath(rootRes)));
        assertTrue(
            roleMan.hasRoleForResource(
                cms,
                user.getName(),
                CmsRole.ACCOUNT_MANAGER,
                cms.getRequestContext().getSitePath(ouRes)));
        assertTrue(
            roleMan.hasRoleForResource(
                cms,
                user.getName(),
                CmsRole.ACCOUNT_MANAGER,
                cms.getRequestContext().getSitePath(ou2Res)));
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
        assertEquals("root organizational unit", rootOu.getDescription(Locale.ENGLISH));
        assertEquals("", rootOu.getName());
        assertEquals(CmsUUID.getNullUUID(), rootOu.getProjectId());
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
        assertTrue(rootGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupUsers())));
        assertTrue(rootGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupGuests())));
        assertTrue(rootGroups.contains(cms.readGroup("group1")));
        assertTrue(rootGroups.contains(cms.readGroup("group2")));
        assertTrue(rootGroups.contains(cms.readGroup("group3")));

        List rootSubGroups = OpenCms.getOrgUnitManager().getGroups(cms, rootOu.getName(), true);
        assertEquals(7, rootSubGroups.size());
        assertTrue(rootSubGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupAdministrators())));
        assertTrue(rootSubGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupUsers())));
        assertTrue(rootSubGroups.contains(cms.readGroup(OpenCms.getDefaultUsers().getGroupGuests())));
        assertTrue(rootSubGroups.contains(cms.readGroup("group1")));
        assertTrue(rootSubGroups.contains(cms.readGroup("group2")));
        assertTrue(rootSubGroups.contains(cms.readGroup("group3")));

        List rootUsers = OpenCms.getOrgUnitManager().getUsers(cms, rootOu.getName(), false);
        assertEquals(5, rootUsers.size());
        assertTrue(rootUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserAdmin())));
        assertTrue(rootUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserExport())));
        assertTrue(rootUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserGuest())));
        assertTrue(rootUsers.contains(cms.readUser("test1")));
        assertTrue(rootUsers.contains(cms.readUser("test2")));

        List rootSubUsers = OpenCms.getOrgUnitManager().getUsers(cms, rootOu.getName(), true);
        assertEquals(5, rootSubUsers.size());
        assertTrue(rootSubUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserAdmin())));
        assertTrue(rootSubUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserExport())));
        assertTrue(rootSubUsers.contains(cms.readUser(OpenCms.getDefaultUsers().getUserGuest())));
        assertTrue(rootSubUsers.contains(cms.readUser("test1")));
        assertTrue(rootSubUsers.contains(cms.readUser("test2")));

        rootOu.setDescription("new description");
        OpenCms.getOrgUnitManager().writeOrganizationalUnit(cms, rootOu);

        try {
            OpenCms.getOrgUnitManager().deleteOrganizationalUnit(cms, rootOu.getName());
            fail("should not be able to delete the root ou");
        } catch (CmsDataAccessException e) {
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
        List myRoles = OpenCms.getRoleManager().getRoles(cms, "", false);
        assertEquals(CmsRole.getSystemRoles().size(), myRoles.size());
        for (int i = 0; i < myRoles.size(); i++) {
            CmsRole role = (CmsRole)myRoles.get(i);
            assertEquals("", role.getOuFqn());
            assertTrue(myRoles.contains(CmsRole.getSystemRoles().get(i).forOrgUnit("")));
            assertTrue(CmsRole.getSystemRoles().contains(role.forOrgUnit(null)));
        }
        // check all roles (but since there are no more ous, it is the same as before)
        myRoles = OpenCms.getRoleManager().getRoles(cms, "", true);
        assertEquals(CmsRole.getSystemRoles().size(), myRoles.size());
        for (int i = 0; i < myRoles.size(); i++) {
            CmsRole role = (CmsRole)myRoles.get(i);
            assertEquals("", role.getOuFqn());
            assertTrue(myRoles.contains(CmsRole.getSystemRoles().get(i).forOrgUnit("")));
            assertTrue(CmsRole.getSystemRoles().contains(role.forOrgUnit(null)));
        }
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
        cms.loginUser("test/test1", "test1");
        assertEquals("test/", cms.getRequestContext().getOuFqn());
        assertEquals("test/test1", cms.getRequestContext().getCurrentUser().getName());
        cms.loginUser("test/test2/test2", "test2");
        assertEquals("test/test2/", cms.getRequestContext().getOuFqn());
        assertEquals("test/test2/test2", cms.getRequestContext().getCurrentUser().getName());

        // login user in deeper ou
        cms.loginUser("test/test2/test1", "test1");
        assertEquals("test/", cms.getRequestContext().getOuFqn());
        assertEquals("test/test1", cms.getRequestContext().getCurrentUser().getName());

        try {
            // try login user in higher ou
            cms.loginUser("test/test2", "test2");
            fail("It should not be possible to login an user in a higher ou");
        } catch (CmsAuthentificationException e) {
            // ok, ignore
        }
    }

    /**
     * Tests webuser ou behavior.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testWebuserOU() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Test webuser ou behavior");

        // try to create a normal ou without associated resource
        try {
            OpenCms.getOrgUnitManager().createOrganizationalUnit(cms, "/webuser", "webuser test", 0, null);
            fail("it should not be possible to create a normal ou without associated resource");
        } catch (CmsVfsException e) {
            // ok
        }

        // create a webuser ou without associated resource
        CmsOrganizationalUnit ou = OpenCms.getOrgUnitManager().createOrganizationalUnit(
            cms,
            "/webuser",
            "webuser test",
            CmsOrganizationalUnit.FLAG_WEBUSERS,
            null);
        // check the new created ou
        // no default Users group
        try {
            cms.readGroup(ou.getName() + OpenCms.getDefaultUsers().getGroupUsers());
            fail("webuser ou should not have a default group");
        } catch (CmsDbEntryNotFoundException e) {
            // ok, ignore
        }
        assertTrue(OpenCms.getOrgUnitManager().getGroups(cms, ou.getName(), true).isEmpty());
        // no default Offline project
        try {
            cms.readProject(ou.getName() + "Offline");
            fail("webuser ou should not have a default project");
        } catch (CmsDbEntryNotFoundException e) {
            // ok, ignore
        }
        // no roles except account manager
        List roles = OpenCms.getRoleManager().getRoles(cms, ou.getName(), true);
        assertEquals(1, roles.size());
        assertEquals(CmsRole.ACCOUNT_MANAGER.forOrgUnit(ou.getName()), roles.get(0));
        // check resources
        assertTrue(OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou.getName()).isEmpty());

        // add a resource
        OpenCms.getOrgUnitManager().addResourceToOrgUnit(cms, ou.getName(), "/");
        // check
        List res = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou.getName());
        assertEquals(1, res.size());
        assertEquals(cms.readResource("/"), res.get(0));
        // remove resource
        OpenCms.getOrgUnitManager().removeResourceFromOrgUnit(cms, ou.getName(), "/");
        // check resources
        assertTrue(OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(cms, ou.getName()).isEmpty());
    }

    /**
     * Returns the list of associated resource root paths.<p>
     *
     * @param cms the cms context
     * @param ou the organizational unit to get the resources for
     *
     * @throws CmsException if something goes wrong
     */
    private void assertOrgUnitResources(CmsObject cms, CmsOrganizationalUnit ou) throws CmsException {

        List resourceList = new ArrayList();
        Iterator itResources = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
            cms,
            ou.getName()).iterator();
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
}

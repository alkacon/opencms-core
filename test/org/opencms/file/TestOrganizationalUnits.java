/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/Attic/TestOrganizationalUnits.java,v $
 * Date   : $Date: 2007/01/08 14:03:04 $
 * Version: $Revision: 1.1.2.1 $
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

package org.opencms.file;

import org.opencms.db.CmsDbConsistencyException;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.OpenCms;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for operations with organizational units.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.1 $
 */
public class TestOrganizationalUnits extends OpenCmsTestCase {

    int m_todo4; // role check
    int m_todo5; // delete ou testcase
    int m_todo6; // online testcases
    int m_todo7; // db creation scripts

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
        suite.addTest(new TestOrganizationalUnits("testFirstLevelOu"));
        suite.addTest(new TestOrganizationalUnits("testDeeperLevelOu"));
        suite.addTest(new TestOrganizationalUnits("testResourceAssociations"));
        suite.addTest(new TestOrganizationalUnits("testPrincipalAssociations"));
        suite.addTest(new TestOrganizationalUnits("testDefaultUsers"));
        suite.addTest(new TestOrganizationalUnits("testResourcePermissions"));
        suite.addTest(new TestOrganizationalUnits("testUserLogin"));

        TestSetup wrapper = new TestSetup(suite) {

            protected void setUp() {

                setupOpenCms("simpletest", "/sites/default/", false);
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
        CmsOrganizationalUnit rootOu = cms.readOrganizationalUnit("");
        CmsOrganizationalUnit ou = cms.readOrganizationalUnit("/test");

        // create a deeper ou
        CmsOrganizationalUnit ou2 = cms.createOrganizationalUnit(ou.getFqn() + "/test2", "my test ou2", 0, "/folder1");

        // check the ou attributes
        assertEquals(0, ou2.getFlags());
        assertEquals("/test", ou2.getParentFqn());
        assertEquals("test2", ou2.getName());
        assertEquals("/test/test2", ou2.getFqn());
        assertEquals(cms.readFolder("/system/orgunits/root/test/test2").getStructureId(), ou2.getId());

        // check ou resources
        assertEquals(1, cms.getResourcesForOrganizationalUnit(ou2.getFqn()).size());
        assertTrue(cms.getResourcesForOrganizationalUnit(ou2.getFqn()).contains(cms.readResource("/folder1")));

        // check the ous
        assertEquals(1, cms.getOrganizationalUnits(rootOu.getFqn(), false).size());
        assertTrue(cms.getOrganizationalUnits(rootOu.getFqn(), false).contains(ou));

        assertEquals(1, cms.getOrganizationalUnits(ou.getFqn(), false).size());
        assertTrue(cms.getOrganizationalUnits(ou.getFqn(), false).contains(ou2));

        assertTrue(cms.getOrganizationalUnits(ou2.getFqn(), false).isEmpty());

        assertEquals(2, cms.getOrganizationalUnits(rootOu.getFqn(), true).size());
        assertTrue(cms.getOrganizationalUnits(rootOu.getFqn(), true).contains(ou));
        assertTrue(cms.getOrganizationalUnits(rootOu.getFqn(), true).contains(ou2));

        assertEquals(1, cms.getOrganizationalUnits(ou.getFqn(), true).size());
        assertTrue(cms.getOrganizationalUnits(ou.getFqn(), true).contains(ou2));

        assertTrue(cms.getOrganizationalUnits(ou2.getFqn(), true).isEmpty());

        cms.readOrganizationalUnit(ou.getFqn());
        try {
            cms.deleteOrganizationalUnit(ou.getFqn());
            fail("should not be possible to delete a ou having sub-ous");
        } catch (CmsDataAccessException e) {
            // ok, ignore
        }
        cms.readOrganizationalUnit(ou.getFqn());
    }

    /**
     * Tests handling with default users.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testDefaultUsers() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing handling with default users");

        // check the root ou
        CmsOrganizationalUnit rootOu = cms.readOrganizationalUnit("");
        CmsOrganizationalUnit ou = cms.readOrganizationalUnit("/test");
        CmsOrganizationalUnit ou2 = cms.readOrganizationalUnit(ou.getFqn() + "/test2");

        assertEquals(
            rootOu.appendFqn(OpenCms.getDefaultUsers().getGroupAdministrators()),
            rootOu.getDefaultUsers().getGroupAdministrators());
        assertEquals(
            rootOu.appendFqn(OpenCms.getDefaultUsers().getGroupProjectmanagers()),
            rootOu.getDefaultUsers().getGroupProjectmanagers());
        assertEquals(
            rootOu.appendFqn(OpenCms.getDefaultUsers().getGroupUsers()),
            rootOu.getDefaultUsers().getGroupUsers());
        assertEquals(
            rootOu.appendFqn(OpenCms.getDefaultUsers().getGroupGuests()),
            rootOu.getDefaultUsers().getGroupGuests());
        assertEquals(
            rootOu.appendFqn(OpenCms.getDefaultUsers().getUserAdmin()),
            rootOu.getDefaultUsers().getUserAdmin());
        assertEquals(
            rootOu.appendFqn(OpenCms.getDefaultUsers().getUserGuest()),
            rootOu.getDefaultUsers().getUserGuest());
        assertEquals(
            rootOu.appendFqn(OpenCms.getDefaultUsers().getUserExport()),
            rootOu.getDefaultUsers().getUserExport());
        assertEquals(
            rootOu.appendFqn(OpenCms.getDefaultUsers().getUserDeletedResource()),
            rootOu.getDefaultUsers().getUserDeletedResource());

        assertEquals(
            ou.appendFqn(OpenCms.getDefaultUsers().getGroupAdministrators()),
            ou.getDefaultUsers().getGroupAdministrators());
        assertEquals(
            ou.appendFqn(OpenCms.getDefaultUsers().getGroupProjectmanagers()),
            ou.getDefaultUsers().getGroupProjectmanagers());
        assertEquals(ou.appendFqn(OpenCms.getDefaultUsers().getGroupUsers()), ou.getDefaultUsers().getGroupUsers());
        assertEquals(ou.appendFqn(OpenCms.getDefaultUsers().getGroupGuests()), ou.getDefaultUsers().getGroupGuests());
        assertEquals(ou.appendFqn(OpenCms.getDefaultUsers().getUserAdmin()), ou.getDefaultUsers().getUserAdmin());
        assertEquals(ou.appendFqn(OpenCms.getDefaultUsers().getUserGuest()), ou.getDefaultUsers().getUserGuest());
        assertEquals(ou.appendFqn(OpenCms.getDefaultUsers().getUserExport()), ou.getDefaultUsers().getUserExport());
        assertEquals(
            ou.appendFqn(OpenCms.getDefaultUsers().getUserDeletedResource()),
            ou.getDefaultUsers().getUserDeletedResource());

        assertEquals(
            ou2.appendFqn(OpenCms.getDefaultUsers().getGroupAdministrators()),
            ou2.getDefaultUsers().getGroupAdministrators());
        assertEquals(
            ou2.appendFqn(OpenCms.getDefaultUsers().getGroupProjectmanagers()),
            ou2.getDefaultUsers().getGroupProjectmanagers());
        assertEquals(ou2.appendFqn(OpenCms.getDefaultUsers().getGroupUsers()), ou2.getDefaultUsers().getGroupUsers());
        assertEquals(ou2.appendFqn(OpenCms.getDefaultUsers().getGroupGuests()), ou2.getDefaultUsers().getGroupGuests());
        assertEquals(ou2.appendFqn(OpenCms.getDefaultUsers().getUserAdmin()), ou2.getDefaultUsers().getUserAdmin());
        assertEquals(ou2.appendFqn(OpenCms.getDefaultUsers().getUserGuest()), ou2.getDefaultUsers().getUserGuest());
        assertEquals(ou2.appendFqn(OpenCms.getDefaultUsers().getUserExport()), ou2.getDefaultUsers().getUserExport());
        assertEquals(
            ou2.appendFqn(OpenCms.getDefaultUsers().getUserDeletedResource()),
            ou2.getDefaultUsers().getUserDeletedResource());

        try {
            cms.createGroup(ou.appendFqn(OpenCms.getDefaultUsers().getGroupAdministrators()), "test", 0, null);
            fail("there should be already a group with the same name in the ou");
        } catch (CmsVfsException e) {
            // ok, ignore
        }
        try {
            cms.createGroup(ou.appendFqn(OpenCms.getDefaultUsers().getGroupProjectmanagers()), "test", 0, null);
            fail("there should be already a group with the same name in the ou");
        } catch (CmsVfsException e) {
            // ok, ignore
        }
        try {
            cms.createGroup(ou.appendFqn(OpenCms.getDefaultUsers().getGroupUsers()), "test", 0, null);
            fail("there should be already a group with the same name in the ou");
        } catch (CmsVfsException e) {
            // ok, ignore
        }
        try {
            cms.createGroup(ou.appendFqn(OpenCms.getDefaultUsers().getGroupGuests()), "test", 0, null);
            fail("there should be already a group with the same name in the ou");
        } catch (CmsVfsException e) {
            // ok, ignore
        }
        try {
            cms.createUser(ou.appendFqn(OpenCms.getDefaultUsers().getUserAdmin()), "test", "test", null);
            fail("there should be already an user with the same name in the ou");
        } catch (CmsVfsException e) {
            // ok, ignore
        }
        try {
            cms.createUser(ou.appendFqn(OpenCms.getDefaultUsers().getUserGuest()), "test", "test", null);
            fail("there should be already an user with the same name in the ou");
        } catch (CmsVfsException e) {
            // ok, ignore
        }
        try {
            cms.createUser(ou.appendFqn(OpenCms.getDefaultUsers().getUserExport()), "test", "test", null);
            fail("there should be already an user with the same name in the ou");
        } catch (CmsVfsException e) {
            // ok, ignore
        }
        try {
            cms.createUser(ou.appendFqn(OpenCms.getDefaultUsers().getUserDeletedResource()), "test", "test", null);
            fail("there should be already an user with the same name in the ou");
        } catch (CmsVfsException e) {
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
        CmsOrganizationalUnit rootOu = cms.readOrganizationalUnit("");

        // create a first level ou
        CmsOrganizationalUnit ou = cms.createOrganizationalUnit("/test", "my test ou", 0, "/");

        // check the ou attributes
        assertEquals(0, ou.getFlags());
        assertEquals("", ou.getParentFqn());
        assertEquals("my test ou", ou.getDescription());
        assertEquals("test", ou.getName());
        assertEquals("/test", ou.getFqn());
        assertEquals(cms.readFolder("/system/orgunits/root/test").getStructureId(), ou.getId());

        // check ou resources
        assertEquals(1, cms.getResourcesForOrganizationalUnit(ou.getFqn()).size());
        assertTrue(cms.getResourcesForOrganizationalUnit(ou.getFqn()).contains(cms.readResource("/")));

        // check the ous
        assertEquals(1, cms.getOrganizationalUnits(rootOu.getFqn(), false).size());
        assertTrue(cms.getOrganizationalUnits(rootOu.getFqn(), false).contains(ou));
        assertTrue(cms.getOrganizationalUnits(ou.getFqn(), false).isEmpty());

        assertEquals(1, cms.getOrganizationalUnits(rootOu.getFqn(), true).size());
        assertTrue(cms.getOrganizationalUnits(rootOu.getFqn(), true).contains(ou));
        assertTrue(cms.getOrganizationalUnits(ou.getFqn(), true).isEmpty());

        // test to update ou attributes
        ou.setDescription("new ou description");
        ou.setFlags(123);
        cms.writeOrganizationalUnit(ou);

        ou = cms.readOrganizationalUnit(ou.getFqn());
        // check the ou attributes again
        assertEquals(123, ou.getFlags());
        assertEquals("", ou.getParentFqn());
        assertEquals("new ou description", ou.getDescription());
        assertEquals("test", ou.getName());
        assertEquals("/test", ou.getFqn());
        assertEquals(cms.readFolder("/system/orgunits/root/test").getStructureId(), ou.getId());

        // check default users & groups for the new ou
        assertEquals(4, cms.getGroupsForOrganizationalUnit(ou.getFqn(), false).size());
        assertTrue(cms.getGroupsForOrganizationalUnit(ou.getFqn(), false).contains(
            cms.readGroup(ou.getDefaultUsers().getGroupAdministrators())));
        assertTrue(cms.getGroupsForOrganizationalUnit(ou.getFqn(), false).contains(
            cms.readGroup(ou.getDefaultUsers().getGroupGuests())));
        assertTrue(cms.getGroupsForOrganizationalUnit(ou.getFqn(), false).contains(
            cms.readGroup(ou.getDefaultUsers().getGroupProjectmanagers())));
        assertTrue(cms.getGroupsForOrganizationalUnit(ou.getFqn(), false).contains(
            cms.readGroup(ou.getDefaultUsers().getGroupUsers())));

        assertEquals(4, cms.getGroupsForOrganizationalUnit(ou.getFqn(), true).size());
        assertTrue(cms.getGroupsForOrganizationalUnit(ou.getFqn(), true).contains(
            cms.readGroup(ou.getDefaultUsers().getGroupAdministrators())));
        assertTrue(cms.getGroupsForOrganizationalUnit(ou.getFqn(), true).contains(
            cms.readGroup(ou.getDefaultUsers().getGroupGuests())));
        assertTrue(cms.getGroupsForOrganizationalUnit(ou.getFqn(), true).contains(
            cms.readGroup(ou.getDefaultUsers().getGroupProjectmanagers())));
        assertTrue(cms.getGroupsForOrganizationalUnit(ou.getFqn(), true).contains(
            cms.readGroup(ou.getDefaultUsers().getGroupUsers())));

        assertEquals(3, cms.getUsersForOrganizationalUnit(ou.getFqn(), false).size());
        assertTrue(cms.getUsersForOrganizationalUnit(ou.getFqn(), false).contains(
            cms.readUser(ou.getDefaultUsers().getUserAdmin())));
        assertTrue(cms.getUsersForOrganizationalUnit(ou.getFqn(), false).contains(
            cms.readUser(ou.getDefaultUsers().getUserExport())));
        assertTrue(cms.getUsersForOrganizationalUnit(ou.getFqn(), false).contains(
            cms.readUser(ou.getDefaultUsers().getUserGuest())));

        assertEquals(3, cms.getUsersForOrganizationalUnit(ou.getFqn(), true).size());
        assertTrue(cms.getUsersForOrganizationalUnit(ou.getFqn(), true).contains(
            cms.readUser(ou.getDefaultUsers().getUserAdmin())));
        assertTrue(cms.getUsersForOrganizationalUnit(ou.getFqn(), true).contains(
            cms.readUser(ou.getDefaultUsers().getUserExport())));
        assertTrue(cms.getUsersForOrganizationalUnit(ou.getFqn(), true).contains(
            cms.readUser(ou.getDefaultUsers().getUserGuest())));

        // check the root ou principals again
        assertEquals(7, cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).size());
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(
            cms.readGroup(OpenCms.getDefaultUsers().getGroupAdministrators())));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(
            cms.readGroup(OpenCms.getDefaultUsers().getGroupGuests())));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(
            cms.readGroup(OpenCms.getDefaultUsers().getGroupProjectmanagers())));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(
            cms.readGroup(OpenCms.getDefaultUsers().getGroupUsers())));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readGroup("/group1")));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readGroup("/group2")));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readGroup("/group3")));

        assertEquals(11, cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).size());
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readGroup(OpenCms.getDefaultUsers().getGroupAdministrators())));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readGroup(OpenCms.getDefaultUsers().getGroupGuests())));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readGroup(OpenCms.getDefaultUsers().getGroupProjectmanagers())));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readGroup(OpenCms.getDefaultUsers().getGroupUsers())));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(cms.readGroup("/group1")));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(cms.readGroup("/group2")));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(cms.readGroup("/group3")));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readGroup(ou.getDefaultUsers().getGroupAdministrators())));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readGroup(ou.getDefaultUsers().getGroupGuests())));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readGroup(ou.getDefaultUsers().getGroupProjectmanagers())));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readGroup(ou.getDefaultUsers().getGroupUsers())));

        assertEquals(5, cms.getUsersForOrganizationalUnit(rootOu.getFqn(), false).size());
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), false).contains(
            cms.readUser(OpenCms.getDefaultUsers().getUserAdmin())));
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), false).contains(
            cms.readUser(OpenCms.getDefaultUsers().getUserExport())));
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), false).contains(
            cms.readUser(OpenCms.getDefaultUsers().getUserGuest())));
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readUser("/test1")));
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readUser("/test2")));

        assertEquals(8, cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).size());
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readUser(OpenCms.getDefaultUsers().getUserAdmin())));
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readUser(OpenCms.getDefaultUsers().getUserExport())));
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readUser(OpenCms.getDefaultUsers().getUserGuest())));
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).contains(cms.readUser("/test1")));
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).contains(cms.readUser("/test2")));
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readUser(ou.getDefaultUsers().getUserAdmin())));
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readUser(ou.getDefaultUsers().getUserExport())));
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readUser(ou.getDefaultUsers().getUserGuest())));
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
        CmsOrganizationalUnit rootOu = cms.readOrganizationalUnit("");
        CmsOrganizationalUnit ou = cms.readOrganizationalUnit("/test");
        CmsOrganizationalUnit ou2 = cms.readOrganizationalUnit(ou.getFqn() + "/test2");

        try {
            // move to move /test1 to /test/test1 
            cms.setPrincipalsOrganizationalUnit(ou.getFqn(), I_CmsPrincipal.PRINCIPAL_USER, "/test1");
            fail("it should not be possible to move the user to other ou");
        } catch (CmsDbConsistencyException e) {
            // ok, ignore
        }
        try {
            // move to move /group1 to /test/group1 
            cms.setPrincipalsOrganizationalUnit(ou.getFqn(), I_CmsPrincipal.PRINCIPAL_GROUP, "/group1");
            fail("it should not be possible to move the group (since a member is also member of other group) to other ou");
        } catch (CmsDbConsistencyException e) {
            // ok, ignore
        }
        cms.removeUserFromGroup("/test1", "/Users");

        // move /group1 to /test/group1 
        cms.setPrincipalsOrganizationalUnit(ou.getFqn(), I_CmsPrincipal.PRINCIPAL_GROUP, "/group1");
        try {
            cms.readGroup("/group1");
            fail("it should not be possible to read the group in the old ou");
        } catch (CmsDbEntryNotFoundException e) {
            // ok, ignore
        }
        try {
            cms.readUser("/test1");
            fail("it should not be possible to read the user in the old ou");
        } catch (CmsDbEntryNotFoundException e) {
            // ok, ignore
        }
        cms.readGroup("/test/group1");
        cms.readUser("/test/test1");
        assertEquals(1, cms.getGroupsOfUser("/test/test1").size());
        assertTrue(cms.getGroupsOfUser("/test/test1").contains(cms.readGroup("/test/group1")));
        assertEquals(1, cms.getUsersOfGroup("/test/group1").size());
        assertTrue(cms.getUsersOfGroup("/test/group1").contains(cms.readUser("/test/test1")));

        assertEquals(6, cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).size());
        assertFalse(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readGroup("/test/group1")));
        assertEquals(15, cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).size());
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(cms.readGroup("/test/group1")));

        assertEquals(4, cms.getUsersForOrganizationalUnit(rootOu.getFqn(), false).size());
        assertFalse(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readUser("/test/test1")));
        assertEquals(11, cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).size());
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).contains(cms.readUser("/test/test1")));

        assertEquals(5, cms.getGroupsForOrganizationalUnit(ou.getFqn(), false).size());
        assertTrue(cms.getGroupsForOrganizationalUnit(ou.getFqn(), false).contains(cms.readGroup("/test/group1")));
        assertEquals(9, cms.getGroupsForOrganizationalUnit(ou.getFqn(), true).size());
        assertTrue(cms.getGroupsForOrganizationalUnit(ou.getFqn(), true).contains(cms.readGroup("/test/group1")));

        assertEquals(4, cms.getUsersForOrganizationalUnit(ou.getFqn(), false).size());
        assertTrue(cms.getUsersForOrganizationalUnit(ou.getFqn(), false).contains(cms.readUser("/test/test1")));
        assertEquals(7, cms.getUsersForOrganizationalUnit(ou.getFqn(), true).size());
        assertTrue(cms.getUsersForOrganizationalUnit(ou.getFqn(), true).contains(cms.readUser("/test/test1")));

        // TODO: what about resource permissions when moving a principal to other ou??
        int todo; // remove them?

        cms.removeUserFromGroup("/test2", "/Users");
        // move /group2 to /test/test2/group2 
        cms.setPrincipalsOrganizationalUnit(ou2.getFqn(), I_CmsPrincipal.PRINCIPAL_GROUP, "/group2");
        try {
            cms.readGroup("/group2");
            fail("it should not be possible to read the group in the old ou");
        } catch (CmsDbEntryNotFoundException e) {
            // ok, ignore
        }
        try {
            cms.readUser("/test2");
            fail("it should not be possible to read the user in the old ou");
        } catch (CmsDbEntryNotFoundException e) {
            // ok, ignore
        }
        cms.readGroup("/test/test2/group2");
        cms.readUser("/test/test2/test2");
        assertEquals(1, cms.getGroupsOfUser("/test/test2/test2").size());
        assertTrue(cms.getGroupsOfUser("/test/test2/test2").contains(cms.readGroup("/test/test2/group2")));
        assertEquals(1, cms.getUsersOfGroup("/test/test2/group2").size());
        assertTrue(cms.getUsersOfGroup("/test/test2/group2").contains(cms.readUser("/test/test2/test2")));

        assertEquals(5, cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).size());
        assertFalse(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readGroup("/test/group1")));
        assertFalse(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(
            cms.readGroup("/test/test2/group2")));
        assertEquals(15, cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).size());
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(cms.readGroup("/test/group1")));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readGroup("/test/test2/group2")));

        assertEquals(3, cms.getUsersForOrganizationalUnit(rootOu.getFqn(), false).size());
        assertFalse(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readUser("/test/test1")));
        assertFalse(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), false).contains(
            cms.readUser("/test/test2/test2")));
        assertEquals(11, cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).size());
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).contains(cms.readUser("/test/test1")));
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).contains(cms.readUser("/test/test2/test2")));

        assertEquals(5, cms.getGroupsForOrganizationalUnit(ou.getFqn(), false).size());
        assertTrue(cms.getGroupsForOrganizationalUnit(ou.getFqn(), false).contains(cms.readGroup("/test/group1")));
        assertEquals(10, cms.getGroupsForOrganizationalUnit(ou.getFqn(), true).size());
        assertTrue(cms.getGroupsForOrganizationalUnit(ou.getFqn(), true).contains(cms.readGroup("/test/group1")));
        assertTrue(cms.getGroupsForOrganizationalUnit(ou.getFqn(), true).contains(cms.readGroup("/test/test2/group2")));

        assertEquals(4, cms.getUsersForOrganizationalUnit(ou.getFqn(), false).size());
        assertTrue(cms.getUsersForOrganizationalUnit(ou.getFqn(), false).contains(cms.readUser("/test/test1")));
        assertEquals(8, cms.getUsersForOrganizationalUnit(ou.getFqn(), true).size());
        assertTrue(cms.getUsersForOrganizationalUnit(ou.getFqn(), true).contains(cms.readUser("/test/test1")));
        assertTrue(cms.getUsersForOrganizationalUnit(ou.getFqn(), true).contains(cms.readUser("/test/test2/test2")));
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
        CmsOrganizationalUnit rootOu = cms.readOrganizationalUnit("");
        CmsOrganizationalUnit ou = cms.readOrganizationalUnit("/test");
        CmsOrganizationalUnit ou2 = cms.readOrganizationalUnit("/test/test2");

        // creation tests
        try {
            cms.createOrganizationalUnit("test2", "my test ou2", 0, "/");
            fail("should not be possible to create an ou at root level");
        } catch (CmsDataAccessException e) {
            // ok, just be sure
            assertEquals(2, cms.getOrganizationalUnits(rootOu.getFqn(), true).size());
            assertTrue(cms.getOrganizationalUnits(rootOu.getFqn(), true).contains(ou));
            assertTrue(cms.getOrganizationalUnits(rootOu.getFqn(), true).contains(ou2));
        }

        try {
            cms.createOrganizationalUnit("/doesnotexist/test2", "my test ou2", 0, "/");
            fail("should not be possible to create a new ou with a not valid parent");
        } catch (CmsDataAccessException e) {
            // ok, just be sure the ou has not been created
            assertEquals(2, cms.getOrganizationalUnits(rootOu.getFqn(), true).size());
            assertTrue(cms.getOrganizationalUnits(rootOu.getFqn(), true).contains(ou));
            assertTrue(cms.getOrganizationalUnits(rootOu.getFqn(), true).contains(ou2));
        }

        try {
            cms.createOrganizationalUnit(ou2.getFqn() + "/test3", "my test ou3", 0, "/folder2");
            fail("should not be possible to create a new ou with a resource out of the parent scope");
        } catch (CmsDataAccessException e) {
            // ok, just be sure the ou has not been created
            assertEquals(2, cms.getOrganizationalUnits(rootOu.getFqn(), true).size());
            assertTrue(cms.getOrganizationalUnits(rootOu.getFqn(), true).contains(ou));
            assertTrue(cms.getOrganizationalUnits(rootOu.getFqn(), true).contains(ou2));
        }

        // resource addition tests
        try {
            cms.addResourceToOrgUnit(ou.getFqn(), "/folder1");
            fail("it should not be possible to add a resource that is contained in another ou resource");
        } catch (CmsDataAccessException e) {
            // ok, just check again to be sure
            assertEquals(1, cms.getResourcesForOrganizationalUnit(ou.getFqn()).size());
            assertTrue(cms.getResourcesForOrganizationalUnit(ou.getFqn()).contains(cms.readResource("/")));
        }

        try {
            cms.addResourceToOrgUnit(ou.getFqn(), "/sites/doesnotexist");
            fail("should not be possible to add an unexistent resource to an ou");
        } catch (CmsDataAccessException e) {
            // ok, just be sure
            assertEquals(1, cms.getResourcesForOrganizationalUnit(ou.getFqn()).size());
            assertTrue(cms.getResourcesForOrganizationalUnit(ou.getFqn()).contains(cms.readResource("/")));
        }

        // add an additional resource
        cms.addResourceToOrgUnit(ou2.getFqn(), "/folder2");
        // check ou resources again
        assertEquals(2, cms.getResourcesForOrganizationalUnit(ou2.getFqn()).size());
        assertTrue(cms.getResourcesForOrganizationalUnit(ou2.getFqn()).contains(cms.readResource("/folder1")));
        assertTrue(cms.getResourcesForOrganizationalUnit(ou2.getFqn()).contains(cms.readResource("/folder2")));

        // resource remotion tests
        try {
            cms.removeResourceFromOrgUnit(ou2.getFqn(), "/folder1/index.html");
            fail("should not be possible to remove an not associated resource from an ou");
        } catch (CmsDataAccessException e) {
            // ok, just be sure
            assertEquals(2, cms.getResourcesForOrganizationalUnit(ou2.getFqn()).size());
            assertTrue(cms.getResourcesForOrganizationalUnit(ou2.getFqn()).contains(cms.readResource("/folder1")));
            assertTrue(cms.getResourcesForOrganizationalUnit(ou2.getFqn()).contains(cms.readResource("/folder2")));
        }

        // remove a resource
        cms.removeResourceFromOrgUnit(ou2.getFqn(), "/folder1");
        // check ou resource
        assertEquals(1, cms.getResourcesForOrganizationalUnit(ou2.getFqn()).size());
        assertTrue(cms.getResourcesForOrganizationalUnit(ou2.getFqn()).contains(cms.readResource("/folder2")));
    }

    /**
     * Tests handling with default organizational unit resources permissions.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testResourcePermissions() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing handling with default organizational unit resources permissions");

        // check the root ou
        CmsOrganizationalUnit rootOu = cms.readOrganizationalUnit("");
        CmsOrganizationalUnit ou = cms.readOrganizationalUnit("/test");
        CmsOrganizationalUnit ou2 = cms.readOrganizationalUnit(ou.getFqn() + "/test2");

        cms.getRequestContext().setSiteRoot("");
        String res = ((CmsResource)cms.getResourcesForOrganizationalUnit(rootOu.getFqn()).get(0)).getRootPath();
        assertEquals("/", res);

        // check default permissions for root ou
        assertPermissionString(
            cms,
            res,
            cms.readGroup(rootOu.getDefaultUsers().getGroupAdministrators()),
            "+r+w+v+c+d+i-l");
        assertPermissionString(
            cms,
            res,
            cms.readGroup(rootOu.getDefaultUsers().getGroupProjectmanagers()),
            "+r+w+v+c+d+i-l");
        assertPermissionString(cms, res, cms.readGroup(rootOu.getDefaultUsers().getGroupUsers()), "+r+w+v+c+i-l");
        assertPermissionString(cms, res, cms.readGroup(rootOu.getDefaultUsers().getGroupGuests()), "+r+v+i-l");

        // check default permissions for ou
        res = ((CmsResource)cms.getResourcesForOrganizationalUnit(ou.getFqn()).get(0)).getRootPath();
        assertPermissionString(cms, res, cms.readGroup(ou.getDefaultUsers().getGroupAdministrators()), "+r+w+v+c+d+i-l");
        assertPermissionString(
            cms,
            res,
            cms.readGroup(ou.getDefaultUsers().getGroupProjectmanagers()),
            "+r+w+v+c+d+i-l");
        assertPermissionString(cms, res, cms.readGroup(ou.getDefaultUsers().getGroupUsers()), "+r+w+v+c+i-l");
        assertPermissionString(cms, res, cms.readGroup(ou.getDefaultUsers().getGroupGuests()), "+r+v+i-l");

        // check default permissions for ou2
        res = ((CmsResource)cms.getResourcesForOrganizationalUnit(ou2.getFqn()).get(0)).getRootPath();
        assertPermissionString(
            cms,
            res,
            cms.readGroup(ou2.getDefaultUsers().getGroupAdministrators()),
            "+r+w+v+c+d+i-l");
        assertPermissionString(
            cms,
            res,
            cms.readGroup(ou2.getDefaultUsers().getGroupProjectmanagers()),
            "+r+w+v+c+d+i-l");
        assertPermissionString(cms, res, cms.readGroup(ou2.getDefaultUsers().getGroupUsers()), "+r+w+v+c+i-l");
        assertPermissionString(cms, res, cms.readGroup(ou2.getDefaultUsers().getGroupGuests()), "+r+v+i-l");

        String folder = "/sites/default/folder1";
        // check permissions before adding folder to ou2
        assertPermissionString(cms, folder, cms.readGroup(ou2.getDefaultUsers().getGroupAdministrators()), null);
        assertPermissionString(cms, folder, cms.readGroup(ou2.getDefaultUsers().getGroupProjectmanagers()), null);
        assertPermissionString(cms, folder, cms.readGroup(ou2.getDefaultUsers().getGroupUsers()), null);
        assertPermissionString(cms, folder, cms.readGroup(ou2.getDefaultUsers().getGroupGuests()), null);

        cms.addResourceToOrgUnit(ou2.getFqn(), folder);
        // check permissions after adding folder to ou2
        assertPermissionString(
            cms,
            folder,
            cms.readGroup(ou2.getDefaultUsers().getGroupAdministrators()),
            "+r+w+v+c+d+i-l");
        assertPermissionString(
            cms,
            folder,
            cms.readGroup(ou2.getDefaultUsers().getGroupProjectmanagers()),
            "+r+w+v+c+d+i-l");
        assertPermissionString(cms, folder, cms.readGroup(ou2.getDefaultUsers().getGroupUsers()), "+r+w+v+c+i-l");
        assertPermissionString(cms, folder, cms.readGroup(ou2.getDefaultUsers().getGroupGuests()), "+r+v+i-l");

        cms.removeResourceFromOrgUnit(ou2.getFqn(), folder);
        // check permissions after removing folder from ou2
        assertPermissionString(cms, folder, cms.readGroup(ou2.getDefaultUsers().getGroupAdministrators()), null);
        assertPermissionString(cms, folder, cms.readGroup(ou2.getDefaultUsers().getGroupProjectmanagers()), null);
        assertPermissionString(cms, folder, cms.readGroup(ou2.getDefaultUsers().getGroupUsers()), null);
        assertPermissionString(cms, folder, cms.readGroup(ou2.getDefaultUsers().getGroupGuests()), null);
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
        CmsOrganizationalUnit rootOu = cms.readOrganizationalUnit("");

        // check all the ous
        assertTrue(cms.getOrganizationalUnits(rootOu.getFqn(), false).isEmpty());
        assertTrue(cms.getOrganizationalUnits(rootOu.getFqn(), true).isEmpty());

        // check the root ou attributes
        assertEquals(0, rootOu.getFlags());
        assertNull(rootOu.getParentFqn());
        assertEquals("The root organizational unit", rootOu.getDescription());
        assertEquals("", rootOu.getName());
        assertEquals("", rootOu.getFqn());
        assertEquals(cms.readFolder("/system/orgunits/root").getStructureId(), rootOu.getId());

        // check root ou resources
        assertEquals(1, cms.getResourcesForOrganizationalUnit(rootOu.getFqn()).size());
        String site = cms.getRequestContext().getSiteRoot();
        cms.getRequestContext().setSiteRoot("/");
        assertTrue(cms.getResourcesForOrganizationalUnit(rootOu.getFqn()).contains(cms.readResource("/")));
        cms.getRequestContext().setSiteRoot(site);

        // check the root ou principals
        assertEquals(7, cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).size());
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(
            cms.readGroup(OpenCms.getDefaultUsers().getGroupAdministrators())));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(
            cms.readGroup(OpenCms.getDefaultUsers().getGroupGuests())));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(
            cms.readGroup(OpenCms.getDefaultUsers().getGroupProjectmanagers())));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(
            cms.readGroup(OpenCms.getDefaultUsers().getGroupUsers())));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readGroup("/group1")));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readGroup("/group2")));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readGroup("/group3")));
        assertEquals(7, cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).size());
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readGroup(OpenCms.getDefaultUsers().getGroupAdministrators())));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readGroup(OpenCms.getDefaultUsers().getGroupGuests())));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readGroup(OpenCms.getDefaultUsers().getGroupProjectmanagers())));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readGroup(OpenCms.getDefaultUsers().getGroupUsers())));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(cms.readGroup("/group1")));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(cms.readGroup("/group2")));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(cms.readGroup("/group3")));
        assertEquals(5, cms.getUsersForOrganizationalUnit(rootOu.getFqn(), false).size());
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), false).contains(
            cms.readUser(OpenCms.getDefaultUsers().getUserAdmin())));
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), false).contains(
            cms.readUser(OpenCms.getDefaultUsers().getUserExport())));
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), false).contains(
            cms.readUser(OpenCms.getDefaultUsers().getUserGuest())));
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readUser("/test1")));
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readUser("/test2")));
        assertEquals(5, cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).size());
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readUser(OpenCms.getDefaultUsers().getUserAdmin())));
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readUser(OpenCms.getDefaultUsers().getUserExport())));
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readUser(OpenCms.getDefaultUsers().getUserGuest())));
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).contains(cms.readUser("/test1")));
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).contains(cms.readUser("/test2")));

        try {
            rootOu.setDescription("new description");
            cms.writeOrganizationalUnit(rootOu);
            fail("should not be able to edit the root ou");
        } catch (CmsIllegalStateException e) {
            // ok, ignore
        }
    }

    /**
     * Tests handling with a deeper level ou.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testUserLogin() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing handling with a deeper level ou");

        // login users in respective ous
        cms.loginUser("/test/test1", "test1");
        assertEquals("/test", cms.getRequestContext().getOuFqn());
        assertEquals("/test/test1", cms.getRequestContext().currentUser().getName());
        cms.loginUser("/test/test2/test2", "test2");
        assertEquals("/test/test2", cms.getRequestContext().getOuFqn());
        assertEquals("/test/test2/test2", cms.getRequestContext().currentUser().getName());

        // login user in deeper ou
        cms.loginUser("/test/test2/test1", "test1");
        assertEquals("/test/test2", cms.getRequestContext().getOuFqn());
        assertEquals("/test/test1", cms.getRequestContext().currentUser().getName());

        try {
            // try login user in higher ou
            cms.loginUser("/test/test2", "test2");
            fail("It should not be possible to login an user in a higher ou");
        } catch (CmsException e) {
            e.printStackTrace();
            // ok, ignore
        }
    }
}

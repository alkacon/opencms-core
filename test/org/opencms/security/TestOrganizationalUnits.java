/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/security/TestOrganizationalUnits.java,v $
 * Date   : $Date: 2007/01/16 09:50:47 $
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
 * @version $Revision: 1.1.2.1 $
 */
public class TestOrganizationalUnits extends OpenCmsTestCase {

    int m_todo5; // default groups/users check, in special CmsUser#isGuest
    int m_todo6; // role check
    int m_todo7; // db creation scripts
    int m_todo8; // localization

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
        suite.addTest(new TestOrganizationalUnits("testResourcePermissions"));
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
     * Tests handling with a first level ou.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testFirstLevelOu() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing handling with a first level ou");

        // check the root ou
        CmsOrganizationalUnit rootOu = cms.readOrganizationalUnit("");

        cms.addUserToGroup("/test1", "/Administrators");
        cms.loginUser("/test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        cms.lockResource("/system/");

        cms.loginUser("/Admin", "admin");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

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
        assertTrue(cms.readFolder("/system/orgunits/root/test").isInternal());

        ou.setFlags(0);
        cms.writeOrganizationalUnit(ou);

        ou = cms.readOrganizationalUnit(ou.getFqn());
        assertEquals(0, ou.getFlags());
        assertTrue(cms.readFolder("/system/orgunits/root/test").isInternal());

        // create a first level ou
        CmsOrganizationalUnit ou2 = cms.createOrganizationalUnit("/test2", "my test ou2", 0, "/");

        // check the ou attributes
        assertEquals(0, ou2.getFlags());
        assertEquals("", ou2.getParentFqn());
        assertEquals("my test ou2", ou2.getDescription());
        assertEquals("test2", ou2.getName());
        assertEquals("/test2", ou2.getFqn());
        assertEquals(cms.readFolder("/system/orgunits/root/test2").getStructureId(), ou2.getId());

        // check ou resources
        assertEquals(1, cms.getResourcesForOrganizationalUnit(ou2.getFqn()).size());
        assertTrue(cms.getResourcesForOrganizationalUnit(ou2.getFqn()).contains(cms.readResource("/")));

        // check the ous
        assertEquals(2, cms.getOrganizationalUnits(rootOu.getFqn(), false).size());
        assertTrue(cms.getOrganizationalUnits(rootOu.getFqn(), false).contains(ou));
        assertTrue(cms.getOrganizationalUnits(rootOu.getFqn(), false).contains(ou2));
        assertTrue(cms.getOrganizationalUnits(ou2.getFqn(), false).isEmpty());

        assertEquals(2, cms.getOrganizationalUnits(rootOu.getFqn(), true).size());
        assertTrue(cms.getOrganizationalUnits(rootOu.getFqn(), true).contains(ou));
        assertTrue(cms.getOrganizationalUnits(rootOu.getFqn(), true).contains(ou2));
        assertTrue(cms.getOrganizationalUnits(ou.getFqn(), true).isEmpty());

        // delete 2nd ou
        cms.deleteOrganizationalUnit(ou2.getFqn());

        try {
            cms.readOrganizationalUnit(ou2.getFqn());
            fail("it should not be possible to read the deleted ou");
        } catch (CmsDataAccessException e) {
            // ok, ignore
        }

        // check the ous again
        assertEquals(1, cms.getOrganizationalUnits(rootOu.getFqn(), false).size());
        assertTrue(cms.getOrganizationalUnits(rootOu.getFqn(), false).contains(ou));
        assertTrue(cms.getOrganizationalUnits(ou.getFqn(), false).isEmpty());

        assertEquals(1, cms.getOrganizationalUnits(rootOu.getFqn(), true).size());
        assertTrue(cms.getOrganizationalUnits(rootOu.getFqn(), true).contains(ou));
        assertTrue(cms.getOrganizationalUnits(ou.getFqn(), true).isEmpty());

        // check default users & groups for the new ou
        assertEquals(0, cms.getGroupsForOrganizationalUnit(ou.getFqn(), false).size());
        assertEquals(0, cms.getGroupsForOrganizationalUnit(ou.getFqn(), true).size());
        assertEquals(0, cms.getUsersForOrganizationalUnit(ou.getFqn(), false).size());
        assertEquals(0, cms.getUsersForOrganizationalUnit(ou.getFqn(), true).size());

        // check the root ou principals again
        assertEquals(4, cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).size());
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(
            cms.readGroup(OpenCms.getDefaultUsers().getGroupGuests())));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readGroup("/group1")));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readGroup("/group2")));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readGroup("/group3")));

        assertEquals(4, cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).size());
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readGroup(OpenCms.getDefaultUsers().getGroupGuests())));
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

        cms.removeUserFromGroup("/test1", "/Administrators");
        assertLock(cms, "/system/", CmsLockType.EXCLUSIVE, cms.readUser("/test1"));
        cms.changeLock("/system/");
        cms.unlockResource("/system/");
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
            fail("it should not be possible to move the user to other ou (since it is still member of a group)");
        } catch (CmsDbConsistencyException e) {
            // ok, ignore
        }
        try {
            // move to move /group1 to /test/group1 
            cms.setPrincipalsOrganizationalUnit(ou.getFqn(), I_CmsPrincipal.PRINCIPAL_GROUP, "/group1");
            fail("it should not be possible to move the group to other ou (since a member is still member of other group)");
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

        assertEquals(3, cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).size());
        assertFalse(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readGroup("/test/group1")));
        assertEquals(4, cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).size());
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(cms.readGroup("/test/group1")));

        assertEquals(4, cms.getUsersForOrganizationalUnit(rootOu.getFqn(), false).size());
        assertFalse(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readUser("/test/test1")));
        assertEquals(5, cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).size());
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).contains(cms.readUser("/test/test1")));

        assertEquals(1, cms.getGroupsForOrganizationalUnit(ou.getFqn(), false).size());
        assertTrue(cms.getGroupsForOrganizationalUnit(ou.getFqn(), false).contains(cms.readGroup("/test/group1")));
        assertEquals(1, cms.getGroupsForOrganizationalUnit(ou.getFqn(), true).size());
        assertTrue(cms.getGroupsForOrganizationalUnit(ou.getFqn(), true).contains(cms.readGroup("/test/group1")));

        assertEquals(1, cms.getUsersForOrganizationalUnit(ou.getFqn(), false).size());
        assertTrue(cms.getUsersForOrganizationalUnit(ou.getFqn(), false).contains(cms.readUser("/test/test1")));
        assertEquals(1, cms.getUsersForOrganizationalUnit(ou.getFqn(), true).size());
        assertTrue(cms.getUsersForOrganizationalUnit(ou.getFqn(), true).contains(cms.readUser("/test/test1")));

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

        assertEquals(2, cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).size());
        assertFalse(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readGroup("/test/group1")));
        assertFalse(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(
            cms.readGroup("/test/test2/group2")));
        assertEquals(4, cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).size());
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(cms.readGroup("/test/group1")));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readGroup("/test/test2/group2")));

        assertEquals(3, cms.getUsersForOrganizationalUnit(rootOu.getFqn(), false).size());
        assertFalse(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readUser("/test/test1")));
        assertFalse(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), false).contains(
            cms.readUser("/test/test2/test2")));
        assertEquals(5, cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).size());
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).contains(cms.readUser("/test/test1")));
        assertTrue(cms.getUsersForOrganizationalUnit(rootOu.getFqn(), true).contains(cms.readUser("/test/test2/test2")));

        assertEquals(1, cms.getGroupsForOrganizationalUnit(ou.getFqn(), false).size());
        assertTrue(cms.getGroupsForOrganizationalUnit(ou.getFqn(), false).contains(cms.readGroup("/test/group1")));
        assertEquals(2, cms.getGroupsForOrganizationalUnit(ou.getFqn(), true).size());
        assertTrue(cms.getGroupsForOrganizationalUnit(ou.getFqn(), true).contains(cms.readGroup("/test/group1")));
        assertTrue(cms.getGroupsForOrganizationalUnit(ou.getFqn(), true).contains(cms.readGroup("/test/test2/group2")));

        assertEquals(1, cms.getUsersForOrganizationalUnit(ou.getFqn(), false).size());
        assertTrue(cms.getUsersForOrganizationalUnit(ou.getFqn(), false).contains(cms.readUser("/test/test1")));
        assertEquals(2, cms.getUsersForOrganizationalUnit(ou.getFqn(), true).size());
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

        // test adding the same resource as a parent ou resource
        assertEquals(1, cms.getResourcesForOrganizationalUnit(ou2.getFqn()).size());
        assertTrue(cms.getResourcesForOrganizationalUnit(ou2.getFqn()).contains(cms.readResource("/folder1")));
        CmsOrganizationalUnit ou3 = cms.createOrganizationalUnit(
            CmsOrganizationalUnit.appendFqn(ou2.getFqn(), "test3"),
            "it will not last too long",
            0,
            "/folder1");
        assertEquals(1, cms.getResourcesForOrganizationalUnit(ou3.getFqn()).size());
        assertTrue(cms.getResourcesForOrganizationalUnit(ou3.getFqn()).contains(cms.readResource("/folder1")));
        cms.deleteOrganizationalUnit(ou3.getFqn());

        // test removing all resources
        assertEquals(1, cms.getResourcesForOrganizationalUnit(ou2.getFqn()).size());
        assertTrue(cms.getResourcesForOrganizationalUnit(ou2.getFqn()).contains(cms.readResource("/folder1")));
        try {
            // remove a resource
            cms.removeResourceFromOrgUnit(ou2.getFqn(), "/folder1");
            fail("it should not be possible to remove all resources");
        } catch (CmsDataAccessException e) {
            assertEquals(
                ((CmsDataAccessException)e.getCause()).getMessageContainer().getKey(),
                org.opencms.db.generic.Messages.ERR_ORGUNIT_REMOVE_LAST_RESOURCE_2);
            // ok, check again just to be sure
            assertEquals(1, cms.getResourcesForOrganizationalUnit(ou2.getFqn()).size());
            assertTrue(cms.getResourcesForOrganizationalUnit(ou2.getFqn()).contains(cms.readResource("/folder1")));
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
        assertEquals(4, cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).size());
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(
            cms.readGroup(OpenCms.getDefaultUsers().getGroupGuests())));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readGroup("/group1")));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readGroup("/group2")));
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), false).contains(cms.readGroup("/group3")));
        assertEquals(4, cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).size());
        assertTrue(cms.getGroupsForOrganizationalUnit(rootOu.getFqn(), true).contains(
            cms.readGroup(OpenCms.getDefaultUsers().getGroupGuests())));
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

        rootOu.setDescription("new description");
        cms.writeOrganizationalUnit(rootOu);

        try {
            rootOu.setParentFqn(rootOu.getFqn());
            cms.writeOrganizationalUnit(rootOu);
            fail("should not be able to edit the root ou");
        } catch (CmsIllegalStateException e) {
            // ok, ignore
        }

        try {
            cms.deleteOrganizationalUnit(rootOu.getFqn());
            fail("should not be able to delete the root ou");
        } catch (CmsDbConsistencyException e) {
            // ok, ignore
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

        cms.addUserToGroup("/test/test1", "/Administrators");
        cms.loginUser("/test/test2/test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        try {
            cms.deleteOrganizationalUnit(cms.getRequestContext().getOuFqn());
            fail("it should not be possible to delete an organizational unit that is used in the current request context");
        } catch (CmsDbConsistencyException e) {
            // ok, now check the error message
            assertEquals(
                ((CmsDbConsistencyException)e.getCause()).getMessageContainer().getKey(),
                org.opencms.db.Messages.ERR_ORGUNIT_DELETE_IN_CONTEXT_1);
        }
        try {
            cms.deleteOrganizationalUnit(cms.getRequestContext().currentUser().getOrganizationalUnitFqn());
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
            cms.deleteOrganizationalUnit("/test");
            fail("it should not be possible to delete an organizational unit that has sub-organizational units");
        } catch (CmsDbConsistencyException e) {
            // ok, now check the error message
            assertEquals(
                ((CmsDbConsistencyException)e.getCause()).getMessageContainer().getKey(),
                org.opencms.db.Messages.ERR_ORGUNIT_DELETE_SUB_ORGUNITS_1);
        }
        CmsOrganizationalUnit ou2 = cms.readOrganizationalUnit("/test/test2");
        try {
            cms.deleteOrganizationalUnit(ou2.getFqn());
            fail("it should not be possible to delete an organizational unit that has groups & users units");
        } catch (CmsDbConsistencyException e) {
            // ok, now check the error message
            assertEquals(
                ((CmsDbConsistencyException)e.getCause()).getMessageContainer().getKey(),
                org.opencms.db.Messages.ERR_ORGUNIT_DELETE_GROUPS_1);
        }

        Iterator itGroups = cms.getGroupsForOrganizationalUnit(ou2.getFqn(), false).iterator();
        while (itGroups.hasNext()) {
            CmsGroup group = (CmsGroup)itGroups.next();
            cms.deleteGroup(group.getName());
        }
        assertTrue(cms.getGroupsForOrganizationalUnit(ou2.getFqn(), true).isEmpty());

        try {
            cms.deleteOrganizationalUnit(ou2.getFqn());
            fail("it should not be possible to delete an organizational unit that has users units");
        } catch (CmsDbConsistencyException e) {
            // ok, now check the error message
            assertEquals(
                ((CmsDbConsistencyException)e.getCause()).getMessageContainer().getKey(),
                org.opencms.db.Messages.ERR_ORGUNIT_DELETE_USERS_1);
        }

        Iterator itUsers = cms.getUsersForOrganizationalUnit(ou2.getFqn(), false).iterator();
        while (itUsers.hasNext()) {
            CmsUser user = (CmsUser)itUsers.next();
            cms.deleteUser(user.getName());
        }
        assertTrue(cms.getUsersForOrganizationalUnit(ou2.getFqn(), true).isEmpty());

        cms.deleteOrganizationalUnit(ou2.getFqn());

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
        CmsOrganizationalUnit rootOu = cms.readOrganizationalUnit("");
        CmsOrganizationalUnit ou = cms.readOrganizationalUnit("/test");
        CmsOrganizationalUnit ou2 = cms.readOrganizationalUnit(ou.getFqn() + "/test2");

        // check offline project
        CmsFolder rootOuFolder = cms.readFolder("/system/orgunits/root/");
        assertEquals(rootOu.getId(), rootOuFolder.getStructureId());
        assertEquals(rootOu.getFlags(), rootOuFolder.getFlags() & ~CmsResource.FLAG_INTERNAL);
        assertTrue(rootOuFolder.getState().isUnchanged());
        assertTrue(rootOuFolder.isInternal());
        assertEquals(rootOu.getDescription(), cms.readPropertyObject(
            rootOuFolder,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            false).getValue());
        assertOrgUnitResources(cms, rootOu);

        CmsFolder ouFolder = cms.readFolder("/system/orgunits/root/test");
        assertEquals(ou.getId(), ouFolder.getStructureId());
        assertEquals(ou.getFlags(), ouFolder.getFlags() & ~CmsResource.FLAG_INTERNAL);
        assertTrue(ouFolder.getState().isUnchanged());
        assertTrue(ouFolder.isInternal());
        assertEquals(ou.getDescription(), cms.readPropertyObject(
            ouFolder,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            false).getValue());
        assertOrgUnitResources(cms, ou);

        CmsFolder ou2Folder = cms.readFolder("/system/orgunits/root/test/test2");
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
        rootOuFolder = cms.readFolder("/system/orgunits/root/");
        assertEquals(rootOu.getId(), rootOuFolder.getStructureId());
        assertEquals(rootOu.getFlags(), rootOuFolder.getFlags() & ~CmsResource.FLAG_INTERNAL);
        assertTrue(rootOuFolder.getState().isUnchanged());
        assertTrue(rootOuFolder.isInternal());
        assertEquals(rootOu.getDescription(), cms.readPropertyObject(
            rootOuFolder,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            false).getValue());
        assertOrgUnitResources(cms, rootOu);

        ouFolder = cms.readFolder("/system/orgunits/root/test");
        assertEquals(ou.getId(), ouFolder.getStructureId());
        assertEquals(ou.getFlags(), ouFolder.getFlags() & ~CmsResource.FLAG_INTERNAL);
        assertTrue(ouFolder.getState().isUnchanged());
        assertTrue(ouFolder.isInternal());
        assertEquals(ou.getDescription(), cms.readPropertyObject(
            ouFolder,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            false).getValue());
        assertOrgUnitResources(cms, ou);

        ou2Folder = cms.readFolder("/system/orgunits/root/test/test2");
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
        Iterator itResources = cms.getResourcesForOrganizationalUnit(ou.getFqn()).iterator();
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

        assertEquals(3, cms.getUsersForOrganizationalUnit("/", false).size());
        assertEquals(5, cms.getUsersForOrganizationalUnit("/", true).size());
        // try to create another user 'test1' in the root ou 
        cms.createUser("/test1", "test1", "test user", null);

        assertEquals(4, cms.getUsersForOrganizationalUnit("/", false).size());
        assertEquals(6, cms.getUsersForOrganizationalUnit("/", true).size());

        assertEquals(1, cms.getUsersForOrganizationalUnit("/test", false).size());
        assertEquals(2, cms.getUsersForOrganizationalUnit("/test", true).size());
        try {
            // try to create another user 'test1' in the /test ou 
            cms.createUser("/test/test1", "test1", "test user", null);
            fail("it could not be possible to create 2 users with the same name in an ou");
        } catch (CmsVfsException e) {
            // ok, ignore
        }
        assertEquals(1, cms.getUsersForOrganizationalUnit("/test", false).size());
        assertEquals(2, cms.getUsersForOrganizationalUnit("/test", true).size());

        assertEquals(2, cms.getGroupsForOrganizationalUnit("/", false).size());
        assertEquals(4, cms.getGroupsForOrganizationalUnit("/", true).size());
        // try to create another group 'group1' in the root ou 
        cms.createGroup("/group1", "test group", 0, null);

        assertEquals(3, cms.getGroupsForOrganizationalUnit("/", false).size());
        assertEquals(5, cms.getGroupsForOrganizationalUnit("/", true).size());

        assertEquals(1, cms.getGroupsForOrganizationalUnit("/test", false).size());
        assertEquals(2, cms.getGroupsForOrganizationalUnit("/test", true).size());
        try {
            // try to create another group 'group1' in the /test ou 
            cms.createGroup("/test/group1", "test group", 0, null);
            fail("it could not be possible to create 2 groups with the same name in an ou");
        } catch (CmsVfsException e) {
            // ok, ignore
        }
        assertEquals(1, cms.getGroupsForOrganizationalUnit("/test", false).size());
        assertEquals(2, cms.getGroupsForOrganizationalUnit("/test", true).size());

        assertEquals(1, cms.getUsersOfGroup("/test/group1").size());
        assertTrue(cms.getUsersOfGroup("/test/group1").contains(cms.readUser("/test/test1")));
        assertTrue(cms.getGroupsOfUser("/test1").isEmpty());
        // add user of root ou to group of 1st level ou
        cms.addUserToGroup("/test1", "/test/group1");
        assertEquals(2, cms.getUsersOfGroup("/test/group1").size());
        assertTrue(cms.getUsersOfGroup("/test/group1").contains(cms.readUser("/test/test1")));
        assertTrue(cms.getUsersOfGroup("/test/group1").contains(cms.readUser("/test1")));
        assertEquals(1, cms.getGroupsOfUser("/test1").size());
        assertTrue(cms.getGroupsOfUser("/test1").contains(cms.readGroup("/test/group1")));

        assertTrue(cms.getUsersOfGroup("/group1").isEmpty());
        assertEquals(1, cms.getGroupsOfUser("/test/test1").size());
        assertTrue(cms.getGroupsOfUser("/test/test1").contains(cms.readGroup("/test/group1")));
        // add user of 1st level ou to group of root ou
        cms.addUserToGroup("/test/test1", "/group1");
        assertEquals(1, cms.getUsersOfGroup("/group1").size());
        assertTrue(cms.getUsersOfGroup("/group1").contains(cms.readUser("/test/test1")));
        assertEquals(2, cms.getGroupsOfUser("/test/test1").size());
        assertTrue(cms.getGroupsOfUser("/test/test1").contains(cms.readGroup("/test/group1")));
        assertTrue(cms.getGroupsOfUser("/test/test1").contains(cms.readGroup("/group1")));
    }
}

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

import org.opencms.main.OpenCms;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestResourceFilter;

import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for the "chacc" method of the CmsObject.<p>
 *
 */
public class TestChacc extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestChacc(String arg0) {

        super(arg0);
    }

    /**
     * Test the chacc method on a file and a group.<p>
     *
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to change permissions
     * @param group the group to change the permissions from
     * @param permissions the new permission set for this group
     * @param flags the flags for modifying the permission set
     * @throws Throwable if something goes wrong
     */
    public static void chaccFileGroup(
        OpenCmsTestCase tc,
        CmsObject cms,
        String resource1,
        CmsGroup group,
        CmsPermissionSet permissions,
        int flags) throws Throwable {

        tc.storeResources(cms, resource1);

        cms.lockResource(resource1);
        cms.chacc(
            resource1,
            I_CmsPrincipal.PRINCIPAL_GROUP,
            group.getName(),
            permissions.getAllowedPermissions(),
            permissions.getDeniedPermissions(),
            flags);
        cms.unlockResource(resource1);

        // now evaluate the result
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_CHACC);
        // test the ace of the new permission
        // add the group flag to the acl
        CmsResource res = cms.readResource(resource1, CmsResourceFilter.ALL);

        CmsAccessControlEntry ace = new CmsAccessControlEntry(
            res.getResourceId(),
            group.getId(),
            permissions.getAllowedPermissions(),
            permissions.getDeniedPermissions(),
            flags + CmsAccessControlEntry.ACCESS_FLAGS_GROUP);
        tc.assertAce(cms, resource1, ace);
        // test the acl with the permission set
        int denied = permissions.getDeniedPermissions();
        if (flags == CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE) {
            denied = 0;
        }
        CmsPermissionSet permission = new CmsPermissionSet(permissions.getAllowedPermissions(), denied);
        tc.assertAcl(cms, resource1, group.getId(), permission);
    }

    /**
     * Test the chacc method on a file and a user.<p>
     *
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to change permissions
     * @param user the user to change the permissions from
     * @param permissions the new permission set for this group
     * @param flags the flags for modifying the permission set
     * @throws Throwable if something goes wrong
     */
    public static void chaccFileUser(
        OpenCmsTestCase tc,
        CmsObject cms,
        String resource1,
        CmsUser user,
        CmsPermissionSet permissions,
        int flags) throws Throwable {

        tc.storeResources(cms, resource1);

        cms.lockResource(resource1);
        cms.chacc(
            resource1,
            I_CmsPrincipal.PRINCIPAL_USER,
            user.getName(),
            permissions.getAllowedPermissions(),
            permissions.getDeniedPermissions(),
            flags);
        cms.unlockResource(resource1);

        // now evaluate the result
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_CHACC);
        // test the ace of the new permission
        // add the user flag to the acl
        CmsResource res = cms.readResource(resource1, CmsResourceFilter.ALL);

        CmsAccessControlEntry ace = new CmsAccessControlEntry(
            res.getResourceId(),
            user.getId(),
            permissions.getAllowedPermissions(),
            permissions.getDeniedPermissions(),
            flags + CmsAccessControlEntry.ACCESS_FLAGS_USER);
        tc.assertAce(cms, resource1, ace);
        // test the acl with the permission set
        int denied = permissions.getDeniedPermissions();
        if (flags == CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE) {
            denied = 0;
        }
        CmsPermissionSet permission = new CmsPermissionSet(permissions.getAllowedPermissions(), denied);
        tc.assertAcl(cms, resource1, user.getId(), permission);
    }

    /**
     * Test the chacc method on a file and a group.<p>
     *
     * @param tc the OpenCmsTestCase
     * @param cms the CmsObject
     * @param resource1 the resource to change permissions
     * @param group the group to change the permissions from
     * @param permissions the new permission set for this group
     * @param flags the flags for modifying the permission set
     * @throws Throwable if something goes wrong
     */
    public static void chaccFolderGroup(
        OpenCmsTestCase tc,
        CmsObject cms,
        String resource1,
        CmsGroup group,
        CmsPermissionSet permissions,
        int flags) throws Throwable {

        tc.storeResources(cms, resource1);

        cms.lockResource(resource1);
        cms.chacc(
            resource1,
            I_CmsPrincipal.PRINCIPAL_GROUP,
            group.getName(),
            permissions.getAllowedPermissions(),
            permissions.getDeniedPermissions(),
            flags);
        cms.unlockResource(resource1);

        // now evaluate the result
        tc.assertFilter(cms, resource1, OpenCmsTestResourceFilter.FILTER_CHACC);
        // test the ace of the new permission
        // add the group flag to the acl
        CmsResource res = cms.readResource(resource1, CmsResourceFilter.ALL);

        CmsAccessControlEntry ace = new CmsAccessControlEntry(
            res.getResourceId(),
            group.getId(),
            permissions.getAllowedPermissions(),
            permissions.getDeniedPermissions(),
            flags + CmsAccessControlEntry.ACCESS_FLAGS_GROUP);
        tc.assertAce(cms, resource1, ace);
        // test the acl with the permission set
        int denied = permissions.getDeniedPermissions();
        if ((flags & CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE) > 0) {
            denied = 0;
        }
        CmsPermissionSet permission = new CmsPermissionSet(permissions.getAllowedPermissions(), denied);
        tc.assertAcl(cms, resource1, group.getId(), permission);

        // now check all the subresources in the folder, access must be modified as well
        List subresources = cms.readResources(resource1, CmsResourceFilter.ALL);
        Iterator j = subresources.iterator();

        while (j.hasNext()) {
            CmsResource subRes = (CmsResource)j.next();
            String subResName = cms.getSitePath(subRes);
            // now evaluate the result
            tc.assertFilter(cms, subResName, OpenCmsTestResourceFilter.FILTER_CHACC);
            // test the ace of the new permission
            // add the group and the inherited flag to the acl
            ace = new CmsAccessControlEntry(
                res.getResourceId(),
                group.getId(),
                permissions.getAllowedPermissions(),
                permissions.getDeniedPermissions(),
                flags + CmsAccessControlEntry.ACCESS_FLAGS_GROUP + CmsAccessControlEntry.ACCESS_FLAGS_INHERITED);
            tc.assertAce(cms, subResName, ace);

            // test the acl with the permission set
            permission = new CmsPermissionSet(permissions.getAllowedPermissions(), denied);
            tc.assertAcl(cms, resource1, subResName, group.getId(), permission);

        }
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestChacc.class.getName());

        suite.addTest(new TestChacc("testChaccFileGroup"));
        suite.addTest(new TestChacc("testChaccFileUser"));
        suite.addTest(new TestChacc("testChaccFileAllOthers"));
        suite.addTest(new TestChacc("testChaccFileOverwriteAll"));
        suite.addTest(new TestChacc("testChaccAddRemove"));

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
     * Test the creation and deletion of access control entries and checks permissions of a test user.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testChaccAddRemove() throws Throwable {

        echo("Testing adding and removing ACEs on files and folders");

        CmsObject cms = getCmsObject();
        cms.createGroup("Testgroup", "A test group", 0, null);
        CmsGroup testGroup = cms.readGroup("Testgroup");
        cms.createUser("testuser", "test", "A test user", null);
        cms.addUserToGroup("testuser", "Testgroup");
        OpenCms.getRoleManager().addUserToRole(cms, CmsRole.ELEMENT_AUTHOR, "testuser");
        CmsUser testUser = cms.readUser("testuser");

        CmsProject offline = cms.readProject("Offline");

        String resName = "/folder2/";

        cms.lockResource(resName);
        cms.chacc(resName, I_CmsPrincipal.PRINCIPAL_USER, testUser.getName(), "+r+w+v+i");
        cms.chacc(resName, I_CmsPrincipal.PRINCIPAL_GROUP, testGroup.getName(), "+r+v+i");
        cms.unlockResource(resName);
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        CmsPermissionSet permissions = new CmsPermissionSet(
            CmsPermissionSet.PERMISSION_READ | CmsPermissionSet.PERMISSION_VIEW | CmsPermissionSet.PERMISSION_WRITE,
            0);

        // check set permissions for the test user
        cms.loginUser("testuser", "test");
        cms.getRequestContext().setCurrentProject(offline);
        cms.lockResource(resName);
        assertTrue(cms.hasPermissions(cms.readResource(resName), permissions));
        assertTrue(cms.hasPermissions(cms.readResource("/folder2/index.html"), permissions));
        assertFalse(cms.hasPermissions(cms.readResource("/folder1/"), permissions));
        cms.unlockResource(resName);

        // switch back to Admin user and remove ACE
        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(offline);
        cms.lockResource(resName);
        cms.rmacc(resName, I_CmsPrincipal.PRINCIPAL_USER, testUser.getName());
        cms.unlockResource(resName);
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.loginUser("testuser", "test");
        cms.getRequestContext().setCurrentProject(offline);
        assertFalse(cms.hasPermissions(cms.readResource(resName), CmsPermissionSet.ACCESS_WRITE));

        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(offline);
        cms.lockResource(resName);
        cms.rmacc(resName, I_CmsPrincipal.PRINCIPAL_GROUP, testGroup.getName());
        cms.unlockResource(resName);
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        // re-check permissions of test user after removing ACE
        cms.loginUser("testuser", "test");
        assertFalse(cms.hasPermissions(cms.readResource(resName), permissions));
    }

    /**
     * Test the chacc method for the special 'all others' principal.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testChaccFileAllOthers() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing the chacc method for the special 'all others' principal");

        CmsPermissionSet permissions = CmsPermissionSet.ACCESS_READ;
        String resource = "/folder1/subfolder11/";
        int flags = CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE
            + CmsAccessControlEntry.ACCESS_FLAGS_ALLOTHERS
            + CmsAccessControlEntry.ACCESS_FLAGS_INHERIT;

        storeResources(cms, resource, true);

        cms.lockResource(resource);
        cms.chacc(
            resource,
            I_CmsPrincipal.PRINCIPAL_GROUP,
            CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME,
            permissions.getAllowedPermissions(),
            permissions.getDeniedPermissions(),
            flags);
        cms.unlockResource(resource);

        // now evaluate the result
        assertFilter(cms, resource, OpenCmsTestResourceFilter.FILTER_CHACC);
        // test the ace of the new permission
        // add the all others flag to the acl
        CmsResource res = cms.readResource(resource, CmsResourceFilter.ALL);
        CmsAccessControlEntry ace = new CmsAccessControlEntry(
            res.getResourceId(),
            CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID,
            permissions.getAllowedPermissions(),
            permissions.getDeniedPermissions(),
            flags);
        assertAce(cms, resource, ace);
        // test the acl with the permission set
        int denied = permissions.getDeniedPermissions();
        if ((flags & CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE) > 0) {
            denied = 0;
        }
        CmsPermissionSet permission = new CmsPermissionSet(permissions.getAllowedPermissions(), denied);
        assertAcl(cms, resource, CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID, permission);

        // now check all the subresources in the folder, access must be modified as well
        List subresources = cms.readResources(resource, CmsResourceFilter.ALL);
        Iterator j = subresources.iterator();

        while (j.hasNext()) {
            CmsResource subRes = (CmsResource)j.next();
            String subResName = cms.getSitePath(subRes);
            // now evaluate the result
            assertFilter(cms, subResName, OpenCmsTestResourceFilter.FILTER_CHACC);
            // test the ace of the new permission
            // add the group and the inherited flag to the acl
            ace = new CmsAccessControlEntry(
                res.getResourceId(),
                CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID,
                permissions.getAllowedPermissions(),
                permissions.getDeniedPermissions(),
                flags + CmsAccessControlEntry.ACCESS_FLAGS_INHERITED);
            assertAce(cms, subResName, ace);

            // test the acl with the permission set
            permission = new CmsPermissionSet(permissions.getAllowedPermissions(), denied);
            assertAcl(cms, resource, subResName, CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID, permission);
        }
    }

    /**
     * Test the chacc method on a file and a group.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testChaccFileGroup() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing chacc on a file and a group");
        chaccFileGroup(
            this,
            cms,
            "/index.html",
            cms.readGroup("Users"),
            CmsPermissionSet.ACCESS_READ,
            CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE);
    }

    /**
     * Test the chacc method for the special 'overwrite all' principal.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testChaccFileOverwriteAll() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing the chacc method for the special 'overwrite all' principal");

        CmsPermissionSet permissions = CmsPermissionSet.ACCESS_READ;
        String resource = "/folder1/subfolder11/";
        int flags = CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE
            + CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE_ALL
            + CmsAccessControlEntry.ACCESS_FLAGS_INHERIT;

        storeResources(cms, resource, true);

        cms.lockResource(resource);
        cms.chacc(
            resource,
            I_CmsPrincipal.PRINCIPAL_GROUP,
            CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_NAME,
            permissions.getAllowedPermissions(),
            permissions.getDeniedPermissions(),
            flags);
        cms.unlockResource(resource);

        // now evaluate the result
        assertFilter(cms, resource, OpenCmsTestResourceFilter.FILTER_CHACC);
        // test the ace of the new permission
        // add the all others flag to the acl
        CmsResource res = cms.readResource(resource, CmsResourceFilter.ALL);
        CmsAccessControlEntry ace = new CmsAccessControlEntry(
            res.getResourceId(),
            CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID,
            permissions.getAllowedPermissions(),
            permissions.getDeniedPermissions(),
            flags);
        assertAce(cms, resource, ace);
        // test the acl with the permission set
        int denied = permissions.getDeniedPermissions();
        if ((flags & CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE) > 0) {
            denied = 0;
        }
        CmsPermissionSet permission = new CmsPermissionSet(permissions.getAllowedPermissions(), denied);
        assertAcl(cms, resource, CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID, permission);

        // now check all the subresources in the folder, access must be modified as well
        List subresources = cms.readResources(resource, CmsResourceFilter.ALL);
        Iterator j = subresources.iterator();

        while (j.hasNext()) {
            CmsResource subRes = (CmsResource)j.next();
            String subResName = cms.getSitePath(subRes);
            // now evaluate the result
            assertFilter(cms, subResName, OpenCmsTestResourceFilter.FILTER_CHACC);
            // test the ace of the new permission
            // add the group and the inherited flag to the acl
            ace = new CmsAccessControlEntry(
                res.getResourceId(),
                CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID,
                permissions.getAllowedPermissions(),
                permissions.getDeniedPermissions(),
                flags + CmsAccessControlEntry.ACCESS_FLAGS_INHERITED);
            assertAce(cms, subResName, ace);

            // test the acl with the permission set
            permission = new CmsPermissionSet(permissions.getAllowedPermissions(), denied);
            assertAcl(cms, resource, subResName, CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID, permission);
        }
    }

    /**
     * Test the chacc method on a file and a user.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testChaccFileUser() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing chacc on a file and a user");
        chaccFileUser(this, cms, "/folder1/index.html", cms.readUser("Guest"), CmsPermissionSet.ACCESS_WRITE, 0);
    }

    /**
     * Test the chacc method on a folder and a group.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testChaccFolderGroup() throws Throwable {

        //TODO: This test is not working correctly so far!
        CmsObject cms = getCmsObject();
        echo("Testing chacc on a folder and a group");
        chaccFolderGroup(
            this,
            cms,
            "/folder2/",
            cms.readGroup("Guests"),
            CmsPermissionSet.ACCESS_READ,
            CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE + CmsAccessControlEntry.ACCESS_FLAGS_INHERIT);
    }
}
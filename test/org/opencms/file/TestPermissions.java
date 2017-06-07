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

import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsPermissionSetCustom;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsUUID;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for VFS permissions.<p>
 */
public class TestPermissions extends OpenCmsTestCase {

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestPermissions.class.getName());

        suite.addTest(new TestPermissions("testLockStatusPermission"));
        suite.addTest(new TestPermissions("testPublishPermissions"));
        suite.addTest(new TestPermissions("testSiblingPermissions"));
        suite.addTest(new TestPermissions("testVisiblePermission"));
        suite.addTest(new TestPermissions("testVisiblePermissionForFolder"));
        suite.addTest(new TestPermissions("testFilterForFolder"));
        suite.addTest(new TestPermissions("testDefaultPermissions"));
        suite.addTest(new TestPermissions("testPermissionOverwrite"));
        suite.addTest(new TestPermissions("testPermissionInheritance"));
        suite.addTest(new TestPermissions("testUserDeletion"));

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
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestPermissions(String arg0) {

        super(arg0);
    }

    /**
     * @throws Throwable if something goes wrong
     */
    public void testDefaultPermissions() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing default permissions");

        String resourcename = "testDefaultPermissions.txt";
        cms.createResource(resourcename, CmsResourceTypePlain.getStaticTypeId());

        cms.createUser("testAdmin", "secret", "", null);
        cms.addUserToGroup("testAdmin", OpenCms.getDefaultUsers().getGroupAdministrators());
        cms.createUser("testUser", "secret", "", null);
        cms.addUserToGroup("testUser", OpenCms.getDefaultUsers().getGroupUsers());
        cms.createUser("testGuest", "secret", "", null);
        cms.addUserToGroup("testGuest", OpenCms.getDefaultUsers().getGroupGuests());

        assertEquals("+r+w+v+c+d", cms.getPermissions(resourcename, "testAdmin").getPermissionString());
        assertEquals("+r+w+v", cms.getPermissions(resourcename, "testUser").getPermissionString());
        assertEquals("+r+v", cms.getPermissions(resourcename, "testGuest").getPermissionString());
    }

    /**
     * Test the resource filter files in a folder.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testFilterForFolder() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing resource filer for the files in a folder");

        String folder = "/types";
        // read only "image" resources
        List resultList;
        // resources in folder only method
        resultList = cms.getResourcesInFolder(
            folder,
            CmsResourceFilter.requireType(CmsResourceTypeImage.getStaticTypeId()));
        if (resultList.size() != 1) {
            fail("There is only 1 image resource in the folder, not " + resultList.size());
        }
        // files in folder only method
        resultList = cms.getFilesInFolder(
            folder,
            CmsResourceFilter.requireType(CmsResourceTypeImage.getStaticTypeId()));
        if (resultList.size() != 1) {
            fail("There is only 1 image resource in the folder, not " + resultList.size());
        }
        // subtree method
        resultList = cms.readResources(folder, CmsResourceFilter.requireType(CmsResourceTypeImage.getStaticTypeId()));
        if (resultList.size() != 1) {
            fail("There is only 1 image resource in the folder, not " + resultList.size());
        }
    }

    /**
     * Test the lock status permissions.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testLockStatusPermission() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing lock status permissions on a file");

        String resource = "/folder1/page1.html";
        CmsResource res = cms.readResource(resource);

        // first lock resource as user "test1"
        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        cms.lockResource(resource);
        assertTrue(cms.hasPermissions(res, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL));

        // now check resource as user "test2"
        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        assertTrue(cms.hasPermissions(res, CmsPermissionSet.ACCESS_WRITE, false, CmsResourceFilter.ALL));
        assertFalse(cms.hasPermissions(res, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL));

        // switch the lock to user "test2"
        cms.changeLock(resource);
        assertTrue(cms.hasPermissions(res, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL));

        // back to user "test1"
        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        assertTrue(cms.hasPermissions(res, CmsPermissionSet.ACCESS_WRITE, false, CmsResourceFilter.ALL));
        assertFalse(cms.hasPermissions(res, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL));

        // switch the lock to user "test1"
        cms.changeLock(resource);
        assertTrue(cms.hasPermissions(res, CmsPermissionSet.ACCESS_WRITE, true, CmsResourceFilter.ALL));
        cms.unlockResource(resource);
    }

    /**
     * Tests the inheritance of permissions.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testPermissionInheritance() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing inheritance of permissions");

        String foldername = "testPermissionInheritance";
        String subfoldername = foldername + "/" + "subfolder";
        String resourcename = foldername + "/test.txt";
        String subresourcename = subfoldername + "/subtest.txt";

        cms.createResource(foldername, CmsResourceTypeFolder.getStaticTypeId());
        cms.createResource(subfoldername, CmsResourceTypeFolder.getStaticTypeId());
        cms.createResource(resourcename, CmsResourceTypePlain.getStaticTypeId());
        cms.createResource(subresourcename, CmsResourceTypePlain.getStaticTypeId());

        assertEquals("+r+w+v", cms.getPermissions(resourcename, "testUser").getPermissionString());
        assertEquals("+r+w+v", cms.getPermissions(subfoldername, "testUser").getPermissionString());
        assertEquals("+r+w+v", cms.getPermissions(subresourcename, "testUser").getPermissionString());

        cms.chacc(foldername, I_CmsPrincipal.PRINCIPAL_GROUP, "Users", "+o");

        assertEquals("", cms.getPermissions(resourcename, "testUser").getPermissionString());
        assertEquals("+r+w+v", cms.getPermissions(subfoldername, "testUser").getPermissionString());
        assertEquals("+r+w+v", cms.getPermissions(subresourcename, "testUser").getPermissionString());

        cms.chacc(foldername, I_CmsPrincipal.PRINCIPAL_GROUP, "Users", "+o+i");
        assertEquals("", cms.getPermissions(resourcename, "testUser").getPermissionString());
        assertEquals("", cms.getPermissions(subfoldername, "testUser").getPermissionString());
        assertEquals("", cms.getPermissions(subresourcename, "testUser").getPermissionString());

        cms.createGroup("GroupA", "", 0, "");
        cms.createGroup("GroupB", "", 0, "");
        cms.createGroup("GroupC", "", 0, "");
        cms.createGroup("GroupD", "", 0, "");

        cms.addUserToGroup("testUser", "GroupA");
        cms.addUserToGroup("testUser", "GroupB");
        cms.addUserToGroup("testUser", "GroupC");
        cms.addUserToGroup("testUser", "GroupD");

        cms.chacc(foldername, I_CmsPrincipal.PRINCIPAL_GROUP, "GroupA", "+r");
        assertEquals("+r", cms.getPermissions(resourcename, "testUser").getPermissionString());
        assertEquals("", cms.getPermissions(subfoldername, "testUser").getPermissionString());
        assertEquals("", cms.getPermissions(subresourcename, "testUser").getPermissionString());

        cms.chacc(foldername, I_CmsPrincipal.PRINCIPAL_GROUP, "GroupA", "+r+i");
        assertEquals("+r", cms.getPermissions(resourcename, "testUser").getPermissionString());
        assertEquals("+r", cms.getPermissions(subfoldername, "testUser").getPermissionString());
        assertEquals("+r", cms.getPermissions(subresourcename, "testUser").getPermissionString());

        cms.chacc(foldername, I_CmsPrincipal.PRINCIPAL_GROUP, "GroupB", "+w");
        assertEquals("+r+w", cms.getPermissions(resourcename, "testUser").getPermissionString());
        assertEquals("+r", cms.getPermissions(subfoldername, "testUser").getPermissionString());
        assertEquals("+r", cms.getPermissions(subresourcename, "testUser").getPermissionString());

        cms.chacc(foldername, I_CmsPrincipal.PRINCIPAL_GROUP, "GroupB", "+w+i");
        assertEquals("+r+w", cms.getPermissions(resourcename, "testUser").getPermissionString());
        assertEquals("+r+w", cms.getPermissions(subfoldername, "testUser").getPermissionString());
        assertEquals("+r+w", cms.getPermissions(subresourcename, "testUser").getPermissionString());

        cms.chacc(foldername, I_CmsPrincipal.PRINCIPAL_GROUP, "GroupC", "-r");
        assertEquals("-r+w", cms.getPermissions(resourcename, "testUser").getPermissionString());
        assertEquals("+r+w", cms.getPermissions(subfoldername, "testUser").getPermissionString());
        assertEquals("+r+w", cms.getPermissions(subresourcename, "testUser").getPermissionString());

        cms.chacc(foldername, I_CmsPrincipal.PRINCIPAL_GROUP, "GroupC", "-r+i");
        assertEquals("-r+w", cms.getPermissions(resourcename, "testUser").getPermissionString());
        assertEquals("-r+w", cms.getPermissions(subfoldername, "testUser").getPermissionString());
        assertEquals("-r+w", cms.getPermissions(subresourcename, "testUser").getPermissionString());

        cms.chacc(foldername, I_CmsPrincipal.PRINCIPAL_GROUP, "GroupD", "-w");
        assertEquals("-r-w", cms.getPermissions(resourcename, "testUser").getPermissionString());
        assertEquals("-r+w", cms.getPermissions(subfoldername, "testUser").getPermissionString());
        assertEquals("-r+w", cms.getPermissions(subresourcename, "testUser").getPermissionString());

        cms.chacc(foldername, I_CmsPrincipal.PRINCIPAL_GROUP, "GroupD", "-w+i");
        assertEquals("-r-w", cms.getPermissions(resourcename, "testUser").getPermissionString());
        assertEquals("-r-w", cms.getPermissions(subfoldername, "testUser").getPermissionString());
        assertEquals("-r-w", cms.getPermissions(subresourcename, "testUser").getPermissionString());
    }

    /**
     * Tests the overwriting of permissions.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testPermissionOverwrite() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing permission overwrite");

        String foldername = "testPermissionOverwrite";
        cms.createResource(foldername, CmsResourceTypeFolder.getStaticTypeId());

        assertEquals("+r+w+v", cms.getPermissions(foldername, "testUser").getPermissionString());

        cms.chacc(foldername, I_CmsPrincipal.PRINCIPAL_GROUP, "Users", "+o");
        assertEquals("", cms.getPermissions(foldername, "testUser").getPermissionString());

        cms.chacc(foldername, I_CmsPrincipal.PRINCIPAL_GROUP, "Users", "-r");
        assertEquals("-r+w+v", cms.getPermissions(foldername, "testUser").getPermissionString());
    }

    /**
     * Test the publish permissions.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testPublishPermissions() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing publish permissions for a user");

        // Update for 9.5:
        // In the default installation the "Users" group now is set as manager group for the Offline project.
        // Therefore all members of "Users" can publish everything in the Offline project.
        // Direct publish is always granted in this case.
        // So we first delete the default Offline project and create a new Offline project
        // where the users are NOT allowed to publish, and switch to this.
        cms.deleteProject(cms.readProject("Offline").getUuid());
        cms.createProject(
            "Offline",
            "Project used for test case",
            OpenCms.getDefaultUsers().getGroupAdministrators(),
            OpenCms.getDefaultUsers().getGroupAdministrators());
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        cms.copyResourceToProject("/");

        String resource = "/folder1/page1.html";

        cms.lockResource(resource);
        // modify the resource permissions for the tests
        // remove all "Users" group permissions
        cms.chacc(
            resource,
            I_CmsPrincipal.PRINCIPAL_GROUP,
            OpenCms.getDefaultUsers().getGroupUsers(),
            0,
            0,
            CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE);

        // allow read and write for user "test1"
        cms.chacc(
            resource,
            I_CmsPrincipal.PRINCIPAL_USER,
            "test1",
            CmsPermissionSet.PERMISSION_READ + CmsPermissionSet.PERMISSION_WRITE,
            0,
            CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE);
        // allow read and write and direct publish for user "test2"
        cms.chacc(
            resource,
            I_CmsPrincipal.PRINCIPAL_USER,
            "test2",
            CmsPermissionSet.PERMISSION_READ
                + CmsPermissionSet.PERMISSION_WRITE
                + CmsPermissionSet.PERMISSION_DIRECT_PUBLISH,
            0,
            CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE);
        cms.unlockResource(resource);

        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        try {
            OpenCms.getPublishManager().getPublishList(cms, cms.readResource(resource), false);
            fail("Publish permissions available but should not be available for user test1");
        } catch (Exception e) {
            // ok, continue
        }

        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        try {
            OpenCms.getPublishManager().getPublishList(cms, cms.readResource(resource), false);
        } catch (Exception e) {
            fail("Publish permissions unavailable but should be available for user test2");
        }

        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        try {
            OpenCms.getPublishManager().getPublishList(cms, cms.readResource(resource), false);
        } catch (Exception e) {
            fail("Publish permissions unavailable but should be available for user Admin");
        }

        // create a new folder
        String folder = "/newfolder/";
        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        // create a new folder
        cms.createResource(folder, CmsResourceTypeFolder.getStaticTypeId());

        // apply permissions to folder
        cms.lockResource(folder);
        // modify the resource permissions for the tests
        // remove all "Users" group permissions
        cms.chacc(
            folder,
            I_CmsPrincipal.PRINCIPAL_GROUP,
            OpenCms.getDefaultUsers().getGroupUsers(),
            0,
            0,
            CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE + CmsAccessControlEntry.ACCESS_FLAGS_INHERIT);
        // allow only read and write for user "test1"
        cms.chacc(
            folder,
            I_CmsPrincipal.PRINCIPAL_USER,
            "test1",
            CmsPermissionSet.PERMISSION_READ + CmsPermissionSet.PERMISSION_WRITE,
            0,
            CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE + CmsAccessControlEntry.ACCESS_FLAGS_INHERIT);
        // allow read, write and and direct publish for user "test2"
        cms.chacc(
            folder,
            I_CmsPrincipal.PRINCIPAL_USER,
            "test2",
            CmsPermissionSet.PERMISSION_READ
                + CmsPermissionSet.PERMISSION_WRITE
                + CmsPermissionSet.PERMISSION_DIRECT_PUBLISH,
            0,
            CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE + CmsAccessControlEntry.ACCESS_FLAGS_INHERIT);
        cms.unlockResource(folder);

        resource = "/newfolder/newpage.html";
        cms.createResource(
            resource,
            CmsResourceTypePlain.getStaticTypeId(),
            "This is a test".getBytes(),
            Collections.EMPTY_LIST);
        cms.unlockResource(resource);

        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        try {
            OpenCms.getPublishManager().getPublishList(cms, cms.readResource(resource), false);
            fail("Publish permissions available but should not be available for user test1");
        } catch (Exception e) {
            // ok, ignore
        }

        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        try {
            OpenCms.getPublishManager().getPublishList(cms, cms.readResource(resource), false);
            fail(
                "Publish permissions available but should be unavailable for user test2 because the parent folder is new");
        } catch (Exception e) {
            // ok, ignore
        }
        try {
            OpenCms.getPublishManager().getPublishList(cms, cms.readResource(folder), false);
        } catch (Exception e) {
            fail("Publish permissions on new folder unavailable but should be available for user test2");
        }
        OpenCms.getPublishManager().publishResource(cms, folder);
        OpenCms.getPublishManager().waitWhileRunning();
        try {
            OpenCms.getPublishManager().getPublishList(cms, cms.readResource(resource), false);
        } catch (Exception e) {
            fail(
                "Publish permissions unavailable but should be available for user test2 because the parent folder is now published");
        }
    }

    /**
     * Test the permissions on siblings.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testSiblingPermissions() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing permissions on siblings");

        String resName1 = "/folder1/page4.html";
        // set some permissions on a resource
        CmsResource res1 = cms.readResource(resName1);
        CmsUser user = cms.readUser(OpenCms.getDefaultUsers().getUserGuest());
        CmsPermissionSet permissions = new CmsPermissionSetCustom("-r");
        TestChacc.chaccFileUser(this, cms, resName1, user, permissions, 0);

        // create a sibling
        String resName2 = "/folder1/page4sib.html";
        cms.createSibling(resName1, resName2, null);

        // check that the sibling also has the permission set
        CmsAccessControlEntry ace = new CmsAccessControlEntry(
            res1.getResourceId(),
            user.getId(),
            permissions.getAllowedPermissions(),
            permissions.getDeniedPermissions(),
            CmsAccessControlEntry.ACCESS_FLAGS_USER);
        assertAce(cms, resName1, ace);
        assertAcl(cms, resName1, user.getId(), permissions);

        // delete the sibling
        cms.deleteResource(resName2, CmsResource.DELETE_PRESERVE_SIBLINGS);

        // check the permissions on the remaining resource
        assertAce(cms, resName1, ace);
        assertAcl(cms, resName1, user.getId(), permissions);

        // create the sibling again
        cms.createSibling(resName1, resName2, null);
        // check that the sibling also has the permission set
        assertAce(cms, resName1, ace);
        assertAcl(cms, resName1, user.getId(), permissions);

        // publish
        OpenCms.getPublishManager().publishResource(cms, resName2, true, new CmsLogReport(Locale.ENGLISH, getClass()));
        OpenCms.getPublishManager().waitWhileRunning();

        // delete the sibling
        cms.lockResource(resName2);
        cms.deleteResource(resName2, CmsResource.DELETE_PRESERVE_SIBLINGS);

        // publish
        OpenCms.getPublishManager().publishResource(cms, resName2);
        OpenCms.getPublishManager().waitWhileRunning();

        // check the permissions on the remaining resource
        assertAce(cms, resName1, ace);
        assertAcl(cms, resName1, user.getId(), permissions);
    }

    /**
     * Test permissions after deleting a user.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUserDeletion() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing permissions after deleting a user");

        String resourcename = "userDelete.txt";
        String username = "deleteUser";
        // create a resource
        cms.createResource(resourcename, CmsResourceTypePlain.getStaticTypeId());
        // create a user
        cms.createUser(username, "deleteMe", "", null);
        // add a permission for this user
        cms.chacc(resourcename, I_CmsPrincipal.PRINCIPAL_USER, username, "+r+w+v+c+d");
        // now delete the user again
        cms.deleteUser(username);

        // get all ace of this resource
        List aces = cms.getAccessControlEntries(resourcename);

        Iterator i = aces.iterator();
        // loop through all ace and check if the users/groups belonging to this entry still exist
        while (i.hasNext()) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry)i.next();

            CmsUUID principal = ace.getPrincipal();
            // the principal is missing, so the test must fail
            if (cms.lookupPrincipal(principal) == null) {
                if (CmsRole.valueOfId(principal) == null) { // it could also be a role
                    if (!principal.equals(CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID)
                        && !principal.equals(CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID)) { // or a special id
                        fail("Principal " + principal.toString() + " is missing");
                    }
                }
            }
        }
    }

    /**
     * Test the visible permissions.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testVisiblePermission() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing visible permissions on a file");

        String resource = "index.html";
        CmsResource res = cms.readResource(resource);

        cms.lockResource(resource);
        // modify the resource permissions for the tests
        // remove all "Users" group permissions
        cms.chacc(
            resource,
            I_CmsPrincipal.PRINCIPAL_GROUP,
            OpenCms.getDefaultUsers().getGroupUsers(),
            0,
            0,
            CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE);
        // allow only read for user "test1"
        cms.chacc(
            resource,
            I_CmsPrincipal.PRINCIPAL_USER,
            "test1",
            CmsPermissionSet.PERMISSION_READ,
            0,
            CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE);
        // allow read and visible for user "test2"
        cms.chacc(
            resource,
            I_CmsPrincipal.PRINCIPAL_USER,
            "test2",
            CmsPermissionSet.PERMISSION_READ + CmsPermissionSet.PERMISSION_VIEW,
            0,
            CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE);
        cms.unlockResource(resource);

        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        if (!cms.hasPermissions(
            res,
            new CmsPermissionSet(CmsPermissionSet.PERMISSION_VIEW, 0),
            true,
            CmsResourceFilter.ALL)) {
            fail("Visible permission checked but should have been ignored");
        }
        if (cms.hasPermissions(
            res,
            new CmsPermissionSet(CmsPermissionSet.PERMISSION_VIEW, 0),
            true,
            CmsResourceFilter.ONLY_VISIBLE)) {
            fail("Visible permission not checked");
        }

        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        if (!cms.hasPermissions(
            res,
            new CmsPermissionSet(CmsPermissionSet.PERMISSION_VIEW, 0),
            true,
            CmsResourceFilter.ALL)) {
            fail("Visible permission checked but should be ignored");
        }
        if (!cms.hasPermissions(
            res,
            new CmsPermissionSet(CmsPermissionSet.PERMISSION_VIEW, 0),
            true,
            CmsResourceFilter.ONLY_VISIBLE)) {
            fail("Visible permission not detected");
        }
    }

    /**
     * Test the visible permissions on a list of files in a folder.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testVisiblePermissionForFolder() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing visible permissions on a list of files in a folder");

        String folder = "/types";

        // apply permissions to folder
        cms.lockResource(folder);
        // modify the resource permissions for the tests
        // remove all "Users" group permissions
        cms.chacc(
            folder,
            I_CmsPrincipal.PRINCIPAL_GROUP,
            OpenCms.getDefaultUsers().getGroupUsers(),
            0,
            0,
            CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE + CmsAccessControlEntry.ACCESS_FLAGS_INHERIT);
        // allow only read for user "test1"
        cms.chacc(
            folder,
            I_CmsPrincipal.PRINCIPAL_USER,
            "test1",
            CmsPermissionSet.PERMISSION_READ,
            0,
            CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE + CmsAccessControlEntry.ACCESS_FLAGS_INHERIT);
        // allow read and visible for user "test2"
        cms.chacc(
            folder,
            I_CmsPrincipal.PRINCIPAL_USER,
            "test2",
            CmsPermissionSet.PERMISSION_READ + CmsPermissionSet.PERMISSION_VIEW,
            0,
            CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE + CmsAccessControlEntry.ACCESS_FLAGS_INHERIT);
        cms.unlockResource(folder);

        List resultList;

        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        // read excluding invisible resources
        resultList = cms.readResources(folder, CmsResourceFilter.ONLY_VISIBLE);
        if (resultList.size() > 0) {
            fail(
                "Was able to read "
                    + resultList.size()
                    + " invisible resources in a folder with filter excluding invisible resources");
        }
        boolean hasViewAccess = cms.hasPermissions(
            cms.readResource(folder, CmsResourceFilter.ALL),
            CmsPermissionSet.ACCESS_VIEW,
            false,
            CmsResourceFilter.ONLY_VISIBLE);
        assertFalse("the user has view access permission despite the view permission has been removed", hasViewAccess);
        // read again now including invisible resources
        resultList = cms.readResources(folder, CmsResourceFilter.ALL);
        if (resultList.size() != 6) {
            fail("There should be 6 visible resource in the folder, not " + resultList.size());
        }

        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        resultList = cms.readResources(folder, CmsResourceFilter.ONLY_VISIBLE);
        if (resultList.size() != 6) {
            fail("There should be 6 visible resource in the folder, not " + resultList.size());
        }
    }
}

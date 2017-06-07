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

import org.opencms.db.CmsResourceState;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.lock.CmsLockType;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPermissionViolationException;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for VFS permissions.<p>
 */
public class TestDeletion extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestDeletion(String arg0) {

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
        suite.setName(TestDeletion.class.getName());

        suite.addTest(new TestDeletion("testGroupDeletion"));
        suite.addTest(new TestDeletion("testDeleteFolderAfterMove"));
        suite.addTest(new TestDeletion("testDeleteFolderAfterMoveWithLock"));
        suite.addTest(new TestDeletion("testDeleteFolderAfterDeleteWithLock"));
        suite.addTest(new TestDeletion("testAdvancedGroupDeletion"));
        suite.addTest(new TestDeletion("testUserDeletion"));
        suite.addTest(new TestDeletion("testDeleteFolderWithUnvisibleResources"));
        suite.addTest(new TestDeletion("testDeleteFolderWithLockedSiblings"));
        suite.addTest(new TestDeletion("testDeleteFolderWithLockedResources"));
        suite.addTest(new TestDeletion("testDeleteWithoutWritePermissions"));

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
     * Tests an advanced group deletion.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testAdvancedGroupDeletion() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing advanced group deletion");

        // create test group
        cms.createGroup("Testgroup", "A test group", 0, null);
        CmsGroup testGroup = cms.readGroup("Testgroup");
        // set a child
        cms.setParentGroup(OpenCms.getDefaultUsers().getGroupUsers(), testGroup.getName());
        // create test user
        cms.createUser("testuser1", "test1", "A test user 1", null);
        cms.addUserToGroup("testuser1", "Testgroup");
        CmsUser testUser1 = cms.readUser("testuser1");

        // login in offline project
        CmsProject offline = cms.readProject("Offline");
        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(offline);

        // change permission of a resource
        String resName = "/folder1/subfolder11/subsubfolder111/text.txt";
        cms.lockResource(resName);
        cms.chacc(resName, I_CmsPrincipal.PRINCIPAL_USER, testUser1.getName(), "+r+w+v+i");
        cms.chacc(resName, I_CmsPrincipal.PRINCIPAL_GROUP, testGroup.getName(), "+r+v+i");
        cms.unlockResource(resName);
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        // create a 2nd test group
        cms.createGroup("testgroup2", "A test group 2", 0, null);
        CmsGroup testGroup2 = cms.readGroup("testgroup2");

        // remember group data
        List children = cms.getChildren(testGroup.getName(), false);
        List users = cms.getUsersOfGroup(testGroup.getName());

        // delete the test group
        cms.deleteGroup(testGroup.getId(), testGroup2.getId());

        // check ace for the resource
        boolean found = false;
        Iterator it = cms.getAccessControlEntries(resName, false).iterator();
        while (it.hasNext()) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry)it.next();
            if (ace.getPrincipal().equals(testGroup2.getId())) {
                CmsAccessControlEntry newAce = new CmsAccessControlEntry(
                    cms.readResource(resName).getResourceId(),
                    testGroup2.getId(),
                    "+r+v+i");
                newAce.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_GROUP);
                assertTrue(newAce.equals(ace));
                found = true;
            }
        }
        assertTrue(found);

        // check group data
        assertEquals(children, cms.getChildren(testGroup2.getName(), false));
        assertEquals(users, cms.getUsersOfGroup(testGroup2.getName()));

        // restore the previous state
        cms.deleteUser(testUser1.getId());
        cms.setParentGroup(OpenCms.getDefaultUsers().getGroupUsers(), null);
        cms.deleteGroup(testGroup2.getName());
    }

    /**
     * Tests to delete a folder after deleting a subresource with lock.<p>
     *
     * @throws Exception if the test fails
     */
    public void testDeleteFolderAfterDeleteWithLock() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to delete a folder after deleting a subresource with lock");

        CmsResource folder = cms.createResource("breakFolder", CmsResourceTypeFolder.getStaticTypeId());
        CmsResource file1 = cms.createResource(
            "breakFolder/file1.txt",
            CmsResourceTypePlain.getStaticTypeId(),
            "test1".getBytes(),
            null);
        cms.createResource("breakFolder/file2.txt", CmsResourceTypePlain.getStaticTypeId(), "test2".getBytes(), null);
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.lockResource(cms.getSitePath(file1));
        cms.deleteResource(cms.getSitePath(file1), CmsResource.DELETE_PRESERVE_SIBLINGS);

        OpenCms.getRoleManager().addUserToRole(cms, CmsRole.PROJECT_MANAGER, "test2");

        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        cms.lockResource(cms.getSitePath(folder));
        cms.deleteResource(cms.getSitePath(folder), CmsResource.DELETE_PRESERVE_SIBLINGS);

        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Tests to delete a folder after moving a subresource.<p>
     *
     * @throws Exception if the test fails
     */
    public void testDeleteFolderAfterMove() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to delete a folder after moving a subresource");

        CmsResource folder = cms.createResource("breakFolder", CmsResourceTypeFolder.getStaticTypeId());
        CmsResource file1 = cms.createResource(
            "breakFolder/file1.txt",
            CmsResourceTypePlain.getStaticTypeId(),
            "test1".getBytes(),
            null);
        cms.createResource("breakFolder/file2.txt", CmsResourceTypePlain.getStaticTypeId(), "test2".getBytes(), null);
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.lockResource(cms.getSitePath(file1));
        cms.moveResource(cms.getSitePath(file1), "folder1/movedFile1.txt");

        cms.lockResource(cms.getSitePath(folder));
        cms.deleteResource(cms.getSitePath(folder), CmsResource.DELETE_PRESERVE_SIBLINGS);

        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Tests to delete a folder after moving a subresource with lock.<p>
     *
     * @throws Exception if the test fails
     */
    public void testDeleteFolderAfterMoveWithLock() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to delete a folder after moving a subresource with lock");

        CmsResource folder = cms.createResource("breakFolder", CmsResourceTypeFolder.getStaticTypeId());
        CmsResource file1 = cms.createResource(
            "breakFolder/file1.txt",
            CmsResourceTypePlain.getStaticTypeId(),
            "test1".getBytes(),
            null);
        cms.createResource("breakFolder/file2.txt", CmsResourceTypePlain.getStaticTypeId(), "test2".getBytes(), null);
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.lockResource(cms.getSitePath(file1));
        cms.moveResource(cms.getSitePath(file1), "folder1/movedFile2.txt");

        OpenCms.getRoleManager().addUserToRole(cms, CmsRole.PROJECT_MANAGER, "test2");

        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        cms.lockResource(cms.getSitePath(folder));
        cms.deleteResource(cms.getSitePath(folder), CmsResource.DELETE_PRESERVE_SIBLINGS);

        // publish all test2 resources
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        // publish all remaining resources
        cms = getCmsObject();
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Tests to delete a folder structure with (from other user) locked resources inside.<p>
     *
     * @throws Exception if the test fails
     */
    public void testDeleteFolderWithLockedResources() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to delete a folder structure with (from other user) locked resources inside");

        String folder = "/mytestfolder3";
        String file = "/index.html";

        // create folder
        cms.createResource(folder, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.unlockResource(folder);

        // switch user
        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        // copy resource
        cms.copyResource(file, folder + file, CmsResource.COPY_AS_SIBLING);
        assertLock(cms, file, CmsLockType.EXCLUSIVE);
        assertLock(cms, folder + file, CmsLockType.SHARED_EXCLUSIVE);
        cms.changeLock(folder + file);
        assertLock(cms, file, CmsLockType.SHARED_EXCLUSIVE);
        assertLock(cms, folder + file, CmsLockType.EXCLUSIVE);

        // switch back
        CmsUser user = cms.getRequestContext().getCurrentUser();
        cms = getCmsObject();

        assertLock(cms, file, CmsLockType.SHARED_EXCLUSIVE, user);
        assertLock(cms, folder + file, CmsLockType.EXCLUSIVE, user);

        // delete the folder
        cms.lockResource(folder);
        assertLock(cms, folder + file, CmsLockType.INHERITED);
        assertLock(cms, file, CmsLockType.UNLOCKED);

        cms.deleteResource(folder, CmsResource.DELETE_PRESERVE_SIBLINGS);
    }

    /**
     * Tests to delete a folder structure with (from other user) locked siblings inside.<p>
     *
     * @throws Exception if the test fails
     */
    public void testDeleteFolderWithLockedSiblings() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to delete a folder structure with (from other user) locked siblings inside");

        String folder = "/mytestfolder2";
        String file = "/index.html";

        // create folder
        cms.createResource(folder, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.unlockResource(folder);

        // switch user
        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        // copy resource
        cms.copyResource(file, folder + file, CmsResource.COPY_AS_SIBLING);
        cms.unlockResource(folder + file);

        // lock the sibling
        cms.lockResource(file);

        // switch back
        CmsUser user = cms.getRequestContext().getCurrentUser();
        cms = getCmsObject();

        int sibCount = cms.readResource(file).getSiblingCount();
        assertLock(cms, folder + file, CmsLockType.SHARED_EXCLUSIVE, user);

        // delete the folder
        cms.lockResource(folder);
        assertLock(cms, folder + file, CmsLockType.SHARED_INHERITED, user);
        cms.deleteResource(folder, CmsResource.DELETE_PRESERVE_SIBLINGS);

        assertSiblingCount(cms, file, sibCount - 1);
    }

    /**
     * Tests to delete a folder structure with invisible resources inside.<p>
     *
     * @throws Exception if the test fails
     */
    public void testDeleteFolderWithUnvisibleResources() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to delete a folder structure with invisible resources inside");

        // Creating paths
        String folder = "/mytestfolder/";
        String file = "index.html";

        // create structure
        cms.createResource(folder, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.createResource(folder + file, CmsResourceTypePlain.getStaticTypeId());
        // change permissions
        cms.chacc(folder + file, I_CmsPrincipal.PRINCIPAL_USER, "test2", "-r+v+i");
        // unlock resources
        cms.unlockResource(folder);
        // publish
        OpenCms.getPublishManager().publishResource(cms, folder);
        OpenCms.getPublishManager().waitWhileRunning();

        // lock the whole folder as test2
        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        cms.lockResource(folder);

        // delete the folder
        cms.deleteResource(folder, CmsResource.DELETE_PRESERVE_SIBLINGS);

        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        assertEquals(cms.readResource(folder + file, CmsResourceFilter.ALL).getState(), CmsResource.STATE_DELETED);
    }

    /**
     * Tests to delete a folder with no write permission on a subresource.<p>
     *
     * @throws Exception if the test fails
     */
    public void testDeleteWithoutWritePermissions() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to delete a folder with no write permission on a subresource");

        // Creating paths
        String folder = "/folder1/";
        String test = "subfolder11/";

        List list = cms.readResources(folder, CmsResourceFilter.ALL, true);
        int files = list.size();

        // remove read permission for test2
        cms.lockResource(folder);
        cms.chacc(folder, I_CmsPrincipal.PRINCIPAL_USER, "test2", "+r+w+v+i");
        cms.chacc(folder + test, I_CmsPrincipal.PRINCIPAL_USER, "test2", "+r-w+v+i");
        cms.unlockResource(folder);

        // login as test2
        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        cms.lockResource(folder);
        // try delete the folder
        try {
            cms.deleteResource(folder, CmsResource.DELETE_PRESERVE_SIBLINGS);
            fail("to delete a resource with no write permission on a subresource should fail");
        } catch (CmsPermissionViolationException e) {
            // ok
        }

        // login as Admin for testing
        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        assertEquals(
            "there missing files after deletion try",
            files,
            cms.readResources(folder, CmsResourceFilter.ALL, true).size());
    }

    /**
     * Tests user group deletion.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testGroupDeletion() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing user group deletion");

        String groupname = "deleteGroup";

        List expected = OpenCms.getOrgUnitManager().getGroups(cms, "", true);

        // create group
        cms.createGroup(groupname, "deleteMe", I_CmsPrincipal.FLAG_ENABLED, "Users");

        // now delete the group again
        cms.deleteGroup(groupname);

        List actual = OpenCms.getOrgUnitManager().getGroups(cms, "", true);

        assertEquals(expected, actual);
    }

    /**
     * Tests user deletion.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUserDeletion() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing user deletion");

        cms.createGroup("Testgroup", "A test group", 0, null);
        CmsGroup testGroup = cms.readGroup("Testgroup");
        cms.createUser("testuser1", "test1", "A test user 1", null);
        cms.addUserToGroup("testuser1", "Testgroup");
        OpenCms.getRoleManager().addUserToRole(cms, CmsRole.ELEMENT_AUTHOR, "testuser1");
        CmsUser testUser1 = cms.readUser("testuser1");

        cms.lockResource("/");
        cms.chacc("/", I_CmsPrincipal.PRINCIPAL_USER, testUser1.getName(), "+r+w+v+i+c");
        cms.unlockResource("/");
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.loginUser(testUser1.getName(), "test1");
        CmsProject offline = cms.readProject("Offline");
        cms.getRequestContext().setCurrentProject(offline);

        String resName = "/myfile.txt";
        cms.createResource(resName, CmsResourceTypePlain.getStaticTypeId(), "my content".getBytes(), null);
        cms.lockResource(resName);
        cms.chacc(resName, I_CmsPrincipal.PRINCIPAL_USER, testUser1.getName(), "+r+w+v+i");
        cms.chacc(resName, I_CmsPrincipal.PRINCIPAL_GROUP, testGroup.getName(), "+r+v+i");
        cms.unlockResource(resName);

        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(offline);
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        String resName2 = "/folder1/subfolder11/subsubfolder111/text.txt";
        cms.lockResource(resName2);
        cms.chacc(resName2, I_CmsPrincipal.PRINCIPAL_USER, testUser1.getName(), "+r+w+v+i");
        cms.chacc(resName2, I_CmsPrincipal.PRINCIPAL_GROUP, testGroup.getName(), "+r+v+i");
        cms.unlockResource(resName2);
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.createUser("testuser2", "test2", "A test user 2", null);
        cms.addUserToGroup("testuser2", "Testgroup");
        CmsUser testUser2 = cms.readUser("testuser2");

        CmsResourceState state = cms.readResource(resName).getState();
        cms.deleteUser(testUser1.getId(), testUser2.getId());

        // check attributes
        CmsResource res = cms.readResource(resName);
        assertEquals(res.getUserCreated(), testUser2.getId());
        assertEquals(res.getUserLastModified(), testUser2.getId());
        assertEquals(res.getState(), state);

        // check ace for first resource
        boolean found = false;
        Iterator it = cms.getAccessControlEntries(resName, false).iterator();
        while (it.hasNext()) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry)it.next();
            if (ace.getPrincipal().equals(testUser2.getId())) {
                CmsAccessControlEntry newAce = new CmsAccessControlEntry(
                    cms.readResource(resName).getResourceId(),
                    testUser2.getId(),
                    "+r+w+v+i");
                newAce.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_USER);
                assertTrue(newAce.equals(ace));
                found = true;
            }
        }
        assertTrue(found);

        // check ace for second resource
        found = false;
        it = cms.getAccessControlEntries(resName2, false).iterator();
        while (it.hasNext()) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry)it.next();
            if (ace.getPrincipal().equals(testUser2.getId())) {
                CmsAccessControlEntry newAce = new CmsAccessControlEntry(
                    cms.readResource(resName2).getResourceId(),
                    testUser2.getId(),
                    "+r+w+v+i");
                newAce.setFlags(CmsAccessControlEntry.ACCESS_FLAGS_USER);
                assertTrue(newAce.equals(ace));
                found = true;
            }
        }
        assertTrue(found);

        // restore the previous state
        cms.lockResource(resName);
        cms.deleteResource(resName, CmsResource.DELETE_REMOVE_SIBLINGS);
        cms.unlockResource(resName);
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
        cms.deleteUser(testUser2.getId());
        cms.deleteGroup(testGroup.getName());
    }
}

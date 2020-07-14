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
 * For further information about Alkacon Software, please see the
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

import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import junit.framework.Test;

/**
 * Test cases for shallow locks.
 */
public class TestShallowLocks extends OpenCmsTestCase {

    /**
     * Creates a new test instance.
     *
     * @param name the test name
     */
    public TestShallowLocks(String name) {

        super(name);

    }

    /**
     * Creates the test suite.
     *
     * @return the test suite
     */
    public static Test suite() {

        return generateSetupTestWrapper(TestShallowLocks.class, "simpletest", "/");
    }

    /**
     * Test case.
     *
     * @throws Exception
     */
    public void testGetBlockingResources() throws Exception {

        CmsObject cms = getCmsObject();
        CmsResource file = makeTestFile(cms, "/testGetBlockingResources/folder/file.txt");
        cms.lockResourceShallow(cms.readResource("/testGetBlockingResources"));
        CmsObject otherCms = OpenCms.initCmsObject(cms);
        setupUsers();
        otherCms.loginUser("Beta", "beta");
        otherCms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        List<CmsResource> b1 = otherCms.getBlockingLockedResources("/testGetBlockingResources");
        List<CmsResource> blocking = otherCms.getBlockingLockedResources("/testGetBlockingResources/folder");
        assertEquals(
            "Folder with shallow lock should not appear as a blocking resource for its children",
            0,
            blocking.size());

    }

    /**
     * Test case.
     *
     * @throws Exception
     */
    public void testLockType() throws Exception {

        CmsObject cms = getCmsObject();
        CmsResource tb = makeTestFile(cms, "testBar");
        cms.lockResourceShallow(tb);
        assertEquals(CmsLockType.SHALLOW, cms.getLock(tb).getType());
    }

    /**
     * Test case.
     *
     * @throws Exception
     */
    public void testPublish() throws Exception {

        CmsObject cms = getCmsObject();

        CmsProject tempProject = cms.createTempfileProject();
        cms.getRequestContext().setCurrentProject(tempProject);
        CmsResource tb = makeTestFile(cms, "/testPublish/file.txt");
        cms.lockResourceShallow(cms.readResource("/testPublish"));
        assertLock(cms, "/testPublish", CmsLockType.SHALLOW);
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
        assertLock(cms, "/testPublish", CmsLockType.UNLOCKED);

        tempProject = cms.createTempfileProject();
        cms.getRequestContext().setCurrentProject(tempProject);
        tb = makeTestFile(cms, "/testPublish2/subfolder/file.txt");
        cms.lockResourceShallow(cms.readResource("/testPublish2/subfolder"));
        assertLock(cms, "/testPublish2/subfolder", CmsLockType.SHALLOW);
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
        assertLock(cms, "/testPublish2/subfolder", CmsLockType.UNLOCKED);
    }

    /**
     * Test case.
     *
     * @throws Exception
     */
    public void testShallowLockBasic() throws Exception {

        CmsObject cms = getCmsObject();
        CmsObject otherCms = OpenCms.initCmsObject(cms);
        setupUsers();
        String folderPath = "/testShallowLockBasic/folder";
        String filePath = "/testShallowLockBasic/folder/file.txt";
        otherCms.loginUser("Beta", "beta");
        otherCms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        CmsResource testfile1 = makeTestFile(cms, filePath);
        CmsResource folder = cms.readResource(folderPath);
        CmsResource file = cms.readResource(filePath);
        cms.lockResourceShallow(folder);
        assertLock(cms, cms.getSitePath(file), CmsLockType.UNLOCKED);
        otherCms.lockResourceShallow(file);
        tryChangeTitle(cms, folderPath);
        tryChangeTitle(otherCms, filePath);
    }

    /**
     * Test case.
     *
     * @throws Exception
     */
    public void testShallowLockBasic2() throws Exception {

        CmsObject cms = getCmsObject();
        CmsObject otherCms = OpenCms.initCmsObject(cms);
        setupUsers();
        String folderPath = "/testShallowLockBasic2/folder";
        String filePath = "/testShallowLockBasic2/folder/file.txt";
        otherCms.loginUser("Beta", "beta");
        otherCms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        CmsResource testfile1 = makeTestFile(cms, filePath);
        CmsResource folder = cms.readResource(folderPath);
        CmsResource file = cms.readResource(filePath);
        cms.lockResourceShallow(folder);
        otherCms.lockResource(file);
        tryChangeTitle(cms, folderPath);
        tryChangeTitle(otherCms, filePath);
    }

    /**
     * Test case.
     *
     * @throws Exception
     */
    public void testShallowLockBasic3() throws Exception {

        CmsObject cms = getCmsObject();
        CmsObject otherCms = OpenCms.initCmsObject(cms);
        setupUsers();
        String folderPath = "/testShallowLockBasic3/folder";
        String filePath = "/testShallowLockBasic3/folder/file.txt";
        otherCms.loginUser("Beta", "beta");
        otherCms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        CmsResource testfile1 = makeTestFile(cms, filePath);
        CmsResource folder = cms.readResource(folderPath);
        CmsResource file = cms.readResource(filePath);
        cms.lockResource(folder);
        assertThrows(
            "Should not be able to use a shallow lock in a normally locked folder.",
            () -> otherCms.lockResourceShallow(file));
    }

    /**
     * Test case.
     *
     * @throws Exception
     */
    public void testShallowLockDoesNotAllowDelete() throws Exception {

        CmsObject cms = getCmsObject();
        CmsResource file = makeTestFile(cms, "/testShallowLockDoesNotAllowDelete/file.txt");
        cms.lockResourceShallow(cms.readResource("/testShallowLockDoesNotAllowDelete"));
        assertThrows(
            "Shallow lock should not allow delete operations",
            () -> cms.deleteResource("/testShallowLockDoesNotAllowDelete", CmsResource.DELETE_PRESERVE_SIBLINGS));
    }

    /**
     * Test case.
     *
     * @throws Exception
     */
    public void testShallowLockDoesNotAllowMove() throws Exception {

        CmsObject cms = getCmsObject();
        CmsResource file = makeTestFile(cms, "/testShallowLockDoesNotAllowMove/file.txt");
        cms.lockResourceShallow(cms.readResource("/testShallowLockDoesNotAllowMove"));
        assertThrows(
            "Shallow lock should not allow move operations",
            () -> cms.moveResource("/testShallowLockDoesNotAllowMove", "/testShallowLockDoesNotAllowMove2"));
    }

    /**
     * Test case.
     *
     * @throws Exception
     */
    public void testShallowLockOnFolderDoesNotOverrideChildLock() throws Exception {

        CmsObject cms = getCmsObject();
        CmsObject otherCms = OpenCms.initCmsObject(cms);
        setupUsers();
        String folderPath = "/testShallowLockOnFolderDoesNotOverrideChildLock/folder";
        String filePath = "/testShallowLockOnFolderDoesNotOverrideChildLock/folder/file.txt";
        otherCms.loginUser("Beta", "beta");
        otherCms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        CmsResource testfile1 = makeTestFile(cms, filePath);
        CmsResource folder = cms.readResource(folderPath);
        CmsResource file = cms.readResource(filePath);
        otherCms.lockResource(file);
        cms.lockResourceShallow(folder);
        CmsLock childLock = cms.getLock(file);
        assertEquals(CmsLockType.EXCLUSIVE, childLock.getType());
        tryChangeTitle(otherCms, cms.getSitePath(file));
    }

    /**
     * Test case.
     *
     * @throws Exception
     */
    public void testUnlock() throws Exception {

        CmsObject cms = getCmsObject();
        makeTestFile(cms, "/testUnlock/test.txt");
        makeTestFile(cms, "/testUnlock/test2.txt");
        CmsResource folder = cms.readResource("/testUnlock");
        cms.lockResource("/testUnlock/test.txt");
        cms.lockResourceShallow(folder);
        cms.lockResource("/testUnlock/test2.txt");
        cms.unlockResource(folder);
        assertLock(cms, "/testUnlock/test.txt", CmsLockType.EXCLUSIVE);
        assertLock(cms, "/testUnlock/test2.txt", CmsLockType.EXCLUSIVE);
    }

    /**
     * Test case.
     *
     * @throws Exception
     */
    public void testUpgradeLockFails() throws Exception {

        CmsObject cms = getCmsObject();
        // CmsResource testFolder = ensureTestFolder(cms);
        CmsResource tb = makeTestFile(cms, "testUpgradeLock");
        cms.lockResourceShallow(tb);
        assertThrows("Trying to normally lock a shallow-locked folder should fail.", () -> cms.lockResource(tb));
    }

    /**
     * Test case.
     *
     * @throws Exception
     */
    public void testWriteProps() throws Exception {

        CmsObject cms = getCmsObject();
        CmsResource tb = makeTestFile(cms, "testWriteProps");
        cms.lockResourceShallow(tb);
        cms.writePropertyObjects(
            tb,
            Arrays.asList(new CmsProperty(CmsPropertyDefinition.PROPERTY_DESCRIPTION, "Desc 1", null)));
    }

    /**
     * Helper for creating a test file and all its parents folders.
     *
     * All created resources are unlocked.
     *
     * @param cms the CMS context to use
     * @param path the path at which to create the test file
     * @return the newly created test file
     *
     * @throws CmsException if something goes wrong
     */
    CmsResource makeTestFile(CmsObject cms, String path) throws CmsException {

        String currentPath = path;
        List<String> ancestors = new ArrayList<>();
        while (currentPath != null) {
            ancestors.add(currentPath);
            currentPath = CmsResource.getParentFolder(currentPath);
        }
        CmsResource result = null;
        Collections.sort(ancestors);
        for (String ancestor : ancestors) {
            if (!cms.existsResource(ancestor)) {
                int type = 0;
                if (ancestor.equals(path)) {
                    type = 1;
                }
                CmsResource created = cms.createResource(ancestor, type);
                if (ancestor.equals(path)) {
                    result = created;
                }
                cms.unlockResource(created);
            }
        }
        return result;
    }

    /**
     * Sets up the necessary user accounts.
     *
     * @throws Exception if something goes wrong
     */
    private void setupUsers() throws Exception {

        CmsObject cms = getCmsObject();
        try {
            CmsUser user = cms.readUser("Beta");
        } catch (Exception e) {
            cms.createUser("Beta", "beta", "desc", new HashMap<>());
            cms.addUserToGroup("Beta", "Administrators");
        }
    }

    /**
     * Tries to set the Title property to a new value on the given resources, and then verifies that the value has actually been written.
     *
     * @param cms the CMS context to use
     * @param path the path for which to modify the property
     * @throws Exception if setting the property fails
     */
    private void tryChangeTitle(CmsObject cms, String path) throws Exception {

        String s = "EXPECTED-TITLE-" + System.currentTimeMillis();
        cms.writePropertyObjects(cms.readResource(path), Arrays.asList(new CmsProperty("Title", s, null)));
        cms.readPropertyObject(path, "Title", false);
        assertEquals(s, cms.readPropertyObject(path, "Title", false).getStructureValue());

    }

}

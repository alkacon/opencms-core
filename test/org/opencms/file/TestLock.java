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
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.lock.CmsLockException;
import org.opencms.lock.CmsLockFilter;
import org.opencms.lock.CmsLockType;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsPermissionViolationException;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestResourceFilter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for lock operation.<p>
 */
public class TestLock extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestLock(String arg0) {

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
        suite.setName(TestLock.class.getName());

        suite.addTest(new TestLock("testLockFilter"));
        suite.addTest(new TestLock("testLockWithDeletedNewFiles"));
        suite.addTest(new TestLock("testLockOtherUser"));
        suite.addTest(new TestLock("testLockForFile"));
        suite.addTest(new TestLock("testLockForFolder"));
        suite.addTest(new TestLock("testLockForFolderPrelockedShared"));
        suite.addTest(new TestLock("testLockForFolderPrelockedExclusive"));
        suite.addTest(new TestLock("testLockSteal"));
        suite.addTest(new TestLock("testLockRequired"));
        suite.addTest(new TestLock("testLockInherit"));
        suite.addTest(new TestLock("testLockForSiblings"));
        suite.addTest(new TestLock("testLockForBaseOperations"));
        suite.addTest(new TestLock("testCopyToLockedFolder"));
        suite.addTest(new TestLock("testCreationInLockedFolder"));
        suite.addTest(new TestLock("testTempFileCreationInLockedFolder"));

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
     * Tests copying a file to a folder locked by another user.<p>
     *
     * @throws Exception if the test fails
     */
    public void testCopyToLockedFolder() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing copying a file to a folder locked by another user");

        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        String source = "/index.html";
        String folder = "/folder2";
        String destination = "/copytest.html";

        cms.lockResource(folder);

        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        try {
            cms.copyResource(source, folder + destination);
            fail("it is not allowed to create a resource in a folder locked by other user");
        } catch (CmsLockException e) {
            // ok, ignore
        }
    }

    /**
     * Tests creating a new file in a folder locked by another user.<p>
     *
     * @throws Exception if the test fails
     */
    public void testCreationInLockedFolder() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing creating a new file in a folder locked by another user");

        String fileName = "/folder2/creationtest.html";

        // NOTE: folder still locked by test1 from previous test case

        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        try {
            cms.createResource(fileName, CmsResourceTypePlain.getStaticTypeId());
            fail("it is not allowed to create a resource in a folder locked by other user");
        } catch (CmsLockException e) {
            // ok, ignore
        }
    }

    /**
     * Test the lock filter. see bug #1460.<p>
     *
     * @throws Throwable is something goes wrong
     */
    public void testLockFilter() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing lock filter");

        String fileName = "testLockFilter";
        String folderName = "testLockFilterFolder";

        // create a file and a folder where the file name is a prefix of the folder name
        cms.createResource(fileName, CmsResourceTypePlain.getStaticTypeId());
        cms.createResource(folderName, CmsResourceTypeFolder.RESOURCE_TYPE_ID);

        // publish only the folder, so the file remains locked
        OpenCms.getPublishManager().publishResource(cms, folderName);
        OpenCms.getPublishManager().waitWhileRunning();

        // check for lock in or on the folder
        List lockedRes = cms.getLockedResources(folderName, CmsLockFilter.FILTER_ALL);
        // should be empty
        assertTrue(lockedRes.isEmpty());
    }

    /**
     * Tests lock status of a resource for basic operations.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testLockForBaseOperations() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing lock state for basic operations");

        String source = "/types/text.txt";
        String destination1 = "/types/text_new1.txt";
        String destination2 = "/types/text_new2.txt";
        storeResources(cms, source);

        // copy source
        cms.copyResource(source, destination1, CmsResource.COPY_AS_NEW);

        // since source was not locked, destination must be locked exclusive
        // and source must still be unlocked
        assertLock(cms, source, CmsLockType.UNLOCKED);
        assertLock(cms, destination1, CmsLockType.EXCLUSIVE);

        // copy source again
        cms.lockResource(source);
        cms.copyResource(source, destination2, CmsResource.COPY_AS_NEW);

        // both source and destination must be exlusive locked
        assertLock(cms, source, CmsLockType.EXCLUSIVE);
        assertLock(cms, destination2, CmsLockType.EXCLUSIVE);

        // now some move tests
        source = "/types/jsp.jsp";
        destination1 = "/types/jsp_new1.html";

        // lock resource
        cms.lockResource(source);
        cms.moveResource(source, destination1);

        // since source was locked, destination must be shared exclusive
        assertLock(cms, destination1, CmsLockType.EXCLUSIVE);
    }

    /**
     * Tests lock status of a file and its siblings.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testLockForFile() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing locking of files");

        String source = "/folder1/subfolder11/page1.html";
        String sibling1 = "/folder1/subfolder12/page1.html";
        String sibling2 = "/folder2/subfolder22/page1.html";
        storeResources(cms, source);

        // lock source
        cms.lockResource(source);

        // the source file must have an exclusive lock
        // all siblings must have shared locks
        assertLock(cms, source, CmsLockType.EXCLUSIVE);
        assertLock(cms, sibling1, CmsLockType.SHARED_EXCLUSIVE);
        assertLock(cms, sibling2, CmsLockType.SHARED_EXCLUSIVE);

        // now unlock the source
        cms.unlockResource(source);

        // the source file and all sibling are unlocked now
        assertLock(cms, source, CmsLockType.UNLOCKED);
        assertLock(cms, sibling1, CmsLockType.UNLOCKED);
        assertLock(cms, sibling2, CmsLockType.UNLOCKED);
    }

    /**
     * Tests lock status of a folder and its siblings.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testLockForFolder() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing locking of folders");

        String folder = "/folder1/subfolder12/";
        String sibling1 = "/folder1/subfolder12/page1.html";
        String sibling2 = "/folder2/subfolder22/page1.html";
        storeResources(cms, folder);

        // lock folder
        cms.lockResource(folder);

        // the source folder must have an exclusive lock
        assertLock(cms, folder, CmsLockType.EXCLUSIVE);

        // all resources in the folder must have an inherited lock
        List resources = cms.getResourcesInFolder(folder, CmsResourceFilter.DEFAULT);
        Iterator i = resources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            assertLock(cms, cms.getSitePath(res), CmsLockType.INHERITED);
        }

        // all siblings inside the folder must have an inherited lock
        assertLock(cms, sibling1, CmsLockType.INHERITED);
        // all siblings outside the folder must not locked
        assertLock(cms, sibling2, CmsLockType.UNLOCKED);

        // now unlock the folder
        cms.unlockResource(folder);

        // the source folder must be unlocked
        assertLock(cms, folder, CmsLockType.UNLOCKED);

        // all resources in the folder must be unlocked
        resources = cms.getResourcesInFolder(folder, CmsResourceFilter.DEFAULT);
        i = resources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            assertLock(cms, cms.getSitePath(res), CmsLockType.UNLOCKED);
        }

        // all siblings outside of the folder must not locked
        assertLock(cms, sibling1, CmsLockType.UNLOCKED);
        assertLock(cms, sibling2, CmsLockType.UNLOCKED);

    }

    /**
     * Tests lock status of a folder and its siblings.<p>
     *
     * In this test, the folder has some pre-locked siblings with exclusive locks in it
     *
     * @throws Throwable if something goes wrong
     */
    public void testLockForFolderPrelockedExclusive() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing locking of folders with exclusive prelocks");

        String folder = "/folder1/subfolder12/";
        String source = "/folder1/subfolder12/page1.html";
        String sibling1 = "/folder1/subfolder11/page1.html";
        String sibling2 = "/folder2/subfolder22/page1.html";
        storeResources(cms, folder);

        // lock source
        cms.lockResource(source);

        // the source file must have an exclusive lock
        // all siblings must have shared exclusive locks
        assertLock(cms, source, CmsLockType.EXCLUSIVE);
        assertLock(cms, sibling1, CmsLockType.SHARED_EXCLUSIVE);
        assertLock(cms, sibling2, CmsLockType.SHARED_EXCLUSIVE);

        // lock folder
        cms.lockResource(folder);

        // all resources in the folder must have an inherited lock
        List resources = cms.getResourcesInFolder(folder, CmsResourceFilter.DEFAULT);
        Iterator i = resources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            assertLock(cms, cms.getSitePath(res), CmsLockType.INHERITED);
        }

        // The siblings outside the folder are unlocked
        assertLock(cms, sibling1, CmsLockType.UNLOCKED);
        assertLock(cms, sibling2, CmsLockType.UNLOCKED);

        // now unlock the folder
        cms.unlockResource(folder);

        // the source folder must be unlocked
        assertLock(cms, folder, CmsLockType.UNLOCKED);

        // all resources in the folder must be unlocked
        resources = cms.getResourcesInFolder(folder, CmsResourceFilter.DEFAULT);
        i = resources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            assertLock(cms, cms.getSitePath(res), CmsLockType.UNLOCKED);
        }

        // The siblings outside the folder kept their previous lock state
        assertLock(cms, sibling1, CmsLockType.UNLOCKED);
        assertLock(cms, sibling2, CmsLockType.UNLOCKED);

    }

    /**
     * Tests lock status of a folder and its siblings.<p>
     *
     * In this test, the folder has some prelocked siblings with shared locks in it
     *
     * @throws Throwable if something goes wrong
     */
    public void testLockForFolderPrelockedShared() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing locking of folders with shared prelocks");

        String folder = "/folder1/subfolder12/";
        String source = "/folder1/subfolder11/page1.html";
        String sibling1 = "/folder1/subfolder12/page1.html";
        String sibling2 = "/folder2/subfolder22/page1.html";
        storeResources(cms, folder);

        // lock source
        cms.lockResource(source);

        // the source file must have an exclusive lock
        // all siblings must have shared exclusive locks
        assertLock(cms, source, CmsLockType.EXCLUSIVE);
        assertLock(cms, sibling1, CmsLockType.SHARED_EXCLUSIVE);
        assertLock(cms, sibling2, CmsLockType.SHARED_EXCLUSIVE);

        // lock folder
        cms.lockResource(folder);

        // all resources in the folder must have an inherited or shared inherited lock
        List resources = cms.getResourcesInFolder(folder, CmsResourceFilter.DEFAULT);
        Iterator i = resources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            // the sibling to the resource "source" must have an shared inherited lock
            // all other resources must have a inherited lock
            if (cms.getSitePath(res).equals(sibling1)) {
                assertLock(cms, cms.getSitePath(res), CmsLockType.SHARED_INHERITED);
            } else {
                assertLock(cms, cms.getSitePath(res), CmsLockType.INHERITED);
            }
        }

        // The siblings outside the folder kept their previous lock state
        assertLock(cms, source, CmsLockType.EXCLUSIVE);
        assertLock(cms, sibling2, CmsLockType.SHARED_EXCLUSIVE);

        // now unlock the folder
        cms.unlockResource(folder);

        // the source folder must be unlocked
        assertLock(cms, folder, CmsLockType.UNLOCKED);

        // all resources in the folder must be unlocked, except those siblings that had a
        // shared inherited lock, they must get a shared exclusive lock
        resources = cms.getResourcesInFolder(folder, CmsResourceFilter.DEFAULT);
        i = resources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            if (cms.getSitePath(res).equals(sibling1)) {
                assertLock(cms, cms.getSitePath(res), CmsLockType.SHARED_EXCLUSIVE);
            } else {
                assertLock(cms, cms.getSitePath(res), CmsLockType.UNLOCKED);
            }
        }

        // The siblings outside the folder kept their previous lock state
        assertLock(cms, source, CmsLockType.EXCLUSIVE);
        assertLock(cms, sibling2, CmsLockType.SHARED_EXCLUSIVE);

        // now unlock the source
        cms.unlockResource(source);

        // the source file and all sibling are unlocked now
        assertLock(cms, source, CmsLockType.UNLOCKED);
        assertLock(cms, sibling1, CmsLockType.UNLOCKED);
        assertLock(cms, sibling2, CmsLockType.UNLOCKED);

    }

    /**
     * Tests lock status of a resource during sibling creation.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testLockForSiblings() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing lock state after sibling creation");

        String source = "/folder2/index.html";
        String destination1 = "/folder2/index_sib1.html";
        String destination2 = "/folder2/index_sib2.html";
        storeResources(cms, source);

        // copy source
        cms.copyResource(source, destination1, CmsResource.COPY_AS_SIBLING);

        // since source was not locked, destination must be locked exclusive
        // and source must be locked shared
        assertLock(cms, source, CmsLockType.SHARED_EXCLUSIVE);
        assertLock(cms, destination1, CmsLockType.EXCLUSIVE);

        // copy source again
        cms.copyResource(source, destination2, CmsResource.COPY_AS_SIBLING);

        // since one sibling was already exclusive locked,
        // new sibling must be shared locked
        assertLock(cms, source, CmsLockType.SHARED_EXCLUSIVE);
        assertLock(cms, destination1, CmsLockType.EXCLUSIVE);
        assertLock(cms, destination2, CmsLockType.SHARED_EXCLUSIVE);

        // same stuff but in a different order
        source = "/folder2/page1.html";
        destination1 = "/folder2/page1_sib1.html";
        destination2 = "/folder2/page1_sib2.html";

        // this time source is already locked
        cms.lockResource(source);
        cms.createSibling(source, destination1, null);

        // since source was locked, destination must be shared exclusive
        assertLock(cms, source, CmsLockType.EXCLUSIVE);
        assertLock(cms, destination1, CmsLockType.SHARED_EXCLUSIVE);

        // create another sibling
        cms.createSibling(destination1, destination2, null);
        // since one sibling was already exclusive locked,
        // new sibling must be shared locked
        assertLock(cms, source, CmsLockType.EXCLUSIVE);
        assertLock(cms, destination1, CmsLockType.SHARED_EXCLUSIVE);
        assertLock(cms, destination2, CmsLockType.SHARED_EXCLUSIVE);

        // now create a sibling (from a locked resource) over a (locked) deleted resource
        source = "/folder2/image1.gif";
        String destination = "/folder2/image2.gif";

        cms.lockResource(source);
        cms.lockResource(destination);
        cms.deleteResource(destination, CmsResource.DELETE_PRESERVE_SIBLINGS);

        // both resources should be exclusive locked
        assertLock(cms, source, CmsLockType.EXCLUSIVE);
        assertLock(cms, destination, CmsLockType.EXCLUSIVE);

        // now copy a sibling over the deleted
        cms.copyResource(source, destination, CmsResource.COPY_AS_SIBLING);

        // now the destination should have a shared lock
        assertLock(cms, source, CmsLockType.EXCLUSIVE);
        assertLock(cms, destination, CmsLockType.SHARED_EXCLUSIVE);
    }

    /**
     * Tests an inherited lock in a resource delete scenario.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testLockInherit() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing inherited lock delete scenario");

        String source = "/folder1/index.html";
        String folder = "/folder1/";
        storeResources(cms, source);

        // first delete the resource
        cms.lockResource(source);
        cms.deleteResource(source, CmsResource.DELETE_PRESERVE_SIBLINGS);

        // now lock the folder
        cms.lockResource(folder);

        // make sure the deleted file has an inherited lock
        assertLock(cms, source, CmsLockType.INHERITED);
    }

    /**
     * Tests a lock set by other user.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testLockOtherUser() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing a lock set by other user");

        String source = "/folder1/image1.gif";

        // get the offline project
        CmsProject offlineProject = cms.readProject("Offline");

        // login as user "test1"
        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(offlineProject);

        // now lock the resource
        cms.lockResource(source);

        // make sure the file has an exclusive lock
        assertLock(cms, source, CmsLockType.EXCLUSIVE);

        // login back as admin
        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(offlineProject);

        CmsUser test1 = cms.readUser("test1");

        // make sure the file has still an exclusive lock on the test user
        assertLock(cms, source, CmsLockType.EXCLUSIVE, test1);
    }

    /**
     * Ensures that a lock is required for all write/control operations.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testLockRequired() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing if a lock is required for write/control operations");

        String source = "/index.html";
        storeResources(cms, source);
        long timestamp = System.currentTimeMillis();

        // make sure source is not locked
        assertLock(cms, source, CmsLockType.UNLOCKED);

        CmsFile file = cms.readFile(source);

        boolean needLock;

        needLock = false;
        try {
            cms.setDateLastModified(source, timestamp, false);
        } catch (CmsLockException e) {
            // must throw a security exception because resource is not locked
            needLock = true;
        }
        if (!needLock) {
            fail("Touch operation on resource permitted without a lock on the current user!");
        }

        needLock = false;
        try {
            cms.deleteResource(source, CmsResource.DELETE_PRESERVE_SIBLINGS);
        } catch (CmsLockException e) {
            // must throw a security exception because resource is not locked
            needLock = true;
        }
        if (!needLock) {
            fail("Delete operation on resource permitted without a lock on the current user!");
        }

        needLock = false;
        try {
            cms.writeFile(file);
        } catch (CmsLockException e) {
            // must throw a security exception because resource is not locked
            needLock = true;
        }
        if (!needLock) {
            fail("Write operation on resource permitted without a lock on the current user!");
        }

        needLock = false;
        try {
            cms.moveResource(source, "index_dest.html");
        } catch (CmsLockException e) {
            // must throw a security exception because resource is not locked
            needLock = true;
        }
        if (!needLock) {
            fail("Move operation on resource permitted without a lock on the current user!");
        }

        needLock = false;
        try {
            cms.writePropertyObject(source, new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "New title", null));
        } catch (CmsLockException e) {
            // must throw a security exception because resource is not locked
            needLock = true;
        }
        if (!needLock) {
            fail("Write property operation on resource permitted without a lock on the current user!");
        }

        needLock = false;
        try {
            List properties = new ArrayList();
            properties.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "New title 2", null));
            cms.writePropertyObjects(source, properties);
        } catch (CmsLockException e) {
            // must throw a security exception because resource is not locked
            needLock = true;
        }
        if (!needLock) {
            fail("Write property list operation on resource permitted without a lock on the current user!");
        }

        needLock = false;
        try {
            cms.chflags(source, 1234);
        } catch (CmsLockException e) {
            // must throw a security exception because resource is not locked
            needLock = true;
        }
        if (!needLock) {
            fail("Change flags operation on resource permitted without a lock on the current user!");
        }

        needLock = false;
        try {
            cms.chtype(source, CmsResourceTypePlain.getStaticTypeId());
        } catch (CmsLockException e) {
            // must throw a security exception because resource is not locked
            needLock = true;
        }
        if (!needLock) {
            fail("Change type operation on resource permitted without a lock on the current user!");
        }

        needLock = false;
        try {
            cms.replaceResource(source, CmsResourceTypePlain.getStaticTypeId(), "Kaputt".getBytes(), null);
        } catch (CmsLockException e) {
            // must throw a security exception because resource is not locked
            needLock = true;
        }
        if (!needLock) {
            fail("Replace operation on resource permitted without a lock on the current user!");
        }

        needLock = false;
        try {
            CmsPermissionSet permissions = new CmsPermissionSet(
                CmsPermissionSet.PERMISSION_WRITE,
                CmsPermissionSet.PERMISSION_READ);
            cms.chacc(
                source,
                I_CmsPrincipal.PRINCIPAL_GROUP,
                OpenCms.getDefaultUsers().getGroupAdministrators(),
                permissions.getAllowedPermissions(),
                permissions.getDeniedPermissions(),
                CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE);
        } catch (CmsLockException e) {
            // must throw a security exception because resource is not locked
            needLock = true;
        }
        if (!needLock) {
            fail("Change permissions operation on resource permitted without a lock on the current user!");
        }

        needLock = false;
        try {
            cms.undeleteResource(source, true);
        } catch (CmsLockException e) {
            // must throw a security exception because resource is not locked
            needLock = true;
        }
        if (!needLock) {
            fail("Unlock operation on resource permitted without a lock on the current user!");
        }

        // make sure original resource is unchanged
        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_EQUAL);

        // now perform a delete operation with lock
        cms.lockResource(source);
        cms.deleteResource(source, CmsResource.DELETE_PRESERVE_SIBLINGS);

        // now undelete the resource
        cms.lockResource(source);
        cms.undoChanges(source, CmsResource.UNDO_CONTENT_RECURSIVE);
        cms.unlockResource(source);

        // make sure original resource is still unchanged
        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_UNDOCHANGES_ALL);
    }

    /**
     * Tests to steal a lock.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testLockSteal() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing stealing a lock");

        String source = "/folder1/subfolder11/page1.html";
        String sibling1 = "/folder1/subfolder12/page1.html";
        String sibling2 = "/folder2/subfolder22/page1.html";
        storeResources(cms, source);

        // get the offline project
        CmsProject offlineProject = cms.readProject("Offline");

        // login as user "test1"
        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(offlineProject);

        // lock source
        cms.lockResource(source);

        // the source file must have an exclusive lock
        // all siblings must have shared locks
        assertLock(cms, source, CmsLockType.EXCLUSIVE);
        assertLock(cms, sibling1, CmsLockType.SHARED_EXCLUSIVE);
        assertLock(cms, sibling2, CmsLockType.SHARED_EXCLUSIVE);

        // login as user "admin"
        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(offlineProject);

        // steal lock from first sibling
        cms.changeLock(sibling1);

        // the sibling1 file must have an exclusive lock
        // all siblings of it must have shared locks
        assertLock(cms, sibling1, CmsLockType.EXCLUSIVE);
        assertLock(cms, source, CmsLockType.SHARED_EXCLUSIVE);
        assertLock(cms, sibling2, CmsLockType.SHARED_EXCLUSIVE);

        // now revoke write permissions for user "test2"
        cms.chacc(
            source,
            I_CmsPrincipal.PRINCIPAL_USER,
            "test2",
            0,
            CmsPermissionSet.PERMISSION_WRITE,
            CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE + CmsAccessControlEntry.ACCESS_FLAGS_INHERIT);

        // switch to user "test2"
        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(offlineProject);

        Exception error = null;
        try {
            // try to steal lock from the source
            cms.changeLock(source);
        } catch (CmsPermissionViolationException e) {
            error = e;
        }
        assertNotNull(error);
        try {
            // try to steal lock from the first sibling
            cms.changeLock(sibling1);
        } catch (CmsPermissionViolationException e) {
            error = e;
        }
        assertNotNull(error);
        try {
            // try to steal lock from the second sibling
            cms.changeLock(sibling2);
        } catch (CmsPermissionViolationException e) {
            error = e;
        }
        assertNotNull(error);

        // login as user "Admin" again
        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(offlineProject);

        // assert the locks are still there
        assertLock(cms, sibling1, CmsLockType.EXCLUSIVE);
        assertLock(cms, source, CmsLockType.SHARED_EXCLUSIVE);
        assertLock(cms, sibling2, CmsLockType.SHARED_EXCLUSIVE);

        // login as user "test1" again
        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(offlineProject);

        // steal lock from second sibling
        cms.changeLock(sibling2);

        // assert the locks for siblings are there
        assertLock(cms, sibling2, CmsLockType.EXCLUSIVE);
        assertLock(cms, source, CmsLockType.SHARED_EXCLUSIVE);
        assertLock(cms, sibling1, CmsLockType.SHARED_EXCLUSIVE);
    }

    /**
     * Tests lock status after a new file has been deleted in offline project.<p>
     *
     * Issue description:
     * User A creates a new file, but deletes it without ever publishing it.
     * Now user B create a new file with the same name / path.
     * The file was still in the lock manager but for user A, this generated
     * an error for user B.<p>
     *
     * Solution:
     * Remove new files that are deleted from the lock manager.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testLockWithDeletedNewFiles() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing lock status of a deleted new file");

        String source = "/folder1/newfile.html";

        // create a new resource as default test user
        cms.createResource(source, CmsResourceTypePlain.getStaticTypeId());
        // the source file must now have an exclusive lock
        assertLock(cms, source, CmsLockType.EXCLUSIVE);
        // now delete the created resource
        cms.deleteResource(source, CmsResource.DELETE_REMOVE_SIBLINGS);

        // now login as user "test2"
        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        // now create the resource again
        cms.createResource(source, CmsResourceTypePlain.getStaticTypeId());

        // the newly created resource must now be locked to user "test2"
        assertLock(cms, source, CmsLockType.EXCLUSIVE);
    }

    /**
     * Tests creating a new temporary file in a folder locked by another user.<p>
     *
     * @throws Exception if the test fails
     */
    public void testTempFileCreationInLockedFolder() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing creating a new file in a folder locked by another user");

        String fileName = "/folder2/~creationtest.html";

        // NOTE: folder still locked by test1 from previous test case

        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        try {
            cms.createResource(fileName, CmsResourceTypePlain.getStaticTypeId());
            fail("it is not allowed to create a resource in a folder locked by other user");
        } catch (CmsLockException e) {
            // ok, ignore
        }

        // it should be allowed for the root admin
        cms = getCmsObject();
        cms.createResource(fileName, CmsResourceTypePlain.getStaticTypeId());
        assertLock(cms, fileName, CmsLockType.INHERITED, cms.readUser("test1"));
    }
}

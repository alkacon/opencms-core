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

import org.opencms.db.CmsDriverManager;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsVfsUtil;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import junit.framework.Test;

/**
 * Unit test for the "setDateExpired" and "setDateReleased" method of the CmsObject.<p>
 */
public class TestAvailability extends OpenCmsTestCase {

    private static final long MSECS_PER_DAY = 1000 * 60 * 60 * 12;

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestAvailability(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        return generateSetupTestWrapper(TestAvailability.class, "simpletest", "/");

    }

    /**
     * Test to set release date on a resource.<p>
     *
     * The method reads the file, and tests if the file cannot be read with the CmsResourceFilter.DEFAULT.<p>
     *
     * @throws Throwable if something goes wrong
     */

    public void testDateExpired() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing to set expire date");

        String resourceName = "/index.html";

        long yesterday = System.currentTimeMillis() - MSECS_PER_DAY;
        cms.lockResource(resourceName);
        cms.setDateExpired(resourceName, yesterday, false);
        cms.unlockResource(resourceName);

        testOutsideTimeRange(cms, resourceName, CmsResource.DATE_RELEASED_DEFAULT, yesterday);
        cms.lockResource(resourceName);
        cms.undoChanges(resourceName, CmsResource.UNDO_CONTENT);
        cms.unlockResource(resourceName);
    }

    /**
     * Test to set release date on a resource.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testDateReleased() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing to set release date");

        String resourceName = "/index.html";
        long tomorrow = System.currentTimeMillis() + MSECS_PER_DAY;
        cms.lockResource(resourceName);
        cms.setDateReleased(resourceName, tomorrow, false);
        cms.unlockResource(resourceName);

        testOutsideTimeRange(cms, resourceName, tomorrow, CmsResource.DATE_EXPIRED_DEFAULT);
        cms.lockResource(resourceName);
        cms.undoChanges(resourceName, CmsResource.UNDO_CONTENT);
        cms.unlockResource(resourceName);
    }

    /**
     * Test for the 'exclusive access' feature.
     *
     * @throws Exception -
     */
    public void testExclusiveAccess() throws Exception {

        String testName = getName();
        String folder = "/system/" + testName;
        String path = folder + "/" + "file1.txt";
        CmsObject cms = getCmsObject();
        CmsVfsUtil.createFolder(cms, folder);
        cms.lockResourceTemporary(folder);
        CmsGroup goodGroup = cms.createGroup("group_" + testName, "", 0, null);
        CmsUser goodUser = createUserWithGroups("userInGroup_" + testName, "Users", goodGroup.getName());
        CmsUser badUser = createUserWithGroups("userNotInGroup_" + testName, "Users");

        CmsResource resource = cms.createResource(
            path,
            OpenCms.getResourceManager().getResourceType(CmsResourceTypePlain.getStaticTypeName()));
        cms.chacc(folder, I_CmsPrincipal.PRINCIPAL_GROUP, "Users", "+r+w+i");
        cms.chacc(
            resource.getRootPath(),
            I_CmsPrincipal.PRINCIPAL_GROUP,
            CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME,
            "+r+w");
        CmsObject goodCms = switchUser(cms, goodUser);
        CmsObject badCms = switchUser(cms, badUser);

        cms.chacc(resource.getRootPath(), I_CmsPrincipal.PRINCIPAL_GROUP, goodGroup.getName(), "+l");

        // restriction shouldn't take effect until availability is set
        checkResourceAccessible(goodCms, path);
        checkResourceAccessible(badCms, path);

        resource.setDateReleased(System.currentTimeMillis() + MSECS_PER_DAY);
        cms.writeResource(resource);

        checkResourceAccessible(goodCms, path);
        checkResourceRestricted(badCms, path);

        // now check that having only inherited 'responsible' entries don't restrict access
        cms.rmacc(resource.getRootPath(), I_CmsPrincipal.PRINCIPAL_GROUP, goodGroup.getName());
        cms.chacc(folder, I_CmsPrincipal.PRINCIPAL_GROUP, goodGroup.getName(), "+l");
        checkResourceAccessible(goodCms, path);
        checkResourceAccessible(badCms, path);

    }

    /**
     * Test for the 'exclusive access' feature.
     *
     * @throws Exception -
     */
    public void testExclusiveAccessOnlineTimeDependent() throws Exception {

        String testName = getName();
        String folder = "/system/" + testName;
        String path = folder + "/" + "file1.txt";
        CmsObject cms = getCmsObject();
        CmsVfsUtil.createFolder(cms, folder);
        cms.lockResourceTemporary(folder);
        CmsGroup goodGroup = cms.createGroup("group_" + testName, "", 0, null);
        CmsUser badUser = createUserWithGroups("userNotInGroup_" + testName, "Users");
        CmsUser goodUser = createUserWithGroups("userInGroup_" + testName, "Users", goodGroup.getName());
        CmsObject goodCms = switchUser(cms, goodUser);

        CmsResource resource = cms.createResource(
            path,
            OpenCms.getResourceManager().getResourceType(CmsResourceTypePlain.getStaticTypeName()));
        cms.chacc(folder, I_CmsPrincipal.PRINCIPAL_GROUP, "Users", "+r+w+i");
        cms.chacc(
            resource.getRootPath(),
            I_CmsPrincipal.PRINCIPAL_GROUP,
            CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME,
            "+r+w");
        CmsObject badCms = switchUser(cms, badUser);
        badCms.getRequestContext().setCurrentProject(cms.readProject("Online"));
        goodCms.getRequestContext().setCurrentProject(cms.readProject("Online"));

        cms.chacc(resource.getRootPath(), I_CmsPrincipal.PRINCIPAL_GROUP, goodGroup.getName(), "+l");

        resource.setDateReleased(2000);
        resource.setDateExpired(4000);
        cms.writeResource(resource);
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
        setClock(badCms, 0);
        setClock(goodCms, 0);
        checkResourceRestricted(badCms, path);
        checkResourceAccessible(goodCms, path);
        setClock(badCms, 3000);
        setClock(goodCms, 3000);
        checkResourceAccessible(badCms, path, CmsResourceFilter.DEFAULT);
        checkResourceAccessible(goodCms, path);
        setClock(badCms, 5000);
        setClock(goodCms, 5000);
        checkResourceRestricted(badCms, path);
        checkResourceAccessible(goodCms, path);
    }

    /**
     * Test for the 'exclusive access' feature.
     *
     * @throws Exception -
     */
    public void testExclusiveAccessTimeDependent() throws Exception {

        String testName = getName();
        String folder = "/system/" + testName;
        String path = folder + "/" + "file1.txt";
        CmsObject cms = getCmsObject();
        CmsVfsUtil.createFolder(cms, folder);
        cms.lockResourceTemporary(folder);
        CmsGroup goodGroup = cms.createGroup("group_" + testName, "", 0, null);
        CmsUser badUser = createUserWithGroups("userNotInGroup_" + testName, "Users");
        CmsUser goodUser = createUserWithGroups("userInGroup_" + testName, "Users", goodGroup.getName());
        CmsObject goodCms = switchUser(cms, goodUser);

        CmsResource resource = cms.createResource(
            path,
            OpenCms.getResourceManager().getResourceType(CmsResourceTypePlain.getStaticTypeName()));
        cms.chacc(folder, I_CmsPrincipal.PRINCIPAL_GROUP, "Users", "+r+w+i");
        cms.chacc(
            resource.getRootPath(),
            I_CmsPrincipal.PRINCIPAL_GROUP,
            CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME,
            "+r+w");
        CmsObject badCms = switchUser(cms, badUser);

        cms.chacc(resource.getRootPath(), I_CmsPrincipal.PRINCIPAL_GROUP, goodGroup.getName(), "+l");

        resource.setDateReleased(2000);
        resource.setDateExpired(4000);
        cms.writeResource(resource);
        setClock(badCms, 0);
        setClock(goodCms, 0);
        checkResourceRestricted(badCms, path);
        checkResourceAccessible(goodCms, path, CmsResourceFilter.IGNORE_EXPIRATION);
        setClock(badCms, 3000);
        setClock(goodCms, 3000);
        checkResourceAccessible(badCms, path, CmsResourceFilter.IGNORE_EXPIRATION);
        checkResourceAccessible(badCms, path, CmsResourceFilter.DEFAULT);
        checkResourceAccessible(goodCms, path, CmsResourceFilter.DEFAULT);
        setClock(badCms, 5000);
        setClock(goodCms, 5000);
        checkResourceRestricted(badCms, path);
        checkResourceAccessible(goodCms, path, CmsResourceFilter.IGNORE_EXPIRATION);
    }

    /**
     * Test for the 'exclusive access' feature.
     *
     * @throws Exception -
     */
    public void testExclusiveAccessTimeDependentMultipleResponsibleGroups() throws Exception {

        String testName = getName();
        String folder = "/system/" + testName;
        String path = folder + "/" + "file1.txt";
        CmsObject cms = getCmsObject();
        CmsVfsUtil.createFolder(cms, folder);
        cms.lockResourceTemporary(folder);
        CmsGroup goodGroup = cms.createGroup("group_" + testName, "", 0, null);
        CmsGroup goodGroup2 = cms.createGroup("group2_" + testName, "", 0, null);
        CmsUser badUser = createUserWithGroups("userNotInGroup_" + testName, "Users");
        CmsUser goodUser = createUserWithGroups("userInGroup_" + testName, "Users", goodGroup.getName());
        CmsObject goodCms = switchUser(cms, goodUser);

        CmsResource resource = cms.createResource(
            path,
            OpenCms.getResourceManager().getResourceType(CmsResourceTypePlain.getStaticTypeName()));
        cms.chacc(folder, I_CmsPrincipal.PRINCIPAL_GROUP, "Users", "+r+w+i");
        cms.chacc(
            resource.getRootPath(),
            I_CmsPrincipal.PRINCIPAL_GROUP,
            CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME,
            "+r+w");
        CmsObject badCms = switchUser(cms, badUser);

        cms.chacc(resource.getRootPath(), I_CmsPrincipal.PRINCIPAL_GROUP, goodGroup.getName(), "+l");
        cms.chacc(resource.getRootPath(), I_CmsPrincipal.PRINCIPAL_GROUP, goodGroup2.getName(), "+l");

        resource.setDateReleased(2000);
        resource.setDateExpired(4000);
        cms.writeResource(resource);
        setClock(badCms, 0);
        setClock(goodCms, 0);
        checkResourceRestricted(badCms, path);
        checkResourceAccessible(goodCms, path, CmsResourceFilter.IGNORE_EXPIRATION);
        setClock(badCms, 3000);
        setClock(goodCms, 3000);
        checkResourceAccessible(badCms, path, CmsResourceFilter.DEFAULT);
        checkResourceAccessible(goodCms, path, CmsResourceFilter.DEFAULT);
        setClock(badCms, 5000);
        setClock(goodCms, 5000);
        checkResourceRestricted(badCms, path);
        checkResourceAccessible(goodCms, path, CmsResourceFilter.IGNORE_EXPIRATION);
    }

    /**
     * Test to set expired date on a folder.<p>
     *
     * The method reads the file, and tests if the file cannot be read with the CmsResourceFilter.DEFAULT.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testFolderDateExpired() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing expire date in a folder");

        String folderName = "/folder1/";
        String resName = folderName + "index.html";
        long yesterday = System.currentTimeMillis() - MSECS_PER_DAY;
        cms.lockResource(folderName);
        cms.setDateExpired(folderName, yesterday, true);
        cms.unlockResource(folderName);

        testOutsideTimeRange(cms, folderName, CmsResource.DATE_RELEASED_DEFAULT, yesterday);
        testOutsideTimeRange(cms, resName, CmsResource.DATE_RELEASED_DEFAULT, yesterday);
        cms.lockResource(folderName);
        cms.undoChanges(folderName, CmsResource.UNDO_CONTENT_RECURSIVE);
        cms.unlockResource(folderName);
    }

    /**
     * Test to set release date on a folder.<p>
     *
     * The method reads the file, and tests if the file cannot be read with the CmsResourceFilter.DEFAULT.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testFolderDateReleased() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing release date in a folder");

        String folderName = "/folder1/";
        String resName = folderName + "index.html";
        long tomorrow = System.currentTimeMillis() + MSECS_PER_DAY;
        cms.lockResource(folderName);
        cms.setDateReleased(folderName, tomorrow, true);
        cms.unlockResource(folderName);

        testOutsideTimeRange(cms, folderName, tomorrow, CmsResource.DATE_EXPIRED_DEFAULT);
        testOutsideTimeRange(cms, resName, tomorrow, CmsResource.DATE_EXPIRED_DEFAULT);
        cms.lockResource(folderName);
        cms.undoChanges(folderName, CmsResource.UNDO_CONTENT_RECURSIVE);
        cms.unlockResource(folderName);
    }

    public void testSetRestricted() throws Exception {

        String testName = getName();
        String folder = "/system/" + testName;
        String path = folder + "/" + "file1.txt";
        CmsObject cms = getCmsObject();
        CmsVfsUtil.createFolder(cms, folder);
        cms.lockResourceTemporary(folder);
        CmsGroup goodGroup = cms.createGroup("group_" + testName, "", 0, null);
        CmsUser badUser = createUserWithGroups("userNotInGroup_" + testName, "Users");
        CmsUser goodUser = createUserWithGroups("userInGroup_" + testName, "Users", goodGroup.getName());
        CmsObject goodCms = switchUser(cms, goodUser);

        CmsResource resource = cms.createResource(
            path,
            OpenCms.getResourceManager().getResourceType(CmsResourceTypePlain.getStaticTypeName()));
        cms.chacc(folder, I_CmsPrincipal.PRINCIPAL_GROUP, "Users", "+r+w+i");
        cms.chacc(
            resource.getRootPath(),
            I_CmsPrincipal.PRINCIPAL_GROUP,
            CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME,
            "+r+w");
        CmsObject badCms = switchUser(cms, badUser);
        cms.unlockResource(folder);
        badCms.lockResource(resource);
        try {

            badCms.setRestricted(resource, goodGroup.getName(), true);
            fail("Should not be allowed to call setRestricted for this group.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            badCms.unlockResource(resource);
        }
        goodCms.lockResourceTemporary(path);
        goodCms.setRestricted(resource, goodGroup.getName(), true);

        assertTrue(
            "Responsible entry not found",
            goodCms.getAccessControlEntries(path).stream().anyMatch(
                ace -> ace.getPrincipal().equals(goodGroup.getId()) && ace.isResponsible()));

        goodCms.setRestricted(resource, goodGroup.getName(), false);

        assertFalse(
            "Responsible entry found when it shouldn't exist",
            goodCms.getAccessControlEntries(path).stream().anyMatch(
                ace -> ace.getPrincipal().equals(goodGroup.getId()) && ace.isResponsible()));
        assertFalse(
            "Entry for the  good group shouldn't exist",
            goodCms.getAccessControlEntries(path).stream().anyMatch(
                ace -> ace.getPrincipal().equals(goodGroup.getId())));
    }

    /**
     * Test to set expired date on a subfolder.<p>
     *
     * The method reads the file, and tests if the file cannot be read with the CmsResourceFilter.DEFAULT.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testSubFolderDateExpired() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing expire date in a folder");

        String folderName = "/folder1";
        String folderName2 = "/subfolder11";
        String resName = "/index.html";
        long yesterday = System.currentTimeMillis() - MSECS_PER_DAY;
        cms.lockResource(folderName);
        cms.setDateExpired(folderName, yesterday, true);
        cms.unlockResource(folderName);

        testOutsideTimeRange(cms, folderName, CmsResource.DATE_RELEASED_DEFAULT, yesterday);
        testOutsideTimeRange(cms, folderName + resName, CmsResource.DATE_RELEASED_DEFAULT, yesterday);
        testOutsideTimeRange(cms, folderName + folderName2 + resName, CmsResource.DATE_RELEASED_DEFAULT, yesterday);
        cms.lockResource(folderName);
        cms.undoChanges(folderName, CmsResource.UNDO_CONTENT_RECURSIVE);
        cms.unlockResource(folderName);
    }

    /**
     * Test to set release date on a subfolder.<p>
     *
     * The method reads the file, and tests if the file cannot be read with the CmsResourceFilter.DEFAULT.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testSubFolderDateReleased() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing release date in a subfolder");

        String folderName = "/folder1";
        String folderName2 = "/subfolder11";
        String resName = "/index.html";
        long tomorrow = System.currentTimeMillis() + MSECS_PER_DAY;
        cms.lockResource(folderName);
        cms.setDateReleased(folderName, tomorrow, true);
        cms.unlockResource(folderName);

        testOutsideTimeRange(cms, folderName, tomorrow, CmsResource.DATE_EXPIRED_DEFAULT);
        testOutsideTimeRange(cms, folderName + resName, tomorrow, CmsResource.DATE_EXPIRED_DEFAULT);
        testOutsideTimeRange(cms, folderName + folderName2 + resName, tomorrow, CmsResource.DATE_EXPIRED_DEFAULT);
        cms.lockResource(folderName);
        cms.undoChanges(folderName, CmsResource.UNDO_CONTENT_RECURSIVE);
        cms.unlockResource(folderName);
    }

    /**
     * Checks that a resource is accessible, both directly and as part of a directory listing .
     *
     * @param cms a CMS context
     * @param path a path
     * @throws Exception if something goes wrong
     */
    private void checkResourceAccessible(CmsObject cms, String path) throws Exception {

        checkResourceAccessible(cms, path, CmsResourceFilter.IGNORE_EXPIRATION);
    }

    /**
     * Checks that a resource is accessible, both directly and as part of a directory listing .
     *
     * @param cms a CMS context
     * @param path a path
     * @param filter the resource filter to use
     * @throws Exception if something goes wrong
     */
    private void checkResourceAccessible(CmsObject cms, String path, CmsResourceFilter filter) throws Exception {

        CmsResource resource = cms.readResource(path, filter);
        String folder = CmsResource.getParentFolder(path);
        List<CmsResource> filesInFolder = cms.readResources(folder, filter);
        assertTrue(
            "List of files in folder should include "
                + resource.getRootPath()
                + " for "
                + cms.getRequestContext().getCurrentUser().getName(),
            filesInFolder.contains(resource));
    }

    /**
     * Checks that the resource can't be read either directly or as part of a directory listing.
     *
     * @param cms the CMS context
     * @param path the path
     * @throws Exception if something goes wrong
     */
    private void checkResourceRestricted(CmsObject cms, String path) throws Exception {

        Exception ex1 = null;
        try {
            cms.readResource(path, CmsResourceFilter.IGNORE_EXPIRATION);
        } catch (Exception ex) {
            ex1 = ex;
        }
        assertNotNull(
            "readResource should not have succeeded for path "
                + path
                + " and user "
                + cms.getRequestContext().getCurrentUser().getName(),
            ex1);
        String folder = CmsResource.getParentFolder(path);
        List<CmsResource> filesInFolder = cms.readResources(folder, CmsResourceFilter.IGNORE_EXPIRATION);
        assertFalse(
            "List of files in folder "
                + folder
                + " should not include "
                + path
                + " for user "
                + cms.getRequestContext().getCurrentUser().getName(),
            filesInFolder.stream().anyMatch(res -> path.equals(res.getRootPath())));
    }

    /**
     * Helper method for creating a user with a set of initial groups.
     *
     * @param userName the user name
     * @param groups the names of the groups to assign the user to
     * @return the new user
     * @throws CmsException if something goes wrong
     */
    private CmsUser createUserWithGroups(String userName, String... groups) throws CmsException {

        CmsUser user = getCmsObject().createUser(userName, "password", "", new HashMap<>());
        for (String group : groups) {
            getCmsObject().addUserToGroup(user.getName(), group);
        }
        return user;

    }

    /**
     * Helper method for setting the time used in the exclusive access check for the purpose of testing.
     *
     * @param cms the CmsObject in which to set the time
     * @param time the time to set
     */
    private void setClock(CmsObject cms, final long time) {

        // the request time is used for the normal availability check, while the exclusive access check uses the request context attribute if set
        cms.getRequestContext().setRequestTime(time);
        cms.getRequestContext().setAttribute(
            CmsDriverManager.ATTR_EXCLUSIVE_ACCESS_CLOCK,
            (Supplier<Long>)(() -> Long.valueOf(time)));

    }

    /**
     * Helper method for creating a new CmsObject for a specific user
     * @param cms the CmsObject
     * @param user the user
     * @return the CmsObject initialized with the user
     * @throws Exception if something goes wrong
     */
    private CmsObject switchUser(CmsObject cms, CmsUser user) throws Exception {

        CmsContextInfo context = new CmsContextInfo(cms.getRequestContext());
        context.setUserName(user.getName());
        return OpenCms.initCmsObject(cms, context);

    }

    private void testOutsideTimeRange(CmsObject cms, String resourceName, long released, long expired)
    throws CmsException {

        try {
            // should throw exception
            cms.readResource(resourceName, CmsResourceFilter.DEFAULT);
            fail("Read outside-of-time-range resource with filter CmsResourceFilter.DEFAULT");
        } catch (CmsVfsResourceNotFoundException e) {
            // ok
        }

        CmsResource resource;
        try {
            resource = cms.readResource(resourceName, CmsResourceFilter.ALL);
        } catch (CmsException e) {
            fail("Unable to read outside-of-time-range resource with filter CmsResourceFilter.ALL");
            return;
        }
        assertEquals(released, resource.getDateReleased());
        assertEquals(expired, resource.getDateExpired());
        assertEquals(cms.getRequestContext().getCurrentProject().getUuid(), resource.getProjectLastModified());
    }

}

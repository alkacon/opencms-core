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
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.lock.CmsLockException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionViolationException;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestResourceConfigurableFilter;
import org.opencms.test.OpenCmsTestResourceFilter;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for the create and import methods.<p>
 */
public class TestCreateWriteResource extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCreateWriteResource(String arg0) {

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
        suite.setName(TestCreateWriteResource.class.getName());

        suite.addTest(new TestCreateWriteResource("testCreateResourceLockedFolder"));
        suite.addTest(new TestCreateWriteResource("testImportResource"));
        suite.addTest(new TestCreateWriteResource("testImportResourceAgain"));
        suite.addTest(new TestCreateWriteResource("testImportSibling"));
        suite.addTest(new TestCreateWriteResource("testImportFolder"));
        suite.addTest(new TestCreateWriteResource("testImportFolderAgain"));
        suite.addTest(new TestCreateWriteResource("testCreateResource"));
        suite.addTest(new TestCreateWriteResource("testCreateResourceJsp"));
        suite.addTest(new TestCreateWriteResource("testCreateResourceAgain"));
        suite.addTest(new TestCreateWriteResource("testCreateFolder"));
        suite.addTest(new TestCreateWriteResource("testCreateFolderAgain"));
        suite.addTest(new TestCreateWriteResource("testCreateDotnameResources"));
        suite.addTest(new TestCreateWriteResource("testOverwriteInvisibleResource"));
        suite.addTest(new TestCreateWriteResource("testCreateResourceWithSpecialChars"));

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
     * Test creation of invalid resources that have only dots in their name.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testCreateDotnameResources() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing creating a resource with only dots in the name");

        Exception error = null;
        try {
            cms.createResource("/folder1/.", CmsResourceTypeFolder.getStaticTypeId(), null, null);
        } catch (CmsIllegalArgumentException e) {
            assertEquals(org.opencms.db.Messages.ERR_CREATE_RESOURCE_1, e.getMessageContainer().getKey());
            error = e;
        }
        assertNotNull(error);
        error = null;
        try {
            cms.createResource("/folder1/..", CmsResourceTypePlain.getStaticTypeId(), null, null);
        } catch (CmsIllegalArgumentException e) {
            assertEquals(org.opencms.db.Messages.ERR_CREATE_RESOURCE_1, e.getMessageContainer().getKey());
            error = e;
        }
        assertNotNull(error);
        error = null;
        try {
            cms.createResource("/folder1/.../", CmsResourceTypeFolder.getStaticTypeId(), null, null);
        } catch (CmsIllegalArgumentException e) {
            assertEquals(org.opencms.db.Messages.ERR_CREATE_RESOURCE_1, e.getMessageContainer().getKey());
            error = e;
        }
        assertNotNull(error);
        error = null;
        try {
            cms.createResource("/folder1/....", CmsResourceTypePlain.getStaticTypeId(), null, null);
        } catch (CmsIllegalArgumentException e) {
            assertEquals(org.opencms.db.Messages.ERR_CREATE_RESOURCE_1, e.getMessageContainer().getKey());
            error = e;
        }
        assertNotNull(error);
        error = null;
        try {
            cms.createResource("/folder1/...../", CmsResourceTypeFolder.getStaticTypeId(), null, null);
        } catch (CmsIllegalArgumentException e) {
            assertEquals(org.opencms.db.Messages.ERR_CREATE_RESOURCE_1, e.getMessageContainer().getKey());
            error = e;
        }
        assertNotNull(error);
    }

    /**
     * Test the create resource method for a folder.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testCreateFolder() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing creating a folder");

        String resourcename = "/folder1/test2/";
        long timestamp = System.currentTimeMillis() - 1;

        cms.createResource(resourcename, CmsResourceTypeFolder.getStaticTypeId(), null, null);

        // check the created folder
        CmsFolder folder = cms.readFolder(resourcename);

        assertEquals(folder.getState(), CmsResource.STATE_NEW);
        assertTrue(folder.getDateLastModified() > timestamp);
        assertTrue(folder.getDateCreated() > timestamp);

        // ensure created resource is a folder
        assertIsFolder(cms, resourcename);
        // project must be current project
        assertProject(cms, resourcename, cms.getRequestContext().getCurrentProject());
        // state must be "new"
        assertState(cms, resourcename, CmsResource.STATE_NEW);
        // date last modified
        assertDateLastModifiedAfter(cms, resourcename, timestamp);
        // date created
        assertDateCreatedAfter(cms, resourcename, timestamp);
        // the user last modified must be the current user
        assertUserLastModified(cms, resourcename, cms.getRequestContext().getCurrentUser());

        // publish the project
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);
    }

    /**
     * Test the create a folder again.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testCreateFolderAgain() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing to create an existing folder again");

        String resourcename = "/folder1/test2/";
        storeResources(cms, resourcename);
        long timestamp = System.currentTimeMillis();

        assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);
        cms.lockResource(resourcename);

        try {
            // resource exists and is not deleted, creation must throw exception
            cms.createResource(resourcename, CmsResourceTypeFolder.getStaticTypeId(), null, null);
            fail("Existing resource '" + resourcename + "' was not detected!");
        } catch (CmsVfsResourceAlreadyExistsException e) {
            // ok
        }

        // read resource for comparing id's later
        CmsResource original = cms.readResource(resourcename);

        // delete resource and try again
        cms.deleteResource(resourcename, CmsResource.DELETE_PRESERVE_SIBLINGS);
        // the resource needs to be published first
        OpenCms.getPublishManager().publishResource(cms, resourcename);
        OpenCms.getPublishManager().waitWhileRunning();
        cms.createResource(resourcename, CmsResourceTypeFolder.getStaticTypeId(), null, null);

        // ensure created resource is a folder
        assertIsFolder(cms, resourcename);
        // project must be current project
        assertProject(cms, resourcename, cms.getRequestContext().getCurrentProject());
        // state must be "new"
        assertState(cms, resourcename, CmsResource.STATE_NEW);
        // date last modified
        assertDateLastModifiedAfter(cms, resourcename, timestamp);
        // the user last modified must be the current user
        assertUserLastModified(cms, resourcename, cms.getRequestContext().getCurrentUser());
        // date created
        assertDateCreatedAfter(cms, resourcename, timestamp);
        // the user created must be the current
        assertUserCreated(cms, resourcename, cms.getRequestContext().getCurrentUser());

        // compare id's
        CmsResource created = cms.readResource(resourcename);
        if (created.getResourceId().equals(original.getResourceId())) {
            fail("A created folder that replaced a deleted folder must not have the same resource id!");
        }

        // publish the project
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);
    }

    /**
     * Test the create resource method.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testCreateResource() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing create resource");

        String resourcename = "/folder1/test2.html";
        long timestamp = System.currentTimeMillis() - 1;

        String contentStr = "Hello this is my other content";
        byte[] content = contentStr.getBytes();

        cms.createResource(resourcename, CmsResourceTypePlain.getStaticTypeId(), content, null);

        // ensure created resource type
        assertResourceType(cms, resourcename, CmsResourceTypePlain.getStaticTypeId());
        // project must be current project
        assertProject(cms, resourcename, cms.getRequestContext().getCurrentProject());
        // state must be "new"
        assertState(cms, resourcename, CmsResource.STATE_NEW);
        // date last modified
        assertDateLastModifiedAfter(cms, resourcename, timestamp);
        // date created
        assertDateCreatedAfter(cms, resourcename, timestamp);
        // the user last modified must be the current user
        assertUserLastModified(cms, resourcename, cms.getRequestContext().getCurrentUser());
        // check the content
        assertContent(cms, resourcename, content);

        // publish the project
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);
    }

    /**
     * Test the create resource method for an already existing resource.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testCreateResourceAgain() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing to create an existing resource again");

        String resourcename = "/folder1/test2.html";
        storeResources(cms, resourcename);
        long timestamp = System.currentTimeMillis();

        String contentStr = "Hello this is my NEW AND ALSO CHANGED other content";
        byte[] content = contentStr.getBytes();

        assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);
        cms.lockResource(resourcename);

        try {
            // resource exists and is not deleted, creation must throw exception
            cms.createResource(resourcename, CmsResourceTypePlain.getStaticTypeId(), content, null);
        } catch (Throwable e) {
            if (!(e instanceof CmsVfsResourceAlreadyExistsException)) {
                fail("Existing resource '" + resourcename + "' was not detected!");
            }
        }

        // read resource for comparing id's later
        CmsResource original = cms.readResource(resourcename);

        // delete resource and try again
        cms.deleteResource(resourcename, CmsResource.DELETE_PRESERVE_SIBLINGS);
        // the resource needs to be published first
        OpenCms.getPublishManager().publishResource(cms, resourcename);
        OpenCms.getPublishManager().waitWhileRunning();
        cms.createResource(resourcename, CmsResourceTypePlain.getStaticTypeId(), content, null);

        // project must be current project
        assertProject(cms, resourcename, cms.getRequestContext().getCurrentProject());
        // state must be "new"
        assertState(cms, resourcename, CmsResource.STATE_NEW);
        // date last modified
        assertDateLastModifiedAfter(cms, resourcename, timestamp);
        // the user last modified must be the current user
        assertUserLastModified(cms, resourcename, cms.getRequestContext().getCurrentUser());
        // date created
        assertDateCreatedAfter(cms, resourcename, timestamp);
        // the user created must be the current user
        assertUserCreated(cms, resourcename, cms.getRequestContext().getCurrentUser());
        // check the content
        assertContent(cms, resourcename, content);

        // compare id's
        CmsResource created = cms.readResource(resourcename);
        if (created.getResourceId().equals(original.getResourceId())) {
            fail("A created resource that replaced a deleted resource must not have the same resource id!");
        }

        // publish the project
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);
    }

    /**
     * Test the create resource method for jsp files without permissions.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testCreateResourceJsp() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing create resource for jsp files without permissions");
        CmsProject offlineProject = cms.getRequestContext().getCurrentProject();

        String path = "/testCreateResourceJsp.jsp";
        String contentStr = "this is a really bad jsp code";

        // this should work since we are admin
        cms.createResource(path, CmsResourceTypeJsp.getJSPTypeId(), contentStr.getBytes(), null);

        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(offlineProject);

        String path2 = "/testCreateResourceJsp2.jsp";
        try {
            cms.createResource(path2, CmsResourceTypeJsp.getJSPTypeId(), contentStr.getBytes(), null);
            fail("createResource for jsp without permissions should fail");
        } catch (CmsSecurityException e) {
            // ok
        }
    }

    /**
     * Test resource creation in a locked folder.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testCreateResourceLockedFolder() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing resource creation in a locked folder");

        String foldername = "/folder1/";
        String resourcename = foldername + "newResLF.html";

        // lock the folder
        cms.lockResource(foldername);

        // login as test2
        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        // try to create the file
        try {
            cms.createResource(resourcename, CmsResourceTypePlain.getStaticTypeId());
            fail("should not be able to create a file in a locked folder.");
        } catch (CmsLockException e) {
            // ok, ignore
        }

        // now try as the user that owns the folder lock
        cms = getCmsObject();

        cms.createResource(resourcename, CmsResourceTypePlain.getStaticTypeId());
    }

    /**
     * Test the create resource method.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testCreateResourceWithSpecialChars() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing create resource with special character \"$\"");

        String resourcename = "/folder1/test$2.html";
        long timestamp = System.currentTimeMillis() - 1;

        String contentStr = "Hello this is my content";
        byte[] content = contentStr.getBytes();

        cms.createResource(resourcename, CmsResourceTypePlain.getStaticTypeId(), content, null);

        // ensure created resource type
        assertResourceType(cms, resourcename, CmsResourceTypePlain.getStaticTypeId());
        // project must be current project
        assertProject(cms, resourcename, cms.getRequestContext().getCurrentProject());
        // state must be "new"
        assertState(cms, resourcename, CmsResource.STATE_NEW);
        // date last modified
        assertDateLastModifiedAfter(cms, resourcename, timestamp);
        // date created
        assertDateCreatedAfter(cms, resourcename, timestamp);
        // the user last modified must be the current user
        assertUserLastModified(cms, resourcename, cms.getRequestContext().getCurrentUser());
        // check the content
        assertContent(cms, resourcename, content);

        // publish the project
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);
    }

    /**
     * Test the import resource method with a folder.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testImportFolder() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing import resource for a folder");

        String resourcename = "/folder1/test1/";

        long timestamp = System.currentTimeMillis() - 87654321;

        // create a new resource
        CmsResource resource = new CmsResource(
            CmsUUID.getNullUUID(),
            CmsUUID.getNullUUID(),
            resourcename,
            CmsResourceTypeFolder.getStaticTypeId(),
            true,
            0,
            cms.getRequestContext().getCurrentProject().getUuid(),
            CmsResource.STATE_NEW,
            timestamp,
            cms.getRequestContext().getCurrentUser().getId(),
            timestamp,
            cms.getRequestContext().getCurrentUser().getId(),
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT,
            1,
            -1,
            0,
            0);

        cms.importResource(resourcename, resource, null, null);

        // ensure created resource is a folder
        assertIsFolder(cms, resourcename);
        // project must be current project
        assertProject(cms, resourcename, cms.getRequestContext().getCurrentProject());
        // state must be "new"
        assertState(cms, resourcename, CmsResource.STATE_NEW);
        // date last modified
        assertDateLastModified(cms, resourcename, timestamp);
        // date created
        assertDateCreated(cms, resourcename, timestamp);
        // the user last modified must be the current user
        assertUserLastModified(cms, resourcename, cms.getRequestContext().getCurrentUser());

        // publish the project
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);
    }

    /**
     * Test the import resource method for an existing folder.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testImportFolderAgain() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing to import an existing folder again");

        String resourcename = "/folder1/test1/";

        storeResources(cms, resourcename);
        long timestamp = System.currentTimeMillis() - 12345678;

        CmsFolder folder = cms.readFolder(resourcename);

        // create a new folder
        CmsResource resource = new CmsResource(
            folder.getStructureId(),
            new CmsUUID(),
            resourcename,
            CmsResourceTypeFolder.getStaticTypeId(),
            true,
            0,
            cms.getRequestContext().getCurrentProject().getUuid(),
            CmsResource.STATE_NEW,
            timestamp,
            cms.getRequestContext().getCurrentUser().getId(),
            timestamp,
            cms.getRequestContext().getCurrentUser().getId(),
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT,
            1,
            -1,
            0,
            0);

        cms.importResource(resourcename, resource, null, null);

        // ensure created resource is a folder
        assertIsFolder(cms, resourcename);
        // project must be current project
        assertProject(cms, resourcename, cms.getRequestContext().getCurrentProject());
        // state must be "new"
        assertState(cms, resourcename, CmsResource.STATE_CHANGED);
        // date last modified
        assertDateLastModified(cms, resourcename, timestamp);
        // the user last modified must be the current user
        assertUserLastModified(cms, resourcename, cms.getRequestContext().getCurrentUser());
        // now evaluate the filter
        assertFilter(cms, resourcename, OpenCmsTestResourceFilter.FILTER_CREATE_RESOURCE);

        // publish the project
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);
    }

    /**
     * Test the import resource method.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testImportResource() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing import resource");

        String resourcename = "/folder1/test1.html";

        String contentStr = "Hello this is my content";
        byte[] content = contentStr.getBytes();
        long timestamp = System.currentTimeMillis() - 87654321;

        // create a new resource
        CmsResource resource = new CmsResource(
            CmsUUID.getNullUUID(),
            CmsUUID.getNullUUID(),
            resourcename,
            CmsResourceTypePlain.getStaticTypeId(),
            false,
            0,
            cms.getRequestContext().getCurrentProject().getUuid(),
            CmsResource.STATE_NEW,
            timestamp,
            cms.getRequestContext().getCurrentUser().getId(),
            timestamp,
            cms.getRequestContext().getCurrentUser().getId(),
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT,
            1,
            content.length,
            0,
            0);

        cms.importResource(resourcename, resource, content, null);

        // ensure created resource type
        assertResourceType(cms, resourcename, CmsResourceTypePlain.getStaticTypeId());
        // project must be current project
        assertProject(cms, resourcename, cms.getRequestContext().getCurrentProject());
        // state must be "new"
        assertState(cms, resourcename, CmsResource.STATE_NEW);
        // date last modified
        assertDateLastModified(cms, resourcename, timestamp);
        // date created
        assertDateCreated(cms, resourcename, timestamp);
        // the user last modified must be the current user
        assertUserLastModified(cms, resourcename, cms.getRequestContext().getCurrentUser());
        // the content
        assertContent(cms, resourcename, content);

        // publish the project
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);
    }

    /**
     * Test the import resource method.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testImportResourceAgain() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing to import an existing resource again");

        String resourcename = "/folder1/test1.html";

        storeResources(cms, resourcename);
        long timestamp = System.currentTimeMillis() - 12345678;

        CmsResource res = cms.readResource(resourcename);

        String contentStr = "Hello this is my NEW AND CHANGED content";
        byte[] content = contentStr.getBytes();

        // create a new resource
        CmsResource resource = new CmsResource(
            res.getStructureId(),
            res.getResourceId(),
            resourcename,
            CmsResourceTypePlain.getStaticTypeId(),
            false,
            0,
            cms.getRequestContext().getCurrentProject().getUuid(),
            CmsResource.STATE_NEW,
            timestamp,
            cms.getRequestContext().getCurrentUser().getId(),
            timestamp,
            cms.getRequestContext().getCurrentUser().getId(),
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT,
            1,
            content.length,
            0,
            0);

        cms.importResource(resourcename, resource, content, null);

        // ensure created resource type
        assertResourceType(cms, resourcename, CmsResourceTypePlain.getStaticTypeId());
        // project must be current project
        assertProject(cms, resourcename, cms.getRequestContext().getCurrentProject());
        // state must be "new"
        assertState(cms, resourcename, CmsResource.STATE_CHANGED);
        // date last modified
        assertDateLastModified(cms, resourcename, timestamp);
        // the user last modified must be the current user
        assertUserLastModified(cms, resourcename, cms.getRequestContext().getCurrentUser());
        // now evaluate the filter
        assertFilter(cms, resourcename, OpenCmsTestResourceFilter.FILTER_CREATE_RESOURCE);

        // publish the project
        cms.unlockProject(cms.getRequestContext().getCurrentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        assertState(cms, resourcename, CmsResource.STATE_UNCHANGED);
    }

    /**
     * Test the import of a sibling.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testImportSibling() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing to import an existing resource as sibling");

        CmsProperty prop1 = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "The title", null);
        CmsProperty prop2 = new CmsProperty(CmsPropertyDefinition.PROPERTY_DESCRIPTION, "The description", null);
        CmsProperty prop3 = new CmsProperty(CmsPropertyDefinition.PROPERTY_KEYWORDS, "The keywords", null);

        List<CmsProperty> properties = new ArrayList<CmsProperty>();
        properties.add(prop1);

        String siblingname = "/folder1/test1.html";

        // make sure some non-shared properties are attached to the sibling
        cms.lockResource(siblingname);
        cms.writePropertyObjects(siblingname, properties);
        cms.unlockResource(siblingname);

        long timestamp = System.currentTimeMillis() - 12345678;

        String resourcename1 = "/folder2/test1_sib1.html";
        String resourcename2 = "/folder1/subfolder11/test1_sib2.html";

        // read the existing resource to create siblings for
        CmsFile file = cms.readFile(siblingname);
        byte[] content = file.getContents();

        assertTrue(file.getLength() > 0);
        assertTrue(content.length > 0);

        System.err.println(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot());
        storeResources(cms, siblingname);
        System.err.println(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot());

        // create a new resource
        CmsResource resource;

        // cw: Test changed: must now provide correct content size in resource
        resource = new CmsResource(
            CmsUUID.getNullUUID(),
            file.getResourceId(),
            resourcename2,
            CmsResourceTypePlain.getStaticTypeId(),
            false,
            0,
            cms.getRequestContext().getCurrentProject().getUuid(),
            CmsResource.STATE_NEW,
            timestamp,
            cms.getRequestContext().getCurrentUser().getId(),
            timestamp,
            cms.getRequestContext().getCurrentUser().getId(),
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT,
            1,
            content.length,
            0,
            0);

        properties.add(prop2);
        // using null as content must create sibling of existing content
        cms.importResource(resourcename2, resource, null, properties);
        System.err.println(OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot());

        // project must be current project
        assertProject(cms, resourcename2, cms.getRequestContext().getCurrentProject());
        // resource type
        assertResourceType(cms, resourcename2, CmsResourceTypePlain.getStaticTypeId());
        assertResourceType(cms, siblingname, CmsResourceTypePlain.getStaticTypeId());
        // state
        assertState(cms, resourcename2, CmsResource.STATE_NEW);
        assertState(cms, siblingname, CmsResource.STATE_CHANGED);
        // date last modified
        assertDateLastModified(cms, resourcename2, file.getDateLastModified());
        assertDateLastModified(cms, siblingname, file.getDateLastModified());
        // the user last modified
        assertUserLastModified(cms, resourcename2, cms.getRequestContext().getCurrentUser());
        assertUserLastModified(cms, siblingname, cms.getRequestContext().getCurrentUser());
        // content must be identical to stored content of new resource
        assertContent(cms, resourcename2, content);
        assertContent(cms, siblingname, content);
        // check the sibling count
        assertSiblingCountIncremented(cms, siblingname, 1);

        // now evaluate the filter
        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter(
            OpenCmsTestResourceFilter.FILTER_CREATE_RESOURCE);

        filter.disableSiblingCountTest();
        assertFilter(cms, siblingname, filter);

        String contentStr = "Hello this is my NEW AND CHANGED sibling content";
        content = contentStr.getBytes();

        resource = new CmsResource(
            new CmsUUID(),
            file.getResourceId(),
            resourcename1,
            CmsResourceTypePlain.getStaticTypeId(),
            false,
            0,
            cms.getRequestContext().getCurrentProject().getUuid(),
            CmsResource.STATE_NEW,
            timestamp,
            cms.getRequestContext().getCurrentUser().getId(),
            timestamp,
            cms.getRequestContext().getCurrentUser().getId(),
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT,
            1,
            content.length,
            timestamp,
            0);

        properties.add(prop3);
        // using new content must replace existing content
        cms.importResource(resourcename1, resource, content, properties);

        // project must be current project
        assertProject(cms, resourcename1, cms.getRequestContext().getCurrentProject());
        assertProject(cms, resourcename2, cms.getRequestContext().getCurrentProject());
        // resource type
        assertResourceType(cms, resourcename1, CmsResourceTypePlain.getStaticTypeId());
        assertResourceType(cms, resourcename2, CmsResourceTypePlain.getStaticTypeId());
        assertResourceType(cms, siblingname, CmsResourceTypePlain.getStaticTypeId());
        // state
        assertState(cms, resourcename1, CmsResource.STATE_NEW);
        assertState(cms, resourcename2, CmsResource.STATE_NEW);
        assertState(cms, siblingname, CmsResource.STATE_CHANGED);
        // date last modified
        assertDateLastModified(cms, resourcename1, timestamp);
        assertDateLastModified(cms, resourcename2, timestamp);
        assertDateLastModified(cms, siblingname, timestamp);
        // the user last modified
        assertUserLastModified(cms, resourcename1, cms.getRequestContext().getCurrentUser());
        assertUserLastModified(cms, resourcename2, cms.getRequestContext().getCurrentUser());
        assertUserLastModified(cms, siblingname, cms.getRequestContext().getCurrentUser());
        // content must be identical to stored content of new resource
        assertContent(cms, resourcename1, content);
        assertContent(cms, resourcename2, content);
        assertContent(cms, siblingname, content);
        // check the sibling count
        assertSiblingCountIncremented(cms, siblingname, 2);

        // now evaluate the filter
        assertFilter(cms, siblingname, filter);

        // publish the project
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        assertState(cms, resourcename1, CmsResource.STATE_UNCHANGED);
        assertState(cms, resourcename2, CmsResource.STATE_UNCHANGED);
    }

    /**
     * Tests to overwrite invisible resource.<p>
     *
     * @throws Exception if the test fails
     */
    public void testOverwriteInvisibleResource() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing to overwrite invisible resource");

        // Creating paths
        String source = "index.html";
        String target = "/test_index.html";

        cms.createResource(target, CmsResourceTypePlain.getStaticTypeId());

        // remove read permission for user test2
        cms.chacc(target, I_CmsPrincipal.PRINCIPAL_USER, "test2", "-r+v+i");
        cms.unlockResource(target);
        storeResources(cms, source);
        storeResources(cms, target);

        // login as test2
        cms.loginUser("test2", "test2");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        // try to read the file
        try {
            cms.readResource(target, CmsResourceFilter.ALL);
            fail("should fail to read the resource without permissions");
        } catch (CmsPermissionViolationException e) {
            // ok
        }

        // try to overwrite without locking
        try {
            cms.copyResource(source, target);
            fail("should fail to overwrite a resource without a lock on the target");
        } catch (CmsLockException e) {
            // ok
        }

        // try to lock the target, this is not possible because of missing permissions
        try {
            cms.lockResource(target);
            fail("should fail to overwrite the resource without read permissions");
        } catch (CmsPermissionViolationException e) {
            // ok
        }

        // now try to create a resource with the same name, must also fail
        try {
            cms.createResource(target, CmsResourceTypeXmlPage.getStaticTypeId());
            fail("should fail to create a resource that already exists");
        } catch (CmsLockException e) {
            // ok
        }

        // login again as admin
        cms.loginUser("Admin", "admin");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        // make sure nothing has been changed
        assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_EQUAL);
        assertFilter(cms, target, OpenCmsTestResourceFilter.FILTER_EQUAL);
    }
}

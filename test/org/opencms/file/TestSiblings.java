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

import org.opencms.db.CmsPublishList;
import org.opencms.db.CmsResourceState;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.importexport.CmsImportParameters;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.report.CmsShellReport;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestResourceFilter;
import org.opencms.util.CmsResourceTranslator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit test for operations on siblings.<p>
 */
public class TestSiblings extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestSiblings(String arg0) {

        super(arg0);
    }

    /**
     * Creates a copy of a resource as a new sibling.<p>
     *
     * @param tc the OpenCms test case
     * @param cms the current user's Cms object
     * @param source path/resource name of the existing resource
     * @param target path/resource name of the new sibling
     * @throws Exception if something goes wrong
     */
    public static void copyResourceAsSibling(OpenCmsTestCase tc, CmsObject cms, String source, String target)
    throws Exception {

        // save the source in the store
        tc.storeResources(cms, source);

        // copy source to target as a sibling, the new sibling should not be locked
        cms.copyResource(source, target, CmsResource.COPY_AS_SIBLING);

        // validate the source sibling

        // validate if all unmodified fields on the source resource are still equal
        tc.assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_EXISTING_SIBLING);
        // validate if the last-modified-in-project field is the current project
        tc.assertProject(cms, source, cms.getRequestContext().getCurrentProject());
        // validate if the sibling count field has been incremented
        tc.assertSiblingCountIncremented(cms, source, 1);
        // validate if the sibling does not have a red flag
        tc.assertModifiedInCurrentProject(cms, source, false);
        // validate if the lock is an exclusive shared lock for the current user
        tc.assertLock(cms, source, CmsLockType.SHARED_EXCLUSIVE);

        // validate the target sibling

        // validate the fields that both in the existing and the new sibling have to be equal
        tc.assertFilter(cms, source, target, OpenCmsTestResourceFilter.FILTER_EXISTING_AND_NEW_SIBLING);
        // validate if the state of the new sibling is "new" (blue)
        tc.assertState(cms, target, CmsResource.STATE_NEW);
        // validate if the new sibling has a red flag
        tc.assertModifiedInCurrentProject(cms, target, true);
        // validate if the lock is an exclusive lock for the current user
        tc.assertLock(cms, target, CmsLockType.EXCLUSIVE);
    }

    /**
     * Creates a new sibling of a resource.<p>
     *
     * @param tc the OpenCms test case
     * @param cms the current user's Cms object
     * @param source path/resource name of the existing resource
     * @param target path/resource name of the new sibling
     * @throws Exception if something goes wrong
     */
    public static void createSibling(OpenCmsTestCase tc, CmsObject cms, String source, String target) throws Exception {

        // save the source in the store
        tc.storeResources(cms, source);

        // create a new sibling from the source
        List properties = cms.readPropertyObjects(source, false);
        cms.createSibling(source, target, properties);

        // validate the source sibling

        // validate if all unmodified fields of the source are still equal
        tc.assertFilter(cms, source, OpenCmsTestResourceFilter.FILTER_EXISTING_SIBLING);
        // validate if the last-modified-in-project field is the current project
        tc.assertProject(cms, source, cms.getRequestContext().getCurrentProject());
        // validate if the sibling count field has been incremented
        tc.assertSiblingCountIncremented(cms, source, 1);
        // validate if the sibling does not have a red flag
        tc.assertModifiedInCurrentProject(cms, source, false);
        // validate if the lock is an exclusive shared lock for the current user
        tc.assertLock(cms, source, CmsLockType.SHARED_EXCLUSIVE);

        // validate the target sibling

        // validate the fields that both in the existing and the new sibling have to be equal
        tc.assertFilter(cms, source, target, OpenCmsTestResourceFilter.FILTER_EXISTING_AND_NEW_SIBLING);
        // validate if the state of the new sibling is "new" (blue)
        tc.assertState(cms, target, CmsResource.STATE_NEW);
        // validate if the new sibling has a red flag
        tc.assertModifiedInCurrentProject(cms, target, true);
        // validate if the lock is an exclusive lock for the current user
        tc.assertLock(cms, target, CmsLockType.EXCLUSIVE);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestSiblings.class.getName());

        suite.addTest(new TestSiblings("testSiblingsCopy"));
        suite.addTest(new TestSiblings("testSiblingsCreate"));
        suite.addTest(new TestSiblings("testSiblingIssueAfterImport"));
        suite.addTest(new TestSiblings("testDeleteAllSiblings"));
        suite.addTest(new TestSiblings("testSiblingStateIssue"));
        suite.addTest(new TestSiblings("testSiblingsRelations"));
        suite.addTest(new TestSiblings("testSiblingProjects"));
        suite.addTest(new TestSiblings("testSiblingsCreateIssue"));
        suite.addTest(new TestSiblings("testSiblingsV7PublishIssue"));
        suite.addTest(new TestSiblings("testSiblingsNewDeletePublishIssue"));

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
     * Tests deletion of a resource together with all siblings.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testDeleteAllSiblings() throws Throwable {

        echo("Creating a new resource with 2 siblings, then deleting it with all siblings again");
        CmsObject cms = getCmsObject();

        String sib1Name = "/folder1/sib1.txt";
        String sib2Name = "/folder1/sib2.txt";
        String sib3Name = "/folder1/sib3.txt";

        cms.createResource(sib1Name, CmsResourceTypePlain.getStaticTypeId());
        cms.createSibling(sib1Name, sib2Name, Collections.EMPTY_LIST);
        cms.createSibling(sib1Name, sib3Name, Collections.EMPTY_LIST);

        cms.deleteResource(sib1Name, CmsResource.DELETE_REMOVE_SIBLINGS);

        CmsResource sib2Resource = null;
        try {
            sib2Resource = cms.readResource(sib2Name);
        } catch (CmsVfsResourceNotFoundException e) {
            // intentionally left blank
        }

        if (sib2Resource != null) {
            fail("Sibling " + sib2Name + " has not been deleted!");
        }

        CmsResource sib3Resource = null;
        try {
            sib3Resource = cms.readResource(sib3Name);
        } catch (CmsVfsResourceNotFoundException e) {
            // intentionally left blank
        }

        if (sib3Resource != null) {
            fail("Sibling " + sib3Name + " has not been deleted!");
        }
    }

    /**
     * Error scenario where an import is deleted that contains siblings inside the same folders.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testSiblingIssueAfterImport() throws Exception {

        echo("Testing sibling issue after import");

        CmsResourceTranslator oldFolderTranslator = OpenCms.getResourceManager().getFolderTranslator();

        CmsResourceTranslator folderTranslator = new CmsResourceTranslator(
            new String[] {
                "s#^/sites/default/content/bodys(.*)#/system/bodies$1#",
                "s#^/sites/default/pics/system(.*)#/system/workplace/resources$1#",
                "s#^/sites/default/pics(.*)#/system/galleries/pics$1#",
                "s#^/sites/default/download(.*)#/system/galleries/download$1#",
                "s#^/sites/default/externallinks(.*)#/system/galleries/externallinks$1#",
                "s#^/sites/default/htmlgalleries(.*)#/system/galleries/htmlgalleries$1#",
                "s#^/sites/default/content(.*)#/system$1#"},
            false);

        // set modified folder translator
        OpenCms.getResourceManager().setTranslators(
            folderTranslator,
            OpenCms.getResourceManager().getFileTranslator(),
            OpenCms.getResourceManager().getXsdTranslator());

        try {

            CmsObject cms = getCmsObject();

            cms.getRequestContext().setSiteRoot("/");

            // need to create the "galleries" folder manually
            cms.createResource("/system/galleries", CmsResourceTypeFolder.RESOURCE_TYPE_ID);
            cms.unlockResource("/system/galleries");

            cms.getRequestContext().setSiteRoot("/sites/default");

            // import the files
            String importFile = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf("packages/testimport01.zip");
            OpenCms.getImportExportManager().importData(
                cms,
                new CmsShellReport(cms.getRequestContext().getLocale()),
                new CmsImportParameters(importFile, "/", true));

            // clean up for the next test
            cms.getRequestContext().setSiteRoot("/");
            cms.lockResource("/sites/default");
            cms.lockResource("/system");

            // using the option "DELETE_REMOVE_SIBLINGS" caused an error here!
            cms.deleteResource("/sites/default/importtest", CmsResource.DELETE_REMOVE_SIBLINGS);
            cms.deleteResource("/system/galleries/pics", CmsResource.DELETE_REMOVE_SIBLINGS);

            cms.unlockResource("/sites/default");
            cms.unlockResource("/system");
            OpenCms.getPublishManager().publishProject(cms);
            OpenCms.getPublishManager().waitWhileRunning();
        } finally {

            // reset the translation rules
            OpenCms.getResourceManager().setTranslators(
                oldFolderTranslator,
                OpenCms.getResourceManager().getFileTranslator(),
                OpenCms.getResourceManager().getXsdTranslator());

        }
    }

    /**
     * Tests the "project last modified" state with sibling operations across different projects.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testSiblingProjects() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing the 'project last modified' state of siblings across different projects");

        String source = "/folder1/image1.gif";
        String target = "/folder2/image1_sibling2.gif";
        createSibling(this, cms, source, target);

        OpenCms.getPublishManager().publishResource(cms, target, true, new CmsShellReport(Locale.ENGLISH));
        OpenCms.getPublishManager().waitWhileRunning();

        assertState(cms, source, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, target, CmsResourceState.STATE_UNCHANGED);
        assertLock(cms, source, CmsLockType.UNLOCKED);
        assertLock(cms, target, CmsLockType.UNLOCKED);

        // create a new org unit, that will automatically create a new project...
        CmsOrganizationalUnit ou = OpenCms.getOrgUnitManager().createOrganizationalUnit(
            cms,
            "/test",
            "test",
            0,
            "/folder1");

        // this user will have publish permissions on the test ou project, but not on the root Offline project
        cms.createUser("/test/myuser", "myuser", "blah-blah", null);
        cms.addUserToGroup("/test/myuser", ou.getName() + OpenCms.getDefaultUsers().getGroupUsers());
        // the default permissions for the user are: +r+w+v+c since it is indirect user of the /Users group,
        // so we need to explicitly remove the w flag, but we can not do that on the sibling itself,
        // since the ACEs are bound to the resource entries, so we do it on the folder
        cms.lockResource("/folder2/");
        cms.chacc("/folder2/", I_CmsPrincipal.PRINCIPAL_USER, "/test/myuser", "-w");

        OpenCms.getPublishManager().publishResource(cms, "/folder2/");
        OpenCms.getPublishManager().waitWhileRunning();

        CmsProject prj = cms.readProject(ou.getProjectId());

        cms.loginUser("/test/myuser", "myuser");
        cms.getRequestContext().setCurrentProject(prj);

        // check the permissions
        assertTrue(
            cms.hasPermissions(
                cms.readResource(source, CmsResourceFilter.ALL),
                CmsPermissionSet.ACCESS_WRITE,
                false,
                CmsResourceFilter.ALL));
        assertFalse(
            cms.hasPermissions(
                cms.readResource(target, CmsResourceFilter.ALL),
                CmsPermissionSet.ACCESS_WRITE,
                false,
                CmsResourceFilter.ALL));

        // change a resource attribute
        cms.lockResource(source);
        cms.setDateLastModified(source, System.currentTimeMillis(), false);
        // check the project las modified
        assertProject(cms, source, prj);
        // the sibling has the same project last modified
        assertProject(cms, target, prj);

        // check the publish list
        CmsPublishList pl = OpenCms.getPublishManager().getPublishList(cms, cms.readResource(source), true);
        assertEquals(2, pl.size());
        assertTrue(pl.getFileList().contains(cms.readResource(source)));
        assertTrue(pl.getFileList().contains(cms.readResource("/folder1/image1_sibling.gif")));

        // publish
        OpenCms.getPublishManager().publishResource(cms, source, true, new CmsShellReport(Locale.ENGLISH));
        OpenCms.getPublishManager().waitWhileRunning();
        assertState(cms, source, CmsResourceState.STATE_UNCHANGED);
        // the sibling has been published, since we just changed a resource attribute, this is ok so
        assertState(cms, target, CmsResourceState.STATE_UNCHANGED);

        // now the same for an structure property
        cms.lockResource(source);
        cms.writePropertyObject(source, new CmsProperty(CmsPropertyDefinition.PROPERTY_DESCRIPTION, "desc", null));
        // check the project las modified
        assertProject(cms, source, prj);
        // the sibling has the same project last modified
        assertProject(cms, target, prj);
        // the sibling is still unchanged since no shared property has been changed
        assertState(cms, target, CmsResourceState.STATE_UNCHANGED);

        // check the publish list
        pl = OpenCms.getPublishManager().getPublishList(cms, cms.readResource(source), true);
        assertEquals(1, pl.size());
        assertTrue(pl.getFileList().contains(cms.readResource(source)));

        // publish
        OpenCms.getPublishManager().publishResource(cms, source, true, new CmsShellReport(Locale.ENGLISH));
        OpenCms.getPublishManager().waitWhileRunning();

        assertState(cms, source, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, target, CmsResourceState.STATE_UNCHANGED);

        // now check deleting the sibling
        cms.lockResource(source);
        cms.deleteResource(source, CmsResource.DELETE_REMOVE_SIBLINGS);
        assertState(cms, source, CmsResourceState.STATE_DELETED);
        // the sibling has not been deleted, since the current user has no write permissions on the sibling!
        assertState(cms, target, CmsResourceState.STATE_UNCHANGED);

        // check the publish list
        pl = OpenCms.getPublishManager().getPublishList(cms, cms.readResource(source, CmsResourceFilter.ALL), true);
        assertEquals(2, pl.size());
        assertTrue(pl.getFileList().contains(cms.readResource(source, CmsResourceFilter.ALL)));
        assertTrue(pl.getFileList().contains(cms.readResource("/folder1/image1_sibling.gif", CmsResourceFilter.ALL)));

        // publish
        OpenCms.getPublishManager().publishResource(cms, source, true, new CmsShellReport(Locale.ENGLISH));
        OpenCms.getPublishManager().waitWhileRunning();
        assertFalse(cms.existsResource(source));
        // the sibling has not been published!
        assertTrue(cms.existsResource(target));
        assertState(cms, target, CmsResourceState.STATE_UNCHANGED);
        assertSiblingCount(cms, target, 1);
    }

    /**
     * Tests the "copy as new sibling" function.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testSiblingsCopy() throws Throwable {

        CmsObject cms = getCmsObject();
        String source = "/index.html";
        String target = "/index_sibling.html";
        echo("Copying " + source + " as a new sibling to " + target);
        copyResourceAsSibling(this, cms, source, target);
    }

    /**
     * Does an "undo changes" from the online project on a resource with more than 1 sibling.<p>
     */
    /*
     public static void undoChangesWithSiblings(...) throws Throwable {
     // this test should do the following:
     // - create a sibling of a resource
     // - e.g. touch the black/unchanged sibling so that it gets red/changed
     // - make an "undo changes" -> the last-modified-in-project ID in the resource record
     // of the resource must be the ID of the current project, and not 0
     // - this is to ensure that the new/changed/deleted other sibling still have a valid
     // state which consist of the last-modified-in-project ID plus the resource state
     // - otherwise this may result in grey flags

     Another issue:
     What happens if a user A has an exclusive lock on a resource X,
     and user B does a "copy as sibling Y" of X, or "create
     new sibling Y" of X. The lock status of the resource X is exclusive
     to A, but test implies that it would be switched to B after operation!
     Maybe copy as / create new sibling must not be allowed if original is
     currently locked by another user?

     }
     */

    /**
     * Tests the "copy as new sibling" function.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testSiblingsCreate() throws Throwable {

        CmsObject cms = getCmsObject();
        String source = "/folder1/image1.gif";
        String target = "/folder1/image1_sibling.gif";
        echo("Creating a new sibling " + target + " from " + source);
        createSibling(this, cms, source, target);
    }

    /**
     * Tests creating 2 new siblings and publishing just one of them.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testSiblingsCreateIssue() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing creating 2 new siblings and publishing just one of them");

        String source = "/folder1/newsource.txt";
        cms.createResource(source, CmsResourceTypePlain.getStaticTypeId());
        String target = "/folder1/newsibling.txt";
        cms.createSibling(source, target, null);

        assertState(cms, source, CmsResourceState.STATE_NEW);
        assertState(cms, target, CmsResourceState.STATE_NEW);

        OpenCms.getPublishManager().publishResource(cms, source);
        OpenCms.getPublishManager().waitWhileRunning();

        assertState(cms, source, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, target, CmsResourceState.STATE_NEW);
    }

    /**
     * Tests an issue, where there is an error in OpenCms V7 when publishing two siblings of the same resource.
     * One sibling is new created, the other one is a deleted one. In OpenCms V8 this scenario should give no error.<p>
     *
     * @throws Exception if the test fails
     */
    public void testSiblingsNewDeletePublishIssue() throws Exception {

        echo("Tests publish issue with two siblings (one new sibling and one deleted sibling) of the same resource.");
        CmsObject cms = getCmsObject();

        CmsProject offlineProject = cms.getRequestContext().getCurrentProject();
        CmsProject onlineProject = cms.readProject(CmsProject.ONLINE_PROJECT_ID);

        // step 1: create the basic scenario

        // first we create a complete new folder as base for the test
        String mainFolder = "/test/";
        cms.createResource(mainFolder, CmsResourceTypeFolder.getStaticTypeId());

        // create the resource where siblings are made from later
        String source = mainFolder + "testabc.xml";
        cms.createResource(source, CmsResourceTypePlain.getStaticTypeId(), null, null);

        // create the sub folder /test/de/
        String subFolderDe = mainFolder + "de/";
        cms.createResource(subFolderDe, CmsResourceTypeFolder.getStaticTypeId());

        // create the sub folder /test/en/
        String subFolderEn = mainFolder + "en/";
        cms.createResource(subFolderEn, CmsResourceTypeFolder.getStaticTypeId());

        // create the sub folder /test/nl/
        String subFolderNl = mainFolder + "nl/";
        cms.createResource(subFolderNl, CmsResourceTypeFolder.getStaticTypeId());

        // publish the folder
        OpenCms.getPublishManager().publishResource(cms, mainFolder);
        OpenCms.getPublishManager().waitWhileRunning();

        // check both online and offline project
        cms.getRequestContext().setCurrentProject(offlineProject);
        assertState(cms, mainFolder, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, source, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, subFolderDe, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, subFolderEn, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, subFolderNl, CmsResourceState.STATE_UNCHANGED);
        cms.getRequestContext().setCurrentProject(onlineProject);
        assertState(cms, mainFolder, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, source, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, subFolderDe, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, subFolderEn, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, subFolderNl, CmsResourceState.STATE_UNCHANGED);

        // switch back to the "Offline" project
        cms.getRequestContext().setCurrentProject(offlineProject);

        // create a new sibling using the "copy as" option
        // this is the resource which is deleted in step 2
        String siblingDe = subFolderDe + "testabc.xml";
        cms.copyResource(source, siblingDe, CmsResource.COPY_AS_SIBLING);

        // create a new sibling using the "copy as" option
        // with this resource nothing is done in step 2
        String siblingEn = subFolderEn + "testabc.xml";
        cms.copyResource(source, siblingEn, CmsResource.COPY_AS_SIBLING);

        // publish the folder
        OpenCms.getPublishManager().publishResource(cms, mainFolder);
        OpenCms.getPublishManager().waitWhileRunning();

        // check both online and offline project
        cms.getRequestContext().setCurrentProject(offlineProject);
        assertState(cms, mainFolder, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, source, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, subFolderDe, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, siblingDe, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, subFolderEn, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, siblingEn, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, subFolderNl, CmsResourceState.STATE_UNCHANGED);
        cms.getRequestContext().setCurrentProject(onlineProject);
        assertState(cms, mainFolder, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, source, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, subFolderDe, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, siblingDe, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, subFolderEn, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, siblingEn, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, subFolderNl, CmsResourceState.STATE_UNCHANGED);

        // step 2: create the issue scenario
        // delete one sibling, create another sibling of the same resource and publish the project then

        // switch back to the "Offline" project
        cms.getRequestContext().setCurrentProject(offlineProject);

        // delete the sibling /test/de/testabc.xml
        cms.lockResource(siblingDe);
        cms.deleteResource(siblingDe, CmsResource.DELETE_PRESERVE_SIBLINGS);

        // create a new sibling /test/nl/testabc.xml
        String siblingNl = subFolderNl + "testabc.xml";
        cms.copyResource(source, siblingNl, CmsResource.COPY_AS_SIBLING);

        // publish the project
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        // check that the sibling to delete is really deleted in the "Offline" project
        cms.getRequestContext().setCurrentProject(offlineProject);
        String offReadSiblDe = null;
        try {
            cms.readResource(siblingDe);
        } catch (CmsException e) {
            offReadSiblDe = e.toString();
        }
        // validate the publish result string
        assertNotNull("The sibling /test/de/testabc.xml is not deleted in the 'Offline' project.", offReadSiblDe);

        // check that the sibling to delete is really deleted in the "Online" project
        cms.getRequestContext().setCurrentProject(onlineProject);
        String onReadSiblDe = null;
        try {
            cms.readResource(siblingDe);
        } catch (CmsException e) {
            onReadSiblDe = e.toString();
        }
        // validate the publish result string
        assertNotNull("The sibling /test/de/testabc.xml is not deleted in the 'Online' project.", onReadSiblDe);

        // check both online and offline project
        cms.getRequestContext().setCurrentProject(offlineProject);
        assertState(cms, mainFolder, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, source, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, subFolderDe, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, subFolderEn, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, siblingEn, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, subFolderNl, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, siblingNl, CmsResourceState.STATE_UNCHANGED);
        cms.getRequestContext().setCurrentProject(onlineProject);
        assertState(cms, mainFolder, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, source, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, subFolderDe, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, subFolderEn, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, siblingEn, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, subFolderNl, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, siblingNl, CmsResourceState.STATE_UNCHANGED);
    }

    /**
     * Tests the link management features with siblings.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testSiblingsRelations() throws Throwable {

        echo("Testing link management features with siblings");
        CmsObject cms = getCmsObject();

        String sib1Name = "/folder1/sib1.html";
        String sib2Name = "/folder1/sib2.html";
        String sib3Name = "/folder1/sib3.html";

        String targetName = "/folder1/image2.gif";
        CmsResource target = cms.readResource(targetName);

        CmsResource sib1 = cms.createResource(sib1Name, CmsResourceTypeXmlPage.getStaticTypeId());
        int sources = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size();
        TestLinkValidation.setContent(cms, sib1Name, "<img src='" + targetName + "'>");
        assertEquals(sources + 1, cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size());
        sources++;
        List links = cms.getRelationsForResource(sib1Name, CmsRelationFilter.TARGETS);
        assertEquals(1, links.size());
        CmsRelation relation = new CmsRelation(sib1, target, CmsRelationType.EMBEDDED_IMAGE);
        assertRelation(relation, (CmsRelation)links.get(0));

        cms.createSibling(sib1Name, sib2Name, Collections.EMPTY_LIST);
        CmsResource sib2 = cms.readResource(sib2Name);
        assertEquals(sources + 1, cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size());
        sources++;
        links = cms.getRelationsForResource(sib2Name, CmsRelationFilter.TARGETS);
        assertEquals(1, links.size());
        relation = new CmsRelation(sib2, target, CmsRelationType.EMBEDDED_IMAGE);
        assertRelation(relation, (CmsRelation)links.get(0));

        cms.createSibling(sib1Name, sib3Name, Collections.EMPTY_LIST);
        CmsResource sib3 = cms.readResource(sib3Name);
        assertEquals(sources + 1, cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size());
        sources++;
        links = cms.getRelationsForResource(sib3Name, CmsRelationFilter.TARGETS);
        assertEquals(1, links.size());
        relation = new CmsRelation(sib3, target, CmsRelationType.EMBEDDED_IMAGE);
        assertRelation(relation, (CmsRelation)links.get(0));

        // remove the link
        TestLinkValidation.setContent(cms, sib1Name, "<h1>hello world!</h1>");
        assertEquals(sources - 3, cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size());
        sources -= 3;
        assertTrue(cms.getRelationsForResource(sib1Name, CmsRelationFilter.TARGETS).isEmpty());
        assertTrue(cms.getRelationsForResource(sib2Name, CmsRelationFilter.TARGETS).isEmpty());
        assertTrue(cms.getRelationsForResource(sib3Name, CmsRelationFilter.TARGETS).isEmpty());

        // add the link again
        TestLinkValidation.setContent(cms, sib1Name, "<img src='" + targetName + "'>");
        assertEquals(sources + 3, cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size());
        sources += 3;
        links = cms.getRelationsForResource(sib1Name, CmsRelationFilter.TARGETS);
        assertEquals(1, links.size());
        relation = new CmsRelation(sib1, target, CmsRelationType.EMBEDDED_IMAGE);
        assertRelation(relation, (CmsRelation)links.get(0));
        links = cms.getRelationsForResource(sib2Name, CmsRelationFilter.TARGETS);
        assertEquals(1, links.size());
        relation = new CmsRelation(sib2, target, CmsRelationType.EMBEDDED_IMAGE);
        assertRelation(relation, (CmsRelation)links.get(0));
        links = cms.getRelationsForResource(sib3Name, CmsRelationFilter.TARGETS);
        assertEquals(1, links.size());
        relation = new CmsRelation(sib3, target, CmsRelationType.EMBEDDED_IMAGE);
        assertRelation(relation, (CmsRelation)links.get(0));

        cms.deleteResource(sib3Name, CmsResource.DELETE_PRESERVE_SIBLINGS);
        assertEquals(sources - 1, cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size());
        sources--;
        cms.deleteResource(sib2Name, CmsResource.DELETE_PRESERVE_SIBLINGS);
        assertEquals(sources - 1, cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size());
        sources--;
        cms.deleteResource(sib1Name, CmsResource.DELETE_PRESERVE_SIBLINGS);
        assertEquals(sources - 1, cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size());
        sources--;
    }

    /**
     * Tests if setting the flags of a sibling will do any modifications to other siblings.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testSiblingStateIssue() throws Throwable {

        echo("Tests issue with resource state and siblings");
        CmsObject cms = getCmsObject();

        // part 1: changes to the fields that are part of the resource table
        String resource1 = "/folder1/page1.html";
        String sibling1 = "/folder1/sibling1.html";

        // create a sibling
        cms.copyResource(resource1, sibling1, CmsResource.COPY_AS_SIBLING);

        // verify the state of the resources
        assertState(cms, resource1, CmsResource.STATE_UNCHANGED);
        assertState(cms, sibling1, CmsResource.STATE_NEW);

        // now set the flags for the sibling
        cms.chflags(sibling1, 1024);
        cms.chtype(sibling1, CmsResourceTypeBinary.getStaticTypeId());

        // verify the state of the resources after the change
        assertState(cms, resource1, CmsResource.STATE_CHANGED);
        assertState(cms, sibling1, CmsResource.STATE_NEW);

        // part 2: now the same operation with a new copy
        String copy1 = "/folder1/copy1.html";
        sibling1 = "/folder1/siblingofcopy1.html";

        // create a copy
        cms.copyResource(resource1, copy1, CmsResource.COPY_AS_NEW);
        cms.copyResource(copy1, sibling1, CmsResource.COPY_AS_SIBLING);

        // verify the state of the resources
        assertState(cms, copy1, CmsResource.STATE_NEW);
        assertState(cms, sibling1, CmsResource.STATE_NEW);

        // now set the flags for the sibling
        cms.chflags(sibling1, 1024);
        cms.chtype(sibling1, CmsResourceTypeBinary.getStaticTypeId());

        // verify the state of the resources after the change
        assertState(cms, copy1, CmsResource.STATE_NEW);
        assertState(cms, sibling1, CmsResource.STATE_NEW);

        // part 3: changes to the fields that are part of the structure table
        resource1 = "/folder1/page2.html";
        sibling1 = "/folder1/sibling2.html";

        // create a sibling
        cms.copyResource(resource1, sibling1, CmsResource.COPY_AS_SIBLING);

        // verify the state of the resources
        assertState(cms, resource1, CmsResource.STATE_UNCHANGED);
        assertState(cms, sibling1, CmsResource.STATE_NEW);

        // after changes of dates the resource states must be the same
        cms.setDateExpired(sibling1, System.currentTimeMillis() + 1000, false);
        cms.setDateReleased(sibling1, System.currentTimeMillis() - 1000, false);

        // verify the state of the resources after the change
        assertState(cms, resource1, CmsResource.STATE_UNCHANGED);
        assertState(cms, sibling1, CmsResource.STATE_NEW);

        // step 4: changes to the fields that are part of the resource table
        cms.setDateLastModified(sibling1, System.currentTimeMillis(), false);

        // verify the state of the resources after the change
        assertState(cms, resource1, CmsResource.STATE_CHANGED);
        assertState(cms, sibling1, CmsResource.STATE_NEW);

        // part 5: now the same operation with a new copy
        copy1 = "/folder1/copy2.html";
        sibling1 = "/folder1/siblingofcopy2.html";

        // create a copy
        cms.copyResource(resource1, copy1, CmsResource.COPY_AS_NEW);
        cms.copyResource(copy1, sibling1, CmsResource.COPY_AS_SIBLING);

        // verify the state of the resources
        assertState(cms, copy1, CmsResource.STATE_NEW);
        assertState(cms, sibling1, CmsResource.STATE_NEW);

        // change date of last modification
        cms.setDateLastModified(sibling1, System.currentTimeMillis(), false);

        // verify the state of the resources after the change
        assertState(cms, copy1, CmsResource.STATE_NEW);
        assertState(cms, sibling1, CmsResource.STATE_NEW);

        // modify release info
        cms.setDateExpired(sibling1, System.currentTimeMillis() + 1000, false);
        cms.setDateReleased(sibling1, System.currentTimeMillis() - 1000, false);

        // verify the state of the resources after the change
        assertState(cms, copy1, CmsResource.STATE_NEW);
        assertState(cms, sibling1, CmsResource.STATE_NEW);
    }

    /**
     * Tests an issue present in OpenCms 7 where online content was not replaced after publish.<p>
     *
     * @throws Exception if the test fails
     */
    public void testSiblingsV7PublishIssue() throws Exception {

        echo("Tests OpenCms v7 publish issue with siblings");
        CmsObject cms = getCmsObject();

        CmsProject offlineProject = cms.getRequestContext().getCurrentProject();
        CmsProject onlineProject = cms.readProject(CmsProject.ONLINE_PROJECT_ID);

        // first we create a complete new folder as base for the test
        String folder = "/publish_v7issue/";
        cms.createResource(folder, CmsResourceTypeFolder.getStaticTypeId());

        String firstContent = "This is the first content";
        byte[] firstContentBytes = firstContent.getBytes();

        CmsProperty firstTitleProperty = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "The first title", null);
        List firstProperties = new ArrayList();
        firstProperties.add(firstTitleProperty);

        String source = folder + "test_en.txt";
        cms.createResource(source, CmsResourceTypePlain.getStaticTypeId(), firstContentBytes, firstProperties);

        assertState(cms, folder, CmsResourceState.STATE_NEW);
        assertState(cms, source, CmsResourceState.STATE_NEW);

        // publish the folder
        OpenCms.getPublishManager().publishResource(cms, folder);
        OpenCms.getPublishManager().waitWhileRunning();

        // check both online and offline project
        cms.getRequestContext().setCurrentProject(onlineProject);
        assertState(cms, folder, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, source, CmsResourceState.STATE_UNCHANGED);
        assertContent(cms, source, firstContentBytes);
        cms.getRequestContext().setCurrentProject(offlineProject);
        assertState(cms, folder, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, source, CmsResourceState.STATE_UNCHANGED);
        assertContent(cms, source, firstContentBytes);

        // create a new sibling using the "copy as" option
        String sibling = folder + "test_de.txt";
        copyResourceAsSibling(this, cms, source, sibling);
        assertState(cms, sibling, CmsResourceState.STATE_NEW);
        assertContent(cms, sibling, firstContentBytes);

        // change the content (this should affect both siblings)
        String secondContent = "++++++++ This is the SECOND content ++++++++++";
        byte[] secondContentBytes = secondContent.getBytes();
        CmsFile sourceFile = cms.readFile(source);
        sourceFile.setContents(secondContentBytes);
        cms.writeFile(sourceFile);

        assertState(cms, sibling, CmsResourceState.STATE_NEW);
        assertState(cms, source, CmsResourceState.STATE_CHANGED);
        assertContent(cms, sibling, secondContentBytes);
        assertContent(cms, source, secondContentBytes);

        // now change a property on the first sibling
        CmsProperty secondTitleProperty = new CmsProperty(
            CmsPropertyDefinition.PROPERTY_TITLE,
            "The SECOND title",
            null);
        cms.writePropertyObject(source, secondTitleProperty);

        // publish the folder again
        OpenCms.getPublishManager().publishResource(cms, folder);
        OpenCms.getPublishManager().waitWhileRunning();

        // get the content first for printing it to the console
        cms.getRequestContext().setCurrentProject(onlineProject);
        String contentOnline = new String(cms.readFile(source).getContents());
        cms.getRequestContext().setCurrentProject(offlineProject);
        String contentOffline = new String(cms.readFile(source).getContents());

        echo("Online Content:\n" + contentOnline);
        echo("Offline Content:\n" + contentOffline);

        // check both online and offline project
        cms.getRequestContext().setCurrentProject(offlineProject);
        assertState(cms, folder, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, source, CmsResourceState.STATE_UNCHANGED);
        assertContent(cms, sibling, secondContentBytes);
        assertContent(cms, source, secondContentBytes);
        cms.getRequestContext().setCurrentProject(onlineProject);
        assertState(cms, folder, CmsResourceState.STATE_UNCHANGED);
        assertState(cms, source, CmsResourceState.STATE_UNCHANGED);
        assertContent(cms, sibling, secondContentBytes);
        assertContent(cms, source, secondContentBytes);
    }
}
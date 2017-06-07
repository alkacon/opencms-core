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
import org.opencms.file.history.CmsHistoryFile;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestResourceConfigurableFilter;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for history operation.<p>
 *
 * @since 6.9.1
 */
public class TestHistory extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestHistory(String arg0) {

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
        suite.setName(TestHistory.class.getName());

        suite.addTest(new TestHistory("testFileRestore"));
        suite.addTest(new TestHistory("testReadDeleted"));
        suite.addTest(new TestHistory("testDeleteDate"));
        suite.addTest(new TestHistory("testFileRestoreIteration"));
        suite.addTest(new TestHistory("testSiblingsRestoration"));
        suite.addTest(new TestHistory("testSiblingsEdition"));
        suite.addTest(new TestHistory("testSiblingsV7HistoryIssue2"));
        suite.addTest(new TestHistory("testSiblingVersions"));
        suite.addTest(new TestHistory("testSiblingRestoreIteration"));
        suite.addTest(new TestHistory("testCreateAndDeleteFile"));
        suite.addTest(new TestHistory("testCreateAndDeleteFolder"));
        suite.addTest(new TestHistory("testMoveFile"));
        suite.addTest(new TestHistory("testPathHistory"));
        suite.addTest(new TestHistory("testFileHistory"));
        suite.addTest(new TestHistory("testFileHistoryFileWithSibling"));
        suite.addTest(new TestHistory("testFileVersions"));
        suite.addTest(new TestHistory("testVersioningLimit"));
        suite.addTest(new TestHistory("testSiblingsV7HistoryIssue"));

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
     * Creates and deletes a file n-times and tests if the historical data
     * are correct and if the content can be properly restored.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testCreateAndDeleteFile() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing history creating, modifying and deleting a file");

        String filename = "/dummy1.txt";
        int counter = 2;

        // create a plain text file
        CmsResource res = cms.createResource(
            filename,
            CmsResourceTypePlain.getStaticTypeId(),
            "content version 0".getBytes(),
            null);
        cms.unlockResource(filename);
        OpenCms.getPublishManager().publishResource(cms, filename);
        OpenCms.getPublishManager().waitWhileRunning();

        for (int i = 1; i <= counter; i++) {
            // create a plain text file
            String contentStr = "content version " + i;
            CmsFile file = cms.readFile(filename);
            file.setContents(contentStr.getBytes());
            cms.lockResource(filename);
            cms.writeFile(file);
            OpenCms.getPublishManager().publishResource(cms, filename);
            OpenCms.getPublishManager().waitWhileRunning();
        }
        CmsFile file = cms.readFile(filename);
        file.setContents(("content version " + (counter + 1)).getBytes());
        cms.lockResource(filename);
        cms.writeFile(file);
        // delete the file
        cms.deleteResource(filename, CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.unlockResource(filename);

        OpenCms.getPublishManager().publishResource(cms, filename);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.createResource(filename, CmsResourceTypePlain.getStaticTypeId(), null, null);
        // the history works only by ID so the expected number of versions is ZERO
        List historyResources = cms.readAllAvailableVersions(filename);
        assertTrue(historyResources.isEmpty());

        // re-create the resource to be able to read all versions from the history
        // assert that we have the expected number of versions in the history
        String importFile = "import.txt";
        cms.importResource(importFile, res, "blah-blah".getBytes(), null);
        historyResources = cms.readAllAvailableVersions(importFile);
        assertEquals(counter + 2, historyResources.size()); // counter + created + deleted

        for (int i = 0; i < (counter + 2); i++) {
            // the list of historical resources contains at index 0 the
            // resource with the highest version and tag ID
            int version = (counter + 2) - i;
            String contentStr = "content version " + (version - 1);

            // assert that the historical resource has the correct version
            I_CmsHistoryResource historyResource = (I_CmsHistoryResource)historyResources.get(i);
            assertEquals(version, historyResource.getVersion());

            cms.restoreResourceVersion(historyResource.getStructureId(), historyResource.getVersion());
            file = cms.readFile(importFile);

            // assert that the content and version fit together
            String restoredContent = getContentString(cms, file.getContents());
            assertEquals(contentStr, restoredContent);
        }
    }

    /**
     * Creates and deletes a folder n-times and tests if the historical data
     * are correct and if the properties can be properly restored.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testCreateAndDeleteFolder() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing history creating, modifying and deleting a folder");

        String folderName = "/dummy1/";
        int counter = 2;

        // create a folder
        CmsResource res = cms.createResource(folderName, CmsResourceTypeFolder.getStaticTypeId(), null, null);
        cms.writePropertyObject(
            folderName,
            new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "title version \u00E4\u00F6\u00DF\u20AC 0", null));
        cms.unlockResource(folderName);
        OpenCms.getPublishManager().publishResource(cms, folderName);
        OpenCms.getPublishManager().waitWhileRunning();
        // check
        assertHistory(cms, folderName, 1);

        for (int i = 1; i <= counter; i++) {
            cms.lockResource(folderName);
            cms.writePropertyObject(
                folderName,
                new CmsProperty(
                    CmsPropertyDefinition.PROPERTY_TITLE,
                    "title version \u00E4\u00F6\u00DF\u20AC " + i,
                    null));
            // check
            assertHistory(cms, folderName, 1 + i);
            OpenCms.getPublishManager().publishResource(cms, folderName);
            OpenCms.getPublishManager().waitWhileRunning();
            // check
            assertHistory(cms, folderName, 1 + i);
        }
        cms.lockResource(folderName);
        cms.writePropertyObject(
            folderName,
            new CmsProperty(
                CmsPropertyDefinition.PROPERTY_TITLE,
                "title version \u00E4\u00F6\u00DF\u20AC " + (counter + 1),
                null));
        // delete the folder
        cms.deleteResource(folderName, CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.unlockResource(folderName);
        OpenCms.getPublishManager().publishResource(cms, folderName);
        OpenCms.getPublishManager().waitWhileRunning();

        // the history works only by ID so the expected number of versions is ZERO
        cms.createResource(folderName, CmsResourceTypeFolder.getStaticTypeId(), null, null);
        List historyResources = cms.readAllAvailableVersions(folderName);
        assertTrue(historyResources.isEmpty());

        // re-create the folder to be able to read all versions from the history
        // assert that we have the expected number of versions in the history
        String importFolder = "import/";
        cms.importResource(importFolder, res, null, null);
        historyResources = cms.readAllAvailableVersions(importFolder);
        assertEquals(counter + 2, historyResources.size()); // counter + created + deleted

        for (int i = 0; i < (counter + 2); i++) {
            // the list of historical resources contains at index 0 the
            // folder with the highest version and tag ID
            int version = (counter + 2) - i;
            String title = "title version \u00E4\u00F6\u00DF\u20AC " + (version - 1);

            // assert that the historical resource has the correct version
            I_CmsHistoryResource historyResource = (I_CmsHistoryResource)historyResources.get(i);
            assertEquals(version, historyResource.getVersion());

            cms.restoreResourceVersion(historyResource.getStructureId(), historyResource.getVersion());
            CmsProperty property = cms.readPropertyObject(
                cms.readResource(importFolder),
                CmsPropertyDefinition.PROPERTY_TITLE,
                false);

            // assert that the title and version fit together
            assertEquals(title, property.getStructureValue());
        }
    }

    /**
     * Tests the delete date of deleted resources.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testDeleteDate() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing the delete date of deleted resources");

        String folderName = "/folderReadDeletedDate";
        String fileName = "/test.txt";

        cms.createResource(folderName, CmsResourceTypeFolder.getStaticTypeId());
        cms.createResource(folderName + fileName, CmsResourceTypePlain.getStaticTypeId(), "hallo".getBytes(), null);
        OpenCms.getPublishManager().publishResource(cms, folderName);
        OpenCms.getPublishManager().waitWhileRunning();

        long time = cms.readResource(folderName + fileName).getDateLastModified();
        cms.lockResource(folderName + fileName);
        cms.deleteResource(folderName + fileName, CmsResource.DELETE_REMOVE_SIBLINGS);
        echo("time: " + time);
        echo("dlm: " + cms.readResource(folderName + fileName, CmsResourceFilter.ALL).getDateLastModified());

        OpenCms.getPublishManager().publishResource(cms, folderName + fileName);
        OpenCms.getPublishManager().waitWhileRunning();

        // get the deleted resources in the new folder
        List deleted = cms.readDeletedResources(folderName, false);
        assertEquals(1, deleted.size());
        echo("dlm2: " + ((I_CmsHistoryResource)deleted.get(0)).getDateLastModified());
        if (((I_CmsHistoryResource)deleted.get(0)).getDateLastModified() < time) {
            fail("the date last modified after deletion is wrong.");
        }
    }

    /**
     * Creates a file, modifies and publishes it n-times, create a sibling,
     * publishes both and compares the histories.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testFileHistory() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing history with one sibling");

        String filename = "/testFileHistory1.txt";
        String siblingname = "/testFileHistory2.txt";
        int counter = 2;

        // create a plain text file
        String contentStr = "content version " + 0;
        cms.createResource(filename, CmsResourceTypePlain.getStaticTypeId(), contentStr.getBytes(), null);
        OpenCms.getPublishManager().publishResource(cms, filename);
        OpenCms.getPublishManager().waitWhileRunning();

        for (int i = 1; i <= counter; i++) {
            // modify the plain text file
            contentStr = "content version " + i;
            CmsFile file = cms.readFile(filename);
            file.setContents(contentStr.getBytes());
            cms.lockResource(filename);
            cms.writeFile(file);
            cms.unlockResource(filename);
            OpenCms.getPublishManager().publishResource(cms, filename);
            OpenCms.getPublishManager().waitWhileRunning();
        }

        // create a sibling
        cms.copyResource(filename, siblingname, CmsResource.COPY_AS_SIBLING);
        cms.unlockResource(siblingname);
        OpenCms.getPublishManager().publishResource(cms, siblingname);
        OpenCms.getPublishManager().waitWhileRunning();

        for (int i = 1; i <= counter; i++) {
            // modify the sibling text file
            contentStr = "sibling content version " + (counter + i);
            CmsFile file = cms.readFile(siblingname);
            file.setContents(contentStr.getBytes());
            cms.lockResource(siblingname);
            cms.writeFile(file);
            cms.unlockResource(siblingname);
            OpenCms.getPublishManager().publishResource(cms, siblingname);
            OpenCms.getPublishManager().waitWhileRunning();
        }

        List historyResourcesForFile = cms.readAllAvailableVersions(filename);
        List historyResourcesForSibling = cms.readAllAvailableVersions(siblingname);

        // 1 creation
        // counter modifications of original file
        // 0 sibling creation, does not affects the history for the original file
        // counter modifications of sibling
        assertEquals(1 + counter + 0 + counter, historyResourcesForFile.size());
        // 1 creation
        // counter modifications of original file
        // 1 sibling creation
        // counter modifications of sibling
        assertEquals(1 + counter + 1 + counter, historyResourcesForSibling.size());
    }

    /**
     * creates a file, modifies and publishes it n-times, create 2 siblings,
     * delete file and sibling N2, delete some versions from history, restore
     * file and sibling from history and compare contents.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testFileHistoryFileWithSibling() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing history with several siblings");

        String filename = "/testFileRoot.txt";
        String siblingname = "/testFileSibling1.txt";
        String siblingname2 = "/testFileSibling2.txt";

        int counter = 3;
        int counterSibl = 4;
        int counterSibl2 = 5;

        // create a plain text file
        String contentStr = "content version " + 0;
        cms.createResource(filename, CmsResourceTypePlain.getStaticTypeId(), contentStr.getBytes(), null);

        for (int i = 1; i <= counter; i++) {
            // modify the plain text file
            contentStr = "content version " + i;
            CmsFile file = cms.readFile(filename);
            file.setContents(contentStr.getBytes());
            cms.lockResource(filename);
            cms.writeFile(file);
            CmsProperty property = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "RootTitle" + i, null);
            cms.writePropertyObject(filename, property);
            cms.unlockResource(filename);
            OpenCms.getPublishManager().publishResource(cms, filename);
            OpenCms.getPublishManager().waitWhileRunning();
        }

        // create a sibling
        cms.copyResource(filename, siblingname, CmsResource.COPY_AS_SIBLING);
        cms.unlockResource(siblingname);
        OpenCms.getPublishManager().publishResource(cms, siblingname);
        OpenCms.getPublishManager().waitWhileRunning();

        for (int i = 1; i <= counterSibl; i++) {
            // modify the sibling text file
            contentStr = "sibling content version " + (counter + i);
            CmsFile file = cms.readFile(siblingname);
            file.setContents(contentStr.getBytes());
            cms.lockResource(siblingname);
            cms.writeFile(file);
            CmsProperty property = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "SiblingTitle" + i, null);
            cms.writePropertyObject(siblingname, property);
            cms.unlockResource(siblingname);
            OpenCms.getPublishManager().publishResource(cms, siblingname);
            OpenCms.getPublishManager().waitWhileRunning();
        }

        // create a sibling2
        cms.copyResource(filename, siblingname2, CmsResource.COPY_AS_SIBLING);
        cms.unlockResource(siblingname2);
        OpenCms.getPublishManager().publishResource(cms, siblingname2);
        OpenCms.getPublishManager().waitWhileRunning();

        for (int i = 1; i <= counterSibl2; i++) {
            // modify the sibling text file
            contentStr = "sibling2 content version " + (counter + counterSibl2 + i);
            CmsFile file = cms.readFile(siblingname2);
            file.setContents(contentStr.getBytes());
            cms.lockResource(siblingname2);
            cms.writeFile(file);
            CmsProperty property = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "Sibling2Title" + i, null);
            cms.writePropertyObject(siblingname2, property);
            cms.unlockResource(siblingname2);
            OpenCms.getPublishManager().publishResource(cms, siblingname2);
            OpenCms.getPublishManager().waitWhileRunning();
        }

        // now delete and publish the root
        cms.lockResource(filename);
        cms.deleteResource(filename, CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.unlockResource(filename);
        OpenCms.getPublishManager().publishResource(cms, filename);
        OpenCms.getPublishManager().waitWhileRunning();

        // now delete and publish second sibling
        cms.lockResource(siblingname2);
        cms.deleteResource(siblingname2, CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.unlockResource(siblingname2);
        OpenCms.getPublishManager().publishResource(cms, siblingname2);
        OpenCms.getPublishManager().waitWhileRunning();

        List allFiles = cms.readAllAvailableVersions(siblingname);
        // 0 creation is not published
        // 'counter' modifications to the original file
        // '1' first sibling creation
        // 'counterSibl' first sibling modifications
        // '0' second sibling creation, affects just the given sibling
        // 'counterSibl2' second sibling modifications
        // '0' original file deletion, affects just the given sibling
        // '0' second sibling deletion, affects just the given sibling
        // -3 versions deleted due to history overflow, while deleting original file
        assertEquals((0 + counter + 1 + counterSibl + 0 + counterSibl2 + 0 + 0) - 3, allFiles.size());

        //Delete historical entries, keep only 3 latest versions.
        cms.deleteHistoricalVersions(3, 3, -1, new CmsShellReport(cms.getRequestContext().getLocale()));

        allFiles = cms.readAllAvailableVersions(siblingname);
        // 3 the number of versions that should remain for the sibling
        // 3 the number of versions that should remain for the second sibling
        // 2 additional remaining versions of the original file (one overlap)
        assertEquals(3 + counterSibl2, allFiles.size()); // it is not 3 since there are more versions coming from the siblings!

        I_CmsHistoryResource history = (I_CmsHistoryResource)allFiles.get(1);
        cms.lockResource(siblingname);
        //and restore it from history
        cms.restoreResourceVersion(history.getStructureId(), history.getVersion());
        cms.unlockResource(siblingname);
        CmsFile file = cms.readFile(siblingname);

        // assert that the content and version fit together
        String restoredContent = getContentString(cms, file.getContents());

        // the content is coming from a sibling2 modification
        assertEquals("sibling2 content version 12", restoredContent);
        CmsProperty prop = cms.readPropertyObject(siblingname, CmsPropertyDefinition.PROPERTY_TITLE, false);
        // the property is coming from the sibling's last set title
        assertEquals("SiblingTitle4", prop.getValue());

        // create a new empty resource
        cms.createResource(siblingname2, CmsResourceTypePlain.getStaticTypeId(), null, null);

        allFiles = cms.readAllAvailableVersions(siblingname2);
        assertTrue(allFiles.isEmpty());
    }

    /**
     * Test restoring a file also possible missing folders are restored.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testFileRestore() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing restoring a file also possible missing folders are restored");

        // initial check
        List deletedResources = cms.readDeletedResources("/", true);
        assertTrue(deletedResources.isEmpty());

        // create a new folder and resource
        CmsResource folder = cms.createResource("testFolder", CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        // create a relation
        cms.lockResource("index.html");
        cms.addRelationToResource("index.html", "testFolder", CmsRelationType.CATEGORY.getName());
        // write props
        cms.writePropertyObject(
            "testFolder",
            new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "strFolder", "resFolder"));
        CmsResource res = cms.createResource(
            "testFolder/test.txt",
            CmsResourceTypePlain.getStaticTypeId(),
            "test".getBytes(),
            null);
        // create a relation
        cms.addRelationToResource("index.html", "testFolder/test.txt", CmsRelationType.CATEGORY.getName());
        // write props
        cms.writePropertyObject(
            "testFolder/test.txt",
            new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "strFile", "resFile"));

        storeResources(cms, "testFolder", true);

        // publish
        OpenCms.getPublishManager().publishResource(cms, "testFolder");
        OpenCms.getPublishManager().waitWhileRunning();

        // delete the folder and file
        cms.lockResource("testFolder");
        cms.deleteResource("testFolder/test.txt", CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.deleteResource("testFolder", CmsResource.DELETE_PRESERVE_SIBLINGS);

        // publish again
        OpenCms.getPublishManager().publishResource(cms, "testFolder");
        OpenCms.getPublishManager().waitWhileRunning();

        // be sure the files were deleted, this could fail with enabled cache
        assertFalse(cms.existsResource("testFolder", CmsResourceFilter.ALL));
        assertFalse(cms.existsResource("testFolder/test.txt", CmsResourceFilter.ALL));

        // be sure the files were deleted
        CmsProject offline = cms.getRequestContext().getCurrentProject();
        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
        assertFalse(cms.existsResource("testFolder", CmsResourceFilter.ALL));
        assertFalse(cms.existsResource("testFolder/test.txt", CmsResourceFilter.ALL));
        cms.getRequestContext().setCurrentProject(offline);

        // check the deleted resources
        deletedResources = cms.readDeletedResources("/", false);
        assertEquals(1, deletedResources.size());
        assertEquals("/testFolder/", cms.getSitePath((CmsResource)deletedResources.get(0)));

        deletedResources = cms.readDeletedResources("/", true);
        assertEquals(2, deletedResources.size());
        assertEquals("/testFolder/", cms.getSitePath((CmsResource)deletedResources.get(0)));
        assertEquals("/testFolder/test.txt", cms.getSitePath((CmsResource)deletedResources.get(1)));

        // restore the deleted file
        cms.restoreDeletedResource(res.getStructureId());

        // check the deleted resources
        deletedResources = cms.readDeletedResources("/", true);
        assertTrue(deletedResources.isEmpty());

        // assert the resources
        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();
        filter.enableDateCreatedSecTest();
        filter.disableDateContentTest();
        assertFilter(cms, "testFolder/", filter);
        assertFilter(cms, cms.getSitePath(res), filter);

        // assert the relations
        List relations = cms.getRelationsForResource("/testFolder", CmsRelationFilter.SOURCES);
        assertEquals(1, relations.size());
        assertRelation(
            new CmsRelation(cms.readResource("index.html"), folder, CmsRelationType.CATEGORY),
            (CmsRelation)relations.get(0));
        relations = cms.getRelationsForResource("/testFolder/test.txt", CmsRelationFilter.SOURCES);
        assertEquals(1, relations.size());
        assertRelation(
            new CmsRelation(cms.readResource("index.html"), res, CmsRelationType.CATEGORY),
            (CmsRelation)relations.get(0));

        // delete again
        cms.lockResource("testFolder");
        cms.deleteResource("testFolder/test.txt", CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.deleteResource("testFolder", CmsResource.DELETE_PRESERVE_SIBLINGS);

        // create new ones
        cms.createResource("testFolder", CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.createResource("testFolder/test.txt", CmsResourceTypePlain.getStaticTypeId(), "test".getBytes(), null);

        // check the deleted resources
        deletedResources = cms.readDeletedResources("/", false);
        assertEquals(1, deletedResources.size());
        assertEquals("/testFolder/", cms.getSitePath((CmsResource)deletedResources.get(0)));

        deletedResources = cms.readDeletedResources("/", true);
        assertEquals(2, deletedResources.size());
        assertEquals("/testFolder/", cms.getSitePath((CmsResource)deletedResources.get(0)));
        assertEquals("/testFolder/test.txt", cms.getSitePath((CmsResource)deletedResources.get(1)));

        // restore the deleted file
        cms.restoreDeletedResource(res.getStructureId());

        // assert again, checking for the new name!
        assertFalse(cms.readFolder("testFolder").getStructureId().equals(folder.getStructureId()));
        setMapping("/testFolder/test_1.txt", "/testFolder/test.txt");
        filter.disableNameTest();
        assertFilter(cms, "/testFolder/test_1.txt", filter);

        // restore the deleted folder
        cms.restoreDeletedResource(folder.getStructureId());
        setMapping("/testFolder_1/", "testFolder/");
        assertFilter(cms, "/testFolder_1/", filter);
    }

    /**
     * Test restoring and deleting a file several times.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testFileRestoreIteration() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing restoring and deleting a file several times");

        String resName = "fileRestoreIteration.txt";

        int vers = OpenCms.getSystemInfo().getHistoryVersions();
        int delVers = OpenCms.getSystemInfo().getHistoryVersionsAfterDeletion();
        try {
            OpenCms.getSystemInfo().setVersionHistorySettings(true, 10, 10);

            // create new resource
            CmsResource res = cms.createResource(resName, CmsResourceTypePlain.getStaticTypeId());
            // check offline
            assertHistory(cms, resName, 1);

            // publish
            OpenCms.getPublishManager().publishResource(cms, resName);
            OpenCms.getPublishManager().waitWhileRunning();
            // check after publish
            assertHistory(cms, resName, 1);

            for (int i = 0; i < 3; i++) {
                cms.lockResource(resName);
                cms.deleteResource(resName, CmsResource.DELETE_PRESERVE_SIBLINGS);
                // check offline
                assertHistory(cms, resName, 2 + (i * 2));
                // publish
                OpenCms.getPublishManager().publishResource(cms, resName);
                OpenCms.getPublishManager().waitWhileRunning();
                // restore
                cms.restoreDeletedResource(res.getStructureId());
                // check offline
                assertHistoryForRestored(cms, resName, 3 + (i * 2));
                // publish
                OpenCms.getPublishManager().publishResource(cms, resName);
                OpenCms.getPublishManager().waitWhileRunning();
                // check after publish
                assertHistory(cms, resName, 3 + (i * 2));
            }
        } finally {
            OpenCms.getSystemInfo().setVersionHistorySettings(true, vers, delVers);
        }
    }

    /**
     * Test the version numbers of a file after several modifications.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testFileVersions() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing the version numbers of a file after several modifications");

        String resName = "fileVersionTest.txt";

        CmsProject offline = cms.getRequestContext().getCurrentProject();
        CmsProject online = cms.readProject(CmsProject.ONLINE_PROJECT_ID);

        // 1. create new resource
        cms.createResource(resName, CmsResourceTypePlain.getStaticTypeId());
        // check offline
        assertHistory(cms, resName, 1);

        // publish
        OpenCms.getPublishManager().publishResource(cms, resName);
        OpenCms.getPublishManager().waitWhileRunning();

        // check after publish
        cms.getRequestContext().setCurrentProject(online);
        assertHistory(cms, resName, 1);

        // 2. modify resource
        cms.getRequestContext().setCurrentProject(offline);
        cms.lockResource(resName);
        cms.setDateLastModified(resName, System.currentTimeMillis(), false);

        // check offline
        assertHistory(cms, resName, 2);

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertHistory(cms, resName, 1);

        // publish
        cms.getRequestContext().setCurrentProject(offline);
        OpenCms.getPublishManager().publishResource(cms, resName);
        OpenCms.getPublishManager().waitWhileRunning();

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertHistory(cms, resName, 2);

        // check offline
        cms.getRequestContext().setCurrentProject(offline);
        assertHistory(cms, resName, 2);

        // 3. modify structure
        cms.lockResource(resName);
        cms.writePropertyObject(resName, new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "test", null));

        // check offline
        assertHistory(cms, resName, 3);

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertHistory(cms, resName, 2);

        // publish
        cms.getRequestContext().setCurrentProject(offline);
        assertHistory(cms, resName, 3);
        OpenCms.getPublishManager().publishResource(cms, resName);
        OpenCms.getPublishManager().waitWhileRunning();

        // check offline
        assertHistory(cms, resName, 3);

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertHistory(cms, resName, 3);

        String sibName = "fileVersionTest_sib.txt";

        // 4. create sibling
        cms.getRequestContext().setCurrentProject(offline);
        cms.createSibling(resName, sibName, null);

        // check offline
        assertHistory(cms, resName, 3);
        assertHistory(cms, sibName, 3);

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertHistory(cms, resName, 3);

        // publish sibling
        cms.getRequestContext().setCurrentProject(offline);
        OpenCms.getPublishManager().publishResource(cms, sibName);
        OpenCms.getPublishManager().waitWhileRunning();

        // check offline
        assertHistory(cms, resName, 3);
        assertHistory(cms, sibName, 3);

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertHistory(cms, resName, 3);
        assertHistory(cms, sibName, 3);

        // 5. modify sibling resource
        cms.getRequestContext().setCurrentProject(offline);
        cms.lockResource(sibName);
        cms.writePropertyObject(
            sibName,
            new CmsProperty(CmsPropertyDefinition.PROPERTY_DESCRIPTION, null, "test description"));

        // check offline
        assertHistory(cms, resName, 4);
        assertHistory(cms, sibName, 4);

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertHistory(cms, resName, 3);
        assertHistory(cms, sibName, 3);

        // publish
        cms.getRequestContext().setCurrentProject(offline);
        OpenCms.getPublishManager().publishResource(cms, sibName);
        OpenCms.getPublishManager().waitWhileRunning();

        // check offline
        assertHistory(cms, resName, 4);
        assertHistory(cms, sibName, 4);

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertHistory(cms, resName, 4);
        assertHistory(cms, sibName, 4);

        // 6. modify structure
        cms.getRequestContext().setCurrentProject(offline);
        cms.lockResource(resName);
        cms.setDateExpired(resName, System.currentTimeMillis() + 100000, false);

        // check offline
        assertHistory(cms, resName, 5);
        assertHistory(cms, sibName, 4);

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertHistory(cms, resName, 4);
        assertHistory(cms, sibName, 4);

        // publish
        cms.getRequestContext().setCurrentProject(offline);
        OpenCms.getPublishManager().publishResource(cms, resName);
        OpenCms.getPublishManager().waitWhileRunning();

        // check offline
        assertHistory(cms, resName, 5);
        assertHistory(cms, sibName, 4);

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertHistory(cms, resName, 5);
        assertHistory(cms, sibName, 4);

        // 7. modify resource
        cms.getRequestContext().setCurrentProject(offline);
        cms.lockResource(resName);
        cms.writePropertyObject(resName, new CmsProperty(CmsPropertyDefinition.PROPERTY_NAVPOS, null, "1.5"));

        // check offline
        assertHistory(cms, resName, 6);
        assertHistory(cms, sibName, 5);

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertHistory(cms, resName, 5);
        assertHistory(cms, sibName, 4);

        // publish
        cms.getRequestContext().setCurrentProject(offline);
        OpenCms.getPublishManager().publishResource(cms, resName);
        OpenCms.getPublishManager().waitWhileRunning();

        // check offline
        assertHistory(cms, resName, 6);
        assertHistory(cms, sibName, 5);

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertHistory(cms, resName, 6);
        assertHistory(cms, sibName, 5);

        // 8. modify sibling structure
        cms.getRequestContext().setCurrentProject(offline);
        cms.lockResource(sibName);
        cms.setDateReleased(sibName, System.currentTimeMillis() - 1000, false);

        // check offline
        assertHistory(cms, resName, 6);
        assertHistory(cms, sibName, 6);

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertHistory(cms, resName, 6);
        assertHistory(cms, sibName, 5);

        // publish
        cms.getRequestContext().setCurrentProject(offline);
        OpenCms.getPublishManager().publishResource(cms, sibName);
        OpenCms.getPublishManager().waitWhileRunning();

        // check offline
        assertHistory(cms, resName, 6);
        assertHistory(cms, sibName, 6);

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertHistory(cms, resName, 6);
        assertHistory(cms, sibName, 6);

        /*

         restore res version 2
         assert res version == 6 (res = 4, str = 2)
         assert sib version == 6 (res = 4, sib = 2)

         move

         */
    }

    /**
     * Moves a resource n-times and tests if the version ID of the history resources
     * are correct and if the content could be restored for a specified version ID.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testMoveFile() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing history after moving a resource n-times");

        String filename = "/dummyMove";
        String ext = ".txt";
        String resName = filename + 0 + ext;
        int counter = 2;

        // create a plain text file
        CmsResource res = cms.createResource(
            resName,
            CmsResourceTypePlain.getStaticTypeId(),
            "content version 0".getBytes(),
            null);
        cms.unlockResource(resName);
        OpenCms.getPublishManager().publishResource(cms, resName);
        OpenCms.getPublishManager().waitWhileRunning();

        for (int i = 1; i <= counter; i++) {
            // create a plain text file
            String contentStr = "content version " + i;
            CmsFile file = cms.readFile(resName);
            file.setContents(contentStr.getBytes());
            cms.lockResource(resName);
            cms.writeFile(file);
            String newName = filename + i + ext;
            cms.moveResource(resName, newName);
            OpenCms.getPublishManager().publishResource(cms, newName);
            OpenCms.getPublishManager().waitWhileRunning();
            resName = newName;
        }
        CmsFile file = cms.readFile(resName);
        file.setContents("content version 3".getBytes());
        cms.lockResource(resName);
        cms.writeFile(file);
        // delete the resource again
        cms.deleteResource(resName, CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.unlockResource(resName);
        OpenCms.getPublishManager().publishResource(cms, resName);
        OpenCms.getPublishManager().waitWhileRunning();

        // the history works only by ID so the expected number of versions is ZERO
        cms.createResource(resName, CmsResourceTypePlain.getStaticTypeId(), null, null);
        List historyResources = cms.readAllAvailableVersions(resName);
        assertTrue(historyResources.isEmpty());

        // re-create the resource to be able to read all versions from the history
        // assert that we have the expected number of versions in the history
        String importFile = "import.txt";
        cms.importResource(importFile, res, "blah-blah".getBytes(), null);
        historyResources = cms.readAllAvailableVersions(importFile);
        assertEquals(counter + 2, historyResources.size()); // counter + created + deleted

        for (int i = 0; i < (counter + 2); i++) {
            // the list of historical resources contains at index 0 the
            // resource with the highest version and tag ID
            int version = (counter + 2) - i;
            String contentStr = "content version " + (version - 1);

            // assert that the historical resource has the correct version
            I_CmsHistoryResource historyResource = (I_CmsHistoryResource)historyResources.get(i);
            assertEquals(version, historyResource.getVersion());

            cms.restoreResourceVersion(historyResource.getStructureId(), historyResource.getVersion());
            file = cms.readFile(importFile);

            // assert that the content and version fit together
            String restoredContent = getContentString(cms, file.getContents());
            assertEquals(contentStr, restoredContent);
        }
    }

    /**
     * Tests reconstructing the resource path for the history.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testPathHistory() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing the reconstruction of the resource path for the history");

        String foldername = "/folderMove";
        String filename = "/dummyMove";
        String ext = ".txt";
        String path = foldername + 0;
        String resName = path + filename + 0 + ext;
        int counter = 2;

        // create folder
        cms.createResource(path, CmsResourceTypeFolder.getStaticTypeId());
        cms.unlockResource(path);

        // create a plain text file
        CmsResource res = cms.createResource(
            resName,
            CmsResourceTypePlain.getStaticTypeId(),
            "content version 0".getBytes(),
            null);
        cms.unlockResource(resName);
        OpenCms.getPublishManager().publishResource(cms, path);
        OpenCms.getPublishManager().waitWhileRunning();

        for (int i = 1; i <= counter; i++) {
            // modify the file
            String contentStr = "content version " + i;
            CmsFile file = cms.readFile(resName);
            file.setContents(contentStr.getBytes());
            cms.lockResource(resName);
            cms.writeFile(file);
            // move the folder
            String newPath = foldername + i;
            cms.lockResource(path);
            cms.moveResource(path, newPath);
            // move the file
            String newName = newPath + filename + i + ext;
            cms.moveResource(newPath + filename + (i - 1) + ext, newName);
            // publish
            OpenCms.getPublishManager().publishResource(cms, newPath);
            OpenCms.getPublishManager().waitWhileRunning();
            resName = newName;
            path = newPath;
        }
        // modify the file
        CmsFile file = cms.readFile(resName);
        file.setContents(("content version " + (counter + 1)).getBytes());
        cms.lockResource(resName);
        cms.writeFile(file);
        // move the folder
        String newPath = foldername + (counter + 1);
        cms.lockResource(path);
        cms.moveResource(path, newPath);
        // move the file
        String newName = newPath + filename + (counter + 1) + ext;
        cms.moveResource(newPath + filename + counter + ext, newName);

        CmsFolder folder = cms.readFolder(newPath);
        // delete the folder
        cms.deleteResource(newPath, CmsResource.DELETE_PRESERVE_SIBLINGS);
        // publish
        OpenCms.getPublishManager().publishResource(cms, newPath);
        OpenCms.getPublishManager().waitWhileRunning();

        resName = filename + ext;
        // the history works only by ID so the expected number of versions is ZERO
        cms.createResource(resName, CmsResourceTypePlain.getStaticTypeId(), null, null);
        List historyResources = cms.readAllAvailableVersions(resName);
        assertTrue(historyResources.isEmpty());

        // re-create the resource to be able to read all versions from the history
        // assert that we have the expected number of versions in the history
        String importFile = "import2.txt";
        cms.importResource(importFile, res, "blah-blah".getBytes(), null);
        historyResources = cms.readAllAvailableVersions(importFile);
        assertEquals(counter + 2, historyResources.size()); // 1 (created) + counter (modified/moved) + 1 (deleted)

        for (int i = 0; i < (counter + 2); i++) {
            // the list of historical resources contains at index 0 the
            // resource with the highest version and tag ID
            int version = (counter + 2) - i;
            String contentStr = "content version " + (version - 1);
            String histResName = foldername + (version - 1) + filename + (version - 1) + ext;

            // assert that the historical resource has the correct version and path
            I_CmsHistoryResource historyResource = (I_CmsHistoryResource)historyResources.get(i);
            assertEquals(version, historyResource.getVersion());
            assertEquals(histResName, cms.getSitePath((CmsResource)historyResource));

            // restore the version
            cms.restoreResourceVersion(historyResource.getStructureId(), historyResource.getVersion());
            file = cms.readFile(importFile);

            // assert that the content and version fit together
            String restoredContent = getContentString(cms, file.getContents());
            assertEquals(contentStr, restoredContent);
        }

        // now recreate the folder
        String importFolder = "/import";
        cms.importResource(importFolder, folder, null, null);
        historyResources = cms.readAllAvailableVersions(importFile);
        assertEquals(counter + 2, historyResources.size()); // 1 (created) + counter (modified/moved) + 1 (deleted)

        cms.lockResource(importFile);

        for (int i = 0; i < (counter + 2); i++) {
            // the list of historical resources contains at index 0 the
            // resource with the highest version and tag ID
            int version = (counter + 2) - i;
            String contentStr = "content version " + (version - 1);
            String histResName = foldername + (version - 1) + filename + (version - 1) + ext;

            // assert that the historical resource has the correct version and path
            I_CmsHistoryResource historyResource = (I_CmsHistoryResource)historyResources.get(i);
            assertEquals(version, historyResource.getVersion());
            assertEquals(histResName, cms.getSitePath((CmsResource)historyResource));

            // restore the version
            cms.restoreResourceVersion(historyResource.getStructureId(), historyResource.getVersion());
            file = cms.readFile(importFile);

            // assert that the content and version fit together
            String restoredContent = getContentString(cms, file.getContents());
            assertEquals(contentStr, restoredContent);
        }
    }

    /**
     * Tests the retrieval of deleted resources by name in a folder.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testReadDeleted() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing the retrieval of deleted resources by name in a folder");

        String folderName = "/folderReadDeleted";
        String fileName = "/test.txt";

        cms.createResource(folderName, CmsResourceTypeFolder.getStaticTypeId());
        cms.createResource(
            folderName + fileName,
            CmsResourceTypePlain.getStaticTypeId(),
            "test content".getBytes(),
            null);

        OpenCms.getPublishManager().publishResource(cms, folderName);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.lockResource(folderName);
        cms.deleteResource(folderName, CmsResource.DELETE_PRESERVE_SIBLINGS);

        OpenCms.getPublishManager().publishResource(cms, folderName);
        OpenCms.getPublishManager().waitWhileRunning();

        // create with new id
        cms.createResource(folderName, CmsResourceTypeFolder.getStaticTypeId());
        // get the deleted resources in the new folder
        List deleted = cms.readDeletedResources(folderName, false);
        assertEquals(1, deleted.size());

        cms.restoreDeletedResource(((I_CmsHistoryResource)deleted.get(0)).getStructureId());
        cms.readResource(((I_CmsHistoryResource)deleted.get(0)).getStructureId());
        cms.readResource(cms.getRequestContext().removeSiteRoot(((I_CmsHistoryResource)deleted.get(0)).getRootPath()));
    }

    /**
     * Test restoring and deleting a sibling several times.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testSiblingRestoreIteration() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing restoring and deleting a sibling several times");

        String resName = "siblingRestoreIteration.txt";
        String sibName = "sibling2RestoreIteration.txt";

        int vers = OpenCms.getSystemInfo().getHistoryVersions();
        int delVers = OpenCms.getSystemInfo().getHistoryVersionsAfterDeletion();
        try {
            OpenCms.getSystemInfo().setVersionHistorySettings(true, 10, 10);

            // create new resource
            cms.createResource(resName, CmsResourceTypePlain.getStaticTypeId());
            // check offline
            assertHistory(cms, resName, 1);

            // publish
            OpenCms.getPublishManager().publishResource(cms, resName);
            OpenCms.getPublishManager().waitWhileRunning();
            // check after publish
            assertHistory(cms, resName, 1);

            // create sibling
            cms.copyResource(resName, sibName, CmsResource.COPY_AS_SIBLING);
            CmsResource sib = cms.readResource(sibName);
            // check offline
            assertHistory(cms, resName, 1);
            // new siblings have no history until they get first published
            assertVersion(cms, sibName, 2);

            // publish
            OpenCms.getPublishManager().publishResource(cms, sibName);
            OpenCms.getPublishManager().waitWhileRunning();
            // check after publish
            assertHistory(cms, resName, 1);
            // after publishing the sibling gets the history of the resource
            assertHistory(cms, sibName, 2);

            for (int i = 0; i < 3; i++) {
                cms.lockResource(sibName);
                cms.deleteResource(sibName, CmsResource.DELETE_PRESERVE_SIBLINGS);
                // check offline
                assertHistory(cms, resName, 1 + i);
                assertHistory(cms, sibName, 3 + (i * 2));
                // publish
                OpenCms.getPublishManager().publishResource(cms, sibName);
                OpenCms.getPublishManager().waitWhileRunning();
                // check after publish
                assertHistory(cms, resName, 1 + i);
                // restore
                cms.restoreDeletedResource(sib.getStructureId());
                // check offline
                assertVersion(cms, resName, 2 + i);
                assertHistoryForRestored(cms, sibName, 4 + (i * 2));
                // publish
                OpenCms.getPublishManager().publishResource(cms, sibName);
                OpenCms.getPublishManager().waitWhileRunning();
                // check after publish
                assertVersion(cms, resName, 2 + i);
                assertHistory(cms, sibName, 4 + (i * 2));
            }
        } finally {
            OpenCms.getSystemInfo().setVersionHistorySettings(true, vers, delVers);
        }
    }

    /**
     * Test a create and edit siblings scenario.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testSiblingsEdition() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing history with siblings edition");

        // first we create a complete new folder as base for the test
        String folder = "/siblings_edition/";
        cms.createResource(folder, CmsResourceTypeFolder.getStaticTypeId());
        OpenCms.getPublishManager().publishResource(cms, folder);
        OpenCms.getPublishManager().waitWhileRunning();

        // 1. create first sibling
        String s1name = folder + "s1.txt";
        CmsResource s1 = cms.createResource(
            s1name,
            CmsResourceTypePlain.getStaticTypeId(),
            "first sibling".getBytes(),
            null);

        // check the history
        assertHistory(cms, s1name, 1);

        // 2. publish s1
        OpenCms.getPublishManager().publishResource(cms, s1name);
        OpenCms.getPublishManager().waitWhileRunning();

        // check the history
        assertHistory(cms, s1name, 1);
        I_CmsHistoryResource histRes = cms.readResource(s1.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());

        // remember pub tag to be able to check the next operations
        int basePubTag = histRes.getPublishTag();

        // 3. create second sibling
        String s2name = folder + "s2.txt";
        CmsResource s2 = cms.createSibling(s1name, s2name, null);

        // check the history
        assertHistory(cms, s1name, 1);
        assertHistory(cms, s2name, 2);

        // 4. publish s2
        OpenCms.getPublishManager().publishResource(cms, s2name);
        OpenCms.getPublishManager().waitWhileRunning();

        // check the history for s1
        assertHistory(cms, s1name, 1);
        histRes = cms.readResource(s1.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag, histRes.getPublishTag());

        // check the history for s2
        assertHistory(cms, s2name, 2);
        histRes = cms.readResource(s2.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 2);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag + 1, histRes.getPublishTag());

        // 5. make a resource change
        cms.lockResource(s1name);
        cms.writePropertyObject(s1name, new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, null, "resource change"));

        // check history
        assertHistory(cms, s1name, 2);
        assertHistory(cms, s2name, 3);

        // 6. make a structure change on s2
        cms.changeLock(s2name);
        cms.writePropertyObject(
            s2name,
            new CmsProperty(CmsPropertyDefinition.PROPERTY_NAVTEXT, "structure change", null));

        // check history
        assertHistory(cms, s1name, 2);
        assertHistory(cms, s2name, 3);

        // 7. publish s1
        OpenCms.getPublishManager().publishResource(cms, s1name);
        OpenCms.getPublishManager().waitWhileRunning();

        // check the history for s1
        assertHistory(cms, s1name, 2);
        histRes = cms.readResource(s1.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag, histRes.getPublishTag());
        histRes = cms.readResource(s1.getStructureId(), 2);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 2, histRes.getPublishTag());

        // check the history for s2
        assertHistory(cms, s2name, 4);
        histRes = cms.readResource(s2.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 2);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag + 1, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 3);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 2, histRes.getPublishTag());

        // 8. publish s2
        OpenCms.getPublishManager().publishResource(cms, s2name);
        OpenCms.getPublishManager().waitWhileRunning();

        // check the history for s1
        assertHistory(cms, s1name, 2);
        histRes = cms.readResource(s1.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag, histRes.getPublishTag());
        histRes = cms.readResource(s1.getStructureId(), 2);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 2, histRes.getPublishTag());

        // check the history for s2
        assertHistory(cms, s2name, 4);
        histRes = cms.readResource(s2.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 2);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag + 1, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 3);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 2, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 4);
        assertEquals(2, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 3, histRes.getPublishTag());

        // 9. change structure on s1
        cms.lockResource(s1name);
        cms.setDateExpired(s1name, System.currentTimeMillis(), false);

        // check history
        assertHistory(cms, s1name, 3);
        assertHistory(cms, s2name, 4);

        // 10. change structure on s2
        cms.changeLock(s2name);
        cms.setDateReleased(s2name, System.currentTimeMillis(), false);

        // check history
        assertHistory(cms, s1name, 3);
        assertHistory(cms, s2name, 5);

        // 11. publish s1+s2
        OpenCms.getPublishManager().publishResource(cms, s2name, true, null);
        OpenCms.getPublishManager().waitWhileRunning();

        // check the history for s1
        assertHistory(cms, s1name, 3);
        histRes = cms.readResource(s1.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag, histRes.getPublishTag());
        histRes = cms.readResource(s1.getStructureId(), 2);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 2, histRes.getPublishTag());
        histRes = cms.readResource(s1.getStructureId(), 3);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 4, histRes.getPublishTag());

        // check the history for s2
        assertHistory(cms, s2name, 5);
        histRes = cms.readResource(s2.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 2);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag + 1, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 3);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 2, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 4);
        assertEquals(2, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 3, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 5);
        assertEquals(3, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 4, histRes.getPublishTag());

        // 12. make a resource change
        CmsFile file = cms.readFile(s1name);
        file.setContents("resource changed".getBytes());
        cms.lockResource(s1name);
        cms.writeFile(file);

        // check history
        assertHistory(cms, s1name, 4);
        assertHistory(cms, s2name, 6);

        // 13. make a structure change
        cms.changeLock(s2name);
        cms.writePropertyObject(
            s2name,
            new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "structure change", null));

        // check history
        assertHistory(cms, s1name, 4);
        assertHistory(cms, s2name, 6);

        // 14. publish s2 (s1 will be unchanged after that)
        OpenCms.getPublishManager().publishResource(cms, s2name);
        OpenCms.getPublishManager().waitWhileRunning();

        // check the history for s1
        assertHistory(cms, s1name, 4);
        histRes = cms.readResource(s1.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag, histRes.getPublishTag());
        histRes = cms.readResource(s1.getStructureId(), 2);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 2, histRes.getPublishTag());
        histRes = cms.readResource(s1.getStructureId(), 3);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 4, histRes.getPublishTag());
        histRes = cms.readResource(s1.getStructureId(), 4);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(3, histRes.getResourceVersion());
        assertEquals(basePubTag + 5, histRes.getPublishTag());

        // check the history for s2
        assertHistory(cms, s2name, 6);
        histRes = cms.readResource(s2.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 2);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag + 1, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 3);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 2, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 4);
        assertEquals(2, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 3, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 5);
        assertEquals(3, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 4, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 6);
        assertEquals(3, histRes.getStructureVersion());
        assertEquals(3, histRes.getResourceVersion());
        assertEquals(basePubTag + 5, histRes.getPublishTag());

        // 15. make a resource change
        cms.lockResource(s1name);
        cms.writePropertyObject(
            s1name,
            new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, null, "new resource change"));

        // check history
        assertHistory(cms, s1name, 5);
        assertHistory(cms, s2name, 7);

        // 16. make a structure change on s1
        cms.writePropertyObject(
            s1name,
            new CmsProperty(CmsPropertyDefinition.PROPERTY_NAVTEXT, "new structure change", null));

        // check history
        assertHistory(cms, s1name, 5);
        assertHistory(cms, s2name, 7);

        // 17. make a structure change on s2
        cms.changeLock(s2name);
        cms.writePropertyObject(
            s2name,
            new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "new structure change", null));

        // check history
        assertHistory(cms, s1name, 5);
        assertHistory(cms, s2name, 7);

        // 18. publish s2
        OpenCms.getPublishManager().publishResource(cms, s2name);
        OpenCms.getPublishManager().waitWhileRunning();

        // check the history for s1
        assertHistory(cms, s1name, 6);
        histRes = cms.readResource(s1.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag, histRes.getPublishTag());
        histRes = cms.readResource(s1.getStructureId(), 2);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 2, histRes.getPublishTag());
        histRes = cms.readResource(s1.getStructureId(), 3);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 4, histRes.getPublishTag());
        histRes = cms.readResource(s1.getStructureId(), 4);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(3, histRes.getResourceVersion());
        assertEquals(basePubTag + 5, histRes.getPublishTag());
        histRes = cms.readResource(s1.getStructureId(), 5);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(4, histRes.getResourceVersion());
        assertEquals(basePubTag + 6, histRes.getPublishTag());

        // check the history for s2
        assertHistory(cms, s2name, 7);
        histRes = cms.readResource(s2.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 2);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag + 1, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 3);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 2, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 4);
        assertEquals(2, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 3, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 5);
        assertEquals(3, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 4, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 6);
        assertEquals(3, histRes.getStructureVersion());
        assertEquals(3, histRes.getResourceVersion());
        assertEquals(basePubTag + 5, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 7);
        assertEquals(3, histRes.getStructureVersion());
        assertEquals(4, histRes.getResourceVersion());
        assertEquals(basePubTag + 6, histRes.getPublishTag());

        // 19. publish s1
        OpenCms.getPublishManager().publishResource(cms, s1name);
        OpenCms.getPublishManager().waitWhileRunning();

        // check the history for s1
        assertHistory(cms, s1name, 6);
        histRes = cms.readResource(s1.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag, histRes.getPublishTag());
        histRes = cms.readResource(s1.getStructureId(), 2);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 2, histRes.getPublishTag());
        histRes = cms.readResource(s1.getStructureId(), 3);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 4, histRes.getPublishTag());
        histRes = cms.readResource(s1.getStructureId(), 4);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(3, histRes.getResourceVersion());
        assertEquals(basePubTag + 5, histRes.getPublishTag());
        histRes = cms.readResource(s1.getStructureId(), 5);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(4, histRes.getResourceVersion());
        assertEquals(basePubTag + 6, histRes.getPublishTag());
        histRes = cms.readResource(s1.getStructureId(), 6);
        assertEquals(2, histRes.getStructureVersion());
        assertEquals(4, histRes.getResourceVersion());
        assertEquals(basePubTag + 7, histRes.getPublishTag());

        // check the history for s2
        assertHistory(cms, s2name, 7);
        histRes = cms.readResource(s2.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 2);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag + 1, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 3);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 2, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 4);
        assertEquals(2, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 3, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 5);
        assertEquals(3, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 4, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 6);
        assertEquals(3, histRes.getStructureVersion());
        assertEquals(3, histRes.getResourceVersion());
        assertEquals(basePubTag + 5, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 7);
        assertEquals(3, histRes.getStructureVersion());
        assertEquals(4, histRes.getResourceVersion());
        assertEquals(basePubTag + 6, histRes.getPublishTag());

        // 20. create a third sibling
        String s3name = folder + "s3.txt";
        CmsResource s3 = cms.createSibling(s1name, s3name, null);

        // check history
        assertHistory(cms, s1name, 6);
        assertHistory(cms, s2name, 7);
        assertHistory(cms, s3name, 5);

        // 21. publish s3
        OpenCms.getPublishManager().publishResource(cms, s3name);
        OpenCms.getPublishManager().waitWhileRunning();

        // check the history for s1
        assertHistory(cms, s1name, 6);
        histRes = cms.readResource(s1.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag, histRes.getPublishTag());
        histRes = cms.readResource(s1.getStructureId(), 2);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 2, histRes.getPublishTag());
        histRes = cms.readResource(s1.getStructureId(), 3);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 4, histRes.getPublishTag());
        histRes = cms.readResource(s1.getStructureId(), 4);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(3, histRes.getResourceVersion());
        assertEquals(basePubTag + 5, histRes.getPublishTag());
        histRes = cms.readResource(s1.getStructureId(), 5);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(4, histRes.getResourceVersion());
        assertEquals(basePubTag + 6, histRes.getPublishTag());
        histRes = cms.readResource(s1.getStructureId(), 6);
        assertEquals(2, histRes.getStructureVersion());
        assertEquals(4, histRes.getResourceVersion());
        assertEquals(basePubTag + 7, histRes.getPublishTag());

        // check the history for s2
        assertHistory(cms, s2name, 7);
        histRes = cms.readResource(s2.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 2);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag + 1, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 3);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 2, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 4);
        assertEquals(2, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 3, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 5);
        assertEquals(3, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 4, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 6);
        assertEquals(3, histRes.getStructureVersion());
        assertEquals(3, histRes.getResourceVersion());
        assertEquals(basePubTag + 5, histRes.getPublishTag());
        histRes = cms.readResource(s2.getStructureId(), 7);
        assertEquals(3, histRes.getStructureVersion());
        assertEquals(4, histRes.getResourceVersion());
        assertEquals(basePubTag + 6, histRes.getPublishTag());

        // check the history for s3
        assertHistory(cms, s3name, 5);
        histRes = cms.readResource(s3.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag, histRes.getPublishTag());
        histRes = cms.readResource(s3.getStructureId(), 2);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 2, histRes.getPublishTag());
        histRes = cms.readResource(s3.getStructureId(), 3);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(3, histRes.getResourceVersion());
        assertEquals(basePubTag + 5, histRes.getPublishTag());
        histRes = cms.readResource(s3.getStructureId(), 4);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(4, histRes.getResourceVersion());
        assertEquals(basePubTag + 6, histRes.getPublishTag());
        histRes = cms.readResource(s3.getStructureId(), 5);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(4, histRes.getResourceVersion());
        assertEquals(basePubTag + 8, histRes.getPublishTag());
    }

    /**
     * Test a sibling restoration scenario.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testSiblingsRestoration() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing history with siblings restoration");

        // first we create a complete new folder as base for the test
        String folder = "/siblings_restoration/";
        cms.createResource(folder, CmsResourceTypeFolder.getStaticTypeId());
        OpenCms.getPublishManager().publishResource(cms, folder);
        OpenCms.getPublishManager().waitWhileRunning();

        // 1. create first sibling
        String s1name = folder + "s1.txt";
        String txt1 = "first sibling";
        CmsResource s1 = cms.createResource(s1name, CmsResourceTypePlain.getStaticTypeId(), txt1.getBytes(), null);

        // check the history
        assertHistory(cms, s1name, 1);

        // 2. create second sibling
        String s2name = folder + "s2.txt";
        CmsResource s2 = cms.createSibling(s1name, s2name, null);

        // check the history
        assertHistory(cms, s1name, 1);
        assertHistory(cms, s2name, 1);

        // 3. publish both
        OpenCms.getPublishManager().publishResource(cms, folder);
        OpenCms.getPublishManager().waitWhileRunning();

        // check the history for s1
        assertHistory(cms, s1name, 1);
        I_CmsHistoryResource histRes = cms.readResource(s1.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(txt1, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));

        // check the history for s2
        assertHistory(cms, s2name, 1);
        histRes = cms.readResource(s1.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(txt1, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));

        // remember pub tag to be able to check the next operations
        int basePubTag = histRes.getPublishTag();

        // 4. create third sibling
        String s3name = folder + "s3.txt";
        CmsResource s3 = cms.createSibling(s1name, s3name, null);

        // check the history
        assertHistory(cms, s1name, 1);
        assertHistory(cms, s2name, 1);
        assertHistory(cms, s3name, 2);

        // 5. make a resource change
        cms.changeLock(s1name);
        cms.writePropertyObject(s1name, new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, null, "resource change"));

        // check the history
        assertHistory(cms, s1name, 2);
        assertHistory(cms, s2name, 2);
        assertHistory(cms, s3name, 2);

        // 6. publish s3
        OpenCms.getPublishManager().publishResource(cms, s3name);
        OpenCms.getPublishManager().waitWhileRunning();

        // check the history for s1
        assertHistory(cms, s1name, 2);
        histRes = cms.readResource(s1.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertFalse(histRes.getState().isDeleted());
        assertEquals(basePubTag, histRes.getPublishTag());
        assertEquals(txt1, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));
        histRes = cms.readResource(s1.getStructureId(), 2);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertFalse(histRes.getState().isDeleted());
        assertEquals(basePubTag + 1, histRes.getPublishTag());
        assertEquals(txt1, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));

        // check the history for s2
        assertHistory(cms, s2name, 2);
        histRes = cms.readResource(s1.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertFalse(histRes.getState().isDeleted());
        assertEquals(basePubTag, histRes.getPublishTag());
        assertEquals(txt1, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));
        histRes = cms.readResource(s2.getStructureId(), 2);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertFalse(histRes.getState().isDeleted());
        assertEquals(basePubTag + 1, histRes.getPublishTag());
        assertEquals(txt1, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));

        // check the history for s3
        assertHistory(cms, s3name, 2);
        histRes = cms.readResource(s3.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertFalse(histRes.getState().isDeleted());
        assertEquals(basePubTag, histRes.getPublishTag());
        assertEquals(txt1, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));
        histRes = cms.readResource(s3.getStructureId(), 2);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertFalse(histRes.getState().isDeleted());
        assertEquals(basePubTag + 1, histRes.getPublishTag());
        assertEquals(txt1, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));

        // 7. delete s3
        cms.lockResource(s3name);
        cms.deleteResource(s3name, CmsResource.DELETE_PRESERVE_SIBLINGS);

        // check history
        assertHistory(cms, s1name, 2);
        assertHistory(cms, s2name, 2);
        assertEquals(3, cms.readResource(s3name, CmsResourceFilter.ALL).getVersion());

        // 8. publish s3
        OpenCms.getPublishManager().publishResource(cms, s3name);
        OpenCms.getPublishManager().waitWhileRunning();

        // check the history for s1
        assertHistory(cms, s1name, 2);
        histRes = cms.readResource(s1.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertFalse(histRes.getState().isDeleted());
        assertEquals(basePubTag, histRes.getPublishTag());
        assertEquals(txt1, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));
        histRes = cms.readResource(s1.getStructureId(), 2);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertFalse(histRes.getState().isDeleted());
        assertEquals(basePubTag + 1, histRes.getPublishTag());
        assertEquals(txt1, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));

        // check the history for s2
        assertHistory(cms, s2name, 2);
        histRes = cms.readResource(s1.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertFalse(histRes.getState().isDeleted());
        assertEquals(basePubTag, histRes.getPublishTag());
        assertEquals(txt1, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));
        histRes = cms.readResource(s2.getStructureId(), 2);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertFalse(histRes.getState().isDeleted());
        assertEquals(basePubTag + 1, histRes.getPublishTag());
        assertEquals(txt1, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));

        // 9. delete s2
        cms.lockResource(s2name);
        cms.deleteResource(s2name, CmsResource.DELETE_PRESERVE_SIBLINGS);

        // check history
        assertHistory(cms, s1name, 2);
        assertEquals(3, cms.readResource(s2name, CmsResourceFilter.ALL).getVersion());

        // 10. make a resource change
        CmsFile file = cms.readFile(s1name);
        String txt2 = "resource changed";
        file.setContents(txt2.getBytes());
        cms.changeLock(s1name);
        cms.writeFile(file);

        // check history
        assertHistory(cms, s1name, 3);

        // 11. publish s2
        OpenCms.getPublishManager().publishResource(cms, s2name);
        OpenCms.getPublishManager().waitWhileRunning();

        // check online contents
        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
        assertContent(cms, s1name, txt1.getBytes());
        // go back to offline
        cms = getCmsObject();

        // check the history for s1
        assertState(cms, s1name, CmsResource.STATE_CHANGED);
        assertHistory(cms, s1name, 3);
        histRes = cms.readResource(s1.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertFalse(histRes.getState().isDeleted());
        assertEquals(basePubTag, histRes.getPublishTag());
        assertEquals(txt1, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));
        histRes = cms.readResource(s1.getStructureId(), 2);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertFalse(histRes.getState().isDeleted());
        assertEquals(basePubTag + 1, histRes.getPublishTag());
        assertEquals(txt1, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));

        // 12. delete s1
        cms.lockResource(s1name);
        cms.deleteResource(s1name, CmsResource.DELETE_PRESERVE_SIBLINGS);

        // assert version
        assertEquals(3, cms.readResource(s1name, CmsResourceFilter.ALL).getVersion());

        // 13. publish s1
        OpenCms.getPublishManager().publishResource(cms, s1name);
        OpenCms.getPublishManager().waitWhileRunning();

        // 14. restore s1
        cms.restoreDeletedResource(s1.getStructureId());

        // check history
        assertHistoryForRestored(cms, s1name, 4);

        // 15. publish s1
        OpenCms.getPublishManager().publishResource(cms, s1name);
        OpenCms.getPublishManager().waitWhileRunning();

        // check the history for s1
        assertHistory(cms, s1name, 4);
        histRes = cms.readResource(s1.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertFalse(histRes.getState().isDeleted());
        assertEquals(basePubTag, histRes.getPublishTag());
        assertEquals(txt1, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));
        histRes = cms.readResource(s1.getStructureId(), 2);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertFalse(histRes.getState().isDeleted());
        assertEquals(basePubTag + 1, histRes.getPublishTag());
        assertEquals(txt1, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));
        histRes = cms.readResource(s1.getStructureId(), 3);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(3, histRes.getResourceVersion());
        assertTrue(histRes.getState().isDeleted());
        assertEquals(basePubTag + 4, histRes.getPublishTag());
        assertEquals(txt2, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));
        histRes = cms.readResource(s1.getStructureId(), 4);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(4, histRes.getResourceVersion());
        assertFalse(histRes.getState().isDeleted());
        assertEquals(basePubTag + 5, histRes.getPublishTag());
        assertEquals(txt2, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));

        // 16. restore s2
        cms.restoreDeletedResource(s2.getStructureId());

        // check history
        assertHistory(cms, s1name, 5);
        assertHistoryForRestored(cms, s2name, 6);

        // 17. publish s2
        OpenCms.getPublishManager().publishResource(cms, s2name);
        OpenCms.getPublishManager().waitWhileRunning();

        // check the history for s1
        assertHistory(cms, s1name, 5);
        histRes = cms.readResource(s1.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertFalse(histRes.getState().isDeleted());
        assertEquals(basePubTag, histRes.getPublishTag());
        assertEquals(txt1, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));
        histRes = cms.readResource(s1.getStructureId(), 2);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertFalse(histRes.getState().isDeleted());
        assertEquals(basePubTag + 1, histRes.getPublishTag());
        assertEquals(txt1, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));
        histRes = cms.readResource(s1.getStructureId(), 3);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(3, histRes.getResourceVersion());
        assertTrue(histRes.getState().isDeleted());
        assertEquals(basePubTag + 4, histRes.getPublishTag());
        assertEquals(txt2, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));
        histRes = cms.readResource(s1.getStructureId(), 4);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(4, histRes.getResourceVersion());
        assertFalse(histRes.getState().isDeleted());
        assertEquals(basePubTag + 5, histRes.getPublishTag());
        assertEquals(txt2, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));
        histRes = cms.readResource(s1.getStructureId(), 5);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(5, histRes.getResourceVersion());
        assertFalse(histRes.getState().isDeleted());
        assertEquals(basePubTag + 6, histRes.getPublishTag());
        // we deleted s2 before publishing the contents, so the restored version has the old content
        assertEquals(txt1, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));

        // check the history for s2
        assertHistory(cms, s2name, 6);
        histRes = cms.readResource(s1.getStructureId(), 1);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(1, histRes.getResourceVersion());
        assertEquals(basePubTag, histRes.getPublishTag());
        assertEquals(txt1, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));
        histRes = cms.readResource(s2.getStructureId(), 2);
        assertEquals(0, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 1, histRes.getPublishTag());
        assertEquals(txt1, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));
        histRes = cms.readResource(s2.getStructureId(), 3);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(2, histRes.getResourceVersion());
        assertEquals(basePubTag + 3, histRes.getPublishTag());
        assertEquals(txt1, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));
        histRes = cms.readResource(s2.getStructureId(), 4);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(3, histRes.getResourceVersion());
        assertTrue(histRes.getState().isDeleted());
        assertEquals(basePubTag + 4, histRes.getPublishTag());
        assertEquals(txt2, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));
        histRes = cms.readResource(s2.getStructureId(), 5);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(4, histRes.getResourceVersion());
        assertTrue(histRes.getState().isDeleted());
        assertEquals(basePubTag + 5, histRes.getPublishTag());
        assertEquals(txt2, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));
        histRes = cms.readResource(s2.getStructureId(), 6);
        assertEquals(1, histRes.getStructureVersion());
        assertEquals(5, histRes.getResourceVersion());
        assertFalse(histRes.getState().isDeleted());
        assertEquals(basePubTag + 6, histRes.getPublishTag());
        // we deleted s2 before publishing the contents, so the restored version has the old content
        assertEquals(txt1, new String(cms.readFile((CmsHistoryFile)histRes).getContents()));
    }

    /**
     * Tests an issue present in OpenCms 7 siblings have a wrong history.<p>
     *
     * @throws Exception if the test fails
     */
    public void testSiblingsV7HistoryIssue() throws Exception {

        echo("Tests OpenCms v7 history issue with siblings");
        CmsObject cms = getCmsObject();

        // first we create a complete new folder as base for the test
        String folderA = "/history_v7issue_a/";
        cms.createResource(folderA, CmsResourceTypeFolder.getStaticTypeId());

        String firstContent = "This is the first content";
        byte[] firstContentBytes = firstContent.getBytes();

        String source = folderA + "1.txt";
        cms.createResource(source, CmsResourceTypePlain.getStaticTypeId(), firstContentBytes, null);

        assertState(cms, folderA, CmsResourceState.STATE_NEW);
        assertState(cms, source, CmsResourceState.STATE_NEW);

        // publish the folder
        OpenCms.getPublishManager().publishResource(cms, folderA);
        OpenCms.getPublishManager().waitWhileRunning();

        assertHistory(cms, source, 1);

        String secondContent = "This is the second content";
        byte[] secondContentBytes = secondContent.getBytes();

        CmsFile sourceFile = cms.readFile(source);
        cms.lockResource(source);
        sourceFile.setContents(secondContentBytes);
        cms.writeFile(sourceFile);

        // publish the folder again
        OpenCms.getPublishManager().publishResource(cms, folderA);
        OpenCms.getPublishManager().waitWhileRunning();

        assertHistory(cms, source, 2);

        // create a another new folder as base for the test
        String folderB = "/history_v7issue_b/";
        cms.createResource(folderB, CmsResourceTypeFolder.getStaticTypeId());

        // publish the new folder
        OpenCms.getPublishManager().publishResource(cms, folderB);
        OpenCms.getPublishManager().waitWhileRunning();

        // copy the source to the new folder
        String destination = folderB + "2.txt";
        cms.copyResource(source, destination, CmsResource.COPY_AS_SIBLING);

        // now publish the sibling
        OpenCms.getPublishManager().publishResource(cms, destination, false, null);
        OpenCms.getPublishManager().waitWhileRunning();

        assertHistory(cms, source, 2);
        assertHistory(cms, destination, 3);
    }

    /**
     * Tests an issue present in OpenCms 7 siblings have a wrong history.<p>
     *
     * @throws Exception if the test fails
     */
    public void testSiblingsV7HistoryIssue2() throws Exception {

        echo("Tests OpenCms v7 history issue with siblings");
        CmsObject cms = getCmsObject();

        // first we create a complete new folder as base for the test
        String folderA = "/history_v7issue2_a/";
        cms.createResource(folderA, CmsResourceTypeFolder.getStaticTypeId());

        // create a new resource
        String firstContent = "This is the first content";
        byte[] firstContentBytes = firstContent.getBytes();

        String source = folderA + "1.txt";
        cms.createResource(source, CmsResourceTypePlain.getStaticTypeId(), firstContentBytes, null);

        // check
        assertState(cms, folderA, CmsResourceState.STATE_NEW);
        assertState(cms, source, CmsResourceState.STATE_NEW);

        assertHistory(cms, source, 1);

        // publish the folder and resource
        OpenCms.getPublishManager().publishResource(cms, folderA);
        OpenCms.getPublishManager().waitWhileRunning();

        // check
        assertHistory(cms, source, 1);

        // modify the resource (resource entry)
        String secondContent = "This is the second content";
        byte[] secondContentBytes = secondContent.getBytes();

        CmsFile sourceFile = cms.readFile(source);
        cms.lockResource(source);
        sourceFile.setContents(secondContentBytes);
        cms.writeFile(sourceFile);

        // check
        assertHistory(cms, source, 2);

        // create a another new folder as base for the test
        String folderB = "/history_v7issue2_b/";
        cms.createResource(folderB, CmsResourceTypeFolder.getStaticTypeId());

        // publish the new folder
        OpenCms.getPublishManager().publishResource(cms, folderB);
        OpenCms.getPublishManager().waitWhileRunning();

        // check
        assertHistory(cms, source, 2);

        // copy the source to the new folder as sibling
        String destination = folderB + "2.txt";
        cms.copyResource(source, destination, CmsResource.COPY_AS_SIBLING);

        // check
        assertHistory(cms, source, 2);
        assertHistory(cms, destination, 2);

        // now publish the sibling
        OpenCms.getPublishManager().publishResource(cms, destination, false, null);
        OpenCms.getPublishManager().waitWhileRunning();

        // check
        assertHistory(cms, source, 2);
        assertHistory(cms, destination, 2);
    }

    /**
     * Test the version number of siblings after different operations.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testSiblingVersions() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing sibling version numbers");

        String sib1 = "/sibVer1.txt";
        String sib2 = "/sibVer2.txt";

        cms.createResource(sib1, CmsResourceTypePlain.getStaticTypeId());
        cms.createSibling(sib1, sib2, null);

        // check offline
        assertHistory(cms, sib1, 1);
        assertHistory(cms, sib2, 1);

        OpenCms.getPublishManager().publishResource(cms, sib1, true, new CmsShellReport(Locale.ENGLISH));
        OpenCms.getPublishManager().waitWhileRunning();

        // check after publish
        assertHistory(cms, sib1, 1);
        // siblings where published together
        assertHistory(cms, sib2, 1);

        // check online
        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));
        assertHistory(cms, sib2, 1);

        // back to offline
        cms = getCmsObject();

        // now the same but in a different sequence
        String sib3 = "/sibVer3.txt";
        String sib4 = "/sibVer4.txt";

        cms.createResource(sib3, CmsResourceTypePlain.getStaticTypeId());
        cms.createSibling(sib3, sib4, null);

        // check offline
        assertHistory(cms, sib3, 1);
        assertHistory(cms, sib4, 1);

        OpenCms.getPublishManager().publishResource(cms, sib3);
        OpenCms.getPublishManager().waitWhileRunning();

        // check after publish of sib3
        assertHistory(cms, sib3, 1);
        assertHistory(cms, sib4, 2);

        OpenCms.getPublishManager().publishResource(cms, sib4);
        OpenCms.getPublishManager().waitWhileRunning();

        // check after publish of sib3
        assertHistory(cms, sib3, 1);
        assertHistory(cms, sib4, 2);
    }

    /**
     * Test that the versions are properly updated
     * after reaching the limit of stored versions.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testVersioningLimit() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing versioning limit");

        String source = "/index.html";
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        // set the history version settings
        OpenCms.getSystemInfo().setVersionHistorySettings(true, 3, 3);

        // make 5 versions
        for (int i = 0; i < 5; i++) {
            if (i < 3) {
                assertEquals(i + 1, cms.readAllAvailableVersions(source).size());
            } else {
                assertEquals(3, cms.readAllAvailableVersions(source).size());
            }
            cms.lockResource(source);
            cms.setDateLastModified(source, System.currentTimeMillis(), false);
            cms.setDateExpired(source, System.currentTimeMillis(), false);
            cms.setDateReleased(source, System.currentTimeMillis(), false);
            cms.unlockResource(source);
            OpenCms.getPublishManager().publishResource(cms, source);
            OpenCms.getPublishManager().waitWhileRunning();
        }
    }

    /**
     * Turns the byte content of a resource into a string.<p>
     *
     * @param cms the current user's Cms object
     * @param content the byte content of a resource
     * @return the content as a string
     */
    protected String getContentString(CmsObject cms, byte[] content) {

        try {
            return new String(content, cms.getRequestContext().getEncoding());
        } catch (UnsupportedEncodingException e) {
            return new String(content);
        }
    }
}

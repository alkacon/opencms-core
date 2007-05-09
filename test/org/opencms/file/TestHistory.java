/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestHistory.java,v $
 * Date   : $Date: 2007/05/09 07:59:19 $
 * Version: $Revision: 1.1.2.3 $
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

package org.opencms.file;

import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.io.UnsupportedEncodingException;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for history operation.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.3 $
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

        suite.addTest(new TestHistory("testCreateAndDeleteFile"));
        suite.addTest(new TestHistory("testCreateAndDeleteFolder"));
        suite.addTest(new TestHistory("testMoveFile"));
        suite.addTest(new TestHistory("testFileHistory"));
        suite.addTest(new TestHistory("testFileHistoryFileWithSibling"));
        suite.addTest(new TestHistory("testFileVersions"));
        suite.addTest(new TestHistory("testVersioningLimit"));

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

        // the history works only by ID so the expected number of versions is ZERO
        cms.createResource(filename, CmsResourceTypePlain.getStaticTypeId(), null, null);
        List historyResources = cms.readAllAvailableVersions(filename);
        assertTrue(historyResources.isEmpty());

        // re-create the resource to be able to read all versions from the history
        // assert that we have the expected number of versions in the history
        String importFile = "import.txt";
        cms.importResource(importFile, res, "blah-blah".getBytes(), null);
        historyResources = cms.readAllAvailableVersions(importFile);
        assertEquals(counter + 2, historyResources.size()); // counter + created + deleted

        for (int i = 0; i < counter + 2; i++) {
            // the list of historical resources contains at index 0 the 
            // resource with the highest version and tag ID
            int version = counter + 2 - i;
            String contentStr = "content version " + (version - 1);

            // assert that the historical resource has the correct version
            I_CmsHistoryResource historyResource = (I_CmsHistoryResource)historyResources.get(i);
            assertEquals(version, historyResource.getVersion());

            cms.restoreResourceVersion(historyResource.getResource().getStructureId(), historyResource.getVersion());
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
        cms.writePropertyObject(folderName, new CmsProperty(
            CmsPropertyDefinition.PROPERTY_TITLE,
            "title version 0",
            null));
        cms.unlockResource(folderName);
        OpenCms.getPublishManager().publishResource(cms, folderName);
        OpenCms.getPublishManager().waitWhileRunning();

        for (int i = 1; i <= counter; i++) {
            cms.lockResource(folderName);
            cms.writePropertyObject(folderName, new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "title version "
                + i, null));
            OpenCms.getPublishManager().publishResource(cms, folderName);
            OpenCms.getPublishManager().waitWhileRunning();
        }
        cms.lockResource(folderName);
        cms.writePropertyObject(folderName, new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "title version "
            + (counter + 1), null));
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

        for (int i = 0; i < counter + 2; i++) {
            // the list of historical resources contains at index 0 the 
            // folder with the highest version and tag ID
            int version = counter + 2 - i;
            String title = "title version " + (version - 1);

            // assert that the historical resource has the correct version
            I_CmsHistoryResource historyResource = (I_CmsHistoryResource)historyResources.get(i);
            assertEquals(version, historyResource.getVersion());

            cms.restoreResourceVersion(historyResource.getResource().getStructureId(), historyResource.getVersion());
            CmsProperty property = cms.readPropertyObject(
                cms.readResource(importFolder),
                CmsPropertyDefinition.PROPERTY_TITLE,
                false);

            // assert that the title and version fit together
            assertEquals(title, property.getStructureValue());
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
        assertEquals(counter + 1, historyResourcesForFile.size());
        assertEquals(2 * (counter + 1), historyResourcesForSibling.size());
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

        // if this does not match, the logic for deletion of old versions is responsible
        int todo;
        List allFiles = cms.readAllAvailableVersions(siblingname);
        //        assertEquals(counter + counterSibl + 1, allFiles.size());

        //Delete historical entries, keep only 3 latest versions. 
        cms.deleteHistoricalVersions(false, 3, new CmsShellReport(cms.getRequestContext().getLocale()));

        allFiles = cms.readAllAvailableVersions(siblingname);
        assertEquals(3, allFiles.size());

        I_CmsHistoryResource history = (I_CmsHistoryResource)allFiles.get(1);
        cms.lockResource(siblingname);
        //and restore it from history
        cms.restoreResourceVersion(history.getResource().getStructureId(), history.getVersion());
        cms.unlockResource(siblingname);
        CmsFile file = cms.readFile(siblingname);

        // assert that the content and version fit together
        String restoredContent = getContentString(cms, file.getContents());

        assertEquals("sibling content version 6", restoredContent);
        CmsProperty prop = cms.readPropertyObject(siblingname, CmsPropertyDefinition.PROPERTY_TITLE, false);
        assertEquals("SiblingTitle3", prop.getValue());

        // create a new empty resource
        cms.createResource(siblingname2, CmsResourceTypePlain.getStaticTypeId(), null, null);

        allFiles = cms.readAllAvailableVersions(siblingname2);
        assertTrue(allFiles.isEmpty());
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

        CmsProject offline = cms.getRequestContext().currentProject();
        CmsProject online = cms.readProject(CmsProject.ONLINE_PROJECT_ID);

        // 1. create new resource
        cms.createResource(resName, CmsResourceTypePlain.getStaticTypeId());
        // check offline
        assertVersion(cms, resName, 1);

        // publish
        OpenCms.getPublishManager().publishResource(cms, resName);
        OpenCms.getPublishManager().waitWhileRunning();

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertVersion(cms, resName, 1);

        // 2. modify resource
        cms.getRequestContext().setCurrentProject(offline);
        cms.lockResource(resName);
        cms.setDateLastModified(resName, System.currentTimeMillis(), false);
        // check offline
        assertVersion(cms, resName, 2);

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertVersion(cms, resName, 1);

        // publish
        cms.getRequestContext().setCurrentProject(offline);
        OpenCms.getPublishManager().publishResource(cms, resName);
        OpenCms.getPublishManager().waitWhileRunning();

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertVersion(cms, resName, 2);

        // 3. modify structure
        cms.getRequestContext().setCurrentProject(offline);
        cms.lockResource(resName);
        cms.writePropertyObject(resName, new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "test", null));
        // check offline
        assertVersion(cms, resName, 3);

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertVersion(cms, resName, 2);

        // publish
        cms.getRequestContext().setCurrentProject(offline);
        assertVersion(cms, resName, 3);
        OpenCms.getPublishManager().publishResource(cms, resName);
        OpenCms.getPublishManager().waitWhileRunning();
        assertVersion(cms, resName, 3);

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertVersion(cms, resName, 3);

        String sibName = "fileVersionTest_sib.txt";

        // 4. create sibling cms.readResource(resName)
        cms.getRequestContext().setCurrentProject(offline);
        cms.createSibling(resName, sibName, null);

        // check offline
        assertVersion(cms, resName, 3);
        assertVersion(cms, sibName, 3);

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertVersion(cms, resName, 3);

        // publish sibling
        cms.getRequestContext().setCurrentProject(offline);
        OpenCms.getPublishManager().publishResource(cms, sibName);
        OpenCms.getPublishManager().waitWhileRunning();

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertVersion(cms, resName, 3);
        assertVersion(cms, sibName, 3);

        // 5. modify sibling resource
        cms.getRequestContext().setCurrentProject(offline);
        cms.lockResource(sibName);
        cms.writePropertyObject(sibName, new CmsProperty(
            CmsPropertyDefinition.PROPERTY_DESCRIPTION,
            null,
            "test description"));
        // check offline
        assertVersion(cms, resName, 4);
        assertVersion(cms, sibName, 4);

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertVersion(cms, resName, 3);
        assertVersion(cms, sibName, 3);

        // publish
        cms.getRequestContext().setCurrentProject(offline);
        OpenCms.getPublishManager().publishResource(cms, sibName);
        OpenCms.getPublishManager().waitWhileRunning();

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertVersion(cms, resName, 4);
        assertVersion(cms, sibName, 4);

        // 6. modify structure
        cms.getRequestContext().setCurrentProject(offline);
        cms.lockResource(resName);
        cms.setDateExpired(resName, System.currentTimeMillis() + 100000, false);
        // check offline
        assertVersion(cms, resName, 5);
        assertVersion(cms, sibName, 4);

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertVersion(cms, resName, 4);
        assertVersion(cms, sibName, 4);

        // publish
        cms.getRequestContext().setCurrentProject(offline);
        OpenCms.getPublishManager().publishResource(cms, resName);
        OpenCms.getPublishManager().waitWhileRunning();

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertVersion(cms, resName, 5);
        assertVersion(cms, sibName, 4);

        // 7. modify resource
        cms.getRequestContext().setCurrentProject(offline);
        cms.lockResource(resName);
        cms.writePropertyObject(resName, new CmsProperty(CmsPropertyDefinition.PROPERTY_NAVPOS, null, "1.5"));
        // check offline
        assertVersion(cms, resName, 6);
        assertVersion(cms, sibName, 5);

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertVersion(cms, resName, 5);
        assertVersion(cms, sibName, 4);

        // publish
        cms.getRequestContext().setCurrentProject(offline);
        OpenCms.getPublishManager().publishResource(cms, resName);
        OpenCms.getPublishManager().waitWhileRunning();

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertVersion(cms, resName, 6);
        assertVersion(cms, sibName, 5);

        // 8. modify sibling structure
        cms.getRequestContext().setCurrentProject(offline);
        cms.lockResource(sibName);
        cms.setDateReleased(sibName, System.currentTimeMillis() - 1000, false);
        // check offline
        assertVersion(cms, resName, 6);
        assertVersion(cms, sibName, 6);

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertVersion(cms, resName, 6);
        assertVersion(cms, sibName, 5);

        // publish
        cms.getRequestContext().setCurrentProject(offline);
        OpenCms.getPublishManager().publishResource(cms, sibName);
        OpenCms.getPublishManager().waitWhileRunning();

        // check online
        cms.getRequestContext().setCurrentProject(online);
        assertVersion(cms, resName, 6);
        assertVersion(cms, sibName, 6);

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

        for (int i = 0; i < counter + 2; i++) {
            // the list of historical resources contains at index 0 the 
            // resource with the highest version and tag ID
            int version = counter + 2 - i;
            String contentStr = "content version " + (version - 1);

            // assert that the historical resource has the correct version
            I_CmsHistoryResource historyResource = (I_CmsHistoryResource)historyResources.get(i);
            assertEquals(version, historyResource.getVersion());

            cms.restoreResourceVersion(historyResource.getResource().getStructureId(), historyResource.getVersion());
            file = cms.readFile(importFile);

            // assert that the content and version fit together
            String restoredContent = getContentString(cms, file.getContents());
            assertEquals(contentStr, restoredContent);
        }
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

        // set the versioning settings
        OpenCms.getSystemInfo().setVersionHistorySettings(true, 3);

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

/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/TestHistory.java,v $
 * Date   : $Date: 2007/04/26 14:31:08 $
 * Version: $Revision: 1.1.2.1 $
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
 * @version $Revision: 1.1.2.1 $
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

        suite.addTest(new TestHistory("testCreateAndDeleteResources"));
        suite.addTest(new TestHistory("testFileHistory"));
        suite.addTest(new TestHistory("testFileHistoryFileWithSibling"));
        suite.addTest(new TestHistory("testFileVersions"));

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
     * Creates and deletes a resource n-times and tests if the version ID of the history resources
     * are correct and if the content could be restored for a specified version ID.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCreateAndDeleteResources() throws Throwable {

        CmsObject cms = getCmsObject();
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
        file.setContents("content version 3".getBytes());
        cms.lockResource(filename);
        cms.writeFile(file);
        // delete the resource again
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
     * creates a file, modifies and publishes it n-times, create 2 siblings, 
     * delete file and sibling N2, delete some versions from history, restore 
     * file and sibling from history and compare contents.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testFileHistoryFileWithSibling() throws Throwable {

        CmsObject cms = getCmsObject();
        String filename = "/testFileRoot.txt";
        String siblingname = "/testFileSibling1.txt";
        String siblingname2 = "/testFileSibling2.txt";

        int counter = 5;
        int counterSibl = 6;
        int counterSibl2 = 7;

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

        //Delete historical entries, keep only 3 latest versions. 
        cms.deleteHistoricalVersions(false, 3, new CmsShellReport(cms.getRequestContext().getLocale()));

        List allFiles = cms.readAllAvailableVersions(siblingname);
        assertEquals(3, allFiles.size());

        I_CmsHistoryResource history = (I_CmsHistoryResource)allFiles.get(1);
        cms.lockResource(siblingname);
        //and restore it from history
        cms.restoreResourceVersion(history.getResource().getStructureId(), history.getVersion());
        cms.unlockResource(siblingname);
        CmsFile file = cms.readFile(siblingname);

        // assert that the content and version fit together
        String restoredContent = getContentString(cms, file.getContents());

        assertEquals("sibling content version 10", restoredContent);
        CmsProperty prop = cms.readPropertyObject(siblingname, CmsPropertyDefinition.PROPERTY_TITLE, false);
        assertEquals("SiblingTitle5", prop.getValue());

        // create a new empty resource
        cms.createResource(siblingname2, CmsResourceTypePlain.getStaticTypeId(), null, null);

        allFiles = cms.readAllAvailableVersions(siblingname2);
        assertTrue(allFiles.isEmpty());
    }

    /**
     * creates a file, modifies and publishes it n-times, create a sibling, 
     * publishes both and compares the histories.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testFileHistory() throws Throwable {

        CmsObject cms = getCmsObject();
        String filename = "/testFileHistory1.txt";
        String siblingname = "/testFileHistory2.txt";
        CmsProject offlineProject = cms.getRequestContext().currentProject();
        int counter = 2;

        String storedSiteRoot = cms.getRequestContext().getSiteRoot();
        try {
            // switch to the default site in the offline project
            cms.getRequestContext().setSiteRoot("/sites/default/");
            cms.getRequestContext().setCurrentProject(offlineProject);

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
        } finally {
            cms.getRequestContext().setSiteRoot(storedSiteRoot);
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

         */
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

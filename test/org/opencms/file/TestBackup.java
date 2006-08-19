/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/Attic/TestBackup.java,v $
 * Date   : $Date: 2006/08/19 13:40:37 $
 * Version: $Revision: 1.7.8.2 $
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

import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for backup operation.<p>
 * 
 * @author Thomas Weckert  
 * @version $Revision: 1.7.8.2 $
 */
public class TestBackup extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestBackup(String arg0) {

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
        suite.setName(TestCopy.class.getName());

        suite.addTest(new TestBackup("testFileBackupFileWithSiblingDate"));
        suite.addTest(new TestBackup("testCreateAndDeleteResources"));
        suite.addTest(new TestBackup("testFileHistory"));
        suite.addTest(new TestBackup("testFileBackupFileWithSibling"));

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
     * Creates and deletes a resource n-times and tests if the version ID of the backup resources
     * are correct and if the content could be restored for a specified version ID.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testCreateAndDeleteResources() throws Throwable {

        CmsObject cms = getCmsObject();
        String filename = "/dummy1.txt";
        CmsProject offlineProject = cms.getRequestContext().currentProject();
        int counter = 5;

        try {

            // switch to the default site in the offline project
            cms.getRequestContext().saveSiteRoot();
            cms.getRequestContext().setSiteRoot("/sites/default/");
            cms.getRequestContext().setCurrentProject(offlineProject);

            for (int i = 1; i <= counter; i++) {

                // create a plain text file
                String contentStr = "content version " + i;
                cms.createResource(filename, CmsResourceTypePlain.getStaticTypeId(), contentStr.getBytes(), null);
                cms.unlockResource(filename);
                cms.publishResource(filename);

                // delete the resource again
                cms.lockResource(filename);
                cms.deleteResource(filename, CmsResource.DELETE_PRESERVE_SIBLINGS);
                cms.unlockResource(filename);
                cms.publishResource(filename);
            }

            // re-create the resource to be able to read all versions from the history
            // assert that we have the expected number of versions in the history
            cms.createResource(filename, CmsResourceTypePlain.getStaticTypeId(), null, null);
            List backupResources = cms.readAllBackupFileHeaders(filename);
            assertEquals(counter, backupResources.size());

            for (int i = 0; i < counter; i++) {

                // the list of backup resources contains at index 0 the 
                // resource with the highest version and tag ID
                int version = counter - i;
                String contentStr = "content version " + version;

                // assert that the backup resource has the correct version
                CmsBackupResource backupResource = (CmsBackupResource)backupResources.get(i);
                assertEquals(version, backupResource.getVersionId());

                cms.restoreResourceBackup(filename, backupResource.getTagId());
                CmsFile file = cms.readFile(filename);

                // assert that the content and version fit together
                String restoredContent = getContentString(cms, file.getContents());
                assertEquals(contentStr, restoredContent);
            }
        } finally {
            cms.getRequestContext().restoreSiteRoot();
        }
    }

    /**
     * creates a file, modifies and publishes it n-times, create 2 siblings, 
     * delete file and sibling N2, delete some versions from backup, restore 
     * file and sibling from backup and compare contents.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testFileBackupFileWithSibling() throws Throwable {

        CmsObject cms = getCmsObject();
        String filename = "/testFileRoot.txt";
        String siblingname = "/testFileSibling1.txt";
        String siblingname2 = "/testFileSibling2.txt";
        String content3 = "content version 4";
        String sibling2Content = "sibling2 content version 19";
        CmsProperty property = null;
        CmsProject offlineProject = cms.getRequestContext().currentProject();
        int counter = 5;
        int counterSibl = 6;
        int counterSibl2 = 7;

        try {

            // switch to the default site in the offline project
            cms.getRequestContext().saveSiteRoot();
            cms.getRequestContext().setSiteRoot("/sites/default/");
            cms.getRequestContext().setCurrentProject(offlineProject);

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
                property = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "RootTitle" + i, null);
                cms.writePropertyObject(filename, property);
                cms.unlockResource(filename);
                cms.publishResource(filename);

            }

            // create a sibling
            cms.copyResource(filename, siblingname, CmsResource.COPY_AS_SIBLING);
            cms.unlockResource(siblingname);
            cms.publishResource(siblingname);

            for (int i = 1; i <= counterSibl; i++) {

                // modify the sibling text file
                contentStr = "sibling content version " + (counter + i);
                CmsFile file = cms.readFile(siblingname);
                file.setContents(contentStr.getBytes());
                cms.lockResource(siblingname);
                cms.writeFile(file);
                property = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "SiblingTitle" + i, null);
                cms.writePropertyObject(siblingname, property);
                cms.unlockResource(siblingname);
                cms.publishResource(siblingname);

            }

            // create a sibling2
            cms.copyResource(filename, siblingname2, CmsResource.COPY_AS_SIBLING);
            cms.unlockResource(siblingname2);
            cms.publishResource(siblingname2);

            for (int i = 1; i <= counterSibl2; i++) {

                // modify the sibling text file
                contentStr = "sibling2 content version " + (counter + counterSibl2 + i);
                CmsFile file = cms.readFile(siblingname2);
                file.setContents(contentStr.getBytes());
                cms.lockResource(siblingname2);
                cms.writeFile(file);
                property = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "Sibling2Title" + i, null);
                cms.writePropertyObject(siblingname2, property);
                cms.unlockResource(siblingname2);
                cms.publishResource(siblingname2);
            }

            //          now delete and publish the root
            cms.lockResource(filename);
            cms.deleteResource(filename, CmsResource.DELETE_PRESERVE_SIBLINGS);
            cms.unlockResource(filename);
            cms.publishResource(filename);

            //          now delete and publish sibling     
            cms.lockResource(siblingname2);
            cms.deleteResource(siblingname2, CmsResource.DELETE_PRESERVE_SIBLINGS);
            cms.unlockResource(siblingname2);
            cms.publishResource(siblingname2);

            //Delete backups, keep only 3 latest versions. 
            cms.deleteBackups(0, 3, new CmsShellReport(cms.getRequestContext().getLocale()));
            //restore root file with 
            cms.createResource(filename, CmsResourceTypePlain.getStaticTypeId(), null, null);

            List allFiles = cms.readAllBackupFileHeaders(filename);

            CmsBackupResource backup = (CmsBackupResource)allFiles.get(1);
            cms.lockResource(filename);
            //and restore it from history
            cms.restoreResourceBackup(filename, backup.getTagId());
            cms.unlockResource(filename);
            CmsFile file = cms.readFile(filename);

            // assert that the content and version fit together
            String restoredContent = getContentString(cms, file.getContents());

            assertEquals(content3, restoredContent);
            assertEquals(3, allFiles.size());
            CmsProperty prop;
            prop = cms.readPropertyObject(filename, CmsPropertyDefinition.PROPERTY_TITLE, false);
            assertEquals("RootTitle4", prop.getValue());

            // create a new empty resource
            cms.createResource(siblingname2, CmsResourceTypePlain.getStaticTypeId(), null, null);

            allFiles = cms.readAllBackupFileHeaders(siblingname2);
            backup = (CmsBackupResource)allFiles.get(0);
            cms.lockResource(siblingname2);
            //and restore it from history
            cms.restoreResourceBackup(siblingname2, backup.getTagId());
            cms.unlockResource(siblingname2);
            file = cms.readFile(siblingname2);

            // assert that the content and version fit together
            restoredContent = getContentString(cms, file.getContents());
            assertEquals(sibling2Content, restoredContent);
            assertEquals(3, allFiles.size());
            prop = cms.readPropertyObject(siblingname2, CmsPropertyDefinition.PROPERTY_TITLE, false);
            assertEquals("Sibling2Title7", prop.getValue());

        } finally {
            cms.getRequestContext().restoreSiteRoot();
        }
    }

    /**
     * creates a file, modifies and publishes it n-times, create 1 sibling, 
     * delete file and sibling, delete some versions from backup by date, restore 
     * file and sibling from backup and compare content.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testFileBackupFileWithSiblingDate() throws Throwable {

        CmsObject cms = getCmsObject();
        String filename = "/testFile.txt";
        String siblingname = "/testFileSibl.txt";
        String content3 = "content version 4";
        String siblingContent = "sibling content version 6";
        CmsProperty property = null;
        CmsProject offlineProject = cms.getRequestContext().currentProject();
        int counter = 5;
        int counterSibl = 6;
        long timeToDeleted = 0;

        try {

            // switch to the default site in the offline project
            cms.getRequestContext().saveSiteRoot();
            cms.getRequestContext().setSiteRoot("/sites/default/");
            cms.getRequestContext().setCurrentProject(offlineProject);

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
                property = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "RootTitle" + i, null);
                cms.writePropertyObject(filename, property);
                cms.unlockResource(filename);
                cms.publishResource(filename);
                Thread.sleep(1500);
                //create Sibling and publish it 6 times. 
                if (i == 3) {

                    System.out.println("Time to deleted: " + new Timestamp(timeToDeleted));
                    // create a sibling
                    cms.copyResource(filename, siblingname, CmsResource.COPY_AS_SIBLING);
                    cms.unlockResource(siblingname);
                    cms.publishResource(siblingname);

                    for (int j = 1; j <= counterSibl; j++) {

                        // modify the sibling text file
                        contentStr = "sibling content version " + j;
                        CmsFile siblFile = cms.readFile(siblingname);
                        siblFile.setContents(contentStr.getBytes());
                        cms.lockResource(siblingname);
                        cms.writeFile(siblFile);
                        property = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "SiblingTitle" + i, null);
                        cms.writePropertyObject(siblingname, property);
                        cms.unlockResource(siblingname);
                        cms.publishResource(siblingname);
                        Thread.sleep(1500);

                        if (j == 4) {
                            timeToDeleted = System.currentTimeMillis();
                        }
                    }
                }
            }

            // now delete and publish the root
            cms.lockResource(filename);
            cms.deleteResource(filename, CmsResource.DELETE_PRESERVE_SIBLINGS);
            cms.unlockResource(filename);
            cms.publishResource(filename);
            Thread.sleep(1500);

            // now delete and publish sibling     
            cms.lockResource(siblingname);
            cms.deleteResource(siblingname, CmsResource.DELETE_PRESERVE_SIBLINGS);
            cms.unlockResource(siblingname);
            cms.publishResource(siblingname);

            //Deleted backups
            cms.deleteBackups(timeToDeleted, 0, new CmsShellReport(cms.getRequestContext().getLocale()));
            
            // create a new empty resource
            cms.createResource(filename, CmsResourceTypePlain.getStaticTypeId(), null, null);

            List allFiles = cms.readAllBackupFileHeaders(filename);

            CmsBackupResource backup = (CmsBackupResource)allFiles.get(1);

            cms.lockResource(filename);
            // and restore it from history
            cms.restoreResourceBackup(filename, backup.getTagId());
            cms.unlockResource(filename);
            CmsFile file = cms.readFile(filename);

            // assert that the content and version fit together
            String restoredContent = getContentString(cms, file.getContents());

            assertEquals(content3, restoredContent);
            //Only latest 2 version after inner loop will not deleted. 
            assertEquals(2, allFiles.size());
            CmsProperty prop;
            prop = cms.readPropertyObject(filename, CmsPropertyDefinition.PROPERTY_TITLE, false);
            assertEquals("RootTitle4", prop.getValue());

            // create a new empty resource
            cms.createResource(siblingname, CmsResourceTypePlain.getStaticTypeId(), null, null);

            allFiles = cms.readAllBackupFileHeaders(siblingname);
            backup = (CmsBackupResource)allFiles.get(0);
            cms.lockResource(siblingname);
            // and restore it from history
            cms.restoreResourceBackup(siblingname, backup.getTagId());
            cms.unlockResource(siblingname);
            file = cms.readFile(siblingname);

            // assert that the content and version fit together
            restoredContent = getContentString(cms, file.getContents());
            int todo; // fails with oracle, i guess it is the timestamp field fault
            assertEquals(3, allFiles.size());
            assertEquals(siblingContent, restoredContent);
            prop = cms.readPropertyObject(siblingname, CmsPropertyDefinition.PROPERTY_TITLE, false);
            assertEquals("SiblingTitle3", prop.getValue());

        } finally {
            cms.getRequestContext().restoreSiteRoot();
        }
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
        int counter = 5;

        try {

            // switch to the default site in the offline project
            cms.getRequestContext().saveSiteRoot();
            cms.getRequestContext().setSiteRoot("/sites/default/");
            cms.getRequestContext().setCurrentProject(offlineProject);

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
                cms.unlockResource(filename);
                cms.publishResource(filename);
            }

            // create a sibling
            cms.copyResource(filename, siblingname, CmsResource.COPY_AS_SIBLING);
            cms.unlockResource(siblingname);
            cms.publishResource(siblingname);

            for (int i = 1; i <= counter; i++) {

                // modify the sibling text file
                contentStr = "sibling content version " + (counter + i);
                CmsFile file = cms.readFile(siblingname);
                file.setContents(contentStr.getBytes());
                cms.lockResource(siblingname);
                cms.writeFile(file);
                cms.unlockResource(siblingname);
                cms.publishResource(siblingname);
            }

            List backupResourcesForFile = cms.readAllBackupFileHeaders(filename);
            List backupResourcesForSibling = cms.readAllBackupFileHeaders(siblingname);
            assertEquals(backupResourcesForFile, backupResourcesForSibling);
            assertEquals(2 * counter + 1, backupResourcesForFile.size());
        } finally {
            cms.getRequestContext().restoreSiteRoot();
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

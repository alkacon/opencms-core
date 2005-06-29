/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/file/Attic/TestBackup.java,v $
 * Date   : $Date: 2005/06/29 15:08:40 $
 * Version: $Revision: 1.6 $
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
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.io.UnsupportedEncodingException;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for backup operation.<p>
 * 
 * @author Thomas Weckert  
 * @version $Revision: 1.6 $
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

        //suite.addTest(new TestBackup("testCreateAndDeleteResources"));
        suite.addTest(new TestBackup("testFileHistory"));

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
            assertEquals(2 + counter + 1, backupResourcesForFile.size());
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

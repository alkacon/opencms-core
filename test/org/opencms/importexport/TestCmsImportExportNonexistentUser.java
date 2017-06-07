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

package org.opencms.importexport;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule.ExportMode;
import org.opencms.report.CmsShellReport;
import org.opencms.security.CmsRole;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.ArrayList;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests exporting/import VFS data with nonexistent users.<p>
 *
 */
public class TestCmsImportExportNonexistentUser extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsImportExportNonexistentUser(String arg0) {

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
        suite.setName(TestCmsImportExport.class.getName());

        suite.addTest(new TestCmsImportExportNonexistentUser("testImportExportNonexistentUser"));

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
     * Tests exporting and import of VFS data with a nonexistent/deleted user.<p>
     *
     * The username of the deleted user should in the export manifest be replaced
     * by the name of the Admin user.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testImportExportNonexistentUser() throws Exception {

        String zipExportFilename = null;
        CmsObject cms = getCmsObject();
        String storedSiteRoot = null;

        try {
            String username = "tempuser";
            String password = "password";
            String filename = "/dummy1.txt";
            String contentStr = "This is a comment. I love comments.";
            zipExportFilename = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
                "packages/testImportExportNonexistentUser.zip");
            byte[] content = contentStr.getBytes();
            CmsProject offlineProject = cms.getRequestContext().getCurrentProject();

            // create a temporary user for this test case
            cms.createUser(username, password, "Temporary user for import/export test case", null);
            // add this user to the user group
            cms.addUserToGroup(username, OpenCms.getDefaultUsers().getGroupUsers());
            // give this user the project manager role so that he can publish anything
            OpenCms.getRoleManager().addUserToRole(cms, CmsRole.PROJECT_MANAGER, username);

            // switch to the temporary user, offline project and default site
            cms.loginUser(username, password);

            storedSiteRoot = cms.getRequestContext().getSiteRoot();
            cms.getRequestContext().setSiteRoot("/sites/default/");
            cms.getRequestContext().setCurrentProject(offlineProject);

            // create a dummy plain text file by the temporary user
            cms.createResource(filename, CmsResourceTypePlain.getStaticTypeId(), content, null);
            // publish the dummy plain text file
            cms.unlockResource(filename);
            OpenCms.getPublishManager().publishResource(cms, filename);
            OpenCms.getPublishManager().waitWhileRunning();

            // switch back to the Admin user, offline project and default site
            cms.loginUser("Admin", "admin");
            cms.getRequestContext().setSiteRoot("/sites/default/");
            cms.getRequestContext().setCurrentProject(offlineProject);
            // delete the temporary user
            cms.deleteUser(username);

            // export the dummy plain text file
            List exportPaths = new ArrayList(1);
            exportPaths.add(filename);
            CmsVfsImportExportHandler vfsExportHandler = new CmsVfsImportExportHandler();
            CmsExportParameters params = new CmsExportParameters(
                zipExportFilename,
                null,
                true,
                false,
                false,
                exportPaths,
                false,
                true,
                0,
                true,
                false,
                ExportMode.DEFAULT);
            vfsExportHandler.setExportParams(params);

            OpenCms.getImportExportManager().exportData(
                cms,
                vfsExportHandler,
                new CmsShellReport(cms.getRequestContext().getLocale()));

            // delete the dummy plain text file
            cms.lockResource(filename);
            cms.deleteResource(filename, CmsResource.DELETE_REMOVE_SIBLINGS);
            // publish the deleted dummy plain text file
            cms.unlockResource(filename);
            OpenCms.getPublishManager().publishResource(cms, filename);
            OpenCms.getPublishManager().waitWhileRunning();

            // re-import the exported dummy plain text file
            OpenCms.getImportExportManager().importData(
                cms,
                new CmsShellReport(cms.getRequestContext().getLocale()),
                new CmsImportParameters(zipExportFilename, "/", true));
        } catch (Exception e) {
            fail(e.toString());
        } finally {
            deleteFile(zipExportFilename);
            if (storedSiteRoot != null) {
                cms.getRequestContext().setSiteRoot(storedSiteRoot);
            }
        }
    }

}
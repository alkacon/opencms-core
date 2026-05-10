/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.module;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsSystemInfo;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestRunner;
import org.opencms.workplace.threads.CmsExportThread;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.core.appender.OpenCmsTestLogAppender;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

/** Tests concerning the "exclude resources" feature. */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestModuleExcludeResources extends OpenCmsTestRunner {

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
        String path = OpenCms.getSystemInfo().getPackagesRfsPath() + CmsSystemInfo.FOLDER_MODULES;
        File modulesDir = new File(path);

        if (!modulesDir.exists()) {
            System.out.println("creating directory: " + path);

            try {
                modulesDir.mkdir();
                System.out.println("created directory " + path);
            } catch (SecurityException se) {
                System.err.println("unable to create directory " + path);
                se.printStackTrace();
            }
        }

        // this test causes issues that are written to the error log channel
        OpenCmsTestLogAppender.setBreakOnError(false);
    }

    /**
     * Tests module import, export and deletion where excluded resources are relevant.
     * All actions are performed relative to the root site.
     */
    @Order(1)
    @Test
    public void testModuleExcludeResourcesRootPath() {

        testModuleExcludeResources("/");
    }

    /**
     * Tests module import, export and deletion where excluded resources are relevant.
     * All actions are performed relative to the "/sites/default".
     */
    @Order(2)
    @Test
    public void testModuleExcludeResourcesSitePath() {

        testModuleExcludeResources("/sites/default/");

    }

    @AfterAll
    void cleanModulesDir() {

        String path = OpenCms.getSystemInfo().getPackagesRfsPath() + CmsSystemInfo.FOLDER_MODULES;
        File modulesDir = new File(path);

        if (modulesDir.exists()) {
            System.out.println("removing directory: " + path);

            try {
                FileUtils.deleteDirectory(modulesDir);
                System.out.println("created directory " + path);
            } catch (IOException se) {
                System.err.println("unable to create directory " + path);
                se.printStackTrace();
            }
        }
        OpenCmsTestLogAppender.setBreakOnError(true);
    }

    /** The test method used by both tests of the class.
     * It imports, exports and deletes modules and checks if the exclude resources set for one module
     * do the job correctly.
     *
     * @param siteRoot the site root, relative to which the modules are imported, exported and deleted.
     */
    private void testModuleExcludeResources(String siteRoot) {

        try {

            CmsObject cms = OpenCms.initCmsObject(getCmsObject());
            cms.getRequestContext().setSiteRoot(siteRoot);

            CmsModuleManager moduleManager = OpenCms.getModuleManager();

            String excludedResource = "/moduleresource/exclude/";
            String includedResource = "/moduleresource/subresource/";

            echo("Test module with exclude resources for site \"" + siteRoot + "\"");

            String mainModule = "org.opencms.test.modules.excluderesource.main";

            importModule(cms, mainModule);

            String subModule = "org.opencms.test.modules.excluderesource.sub";

            importModule(cms, subModule);

            assertTrue(
                cms.existsResource(excludedResource),
                "After sub-module import, the expected resource " + excludedResource + "does not exist.");

            // export the main module
            CmsModule module = OpenCms.getModuleManager().getModule(mainModule);
            CmsModuleImportExportHandler exportHandler = CmsModuleImportExportHandler.getExportHandler(
                cms,
                module,
                "Exporting " + mainModule);
            CmsExportThread exportThread = new CmsExportThread(cms, exportHandler, false);

            exportThread.start();

            exportThread.join();

            moduleManager.deleteModule(cms, mainModule, false, new CmsShellReport(cms.getRequestContext().getLocale()));

            assertTrue(
                !cms.existsResource(includedResource),
                "After main-module deletion, the resource " + includedResource + "is still present.");
            assertTrue(
                cms.existsResource(excludedResource),
                "After main-module deletion, the expected resource " + excludedResource + "does not exist anymore.");

            OpenCms.getModuleManager().deleteModule(
                cms,
                subModule,
                false,
                new CmsShellReport(cms.getRequestContext().getLocale()));

            assertTrue(
                !cms.existsResource(excludedResource),
                "After sub-module deletion, the resource " + excludedResource + "is still present.");

            importModule(cms, mainModule, "_0.1", "modules");

            assertTrue(
                !cms.existsResource(excludedResource),
                "After main-module re-import, the resource " + excludedResource + "is present again.");

            OpenCms.getModuleManager().deleteModule(
                cms,
                mainModule,
                false,
                new CmsShellReport(cms.getRequestContext().getLocale()));

        } catch (Exception e) { //CmsException or InterruptedException
            e.printStackTrace();
            fail("Exception: " + e.getStackTrace());
        }
    }

}

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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.module;

import org.opencms.file.CmsObject;
import org.opencms.importexport.CmsImportParameters;
import org.opencms.main.CmsException;
import org.opencms.main.CmsSystemInfo;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestLogAppender;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.workplace.threads.CmsExportThread;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/** Tests concerning the "exclude resources" feature. */
public class TestModuleExcludeResources extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestModuleExcludeResources(String arg0) {

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
        suite.setName(TestModuleExcludeResources.class.getName());

        suite.addTest(new TestModuleExcludeResources("testModuleExcludeResourcesRootPath"));
        suite.addTest(new TestModuleExcludeResources("testModuleExcludeResourcesSitePath"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/");
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

            @Override
            protected void tearDown() {

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

                removeOpenCms();

            }
        };

        return wrapper;
    }

    /**
     * Tests module import, export and deletion where excluded resources are relevant.
     * All actions are performed relative to the root site.
     */
    public void testModuleExcludeResourcesRootPath() {

        testModuleExcludeResources("/");
    }

    /**
     * Tests module import, export and deletion where excluded resources are relevant.
     * All actions are performed relative to the "/sites/default".
     */
    public void testModuleExcludeResourcesSitePath() {

        testModuleExcludeResources("/sites/default/");

    }

    /** Helper for importing modules from the package path.
     * The module .zip files must be named <code>modulename + ".zip"</code>.
     * @param cms the {@link CmsObject} used for the import
     * @param moduleName the name of the module to import
     * @return <code>true</code> if the import was successful, otherwise <code>false</code>.
     * @throws CmsException can be thrown for several reasons
     */
    private boolean importModule(CmsObject cms, String moduleName) throws CmsException {

        return importModule(cms, moduleName, "", "");
    }

    /** Helper for importing modules.
     * Modules are imported from the package path's subfolder specified by <code>subfolder</code>.
     * The module's .zip file must be named <code>moduleName + suffix + ".zip"</code>
     * @param cms the {@link CmsObject} used for the import
     * @param moduleName the name of the module to import
     * @param suffix a suffix on the module's .zip file, that has to be named <code>moduleName + suffix + ".zip"</code>
     * @param subfolder a subfolder of the package path, where the module .zip is located (without leading and trailing "/")
     * @return <code>true</code> if the import was successful, otherwise <code>false</code>.
     * @throws CmsException can be thrown for several reasons
     */
    private boolean importModule(CmsObject cms, String moduleName, String suffix, String subfolder)
    throws CmsException {

        echo("Importing module " + moduleName + "...");

        String moduleFile = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            "packages/" + subfolder + "/" + moduleName + suffix + ".zip");
        OpenCms.getImportExportManager().importData(
            cms,
            new CmsShellReport(cms.getRequestContext().getLocale()),
            new CmsImportParameters(moduleFile, "/", true));

        // basic check if the module was imported correctly
        if (!OpenCms.getModuleManager().hasModule(moduleName)) {
            fail("Module '" + moduleName + "' was not imported!");
            return false;
        }
        return true;

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
                "After sub-module import, the expected resource " + excludedResource + "does not exist.",
                cms.existsResource(excludedResource));

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
                "After main-module deletion, the resource " + includedResource + "is still present.",
                !cms.existsResource(includedResource));
            assertTrue(
                "After main-module deletion, the expected resource " + excludedResource + "does not exist anymore.",
                cms.existsResource(excludedResource));

            OpenCms.getModuleManager().deleteModule(
                cms,
                subModule,
                false,
                new CmsShellReport(cms.getRequestContext().getLocale()));

            assertTrue(
                "After sub-module deletion, the resource " + excludedResource + "is still present.",
                !cms.existsResource(excludedResource));

            importModule(cms, mainModule, "_0.1", "modules");

            assertTrue(
                "After main-module re-import, the resource " + excludedResource + "is present again.",
                !cms.existsResource(excludedResource));

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

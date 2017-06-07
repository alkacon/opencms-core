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

package org.opencms.module;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.importexport.CmsImportParameters;
import org.opencms.lock.CmsLockException;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule.ExportMode;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for issues found in the new module mechanism.<p>
 */
public class TestModuleIssues extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestModuleIssues(String arg0) {

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
        suite.setName(TestModuleIssues.class.getName());

        suite.addTest(new TestModuleIssues("testAdditionalSystemFolder"));
        suite.addTest(new TestModuleIssues("testModuleDeletion"));

        // important: this must be the last called method since the OpenCms installation is removed from there
        suite.addTest(new TestModuleIssues("testShutdownMethod"));

        TestSetup wrapper = new TestSetup(suite) {

            /**
             * @see junit.extensions.TestSetup#setUp()
             */
            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/");
            }

            /**
             * @see junit.extensions.TestSetup#tearDown()
             */
            @Override
            protected void tearDown() {

                // done in "testShutdownMethod"
                // removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Issue: Additional "system" folder created in current site after module import.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testAdditionalSystemFolder() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing for additional 'system' folder after module import");

        String moduleName = "org.opencms.test.modules.test3";
        String moduleFile = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            "packages/" + moduleName + ".zip");
        OpenCms.getImportExportManager().importData(
            cms,
            new CmsShellReport(cms.getRequestContext().getLocale()),
            new CmsImportParameters(moduleFile, "/", true));

        // basic check if the module was imported correctly
        if (!OpenCms.getModuleManager().hasModule(moduleName)) {
            fail("Module '" + moduleName + "' was not imported!");
        }

        cms.getRequestContext().setSiteRoot("/");
        boolean found = true;
        try {
            cms.readFolder("/sites/default/system/");
        } catch (CmsVfsResourceNotFoundException e) {
            // this is the expected result
            found = false;
        }

        if (found) {
            fail("Additional 'system' folder was created!");
        }
    }

    /**
     * Issue: Module can not be deleted if there are locked resources.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testModuleDeletion() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing module deletion with locked resources");

        // create the resources for the test
        String folderName = "/testModuleDeletion/";
        String fileOneName = folderName + "fileOne.txt";
        String fileTwoName = folderName + "fileTwo.txt";

        cms.createResource(folderName, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.createResource(fileOneName, CmsResourceTypePlain.getStaticTypeId(), "first test file".getBytes(), null);
        cms.createResource(fileTwoName, CmsResourceTypePlain.getStaticTypeId(), "second test file".getBytes(), null);

        cms.unlockResource(folderName);

        // create module
        String moduleName = "org.opencms.test.modules.testModuleDeletion";
        List resources = new ArrayList();
        resources.add(folderName);
        CmsModule module = new CmsModule(
            moduleName,
            "test",
            "test",
            null,
            null,
            null,
            ExportMode.DEFAULT,
            "test",
            new CmsModuleVersion("0.0.1"),
            "test",
            "test@test.com",
            System.currentTimeMillis(),
            cms.getRequestContext().getCurrentUser().getName(),
            System.currentTimeMillis(),
            null,
            null,
            resources,
            null,
            null);
        OpenCms.getModuleManager().addModule(cms, module);

        // lock a module file by other user
        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        cms.lockResource(fileOneName);

        // now try to delete
        cms = getCmsObject();
        I_CmsReport report = new CmsShellReport(Locale.ENGLISH);
        try {
            OpenCms.getModuleManager().deleteModule(cms, moduleName, false, report);
            fail("it should not be possible to delete a module containing a file locked by other user");
        } catch (CmsLockException e) {
            // ok, ignore
        }
        // the report error, and the module files, and the module itself
        if (!report.hasError()
            || !cms.existsResource(fileOneName)
            || !OpenCms.getModuleManager().hasModule(moduleName)) {
            fail("it should not be possible to delete a module containing a file locked by other user");
        }

        // lock a super module folder by other user
        cms.loginUser("test1", "test1");
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        cms.lockResource("/");

        // now try to delete again
        cms = getCmsObject();
        report = new CmsShellReport(Locale.ENGLISH);
        try {
            OpenCms.getModuleManager().deleteModule(cms, moduleName, false, report);
            fail("it should not be possible to delete a module when a super folder is locked by other user");
        } catch (CmsLockException e) {
            // ok, ignore
        }
        // the report error, and the module files, and the module itself
        if (!report.hasError()
            || !cms.existsResource(fileOneName)
            || !OpenCms.getModuleManager().hasModule(moduleName)) {
            fail("it should not be possible to delete a module when a super folder is locked by other user");
        }

        // lock a super module folder by the same user
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        cms.changeLock("/");

        // now try to delete again
        cms = getCmsObject();
        report = new CmsShellReport(Locale.ENGLISH);
        try {
            OpenCms.getModuleManager().deleteModule(cms, moduleName, false, report);
            fail("it should not be possible to delete a module when a super folder is locked by the same user");
        } catch (CmsLockException e) {
            // ok, ignore
        }
        // the report error, and the module files, and the module itself
        if (!report.hasError()
            || !cms.existsResource(fileOneName)
            || !OpenCms.getModuleManager().hasModule(moduleName)) {
            fail("it should not be possible to delete a module when a super folder is locked by the same user");
        }

        // lock only module resources by the same user
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        cms.unlockResource("/");
        cms.lockResource(fileTwoName);

        // now try to delete again
        cms = getCmsObject();
        report = new CmsShellReport(Locale.ENGLISH);
        OpenCms.getModuleManager().deleteModule(cms, moduleName, false, report);
        // the report error, and the module files, and the module itself
        if (report.hasError() || cms.existsResource(fileOneName) || OpenCms.getModuleManager().hasModule(moduleName)) {
            fail("it should be possible to delete a module containing a file locked by the same user");
        }
    }

    /**
     * Issue: Shutdown method never called on module.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testShutdownMethod() throws Exception {

        echo("Testing module shutdown method");

        String moduleName = "org.opencms.configuration.TestModule1";

        // basic check if the module was imported correctly (during configuration)
        if (!OpenCms.getModuleManager().hasModule(moduleName)) {
            fail("Module '" + moduleName + "' was not imported!");
        }

        I_CmsModuleAction actionInstance = OpenCms.getModuleManager().getModule(moduleName).getActionInstance();

        if (actionInstance == null) {
            fail("Module '" + moduleName + "' has no action instance!");
        }

        if (!(actionInstance instanceof TestModuleActionImpl)) {
            fail("Module '" + moduleName + "' has action class of unexpected type!");
        }

        // remove OpenCms installations, must call shutdown
        removeOpenCms();

        // check if shutdown flag was set to "true"
        assertTrue(TestModuleActionImpl.m_shutDown);

        // reset flag for next test
        TestModuleActionImpl.m_shutDown = false;
    }
}

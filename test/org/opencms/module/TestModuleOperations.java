/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.module;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.file.CmsObject;
import org.opencms.file.types.CmsResourceTypeUnknown;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.importexport.CmsImportParameters;
import org.opencms.loader.CmsDumpLoader;
import org.opencms.lock.CmsLockException;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.security.CmsSecurityException;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestLogAppender;
import org.opencms.test.OpenCmsTestProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for OpenCms module operations.<p>
 */
public class TestModuleOperations extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestModuleOperations(String arg0) {

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
        suite.setName(TestModuleOperations.class.getName());

        suite.addTest(new TestModuleOperations("testModuleImport"));
        suite.addTest(new TestModuleOperations("testModuleExport"));
        suite.addTest(new TestModuleOperations("testOldModuleImport"));
        suite.addTest(new TestModuleOperations("testModuleImportConflictId"));
        suite.addTest(new TestModuleOperations("testModuleUpdateWithResourceId"));
        suite.addTest(new TestModuleOperations("testModuleImportMissingResTypeClass"));
        suite.addTest(new TestModuleOperations("testModuleDependencies"));
        suite.addTest(new TestModuleOperations("testModuleAdditionalResourcesWorkaround"));
        suite.addTest(new TestModuleOperations("testModuleActionClass"));

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
     * Tests a module action class.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testModuleActionClass() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing module action class");

        String moduleName = "org.opencms.configuration.TestModule1";

        // basic check if the module was imported correctly (during configuration)
        if (!OpenCms.getModuleManager().hasModule(moduleName)) {
            fail("Module '" + moduleName + "' was not imported!");
        }

        CmsModule module = OpenCms.getModuleManager().getModule(moduleName);
        I_CmsModuleAction actionInstance = module.getActionInstance();

        if (actionInstance == null) {
            fail("Module '" + moduleName + "' has no action instance!");
        }

        if (!(actionInstance instanceof TestModuleActionImpl)) {
            fail("Module '" + moduleName + "' has action class of unexpected type!");
        }

        // since module is configured by default, initialize must have been already called
        assertEquals(true, TestModuleActionImpl.m_initialize);
        // since something was published during setup, module method must habe been called
        assertEquals(true, TestModuleActionImpl.m_publishProject);
        // other values should not have been changed
        assertEquals(false, TestModuleActionImpl.m_moduleUpdate);
        assertEquals(false, TestModuleActionImpl.m_moduleUninstall);
        assertEquals(false, TestModuleActionImpl.m_shutDown);
        // reset other module action values
        TestModuleActionImpl.m_cmsEvent = -1;
        TestModuleActionImpl.m_publishProject = false;

        // publish the current project
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
        assertEquals(true, TestModuleActionImpl.m_publishProject);
        assertTrue(TestModuleActionImpl.m_cmsEvent == I_CmsEventListener.EVENT_PUBLISH_PROJECT);

        // update the module
        CmsModule newModule = new CmsModule(
            module.getName(),
            module.getGroup(),
            module.getDescription(),
            module.getActionClass(),
            module.getImportScript(),
            module.getDescription(),
            module.getVersion(),
            module.getAuthorName(),
            module.getAuthorEmail(),
            module.getDateCreated(),
            module.getUserInstalled(),
            module.getDateInstalled(),
            module.getDependencies(),
            module.getExportPoints(),
            module.getResources(),
            module.getParameters());

        // update the module
        OpenCms.getModuleManager().updateModule(cms, newModule);
        assertEquals(true, TestModuleActionImpl.m_moduleUpdate);

        // make sure we are in the "Offline" project
        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));
        assertEquals("Offline", cms.getRequestContext().getCurrentProject().getName());

        // delete the module
        OpenCms.getModuleManager().deleteModule(
            cms,
            module.getName(),
            false,
            new CmsShellReport(cms.getRequestContext().getLocale()));
        assertEquals(true, TestModuleActionImpl.m_moduleUninstall);

        // reset module action values
        TestModuleActionImpl.m_cmsEvent = -1;
        TestModuleActionImpl.m_publishProject = false;

        // publish the current project 
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
        // since module was uninstalled, no update on action class must have happend
        assertEquals(false, TestModuleActionImpl.m_publishProject);
        assertTrue(TestModuleActionImpl.m_cmsEvent == -1);
    }

    /**
     * Tests a the "additionalresources" workaround.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testModuleAdditionalResourcesWorkaround() throws Throwable {

        CmsObject cms = getCmsObject();
        String moduleName = "org.opencms.test.additionalResourcesWorkaround";

        // first test - conversion of resources to "additionalresources"
        String res1 = "/system/modules/tests/test1/";
        String res2 = "/system/modules/tests/test2/";
        String res3 = "/system/modules/tests/test3/";
        String res4 = "/system/modules/tests/test4/";
        String res5 = "/system/modules/tests/test5/";

        List<String> resources = new ArrayList<String>();
        resources.add(res1);
        resources.add(res2);
        resources.add(res3);
        resources.add(res4);

        String sep = ";";
        String additionalResources = res1 + sep + res2 + sep + res3 + sep + res4;

        CmsModule module1;

        //  test - set resources based on "additionalresources"
        additionalResources += sep + "-" + sep + res5;

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("additionalresources", additionalResources);

        module1 = new CmsModule(
            moduleName,
            "A test module for additioanlresources",
            "ModuleGroup",
            null,
            null,
            null,
            new CmsModuleVersion("1.0"),
            "Alexander Kandzior",
            "alex@opencms.org",
            System.currentTimeMillis(),
            null,
            0L,
            null,
            null,
            null,
            parameters);

        OpenCms.getModuleManager().addModule(cms, module1);
        module1 = OpenCms.getModuleManager().getModule(moduleName);

        assertTrue(module1.getResources().size() == 5);
        resources = module1.getResources();
        assertTrue(resources.contains(res1));
        assertTrue(resources.contains(res2));
        assertTrue(resources.contains(res3));
        assertTrue(resources.contains(res4));
        assertTrue(resources.contains(res5));

    }

    /**
     * Tests a module import.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testModuleDependencies() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing module dependencies");

        String moduleName = "org.opencms.test.modules.test2";

        String moduleFile = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            "packages/" + moduleName + ".zip");

        boolean caughtException = false;
        try {
            OpenCms.getImportExportManager().importData(
                cms,
                new CmsShellReport(cms.getRequestContext().getLocale()),
                new CmsImportParameters(moduleFile, "/", true));
        } catch (CmsException e) {
            // any other CmsException means test failure
            caughtException = true;
        }

        // basic check if the module was _not_ imported
        if (!caughtException || OpenCms.getModuleManager().hasModule(moduleName)) {
            fail("Module '" + moduleName + "' was imported even though depdencies are not fullfilled!");
        }

        CmsModule module;
        module = CmsModuleImportExportHandler.readModuleFromImport(moduleFile);

        List<CmsModuleDependency> dependencies = OpenCms.getModuleManager().checkDependencies(
            module,
            CmsModuleManager.DEPENDENCY_MODE_IMPORT);

        if (dependencies.size() != 2) {
            fail("Module '" + moduleName + "' has 2 dependencies, not " + dependencies.size());
        }

        CmsModuleDependency dep1 = new CmsModuleDependency("org.opencms.test.dependency1", new CmsModuleVersion("1.0"));
        CmsModuleDependency dep2 = new CmsModuleDependency("org.opencms.test.dependency2", new CmsModuleVersion("2.0"));

        if (!dependencies.contains(dep1)) {
            fail("Missing required dependency: " + dep1);
        }
        if (!dependencies.contains(dep2)) {
            fail("Missing required dependency: " + dep2);
        }

        // add one (of two) depdendencies, with different but o.k. version number
        addDependency(cms, new CmsModuleDependency(dep1.getName(), new CmsModuleVersion("2.5")));

        dependencies = OpenCms.getModuleManager().checkDependencies(module, CmsModuleManager.DEPENDENCY_MODE_IMPORT);

        if (dependencies.size() != 1) {
            fail("Module '" + moduleName + "' still needs 1 dependency, not " + dependencies.size());
        }

        if (!dependencies.contains(dep2)) {
            fail("Missing required dependency: " + dep2);
        }

        // add depdendency with wrong version
        CmsModuleDependency dep3 = new CmsModuleDependency("org.opencms.test.dependency2", new CmsModuleVersion("1.0"));
        addDependency(cms, dep3);

        dependencies = OpenCms.getModuleManager().checkDependencies(module, CmsModuleManager.DEPENDENCY_MODE_IMPORT);

        if (dependencies.size() != 1) {
            fail("Module '" + moduleName + "' still needs 1 dependency, not " + dependencies.size());
        }

        if (!dependencies.contains(dep2)) {
            fail("Missing required dependency: " + dep2);
        }

        // finally add depdendency with right version
        addDependency(cms, dep2);

        dependencies = OpenCms.getModuleManager().checkDependencies(module, CmsModuleManager.DEPENDENCY_MODE_IMPORT);

        if (dependencies.size() != 0) {
            fail("Module '" + moduleName + "' must have dependencies fullfilled");
        }

        // now try the import again, this time it must work
        OpenCms.getImportExportManager().importData(
            cms,
            new CmsShellReport(cms.getRequestContext().getLocale()),
            new CmsImportParameters(moduleFile, "/", true));

        module = OpenCms.getModuleManager().getModule(moduleName);
        // check the module data
        assertEquals(module.getNiceName(), "OpenCms configuration test module 2");
        assertEquals(module.getDescription(), "Test 2 for the OpenCms module import");
        assertEquals(module.getVersion(), new CmsModuleVersion("1.5"));
        assertTrue(module.getActionClass() == null);
        assertEquals(module.getAuthorName(), "Alexander Kandzior");
        assertEquals(module.getAuthorEmail(), "alex@opencms.org");
        assertEquals(module.getExportPoints().size(), 2);
        assertEquals(module.getResources().size(), 1);
        assertEquals(module.getResources().get(0), "/system/modules/org.opencms.test.modules.test2/");
        assertEquals(module.getParameter("param1"), "value1");
        assertEquals(module.getParameter("param2"), "value2");

        // now try to delete the dependencies, this must generate an error
        caughtException = false;
        try {
            OpenCms.getModuleManager().deleteModule(
                cms,
                dep1.getName(),
                false,
                new CmsShellReport(cms.getRequestContext().getLocale()));
        } catch (CmsConfigurationException e) {
            caughtException = true;
        }
        if (!caughtException) {
            fail("Deleting '" + dep1.getName() + "' must generate an error");
        }
        caughtException = false;
        try {
            OpenCms.getModuleManager().deleteModule(
                cms,
                dep2.getName(),
                false,
                new CmsShellReport(cms.getRequestContext().getLocale()));
        } catch (CmsConfigurationException e) {
            caughtException = true;
        }
        if (!caughtException) {
            fail("Deleting '" + dep2.getName() + "' must generate an error");
        }

        CmsModuleDependency dep4 = new CmsModuleDependency(
            "org.opencms.test.modules.test2",
            new CmsModuleVersion("1.5"));

        module = OpenCms.getModuleManager().getModule(dep1.getName());
        dependencies = OpenCms.getModuleManager().checkDependencies(module, CmsModuleManager.DEPENDENCY_MODE_DELETE);
        if (!dependencies.contains(dep4)) {
            fail("Dependency not checked: " + dep4);
        }

        module = OpenCms.getModuleManager().getModule(dep2.getName());
        dependencies = OpenCms.getModuleManager().checkDependencies(module, CmsModuleManager.DEPENDENCY_MODE_DELETE);
        if (!dependencies.contains(dep4)) {
            fail("Dependency not checked: " + dep4);
        }

        // delete the imported module
        OpenCms.getModuleManager().deleteModule(
            cms,
            moduleName,
            false,
            new CmsShellReport(cms.getRequestContext().getLocale()));

        // delete the dependencies (must work now since dependency is removed)
        OpenCms.getModuleManager().deleteModule(
            cms,
            dep1.getName(),
            false,
            new CmsShellReport(cms.getRequestContext().getLocale()));
        OpenCms.getModuleManager().deleteModule(
            cms,
            dep2.getName(),
            false,
            new CmsShellReport(cms.getRequestContext().getLocale()));

        // publish the current project
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
    }

    /**
     * Tests a module export (and then re-import).<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testModuleExport() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing export an re-import of a module");

        String moduleName = "org.opencms.test.modules.testExport";

        String moduleFile = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            "packages/" + moduleName + ".zip");
        File file = new File(moduleFile);
        if (file.exists()) {
            // probably from a old test run that didn't end with a success
            file.delete();
        }

        CmsModule module = new CmsModule(
            moduleName,
            "A test module for export",
            "ModuleGroup",
            null,
            null,
            "This is the description",
            new CmsModuleVersion("1.0"),
            "Alexander Kandzior",
            "alex@opencms.org",
            System.currentTimeMillis(),
            null,
            0L,
            null,
            null,
            null,
            null);

        // add the module to the module manager
        OpenCms.getModuleManager().addModule(cms, module);

        // basic check if the module was created correctly
        if (!OpenCms.getModuleManager().hasModule(moduleName)) {
            fail("Module '" + moduleName + "' was not created!");
        }

        // generate a module export handler
        CmsModuleImportExportHandler moduleExportHandler = new CmsModuleImportExportHandler();
        moduleExportHandler.setFileName(moduleFile);
        moduleExportHandler.setAdditionalResources(new String[0]);
        moduleExportHandler.setModuleName(moduleName.replace('\\', '/'));
        moduleExportHandler.setDescription("Module export of " + moduleExportHandler.getModuleName());

        // export the module
        OpenCms.getImportExportManager().exportData(
            cms,
            moduleExportHandler,
            new CmsShellReport(cms.getRequestContext().getLocale()));

        // now delete the module from the manager
        OpenCms.getModuleManager().deleteModule(
            cms,
            moduleName,
            false,
            new CmsShellReport(cms.getRequestContext().getLocale()));

        // ensure that the module was deleted
        if (OpenCms.getModuleManager().hasModule(moduleName)) {
            fail("Module '" + moduleName + "' was not deleted!");
        }

        // now import the module again
        OpenCms.getImportExportManager().importData(
            cms,
            new CmsShellReport(cms.getRequestContext().getLocale()),
            new CmsImportParameters(moduleFile, "/", true));

        // basic check if the module was imported correctly
        if (!OpenCms.getModuleManager().hasModule(moduleName)) {
            fail("Module '" + moduleName + "' was not imported!");
        }

        CmsModule importedModule = OpenCms.getModuleManager().getModule(moduleName);
        // now check the other module data
        if (!module.isIdentical(importedModule)) {
            fail("Impoted module not identical to original module!");
        }

        if (file.exists()) {
            // clean up
            file.delete();
        } else {
            fail("Module export file was not written to expected location!");
        }
    }

    /**
     * Tests a module import.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testModuleImport() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing import of a module");

        String moduleName = "org.opencms.test.modules.test1";

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

        CmsModule module = OpenCms.getModuleManager().getModule(moduleName);
        // now check the other module data
        assertEquals(module.getNiceName(), "OpenCms configuration test module 1");
        assertEquals(module.getDescription(), "Test 1 for the OpenCms module import");
        assertEquals(module.getVersion(), new CmsModuleVersion("1.0"));
        assertTrue(module.getActionClass() == null);
        assertEquals(module.getAuthorName(), "Alexander Kandzior");
        assertEquals(module.getAuthorEmail(), "alex@opencms.org");
        assertEquals(module.getExportPoints().size(), 2);
        assertEquals(module.getResources().size(), 1);
        assertEquals(module.getResources().get(0), "/system/modules/org.opencms.test.modules.test1/");
        assertEquals(module.getParameter("param1"), "value1");
        assertEquals(module.getParameter("param2"), "value2");
    }

    /**
     * Tests the import of a module that has a duplicate id.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testModuleImportConflictId() throws Exception {

        // this test imports a module with the id "12" and name "article"
        // id 12 is already configured for "tablegallery"

        CmsObject cms = getCmsObject();
        echo("Testing import of a module with a conflicting id");

        String moduleName = "org.opencms.test.modules.testConflictId";

        String moduleFile = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            "packages/" + moduleName + ".zip");
        try {
            OpenCms.getImportExportManager().importData(
                cms,
                new CmsShellReport(cms.getRequestContext().getLocale()),
                new CmsImportParameters(moduleFile, "/", true));
        } catch (CmsConfigurationException e) {
            // this is the expected exception - the module should not have been imported
        }

        // check if the module was not imported
        if (OpenCms.getModuleManager().hasModule(moduleName)) {
            fail("Module '" + moduleName + "' was imported, but should not have been because of id conflicts!");
        }

        // next test: try a module that has an internal id conflict
        echo("Testing import of a module with internal conflicting ids");

        moduleName = "org.opencms.test.modules.testConflictId2";
        moduleFile = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf("packages/" + moduleName + ".zip");

        try {
            OpenCms.getImportExportManager().importData(
                cms,
                new CmsShellReport(cms.getRequestContext().getLocale()),
                new CmsImportParameters(moduleFile, "/", true));
        } catch (CmsConfigurationException e) {
            // this is the expected exception - the module should not have been imported
        }

        // check if the module was not imported
        if (OpenCms.getModuleManager().hasModule(moduleName)) {
            fail("Module '" + moduleName + "' was imported, but should not have been because of id conflicts!");
        }
    }

    /**
     * Tests a module import with an unknown resource type class.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testModuleImportMissingResTypeClass() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing a module import with an unknown resource type class");

        String moduleName = "org.opencms.test.modules.test4";

        String moduleFile = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            "packages/" + moduleName + ".zip");

        printExceptionWarning();

        try {
            // importing this module will generate an expected error in the log
            OpenCmsTestLogAppender.setBreakOnError(false);
            OpenCms.getImportExportManager().importData(
                cms,
                new CmsShellReport(cms.getRequestContext().getLocale()),
                new CmsImportParameters(moduleFile, "/", true));
        } finally {
            OpenCmsTestLogAppender.setBreakOnError(true);
        }

        // basic check if the module was imported correctly
        if (!OpenCms.getModuleManager().hasModule(moduleName)) {
            fail("Module '" + moduleName + "' was not imported!");
        }

        CmsModule module = OpenCms.getModuleManager().getModule(moduleName);
        // now check the other module data
        assertEquals(module.getNiceName(), "OpenCms configuration test module 4");
        assertEquals(module.getDescription(), "Test 4 for the OpenCms module import: Missing classes");
        assertEquals(module.getVersion(), new CmsModuleVersion("1.0"));
        assertEquals(module.getActionClass(), "org.opencms.missing.moduleClass");
        assertEquals(module.getAuthorName(), "Alexander Kandzior");
        assertEquals(module.getAuthorEmail(), "alex@opencms.org");
        assertEquals(module.getExportPoints().size(), 0);
        assertEquals(module.getResources().size(), 0);
        assertEquals(module.getParameter("param1"), "value1");
        assertEquals(module.getParameter("param2"), "value2");

        // check for the new resource type
        I_CmsResourceType missingType = OpenCms.getResourceManager().getResourceType("missing");
        assertNotNull(missingType);
        // check configured and actual class name
        assertEquals(missingType.getClassName(), "org.opencms.missing.resourceTypeClass");
        assertEquals(missingType.getTypeId(), 88);
        assertEquals(missingType.getLoaderId(), CmsDumpLoader.RESOURCE_LOADER_ID);
        assertEquals(missingType.getClass().getName(), CmsResourceTypeUnknown.class.getName());
    }

    /**
     * Tests a update of a module that contains a new resource type.<p>
     * 
     * This test was added because there was an issue where modules with a resource 
     * type generated an error "conficting id" during update.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testModuleUpdateWithResourceId() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing update of a module that contains a resource type id definition");

        String moduleName = "org.opencms.test.modules.testConflictIdUpdate";

        String moduleFile = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            "packages/" + moduleName + ".zip");

        // now import the module first time - this must work
        OpenCms.getImportExportManager().importData(
            cms,
            new CmsShellReport(cms.getRequestContext().getLocale()),
            new CmsImportParameters(moduleFile, "/", true));

        // basic check if the module was imported correctly
        if (!OpenCms.getModuleManager().hasModule(moduleName)) {
            fail("Module '" + moduleName + "' was not imported!");
        }

        // update the module - this is a "delete" and then "import again" operation

        // first delete the module - marked as "true" for update
        OpenCms.getModuleManager().deleteModule(
            cms,
            moduleName,
            true,
            new CmsShellReport(cms.getRequestContext().getLocale()));

        // basic check if the module was deletet correctly
        if (OpenCms.getModuleManager().hasModule(moduleName)) {
            fail("Module '" + moduleName + "' was not deleted!");
        }

        // now import the module again second time
        OpenCms.getImportExportManager().importData(
            cms,
            new CmsShellReport(cms.getRequestContext().getLocale()),
            new CmsImportParameters(moduleFile, "/", true));

        // basic check if the module was imported correctly again
        if (!OpenCms.getModuleManager().hasModule(moduleName)) {
            fail("Module '" + moduleName + "' was not imported!");
        }
    }

    /**
     * Tests a module import of an old (OpenCms 5.0) style module.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    public void testOldModuleImport() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing import of an old OpenCms 5.0 module");

        String moduleName = "org.opencms.test.modules.testOld";

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

        CmsModule module = OpenCms.getModuleManager().getModule(moduleName);
        // now check the other module data
        assertEquals(module.getNiceName(), "OpenCms v5.0 test module");
        assertEquals(
            module.getDescription().trim(),
            "This is a module in the OpenCms v5.0.x style.\n\t\tThe XML format for modules has changed in OpenCms 6.0.");
        assertEquals(module.getVersion(), new CmsModuleVersion("2.05"));
        assertTrue(module.getActionClass() == null);
        assertEquals(module.getAuthorName(), "Alexander Kandzior");
        assertEquals(module.getAuthorEmail(), "alex@opencms.org");
        // check if "additionalresources" where converted to module resources        
        assertTrue(module.getResources().size() == 2);
        assertEquals(module.getResources().get(0), "/system/modules/org.opencms.test.modules.testOld/");
        assertEquals(module.getResources().get(1), "/alkacon-documentation/documentation-flexcache/");
    }

    /**
     * Adds a module dependency for the tests.<p>
     * 
     * @param cms the current OpenCms context
     * @param dep the dependency to check
     * 
     * @throws CmsConfigurationException in case something goes wrong
     * @throws CmsSecurityException in case something goes wrong
     * @throws CmsLockException if the module resources can not be locked
     */
    private void addDependency(CmsObject cms, CmsModuleDependency dep)
    throws CmsSecurityException, CmsConfigurationException, CmsLockException {

        if (OpenCms.getModuleManager().hasModule(dep.getName())) {
            // remove other version of dependency if it exists
            OpenCms.getModuleManager().deleteModule(
                cms,
                dep.getName(),
                true,
                new CmsShellReport(cms.getRequestContext().getLocale()));
        }

        CmsModule module = new CmsModule(
            dep.getName(),
            "A test module dependency",
            "ModuleGroup",
            null,
            null,
            null,
            dep.getVersion(),
            "Alexander Kandzior",
            "alex@opencms.org",
            System.currentTimeMillis(),
            null,
            0L,
            null,
            null,
            null,
            null);

        // add the module to the module manager
        OpenCms.getModuleManager().addModule(cms, module);
    }
}
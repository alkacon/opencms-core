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

import org.opencms.db.CmsExportPoint;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.importexport.CmsImportParameters;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockFilter;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestResourceConfigurableFilter;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.util.CmsZipBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import junit.framework.Test;

/**
 * Unit tests for OpenCms module  updates.<p>
 */
public class TestModuleUpdate extends OpenCmsTestCase {

    /** Module name. */
    public static final String MODULE = "org.test.foo";

    /** Base path for the module. */
    public static final String MODULE_PATH = "/system/modules/" + MODULE;

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestModuleUpdate(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);
        return generateSetupTestWrapper(TestModuleUpdate.class, "simpletest", "/");
    }

    /**
     * Creates a temporary file for module exports.<p>
     *
     * @return the created file
     * @throws IOException if something goes wrong
     */
    public File tempExport() throws IOException {

        File file = File.createTempFile("opencms-test-export_", ".zip");
        file.deleteOnExit();
        return file;
    }

    /**
     * Test case.<p>
     * @throws Exception if an error happens
     */
    public void testAcl() throws Exception {

        CmsObject cms = cms();
        removeTestModuleIfExists(cms);
        File export = null;
        // use custom resource storage so there is no interference from other tests
        newStorage();
        String username = "ModuleTestAclUser";
        cms.createUser(username, "password", "description", new HashMap<String, Object>());

        // use blocks so we don't accidentally use wrong object
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            builder.addTextFile("test.txt", "test");
            cms.chacc(MODULE_PATH + "/test.txt", "USER", username, "+w+v+c");
            cms.chacc(MODULE_PATH + "/test.txt", "USER", "ALL_OTHERS", "-r");
            builder.addTextFile("test2.txt", "test");
            builder.publish();
            export = tempExport();
            builder.export(export.getAbsolutePath());
            storeResources(cms, MODULE_PATH);
            builder.delete();
        }
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            builder.addTextFile("test.txt", "test");
            builder.addTextFile("test2.txt", "test");
            cms.chacc(MODULE_PATH + "/test2.txt", "USER", username, "+w+v+c");
            builder.publish();
        }

        CmsReplaceModuleInfo result = OpenCms.getModuleManager().replaceModule(
            cms,
            export.getAbsolutePath(),
            new CmsShellReport(Locale.ENGLISH));
        assertTrue("should have used update mechanism", result.usedUpdater());
        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();
        filter.disableProjectLastModifiedTest();
        filter.disableDateContentTest();
        filter.disableDateLastModifiedTest();
        filter.disableResourceIdTest();
        List<CmsResource> resources = new ArrayList<>();
        resources.add(cms.readResource(MODULE_PATH, CmsResourceFilter.ALL));
        resources.addAll(cms.readResources(MODULE_PATH, CmsResourceFilter.ALL, true));

        // first test that existing resources match their stored version, then check that there are no extra resources
        for (CmsResource resource : resources) {
            System.out.println("Comparing " + resource.getRootPath());
            assertFilter(cms, resource.getRootPath(), filter);
        }
        assertEquals("Resource count doesn't match", m_currentResourceStrorage.size(), resources.size());

    }

    /**
     * Test case.<p>
     * @throws Exception if an error happens
     */
    public void testExplodedModule() throws Exception {

        CmsObject cms = cms();
        removeTestModuleIfExists(cms);
        File export = null;
        // use custom resource storage so there is no interference from other tests
        newStorage();

        // use blocks so we don't accidentally use wrong object
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, "org.opencms.bar");
            builder.addExplodedModule(
                "org.opencms.bar1",
                Arrays.asList("/system/modules/org.opencms.bar/a1.txt", "/system/modules/org.opencms.bar/dir/a2.txt"));
            builder.setNextStructureId(new CmsUUID());
            builder.addFolder("");
            builder.setNextStructureId(new CmsUUID());
            builder.addFolder("dir");
            builder.addTextFile("a1.txt", "this is the modified foo file");
            builder.addTextFile("dir/a2.txt", "this is the bar file");
            builder.publish();
            export = tempExport();
            builder.export(export.getAbsolutePath());
            builder.delete();
        }

        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, "org.opencms.bar");
            builder.addExplodedModule(
                "org.opencms.bar1",
                Arrays.asList(
                    "/system/modules/org.opencms.bar/a1.txt",
                    "/system/modules/org.opencms.bar/dir/a2.txt",
                    "/system/modules/org.opencms.bar/todelete.txt"));
            builder.setNextStructureId(new CmsUUID());
            builder.addFolder("");
            builder.setNextStructureId(new CmsUUID());
            builder.addFolder("dir");
            builder.addTextFile("a1.txt", "this is the foo file");
            builder.addTextFile("todelete.txt", "todelete");
            builder.addTextFile("dir/a2.txt", "this is the bar file");
            builder.addTextFile("notinmodule.txt", "notinmodule");
            builder.publish();
        }

        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();
        filter.disableProjectLastModifiedTest();
        filter.disableDateContentTest();
        filter.disableDateLastModifiedTest();
        filter.disableResourceIdTest();

        CmsReplaceModuleInfo info = OpenCms.getModuleManager().replaceModule(
            cms,
            export.getAbsolutePath(),
            new CmsShellReport(Locale.ENGLISH));
        assertTrue("Should have used new module updater", info.usedUpdater());
        cms.readResource("/system/modules/org.opencms.bar/notinmodule.txt");
        assertFalse("file should have been deleted", cms.existsResource("/system/module/org.opencms.bar/todelete.txt"));

    }

    /**
     * Test case.<p>
     * @throws Exception if an error happens
     */
    public void testExportPoints() throws Exception {

        File target = File.createTempFile("ocms-test-exportpoint-", ".dat");
        target.delete();
        target.deleteOnExit();
        CmsObject cms = cms();
        removeTestModuleIfExists(cms);

        File export = null;

        // use blocks so we don't accidentally use wrong object
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addExportPoint(new CmsExportPoint(MODULE_PATH + "/test.txt", target.getAbsolutePath()));
            builder.addModule();
            builder.addFolder("");
            builder.addTextFile("test.txt", "new");
            builder.publish();
            export = tempExport();
            builder.export(export.getAbsolutePath());
            storeResources(cms, MODULE_PATH);
            builder.delete();
        }
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            builder.publish();
        }
        assertFalse("Export point should not exist", target.exists());
        CmsReplaceModuleInfo info = OpenCms.getModuleManager().replaceModule(
            cms,
            export.getAbsolutePath(),
            new CmsShellReport(Locale.ENGLISH));

        assertTrue("Export point has not been exported", target.exists());
        assertTrue("Module update should have been used", info.usedUpdater());
    }

    /**
     * Test case.<p>
     * @throws Exception if an error happens
     */
    public void testImportScript() throws Exception {

        CmsObject cms = cms();
        removeTestModuleIfExists(cms);
        File export = null;
        // use custom resource storage so there is no interference from other tests

        // use blocks so we don't accidentally use wrong object
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.setImportScript("createFolder \"/system/\" \"testImportScriptFolder/\"");
            builder.addModule();
            builder.addFolder("");
            builder.publish();
            export = tempExport();

            builder.export(export.getAbsolutePath());
            builder.delete();
        }
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            builder.publish();
        }

        CmsReplaceModuleInfo result = OpenCms.getModuleManager().replaceModule(
            cms,
            export.getAbsolutePath(),
            new CmsShellReport(Locale.ENGLISH));
        assertTrue("should have used update mechanism", result.usedUpdater());
        cms.readResource("/system/testImportScriptFolder");
    }

    /**
     * Test case.<p>
     * @throws Exception if an error happens
     */
    public void testModuleResourceChangeToSubfolder() throws Exception {

        CmsObject cms = cms();
        removeTestModuleIfExists(cms);
        File export1 = null;
        File export2 = null;

        CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
        builder.addModule();
        builder.addFolder("");
        builder.addFolder("folder");
        builder.addTextFile("folder/file.txt", "123");
        builder.publish();
        export1 = tempExport();
        builder.export(export1.getAbsolutePath());
        builder.updateModuleResources("/system/modules/" + MODULE + "/folder");
        export2 = tempExport();
        builder.export(export2.getAbsolutePath());
        builder.updateModuleResources("/system/modules/" + MODULE);
        builder.delete();

        CmsShellReport report = new CmsShellReport(Locale.ENGLISH);
        OpenCms.getModuleManager().replaceModule(cms, export1.getAbsolutePath(), report);
        OpenCms.getModuleManager().replaceModule(cms, export2.getAbsolutePath(), report);

        CmsResource res = cms.readResource("/system/modules/" + MODULE);
        CmsLock lock = cms.getLock(res);
        cms.lockResource(res);
        cms.deleteResource("/system/modules/" + MODULE, CmsResource.DELETE_PRESERVE_SIBLINGS);
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

    }

    /**
     * Test case.<p>
     * @throws Exception if an error happens
     */
    public void testMoveNewDelete() throws Exception {

        CmsObject cms = cms();
        removeTestModuleIfExists(cms);

        File export = null;
        // use custom resource storage so there is no interference from other tests
        newStorage();

        // use blocks so we don't accidentally use wrong object
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            builder.addFolder("dir1");
            builder.addFolder("dir2");
            builder.addTextFile("filefromdeleteddir.txt", "file from deleted dir");
            // foo.txt is the moved file, the module builder helper calculates the uuid from the file name (not path)
            builder.addTextFile("dir2/moved.txt", "foo");
            builder.addTextFile("new.txt", "new");
            builder.publish();
            export = tempExport();
            builder.export(export.getAbsolutePath());
            storeResources(cms, MODULE_PATH);
            builder.delete();
        }
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            builder.addFolder("dir1");
            builder.addFolder("dir2");
            builder.addFolder("deleteddir");
            builder.addTextFile("deleteddir/filefromdeleteddir.txt", "file from deleted dir");
            builder.addTextFile("old.txt", "old");
            builder.addTextFile("dir1/moved.txt", "foo");
            builder.publish();
        }
        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();
        filter.disableProjectLastModifiedTest();
        filter.disableDateContentTest();
        filter.disableDateLastModifiedTest();

        CmsReplaceModuleInfo result = OpenCms.getModuleManager().replaceModule(
            cms,
            export.getAbsolutePath(),
            new CmsShellReport(Locale.ENGLISH));
        assertTrue("should have used update mechanism", result.usedUpdater());
        List<CmsResource> resources = new ArrayList<>();
        resources.add(cms.readResource(MODULE_PATH, CmsResourceFilter.ALL));
        resources.addAll(cms.readResources(MODULE_PATH, CmsResourceFilter.ALL, true));

        // first test that existing resources match their stored version, then check that there are no extra resources
        for (CmsResource resource : resources) {
            System.out.println("Comparing " + resource.getRootPath());
            assertFilter(cms, resource.getRootPath(), filter);
        }
        assertEquals("Resource count doesn't match", m_currentResourceStrorage.size(), resources.size());
    }

    /**
     * Test case.<p>
     * @throws Exception if an error happens
     */
    public void testNestedMove() throws Exception {

        CmsObject cms = cms();
        removeTestModuleIfExists(cms);

        File export = null;
        // use custom resource storage so there is no interference from other tests
        newStorage();
        CmsUUID foo = new CmsUUID();
        CmsUUID page = new CmsUUID();

        // use blocks so we don't accidentally use wrong object
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            builder.setNextStructureId(foo);
            builder.setNextResourceId(foo);
            builder.addFolder("foo2");
            builder.addFolder("foo2/news");
            builder.setNextStructureId(page);
            builder.setNextResourceId(page);
            builder.addFolder("foo2/news/article");
            builder.publish();
            export = tempExport();
            builder.export(export.getAbsolutePath());
            builder.delete();
        }
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            builder.setNextStructureId(foo);
            builder.setNextResourceId(foo);
            builder.addFolder("foo1");
            builder.setNextStructureId(page);
            builder.setNextResourceId(page);
            builder.addFolder("foo1/news");
            builder.publish();
        }
        CmsReplaceModuleInfo replaceInfo = OpenCms.getModuleManager().replaceModule(
            cms,
            export.getAbsolutePath(),
            new CmsShellReport(Locale.ENGLISH));
        assertFalse("new module update mechanism should not have been used", replaceInfo.usedUpdater());

    }

    /**
     * Test case.<p>
     * @throws Exception if an error happens
     */
    public void testNewTypeWithContents() throws Exception {

        CmsObject cms = cms();
        removeTestModuleIfExists(cms);
        String typeName = "tntwc";
        int typeId = 77994;
        File[] exports = new File[] {null, null};
        for (int version : new int[] {0, 1}) {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            if (version == 1) {
                builder.addType(typeName, typeId);
            }
            builder.addModule();
            builder.addFolder("");
            builder.addTextFile("plain.txt", "test");
            if (version == 1) {
                builder.addFile(typeName, "special.txt", "special");
            }
            builder.publish();
            File exportFile = tempExport();
            builder.export(exportFile.getAbsolutePath());
            exports[version] = exportFile;
            builder.delete();
        }

        CmsShellReport report = new CmsShellReport(Locale.ENGLISH);
        OpenCms.getModuleManager().replaceModule(cms, exports[0].getAbsolutePath(), report);
        OpenCms.getModuleManager().replaceModule(cms, exports[1].getAbsolutePath(), report);
        assertEquals(typeId, cms.readResource("/system/modules/" + MODULE + "/special.txt").getTypeId());
    }

    /**
     * Test case.<p>
     * @throws Exception if an error happens
     */
    public void testParseLinks() throws Exception {

        CmsObject cms = cms();
        removeTestModuleIfExists(cms);
        File export = null;
        // use custom resource storage so there is no interference from other tests
        newStorage();
        // use blocks so we don't accidentally use wrong object
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            builder.addTextFile("test.txt", "test");
            builder.addFile("jsp", "test.jsp", "%(link.weak:" + MODULE_PATH + "/test.txt)");
            builder.publish();
            export = tempExport();
            builder.export(export.getAbsolutePath());
            builder.delete();
        }
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            builder.addTextFile("test.txt", "test");
            builder.publish();
        }

        CmsReplaceModuleInfo result = OpenCms.getModuleManager().replaceModule(
            cms,
            export.getAbsolutePath(),
            new CmsShellReport(Locale.ENGLISH));
        assertTrue("should have used update mechanism", result.usedUpdater());
        // assertTrue("New updater should have been used", info.usedUpdater());
        CmsResource testJsp = cms.readResource("/system/modules/org.test.foo/test.jsp");
        List<CmsRelation> relations = cms.readRelations(
            CmsRelationFilter.relationsFromStructureId(testJsp.getStructureId()));
        assertEquals("Should have one relation", 1, relations.size());
        CmsRelation relation = relations.get(0);
        assertEquals("/system/modules/org.test.foo/test.txt", relation.getTargetPath());

    }

    /**
     * Test case.<p>
     * @throws Exception if an error happens
     */
    public void testProperties() throws Exception {

        CmsObject cms = cms();
        removeTestModuleIfExists(cms);
        File export = null;
        // use custom resource storage so there is no interference from other tests
        newStorage();

        // use blocks so we don't accidentally use wrong object
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            builder.addTextFile("test.txt", "test");
            cms.writePropertyObject(MODULE_PATH + "/test.txt", new CmsProperty("Title", "title", null));
            cms.writePropertyObject(MODULE_PATH + "/test.txt", new CmsProperty("Description", "desc", null));
            builder.addTextFile("new.txt", "new");
            cms.writePropertyObject(MODULE_PATH + "/new.txt", new CmsProperty("Title", "title", null));
            builder.publish();
            export = tempExport();
            builder.export(export.getAbsolutePath());
            storeResources(cms, MODULE_PATH);
            builder.delete();
        }
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            builder.addTextFile("test.txt", "test");
            cms.writePropertyObject(MODULE_PATH + "/test.txt", new CmsProperty("Title", "title2", null));
            cms.writePropertyObject(MODULE_PATH + "/test.txt", new CmsProperty("template", "template", null));
            builder.publish();
        }

        CmsReplaceModuleInfo result = OpenCms.getModuleManager().replaceModule(
            cms,
            export.getAbsolutePath(),
            new CmsShellReport(Locale.ENGLISH));
        assertTrue("should have used update mechanism", result.usedUpdater());
        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();
        filter.disableProjectLastModifiedTest();
        filter.disableDateContentTest();
        filter.disableDateLastModifiedTest();

        List<CmsResource> resources = new ArrayList<>();
        resources.add(cms.readResource(MODULE_PATH, CmsResourceFilter.ALL));
        resources.addAll(cms.readResources(MODULE_PATH, CmsResourceFilter.ALL, true));

        // first test that existing resources match their stored version, then check that there are no extra resources
        for (CmsResource resource : resources) {
            System.out.println("Comparing " + resource.getRootPath());
            assertFilter(cms, resource.getRootPath(), filter);
        }
        assertEquals("Resource count doesn't match", m_currentResourceStrorage.size(), resources.size());

    }

    /**
     * Test case.<p>
     * @throws Exception if an error happens
     */
    public void testRelations() throws Exception {

        CmsObject cms = cms();
        removeTestModuleIfExists(cms);
        File export = null;
        // use custom resource storage so there is no interference from other tests
        newStorage();

        CmsResource relTarget = cms.createResource(
            "system/testRelationsTarget",
            OpenCms.getResourceManager().getResourceType("folder"));

        // use blocks so we don't accidentally use wrong object
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            CmsResource test = builder.addTextFile("test.txt", "test");
            cms.addRelationToResource(test, relTarget, "TESTRELATION1");
            CmsResource test2 = builder.addFile(
                "jsp",
                "test2.txt",
                "%(link.weak:/system/modules/org.test.foo/test.txt");
            cms.addRelationToResource(test2, relTarget, "TESTRELATION2");
            builder.publish();
            export = tempExport();
            builder.export(export.getAbsolutePath());
            storeResources(cms, MODULE_PATH);
            builder.delete();

        }
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            CmsResource test = builder.addTextFile("test.txt", "test");
            cms.addRelationToResource(test, relTarget, "TESTRELATION2");
            builder.addTextFile("test2.txt", "test");
            builder.publish();
        }

        CmsReplaceModuleInfo result = OpenCms.getModuleManager().replaceModule(
            cms,
            export.getAbsolutePath(),
            new CmsShellReport(Locale.ENGLISH));
        assertTrue("should have used update mechanism", result.usedUpdater());
        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();
        filter.disableProjectLastModifiedTest();
        filter.disableDateContentTest();
        filter.disableDateLastModifiedTest();
        //filter.disableResourceIdTest();
        List<CmsResource> resources = new ArrayList<>();
        resources.add(cms.readResource(MODULE_PATH, CmsResourceFilter.ALL));
        resources.addAll(cms.readResources(MODULE_PATH, CmsResourceFilter.ALL, true));

        // first test that existing resources match their stored version, then check that there are no extra resources
        for (CmsResource resource : resources) {
            System.out.println("Comparing " + resource.getRootPath());
            assertFilter(cms, resource.getRootPath(), filter);
        }
        assertEquals("Resource count doesn't match", m_currentResourceStrorage.size(), resources.size());

    }

    /**
     * Test case for exporting / importing relations to immutables.
     *
     * @throws Exception
     */
    public void testRelationsToImmutable() throws Exception {

        CmsObject cms = cms();

        CmsResource sysWorkplace = cms.createResource("/system/workplace", 0);
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
        removeTestModuleIfExists(cms);
        File export = null;
        CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
        builder.addModule();
        builder.addFolder("");
        CmsResource foo = builder.addTextFile("foo.txt", "text");
        cms.addRelationToResource(foo, sysWorkplace, CmsRelationType.CATEGORY.getName());
        export = tempExport();
        builder.publish();
        builder.export(export.getAbsolutePath());
        builder.delete();
        cms.lockResource(sysWorkplace);
        cms.deleteResource("/system/workplace", CmsResource.DELETE_PRESERVE_SIBLINGS);
        builder.publish();
        CmsResource sysWorkplace2 = cms.createResource("/system/workplace", 0);
        assertNotSame(sysWorkplace.getStructureId(), sysWorkplace2.getStructureId());
        builder.publish();
        OpenCms.getModuleManager().replaceModule(cms, export.getAbsolutePath(), new CmsShellReport(Locale.ENGLISH));
        List<CmsRelation> relations = cms.readRelations(
            CmsRelationFilter.relationsFromStructureId(foo.getStructureId()));
        assertEquals(1, relations.size());
        CmsRelation rel = relations.get(0);
        assertEquals(sysWorkplace2.getStructureId(), rel.getTargetId());
        builder.delete();
        cms.lockResource(sysWorkplace2);
        cms.deleteResource("/system/workplace", CmsResource.DELETE_PRESERVE_SIBLINGS);
        builder.publish();
    }

    /**
     * Test case for exporting / importing relations to immutables.
     *
     * @throws Exception
     */
    public void testRelationsToImmutable2() throws Exception {

        CmsObject cms = cms();

        CmsResource sysWorkplace = cms.createResource("/system/workplace", 0);
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();
        removeTestModuleIfExists(cms);
        File export = null;
        CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
        builder.addModule();
        builder.addFolder("");
        CmsResource foo = builder.addTextFile("foo.txt", "text");
        cms.addRelationToResource(foo, sysWorkplace, CmsRelationType.CATEGORY.getName());
        export = tempExport();
        builder.publish();
        builder.export(export.getAbsolutePath());
        cms.lockResource(sysWorkplace);
        cms.deleteResource("/system/workplace", CmsResource.DELETE_PRESERVE_SIBLINGS);
        builder.publish();
        CmsResource sysWorkplace2 = cms.createResource("/system/workplace", 0);
        assertNotSame(sysWorkplace.getStructureId(), sysWorkplace2.getStructureId());
        builder.publish();
        OpenCms.getModuleManager().replaceModule(cms, export.getAbsolutePath(), new CmsShellReport(Locale.ENGLISH));
        List<CmsRelation> relations = cms.readRelations(
            CmsRelationFilter.relationsFromStructureId(foo.getStructureId()));
        assertEquals(1, relations.size());
        CmsRelation rel = relations.get(0);
        assertEquals(sysWorkplace2.getStructureId(), rel.getTargetId());
        builder.delete();
        cms.lockResource(sysWorkplace2);
        cms.deleteResource("/system/workplace", CmsResource.DELETE_PRESERVE_SIBLINGS);
        builder.publish();
    }

    /**
     * Test case.<p>
     * @throws Exception if an error happens
     */
    public void testSiblings() throws Exception {

        CmsObject cms = cms();
        removeTestModuleIfExists(cms);

        File export = null;
        // use custom resource storage so there is no interference from other tests
        newStorage();
        CmsUUID resId = new CmsUUID();

        // use blocks so we don't accidentally use wrong object
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");

            builder.setNextResourceId(resId);
            String content = "content1";
            builder.addTextFile("file1.txt", content);
            builder.setNextResourceId(resId);
            builder.addTextFile("file2.txt", null);
            builder.publish();
            assertEquals(
                "file content doesn't match",
                "content1",
                new String(cms.readFile(MODULE_PATH + "/file1.txt").getContents(), "UTF-8"));
            assertEquals(
                "file content doesn't match",
                "content1",
                new String(cms.readFile(MODULE_PATH + "/file2.txt").getContents(), "UTF-8"));

            export = tempExport();
            builder.export(export.getAbsolutePath());
            storeResources(cms, MODULE_PATH);
            builder.delete();
        }
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            builder.setNextResourceId(resId);
            String content = "content2";
            builder.addTextFile("file1.txt", content);
            builder.setNextResourceId(resId);
            builder.addTextFile("file2.txt", null);
            assertEquals(
                "file content doesn't match",
                "content2",
                new String(cms.readFile(MODULE_PATH + "/file1.txt").getContents(), "UTF-8"));
            assertEquals(
                "file content doesn't match",
                "content2",
                new String(cms.readFile(MODULE_PATH + "/file2.txt").getContents(), "UTF-8"));

            builder.publish();
        }
        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();
        filter.disableProjectLastModifiedTest();
        filter.disableDateContentTest();
        filter.disableDateLastModifiedTest();

        CmsReplaceModuleInfo result = OpenCms.getModuleManager().replaceModule(
            cms,
            export.getAbsolutePath(),
            new CmsShellReport(Locale.ENGLISH));
        assertTrue("should have used update mechanism", result.usedUpdater());
        List<CmsResource> resources = new ArrayList<>();
        resources.add(cms.readResource(MODULE_PATH, CmsResourceFilter.ALL));
        resources.addAll(cms.readResources(MODULE_PATH, CmsResourceFilter.ALL, true));

        // first test that existing resources match their stored version, then check that there are no extra resources
        for (CmsResource resource : resources) {
            System.out.println("Comparing " + resource.getRootPath());
            assertFilter(cms, resource.getRootPath(), filter);
        }
        assertEquals("Resource count doesn't match", m_currentResourceStrorage.size(), resources.size());
    }

    /**
     * Test case.<p>
     * @throws Exception if an error happens
     */
    public void testUnlockedAndUnchanged() throws Exception {

        CmsObject cms = cms();
        removeTestModuleIfExists(cms);
        File export = null;
        // use custom resource storage so there is no interference from other tests
        newStorage();

        // use blocks so we don't accidentally use wrong object
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            builder.addTextFile("bar.txt", "this is the original bar file");
            builder.publish();
            export = tempExport();
            builder.export(export.getAbsolutePath());
            storeResources(cms, MODULE_PATH);
            builder.delete();
        }
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            builder.addTextFile("foo.txt", "this is the original foo file");
            builder.publish();
        }
        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();
        filter.disableProjectLastModifiedTest();
        filter.disableDateContentTest();
        filter.disableDateLastModifiedTest();
        filter.disableResourceIdTest();

        CmsReplaceModuleInfo result = OpenCms.getModuleManager().replaceModule(
            cms,
            export.getAbsolutePath(),
            new CmsShellReport(Locale.ENGLISH));
        assertTrue("should have used update mechanism", result.usedUpdater());
        CmsResource mainFolder = cms.readResource(MODULE_PATH);
        assertEquals(CmsResource.STATE_UNCHANGED, mainFolder.getState());
        assertEquals(CmsResource.STATE_UNCHANGED, cms.readResource(MODULE_PATH + "/bar.txt").getState());
        assertTrue(
            "there are locked resources in the main folder",
            cms.getLockedResources(mainFolder, CmsLockFilter.FILTER_ALL).isEmpty());
    }

    /**
     * Test case.<p>
     * @throws Exception if an error happens
     */
    public void testUpdateContent() throws Exception {

        CmsObject cms = cms();
        removeTestModuleIfExists(cms);
        File export = null;
        // use custom resource storage so there is no interference from other tests
        newStorage();

        // use blocks so we don't accidentally use wrong object
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            builder.addTextFile("foo.txt", "this is the modified foo file");
            builder.addTextFile("bar.txt", "this is the bar file");
            builder.publish();
            export = tempExport();
            builder.export(export.getAbsolutePath());
            storeResources(cms, MODULE_PATH);
            builder.delete();
        }
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            builder.addTextFile("foo.txt", "this is the original foo file");
            builder.addTextFile("bar.txt", "this is the bar file");
            builder.publish();
        }
        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();
        filter.disableProjectLastModifiedTest();
        filter.disableDateContentTest();
        filter.disableDateLastModifiedTest();
        filter.disableResourceIdTest();

        CmsReplaceModuleInfo info = OpenCms.getModuleManager().replaceModule(
            cms,
            export.getAbsolutePath(),
            new CmsShellReport(Locale.ENGLISH));
        assertTrue("Should have used new module updater", info.usedUpdater());

        List<CmsResource> resources = new ArrayList<>();
        resources.add(cms.readResource(MODULE_PATH, CmsResourceFilter.ALL));
        resources.addAll(cms.readResources(MODULE_PATH, CmsResourceFilter.ALL, true));

        // first test that existing resources match their stored version, then check that there are no extra resources
        for (CmsResource resource : resources) {
            System.out.println("Comparing " + resource.getRootPath());
            assertFilter(cms, resource.getRootPath(), filter);
        }
        assertEquals("Resource count doesn't match", m_currentResourceStrorage.size(), resources.size());

    }

    /**
     * Test case.<p>
     * @throws Exception if an error happens
     */
    public void testUpdateModuleWithModifiedResource() throws Exception {

        CmsObject cms = cms();
        removeTestModuleIfExists(cms);
        File export1 = null;

        CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
        builder.addModule();
        builder.addFolder("");
        CmsResource res = builder.addTextFile("file.txt", "aaa");
        builder.publish();
        export1 = tempExport();
        builder.export(export1.getAbsolutePath());
        try {
            CmsFile file = cms.readFile(res);
            file.setContents("aaa".getBytes("UTF-8"));
            cms.lockResourceTemporary(res);
            cms.writeFile(file);
            CmsShellReport report = new CmsShellReport(Locale.ENGLISH);
            OpenCms.getModuleManager().replaceModule(cms, export1.getAbsolutePath(), report);
            assertTrue(cms.readResource(res.getRootPath()).getState().isUnchanged());

        } finally {
            builder.delete();
        }

    }

    /**
     * Test case.<p>
     * @throws Exception if an error happens
     */
    public void testUpdateTypes() throws Exception {

        CmsObject cms = cms();
        removeTestModuleIfExists(cms);
        File export = null;
        // use custom resource storage so there is no interference from other tests
        newStorage();

        // use blocks so we don't accidentally use wrong object
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addType("firsttype", 7001);
            builder.addModule();
            export = tempExport();
            builder.export(export.getAbsolutePath());
            builder.delete();
        }
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addType("secondtype", 7002);
            builder.addModule();
        }

        CmsReplaceModuleInfo result = OpenCms.getModuleManager().replaceModule(
            cms,
            export.getAbsolutePath(),
            new CmsShellReport(Locale.ENGLISH));
        assertTrue("should have used update mechanism", result.usedUpdater());
        assertNull(
            "explorer type secondtype should have been removed",
            OpenCms.getWorkplaceManager().getExplorerTypeSetting("secondtype"));

        assertNotNull(
            "explorer type firsttype is missing",
            OpenCms.getWorkplaceManager().getExplorerTypeSetting("firsttype"));

        assertTrue("missing type firsttype", OpenCms.getResourceManager().hasResourceType("firsttype"));
        assertFalse("shouldn't have type secondttype", OpenCms.getResourceManager().hasResourceType("secondtype"));

    }

    /**
     * Test case.<p>
     * @throws Exception if an error happens
     */
    public void testUpdateWithSimpleFileIdConflict() throws Exception {

        CmsObject cms = cms();
        removeTestModuleIfExists(cms);
        File export = null;
        // use custom resource storage so there is no interference from other tests
        newStorage();

        // use blocks so we don't accidentally use wrong object
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            builder.addTextFile("foo.txt", "this is the modified foo file");
            builder.publish();
            export = tempExport();
            builder.export(export.getAbsolutePath());
            storeResources(cms, MODULE_PATH);
            builder.delete();
        }
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            builder.setNextStructureId(new CmsUUID());
            builder.setNextResourceId(new CmsUUID());
            builder.addTextFile("foo.txt", "this is the original foo file");
            builder.publish();
        }
        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();
        filter.disableProjectLastModifiedTest();
        filter.disableDateContentTest();
        filter.disableDateLastModifiedTest();
        filter.disableResourceIdTest();

        CmsReplaceModuleInfo info = OpenCms.getModuleManager().replaceModule(
            cms,
            export.getAbsolutePath(),
            new CmsShellReport(Locale.ENGLISH));
        assertTrue("Should have used new module updater", info.usedUpdater());

        List<CmsResource> resources = new ArrayList<>();
        resources.add(cms.readResource(MODULE_PATH, CmsResourceFilter.ALL));
        resources.addAll(cms.readResources(MODULE_PATH, CmsResourceFilter.ALL, true));

        // first test that existing resources match their stored version, then check that there are no extra resources
        for (CmsResource resource : resources) {
            System.out.println("Comparing " + resource.getRootPath());
            assertFilter(cms, resource.getRootPath(), filter);
        }
        assertEquals("Resource count doesn't match", m_currentResourceStrorage.size(), resources.size());

    }

    /**
     * Test case.<p>
     * @throws Exception if an error happens
     */
    public void testUpdateWithSimpleFileIdConflict2() throws Exception {

        CmsObject cms = cms();
        removeTestModuleIfExists(cms);
        CmsResource res = cms.createResource("/system/anotherfile", 1);
        CmsUUID sid = res.getStructureId();
        CmsUUID rid = res.getResourceId();

        CmsUUID[][] idSeqs = new CmsUUID[][] {{new CmsUUID(), new CmsUUID()}, {sid, rid}};
        // First create a module, then try to update it with a module containing the same resource path but with
        // a structure id that occurs elsewhere in the system

        for (int i = 0; i < 2; i++) {
            String manifest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "\n"
                + "<export>\n"
                + "    <info>\n"
                + "        <infoproject>Offline</infoproject>\n"
                + "        <export_version>10</export_version>\n"
                + "    </info>\n"
                + "    <module>\n"
                + "        <name>org.opencms.configtest2</name>\n"
                + "        <nicename><![CDATA[OpenCms configuration module]]></nicename>\n"
                + "        <group>OpenCms Editors</group>\n"
                + "        <class />\n"
                + "        <site>/</site>\n"
                + "        <export-mode name=\"reduced\" />\n"
                + "        <description><![CDATA[<p>Contains various configuration files to specify OpenCms core behavior.</p>\n"
                + "<p><i>&copy; by Alkacon Software GmbH &amp; Co. KG (http://www.alkacon.com).</i></p>]]></description>\n"
                + "        <version>11.0.0</version>\n"
                + "        <authorname><![CDATA[Alkacon Software GmbH &amp; Co. KG]]></authorname>\n"
                + "        <authoremail><![CDATA[info@alkacon.com]]></authoremail>\n"
                + "        <datecreated />\n"
                + "        <userinstalled />\n"
                + "        <dateinstalled />\n"
                + "        <dependencies />\n"
                + "        <exportpoints />\n"
                + "        <resources>\n"
                + "            <resource uri=\"/system/test1234\" />\n"
                + "        </resources>\n"
                + "        <excluderesources />\n"
                + "        <parameters />\n"
                + "    </module>\n"
                + "    <files>\n"
                + "        <file>\n"
                + "            <destination>system</destination>\n"
                + "            <type>folder</type>\n"
                + "            <properties />\n"
                + "        </file>\n"
                + "        <file>\n"
                + "            <source>system/test1234</source>\n"
                + "            <destination>system/test1234</destination>\n"
                + "            <uuidstructure>"
                + idSeqs[i][0]
                + "</uuidstructure>\n"
                + "            <uuidresource>"
                + idSeqs[i][1]
                + "</uuidresource>\n"
                + "            <type>plain</type>\n"
                + "            <datecreated>Tue, 08 Nov 2005 15:35:44 GMT</datecreated>\n"
                + "            <flags>0</flags>\n"
                + "            <properties />\n"
                + "            <relations />\n"
                + "            <accesscontrol />\n"
                + "        </file>\n"
                + "   </files>\n"
                + "</export>\n"
                + "";

            CmsZipBuilder zipBuilder = new CmsZipBuilder();
            zipBuilder.addFile("manifest.xml", manifest);
            zipBuilder.addFile("system/test1234", "test1234");
            File importZip = zipBuilder.writeZip();
            importZip.deleteOnExit();

            CmsShellReport report = new CmsShellReport(Locale.ENGLISH);

            CmsReplaceModuleInfo info = OpenCms.getModuleManager().replaceModule(
                cms,
                importZip.getCanonicalPath(),
                report);
            if (i == 1) {
                assertFalse("Should have not used new module update", info.usedUpdater());
            }
        }
    }

    /**
     * Test case.<p>
     * @throws Exception if an error happens
     */
    public void testUseOldModuleReplaceWhenIdsCollide() throws Exception {

        CmsObject cms = cms();
        removeTestModuleIfExists(cms);

        File export = null;
        // use custom resource storage so there is no interference from other tests
        newStorage();
        CmsUUID a = new CmsUUID();
        CmsUUID b = new CmsUUID();

        // use blocks so we don't accidentally use wrong object
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            builder.setNextStructureId(a);
            builder.addTextFile("a.txt", "a");
            builder.setNextStructureId(b);
            builder.addTextFile("b.txt", "b");
            builder.publish();
            export = tempExport();
            builder.export(export.getAbsolutePath());
            builder.delete();
        }
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            builder.setNextStructureId(b);
            builder.addTextFile("a.txt", "a");
            builder.setNextStructureId(a);
            builder.addTextFile("b.txt", "b");
            builder.publish();
        }
        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();
        filter.disableProjectLastModifiedTest();
        filter.disableDateContentTest();
        filter.disableDateLastModifiedTest();
        filter.disableResourceIdTest();

        CmsReplaceModuleInfo replaceInfo = OpenCms.getModuleManager().replaceModule(
            cms,
            export.getAbsolutePath(),
            new CmsShellReport(Locale.ENGLISH));
        assertFalse("new module update mechanism should not have been used", replaceInfo.usedUpdater());

    }

    /**
     * Test case.<p>
     * @throws Exception if an error happens
     */
    public void testUseOldReplaceIfModuleResourcesHaveNoStructureId() throws Exception {

        String manifest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "\n"
            + "<export>\n"
            + "    <info>\n"
            + "        <infoproject>Offline</infoproject>\n"
            + "        <export_version>10</export_version>\n"
            + "    </info>\n"
            + "    <module>\n"
            + "        <name>org.opencms.configtest</name>\n"
            + "        <nicename><![CDATA[OpenCms configuration module]]></nicename>\n"
            + "        <group>OpenCms Editors</group>\n"
            + "        <class />\n"
            + "        <site>/</site>\n"
            + "        <export-mode name=\"reduced\" />\n"
            + "        <description><![CDATA[<p>Contains various configuration files to specify OpenCms core behavior.</p>\n"
            + "<p><i>&copy; by Alkacon Software GmbH &amp; Co. KG (http://www.alkacon.com).</i></p>]]></description>\n"
            + "        <version>11.0.0</version>\n"
            + "        <authorname><![CDATA[Alkacon Software GmbH &amp; Co. KG]]></authorname>\n"
            + "        <authoremail><![CDATA[info@alkacon.com]]></authoremail>\n"
            + "        <datecreated />\n"
            + "        <userinstalled />\n"
            + "        <dateinstalled />\n"
            + "        <dependencies />\n"
            + "        <exportpoints />\n"
            + "        <resources>\n"
            + "            <resource uri=\"/system/test123\" />\n"
            + "        </resources>\n"
            + "        <excluderesources />\n"
            + "        <parameters />\n"
            + "    </module>\n"
            + "    <files>\n"
            + "        <file>\n"
            + "            <destination>system</destination>\n"
            + "            <type>folder</type>\n"
            + "            <properties />\n"
            + "        </file>\n"
            + "        <file>\n"
            + "            <source>system/test123</source>\n"
            + "            <destination>system/test123</destination>\n"
            + "            <type>plain</type>\n"
            + "            <datecreated>Tue, 08 Nov 2005 15:35:44 GMT</datecreated>\n"
            + "            <flags>0</flags>\n"
            + "            <properties />\n"
            + "            <relations />\n"
            + "            <accesscontrol />\n"
            + "        </file>\n"
            + "   </files>\n"
            + "</export>\n"
            + "";
        CmsZipBuilder zipBuilder = new CmsZipBuilder();
        zipBuilder.addFile("manifest.xml", manifest);
        zipBuilder.addFile("system/test123", "test123");
        File importZip = zipBuilder.writeZip();
        importZip.deleteOnExit();
        CmsObject cms = cms();
        CmsShellReport report = new CmsShellReport(Locale.ENGLISH);
        CmsImportParameters params = new CmsImportParameters(importZip.getCanonicalPath(), "/", true);
        OpenCms.getImportExportManager().importData(cms, report, params);

        CmsReplaceModuleInfo info = OpenCms.getModuleManager().replaceModule(cms, importZip.getCanonicalPath(), report);
        assertFalse("Should have not used new module update", info.usedUpdater());

    }

    /**
     * Test case.<p>
     * @throws Exception if an error happens
     */
    public void testUseOldReplaceIfSiblingStructureIsDifferent() throws Exception {

        CmsObject cms = cms();
        removeTestModuleIfExists(cms);

        File export = null;
        // use custom resource storage so there is no interference from other tests
        newStorage();
        CmsUUID resId = new CmsUUID();

        // use blocks so we don't accidentally use wrong object
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");

            builder.setNextResourceId(resId);
            String content = "content1";
            builder.addTextFile("file1.txt", content);
            builder.setNextResourceId(resId);
            builder.addTextFile("file2.txt", null);
            builder.publish();

            export = tempExport();
            builder.export(export.getAbsolutePath());
            storeResources(cms, MODULE_PATH);
            builder.delete();
        }
        {
            CmsTestModuleBuilder builder = new CmsTestModuleBuilder(cms, MODULE);
            builder.addModule();
            builder.addFolder("");
            builder.setNextResourceId(resId);
            String content = "content2";
            builder.addTextFile("file1.txt", content);
            builder.addTextFile("file2.txt", "othercontent");

            builder.publish();
        }
        OpenCmsTestResourceConfigurableFilter filter = new OpenCmsTestResourceConfigurableFilter();
        filter.disableProjectLastModifiedTest();
        filter.disableDateContentTest();
        filter.disableDateLastModifiedTest();

        CmsReplaceModuleInfo result = OpenCms.getModuleManager().replaceModule(
            cms,
            export.getAbsolutePath(),
            new CmsShellReport(Locale.ENGLISH));
        assertFalse("should have used old replace mechanism", result.usedUpdater());
    }

    /**
     * Creates and switches to a new resource storage for the currently executed test case.<p>
     *
     * @throws CmsException if something goes wrong
     */
    protected void newStorage() throws CmsException {

        String name = TestModuleUpdate.class.getName() + "." + getName();
        createStorage(name);
        switchStorage(name);
    }

    /**
     * Gets a new CmsObject copy.<p>
     *
     * @return a new CmsObject copy
     *
     * @throws CmsException if something goes wrong
     */
    CmsObject cms() throws CmsException {

        return OpenCms.initCmsObject(getCmsObject());
    }

    /**
     * Returns the full path for a resource given the module relative path.<p>
     *
     * @param s the module relative path
     * @return the full path
     */
    String modulePath(String s) {

        return CmsStringUtil.joinPaths(MODULE_PATH, s);
    }

    /**
     * Removes the test module if it exists.<p>
     *
     * @param cms the CMS context
     * @throws CmsException if something goes wrong
     */
    private void removeTestModuleIfExists(CmsObject cms) throws CmsException {

        if (OpenCms.getModuleManager().hasModule(MODULE)) {
            OpenCms.getModuleManager().deleteModule(cms, MODULE, false, new CmsShellReport(Locale.ENGLISH));
        }
    }

}
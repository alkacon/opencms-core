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

package org.opencms.file.types;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.TestLinkValidation;
import org.opencms.importexport.CmsExportParameters;
import org.opencms.importexport.CmsImportParameters;
import org.opencms.importexport.CmsVfsImportExportHandler;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule.ExportMode;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.page.CmsXmlPageFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for the link parseable resource types.<p>
 */
public class TestLinkParseableResourceTypes extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestLinkParseableResourceTypes(String arg0) {

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
        suite.setName(TestLinkParseableResourceTypes.class.getName());

        suite.addTest(new TestLinkParseableResourceTypes("testInitialSetup"));
        suite.addTest(new TestLinkParseableResourceTypes("testCopyResource"));
        suite.addTest(new TestLinkParseableResourceTypes("testCreateResource"));
        suite.addTest(new TestLinkParseableResourceTypes("testCreateSibling"));
        suite.addTest(new TestLinkParseableResourceTypes("testMoveResource"));
        suite.addTest(new TestLinkParseableResourceTypes("testReplaceLinkParseableResource"));
        suite.addTest(new TestLinkParseableResourceTypes("testReplaceNonLinkParseableResource"));
        suite.addTest(new TestLinkParseableResourceTypes("testChTypeLinkParseable"));
        suite.addTest(new TestLinkParseableResourceTypes("testChTypeNonLinkParseable"));
        suite.addTest(new TestLinkParseableResourceTypes("testWriteFile"));
        suite.addTest(new TestLinkParseableResourceTypes("testImportResourceLinkParseable"));
        suite.addTest(new TestLinkParseableResourceTypes("testImportResourceNonLinkParseable"));
        suite.addTest(new TestLinkParseableResourceTypes("testDeleteResource"));
        suite.addTest(new TestLinkParseableResourceTypes("testDeleteFolder"));
        suite.addTest(new TestLinkParseableResourceTypes("testUndoChanges"));

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
     * Test chType method, change type link parseable with non link parseable.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testChTypeLinkParseable() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing 'chType' method, change type link parseable with non link parseable");

        String sourceName = "/index_created.html";
        String targetName = "/folder1/image2.gif";
        CmsResource target = cms.readResource(targetName);

        List<CmsRelation> relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        int sources = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size();

        cms.lockResource(sourceName);
        cms.chtype(sourceName, CmsResourceTypeBinary.getStaticTypeId());
        CmsResource changed = cms.readResource(sourceName);

        assertRelationOperation(cms, changed, target, sources - 1, 0);

        // check the online project
        CmsProject project = cms.getRequestContext().getCurrentProject();
        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        sources = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size();
        assertRelationOperation(cms, changed, target, sources, 1);

        cms.getRequestContext().setCurrentProject(project);

        cms.unlockResource(sourceName);
        OpenCms.getPublishManager().publishResource(cms, sourceName);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        assertRelationOperation(cms, changed, target, sources - 1, 0);
    }

    /**
     * Test chType method, change type non link parseable with link parseable.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testChTypeNonLinkParseable() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing 'chType' method, change type non link parseable with link parseable");

        String sourceName = "/index_created.html";
        String targetName = "/folder1/image2.gif";
        CmsResource target = cms.readResource(targetName);

        List<CmsRelation> relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        int sources = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size();

        cms.lockResource(sourceName);
        cms.chtype(sourceName, CmsResourceTypeXmlPage.getStaticTypeId());
        CmsResource changed = cms.readResource(sourceName);

        assertRelationOperation(cms, changed, target, sources + 1, 1);

        // check the online project
        CmsProject project = cms.getRequestContext().getCurrentProject();
        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        sources = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size();
        assertRelationOperation(cms, changed, target, sources, 0);

        cms.getRequestContext().setCurrentProject(project);

        cms.unlockResource(sourceName);
        OpenCms.getPublishManager().publishResource(cms, sourceName);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        assertRelationOperation(cms, changed, target, sources + 1, 1);
    }

    /**
     * Test copyResource method.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testCopyResource() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing 'copyResource' method");

        String sourceName = "/index.html";
        CmsResource source = cms.readResource(sourceName);
        String targetName = "/folder1/image2.gif";
        CmsResource target = cms.readResource(targetName);

        List<CmsRelation> relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        int sources = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size();

        String copyName = "/index_copy.html";
        cms.lockResource(sourceName);
        cms.copyResource(sourceName, copyName);
        CmsResource copy = cms.readResource(copyName);

        assertRelationOperation(cms, source, target, sources + 1, 1);
        assertRelationOperation(cms, copy, target, sources + 1, 1);

        // check the online project
        CmsProject project = cms.getRequestContext().getCurrentProject();
        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        sources = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size();

        cms.getRequestContext().setCurrentProject(project);

        cms.unlockResource(copyName);
        OpenCms.getPublishManager().publishResource(cms, copyName);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        assertRelationOperation(cms, source, target, sources + 1, 1);
        assertRelationOperation(cms, copy, target, sources + 1, 1);
    }

    /**
     * Test createResource method.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testCreateResource() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing 'createResource' method");

        String sourceName = "/index.html";
        CmsResource source = cms.readResource(sourceName);
        String targetName = "/folder1/image2.gif";
        CmsResource target = cms.readResource(targetName);

        List<CmsRelation> relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        int sources = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size();

        String createdName = "/index_created.html";
        cms.createResource(createdName, source.getTypeId(), cms.readFile(source).getContents(), null);
        CmsResource created = cms.readResource(createdName);

        assertRelationOperation(cms, source, target, sources + 1, 1);
        assertRelationOperation(cms, created, target, sources + 1, 1);

        // check the online project
        CmsProject project = cms.getRequestContext().getCurrentProject();
        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        sources = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size();

        cms.getRequestContext().setCurrentProject(project);

        cms.unlockResource(createdName);
        OpenCms.getPublishManager().publishResource(cms, createdName);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        assertRelationOperation(cms, source, target, sources + 1, 1);
        assertRelationOperation(cms, created, target, sources + 1, 1);
    }

    /**
     * Test createSibling method.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testCreateSibling() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing 'createSibling' method");

        String sourceName = "/index.html";
        CmsResource source = cms.readResource(sourceName);
        String targetName = "/folder1/image2.gif";
        CmsResource target = cms.readResource(targetName);

        List<CmsRelation> relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        int sources = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size();

        String siblingName = "/index_sibling.html";
        cms.lockResource(sourceName);
        cms.createSibling(sourceName, siblingName, null);
        CmsResource sibling = cms.readResource(siblingName);

        assertRelationOperation(cms, source, target, sources + 1, 1);
        assertRelationOperation(cms, sibling, target, sources + 1, 1);

        // check the online project
        CmsProject project = cms.getRequestContext().getCurrentProject();
        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        sources = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size();

        cms.getRequestContext().setCurrentProject(project);

        cms.unlockResource(siblingName);
        OpenCms.getPublishManager().publishResource(cms, siblingName);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        assertRelationOperation(cms, source, target, sources + 1, 1);
        assertRelationOperation(cms, sibling, target, sources + 1, 1);
    }

    /**
     * Test deleteResource method for a folder.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testDeleteFolder() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing 'deleteResource' method for a folder");

        String folderName = "/testFolder";
        cms.createResource(folderName, CmsResourceTypeFolder.RESOURCE_TYPE_ID);

        String targetName = "/folder2/image2.gif";
        CmsResource target = cms.readResource(targetName);

        String sourceName = folderName + "/index.html";
        CmsResource source = cms.createResource(sourceName, CmsResourceTypeXmlPage.getStaticTypeId());
        TestLinkValidation.setContent(cms, sourceName, "<img src='" + targetName + "'>");

        List<CmsRelation> relations = cms.getRelationsForResource(sourceName, CmsRelationFilter.TARGETS);
        assertEquals(1, relations.size());
        CmsRelation expected = new CmsRelation(source, target, CmsRelationType.EMBEDDED_IMAGE);
        assertRelation(expected, relations.get(0));
        relations = cms.getRelationsForResource(sourceName, CmsRelationFilter.SOURCES);
        assertTrue(relations.isEmpty());

        relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        relations = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES);
        assertEquals(1, relations.size());

        cms.lockResource(folderName);
        cms.deleteResource(folderName, CmsResource.DELETE_PRESERVE_SIBLINGS);

        relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        relations = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES);
        assertTrue(relations.isEmpty());
    }

    /**
     * Test deleteResource method.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testDeleteResource() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing 'deleteResource' method");

        String sourceName = "/index_copy.html";
        CmsResource source = cms.readResource(sourceName);

        String targetName = "/folder1/image2.gif";
        CmsResource target = cms.readResource(targetName);

        List<CmsRelation> relations = cms.getRelationsForResource(sourceName, CmsRelationFilter.TARGETS);
        assertEquals(relations.size(), 1);
        CmsRelation expected = new CmsRelation(source, target, CmsRelationType.EMBEDDED_IMAGE);
        assertRelation(expected, relations.get(0));
        relations = cms.getRelationsForResource(sourceName, CmsRelationFilter.SOURCES);
        assertTrue(relations.isEmpty());

        relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        int sources = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size();

        cms.lockResource(sourceName);
        cms.deleteResource(sourceName, CmsResource.DELETE_PRESERVE_SIBLINGS);

        relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        relations = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES);
        assertEquals(sources, relations.size());

        // there is no direct way to check the relations of deleted resources
        /*
         String query = "select * from cms_offline_resource_relations where relation_source_id = '"
         + source.getStructureId()
         + "' or relation_source_path like '"
         + source.getRootPath()
         + "%';";
         */

        // now test deleting a target
        relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size();

        cms.lockResource(targetName);
        cms.deleteResource(targetName, CmsResource.DELETE_PRESERVE_SIBLINGS);

        sourceName = "/index.html";
        source = cms.readResource(sourceName);
        relations = cms.getRelationsForResource(sourceName, CmsRelationFilter.TARGETS);
        assertEquals(1, relations.size());
        expected = new CmsRelation(source, target, CmsRelationType.EMBEDDED_IMAGE);
        assertRelation(expected, relations.get(0));
        relations = cms.getRelationsForResource(sourceName, CmsRelationFilter.SOURCES);
        assertTrue(relations.isEmpty());

        // there is no direct way to check the relations of deleted resources
        /*
         query = "select * from cms_offline_resource_relations where relation_target_id = '"
         + target.getStructureId()
         + "' or relation_target_path like '"
         + target.getRootPath()
         + "%';";
         */
    }

    /**
     * Test importResource method for link parseable resources.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testImportResourceLinkParseable() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing 'importResource' method for link parseable resources");

        String sourceName = "/index.html";
        CmsResource source = cms.readResource(sourceName);
        String targetName = "folder1/subfolder11/subsubfolder111/jsp.jsp";
        String zipExportFilename = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            "packages/testImportChangeType.zip");

        try {
            List<CmsRelation> relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
            assertTrue(relations.isEmpty());

            // replace by link parseable
            cms.lockResource(targetName);
            cms.replaceResource(targetName, source.getTypeId(), cms.readFile(source).getContents(), null);

            relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
            assertEquals(relations.size(), 1);

            // export the file
            List<String> exportPaths = new ArrayList<String>(1);
            exportPaths.add(targetName);
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

            // change the type back
            cms.undoChanges(targetName, CmsResource.UNDO_CONTENT);

            relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
            assertTrue(relations.isEmpty());

            // re-import the exported file
            OpenCms.getImportExportManager().importData(
                cms,
                new CmsShellReport(cms.getRequestContext().getLocale()),
                new CmsImportParameters(zipExportFilename, "/", true));

            relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
            assertEquals(relations.size(), 1);
        } finally {
            try {
                if (zipExportFilename != null) {
                    File file = new File(zipExportFilename);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            } catch (Throwable t) {
                // intentionally left blank
            }
        }
    }

    /**
     * Test importResource method for non link parseable resources.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testImportResourceNonLinkParseable() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing 'importResource' method for non link parseable resources");

        String sourceName = "/index_created.html";
        String zipExportFilename = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            "packages/testImportChangeType.zip");
        try {

            List<CmsRelation> relations = cms.getRelationsForResource(sourceName, CmsRelationFilter.TARGETS);
            assertEquals(relations.size(), 1);

            // change the type of the resource
            cms.lockResource(sourceName);
            cms.chtype(sourceName, CmsResourceTypeBinary.getStaticTypeId());

            relations = cms.getRelationsForResource(sourceName, CmsRelationFilter.TARGETS);
            assertTrue(relations.isEmpty());

            // export the file
            List<String> exportPaths = new ArrayList<String>(1);
            exportPaths.add(sourceName);
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

            // change the type back
            cms.undoChanges(sourceName, CmsResource.UNDO_CONTENT);

            relations = cms.getRelationsForResource(sourceName, CmsRelationFilter.TARGETS);
            assertEquals(1, relations.size());

            // re-import the exported file
            OpenCms.getImportExportManager().importData(
                cms,
                new CmsShellReport(cms.getRequestContext().getLocale()),
                new CmsImportParameters(zipExportFilename, "/", true));

            relations = cms.getRelationsForResource(sourceName, CmsRelationFilter.TARGETS);
            assertTrue(relations.isEmpty());
        } finally {
            deleteFile(zipExportFilename);
        }
    }

    /**
     * Test the links after the setup.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testInitialSetup() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing the links after the setup");

        String sourceName = "/index.html";
        CmsResource source = cms.readResource(sourceName);
        String targetName = "/folder1/image2.gif";
        CmsResource target = cms.readResource(targetName);
        List<CmsRelation> relations = cms.getRelationsForResource(sourceName, CmsRelationFilter.TARGETS);
        assertEquals(1, relations.size());
        CmsRelation expected = new CmsRelation(source, target, CmsRelationType.EMBEDDED_IMAGE);
        assertRelation(expected, relations.get(0));
        relations = cms.getRelationsForResource(sourceName, CmsRelationFilter.SOURCES);
        assertTrue(relations.isEmpty());

        relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        relations = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES);
        assertEquals(20, relations.size());
    }

    /**
     * Test moveResource method.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testMoveResource() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing 'moveResource' method");

        String sourceName = "/index_sibling.html";
        String targetName = "/folder1/image2.gif";
        CmsResource target = cms.readResource(targetName);

        List<CmsRelation> relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        int sources = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size();

        String movedName = "/index_moved.html";
        cms.lockResource(sourceName);
        cms.moveResource(sourceName, movedName);
        CmsResource moved = cms.readResource(movedName);

        assertRelationOperation(cms, moved, target, sources, 1);

        // check the online project
        CmsProject project = cms.getRequestContext().getCurrentProject();
        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        sources = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size();

        cms.getRequestContext().setCurrentProject(project);

        cms.unlockResource(movedName);
        OpenCms.getPublishManager().publishResource(cms, movedName);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        assertRelationOperation(cms, moved, target, sources, 1);
    }

    /**
     * Test replaceResource method.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testReplaceLinkParseableResource() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing 'replaceResource' method, replace link parseable with non link parseable resource");

        String sourceName = "/folder1/subfolder11/subsubfolder111/jsp.jsp";
        CmsResource source = cms.readResource(sourceName);
        String targetName = "/folder1/image2.gif";
        CmsResource target = cms.readResource(targetName);

        List<CmsRelation> relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        int sources = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size();

        String replacedName = "/index_created.html";
        cms.lockResource(replacedName);
        cms.replaceResource(replacedName, source.getTypeId(), cms.readFile(source).getContents(), null);
        CmsResource replaced = cms.readResource(replacedName);

        relations = cms.getRelationsForResource(replacedName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        assertRelationOperation(cms, replaced, target, sources - 1, 0);

        // check the online project
        CmsProject project = cms.getRequestContext().getCurrentProject();
        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        sources = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size();
        assertRelationOperation(cms, replaced, target, sources, 1);

        cms.getRequestContext().setCurrentProject(project);

        cms.unlockResource(replacedName);
        OpenCms.getPublishManager().publishResource(cms, replacedName);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        relations = cms.getRelationsForResource(replacedName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        assertRelationOperation(cms, replaced, target, sources - 1, 0);
    }

    /**
     * Test replaceResource method.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testReplaceNonLinkParseableResource() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing 'replaceResource' method, replace non link parseable with link parseable resource");

        String sourceName = "/index.html";
        CmsResource source = cms.readResource(sourceName);
        String targetName = "/folder1/image2.gif";
        CmsResource target = cms.readResource(targetName);

        List<CmsRelation> relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        int sources = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size();

        String replacedName = "/index_created.html";
        cms.lockResource(replacedName);
        cms.replaceResource(replacedName, source.getTypeId(), cms.readFile(source).getContents(), null);
        CmsResource replaced = cms.readResource(replacedName);

        assertRelationOperation(cms, replaced, target, sources + 1, 1);

        // check the online project
        CmsProject project = cms.getRequestContext().getCurrentProject();
        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        sources = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size();
        assertRelationOperation(cms, replaced, target, sources, 0);

        cms.getRequestContext().setCurrentProject(project);

        cms.unlockResource(replacedName);
        OpenCms.getPublishManager().publishResource(cms, replacedName);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        assertRelationOperation(cms, replaced, target, sources + 1, 1);
    }

    /**
     * Test undoChanges method.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testUndoChanges() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing 'undoChanges' method");

        String sourceName = "/index.html";
        CmsResource source = cms.readResource(sourceName);
        String targetName = "/folder1/index.html";
        CmsResource target = cms.readResource(targetName);

        List<CmsRelation> relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertEquals(1, relations.size());
        assertTrue(cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).isEmpty());

        // add new relation
        cms.lockResource(sourceName);
        cms.addRelationToResource(sourceName, targetName, CmsRelationType.CATEGORY.getName());

        relations = cms.getRelationsForResource(sourceName, CmsRelationFilter.TARGETS);
        assertEquals(2, relations.size());
        assertRelation(new CmsRelation(source, target, CmsRelationType.CATEGORY), relations.get(1));
        relations = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES);
        assertEquals(1, relations.size());
        assertRelation(new CmsRelation(source, target, CmsRelationType.CATEGORY), relations.get(0));

        // publish
        OpenCms.getPublishManager().publishResource(cms, sourceName);
        OpenCms.getPublishManager().waitWhileRunning();

        // delete relation
        cms.lockResource(sourceName);
        cms.deleteRelationsFromResource(
            sourceName,
            CmsRelationFilter.TARGETS.filterResource(target).filterNotDefinedInContent());

        relations = cms.getRelationsForResource(sourceName, CmsRelationFilter.TARGETS);
        assertEquals(1, relations.size());
        assertTrue(cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).isEmpty());

        // undo changes
        cms.undoChanges(sourceName, CmsResource.UNDO_CONTENT);

        relations = cms.getRelationsForResource(sourceName, CmsRelationFilter.TARGETS);
        assertEquals(2, relations.size());
        assertRelation(new CmsRelation(source, target, CmsRelationType.CATEGORY), relations.get(1));
        relations = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES);
        assertEquals(1, relations.size());
        assertRelation(new CmsRelation(source, target, CmsRelationType.CATEGORY), relations.get(0));
    }

    /**
     * Test writeFile method.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testWriteFile() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing 'writeFile' method");

        String sourceName = "/index.html";
        CmsResource source = cms.readResource(sourceName);
        String targetName = "/folder1/image2.gif";
        CmsResource target = cms.readResource(targetName);

        int sources = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size();
        assertRelationOperation(cms, source, target, sources, 1);

        // duplicate the english body into a new german body element
        CmsFile file = cms.readFile(source);
        CmsXmlPage page = CmsXmlPageFactory.unmarshal(cms, file);
        page.addLocale(cms, Locale.GERMAN);
        page.addValue("body", Locale.GERMAN);
        page.setStringValue(cms, "body", Locale.GERMAN, page.getStringValue(cms, "body", Locale.ENGLISH));
        file.setContents(page.marshal());

        cms.lockResource(sourceName);
        cms.writeFile(file);

        // the same <link> node is reused
        assertRelationOperation(cms, source, target, sources, 1);

        // check the online project
        CmsProject project = cms.getRequestContext().getCurrentProject();
        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        List<CmsRelation> relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        sources = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size();
        assertRelationOperation(cms, source, target, sources, 1);

        cms.getRequestContext().setCurrentProject(project);

        cms.unlockResource(sourceName);
        OpenCms.getPublishManager().publishResource(cms, sourceName);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        assertRelationOperation(cms, source, target, sources, 1);
        cms.getRequestContext().setCurrentProject(project);

        String newLinkName = "/index_created.html";

        // now add an additional link
        file = cms.readFile(source);
        page = CmsXmlPageFactory.unmarshal(cms, file);
        page.addLocale(cms, Locale.FRENCH);
        page.addValue("body", Locale.FRENCH);
        page.setStringValue(cms, "body", Locale.FRENCH, "<a href='" + newLinkName + "'>French</a>");
        file.setContents(page.marshal());

        relations = cms.getRelationsForResource(newLinkName, CmsRelationFilter.TARGETS);
        assertEquals(relations.size(), 1);
        relations = cms.getRelationsForResource(newLinkName, CmsRelationFilter.SOURCES);
        assertTrue(relations.isEmpty());

        cms.lockResource(sourceName);
        cms.writeFile(file);

        assertRelationOperation(cms, source, target, sources, 2);
        relations = cms.getRelationsForResource(newLinkName, CmsRelationFilter.TARGETS);
        assertEquals(relations.size(), 1);
        relations = cms.getRelationsForResource(newLinkName, CmsRelationFilter.SOURCES);
        assertEquals(relations.size(), 2); // since the source file has a sibling!

        // check the online project
        project = cms.getRequestContext().getCurrentProject();
        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        sources = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size();
        assertRelationOperation(cms, source, target, sources, 1);
        relations = cms.getRelationsForResource(newLinkName, CmsRelationFilter.TARGETS);
        assertEquals(relations.size(), 1);
        relations = cms.getRelationsForResource(newLinkName, CmsRelationFilter.SOURCES);
        assertTrue(relations.isEmpty());

        cms.getRequestContext().setCurrentProject(project);

        cms.unlockResource(sourceName);
        OpenCms.getPublishManager().publishResource(cms, sourceName);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        assertRelationOperation(cms, source, target, sources, 2);
        relations = cms.getRelationsForResource(newLinkName, CmsRelationFilter.TARGETS);
        assertEquals(relations.size(), 1);
        relations = cms.getRelationsForResource(newLinkName, CmsRelationFilter.SOURCES);
        assertEquals(relations.size(), 1);

        cms.getRequestContext().setCurrentProject(project);

        // now go back
        file = cms.readFile(source);
        page = CmsXmlPageFactory.unmarshal(cms, file);
        page.removeLocale(Locale.GERMAN);
        page.removeLocale(Locale.FRENCH);
        file.setContents(page.marshal());

        relations = cms.getRelationsForResource(newLinkName, CmsRelationFilter.TARGETS);
        assertEquals(relations.size(), 1);
        relations = cms.getRelationsForResource(newLinkName, CmsRelationFilter.SOURCES);
        assertEquals(relations.size(), 2); // since the source file has a sibling!

        cms.lockResource(sourceName);
        cms.writeFile(file);

        assertRelationOperation(cms, source, target, sources, 1);
        relations = cms.getRelationsForResource(newLinkName, CmsRelationFilter.TARGETS);
        assertEquals(relations.size(), 1);
        relations = cms.getRelationsForResource(newLinkName, CmsRelationFilter.SOURCES);
        assertTrue(relations.isEmpty());

        // check the online project
        project = cms.getRequestContext().getCurrentProject();
        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        relations = cms.getRelationsForResource(targetName, CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        sources = cms.getRelationsForResource(targetName, CmsRelationFilter.SOURCES).size();
        assertRelationOperation(cms, source, target, sources, 2);
        relations = cms.getRelationsForResource(newLinkName, CmsRelationFilter.TARGETS);
        assertEquals(relations.size(), 1);
        relations = cms.getRelationsForResource(newLinkName, CmsRelationFilter.SOURCES);
        assertEquals(relations.size(), 1);

        cms.getRequestContext().setCurrentProject(project);

        cms.unlockResource(sourceName);
        OpenCms.getPublishManager().publishResource(cms, sourceName);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.getRequestContext().setCurrentProject(cms.readProject(CmsProject.ONLINE_PROJECT_ID));

        assertRelationOperation(cms, source, target, sources, 1);
        relations = cms.getRelationsForResource(newLinkName, CmsRelationFilter.TARGETS);
        assertEquals(relations.size(), 1);
        relations = cms.getRelationsForResource(newLinkName, CmsRelationFilter.SOURCES);
        assertTrue(relations.isEmpty());
    }

    private void assertRelationOperation(
        CmsObject cms,
        CmsResource source,
        CmsResource target,
        int sources,
        int sourceRelations)
    throws CmsException {

        List<CmsRelation> relations = cms.getRelationsForResource(
            cms.getRequestContext().removeSiteRoot(source.getRootPath()),
            CmsRelationFilter.TARGETS);
        assertEquals(relations.size(), sourceRelations);
        if (relations.size() == 1) {
            CmsRelation expected = new CmsRelation(source, target, CmsRelationType.EMBEDDED_IMAGE);
            assertRelation(expected, relations.get(0));
        }
        relations = cms.getRelationsForResource(
            cms.getRequestContext().removeSiteRoot(source.getRootPath()),
            CmsRelationFilter.SOURCES);
        assertTrue(relations.isEmpty());

        relations = cms.getRelationsForResource(
            cms.getRequestContext().removeSiteRoot(target.getRootPath()),
            CmsRelationFilter.TARGETS);
        assertTrue(relations.isEmpty());
        relations = cms.getRelationsForResource(
            cms.getRequestContext().removeSiteRoot(target.getRootPath()),
            CmsRelationFilter.SOURCES);
        assertEquals(relations.size(), sources);
    }
}

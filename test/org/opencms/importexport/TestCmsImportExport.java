/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/importexport/TestCmsImportExport.java,v $
 * Date   : $Date: 2007/06/25 15:02:17 $
 * Version: $Revision: 1.16.4.10 $
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

package org.opencms.importexport;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.security.CmsDefaultPasswordHandler;
import org.opencms.security.I_CmsPasswordHandler;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.test.OpenCmsTestResourceFilter;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsResourceTranslator;
import org.opencms.relations.I_CmsLinkParseable;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.page.CmsXmlPageFactory;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Comment for <code>TestCmsImportExport</code>.<p>
 */
public class TestCmsImportExport extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsImportExport(String arg0) {

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

        suite.addTest(new TestCmsImportExport("testSetup"));
        suite.addTest(new TestCmsImportExport("testUserImport"));
        suite.addTest(new TestCmsImportExport("testImportExportFolder"));
        suite.addTest(new TestCmsImportExport("testImportExportId"));
        suite.addTest(new TestCmsImportExport("testImportExportBrokenLinksHtml"));
        suite.addTest(new TestCmsImportExport("testImportExportBrokenLinksXml"));
        suite.addTest(new TestCmsImportExport("testImportResourceTranslator"));
        suite.addTest(new TestCmsImportExport("testImportResourceTranslatorMultipleSite"));
        suite.addTest(new TestCmsImportExport("testImportRecreatedFile"));
        suite.addTest(new TestCmsImportExport("testImportSibling"));
        suite.addTest(new TestCmsImportExport("testImportRecreatedSibling"));
        suite.addTest(new TestCmsImportExport("testImportMovedResource"));
        suite.addTest(new TestCmsImportExport("testImportChangedContent"));
        suite.addTest(new TestCmsImportExport("testImportRelations"));

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
     * Tests the import of a resource that has been edited.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testImportChangedContent() throws Exception {

        CmsObject cms = getCmsObject();

        echo("Testing the import of a resource that has been edited.");
        String filename1 = "/newfile6.html";
        String zipExportFilename = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            "packages/testImportChangedContent.zip");

        try {
            cms.getRequestContext().setSiteRoot("/");
            // create file
            CmsResource res1 = cms.createResource(filename1, CmsResourceTypePlain.getStaticTypeId());

            // publish the file
            cms.unlockResource(filename1);
            OpenCms.getPublishManager().publishResource(cms, filename1);
            OpenCms.getPublishManager().waitWhileRunning();

            storeResources(cms, filename1);

            // export the file
            CmsVfsImportExportHandler vfsExportHandler = new CmsVfsImportExportHandler();
            vfsExportHandler.setFileName(zipExportFilename);
            List exportPaths = new ArrayList(1);
            exportPaths.add(filename1);
            vfsExportHandler.setExportPaths(exportPaths);
            vfsExportHandler.setIncludeSystem(false);
            vfsExportHandler.setIncludeUnchanged(true);
            vfsExportHandler.setExportUserdata(false);
            OpenCms.getImportExportManager().exportData(
                cms,
                vfsExportHandler,
                new CmsShellReport(cms.getRequestContext().getLocale()));

            // edit the file
            String content = "new content";
            CmsFile file = new CmsFile(cms.readResource(filename1));
            file.setContents(content.getBytes());

            cms.lockResource(filename1);
            cms.writeFile(file);

            // publish the file
            cms.unlockResource(filename1);
            OpenCms.getPublishManager().publishResource(cms, filename1);
            OpenCms.getPublishManager().waitWhileRunning();

            assertFilter(cms, cms.readResource(filename1), OpenCmsTestResourceFilter.FILTER_REPLACERESOURCE);

            cms.readResource(res1.getStructureId()); // check resource by id

            // re-import the exported files
            OpenCms.getImportExportManager().importData(
                cms,
                zipExportFilename,
                "/",
                new CmsShellReport(cms.getRequestContext().getLocale()));

            // publish the file
            cms.unlockResource(filename1);
            OpenCms.getPublishManager().publishResource(cms, filename1);
            OpenCms.getPublishManager().waitWhileRunning();

            assertFilter(cms, cms.readResource(filename1), OpenCmsTestResourceFilter.FILTER_IMPORTEXPORT);
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
     * Tests the import of linked XmlPages in a different site, so that the link paths get broken.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testImportExportBrokenLinksHtml() throws Exception {

        CmsObject cms = getCmsObject();

        echo("Testing the import of linked XmlPages in a different site, so that the link paths get broken.");
        String filename1 = "xmlpage1.html";
        String filename2 = "xmlpage2.html";
        String zipExportFilename = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            "packages/testImportExportBrokenLinks.zip");

        String storedSiteRoot = cms.getRequestContext().getSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot("/sites/default/");

            // create files 
            CmsResource res1 = cms.createResource(filename1, CmsResourceTypeXmlPage.getStaticTypeId());

            CmsResource res2 = cms.createResource(filename2, CmsResourceTypeXmlPage.getStaticTypeId());
            CmsFile file2 = CmsFile.upgrade(res2, cms);
            CmsXmlPage page2 = CmsXmlPageFactory.unmarshal(cms, file2, true);
            page2.addValue("test", Locale.ENGLISH);
            page2.setStringValue(cms, "test", Locale.ENGLISH, "<a href='" + filename1 + "'>test</a>");
            file2.setContents(page2.marshal());
            cms.writeFile(file2);

            // if done before file2 exists, no structure id is stored
            CmsFile file1 = CmsFile.upgrade(res1, cms);
            CmsXmlPage page1 = CmsXmlPageFactory.unmarshal(cms, file1, true);
            page1.addValue("test", Locale.ENGLISH);
            page1.setStringValue(cms, "test", Locale.ENGLISH, "<a href='" + filename2 + "'>test</a>");
            file1.setContents(page1.marshal());
            cms.writeFile(file1);

            // publish the files
            cms.unlockProject(cms.getRequestContext().currentProject().getUuid());
            OpenCms.getPublishManager().publishProject(cms);
            OpenCms.getPublishManager().waitWhileRunning();

            // export the files
            CmsVfsImportExportHandler vfsExportHandler = new CmsVfsImportExportHandler();
            vfsExportHandler.setFileName(zipExportFilename);
            List exportPaths = new ArrayList(1);
            exportPaths.add(filename1);
            exportPaths.add(filename2);
            vfsExportHandler.setExportPaths(exportPaths);
            vfsExportHandler.setIncludeSystem(false);
            vfsExportHandler.setIncludeUnchanged(true);
            vfsExportHandler.setExportUserdata(false);
            OpenCms.getImportExportManager().exportData(
                cms,
                vfsExportHandler,
                new CmsShellReport(cms.getRequestContext().getLocale()));

            // delete the both files
            cms.lockResource(filename1);
            cms.lockResource(filename2);
            cms.deleteResource(filename1, CmsResource.DELETE_REMOVE_SIBLINGS);
            cms.deleteResource(filename2, CmsResource.DELETE_REMOVE_SIBLINGS);
            // publish the deleted files
            cms.unlockProject(cms.getRequestContext().currentProject().getUuid());
            OpenCms.getPublishManager().publishProject(cms);
            OpenCms.getPublishManager().waitWhileRunning();
        } finally {
            cms.getRequestContext().setSiteRoot(storedSiteRoot);
        }

        try {
            cms.getRequestContext().setSiteRoot("/");

            // re-import the exported files
            OpenCms.getImportExportManager().importData(
                cms,
                zipExportFilename,
                "/",
                new CmsShellReport(cms.getRequestContext().getLocale()));

            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(
                CmsResourceTypeXmlPage.getStaticTypeId());
            I_CmsLinkParseable validatable = (I_CmsLinkParseable)type;

            // check the links
            CmsFile res1 = cms.readFile(filename1);
            List links1 = validatable.parseLinks(cms, res1);
            assertEquals(links1.size(), 1);
            assertEquals(links1.get(0).toString(), cms.getRequestContext().addSiteRoot(filename2));

            CmsFile res2 = cms.readFile(filename2);
            List links2 = validatable.parseLinks(cms, res2);
            assertEquals(links2.size(), 1);
            assertEquals(links2.get(0).toString(), cms.getRequestContext().addSiteRoot(filename1));
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
     * Tests the import of linked XmlContent in a different site, so that the link paths get broken.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testImportExportBrokenLinksXml() throws Exception {

        CmsObject cms = getCmsObject();

        echo("Testing the import of linked Xml Content in a different site, so that the link paths get broken.");
        String filename1 = "/xmlcontent.html";
        String filename2 = "/xmlcontent2.html";
        String zipExportFilename = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            "packages/testImportExportBrokenLinks.zip");
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);

        cms.getRequestContext().setSiteRoot("/sites/default/");

        // create files 
        CmsResource res1 = cms.createResource(filename1, OpenCmsTestCase.ARTICLE_TYPEID);

        CmsResource res2 = cms.createResource(filename2, OpenCmsTestCase.ARTICLE_TYPEID);
        CmsFile file2 = CmsFile.upgrade(res2, cms);
        String content2 = new String(file2.getContents(), CmsEncoder.ENCODING_UTF_8);
        CmsXmlContent xmlcontent2 = CmsXmlContentFactory.unmarshal(content2, CmsEncoder.ENCODING_UTF_8, resolver);
        xmlcontent2.getValue("Text", Locale.ENGLISH, 0).setStringValue(cms, "<a href='" + filename1 + "'>test</a>");
        if (!xmlcontent2.hasValue("Homepage", Locale.ENGLISH, 0)) {
            xmlcontent2.addValue(cms, "Homepage", Locale.ENGLISH, 0);
        }
        xmlcontent2.getValue("Homepage", Locale.ENGLISH, 0).setStringValue(cms, filename1);
        file2.setContents(xmlcontent2.marshal());
        cms.writeFile(file2);

        // if done before file2 exists, no structure id is stored
        CmsFile file1 = CmsFile.upgrade(res1, cms);
        String content1 = new String(file1.getContents(), CmsEncoder.ENCODING_UTF_8);
        CmsXmlContent xmlcontent1 = CmsXmlContentFactory.unmarshal(content1, CmsEncoder.ENCODING_UTF_8, resolver);
        xmlcontent1.getValue("Text", Locale.ENGLISH, 0).setStringValue(cms, "<a href='" + filename2 + "'>test</a>");
        if (!xmlcontent1.hasValue("Homepage", Locale.ENGLISH, 0)) {
            xmlcontent1.addValue(cms, "Homepage", Locale.ENGLISH, 0);
        }
        xmlcontent1.getValue("Homepage", Locale.ENGLISH, 0).setStringValue(cms, filename2);
        file1.setContents(xmlcontent1.marshal());
        cms.writeFile(file1);

        // publish the files
        cms.unlockProject(cms.getRequestContext().currentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        // export the files
        CmsVfsImportExportHandler vfsExportHandler = new CmsVfsImportExportHandler();
        vfsExportHandler.setFileName(zipExportFilename);
        List exportPaths = new ArrayList(1);
        exportPaths.add(filename1);
        exportPaths.add(filename2);
        vfsExportHandler.setExportPaths(exportPaths);
        vfsExportHandler.setIncludeSystem(false);
        vfsExportHandler.setIncludeUnchanged(true);
        vfsExportHandler.setExportUserdata(false);
        OpenCms.getImportExportManager().exportData(
            cms,
            vfsExportHandler,
            new CmsShellReport(cms.getRequestContext().getLocale()));

        // delete the both files
        cms.lockResource(filename1);
        cms.lockResource(filename2);
        cms.deleteResource(filename1, CmsResource.DELETE_REMOVE_SIBLINGS);
        cms.deleteResource(filename2, CmsResource.DELETE_REMOVE_SIBLINGS);
        // publish the deleted files
        cms.unlockProject(cms.getRequestContext().currentProject().getUuid());
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.getRequestContext().setSiteRoot("/");
        try {
            // re-import the exported files
            OpenCms.getImportExportManager().importData(
                cms,
                zipExportFilename,
                "/",
                new CmsShellReport(cms.getRequestContext().getLocale()));

            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(OpenCmsTestCase.ARTICLE_TYPEID);
            I_CmsLinkParseable validatable = (I_CmsLinkParseable)type;

            // check the links
            CmsFile newRes1 = cms.readFile(filename1);
            List links1 = validatable.parseLinks(cms, newRes1);
            assertEquals(links1.size(), 2);
            assertEquals(links1.get(0).toString(), cms.getRequestContext().addSiteRoot(filename2));
            assertEquals(links1.get(1).toString(), cms.getRequestContext().addSiteRoot(filename2));

            CmsFile newRes2 = cms.readFile(filename2);
            List links2 = validatable.parseLinks(cms, newRes2);
            assertEquals(links2.size(), 2);
            assertEquals(links2.get(0).toString(), cms.getRequestContext().addSiteRoot(filename1));
            assertEquals(links2.get(1).toString(), cms.getRequestContext().addSiteRoot(filename1));
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
     * Tests an overwriting import of VFS data.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testImportExportFolder() throws Exception {

        CmsObject cms = getCmsObject();

        echo("Testing an overwriting import of VFS data.");
        String filename = "folder1/";
        String zipExportFilename = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            "packages/testImportExportFolder.zip");

        try {
            // export the folder
            CmsVfsImportExportHandler vfsExportHandler = new CmsVfsImportExportHandler();
            vfsExportHandler.setFileName(zipExportFilename);
            List exportPaths = new ArrayList(1);
            exportPaths.add(filename);
            vfsExportHandler.setExportPaths(exportPaths);
            vfsExportHandler.setIncludeSystem(false);
            vfsExportHandler.setIncludeUnchanged(true);
            vfsExportHandler.setExportUserdata(false);
            OpenCms.getImportExportManager().exportData(
                cms,
                vfsExportHandler,
                new CmsShellReport(cms.getRequestContext().getLocale()));

            // re-import the exported folder
            OpenCms.getImportExportManager().importData(
                cms,
                zipExportFilename,
                "/",
                new CmsShellReport(cms.getRequestContext().getLocale()));

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
     * Tests import with structure id.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testImportExportId() throws Exception {

        CmsObject cms = getCmsObject();

        echo("Testing import with structure id.");
        String filename = "/dummy2.txt";
        String contentStr = "This is a comment. I love comments.";
        String zipExportFilename = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            "packages/testImportExportId.zip");
        byte[] content = contentStr.getBytes();

        try {
            cms.getRequestContext().setSiteRoot("/");
            // create a dummy plain text file by the temporary user
            cms.createResource(filename, CmsResourceTypePlain.getStaticTypeId(), content, null);

            // publish the dummy plain text file
            cms.unlockResource(filename);
            OpenCms.getPublishManager().publishResource(cms, filename);
            OpenCms.getPublishManager().waitWhileRunning();

            storeResources(cms, filename);

            // export the dummy plain text file
            CmsVfsImportExportHandler vfsExportHandler = new CmsVfsImportExportHandler();
            vfsExportHandler.setFileName(zipExportFilename);
            List exportPaths = new ArrayList(1);
            exportPaths.add(filename);
            vfsExportHandler.setExportPaths(exportPaths);
            vfsExportHandler.setIncludeSystem(false);
            vfsExportHandler.setIncludeUnchanged(true);
            vfsExportHandler.setExportUserdata(false);
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
                zipExportFilename,
                "/",
                new CmsShellReport(cms.getRequestContext().getLocale()));

            // publish the file
            cms.unlockResource(filename);
            OpenCms.getPublishManager().publishResource(cms, filename);
            OpenCms.getPublishManager().waitWhileRunning();

            assertFilter(cms, cms.readResource(filename), OpenCmsTestResourceFilter.FILTER_IMPORTEXPORT);
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
     * Tests the import of a resource that has been moved.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testImportMovedResource() throws Exception {

        CmsObject cms = getCmsObject();

        echo("Testing the import of a resource that has been moved.");
        String filename1 = "/newfile4.html";
        String filename2 = "/movedfile.html";
        String zipExportFilename = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            "packages/testImportExportMovedResource.zip");

        try {
            cms.getRequestContext().setSiteRoot("/");
            // create file
            CmsResource resBefore = cms.createResource(filename1, CmsResourceTypePlain.getStaticTypeId());

            // publish the file
            cms.unlockResource(filename1);
            OpenCms.getPublishManager().publishResource(cms, filename1);
            OpenCms.getPublishManager().waitWhileRunning();

            storeResources(cms, filename1);

            // export the file
            CmsVfsImportExportHandler vfsExportHandler = new CmsVfsImportExportHandler();
            vfsExportHandler.setFileName(zipExportFilename);
            List exportPaths = new ArrayList(1);
            exportPaths.add(filename1);
            vfsExportHandler.setExportPaths(exportPaths);
            vfsExportHandler.setIncludeSystem(false);
            vfsExportHandler.setIncludeUnchanged(true);
            vfsExportHandler.setExportUserdata(false);
            OpenCms.getImportExportManager().exportData(
                cms,
                vfsExportHandler,
                new CmsShellReport(cms.getRequestContext().getLocale()));

            // move the file
            cms.lockResource(filename1);
            cms.moveResource(filename1, filename2);

            // publish the file
            cms.unlockResource(filename2);
            OpenCms.getPublishManager().publishResource(cms, filename2);
            OpenCms.getPublishManager().waitWhileRunning();

            assertFalse(cms.existsResource(filename1));
            assertTrue(cms.existsResource(filename2));

            cms.readResource(resBefore.getStructureId()); // check resource by id
            try {
                cms.readResource(filename1); // check resource by name
                fail("should not be found");
            } catch (Exception e) {
                // ok
            }

            // re-import the exported files
            OpenCms.getImportExportManager().importData(
                cms,
                zipExportFilename,
                "/",
                new CmsShellReport(cms.getRequestContext().getLocale()));

            // publish the file
            cms.unlockResource(filename1);
            OpenCms.getPublishManager().publishResource(cms, filename1);
            OpenCms.getPublishManager().waitWhileRunning();

            assertFalse(cms.existsResource(filename2));
            assertFilter(cms, cms.readResource(filename1), OpenCmsTestResourceFilter.FILTER_IMPORTEXPORT);

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
     * Tests the import of a resource that has been recreated.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testImportRecreatedFile() throws Exception {

        CmsObject cms = getCmsObject();

        echo("Testing the import of a resource that has been recreated.");
        String filename1 = "/newfile5.html";
        String zipExportFilename = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            "packages/testImportRecreatedFile.zip");

        try {
            cms.getRequestContext().setSiteRoot("/");
            // create file
            CmsResource resBefore = cms.createResource(filename1, CmsResourceTypePlain.getStaticTypeId());

            // publish the file
            cms.unlockResource(filename1);
            OpenCms.getPublishManager().publishResource(cms, filename1);
            OpenCms.getPublishManager().waitWhileRunning();

            // save the file state for later comparison
            storeResources(cms, filename1);

            // export the file
            CmsVfsImportExportHandler vfsExportHandler = new CmsVfsImportExportHandler();
            vfsExportHandler.setFileName(zipExportFilename);
            List exportPaths = new ArrayList(1);
            exportPaths.add(filename1);
            vfsExportHandler.setExportPaths(exportPaths);
            vfsExportHandler.setIncludeSystem(false);
            vfsExportHandler.setIncludeUnchanged(true);
            vfsExportHandler.setExportUserdata(false);
            OpenCms.getImportExportManager().exportData(
                cms,
                vfsExportHandler,
                new CmsShellReport(cms.getRequestContext().getLocale()));

            // delete the file
            cms.lockResource(filename1);
            cms.deleteResource(filename1, CmsResource.DELETE_REMOVE_SIBLINGS);

            // publish the deleted file
            cms.unlockResource(filename1);
            OpenCms.getPublishManager().publishResource(cms, filename1);
            OpenCms.getPublishManager().waitWhileRunning();

            assertFalse(cms.existsResource(filename1));

            // create new file at the same location 
            cms.createResource(filename1, CmsResourceTypeImage.getStaticTypeId());

            // publish the new file
            cms.unlockResource(filename1);
            OpenCms.getPublishManager().publishResource(cms, filename1);
            OpenCms.getPublishManager().waitWhileRunning();

            cms.readResource(filename1); // check resource by name
            try {
                cms.readResource(resBefore.getStructureId()); // check resource by id
                fail("should not be found");
            } catch (Exception e) {
                // ok
            }

            // re-import the exported file
            OpenCms.getImportExportManager().importData(
                cms,
                zipExportFilename,
                "/",
                new CmsShellReport(cms.getRequestContext().getLocale()));

            // publish the imported file
            cms.unlockResource(filename1);
            OpenCms.getPublishManager().publishResource(cms, filename1);
            OpenCms.getPublishManager().waitWhileRunning();

            // read the imported file
            CmsResource resAfter = cms.readResource(filename1);

            // check it against the saved version
            assertFilter(cms, resAfter, OpenCmsTestResourceFilter.FILTER_IMPORTEXPORT_OVERWRITE);
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
     * Tests the import of a sibling that has been recreated.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testImportRecreatedSibling() throws Exception {

        CmsObject cms = getCmsObject();

        echo("Testing the import of a sibling that has been recreated.");
        String filename1 = "/newfile3.html";
        String filename2 = "sibling2.html";
        String zipExportFilename = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            "packages/testImportRecreatedFile.zip");

        try {
            cms.getRequestContext().setSiteRoot("/");
            // create files
            CmsResource res1 = cms.createResource(filename1, CmsResourceTypePlain.getStaticTypeId());
            cms.createSibling(filename1, filename2, null);

            // publish the files
            cms.unlockProject(cms.getRequestContext().currentProject().getUuid());
            OpenCms.getPublishManager().publishProject(cms);
            OpenCms.getPublishManager().waitWhileRunning();

            storeResources(cms, filename1);

            // export the file
            CmsVfsImportExportHandler vfsExportHandler = new CmsVfsImportExportHandler();
            vfsExportHandler.setFileName(zipExportFilename);
            List exportPaths = new ArrayList(1);
            exportPaths.add(filename1);
            vfsExportHandler.setExportPaths(exportPaths);
            vfsExportHandler.setIncludeSystem(false);
            vfsExportHandler.setIncludeUnchanged(true);
            vfsExportHandler.setExportUserdata(false);
            OpenCms.getImportExportManager().exportData(
                cms,
                vfsExportHandler,
                new CmsShellReport(cms.getRequestContext().getLocale()));

            cms.lockResource(filename1);
            cms.deleteResource(filename1, CmsResource.DELETE_PRESERVE_SIBLINGS);

            // publish the file
            cms.unlockResource(filename1);
            OpenCms.getPublishManager().publishResource(cms, filename1);
            OpenCms.getPublishManager().waitWhileRunning();

            assertFalse(cms.existsResource(filename1));

            // create new sibling
            cms.createSibling(filename2, filename1, null);

            // publish the file
            cms.unlockResource(filename1);
            OpenCms.getPublishManager().publishResource(cms, filename1);
            OpenCms.getPublishManager().waitWhileRunning();

            cms.readResource(filename1); // check resource by name
            try {
                cms.readResource(res1.getStructureId()); // check resource by id
                fail("should not be found");
            } catch (Exception e) {
                // ok
            }

            // try to re-import the exported files, should fail
            OpenCms.getImportExportManager().importData(
                cms,
                zipExportFilename,
                "/",
                new CmsShellReport(cms.getRequestContext().getLocale()));

            // publish the file
            cms.unlockResource(filename1);
            OpenCms.getPublishManager().publishResource(cms, filename1);
            OpenCms.getPublishManager().waitWhileRunning();

            // read file
            CmsResource resAfter = cms.readResource(filename1);

            // since nothing change
            assertFilter(cms, resAfter, OpenCmsTestResourceFilter.FILTER_IMPORTEXPORT_OVERWRITE);
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
     * Tests the import of a resource that has been edited.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testImportRelations() throws Exception {

        CmsObject cms = getCmsObject();

        echo("Testing the import of a resource with relations.");

        String filename = "/index.html";
        String zipExportFilename = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            "packages/testImportRelations.zip");

        try {
            // first there are no relations
            assertTrue(cms.getRelationsForResource(filename, CmsRelationFilter.TARGETS.filterNotDefinedInContent()).isEmpty());

            // add relation
            cms.lockResource(filename);
            CmsCategoryService catService = CmsCategoryService.getInstance();
            CmsCategory cat = catService.createCategory(cms, null, "abc", "title", "description");
            catService.addResourceToCategory(cms, filename, cat.getPath());

            // now check the new relation
            List relations = cms.getRelationsForResource(
                filename,
                CmsRelationFilter.TARGETS.filterNotDefinedInContent());
            assertEquals(1, relations.size());
            assertRelation(new CmsRelation(
                cms.readResource(filename),
                cms.readResource(cat.getId()),
                CmsRelationType.CATEGORY), (CmsRelation)relations.get(0));

            // export the file
            CmsVfsImportExportHandler vfsExportHandler = new CmsVfsImportExportHandler();
            vfsExportHandler.setFileName(zipExportFilename);
            List exportPaths = new ArrayList(1);
            exportPaths.add(filename);
            vfsExportHandler.setExportPaths(exportPaths);
            vfsExportHandler.setIncludeSystem(false);
            vfsExportHandler.setIncludeUnchanged(true);
            vfsExportHandler.setExportUserdata(false);
            OpenCms.getImportExportManager().exportData(
                cms,
                vfsExportHandler,
                new CmsShellReport(cms.getRequestContext().getLocale()));

            // delete resource
            cms.deleteResource(filename, CmsResource.DELETE_PRESERVE_SIBLINGS);

            // publish
            OpenCms.getPublishManager().publishResource(cms, filename);
            OpenCms.getPublishManager().waitWhileRunning();

            // create new res
            cms.createResource(filename, CmsResourceTypePlain.getStaticTypeId());

            // recheck that there are no relations
            assertTrue(cms.getRelationsForResource(filename, CmsRelationFilter.TARGETS.filterNotDefinedInContent()).isEmpty());

            // re-import the exported files
            OpenCms.getImportExportManager().importData(
                cms,
                zipExportFilename,
                "/",
                new CmsShellReport(cms.getRequestContext().getLocale()));

            // now check the imported relation
            relations = cms.getRelationsForResource(filename, CmsRelationFilter.TARGETS.filterNotDefinedInContent());
            assertEquals(1, relations.size());
            assertRelation(new CmsRelation(
                cms.readResource(filename),
                cms.readResource(cat.getId()),
                CmsRelationType.CATEGORY), (CmsRelation)relations.get(0));
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
     * Tests the resource translation during import.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testImportResourceTranslator() throws Exception {

        echo("Testing resource translator for import");
        CmsObject cms = OpenCms.initCmsObject(getCmsObject());

        cms.getRequestContext().setSiteRoot("/");

        // need to create the "galleries" folder manually
        cms.createResource("/system/galleries", CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.unlockResource("/system/galleries");

        CmsResourceTranslator oldFolderTranslator = OpenCms.getResourceManager().getFolderTranslator();

        CmsResourceTranslator folderTranslator = new CmsResourceTranslator(new String[] {
            "s#^/sites/default/content/bodys(.*)#/system/bodies$1#",
            "s#^/sites/default/pics/system(.*)#/system/workplace/resources$1#",
            "s#^/sites/default/pics(.*)#/system/galleries/pics$1#",
            "s#^/sites/default/download(.*)#/system/galleries/download$1#",
            "s#^/sites/default/externallinks(.*)#/system/galleries/externallinks$1#",
            "s#^/sites/default/htmlgalleries(.*)#/system/galleries/htmlgalleries$1#",
            "s#^/sites/default/content(.*)#/system$1#"}, false);

        // set modified folder translator
        OpenCms.getResourceManager().setTranslators(folderTranslator, OpenCms.getResourceManager().getFileTranslator());

        // update OpenCms context to ensure new translator is used
        cms = getCmsObject();

        cms.getRequestContext().setSiteRoot("/sites/default");

        // import the files
        String importFile = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf("packages/testimport01.zip");
        OpenCms.getImportExportManager().importData(
            cms,
            importFile,
            "/",
            new CmsShellReport(cms.getRequestContext().getLocale()));

        // check the results of the import
        CmsXmlPage page;
        CmsFile file;
        CmsLinkTable table;
        List links;
        Iterator i;
        List siblings;

        // test "/importtest/index.html"
        file = cms.readFile("/importtest/index.html");
        page = CmsXmlPageFactory.unmarshal(cms, file);

        table = page.getLinkTable("body", CmsLocaleManager.getDefaultLocale());
        links = new ArrayList();
        i = table.iterator();
        while (i.hasNext()) {
            CmsLink link = (CmsLink)i.next();
            links.add(link.toString());
        }
        assertTrue(links.size() == 2);
        assertTrue(links.contains("/sites/default/importtest/page2.html"));
        assertTrue(links.contains("/sites/default/importtest/page3.html"));

        siblings = cms.readSiblings("/importtest/index.html", CmsResourceFilter.ALL);
        i = siblings.iterator();
        links = new ArrayList();
        while (i.hasNext()) {
            CmsResource sibling = (CmsResource)i.next();
            links.add(sibling.getRootPath());
        }
        assertEquals(2, links.size());
        assertTrue(links.contains("/sites/default/importtest/index.html"));
        assertTrue(links.contains("/sites/default/importtest/linktest.html"));

        // test "/importtest/page2.html"
        file = cms.readFile("/importtest/page2.html");
        page = CmsXmlPageFactory.unmarshal(cms, file);

        table = page.getLinkTable("body", CmsLocaleManager.getDefaultLocale());
        links = new ArrayList();
        i = table.iterator();
        while (i.hasNext()) {
            CmsLink link = (CmsLink)i.next();
            links.add(link.toString());
            System.out.println("Link: " + link.toString());
        }
        assertEquals(2, links.size());
        assertTrue(links.contains("/system/galleries/pics/_anfang/bg_teaser_test2.jpg"));
        assertTrue(links.contains("/sites/default/importtest/index.html"));

        // test "/importtest/linktest.html" (sibling of "/importtest/index.html")
        file = cms.readFile("/importtest/linktest.html");
        assertEquals(CmsResourceTypeXmlPage.getStaticTypeId(), file.getTypeId());
        page = CmsXmlPageFactory.unmarshal(cms, file);

        table = page.getLinkTable("body", CmsLocaleManager.getDefaultLocale());
        links = new ArrayList();
        i = table.iterator();
        while (i.hasNext()) {
            CmsLink link = (CmsLink)i.next();
            links.add(link.toString());
            System.out.println("Link: " + link.toString());
        }
        assertEquals(2, links.size());
        assertTrue(links.contains("/sites/default/importtest/page2.html"));
        assertTrue(links.contains("/sites/default/importtest/page3.html"));

        siblings = cms.readSiblings("/importtest/linktest.html", CmsResourceFilter.ALL);
        i = siblings.iterator();
        links = new ArrayList();
        while (i.hasNext()) {
            CmsResource sibling = (CmsResource)i.next();
            links.add(sibling.getRootPath());
        }
        assertEquals(2, links.size());
        assertTrue(links.contains("/sites/default/importtest/index.html"));
        assertTrue(links.contains("/sites/default/importtest/linktest.html"));

        // test "/othertest/index.html"
        file = cms.readFile("/othertest/index.html");
        page = CmsXmlPageFactory.unmarshal(cms, file);

        table = page.getLinkTable("body", CmsLocaleManager.getDefaultLocale());
        links = new ArrayList();
        i = table.iterator();
        while (i.hasNext()) {
            CmsLink link = (CmsLink)i.next();
            links.add(link.toString());
            System.out.println("Link: " + link.toString());
        }
        assertTrue(links.size() == 2);
        assertTrue(links.contains("/sites/default/importtest/page2.html"));
        assertTrue(links.contains("/sites/default/importtest/page3.html"));

        // clean up for the next test
        cms.getRequestContext().setSiteRoot("/");
        cms.lockResource("/sites/default");
        cms.lockResource("/system");
        cms.deleteResource("/sites/default/importtest", CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.deleteResource("/system/galleries/pics", CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.unlockResource("/sites/default");
        cms.unlockResource("/system");
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        // reset the translation rules
        OpenCms.getResourceManager().setTranslators(
            oldFolderTranslator,
            OpenCms.getResourceManager().getFileTranslator());
    }

    /**
     * Tests the resource translation during import with multiple sites.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testImportResourceTranslatorMultipleSite() throws Exception {

        echo("Testing resource translator with multiple sites");
        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("/");

        // create a second site
        cms.createResource("/sites/mysite", CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.unlockResource("/sites/mysite");

        cms.createResource("/sites/othersite", CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        cms.unlockResource("/sites/othersite");

        CmsResourceTranslator oldFolderTranslator = OpenCms.getResourceManager().getFolderTranslator();

        CmsResourceTranslator folderTranslator = new CmsResourceTranslator(new String[] {
            "s#^/sites(.*)#/sites$1#",
            "s#^/system(.*)#/system$1#",
            "s#^/content/bodys(.*)#/system/bodies$1#",
            "s#^/pics(.*)#/system/galleries/pics$1#",
            "s#^/download(.*)#/system/galleries/download$1#",
            "s#^/externallinks(.*)#/system/galleries/externallinks$1#",
            "s#^/htmlgalleries(.*)#/system/galleries/htmlgalleries$1#",
            "s#^/content(.*)#/system$1#",
            "s#^/othertest(.*)#/sites/othersite$1#",
            "s#^/(.*)#/sites/mysite/$1#"}, false);

        // set modified folder translator
        OpenCms.getResourceManager().setTranslators(folderTranslator, OpenCms.getResourceManager().getFileTranslator());

        // update OpenCms context to ensure new translator is used
        cms = getCmsObject();

        // set root site
        cms.getRequestContext().setSiteRoot("/");

        // import the files
        String importFile = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf("packages/testimport01.zip");
        OpenCms.getImportExportManager().importData(
            cms,
            importFile,
            "/",
            new CmsShellReport(cms.getRequestContext().getLocale()));

        // now switch to "mysite"
        cms.getRequestContext().setSiteRoot("/sites/mysite/");

        // check the results of the import
        CmsXmlPage page;
        CmsFile file;
        CmsLinkTable table;
        List links;
        Iterator i;
        List siblings;

        // test "/importtest/index.html"
        file = cms.readFile("/importtest/index.html");
        page = CmsXmlPageFactory.unmarshal(cms, file);

        table = page.getLinkTable("body", CmsLocaleManager.getDefaultLocale());
        links = new ArrayList();
        i = table.iterator();
        while (i.hasNext()) {
            CmsLink link = (CmsLink)i.next();
            links.add(link.toString());
        }
        assertTrue(links.size() == 2);
        assertTrue(links.contains("/sites/mysite/importtest/page2.html"));
        assertTrue(links.contains("/sites/mysite/importtest/page3.html"));

        siblings = cms.readSiblings("/importtest/index.html", CmsResourceFilter.ALL);
        i = siblings.iterator();
        links = new ArrayList();
        while (i.hasNext()) {
            CmsResource sibling = (CmsResource)i.next();
            links.add(sibling.getRootPath());
        }
        assertEquals(2, links.size());
        assertTrue(links.contains("/sites/mysite/importtest/index.html"));
        assertTrue(links.contains("/sites/mysite/importtest/linktest.html"));

        // test "/importtest/page2.html"
        file = cms.readFile("/importtest/page2.html");
        page = CmsXmlPageFactory.unmarshal(cms, file);

        table = page.getLinkTable("body", CmsLocaleManager.getDefaultLocale());
        links = new ArrayList();
        i = table.iterator();
        while (i.hasNext()) {
            CmsLink link = (CmsLink)i.next();
            links.add(link.toString());
        }
        assertEquals(2, links.size());
        assertTrue(links.contains("/system/galleries/pics/_anfang/bg_teaser_test2.jpg"));
        assertTrue(links.contains("/sites/mysite/importtest/index.html"));

        // test "/importtest/linktest.html" (sibling of "/importtest/index.html")
        file = cms.readFile("/importtest/linktest.html");
        assertEquals(CmsResourceTypeXmlPage.getStaticTypeId(), file.getTypeId());
        page = CmsXmlPageFactory.unmarshal(cms, file);

        table = page.getLinkTable("body", CmsLocaleManager.getDefaultLocale());
        links = new ArrayList();
        i = table.iterator();
        while (i.hasNext()) {
            CmsLink link = (CmsLink)i.next();
            links.add(link.toString());
            System.out.println("Link: " + link.toString());
        }
        assertEquals(2, links.size());
        assertTrue(links.contains("/sites/mysite/importtest/page2.html"));
        assertTrue(links.contains("/sites/mysite/importtest/page3.html"));

        siblings = cms.readSiblings("/importtest/linktest.html", CmsResourceFilter.ALL);
        i = siblings.iterator();
        links = new ArrayList();
        while (i.hasNext()) {
            CmsResource sibling = (CmsResource)i.next();
            links.add(sibling.getRootPath());
            System.out.println("Sibling: " + sibling.toString());
        }
        assertEquals(2, links.size());
        assertTrue(links.contains("/sites/mysite/importtest/index.html"));
        assertTrue(links.contains("/sites/mysite/importtest/linktest.html"));

        // now switch to "othersite"
        cms.getRequestContext().setSiteRoot("/sites/othersite/");

        // test "/othertest/index.html"
        file = cms.readFile("/index.html");
        page = CmsXmlPageFactory.unmarshal(cms, file);

        table = page.getLinkTable("body", CmsLocaleManager.getDefaultLocale());
        links = new ArrayList();
        i = table.iterator();
        while (i.hasNext()) {
            CmsLink link = (CmsLink)i.next();
            links.add(link.toString());
            System.out.println("Link: " + link.toString());
        }
        assertTrue(links.size() == 2);
        assertTrue(links.contains("/sites/mysite/importtest/page2.html"));
        assertTrue(links.contains("/sites/mysite/importtest/page3.html"));

        // reset the translation rules
        OpenCms.getResourceManager().setTranslators(
            oldFolderTranslator,
            OpenCms.getResourceManager().getFileTranslator());
    }

    /**
     * Tests the import of a sibling.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testImportSibling() throws Exception {

        CmsObject cms = getCmsObject();

        echo("Testing the import of a sibling.");
        String filename1 = "/newfile2.html";
        String filename2 = "sibling.html";
        String zipExportFilename = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            "packages/testImportRecreatedFile.zip");

        try {
            cms.getRequestContext().setSiteRoot("/");
            // create files
            CmsResource res1 = cms.createResource(filename1, CmsResourceTypePlain.getStaticTypeId());
            cms.createSibling(filename1, filename2, null);

            // publish the files
            cms.unlockProject(cms.getRequestContext().currentProject().getUuid());
            OpenCms.getPublishManager().publishProject(cms);
            OpenCms.getPublishManager().waitWhileRunning();

            storeResources(cms, filename1);

            // export the file
            CmsVfsImportExportHandler vfsExportHandler = new CmsVfsImportExportHandler();
            vfsExportHandler.setFileName(zipExportFilename);
            List exportPaths = new ArrayList(1);
            exportPaths.add(filename1);
            vfsExportHandler.setExportPaths(exportPaths);
            vfsExportHandler.setIncludeSystem(false);
            vfsExportHandler.setIncludeUnchanged(true);
            vfsExportHandler.setExportUserdata(false);
            OpenCms.getImportExportManager().exportData(
                cms,
                vfsExportHandler,
                new CmsShellReport(cms.getRequestContext().getLocale()));

            cms.lockResource(filename1);
            cms.deleteResource(filename1, CmsResource.DELETE_PRESERVE_SIBLINGS);

            // publish the file
            cms.unlockResource(filename1);
            OpenCms.getPublishManager().publishResource(cms, filename1);
            OpenCms.getPublishManager().waitWhileRunning();

            assertFalse(cms.existsResource(filename1));

            try {
                cms.readResource(filename1); // check resource by name
                fail("should not be found");
            } catch (Exception e) {
                // ok
            }
            try {
                cms.readResource(res1.getStructureId()); // check resource by id
                fail("should not be found");
            } catch (Exception e) {
                // ok
            }

            // re-import the exported files
            OpenCms.getImportExportManager().importData(
                cms,
                zipExportFilename,
                "/",
                new CmsShellReport(cms.getRequestContext().getLocale()));

            // publish the file
            cms.unlockResource(filename1);
            OpenCms.getPublishManager().publishResource(cms, filename1);
            OpenCms.getPublishManager().waitWhileRunning();

            assertFilter(cms, cms.readResource(filename1), OpenCmsTestResourceFilter.FILTER_IMPORTEXPORT_SIBLING);
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
     * Tests the import of resources during setup.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testSetup() throws Exception {

        CmsObject cms = getCmsObject();

        echo("Testing the import of resources during setup.");

        CmsResource resource = cms.readResource("index.html");

        long expectedDateCreated = convertTimestamp("Tue, 01 Jun 2004 09:11:24 GMT");
        long expectedDateLastModified = convertTimestamp("Wed, 27 Sep 2006 15:11:58 GMT");

        assertEquals(expectedDateCreated, resource.getDateCreated());
        assertEquals(expectedDateLastModified, resource.getDateLastModified());
    }

    /**
     * Tests if the user passwords are imported correctly.<p>
     * The password digests are hex-128 (legacy) encoded, and
     * should be converted to base-64. 
     * 
     * @throws Exception if something goes wrong
     */
    public void testUserImport() throws Exception {

        CmsObject cms = getCmsObject();

        echo("Testing the user import.");
        I_CmsPasswordHandler passwordHandler = new CmsDefaultPasswordHandler();
        CmsUser user;

        // check if passwords will be converted
        echo("Testing passwords of imported users");

        // check the admin user
        user = cms.readUser("Admin");
        assertEquals(user.getPassword(), passwordHandler.digest("admin", "MD5", CmsEncoder.ENCODING_UTF_8));

        // check the guest user
        user = cms.readUser("Guest");
        assertEquals(user.getPassword(), passwordHandler.digest("", "MD5", CmsEncoder.ENCODING_UTF_8));

        // check the test1 user
        user = cms.readUser("test1");
        assertEquals(user.getPassword(), passwordHandler.digest("test1", "MD5", CmsEncoder.ENCODING_UTF_8));

        // check the test2 user
        user = cms.readUser("test2");
        assertEquals(user.getPassword(), passwordHandler.digest("test2", "MD5", CmsEncoder.ENCODING_UTF_8));
    }

    /**
     * Convert a given timestamp from a String format to a long value.<p>
     * 
     * The timestamp is either the string representation of a long value (old export format)
     * or a user-readable string format.
     * 
     * @param timestamp timestamp to convert
     * @return long value of the timestamp
     */
    private long convertTimestamp(String timestamp) {

        long value = 0;
        // try to parse the timestamp string
        // if it successes, its an old style long value
        try {
            value = Long.parseLong(timestamp);

        } catch (NumberFormatException e) {
            // the timestamp was in in a user-readable string format, create the long value form it
            try {
                value = CmsDateUtil.parseHeaderDate(timestamp);
            } catch (ParseException pe) {
                value = System.currentTimeMillis();
            }
        }
        return value;
    }
}
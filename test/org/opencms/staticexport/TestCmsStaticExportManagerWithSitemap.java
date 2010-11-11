/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/staticexport/Attic/TestCmsStaticExportManagerWithSitemap.java,v $
 * Date   : $Date: 2010/11/11 13:08:18 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.staticexport;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishManager;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.xml.sitemap.CmsInternalSitemapEntry;
import org.opencms.xml.sitemap.CmsSitemapEntry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for static export manager with sitemap and containerpages in use.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class TestCmsStaticExportManagerWithSitemap extends OpenCmsTestCase {

    /**
     * The temp file.<p>
     */
    private static final File TMP_FILE = new File("test"
        + File.separator
        + "data"
        + File.separator
        + "tmp"
        + File.separator
        + "temp-exportdata.txt");

    /** /sites/default/cntpages/index.html. */
    private String m_rfsNameForContainerPage = "/data/export/sites/default/";

    /** /sites/default/cntpages/index.html. */
    private String m_rfsNameForContainerPageExportname = "/data/export/t3/";

    /** /sites/default/contents/content_0001.xml. */
    private String m_rfsNameForDetailPage = "/data/export/sites/default/item_2/item_1/c611b7c6-d6a2-11df-869e-edac56888092/";

    /** /sites/default/contents/content_0001.xml. */
    private String m_rfsNameForDetailPageExportname = "/data/export/t3/item_2/item_1/c611b7c6-d6a2-11df-869e-edac56888092/";

    /** /system/modules/org.opencms.ade.containerpage/schemas/field.xsd. */
    private String m_rfsNameForSystemResource = "/data/export/system/modules/org.opencms.ade.containerpage/schemas/field.xsd";

    /** /system/modules/org.opencms.ade.containerpage/schemas/field.xsd. */
    private String m_rfsNameForSystemResourceExportname = "/data/export/system/modules/org.opencms.ade.containerpage/schemas/field.xsd";

    /** /sites/default/_config/content.xsd. */
    private String m_rfsNameForVfsResource = "/data/export/sites/default/_config/content.xsd";

    /** /sites/default/_config/content.xsd. */
    private String m_rfsNameForVfsResourceExportname = "/data/export/sites/default/_config/content.xsd";

    /** /sites/default/cntpages/index.html. */
    private String m_vfsNameForContainerPage = "/sites/default/";

    /** /sites/default/contents/content_0001.xml. */
    private String m_vfsNameForDetailPage = "/sites/default/item_2/item_1/c611b7c6-d6a2-11df-869e-edac56888092/";

    /** /system/modules/org.opencms.ade.containerpage/schemas/field.xsd. */
    private String m_vfsNameForSystemResource = "/system/modules/org.opencms.ade.containerpage/schemas/field.xsd";

    /** /sites/default/_config/content.xsd. */
    private String m_vfsNameForVfsResource = "/sites/default/_config/content.xsd";

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsStaticExportManagerWithSitemap(String arg0) {

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
        suite.setName(TestCmsStaticExportManagerWithSitemap.class.getName());

        suite.addTest(new TestCmsStaticExportManagerWithSitemap("testIsExportLink"));
        suite.addTest(new TestCmsStaticExportManagerWithSitemap("testRfsName"));
        suite.addTest(new TestCmsStaticExportManagerWithSitemap("testRfsNameWithExportName"));
        suite.addTest(new TestCmsStaticExportManagerWithSitemap("testVfsNameInternal"));
        suite.addTest(new TestCmsStaticExportManagerWithSitemap("testVfsNameInternalWithExportname"));
        suite.addTest(new TestCmsStaticExportManagerWithSitemap("testVfsNameInternalWithParameters"));
        suite.addTest(new TestCmsStaticExportManagerWithSitemap("testVfsNameInternalWithExportnameAndParameters"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("ade-modules", "/");
                importData("ade-content", "/");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
                try {
                    deleteTmpFile();
                } catch (Exception e) {
                    System.out.println(e.getLocalizedMessage());
                }
            }
        };
        return wrapper;
    }

    /**
     * Deletes the temp file with its parent folder.<p>
     * 
     * @return true if successfully
     * 
     * @throws Exception if something goes wrong
     */
    protected static boolean deleteTmpFile() throws Exception {

        boolean success = false;
        if (TMP_FILE.getParentFile().exists() && TMP_FILE.getParentFile().canWrite()) {
            success = deleteTree(TMP_FILE.getParentFile());
        }
        return success;
    }

    /**
     * Deletes a tree.<p>
     * 
     * @param path the path to delete
     * 
     * @return true if successfully
     */
    private static boolean deleteTree(File path) {

        for (File file : path.listFiles()) {
            if (file.isDirectory()) {
                deleteTree(file);
            } else {
                file.delete();
            }
        }
        return path.delete();
    }

    /**
     * Tests the returned value of the method {@link CmsStaticExportManager#isExportLink(CmsObject, String)}
     * for some selected resources.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testIsExportLink() throws Exception {

        echo("Testing is export link");

        if (changeToNormal()) {
            CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            boolean isSystemResourceToBeExported = OpenCms.getStaticExportManager().isExportLink(
                cms,
                m_vfsNameForSystemResource);
            assertTrue(!isSystemResourceToBeExported);

            boolean isVfsResourceToBeExported = OpenCms.getStaticExportManager().isExportLink(
                cms,
                m_vfsNameForVfsResource);
            assertTrue(!isVfsResourceToBeExported);

            boolean isCntPageToBeExported = OpenCms.getStaticExportManager().isExportLink(
                cms,
                m_vfsNameForContainerPage);
            assertTrue(isCntPageToBeExported);

            boolean isDetailPageToBeExported = OpenCms.getStaticExportManager().isExportLink(
                cms,
                m_vfsNameForDetailPage);
            assertTrue(isDetailPageToBeExported);

        } else {
            assertTrue(false);
        }
    }

    /**
     * Tests the returned rfs name for a sitemap configuration without an export name.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testRfsName() throws Exception {

        echo("Testing get rfs name");

        if (changeToNormal()) {
            CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            String rfsNameForSystemResource = OpenCms.getStaticExportManager().getRfsName(
                cms,
                m_vfsNameForSystemResource);
            assertEquals(m_rfsNameForSystemResource, rfsNameForSystemResource);

            String rfsNameForVfsResource = OpenCms.getStaticExportManager().getRfsName(cms, m_vfsNameForVfsResource);
            assertEquals(m_rfsNameForVfsResource, rfsNameForVfsResource);

            String rfsNameForContainerPage = OpenCms.getStaticExportManager().getRfsName(cms, m_vfsNameForContainerPage);
            assertEquals(m_rfsNameForContainerPage, rfsNameForContainerPage);

            String rfsNameForDetailPage = OpenCms.getStaticExportManager().getRfsName(cms, m_vfsNameForDetailPage);
            assertEquals(m_rfsNameForDetailPage, rfsNameForDetailPage);
        } else {
            assertTrue(false);
        }
    }

    /**
     * Tests the returned rfs name for a sitemap configuration with an export name.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testRfsNameWithExportName() throws Exception {

        echo("Testing get rfs name with export name");
        addUrlNameMapping();

        if (changeToExportName()) {
            CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            String rfsNameForSystemResource = OpenCms.getStaticExportManager().getRfsName(
                cms,
                m_vfsNameForSystemResource);
            assertEquals(m_rfsNameForSystemResourceExportname, rfsNameForSystemResource);

            String rfsNameForVfsResource = OpenCms.getStaticExportManager().getRfsName(cms, m_vfsNameForVfsResource);
            assertEquals(m_rfsNameForVfsResourceExportname, rfsNameForVfsResource);

            String rfsNameForContainerPage = OpenCms.getStaticExportManager().getRfsName(cms, m_vfsNameForContainerPage);
            assertEquals(m_rfsNameForContainerPageExportname, rfsNameForContainerPage);

            String rfsNameForDetailPage = OpenCms.getStaticExportManager().getRfsName(cms, m_vfsNameForDetailPage);
            assertEquals(m_rfsNameForDetailPageExportname, rfsNameForDetailPage);
        } else {
            assertTrue(false);
        }
    }

    /**
     * Tests if the static export data object returned by the method 
     * {@link CmsStaticExportManager#getVfsNameInternal(CmsObject, String)}
     * is identical to the expected data objects defined in the file
     * <code>"export-data.txt"</code> in this package.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testVfsNameInternal() throws Exception {

        echo("Testing get vfs name internal");
        addUrlNameMapping();
        if (changeToNormal()) {
            File tobeExportDataFile = new File(getClass().getResource("export-data.txt").getFile());
            testVfsInt(tobeExportDataFile, null);
        } else {
            assertTrue(false);
        }
    }

    /**
     * Tests if the static export data object returned by the method 
     * {@link CmsStaticExportManager#getVfsNameInternal(CmsObject, String)}
     * is identical to the expected data objects defined in the file
     * <code>"export-data.txt"</code> in this package.<p>
     * 
     * @throws Exception throws an exception if something goes wrong
     */
    public void testVfsNameInternalWithExportname() throws Exception {

        echo("Testing get vfs name internal with export name");
        addUrlNameMapping();
        if (changeToExportName()) {
            File tobeExportDataFile = new File(getClass().getResource("export-data-exportname.txt").getFile());
            testVfsInt(tobeExportDataFile, null);
        } else {
            assertTrue(false);
        }
    }

    /**
     * Tests if the static export data object returned by the method 
     * {@link CmsStaticExportManager#getVfsNameInternal(CmsObject, String)}
     * is identical to the expected data objects defined in the file
     * <code>"export-data.txt"</code> in this package.<p>
     * 
     * @throws Exception throws an exception if something goes wrong
     */
    public void testVfsNameInternalWithExportnameAndParameters() throws Exception {

        echo("Testing get vfs name internal with export name");
        addUrlNameMapping();

        if (changeToExportName()) {
            File tobeExportDataFile = new File(
                getClass().getResource("export-data-exportname-parameters.txt").getFile());
            testVfsInt(tobeExportDataFile, "?a=b&c=d");
        } else {
            assertTrue(false);
        }
    }

    /**
     * Tests if the static export data object returned by the method 
     * {@link CmsStaticExportManager#getVfsNameInternal(CmsObject, String)}
     * is identical to the expected data objects defined in the file
     * <code>"export-data.txt"</code> in this package.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testVfsNameInternalWithParameters() throws Exception {

        echo("Testing get vfs name internal");
        addUrlNameMapping();

        if (changeToNormal()) {
            File tobeExportDataFile = new File(getClass().getResource("export-data-parameters.txt").getFile());
            testVfsInt(tobeExportDataFile, "?a=b&c=d");
        } else {
            assertTrue(false);
        }
    }

    /** 
     * Helper method to add an URL name for a single XML content.<p> 
     * 
     * @throws Exception if something goes wrong  
     */
    private void addUrlNameMapping() throws Exception {

        CmsObject cms = OpenCms.initCmsObject(getCmsObject());
        CmsProject offline = cms.readProject("Offline");
        String resName = "/contents/content_0003.xml";
        cms.getRequestContext().setCurrentProject(offline);
        CmsResource res = cms.readResource(resName);
        cms.lockResource(resName);
        cms.setDateLastModified(resName, System.currentTimeMillis(), false);
        //cms.unlockResource(resName);
        if (cms.readNewestUrlNameForId(res.getStructureId()) == null) {
            System.out.println("========== adding new urlname mapping ==========");
            cms.writeUrlNameMapping("hello_world", res.getStructureId());
            CmsPublishManager pubMan = OpenCms.getPublishManager();
            pubMan.publishProject(cms);
            pubMan.waitWhileRunning();
        }
    }

    /**
     * Changes the <code>"ade.sitemap"</code> property to use a configuration with an exportname.<p>
     * 
     * @return true if the change was successful
     * 
     * @throws Exception if something goes wrong
     */
    private boolean changeToExportName() throws Exception {

        boolean success = true;
        CmsObject adminCms = getCmsObject();
        adminCms.getRequestContext().setSiteRoot("/");
        if (adminCms.getLock(adminCms.readResource("/sites/default/")).isLockableBy(
            adminCms.getRequestContext().currentUser())) {
            adminCms.lockResource("/sites/default/");
            CmsProperty sitemapProp = new CmsProperty(
                "ade.sitemap",
                "/_config/sitemap_exportname",
                "/_config/sitemap_exportname",
                true);
            adminCms.writePropertyObject("/sites/default/", sitemapProp);
        } else {
            success = false;
        }
        if (adminCms.getLock(adminCms.readResource("/system/modules/org.opencms.ade.sitemap/schemas/")).isLockableBy(
            adminCms.getRequestContext().currentUser())) {
            adminCms.lockResource("/system/modules/org.opencms.ade.sitemap/schemas/");
            CmsProperty exportNameProp = new CmsProperty("exportname", "sitemap-schemas", "sitemap-schemas", true);
            adminCms.writePropertyObject("/system/modules/org.opencms.ade.sitemap/schemas/", exportNameProp);
        } else {
            success = false;
        }
        OpenCms.getPublishManager().publishProject(adminCms);
        OpenCms.getPublishManager().waitWhileRunning();
        OpenCms.getMemoryMonitor().clearCache();
        return success;
    }

    /**
     * Changes the <code>"ade.sitemap"</code> property to use a configuration without an exportname.<p>
     * 
     * @return true if the change was successful
     * 
     * @throws Exception if something goes wrong
     */
    private boolean changeToNormal() throws Exception {

        boolean success = true;
        CmsObject adminCms = getCmsObject();
        adminCms.getRequestContext().setSiteRoot("/");
        if (adminCms.getLock(adminCms.readResource("/sites/default/")).isLockableBy(
            adminCms.getRequestContext().currentUser())) {
            adminCms.lockResource("/sites/default/");
            CmsProperty sitemapProp = new CmsProperty("ade.sitemap", "/_config/sitemap", "/_config/sitemap", true);
            adminCms.writePropertyObject("/sites/default/", sitemapProp);
        } else {
            success = false;
        }
        if (adminCms.getLock(adminCms.readResource("/system/modules/org.opencms.ade.sitemap/schemas/")).isLockableBy(
            adminCms.getRequestContext().currentUser())) {
            adminCms.lockResource("/system/modules/org.opencms.ade.sitemap/schemas/");
            CmsProperty exportNameProp = new CmsProperty("exportname", "", "", true);
            adminCms.writePropertyObject("/system/modules/org.opencms.ade.sitemap/schemas/", exportNameProp);
        } else {
            success = false;
        }
        OpenCms.getPublishManager().publishProject(adminCms);
        OpenCms.getPublishManager().waitWhileRunning();
        OpenCms.getMemoryMonitor().clearCache();
        return success;
    }

    /**
     * Compares two files for having identically content.<p>
     * 
     * @param f1 the file one
     * @param f2 the file two
     * 
     * @return <code>true</code> if the two files have the same content, <code>false</code> otherwise
     */
    private boolean compareFiles(File f1, File f2) {

        boolean same = true;

        try {
            BufferedReader in1 = new BufferedReader(new FileReader(f1));
            BufferedReader in2 = new BufferedReader(new FileReader(f2));

            String inText1, inText2;

            while ((inText1 = in1.readLine()) != null) {
                inText2 = in2.readLine();
                if ((inText2 == null) || (!inText1.equals(inText2))) {
                    same = false;
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            same = false;
        } catch (IOException e) {
            e.printStackTrace();
            same = false;
        }

        return same;
    }

    private boolean createTmpFile() throws Exception {

        boolean success = false;
        if (TMP_FILE.exists()) {
            deleteTmpFile();
        }
        if (!TMP_FILE.exists()) {
            TMP_FILE.getParentFile().mkdirs();
            success = TMP_FILE.createNewFile();
        }
        return success;
    }

    /**
     * Goes over all sitemap entries and writes a temporary file with the
     * information from the export data objects returned by the method
     * {@link CmsStaticExportManager#getRfsExportData(CmsObject, String)}.<p>
     * 
     * @param entries the root entries of all configured sitemaps
     */
    private void testEntries(List<CmsInternalSitemapEntry> entries, String parameters) throws Exception {

        CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
        CmsStaticExportManager manager = OpenCms.getStaticExportManager();

        for (CmsInternalSitemapEntry entry : entries) {

            CmsStaticExportData data;

            // get the rfsName for the current entry
            String rfsName = manager.getRfsName(cms, entry.getRootPath(), parameters);

            // get the rfs prefix for the current entry
            String rfsPrefix = OpenCms.getStaticExportManager().getRfsPrefixForRfsName(rfsName);
            // substring the rfsName for the prefix
            rfsName = rfsName.substring(rfsPrefix.length());
            // execute the method to test
            data = manager.getRfsExportData(cms, rfsName);
            // add the calculated rfs name to the data object
            data.setRfsName(rfsName);

            // write the object data to a pseudo xml file
            if (TMP_FILE.exists()) {
                FileWriter fw = new FileWriter(TMP_FILE, true);
                PrintWriter out = new PrintWriter(fw);
                out.println("<exportdata>");
                out.println("  <vfsName>" + data.getVfsName() + "</vfsName>");
                out.println("  <rfsName>" + data.getRfsName() + "</rfsName>");
                out.println("  <resName>" + data.getResource().getRootPath() + "</resName>");
                out.println("  <parameters>" + data.getParameters() + "</parameters>");
                out.println("</exportdata>");
                out.close();
            }

            if (entry.isSitemap()) {
                testEntries(entry.getSubEntries(), parameters);
            }
        }
    }

    /**
     * Helper for vfs internal test.<p>
     * 
     * @param tobeExportDataFile the file with the target state
     * 
     * @throws Exception if something goes wrong
     */
    private void testVfsInt(File tobeExportDataFile, String parameters) throws Exception {

        createTmpFile();
        // get the export cms
        CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());

        // test the entire sitemap
        List<CmsSitemapEntry> rootEntries = OpenCms.getSitemapManager().getRootSitemapRootEntries(cms);
        for (CmsSitemapEntry rootEntry : rootEntries) {
            CmsInternalSitemapEntry entry = (CmsInternalSitemapEntry)rootEntry;
            List<CmsInternalSitemapEntry> entries = entry.getSubEntries();
            testEntries(entries, parameters);
        }

        // now test some detail pages as sitemap entry 
        List<CmsInternalSitemapEntry> detailEntries = new ArrayList<CmsInternalSitemapEntry>();
        List<CmsResource> detailResources = cms.readResources("/sites/default/contents/", CmsResourceFilter.ALL);
        for (CmsResource res : detailResources) {
            String uri = "/sites/default/item_2/item_1/" + res.getStructureId();
            if (res.getRootPath().endsWith("content_0003.xml")) {
                uri = "/sites/default/item_2/item_1/hello_world";
            }
            CmsInternalSitemapEntry entry = (CmsInternalSitemapEntry)OpenCms.getSitemapManager().getEntryForUri(
                cms,
                uri);
            detailEntries.add(entry);
        }
        testEntries(detailEntries, parameters);

        // now test some vfs resources under the default site 
        List<CmsInternalSitemapEntry> vfsEntries = new ArrayList<CmsInternalSitemapEntry>();
        List<CmsResource> vfsResources = cms.readResources("/sites/default/cntpages/", CmsResourceFilter.ALL);
        for (CmsResource res : vfsResources) {
            CmsInternalSitemapEntry entry = (CmsInternalSitemapEntry)OpenCms.getSitemapManager().getEntryForUri(
                cms,
                res.getRootPath());
            vfsEntries.add(entry);
        }
        testEntries(vfsEntries, parameters);

        // now test some system resources
        List<CmsInternalSitemapEntry> systemEntries = new ArrayList<CmsInternalSitemapEntry>();
        List<CmsResource> systemResources = cms.readResources(
            "/system/modules/org.opencms.ade.sitemap/schemas/",
            CmsResourceFilter.ALL);
        for (CmsResource res : systemResources) {
            CmsInternalSitemapEntry entry = (CmsInternalSitemapEntry)OpenCms.getSitemapManager().getEntryForUri(
                cms,
                res.getRootPath());
            systemEntries.add(entry);
        }
        testEntries(systemEntries, parameters);

        // compare the target state with the current state
        assertTrue(compareFiles(TMP_FILE, tobeExportDataFile));
        deleteTmpFile();
    }
}

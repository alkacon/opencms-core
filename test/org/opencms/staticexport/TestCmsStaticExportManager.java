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

package org.opencms.staticexport;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlEntityResolver;

import java.util.Collections;
import java.util.regex.Pattern;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for static export manager.<p>
 *
 * @since 6.0.0
 */
public class TestCmsStaticExportManager extends OpenCmsTestCase {

    /** simple xml content schema system id, with attachment relation type. */
    private static final String SCHEMA_SYSTEM_ID_14 = "http://www.opencms.org/test14.xsd";

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsStaticExportManager(String arg0) {

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
        suite.setName(TestCmsStaticExportManager.class.getName());

        suite.addTest(new TestCmsStaticExportManager("testExportJspLinkGeneration"));
        suite.addTest(new TestCmsStaticExportManager("testDefaultSuffixLinkGeneration"));
        suite.addTest(new TestCmsStaticExportManager("testSiteExport"));

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
     * Tests the link generation for statically exported files by default suffix.<p>
     *
     * @throws Exception if the test fails
     */
    public void testDefaultSuffixLinkGeneration() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing default suffix statix export link generation");

        String folder = "/folder1/subfolder11/subsubfolder111/";
        String vfsName1 = folder + "image.gif";
        String vfsName2 = folder + "xml.xml";
        // make sure "export" is not set for the folder
        CmsProperty exportProp = cms.readPropertyObject(vfsName1, CmsPropertyDefinition.PROPERTY_EXPORT, true);
        assertTrue(exportProp.isNullProperty());

        // make sure static export on default is disabled
        OpenCms.getStaticExportManager().setDefault(CmsStringUtil.FALSE);
        String rfsPrefix = OpenCms.getStaticExportManager().getRfsPrefix(
            cms.getRequestContext().getSiteRoot() + folder);
        String expected1, expected2;

        cms.getRequestContext().setCurrentProject(cms.readProject("Online"));

        echo("Testing default export based on file suffix");

        assertTrue(OpenCms.getStaticExportManager().isSuffixExportable(vfsName1));
        expected1 = rfsPrefix + cms.getRequestContext().getSiteRoot() + vfsName1;
        checkLinkWithoutParameters(cms, vfsName1, expected1);
        checkLinkWithParameters(cms, vfsName1, expected1);
        assertEquals(expected1, OpenCms.getLinkManager().substituteLink(cms, vfsName1));

        assertFalse(OpenCms.getStaticExportManager().isSuffixExportable(vfsName2));
        expected1 = rfsPrefix + cms.getRequestContext().getSiteRoot() + vfsName2;
        checkLinkWithoutParameters(cms, vfsName2, expected1);
        checkLinkWithParameters(cms, vfsName2, expected1);
        expected2 = OpenCms.getStaticExportManager().getVfsPrefix() + vfsName2;
        assertEquals(expected2, OpenCms.getLinkManager().substituteLink(cms, vfsName2));

        echo("Testing default export based on file suffix with 'exportname' property set");

        cms.getRequestContext().setCurrentProject(cms.readProject("Offline"));

        // set "exportname" property to JSP
        cms.lockResource(folder);
        cms.writePropertyObject(folder, new CmsProperty(CmsPropertyDefinition.PROPERTY_EXPORTNAME, "testfolder", null));
        cms.unlockResource(folder);
        // publish the changes
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.getRequestContext().setCurrentProject(cms.readProject("Online"));

        assertTrue(OpenCms.getStaticExportManager().isSuffixExportable(vfsName1));
        expected1 = rfsPrefix + "/testfolder/image.gif";
        checkLinkWithoutParameters(cms, vfsName1, expected1);
        checkLinkWithParameters(cms, vfsName1, expected1);
        assertEquals(expected1, OpenCms.getLinkManager().substituteLink(cms, vfsName1));

        assertFalse(OpenCms.getStaticExportManager().isSuffixExportable(vfsName2));
        expected1 = rfsPrefix + "/testfolder/xml.xml";
        checkLinkWithoutParameters(cms, vfsName2, expected1);
        checkLinkWithParameters(cms, vfsName2, expected1);
        expected2 = OpenCms.getStaticExportManager().getVfsPrefix() + vfsName2;
        assertEquals(expected2, OpenCms.getLinkManager().substituteLink(cms, vfsName2));
    }

    /**
     * Tests the link generation for statically exported JSP files.<p>
     *
     * @throws Exception if the test fails
     */
    public void testExportJspLinkGeneration() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing the link generation for exported JSP pages");

        // fist setup a little test scenario with the imported data
        String folder = "/types/";
        String vfsName = folder + "jsp.jsp";
        CmsProperty exportProp = new CmsProperty(CmsPropertyDefinition.PROPERTY_EXPORT, CmsStringUtil.TRUE, null);
        cms.lockResource(folder);
        cms.writePropertyObject(folder, exportProp);
        cms.unlockResource(folder);
        cms.lockResource(vfsName);
        cms.writePropertyObject(vfsName, exportProp);
        cms.unlockResource(vfsName);
        // publish the changes
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        String rfsPrefix = OpenCms.getStaticExportManager().getRfsPrefix(
            cms.getRequestContext().getSiteRoot() + folder);
        String expected;

        echo("Testing basic export name generating functions for a JSP");

        expected = rfsPrefix + cms.getRequestContext().getSiteRoot() + vfsName + ".html";
        checkLinkWithoutParameters(cms, vfsName, expected);
        checkLinkWithParameters(cms, vfsName, expected);

        echo("Testing export name generating functions for a JSP with 'exportname' property set");

        // set "exportname" property to JSP
        cms.lockResource(folder);
        cms.writePropertyObject(folder, new CmsProperty(CmsPropertyDefinition.PROPERTY_EXPORTNAME, "myfolder", null));
        cms.unlockResource(folder);
        // publish the changes
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        expected = rfsPrefix + "/myfolder/jsp.jsp.html";
        checkLinkWithoutParameters(cms, vfsName, expected);
        checkLinkWithParameters(cms, vfsName, expected);

        echo(
            "Testing export name generating functions for a JSP with 'exportname' property AND 'exportsuffix' property set");

        // set "exportsuffix" property to JSP
        cms.lockResource(vfsName);
        cms.writePropertyObject(vfsName, new CmsProperty(CmsPropertyDefinition.PROPERTY_EXPORTSUFFIX, ".txt", null));
        cms.unlockResource(vfsName);
        // publish the changes
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        expected = rfsPrefix + "/myfolder/jsp.jsp.txt";
        checkLinkWithoutParameters(cms, vfsName, expected);
        checkLinkWithParameters(cms, vfsName, expected);

        echo("Testing export name generating functions for a JSP with only 'exportsuffix' property set");

        // remove "exportname" property from JSP
        cms.lockResource(folder);
        cms.writePropertyObject(
            folder,
            new CmsProperty(
                CmsPropertyDefinition.PROPERTY_EXPORTNAME,
                CmsProperty.DELETE_VALUE,
                CmsProperty.DELETE_VALUE));
        cms.unlockResource(folder);
        cms.lockResource(vfsName);
        cms.writePropertyObject(vfsName, new CmsProperty(CmsPropertyDefinition.PROPERTY_EXPORTSUFFIX, ".pdf", null));
        cms.unlockResource(vfsName);
        // publish the changes
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        expected = rfsPrefix + cms.getRequestContext().getSiteRoot() + vfsName + ".pdf";
        checkLinkWithoutParameters(cms, vfsName, expected);
        checkLinkWithParameters(cms, vfsName, expected);
    }

    /**
     * Tests saving XML contents with links from/to various sites.<p>
     * 
     * @throws Exception 
     */
    public void testSiteExport() throws Exception {

        // set the site root to the root site
        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("");

        CmsProperty exportProp = new CmsProperty(CmsPropertyDefinition.PROPERTY_EXPORT, CmsStringUtil.TRUE, null);
        CmsProperty exportNameProp = new CmsProperty(CmsPropertyDefinition.PROPERTY_EXPORTNAME, "/", null);

        // create site folder one
        CmsResource siteFolder = cms.createResource("/sites/site-one/", CmsResourceTypeFolder.getStaticTypeId());
        cms.writePropertyObject(siteFolder.getRootPath(), exportProp);
        cms.writePropertyObject(siteFolder.getRootPath(), exportNameProp);
        cms.unlockResource(siteFolder.getRootPath());

        // create site folder two
        siteFolder = cms.createResource("/sites/site-two/", CmsResourceTypeFolder.getStaticTypeId());
        cms.writePropertyObject(siteFolder.getRootPath(), exportProp);
        cms.writePropertyObject(siteFolder.getRootPath(), exportNameProp);
        cms.unlockResource(siteFolder.getRootPath());

        // unmarshal content definition
        String content = CmsFileUtil.readFile(
            "org/opencms/xml/content/xmlcontent-definition-14.xsd",
            CmsEncoder.ENCODING_UTF_8);
        // store content definition in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(SCHEMA_SYSTEM_ID_14, content.getBytes(CmsEncoder.ENCODING_UTF_8));

        // Create files for site one
        createTestFile("/sites/site-one/testfile-file1.html");
        createTestFile("/sites/site-one/testfile-file2.html");
        createTestFile("/sites/site-one/testfile-file3.html");

        // Create files for site two
        createTestFile("/sites/site-two/testfile-file1.html");
        createTestFile("/sites/site-two/testfile-file2.html");
        createTestFile("/sites/site-two/testfile-file3.html");

        // publish the created site folders
        OpenCms.getPublishManager().publishProject(cms);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.getRequestContext().setSiteRoot("/sites/site-one");
        checkLinkWithoutParameters(cms, "/testfile-file1.html", "/data/export/testfile-file1.html");
        checkLinkWithoutParameters(cms, "/testfile-file2.html", "/data/export/testfile-file2.html");
        checkLinkWithoutParameters(cms, "/testfile-file3.html", "/data/export/testfile-file3.html");
        checkLinkWithParameters(cms, "/testfile-file1.html", "/data/export/testfile-file1.html");
        checkLinkWithParameters(cms, "/testfile-file2.html", "/data/export/testfile-file2.html");
        checkLinkWithParameters(cms, "/testfile-file3.html", "/data/export/testfile-file3.html");

        cms.getRequestContext().setSiteRoot("/sites/site-two");
        checkLinkWithoutParameters(cms, "/testfile-file1.html", "/data/export/testfile-file1.html");
        checkLinkWithoutParameters(cms, "/testfile-file2.html", "/data/export/testfile-file2.html");
        checkLinkWithoutParameters(cms, "/testfile-file3.html", "/data/export/testfile-file3.html");
        checkLinkWithParameters(cms, "/testfile-file1.html", "/data/export/testfile-file1.html");
        checkLinkWithParameters(cms, "/testfile-file2.html", "/data/export/testfile-file2.html");
        checkLinkWithParameters(cms, "/testfile-file3.html", "/data/export/testfile-file3.html");

    }

    /**
     * Checks a link that has no parameters.<p>
     *
     * @param cms the cms context
     * @param vfsName the vfsName of the resource to test the link for
     * @param expected the previous generated link
     */
    private void checkLinkWithoutParameters(CmsObject cms, String vfsName, String expected) {

        // check JSP without parameters
        String rfsName = OpenCms.getStaticExportManager().getRfsName(cms, vfsName);
        System.out.println("RFS name: " + rfsName + " VFS name: " + vfsName);
        assertEquals(expected, rfsName);
        assertEquals(vfsName, OpenCms.getStaticExportManager().getVfsName(cms, rfsName));
    }

    /**
     * Checks a link that has parameters.<p>
     *
     * @param cms the cms context
     * @param vfsName the vfsName of the resource to test the link for
     * @param expected the previous generated link
     */
    private void checkLinkWithParameters(CmsObject cms, String vfsName, String expected) {

        // check JSP WITH parameters
        String rfsName = OpenCms.getStaticExportManager().getRfsName(cms, vfsName, "?a=b&c=d", null);
        System.out.println("RFS name: " + rfsName + " VFS name: " + vfsName);
        String extension = expected.substring(expected.lastIndexOf('.'));
        Pattern pattern = Pattern.compile(
            "^" + CmsStringUtil.escapePattern(expected + "_") + "\\d*" + CmsStringUtil.escapePattern(extension) + "$");
        assertTrue(pattern.matcher(rfsName).matches());
        assertEquals(vfsName, OpenCms.getStaticExportManager().getVfsName(cms, rfsName));
    }

    /**
     * Creates a test file for the testSiteLinks test.<p>
     * 
     * @param rootPath the test file path 
     * @return the new test file 
     * @throws Exception if something goes wrong 
     */
    private CmsResource createTestFile(String rootPath) throws Exception {

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("");
        int xmlcontentType = OpenCms.getResourceManager().getResourceType("xmlcontent").getTypeId();
        byte[] content = CmsFileUtil.readFile("org/opencms/xml/content/xmlcontent-14.xml");
        CmsResource tsl = cms.createResource(rootPath, xmlcontentType, content, Collections.<CmsProperty> emptyList());
        cms.unlockResource(tsl);
        return tsl;
    }
}

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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.staticexport;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsJupiterTestCase;
import org.opencms.test.OpenCmsTestEnvironment;
import org.opencms.xml.page.CmsXmlPage;

import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @since 6.0.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestCmsLinkManager extends OpenCmsJupiterTestCase {

    /** Legacy suite setup groups. */
    private enum SetupGroup {
        DEFAULT,
        ADJUSTED_VFS_PREFIX,
        SINGLE_TREE,
        EMPTY_CONTEXT
    }

    /** Content for a simple test page. */
    private static final String PAGE_01 = "<html><body><a href=\"/system/news/test.html?__locale=de\">test</a></body></html>";

    /** The current VFS prefix as added to internal links according to the configuration in opencms-importexport.xml. */
    private String m_vfsPrefix;

    /** The currently active legacy setup group. */
    private SetupGroup m_activeSetupGroup;

    /**
     * @see org.opencms.test.OpenCmsJupiterTestCase#shouldBootOpenCms()
     */
    @Override
    protected boolean shouldBootOpenCms() {

        return false;
    }

    /**
     * @see org.opencms.test.OpenCmsJupiterTestCase#shouldInitConfiguration()
     */
    @Override
    protected boolean shouldInitConfiguration() {

        return true;
    }

    /**
     * @see org.opencms.test.OpenCmsJupiterTestCase#shouldLogTestStartBeforeEach()
     */
    @Override
    protected boolean shouldLogTestStartBeforeEach() {

        return false;
    }

    /**
     * Sets up the required OpenCms variant for the current legacy test order entry.<p>
     *
     * @throws Exception if setup fails
     */
    @BeforeEach
    public void setUpOpenCmsForTest(TestInfo testInfo) throws Exception {

        prepareCurrentTest(testInfo);
        SetupGroup targetGroup = getSetupGroup();
        if ((m_activeSetupGroup != null) && (m_activeSetupGroup != targetGroup)) {
            removeOpenCms();
            m_activeSetupGroup = null;
        }
        if (m_activeSetupGroup == targetGroup) {
            printTestStart(testInfo);
            return;
        }
        m_vfsPrefix = null;
        switch (targetGroup) {
            case DEFAULT:
                OpenCmsTestEnvironment.setupOpenCms("simpletest", "/");
                break;
            case ADJUSTED_VFS_PREFIX:
                OpenCmsTestEnvironment.setupOpenCms(
                    "simpletest",
                    "/",
                    null,
                    "/../org/opencms/staticexport",
                    getClass().getName(),
                    true);
                break;
            case SINGLE_TREE:
                OpenCmsTestEnvironment.setupOpenCms(
                    "simpletest",
                    "/",
                    null,
                    "localizationConfig",
                    getClass().getName(),
                    true);
                break;
            case EMPTY_CONTEXT:
                OpenCmsTestEnvironment.setupOpenCms(
                    "simpletest",
                    "/",
                    null,
                    null,
                    getClass().getName(),
                    "*",
                    "/data",
                    true);
                break;
            default:
                throw new IllegalStateException("Unexpected setup group: " + targetGroup);
        }
        m_activeSetupGroup = targetGroup;
        printTestStart(testInfo);
    }

    /**
     * Removes the last active OpenCms instance.<p>
     */
    @AfterEach
    public void resetCachedState() {

        m_vfsPrefix = null;
    }

    /**
     * Removes the final legacy setup group instance.<p>
     */
    @org.junit.jupiter.api.AfterAll
    public void tearDownOpenCmsForLastGroup() {

        if (m_activeSetupGroup != null) {
            removeOpenCms();
            m_activeSetupGroup = null;
        }
    }

    /**
     * Returns the legacy setup group for the current test method.<p>
     *
     * @return the setup group
     */
    private SetupGroup getSetupGroup() {

        switch (getName()) {
            case "testToAbsolute":
            case "testLinkSubstitution":
            case "testSymmetricSubstitution":
            case "testCustomLinkHandler":
            case "testRootPathAdjustment":
            case "testAbsolutePathAdjustment":
            case "testLinkSubstitutionPreserveSpecialChars":
                return SetupGroup.DEFAULT;
            case "testToAbsoluteWithAdjustedVfsPrefix":
            case "testLinkSubstitutionWithAdjustedVfsPrefix":
            case "testSymmetricSubstitutionWithAdjustedVfsPrefix":
            case "testCustomLinkHandlerWithAdjustedVfsPrefix":
            case "testRootPathAdjustmentWithAdjustedVfsPrefix":
            case "testAbsolutePathAdjustmentWithAdjustedVfsPrefix":
                return SetupGroup.ADJUSTED_VFS_PREFIX;
            case "testSingleTreeLinkSubstitution":
            case "testSymmetricSubstitutionForSingleTree":
            case "testRootPathAdjustmentForSingleTree":
            case "testAbsolutePathAdjustmentForSingleTree":
                return SetupGroup.SINGLE_TREE;
            case "testRootPathAdjustmentWithEmptyOpenCmsContext":
                return SetupGroup.EMPTY_CONTEXT;
            default:
                throw new IllegalStateException("Unexpected test method: " + getName());
        }
    }

    /**
     * Tests the method getAbsoluteUri.<p>
     */
    @Test
    @Order(1)
    public void testToAbsolute() {

        String test;

        test = CmsLinkManager.getRelativeUri("/dir1/dir2/index.html", "/dir1/dirB/index.html");
        System.out.println(test);
        assertEquals(test, "../dirB/index.html");

        test = CmsLinkManager.getRelativeUri("/exp/en/test/index.html", "/exp/de/test/index.html");
        System.out.println(test);
        assertEquals(test, "../../de/test/index.html");

        test = CmsLinkManager.getAbsoluteUri("../../index.html", "/dir1/dir2/dir3/");
        System.out.println(test);
        assertEquals(test, "/dir1/index.html");

        test = CmsLinkManager.getAbsoluteUri("./../././.././dir2/./../index.html", "/dir1/dir2/dir3/");
        System.out.println(test);
        assertEquals(test, "/dir1/index.html");

        test = CmsLinkManager.getAbsoluteUri("/dirA/index.html", "/dir1/dir2/dir3/");
        System.out.println(test);
        assertEquals(test, "/dirA/index.html");
    }

    /**
     * Tests the link substitution.<p>
     *
     * @throws Exception if test fails
     */
    @Test
    @Order(2)
    public void testLinkSubstitution() throws Exception {

        String test;
        CmsObject cms = getCmsObject();
        echo("Testing link substitution");

        cms.getRequestContext().setCurrentProject(cms.readProject("Online"));
        CmsLinkManager linkManager = OpenCms.getLinkManager();

        test = linkManager.substituteLink(cms, "/folder1/index.html?additionalParam", "/sites/default");
        System.out.println(test);
        assertEquals(getVfsPrefix() + "/folder1/index.html?additionalParam", test);

        test = linkManager.substituteLink(
            cms,
            CmsLinkManager.getAbsoluteUri("/", "/folder1/index.html"),
            "/sites/default");
        System.out.println(test);
        assertEquals(getVfsPrefix() + "/", test);

        test = linkManager.substituteLink(
            cms,
            CmsLinkManager.getAbsoluteUri("./", "/folder1/index.html"),
            "/sites/default");
        System.out.println(test);
        assertEquals(getVfsPrefix() + "/folder1/", test);

        test = CmsLinkManager.getRelativeUri("/index.html", "/index.html");
        System.out.println(test);
        assertEquals("index.html", test);

        test = CmsLinkManager.getRelativeUri("/folder1/index.html", "/folder1/");
        System.out.println(test);
        assertEquals("./", test);

        test = CmsLinkManager.getRelativeUri("/index.html", "/");
        System.out.println(test);
        assertEquals("./", test);

        test = CmsLinkManager.getRelativeUri("/index.html", "./");
        System.out.println(test);
        assertEquals("./", test);

        test = CmsLinkManager.getRelativeUri("/", "/");
        System.out.println(test);
        assertEquals("./", test);
    }

    /**
     * Tests symmetric link / root path substitution.<p>
     *
     * @throws Exception if test fails
     */
    @Test
    @Order(3)
    public void testSymmetricSubstitution() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing symmetric link / root path substitution substitution");

        CmsResource res = cms.readResource("/xmlcontent/article_0001.html");
        CmsLinkManager lm = OpenCms.getLinkManager();

        String link = lm.substituteLinkForRootPath(cms, res.getRootPath());
        String rootPath = lm.getRootPath(cms, link);
        assertEquals(res.getRootPath(), rootPath);

        link = lm.getServerLink(cms, res.getRootPath());
        rootPath = lm.getRootPath(cms, link);
        assertEquals(res.getRootPath(), rootPath);
    }

    /**
     * Tests symmetric link / root path substitution with a custom link handler.<p>
     *
     * @throws Exception if test fails
     */
    @Test
    @Order(4)
    public void testCustomLinkHandler() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing symmetric link / root path substitution with a custom link handler");

        CmsLinkManager lm = OpenCms.getLinkManager();
        I_CmsLinkSubstitutionHandler lh = new CmsTestLinkSubstitutionHandler();
        lm.setLinkSubstitutionHandler(cms, lh);

        cms.createResource("/system/news", CmsResourceTypeFolder.getStaticTypeId());
        String resName = "/system/news/test.html";
        cms.createResource(resName, CmsResourceTypeXmlPage.getStaticTypeId());
        OpenCms.getPublishManager().publishResource(cms, "/system/news");
        OpenCms.getPublishManager().waitWhileRunning();
        CmsResource res = cms.readResource(resName);

        testCustomLinkHandler(cms, res.getRootPath());
        OpenCms.getStaticExportManager().setVfsPrefix("");
        OpenCms.getStaticExportManager().initialize(cms);
        testCustomLinkHandler(cms, res.getRootPath());
        cms.getRequestContext().setUri(resName);
        cms.getRequestContext().setSiteRoot("");
        testCustomLinkHandler(cms, res.getRootPath());
        testCustomLinkHandler(cms, res.getRootPath() + "?__locale=de");

        CmsXmlPage page = new CmsXmlPage(Locale.ENGLISH, CmsEncoder.ENCODING_UTF_8);
        page.addValue("body", Locale.ENGLISH);
        page.setStringValue(cms, "body", Locale.ENGLISH, PAGE_01);

        cms.lockResource(resName);
        CmsFile file = cms.readFile(res);
        file.setContents(page.marshal());
        cms.writeFile(file);
    }

    /**
     * Test how the OpenCms context is removed from URLs when getting root URLs.
     * Intended behavior: if and only if a link starts with "${context}/" the
     * context is removed.
     *
     * Example: with context "/opencms":
     * input link: /opencms/path  output link: /path
     * input link: /opencmswhatever/path output link: /opencmswhatever/path
     *
     * Assumption: OpenCms context never ends with "/".
     *
     * @throws CmsException from getCmsObject()
     */
    @Test
    @Order(5)
    public void testRootPathAdjustment() throws CmsException {

        echo("Testing root path adjustment / context removement");
        String context = OpenCms.getSystemInfo().getOpenCmsContext();

        echo("Using OpenCms context \"" + context + "\"");
        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("/");
        String link1 = "/test";
        CmsLinkManager lm = OpenCms.getLinkManager();
        assertEquals(link1, lm.getRootPath(cms, context + link1));

        String link2 = context.isEmpty() ? "/test" : "test";
        assertEquals(context + link2, lm.getRootPath(cms, context + link2));
    }

    /**
     * Test how the OpenCms context is removed from URLs when getting URLs with http://....
     * Intended behavior: if and only if a link starts with "${server-url}/${context}/" the
     * context is removed.
     *
     * Example: with context "http://localhost:8080/opencms":
     * input link: http://localhost:8080/opencms/path  output link: /path
     * input link: http://localhost:8080/opencmswhatever/path output link: /opencmswhatever/path
     *
     * Assumption: OpenCms context never ends with "/".
     *
     * @throws CmsException from getCmsObject()
     */
    @Test
    @Order(6)
    public void testAbsolutePathAdjustment() throws CmsException {

        echo("Testing root path adjustment / context removement for absolute paths");
        String context = OpenCms.getSystemInfo().getOpenCmsContext();
        echo("Using OpenCms context: " + context);

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("/");
        String link = context + "-test";
        String[] folders = context.split("/");
        String parents = "";
        for (int i = 1; i < (folders.length - 1); i++) {
            parents += "/" + folders[i];
            cms.createResource(parents, new CmsResourceTypeFolder());
        }
        cms.createResource(link, new CmsResourceTypeFolder());
        CmsLinkManager lm = OpenCms.getLinkManager();
        String serverlink = lm.getServerLink(cms, "/");
        serverlink = serverlink.substring(0, serverlink.length() - 1);
        String inputlink = serverlink + link;
        echo(
            "Checking link "
                + inputlink
                + " in context "
                + context
                + " and site \""
                + cms.getRequestContext().getSiteRoot()
                + "\"");
        String outputlink = lm.getRootPath(cms, inputlink);
        echo("Result: " + outputlink);
        assertEquals(link, outputlink);
    }

    /**
     * Tests the link substitution.<p>
     *
     * @throws Exception if test fails
     */
    @Test
    @Order(7)
    public void testLinkSubstitutionPreserveSpecialChars() throws Exception {

        CmsObject cms = getCmsObject();
        CmsLinkManager linkManager = OpenCms.getLinkManager();
        String link = "http://foo.invalid/%26/x?f=%26&g=%26";
        assertEquals(link, linkManager.substituteLinkForUnknownTarget(cms, link));
    }

    /**
     * @see #testToAbsolute()
     */
    @Test
    @Order(8)
    public void testToAbsoluteWithAdjustedVfsPrefix() {

        testToAbsolute();
    }

    /**
     * @throws Exception if tests fail
     * @see #testLinkSubstitution()
     */
    @Test
    @Order(9)
    public void testLinkSubstitutionWithAdjustedVfsPrefix() throws Exception {

        testLinkSubstitution();
    }

    /**
     * @throws Exception if tests fail
     * @see #testSymmetricSubstitution()
     */
    @Test
    @Order(10)
    public void testSymmetricSubstitutionWithAdjustedVfsPrefix() throws Exception {

        testSymmetricSubstitution();
    }

    /**
     * @throws Exception if tests fail
     * @see #testCustomLinkHandler()
     */
    @Test
    @Order(11)
    public void testCustomLinkHandlerWithAdjustedVfsPrefix() throws Exception {

        testCustomLinkHandler();
    }

    /**
     * @throws CmsException from getCmsObject()
     * @see #testRootPathAdjustment()
     */
    @Test
    @Order(12)
    public void testRootPathAdjustmentWithAdjustedVfsPrefix() throws CmsException {

        testRootPathAdjustment();
    }

    /**
     * @throws CmsException  from getCmsObject()
     * @see #testAbsolutePathAdjustment()
     */
    @Test
    @Order(13)
    public void testAbsolutePathAdjustmentWithAdjustedVfsPrefix() throws CmsException {

        testAbsolutePathAdjustment();
    }

    /**
     * Tests the link substitution.<p>
     *
     * @throws Exception if test fails
     */
    @Test
    @Order(14)
    public void testSingleTreeLinkSubstitution() throws Exception {

        String test;
        CmsObject cms = getCmsObject();
        echo("Testing link substitution");
        String localeInsert = "/" + cms.getRequestContext().getLocale().toString();
        cms.getRequestContext().setCurrentProject(cms.readProject("Online"));
        CmsLinkManager linkManager = OpenCms.getLinkManager();

        test = linkManager.substituteLink(cms, "/folder1/index.html?additionalParam", "/sites/default");
        System.out.println(test);
        assertEquals(getVfsPrefix() + localeInsert + "/folder1/index.html?additionalParam", test);

        test = linkManager.substituteLink(
            cms,
            CmsLinkManager.getAbsoluteUri("/", "/folder1/index.html"),
            "/sites/default");
        System.out.println(test);
        assertEquals(getVfsPrefix() + localeInsert + "/", test);

        test = linkManager.substituteLink(
            cms,
            CmsLinkManager.getAbsoluteUri("./", "/folder1/index.html"),
            "/sites/default");
        System.out.println(test);
        assertEquals(getVfsPrefix() + localeInsert + "/folder1/", test);

        test = CmsLinkManager.getRelativeUri("/index.html", "/index.html");
        System.out.println(test);
        assertEquals("index.html", test);

        test = CmsLinkManager.getRelativeUri("/folder1/index.html", "/folder1/");
        System.out.println(test);
        assertEquals("./", test);

        test = CmsLinkManager.getRelativeUri("/index.html", "/");
        System.out.println(test);
        assertEquals("./", test);

        test = CmsLinkManager.getRelativeUri("/index.html", "./");
        System.out.println(test);
        assertEquals("./", test);

        test = CmsLinkManager.getRelativeUri("/", "/");
        System.out.println(test);
        assertEquals("./", test);
    }

    /**
     * @throws Exception if tests fail
     * @see #testSymmetricSubstitution()
     */
    @Test
    @Order(15)
    public void testSymmetricSubstitutionForSingleTree() throws Exception {

        testSymmetricSubstitution();
    }

    /**
     * @throws CmsException from getCmsObject()
     * @see #testRootPathAdjustment()
     */
    @Test
    @Order(16)
    public void testRootPathAdjustmentForSingleTree() throws CmsException {

        testRootPathAdjustment();
    }

    /**
     * @throws CmsException  from getCmsObject()
     * @see #testAbsolutePathAdjustment()
     */
    @Test
    @Order(17)
    public void testAbsolutePathAdjustmentForSingleTree() throws CmsException {

        testAbsolutePathAdjustment();
    }

    /**
     * Just a copy of the called method - if the same method is called twice,
     * the JUnit Eclipse plugin (or JUnit itself?) behaves strange (if the test failed or succeeded is only mentioned
     * for the last test occurrence.
     *
     * @throws CmsException from getCmsObject()
     */
    @Test
    @Order(18)
    public void testRootPathAdjustmentWithEmptyOpenCmsContext() throws CmsException {

        testRootPathAdjustment();
    }

    /**
     * Initializes m_vfsPrefix lazily, otherwise it does not work.
     *
     * @return the VFS prefix as added to internal links
     */
    protected String getVfsPrefix() {

        if (m_vfsPrefix == null) {
            m_vfsPrefix = OpenCms.getStaticExportManager().getVfsPrefix();
        }
        return m_vfsPrefix;
    }

    /**
     * Internal test method for custom link test.<p>
     *
     * @param cms the current OpenCms context
     * @param path the resource path in the VFS to check the links for
     *
     * @throws Exception in case the test fails
     */
    private void testCustomLinkHandler(CmsObject cms, String path) throws Exception {

        CmsLinkManager lm = OpenCms.getLinkManager();

        String link = lm.substituteLinkForRootPath(cms, path);
        String rootPath = lm.getRootPath(cms, link);
        assertEquals(path, rootPath);

        link = lm.getServerLink(cms, path);
        rootPath = lm.getRootPath(cms, link);
        assertEquals(path, rootPath);
    }
}

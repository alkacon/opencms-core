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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.xml.page.CmsXmlPage;

import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @since 6.0.0
 */
public class TestCmsLinkManager extends OpenCmsTestCase {

    /** Content for a simple test page. */
    private static final String PAGE_01 = "<html><body><a href=\"/system/news/test.html?__locale=de\">test</a></body></html>";

    /** The current VFS prefix as added to internal links according to the configuration in opencms-importexport.xml. */
    private String m_vfsPrefix;

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsLinkManager(String arg0) {

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
        TestSuite suite1 = new TestSuite();
        TestSuite suite2 = new TestSuite();
        TestSuite suite3 = new TestSuite();
        suite1.setName(TestCmsLinkManager.class.getName());

        suite1.addTest(new TestCmsLinkManager("testToAbsolute"));
        suite1.addTest(new TestCmsLinkManager("testLinkSubstitution"));
        suite1.addTest(new TestCmsLinkManager("testSymmetricSubstitution"));
        suite1.addTest(new TestCmsLinkManager("testCustomLinkHandler"));
        suite1.addTest(new TestCmsLinkManager("testRootPathAdjustment"));
        suite1.addTest(new TestCmsLinkManager("testAbsolutePathAdjustment"));

        suite2.addTest(new TestCmsLinkManager("testToAbsoluteWithAdjustedVfsPrefix"));
        suite2.addTest(new TestCmsLinkManager("testLinkSubstitutionWithAdjustedVfsPrefix"));
        suite2.addTest(new TestCmsLinkManager("testSymmetricSubstitutionWithAdjustedVfsPrefix"));
        suite2.addTest(new TestCmsLinkManager("testCustomLinkHandlerWithAdjustedVfsPrefix"));
        suite2.addTest(new TestCmsLinkManager("testRootPathAdjustmentWithAdjustedVfsPrefix"));
        suite2.addTest(new TestCmsLinkManager("testAbsolutePathAdjustmentWithAdjustedVfsPrefix"));

        suite3.addTest(new TestCmsLinkManager("testSingleTreeLinkSubstitution"));
        suite3.addTest(new TestCmsLinkManager("testSymmetricSubstitutionForSingleTree"));
        suite3.addTest(new TestCmsLinkManager("testRootPathAdjustmentForSingleTree"));
        suite3.addTest(new TestCmsLinkManager("testAbsolutePathAdjustmentForSingleTree"));

        TestSetup wrapper1 = new TestSetup(suite1) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }

        };

        // Test where the vfsPrefix is only the webapp-name, i.e., "/data",
        // not webappname/servletname, i.e., "/data/opencms".
        TestSetup wrapper2 = new TestSetup(suite2) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/", "/../org/opencms/staticexport");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }

        };

        TestSetup wrapper3 = new TestSetup(suite3) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/", "localizationConfig");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }

        };

        suite.addTest(wrapper1);
        suite.addTest(wrapper2);
        suite.addTest(wrapper3);
        // Test adjustment when OpenCms context is empty
        suite.addTest(wrapTest("*", "/data", "", "testRootPathAdjustmentWithEmptyOpenCmsContext"));

        return suite;
    }

    /**
     * Runs the given test wrapped in an OpenCms instance where servletName and defaultWebAppName
     * are adjusted to test for different OpenCms contexts.
     *
     * @param servletName the servlet name used for the OpenCms instance
     * @param defaultWebAppName the default webapp name used for the OpenCms instance
     * @param vfsPrefix The vfsPrefix attached to absolute internal links.
     * @param testCase the name of the test case to wrap
     * @return the wrapped test case
     */
    protected static Test wrapTest(
        final String servletName,
        final String defaultWebAppName,
        final String vfsPrefix,
        final String testCase) {

        return new TestSetup(new TestCmsLinkManager(testCase)) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/", null, null, null, servletName, defaultWebAppName, true);
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }

        };
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
     *
     */
    public void testAbsolutePathAdjustment() throws CmsException {

        echo("Testing root path adjustment / context removement for absolute paths");
        String context = OpenCms.getSystemInfo().getOpenCmsContext();
        echo("Using OpenCms context: " + context);

        // put resource to access
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
     * @throws CmsException  from getCmsObject()
     * @see #testAbsolutePathAdjustment()
     */
    public void testAbsolutePathAdjustmentForSingleTree() throws CmsException {

        testAbsolutePathAdjustment();
    }

    /**
     * @throws CmsException  from getCmsObject()
     * @see #testAbsolutePathAdjustment()
     */
    public void testAbsolutePathAdjustmentWithAdjustedVfsPrefix() throws CmsException {

        testAbsolutePathAdjustment();
    }

    /**
     * Tests symmetric link / root path substitution with a custom link handler.<p>
     *
     * @throws Exception if test fails
     */
    public void testCustomLinkHandler() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing symmetric link / root path substitution with a custom link handler");

        CmsLinkManager lm = OpenCms.getLinkManager();
        I_CmsLinkSubstitutionHandler lh = new CmsTestLinkSubstitutionHandler();
        lm.setLinkSubstitutionHandler(cms, lh);

        // create required special "/system/news/" folder with a resource
        cms.createResource("/system/news", CmsResourceTypeFolder.getStaticTypeId());
        String resName = "/system/news/test.html";
        cms.createResource(resName, CmsResourceTypeXmlPage.getStaticTypeId());
        OpenCms.getPublishManager().publishResource(cms, "/system/news");
        OpenCms.getPublishManager().waitWhileRunning();
        CmsResource res = cms.readResource(resName);

        // test with default setup
        testCustomLinkHandler(cms, res.getRootPath());
        // test with "empty" VFS prefix
        OpenCms.getStaticExportManager().setVfsPrefix("");
        OpenCms.getStaticExportManager().initialize(cms);
        testCustomLinkHandler(cms, res.getRootPath());
        // test when current URI is in "/system/" folder and current site root is empty
        cms.getRequestContext().setUri(resName);
        cms.getRequestContext().setSiteRoot("");
        testCustomLinkHandler(cms, res.getRootPath());
        // test with localized information
        testCustomLinkHandler(cms, res.getRootPath() + "?__locale=de");

        // now try with some real content on a page
        CmsXmlPage page = new CmsXmlPage(Locale.ENGLISH, CmsEncoder.ENCODING_UTF_8);
        page.addValue("body", Locale.ENGLISH);
        page.setStringValue(cms, "body", Locale.ENGLISH, PAGE_01);

        cms.lockResource(resName);
        CmsFile file = cms.readFile(res);
        file.setContents(page.marshal());
        cms.writeFile(file);
    }

    /**
     * @throws Exception if tests fail
     * @see #testCustomLinkHandler()
     */
    public void testCustomLinkHandlerWithAdjustedVfsPrefix() throws Exception {

        testCustomLinkHandler();
    }

    /**
     * Tests the link substitution.<p>
     *
     * @throws Exception if test fails
     */
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
     * @throws Exception if tests fail
     * @see #testLinkSubstitution()
     */
    public void testLinkSubstitutionWithAdjustedVfsPrefix() throws Exception {

        testLinkSubstitution();
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
     *
     */
    public void testRootPathAdjustment() throws CmsException {

        echo("Testing root path adjustment / context removement");
        String context = OpenCms.getSystemInfo().getOpenCmsContext();

        echo("Using OpenCms context \"" + context + "\"");
        //Switch to root site
        CmsObject cms = getCmsObject();
        cms.getRequestContext().setSiteRoot("/");
        String link1 = "/test";
        CmsLinkManager lm = OpenCms.getLinkManager();
        assertEquals(link1, lm.getRootPath(cms, context + link1));

        String link2 = context.isEmpty() ? "/test" : "test";
        assertEquals(context + link2, lm.getRootPath(cms, context + link2));
    }

    /**
     * @throws CmsException from getCmsObject()
     * @see #testRootPathAdjustment()
     */
    public void testRootPathAdjustmentForSingleTree() throws CmsException {

        testRootPathAdjustment();
    }

    /**
     * @throws CmsException from getCmsObject()
     * @see #testRootPathAdjustment()
     */
    public void testRootPathAdjustmentWithAdjustedVfsPrefix() throws CmsException {

        testRootPathAdjustment();
    }

    /**
     * Just a copy of the called method - if the same method is called twice,
     * the JUnit Eclipse plugin (or JUnit itself?) behaves strange (if the test failed or succeeded is only mentioned
     * for the last test occurrence.
     *
     * @throws CmsException from getCmsObject()
     */
    public void testRootPathAdjustmentWithEmptyOpenCmsContext() throws CmsException {

        testRootPathAdjustment();
    }

    /**
     * Tests the link substitution.<p>
     *
     * @throws Exception if test fails
     */
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
     * Tests symmetric link / root path substitution.<p>
     *
     * @throws Exception if test fails
     */
    public void testSymmetricSubstitution() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing symmetric link / root path substitution substitution");

        // read the resource to make sure we certainly use an existing root path
        CmsResource res = cms.readResource("/xmlcontent/article_0001.html");
        CmsLinkManager lm = OpenCms.getLinkManager();

        // first try: no server info
        String link = lm.substituteLinkForRootPath(cms, res.getRootPath());
        String rootPath = lm.getRootPath(cms, link);
        assertEquals(res.getRootPath(), rootPath);

        // second try: with server and protocol
        link = lm.getServerLink(cms, res.getRootPath());
        rootPath = lm.getRootPath(cms, link);
        assertEquals(res.getRootPath(), rootPath);
    }

    public void testSymmetricSubstitutionForSingleTree() throws Exception {

        testSymmetricSubstitution();
    }

    /**
     * @throws Exception if tests fail
     * @see #testSymmetricSubstitution()
     */
    public void testSymmetricSubstitutionWithAdjustedVfsPrefix() throws Exception {

        testSymmetricSubstitution();
    }

    /**
     * Tests the method getAbsoluteUri.<p>
     */
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
     * @see #testToAbsolute()
     */
    public void testToAbsoluteWithAdjustedVfsPrefix() {

        testToAbsolute();
    }

    /**
     * Initializes m_vfsPrefix lazily, otherwise it does not work.
     * @return the VFS prefix as added to internal links
     */
    protected String getVfsPrefix() {

        if (null == m_vfsPrefix) {
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

        // first try: no server info
        String link = lm.substituteLinkForRootPath(cms, path);
        String rootPath = lm.getRootPath(cms, link);
        assertEquals(path, rootPath);

        // second try: with server and protocol
        link = lm.getServerLink(cms, path);
        rootPath = lm.getRootPath(cms, link);
        assertEquals(path, rootPath);
    }
}
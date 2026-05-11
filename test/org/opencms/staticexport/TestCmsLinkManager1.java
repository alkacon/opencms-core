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
import org.opencms.test.OpenCmsTestRunner;
import org.opencms.xml.page.CmsXmlPage;

import java.util.Locale;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Tests for TestCmsLinkManager suite 1: setupOpenCms("simpletest", "/").<p>
 *
 * @since 6.0.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestCmsLinkManager1 extends OpenCmsTestRunner {

    private static final String PAGE_01 = "<html><body><a href=\"/system/news/test.html?__locale=de\">test</a></body></html>";

    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

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

        testCustomLinkHandlerHelper(cms, res.getRootPath());
        OpenCms.getStaticExportManager().setVfsPrefix("");
        OpenCms.getStaticExportManager().initialize(cms);
        testCustomLinkHandlerHelper(cms, res.getRootPath());
        cms.getRequestContext().setUri(resName);
        cms.getRequestContext().setSiteRoot("");
        testCustomLinkHandlerHelper(cms, res.getRootPath());
        testCustomLinkHandlerHelper(cms, res.getRootPath() + "?__locale=de");

        CmsXmlPage page = new CmsXmlPage(Locale.ENGLISH, CmsEncoder.ENCODING_UTF_8);
        page.addValue("body", Locale.ENGLISH);
        page.setStringValue(cms, "body", Locale.ENGLISH, PAGE_01);

        cms.lockResource(resName);
        CmsFile file = cms.readFile(res);
        file.setContents(page.marshal());
        cms.writeFile(file);
    }

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

    @Test
    @Order(7)
    public void testLinkSubstitutionPreserveSpecialChars() throws Exception {

        CmsObject cms = getCmsObject();
        CmsLinkManager linkManager = OpenCms.getLinkManager();
        String link = "http://foo.invalid/%26/x?f=%26&g=%26";
        assertEquals(link, linkManager.substituteLinkForUnknownTarget(cms, link));
    }

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

    protected String getVfsPrefix() {

        return OpenCms.getStaticExportManager().getVfsPrefix();
    }

    private void testCustomLinkHandlerHelper(CmsObject cms, String path) throws Exception {

        CmsLinkManager lm = OpenCms.getLinkManager();

        String link = lm.substituteLinkForRootPath(cms, path);
        String rootPath = lm.getRootPath(cms, link);
        assertEquals(path, rootPath);

        link = lm.getServerLink(cms, path);
        rootPath = lm.getRootPath(cms, link);
        assertEquals(path, rootPath);
    }
}

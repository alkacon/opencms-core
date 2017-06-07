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

package org.opencms.xml.page;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.report.CmsShellReport;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.tools.content.CmsElementRename;
import org.opencms.xml.CmsXmlEntityResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the XML page that require a running OpenCms system.<p>
 *
 * @since 6.0.0
 */
public class TestCmsXmlPageInSystem extends OpenCmsTestCase {

    /** Used encoding. */
    private static final String UTF8 = CmsEncoder.ENCODING_UTF_8;

    /** The current VFS prefix as added to internal links according to the configuration in opencms-importexport.xml. */
    private String m_vfsPrefix;

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsXmlPageInSystem(String arg0) {

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
        suite.setName(TestCmsXmlPageInSystem.class.getName());

        suite.addTest(new TestCmsXmlPageInSystem("testLinksWithSpecialChars"));
        suite.addTest(new TestCmsXmlPageInSystem("testLinkParameterIssue"));
        suite.addTest(new TestCmsXmlPageInSystem("testSchemaCachePublishIssue"));
        suite.addTest(new TestCmsXmlPageInSystem("testLinkReplacement"));
        suite.addTest(new TestCmsXmlPageInSystem("testCommentInSource"));
        suite.addTest(new TestCmsXmlPageInSystem("testXmlPageRenameElement"));
        suite.addTest(new TestCmsXmlPageInSystem("testMalformedPage"));
        suite.addTest(new TestCmsXmlPageInSystem("testXmlPageCreate"));
        suite.addTest(new TestCmsXmlPageInSystem("testAnchorLink"));

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
     * Tests the usage of an anchor link.<p>
     *
     * @throws Exception in case something goes wrong
     */
    public void testAnchorLink() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing usage of an anchor link");

        String filename = "xmlpageAnchor.html";
        CmsResource res = cms.createResource(filename, CmsResourceTypeXmlPage.getStaticTypeId());

        // check the relations
        assertTrue(cms.getRelationsForResource(filename, CmsRelationFilter.ALL).isEmpty());
        assertTrue(cms.getRelationsForResource(filename, CmsRelationFilter.SOURCES).isEmpty());
        assertTrue(cms.getRelationsForResource(filename, CmsRelationFilter.TARGETS).isEmpty());

        CmsFile file = cms.readFile(res);
        CmsXmlPage page = CmsXmlPageFactory.unmarshal(cms, file, true);
        assertTrue(page.hasLocale(Locale.ENGLISH));
        try {
            page.addLocale(cms, Locale.ENGLISH);
            fail("where is the default locale!?");
        } catch (Exception e) {
            // should fail
        }
        page.addValue("test", Locale.ENGLISH);
        page.getValue("test", Locale.ENGLISH).setStringValue(cms, "<a href='#test'>test</a>");
        file.setContents(page.marshal());
        cms.writeFile(file);

        // check the relations, anchors should not be considered as a relation
        assertTrue(cms.getRelationsForResource(filename, CmsRelationFilter.ALL).isEmpty());
        assertTrue(cms.getRelationsForResource(filename, CmsRelationFilter.SOURCES).isEmpty());
        assertTrue(cms.getRelationsForResource(filename, CmsRelationFilter.TARGETS).isEmpty());
    }

    /**
     * Tests comments in the page HTML source code.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testCommentInSource() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing XML page comment handling");

        String filename = "/folder1/subfolder11/test2.html";
        List<CmsProperty> properties = new ArrayList<CmsProperty>();
        properties.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, UTF8, null));
        properties.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_LOCALE, Locale.ENGLISH.toString(), null));
        String content = CmsXmlPageFactory.createDocument(Locale.ENGLISH, UTF8);
        cms.createResource(filename, CmsResourceTypeXmlPage.getStaticTypeId(), content.getBytes(UTF8), properties);

        CmsFile file = cms.readFile(filename);
        CmsXmlPage page = CmsXmlPageFactory.unmarshal(cms, file);
        String element = "test";
        page.addValue(element, Locale.ENGLISH);

        String result;

        // first test using a simple comment
        content = "<h1>Comment Test 1</h1>\n<!-- This is a comment -->\nSome text here...";
        page.setStringValue(cms, element, Locale.ENGLISH, content);
        result = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals(content, result);

        // more complex comment test
        content = "<!-- First comment --><h1>Comment Test 2</h1>\n<!-- This is a comment -->\nSome text here...<!-- Another comment -->";
        page.setStringValue(cms, element, Locale.ENGLISH, content);
        result = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals(content, result);

        // mix of comment and links
        content = "<!-- First comment --><img src=\""
            + getVfsPrefix()
            + "/image.gif\" alt=\"an image\" />\n<!-- This is a comment -->\n<a href=\""
            + getVfsPrefix()
            + "/index.html\">Link</a><!-- Another comment -->";
        page.setStringValue(cms, element, Locale.ENGLISH, content);
        result = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals(content, result);

        // commented out html tags
        content = "<!-- <img src=\""
            + getVfsPrefix()
            + "/image.gif\" alt=\"an image\" />\n<h1>some text</h1>--><!-- This is a comment -->\n<a href=\""
            + getVfsPrefix()
            + "/index.html\">Link</a><!-- Another comment -->";
        page.setStringValue(cms, element, Locale.ENGLISH, content);
        result = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals(content, result);

        // nested comments
        content = "<!-- Start of comment <!-- img src=\""
            + getVfsPrefix()
            + "/image.gif\" alt=\"an image\" / -->\n<h1>some text</h1><!-- This is a comment -->\n End of comment! --> <a href=\""
            + getVfsPrefix()
            + "/index.html\">Link</a><!-- Another comment -->";
        page.setStringValue(cms, element, Locale.ENGLISH, content);
        result = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals(content, result);
    }

    /**
     * Tests link issue with certain parameters.<p>
     *
     * Description of the issue:
     * links with parameters <code>&lt;a href="form.jsp?a=b&language=xy"&gt;</code> are replaced by
     * <code>&lt;a href="form.jsp?a=b?uage=xy"&gt;</code>.<p>
     *
     * This issue turned out to be a bug in the HtmlParser component,
     * updating the component from version 1.4 to version 1.5 solved the issue.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testLinkParameterIssue() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing XML page link parameter issue");

        String filename = "/folder1/subfolder11/test_param_1.html";
        String content = CmsXmlPageFactory.createDocument(Locale.ENGLISH, UTF8);
        List<CmsProperty> properties = new ArrayList<CmsProperty>();
        properties.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, UTF8, null));
        properties.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_LOCALE, Locale.ENGLISH.toString(), null));
        properties.add(new CmsProperty(CmsXmlPage.PROPERTY_ALLOW_RELATIVE, CmsStringUtil.FALSE, null));
        cms.createResource(filename, CmsResourceTypeXmlPage.getStaticTypeId(), content.getBytes(UTF8), properties);

        CmsFile file = cms.readFile(filename);
        CmsXmlPage page = CmsXmlPageFactory.unmarshal(cms, file);
        String element = "test";
        page.addValue(element, Locale.ENGLISH);
        String text;

        page.setStringValue(cms, element, Locale.ENGLISH, "<a href=\"index.html?a=b&someparam=de\">link</a>");
        text = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals(
            "<a href=\"" + getVfsPrefix() + "/folder1/subfolder11/index.html?a=b&amp;someparam=de\">link</a>",
            text);

        page.setStringValue(cms, element, Locale.ENGLISH, "<a href=\"index.html?language=de\">link</a>");
        text = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals("<a href=\"" + getVfsPrefix() + "/folder1/subfolder11/index.html?language=de\">link</a>", text);

        page.setStringValue(cms, element, Locale.ENGLISH, "<a href=\"index.html?a=b&language=de\">link</a>");
        text = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals(
            "<a href=\"" + getVfsPrefix() + "/folder1/subfolder11/index.html?a=b&amp;language=de\">link</a>",
            text);

        page.setStringValue(cms, element, Locale.ENGLISH, "<a href=\"index_noexist.html?a=b&language=de\">link</a>");
        text = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals(
            "<a href=\"" + getVfsPrefix() + "/folder1/subfolder11/index_noexist.html?a=b&amp;language=de\">link</a>",
            text);

        page.setStringValue(
            cms,
            element,
            Locale.ENGLISH,
            "<a href=\"index_noexist.html?a=b&product=somthing\">link</a>");
        text = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals(
            "<a href=\""
                + getVfsPrefix()
                + "/folder1/subfolder11/index_noexist.html?a=b&amp;product=somthing\">link</a>",
            text);
    }

    /**
     * Tests XML link replacement.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testLinkReplacement() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing XML page link replacement");

        String filename = "/folder1/subfolder11/test1.html";
        String content = CmsXmlPageFactory.createDocument(Locale.ENGLISH, UTF8);
        List<CmsProperty> properties = new ArrayList<CmsProperty>();
        properties.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, UTF8, null));
        properties.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_LOCALE, Locale.ENGLISH.toString(), null));
        properties.add(new CmsProperty(CmsXmlPage.PROPERTY_ALLOW_RELATIVE, CmsStringUtil.FALSE, null));
        cms.createResource(filename, CmsResourceTypeXmlPage.getStaticTypeId(), content.getBytes(UTF8), properties);

        CmsFile file = cms.readFile(filename);
        CmsXmlPage page = CmsXmlPageFactory.unmarshal(cms, file);
        String element = "test";
        page.addValue(element, Locale.ENGLISH);
        String text;

        // test link replacement with existing file
        page.setStringValue(cms, element, Locale.ENGLISH, "<a href=\"index.html\">link</a>");
        text = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals("<a href=\"" + getVfsPrefix() + "/folder1/subfolder11/index.html\">link</a>", text);
        file.setContents(page.marshal());

        // move the file
        String source = "/folder1/subfolder11/index.html";
        String destination = "/folder1/subfolder11/index_new.html";
        CmsResource movedRes = cms.readResource(source);
        cms.lockResource(source);
        cms.moveResource(source, destination);

        // test link replacement with moved file
        page = CmsXmlPageFactory.unmarshal(cms, file);
        text = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals("<a href=\"" + getVfsPrefix() + "/folder1/subfolder11/index_new.html\">link</a>", text);
        assertEquals(
            movedRes.getStructureId(),
            page.getLinkTable(element, Locale.ENGLISH).getLink("link0").getStructureId());
        file.setContents(page.marshal());

        // delete and recreate the file
        cms.deleteResource(destination, CmsResource.DELETE_PRESERVE_SIBLINGS);
        cms.unlockResource(destination);
        OpenCms.getPublishManager().publishResource(cms, destination);
        OpenCms.getPublishManager().waitWhileRunning();
        CmsResource newRes = cms.createResource(
            destination,
            CmsResourceTypeXmlPage.getStaticTypeId(),
            content.getBytes(UTF8),
            properties);
        assertNotSame(movedRes.getStructureId(), newRes.getStructureId());

        // test link replacement with recreated resource
        page = CmsXmlPageFactory.unmarshal(cms, file);
        text = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals("<a href=\"" + getVfsPrefix() + "/folder1/subfolder11/index_new.html\">link</a>", text);
        assertEquals(
            newRes.getStructureId(),
            page.getLinkTable(element, Locale.ENGLISH).getLink("link0").getStructureId());
        file.setContents(page.marshal());

        // test link replacement with non-existing file
        page.setStringValue(cms, element, Locale.ENGLISH, "<a href=\"index_noexist.html\">link</a>");
        text = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals("<a href=\"" + getVfsPrefix() + "/folder1/subfolder11/index_noexist.html\">link</a>", text);
    }

    /**
     * Testing pages with international chars in link parameters.<p>
     *
     * @throws Exception if something goes wrong
     */
    public void testLinksWithSpecialChars() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing pages with international chars in link parameters:\n");

        String filename = "xmlpageint.html";

        String content = CmsXmlPageFactory.createDocument(Locale.ENGLISH, UTF8);
        List<CmsProperty> properties = new ArrayList<CmsProperty>();
        properties.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, UTF8, null));
        properties.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_LOCALE, Locale.ENGLISH.toString(), null));
        properties.add(new CmsProperty(CmsXmlPage.PROPERTY_ALLOW_RELATIVE, CmsStringUtil.FALSE, null));
        CmsResource res = cms.createResource(
            filename,
            CmsResourceTypeXmlPage.getStaticTypeId(),
            content.getBytes(UTF8),
            properties);
        CmsFile file = cms.readFile(res);
        CmsXmlPage page = CmsXmlPageFactory.unmarshal(cms, file, true);
        assertTrue(page.hasLocale(Locale.ENGLISH));

        String element = "test";
        page.addValue(element, Locale.ENGLISH);
        String text;

        page.setStringValue(
            cms,
            element,
            Locale.ENGLISH,
            "<a href=\"index.html?bad=\u00E4\u00F6\u00FC\u00C4\u00D6\u00DC\u00DF&good=aouAOUS\">link</a>");
        text = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals(
            "<a href=\""
                + getVfsPrefix()
                + "/index.html?bad=\u00E4\u00F6\u00FC\u00C4\u00D6\u00DC\u00DF&amp;good=aouAOUS\">link</a>",
            text);

        file.setContents(page.marshal());
        cms.writeFile(file);

        file = cms.readFile(file);
        page = CmsXmlPageFactory.unmarshal(cms, file, true);

        text = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals(
            "<a href=\""
                + getVfsPrefix()
                + "/index.html?bad=\u00E4\u00F6\u00FC\u00C4\u00D6\u00DC\u00DF&amp;good=aouAOUS\">link</a>",
            text);
    }

    /**
     * Test malformed page structures.<p>
     *
     * @throws Exception in case something goes wrong
     */
    public void testMalformedPage() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing malformed page element structures");

        // overwrite an existing page with a bad content
        String resourcename = "/folder1/page2.html";
        cms.lockResource(resourcename);

        CmsFile file = cms.readFile(resourcename);

        // read malformed XML page
        String pageStr = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-5.xml", "ISO-8859-1");
        file.setContents(pageStr.getBytes("ISO-8859-1"));

        cms.writeFile(file);
    }

    /**
     * Test the schema cache publish issue.<p>
     *
     * Description of the issue:
     * After the initial publish, the XML page schema does not work anymore.<p>
     *
     * @throws Exception in case something goes wrong
     */
    public void testSchemaCachePublishIssue() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing the validation for values in the XML content");

        String resourcename = "/folder1/page1.html";
        cms.lockResource(resourcename);

        CmsFile file = cms.readFile(resourcename);
        CmsXmlPage page = CmsXmlPageFactory.unmarshal(cms, file);
        page.validateXmlStructure(new CmsXmlEntityResolver(cms));
        page.setStringValue(cms, "body", Locale.ENGLISH, "This is a test");
        assertEquals("This is a test", page.getValue("body", Locale.ENGLISH).getStringValue(cms));
        file.setContents(page.marshal());
        cms.writeFile(file);

        cms.unlockResource(resourcename);
        OpenCms.getPublishManager().publishResource(cms, resourcename);
        OpenCms.getPublishManager().waitWhileRunning();

        cms.lockResource(resourcename);

        file = cms.readFile(resourcename);
        page = CmsXmlPageFactory.unmarshal(cms, file);
        page.validateXmlStructure(new CmsXmlEntityResolver(cms));
        page.setStringValue(cms, "body", Locale.ENGLISH, "This is a another test");
        assertEquals("This is a another test", page.getValue("body", Locale.ENGLISH).getStringValue(cms));
        file.setContents(page.marshal());
        cms.writeFile(file);
    }

    /**
     * Tests creating a XMl page with the API.<p>
     *
     * @throws Exception in case something goes wrong
     */
    public void testXmlPageCreate() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing creation of an XML page:\n");

        String filename = "xmlpage.html";
        CmsResource res = cms.createResource(filename, CmsResourceTypeXmlPage.getStaticTypeId());
        CmsFile file = cms.readFile(res);
        CmsXmlPage page = CmsXmlPageFactory.unmarshal(cms, file, true);
        assertTrue(page.hasLocale(Locale.ENGLISH));
        try {
            page.addLocale(cms, Locale.ENGLISH);
            fail("where is the default locale!?");
        } catch (Exception e) {
            // should fail
        }
        page.addValue("test", Locale.ENGLISH);
        file.setContents(page.marshal());
        cms.writeFile(file);
    }

    /**
     * Tests accessing element names in the XML page.<p>
     *
     * @throws Exception in case something goes wrong
     */
    public void testXmlPageRenameElement() throws Exception {

        String folder = "/folder1/";
        String recursive = CmsStringUtil.TRUE;
        String template = "ALL";
        String locale = "ALL";
        String oldElement = "body";
        String newElement = "NewElement";
        String removeEmptyElements = CmsStringUtil.FALSE;
        String validateNewElement = CmsStringUtil.FALSE;

        echo("Testing XML page rename element handling");
        CmsElementRename wp = new CmsElementRename(
            null,
            getCmsObject(),
            folder,
            recursive,
            template,
            locale,
            oldElement,
            newElement,
            removeEmptyElements,
            validateNewElement);

        echo("Testing initialize CmsElementRename class");
        assertEquals(folder, wp.getParamResource());
        assertEquals(recursive, wp.getParamRecursive());
        assertEquals(template, wp.getParamTemplate());
        assertEquals(locale, wp.getParamLocale());
        assertEquals(oldElement, wp.getParamOldElement());
        assertEquals(newElement, wp.getParamNewElement());
        assertEquals(removeEmptyElements, wp.getParamRemoveEmptyElements());
        assertEquals(validateNewElement, wp.getParamValidateNewElement());
        echo("CmsElementRename class initialized successfully");
        echo("Xml Page Element Rename Start");
        wp.actionRename(new CmsShellReport(Locale.ENGLISH));
        echo("Xml Page Element Rename End");
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

}

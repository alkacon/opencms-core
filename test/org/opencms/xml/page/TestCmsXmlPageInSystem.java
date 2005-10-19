/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/xml/page/TestCmsXmlPageInSystem.java,v $
 * Date   : $Date: 2005/10/19 09:24:33 $
 * Version: $Revision: 1.21.2.1 $
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

package org.opencms.xml.page;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsEncoder;
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
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.21.2.1 $
 * 
 * @since 6.0.0
 */
public class TestCmsXmlPageInSystem extends OpenCmsTestCase {

    private static final String UTF8 = CmsEncoder.ENCODING_UTF_8;

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

        suite.addTest(new TestCmsXmlPageInSystem("testLinkParameterIssue"));
        suite.addTest(new TestCmsXmlPageInSystem("testSchemaCachePublishIssue"));
        suite.addTest(new TestCmsXmlPageInSystem("testLinkReplacement"));
        suite.addTest(new TestCmsXmlPageInSystem("testCommentInSource"));
        suite.addTest(new TestCmsXmlPageInSystem("testXmlPageRenameElement"));
        suite.addTest(new TestCmsXmlPageInSystem("testMalformedPage"));

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
     * Tests comments in the page HTML source code.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testCommentInSource() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing XML page comment handling");

        String filename = "/folder1/subfolder11/test2.html";
        List properties = new ArrayList();
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
        content = "<!-- First comment --><img src=\"/data/opencms/image.gif\" alt=\"an image\" />\n<!-- This is a comment -->\n<a href=\"/data/opencms/index.html\">Link</a><!-- Another comment -->";
        page.setStringValue(cms, element, Locale.ENGLISH, content);
        result = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals(content, result);

        // commented out html tags
        content = "<!-- <img src=\"/data/opencms/image.gif\" alt=\"an image\" />\n<h1>some text</h1>--><!-- This is a comment -->\n<a href=\"/data/opencms/index.html\">Link</a><!-- Another comment -->";
        page.setStringValue(cms, element, Locale.ENGLISH, content);
        result = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals(content, result);

        // nested comments
        content = "<!-- Start of comment <!-- img src=\"/data/opencms/image.gif\" alt=\"an image\" / -->\n<h1>some text</h1><!-- This is a comment -->\n End of comment! --> <a href=\"/data/opencms/index.html\">Link</a><!-- Another comment -->";
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
        List properties = new ArrayList();
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
        assertEquals("<a href=\"/data/opencms/folder1/subfolder11/index.html?a=b&amp;someparam=de\">link</a>", text);

        page.setStringValue(cms, element, Locale.ENGLISH, "<a href=\"index.html?language=de\">link</a>");
        text = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals("<a href=\"/data/opencms/folder1/subfolder11/index.html?language=de\">link</a>", text);

        page.setStringValue(cms, element, Locale.ENGLISH, "<a href=\"index.html?a=b&language=de\">link</a>");
        text = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals("<a href=\"/data/opencms/folder1/subfolder11/index.html?a=b&amp;language=de\">link</a>", text);

        page.setStringValue(cms, element, Locale.ENGLISH, "<a href=\"index_noexist.html?a=b&language=de\">link</a>");
        text = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals("<a href=\"/data/opencms/folder1/subfolder11/index_noexist.html?a=b&amp;language=de\">link</a>", text);

        page.setStringValue(
            cms,
            element,
            Locale.ENGLISH,
            "<a href=\"index_noexist.html?a=b&product=somthing\">link</a>");
        text = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals(
            "<a href=\"/data/opencms/folder1/subfolder11/index_noexist.html?a=b&amp;product=somthing\">link</a>",
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
        List properties = new ArrayList();
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
        assertEquals("<a href=\"/data/opencms/folder1/subfolder11/index.html\">link</a>", text);

        // test link replacement with non-existing file
        page.setStringValue(cms, element, Locale.ENGLISH, "<a href=\"index_noexist.html\">link</a>");
        text = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals("<a href=\"/data/opencms/folder1/subfolder11/index_noexist.html\">link</a>", text);
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
        cms.publishResource(resourcename);

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

}

/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/xml/page/TestCmsXmlPage.java,v $
 * Date   : $Date: 2005/07/29 10:13:57 $
 * Version: $Revision: 1.18 $
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

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsLink;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlContentTypeManager;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.types.CmsXmlHtmlValue;

import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

/**
 * Tests for the XML page that doesn't require a running OpenCms system.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.18 $
 * 
 * @since 6.0.0
 */
public class TestCmsXmlPage extends TestCase {

    private static final String XMLPAGE_SCHEMA_SYSTEM_ID = CmsConfigurationManager.DEFAULT_DTD_PREFIX
        + "xmlpage.xsd";

    private static final String UTF8 = CmsEncoder.ENCODING_UTF_8;

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsXmlPage(String arg0) {

        super(arg0);
    }

    /**
     * Tests reading and updating link elements from the XML page.<p> 
     * 
     * @throws Exception in case something goes wrong
     */
    public void testUpdateXmlPageLink() throws Exception {

        // create a XML entity resolver
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);

        CmsXmlPage page;
        CmsLink link;
        String content;

        // validate xmlpage 4
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-4.xml", UTF8);
        page = CmsXmlPageFactory.unmarshal(content, UTF8, resolver);
        link = page.getLinkTable("body", Locale.ENGLISH).getLink("link0");
        // test values provided in input
        assertEquals("/sites/default/test.html", link.getTarget());
        assertEquals(null, link.getAnchor());
        assertEquals(null, link.getQuery());

        // validate xmlpage 3
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-3.xml", UTF8);
        page = CmsXmlPageFactory.unmarshal(content, UTF8, resolver);
        link = page.getLinkTable("body", Locale.ENGLISH).getLink("link0");
        // test values provided in input
        assertEquals("/sites/default/folder1/image2.gif", link.getTarget());
        assertEquals("test", link.getAnchor());
        assertEquals("param=1&param2=2", link.getQuery());

        // update xml page link with components
        link.updateLink("/test/link1/changed2.gif", "foo", "a=b&c=d");
        // page.marshal();
        link = page.getLinkTable("body", Locale.ENGLISH).getLink("link0");
        assertEquals("/test/link1/changed2.gif", link.getTarget());
        assertEquals("foo", link.getAnchor());
        assertEquals("a=b&c=d", link.getQuery());

        // update xml page link with uri
        link.updateLink("/foo/bar/link/test.jpg#bar?c=d&x=y");
        page.marshal();
        link = page.getLinkTable("body", Locale.ENGLISH).getLink("link0");
        assertEquals("/foo/bar/link/test.jpg", link.getTarget());
        assertEquals("bar", link.getAnchor());
        assertEquals("c=d&x=y", link.getQuery());

        // update xml page link with components, query null
        link.updateLink("/test/link1/changed3.jpg", "bizz", null);
        page.marshal();
        link = page.getLinkTable("body", Locale.ENGLISH).getLink("link0");
        assertEquals("/test/link1/changed3.jpg", link.getTarget());
        assertEquals("bizz", link.getAnchor());
        assertEquals(null, link.getQuery());

        // update xml page link with components, anchor null
        link.updateLink("/test/link1/changed4.jpg", null, "c=d&x=y");
        page.marshal();
        link = page.getLinkTable("body", Locale.ENGLISH).getLink("link0");
        assertEquals("/test/link1/changed4.jpg", link.getTarget());
        assertEquals(null, link.getAnchor());
        assertEquals("c=d&x=y", link.getQuery());

        // update xml page link with uri without components
        link.updateLink("/foo/bar/baz/test.png");
        page.marshal();
        link = page.getLinkTable("body", Locale.ENGLISH).getLink("link0");
        assertEquals("/foo/bar/baz/test.png", link.getTarget());
        assertEquals(null, link.getAnchor());
        assertEquals(null, link.getQuery());
    }

    /**
     * Test validating a XML page with the XML page schema.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testValidateXmlPageWithSchema() throws Exception {

        CmsXmlContentTypeManager typeManager = OpenCms.getXmlContentTypeManager();
        typeManager.addContentType(CmsXmlHtmlValue.class);

        // create a XML entity resolver
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);
        String content;
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage.xsd", UTF8);

        // store schema in entitiy resolver
        CmsXmlEntityResolver.cacheSystemId(XMLPAGE_SCHEMA_SYSTEM_ID, content.getBytes(UTF8));

        // validate the minimal xmlpage
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-minimal.xml", UTF8);
        CmsXmlUtils.validateXmlStructure(content.getBytes(UTF8), resolver);

        // validate the xmlpage 2
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-1.xml", UTF8);
        CmsXmlUtils.validateXmlStructure(content.getBytes(UTF8), resolver);

        // validate the xmlpage 3
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-2.xml", UTF8);
        CmsXmlUtils.validateXmlStructure(content.getBytes(UTF8), resolver);
    }

    /**
     * Tests using a XML page with a XML content definition.<p> 
     * 
     * @throws Exception  in case something goes wrong
     */
    public void testXmlPageAsXmlContentDefinition() throws Exception {

        CmsXmlContentTypeManager typeManager = OpenCms.getXmlContentTypeManager();
        typeManager.addContentType(CmsXmlHtmlValue.class);

        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);

        String content;

        // unmarshal content definition
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage.xsd", UTF8);
        CmsXmlContentDefinition cd1 = CmsXmlContentDefinition.unmarshal(content, XMLPAGE_SCHEMA_SYSTEM_ID, resolver);

        // create new content definition form objects
        CmsXmlContentDefinition cd2 = new CmsXmlContentDefinition("page", XMLPAGE_SCHEMA_SYSTEM_ID);
        cd2.addType(new CmsXmlHtmlValue("element", "0", String.valueOf(Integer.MAX_VALUE)));

        // ensure content definitions are equal
        assertEquals(cd1, cd2);

        // obtain content definition from a XML page
        String pageStr = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-minimal.xml", UTF8);
        CmsXmlPage page = CmsXmlPageFactory.unmarshal(pageStr, UTF8, resolver);
        CmsXmlContentDefinition cd3 = page.getContentDefinition();

        // ensure content definitions are equal
        assertEquals(cd1, cd3);
    }

    /**
     * Tests creating a XMl page (final version) with the API.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testXmlPageCreateMinimal() throws Exception {

        // create a XML entity resolver
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);

        String pageStr = CmsXmlPageFactory.createDocument(Locale.ENGLISH, UTF8);
        System.out.println("Testing creation of a minimal valid XML page:\n");
        System.out.println(pageStr);

        // now compare against stored version of minimal XML page 
        String minimalPageStr = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-minimal.xml", UTF8);
        // remove windows-style linebreaks
        minimalPageStr = CmsStringUtil.substitute(minimalPageStr, "\r\n", "\n");
        assertEquals(pageStr, minimalPageStr);

        // create a new XML page with this content, marshal it and compare
        CmsXmlPage page = CmsXmlPageFactory.unmarshal(pageStr, UTF8, resolver);
        byte[] bytes = page.marshal();
        String newPageStr = new String(bytes, UTF8);
        assertEquals(pageStr, newPageStr);
    }

    /**
     * Tests accessing element names in the XML page.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testXmlPageElementNames() throws Exception {

        // create a XML entity resolver
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);

        System.out.println("Testing element name access in the XML page\n");

        // load stored XML page
        String pageStr = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-2.xml", UTF8);

        // create a new XML page with this content
        CmsXmlPage page = CmsXmlPageFactory.unmarshal(pageStr, UTF8, resolver);

        assertTrue(page.hasValue("body", Locale.ENGLISH));
        assertTrue(page.hasValue("body2", Locale.ENGLISH));
        assertTrue(page.hasValue("body", Locale.GERMAN));

        List names;

        names = page.getNames(Locale.ENGLISH);
        assertEquals(2, names.size());
        assertTrue(names.contains("body"));
        assertTrue(names.contains("body2"));

        names = page.getNames(Locale.GERMAN);
        assertEquals(1, names.size());
        assertTrue(names.contains("body"));

        page.addLocale(null, Locale.FRENCH);
        page.addValue("newbody", Locale.FRENCH);
        page.addValue("newbody2", Locale.FRENCH);
        page.addValue("anotherbody", Locale.FRENCH);

        names = page.getNames(Locale.FRENCH);
        assertEquals(3, names.size());
        assertTrue(names.contains("newbody"));
        assertTrue(names.contains("newbody2"));
        assertTrue(names.contains("anotherbody"));

        page.removeValue("body2", Locale.ENGLISH);
        names = page.getNames(Locale.ENGLISH);
        assertEquals(1, names.size());
        assertTrue(names.contains("body"));

        page.removeLocale(Locale.GERMAN);
        names = page.getNames(Locale.GERMAN);
        assertEquals(0, names.size());

        boolean success = false;
        try {
            page.addValue("body[0]", Locale.ENGLISH);
        } catch (CmsIllegalArgumentException e) {
            success = true;
        }
        if (!success) {
            throw new Exception("Multiple element name creation possible");
        }

        success = false;
        try {
            page.addValue("body[1]", Locale.ENGLISH);
        } catch (CmsIllegalArgumentException e) {
            success = true;
        }
        if (!success) {
            throw new Exception("Page element name creation with index [1] possible");
        }
    }

    /**
     * Tests acessing XML page values via locales.<p> 
     *
     * @throws Exception in case something goes wrong
     */
    public void testXmlPageLocaleAccess() throws Exception {

        // create a XML entity resolver
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);

        CmsXmlPage page;
        String content;

        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-2.xml", UTF8);
        page = CmsXmlPageFactory.unmarshal(content, UTF8, resolver);

        List locales;

        locales = page.getLocales("body");
        assertEquals(2, locales.size());
        assertTrue(locales.contains(Locale.ENGLISH));
        assertTrue(locales.contains(Locale.GERMAN));

        locales = page.getLocales("body2");
        assertEquals(1, locales.size());
        assertTrue(locales.contains(Locale.ENGLISH));
    }

    
    /**
     * Tests copying, moving and removing locales from a XML page.<p> 
     *
     * @throws Exception in case something goes wrong
     */
    public void testXmlPageLocaleCopyMoveRemove() throws Exception {

        // create a XML entity resolver
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);

        CmsXmlPage page;
        String content;

        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-2.xml", UTF8);
        page = CmsXmlPageFactory.unmarshal(content, UTF8, resolver);

        
        assertEquals(2, page.getLocales().size());
        assertTrue(page.hasLocale(Locale.ENGLISH));
        assertTrue(page.hasLocale(Locale.GERMAN));

        page.copyLocale(Locale.GERMAN, Locale.FRENCH);
        assertEquals(3, page.getLocales().size());
        assertTrue(page.hasLocale(Locale.ENGLISH));
        assertTrue(page.hasLocale(Locale.GERMAN));
        assertTrue(page.hasLocale(Locale.FRENCH));

        page.moveLocale(Locale.FRENCH, Locale.ITALIAN);
        assertEquals(3, page.getLocales().size());
        assertTrue(page.hasLocale(Locale.ENGLISH));
        assertTrue(page.hasLocale(Locale.GERMAN));
        assertTrue(page.hasLocale(Locale.ITALIAN));
        
        page.removeLocale(Locale.ITALIAN);
        assertEquals(2, page.getLocales().size());
        assertTrue(page.hasLocale(Locale.ENGLISH));
        assertTrue(page.hasLocale(Locale.GERMAN));
        
        page.removeLocale(Locale.ENGLISH);
        assertEquals(1, page.getLocales().size());
        assertTrue(page.hasLocale(Locale.GERMAN));
    }
    
    /**
     * Tests reading elements from the updated, final version of the XML page.<p> 
     * 
     * @throws Exception  in case something goes wrong
     */
    public void testXmlPageReadFinalVersion() throws Exception {

        // create a XML entity resolver
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);

        CmsXmlPage page;
        String content;

        // validate "final" xmlpage 1
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-1.xml", UTF8);
        page = CmsXmlPageFactory.unmarshal(content, UTF8, resolver);
        assertTrue(page.hasValue("body", Locale.ENGLISH));
        CmsLinkTable table = page.getLinkTable("body", Locale.ENGLISH);
        assertTrue(table.getLink("link0").isInternal());
        assertEquals("English! Image <img src=\"/sites/default/folder1/image2.gif\" />", page.getStringValue(
            null,
            "body",
            Locale.ENGLISH));

        // validate "final" xmlpage 2
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-2.xml", UTF8);
        page = CmsXmlPageFactory.unmarshal(content, UTF8, resolver);
        assertTrue(page.hasValue("body", Locale.ENGLISH));
        assertTrue(page.hasValue("body", Locale.GERMAN));
        assertTrue(page.hasValue("body2", Locale.ENGLISH));
        assertEquals("English! Image <img src=\"/sites/default/folder1/image2.gif\" />", page.getStringValue(
            null,
            "body",
            Locale.ENGLISH));
        assertEquals("English 2!", page.getStringValue(null, "body2", Locale.ENGLISH));
        assertEquals("Deutsch! Image <img src=\"/sites/default/folder1/image2.gif\" />", page.getStringValue(
            null,
            "body",
            Locale.GERMAN));
    }

    /**
     * Tests reading elements from the "old", pre 5.5.0 version of the XML page.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testXmlPageReadOldVersion() throws Exception {

        // create a XML entity resolver
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);

        CmsXmlPage page;
        String content;

        // validate "old" xmlpage 1
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-old-1.xml", UTF8);
        page = CmsXmlPageFactory.unmarshal(content, UTF8, resolver);
        assertTrue(page.hasValue("body", Locale.ENGLISH));
        CmsLinkTable table = page.getLinkTable("body", Locale.ENGLISH);
        assertTrue(table.getLink("link0").isInternal());
        assertEquals("English! Image <img src=\"/sites/default/folder1/image2.gif\" />", page.getStringValue(
            null,
            "body",
            Locale.ENGLISH));

        // validate "old" xmlpage 2
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-old-2.xml", UTF8);
        page = CmsXmlPageFactory.unmarshal(content, UTF8, resolver);
        assertTrue(page.hasValue("body", Locale.ENGLISH));
        assertTrue(page.hasValue("body", Locale.GERMAN));
        assertTrue(page.hasValue("body2", Locale.ENGLISH));
        assertEquals("English! Image <img src=\"/sites/default/folder1/image2.gif\" />", page.getStringValue(
            null,
            "body",
            Locale.ENGLISH));
        assertEquals("English 2!", page.getStringValue(null, "body2", Locale.ENGLISH));
        assertEquals("Deutsch! Image <img src=\"/sites/default/folder1/image2.gif\" />", page.getStringValue(
            null,
            "body",
            Locale.GERMAN));
    }

    /**
     * Tests accessing element names in the XML page.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testXmlPageRenameElement() throws Exception {

        // create a XML entity resolver for test case
        CmsXmlContentTypeManager.createTypeManagerForTestCases();
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);

        System.out.println("Testing renaming element in the XML page\n");

        // load stored XML page
        String pageStr = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-2.xml", UTF8);

        // create a new XML page with this content
        CmsXmlPage page = CmsXmlPageFactory.unmarshal(pageStr, UTF8, resolver);

        page.renameValue("body2", "bodyNEW", Locale.ENGLISH);
        page.validateXmlStructure(resolver);
        page.marshal();
        // check if page has the element 'body2NEW'
        assertTrue(page.hasValue("bodyNEW", Locale.ENGLISH));
        // check if page has the element 'body2'
        assertFalse(page.hasValue("body2", Locale.ENGLISH));
        System.out.println(page.toString());
    }

    /**
     * Tests writing elements to the updated, final version of the XML page.<p> 
     *
     * @throws Exception in case something goes wrong
     */
    public void testXmlPageWriteFinalVersion() throws Exception {

        // create a XML entity resolver
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);

        CmsXmlPage page;
        String content;

        // validate "final" xmlpage 1
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-1.xml", UTF8);
        page = CmsXmlPageFactory.unmarshal(content, UTF8, resolver);
        page.addValue("body3", Locale.ENGLISH);
        page.setStringValue(null, "body3", Locale.ENGLISH, "English WRITTEN! Image <img src=\"/test/image.gif\" />");
        assertTrue(page.hasValue("body3", Locale.ENGLISH));
        CmsLinkTable table = page.getLinkTable("body3", Locale.ENGLISH);
        assertTrue(table.getLink("link0").isInternal());
        assertEquals("English WRITTEN! Image <img src=\"/test/image.gif\" />", page.getStringValue(
            null,
            "body3",
            Locale.ENGLISH));
    }

    /**
     * Tests writing elements to the "old", pre 5.5.0 version of the XML page.<p>
     * 
     * @throws Exception in case something goes wrong
     */
    public void testXmlPageWriteOldVersion() throws Exception {

        // create a XML entity resolver
        CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(null);

        CmsXmlPage page;
        String content;

        // validate "old" xmlpage 1
        content = CmsFileUtil.readFile("org/opencms/xml/page/xmlpage-old-1.xml", UTF8);
        page = CmsXmlPageFactory.unmarshal(content, UTF8, resolver);
        page.addValue("body3", Locale.ENGLISH);
        page.setStringValue(null, "body3", Locale.ENGLISH, "English WRITTEN! Image <img src=\"/test/image.gif\" />");
        assertTrue(page.hasValue("body3", Locale.ENGLISH));
        CmsLinkTable table = page.getLinkTable("body3", Locale.ENGLISH);
        assertTrue(table.getLink("link0").isInternal());
        assertEquals("English WRITTEN! Image <img src=\"/test/image.gif\" />", page.getStringValue(
            null,
            "body3",
            Locale.ENGLISH));
    }
}

/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/util/TestCmsStringUtil.java,v $
 * Date   : $Date: 2005/09/20 15:39:08 $
 * Version: $Revision: 1.10.2.1 $
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

package org.opencms.util;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

/** 
 * Test cases for {@link org.opencms.util.CmsStringUtil}.<p>
 * 
 * @author Andreas Zahner 
 * @author Achim Westermann 
 * 
 * @version $Revision: 1.10.2.1 $
 */
public class TestCmsStringUtil extends TestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsStringUtil(String arg0) {

        super(arg0);
    }

    /**
     * Tests content replacement during import.<p>
     */
    public void testCmsContentReplacement() {

        String content, result, context, search, replace;

        content = "<html><body>\n"
            + "See <a href=\"http://www.opencms.org/opencms/opencms/opencms/index.html\">\n"
            + "http://www.opencms.org/opencms/opencms/opencms/index.html</a>\n"
            + "or <a href=\"/opencms/opencms/opencms/index.html\">\n"
            + "/opencms/opencms/opencms/index.html</a>\n"
            + "<img src=\"/opencms/opencms/system/galleries/pics/test/test.gif\">\n"
            + "<img src=\"http://www.othersite.org/opencms/opencms/system/galleries/pics/test/test.gif\">\n"
            + "Some URL in the Text: http://www.thirdsite.org/opencms/opencms/some/url.html.\n"
            + "Another URL in the Text: /opencms/opencms/some/url.html.\n"
            + "</body></html>\n";

        result = "<html><body>\n"
            + "See <a href=\"http://www.opencms.org/opencms/opencms/opencms/index.html\">\n"
            + "http://www.opencms.org/opencms/opencms/opencms/index.html</a>\n"
            + "or <a href=\""
            + CmsStringUtil.MACRO_OPENCMS_CONTEXT
            + "/opencms/index.html\">\n"
            + CmsStringUtil.MACRO_OPENCMS_CONTEXT
            + "/opencms/index.html</a>\n"
            + "<img src=\""
            + CmsStringUtil.MACRO_OPENCMS_CONTEXT
            + "/system/galleries/pics/test/test.gif\">\n"
            + "<img src=\"http://www.othersite.org/opencms/opencms/system/galleries/pics/test/test.gif\">\n"
            + "Some URL in the Text: http://www.thirdsite.org/opencms/opencms/some/url.html.\n"
            + "Another URL in the Text: "
            + CmsStringUtil.MACRO_OPENCMS_CONTEXT
            + "/some/url.html.\n"
            + "</body></html>\n";

        context = "/opencms/opencms/";

        // search = "([>\"']\\s*)" + context;
        search = "([^\\w/])" + context;
        replace = "$1" + CmsStringUtil.escapePattern(CmsStringUtil.MACRO_OPENCMS_CONTEXT) + "/";

        String test = CmsStringUtil.substitutePerl(content, search, replace, "g");

        System.err.println(this.getClass().getName() + ".testCmsContentReplacement():");
        System.err.println(test);
        assertEquals(test, result);

        test = CmsStringUtil.substituteContextPath(content, context);
        assertEquals(test, result);
    }

    /**
     * Combined tests.<p>
     */
    public void testCombined() {

        String test;
        String content = "<p>A paragraph with text...<img src=\"/opencms/opencms/empty.gif\"></p>\n<a href=\"/opencms/opencms/test.jpg\">";
        String search = "/opencms/opencms/";
        String replace = "${path}";
        test = CmsStringUtil.substitute(content, search, replace);
        assertEquals(
            test,
            "<p>A paragraph with text...<img src=\"${path}empty.gif\"></p>\n<a href=\"${path}test.jpg\">");
        test = CmsStringUtil.substitute(test, replace, search);
        assertEquals(
            test,
            "<p>A paragraph with text...<img src=\"/opencms/opencms/empty.gif\"></p>\n<a href=\"/opencms/opencms/test.jpg\">");
    }

    /**
     * Tests for complext import patterns.<p>
     */
    public void testComplexPatternForImport() {

        String content = "<cms:link>/pics/test.gif</cms:link> <img src=\"/pics/test.gif\"> script = '/pics/test.gif' <cms:link> /pics/othertest.gif </cms:link>\n"
            + "<cms:link>/mymodule/pics/test.gif</cms:link> <img src=\"/mymodule/pics/test.gif\"> script = '/mymodule/pics/test.gif' <cms:link> /mymodule/system/galleries/pics/othertest.gif </cms:link>";
        String search = "([>\"']\\s*)/pics/";
        String replace = "$1/system/galleries/pics/";
        String test = CmsStringUtil.substitutePerl(content, search, replace, "g");
        assertEquals(
            test,
            "<cms:link>/system/galleries/pics/test.gif</cms:link> <img src=\"/system/galleries/pics/test.gif\"> script = '/system/galleries/pics/test.gif' <cms:link> /system/galleries/pics/othertest.gif </cms:link>\n"
                + "<cms:link>/mymodule/pics/test.gif</cms:link> <img src=\"/mymodule/pics/test.gif\"> script = '/mymodule/pics/test.gif' <cms:link> /mymodule/system/galleries/pics/othertest.gif </cms:link>");
    }

    /**
     * Tests for the escape patterns.<p>
     */
    public void testEscapePattern() {

        String test;
        test = CmsStringUtil.escapePattern("/opencms/opencms");
        assertEquals(test, "\\/opencms\\/opencms");
        test = CmsStringUtil.escapePattern("/opencms/$");
        assertEquals(test, "\\/opencms\\/\\$");
    }

    /**
     * Tests the body tag extraction.<p> 
     */
    public void testExtractHtmlBody() {

        String content, result;
        String innerContent = "This is body content in the body\n<h1>A headline</h1>\nSome text in the body\n";

        content = "<html><body>" + innerContent + "</body></html>";
        result = CmsStringUtil.extractHtmlBody(content);
        assertEquals(result, innerContent);

        content = "<html><body style='css' background-color:#ffffff>" + innerContent + "</body></html>";
        result = CmsStringUtil.extractHtmlBody(content);
        assertEquals(result, innerContent);

        content = "<html>\n<title>Test</title>\n<body style='css' background-color:#ffffff>"
            + innerContent
            + "</body>\n</html>";
        result = CmsStringUtil.extractHtmlBody(content);
        assertEquals(result, innerContent);

        content = "<html>< body style='css' background-color:#ffffff>" + innerContent + "</ BODY>";
        result = CmsStringUtil.extractHtmlBody(content);
        assertEquals(result, innerContent);

        content = "<BODY>" + innerContent + "</boDY></html></body><body>somemoretext</BODY>";
        result = CmsStringUtil.extractHtmlBody(content);
        assertEquals(result, innerContent);

        content = innerContent + "</boDY></html>";
        result = CmsStringUtil.extractHtmlBody(content);
        assertEquals(result, innerContent);

        content = "<html><BODY>" + innerContent;
        result = CmsStringUtil.extractHtmlBody(content);
        assertEquals(result, innerContent);

        content = innerContent;
        result = CmsStringUtil.extractHtmlBody(content);
        assertEquals(result, innerContent);
    }

    /**
     * Tests the xml encoding extraction.<p>
     */
    public void testExtractXmlEncoding() {

        String xml, result;

        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<!DOCTYPE opencms SYSTEM"
            + "\"http://www.opencms.org/dtd/6.0/opencms-importexport.dtd\">\n"
            + "<opencms/>";

        result = CmsStringUtil.extractXmlEncoding(xml);
        assertEquals(result, "UTF-8");

        xml = "<?xml version=\"1.0\" encoding='ISO-8859-1'?>\n" + "<opencms/>";

        result = CmsStringUtil.extractXmlEncoding(xml);
        assertEquals(result, "ISO-8859-1");
    }

    /**
     * Further tests.<p> 
     */
    public void testLine() {

        String content = "<edittemplate><![CDATA[<H4><IMG style=\"WIDTH: 77px; HEIGHT: 77px\" alt=\"Homepage animation\" hspace=8 src=\"/opencms/opencms/pics/alkacon/x_hp_ani04.gif\" align=right vspace=8 border=0><IMG style=\"WIDTH: 307px; HEIGHT: 52px\" alt=\"Homepage animation\" hspace=0 src=\"/opencms/opencms/pics/alkacon/x_hp_ani05.gif\" vspace=8 border=0></H4>\n<P>Alkacon Software provides software development services for the digital business. We are specialized in web - based content management solutions build on open source Java Software. </P>\n<P>Alkacon Software is a major contributor to the <A href=\"http://www.opencms.org\" target=_blank>OpenCms Project</A>. OpenCms is an enterprise - ready content management platform build in Java from open source components. OpenCms can easily be deployed on almost any existing IT infrastructure and provides powerful features especially suited for large enterprise internet or intranet applications. </P>\n<P>Alkacon Software offers standard <A href=\"/alkacon/en/services/opencms/index.html\" target=_self>service and support </A>packages for OpenCms, providing an optional layer of security and convenience often required for mission critical OpenCms installations.</P>\n<UL>\n<LI><IMG style=\"WIDTH: 125px; HEIGHT: 34px\" alt=OpenCms hspace=3 src=\"/opencms/opencms/pics/alkacon/logo_opencms_125.gif\" align=right border=0>Learn more about our <A href=\"/alkacon/en/services/index.html\" target=_self>Services</A> \n<LI>Subscribe to our&nbsp;<A href=\"/alkacon/en/company/contact/newsletter.html\" target=_self>Company Newsletter</A> \n<LI>Questions? <A href=\"/alkacon/en/company/contact/index.html\" target=_self>Contact us</A></LI></UL>\n<P>&nbsp;</P>]]></edittemplate>";
        String search = "/pics/";
        String replace = "/system/galleries/pics/";
        String test = CmsStringUtil.substitute(content, search, replace);
        assertEquals(
            test,
            "<edittemplate><![CDATA[<H4><IMG style=\"WIDTH: 77px; HEIGHT: 77px\" alt=\"Homepage animation\" hspace=8 src=\"/opencms/opencms/system/galleries/pics/alkacon/x_hp_ani04.gif\" align=right vspace=8 border=0><IMG style=\"WIDTH: 307px; HEIGHT: 52px\" alt=\"Homepage animation\" hspace=0 src=\"/opencms/opencms/system/galleries/pics/alkacon/x_hp_ani05.gif\" vspace=8 border=0></H4>\n<P>Alkacon Software provides software development services for the digital business. We are specialized in web - based content management solutions build on open source Java Software. </P>\n<P>Alkacon Software is a major contributor to the <A href=\"http://www.opencms.org\" target=_blank>OpenCms Project</A>. OpenCms is an enterprise - ready content management platform build in Java from open source components. OpenCms can easily be deployed on almost any existing IT infrastructure and provides powerful features especially suited for large enterprise internet or intranet applications. </P>\n<P>Alkacon Software offers standard <A href=\"/alkacon/en/services/opencms/index.html\" target=_self>service and support </A>packages for OpenCms, providing an optional layer of security and convenience often required for mission critical OpenCms installations.</P>\n<UL>\n<LI><IMG style=\"WIDTH: 125px; HEIGHT: 34px\" alt=OpenCms hspace=3 src=\"/opencms/opencms/system/galleries/pics/alkacon/logo_opencms_125.gif\" align=right border=0>Learn more about our <A href=\"/alkacon/en/services/index.html\" target=_self>Services</A> \n<LI>Subscribe to our&nbsp;<A href=\"/alkacon/en/company/contact/newsletter.html\" target=_self>Company Newsletter</A> \n<LI>Questions? <A href=\"/alkacon/en/company/contact/index.html\" target=_self>Contact us</A></LI></UL>\n<P>&nbsp;</P>]]></edittemplate>");
    }

    /**
     * Tests <code>{@link CmsStringUtil#splitAsArray(String, char)}</code>.<p>
     */
    public void testSplitAsArrayStringChar() {

        // test usability for path-tokenization (e.g. admin tool of workplace)
        String toSplit = "/system/workplace/admin/searchindex/";
        String[] result = CmsStringUtil.splitAsArray(toSplit, '/');
        assertEquals("The string \""
            + toSplit
            + "\" split with separator '/'  should contain 4 tokens but was split to: \""
            + Arrays.asList(result)
            + "\"!", 4, result.length);

        // test an empty String: 
        toSplit = "";
        result = CmsStringUtil.splitAsArray(toSplit, '/');
        assertEquals("The empty String \"\" should contain zero tokens but was split to: \""
            + Arrays.asList(result)
            + "\"!", 0, result.length);

        // test a whitespace String with truncation:
        toSplit = "               ";
        result = CmsStringUtil.splitAsArray(toSplit, '/');
        assertEquals("The String \""
            + toSplit
            + "\" split with separator '/'  should contain zero tokens but was split to: \""
            + Arrays.asList(result)
            + "\"!", 1, result.length);

        // test a 1 separator-only String
        toSplit = "/";
        result = CmsStringUtil.splitAsArray(toSplit, '/');
        assertEquals("The String \""
            + toSplit
            + "\" split with separator '/'  should contain 2 tokens but was split to: \""
            + result
            + "\"!", 0, result.length);

        // test a 2 separator-only String
        toSplit = "//";
        result = CmsStringUtil.splitAsArray(toSplit, '/');
        assertEquals("The String \""
            + toSplit
            + "\" split with separator '/'  should contain 1 token but was split to: \""
            + Arrays.asList(result)
            + "\"!", 1, result.length);

        // test a single token String with starting delimiter
        toSplit = "/token";
        result = CmsStringUtil.splitAsArray(toSplit, '/');
        assertEquals("The String \""
            + toSplit
            + "\" split with separator '/' should contain 1 token but was split to: \""
            + Arrays.asList(result)
            + "\"!", 1, result.length);

        // test a single token String with ending delimiter
        toSplit = "token/";
        result = CmsStringUtil.splitAsArray(toSplit, '/');
        assertEquals("The String \""
            + toSplit
            + "\" split with separator '/'   should contain 1 token but was split to: \""
            + Arrays.asList(result)
            + "\"!", 1, result.length);

        // test a 3 separator-only String
        toSplit = "///";
        result = CmsStringUtil.splitAsArray(toSplit, '/');
        assertEquals("The String \""
            + toSplit
            + "\" split with separator '/' should contain 2 tokens but was split to: \""
            + Arrays.asList(result)
            + "\"!", 2, result.length);
    }

    /**
     * Tests <code>{@link CmsStringUtil#splitAsArray(String, String)}</code>.<p>
     */
    public void testSplitAsArrayStringString() {

        // test usability for path-tokenization (e.g. admin tool of workplace)
        String toSplit = "/system/workplace/admin/searchindex/";
        String[] result = CmsStringUtil.splitAsArray(toSplit, "/");
        assertEquals("The string \""
            + toSplit
            + "\" split with separator \"/\"  should contain 4 tokens but was split to: \""
            + Arrays.asList(result)
            + "\"!", 4, result.length);

        // test an empty String: 
        toSplit = "";
        result = CmsStringUtil.splitAsArray(toSplit, "/");
        assertEquals("The empty String \"\" should contain zero tokens but was split to: \""
            + Arrays.asList(result)
            + "\"!", 0, result.length);

        // test a whitespace String with truncation:
        toSplit = "               ";
        result = CmsStringUtil.splitAsArray(toSplit, "/");
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \"/\"  should contain zero tokens but was split to: \""
            + Arrays.asList(result)
            + "\"!", 1, result.length);

        // test a 1 separator-only String
        toSplit = "/";
        result = CmsStringUtil.splitAsArray(toSplit, "/");
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \"/\"  should contain 2 tokens but was split to: \""
            + result
            + "\"!", 0, result.length);

        // test a 2 separator-only String
        toSplit = "//";
        result = CmsStringUtil.splitAsArray(toSplit, "/");
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \"/\"  should contain 1 token but was split to: \""
            + Arrays.asList(result)
            + "\"!", 1, result.length);

        // test a single token String with starting delimiter
        toSplit = "/token";
        result = CmsStringUtil.splitAsArray(toSplit, "/");
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \"/\"  should contain 1 token but was split to: \""
            + Arrays.asList(result)
            + "\"!", 1, result.length);

        // test a single token String with ending delimiter
        toSplit = "token/";
        result = CmsStringUtil.splitAsArray(toSplit, "/");
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \"/\"   should contain 1 token but was split to: \""
            + Arrays.asList(result)
            + "\"!", 1, result.length);

        // test a 3 separator-only String
        toSplit = "///";
        result = CmsStringUtil.splitAsArray(toSplit, "/");
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \"/\"  should contain 2 tokens but was split to: \""
            + Arrays.asList(result)
            + "\"!", 2, result.length);

        // repetition of the same tests with a separator longer than 1 (as splitAsList(String,String,boolean) 
        // delegates to splitAsList(String, char, boolean) if separator's length is zero 
        // test usability for path-tokenization (e.g. admin tool of workplace)
        toSplit = ",,system,,workplace,,admin,,searchindex,,";
        result = CmsStringUtil.splitAsArray(toSplit, ",,");
        assertEquals("The string \""
            + toSplit
            + "\" split with separator \",,\"  should contain 4 tokens but was split to: \""
            + Arrays.asList(result)
            + "\"!", 4, result.length);

        // test an empty String: 
        toSplit = "";
        result = CmsStringUtil.splitAsArray(toSplit, ",,");
        assertEquals("The empty String \"\" should contain zero tokens but was split to: \""
            + Arrays.asList(result)
            + "\"!", 0, result.length);

        // test a whitespace String with truncation:
        toSplit = "               ";
        result = CmsStringUtil.splitAsArray(toSplit, ",,");
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \",,\" should contain zero tokens but was split to: \""
            + Arrays.asList(result)
            + "\"!", 1, result.length);

        // test a 1 separator-only String
        toSplit = ",,";
        result = CmsStringUtil.splitAsArray(toSplit, ",,");
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \",,\" should contain 2 tokens but was split to: \""
            + Arrays.asList(result)
            + "\"!", 0, result.length);

        // test a 2 separator-only String
        toSplit = ",,,,";
        result = CmsStringUtil.splitAsArray(toSplit, ",,");
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \",,\"  should contain 1 token but was split to: \""
            + Arrays.asList(result)
            + "\"!", 1, result.length);

        // test a single token String with starting delimiter
        toSplit = ",,token";
        result = CmsStringUtil.splitAsArray(toSplit, ",,");
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \",,\" should contain 1 token but was split to: \""
            + Arrays.asList(result)
            + "\"!", 1, result.length);

        // test a single token String with ending delimiter
        toSplit = "token,,";
        result = CmsStringUtil.splitAsArray(toSplit, ",,");
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \",,\" should contain 1 token but was split to: \""
            + Arrays.asList(result)
            + "\"!", 1, result.length);

        // test a 3 separator-only String
        toSplit = ",,,,,,";
        result = CmsStringUtil.splitAsArray(toSplit, ",,");
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \",,\" should contain 2 tokens but was split to: \""
            + Arrays.asList(result)
            + "\"!", 2, result.length);
        
        toSplit = ",a,,b,c,";
        result = CmsStringUtil.splitAsArray(toSplit, ",");
        assertEquals("a", result[0]);
        assertEquals("", result[1]);
        assertEquals("b", result[2]);
        assertEquals("c", result[3]);
        assertEquals(4, result.length);
        
        toSplit = ",,a, aber nicht b,,,,b, aber nicht c,,c, but not a,,";
        result = CmsStringUtil.splitAsArray(toSplit, ",,");
        assertEquals("a, aber nicht b", result[0]);
        assertEquals("", result[1]);
        assertEquals("b, aber nicht c", result[2]);
        assertEquals("c, but not a", result[3]);
        assertEquals(4, result.length);
    }

    /**
     * Tests <code>{@link CmsStringUtil#splitAsList(String, char, boolean)}</code>.<p>
     */
    public void testSplitAsListStringCharBoolean() {

        // test usability for path-tokenization (e.g. admin tool of workplace)
        String toSplit = "/system/workplace/admin/searchindex/";
        List result = CmsStringUtil.splitAsList(toSplit, '/', false);
        assertEquals("The string \""
            + toSplit
            + "\" split with separator \"/\"  should contain 4 tokens but was split to: \""
            + result
            + "\"!", 4, result.size());

        // test an empty String: 
        toSplit = "";
        result = CmsStringUtil.splitAsList(toSplit, '/', false);
        assertEquals(
            "The empty String \"\" should contain zero tokens but was split to: \"" + result + "\"!",
            0,
            result.size());

        // test a whitespace String with truncation:
        toSplit = "               ";
        result = CmsStringUtil.splitAsList(toSplit, '/', true);
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \"/\"  should contain zero tokens but was split to: \""
            + result
            + "\"!", 1, result.size());

        // test a 1 separator-only String
        toSplit = "/";
        result = CmsStringUtil.splitAsList(toSplit, '/', false);
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \"/\"  should contain 2 tokens but was split to: \""
            + result
            + "\"!", 0, result.size());

        // test a 2 separator-only String
        toSplit = "//";
        result = CmsStringUtil.splitAsList(toSplit, '/', false);
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \"/\"  should contain 1 token but was split to: \""
            + result
            + "\"!", 1, result.size());

        // test a single token String with starting delimiter
        toSplit = "/token";
        result = CmsStringUtil.splitAsList(toSplit, '/', false);
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \"/\"  should contain 1 token but was split to: \""
            + result
            + "\"!", 1, result.size());

        // test a single token String with ending delimiter
        toSplit = "token/";
        result = CmsStringUtil.splitAsList(toSplit, '/', false);
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \"/\"   should contain 1 token but was split to: \""
            + result
            + "\"!", 1, result.size());

        // test a 3 separator-only String
        toSplit = "///";
        result = CmsStringUtil.splitAsList(toSplit, '/', false);
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \"/\"  should contain 2 tokens but was split to: \""
            + result
            + "\"!", 2, result.size());
        
        toSplit = ",a,,b,c,";
        result = CmsStringUtil.splitAsList(toSplit, ',');
        assertEquals("a", result.get(0));
        assertEquals("", result.get(1));
        assertEquals("b", result.get(2));
        assertEquals("c", result.get(3));
        assertEquals(4, result.size());        
    }

    /**
     * Tests <code>{@link CmsStringUtil#splitAsList(String, String, boolean)}</code>.<p>
     */
    public void testSplitAsListStringStringBoolean() {

        // test usability for path-tokenization (e.g. admin tool of workplace)
        String toSplit = "/system/workplace/admin/searchindex/";
        List result = CmsStringUtil.splitAsList(toSplit, "/", false);
        assertEquals("The string \""
            + toSplit
            + "\" split with separator \"/\"  should contain 4 tokens but was split to: \""
            + result
            + "\"!", 4, result.size());

        // test an empty String: 
        toSplit = "";
        result = CmsStringUtil.splitAsList(toSplit, "/", false);
        assertEquals(
            "The empty String \"\" should contain zero tokens but was split to: \"" + result + "\"!",
            0,
            result.size());

        // test a whitespace String with truncation:
        toSplit = "               ";
        result = CmsStringUtil.splitAsList(toSplit, "/", true);
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \"/\"  should contain zero tokens but was split to: \""
            + result
            + "\"!", 1, result.size());

        // test a 1 separator-only String
        toSplit = "/";
        result = CmsStringUtil.splitAsList(toSplit, "/", false);
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \"/\"  should contain 2 tokens but was split to: \""
            + result
            + "\"!", 0, result.size());

        // test a 2 separator-only String
        toSplit = "//";
        result = CmsStringUtil.splitAsList(toSplit, "/", false);
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \"/\"  should contain 1 token but was split to: \""
            + result
            + "\"!", 1, result.size());

        // test a single token String with starting delimiter
        toSplit = "/token";
        result = CmsStringUtil.splitAsList(toSplit, "/", false);
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \"/\"  should contain 1 token but was split to: \""
            + result
            + "\"!", 1, result.size());

        // test a single token String with ending delimiter
        toSplit = "token/";
        result = CmsStringUtil.splitAsList(toSplit, "/", false);
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \"/\"   should contain 1 token but was split to: \""
            + result
            + "\"!", 1, result.size());

        // test a 3 separator-only String
        toSplit = "///";
        result = CmsStringUtil.splitAsList(toSplit, "/", false);
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \"/\"  should contain 2 tokens but was split to: \""
            + result
            + "\"!", 2, result.size());

        // repetition of the same tests with a separator longer than 1 (as splitAsList(String,String,boolean) 
        // delegates to splitAsList(String, char, boolean) if separator's length is zero 
        // test usability for path-tokenization (e.g. admin tool of workplace)
        toSplit = ",,system,,workplace,,admin,,searchindex,,";
        result = CmsStringUtil.splitAsList(toSplit, ",,", false);
        assertEquals("The string \""
            + toSplit
            + "\" split with separator \",,\"  should contain 4 tokens but was split to: \""
            + result
            + "\"!", 4, result.size());

        // test an empty String: 
        toSplit = "";
        result = CmsStringUtil.splitAsList(toSplit, ",,", false);
        assertEquals(
            "The empty String \"\" should contain zero tokens but was split to: \"" + result + "\"!",
            0,
            result.size());

        // test a whitespace String with truncation:
        toSplit = "               ";
        result = CmsStringUtil.splitAsList(toSplit, ",,", true);
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \",,\" should contain zero tokens but was split to: \""
            + result
            + "\"!", 1, result.size());

        // test a 1 separator-only String
        toSplit = ",,";
        result = CmsStringUtil.splitAsList(toSplit, ",,", false);
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \",,\" should contain 2 tokens but was split to: \""
            + result
            + "\"!", 0, result.size());

        // test a 2 separator-only String
        toSplit = ",,,,";
        result = CmsStringUtil.splitAsList(toSplit, ",,", false);
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \",,\"  should contain 1 token but was split to: \""
            + result
            + "\"!", 1, result.size());

        // test a single token String with starting delimiter
        toSplit = ",,token";
        result = CmsStringUtil.splitAsList(toSplit, ",,", false);
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \",,\" should contain 1 token but was split to: \""
            + result
            + "\"!", 1, result.size());

        // test a single token String with ending delimiter
        toSplit = "token,,";
        result = CmsStringUtil.splitAsList(toSplit, ",,", false);
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \",,\" should contain 1 token but was split to: \""
            + result
            + "\"!", 1, result.size());

        // test a 3 separator-only String
        toSplit = ",,,,,,";
        result = CmsStringUtil.splitAsList(toSplit, ",,", false);
        assertEquals("The String \""
            + toSplit
            + "\" split with separator \",,\" should contain 2 tokens but was split to: \""
            + result
            + "\"!", 2, result.size());
    }

    /**
     * Tests the basic String substitution.<p>
     */
    public void testSubstitute() {

        String test, result;

        String content = "<a href=\"/opencms/opencms/test.jpg\">";
        String search = "/opencms/opencms/";
        String replace = "\\${path}";

        test = CmsStringUtil.substitute(content, search, replace);
        System.out.println(test);
        assertEquals(test, "<a href=\"\\${path}test.jpg\">");

        test = CmsStringUtil.substitute(test, replace, search);
        assertEquals(test, "<a href=\"/opencms/opencms/test.jpg\">");

        content = "[0-9]$1/[^a]|/([}>\"'\\[]\\s*)/pics/";
        result = "[0-9]$1/[^a]|/([}>\"'\\[]\\s*)/pucs/";
        test = CmsStringUtil.substitute(content, "i", "u");
        assertEquals(test, result);
    }
}

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

package org.opencms.util;

import org.opencms.i18n.CmsEncoder;
import org.opencms.test.OpenCmsTestCase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

/**
 * Test cases for {@link org.opencms.util.CmsStringUtil}.<p>
 */
public class TestCmsStringUtil extends OpenCmsTestCase {

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
     * Tests the parseDuration method.<p>
     */
    public void testDuration() {

        long second = 1000;
        long minute = 60 * second;
        long hour = 60 * minute;
        long day = 24 * hour;

        assertEquals(day + (5 * minute), CmsStringUtil.parseDuration("   1d 5m  ", 0));
        assertEquals(hour + 5, CmsStringUtil.parseDuration("1h5ms", 0));
        assertEquals(3 * hour, CmsStringUtil.parseDuration("4x3h5y", 0));
        assertEquals((5 * second) + 16, CmsStringUtil.parseDuration("5s16ms", 0));

        // check default value
        assertEquals(0, CmsStringUtil.parseDuration("0s", 10));
        assertEquals(10, CmsStringUtil.parseDuration("4440", 10));
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
        assertEquals(result, CmsEncoder.ENCODING_UTF_8);

        xml = "<?xml version=\"1.0\" encoding='ISO-8859-1'?>\n" + "<opencms/>";

        result = CmsStringUtil.extractXmlEncoding(xml);
        assertEquals(result, "ISO-8859-1");
    }

    /**
     * Tests for the resource name formatting.<p>
     */
    public void testFormatResourceName() {

        String test;
        test = "/xmlcontentdemo/list.jsp";
        assertEquals("/.../list.jsp", CmsStringUtil.formatResourceName(test, 10));
        test = "/xmlcontentdemo/list.jsp";
        assertEquals("/xmlcontentdemo/list.jsp", CmsStringUtil.formatResourceName(test, 25));
        test = "/averylongresourcename.jsp";
        assertEquals("/averylongresourcename.jsp", CmsStringUtil.formatResourceName(test, 25));
        test = "/folder1/folder2/averylongresourcename.jsp";
        assertEquals("/.../averylongresourcename.jsp", CmsStringUtil.formatResourceName(test, 25));
        test = "/myfolder/subfolder/index.html";
        assertEquals("/.../index.html", CmsStringUtil.formatResourceName(test, 21));
        assertEquals("/myfolder/.../index.html", CmsStringUtil.formatResourceName(test, 25));
        test = "/myfolder/subfolder/subsubfolder/index.html";
        assertEquals("/myfolder/.../subsubfolder/index.html", CmsStringUtil.formatResourceName(test, 40));
        assertEquals("/myfolder/.../index.html", CmsStringUtil.formatResourceName(test, 36));
        assertEquals("/myfolder/.../index.html", CmsStringUtil.formatResourceName(test, 24));
        assertEquals("/.../index.html", CmsStringUtil.formatResourceName(test, 21));
        test = "/demopages/search-demo/example-documents/";
        assertEquals("/demopages/.../example-documents/", CmsStringUtil.formatResourceName(test, 40));
        assertEquals("/demopages/search-demo/example-documents/", CmsStringUtil.formatResourceName(test, 41));
    }

    /**
     * Test for getting the common prefix of two paths.
     */
    public void testGetCommonPrefixPath() {

        assertEquals("/", CmsStringUtil.getCommonPrefixPath("/foo", "/bar"));
        assertEquals("/foo", CmsStringUtil.getCommonPrefixPath("/foo/bar", "/foo/baz"));
        assertEquals("/foo", CmsStringUtil.getCommonPrefixPath("/foo/bar/", "/foo/baz"));
        assertEquals("/foo", CmsStringUtil.getCommonPrefixPath("/foo/bar/", "/foo/baz/"));
        assertEquals("/foo/bar", CmsStringUtil.getCommonPrefixPath("/foo/bar/baz/qux", "/foo/bar/xyzzy/narf"));
        assertEquals("/foo/bar", CmsStringUtil.getCommonPrefixPath("/foo/bar/baz/qux", "/foo/bar/"));
        assertEquals("/foo/bar", CmsStringUtil.getCommonPrefixPath("/foo/bar/baz/qux", "/foo/bar/xyzzy/narf/quop"));
        assertEquals("/foo/bar", CmsStringUtil.getCommonPrefixPath("/foo/bar/baz", "/foo/bar/baz1"));
        assertEquals("/", CmsStringUtil.getCommonPrefixPath("/foo/bar/baz/qux", "/qux/baz/bar/foo"));
        assertEquals("/", CmsStringUtil.getCommonPrefixPath("/foo/bar/baz/qux", "/"));
    }

    /**
     * Test for the getRelativeSubPath utility method.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testGetRelativeSubPath() throws Exception {

        assertEquals("/", CmsStringUtil.getRelativeSubPath("/foo", "/foo/"));
        assertEquals("/", CmsStringUtil.getRelativeSubPath("/foo", "/foo"));
        assertEquals("/foo", CmsStringUtil.getRelativeSubPath("/bar", "/bar/foo"));
        assertEquals("/foo", CmsStringUtil.getRelativeSubPath("/bar", "/bar/foo/"));
        assertEquals("/foo", CmsStringUtil.getRelativeSubPath("/bar/", "/bar/foo/"));
        assertEquals(null, CmsStringUtil.getRelativeSubPath("/foo", "/foo1"));
        assertEquals(null, CmsStringUtil.getRelativeSubPath("/foo", "/bar"));
    }

    /**
     * Test case for join path method-<p>
     */
    public void testJoinPath() {

        assertEquals("/system/", CmsStringUtil.joinPaths("/system/"));
        assertEquals("/system/", CmsStringUtil.joinPaths("/system", "/"));
        assertEquals("/system/", CmsStringUtil.joinPaths("/system", ""));
        assertEquals("/system/", CmsStringUtil.joinPaths("/system/", "/"));
        assertEquals("/system/", CmsStringUtil.joinPaths("/", "/system/", "/"));
        assertEquals("/system/", CmsStringUtil.joinPaths("/", "system", "/"));
        assertEquals("/system/", CmsStringUtil.joinPaths("", "/system/", ""));
        assertEquals("/system/", CmsStringUtil.joinPaths("/system/", "/", "/"));
        assertEquals("/system/", CmsStringUtil.joinPaths("/", "/", "/system/"));
        assertEquals("/system/", CmsStringUtil.joinPaths("", "", "/system/"));
        assertEquals("/foo/bar/baz", CmsStringUtil.joinPaths("/foo/", "/bar", "baz"));
        assertEquals("/foo/bar/baz", CmsStringUtil.joinPaths("/foo", "bar", "baz"));
        assertEquals("/foo/bar/baz", CmsStringUtil.joinPaths("/foo/", "/bar/", "/baz"));
        assertEquals("/foo/bar/baz", CmsStringUtil.joinPaths("/foo", "/bar/", "baz"));
        assertEquals("/foo/bar/baz", CmsStringUtil.joinPaths("/foo//bar/", "/baz"));
        assertEquals("/foo/bar/baz", CmsStringUtil.joinPaths("/foo/////////bar/", "/baz"));
        assertEquals("/foo/bar/baz", CmsStringUtil.joinPaths("//////////foo/////////bar/////", "//////baz"));
    }

    /**
     * Test case for {@link CmsStringUtil#lastIndexOf(String, char[])} method.<p>
     */
    public void testLastIndexOf() {

        int result;

        result = CmsStringUtil.lastIndexOf("This is a Text", CmsStringUtil.SENTENCE_ENDING_CHARS);
        assertEquals(-1, result);

        result = CmsStringUtil.lastIndexOf("This ! is ? a . Text", CmsStringUtil.SENTENCE_ENDING_CHARS);
        assertEquals(14, result);
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
     * Test for the isPrefixPath method.
     */
    public void testPrefixPath() {

        assertTrue(CmsStringUtil.isPrefixPath("/", "/a"));
        assertTrue(CmsStringUtil.isPrefixPath("/a", "/a/b"));
        assertTrue(CmsStringUtil.isPrefixPath("/a/", "/a/b"));
        assertTrue(CmsStringUtil.isPrefixPath("/", "/a/b"));
        assertTrue(CmsStringUtil.isPrefixPath("", "/a/b"));
        assertTrue(CmsStringUtil.isPrefixPath("/a/b/", "/a/b"));
        assertTrue(CmsStringUtil.isPrefixPath("/a/b/", "/a/b/"));

        assertFalse(CmsStringUtil.isPrefixPath("/a", "/aa/b"));
        assertFalse(CmsStringUtil.isPrefixPath("/a/b", "/a"));

    }

    /**
     * Test for the isProperPrefixPath method.
     */
    public void testProperPrefixPath() {

        assertTrue(CmsStringUtil.isProperPrefixPath("/", "/a"));
        assertTrue(CmsStringUtil.isProperPrefixPath("/a", "/a/b"));
        assertTrue(CmsStringUtil.isProperPrefixPath("/a/", "/a/b"));
        assertTrue(CmsStringUtil.isProperPrefixPath("/", "/a/b"));
        assertTrue(CmsStringUtil.isProperPrefixPath("", "/a/b"));
        assertFalse(CmsStringUtil.isProperPrefixPath("/a/b/", "/a/b"));
        assertFalse(CmsStringUtil.isProperPrefixPath("/a/b/", "/a/b/"));
        assertFalse(CmsStringUtil.isProperPrefixPath("/a/b", "/a/b/"));
        assertFalse(CmsStringUtil.isProperPrefixPath("/a", "/aa/b"));
        assertFalse(CmsStringUtil.isProperPrefixPath("/a/b", "/a"));

    }

    /**
     * Tests the 'removePrefixPath' method.
     */
    public void testRemovePrefixPath() {

        assertEquals(java.util.Optional.empty(), CmsStringUtil.removePrefixPath("/foo", "/foobar"));
        assertEquals(java.util.Optional.empty(), CmsStringUtil.removePrefixPath("/foo", "/bar"));
        assertEquals(java.util.Optional.of("/bar"), CmsStringUtil.removePrefixPath("/foo", "/foo/bar"));
        assertEquals(java.util.Optional.of("/bar"), CmsStringUtil.removePrefixPath("/foo/", "/foo/bar"));
        assertEquals(java.util.Optional.of("/bar"), CmsStringUtil.removePrefixPath("/foo/", "/foo/bar/"));
        assertEquals(java.util.Optional.of("/bar"), CmsStringUtil.removePrefixPath("/foo", "/foo/bar/"));
        assertEquals(
            java.util.Optional.of("/foo/bar"),
            CmsStringUtil.removePrefixPath("/xyzzy/blargh", "/xyzzy/blargh/foo/bar"));
        assertEquals(java.util.Optional.of("/"), CmsStringUtil.removePrefixPath("/foo", "/foo"));
        assertEquals(java.util.Optional.of("/"), CmsStringUtil.removePrefixPath("/foo/", "/foo/"));
        assertEquals(java.util.Optional.of("/"), CmsStringUtil.removePrefixPath("/foo/", "/foo"));
        assertEquals(java.util.Optional.of("/"), CmsStringUtil.removePrefixPath("/foo", "/foo/"));
    }

    /**
     * Tests <code>{@link CmsStringUtil#replacePrefix(String, String, String, boolean)}</code>.<p>
     */
    public void testReplacePrefix() {

        assertEquals(Optional.absent(), CmsStringUtil.replacePrefix("foo", "x", "y", false));
        assertEquals(Optional.of("y-foo"), CmsStringUtil.replacePrefix("x-foo", "x", "y", false));
        assertEquals(Optional.of("xxx-foo"), CmsStringUtil.replacePrefix("xx-foo", "x", "xx", false));

        assertEquals(Optional.of("y-foo"), CmsStringUtil.replacePrefix("X-foo", "x", "y", true));
        assertEquals(Optional.absent(), CmsStringUtil.replacePrefix("X-foo", "x", "y", false));

    }

    /**
     * Test for the splitAsMap() method.
     */
    public void testSplitAsMap() throws Exception {
        String config ="\n"
            + "        A:Label for A|\n"
            + "        B:Label for B|\n"
            + "        C:Label for C|\n"
            + "        foo  |\n"
            + "        none:Label for none\n"
            + "    ";
        Map<String, String> options = CmsStringUtil.splitAsMap(config, "|", ":");
        Map<String, String> expected = new HashMap<>();
        expected.put("A", "Label for A");
        expected.put("B", "Label for B");
        expected.put("C", "Label for C");
        expected.put("foo", "");
        expected.put("none", "Label for none");
        assertEquals(expected, options);
    }

    /**
     * Tests <code>{@link CmsStringUtil#splitAsArray(String, char)}</code>.<p>
     */
    public void testSplitCharDelimiter() {

        String toSplit;
        char delimChar = '/';
        String[] arrayResult;
        List<String> listResult;

        // test usability for path-tokenization (e.g. admin tool of workplace)
        toSplit = "/system/workplace/admin/searchindex/";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimChar);
        assertEquals(4, arrayResult.length);
        assertEquals("system", arrayResult[0]);
        assertEquals("workplace", arrayResult[1]);
        assertEquals("admin", arrayResult[2]);
        assertEquals("searchindex", arrayResult[3]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimChar);
        assertEquals(Arrays.asList(arrayResult), listResult);

        // test an empty String:
        toSplit = "";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimChar);
        assertEquals(0, arrayResult.length);
        listResult = CmsStringUtil.splitAsList(toSplit, delimChar);
        assertEquals(Arrays.asList(arrayResult), listResult);

        // test a whitespace only String
        toSplit = "               ";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimChar);
        assertEquals(1, arrayResult.length);
        assertEquals(toSplit, arrayResult[0]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimChar);
        assertEquals(Arrays.asList(arrayResult), listResult);

        // with truncation
        listResult = CmsStringUtil.splitAsList(toSplit, delimChar, true);
        assertEquals(1, listResult.size());
        assertEquals("", listResult.get(0));

        // test a 1 separator-only String
        toSplit = "/";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimChar);
        assertEquals(0, arrayResult.length);
        listResult = CmsStringUtil.splitAsList(toSplit, delimChar);
        assertEquals(Arrays.asList(arrayResult), listResult);

        // test a 2 separator-only String
        toSplit = "//";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimChar);
        assertEquals(1, arrayResult.length);
        assertEquals("", arrayResult[0]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimChar);
        assertEquals(Arrays.asList(arrayResult), listResult);

        // test a single token String with starting delimiter
        toSplit = "/token";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimChar);
        assertEquals(1, arrayResult.length);
        assertEquals("token", arrayResult[0]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimChar);
        assertEquals(Arrays.asList(arrayResult), listResult);

        // test a single token String with ending delimiter
        toSplit = "token/";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimChar);
        assertEquals(1, arrayResult.length);
        assertEquals("token", arrayResult[0]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimChar);
        assertEquals(Arrays.asList(arrayResult), listResult);

        // test a 3 separator-only String
        toSplit = "///";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimChar);
        assertEquals(2, arrayResult.length);
        assertEquals("", arrayResult[0]);
        assertEquals("", arrayResult[1]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimChar);
        assertEquals(Arrays.asList(arrayResult), listResult);

        toSplit = "/a // b/ c /";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimChar);
        assertEquals(4, arrayResult.length);
        assertEquals("a ", arrayResult[0]);
        assertEquals("", arrayResult[1]);
        assertEquals(" b", arrayResult[2]);
        assertEquals(" c ", arrayResult[3]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimChar);
        assertEquals(Arrays.asList(arrayResult), listResult);

        // with truncation
        listResult = CmsStringUtil.splitAsList(toSplit, delimChar, true);
        assertEquals(4, listResult.size());
        assertEquals("a", listResult.get(0));
        assertEquals("", listResult.get(1));
        assertEquals("b", listResult.get(2));
        assertEquals("c", listResult.get(3));
    }


    /**
     * Test for the splitOptions() method.
     */
    public void testSplitOptions() throws Exception {
        String config ="\n"
            + "        A:Label for A|\n"
            + "        B:Label for B|\n"
            + "        C:Label for C \\\\|\n"
            + "        foo  |\n"
            + "        none:Label for none|\n"
            + "        a\\:a\\|b\\:b:c\\:c\\|d\\:d|\n"
            + "    ";
        Map<String, String> options = CmsStringUtil.splitOptions(config);
        Map<String, String> expected = new HashMap<>();
        expected.put("A", "Label for A");
        expected.put("B", "Label for B");
        expected.put("C", "Label for C \\");
        expected.put("foo", "");
        expected.put("none", "Label for none");
        expected.put("a:a|b:b", "c:c|d:d");
        assertEquals(expected, options);
    }

    /**
     * Tests <code>{@link CmsStringUtil#splitAsArray(String, String)}</code>.<p>
     */
    public void testSplitStringDelimiter() {

        String toSplit;
        String delimString = "/";
        String[] arrayResult;
        List<String> listResult;

        toSplit = "/system/workplace/admin/searchindex/";

        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimString);
        assertEquals(4, arrayResult.length);
        assertEquals("system", arrayResult[0]);
        assertEquals("workplace", arrayResult[1]);
        assertEquals("admin", arrayResult[2]);
        assertEquals("searchindex", arrayResult[3]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimString);
        assertEquals(Arrays.asList(arrayResult), listResult);

        // test an empty String:
        toSplit = "";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimString);
        assertEquals(0, arrayResult.length);
        listResult = CmsStringUtil.splitAsList(toSplit, delimString);
        assertEquals(Arrays.asList(arrayResult), listResult);

        // whitespace only String
        toSplit = "               ";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimString);
        assertEquals(1, arrayResult.length);
        assertEquals(toSplit, arrayResult[0]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimString);
        assertEquals(Arrays.asList(arrayResult), listResult);

        // with truncation
        listResult = CmsStringUtil.splitAsList(toSplit, delimString, true);
        assertEquals(1, listResult.size());
        assertEquals("", listResult.get(0));

        // test a 1 separator-only String
        toSplit = "/";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimString);
        assertEquals(0, arrayResult.length);
        listResult = CmsStringUtil.splitAsList(toSplit, delimString);
        assertEquals(Arrays.asList(arrayResult), listResult);

        // test a 2 separator-only String
        toSplit = "//";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimString);
        assertEquals(1, arrayResult.length);
        assertEquals("", arrayResult[0]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimString);
        assertEquals(Arrays.asList(arrayResult), listResult);

        // test a single token String with starting delimiter
        toSplit = "/token";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimString);
        assertEquals(1, arrayResult.length);
        assertEquals("token", arrayResult[0]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimString);
        assertEquals(Arrays.asList(arrayResult), listResult);

        // test a single token String with ending delimiter
        toSplit = "token/";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimString);
        assertEquals(1, arrayResult.length);
        assertEquals("token", arrayResult[0]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimString);
        assertEquals(Arrays.asList(arrayResult), listResult);

        // test a 3 separator-only String
        toSplit = "///";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimString);
        assertEquals(2, arrayResult.length);
        assertEquals("", arrayResult[0]);
        assertEquals("", arrayResult[1]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimString);
        assertEquals(Arrays.asList(arrayResult), listResult);

        toSplit = "/a // b/ c /";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimString);
        assertEquals(4, arrayResult.length);
        assertEquals("a ", arrayResult[0]);
        assertEquals("", arrayResult[1]);
        assertEquals(" b", arrayResult[2]);
        assertEquals(" c ", arrayResult[3]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimString);
        assertEquals(Arrays.asList(arrayResult), listResult);

        // with truncation
        listResult = CmsStringUtil.splitAsList(toSplit, delimString, true);
        assertEquals(4, listResult.size());
        assertEquals("a", listResult.get(0));
        assertEquals("", listResult.get(1));
        assertEquals("b", listResult.get(2));
        assertEquals("c", listResult.get(3));

        // some tests with a separator longer than 1

        delimString = ",,";
        toSplit = ",,system,,workplace,,admin,,searchindex,,";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimString);
        assertEquals(4, arrayResult.length);
        assertEquals("system", arrayResult[0]);
        assertEquals("workplace", arrayResult[1]);
        assertEquals("admin", arrayResult[2]);
        assertEquals("searchindex", arrayResult[3]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimString);
        assertEquals(Arrays.asList(arrayResult), listResult);

        // test an empty String:
        toSplit = "";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimString);
        assertEquals(0, arrayResult.length);
        listResult = CmsStringUtil.splitAsList(toSplit, delimString);
        assertEquals(Arrays.asList(arrayResult), listResult);

        // test a whitespace String with truncation:
        toSplit = "               ";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimString);
        assertEquals(1, arrayResult.length);
        assertEquals(toSplit, arrayResult[0]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimString);
        assertEquals(Arrays.asList(arrayResult), listResult);

        // test a 1 separator-only String
        toSplit = ",,";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimString);
        assertEquals(0, arrayResult.length);
        listResult = CmsStringUtil.splitAsList(toSplit, delimString);
        assertEquals(Arrays.asList(arrayResult), listResult);

        // test a 2 separator-only String
        toSplit = ",,,,";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimString);
        assertEquals(1, arrayResult.length);
        assertEquals("", arrayResult[0]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimString);
        assertEquals(Arrays.asList(arrayResult), listResult);

        // test a single token String with starting delimiter
        toSplit = ",,token";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimString);
        assertEquals(1, arrayResult.length);
        assertEquals("token", arrayResult[0]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimString);
        assertEquals(Arrays.asList(arrayResult), listResult);

        // test a single token String with ending delimiter
        toSplit = "token,,";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimString);
        assertEquals(1, arrayResult.length);
        assertEquals("token", arrayResult[0]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimString);
        assertEquals(Arrays.asList(arrayResult), listResult);

        // test a 3 separator-only String
        toSplit = ",,,,,,";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimString);
        assertEquals(2, arrayResult.length);
        assertEquals("", arrayResult[0]);
        assertEquals("", arrayResult[1]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimString);
        assertEquals(Arrays.asList(arrayResult), listResult);

        toSplit = ",,a, aber nicht b,,,,b, aber nicht c,,c, but not a,,";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimString);
        assertEquals(4, arrayResult.length);
        assertEquals("a, aber nicht b", arrayResult[0]);
        assertEquals("", arrayResult[1]);
        assertEquals("b, aber nicht c", arrayResult[2]);
        assertEquals("c, but not a", arrayResult[3]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimString);
        assertEquals(Arrays.asList(arrayResult), listResult);

        delimString = "/delim/";
        toSplit = "/delim fake at start/delim//not a delim//delim//delim//delim fake at end";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimString);
        assertEquals(4, arrayResult.length);
        assertEquals("/delim fake at start", arrayResult[0]);
        assertEquals("/not a delim/", arrayResult[1]);
        assertEquals("", arrayResult[2]);
        assertEquals("/delim fake at end", arrayResult[3]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimString);
        assertEquals(Arrays.asList(arrayResult), listResult);

        toSplit = "/delim fake at start/delim//not a delim//delim//delim//delim";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimString);
        assertEquals(4, arrayResult.length);
        assertEquals("/delim fake at start", arrayResult[0]);
        assertEquals("/not a delim/", arrayResult[1]);
        assertEquals("", arrayResult[2]);
        assertEquals("/delim", arrayResult[3]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimString);
        assertEquals(Arrays.asList(arrayResult), listResult);

        toSplit = "/delim//delim fake at start/delim//not a delim//delim//delim//delim fake at end/delim/";
        arrayResult = CmsStringUtil.splitAsArray(toSplit, delimString);
        assertEquals(4, arrayResult.length);
        assertEquals("/delim fake at start", arrayResult[0]);
        assertEquals("/not a delim/", arrayResult[1]);
        assertEquals("", arrayResult[2]);
        assertEquals("/delim fake at end", arrayResult[3]);
        listResult = CmsStringUtil.splitAsList(toSplit, delimString);
        assertEquals(Arrays.asList(arrayResult), listResult);
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

        content = "/delim//delim fake at start/delim//not a delim//delim//delim//delim fake at end/delim/";
        result = "REPLACED!/delim fake at startREPLACED!/not a delim/REPLACED!REPLACED!/delim fake at endREPLACED!";
        test = CmsStringUtil.substitute(content, "/delim/", "REPLACED!");
        assertEquals(test, result);
    }

    /**
     * Tests path component translation.
     */
    public void testTranslatePathComponents() {

        String[] substitutions = {"s/ /_/g", "s/[^a-z0-9_]/x/g"};
        CmsResourceTranslator translator = new CmsResourceTranslator(substitutions, true);
        String input = "the quick brown fo$//ju/m/ps/over the_lazy/dog";
        String expectedOutput = "the_quick_brown_fox//ju/m/ps/over_the_lazy/dog";
        assertNotSame(expectedOutput, translator.translateResource(input));
        assertEquals(expectedOutput, CmsStringUtil.translatePathComponents(translator, input));
    }

    /**
     * Test case for {@link CmsStringUtil#trimToSize(String, int)} method.<p>
     */
    public void testTrimToSize() {

        String text, result, expected;

        text = "This is a short sentence.";
        expected = text;
        result = CmsStringUtil.trimToSize(text, 75);
        assertEquals(expected, result);

        text = "I am a short sentence.";
        expected = "I am a...";
        result = CmsStringUtil.trimToSize(text, 10, "...");
        assertEquals(expected, result);

        text = "I_am_a_short_sentence.";
        expected = "I_am_a_sh...";
        result = CmsStringUtil.trimToSize(text, 12, "...");
        assertEquals(expected, result);

        text = "I am a short sentence.";
        expected = "I am...";
        result = CmsStringUtil.trimToSize(text, 8, "...");
        assertEquals(expected, result);

        text = "I am a short sentence.";
        expected = "I...";
        result = CmsStringUtil.trimToSize(text, 7, "...");
        assertEquals(expected, result);

        text = "I am a short sentence.";
        expected = "I...";
        result = CmsStringUtil.trimToSize(text, 6, "...");
        assertEquals(expected, result);

        text = "I am a short sentence.";
        expected = "I...";
        result = CmsStringUtil.trimToSize(text, 5, "...");
        assertEquals(expected, result);

        text = "I am a short sentence.";
        expected = "I...";
        result = CmsStringUtil.trimToSize(text, 4, "...");
        assertEquals(expected, result);

        text = "I am a short sentence.";
        expected = "...";
        result = CmsStringUtil.trimToSize(text, 3, "...");
        assertEquals(expected, result);

        text = "I am a short sentence.";
        expected = "..";
        result = CmsStringUtil.trimToSize(text, 2, "...");
        assertEquals(expected, result);

        text = "I am a short sentence.";
        expected = ".";
        result = CmsStringUtil.trimToSize(text, 1, "...");
        assertEquals(expected, result);

        text = null;
        expected = null;
        result = CmsStringUtil.trimToSize(text, 10);
        assertEquals(expected, result);
    }

    /**
     * Test case for {@link CmsStringUtil#trimToSize(String, int, int, String)}.<p>
     */
    public void testTrimToSizeText() {

        String text, result, expected;

        text = "This is a short sentence.";
        expected = text;
        result = CmsStringUtil.trimToSize(text, 75, 40, " ...");
        assertEquals(expected, result);

        text = "This is a short sentence. This is a longer sentence at the end of the short sentence.";
        expected = "This is a short sentence. This is a longer sentence at the end of the ...";
        result = CmsStringUtil.trimToSize(text, 75, 40, " ...");
        assertEquals(expected, result);

        text = "This is a short sentence. This is a longer sentence at the end of the short sentence.";
        expected = "This is a short sentence. ...";
        result = CmsStringUtil.trimToSize(text, 75, 75, " ...");
        assertEquals(expected, result);
    }

}
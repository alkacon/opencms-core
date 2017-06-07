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

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.nodes.TextNode;

/**
 * Test case for <code>{@link org.opencms.util.CmsHtmlExtractor}</code>.<p>
 *
 * @since 6.2.0
 */
public class TestCmsHtmlExtractor extends OpenCmsTestCase {

    private static final String HTML_PAGE_1 = "<html><title>This is the title</title><body><h1>A headline</h1>This is a test.<br>"
        + "This  is&nbsp;a <a href=\"http://www.opencms.org\">link</a> in a    paragraph.<p>Some more text here. "
        + "This is a very long line, because this is long line, because this is long line, because this is long line, because this is long line. "
        + "This is a very long line, because this is long line, because this is long line, because this is long line, because this is long line. "
        + "<p>This is a paragraph.</p>"
        + "This is a very long line, because this is long line, because this is long line, because this is long line, because this is long line. "
        + "<div><p>This is a p in a div<p>This is another p in a div<p></div>"
        + "<h2>Another headline <b>with some tag content</b></h2>"
        + "<p>This is a paragraph.</p>"
        + "This is a very long line, because this is long line, because this is long line, because this is long line, because this is long line. "
        + "<div><p>This is a p in a div<p>This is another p in a div<p></div>"
        + "</body></html>";

    /**
     * Extracts plain text from a String that contains HTML.<p>
     *
     * @param content the HTML content to extract the text from
     *
     * @return the extracted plain text
     *
     * @throws Exception in case something goes wrong
     */
    public static String extractFromHtml2(String content) throws Exception {

        Parser parser = new Parser();
        parser.setInputHTML(content);

        StringBean stringBean = new StringBean();
        stringBean.setLinks(true);
        stringBean.setCollapse(true);

        parser.visitAllNodesWith(stringBean);

        return stringBean.getStrings();
    }

    /**
     * Tests the HTML extractor.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testHtmlExtractor() throws Exception {

        String result;

        result = CmsHtmlExtractor.extractText(HTML_PAGE_1, CmsEncoder.ENCODING_ISO_8859_1);
        System.out.println(result + "\n\n");

        result = extractFromHtml(HTML_PAGE_1);
        System.out.println(result + "\n\n");

        result = extractFromHtml2(HTML_PAGE_1);
        System.out.println(result + "\n\n");
    }

    /**
     * Tests the HTML extractor with an empty input String.<p>
     *
     * @throws Exception in case the test fails
     */
    public void testHtmlExtractorWithEmptyInput() throws Exception {

        String input, result;
        input = "";
        result = CmsHtmlExtractor.extractText(input, CmsEncoder.ENCODING_ISO_8859_1);
        assertEquals("Empty input should generate empty output", input, result);

        input = null;
        result = CmsHtmlExtractor.extractText(input, CmsEncoder.ENCODING_ISO_8859_1);
        assertEquals("null input should generate null output", input, result);

        input = "   \t\r\n  ";
        result = CmsHtmlExtractor.extractText(input, CmsEncoder.ENCODING_ISO_8859_1);
        assertEquals("Whitespace only input should generate empty String output", "", result);
    }

    /**
     * Extracts plain text from a String that contains HTML.<p>
     *
     * @param content the HTML content to extract the text from
     *
     * @return the extracted plain text
     *
     * @throws Exception in case something goes wrong
     */
    private String extractFromHtml(String content) throws Exception {

        Parser myParser;
        Node[] nodes = null;
        myParser = Parser.createParser(content, null);

        NodeFilter filter = new NodeClassFilter(TextNode.class);

        nodes = myParser.extractAllNodesThatMatch(filter).toNodeArray();

        StringBuffer result = new StringBuffer();

        for (int i = 0; i < nodes.length; i++) {
            TextNode textnode = (TextNode)nodes[i];
            String line = textnode.toPlainTextString().trim();
            result.append(line);
        }

        return result.toString();
    }
}
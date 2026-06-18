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

package org.opencms.util;

import org.opencms.test.OpenCmsTestRunner;

import org.junit.jupiter.api.Test;

/**
 * Test case for <code>{@link org.opencms.util.CmsHtml2MarkdownConverter}</code>.<p>
 *
 * @since 22.0.0
 */
public class TestCmsHtml2MarkdownConverter extends OpenCmsTestRunner {

    /**
     * Tests blank and null input.<p>
     */
    @Test
    public void testBlankInput() {

        assertEquals("", CmsHtml2MarkdownConverter.html2markdown(null));
        assertEquals("", CmsHtml2MarkdownConverter.html2markdown("   "));
    }

    /**
     * Tests that a link wrapping block level content (such as a whole card) keeps the wrapped heading
     * and paragraph as blocks instead of flattening them to inline text. The link is dropped, the
     * heading stays a heading.<p>
     */
    @Test
    public void testBlockLevelLink() {

        String html = "<a href=\"/download\"><div class=\"heading\"><h3>Download OpenCms 21</h3></div>"
            + "<div class=\"body\"><p>Download the latest version.</p></div></a>";
        assertEquals(
            "### Download OpenCms 21\n\nDownload the latest version.",
            CmsHtml2MarkdownConverter.html2markdown(html, true, true));
    }

    /**
     * Tests blockquotes, including multiple paragraphs.<p>
     */
    @Test
    public void testBlockquote() {

        assertEquals("> Quote", CmsHtml2MarkdownConverter.html2markdown("<blockquote><p>Quote</p></blockquote>"));
        assertEquals(
            "> A\n>\n> B",
            CmsHtml2MarkdownConverter.html2markdown("<blockquote><p>A</p><p>B</p></blockquote>"));
    }

    /**
     * Tests fenced code blocks and inline code.<p>
     */
    @Test
    public void testCode() {

        assertEquals(
            "```java\nint x = 1;\n```",
            CmsHtml2MarkdownConverter.html2markdown("<pre><code class=\"language-java\">int x = 1;</code></pre>"));
        assertEquals("Use `x = 1` now", CmsHtml2MarkdownConverter.html2markdown("<p>Use <code>x = 1</code> now</p>"));
        // blank lines inside a code block are preserved (the per-block strip only touches the fences)
        assertEquals("```\na\n\nb\n```", CmsHtml2MarkdownConverter.html2markdown("<pre><code>a\n\nb</code></pre>"));
    }

    /**
     * Tests minimal Markdown escaping in text content.<p>
     */
    @Test
    public void testEscaping() {

        assertEquals("a\\*b\\_c", CmsHtml2MarkdownConverter.html2markdown("<p>a*b_c</p>"));
    }

    /**
     * Tests headings and inline emphasis.<p>
     */
    @Test
    public void testHeadingsAndEmphasis() {

        assertEquals(
            "# Title\n\nHello **world** and *mars*.",
            CmsHtml2MarkdownConverter.html2markdown(
                "<h1>Title</h1><p>Hello <strong>world</strong> and <em>mars</em>.</p>"));
        assertEquals("## Hello *x*", CmsHtml2MarkdownConverter.html2markdown("<h2>Hello <em>x</em></h2>"));
    }

    /**
     * Tests horizontal rules and hard line breaks.<p>
     */
    @Test
    public void testHrAndBr() {

        assertEquals("a\nb\n\n---", CmsHtml2MarkdownConverter.html2markdown("<p>a<br>b</p><hr>"));
    }

    /**
     * Tests the ignoreImages option, which drops images and their related markup (for example the
     * copyright text held in the title attribute).<p>
     */
    @Test
    public void testIgnoreImages() {

        String html = "<p>Text</p><p><img src=\"/a.png\" alt=\"Alt\" title=\"(c) X\"></p>";
        assertEquals("Text\n\n![Alt](/a.png \"(c) X\")", CmsHtml2MarkdownConverter.html2markdown(html));
        assertEquals("Text", CmsHtml2MarkdownConverter.html2markdown(html, true, false));
    }

    /**
     * Tests images.<p>
     */
    @Test
    public void testImage() {

        assertEquals(
            "![An image](/img.png)",
            CmsHtml2MarkdownConverter.html2markdown("<p><img src=\"/img.png\" alt=\"An image\"></p>"));
    }

    /**
     * Tests that adjacent CSS-spaced elements flattened inline (for example a list teaser tile) are
     * separated by a space, including nested block boundaries and empty elements, while a space that
     * is already present is not doubled.<p>
     */
    @Test
    public void testInlineBoundarySpacing() {

        // adjacent elements with no whitespace between them are separated
        assertEquals(
            "- a b",
            CmsHtml2MarkdownConverter.html2markdown("<ul><li><span>a</span><span>b</span></li></ul>"));
        // a nested block boundary also yields a space
        assertEquals(
            "- some other text",
            CmsHtml2MarkdownConverter.html2markdown("<ul><li><div>some<div>other</div>text</div></li></ul>"));
        // an empty element becomes a single space
        assertEquals(
            "- some text",
            CmsHtml2MarkdownConverter.html2markdown("<ul><li><div>some<span class=\"x\"></span>text</div></li></ul>"));
        // a space already present in the source is not doubled
        assertEquals(
            "- a b",
            CmsHtml2MarkdownConverter.html2markdown("<ul><li><span>a</span> <span>b</span></li></ul>"));
        // attaching punctuation in its own element keeps no leading space (e.g. a colon in an sr-only span)
        assertEquals(
            "- Release notes: OpenCms 21",
            CmsHtml2MarkdownConverter.html2markdown(
                "<ul><li><span>Release notes<span class=\"sr-only\">:</span></span><span>OpenCms 21</span></li></ul>"));
    }

    /**
     * Tests a plain inline fragment without any block level wrapper.<p>
     */
    @Test
    public void testInlineFragment() {

        assertEquals("Hello **x**", CmsHtml2MarkdownConverter.html2markdown("Hello <b>x</b>"));
    }

    /**
     * Tests that inline markup (bold, italic, inline code, links) is not force separated, so it keeps
     * the whitespace from the source.<p>
     */
    @Test
    public void testInlineMarkupNotForceSeparated() {

        assertEquals("go**now**", CmsHtml2MarkdownConverter.html2markdown("<p>go<strong>now</strong></p>"));
        assertEquals(
            "See the docs now",
            CmsHtml2MarkdownConverter.html2markdown("<p>See <a href=\"/x\">the docs</a> now</p>", false, true));
    }

    /**
     * Tests links with and without a title.<p>
     */
    @Test
    public void testLinks() {

        assertEquals(
            "[link](https://x.com) and [y](/y \"Y\")",
            CmsHtml2MarkdownConverter.html2markdown(
                "<p><a href=\"https://x.com\">link</a> and <a href=\"/y\" title=\"Y\">y</a></p>"));
    }

    /**
     * Tests unordered and ordered lists.<p>
     */
    @Test
    public void testLists() {

        assertEquals("- One\n- Two", CmsHtml2MarkdownConverter.html2markdown("<ul><li>One</li><li>Two</li></ul>"));
        assertEquals("1. One\n2. Two", CmsHtml2MarkdownConverter.html2markdown("<ol><li>One</li><li>Two</li></ol>"));
    }

    /**
     * Tests nested inline markup (bold inside a link).<p>
     */
    @Test
    public void testNestedInline() {

        assertEquals(
            "[**bold link**](/x)",
            CmsHtml2MarkdownConverter.html2markdown("<p><a href=\"/x\"><strong>bold link</strong></a></p>"));
    }

    /**
     * Tests nested lists.<p>
     */
    @Test
    public void testNestedList() {

        assertEquals(
            "- A\n  - B\n- C",
            CmsHtml2MarkdownConverter.html2markdown("<ul><li>A<ul><li>B</li></ul></li><li>C</li></ul>"));
    }

    /**
     * Tests folding of typographic punctuation to plain ASCII.<p>
     */
    @Test
    public void testPunctuationFolding() {

        // em dash and en dash become a hyphen
        assertEquals("a-b", CmsHtml2MarkdownConverter.html2markdown("<p>a—b</p>"));
        assertEquals("1-2", CmsHtml2MarkdownConverter.html2markdown("<p>1–2</p>"));
        // German low-9 and closing double quotes become straight double quotes
        assertEquals("\"Hallo\"", CmsHtml2MarkdownConverter.html2markdown("<p>„Hallo“</p>"));
        // guillemets become straight double quotes
        assertEquals("\"Bonjour\"", CmsHtml2MarkdownConverter.html2markdown("<p>«Bonjour»</p>"));
        // a curly apostrophe becomes a straight apostrophe
        assertEquals("it's", CmsHtml2MarkdownConverter.html2markdown("<p>it’s</p>"));
        // a horizontal ellipsis becomes three dots
        assertEquals("Wait...", CmsHtml2MarkdownConverter.html2markdown("<p>Wait…</p>"));
        // acute accent and modifier apostrophe become a straight apostrophe
        assertEquals("it's", CmsHtml2MarkdownConverter.html2markdown("<p>it´s</p>"));
        assertEquals("it's", CmsHtml2MarkdownConverter.html2markdown("<p>itʼs</p>"));
        // modifier double prime and ditto mark become a straight double quote
        assertEquals("5\"", CmsHtml2MarkdownConverter.html2markdown("<p>5ʺ</p>"));
        assertEquals("\"", CmsHtml2MarkdownConverter.html2markdown("<p>〃</p>"));
        // fraction and division slashes become a slash
        assertEquals("1/2", CmsHtml2MarkdownConverter.html2markdown("<p>1⁄2</p>"));
        assertEquals("a/b", CmsHtml2MarkdownConverter.html2markdown("<p>a∕b</p>"));
    }

    /**
     * Tests the removeLinks option, which renders the link text but drops the link markup.<p>
     */
    @Test
    public void testRemoveLinks() {

        String html = "<p>See <a href=\"/x\" title=\"T\">the docs</a> now</p>";
        assertEquals("See [the docs](/x \"T\") now", CmsHtml2MarkdownConverter.html2markdown(html));
        assertEquals("See the docs now", CmsHtml2MarkdownConverter.html2markdown(html, false, true));
    }

    /**
     * Tests that script and style elements are dropped entirely rather than rendered as text, for
     * example ld+json structured data carrying an image copyright notice.<p>
     */
    @Test
    public void testScriptAndStyleDropped() {

        String html = "<p>Visible</p>"
            + "<script type=\"application/ld+json\">{\"copyrightNotice\":\"(c) Someone\"}</script>"
            + "<style>.x{color:red}</style>";
        assertEquals("Visible", CmsHtml2MarkdownConverter.html2markdown(html));
    }

    /**
     * Tests that blocks are separated by a single blank line, with no extra blank lines.<p>
     */
    @Test
    public void testSingleBlankLineBetweenBlocks() {

        assertEquals("A\n\nB\n\nC", CmsHtml2MarkdownConverter.html2markdown("<div><p>A</p><p>B</p><p>C</p></div>"));
    }

    /**
     * Tests that non-breaking spaces and other special or invisible characters are normalized.<p>
     */
    @Test
    public void testSpecialWhitespace() {

        // non-breaking space (&nbsp;, U+00A0) collapses to a regular space
        assertEquals("Open Source", CmsHtml2MarkdownConverter.html2markdown("<p>Open&nbsp;Source</p>"));
        // a zero-width space (U+200B) is dropped
        assertEquals("OpenSource", CmsHtml2MarkdownConverter.html2markdown("<p>Open\u200bSource</p>"));
        // a soft hyphen (U+00AD) is dropped
        assertEquals("ab", CmsHtml2MarkdownConverter.html2markdown("<p>a\u00adb</p>"));
    }

    /**
     * Tests folding of the wider set of NLM symbol-to-ASCII mappings.<p>
     */
    @Test
    public void testSymbolFolding() {

        // comparison operators
        assertEquals("<=", CmsHtml2MarkdownConverter.html2markdown("<p>" + (char)0x2264 + "</p>"));
        assertEquals(">=", CmsHtml2MarkdownConverter.html2markdown("<p>" + (char)0x2265 + "</p>"));
        // angle brackets and single angle quotes
        assertEquals("<", CmsHtml2MarkdownConverter.html2markdown("<p>" + (char)0x3008 + "</p>"));
        assertEquals(">", CmsHtml2MarkdownConverter.html2markdown("<p>" + (char)0x3009 + "</p>"));
        assertEquals("<", CmsHtml2MarkdownConverter.html2markdown("<p>" + (char)0x2039 + "</p>"));
        assertEquals(">", CmsHtml2MarkdownConverter.html2markdown("<p>" + (char)0x203A + "</p>"));
        // ratio -> colon, single low-9 quote -> comma, music sharp -> number sign
        assertEquals(":", CmsHtml2MarkdownConverter.html2markdown("<p>" + (char)0x2236 + "</p>"));
        assertEquals(",", CmsHtml2MarkdownConverter.html2markdown("<p>" + (char)0x201A + "</p>"));
        assertEquals("#", CmsHtml2MarkdownConverter.html2markdown("<p>" + (char)0x266F + "</p>"));
        // triple prime, double vertical line, tilde operator, caret
        assertEquals("'''", CmsHtml2MarkdownConverter.html2markdown("<p>" + (char)0x2034 + "</p>"));
        assertEquals("||", CmsHtml2MarkdownConverter.html2markdown("<p>" + (char)0x2016 + "</p>"));
        assertEquals("~", CmsHtml2MarkdownConverter.html2markdown("<p>" + (char)0x223C + "</p>"));
        assertEquals("^", CmsHtml2MarkdownConverter.html2markdown("<p>" + (char)0x2038 + "</p>"));
        // replacements that are themselves Markdown special characters get escaped (* [ backslash)
        assertEquals("\\*", CmsHtml2MarkdownConverter.html2markdown("<p>" + (char)0x2217 + "</p>"));
        assertEquals("\\[", CmsHtml2MarkdownConverter.html2markdown("<p>" + (char)0x27E6 + "</p>"));
        assertEquals("\\\\", CmsHtml2MarkdownConverter.html2markdown("<p>" + (char)0x2216 + "</p>"));
    }

    /**
     * Tests that unknown tags are passed through as their text content.<p>
     */
    @Test
    public void testUnknownTagPassThrough() {

        assertEquals(
            "Hello marked text",
            CmsHtml2MarkdownConverter.html2markdown("<p>Hello <mark>marked</mark> text</p>"));
    }

    /**
     * Tests whitespace collapsing in text content.<p>
     */
    @Test
    public void testWhitespaceCollapse() {

        assertEquals("Hello world", CmsHtml2MarkdownConverter.html2markdown("<p>  Hello   world  </p>"));
    }
}

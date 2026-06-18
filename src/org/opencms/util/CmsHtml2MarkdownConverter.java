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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

/**
 * Converts an HTML fragment to Markdown.<p>
 *
 * This converter is intended for HTML <i>pieces</i> (rich text fragments), not complete HTML pages.
 * It parses the input with Jsoup using {@link Jsoup#parseBodyFragment(String)} and renders a common
 * set of Markdown constructs: headings, paragraphs, bold and italic, inline code, links, images,
 * ordered and unordered (including nested) lists, blockquotes, fenced code blocks and horizontal rules.<p>
 *
 * Tags that are not recognized are passed through, so their textual content is preserved. The main
 * use case is to feed CMS content into LLM prompts in a compact, model friendly form.<p>
 *
 * Note: only a minimal set of Markdown special characters is escaped in text nodes. Perfect round
 * trip escaping (for example of a leading "#", "-" or "&gt;" at the start of a line) is out of scope.
 * GFM tables, task lists and strikethrough are not supported.<p>
 *
 * @since 22.0.0
 */
public class CmsHtml2MarkdownConverter {

    /** The block level tags that are rendered as separate Markdown blocks. */
    private static final Set<String> BLOCK_TAGS = Set.of(
        "h1",
        "h2",
        "h3",
        "h4",
        "h5",
        "h6",
        "p",
        "div",
        "section",
        "article",
        "header",
        "footer",
        "figure",
        "figcaption",
        "blockquote",
        "ul",
        "ol",
        "pre",
        "hr",
        "table");

    /** The Markdown special characters that are escaped in text nodes. */
    private static final Set<Character> ESCAPE_CHARS = Set.of(
        Character.valueOf('\\'),
        Character.valueOf('`'),
        Character.valueOf('*'),
        Character.valueOf('_'),
        Character.valueOf('['),
        Character.valueOf(']'));

    /** Punctuation that attaches to the preceding word, so no separating space is inserted before it. */
    private static final String ATTACHING_PUNCTUATION = ".,:;!?)]}";

    /** Maps typographic symbols and punctuation to their plain ASCII replacement (keyed by code point). */
    private static final Map<Character, String> SYMBOL_FOLD = createSymbolFold();

    /** Whether images and their related markup are dropped from the output. */
    private final boolean m_ignoreImages;

    /** Whether links are rendered as their text only, dropping the link markup. */
    private final boolean m_removeLinks;

    /**
     * Creates a new converter that keeps images and links untouched.<p>
     */
    public CmsHtml2MarkdownConverter() {

        this(false, false);
    }

    /**
     * Creates a new converter.<p>
     *
     * @param ignoreImages if <code>true</code>, images and their related markup are dropped from the output
     * @param removeLinks if <code>true</code>, links are rendered as their text only, dropping the link markup
     */
    public CmsHtml2MarkdownConverter(boolean ignoreImages, boolean removeLinks) {

        m_ignoreImages = ignoreImages;
        m_removeLinks = removeLinks;
    }

    /**
     * Converts the given HTML fragment to Markdown.<p>
     *
     * @param html the HTML fragment to convert
     *
     * @return the Markdown representation of the given HTML fragment
     */
    public static String html2markdown(String html) {

        return html2markdown(html, false, false);
    }

    /**
     * Converts the given HTML fragment to Markdown.<p>
     *
     * @param html the HTML fragment to convert
     * @param ignoreImages if <code>true</code>, images and their related markup are dropped from the output
     * @param removeLinks if <code>true</code>, links are rendered as their text only, dropping the link markup
     *
     * @return the Markdown representation of the given HTML fragment
     */
    public static String html2markdown(String html, boolean ignoreImages, boolean removeLinks) {

        return new CmsHtml2MarkdownConverter(ignoreImages, removeLinks).convert(html);
    }

    /**
     * Builds the symbol-to-ASCII fold map following the NLM Lexical Tools mapSymbolToAscii table.<p>
     *
     * Invisible characters (soft hyphen, zero width spaces, word joiner, byte order mark) are not folded
     * here; they are removed earlier in <code>{@link #normalizeWhitespace(String)}</code>.<p>
     *
     * @return the symbol-to-ASCII fold map
     */
    private static Map<Character, String> createSymbolFold() {

        Map<Character, String> map = new HashMap<Character, String>(128);
        putFold(map, "-", 0x2010, 0x2011, 0x2012, 0x2013, 0x2014, 0x2212);
        putFold(map, "--", 0x2015);
        putFold(
            map,
            Character.toString('"'),
            0x00AB,
            0x00BB,
            0x02BA,
            0x030B,
            0x030E,
            0x201C,
            0x201D,
            0x201E,
            0x201F,
            0x2033,
            0x2036,
            0x3003,
            0x301D,
            0x301E);
        putFold(map, "'", 0x00B4, 0x02B9, 0x02BC, 0x02C8, 0x0301, 0x2018, 0x2019, 0x201B, 0x2032);
        putFold(map, "'''", 0x2034, 0x2037);
        putFold(map, "`", 0x02CB, 0x0300, 0x2035);
        putFold(map, "_", 0x02CD, 0x0331, 0x0332, 0x2017);
        putFold(map, "~", 0x02DC, 0x0303, 0x2053, 0x223C, 0x301C);
        putFold(map, "^", 0x02C4, 0x02C6, 0x0302, 0x2038, 0x2303);
        putFold(map, "/", 0x00F7, 0x0338, 0x2044, 0x2215);
        putFold(map, Character.toString('\\'), 0x20E5, 0x2216);
        putFold(map, "|", 0x01C0, 0x05C0, 0x2223, 0x2758);
        putFold(map, "||", 0x2016);
        putFold(map, "!", 0x01C3, 0x2762);
        putFold(map, ":", 0x0589, 0x05C3, 0x2236);
        putFold(map, "%", 0x066A, 0x2052);
        putFold(map, "*", 0x066D, 0x204E, 0x2217, 0x2731);
        putFold(map, "<", 0x2039, 0x2329, 0x27E8, 0x3008);
        putFold(map, ">", 0x203A, 0x232A, 0x27E9, 0x3009);
        putFold(map, "<=", 0x2264, 0x2266);
        putFold(map, ">=", 0x2265, 0x2267);
        putFold(map, "[", 0x27E6);
        putFold(map, "]", 0x301B);
        putFold(map, "{", 0x2983);
        putFold(map, "}", 0x2984);
        putFold(map, "#", 0x266F);
        putFold(map, "?", 0x203D);
        putFold(map, ",", 0x201A);
        putFold(map, "...", 0x2026);
        return map;
    }

    /**
     * Detects the code language from a "language-xxx" CSS class, if present.<p>
     *
     * @param element the element to inspect
     *
     * @return the detected language, or the empty String if none was found
     */
    private static String detectLanguage(Element element) {

        for (String name : element.classNames()) {
            if (name.startsWith("language-")) {
                return name.substring("language-".length());
            }
        }
        return "";
    }

    /**
     * Returns whether the given buffer ends with a whitespace character.<p>
     *
     * @param buffer the buffer to check
     *
     * @return <code>true</code> if the buffer ends with whitespace
     */
    private static boolean endsWithWhitespace(StringBuilder buffer) {

        return (buffer.length() > 0) && Character.isWhitespace(buffer.charAt(buffer.length() - 1));
    }

    /**
     * Backslash escapes a minimal set of Markdown special characters in the given text.<p>
     *
     * @param text the text to escape
     *
     * @return the escaped text
     */
    private static String escape(String text) {

        StringBuilder result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (ESCAPE_CHARS.contains(Character.valueOf(c))) {
                result.append('\\');
            }
            result.append(c);
        }
        return result.toString();
    }

    /**
     * Appends the given inline buffer as a trimmed block to the given list, then resets the buffer.<p>
     *
     * @param inline the inline buffer
     * @param blocks the list of blocks to append to
     */
    private static void flushInline(StringBuilder inline, List<String> blocks) {

        String text = inline.toString().trim();
        if (!text.isEmpty()) {
            blocks.add(text);
        }
        inline.setLength(0);
    }

    /**
     * Folds well known typographic symbols and punctuation to plain ASCII, following the NLM Lexical
     * Tools symbol-to-ASCII mapping table (mapSymbolToAscii).<p>
     *
     * Dashes and the minus sign become a hyphen; typographic, angle and prime quotes become straight
     * quotes or apostrophes; the ellipsis becomes three dots; and fancy slashes, bars, brackets,
     * asterisks, carets, tildes, accents and comparison signs become their ASCII counterparts. Where a
     * replacement is itself a Markdown special character it is escaped afterwards by
     * <code>{@link #escape(String)}</code>.<p>
     *
     * @param text the text to fold
     *
     * @return the text with typographic symbols replaced by ASCII equivalents
     */
    private static String foldPunctuation(String text) {

        StringBuilder result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String replacement = SYMBOL_FOLD.get(Character.valueOf(c));
            if (replacement != null) {
                result.append(replacement);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Returns whether the given node must be rendered as a separate Markdown block.<p>
     *
     * A node is a block when its tag is a block level tag, or when it is an otherwise inline element
     * (for example a link wrapping a whole card) that contains block level content; in the latter
     * case it is unwrapped and rendered as blocks, so the wrapped headings and paragraphs keep their
     * structure instead of being flattened to inline text.<p>
     *
     * @param node the node to check
     *
     * @return <code>true</code> if the node must be rendered as a block
     */
    private static boolean isBlock(Node node) {

        if (!(node instanceof Element)) {
            return false;
        }
        Element element = (Element)node;
        if (BLOCK_TAGS.contains(element.tagName())) {
            return true;
        }
        for (Node child : element.childNodes()) {
            if (isBlock(child)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the given element is an ordered or unordered list.<p>
     *
     * @param element the element to check
     *
     * @return <code>true</code> if the element is a ul or ol
     */
    private static boolean isList(Element element) {

        return "ul".equals(element.tagName()) || "ol".equals(element.tagName());
    }

    /**
     * Returns whether the given node is a structural or empty element whose boundary with adjacent
     * content should be separated by a space. Text nodes and inline markup elements (bold, italic,
     * inline code, links, line breaks, images and dropped script / style) are not separating, so they
     * keep the whitespace as it is in the source.<p>
     *
     * @param node the node to check
     *
     * @return <code>true</code> if the node's boundary should get a separating space
     */
    private static boolean isSeparating(Node node) {

        if (!(node instanceof Element)) {
            return false;
        }
        switch (((Element)node).tagName()) {
            case "a":
            case "b":
            case "br":
            case "code":
            case "em":
            case "i":
            case "img":
            case "script":
            case "strong":
            case "style":
                return false;
            default:
                return true;
        }
    }

    /**
     * Normalizes the whitespace in the given text for Markdown output.<p>
     *
     * Zero width and invisible formatting characters (soft hyphen, zero width spaces, word joiner,
     * byte order mark) are dropped; runs of whitespace, including non breaking and other Unicode
     * spaces, are collapsed to a single regular space (a single leading and trailing space is kept).<p>
     *
     * @param text the text to normalize
     *
     * @return the normalized text
     */
    private static String normalizeWhitespace(String text) {

        // drop zero width and invisible formatting characters (soft hyphen, ZWSP, ZWNJ, ZWJ, word joiner, BOM)
        String cleaned = text.replaceAll("[\\u00AD\\u200B\\u200C\\u200D\\u2060\\uFEFF]", "");
        // collapse whitespace, including non breaking and other Unicode spaces, to a single regular space
        return cleaned.replaceAll("[\\s\\u0085\\u00A0\\u1680\\u2000-\\u200A\\u2028\\u2029\\u202F\\u205F\\u3000]+", " ");
    }

    /**
     * Puts a fold mapping for each of the given code points into the given map.<p>
     *
     * @param map the map to fill
     * @param replacement the ASCII replacement string
     * @param codePoints the source code points that map to the replacement
     */
    private static void putFold(Map<Character, String> map, String replacement, int... codePoints) {

        for (int codePoint : codePoints) {
            map.put(Character.valueOf((char)codePoint), replacement);
        }
    }

    /**
     * Renders an inline code element.<p>
     *
     * @param element the code element
     *
     * @return the rendered inline code, or the empty String if there is no content
     */
    private static String renderInlineCode(Element element) {

        String code = element.text();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(code)) {
            return "";
        }
        return "`" + code + "`";
    }

    /**
     * Renders a pre element as a fenced Markdown code block.<p>
     *
     * @param element the pre element
     *
     * @return the rendered fenced code block
     */
    private static String renderPre(Element element) {

        Element codeElement = element.selectFirst("code");
        String language = detectLanguage(codeElement != null ? codeElement : element);
        String code = (codeElement != null ? codeElement : element).wholeText();
        // strip a trailing line break to avoid an empty line before the closing fence
        code = code.replaceAll("\\n+$", "");
        return "```" + language + "\n" + code + "\n```";
    }

    /**
     * Returns whether the given text starts with a punctuation character that attaches to the
     * preceding word (see {@link #ATTACHING_PUNCTUATION}), so no separating space should precede it.<p>
     *
     * @param text the text to check
     *
     * @return <code>true</code> if the text starts with attaching punctuation
     */
    private static boolean startsWithPunctuation(String text) {

        return !text.isEmpty() && (ATTACHING_PUNCTUATION.indexOf(text.charAt(0)) >= 0);
    }

    /**
     * Returns whether the given text starts with a whitespace character.<p>
     *
     * @param text the text to check
     *
     * @return <code>true</code> if the text starts with whitespace
     */
    private static boolean startsWithWhitespace(String text) {

        return !text.isEmpty() && Character.isWhitespace(text.charAt(0));
    }

    /**
     * Wraps the given inline content with a Markdown marker, keeping leading and trailing
     * whitespace outside the marker so the markup renders correctly.<p>
     *
     * @param content the inline content
     * @param marker the marker to wrap with (for example "**")
     *
     * @return the wrapped content
     */
    private static String wrap(String content, String marker) {

        String core = content.trim();
        if (core.isEmpty()) {
            return content;
        }
        int start = content.indexOf(core);
        String leading = content.substring(0, start);
        String trailing = content.substring(start + core.length());
        return leading + marker + core + marker + trailing;
    }

    /**
     * Converts the given HTML fragment to Markdown.<p>
     *
     * @param html the HTML fragment to convert
     *
     * @return the Markdown representation of the given HTML fragment
     */
    public String convert(String html) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(html)) {
            return "";
        }
        Document doc = Jsoup.parseBodyFragment(html);
        StringBuilder result = new StringBuilder();
        for (String block : renderBlocks(doc.body())) {
            // strip each block and drop blank ones, so a stray empty or trailing-newline block can
            // never produce more than a single blank line between blocks (code block content, which
            // starts and ends with a fence, is unaffected by the strip)
            String trimmed = block.strip();
            if (!trimmed.isEmpty()) {
                if (result.length() > 0) {
                    result.append("\n\n");
                }
                result.append(trimmed);
            }
        }
        return result.toString();
    }

    /**
     * Appends the inline rendering of a child node to the buffer, inserting a single separating space
     * at the boundary between two structural or empty elements when none is present, so content that
     * relies on CSS for spacing (for example list teaser tiles) does not run together. Inline markup
     * (bold, italic, inline code, links, line breaks, images) keeps the source whitespace and is never
     * force separated.<p>
     *
     * @param result the inline buffer
     * @param previous the previously appended child node, or <code>null</code>
     * @param child the child node to append
     */
    private void appendInline(StringBuilder result, Node previous, Node child) {

        String part = renderInline(child);
        if ((result.length() > 0)
            && (isSeparating(previous) || isSeparating(child))
            && !endsWithWhitespace(result)
            && !startsWithWhitespace(part)
            && !startsWithPunctuation(part)) {
            result.append(' ');
        }
        result.append(part);
    }

    /**
     * Renders a single block level element as a list of Markdown blocks.<p>
     *
     * @param element the block level element to render
     *
     * @return the list of Markdown blocks
     */
    private List<String> renderBlock(Element element) {

        String tag = element.tagName();
        if ((tag.length() == 2) && (tag.charAt(0) == 'h') && (tag.charAt(1) >= '1') && (tag.charAt(1) <= '6')) {
            int level = tag.charAt(1) - '0';
            String text = renderInlineChildren(element).replace('\n', ' ').trim();
            return List.of("#".repeat(level) + " " + text);
        }
        switch (tag) {
            case "hr":
                return List.of("---");
            case "pre":
                return List.of(renderPre(element));
            case "blockquote":
                return List.of(renderBlockquote(element));
            case "ul":
                return List.of(renderList(element, false, "").stripTrailing());
            case "ol":
                return List.of(renderList(element, true, "").stripTrailing());
            default:
                // p, div and other (pass through) block containers
                return renderBlocks(element);
        }
    }

    /**
     * Renders a blockquote element, prefixing every produced line with the Markdown quote marker.<p>
     *
     * @param element the blockquote element
     *
     * @return the rendered blockquote block
     */
    private String renderBlockquote(Element element) {

        String inner = String.join("\n\n", renderBlocks(element));
        StringBuilder result = new StringBuilder();
        String[] lines = inner.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                result.append('\n');
            }
            result.append(lines[i].isEmpty() ? ">" : "> " + lines[i]);
        }
        return result.toString();
    }

    /**
     * Renders the child nodes of the given element as a list of Markdown blocks.<p>
     *
     * Inline content is collected into paragraph blocks, block level children are rendered separately.<p>
     *
     * @param element the element whose children to render
     *
     * @return the list of Markdown blocks
     */
    private List<String> renderBlocks(Element element) {

        List<String> blocks = new ArrayList<String>();
        StringBuilder inline = new StringBuilder();
        Node previous = null;
        for (Node node : element.childNodes()) {
            if (isBlock(node)) {
                flushInline(inline, blocks);
                blocks.addAll(renderBlock((Element)node));
                previous = null;
            } else {
                appendInline(inline, previous, node);
                previous = node;
            }
        }
        flushInline(inline, blocks);
        return blocks;
    }

    /**
     * Renders an image element as a Markdown image.<p>
     *
     * @param element the image element
     *
     * @return the rendered Markdown image, or the empty String if there is no src
     */
    private String renderImage(Element element) {

        if (m_ignoreImages) {
            return "";
        }
        String src = element.attr("src").trim();
        if (src.isEmpty()) {
            return "";
        }
        String alt = element.attr("alt").trim();
        String title = element.attr("title").trim();
        String titlePart = title.isEmpty() ? "" : " \"" + title + "\"";
        return "![" + alt + "](" + src + titlePart + ")";
    }

    /**
     * Renders a single node as inline Markdown.<p>
     *
     * @param node the node to render
     *
     * @return the rendered inline Markdown
     */
    private String renderInline(Node node) {

        if (node instanceof TextNode) {
            return escape(foldPunctuation(normalizeWhitespace(((TextNode)node).getWholeText())));
        }
        if (!(node instanceof Element)) {
            return "";
        }
        Element element = (Element)node;
        switch (element.tagName()) {
            case "strong":
            case "b":
                return wrap(renderInlineChildren(element), "**");
            case "em":
            case "i":
                return wrap(renderInlineChildren(element), "*");
            case "code":
                return renderInlineCode(element);
            case "a":
                return renderLink(element);
            case "img":
                return renderImage(element);
            case "br":
                return "\n";
            case "script":
            case "style":
                // non-content markup (for example ld+json structured data carrying an image
                // copyright notice); drop the element and its text content entirely
                return "";
            default:
                // span and any other inline wrapper: render the children only
                return renderInlineChildren(element);
        }
    }

    /**
     * Renders all child nodes of the given element as inline Markdown.<p>
     *
     * @param element the element whose children to render
     *
     * @return the rendered inline Markdown
     */
    private String renderInlineChildren(Element element) {

        StringBuilder result = new StringBuilder();
        Node previous = null;
        for (Node node : element.childNodes()) {
            appendInline(result, previous, node);
            previous = node;
        }
        return result.toString();
    }

    /**
     * Renders an anchor element as a Markdown link.<p>
     *
     * @param element the anchor element
     *
     * @return the rendered Markdown link, or just the inline text if there is no href
     */
    private String renderLink(Element element) {

        String text = renderInlineChildren(element);
        if (m_removeLinks) {
            return text;
        }
        String href = element.attr("href").trim();
        if (href.isEmpty()) {
            return text;
        }
        String core = text.trim();
        if (core.isEmpty()) {
            core = href;
        }
        String title = element.attr("title").trim();
        String titlePart = title.isEmpty() ? "" : " \"" + title + "\"";
        return "[" + core + "](" + href + titlePart + ")";
    }

    /**
     * Renders an ordered or unordered list, including nested lists.<p>
     *
     * @param list the list element (ul or ol)
     * @param ordered if the list is ordered
     * @param indent the indentation prefix for the items of this list
     *
     * @return the rendered list, terminated by a line break
     */
    private String renderList(Element list, boolean ordered, String indent) {

        StringBuilder result = new StringBuilder();
        int counter = 1;
        for (Element item : list.children()) {
            if (!"li".equals(item.tagName())) {
                continue;
            }
            String marker = ordered ? (counter++) + ". " : "- ";
            StringBuilder inline = new StringBuilder();
            List<Element> nested = new ArrayList<Element>();
            Node previous = null;
            for (Node node : item.childNodes()) {
                if ((node instanceof Element) && isList((Element)node)) {
                    nested.add((Element)node);
                } else {
                    appendInline(inline, previous, node);
                    previous = node;
                }
            }
            String text = inline.toString().replace('\n', ' ').trim();
            result.append(indent).append(marker).append(text).append('\n');
            String childIndent = indent + " ".repeat(marker.length());
            for (Element nestedList : nested) {
                result.append(renderList(nestedList, "ol".equals(nestedList.tagName()), childIndent));
            }
        }
        return result.toString();
    }
}

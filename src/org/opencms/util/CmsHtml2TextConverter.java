
package org.opencms.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.Translate;
import org.htmlparser.visitors.NodeVisitor;

/**
 * Extracts the HTML page content.<p>
 */
public class CmsHtml2TextConverter extends NodeVisitor {

    /** The array of supported tag names. */
    private static final String[] TAG_ARRAY = new String[] {
        "H1",
        "H2",
        "H3",
        "H4",
        "H5",
        "H6",
        "P",
        "DIV",
        "SPAN",
        "BR",
        "OL",
        "UL",
        "LI",
        "TABLE",
        "TD",
        "TR",
        "TH",
        "THEAD",
        "TBODY",
        "TFOOT"};

    /** The list of supported tag names. */
    private static final List TAG_LIST = Arrays.asList(TAG_ARRAY);

    /** Indicated to append or store the next line breaks. */
    private boolean m_appendBr;

    /** Map of stored attributes that must bw written to the output when the tag closes. */
    private Map m_attributeMap;

    /** The last appended line break count. */
    private int m_brCount;

    /** The current indentation. */
    private int m_indent;

    /** The current line length. */
    private int m_lineLength;

    /** The marker String (for headlines, bullets etc.). */
    private String m_marker;

    /** The maximum line length. */
    private int m_maxLineLength;

    /** The buffer to write the out to. */
    private StringBuffer m_result;

    /** The last stored, but not appended line break count. */
    private int m_storedBrCount;

    /**
     * Creates a new instance of the html converter.<p>
     */
    public CmsHtml2TextConverter() {

        m_result = new StringBuffer(512);
        m_maxLineLength = 100;
        m_attributeMap = new HashMap(16);
    }

    /**
     * Extracts the text from the given html content, assuming the given html encoding.<p>
     * 
     * @param html the content to extract the plain text from
     * @param encoding the encoding to use
     * 
     * @return the text extracted from the given html content
     * 
     * @throws Exception if something goes wrong
     */
    public static String html2text(String html, String encoding) throws Exception {

        // initialize a parser with the given charset
        Parser parser = new Parser();
        Lexer lexer = new Lexer();
        Page page = new Page(html, encoding);
        lexer.setPage(page);
        parser.setLexer(lexer);
        // create the converter instance
        CmsHtml2TextConverter converter = new CmsHtml2TextConverter();
        parser.visitAllNodesWith(converter);
        // return the result
        return converter.getResult();
    }

    /**
     * Returns the text extraction result.<p>
     * 
     * @return the text extraction result
     */
    public String getResult() {

        return m_result.toString();
    }

    /**
     * @see org.htmlparser.visitors.NodeVisitor#visitEndTag(org.htmlparser.Tag)
     */
    public void visitEndTag(Tag tag) {

        m_appendBr = false;
        appendLinebreaks(tag, false);
        String attribute = (String)m_attributeMap.remove(tag.getParent());
        if (attribute != null) {
            appendText(attribute);
        }
    }

    /**
     * @see org.htmlparser.visitors.NodeVisitor#visitStringNode(org.htmlparser.Text)
     */
    public void visitStringNode(Text text) {

        appendText(text.toPlainTextString());
    }

    /**
     * @see org.htmlparser.visitors.NodeVisitor#visitTag(org.htmlparser.Tag)
     */
    public void visitTag(Tag tag) {

        m_appendBr = true;
        appendLinebreaks(tag, true);

        if (tag.getTagName().equals("IMG")) {
            appendText("#Image#");
        }

        String href = tag.getAttribute("href");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(href)) {
            appendAttribute(tag, " [" + href.trim() + "]");
        }
        String src = tag.getAttribute("src");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(src)) {
            appendAttribute(tag, " [" + src.trim() + "]");
        }
        String title = tag.getAttribute("Title");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(title)) {
            appendAttribute(tag, " {" + title.trim() + "}");
        }
        String alt = tag.getAttribute("Alt");
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(alt)) {
            appendAttribute(tag, " {" + alt.trim() + "}");
        }
    }

    /**
     * Collapse HTML whitespace in the given String.<p>
     * 
     * @param string the string to collapse
     * 
     * @return the input String with all HTML whitespace collapsed
     */
    protected String collapse(String string) {

        int len = string.length();
        StringBuffer result = new StringBuffer(len);
        int state = 0;
        for (int i = 0; i < len; i++) {
            char c = string.charAt(i);
            switch (c) {
                // see HTML specification section 9.1 White space
                // http://www.w3.org/TR/html4/struct/text.html#h-9.1
                case '\u0020':
                case '\u0009':
                case '\u000C':
                case '\u200B':
                case '\r':
                case '\n':
                    if (0 != state) {
                        state = 1;
                    }
                    break;
                default:
                    if (1 == state) {
                        result.append(' ');
                    }
                    state = 2;
                    result.append(c);
            }
        }
        return result.toString();
    }

    private void appendAttribute(Tag tag, String text) {

        if (tag.getTagName().equals("IMG")) {
            appendText(text);
        } else {
            String current = (String)m_attributeMap.get(tag);
            if (current != null) {
                text = current + text;
            }
            m_attributeMap.put(tag, text);
        }
    }

    private void appendIndentation() {

        if (m_lineLength <= m_indent) {
            int len = (m_marker != null) ? m_indent - (m_marker.length() + 1) : m_indent;
            for (int i = 0; i < len; i++) {
                m_result.append(' ');
            }
            if (m_marker != null) {
                m_result.append(m_marker);
                m_result.append(' ');
                m_marker = null;
            }
        }
    }

    private void appendLinebreak(int count) {

        appendLinebreak(count, false);
    }

    private void appendLinebreak(int count, boolean force) {

        if (m_appendBr) {
            if (m_storedBrCount > count) {
                count = m_storedBrCount;
            }
            m_storedBrCount = 0;
            if (force) {
                m_brCount = 0;
            }
            while (m_brCount < count) {
                m_result.append("\r\n");
                m_brCount++;
            }
            m_lineLength = m_indent;
        } else {
            while (m_storedBrCount < count) {
                m_storedBrCount++;
            }
        }
    }

    private void appendLinebreaks(Tag tag, boolean open) {

        String name = tag.getTagName();
        int pos = TAG_LIST.indexOf(name);

        switch (pos) {
            case 0: // H1
                setMarker("=", open);
                setIndentation(2, open);
                appendLinebreak(2);
                break;
            case 1: // H2            
                setMarker("==", open);
                setIndentation(3, open);
                appendLinebreak(2);
                break;
            case 2: // H3
                setMarker("===", open);
                setIndentation(4, open);
                appendLinebreak(2);
                break;
            case 3: // H4
                setMarker("====", open);
                setIndentation(5, open);
                appendLinebreak(2);
                break;
            case 4: // H5
                setMarker("=====", open);
                setIndentation(6, open);
                appendLinebreak(2);
                break;
            case 5: // H6
                setMarker("=======", open);
                setIndentation(7, open);
                appendLinebreak(2);
                break;
            case 6: // P
            case 7: // DIV
                appendLinebreak(2);
                break;
            case 8: // SPAN
                break;
            case 9: // BR
                appendLinebreak(1, true);
                break;
            case 10: // OL
            case 11: // UL
                appendLinebreak(2);
                break;
            case 12: // LI
                setMarker("*", open);
                setIndentation(5, open);
                appendLinebreak(1);
                break;
            case 13: // TABLE
                setIndentation(5, open);
                appendLinebreak(2);
                if (open) {
                    appendLinebreak(1);
                    appendText("-----");
                    appendLinebreak(1);
                }
                break;
            case 14: // TD
                setMarker("--", open);
                appendLinebreak(2);
                break;
            case 15: // TR
                if (!open) {
                    appendLinebreak(1);
                    appendText("-----");
                    appendLinebreak(1);
                }
                break;
            case 16: // TH
            case 17: // THEAD
            case 18: // TBODY
            case 19: // TFOOT
                appendLinebreak(1);
                break;
            default: // unknown tag (ignore)                
        }
    }

    private void appendText(String text) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(text)) {
            text = Translate.decode(text);
            text = collapse(text);
            text = text.trim();
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(text)) {

            if (m_storedBrCount > 0) {
                m_appendBr = true;
                appendLinebreak(m_storedBrCount);
            }
            appendIndentation();
            m_brCount = 0;

            List wordList = CmsStringUtil.splitAsList(text, ' ');
            Iterator i = wordList.iterator();
            while (i.hasNext()) {
                String word = (String)i.next();
                boolean hasNbsp = ((word.charAt(0) == 160) || (word.charAt(word.length() - 1) == 160));
                if ((word.length() + 1 + m_lineLength) > m_maxLineLength) {
                    m_appendBr = true;
                    appendLinebreak(1);
                    appendIndentation();
                    m_brCount = 0;
                } else {
                    if (!hasNbsp
                        && (m_lineLength > m_indent)
                        && (m_result.charAt(m_result.length() - 1) != 160)
                        && (m_result.charAt(m_result.length() - 1) != 32)) {

                        m_result.append(' ');
                        m_lineLength++;
                    }
                }
                m_result.append(word);
                m_lineLength += word.length();
            }
        }
    }

    private void setIndentation(int length, boolean open) {

        if (open) {
            m_indent += length;
        } else {
            m_indent -= length;
            if (m_indent < 0) {
                m_indent = 0;
            }
        }
    }

    private void setMarker(String marker, boolean open) {

        if (open) {
            m_marker = marker;
        }
    }
}
/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/Attic/CmsHtmlExtractor.java,v $
 * Date   : $Date: 2004/11/16 16:57:59 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Derrick Oswald
//
// Revision Control Information
//
// $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/Attic/CmsHtmlExtractor.java,v $
// $Author: c.weinholz $
// $Date: 2004/11/16 16:57:59 $
// $Revision: 1.4 $
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
package org.opencms.search.documents;

import org.opencms.staticexport.CmsLinkProcessor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.htmlparser.Parser;
import org.htmlparser.StringNode;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.Tag;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.Translate;
import org.htmlparser.visitors.NodeVisitor;

/**
 * Extracts plain text from HTML.<p>
 * 
 * @version $Revision: 1.4 $ $Date: 2004/11/16 16:57:59 $
 * @author  Carsten Weinholz (c.weinholz@alkacon.com)
 *
 * @author Derrick Oswald
 * Created on December 23, 2002, 5:01 PM
 */
public class CmsHtmlExtractor extends NodeVisitor {

    /**
     * Property name in event where the URL contents changes.
     */
    public static final String PROP_STRINGS_PROPERTY = "Strings";

    /**
     * Property name in event where the 'embed links' state changes.
     */
    public static final String PROP_LINKS_PROPERTY = "Links";

    /**
     * Property name in event where the URL changes.
     */
    public static final String PROP_URL_PROPERTY = "URL";

    /**
     * Property name in event where the 'replace non-breaking spaces' state changes.
     */
    public static final String PROP_REPLACE_SPACE_PROPERTY = "ReplaceSpace";

    /**
     * Property name in event where the 'collapse whitespace' state changes.
     */
    public static final String PROP_COLLAPSE_PROPERTY = "Collapse";

    /**
     * Property name in event where the connection changes.
     */
    public static final String PROP_CONNECTION_PROPERTY = "Connection";

    /**
     * A newline.
     */
    private static final String newline = System.getProperty ("line.separator");

    /**
     * The length of the newline.
     */
    private static final int newline_size = newline.length ();

    /**
     * The parser used to extract strings.
     */
    private Parser m_parser;

    /**
     * If <code>true</code> the link URLs are embedded in the text output.
     */
    private boolean m_links;

    /**
     * If <code>true</code> regular space characters are substituted for
     * non-breaking spaces in the text output.
     */
    private boolean m_replaceSpace;

    /**
     * If <code>true</code> sequences of whitespace characters are replaced with a
     * single space character.
     */
    private boolean m_collapse;

    /**
     * The buffer text is stored in while traversing the HTML.
     */
    private StringBuffer m_buffer;

    /**
     * Set <code>true</code> when traversing a SCRIPT tag.
     */
    private boolean m_isScript;

    /**
     * Set <code>true</code> when traversing a PRE tag.
     */
    private boolean m_isPre;

    /**
     * Create a StringBean object.
     * Default property values are set to 'do the right thing':
     * <p><code>Links</code> is set <code>false</code> so text appears like a
     * browser would display it, albeit without the colour or underline clues
     * normally associated with a link.</p>
     * <p><code>ReplaceNonBreakingSpaces</code> is set <code>true</code>, so
     * that printing the text works, but the extra information regarding these
     * formatting marks is available if you set it false.</p>
     * <p><code>Collapse</code> is set <code>true</code>, so text appears
     * compact like a browser would display it.</p>
     */
    public CmsHtmlExtractor () {
        super (true, true);
        m_parser = new Parser ();
        m_links = false;
        m_replaceSpace = true;
        m_collapse = true;
    }

    //
    // internals
    //

    /**
     * Appends a newline to the buffer if there isn't one there already.
     * Except if the buffer is empty.
     */
    protected void carriage_return () {
        int length;

        length = m_buffer.length ();
        if ((0 != length) // why bother appending newlines to the beginning of a buffer
            && ((newline_size <= length) // not enough chars to hold a newline
            && (!m_buffer.substring (length - newline_size, length).equals (newline)))) {

            m_buffer.append (newline);
        }
    }

    /**
     * Add the given text collapsing whitespace.
     * Use a little finite state machine:
     * <pre>
     * state 0: whitepace was last emitted character
     * state 1: in whitespace
     * state 2: in word
     * A whitespace character moves us to state 1 and any other character
     * moves us to state 2, except that state 0 stays in state 0 until
     * a non-whitespace and going from whitespace to word we emit a space
     * before the character:
     *    input:     whitespace   other-character
     * state\next
     *    0               0             2
     *    1               1        space then 2
     *    2               1             2
     * </pre>
     * @param buffer The buffer to append to.
     * @param string The string to append.
     */
    protected void collapse (StringBuffer buffer, String string) {
        int chars;
        int length;
        int state;
        char character;

        chars = string.length ();
        if (0 != chars) {
            length = buffer.length ();
            state = ((0 == length)
                        || (buffer.charAt (length - 1) == ' ')
                        || ((newline_size <= length) && buffer.substring (length - newline_size, length).equals (newline))) ? 0 : 1;
            for (int i = 0; i < chars; i++) {
                character = string.charAt (i);
                switch (character) {
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
                        buffer.append (' ');
                    }
                    state = 2;
                    buffer.append (character);
                }
            }
        }
    }

    /**
     * Extract the text from a page.
     *
     * @param content the html content
     * @param encoding the encoding of the content
     *
     * @return The textual contents of the page
     * @throws ParserException if something goes wrong
     */
    public String extractText(String content, String encoding) throws ParserException {

        m_isPre = false;
        m_isScript = false;
        m_buffer = new StringBuffer (4096);

        // we must make sure that the content passed to the parser always is 
        // a "valid" HTML page, i.e. is surrounded by <html><body>...</body></html> 
        // otherwise you will get strange results for some specific HTML constructs
        StringBuffer newContent = new StringBuffer(content.length() + 32);
        
        newContent.append(CmsLinkProcessor.C_HTML_START);
        newContent.append(content);
        newContent.append(CmsLinkProcessor.C_HTML_END);
                
        // create the lexer and parse the input    
        Lexer lexer = new Lexer();
        Page page;
        try {
            // make sure the Lexer uses the right encoding
            InputStream stream = new ByteArrayInputStream(newContent.toString().getBytes(encoding));
            page = new Page(stream, encoding);
        } catch (UnsupportedEncodingException e) {
            // fall back to default encoding, should not happen since all xml pages must have a valid encoding    
            throw new ParserException("Invalid encoding for HTML content parsing '" + encoding + "'");
        }
        lexer.setPage(page);
        m_parser.setLexer(lexer);      
        m_parser.visitAllNodesWith(this);
        
        String result = m_buffer.toString();
        m_buffer = null;
        return result; 
    }

    //
    // Properties
    //

    /**
     * Get the current 'include links' state.
     * @return <code>true</code> if link text is included in the text extracted
     * from the URL, <code>false</code> otherwise.
     */
    public boolean getLinks () {
        return (m_links);
    }

    /**
     * Set the 'include links' state.
     * If the setting is changed after the URL has been set, the text from the
     * URL will be reacquired, which is possibly expensive.
     * @param links Use <code>true</code> if link text is to be included in the
     * text extracted from the URL, <code>false</code> otherwise.
     */
    public void setLinks (boolean links) {
        m_links = links;
    }

    /**
     * Get the current 'replace non breaking spaces' state.
     * @return <code>true</code> if non-breaking spaces (character '&#92;u00a0',
     * numeric character reference &amp;#160; or character entity reference &amp;nbsp;)
     * are to be replaced with normal spaces (character '&#92;u0020').
     */
    public boolean getReplaceNonBreakingSpaces () {
        return (m_replaceSpace);
    }

    /**
     * Set the 'replace non breaking spaces' state.
     * If the setting is changed after the URL has been set, the text from the
     * URL will be reacquired, which is possibly expensive.
     * @param replace_space <code>true</code> if non-breaking spaces (character '&#92;u00a0',
     * numeric character reference &amp;#160; or character entity reference &amp;nbsp;)
     * are to be replaced with normal spaces (character '&#92;u0020').
     */
    public void setReplaceNonBreakingSpaces (boolean replace_space) {
        m_replaceSpace = replace_space;
    }

    /**
     * Get the current 'collapse whitespace' state.
     * If set to <code>true</code> this emulates the operation of browsers
     * in interpretting text where <quote>user agents should collapse input white
     * space sequences when producing output inter-word space</quote>.
     * See HTML specification section 9.1 White space
     * <a href="http://www.w3.org/TR/html4/struct/text.html#h-9.1">
     * http://www.w3.org/TR/html4/struct/text.html#h-9.1</a>.
     * @return <code>true</code> if sequences of whitespace (space '&#92;u0020',
     * tab '&#92;u0009', form feed '&#92;u000C', zero-width space '&#92;u200B',
     * carriage-return '\r' and newline '\n') are to be replaced with a single
     * space.
     */
    public boolean getCollapse () {
        return (m_collapse);
    }

    /**
     * Set the current 'collapse whitespace' state.
     * If the setting is changed after the URL has been set, the text from the
     * URL will be reacquired, which is possibly expensive.
     * @param collapse_whitespace If <code>true</code>, sequences of whitespace
     * will be reduced to a single space.
     */
    public void setCollapse (boolean collapse_whitespace) {
        m_collapse = collapse_whitespace;
    }

    //
    // NodeVisitor overrides
    //

    /**
     * Appends the link as text between angle brackets to the output.
     * @param link The link to process.
     */
    public void visitLinkTag (LinkTag link) {
        if (getLinks ()) {
            m_buffer.append ("<");
            m_buffer.append (link.getLink ());
            m_buffer.append (">");
        }
    }

    /**
     * Appends the text to the output.
     * @param string The text node.
     */
    public void visitStringNode (StringNode string) {
        if (!m_isScript) {
            String text = string.getText ();
            if (!m_isPre) {
                text = Translate.decode (text);
                if (getReplaceNonBreakingSpaces ()) {
                    text = text.replace ('\u00a0', ' ');
                }
                if (getCollapse ()) {
                    collapse (m_buffer, text);
                } else {
                    m_buffer.append (text);
                }
            } else {
                m_buffer.append (text);
            }
        }
    }

    /**
     * Appends a newline to the output if the tag breaks flow, and
     * possibly sets the state of the PRE and SCRIPT flags.
     *
     * @param tag the tag
     */
    public void visitTag (Tag tag) {
        String name;

        name = tag.getTagName ();
        if (name.equalsIgnoreCase ("PRE")) {
            m_isPre = true;
        } else if (name.equalsIgnoreCase ("SCRIPT")) {
            m_isScript = true;
        }
        if (tag.breaksFlow ()) {
            carriage_return ();
        }
    }

    /**
     * Resets the state of the PRE and SCRIPT flags.
     * @param tag The end tag to process.
     */
    public void visitEndTag (Tag tag) {
        String name;

        name = tag.getTagName ();
        if (name.equalsIgnoreCase ("PRE")) {
            m_isPre = false;
        } else if (name.equalsIgnoreCase ("SCRIPT")) {
            m_isScript = false;
        }
    }
}
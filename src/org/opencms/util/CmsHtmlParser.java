/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/CmsHtmlParser.java,v $
 * Date   : $Date: 2007/07/04 16:57:30 $
 * Version: $Revision: 1.4 $
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

import org.opencms.jsp.parse.DivTag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.htmlparser.Parser;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.tags.Div;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

/**
 * Base utility class for OpenCms <code>{@link org.htmlparser.visitors.NodeVisitor}</code>
 * implementations, which provides some often used utility functions.
 * <p>
 * 
 * This base implementation is only a "pass through" class, that is the content is parsed, but the
 * generated result is exactly identical to the input.
 * <p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 6.2.0
 */
public class CmsHtmlParser extends NodeVisitor implements I_CmsHtmlNodeVisitor {

    /** List of upper case tag name strings of tags that should not be auto-corrected if closing divs are missing. */
    private List m_noAutoCloseTags;

    /** The array of supported tag names. */
    // important: don't change the order of these tags in the source, subclasses may expect the tags
    // at the exact indices give here
    // if you want to add tags, add them at the end
    protected static final String[] TAG_ARRAY = new String[] {
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
    protected static final List TAG_LIST = Arrays.asList(TAG_ARRAY);

    /** Indicates if "echo" mode is on, that is all content is written to the result by default. */
    protected boolean m_echo;

    /** The buffer to write the out to. */
    protected StringBuffer m_result;

    /** The providable configuration - never null by contract of interface. */
    private String m_configuration = "";

    /**
     * Creates a new instance of the html converter with echo mode set to <code>false</code>.
     * <p>
     */
    public CmsHtmlParser() {

        this(false);
    }

    /**
     * Creates a new instance of the html converter.
     * <p>
     * 
     * @param echo indicates if "echo" mode is on, that is all content is written to the result
     */
    public CmsHtmlParser(boolean echo) {

        m_result = new StringBuffer(1024);
        m_echo = echo;
        m_noAutoCloseTags = new ArrayList(32);
    }


    /**
     * Internally degrades Composite tags that do have children in the DOM tree 
     * to simple single tags. This allows to avoid auto correction of unclosed HTML tags.<p>
     * 
     * @return A node factory that will not autocorrect open tags specified via <code>{@link #setNoAutoCloseTags(List)}</code>
     */
    private PrototypicalNodeFactory configureNoAutoCorrectionTags() {

        PrototypicalNodeFactory factory = new PrototypicalNodeFactory();

        String tagName;
        Iterator it = m_noAutoCloseTags.iterator();
        Div div = new Div();
        List divNames = Arrays.asList(div.getIds());
        while (it.hasNext()) {
            tagName = ((String)it.next());
            // div
            if (divNames.contains(tagName)) {
                factory.unregisterTag(new Div());
                factory.registerTag(new DivTag());
            }
            // TODO: add more tags for flat parsing / non correction of missing closing tags here
        }
        return factory;
    }

    /**
     * @see org.opencms.util.I_CmsHtmlNodeVisitor#getConfiguration()
     */
    public String getConfiguration() {

        return m_configuration;
    }

    /**
     * @see org.opencms.util.I_CmsHtmlNodeVisitor#getResult()
     */
    public String getResult() {

        return m_result.toString();
    }

    /**
     * Returns the HTML for the given tag itself (not the tag content).
     * <p>
     * 
     * @param tag the tag to create the HTML for
     * 
     * @return the HTML for the given tag
     */
    public String getTagHtml(Tag tag) {

        StringBuffer result = new StringBuffer(32);
        result.append('<');
        result.append(tag.getText());
        result.append('>');
        return result.toString();
    }

    /**
     * @see org.opencms.util.I_CmsHtmlNodeVisitor#process(java.lang.String, java.lang.String)
     */
    public String process(String html, String encoding) throws ParserException {
        m_result = new StringBuffer();
        Parser parser = new Parser();
        Lexer lexer = new Lexer();

        // initialize the page with the given charset
        Page page = new Page(html, encoding);
        lexer.setPage(page);
        parser.setLexer(lexer);

        if (m_noAutoCloseTags != null && m_noAutoCloseTags.size() > 0) {
            // Degrade Composite tags that do have children in the DOM tree 
            // to simple single tags: This allows to finish this tag with openend HTML tags without the effect 
            // that htmlparser will generate the closing tags. 
            PrototypicalNodeFactory factory = configureNoAutoCorrectionTags();
            lexer.setNodeFactory(factory);
        }

        // process the page using the given visitor
        parser.visitAllNodesWith(this);
        // return the result
        return getResult();
    }

    /**
     * 
     * @see org.opencms.util.I_CmsHtmlNodeVisitor#setConfiguration(java.lang.String)
     */
    public void setConfiguration(String configuration) {

        if (CmsStringUtil.isNotEmpty(configuration)) {
            m_configuration = configuration;
        }

    }

    /**
     * @see org.opencms.util.I_CmsHtmlNodeVisitor#visitEndTag(org.htmlparser.Tag)
     */
    public void visitEndTag(Tag tag) {

        if (m_echo) {
            m_result.append(getTagHtml(tag));
        }
    }

    /**
     * @see org.opencms.util.I_CmsHtmlNodeVisitor#visitRemarkNode(org.htmlparser.Remark)
     */
    public void visitRemarkNode(Remark remark) {

        if (m_echo) {
            m_result.append(remark.toHtml());
        }
    }

    /**
     * @see org.opencms.util.I_CmsHtmlNodeVisitor#visitStringNode(org.htmlparser.Text)
     */
    public void visitStringNode(Text text) {

        if (m_echo) {
            m_result.append(text.getText());
        }
    }

    /**
     * @see org.opencms.util.I_CmsHtmlNodeVisitor#visitTag(org.htmlparser.Tag)
     */
    public void visitTag(Tag tag) {

        if (m_echo) {
            m_result.append(getTagHtml(tag));
        }
    }

    /**
     * Collapse HTML whitespace in the given String.
     * <p>
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

    /**
     * Returns a list of upper case tag names for which parsing / visiting will not correct missing closing tags.<p>
     * 
     * 
     * @return a List of upper case tag names for which parsing / visiting will not correct missing closing tags
     */
    public List getNoAutoCloseTags() {

        return m_noAutoCloseTags;
    }

    /**
     * Sets a list of upper case tag names for which parsing / visiting should not correct missing closing tags.<p> 
     * 
     * @param noAutoCloseTagList a list of upper case tag names for which parsing / visiting 
     *      should not correct missing closing tags to set.
     */
    public void setNoAutoCloseTags(List noAutoCloseTagList) {

        // ensuring upper case
        m_noAutoCloseTags.clear();
        if (noAutoCloseTagList != null) {
            Iterator it = noAutoCloseTagList.iterator();
            while (it.hasNext()) {
                m_noAutoCloseTags.add(((String)it.next()).toUpperCase());
            }
        }
    }
}
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

package org.opencms.util;

import org.opencms.i18n.CmsMessageContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.htmlparser.Parser;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.Tag;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

/**
 * Validates HTML.<p>
 */
public class CmsHtmlValidator extends NodeVisitor {

    /** Void HTML elements that do not need to be closed. */
    private static final Set<String> AUTOCLOSE_TAGS = new HashSet<String>();
    /** Tags to override the HTMLParser composite tags, to avoid automatic closing of unbalanced tags. */
    private static final String[] NO_AUTOCLOSE_TAGS = new String[] {
        "APPLET",
        "BLOCKQUOTE",
        "BODY",
        "LI",
        "UL",
        "OL",
        "DL",
        "DD",
        "DT",
        "DIV",
        "FORM",
        "FRAMESET",
        "HTML",
        "H1",
        "H2",
        "H3",
        "H4",
        "H5",
        "H6",
        "HEAD",
        "LABEL",
        "A",
        "OBJECT",
        "OPTION",
        "P",
        "SCRIPT",
        "NOSCRIPT",
        "SELECT",
        "SPAN",
        "STYLE",
        "TD",
        "TR",
        "TBODY",
        "TFOOT",
        "THEAD",
        "TEXTAREA",
        "TITLE"};

    static {
        Collections.addAll(
            AUTOCLOSE_TAGS,
            "AREA",
            "BASE",
            "BR",
            "COL",
            "EMBED",
            "HR",
            "IMG",
            "INPUT",
            "KEYGEN",
            "LINK",
            "MENUITEM",
            "META",
            "PARAM",
            "SOURCE",
            "TRACK",
            "WBR");
    }

    /** The error messages. */
    private List<CmsMessageContainer> m_messages = new ArrayList<CmsMessageContainer>();

    /** The number of root elements. */
    private int m_rootElementCount;

    /** The stack of opened HTML tags. */
    private Stack<String> m_stack = new Stack<String>();

    /** The number of unbalanced closed tags. */
    private int m_unbalancedClosedTags;

    /** The number of unbalanced opened tags. */
    private int m_unbalancedOpenedTags;

    /**
     * Returns the validation error messages.<p>
     *
     * @return the error messages
     */
    public List<CmsMessageContainer> getMessages() {

        return m_messages;
    }

    /**
     * Returns the number of root elements.<p>
     *
     * @return the number of root elements
     */
    public int getRootElementCount() {

        return m_rootElementCount;
    }

    /**
     * Returns whether the validated HTML is balanced.<p>
     *
     * @return <code>true</code> in case the validated HTML is balanced
     */
    public boolean isBalanced() {

        System.out.println(
            "Unbalanced opened "
                + m_unbalancedOpenedTags
                + " tags, unbalanced closed "
                + m_unbalancedClosedTags
                + " tags.");
        return (m_unbalancedOpenedTags == 0) && (m_unbalancedClosedTags == 0);
    }

    /**
     * Validates the given HTML string.<p>
     *
     * @param html the HTML to validate
     *
     * @throws ParserException in case parsing fails
     */
    public void validate(String html) throws ParserException {

        m_unbalancedClosedTags = 0;
        m_unbalancedOpenedTags = 0;
        m_rootElementCount = 0;
        m_stack.clear();
        m_messages.clear();
        Parser parser = new Parser();
        Lexer lexer = new Lexer();
        Page page = new Page(html, "UTF-8");
        lexer.setPage(page);
        parser.setLexer(lexer);
        // override built in composite tags to skip automatic tag closing
        PrototypicalNodeFactory factory = configureNoAutoCorrectionTags();
        lexer.setNodeFactory(factory);

        parser.visitAllNodesWith(this);
    }

    /**
     * @see org.htmlparser.visitors.NodeVisitor#visitEndTag(org.htmlparser.Tag)
     */
    @Override
    public void visitEndTag(Tag tag) {

        String tagName = tag.getTagName();
        if (tagName.equals(m_stack.peek())) {
            m_stack.pop();
        } else {
            if (m_stack.contains(tagName)) {
                while (!tagName.equals(m_stack.peek())) {
                    String enclosedTag = m_stack.pop();
                    if (AUTOCLOSE_TAGS.contains(enclosedTag)) {
                        System.out.println("Unbalanced void tag " + enclosedTag + ", will be ignored.");
                    } else {
                        System.out.println("Unbalanced opening tag: " + enclosedTag);
                        m_messages.add(Messages.get().container(Messages.ERR_UNBALANCED_OPENING_TAG_1, enclosedTag));
                        m_unbalancedOpenedTags++;
                    }
                }
                m_stack.pop();
            } else {
                System.out.println("Unbalanced closing tag: " + tagName);
                m_messages.add(Messages.get().container(Messages.ERR_UNBALANCED_CLOSING_TAG_1, tagName));
                m_unbalancedClosedTags++;
            }
        }
    }

    /**
     * @see org.htmlparser.visitors.NodeVisitor#visitTag(org.htmlparser.Tag)
     */
    @Override
    public void visitTag(Tag tag) {

        if (m_stack.isEmpty()) {
            m_rootElementCount++;
        }

        if (!tag.isEmptyXmlTag()) {
            m_stack.push(tag.getTagName());
        }
    }

    /**
     * Internally degrades Composite tags that do have children in the DOM tree
     * to simple single tags. This allows to avoid auto correction of unclosed HTML tags.<p>
     *
     * @return A node factory that will not auto correct open tags
     */
    protected PrototypicalNodeFactory configureNoAutoCorrectionTags() {

        PrototypicalNodeFactory factory = new PrototypicalNodeFactory();

        CmsNoAutoCloseTag noAutoCloseTag = new CmsNoAutoCloseTag(NO_AUTOCLOSE_TAGS);
        factory.unregisterTag(noAutoCloseTag);
        factory.registerTag(noAutoCloseTag);
        return factory;
    }
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/CmsHtmlTagRemoveFactory.java,v $
 * Date   : $Date: 2006/03/27 14:52:41 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2006 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.main.CmsLog;

import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;

import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.Tag;
import org.htmlparser.lexer.Page;
import org.htmlparser.scanners.Scanner;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;
import org.htmlparser.visitors.NodeVisitor;

/**
 * 
 * A tag factory for htmlparser that is able to "remove tags".
 * <p>
 * 
 * Create an instance, add the {@link org.htmlparser.Tag} instances to remove and assign this
 * factory to the {@link org.htmlparser.Parser} before starting a visit. A demo usage is shown in
 * {@link org.opencms.workplace.tools.content.CmsTagReplaceParser}.
 * <p>
 * 
 * The tags are not actually removed: They are linked in the document object model tree of the HTML
 * that the parser generates. They just will not accept any {@link NodeVisitor} instances and
 * therefore be invisible in any output a visitor will generate from the visited tree.
 * <p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 6.1.8
 * 
 */
public final class CmsHtmlTagRemoveFactory extends PrototypicalNodeFactory {

    /**
     * 
     * A Tag implementation that will not accept any {@link NodeVisitor} stopping by.
     * <p>
     * 
     * When visiting the corresponding tree of tags, this tag will be there but the visitor will not
     * see it as it is not accepted. This allows "elimination" of this tag in the output the visitor
     * generates from the document object model (e.g. HTML code again).
     * <p>
     * 
     * Potential child tags will be visible to visitors (unless they are instances of this class).
     * <p>
     * 
     * @author Achim Westermann
     * 
     * @version $Revision: 1.2 $
     * 
     * @since 6.1.8
     * 
     */
    private static final class CmsInvisibleTag implements Tag {

        /** Generated serial version UID. */
        private static final long serialVersionUID = -3397880117291165819L;

        /** The real underlying tag. */
        private Tag m_decorated;

        /**
         * Constructor with the delegate to wrap.
         * <p>
         * 
         * Every property is accessed transparently from the delegate, except that visitors are not
         * welcome.
         * <p>
         * 
         * @param delegate the tag to hide.
         */
        private CmsInvisibleTag(Tag delegate) {

            m_decorated = delegate;
        }

        /**
         * @see org.htmlparser.nodes.TagNode#accept(org.htmlparser.visitors.NodeVisitor)
         */
        public void accept(NodeVisitor visitor) {

            // be invisible but show the children (if they like visits)
            NodeList children = m_decorated.getChildren();
            if (children == null) {
                return;
            }
            SimpleNodeIterator itChildren = children.elements();
            while (itChildren.hasMoreNodes()) {
                itChildren.nextNode().accept(visitor);
            }
        }

        /**
         * @see org.htmlparser.Tag#breaksFlow()
         */
        public boolean breaksFlow() {

            return m_decorated.breaksFlow();
        }

        /**
         * @see org.htmlparser.Node#clone()
         */
        public Object clone() throws CloneNotSupportedException {

            return m_decorated.clone();
        }

        /**
         * @see org.htmlparser.Node#collectInto(org.htmlparser.util.NodeList,
         *      org.htmlparser.NodeFilter)
         */
        public void collectInto(NodeList arg0, NodeFilter arg1) {

            m_decorated.collectInto(arg0, arg1);
        }

        /**
         * @see org.htmlparser.Node#doSemanticAction()
         */
        public void doSemanticAction() throws ParserException {

            m_decorated.doSemanticAction();
        }

        /**
         * @see org.htmlparser.Tag#getAttribute(java.lang.String)
         */
        public String getAttribute(String arg0) {

            return m_decorated.getAttribute(arg0);
        }

        /**
         * @see org.htmlparser.Tag#getAttributeEx(java.lang.String)
         */
        public Attribute getAttributeEx(String arg0) {

            return m_decorated.getAttributeEx(arg0);
        }

        /**
         * @deprecated
         * @see org.htmlparser.Tag#getAttributes()
         */
        public Hashtable getAttributes() {

            return m_decorated.getAttributes();
        }

        /**
         * @see org.htmlparser.Tag#getAttributesEx()
         */
        public Vector getAttributesEx() {

            return m_decorated.getAttributesEx();
        }

        /**
         * @see org.htmlparser.Node#getChildren()
         */
        public NodeList getChildren() {

            return m_decorated.getChildren();
        }

        /**
         * @see org.htmlparser.Tag#getEnders()
         */
        public String[] getEnders() {

            return m_decorated.getEnders();
        }

        /**
         * @see org.htmlparser.Tag#getEndingLineNumber()
         */
        public int getEndingLineNumber() {

            return m_decorated.getEndingLineNumber();
        }

        /**
         * @see org.htmlparser.Node#getEndPosition()
         */
        public int getEndPosition() {

            return m_decorated.getEndPosition();
        }

        /**
         * @see org.htmlparser.Tag#getEndTag()
         */
        public Tag getEndTag() {

            return m_decorated.getEndTag();
        }

        /**
         * @see org.htmlparser.Tag#getEndTagEnders()
         */
        public String[] getEndTagEnders() {

            return m_decorated.getEndTagEnders();
        }

        /**
         * @see org.htmlparser.Tag#getIds()
         */
        public String[] getIds() {

            return m_decorated.getIds();
        }

        /**
         * @see org.htmlparser.Node#getPage()
         */
        public Page getPage() {

            return m_decorated.getPage();
        }

        /**
         * @see org.htmlparser.Node#getParent()
         */
        public Node getParent() {

            return m_decorated.getParent();
        }

        /**
         * @see org.htmlparser.Tag#getRawTagName()
         */
        public String getRawTagName() {

            return m_decorated.getRawTagName();
        }

        /**
         * @see org.htmlparser.Tag#getStartingLineNumber()
         */
        public int getStartingLineNumber() {

            return m_decorated.getStartingLineNumber();
        }

        /**
         * @see org.htmlparser.Node#getStartPosition()
         */
        public int getStartPosition() {

            return m_decorated.getStartPosition();
        }

        /**
         * @see org.htmlparser.Tag#getTagName()
         */
        public String getTagName() {

            return m_decorated.getTagName();
        }

        /**
         * @see org.htmlparser.Node#getText()
         */
        public String getText() {

            return m_decorated.getText();
        }

        /**
         * @see org.htmlparser.Tag#getThisScanner()
         */
        public Scanner getThisScanner() {

            return m_decorated.getThisScanner();
        }

        /**
         * @see org.htmlparser.Tag#isEmptyXmlTag()
         */
        public boolean isEmptyXmlTag() {

            return m_decorated.isEmptyXmlTag();
        }

        /**
         * @see org.htmlparser.Tag#isEndTag()
         */
        public boolean isEndTag() {

            return m_decorated.isEndTag();
        }

        /**
         * @see org.htmlparser.Tag#removeAttribute(java.lang.String)
         */
        public void removeAttribute(String arg0) {

            m_decorated.removeAttribute(arg0);
        }

        /**
         * @see org.htmlparser.Tag#setAttribute(java.lang.String, java.lang.String)
         */
        public void setAttribute(String arg0, String arg1) {

            m_decorated.setAttribute(arg0, arg1);
        }

        /**
         * @see org.htmlparser.Tag#setAttribute(java.lang.String, java.lang.String, char)
         */
        public void setAttribute(String arg0, String arg1, char arg2) {

            m_decorated.setAttribute(arg0, arg1, arg2);
        }

        /**
         * @see org.htmlparser.Tag#setAttributeEx(org.htmlparser.Attribute)
         */
        public void setAttributeEx(Attribute arg0) {

            m_decorated.setAttributeEx(arg0);
        }

        /**
         * 
         * @deprecated
         * 
         * @see org.htmlparser.Tag#setAttributes(java.util.Hashtable)
         */
        public void setAttributes(Hashtable arg0) {

            m_decorated.setAttributes(arg0);
        }

        /**
         * @see org.htmlparser.Tag#setAttributesEx(java.util.Vector)
         */
        public void setAttributesEx(Vector arg0) {

            m_decorated.setAttributesEx(arg0);
        }

        /**
         * @see org.htmlparser.Node#setChildren(org.htmlparser.util.NodeList)
         */
        public void setChildren(NodeList arg0) {

            m_decorated.setChildren(arg0);
        }

        /**
         * @see org.htmlparser.Tag#setEmptyXmlTag(boolean)
         */
        public void setEmptyXmlTag(boolean arg0) {

            m_decorated.setEmptyXmlTag(arg0);
        }

        /**
         * @see org.htmlparser.Node#setEndPosition(int)
         */
        public void setEndPosition(int arg0) {

            m_decorated.setEndPosition(arg0);
        }

        /**
         * @see org.htmlparser.Tag#setEndTag(org.htmlparser.Tag)
         */
        public void setEndTag(Tag arg0) {

            m_decorated.setEndTag(arg0);
        }

        /**
         * @see org.htmlparser.Node#setPage(org.htmlparser.lexer.Page)
         */
        public void setPage(Page arg0) {

            m_decorated.setPage(arg0);
        }

        /**
         * @see org.htmlparser.Node#setParent(org.htmlparser.Node)
         */
        public void setParent(Node arg0) {

            m_decorated.setParent(arg0);
        }

        /**
         * @see org.htmlparser.Node#setStartPosition(int)
         */
        public void setStartPosition(int arg0) {

            m_decorated.setStartPosition(arg0);
        }

        /**
         * @see org.htmlparser.Tag#setTagName(java.lang.String)
         */
        public void setTagName(String arg0) {

            m_decorated.setTagName(arg0);
        }

        /**
         * @see org.htmlparser.Node#setText(java.lang.String)
         */
        public void setText(String arg0) {

            m_decorated.setText(arg0);
        }

        /**
         * @see org.htmlparser.Tag#setThisScanner(org.htmlparser.scanners.Scanner)
         */
        public void setThisScanner(Scanner arg0) {

            m_decorated.setThisScanner(arg0);
        }

        /**
         * @see org.htmlparser.Node#toHtml()
         */
        public String toHtml() {

            return m_decorated.toHtml();
        }

        /**
         * @see org.htmlparser.Node#toPlainTextString()
         */
        public String toPlainTextString() {

            return m_decorated.toPlainTextString();
        }

        /**
         * @see org.htmlparser.Node#toString()
         */
        public String toString() {

            return m_decorated.toString();
        }

    }

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsHtmlTagRemoveFactory.class);

    /** Generated serial version UID. */
    private static final long serialVersionUID = 6961158563666656633L;

    /** The tags to hide from the node visitors. */
    private Set m_invisibleTags;

    /**
     * Create a new factory with all tags registered.
     * <p>
     * 
     */
    public CmsHtmlTagRemoveFactory() {

        super();
        m_invisibleTags = new TreeSet();
    }

    /**
     * Add a tag that will be invisible for {@link NodeVisitor} instances.
     * <p>
     * 
     * Not only "this" tag will be invisible but all parsed Tags that have the same name (case
     * insensitive).
     * <p>
     * 
     * @param tag the tag that will be invisible for all {@link NodeVisitor} instances.
     * 
     * @return true if the tag was added to the internal set of tags to remove, false if not (was
     *         contained before, has no name,...).
     */
    public boolean addTagRemoval(Tag tag) {

        boolean result = false;
        String tagName = tag.getTagName();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(tagName)) {
            result = m_invisibleTags.add(tagName.toLowerCase());
        }
        return result;
    }

    /**
     * @see org.htmlparser.PrototypicalNodeFactory#createTagNode(org.htmlparser.lexer.Page, int,
     *      int, java.util.Vector)
     */
    public Tag createTagNode(Page arg0, int arg1, int arg2, Vector arg3) {

        try {
            String tagName = ((Attribute)arg3.get(0)).getName().toLowerCase();
            // end tags have names like "/a"....
            if (tagName.charAt(0) == '/') {
                tagName = tagName.substring(1);
            }
            Tag result = super.createTagNode(arg0, arg1, arg2, arg3);
            if (m_invisibleTags.contains(tagName)) {
                result = new CmsInvisibleTag(result);
            }
            return result;
        } catch (RuntimeException rte) {
            if (LOG.isErrorEnabled()) {
                // log here, as htmlparser 1.5 did swallow exceptions from here and threw NPEs from
                // other places
                LOG.error(rte);
            }
            throw rte;
        }
    }

}

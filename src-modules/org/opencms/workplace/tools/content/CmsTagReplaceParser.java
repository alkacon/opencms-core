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

package org.opencms.workplace.tools.content;

import org.opencms.util.CmsHtmlParser;
import org.opencms.util.CmsHtmlTagRemoveFactory;
import org.opencms.util.CmsStringUtil;

import java.util.Iterator;

import org.htmlparser.NodeFactory;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.ParserException;

/**
 *
 * Html parser / visitor combination that visits a document and replaces Tag names by using the
 * replacement configuration of a {@link org.opencms.workplace.tools.content.CmsTagReplaceSettings}
 * instance.
 * <p>
 *
 * Instances are reusable.
 * <p>
 *
 * @since 6.1.7
 *
 */
public final class CmsTagReplaceParser extends CmsHtmlParser {

    /** A tag factory that is able to make tags invisible to visitors. */
    private final NodeFactory m_nodeFactory;

    /**
     * Boolean flag that is set to true if during last call to {@link #process(String, String)}
     * content was changed.
     */
    private boolean m_changedContent;
    /**
     * The settings to use for replacing tags.
     */
    private final CmsTagReplaceSettings m_settings;

    /**
     * Default constructor that turns echo on and uses the settings for replacing tags.
     * <p>
     *
     * @param settings the settings to use for tag replacement.
     */
    public CmsTagReplaceParser(CmsTagReplaceSettings settings) {

        // echo on
        super(true);
        m_settings = settings;
        CmsHtmlTagRemoveFactory nodeFactory = new CmsHtmlTagRemoveFactory();
        // add the removals of the settings to the tag factory:
        Iterator itDeleteTags = m_settings.getDeleteTags().iterator();
        while (itDeleteTags.hasNext()) {
            nodeFactory.addTagRemoval((Tag)itDeleteTags.next());
        }
        m_nodeFactory = nodeFactory;

    }

    /**
     * Overridden to also return the attributes of the Tag.
     * <p>
     *
     * @see org.opencms.util.CmsHtmlParser#getTagHtml(org.htmlparser.Tag)
     */
    @Override
    public String getTagHtml(Tag tag) {

        if (CmsStringUtil.isEmpty(tag.getTagName())) {
            return "";
        }
        StringBuffer result = new StringBuffer(32);
        result.append('<');
        // Tag name is the first "Attribute"...
        Iterator itAttributes = tag.getAttributesEx().iterator();
        while (itAttributes.hasNext()) {
            result.append(itAttributes.next().toString());
            // avoid trailing whitespaces like <H1 >
            // in 2nd run htmlparser 1.5 would turn the whitespace into an Attribute with null name
            if (itAttributes.hasNext()) {
                result.append(' ');
            }
        }
        result.append('>');
        return result.toString();
    }

    /**
     * Extracts the text from the given html content, assuming the given html encoding.
     * <p>
     * Additionally tags are replaced / removed according to the configuration of this instance.
     * <p>
     *
     * <h3>Please note:</h3>
     * There are static process methods in the superclass that will not do the replacements /
     * removals. Don't mix them up with this method.
     * <p>
     *
     * @param html the content to extract the plain text from.
     *
     * @param encoding the encoding to use.
     *
     * @return the text extracted from the given html content.
     *
     * @throws ParserException if something goes wrong.
     */
    @Override
    public String process(String html, String encoding) throws ParserException {

        // clear from potential previous run:
        m_result = new StringBuffer();
        m_changedContent = false;

        // initialize a parser with the given charset
        Parser parser = new Parser();
        parser.setNodeFactory(m_nodeFactory);
        Lexer lexer = new Lexer();
        Page page = new Page(html, encoding);
        lexer.setPage(page);
        parser.setLexer(lexer);
        // process the page using the given visitor
        parser.visitAllNodesWith(this);
        // return the result
        return getResult();
    }

    /**
     * @see org.opencms.util.CmsHtmlParser#visitEndTag(org.htmlparser.Tag)
     */
    @Override
    public void visitEndTag(Tag tag) {

        boolean change = m_settings.replace(tag);
        if (change) {
            m_changedContent = true;
        }
        super.visitEndTag(tag);
    }

    /**
     * @see org.opencms.util.CmsHtmlParser#visitTag(org.htmlparser.Tag)
     */
    @Override
    public void visitTag(Tag tag) {

        boolean change = m_settings.replace(tag);
        if (change) {
            m_changedContent = true;
        }
        super.visitTag(tag);
    }

    /**
     * Returns the changedContent.
     * <p>
     *
     * @return the changedContent
     */
    public boolean isChangedContent() {

        return m_changedContent;
    }

}

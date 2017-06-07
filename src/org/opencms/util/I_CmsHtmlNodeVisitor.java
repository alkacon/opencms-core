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

import java.util.List;

import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.util.ParserException;

/**
 *
 * Interface for a combination of a visitor of HTML documents along with the hook to start the
 * parser / lexer that triggers the visit.
 * <p>
 *
 *
 *
 * @since 6.1.3
 *
 */
public interface I_CmsHtmlNodeVisitor {

    /**
     * Returns the configuartion String of this visitor or the empty String if was not provided
     * before.
     * <p>
     *
     * @return the configuartion String of this visitor - by this contract never null but an empty
     *         String if not provided.
     *
     * @see #setConfiguration(String)
     */
    String getConfiguration();

    /**
     * Returns the text extraction result.
     * <p>
     *
     * @return the text extraction result
     */
    String getResult();

    /**
     * Extracts the text from the given html content, assuming the given html encoding.
     * <p>
     *
     * @param html the content to extract the plain text from
     * @param encoding the encoding to use
     *
     * @return the text extracted from the given html content
     *
     * @throws ParserException if something goes wrong
     */
    String process(String html, String encoding) throws ParserException;

    /**
     * Set a configuartion String for this visitor.
     * <p>
     *
     * This will most likely be done with data from an xsd, custom jsp tag, ...
     * <p>
     *
     * @param configuration the configuration of this visitor to set.
     */
    void setConfiguration(String configuration);

    /**
     * Sets a list of upper case tag names for which parsing / visitng should not correct missing closing tags.<p>
     *
     * This has to be used before <code>{@link #process(String, String)}</code> is invoked to take an effect.<p>
     *
     * @param noAutoCloseTags a list of upper case tag names for which parsing / visiting
     *      should not correct missing closing tags to set.
     */
    void setNoAutoCloseTags(List<String> noAutoCloseTags);

    /**
     * Visitor method (callback) invoked when a closing Tag is encountered.
     * <p>
     *
     * @param tag the tag that is ended.
     *
     * @see org.htmlparser.visitors.NodeVisitor#visitEndTag(org.htmlparser.Tag)
     */
    void visitEndTag(Tag tag);

    /**
     * Visitor method (callback) invoked when a remark Tag (HTML comment) is encountered.
     * <p>
     *
     * @param remark the remark Tag to visit.
     *
     * @see org.htmlparser.visitors.NodeVisitor#visitRemarkNode(org.htmlparser.Remark)
     */
    void visitRemarkNode(Remark remark);

    /**
     *
     * Visitor method (callback) invoked when a remark Tag (HTML comment) is encountered.
     * <p>
     *
     * @param text the text that is visited.
     *
     * @see org.htmlparser.visitors.NodeVisitor#visitStringNode(org.htmlparser.Text)
     */
    void visitStringNode(Text text);

    /**
     * Visitor method (callback) invoked when a starting Tag (HTML comment) is encountered.
     * <p>
     *
     * @param tag the tag that is visited.
     *
     * @see org.htmlparser.visitors.NodeVisitor#visitTag(org.htmlparser.Tag)
     */
    void visitTag(Tag tag);

}
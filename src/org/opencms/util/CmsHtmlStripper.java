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
import org.opencms.main.CmsLog;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;

import org.htmlparser.Attribute;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.ParserException;

/**
 * Simple html tag stripper that allows configuration of html tag names that are allowed.
 * <p>
 *
 * All tags that are not explicitly allowed via invocation of one of the
 * <code>addPreserve...</code> methods will be missing in the result of the method
 * <code>{@link #stripHtml(String)}</code>.<p>
 *
 * Instances are reusable but not shareable (multithreading). If configuration should be changed
 * between subsequent invocations of <code>{@link #stripHtml(String)}</code> method
 * <code>{@link #reset()}</code> has to be called.
 * <p>
 *
 * @since 6.9.2
 *
 */
public final class CmsHtmlStripper {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsHtmlStripper.class);

    /** A tag factory that is able to make tags invisible to visitors. */
    private CmsHtmlTagRemoveFactory m_nodeFactory;

    /** Flag to control whether tidy is used. */
    private boolean m_useTidy;

    /**
     * Default constructor that turns echo on and uses the settings for replacing tags.
     * <p>
     */
    public CmsHtmlStripper() {

        reset();
    }

    /**
     * Creates an instance with control whether tidy is used.<p>
     *
     * @param useTidy if true tidy will be used
     */
    public CmsHtmlStripper(final boolean useTidy) {

        this();
        m_useTidy = useTidy;
    }

    /**
     * Adds a tag that will be preserved by <code>{@link #stripHtml(String)}</code>.<p>
     *
     * @param tagName the name of the tag to keep (case insensitive)
     *
     * @return true if the tagName was added correctly to the internal engine
     */
    public boolean addPreserveTag(final String tagName) {

        Vector<Attribute> attributeList = new Vector<Attribute>(1);
        Attribute tagNameAttribute = new Attribute();
        tagNameAttribute.setName(tagName.toLowerCase());
        attributeList.add(tagNameAttribute);
        Tag keepTag = m_nodeFactory.createTagNode(null, 0, 0, attributeList);
        boolean result = m_nodeFactory.addTagPreserve(keepTag);
        return result;
    }

    /**
     * Convenience method for adding several tags to preserve.<p>
     *
     * @param preserveTags a <code>List&lt;String&gt;</code> with the case-insensitive tag names of the tags to preserve
     *
     * @see #addPreserveTag(String)
     */
    public void addPreserveTagList(List<String> preserveTags) {

        for (Iterator<String> it = preserveTags.iterator(); it.hasNext();) {
            addPreserveTag(it.next());
        }
    }

    /**
     * Convenience method for adding several tags to preserve
     * in form of a delimiter-separated String.<p>
     *
     * The String will be <code>{@link CmsStringUtil#splitAsList(String, char, boolean)}</code>
     * with <code>tagList</code> as the first argument, <code>separator</code> as the
     * second argument and the third argument set to true (trimming - support).<p>
     *
     * @param tagList a delimiter-separated String with case-insensitive tag names to preserve by
     *      <code>{@link #stripHtml(String)}</code>
     * @param separator the delimiter that separates tag names in the <code>tagList</code> argument
     *
     * @see #addPreserveTag(String)
     */
    public void addPreserveTags(final String tagList, final char separator) {

        List<String> tags = CmsStringUtil.splitAsList(tagList, separator, true);
        addPreserveTagList(tags);
    }

    /**
     * Resets the configuration of the tags to preserve.<p>
     *
     * This is called from the constructor and only has to be called if this
     * instance is reused with a differen configuration (of tags to keep).<p>
     *
     */
    public void reset() {

        m_nodeFactory = new CmsHtmlTagRemoveFactory();
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
     * @return the text extracted from the given html content.
     *
     * @throws ParserException if something goes wrong.
     */
    public String stripHtml(final String html) throws ParserException {

        String content = html;
        if (m_useTidy) {
            content = tidy(content);
        }

        // initialize a parser with the given charset
        Parser parser = new Parser();
        parser.setNodeFactory(m_nodeFactory);
        Lexer lexer = new Lexer();
        Page page = new Page(content);
        lexer.setPage(page);
        parser.setLexer(lexer);
        // process the page using a string collection wizard
        // echo on
        CmsHtmlParser visitor = new CmsHtmlParser(true);
        parser.visitAllNodesWith(visitor);
        // return the result
        return visitor.getResult();
    }

    /**
     * Internally tidies with cleanup and XHTML.<p>
     *
     * @param content HTML to clean
     *
     * @return the tidy HTML
     */
    private String tidy(final String content) {

        CmsHtmlConverter converter = new CmsHtmlConverter(
            CmsEncoder.ENCODING_UTF_8,
            new StringBuffer(CmsHtmlConverter.PARAM_WORD).append(";").append(CmsHtmlConverter.PARAM_XHTML).toString());
        String result = content;
        try {
            result = converter.convertToString(content);
        } catch (UnsupportedEncodingException e) {
            // should never happen
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(Messages.LOG_WARN_TIDY_FAILURE_0), e);
            }
        }
        return result;
    }
}

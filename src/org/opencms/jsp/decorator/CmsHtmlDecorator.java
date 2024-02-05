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

package org.opencms.jsp.decorator;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsHtmlParser;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.util.Translate;

/**
 * The CmsHtmlDecorator is the main object for processing the text decorations.<p>
 *
 * It uses the information of a <code>{@link CmsDecoratorConfiguration}</code> to process the
 * text decorations.
 *
 * @since 6.1.3
 */
public class CmsHtmlDecorator extends CmsHtmlParser {

    /** Delimiters for string seperation. */
    private static final String[] DELIMITERS = {
        " ",
        ",",
        ".",
        ";",
        ":",
        "!",
        "(",
        ")",
        "'",
        "?",
        "/",
        "\u00A7",
        "\"",
        "&nbsp;",
        "&quot;",
        "\r\n",
        "\n"};

    /** Delimiters for second level string separation. */
    private static final String[] DELIMITERS_SECOND_LEVEL = {
        "-",
        "@",
        "/",
        "&frasl;",
        ".",
        ",",
        "(",
        ")",
        "{",
        "}",
        "[",
        "]",
        "\"",
        "&quot;",
        "!",
        "?",
        ";",
        "&",
        "&amp;",
        "%",
        "\u00A7",
        "&sect;"};

    /** Steps for forward lookup in workd list. */
    private static final int FORWARD_LOOKUP = 10;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsHtmlDecorator.class);

    /** Non translators, strings starting with those values must not be translated. */
    private static final String[] NON_TRANSLATORS = {"&nbsp;", "&quot;"};

    /** The decoration configuration.<p> */
    I_CmsDecoratorConfiguration m_config;

    /** Decoration bundle to be used by the decorator. */
    CmsDecorationBundle m_decorations;

    /** the CmsObject. */
    private CmsObject m_cms;

    /** decorate flag. */
    private boolean m_decorate;

    /**
     * Constructor, creates a new, empty CmsHtmlDecorator.<p>
     *
     * @param cms the CmsObject
     * @throws CmsException if something goes wrong
     */
    public CmsHtmlDecorator(CmsObject cms)
    throws CmsException {

        m_config = new CmsDecoratorConfiguration(cms);
        m_decorations = m_config.getDecorations();
        m_result = new StringBuffer(512);
        m_echo = true;
        m_decorate = true;

    }

    /**
     * Constructor, creates a new CmsHtmlDecorator with a given configuration.<p>
     *
     * @param cms the CmsObject
     * @param config the configuration to be used
     *
     */
    public CmsHtmlDecorator(CmsObject cms, I_CmsDecoratorConfiguration config) {

        m_config = config;
        m_decorations = config.getDecorations();
        m_result = new StringBuffer(512);
        m_echo = true;
        m_decorate = true;
        m_cms = cms;
    }

    /**
     * Splits a String into substrings along the provided delimiter list and returns
     * the result as a List of Substrings.<p>
     *
     * @param source the String to split
     * @param delimiters the delimiters to split at
     * @param trim flag to indicate if leading and trailing whitespaces should be omitted
     * @param includeDelimiters flag to indicate if the delimiters should be included as well
     *
     * @return the List of splitted Substrings
     */
    public static List<String> splitAsList(String source, String[] delimiters, boolean trim, boolean includeDelimiters) {

        List<String> result = new ArrayList<String>();
        String delimiter = "";
        int i = 0;
        int l = source.length();
        int n = -1;
        int max = Integer.MAX_VALUE;

        // find the next delimiter
        for (int j = 0; j < delimiters.length; j++) {
            int delimPos = source.indexOf(delimiters[j]);
            if (delimPos > -1) {
                if (delimPos < max) {
                    max = delimPos;
                    n = delimPos;
                    delimiter = delimiters[j];
                }
            }
        }

        while (n != -1) {
            // zero - length items are not seen as tokens at start or end
            if ((i < n) || ((i > 0) && (i < l))) {
                result.add(trim ? source.substring(i, n).trim() : source.substring(i, n));
                // add the delimiter to the list as well
                if (includeDelimiters && ((n + delimiter.length()) <= l)) {
                    result.add(source.substring(n, n + delimiter.length()));
                }
            } else {
                // add the delimiter to the list as well
                if (includeDelimiters && source.startsWith(delimiter)) {
                    result.add(delimiter);
                }
            }
            i = n + delimiter.length();

            // find the next delimiter
            max = Integer.MAX_VALUE;
            n = -1;
            for (int j = 0; j < delimiters.length; j++) {
                int delimPos = source.indexOf(delimiters[j], i);
                if (delimPos > -1) {
                    if (delimPos < max) {
                        max = delimPos;
                        n = delimPos;
                        delimiter = delimiters[j];
                    }
                }
            }

        }
        // is there a non - empty String to cut from the tail?
        if (n < 0) {
            n = source.length();
        }
        if (i < n) {
            result.add(trim ? source.substring(i).trim() : source.substring(i));
        }
        return result;
    }

    /**
     * Processes a HTML string and adds text decorations according to the decoration configuration.<p>
     *
     * @param html a string holding the HTML code that should be added with text decorations
     * @param encoding the encoding to be used
     * @return a HTML string with the decorations added.
     * @throws Exception if something goes wrong
     */
    public String doDecoration(String html, String encoding) throws Exception {

        return process(html, encoding);
    }

    /**
     * Resets the first occurance flags of all decoration objects.<p>
     *
     * This is nescessary if decoration objects should be used for processing more than once.     *
     */
    public void resetDecorationDefinitions() {

        m_config.resetMarkedDecorations();
    }

    /**
     * @see org.htmlparser.visitors.NodeVisitor#visitStringNode(org.htmlparser.Text)
     */
    @Override
    public void visitStringNode(Text text) {

        appendText(text.toPlainTextString(), DELIMITERS, true);
    }

    /**
     * @see org.htmlparser.visitors.NodeVisitor#visitTag(org.htmlparser.Tag)
     */
    @Override
    public void visitTag(Tag tag) {

        super.visitTag(tag);
        // get the tagname
        String tagname = tag.getTagName();
        // this is one of the tags that should not allow decoation
        if (m_config.isExcluded(tagname)) {
            m_decorate = false;
        } else {
            m_decorate = true;
            // check if the tag has one of the exclusd attribute
            if (m_config.isExcludedAttr(tag)) {
                m_decorate = false;
            }
        }

    }

    /**
     * Appends a text decoration to the output.<p>
     *
     * A lookup is made to find a text decoration for each word in the given text.
     * If a text decoration is found, the word will be decorated and added to the output.
     * If no text decoration is found, the word alone will be added to the output.
     *
     * @param text the text to add a text decoration for
     * @param delimiters delimiters for text seperation
     * @param recursive flag for recusrive search
     */
    private void appendText(String text, String[] delimiters, boolean recursive) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_HTML_DECORATOR_APPEND_TEXT_2, m_config, text));
        }

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(text) && m_decorate) {

            // split the input into single words
            List<String> wordList = splitAsList(text, delimiters, false, true);
            int wordCount = wordList.size();
            for (int i = 0; i < wordCount; i++) {
                String word = wordList.get(i);
                boolean alreadyDecorated = false;
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(
                        Messages.LOG_HTML_DECORATOR_PROCESS_WORD_2,
                        word,
                        Boolean.valueOf(mustDecode(word, wordList, i))));
                }

                // test if the word must be decoded
                if (mustDecode(word, wordList, i)) {
                    word = Translate.decode(word);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.LOG_HTML_DECORATOR_DECODED_WORD_1, word));
                    }
                }

                // test if the word is no delimiter
                // try to get a decoration if it is not
                CmsDecorationObject decObj = null;
                CmsDecorationObject wordDecObj = null;
                if (!hasDelimiter(word, delimiters)) {
                    wordDecObj = (CmsDecorationObject)m_decorations.get(word);
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(
                        Messages.LOG_HTML_DECORATOR_DECORATION_FOUND_2,
                        wordDecObj,
                        word));
                }

                // if there is a decoration object for this word, we must do the decoration
                // if not, we must test if the word itself consists of several parts divided by
                // second level delimiters
                //if ((decObj == null)) {
                if (recursive
                    && hasDelimiter(word, DELIMITERS_SECOND_LEVEL)
                    && !startsWithDelimiter(word, DELIMITERS_SECOND_LEVEL)) {
                    // add the following symbol if possible to allow the second level decoration
                    // test to make a forward lookup as well
                    String secondLevel = word;
                    if (i < (wordCount - 1)) {
                        String nextWord = wordList.get(i + 1);
                        if (!nextWord.equals(" ")) {
                            //don't allow HTML entities to be split in the middle during the recursion!
                            String afterNextWord = "";
                            if (i < (wordCount - 2)) {
                                afterNextWord = wordList.get(i + 2);
                            }
                            if (nextWord.contains("&") && afterNextWord.equals(";")) {
                                secondLevel = word + nextWord + ";";
                                i += 2;
                            } else {
                                secondLevel = word + nextWord;
                                i++;
                            }
                        }
                    }
                    // check if the result is modified by any second level decoration
                    int sizeBefore = m_result.length();
                    appendText(secondLevel, DELIMITERS_SECOND_LEVEL, false);
                    if (sizeBefore != m_result.length()) {
                        alreadyDecorated = true;
                    }

                } else {
                    // make a forward lookup to the next elements of the word list to check
                    // if the combination of word and delimiter can be found as a decoration key
                    // an example would be "Dr." wich must be decorated with "Doctor"
                    StringBuffer decKey = new StringBuffer();
                    decKey.append(word);
                    // calculate how much forward looking must be made
                    int forwardLookup = wordList.size() - i - 1;
                    if (forwardLookup > FORWARD_LOOKUP) {
                        forwardLookup = FORWARD_LOOKUP;
                    }
                    if (i < (wordCount - forwardLookup)) {
                        for (int j = 1; j <= forwardLookup; j++) {
                            decKey.append(wordList.get(i + j));
                            decObj = (CmsDecorationObject)m_decorations.get(decKey.toString());
                            if (LOG.isDebugEnabled()) {
                                LOG.debug(Messages.get().getBundle().key(
                                    Messages.LOG_HTML_DECORATOR_DECORATION_FOUND_FWL_3,
                                    decObj,
                                    word,
                                    Integer.valueOf(j)));
                            }
                            if (decObj != null) {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug(Messages.get().getBundle().key(
                                        Messages.LOG_HTML_DECORATOR_DECORATION_APPEND_DECORATION_1,
                                        decObj.getContentDecoration(
                                            m_config,
                                            decKey.toString(),
                                            m_cms.getRequestContext().getLocale().toString())));
                                }
                                // decorate the current word with the following delimiter
                                m_result.append(decObj.getContentDecoration(
                                    m_config,
                                    decKey.toString(),
                                    m_cms.getRequestContext().getLocale().toString()));
                                // important, we must skip the next element of the list
                                i += j;
                                // reset the decObj
                                alreadyDecorated = true;
                                break;
                            }
                        }
                    }
                    if ((decObj == null) && (wordDecObj == null)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(Messages.get().getBundle().key(
                                Messages.LOG_HTML_DECORATOR_DECORATION_APPEND_WORD_1,
                                word));
                        }
                        // no decoration was found, use the word alone
                        m_result.append(word);
                    }
                }
                //} else {
                if ((wordDecObj != null) && !alreadyDecorated) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(
                            Messages.LOG_HTML_DECORATOR_DECORATION_APPEND_DECORATION_1,
                            wordDecObj.getContentDecoration(
                                m_config,
                                word,
                                m_cms.getRequestContext().getLocale().toString())));
                    }
                    // decorate the current word
                    m_result.append(wordDecObj.getContentDecoration(
                        m_config,
                        word,
                        m_cms.getRequestContext().getLocale().toString()));
                }
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_HTML_DECORATOR_DECORATION_APPEND_ORIGINALTEXT_1,
                    text));
            }
            m_result.append(text);
        }
        m_decorate = true;
    }

    /**
     * Checks if a word contains a given delimiter.<p>
     *
     * @param word the word to test
     * @param delimiters array of delimiter strings
     * @return true if the word contains the delimiter, false otherwiese
     */
    private boolean hasDelimiter(String word, String[] delimiters) {

        boolean delim = false;
        for (int i = 0; i < delimiters.length; i++) {
            if (word.indexOf(delimiters[i]) > -1) {
                delim = true;
                break;
            }
        }
        return delim;
    }

    /**
     * Checks if a word must be decoded.<p>
     *
     * The given word is compared to a negative list of words which must not be decoded.<p>
     *
     * @param word the word to test
     * @param wordList the list of words which must not be decoded
     * @param count the count in the list
     *
     * @return true if the word must be decoded, false otherweise
     */
    private boolean mustDecode(String word, List<String> wordList, int count) {

        boolean decode = true;
        String nextWord = null;

        if (count < (wordList.size() - 1)) {
            nextWord = wordList.get(count + 1);
        }
        // test if the current word contains a "&" and the following with a ";"
        // if so, we must not decode the word
        if ((nextWord != null) && (word.indexOf("&") > -1) && nextWord.startsWith(";")) {
            return false;
        } else {
            // now scheck if the word matches one of the non decoder tokens
            for (int i = 0; i < NON_TRANSLATORS.length; i++) {
                if (word.startsWith(NON_TRANSLATORS[i])) {
                    decode = false;
                    break;
                }
            }
        }
        return decode;
    }

    /**
     * Checks if a word starts with a given delimiter.<p>
     *
     * @param word the word to test
     * @param delimiters array of delimiter strings
     * @return true if the word starts with the delimiter, false otherwiese
     */
    private boolean startsWithDelimiter(String word, String[] delimiters) {

        boolean delim = false;
        for (int i = 0; i < delimiters.length; i++) {
            if (word.startsWith(delimiters[i])) {
                delim = true;
                break;
            }
        }
        return delim;
    }

}

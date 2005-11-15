/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/decorator/CmsHtmlDecorator.java,v $
 * Date   : $Date: 2005/11/15 09:42:27 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.jsp.decorator;

import org.opencms.file.CmsObject;
import org.opencms.util.CmsHtmlParser;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.htmlparser.Text;
import org.htmlparser.util.Translate;

/**
 * The CmsHtmlDecorator is the main object for processing the text decorations.<p>
 * 
 * It uses the information of a <code>{@link CmsDecoratorConfiguration}</code> to process the
 * text decorations.
 *
 * @author Michael Emmerich  
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.1.3 
 */
public class CmsHtmlDecorator extends CmsHtmlParser {

    /** Delimiters for string seperation. */
    private static final String[] DELIMITERS = {" ", ",", ".", ";", ":", "!", "(", ")", "'", "&nbsp;"};

    /** Delimiters for second level string seperation. */
    private static final String[] DELIMITERS_SECOND_LEVEL = {"-"};

    /** Non translaotors, those strings must nut be translated. */
    private static final String[] NON_TRANSLATORS = {"&nbsp;"};

    /** The decoration configuration.<p> */
    CmsDecoratorConfiguration m_config;

    /** Decoration bundle to be used by the decorator. */
    CmsDecorationBundle m_decorations;

    /**
     * Constructor, creates a new CmsHtmlDecorator with a given configuration.<p>
     * 
     * @param config the configuration to be used
     */
    public CmsHtmlDecorator(CmsDecoratorConfiguration config) {

        m_config = config;
        m_decorations = config.getDecorations();
        m_result = new StringBuffer(512);
        m_echo = true;
    }

    /**
     * Constructor, creates a new, empty CmsHtmlDecorator.<p>
     * 
     * @param cms the CmsObject
     */
    public CmsHtmlDecorator(CmsObject cms) {

        m_config = new CmsDecoratorConfiguration(cms);
        m_decorations = m_config.getDecorations();
        m_result = new StringBuffer(512);
        m_echo = true;

    }

    /**
     * Processes a HTML string and adds text decorations according to the decoration configuration.<p>
     * 
     * @param html a string holding the HTML code that should be added with text decorations
     * @param config the decoration configuration
     * @param encoding the encoding to be used
     * @return a HTML string with the decorations added.
     * @throws Exception if something goes wrong
     */
    public static String doDecoration(String html, CmsDecoratorConfiguration config, String encoding) throws Exception {

        CmsHtmlDecorator processor = new CmsHtmlDecorator(config);
        // create the converter instance
        return process(html, encoding, processor);
    }

    /**
     * Processes a HTML string and adds text decorations according to the decoration configuration.<p>
     * 
     * @param html a string holding the HTML code that should be added with text decorations
     * @param processor an already configured 
     * @param encoding the encoding to be used
     * @return a HTML string with the decorations added.
     * @throws Exception if something goes wrong
     */
    public static String doDecoration(String html, CmsHtmlDecorator processor, String encoding) throws Exception {

        processor.m_result = new StringBuffer(512);
        // create the converter instance
        return process(html, encoding, processor);
    }

    /**
     * Splits a String into substrings along the provided delimiter list and returns
     * the result as a List of Substrings.<p>
     *
     * @param source the String to split
     * @param delimiters the delimiters to split at
     * @param trim flag to indicate if leading and trailing whitespaces should be omitted
     *
     * @return the List of splitted Substrings
     */
    public static List splitAsList(String source, String[] delimiters, boolean trim) {

        List result = new ArrayList();
        String delimiter = new String();
        int i = 0;
        int l = source.length();
        int n = -1;
        int max = Integer.MAX_VALUE;

        // find the next delimiter
        for (int j = 0; j < delimiters.length; j++) {
            if (source.indexOf(delimiters[j]) > -1) {
                if (source.indexOf(delimiters[j]) < max) {
                    max = source.indexOf(delimiters[j]);
                    n = source.indexOf(delimiters[j]);
                    delimiter = delimiters[j];
                }
            }
        }

        while (n != -1) {
            // zero - length items are not seen as tokens at start or end
            if ((i < n) || (i > 0) && (i < l)) {
                result.add(trim ? source.substring(i, n).trim() : source.substring(i, n));
                // add the delimiter to the list as well
                if (n + delimiter.length() <= l) {
                    result.add(source.substring(n, n + delimiter.length()));
                }
            }
            i = n + delimiter.length();

            // find the next delimiter
            max = Integer.MAX_VALUE;
            n = -1;
            for (int j = 0; j < delimiters.length; j++) {
                if (source.indexOf(delimiters[j], i) > -1) {
                    if (source.indexOf(delimiters[j], i) < max) {
                        max = source.indexOf(delimiters[j], i);
                        n = source.indexOf(delimiters[j], i);
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
    public void visitStringNode(Text text) {

        appendText(text.toPlainTextString(), DELIMITERS, true);
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

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(text)) {

            // split the input into single words
            List wordList = splitAsList(text, delimiters, true);
            Iterator i = wordList.iterator();
            while (i.hasNext()) {
                String word = (String)i.next();

                // test if the word must be decoded
                if (mustDecode(word)) {
                    word = Translate.decode(word);
                }

                // test if the word is no delimiter
                // try to get a decoration if it is not
                CmsDecorationObject decObj = null;
                if (!hasDelimiter(word, delimiters)) {
                    decObj = (CmsDecorationObject)m_decorations.get(word);
                }

                // if there is a decoration obejct for this word, we must do the decoration
                // if not, we must test if the word itself consits of several parts diveded by
                // second level delimiters
                // if no second level delimiters are found, its a word without any decoration at all
                // which can be added to the result directly.
                if (decObj == null) {
                    if (hasDelimiter(word, DELIMITERS_SECOND_LEVEL) && recursive) {
                        appendText(word, DELIMITERS_SECOND_LEVEL, false);
                    } else {
                        m_result.append(word);
                    }
                } else {
                    // decorate the current word
                    m_result.append(decObj.getContentDecoration(m_config));
                }
            }
        } else {
            m_result.append(text);
        }
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
     * The given word is compated to a negative list of words which must not be decoded.
     * @param word the word to test
     * @return true if the word must be decoded, false otherweise
     */
    private boolean mustDecode(String word) {

        boolean decode = true;
        for (int i = 0; i < NON_TRANSLATORS.length; i++) {
            if (word.equals(NON_TRANSLATORS[i])) {
                decode = false;
                break;
            }
        }
        return decode;
    }

}

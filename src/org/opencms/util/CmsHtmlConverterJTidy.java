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

import org.opencms.main.CmsLog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import org.w3c.tidy.Tidy;

/**
 * HTML cleaner and pretty printer using JTidy.<p>
 *
 * Used to clean up HTML code (e.g. remove word tags) and optionally create XHTML from HTML.<p>
 *
 * @since 6.0.0
 */
public class CmsHtmlConverterJTidy extends A_CmsHtmlConverter {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsHtmlConverterJTidy.class);

    /** List of default modes if none were specified explicitly. */
    private static final List<String> MODES_DEFAULT = Collections.unmodifiableList(
        Arrays.asList(new String[] {CmsHtmlConverter.PARAM_ENABLED}));

    /** Regular expression for cleanup. */
    String[] m_cleanupPatterns = {
        "<o:p>.*(\\r\\n)*.*</o:p>",
        "<o:p>.*(\\r\\n)*.*</O:p>",
        "<\\?xml:.*(\\r\\n).*/>",
        "<\\?xml:.*(\\r\\n).*(\\r\\n).*/\\?>",
        "<\\?xml:.*(\\r\\n).*(\\r\\n).*/>",
        "<\\?xml:(.*(\\r\\n)).*/\\?>",
        "<o:SmartTagType.*(\\r\\n)*.*/>",
        "<o:smarttagtype.*(\\r\\n)*.*/>"};

    /** Patterns for cleanup. */
    Pattern[] m_clearStyle;

    /** Regular expressions for paragraph replacements -- additionally remove leading and trailing breaks. */
    String[] m_replaceParagraphPatterns = {
        "</ul>\n<br />",
        "</ol>\n<br />",
        "<p><br />",
        "<p>",
        "<br />(\\s)*&nbsp;(\\s)*</p>",
        "<br /></p>",
        "</p>",
        "^<br />",
        "<br />$"};

    /** Values for paragraph replacements. */
    String[] m_replaceParagraphValues = {"</ul>", "</ol>", "<br />", "<br />", "<br />", "<br />", "<br />", "", ""};

    /** Regular expression for replace. */
    String[] m_replacePatterns = {
        "&#160;",
        "(\\r\\n){2,}",
        "\u2013",
        "(\\n){2,}",
        "\\(\\r\\n<",
        "\\(\\n<",
        "\\(\\r\\n(\\ ){1,}<",
        "\\(\\n(\\ ){1,}<",
        "\\r\\n<span",
        "\\n<span"};

    /** Patterns for replace. */
    Pattern[] m_replaceStyle;

    /** Values for replace. */
    String[] m_replaceValues = {"&nbsp;", "", "&ndash;", "", "(<", "(<", "(<", "(<", "<span", "<span"};

    /** The tidy to use. */
    Tidy m_tidy;

    /** The length of the line separator. */
    private int m_lineSeparatorLength;

    /** Indicates if this converter is enabled or not. */
    private boolean m_modeEnabled;

    /** Indicates if paragraph replacement mode is enabled or not. */
    private boolean m_modeReplaceParagraphs;

    /** Indicates if word cleanup mode is enabled or not. */
    private boolean m_modeWord;

    /** Indicates if XHTML conversion mode is enabled or not. */
    private boolean m_modeXhtml;

    /**
     * Constructor, creates a new CmsHtmlConverterJTidy.<p>
     */
    public CmsHtmlConverterJTidy() {

        super(null, MODES_DEFAULT);
    }

    /**
     * Constructor, creates a new CmsHtmlConverterJTidy.<p>
     *
     * Possible values for the conversion mode are:<ul>
     * <li>{@link CmsHtmlConverter#PARAM_DISABLED}: The conversion is disabled.
     * <li>{@link CmsHtmlConverter#PARAM_ENABLED}: Conversion is enabled without transformation, so HTML is pretty printed only.
     * <li>{@link CmsHtmlConverter#PARAM_XHTML}: Conversion from HTML to XHTML is enabled.
     * <li>{@link CmsHtmlConverter#PARAM_WORD}: Cleanup of word like HTML tags is enabled.
     * <li>{@link CmsHtmlConverter#PARAM_REPLACE_PARAGRAPHS}: Cleanup of paragraphs and leading/trailing line breaks is enabled.
     *
     * </ul>
     *
     * @param encoding the encoding used for the HTML code conversion
     * @param modes the conversion modes to use
     */
    public CmsHtmlConverterJTidy(String encoding, List<String> modes) {

        super(encoding, modes);
    }

    /**
     * Converts the given HTML code according to the settings of this converter.<p>
     *
     * @param htmlInput HTML input stored in a string
     * @return string containing the converted HTML
     *
     * @throws UnsupportedEncodingException if the encoding set for the conversion is not supported
     */
    @Override
    public String convertToString(String htmlInput) throws UnsupportedEncodingException {

        // initialize the modes
        initModes();
        // only do parsing if the mode is not set to disabled
        if (m_modeEnabled) {

            // do a maximum of 10 loops
            int max = m_modeWord ? 10 : 1;
            int count = 0;

            // we may have to do several parsing runs until all tags are removed
            int oldSize = htmlInput.length();
            String workHtml = regExp(htmlInput);
            while (count < max) {
                count++;

                // first add the optional header if in word mode
                if (m_modeWord) {
                    workHtml = adjustHtml(workHtml);
                }
                // now use tidy to parse and format the HTML
                workHtml = parse(workHtml);
                if (m_modeWord) {
                    // cut off the line separator, which is always appended
                    workHtml = workHtml.substring(0, workHtml.length() - m_lineSeparatorLength);
                }

                if (workHtml.length() == oldSize) {
                    // no change in HTML code after last processing loop
                    workHtml = regExp(workHtml);
                    break;
                }
                oldSize = workHtml.length();
                workHtml = regExp(workHtml);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(
                        Messages.LOG_PARSING_RUNS_2,
                        this.getClass().getName(),
                        Integer.valueOf(count)));
            }
            htmlInput = workHtml;
        }

        return htmlInput;
    }

    /**
     * Adjusts the HTML input code in WORD mode if necessary.<p>
     *
     * When in WORD mode, the HTML tag must contain the xmlns:o="urn:schemas-microsoft-com:office:office"
     * attribute, otherwise tide will not remove the WORD tags from the document.
     *
     * @param htmlInput the HTML input
     * @return adjusted HTML input
     */
    private String adjustHtml(String htmlInput) {

        // check if we have some opening and closing HTML tags
        if ((htmlInput.toLowerCase().indexOf("<html>") == -1) && (htmlInput.toLowerCase().indexOf("</html>") == -1)) {
            // add a correct HTML tag for word generated HTML
            StringBuffer tmp = new StringBuffer();
            tmp.append("<html xmlns:o=\"\"><body>");
            tmp.append(htmlInput);
            tmp.append("</body></html>");
            htmlInput = tmp.toString();
        }
        return htmlInput;
    }

    /**
     * Initializes the JTidy modes.<p>
     */
    private void initModes() {

        // set all internal modes to disabled
        m_modeEnabled = false;
        m_modeReplaceParagraphs = false;
        m_modeWord = false;
        m_modeXhtml = false;

        // extract all operation modes
        List<String> modes = getModes();

        // configure the tidy depending on the operation mode
        if (modes.contains(CmsHtmlConverter.PARAM_ENABLED)) {
            m_modeEnabled = true;
        }
        if (modes.contains(CmsHtmlConverter.PARAM_XHTML)) {
            m_modeEnabled = true;
            m_modeXhtml = true;
        }
        if (modes.contains(CmsHtmlConverter.PARAM_WORD)) {
            m_modeEnabled = true;
            m_modeWord = true;
        }
        if (modes.contains(CmsHtmlConverter.PARAM_REPLACE_PARAGRAPHS)) {
            m_modeEnabled = true;
            m_modeReplaceParagraphs = true;
        }

        // get line separator length
        m_lineSeparatorLength = System.getProperty("line.separator").length();

        // we need this only if the conversion is enabled
        if (m_modeEnabled) {

            // create the main tidy object
            m_tidy = new Tidy();

            // set specified word, XHTML conversion settings
            m_tidy.setXHTML(m_modeXhtml);
            m_tidy.setWord2000(m_modeWord);

            // add additional tags
            // those are required to handle word 2002 (and newer) documents
            Properties additionalTags = new Properties();
            additionalTags.put("new-empty-tags", "o:smarttagtype");
            additionalTags.put("new-inline-tags", "o:smarttagtype");
            m_tidy.getConfiguration().addProps(additionalTags);

            // set the default tidy configuration

            // set the tidy encoding
            m_tidy.setInputEncoding(getEncoding());
            m_tidy.setOutputEncoding(getEncoding());

            // disable the tidy meta element in output
            m_tidy.setTidyMark(false);
            // disable clean mode
            m_tidy.setMakeClean(false);
            // enable numeric entities
            m_tidy.setNumEntities(true);
            // create output of the body only
            m_tidy.setPrintBodyOnly(true);
            // disable URI fixing, because it breaks domain names with special characters (IDNs) in links when used in HTML fields
            m_tidy.setFixUri(false);
            // force output creation even if there are tidy errors
            m_tidy.setForceOutput(true);
            // set tidy to quiet mode to prevent output
            m_tidy.setQuiet(true);
            // disable warning output
            m_tidy.setShowWarnings(false);
            // allow comments in the output
            m_tidy.setHideComments(false);
            // set no line break before a <br>
            m_tidy.setBreakBeforeBR(false);
            // don't wrap attribute values
            m_tidy.setWrapAttVals(false);
            // warp lines after 100 chars
            m_tidy.setWraplen(100);
            // no indentation
            m_tidy.setSpaces(0);

            if (m_modeWord) {
                // create the regular expression for cleanup, only used in word clean mode
                m_clearStyle = new Pattern[m_cleanupPatterns.length];
                for (int i = 0; i < m_cleanupPatterns.length; i++) {
                    m_clearStyle[i] = Pattern.compile(m_cleanupPatterns[i]);
                }
            }

            // add paragraph replacement regular expression and values if needed
            if (m_modeReplaceParagraphs) {
                // add the regular expression and values for paragraph replacements
                String[] newPatterns = new String[m_replacePatterns.length + m_replaceParagraphPatterns.length];
                String[] newValues = new String[m_replacePatterns.length + m_replaceParagraphPatterns.length];
                System.arraycopy(m_replacePatterns, 0, newPatterns, 0, m_replacePatterns.length);
                System.arraycopy(
                    m_replaceParagraphPatterns,
                    0,
                    newPatterns,
                    m_replacePatterns.length,
                    m_replaceParagraphPatterns.length);
                System.arraycopy(m_replaceValues, 0, newValues, 0, m_replacePatterns.length);
                System.arraycopy(
                    m_replaceParagraphValues,
                    0,
                    newValues,
                    m_replacePatterns.length,
                    m_replaceParagraphPatterns.length);
                m_replacePatterns = newPatterns;
                m_replaceValues = newValues;
            }

            // create the regular expression for replace
            m_replaceStyle = new Pattern[m_replacePatterns.length];
            for (int i = 0; i < m_replacePatterns.length; i++) {
                m_replaceStyle[i] = Pattern.compile(m_replacePatterns[i]);
            }
        }
    }

    /**
     * Parses a byte array containing HTML code with different parsing modes.<p>
     *
     * @param htmlInput a byte array containing raw HTML code
     *
     * @return parsed and cleared HTML code
     *
     * @throws UnsupportedEncodingException if the encoding set for the conversion is not supported
     */
    private String parse(String htmlInput) throws UnsupportedEncodingException {

        // prepare the streams
        ByteArrayInputStream in = new ByteArrayInputStream(htmlInput.getBytes(getEncoding()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // do the parsing
        m_tidy.parse(in, out);
        // return the result
        byte[] result = out.toByteArray();
        return new String(result, getEncoding());
    }

    /**
     * Parses the htmlInput with regular expressions for cleanup purposes.<p>
     *
     * @param htmlInput the HTML input
     *
     * @return the processed HTML
     */
    private String regExp(String htmlInput) {

        String parsedHtml = htmlInput.trim();

        if (m_modeWord) {
            // process all cleanup regular expressions
            for (int i = 0; i < m_cleanupPatterns.length; i++) {
                parsedHtml = m_clearStyle[i].matcher(parsedHtml).replaceAll("");
            }
        }

        // process all replace regular expressions
        for (int i = 0; i < m_replacePatterns.length; i++) {
            parsedHtml = m_replaceStyle[i].matcher(parsedHtml).replaceAll(m_replaceValues[i]);
        }

        return parsedHtml;
    }
}
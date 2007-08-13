/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/CmsHtmlConverter.java,v $
 * Date   : $Date: 2007/08/13 16:29:56 $
 * Version: $Revision: 1.30 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2007 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import org.w3c.tidy.Tidy;

/**
 * Html cleaner and pretty printer.<p>
 * 
 * Used to clean up html code (e.g. remove word tags) and optionally create xhtml from html.<p>
 *   
 * @author Michael Emmerich 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.30 $ 
 * 
 * @since 6.0.0 
 */
public class CmsHtmlConverter {

    /** Parameter value for disabled mode. **/
    public static final String PARAM_DISABLED = CmsStringUtil.FALSE;

    /** Parameter value for enabled mode. **/
    public static final String PARAM_ENABLED = CmsStringUtil.TRUE;

    /** Parameter value for WORD mode. **/
    public static final String PARAM_WORD = "cleanup";

    /** Parameter value for XHTML mode. **/
    public static final String PARAM_XHTML = "xhtml";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsHtmlConverter.class);

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

    /** The input encoding. */
    String m_encoding;

    /** Regular expression for replace. */
    String[] m_replacePatterns = {
        "&#160;",
        "(\\r\\n){2,}",
        "–",
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

    /** Indicates if word cleanup mode is enabled or not. */
    private boolean m_modeWord;

    /** Indicates if xhtml conversion mode is enabled or not. */
    private boolean m_modeXhtml;

    /**
     * Constructor, creates a new CmsHtmlConverter.<p>
     * 
     * The encoding used by default is {@link CmsEncoder#ENCODING_UTF_8}.<p>
     */
    public CmsHtmlConverter() {

        init(CmsEncoder.ENCODING_UTF_8, PARAM_ENABLED);
    }

    /**
     * Constructor, creates a new CmsHtmlConverter.<p>
     * 
     * Possible values for the conversion mode are:<ul>
     * <li>{@link #PARAM_DISABLED}: The conversion is disabled.
     * <li>{@link #PARAM_ENABLED}: Conversion is enabled without transformation, so html is pretty printed only. 
     * <li>{@link #PARAM_XHTML}: Conversion from html to xhtml is enabled.
     * <li>{@link #PARAM_WORD}: Cleanup of word like html tags is enabled.
     * </ul>
     * Values can be combined with the <code>;</code> separator, so it's possible to convert 
     * to xhtml and clean from word at the same time.<p>
     * 
     * @param encoding the encoding used for the html code conversion
     * @param mode the conversion mode to use
     */
    public CmsHtmlConverter(String encoding, String mode) {

        init(encoding, mode);
    }

    /**
     * Reads the content conversion property of a given resource and returns it's value.<p>
     * 
     * A default value (disabled) is returned if the property could not be read.<p>
     * 
     * @param cms the CmsObject
     * @param resource the resource in the vfs
     * @return the content conversion property value
     */
    public static String getConversionSettings(CmsObject cms, CmsResource resource) {

        // read the content-conversion property
        String contentConversion;
        try {
            String resourceName = cms.getSitePath(resource);
            CmsProperty contentConversionProperty = cms.readPropertyObject(
                resourceName,
                CmsPropertyDefinition.PROPERTY_CONTENT_CONVERSION,
                true);
            contentConversion = contentConversionProperty.getValue(CmsHtmlConverter.PARAM_DISABLED);
        } catch (CmsException e) {
            // if there was an error reading the property, choose a default value
            contentConversion = CmsHtmlConverter.PARAM_DISABLED;
        }
        return contentConversion;
    }

    /**
     * Tests if the content conversion is enabled.<p>
     * 
     * @param conversionMode the content conversion mode string
     * @return ture or false
     */
    public static boolean isConversionEnabled(String conversionMode) {

        boolean value = true;
        if ((conversionMode == null) || (conversionMode.indexOf(PARAM_DISABLED) != -1)) {
            value = false;
        }
        return value;
    }

    /**
     * Converts the given html code according to the settings of this converter.<p>
     * 
     * @param htmlInput html input stored in an array of bytes
     * @return array of bytes contining the converted html
     * 
     * @throws UnsupportedEncodingException if the encoding set for the conversion is not supported
     */
    public byte[] convertToByte(byte[] htmlInput) throws UnsupportedEncodingException {

        if (m_modeEnabled) {
            // only do any processing if the conversion is enabled
            return convertToByte(new String(htmlInput, m_encoding));
        }
        return htmlInput;
    }

    /**
     * Converts the given html code according to the settings of this converter.<p>
     * 
     * @param htmlInput html input stored in a string
     * @return array of bytes contining the converted html
     * 
     * @throws UnsupportedEncodingException if the encoding set for the conversion is not supported
     */
    public byte[] convertToByte(String htmlInput) throws UnsupportedEncodingException {

        return convertToString(htmlInput).getBytes(m_encoding);
    }

    /**
     * Converts the given html code according to the settings of this converter.<p>
     * 
     * If an any error occurs during the conversion process, the original input is returned unmodified.<p>
     * 
     * @param htmlInput html input stored in an array of bytes
     * @return array of bytes contining the converted html
     */
    public byte[] convertToByteSilent(byte[] htmlInput) {

        try {
            return convertToByte(htmlInput);
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(Messages.LOG_CONVERSION_BYTE_FAILED_0), e);
            }
            return htmlInput;
        }
    }

    /**
     * Converts the given html code according to the settings of this converter.<p>
     * 
     * If an any error occurs during the conversion process, the original input is returned unmodified.<p>
     * 
     * @param htmlInput html input stored in a string
     * @return array of bytes contining the converted html
     */
    public byte[] convertToByteSilent(String htmlInput) {

        try {
            return convertToByte(htmlInput.getBytes(m_encoding));
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(Messages.LOG_CONVERSION_BYTE_FAILED_0), e);
            }
            try {
                return htmlInput.getBytes(m_encoding);
            } catch (UnsupportedEncodingException e1) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(Messages.LOG_CONVERSION_BYTE_FAILED_0), e1);
                }
                return htmlInput.getBytes();
            }
        }
    }

    /**
     * Converts the given html code according to the settings of this converter.<p>
     * 
     * @param htmlInput html input stored in an array of bytes
     * @return string contining the converted html
     * 
     * @throws UnsupportedEncodingException if the encoding set for the conversion is not supported
     */
    public String convertToString(byte[] htmlInput) throws UnsupportedEncodingException {

        return convertToString(new String(htmlInput, m_encoding));
    }

    /**
     * Converts the given html code according to the settings of this converter.<p>
     * 
     * @param htmlInput html input stored in a string
     * @return string contining the converted html
     * 
     * @throws UnsupportedEncodingException if the encoding set for the conversion is not supported
     */
    public String convertToString(String htmlInput) throws UnsupportedEncodingException {

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
                // now use tidy to parse and format the html
                workHtml = parse(workHtml, m_encoding);
                if (m_modeWord) {
                    // cut off the line separator, which is always appended
                    workHtml = workHtml.substring(0, workHtml.length() - m_lineSeparatorLength);
                }

                if (workHtml.length() == oldSize) {
                    // no change in html code after last processing loop
                    workHtml = regExp(workHtml);
                    break;
                }
                oldSize = workHtml.length();
                workHtml = regExp(workHtml);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_PARSING_RUNS_2,
                    this.getClass().getName(),
                    new Integer(count)));
            }
            htmlInput = workHtml;
        }

        return htmlInput;
    }

    /**
     * Converts the given html code according to the settings of this converter.<p>
     * 
     * If an any error occurs during the conversion process, the original input is returned unmodified.<p>
     * 
     * @param htmlInput html input stored in an array of bytes
     * 
     * @return string contining the converted html
     */
    public String convertToStringSilent(byte[] htmlInput) {

        try {
            return convertToString(htmlInput);
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(Messages.LOG_CONVERSION_BYTE_FAILED_0), e);
            }
            try {
                return new String(htmlInput, m_encoding);
            } catch (UnsupportedEncodingException e1) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(Messages.LOG_CONVERSION_BYTE_FAILED_0), e1);
                }
                return new String(htmlInput);
            }
        }
    }

    /**
     * Converts the given html code according to the settings of this converter.<p>
     * 
     * If an any error occurs during the conversion process, the original input is returned unmodified.<p>
     * 
     * @param htmlInput html input stored in string 
     * 
     * @return string contining the converted html
     */
    public String convertToStringSilent(String htmlInput) {

        try {
            return convertToString(htmlInput);
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(Messages.LOG_CONVERSION_BYTE_FAILED_0), e);
            }
            return htmlInput;
        }
    }

    /**
     * Returns the encoding used for the html code conversion.<p>
     * 
     * @return the encoding used for the html code conversion
     */
    public String getEncoding() {

        return m_encoding;
    }

    /**
     * Adjusts the html input code in WORD mode if nescessary.<p>
     * 
     * When in WORD mode, the html tag must contain the xmlns:o="urn:schemas-microsoft-com:office:office"
     * attribute, otherwiese tide will not remove the WORD tags from the document.
     * 
     * @param htmlInput the html input
     * @return adjusted html input
     */
    private String adjustHtml(String htmlInput) {

        // check if we have some opening and closing html tags
        if ((htmlInput.toLowerCase().indexOf("<html>") == -1) && (htmlInput.toLowerCase().indexOf("</html>") == -1)) {
            // add a correct <html> tag for word generated html
            StringBuffer tmp = new StringBuffer();
            tmp.append("<html xmlns:o=\"\"><body>");
            tmp.append(htmlInput);
            tmp.append("</body></html>");
            htmlInput = tmp.toString();
        }
        return htmlInput;
    }

    /**
     * Extracts all mode parameters from the mode property value and stores them in a list.<p>
     * 
     * Values must be seperated iwth a semicolon.
     * 
     * @param mode the mode paramter string
     * @return list with all extracted nodes
     */
    private List extractModes(String mode) {

        ArrayList extractedModes = new ArrayList();
        if (mode != null) {
            StringTokenizer extract = new StringTokenizer(mode, ";");
            while (extract.hasMoreTokens()) {
                String tok = extract.nextToken();
                extractedModes.add(tok);
            }
        }
        return extractedModes;
    }

    /**
     * Initializes the CmsHtmlConverter.<p>
     * 
     * @param encoding the encoding used for the html code conversion
     * @param mode the mode parameter to select the operation mode of the converter.
     */
    private void init(String encoding, String mode) {

        // extract all operation mode
        List modes = extractModes(mode);

        // confiugurate the tidy depending on the operation mode
        if (modes.contains(PARAM_ENABLED)) {
            m_modeEnabled = true;
        }
        if (modes.contains(PARAM_XHTML)) {
            m_modeEnabled = true;
            m_modeXhtml = true;
        }
        if (modes.contains(PARAM_WORD)) {
            m_modeEnabled = true;
            m_modeWord = true;
        }

        // set the encoding
        m_encoding = encoding;

        // get line separator length
        m_lineSeparatorLength = System.getProperty("line.separator").length();

        // we need this only if the conversion is enabled
        if (m_modeEnabled) {

            // create the main tidy object
            m_tidy = new Tidy();

            // set specified word, xhtml conversion settings
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
            m_tidy.setInputEncoding(encoding);
            m_tidy.setOutputEncoding(encoding);

            // disable the tidy meta element in output
            m_tidy.setTidyMark(false);
            // disable clean mode
            m_tidy.setMakeClean(false);
            // enable num entities
            m_tidy.setNumEntities(true);
            // create output of the body only
            m_tidy.setPrintBodyOnly(true);
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
            // dont wrap attribute values
            m_tidy.setWrapAttVals(false);
            // warp lines after 100 chars
            m_tidy.setWraplen(100);
            // no indentation
            m_tidy.setSpaces(0);

            if (m_modeWord) {
                // create the regexp for cleanup, only used in word clean mode
                m_clearStyle = new Pattern[m_cleanupPatterns.length];
                for (int i = 0; i < m_cleanupPatterns.length; i++) {
                    m_clearStyle[i] = Pattern.compile(m_cleanupPatterns[i]);
                }
            }

            // create the regexp for replace
            m_replaceStyle = new Pattern[m_replacePatterns.length];
            for (int i = 0; i < m_replacePatterns.length; i++) {
                m_replaceStyle[i] = Pattern.compile(m_replacePatterns[i]);
            }
        }
    }

    /**
     * Parses a byte array containing html code with different parsing modes.<p>
     * 
     * @param htmlInput a byte array containing raw html code
     * @param encoding the  encoding
     * 
     * @return parsed and cleared html code
     * 
     * @throws UnsupportedEncodingException if the encoding set for the conversion is not supported
     */
    private String parse(String htmlInput, String encoding) throws UnsupportedEncodingException {

        // prepare the streams
        ByteArrayInputStream in = new ByteArrayInputStream(htmlInput.getBytes(encoding));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // do the parsing
        m_tidy.parse(in, out);
        // return the result
        byte[] result = out.toByteArray();
        return new String(result, encoding);
    }

    /**
     * Parses the htmlInput with regular expressions for cleanup purposes.<p>
     * 
     * @param htmlInput the html input
     * @return processed html
     */
    private String regExp(String htmlInput) {

        String parsedHtml = htmlInput.trim();

        if (m_modeWord) {
            // process all cleanup regexp
            for (int i = 0; i < m_cleanupPatterns.length; i++) {
                parsedHtml = m_clearStyle[i].matcher(parsedHtml).replaceAll("");
            }
        }

        // process all replace regexp
        for (int i = 0; i < m_replacePatterns.length; i++) {
            parsedHtml = m_replaceStyle[i].matcher(parsedHtml).replaceAll(m_replaceValues[i]);
        }

        return parsedHtml;
    }
}
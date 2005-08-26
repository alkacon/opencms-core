/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/CmsHtmlConverter.java,v $
 * Date   : $Date: 2005/08/26 09:59:41 $
 * Version: $Revision: 1.20 $
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import org.w3c.tidy.Tidy;

/**
 * Html cleaner, used to clean up html code (e.g. remove word tags) and created xhtml output.<p>
 *   
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.20 $ 
 * 
 * @since 6.0.0 
 */
public class CmsHtmlConverter {

    /** param value for disabled mode. **/
    public static final String PARAM_DISABLED = "false";

    /** param value for enabled mode. **/
    public static final String PARAM_ENABLED = "true";

    /** param value for WORD mode. **/
    public static final String PARAM_WORD = "cleanup";

    /** param value for XHTML mode. **/
    public static final String PARAM_XHTML = "xhtml";

    /** constant for disabled mode. */
    static final int MODE_DISABLED = 0;

    /** constant for enabled mode. */
    static final int MODE_ENABLED = 1;

    /** constant for WORD-removal parsing mode. */
    static final int MODE_WORD = 3;

    /** constant for XHTML parsing mode. */
    static final int MODE_XHTML = 2;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsHtmlConverter.class);

    /** regular expression for cleanup. */
    String[] m_cleanupPatterns = {
        "<o:p>.*(\\r\\n)*.*</o:p>",
        "<o:p>.*(\\r\\n)*.*</O:p>",
        "<\\?xml:.*(\\r\\n).*/>",
        "<\\?xml:.*(\\r\\n).*(\\r\\n).*/\\?>",
        "<\\?xml:.*(\\r\\n).*(\\r\\n).*/>",
        "<\\?xml:(.*(\\r\\n)).*/\\?>",
        "<o:SmartTagType.*(\\r\\n)*.*/>",
        "<o:smarttagtype.*(\\r\\n)*.*/>"};

    /** patterns for cleanup. */
    Pattern[] m_clearStyle;

    /** the input encoding. */
    String m_encoding;

    /** the operation mode. */
    List m_mode;

    /** regular expression for replace. */
    String[] m_replacePatterns = {"&#160;",
                                  "\\r\\n\\r\\n"};

    /** patterns for replace. */
    Pattern[] m_replaceStyle;

    /** values for replace. */
    String[] m_replaceValues = {"&nbsp;",
                                ""};

    /** the tidy to use. */
    Tidy m_tidy;

    /**
     * Constructor, creates a new CmsHtmlConverter.<p>
     * 
     */
    public CmsHtmlConverter() {

        m_tidy = new Tidy();
        m_encoding = CmsEncoder.ENCODING_UTF_8;
        init(PARAM_ENABLED);
    }

    /**
     * Constructor, creates a new CmsHtmlConverter.<p>
     * 
     * @param encoding the input encoding
     * @param mode the conversion mode, possible values are MODE_XHTML, MODE_HTML, MODE_WORD
     */
    public CmsHtmlConverter(String encoding, String mode) {

        m_tidy = new Tidy();
        m_encoding = encoding;
        init(mode);
    }

    /**
     * Reads the content conversion property of a given resource and returns its value.<p>
     * 
     * A default value (disabled) is returned if the property could not be read
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
            contentConversion = contentConversionProperty.getValue();
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
        if ((conversionMode == null) || (conversionMode.indexOf(MODE_DISABLED) != -1)) {
            value = false;
        }
        return value;
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
    public String adjustHtml(String htmlInput) {

        // we only have to do an adjustment id we are in WORD mode
        if (m_mode.contains(PARAM_WORD)) {
            // check if we have some opening and closing html tags
            if ((htmlInput.toLowerCase().indexOf("<html>") == -1) && (htmlInput.toLowerCase().indexOf("</html>") == -1)) {
                // add a correct <html> tag for word generated html
                StringBuffer tmp = new StringBuffer();
                tmp.append("<html xmlns:o=\"\"><body>");
                tmp.append(htmlInput);
                tmp.append("</body></html>");
                htmlInput = tmp.toString();
            }
        }
        return htmlInput;
    }

    /**
     * Converts an html code into clean html (or xhtml), depending on the converter settings.<p>
     * 
     * @param htmlInput html input stored in an array of bytes
     * @return array of bytes contining the converted html
     * @throws Exception if something goes wrong
     */
    public byte[] convertToByte(byte[] htmlInput) throws Exception {

        // only do some parsing if the parsing mode is not set to disabled
        if (m_mode.size() > 0 && !m_mode.contains(PARAM_DISABLED)) {

            int loop = 10;
            int count = 0;

            // we have to do several parsing runs until all tags are removed
            int oldSize = htmlInput.length;
            byte[] parsedRun = regExp(new String(htmlInput, m_encoding)).getBytes(m_encoding);

            while (loop > 0) {
                loop--;

                // first add the optional header if in word mode                
                String parsedContent = adjustHtml(new String(parsedRun, m_encoding));

                parsedRun = parse(parsedContent.getBytes(m_encoding), m_encoding);
                if (parsedRun.length == oldSize) {
                   break;
                } else {
                    oldSize = parsedRun.length;
                    count++;
                }
                parsedRun = regExp(new String(parsedRun, m_encoding)).getBytes(m_encoding);
            }
            if (LOG.isInfoEnabled()) {
                LOG.info(Messages.get().key(Messages.LOG_PARSING_RUNS_2, this.getClass().getName(), new Integer(count)));
            }

            return regExp(new String(parsedRun, m_encoding)).getBytes(m_encoding);

        } else {
            // the parsing mode was disabled, so return the oringinal value
            return htmlInput;
        }
    }

    /**
     * Converts an html code into clean html (or xhtml), depending on the converter settings.<p>
     * 
     * @param htmlInput html input stored in a string
     * @return array of bytes contining the converted html
     * @throws Exception if something goes wrong
     */
    public byte[] convertToByte(String htmlInput) throws Exception {

        return convertToByte(htmlInput.getBytes(m_encoding));
    }

    /**
     * Converts an html code into clean html (or xhtml), depending on the converter settings.<p>
     * If an error occurs during the conversion process, the original input is returned.
     * 
     * @param htmlInput html input stored in an array of bytes
     * @return array of bytes contining the converted html
     */
    public byte[] convertToByteSilent(byte[] htmlInput) {

        try {
            return convertToByte(htmlInput);
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().key(Messages.LOG_CONVERSION_BYTE_FAILED_0), e);
            }
            return htmlInput;
        }
    }

    /**
     * Converts an html code into clean html (or xhtml), depending on the converter settings.<p>
     * 
     * @param htmlInput html input stored in a string
     * @return array of bytes contining the converted html
     */
    public byte[] convertToByteSilent(String htmlInput) {

        try {
            return convertToByte(htmlInput.getBytes(m_encoding));
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().key(Messages.LOG_CONVERSION_BYTE_FAILED_0), e);
            }
            try {
                return htmlInput.getBytes(m_encoding);
            } catch (UnsupportedEncodingException e1) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().key(Messages.LOG_CONVERSION_BYTE_FAILED_0), e1);
                }
                return htmlInput.getBytes();
            }
        }
    }

    /**
     * Converts an html code into clean html (or xhtml), depending on the converter settings.<p>
     * 
     * @param htmlInput html input stored in an array of bytes
     * @return string contining the converted html
     * @throws Exception if something goes wrong
     */
    public String convertToString(byte[] htmlInput) throws Exception {

        byte[] result = convertToByte(htmlInput);
        return new String(result, m_encoding);
    }

    /**
     * Converts an html code into clean html (or xhtml), depending on the converter settings.<p>
     * 
     * @param htmlInput html input stored in a string
     * @return string contining the converted html
     * @throws Exception if something goes wrong
     */
    public String convertToString(String htmlInput) throws Exception {

        byte[] result = convertToByte(htmlInput.getBytes(m_encoding));
        return new String(result, m_encoding);
    }

    /**
     * Converts an html code into clean html (or xhtml), depending on the converter settings.<p>
     * If an error occurs during the conversion process, the original input is returned.
     * 
     * @param htmlInput html input stored in an array of bytes
     * @return string contining the converted html
     */
    public String convertToStringSilent(byte[] htmlInput) {

        try {
            return convertToString(htmlInput);
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().key(Messages.LOG_CONVERSION_BYTE_FAILED_0), e);
            }
            try {
                return new String(htmlInput, m_encoding);
            } catch (UnsupportedEncodingException e1) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().key(Messages.LOG_CONVERSION_BYTE_FAILED_0), e1);
                }
                return new String(htmlInput);
            }
        }
    }

    /**
     * Converts an html code into clean html (or xhtml), depending on the converter settings.<p>
     * If an error occurs during the conversion process, the original input is returned.
     * 
     * @param htmlInput html input stored in string 
     * @return string contining the converted html
     */
    public String convertToStringSilent(String htmlInput) {

        try {
            return convertToString(htmlInput);
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().key(Messages.LOG_CONVERSION_BYTE_FAILED_0), e);
            }
            return htmlInput;
        }
    }

    /**
     * Gets the encoding.<p>
     * 
     * @return encoding as string representation
     */
    public String getEncoding() {

        return m_encoding;
    }

    /**
     * Returns a byte array containing the content of server FS file.<p>
     *
     * @param file the name of the file to read
     * @return bytes[] the content of the file
     * @throws Exception if something goes wrong
     */
    public byte[] getFileBytes(File file) throws Exception {

        byte[] buffer = null;
        FileInputStream fileStream = null;
        int charsRead;
        int size;
        try {
            fileStream = new FileInputStream(file);
            charsRead = 0;
            size = new Long(file.length()).intValue();
            buffer = new byte[size];
            while (charsRead < size) {
                charsRead += fileStream.read(buffer, charsRead, size - charsRead);
            }
            return buffer;
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (fileStream != null) {
                    fileStream.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
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
     * @param mode the mode parameter to select the operation mode of the converter. 
     */
    private void init(String mode) {

        // add additional tags
        // those are newscessary to handle word 2002+ documents
        Properties additionalTags = new Properties();
        additionalTags.put("new-empty-tags", "o:smarttagtype");
        additionalTags.put("new-inline-tags", "o:smarttagtype");
        m_tidy.getConfiguration().addProps(additionalTags);

        // set the default tidy configuration

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

        // extract all operation mode
        m_mode = extractModes(mode);

        // confiugurate the tidy depending on the operation mode
        if (m_mode.contains(PARAM_ENABLED)) {
            m_tidy.setXHTML(false);
            m_tidy.setWord2000(false);
        }
        if (m_mode.contains(PARAM_XHTML)) {
            m_tidy.setXHTML(true);
        }
        if (m_mode.contains(PARAM_WORD)) {
            m_tidy.setWord2000(true);
        }

        // create the regexp for cleanup
        m_clearStyle = new Pattern[m_cleanupPatterns.length];
        for (int i = 0; i < m_cleanupPatterns.length; i++) {
            m_clearStyle[i] = Pattern.compile(m_cleanupPatterns[i]);
        }

        // create the regexp for replace
        m_replaceStyle = new Pattern[m_replacePatterns.length];
        for (int i = 0; i < m_replacePatterns.length; i++) {
            m_replaceStyle[i] = Pattern.compile(m_replacePatterns[i]);
        }

    }

    /**
     * Parses a string containing html code with different paring modes.<p>
     * 
     * @param htmlInput a string containing raw html code
     * @param encoding the  encoding
     * @return parsed and cleared html code
     */
    private byte[] parse(byte[] htmlInput, String encoding) {

        byte[] parsedHtml;

        // set the encoding
        m_tidy.setInputEncoding(encoding);
        m_tidy.setOutputEncoding(encoding);

        // prepare the streams
        ByteArrayInputStream in = new ByteArrayInputStream(htmlInput);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // do the parsing
        m_tidy.parse(in, out);

        parsedHtml = out.toByteArray();

        return parsedHtml;
    }

    /**
     * Parses the htmlInput with regular expressions for cleanup purposes.<p>
     * 
     * @param htmlInput the html input
     * @return processed html
     */
    private String regExp(String htmlInput) {

        String parsedHtml = new String();
        parsedHtml = htmlInput;

        // process all cleanup regexp
        for (int i = 0; i < m_cleanupPatterns.length; i++) {
            parsedHtml = m_clearStyle[i].matcher(parsedHtml).replaceAll("");
        }

        // process all replace regexp
        for (int i = 0; i < m_replacePatterns.length; i++) {
            parsedHtml = m_replaceStyle[i].matcher(parsedHtml).replaceAll(m_replaceValues[i]);
        }

        return parsedHtml;
    }

}
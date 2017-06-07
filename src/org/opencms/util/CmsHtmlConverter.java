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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;

/**
 * HTML cleaner and pretty printer.<p>
 *
 * Used to clean up HTML code (e.g. remove word tags) and optionally create XHTML from HTML.<p>
 *
 * @since 6.0.0
 */
public class CmsHtmlConverter {

    /** Parameter value for disabled mode. **/
    public static final String PARAM_DISABLED = CmsStringUtil.FALSE;

    /** Parameter value for enabled mode. **/
    public static final String PARAM_ENABLED = CmsStringUtil.TRUE;

    /** Parameter value for replace paragraph mode. */
    public static final String PARAM_REPLACE_PARAGRAPHS = "replace-paragraphs";

    /** Parameter value for WORD mode. **/
    public static final String PARAM_WORD = "cleanup";

    /** Parameter value for XHTML mode. **/
    public static final String PARAM_XHTML = "xhtml";

    /** The separator used for the configured modes String. */
    public static final char SEPARATOR_MODES = ';';

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsHtmlConverter.class);

    /** The encoding used for the HTML code conversion. */
    private String m_encoding;

    /** The conversion mode for the converter. */
    private String m_mode;

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
     * Possible values for the default conversion mode are:<ul>
     * <li>{@link #PARAM_DISABLED}: The conversion is disabled.</li>
     * <li>{@link #PARAM_ENABLED}: Conversion is enabled without transformation, so HTML is pretty printed only.</li>
     * <li>{@link #PARAM_XHTML}: Conversion from HTML to XHTML is enabled.</li>
     * <li>{@link #PARAM_WORD}: Cleanup of word like HTML tags is enabled.</li>
     * <li>Other values can be used by the implementing converter class.</li>
     * </ul>
     * Values can be combined with the <code>;</code> separator, so it is e.g. possible to convert
     * to XHTML and clean from word at the same time.<p>
     *
     * @param encoding the encoding used for the HTML code conversion
     * @param mode the conversion mode to use
     */
    public CmsHtmlConverter(String encoding, String mode) {

        init(encoding, mode);
    }

    /**
     * Reads the content conversion property of a given resource and returns its value.<p>
     *
     * A default value (disabled) is returned if the property could not be read.<p>
     *
     * @param cms the CmsObject
     * @param resource the resource in the VFS
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
     * @return true or false
     */
    public static boolean isConversionEnabled(String conversionMode) {

        boolean value = true;
        if ((conversionMode == null) || (conversionMode.indexOf(PARAM_DISABLED) != -1)) {
            value = false;
        }
        return value;
    }

    /**
     * Converts the given HTML code according to the settings of this converter.<p>
     *
     * @param htmlInput HTML input stored in an array of bytes
     * @return array of bytes containing the converted HTML
     *
     * @throws UnsupportedEncodingException if the encoding set for the conversion is not supported
     */
    public byte[] convertToByte(byte[] htmlInput) throws UnsupportedEncodingException {

        return convertToByte(new String(htmlInput, getEncoding()));
    }

    /**
     * Converts the given HTML code according to the settings of this converter.<p>
     *
     * @param htmlInput HTML input stored in a string
     * @return array of bytes containing the converted HTML
     *
     * @throws UnsupportedEncodingException if the encoding set for the conversion is not supported
     */
    public byte[] convertToByte(String htmlInput) throws UnsupportedEncodingException {

        return convertToString(htmlInput).getBytes(getEncoding());
    }

    /**
     * Converts the given HTML code according to the settings of this converter.<p>
     *
     * If an any error occurs during the conversion process, the original input is returned unmodified.<p>
     *
     * @param htmlInput HTML input stored in an array of bytes
     * @return array of bytes containing the converted HTML
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
     * Converts the given HTML code according to the settings of this converter.<p>
     *
     * If an any error occurs during the conversion process, the original input is returned unmodified.<p>
     *
     * @param htmlInput HTML input stored in a string
     * @return array of bytes containing the converted HTML
     */
    public byte[] convertToByteSilent(String htmlInput) {

        try {
            return convertToByte(htmlInput.getBytes(getEncoding()));
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(Messages.LOG_CONVERSION_BYTE_FAILED_0), e);
            }
            try {
                return htmlInput.getBytes(getEncoding());
            } catch (UnsupportedEncodingException e1) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(Messages.LOG_CONVERSION_BYTE_FAILED_0), e1);
                }
                return htmlInput.getBytes();
            }
        }
    }

    /**
     * Converts the given HTML code according to the settings of this converter.<p>
     *
     * @param htmlInput HTML input stored in an array of bytes
     * @return string containing the converted HTML
     *
     * @throws UnsupportedEncodingException if the encoding set for the conversion is not supported
     */
    public String convertToString(byte[] htmlInput) throws UnsupportedEncodingException {

        return convertToString(new String(htmlInput, getEncoding()));
    }

    /**
     * Converts the given HTML code according to the settings of the converter.<p>
     *
     * @param htmlInput HTML input stored in a string
     * @return string containing the converted HTML
     *
     * @throws UnsupportedEncodingException if the encoding set for the conversion is not supported
     */
    public String convertToString(String htmlInput) throws UnsupportedEncodingException {

        // first: collect all converter classes to use on the input
        Map<String, List<String>> converters = new HashMap<String, List<String>>();
        for (Iterator<String> i = getModes().iterator(); i.hasNext();) {
            String mode = i.next();
            String converterClass = OpenCms.getResourceManager().getHtmlConverter(mode);
            List<String> modes = new ArrayList<String>();
            if (converters.containsKey(converterClass)) {
                // converter class already defined for a previous mode, get mode list
                modes = converters.get(converterClass);
            }
            // add mode name to list for the converter
            modes.add(mode);
            // store converter with modes in map
            converters.put(converterClass, modes);
        }

        // second: convert the content with all found converter classes
        for (Iterator<Entry<String, List<String>>> i = converters.entrySet().iterator(); i.hasNext();) {
            Entry<String, List<String>> entry = i.next();
            String className = entry.getKey();
            List<String> modes = entry.getValue();
            try {
                I_CmsHtmlConverter converter = (I_CmsHtmlConverter)Class.forName(className).newInstance();
                // initialize converter
                converter.init(getEncoding(), modes);
                // convert input String
                htmlInput = converter.convertToString(htmlInput);
            } catch (ClassNotFoundException e) {
                LOG.error(
                    org.opencms.loader.Messages.get().getBundle().key(
                        org.opencms.loader.Messages.LOG_HTML_CONVERTER_CLASS_NOT_FOUND_1,
                        className),
                    e);
            } catch (IllegalAccessException e) {
                LOG.error(
                    org.opencms.loader.Messages.get().getBundle().key(
                        org.opencms.loader.Messages.LOG_HTML_CONVERTER_CLASS_NOT_FOUND_1,
                        className),
                    e);
            } catch (InstantiationException e) {
                LOG.error(
                    org.opencms.loader.Messages.get().getBundle().key(
                        org.opencms.loader.Messages.LOG_HTML_CONVERTER_CLASS_NOT_FOUND_1,
                        className),
                    e);
            }
        }
        return htmlInput;
    }

    /**
     * Converts the given HTML code according to the settings of this converter.<p>
     *
     * If an any error occurs during the conversion process, the original input is returned unmodified.<p>
     *
     * @param htmlInput HTML input stored in an array of bytes
     *
     * @return string containing the converted HTML
     */
    public String convertToStringSilent(byte[] htmlInput) {

        try {
            return convertToString(htmlInput);
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(Messages.LOG_CONVERSION_BYTE_FAILED_0), e);
            }
            try {
                return new String(htmlInput, getEncoding());
            } catch (UnsupportedEncodingException e1) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(Messages.LOG_CONVERSION_BYTE_FAILED_0), e1);
                }
                return new String(htmlInput);
            }
        }
    }

    /**
     * Converts the given HTML code according to the settings of this converter.<p>
     *
     * If an any error occurs during the conversion process, the original input is returned unmodified.<p>
     *
     * @param htmlInput HTML input stored in string
     *
     * @return string containing the converted HTML
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
     * Returns the encoding used for the HTML code conversion.<p>
     *
     * @return the encoding used for the HTML code conversion
     */
    public String getEncoding() {

        return m_encoding;
    }

    /**
     * Returns the conversion mode to use.<p>
     *
     * @return the conversion mode to use
     */
    public String getMode() {

        return m_mode;
    }

    /**
     * Returns the conversion modes to use as List of String parameters.<p>
     *
     * @return the conversion modes to use as List of String parameters
     */
    private List<String> getModes() {

        List<String> modes = new ArrayList<String>();
        try {
            modes = CmsStringUtil.splitAsList(getMode(), SEPARATOR_MODES, true);
        } catch (Exception e) {
            // error generating list, an empty list will be returned
        }

        return modes;
    }

    /**
     * Initializes the HTML converter instance.<p>
     *
     * Possible values for the conversion mode are dependent from the converter implementation.<p>
     *
     * Values can be combined with the <code>;</code> separator, so that it is e.g. possible to convert
     * to XHTML and clean from word at the same time.<p>
     *
     * @param encoding the encoding used for the HTML code conversion
     * @param mode the conversion mode to use
     */
    private void init(String encoding, String mode) {

        if (encoding == null) {
            m_encoding = CmsEncoder.ENCODING_UTF_8;
        } else {
            m_encoding = encoding;
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(mode)) {
            m_mode = "";
        } else {
            m_mode = mode;
        }
    }

}
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

package org.opencms.xml.types;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.loader.CmsImageScaler;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.I_CmsXmlDocument;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.dom4j.Element;

/**
 * Describes the XML content type "OpenCmsVfsImage".<p>
 *
 * This type allows links to internal VFS images only.<p>
 *
 * @since 7.5.0
 */
public class CmsXmlVfsImageValue extends CmsXmlVfsFileValue {

    /** Node name for the scale element. */
    public static final String NODE_SCALE = "scale";

    /** Request parameter name for the description parameter. */
    public static final String PARAM_DESCRIPTION = "description";

    /** Request parameter name for the format parameter. */
    public static final String PARAM_FORMAT = "format";

    /** The name of this type as used in the XML schema. */
    public static final String TYPE_NAME_IMAGE = "OpenCmsVfsImage";

    /** The schema definition String is located in a text for easier editing. */
    private static String m_schemaDefinition;

    /** The description text of the image. */
    private String m_description;

    /** The selected image format. */
    private String m_format;

    /** Holds the parameters of the URL. */
    private Map<String, String[]> m_parameters;

    /** The scale options of the image. */
    private String m_scaleOptions;

    /**
     * Creates a new, empty schema type descriptor of type "OpenCmsVfsImage".<p>
     */
    public CmsXmlVfsImageValue() {

        // empty constructor is required for class registration
    }

    /**
     * Creates a new XML content value of type "OpenCmsVfsImage".<p>
     *
     * @param document the XML content instance this value belongs to
     * @param element the XML element that contains this value
     * @param locale the locale this value is created for
     * @param type the type instance to create the value for
     */
    public CmsXmlVfsImageValue(I_CmsXmlDocument document, Element element, Locale locale, I_CmsXmlSchemaType type) {

        super(document, element, locale, type);
    }

    /**
     * Creates a new schema type descriptor for the type "OpenCmsVfsImage".<p>
     *
     * @param name the name of the XML node containing the value according to the XML schema
     * @param minOccurs minimum number of occurrences of this type according to the XML schema
     * @param maxOccurs maximum number of occurrences of this type according to the XML schema
     */
    public CmsXmlVfsImageValue(String name, String minOccurs, String maxOccurs) {

        super(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#createValue(I_CmsXmlDocument, org.dom4j.Element, Locale)
     */
    @Override
    public I_CmsXmlContentValue createValue(I_CmsXmlDocument document, Element element, Locale locale) {

        return new CmsXmlVfsImageValue(document, element, locale, this);
    }

    /**
     * Returns the description of the image.<p>
     *
     * @param cms the current users context
     * @return the description of the image or an empty String
     */
    public String getDescription(CmsObject cms) {

        if (m_description == null) {
            if (m_element.element(PARAM_DESCRIPTION) != null) {
                m_description = m_element.element(PARAM_DESCRIPTION).getText();
            }
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_description)) {
                m_description = getParameterValue(cms, PARAM_DESCRIPTION);
                m_description = CmsEncoder.unescape(m_description, CmsEncoder.ENCODING_UTF_8);
            }
        }
        return m_description;
    }

    /**
     * Returns the format information of the image.<p>
     *
     * @param cms the current users context
     * @return the format information of the image or an empty String
     */
    public String getFormat(CmsObject cms) {

        if (m_format == null) {
            if (m_element.element(PARAM_FORMAT) != null) {
                m_format = m_element.element(PARAM_FORMAT).getText();
            }
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_format)) {
                m_format = getParameterValue(cms, PARAM_FORMAT);
            }
        }
        return m_format;
    }

    /**
     * Returns the link without parameters from the string value.<p>
     *
     * @param cms the current users context
     * @return the link without parameters
     */
    public String getRequestLink(CmsObject cms) {

        return CmsRequestUtil.getRequestLink(getStringValue(cms));
    }

    /**
     * Returns the scale options of the image.<p>
     *
     * @param cms the current users context
     * @return the scale options of the image or an empty String
     */
    public String getScaleOptions(CmsObject cms) {

        if (m_scaleOptions == null) {
            if (m_element.element(NODE_SCALE) != null) {
                m_scaleOptions = m_element.element(NODE_SCALE).getText();
            }
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_scaleOptions)) {
                m_scaleOptions = getParameterValue(cms, CmsImageScaler.PARAM_SCALE);
            }
        }
        return m_scaleOptions;
    }

    /**
     * @see org.opencms.xml.types.I_CmsXmlSchemaType#getSchemaDefinition()
     */
    @Override
    public String getSchemaDefinition() {

        // the schema definition is located in a separate file for easier editing
        if (m_schemaDefinition == null) {
            m_schemaDefinition = readSchemaDefinition("org/opencms/xml/types/XmlVfsImageValue.xsd");
        }
        return m_schemaDefinition;
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#getTypeName()
     */
    @Override
    public String getTypeName() {

        return TYPE_NAME_IMAGE;
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#newInstance(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public I_CmsXmlSchemaType newInstance(String name, String minOccurs, String maxOccurs) {

        return new CmsXmlVfsImageValue(name, minOccurs, maxOccurs);
    }

    /**
     * Sets the description of the image.<p>
     *
     * @param cms the current users context
     * @param description the description of the image
     */
    public void setDescription(CmsObject cms, String description) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(description)) {
            m_description = "";
            if (m_element.element(PARAM_DESCRIPTION) != null) {
                m_element.remove(m_element.element(PARAM_DESCRIPTION));
            }
        } else {
            m_description = description;
            description = CmsEncoder.escapeWBlanks(description, CmsEncoder.ENCODING_UTF_8);
        }
        setParameterValue(cms, PARAM_DESCRIPTION, description);
    }

    /**
     * Sets the format information of the image.<p>
     *
     * @param cms the current users contexts
     * @param format the format information of the image
     */
    public void setFormat(CmsObject cms, String format) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(format)) {
            m_format = "";
            if (m_element.element(PARAM_FORMAT) != null) {
                m_element.remove(m_element.element(PARAM_FORMAT));
            }
        } else {
            m_format = format;
        }
        setParameterValue(cms, PARAM_FORMAT, format);
    }

    /**
     * Sets the scale options of the image.<p>
     *
     * @param cms the current users context
     * @param scaleOptions the scale options of the image
     */
    public void setScaleOptions(CmsObject cms, String scaleOptions) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(scaleOptions)) {
            m_scaleOptions = "";
            if (m_element.element(NODE_SCALE) != null) {
                m_element.remove(m_element.element(NODE_SCALE));
            }
        } else {
            m_scaleOptions = scaleOptions;
        }
        setParameterValue(cms, CmsImageScaler.PARAM_SCALE, scaleOptions);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#setStringValue(org.opencms.file.CmsObject, java.lang.String)
     */
    @Override
    public void setStringValue(CmsObject cms, String value) throws CmsIllegalArgumentException {

        // call the super implementation to set the value
        super.setStringValue(cms, value);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
            // no valid value given
            return;
        }

        // get the request parameters from the provided value
        Map<String, String[]> params = getParameterMap(value);

        // create description element if present as parameter
        String desc = getParameterValue(cms, params, PARAM_DESCRIPTION);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(desc)) {
            desc = CmsEncoder.unescape(desc, CmsEncoder.ENCODING_UTF_8);
            m_element.addElement(PARAM_DESCRIPTION).addCDATA(desc);
        }
        // create format name element if present as parameter
        String format = getParameterValue(cms, params, PARAM_FORMAT);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(format)) {
            m_element.addElement(PARAM_FORMAT).addCDATA(format);
        }
        // create scale element if present as parameter
        String scale = getParameterValue(cms, params, CmsImageScaler.PARAM_SCALE);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(scale)) {
            m_element.addElement(NODE_SCALE).addCDATA(scale);
        }
        // reset the parameter map
        m_parameters = null;
        // reset the members containing the element values
        m_format = null;
        m_description = null;
        m_scaleOptions = null;
    }

    /**
     * Returns the parameters as Map from the given url String.<p>
     *
     * @param url the url String to get the parameters from
     * @return the parameters as Map
     */
    private Map<String, String[]> getParameterMap(String url) {

        Map<String, String[]> result = new HashMap<String, String[]>();
        if (CmsStringUtil.isNotEmpty(url)) {
            int pos = url.indexOf(CmsRequestUtil.URL_DELIMITER);
            if (pos >= 0) {
                result = CmsRequestUtil.createParameterMap(url.substring(pos + 1));
            }
        }
        return result;
    }

    /**
     * Returns the value of the given parameter name from a parameter map.<p>
     *
     * @param cms the current users context
     * @param parameterMap the map containing the parameters
     * @param key the parameter name
     * @return the value of the parameter or an empty String
     */
    private String getParameterValue(CmsObject cms, Map<String, String[]> parameterMap, String key) {

        String result = null;
        String[] params = parameterMap.get(key);
        if ((params != null) && (params.length > 0)) {
            result = params[0];
        }
        if (result == null) {
            return "";
        }
        return result;
    }

    /**
     * Returns the value of the given parameter name from the current parameter map.<p>
     *
     * @param cms the current users context
     * @param key the parameter name
     * @return the value of the parameter or an empty String
     */
    private String getParameterValue(CmsObject cms, String key) {

        if (m_parameters == null) {
            m_parameters = getParameterMap(getStringValue(cms));
        }
        return getParameterValue(cms, m_parameters, key);
    }

    /**
     * Sets a parameter for the image with the provided key as name and the value.<p>
     *
     * @param cms the current users context
     * @param key the parameter name to set
     * @param value the value of the parameter
     */
    private void setParameterValue(CmsObject cms, String key, String value) {

        if (m_parameters == null) {
            m_parameters = getParameterMap(getStringValue(cms));
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value) && m_parameters.containsKey(key)) {
            m_parameters.remove(key);
        } else if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
            m_parameters.put(key, new String[] {value});
        }
        String result = CmsRequestUtil.getRequestLink(getStringValue(cms));
        result = CmsRequestUtil.appendParameters(result, m_parameters, false);
        setStringValue(cms, result);
    }

}
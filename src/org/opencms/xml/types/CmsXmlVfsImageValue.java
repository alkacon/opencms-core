/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/types/CmsXmlVfsImageValue.java,v $
 * Date   : $Date: 2008/11/07 15:44:13 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2008 Alkacon Software GmbH (http://www.alkacon.com)
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
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 7.0.0 
 */
public class CmsXmlVfsImageValue extends CmsXmlVfsFileValue {

    /** Request parameter name for the description parameter. */
    public static final String PARAM_DESCRIPTION = "description";

    /** Request parameter name for the format parameter. */
    public static final String PARAM_FORMAT = "format";

    /** The name of this type as used in the XML schema. */
    public static final String TYPE_NAME_IMAGE = "OpenCmsVfsImage";

    /** Holds the parameters of the URL. */
    private Map m_parameters;

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
     * @param minOccurs minimum number of occurences of this type according to the XML schema
     * @param maxOccurs maximum number of occurences of this type according to the XML schema
     */
    public CmsXmlVfsImageValue(String name, String minOccurs, String maxOccurs) {

        super(name, minOccurs, maxOccurs);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#createValue(I_CmsXmlDocument, org.dom4j.Element, Locale)
     */
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

        String result = getParameterValue(cms, PARAM_DESCRIPTION);
        return CmsEncoder.unescape(result, CmsEncoder.ENCODING_UTF_8);
    }

    /**
     * Returns the format information of the image.<p>
     * 
     * @param cms the current users context
     * @return the format information of the image or an empty String
     */
    public String getFormat(CmsObject cms) {

        return getParameterValue(cms, PARAM_FORMAT);
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

        return getParameterValue(cms, CmsImageScaler.PARAM_SCALE);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#getTypeName()
     */
    public String getTypeName() {

        return TYPE_NAME_IMAGE;
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#newInstance(java.lang.String, java.lang.String, java.lang.String)
     */
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

        if (CmsStringUtil.isNotEmpty(description)) {
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

        setParameterValue(cms, PARAM_FORMAT, format);
    }

    /**
     * Sets the scale options of the image.<p>
     * 
     * @param cms the current users context
     * @param scaleOptions the scale options of the image
     */
    public void setScaleOptions(CmsObject cms, String scaleOptions) {

        setParameterValue(cms, CmsImageScaler.PARAM_SCALE, scaleOptions);
    }

    /**
     * @see org.opencms.xml.types.A_CmsXmlContentValue#setStringValue(org.opencms.file.CmsObject, java.lang.String)
     */
    public void setStringValue(CmsObject cms, String value) throws CmsIllegalArgumentException {

        // call the super implementation to set the value
        super.setStringValue(cms, value);
        // reset the parameter map
        m_parameters = null;
    }

    /**
     * Returns the parameters as Map from the given url String.<p>
     * 
     * @param url the url String to get the parameters from
     * @return the parameters as Map
     */
    private Map getParameterMap(String url) {

        Map result = new HashMap();
        if (CmsStringUtil.isNotEmpty(url)) {
            int pos = url.indexOf(CmsRequestUtil.URL_DELIMITER);
            if (pos >= 0) {
                result = CmsRequestUtil.createParameterMap(url.substring(pos + 1));
            }
        }
        return result;
    }

    /**
     * Returns the value of the given parameter name.<p>
     * 
     * @param cms the current users context
     * @param key the parameter name
     * @return the value of the parameter or an empty String
     */
    private String getParameterValue(CmsObject cms, String key) {

        if (m_parameters == null) {
            m_parameters = getParameterMap(getStringValue(cms));
        }
        String result = null;
        String[] params = ((String[])m_parameters.get(key));
        if (params != null && params.length > 0) {
            result = params[0];
        }
        if (result == null) {
            return "";
        }
        return result;
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
            m_parameters.put(key, value);
        }
        String result = CmsRequestUtil.getRequestLink(getStringValue(cms));
        result = CmsRequestUtil.appendParameters(result, m_parameters, false);
        setStringValue(cms, result);
    }

}
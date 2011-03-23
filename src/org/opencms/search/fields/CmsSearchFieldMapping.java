/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/fields/CmsSearchFieldMapping.java,v $
 * Date   : $Date: 2011/03/23 14:53:14 $
 * Version: $Revision: 1.16 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.search.fields;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsRuntimeException;
import org.opencms.search.Messages;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.util.CmsStringUtil;

import java.util.List;

/**
 * Describes a mapping of a piece of content from an OpenCms VFS resource to a field of a search index.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.16 $ 
 * 
 * @since 7.0.0 
 */
public class CmsSearchFieldMapping {

    /** The configured default value. */
    private String m_defaultValue;

    /** Pre-calculated hash value. */
    private int m_hashCode;

    /** The parameter for the mapping type. */
    private String m_param;

    /** The mapping type. */
    private CmsSearchFieldMappingType m_type;

    /**
     * Public constructor for a new search field mapping.<p>
     */
    public CmsSearchFieldMapping() {

        // no initialization required
    }

    /**
     * Public constructor for a new search field mapping.<p>
     * 
     * @param type the type to use, see {@link #setType(CmsSearchFieldMappingType)}
     * @param param the mapping parameter, see {@link #setParam(String)}
     */
    public CmsSearchFieldMapping(CmsSearchFieldMappingType type, String param) {

        this();
        setType(type);
        setParam(param);
    }

    /**
     * Two mappings are equal if the type and the parameter is equal.<p>
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsSearchFieldMapping) {
            CmsSearchFieldMapping other = (CmsSearchFieldMapping)obj;
            return CmsStringUtil.isEqual(m_type, other.m_type) && CmsStringUtil.isEqual(m_param, other.m_param);
        }
        return false;
    }

    /**
     * Returns the default value used for this field mapping in case no content is available.<p>
     * 
     * @return the default value used for this field mapping in case no content is available
     */
    public String getDefaultValue() {

        return m_defaultValue;
    }

    /**
     * Returns the mapping parameter.<p>
     *
     * The parameter is used depending on the implementation of the rules of 
     * the selected {@link CmsSearchFieldMappingType}.<p>
     *
     * @return the mapping parameter
     */
    public String getParam() {

        return m_param;
    }

    /**
     * Returns the String value extracted form the provided data according to the rules of this mapping type.<p> 
     * 
     * @param cms the OpenCms context used for building the search index
     * @param res the resource that is indexed
     * @param extractionResult the plain text extraction result from the resource
     * @param properties the list of all properties directly attached to the resource (not searched)
     * @param propertiesSearched the list of all searched properties of the resource  
     * 
     * @return the String value extracted form the provided data according to the rules of this mapping type
     */
    public String getStringValue(
        CmsObject cms,
        CmsResource res,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        String content = null;
        switch (getType().getMode()) {
            case 0: // content
                if (extractionResult != null) {
                    content = extractionResult.getContent();
                }
                break;
            case 1: // property
            case 2: // property-search
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParam())) {
                    boolean search = (getType() == CmsSearchFieldMappingType.PROPERTY_SEARCH);
                    if (search) {
                        content = CmsProperty.get(getParam(), propertiesSearched).getValue();
                    } else {
                        content = CmsProperty.get(getParam(), properties).getValue();
                    }
                }
                break;
            case 3: // item
                if ((extractionResult != null) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParam())) {
                    content = extractionResult.getContentItems().get(getParam());
                }
                break;
            default:
                // noop, content is already null
        }
        if (content == null) {
            // in case the content is not available, use the default value for this mapping
            content = getDefaultValue();
        }
        return content;
    }

    /**
     * Returns the mapping type.<p>
     *
     * @return the mapping type
     */
    public CmsSearchFieldMappingType getType() {

        return m_type;
    }

    /**
     * The hash code depends on the type and the parameter.<p>
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        if (m_hashCode == 0) {
            int hashCode = 73 * ((m_type == null) ? 29 : m_type.hashCode());
            if (m_param != null) {
                hashCode += m_param.hashCode();
            }
            m_hashCode = hashCode;
        }
        return m_hashCode;
    }

    /**
     * Sets the default value for this field mapping in case no content is available.<p> 
     * 
     * @param defaultValue the default value to set
     */
    public void setDefaultValue(String defaultValue) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(defaultValue)) {
            m_defaultValue = defaultValue.trim();
        } else {
            m_defaultValue = null;
        }
    }

    /**
     * Sets the mapping parameter.<p>
     *
     * The parameter is used depending on the implementation of the rules of 
     * the selected {@link CmsSearchFieldMappingType}.<p>
     *
     * @param param the parameter to set
     */
    public void setParam(String param) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(param)) {
            m_param = param.trim();
        } else {
            m_param = null;
        }
    }

    /**
     * Sets the mapping type.<p>
     *
     * @param type the type to set
     */
    public void setType(CmsSearchFieldMappingType type) {

        m_type = type;
    }

    /**
     * Sets the mapping type as a String.<p>
     *
     * @param type the name of the type to set
     */
    public void setType(String type) {

        CmsSearchFieldMappingType mappingType = CmsSearchFieldMappingType.valueOf(type);
        if (mappingType == null) {
            // invalid mapping type has been used, throw an exception
            throw new CmsRuntimeException(new CmsMessageContainer(
                Messages.get(),
                Messages.ERR_FIELD_TYPE_UNKNOWN_1,
                new Object[] {type}));
        }
        setType(mappingType);
    }
}
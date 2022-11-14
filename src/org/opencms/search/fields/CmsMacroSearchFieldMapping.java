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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.search.galleries.CmsGalleryNameMacroResolver;
import org.opencms.util.CmsMacroResolver;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Field mapping to resolve macros as for gallery names.
 *
 * The main purpose is to use stringtemplates for special mappings to Solr fields.
 *
 * For this use case, define a parameter (via <code>xsd:annotation/xsd:appinfo/parameters/param</code> and use a stringtemplate as value.
 * In the solr mapping, you just place <code>%(stringtemplate:paramName)</code>.
 *
 * Example (there is some element "Type" and in the parameters section of the schema, there's a param "eventKind"):
 * <pre>
 *   &lt;searchsetting element="Type"&gt;
 *     &lt;solrfield targetfield="event-kind" sourcefield="*_s"&gt;
 *       &lt;mapping type="dynamic" class="org.opencms.search.fields.CmsSchemaParameterSearchFieldMapping"&gt;%(stringtemplate:eventKind)&lt;/mapping&gt;
 *     &lt;/solrfield&gt;
 *   &lt;/searchsetting&gt;
 * </pre>
 */
public class CmsMacroSearchFieldMapping implements I_CmsSearchFieldMapping {

    /** Serialization id */
    private static final long serialVersionUID = 1L;

    /** Logger for the class */
    protected static final Log LOG = CmsLog.getLog(CmsMacroSearchFieldMapping.class);

    /** The configuration parameter as handed over to the mapping. */
    private String m_param;

    /** The mapping type. */
    private CmsSearchFieldMappingType m_type;

    /** The default value set via the interface method. */
    private String m_defaultValue = null;

    /** The content locale to index for. */
    private Locale m_locale = null;

    /**
     * Public constructor for a new search field mapping.
     * <p>
     */
    public CmsMacroSearchFieldMapping() {

        m_param = null;
        setType(CmsSearchFieldMappingType.DYNAMIC);
    }

    /**
     * Public constructor for a new search field mapping.
     * <p>
     *
     * @param type
     *            the type to use, see
     *            {@link #setType(CmsSearchFieldMappingType)}
     * @param param
     *            the mapping parameter, see {@link #setParam(String)}
     */
    public CmsMacroSearchFieldMapping(CmsSearchFieldMappingType type, String param) {

        this();
        setParam(param);
        setType(type);
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#getDefaultValue()
     */
    public String getDefaultValue() {

        return m_defaultValue;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#getParam()
     */
    public String getParam() {

        return m_param;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#getStringValue(org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.extractors.I_CmsExtractionResult, java.util.List, java.util.List)
     */
    public String getStringValue(
        CmsObject cms,
        CmsResource res,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        if (m_param != null) {
            try {
                CmsObject cmsClone = OpenCms.initCmsObject(cms);
                if (null != m_locale) {
                    cmsClone.getRequestContext().setLocale(m_locale);
                }
                CmsFile file = cmsClone.readFile(res);
                CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsClone, file);
                CmsMacroResolver resolver = new CmsGalleryNameMacroResolver(cms, content, m_locale);
                return resolver.resolveMacros(m_param);
            } catch (CmsException e) {
                LOG.error("Failed to resolve search field mapping value. Returning null.", e);
            }
        }
        return null;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#getType()
     */
    @Override
    public CmsSearchFieldMappingType getType() {

        return m_type;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#setDefaultValue(java.lang.String)
     */
    @Override
    public void setDefaultValue(String defaultValue) {

        m_defaultValue = defaultValue;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#setLocale(java.util.Locale)
     */
    @Override
    public void setLocale(Locale locale) {

        m_locale = locale;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#setParam(java.lang.String)
     */
    @Override
    public void setParam(String param) {

        m_param = param;

    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#setType(org.opencms.search.fields.CmsSearchFieldMappingType)
     */
    @Override
    public void setType(CmsSearchFieldMappingType type) {

        m_type = type;

    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#setType(java.lang.String)
     */
    @Override
    public void setType(String type) {

        m_type = CmsSearchFieldMappingType.valueOf(type);

    }

}

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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.I_CmsSearchIndex;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.search.solr.CmsSolrDocumentXmlContent;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.util.CmsGeoUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsGeoMappingConfiguration;
import org.opencms.xml.content.I_CmsXmlContentHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Class extracting the Geo coordinates from a content field.
 */
public class CmsGeoCoordinateFieldMapping implements I_CmsSearchFieldMapping {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsGeoCoordinateFieldMapping.class);

    /** Serial version UID. */
    private static final long serialVersionUID = 1;

    /** Maximum recursion depth for following links. */
    public static final int MAX_DEPTH = 2;

    /** The geo-mapping configuration. */
    private CmsGeoMappingConfiguration m_config;

    /**
     * Creates a new instance.
     *
     * @param config the configuration to use
     */
    public CmsGeoCoordinateFieldMapping(CmsGeoMappingConfiguration config) {

        m_config = config;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#getDefaultValue()
     */
    public String getDefaultValue() {

        return "0.000000,0.000000";
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#getParam()
     */
    public String getParam() {

        return "";
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

        String result = getStringValue(0, m_config, cms, res, extractionResult);
        return result;
    }

    /**
     * Gets the mapped value.
     *
     * @param depth the current recursion depth
     * @param mappingConfig the mapping configuration to use
     * @param cms the CMS context
     * @param res the resource for which to get the value
     * @param extractionResult the extraction result of the resource
     *
     * @return the mapped value
     */
    public String getStringValue(
        int depth,
        CmsGeoMappingConfiguration mappingConfig,
        CmsObject cms,
        CmsResource res,
        I_CmsExtractionResult extractionResult) {

        for (CmsGeoMappingConfiguration.Entry entry : mappingConfig.getEntries()) {
            try {
                switch (entry.getType()) {
                    case field:
                        String value = findFirstCoordinatesValue(extractionResult, entry.getValue());
                        String coord = CmsGeoUtil.parseCoordinates(value);
                        if (coord != null) {
                            return coord;
                        }
                        break;
                    case link:
                        if (depth >= MAX_DEPTH) {
                            LOG.error(
                                "maximum depth exceeded for linked geo-coordinate mapping in " + res.getRootPath());
                            return null;
                        }
                        String xpath = CmsXmlUtils.createXpath(entry.getValue(), 1);
                        Set<String> paths = new HashSet<>();
                        for (Locale locale : extractionResult.getLocales()) {
                            String path = extractionResult.getContentItems(locale).get(xpath);
                            if (path != null) {
                                paths.add(path);
                            }
                        }
                        I_CmsSearchIndex index = OpenCms.getSearchManager().getIndex(
                            cms.getRequestContext().getCurrentProject().isOnlineProject()
                            ? CmsSolrIndex.DEFAULT_INDEX_NAME_ONLINE
                            : CmsSolrIndex.DEFAULT_INDEX_NAME_OFFLINE);

                        for (String path : paths) {
                            try {
                                CmsResource linkedResource = cms.readResource(path);
                                A_CmsXmlDocument[] contentHolder = new A_CmsXmlDocument[] {null};
                                CmsExtractionResult linkExtractionResult = CmsSolrDocumentXmlContent.extractXmlContent(
                                    cms,
                                    linkedResource,
                                    index,
                                    null,
                                    new HashSet<>(),
                                    content -> {
                                        contentHolder[0] = content;
                                    });
                                if ((linkExtractionResult != null) && (contentHolder[0] != null)) {
                                    I_CmsXmlContentHandler linkContentHandler = contentHolder[0].getContentDefinition().getContentHandler();
                                    CmsGeoMappingConfiguration linkMappingConfig = linkContentHandler.getGeoMappingConfiguration();
                                    if (linkMappingConfig != null) {
                                        String linkResult = getStringValue(
                                            depth + 1,
                                            linkMappingConfig,
                                            cms,
                                            contentHolder[0].getFile(),
                                            linkExtractionResult);
                                        if (linkResult != null) {
                                            return linkResult;
                                        }
                                    }
                                }
                            } catch (CmsVfsResourceNotFoundException e) {
                                LOG.debug(e.getLocalizedMessage(), e);
                            } catch (Exception e) {
                                LOG.error(e.getLocalizedMessage(), e);
                            }
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return null;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#getType()
     */
    public CmsSearchFieldMappingType getType() {

        return CmsSearchFieldMappingType.ITEM;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#setDefaultValue(java.lang.String)
     */
    public void setDefaultValue(String defaultValue) {

        // not used
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#setParam(java.lang.String)
     */
    public void setParam(String param) {

        // not used
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#setType(org.opencms.search.fields.CmsSearchFieldMappingType)
     */
    public void setType(CmsSearchFieldMappingType type) {

        // not used
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldMapping#setType(java.lang.String)
     */
    public void setType(String type) {

        // not used
    }

    /**
     * At first, we search for a coordinates value in the best matching locale of the extraction
     * result. If not available, search for a coordinates value in other locales. In the case of
     * a multi-valued field, only the first coordinates value is returned. Further values are ignored.
     *
     * @param xpath the path to look up in the extraction result
     * @param extractionResult the extraction result
     * @return the coordinates value
     */
    private String findFirstCoordinatesValue(I_CmsExtractionResult extractionResult, String xpath) {

        xpath = CmsXmlUtils.createXpath(xpath, 1);
        String value = extractionResult.getContentItems().get(xpath);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
            for (Locale locale : extractionResult.getLocales()) {
                String val = extractionResult.getContentItems(locale).get(xpath);
                if (!CmsStringUtil.isEmptyOrWhitespaceOnly(val)) {
                    return val;
                }
            }
        }
        return value;
    }

}

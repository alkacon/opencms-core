/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.search.solr;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.I_CmsSearchDocument;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.search.fields.A_CmsSearchFieldConfiguration;
import org.opencms.search.fields.CmsSearchFieldMapping;
import org.opencms.search.fields.CmsSearchFieldMappingType;
import org.opencms.search.fields.I_CmsSearchField;
import org.opencms.search.fields.I_CmsSearchFieldMapping;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.solr.common.SolrInputDocument;

/**
 * The search field implementation for Solr.<p>
 * 
 * @since 8.5.0
 */
public class CmsSolrFieldConfiguration extends A_CmsSearchFieldConfiguration {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSolrFieldConfiguration.class);

    /** Stores additional fields to index. */
    private List<I_CmsSearchField> m_additionalFields;

    /** Signals if initialization has been done already. */
    private boolean m_initialized;

    /**
     * Default constructor.<p>
     */
    public CmsSolrFieldConfiguration() {

        super();
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldConfiguration#createEmptyDocument(org.opencms.file.CmsResource)
     */
    public I_CmsSearchDocument createEmptyDocument(CmsResource resource) {

        CmsSolrDocument doc = new CmsSolrDocument(new SolrInputDocument());
        doc.setId(resource.getStructureId());
        return doc;
    }

    /**
     * @see org.opencms.search.fields.A_CmsSearchFieldConfiguration#appendProperties(org.opencms.search.I_CmsSearchDocument, org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.extractors.I_CmsExtractionResult, java.util.List, java.util.List)
     */
    @Override
    protected I_CmsSearchDocument appendProperties(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extraction,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        for (CmsProperty prop : properties) {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(prop.getValue())) {
                document.addSearchField(new CmsSolrField(
                    prop.getName() + I_CmsSearchField.FIELD_DYNAMIC_PROPERTIES,
                    null,
                    null,
                    null,
                    I_CmsSearchField.BOOST_DEFAULT), prop.getValue());
            }
        }
        return document;
    }

    /**
     * @see org.opencms.search.fields.A_CmsSearchFieldConfiguration#appendDates(org.opencms.search.I_CmsSearchDocument, org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.extractors.I_CmsExtractionResult, java.util.List, java.util.List)
     */
    @Override
    protected I_CmsSearchDocument appendDates(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        document.addDateField(I_CmsSearchField.FIELD_DATE_CREATED, resource.getDateCreated(), false);
        document.addDateField(I_CmsSearchField.FIELD_DATE_LASTMODIFIED, resource.getDateLastModified(), false);
        document.addDateField(I_CmsSearchField.FIELD_DATE_CONTENT, resource.getDateContent(), false);
        document.addDateField(I_CmsSearchField.FIELD_DATE_RELEASED, resource.getDateReleased(), false);
        document.addDateField(I_CmsSearchField.FIELD_DATE_EXPIRED, resource.getDateExpired(), false);

        return document;
    }

    /**
     * @see org.opencms.search.fields.A_CmsSearchFieldConfiguration#appendFieldMapping(org.opencms.search.I_CmsSearchDocument, org.opencms.search.fields.I_CmsSearchField, org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.extractors.I_CmsExtractionResult, java.util.List, java.util.List)
     */
    @Override
    protected I_CmsSearchDocument appendFieldMapping(
        I_CmsSearchDocument document,
        I_CmsSearchField sfield,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        CmsSolrField field = (CmsSolrField)sfield;
        try {
            CmsObject clone = OpenCms.initCmsObject(cms);
            clone.getRequestContext().setLocale(field.getLocale());
            StringBuffer text = new StringBuffer();
            for (I_CmsSearchFieldMapping mapping : field.getMappings()) {
                if (extractionResult != null) {
                    String mapResult = null;
                    if ((field.getLocale() != null) && mapping.getType().equals(CmsSearchFieldMappingType.CONTENT)) {
                        // try the localized content field
                        String key = A_CmsSearchFieldConfiguration.getLocaleExtendedName(
                            I_CmsSearchField.FIELD_CONTENT,
                            field.getLocale());
                        mapResult = extractionResult.getContentItems().get(key);
                    } else {
                        mapResult = mapping.getStringValue(
                            clone,
                            resource,
                            extractionResult,
                            properties,
                            propertiesSearched);
                    }
                    if (mapResult != null) {
                        if (text.length() > 0) {
                            text.append('\n');
                        }
                        text.append(mapResult);
                    } else if (mapping.getDefaultValue() != null) {
                        text.append("\n" + mapping.getDefaultValue());
                    }
                }
            }
            if ((text.length() <= 0) && (field.getDefaultValue() != null)) {
                text.append(field.getDefaultValue());
            }
            if (text.length() > 0) {
                document.addSearchField(field, text.toString());
            }
        } catch (CmsException e) {
            // nothing to do just log
            LOG.debug(e);
        }
        return document;
    }

    /**
     * @see org.opencms.search.fields.A_CmsSearchFieldConfiguration#appendFieldMappings(org.opencms.search.I_CmsSearchDocument, org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.extractors.I_CmsExtractionResult, java.util.List, java.util.List)
     */
    @Override
    protected I_CmsSearchDocument appendFieldMappings(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        if (!m_initialized) {
            // we need a lazy initialization here, because the OpenCms locale manager
            // has not been finally initialized when the search field configuration is created
            addContentFields();
            addAdditionalFields();
            m_initialized = true;
        }

        if ((extractionResult != null) && (extractionResult.getMappingFields() != null)) {
            for (I_CmsSearchField field : extractionResult.getMappingFields()) {
                document = appendFieldMapping(
                    document,
                    field,
                    cms,
                    resource,
                    extractionResult,
                    properties,
                    propertiesSearched);
            }
        }

        return super.appendFieldMappings(document, cms, resource, extractionResult, properties, propertiesSearched);
    }

    /**
     * @see org.opencms.search.fields.A_CmsSearchFieldConfiguration#appendLocales(org.opencms.search.I_CmsSearchDocument, org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.extractors.I_CmsExtractionResult, java.util.List, java.util.List)
     */
    @Override
    protected I_CmsSearchDocument appendLocales(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extraction,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        if ((extraction != null)
            && (extraction.getContentItems() != null)
            && (extraction.getContentItems().get(I_CmsSearchField.FIELD_RESOURCE_LOCALES) != null)) {
            String localesAsString = extraction.getContentItems().get(I_CmsSearchField.FIELD_RESOURCE_LOCALES);
            document.addResourceLocales(CmsStringUtil.splitAsList(localesAsString, ' '));
        } else {
            List<String> localesAsList = new ArrayList<String>();
            List<Locale> locales = OpenCms.getLocaleManager().getDefaultLocales(cms, resource);
            for (Locale locale : locales) {
                localesAsList.add(locale.toString());
            }
            document.addResourceLocales(localesAsList);
        }

        return super.appendLocales(document, cms, resource, extraction, properties, propertiesSearched);
    }

    /**
     * Sets the additionalFields.<p>
     *
     * @param additionalFields the additionalFields to set
     */
    protected void setAdditionalFields(List<I_CmsSearchField> additionalFields) {

        m_additionalFields = additionalFields;
    }

    /**
     * Adds the additional fields to the configuration, if they are not null.<p>
     */
    private void addAdditionalFields() {

        if (m_additionalFields != null) {
            addFields(m_additionalFields);
        }
    }

    /**
     * Adds a localized field for the extracted content to the schema.<p>
     */
    private void addContentFields() {

        // add the content_<locale> fields to this configuration

        CmsSolrField solrField = new CmsSolrField(
            I_CmsSearchField.FIELD_CONTENT,
            null,
            null,
            null,
            I_CmsSearchField.BOOST_DEFAULT);
        solrField.addMapping(new CmsSearchFieldMapping(
            CmsSearchFieldMappingType.CONTENT,
            I_CmsSearchField.FIELD_CONTENT));
        addField(solrField);
        for (Locale locale : OpenCms.getLocaleManager().getAvailableLocales()) {
            solrField = new CmsSolrField(A_CmsSearchFieldConfiguration.getLocaleExtendedName(
                I_CmsSearchField.FIELD_CONTENT,
                locale), null, locale, null, I_CmsSearchField.BOOST_DEFAULT);
            solrField.addMapping(new CmsSearchFieldMapping(
                CmsSearchFieldMappingType.CONTENT,
                I_CmsSearchField.FIELD_CONTENT));
            addField(solrField);
        }
    }
}

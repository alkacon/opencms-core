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

import org.opencms.ade.containerpage.CmsDetailOnlyContainerUtil;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.loader.CmsResourceManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndexSource;
import org.opencms.search.CmsSearchUtil;
import org.opencms.search.I_CmsSearchDocument;
import org.opencms.search.documents.CmsDocumentDependency;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.search.fields.CmsLuceneField;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.search.fields.CmsSearchFieldMapping;
import org.opencms.search.fields.CmsSearchFieldMappingType;
import org.opencms.search.fields.I_CmsSearchFieldMapping;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsVfsUtil;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.content.I_CmsXmlContentHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.solr.common.SolrInputDocument;

/**
 * The search field implementation for Solr.<p>
 *
 * @since 8.5.0
 */
public class CmsSolrFieldConfiguration extends CmsSearchFieldConfiguration {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSolrFieldConfiguration.class);

    /** The content locale for the indexed document is stored in order to save performance. */
    private Collection<Locale> m_contentLocales;

    /** A list of Solr fields. */
    private Map<String, CmsSolrField> m_solrFields = new HashMap<String, CmsSolrField>();

    /**
     * Default constructor.<p>
     */
    public CmsSolrFieldConfiguration() {

        super();
    }

    /**
     * Adds the additional fields to the configuration, if they are not null.<p>
     *
     * @param additionalFields the additional fields to add
     */
    public void addAdditionalFields(List<CmsSolrField> additionalFields) {

        if (additionalFields != null) {
            for (CmsSolrField solrField : additionalFields) {
                m_solrFields.put(solrField.getName(), solrField);
            }
        }
    }

    /**
     * Returns all configured Solr fields.<p>
     *
     * @return all configured Solr fields
     */
    public Map<String, CmsSolrField> getSolrFields() {

        return Collections.unmodifiableMap(m_solrFields);
    }

    /**
     * @see org.opencms.search.fields.CmsSearchFieldConfiguration#init()
     */
    @Override
    public void init() {

        super.init();
        addAdditionalFields();
    }

    /**
     * @see org.opencms.search.fields.CmsSearchFieldConfiguration#appendAdditionalValuesToDcoument(org.opencms.search.I_CmsSearchDocument, org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.extractors.I_CmsExtractionResult, java.util.List, java.util.List)
     */
    @Override
    protected I_CmsSearchDocument appendAdditionalValuesToDcoument(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        String mimeType = OpenCms.getResourceManager().getMimeType(resource.getName(), null);
        if (mimeType != null) {
            document.addSearchField(m_solrFields.get(CmsSearchField.FIELD_MIMETYPE), mimeType);
        }

        document.addSearchField(m_solrFields.get(CmsSearchField.FIELD_FILENAME), resource.getName());

        document.addSearchField(m_solrFields.get(CmsSearchField.FIELD_VERSION), "" + resource.getVersion());

        try {
            if (CmsResourceTypeXmlContent.isXmlContent(resource)) {
                I_CmsXmlContentHandler handler = CmsXmlContentDefinition.getContentHandlerForResource(cms, resource);
                if ((handler != null) && handler.isContainerPageOnly()) {
                    if (document.getDocument() instanceof SolrInputDocument) {
                        SolrInputDocument doc = (SolrInputDocument)document.getDocument();
                        doc.removeField(CmsSearchField.FIELD_SEARCH_EXCLUDE);
                    } else {
                        //TODO: Warning - but should not happen.
                    }
                    document.addSearchField(m_solrFields.get(CmsSearchField.FIELD_SEARCH_EXCLUDE), "true");
                }
            }
        } catch (CmsException e) {
            LOG.error(e.getMessage(), e);
        }

        List<String> searchExcludeOptions = document.getMultivaluedFieldAsStringList(
            CmsSearchField.FIELD_SEARCH_EXCLUDE);
        if ((searchExcludeOptions == null) || searchExcludeOptions.isEmpty()) {
            document.addSearchField(m_solrFields.get(CmsSearchField.FIELD_SEARCH_EXCLUDE), "false");
        }
        if (resource.getRootPath().startsWith("/system")
            || (CmsResourceTypeJsp.getJSPTypeId() == resource.getTypeId())) {
            document.addSearchField(m_solrFields.get(CmsSearchField.FIELD_SEARCH_CHANNEL), "gallery");
        } else {
            document.addSearchField(m_solrFields.get(CmsSearchField.FIELD_SEARCH_CHANNEL), "content");
        }

        document = appendFieldsForListSortOptions(document);

        document = appendFieldsForListSearch(document, cms, resource);

        if (resource.getRootPath().startsWith(OpenCms.getSiteManager().getSharedFolder())
            || (null != OpenCms.getSiteManager().getSiteRoot(resource.getRootPath()))) {
            appendSpellFields(document);
        }

        return document;
    }

    /**
     * @see org.opencms.search.fields.CmsSearchFieldConfiguration#appendDates(org.opencms.search.I_CmsSearchDocument, org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.extractors.I_CmsExtractionResult, java.util.List, java.util.List)
     */
    @Override
    protected I_CmsSearchDocument appendDates(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        document.addDateField(CmsSearchField.FIELD_DATE_CREATED, resource.getDateCreated(), false);
        document.addDateField(CmsSearchField.FIELD_DATE_LASTMODIFIED, resource.getDateLastModified(), false);
        document.addDateField(CmsSearchField.FIELD_DATE_CONTENT, resource.getDateContent(), false);
        document.addDateField(CmsSearchField.FIELD_DATE_RELEASED, resource.getDateReleased(), false);
        document.addDateField(CmsSearchField.FIELD_DATE_EXPIRED, resource.getDateExpired(), false);

        return document;
    }

    /**
     * @see org.opencms.search.fields.CmsSearchFieldConfiguration#appendFieldMapping(org.opencms.search.I_CmsSearchDocument, org.opencms.search.fields.CmsSearchField, org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.extractors.I_CmsExtractionResult, java.util.List, java.util.List)
     */
    @Override
    protected I_CmsSearchDocument appendFieldMapping(
        I_CmsSearchDocument document,
        CmsSearchField sfield,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        CmsSolrField field = (CmsSolrField)sfield;
        try {
            StringBuffer text = new StringBuffer();
            for (I_CmsSearchFieldMapping mapping : field.getMappings()) {
                // loop over the mappings of the given field
                if (extractionResult != null) {
                    String mapResult = null;
                    if ((field.getLocale() != null) && mapping.getType().equals(CmsSearchFieldMappingType.CONTENT)) {
                        // this is a localized content field, try to retrieve the localized content extraction
                        mapResult = extractionResult.getContent(field.getLocale());
                        if (mapResult == null) {
                            // no localized content extracted
                            if (!(CmsResourceTypeXmlContent.isXmlContent(resource)
                                || CmsResourceTypeXmlPage.isXmlPage(resource))) {
                                // the resource is no XML content nor an XML page
                                if ((m_contentLocales != null) && m_contentLocales.contains(field.getLocale())) {
                                    // the resource to get the extracted content for has the locale of this field,
                                    // so store the extraction content into this field
                                    mapResult = extractionResult.getContent();
                                }
                            }
                        }
                    } else {
                        // this is not a localized content field, just perform the regular mapping
                        mapResult = mapping.getStringValue(
                            cms,
                            resource,
                            extractionResult,
                            properties,
                            propertiesSearched);
                    }
                    if (text.length() > 0) {
                        text.append('\n');
                    }
                    if (mapResult != null) {
                        text.append(mapResult);
                    } else if (mapping.getDefaultValue() != null) {
                        // no mapping result found, but a default is configured
                        text.append(mapping.getDefaultValue());
                    }
                } else if (mapping.getStringValue(
                    cms,
                    resource,
                    extractionResult,
                    properties,
                    propertiesSearched) != null) {
                        String value = mapping.getStringValue(
                            cms,
                            resource,
                            extractionResult,
                            properties,
                            propertiesSearched);
                        if (value != null) {
                            document.addSearchField(field, value);
                        }
                    }
            }
            if ((text.length() <= 0) && (field.getDefaultValue() != null)) {
                text.append(field.getDefaultValue());
            }
            if (text.length() > 0) {
                document.addSearchField(field, text.toString());
            }
        } catch (Exception e) {
            // nothing to do just log
            LOG.error(e.getLocalizedMessage(), e);
        }
        return document;
    }

    /**
     * @see org.opencms.search.fields.CmsSearchFieldConfiguration#appendFieldMappings(org.opencms.search.I_CmsSearchDocument, org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.extractors.I_CmsExtractionResult, java.util.List, java.util.List)
     */
    @Override
    protected I_CmsSearchDocument appendFieldMappings(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        List<String> systemFields = new ArrayList<String>();
        // append field mappings directly stored in the extraction result
        if (null != extractionResult) {
            Map<String, String> fieldMappings = extractionResult.getFieldMappings();
            for (String fieldName : fieldMappings.keySet()) {
                String value = fieldMappings.get(fieldName);
                CmsSolrField f = new CmsSolrField(fieldName, null, null, null);
                document.addSearchField(f, value);
                systemFields.add(fieldName);
            }
        }

        Set<CmsSearchField> mappedFields = getXSDMappings(cms, resource);
        if (mappedFields != null) {
            for (CmsSearchField field : mappedFields) {
                if (!systemFields.contains(field.getName())) {
                    document = appendFieldMapping(
                        document,
                        field,
                        cms,
                        resource,
                        extractionResult,
                        properties,
                        propertiesSearched);
                } else {
                    LOG.error(
                        Messages.get().getBundle().key(
                            Messages.LOG_SOLR_ERR_MAPPING_TO_INTERNALLY_USED_FIELD_2,
                            resource.getRootPath(),
                            field.getName()));
                }
            }
        }

        // add field mappings from elements of a container page
        if (CmsResourceTypeXmlContainerPage.isContainerPage(resource)) {
            document = appendFieldMappingsFromElementsOnThePage(document, cms, resource, systemFields);
        } else {
            try {
                for (CmsResource detailOnlyPage : CmsDetailOnlyContainerUtil.getDetailOnlyResources(cms, resource)) {
                    try {
                        document = appendFieldMappingsFromElementsOnThePage(
                            document,
                            cms,
                            detailOnlyPage,
                            systemFields);
                    } catch (Throwable t) {
                        LOG.warn(
                            Messages.get().getBundle().key(
                                Messages.LOG_SOLR_WARN_DETAIL_ONLY_PAGE_MAPPINGS_FOR_PAGE_2,
                                null == resource ? "null" : resource.getRootPath(),
                                null == detailOnlyPage ? "null" : detailOnlyPage.getRootPath()),
                            t);
                    }
                }
            } catch (Throwable t) {
                LOG.warn(
                    Messages.get().getBundle().key(
                        Messages.LOG_SOLR_WARN_DETAIL_ONLY_PAGE_MAPPINGS_1,
                        null == resource ? "null" : resource.getRootPath()),
                    t);
            }
        }

        for (CmsSolrField field : m_solrFields.values()) {
            document = appendFieldMapping(
                document,
                field,
                cms,
                resource,
                extractionResult,
                properties,
                propertiesSearched);
        }

        return document;
    }

    /**
     * Adds search fields from elements on a container page to a container page's document.
     * @param document The document for the container page
     * @param cms The current CmsObject
     * @param resource The resource of the container page
     * @param systemFields The list of field names for fields where mappings to should be discarded, since these fields are used system internally.
     * @return the manipulated document
     */
    protected I_CmsSearchDocument appendFieldMappingsFromElementsOnThePage(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        List<String> systemFields) {

        try {
            CmsFile file = cms.readFile(resource);
            CmsXmlContainerPage containerPage = CmsXmlContainerPageFactory.unmarshal(cms, file);
            CmsContainerPageBean containerBean = containerPage.getContainerPage(cms);
            if (containerBean != null) {
                for (CmsContainerElementBean element : containerBean.getElements()) {
                    element.initResource(cms);
                    CmsResource elemResource = element.getResource();
                    Set<CmsSearchField> mappedFields = getXSDMappingsForPage(cms, elemResource);
                    if (mappedFields != null) {

                        for (CmsSearchField field : mappedFields) {
                            if (!systemFields.contains(field.getName())) {
                                try {
                                    I_CmsExtractionResult extractionResult = CmsSolrDocumentXmlContent.extractXmlContent(
                                        cms,
                                        elemResource,
                                        getIndex());
                                    document = appendFieldMapping(
                                        document,
                                        field,
                                        cms,
                                        elemResource,
                                        extractionResult,
                                        cms.readPropertyObjects(resource, false),
                                        cms.readPropertyObjects(resource, true));
                                } catch (Exception e) {
                                    LOG.error(
                                        Messages.get().getBundle().key(
                                            Messages.LOG_SOLR_ERR_MAPPING_UNREADABLE_CONTENT_3,
                                            elemResource.getRootPath(),
                                            field.getName(),
                                            resource.getRootPath()),
                                        e);
                                }
                            } else {
                                LOG.error(
                                    Messages.get().getBundle().key(
                                        Messages.LOG_SOLR_ERR_MAPPING_TO_INTERNALLY_USED_FIELD_3,
                                        elemResource.getRootPath(),
                                        field.getName(),
                                        resource.getRootPath()));
                            }
                        }
                    }
                }
            }
        } catch (CmsException e) {
            // Should be thrown if element on the page does not exist anymore - this is possible, but not necessarily an error.
            // Hence, just notice it in the debug log.
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }
        return document;
    }

    /**
     * @see org.opencms.search.fields.CmsSearchFieldConfiguration#appendLocales(org.opencms.search.I_CmsSearchDocument, org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.extractors.I_CmsExtractionResult, java.util.List, java.util.List)
     */
    @Override
    protected I_CmsSearchDocument appendLocales(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extraction,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        // append the resource locales
        Collection<Locale> resourceLocales = new ArrayList<Locale>();
        if ((extraction != null) && (!extraction.getLocales().isEmpty())) {

            CmsResourceManager resMan = OpenCms.getResourceManager();
            resourceLocales = extraction.getLocales();
            boolean isGroup = false;
            for (String groupType : Arrays.asList(
                CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_NAME,
                CmsResourceTypeXmlContainerPage.INHERIT_CONTAINER_TYPE_NAME)) {
                if (resMan.matchResourceType(groupType, resource.getTypeId())) {
                    isGroup = true;
                    break;
                }
            }
            if (isGroup) {
                // groups are locale independent, so they have to have *all* locales so they are found for each one
                m_contentLocales = OpenCms.getLocaleManager().getAvailableLocales();
            } else {
                m_contentLocales = resourceLocales;
            }
        } else {
            // For all other resources add all default locales
            resourceLocales = OpenCms.getLocaleManager().getDefaultLocales(cms, resource);

            /*
             * A problem is likely to arise when dealing with multilingual fields:
             * Only values extracted from XML resources are written into the Solr locale-aware fields (e.g.
             * "title_<locale>_s"), therefore sorting by them will not work as non-XML (unilingual) resources extract
             * the information by the resource property facility and will not write to an Solr locale-aware field.
             *
             * The following code is used to fix this behavior, at least for "Title".
             */

            // Check all passed properties for "Title"...
            for (final CmsProperty prop : propertiesSearched) {
                if (prop.getName().equals(CmsPropertyDefinition.PROPERTY_TITLE)) {
                    final String value = prop.getValue();

                    // Write a Solr locale-aware field for every locale the system supports...
                    final List<Locale> availableLocales = OpenCms.getLocaleManager().getAvailableLocales();
                    for (final Locale locale : availableLocales) {
                        final String lang = locale.getLanguage();
                        // Don't proceed if a field has already written for this locale.
                        if (!resourceLocales.contains(lang)) {
                            final String effFieldName = CmsSearchFieldConfiguration.getLocaleExtendedName(
                                CmsSearchField.FIELD_TITLE_UNSTORED,
                                locale) + "_s";

                            final CmsSolrField f = new CmsSolrField(effFieldName, null, null, null);
                            document.addSearchField(f, value);
                        }
                    }
                }
            }
            m_contentLocales = getContentLocales(cms, resource, extraction);
        }

        document.addResourceLocales(resourceLocales);
        document.addContentLocales(m_contentLocales);

        // append document dependencies if configured
        if (hasLocaleDependencies()) {
            CmsDocumentDependency dep = CmsDocumentDependency.load(cms, resource);
            ((CmsSolrDocument)document).addDocumentDependency(cms, dep);
        }
        return document;
    }

    /**
     * @see org.opencms.search.fields.CmsSearchFieldConfiguration#appendProperties(org.opencms.search.I_CmsSearchDocument, org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.extractors.I_CmsExtractionResult, java.util.List, java.util.List)
     */
    @Override
    protected I_CmsSearchDocument appendProperties(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extraction,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        for (CmsProperty prop : propertiesSearched) {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(prop.getValue())) {
                String value = CmsSearchUtil.stripHtmlFromPropertyIfNecessary(prop.getName(), prop.getValue());
                document.addSearchField(
                    new CmsSolrField(prop.getName() + CmsSearchField.FIELD_DYNAMIC_PROPERTIES, null, null, null),
                    value);

                // Also write the property using the dynamic field '_s' in order to prevent tokenization
                // of the property. The resulting field is named '<property>_prop_s'.
                document.addSearchField(
                    new CmsSolrField(prop.getName() + CmsSearchField.FIELD_DYNAMIC_PROPERTIES + "_s", null, null, null),
                    value);
            }
        }

        for (CmsProperty prop : properties) {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(prop.getValue())) {
                String value = CmsSearchUtil.stripHtmlFromPropertyIfNecessary(prop.getName(), prop.getValue());
                document.addSearchField(
                    new CmsSolrField(prop.getName() + CmsSearchField.FIELD_DYNAMIC_PROPERTIES_DIRECT, null, null, null),
                    value);

                // Also write the property using the dynamic field '_s' in order to prevent tokenization
                // of the property. The resulting field is named '<property>_prop_nosearch_s'.
                document.addSearchField(
                    new CmsSolrField(
                        prop.getName() + CmsSearchField.FIELD_DYNAMIC_PROPERTIES_DIRECT + "_s",
                        null,
                        null,
                        null),
                    value);
            }
        }
        return document;
    }

    /**
     * Retrieves the locales for an content, that is whether an XML content nor an XML page.<p>
     *
     * Uses following strategy:
     * <ul>
     * <li>first by file name</li>
     * <li>then by detection and</li>
     * <li>otherwise take the first configured default locale for this resource</li>
     * </ul>
     *
     * @param cms the current CmsObject
     * @param resource the resource to get the content locales for
     * @param extraction the extraction result
     *
     * @return the determined locales for the given resource
     */
    protected List<Locale> getContentLocales(CmsObject cms, CmsResource resource, I_CmsExtractionResult extraction) {

        // try to detect locale by filename
        Locale detectedLocale = CmsStringUtil.getLocaleForName(resource.getRootPath());
        if (!OpenCms.getLocaleManager().getAvailableLocales(cms, resource).contains(detectedLocale)) {
            detectedLocale = null;
        }
        // try to detect locale by language detector
        if (getIndex().isLanguageDetection()
            && (detectedLocale == null)
            && (extraction != null)
            && (extraction.getContent() != null)) {
            detectedLocale = CmsStringUtil.getLocaleForText(extraction.getContent());
        }
        // take the detected locale or use the first configured default locale for this resource
        List<Locale> result = new ArrayList<Locale>();
        if (detectedLocale != null) {
            // take the found locale
            result.add(detectedLocale);
        } else {

            // take all locales set via locale-available or the configured default locales as fall-back for this resource
            result.addAll(OpenCms.getLocaleManager().getAvailableLocales(cms, resource));
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_LANGUAGE_DETECTION_FAILED_1, resource));
        }
        return result;
    }

    /**
     * Returns the search field mappings declared within the XSD.<p>
     *
     * @param cms the CmsObject
     * @param resource the resource
     *
     * @return the fields to map
     */
    protected Set<CmsSearchField> getXSDMappings(CmsObject cms, CmsResource resource) {

        try {
            if (CmsResourceTypeXmlContent.isXmlContent(resource)) {
                I_CmsXmlContentHandler handler = CmsXmlContentDefinition.getContentHandlerForResource(cms, resource);
                if ((handler != null) && !handler.getSearchFields().isEmpty()) {
                    return handler.getSearchFields();
                }
            }
        } catch (CmsException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Returns the search field mappings declared within the XSD that should be applied to the container page.<p>
     *
     * @param cms the CmsObject
     * @param resource the resource
     *
     * @return the fields to map
     */
    protected Set<CmsSearchField> getXSDMappingsForPage(CmsObject cms, CmsResource resource) {

        try {
            if (CmsResourceTypeXmlContent.isXmlContent(resource)) {
                I_CmsXmlContentHandler handler = CmsXmlContentDefinition.getContentHandlerForResource(cms, resource);
                if ((handler != null) && !handler.getSearchFieldsForPage().isEmpty()) {
                    return handler.getSearchFieldsForPage();
                }
            }
        } catch (CmsException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Adds additional fields to this field configuration.<p>
     */
    private void addAdditionalFields() {

        /*
         * Add fields from opencms-search.xml (Lucene fields)
         */
        for (CmsSearchField field : getFields()) {
            if (field instanceof CmsLuceneField) {
                CmsSolrField newSolrField = new CmsSolrField((CmsLuceneField)field);
                m_solrFields.put(newSolrField.getName(), newSolrField);
            }
        }

        /*
         * Add the content fields (multiple for contents with more than one locale)
         */
        // add the content_<locale> fields to this configuration
        CmsSolrField solrField = new CmsSolrField(CmsSearchField.FIELD_CONTENT, null, null, null);
        solrField.addMapping(
            new CmsSearchFieldMapping(CmsSearchFieldMappingType.CONTENT, CmsSearchField.FIELD_CONTENT));
        m_solrFields.put(solrField.getName(), solrField);
        for (Locale locale : OpenCms.getLocaleManager().getAvailableLocales()) {
            solrField = new CmsSolrField(
                CmsSearchFieldConfiguration.getLocaleExtendedName(CmsSearchField.FIELD_CONTENT, locale),
                Collections.singletonList(locale.toString() + CmsSearchField.FIELD_EXCERPT),
                locale,
                null);
            solrField.addMapping(
                new CmsSearchFieldMapping(CmsSearchFieldMappingType.CONTENT, CmsSearchField.FIELD_CONTENT));
            m_solrFields.put(solrField.getName(), solrField);
        }

        /*
         * Fields filled within appendFields
         */
        CmsSolrField sfield = new CmsSolrField(CmsSearchField.FIELD_MIMETYPE, null, null, null);
        m_solrFields.put(sfield.getName(), sfield);

        sfield = new CmsSolrField(CmsSearchField.FIELD_FILENAME, null, null, null);
        m_solrFields.put(sfield.getName(), sfield);

        sfield = new CmsSolrField(CmsSearchField.FIELD_VERSION, null, null, null);
        m_solrFields.put(sfield.getName(), sfield);

        sfield = new CmsSolrField(CmsSearchField.FIELD_SEARCH_CHANNEL, null, null, null);
        m_solrFields.put(sfield.getName(), sfield);

        /*
         * Fields with mapping
         */
        sfield = new CmsSolrField(CmsSearchField.FIELD_STATE, null, null, null);
        CmsSearchFieldMapping map = new CmsSearchFieldMapping(
            CmsSearchFieldMappingType.ATTRIBUTE,
            CmsSearchField.FIELD_STATE);
        sfield.addMapping(map);
        m_solrFields.put(sfield.getName(), sfield);

        sfield = new CmsSolrField(CmsSearchField.FIELD_USER_LAST_MODIFIED, null, null, null);
        map = new CmsSearchFieldMapping(CmsSearchFieldMappingType.ATTRIBUTE, CmsSearchField.FIELD_USER_LAST_MODIFIED);
        sfield.addMapping(map);
        m_solrFields.put(sfield.getName(), sfield);

        sfield = new CmsSolrField(CmsSearchField.FIELD_USER_CREATED, null, null, null);
        map = new CmsSearchFieldMapping(CmsSearchFieldMappingType.ATTRIBUTE, CmsSearchField.FIELD_USER_CREATED);
        sfield.addMapping(map);
        m_solrFields.put(sfield.getName(), sfield);

        sfield = new CmsSolrField(CmsSearchField.FIELD_META, null, null, null);
        map = new CmsSearchFieldMapping(CmsSearchFieldMappingType.PROPERTY, CmsPropertyDefinition.PROPERTY_TITLE);
        sfield.addMapping(map);
        map = new CmsSearchFieldMapping(CmsSearchFieldMappingType.PROPERTY, CmsPropertyDefinition.PROPERTY_DESCRIPTION);
        sfield.addMapping(map);
        map = new CmsSearchFieldMapping(CmsSearchFieldMappingType.ATTRIBUTE, I_CmsXmlConfiguration.A_NAME);
        sfield.addMapping(map);
        m_solrFields.put(sfield.getName(), sfield);

        sfield = new CmsSolrField(CmsSearchField.FIELD_SEARCH_EXCLUDE, null, null, null);
        map = new CmsSearchFieldMapping(
            CmsSearchFieldMappingType.PROPERTY_SEARCH,
            CmsPropertyDefinition.PROPERTY_SEARCH_EXCLUDE);
        sfield.addMapping(map);
        m_solrFields.put(sfield.getName(), sfield);

    }

    /**
     * Adds multiple fields to the document that are used to search in by the list app.
     *
     * <p>The fields are:
     * <ul>
     *  <li>description_{locale}</li>
     *  <li>keywords_{locale}</li>
     * </ul>
     * for each of the locales the document is available in.</p>
     *
     * @param document the document to index with all other fields already added.
     * @param cms the current context
     * @param resource the resource that is indexed
     * @param properties the direct properties of the resource
     * @return the document extended by the fields used by the list.
     */

    private I_CmsSearchDocument appendFieldsForListSearch(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource) {

        List<String> locales = document.getMultivaluedFieldAsStringList(CmsSearchField.FIELD_CONTENT_LOCALES);
        for (String locale : locales) {
            fillLocalizedFieldWithPropertyFallbacks(
                cms,
                document,
                resource,
                locale,
                CmsSearchField.FIELD_DESCRIPTION,
                CmsPropertyDefinition.PROPERTY_DESCRIPTION);
            fillLocalizedFieldWithPropertyFallbacks(
                cms,
                document,
                resource,
                locale,
                CmsSearchField.FIELD_KEYWORDS,
                CmsPropertyDefinition.PROPERTY_KEYWORDS);
        }

        return document;

    }

    /**
     * Adds multiple fields to the document that are used for the sort options in the list app.
     *
     * <p>The fields are:
     * <ul>
     *  <li>instancedate_dt</li>
     *  <li>instancedatecurrenttill_dt</li>
     *  <li>instancedaterange_dr</li>
     *  <li>disptitle_s</li>
     *  <li>disporder_i</li>
     * </ul>
     * and localized versions for each content locale.</p>
     *
     * @param document the document to index with all other fields already added.
     * @return the document extended by the fields used by the list.
     */
    private I_CmsSearchDocument appendFieldsForListSortOptions(I_CmsSearchDocument document) {

        // add non-localized fields
        // add instance date
        String fieldName = CmsSearchField.FIELD_INSTANCEDATE + CmsSearchField.FIELD_POSTFIX_DATE;
        Date instanceDate = document.getFieldValueAsDate(fieldName);
        if ((null == instanceDate) || (instanceDate.getTime() == 0)) {
            String instanceDateCopyField = document.getFieldValueAsString(
                CmsPropertyDefinition.PROPERTY_INSTANCEDATE_COPYFIELD + CmsSearchField.FIELD_DYNAMIC_PROPERTIES);
            if (null != instanceDateCopyField) {
                instanceDate = document.getFieldValueAsDate(instanceDateCopyField);
            }
            if ((null == instanceDate) || (instanceDate.getTime() == 0)) {
                instanceDate = document.getFieldValueAsDate(CmsSearchField.FIELD_DATE_RELEASED);
            }
            if ((null == instanceDate) || (instanceDate.getTime() == 0)) {
                instanceDate = document.getFieldValueAsDate(CmsSearchField.FIELD_DATE_LASTMODIFIED);
            }
            document.addDateField(fieldName, instanceDate.getTime(), false);
        }
        // Set instancedaterange_dr
        fieldName = CmsSearchField.FIELD_INSTANCEDATE_RANGE + CmsSearchField.FIELD_POSTFIX_DATE_RANGE;
        String instanceDateString = document.getFieldValueAsString(
            CmsSearchField.FIELD_INSTANCEDATE + CmsSearchField.FIELD_POSTFIX_DATE);
        String instanceDateRangeString = "[" + instanceDateString + " TO " + instanceDateString + "]";
        ((SolrInputDocument)document.getDocument()).setField(fieldName, instanceDateRangeString);
        // Set instancedatecurrenttill_dt to instancedate_dt if not set yet
        fieldName = CmsSearchField.FIELD_INSTANCEDATE_CURRENT_TILL + CmsSearchField.FIELD_POSTFIX_DATE;
        Date instanceDateCurrentTill = document.getFieldValueAsDate(fieldName);
        if ((null == instanceDateCurrentTill) || (instanceDateCurrentTill.getTime() == 0)) {
            document.addDateField(fieldName, instanceDate.getTime(), false);
        }
        // add disp-title field
        fieldName = CmsSearchField.FIELD_DISPTITLE + CmsSearchField.FIELD_POSTFIX_SORT;
        String dispTitle = document.getFieldValueAsString(fieldName);
        if (null == dispTitle) {
            dispTitle = document.getFieldValueAsString(
                CmsPropertyDefinition.PROPERTY_TITLE + CmsSearchField.FIELD_DYNAMIC_PROPERTIES_DIRECT);
            if (null == dispTitle) {
                dispTitle = document.getFieldValueAsString(CmsSearchField.FIELD_FILENAME);
            }
            document.addSearchField(new CmsSolrField(fieldName, null, null, null), dispTitle);
        }

        // add disp-order field
        fieldName = CmsSearchField.FIELD_DISPORDER + CmsSearchField.FIELD_POSTFIX_INT;
        String dispOrder = document.getFieldValueAsString(fieldName);
        if (null == dispOrder) {
            dispOrder = document.getFieldValueAsString(
                CmsPropertyDefinition.PROPERTY_DISPLAY_ORDER + CmsSearchField.FIELD_DYNAMIC_PROPERTIES);
            if (null != dispOrder) {
                try {
                    int o = Integer.parseInt(dispOrder);
                    dispOrder = String.valueOf(o);
                } catch (NullPointerException | NumberFormatException e) {
                    LOG.warn(
                        "Property "
                            + CmsPropertyDefinition.PROPERTY_DISPLAY_ORDER
                            + " contains not a valid integer number.");
                    dispOrder = "0";
                }
            } else {
                dispOrder = "0";
            }
            document.addSearchField(new CmsSolrField(fieldName, null, null, null), dispOrder);
        }

        // add localized fields
        for (String locale : document.getMultivaluedFieldAsStringList(CmsSearchField.FIELD_CONTENT_LOCALES)) {
            // instance date
            fieldName = CmsSearchField.FIELD_INSTANCEDATE + "_" + locale + CmsSearchField.FIELD_POSTFIX_DATE;
            Date localeInstanceDate = document.getFieldValueAsDate(fieldName);
            if ((null == localeInstanceDate) || (localeInstanceDate.getTime() == 0)) {
                localeInstanceDate = instanceDate;
                document.addDateField(fieldName, localeInstanceDate.getTime(), false);
            }
            // instance date range
            fieldName = CmsSearchField.FIELD_INSTANCEDATE_RANGE
                + "_"
                + locale
                + CmsSearchField.FIELD_POSTFIX_DATE_RANGE;
            String localeInstanceDateString = document.getFieldValueAsString(
                CmsSearchField.FIELD_INSTANCEDATE + "_" + locale + CmsSearchField.FIELD_POSTFIX_DATE);
            String localeInstanceDateRangeString = "["
                + localeInstanceDateString
                + " TO "
                + localeInstanceDateString
                + "]";
            ((SolrInputDocument)document.getDocument()).setField(fieldName, localeInstanceDateRangeString);
            // Set instancedatecurrenttill_dt to instancedate_dt if not set yet
            fieldName = CmsSearchField.FIELD_INSTANCEDATE_CURRENT_TILL
                + "_"
                + locale
                + CmsSearchField.FIELD_POSTFIX_DATE;
            Date localeInstanceDateCurrentTill = document.getFieldValueAsDate(fieldName);
            if ((null == localeInstanceDateCurrentTill) || (localeInstanceDateCurrentTill.getTime() == 0)) {
                document.addDateField(fieldName, localeInstanceDate.getTime(), false);
            }
            // disp-title field for title display and sorting
            fieldName = CmsSearchField.FIELD_DISPTITLE + "_" + locale + CmsSearchField.FIELD_POSTFIX_SORT;
            if (null == document.getFieldValueAsString(fieldName)) {
                String localizedTitle = document.getFieldValueAsString(
                    CmsPropertyDefinition.PROPERTY_TITLE
                        + "_"
                        + locale
                        + CmsSearchField.FIELD_DYNAMIC_PROPERTIES_DIRECT);
                document.addSearchField(
                    new CmsSolrField(fieldName, null, null, null),
                    null == localizedTitle ? dispTitle : localizedTitle);
            }
            // disp-order field
            fieldName = CmsSearchField.FIELD_DISPORDER + "_" + locale + CmsSearchField.FIELD_POSTFIX_INT;
            if (null == document.getFieldValueAsString(fieldName)) {
                String localizedOrder = document.getFieldValueAsString(
                    CmsPropertyDefinition.PROPERTY_DISPLAY_ORDER
                        + "_"
                        + locale
                        + CmsSearchField.FIELD_DYNAMIC_PROPERTIES);
                if (null != localizedOrder) {
                    try {
                        int o = Integer.parseInt(localizedOrder);
                        localizedOrder = String.valueOf(o);
                    } catch (NullPointerException | NumberFormatException e) {
                        LOG.warn(
                            "Property "
                                + CmsPropertyDefinition.PROPERTY_DISPLAY_ORDER
                                + "_"
                                + locale
                                + " contains not a valid integer number.");
                    }
                }
                document.addSearchField(
                    new CmsSolrField(fieldName, null, null, null),
                    null == localizedOrder ? dispOrder : localizedOrder);
            }
        }

        return document;
    }

    /**
     * Copy the content and the title property of the document to a spell field / a language specific spell field.
     * @param document the document that gets extended by the spell fields.
     */
    private void appendSpellFields(I_CmsSearchDocument document) {

        /*
         * Add the content fields (multiple for contents with more than one locale)
         */
        // add the content_<locale> fields to this configuration
        String title = document.getFieldValueAsString(
            CmsPropertyDefinition.PROPERTY_TITLE + CmsSearchField.FIELD_DYNAMIC_PROPERTIES_DIRECT);
        document.addSearchField(
            new CmsSolrField(CmsSearchField.FIELD_SPELL, null, null, null),
            document.getFieldValueAsString(CmsSearchField.FIELD_CONTENT) + "\n" + title);
        for (Locale locale : OpenCms.getLocaleManager().getAvailableLocales()) {
            document.addSearchField(
                new CmsSolrField(locale + "_" + CmsSearchField.FIELD_SPELL, null, locale, null),
                document.getFieldValueAsString(
                    CmsSearchFieldConfiguration.getLocaleExtendedName(CmsSearchField.FIELD_CONTENT, locale))
                    + "\n"
                    + title);
        }
    }

    /**
     * Fills the field with the name extended by "_{locale}" with the property value, if the field is not already present in the document.
     *
     * If the localized property is not present, the default property value is used to get the value.
     * Properties set on the indexed resource itself are always preferred. If no suitable property is present, the parent properties are used when given.
     *
     * @param cms the current context
     * @param document the document to add the field to.
     * @param resource the currently indexed resource.
     * @param locale the locale to add the field for.
     * @param fieldName the name of the field to add (without locale postfix)
     * @param propertyName the property name of the property to get the value from (without locale postfix)
     */
    private void fillLocalizedFieldWithPropertyFallbacks(
        CmsObject cms,
        I_CmsSearchDocument document,
        CmsResource resource,
        String locale,
        String fieldName,
        String propertyName) {

        Locale l = CmsLocaleManager.getLocale(locale);
        String localeExtendedFieldName = getLocaleExtendedName(fieldName, locale);

        if (!document.getFieldNames().contains(localeExtendedFieldName)) {
            String value = CmsVfsUtil.readPropertyValueWithFolderFallbackForDefaultFiles(
                cms,
                resource,
                propertyName,
                l);
            if (value != null) {
                document.addSearchField(new CmsSolrField(localeExtendedFieldName, null, null, null), value);
            }
        }

    }

    /**
     * Returns <code>true</code> if at least one of the index sources uses a VFS indexer that is able
     * to index locale dependent resources.<p>
     *
     * TODO This should be improved somehow
     *
     * @return <code>true</code> if this field configuration should resolve locale dependencies
     */
    private boolean hasLocaleDependencies() {

        for (CmsSearchIndexSource source : getIndex().getSources()) {
            if (source.getIndexer().isLocaleDependenciesEnable()) {
                return true;
            }
        }
        return false;
    }
}

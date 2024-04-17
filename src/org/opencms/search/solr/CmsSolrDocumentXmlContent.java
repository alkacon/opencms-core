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

import org.opencms.acacia.shared.I_CmsSerialDateValue;
import org.opencms.ade.containerpage.CmsDetailOnlyContainerUtil;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsIndexException;
import org.opencms.search.CmsSearchUtil;
import org.opencms.search.I_CmsSearchIndex;
import org.opencms.search.documents.A_CmsVfsDocument;
import org.opencms.search.documents.CmsIndexNoContentException;
import org.opencms.search.documents.Messages;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.search.galleries.CmsGalleryNameMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.widgets.serialdate.CmsSerialDateBeanFactory;
import org.opencms.widgets.serialdate.CmsSerialDateValue;
import org.opencms.widgets.serialdate.I_CmsSerialDateBean;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.I_CmsXmlContentHandler;
import org.opencms.xml.types.CmsXmlDateTimeValue;
import org.opencms.xml.types.CmsXmlHtmlValue;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.CmsXmlSerialDateValue;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.logging.Log;

import com.google.common.collect.Sets;

/**
 * Special document text extraction factory for Solr index.<p>
 *
 * @since 8.5.0
 */
public class CmsSolrDocumentXmlContent extends A_CmsVfsDocument {

    /**
     * The gallery name is determined by resolving the macros in a string which can either come from a field mapped
     * to the gallery name, or the title, or from default values for those fields. This class is used to select the
     * value to use and performs the macro substitution.
     */
    private static class GalleryNameChooser {

        /** CMS context for this instance. */
        private CmsObject m_cms;

        /** Current XML content. */
        private A_CmsXmlDocument m_content;

        /** Default value of field mapped to gallery name. */
        private String m_defaultGalleryNameValue;

        /** Default value of field mapped to title. */
        private String m_defaultTitleValue;

        /** Current locale. */
        private Locale m_locale;

        /** Content value mapped to Description property. */
        private String m_mappedDescriptionValue;

        /** Content value mapped to gallery description. */
        private String m_mappedGalleryDescriptionValue;

        /** Content value mapped to gallery name. */
        private String m_mappedGalleryNameValue;

        /** Content value mapped to title. */
        private String m_mappedTitleValue;

        /**
         * Creates a new instance.<p>
         *
         * @param cms the CMS context
         * @param content the XML content
         * @param locale the locale in the XML content
         */
        public GalleryNameChooser(CmsObject cms, A_CmsXmlDocument content, Locale locale) {

            m_cms = cms;
            m_content = content;
            m_locale = locale;
        }

        /**
         * Selects the description displayed in the gallery.<p>
         *
         * This method assumes that all the available values have been set via the setters of this class.
         *
         * @return the description
         *
         * @throws CmsException of something goes wrong
         */
        public String getDescription() throws CmsException {

            return getDescription(m_locale);
        }

        /**
            * Selects the description displayed in the gallery.<p>
            *
            * This method assumes that all the available values have been set via the setters of this class.
            *
            * @param locale the locale to get the description in
            *
            * @return the description
            *
            * @throws CmsException of something goes wrong
            */
        public String getDescription(Locale locale) throws CmsException {

            String result = null;
            for (String resultCandidateWithMacros : new String[] {
                m_mappedGalleryDescriptionValue,
                m_mappedDescriptionValue}) {
                if (!CmsStringUtil.isEmptyOrWhitespaceOnly(resultCandidateWithMacros)) {
                    CmsGalleryNameMacroResolver resolver = new CmsGalleryNameMacroResolver(m_cms, m_content, locale);
                    result = resolver.resolveMacros(resultCandidateWithMacros);
                    return result;
                }
            }
            result = m_cms.readPropertyObject(
                m_content.getFile(),
                CmsPropertyDefinition.PROPERTY_DESCRIPTION,
                false).getValue();
            return result;
        }

        /**
         * Selects the gallery name.<p>
         *
         * This method assumes that all the available values have been set via the setters of this class.
         *
         * @return the gallery name
         *
         * @throws CmsException of something goes wrong
         */
        public String getGalleryName() throws CmsException {

            return getGalleryName(m_locale);
        }

        /**
        * Selects the gallery name.<p>
        *
        * This method assumes that all the available values have been set via the setters of this class.
        *
        * @param locale the locale to get the gallery name in
        *
        * @return the gallery name
        *
        * @throws CmsException of something goes wrong
        */
        public String getGalleryName(Locale locale) throws CmsException {

            String result = null;
            for (String resultCandidateWithMacros : new String[] {
                // Prioritize gallery name over title, and actual content values over defaults
                m_mappedGalleryNameValue,
                m_defaultGalleryNameValue,
                m_mappedTitleValue,
                m_defaultTitleValue}) {
                if (!CmsStringUtil.isEmptyOrWhitespaceOnly(resultCandidateWithMacros)) {
                    CmsGalleryNameMacroResolver resolver = new CmsGalleryNameMacroResolver(m_cms, m_content, locale);
                    result = resolver.resolveMacros(resultCandidateWithMacros);
                    return result;
                }
            }
            result = m_cms.readPropertyObject(
                m_content.getFile(),
                CmsPropertyDefinition.PROPERTY_TITLE,
                false).getValue();
            return result;
        }

        /**
         * Sets the defaultGalleryNameValue.<p>
         *
         * @param defaultGalleryNameValue the defaultGalleryNameValue to set
         */
        public void setDefaultGalleryNameValue(String defaultGalleryNameValue) {

            m_defaultGalleryNameValue = defaultGalleryNameValue;
        }

        /**
         * Sets the defaultTitleValue.<p>
         *
         * @param defaultTitleValue the defaultTitleValue to set
         */
        public void setDefaultTitleValue(String defaultTitleValue) {

            m_defaultTitleValue = defaultTitleValue;
        }

        /**
         * Sets the mapped description value.<p>
         *
         * @param mappedDescriptionValue the mappedDescriptionValue to set
         */
        public void setMappedDescriptionValue(String mappedDescriptionValue) {

            m_mappedDescriptionValue = mappedDescriptionValue;
        }

        /**
         * Sets the name from a value mapped via 'galleryDescription'.
         *
         * @param mappedGalleryDescriptionValue the value that has been mapped
         */
        public void setMappedGalleryDescriptionValue(String mappedGalleryDescriptionValue) {

            m_mappedGalleryDescriptionValue = mappedGalleryDescriptionValue;
        }

        /**
         * Sets the mappedGalleryNameValue.<p>
         *
         * @param mappedGalleryNameValue the mappedGalleryNameValue to set
         */
        public void setMappedGalleryNameValue(String mappedGalleryNameValue) {

            m_mappedGalleryNameValue = mappedGalleryNameValue;
        }

        /**
         * Sets the mappedTitleValue.<p>
         *
         * @param mappedTitleValue the mappedTitleValue to set
         */
        public void setMappedTitleValue(String mappedTitleValue) {

            m_mappedTitleValue = mappedTitleValue;
        }
    }

    /** Mapping name used to indicate that the value should be used for the gallery description. */
    public static final String MAPPING_GALLERY_DESCRIPTION = "galleryDescription";

    /** Mapping name used to indicate that the value should be used for the gallery name. */
    public static final String MAPPING_GALLERY_NAME = "galleryName";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSolrDocumentXmlContent.class);

    /**
     * Public constructor.<p>
     *
     * @param name the name for the document type
     */
    public CmsSolrDocumentXmlContent(String name) {

        super(name);
    }

    /**
     * Collects a list of all possible XPaths for a content definition.<p>
     *
     * @param cms the CMS context to use
     * @param def the content definition
     * @param path the path of the given content definition
     * @param result the set used to collect the XPaths
     */
    public static void collectSchemaXpathsForSimpleValues(
        CmsObject cms,
        CmsXmlContentDefinition def,
        String path,
        Set<String> result) {

        List<I_CmsXmlSchemaType> nestedTypes = def.getTypeSequence();
        for (I_CmsXmlSchemaType nestedType : nestedTypes) {
            String subPath = path + "/" + nestedType.getName();
            if (nestedType instanceof CmsXmlNestedContentDefinition) {
                CmsXmlContentDefinition nestedDef = ((CmsXmlNestedContentDefinition)nestedType).getNestedContentDefinition();
                collectSchemaXpathsForSimpleValues(cms, nestedDef, subPath, result);
            } else {
                result.add(subPath);
            }
        }
    }

    /**
     * Extracts the content of a single XML content resource.<p>
     *
     * @param cms the cms context
     * @param resource the resource
     * @param index the used index
     *
     * @return the extraction result
     *
     * @throws CmsException in case reading or unmarshalling the content fails
     */
    public static CmsExtractionResult extractXmlContent(CmsObject cms, CmsResource resource, I_CmsSearchIndex index)
    throws CmsException {

        return extractXmlContent(cms, resource, index, null);
    }

    /**
     * Extracts the content of a single XML content resource.<p>
     *
     * @param cms the cms context
     * @param resource the resource
     * @param index the used index
     * @param forceLocale if set, only the content values for the given locale will be extracted
     *
     * @return the extraction result
     *
     * @throws CmsException in case reading or unmarshalling the content fails
     */
    public static CmsExtractionResult extractXmlContent(
        CmsObject cms,
        CmsResource resource,
        I_CmsSearchIndex index,
        Locale forceLocale)
    throws CmsException {

        return extractXmlContent(cms, resource, index, forceLocale, null);
    }

    /**
     * Extracts the content of a single XML content resource.<p>
     *
     * @param cms the cms context
     * @param resource the resource
     * @param index the used index
     * @param forceLocale if set, only the content values for the given locale will be extracted
     * @param alreadyExtracted keeps track of ids of contents which have already been extracted
     *
     * @return the extraction result
     *
     * @throws CmsException in case reading or unmarshalling the content fails
     */
    public static CmsExtractionResult extractXmlContent(
        CmsObject cms,
        CmsResource resource,
        I_CmsSearchIndex index,
        Locale forceLocale,
        Set<CmsUUID> alreadyExtracted)
    throws CmsException {

        return extractXmlContent(
            cms,
            resource,
            index,
            forceLocale,
            alreadyExtracted,
            content -> {/*do nothing with the content*/});

    }

    /**
     * Extracts the content of a single XML content resource.<p>
     *
     * @param cms the cms context
     * @param resource the resource
     * @param index the used index
     * @param forceLocale if set, only the content values for the given locale will be extracted
     * @param alreadyExtracted keeps track of ids of contents which have already been extracted
     * @param contentConsumer gets called with the unmarshalled content object
     *
     * @return the extraction result
     *
     * @throws CmsException in case reading or unmarshalling the content fails
     */
    public static CmsExtractionResult extractXmlContent(
        CmsObject cms,
        CmsResource resource,
        I_CmsSearchIndex index,
        Locale forceLocale,
        Set<CmsUUID> alreadyExtracted,
        Consumer<A_CmsXmlDocument> contentConsumer)
    throws CmsException {

        if (null == alreadyExtracted) {
            alreadyExtracted = Collections.emptySet();
        }
        // un-marshal the content
        CmsFile file = cms.readFile(resource);
        if (file.getLength() <= 0) {
            throw new CmsIndexNoContentException(
                Messages.get().container(Messages.ERR_NO_CONTENT_1, resource.getRootPath()));
        }
        A_CmsXmlDocument xmlContent = CmsXmlContentFactory.unmarshal(cms, file);
        if (contentConsumer != null) {
            contentConsumer.accept(xmlContent);
        }

        // initialize some variables
        Map<Locale, LinkedHashMap<String, String>> items = new HashMap<Locale, LinkedHashMap<String, String>>();
        Map<String, String> fieldMappings = new HashMap<String, String>();
        List<Locale> contentLocales = forceLocale != null
        ? Collections.singletonList(forceLocale)
        : xmlContent.getLocales();
        Locale resourceLocale = index.getLocaleForResource(cms, resource, contentLocales);

        LinkedHashMap<String, String> localeItems = null;
        GalleryNameChooser galleryNameChooser = null;
        // loop over the locales of the content
        for (Locale locale : contentLocales) {
            galleryNameChooser = new GalleryNameChooser(cms, xmlContent, locale);
            localeItems = new LinkedHashMap<String, String>();
            StringBuffer textContent = new StringBuffer();
            // store the locales of the content as space separated field
            // loop over the available element paths of the current content locale
            List<String> paths = xmlContent.getNames(locale);
            for (String xpath : paths) {

                // try to get the value extraction for the current element path
                String extracted = null;
                I_CmsXmlContentValue value = xmlContent.getValue(xpath, locale);
                try {
                    //the new DatePointField.createField dose not support milliseconds
                    if (value instanceof CmsXmlDateTimeValue) {
                        extracted = CmsSearchUtil.getDateAsIso8601(((CmsXmlDateTimeValue)value).getDateTimeValue());
                    } else {
                        extracted = value.getPlainText(cms);
                        if (CmsStringUtil.isEmptyOrWhitespaceOnly(extracted)
                            && value.isSimpleType()
                            && !(value instanceof CmsXmlHtmlValue)) {
                            // no text value for simple type, so take the string value as item
                            // prevent this for elements of type "OpenCmsHtml", since this causes problematic values
                            // being indexed, e.g., <iframe ...></iframe>
                            // TODO: Why is this special handling needed at all???
                            extracted = value.getStringValue(cms);
                        }
                    }
                } catch (Exception e) {
                    // it can happen that a exception is thrown while extracting a single value
                    LOG.warn(Messages.get().container(Messages.LOG_EXTRACT_VALUE_2, xpath, resource), e);
                }

                // put the extraction to the items and to the textual content
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(extracted)) {
                    localeItems.put(xpath, extracted);
                }
                switch (xmlContent.getHandler().getSearchContentType(value)) {
                    case TRUE:
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(extracted)) {
                            textContent.append(extracted);
                            textContent.append('\n');
                        }
                        break;
                    case CONTENT:
                        // TODO: Potentially extend to allow for indexing of non-xml-contents as well.
                        String potentialLinkValue = value.getStringValue(cms);
                        try {
                            if ((null != potentialLinkValue)
                                && !potentialLinkValue.isEmpty()
                                && cms.existsResource(potentialLinkValue)) {
                                CmsResource linkedRes = cms.readResource(potentialLinkValue);
                                if (CmsResourceTypeXmlContent.isXmlContent(linkedRes)
                                    && !alreadyExtracted.contains(linkedRes.getStructureId())) {
                                    Set<CmsUUID> newAlreadyExtracted = new HashSet<>(alreadyExtracted);
                                    newAlreadyExtracted.add(resource.getStructureId());
                                    I_CmsExtractionResult exRes = CmsSolrDocumentXmlContent.extractXmlContent(
                                        cms,
                                        linkedRes,
                                        index,
                                        locale,
                                        newAlreadyExtracted);
                                    String exContent = exRes.getContent(locale);
                                    if ((exContent != null) && !exContent.trim().isEmpty()) {
                                        textContent.append(exContent.trim());
                                        textContent.append('\n');
                                        break; // Success - we break here to not repeatedly programm a warning.
                                    }
                                }
                            }
                            if (LOG.isInfoEnabled()) {
                                LOG.info(
                                    "When indexing resource "
                                        + resource.getRootPath()
                                        + ", the elements value "
                                        + value.getPath()
                                        + " in locale "
                                        + locale
                                        + " does not contain a link to an XML content. Hence, the linked element's content is not added to the content indexed for the resource itself.");
                            }
                        } catch (Throwable t) {
                            LOG.error(
                                "Failed to add content of resource (site path) "
                                    + potentialLinkValue
                                    + " to content of resource (root path) "
                                    + resource.getRootPath()
                                    + " when indexing the resource for locale "
                                    + locale
                                    + ". Skipping this content part.",
                                t);
                        }
                        break;
                    default:
                        // we do not index the content element for the content field.
                        break;
                }

                List<String> mappings = xmlContent.getHandler().getMappings(value.getPath());
                if (mappings.size() > 0) {
                    // mappings are defined, lets check if we have mappings that interest us
                    for (String mapping : mappings) {
                        if (mapping.startsWith(I_CmsXmlContentHandler.MAPTO_PROPERTY)) {
                            // this is a property mapping
                            String propertyName = mapping.substring(mapping.lastIndexOf(':') + 1);
                            if (CmsPropertyDefinition.PROPERTY_TITLE.equals(propertyName)
                                || CmsPropertyDefinition.PROPERTY_DESCRIPTION.equals(propertyName)) {

                                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(extracted)) {
                                    if (CmsPropertyDefinition.PROPERTY_TITLE.equals(propertyName)) {
                                        galleryNameChooser.setMappedTitleValue(extracted);
                                    } else {
                                        // if field is not title, it must be description
                                        galleryNameChooser.setMappedDescriptionValue(extracted);
                                    }
                                }
                            }
                        } else if (mapping.equals(MAPPING_GALLERY_NAME)) {
                            galleryNameChooser.setMappedGalleryNameValue(value.getPlainText(cms));
                        } else if (mapping.equals(MAPPING_GALLERY_DESCRIPTION)) {
                            galleryNameChooser.setMappedGalleryDescriptionValue(value.getPlainText(cms));
                        }
                    }
                }
                if (value instanceof CmsXmlSerialDateValue) {
                    if ((null != extracted) && !extracted.isEmpty()) {
                        I_CmsSerialDateValue serialDateValue = new CmsSerialDateValue(extracted);
                        I_CmsSerialDateBean serialDateBean = CmsSerialDateBeanFactory.createSerialDateBean(
                            serialDateValue);
                        if (null != serialDateBean) {
                            StringBuffer values = new StringBuffer();
                            StringBuffer endValues = new StringBuffer();
                            StringBuffer currentTillValues = new StringBuffer();
                            for (Long eventDate : serialDateBean.getDatesAsLong()) {
                                values.append("\n").append(eventDate.toString());
                                long endDate = null != serialDateBean.getEventDuration()
                                ? eventDate.longValue() + serialDateBean.getEventDuration().longValue()
                                : eventDate.longValue();
                                endValues.append("\n").append(Long.toString(endDate));
                                // Special treatment for events that end at 00:00:
                                // To not show them at the day after they ended, one millisecond is removed from the end time
                                // for the "currenttill"-time
                                currentTillValues.append("\n").append(
                                    serialDateValue.isCurrentTillEnd()
                                    ? Long.valueOf(
                                        serialDateValue.endsAtMidNight() && (endDate > eventDate.longValue())
                                        ? endDate - 1L
                                        : endDate)
                                    : eventDate);
                            }
                            fieldMappings.put(CmsSearchField.FIELD_SERIESDATES, values.substring(1));
                            fieldMappings.put(CmsSearchField.FIELD_SERIESDATES_END, endValues.substring(1));
                            fieldMappings.put(
                                CmsSearchField.FIELD_SERIESDATES_CURRENT_TILL,
                                currentTillValues.substring(1));
                            fieldMappings.put(
                                CmsSearchField.FIELD_SERIESDATES_TYPE,
                                serialDateValue.getDateType().toString());
                        } else {
                            LOG.warn(
                                "Serial date value \""
                                    + value.getStringValue(cms)
                                    + "\" at element \""
                                    + value.getPath()
                                    + "\" is invalid. No dates are indexed for resource \""
                                    + resource.getRootPath()
                                    + "\".");
                        }
                    }
                }
            }

            Set<String> xpaths = Sets.newHashSet();
            collectSchemaXpathsForSimpleValues(cms, xmlContent.getContentDefinition(), "", xpaths);
            for (String xpath : xpaths) {
                // mappings always are stored with indexes, so we add them to the xpath
                List<String> mappings = xmlContent.getHandler().getMappings(CmsXmlUtils.createXpath(xpath, 1));
                for (String mapping : mappings) {

                    if (mapping.equals(MAPPING_GALLERY_NAME)
                        || mapping.equals(
                            I_CmsXmlContentHandler.MAPTO_PROPERTY + CmsPropertyDefinition.PROPERTY_TITLE)) {
                        String defaultValue = xmlContent.getHandler().getDefault(
                            cms,
                            xmlContent.getFile(),
                            null,
                            xpath,
                            locale);
                        if (mapping.equals(MAPPING_GALLERY_NAME)) {
                            galleryNameChooser.setDefaultGalleryNameValue(defaultValue);
                        } else {
                            galleryNameChooser.setDefaultTitleValue(defaultValue);
                        }
                    }
                }
            }

            final String galleryTitleFieldKey = CmsSearchFieldConfiguration.getLocaleExtendedName(
                CmsSearchField.FIELD_TITLE_UNSTORED,
                locale) + "_s";
            final String galleryNameValue = galleryNameChooser.getGalleryName();
            fieldMappings.put(galleryTitleFieldKey, galleryNameValue);
            fieldMappings.put(
                CmsSearchFieldConfiguration.getLocaleExtendedName(CmsSearchField.FIELD_DESCRIPTION, locale) + "_s",
                galleryNameChooser.getDescription());

            // handle the textual content
            if (textContent.length() > 0) {
                // add the textual content with a localized key to the items
                //String key = CmsSearchFieldConfiguration.getLocaleExtendedName(CmsSearchField.FIELD_CONTENT, locale);
                //items.put(key, textContent.toString());
                // use the default locale of this resource as general text content for the extraction result
                localeItems.put(I_CmsExtractionResult.ITEM_CONTENT, textContent.toString());
            }
            items.put(locale, localeItems);
        }
        // if the content is locale independent, it should have only one content locale, but that should be indexed for all available locales.
        // TODO: One could think of different indexing behavior, i.e., index only for getDefaultLocales(cms,resource)
        //       But using getAvailableLocales(cms,resource) does not work, because locale-available is set to "en" for all that content.
        if ((xmlContent instanceof CmsXmlContent) && ((CmsXmlContent)xmlContent).isLocaleIndependent()) {
            if (forceLocale != null) {
                items.put(forceLocale, localeItems);
            } else {
                for (Locale l : OpenCms.getLocaleManager().getAvailableLocales()) {
                    items.put(l, localeItems);
                    if (null != galleryNameChooser) {
                        final String galleryTitleFieldKey = CmsSearchFieldConfiguration.getLocaleExtendedName(
                            CmsSearchField.FIELD_TITLE_UNSTORED,
                            l) + "_s";
                        fieldMappings.put(galleryTitleFieldKey, galleryNameChooser.getGalleryName(l));
                        fieldMappings.put(
                            CmsSearchFieldConfiguration.getLocaleExtendedName(CmsSearchField.FIELD_DESCRIPTION, l)
                                + "_s",
                            galleryNameChooser.getDescription(l));
                    }
                }
            }
        }
        // add the locales that have been indexed for this document as item and return the extraction result
        // fieldMappings.put(CmsSearchField.FIELD_RESOURCE_LOCALES, locales.toString().trim());
        return new CmsExtractionResult(resourceLocale, items, fieldMappings);

    }

    /**
     * @see org.opencms.search.documents.CmsDocumentXmlContent#extractContent(org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.I_CmsSearchIndex)
     */
    @Override
    public I_CmsExtractionResult extractContent(CmsObject cms, CmsResource resource, I_CmsSearchIndex index)
    throws CmsException {

        logContentExtraction(resource, index);

        try {
            I_CmsExtractionResult result = null;
            List<I_CmsExtractionResult> ex = new ArrayList<I_CmsExtractionResult>();
            for (CmsResource detailContainers : CmsDetailOnlyContainerUtil.getDetailOnlyResources(cms, resource)) {
                CmsSolrDocumentContainerPage containerpageExtractor = new CmsSolrDocumentContainerPage("");
                String localeTemp = detailContainers.getRootPath();
                localeTemp = CmsResource.getParentFolder(localeTemp);
                localeTemp = CmsResource.getName(localeTemp);
                localeTemp = localeTemp.substring(0, localeTemp.length() - 1);
                Locale locale = CmsLocaleManager.getLocale(localeTemp);
                if (CmsDetailOnlyContainerUtil.useSingleLocaleDetailContainers(
                    OpenCms.getSiteManager().getSiteRoot(resource.getRootPath()))
                    && locale.equals(CmsLocaleManager.getDefaultLocale())) {
                    // in case of single locale detail containers do not force the locale
                    locale = null;
                }
                I_CmsExtractionResult containersExtractionResult = containerpageExtractor.extractContent(
                    cms,
                    detailContainers,
                    index,
                    locale);
                // only use the locales of the resource itself, not the ones of the detail containers page
                containersExtractionResult.getContentItems().remove(CmsSearchField.FIELD_RESOURCE_LOCALES);

                ex.add(containersExtractionResult);
            }
            result = extractXmlContent(cms, resource, index);
            result = result.merge(ex);
            return result;

        } catch (Throwable t) {
            throw new CmsIndexException(Messages.get().container(Messages.ERR_TEXT_EXTRACTION_1, resource), t);
        }
    }

    /**
     * Solr index content is stored in multiple languages, so the result is NOT locale dependent.<p>
     *
     * @see org.opencms.search.documents.I_CmsDocumentFactory#isLocaleDependend()
     */
    public boolean isLocaleDependend() {

        return false;
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#isUsingCache()
     */
    @Override
    public boolean isOnlyDependentOnContent() {

        return false;
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#isUsingCache()
     */
    public boolean isUsingCache() {

        return false;
    }
}

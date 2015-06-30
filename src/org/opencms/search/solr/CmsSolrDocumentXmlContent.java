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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.jsp.CmsJspTagContainer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsIndexException;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.documents.A_CmsVfsDocument;
import org.opencms.search.documents.CmsIndexNoContentException;
import org.opencms.search.documents.Messages;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.search.galleries.CmsGalleryNameMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.I_CmsXmlContentHandler;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.Sets;

/**
 * Special document text extraction factory for Solr index.<p>
 *
 * @since 8.5.0
 */
public class CmsSolrDocumentXmlContent extends A_CmsVfsDocument {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSolrDocumentXmlContent.class);

    /** The solr document type name for xml-contents. */
    public static final String TYPE_XMLCONTENT_SOLR = "xmlcontent-solr";

    /** Mapping name used to indicate that the value should be used for the gallery name. */
    public static final String MAPPING_GALLERY_NAME = "galleryName";

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
    public static CmsExtractionResult extractXmlContent(CmsObject cms, CmsResource resource, CmsSearchIndex index)
    throws CmsException {

        // un-marshal the content
        CmsFile file = cms.readFile(resource);
        if (file.getLength() <= 0) {
            throw new CmsIndexNoContentException(
                Messages.get().container(Messages.ERR_NO_CONTENT_1, resource.getRootPath()));
        }
        A_CmsXmlDocument xmlContent = CmsXmlContentFactory.unmarshal(cms, file);

        // initialize some variables
        Map<Locale, Map<String, String>> items = new HashMap<Locale, Map<String, String>>();
        Map<String, String> fieldMappings = new HashMap<String, String>();
        StringBuffer locales = new StringBuffer();
        Locale resourceLocale = index.getLocaleForResource(cms, resource, xmlContent.getLocales());

        // loop over the locales of the content
        for (Locale locale : xmlContent.getLocales()) {
            Map<String, String> localeItems = new HashMap<String, String>();
            StringBuffer textContent = new StringBuffer();
            // store the locales of the content as space separated field
            locales.append(locale.toString());
            locales.append(' ');

            boolean hasTitleMapping = false;
            String galleryNameTemplate = null;

            // loop over the available element paths of the current content locale
            List<String> paths = xmlContent.getNames(locale);
            for (String xpath : paths) {

                // try to get the value extraction for the current element path
                String extracted = null;
                I_CmsXmlContentValue value = xmlContent.getValue(xpath, locale);
                try {
                    extracted = value.getPlainText(cms);
                    if (CmsStringUtil.isEmptyOrWhitespaceOnly(extracted) && value.isSimpleType()) {
                        // no text value for simple type, so take the string value as item
                        extracted = value.getStringValue(cms);
                    }
                } catch (Exception e) {
                    // it can happen that a exception is thrown while extracting a single value
                    LOG.warn(Messages.get().container(Messages.LOG_EXTRACT_VALUE_2, xpath, resource), e);
                }

                // put the extraction to the items and to the textual content
                if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(extracted)) {
                    localeItems.put(xpath, extracted);
                }
                if (value.getContentDefinition().getContentHandler().isSearchable(value)
                    && CmsStringUtil.isNotEmptyOrWhitespaceOnly(extracted)) {
                    // value is search-able and the extraction is not empty, so added to the textual content
                    textContent.append(extracted);
                    textContent.append('\n');
                }

                // TODO: ?
                /* ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ */

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
                                    // search index field names and property names are different ["Title" vs. "title"]
                                    String fieldName = null;
                                    if (CmsPropertyDefinition.PROPERTY_TITLE.equals(propertyName)) {
                                        // field is title
                                        hasTitleMapping = true;
                                        fieldName = CmsSearchField.FIELD_TITLE_UNSTORED;
                                    } else {
                                        // if field is not title, it must be description
                                        fieldName = CmsSearchField.FIELD_DESCRIPTION;
                                    }
                                    fieldMappings.put(
                                        CmsSearchFieldConfiguration.getLocaleExtendedName(fieldName, locale) + "_s",
                                        extracted);
                                }
                            } else if (mapping.equals(MAPPING_GALLERY_NAME)) {
                                galleryNameTemplate = value.getPlainText(cms);
                                LOG.info(
                                    "Found gallery name template for "
                                        + resource.getRootPath()
                                        + ":"
                                        + galleryNameTemplate);

                            }
                        }
                    }
                }
            }
            if (galleryNameTemplate == null) {
                // In the loop above, we only looked at mappings for values which are actually there.
                // But the galleryName mapping should use the configured default value if the value is not
                // present in the content. So we need to find the xpath for which the galleryName mapping is defined,
                // if any.

                Set<String> xpaths = Sets.newHashSet();
                collectSchemaXpathsForSimpleValues(cms, xmlContent.getContentDefinition(), "", xpaths);
                for (String xpath : xpaths) {
                    // mappings always are stored with indexes, so we add them to the xpath
                    List<String> mappings = xmlContent.getHandler().getMappings(CmsXmlUtils.createXpath(xpath, 1));
                    for (String mapping : mappings) {
                        if (mapping.equals(MAPPING_GALLERY_NAME)) {
                            galleryNameTemplate = xmlContent.getHandler().getDefault(
                                cms,
                                xmlContent.getFile(),
                                null,
                                xpath,
                                locale);
                            LOG.info(
                                "Using default value for gallery name template in "
                                    + resource.getRootPath()
                                    + ": "
                                    + galleryNameTemplate);
                            break;
                        }
                    }
                }
            }

            if (!hasTitleMapping) {
                // in case no title mapping present, use the title property for all locales
                String title = cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue();
                fieldMappings.put(
                    CmsSearchFieldConfiguration.getLocaleExtendedName(CmsSearchField.FIELD_TITLE_UNSTORED, locale)
                        + "_s",
                    title);
            }

            /* ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ */

            if (galleryNameTemplate != null) {
                CmsGalleryNameMacroResolver macroResolver = new CmsGalleryNameMacroResolver(cms, xmlContent, locale);
                String galleryName = macroResolver.resolveMacros(galleryNameTemplate);
                LOG.info(
                    "Using gallery name mapping '"
                        + galleryNameTemplate
                        + "' for '"
                        + resource.getRootPath()
                        + "' in locale "
                        + locale
                        + ", resulting in gallery name '"
                        + galleryName
                        + "'");
                fieldMappings.put(
                    CmsSearchFieldConfiguration.getLocaleExtendedName(CmsSearchField.FIELD_TITLE_UNSTORED, locale)
                        + "_s",
                    galleryName);
            }

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

        // add the locales that have been indexed for this document as item and return the extraction result
        // fieldMappings.put(CmsSearchField.FIELD_RESOURCE_LOCALES, locales.toString().trim());
        return new CmsExtractionResult(resourceLocale, items, fieldMappings);
    }

    /**
     * @see org.opencms.search.documents.CmsDocumentXmlContent#extractContent(org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.CmsSearchIndex)
     */
    @Override
    public I_CmsExtractionResult extractContent(CmsObject cms, CmsResource resource, CmsSearchIndex index)
    throws CmsException {

        logContentExtraction(resource, index);

        try {
            I_CmsExtractionResult result = null;
            if (hasDetailContainers(cms, resource)) {
                String detailContainerPage = CmsJspTagContainer.getDetailOnlyPageName(cms.getSitePath(resource));
                CmsResource detailContainers = cms.readResource(detailContainerPage);
                List<I_CmsExtractionResult> ex = new ArrayList<I_CmsExtractionResult>();
                CmsSolrDocumentContainerPage containerpageExtractor = new CmsSolrDocumentContainerPage("");
                I_CmsExtractionResult containersExtractionResult = containerpageExtractor.extractContent(
                    cms,
                    detailContainers,
                    index);
                // only use the locales of the resource itself, not the ones of the detail containers page
                containersExtractionResult.getContentItems().remove(CmsSearchField.FIELD_RESOURCE_LOCALES);
                ex.add(containersExtractionResult);
                result = extractXmlContent(cms, resource, index);
                result = result.merge(ex);
            } else {
                result = extractXmlContent(cms, resource, index);
            }
            return result;

        } catch (Throwable t) {
            throw new CmsIndexException(Messages.get().container(Messages.ERR_TEXT_EXTRACTION_1, resource), t);
        }
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#getDocumentKeys(java.util.List, java.util.List)
     */
    @Override
    public List<String> getDocumentKeys(List<String> resourceTypes, List<String> mimeTypes) throws CmsException {

        if (resourceTypes.contains("*")) {
            // we need to find all configured XML content types
            List<String> allTypes = new ArrayList<String>();
            for (Iterator<I_CmsResourceType> i = OpenCms.getResourceManager().getResourceTypes().iterator(); i.hasNext();) {
                I_CmsResourceType resourceType = i.next();
                if ((resourceType instanceof CmsResourceTypeXmlContent)
                    // either we need a configured schema, or another class name (which must then contain an inline schema)
                    && (((CmsResourceTypeXmlContent)resourceType).getConfiguration().containsKey(
                        CmsResourceTypeXmlContent.CONFIGURATION_SCHEMA)
                        || !CmsResourceTypeXmlContent.class.equals(resourceType.getClass()))) {
                    // add the XML content resource type name
                    allTypes.add(resourceType.getTypeName());
                }
            }
            resourceTypes = allTypes;
        }

        return super.getDocumentKeys(resourceTypes, mimeTypes);
    }

    /**
     * Checks whether the resource has associated detail containers.<p>
     *
     * @param cms the cms context
     * @param resource the resource
     *
     * @return <code>true</code> if the resource has associated detail containers
     */
    public boolean hasDetailContainers(CmsObject cms, CmsResource resource) {

        String detailContainerPage = CmsJspTagContainer.getDetailOnlyPageName(cms.getSitePath(resource));
        return cms.existsResource(detailContainerPage);
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
    public boolean isUsingCache() {

        return true;
    }
}

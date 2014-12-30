/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.search.galleries;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsIndexException;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.I_CmsSearchDocument;
import org.opencms.search.documents.CmsDocumentXmlContent;
import org.opencms.search.documents.Messages;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.I_CmsXmlContentHandler;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.Sets;

/**
 * Special document text extraction factory for the gallery index that creates multiple fields for the content
 * in all the languages available in an XML content.<p>
 * 
 * @since 8.0.0 
 */
public class CmsGalleryDocumentXmlContent extends CmsDocumentXmlContent {

    /** Mapping name used to indicate that the value should be used for the gallery name. */
    public static final String MAPPING_GALLERY_NAME = "galleryName";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDocumentXmlContent.class);

    /**
     * Creates a new instance of this Lucene document factory.<p>
     * 
     * @param name name of the document type
     */
    public CmsGalleryDocumentXmlContent(String name) {

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
     * Generates a new lucene document instance from contents of the given resource for the provided index.<p>
     * 
     * For gallery document generators, we never check for {@link org.opencms.search.CmsSearchIndex#isExtractingContent()} since
     * all these classes are assumed to be written with optimizations special to gallery search indexing anyway.<p>
     * 
     * @see org.opencms.search.fields.CmsLuceneFieldConfiguration#createDocument(CmsObject, CmsResource, CmsSearchIndex, I_CmsExtractionResult)
     * @see org.opencms.search.documents.I_CmsDocumentFactory#createDocument(CmsObject, CmsResource, CmsSearchIndex)
     */
    @Override
    public I_CmsSearchDocument createDocument(CmsObject cms, CmsResource resource, CmsSearchIndex index)
    throws CmsException {

        // extract the content from the resource
        I_CmsExtractionResult content = null;

        // extraction result has not been attached to the resource
        try {
            content = extractContent(cms, resource, index);
        } catch (Exception e) {
            // text extraction failed for document - continue indexing meta information only
            LOG.error(Messages.get().getBundle().key(Messages.ERR_TEXT_EXTRACTION_1, resource.getRootPath()), e);
        }
        // create the Lucene document according to the index field configuration
        return index.getFieldConfiguration().createDocument(cms, resource, index, content);
    }

    /**
     * Returns the raw text content of a given VFS resource of type <code>CmsResourceTypeXmlContent</code>.<p>
     * 
     * All XML nodes from the content for all locales will be stored separately in the item map 
     * which you can access using {@link CmsExtractionResult#getContentItems()}. The XML elements will be 
     * accessible using their xpath. The xpath will start with the locale and have the form like for example 
     * <code>de/Text[1]</code> or <code>en/Nested[1]/Text[1]</code>.<p>  
     * 
     * @see org.opencms.search.documents.I_CmsSearchExtractor#extractContent(CmsObject, CmsResource, CmsSearchIndex)
     */
    @Override
    public I_CmsExtractionResult extractContent(CmsObject cms, CmsResource resource, CmsSearchIndex index)
    throws CmsException {

        logContentExtraction(resource, index);
        try {
            CmsFile file = readFile(cms, resource);
            A_CmsXmlDocument xmlContent = CmsXmlContentFactory.unmarshal(cms, file);

            Map<String, String> items = new HashMap<String, String>();
            StringBuffer locales = new StringBuffer();
            for (Locale locale : xmlContent.getLocales()) {
                locales.append(locale.toString());
                locales.append(' ');
                StringBuffer content = new StringBuffer();
                boolean hasTitleMapping = false;
                String galleryNameTemplate = null;
                for (String xpath : xmlContent.getNames(locale)) {
                    I_CmsXmlContentValue value = xmlContent.getValue(xpath, locale);
                    if (value.getContentDefinition().getContentHandler().isSearchable(value)) {
                        // create the content value for the locale by adding all String values in the XML nodes
                        String extracted = value.getPlainText(cms);
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(extracted)) {
                            content.append(extracted);
                            content.append('\n');
                        }
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
                                    String extracted = value.getPlainText(cms);
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
                                        putMappingValue(xmlContent, fieldName, locale, items, extracted);
                                    }
                                }
                            } else if (mapping.equals(MAPPING_GALLERY_NAME)) {
                                galleryNameTemplate = value.getPlainText(cms);
                                LOG.info("Found gallery name template for "
                                    + resource.getRootPath()
                                    + ":"
                                    + galleryNameTemplate);

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
                                LOG.info("Using default value for gallery name template in "
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
                    putMappingValue(xmlContent, CmsSearchField.FIELD_TITLE_UNSTORED, locale, items, title);
                }

                if (galleryNameTemplate != null) {
                    CmsGalleryNameMacroResolver macroResolver = new CmsGalleryNameMacroResolver(cms, xmlContent, locale);
                    String galleryName = macroResolver.resolveMacros(galleryNameTemplate);
                    LOG.info("Using gallery name mapping '"
                        + galleryNameTemplate
                        + "' for '"
                        + resource.getRootPath()
                        + "' in locale "
                        + locale
                        + ", resulting in gallery name '"
                        + galleryName
                        + "'");
                    putMappingValue(xmlContent, CmsSearchField.FIELD_TITLE_UNSTORED, locale, items, galleryName);
                }

                if (content.length() > 0) {
                    // append language individual content field
                    items.put(
                        CmsSearchFieldConfiguration.getLocaleExtendedName(CmsSearchField.FIELD_CONTENT, locale),
                        content.toString());
                }
                // store the locales
                items.put(
                    CmsSearchField.FIELD_RESOURCE_LOCALES,
                    CmsStringUtil.listAsString(getLocalesToStore(xmlContent), " "));
            }

            // return the extraction result
            return new CmsExtractionResult(null, items);
        } catch (Exception e) {
            throw new CmsIndexException(
                Messages.get().container(Messages.ERR_TEXT_EXTRACTION_1, resource.getRootPath()),
                e);
        }
    }

    /**
     * Gallery index content is stored in multiple languages, so the result is NOT locale dependent.<p>
     * 
     * @see org.opencms.search.documents.CmsDocumentXmlContent#isLocaleDependend()
     */
    @Override
    public boolean isLocaleDependend() {

        return false;
    }

    /**
     * Gets the locales which should be stored in the locale field of the document for the given content.<p>
     * 
     * @param content the XML content 
     * 
     * @return the list of locales for the locale field 
     */
    protected List<Locale> getLocalesToStore(A_CmsXmlDocument content) {

        if (isGroup(content) || CmsResourceTypeXmlContainerPage.isContainerPage(content.getFile())) {
            return OpenCms.getLocaleManager().getAvailableLocales();
        } else {
            return content.getLocales();
        }
    }

    /**
     * Returns the locales which the given field should be written to in the document.
     * 
     * @param xmlContent the XML content 
     * @param fieldName the field name 
     * @param sourceLocale the source locale
     * 
     * @return the list of locales to which the field should be written 
     */
    protected List<Locale> getTargetLocalesForField(A_CmsXmlDocument xmlContent, String fieldName, Locale sourceLocale) {

        if (isGroup(xmlContent) && sourceLocale.equals(Locale.ENGLISH)) {
            return OpenCms.getLocaleManager().getAvailableLocales();
        } else {
            return Collections.singletonList(sourceLocale);
        }
    }

    /**
     * Helper method to check whether the content is an element group.<p>
     * 
     * @param content the content to check 
     * 
     * @return true if the content is an element group 
     */
    protected boolean isGroup(A_CmsXmlDocument content) {

        try {
            int typeId = content.getFile().getTypeId();
            I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(typeId);
            String typeName = type.getTypeName();
            return CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_NAME.equals(typeName)
                || CmsResourceTypeXmlContainerPage.INHERIT_CONTAINER_TYPE_NAME.equals(typeName);
        } catch (CmsLoaderException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return false;
        }
    }

    /**
     * Adds the given value to the document items for all target locales.<p>
     * 
     * @param xmlContent the XML content 
     * @param fieldName the field name 
     * @param sourceLocale the source locale
     * @param items the document items
     * @param value the value to put
     */
    protected void putMappingValue(
        A_CmsXmlDocument xmlContent,
        String fieldName,
        Locale sourceLocale,
        Map<String, String> items,
        String value) {

        List<Locale> fieldTargetLocales = getTargetLocalesForField(xmlContent, fieldName, sourceLocale);
        // append language individual property field
        for (Locale targetLocale : fieldTargetLocales) {
            items.put(CmsSearchFieldConfiguration.getLocaleExtendedName(fieldName, targetLocale), value);
        }
    }
}

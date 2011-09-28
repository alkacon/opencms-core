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
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.search.CmsIndexException;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.documents.CmsDocumentXmlContent;
import org.opencms.search.documents.Messages;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.I_CmsXmlContentHandler;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.Document;

/**
 * Special document text extraction factory for the gallery index that creates multiple fields for the content
 * in all the languages available in an XML content.<p>
 * 
 * @since 8.0.0 
 */
public class CmsGalleryDocumentXmlContent extends CmsDocumentXmlContent {

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
     * Generates a new lucene document instance from contents of the given resource for the provided index.<p>
     * 
     * For gallery document generators, we never check for {@link CmsSearchIndex#isExtractingContent()} since
     * all these classes are assumed to be written with optimizations special to gallery search indexing anyway.<p>
     * 
     * @see org.opencms.search.fields.CmsSearchFieldConfiguration#createDocument(CmsObject, CmsResource, CmsSearchIndex, I_CmsExtractionResult)
     * @see org.opencms.search.documents.I_CmsDocumentFactory#createDocument(CmsObject, CmsResource, CmsSearchIndex)
     */
    @Override
    public Document createDocument(CmsObject cms, CmsResource resource, CmsSearchIndex index) throws CmsException {

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
                    if ((mappings != null) && (mappings.size() > 0)) {
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
                                            fieldName = CmsSearchField.FIELD_TITLE_UNSTORED;
                                        } else {
                                            // if field is not title, it must be description
                                            fieldName = CmsSearchField.FIELD_DESCRIPTION;
                                        }
                                        // append language individual property field
                                        items.put(
                                            CmsGallerySearchFieldConfiguration.getLocaleExtendedName(fieldName, locale),
                                            extracted);
                                    }
                                }
                            }
                        }
                    }
                }
                if (content.length() > 0) {
                    // append language individual content field
                    items.put(
                        CmsGallerySearchFieldConfiguration.getLocaleExtendedName(CmsSearchField.FIELD_CONTENT, locale),
                        content.toString());
                }
                // store the locales
                items.put(CmsGallerySearchFieldMapping.FIELD_RESOURCE_LOCALES, locales.toString());
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
}

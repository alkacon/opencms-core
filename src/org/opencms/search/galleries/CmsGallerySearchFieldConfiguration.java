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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.main.OpenCms;
import org.opencms.search.I_CmsSearchDocument;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.search.fields.CmsLuceneField;
import org.opencms.search.fields.CmsLuceneFieldConfiguration;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.util.CmsStringUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * Describes the search field configuration that is used by the gallery index.<p>
 * 
 * @since 8.0.0 
 */
public class CmsGallerySearchFieldConfiguration extends CmsLuceneFieldConfiguration {

    /**
     * Default constructor.<p>
     */
    public CmsGallerySearchFieldConfiguration() {

        // nothing special to do here
    }

    /**
     * Extends the given document by the configured field mappings.<p>
     * 
     * @param document the document to extend
     * @param cms the OpenCms context used for building the search index
     * @param resource the resource that is indexed
     * @param extractionResult the plain text extraction result from the resource
     * @param properties the list of all properties directly attached to the resource (not searched)
     * @param propertiesSearched the list of all searched properties of the resource  
     * 
     * @return the document extended by the configured field mappings
     */
    @Override
    protected I_CmsSearchDocument appendFieldMappings(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        Iterator<CmsSearchField> fieldConfigs = getFields().iterator();
        while (fieldConfigs.hasNext()) {
            // check all field configurations 
            CmsSearchField fieldConfig = fieldConfigs.next();

            if (CmsSearchField.FIELD_TITLE.equals(fieldConfig.getName())
                || (CmsResourceTypeXmlContent.isXmlContent(resource) && (CmsSearchField.FIELD_CONTENT.equals(fieldConfig.getName())
                    || CmsSearchField.FIELD_TITLE_UNSTORED.equals(fieldConfig.getName())
                    || CmsSearchField.FIELD_SORT_TITLE.equals(fieldConfig.getName())
                    || CmsSearchField.FIELD_DESCRIPTION.equals(fieldConfig.getName()) || CmsSearchField.FIELD_META.equals(fieldConfig.getName())))) {
                appendMultipleFieldMapping(
                // XML content and special multiple language mapping field
                    document,
                    fieldConfig,
                    cms,
                    resource,
                    extractionResult,
                    properties,
                    propertiesSearched);
            } else {
                // not an XML content or standard field - use standard mappings as configured
                appendFieldMapping(
                    document,
                    fieldConfig,
                    cms,
                    resource,
                    extractionResult,
                    properties,
                    propertiesSearched);
            }
        }

        return document;
    }

    /**
     * Extends the given document by the gallery index special multiple language filed mappings for the given field.<p>
     * 
     * @param document the document to extend
     * @param field the field to create the mappings for
     * @param cms the OpenCms context used for building the search index
     * @param resource the resource that is indexed
     * @param extractionResult the plain text extraction result from the resource
     * @param properties the list of all properties directly attached to the resource (not searched)
     * @param propertiesSearched the list of all searched properties of the resource  
     * 
     * @return the document extended by the gallery index special multiple language filed mappings for the given field
     */
    protected I_CmsSearchDocument appendMultipleFieldMapping(
        I_CmsSearchDocument document,
        CmsSearchField field,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        String mappingName = null;
        String fieldName = field.getName();
        String value = null;

        if (CmsSearchField.FIELD_CONTENT.equals(fieldName)) {
            mappingName = CmsSearchField.FIELD_CONTENT;
        } else if (CmsSearchField.FIELD_TITLE_UNSTORED.equals(fieldName)) {
            mappingName = CmsSearchField.FIELD_TITLE_UNSTORED;
        } else if (CmsSearchField.FIELD_TITLE.equals(fieldName) || CmsSearchField.FIELD_SORT_TITLE.equals(fieldName)) {
            if (!CmsResourceTypeXmlContent.isXmlContent(resource)) {
                // not an XML content - we need to read the property and map it to all fields
                value = CmsProperty.get(CmsPropertyDefinition.PROPERTY_TITLE, properties).getValue();
            } else {
                mappingName = CmsSearchField.FIELD_TITLE_UNSTORED;
            }
        } else if (CmsSearchField.FIELD_DESCRIPTION.equals(fieldName)) {
            mappingName = CmsSearchField.FIELD_DESCRIPTION;
        } else if (CmsSearchField.FIELD_META.equals(fieldName)) {
            mappingName = CmsSearchField.FIELD_META;
        }

        for (Locale locale : OpenCms.getLocaleManager().getAvailableLocales()) {
            // iterate all configured locales

            if ((mappingName != null) && (extractionResult != null)) {
                // should be the case for XML contents only
                if (mappingName == CmsSearchField.FIELD_META) {
                    // meta field - we can use == because the String has been initialized above
                    String title = extractionResult.getContentItems().get(
                        getLocaleExtendedName(CmsSearchField.FIELD_TITLE_UNSTORED, locale));
                    String description = extractionResult.getContentItems().get(
                        getLocaleExtendedName(CmsSearchField.FIELD_DESCRIPTION, locale));
                    StringBuffer v = new StringBuffer();
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(title)) {
                        v.append(title);
                        v.append('\n');
                    }
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(description)) {
                        v.append(description);
                    }
                    if (v.length() > 0) {
                        value = v.toString();
                    }
                } else {
                    String localeMapping = getLocaleExtendedName(mappingName, locale);
                    value = extractionResult.getContentItems().get(localeMapping);
                }
            }

            if ((value != null) && (field instanceof CmsLuceneField)) {
                // In order to search and sort case insensitive in the title field
                // take the lower case value for the un-stored title field.
                if (field.getName().equals(CmsSearchField.FIELD_TITLE_UNSTORED)
                    || field.getName().equals(CmsSearchField.FIELD_SORT_TITLE)) {
                    value = value.toLowerCase();
                }
                // localized content is available for this field
                Field fieldable = ((CmsLuceneField)field).createField(getLocaleExtendedName(fieldName, locale), value);
                ((Document)document.getDocument()).add(fieldable);
            }
        }

        return document;
    }
}
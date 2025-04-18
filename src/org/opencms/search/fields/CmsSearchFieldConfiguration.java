/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.search.fields;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategoryService;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.I_CmsSearchDocument;
import org.opencms.search.I_CmsSearchIndex;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.solr.uninverting.UninvertingReader.Type;

/**
 * Abstract implementation for OpenCms search field configurations.<p>
 *
 * @since 8.5.0
 */
public class CmsSearchFieldConfiguration extends A_CmsSearchFieldConfiguration {

    /** A list of fields that should be lazy-loaded. */
    public static final List<String> LAZY_FIELDS = new ArrayList<String>();

    /** The name for the standard field configuration. */
    public static final String STR_STANDARD = "standard";

    /** The serial version id. */
    private static final long serialVersionUID = -7581572963583498549L;

    static {
        LAZY_FIELDS.add(CmsSearchField.FIELD_CONTENT);
        LAZY_FIELDS.add(CmsSearchField.FIELD_CONTENT_BLOB);
    }

    /** The current index. */
    private transient CmsSearchIndex m_index;

    /**
     * Creates a new, empty field configuration.<p>
     */
    public CmsSearchFieldConfiguration() {

        super();
    }

    /**
     * Returns the locale extended name for the given lookup String.<p>
     *
     * @param lookup the lookup String
     * @param locale the locale
     *
     * @return the locale extended name for the given lookup String
     */
    public static final String getLocaleExtendedName(String lookup, Locale locale) {

        if (locale == null) {
            return lookup;
        }
        return getLocaleExtendedName(lookup, locale.toString());
    }

    /**
     * Returns the locale extended name for the given lookup String.<p>
     *
     * @param lookup the lookup String
     * @param locale the locale
     *
     * @return the locale extended name for the given lookup String
     */
    public static final String getLocaleExtendedName(String lookup, String locale) {

        StringBuffer result = new StringBuffer(32);
        result.append(lookup);
        result.append('_');
        result.append(locale);
        return result.toString();
    }

    /**
     * Creates a space separated list of all parent folders of the given root path.<p>
     *
     * @param rootPath the root path to get the parent folder list for
     *
     * @return a space separated list of all parent folders of the given root path
     */
    public static String getParentFolderTokens(String rootPath) {

        if (CmsStringUtil.isEmpty(rootPath)) {
            return "/";
        }
        StringBuffer result = new StringBuffer(128);
        String folderName = CmsResource.getFolderPath(rootPath);
        for (int i = 0; i < folderName.length(); i++) {
            char c = folderName.charAt(i);
            if (c == '/') {
                if (result.length() > 0) {
                    result.append(' ');
                }
                result.append(folderName.substring(0, i + 1));
            }
        }
        return result.toString();
    }

    /** To allow sorting on a field the field must be added to the map given to {@link org.apache.solr.uninverting.UninvertingReader#wrap(org.apache.lucene.index.DirectoryReader, Map)}.
     *  The method adds the configured fields.
     * @param uninvertingMap the map to which the fields are added.
     */
    @Override
    public void addUninvertingMappings(Map<String, Type> uninvertingMap) {

        for (String fieldName : getFieldNames()) {
            uninvertingMap.put(fieldName, Type.SORTED);
        }

    }

    /**
     * Creates the Lucene Document with this field configuration for the provided VFS resource, search index and content.<p>
     *
     * This triggers the indexing process for the given VFS resource according to the configuration
     * of the provided index.<p>
     *
     * The provided index resource contains the basic contents to index.
     * The provided search index contains the configuration what to index, such as the locale and
     * possible special field mappings.<p>
     *
     * @param cms the OpenCms user context used to access the OpenCms VFS
     * @param resource the resource to create the Lucene document from
     * @param index the search index to create the Document for
     * @param extraction the plain text content extracted from the document
     *
     * @return the Search Document for the given VFS resource and the given search index
     *
     * @throws CmsException if something goes wrong
     */
    public I_CmsSearchDocument createDocument(
        CmsObject cms,
        CmsResource resource,
        I_CmsSearchIndex index,
        I_CmsExtractionResult extraction)
    throws CmsException {

        m_index = (CmsSearchIndex)index;

        I_CmsSearchDocument document = m_index.createEmptyDocument(resource);

        List<CmsProperty> propertiesSearched = cms.readPropertyObjects(resource, true);
        List<CmsProperty> properties = cms.readPropertyObjects(resource, false);

        document = appendContentBlob(document, cms, resource, extraction, properties, propertiesSearched);
        document = appendPath(document, cms, resource, extraction, properties, propertiesSearched);
        document = appendType(document, cms, resource, extraction, properties, propertiesSearched);
        document = appendFileSize(document, cms, resource, extraction, properties, propertiesSearched);
        document = appendDates(document, cms, resource, extraction, properties, propertiesSearched);
        document = appendLocales(document, cms, resource, extraction, properties, propertiesSearched);
        document = appendProperties(document, cms, resource, extraction, properties, propertiesSearched);
        document = appendCategories(document, cms, resource, extraction, properties, propertiesSearched);
        document = appendFieldMappings(document, cms, resource, extraction, properties, propertiesSearched);
        document = appendAdditionalValuesToDcoument(
            document,
            cms,
            resource,
            extraction,
            properties,
            propertiesSearched);

        return document;
    }

    /**
     * Returns the index.<p>
     *
     * @return the index
     */
    public I_CmsSearchIndex getIndex() {

        return m_index;
    }

    /**
     * Sets the index.<p>
     *
     * @param index the index to set
     */
    public void setIndex(CmsSearchIndex index) {

        m_index = index;
    }

    /**
     * Overriding this method allows to append some 'extra' values/fields to a document
     * without overriding the {@link #createDocument} method itself.<p>
     *
     * The method {@link #createDocument} reads all properties of the current resource which is
     * an expensive operation. In order to avoid reading those properties twice, this method has been introduced.<p>
     *
     * Compared with all the other appender methods the name of this method is generic.<p>
     *
     * In this default implementation the document is returned unchanged.<p>
     *
     * @param document the document to extend
     * @param cms the OpenCms context used for building the search index
     * @param resource the resource that is indexed
     * @param extraction the plain text extraction result from the resource
     * @param properties the list of all properties directly attached to the resource (not searched)
     * @param propertiesSearched the list of all searched properties of the resource
     *
     * @return the document extended by resource category information
     */
    protected I_CmsSearchDocument appendAdditionalValuesToDcoument(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extraction,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        return document;
    }

    /**
     * Extends the given document by resource category information based on properties.<p>
     *
     * @param document the document to extend
     * @param cms the OpenCms context used for building the search index
     * @param resource the resource that is indexed
     * @param extractionResult the plain text extraction result from the resource
     * @param properties the list of all properties directly attached to the resource (not searched)
     * @param propertiesSearched the list of all searched properties of the resource
     *
     * @return the document extended by resource category information
     *
     * @throws CmsException if something goes wrong
     */
    protected I_CmsSearchDocument appendCategories(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched)
    throws CmsException {

        CmsCategoryService categoryService = CmsCategoryService.getInstance();
        document.addCategoryField(categoryService.readResourceCategories(cms, resource));

        return document;
    }

    /**
     * Extends the given document by a field that contains the extracted content blob.<p>
     *
     * @param document the document to extend
     * @param cms the OpenCms context used for building the search index
     * @param resource the resource that is indexed
     * @param extractionResult the plain text extraction result from the resource
     * @param properties the list of all properties directly attached to the resource (not searched)
     * @param propertiesSearched the list of all searched properties of the resource
     *
     * @return the document extended by a field that contains the extracted content blob
     */
    protected I_CmsSearchDocument appendContentBlob(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        if (extractionResult != null) {
            byte[] data = extractionResult.getBytes();
            if (data != null) {
                document.addContentField(data);
            }
        }

        return document;
    }

    /**
     * Extends the given document by fields for date of creation, content and last modification.<p>
     *
     * @param document the document to extend
     * @param cms the OpenCms context used for building the search index
     * @param resource the resource that is indexed
     * @param extractionResult the plain text extraction result from the resource
     * @param properties the list of all properties directly attached to the resource (not searched)
     * @param propertiesSearched the list of all searched properties of the resource
     *
     * @return the document extended by fields for date of creation, content and last modification
     */
    protected I_CmsSearchDocument appendDates(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        document.addDateField(CmsSearchField.FIELD_DATE_CREATED, resource.getDateCreated(), true);
        document.addDateField(CmsSearchField.FIELD_DATE_LASTMODIFIED, resource.getDateLastModified(), true);
        document.addDateField(CmsSearchField.FIELD_DATE_CONTENT, resource.getDateContent(), false);

        return document;
    }

    /**
     * Extends the given document by the mappings for the given field.<p>
     *
     * @param document the document to extend
     * @param field the field to create the mappings for
     * @param cms the OpenCms context used for building the search index
     * @param resource the resource that is indexed
     * @param extractionResult the plain text extraction result from the resource
     * @param properties the list of all properties directly attached to the resource (not searched)
     * @param propertiesSearched the list of all searched properties of the resource
     *
     * @return the document extended by the mappings for the given field
     */
    protected I_CmsSearchDocument appendFieldMapping(
        I_CmsSearchDocument document,
        CmsSearchField field,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        StringBuffer text = new StringBuffer();
        for (I_CmsSearchFieldMapping mapping : field.getMappings()) {
            String mapResult = mapping.getStringValue(cms, resource, extractionResult, properties, propertiesSearched);
            if (mapResult != null) {
                if (text.length() > 0) {
                    text.append('\n');
                }
                text.append(mapResult);
            }
        }
        if (text.length() > 0) {
            document.addSearchField(field, text.toString());
        }

        return document;
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
    protected I_CmsSearchDocument appendFieldMappings(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        for (CmsSearchField field : getFields()) {
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
     * Extends the given document by the "size" field.<p>
     *
     * @param document the document to extend
     * @param cms the OpenCms context used for building the search index
     * @param resource the resource that is indexed
     * @param extractionResult the plain text extraction result from the resource
     * @param properties the list of all properties directly attached to the resource (not searched)
     * @param propertiesSearched the list of all searched properties of the resource
     *
     * @return the document extended by the resource locales
     */
    protected I_CmsSearchDocument appendFileSize(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        document.addFileSizeField(resource.getLength());

        return document;
    }

    /**
     * Extends the given document by the "res_locales" field.<p>
     *
     * @param document the document to extend
     * @param cms the OpenCms context used for building the search index
     * @param resource the resource that is indexed
     * @param extraction the plain text extraction result from the resource
     * @param properties the list of all properties directly attached to the resource (not searched)
     * @param propertiesSearched the list of all searched properties of the resource
     *
     * @return the document extended by the resource locales
     */
    protected I_CmsSearchDocument appendLocales(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extraction,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        return document;
    }

    /**
     * Extends the given document by fields for VFS path lookup.<p>
     *
     * @param document the document to extend
     * @param cms the OpenCms context used for building the search index
     * @param resource the resource that is indexed
     * @param extractionResult the plain text extraction result from the resource
     * @param properties the list of all properties directly attached to the resource (not searched)
     * @param propertiesSearched the list of all searched properties of the resource
     *
     * @return the document extended by fields for VFS path lookup
     */
    protected I_CmsSearchDocument appendPath(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        document.addPathField(resource.getRootPath());

        document.addRootPathField(resource.getRootPath());

        return document;
    }

    /**
     * Appends all direct properties, that are not empty or white space only to the document.<p>
     *
     * @param document the document to extend
     * @param cms the OpenCms context used for building the search index
     * @param resource the resource that is indexed
     * @param extraction the plain text extraction result from the resource
     * @param properties the list of all properties directly attached to the resource (not searched)
     * @param propertiesSearched the list of all searched properties of the resource
     *
     * @return the document extended by resource category information
     */
    protected I_CmsSearchDocument appendProperties(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extraction,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        return document;
    }

    /**
     * Extends the given document by a field that contains the resource type name.<p>
     *
     * @param document the document to extend
     * @param cms the OpenCms context used for building the search index
     * @param resource the resource that is indexed
     * @param extractionResult the plain text extraction result from the resource
     * @param properties the list of all properties directly attached to the resource (not searched)
     * @param propertiesSearched the list of all searched properties of the resource
     *
     * @return the document extended by a field that contains the resource type name
     *
     * @throws CmsLoaderException in case of errors identifying the resource type name
     */
    protected I_CmsSearchDocument appendType(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched)
    throws CmsLoaderException {

        // add the resource type to the document
        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource.getTypeId());
        String typeName = "VFS";
        if (type != null) {
            typeName = type.getTypeName();
        }
        document.addTypeField(typeName);

        // add the file name suffix to the document
        String resName = CmsResource.getName(resource.getRootPath());
        int index = resName.lastIndexOf('.');
        if ((index != -1) && (resName.length() > index)) {
            document.addSuffixField(resName.substring(index + 1));
        }
        return document;
    }

}

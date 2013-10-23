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

package org.opencms.search.fields;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategoryService;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.I_CmsSearchDocument;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Abstract implementation for OpenCms search field configurations.<p>
 * 
 * @since 8.5.0
 */
public class CmsSearchFieldConfiguration implements Comparable<CmsSearchFieldConfiguration>, I_CmsSearchFieldAppdender {

    /** A list of fields that should be lazy-loaded. */
    public static final List<String> LAZY_FIELDS = new ArrayList<String>();

    /** The name for the standard field configuration. */
    public static final String STR_STANDARD = "standard";

    /** The description of the configuration. */
    private String m_description;

    /** Map to lookup the configured {@link CmsSearchField} instances by name. */
    private Map<String, CmsSearchField> m_fieldLookup;

    /** The list of configured {@link CmsSearchField} names. */
    private List<String> m_fieldNames;

    /** The list of configured {@link CmsSearchField} instances. */
    private List<CmsSearchField> m_fields;

    /** The current index. */
    private CmsSearchIndex m_index;

    /** The name of the configuration. */
    private String m_name;

    /**
     * Creates a new, empty field configuration.<p>
     */
    public CmsSearchFieldConfiguration() {

        m_fields = new ArrayList<CmsSearchField>();
    }

    static {
        LAZY_FIELDS.add(CmsSearchField.FIELD_CONTENT);
        LAZY_FIELDS.add(CmsSearchField.FIELD_CONTENT_BLOB);
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

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldAppdender#addAdditionalFields()
     */
    public void addAdditionalFields() {

        // nothing to do here
    }

    /**
     * Adds a field to this search field configuration.<p>
     * 
     * @param field the field to add
     */
    public void addField(CmsSearchField field) {

        if (field != null) {
            m_fields.add(field);
        }
    }

    /**
     * Adds fields.<p>
     * 
     * @param fields the fields to add
     */
    public void addFields(Collection<CmsSearchField> fields) {

        for (CmsSearchField field : fields) {
            if (!getFieldNames().contains(field.getName())) {
                addField(field);
            }
        }
    }

    /**
     * Appends the specific search fields to the document.<p>
     * 
     * @see org.opencms.search.fields.I_CmsSearchFieldAppdender#appendFields(org.opencms.search.I_CmsSearchDocument, org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.search.extractors.I_CmsExtractionResult, java.util.List, java.util.List)
     */
    public I_CmsSearchDocument appendFields(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        return document;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(CmsSearchFieldConfiguration obj) {

        return m_name.compareTo(obj.getName());
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
        CmsSearchIndex index,
        I_CmsExtractionResult extraction) throws CmsException {

        m_index = index;

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
        document = setBoost(document, cms, resource, extraction, properties, propertiesSearched);
        document = appendFields(document, cms, resource, extraction, properties, propertiesSearched);

        return document;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if ((obj instanceof CmsSearchFieldConfiguration)) {
            return ((CmsSearchFieldConfiguration)obj).getName().equals(m_name);
        }
        return false;
    }

    /**
     * Returns the description of this field configuration.<p>
     * 
     * @return the description of this field configuration
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the configured {@link CmsSearchField} instance with the given name.<p>
     * 
     * @param name the search field name to look up
     * 
     * @return the configured {@link CmsSearchField} instance with the given name
     */
    public CmsSearchField getField(String name) {

        if (m_fieldLookup == null) {
            // lazy initialize the field names
            m_fieldLookup = new HashMap<String, CmsSearchField>();
            for (CmsSearchField field : m_fields) {
                m_fieldLookup.put(field.getName(), field);
            }
        }
        return m_fieldLookup.get(name);
    }

    /**
     * Returns the list of configured field names (Strings).<p>
     * 
     * @return the list of configured field names (Strings)
     */
    public List<String> getFieldNames() {

        if (m_fieldNames == null) {
            // lazy initialize the field names
            m_fieldNames = new ArrayList<String>();
            for (CmsSearchField field : m_fields) {
                m_fieldNames.add(field.getName());
            }
        }
        // create a copy of the list to prevent changes in other classes
        return new ArrayList<String>(m_fieldNames);
    }

    /**
     * Returns the list of configured {@link CmsSearchField} instances.<p>
     * 
     * @return the list of configured {@link CmsSearchField} instances
     */
    public List<CmsSearchField> getFields() {

        return m_fields;
    }

    /**
     * Returns the index.<p>
     *
     * @return the index
     */
    public CmsSearchIndex getIndex() {

        return m_index;
    }

    /**
     * Returns the name of this field configuration.<p>
     *
     * @return the name of this field configuration
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_name.hashCode();
    }

    /**
     * Initializes this field configuration.<p>
     */
    public void init() {

        addAdditionalFields();
    }

    /**
     * Sets the description of this field configuration.<p>
     * 
     * @param description the description to set
     */
    public void setDescription(String description) {

        m_description = description;
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
     * Sets the name of this field configuration.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        m_name = name;
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
        List<CmsProperty> propertiesSearched) throws CmsException {

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
        List<CmsProperty> propertiesSearched) throws CmsLoaderException {

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

    /**
     * Extends the given document with a boost factor.<p>
     * 
     * @param document the document to extend
     * @param cms the OpenCms context used for building the search index
     * @param resource the resource that is indexed
     * @param extractionResult the plain text extraction result from the resource
     * @param properties the list of all properties directly attached to the resource (not searched)
     * @param propertiesSearched the list of all searched properties of the resource  
     * 
     * @return the document extended by a boost factor
     */
    protected I_CmsSearchDocument setBoost(
        I_CmsSearchDocument document,
        CmsObject cms,
        CmsResource resource,
        I_CmsExtractionResult extractionResult,
        List<CmsProperty> properties,
        List<CmsProperty> propertiesSearched) {

        String value;
        // set individual document boost factor for the search
        float boost = CmsSearchField.BOOST_DEFAULT;
        // note that the priority property IS searched, so you can easily flag whole folders as "high" or "low"
        value = CmsProperty.get(CmsPropertyDefinition.PROPERTY_SEARCH_PRIORITY, propertiesSearched).getValue();
        if (value != null) {
            value = value.trim().toLowerCase();
            if (value.equals(I_CmsSearchDocument.SEARCH_PRIORITY_MAX_VALUE)) {
                boost = 2.0f;
            } else if (value.equals(I_CmsSearchDocument.SEARCH_PRIORITY_HIGH_VALUE)) {
                boost = 1.5f;
            } else if (value.equals(I_CmsSearchDocument.SEARCH_PRIORITY_LOW_VALUE)) {
                boost = 0.5f;
            }
        }
        if (boost != CmsSearchField.BOOST_DEFAULT) {
            // set individual document boost factor if required
            document.setBoost(boost);
        }

        return document;
    }
}

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

import org.opencms.util.CmsStringUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A abstract implementation for a search field.<p>
 * 
 * @since 8.5.0
 */
public class CmsSearchField implements Serializable {

    /** Th default boost factor (1.0), used in case no boost has been set for a field. */
    public static final float BOOST_DEFAULT = 1.0f;

    /** Name of the field that contains the (optional) category of the document (hardcoded). */
    public static final String FIELD_CATEGORY = "category";

    /** Name of the field that usually contains the complete content of the document (optional). */
    public static final String FIELD_CONTENT = "content";

    /** Name of the field that contains the complete extracted content of the document as serialized object (hardcoded). */
    public static final String FIELD_CONTENT_BLOB = "contentblob";

    /** Name of the field that contains the locale of the document. */
    public static final String FIELD_CONTENT_LOCALES = "con_locales";

    /** Name of the field that contains the document content date (hardcoded). */
    public static final String FIELD_DATE_CONTENT = "contentdate";

    /** Name of the field that contains the document creation date (hardcoded). */
    public static final String FIELD_DATE_CREATED = "created";

    /** Name of the field that contains the document creation date for fast lookup (hardcoded). */
    public static final String FIELD_DATE_CREATED_LOOKUP = "created_lookup";

    /** The field name for the expiration date. */
    public static final String FIELD_DATE_EXPIRED = "expired";

    /** Name of the field that contains the document last modification date (hardcoded). */
    public static final String FIELD_DATE_LASTMODIFIED = "lastmodified";

    /** Name of the field that contains the document last modification date for fast lookup (hardcoded). */
    public static final String FIELD_DATE_LASTMODIFIED_LOOKUP = "lastmodified_lookup";

    /** The lookup suffix for date fields. */
    public static final String FIELD_DATE_LOOKUP_SUFFIX = "_lookup";

    /** The field name for the release date. */
    public static final String FIELD_DATE_RELEASED = "released";

    /** The dependency type. */
    public static final String FIELD_DEPENDENCY_TYPE = "dependencyType";

    /** Name of the field that usually contains the value of the "Description" property of the document (optional). */
    public static final String FIELD_DESCRIPTION = "description";

    /** Name of the dynamic exact field. */
    public static final String FIELD_DYNAMIC_EXACT = "_exact";

    /** Name of the dynamic property field. */
    public static final String FIELD_DYNAMIC_PROPERTIES = "_prop";

    /** The name of the dynamic field that stores the shortened value of the content field in order to save performance. */
    public static final String FIELD_EXCERPT = "_excerpt";

    /** Name of the field that contains the filename. */
    public static final String FIELD_FILENAME = "filename";

    /** Name of the field that contains the documents structure id. */
    public static final String FIELD_ID = "id";

    /** Name of the field that usually contains the value of the "Keywords" property of the document (optional). */
    public static final String FIELD_KEYWORDS = "keywords";

    /** The field name for the link. */
    public static final String FIELD_LINK = "link";

    /** 
     * Name of the field that usually combines all document "meta" information, 
     * that is the values of the "Title", "Keywords" and "Description" properties (optional).
     */
    public static final String FIELD_META = "meta";

    /** Name of the field that contains the mime type. */
    public static final String FIELD_MIMETYPE = "mimetype";

    /** Name of the field that contains all VFS parent folders of a document (hardcoded). */
    public static final String FIELD_PARENT_FOLDERS = "parent-folders";

    /** Name of the field that contains the document root path in the VFS (hardcoded). */
    public static final String FIELD_PATH = "path";

    /** The prefix used to store dependency fields. */
    public static final String FIELD_PREFIX_DEPENDENCY = "dep_";

    /** The prefix for dynamic fields. */
    public static final String FIELD_PREFIX_DYNAMIC = "*_";

    /** The default text field prefix. */
    public static final String FIELD_PREFIX_TEXT = "text_";

    /** 
     * Name of the field that contains the (optional) document priority, 
     * which can be used to boost the document in the result list (hardcoded). 
     */
    public static final String FIELD_PRIORITY = "priority";

    /** Name of the field that contains the resource locales of the document. */
    public static final String FIELD_RESOURCE_LOCALES = "res_locales";

    /** The name of the score field. */
    public static final String FIELD_SCORE = "score";

    /** Name of the field that contains the searched property value of 'search.exclude'. */
    public static final String FIELD_SEARCH_EXCLUDE = "search_exclude";

    /** Name of the field that usually contains file size. */
    public static final String FIELD_SIZE = "size";

    /** Name of the field that contains the lower-case title, untokenized, for sorting. */
    public static final String FIELD_SORT_TITLE = "sort-title";

    /** Name of the field that contains the resource state. */
    public static final String FIELD_STATE = "state";

    /** Name of the field that contains the file name suffix of the resource. */
    public static final String FIELD_SUFFIX = "suffix";

    /** Name of the field that contains the general text of a resource and also serves as prefix. */
    public static final String FIELD_TEXT = "text";

    /** 
     * Name of the field that usually contains the value of the "Title" property of the document 
     * as a keyword used for sorting and also for retrieving the title text (optional).
     * 
     * Please note: This field should NOT be used for searching. Use {@link #FIELD_TITLE_UNSTORED} instead.<p>
     */
    public static final String FIELD_TITLE = "title-key";

    /** 
     * Name of the field that usually contains the value of the "Title" property of the document 
     * in an analyzed form used for searching in the title (optional).
     */
    public static final String FIELD_TITLE_UNSTORED = "title";

    /** Name of the field that contains the type of the document. */
    public static final String FIELD_TYPE = "type";

    /** Name of the field that contains the user created. */
    public static final String FIELD_USER_CREATED = "userCreated";

    /** Name of the field that contains the user last modified. */
    public static final String FIELD_USER_LAST_MODIFIED = "userLastModified";

    /** Name of the field that contains the latest version number of the resource. */
    public static final String FIELD_VERSION = "version";

    /** Serial version UID. */
    private static final long serialVersionUID = 3185631015824549119L;

    /** The boost factor of the field. */
    private float m_boost;

    /** A default value for the field in case the content does not provide the value. */
    private String m_defaultValue;

    /** Indicates if this field should be used for generating the excerpt. */
    private boolean m_excerpt;

    /** Indicates if the content of this field should be indexed. */
    private boolean m_indexed;

    /** The search field mappings. */
    private List<I_CmsSearchFieldMapping> m_mappings;

    /** The name of the field. */
    private String m_name;

    /** Indicates if the content of this field should be stored. */
    private boolean m_stored;

    /**
     * Creates a new search field.<p>
     */
    public CmsSearchField() {

        m_mappings = new ArrayList<I_CmsSearchFieldMapping>();
        m_boost = BOOST_DEFAULT;
    }

    /**
     * Creates a new search field.<p>
     * 
     * @param name the name of the field, see {@link #setName(String)}
     * @param defaultValue the default value to use, see {@link #setDefaultValue(String)}
     * @param boost the boost factor, see {@link #setBoost(float)}
     * 
     */
    public CmsSearchField(String name, String defaultValue, float boost) {

        this();
        m_name = name;
        m_boost = boost;
        m_defaultValue = defaultValue;
    }

    /**
     * Adds a new field mapping to the internal list of mappings.<p>
     * 
     * @param mapping the mapping to add
     */
    public void addMapping(I_CmsSearchFieldMapping mapping) {

        m_mappings.add(mapping);
    }

    /**
     * Two fields are equal if the name of the Lucene field is equal.<p>
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if ((obj instanceof CmsSearchField)) {
            return CmsStringUtil.isEqual(m_name, ((CmsSearchField)obj).getName());
        }
        return false;
    }

    /**
     * Returns the boost factor of this field.<p>
     *
     * The boost factor is a Lucene function that controls the "importance" of a field in the 
     * search result ranking. The default is <code>1.0</code>. A lower boost factor will make the field 
     * less important for the result ranking, a higher value will make it more important.<p>
     *
     * @return the boost factor of this field
     */
    public float getBoost() {

        return m_boost;
    }

    /**
     * Returns the default value to use if no content for this field was collected.<p>
     *
     * In case no default is configured, <code>null</code> is returned.<p>
     *
     * @return the default value to use if no content for this field was collected
     */
    public String getDefaultValue() {

        return m_defaultValue;
    }

    /**
     * Returns the String value state of this field if it is indexed (and possibly tokenized) in the index.<p>
     * 
     * <b>IMPORTANT:</b> Not supported by Solr
     * 
     * @return the String value state of this field if it is indexed (and possibly tokenized) in the index
     */
    public String getIndexed() {

        return null;
    }

    /**
     * Returns the mappings for this field.<p>
     * 
     * @return the mappings for this field
     */
    public List<I_CmsSearchFieldMapping> getMappings() {

        return m_mappings;
    }

    /**
     * Returns the name of this field in the Lucene search index.<p>
     *
     * @return the name of this field in the Lucene search index
     */
    public String getName() {

        return m_name;
    }

    /**
     * The hash code for a field is based only on the field name.<p>
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_name == null ? 41 : m_name.hashCode();
    }

    /**
     * Returns the indexed.<p>
     *
     * @return the indexed
     */
    public boolean isIndexed() {

        return m_indexed;
    }

    /**
     * Returns <code>true</code> if this fields content is used in the search result excerpt.<p>
     *
     * @return <code>true</code> if this fields content is used in the search result excerpt
     * 
     * @see #isStored()
     */
    public boolean isInExcerpt() {

        return m_excerpt;
    }

    /**
     * Returns <code>true</code> if the content of this field is stored in the Lucene index.<p>
     *
     * Please refer to the Lucene documentation about {@link org.apache.lucene.document.Field.Store}
     * for the concept behind stored and unstored fields.<p>
     *
     * @return <code>true</code> if the content of this field is stored in the Lucene index
     */
    public boolean isStored() {

        return m_stored;
    }

    /**
     * Sets the boost factor for this field.<p>
     *
     * The boost factor is a Lucene function that controls the "importance" of a field in the 
     * search result ranking. The default is <code>1.0</code>. A lower boost factor will make the field 
     * less important for the result ranking, a higher value will make it more important.<p>
     * 
     * <b>Use with caution:</b> You should only use this if you fully understand the concept behind 
     * boost factors. Otherwise it is likely that your result rankings will be worse then with 
     * the default values.<p>
     *
     * @param boost the boost factor to set
     */
    public void setBoost(float boost) {

        if (boost < 0.0F) {
            boost = 0.0F;
        }
        m_boost = boost;
    }

    /**
     * Sets the boost factor for this field from a String value.<p>
     * 
     * @param boostAsString the boost factor to set
     * 
     * @see #setBoost(float)
     */
    public void setBoost(String boostAsString) {

        try {
            setBoost(Float.valueOf(boostAsString).floatValue());
        } catch (NumberFormatException e) {
            setBoost(1.0F);
        }
    }

    /**
     * Sets the default value to use if no content for this field was collected.<p>
     *
     * @param defaultValue the default value to set
     */
    public void setDefaultValue(String defaultValue) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(defaultValue)) {
            m_defaultValue = defaultValue.trim();
        } else {
            m_defaultValue = null;
        }
    }

    /**
     * Controls if the content of this field is indexed (and possibly tokenized) in the Lucene index.<p> 
     *
     * @param indexed the indexed to set
     */
    public void setIndexed(boolean indexed) {

        m_indexed = indexed;
    }

    /**
     * Controls if this fields content is used in the search result excerpt.<p>
     *
     * @param excerpt if <code>true</code>, then this fields content is used in the search excerpt
     */
    public void setInExcerpt(boolean excerpt) {

        m_excerpt = excerpt;
    }

    /**
     * Sets the name of this field in the Lucene search index.<p>
     *
     * @param fieldName the name to set
     */
    public void setName(String fieldName) {

        m_name = fieldName;
    }

    /**
     * Controls if the content of this field is stored in the Lucene index.<p>
     *
     * Please refer to the Lucene documentation about {@link org.apache.lucene.document.Field.Store}
     * for the concept behind stored and unstored fields.<p>
     *
     * @param stored if <code>true</code>, then the field content is stored
     */
    public void setStored(boolean stored) {

        m_stored = stored;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return getName();
    }
}
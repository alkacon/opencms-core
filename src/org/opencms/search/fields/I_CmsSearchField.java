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

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import org.apache.lucene.document.Fieldable;

/**
 * The interface for search field implementations.<p>
 * 
 * @since 8.5.0
 */
public interface I_CmsSearchField extends Serializable {

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
    public static final String FIELD_DATE_RELEASED = "relased";

    /** The dependency type. */
    public static final String FIELD_DEPENDENCY_TYPE = "dependencyType";

    /** Name of the field that usually contains the value of the "Description" property of the document (optional). */
    public static final String FIELD_DESCRIPTION = "description";

    /** Name of the dynamic exact field. */
    public static final String FIELD_DYNAMIC_EXACT = "_exact";

    /** Name of the dynamic property field. */
    public static final String FIELD_DYNAMIC_PROPERTIES = "_prop";

    /** Name of the field that contains the documents structure id. */
    public static final String FIELD_ID = "id";

    /** Name of the field that usually contains the value of the "Keywords" property of the document (optional). */
    public static final String FIELD_KEYWORDS = "keywords";

    /** 
     * Name of the field that usually combines all document "meta" information, 
     * that is the values of the "Title", "Keywords" and "Description" properties (optional).
     */
    public static final String FIELD_META = "meta";

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

    /** Name of the field that contains the file name suffix of the resource. */
    public static final String FIELD_SUFFIX = "suffix";

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

    /**
     * Adds a new field mapping to the internal list of mappings.<p>
     * 
     * @param mapping the mapping to add
     */
    void addMapping(I_CmsSearchFieldMapping mapping);

    /**
     * Creates a field from the configuration and the provided content.<p>
     * 
     * The configured name of the field as provided by {@link #getName()} is used.<p>
     * 
     * If no valid content is provided (that is the content is either <code>null</code> or 
     * only whitespace), then no field is created and <code>null</code> is returned.<p>
     * 
     * @param content the content to create the field with
     * 
     * @return a field created from the configuration and the provided content
     */
    Fieldable createField(String content);

    /**
     * Creates a field with the given name from the configuration and the provided content.<p>
     * 
     * If no valid content is provided (that is the content is either <code>null</code> or 
     * only whitespace), then no field is created and <code>null</code> is returned.<p>
     * 
     * @param name the name of the field to create
     * @param content the content to create the field with
     * 
     * @return a field with the given name from the configuration and the provided content
     */
    Fieldable createField(String name, String content);

    /**
     * Returns the boost factor of this field.<p>
     *
     * The boost factor is a Lucene function that controls the "importance" of a field in the 
     * search result ranking. The default is <code>1.0</code>. A lower boost factor will make the field 
     * less important for the result ranking, a higher value will make it more important.<p>
     *
     * @return the boost factor of this field
     */
    float getBoost();

    /**
     * Returns the default value to use if no content for this field was collected.<p>
     *
     * In case no default is configured, <code>null</code> is returned.<p>
     *
     * @return the default value to use if no content for this field was collected
     */
    String getDefaultValue();

    /**
     * Returns the locale of this field or <code>null</code> if the field does not have a locale.<p>
     * 
     * @return the locale of this field
     */
    Locale getLocale();

    /**
     * Returns the mappings for this field.<p>
     * 
     * @return the mappings for this field
     */
    List<I_CmsSearchFieldMapping> getMappings();

    /**
     * Returns the name of this field in the Lucene search index.<p>
     *
     * @return the name of this field in the Lucene search index
     */
    String getName();

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
    void setBoost(float boost);

    /**
     * Sets the boost factor for this field from a String value.<p>
     * 
     * @param boostAsString the boost factor to set
     * 
     * @see #setBoost(float)
     */
    void setBoost(String boostAsString);

    /**
     * Sets the default value to use if no content for this field was collected.<p>
     *
     * @param defaultValue the default value to set
     */
    void setDefaultValue(String defaultValue);

    /**
     * Sets the name of this field in the Lucene search index.<p>
     *
     * @param fieldName the name to set
     */
    void setName(String fieldName);
}

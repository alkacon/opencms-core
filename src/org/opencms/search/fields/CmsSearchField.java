/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/fields/CmsSearchField.java,v $
 * Date   : $Date: 2008/08/15 16:08:22 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2008 Alkacon Software GmbH (http://www.alkacon.com)
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

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;

/**
 * An individual field configuration in a search index.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 7.0.0 
 */
public class CmsSearchField {

    /** Th default boost factor (1.0), used in case no boost has been set for a field. */
    public static final float BOOST_DEFAULT = 1.0f;

    /** Name of the field that contains the (optional) category of the document (hardcoded). */
    public static final String FIELD_CATEGORY = "category";

    /** Name of the field that usually contains the complete content of the document (optional). */
    public static final String FIELD_CONTENT = "content";

    /** Name of the field that contains the complete extracted content of the document as serialized object (hardcoded). */
    public static final String FIELD_CONTENT_BLOB = "contentblob";

    /** Name of the field that contains the document content date (hardcoded). */
    public static final String FIELD_DATE_CONTENT = "contentdate";

    /** Name of the field that contains the document creation date (hardcoded). */
    public static final String FIELD_DATE_CREATED = "created";

    /** Name of the field that contains the document last modification date (hardcoded). */
    public static final String FIELD_DATE_LASTMODIFIED = "lastmodified";

    /** Name of the field that usually contains the value of the "Description" property of the document (optional). */
    public static final String FIELD_DESCRIPTION = "description";

    /** Name of the field that usually contains the value of the "Keywords" property of the document (optional). */
    public static final String FIELD_KEYWORDS = "keywords";

    /** 
     * Name of the field that usually combines all document "meta" information, 
     * that is the values of the "Title", "Keywords" and "Description" properties (optional).
     */
    public static final String FIELD_META = "meta";

    /** Name of the field that contains the document root path in the VFS (hardcoded). */
    public static final String FIELD_PATH = "path";

    /** 
     * Name of the field that contains the (optional) document priority, 
     * which can be used to boost the document in the result list (hardcoded). 
     */
    public static final String FIELD_PRIORITY = "priority";

    /** Name of the field that contains a special format of the document root path in the VFS for optimized searches (hardcoded). */
    public static final String FIELD_ROOT = "root";

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

    /** Value of m_displayName if field should not be displayed. */
    public static final String IGNORE_DISPLAY_NAME = "-";

    /** Constant for the "compress" index setting. */
    public static final String STR_COMPRESS = "compress";

    /** Constant for the "no" index setting. */
    public static final String STR_NO = "no";

    /** Constant for the "tokenized" index setting. */
    public static final String STR_TOKENIZED = "tokenized";

    /** Constant for the "untokenized" index setting. */
    public static final String STR_UN_TOKENIZED = "untokenized";

    /** Constant for the "yes" index setting. */
    public static final String STR_YES = "yes";

    /** The special analyzer to use for this field. */
    private Analyzer m_analyzer;

    /** The boost factor of the field. */
    private float m_boost;

    /** Indicates if the content of this field is compressed. */
    private boolean m_compressed;

    /** A default value for the field in case the content does not provide the value. */
    private String m_defaultValue;

    /** Indicates if this field should be displayed. */
    private boolean m_displayed;

    /** The display name of the field. */
    private String m_displayName;

    /** The display name set from the configuration. */
    private String m_displayNameForConfiguration;

    /** Indicates if this field should be used for generating the excerpt. */
    private boolean m_excerpt;

    /** Indicates if the content of this field should be indexed. */
    private boolean m_indexed;

    /** The search field mappings. */
    private List m_mappings;

    /** The name of the field. */
    private String m_name;

    /** Indicates if the content of this field should be stored. */
    private boolean m_stored;

    /** Indicates if the content of this field should be tokenized. */
    private boolean m_tokenized;

    /**
     * Creates a new search field configuration.<p>
     */
    public CmsSearchField() {

        m_mappings = new ArrayList();
        m_boost = BOOST_DEFAULT;
    }

    /**
     * Creates a new search field configuration.<p>
     * 
     * The field will be tokenized if it is indexed.
     * The field will not be in the excerpt. 
     * The boost value is the default, that is no special boost is used.
     * There is no default value.<p> 
     * 
     * @param name the name of the field, see {@link #setName(String)}
     * @param displayName the display name of this field, see {@link #setDisplayName(String)}
     * @param isStored controls if the field is stored and in the excerpt, see {@link #setStored(boolean)}
     * @param isIndexed controls if the field is indexed and tokenized, see {@link #setIndexed(boolean)}
     */
    public CmsSearchField(String name, String displayName, boolean isStored, boolean isIndexed) {

        this(name, displayName, isStored, isIndexed, isIndexed, false, BOOST_DEFAULT, null);
    }

    /**
     * Creates a new search field configuration.<p>
     * 
     * @param name the name of the field, see {@link #setName(String)}
     * @param displayName the display name of this field, see {@link #setDisplayName(String)}
     * @param isStored controls if the field is stored, see {@link #setStored(boolean)}
     * @param isCompressed controls if the filed is compressed, see {@link #setCompressed(boolean)}
     * @param isIndexed controls if the field is indexed, see {@link #setIndexed(boolean)}
     * @param isTokenized controls if the field is tokenized, see {@link #setStored(boolean)}
     * @param isInExcerpt controls if the field is in the excerpt, see {@link #isInExcerptAndStored()}
     * @param analyzer the Lucene analyzer to use for this field
     * @param boost the boost factor for the field, see {@link #setBoost(float)}
     * @param defaultValue the default value for the field, see {@link #setDefaultValue(String)}
     */
    public CmsSearchField(
        String name,
        String displayName,
        boolean isStored,
        boolean isCompressed,
        boolean isIndexed,
        boolean isTokenized,
        boolean isInExcerpt,
        Analyzer analyzer,
        float boost,
        String defaultValue) {

        this();
        setDisplayName(displayName);
        setName(name);
        setStored(isStored);
        setCompressed(isCompressed);
        setIndexed(isIndexed);
        setTokenized(isTokenized);
        setInExcerpt(isInExcerpt);
        setAnalyzer(analyzer);
        setBoost(boost);
        setDefaultValue(defaultValue);
    }

    /**
     * Creates a new search field configuration.<p>
     * 
     * @param name the name of the field, see {@link #setName(String)}
     * @param displayName the display name of this field, see {@link #setDisplayName(String)}
     * @param isStored controls if the field is stored, see {@link #setStored(boolean)}
     * @param isIndexed controls if the field is indexed, see {@link #setIndexed(boolean)}
     * @param isTokenized controls if the field is tokenized, see {@link #setStored(boolean)}
     * @param isInExcerpt controls if the field is in the excerpt, see {@link #isInExcerptAndStored()}
     * @param boost the boost factor for the field, see {@link #setBoost(float)}
     * @param defaultValue the default value for the field, see {@link #setDefaultValue(String)}
     */
    public CmsSearchField(
        String name,
        String displayName,
        boolean isStored,
        boolean isIndexed,
        boolean isTokenized,
        boolean isInExcerpt,
        float boost,
        String defaultValue) {

        this(name, displayName, isStored, false, isIndexed, isTokenized, isInExcerpt, null, boost, defaultValue);
    }

    /**
     * Adds a new field mapping to the internal list of mappings.<p>
     * 
     * @param mapping the mapping to add
     */
    public void addMapping(CmsSearchFieldMapping mapping) {

        m_mappings.add(mapping);
    }

    /**
     * Creates a Lucene field from the configuration and the provided content.<p>
     * 
     * If no valid content is provided (ie. the content is either <code>null</code> or 
     * only whitespace), then no field is created and <code>null</code> is returned.<p>
     * 
     * @param content the content to create the field with
     * 
     * @return a Lucene field created from the configuration and the provided conten
     */
    public Field createField(String content) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(content)) {
            content = getDefaultValue();
        }
        if (content != null) {

            Index index = Field.Index.NO;
            if (isIndexed()) {
                if (isTokenizedAndIndexed()) {
                    index = Field.Index.TOKENIZED;
                } else {
                    index = Field.Index.UN_TOKENIZED;
                }
            }
            Field.Store store = Field.Store.NO;
            if (isCompressed()) {
                store = Field.Store.COMPRESS;
            } else if (isStored()) {
                store = Field.Store.YES;
            }
            Field result = new Field(getName(), content, store, index);
            if (getBoost() != BOOST_DEFAULT) {
                result.setBoost(getBoost());
            }
            return result;
        }
        return null;
    }

    /**
     * Two fields are equal if the name of the Lucene field is equal.<p>
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (obj instanceof CmsSearchField) {
            return CmsStringUtil.isEqual(m_name, ((CmsSearchField)obj).m_name);
        }
        return false;
    }

    /**
     * Returns the analyzer used for this field.<p>
     *
     * @return the analyzer used for this field
     */
    public Analyzer getAnalyzer() {

        return m_analyzer;
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
     * Returns the boost factor of thsi field as String value for display use.<p>
     * 
     * @return the boost factor of thsi field as String value for display use
     */
    public String getBoostDisplay() {

        if (m_boost == BOOST_DEFAULT) {
            return null;
        }
        return String.valueOf(m_boost);
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
     * Returns the display name of the field.<p>
     * 
     * @return the display name of the field
     */
    public String getDisplayName() {

        if (!isDisplayed()) {
            return IGNORE_DISPLAY_NAME;
        }
        if (m_displayName == null) {
            return m_name;
        } else {
            return m_displayName;
        }
    }

    /**
     * Returns the displayNameForConfiguration.<p>
     *
     * @return the displayNameForConfiguration
     */
    public String getDisplayNameForConfiguration() {

        return m_displayNameForConfiguration;
    }

    /**
     * Returns the String value state of this field if it is indexed (and possibly tokenized) in the Lucene index.<p>
     * 
     * @return the String value state of this field if it is indexed (and possibly tokenized) in the Lucene index
     * 
     * @see #isTokenizedAndIndexed()
     * @see #isIndexed()
     */
    public String getIndexed() {

        if (isTokenizedAndIndexed()) {
            return String.valueOf(isTokenizedAndIndexed());
        }
        if (isIndexed()) {
            return STR_UN_TOKENIZED;
        } else {
            return String.valueOf(isIndexed());
        }
    }

    /**
     * Returns the mappings for this field.<p>
     * 
     * @return the mappings for this field
     */
    public List getMappings() {

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
    public int hashCode() {

        return (m_name == null) ? 41 : m_name.hashCode();
    }

    /**
     * Returns <code>true</code> if the content of this field is compressed.<p>
     *
     * If the field is compressed, it must also be stored, this means 
     * {@link #isStored()} will always return <code>true</code> for compressed fields.<p>
     *
     * @return <code>true</code> if the content of this field is compressed
     */
    public boolean isCompressed() {

        return m_compressed;
    }

    /**
     * Returns true if the field should be displayed.<p>
     * 
     * @return returns true if the field should be displayed otherwise false
     */
    public boolean isDisplayed() {

        return m_displayed;
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
     * Returns <code>true</code> if this fields content is used in the search result excerpt.<p>
     *
     * A field can only be used in the excerpt if it is stored, see {@link #isStored()}.<p>
     *
     * @return <code>true</code> if this fields content is used in the search result excerpt
     * 
     * @see #isStored()
     */
    public boolean isInExcerptAndStored() {

        return m_excerpt && m_stored;
    }

    /**
     * Returns <code>true</code> if the content of this field is stored in the Lucene index.<p>
     *
     * Please refer to the Lucene documentation about {@link org.apache.lucene.document.Field.Store}
     * for the concept behind stored and unstored fields.<p>
     *
     * @return <code>true</code> if the content of this field is stored in the Lucene index
     * 
     * @see #isTokenizedAndIndexed()
     */
    public boolean isStored() {

        return m_stored;
    }

    /**
     * Returns <code>true</code> if the content of this field is tokenized in the Lucene index.<p>
     * 
     * Please refer to the Lucene documentation about {@link org.apache.lucene.document.Field.Index}
     * for the concept behind tokenized and untokenized fields.<p>
     *
     * @return <code>true</code> if the content of this field is tokenized in the Lucene index
     */
    public boolean isTokenized() {

        return m_tokenized;
    }

    /**
     * Returns <code>true</code> if the content of this field is tokenized in the Lucene index.<p>
     * 
     * A field can only be tokenized if it is also indexed, see {@link #isIndexed()}.<p>
     * 
     * Please refer to the Lucene documentation about {@link org.apache.lucene.document.Field.Index}
     * for the concept behind tokenized and untokenized fields.<p>
     *
     * @return <code>true</code> if the content of this field is tokenized in the Lucene index
     * 
     * @see #isStored()
     * @see #isIndexed()
     */
    public boolean isTokenizedAndIndexed() {

        return m_tokenized && m_indexed;
    }

    /**
     * Sets the analyzer used for this field.<p>
     *
     * @param analyzer the analyzer to set
     */
    public void setAnalyzer(Analyzer analyzer) {

        m_analyzer = analyzer;
    }

    /**
     * Sets the analyzer used for this field.<p>
     *
     * The parameter must be a name of a class the implements the Lucene {@link Analyzer} interface.
     *
     * @param analyzer the analyzer class name to set
     * 
     * @throws Exception in case of problems creating the analyzer class instance
     */
    public void setAnalyzer(String analyzer) throws Exception {

        Class analyzerClass;
        try {
            analyzerClass = Class.forName(analyzer);
        } catch (ClassNotFoundException e) {
            // allow Lucene standard classes to be written in a short form
            analyzerClass = Class.forName("org.apache.lucene.analysis." + analyzer);
        }
        setAnalyzer((Analyzer)analyzerClass.newInstance());
    }

    /**
     * Sets the boost factor for this field.<p>
     *
     * The boost factor is a Lucene function that controls the "importance" of a field in the 
     * search result ranking. The default is <code>1.0</code>. A lower boost factor will make the field 
     * less important for the result ranking, a higher value will make it more important.<p>
     * 
     * <b>Use with caution:</b> You should only use this if you fully understand the concept behind 
     * Lucene boost factors. Otherwise it is likley that your result rankings will be worse then with 
     * the default values.<p>
     *
     * @param boost the boost factor to set
     */
    public void setBoost(float boost) {

        if (boost < 0.0f) {
            boost = 0.0f;
        }
        m_boost = boost;
    }

    /**
     * Sets the boost factor for this field from a String value.<p>
     * 
     * @param boost the boost factor to set
     * 
     * @see #setBoost(float)
     */
    public void setBoost(String boost) {

        try {
            setBoost(Float.valueOf(boost).floatValue());
        } catch (NumberFormatException e) {
            // invalid number format, use default boost factor
            setBoost(BOOST_DEFAULT);
        }
    }

    /**
     * Sets the boost factor of this field (only for display use).<p>
     * 
     * @param boost the boost factor to set
     * 
     * @see #setBoost(String)
     */
    public void setBoostDisplay(String boost) {

        setBoost(boost);
    }

    /**
     * Controls if this field value will be stored compressed or not.<p>
     *
     * If this is set to <code>true</code>, the value for {@link #isStored()} will also 
     * be set to <code>true</code>, since compressed fields are always stored.<p>
     *
     * @param compressed if <code>true</code>, the field value will be stored compressed
     */
    public void setCompressed(boolean compressed) {

        m_compressed = compressed;
        if (compressed) {
            setStored(true);
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
     * Controls if the field is displayed or not.<p> 
     * 
     * @param displayed if true the field is displayed
     */
    public void setDisplayed(boolean displayed) {

        m_displayed = displayed;
    }

    /**
     * Sets the display name. If the given name equals IGNORE_DISPLAY_NAME the field is not displayed.<p> 
     * 
     * @param displayName the display name to set
     */
    public void setDisplayName(String displayName) {

        if (CmsStringUtil.isEmpty(displayName) || (IGNORE_DISPLAY_NAME.equals(displayName))) {
            m_displayName = null;
            setDisplayed(false);
        } else {
            m_displayName = displayName;
            m_displayNameForConfiguration = displayName;
            setDisplayed(true);
        }
    }

    /**
     * Sets the displayNameForConfiguration.<p>
     *
     * @param displayNameForConfiguration the displayNameForConfiguration to set
     */
    public void setDisplayNameForConfiguration(String displayNameForConfiguration) {

        m_displayNameForConfiguration = displayNameForConfiguration;
        setDisplayName(displayNameForConfiguration);
    }

    /**
     * Controls if the content of this field is indexed (and possibly tokenized) in the Lucene index.<p> 
     *
     * @param indexed the indexed to set
     * 
     * @see #setTokenized(boolean)
     */
    public void setIndexed(boolean indexed) {

        m_indexed = indexed;
    }

    /**
     * Controls if the content of this field is indexed (and possibly tokenized) in the Lucene index from a String parameter.<p> 
     * 
     * This sets the values for {@link #isIndexed()} as well as {@link #isTokenizedAndIndexed()}.<p>
     * 
     * The parameter can have the following values:
     * <ul>
     * <li><b>"true"</b> or <b>"tokenized"</b>: The field is indexed and tokenized.
     * <li><b>"false"</b> or <b>"no"</b>: The field is not indexed and not tokenized.
     * <li><b>"untokenized"</b>: The field is indexed but not tokenized.
     * </ul>
     * 
     * @param indexed the index setting to use
     * 
     * @see #setIndexed(boolean)
     * @see #setTokenized(boolean)
     */
    public void setIndexed(String indexed) {

        boolean isIndexed = false;
        boolean isTokenized = false;
        if (indexed != null) {
            indexed = indexed.trim().toLowerCase();
            if (STR_TOKENIZED.equals(indexed)) {
                isIndexed = true;
                isTokenized = true;
            } else if (STR_UN_TOKENIZED.equals(indexed)) {
                isIndexed = true;
            } else if (STR_NO.equals(indexed)) {
                // "no", both values will be false
            } else {
                // only "true" or "false" remain
                isIndexed = Boolean.valueOf(indexed).booleanValue();
                isTokenized = isIndexed;
            }
        }
        setIndexed(isIndexed);
        setTokenized(isTokenized);
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
     * Controls if this fields content is used in the search result excerpt.<p>
     * 
     * @param excerpt if <code>"true"</code>, then this fields content is used in the search excerpt
     * 
     * @see #setInExcerpt(boolean)
     */
    public void setInExcerpt(String excerpt) {

        setInExcerpt(Boolean.valueOf(String.valueOf(excerpt)).booleanValue());
    }

    /**
     * Sets the name of this field in the Lucene search index.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Controls if the content of this field is stored in the Lucene index.<p>
     *
     * Please refer to the Lucene documentation about {@link org.apache.lucene.document.Field.Store}
     * for the concept behind stored and unstored fields.<p>
     *
     * @param stored if <code>true</code>, then the field content is stored
     * 
     * @see #setTokenized(boolean)
     */
    public void setStored(boolean stored) {

        m_stored = stored;
    }

    /**
     * Controls if the content of this field is stored in the Lucene index from a String parameter.<p> 
     * 
     * @param stored if <code>"true"</code>, then the field content is stored
     * 
     * @see #setStored(boolean)
     */
    public void setStored(String stored) {

        boolean isStored = false;
        boolean isCompressed = false;
        if (stored != null) {
            stored = stored.trim().toLowerCase();
            if (STR_COMPRESS.equals(stored)) {
                isCompressed = true;
                isStored = true;
            } else if (STR_YES.equals(stored)) {
                // "yes", value will be stored but not compressed
                isStored = true;
            } else {
                // only "true" or "false" remain
                isStored = Boolean.valueOf(stored).booleanValue();
            }
        }
        setStored(isStored);
        setCompressed(isCompressed);
    }

    /**
     * Controls if the content of this field is tokenized in the Lucene index.<p>
     *
     * Please refer to the Lucene documentation about {@link org.apache.lucene.document.Field.Index}
     * for the concept behind tokenized and untokenized fields.<p>
     *
     * @param tokenized if <code>true</code>, then the field content is tokenized
     * 
     * @see #setStored(boolean)
     */
    public void setTokenized(boolean tokenized) {

        m_tokenized = tokenized;
    }
}
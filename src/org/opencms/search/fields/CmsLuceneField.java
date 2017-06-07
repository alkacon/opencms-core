/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

import org.opencms.search.CmsSearchManager;
import org.opencms.util.CmsStringUtil;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;

/**
 * An individual field configuration in a Lucene search index.<p>
 *
 * @since 7.0.0
 */
public class CmsLuceneField extends CmsSearchField {

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

    /** The serial version UID. */
    private static final long serialVersionUID = -4946013624087640706L;

    /** The special analyzer to use for this field. */
    private Analyzer m_analyzer;

    /** The boost factor of the field. */
    private float m_boost;

    /** Indicates if the content of this field is compressed. */
    private boolean m_compressed;

    /** Indicates if this field should be displayed. */
    private boolean m_displayed;

    /** The display name of the field. */
    private String m_displayName;

    /** The display name set from the configuration. */
    private String m_displayNameForConfiguration;

    /** Indicates if the content of this field should be tokenized. */
    private boolean m_tokenized;

    /** The type used to convert a field to a Solr field. */
    private String m_type;

    /**
     * Creates a new search field configuration.<p>
     */
    public CmsLuceneField() {

        super();
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
    public CmsLuceneField(String name, String displayName, boolean isStored, boolean isIndexed) {

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
     * @param analyzer the analyzer to use, see {@link #setAnalyzer(Analyzer)}
     * @param boost the boost factor for the field, see {@link #setBoost(float)}
     * @param defaultValue the default value for the field, see {@link #setDefaultValue(String)}
     */
    public CmsLuceneField(
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

        super(name, defaultValue, boost);
        setDisplayName(displayName);
        setStored(isStored);
        setCompressed(isCompressed);
        setIndexed(isIndexed);
        setTokenized(isTokenized);
        setInExcerpt(isInExcerpt);
        setAnalyzer(analyzer);
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
    public CmsLuceneField(
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
    public Field createField(String content) {

        return createField(getName(), content);
    }

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
    public Field createField(String name, String content) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(content)) {
            content = getDefaultValue();
        }
        if (content != null) {
            final FieldType ft = new FieldType();
            if (isIndexed()) {
                if (isTokenizedAndIndexed()) {
                    ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
                    ft.setTokenized(true);
                } else {
                    ft.setIndexOptions(IndexOptions.DOCS);
                    ft.setTokenized(false);
                }
            }
            ft.setStored(isStored() || isCompressed());
            Field result = new Field(name, content, ft);
            if (getBoost() != BOOST_DEFAULT) {
                result.setBoost(getBoost());
            }
            return result;
        }
        return null;
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
     * Returns the boost factor of this field as String value for display use.<p>
     *
     * @return the boost factor of this field as String value for display use
     */
    public String getBoostDisplay() {

        if (m_boost == BOOST_DEFAULT) {
            return null;
        }
        return String.valueOf(m_boost);
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
            return getName();
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
    @Override
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
     * Returns the type.<p>
     *
     * @return the type
     */
    public String getType() {

        return m_type;
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
     * Returns <code>true</code> if this fields content is used in the search result excerpt.<p>
     *
     * A field can only be used in the excerpt if it is stored, see {@link #isStored()}.<p>
     *
     * @return <code>true</code> if this fields content is used in the search result excerpt
     *
     * @see #isStored()
     */
    public boolean isInExcerptAndStored() {

        return isInExcerpt() && isStored();
    }

    /**
     * Returns <code>true</code> if the content of this field is tokenized in the Lucene index.<p>
     *
     * Please refer to the Lucene documentation about the concept behind tokenized and untokenized fields.<p>
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
     * Please refer to the Lucene documentation about the concept behind tokenized and untokenized fields.<p>
     *
     * @return <code>true</code> if the content of this field is tokenized in the Lucene index
     *
     * @see #isStored()
     * @see #isIndexed()
     */
    public boolean isTokenizedAndIndexed() {

        return m_tokenized && isIndexed();
    }

    /**
     * Closes the analyzer.<p>
     */
    public void closeAnalyzer() {

        if (m_analyzer != null) {
            m_analyzer.close();
        }
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
     * @param analyzerName the analyzer class name to set
     *
     * @throws Exception in case of problems creating the analyzer class instance
     */
    public void setAnalyzer(String analyzerName) throws Exception {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(analyzerName)) {
            setAnalyzer(CmsSearchManager.getAnalyzer(analyzerName));
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
     * @param excerpt if <code>"true"</code>, then this fields content is used in the search excerpt
     *
     * @see #setInExcerpt(boolean)
     */
    public void setInExcerpt(String excerpt) {

        setInExcerpt(Boolean.valueOf(String.valueOf(excerpt)).booleanValue());
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
     * Please refer to the Lucene documentation about the concept behind tokenized and untokenized fields.<p>
     *
     * @param tokenized if <code>true</code>, then the field content is tokenized
     *
     * @see #setStored(boolean)
     */
    public void setTokenized(boolean tokenized) {

        m_tokenized = tokenized;
    }

    /**
     * Sets the type.<p>
     *
     * @param type the type to set
     */
    public void setType(String type) {

        m_type = type;
    }
}
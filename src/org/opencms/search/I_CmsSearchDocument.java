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
 * Lesser General License for more details.
 *
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.search;

import org.opencms.relations.CmsCategory;
import org.opencms.search.fields.CmsSearchField;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * The interface for search documents.<p>
 */
public interface I_CmsSearchDocument {

    /** Value for "high" search priority. */
    String SEARCH_PRIORITY_HIGH_VALUE = "high";

    /** Value for "low" search priority. */
    String SEARCH_PRIORITY_LOW_VALUE = "low";

    /** Value for "maximum" search priority. */
    String SEARCH_PRIORITY_MAX_VALUE = "max";

    /** Value for "normal" search priority. */
    String SEARCH_PRIORITY_NORMAL_VALUE = "normal";

    /** The VFS prefix for document keys. */
    String VFS_DOCUMENT_KEY_PREFIX = "VFS";

    /**
     * Adds the list of the given categories to this document.<p>
     *
     * @param categories the categories to add
     */
    void addCategoryField(List<CmsCategory> categories);

    /**
     * Adds the given content byte array to this document.<p>
     *
     * @param content the content to add
     */
    void addContentField(byte[] content);

    /**
     * Adds the locales of the content to this document.<p>
     *
     * @param locales the locales of the content
     */
    void addContentLocales(Collection<Locale> locales);

    /**
     * Puts the given date into the field with the given name.<p>
     *
     * @param name the name to put the date in
     * @param date the date to pu into the field
     * @param analyzed <code>true</code> if the inserted value should be analyzable
     */
    void addDateField(String name, long date, boolean analyzed);

    /**
     * Adds the given file size as field to this document.<p>
     *
     * @param length the length
     */
    void addFileSizeField(int length);

    /**
     * Puts the given path into this document.<p>
     *
     * @param rootPath the given path into this document
     */
    void addPathField(String rootPath);

    /**
     * Adds the locales of the resource to this document.<p>
     *
     * @param locales the locales of the resource
     */
    void addResourceLocales(Collection<Locale> locales);

    /**
     * Puts the given root path into its default field.<p>
     *
     * @param rootPath the root path to put into the field
     */
    void addRootPathField(String rootPath);

    /**
     * Adds a dynamic search field to the index.<p>
     *
     * @param field the field
     * @param value the value
     */
    void addSearchField(CmsSearchField field, String value);

    /**
     * Adds the suffix field to the document. This field should contain the resource suffix.<p>
     *
     * <b>Example</b><br/>
     * <code>'html' for a file named 'article.html'</code>
     *
     * @param suffix the suffix to add
     */
    void addSuffixField(String suffix);

    /**
     * Adds the resource type to this document.<p>
     * @param paramString
     */
    void addTypeField(String paramString);

    /**
     * Returns the content blob of this document.<p>
     *
     * @return the content blob
     */
    byte[] getContentBlob();

    /**
     * Returns the concrete document as Object to be cast if necessary.<p>
     *
     * @return the document as Object
     */
    Object getDocument();

    /**
     * Returns all field names of this document.<p>
     *
     * @return the field names
     */
    List<String> getFieldNames();

    /**
     * Tries to return the value of the field for the given name as Date,
     * <code>null</code> if the field is empty or if the field is not of the type date.<p>
     *
     * @param fieldName the name of the field to get the Date value for
     *
     * @return the date or <code>null</code>
     */
    Date getFieldValueAsDate(String fieldName);

    /**
     * Returns the value of the field for the given name as String.<p>
     *
     * @param fieldName the name of the field to get the String value for
     *
     * @return the String value or <code>null</code> if empty
     */
    String getFieldValueAsString(String fieldName);

    /**
     * Returns a list of Strings representing the values of an multi valued field.<p>
     *
     * @param fieldName the name of the multi valued field to get the content of
     *
     * @return the list of Strings, or <code>null</code>
     */
    List<String> getMultivaluedFieldAsStringList(String fieldName);

    /**
     * Returns the root path of the referenced VFS resource of this document.<p>
     *
     * @return the root path
     */
    String getPath();

    /**
     * Returns the score for this document.<p>
     *
     * @return the score
     */
    float getScore();

    /**
     * Returns the resource type of the referenced VFS resource of this document.<p>
     *
     * @return the type
     */
    String getType();

    /**
     * Sets the boost factor for the whole document.<p>
     *
     * @param boost the factor to set
     */
    void setBoost(float boost);

    /**
     * Sets the score for this document.<p>
     *
     * @param score the score
     */
    void setScore(float score);
}

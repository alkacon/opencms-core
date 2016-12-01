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

package org.opencms.search.extractors;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The result of a document text extraction.<p>
 *
 * This data structure contains the extracted text as well as (optional)
 * meta information extracted from the document.<p>
 *
 * @since 6.0.0
 */
public interface I_CmsExtractionResult {

    /** Key to access the document author name in the item map. */
    String ITEM_AUTHOR = "author";

    /** Key to access the document category in the item map. */
    String ITEM_CATEGORY = "category";

    /** Key to access the document comments in the item map. */
    String ITEM_COMMENTS = "comments";

    /** Key to access the document company name in the item map. */
    String ITEM_COMPANY = "company";

    /** Key for accessing the default (combined) content in {@link #getContentItems()}. */
    String ITEM_CONTENT = "__content";

    /** Key to access the document creator name in the item map. */
    String ITEM_CREATOR = "creator";

    /** Key to access the document keywords in the item map. */
    String ITEM_KEYWORDS = "keywords";

    /** Key to access the document manager name in the item map. */
    String ITEM_MANAGER = "manager";

    /** Key to access the document producer name in the item map. */
    String ITEM_PRODUCER = "producer";

    /** Key for accessing the raw content in {@link #getContentItems()}. */
    String ITEM_RAW = "__raw";

    /** Key to access the document subject in the item map. */
    String ITEM_SUBJECT = "subject";

    /** Key to access the document title in the item map. */
    String ITEM_TITLE = "title";

    /** All items that should be merged. */
    String[] ITEMS_TO_MERGE = {ITEM_CONTENT};

    /**
     * Returns this extraction result serialized as a byte array.<p>
     *
     * @return this extraction result serialized as a byte array
     */
    byte[] getBytes();

    /**
     * Returns the extracted content of the best fitting locale combined as a String.<p>
     *
     * @return the extracted content of the best fitting locale combined as a String
     */
    String getContent();

    /**
     * Returns the extracted content for the given locale combined as a String.<p>
     * @param locale the locale of the extracted content
     *
     * @return the extracted content for the given locale combined as a String
     */
    String getContent(Locale locale);

    /**
     * Returns the extracted content for the best fitting locale as individual items.<p>
     *
     * The result Map contains all content items extracted
     * by the extractor. The key is always a String, and contains the name of the item.
     * The value is also a String and contains the extracted text.<p>
     *
     * The detailed form will depend on the resource type indexed:
     * <ul>
     * <li>For a <code>xmlpage</code>, the key will be the element name, and the value
     * will be the text of the element.
     * <li>For a <code>xmlcontent</code>, the key will be the xpath of the XML node,
     * and the value will be the text of that XML node.
     * <li>In case the document contains meta information (for example PDF or MS Office documents),
     * the meta information is stored with the name of the meta field as key and the content as value.
     * <li>For all other resource types, there will be only ony key {@link #ITEM_CONTENT},
     * which will contain the value of the complete content.
     * </ul>
     *
     * The map has to be ordered to e.g., get the correct indexing order for search field mappings
     * when a sequence of values is mapped to a multi-valued search field.
     *
     * @return the extracted content as individual items
     */
    LinkedHashMap<String, String> getContentItems();

    /** <p>Returns the extracted content for a given locale as individual items.</p>
     * @param locale the locale of the extracted content items
     *
     * @return the extracted content for a given locale as individual items.
     *
     * @see #getContentItems()
     */
    LinkedHashMap<String, String> getContentItems(Locale locale);

    /** Returns the best fitting locale for the content.
     * @return the best fitting locale for the content
     */
    Locale getDefaultLocale();

    /**
     * Returns a map from search fields to values that should be stored in that fields.
     * @return A map from search fields to values that should be stored in that fields.
     */
    Map<String, String> getFieldMappings();

    /** Returns the locales in which the content is available.
     * @return the locales in which the content is available
     */
    Collection<Locale> getLocales();

    /** Appends, for the locales of the current collection result, the content fields
     * from all provided extraction results to the current extraction result.
     *
     * @param extractionResults the extraction results to merge
     * @return the merged result
     */
    I_CmsExtractionResult merge(List<I_CmsExtractionResult> extractionResults);

    /**
     * Releases the information stored in this extraction result, to free up the memory used.<p>
     */
    void release();
}
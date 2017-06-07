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

import org.opencms.util.CmsStringUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
public class CmsExtractionResult implements I_CmsExtractionResult, Serializable {

    /** UID required for safe serialization. */
    private static final long serialVersionUID = 1465447302192195154L;

    /** The extracted individual content items. */
    private Map<Locale, LinkedHashMap<String, String>> m_contentItems;

    /** The locales of the content. */
    private Collection<Locale> m_locales;

    /** The default locale of the content. Can be <code>null</code> for unilingual extraction results. */
    private Locale m_defaultLocale;

    /** The extracted values directly added to the index. */
    private Map<String, String> m_fieldMappings;

    /** The serialized version of this object. */
    private byte[] m_serializedVersion;

    /** Creates a new multilingual extraction result.
     * @param defaultLocale the default (best fitting) locale of the result.
     * @param multilingualContentItems the content items for the different locales
     * @param fieldMappings special mappings to search fields with values extracted from the content
     */
    public CmsExtractionResult(
        Locale defaultLocale,
        Map<Locale, LinkedHashMap<String, String>> multilingualContentItems,
        Map<String, String> fieldMappings) {

        m_defaultLocale = defaultLocale;
        m_contentItems = null != multilingualContentItems
        ? removeNullEntries(multilingualContentItems)
        : new HashMap<Locale, LinkedHashMap<String, String>>(1);

        // set the locales
        m_locales = new HashSet<Locale>();
        for (Locale locale : m_contentItems.keySet()) {
            if (null != locale) {
                m_locales.add(locale);
            }
        }

        // ensure that a version for the default locale is present just to prevent null-checks
        if (null == m_contentItems.get(m_defaultLocale)) {
            m_contentItems.put(m_defaultLocale, new LinkedHashMap<String, String>());
        }
        m_fieldMappings = null != fieldMappings ? fieldMappings : new HashMap<String, String>();

    }

    /**
     * Creates a new extraction result without meta information and without additional fields.<p>
     *
     * @param content the extracted content
     */
    public CmsExtractionResult(String content) {

        this(content, null, null);
        m_contentItems.get(m_defaultLocale).put(ITEM_RAW, content);
    }

    /**
     * Creates a new unilingual extraction result.<p>
     *
     * @param content the extracted content
     * @param contentItems the individual extracted content items
     */
    public CmsExtractionResult(String content, LinkedHashMap<String, String> contentItems) {

        this(content, contentItems, null);
    }

    /**
     * Creates a new unilingual extraction result.<p>
     *
     * @param content the extracted content
     * @param contentItems the individual extracted content items
     * @param fieldMappings extraction results that should directly be indexed
     */
    public CmsExtractionResult(
        String content,
        LinkedHashMap<String, String> contentItems,
        Map<String, String> fieldMappings) {

        m_defaultLocale = null;
        m_locales = new HashSet<Locale>();
        m_contentItems = new LinkedHashMap<Locale, LinkedHashMap<String, String>>(1);
        if (fieldMappings != null) {
            m_fieldMappings = fieldMappings;
        } else {
            m_fieldMappings = new HashMap<String, String>();
        }
        if (contentItems != null) {
            m_contentItems.put(m_defaultLocale, contentItems);
        } else {
            m_contentItems.put(m_defaultLocale, new LinkedHashMap<String, String>());
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(content)) {
            m_contentItems.get(m_defaultLocale).put(ITEM_CONTENT, content);
        }
    }

    /**
     * Creates an extraction result from a serialized byte array.<p>
     *
     * @param bytes the serialized version of the extraction result
     *
     * @return extraction result created from the serialized byte array
     */
    public static final CmsExtractionResult fromBytes(byte[] bytes) {

        Object obj = null;
        if (bytes != null) {
            // create an object out of the byte array
            try {
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                ObjectInputStream oin = new ObjectInputStream(in);
                obj = oin.readObject();
                oin.close();
            } catch (Exception e) {
                // ignore, null is not an instance of CmsExtractionResult
            }
            if (obj instanceof CmsExtractionResult) {
                CmsExtractionResult result = (CmsExtractionResult)obj;
                result.m_serializedVersion = bytes;
                return result;
            }
        }
        return null;
    }

    /**
     * @see org.opencms.search.extractors.I_CmsExtractionResult#getBytes()
     */
    public byte[] getBytes() {

        // check if we have a cached version of the serialized object available
        if (m_serializedVersion != null) {
            return m_serializedVersion;
        }
        try {
            // serialize this object and return
            ByteArrayOutputStream out = new ByteArrayOutputStream(512);
            ObjectOutputStream oout = new ObjectOutputStream(out);
            oout.writeObject(this);
            oout.close();
            m_serializedVersion = out.toByteArray();
        } catch (Exception e) {
            // ignore, serialized version will be null
        }
        return m_serializedVersion;
    }

    /**
     * @see org.opencms.search.extractors.I_CmsExtractionResult#getContent()
     */
    public String getContent() {

        return m_contentItems.get(m_defaultLocale).get(ITEM_CONTENT);
    }

    /**
     * @see org.opencms.search.extractors.I_CmsExtractionResult#getContent(java.util.Locale)
     */
    public String getContent(Locale locale) {

        Map<String, String> localeItems = m_contentItems.get(locale);
        return null == localeItems ? null : localeItems.get(ITEM_CONTENT);
    }

    /**
     * @see org.opencms.search.extractors.I_CmsExtractionResult#getContentItems()
     */
    public LinkedHashMap<String, String> getContentItems() {

        return m_contentItems.get(m_defaultLocale);
    }

    /**
     * @see org.opencms.search.extractors.I_CmsExtractionResult#getContentItems(java.util.Locale)
     */
    public LinkedHashMap<String, String> getContentItems(Locale locale) {

        LinkedHashMap<String, String> localeItems = m_contentItems.get(locale);
        return null == localeItems ? new LinkedHashMap<String, String>() : localeItems;
    }

    /**
     * @see org.opencms.search.extractors.I_CmsExtractionResult#getDefaultLocale()
     */
    public Locale getDefaultLocale() {

        return m_defaultLocale;
    }

    /**
     * @see org.opencms.search.extractors.I_CmsExtractionResult#getFieldMappings()
     */
    public Map<String, String> getFieldMappings() {

        return m_fieldMappings;
    }

    /**
     * @see org.opencms.search.extractors.I_CmsExtractionResult#getLocales()
     */
    public Collection<Locale> getLocales() {

        return m_locales;
    }

    /**
     * @see org.opencms.search.extractors.I_CmsExtractionResult#merge(java.util.List)
     */
    public I_CmsExtractionResult merge(List<I_CmsExtractionResult> extractionResults) {

        //prepare copy
        Map<Locale, LinkedHashMap<String, String>> contentItems = new HashMap<Locale, LinkedHashMap<String, String>>(
            m_locales.size());
        for (Locale locale : m_locales) {
            LinkedHashMap<String, String> originalLocalValues = m_contentItems.get(locale);
            LinkedHashMap<String, String> localeValues = new LinkedHashMap<String, String>(originalLocalValues);
            contentItems.put(locale, localeValues);
        }

        HashMap<String, String> fieldMappings = new HashMap<String, String>(m_fieldMappings.size());
        for (String fieldMapping : m_fieldMappings.keySet()) {
            fieldMappings.put(fieldMapping, m_fieldMappings.get(fieldMapping));
        }

        //merge content from the other extraction results
        for (Locale locale : m_locales) {
            Map<String, String> localeValues = contentItems.get(locale);
            for (I_CmsExtractionResult result : extractionResults) {
                if (result.getLocales().contains(locale)) {
                    Map<String, String> resultLocaleValues = result.getContentItems(locale);
                    for (String item : Arrays.asList(ITEMS_TO_MERGE)) {
                        localeValues = mergeItem(item, localeValues, resultLocaleValues);
                    }
                }
            }
        }
        return new CmsExtractionResult(m_defaultLocale, contentItems, fieldMappings);
    }

    /**
     * @see org.opencms.search.extractors.I_CmsExtractionResult#release()
     */
    public void release() {

        if (!m_contentItems.isEmpty()) {
            m_contentItems.clear();
        }
        m_contentItems = null;
        m_serializedVersion = null;
    }

    /** Merges the item from the resultLocaleValues into the corresponding item of the localeValues.
     * @param item the item to merge
     * @param localeValues the values where the item gets merged into
     * @param resultLocaleValues the values where the item to merge is read from
     * @return the modified localeValues with the merged item
     */
    private Map<String, String> mergeItem(
        String item,
        Map<String, String> localeValues,
        Map<String, String> resultLocaleValues) {

        if (resultLocaleValues.get(item) != null) {
            if (localeValues.get(item) != null) {
                localeValues.put(item, localeValues.get(item) + " " + resultLocaleValues.get(item));
            } else {
                localeValues.put(item, resultLocaleValues.get(item));
            }
        }

        return localeValues;
    }

    /** Replaces all <code>null</code> values with empty maps.
     * @param multilingualContentItems the map where replacement should take place
     * @return the map with all <code>null</code> values replaced with empty maps.
     */
    private Map<Locale, LinkedHashMap<String, String>> removeNullEntries(
        Map<Locale, LinkedHashMap<String, String>> multilingualContentItems) {

        for (Locale locale : multilingualContentItems.keySet()) {
            if (null == multilingualContentItems.get(locale)) {
                multilingualContentItems.put(locale, new LinkedHashMap<String, String>());
            }
        }
        return multilingualContentItems;
    }
}
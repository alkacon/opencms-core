/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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

package org.opencms.xml.content;

import org.opencms.file.CmsObject;
import org.opencms.relations.CmsLink;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.ComparisonChain;

/**
 * Represents the concrete location of an XML content value.<p>
 *
 * @since 8.0.0
 */
public class CmsXmlContentValueLocation implements I_CmsXmlContentValueLocation {

    /** A value and its precomputed sort keys. */
    private static class SortableValue {

        /** The numeric index of the last path element. */
        private final int m_index;

        /** The location to return. */
        private final CmsXmlContentValueLocation m_location;

        /** The path without the final index. */
        private final String m_path;

        /**
         * Creates a sortable value.<p>
         *
         * @param location the location
         * @param path the full indexed path
         */
        SortableValue(CmsXmlContentValueLocation location, String path) {

            m_location = location;
            m_path = CmsXmlUtils.removeXpathIndex(path);
            m_index = CmsXmlUtils.getXpathIndexInt(path);
        }
    }

    /** Whether paths may be cached while reading an unchanged document. */
    private final boolean m_cachePaths;

    /** The lazily cached path. */
    private String m_path;

    /** The XML content value. */
    private I_CmsXmlContentValue m_value;

    /**
     * Constructs a new XML content value location.<p>
     *
     * @param value the XML content value
     */
    public CmsXmlContentValueLocation(I_CmsXmlContentValue value) {

        this(value, false);
    }

    /**
     * Constructs a location with optional path caching.<p>
     *
     * If caching is enabled, the document structure must remain unchanged while this
     * location or any locations obtained from it are in use.<p>
     *
     * @param value the XML content value
     * @param cachePaths whether to cache paths for read-only access
     */
    public CmsXmlContentValueLocation(I_CmsXmlContentValue value, boolean cachePaths) {

        if (value == null) {
            throw new UnsupportedOperationException("Can't create content value location with a null value.");
        }
        m_value = value;
        m_cachePaths = cachePaths;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentValueLocation#asId(org.opencms.file.CmsObject)
     */
    public CmsUUID asId(CmsObject cms) {

        CmsLink link = ((CmsXmlVfsFileValue)m_value).getLink(cms);
        if (link == null) {
            return null;
        }
        return link.getStructureId();
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentValueLocation#asString(org.opencms.file.CmsObject)
     */
    public String asString(CmsObject cms) {

        return m_value.getStringValue(cms);
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentLocation#getDocument()
     */
    public I_CmsXmlDocument getDocument() {

        return m_value.getDocument();
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentLocation#getLocale()
     */
    public Locale getLocale() {

        return m_value.getLocale();
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentLocation#getSubValue(java.lang.String)
     */
    public CmsXmlContentValueLocation getSubValue(String subPath) {

        Locale locale = m_value.getLocale();

        I_CmsXmlContentValue subValue = m_value.getDocument().getValue(
            CmsXmlUtils.concatXpath(getPath(), subPath),
            locale);
        if (subValue != null) {
            return new CmsXmlContentValueLocation(subValue, m_cachePaths);
        }
        return null;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentLocation#getSubValues(java.lang.String)
     */
    public List<I_CmsXmlContentValueLocation> getSubValues(String subPath) {

        List<I_CmsXmlContentValueLocation> result = new ArrayList<I_CmsXmlContentValueLocation>();
        String requiredLastElement = CmsXmlUtils.getLastXpathElement(subPath);
        Locale locale = m_value.getLocale();
        List<I_CmsXmlContentValue> subValues = m_value.getDocument().getValues(
            CmsXmlUtils.concatXpath(getPath(), subPath),
            locale);
        List<SortableValue> sortedValues = new ArrayList<>();
        for (I_CmsXmlContentValue subValue : subValues) {
            if (subValue != null) {
                String path = subValue.getPath();
                // Repeating choices return values with other names too. Use the already computed
                // path to preserve the existing filtering semantics, including qualified names.
                if (CmsXmlUtils.getLastXpathElement(path).equals(requiredLastElement)) {
                    CmsXmlContentValueLocation location = new CmsXmlContentValueLocation(subValue, m_cachePaths);
                    if (m_cachePaths) {
                        location.m_path = path;
                    }
                    sortedValues.add(new SortableValue(location, path));
                }
            }
        }

        Collections.sort(sortedValues, new Comparator<SortableValue>() {

            public int compare(SortableValue firstValue, SortableValue secondValue) {

                return ComparisonChain.start().compare(firstValue.m_path, secondValue.m_path).compare(
                    firstValue.m_index,
                    secondValue.m_index).result();
            }
        });

        for (SortableValue value : sortedValues) {
            result.add(value.m_location);
        }
        return result;
    }

    /**
     * Returns the content value at the given location.<p>
     *
     * @return the content value at the given location
     */
    public I_CmsXmlContentValue getValue() {

        return m_value;
    }

    /**
     * Gets the path, reusing it when read-only access was requested.<p>
     *
     * @return the indexed path
     */
    private String getPath() {

        if (!m_cachePaths) {
            return m_value.getPath();
        }
        if (m_path == null) {
            m_path = m_value.getPath();
        }
        return m_path;
    }

}

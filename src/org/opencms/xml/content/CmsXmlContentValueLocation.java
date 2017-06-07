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
import com.google.common.collect.Lists;

/**
 * Represents the concrete location of an XML content value.<p>
 *
 * @since 8.0.0
 */
public class CmsXmlContentValueLocation implements I_CmsXmlContentValueLocation {

    /** The XML content value. */
    private I_CmsXmlContentValue m_value;

    /**
     * Constructs a new XML content value location.<p>
     *
     * @param value the XML content value
     */
    public CmsXmlContentValueLocation(I_CmsXmlContentValue value) {

        if (value == null) {
            throw new UnsupportedOperationException("Can't create content value location with a null value.");
        }
        m_value = value;
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
            CmsXmlUtils.concatXpath(m_value.getPath(), subPath),
            locale);
        if (subValue != null) {
            return new CmsXmlContentValueLocation(subValue);
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
        List<I_CmsXmlContentValue> subValues = Lists.newArrayList(
            m_value.getDocument().getValues(CmsXmlUtils.concatXpath(m_value.getPath(), subPath), locale));

        Collections.sort(subValues, new Comparator<I_CmsXmlContentValue>() {

            public int compare(I_CmsXmlContentValue firstValue, I_CmsXmlContentValue secondValue) {

                String firstPath = CmsXmlUtils.removeXpathIndex(firstValue.getPath());
                String secondPath = CmsXmlUtils.removeXpathIndex(secondValue.getPath());
                int firstIndex = CmsXmlUtils.getXpathIndexInt(firstValue.getPath());
                int secondIndex = CmsXmlUtils.getXpathIndexInt(secondValue.getPath());
                int comparisonResult = ComparisonChain.start().compare(firstPath, secondPath).compare(
                    firstIndex,
                    secondIndex).result();
                return comparisonResult;
            }
        });

        for (I_CmsXmlContentValue subValue : subValues) {
            if (subValue != null) {
                // if subPath is the path of one option of a choice element, getValues() will, strangely,
                // return all values of the choice, regardless of their name, so we need to check
                // the name by hand
                String lastElement = CmsXmlUtils.getLastXpathElement(subValue.getPath());
                if (lastElement.equals(requiredLastElement)) {
                    result.add(new CmsXmlContentValueLocation(subValue));
                }
            }
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

}

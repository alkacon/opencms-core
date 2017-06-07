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

import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Represents the root of an XML content for a given locale.<p>
 *
 * @since 8.0.0
 */
public class CmsXmlContentRootLocation implements I_CmsXmlContentLocation {

    /** The XML document. */
    private I_CmsXmlDocument m_document;

    /** The locale. */
    private Locale m_locale;

    /**
     * Creates a new root location for a given locale.<p>
     *
     * @param doc the XML document
     * @param locale the locale
     */
    public CmsXmlContentRootLocation(I_CmsXmlDocument doc, Locale locale) {

        m_document = doc;
        m_locale = locale;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentLocation#getDocument()
     */
    public I_CmsXmlDocument getDocument() {

        return m_document;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentLocation#getLocale()
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentLocation#getSubValue(java.lang.String)
     */
    public I_CmsXmlContentValueLocation getSubValue(String subPath) {

        I_CmsXmlContentValue value = m_document.getValue(subPath, m_locale);
        if (value == null) {
            return null;
        }
        return new CmsXmlContentValueLocation(value);
    }

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentLocation#getSubValues(java.lang.String)
     */
    public List<I_CmsXmlContentValueLocation> getSubValues(String subPath) {

        List<I_CmsXmlContentValue> values = m_document.getValues(subPath, m_locale);
        List<I_CmsXmlContentValueLocation> result = new ArrayList<I_CmsXmlContentValueLocation>();
        for (I_CmsXmlContentValue value : values) {
            if (value != null) {
                result.add(new CmsXmlContentValueLocation(value));
            }
        }
        return result;
    }
}

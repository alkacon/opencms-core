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

package org.opencms.xml.content;

import org.opencms.file.CmsObject;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.List;
import java.util.Locale;

/**
 * Describes the sequence of XML content values of a specific type in an XML content instance.<p>
 *
 * @since 6.0.0
 */
public class CmsXmlContentValueSequence {

    /** The XML content this sequence element is based on. */
    private CmsXmlContent m_content;

    /** Indicates if this is a choice sequence or not. */
    private boolean m_isChoiceSequence;

    /** The locale this sequence is based on. */
    private Locale m_locale;

    /** The maximum occurrences of the elements in this sequence. */
    private int m_maxOccurs;

    /** The minimum occurrences of the elements in this sequence. */
    private int m_minOccurs;

    /** The Xpath this content value sequence was generated for. */
    private String m_path;

    /** The list of XML content values for the selected schema type and locale in the XML content. */
    private List<I_CmsXmlContentValue> m_values;

    /**
     * Generates a new content sequence element from the given type, content and content definition.<p>
     *
     * @param path the path in the document to generate the value sequence for
     * @param locale the locale to get the content values from
     * @param content the XML content to generate the sequence element out of
     */
    public CmsXmlContentValueSequence(String path, Locale locale, CmsXmlContent content) {

        m_locale = locale;
        m_content = content;
        m_path = CmsXmlUtils.removeXpathIndex(path);
        I_CmsXmlSchemaType type = m_content.getContentDefinition().getSchemaType(m_path);
        m_isChoiceSequence = type.isChoiceOption();
        if (m_isChoiceSequence) {
            m_values = m_content.getSubValues(CmsXmlUtils.removeLastXpathElement(m_path), m_locale);
        } else {
            m_values = m_content.getValues(path, m_locale);
        }
        if (type.getContentDefinition().getChoiceMaxOccurs() > 1) {
            m_minOccurs = 0;
            m_maxOccurs = type.getContentDefinition().getChoiceMaxOccurs();
        } else {
            if (m_isChoiceSequence && !m_values.isEmpty()) {
                type = m_values.get(0);
            }
            m_minOccurs = type.getMinOccurs();
            m_maxOccurs = type.getMaxOccurs();
        }
    }

    /**
     * Adds a value element of the given type
     * at the selected index to the XML content document.<p>
     *
     * @param cms the current users OpenCms context
     * @param type the type to add
     * @param index the index where to add the new value element
     *
     * @return the added XML content value element
     *
     * @see CmsXmlContent#addValue(CmsObject, String, Locale, int)
     * @see #addValue(CmsObject, String, int)
     * @see #addValue(CmsObject, int)
     */
    public I_CmsXmlContentValue addValue(CmsObject cms, I_CmsXmlSchemaType type, int index) {

        String xpath = CmsXmlUtils.concatXpath(CmsXmlUtils.removeLastXpathElement(getPath()), type.getName());
        return addValue(cms, xpath, index);
    }

    /**
     * Adds a value element of the type the original xpath indicates
     * at the selected index to the XML content document.<p>
     *
     * The "original xpath" is the path used in the constructor when creating
     * this value sequence.<p>
     *
     * @param cms the current users OpenCms context
     * @param index the index where to add the new value element
     *
     * @return the added XML content value element
     *
     * @see CmsXmlContent#addValue(CmsObject, String, Locale, int)
     * @see #addValue(CmsObject, String, int)
     * @see #addValue(CmsObject, I_CmsXmlSchemaType, int)
     */
    public I_CmsXmlContentValue addValue(CmsObject cms, int index) {

        return addValue(cms, getPath(), index);
    }

    /**
     * Adds a value element of the type indicated by the given xpath
     * at the selected index to the XML content document.<p>
     *
     * @param cms the current users OpenCms context
     * @param xpath the path that indicates the element type in the content definition
     * @param index the index where to add the new value element
     *
     * @return the added XML content value element
     *
     * @see CmsXmlContent#addValue(CmsObject, String, Locale, int)
     * @see #addValue(CmsObject, I_CmsXmlSchemaType, int)
     * @see #addValue(CmsObject, int)
     */
    public I_CmsXmlContentValue addValue(CmsObject cms, String xpath, int index) {

        I_CmsXmlContentValue newValue = m_content.addValue(cms, xpath, getLocale(), index);

        // re-initialize the value list
        m_values = m_content.getValues(getPath(), getLocale());

        return newValue;
    }

    /**
     * Returns the count of XML content values for the selected schema type and locale in the XML content.<p>
     *
     * @return the count of XML content values for the selected schema type and locale in the XML content
     */
    public int getElementCount() {

        return m_values.size();
    }

    /**
     * Returns the locale this sequence is based on.<p>
     *
     * @return the locale this sequence is based on
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the maximum occurrences of this type in the XML content schema.<p>
     *
     * @return the maximum occurrences of this type in the XML content schema
     *
     * @see I_CmsXmlSchemaType#getMaxOccurs()
     */
    public int getMaxOccurs() {

        return m_maxOccurs;
    }

    /**
     * Returns the minimum occurrences of this type in the XML content schema.<p>
     *
     * @return the minimum occurrences of this type in the XML content schema
     *
     * @see I_CmsXmlSchemaType#getMinOccurs()
     */
    public int getMinOccurs() {

        return m_minOccurs;
    }

    /**
     * Returns the (simplified) Xpath expression that identifies the root node
     * of this content value sequence.<p>
     *
     * @return the (simplified) Xpath expression that identifies the root node
     *      of this content value sequence
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Returns the XML content values from the index position of this sequence.<p>
     *
     * @param index the index position to get the value from
     *
     * @return the XML content values from the index position of this sequence
     */
    public I_CmsXmlContentValue getValue(int index) {

        return m_values.get(index);
    }

    /**
     * Returns the list of XML content values for the selected schema type and locale in the XML content.<p>
     *
     * @return the list of XML content values for the selected schema type and locale in the XML content
     *
     * @see #getValue(int)
     */
    public List<I_CmsXmlContentValue> getValues() {

        return m_values;
    }

    /**
     * Returns <code>true</code> if this sequence represents a choice sequence.<p>
     *
     * @return <code>true</code> if this sequence represents a choice sequence
     */
    public boolean isChoiceSequence() {

        return m_isChoiceSequence;
    }

    /**
     * Removes the value element of the sequence type at the selected index from XML content document.<p>
     *
     * @param index the index where to remove the value element
     *
     * @see CmsXmlContent#removeValue(String, Locale, int)
     */
    public void removeValue(int index) {

        m_content.removeValue(getPath(), getLocale(), index);

        // re-initialize the value list
        m_values = m_content.getValues(getPath(), getLocale());
    }
}
/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/CmsXmlContentValueSequence.java,v $
 * Date   : $Date: 2005/02/17 12:45:12 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.List;
import java.util.Locale;

/**
 * Describes the sequence of XML content values of a specific type in an XML content instance.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.7 $
 * @since 5.5.0
 */
public class CmsXmlContentValueSequence {

    /** The XML content this sequence element is based on. */
    private CmsXmlContent m_content;

    /** The locale this sequence is based on. */
    private Locale m_locale;

    /** The Xpath this content value seqnence was generated for. */
    private String m_path;

    /** The XML schema type this sequence is based on. */
    private I_CmsXmlSchemaType m_schemaType;

    /** The list of XML content values for the selected schema type and locale in the XML content. */
    private List m_values;

    /**
     * Generates a new content sequence element from the given type, content and content defintion.<p>
     * 
     * @param path the path in the document to generate the value sequence for
     * @param schemaType the schema type to generate the seqnence element for
     * @param locale the locale to get the content values from
     * @param content the XML content to generate the sequence element out of
     */
    public CmsXmlContentValueSequence(String path, I_CmsXmlSchemaType schemaType, Locale locale, CmsXmlContent content) {

        m_schemaType = schemaType;
        m_locale = locale;
        m_content = content;
        m_values = m_content.getValues(path, m_locale);
        m_path = CmsXmlUtils.removeXpathIndex(path);
    }

    /**
     * Adds a value element of the sequence type at the seleted index to the XML content document.<p> 
     * 
     * @param cms the current users OpenCms context
     * @param index the index where to add the new value element
     * 
     * @return the added XML content value element
     * 
     * @see CmsXmlContent#addValue(CmsObject, String, Locale, int)
     */
    public I_CmsXmlContentValue addValue(CmsObject cms, int index) {

        I_CmsXmlContentValue newValue = m_content.addValue(cms, getPath(), getLocale(), index);

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
     * Returns the XML element node name of this sequence element in the current schema.<p>
     *
     * The XML element node name can be configured in the schema.
     * For example, the node name could be <code>"Title"</code>,
     * <code>"Teaser"</code> or <code>"Text"</code>. The XML schema controls 
     * what node names are allowed.<p> 
     *
     * @return the XML node name of this sequence element in the current schema
     * 
     * @see I_CmsXmlSchemaType#getElementName()
     */
    public String getElementName() {

        return m_schemaType.getElementName();
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
     * Returns the maximum occurences of this type in the XML content schema.<p>
     *
     * @return the maximum occurences of this type in the XML content schema
     * 
     * @see I_CmsXmlSchemaType#getMaxOccurs()
     */
    public int getMaxOccurs() {

        return m_schemaType.getMaxOccurs();
    }

    /**
     * Returns the minimum occurences of this type in the XML content schema.<p>
     *
     * @return the minimum occurences of this type in the XML content schema
     * 
     * @see I_CmsXmlSchemaType#getMinOccurs()
     */
    public int getMinOccurs() {

        return m_schemaType.getMinOccurs();
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
     * Returns the XML content values from the index position of this seqnence.<p>
     * 
     * @param index the index position to get the value from
     * 
     * @return the XML content values from the index position of this seqnence
     */
    public I_CmsXmlContentValue getValue(int index) {

        return (I_CmsXmlContentValue)m_values.get(index);
    }

    /**
     * Returns the list of XML content values for the selected schema type and locale in the XML content.<p>
     * 
     * @return the list of XML content values for the selected schema type and locale in the XML content
     * 
     * @see CmsXmlContentValueSequence#getValue(int)
     */
    public List getValues() {

        return m_values;
    }

    /**
     * Return the XML schema type of this sequence element.<p>
     * 
     * @return the XML schema type of this sequence element
     */
    public I_CmsXmlSchemaType getXmlSchemaType() {

        return m_schemaType;
    }

    /**
     * Returns <code>true</code> if more elements of this type can be added to the XML content.<p>
     * 
     * @return <code>true</code> if more elements of this type can be added to the XML content
     */
    public boolean isExtendable() {

        return getElementCount() < getMaxOccurs();
    }

    /**
     * Returns <code>true</code> if elements of this type can be removed from the XML content.<p>
     * 
     * @return <code>true</code> if elements of this type can be removed from the XML content
     */
    public boolean isReducable() {

        return getElementCount() > getMinOccurs();
    }

    /**
     * Returns <code>true</code> if this is a simple type, or <code>false</code>
     * if this type is a nested schema.<p>
     * 
     * If a value is a nested schema, it must be an instance of {@link org.opencms.xml.types.CmsXmlNestedContentDefinition}.<p> 
     * 
     * @return true if this is  a simple type, or false if this type is a nested schema
     * 
     * @see org.opencms.xml.types.CmsXmlNestedContentDefinition
     */
    public boolean isSimpleType() {

        return m_schemaType.isSimpleType();
    }

    /**
     * Removes the value element of the sequence type at the seleted index from XML content document.<p> 
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
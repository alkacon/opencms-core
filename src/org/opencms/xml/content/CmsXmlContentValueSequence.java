/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/CmsXmlContentValueSequence.java,v $
 * Date   : $Date: 2004/11/29 01:38:15 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.List;
import java.util.Locale;

/**
 * Describes the sequence of XML content values of a specific type in an XML content instance.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.2 $
 * @since 5.5.0
 */
public class CmsXmlContentValueSequence {

    /** The XML content this sequence element is based on. */
    private CmsXmlContent m_content;

    /** The locale this sequence is based on. */
    private Locale m_locale;

    /** The XML schema type this sequence is based on. */
    private I_CmsXmlSchemaType m_schemaType;

    /** The list of XML content values for the selected schema type and locale in the XML content. */
    private List m_values;

    /**
     * Generates a new content sequence element from the given type, content and content defintion.<p>
     * 
     * @param schemaType the schema type to generate the seqnence element for
     * @param locale the locale to get the content values from
     * @param content the XML content to generate the sequence element out of
     */
    public CmsXmlContentValueSequence(I_CmsXmlSchemaType schemaType, Locale locale, CmsXmlContent content) {

        m_schemaType = schemaType;
        m_locale = locale;
        m_content = content;
        m_values = m_content.getValues(getElementName(), m_locale);
    }

    /**
     * Adds a value element of the sequence type at the seleted index to the XML content document.<p> 
     * 
     * @param index the index where to add the new value element
     * 
     * @return the added XML content value element
     * 
     * @see CmsXmlContent#addValue(String, Locale, int)
     */
    public I_CmsXmlContentValue addValue(int index) {

        I_CmsXmlContentValue newValue = m_content.addValue(m_schemaType.getElementName(), getLocale(), index);

        // re-initialize the value list
        m_values = m_content.getValues(getElementName(), getLocale());

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
     * Returns the list of XML content values for the selected schema type and locale in the XML content.<p>
     * 
     * @return the list of XML content values for the selected schema type and locale in the XML content
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
     * Returns <code>true</code> if this sequence element is of a simple type, or <code>false</code>
     * if it is a cascaded schema type.<p>
     * 
     * @return true if this sequence element is of a simple type, or false if it is a cascaded schema
     * 
     * @see I_CmsXmlSchemaType#isSimpleType() 
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

        m_content.removeValue(getElementName(), getLocale(), index);
        
        // re-initialize the value list
        m_values = m_content.getValues(getElementName(), getLocale());
    }
}
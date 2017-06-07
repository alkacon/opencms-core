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

package org.opencms.xml;

import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.List;

/**
 * Simple data structure to describe a type sequence in a XML schema.<p>
 *
 * @since 6.0.0
 */
class CmsXmlComplexTypeSequence {

    /** Max occurs value for xsd:choice definitions. */
    private int m_choiceMaxOccurs;

    /** Indicates if this type sequence has a language attribute. */
    private boolean m_hasLanguageAttribute;

    /** The name of the complex type sequence. */
    private String m_name;

    /** The type sequence elements. */
    private List<I_CmsXmlSchemaType> m_sequence;

    /** Indicates the type of the sequence. */
    private CmsXmlContentDefinition.SequenceType m_sequenceType;

    /**
     * Creates a new complex type sequence data structure.<p>
     *
     * @param name the name of the sequence
     * @param sequence the type sequence element list
     * @param hasLanguageAttribute indicates if a "language" attribute is present
     * @param sequenceType indicates the type of the sequence
     * @param choiceMaxOccurs indicated the maxOccurs value for a xsd:choice sequence
     */
    protected CmsXmlComplexTypeSequence(
        String name,
        List<I_CmsXmlSchemaType> sequence,
        boolean hasLanguageAttribute,
        CmsXmlContentDefinition.SequenceType sequenceType,
        int choiceMaxOccurs) {

        m_name = name;
        m_sequence = sequence;
        m_hasLanguageAttribute = hasLanguageAttribute;
        m_sequenceType = sequenceType;
        m_choiceMaxOccurs = choiceMaxOccurs;
    }

    /**
     * Returns the maxOccurs value for <code>xsd:choice</code> definitions.<p>
     *
     * @return the maxOccurs value for <code>xsd:choice</code> definitions
     */
    public int getChoiceMaxOccurs() {

        return m_choiceMaxOccurs;
    }

    /**
     * Returns the name of the sequence.<p>
     *
     * @return the name of the sequence
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the type sequence element list.<p>
     *
     * @return the type sequence element list
     */
    public List<I_CmsXmlSchemaType> getSequence() {

        return m_sequence;
    }

    /**
     * Returns the type of this sequence.<p>
     *
     * @return the type of this sequence
     */
    public CmsXmlContentDefinition.SequenceType getSequenceType() {

        return m_sequenceType;
    }

    /**
     * Returns <code>true</code> if a "language" attribute is present in this sequence.<p>
     *
     * @return <code>true</code> if a "language" attribute is present in this sequence
     */
    public boolean hasLanguageAttribute() {

        return m_hasLanguageAttribute;
    }

}
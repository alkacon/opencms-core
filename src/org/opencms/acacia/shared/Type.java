/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.acacia.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serializable {@link org.opencms.acacia.shared.I_Type} implementation.<p>
 */
public class Type implements I_Type, Serializable {

    /** The choice attribute name. */
    public static final String CHOICE_ATTRIBUTE_NAME = "ATTRIBUTE_CHOICE";

    /** The serial version id. */
    private static final long serialVersionUID = -7965094404314721990L;

    /** Flag indicating if this is a choice type. */
    private int m_choiceMaxOccurrence;

    /** The type id. */
    private String m_id;

    /** The max occurrences of the type attributes. */
    private Map<String, Integer> m_maxs;

    /** The min occurrences of the type attributes. */
    private Map<String, Integer> m_mins;

    /** The attribute names. */
    private List<String> m_names;

    /** The attribute types. */
    private Map<String, String> m_types;

    /**
     * Constructor.<p>
     * 
     * @param id the type id/name
     */
    public Type(String id) {

        this();
        m_id = id;
    }

    /**
     * Constructor. For serialization only.<p>
     */
    protected Type() {

        m_names = new ArrayList<String>();
        m_types = new HashMap<String, String>();
        m_maxs = new HashMap<String, Integer>();
        m_mins = new HashMap<String, Integer>();
    }

    /**
     * @see org.opencms.acacia.shared.I_Type#addAttribute(java.lang.String, java.lang.String, int, int)
     */
    public void addAttribute(String attributeName, String attributeType, int minOccurrence, int maxOccurrence) {

        m_names.add(attributeName);
        m_types.put(attributeName, attributeType);
        m_mins.put(attributeName, new Integer(minOccurrence));
        m_maxs.put(attributeName, new Integer(maxOccurrence));
    }

    /**
     * @see org.opencms.acacia.shared.I_Type#getAttributeMaxOccurrence(java.lang.String)
     */
    public int getAttributeMaxOccurrence(String attributeName) {

        return m_maxs.get(attributeName).intValue();
    }

    /**
     * @see org.opencms.acacia.shared.I_Type#getAttributeMinOccurrence(java.lang.String)
     */
    public int getAttributeMinOccurrence(String attributeName) {

        return m_mins.get(attributeName).intValue();
    }

    /**
     * @see org.opencms.acacia.shared.I_Type#getAttributeNames()
     */
    public List<String> getAttributeNames() {

        return Collections.unmodifiableList(m_names);
    }

    /**
     * @see org.opencms.acacia.shared.I_Type#getAttributeType(java.lang.String)
     */
    public I_Type getAttributeType(String attributeName) {

        throw new UnsupportedOperationException();
    }

    /**
     * @see org.opencms.acacia.shared.I_Type#getAttributeTypeName(java.lang.String)
     */
    public String getAttributeTypeName(String attributeName) {

        return m_types.get(attributeName);
    }

    /**
     * @see org.opencms.acacia.shared.I_Type#getChoiceMaxOccurrence()
     */
    public int getChoiceMaxOccurrence() {

        return m_choiceMaxOccurrence;
    }

    /**
     * @see org.opencms.acacia.shared.I_Type#getId()
     */
    public String getId() {

        return m_id;
    }

    /**
     * @see org.opencms.acacia.shared.I_Type#isChoice()
     */
    public boolean isChoice() {

        return m_choiceMaxOccurrence > 0;
    }

    /**
     * @see org.opencms.acacia.shared.I_Type#isSimpleType()
     */
    public boolean isSimpleType() {

        return m_names.isEmpty();
    }

    /**
     * @see org.opencms.acacia.shared.I_Type#setChoiceMaxOccurrence(int)
     */
    public void setChoiceMaxOccurrence(int choiceMaxOccurrence) {

        m_choiceMaxOccurrence = choiceMaxOccurrence;
    }
}

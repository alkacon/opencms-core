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
 * CmsEntity attribute type data.<p>
 */
public class CmsType implements Serializable {

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
    private Map<String, CmsType> m_types;

    /**
     * Constructor.<p>
     *
     * @param id the type id/name
     */
    public CmsType(String id) {

        this();
        m_id = id;
    }

    /**
     * Constructor. For serialization only.<p>
     */
    protected CmsType() {

        m_names = new ArrayList<String>();
        m_types = new HashMap<String, CmsType>();
        m_maxs = new HashMap<String, Integer>();
        m_mins = new HashMap<String, Integer>();
    }

    /**
     * Adds an attribute to the type.<p>
     *
     * @param attributeName the attribute name
     * @param attributeType the attribute type
     * @param minOccurrence the minimum occurrence of this attribute
     * @param maxOccurrence the axnimum occurrence of this attribute
     */
    public void addAttribute(String attributeName, CmsType attributeType, int minOccurrence, int maxOccurrence) {

        m_names.add(attributeName);
        m_types.put(attributeName, attributeType);
        m_mins.put(attributeName, Integer.valueOf(minOccurrence));
        m_maxs.put(attributeName, Integer.valueOf(maxOccurrence));
    }

    /**
     * Returns the maximum occurrence of the given attribute.<p>
     *
     * @param attributeName the attribute name
     *
     * @return the maximum occurrence
     */
    public int getAttributeMaxOccurrence(String attributeName) {

        return m_maxs.get(attributeName).intValue();
    }

    /**
     * Returns the minimum occurrence of the given attribute.<p>
     *
     * @param attributeName the attribute name
     *
     * @return the minimum occurrence
     */
    public int getAttributeMinOccurrence(String attributeName) {

        return m_mins.get(attributeName).intValue();
    }

    /**
     * The names of the attributes of this type.<p>
     *
     * @return the attribute names
     */
    public List<String> getAttributeNames() {

        return Collections.unmodifiableList(m_names);
    }

    /**
     * Returns the type of the given attribute.<p>
     *
     * @param attributeName the attribute name
     *
     * @return the type of the given attribute
     */
    public CmsType getAttributeType(String attributeName) {

        return m_types.get(attributeName);
    }

    /**
     * Returns the type name of the given attribute.<p>
     *
     * @param attributeName the attribute name
     *
     * @return the type name of the given attribute
     */
    public String getAttributeTypeName(String attributeName) {

        CmsType attrType = m_types.get(attributeName);
        return attrType == null ? null : attrType.getId();
    }

    /**
     * Returns the maximum choice occurrence.<p>
     *
     * @return the maximum choice occurrence
     */
    public int getChoiceMaxOccurrence() {

        return m_choiceMaxOccurrence;
    }

    /**
     * Returns the name of the type.<p>
     *
     * @return the name of the type
     */
    public String getId() {

        return m_id;
    }

    /**
     * Returns if this is a choice type.<p>
     *
     * @return <code>true</code> if this is a choice type
     */
    public boolean isChoice() {

        return m_choiceMaxOccurrence > 0;
    }

    /**
     * Returns if this is a simple type. Simple types have no attributes.<p>
     *
     * @return <code>true</code> if this is a simple type
     */
    public boolean isSimpleType() {

        return m_names.isEmpty();
    }

    /**
     * Sets the maximum choice occurrence.<p>
     *
     * @param choiceMaxOccurrence the maximum choice occurrence
     */
    public void setChoiceMaxOccurrence(int choiceMaxOccurrence) {

        m_choiceMaxOccurrence = choiceMaxOccurrence;
    }
}

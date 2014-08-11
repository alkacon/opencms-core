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
import java.util.List;

/**
 * Serializable entity attribute implementation.<p>
 */
public class EntityAttribute implements Serializable {

    /** Serial version id. */
    private static final long serialVersionUID = 8283921354261037725L;

    /** The complex type values. */
    private List<Entity> m_entityValues;

    /** The attribute name. */
    private String m_name;

    /** The simple type values. */
    private List<String> m_simpleValues;

    /**
     * Constructor. For serialization only.<p>
     */
    protected EntityAttribute() {

    }

    /**
     * Creates a entity type attribute.<p>
     * 
     * @param name the attribute name
     * @param values the attribute values
     * 
     * @return the newly created attribute
     */
    public static EntityAttribute createEntityAttribute(String name, List<Entity> values) {

        EntityAttribute result = new EntityAttribute();
        result.m_name = name;
        result.m_entityValues = Collections.unmodifiableList(values);
        return result;
    }

    /**
     * Creates a simple type attribute.<p>
     * 
     * @param name the attribute name
     * @param values the attribute values
     * 
     * @return the newly created attribute
     */
    public static EntityAttribute createSimpleAttribute(String name, List<String> values) {

        EntityAttribute result = new EntityAttribute();
        result.m_name = name;
        result.m_simpleValues = Collections.unmodifiableList(values);
        return result;
    }

    /**
     * Returns the attribute name.<p>
     * 
     * @return the attribute name
     */
    public String getAttributeName() {

        return m_name;
    }

    /**
     * Returns the first complex value in the list.<p>
     * 
     * @return the first complex value
     */
    public Entity getComplexValue() {

        return m_entityValues.get(0);
    }

    /**
     * Returns the list of complex values.<p>
     * 
     * @return the list of complex values
     */
    public List<Entity> getComplexValues() {

        List<Entity> result = new ArrayList<Entity>();
        result.addAll(m_entityValues);
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the first simple value in the list.<p>
     * 
     * @return the first simple value
     */
    public String getSimpleValue() {

        return m_simpleValues.get(0);
    }

    /**
     * Returns the list of simple values.<p>
     * 
     * @return the list of simple values
     */
    public List<String> getSimpleValues() {

        return Collections.unmodifiableList(m_simpleValues);
    }

    /**
     * Returns the number of values set for this attribute.<p>
     * 
     * @return the number of values
     */
    public int getValueCount() {

        if (isComplexValue()) {
            return m_entityValues.size();
        }
        return m_simpleValues.size();
    }

    /**
     * Returns if the is a complex type value.<p>
     * 
     * @return <code>true</code> if this is a complex type value
     */
    public boolean isComplexValue() {

        return m_entityValues != null;
    }

    /**
     * Returns if the is a simple type value.<p>
     * 
     * @return <code>true</code> if this is a simple type value
     */
    public boolean isSimpleValue() {

        return m_simpleValues != null;
    }

    /**
     * Returns if this is a single value attribute.<p>
     * 
     * @return <code>true</code> if this is a single value attribute
     */
    public boolean isSingleValue() {

        if (isComplexValue()) {
            return m_entityValues.size() == 1;
        }
        return m_simpleValues.size() == 1;
    }
}

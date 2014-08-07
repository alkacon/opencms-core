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

package org.opencms.acacia.client.entity;

import org.opencms.acacia.shared.I_Entity;
import org.opencms.acacia.shared.I_EntityAttribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.JsArrayString;

/**
 * The entity attribute values.<p>
 */
public final class EntityAttribute implements I_EntityAttribute {

    /** The complex type values. */
    private List<I_Entity> m_entityValues;

    /** The attribute name. */
    private String m_name;

    /** The simple type values. */
    private List<String> m_simpleValues;

    /**
     * Constructor.<p>
     * 
     * @param name the attribute name
     * @param values the values
     */
    public EntityAttribute(String name, I_EntityCollection values) {

        m_name = name;
        m_entityValues = new ArrayList<I_Entity>();
        for (int i = 0; i < values.size(); i++) {
            m_entityValues.add(values.getEntity(i));
        }
    }

    /**
     * Constructor.<p>
     * 
     * @param name the attribute name
     * @param values the values
     */
    public EntityAttribute(String name, JsArrayString values) {

        m_name = name;
        m_simpleValues = new ArrayList<String>();
        for (int i = 0; i < values.length(); i++) {
            m_simpleValues.add(values.get(i));
        }
    }

    /**
     * @see org.opencms.acacia.shared.I_EntityAttribute#getAttributeName()
     */
    public String getAttributeName() {

        return m_name;
    }

    /**
     * @see org.opencms.acacia.shared.I_EntityAttribute#getComplexValue()
     */
    public I_Entity getComplexValue() {

        return m_entityValues.get(0);
    }

    /**
     * @see org.opencms.acacia.shared.I_EntityAttribute#getComplexValues()
     */
    public List<I_Entity> getComplexValues() {

        return Collections.unmodifiableList(m_entityValues);
    }

    /**
     * @see org.opencms.acacia.shared.I_EntityAttribute#getSimpleValue()
     */
    public String getSimpleValue() {

        return m_simpleValues.get(0);
    }

    /**
     * @see org.opencms.acacia.shared.I_EntityAttribute#getSimpleValues()
     */
    public List<String> getSimpleValues() {

        return Collections.unmodifiableList(m_simpleValues);
    }

    /**
     * @see org.opencms.acacia.shared.I_EntityAttribute#getValueCount()
     */
    public int getValueCount() {

        if (isComplexValue()) {
            return m_entityValues.size();
        }
        return m_simpleValues.size();
    }

    /**
     * @see org.opencms.acacia.shared.I_EntityAttribute#isComplexValue()
     */
    public boolean isComplexValue() {

        return m_entityValues != null;
    }

    /**
     * @see org.opencms.acacia.shared.I_EntityAttribute#isSimpleValue()
     */
    public boolean isSimpleValue() {

        return m_simpleValues != null;
    }

    /**
     * @see org.opencms.acacia.shared.I_EntityAttribute#isSingleValue()
     */
    public boolean isSingleValue() {

        if (isComplexValue()) {
            return m_entityValues.size() == 1;
        }
        return m_simpleValues.size() == 1;
    }
}

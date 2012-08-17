/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.search.fields;

import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A abstract implementation for a search field.<p>
 * 
 * @since 8.5.0
 */
public abstract class A_CmsSearchField implements I_CmsSearchField {

    /** Serial version UID. */
    private static final long serialVersionUID = 3185631015824549119L;

    /** The boost factor of the field. */
    private float m_boost;

    /** A default value for the field in case the content does not provide the value. */
    private String m_defaultValue;

    /** The search field mappings. */
    private List<I_CmsSearchFieldMapping> m_mappings;

    /** The name of the field. */
    private String m_name;

    /**
     * Creates a new search field.<p>
     */
    public A_CmsSearchField() {

        m_mappings = new ArrayList<I_CmsSearchFieldMapping>();
        m_boost = BOOST_DEFAULT;
    }

    /**
     * Creates a new search field.<p>
     * 
     * @param name the name of the field, see {@link #setName(String)}
     * @param defaultValue the default value to use, see {@link #setDefaultValue(String)}
     * @param boost the boost factor, see {@link #setBoost(float)}
     * 
     */
    public A_CmsSearchField(String name, String defaultValue, float boost) {

        this();
        m_name = name;
        m_boost = boost;
        m_defaultValue = defaultValue;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchField#addMapping(org.opencms.search.fields.I_CmsSearchFieldMapping)
     */
    public void addMapping(I_CmsSearchFieldMapping mapping) {

        m_mappings.add(mapping);
    }

    /**
     * Two fields are equal if the name of the Lucene field is equal.<p>
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if ((obj instanceof A_CmsSearchField)) {
            return CmsStringUtil.isEqual(m_name, ((A_CmsSearchField)obj).getName());
        }
        return false;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchField#getBoost()
     */
    public float getBoost() {

        return m_boost;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchField#getDefaultValue()
     */
    public String getDefaultValue() {

        return m_defaultValue;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchField#getMappings()
     */
    public List<I_CmsSearchFieldMapping> getMappings() {

        return m_mappings;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchField#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * The hash code for a field is based only on the field name.<p>
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_name == null ? 41 : this.m_name.hashCode();
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchField#setBoost(float)
     */
    public void setBoost(float boost) {

        if (boost < 0.0F) {
            boost = 0.0F;
        }
        m_boost = boost;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchField#setBoost(java.lang.String)
     */
    public void setBoost(String boost) {

        try {
            setBoost(Float.valueOf(boost).floatValue());
        } catch (NumberFormatException e) {
            setBoost(1.0F);
        }
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchField#setDefaultValue(java.lang.String)
     */
    public void setDefaultValue(String defaultValue) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(defaultValue)) {
            m_defaultValue = defaultValue.trim();
        } else {
            m_defaultValue = null;
        }
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchField#setName(java.lang.String)
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return getName();
    }
}
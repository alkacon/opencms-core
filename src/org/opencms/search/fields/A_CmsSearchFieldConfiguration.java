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

package org.opencms.search.fields;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.solr.uninverting.UninvertingReader.Type;

/**
 * Base class for a typical field configuration. Basically handles name and description
 * and provides defaults for interface methods typically not of interest for most implementations.
 */
public abstract class A_CmsSearchFieldConfiguration implements I_CmsSearchFieldConfiguration {

    /** Description of the field configuration. */
    private String m_description;
    /** Name of the field configuration. */
    private String m_name;
    /** The list of configured {@link CmsSearchField} instances. */
    private List<CmsSearchField> m_fields;

    /** Map to lookup the configured {@link CmsSearchField} instances by name. */
    private Map<String, CmsSearchField> m_fieldLookup;

    /** The list of configured {@link CmsSearchField} names. */
    private List<String> m_fieldNames;

    /**
     * Creates a new empty field configuration.
     */
    public A_CmsSearchFieldConfiguration() {

        m_fields = new ArrayList<CmsSearchField>();
    }

    /**
     * Adds a field to this search field configuration.<p>
     *
     * @param field the field to add
     */
    public void addField(CmsSearchField field) {

        if (field != null) {
            m_fields.add(field);
        }
    }

    /**
     * Adds fields.<p>
     *
     * @param fields the fields to add
     */
    public void addFields(Collection<CmsSearchField> fields) {

        for (CmsSearchField field : fields) {
            if (!getFieldNames().contains(field.getName())) {
                addField(field);
            }
        }
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldConfiguration#addUninvertingMappings(java.util.Map)
     */
    public void addUninvertingMappings(Map<String, Type> uninvertingMap) {

        // Do nothing by default
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(I_CmsSearchFieldConfiguration o) {

        return m_name.compareTo(o.getName());
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj.getClass().getName().equals(obj.getClass().getName())) {
            return Objects.equals(((CmsSearchFieldConfiguration)obj).getName(), getName());
        }
        return false;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldConfiguration#getDescription()
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the configured {@link CmsSearchField} instance with the given name.<p>
     *
     * @param name the search field name to look up
     *
     * @return the configured {@link CmsSearchField} instance with the given name
     */
    public CmsSearchField getField(String name) {

        if (m_fieldLookup == null) {
            // lazy initialize the field names
            m_fieldLookup = new HashMap<String, CmsSearchField>();
            for (CmsSearchField field : m_fields) {
                m_fieldLookup.put(field.getName(), field);
            }
        }
        return m_fieldLookup.get(name);
    }

    /**
     * Returns the list of configured field names (Strings).<p>
     *
     * @return the list of configured field names (Strings)
     */
    public List<String> getFieldNames() {

        if (m_fieldNames == null) {
            // lazy initialize the field names
            m_fieldNames = new ArrayList<String>();
            for (CmsSearchField field : m_fields) {
                m_fieldNames.add(field.getName());
            }
        }
        // create a copy of the list to prevent changes in other classes
        return new ArrayList<String>(m_fieldNames);
    }

    /**
     * Returns the list of configured {@link CmsSearchField} instances.<p>
     *
     * @return the list of configured {@link CmsSearchField} instances
     */
    @Override
    public List<CmsSearchField> getFields() {

        return m_fields;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldConfiguration#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return null == m_name ? 0 : m_name.hashCode();
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldConfiguration#init()
     */
    public void init() {

        // By default do nothing

    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldConfiguration#setDescription(java.lang.String)
     */
    public void setDescription(String description) {

        m_description = description;

    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchFieldConfiguration#setName(java.lang.String)
     */
    public void setName(String name) {

        m_name = name;

    }
}

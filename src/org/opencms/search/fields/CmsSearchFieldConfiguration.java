/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/fields/CmsSearchFieldConfiguration.java,v $
 * Date   : $Date: 2006/12/11 13:23:58 $
 * Version: $Revision: 1.1.2.2 $
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

package org.opencms.search.fields;

import org.opencms.file.CmsPropertyDefinition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Describes a configuration of fields that are used in building a search index.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 7.0.0 
 */
public class CmsSearchFieldConfiguration implements Comparable {

    /**
     * The default for the standard search configuration.<p>
     * 
     * This defines the default that is used in case no "standard" field configuration 
     * is defined in <code>opencms-search.xml</code>.<p>
     */
    public static final CmsSearchFieldConfiguration DEFAULT_STANDARD = createStandardConfiguration();

    /** The name for the standard field configuration. */
    public static final String STR_STANDARD = "standard";

    /** The description for the standard field configuration. */
    public static final String STR_STANDARD_DESCRIPTION = "The standard field configuration.";

    /** The description of the configuration. */
    private String m_description;

    /** Contains all names of the fields that are used in the excerpt. */
    private List m_excerptFieldNames;

    /** The list of configured {@link CmsSearchField} names. */
    private List m_fieldNames;

    /** The list of configured {@link CmsSearchField} objects. */
    private List m_fields;

    /** The name of the configuration. */
    private String m_name;

    /**
     * Creates a new, empty field configuration.<p>
     */
    public CmsSearchFieldConfiguration() {

        m_fields = new ArrayList();
    }

    /**
     * Creates the default standard search configuration.<p>
     * 
     * This defines the default that is used in case no "standard" field configuration 
     * is defined in <code>opencms-search.xml</code>.<p>
     * 
     * @return the default standard search configuration
     */
    private static CmsSearchFieldConfiguration createStandardConfiguration() {

        CmsSearchFieldConfiguration result = new CmsSearchFieldConfiguration();
        result.setName(STR_STANDARD);
        result.setDescription(STR_STANDARD_DESCRIPTION);

        CmsSearchField field;
        // content mapping
        field = new CmsSearchField(CmsSearchField.FIELD_CONTENT, "%{label.searchindex.searchFieldContent}", true, true);
        field.addMapping(new CmsSearchFieldMapping(CmsSearchFieldMappingType.CONTENT, null));
        result.addField(field);

        // title mapping as a keyword
        field = new CmsSearchField(
            CmsSearchField.FIELD_TITLE,
            CmsSearchField.IGNORE_DISPLAY_NAME,
            true,
            true,
            false,
            false,
            0.0f,
            null);
        field.addMapping(new CmsSearchFieldMapping(
            CmsSearchFieldMappingType.PROPERTY,
            CmsPropertyDefinition.PROPERTY_TITLE));
        result.addField(field);

        // title mapping as indexed field
        field = new CmsSearchField(
            CmsSearchField.FIELD_TITLE_UNSTORED,
            "%{label.searchindex.searchFieldTitle}",
            false,
            true);
        field.addMapping(new CmsSearchFieldMapping(
            CmsSearchFieldMappingType.PROPERTY,
            CmsPropertyDefinition.PROPERTY_TITLE));
        result.addField(field);

        // mapping of "Keywords" property to search field with the same name
        field = new CmsSearchField(
            CmsSearchField.FIELD_KEYWORDS,
            "%{label.searchindex.searchFieldKeywords}",
            true,
            true);
        field.addMapping(new CmsSearchFieldMapping(
            CmsSearchFieldMappingType.PROPERTY,
            CmsPropertyDefinition.PROPERTY_KEYWORDS));
        result.addField(field);

        // mapping of "Description" property to search field with the same name
        field = new CmsSearchField(
            CmsSearchField.FIELD_DESCRIPTION,
            "%{label.searchindex.searchFieldDescription}",
            true,
            true);
        field.addMapping(new CmsSearchFieldMapping(
            CmsSearchFieldMappingType.PROPERTY,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION));
        result.addField(field);

        // "meta" field is a combination of "Title", "Keywords" and "Description" properties
        field = new CmsSearchField(CmsSearchField.FIELD_META, "%{label.searchindex.searchFieldMeta}", false, true);
        field.addMapping(new CmsSearchFieldMapping(
            CmsSearchFieldMappingType.PROPERTY,
            CmsPropertyDefinition.PROPERTY_TITLE));
        field.addMapping(new CmsSearchFieldMapping(
            CmsSearchFieldMappingType.PROPERTY,
            CmsPropertyDefinition.PROPERTY_KEYWORDS));
        field.addMapping(new CmsSearchFieldMapping(
            CmsSearchFieldMappingType.PROPERTY,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION));
        result.addField(field);

        return result;
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
     * @see java.lang.Comparable#compareTo(Object)
     */
    public int compareTo(Object obj) {

        if (obj instanceof CmsSearchFieldConfiguration) {
            return m_name.compareTo(((CmsSearchFieldConfiguration)obj).m_name);
        }
        return 0;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsSearchFieldConfiguration) {
            return ((CmsSearchFieldConfiguration)obj).m_name.equals(m_name);
        }
        return false;
    }

    /**
     * Returns the description of this field configuration.<p>
     * 
     * @return the description of this field configuration
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns a list of all field names (Strings) that are used in generating the search excerpt.<p>
     * 
     * @return a list of all field names (Strings) that are used in generating the search excerpt
     */
    public List getExcerptFieldNames() {

        if (m_excerptFieldNames == null) {
            // lazy initialize the field names
            m_excerptFieldNames = new ArrayList();
            Iterator i = m_fields.iterator();
            while (i.hasNext()) {
                CmsSearchField field = (CmsSearchField)i.next();
                if (field.isInExcerptAndStored()) {
                    m_excerptFieldNames.add(field.getName());
                }
            }
        }

        // create a copy of the list to prevent changes in other classes
        return new ArrayList(m_excerptFieldNames);
    }

    /**
     * Returns the list of configured field names (Strings).<p>
     * 
     * @return the list of configured field names (Strings)
     */
    public List getFieldNames() {

        if (m_fieldNames == null) {
            // lazy initialize the field names
            m_fieldNames = new ArrayList();
            Iterator i = m_fields.iterator();
            while (i.hasNext()) {
                m_fieldNames.add(((CmsSearchField)i.next()).getName());
            }
        }

        // create a copy of the list to prevent changes in other classes
        return new ArrayList(m_fieldNames);
    }

    /**
     * Returns the list of configured {@link CmsSearchField} instances.<p>
     * 
     * @return the list of configured {@link CmsSearchField} instances
     */
    public List getFields() {

        return m_fields;
    }

    /**
     * Returns the name of this field configuration.<p>
     *
     * @return the name of this field configuration
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return m_name.hashCode();
    }

    /**
     * Sets the description of this field configuration.<p>
     * 
     * @param description the description to set
     */
    public void setDescription(String description) {

        m_description = description;
    }

    /**
     * Sets the name of this field configuration.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        m_name = name;
    }
}
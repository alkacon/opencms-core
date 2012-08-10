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

package org.opencms.search.solr;

import org.opencms.main.OpenCms;
import org.opencms.search.fields.A_CmsSearchField;
import org.opencms.search.fields.A_CmsSearchFieldConfiguration;

import java.util.List;
import java.util.Locale;

import org.apache.lucene.document.Fieldable;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;

/**
 * An individual field for the Solr search index.<p>
 * 
 * @since 8.5.0
 */
public class CmsSolrField extends A_CmsSearchField {

    /** The fields to copy the value of this field to. */
    private List<String> m_copyFields;

    /** The locale of this field. */
    private Locale m_locale;

    /** The name of the field to use as prototype in order to create this field. */
    private String m_sourceField;

    /** The name of the field without locale postfix. */
    private String m_targetField;

    /**
     * Public constructor.<p>
     * 
     * @param sourceField the source field name
     * @param targetField the target field name
     * @param copyFields the field names to copy this field's value to
     * @param locale the locale
     * @param defaultValue the default value
     * @param boost the boost factor
     */
    public CmsSolrField(
        String sourceField,
        String targetField,
        List<String> copyFields,
        Locale locale,
        String defaultValue,
        float boost) {

        super(A_CmsSearchFieldConfiguration.getLocaleExtendedName(targetField, locale), defaultValue, boost);
        m_sourceField = sourceField;
        m_targetField = targetField;
        m_copyFields = copyFields;
        m_locale = locale;
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchField#createField(java.lang.String)
     */
    public Fieldable createField(String value) {

        return createField(getName(), value);
    }

    /**
     * @see org.opencms.search.fields.I_CmsSearchField#createField(java.lang.String, java.lang.String)
     */
    public Fieldable createField(String name, String value) {

        Fieldable fieldable = null;
        IndexSchema schema = OpenCms.getSearchManager().getSolrServerConfiguration().getSolrSchema();
        if (!schema.hasExplicitField(name)) {
            for (SchemaField protoType : schema.getDynamicFieldPrototypes()) {
                if (protoType.getName().equals(getSourceField())) {
                    SchemaField schemaField = new SchemaField(protoType, name);
                    schema.registerDynamicField(new SchemaField[] {schemaField});
                    fieldable = schemaField.createField(value, getBoost());
                }
            }
        }
        if (getCopyFields() != null) {
            for (String copyName : getCopyFields()) {
                schema.registerCopyField(name, copyName);
            }
        }
        return fieldable;
    }

    /**
     * Returns the copy fields.<p>
     * 
     * @return the copy fields.<p>
     */
    public List<String> getCopyFields() {

        return m_copyFields;
    }

    /**
     * Returns the locale.<p>
     * 
     * @return the locale
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the source field name.<p>
     * 
     * @return the source field name
     */
    public String getSourceField() {

        return m_sourceField;
    }

    /**
     * Returns the target field name.<p>
     * 
     * @return the target field name
     */
    public String getTargetField() {

        return m_targetField;
    }

    /**
     * Sets the copy field names.<p>
     * 
     * @param copyFields the field name to use as copy fields
     */
    public void setCopyFields(List<String> copyFields) {

        m_copyFields = copyFields;
    }

    /**
     * Sets the locale
     * 
     * @param locale the locale to set
     */
    public void setLocale(Locale locale) {

        m_locale = locale;
    }

    /**
     * Sets the field name of the field to use as prototype.<p>
     *  
     * @param sourceField the field name to use as prototype
     */
    public void setSourceField(String sourceField) {

        m_sourceField = sourceField;
    }

    /**
     * Sets the target field name
     * 
     * @param targetField the name to set
     */
    public void setTargetField(String targetField) {

        m_targetField = targetField;
    }
}
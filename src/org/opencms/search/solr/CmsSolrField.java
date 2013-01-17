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
import org.opencms.search.fields.CmsLuceneField;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.I_CmsSearchFieldMapping;

import java.util.List;
import java.util.Locale;

import org.apache.solr.schema.IndexSchema;

/**
 * An individual field for the Solr search index.<p>
 * 
 * @since 8.5.0
 */
public class CmsSolrField extends CmsSearchField {

    /** The serial version UID. */
    private static final long serialVersionUID = -3920245109164517028L;

    /** The fields to copy the value of this field to. */
    private List<String> m_copyFields;

    /** The locale of this field. */
    private Locale m_locale;

    /** The name of the field. */
    private String m_targetField;

    /**
     * Public constructor.<p>
     * 
     * @param luceneField
     */
    public CmsSolrField(CmsLuceneField luceneField) {

        super();
        String name = luceneField.getName();
        IndexSchema schema = OpenCms.getSearchManager().getSolrServerConfiguration().getSolrSchema();
        if (schema.hasExplicitField(name)) {
            // take the lucene field name for Solr
        } else if ((luceneField.getType() != null)
            && schema.isDynamicField(luceneField.getName() + "_" + luceneField.getType())) {
            // try to use the specified type attribute as dynamic field suffix
            name = luceneField.getName() + "_" + luceneField.getType();
        } else {
            // fallback create a general_text field
            name = luceneField.getName() + "_txt";
        }
        setName(name);
        setBoost(luceneField.getBoost());
        setDefaultValue(luceneField.getDefaultValue());

        for (I_CmsSearchFieldMapping mapping : luceneField.getMappings()) {
            addMapping(mapping);
        }
    }

    /**
     * Public constructor.<p>
     * 
     * @param targetField the target field name
     * @param copyFields the field names to copy this field's value to
     * @param locale the locale
     * @param defaultValue the default value
     * @param boost the boost factor
     */
    public CmsSolrField(String targetField, List<String> copyFields, Locale locale, String defaultValue, float boost) {

        super(targetField, defaultValue, boost);
        m_targetField = targetField;
        m_copyFields = copyFields;
        m_locale = locale;
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
     * Returns the locale of this field or <code>null</code> if the field does not have a locale.<p>
     * 
     * @return the locale of this field
     */
    public Locale getLocale() {

        return m_locale;
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
     * Sets the locale.<p>
     * 
     * @param locale the locale to set
     */
    public void setLocale(Locale locale) {

        m_locale = locale;
    }

    /**
     * Sets the target field name.<p>
     * 
     * @param targetField the name to set
     */
    public void setTargetField(String targetField) {

        m_targetField = targetField;
    }

    /**
     * @see org.opencms.search.fields.CmsSearchField#toString()
     */
    @Override
    public String toString() {

        return getName()
            + "["
            + " boost:"
            + getBoost()
            + " defaultValue:"
            + getDefaultValue()
            + " targetField:"
            + getTargetField()
            + " locale:"
            + getLocale()
            + " copyFields:"
            + getCopyFields()
            + " ]";
    }
}
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategory;
import org.opencms.search.I_CmsSearchDocument;
import org.opencms.search.documents.CmsDocumentDependency;
import org.opencms.search.fields.I_CmsSearchField;
import org.opencms.util.CmsUUID;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.schema.DateField;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;

/**
 * A search document implementation for Solr indexes.<p>
 * 
 * @since 8.5.0
 */
public class CmsSolrDocument implements I_CmsSearchDocument {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSolrDocument.class);

    /** The Solr document. */
    private SolrInputDocument m_doc;

    /** Holds the score for this document. */
    private float m_score;

    /**
     * Public constructor to create a encapsulate a Solr document.<p>
     * 
     * @param doc the Solr document
     */
    public CmsSolrDocument(SolrDocument doc) {

        m_doc = ClientUtils.toSolrInputDocument(doc);
    }

    /**
     * Public constructor to create a encapsulate a Solr document.<p>
     * 
     * @param doc the Solr document
     */
    public CmsSolrDocument(SolrInputDocument doc) {

        m_doc = doc;

    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addCategoryField(java.util.List)
     */
    public void addCategoryField(List<CmsCategory> categories) {

        if ((categories != null) && (categories.size() > 0)) {
            for (CmsCategory category : categories) {
                m_doc.addField(I_CmsSearchField.FIELD_CATEGORY, category.getPath());
            }
        }
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addContentField(byte[])
     */
    public void addContentField(byte[] data) {

        m_doc.setField(I_CmsSearchField.FIELD_CONTENT_BLOB, ByteBuffer.wrap(data));
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addContentLocales(java.util.List)
     */
    public void addContentLocales(List<Locale> locales) {

        if ((locales != null) && !locales.isEmpty()) {
            for (Locale locale : locales) {
                m_doc.addField(I_CmsSearchField.FIELD_CONTENT_LOCALES, locale.toString());
            }
        }
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addDateField(java.lang.String, long, boolean)
     */
    public void addDateField(String name, long time, boolean analyzed) {

        String dateValue = DateField.formatExternal(new Date(time));
        m_doc.addField(name, dateValue);
        if (analyzed) {
            m_doc.addField(name + I_CmsSearchField.FIELD_DATE_LOOKUP_SUFFIX, dateValue);
        }
    }

    /**
     * Adds the given document dependency to this document.<p>
     * 
     * @param cms the current CmsObject
     * @param resDeps the dependency
     */
    public void addDocumentDependency(CmsObject cms, CmsDocumentDependency resDeps) {

        m_doc.addField(I_CmsSearchField.FIELD_DEPENDENCY_TYPE, resDeps.getType());
        for (CmsDocumentDependency dep : resDeps.getDependencies()) {
            m_doc.addField("dep_" + dep.getType().toString(), dep.toJSON(cms, true).toString());
        }
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addPathField(java.lang.String)
     */
    public void addPathField(String rootPath) {

        String folderName = CmsResource.getFolderPath(rootPath);
        for (int i = 0; i < folderName.length(); i++) {
            char c = folderName.charAt(i);
            if (c == '/') {
                m_doc.addField(I_CmsSearchField.FIELD_PARENT_FOLDERS, folderName.substring(0, i + 1));
            }
        }
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addResourceLocales(java.util.List)
     */
    public void addResourceLocales(List<Locale> locales) {

        if ((locales != null) && !locales.isEmpty()) {
            for (Locale locale : locales) {
                m_doc.addField(I_CmsSearchField.FIELD_RESOURCE_LOCALES, locale.toString());
            }
        }
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addRootPathField(java.lang.String)
     */
    public void addRootPathField(String rootPath) {

        m_doc.addField(I_CmsSearchField.FIELD_PATH, rootPath);
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addSearchField(org.opencms.search.fields.I_CmsSearchField, java.lang.String)
     */
    public void addSearchField(I_CmsSearchField sfield, String value) {

        CmsSolrField field = (CmsSolrField)sfield;
        List<String> fieldsToAdd = new ArrayList<String>(Collections.singletonList(field.getName()));
        if ((field.getCopyFields() != null) && !field.getCopyFields().isEmpty()) {
            fieldsToAdd.addAll(field.getCopyFields());
        }
        for (String fieldName : fieldsToAdd) {

            IndexSchema schema = OpenCms.getSearchManager().getSolrServerConfiguration().getSolrSchema();
            try {
                FieldType type = schema.getFieldType(fieldName);
                if (type instanceof DateField) {
                    value = DateField.formatExternal(new Date(new Long(value).longValue()));
                }

                SolrInputField exfield = m_doc.getField(fieldName);
                if (exfield == null) {
                    if (schema.hasExplicitField(fieldName)) {
                        m_doc.addField(fieldName, value);
                    } else {
                        m_doc.addField(fieldName, value, field.getBoost());
                    }
                } else {
                    if (schema.hasExplicitField(fieldName)) {
                        m_doc.setField(fieldName, value);
                    } else {
                        m_doc.setField(fieldName, value, field.getBoost());
                    }
                }
            } catch (SolrException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addSuffixField(java.lang.String)
     */
    public void addSuffixField(String suffix) {

        m_doc.addField(I_CmsSearchField.FIELD_SUFFIX, suffix);
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addTypeField(java.lang.String)
     */
    public void addTypeField(String type) {

        m_doc.addField(I_CmsSearchField.FIELD_TYPE, type);
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#getContentBlob()
     */
    public byte[] getContentBlob() {

        Object o = m_doc.getFieldValue(I_CmsSearchField.FIELD_CONTENT_BLOB);
        if (o != null) {
            if (o instanceof byte[]) {
                return (byte[])o;
            }
            return o.toString().getBytes();
        }
        return null;
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#getDocument()
     */
    public Object getDocument() {

        return m_doc;
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#getFieldNames()
     */
    public List<String> getFieldNames() {

        return new ArrayList<String>(m_doc.getFieldNames());
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#getFieldValueAsDate(java.lang.String)
     */
    public Date getFieldValueAsDate(String fieldName) {

        Object o = m_doc.getFieldValue(fieldName);
        if (o instanceof Date) {
            return (Date)o;
        }
        if (o != null) {
            try {
                return DateField.parseDate(o.toString());
            } catch (ParseException e) {
                // ignore: not a valid date format
                LOG.debug(e);
            }
        }
        return null;
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#getFieldValueAsString(java.lang.String)
     */
    public String getFieldValueAsString(String fieldName) {

        Object o = m_doc.getFieldValue(fieldName);
        if (o != null) {
            return o.toString();
        }
        return null;
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#getMultivaluedFieldAsStringList(java.lang.String)
     */
    public List<String> getMultivaluedFieldAsStringList(String fieldName) {

        List<String> result = new ArrayList<String>();
        Collection<Object> coll = m_doc.getFieldValues(fieldName);
        if (coll != null) {
            for (Object o : coll) {
                if (o != null) {
                    result.add(o.toString());
                }
            }
            return result;
        }
        return null;
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#getPath()
     */
    public String getPath() {

        return getFieldValueAsString(I_CmsSearchField.FIELD_PATH);
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#getScore()
     */
    public float getScore() {

        Float score = (Float)getSolrDocument().getFirstValue(I_CmsSearchField.FIELD_SCORE);
        if (score != null) {
            m_score = score.floatValue();
            return m_score;
        }
        return 0F;
    }

    /**
     * Returns the Solr document.<p>
     * 
     * @return the Solr document
     */
    public SolrDocument getSolrDocument() {

        return ClientUtils.toSolrDocument(m_doc);
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#getType()
     */
    public String getType() {

        return getFieldValueAsString(I_CmsSearchField.FIELD_TYPE);
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#setBoost(float)
     */
    public void setBoost(float boost) {

        m_doc.setDocumentBoost(boost);
    }

    /**
     * Sets the id of this document.<p>
     * 
     * @param structureId the structure id to use
     */
    public void setId(CmsUUID structureId) {

        m_doc.addField(I_CmsSearchField.FIELD_ID, structureId);

    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#setScore(float)
     */
    public void setScore(float score) {

        m_score = score;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return getFieldValueAsString(I_CmsSearchField.FIELD_PATH);
    }
}

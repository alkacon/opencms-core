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
import org.opencms.search.CmsSearchUtil;
import org.opencms.search.I_CmsSearchDocument;
import org.opencms.search.documents.CmsDocumentDependency;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.util.CmsStringUtil;
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
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.schema.DatePointField;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;

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

        this();
        m_doc = CmsSearchUtil.toSolrInputDocument(doc);
    }

    /**
     * Public constructor to create a encapsulate a Solr document.<p>
     *
     * @param doc the Solr document
     */
    public CmsSolrDocument(SolrInputDocument doc) {

        this();
        m_doc = doc;
    }

    /**
     * Private constructor.<p>
     */
    private CmsSolrDocument() {

    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addCategoryField(java.util.List)
     */
    public void addCategoryField(List<CmsCategory> categories) {

        if ((categories != null) && (categories.size() > 0)) {
            for (CmsCategory category : categories) {
                m_doc.addField(CmsSearchField.FIELD_CATEGORY, category.getPath());
            }
        }
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addContentField(byte[])
     */
    public void addContentField(byte[] data) {

        m_doc.setField(CmsSearchField.FIELD_CONTENT_BLOB, ByteBuffer.wrap(data));
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addContentLocales(java.util.Collection)
     */
    public void addContentLocales(Collection<Locale> locales) {

        if ((locales != null) && !locales.isEmpty()) {
            for (Locale locale : locales) {
                m_doc.addField(CmsSearchField.FIELD_CONTENT_LOCALES, locale.toString());
            }
        }
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addDateField(java.lang.String, long, boolean)
     */
    public void addDateField(String name, long time, boolean analyzed) {

        String val = CmsSearchUtil.getDateAsIso8601(time);
        m_doc.addField(name, val);
        if (analyzed) {
            m_doc.addField(name + CmsSearchField.FIELD_DATE_LOOKUP_SUFFIX, val);
        }
    }

    /**
     * Adds the given document dependency to this document.<p>
     *
     * @param cms the current CmsObject
     * @param resDeps the dependency
     */
    public void addDocumentDependency(CmsObject cms, CmsDocumentDependency resDeps) {

        if (resDeps != null) {
            m_doc.addField(CmsSearchField.FIELD_DEPENDENCY_TYPE, resDeps.getType());
            if ((resDeps.getMainDocument() != null) && (resDeps.getType() != null)) {
                m_doc.addField(
                    CmsSearchField.FIELD_PREFIX_DEPENDENCY + resDeps.getType().toString(),
                    resDeps.getMainDocument().toDependencyString(cms));
            }
            for (CmsDocumentDependency dep : resDeps.getVariants()) {
                m_doc.addField(
                    CmsSearchField.FIELD_PREFIX_DEPENDENCY + dep.getType().toString(),
                    dep.toDependencyString(cms));
            }
            for (CmsDocumentDependency dep : resDeps.getAttachments()) {
                m_doc.addField(
                    CmsSearchField.FIELD_PREFIX_DEPENDENCY + dep.getType().toString(),
                    dep.toDependencyString(cms));
            }
        }
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addFileSizeField(int)
     */
    public void addFileSizeField(int length) {

        if (OpenCms.getSearchManager().getSolrServerConfiguration().getSolrSchema().hasExplicitField(
            CmsSearchField.FIELD_SIZE)) {
            m_doc.addField(CmsSearchField.FIELD_SIZE, Integer.valueOf(length));
        }
    }

    /**
     * Adds a multi-valued field.<p>
     *
     * @param fieldName the field name to put the values in
     * @param values the values to put in the field
     */
    public void addMultiValuedField(String fieldName, List<String> values) {

        if ((values != null) && (values.size() > 0)) {
            for (String value : values) {
                m_doc.addField(fieldName, value);
            }
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
                m_doc.addField(CmsSearchField.FIELD_PARENT_FOLDERS, folderName.substring(0, i + 1));
            }
        }
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addResourceLocales(java.util.Collection)
     */
    public void addResourceLocales(Collection<Locale> locales) {

        if ((locales != null) && !locales.isEmpty()) {
            for (Locale locale : locales) {
                m_doc.addField(CmsSearchField.FIELD_RESOURCE_LOCALES, locale.toString());
            }
        }
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addRootPathField(java.lang.String)
     */
    public void addRootPathField(String rootPath) {

        m_doc.addField(CmsSearchField.FIELD_PATH, rootPath);
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addSearchField(org.opencms.search.fields.CmsSearchField, java.lang.String)
     */
    public void addSearchField(CmsSearchField sfield, String value) {

        CmsSolrField field = (CmsSolrField)sfield;
        List<String> fieldsToAdd = new ArrayList<String>(Collections.singletonList(field.getName()));
        if ((field.getCopyFields() != null) && !field.getCopyFields().isEmpty()) {
            fieldsToAdd.addAll(field.getCopyFields());
        }
        IndexSchema schema = OpenCms.getSearchManager().getSolrServerConfiguration().getSolrSchema();
        for (String fieldName : fieldsToAdd) {
            try {
                List<String> splitedValues = new ArrayList<String>();
                boolean multi = false;
                boolean overrideValue = false;

                try {
                    SchemaField f = schema.getField(fieldName);
                    if ((f != null) && (!field.getName().startsWith(CmsSearchField.FIELD_CONTENT))) {
                        multi = f.multiValued();
                        overrideValue = !multi;
                    }
                } catch (@SuppressWarnings("unused") SolrException e) {
                    LOG.warn(Messages.get().getBundle().key(Messages.LOG_SOLR_FIELD_NOT_FOUND_1, field.toString()));
                }
                if (multi) {
                    splitedValues = CmsStringUtil.splitAsList(value.toString(), "\n");
                } else {
                    splitedValues.add(value);
                }
                for (String val : splitedValues) {
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(val)) {
                        try {
                            FieldType fieldType = schema.getFieldType(fieldName);
                            if (fieldType instanceof DatePointField) {
                                //sometime,the val is already Iso8601 formated
                                if (!val.contains("Z")) {
                                    val = CmsSearchUtil.getDateAsIso8601(Long.valueOf(val).longValue());
                                }
                            }
                        } catch (SolrException e) {
                            LOG.debug(e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                        if (fieldName.endsWith(CmsSearchField.FIELD_EXCERPT)) {
                            // TODO: make the length and the area configurable
                            val = CmsStringUtil.trimToSize(val, 1000, 50, "");
                        }
                        if (overrideValue) {
                            m_doc.setField(fieldName, val);
                        } else {
                            m_doc.addField(fieldName, val);
                        }
                    }
                }
            } catch (SolrException e) {
                LOG.error(e.getMessage(), e);
            } catch (@SuppressWarnings("unused") RuntimeException e) {
                // noop
            }
        }
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addSuffixField(java.lang.String)
     */
    public void addSuffixField(String suffix) {

        m_doc.addField(CmsSearchField.FIELD_SUFFIX, suffix);
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addTypeField(java.lang.String)
     */
    public void addTypeField(String type) {

        m_doc.addField(CmsSearchField.FIELD_TYPE, type);
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#getContentBlob()
     */
    public byte[] getContentBlob() {

        Object o = m_doc.getFieldValue(CmsSearchField.FIELD_CONTENT_BLOB);
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
                return CmsSearchUtil.parseDate(o.toString());
            } catch (ParseException e) {
                // ignore: not a valid date format
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }
        return null;
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#getFieldValueAsString(java.lang.String)
     */
    public String getFieldValueAsString(String fieldName) {

        List<String> values = getMultivaluedFieldAsStringList(fieldName);
        if ((values != null) && !values.isEmpty()) {
            return CmsStringUtil.listAsString(values, "\n");
        } else {
            Object o = m_doc.getFieldValue(fieldName);
            if (o != null) {
                return o.toString();
            }
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

        return getFieldValueAsString(CmsSearchField.FIELD_PATH);
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#getScore()
     */
    public float getScore() {

        Float score = (Float)getSolrDocument().getFirstValue(CmsSearchField.FIELD_SCORE);
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

        return CmsSearchUtil.toSolrDocument(m_doc);
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#getType()
     */
    public String getType() {

        return getFieldValueAsString(CmsSearchField.FIELD_TYPE);
    }

    /**
     * Sets the id of this document.<p>
     *
     * @param structureId the structure id to use
     */
    public void setId(CmsUUID structureId) {

        m_doc.addField(CmsSearchField.FIELD_ID, structureId.toString());

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

        return getFieldValueAsString(CmsSearchField.FIELD_PATH);
    }
}

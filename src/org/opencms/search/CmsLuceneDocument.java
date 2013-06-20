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

package org.opencms.search;

import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategory;
import org.opencms.search.fields.CmsLuceneField;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;
import org.apache.tika.io.IOUtils;

/**
 * A Lucene search document implementation.<p>
 */
public class CmsLuceneDocument implements I_CmsSearchDocument {

    /**
     * Type for a stored-only field.
     */
    public static final FieldType NOT_STORED_ANALYSED_TYPE;

    /**
     * Type for a stored-and analyzed fields.
     */
    public static final FieldType STORED_ANALYSED_TYPE;

    /**
     * Type for a stored-only field.
     */
    public static final FieldType STORED_NOT_ANALYSED_TYPE;

    /** The Lucene document. */
    private Document m_doc;

    /** The fields stored in this document. */
    private Map<String, Field> m_fields;

    /** Holds the score for this document. */
    private float m_score;

    /**
     * Public constructor.<p>
     * 
     * @param doc the Lucene document
     */
    public CmsLuceneDocument(Document doc) {

        m_doc = doc;
        m_fields = new HashMap<String, Field>();
    }

    static {
        STORED_ANALYSED_TYPE = new FieldType();
        STORED_ANALYSED_TYPE.setIndexed(true);
        STORED_ANALYSED_TYPE.setOmitNorms(false);
        STORED_ANALYSED_TYPE.setStored(true);
        STORED_ANALYSED_TYPE.setTokenized(true);
        STORED_ANALYSED_TYPE.setStoreTermVectorPositions(false);
        STORED_ANALYSED_TYPE.setStoreTermVectorOffsets(false);
        STORED_ANALYSED_TYPE.setStoreTermVectors(false);
        STORED_ANALYSED_TYPE.freeze();
    }

    static {
        NOT_STORED_ANALYSED_TYPE = new FieldType();
        NOT_STORED_ANALYSED_TYPE.setIndexed(true);
        NOT_STORED_ANALYSED_TYPE.setOmitNorms(false);
        NOT_STORED_ANALYSED_TYPE.setStored(false);
        NOT_STORED_ANALYSED_TYPE.setTokenized(true);
        NOT_STORED_ANALYSED_TYPE.setStoreTermVectorPositions(false);
        NOT_STORED_ANALYSED_TYPE.setStoreTermVectorOffsets(false);
        NOT_STORED_ANALYSED_TYPE.setStoreTermVectors(false);
        NOT_STORED_ANALYSED_TYPE.freeze();
    }

    static {
        STORED_NOT_ANALYSED_TYPE = new FieldType();
        STORED_NOT_ANALYSED_TYPE.setIndexed(true);
        STORED_NOT_ANALYSED_TYPE.setOmitNorms(false);
        STORED_NOT_ANALYSED_TYPE.setStored(true);
        STORED_NOT_ANALYSED_TYPE.setTokenized(false);
        STORED_NOT_ANALYSED_TYPE.setStoreTermVectorPositions(false);
        STORED_NOT_ANALYSED_TYPE.setStoreTermVectorOffsets(false);
        STORED_NOT_ANALYSED_TYPE.setStoreTermVectors(false);
        STORED_NOT_ANALYSED_TYPE.freeze();
    }

    /**
     * Generate a list of date terms for the optimized date range search.<p>
     * 
     * @param date the date for get the date terms for
     * 
     * @return a list of date terms for the optimized date range search
     * 
     * @see CmsSearchIndex#getDateRangeSpan(long, long)
     */
    public static String getDateTerms(long date) {

        Calendar cal = Calendar.getInstance(OpenCms.getLocaleManager().getTimeZone());
        cal.setTimeInMillis(date);
        String day = CmsSearchIndex.DATES[cal.get(5)];
        String month = CmsSearchIndex.DATES[(cal.get(2) + 1)];
        String year = String.valueOf(cal.get(1));

        StringBuffer result = new StringBuffer();
        result.append(year);
        result.append(month);
        result.append(day);
        result.append(' ');
        result.append(year);
        result.append(month);
        result.append(' ');
        result.append(year);
        return result.toString();
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addCategoryField(java.util.List)
     */
    public void addCategoryField(List<CmsCategory> categories) {

        if ((categories != null) && (categories.size() > 0)) {

            StringBuffer categoryBuffer = new StringBuffer(128);
            for (CmsCategory category : categories) {
                categoryBuffer.append(category.getPath());
                categoryBuffer.append(' ');
            }
            if (categoryBuffer.length() > 0) {
                Field field = new Field(
                    CmsSearchField.FIELD_CATEGORY,
                    categoryBuffer.toString().toLowerCase(),
                    STORED_ANALYSED_TYPE);
                field.setBoost(0);
                add(field);
            }
        } else {
            // synthetic "unknown" category if no category property defined for resource
            Field field = new Field(
                CmsSearchField.FIELD_CATEGORY,
                CmsSearchCategoryCollector.UNKNOWN_CATEGORY,
                STORED_ANALYSED_TYPE);
            add(field);
        }
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addContentField(byte[])
     */
    public void addContentField(byte[] data) {

        Field field = new StoredField(CmsSearchField.FIELD_CONTENT_BLOB, data);
        m_doc.add(field);
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addContentLocales(java.util.List)
     */
    public void addContentLocales(List<Locale> locales) {

        // Lucene documents are not localized by defualt: Nothing to do here
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addDateField(java.lang.String, long, boolean)
     */
    public void addDateField(String name, long date, boolean analyzed) {

        Field field = new Field(
            name,
            DateTools.dateToString(new Date(date), DateTools.Resolution.MILLISECOND),
            STORED_NOT_ANALYSED_TYPE);
        field.setBoost(0.0F);
        add(field);

        if (analyzed) {
            field = new Field(
                name + CmsSearchField.FIELD_DATE_LOOKUP_SUFFIX,
                getDateTerms(date),
                NOT_STORED_ANALYSED_TYPE);
            add(field);
        }
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addFileSizeField(int)
     */
    public void addFileSizeField(int length) {

        // a default lucene implementation does not have a field for the file size
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addPathField(java.lang.String)
     */
    public void addPathField(String rootPath) {

        String parentFolders = CmsSearchFieldConfiguration.getParentFolderTokens(rootPath);
        Field field = new Field(CmsSearchField.FIELD_PARENT_FOLDERS, parentFolders, NOT_STORED_ANALYSED_TYPE);
        field.setBoost(0.0F);
        add(field);
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addResourceLocales(java.util.List)
     */
    public void addResourceLocales(List<Locale> locales) {

        // A default lucene document has only one locale.
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addRootPathField(java.lang.String)
     */
    public void addRootPathField(String rootPath) {

        add(new StringField(CmsSearchField.FIELD_PATH, rootPath, Field.Store.YES));
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addSearchField(org.opencms.search.fields.CmsSearchField, java.lang.String)
     */
    public void addSearchField(CmsSearchField field, String value) {

        if (field instanceof CmsLuceneField) {
            add(((CmsLuceneField)field).createField(value));
        } else {
            new CmsRuntimeException(Messages.get().container(
                Messages.LOG_INVALID_FIELD_CLASS_1,
                field.getClass().getName()));
        }
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addSuffixField(java.lang.String)
     */
    public void addSuffixField(String suffix) {

        add(new StringField(CmsSearchField.FIELD_SUFFIX, suffix, Field.Store.YES));
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addTypeField(java.lang.String)
     */
    public void addTypeField(String typeName) {

        add(new StringField(CmsSearchField.FIELD_TYPE, typeName, Field.Store.YES));
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#getContentBlob()
     */
    public byte[] getContentBlob() {

        IndexableField fieldContentBlob = m_doc.getField(CmsSearchField.FIELD_CONTENT_BLOB);
        if (fieldContentBlob != null) {
            try {
                if (fieldContentBlob.readerValue() != null) {
                    return IOUtils.toByteArray(fieldContentBlob.readerValue());
                }
            } catch (IOException e) {
                // TODO:
            }
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

        List<String> result = new ArrayList<String>();
        for (IndexableField field : m_doc.getFields()) {
            result.add(field.name());
        }
        return result;
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#getFieldValueAsDate(java.lang.String)
     */
    public Date getFieldValueAsDate(String fieldName) {

        String contentDate = getFieldValueAsString(fieldName);
        if (contentDate != null) {
            try {
                return new Date(DateTools.stringToTime(contentDate));
            } catch (ParseException e) {
                // ignore and assume the given field name does not refer a date field
            }
        }
        return null;
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#getFieldValueAsString(java.lang.String)
     */
    public String getFieldValueAsString(String fieldName) {

        IndexableField fieldValue = m_doc.getField(fieldName);
        if (fieldValue != null) {
            return fieldValue.stringValue();
        }
        return null;
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#getMultivaluedFieldAsStringList(java.lang.String)
     */
    public List<String> getMultivaluedFieldAsStringList(String fieldName) {

        return Collections.singletonList(getFieldValueAsString(fieldName));
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

        return m_score;
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#getType()
     */
    public String getType() {

        return getFieldValueAsString(CmsSearchField.FIELD_TYPE);
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#setBoost(float)
     */
    public void setBoost(float boost) {

        m_doc.removeFields(CmsSearchField.FIELD_META);
        m_doc.removeFields(CmsSearchField.FIELD_CONTENT);
        m_doc.removeFields(CmsSearchField.FIELD_DESCRIPTION);
        m_doc.removeFields(CmsSearchField.FIELD_KEYWORDS);
        m_doc.removeFields(CmsSearchField.FIELD_TITLE);
        m_doc.removeFields(CmsSearchField.FIELD_TITLE_UNSTORED);

        Field f = m_fields.get(CmsSearchField.FIELD_META);
        if (f != null) {
            f.setBoost(boost);
            m_doc.add(f);
        }
        f = m_fields.get(CmsSearchField.FIELD_CONTENT);
        if (f != null) {
            f.setBoost(boost);
            m_doc.add(f);
        }
        f = m_fields.get(CmsSearchField.FIELD_DESCRIPTION);
        if (f != null) {
            f.setBoost(boost);
            m_doc.add(f);
        }
        f = m_fields.get(CmsSearchField.FIELD_KEYWORDS);
        if (f != null) {
            f.setBoost(boost);
            m_doc.add(f);
        }
        f = m_fields.get(CmsSearchField.FIELD_TITLE);
        if (f != null) {
            f.setBoost(boost);
            m_doc.add(f);
        }
        f = m_fields.get(CmsSearchField.FIELD_META);
        if (f != null) {
            f.setBoost(boost);
            m_doc.add(f);
        }
        f = m_fields.get(CmsSearchField.FIELD_TITLE_UNSTORED);
        if (f != null) {
            f.setBoost(boost);
            m_doc.add(f);
        }
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#setScore(float)
     */
    public void setScore(float score) {

        m_score = score;
    }

    /**
     * Adds a field to this document.<p>
     * 
     * @param f the field to add
     */
    private void add(Field f) {

        m_fields.put(f.name(), f);
        m_doc.add(f);
    }
}
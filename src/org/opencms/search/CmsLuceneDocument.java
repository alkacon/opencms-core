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

import org.opencms.main.OpenCms;
import org.opencms.relations.CmsCategory;
import org.opencms.search.fields.A_CmsSearchFieldConfiguration;
import org.opencms.search.fields.I_CmsSearchField;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

/**
 * A Lucene search document implamentation.<p>
 */
public class CmsLuceneDocument implements I_CmsSearchDocument {

    /** The Lucene document. */
    private Document m_doc;

    /** Holds the score for this document. */
    private float m_score;

    /**
     * Public constructor.<p>
     * 
     * @param doc the Lucene document
     */
    public CmsLuceneDocument(Document doc) {

        m_doc = doc;
    }

    /**
     * Generate a list of date terms for the optimized date range search.<p>
     * 
     * @param date the date for get the date terms for
     * 
     * @return a list of date terms for the optimized date range search
     * 
     * @see CmsLuceneIndex#getDateRangeSpan(long, long)
     */
    public static String getDateTerms(long date) {

        Calendar cal = Calendar.getInstance(OpenCms.getLocaleManager().getTimeZone());
        cal.setTimeInMillis(date);
        String day = CmsLuceneIndex.DATES[cal.get(5)];
        String month = CmsLuceneIndex.DATES[(cal.get(2) + 1)];
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
                Fieldable field = new Field(
                    I_CmsSearchField.FIELD_CATEGORY,
                    categoryBuffer.toString().toLowerCase(),
                    Field.Store.YES,
                    Field.Index.ANALYZED);
                field.setBoost(0);
                m_doc.add(field);
            }
        } else {
            // synthetic "unknown" category if no category property defined for resource
            Fieldable field = new Field(
                I_CmsSearchField.FIELD_CATEGORY,
                CmsSearchCategoryCollector.UNKNOWN_CATEGORY,
                Field.Store.YES,
                Field.Index.ANALYZED);
            m_doc.add(field);
        }

    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addContentField(byte[])
     */
    public void addContentField(byte[] data) {

        Fieldable field = new Field(I_CmsSearchField.FIELD_CONTENT_BLOB, data);
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

        Fieldable field = new Field(
            name,
            DateTools.dateToString(new Date(date), DateTools.Resolution.MILLISECOND),
            Field.Store.YES,
            Field.Index.NOT_ANALYZED);

        field.setBoost(0.0F);
        m_doc.add(field);
        if (analyzed) {
            field = new Field(
                name + I_CmsSearchField.FIELD_DATE_LOOKUP_SUFFIX,
                getDateTerms(date),
                Field.Store.NO,
                Field.Index.ANALYZED);

            m_doc.add(field);
        }
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addPathField(java.lang.String)
     */
    public void addPathField(String rootPath) {

        String parentFolders = A_CmsSearchFieldConfiguration.getParentFolderTokens(rootPath);
        Fieldable field = new Field(
            I_CmsSearchField.FIELD_PARENT_FOLDERS,
            parentFolders,
            Field.Store.NO,
            Field.Index.ANALYZED);
        field.setBoost(0.0F);
        m_doc.add(field);
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

        m_doc.add(new Field(I_CmsSearchField.FIELD_PATH, rootPath, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addSearchField(org.opencms.search.fields.I_CmsSearchField, java.lang.String)
     */
    public void addSearchField(I_CmsSearchField field, String value) {

        m_doc.add(field.createField(value));
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#addTypeField(java.lang.String)
     */
    public void addTypeField(String typeName) {

        m_doc.add(new Field(I_CmsSearchField.FIELD_TYPE, typeName, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#getContentBlob()
     */
    public byte[] getContentBlob() {

        Fieldable fieldContentBlob = m_doc.getFieldable(I_CmsSearchField.FIELD_CONTENT_BLOB);
        if (fieldContentBlob != null) {
            return fieldContentBlob.getBinaryValue();
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
        for (Fieldable field : m_doc.getFields()) {
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

        Fieldable fieldValue = m_doc.getFieldable(fieldName);
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

        return getFieldValueAsString(I_CmsSearchField.FIELD_PATH);
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

        return getFieldValueAsString(I_CmsSearchField.FIELD_TYPE);
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#setBoost(float)
     */
    public void setBoost(float boost) {

        m_doc.setBoost(boost);
    }

    /**
     * @see org.opencms.search.I_CmsSearchDocument#setScore(float)
     */
    public void setScore(float score) {

        m_score = score;
    }
}
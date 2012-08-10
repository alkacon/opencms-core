/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.file.CmsResource;
import org.opencms.search.CmsLuceneDocument;
import org.opencms.search.CmsLuceneIndex;
import org.opencms.search.I_CmsSearchDocument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;

/**
 * Describes a configuration of fields that are used in building a search index.<p>
 * 
 * @since 7.0.0 
 */
public class CmsSearchFieldConfiguration extends A_CmsSearchFieldConfiguration {

    /**
     * The default for the standard search configuration.<p>
     * 
     * This defines the default that is used in case no "standard" field configuration 
     * is defined in <code>opencms-search.xml</code>.<p>
     */
    public static final CmsSearchFieldConfiguration DEFAULT_STANDARD = createStandardConfiguration();

    /** The description for the standard field configuration. */
    public static final String STR_STANDARD_DESCRIPTION = "The standard OpenCms search index field configuration.";

    /** Contains all names of the fields that are used in the excerpt. */
    private List<String> m_excerptFieldNames;

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
        // content mapping, store as compressed value
        field = new CmsSearchField(
            I_CmsSearchField.FIELD_CONTENT,
            "%(key.field.content)",
            true,
            true,
            true,
            true,
            true,
            null,
            I_CmsSearchField.BOOST_DEFAULT,
            null);
        field.addMapping(new CmsSearchFieldMapping(CmsSearchFieldMappingType.CONTENT, null));
        result.addField(field);

        // title mapping as a keyword
        field = new CmsSearchField(
            I_CmsSearchField.FIELD_TITLE,
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
        field = new CmsSearchField(I_CmsSearchField.FIELD_TITLE_UNSTORED, "%(key.field.title)", false, true);
        field.addMapping(new CmsSearchFieldMapping(
            CmsSearchFieldMappingType.PROPERTY,
            CmsPropertyDefinition.PROPERTY_TITLE));
        result.addField(field);

        // mapping of "Keywords" property to search field with the same name
        field = new CmsSearchField(I_CmsSearchField.FIELD_KEYWORDS, "%(key.field.keywords)", true, true);
        field.addMapping(new CmsSearchFieldMapping(
            CmsSearchFieldMappingType.PROPERTY,
            CmsPropertyDefinition.PROPERTY_KEYWORDS));
        result.addField(field);

        // mapping of "Description" property to search field with the same name
        field = new CmsSearchField(I_CmsSearchField.FIELD_DESCRIPTION, "%(key.field.description)", true, true);
        field.addMapping(new CmsSearchFieldMapping(
            CmsSearchFieldMappingType.PROPERTY,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION));
        result.addField(field);

        // "meta" field is a combination of "Title", "Keywords" and "Description" properties
        field = new CmsSearchField(I_CmsSearchField.FIELD_META, "%(key.field.meta)", false, true);
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
     * @see org.opencms.search.fields.I_CmsSearchFieldConfiguration#createEmptyDocument(org.opencms.file.CmsResource)
     */
    public I_CmsSearchDocument createEmptyDocument(CmsResource res) {

        return new CmsLuceneDocument(new Document());
    }

    /**
     * Returns an analyzer that wraps the given base analyzer with the analyzers of this individual field configuration.<p>
     * 
     * @param analyzer the base analyzer to wrap
     * 
     * @return an analyzer that wraps the given base analyzer with the analyzers of this individual field configuration
     */
    public Analyzer getAnalyzer(Analyzer analyzer) {

        // parent folder and last modified lookup fields must use whitespace analyzer
        WhitespaceAnalyzer ws = new WhitespaceAnalyzer(CmsLuceneIndex.LUCENE_VERSION);
        Map<String, Analyzer> analyzers = new HashMap<String, Analyzer>();
        // first make map the default hard coded fields
        analyzers.put(I_CmsSearchField.FIELD_PARENT_FOLDERS, ws);
        analyzers.put(I_CmsSearchField.FIELD_CATEGORY, ws);
        analyzers.put(I_CmsSearchField.FIELD_DATE_LASTMODIFIED_LOOKUP, ws);
        analyzers.put(I_CmsSearchField.FIELD_DATE_CREATED_LOOKUP, ws);

        for (CmsSearchField field : getLuceneFields()) {
            Analyzer fieldAnalyzer = field.getAnalyzer();
            if (fieldAnalyzer != null) {
                // this field has an individual analyzer configured
                analyzers.put(field.getName(), fieldAnalyzer);
            }
        }
        // return the individual field configured analyzer
        return new PerFieldAnalyzerWrapper(analyzer, analyzers);
    }

    /**
     * Returns a list of all field names (Strings) that are used in generating the search excerpt.<p>
     * 
     * @return a list of all field names (Strings) that are used in generating the search excerpt
     */
    public List<String> getExcerptFieldNames() {

        if (m_excerptFieldNames == null) {
            // lazy initialize the field names
            m_excerptFieldNames = new ArrayList<String>();
            Iterator<I_CmsSearchField> i = getFields().iterator();
            while (i.hasNext()) {
                CmsSearchField field = (CmsSearchField)i.next();
                if (field.isInExcerptAndStored()) {
                    m_excerptFieldNames.add(field.getName());
                }
            }
        }

        // create a copy of the list to prevent changes in other classes
        return new ArrayList<String>(m_excerptFieldNames);
    }

    /**
     * Returns a list of the concrete Lucene search fields.<p>
     * 
     * @return a list of lucene search fields
     */
    public List<CmsSearchField> getLuceneFields() {

        List<CmsSearchField> result = new ArrayList<CmsSearchField>();
        for (I_CmsSearchField field : getFields()) {
            if (field instanceof CmsSearchField) {
                result.add((CmsSearchField)field);
            }
        }
        return result;
    }
}

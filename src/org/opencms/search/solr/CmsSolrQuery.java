/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2008 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.i18n.CmsEncoder;
import org.opencms.search.fields.I_CmsSearchField;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.common.params.CommonParams;

/**
 * A Solr search query.<p>
 */
public class CmsSolrQuery {

    /** A constant to add the score field to the result documents. */
    public static final String ALL_RETURN_FIELDS = "*,score";

    /** The default facet date gap. */
    public static final String DEFAULT_FACET_DATE_GAP = "+1DAY";

    /** The default query. */
    public static final String DEFAULT_QUERY = "*:*";

    /** The query type. */
    public static final String DEFAULT_QUERY_TYPE = "dismax";

    /** The default search result count. */
    public static final int DEFAULT_ROWS = 10;

    /** A constant to add the score field to the result documents. */
    public static final String MINIMUM_FIELDS = I_CmsSearchField.FIELD_PATH
        + ","
        + I_CmsSearchField.FIELD_TYPE
        + ","
        + I_CmsSearchField.FIELD_ID;

    /** A constant to add the score field to the result documents. */
    public static final String STRUCTURE_FIELDS = I_CmsSearchField.FIELD_PATH
        + ","
        + I_CmsSearchField.FIELD_TYPE
        + ","
        + I_CmsSearchField.FIELD_ID
        + ","
        + I_CmsSearchField.FIELD_CATEGORY
        + ","
        + I_CmsSearchField.FIELD_DATE_CONTENT
        + ","
        + I_CmsSearchField.FIELD_DATE_CREATED
        + ","
        + I_CmsSearchField.FIELD_DATE_EXPIRED
        + ","
        + I_CmsSearchField.FIELD_DATE_LASTMODIFIED
        + ","
        + I_CmsSearchField.FIELD_DATE_RELEASED
        + ","
        + I_CmsSearchField.FIELD_SUFFIX
        + ","
        + I_CmsSearchField.FIELD_DEPENDENCY_TYPE
        + ","
        + I_CmsSearchField.FIELD_DESCRIPTION
        + ","
        + CmsPropertyDefinition.PROPERTY_TITLE
        + "_prop"
        + ","
        + I_CmsSearchField.FIELD_RESOURCE_LOCALES
        + ","
        + I_CmsSearchField.FIELD_CONTENT_LOCALES
        + ","
        + I_CmsSearchField.FIELD_SCORE
        + ","
        + I_CmsSearchField.FIELD_PARENT_FOLDERS;

    /** The categories. */
    private List<String> m_categories = new ArrayList<String>();

    /** A map of date ranges (key = field name, value = Pair<start, end>). */
    private Map<String, CmsPair<Date, Date>> m_dateRanges = new HashMap<String, CmsPair<Date, Date>>();

    /** The facet date gap to use for date facets. */
    private String m_facetDateGap = DEFAULT_FACET_DATE_GAP;

    /** The return fields.  */
    private String m_fields = ALL_RETURN_FIELDS;

    /** The locales to search for. */
    private List<Locale> m_locales = new ArrayList<Locale>();

    /** The parameters given by the 'query string'.  */
    private Map<String, String[]> m_queryParameters = new HashMap<String, String[]>();

    /** The query String. */
    private String m_queryString = DEFAULT_QUERY;

    /** The query type. */
    private String m_queryType = DEFAULT_QUERY_TYPE;

    /** The list of resource types to search for. */
    private List<String> m_resourceTypes = new ArrayList<String>();

    /** The count of documents returned. */
    private Integer m_rows = new Integer(DEFAULT_ROWS);

    /** The absolute VFS paths that should be considered for searching. */
    private List<String> m_searchRoots = new ArrayList<String>();

    /** The Solr query. */
    private SolrQuery m_solrQuery = new SolrQuery();

    /** The of sort fields (key = field name, value = sort order), if empty the Solr default is taken (by score). */
    private Map<String, ORDER> m_sortFields = new LinkedHashMap<String, SolrQuery.ORDER>();

    /** Signals whether this is a structure query or not. */
    private boolean m_structureQuery;

    /** The text to search for. */
    private String m_text;

    /** The name of the field to search the text in. */
    private List<String> m_textSearchFields = new ArrayList<String>();

    /**
     * Default constructor.<p>
     */
    public CmsSolrQuery() {

        // noop
    }

    /**
     * Public constructor.<p>
     * 
     * @param cms the current OpenCms context
     * @param queryParams the Solr query parameters
     */
    public CmsSolrQuery(CmsObject cms, Map<String, String[]> queryParams) {

        // set the values from the request context
        if (cms != null) {
            m_locales.add(cms.getRequestContext().getLocale());
            m_searchRoots.add(cms.getRequestContext().getSiteRoot() + "/");
        }
        if (queryParams != null) {
            m_queryParameters = queryParams;
        }
    }

    /**
     * Returns the categories.<p>
     *
     * @return the categories
     */
    public List<String> getCategories() {

        return m_categories;
    }

    /**
     * Returns the dateRanges.<p>
     *
     * @return the dateRanges
     */
    public Map<String, CmsPair<Date, Date>> getDateRanges() {

        return m_dateRanges;
    }

    /**
     * Returns the facetDateGap.<p>
     *
     * @return the facetDateGap
     */
    public String getFacetDateGap() {

        return m_facetDateGap;
    }

    /**
     * Returns the fields.<p>
     *
     * @return the fields
     */
    public String getFields() {

        return m_fields;
    }

    /**
     * Returns the locales.<p>
     *
     * @return the locales
     */
    public List<Locale> getLocales() {

        return m_locales;
    }

    /**
     * Returns the queryString.<p>
     *
     * @return the queryString
     */
    public String getQueryString() {

        return m_queryString;
    }

    /**
     * Returns the queryType.<p>
     *
     * @return the queryType
     */
    public String getQueryType() {

        return m_queryType;
    }

    /**
     * Returns the resourceTypes.<p>
     *
     * @return the resourceTypes
     */
    public List<String> getResourceTypes() {

        return m_resourceTypes;
    }

    /**
     * Returns the rows.<p>
     *
     * @return the rows
     */
    public Integer getRows() {

        return m_rows;
    }

    /**
     * Returns the searchRoots.<p>
     *
     * @return the searchRoots
     */
    public List<String> getSearchRoots() {

        return m_searchRoots;
    }

    /**
     * Returns the solrQuery.<p>
     *
     * @return the solrQuery
     */
    public SolrQuery getSolrQuery() {

        return m_solrQuery;
    }

    /**
     * Returns the sortFields.<p>
     *
     * @return the sortFields
     */
    public Map<String, ORDER> getSortFields() {

        return m_sortFields;
    }

    /**
     * Returns the text.<p>
     *
     * @return the text
     */
    public String getText() {

        return m_text;
    }

    /**
     * Returns the textSearchFields.<p>
     *
     * @return the textSearchFields
     */
    public List<String> getTextSearchFields() {

        return m_textSearchFields;
    }

    /**
     * Returns the structureQuery.<p>
     *
     * @return the structureQuery
     */
    public boolean isStructureQuery() {

        return m_structureQuery;
    }

    /**
     * Sets the categories.<p>
     *
     * @param categories the categories to set
     */
    public void setCategories(List<String> categories) {

        m_categories = categories;
    }

    /**
     * Sets the dateRanges.<p>
     *
     * @param dateRanges the dateRanges to set
     */
    public void setDateRanges(Map<String, CmsPair<Date, Date>> dateRanges) {

        m_dateRanges = dateRanges;
    }

    /**
     * Sets the facetDateGap.<p>
     *
     * @param facetDateGap the facetDateGap to set
     */
    public void setFacetDateGap(String facetDateGap) {

        m_facetDateGap = facetDateGap;
    }

    /**
     * Sets the fields.<p>
     *
     * @param fields the fields to set
     */
    public void setFields(String fields) {

        m_fields = fields;
    }

    /**
     * Sets the locales.<p>
     *
     * @param locales the locales to set
     */
    public void setLocales(List<Locale> locales) {

        m_locales = locales;
    }

    /**
     * Sets the locales.<p>
     *
     * @param locales the locales to set
     */
    public void setLocales(Locale... locales) {

        m_locales = Arrays.asList(locales);
    }

    /**
     * Sets the queryString.<p>
     *
     * @param queryString the queryString to set
     */
    public void setQueryString(String queryString) {

        m_queryString = queryString;
    }

    /**
     * Sets the queryType.<p>
     *
     * @param queryType the queryType to set
     */
    public void setQueryType(String queryType) {

        m_queryType = queryType;
    }

    /**
     * Sets the resourceTypes.<p>
     *
     * @param resourceTypes the resourceTypes to set
     */
    public void setResourceTypes(List<String> resourceTypes) {

        m_resourceTypes = resourceTypes;
    }

    /**
     * Sets the resourceTypes.<p>
     *
     * @param resourceTypes the resourceTypes to set
     */
    public void setResourceTypes(String... resourceTypes) {

        m_resourceTypes = Arrays.asList(resourceTypes);
    }

    /**
     * Sets the rows.<p>
     *
     * @param rows the rows to set
     */
    public void setRows(Integer rows) {

        m_rows = rows;
    }

    /**
     * Sets the searchRoots.<p>
     *
     * @param searchRoots the searchRoots to set
     */
    public void setSearchRoots(List<String> searchRoots) {

        m_searchRoots = searchRoots;
    }

    /**
     * Sets the searchRoots.<p>
     *
     * @param searchRoots the searchRoots to set
     */
    public void setSearchRoots(String... searchRoots) {

        m_searchRoots = Arrays.asList(searchRoots);
    }

    /**
     * Sets the solrQuery.<p>
     *
     * @param solrQuery the solrQuery to set
     */
    public void setSolrQuery(SolrQuery solrQuery) {

        m_solrQuery = solrQuery;
    }

    /**
     * Sets the sortFields.<p>
     *
     * @param sortFields the sortFields to set
     */
    public void setSortFields(Map<String, ORDER> sortFields) {

        m_sortFields = sortFields;
    }

    /**
     * Sets the structureQuery.<p>
     *
     * @param structureQuery the structureQuery to set
     */
    public void setStructureQuery(boolean structureQuery) {

        m_structureQuery = structureQuery;
    }

    /**
     * Sets the text.<p>
     *
     * @param text the text to set
     */
    public void setText(String text) {

        m_text = text;
    }

    /**
     * Sets the textSearchFields.<p>
     *
     * @param textSearchFields the textSearchFields to set
     */
    public void setTextSearchFields(String... textSearchFields) {

        m_textSearchFields = Arrays.asList(textSearchFields);
    }

    /**
     * Sets the textSearchFields.<p>
     *
     * @param textSearchFields the textSearchFields to set
     */
    public void setTextSearchFields(List<String> textSearchFields) {

        m_textSearchFields = textSearchFields;
    }

    /**
     * Returns the Solr query.<p>
     * 
     * @return the Solr query
     */
    public SolrQuery toQuery() {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_text)) {
            m_solrQuery.setQuery(createTextQuery());
        } else {
            m_solrQuery.setQuery(m_queryString);
        }
        if (m_structureQuery) {
            m_solrQuery.setFields(STRUCTURE_FIELDS);
        } else {
            m_solrQuery.setFields(m_fields);
        }
        m_solrQuery.setQueryType(m_queryType);
        m_solrQuery.setRows(m_rows);

        // set the values from the members
        if ((m_sortFields != null) && !m_sortFields.isEmpty()) {
            addSortFields(m_sortFields);
        }
        if ((m_locales != null) && !m_locales.isEmpty()) {
            addLocales(m_locales);
        }
        if ((m_dateRanges != null) && !m_dateRanges.isEmpty()) {
            addDateRanges(m_dateRanges);
        }
        if ((m_categories != null) && !m_categories.isEmpty()) {
            addFilterQuery(I_CmsSearchField.FIELD_CATEGORY + "_exact", true, m_categories);
        }
        if ((m_resourceTypes != null) && !m_resourceTypes.isEmpty()) {
            addFilterQuery(I_CmsSearchField.FIELD_TYPE, false, m_resourceTypes);
        }
        if ((m_searchRoots != null) && !m_searchRoots.isEmpty()) {
            addFilterQuery(I_CmsSearchField.FIELD_PARENT_FOLDERS, false, m_searchRoots);
        }

        // overwrite already set values with values from query String
        if ((m_queryParameters != null) && !m_queryParameters.isEmpty()) {
            for (Map.Entry<String, String[]> entry : m_queryParameters.entrySet()) {
                if (!entry.getKey().equals(CommonParams.FQ)) {
                    // add or replace all parameters from the query String
                    m_solrQuery.setParam(entry.getKey(), entry.getValue());
                } else {
                    // special handling for filter queries
                    replaceFilterQueries(entry.getValue());
                }
            }
        }
        ensureReturnFields();
        return m_solrQuery;
    }

    @Override
    public String toString() {

        return CmsEncoder.decode(toQuery().toString());
    }

    /**
     * Sets date ranges.<p>
     * 
     * The parameter Map uses as:<p>
     * <ul>
     * <li><code>keys: </code>Solr field name {@link org.opencms.search.fields.I_CmsSearchField} and
     * <li><code>values: </code> pairs with min date as first and max date as second {@link org.opencms.util.CmsPair}
     * </ul>
     * Alternatively you can use Solr standard query syntax like:<p>
     * <ul>
     * <li><code>+created:[* TO NOW]</code>
     * <li><code>+lastmodified:[' + date + ' TO NOW]</code>
     * </ul>
     * whereby date is Solr formated:
     * {@link org.apache.solr.schema.DateField#formatExternal(Date)}
     * <p>
     * 
     * @param ranges the ranges map with field name as key and a CmsPair with min date as first and max date as second
     */
    private void addDateRanges(Map<String, CmsPair<Date, Date>> ranges) {

        // remove the date ranges
        if (m_dateRanges != null) {
            for (Map.Entry<String, CmsPair<Date, Date>> entry : m_dateRanges.entrySet()) {
                m_solrQuery.removeFacetField(entry.getKey());
            }
        }
        // add the date ranges
        if (ranges != null) {
            for (Map.Entry<String, CmsPair<Date, Date>> entry : ranges.entrySet()) {
                m_solrQuery.addDateRangeFacet(
                    entry.getKey(),
                    entry.getValue().getFirst(),
                    entry.getValue().getSecond(),
                    m_facetDateGap);
            }
        }
        m_dateRanges = ranges;
    }

    /**
     * Adds a filter query.<p>
     * 
     * @param fieldName the field name
     * @param all <code>true</code> to combine the given values with 'AND', <code>false</code> for 'OR'
     * @param vals the values
     */
    private void addFilterQuery(String fieldName, boolean all, List<String> vals) {

        if (m_solrQuery.getFilterQueries() != null) {
            for (String fq : m_solrQuery.getFilterQueries()) {
                if (fq.startsWith(fieldName + ":")) {
                    m_solrQuery.removeFilterQuery(fq);
                }
            }
        }
        String fq = createFilterQuery(fieldName, all, vals);
        m_solrQuery.addFilterQuery(fq);
    }

    /**
     * Sets the locales.<p>
     * 
     * @param locales the locales to set
     */
    private void addLocales(List<Locale> locales) {

        m_locales = locales;
        m_textSearchFields = new ArrayList<String>();
        if ((locales == null) || locales.isEmpty()) {
            m_textSearchFields.add(I_CmsSearchField.FIELD_TEXT);
            if (m_solrQuery.getFilterQueries() != null) {
                for (String fq : m_solrQuery.getFilterQueries()) {
                    if (fq.startsWith(I_CmsSearchField.FIELD_CONTENT_LOCALES + ":")) {
                        m_solrQuery.removeFilterQuery(fq);
                    }
                }
            }
        } else {
            List<String> localeStrings = new ArrayList<String>();
            for (Locale locale : locales) {
                localeStrings.add(locale.toString());
                m_textSearchFields.add("text_" + locale);
            }
            addFilterQuery(I_CmsSearchField.FIELD_CONTENT_LOCALES, false, localeStrings);
        }
    }

    /**
     * Sets the fields that should be used to sort the results.<p>
     * 
     * @param sortFields the sort fields to set
     */
    private void addSortFields(Map<String, ORDER> sortFields) {

        // add the sort fields to the query
        m_sortFields.putAll(sortFields);
        if (m_sortFields != null) {
            for (Map.Entry<String, ORDER> entry : m_sortFields.entrySet()) {
                m_solrQuery.addSortField(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Add the given values to any existing name.<p>
     * 
     * @param fieldName the name of the field to add to the query
     * @param all <code>true</code> for combining multiple values with 'AND' 
     * @param vals List of value(s) added to the name
     * 
     * @return this query
     */
    private String createFilterQuery(String fieldName, boolean all, List<String> vals) {

        String filterQuery = null;
        if ((vals != null)) {
            if (vals.size() == 1) {
                filterQuery = fieldName + ":" + vals.get(0);
            } else if (vals.size() > 1) {
                filterQuery = fieldName + ":(";
                for (int j = 0; j < vals.size(); j++) {
                    String val = vals.get(j);
                    filterQuery += val;
                    if (vals.size() > (j + 1)) {
                        if (all) {
                            filterQuery += " AND ";
                        } else {
                            filterQuery += " OR ";
                        }
                    }
                }
                filterQuery += ")";
            }
        }
        return filterQuery;
    }

    /**
     * Creates a OR combined 'q' parameter.<p>
     * 
     * @return returns the 'q' parameter
     */
    private String createTextQuery() {

        if (m_textSearchFields.isEmpty()) {
            m_textSearchFields.add(I_CmsSearchField.FIELD_TEXT);
        }
        String q = "{!q.op=OR qf=";
        boolean first = true;
        for (String textField : m_textSearchFields) {
            if (!first) {
                q += " ";
            }
            q += textField;
        }
        q += "}" + m_text;
        return q;
    }

    /**
     * Ensures that at least the 'path' and the 'type' are part of the fields returned field list.<p>
     * 
     * @see CommonParams#FL
     */
    private void ensureReturnFields() {

        String[] fl = m_solrQuery.getParams(CommonParams.FL);
        if ((fl != null) && (fl.length > 0)) {
            List<String> result = new ArrayList<String>();
            for (String field : fl) {
                String commasep = field.replaceAll(" ", ",");
                List<String> list = CmsStringUtil.splitAsList(commasep, ',');
                if (!list.contains("*")) {
                    for (String reqField : CmsStringUtil.splitAsList(MINIMUM_FIELDS, ",")) {
                        if (!list.contains(reqField)) {
                            list.add(reqField);
                        }
                    }
                }
                result.addAll(list);
            }
            m_solrQuery.setParam(CommonParams.FL, CmsStringUtil.arrayAsString(result.toArray(new String[0]), ","));
        } else {
            m_solrQuery.setParam(CommonParams.FL, MINIMUM_FIELDS);
        }
    }

    /**
     * Removes those filter queries from the Solr filter queries that restrict the same field.<p>
     * 
     * @param fqs the filter queries to remove
     */
    private void replaceFilterQueries(String[] fqs) {

        // iterate over the given filter queries to remove
        for (String fq : fqs) {
            int idx = fq.indexOf(':');
            if (idx != -1) {
                // get the field name of the fq to remove
                String fieldName = fq.substring(0, idx);
                // iterate over the fqs of the already existing fqs from the solr query
                if (m_solrQuery.getFilterQueries() != null) {
                    for (String sfq : m_solrQuery.getFilterQueries()) {
                        if (sfq.startsWith(fieldName + ":")) {
                            // there exists a filter query for exact the same field
                            // remove it
                            m_solrQuery.removeFilterQuery(sfq);
                        }
                    }
                }
            }
        }
        m_solrQuery.addFilterQuery(fqs);
    }

}

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
import org.opencms.search.fields.I_CmsSearchField;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;

/**
 * A Solr query that holds the OpenCms specific search query parameters.<p>
 * 
 * @since 8.5.0
 */
public class CmsSolrQuery extends SolrQuery {

    /** The default query. */
    public static final String DEFAULT_QUERY = "*:*";

    /** The default facet date gab. */
    public static final String FACET_DATE_GAB = "+1DAY";

    /** The query type. */
    public static final String QUERY_TYPE = "dismax";

    /** The default search result count. */
    public static final int RESULT_COUNT = 100;

    /** A constant to add the score field to the result documents. */
    public static final String SCORE_FIELD = "*,score";

    /** The serial version UID. */
    private static final long serialVersionUID = -3775730785338262128L;

    /** The list of categories to search for. */
    private String[] m_categories;

    /** A map of date ranges (key = field name, value = Pair<start, end>). */
    private Map<String, CmsPair<Date, Date>> m_dateRanges;

    /** The facet date gab to use for date facets. */
    private String m_facetDateGab;

    /** The filter queries that are currently used for this query. */
    private Map<String, String> m_filterQueries = new HashMap<String, String>();

    /** The list of locales to search in. */
    private String[] m_locales;

    /** The list of resource types to search for. */
    private String[] m_resourceTypes;

    /** VFS paths that should be considered for searching. */
    private String[] m_searchRoots;

    /** The of sort fields (key = field name, value = sort order), if empty the Solr default is taken (by score). */
    private Map<String, ORDER> m_sortFieldsMap;

    /** The name of the field to search the text in. */
    private List<String> m_textFields = new ArrayList<String>();

    /** The text search. */
    private String[] m_texts;

    /**
     * Creates a empty Solr query that only has the minimum query parameters set to perform a OpenCms search query.<p>
     */
    public CmsSolrQuery() {

        this(null, null, null, null, null);
    }

    /**
     * Creates a query.<p>
     * 
     * @param cms the current CmsObject
     */
    public CmsSolrQuery(CmsObject cms) {

        this(cms, null, null, null, null);
    }

    /**
     * Creates a {@link CmsSolrQuery} based on a request parameter map as argument.<p>
     * 
     * @param cms the cms
     * @param params the request parameter map
     * 
     */
    public CmsSolrQuery(CmsObject cms, Map<String, String[]> params) {

        this(cms, null, params, null, null);
    }

    /**
     * Creates a new {@link CmsSolrQuery} and sets the Solr query string.<p>
     * 
     * @param cms the CMS object
     * @param q the Solr query string {@link CommonParams#Q}
     */
    public CmsSolrQuery(CmsObject cms, String q) {

        this(cms, q, null, null, null);
    }

    /**
     * Creates a new {@link CmsSolrQuery} and initializes its members with the defaults.<p>
     * 
     * <b>Please note</b>: at least the one of the parameters query or parameters must not be <code>null</code>
     * 
     * The used defaults are:
     * <ul>
     * <li>{@link #DEFAULT_QUERY}
     * <li>{@link #QUERY_TYPE}
     * <li>{@link #SCORE_FIELD}
     * <li>{@link #RESULT_COUNT}
     * <li>{@link #FACET_DATE_GAB}
     * <li>{@link I_CmsSearchField#FIELD_PREFIX_TEXT}<code>_&lt;DEFAULT_LOCALE&gt;</code>
     * </ul>
     * 
     * @param cms the cms to use, can be <code>null</code>
     * @param query the Solr query string {@link CommonParams#Q}
     * @param parameters the request parameter map, can contain any Solr query parameter
     * @param locales the locales that should be used for the search, can be <code>null</code>
     * @param searchRoots the search roots that should be used
     */
    public CmsSolrQuery(
        CmsObject cms,
        String query,
        Map<String, String[]> parameters,
        List<Locale> locales,
        List<String> searchRoots) {

        // set the defaults
        setQueryType(QUERY_TYPE);
        addField(SCORE_FIELD);
        setRows(new Integer(CmsSolrQuery.RESULT_COUNT));
        setFacetDateGab(FACET_DATE_GAB);
        addTextFields("text");

        // set the query
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(query)) {
            setQuery(query);
        } else {
            setQuery(DEFAULT_QUERY);
        }

        // set the locales to search for
        List<Locale> localesToUse = new ArrayList<Locale>();
        if ((locales == null) && (cms != null)) {
            localesToUse.add(cms.getRequestContext().getLocale());
        }
        setLocales(localesToUse);

        // set the search roots
        // set the locales to search for
        List<String> searchRootsToUse = new ArrayList<String>();
        if ((searchRoots == null) && (cms != null)) {
            searchRootsToUse.add(cms.getRequestContext().getSiteRoot() + "/");
        }
        setSearchRoots(searchRootsToUse.toArray(new String[0]));

        // set the given parameters
        if ((parameters != null) && !parameters.isEmpty()) {
            addParameterMap(parameters);
        }

        // ensure the query returns at least the 'path' and the type 'field'
        ensureReturnFields();
    }

    /**
     * Creates a {@link CmsSolrQuery} based on a request parameter map as argument.<p>
     * 
     * Use this constructor in order to let OpenCms behave identical to standard {@link HttpSolrServer}
     * 
     * @param params the request parameter map
     */
    public CmsSolrQuery(Map<String, String[]> params) {

        addField(SCORE_FIELD);
        addParameterMap(params);
        ensureReturnFields();
    }

    /**
     * Adds a search root.<p>
     * 
     * @param searchRoot the VFS path that should be added to the search roots
     */
    public void addSearchRoot(String searchRoot) {

        String[] searchRoots = new String[m_searchRoots.length + 1];
        searchRoots[m_searchRoots.length - 1] = searchRoot;
        setSearchRoots(searchRoots);
    }

    /**
     * Adds all the text fields for the given locales to the list text fields that should be searched.<p>
     * 
     * @param locales the locales to set the text fields for
     */
    public void addTextFields(Locale... locales) {

        for (Locale locale : locales) {
            m_textFields.add(I_CmsSearchField.FIELD_PREFIX_TEXT + locale.toString());
        }
    }

    /**
     * Adds the given fields to those to search text in.<p>
     * 
     * @param textsFields the concrete field names to search for texts in
     */
    public void addTextFields(String... textsFields) {

        m_textFields.addAll(Arrays.asList(textsFields));
    }

    /**
     * Returns the categories.<p>
     *
     * @return the categories
     */
    public String[] getCategories() {

        return m_categories;
    }

    /**
     * Returns the date ranges.<p>
     * 
     * @return the date ranges
     */
    public Map<String, CmsPair<Date, Date>> getDateRanges() {

        return m_dateRanges;
    }

    /**
     * Returns the facetDateGab.<p>
     *
     * @return the facetDateGab
     */
    public String getFacetDateGab() {

        return m_facetDateGab;
    }

    /**
     * Returns the locales.<p>
     *
     * @return the locales
     */
    public String[] getLocales() {

        return m_locales;
    }

    /**
     * Returns the resource types.<p>
     *
     * @return the resource types
     */
    public String[] getResourceTypes() {

        return m_resourceTypes;
    }

    /**
     * Returns the search roots.<p>
     *
     * @return the search roots
     */
    public String[] getSearchRoots() {

        return m_searchRoots;
    }

    /**
     * Returns the sortFields.<p>
     *
     * @return the sortFields
     */
    public Map<String, ORDER> getSortFieldsMap() {

        return m_sortFieldsMap;
    }

    /**
     * Returns the text fields to search in.<p>
     *
     * @return the text fields to search in
     */
    public List<String> getTextFields() {

        return m_textFields;
    }

    /**
     * Returns the search texts.<p>
     * 
     * @return the search texts
     */
    public String[] getTexts() {

        return m_texts;
    }

    /**
     * Sets the categories.<p>
     *
     * @param categories the categories to set
     */
    public void setCategories(String... categories) {

        addFilterQuery(I_CmsSearchField.FIELD_CATEGORY, categories);
        m_categories = categories;
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
    public void setDateRanges(Map<String, CmsPair<Date, Date>> ranges) {

        // remove the date ranges
        if (m_dateRanges != null) {
            for (Map.Entry<String, CmsPair<Date, Date>> entry : m_dateRanges.entrySet()) {
                removeFacetField(entry.getKey());
            }
        }

        // add the date ranges
        if (ranges != null) {
            for (Map.Entry<String, CmsPair<Date, Date>> entry : ranges.entrySet()) {
                addDateRangeFacet(
                    entry.getKey(),
                    entry.getValue().getFirst(),
                    entry.getValue().getSecond(),
                    m_facetDateGab);
            }
        }

        m_dateRanges = ranges;
    }

    /**
     * Sets the facetDateGab.<p>
     *
     * @param facetDateGab the facetDateGab to set
     */
    public void setFacetDateGab(String facetDateGab) {

        m_facetDateGab = facetDateGab;
    }

    /**
     * Sets the locales.<p>
     *
     * @param locales the locales to set
     */
    public void setLocales(List<Locale> locales) {

        m_textFields.clear();
        if ((locales == null) || locales.isEmpty()) {
            m_textFields.add("text");
            if ((m_locales != null) && (m_locales.length > 0)) {
                removeFilterQuery(I_CmsSearchField.FIELD_CONTENT_LOCALES);
            }
            m_locales = null;
        } else {
            List<String> localeStrings = new ArrayList<String>();
            for (Locale locale : locales) {
                localeStrings.add(locale.toString());
                m_textFields.add("text_" + locale);
            }
            String[] asArray = localeStrings.toArray(new String[0]);
            addFilterQuery(I_CmsSearchField.FIELD_CONTENT_LOCALES, asArray);
            m_locales = asArray;
        }
    }

    /**
     * Sets the resource types.<p>
     *
     * @param resourceTypes the resource types to set
     */
    public void setResourceTypes(String... resourceTypes) {

        addFilterQuery(I_CmsSearchField.FIELD_TYPE, resourceTypes);
        m_resourceTypes = resourceTypes;
    }

    /**
     * Sets the search roots.<p>
     *
     * @param searchRoots the VFS paths to search in
     */
    public void setSearchRoots(String... searchRoots) {

        if ((searchRoots != null) && (searchRoots.length > 0)) {
            addFilterQuery(I_CmsSearchField.FIELD_PARENT_FOLDERS, searchRoots);
        }
        m_searchRoots = searchRoots;
    }

    /**
     * Sets the fields that should be used to sort the results.<p>
     *
     * @param sortFields the sort fields to set
     */
    public void setSortFieldsMap(Map<String, ORDER> sortFields) {

        m_sortFieldsMap = sortFields;

        // add the sort fields to the query
        if (m_sortFieldsMap != null) {
            for (Map.Entry<String, ORDER> entry : m_sortFieldsMap.entrySet()) {
                addSortField(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Sets the search text phrases to search for.<p>
     * 
     * @param texts the texts
     */
    public void setTexts(String... texts) {

        m_texts = texts;
        String vals = CmsStringUtil.arrayAsString(texts, " ");
        String query = "{!q.op=OR ";
        for (String textField : m_textFields) {
            query += "qf=" + textField + " ";
        }
        query += "}" + vals;
        setQuery(query);
    }

    /**
     * Adds a filter query.<p>
     * 
     * @param fieldName the field name
     * @param all <code>true</code> to combine the given values with 'AND', <code>false</code> for 'OR'
     * @param vals the values
     */
    private void addFilterQuery(String fieldName, boolean all, String... vals) {

        if (m_filterQueries.get(fieldName) != null) {
            removeFilterQuery(m_filterQueries.get(fieldName));
            m_filterQueries.remove(fieldName);
        }
        String query = createFilterQuery(fieldName, all, vals);
        m_filterQueries.put(fieldName, query);
        super.addFilterQuery(query);
    }

    /**
     * Adds a filter query.<p>
     * 
     * Calls {@link #addFilterQuery(String, boolean, String...)} with false
     * 
     * @see #addFilterQuery(String, boolean, String...)
     * 
     * @param fieldName the field name
     * @param vals the values
     */
    private void addFilterQuery(String fieldName, String... vals) {

        addFilterQuery(fieldName, false, vals);
    }

    /**
     * Adds the given parameters to the Solr query and ensures that at least the 'path' and the 'type'
     * is part of the returned fields.<p>
     * 
     * @param params the parameters
     */
    private void addParameterMap(Map<String, String[]> params) {

        add(new ModifiableSolrParams(params));
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
    private String createFilterQuery(String fieldName, boolean all, String... vals) {

        String filterQuery = null;
        if ((vals != null)) {
            if (vals.length == 1) {
                filterQuery = fieldName + ":" + vals[0];
            } else if (vals.length > 1) {
                filterQuery = fieldName + ":(";
                for (int j = 0; j < vals.length; j++) {
                    String val = vals[j];
                    filterQuery += val;
                    if (vals.length > (j + 1)) {
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
     * Ensures that at least the 'path' and the 'type' are part of the fields returned field list.<p>
     * 
     * @see CommonParams#FL
     */
    private void ensureReturnFields() {

        String[] fl = getParams(CommonParams.FL);
        if ((fl != null) && (fl.length > 0)) {
            List<String> result = new ArrayList<String>();
            for (String field : fl) {
                String commasep = field.replaceAll(" ", ",");
                List<String> list = CmsStringUtil.splitAsList(commasep, ',');
                if (!list.contains("*")) {
                    if (!list.contains(I_CmsSearchField.FIELD_PATH)) {
                        list.add(I_CmsSearchField.FIELD_PATH);
                    }
                    if (!list.contains(I_CmsSearchField.FIELD_TYPE)) {
                        list.add(I_CmsSearchField.FIELD_TYPE);
                    }
                }
                result.addAll(list);
            }
            setParam(CommonParams.FL, CmsStringUtil.arrayAsString(result.toArray(new String[0]), ","));
        }
    }
}

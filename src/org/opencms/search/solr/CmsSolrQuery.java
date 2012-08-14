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
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.search.CmsSearchException;
import org.opencms.search.fields.I_CmsSearchField;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
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
    private String m_textField;

    /** The text search. */
    private String[] m_texts;

    /**
     * Creates a new {@link CmsSolrQuery} and initializes its members with the defaults.<p>
     * 
     * The defaults are:
     * <ul>
     * <li>{@link #DEFAULT_QUERY}
     * <li>{@link #QUERY_TYPE}
     * <li>{@link #SCORE_FIELD}
     * <li>{@link #RESULT_COUNT}
     * <li>{@link #FACET_DATE_GAB}
     * <li>{@link I_CmsSearchField#FIELD_PREFIX_TEXT}<code>_&lt;DEFAULT_LOCALE&gt;</code>
     * </ul>
     */
    public CmsSolrQuery() {

        setQuery(DEFAULT_QUERY);
        setQueryType(QUERY_TYPE);
        addField(SCORE_FIELD);
        setRows(new Integer(CmsSolrQuery.RESULT_COUNT));
        setFacetDateGab(FACET_DATE_GAB);
        m_textField = I_CmsSearchField.FIELD_PREFIX_TEXT + CmsLocaleManager.getDefaultLocale().toString();
    }

    /**
     * Creates a new {@link CmsSolrQuery} and initializes the site root.<p>
     * 
     * @param cms the CMS object
     * 
     * @see #CmsSolrQuery(Locale)
     */
    public CmsSolrQuery(CmsObject cms) {

        this(cms.getRequestContext().getLocale());
        setSearchRoots(cms.getRequestContext().getSiteRoot() + "/");
    }

    /**
     * Creates a {@link CmsSolrQuery} based on a request parameter map as argument.<p>
     * 
     * @param cms the CMS object
     * @param params the request parameter map
     * 
     * @throws CmsSearchException if search params missing
     * 
     * @see #CmsSolrQuery(Locale)
     */
    public CmsSolrQuery(CmsObject cms, Map<String, String[]> params)
    throws CmsSearchException {

        this();
        Map<String, String[]> newParams = new HashMap<String, String[]>();
        if ((params == null) || params.isEmpty()) {
            // if there are no parameters given
            throw new CmsSearchException(Messages.get().container(
                Messages.ERR_SEARCH_INVALID_SEARCH_1,
                Messages.get().container(Messages.ERR_SEARCH_INVALID_SEARCH_NO_PARAMS_0)));
        }

        // several parameters are given take them as they are
        newParams = params;
        if (newParams.containsKey(CommonParams.FL)) {
            // it exists a 'fl' parameter (return fields for a document)
            // in order to perform the permission check we must have the
            // path and the type in the resulting documents otherwise the
            // permission check will not work, so add them as parameter.
            String[] fl = newParams.get(CommonParams.FL);
            String commasep = fl[0].replaceAll(" ", ",");
            List<String> list = CmsStringUtil.splitAsList(commasep, ',');
            if (!list.contains(I_CmsSearchField.FIELD_PATH)) {
                list.add(I_CmsSearchField.FIELD_PATH);
            }
            if (!list.contains(I_CmsSearchField.FIELD_TYPE)) {
                list.add(I_CmsSearchField.FIELD_TYPE);
            }
            newParams.put(CommonParams.FL, new String[] {CmsStringUtil.listAsString(list, ",")});
        }

        ModifiableSolrParams p = new ModifiableSolrParams(newParams);
        add(p);
        Locale locale = cms.getRequestContext().getLocale();
        // setLocales(locale.toString());
        m_textField = I_CmsSearchField.FIELD_PREFIX_TEXT + locale.toString();

    }

    /**
     * Creates a new {@link CmsSolrQuery} and sets the Solr query string.<p>
     * 
     * @param cms the CMS object
     * @param q the Solr query string {@link CommonParams#Q}
     * 
     * @see #CmsSolrQuery(Locale)
     */
    public CmsSolrQuery(CmsObject cms, String q) {

        this(cms.getRequestContext().getLocale());
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(q)) {
            setQuery(q);
        }
    }

    /**
     * Creates a new {@link CmsSolrQuery} and initializes the query with the given locale.<p>
     * 
     * @param locale the locale to use for initialization
     * 
     * @see #CmsSolrQuery()
     */
    public CmsSolrQuery(Locale locale) {

        this();
        setLocales(locale.toString());
        m_textField = I_CmsSearchField.FIELD_PREFIX_TEXT + locale.toString();
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
     * Returns the textField.<p>
     *
     * @return the textField
     */
    public String getTextField() {

        return m_textField;
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
    public void setLocales(String... locales) {

        addFilterQuery(I_CmsSearchField.FIELD_RESOURCE_LOCALES, locales);
        m_locales = locales;
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

        addFilterQuery(I_CmsSearchField.FIELD_PARENT_FOLDERS, searchRoots);
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

        addFilterQuery(m_textField, true, texts);
        m_texts = texts;
    }

    /**
     * Adds a filter query.<p>
     * 
     * @param fieldName the field name
     * @param all <code>true</code> to combins the given values with 
     * logical 'AND', <code>false</code> for 'OR' combination
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
}

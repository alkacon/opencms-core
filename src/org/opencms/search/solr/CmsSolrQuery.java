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
import org.opencms.main.OpenCms;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CommonParams;

/**
 * A Solr search query.<p>
 */
public class CmsSolrQuery extends SolrQuery {

    /** A constant to add the score field to the result documents. */
    public static final String ALL_RETURN_FIELDS = "*,score";

    /** The default facet date gap. */
    public static final String DEFAULT_FACET_DATE_GAP = "+1DAY";

    /** The default query. */
    public static final String DEFAULT_QUERY = "*:*";

    /** The query type. */
    public static final String DEFAULT_QUERY_TYPE = "edismax";

    /** The default search result count. */
    public static final Integer DEFAULT_ROWS = new Integer(10);

    /** A constant to add the score field to the result documents. */
    public static final String MINIMUM_FIELDS = CmsSearchField.FIELD_PATH
        + ","
        + CmsSearchField.FIELD_TYPE
        + ","
        + CmsSearchField.FIELD_ID;

    /** A constant to add the score field to the result documents. */
    public static final String STRUCTURE_FIELDS = CmsSearchField.FIELD_PATH
        + ","
        + CmsSearchField.FIELD_TYPE
        + ","
        + CmsSearchField.FIELD_ID
        + ","
        + CmsSearchField.FIELD_CATEGORY
        + ","
        + CmsSearchField.FIELD_DATE_CONTENT
        + ","
        + CmsSearchField.FIELD_DATE_CREATED
        + ","
        + CmsSearchField.FIELD_DATE_EXPIRED
        + ","
        + CmsSearchField.FIELD_DATE_LASTMODIFIED
        + ","
        + CmsSearchField.FIELD_DATE_RELEASED
        + ","
        + CmsSearchField.FIELD_SUFFIX
        + ","
        + CmsSearchField.FIELD_DEPENDENCY_TYPE
        + ","
        + CmsSearchField.FIELD_DESCRIPTION
        + ","
        + CmsPropertyDefinition.PROPERTY_TITLE
        + CmsSearchField.FIELD_DYNAMIC_PROPERTIES
        + ","
        + CmsSearchField.FIELD_RESOURCE_LOCALES
        + ","
        + CmsSearchField.FIELD_CONTENT_LOCALES
        + ","
        + CmsSearchField.FIELD_SCORE
        + ","
        + CmsSearchField.FIELD_PARENT_FOLDERS;

    /** The serial version UID. */
    private static final long serialVersionUID = -2387357736597627703L;

    /** The facet date gap to use for date facets. */
    private String m_facetDateGap = DEFAULT_FACET_DATE_GAP;

    /** The parameters given by the 'query string'.  */
    private Map<String, String[]> m_queryParameters = new HashMap<String, String[]>();

    /** The name of the field to search the text in. */
    private List<String> m_textSearchFields = new ArrayList<String>();

    /**
     * Default constructor.<p>
     */
    public CmsSolrQuery() {

        this(null, null);
    }

    /**
     * Public constructor.<p>
     * 
     * @param cms the current OpenCms context
     * @param queryParams the Solr query parameters
     */
    public CmsSolrQuery(CmsObject cms, Map<String, String[]> queryParams) {

        setQuery(DEFAULT_QUERY);
        setFields(ALL_RETURN_FIELDS);
        setRequestHandler(DEFAULT_QUERY_TYPE);
        setRows(DEFAULT_ROWS);

        // set the values from the request context
        if (cms != null) {
            setLocales(Collections.singletonList(cms.getRequestContext().getLocale()));
            setSearchRoots(Collections.singletonList(cms.getRequestContext().getSiteRoot() + "/"));
        }
        if (queryParams != null) {
            m_queryParameters = queryParams;
        }
        ensureParameters();
        ensureReturnFields();
    }

    /**
     * Returns the resource type if only one is set as filter query.<p>
     * 
     * @param fqs the field queries to check
     * 
     * @return the type or <code>null</code>
     */
    public static String getResourceType(String[] fqs) {

        String ret = null;
        int count = 0;
        if (fqs != null) {
            for (String fq : fqs) {
                if (fq.startsWith(CmsSearchField.FIELD_TYPE + ":")) {
                    String val = fq.substring((CmsSearchField.FIELD_TYPE + ":").length());
                    val = val.replaceAll("\"", "");
                    if (OpenCms.getResourceManager().hasResourceType(val)) {
                        count++;
                        ret = val;
                    }
                }
            }
        }
        return (count == 1) ? ret : null;
    }

    /**
     * Adds a filter query.<p>
     * 
     * @param fieldName the field name
     * @param all <code>true</code> to combine the given values with 'AND', <code>false</code> for 'OR'
     * @param vals the values
     */
    public void addFilterQuery(String fieldName, boolean all, List<String> vals) {

        if (getFilterQueries() != null) {
            for (String fq : getFilterQueries()) {
                if (fq.startsWith(fieldName + ":")) {
                    removeFilterQuery(fq);
                }
            }
        }
        addFilterQuery(createFilterQuery(fieldName, all, vals));
    }

    /**
     * Adds the given fields/orders to the existing sort fields.<p>
     *
     * @param sortFields the sortFields to set
     */
    public void addSortFieldOrders(Map<String, ORDER> sortFields) {

        if ((sortFields != null) && !sortFields.isEmpty()) {
            // add the sort fields to the query
            for (Map.Entry<String, ORDER> entry : sortFields.entrySet()) {
                addSort(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Sets the categories only if not set in the query parameters.<p>
     *
     * @param categories the categories to set
     */
    public void setCategories(List<String> categories) {

        if ((categories != null) && !categories.isEmpty()) {
            addFilterQuery(CmsSearchField.FIELD_CATEGORY + "_exact", true, categories);
        }
    }

    /**
     * Sets the categories only if not set in the query parameters.<p>
     *
     * @param categories the categories to set
     */
    public void setCategories(String... categories) {

        setCategories(Arrays.asList(categories));
    }

    /**
     * Sets date ranges.<p>
     * 
     * This call will overwrite all existing date ranges for the given keys (name of the date facet field).<p>
     * 
     * The parameter Map uses as:<p>
     * <ul>
     * <li><code>keys: </code>Solr field name {@link org.opencms.search.fields.CmsSearchField} and
     * <li><code>values: </code> pairs with min date as first and max date as second {@link org.opencms.util.CmsPair}
     * </ul>
     * Alternatively you can use Solr standard query syntax like:<p>
     * <ul>
     * <li><code>+created:[* TO NOW]</code>
     * <li><code>+lastmodified:[' + date + ' TO NOW]</code>
     * </ul>
     * whereby date is Solr formated:
     * {@link org.opencms.search.solr.CmsSolrDocument#DF}
     * <p>
     * 
     * @param dateRanges the ranges map with field name as key and a CmsPair with min date as first and max date as second
     */
    public void setDateRanges(Map<String, CmsPair<Date, Date>> dateRanges) {

        if ((dateRanges != null) && !dateRanges.isEmpty()) {
            // remove the date ranges
            for (Map.Entry<String, CmsPair<Date, Date>> entry : dateRanges.entrySet()) {
                removeFacetField(entry.getKey());
            }
            // add the date ranges
            for (Map.Entry<String, CmsPair<Date, Date>> entry : dateRanges.entrySet()) {
                addDateRangeFacet(
                    entry.getKey(),
                    entry.getValue().getFirst(),
                    entry.getValue().getSecond(),
                    m_facetDateGap);
            }
        }
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
     * Sets the locales only if not set in the query parameters.<p>
     *
     * @param locales the locales to set
     */
    public void setLocales(List<Locale> locales) {

        m_textSearchFields = new ArrayList<String>();
        if ((locales == null) || locales.isEmpty()) {
            m_textSearchFields.add(CmsSearchField.FIELD_TEXT);
            if (getFilterQueries() != null) {
                for (String fq : getFilterQueries()) {
                    if (fq.startsWith(CmsSearchField.FIELD_CONTENT_LOCALES + ":")) {
                        removeFilterQuery(fq);
                    }
                }
            }
        } else {
            List<String> localeStrings = new ArrayList<String>();
            for (Locale locale : locales) {
                localeStrings.add(locale.toString());
                m_textSearchFields.add("text_" + locale);
            }
            addFilterQuery(CmsSearchField.FIELD_CONTENT_LOCALES, false, localeStrings);
        }
    }

    /**
     * Sets the locales only if not set in the query parameters.<p>
     *
     * @param locales the locales to set
     */
    public void setLocales(Locale... locales) {

        setLocales(Arrays.asList(locales));
    }

    /**
     * Sets the resource types only if not set in the query parameters.<p>
     *
     * @param resourceTypes the resourceTypes to set
     */
    public void setResourceTypes(List<String> resourceTypes) {

        if ((resourceTypes != null) && !resourceTypes.isEmpty()) {
            addFilterQuery(CmsSearchField.FIELD_TYPE, false, resourceTypes);
        }
    }

    /**
     * Sets the resource types only if not set in the query parameters.<p>
     *
     * @param resourceTypes the resourceTypes to set
     */
    public void setResourceTypes(String... resourceTypes) {

        setResourceTypes(Arrays.asList(resourceTypes));
    }

    /**
     * Sets the search roots only if not set as query parameter.<p>
     *
     * @param searchRoots the searchRoots to set
     */
    public void setSearchRoots(List<String> searchRoots) {

        if ((searchRoots != null) && !searchRoots.isEmpty()) {
            addFilterQuery(CmsSearchField.FIELD_PARENT_FOLDERS, false, searchRoots);
        }
    }

    /**
     * Sets the search roots only if not set as query parameter.<p>
     *
     * @param searchRoots the searchRoots to set
     */
    public void setSearchRoots(String... searchRoots) {

        setSearchRoots(Arrays.asList(searchRoots));
    }

    /**
     * Sets the return fields 'fl' to a predefined set that does not contain content specific fields.<p>
     *
     * @param structureQuery the <code>true</code> to return only structural fields
     */
    public void setStructureQuery(boolean structureQuery) {

        if (structureQuery) {
            setFields(STRUCTURE_FIELDS);
        }
    }

    /**
     * Sets the text.<p>
     *
     * @param text the text to set
     */
    public void setText(String text) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(text)) {
            setQuery(createTextQuery(text));
        }
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
     * Sets the textSearchFields.<p>
     *
     * @param textSearchFields the textSearchFields to set
     */
    public void setTextSearchFields(String... textSearchFields) {

        setTextSearchFields(Arrays.asList(textSearchFields));
    }

    /**
     * @see org.apache.solr.common.params.ModifiableSolrParams#toString()
     */
    @Override
    public String toString() {

        return CmsEncoder.decode(super.toString());
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    protected CmsSolrQuery clone() {

        return new CmsSolrQuery(null, CmsRequestUtil.createParameterMap(toString()));
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
                filterQuery = fieldName + ":\"" + vals.get(0) + "\"";
            } else if (vals.size() > 1) {
                filterQuery = fieldName + ":(";
                for (int j = 0; j < vals.size(); j++) {
                    String val = "\"" + vals.get(j) + "\"";
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
     * @param text 
     * 
     * @return returns the 'q' parameter
     */
    private String createTextQuery(String text) {

        if (m_textSearchFields.isEmpty()) {
            m_textSearchFields.add(CmsSearchField.FIELD_TEXT);
        }
        String q = "{!q.op=OR qf=";
        boolean first = true;
        for (String textField : m_textSearchFields) {
            if (!first) {
                q += " ";
            }
            q += textField;
        }
        q += "}" + text;
        return q;
    }

    /**
     * Ensures that the parameters will overwrite the member values.<p> 
     */
    private void ensureParameters() {

        // overwrite already set values with values from query String
        if ((m_queryParameters != null) && !m_queryParameters.isEmpty()) {
            for (Map.Entry<String, String[]> entry : m_queryParameters.entrySet()) {
                if (!entry.getKey().equals(CommonParams.FQ)) {
                    // add or replace all parameters from the query String
                    setParam(entry.getKey(), entry.getValue());
                } else {
                    // special handling for filter queries
                    replaceFilterQueries(entry.getValue());
                }
            }
        }
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
                    for (String reqField : CmsStringUtil.splitAsList(MINIMUM_FIELDS, ",")) {
                        if (!list.contains(reqField)) {
                            list.add(reqField);
                        }
                    }
                }
                result.addAll(list);
            }
            setParam(CommonParams.FL, CmsStringUtil.arrayAsString(result.toArray(new String[0]), ","));
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
                if (getFilterQueries() != null) {
                    for (String sfq : getFilterQueries()) {
                        if (sfq.startsWith(fieldName + ":")) {
                            // there exists a filter query for exact the same field
                            // remove it
                            removeFilterQuery(sfq);
                        }
                    }
                }
            }
        }
        addFilterQuery(fqs);
    }
}

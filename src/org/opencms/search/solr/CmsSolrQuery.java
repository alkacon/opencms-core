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
    public static final Integer DEFAULT_ROWS = Integer.valueOf(10);

    /** A constant to add the score field to the result documents. */
    public static final String MINIMUM_FIELDS = CmsSearchField.FIELD_PATH
        + ","
        + CmsSearchField.FIELD_TYPE
        + ","
        + CmsSearchField.FIELD_SOLR_ID
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

    /** Ignore expiration flag. */
    private boolean m_ignoreExpiration;

    /** The parameters given by the 'query string'.  */
    private Map<String, String[]> m_queryParameters = new HashMap<String, String[]>();

    /** The search words. */
    private String m_text;

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
        ensureExpiration();
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
     * Creates and adds a filter query.<p>
     *
     * @param fieldName the field name to create a filter query on
     * @param vals the values that should match for the given field
     * @param all <code>true</code> to combine the given values with 'AND', <code>false</code> for 'OR'
     * @param useQuotes <code>true</code> to surround the given values with double quotes, <code>false</code> otherwise
     */
    public void addFilterQuery(String fieldName, List<String> vals, boolean all, boolean useQuotes) {

        if (getFilterQueries() != null) {
            for (String fq : getFilterQueries()) {
                if (fq.startsWith(fieldName + ":")) {
                    removeFilterQuery(fq);
                }
            }
        }
        addFilterQuery(createFilterQuery(fieldName, vals, all, useQuotes));
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
     * @see java.lang.Object#clone()
     */
    @Override
    public CmsSolrQuery clone() {

        CmsSolrQuery sq = new CmsSolrQuery(null, CmsRequestUtil.createParameterMap(toString(), true, null));
        if (m_ignoreExpiration) {
            sq.removeExpiration();
        }
        return sq;
    }

    /**
     * Ensures that the initial request parameters will overwrite the member values.<p>
     *
     * You can initialize the query with an HTTP request parameter then make some method calls
     * and finally re-ensure that the initial request parameters will overwrite the changes
     * made in the meanwhile.<p>
     */
    public void ensureParameters() {

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
     * Removes the expiration flag.
     */
    public void removeExpiration() {

        if (getFilterQueries() != null) {
            for (String fq : getFilterQueries()) {
                if (fq.startsWith(CmsSearchField.FIELD_DATE_EXPIRED + ":")
                    || fq.startsWith(CmsSearchField.FIELD_DATE_RELEASED + ":")) {
                    removeFilterQuery(fq);
                }
            }
        }
        m_ignoreExpiration = true;
    }

    /**
     * Sets the categories only if not set in the query parameters.<p>
     *
     * @param categories the categories to set
     */
    public void setCategories(List<String> categories) {

        if ((categories != null) && !categories.isEmpty()) {
            addFilterQuery(CmsSearchField.FIELD_CATEGORY + CmsSearchField.FIELD_DYNAMIC_EXACT, categories, true, true);
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
     * whereby date is Solr formatted:
     * {@link org.opencms.search.CmsSearchUtil#getDateAsIso8601(Date)}
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
     * Sets the Geo filter query if not exists.
     * @param fieldName the field name storing the coordinates
     * @param coordinates the coordinates string as a lat,lng pair
     * @param radius the radius
     * @param units the units of the search radius
     */
    public void setGeoFilterQuery(String fieldName, String coordinates, String radius, String units) {

        String geoFilterQuery = CmsSolrQueryUtil.composeGeoFilterQuery(fieldName, coordinates, radius, units);
        if (!Arrays.asList(getFilterQueries()).contains(geoFilterQuery)) {
            addFilterQuery(geoFilterQuery);
        }
    }

    /**
     * Sets the highlightFields.<p>
     *
     * @param highlightFields the highlightFields to set
     */
    public void setHighlightFields(List<String> highlightFields) {

        setParam("hl.fl", CmsStringUtil.listAsString(highlightFields, ","));
    }

    /**
     * Sets the highlightFields.<p>
     *
     * @param highlightFields the highlightFields to set
     */
    public void setHighlightFields(String... highlightFields) {

        setParam("hl.fl", CmsStringUtil.arrayAsString(highlightFields, ","));
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
                if (!m_textSearchFields.contains("text")
                    && !OpenCms.getLocaleManager().getAvailableLocales().contains(locale)) {
                    // if the locale is not configured in the opencms-system.xml
                    // there will no localized text fields, so take the general one
                    m_textSearchFields.add("text");
                } else {
                    m_textSearchFields.add("text_" + locale);
                }
            }
            addFilterQuery(CmsSearchField.FIELD_CONTENT_LOCALES, localeStrings, false, false);
        }
        if (m_text != null) {
            setText(m_text);
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
     * @see org.apache.solr.client.solrj.SolrQuery#setRequestHandler(java.lang.String)
     */
    @Override
    public SolrQuery setRequestHandler(String qt) {

        SolrQuery q = super.setRequestHandler(qt);
        if (m_text != null) {
            setText(m_text);
        }
        return q;
    }

    /**
     * Sets the resource types only if not set in the query parameters.<p>
     *
     * @param resourceTypes the resourceTypes to set
     */
    public void setResourceTypes(List<String> resourceTypes) {

        if ((resourceTypes != null) && !resourceTypes.isEmpty()) {
            addFilterQuery(CmsSearchField.FIELD_TYPE, resourceTypes, false, false);
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
     * Sets the requested return fields, but ensures that at least the 'path' and the 'type', 'id' and 'solr_id'
     * are part of the fields returned field list.<p>
     *
     * @param returnFields the really requested return fields.
     *
     * @see CommonParams#FL
     */
    public void setReturnFields(String returnFields) {

        ensureReturnFields(new String[] {returnFields});
    }

    /**
     * Sets the search roots only if not set as query parameter.<p>
     *
     * @param searchRoots the searchRoots to set
     */
    public void setSearchRoots(List<String> searchRoots) {

        if ((searchRoots != null) && !searchRoots.isEmpty()) {
            addFilterQuery(CmsSearchField.FIELD_PARENT_FOLDERS, searchRoots, false, true);
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

        m_text = text;
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
        if (m_text != null) {
            setText(m_text);
        }
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
     * Creates a filter query on the given field name.<p>
     *
     * Creates and adds a filter query.<p>
     *
     * @param fieldName the field name to create a filter query on
     * @param vals the values that should match for the given field
     * @param all <code>true</code> to combine the given values with 'AND', <code>false</code> for 'OR'
     * @param useQuotes <code>true</code> to surround the given values with double quotes, <code>false</code> otherwise
     *
     * @return a filter query String e.g. <code>fq=fieldname:val1</code>
     */
    private String createFilterQuery(String fieldName, List<String> vals, boolean all, boolean useQuotes) {

        String filterQuery = null;
        if ((vals != null)) {
            if (vals.size() == 1) {
                if (useQuotes) {
                    filterQuery = fieldName + ":" + "\"" + vals.get(0) + "\"";
                } else {
                    filterQuery = fieldName + ":" + vals.get(0);
                }
            } else if (vals.size() > 1) {
                filterQuery = fieldName + ":(";
                for (int j = 0; j < vals.size(); j++) {
                    String val;
                    if (useQuotes) {
                        val = "\"" + vals.get(j) + "\"";
                    } else {
                        val = vals.get(j);
                    }
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
     * @param text the query string.
     *
     * @return returns the 'q' parameter
     */
    private String createTextQuery(String text) {

        if (m_textSearchFields.isEmpty()) {
            m_textSearchFields.add(CmsSearchField.FIELD_TEXT);
        }
        String q = "{!q.op=OR type=" + getRequestHandler() + " qf=";
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
     * Ensures that expired and not yet released resources are not returned by default.<p>
     */
    private void ensureExpiration() {

        boolean expirationDateSet = false;
        boolean releaseDateSet = false;
        if (getFilterQueries() != null) {
            for (String fq : getFilterQueries()) {
                if (fq.startsWith(CmsSearchField.FIELD_DATE_EXPIRED + ":")) {
                    expirationDateSet = true;
                }
                if (fq.startsWith(CmsSearchField.FIELD_DATE_RELEASED + ":")) {
                    releaseDateSet = true;
                }
            }
        }
        if (!expirationDateSet) {
            addFilterQuery(CmsSearchField.FIELD_DATE_EXPIRED + ":[NOW TO *]");
        }
        if (!releaseDateSet) {
            addFilterQuery(CmsSearchField.FIELD_DATE_RELEASED + ":[* TO NOW]");
        }
    }

    /**
     * Ensures that at least the 'path' and the 'type', 'id' and 'solr_id' are part of the fields returned field list.<p>
     *
     * @see CommonParams#FL
     */
    private void ensureReturnFields() {

        ensureReturnFields(getParams(CommonParams.FL));
    }

    /**
     * Ensures that at least the 'path' and the 'type', 'id' and 'solr_id' are part of the fields returned field list.<p>
     *
     * @param requestedReturnFields the really requested return fields.
     *
     * @see CommonParams#FL
     */
    private void ensureReturnFields(String[] requestedReturnFields) {

        if ((requestedReturnFields != null) && (requestedReturnFields.length > 0)) {
            List<String> result = new ArrayList<String>();
            for (String field : requestedReturnFields) {
                List<String> list = CmsStringUtil.splitAsList(field, ',');
                list.forEach(e -> e.trim());
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
     * Removes those filter queries that restrict the fields used in the given filter query Strings.<p>
     *
     * Searches in the given Strings for a ":", then takes the field name part
     * and removes the already set filter queries queries that are matching the same field name.<p>
     *
     * @param fqs the filter query Strings in the format <code>fq=fieldname:value</code> that should be removed
     */
    private void removeFilterQueries(String[] fqs) {

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
                            // there exists a filter query for exact the same field,  remove it
                            removeFilterQuery(sfq);
                        }
                    }
                }
            }
        }
    }

    /**
     * Removes the given filter queries, if already set and then adds the filter queries again.<p>
     *
     * @param fqs the filter queries to remove
     */
    private void replaceFilterQueries(String[] fqs) {

        removeFilterQueries(fqs);
        addFilterQuery(fqs);
    }
}

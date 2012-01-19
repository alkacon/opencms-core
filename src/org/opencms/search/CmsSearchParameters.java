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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

/**
 * Contains the search parameters for a call to <code>{@link org.opencms.search.CmsSearchIndex#search(org.opencms.file.CmsObject, CmsSearchParameters)}</code>.<p>
 * 
 * Primary purpose is translation of search arguments to response parameters and from request parameters as 
 * well as support for creation of restrictions of several search query parameter sets. <p>
 * 
 * @since 6.0.0 
 */
public class CmsSearchParameters {

    /**
     * Describes a specific search field query.<p>
     */
    public static class CmsSearchFieldQuery {

        /** The field name. */
        private String m_fieldName;

        /** The occur parameter for this field. */
        private Occur m_fieldOccur;

        /** The search term list. */
        private List<String> m_searchTerms;

        /** The occur parameter used for the search term combination. */
        private Occur m_termOccur;

        /**
         * Creates a new search field query with a variable length search term list.<p>
         * 
         * @param fieldName the field name
         * @param fieldOccur the occur parameter for this field
         * @param termList the search term list
         * @param termOccur the occur parameter used for the search term combination
         */
        public CmsSearchFieldQuery(String fieldName, Occur fieldOccur, List<String> termList, Occur termOccur) {

            super();
            m_fieldName = fieldName;
            m_fieldOccur = fieldOccur;
            m_searchTerms = termList;
            m_termOccur = termOccur;
        }

        /**
         * Creates a new search field query with just a single search term.<p>
         * 
         * Please note: Since there is only one term, the ocucr parameter for the term combination is
         * not required and set to <code>null</code>.<p>
         * 
         * @param fieldName the field name
         * @param searchTerm the search term
         * @param fieldOccur the occur parameter for this field
         */
        public CmsSearchFieldQuery(String fieldName, String searchTerm, Occur fieldOccur) {

            this(fieldName, fieldOccur, Arrays.asList(searchTerm), null);
        }

        /**
         * Returns the field name.<p>
         *
         * @return the field name
         */
        public String getFieldName() {

            return m_fieldName;
        }

        /**
         * Returns the occur parameter for this field query.<p>
         *
         * @return the occur parameter for this field query
         */
        public Occur getOccur() {

            return m_fieldOccur;
        }

        /**
         * Returns the first entry from the term list.<p>
         *
         * @return the search query
         * 
         * @deprecated use {@link #getSearchTerms()} instead
         */
        @Deprecated
        public String getSearchQuery() {

            return m_searchTerms.get(0);
        }

        /**
         * Returns the search term list.<p>
         *
         * @return the search term list
         */
        public List<String> getSearchTerms() {

            return m_searchTerms;
        }

        /**
         * Returns the occur parameter used for the search term combination of this field query.<p>
         *
         * @return the occur parameter used for the search term combination of this field query
         */
        public Occur getTermOccur() {

            return m_termOccur;
        }

        /**
         * Sets the name of the field to use this query for.<p>
         *
         * @param fieldName the name of the field to use this query for
         */
        public void setFieldName(String fieldName) {

            m_fieldName = fieldName;
        }

        /**
         * Sets the occur parameter for this field query.<p>
         *
         * @param occur the occur parameter to set
         */
        public void setOccur(BooleanClause.Occur occur) {

            m_fieldOccur = occur;
        }

        /**
         * Sets the search keywords to just a single entry.<p>
         *
         * @param searchQuery the single search keyword to set
         * 
         * @deprecated use {@link #setSearchTerms(List)} instead
         */
        @Deprecated
        public void setSearchQuery(String searchQuery) {

            setSearchTerms(Arrays.asList(searchQuery));
        }

        /**
         * Sets the search terms.<p>
         * 
         * @param searchTerms the search terms to set
         */
        public void setSearchTerms(List<String> searchTerms) {

            m_searchTerms = searchTerms;
        }
    }

    /** Sort result documents by date of creation, then score. */
    public static final Sort SORT_DATE_CREATED = new Sort(new SortField(
        CmsSearchField.FIELD_DATE_CREATED,
        SortField.STRING,
        true));

    /** Sort result documents by date of last modification, then score. */
    public static final Sort SORT_DATE_LASTMODIFIED = new Sort(new SortField(
        CmsSearchField.FIELD_DATE_LASTMODIFIED,
        SortField.STRING,
        true));

    /** Default sort order (by document score). */
    public static final Sort SORT_DEFAULT = Sort.RELEVANCE;

    /** Names of the default sort options. */
    public static final String[] SORT_NAMES = {
        "SORT_DEFAULT",
        "SORT_DATE_CREATED",
        "SORT_DATE_LASTMODIFIED",
        "SORT_TITLE"};

    /** Sort result documents by title, then score. */
    public static final Sort SORT_TITLE = new Sort(new SortField[] {
        new SortField(CmsSearchField.FIELD_TITLE, SortField.STRING),
        SortField.FIELD_SCORE});

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSearchParameters.class);

    /** The number of displayed pages returned by getPageLinks(). */
    protected int m_displayPages;

    /** The number of matches per page. */
    protected int m_matchesPerPage;

    /** If <code>true</code>, the category count is calculated for all search results. */
    private boolean m_calculateCategories;

    /** The list of categories to limit the search to. */
    private List<String> m_categories;

    /** Indicates if all fields should be used for generating the excerpt, regardless if they have been searched or not. */
    private boolean m_excerptOnlySearchedFields;

    /** The map of individual search field queries. */
    private List<CmsSearchFieldQuery> m_fieldQueries;

    /** The list of search index fields to search in. */
    private List<String> m_fields;

    /** The index to search. */
    private CmsSearchIndex m_index;

    /** Indicates if the query part should be ignored so that only filters are used for searching. */
    private boolean m_isIgnoreQuery;

    /** The creation date the resources have to have as maximum. */
    private long m_maxDateCreated;

    /** The last modification date the resources have to have as maximum. */
    private long m_maxDateLastModified;

    /** The creation date the resources have to have as minimum. */
    private long m_minDateCreated;

    /** The last modification date the resources have to have as minimum. */
    private long m_minDateLastModified;

    /** The current result page. */
    private int m_page;

    /** The pre-parsed query. */
    private String m_parsedQuery;

    /** The search query to use. */
    private String m_query;

    /** The minimum length of the search query. */
    private int m_queryLength;

    /** The list of resource types to limit the search to. */
    private List<String> m_resourceTypes;

    /** Only resource that are sub-resource of one of the search roots are included in the search result. */
    private List<String> m_roots;

    /** The sort order for the search. */
    private Sort m_sort;

    /**
     * Creates a new search parameter instance with no search query and 
     * default values for the remaining parameters. <p>
     * 
     * Before using this search parameters for a search method 
     * <code>{@link #setQuery(String)}</code> has to be invoked. <p>
     * 
     */
    public CmsSearchParameters() {

        this("");
    }

    /**
     * Creates a new search parameter instance with the provided search query and 
     * default values for the remaining parameters. <p>
     * 
     * Only the "meta" field (combination of content and title) will be used for search. 
     * No search root restriction is chosen. 
     * No category restriction is used. 
     * No category counts are calculated for the result. 
     * Sorting is turned off. This is a simple but fast setup. <p>
     * 
     * @param query the query to search for 
     */
    public CmsSearchParameters(String query) {

        this(query, null, null, null, null, false, null);

    }

    /**
     * Creates a new search parameter instance with the provided parameter values.<p>
     * 
     * @param query the search term to search the index
     * @param fields the list of fields to search
     * @param roots only resource that are sub-resource of one of the search roots are included in the search result
     * @param categories the list of categories to limit the search to
     * @param resourceTypes the list of resource types to limit the search to
     * @param calculateCategories if <code>true</code>, the category count is calculated for all search results
     *      (use with caution, this option uses much performance)
     * @param sort the sort order for the search
     */
    public CmsSearchParameters(
        String query,
        List<String> fields,
        List<String> roots,
        List<String> categories,
        List<String> resourceTypes,
        boolean calculateCategories,
        Sort sort) {

        super();
        m_query = (query == null) ? "" : query;
        if (fields == null) {
            fields = new ArrayList<String>(2);
            fields.add(CmsSearchIndex.DOC_META_FIELDS[0]);
            fields.add(CmsSearchIndex.DOC_META_FIELDS[1]);
        }
        m_fields = fields;
        if (roots == null) {
            roots = new ArrayList<String>(2);
        }
        m_roots = roots;
        m_categories = (categories == null) ? new ArrayList<String>() : categories;
        m_resourceTypes = (resourceTypes == null) ? new ArrayList<String>() : resourceTypes;
        m_calculateCategories = calculateCategories;
        // null sort is allowed default
        m_sort = sort;
        m_page = 1;
        m_queryLength = -1;
        m_matchesPerPage = 10;
        m_displayPages = 10;
        m_isIgnoreQuery = false;

        m_minDateCreated = Long.MIN_VALUE;
        m_maxDateCreated = Long.MAX_VALUE;
        m_minDateLastModified = Long.MIN_VALUE;
        m_maxDateLastModified = Long.MAX_VALUE;
    }

    /**
     * Adds an individual query for a search field.<p>
     * 
     * If this is used, any setting made with {@link #setQuery(String)} and {@link #setFields(List)}
     * will be ignored and only the individual field search settings will be used.<p>
     *
     * @param query the query to add
     * 
     * @since 7.5.1
     */
    public void addFieldQuery(CmsSearchFieldQuery query) {

        if (m_fieldQueries == null) {
            m_fieldQueries = new ArrayList<CmsSearchFieldQuery>();
            m_fields = new ArrayList<String>();
        }
        m_fieldQueries.add(query);
        // add the used field used in the fields query to the list of fields used in the search
        if (!m_fields.contains(query.getFieldName())) {
            m_fields.add(query.getFieldName());
        }
    }

    /**
     * Adds an individual query for a search field.<p>
     * 
     * If this is used, any setting made with {@link #setQuery(String)} and {@link #setFields(List)}
     * will be ignored and only the individual field search settings will be used.<p>
     * 
     * @param fieldName the field name
     * @param searchQuery the search query
     * @param occur the occur parameter for the query in the field
     * 
     * @since 7.5.1
     */
    public void addFieldQuery(String fieldName, String searchQuery, Occur occur) {

        CmsSearchFieldQuery newQuery = new CmsSearchFieldQuery(fieldName, searchQuery, occur);
        addFieldQuery(newQuery);
    }

    /**
     * Returns whether category counts are calculated for search results or not. <p>
     * 
     * @return a boolean that tells whether category counts are calculated for search results or not
     */
    public boolean getCalculateCategories() {

        return m_calculateCategories;
    }

    /**
     * Returns the list of categories to limit the search to.<p>
     *
     * @return the list of categories to limit the search to
     */
    public List<String> getCategories() {

        return m_categories;
    }

    /**
     * Returns the maximum number of pages which should be shown.<p> 
     * 
     * @return the maximum number of pages which should be shown
     */
    public int getDisplayPages() {

        return m_displayPages;
    }

    /**
     * Returns the list of individual field queries.<p>
     * 
     * @return the list of individual field queries
     *
     * @since 7.5.1
     */
    public List<CmsSearchFieldQuery> getFieldQueries() {

        return m_fieldQueries;
    }

    /**
     * Returns the list of search index field names (Strings) to search in.<p>
     *
     * @return the list of search index field names (Strings) to search in
     */
    public List<String> getFields() {

        return m_fields;
    }

    /**
     * Get the name of the index for the search.<p>
     * 
     * @return the name of the index for the search
     */
    public String getIndex() {

        return m_index.getName();
    }

    /**
     * Gets the number of matches displayed on each page.<p>
     * 
     * @return matches per result page
     */
    public int getMatchesPerPage() {

        return m_matchesPerPage;
    }

    /**
     * Returns the maximum creation date a resource must have to be included in the search result.<p>
     *
     * @return the maximum creation date a resource must have to be included in the search result
     */
    public long getMaxDateCreated() {

        return m_maxDateCreated;
    }

    /**
     * Returns the maximum last modification date a resource must have to be included in the search result.<p>
     *
     * @return the maximum last modification date a resource must have to be included in the search result
     */
    public long getMaxDateLastModified() {

        return m_maxDateLastModified;
    }

    /**
     * Returns the minimum creation date a resource must have to be included in the search result.<p>
     *
     * @return the minimum creation date a resource must have to be included in the search result
     */
    public long getMinDateCreated() {

        return m_minDateCreated;
    }

    /**
     * Returns the minimum last modification date a resource must have to be included in the search result.<p>
     *
     * @return the minimum last modification date a resource must have to be included in the search result
     */
    public long getMinDateLastModified() {

        return m_minDateLastModified;
    }

    /**
     * Returns the parsed query.<p>
     * 
     * The parsed query is automatically set by the OpenCms search index when a query is created 
     * with either {@link #setQuery(String)} or {@link #addFieldQuery(CmsSearchFieldQuery)}.
     * The Lucene query build from the parameters is stored here and can be later used 
     * for paging through the results.<p>
     * 
     * Please note that this returns only to the query part, not the filter part of the search.<p>
     * 
     * @return the parsed query 
     */
    public String getParsedQuery() {

        return m_parsedQuery;
    }

    /**
     * Returns the search query to use.<p>
     *
     * @return the search query to use
     */
    public String getQuery() {

        return m_query;
    }

    /**
     * Gets the minimum search query length.<p>
     * 
     * @return the minimum search query length
     */
    public int getQueryLength() {

        return m_queryLength;
    }

    /**
     * Returns the list of resource types to limit the search to.<p>
     *
     * @return the list of resource types to limit the search to
     *
     * @since 7.5.1
     */
    public List<String> getResourceTypes() {

        return m_resourceTypes;
    }

    /**
     * Returns the list of strings of search roots to use.<p>
     * 
     * Only resource that are sub-resource of one of the search roots are included in the search result.<p>
     * 
     * @return the list of strings of search roots to use
     */
    public List<String> getRoots() {

        return m_roots;
    }

    /**
     * Returns the list of categories to limit the search to.<p>
     *
     * @return the list of categories to limit the search to
     */
    public String getSearchCategories() {

        return toSeparatedString(getCategories(), ',');
    }

    /**
     * Returns the search index to search in or null if not set before 
     * (<code>{@link #setSearchIndex(CmsSearchIndex)}</code>). <p>
     * 
     * @return the search index to search in or null if not set before (<code>{@link #setSearchIndex(CmsSearchIndex)}</code>)
     */
    public CmsSearchIndex getSearchIndex() {

        return m_index;
    }

    /**
     * Returns the search page to display.<p>
     *  
     * @return the search page to display
     */
    public int getSearchPage() {

        return m_page;
    }

    /**
     * Returns the comma separated lists of root folder names to restrict search to.<p>
     * 
     * This method is a "sibling" to method <code>{@link #getRoots()}</code> but with 
     * the support of being usable with widget technology. <p>
     * 
     * @return the comma separated lists of field names to search in
     * 
     * @see #setSortName(String)
     */
    public String getSearchRoots() {

        return toSeparatedString(m_roots, ',');
    }

    /**
     * Returns the instance that defines the sort order for the results. 
     * 
     * @return the instance that defines the sort order for the results
     */
    public Sort getSort() {

        return m_sort;
    }

    /**
     * Returns the name of the sort option being used.<p>
     * @return the name of the sort option being used
     * 
     * @see #SORT_NAMES
     * @see #setSortName(String)
     */
    public String getSortName() {

        if (m_sort == SORT_DATE_CREATED) {
            return SORT_NAMES[1];
        }
        if (m_sort == SORT_DATE_LASTMODIFIED) {
            return SORT_NAMES[2];
        }
        if (m_sort == SORT_TITLE) {
            return SORT_NAMES[3];
        }
        return SORT_NAMES[0];
    }

    /**
     * Returns <code>true</code> if the category count is calculated for all search results.<p>
     *
     * @return <code>true</code> if the category count is calculated for all search results
     */
    public boolean isCalculateCategories() {

        return m_calculateCategories;
    }

    /**
     * Returns <code>true</code> if fields configured for the excerpt should be used for generating the excerpt only 
     * if they have been actually searched in.<p>
     *
     * The default setting is <code>false</code>, which means all text fields configured for the excerpt will
     * be used to generate the excerpt, regardless if they have been searched in or not.<p>
     *
     * Please note: A field will only be included in the excerpt if it has been configured as <code>excerpt="true"</code>
     * in <code>opencms-search.xml</code>. This method controls if so configured fields are used depending on the
     * fields searched, see {@link #setFields(List)}.<p>
     *
     * @return <code>true</code> if fields configured for the excerpt should be used for generating the excerpt only 
     * if they have been actually searched in
     */
    public boolean isExcerptOnlySearchedFields() {

        return m_excerptOnlySearchedFields;
    }

    /**
     * Returns <code>true</code> if the query part should be ignored so that only filters are used for searching.<p>
     *
     * @return <code>true</code> if the query part should be ignored so that only filters are used for searching
     * 
     * @since 8.0.0
     */
    public boolean isIgnoreQuery() {

        return m_isIgnoreQuery;
    }

    /**
     * Creates a merged parameter set from this parameters, restricted by the given other parameters.<p>
     * 
     * This is mainly intended for "search in search result" functions.<p>
     * 
     * The restricted query is build of the queries of both parameters, appended with AND.<p>
     * 
     * The lists in the restriction for <code>{@link #getFields()}</code>, <code>{@link #getRoots()}</code> and
     * <code>{@link #getCategories()}</code> are <b>intersected</b> with the lists of this search parameters. Only
     * elements contained in both lists are included for the created search parameters. 
     * If a list in either the restriction or in this search parameters is <code>null</code>, 
     * the list from the other search parameters is used directly.<p> 
     * 
     * The values for
     * <code>{@link #isCalculateCategories()}</code>
     * and <code>{@link #getSort()}</code> of this parameters are used for the restricted parameters.<p>
     * 
     * @param restriction the parameters to restrict this parameters with
     * @return the restricted parameters
     */
    @SuppressWarnings("unchecked")
    public CmsSearchParameters restrict(CmsSearchParameters restriction) {

        // append queries
        StringBuffer query = new StringBuffer(256);
        if (getQuery() != null) {
            // don't blow up unnecessary closures (if CmsSearch is reused and restricted several times)
            boolean closure = !getQuery().startsWith("+(");
            if (closure) {
                query.append("+(");
            }
            query.append(getQuery());
            if (closure) {
                query.append(")");
            }
        }
        if (restriction.getQuery() != null) {
            // don't let Lucene max terms be exceeded in case someone reuses a CmsSearch and continuously restricts 
            // query with the same restrictions...
            if (query.indexOf(restriction.getQuery()) < 0) {
                query.append(" +(");
                query.append(restriction.getQuery());
                query.append(")");
            }
        }

        // restrict fields
        List<String> fields = null;
        if ((m_fields != null) && (m_fields.size() > 0)) {
            if ((restriction.getFields() != null) && (restriction.getFields().size() > 0)) {
                fields = ListUtils.intersection(m_fields, restriction.getFields());
            } else {
                fields = m_fields;
            }
        } else {
            fields = restriction.getFields();
        }

        // restrict roots
        List<String> roots = null;
        if ((m_roots != null) && (m_roots.size() > 0)) {
            if ((restriction.getRoots() != null) && (restriction.getRoots().size() > 0)) {
                roots = ListUtils.intersection(m_roots, restriction.getRoots());
                // TODO: This only works if there are equal paths in both parameter sets - for two distinct sets 
                //       all root restrictions are dropped with an empty list. 
            } else {
                roots = m_roots;
            }
        } else {
            roots = restriction.getRoots();
        }

        // restrict categories
        List<String> categories = null;
        if ((m_categories != null) && (m_categories.size() > 0)) {
            if ((restriction.getCategories() != null) && (restriction.getCategories().size() > 0)) {
                categories = ListUtils.intersection(m_categories, restriction.getCategories());
            } else {
                categories = m_categories;
            }
        } else {
            categories = restriction.getCategories();
        }

        // restrict resource types
        List<String> resourceTypes = null;
        if ((m_resourceTypes != null) && (m_resourceTypes.size() > 0)) {
            if ((restriction.getResourceTypes() != null) && (restriction.getResourceTypes().size() > 0)) {
                resourceTypes = ListUtils.intersection(m_resourceTypes, restriction.getResourceTypes());
            } else {
                resourceTypes = m_resourceTypes;
            }
        } else {
            resourceTypes = restriction.getResourceTypes();
        }

        // create the new search parameters 
        CmsSearchParameters result = new CmsSearchParameters(
            query.toString(),
            fields,
            roots,
            categories,
            resourceTypes,
            m_calculateCategories,
            m_sort);
        result.setIndex(getIndex());
        return result;
    }

    /**
     * Set whether category counts shall be calculated for the corresponding search results or not.<p> 
     * 
     * @param flag true if category counts shall be calculated for the corresponding search results or false if not
     */
    public void setCalculateCategories(boolean flag) {

        m_calculateCategories = flag;
    }

    /**
     * Set the list of categories (strings) to this parameters. <p> 
     * 
     * @param categories the list of categories (strings) of this parameters
     */
    public void setCategories(List<String> categories) {

        m_categories = categories;
    }

    /**
     * Sets the maximum number of pages which should be shown.<p>
     * 
     * Enter an odd value to achieve a nice, "symmetric" output.<p> 
     * 
     * @param value the maximum number of pages which should be shown
     */
    public void setDisplayPages(int value) {

        m_displayPages = value;
    }

    /**
     * Controls if the excerpt from a field is generated only for searched fields, or for all fields (the default).<p>
     *
     * @param excerptOnlySearchedFields if <code>true</code>, the excerpt is generated only from the fields actually searched in
     * 
     * @see #isExcerptOnlySearchedFields()
     */
    public void setExcerptOnlySearchedFields(boolean excerptOnlySearchedFields) {

        m_excerptOnlySearchedFields = excerptOnlySearchedFields;
    }

    /**
     * Sets the list of strings of names of fields to search in. <p>
     * 
     * @param fields the list of strings of names of fields to search in to set
     */
    public void setFields(List<String> fields) {

        m_fields = fields;
    }

    /**
     * Sets the flag to indicate if the query part should be ignored so that only filters are used for searching.<p>
     *
     * @param isIgnoreQuery the flag to indicate if the query part should be ignored
     * 
     * @since 8.0.0
     */
    public void setIgnoreQuery(boolean isIgnoreQuery) {

        m_isIgnoreQuery = isIgnoreQuery;
    }

    /**
     * Set the name of the index to search.<p>
     * 
     * 
     * @param indexName the name of the index
     */
    public void setIndex(String indexName) {

        CmsSearchIndex index;
        if (CmsStringUtil.isNotEmpty(indexName)) {
            try {
                index = OpenCms.getSearchManager().getIndex(indexName);
                if (index == null) {
                    throw new CmsException(Messages.get().container(Messages.ERR_INDEX_NOT_FOUND_1, indexName));
                }
                setSearchIndex(index);
            } catch (Exception exc) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.LOG_INDEX_ACCESS_FAILED_1, indexName), exc);
                }
            }
        }
    }

    /**
     * Sets the number of matches per page.<p>
     * 
     * @param matches the number of matches per page
     */
    public void setMatchesPerPage(int matches) {

        m_matchesPerPage = matches;
    }

    /**
     * Sets the maximum creation date a resource must have to be included in the search result.<p>
     *
     * @param maxDateCreated the maximum creation date to set
     */
    public void setMaxDateCreated(long maxDateCreated) {

        m_maxDateCreated = maxDateCreated;
    }

    /**
     * Sets the maximum last modification date a resource must have to be included in the search result.<p>
     *
     * @param maxDateLastModified the maximum last modification date to set
     */
    public void setMaxDateLastModified(long maxDateLastModified) {

        m_maxDateLastModified = maxDateLastModified;
    }

    /**
     * Sets the minimum creation date a resource must have to be included in the search result.<p>
     *
     * @param minDateCreated the minimum creation date to set
     */
    public void setMinDateCreated(long minDateCreated) {

        m_minDateCreated = minDateCreated;
    }

    /**
     * Sets the minimum last modification date a resource must have to be included in the search result.<p>
     *
     * @param minDateLastModified he minimum last modification date to set
     */
    public void setMinDateLastModified(long minDateLastModified) {

        m_minDateLastModified = minDateLastModified;
    }

    /**
     * Sets the parsed query.<p>
     * 
     * The parsed query is automatically set by the OpenCms search index when a query is created 
     * with either {@link #setQuery(String)} or {@link #addFieldQuery(CmsSearchFieldQuery)}.
     * The Lucene query build from the parameters is stored here and can be later used 
     * for paging through the results.<p>
     * 
     * Please note that this applies only to the query part, not the filter part of the search.<p>
     * 
     * @param parsedQuery the parsed query to set 
     */
    public void setParsedQuery(String parsedQuery) {

        m_parsedQuery = parsedQuery;
    }

    /**
     * Sets the query to search for. <p> 
     * 
     * The decoding here is tailored for query strings that are 
     * additionally manually UTF-8 encoded at client side (javascript) to get around an 
     * issue with special chars in applications that use non- UTF-8 encoding 
     * (e.g. ISO-8859-1) OpenCms applications. It is not recommended to use this with 
     * front ends that don't encode manually as characters like sole "%" (without number suffix) 
     * will cause an Exception.<p> 
     * 
     * @param query the query to search for to set
     */
    public void setQuery(String query) {

        // for use with widgets the exception is thrown here to enforce the error message next to the widget
        if (query.trim().length() < getQueryLength()) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_QUERY_TOO_SHORT_1,
                new Integer(getQueryLength())));
        }
        m_query = query;
    }

    /**
     * Sets the minimum length of the search query.<p>
     * 
     * @param length the minimum search query length
     */
    public void setQueryLength(int length) {

        m_queryLength = length;
    }

    /**
     * Set the list of resource types (strings) to limit the search to. <p> 
     * 
     * @param resourceTypes the list of resource types (strings) to limit the search to
     *
     * @since 7.5.1
     */
    public void setResourceTypes(List<String> resourceTypes) {

        m_resourceTypes = resourceTypes;
    }

    /**
     * Sets the list of strings of roots to search under for the search.<p>
     * 
     * @param roots  the list of strings of roots to search under for the search to set
     */
    public void setRoots(List<String> roots) {

        m_roots = roots;
    }

    /**
     * Set the comma separated search root names to  restrict search to.<p>
     * 
     * @param categories the comma separated category names to  restrict search to
     */
    public void setSearchCategories(String categories) {

        setCategories(CmsStringUtil.splitAsList(categories, ','));
    }

    /**
     * Sets the search index to use for the search. <p>
     * 
     * @param index the search index to use for the search to set.
     * 
     * @throws CmsIllegalArgumentException if null is given as argument 
     */
    public void setSearchIndex(CmsSearchIndex index) throws CmsIllegalArgumentException {

        if (index == null) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_INDEX_NULL_0));
        }
        m_index = index;
    }

    /**
     * Set the search page to display. <p>
     * 
     * @param page the search page to display
     */
    public void setSearchPage(int page) {

        m_page = page;
    }

    /**
     * Set the comma separated search root names to  restrict search to.<p>
     * 
     * @param rootNameList the comma separated search root names to  restrict search to
     */
    public void setSearchRoots(String rootNameList) {

        m_roots = CmsStringUtil.splitAsList(rootNameList, ',');
    }

    /**
     * Set the instance that defines the sort order for search results. 
     * 
     * @param sortOrder the instance that defines the sort order for search results to set
     */
    public void setSort(Sort sortOrder) {

        m_sort = sortOrder;
    }

    /** 
     * Sets the internal member of type <code>{@link Sort}</code> according to 
     * the given sort name. <p>
     * 
     * For a list of valid sort names, please see <code>{@link #SORT_NAMES}</code>.<p>
     * 
     * @param sortName the name of the sort to use
     * 
     * @see #SORT_NAMES
     */
    public void setSortName(String sortName) {

        if (sortName.equals(SORT_NAMES[1])) {
            m_sort = SORT_DATE_CREATED;
        } else if (sortName.equals(SORT_NAMES[2])) {
            m_sort = SORT_DATE_LASTMODIFIED;
        } else if (sortName.equals(SORT_NAMES[3])) {
            m_sort = SORT_TITLE;
        } else {
            m_sort = SORT_DEFAULT;
        }
    }

    /**
     * Creates a query String build from this search parameters for HTML links.<p>
     * 
     * @return a query String build from this search parameters for HTML links
     */
    public String toQueryString() {

        StringBuffer result = new StringBuffer(128);
        result.append("?action=search");
        if (getParsedQuery() != null) {
            result.append("&parsedQuery=");
            result.append(CmsEncoder.encodeParameter(getParsedQuery()));
        } else {
            result.append("&query=");
            result.append(CmsEncoder.encodeParameter(getQuery()));
        }

        result.append("&matchesPerPage=");
        result.append(getMatchesPerPage());
        result.append("&displayPages=");
        result.append(getDisplayPages());
        result.append("&index=");
        result.append(CmsEncoder.encodeParameter(getIndex()));

        Sort sort = getSort();
        if (sort != CmsSearchParameters.SORT_DEFAULT) {
            result.append("&sort=");
            if (sort == CmsSearchParameters.SORT_TITLE) {
                result.append("title");
            } else if (sort == CmsSearchParameters.SORT_DATE_CREATED) {
                result.append("date-created");
            } else if (sort == CmsSearchParameters.SORT_DATE_LASTMODIFIED) {
                result.append("date-lastmodified");
            }
        }

        if ((getCategories() != null) && (getCategories().size() > 0)) {
            result.append("&category=");
            Iterator<String> it = getCategories().iterator();
            while (it.hasNext()) {
                result.append(it.next());
                if (it.hasNext()) {
                    result.append(',');
                }
            }
        }

        if (getMinDateCreated() > Long.MIN_VALUE) {
            result.append("&minDateCreated=");
            result.append(getMinDateCreated());
        }
        if (getMinDateLastModified() > Long.MIN_VALUE) {
            result.append("&minDateLastModified=");
            result.append(getMinDateLastModified());
        }
        if (getMaxDateCreated() < Long.MAX_VALUE) {
            result.append("&maxDateCreated=");
            result.append(getMaxDateCreated());
        }
        if (getMaxDateLastModified() < Long.MAX_VALUE) {
            result.append("&maxDateLastModified=");
            result.append(getMaxDateLastModified());
        }

        if ((getRoots() != null) && (getRoots().size() > 0)) {
            result.append("&searchRoots=");
            Iterator<String> it = getRoots().iterator();
            while (it.hasNext()) {
                result.append(CmsEncoder.encode(it.next()));
                if (it.hasNext()) {
                    result.append(',');
                }
            }
        }

        if (isExcerptOnlySearchedFields()) {
            result.append("&excerptOnlySearchedFields=true");
        }

        return result.toString();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer result = new StringBuffer();
        result.append("query:[");
        result.append(m_query);
        result.append("] ");
        if ((m_fields != null) && (m_fields.size() > 0)) {
            result.append("fields:[");
            for (int i = 0; i < m_fields.size(); i++) {
                result.append(m_fields.get(i));
                if ((i + 1) < m_fields.size()) {
                    result.append(", ");
                }
            }
            result.append("] ");
        }
        if ((m_roots != null) && (m_roots.size() > 0)) {
            result.append("roots:[");
            for (int i = 0; i < m_roots.size(); i++) {
                result.append(m_roots.get(i));
                if ((i + 1) < m_roots.size()) {
                    result.append(", ");
                }
            }
            result.append("] ");
        }
        if ((m_categories != null) && (m_categories.size() > 0)) {
            result.append("categories:[");
            for (int i = 0; i < m_categories.size(); i++) {
                result.append(m_categories.get(i));
                if ((i + 1) < m_categories.size()) {
                    result.append(", ");
                }
            }
            result.append("] ");
        }
        if ((m_resourceTypes != null) && (m_resourceTypes.size() > 0)) {
            result.append("resourceTypes:[");
            for (int i = 0; i < m_resourceTypes.size(); i++) {
                result.append(m_resourceTypes.get(i));
                if ((i + 1) < m_resourceTypes.size()) {
                    result.append(", ");
                }
            }
            result.append("] ");
        }
        if (m_calculateCategories) {
            result.append("calculate-categories ");
        }
        if (m_excerptOnlySearchedFields) {
            result.append("excerpt-searched-fields-only ");
        }
        result.append("sort:[");
        if (m_sort == CmsSearchParameters.SORT_DEFAULT) {
            result.append("default");
        } else if (m_sort == CmsSearchParameters.SORT_TITLE) {
            result.append("title");
        } else if (m_sort == CmsSearchParameters.SORT_DATE_CREATED) {
            result.append("date-created");
        } else if (m_sort == CmsSearchParameters.SORT_DATE_LASTMODIFIED) {
            result.append("date-lastmodified");
        } else {
            result.append("unknown");
        }
        result.append("]");

        return result.toString();
    }

    /**
     * Concatenates the elements of the string list separated by the given separator character.<p>
     * 
     * @param stringList the list
     * @param separator the separator
     * 
     * @return the concatenated string
     */
    private String toSeparatedString(List<String> stringList, char separator) {

        StringBuffer result = new StringBuffer();
        Iterator<String> it = stringList.iterator();
        while (it.hasNext()) {
            result.append(it.next());
            if (it.hasNext()) {
                result.append(separator);
            }
        }
        return result.toString();
    }
}
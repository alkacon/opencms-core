/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsSearch.java,v $
 * Date   : $Date: 2005/06/27 08:25:56 $
 * Version: $Revision: 1.36 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.documents.I_CmsDocumentFactory;
import org.opencms.util.CmsStringUtil;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

/**
 * Helper class to access the search facility within a jsp.<p>
 * 
 * Typically, the following fields are available for searching:
 * <ul>
 * <li>title - the title of a resource</li>
 * <li>keywords - the keywords of a resource</li>
 * <li>description - the description of a resource</li>
 * <li>content - the aggregated content of a resource</li>
 * <li>created - the creation date of a resource</li>
 * <li>lastmodified - the date of the last modification of a resource</li>
 * <li>path - the path to display the resource</li>
 * <li>channel - the channel of a resource</li>
 * <li>contentdefinition - the name of the content definition class of a resource</li>
 * </ul>
 * 
 * @author Carsten Weinholz 
 * @author Thomas Weckert  
 * 
 * @version $Revision: 1.36 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSearch implements Serializable, Cloneable {

    /** Sort result documents by date of last modification, then score. */
    public static final Sort SORT_DATE_CREATED = new Sort(new SortField[] {
        new SortField(I_CmsDocumentFactory.DOC_DATE_CREATED, true),
        SortField.FIELD_SCORE});

    /** Sort result documents by date of last modification, then score. */
    public static final Sort SORT_DATE_LASTMODIFIED = new Sort(new SortField[] {
        new SortField(I_CmsDocumentFactory.DOC_DATE_LASTMODIFIED, true),
        SortField.FIELD_SCORE});

    /** Default sort order (by document score - for this <code>null</code> gived best performance). */
    public static final Sort SORT_DEFAULT = null;

    /** Sort result documents by title, then score. */
    public static final Sort SORT_TITLE = new Sort(new SortField[] {
        new SortField(I_CmsDocumentFactory.DOC_TITLE_KEY),
        SortField.FIELD_SCORE});

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSearch.class);

    /** Indicates if a category count should be calculated for the result list. */
    protected boolean m_calculateCategories;

    /** Contains the list of categories that the search is limited to. */
    protected String[] m_categories;

    /** The result categories of a search. */
    protected Map m_categoriesFound;

    /** The cms object. */
    protected transient CmsObject m_cms;

    /** The number of displayed pages returned by getPageLinks(). */
    protected int m_displayPages;

    /** The list of fields to search. */
    protected String[] m_fields;

    /** The index to search. */
    protected CmsSearchIndex m_index;

    /** The name of the search index. */
    protected String m_indexName;

    /** The latest exception. */
    protected Exception m_lastException;

    /** The number of matches per page. */
    protected int m_matchesPerPage;

    /** The URL which leads to the next result page. */
    protected String m_nextUrl;

    /** The current result page. */
    protected int m_page;

    /** The number of pages for the result list. */
    protected int m_pageCount;

    /** The restriction for the search parameters, used for "search in seach result". */
    protected CmsSearchParameters m_parameterRestriction;

    /** The search parameters used for searching, build out of the given individual parameter values. */
    protected CmsSearchParameters m_parameters;

    /** The URL which leads to the previous result page. */
    protected String m_prevUrl;

    /** The current query. */
    protected String m_query;

    /** The minimum length of the search query. */
    protected int m_queryLength;

    /** The current search result. */
    protected List m_result;

    /** The search parameter String. */
    protected String m_searchParameters;

    /** The total number of search results matching the query. */
    protected int m_searchResultCount;

    /** Contains the list of search root paths. */
    protected String[] m_searchRoots;

    /** The search sort order. */
    protected Sort m_sortOrder;

    /**
     * Default constructor, used to instanciate the search facility as a bean.<p>
     */
    public CmsSearch() {

        super();

        m_searchRoots = new String[] {""};
        m_page = 1;
        m_searchResultCount = 0;
        m_matchesPerPage = 10;
        m_displayPages = 10;
        m_queryLength = -1;
        m_sortOrder = CmsSearch.SORT_DEFAULT;
        m_fields = CmsSearchIndex.C_DOC_META_FIELDS;
    }

    /**
     * Returns <code>true</code> if a category overview should be shown as part of the result.<p>
     *
     * <b>Please note:</b> The calculation of the category count slows down the search time by an order
     * of magnitude. Make sure that you only use this feature if it's really required! 
     * Be especially careful if your search result list can become large (> 1000 documents), since in this case
     * overall system performance will certainly be impacted considerably when calculating the categories.<p> 
     *
     * @return <code>true</code> if a category overview should be shown as part of the result
     */
    public boolean getCalculateCategories() {

        return m_calculateCategories;
    }

    /**
     * Returns the search categories.<p>
     *
     * @return the search categories
     */
    public String[] getCategories() {

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
     * Gets the current fields list.<p>
     * 
     * @return the fields to search
     */
    public String getFields() {

        if (m_fields == null) {
            return "";
        }
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < m_fields.length; i++) {
            result.append(m_fields[i]);
            result.append(" ");
        }
        return result.toString();
    }

    /**
     * Gets the name of the current search index.<p>
     * 
     * @return the name of the index
     */
    public String getIndex() {

        return m_indexName;
    }

    /**
     * Gets the last exception after a search operation.<p>
     * 
     * @return the exception occured in a search operation or null
     */
    public Exception getLastException() {

        return m_lastException;
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
     * Gets the URL for the link to the next result page.<p>
     * 
     * @return the URL to the next result page
     */
    public String getNextUrl() {

        return m_nextUrl;
    }

    /**
     * Gets the current result page.<p>
     * 
     * @return the current result page
     */
    public int getPage() {

        return m_page;
    }

    /**
     * Creates a sorted map of URLs to link to other search result pages.<p>
     * 
     * The key values are Integers representing the page number, the entry 
     * holds the corresponding link.<p>
     *  
     * @return a map with String URLs
     */
    public Map getPageLinks() {

        Map links = new TreeMap();
        if (m_pageCount <= 1) {
            return links;
        }
        int startIndex, endIndex;
        String link = m_cms.getRequestContext().getUri() + this.getSearchParameters() + "&page=";
        if (getDisplayPages() < 1) {
            // number of displayed pages not limited, build a map with all available page links 
            startIndex = 1;
            endIndex = m_pageCount;
        } else {
            // limited number of displayed pages, calculate page range
            int currentPage = getPage();
            int countBeforeCurrent = getDisplayPages() / 2;
            int countAfterCurrent;
            if ((currentPage - countBeforeCurrent) < 1) {
                // set count before to number of available pages 
                countBeforeCurrent = currentPage - 1;
            }
            // set count after to number of remaining pages (- 1 for current page) 
            countAfterCurrent = getDisplayPages() - countBeforeCurrent - 1;
            // calculate start and end index
            startIndex = currentPage - countBeforeCurrent;
            endIndex = currentPage + countAfterCurrent;
            // check end index
            if (endIndex > m_pageCount) {
                int delta = endIndex - m_pageCount;
                // decrease start index with delta to get the right number of displayed pages
                startIndex -= delta;
                // check start index to avoid values < 1
                if (startIndex < 1) {
                    startIndex = 1;
                }
                endIndex = m_pageCount;
            }
        }

        // build the sorted tree map of page links
        for (int i = startIndex; i <= endIndex; i++) {
            links.put(new Integer(i), (link + i));
        }
        return links;
    }

    /**
     * Returns the search parameters used for searching, build out of the given individual parameter values.<p>
     *
     * @return the search parameters used for searching, build out of the given individual parameter values
     */
    public CmsSearchParameters getParameters() {

        if (m_parameters == null) {
            m_parameters = new CmsSearchParameters(
                m_query,
                m_fields != null ? Arrays.asList(m_fields) : null,
                m_searchRoots != null ? Arrays.asList(m_searchRoots) : null,
                m_categories != null ? Arrays.asList(m_categories) : null,
                m_calculateCategories,
                m_sortOrder);

            if (m_parameterRestriction != null) {
                m_parameters = m_parameters.restrict(m_parameterRestriction);
            }
        }

        return m_parameters;
    }

    /**
     * Gets the URL for the link to the previous result page.<p>
     * 
     * @return the URL to the previous result page
     */
    public String getPreviousUrl() {

        return m_prevUrl;
    }

    /**
     * Gets the current search query.<p>
     * 
     * @return the current query string or null if no query was set before
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
     * Creates a String with the necessary search parameters for page links.<p>
     * 
     * @return String with search parameters
     */
    public String getSearchParameters() {

        // if (m_searchParameters == null) {
        StringBuffer params = new StringBuffer(128);
        params.append("?action=search&query=");
        String query = CmsStringUtil.substitute(m_query, "+", "%2B");
        query = CmsStringUtil.substitute(query, "-", "%2D");
        params.append(CmsEncoder.encode(query, OpenCms.getSystemInfo().getDefaultEncoding()));
        params.append("&matchesPerPage=");
        params.append(this.getMatchesPerPage());
        params.append("&displayPages=");
        params.append(this.getDisplayPages());
        params.append("&index=");
        params.append(CmsEncoder.encode(m_indexName));

        if (m_sortOrder != SORT_DEFAULT) {
            params.append("&sort=");
            // TODO: find a better way to name sort
            if (m_sortOrder == CmsSearch.SORT_TITLE) {
                params.append("title");
            } else if (m_sortOrder == CmsSearch.SORT_DATE_CREATED) {
                params.append("date-created");
            } else if (m_sortOrder == CmsSearch.SORT_DATE_LASTMODIFIED) {
                params.append("date-lastmodified");
            }
        }

        if (m_categories != null) {
            params.append("&category=");
            for (int c = 0; c < m_categories.length; c++) {
                if (c > 0) {
                    params.append(",");
                }
                params.append(m_categories[c]);
            }
        }

        if (m_searchRoots != null) {
            params.append("&searchRoot=");
            for (int c = 0; c < m_searchRoots.length; c++) {
                if (c > 0) {
                    params.append(",");
                }
                params.append(CmsEncoder.encode(m_searchRoots[c]));
            }
        }

        // TODO: Better move this whole method into class "CmsSearchParameters"
        int todo = 0;
        // TODO: handle the multiple search roots (could be easy, just use multiple parameters?)
        // TODO: handle the categories
        // TODO: handle the search fields
        // params.append("&searchRoot=");
        // params.append(CmsEncoder.encode(m_searchRoots[0]));

        return params.toString();
        // cw: cannot store searchParameters any longer since resultRestrictions changes query
        // m_searchParameters = params.toString();
        // return m_searchParameters;
        /* } else {
         return m_searchParameters;
         } */
    }

    /**
     * Returns the search result for the current query, as a list of <code>{@link CmsSearchResult}</code> objects.<p>
     * 
     * @return the search result (may be empty) or null if no index or query was set before
     */
    public List getSearchResult() {

        if (m_cms != null && m_result == null && m_index != null && CmsStringUtil.isNotEmpty(m_query)) {

            if ((this.getQueryLength() > 0) && (m_query.trim().length() < this.getQueryLength())) {

                m_lastException = new CmsSearchException(Messages.get().container(
                    Messages.ERR_QUERY_TOO_SHORT_1,
                    new Integer(this.getQueryLength())));

                return m_result;
            }

            try {

                CmsSearchResultList result = m_index.search(m_cms, getParameters(), m_page, m_matchesPerPage);

                if (result.size() > 0) {

                    m_result = result;
                    m_searchResultCount = result.getHitCount();
                    m_categoriesFound = result.getCategories();

                    // re-caluclate the number of pages for this search result
                    m_pageCount = m_searchResultCount / m_matchesPerPage;
                    if ((m_searchResultCount % m_matchesPerPage) != 0) {
                        m_pageCount++;
                    }

                    // re-calculate the URLs to browse forward and backward in the search result
                    String url = m_cms.getRequestContext().getUri() + getSearchParameters() + "&page=";
                    if (m_page > 1) {
                        m_prevUrl = url + (m_page - 1);
                    }
                    if (m_page < m_pageCount) {
                        m_nextUrl = url + (m_page + 1);
                    }
                } else {
                    m_result = Collections.EMPTY_LIST;
                    m_searchResultCount = 0;
                    m_categoriesFound = null;
                    m_pageCount = 0;
                    m_prevUrl = null;
                    m_nextUrl = null;
                }
            } catch (Exception exc) {

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(Messages.LOG_SEARCHING_FAILED_0), exc);
                }

                m_result = null;
                m_searchResultCount = 0;
                m_pageCount = 0;

                m_lastException = exc;
            }
        }

        return m_result;
    }

    /**
     * Returns a map of categories (Strings) for the last search result, mapped to the hit count (Integer) of 
     * the documents in this category, or <code>null</code> if the categories have not been calculated.<p> 
     *
     * @return a map of categories for the last search result
     * 
     * @see CmsSearch#getCalculateCategories()
     * @see CmsSearch#setCalculateCategories(boolean)
     */
    public Map getSearchResultCategories() {

        return m_categoriesFound;
    }

    /**
     * Returns the total number of search results matching the query.<p>
     * 
     * @return the total number of search results matching the query
     */
    public int getSearchResultCount() {

        return m_searchResultCount;
    }

    /**
     * Returns the search roots.<p>
     * 
     * Only resources that are sub-resources of one of the search roots
     * are included in the search result.<p>
     * 
     * The search roots are used <i>in addition to</i> the current site root
     * of the user performing the search.<p>
     * 
     * By default, the search roots contain only one entry with an empty string.<p>
     * 
     * @return the search roots
     */
    public String[] getSearchRoots() {

        return m_searchRoots;
    }

    /**
     * Returns the sort order used for sorting the results of s search.<p>
     *
     * @return the sort order used for sorting the results of s search
     */
    public Sort getSortOrder() {

        return m_sortOrder;
    }

    /**
     * Initializes the bean with the cms object.<p>
     * 
     * @param cms the cms object
     */
    public void init(CmsObject cms) {

        m_cms = cms;
        m_result = null;
        m_lastException = null;
        m_pageCount = 0;
        m_nextUrl = null;
        m_prevUrl = null;
        m_searchParameters = null;

        if (m_indexName != null) {
            setIndex(m_indexName);
        }
    }

    /**
     * Sets the flag that controls calculation of result categories for the next search, 
     * use this only if it's really required since the search can become very slow using this option.<p>
     *
     * <b>Please note:</b> The calculation of the category count slows down the search time by an order
     * of magnitude. Make sure that you only use this feature if it's really required! 
     * Be especially careful if your search result list can become large (> 1000 documents), since in this case
     * overall system performance will certainly be impacted considerably when calculating the categories.<p> 
     *
     * @param calculateCategories if <code>true</code>, the category count will be calculated for the next search
     */
    public void setCalculateCategories(boolean calculateCategories) {

        m_calculateCategories = calculateCategories;
    }

    /**
     * Sets the search categories, all search results must be in one of the categories,
     * the category set must match the indexed category exactly.<p>
     *
     * All categories will automatically be trimmed and lowercased, since search categories
     * are also stored this way in the index.<p>
     *
     * @param categories the categories to set
     */
    public void setCategories(String[] categories) {

        String[] setCategories = null;
        if (categories != null) {
            if (categories.length != 0) {
                // ensure all categories are not null, trimmed, not-empty and lowercased
                int count = 0;
                String[] setCat = new String[categories.length];
                for (int i = 0; i < categories.length; i++) {
                    String cat = categories[i];
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(cat)) {
                        // all categories must internally be lower case, 
                        // since the index keywords are lowercased as well
                        cat = cat.trim().toLowerCase();
                        setCat[count] = cat;
                        count++;
                    }
                }
                setCategories = new String[count];
                System.arraycopy(setCat, 0, setCategories, 0, count);
            }
        }
        m_categories = setCategories;
        resetLastResult();
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
     * Sets the fields to search.<p>
     * 
     * If the fields are set to <code>null</code>, 
     * or not set at all, the default fields "content" and "meta" are used.<p>
     * 
     * For a list of valid field names, see the Interface constants of
     * <code>{@link org.opencms.search.documents.I_CmsDocumentFactory}</code>. 
     * 
     * @param fields the fields to search
     */
    public void setField(String[] fields) {

        m_fields = fields;
        resetLastResult();
    }

    /**
     * Set the name of the index to search.<p>
     * 
     * A former search result will be deleted.<p>
     * 
     * @param indexName the name of the index
     */
    public void setIndex(String indexName) {

        m_indexName = indexName;
        m_index = null;
        resetLastResult();

        if (m_cms != null && CmsStringUtil.isNotEmpty(indexName)) {
            try {
                m_index = OpenCms.getSearchManager().getIndex(indexName);
                if (m_index == null) {
                    throw new CmsException(Messages.get().container(Messages.ERR_INDEX_NOT_FOUND_1, indexName));
                }
            } catch (Exception exc) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(Messages.LOG_INDEX_ACCESS_FAILED_1, indexName), exc);
                }
                m_lastException = exc;
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
        resetLastResult();
    }

    /**
     * Sets the current result page.<p>
     * 
     * @param page the current result page
     */
    public void setPage(int page) {

        m_page = page;
        resetLastResult();
    }

    /**
     * Sets the search query.<p>
     * 
     * The syntax of the query depends on the search engine used. 
     * A former search result will be deleted.<p>
     * 
     * @param query the search query (escaped format)
     */
    public void setQuery(String query) {

        // do not replace % 
        query = CmsStringUtil.substitute(query, "%", "%25");
        // do not replace +/- 
        query = CmsStringUtil.substitute(query, "+", "%2B");
        query = CmsStringUtil.substitute(query, "-", "%2D");

        m_query = CmsEncoder.decode(query, OpenCms.getSystemInfo().getDefaultEncoding());
        resetLastResult();
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
     * Restrict the result of the next search to the results of the last search, 
     * restricted with the provided parameters.<p>
     * 
     * Use this for "seach in search result" functions.<p> 
     * 
     * @param restriction the restriction to use
     * 
     * @see CmsSearchParameters#restrict(CmsSearchParameters)
     */
    public void setResultRestriction(CmsSearchParameters restriction) {

        resetLastResult();
        m_parameterRestriction = restriction;
    }

    /**
     * Convenience method to set exactly one search root.<p>
     * 
     * @param searchRoot the search root to set
     *
     * @see #setSearchRoots(String[])
     */
    public void setSearchRoot(String searchRoot) {

        setSearchRoots(CmsStringUtil.splitAsArray(searchRoot, ","));
    }

    /**
     * Sets the search root list.<p>
     * 
     * Only resources that are sub-resources of one of the search roots
     * are included in the search result.<p>
     * 
     * The search roots set here are used <i>in addition to</i> the current site root
     * of the user performing the search.<p>
     * 
     * By default, the search roots contain only one entry with an empty string.<p>
     *
     * @param searchRoots the search roots to set
     */
    public void setSearchRoots(String[] searchRoots) {

        m_searchRoots = searchRoots;
        resetLastResult();
    }

    /**
     * Sets the sort order used for sorting the results of s search.<p>
     *
     * @param sortOrder the sort order to set
     */
    public void setSortOrder(Sort sortOrder) {

        m_sortOrder = sortOrder;
        resetLastResult();
    }

    /**
     * Resets the last seach result.<p>
     */
    private void resetLastResult() {

        m_result = null;
        m_lastException = null;
        m_categoriesFound = null;
        m_parameters = null;
        m_parameterRestriction = null;
    }

}
/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsSearch.java,v $
 * Date   : $Date: 2004/08/05 09:28:21 $
 * Version: $Revision: 1.12 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
 * @version $Revision: 1.12 $ $Date: 2004/08/05 09:28:21 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @since 5.3.1
 */
public class CmsSearch implements Serializable, Cloneable {

    /** The cms object. */
    protected transient CmsObject m_cms;

    /** The list of fields to search. */
    protected String m_fields;

    /** The index to search. */
    protected CmsSearchIndex m_index;

    /** The name of the search index. */
    protected String m_indexName;

    /** The current query. */
    protected String m_query;

    /** The minimum length of the search query. */
    protected int m_queryLength = -1;

    /** The current search result. */
    protected List m_result;

    /** The latest exception. */
    protected Exception m_lastException;

    /** The current result page. */
    protected int m_page;

    /** The number of matches per page. */
    protected int m_matchesPerPage = -1;

    /** The number of pages for the result list. */
    protected int m_pageCount;

    /** The number of displayed pages returned by getPageLinks(). */
    protected int m_displayPages = -1;

    /** The URL which leads to the previous result page. */
    protected String m_prevUrl;

    /** The URL which leads to the next result page. */
    protected String m_nextUrl;

    /** The search parameter String. */
    protected String m_searchParameters;

    /** The search root. */
    protected String m_searchRoot;

    /**
     * Default constructor, used to instanciate the search facility as a bean.<p>
     */
    public CmsSearch() {

        super();
        m_searchRoot = "";
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
     * Gets the current fields list.<p>
     * 
     * @return the fields to search
     */
    public String getFields() {

        return m_fields;
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

        if (m_searchParameters == null) {
            StringBuffer params = new StringBuffer(128);
            params.append("?action=search&query=");
            String query = replaceString(m_query, "+", "%2B");
            query = replaceString(query, "-", "%2D");
            params.append(CmsEncoder.encode(query));
            params.append("&matchesPerPage=");
            params.append(this.getMatchesPerPage());
            params.append("&displayPages=");
            params.append(this.getDisplayPages());
            params.append("&index=");
            params.append(CmsEncoder.encode(m_indexName));
            params.append("&searchRoot=");
            params.append(CmsEncoder.encode(this.getSearchRoot()));            
            m_searchParameters = params.toString();
            return m_searchParameters;
        } else {
            return m_searchParameters;
        }
    }

    /**
     * Gets the search result for the current query.<p>
     * 
     * @return the search result (may be empty) or null if no index or query was set before
     */
    public List getSearchResult() {

        if (m_cms != null && m_result == null && m_index != null && m_query != null && !"".equals(m_query.trim())) {

            if ((this.getQueryLength() > 0) && (m_query.trim().length() < this.getQueryLength())) {

                m_lastException = new CmsSearchException("Search query too short, enter at least "
                    + this.getQueryLength()
                    + " characters!");

                return m_result;
            }

            try {
                m_result = m_index.search(m_cms, m_searchRoot, m_query, m_fields);
            } catch (Exception exc) {

                if (OpenCms.getLog(this).isDebugEnabled()) {
                    OpenCms.getLog(this).debug("[" + this.getClass().getName() + "] " + "Searching failed", exc);
                }

                m_result = null;
                m_lastException = exc;
            }
        }

        return m_result;
    }

    /**
     * Gets the search result for the current query and the current page.<p>
     * 
     * @return the search result for the current page (may be empty) or null if no index or query was set before
     */
    public List getSearchResultForPage() {

        if (this.getMatchesPerPage() < 0) {
            return getSearchResult();
        }
        if (this.getPage() <= 0) {
            this.setPage(1);
        }
        List result = new ArrayList(this.getMatchesPerPage());

        // get the complete list of search results
        if (m_result == null) {
            getSearchResult();
            if (m_result == null) {
                return null;
            }
        }

        // calculate the start and end index for the current page
        int startIndex = (this.getPage() - 1) * this.getMatchesPerPage();
        int endIndex = this.getPage() * this.getMatchesPerPage();

        // calculate the number of pages for the result list and the previous and next URLs
        if (this.getMatchesPerPage() < 1) {
            m_pageCount = 1;
        } else {
            m_pageCount = m_result.size() / this.getMatchesPerPage();
            if ((m_result.size() % this.getMatchesPerPage()) != 0) {
                m_pageCount++;
            }
        }
        String url = m_cms.getRequestContext().getUri() + this.getSearchParameters() + "&page=";
        if (this.getPage() > 1) {
            m_prevUrl = url + (this.getPage() - 1);
        }
        if (this.getPage() < m_pageCount) {
            m_nextUrl = url + (this.getPage() + 1);
        }

        // create the result list for the current page
        for (int i = startIndex; i < endIndex; i++) {
            try {
                result.add(m_result.get(i));
            } catch (IndexOutOfBoundsException e) {
                // end of list reached, interrupt loop
                break;
            }
        }
        return result;
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
     * Method to replace a subString with replaceItem.<p>
     * 
     * @param testString the original String
     * @param searchString the subString that has to be replaced
     * @param replaceItem the String that replaces searchString
     * @return String with replaced subStrings
     */
    private String replaceString(String testString, String searchString, String replaceItem) {

        /* if searchString isn't in testString, return (better performance) */
        if (testString.indexOf(searchString) == -1) {
            return testString;
        }
        int tempIndex = 0;
        int searchLen = searchString.length();
        int searchIndex = testString.indexOf(searchString);
        StringBuffer returnString = new StringBuffer(testString.length());
        while (searchIndex != -1) {
            returnString.append(testString.substring(0, searchIndex));
            returnString.append(replaceItem);
            tempIndex = searchIndex + searchLen;
            testString = testString.substring(tempIndex);
            searchIndex = testString.indexOf(searchString);
        }
        returnString.append(testString);
        return returnString.toString();
    }

    /**
     * Sets the fields to search.<p>
     * 
     * Syntax and fieldnames depend on the search engine used.
     * A former search result will be deleted.<p>
     * 
     * @param fields the fields to search
     */
    public void setField(String[] fields) {

        StringBuffer fBuf = new StringBuffer();
        for (int i = 0; i < fields.length; i++) {
            fBuf.append(fields[i]);
            fBuf.append(" ");
        }
        m_fields = fBuf.toString();
        m_result = null;
        m_lastException = null;
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
        m_result = null;
        m_index = null;
        m_lastException = null;

        if (m_cms != null && indexName != null && !"".equals(indexName)) {
            try {
                m_index = OpenCms.getSearchManager().getIndex(indexName);
                if (m_index == null) {
                    throw new CmsException("Index " + indexName + " not found");
                }
            } catch (Exception exc) {
                if (OpenCms.getLog(this).isDebugEnabled()) {
                    OpenCms.getLog(this).debug(
                        "[" + this.getClass().getName() + "] " + "Accessing index " + indexName + " failed",
                        exc);
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
    }

    /**
     * Sets the current result page.<p>
     * 
     * @param page the current result page
     */
    public void setPage(int page) {

        m_page = page;
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

        m_query = CmsEncoder.unescape(query, null);
        m_result = null;
        m_lastException = null;
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
     * Returns the search root.<p>
     * 
     * Only resource that are sub-resource of the search root
     * are included in the search result.<p>
     * 
     * Per default, the search root is an empty string.<p>
     *
     * @return the search root
     */
    public String getSearchRoot() {

        return m_searchRoot;
    }

    /**
     * Sets the search root.<p>
     * 
     * Only resource that are sub-resource of the search root
     * are included in the search result.<p>
     * 
     * Per default, the search root is an empty string.<p>
     *
     * @param searchRoot the search root to set
     */
    public void setSearchRoot(String searchRoot) {

        m_searchRoot = searchRoot;
    }
}
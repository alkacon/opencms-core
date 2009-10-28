/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/Attic/CmsGallerySearch.java,v $
 * Date   : $Date: 2009/10/28 14:24:06 $
 * Version: $Revision: 1.2 $
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

import java.util.List;

/**
 * Helper class for search within galleries.<p>
 * 
 * @author
 * 
 * @version
 * 
 * @since
 */
public class CmsGallerySearch {

    /** Array of requested categories. */
    private String[] m_categories;

    /** Array of requested galleries. */
    private String[] m_galleries;

    /** The number of pages for the result list. */
    private int m_pageCount;

    /** Search query. */
    private String m_query;

    /** The current search result. */
    private List<CmsSearchResult> m_result;

    /** The current result-page-index. */
    private int m_pageIndex;

    /** The total number of search results matching the query. */
    private int m_searchResultCount;

    /** Array of requested types. */
    private int[] m_types;

    /** The number of results per page. */
    private int m_matchesPerPage;

    /**
     * Returns the matchesPerPage.<p>
     *
     * @return the matchesPerPage
     */
    public int getMatchesPerPage() {

        return m_matchesPerPage;
    }

    /**
     * Sets the matchesPerPage.<p>
     *
     * @param matchesPerPage the matchesPerPage to set
     */
    public void setMatchesPerPage(int matchesPerPage) {

        m_matchesPerPage = matchesPerPage;
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
     * Returns the galleries.<p>
     *
     * @return the galleries
     */
    public String[] getGalleries() {

        return m_galleries;
    }

    /**
     * Returns the pageCount.<p>
     *
     * @return the pageCount
     */
    public int getPageCount() {

        return m_pageCount;
    }

    /**
     * Returns the query.<p>
     *
     * @return the query
     */
    public String getQuery() {

        return m_query;
    }

    /**
     * Returns the result.<p>
     *
     * @return the result
     */
    public List<CmsSearchResult> getResult() {

        return m_result;
    }

    /**
     * Returns the resultPage.<p>
     *
     * @return the resultPage
     */
    public int getResultPage() {

        return m_pageIndex;
    }

    /**
     * Returns the searchResultCount.<p>
     *
     * @return the searchResultCount
     */
    public int getSearchResultCount() {

        return m_searchResultCount;
    }

    /**
     * Returns the types.<p>
     *
     * @return the types
     */
    public int[] getTypes() {

        return m_types;
    }

    /**
     * Sets the categories.<p>
     *
     * @param categories the categories to set
     */
    public void setCategories(String[] categories) {

        m_categories = categories;
    }

    /**
     * Sets the galleries.<p>
     *
     * @param galleries the galleries to set
     */
    public void setGalleries(String[] galleries) {

        m_galleries = galleries;
    }

    /**
     * Sets the query.<p>
     *
     * @param query the query to set
     */
    public void setQuery(String query) {

        m_query = query;
    }

    /**
     * Sets the resultPage.<p>
     *
     * @param resultPage the resultPage to set
     */
    public void setResultPage(int resultPage) {

        m_pageIndex = resultPage;
    }

    /**
     * Sets the types.<p>
     *
     * @param types the types to set
     */
    public void setTypes(int[] types) {

        m_types = types;
    }

}

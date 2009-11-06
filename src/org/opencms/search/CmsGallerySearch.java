/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/Attic/CmsGallerySearch.java,v $
 * Date   : $Date: 2009/11/06 08:53:55 $
 * Version: $Revision: 1.4 $
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

    /** Sort parameter constants. */
    public enum SortParam {

        /** Sort created by ascending. */
        CREATEDBY_ASC("createdby.asc"),

        /** Sort created by descending. */
        CREATEDBY_DESC("createdby.desc"),

        /** Sort date created ascending. */
        DATECREATED_ASC("datecreated.asc"),

        /** Sort date created descending. */
        DATECREATED_DESC("datecreated.desc"),

        /** Sort date expired ascending. */
        DATEEXPIRED_ASC("dateexpired.asc"),

        /** Sort date expired descending. */
        DATEEXPIRED_DESC("dateexpired.desc"),

        /** Sort date modified ascending. */
        DATEMODIFIED_ASC("datemodified.asc"),

        /** Sort date modified descending. */
        DATEMODIFIED_DESC("datemodified.desc"),

        /** Sort date released ascending. */
        DATERELEASED_ASC("datereleased.asc"),

        /** Sort date released descending. */
        DATERELEASED_DESC("datereleased.desc"),

        /** Sort modified by ascending. */
        MODIFIEDBY_ASC("modifiedby.asc"),

        /** Sort modified by descending. */
        MODIFIEDBY_DESC("modifiedby.desc"),

        /** Sort path ascending. */
        PATH_ASC("path.asc"),

        /** Sort path descending. */
        PATH_DESC("path.desc"),

        /** Sort score ascending. */
        SCORE_ASC("score.asc"),

        /** Sort score descending. */
        SCORE_DESC("score.desc"),

        /** Sort size ascending. */
        SIZE_ASC("size.asc"),

        /** Sort size descending. */
        SIZE_DESC("size.desc"),

        /** Sort state ascending. */
        STATE_ASC("state.asc"),

        /** Sort state descending. */
        STATE_DESC("state.desc"),

        /** Sort title ascending. */
        TITLE_ASC("title.asc"),

        /** Sort title ascending. */
        TITLE_DESC("title.desc"),

        /** Sort type ascending. */
        TYPE_ASC("type.asc"),

        /** Sort type descending. */
        TYPE_DESC("type.desc");

        /** The default sort param. */
        public static final SortParam DEFAULT = TITLE_DESC;

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private SortParam(String name) {

            m_name = name;
        }

        /** 
         * Returns the name.<p>
         * 
         * @return the name
         */
        public String getName() {

            return m_name;
        }
    }

    /** Array of requested categories. */
    private List<String> m_categories;

    /** Array of requested galleries. */
    private List<String> m_galleries;

    /** The number of results per page. */
    private int m_matchesPerPage;

    /** The number of pages for the result list. */
    private int m_pageCount;

    /** The current result-page-index. */
    private int m_pageIndex;

    /** Search query. */
    private String m_query;

    /** The current search result. */
    private List<CmsSearchResult> m_result;

    /** The total number of search results matching the query. */
    private int m_searchResultCount;

    /** The sort-by properties. */
    private SortParam m_sortBy;

    /** The sort-order. */
    private SortParam m_sortOrder;

    /** Array of requested types. */
    private List<Integer> m_typeIds;

    /**
     * Returns the categories.<p>
     *
     * @return the categories
     */
    public List<String> getCategories() {

        return m_categories;
    }

    /**
     * Returns the galleries.<p>
     *
     * @return the galleries
     */
    public List<String> getGalleries() {

        return m_galleries;
    }

    /**
     * Returns the matchesPerPage.<p>
     *
     * @return the matchesPerPage
     */
    public int getMatchesPerPage() {

        return m_matchesPerPage;
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
     * Returns the sortBy.<p>
     *
     * @return the sortBy
     */
    public SortParam getSortBy() {

        return m_sortBy;
    }

    /**
     * Returns the sortOrder.<p>
     *
     * @return the sortOrder
     */
    public SortParam getSortOrder() {

        return m_sortOrder;
    }

    /**
     * Returns the type-id's.<p>
     *
     * @return the type-id's
     */
    public List<Integer> getTypeIds() {

        return m_typeIds;
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
     * Sets the galleries.<p>
     *
     * @param galleries the galleries to set
     */
    public void setGalleries(List<String> galleries) {

        m_galleries = galleries;
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
     * Sets the sortBy.<p>
     *
     * @param sortBy the sortBy to set
     */
    public void setSortBy(SortParam sortBy) {

        m_sortBy = sortBy;
    }

    /**
     * Sets the sortOrder.<p>
     *
     * @param sortOrder the sortOrder to set
     */
    public void setSortOrder(SortParam sortOrder) {

        m_sortOrder = sortOrder;
    }

    /**
     * Sets the type-id's.<p>
     *
     * @param typeIds the type-id's to set
     */
    public void setTypeIds(List<Integer> typeIds) {

        m_typeIds = typeIds;
    }

}

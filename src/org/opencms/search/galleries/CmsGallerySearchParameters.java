/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/galleries/CmsGallerySearchParameters.java,v $
 * Date   : $Date: 2010/01/11 13:26:40 $
 * Version: $Revision: 1.1 $
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

package org.opencms.search.galleries;

import org.opencms.search.galleries.CmsGallerySearch.SortParam;

import java.util.List;

/**
 * Parameters used for the ADE gallery search.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0 
 */
public class CmsGallerySearchParameters {

    /**
     * Helper class to store a time range.<p>
     */
    class CmsSearchTimeRange {

        /** The start time of the time range. */
        private long m_startTime;

        /** The end time of the time range. */
        private long m_endTime;

        /**
         * Standard constructor.<p>
         * 
         * @param startTime the start time of the time range
         * @param endTime the end time of the time range
         */
        public CmsSearchTimeRange(long startTime, long endTime) {

            m_startTime = startTime;
            m_endTime = endTime;
        }

        /**
         * Returns the start time of the time range.<p>
         * 
         * @return the start time of the time range
         */
        public long getStartTime() {

            return m_startTime;
        }

        /**
         * Returns the end time of the time range.<p>
         * 
         * @return the end time of the time range
         */
        public long getEndTime() {

            return m_endTime;
        }
    }

    /** The time range for the date of resource creation to consider in the search. */
    private CmsSearchTimeRange m_dateCreatedTimeRange;

    /** The time range for the date of resource last modification to consider in the search. */
    private CmsSearchTimeRange m_dateLastModifiedTimeRange;

    /** The categories to search in. */
    private List<String> m_categories;

    /** The galleries to search in. */
    private List<String> m_galleries;

    /** The container types to search in. */
    private List<String> m_containerTypes;

    /** The locale for the search. */
    private String m_locale;

    /** The number of search results per page. */
    private int m_matchesPerPage;

    /** The resource types to search for. */
    private List<String> m_resourceTypes;

    /** The requested page of the result. */
    private int m_resultPage;

    /** The sort order for the search result. */
    private SortParam m_sortOrder;

    /** Search words to search for. */
    private String m_words;

    /**
     * Returns the categories that have been included in the search.<p>
     *
     * @return the categories that have been included in the search
     */
    public List<String> getCategories() {

        return m_categories;
    }

    /**
     * Returns the galleries that have been included in the search.<p>
     *
     * @return the galleries that have been included in the search
     */
    public List<String> getGalleries() {

        return m_galleries;
    }

    /**
     * Returns the locale that has been used for the search.<p>
     *     
     * @return the locale that has been used for the search
     */
    public String getLocale() {

        return m_locale;
    }

    /**
     * Returns the maximum number of matches per result page.<p>
     *
     * @return the the maximum number of matches per result page
     * 
     * @see #getMatchesPerPage()
     * @see #setResultPage(int)
     */
    public int getMatchesPerPage() {

        return m_matchesPerPage;
    }

    /**
     * Returns the names of the resource types that have been included in the search result.<p>
     *
     * @return the names of the resource types that have been included in the search result
     */
    public List<String> getResourceTypes() {

        return m_resourceTypes;
    }

    /**
     * Returns the index of the result page.<p>
     * 
     * @return the index of the result page
     * 
     * @see #setResultPage(int)
     * @see #getMatchesPerPage()
     * @see #setMatchesPerPage(int)
     */
    public int getResultPage() {

        return m_resultPage;
    }

    /**
     * Returns the words (terms) that have been used for the full text search.<p>
     * 
     * @return the words (terms) that have been used for the full text search
     */
    public String getSearchWords() {

        return m_words;
    }

    /**
     * Returns the sort order that has been used in the search.<p>
     * 
     * @return the sort order that has been used in the search
     */
    public SortParam getSortOrder() {

        return m_sortOrder;
    }

    /**
     * Sets the categories for the search.<p>
     *
     * Results are found only if they are contained in at least one of the given categories.
     *
     * @param categories the categories to set
     */
    public void setCategories(List<String> categories) {

        m_categories = categories;
    }

    /** 
     * Sets the time range for the date of resource creation to consider in the search.<p>
     * 
     * @param startTime the start time of the time range
     * @param endTime the end time of the time range
     */
    public void setDateCreatedTimeRange(long startTime, long endTime) {

        if (m_dateCreatedTimeRange == null) {
            m_dateCreatedTimeRange = new CmsSearchTimeRange(startTime, endTime);
        }
    }

    /** 
     * Sets the time range for the date of resource last modification to consider in the search.<p> 
     * 
     * @param startTime the start time of the time range
     * @param endTime the end time of the time range
     */
    public void setDateLastModifiedTimeRange(long startTime, long endTime) {

        if (m_dateLastModifiedTimeRange == null) {
            m_dateLastModifiedTimeRange = new CmsSearchTimeRange(startTime, endTime);
        }
    }

    /**
     * Returns the container types that have been included in the search.<p>
     *
     * @return the container types that have been included in the search
     */
    public List<String> getContainerTypes() {

        return m_containerTypes;
    }

    /**
     * Sets the container types for the search.<p>
     *
     * Results are found only if they are compatible with one of the given container types.
     * If no container type is set, results compatible with any container will be returned in the search result.<p>
     *
     * @param containerTypes the container types to set
     */
    public void setContainerTypes(List<String> containerTypes) {

        m_containerTypes = containerTypes;
    }

    /**
     * Sets the galleries for the search.<p>
     *
     * Results are found only if they are contained in one of the given galleries.
     * If no gallery is set, results from all galleries will be returned in the search result.<p>
     *
     * @param galleries the galleries to set
     */
    public void setGalleries(List<String> galleries) {

        m_galleries = galleries;
    }

    /**
     * Sets the maximum number of matches per result page.<p>
     *
     * Use this together with {@link #setResultPage(int)} in order to split the result 
     * in more than one page.<p> 
     *
     * @param matchesPerPage the the maximum number of matches per result page to set
     * 
     * @see #getMatchesPerPage()
     * @see #setResultPage(int)
     */
    public void setMatchesPerPage(int matchesPerPage) {

        m_matchesPerPage = matchesPerPage;
    }

    /**
     * Sets the names of the resource types to include in the search result.<p>
     *
     * Results are found only if they resources match one of the given resource type names.
     * If no resource type name is set, all resource types will be returned in the search result.<p>
     *
     * @param resourceTypes the names of the resource types to include in the search result
     */
    public void setResourceTypes(List<String> resourceTypes) {

        m_resourceTypes = resourceTypes;
    }

    /**
     * Sets the index of the result page that should be returned.<p>
     *
     * Use this together with {@link #setMatchesPerPage(int)} in order to split the result 
     * in more than one page.<p> 
     *
     * @param resultPage the index of the result page to return
     *
     * @see #getResultPage()
     * @see #getMatchesPerPage()
     * @see #setMatchesPerPage(int)
     */
    public void setResultPage(int resultPage) {

        m_resultPage = resultPage;
    }

    /**
     * Sets the locale for the search.<p>
     * 
     * Results are found only if they match the given locale.
     * If no locale is set, results for all locales will be returned in the search result.<p>
     * 
     * @param locale the locale to set
     */
    public void setSearchLocale(String locale) {

        m_locale = locale;
    }

    /**
     * Sets the words (terms) for the full text search.<p>
     *
     * Results are found only if they text extraction for the resource contains all given search words.
     * If no search word is set, all resources will be returned in the search result.<p>
     *
     * Please note that this should be a list of words separated by white spaces.
     * Simple Lucene modifiers such as (+), (-) and (*) are allowed, but anything more complex then this
     * will be removed.<p> 
     *
     * @param words the words (terms) for the full text search to set
     */
    public void setSearchWords(String words) {

        m_words = words;
    }

    /**
     * Sets the sort order for the search.<p>
     *
     * @param sortOrder the sort order to set
     */
    public void setSortOrder(SortParam sortOrder) {

        m_sortOrder = sortOrder;
    }
}
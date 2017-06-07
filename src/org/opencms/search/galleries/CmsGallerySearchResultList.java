/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

import java.util.ArrayList;

/**
 * The search result list for the gallery search index.<p>
 *
 * @since 8.0.0
 */
public class CmsGallerySearchResultList extends ArrayList<CmsGallerySearchResult> {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 115646669707330088L;

    /** The total number of search results matching the query. */
    private int m_hitCount;

    /** The number of pages for the result list. */
    private int m_pageCount;

    /** The current result-page-index. */
    private int m_pageIndex;

    /**
     * Creates a new result list with a default initial capacity of 100.<p>
     */
    public CmsGallerySearchResultList() {

        this(100);
    }

    /**
     * Creates a new result list with the specified initial capacity.<p>
     *
     * @param initialCapacity the initial capacity
     */
    public CmsGallerySearchResultList(int initialCapacity) {

        super(initialCapacity);
    }

    /**
     * Appends the results from another search result list.<p>
     *
     * @param moreResults the second search result list
     */
    public void append(CmsGallerySearchResultList moreResults) {

        addAll(moreResults);
        m_hitCount = moreResults.getHitCount();
    }

    /**
     * Returns the hit count of all results found in the last search.<p>
     *
     * Since this list will only contain the result objects for the current display page,
     * the size of the list is usually much less then the hit count of all results found.<p>
     *
     * @return the hit count of all results found in the last search
     */
    public int getHitCount() {

        return m_hitCount;
    }

    /**
     * Returns the total number of search result pages.<p>
     *
     * @return the total number of search result pages
     *
     * @see #getHitCount()
     * @see #getResultPage()
     */
    public int getPageCount() {

        return m_pageCount;
    }

    /**
     * Returns the index of the current result page.<p>
     *
     * @return the index of the current result page
     *
     * @see #getHitCount()
     * @see #getPageCount()
     */
    public int getResultPage() {

        return m_pageIndex;
    }

    /**
     * Sets the hit count of all results found in the last search.<p>
     *
     * Since this list will only contain the result objects for the current display page,
     * the size of the list is usually much less then the hit count of all results found.<p>
     *
     *  @param hitCount the hit count to set
     */
    public void setHitCount(int hitCount) {

        m_hitCount = hitCount;
    }

    /**
     * Calculates the result pages.<p>
     *
     * @param pageIndex the index of the current page
     * @param matchesPerPage the matches per page
     */
    public void calculatePages(int pageIndex, int matchesPerPage) {

        m_pageIndex = pageIndex;
        // calculate the number of pages for this search result
        m_pageCount = m_hitCount / matchesPerPage;
        if ((m_hitCount % matchesPerPage) != 0) {
            m_pageCount++;
        }
    }
}
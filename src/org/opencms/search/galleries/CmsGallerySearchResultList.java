/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/galleries/CmsGallerySearchResultList.java,v $
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

import java.util.ArrayList;
import java.util.List;

/**
 * The search result list for the ADE gallery search.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0 
 */
public class CmsGallerySearchResultList extends ArrayList<CmsGallerySearchResult> {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 115646669707330088L;

    /** The number of pages for the result list. */
    private int m_pageCount;

    /** The current result-page-index. */
    private int m_pageIndex;

    /** The individual result objects of the search. */
    private List<CmsGallerySearchResult> m_results;

    /** The total number of search results matching the query. */
    private int m_searchResultCount;

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
     * Returns the total number of search result pages.<p>
     *
     * @return the total number of search result pages
     * 
     * @see #getSearchResultCount()
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
     * @see #getSearchResultCount()
     * @see #getPageCount()
     */
    public int getResultPage() {

        return m_pageIndex;
    }

    /**
     * Returns the individual result objects of the search.<p>
     * 
     * @return the individual result objects of the search
     */
    public List<CmsGallerySearchResult> getResults() {

        return m_results;
    }

    /**
     * Returns the number of search results found.<p>
     *
     * Since this list will only contain the result objects for the current display page,
     * the size of the list is usually much less then the total number of all results found.<p>
     *
     * @return the number of search results found
     * 
     * @see #getResultPage()
     * @see #getPageCount()
     */
    public int getSearchResultCount() {

        return m_searchResultCount;
    }
}
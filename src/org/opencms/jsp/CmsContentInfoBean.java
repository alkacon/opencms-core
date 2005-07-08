/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsContentInfoBean.java,v $
 * Date   : $Date: 2005/07/08 12:50:00 $
 * Version: $Revision: 1.12 $
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

package org.opencms.jsp;

import org.opencms.util.CmsStringUtil;

/**
 * A container to store information about a collector's result.<p>
 * 
 * @author Thomas Weckert  
 * 
 * @version $Revision: 1.12 $ 
 * 
 * @since 6.0.0 
 */
public class CmsContentInfoBean {

    /** The name under which the collector info is saved in the page context. */
    public static final String PAGE_CONTEXT_ATTRIBUTE_NAME = "CollectorInfo";

    /** The number of pages of browse through the result list. */
    private int m_pageCount;

    /** The index of the current page that to be displayed. */
    private int m_pageIndex;

    /** The page index of the last element in the Google-like page navigation. */
    private int m_pageNavEndIndex;

    /** The number of page links in the Google-like page navigation. */
    private int m_pageNavLength;

    /** The page index of the first element in the Google-like page navigation. */
    private int m_pageNavStartIndex;

    /** The number of entries to be displayed on a page. */
    private int m_pageSize;

    /** The index of the current resource that gets iterated in the result list. */
    private int m_resultIndex;

    /** The total size of the collector's result list. */
    private int m_resultSize;

    /**
     * Creates a new content info bean.<p>
     */
    public CmsContentInfoBean() {

        super();

        m_resultSize = -1;
        m_resultIndex = -1;

        m_pageCount = 1;
        m_pageSize = -1;
        m_pageIndex = 1;
        m_pageNavStartIndex = 1;
        m_pageNavEndIndex = 1;
        m_pageNavLength = 10;
    }

    /**
     * Returns the number of pages of browse through the result list.<p>
     * 
     * @return the number of pages of browse through the result list
     */
    public int getPageCount() {

        return m_pageCount;
    }

    /**
     * Returns the index of the current page that gets displayed.<p>
     * 
     * @return the index of the current page that gets displayed
     */
    public int getPageIndex() {

        return m_pageIndex;
    }

    /**
     * Returns the page index of the first element in the Google-like page navigation.<p>
     * 
     * @return the page index of the first element in the Google-like page navigation
     */
    public int getPageNavEndIndex() {

        return m_pageNavEndIndex;
    }

    /**
     * Returns the page index of the last element in the Google-like page navigation.<p>
     * 
     * @return page index of the last element in the Google-like page navigation
     */
    public int getPageNavLength() {

        return m_pageNavLength;
    }

    /**
     * Returns the page index of the first element in the Google-like page navigation.<p>
     * 
     * @return the page index of the first element in the Google-like page navigation
     */
    public int getPageNavStartIndex() {

        return m_pageNavStartIndex;
    }

    /**
     * Returns the size of a page.<p>
     * 
     * @return the size of a page
     */
    public int getPageSize() {

        return m_pageSize;
    }

    /**
     * Returns the index of the current resource that gets iterated in the result list.<p>
     * 
     * @return the index of the current resource that gets iterated in the result list
     */
    public int getResultIndex() {

        return m_resultIndex;
    }

    /**
     * Returns the total size of the collector's result list.<p>
     * 
     * @return the total size of the collector's result list
     */
    public int getResultSize() {

        return m_resultSize;
    }

    /**
     * Returns true if there is no resource in the result list.<p> 
     * 
     * @return true if there is no resource in the result list
     */
    public boolean isEmptyResult() {

        return m_resultSize <= 0;
    }

    /**
     * Returns true if the current resource is the first resource on the current page.<p>
     * 
     * @return true if the current resource is the first resource on the current page
     */
    public boolean isFirstOnPage() {

        return m_resultIndex == (m_pageCount * m_pageSize) + 1;
    }

    /**
     * Returns true if the current resource is the first resource in the result list.<p>
     * 
     * @return true if the current resource is the first resource in the result list
     */
    public boolean isFirstResult() {

        return m_resultIndex == 1;
    }

    /**
     * Returns true if the current resource is the last resource on the current page.<p>
     * 
     * @return true if the current resource is the last resource on the current page
     */
    public boolean isLastOnPage() {

        return (m_resultIndex == (m_pageCount + 1) * m_pageSize) || isLastResult();
    }

    /**
     * Returns true if the current resource is the last resource in the result list.<p>
     * 
     * @return true if the current resource is the last resource in the result list
     */
    public boolean isLastResult() {

        return m_resultIndex == m_resultSize;
    }

    /**
     * Increments the index of the current resource that gets iterated in the result list.<p>
     */
    void incResultIndex() {

        m_resultIndex++;
    }

    /**
     * Initializes the start and end indexes to build a Google-like page navigation.<p>
     */
    void initPageNavIndexes() {

        if (m_pageIndex < m_pageNavLength) {

            m_pageNavStartIndex = 1;
            m_pageNavEndIndex = m_pageCount < m_pageNavLength ? m_pageCount : m_pageNavLength;

        } else {

            int middle = m_pageNavLength / 2;
            m_pageNavStartIndex = m_pageIndex - middle;
            m_pageNavEndIndex = m_pageNavStartIndex + m_pageNavLength - 1;

            if (m_pageNavStartIndex < 1) {
                m_pageNavStartIndex = 1;
            } else if (m_pageNavEndIndex < 1) {
                m_pageNavEndIndex = m_pageCount;
            } else if (m_pageNavEndIndex > m_pageCount) {

                // adjust end index
                m_pageNavEndIndex = m_pageCount;
                m_pageNavStartIndex = m_pageNavEndIndex - m_pageNavLength + 1;

                if (m_pageNavStartIndex < 1) {
                    // adjust the start index again
                    m_pageNavStartIndex = 1;
                }
            }
        }
    }

    /**
     * Initializes the index of the current resource that gets iterated in the result list.<p>
     */
    void initResultIndex() {

        int startIndex = 0;
        if (m_pageIndex > 0 && m_pageSize > 0) {
            startIndex = (m_pageIndex - 1) * m_pageSize;
        }

        m_resultIndex = startIndex > m_resultSize ? m_resultSize : startIndex;
    }

    /**
     * Sets the number of pages of browse through the result list.<p>
     * 
     * @param pageCount the number of pages of browse through the result list
     */
    void setPageCount(int pageCount) {

        m_pageCount = pageCount;
    }

    /**
     * Sets the index of the current page that gets displayed as an int.<p>
     * 
     * @param pageIndex the index of the current page that gets displayed as an int
     */
    void setPageIndex(int pageIndex) {

        m_pageIndex = pageIndex;
    }

    /**
     * Sets the index of the current page that gets displayed as a string.<p>
     * 
     * The specified string gets parsed into an int.<p>
     * 
     * @param pageIndex the index of the current page that gets displayed as a string
     */
    void setPageIndexAsString(String pageIndex) {

        if (CmsStringUtil.isEmpty(pageIndex)) {
            return;
        }

        try {
            m_pageIndex = Integer.parseInt(pageIndex);
        } catch (NumberFormatException e) {
            // intentionally left blank
        }
    }

    /**
     * Sets the page index of the last element in the Google-like page navigation.<p>
     * 
     * @param index the page index of the last element in the Google-like page navigation
     */
    void setPageNavEndIndex(int index) {

        m_pageNavEndIndex = index;
    }

    /**
     * Sets the number of page links in the Google-like page navigation.<p>
     * 
     * @param length the number of page links in the Google-like page navigation
     */
    void setPageNavLength(int length) {

        m_pageNavLength = length;
    }

    /**
     * Sets number of page links in the Google-like page navigation as a string.<p>
     * 
     * @param pageNavLength the number of page links in the Google-like page navigation
     */
    void setPageNavLengthAsString(String pageNavLength) {

        if (CmsStringUtil.isEmpty(pageNavLength)) {
            return;
        }

        try {
            m_pageNavLength = Integer.parseInt(pageNavLength);
        } catch (NumberFormatException e) {
            // intentionally left blank
        }
    }

    /**
     * Sets the page index of the first element in the Google-like page navigation.<p>
     * 
     * @param index the page index of the first element in the Google-like page navigation
     */
    void setPageNavStartIndex(int index) {

        m_pageNavStartIndex = index;
    }

    /**
     * Sets the size of a page as an int.<p>
     * 
     * @param pageSize the size of a page as an int
     */
    void setPageSize(int pageSize) {

        m_pageSize = pageSize;
    }

    /**
     * Sets the size of a page as a string.<p>
     * 
     * The specified string gets parsed into an int.<p>
     * 
     * @param pageSize the size of a page as a string
     */
    void setPageSizeAsString(String pageSize) {

        if (CmsStringUtil.isEmpty(pageSize)) {
            return;
        }

        try {
            m_pageSize = Integer.parseInt(pageSize);
        } catch (NumberFormatException e) {
            // intentionally left blank
        }
    }

    /**
     * Sets the total size of the collector's result list.<p>
     * 
     * @param size the total size of the collector's result list
     */
    void setResultSize(int size) {

        m_resultSize = size;
    }

}
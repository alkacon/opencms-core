/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsContentInfoBean.java,v $
 * Date   : $Date: 2005/01/12 16:43:30 $
 * Version: $Revision: 1.1 $
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

package org.opencms.jsp;

import org.opencms.util.CmsStringUtil;

/**
 * A container to store information about a collector's result.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $
 * @since 6.0 alpha 3
 */
public class CmsContentInfoBean {

    /** The name under which the collector info is saved in the page context. */
    public static final String C_PAGE_CONTEXT_ATTRIBUTE_NAME = "CollectorInfo";

    /** The total size of the collector's result list. */
    private int m_resultSize;
    
    /** The index of the current resource that gets iterated in the result list. */
    private int m_resultIndex;
    
    /** The number of pages of browse through the result list. */
    private int m_pageCount;
    
    /** The size of a page to be displayed. */
    private int m_pageSize;
    
    /** The index of the current page that gets displayed. */
    private int m_pageIndex;

    /**
     * Creates a new content info bean.<p>
     */
    public CmsContentInfoBean() {

        super();

        m_resultSize = -1;
        m_resultIndex = -1;
        m_pageCount = -1;
        m_pageSize = -1;
        m_pageIndex = 1;
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
     * Increments the index of the current resource that gets iterated in the result list.<p>
     */
    void incResultIndex() {

        m_resultIndex++;
    }
    
    /**
     * Initializes the index of the current resource that gets iterated in the result list.<p>
     */
    void initResultIndex() {
        
        int startIndex = 0;        
        if (m_pageIndex > 0 && m_pageSize > 0) {
            startIndex = (m_pageIndex -1) * m_pageSize;
        }
        
        m_resultIndex = startIndex > m_resultSize ? m_resultSize : startIndex;
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
     * Sets the total size of the collector's result list.<p>
     * 
     * @param size the total size of the collector's result list
     */
    void setResultSize(int size) {

        m_resultSize = size;
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
     * Sets the number of pages of browse through the result list.<p>
     * 
     * @param pageCount the number of pages of browse through the result list
     */
    void setPageCount(int pageCount) {

        m_pageCount = pageCount;
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
     * Returns the size of a page.<p>
     * 
     * @return the size of a page
     */
    public int getPageSize() {

        return m_pageSize;
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

}
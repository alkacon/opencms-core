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

package org.opencms.jsp.search.config;

/** Search configuration for pagination. */
public class CmsSearchConfigurationPagination implements I_CmsSearchConfigurationPagination {

    /** The request parameter used to send the current page number. */
    private final String m_pageParam;
    /** The page size. */
    private final int m_pageSize;
    /** The length of the "Google"-like page navigation. Should be an odd number. */
    private final int m_pageNavLength;

    /** The default page size. */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /** The default "Google"-like page navigation length. */
    private static final int DEFAULT_PAGE_NAV_LENGTH = 5;

    /** Constructor setting all configuration options for the pagination.
     * @param pageParam The request parameter used to send the current page number.
     * @param pageSize The page size.
     * @param pageNavLength The length of the "Google"-like page navigation. Should be an odd number.
     */
    public CmsSearchConfigurationPagination(
        final String pageParam,
        final Integer pageSize,
        final Integer pageNavLength) {

        m_pageParam = pageParam;
        m_pageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize.intValue();
        m_pageNavLength = pageNavLength == null ? DEFAULT_PAGE_NAV_LENGTH : pageNavLength.intValue();
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationPagination#getPageNavLength()
     */
    @Override
    public int getPageNavLength() {

        return m_pageNavLength;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationPagination#getPageParam()
     */
    @Override
    public String getPageParam() {

        return m_pageParam;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationPagination#getPageSize()
     */
    @Override
    public int getPageSize() {

        return m_pageSize;
    }
}

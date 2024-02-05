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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Search configuration for pagination. */
public class CmsSearchConfigurationPagination implements I_CmsSearchConfigurationPagination {

    /** The default page size. */
    public static final List<Integer> DEFAULT_PAGE_SIZE = Collections.singletonList(Integer.valueOf(10));
    /** The default "Google"-like page navigation length. */
    public static final int DEFAULT_PAGE_NAV_LENGTH = 5;
    /** The default request parameter to read the current page from. */
    public static final String DEFAULT_PAGE_PARAM = "page";
    /** The request parameter used to send the current page number. */
    private final String m_pageParam;

    /** The page sizes for the (i+1)th page. The last provided page size is the size of all following pages. */
    private final List<Integer> m_pageSizes;

    /** The page size for all pages, where no explicit size is specified. */
    private final int m_pageSizeAllRemainingPages;

    /** The length of the "Google"-like page navigation. Should be an odd number. */
    private final int m_pageNavLength;

    /** Constructor setting all configuration options for the pagination.
     * @param pageParam The request parameter used to send the current page number.
     * @param pageSize The page size.
     * @param pageNavLength The length of the "Google"-like page navigation. Should be an odd number.
     */
    public CmsSearchConfigurationPagination(
        final String pageParam,
        final Integer pageSize,
        final Integer pageNavLength) {

        this(pageParam, null != pageSize ? Collections.singletonList(pageSize) : null, pageNavLength);
    }

    /** Constructor setting all configuration options for the pagination.
     * @param pageParam The request parameter used to send the current page number.
     * @param pageSizes The page sizes for the first pages. The last provided size is the size of all following pages.
     * @param pageNavLength The length of the "Google"-like page navigation. Should be an odd number.
     */
    public CmsSearchConfigurationPagination(
        final String pageParam,
        final List<Integer> pageSizes,
        final Integer pageNavLength) {

        m_pageParam = pageParam == null ? DEFAULT_PAGE_PARAM : pageParam;
        if ((pageSizes == null) || pageSizes.isEmpty()) {
            m_pageSizes = DEFAULT_PAGE_SIZE;
        } else {
            m_pageSizes = new ArrayList<Integer>();
            m_pageSizes.addAll(pageSizes);
        }
        m_pageSizeAllRemainingPages = (m_pageSizes.get(m_pageSizes.size() - 1)).intValue();

        m_pageNavLength = pageNavLength == null ? DEFAULT_PAGE_NAV_LENGTH : pageNavLength.intValue();
    }

    /**
     * Creates a new pagination configuration if at least one of the provided parameters is not null.
     * Otherwise returns null.
     * @param pageParam The request parameter used to send the current page number.
     * @param pageSizes The page sizes for the first pages. The last provided size is the size of all following pages.
     * @param pageNavLength The length of the "Google"-like page navigation. Should be an odd number.
     * @return the pagination configuration, or <code>null</code> if none of the provided parameters is not null.
     */
    public static I_CmsSearchConfigurationPagination create(
        String pageParam,
        List<Integer> pageSizes,
        Integer pageNavLength) {

        return (pageParam != null) || (pageSizes != null) || (pageNavLength != null)
        ? new CmsSearchConfigurationPagination(pageParam, pageSizes, pageNavLength)
        : null;

    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationPagination#getNumPages(long)
     */
    @Override
    public int getNumPages(long numFound) {

        int result = 1;
        for (int pageSize : m_pageSizes) {
            numFound -= pageSize;
            if (numFound <= 0) {
                return result;
            }
            result++;
        }
        // calculation is save, since numFound must be > 0 at that place.
        result += ((numFound - 1) / m_pageSizeAllRemainingPages);
        return result;

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
     *
     * @deprecated see {@link org.opencms.jsp.search.config.I_CmsSearchConfigurationPagination#getPageSize()}
     */
    @Deprecated
    @Override
    public int getPageSize() {

        return m_pageSizeAllRemainingPages;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationPagination#getPageSizes()
     */
    @Override
    public List<Integer> getPageSizes() {

        List<Integer> pageSizes = new ArrayList<>(m_pageSizes.size());
        for (Integer pageSize : m_pageSizes) {
            pageSizes.add(Integer.valueOf(pageSize.intValue()));
        }
        return pageSizes;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationPagination#getSizeOfPage(int)
     */
    @Override
    public int getSizeOfPage(int pageNum) {

        if (pageNum < 1) {
            throw new IllegalArgumentException(
                "You try to determine the size of page "
                    + pageNum
                    + ". But a valid page number must be greater than 0.");
        } else if (pageNum <= m_pageSizes.size()) {
            return m_pageSizes.get(pageNum - 1).intValue();
        } else {
            return m_pageSizeAllRemainingPages;
        }
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationPagination#getStartOfPage(int)
     */
    @Override
    public int getStartOfPage(int pageNum) {

        if (pageNum < 1) {
            throw new IllegalArgumentException(
                "The number of the page, you request the index of the first item for must be greater than 0, but is \""
                    + pageNum
                    + "\".");
        }
        int result = 0;
        for (int i = 0; i < m_pageSizes.size(); i++) {
            if (pageNum > 1) {
                result += m_pageSizes.get(i).intValue();
                pageNum--;
            } else {
                return result;
            }
        }
        result += (pageNum - 1) * m_pageSizeAllRemainingPages;
        return result;
    }

}

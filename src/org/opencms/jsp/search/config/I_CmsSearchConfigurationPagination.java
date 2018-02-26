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

import org.opencms.jsp.search.controller.I_CmsSearchControllerPagination;

import java.util.List;

/** The interface a pagination configuration must implement. */
public interface I_CmsSearchConfigurationPagination {

    /**
     * Calculates the number of pages for the provided number of results.
     * @param numFound the number of results
     * @return the number of result pages for the provided number of results.
     */
    int getNumPages(long numFound);

    /** Returns the length of a "Google"-like navigation. Should typically be an odd number.
     * @return The length of a "Google"-like navigation.
     */
    int getPageNavLength();

    /** Returns the request parameter that should be used to send the current page.
     * @return The request parameter that should be used to send the current page.
     */
    String getPageParam();

    /** Returns the page size of pages that have not explicitely a size set.
     *
     * That means, if you specify the sizes [5,8], meaning the first page should have 5 entries, all others 8,
     * the size 8 is returned by the method.
     *
     * To get the size of a specific page use {@link I_CmsSearchConfigurationPagination#getSizeOfPage(int)}.
     *
     * @return The page size of pages that have not explicitely a size set.
     *
     * @deprecated use either {@link I_CmsSearchConfigurationPagination#getSizeOfPage(int)} to get the size
     * for a specific page or {@link I_CmsSearchControllerPagination#getCurrentPageSize()} to get the size
     * of the current page.
     */
    @Deprecated
    int getPageSize();

    /** Returns the page sizes as configured for the first pages of the search.
     * The last provided size is the size of all following pages.
     * @return the configured page sizes for the first pages.
     */
    List<Integer> getPageSizes();

    /** Returns the page size for the provided page.
     * @param pageNum the number of the page (starting with 1) for which the size should be returned.
     * @return The page size for the provided page.
     */
    int getSizeOfPage(int pageNum);

    /**
     * Returns the index of the first item to show on the given page.
     * @param pageNum the number of the page, for which the index of the first item to show on is requested.
     * @return the index of the first item to show on the provided page.
     */
    int getStartOfPage(int pageNum);

}

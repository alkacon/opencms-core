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

import java.util.List;

/** Configuration for sorting in general. */
public class CmsSearchConfigurationSorting implements I_CmsSearchConfigurationSorting {

    /** Default request parameter holding the selected sort option. */
    public static final String DEFAULT_SORT_PARAM = "sort";
    /** The request parameter used to send the currently chosen search option. */
    private final String m_sortParam;
    /** The available sort options. */
    private final List<I_CmsSearchConfigurationSortOption> m_options;

    /** The default sort option. */
    private final I_CmsSearchConfigurationSortOption m_defaultOption;

    /** Constructor setting all options.
     * @param sortParam The request parameter used to send the currently chosen search option.
     * @param options The available sort options.
     * @param defaultOption The default sort option.
     */
    public CmsSearchConfigurationSorting(
        final String sortParam,
        final List<I_CmsSearchConfigurationSortOption> options,
        final I_CmsSearchConfigurationSortOption defaultOption) {

        m_sortParam = sortParam == null ? DEFAULT_SORT_PARAM : sortParam;
        m_options = options;
        m_defaultOption = defaultOption;
    }

    /** Creates a sort configuration iff at least one of the parameters is not null and the options list is not empty.
     * @param sortParam The request parameter used to send the currently chosen search option.
     * @param options The available sort options.
     * @param defaultOption The default sort option.
     * @return the sort configuration or null, depending on the arguments.
     */
    public static CmsSearchConfigurationSorting create(
        final String sortParam,
        final List<I_CmsSearchConfigurationSortOption> options,
        final I_CmsSearchConfigurationSortOption defaultOption) {

        return (null != sortParam) || ((null != options) && !options.isEmpty()) || (null != defaultOption)
        ? new CmsSearchConfigurationSorting(sortParam, options, defaultOption)
        : null;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationSorting#getDefaultSortOption()
     */
    public I_CmsSearchConfigurationSortOption getDefaultSortOption() {

        return m_defaultOption;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationSorting#getSortOptions()
     */
    @Override
    public List<I_CmsSearchConfigurationSortOption> getSortOptions() {

        return m_options;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationSorting#getSortParam()
     */
    @Override
    public String getSortParam() {

        return m_sortParam;
    }

}

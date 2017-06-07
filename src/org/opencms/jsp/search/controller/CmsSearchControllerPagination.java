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

package org.opencms.jsp.search.controller;

import org.opencms.jsp.search.config.I_CmsSearchConfigurationPagination;
import org.opencms.jsp.search.state.CmsSearchStatePagination;
import org.opencms.jsp.search.state.I_CmsSearchStatePagination;
import org.opencms.search.solr.CmsSolrQuery;

import java.util.Map;

/** Controller for the pagination. */
public class CmsSearchControllerPagination implements I_CmsSearchControllerPagination {

    /** The default page to show. */
    private static final int DEFAULT_PAGE = 1;
    /** The configuration of the pagination. */
    private final I_CmsSearchConfigurationPagination m_config;
    /** The state of the pagination. */
    private final I_CmsSearchStatePagination m_state;

    /** Constructor taking a pagination configuration.
     * @param config The pagination configuration.
     */
    public CmsSearchControllerPagination(final I_CmsSearchConfigurationPagination config) {

        m_config = config;
        m_state = new CmsSearchStatePagination();
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#addParametersForCurrentState(java.util.Map)
     */
    @Override
    public void addParametersForCurrentState(final Map<String, String[]> parameters) {

        if (!(m_state.getCurrentPage() == DEFAULT_PAGE)) {
            parameters.put(m_config.getPageParam(), new String[] {Integer.toString(m_state.getCurrentPage())});
        }
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#addQueryParts(CmsSolrQuery)
     */
    @Override
    public void addQueryParts(CmsSolrQuery query) {

        query.setRows(Integer.valueOf(m_config.getPageSize()));
        final int start = (m_state.getCurrentPage() - 1) * m_config.getPageSize();
        query.setStart(Integer.valueOf(start));
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchControllerPagination#getConfig()
     */
    @Override
    public I_CmsSearchConfigurationPagination getConfig() {

        return m_config;
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchControllerPagination#getState()
     */
    @Override
    public I_CmsSearchStatePagination getState() {

        return m_state;
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#updateForQueryChange()
     */
    @Override
    public void updateForQueryChange() {

        m_state.setIgnorePage(true);

    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#updateFromRequestParameters(java.util.Map, boolean)
     */
    @Override
    public void updateFromRequestParameters(final Map<String, String[]> parameters, boolean isReloaded) {

        if (!m_state.getIgnorePage() && parameters.containsKey(m_config.getPageParam())) {
            final String[] page = parameters.get(m_config.getPageParam());
            if (page.length > 0) {
                try {
                    m_state.setCurrentPage(Integer.valueOf(page[0]).intValue());
                    return;
                } catch (@SuppressWarnings("unused") final NumberFormatException e) {
                    m_state.setCurrentPage(DEFAULT_PAGE);
                }
            }
        }
        m_state.setCurrentPage(DEFAULT_PAGE);
    }
}

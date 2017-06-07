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

import org.opencms.jsp.search.config.I_CmsSearchConfigurationSortOption;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationSorting;
import org.opencms.jsp.search.state.CmsSearchStateSorting;
import org.opencms.jsp.search.state.I_CmsSearchStateSorting;
import org.opencms.search.solr.CmsSolrQuery;

import java.util.Map;

/** Controller for sorting options. */
public class CmsSearchControllerSorting implements I_CmsSearchControllerSorting {

    /** The sorting configuration. */
    private final I_CmsSearchConfigurationSorting m_config;
    /** The state for sorting (chosen option). */
    private final I_CmsSearchStateSorting m_state;

    /** Constructor taking a sorting configuration.
     * @param config The sorting configuration.
     */
    public CmsSearchControllerSorting(final I_CmsSearchConfigurationSorting config) {

        m_config = config;
        m_state = new CmsSearchStateSorting();
        m_state.setSelectedOption(m_config.getDefaultSortOption());
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#addParametersForCurrentState(java.util.Map)
     */
    @Override
    public void addParametersForCurrentState(final Map<String, String[]> parameters) {

        if ((null != m_state.getSelected()) && (m_config.getDefaultSortOption() != m_state.getCheckSelected())) {
            parameters.put(m_config.getSortParam(), new String[] {m_state.getSelected().getParamValue()});
        }
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#addQueryParts(CmsSolrQuery)
     */
    @Override
    public void addQueryParts(CmsSolrQuery query) {

        if (m_state.getSelected() != null) {
            query.set("sort", m_state.getSelected().getSolrValue());
        }
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchControllerSorting#getConfig()
     */
    @Override
    public I_CmsSearchConfigurationSorting getConfig() {

        return m_config;
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchControllerSorting#getState()
     */
    @Override
    public I_CmsSearchStateSorting getState() {

        return m_state;
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#updateForQueryChange()
     */
    @Override
    public void updateForQueryChange() {

        // do nothing

    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#updateFromRequestParameters(java.util.Map, boolean)
     */
    @Override
    public void updateFromRequestParameters(final Map<String, String[]> parameters, boolean isReloaded) {

        if (parameters.containsKey(m_config.getSortParam())) {
            final String[] sortValues = parameters.get(m_config.getSortParam());
            if (sortValues.length > 0) {
                final String sortValue = sortValues[0];
                for (final I_CmsSearchConfigurationSortOption sortOption : m_config.getSortOptions()) {
                    if (sortOption.getParamValue().equals(sortValue)) {
                        m_state.setSelectedOption(sortOption);
                        return;
                    }
                }
            }
        }
    }

}

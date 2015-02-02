/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon;
import org.opencms.jsp.search.state.CmsSearchStateCommon;
import org.opencms.jsp.search.state.I_CmsSearchStateCommon;

import java.util.Map;

/** Search controller for the common search options. */
public class CmsSearchControllerCommon implements I_CmsSearchControllerCommon {

    /** Configuration of common search options. */
    private final I_CmsSearchConfigurationCommon m_config;
    /** State of the common search options. */
    private final I_CmsSearchStateCommon m_state;

    /** Constructor taking the managed configuration.
     * @param config The configuration to manage by the controller.
     */
    public CmsSearchControllerCommon(final I_CmsSearchConfigurationCommon config) {

        m_config = config;
        m_state = new CmsSearchStateCommon();
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#addParametersForCurrentState(java.util.Map)
     */
    @Override
    public void addParametersForCurrentState(final Map<String, String[]> parameters) {

        if (!m_state.getQuery().isEmpty()) {
            parameters.put(m_config.getQueryParam(), new String[] {m_state.getQuery()});
        }

    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#generateQuery()
     */
    @Override
    public String generateQuery() {

        final StringBuffer query = new StringBuffer();
        query.append("q=").append(m_state.getQuery());
        /*
         * if (m_config.getSolrIndex() != null) {
         * query.append("&index=").append(m_config.getSolrIndex()); } if
         * (m_config.getSolrCore() != null) {
         * query.append("&core=").append(m_config.getSolrCore()); }
         */
        if (!m_config.getExtraSolrParams().isEmpty()) {
            query.append('&').append(m_config.getExtraSolrParams());
        }
        return query.toString();
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchControllerCommon#getConfig()
     */
    @Override
    public I_CmsSearchConfigurationCommon getConfig() {

        return m_config;
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchControllerCommon#getState()
     */
    @Override
    public I_CmsSearchStateCommon getState() {

        return m_state;
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#updateForQueryChange()
     */
    @Override
    public void updateForQueryChange() {

        // Nothing to do

    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#updateFromRequestParameters(java.util.Map)
     */
    @Override
    public void updateFromRequestParameters(final Map<String, String[]> parameters) {

        if (parameters.containsKey(m_config.getQueryParam())) {
            final String[] queryStrings = parameters.get(m_config.getQueryParam());
            if (queryStrings.length > 0) {
                m_state.setQuery(queryStrings[0]);
                return;
            }
        }
        if (parameters.containsKey(m_config.getLastQueryParam())) {
            final String[] queryStrings = parameters.get(m_config.getLastQueryParam());
            if (queryStrings.length > 0) {
                m_state.setLastQuery(queryStrings[0]);
                return;
            }
        }
        m_state.setQuery("");
    }

}

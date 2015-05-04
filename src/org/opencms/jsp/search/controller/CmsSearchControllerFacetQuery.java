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

import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetQuery;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetQuery.I_CmsFacetQueryItem;
import org.opencms.jsp.search.state.CmsSearchStateFacet;
import org.opencms.jsp.search.state.I_CmsSearchStateFacet;

import java.util.Iterator;
import java.util.Map;

/** Search controller for the query facet options. */
public class CmsSearchControllerFacetQuery implements I_CmsSearchControllerFacetQuery {

    /** Configuration of the field facet options. */
    private final I_CmsSearchConfigurationFacetQuery m_config;
    /** State of the field facets. */
    private final I_CmsSearchStateFacet m_state;

    /** Constructor taking the managed configuration.
     * @param config The configuration to manage by the controller.
     */
    public CmsSearchControllerFacetQuery(final I_CmsSearchConfigurationFacetQuery config) {

        m_config = config;
        m_state = new CmsSearchStateFacet();
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#addParametersForCurrentState(java.util.Map)
     */
    @Override
    public void addParametersForCurrentState(final Map<String, String[]> parameters) {

        if (!m_state.getCheckedEntries().isEmpty()) {
            parameters.put(
                m_config.getParamKey(),
                m_state.getCheckedEntries().toArray(new String[m_state.getCheckedEntries().size()]));
        }
        if (m_state.getIgnoreChecked()) {
            parameters.put(m_config.getIgnoreMaxParamKey(), null);
        }

    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#generateQuery()
     */
    @Override
    public String generateQuery() {

        StringBuffer query;
        query = generateFacetPart();
        addFilterQueryParts(query);
        return query.toString();
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchControllerFacetQuery#getConfig()
     */
    @Override
    public I_CmsSearchConfigurationFacetQuery getConfig() {

        return m_config;
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchControllerFacetQuery#getState()
     */
    @Override
    public I_CmsSearchStateFacet getState() {

        return m_state;
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#updateForQueryChange()
     */
    @Override
    public void updateForQueryChange() {

        m_state.setIgnoreChecked(true);

    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#updateFromRequestParameters(java.util.Map, boolean)
     */
    @Override
    public void updateFromRequestParameters(final Map<String, String[]> parameters, boolean isReloaded) {

        m_state.setUseLimit(!parameters.containsKey(m_config.getIgnoreMaxParamKey()));

        if (isReloaded) {
            // update checked fields
            m_state.clearChecked();
            if (!m_state.getIgnoreChecked() && parameters.containsKey(m_config.getParamKey())) {
                final String[] checked = parameters.get(m_config.getParamKey());
                for (int i = 0; i < checked.length; i++) {
                    m_state.addChecked(checked[i]);
                }

            }
        } else { // use the preselection on first load
            for (String checked : m_config.getPreSelection()) {
                m_state.addChecked(checked);
            }
        }
    }

    /** Adds filter parts to the query.
     * @param query The query.
     */
    protected void addFilterQueryParts(final StringBuffer query) {

        if (!m_state.getCheckedEntries().isEmpty()) {
            final Iterator<String> fieldIterator = m_state.getCheckedEntries().iterator();
            query.append("&fq={!tag=").append(m_config.getName()).append("}(").append(fieldIterator.next());
            final String concater = m_config.getIsAndFacet() ? " AND " : " OR ";
            while (fieldIterator.hasNext()) {
                query.append(concater);
                query.append(fieldIterator.next());
            }
            query.append(')');
        }
    }

    /** Generate query part for the facet, without filters.
     * @return The query part for the facet
     */
    protected StringBuffer generateFacetPart() {

        final StringBuffer query = new StringBuffer("facet=true");
        for (I_CmsFacetQueryItem q : m_config.getQueryList()) {
            query.append("&facet.query={!ex=").append(m_config.getName()).append("}").append(q.getQuery());
        }
        return query;
    }

}

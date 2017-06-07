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

import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetField;
import org.opencms.jsp.search.state.CmsSearchStateFacet;
import org.opencms.jsp.search.state.I_CmsSearchStateFacet;
import org.opencms.search.solr.CmsSolrQuery;

import java.util.Iterator;
import java.util.Map;

/** Search controller for the field facet options. */
public class CmsSearchControllerFacetField implements I_CmsSearchControllerFacetField {

    /** Configuration of the field facet options. */
    private final I_CmsSearchConfigurationFacetField m_config;
    /** State of the field facets. */
    private final I_CmsSearchStateFacet m_state;

    /** Constructor taking the managed configuration.
     * @param config The configuration to manage by the controller.
     */
    public CmsSearchControllerFacetField(final I_CmsSearchConfigurationFacetField config) {

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
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#addQueryParts(CmsSolrQuery)
     */
    @Override
    public void addQueryParts(CmsSolrQuery query) {

        addFacetPart(query);
        addFilterQueryParts(query);
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchControllerFacetField#getConfig()
     */
    @Override
    public I_CmsSearchConfigurationFacetField getConfig() {

        return m_config;
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchControllerFacetField#getState()
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

    /** Adds the query parts for the facet options, except the filter parts.
     * @param query The query part that is extended with the facet options.
     * @param useLimit Flag, if the limit option should be set.
     */
    protected void addFacetOptions(StringBuffer query, final boolean useLimit) {

        // mincount
        if (m_config.getMinCount() != null) {
            appendFacetOption(query, "mincount", m_config.getMinCount().toString());
        }
        // limit
        if (useLimit && (m_config.getLimit() != null)) {
            appendFacetOption(query, "limit", m_config.getLimit().toString());
        }
        // sort
        if (m_config.getSortOrder() != null) {
            appendFacetOption(query, "sort", m_config.getSortOrder().toString());
        }
        // prefix
        if (!m_config.getPrefix().isEmpty()) {
            appendFacetOption(query, "prefix", m_config.getPrefix());
        }
    }

    /** Generate query part for the facet, without filters.
     * @param query The query, where the facet part should be added
     */
    protected void addFacetPart(CmsSolrQuery query) {

        StringBuffer value = new StringBuffer();
        value.append("{!key=").append(m_config.getName());
        addFacetOptions(value, m_state.getUseLimit());
        if (m_config.getIgnoreAllFacetFilters() || (!m_state.getCheckedEntries().isEmpty() && !m_config.getIsAndFacet())) {
            value.append(" ex=").append(m_config.getIgnoreTags());
        }
        value.append("}");
        value.append(m_config.getField());
        query.add("facet.field", value.toString());
    }

    /** Adds filter parts to the query.
     * @param query The query.
     */
    protected void addFilterQueryParts(CmsSolrQuery query) {

        if (!m_state.getCheckedEntries().isEmpty()) {
            StringBuffer value = new StringBuffer();
            value.append("{!tag=").append(m_config.getName()).append('}');
            value.append(m_config.getField());
            value.append(":(");
            final Iterator<String> fieldIterator = m_state.getCheckedEntries().iterator();
            value.append(m_config.modifyFilterQuery(fieldIterator.next()));
            final String concater = m_config.getIsAndFacet() ? " AND " : " OR ";
            while (fieldIterator.hasNext()) {
                value.append(concater);
                value.append(m_config.modifyFilterQuery(fieldIterator.next()));
            }
            value.append(')');
            query.add("fq", value.toString());
        }
    }

    /** Appends the query part for the facet to the query string.
     * @param query The current query string.
     * @param name The name of the facet parameter, e.g. "limit", "order", ....
     * @param value The value to set for the parameter specified by name.
     */
    protected void appendFacetOption(StringBuffer query, final String name, final String value) {

        query.append(" facet.").append(name).append("=").append(value);
    }

}

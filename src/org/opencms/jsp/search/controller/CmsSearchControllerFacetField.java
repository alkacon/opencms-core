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

import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetField;
import org.opencms.jsp.search.state.CmsSearchStateFacet;
import org.opencms.jsp.search.state.I_CmsSearchStateFacet;

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
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#updateFromRequestParameters(java.util.Map)
     */
    @Override
    public void updateFromRequestParameters(final Map<String, String[]> parameters) {

        // TODO: useLimit update - not yet implemented

        // update checked fields
        m_state.clearChecked();
        if (!m_state.getIgnoreChecked() && parameters.containsKey(m_config.getParamKey())) {
            final String[] checked = parameters.get(m_config.getParamKey());
            for (int i = 0; i < checked.length; i++) {
                m_state.addChecked(checked[i]);
            }

        }
    }

    /** Adds filter parts to the query.
     * @param query The query.
     */
    protected void addFilterQueryParts(final StringBuffer query) {

        if (!m_state.getCheckedEntries().isEmpty()) {
            query.append("&fq={!tag=").append(m_config.getField()).append('}');
            query.append(m_config.getField());
            query.append(":(");
            final Iterator<String> fieldIterator = m_state.getCheckedEntries().iterator();
            query.append(m_config.modifyFilterQuery(fieldIterator.next()));
            final String concater = m_config.getIsAndFacet() ? " AND " : " OR ";
            while (fieldIterator.hasNext()) {
                query.append(concater);
                query.append(m_config.modifyFilterQuery(fieldIterator.next()));
            }
            query.append(')');
        }
    }

    /** Appends the query part for the facet to the query string.
     * @param query The current query string.
     * @param fieldPrefix The facet's field used to identify the facet.
     * @param name The name of the facet parameter, e.g. "limit", "order", ....
     * @param value The value to set for the parameter specified by name.
     */
    protected void appendQueryPart(
        final StringBuffer query,
        final String fieldPrefix,
        final String name,
        final String value) {

        query.append("&");
        if ((fieldPrefix != null) && !fieldPrefix.trim().isEmpty()) {
            query.append("f.").append(fieldPrefix).append('.');
        }
        query.append("facet.").append(name).append("=").append(value);
    }

    /** Generates the query parts for the facet options, except the filter parts.
     * @param fieldPrefix The facet's field used to identify the facet.
     * @param useLimit Flag, if the limit option should be set.
     * @return The common query parts.
     */
    protected StringBuffer generateCommonQueryParts(final String fieldPrefix, final boolean useLimit) {

        final StringBuffer queryPart = new StringBuffer();
        // mincount
        if (m_config.getMinCount() != null) {
            appendQueryPart(queryPart, fieldPrefix, "mincount", m_config.getMinCount().toString());
        }
        // limit
        if (useLimit && (m_config.getLimit() != null)) {
            appendQueryPart(queryPart, fieldPrefix, "limit", m_config.getLimit().toString());
        }
        // sort
        if (m_config.getSortOrder() != null) {
            appendQueryPart(queryPart, fieldPrefix, "sort", m_config.getSortOrder().toString());
        }
        // prefix
        if (!m_config.getPrefix().isEmpty()) {
            appendQueryPart(queryPart, fieldPrefix, "prefix", m_config.getPrefix());
        }
        return queryPart;
    }

    /** Generate query part for the facet, without filters.
     * @return The query part for the facet
     */
    protected StringBuffer generateFacetPart() {

        final StringBuffer query = new StringBuffer();
        query.append("&facet.field=");
        if (!m_state.getCheckedEntries().isEmpty() && !m_config.getIsAndFacet()) {
            query.append("{!ex=").append(m_config.getField()).append('}');
        }
        query.append(m_config.getField());
        query.append(generateCommonQueryParts(m_config.getField(), m_state.getUseLimit()));
        return query;
    }

}

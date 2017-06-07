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

import org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon;
import org.opencms.jsp.search.state.CmsSearchStateCommon;
import org.opencms.jsp.search.state.I_CmsSearchStateCommon;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.util.CmsRequestUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.util.ClientUtils;

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
        parameters.put(m_config.getReloadedParam(), null);
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#addQueryParts(CmsSolrQuery)
     */
    @Override
    public void addQueryParts(CmsSolrQuery query) {

        String queryString = m_state.getQuery();
        if (!m_config.getIgnoreQueryParam()) {
            if (m_config.getEscapeQueryChars()) {
                queryString = ClientUtils.escapeQueryChars(queryString);
            }
            if (queryString.isEmpty() && m_config.getSearchForEmptyQueryParam()) {
                queryString = "*";
            }
            String modifiedQuery = m_config.getModifiedQuery(queryString);
            if (modifiedQuery.startsWith("{!")) {
                modifiedQuery = "{!tag=q " + modifiedQuery.substring(2);
            } else {
                modifiedQuery = "{!tag=q}" + modifiedQuery;
            }
            query.set("q", modifiedQuery);
        }

        if (m_config.getSolrIndex() != null) {
            query.set("index", m_config.getSolrIndex());
        }
        if (m_config.getSolrCore() != null) {
            query.set("core", m_config.getSolrCore());
        }

        if (!m_config.getExtraSolrParams().isEmpty()) {
            Map<String, String[]> extraParamsMap = CmsRequestUtil.createParameterMap(m_config.getExtraSolrParams());
            for (String key : extraParamsMap.keySet()) {
                for (String value : Arrays.asList(extraParamsMap.get(key))) {
                    if (SET_VARIABLES.contains(key)) {
                        query.set(key, value);
                    } else {
                        query.add(key, value);
                    }
                }
            }
        }
        for (String additionalParam : m_state.getAdditionalParameters().keySet()) {
            String additionalParamString = resolveMacros(
                m_config.getAdditionalParameters().get(additionalParam),
                m_state.getAdditionalParameters().get(additionalParam));
            Map<String, String[]> extraParamsMap = CmsRequestUtil.createParameterMap(additionalParamString);
            for (String key : extraParamsMap.keySet()) {
                for (String value : Arrays.asList(extraParamsMap.get(key))) {
                    if (SET_VARIABLES.contains(key)) {
                        query.set(key, value);
                    } else {
                        query.add(key, value);
                    }
                }
            }
        }
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
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#updateFromRequestParameters(java.util.Map, boolean)
     */
    @Override
    public void updateFromRequestParameters(final Map<String, String[]> parameters, boolean isReloaded) {

        m_state.setQuery("");

        if (parameters.containsKey(m_config.getQueryParam())) {
            final String[] queryStrings = parameters.get(m_config.getQueryParam());
            if (queryStrings.length > 0) {
                m_state.setQuery(queryStrings[0]);
            }
        }
        if (parameters.containsKey(m_config.getLastQueryParam())) {
            final String[] queryStrings = parameters.get(m_config.getLastQueryParam());
            if (queryStrings.length > 0) {
                m_state.setLastQuery(queryStrings[0]);
            }
        }
        // Set state for additional query parameters
        Map<String, String> additionalParameters = new HashMap<String, String>(
            m_config.getAdditionalParameters().size());
        for (String key : m_config.getAdditionalParameters().keySet()) {
            if (parameters.containsKey(key)
                && ((parameters.get(key).length > 0) && (parameters.get(key)[0].length() > 0))) {
                additionalParameters.put(key, parameters.get(key)[0]);
            }
        }
        m_state.setAdditionalParameters(additionalParameters);
    }

    /** Replaces the %(value) macro accordingly.
     * @param string The String where the macros should be replaced.
     * @param value The value used for the replacement.
     * @return The original String with %(value) macros replaced.
     */
    private String resolveMacros(final String string, final String value) {

        return string.replaceAll("\\%\\(value\\)", value);
    }
}

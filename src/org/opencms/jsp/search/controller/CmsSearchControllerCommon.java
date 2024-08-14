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

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon;
import org.opencms.jsp.search.state.CmsSearchStateCommon;
import org.opencms.jsp.search.state.I_CmsSearchStateCommon;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.util.CmsRequestUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.CommonParams;

/** Search controller for the common search options. */
public class CmsSearchControllerCommon implements I_CmsSearchControllerCommon {

    /** Value macro. */
    private static final String MACRO_VALUE = "value";
    /** Site root macro. */
    private static final String MACRO_SITE_ROOT = "site_root";
    /** Locale macro. */
    private static final String MACRO_LOCALE = "locale";
    /** Query macro. */
    private static final String MACRO_QUERY = "query";
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
        for (Entry<String, String> e : m_state.getAdditionalParameters().entrySet()) {
            parameters.put(e.getKey(), new String[] {e.getValue()});
        }
    }

    /**
     * @see org.opencms.jsp.search.controller.I_CmsSearchController#addQueryParts(CmsSolrQuery, CmsObject)
     */
    @Override
    public void addQueryParts(CmsSolrQuery query, CmsObject cms) {

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
            String currentSiteRoot = null == cms ? null : cms.getRequestContext().getSiteRoot();
            if ((null != currentSiteRoot) && !currentSiteRoot.endsWith("/")) {
                currentSiteRoot = currentSiteRoot + "/";
            }
            String currentLocale = (null == cms
            ? CmsLocaleManager.getDefaultLocale()
            : cms.getRequestContext().getLocale()).toString();
            String extraParams = m_config.getExtraSolrParams();
            extraParams = resolveMacro(extraParams, MACRO_SITE_ROOT, currentSiteRoot);
            extraParams = resolveMacro(extraParams, MACRO_LOCALE, currentLocale);
            extraParams = resolveMacro(extraParams, MACRO_QUERY, queryString);
            Map<String, String[]> extraParamsMap = CmsRequestUtil.createParameterMap(extraParams, true, null);
            for (String key : extraParamsMap.keySet()) {
                for (String value : Arrays.asList(extraParamsMap.get(key))) {
                    if (SET_VARIABLES.contains(key)) {
                        if (key.equals(CommonParams.FL)) {
                            query.setReturnFields(value);
                        } else {
                            query.set(key, value);
                        }
                    } else {
                        query.add(key, value);
                    }
                }
            }
        }
        for (String additionalParam : m_state.getAdditionalParameters().keySet()) {
            String solrValue = m_config.getAdditionalParameters().get(additionalParam);
            if (null != solrValue) {
                String additionalParamString = resolveMacro(
                    solrValue,
                    MACRO_VALUE,
                    CmsEncoder.encode(m_state.getAdditionalParameters().get(additionalParam), null));
                Map<String, String[]> extraParamsMap = CmsRequestUtil.createParameterMap(
                    additionalParamString,
                    true,
                    null);
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
        // Add timezone query parameter to allow for correct date/time handling if not already present.
        if (!query.getMap().keySet().contains("TZ")) {
            query.add("TZ", TimeZone.getDefault().getID());
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

    /**
     * Replaces the macro with the value, unless the value is <code>null</code>, then the macro is kept.
     *
     * @param string The String where the macros should be replaced.
     * @param value The value used for the replacement.
     * @param macroName The name of the macro to resolve.
     *
     * @return The original String with %(value) macros replaced.
     */
    private String resolveMacro(final String string, final String macroName, final String value) {

        return null != value ? string.replaceAll("\\%\\(" + Pattern.quote(macroName) + "\\)", value) : string;
    }
}

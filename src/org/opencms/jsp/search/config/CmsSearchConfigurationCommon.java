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

import java.util.HashMap;
import java.util.Map;

/**
 * Search configuration for common parameters as the query parameter etc.
 */
public class CmsSearchConfigurationCommon implements I_CmsSearchConfigurationCommon {

    /** The query request parameter. */
    private final String m_queryParam;
    /** The request parameter for the last query. */
    private final String m_lastQueryParam;
    /** A flag, indicating if special query characters should be escaped in the query string. */
    private final boolean m_escapeQueryChars;
    /** The request parameter send to indicate that this is not the first load of the search form. */
    private final String m_reloadedParam;
    /** A modifier for the search query. */
    private final String m_queryModifier;
    /** A flag, indicating if the empty query should be interpreted as "*:*" or if no search should be performed. */
    private final boolean m_searchForEmptyQuery;
    /** A flag, indicating if the query params should be used at all (or if the query is fixed). */
    private final boolean m_ignoreQuery;
    /** The Solr index to use for the query (specified by it's name). */
    private final String m_solrIndex;
    /** The Solr core to use for the query (specified by it's name). */
    private final String m_solrCore;
    /** Extra parameters given to Solr, specified like "p1=v1&p2=v2". */
    private final String m_extraSolrParams;
    /** Additional request parameters mapped to their Solr query parts. */
    private final Map<String, String> m_additionalParameters;
    /** Flag, indicating if the release date should be ignored. */
    private boolean m_ignoreReleaseDate;
    /** Flag, indicating if the expiration date should be ignored. */
    private boolean m_ignoreExpirationDate;

    /** Constructor for the common search configuration, where all configuration parameters are provided.
     * @param queryParam The query request param used by the search form.
     * @param lastQueryParam The last-query request param used by the search form.
     * @param escapeQueryChars A flag, indicating if special query characters in the query string should be escaped (default <code>true</code>).
     * @param reloadedParam The first-call request param used by the search form.
     * @param seachForEmptyQuery A flag, indicating if the empty query should be interpreted as "*:*" or if no search should be performed.
     * @param ignoreQuery A flag, indicating if the query param's values should be used for Solr query generation.
     * @param queryModifier Modifier for the given query string.
     * @param solrIndex The Solr index that should be used for the search.
     * @param solrCore The Solr core that should be used for the search.
     * @param extraSolrParams Extra params that are directly appended to each search query.
     * @param additionalParameters A map from additional request parameters to Solr query parts.
     * @param ignoreReleaseDate A flag, indicating if the release date should be ignored.
     * @param ignoreExpirationDate A flag, indicating if the expiration date should be ignored.
     */
    public CmsSearchConfigurationCommon(
        final String queryParam,
        final String lastQueryParam,
        final Boolean escapeQueryChars,
        final String reloadedParam,
        final Boolean seachForEmptyQuery,
        final Boolean ignoreQuery,
        final String queryModifier,
        final String solrIndex,
        final String solrCore,
        final String extraSolrParams,
        final Map<String, String> additionalParameters,
        final Boolean ignoreReleaseDate,
        final Boolean ignoreExpirationDate) {

        m_queryParam = queryParam;
        m_lastQueryParam = lastQueryParam;
        m_escapeQueryChars = escapeQueryChars != null ? escapeQueryChars.booleanValue() : true;
        m_reloadedParam = reloadedParam;
        m_searchForEmptyQuery = seachForEmptyQuery != null ? seachForEmptyQuery.booleanValue() : false;
        m_ignoreQuery = ignoreQuery != null ? ignoreQuery.booleanValue() : false;
        m_queryModifier = queryModifier;
        m_solrIndex = solrIndex;
        m_solrCore = solrCore;
        m_extraSolrParams = extraSolrParams == null ? "" : extraSolrParams;
        m_additionalParameters = additionalParameters != null ? additionalParameters : new HashMap<String, String>();
        m_ignoreReleaseDate = null == ignoreReleaseDate ? false : ignoreReleaseDate.booleanValue();
        m_ignoreExpirationDate = null == ignoreExpirationDate ? false : ignoreExpirationDate.booleanValue();
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon#getAdditionalParameters()
     */
    @Override
    public Map<String, String> getAdditionalParameters() {

        return m_additionalParameters;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon#getEscapeQueryChars()
     */
    public boolean getEscapeQueryChars() {

        return m_escapeQueryChars;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon#getExtraSolrParams()
     */
    @Override
    public String getExtraSolrParams() {

        return m_extraSolrParams;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon#getIgnoreExpirationDate()
     */
    public boolean getIgnoreExpirationDate() {

        return m_ignoreExpirationDate;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon#getIgnoreQueryParam()
     */
    public boolean getIgnoreQueryParam() {

        return m_ignoreQuery;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon#getIgnoreReleaseDate()
     */
    public boolean getIgnoreReleaseDate() {

        return m_ignoreReleaseDate;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon#getLastQueryParam()
     */
    @Override
    public String getLastQueryParam() {

        return m_lastQueryParam;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon#getModifiedQuery(java.lang.String)
     */
    @Override
    public String getModifiedQuery(String queryString) {

        if (null != m_queryModifier) {
            return m_queryModifier.replace("%(query)", queryString);
        }
        return queryString;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon#getQueryParam()
     */
    @Override
    public String getQueryParam() {

        return m_queryParam;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon#getReloadedParam()
     */
    public String getReloadedParam() {

        return m_reloadedParam;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon#getSearchForEmptyQueryParam()
     */
    @Override
    public boolean getSearchForEmptyQueryParam() {

        return m_searchForEmptyQuery;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon#getSolrCore()
     */
    @Override
    public String getSolrCore() {

        return m_solrCore;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon#getSolrIndex()
     */
    @Override
    public String getSolrIndex() {

        return m_solrIndex;
    }

}

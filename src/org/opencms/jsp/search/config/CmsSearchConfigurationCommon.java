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

package org.opencms.jsp.search.config;

/**
 * Search configuration for common parameters as the query parameter etc.
 */
public class CmsSearchConfigurationCommon implements I_CmsSearchConfigurationCommon {

    /** The query request parameter. */
    private final String m_queryParam;
    /** The request parameter for the last query. */
    private final String m_lastQueryParam;
    /** A flag, indicating if the query params should be used at all (or if the query is fixed). */
    private final boolean m_ignoreQuery;
    /** The Solr index to use for the query (specified by it's name). */
    private final String m_solrIndex;
    /** The Solr core to use for the query (specified by it's name). */
    private final String m_solrCore;
    /** Extra parameters given to Solr, specified like "p1=v1&p2=v2". */
    private final String m_extraSolrParams;

    /** Constructor for the common search configuration, where all configuration parameters are provided.
     * @param queryParam The query request param used by the search form.
     * @param lastQueryParam The last-query request param used by the search form.
     * @param ignoreQuery A flag, indicating if the query param's values should be used for Solr query generation.
     * @param solrIndex The Solr index that should be used for the search.
     * @param solrCore The Solr core that should be used for the search.
     * @param extraSolrParams Extra params that are directly appended to each search query.
     */
    public CmsSearchConfigurationCommon(
        final String queryParam,
        final String lastQueryParam,
        final Boolean ignoreQuery,
        final String solrIndex,
        final String solrCore,
        final String extraSolrParams) {

        m_queryParam = queryParam;
        m_lastQueryParam = lastQueryParam;
        m_ignoreQuery = ignoreQuery != null ? ignoreQuery.booleanValue() : false;
        m_solrIndex = solrIndex;
        m_solrCore = solrCore;
        m_extraSolrParams = extraSolrParams == null ? "" : extraSolrParams;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon#getExtraSolrParams()
     */
    @Override
    public String getExtraSolrParams() {

        return m_extraSolrParams;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon#getIgnoreQueryParam()
     */
    public boolean getIgnoreQueryParam() {

        return m_ignoreQuery;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon#getLastQueryParam()
     */
    @Override
    public String getLastQueryParam() {

        return m_lastQueryParam;
    }

    /**
     * @see org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon#getQueryParam()
     */
    @Override
    public String getQueryParam() {

        return m_queryParam;
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

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

package org.opencms.jsp.search.config.parser;

import org.opencms.file.CmsObject;
import org.opencms.jsp.search.config.CmsSearchConfigurationCommon;
import org.opencms.jsp.search.config.I_CmsSearchConfiguration;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationDidYouMean;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetField;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetQuery;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetRange;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationGeoFilter;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationPagination;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationSorting;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.util.CmsPair;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;

/** Search configuration parser reading a configuration containing a plain Solr query.
 * Only fl might be added additionally. */
public class CmsPlainQuerySearchConfigurationParser implements I_CmsSearchConfigurationParser {

    /** Logger for the class. */
    protected static final Log LOG = CmsLog.getLog(CmsPlainQuerySearchConfigurationParser.class);

    /** The default return fields. */
    private static final String DEFAULT_FL = "id,path";

    /** The whole query string. */
    protected String m_queryString;

    /** The optional base configuration that should be changed by the JSON configuration. */
    private I_CmsSearchConfiguration m_baseConfig;

    /** Constructor taking the JSON as String.
     * @param query The query that is passed to Solr.
     */
    public CmsPlainQuerySearchConfigurationParser(String query) {

        this(query, null);
    }

    /** Constructor taking the JSON as String.
     * @param query The query that is passed to Solr (additional Solr params).
     * @param baseConfig A base configuration that is adjusted by the JSON configuration string.
     */
    public CmsPlainQuerySearchConfigurationParser(String query, I_CmsSearchConfiguration baseConfig) {

        if ((null != query) && !(query.startsWith("fl=") || query.contains("&fl="))) {
            query = query + "&fl=" + DEFAULT_FL;
        }
        m_queryString = query;
        m_baseConfig = baseConfig;

    }

    /**
     * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseCommon(CmsObject)
     */
    public I_CmsSearchConfigurationCommon parseCommon(CmsObject cms) {

        String queryString = m_queryString;
        CmsPair<String, String> idxExtract = extractParam(queryString, "index");
        CmsPair<String, String> coreExtract = extractParam(idxExtract.getFirst(), "core");
        CmsPair<String, String> maxResultsExtract = extractParam(coreExtract.getFirst(), "maxresults");
        String resString = maxResultsExtract.getSecond();
        String indexName = idxExtract.getSecond();
        if (null != indexName) {
            indexName = indexName.trim();
        }
        if (null == indexName) {
            indexName = cms.getRequestContext().getCurrentProject().isOnlineProject()
            ? CmsSolrIndex.DEFAULT_INDEX_NAME_ONLINE
            : CmsSolrIndex.DEFAULT_INDEX_NAME_OFFLINE;
        }
        Integer maxResNum = null;
        if (null != resString) {
            try {
                maxResNum = Integer.valueOf(resString);
            } catch (NumberFormatException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Ignoring param \"maxresults=" + resString + "\" since its not a valid integer.", e);
                }
            }
        }
        if (null == maxResNum) {
            try {
                CmsSolrIndex idx = OpenCms.getSearchManager().getIndexSolr(indexName);
                if (null != idx) {
                    maxResNum = Integer.valueOf(idx.getMaxProcessedResults());
                } else {
                    maxResNum = Integer.valueOf(CmsSolrIndex.MAX_RESULTS_UNLIMITED);
                }
            } catch (Throwable t) {
                // This is ok, it's allowed to have an external other index here.
                LOG.debug(
                    "Parsing plain search configuration for none-CmsSolrIndex "
                        + indexName
                        + ". Setting max processed results to unlimited.");
                maxResNum = Integer.valueOf(CmsSolrIndex.MAX_RESULTS_UNLIMITED);
            }
        }

        return new CmsSearchConfigurationCommon(
            null,
            null,
            null,
            null,
            Boolean.TRUE,
            Boolean.TRUE,
            null,
            indexName,
            coreExtract.getSecond(),
            maxResultsExtract.getFirst(),
            null,
            null,
            null,
            maxResNum.intValue());
    }

    /**
    * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseDidYouMean()
    */
    public I_CmsSearchConfigurationDidYouMean parseDidYouMean() {

        return null != m_baseConfig ? m_baseConfig.getDidYouMeanConfig() : null;
    }

    /**
     * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseFieldFacets()
     */
    public Map<String, I_CmsSearchConfigurationFacetField> parseFieldFacets() {

        return null != m_baseConfig ? m_baseConfig.getFieldFacetConfigs() : Collections.emptyMap();
    }

    /**
     * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseGeoFilter()
     */
    @Override
    public I_CmsSearchConfigurationGeoFilter parseGeoFilter() {

        return null;
    }

    /**
     * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseHighlighter()
     */
    public I_CmsSearchConfigurationHighlighting parseHighlighter() {

        return null != m_baseConfig ? m_baseConfig.getHighlighterConfig() : null;
    }

    /**
     * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parsePagination()
     */
    public I_CmsSearchConfigurationPagination parsePagination() {

        return null != m_baseConfig ? m_baseConfig.getPaginationConfig() : null;
    }

    /**
     * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseQueryFacet()
     */
    public I_CmsSearchConfigurationFacetQuery parseQueryFacet() {

        return null != m_baseConfig ? m_baseConfig.getQueryFacetConfig() : null;
    }

    /**
     * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseRangeFacets()
     */
    public Map<String, I_CmsSearchConfigurationFacetRange> parseRangeFacets() {

        return null != m_baseConfig ? m_baseConfig.getRangeFacetConfigs() : Collections.emptyMap();
    }

    /**
     * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseSorting()
     */
    public I_CmsSearchConfigurationSorting parseSorting() {

        return null != m_baseConfig ? m_baseConfig.getSortConfig() : null;
    }

    /**
     * Extracts the value of a parameter from a query string.
     *
     * For example, extractParam("a=foo&b=bar", "a") will return CmsPair("b=bar", "foo").
     * @param params the parameter string.
     * @param paramKey the key of the parameter to extract the value for.
     * @return a pair of "params without the extracted parameter" and the value of the extracted parameter.
     */
    CmsPair<String, String> extractParam(String params, String paramKey) {

        String extract = null;
        int beginIdx = params.indexOf(paramKey + "=");
        if (beginIdx >= 0) {
            String sub = params.substring(beginIdx + paramKey.length() + 1);
            int endIdx = sub.indexOf("&");
            if (endIdx >= 0) {
                extract = sub.substring(0, endIdx);
                params = params.substring(0, beginIdx) + sub.substring(endIdx + 1);
            } else {
                extract = sub;
                params = beginIdx > 0 ? params.substring(0, beginIdx - 1) : ""; // cut trailing '&'
            }
        }
        return new CmsPair<>(params, extract);

    }
}

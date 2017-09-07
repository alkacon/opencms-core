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

import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.search.config.CmsSearchConfigurationCommon;
import org.opencms.jsp.search.config.CmsSearchConfigurationDidYouMean;
import org.opencms.jsp.search.config.CmsSearchConfigurationFacetField;
import org.opencms.jsp.search.config.CmsSearchConfigurationFacetQuery;
import org.opencms.jsp.search.config.CmsSearchConfigurationFacetQuery.CmsFacetQueryItem;
import org.opencms.jsp.search.config.CmsSearchConfigurationFacetRange;
import org.opencms.jsp.search.config.CmsSearchConfigurationHighlighting;
import org.opencms.jsp.search.config.CmsSearchConfigurationPagination;
import org.opencms.jsp.search.config.CmsSearchConfigurationSortOption;
import org.opencms.jsp.search.config.CmsSearchConfigurationSorting;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationCommon;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationDidYouMean;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacet;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetField;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetQuery;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetQuery.I_CmsFacetQueryItem;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetRange;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationPagination;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationSortOption;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationSorting;
import org.opencms.main.CmsLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/** Search configuration parser reading JSON. */
public class CmsJSONSearchConfigurationParser implements I_CmsSearchConfigurationParser {

    /** Logger for the class. */
    protected static final Log LOG = CmsLog.getLog(CmsJSONSearchConfigurationParser.class);

    /** The keys that can be used in the JSON object */
    /** JSON keys for common options. */
    /** A JSON key. */
    public static final String JSON_KEY_QUERYPARAM = "queryparam";
    /** A JSON key. */
    public static final String JSON_KEY_LAST_QUERYPARAM = "lastqueryparam";
    /** A JSON key. */
    public static final String JSON_KEY_ESCAPE_QUERY_CHARACTERS = "escapequerychars";
    /** A JSON key. */
    public static final String JSON_KEY_RELOADED_PARAM = "reloadedparam";
    /** A JSON key. */
    public static final String JSON_KEY_SEARCH_FOR_EMPTY_QUERY = "searchforemptyquery";
    /** A JSON key. */
    public static final String JSON_KEY_IGNORE_QUERY = "ignorequery";
    /** A JSON key. */
    public static final String JSON_KEY_IGNORE_RELEASE_DATE = "ignoreReleaseDate";
    /** A JSON key. */
    public static final String JSON_KEY_IGNORE_EXPIRATION_DATE = "ignoreExpirationDate";
    /** A JSON key. */
    public static final String JSON_KEY_QUERY_MODIFIER = "querymodifier";
    /** A JSON key. */
    public static final String JSON_KEY_PAGEPARAM = "pageparam";
    /** A JSON key. */
    public static final String JSON_KEY_INDEX = "index";
    /** A JSON key. */
    public static final String JSON_KEY_CORE = "core";
    /** A JSON key. */
    public static final String JSON_KEY_EXTRASOLRPARAMS = "extrasolrparams";
    /** A JSON key. */
    public static final String JSON_KEY_ADDITIONAL_PARAMETERS = "additionalrequestparams";
    /** A JSON key. */
    public static final String JSON_KEY_ADDITIONAL_PARAMETERS_PARAM = "param";
    /** A JSON key. */
    public static final String JSON_KEY_ADDITIONAL_PARAMETERS_SOLRQUERY = "solrquery";
    /** A JSON key. */
    public static final String JSON_KEY_PAGESIZE = "pagesize";
    /** A JSON key. */
    public static final String JSON_KEY_PAGENAVLENGTH = "pagenavlength";
    /** JSON keys for facet configuration. */
    /** The JSON key for the sub-node with all field facet configurations. */
    public static final String JSON_KEY_FIELD_FACETS = "fieldfacets";
    /** The JSON key for the sub-node with all field facet configurations. */
    public static final String JSON_KEY_RANGE_FACETS = "rangefacets";
    /** The JSON key for the sub-node with the query facet configuration. */
    public static final String JSON_KEY_QUERY_FACET = "queryfacet";
    /** JSON keys for a single facet. */
    /** A JSON key. */
    public static final String JSON_KEY_FACET_LIMIT = "limit";
    /** A JSON key. */
    public static final String JSON_KEY_FACET_MINCOUNT = "mincount";
    /** A JSON key. */
    public static final String JSON_KEY_FACET_LABEL = "label";
    /** A JSON key. */
    public static final String JSON_KEY_FACET_FIELD = "field";
    /** A JSON key. */
    public static final String JSON_KEY_FACET_NAME = "name";
    /** A JSON key. */
    public static final String JSON_KEY_FACET_PREFIX = "prefix";
    /** A JSON key. */
    public static final String JSON_KEY_FACET_ORDER = "order";
    /** A JSON key. */
    public static final String JSON_KEY_FACET_FILTERQUERYMODIFIER = "filterquerymodifier";
    /** A JSON key. */
    public static final String JSON_KEY_FACET_ISANDFACET = "isAndFacet";
    /** A JSON key. */
    public static final String JSON_KEY_FACET_IGNOREALLFACETFILTERS = "ignoreAllFacetFilters";
    /** A JSON key. */
    public static final String JSON_KEY_FACET_PRESELECTION = "preselection";
    /** A JSON key. */
    public static final String JSON_KEY_RANGE_FACET_RANGE = "range";
    /** A JSON key. */
    public static final String JSON_KEY_RANGE_FACET_START = "start";
    /** A JSON key. */
    public static final String JSON_KEY_RANGE_FACET_END = "end";
    /** A JSON key. */
    public static final String JSON_KEY_RANGE_FACET_GAP = "gap";
    /** A JSON key. */
    public static final String JSON_KEY_RANGE_FACET_OTHER = "other";
    /** A JSON key. */
    public static final String JSON_KEY_RANGE_FACET_HARDEND = "hardend";
    /** A JSON key. */
    public static final String JSON_KEY_QUERY_FACET_QUERY = "queryitems";
    /** A JSON key. */
    public static final String JSON_KEY_QUERY_FACET_QUERY_QUERY = "query";
    /** A JSON key. */
    public static final String JSON_KEY_QUERY_FACET_QUERY_LABEL = "label";

    /** JSON keys for sort options. */
    /** A JSON key. */
    public static final String JSON_KEY_SORTPARAM = "sortby";
    /** The JSON key for the sub-node with all search option configurations. */
    public static final String JSON_KEY_SORTOPTIONS = "sortoptions";
    /** JSON keys for a single search option. */
    /** A JSON key. */
    public static final String JSON_KEY_SORTOPTION_LABEL = "label";
    /** A JSON key. */
    public static final String JSON_KEY_SORTOPTION_PARAMVALUE = "paramvalue";
    /** A JSON key. */
    public static final String JSON_KEY_SORTOPTION_SOLRVALUE = "solrvalue";
    /** JSON keys for the highlighting configuration. */
    /** The JSON key for the subnode of all highlighting configuration. */
    public static final String JSON_KEY_HIGHLIGHTER = "highlighter";
    /** A JSON key. */
    public static final String JSON_KEY_HIGHLIGHTER_FIELD = "field";
    /** A JSON key. */
    public static final String JSON_KEY_HIGHLIGHTER_SNIPPETS = "snippets";
    /** A JSON key. */
    public static final String JSON_KEY_HIGHLIGHTER_FRAGSIZE = "fragsize";
    /** A JSON key. */
    public static final String JSON_KEY_HIGHLIGHTER_ALTERNATE_FIELD = "alternateField";
    /** A JSON key. */
    public static final String JSON_KEY_HIGHLIGHTER_MAX_LENGTH_ALTERNATE_FIELD = "maxAlternateFieldLength";
    /** A JSON key. */
    public static final String JSON_KEY_HIGHLIGHTER_SIMPLE_PRE = "simple.pre";
    /** A JSON key. */
    public static final String JSON_KEY_HIGHLIGHTER_SIMPLE_POST = "simple.post";
    /** A JSON key. */
    public static final String JSON_KEY_HIGHLIGHTER_FORMATTER = "formatter";
    /** A JSON key. */
    public static final String JSON_KEY_HIGHLIGHTER_FRAGMENTER = "fragmenter";
    /** A JSON key. */
    public static final String JSON_KEY_HIGHLIGHTER_FASTVECTORHIGHLIGHTING = "useFastVectorHighlighting";

    /** JSON keys for "Did you mean ...?" */
    /** A JSON key. */
    public static final String JSON_KEY_DIDYOUMEAN = "didYouMean";
    /** The JSON key for the subnode of all "Did you mean?" configuration. */
    /** A JSON key. */
    public static final String JSON_KEY_DIDYOUMEAN_QUERYPARAM = "didYouMeanQueryParam";
    /** A JSON key. */
    public static final String JSON_KEY_DIDYOUMEAN_COLLATE = "didYouMeanCollate";
    /** A JSON key. */
    public static final String JSON_KEY_DIDYOUMEAN_COUNT = "didYouMeanCount";

    /** The default values. */
    /** A JSON key. */
    public static final String DEFAULT_QUERY_PARAM = "q";
    /** A JSON key. */
    public static final String DEFAULT_LAST_QUERY_PARAM = "lq";
    /** A JSON key. */
    public static final String DEFAULT_RELOADED_PARAM = "reloaded";
    /** A JSON key. */
    public static final String DEFAULT_SORT_PARAM = "sort";
    /** A JSON key. */
    public static final String DEFAULT_PAGE_PARAM = "page";
    /** A JSON key. */
    public static final Integer DEFAULT_PAGE_SIZE = Integer.valueOf(10);
    /** A JSON key. */
    public static final Integer DEFAULT_PAGENAVLENGTH = Integer.valueOf(5);

    /** The whole JSON file. */
    protected JSONObject m_configObject;

    /** Constructor taking the JSON as String.
     * @param json The JSON that should be parsed as String.
     * @throws JSONException Thrown if parsing fails.
     */
    public CmsJSONSearchConfigurationParser(String json)
    throws JSONException {

        init(json);
    }

    /** Helper for reading a mandatory String value list - throwing an Exception if parsing fails.
     * @param json The JSON object where the list should be read from.
     * @param key The key of the value to read.
     * @return The value from the JSON.
     * @throws JSONException thrown when parsing fails.
     */
    protected static List<String> parseMandatoryStringValues(JSONObject json, String key) throws JSONException {

        List<String> list = null;
        JSONArray array = json.getJSONArray(key);
        list = new ArrayList<String>(array.length());
        for (int i = 0; i < array.length(); i++) {
            try {
                String entry = array.getString(i);
                list.add(entry);
            } catch (JSONException e) {
                LOG.info(Messages.get().getBundle().key(Messages.LOG_OPTIONAL_STRING_ENTRY_UNPARSABLE_1, key), e);
            }
        }
        return list;
    }

    /** Helper for reading an optional Boolean value - returning <code>null</code> if parsing fails.
     * @param json The JSON object where the value should be read from.
     * @param key The key of the value to read.
     * @return The value from the JSON, or <code>null</code> if the value does not exist, or is no Boolean.
     */
    protected static Boolean parseOptionalBooleanValue(JSONObject json, String key) {

        try {
            return Boolean.valueOf(json.getBoolean(key));
        } catch (JSONException e) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_OPTIONAL_BOOLEAN_MISSING_1, key), e);
            return null;
        }
    }

    /** Helper for reading an optional Integer value - returning <code>null</code> if parsing fails.
     * @param json The JSON object where the value should be read from.
     * @param key The key of the value to read.
     * @return The value from the JSON, or <code>null</code> if the value does not exist, or is no Integer.
     */
    protected static Integer parseOptionalIntValue(JSONObject json, String key) {

        try {
            return Integer.valueOf(json.getInt(key));
        } catch (JSONException e) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_OPTIONAL_INTEGER_MISSING_1, key), e);
            return null;
        }
    }

    /** Helper for reading an optional String value - returning <code>null</code> if parsing fails.
     * @param json The JSON object where the value should be read from.
     * @param key The key of the value to read.
     * @return The value from the JSON, or <code>null</code> if the value does not exist.
     */
    protected static String parseOptionalStringValue(JSONObject json, String key) {

        try {
            return json.getString(key);
        } catch (JSONException e) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_OPTIONAL_STRING_MISSING_1, key), e);
            return null;
        }
    }

    /** Helper for reading an optional String value list - returning <code>null</code> if parsing fails for the whole list, otherwise just skipping unparsable entries.
     * @param json The JSON object where the list should be read from.
     * @param key The key of the value to read.
     * @return The value from the JSON, or <code>null</code> if the value does not exist.
     */
    protected static List<String> parseOptionalStringValues(JSONObject json, String key) {

        List<String> list = null;
        try {
            list = parseMandatoryStringValues(json, key);
        } catch (JSONException e) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_OPTIONAL_STRING_LIST_MISSING_1, key), e);
            return null;
        }
        return list;
    }

    /**
     * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseCommon()
     */
    @Override
    public I_CmsSearchConfigurationCommon parseCommon() {

        return new CmsSearchConfigurationCommon(
            getQueryParam(),
            getLastQueryParam(),
            getEscapeQueryChars(),
            getFirstCallParam(),
            getSearchForEmptyQuery(),
            getIgnoreQuery(),
            getQueryModifier(),
            getIndex(),
            getCore(),
            getExtraSolrParams(),
            getAdditionalParameters(),
            getIgnoreReleaseDate(),
            getIgnoreExpirationDate());
    }

    /**
     * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseDidYouMean()
     */
    public I_CmsSearchConfigurationDidYouMean parseDidYouMean() {

        try {
            JSONObject didYouMean = m_configObject.getJSONObject(JSON_KEY_DIDYOUMEAN);
            String param = parseOptionalStringValue(didYouMean, JSON_KEY_DIDYOUMEAN_QUERYPARAM);
            // default to the normal query param
            if (null == param) {
                param = getQueryParam();
            }
            Boolean collate = parseOptionalBooleanValue(didYouMean, JSON_KEY_DIDYOUMEAN_COLLATE);
            Integer count = parseOptionalIntValue(didYouMean, JSON_KEY_DIDYOUMEAN_COUNT);
            return new CmsSearchConfigurationDidYouMean(param, collate, count);

        } catch (JSONException e) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_NO_HIGHLIGHTING_CONFIG_0), e);
            return null;
        }

    }

    /**
     * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseFieldFacets()
     */
    @Override
    public Map<String, I_CmsSearchConfigurationFacetField> parseFieldFacets() {

        Map<String, I_CmsSearchConfigurationFacetField> facetConfigs = new LinkedHashMap<String, I_CmsSearchConfigurationFacetField>();
        try {
            JSONArray fieldFacets = m_configObject.getJSONArray(JSON_KEY_FIELD_FACETS);
            for (int i = 0; i < fieldFacets.length(); i++) {

                I_CmsSearchConfigurationFacetField config = parseFieldFacet(fieldFacets.getJSONObject(i));
                if (config != null) {
                    facetConfigs.put(config.getName(), config);
                }
            }
        } catch (JSONException e) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_NO_FACET_CONFIG_0), e);
        }
        return facetConfigs;
    }

    /**
     * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseHighlighter()
     */
    @Override
    public I_CmsSearchConfigurationHighlighting parseHighlighter() {

        try {
            JSONObject highlighter = m_configObject.getJSONObject(JSON_KEY_HIGHLIGHTER);
            String field = highlighter.getString(JSON_KEY_HIGHLIGHTER_FIELD);
            Integer snippets = parseOptionalIntValue(highlighter, JSON_KEY_HIGHLIGHTER_SNIPPETS);
            Integer fragsize = parseOptionalIntValue(highlighter, JSON_KEY_HIGHLIGHTER_FRAGSIZE);
            String alternateField = parseOptionalStringValue(highlighter, JSON_KEY_HIGHLIGHTER_ALTERNATE_FIELD);
            Integer maxAlternateFieldLength = parseOptionalIntValue(
                highlighter,
                JSON_KEY_HIGHLIGHTER_MAX_LENGTH_ALTERNATE_FIELD);
            String pre = parseOptionalStringValue(highlighter, JSON_KEY_HIGHLIGHTER_SIMPLE_PRE);
            String post = parseOptionalStringValue(highlighter, JSON_KEY_HIGHLIGHTER_SIMPLE_POST);
            String formatter = parseOptionalStringValue(highlighter, JSON_KEY_HIGHLIGHTER_FORMATTER);
            String fragmenter = parseOptionalStringValue(highlighter, JSON_KEY_HIGHLIGHTER_FRAGMENTER);
            Boolean useFastVectorHighlighting = parseOptionalBooleanValue(
                highlighter,
                JSON_KEY_HIGHLIGHTER_FASTVECTORHIGHLIGHTING);
            return new CmsSearchConfigurationHighlighting(
                field,
                snippets,
                fragsize,
                alternateField,
                maxAlternateFieldLength,
                pre,
                post,
                formatter,
                fragmenter,
                useFastVectorHighlighting);
        } catch (JSONException e) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_NO_HIGHLIGHTING_CONFIG_0), e);
            return null;
        }
    }

    /**
     * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parsePagination()
     */
    @Override
    public I_CmsSearchConfigurationPagination parsePagination() {

        return new CmsSearchConfigurationPagination(getPageParam(), getPageSize(), getPageNavLength());
    }

    /**
     * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseQueryFacet()
     */
    @Override
    public I_CmsSearchConfigurationFacetQuery parseQueryFacet() {

        try {
            JSONObject queryFacetObject = m_configObject.getJSONObject(JSON_KEY_QUERY_FACET);
            try {
                List<I_CmsFacetQueryItem> queries = parseFacetQueryItems(queryFacetObject);
                String label = parseOptionalStringValue(queryFacetObject, JSON_KEY_FACET_LABEL);
                Boolean isAndFacet = parseOptionalBooleanValue(queryFacetObject, JSON_KEY_FACET_ISANDFACET);
                List<String> preselection = parseOptionalStringValues(queryFacetObject, JSON_KEY_FACET_PRESELECTION);
                Boolean ignoreAllFacetFilters = parseOptionalBooleanValue(
                    queryFacetObject,
                    JSON_KEY_FACET_IGNOREALLFACETFILTERS);
                return new CmsSearchConfigurationFacetQuery(
                    queries,
                    label,
                    isAndFacet,
                    preselection,
                    ignoreAllFacetFilters);
            } catch (JSONException e) {
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.ERR_QUERY_FACET_MANDATORY_KEY_MISSING_1,
                        JSON_KEY_QUERY_FACET_QUERY),
                    e);
                return null;
            }
        } catch (JSONException e) {
            // nothing to do, configuration is optional
            return null;
        }
    }

    /**
     * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseRangeFacets()
     */
    public Map<String, I_CmsSearchConfigurationFacetRange> parseRangeFacets() {

        Map<String, I_CmsSearchConfigurationFacetRange> facetConfigs = new LinkedHashMap<String, I_CmsSearchConfigurationFacetRange>();
        try {
            JSONArray rangeFacets = m_configObject.getJSONArray(JSON_KEY_RANGE_FACETS);
            for (int i = 0; i < rangeFacets.length(); i++) {

                I_CmsSearchConfigurationFacetRange config = parseRangeFacet(rangeFacets.getJSONObject(i));
                if (config != null) {
                    facetConfigs.put(config.getName(), config);
                }
            }
        } catch (JSONException e) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_NO_FACET_CONFIG_0), e);
        }
        return facetConfigs;

    }

    /**
     * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseSorting()
     */
    @Override
    public I_CmsSearchConfigurationSorting parseSorting() {

        List<I_CmsSearchConfigurationSortOption> options = getSortOptions();
        I_CmsSearchConfigurationSortOption defaultOption = (options != null) && !options.isEmpty()
        ? options.get(0)
        : null;
        return new CmsSearchConfigurationSorting(getSortParam(), options, defaultOption);
    }

    /** Returns a map with additional request parameters, mapping the parameter names to Solr query parts.
     * @return A map with additional request parameters, mapping the parameter names to Solr query parts.
     */
    protected Map<String, String> getAdditionalParameters() {

        Map<String, String> result;
        try {
            JSONArray additionalParams = m_configObject.getJSONArray(JSON_KEY_ADDITIONAL_PARAMETERS);
            result = new HashMap<String, String>(additionalParams.length());
            for (int i = 0; i < additionalParams.length(); i++) {
                try {
                    JSONObject currentParam = additionalParams.getJSONObject(i);
                    String param = currentParam.getString(JSON_KEY_ADDITIONAL_PARAMETERS_PARAM);
                    String solrQuery = currentParam.getString(JSON_KEY_ADDITIONAL_PARAMETERS_SOLRQUERY);
                    result.put(param, solrQuery);
                } catch (JSONException e) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_ADDITIONAL_PARAMETER_CONFIG_WRONG_0), e);
                    continue;
                }
            }
        } catch (JSONException e) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_ADDITIONAL_PARAMETER_CONFIG_NOT_PARSED_0), e);
            return new HashMap<String, String>();
        }
        return result;
    }

    /** Returns the configured Solr core, or <code>null</code> if no core is configured.
     * @return The configured Solr core, or <code>null</code> if no core is configured.
     */
    protected String getCore() {

        try {
            return m_configObject.getString(JSON_KEY_CORE);
        } catch (JSONException e) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_NO_CORE_SPECIFIED_0), e);
            return null;
        }
    }

    /**
     * Returns the flag, indicating if the characters in the query string that are commands to Solr should be escaped.
     * @return the flag, indicating if the characters in the query string that are commands to Solr should be escaped.
     */
    protected Boolean getEscapeQueryChars() {

        return parseOptionalBooleanValue(m_configObject, JSON_KEY_ESCAPE_QUERY_CHARACTERS);
    }

    /** Returns the configured extra parameters that should be given to Solr, or the empty string if no parameters are configured.
     * @return The configured extra parameters that should be given to Solr, or the empty string if no parameters are configured.
     */
    protected String getExtraSolrParams() {

        try {
            return m_configObject.getString(JSON_KEY_EXTRASOLRPARAMS);
        } catch (JSONException e) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_NO_EXTRA_PARAMETERS_0), e);
            return "";
        }
    }

    /** Returns the configured request parameter for the last query, or the default parameter if no core is configured.
    * @return The configured request parameter for the last query, or the default parameter if no core is configured.
    */
    protected String getFirstCallParam() {

        String param = parseOptionalStringValue(m_configObject, JSON_KEY_RELOADED_PARAM);
        if (param == null) {
            return DEFAULT_RELOADED_PARAM;
        } else {
            return param;
        }
    }

    /** Returns a flag indicating if also expired resources should be found.
     * @return A flag indicating if also expired resources should be found.
     */
    protected Boolean getIgnoreExpirationDate() {

        return parseOptionalBooleanValue(m_configObject, JSON_KEY_IGNORE_EXPIRATION_DATE);
    }

    /** Returns a flag indicating if the query given by the parameters should be ignored.
     * @return A flag indicating if the query given by the parameters should be ignored.
     */
    protected Boolean getIgnoreQuery() {

        return parseOptionalBooleanValue(m_configObject, JSON_KEY_IGNORE_QUERY);
    }

    /** Returns a flag indicating if also unreleased resources should be found.
     * @return A flag indicating if also unreleased resources should be found.
     */
    protected Boolean getIgnoreReleaseDate() {

        return parseOptionalBooleanValue(m_configObject, JSON_KEY_IGNORE_RELEASE_DATE);
    }

    /** Returns the configured Solr index, or <code>null</code> if no core is configured.
     * @return The configured Solr index, or <code>null</code> if no core is configured.
     */
    protected String getIndex() {

        try {
            return m_configObject.getString(JSON_KEY_INDEX);
        } catch (JSONException e) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_NO_INDEX_SPECIFIED_0), e);
            return null;
        }
    }

    /** Returns the configured request parameter for the last query, or the default parameter if no core is configured.
    * @return The configured request parameter for the last query, or the default parameter if no core is configured.
    */
    protected String getLastQueryParam() {

        String param = parseOptionalStringValue(m_configObject, JSON_KEY_LAST_QUERYPARAM);
        if (param == null) {
            return DEFAULT_LAST_QUERY_PARAM;
        } else {
            return param;
        }
    }

    /** Returns the configured length of the "Google"-like page navigation, or the default parameter if no core is configured.
     * @return The configured length of the "Google"-like page navigation, or the default parameter if no core is configured.
     */
    protected Integer getPageNavLength() {

        Integer param = parseOptionalIntValue(m_configObject, JSON_KEY_PAGENAVLENGTH);
        if (param == null) {
            return DEFAULT_PAGENAVLENGTH;
        } else {
            return param;
        }
    }

    /** Returns the configured request parameter for the current page, or the default parameter if no core is configured.
     * @return The configured request parameter for the current page, or the default parameter if no core is configured.
     */
    protected String getPageParam() {

        String param = parseOptionalStringValue(m_configObject, JSON_KEY_PAGEPARAM);
        if (param == null) {
            return DEFAULT_PAGE_PARAM;
        } else {
            return param;
        }
    }

    /** Returns the configured page size, or the default page size if no core is configured.
     * @return The configured page size, or the default page size if no core is configured.
     */
    protected Integer getPageSize() {

        try {
            return Integer.valueOf(m_configObject.getInt(JSON_KEY_PAGESIZE));
        } catch (JSONException e) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_NO_PAGESIZE_SPECIFIED_0), e);
            return DEFAULT_PAGE_SIZE;
        }
    }

    /** Returns the optional query modifier.
     * @return the optional query modifier.
     */
    protected String getQueryModifier() {

        return parseOptionalStringValue(m_configObject, JSON_KEY_QUERY_MODIFIER);
    }

    /** Returns the configured request parameter for the query string, or the default parameter if no core is configured.
     * @return The configured request parameter for the query string, or the default parameter if no core is configured.
     */
    protected String getQueryParam() {

        String param = parseOptionalStringValue(m_configObject, JSON_KEY_QUERYPARAM);
        if (param == null) {
            return DEFAULT_QUERY_PARAM;
        } else {
            return param;
        }
    }

    /** Returns a flag, indicating if search should be performed using a wildcard if the empty query is given.
     * @return A flag, indicating if search should be performed using a wildcard if the empty query is given.
     */
    protected Boolean getSearchForEmptyQuery() {

        return parseOptionalBooleanValue(m_configObject, JSON_KEY_SEARCH_FOR_EMPTY_QUERY);
    }

    /** Returns the list of the configured sort options, or the empty list if no sort options are configured.
     * @return The list of the configured sort options, or the empty list if no sort options are configured.
     */
    protected List<I_CmsSearchConfigurationSortOption> getSortOptions() {

        List<I_CmsSearchConfigurationSortOption> options = new LinkedList<I_CmsSearchConfigurationSortOption>();
        try {
            JSONArray sortOptions = m_configObject.getJSONArray(JSON_KEY_SORTOPTIONS);
            for (int i = 0; i < sortOptions.length(); i++) {
                I_CmsSearchConfigurationSortOption option = parseSortOption(sortOptions.getJSONObject(i));
                if (option != null) {
                    options.add(option);
                }
            }
        } catch (JSONException e) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_NO_SORT_CONFIG_0), e);
        }
        return options;
    }

    /** Returns the configured request parameter for the sort option, or the default parameter if no core is configured.
     * @return The configured request parameter for the sort option, or the default parameter if no core is configured.
     */
    protected String getSortParam() {

        String param = parseOptionalStringValue(m_configObject, JSON_KEY_SORTPARAM);
        if (param == null) {
            return DEFAULT_SORT_PARAM;
        } else {
            return param;
        }
    }

    /** Initialization that parses the String to a JSON object.
     * @param configString The JSON as string.
     * @throws JSONException thrown if parsing fails.
     */
    protected void init(String configString) throws JSONException {

        m_configObject = new JSONObject(configString);
    }

    /** Parses a single query item for the query facet.
     * @param item JSON object of the query item.
     * @return the parsed query item, or <code>null</code> if parsing failed.
     */
    protected I_CmsFacetQueryItem parseFacetQueryItem(JSONObject item) {

        String query;
        try {
            query = item.getString(JSON_KEY_QUERY_FACET_QUERY_QUERY);
        } catch (JSONException e) {
            // TODO: Log
            return null;
        }
        String label = parseOptionalStringValue(item, JSON_KEY_QUERY_FACET_QUERY_LABEL);
        return new CmsFacetQueryItem(query, label);
    }

    /** Parses the list of query items for the query facet.
     * @param queryFacetObject JSON object representing the node with the query facet.
     * @return list of query options
     * @throws JSONException if the list cannot be parsed.
     */
    protected List<I_CmsFacetQueryItem> parseFacetQueryItems(JSONObject queryFacetObject) throws JSONException {

        JSONArray items = queryFacetObject.getJSONArray(JSON_KEY_QUERY_FACET_QUERY);
        List<I_CmsFacetQueryItem> result = new ArrayList<I_CmsFacetQueryItem>(items.length());
        for (int i = 0; i < items.length(); i++) {
            I_CmsFacetQueryItem item = parseFacetQueryItem(items.getJSONObject(i));
            if (item != null) {
                result.add(item);
            }
        }
        return result;
    }

    /** Parses the field facet configurations.
     * @param fieldFacetObject The JSON sub-node with the field facet configurations.
     * @return The field facet configurations.
     */
    protected I_CmsSearchConfigurationFacetField parseFieldFacet(JSONObject fieldFacetObject) {

        try {
            String field = fieldFacetObject.getString(JSON_KEY_FACET_FIELD);
            String name = parseOptionalStringValue(fieldFacetObject, JSON_KEY_FACET_NAME);
            String label = parseOptionalStringValue(fieldFacetObject, JSON_KEY_FACET_LABEL);
            Integer minCount = parseOptionalIntValue(fieldFacetObject, JSON_KEY_FACET_MINCOUNT);
            Integer limit = parseOptionalIntValue(fieldFacetObject, JSON_KEY_FACET_LIMIT);
            String prefix = parseOptionalStringValue(fieldFacetObject, JSON_KEY_FACET_PREFIX);
            String sorder = parseOptionalStringValue(fieldFacetObject, JSON_KEY_FACET_ORDER);
            I_CmsSearchConfigurationFacet.SortOrder order;
            try {
                order = I_CmsSearchConfigurationFacet.SortOrder.valueOf(sorder);
            } catch (Exception e) {
                order = null;
            }
            String filterQueryModifier = parseOptionalStringValue(fieldFacetObject, JSON_KEY_FACET_FILTERQUERYMODIFIER);
            Boolean isAndFacet = parseOptionalBooleanValue(fieldFacetObject, JSON_KEY_FACET_ISANDFACET);
            List<String> preselection = parseOptionalStringValues(fieldFacetObject, JSON_KEY_FACET_PRESELECTION);
            Boolean ignoreFilterAllFacetFilters = parseOptionalBooleanValue(
                fieldFacetObject,
                JSON_KEY_FACET_IGNOREALLFACETFILTERS);
            return new CmsSearchConfigurationFacetField(
                field,
                name,
                minCount,
                limit,
                prefix,
                label,
                order,
                filterQueryModifier,
                isAndFacet,
                preselection,
                ignoreFilterAllFacetFilters);
        } catch (JSONException e) {
            LOG.error(
                Messages.get().getBundle().key(Messages.ERR_FIELD_FACET_MANDATORY_KEY_MISSING_1, JSON_KEY_FACET_FIELD),
                e);
            return null;
        }
    }

    /** Parses the query facet configurations.
     * @param rangeFacetObject The JSON sub-node with the query facet configurations.
     * @return The query facet configurations.
     */
    protected I_CmsSearchConfigurationFacetRange parseRangeFacet(JSONObject rangeFacetObject) {

        try {
            String range = rangeFacetObject.getString(JSON_KEY_RANGE_FACET_RANGE);
            String name = parseOptionalStringValue(rangeFacetObject, JSON_KEY_FACET_NAME);
            String label = parseOptionalStringValue(rangeFacetObject, JSON_KEY_FACET_LABEL);
            Integer minCount = parseOptionalIntValue(rangeFacetObject, JSON_KEY_FACET_MINCOUNT);
            String start = rangeFacetObject.getString(JSON_KEY_RANGE_FACET_START);
            String end = rangeFacetObject.getString(JSON_KEY_RANGE_FACET_END);
            String gap = rangeFacetObject.getString(JSON_KEY_RANGE_FACET_GAP);
            List<String> sother = parseOptionalStringValues(rangeFacetObject, JSON_KEY_RANGE_FACET_OTHER);
            Boolean hardEnd = parseOptionalBooleanValue(rangeFacetObject, JSON_KEY_RANGE_FACET_HARDEND);
            List<I_CmsSearchConfigurationFacetRange.Other> other = null;
            if (sother != null) {
                other = new ArrayList<I_CmsSearchConfigurationFacetRange.Other>(sother.size());
                for (String so : sother) {
                    try {
                        I_CmsSearchConfigurationFacetRange.Other o = I_CmsSearchConfigurationFacetRange.Other.valueOf(
                            so);
                        other.add(o);
                    } catch (Exception e) {
                        LOG.error(Messages.get().getBundle().key(Messages.ERR_INVALID_OTHER_OPTION_1, so), e);
                    }
                }
            }
            Boolean isAndFacet = parseOptionalBooleanValue(rangeFacetObject, JSON_KEY_FACET_ISANDFACET);
            List<String> preselection = parseOptionalStringValues(rangeFacetObject, JSON_KEY_FACET_PRESELECTION);
            Boolean ignoreAllFacetFilters = parseOptionalBooleanValue(
                rangeFacetObject,
                JSON_KEY_FACET_IGNOREALLFACETFILTERS);
            return new CmsSearchConfigurationFacetRange(
                range,
                start,
                end,
                gap,
                other,
                hardEnd,
                name,
                minCount,
                label,
                isAndFacet,
                preselection,
                ignoreAllFacetFilters);
        } catch (JSONException e) {
            LOG.error(
                Messages.get().getBundle().key(
                    Messages.ERR_RANGE_FACET_MANDATORY_KEY_MISSING_1,
                    JSON_KEY_RANGE_FACET_RANGE
                        + ", "
                        + JSON_KEY_RANGE_FACET_START
                        + ", "
                        + JSON_KEY_RANGE_FACET_END
                        + ", "
                        + JSON_KEY_RANGE_FACET_GAP),
                e);
            return null;
        }

    }

    /** Returns a single sort option configuration as configured via the methods parameter, or null if the parameter does not specify a sort option.
     * @param json The JSON sort option configuration.
     * @return The sort option configuration, or null if the JSON could not be read.
     */
    protected I_CmsSearchConfigurationSortOption parseSortOption(JSONObject json) {

        try {
            String solrValue = json.getString(JSON_KEY_SORTOPTION_SOLRVALUE);
            String paramValue = parseOptionalStringValue(json, JSON_KEY_SORTOPTION_PARAMVALUE);
            paramValue = (paramValue == null) ? solrValue : paramValue;
            String label = parseOptionalStringValue(json, JSON_KEY_SORTOPTION_LABEL);
            label = (label == null) ? paramValue : label;
            return new CmsSearchConfigurationSortOption(label, paramValue, solrValue);
        } catch (JSONException e) {
            LOG.error(
                Messages.get().getBundle().key(Messages.ERR_SORT_OPTION_NOT_PARSABLE_1, JSON_KEY_SORTOPTION_SOLRVALUE),
                e);
            return null;
        }
    }
}

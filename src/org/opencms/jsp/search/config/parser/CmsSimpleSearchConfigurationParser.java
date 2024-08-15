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
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.i18n.CmsEncoder;
import org.opencms.json.JSONException;
import org.opencms.jsp.search.config.CmsSearchConfigurationFacetField;
import org.opencms.jsp.search.config.CmsSearchConfigurationFacetRange;
import org.opencms.jsp.search.config.CmsSearchConfigurationSortOption;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacet.SortOrder;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetField;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetRange;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationPagination;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationSortOption;
import org.opencms.jsp.search.config.parser.simplesearch.CmsCategoryFolderRestrictionBean;
import org.opencms.jsp.search.config.parser.simplesearch.CmsConfigurationBean;
import org.opencms.jsp.search.config.parser.simplesearch.CmsConfigurationBean.CombinationMode;
import org.opencms.jsp.search.config.parser.simplesearch.CmsGeoFilterBean;
import org.opencms.jsp.search.config.parser.simplesearch.daterestrictions.I_CmsDateRestriction;
import org.opencms.jsp.search.config.parser.simplesearch.preconfiguredrestrictions.CmsRestrictionRule;
import org.opencms.jsp.search.config.parser.simplesearch.preconfiguredrestrictions.CmsRestrictionsBean;
import org.opencms.jsp.search.config.parser.simplesearch.preconfiguredrestrictions.CmsRestrictionsBean.FieldValues;
import org.opencms.jsp.search.config.parser.simplesearch.preconfiguredrestrictions.CmsRestrictionsBean.FieldValues.FieldType;
import org.opencms.main.CmsException;
import org.opencms.relations.CmsCategoryService;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.search.solr.CmsSolrQueryUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.solr.common.params.CommonParams;

import com.google.common.collect.Lists;

/**
 * Search configuration parser using a list configuration file as the base configuration with additional JSON.<p>
 */
public class CmsSimpleSearchConfigurationParser extends CmsJSONSearchConfigurationParser {

    /** Sort options that are available by default. */
    public static enum SortOption {

        /** Sort by date ascending. */
        DATE_ASC,
        /** Sort by date descending. */
        DATE_DESC,
        /** Sort by title ascending. */
        TITLE_ASC,
        /** Sort by title descending. */
        TITLE_DESC,
        /** Sort by order ascending. */
        ORDER_ASC,
        /** Sort by order descending. */
        ORDER_DESC;

        /**
         * Generates the suitable {@link I_CmsSearchConfigurationSortOption} for the option.
         * @param l the locale for which the option should be created
         * @return the created {@link I_CmsSearchConfigurationSortOption}
         */
        public I_CmsSearchConfigurationSortOption getOption(Locale l) {

            switch (this) {
                case DATE_ASC:
                    return new CmsSearchConfigurationSortOption("date.asc", "date_asc", getSortDateField(l) + " asc");
                case DATE_DESC:
                    return new CmsSearchConfigurationSortOption(
                        "date.desc",
                        "date_desc",
                        getSortDateField(l) + " desc");
                case TITLE_ASC:
                    return new CmsSearchConfigurationSortOption(
                        "title.asc",
                        "title_asc",
                        getSortTitleField(l) + " asc");
                case TITLE_DESC:
                    return new CmsSearchConfigurationSortOption(
                        "title.desc",
                        "title_desc",
                        getSortTitleField(l) + " desc");
                case ORDER_ASC:
                    return new CmsSearchConfigurationSortOption(
                        "order.asc",
                        "order_asc",
                        getSortOrderField(l) + " asc");
                case ORDER_DESC:
                    return new CmsSearchConfigurationSortOption(
                        "order.desc",
                        "order_desc",
                        getSortOrderField(l) + " desc");
                default:
                    throw new IllegalArgumentException();
            }
        }

        /**
         * Returns the locale specific date field to use for sorting.
         * @param l the locale to use, can be <code>null</code>
         * @return the locale specific date field to use for sorting.
         */
        protected String getSortDateField(Locale l) {

            return CmsSearchField.FIELD_INSTANCEDATE
                + (null != l ? "_" + l.toString() : "")
                + CmsSearchField.FIELD_POSTFIX_DATE;
        }

        /**
         * Returns the locale specific order field to use for sorting.
         * @param l the locale to use, can be <code>null</code>
         * @return the locale specific order field to use for sorting.
         */
        protected String getSortOrderField(Locale l) {

            return CmsSearchField.FIELD_DISPORDER
                + (null != l ? "_" + l.toString() : "")
                + CmsSearchField.FIELD_POSTFIX_INT;
        }

        /**
         * Returns the locale specific title field to use for sorting.
         * @param l the locale to use, can be <code>null</code>
         * @return the locale specific title field to use for sorting.
         */
        protected String getSortTitleField(Locale l) {

            return CmsSearchField.FIELD_DISPTITLE
                + (null != l ? "_" + l.toString() : "")
                + CmsSearchField.FIELD_POSTFIX_SORT;
        }
    }

    /** SOLR field name. */
    public static final String FIELD_CATEGORIES = "category_exact";

    /** SOLR field name. */
    public static final String FIELD_DATE = "instancedate_%s_dt";

    /** SOLR field name. */
    public static final String FIELD_DATE_RANGE = "instancedaterange_%s_dr";

    /** SOLR field name. */
    public static final String FIELD_DATE_FACET_NAME = "instancedate";

    /** SOLR field name. */
    public static final String FIELD_PARENT_FOLDERS = "parent-folders";

    /** Pagination which may override the default pagination. */
    private I_CmsSearchConfigurationPagination m_pagination;

    /** The current cms context. */
    private CmsObject m_cms;

    /** The list configuration bean. */
    private CmsConfigurationBean m_config;

    /** The (mutable) search locale. */
    private Locale m_searchLocale;

    /** The (mutable) sort order. */
    private CmsSimpleSearchConfigurationParser.SortOption m_sortOrder;

    /** Flag which, if true, causes the search to ignore the blacklist. */
    private boolean m_ignoreBlacklist;

    /**
     * Constructor.<p>
     *
     * @param cms the cms context
     * @param config the list configuration
     * @param additionalParamJSON the additional JSON configuration
     *
     * @throws JSONException in case parsing the JSON fails
     */
    public CmsSimpleSearchConfigurationParser(CmsObject cms, CmsConfigurationBean config, String additionalParamJSON)
    throws JSONException {

        super(CmsStringUtil.isEmptyOrWhitespaceOnly(additionalParamJSON) ? "{}" : additionalParamJSON);
        m_cms = cms;
        m_config = config;
    }

    /**
     * Creates an instance for an empty JSON configuration.<p>
     *
     * The point of this is that we know that passing an empty configuration makes it impossible
     * for a JSONException to thrown.
     *
     * @param cms the current CMS context
     * @param config  the search configuration
     *
     * @return the search config parser
     */
    public static CmsSimpleSearchConfigurationParser createInstanceWithNoJsonConfig(
        CmsObject cms,
        CmsConfigurationBean config) {

        try {
            return new CmsSimpleSearchConfigurationParser(cms, config, null);

        } catch (JSONException e) {
            return null;
        }
    }

    /** The default field facets.
     *
     * @param categoryConjunction flag, indicating if category selections in the facet should be "AND" combined.
     * @return the default field facets.
     */
    public static Map<String, I_CmsSearchConfigurationFacetField> getDefaultFieldFacets(boolean categoryConjunction) {

        Map<String, I_CmsSearchConfigurationFacetField> fieldFacets = new HashMap<String, I_CmsSearchConfigurationFacetField>();
        fieldFacets.put(
            FIELD_CATEGORIES,
            new CmsSearchConfigurationFacetField(
                FIELD_CATEGORIES,
                null,
                Integer.valueOf(1),
                Integer.valueOf(200),
                null,
                "Category",
                SortOrder.index,
                null,
                Boolean.valueOf(categoryConjunction),
                null,
                Boolean.TRUE,
                null));
        fieldFacets.put(
            FIELD_PARENT_FOLDERS,
            new CmsSearchConfigurationFacetField(
                FIELD_PARENT_FOLDERS,
                null,
                Integer.valueOf(1),
                Integer.valueOf(200),
                null,
                "Folders",
                SortOrder.index,
                null,
                Boolean.FALSE,
                null,
                Boolean.TRUE,
                null));
        return Collections.unmodifiableMap(fieldFacets);

    }

    /**
     * Returns the initial SOLR query.<p>
     *
     * @return the SOLR query
     */
    public CmsSolrQuery getInitialQuery() {

        Map<String, String[]> queryParams = new HashMap<String, String[]>();
        if (!m_cms.getRequestContext().getCurrentProject().isOnlineProject() && m_config.isShowExpired()) {
            queryParams.put("fq", new String[] {"released:[* TO *]", "expired:[* TO *]"});
        }
        return new CmsSolrQuery(null, queryParams);
    }

    /**
     * Gets the search locale.<p>
     *
     * @return the search locale
     */
    public Locale getSearchLocale() {

        if (m_searchLocale != null) {
            return m_searchLocale;
        }
        return m_cms.getRequestContext().getLocale();
    }

    /**
    * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseFieldFacets()
    */
    @Override
    public Map<String, I_CmsSearchConfigurationFacetField> parseFieldFacets() {

        if (m_configObject.has(JSON_KEY_FIELD_FACETS)) {
            return super.parseFieldFacets();
        } else {
            return getDefaultFieldFacets(true);
        }
    }

    /**
     * @see org.opencms.jsp.search.config.parser.CmsJSONSearchConfigurationParser#parsePagination()
     */
    @Override
    public I_CmsSearchConfigurationPagination parsePagination() {

        if (m_pagination != null) {
            return m_pagination;
        }
        return super.parsePagination();
    }

    /**
    * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseRangeFacets()
    */
    @Override
    public Map<String, I_CmsSearchConfigurationFacetRange> parseRangeFacets() {

        if (m_configObject.has(JSON_KEY_RANGE_FACETS)) {
            return super.parseRangeFacets();
        } else {
            Map<String, I_CmsSearchConfigurationFacetRange> rangeFacets = new HashMap<String, I_CmsSearchConfigurationFacetRange>();
            String indexField = FIELD_DATE;
            if (Boolean.parseBoolean(m_config.getParameterValue(CmsConfigurationBean.PARAM_FILTER_MULTI_DAY))) {
                indexField = FIELD_DATE_RANGE;
            }
            I_CmsSearchConfigurationFacetRange rangeFacet = new CmsSearchConfigurationFacetRange(
                String.format(indexField, getSearchLocale().toString()),
                "NOW/YEAR-20YEARS",
                "NOW/MONTH+5YEARS",
                "+1MONTHS",
                null,
                Boolean.FALSE,
                FIELD_DATE_FACET_NAME,
                Integer.valueOf(1),
                "Date",
                Boolean.FALSE,
                null,
                Boolean.TRUE,
                null);

            rangeFacets.put(rangeFacet.getName(), rangeFacet);
            return rangeFacets;
        }
    }

    /**
     * Sets the 'ignore blacklist' flag.<p>
     *
     * If set, the search will ignore the blacklist from the list configuration.<p>
     *
     * @param ignoreBlacklist true if the blacklist should be ignored
     */
    public void setIgnoreBlacklist(boolean ignoreBlacklist) {

        m_ignoreBlacklist = ignoreBlacklist;
    }

    /**
     * Sets the pagination.<p>
     *
     * If this is set, parsePagination will always return the set value instead of using the default way to compute the pagination
     *
     * @param pagination the pagination
     */
    public void setPagination(I_CmsSearchConfigurationPagination pagination) {

        m_pagination = pagination;
    }

    /**
     * Sets the search locale.<p>
     *
     * @param locale the search locale
     */
    public void setSearchLocale(Locale locale) {

        m_searchLocale = locale;
    }

    /**
     * Sets the sort option.<p>
     *
     * @param sortOption the sort option
     */
    public void setSortOption(String sortOption) {

        if (null != sortOption) {
            try {
                m_sortOrder = CmsSimpleSearchConfigurationParser.SortOption.valueOf(sortOption);
            } catch (IllegalArgumentException e) {
                m_sortOrder = null;
                LOG.warn(
                    "Setting illegal default sort option " + sortOption + " failed. Using Solr's default sort option.");
            }
        } else {
            m_sortOrder = null;
        }
    }

    /**
     * @see org.opencms.jsp.search.config.parser.CmsJSONSearchConfigurationParser#getEscapeQueryChars()
     */
    @Override
    protected Boolean getEscapeQueryChars() {

        if (m_configObject.has(JSON_KEY_ESCAPE_QUERY_CHARACTERS)) {
            return super.getEscapeQueryChars();
        } else {
            return Boolean.TRUE;
        }
    }

    /**
     * @see org.opencms.jsp.search.config.parser.CmsJSONSearchConfigurationParser#getExtraSolrParams()
     */
    @Override
    protected String getExtraSolrParams() {

        String params = super.getExtraSolrParams();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(params)) {
            params = getCategoryFolderFilter()
                + getResourceTypeFilter()
                + getPreconfiguredFilterQuery()
                + getFilterQuery()
                + getBlacklistFilter()
                + getGeoFilterQuery();
        }
        return params;
    }

    /**
     * @see org.opencms.jsp.search.config.parser.CmsJSONSearchConfigurationParser#getIgnoreExpirationDate()
     */
    @Override
    protected Boolean getIgnoreExpirationDate() {

        return getIgnoreReleaseAndExpiration();

    }

    /**
     * @see org.opencms.jsp.search.config.parser.CmsJSONSearchConfigurationParser#getIgnoreReleaseDate()
     */
    @Override
    protected Boolean getIgnoreReleaseDate() {

        return getIgnoreReleaseAndExpiration();
    }

    /**
     * @see org.opencms.jsp.search.config.parser.CmsJSONSearchConfigurationParser#getMaxReturnedResults(java.lang.String)
     */
    @Override
    protected int getMaxReturnedResults(String indexName) {

        return null != m_config.getMaximallyReturnedResults()
        ? m_config.getMaximallyReturnedResults().intValue()
        : super.getMaxReturnedResults(indexName);
    }

    /**
     * @see org.opencms.jsp.search.config.parser.CmsJSONSearchConfigurationParser#getQueryModifier()
     */
    @Override
    protected String getQueryModifier() {

        String modifier = super.getQueryModifier();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(modifier)) {
            modifier = "{!type=edismax qf=\""
                + CmsSearchField.FIELD_CONTENT
                + "_"
                + getSearchLocale().toString()
                + " "
                + CmsPropertyDefinition.PROPERTY_TITLE
                + CmsSearchField.FIELD_DYNAMIC_PROPERTIES
                + " "
                + CmsPropertyDefinition.PROPERTY_DESCRIPTION
                + CmsSearchField.FIELD_DYNAMIC_PROPERTIES_DIRECT
                + " "
                + CmsPropertyDefinition.PROPERTY_DESCRIPTION_HTML
                + CmsSearchField.FIELD_DYNAMIC_PROPERTIES_DIRECT
                + " "
                + CmsSearchField.FIELD_DESCRIPTION
                + "_"
                + getSearchLocale().toString()
                + " "
                + CmsSearchField.FIELD_KEYWORDS
                + "_"
                + getSearchLocale().toString()
                + "\"}%(query)";
        }
        return modifier;
    }

    /**
     * @see org.opencms.jsp.search.config.parser.CmsJSONSearchConfigurationParser#getSearchForEmptyQuery()
     */
    @Override
    protected Boolean getSearchForEmptyQuery() {

        if (m_configObject.has(JSON_KEY_SEARCH_FOR_EMPTY_QUERY)) {
            return super.getSearchForEmptyQuery();
        } else {
            return Boolean.TRUE;
        }
    }

    /**
     * @see org.opencms.jsp.search.config.parser.CmsJSONSearchConfigurationParser#getSortOptions()
     */
    @Override
    protected List<I_CmsSearchConfigurationSortOption> getSortOptions() {

        if (m_configObject.has(JSON_KEY_SORTOPTIONS)) {
            return super.getSortOptions();
        } else {
            List<I_CmsSearchConfigurationSortOption> options = new LinkedList<I_CmsSearchConfigurationSortOption>();

            CmsSimpleSearchConfigurationParser.SortOption currentOption = CmsSimpleSearchConfigurationParser.SortOption.valueOf(
                m_config.getSortOrder());
            if (m_sortOrder != null) {
                currentOption = m_sortOrder;
            }
            Locale locale = getSearchLocale();
            options.add(currentOption.getOption(locale));
            CmsSimpleSearchConfigurationParser.SortOption[] sortOptions = CmsSimpleSearchConfigurationParser.SortOption.values();
            for (int i = 0; i < sortOptions.length; i++) {
                CmsSimpleSearchConfigurationParser.SortOption option = sortOptions[i];
                if (!Objects.equals(currentOption, option)) {
                    options.add(option.getOption(locale));
                }
            }
            return options;
        }
    }

    /**
     * Generates the query part for the preconfigured restrictions for the type.
     * @param type the type to generate the restriction for.
     * @param restrictionsForType the preconfigured restrictions for the type.
     * @return the part of the Solr query for the restriction.
     */
    String generatePreconfiguredRestriction(
        String type,
        Map<CmsRestrictionRule, Collection<FieldValues>> restrictionsForType) {

        String result = "";
        if ((null != restrictionsForType) && (restrictionsForType.size() > 0)) {
            Collection<String> ruleRestrictions = new HashSet<>(restrictionsForType.size());
            for (Map.Entry<CmsRestrictionRule, Collection<FieldValues>> ruleEntry : restrictionsForType.entrySet()) {
                ruleRestrictions.add(generatePreconfiguredRestrictionForRule(ruleEntry.getKey(), ruleEntry.getValue()));
            }
            result = ruleRestrictions.size() > 1
            ? ruleRestrictions.stream().reduce((r1, r2) -> (r1 + " " + CombinationMode.AND + " " + r2)).get()
            : ruleRestrictions.iterator().next();
            if (null != type) {
                result = "type:\"" + type + "\" AND (" + result + ")";
            }
        } else if (null != type) {
            result = "type:\"" + type + "\"";
        }
        return result.isEmpty() ? result : "(" + result + ")";
    }

    /**
     * Generates the query part for the preconfigured restriction for a single rule.
     * @param rule the rule to generate the restriction for.
     * @param values the values provided for the rule.
     * @return the part of the Solr query for the restriction.
     */
    String generatePreconfiguredRestrictionForRule(CmsRestrictionRule rule, Collection<FieldValues> values) {

        Collection<String> resolvedFieldValues = values.stream().map(v -> resolveFieldValues(rule, v)).collect(
            Collectors.toSet());

        String seperator = " " + rule.getCombinationModeBetweenFields().toString() + " ";
        return rule.getFieldForLocale(getSearchLocale())
            + ":("
            + resolvedFieldValues.stream().reduce((v1, v2) -> v1 + seperator + v2).get()
            + ")";

    }

    /**
     * Returns the blacklist filter.<p>
     *
     * @return the blacklist filter
     */
    String getBlacklistFilter() {

        if (m_ignoreBlacklist) {
            return "";
        }
        String result = "";
        List<CmsUUID> blacklist = m_config.getBlacklist();
        List<String> blacklistStrings = Lists.newArrayList();
        for (CmsUUID id : blacklist) {
            blacklistStrings.add("\"" + id.toString() + "\"");
        }
        if (!blacklistStrings.isEmpty()) {
            result = "&fq=" + CmsEncoder.encode("-id:(" + CmsStringUtil.listAsString(blacklistStrings, " OR ") + ")");
        }
        return result;
    }

    /**
     * Returns the category filter string.<p>
     *
     * @return the category filter
     */
    String getCategoryFilterPart() {

        String result = "";
        if (!m_config.getCategories().isEmpty()) {
            List<String> categoryVals = Lists.newArrayList();
            for (String path : m_config.getCategories()) {
                try {
                    path = CmsCategoryService.getInstance().getCategory(
                        m_cms,
                        m_cms.getRequestContext().addSiteRoot(path)).getPath();
                    categoryVals.add("\"" + path + "\"");
                } catch (CmsException e) {
                    LOG.warn(e.getLocalizedMessage(), e);
                }
            }
            if (!categoryVals.isEmpty()) {
                String operator = " " + m_config.getCategoryMode() + " ";
                String valueExpression = CmsStringUtil.listAsString(categoryVals, operator);
                result = "category_exact:(" + valueExpression + ")";

            }
        }
        return result;
    }

    /**
     * Returns the category filter string.<p>
     *
     * @return the category filter
     */
    String getCategoryFolderFilter() {

        String result = "";
        String defaultPart = getFolderFilterPart();
        String categoryFilterPart = getCategoryFilterPart();
        if (!categoryFilterPart.isEmpty()) {
            defaultPart = "((" + defaultPart + ") AND (" + categoryFilterPart + "))";
        }
        for (CmsCategoryFolderRestrictionBean restriction : m_config.getCategoryFolderRestrictions()) {
            String restrictionQuery = restriction.toString();
            if (!restrictionQuery.isEmpty()) {
                restrictionQuery = "(" + restrictionQuery + " AND " + defaultPart + ")";
                if (!result.isEmpty()) {
                    result += " OR ";
                }
                result += restrictionQuery;
            }
        }
        if (result.isEmpty()) {
            result = defaultPart;
        }
        return "fq=" + CmsEncoder.encode(result);
    }

    /**
     * The fields returned by default. Typically the output is done via display formatters and hence nearly no
     * field is necessary. Returning all fields might cause performance problems.
     *
     * @return the default return fields.
     */
    String getDefaultReturnFields() {

        StringBuffer fields = new StringBuffer("");
        fields.append(CmsSearchField.FIELD_PATH);
        fields.append(',');
        fields.append(CmsSearchField.FIELD_INSTANCEDATE).append(CmsSearchField.FIELD_POSTFIX_DATE);
        fields.append(',');
        fields.append(CmsSearchField.FIELD_INSTANCEDATE_END).append(CmsSearchField.FIELD_POSTFIX_DATE);
        fields.append(',');
        fields.append(CmsSearchField.FIELD_INSTANCEDATE_CURRENT_TILL).append(CmsSearchField.FIELD_POSTFIX_DATE);
        fields.append(',');
        fields.append(CmsSearchField.FIELD_INSTANCEDATE).append('_').append(getSearchLocale().toString()).append(
            CmsSearchField.FIELD_POSTFIX_DATE);
        fields.append(',');
        fields.append(CmsSearchField.FIELD_INSTANCEDATE_END).append('_').append(getSearchLocale().toString()).append(
            CmsSearchField.FIELD_POSTFIX_DATE);
        fields.append(',');
        fields.append(CmsSearchField.FIELD_INSTANCEDATE_CURRENT_TILL).append('_').append(
            getSearchLocale().toString()).append(CmsSearchField.FIELD_POSTFIX_DATE);
        fields.append(',');
        fields.append(CmsSearchField.FIELD_ID);
        fields.append(',');
        fields.append(CmsSearchField.FIELD_SOLR_ID);
        fields.append(',');
        fields.append(CmsSearchField.FIELD_DISPTITLE).append('_').append(getSearchLocale().toString()).append("_sort");
        fields.append(',');
        fields.append(CmsSearchField.FIELD_LINK);
        fields.append(',');
        fields.append(CmsSearchField.FIELD_GEOCOORDS);
        return fields.toString();
    }

    /**
     * Returns the filter query string.<p>
     *
     * @return the filter query
     */
    String getFilterQuery() {

        String result = m_config.getFilterQuery();
        if (result == null) {
            result = "";
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result) && !result.startsWith("&")) {
            result = "&" + result;
        }
        if (!result.contains(CommonParams.FL + "=")) {
            result += "&" + CommonParams.FL + "=" + CmsEncoder.encode(getDefaultReturnFields());
        }
        I_CmsDateRestriction dateRestriction = m_config.getDateRestriction();
        if (dateRestriction != null) {
            result += "&fq="
                + CmsEncoder.encode(
                    CmsSearchField.FIELD_INSTANCEDATE_CURRENT_TILL
                        + "_"
                        + getSearchLocale().toString()
                        + "_dt:"
                        + dateRestriction.getRange());

        }
        result += "&fq=con_locales:" + getSearchLocale().toString();
        return result;
    }

    /**
     * Returns the folder filter string.<p>
     *
     * @return the folder filter
     */
    String getFolderFilterPart() {

        String result = "";
        List<String> parentFolderVals = Lists.newArrayList();
        if (!m_config.getFolders().isEmpty()) {
            for (String value : m_config.getFolders()) {
                parentFolderVals.add("\"" + value + "\"");
            }
        }
        if (parentFolderVals.isEmpty()) {
            result = "parent-folders:(\"/\")";
        } else {
            result = "parent-folders:(" + CmsStringUtil.listAsString(parentFolderVals, " OR ") + ")";
        }
        return result;
    }

    /**
     * Returns the Geo filter query string.<p>
     *
     * @return the Geo filter query string
     */
    String getGeoFilterQuery() {

        String result = "";
        CmsGeoFilterBean geoFilterBean = m_config.getGeoFilter();
        if (geoFilterBean != null) {
            String fq = CmsSolrQueryUtil.composeGeoFilterQuery(
                CmsSearchField.FIELD_GEOCOORDS,
                geoFilterBean.getCoordinates(),
                geoFilterBean.getRadius(),
                "km");
            result = "&fq=" + fq;
        }
        return result;
    }

    /**
     * Returns the filter query string.<p>
     *
     * @return the filter query
     */
    String getPreconfiguredFilterQuery() {

        String result = "";
        if (m_config.hasPreconfiguredRestrictions()) {
            CmsRestrictionsBean restrictions = m_config.getPreconfiguredRestrictions();
            String restriction = generatePreconfiguredRestriction(null, restrictions.getRestrictionsForType(null));
            if (!restriction.isEmpty()) {
                result = "&fq=" + CmsEncoder.encode(restriction);
            }
            Collection<String> typedRestrictions = new HashSet<>();
            for (String type : m_config.getTypes()) {
                restriction = generatePreconfiguredRestriction(type, restrictions.getRestrictionsForType(type));
                if (!restriction.isEmpty()) {
                    typedRestrictions.add(restriction);
                }
            }
            if (!typedRestrictions.isEmpty()) {
                result += "&fq="
                    + CmsEncoder.encode(
                        "(" + typedRestrictions.stream().reduce((r1, r2) -> (r1 + " OR " + r2)).get() + ")");
            }
        }
        return result;
    }

    /**
     * Returns the resource type filter string.<p>
     *
     * @return the folder filter
     */
    String getResourceTypeFilter() {

        String result = "";
        // When we have pre-configured restrictions, we need to combine the type filter with these restrictions.
        if (!m_config.hasPreconfiguredRestrictions()) {
            List<String> typeVals = Lists.newArrayList();
            for (String type : m_config.getTypes()) {
                typeVals.add("\"" + type + "\"");
            }
            if (!typeVals.isEmpty()) {
                result = "&fq=" + CmsEncoder.encode("type:(" + CmsStringUtil.listAsString(typeVals, " OR ") + ")");
            }
        }
        return result;
    }

    /**
     * Generates the search string part for one input field value.
     * @param rule the preconfigured rule.
     * @param fieldValues the values in the field.
     * @return the search term part for the value in the field.
     */
    String resolveFieldValues(CmsRestrictionRule rule, FieldValues fieldValues) {

        Collection<String> values = fieldValues.getValues();
        Collection<String> finalValues;
        if (FieldType.PLAIN.equals(fieldValues.getFieldType())) {
            // We are sure that there is exactly one value in that case.
            return "(" + values.iterator().next() + ")";
        } else {
            switch (rule.getMatchType()) {
                case DEFAULT:
                    finalValues = values;
                    break;
                case EXACT:
                    finalValues = values.stream().map(v -> ("\"" + v + "\"")).collect(Collectors.toSet());
                    break;
                case INFIX:
                    finalValues = values.stream().map(
                        v -> ("(" + v + " OR *" + v + " OR *" + v + "* OR " + v + "*)")).collect(Collectors.toSet());
                    break;
                case POSTFIX:
                    finalValues = values.stream().map(v -> ("(" + v + " OR *" + v + ")")).collect(Collectors.toSet());
                    break;
                case PREFIX:
                    finalValues = values.stream().map(v -> ("(" + v + " OR " + v + "*)")).collect(Collectors.toSet());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown match type '" + rule.getMatchType() + "'.");
            }
            if (finalValues.size() > 1) {
                String seperator = " " + rule.getCombinationModeInField().toString() + " ";
                return "(" + finalValues.stream().reduce((v1, v2) -> v1 + seperator + v2).get() + ")";
            } else {
                return finalValues.iterator().next();
            }

        }
    }

    /**
     * Returns a flag, indicating if the release and expiration date should be ignored.
     * @return a flag, indicating if the release and expiration date should be ignored.
     */
    private Boolean getIgnoreReleaseAndExpiration() {

        return Boolean.valueOf(m_config.isShowExpired());
    }
}

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
import org.opencms.json.JSONException;
import org.opencms.jsp.search.config.CmsSearchConfigurationFacetField;
import org.opencms.jsp.search.config.CmsSearchConfigurationFacetRange;
import org.opencms.jsp.search.config.CmsSearchConfigurationSortOption;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacet.SortOrder;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetField;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetRange;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationPagination;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationSortOption;
import org.opencms.main.CmsException;
import org.opencms.relations.CmsCategoryService;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.ui.apps.lists.CmsListManager;
import org.opencms.ui.apps.lists.CmsListManager.ListConfigurationBean;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.types.CmsXmlDisplayFormatterValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

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

    private I_CmsSearchConfigurationPagination m_pagination;

    /** The current cms context. */
    private CmsObject m_cms;

    /** The list configuration bean. */
    private ListConfigurationBean m_config;

    /** The (mutable) search locale. */
    private Locale m_searchLocale;

    /** The (mutable) sort order. */
    private CmsSimpleSearchConfigurationParser.SortOption m_sortOrder;

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
    public CmsSimpleSearchConfigurationParser(
        CmsObject cms,
        CmsListManager.ListConfigurationBean config,
        String additionalParamJSON)
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
        CmsListManager.ListConfigurationBean config) {

        try {
            return new CmsSimpleSearchConfigurationParser(cms, config, null);

        } catch (JSONException e) {
            return null;
        }
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
            return getDefaultFieldFacets(m_config.getCategoryConjunction());
        }
    }

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
            I_CmsSearchConfigurationFacetRange rangeFacet = new CmsSearchConfigurationFacetRange(
                String.format(CmsListManager.FIELD_DATE, getSearchLocale().toString()),
                "NOW/YEAR-20YEARS",
                "NOW/MONTH+2YEARS",
                "+1MONTHS",
                null,
                Boolean.FALSE,
                CmsListManager.FIELD_DATE_FACET_NAME,
                Integer.valueOf(1),
                "Date",
                Boolean.FALSE,
                null,
                Boolean.TRUE);

            rangeFacets.put(rangeFacet.getName(), rangeFacet);
            return rangeFacets;
        }
    }

    public void setIgnoreBlacklist(boolean ignoreBlacklist) {

        m_ignoreBlacklist = ignoreBlacklist;
    }

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
            params = getFolderFilter()
                + getResourceTypeFilter()
                + getCategoryFilter()
                + getFilterQuery()
                + getBlacklistFilter();
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
     * @see org.opencms.jsp.search.config.parser.CmsJSONSearchConfigurationParser#getQueryModifier()
     */
    @Override
    protected String getQueryModifier() {

        String modifier = super.getQueryModifier();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(modifier)) {
            modifier = "{!type=edismax qf=\"content_" + getSearchLocale().toString() + " Title_prop spell\"}%(query)";
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
            result = "&fq=-id:(" + CmsStringUtil.listAsString(blacklistStrings, " OR ") + ")";
        }
        return result;
    }

    /**
     * Returns the category filter string.<p>
     *
     * @return the category filter
     */
    String getCategoryFilter() {

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
                result = "&fq=category_exact:(" + valueExpression + ")";

            }
        }
        return result;
    }

    /**
     * Returns the filter query string.<p>
     *
     * @return the filter query
     */
    String getFilterQuery() {

        String result = m_config.getFilterQuery();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result) && !result.startsWith("&")) {
            result = "&" + result;
        }
        if (m_config.isCurrentOnly()) {
            result += "&fq=instancedatecurrenttill_" + getSearchLocale().toString() + "_dt:[NOW/DAY TO *]";
        }
        return result;
    }

    /**
     * Returns the folder filter string.<p>
     *
     * @return the folder filter
     */
    String getFolderFilter() {

        String result = "";
        List<String> parentFolderVals = Lists.newArrayList();
        if (!m_config.getFolders().isEmpty()) {
            for (String value : m_config.getFolders()) {
                parentFolderVals.add("\"" + value + "\"");
            }
        }
        if (parentFolderVals.isEmpty()) {
            result = "fq=parent-folders:(\"/\")";
        } else {
            result = "fq=parent-folders:(" + CmsStringUtil.listAsString(parentFolderVals, " OR ") + ")";
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
        List<String> typeVals = Lists.newArrayList();
        if (!m_config.getDisplayTypes().isEmpty()) {
            for (String displayType : m_config.getDisplayTypes()) {
                if (displayType.contains(CmsXmlDisplayFormatterValue.SEPARATOR)) {
                    displayType = displayType.substring(0, displayType.indexOf(CmsXmlDisplayFormatterValue.SEPARATOR));
                }
                typeVals.add("\"" + displayType + "\"");
            }
        }
        if (!typeVals.isEmpty()) {
            result = "&fq=type:(" + CmsStringUtil.listAsString(typeVals, " OR ") + ")";
        }
        return result;
    }

    /** The default field facets.
     *
     * @param categoryConjunction flag, indicating if category selections in the facet should be "AND" combined.
     * @return the default field facets.
     */
    private Map<String, I_CmsSearchConfigurationFacetField> getDefaultFieldFacets(boolean categoryConjunction) {

        Map<String, I_CmsSearchConfigurationFacetField> fieldFacets = new HashMap<String, I_CmsSearchConfigurationFacetField>();
        fieldFacets.put(
            CmsListManager.FIELD_CATEGORIES,
            new CmsSearchConfigurationFacetField(
                CmsListManager.FIELD_CATEGORIES,
                null,
                Integer.valueOf(1),
                Integer.valueOf(200),
                null,
                "Category",
                SortOrder.index,
                null,
                Boolean.valueOf(categoryConjunction),
                null,
                Boolean.TRUE));
        fieldFacets.put(
            CmsListManager.FIELD_PARENT_FOLDERS,
            new CmsSearchConfigurationFacetField(
                CmsListManager.FIELD_PARENT_FOLDERS,
                null,
                Integer.valueOf(1),
                Integer.valueOf(200),
                null,
                "Folders",
                SortOrder.index,
                null,
                Boolean.FALSE,
                null,
                Boolean.TRUE));
        return Collections.unmodifiableMap(fieldFacets);

    }

    /**
     * Returns a flag, indicating if the release and expiration date should be ignored.
     * @return a flag, indicating if the release and expiration date should be ignored.
     */
    private Boolean getIgnoreReleaseAndExpiration() {

        return Boolean.valueOf(m_config.isShowExpired());
    }
}

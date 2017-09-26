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
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONException;
import org.opencms.jsp.search.config.CmsSearchConfigurationFacetField;
import org.opencms.jsp.search.config.CmsSearchConfigurationFacetRange;
import org.opencms.jsp.search.config.CmsSearchConfigurationSortOption;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacet.SortOrder;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetField;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetRange;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationSortOption;
import org.opencms.relations.CmsLink;
import org.opencms.ui.apps.lists.CmsListManager;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.CmsXmlDisplayFormatterValue;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Search configuration parser using a list configuration file as the base configuration with additional JSON.<p>
 */
public class CmsSimpleSearchConfigurationParser extends CmsJSONSearchConfigurationParser {

    /** Sort options that are available by default. */
    public static enum SortOption {
        /** Sort by date ascending */
        DATE_ASC,
        /** Sort by date descending */
        DATE_DESC,
        /** Sort by title ascending */
        TITLE_ASC,
        /** Sort by title descending */
        TITLE_DESC,
        /** Sort by order ascending */
        ORDER_ASC,
        /** Sort by order descending */
        ORDER_DESC;

        /**
         * Generates the suitable {@link I_CmsSearchConfigurationSortOption} for the option.
         * @param l the locale for which the option should be created
         * @return the created {@link I_CmsSearchConfigurationSortOption}
         */
        public I_CmsSearchConfigurationSortOption getOption(Locale l) {

            switch (this) {
                case DATE_ASC:
                    return new CmsSearchConfigurationSortOption(
                        "date.asc",
                        "date_asc",
                        "instancedate" + (null != l ? "_" + l.toString() : "") + "_dt asc");
                case DATE_DESC:
                    return new CmsSearchConfigurationSortOption(
                        "date.desc",
                        "date_desc",
                        "instancedate" + (null != l ? "_" + l.toString() : "") + "_dt desc");
                case TITLE_ASC:
                    return new CmsSearchConfigurationSortOption(
                        "title.asc",
                        "title_asc",
                        "disptitle" + (null != l ? "_" + l.toString() : "") + "_s asc");
                case TITLE_DESC:
                    return new CmsSearchConfigurationSortOption(
                        "title.desc",
                        "title_desc",
                        "disptitle" + (null != l ? "_" + l.toString() : "") + "_s desc");
                case ORDER_ASC:
                    return new CmsSearchConfigurationSortOption(
                        "order.asc",
                        "order_asc",
                        "disporder" + (null != l ? "_" + l.toString() : "") + "_i asc");
                case ORDER_DESC:
                    return new CmsSearchConfigurationSortOption(
                        "order.desc",
                        "order_desc",
                        "disporder" + (null != l ? "_" + l.toString() : "") + "_i desc");
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    /** The current cms context. */
    private CmsObject m_cms;

    /** The list configuration content. */
    private CmsXmlContent m_content;

    /**
     * Constructor.<p>
     *
     * @param cms the cms context
     * @param content the list configuration content
     * @param additionalParamJSON the additional JSON configuration
     *
     * @throws JSONException in case parsing the JSON fails
     */
    public CmsSimpleSearchConfigurationParser(CmsObject cms, CmsXmlContent content, String additionalParamJSON)
    throws JSONException {
        super(CmsStringUtil.isEmptyOrWhitespaceOnly(additionalParamJSON) ? "{}" : additionalParamJSON);
        m_cms = cms;
        m_content = content;

    }

    /** The default field facets.
     *
     * @param categoryConjunction flag, indicating if category selections in the facet should be "AND" combined.
     * @return the default field facets.
     */
    private static final Map<String, I_CmsSearchConfigurationFacetField> getDefaultFieldFacets(
        Boolean categoryConjunction) {

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
                categoryConjunction != null ? categoryConjunction : Boolean.FALSE,
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
    * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseFieldFacets()
    */
    @Override
    public Map<String, I_CmsSearchConfigurationFacetField> parseFieldFacets() {

        if (m_configObject.has(JSON_KEY_FIELD_FACETS)) {
            return super.parseFieldFacets();
        } else {
            I_CmsXmlContentValue categoryConjunctionVal = m_content.getValue(
                CmsListManager.N_CATEGORY_CONJUNCTION,
                CmsLocaleManager.MASTER_LOCALE);
            if (((categoryConjunctionVal != null)
                && Boolean.parseBoolean(categoryConjunctionVal.getStringValue(m_cms)))) {
                return getDefaultFieldFacets(Boolean.TRUE);
            } else {
                return getDefaultFieldFacets(Boolean.FALSE);
            }
        }
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
                String.format(CmsListManager.FIELD_DATE, m_cms.getRequestContext().getLocale().toString()),
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
            modifier = "{!type=edismax qf=\"content_"
                + m_cms.getRequestContext().getLocale().toString()
                + " Title_prop spell\"}%(query)";
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
            SortOption currentOption = SortOption.valueOf(
                m_content.getValue(CmsListManager.N_SORT_ORDER, CmsLocaleManager.MASTER_LOCALE).getStringValue(m_cms));
            Locale locale = m_cms.getRequestContext().getLocale();
            options.add(currentOption.getOption(locale));
            SortOption[] sortOptions = SortOption.values();
            for (int i = 0; i < sortOptions.length; i++) {
                SortOption option = sortOptions[i];
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

        String result = "";
        boolean first = true;
        List<I_CmsXmlContentValue> balckListValues = m_content.getValues(
            CmsListManager.N_BLACKLIST,
            CmsLocaleManager.MASTER_LOCALE);
        if (!balckListValues.isEmpty()) {
            for (I_CmsXmlContentValue value : balckListValues) {
                if (!first) {
                    result += " OR ";
                }
                result += "\"" + m_cms.getRequestContext().addSiteRoot(value.getStringValue(m_cms)) + "\"";
                first = false;
            }
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result)) {
            result = "&fq=-path:(" + result + ")";
        }
        return result;
    }

    /**
     * Returns the category filter string.<p>
     *
     * @return the category filter
     */
    String getCategoryFilter() {

        String categories = "";
        I_CmsXmlContentValue categoriesVal = m_content.getValue(
            CmsListManager.N_CATEGORY,
            CmsLocaleManager.MASTER_LOCALE);
        if (categoriesVal != null) {
            categories = categoriesVal.getStringValue(m_cms);
        }
        String result = "";
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(categories)) {
            result = "&fq=category_exact:(";
            for (String path : categories.split(",")) {
                result += path + " ";
            }
            result = result.substring(0, result.length() - 1);
            result += ")";
        }
        return result;
    }

    /**
     * Returns the filter query string.<p>
     *
     * @return the filter query
     */
    String getFilterQuery() {

        String result = "";
        I_CmsXmlContentValue filterVal = m_content.getValue(
            CmsListManager.N_FILTER_QUERY,
            CmsLocaleManager.MASTER_LOCALE);
        if (filterVal != null) {
            result = filterVal.getStringValue(m_cms);
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result) && !result.startsWith("&")) {
            result = "&" + result;
        }
        I_CmsXmlContentValue currentOnlyVal = m_content.getValue(
            CmsListManager.N_CURRENT_ONLY,
            CmsLocaleManager.MASTER_LOCALE);
        if (((currentOnlyVal != null) && Boolean.parseBoolean(currentOnlyVal.getStringValue(m_cms)))) {
            result += "&fq=instancedatecurrenttill_"
                + m_cms.getRequestContext().getLocale().toString()
                + "_dt:[NOW/DAY TO *]";
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
        boolean first = true;
        List<I_CmsXmlContentValue> folderValues = m_content.getValues(
            CmsListManager.N_SEARCH_FOLDER,
            CmsLocaleManager.MASTER_LOCALE);
        if (!folderValues.isEmpty()) {
            for (I_CmsXmlContentValue value : folderValues) {
                if (!first) {
                    result += " OR ";
                }
                CmsLink link = ((CmsXmlVfsFileValue)value).getLink(m_cms);
                if (null != link) {
                    result += "\"" + CmsFileUtil.normalizePath(link.getSiteRoot() + link.getSitePath()) + "\"";
                    first = false;
                }
            }
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(result)) {
            result = "fq=parent-folders:(\"/\")";
        } else {
            result = "fq=parent-folders:(" + result + ")";
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
        boolean first = true;
        List<I_CmsXmlContentValue> typeValues = m_content.getValues(
            CmsListManager.N_DISPLAY_TYPE,
            CmsLocaleManager.MASTER_LOCALE);
        if (!typeValues.isEmpty()) {
            for (I_CmsXmlContentValue value : typeValues) {
                if (!first) {
                    result += " OR ";
                }
                String type = value.getStringValue(m_cms);
                if (type.contains(CmsXmlDisplayFormatterValue.SEPARATOR)) {
                    type = type.substring(0, type.indexOf(CmsXmlDisplayFormatterValue.SEPARATOR));
                }
                result += "\"" + type + "\"";
                first = false;
            }
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result)) {
            result = "&fq=type:(" + result + ")";
        }
        return result;
    }

    /**
     * Returns a flag, indicating if the release and expiration date should be ignored.
     * @return a flag, indicating if the release and expiration date should be ignored.
     */
    private Boolean getIgnoreReleaseAndExpiration() {

        I_CmsXmlContentValue expiredVal = m_content.getValue(
            CmsListManager.N_SHOW_EXPIRED,
            CmsLocaleManager.MASTER_LOCALE);
        if ((expiredVal != null) && Boolean.parseBoolean(expiredVal.getStringValue(m_cms))) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}

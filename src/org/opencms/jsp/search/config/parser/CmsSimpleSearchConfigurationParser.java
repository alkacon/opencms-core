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
import org.opencms.jsp.search.config.I_CmsSearchConfigurationDidYouMean;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacet.SortOrder;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetField;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetQuery;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationFacetRange;
import org.opencms.jsp.search.config.I_CmsSearchConfigurationHighlighting;
import org.opencms.ui.apps.lists.CmsListConfigurationForm;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.CmsXmlDisplayFormatterValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Search configuration parser using a list configuration file as the base configuration with additional JSON.<p>
 */
public class CmsSimpleSearchConfigurationParser extends CmsJSONSearchConfigurationParser {

    /** The default field facets. */
    private static final Map<String, I_CmsSearchConfigurationFacetField> DEFAULT_FIELD_FACETS;
    static {
        Map<String, I_CmsSearchConfigurationFacetField> fieldFacets = new HashMap<String, I_CmsSearchConfigurationFacetField>();
        fieldFacets.put(
            CmsListConfigurationForm.FIELD_CATEGORIES,
            new CmsSearchConfigurationFacetField(
                CmsListConfigurationForm.FIELD_CATEGORIES,
                null,
                Integer.valueOf(1),
                Integer.valueOf(200),
                null,
                "Category",
                SortOrder.index,
                null,
                Boolean.FALSE,
                null,
                Boolean.TRUE));
        fieldFacets.put(
            CmsListConfigurationForm.FIELD_PARENT_FOLDERS,
            new CmsSearchConfigurationFacetField(
                CmsListConfigurationForm.FIELD_PARENT_FOLDERS,
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
        DEFAULT_FIELD_FACETS = Collections.unmodifiableMap(fieldFacets);

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

    /**
     * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseDidYouMean()
     */
    @Override
    public I_CmsSearchConfigurationDidYouMean parseDidYouMean() {

        return null;
    }

    /**
    * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseFieldFacets()
    */
    @Override
    public Map<String, I_CmsSearchConfigurationFacetField> parseFieldFacets() {

        if (m_configObject.has(JSON_KEY_FIELD_FACETS)) {
            return super.parseFieldFacets();
        } else {
            return DEFAULT_FIELD_FACETS;
        }
    }

    /**
    * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseHighlighter()
    */
    @Override
    public I_CmsSearchConfigurationHighlighting parseHighlighter() {

        if (m_configObject.has(JSON_KEY_HIGHLIGHTER)) {
            return super.parseHighlighter();
        } else {
            return null;
        }
    }

    /**
    * @see org.opencms.jsp.search.config.parser.I_CmsSearchConfigurationParser#parseQueryFacet()
    */
    @Override
    public I_CmsSearchConfigurationFacetQuery parseQueryFacet() {

        if (m_configObject.has(JSON_KEY_QUERY_FACET)) {
            return super.parseQueryFacet();
        } else {
            return null;
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
                String.format(CmsListConfigurationForm.FIELD_DATE, m_cms.getRequestContext().getLocale().toString()),
                "NOW/YEAR-20YEARS",
                "NOW/MONTH+2YEARS",
                "+1MONTHS",
                null,
                Boolean.FALSE,
                "newsdate",
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
     * Returns the blacklist filter.<p>
     *
     * @return the blacklist filter
     */
    String getBlacklistFilter() {

        String result = "";
        boolean first = true;
        List<I_CmsXmlContentValue> balckListValues = m_content.getValues(
            CmsListConfigurationForm.N_BLACKLIST,
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
            CmsListConfigurationForm.N_CATEGORY,
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
            CmsListConfigurationForm.N_FILTER_QUERY,
            CmsLocaleManager.MASTER_LOCALE);
        if (filterVal != null) {
            result = filterVal.getStringValue(m_cms);
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result) && !result.startsWith("&")) {
            result = "&" + result;
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
            CmsListConfigurationForm.N_SEARCH_FOLDER,
            CmsLocaleManager.MASTER_LOCALE);
        if (!folderValues.isEmpty()) {
            for (I_CmsXmlContentValue value : folderValues) {
                if (!first) {
                    result += " OR ";
                }
                result += "\"" + m_cms.getRequestContext().addSiteRoot(value.getStringValue(m_cms)) + "\"";
                first = false;
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
            CmsListConfigurationForm.N_DISPLAY_TYPE,
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

}

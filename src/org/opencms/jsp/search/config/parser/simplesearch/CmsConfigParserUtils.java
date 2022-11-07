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

package org.opencms.jsp.search.config.parser.simplesearch;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.jsp.search.config.parser.simplesearch.CmsConfigurationBean.CombinationMode;
import org.opencms.jsp.search.config.parser.simplesearch.daterestrictions.CmsDateRestrictionParser;
import org.opencms.jsp.search.config.parser.simplesearch.daterestrictions.I_CmsDateRestriction;
import org.opencms.jsp.search.config.parser.simplesearch.preconfiguredrestrictions.CmsRestrictionsBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.relations.CmsCategoryService;
import org.opencms.relations.CmsLink;
import org.opencms.util.CmsGeoUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentValueLocation;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;

/** Utils to read and update the list configuration. */
public final class CmsConfigParserUtils {

    /** The logger for this class. */
    static final Log LOG = CmsLog.getLog(CmsConfigParserUtils.class.getName());

    /** List configuration node name and field key. */
    public static final String N_BLACKLIST = "Blacklist";

    /** List configuration node name and field key. */
    public static final String N_CATEGORY = "Category";

    /** List configuration node name and field key. */
    private static final String N_CATEGORY_FOLDER_RESTRICTION = "CategoryFolderFilter";

    /** List configuration node name and field key. */
    private static final String N_COORDINATES = "Coordinates";

    /** List configuration node name and field key. */
    private static final String N_FOLDER = "Folder";

    /** List configuration node name for the category mode. */
    public static final String N_CATEGORY_MODE = "CategoryMode";

    /** XML content node name. */
    public static final String N_DATE_RESTRICTION = "DateRestriction";

    /** List configuration node name and field key. */
    public static final String N_DISPLAY_TYPE = "TypesToCollect";

    /** List configuration node name and field key. */
    public static final String N_FILTER_MULTI_DAY = "FilterMultiDay";

    /** List configuration node name and field key. */
    public static final String N_FILTER_QUERY = "FilterQuery";

    /** List configuration node name and field key. */
    public static final String N_GEO_FILTER = "GeoFilter";

    /** List configuration node name and field key. */
    public static final String N_KEY = "Key";

    /** List configuration node name and field key. */
    public static final String N_PARAMETER = "Parameter";

    /** List configuration node name and field key. */
    public static final String N_RADIUS = "Radius";

    /** List configuration node name and field key. */
    public static final String N_SEARCH_FOLDER = "SearchFolder";

    /** List configuration node name and field key. */
    public static final String N_SHOW_EXPIRED = "ShowExpired";

    /** List configuration node name and field key. */
    public static final String N_SORT_ORDER = "SortOrder";

    /** List configuration node name and field key. */
    public static final String N_TITLE = "Title";

    /** List configuration node name and field key. */
    public static final String N_VALUE = "Value";

    /** List configuration node name and field key. */
    public static final String N_MAX_RESULTS = "MaxResults";

    /** List configuration node name and field key. */
    public static final String N_PRECONFIGURED_FILTER_QUERY = "PreconfiguredFilterQuery";

    /** List configuration node name and field key. */
    public static final String N_RULE = "Rule";

    /** The parameter fields. */
    public static final String[] PARAMETER_FIELDS = new String[] {
        N_TITLE,
        N_CATEGORY,
        N_FILTER_MULTI_DAY,
        N_FILTER_QUERY,
        N_SORT_ORDER,
        N_SHOW_EXPIRED,
        N_MAX_RESULTS};

    /** Statically initialized map from node names to parameter names of the list configuration bean. */
    private static Map<String, String> PARAMS_MAP = createParamsMap();

    /**
     * Parses the list configuration resource.<p>
     *
     * @param cms the CMS context to use
     * @param res the list configuration resource
     *
     * @return the configuration data bean
     */
    public static CmsConfigurationBean parseListConfiguration(CmsObject cms, CmsResource res) {

        CmsConfigurationBean result = new CmsConfigurationBean();
        try {
            CmsFile configFile = cms.readFile(res);
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, configFile);
            Locale locale = CmsLocaleManager.MASTER_LOCALE;

            if (!content.hasLocale(locale)) {
                locale = content.getLocales().get(0);
            }
            for (String field : PARAMETER_FIELDS) {
                String val = content.getStringValue(cms, field, locale);
                if (N_CATEGORY.equals(field)) {
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(val)) {
                        result.setCategories(Arrays.asList(val.split(",")));
                    } else {
                        result.setCategories(Collections.<String> emptyList());
                    }
                } else {
                    String param = PARAMS_MAP.get(field);
                    if (param != null) {
                        result.setParameterValue(param, val);
                    } else {
                        LOG.error("Parameter value for field \"" + field + "\" is unknown and hence ignored.");
                    }
                }
            }

            I_CmsXmlContentValue restrictValue = content.getValue(N_DATE_RESTRICTION, locale);
            if (restrictValue != null) {
                CmsDateRestrictionParser parser = new CmsDateRestrictionParser(cms);
                I_CmsDateRestriction restriction = parser.parse(new CmsXmlContentValueLocation(restrictValue));
                if (restriction == null) {
                    LOG.warn(
                        "Improper date restriction configuration in content "
                            + content.getFile().getRootPath()
                            + ", online="
                            + cms.getRequestContext().getCurrentProject().isOnlineProject());
                }
                result.setDateRestriction(restriction);
            }

            I_CmsXmlContentValue geoFilterValue = content.getValue(N_GEO_FILTER, locale);
            if (geoFilterValue != null) {
                String coordinatesPath = geoFilterValue.getPath() + "/" + N_COORDINATES;
                String radiusPath = geoFilterValue.getPath() + "/" + N_RADIUS;
                I_CmsXmlContentValue coordinatesValue = content.getValue(coordinatesPath, locale);
                I_CmsXmlContentValue radiusValue = content.getValue(radiusPath, locale);
                String coordinates = CmsGeoUtil.parseCoordinates(coordinatesValue.getStringValue(cms));
                String radius = radiusValue.getStringValue(cms);
                boolean radiusValid = false;
                try {
                    Float.parseFloat(radius);
                    radiusValid = true;
                } catch (NumberFormatException e) {
                    radiusValid = false;
                }
                if ((coordinates != null) && radiusValid) {
                    CmsGeoFilterBean listGeoFilterBean = new CmsGeoFilterBean(coordinates, radius);
                    result.setGeoFilter(listGeoFilterBean);
                } else {
                    LOG.warn(
                        "Improper Geo filter in content "
                            + content.getFile().getRootPath()
                            + ", online="
                            + cms.getRequestContext().getCurrentProject().isOnlineProject());
                }
            }

            I_CmsXmlContentValue categoryModeVal = content.getValue(N_CATEGORY_MODE, locale);
            CombinationMode categoryMode = CombinationMode.OR;
            if (categoryModeVal != null) {
                try {
                    categoryMode = CombinationMode.valueOf(categoryModeVal.getStringValue(cms));
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            result.setCategoryMode(categoryMode);

            LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();
            for (I_CmsXmlContentValue parameter : content.getValues(N_PARAMETER, locale)) {
                I_CmsXmlContentValue keyVal = content.getValue(parameter.getPath() + "/" + N_KEY, locale);
                I_CmsXmlContentValue valueVal = content.getValue(parameter.getPath() + "/" + N_VALUE, locale);
                if ((keyVal != null)
                    && CmsStringUtil.isNotEmptyOrWhitespaceOnly(keyVal.getStringValue(cms))
                    && (valueVal != null)) {
                    parameters.put(keyVal.getStringValue(cms), valueVal.getStringValue(cms));
                }
            }
            result.setAdditionalParameters(parameters);
            List<String> displayTypes = new ArrayList<String>();
            List<I_CmsXmlContentValue> typeValues = content.getValues(N_DISPLAY_TYPE, locale);
            if (!typeValues.isEmpty()) {
                for (I_CmsXmlContentValue value : typeValues) {
                    displayTypes.add(value.getStringValue(cms));
                }
            }
            result.setDisplayTypes(displayTypes);
            List<String> folders = new ArrayList<String>();
            List<I_CmsXmlContentValue> folderValues = content.getValues(N_SEARCH_FOLDER, locale);
            if (!folderValues.isEmpty()) {
                for (I_CmsXmlContentValue value : folderValues) {
                    CmsLink val = ((CmsXmlVfsFileValue)value).getLink(cms);
                    if (val != null) {
                        // we are using root paths
                        folders.add(cms.getRequestContext().addSiteRoot(val.getSitePath(cms)));
                    }
                }
            }
            result.setFolders(folders);
            List<CmsUUID> blackList = new ArrayList<CmsUUID>();
            List<I_CmsXmlContentValue> blacklistValues = content.getValues(N_BLACKLIST, locale);
            if (!blacklistValues.isEmpty()) {
                for (I_CmsXmlContentValue value : blacklistValues) {
                    CmsLink link = ((CmsXmlVfsFileValue)value).getLink(cms);
                    if (link != null) {
                        blackList.add(link.getStructureId());
                    }
                }
            }
            result.setBlacklist(blackList);
            List<I_CmsXmlContentValue> categoryFolderRestrictions = content.getValues(
                N_CATEGORY_FOLDER_RESTRICTION,
                locale);
            if (!categoryFolderRestrictions.isEmpty()) {
                for (I_CmsXmlContentValue restriction : categoryFolderRestrictions) {
                    List<String> restrictionFolders = new ArrayList<>();
                    List<I_CmsXmlContentValue> folderVals = content.getValues(
                        CmsXmlUtils.concatXpath(restriction.getPath(), N_FOLDER),
                        locale);
                    for (I_CmsXmlContentValue folderVal : folderVals) {
                        CmsLink val = ((CmsXmlVfsFileValue)folderVal).getLink(cms);
                        if (val != null) {
                            // we are using root paths
                            restrictionFolders.add(cms.getRequestContext().addSiteRoot(val.getSitePath(cms)));
                        }
                    }
                    List<String> restrictionCategorySitePaths;
                    I_CmsXmlContentValue categoryVal = content.getValue(
                        CmsXmlUtils.concatXpath(restriction.getPath(), N_CATEGORY),
                        locale);
                    String categoryString = null != categoryVal ? categoryVal.getStringValue(cms) : "";
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(categoryString)) {
                        restrictionCategorySitePaths = Arrays.asList(categoryString.split(","));
                    } else {
                        restrictionCategorySitePaths = Collections.<String> emptyList();
                    }
                    List<String> restrictionCategories = new ArrayList<>(restrictionCategorySitePaths.size());
                    for (String sitePath : restrictionCategorySitePaths) {
                        try {
                            String path = CmsCategoryService.getInstance().getCategory(
                                cms,
                                cms.getRequestContext().addSiteRoot(sitePath)).getPath();
                            restrictionCategories.add(path);
                        } catch (CmsException e) {
                            LOG.warn(e.getLocalizedMessage(), e);
                        }
                    }
                    String restrictionCategoryMode = content.getValue(
                        CmsXmlUtils.concatXpath(restriction.getPath(), N_CATEGORY_MODE),
                        locale).getStringValue(cms);
                    result.addCategoryFolderFilter(
                        new CmsCategoryFolderRestrictionBean(
                            restrictionCategories,
                            restrictionFolders,
                            null == restrictionCategoryMode ? null : CombinationMode.valueOf(restrictionCategoryMode)));

                }
            }
            List<I_CmsXmlContentValue> preconfiguredRestrictions = content.getValues(
                N_PRECONFIGURED_FILTER_QUERY,
                locale);
            if (!preconfiguredRestrictions.isEmpty()) {
                CmsRestrictionsBean restrictionBean = new CmsRestrictionsBean();
                for (I_CmsXmlContentValue restriction : preconfiguredRestrictions) {
                    String restrictionRule = content.getValue(
                        CmsXmlUtils.concatXpath(restriction.getPath(), N_RULE),
                        locale).getStringValue(cms);
                    List<I_CmsXmlContentValue> restrictionVals = content.getValues(
                        CmsXmlUtils.concatXpath(restriction.getPath(), N_VALUE),
                        locale);
                    List<String> restrictionValues = restrictionVals.stream().map(v -> v.getStringValue(cms)).collect(
                        Collectors.toList());
                    restrictionBean.addRestriction(restrictionRule, restrictionValues);
                }
                result.setPreconfiguredRestrictions(restrictionBean);
            }
        } catch (CmsException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Updates the black list entries in the provided xml content.
     * @param cms the cms context.
     * @param content the xml content to update (must be of type list_config)
     * @param configBean the config bean to get the blacklist entries from.
     * @return the updated content (update is in-place).
     */
    public static CmsXmlContent updateBlackList(
        CmsObject cms,
        CmsXmlContent content,
        CmsConfigurationBean configBean) {

        // list configurations are single locale contents
        Locale locale = CmsLocaleManager.MASTER_LOCALE;
        int count = 0;
        while (content.hasValue(N_BLACKLIST, locale)) {
            content.removeValue(N_BLACKLIST, locale, 0);
        }
        for (CmsUUID hiddenId : configBean.getBlacklist()) {
            CmsXmlVfsFileValue contentVal;
            contentVal = (CmsXmlVfsFileValue)content.addValue(cms, N_BLACKLIST, locale, count);
            contentVal.setIdValue(cms, hiddenId);
            count++;
        }
        return content;
    }

    /**
     * Creates the parameter map, mapping from noed names to parameters of the list configuration bean.
     * @return the parameter map.
     */
    private static Map<String, String> createParamsMap() {

        Map<String, String> result = new HashMap<>();
        result.put(N_TITLE, CmsConfigurationBean.PARAM_TITLE);
        result.put(N_FILTER_MULTI_DAY, CmsConfigurationBean.PARAM_FILTER_MULTI_DAY);
        result.put(N_FILTER_QUERY, CmsConfigurationBean.PARAM_FILTER_QUERY);
        result.put(N_SORT_ORDER, CmsConfigurationBean.PARAM_SORT_ORDER);
        result.put(N_SHOW_EXPIRED, CmsConfigurationBean.PARAM_SHOW_EXPIRED);
        result.put(N_MAX_RESULTS, CmsConfigurationBean.PARAM_MAX_RESULTS);
        return Collections.unmodifiableMap(result);
    }

}

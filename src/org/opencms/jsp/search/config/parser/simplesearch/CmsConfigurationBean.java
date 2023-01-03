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

import org.opencms.jsp.search.config.parser.simplesearch.daterestrictions.I_CmsDateRestriction;
import org.opencms.jsp.search.config.parser.simplesearch.preconfiguredrestrictions.CmsRestrictionsBean;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;
import org.opencms.xml.types.CmsXmlDisplayFormatterValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * The list configuration data.<p>
 */
public class CmsConfigurationBean {

    /**
     * Enum representing how filter queries should be combined in a search.<p>
     */
    public static enum CombinationMode {
        /** Combine with AND. */
        AND,

        /** Combine with OR. */
        OR;
    }

    /** Parameter field key. */
    public static final String N_TITLE = "Title";

    /** Parameter field key. */
    public static final String PARAM_TITLE = "Title";

    /** Parameter field key. */
    public static final String PARAM_FILTER_MULTI_DAY = "FilterMultiDay";

    /** Parameter field key. */
    public static final String PARAM_FILTER_QUERY = "FilterQuery";

    /** Parameter field key. */
    public static final String PARAM_SORT_ORDER = "SortOrder";

    /** Parameter field key. */
    public static final String PARAM_SHOW_EXPIRED = "ShowExpired";

    /** Parameter field key. */
    public static final String PARAM_MAX_RESULTS = "MaxResults";

    /** The logger for this class. */
    static final Log LOG = CmsLog.getLog(CmsConfigurationBean.class.getName());

    /** Special parameter to configure the maximally returned results. */
    private static final String ADDITIONAL_PARAM_MAX_RETURNED_RESULTS = "maxresults";

    /** The additional content parameters. */
    private Map<String, String> m_additionalParameters;

    /** The resource blacklist. */
    private List<CmsUUID> m_blacklist;

    /** The categories. */
    private List<String> m_categories;

    /** The category mode. */
    private CombinationMode m_categoryMode;

    /** The date restriction. */
    private I_CmsDateRestriction m_dateRestriction;

    /** The display types. */
    private List<String> m_dislayTypes;

    /** The Geo filter */
    private CmsGeoFilterBean m_geoFilter;

    /** The folders. */
    private List<String> m_folders;

    /** Search parameters by configuration node name. */
    private Map<String, String> m_parameterFields;

    /** The preconfigured restrictions */
    private CmsRestrictionsBean m_preconfiguredRestrictions;

    /** Combined category and folder restrictions. */
    private List<CmsCategoryFolderRestrictionBean> m_categoryFolderRestrictions = new ArrayList<>();

    /**
     * Constructor.<p>
     */
    public CmsConfigurationBean() {

        m_parameterFields = new HashMap<String, String>();
    }

    /**
     * Extracts the resource type name from a display type string.
     * 
     * @param displayType the display type
     * @return the resource type name 
     */
    public static String getResourceTypeForDisplayType(String displayType) {

        String type = displayType;
        if (type.contains(CmsXmlDisplayFormatterValue.SEPARATOR)) {
            type = type.substring(0, type.indexOf(CmsXmlDisplayFormatterValue.SEPARATOR));
        }
        return type;
    }

    /**
     * Add a combined category-folder restriction.
     * @param listCategoryFolderRestrictionBean the category-folder restriction to add.
     */
    public void addCategoryFolderFilter(CmsCategoryFolderRestrictionBean listCategoryFolderRestrictionBean) {

        m_categoryFolderRestrictions.add(listCategoryFolderRestrictionBean);

    }

    /**
     * Returns the additional content parameters.<p>
     *
     * @return the additional content parameters
     */
    public Map<String, String> getAdditionalParameters() {

        return m_additionalParameters;
    }

    /**
     * Returns the black list.<p>
     *
     * @return the black list
     */
    public List<CmsUUID> getBlacklist() {

        return m_blacklist;
    }

    /**
     * Returns the categories.<p>
     *
     * @return the categories
     */
    public List<String> getCategories() {

        return m_categories;
    }

    /**
     * Returns the combined category-folder restrictions.<p>
     *
     * @return the combined category-folder restrictions
     */
    public List<CmsCategoryFolderRestrictionBean> getCategoryFolderRestrictions() {

        return m_categoryFolderRestrictions;
    }

    /**
     * Gets the category mode.<p>
     *
     * @return the category mode
     */
    public CombinationMode getCategoryMode() {

        return m_categoryMode;
    }

    /**
     * Gets the date restriction.<p>
     *
     * @return the date restriction
     */
    public I_CmsDateRestriction getDateRestriction() {

        return m_dateRestriction;
    }

    /**
     * Returns the display types.<p>
     *
     * @return the display types
     */
    public List<String> getDisplayTypes() {

        return m_dislayTypes;
    }

    /**
     * Gets the filter query.<p>
     *
     * @return the filter query
     */
    public String getFilterQuery() {

        return m_parameterFields.get(PARAM_FILTER_QUERY);
    }

    /**
     * Returns the folders.<p>
     *
     * @return the folders
     */
    public List<String> getFolders() {

        return m_folders;
    }

    /**
     * Returns the Geo filter.<p>
     *
     * @return the Geo filter
     */
    public CmsGeoFilterBean getGeoFilter() {

        return m_geoFilter;
    }

    /**
     * Returns the number of results to return maximally, or <code>null</code> if not explicitly specified.
     * @return the number of results to return maximally, or <code>null</code> if not explicitly specified.
     */
    public Integer getMaximallyReturnedResults() {

        String resString = m_parameterFields.get(PARAM_MAX_RESULTS);
        // Fallback, we first added the restriction as additional parameter. To make it more obvious, we integrated it as extra field.
        // Only if the extra field is not set, we use the additional parameter to be backward compatible.
        if (null == resString) {
            m_additionalParameters.get(ADDITIONAL_PARAM_MAX_RETURNED_RESULTS);
        }
        if (null != resString) {
            try {
                return Integer.valueOf(resString);
            } catch (NumberFormatException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Ignoring invalid maxresults param " + resString + " in list-config.");
                }
            }
        }
        return null;
    }

    /**
     * Returns the parameter map.<p>
     *
     * @return the parameters
     */
    public Map<String, String> getParameters() {

        return m_parameterFields;
    }

    /**
     * Returns the parameter by name.<p>
     *
     * @param key the parameter name
     *
     * @return the parameter value
     */
    public String getParameterValue(String key) {

        return m_parameterFields.get(key);
    }

    /**
     * Returns the preconfigured restrictions.
     * @return the preconfigured restrictions.
     */
    public CmsRestrictionsBean getPreconfiguredRestrictions() {

        return m_preconfiguredRestrictions;
    }

    /**
     * Gets the sort order.<p>
     *
     * @return the sort order
     */
    public String getSortOrder() {

        return getParameterValue(PARAM_SORT_ORDER);
    }

    /**
     * Returns the search types.<p>
     *
     * @return the search types
     */
    public List<String> getTypes() {

        List<String> result = new ArrayList<String>();
        if (m_dislayTypes != null) {
            for (String displayType : m_dislayTypes) {
                String type = getResourceTypeForDisplayType(displayType);
                if (!result.contains(type)) {
                    result.add(type);
                }
            }
        }
        return result;
    }

    /**
     * Returns a flag, indicating if there are preconfigured restrictions.
     *
     * @return <code>true</code> iff there are preconfiugred restrictions, <code>false</code> otherwise.
     */
    public boolean hasPreconfiguredRestrictions() {

        return (null != m_preconfiguredRestrictions) && m_preconfiguredRestrictions.hasRestrictions();
    }

    /**
     * Returns a flag, indicating if there are preconfigured restrictions for the provided type.
     *
     * @param type the type to check the existence of preconfigured restrictions for.
     *
     * @return <code>true</code> iff there are preconfigured restrictions for the provided type, <code>false</code> otherwise.
     */
    public boolean hasTypeSpecificRestriction(String type) {

        return (null != m_preconfiguredRestrictions) && m_preconfiguredRestrictions.hasRestrictionForType(type);
    }

    /**
     * Returns the 'show expired' setting.<p>
     *
     * @return the 'show expired' setting
     */
    public boolean isShowExpired() {

        return Boolean.parseBoolean(m_parameterFields.get(PARAM_SHOW_EXPIRED));

    }

    /**
     * Sets the additional content parameters.<p>
     *
     * @param additionalParameters the additional content parameters to set
     */
    public void setAdditionalParameters(Map<String, String> additionalParameters) {

        m_additionalParameters = additionalParameters;
    }

    /**
     * Sets the blacklist.<p>
     *
     * @param blacklist the blacklist
     */
    public void setBlacklist(List<CmsUUID> blacklist) {

        m_blacklist = blacklist;
    }

    /**
     * Sets the categories.<p>
     *
     * @param categories the categories
     */
    public void setCategories(List<String> categories) {

        m_categories = categories;
    }

    /**
     * Sets the category mode.<p>
     *
     * @param categoryMode the category mode to set
     */
    public void setCategoryMode(CombinationMode categoryMode) {

        m_categoryMode = categoryMode;
    }

    /**
     * Sets the date restrictions.<p>
     *
     * @param restriction the date restrictions
     */
    public void setDateRestriction(I_CmsDateRestriction restriction) {

        m_dateRestriction = restriction;
    }

    /**
     * Sets the display types.<p>
     *
     * @param displayTypes the display types
     */
    public void setDisplayTypes(List<String> displayTypes) {

        m_dislayTypes = displayTypes;
    }

    /**
     * Sets the folders.<p>
     *
     * @param folders the folders
     */
    public void setFolders(List<String> folders) {

        m_folders = folders;
    }

    /**
     * Sets the Geo filter.<p>
     *
     * @param geoFilter the Geo filter
     */
    public void setGeoFilter(CmsGeoFilterBean geoFilter) {

        m_geoFilter = geoFilter;
    }

    /**
     * Sets the parameter by name.<p>
     *
     * @param name the parameter name
     * @param value the parameter value
     */
    public void setParameterValue(String name, String value) {

        m_parameterFields.put(name, value);

    }

    /**
     * Set the preconfigured restrictions.
     *
     * @param restrictionBean the restrictions to set.
     */
    public void setPreconfiguredRestrictions(CmsRestrictionsBean restrictionBean) {

        m_preconfiguredRestrictions = restrictionBean;
    }
}
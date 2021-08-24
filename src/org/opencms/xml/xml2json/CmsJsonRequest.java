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

package org.opencms.xml.xml2json;

import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.search.config.parser.CmsSimpleSearchConfigurationParser.SortOption;
import org.opencms.xml.xml2json.handler.CmsJsonHandlerContainerPage;
import org.opencms.xml.xml2json.handler.CmsJsonHandlerContext;
import org.opencms.xml.xml2json.handler.CmsJsonHandlerFolder;
import org.opencms.xml.xml2json.handler.CmsJsonHandlerList;
import org.opencms.xml.xml2json.handler.CmsJsonHandlerResource;
import org.opencms.xml.xml2json.handler.CmsJsonHandlerXmlContent;
import org.opencms.xml.xml2json.handler.I_CmsJsonHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

/**
 * Class representing a JSON request. Provides utility functions for parameter validation.
 */
public class CmsJsonRequest {

    /** The content request parameter. */
    public static final String PARAM_CONTENT = "content";

    /** The fallback locale request parameter. */
    public static final String PARAM_FALLBACK_LOCALE = "fallbackLocale";

    /** The levels request parameter. */
    public static final String PARAM_LEVELS = "levels";

    /** The locale request parameter. */
    public static final String PARAM_LOCALE = "locale";

    /** The path request parameter. */
    public static final String PARAM_PATH = "path";

    /** The rows request parameter. */
    public static final String PARAM_ROWS = "rows";

    /** The sort request parameter. */
    public static final String PARAM_SORT = "sort";

    /** The start request parameter. */
    public static final String PARAM_START = "start";

    /** The wrapper request parameter. */
    public static final String PARAM_WRAPPER = "wrapper";

    /** The JSON handler context. */
    CmsJsonHandlerContext m_context;

    /** The JSON handler initiating this request.*/
    I_CmsJsonHandler m_handler;

    /** The list of validation errors. */
    List<String> errors = new ArrayList<String>();

    /**
     * Creates a new JSON request.<p>
     *
     * @param context the JSON handler context
     * @param handler the JSON handler initiating this request
     */
    public CmsJsonRequest(CmsJsonHandlerContext context, I_CmsJsonHandler handler) {

        m_context = context;
        m_handler = handler;
    }

    /**
     * Returns the JSON handler context.<p>
     *
     * @return the JSON handler context
     */
    public CmsJsonHandlerContext getContext() {

        return m_context;
    }

    /**
     * Returns the errors of this request as JSON.<p>
     *
     * @return the errors as JSON
     * @throws JSONException if JSON rendering fails
     */
    public JSONObject getErrorsAsJson() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", HttpServletResponse.SC_BAD_REQUEST);
        for (String error : errors) {
            jsonObject.append("errors", error);
        }
        return jsonObject;
    }

    /**
     * Returns the boolean parameter value for a given string.<p>
     *
     * @param bool the string
     * @return the boolean
     */
    public Boolean getParamBoolean(String bool) {

        if ((bool == null) || bool.equals("false")) {
            return Boolean.valueOf(false);
        } else {
            return Boolean.valueOf(true);
        }
    }

    /**
     * Returns the content parameter as boolean.<p>
     *
     * @return the content parameter as boolean
     */
    public Boolean getParamContent() {

        String paramContent = m_context.getParameters().get(PARAM_CONTENT);
        return getParamBoolean(paramContent);
    }

    /**
     * Returns the fallback locale parameter as boolean.<p>
     *
     * @return the fallback locale parameter as boolean
     */
    public Boolean getParamFallbackLocale() {

        String paramFallbackLocale = m_context.getParameters().get(PARAM_FALLBACK_LOCALE);
        return getParamBoolean(paramFallbackLocale);
    }

    /**
     * Returns the levels parameter as integer.<p>
     *
     * @return the levels parameter as integer
     */
    public Integer getParamLevels() {

        String paramLevels = m_context.getParameters().get(PARAM_LEVELS);
        return paramLevels != null ? Integer.valueOf(paramLevels) : null;
    }

    /**
     * Returns the levels parameter as integer.<p>
     *
     * @param defaultLevels if parameter is not set return this default value
     * @return the levels parameter as integer
     */
    public Integer getParamLevels(int defaultLevels) {

        Integer levels = getParamLevels();
        return levels != null ? levels : Integer.valueOf(defaultLevels);
    }

    /**
     * Returns the locale parameter as string.<p>
     *
     * @return the levels parameter as integer
     */
    public String getParamLocale() {

        String paramLocale = m_context.getParameters().get(PARAM_LOCALE);
        return paramLocale;
    }

    /**
     * Returns the path parameter as string.<p>
     *
     * @return the path parameter as string
     */
    public String getParamPath() {

        String paramPath = m_context.getParameters().get(PARAM_PATH);
        return paramPath;
    }

    /**
     * Returns the rows parameter as integer.<p>
     *
     * @return the rows parameter as integer
     */
    public Integer getParamRows() {

        String paramRows = m_context.getParameters().get(PARAM_ROWS);
        return paramRows != null ? Integer.valueOf(paramRows) : null;
    }

    /**
     * Returns the rows parameter as integer.<p>
     *
     * @param defaultRows if parameter is not set return this default value
     * @return the rows parameter as integer
     */
    public Integer getParamRows(int defaultRows) {

        Integer rows = getParamStart();
        return rows != null ? rows : Integer.valueOf(defaultRows);
    }

    /**
     * Returns the sort parameter as string.<p>
     *
     * @return the sort parameter as string
     */
    public String getParamSort() {

        return m_context.getParameters().get(PARAM_SORT);
    }

    /**
     * Returns the sort parameter as string.<p>
     *
     * @param defaultSort if parameter is not set return this default value
     * @return the sort parameter as string
     */
    public String getParamSort(String defaultSort) {

        String sort = getParamSort();
        return sort != null ? sort : defaultSort;
    }

    /**
     * Returns the start parameter as integer.<p>
     *
     * @return the start parameter as integer
     */
    public Integer getParamStart() {

        String paramStart = m_context.getParameters().get(PARAM_START);
        return paramStart != null ? Integer.valueOf(paramStart) : null;
    }

    /**
     * Returns the rows parameter as integer.<p>
     *
     * @param defaultStart if parameter is not set return this default value
     * @return the rows parameter as integer
     */
    public Integer getParamStart(int defaultStart) {

        Integer start = getParamStart();
        return start != null ? start : Integer.valueOf(defaultStart);
    }

    /**
     * Returns the wrapper parameter as string.<p>
     *
     * @return the wrapper parameter as string
     */
    public Boolean getParamWrapper() {

        String paramWrapper = m_context.getParameters().get(PARAM_WRAPPER);
        return getParamBoolean(paramWrapper);
    }

    /**
     * Returns the wrapper parameter as string.<p>
     *
     * @param defaultWrapper if parameter is not set return this default value
     * @return the wrapper parameter as string
     */
    public Boolean getParamWrapper(boolean defaultWrapper) {

        String paramRaw = m_context.getParameters().get(PARAM_WRAPPER);
        if (paramRaw == null) {
            return Boolean.valueOf(defaultWrapper);
        }
        return getParamWrapper();
    }

    /**
     * Whether this JSON request has validation errors.
     *
     * @return whether has validation errors or not
     */
    public boolean hasErrors() {

        return !errors.isEmpty();
    }

    /**
     * Validates this request.<p>
     */
    public void validate() {

        validateRequest();
        validateDependencies();
        validateParamBooleanValue(PARAM_CONTENT, true);
        validateParamBooleanValue(PARAM_FALLBACK_LOCALE, true);
        validateParamPositiveIntegerValue(PARAM_LEVELS, false);
        validateParamPositiveIntegerValue(PARAM_ROWS, false);
        validateParamSortValue(PARAM_SORT, false);
        validateParamPositiveIntegerValue(PARAM_START, false);
        validateParamBooleanValue(PARAM_WRAPPER, true);
    }

    /**
     * Validates all parameter dependencies.
     */
    private void validateDependencies() {

        validateParamDepends(PARAM_FALLBACK_LOCALE, PARAM_LOCALE);
        validateParamDepends(PARAM_PATH, PARAM_LOCALE);
        validateParamDepends(PARAM_ROWS, PARAM_CONTENT);
        validateParamDepends(PARAM_SORT, PARAM_CONTENT);
        validateParamDepends(PARAM_START, PARAM_CONTENT);
    }

    /**
     * Validates a boolean request parameter.<p>
     *
     * @param paramName the name of the parameter to be validated
     * @param maybeEmpty whether the parameter value may be empty
     */
    private void validateParamBooleanValue(String paramName, boolean maybeEmpty) {

        String paramValue = m_context.getParameters().get(paramName);
        if (paramValue != null) {
            if (maybeEmpty && paramValue.equals("")) {
                return;
            }
            if (!(paramValue.equals("true") || paramValue.equals("false"))) {
                errors.add(
                    "<" + paramValue + "> is not a boolean. Boolean expected for parameter <" + paramName + ">.");
            }
        }
    }

    /**
     * For two parameters depending on each other, validates whether the
     * dependency is fulfilled for the current request.
     *
     * @param param the first parameter
     * @param other the second parameter which the first parameter depends on
     */
    private void validateParamDepends(String param, String other) {

        String first = m_context.getParameters().get(param);
        String second = m_context.getParameters().get(other);
        if ((first != null) && (second == null)) {
            errors.add("Parameter <" + param + "> depends on parameter <" + other + ">. <" + other + "> is expected.");
        }
    }

    /**
     * Validates an integer request parameter.<p>
     *
     * @param paramName the name of the parameter to be validated
     * @param maybeEmpty whether the parameter value may be empty
     */
    private void validateParamPositiveIntegerValue(String paramName, boolean maybeEmpty) {

        String paramValue = m_context.getParameters().get(paramName);
        if (paramValue != null) {
            if (maybeEmpty && paramValue.equals("")) {
                return;
            }
            String message = "<"
                + paramValue
                + "> is not a positive integer. Positive integer expected for parameter <"
                + paramName
                + ">.";
            try {
                if (Integer.valueOf(paramValue).intValue() < 0) {
                    errors.add(message);
                }
            } catch (NumberFormatException e) {
                errors.add(message);
            }
        }
    }

    /**
     * Validates a sort request parameter.<p>
     *
     * @param paramName the name of the parameter to be validated
     * @param maybeEmpty whether the parameter value may be empty
     */
    private void validateParamSortValue(String paramName, boolean maybeEmpty) {

        String paramValue = m_context.getParameters().get(paramName);
        if (paramValue != null) {
            if (maybeEmpty && paramValue.equals("")) {
                return;
            }
            List<SortOption> list = Arrays.asList(SortOption.values());
            List<String> allowed = new ArrayList<String>();
            for (SortOption sortOption : SortOption.values()) {
                allowed.add(sortOption.toString());
            }
            String message = "<"
                + paramValue
                + "> is not a valid sort option. One of <"
                + String.join(",", allowed)
                + "> is expected.";
            try {
                if (!list.contains(SortOption.valueOf(paramValue))) {
                    errors.add(message);
                }
            } catch (Exception e) {
                errors.add(message);
            }
        }
    }

    /**
     * For a given list of parameter names supported, validates whether the current
     * parameters used in this request are valid.
     *
     * @param supported the parameters supported
     */
    private void validateParamsSupported(String[] supported) {

        List<String> paramList = Arrays.asList(supported);
        for (String paramName : m_context.getParameters().keySet()) {
            if (!paramList.contains(paramName)) {
                errors.add(
                    "Parameter <"
                        + paramName
                        + "> is not supported for this request type. One of <"
                        + String.join(",", supported)
                        + "> is expected.");
            }
        }
    }

    /**
     * Validates this request type.<p>
     */
    private void validateRequest() {

        if (m_handler instanceof CmsJsonHandlerFolder) {
            validateRequestFolder();
        } else if (m_handler instanceof CmsJsonHandlerContainerPage) {
            validateRequestContainerPage();
        } else if (m_handler instanceof CmsJsonHandlerList) {
            validateRequestList();
        } else if (m_handler instanceof CmsJsonHandlerXmlContent) {
            validateRequestXmlContent();
        } else if (m_handler instanceof CmsJsonHandlerResource) {
            validateRequestResource();
        }
    }

    /**
     * Validates the container page request type.<p>
     */
    private void validateRequestContainerPage() {

        String[] supported = {PARAM_CONTENT, PARAM_WRAPPER, PARAM_LOCALE, PARAM_FALLBACK_LOCALE};
        validateParamsSupported(supported);
    }

    /**
     * Validates the folder request type.<p>
     */
    private void validateRequestFolder() {

        String[] supported = {PARAM_LEVELS, PARAM_CONTENT, PARAM_WRAPPER, PARAM_LOCALE, PARAM_FALLBACK_LOCALE};
        validateParamsSupported(supported);
    }

    /**
     * Validates the list request type.<p>
     */
    private void validateRequestList() {

        String[] supported = {
            PARAM_CONTENT,
            PARAM_WRAPPER,
            PARAM_LOCALE,
            PARAM_FALLBACK_LOCALE,
            PARAM_START,
            PARAM_ROWS,
            PARAM_SORT};
        validateParamsSupported(supported);
    }

    /**
     * Validates the resource request type.<p>
     */
    private void validateRequestResource() {

        String[] supported = {};
        validateParamsSupported(supported);
    }

    /**
     * Validates the XML content request type.<p>
     */
    private void validateRequestXmlContent() {

        String[] supported = {PARAM_CONTENT, PARAM_WRAPPER, PARAM_LOCALE, PARAM_FALLBACK_LOCALE, PARAM_PATH};
        validateParamsSupported(supported);
    }
}

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

package org.opencms.jsp.util;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolderSubSitemap;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.json.JSONTokener;
import org.opencms.jsp.CmsJspResourceWrapper;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.util.CmsHtmlConverter;
import org.opencms.util.CmsHtmlExtractor;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;

/**
 * Provides utility methods to be used as functions from a JSP with the EL.<p>
 *
 * @since 7.0.2
 *
 * @see CmsJspContentAccessBean
 */
public final class CmsJspElFunctions {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspElFunctions.class);

    /**
     * Hide the public constructor.<p>
     */
    private CmsJspElFunctions() {

        // NOOP
    }

    /**
     * Extends the given list by adding the provided object.<p>
     *
     * @param list the list to extend
     * @param value the value to add to the list
     */
    public static void addToList(List<Object> list, Object value) {

        list.add(value);
    }

    /**
     * Returns an OpenCms user context created from an Object.<p>
     *
     * <ul>
     * <li>If the input is already a {@link CmsObject}, it is casted and returned unchanged.
     * <li>If the input is a {@link ServletRequest}, the OpenCms user context is read from the request context.
     * <li>If the input is a {@link PageContext}, the OpenCms user context is read from the request of the page context.
     * <li>Otherwise the input is converted to a String which should be a user name, and creation of a OpenCms
     * user context with this name is attempted. Please note that this will only work if the user name is
     * either the "Guest" user or the "Export" user.
     * <li>If no valid OpenCms user context could be created with all of the above, then a new user context for
     * the "Guest" user is created.
     * </ul>
     *
     * @param input the input to create an OpenCms user context from
     *
     * @return an OpenCms user context created from an Object
     */
    public static CmsObject convertCmsObject(Object input) {

        CmsObject result;
        if (input instanceof CmsObject) {
            result = (CmsObject)input;
        } else if (input instanceof ServletRequest) {
            result = CmsFlexController.getCmsObject((ServletRequest)input);
        } else if (input instanceof PageContext) {
            result = CmsFlexController.getCmsObject(((PageContext)input).getRequest());
        } else {
            try {
                // try to use the given name as user name
                result = OpenCms.initCmsObject(String.valueOf(input));
                // try to set the right site root
                ServletRequest req = convertRequest(input);
                if (req instanceof HttpServletRequest) {
                    result.getRequestContext().setSiteRoot(
                        OpenCms.getSiteManager().matchRequest((HttpServletRequest)req).getSiteRoot());
                }
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
                result = null;
            }
        }
        if (result == null) {
            try {
                result = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
                // try to set the right site root
                ServletRequest req = convertRequest(input);
                if (req instanceof HttpServletRequest) {
                    result.getRequestContext().setSiteRoot(
                        OpenCms.getSiteManager().matchRequest((HttpServletRequest)req).getSiteRoot());
                }
            } catch (CmsException e1) {
                // this should never fail since we can always create a "Guest" user
            }
        }
        return result;
    }

    /**
     * Returns a Date created from an Object.<p>
     *
     * <ul>
     * <li>The Object is first checked if it is a {@link Date} already, if so it is casted and returned unchanged.
     * <li>If not, the input is checked if it is a {@link Long}, and if so the Date is created from the Long value.
     * <li>If it's not a Date and not a Long, the Object is transformed to a String and then it's tried
     * to parse a Long out of the String.
     * <li>If this fails, it is tried to parse as a Date using the
     * default date formatting.
     * <li>If this also fails, a new Date is returned that has been initialized with 0.<p>
     * </ul>
     *
     * @param input the Object to create a Date from
     *
     * @return a Date created from the given Object
     */
    public static Date convertDate(Object input) {

        Date result;
        if (input instanceof Date) {
            result = (Date)input;
        } else if (input instanceof Long) {
            result = new Date(((Long)input).longValue());
        } else {
            String str = String.valueOf(input);
            try {
                // treat the input as a String
                long l = Long.parseLong(str);
                result = new Date(l);
            } catch (NumberFormatException e) {
                try {
                    // try to parse String as a Date
                    result = DateFormat.getDateInstance().parse(str);
                } catch (ParseException e1) {
                    result = null;
                }
                if (result == null) {
                    // use default date if parsing fails
                    result = new Date(0);
                }
            }
        }
        return result;
    }

    /**
     * Returns a Double created from an Object.<p>
     *
     * <ul>
     * <li>If the input already is a {@link java.lang.Number}, this number is returned as Double.
     * <li>If the input is of type {@link A_CmsJspValueWrapper}, the wrapper is converted to a Double using {@link A_CmsJspValueWrapper#getToDouble()}.
     * <li>If the input is a String, it is converted to a Double.
     * <li>Otherwise <code>null</code> is returned.
     * </li>
     *
     * @param input the Object to create a Double from
     *
     * @return a Double created from the given Object
     */
    public static Double convertDouble(Object input) {

        Double result = null;
        if (input instanceof Double) {
            result = (Double)input;
        } else if (input instanceof Number) {
            result = Double.valueOf(((Number)input).doubleValue());
        } else if (input instanceof A_CmsJspValueWrapper) {
            result = ((A_CmsJspValueWrapper)input).getToDouble();
        } else {
            if (input != null) {
                String str = String.valueOf(input);
                // support both "10,0" and "10.0" comma style
                str = str.replace(',', '.');
                try {
                    result = Double.valueOf(str);
                } catch (NumberFormatException e) {
                    LOG.info(e.getLocalizedMessage());
                }
            }
        }
        return result;
    }

    /**
     * Returns a list of attribute values specified by the attribute name of the items of the given list.<p>
     *
     * @param input the list of objects to obtain the attribute values from
     * @param attributeName the name of the attribute to obtain
     * @return a list of attributes specified by the attribute name of the items of the given list
     */
    public static List<Object> convertList(List<Object> input, String attributeName) {

        List<Object> result = new ArrayList<Object>(input.size());

        for (Object item : input) {
            try {
                result.add(PropertyUtils.getProperty(item, attributeName));
            } catch (Exception e) {
                // specified attribute is not implemented, return empty list
                return Collections.emptyList();
            }
        }

        return result;
    }

    /**
     * Returns a Locale created from an Object.<p>
     *
     * <ul>
     * <li>The Object is first checked if it is a {@link Locale} already, if so it is casted and returned.
     * <li>If not, the input is transformed to a String and then a Locale lookup with this String is done.
     * <li>If the locale lookup fails, the OpenCms default locale is returned.
     * </ul>
     *
     * @param input the Object to create a Locale from
     *
     * @return a Locale created from the given Object
     */
    public static Locale convertLocale(Object input) {

        Locale locale;
        if (input instanceof Locale) {
            locale = (Locale)input;
        } else {
            locale = CmsLocaleManager.getLocale(String.valueOf(input));
        }
        return locale;
    }

    /**
     * Returns a resource created from an Object.<p>
     *
     * <ul>
     * <li>If the input is already a {@link CmsResource}, it is casted to the resource and returned unchanged.
     * <li>If the input is a String, the given OpenCms context is used to read a resource with this name from the VFS.
     * <li>If the input is a {@link CmsUUID}, the given OpenCms context is used to read a resource with
     * this UUID from the VFS.
     * <li>Otherwise the input is converted to a String, and then the given OpenCms context is used to read
     * a resource with this name from the VFS.
     * </ul>
     *
     * @param cms the current OpenCms user context
     * @param input the input to create a resource from
     *
     * @return a resource created from the given Object
     *
     * @throws CmsException in case of errors accessing the OpenCms VFS for reading the resource
     */
    public static CmsResource convertRawResource(CmsObject cms, Object input) throws CmsException {

        CmsResource result;
        CmsResourceFilter filter = CmsResourceFilter.ignoreExpirationOffline(cms);
        if (input instanceof CmsResource) {
            // input is already a resource
            result = (CmsResource)input;
        } else if (input instanceof String) {
            if (CmsUUID.isValidUUID((String)input)) {
                // input is a UUID as String
                result = cms.readResource(CmsUUID.valueOf((String)input), filter);
            } else {
                // input is a path as String
                result = cms.readResource(cms.getRequestContext().removeSiteRoot((String)input), filter);
            }
        } else if (input instanceof CmsUUID) {
            // input is a UUID
            result = cms.readResource((CmsUUID)input, filter);
        } else {
            // input seems not really to make sense, try to use it like a String
            result = cms.readResource(String.valueOf(input), filter);
        }
        return result;
    }

    /**
     * Tries to convert the given input object into a request.<p>
     *
     * This is only possible if the input object is already a request
     * or if it is a page context.<p>
     *
     * If everything else, this method returns <code>null</code>.<p>
     *
     * @param input the input object to convert to a request
     *
     * @return a request object, or <code>null</code>
     */
    public static ServletRequest convertRequest(Object input) {

        ServletRequest req = null;
        if (input instanceof ServletRequest) {
            req = (ServletRequest)input;
        } else if (input instanceof PageContext) {
            req = ((PageContext)input).getRequest();
        }
        return req;
    }

    /**
     * Returns a resource wrapper created from the input.
     *
     * The wrapped result of {@link #convertRawResource(CmsObject, Object)} is returned.
     *
     * @param cms the current OpenCms user context
     * @param input the input to create a resource from
     *
     * @return a resource wrapper created from the given Object
     *
     * @throws CmsException in case of errors accessing the OpenCms VFS for reading the resource
     */
    public static CmsJspResourceWrapper convertResource(CmsObject cms, Object input) throws CmsException {

        CmsJspResourceWrapper result;
        if (input instanceof CmsResource) {
            result = CmsJspResourceWrapper.wrap(cms, (CmsResource)input);
        } else {
            result = CmsJspResourceWrapper.wrap(cms, convertRawResource(cms, input));
        }
        return result;
    }

    /**
     * Returns a list of resource wrappers created from the input list of resources.
     *
     * @param cms the current OpenCms user context
     * @param list the list to create the resource wrapper list from
     *
     * @return the list of wrapped resources.
     */
    public static List<CmsJspResourceWrapper> convertResourceList(CmsObject cms, List<CmsResource> list) {

        List<CmsJspResourceWrapper> result = new ArrayList<CmsJspResourceWrapper>(list.size());
        for (CmsResource res : list) {
            result.add(CmsJspResourceWrapper.wrap(cms, res));
        }
        return result;
    }

    /**
     * Returns a CmsUUID created from an Object.<p>
     *
     * <ul>
     * <li>The Object is first checked if it is a {@link CmsUUID} already, if so it is casted and returned.
     * <li>If not, the input is transformed to a byte[] and a new {@link CmsUUID} is created with this byte[].
     * <li>Otherwise the input is casted to a String and a new {@link CmsUUID} is created with this String.
     * </ul>
     *
     * @param input the Object to create a CmsUUID from
     *
     * @return a CmsUUID created from the given Object
     */
    public static CmsUUID convertUUID(Object input) {

        CmsUUID uuid;
        if (input instanceof CmsUUID) {
            uuid = (CmsUUID)input;
        } else if (input instanceof byte[]) {
            uuid = new CmsUUID((byte[])input);
        } else {
            uuid = new CmsUUID(String.valueOf(input));
        }
        return uuid;
    }

    /**
     * Returns a newly created, empty List object.<p>
     *
     * There is no way to create an empty list using standard JSTL methods,
     * hence this function.<p>
     *
     * @return a newly created, empty List object
     */
    public static List<Object> createList() {

        return new ArrayList<Object>();
    }

    /**
     * Returns the current OpenCms user context from the given page context.<p>
     *
     * @param input the input to create a CmsObject from
     *
     * @return the current OpenCms user context from the given page context
     */
    public static CmsObject getCmsObject(Object input) {

        return convertCmsObject(input);
    }

    /**
     * Returns the size of the given list.<p>
     *
     * @param input the list of objects to obtain the size from
     * @return the size of the given list
     */
    public static Integer getListSize(Collection<Object> input) {

        if (input != null) {
            return Integer.valueOf(input.size());
        }
        // input was null
        return Integer.valueOf(0);
    }

    /**
     * Returns a parameter value from the module parameters.<p>
     *
     * @param name the name of the module
     * @param key the parameter to return the value for
     * @return the parameter value from the module parameters, or <code>null</code> if the parameter is not set
     */
    public static String getModuleParam(String name, String key) {

        CmsModule module = OpenCms.getModuleManager().getModule(name);
        if (module != null) {
            return module.getParameter(key);
        }
        return null;
    }

    /**
     * Returns the current request URI.<p>
     *
     * For OpenCms 10.5, this is the same as using <code>${cms.requestContext.uri}</code> on a JSP.<p>
     *
     * @param input the request convertible object to get the navigation URI from
     *
     * @return the current navigation URI
     *
     * @deprecated On a JSP use <code>${cms.requestContext.uri}</code> instead.
     */
    @Deprecated
    public static String getNavigationUri(Object input) {

        ServletRequest req = convertRequest(input);
        if (req == null) {
            return null;
        }
        return getCmsObject(input).getRequestContext().getUri();
    }

    /**
     * Returns the link without parameters from a String that is formatted for a GET request.<p>
     *
     * @param url the URL to remove the parameters from
     * @return the link without parameters
     */
    public static String getRequestLink(String url) {

        return CmsRequestUtil.getRequestLink(url);
    }

    /**
     * Returns the value of a parameter from a String that is formatted for a GET request.<p>
     *
     * @param url the URL to get the parameter value from
     * @param paramName the request parameter name
     * @return the value of the parameter
     */
    public static String getRequestParam(String url, String paramName) {

        Map<String, String[]> params = Collections.emptyMap();
        if (CmsStringUtil.isNotEmpty(url)) {
            int pos = url.indexOf(CmsRequestUtil.URL_DELIMITER);
            if (pos >= 0) {
                params = CmsRequestUtil.createParameterMap(url.substring(pos + 1));
            }
        }
        String[] result = params.get(paramName);
        if (result != null) {
            return result[0];
        }
        return null;
    }

    /**
     * Returns a JSP / EL VFS access bean.<p>
     *
     * @param input the Object to create a CmsObject from
     *
     * @return a JSP / EL VFS access bean
     */
    public static CmsJspVfsAccessBean getVfsAccessBean(Object input) {

        return CmsJspVfsAccessBean.create(CmsJspElFunctions.convertCmsObject(input));
    }

    /**
     * Returns whether the given resource is a sub sitemap folder.<p>
     *
     * @param resource the resource to check
     *
     * @return <code>true</code> if the given resource is a sub sitemap folder
     */
    public static boolean isSubSitemap(CmsResource resource) {

        return (resource != null) && CmsResourceTypeFolderSubSitemap.isSubSitemap(resource);
    }

    /**
     * Returns whether the given value is an instance of {@link A_CmsJspValueWrapper}.<p>
     *
     * @param value the value object to check
     *
     * @return <code>true</code> if the given value is an instance of {@link A_CmsJspValueWrapper}
     */
    public static boolean isWrapper(Object value) {

        return (value instanceof A_CmsJspValueWrapper);
    }

    /**
     * Parses the JSON String and returns the requested value.<p>
     *
     * @param maybeJsonString the JSON string
     * @param key the key
     *
     * @return the json value string
     */
    public static String jsonGetString(Object maybeJsonString, Object key) {

        try {
            if (maybeJsonString == null) {
                return null;
            }
            String jsonString = (String)maybeJsonString;
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(jsonString)) {
                return null;
            }
            JSONObject json = new JSONObject(jsonString);
            String keyString = (String)key;
            return json.optString(keyString);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Converts a string (which is assumed to contain a JSON object whose values are strings only) to a map, for use in JSPs.<p>
     *
     * If the input can't be interpreted as JSON, an empty map is returned.
     *
     * @param jsonString the JSON string
     * @return the map with the keys/values from the JSON
     */
    public static Map<String, String> jsonToMap(String jsonString) {

        Map<String, String> result = Maps.newHashMap();
        if (jsonString != null) {
            try {
                JSONObject json = new JSONObject(jsonString);
                for (String key : json.keySet()) {
                    String value = json.optString(key);
                    if (value != null) {
                        result.put(key, value);
                    }
                }
            } catch (Exception e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Looks up the given key from the map that is passed as a String, and returns either the
     * element found or the empty String.<p>
     *
     * The map String must have the form <code>"key1:value1|key2:value2"</code> etc.<p>
     *
     * @param key the key to look up
     * @param map the map represented as a String
     * @return the element found in the map with the given key, or the empty String
     */
    public static String lookup(String key, String map) {

        return lookup(key, map, "");
    }

    /**
     * Looks up the given key from the map that is passed as a String, and returns either the
     * element found or the default value.<p>
     *
     * The map String must have the form <code>"key1:value1|key2:value2"</code> etc.<p>
     *
     * @param key the key to look up
     * @param map the map represented as a String
     * @param defaultValue the default value
     * @return the element found in the map with the given key, or the default value
     */
    public static String lookup(String key, String map, String defaultValue) {

        Map<String, String> values = CmsStringUtil.splitAsMap(map, "|", ":");
        String result = values.get(key);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(result)) {
            return defaultValue;
        }
        return result;
    }

    /**
     * Calculates the next largest integer for the given number parameter.<p>
     *
     * Note that the result is an Object of type {@link java.lang.Long},
     * so in case the parameter can not be converted to a number, <code>null</code> is returned.<p>
     *
     * @param param an Object that will be converted to a number
     *
     * @return the next largest integer for the given number parameter
     */
    public static Long mathCeil(Object param) {

        Long result = null;
        if ((param instanceof Long) || (param instanceof Integer)) {
            result = Long.valueOf(((Number)param).longValue());
        } else {
            Double d = convertDouble(param);
            if (d != null) {
                result = Long.valueOf((long)Math.ceil(d.doubleValue()));
            }
        }
        return result;
    }

    /**
     * Calculates the next smallest integer for the given number parameter.<p>
     *
     * Note that the result is an Object of type {@link java.lang.Long},
     * so in case the parameter can not be converted to a number, <code>null</code> is returned.<p>
     *
     * @param param an Object that will be converted to a number
     *
     * @return the next smallest integer for the given number parameter
     */
    public static Long mathFloor(Object param) {

        Long result = null;
        if ((param instanceof Long) || (param instanceof Integer)) {
            result = Long.valueOf(((Number)param).longValue());
        } else {
            Double d = convertDouble(param);
            if (d != null) {
                result = Long.valueOf((long)Math.floor(d.doubleValue()));
            }
        }
        return result;
    }

    /**
     * Calculates the next integer for the given number parameter by rounding.<p>
     *
     * Note that the result is an Object of type {@link java.lang.Long},
     * so in case the parameter can not be converted to a number, <code>null</code> is returned.<p>
     *
     * @param param an Object that will be converted to a number
     *
     * @return the next integer for the given number parameter calculated by rounding
     */
    public static Long mathRound(Object param) {

        Long result = null;
        if ((param instanceof Long) || (param instanceof Integer)) {
            result = Long.valueOf(((Number)param).longValue());
        } else {
            Double d = convertDouble(param);
            if (d != null) {
                result = Long.valueOf(Math.round(d.doubleValue()));
            }
        }
        return result;
    }

    /**
     * Parses JSON object.
     *
     * @param value
     * @return
     */
    public static Object parseJson(String value) {

        value = value.trim();
        JSONTokener tok = new JSONTokener(value);
        try {
            return tok.nextValue();
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Repairs the given HTML input by adding potentially missing closing tags.<p>
     *
     * @param input the HTML input
     *
     * @return the repaired HTML or an empty string in case of errors
     */
    public static String repairHtml(String input) {

        CmsHtmlConverter converter = new CmsHtmlConverter();
        String result = converter.convertToStringSilent(input);
        return result == null ? "" : result;
    }

    /**
     * Strips all HTML markup from the given input.<p>
     *
     * <ul>
     * <li>In case the input is an instance of {@link CmsJspContentAccessValueWrapper}, an optimized
     * method is used for the HTML stripping.
     * <li>Otherwise the input is converted to a String and this String is stripped.
     * </ul>
     *
     * @param input the input to Strip from HTML
     *
     * @return the given input with all HTML stripped.
     */
    public static String stripHtml(Object input) {

        if (input instanceof CmsJspContentAccessValueWrapper) {
            CmsJspContentAccessValueWrapper wrapper = (CmsJspContentAccessValueWrapper)input;
            if (wrapper.getExists()) {
                return wrapper.getContentValue().getPlainText(wrapper.getCmsObject());
            } else {
                return "";
            }
        } else {
            try {
                return CmsHtmlExtractor.extractText(
                    String.valueOf(input),
                    OpenCms.getSystemInfo().getDefaultEncoding());
            } catch (org.htmlparser.util.ParserException e) {
                LOG.error(e.getLocalizedMessage(), e);
                return "";
            } catch (UnsupportedEncodingException e) {
                LOG.error(e.getLocalizedMessage(), e);
                return "";
            }
        }
    }

    /**
     * Converts the given Object to a (Double) number, falling back to a default if this is not possible.<p>
     *
     * In case even the default can not be converted to a number, <code>-1.0</code> is returned.<p>
     *
     * @param input the Object to convert to a (Double) number
     * @param def the fall back default value in case the conversion is not possible
     *
     * @return the given Object converted to a (Double) number
     */
    public static Double toNumber(Object input, Object def) {

        Double result = convertDouble(input);
        if (result == null) {
            result = convertDouble(def);
        }
        if (result == null) {
            result = Double.valueOf(-1.0);
        }
        return result;
    }

    /**
     * Returns a substring of the source, which is at most length characters long.<p>
     *
     * If a char is cut, <code>" ..."</code> is appended to the result.<p>
     *
     * @param input the string to trim
     * @param length the maximum length of the string to be returned
     *
     * @return a substring of the source, which is at most length characters long
     *
     * @see CmsStringUtil#trimToSize(String, int, String)
     */
    public static String trimToSize(String input, int length) {

        return CmsStringUtil.trimToSize(input, length, " ...");
    }

    /**
     * Validates a value against a regular expression.<p>
     *
     * @param value the value
     * @param regex the regex
     *
     * @return <code>true</code> if the value satisfies the validation
     */
    public static boolean validateRegex(String value, String regex) {

        return CmsStringUtil.validateRegex(value, regex, true);
    }
}

/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsHtml2TextConverter;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import org.apache.commons.beanutils.PropertyUtils;

/**
 * Provides utility methods to be used as functions from a JSP with the EL.<p>
 * 
 * @since 7.0.2
 * 
 * @see CmsJspContentAccessBean
 */
public final class CmsJspElFunctions {

    /**
     * Hide the public constructor.<p>
     */
    private CmsJspElFunctions() {

        // NOOP
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
    public static CmsResource convertResource(CmsObject cms, Object input) throws CmsException {

        CmsResource result;
        if (input instanceof String) {
            // input is a String
            result = cms.readResource((String)input);
        } else if (input instanceof CmsResource) {
            // input is already a resource
            result = (CmsResource)input;
        } else if (input instanceof CmsUUID) {
            // input is a UUID
            result = cms.readResource((CmsUUID)input);
        } else {
            // input seems not really to make sense, try to use it like a String
            result = cms.readResource(String.valueOf(input));
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
     * Encodes a String in a way that is compatible with the JavaScript escape function.
     * 
     * @param source The text to be encoded
     * @param encoding the encoding type
     * 
     * @return The JavaScript escaped string
     */
    public static String escape(String source, String encoding) {

        return CmsEncoder.escape(source, encoding);
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
    public static Integer getListSize(List<Object> input) {

        if (input != null) {
            return Integer.valueOf(input.size());
        }
        // input was null
        return Integer.valueOf(0);
    }

    /**
     * Returns the current navigation URI.<p> 
     * 
     * Which can be the request URI or the VFS resource URI.<p>
     * 
     * In case a sitemap is used, the navigation URI will be the
     * request URI, if not the VFS resource URI is returned.<p>
     * 
     * @param input the request convertible object to get the navigation URI from
     * 
     * @return the current navigation URI
     */
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
                return wrapper.obtainContentValue().getPlainText(wrapper.obtainCmsObject());
            } else {
                return "";
            }
        }
        try {
            return CmsHtml2TextConverter.html2text(String.valueOf(input), OpenCms.getSystemInfo().getDefaultEncoding());
        } catch (Exception e) {
            return CmsMessages.formatUnknownKey(e.getMessage());
        }
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
     * Decodes a String in a way that is compatible with the JavaScript 
     * unescape function.<p>
     * 
     * @param source The String to be decoded
     * @param encoding the encoding type
     * 
     * @return The JavaScript unescaped String
     */
    public static String unescape(String source, String encoding) {

        return CmsEncoder.unescape(source, encoding);
    }
}
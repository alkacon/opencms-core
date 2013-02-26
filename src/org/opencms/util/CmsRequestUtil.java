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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.util;

import org.opencms.flex.CmsFlexRequest;
import org.opencms.i18n.CmsEncoder;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;

/**
 * Provides utility functions for dealing with values a <code>{@link HttpServletRequest}</code>.<p>
 * 
 * @since 6.0.0 
 */
public final class CmsRequestUtil {

    /** Request attribute that contains the original error code. */
    public static final String ATTRIBUTE_ERRORCODE = "org.opencms.util.CmsErrorCode";

    /** HTTP Accept Header for the cms:device-tag. */
    public static final String HEADER_ACCEPT = "Accept";

    /** HTTP Accept-Charset Header for internal requests used during static export. */
    public static final String HEADER_ACCEPT_CHARSET = "Accept-Charset";

    /** HTTP Accept-Language Header for internal requests used during static export. */
    public static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";

    /** HTTP Header "Cache-Control". */
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";

    /** HTTP Header "Connection". */
    public static final String HEADER_CONNECTION = "Connection";

    /** The "Content-Disposition" http header. */
    public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

    /** The "Content-Type" http header. */
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    /** HTTP Header "Expires". */
    public static final String HEADER_EXPIRES = "Expires";

    /** HTTP Header "If-Modified-Since". */
    public static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";

    /** The Header that stores the session id (used by OpenCms upload applet). */
    public static final String HEADER_JSESSIONID = "JSESSIONID";

    /** HTTP Header "Last-Modified". */
    public static final String HEADER_LAST_MODIFIED = "Last-Modified";

    /** HTTP Header "Location". */
    public static final String HEADER_LOCATION = "Location";

    /** HTTP Header for internal requests used during static export. */
    public static final String HEADER_OPENCMS_EXPORT = "OpenCms-Export";

    /** HTTP Header "Pragma". */
    public static final String HEADER_PRAGMA = "Pragma";

    /** HTTP Header "Server". */
    public static final String HEADER_SERVER = "Server";

    /** HTTP Header "user-agent". */
    public static final String HEADER_USER_AGENT = "user-agent";

    /** HTTP Header value "max-age=" (for "Cache-Control"). */
    public static final String HEADER_VALUE_MAX_AGE = "max-age=";

    /** HTTP Header value "must-revalidate" (for "Cache-Control"). */
    public static final String HEADER_VALUE_MUST_REVALIDATE = "must-revalidate";

    /** HTTP Header value "no-cache" (for "Cache-Control"). */
    public static final String HEADER_VALUE_NO_CACHE = "no-cache";

    /** HTTP Header "WWW-Authenticate". */
    public static final String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";

    /** Identifier for x-forwarded-for (i.e. proxied) request headers. */
    public static final String HEADER_X_FORWARDED_FOR = "x-forwarded-for";

    /** Assignment char between parameter name and values. */
    public static final String PARAMETER_ASSIGNMENT = "=";

    /** Delimiter char between parameters. */
    public static final String PARAMETER_DELIMITER = "&";

    /** Delimiter char between url and query. */
    public static final String URL_DELIMITER = "?";

    /** The prefix for &amp. */
    private static final String AMP = "amp;";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRequestUtil.class);

    /** 
     * Default constructor (empty), private because this class has only 
     * static methods.<p>
     */
    private CmsRequestUtil() {

        // empty
    }

    /**
     * Appends a request parameter to the given URL.<p>
     * 
     * This method takes care about the adding the parameter as an additional 
     * parameter (appending <code>&param=value</code>) or as the first parameter
     * (appending <code>?param=value</code>).<p>
     * 
     * @param url the URL where to append the parameter to
     * @param paramName the paramter name to append
     * @param paramValue the parameter value to append
     * 
     * @return the URL with the given parameter appended
     */
    public static String appendParameter(String url, String paramName, String paramValue) {

        if (CmsStringUtil.isEmpty(url)) {
            return null;
        }
        int pos = url.indexOf(URL_DELIMITER);
        StringBuffer result = new StringBuffer(256);
        result.append(url);
        if (pos >= 0) {
            // url already has parameters
            result.append(PARAMETER_DELIMITER);
        } else {
            // url does not have parameters
            result.append(URL_DELIMITER);
        }
        result.append(paramName);
        result.append(PARAMETER_ASSIGNMENT);
        result.append(paramValue);
        return result.toString();
    }

    /**
     * Appends a map of request parameters to the given URL.<p>
     * 
     * The map can contains values of <code>String[]</code> or 
     * simple <code>String</code> values.<p>
     * 
     * This method takes care about the adding the parameter as an additional 
     * parameter (appending <code>&param=value</code>) or as the first parameter
     * (appending <code>?param=value</code>).<p>
     * 
     * @param url the URL where to append the parameter to
     * @param params the parameters to append
     * @param encode if <code>true</code>, the parameter values are encoded before they are appended
     * 
     * @return the URL with the given parameter appended
     */
    public static String appendParameters(String url, Map<String, String[]> params, boolean encode) {

        if (CmsStringUtil.isEmpty(url)) {
            return null;
        }
        if ((params == null) || params.isEmpty()) {
            return url;
        }
        int pos = url.indexOf(URL_DELIMITER);
        StringBuffer result = new StringBuffer(256);
        result.append(url);
        if (pos >= 0) {
            // url already has parameters
            result.append(PARAMETER_DELIMITER);
        } else {
            // url does not have parameters
            result.append(URL_DELIMITER);
        }
        // ensure all values are of type String[]
        Iterator<Map.Entry<String, String[]>> i = params.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, String[]> entry = i.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            // generics where added later, so make sure that the value really is a String[]
            String[] values = value instanceof String[] ? (String[])value : new String[] {value.toString()};
            for (int j = 0; j < values.length; j++) {
                String strValue = values[j];
                if (encode) {
                    strValue = CmsEncoder.encode(strValue);
                }
                result.append(key);
                result.append(PARAMETER_ASSIGNMENT);
                result.append(strValue);
                if ((j + 1) < values.length) {
                    result.append(PARAMETER_DELIMITER);
                }
            }
            if (i.hasNext()) {
                result.append(PARAMETER_DELIMITER);
            }
        }
        return result.toString();
    }

    /**
     * Creates a valid request parameter map from the given map,
     * most notably changing the values form <code>String</code>
     * to <code>String[]</code> if required.<p>
     * 
     * If the given parameter map is <code>null</code>, then <code>null</code> is returned.<p>
     * 
     * @param params the map of parameters to create a parameter map from
     * @return the created parameter map, all values will be instances of <code>String[]</code>
     */
    public static Map<String, String[]> createParameterMap(Map<String, ?> params) {

        if (params == null) {
            return null;
        }
        Map<String, String[]> result = new HashMap<String, String[]>();
        Iterator<?> i = params.entrySet().iterator();
        while (i.hasNext()) {
            @SuppressWarnings("unchecked")
            Map.Entry<String, ?> entry = (Entry<String, ?>)i.next();
            String key = entry.getKey();
            Object values = entry.getValue();
            if (values instanceof String[]) {
                result.put(key, (String[])values);
            } else {
                if (values != null) {
                    result.put(key, new String[] {values.toString()});
                }
            }
        }
        return result;
    }

    /**
     * Parses the parameters of the given request query part and creates a parameter map out of them.<p>
     * 
     * Please note: This does not parse a full request URI/URL, only the query part that 
     * starts after the "?". For example, in the URI <code>/system/index.html?a=b&amp;c=d</code>,
     * the query part is <code>a=b&amp;c=d</code>.<p>
     * 
     * If the given String is empty, an empty map is returned.<p>
     * 
     * @param query the query to parse
     * @return the parameter map created from the query
     */
    public static Map<String, String[]> createParameterMap(String query) {

        if (CmsStringUtil.isEmpty(query)) {
            // empty query
            return new HashMap<String, String[]>();
        }
        if (query.charAt(0) == URL_DELIMITER.charAt(0)) {
            // remove leading '?' if required
            query = query.substring(1);
        }
        // cut along the different parameters
        String[] params = CmsStringUtil.splitAsArray(query, PARAMETER_DELIMITER);
        Map<String, String[]> parameters = new HashMap<String, String[]>(params.length);
        for (int i = 0; i < params.length; i++) {
            String key = null;
            String value = null;
            // get key and value, separated by a '=' 
            int pos = params[i].indexOf(PARAMETER_ASSIGNMENT);
            if (pos > 0) {
                key = params[i].substring(0, pos);
                value = params[i].substring(pos + 1);
            } else if (pos < 0) {
                key = params[i];
                value = "";
            }
            // adjust the key if it starts with "amp;"
            // this happens when "&amp;" is used instead of a simple "&"
            if ((key != null) && (key.startsWith(AMP))) {
                key = key.substring(AMP.length());
            }
            // now make sure the values are of type String[]
            if (key != null) {
                String[] values = parameters.get(key);
                if (values == null) {
                    // this is the first value, create new array
                    values = new String[] {value};
                } else {
                    // append to the existing value array
                    String[] copy = new String[values.length + 1];
                    System.arraycopy(values, 0, copy, 0, values.length);
                    copy[copy.length - 1] = value;
                    values = copy;
                }
                parameters.put(key, values);
            }
        }
        return parameters;
    }

    /**
     * Returns all parameters of the given request
     * as a request parameter URL String, that is in the form <code>key1=value1&key2=value2</code> etc.
     *  
     * The result will be encoded using the <code>{@link CmsEncoder#encode(String)}</code> function.<p>
     * 
     * @param req the request to read the parameters from
     * 
     * @return all initialized parameters of the given request as request parameter URL String
     */
    public static String encodeParams(HttpServletRequest req) {

        StringBuffer result = new StringBuffer(512);
        Map<String, String[]> params = CmsCollectionsGenericWrapper.map(req.getParameterMap());
        Iterator<Map.Entry<String, String[]>> i = params.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, String[]> entry = i.next();
            String param = entry.getKey();
            String[] values = entry.getValue();
            for (int j = 0; j < values.length; j++) {
                result.append(param);
                result.append("=");
                result.append(CmsEncoder.encode(values[j]));
                if ((j + 1) < values.length) {
                    result.append("&");
                }
            }
            if (i.hasNext()) {
                result.append("&");
            }
        }
        return CmsEncoder.encode(result.toString());
    }

    /**
     * Encodes the given URI, with all parameters from the given request appended.<p>
     * 
     * The result will be encoded using the <code>{@link CmsEncoder#encode(String)}</code> function.<p>
     * 
     * @param req the request where to read the parameters from
     * @param uri the URI to encode
     * @return the encoded URI, with all parameters from the given request appended
     */
    public static String encodeParamsWithUri(String uri, HttpServletRequest req) {

        String result;
        String params = encodeParams(req);
        if (CmsStringUtil.isNotEmpty(params)) {
            result = CmsEncoder.encode(uri + "?") + params;
        } else {
            result = CmsEncoder.encode(uri);
        }
        return result;
    }

    /**
     * Forwards the response to the given target, which may contain parameters appended like for example <code>?a=b&amp;c=d</code>.<p>
     * 
     * Please note: If possible, use <code>{@link #forwardRequest(String, Map, HttpServletRequest, HttpServletResponse)}</code>
     * where the parameters are passed as a map, since the parsing of the parameters may introduce issues with encoding
     * and is in general much less effective.<p>
     * 
     * The parsing of parameters will likely fail for "large values" (e.g. full blown web forms with &lt;textarea&gt;
     * elements etc. Use this method only if you know that the target will just contain up to 3 parameters which 
     * are relatively short and have no encoding or line break issues.<p>
     * 
     * @param target the target to forward to (may contain parameters like <code>?a=b&amp;c=d</code>)
     * @param req the request to forward
     * @param res the response to forward
     * 
     * @throws IOException in case the forwarding fails
     * @throws ServletException in case the forwarding fails
     */
    public static void forwardRequest(String target, HttpServletRequest req, HttpServletResponse res)
    throws IOException, ServletException {

        // clear the current parameters
        CmsUriSplitter uri = new CmsUriSplitter(target);
        Map<String, String[]> params = createParameterMap(uri.getQuery());
        forwardRequest(uri.getPrefix(), params, req, res);
    }

    /**
     * Forwards the response to the given target, with the provided parameter map.<p>
     * 
     * The target URI must NOT have parameters appended like for example <code>?a=b&amp;c=d</code>.
     * The values in the provided map must be of type <code>String[]</code>. If required, use
     * <code>{@link #createParameterMap(Map)}</code> before calling this method to make sure
     * all values are actually of the required array type.<p>
     * 
     * @param target the target to forward to (may NOT contain parameters like <code>?a=b&amp;c=d</code>)
     * @param params the parameter map (the values must be of type <code>String[]</code>
     * @param req the request to forward
     * @param res the response to forward
     * 
     * @throws IOException in case the forwarding fails
     * @throws ServletException in case the forwarding fails
     */
    public static void forwardRequest(
        String target,
        Map<String, String[]> params,
        HttpServletRequest req,
        HttpServletResponse res) throws IOException, ServletException {

        // cast the request back to a flex request so the parameter map can be accessed
        CmsFlexRequest f_req = (CmsFlexRequest)req;
        // set the parameters
        f_req.setParameterMap(params);
        // check for links "into" OpenCms, these may need the webapp name to be removed
        String vfsPrefix = OpenCms.getStaticExportManager().getVfsPrefix();
        if (target.startsWith(vfsPrefix)) {
            // remove VFS prefix (will also work for empty vfs prefix in ROOT webapp case with proxy rules)
            target = target.substring(vfsPrefix.length());
            // append the servlet name
            target = OpenCms.getSystemInfo().getServletPath() + target;
        }
        // forward the request
        f_req.getRequestDispatcher(target).forward(f_req, res);
    }

    /**
     * Returns a map with all request attributes.<p>
     * 
     * @param req the request
     * 
     * @return the attribute map
     */
    public static Map<String, Object> getAtrributeMap(ServletRequest req) {

        if (req instanceof CmsFlexRequest) {
            return ((CmsFlexRequest)req).getAttributeMap();
        }
        Map<String, Object> attrs = new HashMap<String, Object>();
        Enumeration<String> atrrEnum = CmsCollectionsGenericWrapper.enumeration(req.getAttributeNames());
        while (atrrEnum.hasMoreElements()) {
            String key = atrrEnum.nextElement();
            Object value = req.getAttribute(key);
            attrs.put(key, value);
        }
        return attrs;
    }

    /**
     * Returns the value of the cookie with the given name.<p/>
     * 
     * @param jsp the CmsJspActionElement to use
     * @param name the name of the cookie
     * 
     * @return the value of the cookie with the given name or null, if no cookie exists with the name
     */
    public static String getCookieValue(CmsJspActionElement jsp, String name) {

        Cookie[] cookies = jsp.getRequest().getCookies();
        for (int i = 0; (cookies != null) && (i < cookies.length); i++) {
            if (name.equalsIgnoreCase(cookies[i].getName())) {
                return cookies[i].getValue();
            }
        }
        return null;
    }

    /**
     * Converts the given parameter map into an JSON object.<p>
     * 
     * @param params the parameters map to convert
     * 
     * @return the JSON representation of the given parameter map
     */
    public static JSONObject getJsonParameterMap(Map<String, String[]> params) {

        JSONObject result = new JSONObject();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String paramKey = entry.getKey();
            JSONArray paramValue = new JSONArray();
            for (int i = 0, l = entry.getValue().length; i < l; i++) {
                paramValue.put(entry.getValue()[i]);
            }
            try {
                result.putOpt(paramKey, paramValue);
            } catch (JSONException e) {
                // should never happen
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Reads value from the request parameters,
     * will return <code>null</code> if the value is not available or only white space.<p>
     * 
     * The value of the request will also be decoded using <code>{@link CmsEncoder#decode(String)}</code>
     * and also trimmed using <code>{@link String#trim()}</code>.<p>
     * 
     * @param request the request to read the parameter from
     * @param paramName the parameter name to read
     * 
     * @return the request parameter value for the given parameter
     */
    public static String getNotEmptyDecodedParameter(HttpServletRequest request, String paramName) {

        String result = getNotEmptyParameter(request, paramName);
        if (result != null) {
            result = CmsEncoder.decode(result.trim());
        }
        return result;
    }

    /**
     * Reads value from the request parameters,
     * will return <code>null</code> if the value is not available or only white space.<p>
     * 
     * @param request the request to read the parameter from
     * @param paramName the parameter name to read
     * 
     * @return the request parameter value for the given parameter
     */
    public static String getNotEmptyParameter(HttpServletRequest request, String paramName) {

        String result = request.getParameter(paramName);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(result)) {
            result = null;
        }
        return result;
    }

    /**
     * Converts the given JSON object into a valid parameter map.<p>
     * 
     * @param params the JSON object to convert
     * 
     * @return the parameter map from the given JSON object
     */
    public static Map<String, String[]> getParameterMapFromJSON(JSONObject params) {

        Map<String, String[]> result = new HashMap<String, String[]>();
        Iterator<String> itKeys = params.keys();
        while (itKeys.hasNext()) {
            String key = itKeys.next();
            JSONArray paramValue = params.optJSONArray(key);
            result.put(key, new String[paramValue.length()]);
            for (int i = 0, l = paramValue.length(); i < l; i++) {
                result.get(key)[i] = paramValue.optString(i);
            }
        }
        return result;
    }

    /**
     * Returns the link without parameters from a String that is formatted for a GET request.<p>
     * 
     * @param url the URL to remove the parameters from
     * @return the URL without any parameters
     */
    public static String getRequestLink(String url) {

        if (CmsStringUtil.isEmpty(url)) {
            return null;
        }
        int pos = url.indexOf(URL_DELIMITER);
        if (pos >= 0) {
            return url.substring(0, pos);
        }
        return url;

    }

    /**
     * Reads an object from the session of the given HTTP request.<p>
     * 
     * A session will be initialized if the request does not currently have a session.
     * As a result, the request will always have a session after this method has been called.<p> 
     * 
     * Will return <code>null</code> if no corresponding object is found in the session.<p>
     * 
     * @param request the request to get the session from
     * @param key the key of the object to read from the session
     * @return the object received form the session, or <code>null</code>
     */
    public static Object getSessionValue(HttpServletRequest request, String key) {

        HttpSession session = request.getSession(true);
        return session.getAttribute(key);
    }

    /**
     * Parses a request of the form <code>multipart/form-data</code>.
     * 
     * The result list will contain items of type <code>{@link FileItem}</code>.
     * If the request is not of type <code>multipart/form-data</code>, then <code>null</code> is returned.<p>
     * 
     * @param request the HTTP servlet request to parse
     * 
     * @return the list of <code>{@link FileItem}</code> extracted from the multipart request,
     *      or <code>null</code> if the request was not of type <code>multipart/form-data</code>
     */
    public static List<FileItem> readMultipartFileItems(HttpServletRequest request) {

        if (!ServletFileUpload.isMultipartContent(request)) {
            return null;
        }
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // maximum size that will be stored in memory
        factory.setSizeThreshold(4096);
        // the location for saving data that is larger than getSizeThreshold()
        factory.setRepository(new File(OpenCms.getSystemInfo().getPackagesRfsPath()));
        ServletFileUpload fu = new ServletFileUpload(factory);
        // set encoding to correctly handle special chars (e.g. in filenames)
        fu.setHeaderEncoding(request.getCharacterEncoding());
        List<FileItem> result = new ArrayList<FileItem>();
        try {
            List<FileItem> items = CmsCollectionsGenericWrapper.list(fu.parseRequest(request));
            if (items != null) {
                result = items;
            }
        } catch (FileUploadException e) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_PARSE_MULIPART_REQ_FAILED_0), e);
        }
        return result;
    }

    /**
     * Creates a "standard" request parameter map from the values of a 
     * <code>multipart/form-data</code> request.<p>
     * 
     * @param encoding the encoding to use when creating the values
     * @param multiPartFileItems the list of parsed multi part file items
     * 
     * @return a map containing all non-file request parameters
     * 
     * @see #readMultipartFileItems(HttpServletRequest)
     */
    public static Map<String, String[]> readParameterMapFromMultiPart(String encoding, List<FileItem> multiPartFileItems) {

        Map<String, String[]> parameterMap = new HashMap<String, String[]>();
        Iterator<FileItem> i = multiPartFileItems.iterator();
        while (i.hasNext()) {
            FileItem item = i.next();
            String name = item.getFieldName();
            String value = null;
            if ((name != null) && (item.getName() == null)) {
                // only put to map if current item is no file and not null
                try {
                    value = item.getString(encoding);
                } catch (UnsupportedEncodingException e) {
                    LOG.error(Messages.get().getBundle().key(Messages.LOG_ENC_MULTIPART_REQ_ERROR_0), e);
                    value = item.getString();
                }
                if (parameterMap.containsKey(name)) {

                    // append value to parameter values array
                    String[] oldValues = parameterMap.get(name);
                    String[] newValues = new String[oldValues.length + 1];
                    System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
                    newValues[oldValues.length] = value;
                    parameterMap.put(name, newValues);

                } else {
                    parameterMap.put(name, new String[] {value});
                }
            }
        }
        return parameterMap;
    }

    /**
     * Redirects the response to the target link using a "301 - Moved Permanently" header.<p>
     * 
     * This implementation will work only on JSP pages in OpenCms that use the default JSP loader implementation.<p>
     * 
     * @param jsp the OpenCms JSP context
     * @param target the target link
     */
    public static void redirectPermanently(CmsJspActionElement jsp, String target) {

        String newTarget = OpenCms.getLinkManager().substituteLink(jsp.getCmsObject(), target, null, true);
        jsp.getRequest().setAttribute(
            CmsRequestUtil.ATTRIBUTE_ERRORCODE,
            new Integer(HttpServletResponse.SC_MOVED_PERMANENTLY));
        jsp.getResponse().setHeader(HEADER_CONNECTION, "close");
        try {
            jsp.getResponse().sendRedirect(newTarget);
        } catch (IOException e) {
            LOG.error(Messages.get().getBundle().key(Messages.ERR_IOERROR_0), e);
            // In case of an IOException, we send the redirect ourselves
            jsp.getResponse().setHeader(HEADER_LOCATION, newTarget);
        }
    }

    /**
     * Redirects the response to the target link.<p>
     * 
     * Use this method instead of {@link javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)}
     * to avoid relative links with secure sites (and issues with apache).<p>
     * 
     * @param jsp the OpenCms JSP context
     * @param target the target link
     * 
     * @throws IOException if something goes wrong during redirection
     */
    public static void redirectRequestSecure(CmsJspActionElement jsp, String target) throws IOException {

        jsp.getResponse().sendRedirect(OpenCms.getLinkManager().substituteLink(jsp.getCmsObject(), target, null, true));
    }

    /** 
     * Removes an object from the session of the given http request.<p>
     * 
     * A session will be initialized if the request does not currently have a session.
     * As a result, the request will always have a session after this method has been called.<p> 
     * 
     * @param request the request to get the session from
     * @param key the key of the object to be removed from the session
     */
    public static void removeSessionValue(HttpServletRequest request, String key) {

        HttpSession session = request.getSession(true);
        session.removeAttribute(key);
    }

    /** 
     * Sets the value of a specific cookie.<p>
     * If no cookie exists with the value, a new cookie will be created.
     * 
     * @param jsp the CmsJspActionElement to use
     * @param name the name of the cookie
     * @param value the value of the cookie
     */
    public static void setCookieValue(CmsJspActionElement jsp, String name, String value) {

        Cookie[] cookies = jsp.getRequest().getCookies();
        for (int i = 0; (cookies != null) && (i < cookies.length); i++) {
            if (name.equalsIgnoreCase(cookies[i].getName())) {
                cookies[i].setValue(value);
                return;
            }
        }
        Cookie cookie = new Cookie(name, value);
        jsp.getResponse().addCookie(cookie);
    }

    /**
     * Sets headers to the given response to prevent client side caching.<p> 
     * 
     * The following headers are set:<p>
     * <code>
     * Cache-Control: max-age=0<br>
     * Cache-Control: must-revalidate<br>
     * Pragma: no-cache
     * </code>
     * 
     * @param res the request where to set the no-cache headers
     */
    public static void setNoCacheHeaders(HttpServletResponse res) {

        res.setHeader(CmsRequestUtil.HEADER_CACHE_CONTROL, CmsRequestUtil.HEADER_VALUE_MAX_AGE + "0");
        res.addHeader(CmsRequestUtil.HEADER_CACHE_CONTROL, CmsRequestUtil.HEADER_VALUE_MUST_REVALIDATE);
        res.setHeader(CmsRequestUtil.HEADER_PRAGMA, CmsRequestUtil.HEADER_VALUE_NO_CACHE);
    }

    /**
     * Adds an object to the session of the given HTTP request.<p>
     * 
     * A session will be initialized if the request does not currently have a session.
     * As a result, the request will always have a session after this method has been called.<p> 
     * 
     * @param request the request to get the session from
     * @param key the key of the object to be stored in the session
     * @param value the object to be stored in the session
     */
    public static void setSessionValue(HttpServletRequest request, String key, Object value) {

        HttpSession session = request.getSession(true);
        session.setAttribute(key, value);
    }
}
/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/CmsRequestUtil.java,v $
 * Date   : $Date: 2005/09/16 09:03:03 $
 * Version: $Revision: 1.15.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.logging.Log;

/**
 * Provides utility functions for dealing with values a <code>{@link HttpServletRequest}</code>.<p>
 * 
 * @author  Alexander Kandzior 
 *
 * @version $Revision: 1.15.2.1 $ 
 * 
 * @since 6.0.0 
 */
public final class CmsRequestUtil {

    /** Request attribute that contains the original error code. */
    public static final String ATTRIBUTE_ERRORCODE = "org.opencms.util.CmsErrorCode";

    /** HTTP Accept-Charset Header for internal requests used during static export. */
    public static final String HEADER_ACCEPT_CHARSET = "Accept-Charset";

    /** HTTP Accept-Language Header for internal requests used during static export. */
    public static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";

    /** HTTP Header "Cache-Control". */
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";

    /** The "Content-Disposition" http header. */
    public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

    /** The "Content-Type" http header. */
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    /** HTTP Header "Expires". */
    public static final String HEADER_EXPIRES = "Expires";

    /** HTTP Header "If-Modified-Since". */
    public static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";

    /** HTTP Header "Last-Modified". */
    public static final String HEADER_LAST_MODIFIED = "Last-Modified";

    /** HTTP Header for internal requests used during static export. */
    public static final String HEADER_OPENCMS_EXPORT = "OpenCms-Export";

    /** HTTP Header "Pragma". */
    public static final String HEADER_PRAGMA = "Pragma";

    /** HTTP Header "Server". */
    public static final String HEADER_SERVER = "Server";

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
        int pos = url.indexOf('?');
        StringBuffer result = new StringBuffer(256);
        result.append(url);
        if (pos >= 0) {
            // url already has parameters
            result.append('&');
        } else {
            // url does not have parameters
            result.append('?');
        }
        result.append(paramName);
        result.append('=');
        result.append(paramValue);
        return result.toString();
    }

    /**
     * Appends a map of request parameters to the given URL.<p>
     * 
     * The map can cointains values of <code>String[]</code> or 
     * simple <code>String</code> values.<p>
     * 
     * This method takes care about the adding the parameter as an additional 
     * parameter (appending <code>&param=value</code>) or as the first parameter
     * (appending <code>?param=value</code>).<p>
     * 
     * @param url the URL where to append the parameter to
     * @param params the paramters to append
     * @param encode if <code>true</code>, the parameter values are encoded before they are appended
     * 
     * @return the URL with the given parameter appended
     */
    public static String appendParameters(String url, Map params, boolean encode) {

        if (CmsStringUtil.isEmpty(url)) {
            return null;
        }
        if ((params == null) || params.isEmpty()) {
            return url;
        }
        int pos = url.indexOf('?');
        StringBuffer result = new StringBuffer(256);
        result.append(url);
        if (pos >= 0) {
            // url already has parameters
            result.append('&');
        } else {
            // url does not have parameters
            result.append('?');
        }
        // ensure all values are of type String[]
        Map newParams = createParameterMap(params);
        Iterator i = newParams.keySet().iterator();
        while (i.hasNext()) {
            String key = (String)i.next();
            Object value = newParams.get(key);
            String[] values = (String[])value;
            for (int j = 0; j < values.length; j++) {
                String strValue = values[j];
                if (encode) {
                    strValue = CmsEncoder.encode(strValue);
                }
                result.append(key);
                result.append('=');
                result.append(strValue);
                if ((j + 1) < values.length) {
                    result.append('&');
                }
            }
            if (i.hasNext()) {
                result.append('&');
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
    public static Map createParameterMap(Map params) {

        if (params == null) {
            return null;
        }
        HashMap result = new HashMap();
        Iterator i = params.keySet().iterator();
        while (i.hasNext()) {
            String key = i.next().toString();
            Object values = params.get(key);
            if (values instanceof String[]) {
                result.put(key, values);
            } else {
                result.put(key, new String[] {values.toString()});
            }
        }
        return result;
    }

    /**
     * Parses the parameters of the given request query part and creaes a parameter map out of them.<p>
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
    public static Map createParameterMap(String query) {

        if (CmsStringUtil.isEmpty(query)) {
            return new HashMap();
        }
        HashMap parameters = new HashMap();
        String[] params = CmsStringUtil.splitAsArray(query, '&');
        for (int i = 0; i < params.length; i++) {
            String key = null;
            String value = null;
            int pos = params[i].indexOf('=');
            if (pos > 0) {
                key = params[i].substring(0, pos);
                value = params[i].substring(pos + 1);
            } else if (pos < 0) {
                key = params[i];
                value = "";
            }
            if (key != null) {
                String[] values = (String[])parameters.get(key);
                if (values == null) {
                    values = new String[] {value};
                } else {
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
        Map params = req.getParameterMap();
        Iterator i = params.keySet().iterator();
        while (i.hasNext()) {
            String param = (String)i.next();
            String[] values = (String[])params.get(param);
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
     * Encodes the given uri, with all parameters from the given request appended.<p>
     * 
     * The result will be encoded using the <code>{@link CmsEncoder#encode(String)}</code> function.<p>
     * 
     * @param req the request where to read the parameters from
     * @param uri the uri to encode
     * @return the encoded uri, with all parameters from the given request appended
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
     * The parsing of parameters will likley fail for "large values" (e.g. full blown web forms with &lt;textarea&gt;
     * elements etc. Use this method only if you know that the target will just contain up to 3 parameters which 
     * are relativly short and have no encoding or linebreak issues.<p>
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
        String[] uri = splitUri(target);
        Map params = createParameterMap(uri[2]);
        forwardRequest(uri[0], params, req, res);
    }

    /**
     * Forwards the response to the given target, with the provided parameter map.<p>
     * 
     * The target uri must NOT have parameters appended like for example <code>?a=b&amp;c=d</code>.
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
    public static void forwardRequest(String target, Map params, HttpServletRequest req, HttpServletResponse res)
    throws IOException, ServletException {

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
    public static List readMultipartFileItems(HttpServletRequest request) {

        if (!FileUploadBase.isMultipartContent(request)) {
            return null;
        }
        DiskFileUpload fu = new DiskFileUpload();
        // maximum size that will be stored in memory
        fu.setSizeThreshold(4096);
        // the location for saving data that is larger than getSizeThreshold()
        fu.setRepositoryPath(OpenCms.getSystemInfo().getPackagesRfsPath());
        List result = new ArrayList();
        try {
            List items = fu.parseRequest(request);
            if (items != null) {
                result = items;
            }
        } catch (FileUploadException e) {
            LOG.error(Messages.get().key(Messages.LOG_PARSE_MULIPART_REQ_FAILED_0), e);
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
    public static Map readParameterMapFromMultiPart(String encoding, List multiPartFileItems) {

        Map parameterMap = new HashMap();
        Iterator i = multiPartFileItems.iterator();
        while (i.hasNext()) {
            FileItem item = (FileItem)i.next();
            String name = item.getFieldName();
            String value = null;
            if (name != null && item.getName() == null) {
                // only put to map if current item is no file and not null
                try {
                    value = item.getString(encoding);
                } catch (UnsupportedEncodingException e) {
                    LOG.error(Messages.get().key(Messages.LOG_ENC_MULTIPART_REQ_ERROR_0), e);
                    value = item.getString();
                }
                parameterMap.put(name, new String[] {value});
            }
        }
        return parameterMap;
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
     * Splits the given uri string into its components <code>scheme://authority/path#fragment?query</code>.<p>
     * 
     * The result array will always be of size 3. Position 0 will contain the path, position 1 will contain the 
     * fragment and position 2 will contain the query part.<p> 
     * 
     * If no fragment or query is part of the uri, then position 1 and/or 2 will be <code>null</code>.
     * 
     * @param uri the uri string to split
     * @return the components of the uri in an array of size 3
     */
    public static String[] splitUri(String uri) {

        String[] components = new String[3];
        try {
            URI u = new URI(uri);
            components[0] = ((u.getScheme() != null) ? u.getScheme() + ":" : "") + u.getRawSchemeSpecificPart();
            components[1] = u.getRawFragment();
            components[2] = u.getRawQuery();
            if (components[0] != null) {
                int i = components[0].indexOf('?');
                if (i != -1) {
                    components[2] = components[0].substring(i + 1);
                    components[0] = components[0].substring(0, i);
                }
            }
            if (components[1] != null) {
                int i = components[1].indexOf('?');
                if (i != -1) {
                    components[2] = components[1].substring(i + 1);
                    components[1] = components[1].substring(0, i);
                }
            }
        } catch (Exception exc) {
            return null;
        }
        return components;
    }
}
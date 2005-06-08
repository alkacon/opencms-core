/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/CmsRequestUtil.java,v $
 * Date   : $Date: 2005/06/08 12:48:57 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.util;

import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.logging.Log;

/**
 * Provides utility functions for dealing with values a <code>{@link HttpServletRequest}</code>.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * @version $Revision: 1.3 $
 * 
 * @since 6.0
 */
public final class CmsRequestUtil {

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
                result.append(values[j]);
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
     * Reads all parameters from the request, even if the request is of type <code></code>.<p> 
     * 
     * @param encoding the encoding to use when to read parameters
     * @param request the request to read the parameters from
     * 
     * @return the parameters (and multipart items if available) read from the request
     */
    public static CmsRequestParameters getRequestParameters(String encoding, HttpServletRequest request) {

        if (FileUploadBase.isMultipartContent(request)) {
            // this is a multipart request, create a map with all non-file parameters
            return readParametersFromMultiPart(encoding, request);
        }
        return new CmsRequestParameters(request.getParameterMap(), null);
    }

    /**
     * Fills the request parameters in a map if treating a multipart request.<p>
     * 
     * The created map has the parameter names as key, multipart items are recognized, too.<p>
     * 
     * @param request the current HTTP servlet request
     * @return a map containing all non-file request parameters
     */
    private static CmsRequestParameters readParametersFromMultiPart(String encoding, HttpServletRequest request) {

        Map parameterMap = new HashMap();
        DiskFileUpload fu = new DiskFileUpload();
        // maximum size that will be stored in memory
        fu.setSizeThreshold(4096);
        // the location for saving data that is larger than getSizeThreshold()
        fu.setRepositoryPath(OpenCms.getSystemInfo().getWebInfRfsPath());
        List multiPartFileItems = null;
        try {
            multiPartFileItems = fu.parseRequest(request);
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

        } catch (FileUploadException e) {
            LOG.error(Messages.get().key(Messages.LOG_PARSE_MULIPART_REQ_FAILED_0), e);
        }
        return new CmsRequestParameters(parameterMap, multiPartFileItems);
    }
}
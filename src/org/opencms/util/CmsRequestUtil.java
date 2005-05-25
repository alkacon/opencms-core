/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/CmsRequestUtil.java,v $
 * Date   : $Date: 2005/05/25 10:56:53 $
 * Version: $Revision: 1.1 $
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

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides utility functions for dealing with values a <code>{@link HttpServletRequest}</code>.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * @version $Revision: 1.1 $
 * 
 * @since 6.0
 */
public final class CmsRequestUtil {

    /** 
     * Default constructor (empty), private because this class has only 
     * static methods.<p>
     */
    private CmsRequestUtil() {

        // empty
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
     * The value of the request will be decoded using <code>{@link CmsEncoder#decode(String)}</code>
     * and also trimmed using <code>{@link String#trim()}</code>.<p>
     * 
     * @param request the request to read the parameter from
     * @param paramName the parameter name to read
     * 
     * @return the request parameter value for the given parameter
     */
    public static String getParameter(HttpServletRequest request, String paramName) {

        String result = request.getParameter(paramName);
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result)) {
            result = CmsEncoder.decode(result.trim());
        } else {
            result = null;
        }
        return result;
    }
}

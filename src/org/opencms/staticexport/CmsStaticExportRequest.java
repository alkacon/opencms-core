/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/staticexport/CmsStaticExportRequest.java,v $
 * Date   : $Date: 2004/04/05 11:05:21 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.staticexport;

import org.opencms.main.OpenCms;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Wrapper for static export requests, required for parameter based requests.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 */
public class CmsStaticExportRequest extends HttpServletRequestWrapper {
    
    /** The data for this static export request */
    private CmsStaticExportData m_data;
    
    /** Map of parameters from the original request */
    private Map m_parameters;    
    
    /**
     * Creates a new static export request wrapper.<p>
     * 
     * @param req the request to wrap
     * @param data the data for the static export
     */
    public CmsStaticExportRequest(HttpServletRequest req, CmsStaticExportData data) {
        
        super(req);
        m_data = data;
        m_parameters = parseParameters(m_data.getParameters());
    }
    
    /**
     * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
     */    
    public String getParameter(String name) {

        String[] values = (String[]) m_parameters.get(name);
        if (values != null) {
            return (values[0]);
        } else {
            return (null);
        }
    }
    
    /**
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    public Map getParameterMap() {

        return m_parameters;
    }
    
    /**
     * @see javax.servlet.ServletRequest#getParameterNames()
     */
    public Enumeration getParameterNames() {

        Vector v = new Vector();
        v.addAll(m_parameters.keySet());
        return (v.elements());
    }
    
    /**
     * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
     */
    public String[] getParameterValues(String name) {

        return (String[]) m_parameters.get(name);
    }      
    
    /**
     * Parses a standard request parameter String in the format
     * <code>?key1=value1&key2=value2&key2=value3</code>.<p>
     *  
     * @param parameters the parameter String to parse
     * @return the Map of parsed parameters
     */
    private Map parseParameters(String parameters) {
        
        Map result = new HashMap(4);
        
        if (parameters == null) {
            return result;
        }
        
        if (parameters.charAt(0) == '?') {
            parameters = parameters.substring(1);
        }
        
        StringTokenizer tok = new StringTokenizer(parameters, "&");
        while (tok.hasMoreTokens()) {
            String param = tok.nextToken();
            int pos = param.indexOf('=');
            if (pos < 0) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Invalild parameter used for static export wrapper " + param);
                }
                continue;
            }
            String key = param.substring(0, pos);
            String value = param.substring(pos + 1);
            
            if (OpenCms.getLog(this).isDebugEnabled()) {
                OpenCms.getLog(this).debug("Adding static export parameter key=" + key + " value=" + value);
            }
            
            // request parameter Map requires String[] not String values
            String[] valueArray;            
            Object current = result.get(key);
            if (current != null) {
                String[] oldValueArray = (String[])current;
                valueArray = new String[oldValueArray.length + 1];
                System.arraycopy(oldValueArray, 0, valueArray, 0, oldValueArray.length);
                valueArray[valueArray.length] = value;                
            } else {
                valueArray = new String[] {value};                
            }
                        
            result.put(key, valueArray);
        }
        
        return result;
    }
}

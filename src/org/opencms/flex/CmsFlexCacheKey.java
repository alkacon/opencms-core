/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/flex/CmsFlexCacheKey.java,v $
 * Date   : $Date: 2004/02/19 19:14:03 $
 * Version: $Revision: 1.6 $
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
 
package org.opencms.flex;

import org.opencms.main.OpenCms;

import org.opencms.file.CmsObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletRequest;

/**
 * Implements the CmsFlexCacheKey,
 * which is a key used to describe the caching behaviour
 * of a specific resource.<p>
 *
 * It has a lot of "public" variables (which isn't good style, I know)
 * to avoid method calling overhead (a cache is about speed, isn't it :).<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.6 $
 */
public class CmsFlexCacheKey {
    
    /** Marker to identify use of certain String key members (m_uri, m_ip) */
    private static final String IS_USED = "/ /";
    
    /** The OpenCms resource that this key is used for. */    
    public String m_resource;
    
    /** The cache behaviour description for the resource. */    
    public String m_variation;

    /** Cache key variable: Determines if this resource can be cached alwys, never or under certain conditions. -1 = never, 0=check, 1=always */
    public int m_always; 
    
    /** Cache key variable: The uri of the original request */
    public String m_uri;
    
    /** Cache key variable: The user id */
    public String m_user;

    /** Cache key variable: List of parameters */
    public java.util.Map m_params;
    
    /** Cache key variable: List of "blocking" parameters */
    public java.util.Set m_noparams;
    
    /** Cache key variable: Timeout of the resource */
    public long m_timeout;
    
    /** Cache key variable: Determines if the resource sould be always cleared at publish time */
    public boolean m_publish;
    
    /** Cache key variable: The ip address of the request */
    public String m_ip; 
        
    /** Cache key variable: Distinguishes request schemes (http, https etc.) */
    public java.util.Set m_schemes;
    
    /** Cache key variable: The request TCP/IP port */
    public java.util.Set m_ports;

    /** The list of keywords of the Flex cache language */
    private static final List m_cacheCmds = Arrays.asList(new String[] {
        "always", "never", "uri", "user", "params", "no-params", "timeout", "publish-clear", "schemes", "ports", "false", "parse-error", "true", "ip", "element", "locale"});
    //   0         1        2      3       4         5            6          7                8          9        10       11             12      13    14         15
    
    /** Flag raised in case a key parse error occured */
    private boolean m_parseError;
    
    /** Debugging flag */
    private static final boolean DEBUG = false;
    
    /**
     * This constructor is used when building a cache key from a request.<p>
     * 
     * The request contains several data items that are neccessary to construct
     * the output. These items are e.g. the Query-String, the requested resource,
     * the current time etc. etc.
     * All required items are saved in the constructed cache - key.<p>
     *
     * @param target the requested resource in the OpenCms VFS
     * @param online must be true for an online resource, false for offline resources
     * @param workplace must be true for all workplace resources
     * @param request the request to construct the key for
     */    
    public CmsFlexCacheKey(ServletRequest request, String target, boolean online, boolean workplace) {
                
        // Fetch the cms from the request
        CmsObject cms = ((CmsFlexController)request.getAttribute(CmsFlexController.ATTRIBUTE_NAME)).getCmsObject();        

        m_resource = getKeyName(cms.getRequestContext().addSiteRoot(target), online, workplace);     
        m_variation = "never";
        
        // Get the top-level file name / uri
        m_uri = cms.getRequestContext().addSiteRoot(cms.getRequestContext().getUri());
        // Fetch user from the current cms
        m_user = cms.getRequestContext().currentUser().getName();        
        // Get the params
        m_params = request.getParameterMap();
        if (m_params.size() == 0) {
            m_params = null;
        }
        // No-params are null for a request key
        m_noparams = null;
        // Save the request time 
        m_timeout = System.currentTimeMillis();
        // publish-clear is not related to the request
        m_publish = false;
        // alwalys is not related to the request 
        m_always = 0;
        // Save the request scheme
        m_schemes = java.util.Collections.singleton(request.getScheme().toLowerCase());
        // Save the request port
        m_ports = java.util.Collections.singleton(new Integer(request.getServerPort()));
        // Save the request ip
        m_ip = cms.getRequestContext().getRemoteAddress();
        if (OpenCms.getLog(this).isDebugEnabled()) {        
            OpenCms.getLog(this).debug("Creating CmsFlexCacheKey for Request: " + this.toString());
        }        
    }
    
    /**
     * This constructor is used when building a cache key from set of cache directives.<p>
     * 
     * These directives are attached to the properties of the requested resource 
     * on a property called "cache". 
     * The value of this poperty that is passed in this constructor as "cacheDirectives" 
     * is parsed to build the keys data structure.<p>
     *
     * In case a parsing error occures, the value of this key is set to "cache=never", 
     * and the hadParseError() flag is set to true. 
     * This is done to ensure that a valid key is always constructed with the constructor.<p>
     *
     * @param resourcename the full name of the resource including site root
     * @param cacheDirectives the cache directives of the resource (value of the property "cache")
     * @param workplace must be true for all workplace resources
     * @param online must be true for an online resource, false for offline resources
     */        
    public CmsFlexCacheKey(String resourcename, String cacheDirectives, boolean online, boolean workplace) {
        m_resource = getKeyName(resourcename, online, workplace);     
        m_variation = "never";
        m_always = -1;
        m_timeout = -1;
        if (cacheDirectives != null) { 
            parseFlexKey(cacheDirectives);
        }
        if (DEBUG) {
            System.err.println("CmsFlexCacheKey for response generated:\n" + this.toString());
        }
    }
    
    /**
     * Calculates the cache key name that is used as key in 
     * the first level of the FlexCache.<p>
     *
     * @param resourcename the full name of the resource including site root
     * @param online must be true for an online resource, false for offline resources
     * @param workplace must be true for all workplace resources
     * @return fhe FlexCache key name
     */
    public static String getKeyName(String resourcename, boolean online, boolean workplace) {
        StringBuffer result = new StringBuffer(resourcename);
        if (workplace) {
            result.append(CmsFlexCache.C_CACHE_WORKPLACESUFFIX);
        } else if (online) {
            result.append(CmsFlexCache.C_CACHE_ONLINESUFFIX);
        } else {
            result.append(CmsFlexCache.C_CACHE_OFFLINESUFFIX);
        }
        return result.toString();           
    }
    
    /**
     * This flag is used to indicate that a parse error had
     * occured, which can happen if the cache directives String
     * passed to the constructor using the response is
     * not build according to the Flex cache language syntax.<p>
     * 
     * @return true if a parse error did occur, false otherwise
     */    
    public boolean hadParseError() {
        return m_parseError;
    }
        
     /**
      * Compares this key to the other key passed as parameter,
      * from comparing the two keys, a variation String is constructed.<p>
      * 
      * This method is the "heart" of the key matching process.<p>
      *
      * The assumtion is that this key should be the one constructed for the response, 
      * while the parameter key should have been constructed from the request.<p>
      *
      * A short example how this works:
      * If the resource key is "cache=groups" and the request is done from a guest user
      * (which always belongs to the default group "guests"),
      * the constructed variation will be "groups=(guests)".<p>
      * 
      * @param key the key to match this key with
      * @return null if not cachable, or the Variation String if cachable
      */
    public String matchRequestKey(CmsFlexCacheKey key) {
        
        StringBuffer str = new StringBuffer(100);
        if (m_always < 0) {
            if (DEBUG) {
                System.err.println("keymatch: cache=never");
            }
            return null;
        } 
               
        if (DEBUG) {
            System.err.println("keymatch: Checking no-params");
        }
        if ((m_noparams != null) && (key.m_params != null)) {
            if ((m_noparams.size() == 0) && (key.m_params.size() > 0)) {
                return null;
            }
            Iterator i = key.m_params.keySet().iterator();
            while (i.hasNext()) {
                if (m_noparams.contains(i.next())) {
                    return null;
                }
            }
        }

        if (m_always > 0) {
            if (DEBUG) {
                System.err.println("keymatch: cache=always");
            }
            str.append("always");
            return str.toString();
        }
        
        if (m_uri != null) {
            str.append("uri=(");
            str.append(key.m_uri);
            str.append(");");
        }
        
        if (m_ip != null) {
            str.append("ip=(");
            str.append(key.m_ip);
            str.append(");");
        }
                
        if (m_user != null) {
            str.append("user=(");
            str.append(key.m_user);
            str.append(");");
        }
        
        if (m_params != null) {
            str.append("params=(");
            if (key.m_params != null) {
                if (m_params.size() > 0) {
                    // Match only params listed in cache directives
                    Iterator i = m_params.keySet().iterator();            
                    while (i.hasNext()) {
                        Object o = i.next();
                        if (key.m_params.containsKey(o)) {
                            str.append(o);
                            str.append("=");
                            // TODO: handle multiple occurences of the same parameter value
                            String[] values = (String[])key.m_params.get(o);
                            str.append(values[0]);
                            if (i.hasNext()) {
                                str.append(",");
                            }
                        }
                    }
                } else {
                    // Match all request params
                    Iterator i = key.m_params.keySet().iterator();            
                    while (i.hasNext()) {
                        Object o = i.next();
                        str.append(o);
                        str.append("=");
                        // TODO: handle multiple occurences of the same parameter value
                        String[] values = (String[])key.m_params.get(o);
                        str.append(values[0]);
                        if (i.hasNext()) {
                            str.append(",");
                        }
                    }                    
                }
            }
            str.append(")");
        }
        
        if (m_schemes != null) {
            String s = (String)key.m_schemes.iterator().next();
            if ((m_schemes.size() > 0) && (! m_schemes.contains(s.toLowerCase()))) {
                return null;
            }
            str.append("schemes=(");
            str.append(s);
            str.append(");");
        }
        
        if (m_ports != null) {
            Integer i = (Integer)key.m_ports.iterator().next();
            if ((m_ports.size() > 0) && (! m_ports.contains(i))) {
                return null;
            }
            str.append("ports=(");
            str.append(i);
            str.append(");");
        }
        
        if (m_timeout > 0) {
            str.append("timeout=(");
            str.append(m_timeout);
            str.append(");");
        }
        
        return str.toString();
    }
    
    /** 
     * @see java.lang.Object#toString()
     *
     * @return a complete String representation for this key
     */
    public String toString() {
        StringBuffer str = new StringBuffer(100);        

        if (m_always < 0) {
            str.append("never"); 
            if (m_parseError) {
                str.append(";parse-error");
            }            
            return str.toString();
        }
        if (m_noparams != null) {
            // Add "no-cachable" parameters
            if (m_noparams.size() == 0) {
                str.append("no-params;");
            } else {
                str.append("no-params=(");
                Iterator i = m_noparams.iterator();
                while (i.hasNext()) {
                    Object o = i.next();
                    str.append(o);
                    if (i.hasNext()) {
                        str.append(",");
                    }
                }                      
                str.append(");");
            }
        }        
        if (m_always > 0) {
            str.append("always");
            if (m_parseError) {
                str.append(";parse-error");
            }            
            return str.toString();
        }
        if (m_uri != null) {
            if (m_uri == IS_USED) {
                str.append("uri;");
            } else {
                str.append("uri=(");
                str.append(m_uri);
                str.append(");");
            }
        }
        if (m_ip != null) {
            if (m_ip == IS_USED) {
                str.append("ip;");
            } else {
                str.append("ip=(");
                str.append(m_ip);
                str.append(");");
            }
        }        
        if (m_user != null) {
            // Add user data
            if (m_user == IS_USED) {
                str.append("user;");
            } else {
                str.append("user=(");
                str.append(m_user);
                str.append(");");
            }
        }               
        if (m_params != null) {
            // Add parameters
            if (m_params.size() == 0) {
                str.append("params;");
            } else {
                str.append("params=(");
                Iterator i = m_params.keySet().iterator();
                while (i.hasNext()) {
                    Object o = i.next();
                    str.append(o);
                    try {
                        // TODO: handle multiple occurences of the same parameter value
                        String[] param = (String[])m_params.get(o);
                        if (! "&?&".equals(param[0])) {
                            str.append("=");
                            str.append(param[0]);
                        }
                    } catch (Exception e) {                        
                        if (DEBUG) {
                            System.err.println("Exception! o=" + o + "  Exception is " + e);
                        }
                    }
                    if (i.hasNext()) {
                        str.append(",");
                    }
                }            
                str.append(");");
            }
        }
        if (m_timeout >= 0) {
            // Add timeout 
            str.append("timeout=(");
            str.append(m_timeout);
            str.append(");");
        }
        if (m_publish) {
            // Add publish parameters
            str.append("publish-clear;");
        }
        if (m_schemes != null) {
            // Add schemes
            if (m_schemes.size() == 0) {
                str.append("schemes;");
            } else {
                str.append("schemes=(");
                Iterator i = m_schemes.iterator();
                while (i.hasNext()) {
                    str.append(i.next());
                    if (i.hasNext()) {
                        str.append(",");
                    }
                }          
                str.append(");");
            }
        }
        if (m_ports != null) {
            // Add ports
            if (m_ports.size() == 0) {
                str.append("ports;");
            } else {
                str.append("ports=(");
                Iterator i = m_ports.iterator();
                while (i.hasNext()) {
                    str.append(i.next());
                    if (i.hasNext()) {
                        str.append(",");
                    }
                } 
                str.append(");");            
            }
        }        
        
        if (m_parseError) {
            str.append("parse-error;");
        }
        return str.toString();
    }
    
    /**
     * Parse a String in the Flex cache language and construct 
     * the key data structure from this.<p>
     *
     * @param key the String to parse (usually read from the file property "cache")
     */    
    private void parseFlexKey(String key) {
        java.util.StringTokenizer toker = new java.util.StringTokenizer(key, ";");
        try {
            while (toker.hasMoreElements()) {
                String t = toker.nextToken();
                String k = null; 
                String v = null;
                int idx = t.indexOf("=");
                if (idx >= 0) {
                    k = t.substring(0, idx).trim();
                    if (t.length() > idx) {
                        v = t.substring(idx+1).trim();
                    }
                } else {
                    k = t.trim();
                }
                m_always = 0;
                if (DEBUG) {
                    System.err.println("Parsing token:" + t + " key=" + k + " value=" + v);
                }
                switch (m_cacheCmds.indexOf(k)) {
                    case 0: // always
                    case 12:                    
                        m_always = 1;
                        // Continue processing (make sure we find a "never" behind "always")
                        break;
                    case 1: // never
                    case 10:
                        m_always = -1;
                        // No need for any further processing
                        return;
                    case 2: // uri
                        m_uri = IS_USED; // marks m_uri as being used
                        break;
                    case 3: // user
                        m_user = IS_USED; // marks m_user as being used
                        break;
                    case 4: // params
                        m_params = parseValueMap(v);
                        break;
                    case 5: // no-params
                        if (v != null) {
                            // No-params are present
                            m_noparams = parseValueMap(v).keySet();
                        } else {
                            // Never cache with parameters
                            m_noparams = new java.util.HashSet(0);
                        }
                        break;
                    case 6: // timeout
                        m_timeout = Integer.parseInt(v);
                        break;
                    case 7: // publish
                        m_publish = true;
                        break;
                    case 8: // schemes
                        m_schemes = parseValueMap(v).keySet();
                        break;
                    case 9: // ports
                        m_ports = parseValueMap(v).keySet();
                        break;
                    case 11: // previous parse error - ignore
                        break;
                    case 13: // ip
                        m_ip = IS_USED; // marks ip as being used
                        break;
                    default: // unknown directive, throw error
                        m_parseError = true;
                }      
            }
        } catch (Exception e) {
            // Any Exception here indicates a parsing error
            if (DEBUG) {
                System.err.println("----- Error in key parsing: " + e.toString());
            }
            m_parseError = true;
        }
        if (m_parseError) {
            // If string is invalid set cache to "never"
            m_always = -1;
        }
    }

    /** 
     * A helper method for the parsing process which parses
     * Strings like groups=(a, b, c).<p>
     *
     * @param value the String to parse 
     * @return a Map that contains of the parsed values, only the keyset of the Map is needed later
     */    
    private java.util.Map parseValueMap(String value) {
        if (value.charAt(0) == '(') {
            value = value.substring(1);
        }
        int len = value.length() - 1;
        if (value.charAt(len) == ')') {
            value = value.substring(0, len);
        }
        if (value.charAt(len-1) == ',') {
            value = value.substring(0, len-1);
        }
        if (DEBUG) {
            System.err.println("Parsing map: " + value);
        }
        java.util.StringTokenizer toker = new java.util.StringTokenizer(value, ",");
        java.util.Map result = new java.util.HashMap();
        while (toker.hasMoreTokens()) {
            result.put(toker.nextToken().trim(), new String[] {"&?&"});
        }
        return result;
    }
    
}

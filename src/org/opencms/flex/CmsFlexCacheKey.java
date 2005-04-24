/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/flex/CmsFlexCacheKey.java,v $
 * Date   : $Date: 2005/04/24 11:20:30 $
 * Version: $Revision: 1.16 $
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

package org.opencms.flex;

import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;

/**
 * Implements the CmsFlexCacheKey,
 * which is a key used to describe the caching behaviour
 * of a specific resource.<p>
 *
 * It has a lot of variables that are directly accessed (which isn't good style, I know)
 * to avoid method calling overhead (a cache is about speed, isn't it :).<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.16 $
 */
public class CmsFlexCacheKey {

    /** The list of keywords of the Flex cache language. */
    private static final List CACHE_COMMANDS = Arrays.asList(new String[] {"always", // 0
        "never", // 1
        "uri", // 2
        "user", // 3
        "params", // 4
        "no-params", // 5
        "timeout", // 6
        "session", // 7
        "schemes", // 8
        "ports", // 9
        "false", // 10
        "parse-error", // 11
        "true", // 12
        "ip", // 13
        "element", // 14
        "locale", // 15
        "encoding"}); // 16

    /** Marker to identify use of certain String key members (m_uri, m_ip). */
    private static final String IS_USED = "/ /";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFlexCacheKey.class);

    /** Cache key variable: Determines if this resource can be cached alwys, never or under certain conditions. -1 = never, 0=check, 1=always. */
    private int m_always;

    /** Cache key variable: The requested element. */
    private String m_element;

    /** Cache key variable: The requested encoding. */
    private String m_encoding;

    /** Cache key variable: The ip address of the request. */
    private String m_ip;

    /** Cache key variable: The requested locale. */
    private String m_locale;

    /** Cache key variable: List of "blocking" parameters. */
    private Set m_noparams;

    /** Cache key variable: List of parameters. */
    private Set m_params;

    /** Flag raised in case a key parse error occured. */
    private boolean m_parseError;

    /** Cache key variable: The request TCP/IP port. */
    private Set m_ports;

    /** The OpenCms resource that this key is used for. */
    private String m_resource;

    /** Cache key variable: Distinguishes request schemes (http, https etc.). */
    private Set m_schemes;

    /** Cache key variable: List of session variables. */
    private Set m_session;

    /** Cache key variable: timeout of the resource. */
    private long m_timeout;

    /** Cache key variable: the uri of the original request. */
    private String m_uri;

    /** Cache key variable: the user id. */
    private String m_user;

    /** The cache behaviour description for the resource. */
    private String m_variation;

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
     * @param online must be true for an online resource, false for offline resources
     */
    public CmsFlexCacheKey(String resourcename, String cacheDirectives, boolean online) {

        m_resource = getKeyName(resourcename, online);
        m_variation = "never";
        m_always = -1;
        m_timeout = -1;
        if (cacheDirectives != null) {
            parseFlexKey(cacheDirectives);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_FLEXCACHEKEY_GENERATED_1, toString()));
        }
    }

    /**
     * Calculates the cache key name that is used as key in 
     * the first level of the FlexCache.<p>
     * 
     * @param resourcename the full name of the resource including site root
     * @param online must be true for an online resource, false for offline resources
     *
     * @return fhe FlexCache key name
     */
    public static String getKeyName(String resourcename, boolean online) {

        return resourcename.concat(online ? CmsFlexCache.C_CACHE_ONLINESUFFIX : CmsFlexCache.C_CACHE_OFFLINESUFFIX);
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
     * If the cache key is "cache=user" and the request is done from a guest user
     * the constructed variation will be "user=(guest)".<p>
     * 
     * @param key the key to match this key with
     * @return null if not cachable, or the Variation String if cachable
     */
    public String matchRequestKey(CmsFlexRequestKey key) {

        StringBuffer str = new StringBuffer(100);
        if (m_always < 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_FLEXCACHEKEY_KEYMATCH_CACHE_NEVER_0));
            }
            return null;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_FLEXCACHEKEY_KEYMATCH_CHECK_NO_PARAMS_0));
        }
        if ((m_noparams != null) && (key.getParams() != null)) {
            if ((m_noparams.size() == 0) && (key.getParams().size() > 0)) {
                return null;
            }
            Iterator i = key.getParams().keySet().iterator();
            while (i.hasNext()) {
                if (m_noparams.contains(i.next())) {
                    return null;
                }
            }
        }

        if (m_always > 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_FLEXCACHEKEY_KEYMATCH_CACHE_ALWAYS_0));
            }
            str.append("always");
            return str.toString();
        }

        if (m_uri != null) {
            str.append("uri=(");
            str.append(key.getUri());
            str.append(");");
        }

        if (m_element != null) {
            str.append("element=(");
            str.append(key.getElement());
            str.append(");");
        }

        if (m_locale != null) {
            str.append("locale=(");
            str.append(key.getLocale());
            str.append(");");
        }

        if (m_encoding != null) {
            str.append("encoding=(");
            str.append(key.getEncoding());
            str.append(");");
        }

        if (m_ip != null) {
            str.append("ip=(");
            str.append(key.getIp());
            str.append(");");
        }

        if (m_user != null) {
            str.append("user=(");
            str.append(key.getUser());
            str.append(");");
        }

        if (m_params != null) {
            str.append("params=(");
            Map keyParams = key.getParams();
            if (keyParams != null) {
                if (m_params.size() > 0) {
                    // match only params listed in cache directives
                    Iterator i = m_params.iterator();
                    while (i.hasNext()) {
                        Object o = i.next();
                        if (keyParams.containsKey(o)) {
                            str.append(o);
                            str.append("=");
                            // TODO: handle multiple occurences of the same parameter value
                            String[] values = (String[])keyParams.get(o);
                            str.append(values[0]);
                            if (i.hasNext()) {
                                str.append(",");
                            }
                        }
                    }
                } else {
                    // match all request params
                    Iterator i = keyParams.keySet().iterator();
                    while (i.hasNext()) {
                        Object o = i.next();
                        str.append(o);
                        str.append("=");
                        // TODO: handle multiple occurences of the same parameter value
                        String[] values = (String[])keyParams.get(o);
                        str.append(values[0]);
                        if (i.hasNext()) {
                            str.append(",");
                        }
                    }
                }
            }
            str.append(")");
        }

        if (m_session != null) {
            StringBuffer buf = new StringBuffer(32);
            boolean found = false;
            buf.append("session=(");
            HttpSession keySession = key.getSession();
            if (keySession != null) {
                // match only session attributes listed in cache directives
                Iterator i = m_session.iterator();
                while (i.hasNext()) {
                    String name = (String)i.next();
                    Object val = keySession.getAttribute(name);
                    if (val != null) {
                        found = true;
                        buf.append(name);
                        buf.append("=");
                        buf.append(val);
                        if (i.hasNext()) {
                            buf.append(",");
                        }
                    }
                }
            }
            if (found) {
                buf.append(")");
                str.append(buf);
            }
        }

        if (m_schemes != null) {
            String s = key.getScheme();
            if ((m_schemes.size() > 0) && (!m_schemes.contains(s))) {
                return null;
            }
            str.append("schemes=(");
            str.append(s);
            str.append(");");
        }

        if (m_ports != null) {
            Integer i = key.getPort();
            if ((m_ports.size() > 0) && (!m_ports.contains(i))) {
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

        if (str.length() > 0) {
            return str.toString();
        } else {
            return null;
        }
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
            // add "no-cachable" parameters
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
        if (m_element != null) {
            // add element
            if (m_element == IS_USED) {
                str.append("element;");
            } else {
                str.append("element=(");
                str.append(m_element);
                str.append(");");
            }
        }
        if (m_locale != null) {
            // add locale
            if (m_locale == IS_USED) {
                str.append("locale;");
            } else {
                str.append("locale=(");
                str.append(m_locale);
                str.append(");");
            }
        }
        if (m_encoding != null) {
            // add encoding
            if (m_encoding == IS_USED) {
                str.append("encoding;");
            } else {
                str.append("encoding=(");
                str.append(m_encoding);
                str.append(");");
            }
        }
        if (m_ip != null) {
            // add ip
            if (m_ip == IS_USED) {
                str.append("ip;");
            } else {
                str.append("ip=(");
                str.append(m_ip);
                str.append(");");
            }
        }
        if (m_user != null) {
            // add user data
            if (m_user == IS_USED) {
                str.append("user;");
            } else {
                str.append("user=(");
                str.append(m_user);
                str.append(");");
            }
        }
        if (m_params != null) {
            // add parameters
            if (m_params.size() == 0) {
                str.append("params;");
            } else {
                str.append("params=(");
                Iterator i = m_params.iterator();
                while (i.hasNext()) {
                    Object o = i.next();
                    if (I_CmsConstants.C_PARAMETER_ELEMENT.equals(o)) {
                        continue;
                    }
                    str.append(o);
                    if (i.hasNext()) {
                        str.append(",");
                    }
                }
                str.append(");");
            }
        }
        if (m_session != null) {
            // add session variables
            str.append("session=(");
            Iterator i = m_session.iterator();
            while (i.hasNext()) {
                Object o = i.next();
                str.append(o);
                if (i.hasNext()) {
                    str.append(",");
                }
            }
            str.append(");");
        }
        if (m_timeout >= 0) {
            // add timeout 
            str.append("timeout=(");
            str.append(m_timeout);
            str.append(");");
        }
        if (m_schemes != null) {
            // add schemes
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
            // add ports
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
     * Returns the resource.<p>
     *
     * @return the resource
     */
    protected String getResource() {

        return m_resource;
    }

    /**
     * Returns the timeout.<p>
     *
     * @return the timeout
     */
    protected long getTimeout() {

        return m_timeout;
    }

    /**
     * Returns the variation.<p>
     *
     * @return the variation
     */
    protected String getVariation() {

        return m_variation;
    }

    /**
     * Sets the variation.<p>
     *
     * @param variation the variation to set
     */
    protected void setVariation(String variation) {

        m_variation = variation;
    }

    /**
     * Parse a String in the Flex cache language and construct 
     * the key data structure from this.<p>
     *
     * @param key the String to parse (usually read from the file property "cache")
     */
    private void parseFlexKey(String key) {

        StringTokenizer toker = new StringTokenizer(key, ";");
        try {
            while (toker.hasMoreElements()) {
                String t = toker.nextToken();
                String k = null;
                String v = null;
                int idx = t.indexOf("=");
                if (idx >= 0) {
                    k = t.substring(0, idx).trim();
                    if (t.length() > idx) {
                        v = t.substring(idx + 1).trim();
                    }
                } else {
                    k = t.trim();
                }
                m_always = 0;
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(Messages.LOG_FLEXCACHEKEY_PARSE_FLEXKEY_3, t, k, v));
                }
                switch (CACHE_COMMANDS.indexOf(k)) {
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
                        m_params = parseValueList(v);
                        if (m_params.contains(I_CmsConstants.C_PARAMETER_ELEMENT)) {
                            // workaround for element setting by parameter in OpenCms < 6.0
                            m_element = IS_USED;
                            m_params.remove(I_CmsConstants.C_PARAMETER_ELEMENT);
                            if (m_params.size() == 0) {
                                m_params = null;
                            }
                        }
                        break;
                    case 5: // no-params
                        if (v != null) {
                            // No-params are present
                            m_noparams = parseValueList(v);
                        } else {
                            // Never cache with parameters
                            m_noparams = new HashSet(0);
                        }
                        break;
                    case 6: // timeout
                        m_timeout = Integer.parseInt(v);
                        break;
                    case 7: // session
                        m_session = parseValueList(v);
                        if (m_session.size() <= 0) {
                            // session must have at last one variable set
                            m_parseError = true;
                        }
                        break;
                    case 8: // schemes
                        m_schemes = parseValueList(v);
                        break;
                    case 9: // ports
                        m_ports = parseValueList(v);
                        break;
                    case 11: // previous parse error - ignore
                        break;
                    case 13: // ip
                        m_ip = IS_USED; // marks ip as being used
                        break;
                    case 14: // element
                        m_element = IS_USED;
                        break;
                    case 15: // locale
                        m_locale = IS_USED;
                        break;
                    case 16: // encoding
                        m_encoding = IS_USED;
                        break;
                    default: // unknown directive, throw error
                        m_parseError = true;
                }
            }
        } catch (Exception e) {
            // Any Exception here indicates a parsing error
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_FLEXCACHEKEY_PARSE_ERROR_1, e.toString()));
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
    private Set parseValueList(String value) {

        if (value.charAt(0) == '(') {
            value = value.substring(1);
        }
        int len = value.length() - 1;
        if (value.charAt(len) == ')') {
            value = value.substring(0, len);
        }
        if (value.charAt(len - 1) == ',') {
            value = value.substring(0, len - 1);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_FLEXCACHEKEY_PARSE_VALUES_1, value));
        }
        StringTokenizer toker = new StringTokenizer(value, ",");
        Set result = new HashSet();
        while (toker.hasMoreTokens()) {
            result.add(toker.nextToken().trim());
        }
        return result;
    }
}
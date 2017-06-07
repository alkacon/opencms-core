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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;

/**
 * Key used to describe the caching behaviour of a specific resource.<p>
 *
 * It has a lot of variables that are directly accessed (which isn't good style, I know)
 * to avoid method calling overhead (a cache is about speed, isn't it :).<p>
 *
 * @since 6.0.0
 */
public class CmsFlexCacheKey {

    /** Flex cache keyword: always. */
    private static final String CACHE_00_ALWAYS = "always";

    /** Flex cache keyword: never. */
    private static final String CACHE_01_NEVER = "never";

    /** Flex cache keyword: uri. */
    private static final String CACHE_02_URI = "uri";

    /** Flex cache keyword: user. */
    private static final String CACHE_03_USER = "user";

    /** Flex cache keyword: params. */
    private static final String CACHE_04_PARAMS = "params";

    /** Flex cache keyword: no-params. */
    private static final String CACHE_05_NO_PARAMS = "no-params";

    /** Flex cache keyword: timeout. */
    private static final String CACHE_06_TIMEOUT = "timeout";

    /** Flex cache keyword: session. */
    private static final String CACHE_07_SESSION = "session";

    /** Flex cache keyword: schemes. */
    private static final String CACHE_08_SCHEMES = "schemes";

    /** Flex cache keyword: ports. */
    private static final String CACHE_09_PORTS = "ports";

    /** Flex cache keyword: false. */
    private static final String CACHE_10_FALSE = CmsStringUtil.FALSE;

    /** Flex cache keyword: parse-error. */
    private static final String CACHE_11_PARSE_ERROR = "parse-error";

    /** Flex cache keyword: true. */
    private static final String CACHE_12_TRUE = CmsStringUtil.TRUE;

    /** Flex cache keyword: ip. */
    private static final String CACHE_13_IP = "ip";

    /** Flex cache keyword: element. */
    private static final String CACHE_14_ELEMENT = "element";

    /** Flex cache keyword: locale. */
    private static final String CACHE_15_LOCALE = "locale";

    /** Flex cache keyword: encoding. */
    private static final String CACHE_16_ENCODING = "encoding";

    /** Flex cache keyword: site. */
    private static final String CACHE_17_SITE = "site";

    /** Flex cache keyword: attrs. */
    private static final String CACHE_18_ATTRS = "attrs";

    /** Flex cache keyword: no-attrs. */
    private static final String CACHE_19_NO_ATTRS = "no-attrs";

    /** Flex cache keyword: device. */
    private static final String CACHE_20_DEVICE = "device";

    /** Flex cache keyword: container-element. */
    private static final String CACHE_21_CONTAINER_ELEMENT = "container-element";

    /** The list of keywords of the Flex cache language. */
    private static final List<String> CACHE_COMMANDS = Arrays.asList(
        new String[] {
            CACHE_00_ALWAYS,
            CACHE_01_NEVER,
            CACHE_02_URI,
            CACHE_03_USER,
            CACHE_04_PARAMS,
            CACHE_05_NO_PARAMS,
            CACHE_06_TIMEOUT,
            CACHE_07_SESSION,
            CACHE_08_SCHEMES,
            CACHE_09_PORTS,
            CACHE_10_FALSE,
            CACHE_11_PARSE_ERROR,
            CACHE_12_TRUE,
            CACHE_13_IP,
            CACHE_14_ELEMENT,
            CACHE_15_LOCALE,
            CACHE_16_ENCODING,
            CACHE_17_SITE,
            CACHE_18_ATTRS,
            CACHE_19_NO_ATTRS,
            CACHE_20_DEVICE,
            CACHE_21_CONTAINER_ELEMENT});

    /** Marker to identify use of certain String key members (uri, ip etc.). */
    private static final String IS_USED = "/ /";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFlexCacheKey.class);

    /** Cache key variable: Determines if this resource can be cached alwys, never or under certain conditions. -1 = never, 0=check, 1=always. */
    private int m_always;

    /** Cache key variable: List of attributes. */
    private Set<String> m_attrs;

    /** Cache key variable: The current container element. */
    private String m_containerElement;

    /** Cache key variable: The current device. */
    private String m_device;

    /** Cache key variable: The requested element. */
    private String m_element;

    /** Cache key variable: The requested encoding. */
    private String m_encoding;

    /** Cache key variable: The ip address of the request. */
    private String m_ip;

    /** Cache key variable: The requested locale. */
    private String m_locale;

    /** Cache key variable: List of "blocking" attributes. */
    private Set<String> m_noattrs;

    /** Cache key variable: List of "blocking" parameters. */
    private Set<String> m_noparams;

    /** Cache key variable: List of parameters. */
    private Set<String> m_params;

    /** Flag raised in case a key parse error occurred. */
    private boolean m_parseError;

    /** Cache key variable: The request TCP/IP port. */
    private Set<Integer> m_ports;

    /** The OpenCms resource that this key is used for. */
    private String m_resource;

    /** Cache key variable: Distinguishes request schemes (http, https etc.). */
    private Set<String> m_schemes;

    /** Cache key variable: List of session variables. */
    private Set<String> m_session;

    /** Cache key variable: The current site root. */
    private String m_site;

    /** Cache key variable: Timeout of the resource. */
    private long m_timeout;

    /** Cache key variable: The uri of the original request. */
    private String m_uri;

    /** Cache key variable: The user id. */
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
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXCACHEKEY_GENERATED_1, toString()));
        }
    }

    /**
     * Calculates the cache key name that is used as key in
     * the first level of the FlexCache.<p>
     *
     * @param resourcename the full name of the resource including site root
     * @param online must be true for an online resource, false for offline resources
     *
     * @return the FlexCache key name
     */
    public static String getKeyName(String resourcename, boolean online) {

        return resourcename.concat(online ? CmsFlexCache.CACHE_ONLINESUFFIX : CmsFlexCache.CACHE_OFFLINESUFFIX);
    }

    /**
     * Appends a flex cache key value to the given buffer.<p>
     *
     * @param str the buffer to append to
     * @param key the key to append
     * @param value the value to append
     */
    private static void appendKeyValue(StringBuffer str, String key, String value) {

        str.append(key);
        if (value == IS_USED) {
            str.append(";");
        } else {
            str.append("=(");
            str.append(value);
            str.append(");");
        }
    }

    /**
     * This flag is used to indicate that a parse error had
     * occurred, which can happen if the cache directives String
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
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXCACHEKEY_KEYMATCH_CACHE_NEVER_0));
            }
            return null;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXCACHEKEY_KEYMATCH_CHECK_NO_PARAMS_0));
        }
        if ((m_noparams != null) && (key.getParams() != null)) {
            if ((m_noparams.size() == 0) && (key.getParams().size() > 0)) {
                return null;
            }
            Iterator<String> i = key.getParams().keySet().iterator();
            while (i.hasNext()) {
                if (m_noparams.contains(i.next())) {
                    return null;
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXCACHEKEY_KEYMATCH_CHECK_NO_ATTRS_0));
        }
        if ((m_noattrs != null) && (key.getAttributes() != null)) {
            if ((m_noattrs.size() == 0) && (key.getAttributes().size() > 0)) {
                return null;
            }
            Iterator<String> i = key.getAttributes().keySet().iterator();
            while (i.hasNext()) {
                if (m_noattrs.contains(i.next())) {
                    return null;
                }
            }
        }

        if (m_always > 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXCACHEKEY_KEYMATCH_CACHE_ALWAYS_0));
            }
            str.append(CACHE_00_ALWAYS);
            return str.toString();
        }

        if (m_uri != null) {
            appendKeyValue(str, CACHE_02_URI, key.getUri());
        }

        if (m_site != null) {
            appendKeyValue(str, CACHE_17_SITE, key.getSite());
        }

        if (m_element != null) {
            appendKeyValue(str, CACHE_14_ELEMENT, key.getElement());
        }

        if (m_device != null) {
            appendKeyValue(str, CACHE_20_DEVICE, key.getDevice());
        }

        if (m_containerElement != null) {
            appendKeyValue(str, CACHE_21_CONTAINER_ELEMENT, key.getContainerElement());
        }

        if (m_locale != null) {
            appendKeyValue(str, CACHE_15_LOCALE, key.getLocale());
        }

        if (m_encoding != null) {
            appendKeyValue(str, CACHE_16_ENCODING, key.getEncoding());
        }

        if (m_ip != null) {
            appendKeyValue(str, CACHE_13_IP, key.getIp());
        }

        if (m_user != null) {
            appendKeyValue(str, CACHE_03_USER, key.getUser());
        }

        if (m_params != null) {
            str.append(CACHE_04_PARAMS);
            str.append("=(");
            Map<String, String[]> keyParams = key.getParams();
            if (keyParams != null) {
                if (m_params.size() > 0) {
                    // match only params listed in cache directives
                    Iterator<String> i = m_params.iterator();
                    while (i.hasNext()) {
                        Object o = i.next();
                        if (keyParams.containsKey(o)) {
                            str.append(o);
                            str.append("=");
                            // TODO: handle multiple occurrences of the same parameter value
                            String[] values = keyParams.get(o);
                            str.append(values[0]);
                            if (i.hasNext()) {
                                str.append(",");
                            }
                        }
                    }
                } else {
                    // match all request params
                    Iterator<Map.Entry<String, String[]>> i = keyParams.entrySet().iterator();
                    while (i.hasNext()) {
                        Map.Entry<String, String[]> entry = i.next();
                        str.append(entry.getKey());
                        str.append("=");
                        // TODO: handle multiple occurrences of the same parameter value
                        String[] values = entry.getValue();
                        str.append(values[0]);
                        if (i.hasNext()) {
                            str.append(",");
                        }
                    }
                }
            }
            str.append(");");
        }

        if (m_attrs != null) {
            str.append(CACHE_18_ATTRS);
            str.append("=(");
            Map<String, Object> keyAttrs = key.getAttributes();
            if (keyAttrs != null) {
                if (m_attrs.size() > 0) {
                    // match only attributes listed in cache directives
                    Iterator<String> i = m_attrs.iterator();
                    while (i.hasNext()) {
                        String s = i.next();
                        if (keyAttrs.containsKey(s)) {
                            str.append(s);
                            str.append("=");
                            Object value = keyAttrs.get(s);
                            str.append(value);
                            if (i.hasNext()) {
                                str.append(",");
                            }
                        }
                    }
                } else {
                    // match all request attributes
                    Iterator<Map.Entry<String, Object>> i = keyAttrs.entrySet().iterator();
                    while (i.hasNext()) {
                        Map.Entry<String, Object> entry = i.next();
                        str.append(entry.getKey());
                        str.append("=");
                        Object value = entry.getValue();
                        str.append(value);
                        if (i.hasNext()) {
                            str.append(",");
                        }
                    }
                }
            }
            str.append(");");
        }

        if (m_session != null) {
            StringBuffer buf = new StringBuffer(32);
            boolean found = false;
            buf.append(CACHE_07_SESSION);
            buf.append("=(");
            HttpSession keySession = key.getSession();
            if (keySession != null) {
                // match only session attributes listed in cache directives
                Iterator<String> i = m_session.iterator();
                while (i.hasNext()) {
                    String name = i.next();
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
                buf.append(");");
                str.append(buf);
            }
        }

        if (m_schemes != null) {
            String s = key.getScheme();
            if ((m_schemes.size() > 0) && (!m_schemes.contains(s))) {
                return null;
            }
            appendKeyValue(str, CACHE_08_SCHEMES, s);
        }

        if (m_ports != null) {
            Integer i = key.getPort();
            if ((m_ports.size() > 0) && (!m_ports.contains(i))) {
                return null;
            }
            str.append(CACHE_09_PORTS);
            str.append("=(");
            str.append(i);
            str.append(");");
        }

        if (m_timeout > 0) {
            str.append(CACHE_06_TIMEOUT);
            str.append("=(");
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
    @Override
    public String toString() {

        StringBuffer str = new StringBuffer(100);

        if (m_always < 0) {
            str.append(CACHE_01_NEVER);
            if (m_parseError) {
                str.append(";");
                str.append(CACHE_11_PARSE_ERROR);
            }
            return str.toString();
        }
        if (m_noparams != null) {
            // add "no-cachable" parameters
            str.append(CACHE_05_NO_PARAMS);
            if (m_noparams.size() == 0) {
                str.append(";");
            } else {
                str.append("=(");
                Iterator<String> i = m_noparams.iterator();
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
        if (m_noattrs != null) {
            // add "no-cachable" attributes
            str.append(CACHE_19_NO_ATTRS);
            if (m_noattrs.size() == 0) {
                str.append(";");
            } else {
                str.append("=(");
                Iterator<String> i = m_noattrs.iterator();
                while (i.hasNext()) {
                    String s = i.next();
                    str.append(s);
                    if (i.hasNext()) {
                        str.append(",");
                    }
                }
                str.append(");");
            }
        }
        if (m_always > 0) {
            str.append(CACHE_00_ALWAYS);
            if (m_parseError) {
                str.append(";");
                str.append(CACHE_11_PARSE_ERROR);
            }
            return str.toString();
        }
        if (m_uri != null) {
            // add uri
            appendKeyValue(str, CACHE_02_URI, m_uri);
        }
        if (m_site != null) {
            // add site
            appendKeyValue(str, CACHE_17_SITE, m_site);
        }
        if (m_element != null) {
            // add element
            appendKeyValue(str, CACHE_14_ELEMENT, m_element);
        }
        if (m_device != null) {
            appendKeyValue(str, CACHE_20_DEVICE, m_device);
        }
        if (m_containerElement != null) {
            appendKeyValue(str, CACHE_21_CONTAINER_ELEMENT, m_containerElement);
        }
        if (m_locale != null) {
            // add locale
            appendKeyValue(str, CACHE_15_LOCALE, m_locale);
        }
        if (m_encoding != null) {
            // add encoding
            appendKeyValue(str, CACHE_16_ENCODING, m_encoding);
        }
        if (m_ip != null) {
            // add ip
            appendKeyValue(str, CACHE_13_IP, m_ip);
        }
        if (m_user != null) {
            // add user
            appendKeyValue(str, CACHE_03_USER, m_user);
        }
        if (m_params != null) {
            // add parameters
            str.append(CACHE_04_PARAMS);
            if (m_params.size() == 0) {
                str.append(";");
            } else {
                str.append("=(");
                Iterator<String> i = m_params.iterator();
                while (i.hasNext()) {
                    Object o = i.next();
                    if (I_CmsResourceLoader.PARAMETER_ELEMENT.equals(o)) {
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
        if (m_attrs != null) {
            // add attributes
            str.append(CACHE_18_ATTRS);
            if (m_attrs.size() == 0) {
                str.append(";");
            } else {
                str.append("=(");
                Iterator<String> i = m_attrs.iterator();
                while (i.hasNext()) {
                    String s = i.next();
                    str.append(s);
                    if (i.hasNext()) {
                        str.append(",");
                    }
                }
                str.append(");");
            }
        }
        if (m_session != null) {
            // add session variables
            str.append(CACHE_07_SESSION);
            str.append("=(");
            Iterator<String> i = m_session.iterator();
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
            str.append(CACHE_06_TIMEOUT);
            str.append("=(");
            str.append(m_timeout);
            str.append(");");
        }
        if (m_schemes != null) {
            // add schemes
            str.append(CACHE_08_SCHEMES);
            if (m_schemes.size() == 0) {
                str.append(";");
            } else {
                str.append("=(");
                Iterator<String> i = m_schemes.iterator();
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
            str.append(CACHE_09_PORTS);
            if (m_ports.size() == 0) {
                str.append(";");
            } else {
                str.append("=(");
                Iterator<Integer> i = m_ports.iterator();
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
            str.append(CACHE_11_PARSE_ERROR);
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

        List<String> tokens = CmsStringUtil.splitAsList(key, ';', false);
        Iterator<String> i = tokens.iterator();
        try {
            while (i.hasNext()) {
                String t = i.next();
                String k = null;
                String v = null;
                int idx = t.indexOf('=');
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
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXCACHEKEY_PARSE_FLEXKEY_3, t, k, v));
                }
                switch (CACHE_COMMANDS.indexOf(k)) {
                    case 0: // always
                    case 12: // true
                        m_always = 1;
                        // continue processing (make sure we find a "never" behind "always")
                        break;
                    case 1: // never
                    case 10: // false
                        m_always = -1;
                        // no need for any further processing
                        return;
                    case 2: // uri
                        m_uri = IS_USED; // marks m_uri as being used
                        break;
                    case 3: // user
                        m_user = IS_USED; // marks m_user as being used
                        break;
                    case 4: // params
                        if (v != null) {
                            m_params = parseValueList(v);
                        } else {
                            m_params = Collections.emptySet();
                        }

                        if (m_params.contains(I_CmsResourceLoader.PARAMETER_ELEMENT)) {
                            // workaround for element setting by parameter in OpenCms < 6.0
                            m_element = IS_USED;
                            m_params.remove(I_CmsResourceLoader.PARAMETER_ELEMENT);
                            if (m_params.size() == 0) {
                                m_params = null;
                            }
                        }
                        break;
                    case 5: // no-params
                        if (v != null) {
                            // no-params are present
                            m_noparams = parseValueList(v);
                        } else {
                            // never cache with parameters
                            m_noparams = Collections.emptySet();
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
                        Set<String> ports = parseValueList(v);
                        m_ports = new HashSet<Integer>(ports.size());
                        for (String p : ports) {
                            try {
                                m_ports.add(Integer.valueOf(p));
                            } catch (NumberFormatException e) {
                                // ignore this number
                            }
                        }
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
                    case 17: // site
                        m_site = IS_USED;
                        break;
                    case 18: // attrs
                        if (v != null) {
                            m_attrs = parseValueList(v);
                        } else {
                            m_attrs = null;
                        }
                        break;
                    case 19: // no-attrs
                        if (v != null) {
                            // no-attrs are present
                            m_noattrs = parseValueList(v);
                        } else {
                            // never cache with attributes
                            m_noattrs = Collections.emptySet();
                        }
                        break;
                    case 20: // device
                        m_device = IS_USED; // marks m_device as being used
                        break;
                    case 21: // container element
                        m_containerElement = IS_USED;
                        break;
                    default: // unknown directive, throw error
                        m_parseError = true;
                }
            }
        } catch (Exception e) {
            // any Exception here indicates a parsing error
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_FLEXCACHEKEY_PARSE_ERROR_1, e.toString()), e);
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
    private Set<String> parseValueList(String value) {

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
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_FLEXCACHEKEY_PARSE_VALUES_1, value));
        }
        List<String> tokens = CmsStringUtil.splitAsList(value, ',', true);
        Set<String> result = new HashSet<String>();
        result.addAll(tokens);
        return result;
    }
}
/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.test.mock;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

/**
 * Functional {@link HttpServletRequest} mock for OpenCms tests, sufficient to drive the OpenCms
 * request handlers and the session based login flow without a servlet container.<p>
 *
 * It implements the small set of request methods that OpenCms actually reads during request handling
 * and session initialization (method, path info, request URL, scheme/host/port, headers, parameters,
 * attributes, remote address, the request body and the {@link HttpSession}). Everything else is
 * delegated to a throwing proxy via {@link HttpServletRequestWrapper}, so an unexpected dependency
 * fails fast with an {@link UnsupportedOperationException} instead of returning a silently wrong
 * value.<p>
 *
 * The setters are fluent so a request can be assembled in one expression:<p>
 *
 * <pre>
 * CmsMockHttpServletRequest req = new CmsMockHttpServletRequest()
 *     .withMethod("POST")
 *     .withPath("/handleAgent/v1/session/login")
 *     .withJsonBody("{\"username\":\"Admin\",\"password\":\"admin\"}");
 * </pre>
 *
 * @see CmsMockHttpServletResponse
 * @see CmsMockHttpSession
 */
public class CmsMockHttpServletRequest extends HttpServletRequestWrapper {

    /** The request attributes. */
    private final Map<String, Object> m_attributes = new LinkedHashMap<String, Object>();

    /** The request body. */
    private byte[] m_body = new byte[0];

    /** The character encoding. */
    private String m_characterEncoding = "UTF-8";

    /** The context path. */
    private String m_contextPath = "";

    /** The request headers (single value each, case-insensitive lookup via lower-cased keys). */
    private final Map<String, String> m_headers = new LinkedHashMap<String, String>();

    /** The HTTP method. */
    private String m_method = "GET";

    /** The request parameters. */
    private final Map<String, String[]> m_parameters = new LinkedHashMap<String, String[]>();

    /** The path info (the part after the servlet path, e.g. the request handler endpoint). */
    private String m_pathInfo = "/";

    /** The remote address. */
    private String m_remoteAddr = "127.0.0.1";

    /** The request URI. */
    private String m_requestUri;

    /** The scheme. */
    private String m_scheme = "http";

    /** The server name. */
    private String m_serverName = "localhost";

    /** The server port. */
    private int m_serverPort = 8088;

    /** The HTTP session, lazily created. */
    private CmsMockHttpSession m_session;

    /**
     * Creates a new mock request, delegating all not explicitly implemented methods to a throwing
     * proxy.<p>
     */
    public CmsMockHttpServletRequest() {

        super(throwingRequest());
    }

    /**
     * Creates an {@link HttpServletRequest} proxy that throws {@link UnsupportedOperationException}
     * for every call, used as the delegate for the not implemented wrapper methods.<p>
     *
     * @return the throwing request
     */
    private static HttpServletRequest throwingRequest() {

        return (HttpServletRequest)Proxy.newProxyInstance(
            CmsMockHttpServletRequest.class.getClassLoader(),
            new Class<?>[] {HttpServletRequest.class},
            (proxy, method, args) -> {
                throw new UnsupportedOperationException("HttpServletRequest." + method.getName());
            });
    }

    /**
     * @see javax.servlet.ServletRequestWrapper#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(String name) {

        return m_attributes.get(name);
    }

    /**
     * @see javax.servlet.ServletRequestWrapper#getAttributeNames()
     */
    @Override
    public Enumeration<String> getAttributeNames() {

        return Collections.enumeration(m_attributes.keySet());
    }

    /**
     * @see javax.servlet.ServletRequestWrapper#getCharacterEncoding()
     */
    @Override
    public String getCharacterEncoding() {

        return m_characterEncoding;
    }

    /**
     * @see javax.servlet.http.HttpServletRequestWrapper#getContextPath()
     */
    @Override
    public String getContextPath() {

        return m_contextPath;
    }

    /**
     * @see javax.servlet.http.HttpServletRequestWrapper#getHeader(java.lang.String)
     */
    @Override
    public String getHeader(String name) {

        return m_headers.get(name.toLowerCase());
    }

    /**
     * @see javax.servlet.http.HttpServletRequestWrapper#getHeaderNames()
     */
    @Override
    public Enumeration<String> getHeaderNames() {

        return Collections.enumeration(m_headers.keySet());
    }

    /**
     * @see javax.servlet.http.HttpServletRequestWrapper#getHeaders(java.lang.String)
     */
    @Override
    public Enumeration<String> getHeaders(String name) {

        String value = m_headers.get(name.toLowerCase());
        return value == null
        ? Collections.emptyEnumeration()
        : Collections.enumeration(Collections.singletonList(value));
    }

    /**
     * @see javax.servlet.ServletRequestWrapper#getInputStream()
     */
    @Override
    public ServletInputStream getInputStream() {

        final ByteArrayInputStream in = new ByteArrayInputStream(m_body);
        return new ServletInputStream() {

            @Override
            public boolean isFinished() {

                return in.available() == 0;
            }

            @Override
            public boolean isReady() {

                return true;
            }

            @Override
            public int read() {

                return in.read();
            }

            @Override
            public void setReadListener(ReadListener readListener) {

                throw new UnsupportedOperationException("setReadListener");
            }
        };
    }

    /**
     * @see javax.servlet.http.HttpServletRequestWrapper#getMethod()
     */
    @Override
    public String getMethod() {

        return m_method;
    }

    /**
     * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
     */
    @Override
    public String getParameter(String name) {

        String[] values = m_parameters.get(name);
        return ((values == null) || (values.length == 0)) ? null : values[0];
    }

    /**
     * @see javax.servlet.ServletRequestWrapper#getParameterMap()
     */
    @Override
    public Map<String, String[]> getParameterMap() {

        return Collections.unmodifiableMap(m_parameters);
    }

    /**
     * @see javax.servlet.ServletRequestWrapper#getParameterNames()
     */
    @Override
    public Enumeration<String> getParameterNames() {

        return Collections.enumeration(m_parameters.keySet());
    }

    /**
     * @see javax.servlet.ServletRequestWrapper#getParameterValues(java.lang.String)
     */
    @Override
    public String[] getParameterValues(String name) {

        return m_parameters.get(name);
    }

    /**
     * @see javax.servlet.http.HttpServletRequestWrapper#getPathInfo()
     */
    @Override
    public String getPathInfo() {

        return m_pathInfo;
    }

    /**
     * @see javax.servlet.ServletRequestWrapper#getReader()
     */
    @Override
    public BufferedReader getReader() {

        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(m_body), StandardCharsets.UTF_8));
    }

    /**
     * @see javax.servlet.ServletRequestWrapper#getRemoteAddr()
     */
    @Override
    public String getRemoteAddr() {

        return m_remoteAddr;
    }

    /**
     * @see javax.servlet.http.HttpServletRequestWrapper#getRequestURI()
     */
    @Override
    public String getRequestURI() {

        return m_requestUri != null ? m_requestUri : (m_contextPath + m_pathInfo);
    }

    /**
     * @see javax.servlet.http.HttpServletRequestWrapper#getRequestURL()
     */
    @Override
    public StringBuffer getRequestURL() {

        StringBuffer result = new StringBuffer();
        result.append(m_scheme).append("://").append(m_serverName);
        if (m_serverPort > 0) {
            result.append(":").append(m_serverPort);
        }
        result.append(getRequestURI());
        return result;
    }

    /**
     * @see javax.servlet.ServletRequestWrapper#getScheme()
     */
    @Override
    public String getScheme() {

        return m_scheme;
    }

    /**
     * @see javax.servlet.ServletRequestWrapper#getServerName()
     */
    @Override
    public String getServerName() {

        return m_serverName;
    }

    /**
     * @see javax.servlet.ServletRequestWrapper#getServerPort()
     */
    @Override
    public int getServerPort() {

        return m_serverPort;
    }

    /**
     * @see javax.servlet.http.HttpServletRequestWrapper#getServletPath()
     */
    @Override
    public String getServletPath() {

        // an MCP request handler request is not served by a named servlet path
        return "";
    }

    /**
     * @see javax.servlet.http.HttpServletRequestWrapper#getSession()
     */
    @Override
    public HttpSession getSession() {

        return getSession(true);
    }

    /**
     * @see javax.servlet.http.HttpServletRequestWrapper#getSession(boolean)
     */
    @Override
    public HttpSession getSession(boolean create) {

        if ((m_session != null) && m_session.isValid()) {
            m_session.markNotNew();
            return m_session;
        }
        if (create) {
            m_session = new CmsMockHttpSession();
            return m_session;
        }
        return null;
    }

    /**
     * @see javax.servlet.ServletRequestWrapper#removeAttribute(java.lang.String)
     */
    @Override
    public void removeAttribute(String name) {

        m_attributes.remove(name);
    }

    /**
     * @see javax.servlet.ServletRequestWrapper#setAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    public void setAttribute(String name, Object value) {

        if (value == null) {
            m_attributes.remove(name);
        } else {
            m_attributes.put(name, value);
        }
    }

    /**
     * @see javax.servlet.ServletRequestWrapper#setCharacterEncoding(java.lang.String)
     */
    @Override
    public void setCharacterEncoding(String encoding) {

        m_characterEncoding = encoding;
    }

    /**
     * Sets a request header (case-insensitive name).<p>
     *
     * @param name the header name
     * @param value the header value
     *
     * @return this request, for chaining
     */
    public CmsMockHttpServletRequest withHeader(String name, String value) {

        m_headers.put(name.toLowerCase(), value);
        return this;
    }

    /**
     * Sets the server host, used to match a site.<p>
     *
     * @param scheme the scheme, e.g. "http"
     * @param serverName the server name, e.g. "localhost"
     * @param serverPort the server port, e.g. 8080
     *
     * @return this request, for chaining
     */
    public CmsMockHttpServletRequest withHost(String scheme, String serverName, int serverPort) {

        m_scheme = scheme;
        m_serverName = serverName;
        m_serverPort = serverPort;
        return this;
    }

    /**
     * Sets the request body from the given JSON (or any) string, encoded as UTF-8.<p>
     *
     * @param body the request body
     *
     * @return this request, for chaining
     */
    public CmsMockHttpServletRequest withJsonBody(String body) {

        m_body = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
        return this;
    }

    /**
     * Sets the HTTP method.<p>
     *
     * @param method the HTTP method, e.g. "GET" or "POST"
     *
     * @return this request, for chaining
     */
    public CmsMockHttpServletRequest withMethod(String method) {

        m_method = method;
        return this;
    }

    /**
     * Sets a request parameter.<p>
     *
     * @param name the parameter name
     * @param values the parameter values
     *
     * @return this request, for chaining
     */
    public CmsMockHttpServletRequest withParameter(String name, String... values) {

        m_parameters.put(name, values);
        return this;
    }

    /**
     * Sets the path info, i.e. the request handler endpoint such as
     * <code>/handleAgent/v1/session/login</code>; the request URI is derived from it unless set
     * explicitly.<p>
     *
     * @param pathInfo the path info
     *
     * @return this request, for chaining
     */
    public CmsMockHttpServletRequest withPath(String pathInfo) {

        m_pathInfo = pathInfo;
        return this;
    }

    /**
     * Sets the remote address, which also seeds the OpenCms client token.<p>
     *
     * @param remoteAddr the remote address
     *
     * @return this request, for chaining
     */
    public CmsMockHttpServletRequest withRemoteAddr(String remoteAddr) {

        m_remoteAddr = remoteAddr;
        return this;
    }

    /**
     * Sets the request URI explicitly; by default it is derived from the context path and path
     * info.<p>
     *
     * @param requestUri the request URI
     *
     * @return this request, for chaining
     */
    public CmsMockHttpServletRequest withRequestUri(String requestUri) {

        m_requestUri = requestUri;
        return this;
    }
}

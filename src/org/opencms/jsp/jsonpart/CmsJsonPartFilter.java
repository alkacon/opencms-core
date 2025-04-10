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
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp.jsonpart;

import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * This servlet filter post-processes the response output for requests with the parameter '__json=true'.<p>
 *
 * It converts the encoded JSON parts generated by the &lt;cms:jsonpart&gt; tag, converts them to JSON, writes them to the response,
 * and throws everything else away.
 */
public class CmsJsonPartFilter implements Filter {

    /**
     * Request wrapper used to disable direct edit functionality.<p>
     */
    class RequestWrapper extends HttpServletRequestWrapper {

        /**
         * Creates a new instance.<p>
         *
         * @param request the wrapped request
         */
        public RequestWrapper(HttpServletRequest request) {
            super(request);
        }

        /**
         * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
         */
        @Override
        public String getParameter(String name) {

            if (CmsGwtConstants.PARAM_DISABLE_DIRECT_EDIT.equals(name)) {
                return Boolean.TRUE.toString();
            } else {
                return super.getParameter(name);
            }
        }

        /**
         * @see javax.servlet.ServletRequestWrapper#getParameterMap()
         */
        @Override
        public Map<String, String[]> getParameterMap() {

            Map<String, String[]> result = Maps.newHashMap(super.getParameterMap());
            result.put(CmsGwtConstants.PARAM_DISABLE_DIRECT_EDIT, new String[] {"true"});
            return result;
        }

        /**
         * @see javax.servlet.ServletRequestWrapper#getParameterNames()
         */
        @Override
        public Enumeration<String> getParameterNames() {

            Set<String> keys = Sets.newHashSet();
            keys.add(CmsGwtConstants.PARAM_DISABLE_DIRECT_EDIT);
            return new Vector<String>(keys).elements();
        }

        /**
         * @see javax.servlet.ServletRequestWrapper#getParameterValues(java.lang.String)
         */
        @Override
        public String[] getParameterValues(String name) {

            return super.getParameterValues(name);
        }
    }

    /**
     * A response wrapper used to capture output so we can post-process it.<p>
     */
    class ResponseWrapper extends HttpServletResponseWrapper {

        /** The stream used to collect the output bytes. */
        ByteArrayOutputStream m_byteStream = new java.io.ByteArrayOutputStream();

        /** A writer used to collect string-based output. */
        PrintWriter m_printWriter;

        /**
         * Creates a new wrapper instance for the given response.<p>
         *
         * @param response the original response
         */
        public ResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        /**
         * Gets the bytes written so far.<p>
         *
         * @return the bytes written
         */
        public byte[] getBytes() {

            if (m_printWriter != null) {
                m_printWriter.flush();
            }
            return m_byteStream.toByteArray();
        }

        /**
         * @see javax.servlet.ServletResponseWrapper#getOutputStream()
         */
        @Override
        public ServletOutputStream getOutputStream() {

            return new ServletOutputStream() {

                /**
                 * @see java.io.OutputStream#write(byte[])
                 */
                @Override
                public void write(byte[] b) throws IOException {

                    m_byteStream.write(b);
                }

                /**
                 * @see java.io.OutputStream#write(byte[], int, int)
                 */
                @Override
                public void write(byte[] b, int off, int len) {

                    m_byteStream.write(b, off, len);
                }

                /**
                 * @see java.io.OutputStream#write(int)
                 */
                @Override
                public void write(int b) {

                    m_byteStream.write(b);
                }

                /**
                 * @see javax.servlet.ServletOutputStream#isReady()
                 */
                @Override
                public boolean isReady() {

                    return null != m_byteStream;
                }

                /**
                 * @see javax.servlet.ServletOutputStream#setWriteListener(javax.servlet.WriteListener)
                 */
                @Override
                public void setWriteListener(WriteListener writeListener) {
                }
            };
        }

        /**
         * @see javax.servlet.ServletResponseWrapper#getWriter()
         */
        @Override
        public PrintWriter getWriter() throws IOException {

            if (m_printWriter == null) {
                m_printWriter = new PrintWriter(
                    new OutputStreamWriter(m_byteStream, getResponse().getCharacterEncoding()));
            }
            return m_printWriter;
        }

        /**
         * This method does nothing, we want to ignore calls to setContentLength because we want to postprocess
         * the output, resulting in a different length.
         *
         * @see javax.servlet.ServletResponseWrapper#setContentLength(int)
         */
        @Override
        public void setContentLength(int len) {
            // ignore
        }
    }

    /** The static log object for this class. */
    static final Log LOG = CmsLog.getLog(CmsJsonPartFilter.class);

    /** JSON key for the list of part keys. */
    public static final String KEY_PARTS = "parts";

    /** Name of the parameter used to enable JSON rendering. */
    public static final String PARAM_JSON = "__json";

    /** ThreadLocal used to detect nested calls. */
    private ThreadLocal<Boolean> m_isNested = new ThreadLocal<Boolean>();

    /**
     * Detects whether the filter needs to be used for the given request.<p>
     *
     * @param request the request
     * @return true if the filter should be used for the request
     */
    public static boolean isJsonRequest(ServletRequest request) {

        HttpServletRequest sr = (HttpServletRequest)request;
        return (sr.getQueryString() != null) && (sr.getQueryString().contains("__json=true"));
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        // do nothing
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {

        if (isJsonRequest(request)) {
            if (m_isNested.get() == null) {
                try {
                    m_isNested.set(Boolean.TRUE);
                    RequestWrapper reqWrapper = new RequestWrapper((HttpServletRequest)request);
                    ResponseWrapper resWrapper = new ResponseWrapper((HttpServletResponse)response);
                    chain.doFilter(reqWrapper, resWrapper);
                    byte[] data = resWrapper.getBytes();
                    String content = new String(data, resWrapper.getCharacterEncoding());
                    String transformedContent = transformContent(content);
                    byte[] transformedData = transformedContent.getBytes("UTF-8");
                    response.setContentType("application/json; charset=UTF-8");
                    response.setContentLength(transformedData.length);
                    response.getOutputStream().write(transformedData);
                    response.getOutputStream().flush();
                } finally {
                    m_isNested.set(null);
                }
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) {
        // do nothing
    }

    /**
     * Transforms the response content as a string.<p>
     *
     * @param content the content
     * @return the transformed content
     */
    private String transformContent(String content) {

        try {
            List<CmsJsonPart> parts = CmsJsonPart.parseJsonParts(content);
            JSONArray keys = new JSONArray();
            JSONObject output = new JSONObject();
            for (CmsJsonPart part : parts) {
                if (output.has(part.getKey())) {
                    LOG.warn("Duplicate key for JSON parts: " + part.getKey());
                }
                keys.put(part.getKey());
                output.put(part.getKey(), part.getValue());
            }
            output.put(KEY_PARTS, keys);
            return output.toString();
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return content;

        }
    }
}

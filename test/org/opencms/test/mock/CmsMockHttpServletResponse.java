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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Functional {@link HttpServletResponse} mock for OpenCms tests that captures the status, headers and
 * the written body, so a request handler response can be asserted without a servlet container.<p>
 *
 * Only the response methods that the OpenCms request handlers use are implemented; everything else is
 * delegated to a throwing proxy via {@link HttpServletResponseWrapper}, so an unexpected dependency
 * fails fast with an {@link UnsupportedOperationException}.<p>
 *
 * @see CmsMockHttpServletRequest
 */
public class CmsMockHttpServletResponse extends HttpServletResponseWrapper {

    /** The captured response body. */
    private final ByteArrayOutputStream m_buffer = new ByteArrayOutputStream();

    /** The character encoding. */
    private String m_characterEncoding = "UTF-8";

    /** The content type. */
    private String m_contentType;

    /** The response headers. */
    private final Map<String, String> m_headers = new LinkedHashMap<String, String>();

    /** The error message of a {@link #sendError(int, String)} call. */
    private String m_errorMessage;

    /** The status code. */
    private int m_status = HttpServletResponse.SC_OK;

    /** The writer over the captured body, lazily created. */
    private PrintWriter m_writer;

    /**
     * Creates a new mock response, delegating all not explicitly implemented methods to a throwing
     * proxy.<p>
     */
    public CmsMockHttpServletResponse() {

        super(throwingResponse());
    }

    /**
     * Creates an {@link HttpServletResponse} proxy that throws {@link UnsupportedOperationException}
     * for every call, used as the delegate for the not implemented wrapper methods.<p>
     *
     * @return the throwing response
     */
    private static HttpServletResponse throwingResponse() {

        return (HttpServletResponse)Proxy.newProxyInstance(
            CmsMockHttpServletResponse.class.getClassLoader(),
            new Class<?>[] {HttpServletResponse.class},
            (proxy, method, args) -> {
                throw new UnsupportedOperationException("HttpServletResponse." + method.getName());
            });
    }

    /**
     * @see javax.servlet.http.HttpServletResponseWrapper#addHeader(java.lang.String, java.lang.String)
     */
    @Override
    public void addHeader(String name, String value) {

        m_headers.put(name, value);
    }

    /**
     * @see javax.servlet.ServletResponseWrapper#flushBuffer()
     */
    @Override
    public void flushBuffer() {

        if (m_writer != null) {
            m_writer.flush();
        }
    }

    /**
     * @see javax.servlet.ServletResponseWrapper#getCharacterEncoding()
     */
    @Override
    public String getCharacterEncoding() {

        return m_characterEncoding;
    }

    /**
     * Returns the captured response body as a string, decoded with the response character encoding.<p>
     *
     * @return the captured response body
     */
    public String getContentAsString() {

        flushBuffer();
        try {
            return m_buffer.toString(m_characterEncoding);
        } catch (UnsupportedEncodingException e) {
            return new String(m_buffer.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    /**
     * @see javax.servlet.ServletResponseWrapper#getContentType()
     */
    @Override
    public String getContentType() {

        return m_contentType;
    }

    /**
     * Returns the error message passed to {@link #sendError(int, String)}, or <code>null</code>.<p>
     *
     * @return the error message
     */
    public String getErrorMessage() {

        return m_errorMessage;
    }

    /**
     * @see javax.servlet.http.HttpServletResponseWrapper#getHeader(java.lang.String)
     */
    @Override
    public String getHeader(String name) {

        return m_headers.get(name);
    }

    /**
     * @see javax.servlet.ServletResponseWrapper#getOutputStream()
     */
    @Override
    public ServletOutputStream getOutputStream() {

        return new ServletOutputStream() {

            @Override
            public boolean isReady() {

                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

                throw new UnsupportedOperationException("setWriteListener");
            }

            @Override
            public void write(int b) {

                m_buffer.write(b);
            }
        };
    }

    /**
     * @see javax.servlet.http.HttpServletResponseWrapper#getStatus()
     */
    @Override
    public int getStatus() {

        return m_status;
    }

    /**
     * @see javax.servlet.ServletResponseWrapper#getWriter()
     */
    @Override
    public PrintWriter getWriter() {

        if (m_writer == null) {
            m_writer = new PrintWriter(new java.io.OutputStreamWriter(m_buffer, StandardCharsets.UTF_8), true);
        }
        return m_writer;
    }

    /**
     * @see javax.servlet.http.HttpServletResponseWrapper#sendError(int)
     */
    @Override
    public void sendError(int sc) {

        m_status = sc;
    }

    /**
     * @see javax.servlet.http.HttpServletResponseWrapper#sendError(int, java.lang.String)
     */
    @Override
    public void sendError(int sc, String msg) {

        m_status = sc;
        m_errorMessage = msg;
    }

    /**
     * @see javax.servlet.ServletResponseWrapper#setCharacterEncoding(java.lang.String)
     */
    @Override
    public void setCharacterEncoding(String charset) {

        m_characterEncoding = charset;
    }

    /**
     * @see javax.servlet.ServletResponseWrapper#setContentType(java.lang.String)
     */
    @Override
    public void setContentType(String type) {

        m_contentType = type;
    }

    /**
     * @see javax.servlet.http.HttpServletResponseWrapper#setHeader(java.lang.String, java.lang.String)
     */
    @Override
    public void setHeader(String name, String value) {

        m_headers.put(name, value);
    }

    /**
     * @see javax.servlet.http.HttpServletResponseWrapper#setStatus(int)
     */
    @Override
    public void setStatus(int sc) {

        m_status = sc;
    }
}

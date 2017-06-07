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

package org.opencms.test;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Very incomplete implementation of <code>HttpServletResponse</code> for testing.<p>
 *
 */
public class OpenCmsTestServletResponse implements HttpServletResponse {

    /**
     * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
     */
    public void addCookie(Cookie arg0) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
     */
    public void addDateHeader(String arg0, long arg1) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
     */
    public void addHeader(String arg0, String arg1) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
     */
    public void addIntHeader(String arg0, int arg1) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
     */
    public boolean containsHeader(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
     *
     * @deprecated deprecated in Java standard, but still required to implement
     */
    @Deprecated
    public String encodeRedirectUrl(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
     */
    public String encodeRedirectURL(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
     *
     * @deprecated deprecated in Java standard, but still required to implement
     */
    @Deprecated
    public String encodeUrl(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
     */
    public String encodeURL(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.ServletResponse#flushBuffer()
     */
    public void flushBuffer() {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.ServletResponse#getBufferSize()
     */
    public int getBufferSize() {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.ServletResponse#getCharacterEncoding()
     */
    public String getCharacterEncoding() {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.ServletResponse#getContentType()
     */
    public String getContentType() {

        throw new RuntimeException("Not implemented");
    }

    public String getHeader(String name) {

        throw new RuntimeException("Not implemented");
    }

    public Collection<String> getHeaderNames() {

        throw new RuntimeException("Not implemented");
    }

    public Collection<String> getHeaders(String name) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.ServletResponse#getLocale()
     */
    public Locale getLocale() {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.ServletResponse#getOutputStream()
     */
    public ServletOutputStream getOutputStream() {

        throw new RuntimeException("Not implemented");
    }

    public int getStatus() {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.ServletResponse#getWriter()
     */
    public PrintWriter getWriter() {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.ServletResponse#isCommitted()
     */
    public boolean isCommitted() {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.ServletResponse#reset()
     */
    public void reset() {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.ServletResponse#resetBuffer()
     */
    public void resetBuffer() {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#sendError(int)
     */
    public void sendError(int arg0) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
     */
    public void sendError(int arg0, String arg1) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
     */
    public void sendRedirect(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.ServletResponse#setBufferSize(int)
     */
    public void setBufferSize(int arg0) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
     */
    public void setCharacterEncoding(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.ServletResponse#setContentLength(int)
     */
    public void setContentLength(int arg0) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
     */
    public void setContentType(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
     */
    public void setDateHeader(String arg0, long arg1) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
     */
    public void setHeader(String arg0, String arg1) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
     */
    public void setIntHeader(String arg0, int arg1) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
     */
    public void setLocale(Locale arg0) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#setStatus(int)
     */
    public void setStatus(int arg0) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
     *
     * @deprecated deprecated in Java standard, but still required to implement
     */
    @Deprecated
    public void setStatus(int arg0, String arg1) {

        throw new RuntimeException("Not implemented");
    }
}
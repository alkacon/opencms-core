/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/test/OpenCmsTestServletResponse.java,v $
 * Date   : $Date: 2006/11/03 09:42:26 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Very incomple implementation of <code>HttpServletResponse</code> for testing.<p>
 * 
 * @author Alexander Kandzior 
 * @version $Revision: 1.1.2.1 $
 */
public class OpenCmsTestServletResponse implements HttpServletResponse {

    public void addCookie(Cookie arg0) {

        throw new RuntimeException("Not implemented");
    }

    public void addDateHeader(String arg0, long arg1) {

        throw new RuntimeException("Not implemented");
    }

    public void addHeader(String arg0, String arg1) {

        throw new RuntimeException("Not implemented");
    }

    public void addIntHeader(String arg0, int arg1) {

        throw new RuntimeException("Not implemented");
    }

    public boolean containsHeader(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
     * @deprecated
     */
    public String encodeRedirectUrl(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    public String encodeRedirectURL(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
     * @deprecated
     */
    public String encodeUrl(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    public String encodeURL(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    public void flushBuffer() {

        throw new RuntimeException("Not implemented");
    }

    public int getBufferSize() {

        throw new RuntimeException("Not implemented");
    }

    public String getCharacterEncoding() {

        throw new RuntimeException("Not implemented");
    }

    public Locale getLocale() {

        throw new RuntimeException("Not implemented");
    }

    public ServletOutputStream getOutputStream() {

        throw new RuntimeException("Not implemented");
    }

    public PrintWriter getWriter() {

        throw new RuntimeException("Not implemented");
    }

    public boolean isCommitted() {

        throw new RuntimeException("Not implemented");
    }

    public void reset() {

        throw new RuntimeException("Not implemented");
    }

    public void resetBuffer() {

        throw new RuntimeException("Not implemented");
    }

    public void sendError(int arg0, String arg1) {

        throw new RuntimeException("Not implemented");
    }

    public void sendError(int arg0) {

        throw new RuntimeException("Not implemented");
    }

    public void sendRedirect(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    public void setBufferSize(int arg0) {

        throw new RuntimeException("Not implemented");
    }

    public void setContentLength(int arg0) {

        throw new RuntimeException("Not implemented");
    }

    public void setContentType(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    public void setDateHeader(String arg0, long arg1) {

        throw new RuntimeException("Not implemented");
    }

    public void setHeader(String arg0, String arg1) {

        throw new RuntimeException("Not implemented");
    }

    public void setIntHeader(String arg0, int arg1) {

        throw new RuntimeException("Not implemented");
    }

    public void setLocale(Locale arg0) {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
     * @deprecated
     */
    public void setStatus(int arg0, String arg1) {

        throw new RuntimeException("Not implemented");
    }

    public void setStatus(int arg0) {

        throw new RuntimeException("Not implemented");
    }  
}
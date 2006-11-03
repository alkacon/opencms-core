/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/test/OpenCmsTestServletRequest.java,v $
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

import java.io.BufferedReader;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Very incomplete implementation of <code>HttpServletRequest</code> for testing.<p>
 * 
 * @author Alexander Kandzior 
 * @version $Revision: 1.1.2.1 $
 */
public class OpenCmsTestServletRequest implements HttpServletRequest {
    
    /**
     * Constructor for test implementation.<p>
     */
    public OpenCmsTestServletRequest() {
        // noop
    }    

    public Object getAttribute(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    public Enumeration getAttributeNames() {

        throw new RuntimeException("Not implemented");
    }

    public String getAuthType() {

        throw new RuntimeException("Not implemented");
    }

    public String getCharacterEncoding() {

        throw new RuntimeException("Not implemented");
    }

    public int getContentLength() {

        throw new RuntimeException("Not implemented");
    }

    public String getContentType() {

        throw new RuntimeException("Not implemented");
    }

    public String getContextPath() {

        throw new RuntimeException("Not implemented");
    }

    public Cookie[] getCookies() {

        throw new RuntimeException("Not implemented");
    }

    public long getDateHeader(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    public String getHeader(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    public Enumeration getHeaderNames() {

        throw new RuntimeException("Not implemented");
    }

    public Enumeration getHeaders(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    public ServletInputStream getInputStream() {

        throw new RuntimeException("Not implemented");
    }

    public int getIntHeader(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    public Locale getLocale() {

        throw new RuntimeException("Not implemented");
    }

    public Enumeration getLocales() {

        throw new RuntimeException("Not implemented");
    }

    public String getMethod() {

        throw new RuntimeException("Not implemented");
    }

    public String getParameter(String arg0) {

        // no parameter available
        return null;
        // throw new RuntimeException("Not implemented");
    }

    public Map getParameterMap() {

        throw new RuntimeException("Not implemented");
    }

    public Enumeration getParameterNames() {

        throw new RuntimeException("Not implemented");
    }

    public String[] getParameterValues(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    public String getPathInfo() {

        throw new RuntimeException("Not implemented");
    }

    public String getPathTranslated() {

        throw new RuntimeException("Not implemented");
    }

    public String getProtocol() {

        throw new RuntimeException("Not implemented");
    }

    public String getQueryString() {

        throw new RuntimeException("Not implemented");
    }

    public BufferedReader getReader() {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
     * @deprecated 
     */
    public String getRealPath(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    public String getRemoteAddr() {

        throw new RuntimeException("Not implemented");
    }

    public String getRemoteHost() {

        throw new RuntimeException("Not implemented");
    }

    public String getRemoteUser() {

        throw new RuntimeException("Not implemented");
    }

    public RequestDispatcher getRequestDispatcher(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    public String getRequestedSessionId() {

        throw new RuntimeException("Not implemented");
    }

    public String getRequestURI() {

        throw new RuntimeException("Not implemented");
    }

    public StringBuffer getRequestURL() {

        throw new RuntimeException("Not implemented");
    }

    public String getScheme() {

        throw new RuntimeException("Not implemented");
    }

    public String getServerName() {

        throw new RuntimeException("Not implemented");
    }

    public int getServerPort() {

        throw new RuntimeException("Not implemented");
    }

    public String getServletPath() {

        throw new RuntimeException("Not implemented");
    }

    public HttpSession getSession() {

        throw new RuntimeException("Not implemented");
    }

    public HttpSession getSession(boolean arg0) {

        throw new RuntimeException("Not implemented");
    }

    public Principal getUserPrincipal() {

        throw new RuntimeException("Not implemented");
    }

    public boolean isRequestedSessionIdFromCookie() {

        throw new RuntimeException("Not implemented");
    }

    /**
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
     * @deprecated
     */
    public boolean isRequestedSessionIdFromUrl() {

        throw new RuntimeException("Not implemented");
    }

    public boolean isRequestedSessionIdFromURL() {

        throw new RuntimeException("Not implemented");
    }

    public boolean isRequestedSessionIdValid() {

        throw new RuntimeException("Not implemented");
    }

    public boolean isSecure() {

        throw new RuntimeException("Not implemented");
    }

    public boolean isUserInRole(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    public void removeAttribute(String arg0) {

        throw new RuntimeException("Not implemented");
    }

    public void setAttribute(String arg0, Object arg1) {

        throw new RuntimeException("Not implemented");
    }

    public void setCharacterEncoding(String arg0) {

        throw new RuntimeException("Not implemented");
    }     
}
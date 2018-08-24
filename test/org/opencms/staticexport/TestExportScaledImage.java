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

package org.opencms.staticexport;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.loader.CmsImageScaler;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsFileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @since 6.0.0
 */
public class TestExportScaledImage extends OpenCmsTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestExportScaledImage(String arg0) {

        super(arg0);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestExportScaledImage.class.getName());

        suite.addTest(new TestExportScaledImage("testExportScaledImage"));

        TestSetup wrapper = new TestSetup(suite) {

            @Override
            protected void setUp() {

                setupOpenCms("simpletest", "/", "../org/opencms/staticexport/");
            }

            @Override
            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /**
     * Tests the file export.<p>
     *
     * @throws Throwable if something goes wrong
     */
    public void testExportScaledImage() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing export of scaled image");

        // set the export mode to export immediately after publishing resources
        // OpenCms.getStaticExportManager().setHandler("org.opencms.staticexport.CmsAfterPublishStaticExportHandler");
        String resourcename = "/folder1/image1.gif";
        String scaleParams = "cx:5,cy:5,ch:10,cw:10,t:0,h:40,w:40,transparent";

        CmsFile imageFile = cms.readFile(resourcename);
        // now read the exported file in the file system and check its content
        String rootPath = cms.getRequestContext().addSiteRoot(resourcename);
        String exportPath = CmsFileUtil.normalizePath(
            OpenCms.getStaticExportManager().getExportPath(rootPath) + rootPath);

        CmsStaticExportData data = new CmsStaticExportData(
            rootPath,
            rootPath,
            imageFile,
            CmsImageScaler.PARAM_SCALE + "=" + scaleParams);
        CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
        // Request and response are provided only with information needed to get scaling running at all.
        OpenCms.getStaticExportManager().export(new HttpServletRequest() {

            public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {

                // Returning default, since the method is never called in the test case.
                return false;
            }

            public String changeSessionId() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public AsyncContext getAsyncContext() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public Object getAttribute(String name) {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public Enumeration<String> getAttributeNames() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String getAuthType() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String getCharacterEncoding() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public int getContentLength() {

                // Returning default, since the method is never called in the test case.
                return 0;
            }

            public long getContentLengthLong() {

                // Returning default, since the method is never called in the test case.
                return 0;
            }

            public String getContentType() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String getContextPath() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public Cookie[] getCookies() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public long getDateHeader(String name) {

                // Returning default, since the method is never called in the test case.
                return 0;
            }

            public DispatcherType getDispatcherType() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String getHeader(String name) {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public Enumeration<String> getHeaderNames() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public Enumeration<String> getHeaders(String name) {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public ServletInputStream getInputStream() throws IOException {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public int getIntHeader(String name) {

                // Returning default, since the method is never called in the test case.
                return 0;
            }

            public String getLocalAddr() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public Locale getLocale() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public Enumeration<Locale> getLocales() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String getLocalName() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public int getLocalPort() {

                // Returning default, since the method is never called in the test case.
                return 0;
            }

            public String getMethod() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String getParameter(String name) {

                if (Objects.equals(CmsImageScaler.PARAM_SCALE, name)) {
                    return scaleParams;
                }
                return null;
            }

            public Map<String, String[]> getParameterMap() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public Enumeration<String> getParameterNames() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String[] getParameterValues(String name) {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public Part getPart(String name) throws IOException, ServletException {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public Collection<Part> getParts() throws IOException, ServletException {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String getPathInfo() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String getPathTranslated() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String getProtocol() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String getQueryString() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public BufferedReader getReader() throws IOException {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String getRealPath(String path) {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String getRemoteAddr() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String getRemoteHost() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public int getRemotePort() {

                // Returning default, since the method is never called in the test case.
                return 0;
            }

            public String getRemoteUser() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public RequestDispatcher getRequestDispatcher(String path) {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String getRequestedSessionId() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String getRequestURI() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public StringBuffer getRequestURL() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String getScheme() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String getServerName() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public int getServerPort() {

                // Returning default, since the method is never called in the test case.
                return 0;
            }

            public ServletContext getServletContext() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String getServletPath() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public HttpSession getSession() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public HttpSession getSession(boolean create) {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public Principal getUserPrincipal() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public boolean isAsyncStarted() {

                // Returning default, since the method is never called in the test case.
                return false;
            }

            public boolean isAsyncSupported() {

                // Returning default, since the method is never called in the test case.
                return false;
            }

            public boolean isRequestedSessionIdFromCookie() {

                // Returning default, since the method is never called in the test case.
                return false;
            }

            public boolean isRequestedSessionIdFromUrl() {

                // Returning default, since the method is never called in the test case.
                return false;
            }

            public boolean isRequestedSessionIdFromURL() {

                // Returning default, since the method is never called in the test case.
                return false;
            }

            public boolean isRequestedSessionIdValid() {

                // Returning default, since the method is never called in the test case.
                return false;
            }

            public boolean isSecure() {

                // Returning default, since the method is never called in the test case.
                return false;
            }

            public boolean isUserInRole(String role) {

                // Returning default, since the method is never called in the test case.
                return false;
            }

            public void login(String username, String password) throws ServletException {

                // Returning default, since the method is never called in the test case.

            }

            public void logout() throws ServletException {

                // Returning default, since the method is never called in the test case.

            }

            public void removeAttribute(String name) {

                // Returning default, since the method is never called in the test case.

            }

            public void setAttribute(String name, Object o) {

                // Returning default, since the method is never called in the test case.

            }

            public void setCharacterEncoding(String env) throws UnsupportedEncodingException {

                // Returning default, since the method is never called in the test case.

            }

            public AsyncContext startAsync() throws IllegalStateException {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
            throws IllegalStateException {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass)
            throws IOException, ServletException {

                // Returning default, since the method is never called in the test case.
                return null;
            }
        }, new HttpServletResponse() {

            public void addCookie(Cookie cookie) {

                // Returning default, since the method is never called in the test case.

            }

            public void addDateHeader(String name, long date) {

                // Returning default, since the method is never called in the test case.

            }

            public void addHeader(String name, String value) {

                // Returning default, since the method is never called in the test case.

            }

            public void addIntHeader(String name, int value) {

                // Returning default, since the method is never called in the test case.

            }

            public boolean containsHeader(String name) {

                // Returning default, since the method is never called in the test case.
                return false;
            }

            public String encodeRedirectUrl(String url) {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String encodeRedirectURL(String url) {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String encodeUrl(String url) {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String encodeURL(String url) {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public void flushBuffer() throws IOException {

                // Returning default, since the method is never called in the test case.

            }

            public int getBufferSize() {

                // Returning default, since the method is never called in the test case.
                return 0;
            }

            public String getCharacterEncoding() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String getContentType() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public String getHeader(String name) {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public Collection<String> getHeaderNames() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public Collection<String> getHeaders(String name) {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public Locale getLocale() {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public ServletOutputStream getOutputStream() throws IOException {

                // Returning dummy output stream, since we do not want to actually write somewhere in the output stream
                return new ServletOutputStream() {

                    @Override
                    public boolean isReady() {

                        // Returning default, since the method is never called in the test case.
                        return false;
                    }

                    @Override
                    public void setWriteListener(WriteListener writeListener) {

                        // Returning default, since the method is never called in the test case.

                    }

                    @Override
                    public void write(int b) throws IOException {

                        // Returning default, since the method is never called in the test case.

                    }
                };
            }

            public int getStatus() {

                // Returning default, since the method is never called in the test case.
                return 0;
            }

            public PrintWriter getWriter() throws IOException {

                // Returning default, since the method is never called in the test case.
                return null;
            }

            public boolean isCommitted() {

                // Returning default, since the method is never called in the test case.
                return false;
            }

            public void reset() {

                // Returning default, since the method is never called in the test case.

            }

            public void resetBuffer() {

                // Returning default, since the method is never called in the test case.

            }

            public void sendError(int sc) throws IOException {

                // Returning default, since the method is never called in the test case.

            }

            public void sendError(int sc, String msg) throws IOException {

                // Returning default, since the method is never called in the test case.

            }

            public void sendRedirect(String location) throws IOException {

                // Returning default, since the method is never called in the test case.

            }

            public void setBufferSize(int size) {

                // Returning default, since the method is never called in the test case.

            }

            public void setCharacterEncoding(String charset) {

                // Returning default, since the method is never called in the test case.

            }

            public void setContentLength(int len) {

                // Returning default, since the method is never called in the test case.

            }

            public void setContentLengthLong(long len) {

                // Returning default, since the method is never called in the test case.

            }

            public void setContentType(String type) {

                // Returning default, since the method is never called in the test case.

            }

            public void setDateHeader(String name, long date) {

                // Returning default, since the method is never called in the test case.

            }

            public void setHeader(String name, String value) {

                // Returning default, since the method is never called in the test case.

            }

            public void setIntHeader(String name, int value) {

                // Returning default, since the method is never called in the test case.

            }

            public void setLocale(Locale loc) {

                // Returning default, since the method is never called in the test case.

            }

            public void setStatus(int sc) {

                // Returning default, since the method is never called in the test case.

            }

            public void setStatus(int sc, String sm) {

                // Returning default, since the method is never called in the test case.

            }
        }, exportCms, data);

        File f = new File(exportPath);
        assertTrue(f.exists());

        byte[] expectedContent = (new CmsImageScaler(scaleParams)).scaleImage(cms.readFile(resourcename));
        // check the exported content
        byte[] exportContent = new byte[(int)f.length()];
        FileInputStream fileStream = new FileInputStream(f);
        fileStream.read(exportContent);
        fileStream.close();
        assertEquals(Arrays.toString(expectedContent), Arrays.toString(exportContent));
    }
}
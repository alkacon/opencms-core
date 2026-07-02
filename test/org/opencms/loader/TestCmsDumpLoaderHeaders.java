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

package org.opencms.loader;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestRunner;
import org.opencms.util.CmsRequestUtil;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Tests response headers set by the dump loader.<p>
 */
public class TestCmsDumpLoaderHeaders extends OpenCmsTestRunner {

    /**
     * Simple response recorder for servlet response headers and body.<p>
     */
    private static class ResponseRecorder implements InvocationHandler {

        /** The response body. */
        private ByteArrayOutputStream m_body = new ByteArrayOutputStream();

        /** The date headers. */
        private Map<String, Long> m_dateHeaders = new HashMap<>();

        /** The response headers. */
        private Map<String, List<String>> m_headers = new HashMap<>();

        /** The content length. */
        private int m_contentLength = -1;

        /** The status. */
        private int m_status = -1;

        /**
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            String methodName = method.getName();
            if ("setStatus".equals(methodName)) {
                m_status = ((Integer)args[0]).intValue();
                return null;
            } else if ("setContentLength".equals(methodName)) {
                m_contentLength = ((Integer)args[0]).intValue();
                return null;
            } else if ("setDateHeader".equals(methodName)) {
                m_dateHeaders.put((String)args[0], (Long)args[1]);
                return null;
            } else if ("setHeader".equals(methodName)) {
                List<String> values = new ArrayList<>();
                values.add((String)args[1]);
                m_headers.put((String)args[0], values);
                return null;
            } else if ("addHeader".equals(methodName)) {
                m_headers.computeIfAbsent((String)args[0], key -> new ArrayList<>()).add((String)args[1]);
                return null;
            } else if ("containsHeader".equals(methodName)) {
                return Boolean.valueOf(m_headers.containsKey(args[0]) || m_dateHeaders.containsKey(args[0]));
            } else if ("getOutputStream".equals(methodName)) {
                return new ServletOutputStream() {

                    @Override
                    public boolean isReady() {

                        return true;
                    }

                    @Override
                    public void setWriteListener(WriteListener writeListener) {

                        // nothing to do
                    }

                    @Override
                    public void write(byte[] buffer, int offset, int length) throws IOException {

                        m_body.write(buffer, offset, length);
                    }

                    @Override
                    public void write(int value) {

                        m_body.write(value);
                    }
                };
            }
            return defaultValue(method.getReturnType());
        }

        /**
         * Returns the response body.<p>
         *
         * @return the response body
         */
        byte[] getBody() {

            return m_body.toByteArray();
        }

        /**
         * Returns the cache control header values.<p>
         *
         * @return the cache control header values
         */
        List<String> getCacheControlHeaders() {

            return m_headers.get(CmsRequestUtil.HEADER_CACHE_CONTROL);
        }

        /**
         * Returns the content length.<p>
         *
         * @return the content length
         */
        int getContentLength() {

            return m_contentLength;
        }

        /**
         * Returns a date header.<p>
         *
         * @param name the header name
         *
         * @return the date header value
         */
        long getDateHeader(String name) {

            return m_dateHeaders.get(name).longValue();
        }

        /**
         * Returns the response status.<p>
         *
         * @return the response status
         */
        int getStatus() {

            return m_status;
        }
    }

    /** Test resource path. */
    private static final String RESOURCE_NAME = "/types/testdata.zip";

    /**
     * Creates a default value for a method return type.<p>
     *
     * @param type the return type
     *
     * @return the default value
     */
    private static Object defaultValue(Class<?> type) {

        if (type == boolean.class) {
            return Boolean.FALSE;
        } else if (type == int.class) {
            return Integer.valueOf(0);
        } else if (type == long.class) {
            return Long.valueOf(0);
        }
        return null;
    }

    /**
     * @see org.opencms.test.OpenCmsTestRunner#$openCmsSetUp(org.junit.jupiter.api.TestInfo)
     */
    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/", "../org/opencms/staticexport/");
    }

    /**
     * Tests that export and exportTo use the same response headers.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testExportAndExportToSetEquivalentHeaders() throws Throwable {

        CmsObject cms = getCmsObject();
        CmsResource resource = readResource(cms);
        CmsFile file = cms.readFile(resource);
        CmsDumpLoader loader = getDumpLoader(resource);
        ResponseRecorder exportResponseRecorder = new ResponseRecorder();
        HttpServletResponse exportResponse = createResponse(exportResponseRecorder);
        HttpServletRequest exportRequest = createRequest(-1, false, cms);
        byte[] exportedContent = loader.export(cms, resource, exportRequest, exportResponse);

        ResponseRecorder streamingResponseRecorder = new ResponseRecorder();
        HttpServletResponse streamingResponse = createResponse(streamingResponseRecorder);
        ByteArrayOutputStream exportOut = new ByteArrayOutputStream();
        loader.exportTo(cms, resource, createRequest(-1, false, cms), streamingResponse, exportOut);

        assertTrue(Arrays.equals(file.getContents(), exportedContent));
        assertTrue(Arrays.equals(file.getContents(), exportResponseRecorder.getBody()));
        assertTrue(Arrays.equals(file.getContents(), streamingResponseRecorder.getBody()));
        assertTrue(Arrays.equals(file.getContents(), exportOut.toByteArray()));
        assertEquivalentHeaders(resource, exportResponseRecorder, streamingResponseRecorder);
    }

    /**
     * Tests that a matching if-modified-since header prevents writing a body.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testLoadSendsNotModifiedWithoutBody() throws Throwable {

        CmsObject cms = getCmsObject();
        CmsResource resource = readResource(cms);
        long lastModifiedHeader = (resource.getDateLastModified() / 1000) * 1000;
        ResponseRecorder recorder = new ResponseRecorder();

        getDumpLoader(
            resource).load(cms, resource, createRequest(lastModifiedHeader, false, cms), createResponse(recorder));

        assertEquals(HttpServletResponse.SC_NOT_MODIFIED, recorder.getStatus());
        assertEquals(-1, recorder.getContentLength());
        assertEquals(0, recorder.getBody().length);
    }

    /**
     * Tests regular load response headers.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testLoadSetsHeadersBeforeStreaming() throws Throwable {

        CmsObject cms = getCmsObject();
        CmsResource resource = readResource(cms);
        CmsFile file = cms.readFile(resource);
        ResponseRecorder recorder = new ResponseRecorder();
        getDumpLoader(resource).load(cms, resource, createRequest(-1, false, cms), createResponse(recorder));

        assertEquals(HttpServletResponse.SC_OK, recorder.getStatus());
        assertEquals(resource.getLength(), recorder.getContentLength());
        assertEquals(resource.getDateLastModified(), recorder.getDateHeader(CmsRequestUtil.HEADER_LAST_MODIFIED));
        assertNotNull(recorder.getCacheControlHeaders());
        assertTrue(Arrays.equals(file.getContents(), recorder.getBody()));
    }

    /**
     * Tests no-cache headers for workplace users.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testWorkplaceRequestSetsNoCacheHeaders() throws Throwable {

        CmsObject cms = getCmsObject();
        CmsResource resource = readResource(cms);
        ResponseRecorder recorder = new ResponseRecorder();
        long before = System.currentTimeMillis();

        getDumpLoader(resource).load(cms, resource, createRequest(-1, true, cms), createResponse(recorder));

        assertEquals(HttpServletResponse.SC_OK, recorder.getStatus());
        assertTrue(recorder.getDateHeader(CmsRequestUtil.HEADER_LAST_MODIFIED) >= before);
        assertEquals(
            Arrays.asList(
                CmsRequestUtil.HEADER_VALUE_MAX_AGE + "0",
                CmsRequestUtil.HEADER_VALUE_MUST_REVALIDATE,
                CmsRequestUtil.HEADER_VALUE_NO_CACHE,
                CmsRequestUtil.HEADER_VALUE_NO_STORE),
            recorder.getCacheControlHeaders());
    }

    /**
     * Checks equivalent response headers.<p>
     *
     * @param resource the tested resource
     * @param first the first response
     * @param second the second response
     */
    private void assertEquivalentHeaders(CmsResource resource, ResponseRecorder first, ResponseRecorder second) {

        assertEquals(HttpServletResponse.SC_OK, first.getStatus());
        assertEquals(HttpServletResponse.SC_OK, second.getStatus());
        assertEquals(resource.getLength(), first.getContentLength());
        assertEquals(resource.getLength(), second.getContentLength());
        assertEquals(
            first.getDateHeader(CmsRequestUtil.HEADER_LAST_MODIFIED),
            second.getDateHeader(CmsRequestUtil.HEADER_LAST_MODIFIED));
        assertEquals(first.getCacheControlHeaders(), second.getCacheControlHeaders());
    }

    /**
     * Creates a servlet request proxy.<p>
     *
     * @param ifModifiedSince the if-modified-since header value
     * @param workplaceUser if the request should be treated as a workplace request
     * @param cms the CMS context
     *
     * @return the request proxy
     *
     * @throws Exception if setting up a workplace session fails
     */
    private HttpServletRequest createRequest(long ifModifiedSince, boolean workplaceUser, CmsObject cms)
    throws Exception {

        HttpSession session = workplaceUser ? createWorkplaceSession(cms.getRequestContext().getCurrentUser()) : null;
        InvocationHandler handler = new InvocationHandler() {

            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                String methodName = method.getName();
                if ("getDateHeader".equals(methodName) && CmsRequestUtil.HEADER_IF_MODIFIED_SINCE.equals(args[0])) {
                    return Long.valueOf(ifModifiedSince);
                } else if ("getSession".equals(methodName)) {
                    if ((args != null) && (args.length == 1) && Boolean.FALSE.equals(args[0])) {
                        return session;
                    }
                    return session;
                } else if ("getHeader".equals(methodName)) {
                    return null;
                }
                return defaultValue(method.getReturnType());
            }
        };
        return (HttpServletRequest)Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[] {HttpServletRequest.class},
            handler);
    }

    /**
     * Creates a response proxy.<p>
     *
     * @param recorder the response recorder
     *
     * @return the response proxy
     */
    private HttpServletResponse createResponse(ResponseRecorder recorder) {

        return (HttpServletResponse)Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[] {HttpServletResponse.class},
            recorder);
    }

    /**
     * Creates a workplace session proxy.<p>
     *
     * @param user the workplace user
     *
     * @return the session proxy
     *
     * @throws Exception if creating the workplace settings fails
     */
    private HttpSession createWorkplaceSession(CmsUser user) throws Exception {

        Constructor<CmsWorkplaceSettings> constructor = CmsWorkplaceSettings.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        CmsWorkplaceSettings settings = constructor.newInstance();
        settings.setUser(user);
        InvocationHandler handler = new InvocationHandler() {

            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                if ("getAttribute".equals(method.getName())
                    && CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS.equals(args[0])) {
                    return settings;
                }
                return defaultValue(method.getReturnType());
            }
        };
        return (HttpSession)Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[] {HttpSession.class},
            handler);
    }

    /**
     * Returns the dump loader for the resource.<p>
     *
     * @param resource the resource
     *
     * @return the dump loader
     *
     * @throws Exception if reading the loader fails
     */
    private CmsDumpLoader getDumpLoader(CmsResource resource) throws Exception {

        return (CmsDumpLoader)OpenCms.getResourceManager().getLoader(resource);
    }

    /**
     * Reads the test resource.<p>
     *
     * @param cms the CMS context
     *
     * @return the test resource
     *
     * @throws Exception if reading fails
     */
    private CmsResource readResource(CmsObject cms) throws Exception {

        return cms.readResource(RESOURCE_NAME);
    }
}

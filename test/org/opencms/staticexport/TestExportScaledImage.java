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

package org.opencms.staticexport;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.loader.CmsImageLoader;
import org.opencms.loader.CmsImageScaler;
import org.opencms.loader.I_CmsImageCache;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestRunner;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsRequestUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * @since 6.0.0
 */
public class TestExportScaledImage extends OpenCmsTestRunner {

    /**
     * Request recorder.<p>
     */
    private static class RequestRecorder implements InvocationHandler {

        /** The request headers. */
        private Map<String, String> m_headers = new HashMap<String, String>();

        /** The scale parameters. */
        private String m_scaleParams;

        /**
         * Creates a new recorder.<p>
         *
         * @param scaleParams the scale parameters
         */
        RequestRecorder(String scaleParams) {

            m_scaleParams = scaleParams;
        }

        /**
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            if (method.getName().equals("getParameter")) {
                if (Objects.equals(CmsImageScaler.PARAM_SCALE, args[0])) {
                    return m_scaleParams;
                }
                return null;
            } else if (method.getName().equals("getHeader")) {
                return m_headers.get(args[0]);
            } else if (method.getName().equals("getDateHeader")) {
                return Long.valueOf(-1);
            } else if (method.getName().equals("getMethod")) {
                return "GET";
            } else {
                if (method.getReturnType() == int.class) {
                    return Integer.valueOf(0);
                } else if (method.getReturnType() == long.class) {
                    return Long.valueOf(0);
                } else if (method.getReturnType() == boolean.class) {
                    return Boolean.FALSE;
                }
                return null;
            }
        }

        /**
         * Sets a request header.<p>
         *
         * @param name the header name
         * @param value the header value
         */
        void setHeader(String name, String value) {

            m_headers.put(name, value);
        }
    }

    /**
     * Response recorder.<p>
     */
    private static class ResponseRecorder implements InvocationHandler {

        /** The response body. */
        private ByteArrayOutputStream m_body = new ByteArrayOutputStream();

        /** The response headers. */
        private Map<String, String> m_headers = new HashMap<String, String>();

        /** The content length. */
        private int m_contentLength = -1;

        /** The response status. */
        private int m_status = -1;

        /**
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            if (method.getName().equals("getOutputStream")) {
                return new ServletOutputStream() {

                    @Override
                    public boolean isReady() {

                        return true;
                    }

                    @Override
                    public void setWriteListener(WriteListener writeListener) {

                        // noop
                    }

                    @Override
                    public void write(byte[] buffer, int offset, int length) throws IOException {

                        m_body.write(buffer, offset, length);
                    }

                    @Override
                    public void write(int b) throws IOException {

                        m_body.write(b);
                    }
                };
            } else if (method.getName().equals("setStatus")) {
                m_status = ((Integer)args[0]).intValue();
                return null;
            } else if (method.getName().equals("setContentLength")) {
                m_contentLength = ((Integer)args[0]).intValue();
                return null;
            } else if (method.getName().equals("setHeader")) {
                m_headers.put((String)args[0], (String)args[1]);
                return null;
            } else if (method.getName().equals("setDateHeader")) {
                m_headers.put((String)args[0], String.valueOf(args[1]));
                return null;
            } else if (method.getName().equals("containsHeader")) {
                return Boolean.valueOf(m_headers.containsKey(args[0]));
            } else {
                if (method.getReturnType() == int.class) {
                    return Integer.valueOf(0);
                } else if (method.getReturnType() == long.class) {
                    return Long.valueOf(0);
                } else if (method.getReturnType() == boolean.class) {
                    return Boolean.FALSE;
                }
                return null;
            }
        }

        /**
         * Returns the body.<p>
         *
         * @return the body
         */
        byte[] getBody() {

            return m_body.toByteArray();
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
         * Returns a header.<p>
         *
         * @param name the header name
         *
         * @return the header value
         */
        String getHeader(String name) {

            return m_headers.get(name);
        }

        /**
         * Returns the status.<p>
         *
         * @return the status
         */
        int getStatus() {

            return m_status;
        }
    }

    /**
     * In-memory image cache.<p>
     */
    private static class TestImageCache implements I_CmsImageCache {

        /** The content by key. */
        private Map<String, byte[]> m_content = new HashMap<String, byte[]>();

        /**
         * @see org.opencms.loader.I_CmsImageCache#exists(java.lang.String)
         */
        public boolean exists(String key) {

            return m_content.containsKey(key);
        }

        /**
         * @see org.opencms.loader.I_CmsImageCache#getLength(java.lang.String)
         */
        public long getLength(String key) {

            return m_content.get(key).length;
        }

        /**
         * @see org.opencms.loader.I_CmsImageCache#supportsRangeDelivery()
         */
        public boolean supportsRangeDelivery() {

            return true;
        }

        /**
         * @see org.opencms.loader.I_CmsImageCache#write(java.lang.String, byte[])
         */
        public void write(String key, byte[] content) {

            m_content.put(key, content);
        }

        /**
         * @see org.opencms.loader.I_CmsImageCache#writeRangeTo(java.lang.String, long, long, java.io.OutputStream)
         */
        public void writeRangeTo(String key, long start, long length, OutputStream out) throws IOException {

            out.write(m_content.get(key), (int)start, (int)length);
        }

        /**
         * @see org.opencms.loader.I_CmsImageCache#writeTo(java.lang.String, java.io.OutputStream)
         */
        public void writeTo(String key, OutputStream out) throws IOException {

            out.write(m_content.get(key));
        }
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
     * Tests the file export.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
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
        ByteArrayOutputStream responseContent = new ByteArrayOutputStream();
        // Request and response are provided only with information needed to get scaling running at all.
        HttpServletRequest testRequest = (HttpServletRequest)Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[] {HttpServletRequest.class},
            new InvocationHandler() {

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                    if (method.getName().equals("getParameter")) {
                        if (Objects.equals(CmsImageScaler.PARAM_SCALE, args[0])) {
                            return scaleParams;
                        }
                        return null;
                    } else {
                        if (method.getReturnType() == int.class) {
                            return Integer.valueOf(0);
                        } else if (method.getReturnType() == long.class) {
                            return Long.valueOf(0);
                        } else if (method.getReturnType() == boolean.class) {
                            return Boolean.FALSE;
                        }
                        return null;
                    }
                }
            });
        HttpServletResponse testResponse = (HttpServletResponse)Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[] {HttpServletResponse.class},
            new InvocationHandler() {

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                    if (method.getName().equals("getOutputStream")) {
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
                            public void write(byte[] buffer, int offset, int length) throws IOException {

                                responseContent.write(buffer, offset, length);
                            }

                            @Override
                            public void write(int b) throws IOException {

                                responseContent.write(b);
                            }
                        };
                    } else {
                        if (method.getReturnType() == int.class) {
                            return Integer.valueOf(0);
                        } else if (method.getReturnType() == long.class) {
                            return Long.valueOf(0);
                        } else if (method.getReturnType() == boolean.class) {
                            return Boolean.FALSE;
                        }
                        return null;
                    }
                }
            });

        OpenCms.getStaticExportManager().export(testRequest, testResponse, exportCms, data);

        File f = new File(exportPath);
        assertTrue(f.exists());

        byte[] expectedContent = (new CmsImageScaler(scaleParams)).scaleImage(cms.readFile(resourcename));
        // check the exported content
        byte[] exportContent = Files.readAllBytes(f.toPath());
        assertEquals(Arrays.toString(expectedContent), Arrays.toString(exportContent));
        assertEquals(Arrays.toString(expectedContent), Arrays.toString(responseContent.toByteArray()));
        assertTrue(hasCacheFile(new File(CmsImageLoader.getImageRepositoryPath()), exportContent));
    }

    /**
     * Tests direct scaled image export through an image cache.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testExportScaledImageUsesImageCacheDirectResponse() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing direct export of scaled image from image cache");

        String resourcename = "/folder1/image1.gif";
        String scaleParams = "cx:5,cy:5,ch:10,cw:10,t:0,h:40,w:40,transparent";
        CmsFile imageFile = cms.readFile(resourcename);
        String rootPath = cms.getRequestContext().addSiteRoot(resourcename);
        String exportPath = CmsFileUtil.normalizePath(
            OpenCms.getStaticExportManager().getExportPath(rootPath) + rootPath);
        File exportFile = new File(exportPath);
        if (exportFile.exists()) {
            assertTrue(exportFile.delete());
        }

        CmsImageLoader loader = (CmsImageLoader)OpenCms.getResourceManager().getLoader(imageFile);
        Field storeField = CmsImageLoader.class.getDeclaredField("m_imageCache");
        storeField.setAccessible(true);
        Object originalStore = storeField.get(loader);
        TestImageCache store = new TestImageCache();
        try (AutoCloseable directDelivery = configureStoredContentDirectDelivery(true, ".gif")) {
            storeField.set(loader, store);
            CmsStaticExportData data = new CmsStaticExportData(
                rootPath,
                rootPath,
                imageFile,
                CmsImageScaler.PARAM_SCALE + "=" + scaleParams);
            CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            RequestRecorder requestRecorder = new RequestRecorder(scaleParams);
            ResponseRecorder responseRecorder = new ResponseRecorder();

            int status = OpenCms.getStaticExportManager().export(
                createRequest(requestRecorder),
                createResponse(responseRecorder),
                exportCms,
                data);

            byte[] expectedContent = (new CmsImageScaler(scaleParams)).scaleImage(cms.readFile(resourcename));
            assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), Integer.valueOf(status));
            assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), Integer.valueOf(responseRecorder.getStatus()));
            assertEquals(Integer.valueOf(expectedContent.length), Integer.valueOf(responseRecorder.getContentLength()));
            assertEquals(Arrays.toString(expectedContent), Arrays.toString(responseRecorder.getBody()));
            assertEquals(getImageCacheETag(store), responseRecorder.getHeader(CmsRequestUtil.HEADER_ETAG));
            assertFalse(exportFile.exists());
            assertEquals(Integer.valueOf(1), Integer.valueOf(store.m_content.size()));
        } finally {
            storeField.set(loader, originalStore);
        }
    }

    /**
     * Tests If-None-Match delivery for direct scaled image export through an image cache.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testExportScaledImageUsesImageCacheIfNoneMatch() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing direct image cache If-None-Match response");

        String resourcename = "/folder1/image1.gif";
        String scaleParams = "cx:5,cy:5,ch:10,cw:10,t:0,h:40,w:40,transparent";
        CmsFile imageFile = cms.readFile(resourcename);
        String rootPath = cms.getRequestContext().addSiteRoot(resourcename);

        CmsImageLoader loader = (CmsImageLoader)OpenCms.getResourceManager().getLoader(imageFile);
        Field storeField = CmsImageLoader.class.getDeclaredField("m_imageCache");
        storeField.setAccessible(true);
        Object originalStore = storeField.get(loader);
        TestImageCache store = new TestImageCache();
        try (AutoCloseable directDelivery = configureStoredContentDirectDelivery(true, ".gif")) {
            storeField.set(loader, store);
            CmsStaticExportData data = new CmsStaticExportData(
                rootPath,
                rootPath,
                imageFile,
                CmsImageScaler.PARAM_SCALE + "=" + scaleParams);
            CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());

            OpenCms.getStaticExportManager().export(
                createRequest(new RequestRecorder(scaleParams)),
                createResponse(new ResponseRecorder()),
                exportCms,
                data);
            String etag = getImageCacheETag(store);

            RequestRecorder requestRecorder = new RequestRecorder(scaleParams);
            requestRecorder.setHeader(CmsRequestUtil.HEADER_IF_NONE_MATCH, etag);
            ResponseRecorder responseRecorder = new ResponseRecorder();

            int status = OpenCms.getStaticExportManager().export(
                createRequest(requestRecorder),
                createResponse(responseRecorder),
                exportCms,
                data);

            assertEquals(Integer.valueOf(HttpServletResponse.SC_NOT_MODIFIED), Integer.valueOf(status));
            assertEquals(
                Integer.valueOf(HttpServletResponse.SC_NOT_MODIFIED),
                Integer.valueOf(responseRecorder.getStatus()));
            assertEquals(etag, responseRecorder.getHeader(CmsRequestUtil.HEADER_ETAG));
            assertEquals(Integer.valueOf(0), Integer.valueOf(responseRecorder.getBody().length));
        } finally {
            storeField.set(loader, originalStore);
        }
    }

    /**
     * Tests matching If-Range delivery for direct scaled image export through an image cache.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testExportScaledImageUsesImageCacheIfRangeMatchingETag() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing direct image cache If-Range response");

        String resourcename = "/folder1/image1.gif";
        String scaleParams = "cx:5,cy:5,ch:10,cw:10,t:0,h:40,w:40,transparent";
        CmsFile imageFile = cms.readFile(resourcename);
        String rootPath = cms.getRequestContext().addSiteRoot(resourcename);

        CmsImageLoader loader = (CmsImageLoader)OpenCms.getResourceManager().getLoader(imageFile);
        Field storeField = CmsImageLoader.class.getDeclaredField("m_imageCache");
        storeField.setAccessible(true);
        Object originalStore = storeField.get(loader);
        TestImageCache store = new TestImageCache();
        try (AutoCloseable directDelivery = configureStoredContentDirectDelivery(true, ".gif")) {
            storeField.set(loader, store);
            CmsStaticExportData data = new CmsStaticExportData(
                rootPath,
                rootPath,
                imageFile,
                CmsImageScaler.PARAM_SCALE + "=" + scaleParams);
            CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());

            OpenCms.getStaticExportManager().export(
                createRequest(new RequestRecorder(scaleParams)),
                createResponse(new ResponseRecorder()),
                exportCms,
                data);
            String etag = getImageCacheETag(store);

            RequestRecorder requestRecorder = new RequestRecorder(scaleParams);
            requestRecorder.setHeader(CmsRequestUtil.HEADER_RANGE, "bytes=3-12");
            requestRecorder.setHeader(CmsRequestUtil.HEADER_IF_RANGE, etag);
            ResponseRecorder responseRecorder = new ResponseRecorder();

            int status = OpenCms.getStaticExportManager().export(
                createRequest(requestRecorder),
                createResponse(responseRecorder),
                exportCms,
                data);

            byte[] expectedContent = (new CmsImageScaler(scaleParams)).scaleImage(cms.readFile(resourcename));
            byte[] expectedRange = Arrays.copyOfRange(expectedContent, 3, 13);
            assertEquals(Integer.valueOf(HttpServletResponse.SC_PARTIAL_CONTENT), Integer.valueOf(status));
            assertEquals(
                Integer.valueOf(HttpServletResponse.SC_PARTIAL_CONTENT),
                Integer.valueOf(responseRecorder.getStatus()));
            assertEquals(etag, responseRecorder.getHeader(CmsRequestUtil.HEADER_ETAG));
            assertEquals(
                "bytes 3-12/" + expectedContent.length,
                responseRecorder.getHeader(CmsRequestUtil.HEADER_CONTENT_RANGE));
            assertEquals(Arrays.toString(expectedRange), Arrays.toString(responseRecorder.getBody()));
        } finally {
            storeField.set(loader, originalStore);
        }
    }

    /**
     * Tests If-Range fallback to full delivery for direct scaled image export through an image cache.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testExportScaledImageUsesImageCacheIfRangeMismatchAsFullContent() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing direct image cache If-Range fallback response");

        String resourcename = "/folder1/image1.gif";
        String scaleParams = "cx:5,cy:5,ch:10,cw:10,t:0,h:40,w:40,transparent";
        CmsFile imageFile = cms.readFile(resourcename);
        String rootPath = cms.getRequestContext().addSiteRoot(resourcename);

        CmsImageLoader loader = (CmsImageLoader)OpenCms.getResourceManager().getLoader(imageFile);
        Field storeField = CmsImageLoader.class.getDeclaredField("m_imageCache");
        storeField.setAccessible(true);
        Object originalStore = storeField.get(loader);
        TestImageCache store = new TestImageCache();
        try (AutoCloseable directDelivery = configureStoredContentDirectDelivery(true, ".gif")) {
            storeField.set(loader, store);
            CmsStaticExportData data = new CmsStaticExportData(
                rootPath,
                rootPath,
                imageFile,
                CmsImageScaler.PARAM_SCALE + "=" + scaleParams);
            CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());

            OpenCms.getStaticExportManager().export(
                createRequest(new RequestRecorder(scaleParams)),
                createResponse(new ResponseRecorder()),
                exportCms,
                data);
            RequestRecorder requestRecorder = new RequestRecorder(scaleParams);
            requestRecorder.setHeader(CmsRequestUtil.HEADER_RANGE, "bytes=3-12");
            requestRecorder.setHeader(CmsRequestUtil.HEADER_IF_RANGE, "\"other\"");
            ResponseRecorder responseRecorder = new ResponseRecorder();

            int status = OpenCms.getStaticExportManager().export(
                createRequest(requestRecorder),
                createResponse(responseRecorder),
                exportCms,
                data);

            byte[] expectedContent = (new CmsImageScaler(scaleParams)).scaleImage(cms.readFile(resourcename));
            assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), Integer.valueOf(status));
            assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), Integer.valueOf(responseRecorder.getStatus()));
            assertEquals(Integer.valueOf(expectedContent.length), Integer.valueOf(responseRecorder.getContentLength()));
            assertEquals(getImageCacheETag(store), responseRecorder.getHeader(CmsRequestUtil.HEADER_ETAG));
            assertEquals(Arrays.toString(expectedContent), Arrays.toString(responseRecorder.getBody()));
        } finally {
            storeField.set(loader, originalStore);
        }
    }

    /**
     * Tests range delivery for direct scaled image export through an image cache.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testExportScaledImageUsesImageCacheRangeResponse() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing direct range export of scaled image from image cache");

        String resourcename = "/folder1/image1.gif";
        String scaleParams = "cx:5,cy:5,ch:10,cw:10,t:0,h:40,w:40,transparent";
        CmsFile imageFile = cms.readFile(resourcename);
        String rootPath = cms.getRequestContext().addSiteRoot(resourcename);
        String exportPath = CmsFileUtil.normalizePath(
            OpenCms.getStaticExportManager().getExportPath(rootPath) + rootPath);
        File exportFile = new File(exportPath);
        if (exportFile.exists()) {
            assertTrue(exportFile.delete());
        }

        CmsImageLoader loader = (CmsImageLoader)OpenCms.getResourceManager().getLoader(imageFile);
        Field storeField = CmsImageLoader.class.getDeclaredField("m_imageCache");
        storeField.setAccessible(true);
        Object originalStore = storeField.get(loader);
        TestImageCache store = new TestImageCache();
        try (AutoCloseable directDelivery = configureStoredContentDirectDelivery(true, ".gif")) {
            storeField.set(loader, store);
            CmsStaticExportData data = new CmsStaticExportData(
                rootPath,
                rootPath,
                imageFile,
                CmsImageScaler.PARAM_SCALE + "=" + scaleParams);
            CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            RequestRecorder requestRecorder = new RequestRecorder(scaleParams);
            requestRecorder.setHeader(CmsRequestUtil.HEADER_RANGE, "bytes=3-12");
            ResponseRecorder responseRecorder = new ResponseRecorder();

            int status = OpenCms.getStaticExportManager().export(
                createRequest(requestRecorder),
                createResponse(responseRecorder),
                exportCms,
                data);

            byte[] expectedContent = (new CmsImageScaler(scaleParams)).scaleImage(cms.readFile(resourcename));
            byte[] expectedRange = Arrays.copyOfRange(expectedContent, 3, 13);
            assertEquals(Integer.valueOf(HttpServletResponse.SC_PARTIAL_CONTENT), Integer.valueOf(status));
            assertEquals(
                Integer.valueOf(HttpServletResponse.SC_PARTIAL_CONTENT),
                Integer.valueOf(responseRecorder.getStatus()));
            assertEquals(Integer.valueOf(expectedRange.length), Integer.valueOf(responseRecorder.getContentLength()));
            assertEquals(
                "bytes 3-12/" + expectedContent.length,
                responseRecorder.getHeader(CmsRequestUtil.HEADER_CONTENT_RANGE));
            assertEquals(getImageCacheETag(store), responseRecorder.getHeader(CmsRequestUtil.HEADER_ETAG));
            assertEquals(Arrays.toString(expectedRange), Arrays.toString(responseRecorder.getBody()));
            assertFalse(exportFile.exists());
            assertEquals(Integer.valueOf(1), Integer.valueOf(store.m_content.size()));
        } finally {
            storeField.set(loader, originalStore);
        }
    }

    /**
     * Tests scaled image export through an image cache with disabled image suffix.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testExportScaledImageUsesLocalExportWhenImageSuffixIsDisabled() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing local export of scaled image from image cache with disabled image suffix");

        String resourcename = "/folder1/image1.gif";
        String scaleParams = "cx:5,cy:5,ch:10,cw:10,t:0,h:40,w:40,transparent";
        CmsFile imageFile = cms.readFile(resourcename);
        String rootPath = cms.getRequestContext().addSiteRoot(resourcename);
        String exportPath = CmsFileUtil.normalizePath(
            OpenCms.getStaticExportManager().getExportPath(rootPath) + rootPath);
        File exportFile = new File(exportPath);
        if (exportFile.exists()) {
            assertTrue(exportFile.delete());
        }

        CmsImageLoader loader = (CmsImageLoader)OpenCms.getResourceManager().getLoader(imageFile);
        Field storeField = CmsImageLoader.class.getDeclaredField("m_imageCache");
        storeField.setAccessible(true);
        Object originalStore = storeField.get(loader);
        TestImageCache store = new TestImageCache();
        try (AutoCloseable directDelivery = configureStoredContentDirectDelivery(true, ".pdf")) {
            storeField.set(loader, store);
            CmsStaticExportData data = new CmsStaticExportData(
                rootPath,
                rootPath,
                imageFile,
                CmsImageScaler.PARAM_SCALE + "=" + scaleParams);
            CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            RequestRecorder requestRecorder = new RequestRecorder(scaleParams);
            ResponseRecorder responseRecorder = new ResponseRecorder();

            int status = OpenCms.getStaticExportManager().export(
                createRequest(requestRecorder),
                createResponse(responseRecorder),
                exportCms,
                data);

            byte[] expectedContent = (new CmsImageScaler(scaleParams)).scaleImage(cms.readFile(resourcename));
            assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), Integer.valueOf(status));
            assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), Integer.valueOf(responseRecorder.getStatus()));
            assertEquals(Integer.valueOf(expectedContent.length), Integer.valueOf(responseRecorder.getContentLength()));
            assertEquals(Arrays.toString(expectedContent), Arrays.toString(responseRecorder.getBody()));
            assertTrue(exportFile.exists());
            assertEquals(Arrays.toString(expectedContent), Arrays.toString(Files.readAllBytes(exportFile.toPath())));
            assertEquals(Integer.valueOf(1), Integer.valueOf(store.m_content.size()));
        } finally {
            storeField.set(loader, originalStore);
        }
    }

    /**
     * Configures stored content direct delivery for the current test and returns a reset hook.<p>
     *
     * @param enabled if direct delivery should be enabled
     * @param suffixes the enabled suffixes
     *
     * @return a reset hook
     */
    private AutoCloseable configureStoredContentDirectDelivery(boolean enabled, String... suffixes) {

        CmsStaticExportManager manager = OpenCms.getStaticExportManager();
        CmsStoredContentDeliveryConfiguration originalConfiguration = manager.getStoredContentDeliveryConfiguration();
        CmsStoredContentDeliveryConfiguration configuration = new CmsStoredContentDeliveryConfiguration();
        configuration.setEnabled(String.valueOf(enabled));
        for (int i = 0; i < suffixes.length; i++) {
            configuration.addEnabledSuffix(suffixes[i]);
        }
        manager.setStoredContentDeliveryConfiguration(configuration);
        return new AutoCloseable() {

            public void close() {

                manager.setStoredContentDeliveryConfiguration(originalConfiguration);
            }
        };
    }

    /**
     * Creates a request proxy.<p>
     *
     * @param recorder the recorder
     *
     * @return the request
     */
    private HttpServletRequest createRequest(RequestRecorder recorder) {

        return (HttpServletRequest)Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[] {HttpServletRequest.class},
            recorder);
    }

    /**
     * Creates a response proxy.<p>
     *
     * @param recorder the recorder
     *
     * @return the response
     */
    private HttpServletResponse createResponse(ResponseRecorder recorder) {

        return (HttpServletResponse)Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[] {HttpServletResponse.class},
            recorder);
    }

    /**
     * Returns the ETag for the single image cache entry.<p>
     *
     * @param store the image cache store
     * @return the ETag
     */
    private String getImageCacheETag(TestImageCache store) {

        assertEquals(Integer.valueOf(1), Integer.valueOf(store.m_content.size()));
        return "\"" + DigestUtils.sha256Hex(store.m_content.keySet().iterator().next()) + "\"";
    }

    /**
     * Checks if the given image cache repository contains the expected file content.<p>
     *
     * @param folder the folder to check
     * @param expectedContent the expected content
     *
     * @return <code>true</code> if the content was found
     *
     * @throws Exception if reading the cache fails
     */
    private boolean hasCacheFile(File folder, byte[] expectedContent) throws Exception {

        if (!folder.exists()) {
            return false;
        }
        File[] files = folder.listFiles();
        if (files == null) {
            return false;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                if (hasCacheFile(file, expectedContent)) {
                    return true;
                }
            } else if (Arrays.equals(expectedContent, Files.readAllBytes(file.toPath()))) {
                return true;
            }
        }
        return false;
    }
}

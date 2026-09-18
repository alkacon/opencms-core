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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.opencms.configuration.CmsImageCacheConfiguration;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.loader.CmsFsImageCache;
import org.opencms.loader.CmsImageCacheEntryNotFoundException;
import org.opencms.loader.CmsImageLoader;
import org.opencms.loader.CmsImageScaler;
import org.opencms.loader.I_CmsImageCache;
import org.opencms.loader.imagecache.CmsImageCacheAccessRenewal;
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
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @since 6.0.0
 */
public class TestExportScaledImage extends OpenCmsTestRunner {

    /**
     * Request recorder.<p>
     */
    private static class RequestRecorder implements InvocationHandler {

        /** The request date headers. */
        private Map<String, Long> m_dateHeaders = new HashMap<String, Long>();

        /** The request headers. */
        private Map<String, String> m_headers = new HashMap<String, String>();

        /** The request method. */
        private String m_method = "GET";

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
                Long value = m_dateHeaders.get(args[0]);
                return value != null ? value : Long.valueOf(-1);
            } else if (method.getName().equals("getMethod")) {
                return m_method;
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
         * Sets a request date header.<p>
         *
         * @param name the header name
         * @param value the header value
         */
        void setDateHeader(String name, long value) {

            m_dateHeaders.put(name, Long.valueOf(value));
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

        /**
         * Sets the request method.<p>
         *
         * @param method the request method
         */
        void setMethod(String method) {

            m_method = method;
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

        /** The content type. */
        private String m_contentType;

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
            } else if (method.getName().equals("setContentType")) {
                m_contentType = (String)args[0];
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
         * Returns the content type.<p>
         *
         * @return the content type
         */
        String getContentType() {

            return m_contentType;
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

        /** Number of exists calls. */
        private int m_existsCalls;

        /** Number of length calls. */
        private int m_lengthCalls;

        /** Number of range read calls. */
        private int m_rangeReadCalls;

        /** Number of complete read calls. */
        private int m_readCalls;

        /** Number of write calls. */
        private int m_writeCalls;

        /** Number of authoritative existence checks. */
        private int m_authoritativeExistsCalls;

        /** Number of complete reads which should report a missing entry. */
        private int m_missingReadFailures;

        /** Number of complete reads which should report a generic I/O failure. */
        private int m_readFailures;

        /** Number of range reads which should report a missing entry. */
        private int m_missingRangeReadFailures;

        /** Whether the next authoritative existence check should report a missing entry. */
        private boolean m_missingAuthoritativeCheck;

        /**
         * @see org.opencms.loader.I_CmsImageCache#exists(java.lang.String)
         */
        public boolean exists(String key) {

            m_existsCalls++;
            return m_content.containsKey(key);
        }

        /**
         * @see org.opencms.loader.I_CmsImageCache#existsAuthoritatively(java.lang.String)
         */
        @Override
        public boolean existsAuthoritatively(String key) {

            m_authoritativeExistsCalls++;
            if (m_missingAuthoritativeCheck) {
                m_missingAuthoritativeCheck = false;
                return false;
            }
            return m_content.containsKey(key);
        }

        /**
         * @see org.opencms.loader.I_CmsImageCache#getLength(java.lang.String)
         */
        public long getLength(String key) {

            m_lengthCalls++;
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

            m_writeCalls++;
            m_content.put(key, content);
        }

        /**
         * @see org.opencms.loader.I_CmsImageCache#writeRangeTo(java.lang.String, long, long, java.io.OutputStream)
         */
        public void writeRangeTo(String key, long start, long length, OutputStream out) throws IOException {

            m_rangeReadCalls++;
            if (m_missingRangeReadFailures > 0) {
                m_missingRangeReadFailures--;
                throw new CmsImageCacheEntryNotFoundException(key);
            }
            out.write(m_content.get(key), (int)start, (int)length);
        }

        /**
         * @see org.opencms.loader.I_CmsImageCache#writeTo(java.lang.String, java.io.OutputStream)
         */
        public void writeTo(String key, OutputStream out) throws IOException {

            m_readCalls++;
            if (m_missingReadFailures > 0) {
                m_missingReadFailures--;
                throw new CmsImageCacheEntryNotFoundException(key);
            }
            if (m_readFailures > 0) {
                m_readFailures--;
                throw new IOException("Simulated image cache read failure");
            }
            out.write(m_content.get(key));
        }

        /**
         * Resets the storage read counters.<p>
         */
        void resetReadCounters() {

            m_existsCalls = 0;
            m_lengthCalls = 0;
            m_rangeReadCalls = 0;
            m_readCalls = 0;
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
     * Tests that an early image cache 304 response counts as an access for renewal.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testEarlyImageCacheNotModifiedRenewsSharedFsEntry() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing access renewal after an early image-cache 304 response");

        String resourcename = "/folder1/image1.gif";
        String scaleParams = "cx:5,cy:5,ch:10,cw:10,t:0,h:40,w:40,transparent";
        CmsFile imageFile = cms.readFile(resourcename);
        String rootPath = cms.getRequestContext().addSiteRoot(resourcename);
        CmsImageLoader loader = (CmsImageLoader)OpenCms.getResourceManager().getLoader(imageFile);
        Field storeField = CmsImageLoader.class.getDeclaredField("m_imageCache");
        storeField.setAccessible(true);
        Object originalStore = storeField.get(loader);
        Field renewalField = CmsImageLoader.class.getDeclaredField("m_imageCacheAccessRenewal");
        renewalField.setAccessible(true);
        Object originalRenewal = renewalField.get(null);
        Path repository = Files.createTempDirectory("opencms-image-cache-304-renewal");
        CmsImageCacheAccessRenewal renewal = null;
        try (AutoCloseable directDelivery = configureStoredContentDirectDelivery(true, ".gif")) {
            CmsFsImageCache store = new CmsFsImageCache(repository.toString());
            storeField.set(loader, store);
            renewalField.set(null, null);
            CmsStaticExportData data = new CmsStaticExportData(
                rootPath,
                rootPath,
                imageFile,
                CmsImageScaler.PARAM_SCALE + "=" + scaleParams);
            CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            ResponseRecorder initialResponse = new ResponseRecorder();

            assertTrue(
                OpenCms.getStaticExportManager().tryExportImageCache(
                    createRequest(new RequestRecorder(scaleParams)),
                    createResponse(initialResponse),
                    exportCms,
                    data));
            assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), Integer.valueOf(initialResponse.getStatus()));

            Path cacheFile;
            try (Stream<Path> paths = Files.walk(repository)) {
                cacheFile = paths.filter(Files::isRegularFile).findFirst().orElseThrow(AssertionError::new);
            }
            Instant oldTime = Instant.now().minusSeconds(10);
            Files.setLastModifiedTime(cacheFile, FileTime.from(oldTime));
            CmsImageCacheConfiguration configuration = new CmsImageCacheConfiguration();
            configuration.setRetention("renew-on-use", "PT2S", "PT1S", "PT0S");
            configuration.setFs("true", "1");
            configuration.validate();
            renewal = CmsImageCacheAccessRenewal.create(store, configuration);
            renewalField.set(null, renewal);

            RequestRecorder revalidationRequest = new RequestRecorder(scaleParams);
            revalidationRequest.setHeader(
                CmsRequestUtil.HEADER_IF_NONE_MATCH,
                initialResponse.getHeader(CmsRequestUtil.HEADER_ETAG));
            ResponseRecorder revalidationResponse = new ResponseRecorder();
            assertTrue(
                OpenCms.getStaticExportManager().tryExportImageCache(
                    createRequest(revalidationRequest),
                    createResponse(revalidationResponse),
                    exportCms,
                    data));
            assertEquals(
                Integer.valueOf(HttpServletResponse.SC_NOT_MODIFIED),
                Integer.valueOf(revalidationResponse.getStatus()));

            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while ((System.nanoTime() < deadline)
                && !Files.getLastModifiedTime(cacheFile).toInstant().isAfter(oldTime)) {
                Thread.sleep(10);
            }
            assertTrue(Files.getLastModifiedTime(cacheFile).toInstant().isAfter(oldTime));
        } finally {
            if (renewal != null) {
                renewal.close();
            }
            renewalField.set(null, originalRenewal);
            storeField.set(loader, originalStore);
            try (Stream<Path> paths = Files.walk(repository)) {
                paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
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
     * Tests the early direct-response fast path for a registered scaled image link.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testExportScaledImageUsesEarlyImageCacheFastPath() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing early image-cache fast path for a registered scaled image");

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
            RequestRecorder requestRecorder = new RequestRecorder(scaleParams);
            ResponseRecorder responseRecorder = new ResponseRecorder();

            assertTrue(
                OpenCms.getStaticExportManager().tryExportImageCache(
                    createRequest(requestRecorder),
                    createResponse(responseRecorder),
                    exportCms,
                    data));

            byte[] expectedContent = (new CmsImageScaler(scaleParams)).scaleImage(cms.readFile(resourcename));
            assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), Integer.valueOf(responseRecorder.getStatus()));
            assertEquals(Integer.valueOf(expectedContent.length), Integer.valueOf(responseRecorder.getContentLength()));
            assertEquals("image/gif", responseRecorder.getContentType());
            assertEquals(Arrays.toString(expectedContent), Arrays.toString(responseRecorder.getBody()));
            assertEquals(getImageCacheETag(store), responseRecorder.getHeader(CmsRequestUtil.HEADER_ETAG));
            assertEquals(Integer.valueOf(1), Integer.valueOf(store.m_content.size()));

            String etag = getImageCacheETag(store);
            store.resetReadCounters();
            RequestRecorder revalidationRequest = new RequestRecorder(scaleParams);
            revalidationRequest.setHeader(CmsRequestUtil.HEADER_IF_NONE_MATCH, etag);
            ResponseRecorder revalidationResponse = new ResponseRecorder();
            assertTrue(
                OpenCms.getStaticExportManager().tryExportImageCache(
                    createRequest(revalidationRequest),
                    createResponse(revalidationResponse),
                    exportCms,
                    data));
            assertEquals(
                Integer.valueOf(HttpServletResponse.SC_NOT_MODIFIED),
                Integer.valueOf(revalidationResponse.getStatus()));
            assertEquals(etag, revalidationResponse.getHeader(CmsRequestUtil.HEADER_ETAG));
            assertEquals(Integer.valueOf(0), Integer.valueOf(revalidationResponse.getBody().length));
            assertEquals(Integer.valueOf(0), Integer.valueOf(store.m_existsCalls));
            assertEquals(Integer.valueOf(0), Integer.valueOf(store.m_lengthCalls));
            assertEquals(Integer.valueOf(0), Integer.valueOf(store.m_rangeReadCalls));
            assertEquals(Integer.valueOf(0), Integer.valueOf(store.m_readCalls));

            assertFalse(
                OpenCms.getStaticExportManager().tryExportImageCache(
                    createRequest(new RequestRecorder(null)),
                    createResponse(new ResponseRecorder()),
                    exportCms,
                    new CmsStaticExportData(rootPath, rootPath, imageFile, null)));

            RequestRecorder postRequest = new RequestRecorder(scaleParams);
            postRequest.setMethod("POST");
            assertFalse(
                OpenCms.getStaticExportManager().tryExportImageCache(
                    createRequest(postRequest),
                    createResponse(new ResponseRecorder()),
                    exportCms,
                    data));
        } finally {
            storeField.set(loader, originalStore);
        }
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
            requestRecorder.setHeader(CmsRequestUtil.HEADER_RANGE, "bytes=3-12");
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
            assertEquals(null, responseRecorder.getHeader(CmsRequestUtil.HEADER_CONTENT_RANGE));
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

            RequestRecorder headRequestRecorder = new RequestRecorder(scaleParams);
            headRequestRecorder.setMethod("HEAD");
            headRequestRecorder.setHeader(CmsRequestUtil.HEADER_RANGE, "bytes=3-12");
            ResponseRecorder headResponseRecorder = new ResponseRecorder();

            int headStatus = OpenCms.getStaticExportManager().export(
                createRequest(headRequestRecorder),
                createResponse(headResponseRecorder),
                exportCms,
                data);

            assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), Integer.valueOf(headStatus));
            assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), Integer.valueOf(headResponseRecorder.getStatus()));
            assertEquals(
                Integer.valueOf(expectedContent.length),
                Integer.valueOf(headResponseRecorder.getContentLength()));
            assertEquals(null, headResponseRecorder.getHeader(CmsRequestUtil.HEADER_CONTENT_RANGE));
            assertEquals(Integer.valueOf(0), Integer.valueOf(headResponseRecorder.getBody().length));
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
     * Tests shared-cache delivery of a scaled image through the classic local image cache.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testExportScaledImageUsesSharedCacheWithoutLocalExport() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing shared-cache delivery of scaled image from local image cache");

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
        try (AutoCloseable sharedCache = configureSharedCacheDelivery(true)) {
            storeField.set(loader, null);
            CmsStaticExportData data = new CmsStaticExportData(
                rootPath,
                rootPath,
                imageFile,
                CmsImageScaler.PARAM_SCALE + "=" + scaleParams);
            CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            ResponseRecorder responseRecorder = new ResponseRecorder();

            int status = OpenCms.getStaticExportManager().export(
                createRequest(new RequestRecorder(scaleParams)),
                createResponse(responseRecorder),
                exportCms,
                data);

            byte[] expectedContent = (new CmsImageScaler(scaleParams)).scaleImage(cms.readFile(resourcename));
            assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), Integer.valueOf(status));
            assertEquals(Arrays.toString(expectedContent), Arrays.toString(responseRecorder.getBody()));
            assertFalse(exportFile.exists());
            assertTrue(hasCacheFile(new File(CmsImageLoader.getImageRepositoryPath()), expectedContent));
            assertEquals(
                "public, max-age=60, s-maxage=86400, stale-if-error=604800",
                responseRecorder.getHeader(CmsRequestUtil.HEADER_CACHE_CONTROL));
        } finally {
            storeField.set(loader, originalStore);
        }
    }

    /** Tests self-healing after an external image cache entry disappears before GET or range delivery. */
    @Test
    public void testExternalImageCacheSelfHealingForGetAndRange() throws Throwable {

        CmsObject cms = getCmsObject();
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
            assertTrue(
                OpenCms.getStaticExportManager().tryExportImageCache(
                    createRequest(new RequestRecorder(scaleParams)),
                    createResponse(new ResponseRecorder()),
                    exportCms,
                    data));
            byte[] expectedContent = (new CmsImageScaler(scaleParams)).scaleImage(cms.readFile(resourcename));

            store.resetReadCounters();
            store.m_missingReadFailures = 1;
            ResponseRecorder getResponse = new ResponseRecorder();
            assertTrue(
                OpenCms.getStaticExportManager().tryExportImageCache(
                    createRequest(new RequestRecorder(scaleParams)),
                    createResponse(getResponse),
                    exportCms,
                    data));
            assertEquals(Arrays.toString(expectedContent), Arrays.toString(getResponse.getBody()));
            assertEquals(Integer.valueOf(2), Integer.valueOf(store.m_readCalls));
            assertEquals(Integer.valueOf(2), Integer.valueOf(store.m_writeCalls));

            store.resetReadCounters();
            store.m_missingRangeReadFailures = 1;
            RequestRecorder rangeRequest = new RequestRecorder(scaleParams);
            rangeRequest.setHeader(CmsRequestUtil.HEADER_RANGE, "bytes=3-12");
            ResponseRecorder rangeResponse = new ResponseRecorder();
            assertTrue(
                OpenCms.getStaticExportManager().tryExportImageCache(
                    createRequest(rangeRequest),
                    createResponse(rangeResponse),
                    exportCms,
                    data));
            assertEquals(
                Arrays.toString(Arrays.copyOfRange(expectedContent, 3, 13)),
                Arrays.toString(rangeResponse.getBody()));
            assertEquals(Integer.valueOf(2), Integer.valueOf(store.m_rangeReadCalls));
            assertEquals(Integer.valueOf(3), Integer.valueOf(store.m_writeCalls));

            store.resetReadCounters();
            store.m_missingReadFailures = 2;
            int writeCallsBeforeRetry = store.m_writeCalls;
            assertThrows(
                CmsImageCacheEntryNotFoundException.class,
                () -> OpenCms.getStaticExportManager().tryExportImageCache(
                    createRequest(new RequestRecorder(scaleParams)),
                    createResponse(new ResponseRecorder()),
                    exportCms,
                    data));
            assertEquals(Integer.valueOf(2), Integer.valueOf(store.m_readCalls));
            assertEquals(Integer.valueOf(writeCallsBeforeRetry + 1), Integer.valueOf(store.m_writeCalls));

            store.resetReadCounters();
            store.m_readFailures = 1;
            int writeCallsBeforeFailure = store.m_writeCalls;
            assertThrows(
                IOException.class,
                () -> OpenCms.getStaticExportManager().tryExportImageCache(
                    createRequest(new RequestRecorder(scaleParams)),
                    createResponse(new ResponseRecorder()),
                    exportCms,
                    data));
            assertEquals(Integer.valueOf(1), Integer.valueOf(store.m_readCalls));
            assertEquals(Integer.valueOf(writeCallsBeforeFailure), Integer.valueOf(store.m_writeCalls));
        } finally {
            storeField.set(loader, originalStore);
        }
    }

    /** Tests that HEAD revalidates cached positive metadata and regenerates a missing derivative. */
    @Test
    public void testExternalImageCacheSelfHealingForHead() throws Throwable {

        CmsObject cms = getCmsObject();
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
            assertTrue(
                OpenCms.getStaticExportManager().tryExportImageCache(
                    createRequest(new RequestRecorder(scaleParams)),
                    createResponse(new ResponseRecorder()),
                    exportCms,
                    data));

            store.m_missingAuthoritativeCheck = true;
            RequestRecorder headRequest = new RequestRecorder(scaleParams);
            headRequest.setMethod("HEAD");
            ResponseRecorder headResponse = new ResponseRecorder();
            assertTrue(
                OpenCms.getStaticExportManager().tryExportImageCache(
                    createRequest(headRequest),
                    createResponse(headResponse),
                    exportCms,
                    data));

            assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), Integer.valueOf(headResponse.getStatus()));
            assertEquals(Integer.valueOf(0), Integer.valueOf(headResponse.getBody().length));
            assertEquals(Integer.valueOf(1), Integer.valueOf(store.m_authoritativeExistsCalls));
            assertEquals(Integer.valueOf(2), Integer.valueOf(store.m_writeCalls));
        } finally {
            storeField.set(loader, originalStore);
        }
    }

    /**
     * Tests that shared-cache ETag revalidation does not access the configured image cache storage.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testSharedCacheImageRevalidationDoesNotAccessImageCache() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing shared-cache image revalidation without image cache storage access");

        String resourcename = "/folder1/image1.gif";
        String scaleParams = "cx:5,cy:5,ch:10,cw:10,t:0,h:40,w:40,transparent";
        CmsFile imageFile = cms.readFile(resourcename);
        String rootPath = cms.getRequestContext().addSiteRoot(resourcename);

        CmsImageLoader loader = (CmsImageLoader)OpenCms.getResourceManager().getLoader(imageFile);
        Field storeField = CmsImageLoader.class.getDeclaredField("m_imageCache");
        storeField.setAccessible(true);
        Object originalStore = storeField.get(loader);
        TestImageCache store = new TestImageCache();
        try (AutoCloseable sharedCache = configureSharedCacheDelivery(true)) {
            storeField.set(loader, store);
            CmsStaticExportData data = new CmsStaticExportData(
                rootPath,
                rootPath,
                imageFile,
                CmsImageScaler.PARAM_SCALE + "=" + scaleParams);
            CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());

            ResponseRecorder initialResponse = new ResponseRecorder();
            int initialStatus = OpenCms.getStaticExportManager().export(
                createRequest(new RequestRecorder(scaleParams)),
                createResponse(initialResponse),
                exportCms,
                data);
            String etag = getImageCacheETag(store);
            assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), Integer.valueOf(initialStatus));
            assertEquals(etag, initialResponse.getHeader(CmsRequestUtil.HEADER_ETAG));

            // Simulate image-cache eviction: A cache or client which still owns the validated representation can
            // revalidate it from the deterministic key without forcing OpenCms to recreate or inspect the derivative.
            store.m_content.clear();
            store.resetReadCounters();
            RequestRecorder revalidationRequest = new RequestRecorder(scaleParams);
            revalidationRequest.setHeader(CmsRequestUtil.HEADER_IF_NONE_MATCH, etag);
            ResponseRecorder revalidationResponse = new ResponseRecorder();
            int revalidationStatus = OpenCms.getStaticExportManager().export(
                createRequest(revalidationRequest),
                createResponse(revalidationResponse),
                exportCms,
                data);

            assertEquals(Integer.valueOf(HttpServletResponse.SC_NOT_MODIFIED), Integer.valueOf(revalidationStatus));
            assertEquals(
                Integer.valueOf(HttpServletResponse.SC_NOT_MODIFIED),
                Integer.valueOf(revalidationResponse.getStatus()));
            assertEquals(etag, revalidationResponse.getHeader(CmsRequestUtil.HEADER_ETAG));
            assertEquals(Integer.valueOf(0), Integer.valueOf(revalidationResponse.getBody().length));
            assertEquals(Integer.valueOf(0), Integer.valueOf(store.m_existsCalls));
            assertEquals(Integer.valueOf(0), Integer.valueOf(store.m_lengthCalls));
            assertEquals(Integer.valueOf(0), Integer.valueOf(store.m_rangeReadCalls));
            assertEquals(Integer.valueOf(0), Integer.valueOf(store.m_readCalls));

            store.resetReadCounters();
            RequestRecorder precedenceRequest = new RequestRecorder(scaleParams);
            precedenceRequest.setHeader(CmsRequestUtil.HEADER_IF_NONE_MATCH, "\"different\"");
            precedenceRequest.setDateHeader(
                CmsRequestUtil.HEADER_IF_MODIFIED_SINCE,
                (imageFile.getDateLastModified() / 1000) * 1000);
            ResponseRecorder precedenceResponse = new ResponseRecorder();
            int precedenceStatus = OpenCms.getStaticExportManager().export(
                createRequest(precedenceRequest),
                createResponse(precedenceResponse),
                exportCms,
                data);

            assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), Integer.valueOf(precedenceStatus));
            assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), Integer.valueOf(precedenceResponse.getStatus()));
            assertEquals(Integer.valueOf(1), Integer.valueOf(store.m_existsCalls));
            assertEquals(Integer.valueOf(1), Integer.valueOf(store.m_lengthCalls));
            assertEquals(Integer.valueOf(0), Integer.valueOf(store.m_rangeReadCalls));
            assertEquals(Integer.valueOf(1), Integer.valueOf(store.m_readCalls));
        } finally {
            storeField.set(loader, originalStore);
        }
    }

    /**
     * Configures shared-cache delivery for the current test and returns a reset hook.<p>
     *
     * @param enabled if shared-cache delivery should be enabled
     *
     * @return a reset hook
     */
    private AutoCloseable configureSharedCacheDelivery(boolean enabled) {

        CmsStaticExportManager manager = OpenCms.getStaticExportManager();
        CmsSharedCacheConfiguration originalConfiguration = manager.getSharedCacheConfiguration();
        CmsSharedCacheConfiguration configuration = new CmsSharedCacheConfiguration();
        configuration.setEnabled(String.valueOf(enabled));
        CmsSharedCachePolicy defaultPolicy = new CmsSharedCachePolicy();
        defaultPolicy.setClientMaxAge("0");
        defaultPolicy.setSharedMaxAge("3600");
        configuration.addCachePolicy(defaultPolicy);
        CmsSharedCachePolicy imagePolicy = new CmsSharedCachePolicy();
        imagePolicy.setContentType("image/*");
        imagePolicy.setClientMaxAge("60");
        imagePolicy.setSharedMaxAge("86400");
        imagePolicy.setStaleIfError("604800");
        configuration.addCachePolicy(imagePolicy);
        manager.setSharedCacheConfiguration(configuration);
        return new AutoCloseable() {

            public void close() {

                manager.setSharedCacheConfiguration(originalConfiguration);
            }
        };
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

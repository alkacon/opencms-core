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

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.configuration.CmsStoragePolicyConfiguration;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.I_CmsVfsDriver;
import org.opencms.db.generic.CmsDbStorage;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.db.storage.CmsStorageException;
import org.opencms.db.storage.CmsStorageManager;
import org.opencms.db.storage.I_CmsStorageDelivery;
import org.opencms.db.storage.policy.CmsDefaultStoragePolicy;
import org.opencms.db.storage.policy.CmsNoExternalStoragePolicy;
import org.opencms.db.storage.policy.CmsStoragePolicyContext;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.I_CmsFileContentStreamHandler;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.loader.CmsDumpLoader;
import org.opencms.loader.CmsImageLoader;
import org.opencms.loader.I_CmsStoredContentDirectDeliveryLoader;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.OpenCms;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.test.OpenCmsTestRunner;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsRequestUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Tests static export for binary resources stored in the storage layer.<p>
 */
public class TestExportStoredBinary extends OpenCmsTestRunner {

    /**
     * Counting storage implementation used to verify that static export streams stored binaries.<p>
     */
    public static class CountingDbStorage extends CmsDbStorage {

        /** Counter for loadContent calls. */
        private static final AtomicInteger LOAD_CONTENT_CALLS = new AtomicInteger();

        /** Counter for loadContentTo calls. */
        private static final AtomicInteger LOAD_CONTENT_TO_CALLS = new AtomicInteger();

        /** Counter for loadContentFrom calls. */
        private static final AtomicInteger LOAD_CONTENT_FROM_CALLS = new AtomicInteger();

        /**
         * Public constructor.<p>
         *
         * @param sqlManager the SQL manager
         */
        public CountingDbStorage(CmsSqlManager sqlManager) {

            super(sqlManager);
        }

        /**
         * Returns the number of loadContent calls.<p>
         *
         * @return the call count
         */
        public static int getLoadContentCalls() {

            return LOAD_CONTENT_CALLS.get();
        }

        /**
         * Returns the number of loadContentFrom calls.<p>
         *
         * @return the call count
         */
        public static int getLoadContentFromCalls() {

            return LOAD_CONTENT_FROM_CALLS.get();
        }

        /**
         * Returns the number of loadContentTo calls.<p>
         *
         * @return the call count
         */
        public static int getLoadContentToCalls() {

            return LOAD_CONTENT_TO_CALLS.get();
        }

        /**
         * Resets the counters.<p>
         */
        public static void reset() {

            LOAD_CONTENT_CALLS.set(0);
            LOAD_CONTENT_TO_CALLS.set(0);
            LOAD_CONTENT_FROM_CALLS.set(0);
        }

        /**
         * @see org.opencms.db.generic.CmsDbStorage#loadContent(CmsDbContext, String)
         */
        @Override
        public byte[] loadContent(CmsDbContext dbc, String hash) throws Exception {

            LOAD_CONTENT_CALLS.incrementAndGet();
            return super.loadContent(dbc, hash);
        }

        /**
         * @see org.opencms.db.generic.CmsDbStorage#loadContentFrom(CmsDbContext, String, I_CmsFileContentStreamHandler)
         */
        @Override
        public void loadContentFrom(CmsDbContext dbc, String hash, I_CmsFileContentStreamHandler handler)
        throws Exception {

            LOAD_CONTENT_FROM_CALLS.incrementAndGet();
            super.loadContentFrom(dbc, hash, handler);
        }

        /**
         * @see org.opencms.db.generic.CmsDbStorage#loadContentTo(CmsDbContext, String, OutputStream)
         */
        @Override
        public void loadContentTo(CmsDbContext dbc, String hash, OutputStream out) throws Exception {

            LOAD_CONTENT_TO_CALLS.incrementAndGet();
            super.loadContentTo(dbc, hash, out);
        }
    }

    /**
     * Request recorder.<p>
     */
    private static class RequestRecorder implements InvocationHandler {

        /** Headers. */
        private Map<String, String> m_headers = new HashMap<String, String>();

        /** Parameters. */
        private Map<String, String> m_parameters = new HashMap<String, String>();

        /** The request method. */
        private String m_method = "GET";

        /**
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(Object proxy, Method method, Object[] args) {

            String methodName = method.getName();
            if ("getHeader".equals(methodName)) {
                return m_headers.get(args[0]);
            } else if ("getDateHeader".equals(methodName)) {
                String value = m_headers.get(args[0]);
                return Long.valueOf(value == null ? -1 : Long.parseLong(value));
            } else if ("getMethod".equals(methodName)) {
                return m_method;
            } else if ("getParameter".equals(methodName)) {
                return m_parameters.get(args[0]);
            }
            return defaultValue(method.getReturnType());
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

        /**
         * Sets a request parameter.<p>
         *
         * @param name the parameter name
         * @param value the parameter value
         */
        void setParameter(String name, String value) {

            m_parameters.put(name, value);
        }
    }

    /**
     * Response recorder.<p>
     */
    private static class ResponseRecorder implements InvocationHandler {

        /** The response body. */
        private ByteArrayOutputStream m_body = new ByteArrayOutputStream();

        /** The content length. */
        private int m_contentLength = -1;

        /** Headers. */
        private Map<String, String> m_headers = new HashMap<String, String>();

        /** The response status. */
        private int m_status = -1;

        /**
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(Object proxy, Method method, Object[] args) {

            String methodName = method.getName();
            if ("setStatus".equals(methodName)) {
                m_status = ((Integer)args[0]).intValue();
                return null;
            } else if ("setContentLength".equals(methodName)) {
                m_contentLength = ((Integer)args[0]).intValue();
                return null;
            } else if ("setHeader".equals(methodName) || "addHeader".equals(methodName)) {
                m_headers.put((String)args[0], String.valueOf(args[1]));
                return null;
            } else if ("setDateHeader".equals(methodName)) {
                m_headers.put((String)args[0], String.valueOf(args[1]));
                return null;
            } else if ("containsHeader".equals(methodName)) {
                return Boolean.valueOf(m_headers.containsKey(args[0]));
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
                    public void write(byte[] buffer, int offset, int length) {

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
         * Returns the content length.<p>
         *
         * @return the content length
         */
        int getContentLength() {

            return m_contentLength;
        }

        /**
         * Returns a response header.<p>
         *
         * @param name the header name
         *
         * @return the header value
         */
        String getHeader(String name) {

            return m_headers.get(name);
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

    /**
     * Test storage manager exposing an S3-like delivery-capable backend.<p>
     */
    private static class TestDeliveryStorageManager extends CmsStorageManager {

        /** The stored content. */
        private Map<String, byte[]> m_contents = new HashMap<String, byte[]>();

        /** If content was loaded through the classic stream path. */
        private boolean m_loadedContentTo;

        /** If full delivery streaming was used. */
        private boolean m_streamedFull;

        /** If range delivery streaming was used. */
        private boolean m_streamedRange;

        /** The storage identifier allowed for direct delivery. */
        private String m_deliveryStorage;

        /**
         * Creates a new test storage manager.<p>
         *
         * @param sqlManager the SQL manager
         */
        TestDeliveryStorageManager(CmsSqlManager sqlManager) {

            this(sqlManager, "s3test");
        }

        /**
         * Creates a new test storage manager.<p>
         *
         * @param sqlManager the SQL manager
         * @param deliveryStorage the storage identifier allowed for direct delivery
         */
        TestDeliveryStorageManager(CmsSqlManager sqlManager, String deliveryStorage) {

            super(sqlManager, createDeliveryStorageConfiguration(), createDeliveryStoragePolicyConfiguration());
            m_deliveryStorage = deliveryStorage;
        }

        /**
         * Calculates a SHA-512 hash.<p>
         *
         * @param content the content
         *
         * @return the hash
         *
         * @throws Exception if hashing fails
         */
        private static String calculateSha512(byte[] content) throws Exception {

            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(content);
            StringBuilder result = new StringBuilder(hash.length * 2);
            for (int i = 0; i < hash.length; i++) {
                int value = hash[i] & 0xff;
                if (value < 16) {
                    result.append('0');
                }
                result.append(Integer.toHexString(value));
            }
            return result.toString();
        }

        /**
         * @see org.opencms.db.storage.CmsStorageManager#getDeliveryStorage(java.lang.String)
         */
        @Override
        public I_CmsStorageDelivery getDeliveryStorage(String storage) throws CmsStorageException {

            if ((m_deliveryStorage == null) || !m_deliveryStorage.equals(storage)) {
                return null;
            }
            return new I_CmsStorageDelivery() {

                public void streamRangeTo(CmsDbContext dbc, String hash, long start, long length, OutputStream out)
                throws Exception {

                    m_streamedRange = true;
                    byte[] content = m_contents.get(hash);
                    out.write(content, (int)start, (int)length);
                }

                public void streamTo(CmsDbContext dbc, String hash, OutputStream out) throws Exception {

                    m_streamedFull = true;
                    out.write(m_contents.get(hash));
                }

                public boolean supportsRangeDelivery() {

                    return true;
                }
            };
        }

        /**
         * @see org.opencms.db.storage.CmsStorageManager#loadContent(org.opencms.db.CmsDbContext, byte[], java.lang.String, java.lang.String)
         */
        @Override
        public byte[] loadContent(CmsDbContext dbc, byte[] contents, String storage, String hash)
        throws CmsStorageException {

            return m_contents.get(hash);
        }

        /**
         * @see org.opencms.db.storage.CmsStorageManager#loadContentFrom(org.opencms.db.CmsDbContext, byte[], java.lang.String, java.lang.String, org.opencms.file.I_CmsFileContentStreamHandler)
         */
        @Override
        public void loadContentFrom(
            CmsDbContext dbc,
            byte[] contents,
            String storage,
            String hash,
            I_CmsFileContentStreamHandler handler)
        throws CmsStorageException {

            try {
                handler.read(new ByteArrayInputStream(m_contents.get(hash)));
            } catch (Exception e) {
                throw new CmsStorageException("Failed to read test content.", e);
            }
        }

        /**
         * @see org.opencms.db.storage.CmsStorageManager#loadContentTo(org.opencms.db.CmsDbContext, byte[], java.lang.String, java.lang.String, java.io.OutputStream)
         */
        @Override
        public void loadContentTo(CmsDbContext dbc, byte[] contents, String storage, String hash, OutputStream out)
        throws CmsStorageException {

            m_loadedContentTo = true;
            try {
                out.write(m_contents.get(hash));
            } catch (Exception e) {
                throw new CmsStorageException("Failed to write test content.", e);
            }
        }

        /**
         * @see org.opencms.db.storage.CmsStorageManager#prepareContent(org.opencms.db.CmsDbContext, org.opencms.db.storage.policy.CmsStoragePolicyContext)
         */
        @Override
        public StorageResult prepareContent(CmsDbContext dbc, CmsStoragePolicyContext context)
        throws CmsDataAccessException {

            try {
                byte[] content = context.getContent();
                String hash = calculateSha512(content);
                m_contents.put(hash, content);
                return new StorageResult("s3test", hash);
            } catch (Exception e) {
                throw new CmsDataAccessException(
                    org.opencms.db.Messages.get().container(org.opencms.db.Messages.ERR_UNKNOWN_POOL_URL_1, "s3test"),
                    e);
            }
        }
    }

    /** Test content size. */
    private static final int LARGE_CONTENT_SIZE = 600 * 1024;

    /**
     * Creates the configuration for the delivery test storage manager.<p>
     *
     * @return the configuration
     */
    private static CmsParameterConfiguration createDeliveryStorageConfiguration() {

        CmsParameterConfiguration configuration = new CmsParameterConfiguration();
        configuration.add("storage.active", "db");
        return configuration;
    }

    /**
     * Creates the storage policy configuration for the delivery test storage manager.<p>
     *
     * @return the policy configuration
     */
    private static CmsStoragePolicyConfiguration createDeliveryStoragePolicyConfiguration() {

        CmsStoragePolicyConfiguration configuration = new CmsStoragePolicyConfiguration();
        configuration.setClassName(CmsNoExternalStoragePolicy.class.getName());
        return configuration;
    }

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

        setupOpenCms(testInfo, "simpletest", "/", "WEB-INF/config.storage-regression/");
    }

    /**
     * Tests that non dump-loader resources still use the classic static export path.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testNonDumpLoaderResourceKeepsClassicStaticExportPath() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing classic static export for non dump-loader resource");

        Field storageManagerField = getField(getVfsDriver().getClass(), "m_storageManager");
        CmsStorageManager originalStorageManager = (CmsStorageManager)storageManagerField.get(getVfsDriver());
        TestDeliveryStorageManager deliveryStorageManager = null;
        try {
            deliveryStorageManager = new TestDeliveryStorageManager(getStorageSqlManager(originalStorageManager));
            storageManagerField.set(getVfsDriver(), deliveryStorageManager);

            CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            exportCms.getRequestContext().setCurrentProject(exportCms.readProject("Online"));
            String resourceName = "/index.html";
            String rootPath = cms.getRequestContext().addSiteRoot(resourceName);
            CmsResource onlineResource = exportCms.readResource(rootPath);
            CmsStaticExportData data = new CmsStaticExportData(rootPath, rootPath, onlineResource, null);

            OpenCms.getStaticExportManager().export(null, null, exportCms, data);

            assertFalse(OpenCms.getResourceManager().getLoader(onlineResource) instanceof CmsDumpLoader);
            assertFalse(deliveryStorageManager.m_streamedFull);
            assertFalse(deliveryStorageManager.m_streamedRange);
            assertFalse(deliveryStorageManager.m_loadedContentTo);
        } finally {
            storageManagerField.set(getVfsDriver(), originalStorageManager);
            if (deliveryStorageManager != null) {
                deliveryStorageManager.close();
            }
        }
    }

    /**
     * Tests that offline stored binaries can be delivered with range support through the dump loader.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testOfflineProtectedStoredBinaryRangeDeliveryForAuthorizedWorkplaceUser() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing offline range delivery for protected stored binary resource");

        Field storageManagerField = getField(getVfsDriver().getClass(), "m_storageManager");
        CmsStorageManager originalStorageManager = (CmsStorageManager)storageManagerField.get(getVfsDriver());
        TestDeliveryStorageManager deliveryStorageManager = null;
        try {
            deliveryStorageManager = new TestDeliveryStorageManager(getStorageSqlManager(originalStorageManager));
            storageManagerField.set(getVfsDriver(), deliveryStorageManager);

            String resourceName = "/stored-binary-offline-workplace-audio.mp3";
            byte[] content = "0123456789abcdef".getBytes("UTF-8");
            CmsResource resource = cms.createResource(
                resourceName,
                CmsResourceTypeBinary.getStaticTypeId(),
                content,
                null);
            cms.chacc(resourceName, I_CmsPrincipal.PRINCIPAL_USER, "test2", "-r");
            cms.unlockResource(resourceName);

            RequestRecorder requestRecorder = new RequestRecorder();
            requestRecorder.setHeader(CmsRequestUtil.HEADER_RANGE, "bytes=4-9");
            ResponseRecorder responseRecorder = new ResponseRecorder();
            CmsDumpLoader loader = (CmsDumpLoader)OpenCms.getResourceManager().getLoader(resource);

            loader.load(cms, resource, createRequest(requestRecorder), createResponse(responseRecorder));

            assertEquals(
                Integer.valueOf(HttpServletResponse.SC_PARTIAL_CONTENT),
                Integer.valueOf(responseRecorder.getStatus()));
            assertEquals(Integer.valueOf(6), Integer.valueOf(responseRecorder.getContentLength()));
            assertEquals("bytes 4-9/16", responseRecorder.getHeader(CmsRequestUtil.HEADER_CONTENT_RANGE));
            assertTrue(Arrays.equals("456789".getBytes("UTF-8"), responseRecorder.getBody()));
            assertFalse(deliveryStorageManager.m_streamedFull);
            assertTrue(deliveryStorageManager.m_streamedRange);
            assertFalse(deliveryStorageManager.m_loadedContentTo);
        } finally {
            storageManagerField.set(getVfsDriver(), originalStorageManager);
            if (deliveryStorageManager != null) {
                deliveryStorageManager.close();
            }
        }
    }

    /**
     * Tests that protected stored binaries are not written to the local export folder.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testProtectedStoredBinaryDoesNotCreateLocalExportFile() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing protected stored binary static export");

        Field storageManagerField = getField(getVfsDriver().getClass(), "m_storageManager");
        CmsStorageManager originalStorageManager = (CmsStorageManager)storageManagerField.get(getVfsDriver());
        TestDeliveryStorageManager deliveryStorageManager = null;
        try {
            deliveryStorageManager = new TestDeliveryStorageManager(getStorageSqlManager(originalStorageManager));
            storageManagerField.set(getVfsDriver(), deliveryStorageManager);

            String resourceName = "/stored-binary-protected.zip";
            byte[] content = createContent((byte)88);
            CmsResource resource = cms.createResource(
                resourceName,
                CmsResourceTypeBinary.getStaticTypeId(),
                content,
                null);
            cms.chacc(resourceName, I_CmsPrincipal.PRINCIPAL_USER, OpenCms.getDefaultUsers().getUserExport(), "-r");
            cms.unlockResource(resourceName);

            OpenCms.getPublishManager().publishResource(cms, resourceName);
            OpenCms.getPublishManager().waitWhileRunning();

            CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            exportCms.getRequestContext().setCurrentProject(exportCms.readProject("Online"));
            String rootPath = cms.getRequestContext().addSiteRoot(resourceName);
            CmsStaticExportData data = new CmsStaticExportData(rootPath, rootPath, resource, null);

            int status = OpenCms.getStaticExportManager().export(null, null, exportCms, data);

            File exportFile = getExportFile(rootPath);
            assertEquals(Integer.valueOf(HttpServletResponse.SC_SEE_OTHER), Integer.valueOf(status));
            assertFalse(exportFile.exists());
            assertFalse(deliveryStorageManager.m_streamedFull);
            assertFalse(deliveryStorageManager.m_streamedRange);
            assertFalse(deliveryStorageManager.m_loadedContentTo);
        } finally {
            storageManagerField.set(getVfsDriver(), originalStorageManager);
            if (deliveryStorageManager != null) {
                deliveryStorageManager.close();
            }
        }
    }

    /**
     * Tests that stored content direct delivery is denied without read permissions.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testStoredBinaryDirectDeliveryRequiresReadPermission() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing read permission check before stored content direct delivery");

        Field storageManagerField = getField(getVfsDriver().getClass(), "m_storageManager");
        CmsStorageManager originalStorageManager = (CmsStorageManager)storageManagerField.get(getVfsDriver());
        TestDeliveryStorageManager deliveryStorageManager = null;
        try {
            deliveryStorageManager = new TestDeliveryStorageManager(getStorageSqlManager(originalStorageManager));
            storageManagerField.set(getVfsDriver(), deliveryStorageManager);

            String resourceName = "/stored-binary-denied-range.mp3";
            byte[] content = "0123456789abcdef".getBytes("UTF-8");
            CmsResource resource = cms.createResource(
                resourceName,
                CmsResourceTypeBinary.getStaticTypeId(),
                content,
                null);
            cms.chacc(resourceName, I_CmsPrincipal.PRINCIPAL_USER, "test2", "-r");
            cms.unlockResource(resourceName);

            CmsObject deniedCms = OpenCms.initCmsObject(cms, new CmsContextInfo("test2"));
            deniedCms.getRequestContext().setCurrentProject(cms.getRequestContext().getCurrentProject());
            RequestRecorder requestRecorder = new RequestRecorder();
            requestRecorder.setHeader(CmsRequestUtil.HEADER_RANGE, "bytes=4-9");
            ResponseRecorder responseRecorder = new ResponseRecorder();
            CmsDumpLoader loader = (CmsDumpLoader)OpenCms.getResourceManager().getLoader(resource);

            assertFalse(
                loader.deliverStoredContent(
                    deniedCms,
                    resource,
                    createRequest(requestRecorder),
                    createResponse(responseRecorder)));
            assertFalse(deliveryStorageManager.m_streamedFull);
            assertFalse(deliveryStorageManager.m_streamedRange);
            assertFalse(deliveryStorageManager.m_loadedContentTo);
            assertEquals(Integer.valueOf(-1), Integer.valueOf(responseRecorder.getStatus()));
            assertEquals(Integer.valueOf(0), Integer.valueOf(responseRecorder.getBody().length));
        } finally {
            storageManagerField.set(getVfsDriver(), originalStorageManager);
            if (deliveryStorageManager != null) {
                deliveryStorageManager.close();
            }
        }
    }

    /**
     * Tests that stored binaries in DB storage still use the classic export folder during export on demand.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testStoredBinaryExportOnDemandFallsBackToLocalExportForDbStorage() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing on demand static export fallback for DB stored binary resource");

        Field storageManagerField = getField(getVfsDriver().getClass(), "m_storageManager");
        CmsStorageManager originalStorageManager = (CmsStorageManager)storageManagerField.get(getVfsDriver());
        CmsStorageManager countingStorageManager = null;
        AutoCloseable directDelivery = null;
        try {
            countingStorageManager = createCountingStorageManager(getStorageSqlManager(originalStorageManager));
            storageManagerField.set(getVfsDriver(), countingStorageManager);
            directDelivery = enableStoredContentDirectDelivery();

            String resourceName = "/stored-binary-db-delivery-fallback.zip";
            byte[] content = createContent((byte)55);
            CmsResource resource = cms.createResource(
                resourceName,
                CmsResourceTypeBinary.getStaticTypeId(),
                content,
                null);
            cms.unlockResource(resourceName);

            OpenCms.getPublishManager().publishResource(cms, resourceName);
            OpenCms.getPublishManager().waitWhileRunning();

            CountingDbStorage.reset();
            CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            exportCms.getRequestContext().setCurrentProject(exportCms.readProject("Online"));
            String rootPath = cms.getRequestContext().addSiteRoot(resourceName);
            CmsResource onlineResource = exportCms.readResource(rootPath);
            CmsStaticExportData data = new CmsStaticExportData(rootPath, rootPath, onlineResource, null);
            ResponseRecorder responseRecorder = new ResponseRecorder();

            int status = OpenCms.getStaticExportManager().export(
                createRequest(new RequestRecorder()),
                createResponse(responseRecorder),
                exportCms,
                data);

            File exportFile = getExportFile(rootPath);
            assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), Integer.valueOf(status));
            assertTrue(exportFile.exists());
            assertEquals(Integer.valueOf(0), Integer.valueOf(CountingDbStorage.getLoadContentCalls()));
            assertEquals(Integer.valueOf(1), Integer.valueOf(CountingDbStorage.getLoadContentToCalls()));
            assertEquals(Integer.valueOf(content.length), Integer.valueOf((int)exportFile.length()));
            assertTrue(Arrays.equals(content, Files.readAllBytes(exportFile.toPath())));
            assertTrue(Arrays.equals(content, responseRecorder.getBody()));
        } finally {
            if (directDelivery != null) {
                directDelivery.close();
            }
            storageManagerField.set(getVfsDriver(), originalStorageManager);
            if (countingStorageManager != null) {
                countingStorageManager.close();
            }
        }
    }

    /**
     * Tests that stored content in a legacy backend keeps the classic export folder path.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testStoredBinaryExportOnDemandFallsBackToLocalExportForLegacyStorage() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing stored content direct delivery fallback for legacy storage");

        Field storageManagerField = getField(getVfsDriver().getClass(), "m_storageManager");
        CmsStorageManager originalStorageManager = (CmsStorageManager)storageManagerField.get(getVfsDriver());
        TestDeliveryStorageManager deliveryStorageManager = null;
        AutoCloseable directDelivery = null;
        try {
            deliveryStorageManager = new TestDeliveryStorageManager(getStorageSqlManager(originalStorageManager), null);
            storageManagerField.set(getVfsDriver(), deliveryStorageManager);
            directDelivery = enableStoredContentDirectDelivery();

            String resourceName = "/stored-binary-delivery-legacy.zip";
            byte[] content = createContent((byte)59);
            CmsResource resource = cms.createResource(
                resourceName,
                CmsResourceTypeBinary.getStaticTypeId(),
                content,
                null);
            cms.unlockResource(resourceName);

            OpenCms.getPublishManager().publishResource(cms, resourceName);
            OpenCms.getPublishManager().waitWhileRunning();

            CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            exportCms.getRequestContext().setCurrentProject(exportCms.readProject("Online"));
            String rootPath = cms.getRequestContext().addSiteRoot(resourceName);
            CmsResource onlineResource = exportCms.readResource(rootPath);
            CmsStaticExportData data = new CmsStaticExportData(rootPath, rootPath, onlineResource, null);
            File exportFile = getExportFile(rootPath);
            if (exportFile.exists()) {
                assertTrue(exportFile.delete());
            }
            ResponseRecorder responseRecorder = new ResponseRecorder();

            int status = OpenCms.getStaticExportManager().export(
                createRequest(new RequestRecorder()),
                createResponse(responseRecorder),
                exportCms,
                data);

            assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), Integer.valueOf(status));
            assertTrue(exportFile.exists());
            assertFalse(deliveryStorageManager.m_streamedFull);
            assertFalse(deliveryStorageManager.m_streamedRange);
            assertTrue(deliveryStorageManager.m_loadedContentTo);
            assertTrue(Arrays.equals(content, Files.readAllBytes(exportFile.toPath())));
            assertTrue(Arrays.equals(content, responseRecorder.getBody()));
        } finally {
            if (directDelivery != null) {
                directDelivery.close();
            }
            storageManagerField.set(getVfsDriver(), originalStorageManager);
            if (deliveryStorageManager != null) {
                deliveryStorageManager.close();
            }
        }
    }

    /**
     * Tests that disabled stored content delivery keeps the classic export folder path.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testStoredBinaryExportOnDemandFallsBackToLocalExportWhenDirectDeliveryIsDisabled() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing disabled stored content direct delivery configuration");

        Field storageManagerField = getField(getVfsDriver().getClass(), "m_storageManager");
        CmsStorageManager originalStorageManager = (CmsStorageManager)storageManagerField.get(getVfsDriver());
        TestDeliveryStorageManager deliveryStorageManager = null;
        AutoCloseable directDelivery = null;
        try {
            deliveryStorageManager = new TestDeliveryStorageManager(getStorageSqlManager(originalStorageManager));
            storageManagerField.set(getVfsDriver(), deliveryStorageManager);
            directDelivery = configureStoredContentDirectDelivery(false);

            String resourceName = "/stored-binary-delivery-disabled.zip";
            byte[] content = createContent((byte)57);
            CmsResource resource = cms.createResource(
                resourceName,
                CmsResourceTypeBinary.getStaticTypeId(),
                content,
                null);
            cms.unlockResource(resourceName);

            OpenCms.getPublishManager().publishResource(cms, resourceName);
            OpenCms.getPublishManager().waitWhileRunning();

            CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            exportCms.getRequestContext().setCurrentProject(exportCms.readProject("Online"));
            String rootPath = cms.getRequestContext().addSiteRoot(resourceName);
            CmsResource onlineResource = exportCms.readResource(rootPath);
            CmsStaticExportData data = new CmsStaticExportData(rootPath, rootPath, onlineResource, null);
            File exportFile = getExportFile(rootPath);
            if (exportFile.exists()) {
                assertTrue(exportFile.delete());
            }
            ResponseRecorder responseRecorder = new ResponseRecorder();

            int status = OpenCms.getStaticExportManager().export(
                createRequest(new RequestRecorder()),
                createResponse(responseRecorder),
                exportCms,
                data);

            assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), Integer.valueOf(status));
            assertTrue(exportFile.exists());
            assertFalse(deliveryStorageManager.m_streamedFull);
            assertFalse(deliveryStorageManager.m_streamedRange);
            assertTrue(deliveryStorageManager.m_loadedContentTo);
            assertTrue(Arrays.equals(content, Files.readAllBytes(exportFile.toPath())));
            assertTrue(Arrays.equals(content, responseRecorder.getBody()));
        } finally {
            if (directDelivery != null) {
                directDelivery.close();
            }
            storageManagerField.set(getVfsDriver(), originalStorageManager);
            if (deliveryStorageManager != null) {
                deliveryStorageManager.close();
            }
        }
    }

    /**
     * Tests that stored content delivery only applies to configured suffixes.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testStoredBinaryExportOnDemandFallsBackToLocalExportWhenSuffixIsDisabled() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing stored content direct delivery suffix filtering");

        Field storageManagerField = getField(getVfsDriver().getClass(), "m_storageManager");
        CmsStorageManager originalStorageManager = (CmsStorageManager)storageManagerField.get(getVfsDriver());
        TestDeliveryStorageManager deliveryStorageManager = null;
        AutoCloseable directDelivery = null;
        try {
            deliveryStorageManager = new TestDeliveryStorageManager(getStorageSqlManager(originalStorageManager));
            storageManagerField.set(getVfsDriver(), deliveryStorageManager);
            directDelivery = configureStoredContentDirectDelivery(true, ".pdf");

            String resourceName = "/stored-binary-delivery-suffix-disabled.zip";
            byte[] content = createContent((byte)58);
            CmsResource resource = cms.createResource(
                resourceName,
                CmsResourceTypeBinary.getStaticTypeId(),
                content,
                null);
            cms.unlockResource(resourceName);

            OpenCms.getPublishManager().publishResource(cms, resourceName);
            OpenCms.getPublishManager().waitWhileRunning();

            CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            exportCms.getRequestContext().setCurrentProject(exportCms.readProject("Online"));
            String rootPath = cms.getRequestContext().addSiteRoot(resourceName);
            CmsResource onlineResource = exportCms.readResource(rootPath);
            CmsStaticExportData data = new CmsStaticExportData(rootPath, rootPath, onlineResource, null);
            File exportFile = getExportFile(rootPath);
            if (exportFile.exists()) {
                assertTrue(exportFile.delete());
            }
            ResponseRecorder responseRecorder = new ResponseRecorder();

            int status = OpenCms.getStaticExportManager().export(
                createRequest(new RequestRecorder()),
                createResponse(responseRecorder),
                exportCms,
                data);

            assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), Integer.valueOf(status));
            assertTrue(exportFile.exists());
            assertFalse(deliveryStorageManager.m_streamedFull);
            assertFalse(deliveryStorageManager.m_streamedRange);
            assertTrue(deliveryStorageManager.m_loadedContentTo);
            assertTrue(Arrays.equals(content, Files.readAllBytes(exportFile.toPath())));
            assertTrue(Arrays.equals(content, responseRecorder.getBody()));
        } finally {
            if (directDelivery != null) {
                directDelivery.close();
            }
            storageManagerField.set(getVfsDriver(), originalStorageManager);
            if (deliveryStorageManager != null) {
                deliveryStorageManager.close();
            }
        }
    }

    /**
     * Tests that direct-delivery stored binaries are not written to the local export folder during export on demand.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testStoredBinaryExportOnDemandUsesDirectDelivery() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing direct delivery for stored binary resource during export on demand");

        Field storageManagerField = getField(getVfsDriver().getClass(), "m_storageManager");
        CmsStorageManager originalStorageManager = (CmsStorageManager)storageManagerField.get(getVfsDriver());
        TestDeliveryStorageManager deliveryStorageManager = null;
        AutoCloseable directDelivery = null;
        try {
            deliveryStorageManager = new TestDeliveryStorageManager(getStorageSqlManager(originalStorageManager));
            storageManagerField.set(getVfsDriver(), deliveryStorageManager);
            directDelivery = enableStoredContentDirectDelivery();

            String resourceName = "/stored-binary-delivery.zip";
            byte[] content = createContent((byte)42);
            CmsResource resource = cms.createResource(
                resourceName,
                CmsResourceTypeBinary.getStaticTypeId(),
                content,
                null);
            cms.unlockResource(resourceName);

            OpenCms.getPublishManager().publishResource(cms, resourceName);
            OpenCms.getPublishManager().waitWhileRunning();

            CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            exportCms.getRequestContext().setCurrentProject(exportCms.readProject("Online"));
            String rootPath = cms.getRequestContext().addSiteRoot(resourceName);
            CmsResource onlineResource = exportCms.readResource(rootPath);
            CmsStaticExportData data = new CmsStaticExportData(rootPath, rootPath, onlineResource, null);
            File exportFile = getExportFile(rootPath);
            createStaleExportFile(exportFile);
            Field exportHeadersField = getField(CmsStaticExportManager.class, "m_exportHeaders");
            @SuppressWarnings("unchecked")
            List<String> exportHeaders = (List<String>)exportHeadersField.get(OpenCms.getStaticExportManager());
            List<String> originalExportHeaders = new ArrayList<String>(exportHeaders);

            try {
                exportHeaders.add("X-Test-Export:direct");
                ResponseRecorder responseRecorder = new ResponseRecorder();
                int status = OpenCms.getStaticExportManager().export(
                    createRequest(new RequestRecorder()),
                    createResponse(responseRecorder),
                    exportCms,
                    data);

                assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), Integer.valueOf(status));
                assertFalse(exportFile.exists());
                assertTrue(deliveryStorageManager.m_streamedFull);
                assertFalse(deliveryStorageManager.m_streamedRange);
                assertFalse(deliveryStorageManager.m_loadedContentTo);
                assertEquals(Integer.valueOf(content.length), Integer.valueOf(responseRecorder.getContentLength()));
                assertEquals("direct", responseRecorder.getHeader("X-Test-Export"));
                assertTrue(Arrays.equals(content, responseRecorder.getBody()));
            } finally {
                exportHeaders.clear();
                exportHeaders.addAll(originalExportHeaders);
            }
        } finally {
            if (directDelivery != null) {
                directDelivery.close();
            }
            storageManagerField.set(getVfsDriver(), originalStorageManager);
            if (deliveryStorageManager != null) {
                deliveryStorageManager.close();
            }
        }
    }

    /**
     * Tests that HEAD requests during export on demand reach direct delivery without streaming a response body.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testStoredBinaryExportOnDemandUsesDirectDeliveryForHeadRequest() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing direct HEAD delivery for stored binary resource during export on demand");

        Field storageManagerField = getField(getVfsDriver().getClass(), "m_storageManager");
        CmsStorageManager originalStorageManager = (CmsStorageManager)storageManagerField.get(getVfsDriver());
        TestDeliveryStorageManager deliveryStorageManager = null;
        AutoCloseable directDelivery = null;
        try {
            deliveryStorageManager = new TestDeliveryStorageManager(getStorageSqlManager(originalStorageManager));
            storageManagerField.set(getVfsDriver(), deliveryStorageManager);
            directDelivery = enableStoredContentDirectDelivery();

            String resourceName = "/stored-binary-delivery-head.zip";
            byte[] content = createContent((byte)43);
            CmsResource resource = cms.createResource(
                resourceName,
                CmsResourceTypeBinary.getStaticTypeId(),
                content,
                null);
            cms.unlockResource(resourceName);

            OpenCms.getPublishManager().publishResource(cms, resourceName);
            OpenCms.getPublishManager().waitWhileRunning();

            CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            exportCms.getRequestContext().setCurrentProject(exportCms.readProject("Online"));
            String rootPath = cms.getRequestContext().addSiteRoot(resourceName);
            CmsResource onlineResource = exportCms.readResource(rootPath);
            CmsStaticExportData data = new CmsStaticExportData(rootPath, rootPath, onlineResource, null);
            File exportFile = getExportFile(rootPath);
            createStaleExportFile(exportFile);
            RequestRecorder requestRecorder = new RequestRecorder();
            requestRecorder.setMethod("HEAD");
            ResponseRecorder responseRecorder = new ResponseRecorder();

            int status = OpenCms.getStaticExportManager().export(
                createRequest(requestRecorder),
                createResponse(responseRecorder),
                exportCms,
                data);

            assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), Integer.valueOf(status));
            assertFalse(exportFile.exists());
            assertFalse(deliveryStorageManager.m_streamedFull);
            assertFalse(deliveryStorageManager.m_streamedRange);
            assertFalse(deliveryStorageManager.m_loadedContentTo);
            assertEquals(Integer.valueOf(content.length), Integer.valueOf(responseRecorder.getContentLength()));
            assertEquals(Integer.valueOf(0), Integer.valueOf(responseRecorder.getBody().length));
            assertEquals("bytes", responseRecorder.getHeader(CmsRequestUtil.HEADER_ACCEPT_RANGES));
        } finally {
            if (directDelivery != null) {
                directDelivery.close();
            }
            storageManagerField.set(getVfsDriver(), originalStorageManager);
            if (deliveryStorageManager != null) {
                deliveryStorageManager.close();
            }
        }
    }

    /**
     * Tests that range requests during export on demand are delivered through the storage delivery capability.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testStoredBinaryExportOnDemandUsesDirectDeliveryForRangeRequest() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing direct range delivery for stored binary resource during export on demand");

        Field storageManagerField = getField(getVfsDriver().getClass(), "m_storageManager");
        CmsStorageManager originalStorageManager = (CmsStorageManager)storageManagerField.get(getVfsDriver());
        TestDeliveryStorageManager deliveryStorageManager = null;
        AutoCloseable directDelivery = null;
        try {
            deliveryStorageManager = new TestDeliveryStorageManager(getStorageSqlManager(originalStorageManager));
            storageManagerField.set(getVfsDriver(), deliveryStorageManager);
            directDelivery = enableStoredContentDirectDelivery();

            String resourceName = "/stored-binary-delivery-range.zip";
            byte[] content = "0123456789abcdef".getBytes("UTF-8");
            CmsResource resource = cms.createResource(
                resourceName,
                CmsResourceTypeBinary.getStaticTypeId(),
                content,
                null);
            cms.unlockResource(resourceName);

            OpenCms.getPublishManager().publishResource(cms, resourceName);
            OpenCms.getPublishManager().waitWhileRunning();

            CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            exportCms.getRequestContext().setCurrentProject(exportCms.readProject("Online"));
            String rootPath = cms.getRequestContext().addSiteRoot(resourceName);
            CmsResource onlineResource = exportCms.readResource(rootPath);
            CmsStaticExportData data = new CmsStaticExportData(rootPath, rootPath, onlineResource, null);
            File exportFile = getExportFile(rootPath);
            createStaleExportFile(exportFile);
            RequestRecorder requestRecorder = new RequestRecorder();
            requestRecorder.setHeader(CmsRequestUtil.HEADER_RANGE, "bytes=4-9");
            ResponseRecorder responseRecorder = new ResponseRecorder();

            int status = OpenCms.getStaticExportManager().export(
                createRequest(requestRecorder),
                createResponse(responseRecorder),
                exportCms,
                data);

            assertEquals(Integer.valueOf(HttpServletResponse.SC_PARTIAL_CONTENT), Integer.valueOf(status));
            assertFalse(exportFile.exists());
            assertFalse(deliveryStorageManager.m_streamedFull);
            assertTrue(deliveryStorageManager.m_streamedRange);
            assertFalse(deliveryStorageManager.m_loadedContentTo);
            assertEquals(Integer.valueOf(6), Integer.valueOf(responseRecorder.getContentLength()));
            assertEquals("bytes 4-9/16", responseRecorder.getHeader(CmsRequestUtil.HEADER_CONTENT_RANGE));
            assertTrue(Arrays.equals("456789".getBytes("UTF-8"), responseRecorder.getBody()));
        } finally {
            if (directDelivery != null) {
                directDelivery.close();
            }
            storageManagerField.set(getVfsDriver(), originalStorageManager);
            if (deliveryStorageManager != null) {
                deliveryStorageManager.close();
            }
        }
    }

    /**
     * Tests that stored binary resources are streamed during static export.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testStoredBinaryExportUsesStreaming() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing static export streaming for stored binary resource");

        Field storageManagerField = getField(getVfsDriver().getClass(), "m_storageManager");
        CmsStorageManager originalStorageManager = (CmsStorageManager)storageManagerField.get(getVfsDriver());
        CmsStorageManager countingStorageManager = null;
        try {
            countingStorageManager = createCountingStorageManager(getStorageSqlManager(originalStorageManager));
            storageManagerField.set(getVfsDriver(), countingStorageManager);

            String resourceName = "/stored-binary.zip";
            byte[] content = createContent((byte)23);
            CmsResource resource = cms.createResource(
                resourceName,
                CmsResourceTypeBinary.getStaticTypeId(),
                content,
                null);
            cms.unlockResource(resourceName);

            OpenCms.getPublishManager().publishResource(cms, resourceName);
            OpenCms.getPublishManager().waitWhileRunning();

            CountingDbStorage.reset();

            CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            exportCms.getRequestContext().setCurrentProject(exportCms.readProject("Online"));
            String rootPath = cms.getRequestContext().addSiteRoot(resourceName);
            CmsResource onlineResource = exportCms.readResource(rootPath);
            CmsStaticExportData data = new CmsStaticExportData(rootPath, rootPath, onlineResource, null);

            int status = OpenCms.getStaticExportManager().export(null, null, exportCms, data);

            assertEquals(Integer.valueOf(200), Integer.valueOf(status));
            assertEquals(Integer.valueOf(0), Integer.valueOf(CountingDbStorage.getLoadContentCalls()));
            assertEquals(Integer.valueOf(1), Integer.valueOf(CountingDbStorage.getLoadContentToCalls()));

            String exportPath = CmsFileUtil.normalizePath(
                OpenCms.getStaticExportManager().getExportPath(rootPath) + rootPath);
            File exportFile = new File(exportPath);
            assertTrue(exportFile.exists());
            assertEquals(Integer.valueOf(content.length), Integer.valueOf((int)exportFile.length()));
            assertTrue(Arrays.equals(content, Files.readAllBytes(exportFile.toPath())));

            CountingDbStorage.reset();
            ByteArrayOutputStream streamedContent = new ByteArrayOutputStream();
            exportCms.readFileContentFrom(onlineResource, in -> CmsFileUtil.copy(in, streamedContent));
            assertEquals(Integer.valueOf(0), Integer.valueOf(CountingDbStorage.getLoadContentCalls()));
            assertEquals(Integer.valueOf(0), Integer.valueOf(CountingDbStorage.getLoadContentToCalls()));
            assertEquals(Integer.valueOf(1), Integer.valueOf(CountingDbStorage.getLoadContentFromCalls()));
            assertTrue(Arrays.equals(content, streamedContent.toByteArray()));
        } finally {
            storageManagerField.set(getVfsDriver(), originalStorageManager);
            if (countingStorageManager != null) {
                countingStorageManager.close();
            }
        }
    }

    /**
     * Tests that static export does not create local export files for direct-delivery stored binaries.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testStoredBinaryExportWithoutResponseSkipsLocalFileForDirectDelivery() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing static export skip for direct-delivery stored binary resource");

        Field storageManagerField = getField(getVfsDriver().getClass(), "m_storageManager");
        CmsStorageManager originalStorageManager = (CmsStorageManager)storageManagerField.get(getVfsDriver());
        TestDeliveryStorageManager deliveryStorageManager = null;
        AutoCloseable directDelivery = null;
        try {
            deliveryStorageManager = new TestDeliveryStorageManager(getStorageSqlManager(originalStorageManager));
            storageManagerField.set(getVfsDriver(), deliveryStorageManager);
            directDelivery = enableStoredContentDirectDelivery();

            String resourceName = "/stored-binary-delivery-rebuild.zip";
            byte[] content = createContent((byte)77);
            CmsResource resource = cms.createResource(
                resourceName,
                CmsResourceTypeBinary.getStaticTypeId(),
                content,
                null);
            cms.unlockResource(resourceName);

            OpenCms.getPublishManager().publishResource(cms, resourceName);
            OpenCms.getPublishManager().waitWhileRunning();

            CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            exportCms.getRequestContext().setCurrentProject(exportCms.readProject("Online"));
            String rootPath = cms.getRequestContext().addSiteRoot(resourceName);
            CmsResource onlineResource = exportCms.readResource(rootPath);
            CmsStaticExportData data = new CmsStaticExportData(rootPath, rootPath, onlineResource, null);
            File exportFile = getExportFile(rootPath);
            createStaleExportFile(exportFile);

            int status = OpenCms.getStaticExportManager().export(null, null, exportCms, data);

            assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), Integer.valueOf(status));
            assertFalse(exportFile.exists());
            assertFalse(deliveryStorageManager.m_streamedFull);
            assertFalse(deliveryStorageManager.m_streamedRange);
            assertFalse(deliveryStorageManager.m_loadedContentTo);
        } finally {
            if (directDelivery != null) {
                directDelivery.close();
            }
            storageManagerField.set(getVfsDriver(), originalStorageManager);
            if (deliveryStorageManager != null) {
                deliveryStorageManager.close();
            }
        }
    }

    /**
     * Tests that image resources use direct storage delivery only for the original image, not for scaled variants.<p>
     *
     * @throws Throwable if something goes wrong
     */
    @Test
    public void testStoredImageDirectDeliveryKeepsScaledVariantsOnImageLoaderPath() throws Throwable {

        CmsObject cms = getCmsObject();
        echo("Testing direct delivery for stored image resources and image loader handling for scaled variants");

        Field storageManagerField = getField(getVfsDriver().getClass(), "m_storageManager");
        CmsStorageManager originalStorageManager = (CmsStorageManager)storageManagerField.get(getVfsDriver());
        TestDeliveryStorageManager deliveryStorageManager = null;
        AutoCloseable directDelivery = null;
        try {
            deliveryStorageManager = new TestDeliveryStorageManager(getStorageSqlManager(originalStorageManager));
            storageManagerField.set(getVfsDriver(), deliveryStorageManager);
            directDelivery = configureStoredContentDirectDelivery(true, ".jpg");

            String resourceName = "/stored-image-export.jpg";
            byte[] content = readTestImageContent();
            CmsResource resource = cms.createResource(
                resourceName,
                CmsResourceTypeImage.getStaticTypeId(),
                content,
                null);
            cms.unlockResource(resourceName);

            OpenCms.getPublishManager().publishResource(cms, resourceName);
            OpenCms.getPublishManager().waitWhileRunning();

            CmsObject exportCms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserExport());
            exportCms.getRequestContext().setCurrentProject(exportCms.readProject("Online"));
            String rootPath = cms.getRequestContext().addSiteRoot(resourceName);
            CmsResource onlineResource = exportCms.readResource(rootPath);
            CmsStaticExportData data = new CmsStaticExportData(rootPath, rootPath, onlineResource, null);
            File exportFile = getExportFile(rootPath);
            if (exportFile.exists()) {
                assertTrue(exportFile.delete());
            }

            int status = OpenCms.getStaticExportManager().export(null, null, exportCms, data);

            assertEquals(Integer.valueOf(HttpServletResponse.SC_OK), Integer.valueOf(status));
            assertTrue(OpenCms.getResourceManager().getLoader(onlineResource) instanceof CmsImageLoader);
            I_CmsStoredContentDirectDeliveryLoader loader = (I_CmsStoredContentDirectDeliveryLoader)OpenCms.getResourceManager().getLoader(
                onlineResource);
            assertTrue(loader.isStoredContentDirectDeliveryEnabled(exportCms, onlineResource, null, null));
            assertFalse(exportFile.exists());
            assertFalse(deliveryStorageManager.m_streamedFull);
            assertFalse(deliveryStorageManager.m_streamedRange);

            RequestRecorder scaledRequestRecorder = new RequestRecorder();
            scaledRequestRecorder.setParameter("__scale", "w:40,h:40,t:2,q:80");
            assertFalse(
                loader.isStoredContentDirectDeliveryEnabled(
                    exportCms,
                    onlineResource,
                    createRequest(scaledRequestRecorder),
                    null));
        } finally {
            if (directDelivery != null) {
                directDelivery.close();
            }
            storageManagerField.set(getVfsDriver(), originalStorageManager);
            if (deliveryStorageManager != null) {
                deliveryStorageManager.close();
            }
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
     * Creates test content.<p>
     *
     * @param seed the byte value to fill the content with
     *
     * @return the content bytes
     */
    private byte[] createContent(byte seed) {

        byte[] content = new byte[LARGE_CONTENT_SIZE];
        Arrays.fill(content, seed);
        return content;
    }

    /**
     * Creates the counting storage manager.<p>
     *
     * @param sqlManager the SQL manager
     *
     * @return the storage manager
     */
    private CmsStorageManager createCountingStorageManager(CmsSqlManager sqlManager) {

        CmsParameterConfiguration configuration = new CmsParameterConfiguration();
        configuration.add("storage.active", "db");
        configuration.add("storage.backend.db.class", CountingDbStorage.class.getName());

        CmsStoragePolicyConfiguration policyConfiguration = new CmsStoragePolicyConfiguration();
        policyConfiguration.setClassName(CmsDefaultStoragePolicy.class.getName());
        policyConfiguration.addConfigurationParameter(CmsDefaultStoragePolicy.PARAM_THRESHOLD, "0");
        return new CmsStorageManager(sqlManager, configuration, policyConfiguration);
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
     * Creates a stale export file.<p>
     *
     * @param exportFile the file to create
     *
     * @throws Exception if writing fails
     */
    private void createStaleExportFile(File exportFile) throws Exception {

        Files.createDirectories(exportFile.toPath().getParent());
        Files.write(exportFile.toPath(), "stale".getBytes("UTF-8"));
        assertTrue(exportFile.exists());
    }

    /**
     * Enables stored content direct delivery for the current test and returns a reset hook.<p>
     *
     * @return a reset hook
     */
    private AutoCloseable enableStoredContentDirectDelivery() {

        return configureStoredContentDirectDelivery(true);
    }

    /**
     * Returns the static export file for the given root path.<p>
     *
     * @param rootPath the root path
     *
     * @return the export file
     */
    private File getExportFile(String rootPath) {

        String exportPath = CmsFileUtil.normalizePath(
            OpenCms.getStaticExportManager().getExportPath(rootPath) + rootPath);
        return new File(exportPath);
    }

    /**
     * Finds a field in a class hierarchy.<p>
     *
     * @param type the type
     * @param fieldName the field name
     *
     * @return the field
     *
     * @throws NoSuchFieldException if the field can not be found
     */
    private Field getField(Class<?> type, String fieldName) throws NoSuchFieldException {

        Class<?> currentType = type;
        while (currentType != null) {
            try {
                Field result = currentType.getDeclaredField(fieldName);
                result.setAccessible(true);
                return result;
            } catch (NoSuchFieldException e) {
                currentType = currentType.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    /**
     * Gets the SQL manager from a storage manager.<p>
     *
     * @param storageManager the storage manager
     *
     * @return the SQL manager
     *
     * @throws Exception if the field can not be read
     */
    private CmsSqlManager getStorageSqlManager(CmsStorageManager storageManager) throws Exception {

        return (CmsSqlManager)getField(CmsStorageManager.class, "m_sqlManager").get(storageManager);
    }

    /**
     * Gets the current VFS driver.<p>
     *
     * @return the VFS driver
     *
     * @throws Exception if the driver can not be accessed
     */
    private I_CmsVfsDriver getVfsDriver() throws Exception {

        CmsDriverManager driverManager = (CmsDriverManager)getField(
            OpenCms.getSqlManager().getClass(),
            "m_driverManager").get(OpenCms.getSqlManager());
        return driverManager.getVfsDriver();
    }

    /**
     * Reads valid JPEG test content.<p>
     *
     * @return the image content bytes
     *
     * @throws Exception if reading fails
     */
    private byte[] readTestImageContent() throws Exception {

        return Files.readAllBytes(new File("test/org/opencms/loader/img_01.jpg").toPath());
    }
}

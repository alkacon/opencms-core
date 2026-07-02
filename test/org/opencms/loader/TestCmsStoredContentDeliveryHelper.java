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

package org.opencms.loader;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.opencms.db.CmsDbContext;
import org.opencms.db.storage.I_CmsStorageDelivery;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsStoredContentInfo;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsUUID;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;

/**
 * Tests direct HTTP delivery for externally stored contents.<p>
 */
public class TestCmsStoredContentDeliveryHelper {

    /**
     * Request recorder.<p>
     */
    private static class RequestRecorder implements InvocationHandler {

        /** Date headers. */
        private Map<String, Long> m_dateHeaders = new HashMap<>();

        /** Headers. */
        private Map<String, String> m_headers = new HashMap<>();

        /** HTTP method. */
        private String m_method = "GET";

        /**
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(Object proxy, Method method, Object[] args) {

            String methodName = method.getName();
            if ("getHeader".equals(methodName)) {
                return m_headers.get(args[0]);
            } else if ("getDateHeader".equals(methodName)) {
                Long value = m_dateHeaders.get(args[0]);
                if (value != null) {
                    return value;
                }
                if (m_headers.containsKey(args[0])) {
                    throw new IllegalArgumentException(String.valueOf(m_headers.get(args[0])));
                }
                return Long.valueOf(-1);
            } else if ("getMethod".equals(methodName)) {
                return m_method;
            } else if ("getSession".equals(methodName)) {
                return null;
            }
            return defaultValue(method.getReturnType());
        }

        /**
         * Sets a date header.<p>
         *
         * @param name the header name
         * @param value the value
         */
        void setDateHeader(String name, long value) {

            m_dateHeaders.put(name, Long.valueOf(value));
        }

        /**
         * Sets a header.<p>
         *
         * @param name the header name
         * @param value the value
         */
        void setHeader(String name, String value) {

            m_headers.put(name, value);
        }

        /**
         * Sets the HTTP method.<p>
         *
         * @param method the HTTP method
         */
        void setMethod(String method) {

            m_method = method;
        }
    }

    /**
     * Response recorder.<p>
     */
    private static class ResponseRecorder implements InvocationHandler {

        /** Body. */
        private ByteArrayOutputStream m_body = new ByteArrayOutputStream();

        /** Date headers. */
        private Map<String, Long> m_dateHeaders = new HashMap<>();

        /** Headers. */
        private Map<String, List<String>> m_headers = new HashMap<>();

        /** Content length. */
        private int m_contentLength = -1;

        /** Status. */
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
            } else if ("setDateHeader".equals(methodName)) {
                m_dateHeaders.put((String)args[0], (Long)args[1]);
                return null;
            } else if ("setHeader".equals(methodName)) {
                List<String> values = new ArrayList<>();
                values.add(String.valueOf(args[1]));
                m_headers.put((String)args[0], values);
                return null;
            } else if ("addHeader".equals(methodName)) {
                m_headers.computeIfAbsent((String)args[0], key -> new ArrayList<>()).add(String.valueOf(args[1]));
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
         * Returns a date header.<p>
         *
         * @param name the header name
         *
         * @return the date header
         */
        long getDateHeader(String name) {

            return m_dateHeaders.get(name).longValue();
        }

        /**
         * Returns a header.<p>
         *
         * @param name the header name
         *
         * @return the header
         */
        String getHeader(String name) {

            List<String> values = m_headers.get(name);
            return ((values == null) || values.isEmpty()) ? null : values.get(0);
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
     * Test storage.<p>
     */
    private static class TestStorageDelivery implements I_CmsStorageDelivery {

        /** Content. */
        private byte[] m_content;

        /** Last hash. */
        private String m_hash;

        /** Last length. */
        private long m_length = -1;

        /** Last start. */
        private long m_start = -1;

        /** If range delivery is supported. */
        private boolean m_supportsRangeDelivery = true;

        /** If full streaming was used. */
        private boolean m_streamedFull;

        /** If range streaming was used. */
        private boolean m_streamedRange;

        /**
         * Creates a new test storage.<p>
         *
         * @param content the content
         */
        TestStorageDelivery(byte[] content) {

            m_content = content;
        }

        /**
         * @see org.opencms.db.storage.I_CmsStorageDelivery#streamRangeTo(org.opencms.db.CmsDbContext, java.lang.String, long, long, java.io.OutputStream)
         */
        public void streamRangeTo(CmsDbContext dbc, String hash, long start, long length, OutputStream out)
        throws IOException {

            m_streamedRange = true;
            m_hash = hash;
            m_start = start;
            m_length = length;
            out.write(m_content, (int)start, (int)length);
        }

        /**
         * @see org.opencms.db.storage.I_CmsStorageDelivery#streamTo(org.opencms.db.CmsDbContext, java.lang.String, java.io.OutputStream)
         */
        public void streamTo(CmsDbContext dbc, String hash, OutputStream out) throws IOException {

            m_streamedFull = true;
            m_hash = hash;
            out.write(m_content);
        }

        /**
         * @see org.opencms.db.storage.I_CmsStorageDelivery#supportsRangeDelivery()
         */
        public boolean supportsRangeDelivery() {

            return m_supportsRangeDelivery;
        }

        /**
         * Sets if range delivery is supported.<p>
         *
         * @param supportsRangeDelivery if range delivery is supported
         */
        void setSupportsRangeDelivery(boolean supportsRangeDelivery) {

            m_supportsRangeDelivery = supportsRangeDelivery;
        }
    }

    /** Test content. */
    private static final byte[] CONTENT = "0123456789abcdef".getBytes();

    /** Test hash. */
    private static final String HASH = "testhash";

    /** Test last modified timestamp. */
    private static final long LAST_MODIFIED = 1700000000000L;

    /** Test storage id. */
    private static final String STORAGE = "s3test";

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
     * Tests delivery capability checks.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testCanDeliver() throws Exception {

        CmsStoredContentDeliveryHelper helper = new CmsStoredContentDeliveryHelper();
        TestStorageDelivery storage = new TestStorageDelivery(CONTENT);

        assertTrue(helper.canDeliver(createInfo(), storage));
        assertFalse(helper.canDeliver(createInfo(null, HASH), storage));
        assertFalse(helper.canDeliver(createInfo(STORAGE, null), storage));
        assertFalse(helper.canDeliver(createInfo(), null));
    }

    /**
     * Tests full delivery.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testDeliverFullContent() throws Exception {

        TestStorageDelivery storage = new TestStorageDelivery(CONTENT);
        ResponseRecorder response = new ResponseRecorder();

        CmsStoredContentDeliveryHelper.DeliveryResult result = deliver(storage, createRequest(), response);

        assertEquals(CmsStoredContentDeliveryHelper.DeliveryResult.DELIVERED, result);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals(CONTENT.length, response.getContentLength());
        assertEquals("bytes", response.getHeader(CmsRequestUtil.HEADER_ACCEPT_RANGES));
        assertEquals(getETag(), response.getHeader(CmsRequestUtil.HEADER_ETAG));
        assertEquals(
            CmsRequestUtil.HEADER_VALUE_MAX_AGE + "86400",
            response.getHeader(CmsRequestUtil.HEADER_CACHE_CONTROL));
        assertEquals(
            createResource().getDateLastModified(),
            response.getDateHeader(CmsRequestUtil.HEADER_LAST_MODIFIED));
        assertArrayEquals(CONTENT, response.getBody());
        assertTrue(storage.m_streamedFull);
        assertFalse(storage.m_streamedRange);
        assertEquals(HASH, storage.m_hash);
    }

    /**
     * Tests full open ended range delivery.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testDeliverFullOpenEndedRange() throws Exception {

        TestStorageDelivery storage = new TestStorageDelivery(CONTENT);
        RequestRecorder request = createRequest();
        request.setHeader(CmsRequestUtil.HEADER_RANGE, "bytes=0-");
        ResponseRecorder response = new ResponseRecorder();

        deliver(storage, request, response);

        assertEquals(HttpServletResponse.SC_PARTIAL_CONTENT, response.getStatus());
        assertEquals("bytes 0-15/16", response.getHeader(CmsRequestUtil.HEADER_CONTENT_RANGE));
        assertArrayEquals(CONTENT, response.getBody());
    }

    /**
     * Tests HEAD delivery.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testDeliverHeadWithoutBody() throws Exception {

        TestStorageDelivery storage = new TestStorageDelivery(CONTENT);
        RequestRecorder request = createRequest();
        request.setMethod("HEAD");
        ResponseRecorder response = new ResponseRecorder();

        CmsStoredContentDeliveryHelper.DeliveryResult result = deliver(storage, request, response);

        assertEquals(CmsStoredContentDeliveryHelper.DeliveryResult.DELIVERED, result);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals(CONTENT.length, response.getContentLength());
        assertEquals(getETag(), response.getHeader(CmsRequestUtil.HEADER_ETAG));
        assertEquals(0, response.getBody().length);
        assertFalse(storage.m_streamedFull);
        assertFalse(storage.m_streamedRange);
    }

    /**
     * Tests that If-None-Match takes precedence over If-Modified-Since.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testDeliverIfNoneMatchTakesPrecedenceOverIfModifiedSince() throws Exception {

        TestStorageDelivery storage = new TestStorageDelivery(CONTENT);
        RequestRecorder request = createRequest();
        request.setHeader(CmsRequestUtil.HEADER_IF_NONE_MATCH, "\"other\"");
        request.setDateHeader(
            CmsRequestUtil.HEADER_IF_MODIFIED_SINCE,
            (createResource().getDateLastModified() / 1000) * 1000);
        ResponseRecorder response = new ResponseRecorder();

        CmsStoredContentDeliveryHelper.DeliveryResult result = deliver(storage, request, response);

        assertEquals(CmsStoredContentDeliveryHelper.DeliveryResult.DELIVERED, result);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertArrayEquals(CONTENT, response.getBody());
        assertTrue(storage.m_streamedFull);
        assertFalse(storage.m_streamedRange);
    }

    /**
     * Tests matching If-Range ETag delivery.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testDeliverIfRangeMatchingETagAsRange() throws Exception {

        TestStorageDelivery storage = new TestStorageDelivery(CONTENT);
        RequestRecorder request = createRequest();
        request.setHeader(CmsRequestUtil.HEADER_RANGE, "bytes=4-9");
        request.setHeader(CmsRequestUtil.HEADER_IF_RANGE, getETag());
        ResponseRecorder response = new ResponseRecorder();

        deliver(storage, request, response);

        assertEquals(HttpServletResponse.SC_PARTIAL_CONTENT, response.getStatus());
        assertArrayEquals("456789".getBytes(), response.getBody());
        assertTrue(storage.m_streamedRange);
        assertFalse(storage.m_streamedFull);
    }

    /**
     * Tests If-Range fallback to full delivery.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testDeliverIfRangeMismatchAsFullContent() throws Exception {

        TestStorageDelivery storage = new TestStorageDelivery(CONTENT);
        RequestRecorder request = createRequest();
        request.setHeader(CmsRequestUtil.HEADER_RANGE, "bytes=4-9");
        request.setHeader(CmsRequestUtil.HEADER_IF_RANGE, "\"etag\"");
        ResponseRecorder response = new ResponseRecorder();

        deliver(storage, request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertArrayEquals(CONTENT, response.getBody());
        assertTrue(storage.m_streamedFull);
        assertFalse(storage.m_streamedRange);
    }

    /**
     * Tests invalid range delivery.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testDeliverInvalidRange() throws Exception {

        TestStorageDelivery storage = new TestStorageDelivery(CONTENT);
        RequestRecorder request = createRequest();
        request.setHeader(CmsRequestUtil.HEADER_RANGE, "bytes=99-100");
        ResponseRecorder response = new ResponseRecorder();

        CmsStoredContentDeliveryHelper.DeliveryResult result = deliver(storage, request, response);

        assertEquals(CmsStoredContentDeliveryHelper.DeliveryResult.RANGE_NOT_SATISFIABLE, result);
        assertEquals(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE, response.getStatus());
        assertEquals("bytes */16", response.getHeader(CmsRequestUtil.HEADER_CONTENT_RANGE));
        assertEquals(getETag(), response.getHeader(CmsRequestUtil.HEADER_ETAG));
        assertEquals(0, response.getBody().length);
        assertFalse(storage.m_streamedFull);
        assertFalse(storage.m_streamedRange);
    }

    /**
     * Tests multi range fallback to full delivery.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testDeliverMultiRangeAsFullContent() throws Exception {

        TestStorageDelivery storage = new TestStorageDelivery(CONTENT);
        RequestRecorder request = createRequest();
        request.setHeader(CmsRequestUtil.HEADER_RANGE, "bytes=0-1,4-5");
        ResponseRecorder response = new ResponseRecorder();

        deliver(storage, request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertArrayEquals(CONTENT, response.getBody());
        assertTrue(storage.m_streamedFull);
        assertFalse(storage.m_streamedRange);
    }

    /**
     * Tests If-Modified-Since delivery.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testDeliverNotModified() throws Exception {

        TestStorageDelivery storage = new TestStorageDelivery(CONTENT);
        RequestRecorder request = createRequest();
        request.setDateHeader(
            CmsRequestUtil.HEADER_IF_MODIFIED_SINCE,
            (createResource().getDateLastModified() / 1000) * 1000);
        ResponseRecorder response = new ResponseRecorder();

        CmsStoredContentDeliveryHelper.DeliveryResult result = deliver(storage, request, response);

        assertEquals(CmsStoredContentDeliveryHelper.DeliveryResult.NOT_MODIFIED, result);
        assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatus());
        assertEquals(getETag(), response.getHeader(CmsRequestUtil.HEADER_ETAG));
        assertEquals(0, response.getBody().length);
        assertFalse(storage.m_streamedFull);
        assertFalse(storage.m_streamedRange);
    }

    /**
     * Tests If-None-Match delivery.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testDeliverNotModifiedForIfNoneMatch() throws Exception {

        TestStorageDelivery storage = new TestStorageDelivery(CONTENT);
        RequestRecorder request = createRequest();
        request.setHeader(CmsRequestUtil.HEADER_IF_NONE_MATCH, getETag());
        ResponseRecorder response = new ResponseRecorder();

        CmsStoredContentDeliveryHelper.DeliveryResult result = deliver(storage, request, response);

        assertEquals(CmsStoredContentDeliveryHelper.DeliveryResult.NOT_MODIFIED, result);
        assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatus());
        assertEquals(getETag(), response.getHeader(CmsRequestUtil.HEADER_ETAG));
        assertEquals(0, response.getBody().length);
        assertFalse(storage.m_streamedFull);
        assertFalse(storage.m_streamedRange);
    }

    /**
     * Tests closed range delivery.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testDeliverRange() throws Exception {

        TestStorageDelivery storage = new TestStorageDelivery(CONTENT);
        RequestRecorder request = createRequest();
        request.setHeader(CmsRequestUtil.HEADER_RANGE, "bytes=4-9");
        ResponseRecorder response = new ResponseRecorder();

        CmsStoredContentDeliveryHelper.DeliveryResult result = deliver(storage, request, response);

        assertEquals(CmsStoredContentDeliveryHelper.DeliveryResult.DELIVERED, result);
        assertEquals(HttpServletResponse.SC_PARTIAL_CONTENT, response.getStatus());
        assertEquals(6, response.getContentLength());
        assertEquals("bytes 4-9/16", response.getHeader(CmsRequestUtil.HEADER_CONTENT_RANGE));
        assertEquals(getETag(), response.getHeader(CmsRequestUtil.HEADER_ETAG));
        assertArrayEquals("456789".getBytes(), response.getBody());
        assertTrue(storage.m_streamedRange);
        assertFalse(storage.m_streamedFull);
        assertEquals(4, storage.m_start);
        assertEquals(6, storage.m_length);
    }

    /**
     * Tests range fallback if the storage does not support range delivery.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testDeliverRangeAsFullContentWithoutRangeSupport() throws Exception {

        TestStorageDelivery storage = new TestStorageDelivery(CONTENT);
        storage.setSupportsRangeDelivery(false);
        RequestRecorder request = createRequest();
        request.setHeader(CmsRequestUtil.HEADER_RANGE, "bytes=4-9");
        ResponseRecorder response = new ResponseRecorder();

        deliver(storage, request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertArrayEquals(CONTENT, response.getBody());
        assertTrue(storage.m_streamedFull);
        assertFalse(storage.m_streamedRange);
        assertNull(response.getHeader(CmsRequestUtil.HEADER_ACCEPT_RANGES));
    }

    /**
     * Tests open ended range delivery.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testDeliverRangeWithoutEnd() throws Exception {

        TestStorageDelivery storage = new TestStorageDelivery(CONTENT);
        RequestRecorder request = createRequest();
        request.setHeader(CmsRequestUtil.HEADER_RANGE, "bytes=13-");
        ResponseRecorder response = new ResponseRecorder();

        deliver(storage, request, response);

        assertEquals(HttpServletResponse.SC_PARTIAL_CONTENT, response.getStatus());
        assertEquals("bytes 13-15/16", response.getHeader(CmsRequestUtil.HEADER_CONTENT_RANGE));
        assertArrayEquals("def".getBytes(), response.getBody());
    }

    /**
     * Tests single byte range delivery.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testDeliverSingleByteRange() throws Exception {

        TestStorageDelivery storage = new TestStorageDelivery(CONTENT);
        RequestRecorder request = createRequest();
        request.setHeader(CmsRequestUtil.HEADER_RANGE, "bytes=0-0");
        ResponseRecorder response = new ResponseRecorder();

        deliver(storage, request, response);

        assertEquals(HttpServletResponse.SC_PARTIAL_CONTENT, response.getStatus());
        assertEquals("bytes 0-0/16", response.getHeader(CmsRequestUtil.HEADER_CONTENT_RANGE));
        assertArrayEquals("0".getBytes(), response.getBody());
    }

    /**
     * Tests single byte suffix range delivery.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testDeliverSingleByteSuffixRange() throws Exception {

        TestStorageDelivery storage = new TestStorageDelivery(CONTENT);
        RequestRecorder request = createRequest();
        request.setHeader(CmsRequestUtil.HEADER_RANGE, "bytes=-1");
        ResponseRecorder response = new ResponseRecorder();

        deliver(storage, request, response);

        assertEquals(HttpServletResponse.SC_PARTIAL_CONTENT, response.getStatus());
        assertEquals("bytes 15-15/16", response.getHeader(CmsRequestUtil.HEADER_CONTENT_RANGE));
        assertArrayEquals("f".getBytes(), response.getBody());
    }

    /**
     * Tests suffix range delivery.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testDeliverSuffixRange() throws Exception {

        TestStorageDelivery storage = new TestStorageDelivery(CONTENT);
        RequestRecorder request = createRequest();
        request.setHeader(CmsRequestUtil.HEADER_RANGE, "bytes=-4");
        ResponseRecorder response = new ResponseRecorder();

        deliver(storage, request, response);

        assertEquals(HttpServletResponse.SC_PARTIAL_CONTENT, response.getStatus());
        assertEquals("bytes 12-15/16", response.getHeader(CmsRequestUtil.HEADER_CONTENT_RANGE));
        assertArrayEquals("cdef".getBytes(), response.getBody());
    }

    /**
     * Tests that unknown range units fall back to full delivery.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testDeliverUnknownRangeUnitAsFullContent() throws Exception {

        TestStorageDelivery storage = new TestStorageDelivery(CONTENT);
        RequestRecorder request = createRequest();
        request.setHeader(CmsRequestUtil.HEADER_RANGE, "items=0-1");
        ResponseRecorder response = new ResponseRecorder();

        deliver(storage, request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertArrayEquals(CONTENT, response.getBody());
        assertTrue(storage.m_streamedFull);
        assertFalse(storage.m_streamedRange);
    }

    /**
     * Creates stored content info.<p>
     *
     * @return the stored content info
     */
    private CmsStoredContentInfo createInfo() {

        return createInfo(STORAGE, HASH);
    }

    /**
     * Creates stored content info.<p>
     *
     * @param storage the storage id
     * @param hash the content hash
     *
     * @return the stored content info
     */
    private CmsStoredContentInfo createInfo(String storage, String hash) {

        return new CmsStoredContentInfo(createResource(), storage, hash, CONTENT.length);
    }

    /**
     * Creates a request proxy.<p>
     *
     * @return the request recorder
     */
    private RequestRecorder createRequest() {

        return new RequestRecorder();
    }

    /**
     * Creates a test resource.<p>
     *
     * @return the test resource
     */
    private CmsResource createResource() {

        return new CmsResource(
            CmsUUID.getNullUUID(),
            CmsUUID.getNullUUID(),
            "/test.bin",
            1,
            false,
            0,
            CmsUUID.getNullUUID(),
            CmsResource.STATE_UNCHANGED,
            LAST_MODIFIED,
            CmsUUID.getNullUUID(),
            LAST_MODIFIED,
            CmsUUID.getNullUUID(),
            CmsResource.DATE_RELEASED_DEFAULT,
            CmsResource.DATE_EXPIRED_DEFAULT,
            1,
            CONTENT.length,
            LAST_MODIFIED,
            1);
    }

    /**
     * Delivers the test content.<p>
     *
     * @param storage the storage
     * @param request the request recorder
     * @param response the response recorder
     *
     * @return the delivery result
     *
     * @throws Exception if something goes wrong
     */
    private CmsStoredContentDeliveryHelper.DeliveryResult deliver(
        TestStorageDelivery storage,
        RequestRecorder request,
        ResponseRecorder response)
    throws Exception {

        return new CmsStoredContentDeliveryHelper().deliver(
            createInfo(),
            storage,
            (HttpServletRequest)Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[] {HttpServletRequest.class},
                request),
            (HttpServletResponse)Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[] {HttpServletResponse.class},
                response));
    }

    /**
     * Returns the test ETag.<p>
     *
     * @return the test ETag
     */
    private String getETag() {

        return "\"" + HASH + "\"";
    }
}

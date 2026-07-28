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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.opencms.db.storage.s3.I_CmsS3Client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Tests for the S3 image cache.<p>
 */
public class TestCmsS3ImageCache {

    /**
     * S3 client test double.<p>
     */
    private static class TestS3Client implements I_CmsS3Client {

        /** Stored objects. */
        private Map<String, byte[]> m_objects = new HashMap<String, byte[]>();

        /** Whether bucket access was validated. */
        private boolean m_validated;

        /** Whether the client was closed. */
        private boolean m_closed;

        /** Number of combined object metadata reads. */
        private int m_metadataReads;

        /** Whether deleting an object should fail. */
        private boolean m_failDelete;

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#close()
         */
        @Override
        public void close() throws Exception {

            m_closed = true;
        }

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#deleteObject(java.lang.String)
         */
        public void deleteObject(String key) throws Exception {

            if (m_failDelete) {
                throw new IOException("Simulated delete failure");
            }
            m_objects.remove(key);
        }

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#exists(java.lang.String)
         */
        public boolean exists(String key) throws Exception {

            return m_objects.containsKey(key);
        }

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#getObject(java.lang.String)
         */
        public byte[] getObject(String key) throws Exception {

            return m_objects.get(key);
        }

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#getObjectLength(java.lang.String)
         */
        @Override
        public long getObjectLength(String key) throws Exception {

            return m_objects.get(key).length;
        }

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#getObjectLengthIfExists(java.lang.String)
         */
        @Override
        public long getObjectLengthIfExists(String key) throws Exception {

            m_metadataReads += 1;
            byte[] content = m_objects.get(key);
            return content == null ? -1 : content.length;
        }

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#putObject(java.lang.String, byte[])
         */
        public void putObject(String key, byte[] content) throws Exception {

            m_objects.put(key, content);
        }

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#validateBucketAccess()
         */
        @Override
        public void validateBucketAccess() throws Exception {

            m_validated = true;
        }

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#writeObjectRangeTo(java.lang.String, long, long, java.io.OutputStream)
         */
        @Override
        public void writeObjectRangeTo(String key, long start, long length, OutputStream out) throws Exception {

            byte[] content = m_objects.get(key);
            out.write(content, (int)start, (int)length);
        }

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#writeObjectTo(java.lang.String, java.io.OutputStream)
         */
        @Override
        public void writeObjectTo(String key, OutputStream out) throws Exception {

            byte[] content = m_objects.get(key);
            if (content == null) {
                throw new IOException("Object not found: " + key);
            }
            out.write(content);
        }
    }

    /**
     * Tests that closing the image cache closes the S3 client.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testCloseDelegatesToClient() throws Exception {

        TestS3Client client = new TestS3Client();
        CmsS3ImageCache cache = new CmsS3ImageCache(client);

        cache.close();

        assertTrue(client.m_closed);
        assertThrows(IllegalStateException.class, () -> cache.exists("image.jpg"));
    }

    /**
     * Tests that an existing object's length is loaded once and reused.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testExistingObjectMetadataIsCached() throws Exception {

        TestS3Client client = new TestS3Client();
        byte[] content = "scaled image".getBytes("UTF-8");
        client.m_objects.put("image.jpg", content);
        CmsS3ImageCache cache = new CmsS3ImageCache(client);

        assertTrue(cache.exists("image.jpg"));
        assertEquals(content.length, cache.getLength("image.jpg"));
        assertTrue(cache.exists("image.jpg"));
        assertEquals(1, client.m_metadataReads);
    }

    /**
     * Tests that a failed object read invalidates cached metadata.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testFailedReadInvalidatesCachedMetadata() throws Exception {

        TestS3Client client = new TestS3Client();
        CmsS3ImageCache cache = new CmsS3ImageCache(client);
        cache.write("image.jpg", "old".getBytes("UTF-8"));
        client.m_objects.remove("image.jpg");

        assertThrows(IOException.class, () -> cache.writeTo("image.jpg", new ByteArrayOutputStream()));

        byte[] replacement = "replacement".getBytes("UTF-8");
        client.m_objects.put("image.jpg", replacement);
        assertTrue(cache.exists("image.jpg"));
        assertEquals(replacement.length, cache.getLength("image.jpg"));
        assertEquals(1, client.m_metadataReads);
    }

    /** Tests that an incomplete startup health check closes and rejects the client. */
    @Test
    public void testHealthCheckDeleteFailureRejectsClient() {

        TestS3Client client = new TestS3Client();
        client.m_failDelete = true;

        assertThrows(IOException.class, () -> new CmsS3ImageCache(client));
        assertTrue(client.m_closed);
    }

    /** Tests that the startup health check object is removed. */
    @Test
    public void testHealthCheckObjectIsRemoved() throws Exception {

        TestS3Client client = new TestS3Client();

        new CmsS3ImageCache(client, "imagecache/");

        assertTrue(client.m_validated);
        assertTrue(client.m_objects.isEmpty());
    }

    /**
     * Tests that missing entries are not retained in the local metadata cache.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testMissingObjectMetadataIsNotCached() throws Exception {

        TestS3Client client = new TestS3Client();
        CmsS3ImageCache cache = new CmsS3ImageCache(client);

        assertFalse(cache.exists("missing.jpg"));
        assertFalse(cache.exists("missing.jpg"));
        assertEquals(2, client.m_metadataReads);
    }

    /**
     * Tests that the configured prefix is prepended to object keys.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testPrefixIsAppliedToObjectKeys() throws Exception {

        TestS3Client client = new TestS3Client();
        CmsS3ImageCache cache = new CmsS3ImageCache(client, "/imagecache//");
        byte[] content = "scaled image".getBytes("UTF-8");

        cache.write("/export/sites/default/image.jpg_123.jpg", content);

        assertTrue(client.m_validated);
        assertTrue(client.m_objects.containsKey("imagecache/export/sites/default/image.jpg_123.jpg"));
        assertTrue(cache.exists("export/sites/default/image.jpg_123.jpg"));
        assertEquals(content.length, cache.getLength("export/sites/default/image.jpg_123.jpg"));

        ByteArrayOutputStream full = new ByteArrayOutputStream();
        cache.writeTo("export/sites/default/image.jpg_123.jpg", full);
        assertArrayEquals(content, full.toByteArray());

        ByteArrayOutputStream range = new ByteArrayOutputStream();
        cache.writeRangeTo("export/sites/default/image.jpg_123.jpg", 7, 5, range);
        assertEquals("image", range.toString("UTF-8"));
    }

    /**
     * Tests that replacing the S3 client closes the previous client.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testReplacingClientClosesPreviousClient() throws Exception {

        TestS3Client firstClient = new TestS3Client();
        TestS3Client secondClient = new TestS3Client();
        CmsS3ImageCache cache = new CmsS3ImageCache(firstClient);

        cache.initClient(secondClient, null);

        assertTrue(firstClient.m_closed);
        assertTrue(secondClient.m_validated);
        assertFalse(secondClient.m_closed);
    }

    /**
     * Tests that writing an image cache entry also caches its length.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testWriteCachesObjectLength() throws Exception {

        TestS3Client client = new TestS3Client();
        CmsS3ImageCache cache = new CmsS3ImageCache(client);
        byte[] content = "scaled image".getBytes("UTF-8");

        cache.write("image.jpg", content);

        assertTrue(cache.exists("image.jpg"));
        assertEquals(content.length, cache.getLength("image.jpg"));
        assertEquals(0, client.m_metadataReads);
    }
}

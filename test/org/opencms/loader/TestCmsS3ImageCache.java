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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.opencms.db.storage.s3.I_CmsS3Client;

import java.io.ByteArrayOutputStream;
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

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#deleteObject(java.lang.String)
         */
        public void deleteObject(String key) throws Exception {

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

            out.write(m_objects.get(key));
        }
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
}

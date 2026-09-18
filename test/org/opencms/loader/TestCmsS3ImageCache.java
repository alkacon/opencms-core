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

import org.opencms.configuration.CmsImageCacheConfiguration;
import org.opencms.db.storage.CmsStorageBlobNotFoundException;
import org.opencms.db.storage.s3.CmsS3DeleteResult;
import org.opencms.db.storage.s3.CmsS3ObjectMetadata;
import org.opencms.db.storage.s3.I_CmsS3Client;
import org.opencms.loader.imagecache.CmsImageCacheAccessRenewal;
import org.opencms.loader.imagecache.CmsImageCacheCapabilities.Capability;
import org.opencms.loader.imagecache.CmsImageCacheEntry;
import org.opencms.loader.imagecache.CmsImageCacheMaintenanceCleaner;
import org.opencms.loader.imagecache.CmsImageCacheMaintenanceRequest;
import org.opencms.loader.imagecache.CmsImageCacheMaintenanceResult;
import org.opencms.loader.imagecache.CmsImageCacheMaintenanceService;
import org.opencms.loader.imagecache.CmsS3ImageCacheMaintenance;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

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

        /** Number of single-object detail metadata reads. */
        private int m_metadataDetailReads;

        /** Whether deleting an object should fail. */
        private boolean m_failDelete;

        /** Object keys which should produce per-object delete failures. */
        private Set<String> m_failedBatchDeleteKeys = new HashSet<String>();

        /** Object keys which should produce renewal failures. */
        private Set<String> m_failedRenewalKeys = new HashSet<String>();

        /** Observed multi-object delete batch sizes. */
        private List<Integer> m_deleteBatchSizes = new ArrayList<Integer>();

        /** The last prefix passed to the metadata listing. */
        private String m_lastListingPrefix;

        /** Stored object timestamps. */
        private Map<String, Instant> m_lastModified = new HashMap<String, Instant>();

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
            m_lastModified.remove(key);
        }

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#deleteObjects(java.util.List)
         */
        @Override
        public CmsS3DeleteResult deleteObjects(List<String> keys) throws Exception {

            m_deleteBatchSizes.add(Integer.valueOf(keys.size()));
            Map<String, Exception> failures = new LinkedHashMap<String, Exception>();
            for (String key : keys) {
                if (m_failedBatchDeleteKeys.contains(key)) {
                    failures.put(key, new IOException("Simulated batch delete failure for " + key));
                } else {
                    deleteObject(key);
                }
            }
            return new CmsS3DeleteResult(keys, failures);
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

            byte[] content = m_objects.get(key);
            if (content == null) {
                throw new CmsStorageBlobNotFoundException(key);
            }
            return content.length;
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
         * @see org.opencms.db.storage.s3.I_CmsS3Client#getObjectMetadata(java.lang.String)
         */
        @Override
        public CmsS3ObjectMetadata getObjectMetadata(String key) throws Exception {

            m_metadataDetailReads += 1;
            byte[] content = m_objects.get(key);
            if (content == null) {
                throw new IOException("Missing test object: " + key);
            }
            return new CmsS3ObjectMetadata(key, content.length, m_lastModified.get(key), "etag-" + key);
        }

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#putObject(java.lang.String, byte[])
         */
        public void putObject(String key, byte[] content) throws Exception {

            m_objects.put(key, content);
            m_lastModified.put(key, Instant.now());
        }

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#renewObject(java.lang.String, java.lang.String, java.time.Instant)
         */
        @Override
        public synchronized boolean renewObject(String key, String expectedRevision, Instant renewalTime)
        throws Exception {

            if (m_failedRenewalKeys.contains(key)) {
                throw new IOException("Simulated renewal failure for " + key);
            }
            if (!m_objects.containsKey(key) || !("etag-" + key).equals(expectedRevision)) {
                return false;
            }
            m_lastModified.put(key, renewalTime);
            return true;
        }

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#validateBucketAccess()
         */
        @Override
        public void validateBucketAccess() throws Exception {

            m_validated = true;
        }

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#visitObjectKeys(org.opencms.db.storage.s3.I_CmsS3Client.I_CmsS3ObjectKeyVisitor)
         */
        @Override
        public void visitObjectKeys(I_CmsS3ObjectKeyVisitor visitor) throws Exception {

            for (String key : new java.util.ArrayList<String>(m_objects.keySet())) {
                visitor.visit(key);
            }
        }

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#visitObjects(java.lang.String, org.opencms.db.storage.s3.I_CmsS3Client.I_CmsS3ObjectMetadataVisitor)
         */
        @Override
        public void visitObjects(String prefix, I_CmsS3ObjectMetadataVisitor visitor) throws Exception {

            m_lastListingPrefix = prefix;
            for (Map.Entry<String, byte[]> entry : new ArrayList<Map.Entry<String, byte[]>>(m_objects.entrySet())) {
                if ((prefix == null) || entry.getKey().startsWith(prefix)) {
                    visitor.visit(
                        new CmsS3ObjectMetadata(
                            entry.getKey(),
                            entry.getValue().length,
                            m_lastModified.getOrDefault(entry.getKey(), Instant.EPOCH),
                            "etag-" + entry.getKey()));
                }
            }
        }

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#writeObjectRangeTo(java.lang.String, long, long, java.io.OutputStream)
         */
        @Override
        public void writeObjectRangeTo(String key, long start, long length, OutputStream out) throws Exception {

            byte[] content = m_objects.get(key);
            if (content == null) {
                throw new CmsStorageBlobNotFoundException(key);
            }
            out.write(content, (int)start, (int)length);
        }

        @Override
        public CmsS3ObjectMetadata writeObjectRangeToWithMetadata(String key, long start, long length, OutputStream out)
        throws Exception {

            writeObjectRangeTo(key, start, length, out);
            return createMetadata(key, length);
        }

        /**
         * @see org.opencms.db.storage.s3.I_CmsS3Client#writeObjectTo(java.lang.String, java.io.OutputStream)
         */
        @Override
        public void writeObjectTo(String key, OutputStream out) throws Exception {

            byte[] content = m_objects.get(key);
            if (content == null) {
                throw new CmsStorageBlobNotFoundException(key);
            }
            out.write(content);
        }

        @Override
        public CmsS3ObjectMetadata writeObjectToWithMetadata(String key, OutputStream out) throws Exception {

            writeObjectTo(key, out);
            return createMetadata(key, m_objects.get(key).length);
        }

        /** Creates response metadata without simulating an additional HEAD request. */
        private CmsS3ObjectMetadata createMetadata(String key, long length) {

            return new CmsS3ObjectMetadata(key, length, m_lastModified.get(key), "etag-" + key);
        }
    }

    /** Waits for an asynchronous condition. */
    private static void await(BooleanSupplier condition) throws Exception {

        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean() && (System.nanoTime() < deadline)) {
            Thread.sleep(10);
        }
        assertTrue(condition.getAsBoolean());
    }

    /** Tests that a 304-style access without locally observed GET metadata performs one asynchronous HEAD. */
    @Test
    public void testAccessTriggeredRenewalLoadsMetadataFor304() throws Exception {

        TestS3Client client = new TestS3Client();
        CmsS3ImageCache cache = new CmsS3ImageCache(client, "imagecache");
        cache.write("image.jpg", new byte[] {1, 2, 3});
        Instant oldTime = Instant.now().minusSeconds(120);
        client.m_lastModified.put("imagecache/image.jpg", oldTime);
        CmsImageCacheConfiguration configuration = new CmsImageCacheConfiguration();
        configuration.setRetention("renew-on-use", "PT3M", "PT2M", "PT0S");
        configuration.validate();

        try (CmsImageCacheAccessRenewal renewal = CmsImageCacheAccessRenewal.create(cache, configuration)) {
            renewal.recordAccess("image.jpg");
            await(() -> client.m_lastModified.get("imagecache/image.jpg").isAfter(oldTime));
        }

        assertEquals(1, client.m_metadataDetailReads);
    }

    /** Tests access-triggered renewal using metadata returned by the S3 GET without an additional HEAD. */
    @Test
    public void testAccessTriggeredRenewalUsesGetMetadata() throws Exception {

        TestS3Client client = new TestS3Client();
        CmsS3ImageCache cache = new CmsS3ImageCache(client, "imagecache");
        cache.write("image.jpg", new byte[] {1, 2, 3});
        Instant oldTime = Instant.now().minusSeconds(120);
        client.m_lastModified.put("imagecache/image.jpg", oldTime);
        cache.writeTo("image.jpg", new ByteArrayOutputStream());
        CmsImageCacheConfiguration configuration = new CmsImageCacheConfiguration();
        configuration.setRetention("renew-on-use", "PT3M", "PT2M", "PT0S");
        configuration.validate();

        try (CmsImageCacheAccessRenewal renewal = CmsImageCacheAccessRenewal.create(cache, configuration)) {
            renewal.recordAccess("image.jpg");
            await(() -> client.m_lastModified.get("imagecache/image.jpg").isAfter(oldTime));
        }

        assertEquals(0, client.m_metadataDetailReads);
    }

    /** Tests that an authoritative existence check bypasses stale positive metadata. */
    @Test
    public void testAuthoritativeExistsBypassesPositiveMetadataCache() throws Exception {

        TestS3Client client = new TestS3Client();
        CmsS3ImageCache cache = new CmsS3ImageCache(client);
        cache.write("image.jpg", new byte[] {1, 2, 3});
        assertTrue(cache.exists("image.jpg"));
        client.m_objects.remove("image.jpg");

        assertTrue(cache.exists("image.jpg"));
        assertFalse(cache.existsAuthoritatively("image.jpg"));
        assertFalse(cache.exists("image.jpg"));
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

        assertThrows(
            CmsImageCacheEntryNotFoundException.class,
            () -> cache.writeTo("image.jpg", new ByteArrayOutputStream()));
        assertThrows(
            CmsImageCacheEntryNotFoundException.class,
            () -> cache.writeRangeTo("image.jpg", 0, 1, new ByteArrayOutputStream()));
        assertThrows(CmsImageCacheEntryNotFoundException.class, () -> cache.getLength("image.jpg"));

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
     * Tests that listing and clearing are limited to the configured image cache prefix.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testListAndClearRespectPrefix() throws Exception {

        TestS3Client client = new TestS3Client();
        client.m_objects.put("other-storage/content", new byte[] {9});
        CmsS3ImageCache cache = new CmsS3ImageCache(client, "imagecache");
        byte[] first = "first".getBytes("UTF-8");
        byte[] second = "second entry".getBytes("UTF-8");
        cache.write("/sites/default/first.jpg_1.jpg", first);
        cache.write("sites/default/folder/second.jpg_2.jpg", second);

        Map<String, Long> entries = new HashMap<>();
        cache.visitEntries((key, length) -> entries.put(key, Long.valueOf(length)));

        assertEquals(2, entries.size());
        assertEquals(Long.valueOf(first.length), entries.get("sites/default/first.jpg_1.jpg"));
        assertEquals(Long.valueOf(second.length), entries.get("sites/default/folder/second.jpg_2.jpg"));

        cache.clear();

        assertFalse(client.m_objects.containsKey("imagecache/sites/default/first.jpg_1.jpg"));
        assertFalse(client.m_objects.containsKey("imagecache/sites/default/folder/second.jpg_2.jpg"));
        assertTrue(client.m_objects.containsKey("other-storage/content"));
        assertFalse(cache.exists("sites/default/first.jpg_1.jpg"));
    }

    /** Tests that a cache key prefix is applied by the S3 listing instead of being filtered afterwards. */
    @Test
    public void testListingUsesCacheKeyPrefix() throws Exception {

        TestS3Client client = new TestS3Client();
        CmsS3ImageCache cache = new CmsS3ImageCache(client, "imagecache");
        cache.write("sites/default/first.jpg", new byte[] {1});
        cache.write("sites/other/second.jpg", new byte[] {2});
        Map<String, Long> entries = new HashMap<String, Long>();

        cache.visitEntries("/sites/default/", (key, length) -> entries.put(key, Long.valueOf(length)));

        assertEquals("imagecache/sites/default/", client.m_lastListingPrefix);
        assertEquals(1, entries.size());
        assertEquals(Long.valueOf(1), entries.get("sites/default/first.jpg"));
    }

    /** Tests the S3 maintenance capabilities, listing, renewal, deletion and clear operations. */
    @Test
    public void testMaintenanceAdapter() throws Exception {

        TestS3Client client = new TestS3Client();
        CmsS3ImageCache cache = new CmsS3ImageCache(client, "imagecache");
        cache.write("first.jpg", new byte[] {1, 2});
        cache.write("second.jpg", new byte[] {3});
        CmsImageCacheMaintenanceService service = new CmsImageCacheMaintenanceService(
            new CmsS3ImageCacheMaintenance(cache, 1, 2));
        List<CmsImageCacheEntry> entries = new ArrayList<CmsImageCacheEntry>();

        service.visitEntries(entries::add);

        assertEquals("s3", service.getBackendId());
        assertEquals(2, entries.size());
        assertTrue(service.getCapabilities().supports(Capability.ENTRY_TIMESTAMPS));
        assertTrue(service.getCapabilities().supports(Capability.RENEW_ENTRIES));
        assertTrue(entries.stream().allMatch(entry -> entry.getLastModified() != null));
        assertEquals("imagecache/", client.m_lastListingPrefix);
        assertEquals(0, client.m_metadataDetailReads);

        assertTrue(service.getEntry(entries.get(0).getKey()) != null);
        assertEquals(1, client.m_metadataDetailReads);

        Instant renewalTime = Instant.parse("2026-01-01T00:00:00Z");
        CmsImageCacheEntry staleEntry = new CmsImageCacheEntry(
            entries.get(1).getKey(),
            entries.get(1).getLength(),
            entries.get(1).getLastModified(),
            "stale-revision");
        CmsImageCacheMaintenanceResult renewalResult = service.execute(
            CmsImageCacheMaintenanceRequest.renew(Arrays.asList(entries.get(0), staleEntry), renewalTime));
        assertEquals(1, renewalResult.getSucceeded());
        assertEquals(1, renewalResult.getSkipped());
        assertEquals(renewalTime, client.m_lastModified.get("imagecache/" + entries.get(0).getKey()));

        CmsImageCacheMaintenanceResult deleteResult = service.execute(CmsImageCacheMaintenanceRequest.delete(entries));
        assertEquals(2, deleteResult.getSucceeded());
        assertEquals(Arrays.asList(Integer.valueOf(1), Integer.valueOf(1)), client.m_deleteBatchSizes);
        assertTrue(client.m_objects.isEmpty());

        cache.write("third.jpg", new byte[] {4});
        CmsImageCacheMaintenanceResult clearResult = service.execute(CmsImageCacheMaintenanceRequest.clear());
        assertEquals(1, clearResult.getSucceeded());
        assertTrue(client.m_objects.isEmpty());
    }

    /** Tests that per-object S3 batch errors are retained in maintenance metrics. */
    @Test
    public void testMaintenanceBatchDeletePartialFailure() throws Exception {

        TestS3Client client = new TestS3Client();
        CmsS3ImageCache cache = new CmsS3ImageCache(client, "imagecache");
        cache.write("first.jpg", new byte[] {1});
        cache.write("second.jpg", new byte[] {2});
        client.m_failedBatchDeleteKeys.add("imagecache/second.jpg");
        CmsImageCacheMaintenanceService service = new CmsImageCacheMaintenanceService(
            new CmsS3ImageCacheMaintenance(cache, 1000, 1));
        List<CmsImageCacheEntry> entries = new ArrayList<CmsImageCacheEntry>();
        service.visitEntries(entries::add);

        CmsImageCacheMaintenanceResult result = service.execute(CmsImageCacheMaintenanceRequest.delete(entries));

        assertEquals(2, result.getRequested());
        assertEquals(1, result.getSucceeded());
        assertEquals(1, result.getFailed());
        assertTrue(client.m_objects.containsKey("imagecache/second.jpg"));
    }

    /** Tests that per-object S3 renewal failures are retained in maintenance metrics. */
    @Test
    public void testMaintenanceRenewalFailure() throws Exception {

        TestS3Client client = new TestS3Client();
        CmsS3ImageCache cache = new CmsS3ImageCache(client, "imagecache");
        cache.write("first.jpg", new byte[] {1});
        cache.write("second.jpg", new byte[] {2});
        client.m_failedRenewalKeys.add("imagecache/second.jpg");
        CmsImageCacheMaintenanceService service = new CmsImageCacheMaintenanceService(
            new CmsS3ImageCacheMaintenance(cache, 1000, 1, 2, 1000000, 1));
        List<CmsImageCacheEntry> entries = new ArrayList<CmsImageCacheEntry>();
        service.visitEntries(entries::add);

        CmsImageCacheMaintenanceResult result = service.execute(
            CmsImageCacheMaintenanceRequest.renew(entries, Instant.parse("2026-01-01T00:00:00Z")));

        assertEquals(2, result.getRequested());
        assertEquals(1, result.getSucceeded());
        assertEquals(1, result.getFailed());
        assertTrue(result.getFailures().containsKey("second.jpg"));
    }

    /** Tests timestamp-based S3 cleanup in bounded delete batches. */
    @Test
    public void testMaintenanceTimeBasedCleanup() throws Exception {

        TestS3Client client = new TestS3Client();
        CmsS3ImageCache cache = new CmsS3ImageCache(client, "imagecache");
        cache.write("old.jpg", new byte[] {1});
        cache.write("recent.jpg", new byte[] {2});
        client.m_lastModified.put("imagecache/old.jpg", Instant.parse("2026-01-01T00:00:00Z"));
        client.m_lastModified.put("imagecache/recent.jpg", Instant.parse("2026-03-01T00:00:00Z"));
        CmsImageCacheMaintenanceService service = new CmsImageCacheMaintenanceService(
            new CmsS3ImageCacheMaintenance(cache, 1, 1));

        CmsImageCacheMaintenanceCleaner.Result result = CmsImageCacheMaintenanceCleaner.delete(
            service,
            Instant.parse("2026-02-01T00:00:00Z"),
            1,
            null);

        assertEquals(2, result.getScanned());
        assertEquals(1, result.getMatched());
        assertEquals(1, result.getSucceeded());
        assertFalse(cache.exists("old.jpg"));
        assertTrue(cache.exists("recent.jpg"));
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

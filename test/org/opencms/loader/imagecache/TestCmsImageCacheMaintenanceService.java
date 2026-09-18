/*
 * This library is part of OpenCms -
 * The Open Source Content Management System
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

package org.opencms.loader.imagecache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.opencms.configuration.CmsImageCacheConfiguration;
import org.opencms.loader.CmsFsImageCache;
import org.opencms.loader.CmsRfsImageCache;
import org.opencms.loader.imagecache.CmsImageCacheCapabilities.Capability;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Tests the backend-neutral image cache maintenance service.<p>
 */
public class TestCmsImageCacheMaintenanceService {

    /**
     * Deletes a temporary directory tree.<p>
     *
     * @param path the directory path
     * @throws Exception if deletion fails
     */
    private static void deleteDirectory(Path path) throws Exception {

        if (!Files.exists(path)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(path)) {
            paths.sorted(Comparator.reverseOrder()).forEach(file -> {
                try {
                    Files.deleteIfExists(file);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /** Tests request validation. */
    @Test
    public void testRequestValidation() {

        CmsImageCacheEntry entry = new CmsImageCacheEntry("image.jpg", 1, null, null);
        assertThrows(
            IllegalArgumentException.class,
            () -> CmsImageCacheMaintenanceRequest.renew(Arrays.asList(entry), null));
        assertThrows(
            IllegalArgumentException.class,
            () -> CmsImageCacheMaintenanceRequest.delete(Arrays.asList(entry, entry)));
    }

    /** Tests RFS listing, renewal, deletion and clear operations. */
    @Test
    public void testRfsMaintenance() throws Exception {

        Path repository = Files.createTempDirectory("opencms-rfs-image-maintenance");
        try {
            CmsRfsImageCache cache = new CmsRfsImageCache(repository.toString());
            cache.write("folder/first.jpg", new byte[] {1, 2, 3});
            cache.write("folder/second.jpg", new byte[] {4, 5});
            Instant originalTime = Instant.parse("2026-01-01T00:00:00Z");
            Files.setLastModifiedTime(repository.resolve("folder/first.jpg"), FileTime.from(originalTime));

            CmsImageCacheMaintenanceService service = CmsImageCacheMaintenanceService.create(cache);
            List<CmsImageCacheEntry> entries = new ArrayList<CmsImageCacheEntry>();
            service.visitEntries(entries::add);

            assertEquals("rfs", service.getBackendId());
            assertTrue(service.getCapabilities().supports(Capability.ENTRY_TIMESTAMPS));
            assertEquals(2, entries.size());
            CmsImageCacheEntry first = findEntry(entries, "folder/first.jpg");
            CmsImageCacheEntry second = findEntry(entries, "folder/second.jpg");
            assertEquals(originalTime, first.getLastModified());
            assertEquals(3, first.getLength());

            Instant renewalTime = Instant.parse("2026-02-01T00:00:00Z");
            CmsImageCacheMaintenanceResult renewResult = service.execute(
                CmsImageCacheMaintenanceRequest.renew(Arrays.asList(first), renewalTime));
            assertTrue(renewResult.isSuccessful());
            assertEquals(1, renewResult.getSucceeded());
            assertEquals(renewalTime, Files.getLastModifiedTime(repository.resolve("folder/first.jpg")).toInstant());

            CmsImageCacheMaintenanceResult deleteResult = service.execute(
                CmsImageCacheMaintenanceRequest.delete(Arrays.asList(second)));
            assertEquals(1, deleteResult.getSucceeded());
            assertFalse(cache.exists("folder/second.jpg"));

            CmsImageCacheMaintenanceResult clearResult = service.execute(CmsImageCacheMaintenanceRequest.clear());
            assertEquals(1, clearResult.getSucceeded());
            assertFalse(cache.exists("folder/first.jpg"));
        } finally {
            deleteDirectory(repository);
        }
    }

    /** Tests Shared-FS adapter selection and metadata invalidation after a delete. */
    @Test
    public void testSharedFsMaintenance() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-image-maintenance");
        try {
            CmsFsImageCache cache = new CmsFsImageCache(repository.toString());
            cache.write("image.jpg", new byte[] {1, 2, 3, 4});
            assertEquals(4, cache.getLength("image.jpg"));
            CmsImageCacheMaintenanceService service = CmsImageCacheMaintenanceService.create(cache);
            List<CmsImageCacheEntry> entries = new ArrayList<CmsImageCacheEntry>();
            service.visitEntries(entries::add);

            assertEquals("shared-fs", service.getBackendId());
            assertFalse(service.getCapabilities().supports(Capability.RENEW_ENTRIES));
            assertThrows(
                UnsupportedOperationException.class,
                () -> service.execute(CmsImageCacheMaintenanceRequest.renew(entries, Instant.now())));
            CmsImageCacheMaintenanceResult result = service.execute(CmsImageCacheMaintenanceRequest.delete(entries));

            assertEquals(1, result.getSucceeded());
            assertFalse(cache.exists("image.jpg"));
            assertThrows(Exception.class, () -> cache.getLength("image.jpg"));
        } finally {
            deleteDirectory(repository);
        }
    }

    /** Tests configured, revision-checked Shared-FS renewal. */
    @Test
    public void testSharedFsRenewal() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-image-maintenance");
        try {
            CmsFsImageCache cache = new CmsFsImageCache(repository.toString());
            cache.write("first.jpg", new byte[] {1, 2, 3});
            cache.write("second.jpg", new byte[] {4, 5, 6});
            CmsImageCacheConfiguration configuration = new CmsImageCacheConfiguration();
            configuration.setRetention("renew-on-use", "P60D", "P30D", "P14D");
            configuration.setFs("true", "2");
            configuration.validate();
            CmsImageCacheMaintenanceService service = CmsImageCacheMaintenanceService.create(cache, configuration);
            List<CmsImageCacheEntry> entries = new ArrayList<CmsImageCacheEntry>();
            service.visitEntries(entries::add);
            CmsImageCacheEntry first = findEntry(entries, "first.jpg");
            CmsImageCacheEntry second = findEntry(entries, "second.jpg");
            Instant replacementTime = Instant.parse("2026-02-01T00:00:00Z");
            Files.setLastModifiedTime(repository.resolve("second.jpg"), FileTime.from(replacementTime));

            Instant renewalTime = Instant.parse("2026-03-01T00:00:00Z");
            CmsImageCacheMaintenanceResult result = service.execute(
                CmsImageCacheMaintenanceRequest.renew(Arrays.asList(first, second), renewalTime));

            assertTrue(service.getCapabilities().supports(Capability.RENEW_ENTRIES));
            assertEquals(1, result.getSucceeded());
            assertEquals(1, result.getSkipped());
            assertEquals(renewalTime, Files.getLastModifiedTime(repository.resolve("first.jpg")).toInstant());
            assertEquals(replacementTime, Files.getLastModifiedTime(repository.resolve("second.jpg")).toInstant());
        } finally {
            deleteDirectory(repository);
        }
    }

    /** Tests streaming time-based cleanup for Shared-FS. */
    @Test
    public void testSharedFsTimeBasedCleanup() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-image-maintenance");
        try {
            CmsFsImageCache cache = new CmsFsImageCache(repository.toString());
            cache.write("old.jpg", new byte[] {1});
            cache.write("recent.jpg", new byte[] {2});
            Files.setLastModifiedTime(
                repository.resolve("old.jpg"),
                FileTime.from(Instant.parse("2026-01-01T00:00:00Z")));
            Files.setLastModifiedTime(
                repository.resolve("recent.jpg"),
                FileTime.from(Instant.parse("2026-03-01T00:00:00Z")));
            CmsImageCacheMaintenanceService service = CmsImageCacheMaintenanceService.create(cache);
            List<Long> progress = new ArrayList<Long>();

            CmsImageCacheMaintenanceCleaner.Result result = CmsImageCacheMaintenanceCleaner.delete(
                service,
                Instant.parse("2026-02-01T00:00:00Z"),
                1,
                (current, batch) -> progress.add(Long.valueOf(current.getSucceeded())));

            assertEquals(2, result.getScanned());
            assertEquals(1, result.getMatched());
            assertEquals(1, result.getSucceeded());
            assertEquals(Arrays.asList(Long.valueOf(1)), progress);
            assertFalse(cache.exists("old.jpg"));
            assertTrue(cache.exists("recent.jpg"));

            CmsImageCacheMaintenanceCleaner.Result clearResult = CmsImageCacheMaintenanceCleaner.delete(
                service,
                null,
                1,
                null);
            assertEquals(1, clearResult.getSucceeded());
            assertFalse(cache.exists("recent.jpg"));
        } finally {
            deleteDirectory(repository);
        }
    }

    /** Finds an entry by key. */
    private CmsImageCacheEntry findEntry(List<CmsImageCacheEntry> entries, String key) {

        return entries.stream().filter(entry -> key.equals(entry.getKey())).findFirst().orElseThrow();
    }
}

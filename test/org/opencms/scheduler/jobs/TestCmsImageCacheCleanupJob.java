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
 */

package org.opencms.scheduler.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.opencms.configuration.CmsImageCacheConfiguration;
import org.opencms.loader.CmsFsImageCache;
import org.opencms.loader.imagecache.CmsImageCacheMaintenanceCleaner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Comparator;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/** Tests configured cleanup of external image caches. */
public class TestCmsImageCacheCleanupJob {

    /** Deletes a temporary directory tree. */
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

    /** Tests that max-age and the maximum delete count are read from the central configuration. */
    @Test
    public void testConfiguredSharedFsCleanupDeleteLimit() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-cleanup-job");
        try {
            CmsFsImageCache cache = new CmsFsImageCache(repository.toString());
            cache.write("old-1.jpg", new byte[] {1});
            cache.write("old-2.jpg", new byte[] {2});
            cache.write("recent.jpg", new byte[] {3});
            Instant now = Instant.parse("2026-08-05T12:00:00Z");
            Files.setLastModifiedTime(repository.resolve("old-1.jpg"), FileTime.from(now.minusSeconds(172800)));
            Files.setLastModifiedTime(repository.resolve("old-2.jpg"), FileTime.from(now.minusSeconds(172800)));
            Files.setLastModifiedTime(repository.resolve("recent.jpg"), FileTime.from(now.minusSeconds(3600)));
            CmsImageCacheConfiguration configuration = createConfiguration("1", "PT1M");

            CmsImageCacheMaintenanceCleaner.Result result = CmsImageCacheCleanupJob.cleanExternalImageCache(
                cache,
                configuration,
                now,
                null);

            assertEquals(1, result.getMatched());
            assertEquals(1, result.getSucceeded());
            assertTrue(result.isDeleteLimitReached());
            assertFalse(result.isRuntimeLimitReached());
            try (Stream<Path> files = Files.list(repository)) {
                assertEquals(2, files.count());
            }
            assertTrue(cache.exists("recent.jpg"));
        } finally {
            deleteDirectory(repository);
        }
    }

    /** Tests that the soft runtime limit can stop a Shared-FS scan without deleting a new entry. */
    @Test
    public void testConfiguredSharedFsCleanupRuntimeLimit() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-cleanup-job-runtime");
        try {
            CmsFsImageCache cache = new CmsFsImageCache(repository.toString());
            cache.write("old.jpg", new byte[] {1});
            Instant now = Instant.parse("2026-08-05T12:00:00Z");
            Files.setLastModifiedTime(repository.resolve("old.jpg"), FileTime.from(now.minusSeconds(172800)));
            CmsImageCacheConfiguration configuration = createConfiguration("10", "PT0.000000001S");

            CmsImageCacheMaintenanceCleaner.Result result = CmsImageCacheCleanupJob.cleanExternalImageCache(
                cache,
                configuration,
                now,
                null);

            assertEquals(0, result.getSucceeded());
            assertTrue(result.isRuntimeLimitReached());
            assertTrue(cache.exists("old.jpg"));
        } finally {
            deleteDirectory(repository);
        }
    }

    /** Tests that externally managed retention performs no OpenCms cleanup. */
    @Test
    public void testExternallyManagedRetentionSkipsCleanup() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-cleanup-job-external");
        try {
            CmsFsImageCache cache = new CmsFsImageCache(repository.toString());
            cache.write("old.jpg", new byte[] {1});
            CmsImageCacheConfiguration configuration = new CmsImageCacheConfiguration();
            configuration.setRetention("external", null, null, null);
            configuration.validate();

            CmsImageCacheMaintenanceCleaner.Result result = CmsImageCacheCleanupJob.cleanExternalImageCache(
                cache,
                configuration,
                Instant.parse("2026-08-05T12:00:00Z"),
                null);

            assertEquals(0, result.getScanned());
            assertEquals(0, result.getSucceeded());
            assertTrue(cache.exists("old.jpg"));
        } finally {
            deleteDirectory(repository);
        }
    }

    /** Creates the cleanup configuration used by tests. */
    private CmsImageCacheConfiguration createConfiguration(String maxDeletes, String maxRuntime) {

        CmsImageCacheConfiguration configuration = new CmsImageCacheConfiguration();
        configuration.setRetention("fixed", "P1D", null, null);
        configuration.setCleanup(maxDeletes, maxRuntime);
        configuration.validate();
        return configuration;
    }
}

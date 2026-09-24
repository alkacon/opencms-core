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

package org.opencms.loader.imagecache;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.opencms.configuration.CmsImageCacheConfiguration;
import org.opencms.loader.CmsFsImageCache;
import org.opencms.loader.CmsRfsImageCache;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/** Tests access-triggered image cache renewal. */
public class TestCmsImageCacheAccessRenewal {

    /** Waits for an asynchronous condition. */
    private static void await(BooleanSupplier condition) throws Exception {

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean() && (System.nanoTime() < deadline)) {
            Thread.sleep(10);
        }
        assertTrue(condition.getAsBoolean());
    }

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

    /** Tests that renew-on-use implicitly enables Shared-FS renewal while other modes and RFS remain unchanged. */
    @Test
    public void testRenewalModesAndBackends() throws Exception {

        Path repository = Files.createTempDirectory("opencms-access-renewal-disabled");
        try {
            CmsImageCacheConfiguration fsConfiguration = createRenewalConfiguration("PT0S");
            try (CmsImageCacheAccessRenewal renewal = CmsImageCacheAccessRenewal.create(
                new CmsFsImageCache(repository.toString()),
                fsConfiguration)) {
                assertNotNull(renewal);
            }
            CmsImageCacheConfiguration rfsConfiguration = createRenewalConfiguration("PT0S");
            assertNull(
                CmsImageCacheAccessRenewal.create(new CmsRfsImageCache(repository.toString()), rfsConfiguration));
            CmsImageCacheConfiguration fixedConfiguration = new CmsImageCacheConfiguration();
            fixedConfiguration.setRetention("fixed", "PT3M", null, null);
            fixedConfiguration.setFs("1");
            fixedConfiguration.validate();
            assertNull(
                CmsImageCacheAccessRenewal.create(new CmsFsImageCache(repository.toString()), fixedConfiguration));
            CmsImageCacheConfiguration externalConfiguration = new CmsImageCacheConfiguration();
            externalConfiguration.setRetention("external", null, null, null);
            externalConfiguration.setFs("1");
            externalConfiguration.validate();
            assertNull(
                CmsImageCacheAccessRenewal.create(new CmsFsImageCache(repository.toString()), externalConfiguration));
        } finally {
            deleteDirectory(repository);
        }
    }

    /** Tests the renewal window and deterministic jitter bounds. */
    @Test
    public void testRenewalWindowAndJitter() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-access-renewal");
        try {
            CmsFsImageCache cache = new CmsFsImageCache(repository.toString());
            CmsImageCacheConfiguration configuration = createRenewalConfiguration("PT1M");
            Instant now = Instant.parse("2026-08-05T12:00:00Z");
            try (CmsImageCacheAccessRenewal renewal = CmsImageCacheAccessRenewal.create(cache, configuration)) {
                assertFalse(renewal.isRenewalDue("image.jpg", now.minusSeconds(59), now));
                assertTrue(renewal.isRenewalDue("image.jpg", now.minusSeconds(121), now));
            }
        } finally {
            deleteDirectory(repository);
        }
    }

    /** Tests asynchronous Shared-FS touching after an eligible access. */
    @Test
    public void testSharedFsAccessRenewal() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-access-renewal");
        try {
            CmsFsImageCache cache = new CmsFsImageCache(repository.toString());
            cache.write("image.jpg", new byte[] {1, 2, 3});
            Path image = repository.resolve("image.jpg");
            Instant oldTime = Instant.now().minusSeconds(120);
            Files.setLastModifiedTime(image, FileTime.from(oldTime));
            CmsImageCacheConfiguration configuration = createRenewalConfiguration("PT0S");

            try (CmsImageCacheAccessRenewal renewal = CmsImageCacheAccessRenewal.create(cache, configuration)) {
                renewal.recordAccess("image.jpg");
                await(() -> {
                    try {
                        return Files.getLastModifiedTime(image).toInstant().isAfter(oldTime);
                    } catch (Exception e) {
                        return false;
                    }
                });
            }
        } finally {
            deleteDirectory(repository);
        }
    }

    /** Creates the short renewal policy used by tests. */
    private CmsImageCacheConfiguration createRenewalConfiguration(String jitter) {

        CmsImageCacheConfiguration configuration = new CmsImageCacheConfiguration();
        configuration.setRetention("renew-on-use", "PT3M", "PT2M", jitter);
        configuration.setFs("1");
        configuration.validate();
        return configuration;
    }
}

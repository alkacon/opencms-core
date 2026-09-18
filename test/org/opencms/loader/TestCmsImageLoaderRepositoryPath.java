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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.opencms.cache.CmsVfsNameBasedDiskCache;
import org.opencms.configuration.CmsImageCacheConfiguration;
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.scheduler.jobs.CmsImageCacheCleanupJob;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Collections;

import org.junit.jupiter.api.Test;

/**
 * Tests image cache repository path handling.<p>
 */
public class TestCmsImageLoaderRepositoryPath {

    /**
     * Minimal external image cache test double.<p>
     */
    private static class TestImageCache implements I_CmsImageCache {

        /** Number of close calls. */
        private int m_closeCalls;

        @Override
        public void clear() {

            // not needed
        }

        @Override
        public void close() {

            m_closeCalls++;
        }

        public boolean exists(String key) {

            return false;
        }

        public long getLength(String key) {

            return 0;
        }

        public boolean supportsRangeDelivery() {

            return false;
        }

        public void write(String key, byte[] content) {

            // not needed
        }

        public void writeRangeTo(String key, long start, long length, java.io.OutputStream out) {

            // not needed
        }

        public void writeTo(String key, java.io.OutputStream out) {

            // not needed
        }
    }

    /**
     * Tests that classic RFS cleanup remains time based.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testClassicRfsCleanupRemainsTimeBased() throws Exception {

        CmsVfsNameBasedDiskCache previousDiskCache = CmsImageLoader.m_vfsDiskCache;
        String previousRepositoryPath = CmsImageLoader.m_imageRepositoryPath;
        I_CmsImageCache previousImageCache = CmsImageLoader.m_imageCache;
        boolean previousEnabled = CmsImageLoader.m_enabled;
        Path repository = Files.createTempDirectory("opencms-rfs-image-cache-cleanup");
        Path oldEntry = Files.write(repository.resolve("old-entry"), new byte[] {1});
        Path recentEntry = Files.write(repository.resolve("recent-entry"), new byte[] {2});
        try {
            Files.setLastModifiedTime(oldEntry, FileTime.fromMillis(System.currentTimeMillis() - (2 * 60 * 60 * 1000)));
            CmsImageLoader.m_vfsDiskCache = new CmsVfsNameBasedDiskCache("", repository.toString());
            CmsImageLoader.m_imageRepositoryPath = repository.toString();
            CmsImageLoader.m_imageCache = null;

            assertEquals(1, CmsImageCacheCleanupJob.cleanImageCache(1));
            assertFalse(Files.exists(oldEntry));
            assertTrue(Files.exists(recentEntry));
        } finally {
            CmsImageLoader.m_vfsDiskCache = previousDiskCache;
            CmsImageLoader.m_imageRepositoryPath = previousRepositoryPath;
            CmsImageLoader.m_imageCache = previousImageCache;
            CmsImageLoader.m_enabled = previousEnabled;
            Files.deleteIfExists(oldEntry);
            Files.deleteIfExists(recentEntry);
            Files.deleteIfExists(repository);
        }
    }

    /** Tests timestamp-based cleanup events for an external cache. */
    @Test
    public void testExternalCacheTimeBasedCleanupEvent() throws Exception {

        I_CmsImageCache previousImageCache = CmsImageLoader.m_imageCache;
        Path repository = Files.createTempDirectory("opencms-external-image-cache-event");
        CmsFsImageCache cache = new CmsFsImageCache(repository.toString());
        try {
            cache.write("old.jpg", new byte[] {1});
            cache.write("recent.jpg", new byte[] {2});
            Files.setLastModifiedTime(
                repository.resolve("old.jpg"),
                FileTime.fromMillis(System.currentTimeMillis() - (2 * 60 * 60 * 1000)));
            CmsImageLoader.m_imageCache = cache;

            new CmsImageLoader().cmsEvent(
                new CmsEvent(
                    I_CmsEventListener.EVENT_CLEAR_CACHES,
                    Collections.<String, Object> singletonMap(CmsImageLoader.PARAM_CLEAR_IMAGES_CACHE, "1")));

            assertFalse(cache.exists("old.jpg"));
            assertTrue(cache.exists("recent.jpg"));
        } finally {
            CmsImageLoader.m_imageCache = previousImageCache;
            cache.clear();
            Files.deleteIfExists(repository);
        }
    }

    /** Tests that external image caches require an explicit retention policy. */
    @Test
    public void testExternalImageCacheRequiresConfiguration() throws Exception {

        TestImageCache cache = new TestImageCache();
        assertThrows(
            org.opencms.configuration.CmsConfigurationException.class,
            () -> CmsImageLoader.validateExternalImageCacheConfiguration(
                cache,
                CmsImageCacheConfiguration.createLegacyConfiguration()));
        assertEquals(1, cache.m_closeCalls);

        CmsImageCacheConfiguration configuration = new CmsImageCacheConfiguration();
        configuration.setRetention("fixed", "P60D", null, null);
        configuration.validate();
        CmsImageLoader.validateExternalImageCacheConfiguration(cache, configuration);
        assertEquals(1, cache.m_closeCalls);
    }

    /**
     * Tests that a shared FS image cache can not trigger the legacy RFS cleanup.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testFsImageCacheDoesNotUseLegacyRfsCleanup() throws Exception {

        CmsVfsNameBasedDiskCache previousDiskCache = CmsImageLoader.m_vfsDiskCache;
        String previousRepositoryPath = CmsImageLoader.m_imageRepositoryPath;
        I_CmsImageCache previousImageCache = CmsImageLoader.m_imageCache;
        boolean previousEnabled = CmsImageLoader.m_enabled;
        Path repository = Files.createTempDirectory("opencms-fs-image-cache-cleanup");
        CmsFsImageCache cache = new CmsFsImageCache(repository.toString());
        try {
            cache.write("sentinel", new byte[] {1});
            CmsImageLoader.m_vfsDiskCache = new CmsVfsNameBasedDiskCache("", repository.toString());
            CmsImageLoader.m_imageRepositoryPath = repository.toString();
            CmsImageLoader.m_imageCache = cache;
            CmsImageLoader.m_enabled = true;

            assertEquals(0, CmsImageCacheCleanupJob.cleanImageCache(-1));
            assertTrue(cache.exists("sentinel"));
        } finally {
            CmsImageLoader.m_vfsDiskCache = previousDiskCache;
            CmsImageLoader.m_imageRepositoryPath = previousRepositoryPath;
            CmsImageLoader.m_imageCache = previousImageCache;
            CmsImageLoader.m_enabled = previousEnabled;
            cache.clear();
            Files.deleteIfExists(repository);
        }
    }

    /**
     * Tests that an image cache without file system access can not trigger an RFS cleanup.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testNonFileSystemCacheDoesNotExposeOrCleanInternalDiskCachePath() throws Exception {

        CmsVfsNameBasedDiskCache previousDiskCache = CmsImageLoader.m_vfsDiskCache;
        String previousRepositoryPath = CmsImageLoader.m_imageRepositoryPath;
        I_CmsImageCache previousImageCache = CmsImageLoader.m_imageCache;
        boolean previousEnabled = CmsImageLoader.m_enabled;
        Path internalRepository = Files.createTempDirectory("opencms-image-cache-internal-path");
        Path sentinel = Files.write(internalRepository.resolve("must-not-be-deleted"), new byte[] {1});
        try {
            CmsImageLoader.m_vfsDiskCache = new CmsVfsNameBasedDiskCache("", internalRepository.toString());
            CmsImageLoader.m_imageRepositoryPath = null;
            CmsImageLoader.m_imageCache = new TestImageCache();
            CmsImageLoader.m_enabled = true;

            assertNull(CmsImageLoader.getImageRepositoryPath());
            assertEquals(0, CmsImageCacheCleanupJob.cleanImageCache(-1));
            assertTrue(Files.exists(sentinel));
        } finally {
            CmsImageLoader.m_vfsDiskCache = previousDiskCache;
            CmsImageLoader.m_imageRepositoryPath = previousRepositoryPath;
            CmsImageLoader.m_imageCache = previousImageCache;
            CmsImageLoader.m_enabled = previousEnabled;
            Files.deleteIfExists(sentinel);
            Files.deleteIfExists(internalRepository);
        }
    }
}

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

package org.opencms.cache;

import org.opencms.test.OpenCmsTestRunner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Tests for the name based VFS disk cache.<p>
 */
public class TestCmsVfsNameBasedDiskCache extends OpenCmsTestRunner {

    /**
     * Tests cache file creation with parent folders.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testCreateCacheFile() throws Exception {

        Path repository = Files.createTempDirectory("opencms-vfs-disk-cache");
        try {
            CmsVfsNameBasedDiskCache cache = new CmsVfsNameBasedDiskCache(repository.toString(), "/imagecache/");
            String cacheName = repository.resolve("imagecache/test/deep/cache-file.bin").toString();

            File cacheFile = cache.createCacheFile(cacheName);
            try (FileOutputStream out = new FileOutputStream(cacheFile)) {
                out.write("created cache file".getBytes(StandardCharsets.UTF_8));
            }

            assertTrue(cache.hasCacheContent(cacheName));
            assertEquals(cacheName, cache.getCacheFile(cacheName).getAbsolutePath());
        } finally {
            deleteDirectory(repository);
        }
    }

    /**
     * Tests stream based cache writes and reads.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testStreamCacheContent() throws Exception {

        Path repository = Files.createTempDirectory("opencms-vfs-disk-cache");
        try {
            CmsVfsNameBasedDiskCache cache = new CmsVfsNameBasedDiskCache(repository.toString(), "/imagecache/");
            String cacheName = repository.resolve("imagecache/test/stream-cache.bin").toString();
            byte[] content = "stream cache content".getBytes(StandardCharsets.UTF_8);

            cache.saveCacheFile(cacheName, new ByteArrayInputStream(content));

            assertTrue(cache.hasCacheContent(cacheName));
            assertTrue(Arrays.equals(content, cache.getCacheContent(cacheName)));
            try (InputStream in = cache.getCacheInputStream(cacheName)) {
                assertTrue(Arrays.equals(content, readAll(in)));
            }
            assertEquals(cacheName, cache.getCacheFile(cacheName).getAbsolutePath());
        } finally {
            deleteDirectory(repository);
        }
    }

    /**
     * Deletes a directory tree if it exists.<p>
     *
     * @param directory the directory
     *
     * @throws Exception if deleting fails
     */
    private void deleteDirectory(Path directory) throws Exception {

        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
        }
    }

    /**
     * Reads all data from an input stream.<p>
     *
     * @param in the input stream
     *
     * @return the bytes
     *
     * @throws Exception if reading fails
     */
    private byte[] readAll(InputStream in) throws Exception {

        byte[] buffer = new byte[8192];
        int read;
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        while ((read = in.read(buffer)) >= 0) {
            if (read > 0) {
                out.write(buffer, 0, read);
            }
        }
        return out.toByteArray();
    }
}

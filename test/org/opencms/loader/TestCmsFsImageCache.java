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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Tests the FS image cache implementation.<p>
 */
public class TestCmsFsImageCache {

    /**
     * Deletes a directory tree.<p>
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

    /**
     * Tests that concurrent writers leave one complete cache entry.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testConcurrentWritesAreAtomic() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-image-cache");
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            CmsFsImageCache cache = new CmsFsImageCache(repository.toString());
            String key = "/sites/default/.galleries/concurrent.jpg";
            byte[] firstContent = new byte[1024 * 1024];
            byte[] secondContent = new byte[1024 * 1024];
            Arrays.fill(firstContent, (byte)1);
            Arrays.fill(secondContent, (byte)2);
            CountDownLatch start = new CountDownLatch(1);
            Set<Future<?>> writers = new HashSet<>();
            for (int i = 0; i < 8; i++) {
                final byte[] content = ((i % 2) == 0) ? firstContent : secondContent;
                writers.add(executor.submit(() -> {
                    start.await();
                    cache.write(key, content);
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> writer : writers) {
                writer.get();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            cache.writeTo(key, out);
            byte[] storedContent = out.toByteArray();
            assertTrue(Arrays.equals(firstContent, storedContent) || Arrays.equals(secondContent, storedContent));
            try (Stream<Path> paths = Files.walk(repository)) {
                assertFalse(paths.anyMatch(path -> path.getFileName().toString().endsWith(".tmp")));
            }
        } finally {
            executor.shutdownNow();
            deleteDirectory(repository);
        }
    }

    /**
     * Tests listing and clearing all cache entries.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testListAndClear() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-image-cache");
        try {
            CmsFsImageCache cache = new CmsFsImageCache(repository.toString());
            byte[] first = "first".getBytes(StandardCharsets.UTF_8);
            byte[] second = "second entry".getBytes(StandardCharsets.UTF_8);
            cache.write("/sites/default/first.jpg_1.jpg", first);
            cache.write("/sites/default/folder/second.jpg_2.jpg", second);

            Map<String, Long> entries = new HashMap<>();
            cache.visitEntries((key, length) -> entries.put(key, Long.valueOf(length)));

            assertEquals(2, entries.size());
            assertEquals(Long.valueOf(first.length), entries.get("sites/default/first.jpg_1.jpg"));
            assertEquals(Long.valueOf(second.length), entries.get("sites/default/folder/second.jpg_2.jpg"));

            cache.clear();

            assertTrue(Files.isDirectory(repository));
            assertFalse(cache.exists("/sites/default/first.jpg_1.jpg"));
            assertFalse(cache.exists("/sites/default/folder/second.jpg_2.jpg"));
            Map<String, Long> emptyEntries = new HashMap<>();
            cache.visitEntries((key, length) -> emptyEntries.put(key, Long.valueOf(length)));
            assertTrue(emptyEntries.isEmpty());
        } finally {
            deleteDirectory(repository);
        }
    }

    /** Tests that a vanished entry is reported as a recoverable image cache miss. */
    @Test
    public void testMissingEntryIsReportedForSelfHealing() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-image-cache");
        try {
            CmsFsImageCache cache = new CmsFsImageCache(repository.toString());
            String key = "sites/default/image.jpg";
            cache.write(key, new byte[] {1, 2, 3});
            assertEquals(3, cache.getLength(key));
            Files.delete(cache.getPath(key));

            assertThrows(
                CmsImageCacheEntryNotFoundException.class,
                () -> cache.writeTo(key, new ByteArrayOutputStream()));
            assertFalse(cache.existsAuthoritatively(key));
        } finally {
            deleteDirectory(repository);
        }
    }

    /**
     * Tests that prefix listing returns only entries below the selected cache path.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testPrefixListing() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-image-cache");
        try {
            CmsFsImageCache cache = new CmsFsImageCache(repository.toString());
            cache.write("sites/default/folder/first.jpg", new byte[] {1});
            cache.write("sites/default/other/second.jpg", new byte[] {2});
            cache.write("sites/other/third.jpg", new byte[] {3});
            Map<String, Long> entries = new HashMap<String, Long>();

            cache.visitEntries("/sites/default/folder/", (key, length) -> entries.put(key, Long.valueOf(length)));

            assertEquals(1, entries.size());
            assertEquals(Long.valueOf(1), entries.get("sites/default/folder/first.jpg"));
        } finally {
            deleteDirectory(repository);
        }
    }

    /**
     * Tests that cache keys can not escape the repository root.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testRejectsPathTraversal() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-image-cache");
        try {
            CmsFsImageCache cache = new CmsFsImageCache(repository.toString());
            assertThrows(IllegalArgumentException.class, () -> cache.write("../outside.jpg", new byte[] {1}));
        } finally {
            deleteDirectory(repository);
        }
    }

    /**
     * Tests write, full delivery and range delivery.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testWriteAndRangeDelivery() throws Exception {

        Path repository = Files.createTempDirectory("opencms-fs-image-cache");
        try {
            CmsFsImageCache cache = new CmsFsImageCache(repository.toString());
            byte[] content = "0123456789abcdef".getBytes(StandardCharsets.UTF_8);
            String key = "/sites/default/.galleries/image.jpg_123.jpg";

            cache.write(key, content);

            assertTrue(cache.exists(key));
            assertEquals(content.length, cache.getLength(key));
            assertTrue(cache.supportsRangeDelivery());

            ByteArrayOutputStream full = new ByteArrayOutputStream();
            cache.writeTo(key, full);
            assertTrue(Arrays.equals(content, full.toByteArray()));

            ByteArrayOutputStream range = new ByteArrayOutputStream();
            cache.writeRangeTo(key, 4, 6, range);
            assertEquals("456789", range.toString("UTF-8"));

            cache.write(key, new byte[] {1, 2, 3});
            assertEquals(3, cache.getLength(key));
            Files.delete(cache.getPath(key));
            assertFalse(cache.exists(key));
        } finally {
            deleteDirectory(repository);
        }
    }
}

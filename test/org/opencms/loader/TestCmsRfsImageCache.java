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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Tests the RFS image cache implementation.<p>
 */
public class TestCmsRfsImageCache {

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
     * Tests that cache keys can not escape the repository root.<p>
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void testRejectsPathTraversal() throws Exception {

        Path repository = Files.createTempDirectory("opencms-rfs-image-cache");
        try {
            CmsRfsImageCache cache = new CmsRfsImageCache(repository.toString());
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

        Path repository = Files.createTempDirectory("opencms-rfs-image-cache");
        try {
            CmsRfsImageCache cache = new CmsRfsImageCache(repository.toString());
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
        } finally {
            deleteDirectory(repository);
        }
    }
}

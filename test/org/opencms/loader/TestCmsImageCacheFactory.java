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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.db.storage.CmsStorageManager;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests the properties based image cache selection.<p>
 */
public class TestCmsImageCacheFactory {

    /** Temporary test directory. */
    @TempDir
    private Path m_tempDir;

    /**
     * Tests that an FS image cache uses its own repository.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testCreatesSeparateFsImageCache() throws Exception {

        Path imageCachePath = m_tempDir.resolve("imagecache");
        CmsParameterConfiguration configuration = baseConfiguration();
        configuration.add(CmsStorageManager.PARAM_STORAGE_IMAGE_CACHE, "images");
        configuration.add("storage.backend.images.type", "fs");
        configuration.add("storage.backend.images.path", imageCachePath.toString());

        I_CmsImageCache cache = CmsImageCacheFactory.create(configuration);

        CmsFsImageCache fsCache = assertInstanceOf(CmsFsImageCache.class, cache);
        assertEquals(imageCachePath.toAbsolutePath().normalize().toString(), fsCache.getRepositoryPath());
        try (java.util.stream.Stream<Path> entries = Files.list(imageCachePath)) {
            assertEquals(0, entries.count());
        }
    }

    /** Tests that database storage can not be selected as external image cache. */
    @Test
    public void testRejectsDbImageCacheBackend() {

        CmsParameterConfiguration configuration = baseConfiguration();
        configuration.add(CmsStorageManager.PARAM_STORAGE_IMAGE_CACHE, "db");

        assertThrows(CmsConfigurationException.class, () -> CmsImageCacheFactory.create(configuration));
    }

    /** Tests that an FS cache may not overlap a data storage repository. */
    @Test
    public void testRejectsOverlappingFsRepositories() {

        CmsParameterConfiguration configuration = new CmsParameterConfiguration();
        configuration.add(CmsStorageManager.PARAM_STORAGE_ACTIVE, "data");
        configuration.add("storage.backend.data.type", "fs");
        configuration.add("storage.backend.data.path", m_tempDir.resolve("shared").toString());
        configuration.add(CmsStorageManager.PARAM_STORAGE_IMAGE_CACHE, "images");
        configuration.add("storage.backend.images.type", "fs");
        configuration.add("storage.backend.images.path", m_tempDir.resolve("shared/imagecache").toString());

        assertThrows(CmsConfigurationException.class, () -> CmsImageCacheFactory.create(configuration));
    }

    /** Tests that FS image caches do not allow an object key prefix. */
    @Test
    public void testRejectsPrefixForFsImageCache() {

        CmsParameterConfiguration configuration = baseConfiguration();
        configuration.add(CmsStorageManager.PARAM_STORAGE_IMAGE_CACHE, "images");
        configuration.add(CmsStorageManager.PARAM_STORAGE_IMAGE_CACHE_PREFIX, "imagecache/");
        configuration.add("storage.backend.images.type", "fs");
        configuration.add("storage.backend.images.path", m_tempDir.resolve("imagecache").toString());

        assertThrows(CmsConfigurationException.class, () -> CmsImageCacheFactory.create(configuration));
    }

    /** Tests that a separate S3 bucket does not allow a prefix. */
    @Test
    public void testRejectsPrefixForSeparateS3Bucket() {

        CmsParameterConfiguration configuration = baseConfiguration();
        configuration.add(CmsStorageManager.PARAM_STORAGE_IMAGE_CACHE, "images");
        configuration.add(CmsStorageManager.PARAM_STORAGE_IMAGE_CACHE_PREFIX, "imagecache/");
        addS3Backend(configuration, "images", "http://localhost:9000", "image-bucket");

        assertThrows(CmsConfigurationException.class, () -> CmsImageCacheFactory.create(configuration));
    }

    /** Tests that a prefix without an external image cache is rejected. */
    @Test
    public void testRejectsPrefixWithoutImageCacheBackend() {

        CmsParameterConfiguration configuration = baseConfiguration();
        configuration.add(CmsStorageManager.PARAM_STORAGE_IMAGE_CACHE_PREFIX, "imagecache/");

        assertThrows(CmsConfigurationException.class, () -> CmsImageCacheFactory.create(configuration));
    }

    /** Tests that sharing a legacy S3 data bucket also requires a prefix. */
    @Test
    public void testRequiresPrefixForLegacyS3Bucket() {

        CmsParameterConfiguration configuration = baseConfiguration();
        configuration.add(CmsStorageManager.PARAM_STORAGE_LEGACY, "olddata");
        configuration.add(CmsStorageManager.PARAM_STORAGE_IMAGE_CACHE, "images");
        addS3Backend(configuration, "olddata", "http://localhost:9000/", "shared-bucket");
        addS3Backend(configuration, "images", "http://localhost:9000", "shared-bucket");

        assertThrows(CmsConfigurationException.class, () -> CmsImageCacheFactory.create(configuration));
    }

    /** Tests that a shared S3 bucket requires a prefix. */
    @Test
    public void testRequiresPrefixForSharedS3Bucket() {

        CmsParameterConfiguration configuration = new CmsParameterConfiguration();
        configuration.add(CmsStorageManager.PARAM_STORAGE_ACTIVE, "shared");
        configuration.add(CmsStorageManager.PARAM_STORAGE_IMAGE_CACHE, "shared");
        addS3Backend(configuration, "shared", "http://localhost:9000", "shared-bucket");

        assertThrows(CmsConfigurationException.class, () -> CmsImageCacheFactory.create(configuration));
    }

    /** Tests that missing image cache selection keeps the classic RFS cache. */
    @Test
    public void testUsesClassicRfsWhenImageCacheIsNotConfigured() throws Exception {

        assertNull(CmsImageCacheFactory.create(baseConfiguration()));
    }

    /**
     * Adds a complete S3 backend configuration.<p>
     *
     * @param configuration the configuration
     * @param id the backend id
     * @param endpoint the endpoint
     * @param bucket the bucket
     */
    private void addS3Backend(CmsParameterConfiguration configuration, String id, String endpoint, String bucket) {

        String prefix = "storage.backend." + id + ".";
        configuration.add(prefix + "type", "s3");
        configuration.add(prefix + "endpoint", endpoint);
        configuration.add(prefix + "bucket", bucket);
        configuration.add(prefix + "accessKey", "access");
        configuration.add(prefix + "secretKey", "secret");
    }

    /** Returns a configuration using the default database data storage. */
    private CmsParameterConfiguration baseConfiguration() {

        CmsParameterConfiguration result = new CmsParameterConfiguration();
        result.add(CmsStorageManager.PARAM_STORAGE_ACTIVE, "db");
        return result;
    }
}

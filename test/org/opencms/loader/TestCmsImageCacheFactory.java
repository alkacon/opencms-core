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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.db.storage.CmsStorageManager;
import org.opencms.db.storage.s3.CmsS3ClientConfiguration;

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

    /** Tests offline validation of independent S3 buckets on the same endpoint. */
    @Test
    public void testAcceptsSeparateS3Bucket() {

        CmsParameterConfiguration configuration = baseConfiguration();
        configuration.add(CmsStorageManager.PARAM_STORAGE_ACTIVE, "data");
        addS3Configuration(configuration, "storage.backend.data.", "http://localhost:9000", "data-bucket");
        addS3Configuration(configuration, "storage.imagecache.", "http://localhost:9000", "image-bucket");

        assertDoesNotThrow(() -> CmsImageCacheFactory.validateConfiguration(configuration));
    }

    /** Tests that equal bucket names on different endpoints do not conflict. */
    @Test
    public void testAcceptsSeparateS3Endpoint() {

        CmsParameterConfiguration configuration = baseConfiguration();
        configuration.add(CmsStorageManager.PARAM_STORAGE_ACTIVE, "data");
        addS3Configuration(configuration, "storage.backend.data.", "http://localhost:9000", "bucket");
        addS3Configuration(configuration, "storage.imagecache.", "http://localhost:9001", "bucket");

        assertDoesNotThrow(() -> CmsImageCacheFactory.validateConfiguration(configuration));
    }

    /**
     * Tests that an FS image cache uses its own repository.<p>
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testCreatesSeparateFsImageCache() throws Exception {

        Path imageCachePath = m_tempDir.resolve("imagecache");
        CmsParameterConfiguration configuration = baseConfiguration();
        configuration.add("storage.imagecache.type", "fs");
        configuration.add("storage.imagecache.path", imageCachePath.toString());

        I_CmsImageCache cache = CmsImageCacheFactory.create(configuration);

        CmsFsImageCache fsCache = assertInstanceOf(CmsFsImageCache.class, cache);
        assertEquals(imageCachePath.toAbsolutePath().normalize().toString(), fsCache.getRepositoryPath());
        try (java.util.stream.Stream<Path> entries = Files.list(imageCachePath)) {
            assertEquals(0, entries.count());
        }
    }

    /** Tests that direct S3 settings use the shared defaults and connection options. */
    @Test
    public void testReadsDirectS3Settings() {

        CmsParameterConfiguration configuration = baseConfiguration();
        addS3Configuration(configuration, "storage.imagecache.", "http://localhost:9000", "image-bucket");
        configuration.add("storage.imagecache.region", "eu-central-1");
        configuration.add("storage.imagecache.pathStyle", "false");
        configuration.add("storage.imagecache.maxConnections", "17");
        configuration.add("storage.imagecache.connectionTimeout", "1234");
        CmsS3ClientConfiguration s3 = CmsStorageManager.createS3ClientConfiguration(
            configuration,
            "storage.imagecache.");

        assertEquals("image-bucket", s3.getBucketName());
        assertEquals("http://localhost:9000", s3.getEndpoint());
        assertEquals("access", s3.getAccessKey());
        assertEquals("secret", s3.getSecretKey());
        assertEquals("eu-central-1", s3.getRegion());
        assertEquals(false, s3.isPathStyle());
        assertEquals(17, s3.getMaxConnections());
        assertEquals(1234, s3.getConnectionTimeout());
        assertEquals(CmsS3ClientConfiguration.DEFAULT_SOCKET_TIMEOUT, s3.getSocketTimeout());
    }

    /** Tests that the S3 cache may not share an active data bucket. */
    @Test
    public void testRejectsActiveDataBucket() {

        CmsParameterConfiguration configuration = baseConfiguration();
        configuration.add(CmsStorageManager.PARAM_STORAGE_ACTIVE, "data");
        addS3Configuration(configuration, "storage.backend.data.", "http://localhost:9000/", "shared-bucket");
        addS3Configuration(configuration, "storage.imagecache.", "http://localhost:9000", "shared-bucket");

        CmsConfigurationException error = assertThrows(
            CmsConfigurationException.class,
            () -> CmsImageCacheFactory.create(configuration));
        assertTrue(error.getMessage().contains("shared-bucket"));
    }

    /** Tests that database storage can not be selected as external image cache. */
    @Test
    public void testRejectsDbImageCacheBackend() {

        CmsParameterConfiguration configuration = baseConfiguration();
        configuration.add("storage.imagecache.type", "db");

        assertThrows(CmsConfigurationException.class, () -> CmsImageCacheFactory.create(configuration));
    }

    /** Tests that the S3 cache may not share a legacy data bucket. */
    @Test
    public void testRejectsLegacyDataBucket() {

        CmsParameterConfiguration configuration = baseConfiguration();
        configuration.add(CmsStorageManager.PARAM_STORAGE_LEGACY, "olddata");
        addS3Configuration(configuration, "storage.backend.olddata.", "http://localhost:9000/", "shared-bucket");
        addS3Configuration(configuration, "storage.imagecache.", "http://localhost:9000", "shared-bucket");

        CmsConfigurationException error = assertThrows(
            CmsConfigurationException.class,
            () -> CmsImageCacheFactory.validateConfiguration(configuration));
        assertTrue(error.getMessage().contains("shared-bucket"));
    }

    /** Tests that an FS cache requires its own path. */
    @Test
    public void testRejectsMissingFsPath() {

        CmsParameterConfiguration configuration = baseConfiguration();
        configuration.add("storage.imagecache.type", "fs");
        CmsConfigurationException error = assertThrows(
            CmsConfigurationException.class,
            () -> CmsImageCacheFactory.create(configuration));
        assertTrue(error.getCause().getMessage().contains("storage.imagecache.path"));
    }

    /** Tests that a missing S3 bucket fails before any network access. */
    @Test
    public void testRejectsMissingS3Bucket() {

        CmsParameterConfiguration configuration = baseConfiguration();
        addS3Configuration(configuration, "storage.imagecache.", "http://localhost:9000", " ");
        CmsConfigurationException error = assertThrows(
            CmsConfigurationException.class,
            () -> CmsImageCacheFactory.create(configuration));
        assertTrue(error.getCause().getMessage().contains("storage.imagecache.bucket"));
    }

    /** Tests that an FS cache may not overlap a data storage repository. */
    @Test
    public void testRejectsOverlappingFsRepositories() {

        CmsParameterConfiguration configuration = new CmsParameterConfiguration();
        configuration.add(CmsStorageManager.PARAM_STORAGE_ACTIVE, "data");
        configuration.add("storage.backend.data.type", "fs");
        configuration.add("storage.backend.data.path", m_tempDir.resolve("shared").toString());
        configuration.add("storage.imagecache.type", "fs");
        configuration.add("storage.imagecache.path", m_tempDir.resolve("shared/imagecache").toString());

        assertThrows(CmsConfigurationException.class, () -> CmsImageCacheFactory.create(configuration));
    }

    /** Tests that an FS cache may not overlap a legacy data storage repository. */
    @Test
    public void testRejectsOverlappingLegacyFsRepository() {

        CmsParameterConfiguration configuration = baseConfiguration();
        configuration.add("storage.legacy", "olddata");
        configuration.add("storage.backend.olddata.type", "fs");
        configuration.add("storage.backend.olddata.path", m_tempDir.resolve("shared/data").toString());
        configuration.add("storage.imagecache.type", "fs");
        configuration.add("storage.imagecache.path", m_tempDir.resolve("shared").toString());

        assertThrows(CmsConfigurationException.class, () -> CmsImageCacheFactory.validateConfiguration(configuration));
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
     * @param prefix the property prefix
     * @param endpoint the endpoint
     * @param bucket the bucket
     */
    private void addS3Configuration(
        CmsParameterConfiguration configuration,
        String prefix,
        String endpoint,
        String bucket) {

        configuration.add(prefix + "type", "s3");
        configuration.add(prefix + "endpoint", endpoint);
        configuration.add(prefix + "bucket", bucket);
        configuration.add(prefix + "accessKey", "access");
        configuration.add(prefix + "secretKey", "secret");
    }

    /** Returns a configuration using the default database data storage. */
    private CmsParameterConfiguration baseConfiguration() {

        return new CmsParameterConfiguration();
    }
}

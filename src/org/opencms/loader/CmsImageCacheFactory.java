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

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.db.storage.CmsFsStorage;
import org.opencms.db.storage.CmsS3Storage;
import org.opencms.db.storage.CmsStorageManager;
import org.opencms.db.storage.s3.CmsS3ClientConfiguration;
import org.opencms.util.CmsStringUtil;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Creates the external image cache selected in {@code opencms.properties}.<p>
 */
public final class CmsImageCacheFactory {

    /** Prefix for the direct image cache configuration. */
    public static final String PARAM_IMAGE_CACHE_PREFIX = "storage.imagecache.";

    /** Property selecting the image cache type. */
    public static final String PARAM_IMAGE_CACHE_TYPE = PARAM_IMAGE_CACHE_PREFIX + "type";

    /** Hidden constructor. */
    private CmsImageCacheFactory() {

        // utility class
    }

    /**
     * Creates the configured external image cache.<p>
     *
     * @param configuration the runtime property configuration
     * @return the configured cache, or <code>null</code> for the classic RFS image cache
     * @throws CmsConfigurationException if the configuration is invalid or the cache is unavailable
     */
    public static I_CmsImageCache create(CmsParameterConfiguration configuration) throws CmsConfigurationException {

        validateConfiguration(configuration);
        String type = getType(configuration);
        try {
            if (CmsS3Storage.STORAGE_TYPE.equals(type)) {
                return new CmsS3ImageCache(
                    CmsStorageManager.createS3ClientConfiguration(configuration, PARAM_IMAGE_CACHE_PREFIX));
            }
            if (CmsFsStorage.STORAGE_TYPE.equals(type)) {
                return new CmsFsImageCache(getFileSystemPath(configuration).toString());
            }
            return null;
        } catch (Exception e) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_IMAGE_CACHE_INIT_1, type), e);
        }
    }

    /**
     * Validates the image cache settings and separation from active and legacy data storage.<p>
     *
     * This does not open a cache or access S3, so offline maintenance tools can use the same validation.
     *
     * @param configuration the runtime property configuration
     * @throws CmsConfigurationException if the configuration is invalid
     */
    public static void validateConfiguration(CmsParameterConfiguration configuration) throws CmsConfigurationException {

        String type = getType(configuration);
        if (type == null) {
            return;
        }
        try {
            if (CmsS3Storage.STORAGE_TYPE.equals(type)) {
                validateS3Configuration(configuration);
            } else if (CmsFsStorage.STORAGE_TYPE.equals(type)) {
                validateFileSystemConfiguration(configuration);
            } else {
                throw new CmsConfigurationException(
                    Messages.get().container(Messages.ERR_IMAGE_CACHE_CONFIG_TYPE_2, type, PARAM_IMAGE_CACHE_TYPE));
            }
        } catch (CmsConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_IMAGE_CACHE_INIT_1, type), e);
        }
    }

    /** Returns the required image cache root path. */
    private static Path getFileSystemPath(CmsParameterConfiguration configuration) {

        String path = configuration.getString(PARAM_IMAGE_CACHE_PREFIX + "path", null);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(path)) {
            throw new IllegalArgumentException("Missing required configuration property: storage.imagecache.path");
        }
        return normalizePath(path.trim());
    }

    /** Returns the external cache type, or null for the classic local cache. */
    private static String getType(CmsParameterConfiguration configuration) {

        String type = configuration.getString(PARAM_IMAGE_CACHE_TYPE, null);
        return CmsStringUtil.isEmptyOrWhitespaceOnly(type) ? null : type.trim();
    }

    /** Normalizes an S3 endpoint for comparisons. */
    private static String normalizeEndpoint(String endpoint) {

        String result = URI.create(endpoint.trim()).normalize().toString();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result.toLowerCase(java.util.Locale.ROOT);
    }

    /** Normalizes a file system path. */
    private static Path normalizePath(String path) {

        return Paths.get(path).toAbsolutePath().normalize();
    }

    /** Ensures the FS cache does not overlap any data storage repository. */
    private static void validateFileSystemConfiguration(CmsParameterConfiguration configuration)
    throws CmsConfigurationException {

        Path imageCachePath = getFileSystemPath(configuration);
        for (String dataStorageId : CmsStorageManager.getDataStorageIds(configuration)) {
            if (!CmsFsStorage.STORAGE_TYPE.equals(CmsStorageManager.getStorageType(configuration, dataStorageId))) {
                continue;
            }
            Path dataStoragePath = normalizePath(
                CmsStorageManager.getFileSystemStoragePath(configuration, dataStorageId));
            if (imageCachePath.startsWith(dataStoragePath) || dataStoragePath.startsWith(imageCachePath)) {
                throw new CmsConfigurationException(
                    Messages.get().container(
                        Messages.ERR_IMAGE_CACHE_CONFIG_FS_PATH_2,
                        imageCachePath,
                        dataStoragePath));
            }
        }
    }

    /** Ensures the S3 cache has its own bucket, separate from all data storage buckets. */
    private static void validateS3Configuration(CmsParameterConfiguration configuration)
    throws CmsConfigurationException {

        CmsS3ClientConfiguration imageCache = CmsStorageManager.createS3ClientConfiguration(
            configuration,
            PARAM_IMAGE_CACHE_PREFIX);
        for (String dataStorageId : CmsStorageManager.getDataStorageIds(configuration)) {
            if (!CmsS3Storage.STORAGE_TYPE.equals(CmsStorageManager.getStorageType(configuration, dataStorageId))) {
                continue;
            }
            CmsS3ClientConfiguration data = CmsStorageManager.createS3ClientConfiguration(
                configuration,
                dataStorageId,
                null);
            if (normalizeEndpoint(imageCache.getEndpoint()).equals(normalizeEndpoint(data.getEndpoint()))
                && imageCache.getBucketName().equals(data.getBucketName())) {
                throw new CmsConfigurationException(
                    Messages.get().container(Messages.ERR_IMAGE_CACHE_CONFIG_S3_BUCKET_1, imageCache.getBucketName()));
            }
        }
    }
}

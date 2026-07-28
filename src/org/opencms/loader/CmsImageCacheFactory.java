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

        String backendId = CmsStorageManager.getImageCacheStorageId(configuration);
        if (backendId == null) {
            validatePrefixAbsent(configuration, "RFS");
            return null;
        }
        String type = CmsStorageManager.getStorageType(configuration, backendId);
        try {
            if (CmsS3Storage.STORAGE_TYPE.equals(type)) {
                return createS3ImageCache(configuration, backendId);
            }
            if (CmsFsStorage.STORAGE_TYPE.equals(type)) {
                return createFsImageCache(configuration, backendId);
            }
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_IMAGE_CACHE_CONFIG_TYPE_2, type, backendId));
        } catch (CmsConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_IMAGE_CACHE_INIT_1, backendId),
                e);
        }
    }

    /**
     * Creates a file system image cache.<p>
     *
     * @param configuration the runtime property configuration
     * @param backendId the image cache backend id
     * @return the image cache
     * @throws Exception if configuration or initialization fails
     */
    private static I_CmsImageCache createFsImageCache(CmsParameterConfiguration configuration, String backendId)
    throws Exception {

        validatePrefixAbsent(configuration, backendId);
        Path imageCachePath = normalizePath(CmsStorageManager.getFileSystemStoragePath(configuration, backendId));
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
        return new CmsFsImageCache(imageCachePath.toString());
    }

    /**
     * Creates an S3 image cache.<p>
     *
     * @param configuration the runtime property configuration
     * @param backendId the image cache backend id
     * @return the image cache
     * @throws Exception if configuration or initialization fails
     */
    private static I_CmsImageCache createS3ImageCache(CmsParameterConfiguration configuration, String backendId)
    throws Exception {

        CmsS3ClientConfiguration imageCacheConfiguration = CmsStorageManager.createS3ClientConfiguration(
            configuration,
            backendId,
            null);
        boolean sharesDataBucket = false;
        for (String dataStorageId : CmsStorageManager.getDataStorageIds(configuration)) {
            if (!CmsS3Storage.STORAGE_TYPE.equals(CmsStorageManager.getStorageType(configuration, dataStorageId))) {
                continue;
            }
            CmsS3ClientConfiguration dataConfiguration = CmsStorageManager.createS3ClientConfiguration(
                configuration,
                dataStorageId,
                null);
            if (isSameS3Bucket(imageCacheConfiguration, dataConfiguration)) {
                sharesDataBucket = true;
                break;
            }
        }
        String prefix = normalizePrefix(
            configuration.getString(CmsStorageManager.PARAM_STORAGE_IMAGE_CACHE_PREFIX, null));
        if (sharesDataBucket && (prefix == null)) {
            throw new CmsConfigurationException(
                Messages.get().container(
                    Messages.ERR_IMAGE_CACHE_CONFIG_S3_PREFIX_REQUIRED_1,
                    imageCacheConfiguration.getBucketName()));
        }
        if (!sharesDataBucket && (prefix != null)) {
            throw new CmsConfigurationException(
                Messages.get().container(
                    Messages.ERR_IMAGE_CACHE_CONFIG_S3_PREFIX_FORBIDDEN_1,
                    imageCacheConfiguration.getBucketName()));
        }
        return new CmsS3ImageCache(imageCacheConfiguration, prefix);
    }

    /**
     * Returns whether two configurations address the same physical S3 bucket.<p>
     *
     * @param first the first configuration
     * @param second the second configuration
     * @return whether endpoint and bucket match
     */
    private static boolean isSameS3Bucket(CmsS3ClientConfiguration first, CmsS3ClientConfiguration second) {

        return normalizeEndpoint(first.getEndpoint()).equals(normalizeEndpoint(second.getEndpoint()))
            && first.getBucketName().equals(second.getBucketName());
    }

    /**
     * Normalizes an S3 endpoint for comparisons.<p>
     *
     * @param endpoint the endpoint
     * @return the normalized endpoint
     */
    private static String normalizeEndpoint(String endpoint) {

        String result = URI.create(endpoint.trim()).normalize().toString();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result.toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Normalizes a file system path.<p>
     *
     * @param path the path
     * @return the absolute normalized path
     */
    private static Path normalizePath(String path) {

        return Paths.get(path).toAbsolutePath().normalize();
    }

    /**
     * Normalizes an image cache prefix.<p>
     *
     * @param prefix the raw prefix
     * @return the normalized prefix, or <code>null</code>
     */
    private static String normalizePrefix(String prefix) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(prefix)) {
            return null;
        }
        String result = prefix.trim();
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(result) || result.contains("..")) {
            throw new IllegalArgumentException("Invalid image cache prefix: " + prefix);
        }
        return result + "/";
    }

    /**
     * Ensures no image cache prefix is configured.<p>
     *
     * @param configuration the runtime property configuration
     * @param target the configured cache description
     * @throws CmsConfigurationException if a prefix is configured
     */
    private static void validatePrefixAbsent(CmsParameterConfiguration configuration, String target)
    throws CmsConfigurationException {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(
            configuration.getString(CmsStorageManager.PARAM_STORAGE_IMAGE_CACHE_PREFIX, null))) {
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_IMAGE_CACHE_CONFIG_PREFIX_FORBIDDEN_1, target));
        }
    }
}

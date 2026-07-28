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

import org.opencms.db.storage.s3.CmsGenericS3Client;
import org.opencms.db.storage.s3.CmsS3ClientConfiguration;
import org.opencms.db.storage.s3.I_CmsS3Client;
import org.opencms.util.CmsStringUtil;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * S3 based storage for generated image cache entries.<p>
 */
public class CmsS3ImageCache implements I_CmsImageCache {

    /** Maximum number of S3 object lengths kept locally. */
    private static final int LENGTH_CACHE_MAX_SIZE = 10000;

    /** Number of minutes after which a cached S3 object length expires. */
    private static final int LENGTH_CACHE_EXPIRY_MINUTES = 10;

    /** Locally cached S3 object lengths. */
    private Cache<String, Long> m_lengthCache = CacheBuilder.newBuilder().maximumSize(
        LENGTH_CACHE_MAX_SIZE).expireAfterAccess(LENGTH_CACHE_EXPIRY_MINUTES, TimeUnit.MINUTES).build();

    /** The S3 client. */
    private I_CmsS3Client m_s3Client;

    /** The object key prefix. */
    private String m_prefix = "";

    /**
     * Creates a new image cache.<p>
     *
     * @param configuration the S3 configuration
     * @throws Exception if the configured bucket can not be accessed
     */
    public CmsS3ImageCache(CmsS3ClientConfiguration configuration)
    throws Exception {

        this(configuration, null);
    }

    /**
     * Creates a new image cache.<p>
     *
     * @param configuration the S3 configuration
     * @param prefix the object key prefix
     * @throws Exception if the configured bucket can not be accessed
     */
    public CmsS3ImageCache(CmsS3ClientConfiguration configuration, String prefix)
    throws Exception {

        initClient(new CmsGenericS3Client(configuration), prefix);
    }

    /**
     * Creates a new image cache with a custom client.<p>
     *
     * @param s3Client the S3 client
     * @throws Exception if the configured bucket can not be accessed
     */
    CmsS3ImageCache(I_CmsS3Client s3Client)
    throws Exception {

        this(s3Client, null);
    }

    /**
     * Creates a new image cache with a custom client.<p>
     *
     * @param s3Client the S3 client
     * @param prefix the object key prefix
     * @throws Exception if the configured bucket can not be accessed
     */
    CmsS3ImageCache(I_CmsS3Client s3Client, String prefix)
    throws Exception {

        initClient(s3Client, prefix);
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#close()
     */
    @Override
    public void close() throws Exception {

        I_CmsS3Client client = m_s3Client;
        m_s3Client = null;
        m_lengthCache.invalidateAll();
        if (client != null) {
            client.close();
        }
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#exists(java.lang.String)
     */
    public boolean exists(String key) throws Exception {

        ensureInitialized();
        String objectKey = getObjectKey(key);
        Long cachedLength = m_lengthCache.getIfPresent(objectKey);
        if (cachedLength != null) {
            return true;
        }
        long length = m_s3Client.getObjectLengthIfExists(objectKey);
        if (length >= 0) {
            m_lengthCache.put(objectKey, Long.valueOf(length));
            return true;
        }
        return false;
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#getLength(java.lang.String)
     */
    public long getLength(String key) throws Exception {

        ensureInitialized();
        String objectKey = getObjectKey(key);
        Long cachedLength = m_lengthCache.getIfPresent(objectKey);
        if (cachedLength != null) {
            return cachedLength.longValue();
        }
        long length = m_s3Client.getObjectLength(objectKey);
        m_lengthCache.put(objectKey, Long.valueOf(length));
        return length;
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#supportsRangeDelivery()
     */
    public boolean supportsRangeDelivery() {

        return true;
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#write(java.lang.String, byte[])
     */
    public void write(String key, byte[] content) throws Exception {

        ensureInitialized();
        String objectKey = getObjectKey(key);
        m_s3Client.putObject(objectKey, content);
        m_lengthCache.put(objectKey, Long.valueOf(content.length));
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#writeRangeTo(java.lang.String, long, long, java.io.OutputStream)
     */
    public void writeRangeTo(String key, long start, long length, OutputStream out) throws Exception {

        ensureInitialized();
        String objectKey = getObjectKey(key);
        try {
            m_s3Client.writeObjectRangeTo(objectKey, start, length, out);
        } catch (Exception e) {
            m_lengthCache.invalidate(objectKey);
            throw e;
        }
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#writeTo(java.lang.String, java.io.OutputStream)
     */
    public void writeTo(String key, OutputStream out) throws Exception {

        ensureInitialized();
        String objectKey = getObjectKey(key);
        try {
            m_s3Client.writeObjectTo(objectKey, out);
        } catch (Exception e) {
            m_lengthCache.invalidate(objectKey);
            throw e;
        }
    }

    /**
     * Initializes the S3 client.<p>
     *
     * @param s3Client the S3 client
     * @param prefix the object key prefix
     * @throws Exception if the configured bucket can not be accessed
     */
    protected void initClient(I_CmsS3Client s3Client, String prefix) throws Exception {

        String normalizedPrefix = normalizePrefix(prefix);
        String healthCheckKey = normalizedPrefix + ".opencms-healthcheck/" + UUID.randomUUID().toString();
        byte[] healthCheckContent = "OpenCms image cache health check".getBytes(StandardCharsets.UTF_8);
        boolean healthCheckStored = false;
        Exception failure = null;
        try {
            s3Client.validateBucketAccess();
            s3Client.putObject(healthCheckKey, healthCheckContent);
            healthCheckStored = true;
            if (!Arrays.equals(healthCheckContent, s3Client.getObject(healthCheckKey))) {
                throw new IllegalStateException("S3 image cache health check returned different content.");
            }
        } catch (Exception e) {
            failure = e;
            throw e;
        } finally {
            if (healthCheckStored) {
                try {
                    s3Client.deleteObject(healthCheckKey);
                } catch (Exception e) {
                    if (failure != null) {
                        failure.addSuppressed(e);
                    } else {
                        failure = e;
                    }
                }
            }
            if (failure != null) {
                try {
                    s3Client.close();
                } catch (Exception closeException) {
                    failure.addSuppressed(closeException);
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
        if ((m_s3Client != null) && (m_s3Client != s3Client)) {
            m_s3Client.close();
        }
        m_prefix = normalizedPrefix;
        m_s3Client = s3Client;
        m_lengthCache.invalidateAll();
    }

    /**
     * Ensures the cache has been initialized.<p>
     */
    private void ensureInitialized() {

        if (m_s3Client == null) {
            throw new IllegalStateException("Image cache has not been initialized.");
        }
    }

    /**
     * Returns the S3 object key.<p>
     *
     * @param key the image cache key
     * @return the object key
     */
    private String getObjectKey(String key) {

        String normalizedKey = key;
        while (normalizedKey.startsWith("/")) {
            normalizedKey = normalizedKey.substring(1);
        }
        return m_prefix + normalizedKey;
    }

    /**
     * Normalizes an S3 object key prefix.<p>
     *
     * @param prefix the raw prefix
     * @return the normalized prefix
     */
    private String normalizePrefix(String prefix) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(prefix)) {
            return "";
        }
        String result = prefix.trim();
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(result)) {
            return "";
        }
        return result + "/";
    }
}

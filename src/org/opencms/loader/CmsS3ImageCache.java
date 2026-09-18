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

import org.opencms.db.storage.CmsStorageBlobNotFoundException;
import org.opencms.db.storage.s3.CmsGenericS3Client;
import org.opencms.db.storage.s3.CmsS3ClientConfiguration;
import org.opencms.db.storage.s3.CmsS3DeleteResult;
import org.opencms.db.storage.s3.CmsS3ObjectMetadata;
import org.opencms.db.storage.s3.I_CmsS3Client;
import org.opencms.loader.imagecache.CmsImageCacheEntry;
import org.opencms.loader.imagecache.I_CmsImageCacheAccessMetadataProvider;
import org.opencms.util.CmsStringUtil;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * S3 based storage for generated image cache entries.<p>
 */
public class CmsS3ImageCache implements I_CmsImageCache, I_CmsImageCacheAccessMetadataProvider {

    /** Default and maximum S3 delete batch size. */
    private static final int DEFAULT_DELETE_BATCH_SIZE = CmsGenericS3Client.MAX_DELETE_OBJECTS;

    /** Maximum number of S3 object lengths kept locally. */
    private static final int LENGTH_CACHE_MAX_SIZE = 10000;

    /** Number of minutes after which a cached S3 object length expires. */
    private static final int LENGTH_CACHE_EXPIRY_MINUTES = 10;

    /** Locally cached S3 object lengths. */
    private Cache<String, Long> m_lengthCache = CacheBuilder.newBuilder().maximumSize(
        LENGTH_CACHE_MAX_SIZE).expireAfterAccess(LENGTH_CACHE_EXPIRY_MINUTES, TimeUnit.MINUTES).build();

    /** Metadata obtained without an extra request while delivering S3 objects. */
    private Cache<String, CmsImageCacheEntry> m_recentAccessMetadata = CacheBuilder.newBuilder().maximumSize(
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
     * @see org.opencms.loader.I_CmsImageCache#clear()
     */
    @Override
    public void clear() throws Exception {

        clear(DEFAULT_DELETE_BATCH_SIZE);
    }

    /**
     * Clears the image cache using S3 multi-object delete requests.<p>
     *
     * @param deleteBatchSize the number of keys per request
     * @throws Exception if listing or deleting fails
     */
    public void clear(int deleteBatchSize) throws Exception {

        ensureInitialized();
        if ((deleteBatchSize < 1) || (deleteBatchSize > CmsGenericS3Client.MAX_DELETE_OBJECTS)) {
            throw new IllegalArgumentException(
                "S3 delete batch size must be between 1 and " + CmsGenericS3Client.MAX_DELETE_OBJECTS + ".");
        }
        List<String> batch = new ArrayList<String>(deleteBatchSize);
        List<Exception> failures = new ArrayList<Exception>();
        try {
            m_s3Client.visitObjects(m_prefix, metadata -> {
                if (isImageCacheObjectKey(metadata.getKey())) {
                    batch.add(metadata.getKey());
                    if (batch.size() == deleteBatchSize) {
                        failures.addAll(deleteObjectKeys(batch).getFailures().values());
                        batch.clear();
                    }
                }
            });
            if (!batch.isEmpty()) {
                failures.addAll(deleteObjectKeys(batch).getFailures().values());
            }
            if (!failures.isEmpty()) {
                Exception failure = new Exception("Unable to delete " + failures.size() + " S3 image cache entries.");
                for (Exception suppressed : failures) {
                    failure.addSuppressed(suppressed);
                }
                throw failure;
            }
        } finally {
            m_lengthCache.invalidateAll();
            m_recentAccessMetadata.invalidateAll();
        }
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#close()
     */
    @Override
    public void close() throws Exception {

        I_CmsS3Client client = m_s3Client;
        m_s3Client = null;
        m_lengthCache.invalidateAll();
        m_recentAccessMetadata.invalidateAll();
        if (client != null) {
            client.close();
        }
    }

    /**
     * Deletes an image cache entry.<p>
     *
     * @param key the image cache key
     * @throws Exception if deleting fails
     */
    public void delete(String key) throws Exception {

        ensureInitialized();
        String objectKey = getObjectKey(key);
        try {
            m_s3Client.deleteObject(objectKey);
        } finally {
            m_lengthCache.invalidate(objectKey);
            m_recentAccessMetadata.invalidate(normalizeKey(key));
        }
    }

    /**
     * Deletes image cache entries with one S3 multi-object request.<p>
     *
     * @param keys the image cache keys
     * @return the per-entry result using normalized image cache keys
     * @throws Exception if the complete request fails
     */
    public CmsS3DeleteResult deleteBatch(List<String> keys) throws Exception {

        ensureInitialized();
        List<String> objectKeys = new ArrayList<String>(keys.size());
        Map<String, String> objectToCacheKey = new HashMap<String, String>();
        List<String> normalizedKeys = new ArrayList<String>(keys.size());
        Set<String> uniqueKeys = new HashSet<String>();
        for (String key : keys) {
            String normalizedKey = normalizeKey(key);
            if (!uniqueKeys.add(normalizedKey)) {
                throw new IllegalArgumentException("Duplicate normalized image cache key: " + normalizedKey);
            }
            String objectKey = m_prefix + normalizedKey;
            normalizedKeys.add(normalizedKey);
            objectKeys.add(objectKey);
            objectToCacheKey.put(objectKey, normalizedKey);
        }
        try {
            CmsS3DeleteResult objectResult = m_s3Client.deleteObjects(objectKeys);
            Map<String, Exception> failures = new LinkedHashMap<String, Exception>();
            for (Map.Entry<String, Exception> failure : objectResult.getFailures().entrySet()) {
                String cacheKey = objectToCacheKey.get(failure.getKey());
                failures.put(cacheKey == null ? failure.getKey() : cacheKey, failure.getValue());
            }
            return new CmsS3DeleteResult(normalizedKeys, failures);
        } finally {
            for (String objectKey : objectKeys) {
                m_lengthCache.invalidate(objectKey);
            }
            for (String normalizedKey : normalizedKeys) {
                m_recentAccessMetadata.invalidate(normalizedKey);
            }
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
     * @see org.opencms.loader.I_CmsImageCache#existsAuthoritatively(java.lang.String)
     */
    @Override
    public boolean existsAuthoritatively(String key) throws Exception {

        ensureInitialized();
        String objectKey = getObjectKey(key);
        long length = m_s3Client.getObjectLengthIfExists(objectKey);
        if (length >= 0) {
            m_lengthCache.put(objectKey, Long.valueOf(length));
            return true;
        }
        m_lengthCache.invalidate(objectKey);
        m_recentAccessMetadata.invalidate(normalizeKey(key));
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
        try {
            long length = m_s3Client.getObjectLength(objectKey);
            m_lengthCache.put(objectKey, Long.valueOf(length));
            return length;
        } catch (CmsStorageBlobNotFoundException e) {
            m_lengthCache.invalidate(objectKey);
            m_recentAccessMetadata.invalidate(normalizeKey(key));
            throw new CmsImageCacheEntryNotFoundException(key, e);
        }
    }

    /**
     * Loads metadata for a single image cache entry.<p>
     *
     * @param key the image cache key
     * @return the object metadata using the normalized image cache key
     * @throws Exception if the metadata can not be loaded
     */
    public CmsS3ObjectMetadata getMetadata(String key) throws Exception {

        ensureInitialized();
        String objectKey = getObjectKey(key);
        CmsS3ObjectMetadata metadata = m_s3Client.getObjectMetadata(objectKey);
        m_lengthCache.put(objectKey, Long.valueOf(metadata.getLength()));
        CmsS3ObjectMetadata result = new CmsS3ObjectMetadata(
            normalizeKey(key),
            metadata.getLength(),
            metadata.getLastModified(),
            metadata.getRevision());
        rememberAccessMetadata(result);
        return result;
    }

    /**
     * @see org.opencms.loader.imagecache.I_CmsImageCacheAccessMetadataProvider#getRecentAccessMetadata(java.lang.String)
     */
    @Override
    public CmsImageCacheEntry getRecentAccessMetadata(String key) {

        return m_recentAccessMetadata.getIfPresent(normalizeKey(key));
    }

    /**
     * Renews an image cache entry by conditionally copying the S3 object onto itself.<p>
     *
     * @param key the image cache key
     * @param expectedRevision the expected object revision
     * @param renewalTime the requested renewal time
     * @return {@code true} if the entry was renewed, or {@code false} if it no longer matched
     * @throws Exception if renewal fails
     */
    public boolean renew(String key, String expectedRevision, Instant renewalTime) throws Exception {

        ensureInitialized();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(expectedRevision)) {
            throw new IllegalArgumentException("S3 image cache renewal requires an expected object revision.");
        }
        if (renewalTime == null) {
            throw new IllegalArgumentException("S3 image cache renewal requires a renewal time.");
        }
        String objectKey = getObjectKey(key);
        boolean renewed = m_s3Client.renewObject(objectKey, expectedRevision, renewalTime);
        m_recentAccessMetadata.invalidate(normalizeKey(key));
        if (!renewed) {
            m_lengthCache.invalidate(objectKey);
        }
        return renewed;
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#supportsRangeDelivery()
     */
    public boolean supportsRangeDelivery() {

        return true;
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#visitEntries(org.opencms.loader.I_CmsImageCache.I_CmsImageCacheEntryVisitor)
     */
    @Override
    public void visitEntries(I_CmsImageCacheEntryVisitor visitor) throws Exception {

        visitEntries("", visitor);
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#visitEntries(java.lang.String, org.opencms.loader.I_CmsImageCache.I_CmsImageCacheEntryVisitor)
     */
    @Override
    public void visitEntries(String prefix, I_CmsImageCacheEntryVisitor visitor) throws Exception {

        visitEntriesWithMetadata(prefix, metadata -> visitor.visit(metadata.getKey(), metadata.getLength()));
    }

    /**
     * Visits image cache entries with metadata returned directly by the S3 listing.<p>
     *
     * @param visitor the metadata visitor
     * @throws Exception if listing fails
     */
    public void visitEntriesWithMetadata(I_CmsS3Client.I_CmsS3ObjectMetadataVisitor visitor) throws Exception {

        visitEntriesWithMetadata("", visitor);
    }

    /**
     * Visits image cache entries with the given cache key prefix and metadata returned directly by the S3 listing.<p>
     *
     * @param prefix the image cache key prefix, or an empty string for all entries
     * @param visitor the metadata visitor
     * @throws Exception if listing fails
     */
    public void visitEntriesWithMetadata(String prefix, I_CmsS3Client.I_CmsS3ObjectMetadataVisitor visitor)
    throws Exception {

        ensureInitialized();
        String objectPrefix = m_prefix + normalizeKey(prefix == null ? "" : prefix);
        m_s3Client.visitObjects(objectPrefix, metadata -> {
            String objectKey = metadata.getKey();
            if (isImageCacheObjectKey(objectKey)) {
                String key = objectKey.substring(m_prefix.length());
                m_lengthCache.put(objectKey, Long.valueOf(metadata.getLength()));
                visitor.visit(
                    new CmsS3ObjectMetadata(
                        key,
                        metadata.getLength(),
                        metadata.getLastModified(),
                        metadata.getRevision()));
            }
        });
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#write(java.lang.String, byte[])
     */
    public void write(String key, byte[] content) throws Exception {

        ensureInitialized();
        String objectKey = getObjectKey(key);
        m_s3Client.putObject(objectKey, content);
        m_lengthCache.put(objectKey, Long.valueOf(content.length));
        m_recentAccessMetadata.invalidate(normalizeKey(key));
    }

    /**
     * @see org.opencms.loader.I_CmsImageCache#writeRangeTo(java.lang.String, long, long, java.io.OutputStream)
     */
    public void writeRangeTo(String key, long start, long length, OutputStream out) throws Exception {

        ensureInitialized();
        String objectKey = getObjectKey(key);
        try {
            CmsS3ObjectMetadata metadata = m_s3Client.writeObjectRangeToWithMetadata(objectKey, start, length, out);
            Long completeLength = m_lengthCache.getIfPresent(objectKey);
            rememberAccessMetadata(
                new CmsS3ObjectMetadata(
                    normalizeKey(key),
                    completeLength == null ? metadata.getLength() : completeLength.longValue(),
                    metadata.getLastModified(),
                    metadata.getRevision()));
        } catch (CmsStorageBlobNotFoundException e) {
            m_lengthCache.invalidate(objectKey);
            m_recentAccessMetadata.invalidate(normalizeKey(key));
            throw new CmsImageCacheEntryNotFoundException(key, e);
        } catch (Exception e) {
            m_lengthCache.invalidate(objectKey);
            m_recentAccessMetadata.invalidate(normalizeKey(key));
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
            CmsS3ObjectMetadata metadata = m_s3Client.writeObjectToWithMetadata(objectKey, out);
            rememberAccessMetadata(
                new CmsS3ObjectMetadata(
                    normalizeKey(key),
                    metadata.getLength(),
                    metadata.getLastModified(),
                    metadata.getRevision()));
        } catch (CmsStorageBlobNotFoundException e) {
            m_lengthCache.invalidate(objectKey);
            m_recentAccessMetadata.invalidate(normalizeKey(key));
            throw new CmsImageCacheEntryNotFoundException(key, e);
        } catch (Exception e) {
            m_lengthCache.invalidate(objectKey);
            m_recentAccessMetadata.invalidate(normalizeKey(key));
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

    /** Deletes a batch of complete S3 object keys. */
    private CmsS3DeleteResult deleteObjectKeys(List<String> objectKeys) throws Exception {

        List<String> keys = new ArrayList<String>(objectKeys);
        try {
            return m_s3Client.deleteObjects(keys);
        } finally {
            for (String key : keys) {
                m_lengthCache.invalidate(key);
            }
        }
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

        return m_prefix + normalizeKey(key);
    }

    /**
     * Checks whether an object key belongs to this image cache.<p>
     *
     * @param objectKey the object key
     * @return <code>true</code> if the object key belongs to this image cache
     */
    private boolean isImageCacheObjectKey(String objectKey) {

        return objectKey.startsWith(m_prefix) && (objectKey.length() > m_prefix.length());
    }

    /** Normalizes an image cache key. */
    private String normalizeKey(String key) {

        String normalizedKey = key;
        while (normalizedKey.startsWith("/")) {
            normalizedKey = normalizedKey.substring(1);
        }
        return normalizedKey;
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

    /** Stores S3 response metadata for access-triggered renewal. */
    private void rememberAccessMetadata(CmsS3ObjectMetadata metadata) {

        if ((metadata.getLastModified() != null) && CmsStringUtil.isNotEmptyOrWhitespaceOnly(metadata.getRevision())) {
            m_recentAccessMetadata.put(
                normalizeKey(metadata.getKey()),
                new CmsImageCacheEntry(
                    normalizeKey(metadata.getKey()),
                    metadata.getLength(),
                    metadata.getLastModified(),
                    metadata.getRevision()));
        }
    }
}

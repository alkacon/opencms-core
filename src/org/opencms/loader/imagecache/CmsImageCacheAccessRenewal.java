/*
 * This library is part of OpenCms -
 * The Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package org.opencms.loader.imagecache;

import org.opencms.configuration.CmsImageCacheConfiguration;
import org.opencms.configuration.CmsImageCacheConfiguration.RetentionMode;
import org.opencms.loader.CmsFsImageCache;
import org.opencms.loader.CmsS3ImageCache;
import org.opencms.loader.I_CmsImageCache;
import org.opencms.main.CmsLog;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Evaluates successful image cache accesses and renews eligible entries asynchronously.<p>
 */
public final class CmsImageCacheAccessRenewal implements AutoCloseable {

    /** Maximum number of locally remembered entry timestamps. */
    private static final int METADATA_CACHE_MAX_SIZE = 10000;

    /** Number of minutes after which unused local metadata expires. */
    private static final int METADATA_CACHE_EXPIRY_MINUTES = 10;

    /** The logger. */
    private static final Log LOG = CmsLog.getLog(CmsImageCacheAccessRenewal.class);

    /** The configured image cache. */
    private final I_CmsImageCache m_cache;

    /** The bounded background executor. */
    private final ThreadPoolExecutor m_executor;

    /** Locally remembered entry metadata. */
    private final Cache<String, CmsImageCacheEntry> m_metadata = CacheBuilder.newBuilder().maximumSize(
        METADATA_CACHE_MAX_SIZE).expireAfterAccess(METADATA_CACHE_EXPIRY_MINUTES, TimeUnit.MINUTES).build();

    /** Keys with queued or running renewal evaluation. */
    private final Set<String> m_pending = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    /** Number of rejected access events. */
    private final AtomicLong m_rejected = new AtomicLong();

    /** The configured renewal jitter in milliseconds. */
    private final long m_renewalJitterMillis;

    /** The age at which the renewal window starts, before jitter. */
    private final long m_renewalWindowStartMillis;

    /** The maintenance service. */
    private final CmsImageCacheMaintenanceService m_service;

    /**
     * Creates an access renewal coordinator.<p>
     *
     * @param cache the image cache
     * @param configuration the image cache configuration
     * @param concurrency the background worker count
     * @param queueCapacity the maximum number of queued keys
     */
    private CmsImageCacheAccessRenewal(
        I_CmsImageCache cache,
        CmsImageCacheConfiguration configuration,
        int concurrency,
        int queueCapacity) {

        m_cache = cache;
        m_service = CmsImageCacheMaintenanceService.create(cache, configuration);
        m_renewalWindowStartMillis = configuration.getMaxAge().minus(configuration.getRenewalWindow()).toMillis();
        m_renewalJitterMillis = configuration.getRenewalJitter().toMillis();
        AtomicInteger threadCounter = new AtomicInteger();
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(
                runnable,
                "OpenCms image cache access renewal " + threadCounter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
        m_executor = new ThreadPoolExecutor(
            concurrency,
            concurrency,
            0L,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(queueCapacity),
            threadFactory,
            new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * Creates a coordinator when access-triggered renewal is enabled for the cache.<p>
     *
     * @param cache the configured image cache
     * @param configuration the image cache configuration
     * @return the coordinator, or {@code null} when access renewal is disabled or unsupported
     */
    public static CmsImageCacheAccessRenewal create(I_CmsImageCache cache, CmsImageCacheConfiguration configuration) {

        if ((cache == null)
            || (configuration == null)
            || !configuration.isConfigured()
            || (configuration.getRetentionMode() != RetentionMode.renewOnUse)) {
            return null;
        }
        if (cache instanceof CmsS3ImageCache) {
            return new CmsImageCacheAccessRenewal(
                cache,
                configuration,
                configuration.getS3CopyConcurrency(),
                configuration.getS3RenewalQueueCapacity());
        }
        if (cache instanceof CmsFsImageCache) {
            return new CmsImageCacheAccessRenewal(
                cache,
                configuration,
                configuration.getFsTouchConcurrency(),
                CmsImageCacheConfiguration.DEFAULT_S3_RENEWAL_QUEUE_CAPACITY);
        }
        return null;
    }

    /** Shuts down the background executor. */
    @Override
    public void close() {

        m_executor.shutdownNow();
        m_pending.clear();
        m_metadata.invalidateAll();
    }

    /**
     * Records a successful content delivery or HTTP 304 response.<p>
     *
     * The method never accesses the backend and never blocks on renewal work.<p>
     *
     * @param key the image cache key
     */
    public void recordAccess(String key) {

        String normalizedKey = normalizeKey(key);
        CmsImageCacheEntry observed = null;
        if (m_cache instanceof I_CmsImageCacheAccessMetadataProvider) {
            observed = ((I_CmsImageCacheAccessMetadataProvider)m_cache).getRecentAccessMetadata(normalizedKey);
            if (observed != null) {
                m_metadata.put(normalizedKey, observed);
            }
        }
        CmsImageCacheEntry known = observed == null ? m_metadata.getIfPresent(normalizedKey) : observed;
        Instant now = Instant.now();
        if ((known != null) && !isRenewalDue(normalizedKey, known.getLastModified(), now)) {
            return;
        }
        if (!m_pending.add(normalizedKey)) {
            return;
        }
        final CmsImageCacheEntry candidate = known;
        try {
            m_executor.execute(() -> processAccess(normalizedKey, candidate));
        } catch (RejectedExecutionException e) {
            m_pending.remove(normalizedKey);
            long rejected = m_rejected.incrementAndGet();
            if ((rejected == 1) || ((rejected % 1000) == 0)) {
                LOG.warn("Image cache access renewal queue is full; rejected accesses: " + rejected);
            }
        }
    }

    /** Returns whether an entry is old enough for renewal. */
    boolean isRenewalDue(String key, Instant lastModified, Instant now) {

        if ((lastModified == null) || lastModified.isAfter(now)) {
            return false;
        }
        long jitter = 0;
        if (m_renewalJitterMillis > 0) {
            long hash = ByteBuffer.wrap(DigestUtils.sha256(key)).getLong();
            jitter = Long.remainderUnsigned(hash, m_renewalJitterMillis + 1);
        }
        Duration age = Duration.between(lastModified, now);
        return age.compareTo(Duration.ofMillis(m_renewalWindowStartMillis + jitter)) >= 0;
    }

    /** Normalizes a cache key. */
    private String normalizeKey(String key) {

        String result = key;
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        return result;
    }

    /** Evaluates and, if necessary, renews one entry. */
    private void processAccess(String key, CmsImageCacheEntry candidate) {

        try {
            CmsImageCacheEntry entry = candidate;
            if (entry == null) {
                entry = m_service.getEntry(key);
                if (entry == null) {
                    m_metadata.invalidate(key);
                    return;
                }
                m_metadata.put(key, entry);
            }
            Instant now = Instant.now();
            if (!isRenewalDue(key, entry.getLastModified(), now)) {
                return;
            }
            CmsImageCacheMaintenanceResult result = m_service.execute(
                CmsImageCacheMaintenanceRequest.renew(Collections.singletonList(entry), now));
            m_metadata.invalidate(key);
            if (result.getFailed() > 0) {
                LOG.warn("Unable to renew image cache entry after access: " + key);
                for (Exception failure : result.getFailures().values()) {
                    LOG.warn("Image cache access renewal failure for " + key, failure);
                }
            }
        } catch (Exception e) {
            m_metadata.invalidate(key);
            LOG.warn("Unable to evaluate image cache access renewal for " + key, e);
        } finally {
            m_pending.remove(key);
        }
    }
}

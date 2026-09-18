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

package org.opencms.loader.imagecache;

import org.opencms.configuration.CmsImageCacheConfiguration;
import org.opencms.db.storage.s3.CmsGenericS3Client;
import org.opencms.db.storage.s3.CmsS3DeleteResult;
import org.opencms.loader.CmsS3ImageCache;
import org.opencms.loader.imagecache.CmsImageCacheCapabilities.Capability;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Maintenance adapter for the S3 image cache.<p>
 */
public class CmsS3ImageCacheMaintenance implements I_CmsImageCacheMaintenance {

    /** Result for one delete batch. */
    private static final class BatchResult {

        /** The batch entries. */
        private final List<CmsImageCacheEntry> m_entries;

        /** A whole-request failure. */
        private final Exception m_failure;

        /** The per-object result. */
        private final CmsS3DeleteResult m_result;

        /** Creates a batch result. */
        BatchResult(List<CmsImageCacheEntry> entries, CmsS3DeleteResult result, Exception failure) {

            m_entries = entries;
            m_result = result;
            m_failure = failure;
        }
    }

    /** Rate limiter shared by concurrent S3 copy workers. */
    private static final class CopyRateLimiter {

        /** Minimum interval between copy starts. */
        private final long m_intervalNanos;

        /** The next reserved copy start. */
        private long m_nextPermitNanos;

        /** Creates a limiter. */
        CopyRateLimiter(int maxCopiesPerSecond) {

            m_intervalNanos = Math.max(1L, TimeUnit.SECONDS.toNanos(1) / maxCopiesPerSecond);
        }

        /** Waits until the next copy may start. */
        void acquire() throws InterruptedException {

            long delay;
            synchronized (this) {
                long now = System.nanoTime();
                long scheduled = Math.max(now, m_nextPermitNanos);
                delay = scheduled - now;
                m_nextPermitNanos = scheduled + m_intervalNanos;
            }
            if (delay > 0) {
                TimeUnit.NANOSECONDS.sleep(delay);
            }
        }
    }

    /** Result for one renewal. */
    private static final class RenewalResult {

        /** The renewed entry. */
        private final CmsImageCacheEntry m_entry;

        /** The renewal failure. */
        private final Exception m_failure;

        /** Whether renewal was skipped. */
        private final boolean m_skipped;

        /** Creates a renewal result. */
        RenewalResult(CmsImageCacheEntry entry, boolean skipped, Exception failure) {

            m_entry = entry;
            m_skipped = skipped;
            m_failure = failure;
        }
    }

    /** The supported capabilities. */
    private static final CmsImageCacheCapabilities CAPABILITIES = CmsImageCacheCapabilities.of(
        Capability.LIST_ENTRIES,
        Capability.ENTRY_TIMESTAMPS,
        Capability.DELETE_ENTRIES,
        Capability.CLEAR,
        Capability.RENEW_ENTRIES);

    /** The S3 cache. */
    private final CmsS3ImageCache m_cache;

    /** The number of concurrent copy requests. */
    private final int m_copyConcurrency;

    /** The number of concurrent delete requests. */
    private final int m_deleteConcurrency;

    /** The maximum number of keys per delete request. */
    private final int m_deleteBatchSize;

    /** Rate limiter shared by all renewal requests handled by this adapter. */
    private final CopyRateLimiter m_copyRateLimiter;

    /** The maximum number of waiting renewal tasks. */
    private final int m_renewalQueueCapacity;

    /**
     * Creates a maintenance adapter.<p>
     *
     * @param cache the S3 cache
     */
    public CmsS3ImageCacheMaintenance(CmsS3ImageCache cache) {

        this(
            cache,
            CmsImageCacheConfiguration.DEFAULT_S3_DELETE_BATCH_SIZE,
            CmsImageCacheConfiguration.DEFAULT_S3_DELETE_CONCURRENCY,
            CmsImageCacheConfiguration.DEFAULT_S3_COPY_CONCURRENCY,
            CmsImageCacheConfiguration.DEFAULT_S3_MAX_COPIES_PER_SECOND,
            CmsImageCacheConfiguration.DEFAULT_S3_RENEWAL_QUEUE_CAPACITY);
    }

    /**
     * Creates a maintenance adapter.<p>
     *
     * @param cache the S3 cache
     * @param deleteBatchSize the maximum number of keys per delete request
     * @param deleteConcurrency the number of concurrent delete requests
     */
    public CmsS3ImageCacheMaintenance(CmsS3ImageCache cache, int deleteBatchSize, int deleteConcurrency) {

        this(
            cache,
            deleteBatchSize,
            deleteConcurrency,
            CmsImageCacheConfiguration.DEFAULT_S3_COPY_CONCURRENCY,
            CmsImageCacheConfiguration.DEFAULT_S3_MAX_COPIES_PER_SECOND,
            CmsImageCacheConfiguration.DEFAULT_S3_RENEWAL_QUEUE_CAPACITY);
    }

    /**
     * Creates a maintenance adapter.<p>
     *
     * @param cache the S3 cache
     * @param deleteBatchSize the maximum number of keys per delete request
     * @param deleteConcurrency the number of concurrent delete requests
     * @param copyConcurrency the number of concurrent copy requests
     * @param maxCopiesPerSecond the maximum number of copy requests started per second
     * @param renewalQueueCapacity the maximum number of waiting renewal tasks
     */
    public CmsS3ImageCacheMaintenance(
        CmsS3ImageCache cache,
        int deleteBatchSize,
        int deleteConcurrency,
        int copyConcurrency,
        int maxCopiesPerSecond,
        int renewalQueueCapacity) {

        if ((deleteBatchSize < 1) || (deleteBatchSize > CmsGenericS3Client.MAX_DELETE_OBJECTS)) {
            throw new IllegalArgumentException(
                "S3 delete batch size must be between 1 and " + CmsGenericS3Client.MAX_DELETE_OBJECTS + ".");
        }
        if (deleteConcurrency < 1) {
            throw new IllegalArgumentException("S3 delete concurrency must be positive.");
        }
        if (copyConcurrency < 1) {
            throw new IllegalArgumentException("S3 copy concurrency must be positive.");
        }
        if (maxCopiesPerSecond < 1) {
            throw new IllegalArgumentException("S3 maximum copies per second must be positive.");
        }
        if (renewalQueueCapacity < 1) {
            throw new IllegalArgumentException("S3 renewal queue capacity must be positive.");
        }
        if (((long)copyConcurrency + renewalQueueCapacity) > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("S3 copy concurrency and renewal queue capacity are too large.");
        }
        m_cache = cache;
        m_deleteBatchSize = deleteBatchSize;
        m_deleteConcurrency = deleteConcurrency;
        m_copyConcurrency = copyConcurrency;
        m_copyRateLimiter = new CopyRateLimiter(maxCopiesPerSecond);
        m_renewalQueueCapacity = renewalQueueCapacity;
    }

    /**
     * @see org.opencms.loader.imagecache.I_CmsImageCacheMaintenance#execute(org.opencms.loader.imagecache.CmsImageCacheMaintenanceRequest)
     */
    @Override
    public CmsImageCacheMaintenanceResult execute(CmsImageCacheMaintenanceRequest request) throws Exception {

        long start = System.nanoTime();
        int requested = request.getOperation() == CmsImageCacheMaintenanceRequest.Operation.CLEAR
        ? 1
        : request.getEntries().size();
        CmsImageCacheMaintenanceResult.Builder result = new CmsImageCacheMaintenanceResult.Builder(
            request.getOperation(),
            requested);
        switch (request.getOperation()) {
            case CLEAR:
                try {
                    m_cache.clear(m_deleteBatchSize);
                    result.addSuccess();
                } catch (Exception e) {
                    result.addFailure(getBackendId(), e);
                }
                break;
            case DELETE:
                deleteEntries(request.getEntries(), result);
                break;
            case RENEW:
                renewEntries(request.getEntries(), request.getRenewalTime(), result);
                break;
            default:
                throw new IllegalArgumentException(
                    "Unsupported S3 image cache maintenance request: " + request.getOperation());
        }
        return result.build(Duration.ofNanos(System.nanoTime() - start));
    }

    /**
     * @see org.opencms.loader.imagecache.I_CmsImageCacheMaintenance#getBackendId()
     */
    @Override
    public String getBackendId() {

        return "s3";
    }

    /**
     * @see org.opencms.loader.imagecache.I_CmsImageCacheMaintenance#getCapabilities()
     */
    @Override
    public CmsImageCacheCapabilities getCapabilities() {

        return CAPABILITIES;
    }

    /**
     * @see org.opencms.loader.imagecache.I_CmsImageCacheMaintenance#getEntry(java.lang.String)
     */
    @Override
    public CmsImageCacheEntry getEntry(String key) throws Exception {

        org.opencms.db.storage.s3.CmsS3ObjectMetadata metadata;
        try {
            metadata = m_cache.getMetadata(key);
        } catch (org.opencms.db.storage.CmsStorageBlobNotFoundException e) {
            return null;
        }
        return new CmsImageCacheEntry(
            metadata.getKey(),
            metadata.getLength(),
            metadata.getLastModified(),
            metadata.getRevision());
    }

    /**
     * @see org.opencms.loader.imagecache.I_CmsImageCacheMaintenance#visitEntries(org.opencms.loader.imagecache.I_CmsImageCacheMaintenanceEntryVisitor)
     */
    @Override
    public void visitEntries(I_CmsImageCacheMaintenanceEntryVisitor visitor) throws Exception {

        m_cache.visitEntriesWithMetadata(
            metadata -> visitor.visit(
                new CmsImageCacheEntry(
                    metadata.getKey(),
                    metadata.getLength(),
                    metadata.getLastModified(),
                    metadata.getRevision())));
    }

    /** Adds a batch outcome to the maintenance metrics. */
    private void addBatchResult(BatchResult batch, CmsImageCacheMaintenanceResult.Builder result) {

        for (CmsImageCacheEntry entry : batch.m_entries) {
            if (batch.m_failure != null) {
                result.addFailure(entry.getKey(), batch.m_failure);
                continue;
            }
            Exception failure = batch.m_result.getFailures().get(normalizeKey(entry.getKey()));
            if (failure == null) {
                result.addSuccess();
            } else {
                result.addFailure(entry.getKey(), failure);
            }
        }
    }

    /** Adds one renewal outcome to the maintenance metrics. */
    private void addRenewalResult(RenewalResult renewal, CmsImageCacheMaintenanceResult.Builder result) {

        if (renewal.m_failure != null) {
            result.addFailure(renewal.m_entry.getKey(), renewal.m_failure);
        } else if (renewal.m_skipped) {
            result.addSkipped();
        } else {
            result.addSuccess();
        }
    }

    /** Executes one S3 delete batch and retains whole-request failures as per-entry failures. */
    private BatchResult deleteBatch(List<CmsImageCacheEntry> entries) {

        List<String> keys = new ArrayList<String>(entries.size());
        for (CmsImageCacheEntry entry : entries) {
            keys.add(entry.getKey());
        }
        try {
            return new BatchResult(entries, m_cache.deleteBatch(keys), null);
        } catch (Exception e) {
            return new BatchResult(entries, null, e);
        }
    }

    /** Deletes selected entries using concurrent S3 multi-object requests. */
    private void deleteEntries(List<CmsImageCacheEntry> entries, CmsImageCacheMaintenanceResult.Builder result)
    throws Exception {

        List<List<CmsImageCacheEntry>> batches = new ArrayList<List<CmsImageCacheEntry>>();
        for (int start = 0; start < entries.size(); start += m_deleteBatchSize) {
            int end = Math.min(entries.size(), start + m_deleteBatchSize);
            batches.add(new ArrayList<CmsImageCacheEntry>(entries.subList(start, end)));
        }
        if ((m_deleteConcurrency == 1) || (batches.size() < 2)) {
            for (List<CmsImageCacheEntry> batch : batches) {
                addBatchResult(deleteBatch(batch), result);
            }
            return;
        }
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(m_deleteConcurrency, batches.size()));
        try {
            List<Future<BatchResult>> futures = new ArrayList<Future<BatchResult>>(batches.size());
            for (List<CmsImageCacheEntry> batch : batches) {
                futures.add(executor.submit(() -> deleteBatch(batch)));
            }
            for (Future<BatchResult> future : futures) {
                try {
                    addBatchResult(future.get(), result);
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof Exception) {
                        throw (Exception)cause;
                    }
                    throw e;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } finally {
            executor.shutdownNow();
        }
    }

    /** Normalizes a cache key for matching a batch result. */
    private String normalizeKey(String key) {

        String result = key;
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        return result;
    }

    /** Renews selected entries with bounded concurrency, queueing and copy rate. */
    private void renewEntries(
        List<CmsImageCacheEntry> entries,
        Instant renewalTime,
        CmsImageCacheMaintenanceResult.Builder result)
    throws Exception {

        if (entries.isEmpty()) {
            return;
        }
        if ((m_copyConcurrency == 1) || (entries.size() < 2)) {
            for (CmsImageCacheEntry entry : entries) {
                addRenewalResult(renewEntry(entry, renewalTime, m_copyRateLimiter), result);
            }
            return;
        }
        int maximumOutstanding = m_copyConcurrency + m_renewalQueueCapacity;
        Semaphore outstanding = new Semaphore(maximumOutstanding);
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(m_copyConcurrency, entries.size()));
        try {
            List<Future<RenewalResult>> futures = new ArrayList<Future<RenewalResult>>(entries.size());
            for (CmsImageCacheEntry entry : entries) {
                outstanding.acquire();
                try {
                    futures.add(executor.submit(() -> {
                        try {
                            return renewEntry(entry, renewalTime, m_copyRateLimiter);
                        } finally {
                            outstanding.release();
                        }
                    }));
                } catch (RuntimeException e) {
                    outstanding.release();
                    throw e;
                }
            }
            for (Future<RenewalResult> future : futures) {
                try {
                    addRenewalResult(future.get(), result);
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof Exception) {
                        throw (Exception)cause;
                    }
                    throw e;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } finally {
            executor.shutdownNow();
        }
    }

    /** Renews one entry with a conditional S3 self-copy. */
    private RenewalResult renewEntry(CmsImageCacheEntry entry, Instant renewalTime, CopyRateLimiter rateLimiter)
    throws InterruptedException {

        if ((entry.getRevision() == null) || entry.getRevision().trim().isEmpty()) {
            return new RenewalResult(entry, true, null);
        }
        rateLimiter.acquire();
        try {
            boolean renewed = m_cache.renew(entry.getKey(), entry.getRevision(), renewalTime);
            return new RenewalResult(entry, !renewed, null);
        } catch (Exception e) {
            return new RenewalResult(entry, false, e);
        }
    }
}

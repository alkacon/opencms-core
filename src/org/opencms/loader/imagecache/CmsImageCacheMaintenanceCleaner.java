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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Streams timestamp-based image cache cleanup through bounded delete batches.<p>
 */
public final class CmsImageCacheMaintenanceCleaner {

    /** Receives progress after each delete batch. */
    @FunctionalInterface
    public interface I_ProgressListener {

        /**
         * Receives cleanup progress.<p>
         *
         * @param result the accumulated cleanup result
         * @param batchResult the latest delete batch result
         * @throws Exception if progress processing fails
         */
        void onProgress(Result result, CmsImageCacheMaintenanceResult batchResult) throws Exception;
    }

    /** Accumulated cleanup counters. */
    public static final class Result {

        /** Number of failed deletes. */
        private long m_failed;

        /** Whether the configured delete limit stopped this run. */
        private boolean m_deleteLimitReached;

        /** Number of entries matching the time criterion. */
        private long m_matched;

        /** Number of scanned entries. */
        private long m_scanned;

        /** Number of skipped deletes. */
        private long m_skipped;

        /** Number of successful deletes. */
        private long m_succeeded;

        /** Whether the configured runtime limit stopped this run. */
        private boolean m_runtimeLimitReached;

        /** Returns the number of failed deletes. */
        public long getFailed() {

            return m_failed;
        }

        /** Returns the number of entries matching the time criterion. */
        public long getMatched() {

            return m_matched;
        }

        /** Returns the number of scanned entries. */
        public long getScanned() {

            return m_scanned;
        }

        /** Returns the number of skipped deletes. */
        public long getSkipped() {

            return m_skipped;
        }

        /** Returns the number of successful deletes. */
        public long getSucceeded() {

            return m_succeeded;
        }

        /** Returns whether the configured delete limit stopped this run. */
        public boolean isDeleteLimitReached() {

            return m_deleteLimitReached;
        }

        /** Returns whether the configured runtime limit stopped this run. */
        public boolean isRuntimeLimitReached() {

            return m_runtimeLimitReached;
        }
    }

    /** Internal signal used to stop a streaming backend listing after a configured limit was reached. */
    private static final class CleanupStoppedException extends Exception {

        /** Serial version id. */
        private static final long serialVersionUID = 1L;
    }

    /** Utility class. */
    private CmsImageCacheMaintenanceCleaner() {

        // no instances
    }

    /**
     * Deletes entries whose last-modified timestamp is before the cutoff.<p>
     *
     * A {@code null} cutoff selects all entries. Entries without a timestamp are not selected by a time-based
     * cleanup.<p>
     *
     * @param service the maintenance service
     * @param cutoff the exclusive last-modified cutoff, or {@code null} for all entries
     * @param batchSize the maximum number of entries per delete request
     * @param listener the optional progress listener
     * @return the accumulated cleanup result
     * @throws Exception if listing, deletion or progress processing fails
     */
    public static Result delete(
        CmsImageCacheMaintenanceService service,
        Instant cutoff,
        int batchSize,
        I_ProgressListener listener)
    throws Exception {

        return delete(service, cutoff, batchSize, Integer.MAX_VALUE, null, listener);
    }

    /**
     * Deletes entries whose last-modified timestamp is before the cutoff, subject to per-run limits.<p>
     *
     * The runtime limit is soft: an already started backend delete request is allowed to finish before the run is
     * stopped.<p>
     *
     * @param service the maintenance service
     * @param cutoff the exclusive last-modified cutoff, or {@code null} for all entries
     * @param batchSize the maximum number of entries per delete request
     * @param maxDeletes the maximum number of delete attempts in this run
     * @param maxRuntime the maximum runtime, or {@code null} for no runtime limit
     * @param listener the optional progress listener
     * @return the accumulated cleanup result
     * @throws Exception if listing, deletion or progress processing fails
     */
    public static Result delete(
        CmsImageCacheMaintenanceService service,
        Instant cutoff,
        int batchSize,
        int maxDeletes,
        Duration maxRuntime,
        I_ProgressListener listener)
    throws Exception {

        if (batchSize < 1) {
            throw new IllegalArgumentException("Image cache cleanup batch size must be positive.");
        }
        if (maxDeletes < 1) {
            throw new IllegalArgumentException("Image cache cleanup delete limit must be positive.");
        }
        if ((maxRuntime != null) && (maxRuntime.isNegative() || maxRuntime.isZero())) {
            throw new IllegalArgumentException("Image cache cleanup runtime limit must be positive.");
        }
        long startNanos = System.nanoTime();
        long maxRuntimeNanos = toNanosSaturated(maxRuntime);
        Result result = new Result();
        List<CmsImageCacheEntry> batch = new ArrayList<CmsImageCacheEntry>(Math.min(batchSize, maxDeletes));
        try {
            service.visitEntries(entry -> {
                if (isRuntimeLimitReached(startNanos, maxRuntimeNanos)) {
                    deleteBatch(service, batch, result, listener);
                    result.m_runtimeLimitReached = true;
                    throw new CleanupStoppedException();
                }
                result.m_scanned += 1;
                if ((cutoff == null)
                    || ((entry.getLastModified() != null) && entry.getLastModified().isBefore(cutoff))) {
                    result.m_matched += 1;
                    batch.add(entry);
                    if ((batch.size() == batchSize) || (result.m_matched == maxDeletes)) {
                        deleteBatch(service, batch, result, listener);
                    }
                    if (result.m_matched == maxDeletes) {
                        result.m_deleteLimitReached = true;
                        throw new CleanupStoppedException();
                    }
                }
                if (isRuntimeLimitReached(startNanos, maxRuntimeNanos)) {
                    deleteBatch(service, batch, result, listener);
                    result.m_runtimeLimitReached = true;
                    throw new CleanupStoppedException();
                }
            });
        } catch (CleanupStoppedException e) {
            // Expected control flow after a configured per-run limit was reached.
        }
        if (!batch.isEmpty()) {
            deleteBatch(service, batch, result, listener);
        }
        return result;
    }

    /** Executes and reports one delete batch. */
    private static void deleteBatch(
        CmsImageCacheMaintenanceService service,
        List<CmsImageCacheEntry> batch,
        Result result,
        I_ProgressListener listener)
    throws Exception {

        if (batch.isEmpty()) {
            return;
        }
        CmsImageCacheMaintenanceResult batchResult = service.execute(
            CmsImageCacheMaintenanceRequest.delete(new ArrayList<CmsImageCacheEntry>(batch)));
        batch.clear();
        result.m_succeeded += batchResult.getSucceeded();
        result.m_skipped += batchResult.getSkipped();
        result.m_failed += batchResult.getFailed();
        if (listener != null) {
            listener.onProgress(result, batchResult);
        }
    }

    /** Returns whether the soft runtime limit has been reached. */
    private static boolean isRuntimeLimitReached(long startNanos, long maxRuntimeNanos) {

        return (maxRuntimeNanos != Long.MAX_VALUE) && ((System.nanoTime() - startNanos) >= maxRuntimeNanos);
    }

    /** Converts a duration to nanoseconds, saturating at {@link Long#MAX_VALUE}. */
    private static long toNanosSaturated(Duration duration) {

        if (duration == null) {
            return Long.MAX_VALUE;
        }
        try {
            return duration.toNanos();
        } catch (ArithmeticException e) {
            return Long.MAX_VALUE;
        }
    }
}

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
import org.opencms.loader.CmsFsImageCache;
import org.opencms.loader.imagecache.CmsImageCacheCapabilities.Capability;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Maintenance adapter for the shared file system image cache.<p>
 */
public class CmsFsImageCacheMaintenance extends CmsRfsImageCacheMaintenance {

    /** Exception wrapper used to transport visitor exceptions through the file visitor API. */
    private static final class EntryVisitorException extends IOException {

        /** Serial version id. */
        private static final long serialVersionUID = 1L;

        /** Creates an exception wrapper. */
        EntryVisitorException(Exception cause) {

            super(cause);
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

    /** Capabilities without optional FS touching. */
    private static final CmsImageCacheCapabilities CAPABILITIES = CmsImageCacheCapabilities.of(
        Capability.LIST_ENTRIES,
        Capability.ENTRY_TIMESTAMPS,
        Capability.DELETE_ENTRIES,
        Capability.CLEAR);

    /** Capabilities with optional FS touching. */
    private static final CmsImageCacheCapabilities CAPABILITIES_WITH_RENEWAL = CmsImageCacheCapabilities.of(
        Capability.LIST_ENTRIES,
        Capability.ENTRY_TIMESTAMPS,
        Capability.DELETE_ENTRIES,
        Capability.CLEAR,
        Capability.RENEW_ENTRIES);

    /** The shared file system cache. */
    private final CmsFsImageCache m_cache;

    /** Whether touching cache files is enabled. */
    private final boolean m_touchEnabled;

    /** The maximum number of concurrent touch operations. */
    private final int m_touchConcurrency;

    /**
     * Creates a maintenance adapter.<p>
     *
     * @param cache the shared file system cache
     */
    public CmsFsImageCacheMaintenance(CmsFsImageCache cache) {

        this(cache, false, CmsImageCacheConfiguration.DEFAULT_FS_TOUCH_CONCURRENCY);
    }

    /**
     * Creates a maintenance adapter.<p>
     *
     * @param cache the shared file system cache
     * @param touchEnabled whether touching cache entries is enabled
     * @param touchConcurrency the maximum number of concurrent touch operations
     */
    public CmsFsImageCacheMaintenance(CmsFsImageCache cache, boolean touchEnabled, int touchConcurrency) {

        super(cache);
        if (touchConcurrency < 1) {
            throw new IllegalArgumentException("FS touch concurrency must be positive.");
        }
        m_cache = cache;
        m_touchEnabled = touchEnabled;
        m_touchConcurrency = touchConcurrency;
    }

    /**
     * @see org.opencms.loader.imagecache.CmsRfsImageCacheMaintenance#execute(org.opencms.loader.imagecache.CmsImageCacheMaintenanceRequest)
     */
    @Override
    public CmsImageCacheMaintenanceResult execute(CmsImageCacheMaintenanceRequest request) throws Exception {

        if (request.getOperation() != CmsImageCacheMaintenanceRequest.Operation.RENEW) {
            return super.execute(request);
        }
        if (!m_touchEnabled) {
            throw new UnsupportedOperationException("Shared-FS image cache touching is disabled.");
        }
        long start = System.nanoTime();
        CmsImageCacheMaintenanceResult.Builder result = new CmsImageCacheMaintenanceResult.Builder(
            request.getOperation(),
            request.getEntries().size());
        renewEntries(request.getEntries(), request.getRenewalTime(), result);
        return result.build(Duration.ofNanos(System.nanoTime() - start));
    }

    /**
     * @see org.opencms.loader.imagecache.CmsRfsImageCacheMaintenance#getBackendId()
     */
    @Override
    public String getBackendId() {

        return "shared-fs";
    }

    /**
     * @see org.opencms.loader.imagecache.CmsRfsImageCacheMaintenance#getCapabilities()
     */
    @Override
    public CmsImageCacheCapabilities getCapabilities() {

        return m_touchEnabled ? CAPABILITIES_WITH_RENEWAL : CAPABILITIES;
    }

    /**
     * @see org.opencms.loader.imagecache.I_CmsImageCacheMaintenance#getEntry(java.lang.String)
     */
    @Override
    public CmsImageCacheEntry getEntry(String key) throws Exception {

        Path path = toPath(key);
        BasicFileAttributes attributes;
        try {
            attributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (java.nio.file.NoSuchFileException e) {
            return null;
        }
        return attributes.isRegularFile() ? toEntry(path, attributes) : null;
    }

    /**
     * @see org.opencms.loader.imagecache.CmsRfsImageCacheMaintenance#visitEntries(org.opencms.loader.imagecache.I_CmsImageCacheMaintenanceEntryVisitor)
     */
    @Override
    public void visitEntries(I_CmsImageCacheMaintenanceEntryVisitor visitor) throws Exception {

        try {
            Files.walkFileTree(getRepository(), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException {

                    if (attributes.isRegularFile()) {
                        try {
                            visitor.visit(toEntry(path, attributes));
                        } catch (Exception e) {
                            throw new EntryVisitorException(e);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (EntryVisitorException e) {
            throw (Exception)e.getCause();
        }
    }

    /**
     * @see org.opencms.loader.imagecache.CmsRfsImageCacheMaintenance#afterDelete(java.lang.String)
     */
    @Override
    protected void afterDelete(String key) throws Exception {

        m_cache.invalidateLocalMetadata(key);
    }

    /**
     * Shared-FS deletes deliberately leave empty directories in place.<p>
     *
     * Traversing the complete shared repository after every delete request would turn deletion of one image into a
     * full NFS metadata scan. The directories are harmless and are removed by a complete cache clear.<p>
     */
    @Override
    protected void removeEmptyDirectories() {

        // no-op
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

    /** Creates the opaque revision used to detect entries changed since the scan. */
    private String createRevision(BasicFileAttributes attributes) {

        Object fileKey = attributes.fileKey();
        return attributes.lastModifiedTime().toMillis()
            + ":"
            + attributes.size()
            + ":"
            + (fileKey == null ? "" : fileKey.toString());
    }

    /** Renews selected entries with bounded parallelism. */
    private void renewEntries(
        List<CmsImageCacheEntry> entries,
        Instant renewalTime,
        CmsImageCacheMaintenanceResult.Builder result)
    throws Exception {

        if ((m_touchConcurrency == 1) || (entries.size() < 2)) {
            for (CmsImageCacheEntry entry : entries) {
                addRenewalResult(renewEntry(entry, renewalTime), result);
            }
            return;
        }
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(m_touchConcurrency, entries.size()));
        try {
            List<Future<RenewalResult>> futures = new ArrayList<Future<RenewalResult>>(entries.size());
            for (CmsImageCacheEntry entry : entries) {
                futures.add(executor.submit(() -> renewEntry(entry, renewalTime)));
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

    /**
     * Renews one entry if its revision still matches the preceding scan.<p>
     *
     * File systems do not provide an atomic compare-and-touch operation. The check therefore prevents stale planned
     * work in the normal case, but can not eliminate a replacement in the narrow interval before setting the file
     * timestamp.<p>
     */
    private RenewalResult renewEntry(CmsImageCacheEntry entry, Instant renewalTime) {

        if ((entry.getRevision() == null) || entry.getRevision().trim().isEmpty()) {
            return new RenewalResult(entry, true, null);
        }
        try {
            Path path = toPath(entry.getKey());
            BasicFileAttributes attributes;
            try {
                attributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            } catch (java.nio.file.NoSuchFileException e) {
                return new RenewalResult(entry, true, null);
            }
            if (!attributes.isRegularFile() || !entry.getRevision().equals(createRevision(attributes))) {
                return new RenewalResult(entry, true, null);
            }
            Files.setLastModifiedTime(path, FileTime.from(renewalTime));
            return new RenewalResult(entry, false, null);
        } catch (Exception e) {
            return new RenewalResult(entry, false, e);
        }
    }

    /** Creates a maintenance entry without an additional file-system metadata lookup. */
    private CmsImageCacheEntry toEntry(Path path, BasicFileAttributes attributes) {

        String key = getRepository().relativize(path).toString().replace(path.getFileSystem().getSeparator(), "/");
        return new CmsImageCacheEntry(
            key,
            attributes.size(),
            attributes.lastModifiedTime().toInstant(),
            createRevision(attributes));
    }
}

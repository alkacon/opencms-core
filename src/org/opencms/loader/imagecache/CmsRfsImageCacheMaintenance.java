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

import org.opencms.loader.CmsRfsImageCache;
import org.opencms.loader.imagecache.CmsImageCacheCapabilities.Capability;

import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Maintenance adapter for the classic RFS image cache.<p>
 */
public class CmsRfsImageCacheMaintenance implements I_CmsImageCacheMaintenance {

    /** The supported capabilities. */
    private static final CmsImageCacheCapabilities CAPABILITIES = CmsImageCacheCapabilities.of(
        Capability.LIST_ENTRIES,
        Capability.ENTRY_TIMESTAMPS,
        Capability.DELETE_ENTRIES,
        Capability.CLEAR,
        Capability.RENEW_ENTRIES);

    /** The cache. */
    private final CmsRfsImageCache m_cache;

    /** The normalized repository path. */
    private final Path m_repository;

    /**
     * Creates a maintenance adapter.<p>
     *
     * @param cache the RFS cache
     */
    public CmsRfsImageCacheMaintenance(CmsRfsImageCache cache) {

        m_cache = cache;
        m_repository = Paths.get(cache.getRepositoryPath()).toAbsolutePath().normalize();
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
                    m_cache.clear();
                    result.addSuccess();
                } catch (Exception e) {
                    result.addFailure(getBackendId(), e);
                }
                break;
            case DELETE:
                for (CmsImageCacheEntry entry : request.getEntries()) {
                    delete(entry, result);
                }
                removeEmptyDirectories();
                break;
            case RENEW:
                FileTime renewalTime = FileTime.from(request.getRenewalTime());
                for (CmsImageCacheEntry entry : request.getEntries()) {
                    renew(entry, renewalTime, result);
                }
                break;
            default:
                throw new IllegalArgumentException(
                    "Unsupported RFS image cache maintenance request: " + request.getOperation());
        }
        return result.build(Duration.ofNanos(System.nanoTime() - start));
    }

    /**
     * @see org.opencms.loader.imagecache.I_CmsImageCacheMaintenance#getBackendId()
     */
    @Override
    public String getBackendId() {

        return "rfs";
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

        Path path = toPath(key);
        BasicFileAttributes attributes;
        try {
            attributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (java.nio.file.NoSuchFileException e) {
            return null;
        }
        if (!attributes.isRegularFile()) {
            return null;
        }
        return new CmsImageCacheEntry(
            toKey(path),
            attributes.size(),
            attributes.lastModifiedTime().toInstant(),
            attributes.lastModifiedTime().toMillis() + ":" + attributes.size());
    }

    /**
     * @see org.opencms.loader.imagecache.I_CmsImageCacheMaintenance#visitEntries(org.opencms.loader.imagecache.I_CmsImageCacheMaintenanceEntryVisitor)
     */
    @Override
    public void visitEntries(I_CmsImageCacheMaintenanceEntryVisitor visitor) throws Exception {

        try (Stream<Path> paths = Files.walk(m_repository)) {
            Iterator<Path> iterator = paths.filter(
                path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)).iterator();
            while (iterator.hasNext()) {
                Path path = iterator.next();
                BasicFileAttributes attributes = Files.readAttributes(
                    path,
                    BasicFileAttributes.class,
                    LinkOption.NOFOLLOW_LINKS);
                String key = toKey(path);
                String revision = attributes.lastModifiedTime().toMillis() + ":" + attributes.size();
                visitor.visit(
                    new CmsImageCacheEntry(
                        key,
                        attributes.size(),
                        attributes.lastModifiedTime().toInstant(),
                        revision));
            }
        }
    }

    /**
     * Called after an entry has been deleted.<p>
     *
     * @param key the deleted cache key
     * @throws Exception if post-processing fails
     */
    protected void afterDelete(String key) throws Exception {

        // default no-op
    }

    /**
     * Returns the normalized repository path.<p>
     *
     * @return the repository path
     */
    protected Path getRepository() {

        return m_repository;
    }

    /** Removes empty repository subdirectories. */
    protected void removeEmptyDirectories() throws Exception {

        try (Stream<Path> paths = Files.walk(m_repository)) {
            Iterator<Path> iterator = paths.filter(path -> !m_repository.equals(path)).sorted(
                java.util.Comparator.reverseOrder()).iterator();
            while (iterator.hasNext()) {
                Path path = iterator.next();
                if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                    try {
                        Files.delete(path);
                    } catch (DirectoryNotEmptyException e) {
                        // expected for directories which still contain cache entries
                    }
                }
            }
        }
    }

    /** Returns the normalized cache path for a key. */
    protected Path toPath(String key) {

        String normalizedKey = key;
        while (normalizedKey.startsWith("/")) {
            normalizedKey = normalizedKey.substring(1);
        }
        Path result = m_repository.resolve(normalizedKey).normalize();
        if (!result.startsWith(m_repository) || result.equals(m_repository)) {
            throw new IllegalArgumentException("Image cache key outside repository: " + key);
        }
        return result;
    }

    /** Deletes a selected entry. */
    private void delete(CmsImageCacheEntry entry, CmsImageCacheMaintenanceResult.Builder result) {

        try {
            if (Files.deleteIfExists(toPath(entry.getKey()))) {
                afterDelete(entry.getKey());
                result.addSuccess();
            } else {
                result.addSkipped();
            }
        } catch (Exception e) {
            result.addFailure(entry.getKey(), e);
        }
    }

    /** Renews a selected entry. */
    private void renew(CmsImageCacheEntry entry, FileTime renewalTime, CmsImageCacheMaintenanceResult.Builder result) {

        try {
            Path path = toPath(entry.getKey());
            if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                result.addSkipped();
                return;
            }
            Files.setLastModifiedTime(path, renewalTime);
            result.addSuccess();
        } catch (Exception e) {
            result.addFailure(entry.getKey(), e);
        }
    }

    /** Returns a portable cache key for a repository path. */
    private String toKey(Path path) {

        return m_repository.relativize(path).toString().replace(path.getFileSystem().getSeparator(), "/");
    }
}

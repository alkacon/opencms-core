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
import org.opencms.loader.CmsImageLoader;
import org.opencms.loader.CmsRfsImageCache;
import org.opencms.loader.CmsS3ImageCache;
import org.opencms.loader.I_CmsImageCache;
import org.opencms.loader.imagecache.CmsImageCacheCapabilities.Capability;
import org.opencms.main.OpenCms;

import java.util.Objects;

/**
 * Capability-aware entry point for image cache maintenance clients.<p>
 */
public final class CmsImageCacheMaintenanceService {

    /** The maintenance backend. */
    private final I_CmsImageCacheMaintenance m_backend;

    /**
     * Creates a maintenance service.<p>
     *
     * @param backend the maintenance backend
     */
    public CmsImageCacheMaintenanceService(I_CmsImageCacheMaintenance backend) {

        m_backend = Objects.requireNonNull(backend, "backend");
    }

    /**
     * Creates a service for an image cache implementation.<p>
     *
     * @param cache the image cache
     * @return the maintenance service
     */
    public static CmsImageCacheMaintenanceService create(I_CmsImageCache cache) {

        Objects.requireNonNull(cache, "cache");
        if (cache instanceof CmsFsImageCache) {
            return new CmsImageCacheMaintenanceService(new CmsFsImageCacheMaintenance((CmsFsImageCache)cache));
        }
        if (cache instanceof CmsS3ImageCache) {
            return new CmsImageCacheMaintenanceService(new CmsS3ImageCacheMaintenance((CmsS3ImageCache)cache));
        }
        if (cache instanceof CmsRfsImageCache) {
            return new CmsImageCacheMaintenanceService(new CmsRfsImageCacheMaintenance((CmsRfsImageCache)cache));
        }
        throw new IllegalArgumentException("Unsupported image cache implementation: " + cache.getClass().getName());
    }

    /**
     * Creates a service using the central image cache maintenance configuration.<p>
     *
     * @param cache the image cache
     * @param configuration the image cache configuration
     * @return the maintenance service
     */
    public static CmsImageCacheMaintenanceService create(
        I_CmsImageCache cache,
        CmsImageCacheConfiguration configuration) {

        Objects.requireNonNull(cache, "cache");
        Objects.requireNonNull(configuration, "configuration");
        if (cache instanceof CmsFsImageCache) {
            return new CmsImageCacheMaintenanceService(
                new CmsFsImageCacheMaintenance(
                    (CmsFsImageCache)cache,
                    configuration.isFsTouchEnabled(),
                    configuration.getFsTouchConcurrency()));
        }
        if (cache instanceof CmsS3ImageCache) {
            return new CmsImageCacheMaintenanceService(
                new CmsS3ImageCacheMaintenance(
                    (CmsS3ImageCache)cache,
                    configuration.getS3DeleteBatchSize(),
                    configuration.getS3DeleteConcurrency(),
                    configuration.getS3CopyConcurrency(),
                    configuration.getS3MaxCopiesPerSecond(),
                    configuration.getS3RenewalQueueCapacity()));
        }
        return create(cache);
    }

    /**
     * Creates a service for the image cache currently configured in the image loader.<p>
     *
     * @return the maintenance service
     * @throws Exception if the configured cache can not be accessed
     */
    public static CmsImageCacheMaintenanceService createForConfiguredCache() throws Exception {

        I_CmsImageCache imageCache = CmsImageLoader.getImageCache();
        if (imageCache != null) {
            return create(imageCache, OpenCms.getImageCacheConfiguration());
        }
        String repositoryPath = CmsImageLoader.getImageRepositoryPath();
        if (repositoryPath != null) {
            return createForRfsRepository(repositoryPath);
        }
        throw new IllegalStateException("No image cache repository is configured.");
    }

    /**
     * Creates a service for the classic RFS image cache repository.<p>
     *
     * @param repositoryPath the repository path
     * @return the maintenance service
     * @throws Exception if the repository can not be initialized
     */
    public static CmsImageCacheMaintenanceService createForRfsRepository(String repositoryPath) throws Exception {

        return create(new CmsRfsImageCache(repositoryPath));
    }

    /**
     * Executes a maintenance request after checking backend capabilities.<p>
     *
     * @param request the request
     * @return the result
     * @throws Exception if execution fails
     */
    public CmsImageCacheMaintenanceResult execute(CmsImageCacheMaintenanceRequest request) throws Exception {

        Capability capability;
        switch (request.getOperation()) {
            case CLEAR:
                capability = Capability.CLEAR;
                break;
            case DELETE:
                capability = Capability.DELETE_ENTRIES;
                break;
            case RENEW:
                capability = Capability.RENEW_ENTRIES;
                break;
            default:
                throw new IllegalArgumentException(
                    "Unsupported image cache maintenance operation: " + request.getOperation());
        }
        requireCapability(capability);
        return m_backend.execute(request);
    }

    /** Returns the backend identifier. */
    public String getBackendId() {

        return m_backend.getBackendId();
    }

    /** Returns the backend capabilities. */
    public CmsImageCacheCapabilities getCapabilities() {

        return m_backend.getCapabilities();
    }

    /**
     * Loads metadata for one cache entry.<p>
     *
     * @param key the image cache key
     * @return the entry metadata, or {@code null} if the entry does not exist
     * @throws Exception if the metadata can not be loaded
     */
    public CmsImageCacheEntry getEntry(String key) throws Exception {

        requireCapability(Capability.LIST_ENTRIES);
        return m_backend.getEntry(key);
    }

    /**
     * Streams all cache entries.<p>
     *
     * @param visitor the visitor
     * @throws Exception if listing fails
     */
    public void visitEntries(I_CmsImageCacheMaintenanceEntryVisitor visitor) throws Exception {

        requireCapability(Capability.LIST_ENTRIES);
        m_backend.visitEntries(visitor);
    }

    /** Ensures that a backend capability is available. */
    private void requireCapability(Capability capability) {

        if (!m_backend.getCapabilities().supports(capability)) {
            throw new UnsupportedOperationException(
                "Image cache backend " + m_backend.getBackendId() + " does not support " + capability + ".");
        }
    }
}

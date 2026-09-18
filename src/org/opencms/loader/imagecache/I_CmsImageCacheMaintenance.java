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

/**
 * Backend SPI for image cache maintenance.<p>
 */
public interface I_CmsImageCacheMaintenance {

    /**
     * Executes a maintenance request.<p>
     *
     * @param request the request
     * @return the maintenance result
     * @throws Exception if the request can not be completed
     */
    CmsImageCacheMaintenanceResult execute(CmsImageCacheMaintenanceRequest request) throws Exception;

    /**
     * Returns the backend identifier used for diagnostics and metrics.<p>
     *
     * @return the backend identifier
     */
    String getBackendId();

    /**
     * Returns the backend capabilities.<p>
     *
     * @return the backend capabilities
     */
    CmsImageCacheCapabilities getCapabilities();

    /**
     * Loads metadata for a single image cache entry.<p>
     *
     * @param key the image cache key
     * @return the entry metadata, or {@code null} if the entry does not exist
     * @throws Exception if the metadata can not be loaded
     */
    default CmsImageCacheEntry getEntry(String key) throws Exception {

        String normalizedKey = key;
        while (normalizedKey.startsWith("/")) {
            normalizedKey = normalizedKey.substring(1);
        }
        final String expectedKey = normalizedKey;
        CmsImageCacheEntry[] result = new CmsImageCacheEntry[1];
        visitEntries(entry -> {
            String entryKey = entry.getKey();
            while (entryKey.startsWith("/")) {
                entryKey = entryKey.substring(1);
            }
            if (expectedKey.equals(entryKey)) {
                result[0] = entry;
            }
        });
        return result[0];
    }

    /**
     * Streams all image cache entries to a visitor.<p>
     *
     * @param visitor the entry visitor
     * @throws Exception if listing fails
     */
    void visitEntries(I_CmsImageCacheMaintenanceEntryVisitor visitor) throws Exception;
}

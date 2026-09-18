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

/**
 * Provides metadata which was obtained as part of a preceding image cache read.<p>
 *
 * This avoids an additional metadata request when access-triggered renewal evaluates an entry after successful
 * delivery.<p>
 */
public interface I_CmsImageCacheAccessMetadataProvider {

    /**
     * Returns recently observed metadata for an entry without accessing the backend.<p>
     *
     * @param key the image cache key
     * @return the metadata, or {@code null} if no metadata is locally available
     */
    CmsImageCacheEntry getRecentAccessMetadata(String key);
}

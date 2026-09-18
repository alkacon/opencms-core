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

import java.time.Instant;
import java.util.Objects;

/**
 * Backend-neutral metadata for an image cache entry.<p>
 */
public final class CmsImageCacheEntry {

    /** The cache key. */
    private final String m_key;

    /** The entry length. */
    private final long m_length;

    /** The last modification time, if available. */
    private final Instant m_lastModified;

    /** An optional opaque backend revision. */
    private final String m_revision;

    /**
     * Creates an image cache entry.<p>
     *
     * @param key the cache key
     * @param length the entry length
     * @param lastModified the last modification time, or {@code null}
     * @param revision an opaque backend revision, or {@code null}
     */
    public CmsImageCacheEntry(String key, long length, Instant lastModified, String revision) {

        m_key = Objects.requireNonNull(key, "key");
        if (key.isEmpty()) {
            throw new IllegalArgumentException("The image cache entry key must not be empty.");
        }
        if (length < 0) {
            throw new IllegalArgumentException("The image cache entry length must not be negative.");
        }
        m_length = length;
        m_lastModified = lastModified;
        m_revision = revision;
    }

    /**
     * Returns the cache key.<p>
     *
     * @return the cache key
     */
    public String getKey() {

        return m_key;
    }

    /**
     * Returns the last modification time.<p>
     *
     * @return the last modification time, or {@code null}
     */
    public Instant getLastModified() {

        return m_lastModified;
    }

    /**
     * Returns the entry length.<p>
     *
     * @return the entry length
     */
    public long getLength() {

        return m_length;
    }

    /**
     * Returns the opaque backend revision.<p>
     *
     * @return the backend revision, or {@code null}
     */
    public String getRevision() {

        return m_revision;
    }
}

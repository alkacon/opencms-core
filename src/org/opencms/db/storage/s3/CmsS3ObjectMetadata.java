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

package org.opencms.db.storage.s3;

import java.time.Instant;
import java.util.Objects;

/**
 * Metadata returned by an S3 object listing.<p>
 */
public final class CmsS3ObjectMetadata {

    /** The object key. */
    private final String m_key;

    /** The object last-modified timestamp. */
    private final Instant m_lastModified;

    /** The object length. */
    private final long m_length;

    /** The opaque object revision, usually the ETag. */
    private final String m_revision;

    /**
     * Creates object metadata.<p>
     *
     * @param key the object key
     * @param length the object length
     * @param lastModified the last-modified timestamp, or {@code null}
     * @param revision the opaque revision, or {@code null}
     */
    public CmsS3ObjectMetadata(String key, long length, Instant lastModified, String revision) {

        m_key = Objects.requireNonNull(key, "key");
        if (length < 0) {
            throw new IllegalArgumentException("The S3 object length must not be negative.");
        }
        m_length = length;
        m_lastModified = lastModified;
        m_revision = revision;
    }

    /** Returns the object key. */
    public String getKey() {

        return m_key;
    }

    /** Returns the last-modified timestamp, or {@code null}. */
    public Instant getLastModified() {

        return m_lastModified;
    }

    /** Returns the object length. */
    public long getLength() {

        return m_length;
    }

    /** Returns the opaque object revision, or {@code null}. */
    public String getRevision() {

        return m_revision;
    }

}

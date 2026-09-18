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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A backend-neutral image cache maintenance request.<p>
 */
public final class CmsImageCacheMaintenanceRequest {

    /** Maintenance operations. */
    public enum Operation {

        /** Clears the complete cache. */
        CLEAR,

        /** Deletes selected entries. */
        DELETE,

        /** Renews selected entries. */
        RENEW
    }

    /** The selected entries. */
    private final List<CmsImageCacheEntry> m_entries;

    /** The operation. */
    private final Operation m_operation;

    /** The requested renewal timestamp. */
    private final Instant m_renewalTime;

    /**
     * Creates a request.<p>
     *
     * @param operation the operation
     * @param entries the selected entries
     * @param renewalTime the renewal timestamp
     */
    private CmsImageCacheMaintenanceRequest(
        Operation operation,
        List<CmsImageCacheEntry> entries,
        Instant renewalTime) {

        m_operation = Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(entries, "entries");
        m_entries = Collections.unmodifiableList(new ArrayList<CmsImageCacheEntry>(entries));
        if ((operation == Operation.CLEAR) && !entries.isEmpty()) {
            throw new IllegalArgumentException("A clear request must not contain cache entries.");
        }
        if ((operation == Operation.RENEW) && (renewalTime == null)) {
            throw new IllegalArgumentException("A renewal request requires a renewal timestamp.");
        }
        Set<String> keys = new HashSet<String>();
        for (CmsImageCacheEntry entry : entries) {
            if (!keys.add(entry.getKey())) {
                throw new IllegalArgumentException("Duplicate image cache entry key: " + entry.getKey());
            }
        }
        m_renewalTime = renewalTime;
    }

    /**
     * Creates a clear request.<p>
     *
     * @return the request
     */
    public static CmsImageCacheMaintenanceRequest clear() {

        return new CmsImageCacheMaintenanceRequest(Operation.CLEAR, Collections.emptyList(), null);
    }

    /**
     * Creates a delete request.<p>
     *
     * @param entries the entries to delete
     * @return the request
     */
    public static CmsImageCacheMaintenanceRequest delete(List<CmsImageCacheEntry> entries) {

        return new CmsImageCacheMaintenanceRequest(Operation.DELETE, entries, null);
    }

    /**
     * Creates a renewal request.<p>
     *
     * @param entries the entries to renew
     * @param renewalTime the timestamp to apply
     * @return the request
     */
    public static CmsImageCacheMaintenanceRequest renew(List<CmsImageCacheEntry> entries, Instant renewalTime) {

        return new CmsImageCacheMaintenanceRequest(Operation.RENEW, entries, renewalTime);
    }

    /**
     * Returns the selected entries.<p>
     *
     * @return the selected entries
     */
    public List<CmsImageCacheEntry> getEntries() {

        return m_entries;
    }

    /**
     * Returns the operation.<p>
     *
     * @return the operation
     */
    public Operation getOperation() {

        return m_operation;
    }

    /**
     * Returns the requested renewal timestamp.<p>
     *
     * @return the renewal timestamp, or {@code null}
     */
    public Instant getRenewalTime() {

        return m_renewalTime;
    }
}

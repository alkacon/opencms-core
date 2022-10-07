/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.upload;

import org.opencms.util.CmsUUID;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * A table to temporarily store warnings that should be displayed for uploaded files.
 *
 * <p>Warning messages are automatically removed from the table after some time.
 */
public class CmsUploadWarningTable {

    /** The internal cache. */
    private Cache<CmsUUID, String> m_cache;

    /**
     * Creates a new instance.
     */
    public CmsUploadWarningTable() {

        m_cache = CacheBuilder.<CmsUUID, String> newBuilder().concurrencyLevel(2).expireAfterWrite(
            12,
            TimeUnit.HOURS).build();

    }

    /**
     * Clears the table.
     */
    public void clear() {

        m_cache.invalidateAll();
    }

    /**
     * Gets the warning message for the given structure id.
     *
     * @param id a resource structure id
     * @return the warning message (or null if there is no message)
     */
    public String getMessage(CmsUUID id) {

        return m_cache.getIfPresent(id);
    }

    /**
     * Sets the warning message for the given structure id (or clears it, if the message isn null).
     *
     * @param id the structure id of a resource
     * @param message the message to store
     */
    public void setMessage(CmsUUID id, String message) {

        if (message != null) {
            m_cache.put(id, message);
        } else {
            m_cache.invalidate(id);
        }
    }

    /**
     * Gets the size of the table.
     *
     * @return the size
     */
    public long size() {

        return m_cache.size();
    }

}

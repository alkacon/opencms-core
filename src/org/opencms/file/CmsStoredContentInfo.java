/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
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
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file;

import org.opencms.util.CmsStringUtil;

/**
 * Describes where the binary content for a VFS resource is stored without loading the content bytes.<p>
 */
public class CmsStoredContentInfo {

    /** The content hash. */
    private final String m_hash;

    /** The content length. */
    private final long m_length;

    /** The resource. */
    private final CmsResource m_resource;

    /** The storage identifier. */
    private final String m_storage;

    /**
     * Creates a new stored content info instance.<p>
     *
     * @param resource the resource
     * @param storage the storage identifier
     * @param hash the content hash
     * @param length the content length
     */
    public CmsStoredContentInfo(CmsResource resource, String storage, String hash, long length) {

        m_resource = resource;
        m_storage = storage;
        m_hash = hash;
        m_length = length;
    }

    /**
     * Returns the content hash.<p>
     *
     * @return the content hash
     */
    public String getHash() {

        return m_hash;
    }

    /**
     * Returns the content length.<p>
     *
     * @return the content length
     */
    public long getLength() {

        return m_length;
    }

    /**
     * Returns the resource.<p>
     *
     * @return the resource
     */
    public CmsResource getResource() {

        return m_resource;
    }

    /**
     * Returns the storage identifier.<p>
     *
     * @return the storage identifier
     */
    public String getStorage() {

        return m_storage;
    }

    /**
     * Returns if the content is externally stored.<p>
     *
     * @return <code>true</code> if the content is externally stored
     */
    public boolean isExternallyStored() {

        return CmsStringUtil.isNotEmpty(m_storage) && CmsStringUtil.isNotEmpty(m_hash);
    }

    /**
     * Returns if the content is stored in the given storage backend.<p>
     *
     * @param storage the storage identifier
     *
     * @return <code>true</code> if the content is stored in the given storage backend
     */
    public boolean isStoredIn(String storage) {

        return isExternallyStored() && m_storage.equals(storage);
    }
}

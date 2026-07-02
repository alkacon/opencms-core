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

package org.opencms.db.storage;

import org.opencms.file.CmsResource;
import org.opencms.util.CmsUUID;

/**
 * Cache key for stored content info entries.<p>
 */
public final class CmsStoredContentInfoCacheKey {

    /** The content date. */
    private final long m_dateContent;

    /** The project id. */
    private final CmsUUID m_projectId;

    /** The resource id. */
    private final CmsUUID m_resourceId;

    /** The resource version. */
    private final int m_version;

    /**
     * Creates a new cache key.<p>
     *
     * @param projectId the project id
     * @param resource the resource
     */
    public CmsStoredContentInfoCacheKey(CmsUUID projectId, CmsResource resource) {

        m_projectId = projectId;
        m_resourceId = resource.getResourceId();
        m_version = resource.getVersion();
        m_dateContent = resource.getDateContent();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CmsStoredContentInfoCacheKey)) {
            return false;
        }
        CmsStoredContentInfoCacheKey other = (CmsStoredContentInfoCacheKey)obj;
        return m_dateContent == other.m_dateContent
            && m_version == other.m_version
            && m_projectId.equals(other.m_projectId)
            && m_resourceId.equals(other.m_resourceId);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        int result = m_projectId.hashCode();
        result = (31 * result) + m_resourceId.hashCode();
        result = (31 * result) + m_version;
        result = (31 * result) + (int)(m_dateContent ^ (m_dateContent >>> 32));
        return result;
    }
}

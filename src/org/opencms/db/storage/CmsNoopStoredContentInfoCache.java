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

import org.opencms.file.CmsStoredContentInfo;

/**
 * No-op stored content info cache.<p>
 */
public class CmsNoopStoredContentInfoCache implements I_CmsStoredContentInfoCache {

    /**
     * @see org.opencms.db.storage.I_CmsStoredContentInfoCache#clear()
     */
    public void clear() {

        // noop
    }

    /**
     * @see org.opencms.db.storage.I_CmsStoredContentInfoCache#get(org.opencms.db.storage.CmsStoredContentInfoCacheKey)
     */
    public CmsStoredContentInfo get(CmsStoredContentInfoCacheKey key) {

        return null;
    }

    /**
     * @see org.opencms.db.storage.I_CmsStoredContentInfoCache#put(org.opencms.db.storage.CmsStoredContentInfoCacheKey, org.opencms.file.CmsStoredContentInfo)
     */
    public void put(CmsStoredContentInfoCacheKey key, CmsStoredContentInfo info) {

        // noop
    }

    /**
     * @see org.opencms.db.storage.I_CmsStoredContentInfoCache#remove(org.opencms.db.storage.CmsStoredContentInfoCacheKey)
     */
    public void remove(CmsStoredContentInfoCacheKey key) {

        // noop
    }
}

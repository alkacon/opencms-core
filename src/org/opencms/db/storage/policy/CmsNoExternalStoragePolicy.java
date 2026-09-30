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

package org.opencms.db.storage.policy;

/**
 * Compatibility policy for the OpenCms deduplicating media storage engine (DMSE)
 * which keeps newly written content in the regular VFS content columns.<p>
 *
 * This policy can be used with the storage-aware VFS driver when the storage
 * schema should be installed, but binary content should remain in
 * {@code CMS_CONTENTS} and {@code CMS_OFFLINE_CONTENTS} for compatibility or
 * migration purposes. It does not move previously stored blobs back to those
 * columns; they remain readable through their configured backends.<p>
 *
 * @see org.opencms.db.storage.CmsStorageManager
 */
public class CmsNoExternalStoragePolicy implements I_CmsStoragePolicy {

    /**
     * @see org.opencms.db.storage.policy.I_CmsStoragePolicy#isExternalStorageRequired(CmsStoragePolicyContext)
     */
    @Override
    public boolean isExternalStorageRequired(CmsStoragePolicyContext context) {

        return false;
    }
}

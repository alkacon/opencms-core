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

package org.opencms.db.storage;

import org.opencms.db.CmsDbContext;

/**
 * Optional storage capability for enumerating stored content blobs.<p>
 *
 * This is intended for maintenance tooling such as orphan scans. Implementations
 * should report only content hashes, not temporary files or backend-specific
 * housekeeping objects.<p>
 */
public interface I_CmsEnumerableStorage extends I_CmsStorage {

    /**
     * Visitor for content hashes.<p>
     */
    interface I_CmsContentHashVisitor {

        /**
         * Visits a content hash.<p>
         *
         * @param hash the SHA-512 content hash
         * @throws Exception if the visitor fails
         */
        void visit(String hash) throws Exception;
    }

    /**
     * Visits all content hashes stored by this backend.<p>
     *
     * @param dbc the database context
     * @param visitor the content hash visitor
     * @throws Exception if enumeration fails
     */
    void visitContentHashes(CmsDbContext dbc, I_CmsContentHashVisitor visitor) throws Exception;
}

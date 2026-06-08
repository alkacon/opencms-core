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

import org.opencms.db.CmsDbContext;

/**
 * Defines a storage strategy for binary resource content.<p>
 *
 * This interface allows to abstract the physical storage of file contents,
 * enabling storage to external systems like S3.<p>
 */
public interface I_CmsStorage extends AutoCloseable {

    /**
     * Closes this storage backend and releases associated resources.<p>
     *
     * @throws Exception if closing the storage backend fails
     */
    default void close() throws Exception {

        // default no-op
    }

    /**
     * Deletes the content from the storage if applicable.<p>
     *
     * @param dbc the database context
     * @param hash the SHA-512 content hash
     * @throws Exception if the deletion in the external storage fails
     */
    void deleteContent(CmsDbContext dbc, String hash) throws Exception;

    /**
     * Returns the stable storage identifier stored in the VFS content tables.<p>
     *
     * The identifier is the configured backend id, for example {@code media} or
     * {@code archive}. The database storage uses the reserved backend id
     * {@code db}. The identifier must not depend on mutable connection details
     * such as paths, endpoints, or bucket names.<p>
     *
     * @return the stable storage identifier
     */
    String getStorageIdentifier();

    /**
     * Loads the actual binary content.<p>
     *
     * @param dbc the database context
     * @param hash the SHA-512 content hash
     *
     * @return the actual binary file content
     * @throws Exception if the content cannot be retrieved
     */
    byte[] loadContent(CmsDbContext dbc, String hash) throws Exception;

    /**
     * Stores the given content.<p>
     *
     * @param dbc the database context
     * @param hash the SHA-512 content hash
     * @param content the actual binary data to store
     *
     * @throws Exception if the storage operation fails
     */
    void storeContent(CmsDbContext dbc, String hash, byte[] content) throws Exception;

    /**
     * Validates that the storage backend is available for read and write operations.<p>
     *
     * This method is called during startup for configured storage backends. Implementations should
     * perform a lightweight roundtrip which verifies the required prerequisites for storing content.
     *
     * @param dbc the database context
     *
     * @throws Exception if the storage backend is not available
     */
    void validateAvailable(CmsDbContext dbc) throws Exception;
}

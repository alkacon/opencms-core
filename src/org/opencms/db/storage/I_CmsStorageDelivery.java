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

import java.io.OutputStream;

/**
 * Optional capability for storage backends which can be used for HTTP delivery.<p>
 */
public interface I_CmsStorageDelivery {

    /**
     * Streams a byte range to the given output stream.<p>
     *
     * @param dbc the database context
     * @param hash the SHA-512 content hash
     * @param start the first byte to stream
     * @param length the number of bytes to stream
     *
     * @param out the output stream to write to
     *
     * @throws Exception if streaming fails
     */
    void streamRangeTo(CmsDbContext dbc, String hash, long start, long length, OutputStream out) throws Exception;

    /**
     * Streams the content to the given output stream.<p>
     *
     * @param dbc the database context
     * @param hash the SHA-512 content hash
     * @param out the output stream to write to
     *
     * @throws Exception if streaming fails
     */
    void streamTo(CmsDbContext dbc, String hash, OutputStream out) throws Exception;

    /**
     * Returns if range delivery is supported.<p>
     *
     * @return <code>true</code> if byte range delivery is supported
     */
    boolean supportsRangeDelivery();
}

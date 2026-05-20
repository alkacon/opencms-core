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

public abstract class A_CmsStorage {

    /**
     * Converts a content hash into a 3-level slash-separated storage path.<p>
     *
     * @param hash the content hash
     * @return the hashed storage path
     */
    protected String getHashedPath(String hash) {

        String normalizedHash = validateHash(hash);
        return normalizedHash.substring(0, 2)
            + "/"
            + normalizedHash.substring(2, 4)
            + "/"
            + normalizedHash.substring(4, 6)
            + "/"
            + normalizedHash;
    }

    /**
     * Validates and normalizes a content hash.<p>
     *
     * @param hash the content hash
     * @return the normalized content hash
     */
    protected String validateHash(String hash) {

        if ((hash == null) || (hash.length() != 128)) {
            throw new IllegalArgumentException(Messages.get().getBundle().key(Messages.ERR_STORAGE_HASH_INVALID_0));
        }
        for (int i = 0; i < hash.length(); i++) {
            char c = hash.charAt(i);
            if (!(((c >= '0') && (c <= '9')) || ((c >= 'a') && (c <= 'f')) || ((c >= 'A') && (c <= 'F')))) {
                throw new IllegalArgumentException(Messages.get().getBundle().key(Messages.ERR_STORAGE_HASH_INVALID_0));
            }
        }
        return hash.toLowerCase();
    }
}

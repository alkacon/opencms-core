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

package org.opencms.loader;

import java.io.IOException;

/**
 * Signals that an image cache entry disappeared after it had already been found locally.<p>
 *
 * External image caches use this exception to distinguish a stale local metadata entry from other storage errors.
 * The image loader can then regenerate the derivative and retry its delivery once. Streaming methods must raise this
 * exception before writing content to the supplied output stream.<p>
 */
public class CmsImageCacheEntryNotFoundException extends IOException {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new exception.<p>
     *
     * @param key the missing image cache key
     */
    public CmsImageCacheEntryNotFoundException(String key) {

        super("Image cache entry not found: " + key);
    }

    /**
     * Creates a new exception.<p>
     *
     * @param key the missing image cache key
     * @param cause the root cause
     */
    public CmsImageCacheEntryNotFoundException(String key, Throwable cause) {

        super("Image cache entry not found: " + key, cause);
    }
}

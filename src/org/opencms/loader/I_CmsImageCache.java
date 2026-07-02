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

package org.opencms.loader;

import java.io.OutputStream;

/**
 * Storage for generated image cache entries.<p>
 */
public interface I_CmsImageCache {

    /**
     * Returns if the image cache entry exists.<p>
     *
     * @param key the image cache key
     * @return <code>true</code> if the image cache entry exists
     * @throws Exception if the store access fails
     */
    boolean exists(String key) throws Exception;

    /**
     * Returns the image cache entry length.<p>
     *
     * @param key the image cache key
     * @return the image cache entry length
     * @throws Exception if the store access fails
     */
    long getLength(String key) throws Exception;

    /**
     * Returns if range delivery is supported.<p>
     *
     * @return <code>true</code> if ranges can be streamed
     */
    boolean supportsRangeDelivery();

    /**
     * Writes an image cache entry.<p>
     *
     * @param key the image cache key
     * @param content the image cache content
     * @throws Exception if writing fails
     */
    void write(String key, byte[] content) throws Exception;

    /**
     * Writes an image cache entry byte range to the output stream.<p>
     *
     * @param key the image cache key
     * @param start the first byte to write
     * @param length the number of bytes to write
     * @param out the output stream
     * @throws Exception if reading fails
     */
    void writeRangeTo(String key, long start, long length, OutputStream out) throws Exception;

    /**
     * Writes an image cache entry to the output stream.<p>
     *
     * @param key the image cache key
     * @param out the output stream
     * @throws Exception if reading fails
     */
    void writeTo(String key, OutputStream out) throws Exception;
}

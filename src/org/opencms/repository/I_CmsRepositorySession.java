/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.repository;

import org.opencms.main.CmsException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * A repository session which provides basic file and folder operations
 * to the resources in the VFS of OpenCms.<p>
 *
 * @since 6.2.4
 */
public interface I_CmsRepositorySession {

    /**
     * Copies the item found at the source path to the destination path.<p>
     *
     * @param src the path of the item which should be copied
     * @param dest the destination path where to copy to
     * @param overwrite should any existing item be overwritten
     *
     * @throws CmsException if something goes wrong
     */
    void copy(String src, String dest, boolean overwrite) throws CmsException;

    /**
     * Creates a new item at the given path.<p>
     *
     * In this case this should be a collection (directory).<p>
     *
     * @param path the complete path of the new collection
     *
     * @throws CmsException if something goes wrong
     */
    void create(String path) throws CmsException;

    /**
     * Deletes the item at the given path.<p>
     *
     * @param path the complete path of the item to delete
     *
     * @throws CmsException if something goes wrong
     */
    void delete(String path) throws CmsException;

    /**
     * Returns if an item exists at the given path.<p>
     *
     * @param path the complete path of the item to check existance
     *
     * @return true if the item exists otherwise false
     */
    boolean exists(String path);

    /**
     * Returns the item found at the given path.<p>
     *
     * @param path the complete path of the item to return
     *
     * @return the item found at the path
     *
     * @throws CmsException if something goes wrong
     */
    I_CmsRepositoryItem getItem(String path) throws CmsException;

    /**
     * Returns the lock for the resource at the given path.<p>
     *
     * @param path the complete path where to return the lock for
     *
     * @return the found lock as CmsWebdavLockInfo or null if not found
     */
    CmsRepositoryLockInfo getLock(String path);

    /**
     * Returns a list with all items found directly in the given path.<p>
     *
     * @param path the complete path from which to return the items
     *
     * @return a list with {@link I_CmsRepositoryItem} found in the path
     *
     * @throws CmsException if something goes wrong
     */
    List<I_CmsRepositoryItem> list(String path) throws CmsException;

    /**
     * Creates a new lock on the item at the path with the given information
     * in the lock info.<p>
     *
     * @param path the complete path of the item
     * @param lock the information about the lock to create
     *
     * @return if the lock was successfully
     *
     * @throws CmsException if something goes wrong
     */
    boolean lock(String path, CmsRepositoryLockInfo lock) throws CmsException;

    /**
     * Moves an item from a source path to a destination path.<p>
     *
     * @param src the complete path to the item which should be copied
     * @param dest the complete destination path where to copy to
     * @param overwrite should any existing item should be overwritten
     *
     * @throws CmsException if something goes wrong
     */
    void move(String src, String dest, boolean overwrite) throws CmsException;

    /**
     * Saves an item at the given path.<p>
     *
     * This creates a new single item (file) if it does not exist.<p>
     *
     * @param path the complete path of the new item
     * @param inputStream the content of the item
     * @param overwrite should an existing item at the path be overwritten
     *
     * @throws CmsException if something goes wrong
     * @throws IOException if a write error occurs
     */
    void save(String path, InputStream inputStream, boolean overwrite) throws CmsException, IOException;

    /**
     * Unlocks the item found at the path.<p>
     *
     * @param path The complete path of the item to unlock
     */
    void unlock(String path);
}

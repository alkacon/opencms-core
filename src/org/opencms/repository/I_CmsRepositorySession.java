/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/repository/I_CmsRepositorySession.java,v $
 * Date   : $Date: 2007/02/15 15:54:20 $
 * Version: $Revision: 1.1.4.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import java.io.InputStream;
import java.util.List;

/**
 * A session for WebDAV to handle the actions.<p>
 * 
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.1.4.2 $
 * 
 * @since 6.5.6
 */
public interface I_CmsRepositorySession {

    /**
     * Copies the item found at the source path to the destination path.<p>
     * 
     * @param src The path of the item which should be copied
     * @param dest The destination path where to copy to
     * @param overwrite Should any existing item be overwritten
     * 
     * @throws CmsRepositoryException if something goes wrong
     */
    void copy(String src, String dest, boolean overwrite) throws CmsRepositoryException;

    /**
     * Creates a new item at the given path.<p>
     * 
     * In this case this should be a collection (directory).<p>
     * 
     * @param path The complete path of the new collection
     * 
     * @throws CmsRepositoryException if something goes wrong
     */
    void create(String path) throws CmsRepositoryException;

    /**
     * Deletes the item at the given path.<p>
     * 
     * @param path The complete path of the item to delete
     * 
     * @throws CmsRepositoryException if something goes wrong
     */
    void delete(String path) throws CmsRepositoryException;

    /**
     * Returns if an item exists at the given path.<p>
     * 
     * @param path The complete path of the item to check existance
     * 
     * @return true if the item exists otherwise false
     */
    boolean exists(String path);

    /**
     * Returns the item found at the given path.<p>
     * 
     * @param path The complete path of the item to return
     * 
     * @return the item found at the path
     * 
     * @throws CmsRepositoryException if something goes wrong
     */
    I_CmsRepositoryItem getItem(String path) throws CmsRepositoryException;

    /**
     * Returns the lock for the resource at the given path.<p>
     * 
     * @param path The complete path where to return the lock for
     * 
     * @return the found lock as CmsWebdavLockInfo or null if not found
     */
    CmsRepositoryLockInfo getLock(String path);

    /**
     * Returns a list with all items found directly in the given path.<p>
     * 
     * @param path The complete path from which to return the items
     * 
     * @return a list with {@link I_CmsRepositoryItem} found in the path
     * 
     * @throws CmsRepositoryException if something goes wrong
     */
    List list(String path) throws CmsRepositoryException;

    /**
     * Creates a new lock on the item with the path with the given information 
     * in the lock info.<p>
     * 
     * @param path The complete path of the item
     * @param lock The information about the lock to create
     * 
     * @return if the lock was successfully
     * 
     * @throws CmsRepositoryException if something goes wrong
     */
    boolean lock(String path, CmsRepositoryLockInfo lock) throws CmsRepositoryException;

    /**
     * Moves an item from a source path to a destination path.<p>
     * 
     * @param src The complete path to the item which should be copied
     * @param dest The complete destination path where to copy to
     * @param overwrite Should any existing item should be overwritten
     * 
     * @throws CmsRepositoryException if something goes wrong
     */
    void move(String src, String dest, boolean overwrite) throws CmsRepositoryException;

    /**
     * Saves an item at the given path.<p>
     * 
     * This creates a new single item (file) if it does not exist.<p>
     * 
     * @param path The complete path of the new item
     * @param inputStream The content of the item
     * @param overwrite Should an existing item at the path be overwritten
     * 
     * @throws CmsRepositoryException if something goes wrong
     */
    void save(String path, InputStream inputStream, boolean overwrite) throws CmsRepositoryException;

    /**
     * Unlocks the item found at the path.<p>
     * 
     * Should remove the lock token from the existing lock found at the path.<p>
     * 
     * @param path The complete path of the item to unlock
     */
    void unlock(String path);
}

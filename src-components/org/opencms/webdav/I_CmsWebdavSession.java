/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/webdav/Attic/I_CmsWebdavSession.java,v $
 * Date   : $Date: 2007/01/12 17:24:42 $
 * Version: $Revision: 1.1.2.1 $
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

package org.opencms.webdav;

import org.opencms.main.CmsException;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.List;

/**
 * A session for WebDAV to handle the actions.
 * 
 * @author Peter Bonrad
 */
public interface I_CmsWebdavSession {

    /**
     * Copies the item found at the source path to the destination path.
     * 
     * @param src The path of the item which should be copied
     * @param dest The destination path where to copy to
     * @param overwrite Should any existing item should be overwritten
     * @param errorList a table where all the errors occuring are entered. The key
     * is the path and the value is a CmsWebdavStatus.
     * @return the status of the copy action: true if successful otherwise false
     */
    boolean copy(String src, String dest, boolean overwrite, Hashtable errorList);

    /**
     * Creates a new item at the given path. In this case this should
     * be a collection (directory).
     * 
     * @param path The complete path of the new collection
     * @throws CmsException if item at the path already exists
     */
    void create(String path) throws CmsException;

    /**
     * Creates a new item at the given path. This creates a new single
     * item (file).
     * 
     * @param path The complete path of the new item
     * @param inputStream The content of the item
     * @param overwrite Should an existing item at the path be overwritten
     * @throws CmsException if the resource already exists and should not be overwritten
     */
    void create(String path, InputStream inputStream, boolean overwrite) throws CmsException;

    /**
     * Deletes the item at the given path.
     * 
     * @param path The complete path of the item to delete
     * @param errorList a table where all the errors occuring are entered. The key
     * is the path and the value is a CmsWebdavStatus.
     * @return the status of the copy action: true if successful otherwise false
     */
    boolean delete(String path, Hashtable errorList);

    /**
     * Returns if an item exists at the given path.
     * 
     * @param path The complete path of the item to check existance
     * @return true if the item exists otherwise false
     */
    boolean exists(String path);

    /**
     * Returns the item found at the given path. 
     * 
     * @param path The complete path of the item to return
     * @return the item found at the path
     * @throws CmsException if item at the path could not be found
     */
    I_CmsWebdavItem getItem(String path) throws CmsException;

    /**
     * Because it is possible in WebDAV to create shared locks, there is the
     * chance to have a lock on an item with multiple lock tokens. This should
     * return a list of locks (CmsWebdavLockInfo) found at the given path. This
     * includes inherited locks from parent items.
     * 
     * @param path The complete path where to return the locks for
     * @return a list with all found locks (CmsWebdavLockInfo) at the path
     */
    List getLocks(String path);

    /**
     * Returns the content of the item found at the given path as an input stream.
     * 
     * @param path The complete path where to find the item
     * @return the content of the resource as an input stream
     */
    InputStream getStreamContent(String path);

    /**
     * Checks if the item at the path is locked. 
     * 
     * @param path The complete path of the item to check the lock for
     * @param lockTokens The existing lock tokens sent by the client
     * @return true if the item is locked otherwise false
     */
    boolean isLocked(String path, String lockTokens);

    /**
     * Returns a list with all items found directly in the given path.
     * 
     * @param path The complete path from which to return the items
     * @return a list with (I_CmsWebdavItem) found in the path
     * @throws CmsException if item at the path could not be found
     */
    List list(String path) throws CmsException;

    /**
     * Creates a new lock on the item with the path with the given information 
     * in the lock info.
     * 
     * @param path The complete path of the item
     * @param lock The information about the lock to create
     * @param lockToken The lock token to add
     * @param errorLocks List which should be filled with items that are already locked
     * @return if the lock token was successfully added
     */
    boolean lock(String path, CmsWebdavLockInfo lock, String lockToken, List errorLocks) throws CmsException;

    /**
     * Moves an item from a source path to a destination path.
     * 
     * @param src The complete path to the item which should be copied
     * @param dest The complete destination path where to copy to
     * @param overwrite Should any existing item should be overwritten
     */
    void move(String src, String dest, boolean overwrite);

    /**
     * Unlocks the item found at the path. Should remove the lock token
     * from the existing lock found at the path.
     * 
     * @param path The complete path of the item to unlock
     * @param lockTokens The lock tokens sent by the client to remove from the lock
     */
    void unlock(String path, String lockTokens);
}

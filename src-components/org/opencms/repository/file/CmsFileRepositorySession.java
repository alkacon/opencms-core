/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/repository/file/Attic/CmsFileRepositorySession.java,v $
 * Date   : $Date: 2007/02/15 15:54:20 $
 * Version: $Revision: 1.1.2.3 $
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

package org.opencms.repository.file;

import org.opencms.repository.CmsRepositoryItemAlreadyExistsException;
import org.opencms.repository.CmsRepositoryItemNotFoundException;
import org.opencms.repository.CmsRepositoryLockInfo;
import org.opencms.repository.CmsRepositoryPermissionException;
import org.opencms.repository.I_CmsRepositoryItem;
import org.opencms.repository.I_CmsRepositorySession;
import org.opencms.util.CmsFileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Class implementing the CmsRepositorySession and stores the items
 * (files and folders) on the real file system.<p>
 * 
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.1.2.3 $
 * 
 * @since 6.5.6
 */
public class CmsFileRepositorySession implements I_CmsRepositorySession {

    /**
     * Vector of the heritable locks.<p>
     * 
     * Key : path <br>
     * Value : LockInfo
     */
    private List m_collectionLocks = new Vector();

    /**
     * Repository of the locks put on single resources.<p>
     * 
     * Key : path <br />
     * Value : LockInfo
     */
    private Map m_resourceLocks = new Hashtable();

    /** The root path where to store files and directories. */
    private String m_root;

    /** 
     * Constructor to create a new session to use the root path.<p>
     * 
     * @param root The root path to use
     */
    public CmsFileRepositorySession(String root) {

        setRoot(root);
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#copy(java.lang.String, java.lang.String, boolean)
     */
    public void copy(String src, String dest, boolean overwrite)
    throws CmsRepositoryItemNotFoundException, CmsRepositoryPermissionException,
    CmsRepositoryItemAlreadyExistsException {

        if ((dest.toUpperCase().startsWith("/WEB-INF")) || (dest.toUpperCase().startsWith("/META-INF"))) {
            throw new CmsRepositoryPermissionException();
        }

        I_CmsRepositoryItem item = getItem(src);

        // Overwriting the destination
        boolean exists = exists(dest);
        if (overwrite) {

            // Delete destination resource, if it exists
            if (exists) {
                delete(dest);
            }
        }

        if (item.isCollection()) {

            create(dest);

            List list = list(src);
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                String element = (String)iter.next();

                String childDest = dest;
                if (!childDest.equals("/")) {
                    childDest += "/";
                }
                childDest += element;

                String childSrc = src;
                if (!childSrc.equals("/")) {
                    childSrc += "/";
                }
                childSrc += element;

                copy(childSrc, childDest, overwrite);
            }

        } else {

            try {
                FileOutputStream out = new FileOutputStream(getAbsolutePath(dest));
                out.write(item.getContent());
                out.close();
            } catch (IOException e) {
                throw new CmsRepositoryItemAlreadyExistsException();
            }
        }
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#create(java.lang.String)
     */
    public void create(String path) throws CmsRepositoryItemAlreadyExistsException, CmsRepositoryPermissionException {

        if ((path.toUpperCase().startsWith("/WEB-INF")) || (path.toUpperCase().startsWith("/META-INF"))) {
            throw new CmsRepositoryPermissionException();
        }

        File dir = new File(getAbsolutePath(path));
        if (dir.exists()) {
            throw new CmsRepositoryItemAlreadyExistsException();
        }

        dir.mkdir();
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#delete(java.lang.String)
     */
    public void delete(String path) throws CmsRepositoryItemNotFoundException, CmsRepositoryPermissionException {

        I_CmsRepositoryItem item = getItem(path);

        boolean collection = item.isCollection();
        if (collection) {
            deleteCollection(path);
        }

        boolean success = (new File(getAbsolutePath(path))).delete();
        if (!success) {
            throw new CmsRepositoryItemNotFoundException();
        }
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#exists(java.lang.String)
     */
    public boolean exists(String path) {

        return (new File(getAbsolutePath(path))).exists();
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#getItem(java.lang.String)
     */
    public I_CmsRepositoryItem getItem(String path)
    throws CmsRepositoryItemNotFoundException, CmsRepositoryPermissionException {

        if ((path.toUpperCase().startsWith("/WEB-INF")) || (path.toUpperCase().startsWith("/META-INF"))) {
            throw new CmsRepositoryPermissionException();
        }

        if (!exists(path)) {
            throw new CmsRepositoryItemNotFoundException();
        }

        CmsFileRepositoryItem item = new CmsFileRepositoryItem();
        File fileItem = new File(getAbsolutePath(path));

        item.setCollection(fileItem.isDirectory());

        if (fileItem.isFile()) {
            try {
                item.setContent(getBytesFromFile(fileItem));
            } catch (IOException ex) {
                // noop
            }
            item.setContentLength(fileItem.length());
        }

        //item.setCreationDate();
        item.setLastModifiedDate(fileItem.lastModified());
        item.setName(path);

        return item;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#getLock(java.lang.String)
     */
    public CmsRepositoryLockInfo getLock(String path) {

        CmsRepositoryLockInfo resourceLock = (CmsRepositoryLockInfo)m_resourceLocks.get(path);
        if (resourceLock != null) {
            return resourceLock;
        }

        Iterator iter = m_collectionLocks.iterator();
        while (iter.hasNext()) {
            CmsRepositoryLockInfo currentLock = (CmsRepositoryLockInfo)iter.next();
            if (path.startsWith(currentLock.getPath())) {
                return currentLock;
            }
        }

        return null;
    }

    /**
     * Returns the root.<p>
     *
     * @return the root
     */
    public String getRoot() {

        return m_root;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#list(java.lang.String)
     */
    public List list(String path) throws CmsRepositoryItemNotFoundException, CmsRepositoryPermissionException {

        if ((path.toUpperCase().startsWith("/WEB-INF")) || (path.toUpperCase().startsWith("/META-INF"))) {
            throw new CmsRepositoryPermissionException();
        }

        List ret = new ArrayList();
        File dir = new File(getAbsolutePath(path));

        if (!dir.exists()) {
            throw new CmsRepositoryItemNotFoundException();
        }

        String[] children = dir.list();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                // Get filename of file or directory
                String filename = children[i];
                ret.add(filename);
            }
        }

        return ret;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#lock(java.lang.String, org.opencms.repository.CmsRepositoryLockInfo)
     */
    public boolean lock(String path, CmsRepositoryLockInfo lock)
    throws CmsRepositoryItemNotFoundException, CmsRepositoryPermissionException {

        I_CmsRepositoryItem item = getItem(path);

        if ((item.isCollection()) && (lock.getDepth() == CmsRepositoryLockInfo.DEPTH_INFINITY_VALUE)) {

            // Locking a collection (and all its member resources)
            // Checking if a child resource of this collection is already locked
            Iterator iter = m_collectionLocks.iterator();
            while (iter.hasNext()) {
                CmsRepositoryLockInfo currentLock = (CmsRepositoryLockInfo)iter.next();
                if (currentLock.hasExpired()) {
                    m_collectionLocks.remove(currentLock.getPath());
                    continue;
                }
                if ((currentLock.getPath().startsWith(lock.getPath()))
                    && ((currentLock.isExclusive()) || (lock.isExclusive()))) {

                    // A child collection of this collection is locked
                    return false;
                }
            }

            // Yikes: modifying the collection not by the iterator's object,
            // but by one of its attributes.  That means we can't use a
            // normal java.util.Iterator here, because Iterator is fail-fast. ;(
            Enumeration locksList = Collections.enumeration(m_resourceLocks.values());
            while (locksList.hasMoreElements()) {
                CmsRepositoryLockInfo currentLock = (CmsRepositoryLockInfo)locksList.nextElement();
                if (currentLock.hasExpired()) {
                    m_resourceLocks.remove(currentLock.getPath());
                    continue;
                }
                if ((currentLock.getPath().startsWith(lock.getPath()))
                    && ((currentLock.isExclusive()) || (lock.isExclusive()))) {

                    // A child resource of this collection is locked
                    return false;
                }
            }

            m_collectionLocks.add(lock);
            return true;

        } else {

            // Locking a single resource
            // Retrieving an already existing lock on that resource
            CmsRepositoryLockInfo presentLock = (CmsRepositoryLockInfo)m_resourceLocks.get(lock.getPath());
            if (presentLock != null) {

                return false;

            } else {

                m_resourceLocks.put(lock.getPath(), lock);
                return true;
            }
        }
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#move(java.lang.String, java.lang.String, boolean)
     */
    public void move(String src, String dest, boolean overwrite)
    throws CmsRepositoryItemNotFoundException, CmsRepositoryPermissionException,
    CmsRepositoryItemAlreadyExistsException {

        copy(src, dest, overwrite);
        delete(src);
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#save(java.lang.String, java.io.InputStream, boolean)
     */
    public void save(String path, InputStream inputStream, boolean overwrite)
    throws CmsRepositoryItemAlreadyExistsException, CmsRepositoryPermissionException {

        if ((path.toUpperCase().startsWith("/WEB-INF")) || (path.toUpperCase().startsWith("/META-INF"))) {
            throw new CmsRepositoryPermissionException();
        }

        try {

            boolean exists = (new File(getAbsolutePath(path))).exists();
            if ((exists) && (!overwrite)) {
                throw new CmsRepositoryItemAlreadyExistsException();
            }

            // Create file if it does not exist
            FileOutputStream out = new FileOutputStream(getAbsolutePath(path));
            out.write(CmsFileUtil.readFully(inputStream));
            out.close();

        } catch (IOException e) {
            throw new CmsRepositoryItemAlreadyExistsException();
        }

    }

    /**
     * Sets the root.<p>
     *
     * @param root the root to set
     */
    public void setRoot(String root) {

        if (!root.endsWith("/")) {
            root += "/";
        }

        m_root = root;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#unlock(java.lang.String)
     */
    public void unlock(String path) {

        // Checking resource locks
        CmsRepositoryLockInfo lock = (CmsRepositoryLockInfo)m_resourceLocks.get(path);
        Iterator iter = null;
        if (lock != null) {

            m_resourceLocks.remove(path);
        }

        // Checking inheritable collection locks
        iter = m_collectionLocks.iterator();
        while (iter.hasNext()) {
            lock = (CmsRepositoryLockInfo)iter.next();
            if (path.equals(lock.getPath())) {

                iter.remove();
            }
        }
    }

    /**
     * Deletes a collection.<p>
     *
     * @param path Path to the collection to be deleted
     * @throws CmsRepositoryItemNotFoundException if the path could not be found
     * @throws CmsRepositoryPermissionException if there is a permission issue
     */
    private void deleteCollection(String path)
    throws CmsRepositoryItemNotFoundException, CmsRepositoryPermissionException {

        if ((path.toUpperCase().startsWith("/WEB-INF")) || (path.toUpperCase().startsWith("/META-INF"))) {
            throw new CmsRepositoryPermissionException();
        }

        List list = list(path);

        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            String element = (String)iter.next();
            String childName = path;
            if (!childName.equals("/")) {
                childName += "/";
            }

            childName += element;
            delete(childName);
        }
    }

    /**
     * Creates an absolute path out of the given relative path.<p>
     * 
     * @param path The relative path
     * @return the absolute path
     */
    private String getAbsolutePath(String path) {

        if (path.startsWith("/")) {
            return m_root + path.substring(1);
        }

        return m_root + path;
    }

    /**
     * Returns the contents of the file in a byte array.<p>
     * 
     * @param file The file where to read the content
     * @return the content of the file as a byte array
     * @throws IOException if something goes wrong
     */
    private byte[] getBytesFromFile(File file) throws IOException {

        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length) {
            numRead = is.read(bytes, offset, bytes.length - offset);
            if (numRead >= 0) {
                offset += numRead;
            }
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

}

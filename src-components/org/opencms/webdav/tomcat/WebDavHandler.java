/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/webdav/tomcat/Attic/WebDavHandler.java,v $
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

package org.opencms.webdav.tomcat;

import org.opencms.main.CmsException;
import org.opencms.webdav.CmsWebdavLockException;
import org.opencms.webdav.CmsWebdavLockInfo;
import org.opencms.webdav.CmsWebdavResourceException;
import org.opencms.webdav.CmsWebdavServlet;
import org.opencms.webdav.CmsWebdavStatus;
import org.opencms.webdav.I_CmsWebdavItem;
import org.opencms.webdav.Messages;

import java.io.BufferedInputStream;
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

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;

import org.apache.catalina.Globals;
import org.apache.naming.resources.CacheEntry;
import org.apache.naming.resources.ProxyDirContext;
import org.apache.naming.resources.Resource;

/**
 * Test class to use with Tomcat and WebDAV. Not for production use.
 * Only to test the WebDav Servlet. Stores files and folder inside
 * the servlet directory.
 * 
 * @author Peter Bonrad
 */
public class WebDavHandler {

    /** JNDI resources name. */
    private static final String RESOURCES_JNDI_NAME = "java:/comp/Resources";

    /**
     * Vector of the heritable locks.
     * <p>
     * Key : path <br>
     * Value : LockInfo
     */
    private List m_collectionLocks = new Vector();

    /**
     * Repository of the lock-null resources.
     * <p>
     * Key : path of the collection containing the lock-null resource<br>
     * Value : Vector of lock-null resource which are members of the
     * collection. Each element of the Vector is the path associated with
     * the lock-null resource.
     */
    private Map m_lockNullResources = new Hashtable();

    /**
     * Repository of the locks put on single resources.
     * <p>
     * Key : path <br />
     * Value : LockInfo
     */
    private Map m_resourceLocks = new Hashtable();

    /** Proxy directory context. */
    private ProxyDirContext m_resources = null;

    /**
     * Empty default constructor.
     *
     */
    public WebDavHandler() {

        // noop
    }

    /**
     * @see org.opencms.webdav.I_CmsWebdavSession#copy(String, String, boolean, Hashtable)
     */
    public boolean copy(String source, String dest, boolean overwrite, Hashtable errorList) {

        I_CmsWebdavItem item = null;
        try {
            item = getItem(source);
        } catch (CmsException ex) {
            errorList.put(source, new Integer(CmsWebdavStatus.SC_INTERNAL_SERVER_ERROR));
            return false;
        }

        // Overwriting the destination
        boolean exists = exists(dest);
        if (overwrite) {

            // Delete destination resource, if it exists
            if (exists) {
                if (!delete(dest, errorList)) {
                    return false;
                }
            }
        }

        if (item.isCollection()) {

            try {
                create(dest);
            } catch (CmsException e) {
                errorList.put(dest, new Integer(CmsWebdavStatus.SC_CONFLICT));
                return false;
            }

            try {
                List list = list(source);
                Iterator iter = list.iterator();
                while (iter.hasNext()) {
                    String element = (String)iter.next();

                    String childDest = dest;
                    if (!childDest.equals("/")) {
                        childDest += "/";
                    }
                    childDest += element;

                    String childSrc = source;
                    if (!childSrc.equals("/")) {
                        childSrc += "/";
                    }
                    childSrc += element;

                    copy(childSrc, childDest, overwrite, errorList);
                }

            } catch (CmsException e) {
                errorList.put(dest, new Integer(CmsWebdavStatus.SC_INTERNAL_SERVER_ERROR));
                return false;
            }

        } else {

            try {
                boolean save = saveResource(dest, item);
                if (!save) {
                    errorList.put(source, new Integer(CmsWebdavStatus.SC_INTERNAL_SERVER_ERROR));
                    return false;
                }
            } catch (CmsWebdavResourceException e) {
                errorList.put(source, new Integer(CmsWebdavStatus.SC_INTERNAL_SERVER_ERROR));
                return false;
            }

        }

        return true;
    }

    /**
     * @see org.opencms.webdav.I_CmsWebdavSession#create(String)
     */
    public void create(String path) throws CmsException {

        try {
            m_resources.createSubcontext(path);
        } catch (NamingException e) {
            throw new CmsException(Messages.get().container(Messages.EXISTING_RESOURCE_1, path));
        }
    }

    /**
     * @see org.opencms.webdav.I_CmsWebdavSession#lock(String, CmsWebdavLockInfo, String)
     */
    public boolean lock(String path, CmsWebdavLockInfo lock, String lockToken, List errorLocks) throws CmsException {

        boolean exists = true;
        I_CmsWebdavItem item = null;
        try {
            item = getItem(path);
        } catch (CmsException e) {
            exists = false;
        }

        if ((exists) && (item.isCollection()) && (lock.getDepth() == CmsWebdavServlet.INFINITY)) {

            // Locking a collection (and all its member resources)

            // Checking if a child resource of this collection is already locked
            Iterator iter = m_collectionLocks.iterator();
            while (iter.hasNext()) {
                CmsWebdavLockInfo currentLock = (CmsWebdavLockInfo)iter.next();
                if (currentLock.hasExpired()) {
                    m_resourceLocks.remove(currentLock.getPath());
                    continue;
                }
                if ((currentLock.getPath().startsWith(lock.getPath()))
                    && ((currentLock.isExclusive()) || (lock.isExclusive()))) {

                    // A child collection of this collection is locked
                    errorLocks.add(currentLock.getPath());
                }
            }

            // Yikes: modifying the collection not by the iterator's object,
            // but by one of its attributes.  That means we can't use a
            // normal java.util.Iterator here, because Iterator is fail-fast. ;(
            Enumeration locksList = Collections.enumeration(m_resourceLocks.values());
            while (locksList.hasMoreElements()) {
                CmsWebdavLockInfo currentLock = (CmsWebdavLockInfo)locksList.nextElement();
                if (currentLock.hasExpired()) {
                    m_resourceLocks.remove(currentLock.getPath());
                    continue;
                }
                if ((currentLock.getPath().startsWith(lock.getPath()))
                    && ((currentLock.isExclusive()) || (lock.isExclusive()))) {

                    // A child resource of this collection is locked
                    errorLocks.add(currentLock.getPath());
                }
            }

            if (!errorLocks.isEmpty()) {
                return false;
            }

            boolean addLock = true;

            // Checking if there is already a shared lock on this path
            iter = m_collectionLocks.iterator();
            while (iter.hasNext()) {
                CmsWebdavLockInfo currentLock = (CmsWebdavLockInfo)iter.next();
                if (currentLock.getPath().equals(lock.getPath())) {
                    if (currentLock.isExclusive()) {
                        // TODO: fix that
                        //throw new CmsWebdavLockException(CmsWebdavStatus.SC_LOCKED);
                    } else {
                        if (lock.isExclusive()) {
                            // TODO: fix that
                            //throw new CmsWebdavLockException(CmsWebdavStatus.SC_LOCKED);
                        }
                    }

                    currentLock.getTokens().add(lockToken);
                    lock = currentLock;
                    addLock = false;
                }
            }

            if (addLock) {
                lock.getTokens().add(lockToken);
                m_collectionLocks.add(lock);
            }

        } else {

            // Locking a single resource

            // Retrieving an already existing lock on that resource
            CmsWebdavLockInfo presentLock = (CmsWebdavLockInfo)m_resourceLocks.get(lock.getPath());
            if (presentLock != null) {

                if ((presentLock.isExclusive()) || (lock.isExclusive())) {

                    // If either lock is exclusive, the lock can't be granted
                    // TODO: fix that
                    //throw new CmsWebdavLockException(CmsWebdavStatus.SC_PRECONDITION_FAILED);
                } else {
                    presentLock.getTokens().add(lockToken);
                    lock = presentLock;
                }

            } else {

                lock.getTokens().add(lockToken);
                m_resourceLocks.put(lock.getPath(), lock);

                if (!exists) {

                    // "Creating" a lock-null resource
                    int slash = lock.getPath().lastIndexOf('/');
                    String parentPath = lock.getPath().substring(0, slash);

                    List lockNulls = (List)m_lockNullResources.get(parentPath);
                    if (lockNulls == null) {
                        lockNulls = new Vector();
                        m_lockNullResources.put(parentPath, lockNulls);
                    }

                    lockNulls.add(lock.getPath());

                }

                return true;
            }
        }
       
        return false;
    }

    /**
     * @see org.opencms.webdav.I_CmsWebdavSession#create(String, InputStream, boolean)
     */
    public void create(String path, InputStream inputStream, boolean overwrite) 
    throws CmsException {

        try {
            if (overwrite) {
                m_resources.rebind(path, new Resource(inputStream));
            } else {
                m_resources.bind(path, new Resource(inputStream));
            }
        } catch (NamingException e) {
            throw new CmsException(Messages.get().container(Messages.EXISTING_RESOURCE_1, path));
        }
    }

    /**
     * @see org.opencms.webdav.I_CmsWebdavSession#delete(String)
     */
    public boolean delete(String path, Hashtable errorList) {

        if ((path.toUpperCase().startsWith("/WEB-INF")) || (path.toUpperCase().startsWith("/META-INF"))) {
            errorList.put(path, new Integer(CmsWebdavStatus.SC_FORBIDDEN));
            return false;
        }

        I_CmsWebdavItem item = null;
        try {
            item = getItem(path);
        } catch (CmsException e) {
            errorList.put(path, new Integer(CmsWebdavStatus.SC_INTERNAL_SERVER_ERROR));
            return false;
        }

        boolean collection = item.isCollection();
        if (!collection) {
            try {
                m_resources.unbind(path);
            } catch (NamingException  e) {
                errorList.put(path, new Integer(CmsWebdavStatus.SC_INTERNAL_SERVER_ERROR));
                return false;
            }
        } else {

            deleteCollection(path, errorList);
            delete(path, errorList);
        }
        
        return true;
    }

    /**
     * @see org.opencms.webdav.I_CmsWebdavSession#exists(String) 
     */
    public boolean exists(String path) {

        try {
            m_resources.lookup(path);
            return true;
        } catch (NamingException e) {
            return false;
        }
    }

    /**
     * @see org.opencms.webdav.I_CmsWebdavSession#getItem(String)
     */
    public I_CmsWebdavItem getItem(String path) throws CmsException {

        if (!exists(path)) {
            throw new CmsException(Messages.get().container(Messages.MISSING_RESOURCE_1, path));
        }
        WebdavItem ret = new WebdavItem();
        CacheEntry entry = m_resources.lookupCache(path);

        ret.setCollection(entry.context != null);
        ret.setContentLength(entry.attributes.getContentLength());
        ret.setCreationDate(entry.attributes.getCreation());
        ret.setLastModifiedDate(entry.attributes.getLastModified());
        ret.setMimeType(entry.attributes.getMimeType());
        ret.setName(entry.name);
        if (entry.resource != null) {
            ret.setContent(entry.resource.getContent());
        }

        return ret;
    }

    /**
     * Returns the lock for the given resource. This does not include the
     * possible locks inherited from a parent.
     * 
     * @param path the path where the resource can be found
     * @return the lock if the resource was found or null if it was not found
     */
    public CmsWebdavLockInfo getLock(String path) {

        return (CmsWebdavLockInfo)m_resourceLocks.get(path);
    }

    /**
     * Returns all found locks for the resource found at the given path. This
     * includes all locks inherited by a parent.
     * 
     * @param path the path where to find the resource
     * @return all found locks on this resource
     */
    public List getLocks(String path) {

        ArrayList ret = new ArrayList();

        CmsWebdavLockInfo resourceLock = (CmsWebdavLockInfo)m_resourceLocks.get(path);
        if (resourceLock != null) {
            ret.add(resourceLock);
        }

        Iterator iter = m_collectionLocks.iterator();
        while (iter.hasNext()) {
            CmsWebdavLockInfo currentLock = (CmsWebdavLockInfo)iter.next();
            if (path.startsWith(currentLock.getPath())) {
                ret.add(currentLock);
            }
        }

        return ret;
    }

    /**
     * Returns a resource for the given path.
     * 
     * @param path the path where to find the resource
     * @return the found resource for the given path
     * @throws CmsWebdavResourceException if the resource could not be found
     */
    public Object getResource(String path) throws CmsWebdavResourceException {

        Object object = null;
        try {
            object = m_resources.lookup(path);
        } catch (NamingException e) {
            throw new CmsWebdavResourceException("Resource at path \"" + path + "\" not found.");
        }
        return object;
    }

    /**
     * Returns the content of the resource found at the given path as an input stream.
     * 
     * @param path the path where to find the resource
     * @return the content of the resource as an input stream
     * @throws CmsWebdavResourceException if the resource could not be found
     * @throws IOException if an error while reading the content occurs
     */
    public InputStream getStreamContent(String path) throws CmsWebdavResourceException, IOException {

        Resource oldResource = null;
        Object obj = getResource(path);
        if (obj instanceof Resource) {
            oldResource = (Resource)obj;
        }

        // Copy data in oldRevisionContent to contentFile
        if (oldResource != null) {
            return new BufferedInputStream(oldResource.streamContent(), CmsWebdavServlet.BUFFER_SIZE);
        }

        return null;
    }

    /**
     * Initialize this servlet.
     * 
     * @param servletContext The actual servlet context
     * @throws ServletException if a servlet-specified error occurs
     */
    public void init(ServletContext servletContext) throws ServletException {

        // Set our properties from the initialization parameters
        //        String value = null;
        //        try {
        //            value = servlet.getServletConfig().getInitParameter("secret");
        //            if (value != null) {
        //                secret = value;
        //            }
        //        } catch (Exception e) {
        //            servlet.log("WebdavServlet.init: error reading secret from " + value);
        //        }

        // Load the proxy dir context.
        try {
            m_resources = (ProxyDirContext)servletContext.getAttribute(Globals.RESOURCES_ATTR);
        } catch (ClassCastException e) {
            // Failed : Not the right type
        }

        if (m_resources == null) {
            try {
                m_resources = (ProxyDirContext)new InitialContext().lookup(RESOURCES_JNDI_NAME);
            } catch (NamingException e) {
                // Failed
            } catch (ClassCastException e) {
                // Failed : Not the right type
            }
        }

        if (m_resources == null) {
            throw new UnavailableException("No resources");
        }

    }

    /**
     * Returns if the resource is a collection.
     * 
     * @param object the resource to check
     * @return true if the resource is a collection otherwise false
     */
    public boolean isCollection(Object object) {

        return object instanceof DirContext;
    }

    /**
     * @see org.opencms.webdav.I_CmsWebdavSession#isLocked(String, String)
     */
    public boolean isLocked(String path, String lockTokens) {

        // Checking resource locks
        CmsWebdavLockInfo lock = (CmsWebdavLockInfo)m_resourceLocks.get(path);
        if ((lock != null) && (lock.hasExpired())) {
            m_resourceLocks.remove(path);
        } else if (lock != null) {

            // At least one of the tokens of the locks must have been given
            Iterator iter = lock.getTokens().iterator();
            boolean tokenMatch = false;
            while (iter.hasNext()) {
                String token = (String)iter.next();
                if (lockTokens.indexOf(token) != -1) {
                    tokenMatch = true;
                }
            }

            if (!tokenMatch) {
                return true;
            }
        }

        // Checking inheritable collection locks
        Iterator iter = m_collectionLocks.iterator();
        while (iter.hasNext()) {
            lock = (CmsWebdavLockInfo)iter.next();
            if (lock.hasExpired()) {
                iter.remove();
            } else if (path.startsWith(lock.getPath())) {
                Iterator tokenIter = lock.getTokens().iterator();
                boolean tokenMatch = false;
                while (tokenIter.hasNext()) {
                    String token = (String)tokenIter.next();
                    if (lockTokens.indexOf(token) != -1) {
                        tokenMatch = true;
                    }
                }

                if (!tokenMatch) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @see org.opencms.webdav.I_CmsWebdavSession#list(String)
     */
    public List list(String path) throws CmsException {

        List ret = new ArrayList();

        try {
            NamingEnumeration enumeration = m_resources.list(path);
            while (enumeration.hasMoreElements()) {
                NameClassPair ncPair = (NameClassPair)enumeration.nextElement();
                ret.add(ncPair.getName());
            }
        } catch (NamingException e) {
            throw new CmsException(Messages.get().container(Messages.MISSING_RESOURCE_1, path));
        }

        return ret;
    }

    /**
     * Returns a cache entry of the resource at the given path.
     * Has to be replaced later.
     * 
     * @param path The path where to find the resource at
     * @return The cache entry with all information of the resource
     */
    public CacheEntry lookupCache(String path) {

        return m_resources.lookupCache(path);
    }

    /**
     * Renews an existing lock. Extends the time the lock expires.
     * 
     * @param lock all information about the new lock
     * @param path the path where to find the resource to renew the lock
     * @param ifHeader the ifHeader found in the request
     */
    public void renewLock(CmsWebdavLockInfo lock, String path, String ifHeader) {

        // Checking resource locks
        CmsWebdavLockInfo toRenew = (CmsWebdavLockInfo)m_resourceLocks.get(path);
        if (lock != null) {

            // At least one of the tokens of the locks must have been given
            Iterator iter = toRenew.getTokens().iterator();
            while (iter.hasNext()) {
                String token = (String)iter.next();
                if (ifHeader.indexOf(token) != -1) {
                    toRenew.setExpiresAt(lock.getExpiresAt());
                    lock = toRenew;
                }
            }

        }

        // Checking inheritable collection locks
        Iterator iter = m_collectionLocks.iterator();
        while (iter.hasNext()) {
            toRenew = (CmsWebdavLockInfo)iter.next();
            if (path.equals(toRenew.getPath())) {
                Iterator tokenIter = toRenew.getTokens().iterator();
                while (tokenIter.hasNext()) {
                    String token = (String)tokenIter.next();
                    if (ifHeader.indexOf(token) != -1) {
                        toRenew.setExpiresAt(lock.getExpiresAt());
                        lock = toRenew;
                    }
                }
            }
        }

    }

    /**
     * @see org.opencms.webdav.I_CmsWebdavSession#unlock(String, String)
     */
    public void unlock(String path, String lockTokens) {

        // Checking resource locks
        CmsWebdavLockInfo lock = (CmsWebdavLockInfo)m_resourceLocks.get(path);
        Iterator iter = null;
        if (lock != null) {

            // At least one of the tokens of the locks must have been given
            iter = lock.getTokens().iterator();
            while (iter.hasNext()) {
                String token = (String)iter.next();
                if (lockTokens.indexOf(token) != -1) {
                    iter.remove();
                }
            }

            if (lock.getTokens().isEmpty()) {
                m_resourceLocks.remove(path);

                // Removing any lock-null resource which would be present
                m_lockNullResources.remove(path);
            }
        }

        // Checking inheritable collection locks
        iter = m_collectionLocks.iterator();
        while (iter.hasNext()) {
            lock = (CmsWebdavLockInfo)iter.next();
            if (path.equals(lock.getPath())) {
                Iterator tokenIter = lock.getTokens().iterator();
                while (tokenIter.hasNext()) {
                    String token = (String)tokenIter.next();
                    if (lockTokens.indexOf(token) != -1) {
                        tokenIter.remove();
                        break;
                    }
                }

                if (lock.getTokens().isEmpty()) {
                    iter.remove();

                    // Removing any lock-null resource which would be present
                    m_lockNullResources.remove(path);
                }
            }
        }
    }

    /**
     * Deletes a collection.
     *
     * @param resources Resources implementation associated with the context
     * @param path Path to the collection to be deleted
     * @param errorList Contains the list of the errors which occurred
     */
    private void deleteCollection(String path, Hashtable errorList) {

        if ((path.toUpperCase().startsWith("/WEB-INF")) || (path.toUpperCase().startsWith("/META-INF"))) {
            errorList.put(path, new Integer(CmsWebdavStatus.SC_FORBIDDEN));
            return;
        }

        List list = null;
        try {
            list = list(path);
        } catch (CmsException e) {
            errorList.put(path, new Integer(CmsWebdavStatus.SC_INTERNAL_SERVER_ERROR));
            return;
        }

        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            String element = (String)iter.next();
            String childName = path;
            if (!childName.equals("/")) {
                childName += "/";
            }

            childName += element;
            delete(childName, errorList);
        }
    }

    /**
     * Saves an existing resource to the specified path (not a collection).
     * 
     * @param dest the destination path where to save the resource
     * @param object the resource to save
     * @return true if the resource was saved successfully otherwise false
     * @throws CmsWebdavResourceException if the path where to save the resource already exists
     */
    private boolean saveResource(String dest, Object object) throws CmsWebdavResourceException {

        if (object instanceof Resource) {
            try {
                m_resources.bind(dest, object);
                return true;
            } catch (NamingException e) {
                throw new CmsWebdavResourceException("Resource at path \"" + dest + "\" already exists.");
            }
        }

        return false;
    }
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/webdav/tomcat/Attic/WebDavHandler.java,v $
 * Date   : $Date: 2007/01/23 16:58:11 $
 * Version: $Revision: 1.1.2.2 $
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

import org.opencms.webdav.CmsWebdavItemAlreadyExistsException;
import org.opencms.webdav.CmsWebdavItemNotFoundException;
import org.opencms.webdav.CmsWebdavLockInfo;
import org.opencms.webdav.CmsWebdavPermissionException;
import org.opencms.webdav.CmsWebdavStatus;
import org.opencms.webdav.I_CmsWebdavItem;
import org.opencms.webdav.I_CmsWebdavSession;

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
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;

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
public class WebDavHandler implements I_CmsWebdavSession {

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
    public void copy(String source, String dest, boolean overwrite)
    throws CmsWebdavItemNotFoundException, CmsWebdavPermissionException, CmsWebdavItemAlreadyExistsException {

        if ((dest.toUpperCase().startsWith("/WEB-INF")) || (dest.toUpperCase().startsWith("/META-INF"))) {
            throw new CmsWebdavPermissionException();
        }

        I_CmsWebdavItem item = getItem(source);

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

                copy(childSrc, childDest, overwrite);
            }

        } else {

            try {
                saveResource(dest, item);
            } catch (NamingException e) {
                throw new CmsWebdavItemAlreadyExistsException();
            }

        }
    }

    /**
     * @see org.opencms.webdav.I_CmsWebdavSession#create(String)
     */
    public void create(String path) throws CmsWebdavItemAlreadyExistsException, CmsWebdavPermissionException {

        if ((path.toUpperCase().startsWith("/WEB-INF")) || (path.toUpperCase().startsWith("/META-INF"))) {
            throw new CmsWebdavPermissionException();
        }

        try {
            m_resources.createSubcontext(path);
        } catch (NamingException e) {
            throw new CmsWebdavItemAlreadyExistsException();
        }
    }

    /**
     * @see org.opencms.webdav.I_CmsWebdavSession#create(String, InputStream, boolean)
     */
    public void create(String path, InputStream inputStream, boolean overwrite)
    throws CmsWebdavItemAlreadyExistsException, CmsWebdavPermissionException {

        if ((path.toUpperCase().startsWith("/WEB-INF")) || (path.toUpperCase().startsWith("/META-INF"))) {
            throw new CmsWebdavPermissionException();
        }

        try {
            if (overwrite) {
                m_resources.rebind(path, new Resource(inputStream));
            } else {
                m_resources.bind(path, new Resource(inputStream));
            }
        } catch (NamingException e) {
            throw new CmsWebdavItemAlreadyExistsException();
        }
    }

    /**
     * @see org.opencms.webdav.I_CmsWebdavSession#delete(String)
     */
    public void delete(String path) throws CmsWebdavItemNotFoundException, CmsWebdavPermissionException {

        I_CmsWebdavItem item = getItem(path);

        boolean collection = item.isCollection();
        if (collection) {
            deleteCollection(path);
        }

        try {
            m_resources.unbind(path);
        } catch (NamingException e) {
            throw new CmsWebdavItemNotFoundException();
        }
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
    public I_CmsWebdavItem getItem(String path) throws CmsWebdavItemNotFoundException, CmsWebdavPermissionException {

        if ((path.toUpperCase().startsWith("/WEB-INF")) || (path.toUpperCase().startsWith("/META-INF"))) {
            throw new CmsWebdavPermissionException();
        }

        if (!exists(path)) {
            throw new CmsWebdavItemNotFoundException();
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
     * @see org.opencms.webdav.I_CmsWebdavSession#getLock(String)
     */
    public CmsWebdavLockInfo getLock(String path) {

        CmsWebdavLockInfo resourceLock = (CmsWebdavLockInfo)m_resourceLocks.get(path);
        if (resourceLock != null) {
            return resourceLock;
        }

        Iterator iter = m_collectionLocks.iterator();
        while (iter.hasNext()) {
            CmsWebdavLockInfo currentLock = (CmsWebdavLockInfo)iter.next();
            if (path.startsWith(currentLock.getPath())) {
                return currentLock;
            }
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

        // Load the proxy dir context.
        try {
            m_resources = (ProxyDirContext)servletContext.getAttribute("org.apache.catalina.resources");
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
     * @see org.opencms.webdav.I_CmsWebdavSession#list(String)
     */
    public List list(String path) throws CmsWebdavItemNotFoundException, CmsWebdavPermissionException {

        if ((path.toUpperCase().startsWith("/WEB-INF")) || (path.toUpperCase().startsWith("/META-INF"))) {
            throw new CmsWebdavPermissionException();
        }

        List ret = new ArrayList();

        try {
            NamingEnumeration enumeration = m_resources.list(path);
            while (enumeration.hasMoreElements()) {
                NameClassPair ncPair = (NameClassPair)enumeration.nextElement();
                ret.add(ncPair.getName());
            }
        } catch (NamingException e) {
            throw new CmsWebdavItemNotFoundException();
        }

        return ret;
    }

    /**
     * @see org.opencms.webdav.I_CmsWebdavSession#lock(String, CmsWebdavLockInfo)
     */
    public boolean lock(String path, CmsWebdavLockInfo lock)
    throws CmsWebdavItemNotFoundException, CmsWebdavPermissionException {

        I_CmsWebdavItem item = getItem(path);

        if ((item.isCollection()) && (lock.getDepth() == CmsWebdavLockInfo.DEPTH_INFINITY)) {

            // Locking a collection (and all its member resources)
            // Checking if a child resource of this collection is already locked
            Iterator iter = m_collectionLocks.iterator();
            while (iter.hasNext()) {
                CmsWebdavLockInfo currentLock = (CmsWebdavLockInfo)iter.next();
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
                CmsWebdavLockInfo currentLock = (CmsWebdavLockInfo)locksList.nextElement();
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
            CmsWebdavLockInfo presentLock = (CmsWebdavLockInfo)m_resourceLocks.get(lock.getPath());
            if (presentLock != null) {

                return false;

            } else {

                m_resourceLocks.put(lock.getPath(), lock);
                return true;
            }
        }
    }

    /**
     * @see org.opencms.webdav.I_CmsWebdavSession#move(java.lang.String, java.lang.String, boolean)
     */
    public void move(String src, String dest, boolean overwrite)
    throws CmsWebdavItemNotFoundException, CmsWebdavPermissionException, CmsWebdavItemAlreadyExistsException {

        copy(src, dest, overwrite);
        delete(src);
    }

    /**
     * @see org.opencms.webdav.I_CmsWebdavSession#unlock(String)
     */
    public void unlock(String path) {

        // Checking resource locks
        CmsWebdavLockInfo lock = (CmsWebdavLockInfo)m_resourceLocks.get(path);
        Iterator iter = null;
        if (lock != null) {

            if (lock.getTokens().isEmpty()) {
                m_resourceLocks.remove(path);
            }
        }

        // Checking inheritable collection locks
        iter = m_collectionLocks.iterator();
        while (iter.hasNext()) {
            lock = (CmsWebdavLockInfo)iter.next();
            if (path.equals(lock.getPath())) {

                if (lock.getTokens().isEmpty()) {
                    iter.remove();
                }
            }
        }
    }

    /**
     * Deletes a collection.
     *
     * @param resources Resources implementation associated with the context
     * @param path Path to the collection to be deleted
     */
    private int deleteCollection(String path) throws CmsWebdavItemNotFoundException, CmsWebdavPermissionException {

        if ((path.toUpperCase().startsWith("/WEB-INF")) || (path.toUpperCase().startsWith("/META-INF"))) {
            return CmsWebdavStatus.SC_FORBIDDEN;
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

        return CmsWebdavStatus.SC_OK;
    }

    /**
     * Saves an existing resource to the specified path (not a collection).
     * 
     * @param dest the destination path where to save the resource
     * @param object the resource to save
     * @throws NamingException if the path where to save the resource already exists
     */
    private void saveResource(String dest, I_CmsWebdavItem item) throws NamingException {

        Resource res = (Resource)m_resources.lookup(item.getName());
        res.setContent(item.getStreamContent());
        m_resources.bind(dest, res);
    }
}

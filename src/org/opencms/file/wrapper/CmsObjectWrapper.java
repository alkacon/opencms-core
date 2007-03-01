/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/wrapper/CmsObjectWrapper.java,v $
 * Date   : $Date: 2007/03/01 16:58:52 $
 * Version: $Revision: 1.4 $
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

package org.opencms.file.wrapper;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.loader.CmsResourceManager;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Depending on the configured resource type wrappers this class handles the
 * resources to behave different.<p>
 * 
 * Acts mostly like a wrapper for the CmsObject.<p>
 *
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 6.2.4
 */
public class CmsObjectWrapper {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsObjectWrapper.class);
    
    /** The name of the attribute in the {@link org.opencms.file.CmsRequestContext}. */
    public static final String ATTRIBUTE_NAME = "org.opencms.file.wrapper.CmsObjectWrapper";

    /** The initialized CmsObject. */
    private CmsObject m_cms;

    /** The list with the configured wrappers. */
    private List m_wrappers;

    /**
     * Constructor with the CmsObject to wrap.<p>
     * 
     * @param cms the initialized CmsObject
     * @param wrappers the configured wrappers to use
     */
    public CmsObjectWrapper(CmsObject cms, List wrappers) {

        m_cms = cms;
        m_wrappers = wrappers;
    }

    /**
     * Copies a resource.<p>
     * 
     * The copied resource will always be locked to the current user
     * after the copy operation.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the copy operation.<br>
     * Possible values for this parameter are: <br>
     * <ul>
     * <li><code>{@link CmsResource#COPY_AS_NEW}</code></li>
     * <li><code>{@link CmsResource#COPY_AS_SIBLING}</code></li>
     * <li><code>{@link CmsResource#COPY_PRESERVE_SIBLING}</code></li>
     * </ul><p>
     * 
     * @param source the name of the resource to copy (full path)
     * @param destination the name of the copy destination (full path)
     * @param siblingMode indicates how to handle siblings during copy
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the <code>destination</code> argument is null or of length 0
     */
    public void copyResource(String source, String destination, int siblingMode)
    throws CmsException, CmsIllegalArgumentException {

        boolean exec = false;

        // iterate through all wrappers and call "copyResource" till one does not return null
        List wrappers = getWrappers();
        Iterator iter = wrappers.iterator();
        while (iter.hasNext()) {
            I_CmsResourceWrapper wrapper = (I_CmsResourceWrapper)iter.next();
            exec = wrapper.copyResource(m_cms, source, destination, siblingMode);
            if (exec) {
                break;
            }
        }

        // delegate the call to the CmsObject
        if (!exec) {
            m_cms.copyResource(source, destination, siblingMode);
        }

    }

    /**
     * Creates a new resource of the given resource type with 
     * empty content and no properties.<p>
     * 
     * @param resourcename the name of the resource to create (full path)
     * @param type the type of the resource to create
     * 
     * @return the created resource
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the given <code>resourcename</code> is null or of length 0
     * 
     * @see org.opencms.file.CmsObject#createResource(String, int, byte[], List)
     */
    public CmsResource createResource(String resourcename, int type) throws CmsException, CmsIllegalArgumentException {

        return createResource(resourcename, type, new byte[0], Collections.EMPTY_LIST);
    }

    /**
     * Creates a new resource of the given resource type
     * with the provided content and properties.<p>
     * 
     * @param resourcename the name of the resource to create (full path)
     * @param type the type of the resource to create
     * @param content the contents for the new resource
     * @param properties the properties for the new resource
     * 
     * @return the created resource
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the <code>resourcename</code> argument is null or of length 0
     */
    public CmsResource createResource(String resourcename, int type, byte[] content, List properties)
    throws CmsException, CmsIllegalArgumentException {

        CmsResource res = null;

        // iterate through all wrappers and call "createResource" till one does not return null
        List wrappers = getWrappers();
        Iterator iter = wrappers.iterator();
        while (iter.hasNext()) {
            I_CmsResourceWrapper wrapper = (I_CmsResourceWrapper)iter.next();
            res = wrapper.createResource(m_cms, resourcename, type, content, properties);
            if (res != null) {
                break;
            }
        }

        // delegate the call to the CmsObject
        if (res == null) {
            res = m_cms.createResource(resourcename, type, content, properties);
        }

        return res;
    }

    /**
     * Deletes a resource given its name.<p>
     * 
     * The <code>siblingMode</code> parameter controls how to handle siblings 
     * during the delete operation.<br>
     * Possible values for this parameter are: <br>
     * <ul>
     * <li><code>{@link CmsResource#DELETE_REMOVE_SIBLINGS}</code></li>
     * <li><code>{@link CmsResource#DELETE_PRESERVE_SIBLINGS}</code></li>
     * </ul><p>
     * 
     * @param resourcename the name of the resource to delete (full path)
     * @param siblingMode indicates how to handle siblings of the deleted resource
     *
     * @throws CmsException if something goes wrong
     */
    public void deleteResource(String resourcename, int siblingMode) throws CmsException {

        boolean exec = false;

        // iterate through all wrappers and call "deleteResource" till one does not return false
        List wrappers = getWrappers();
        Iterator iter = wrappers.iterator();
        while (iter.hasNext()) {
            I_CmsResourceWrapper wrapper = (I_CmsResourceWrapper)iter.next();
            exec = wrapper.deleteResource(m_cms, resourcename, siblingMode);
            if (exec) {
                break;
            }
        }

        // delegate the call to the CmsObject
        if (!exec) {
            m_cms.deleteResource(resourcename, siblingMode);
        }
    }

    /**
     * Checks the availability of a resource in the VFS,
     * using the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p>
     * 
     * @see CmsObject#existsResource(String)
     *
     * Adds additional functionality for virtual resourcenames created by
     * the wrapper. For example: xmlpages are now folders with resources.
     * The resources belonging to the folder of the xmlpage exists too.
     * 
     * @param resourcename the name of the resource to check (full path)
     * @return <code>true</code> if the resource is available
     */
    public boolean existsResource(String resourcename) {

        // first try to find the resource
        boolean ret = m_cms.existsResource(resourcename);

        // if not exists, ask the resource type wrappers
        if (!ret) {

            List wrappers = getWrappers();
            Iterator iter = wrappers.iterator();
            while (iter.hasNext()) {
                I_CmsResourceWrapper wrapper = (I_CmsResourceWrapper)iter.next();
                try {
                    CmsResource res = wrapper.readResource(m_cms, resourcename, CmsResourceFilter.DEFAULT);
                    if (res != null) {
                        ret = true;
                        break;
                    }
                } catch (CmsException ex) {
                    // noop
                }
            }

        }

        return ret;
    }

    /**
     * Calls the wrapper for the resource or delegate it to the {@link org.opencms.file.CmsObject#getLock(CmsResource)}.<p>
     * 
     * @param resource the resource to return the edition lock state for
     * 
     * @return the edition lock state for the specified resource
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsLock getLock(CmsResource resource) throws CmsException {

        CmsLock lock = null;

        // iterate through all wrappers and call "getLock" till one does not return null
        List wrappers = getWrappers();
        Iterator iter = wrappers.iterator();
        while (iter.hasNext()) {
            I_CmsResourceWrapper wrapper = (I_CmsResourceWrapper)iter.next();
            lock = wrapper.getLock(m_cms, resource);
            if (lock != null) {
                break;
            }
        }

        // delegate the call to the CmsObject
        if (lock == null) {
            lock = m_cms.getLock(resource);
        }

        return lock;
    }

    /**
     * Delegate method for the CmsObject.<p>
     * 
     * @see org.opencms.file.CmsObject#getRequestContext()

     * @return the current users request context
     */
    public CmsRequestContext getRequestContext() {

        return m_cms.getRequestContext();
    }

    /**
     * Returns all child resources of a resource, that is the resources
     * contained in a folder.<p>
     * 
     * With the <code>{@link CmsResourceFilter}</code> provided as parameter
     * you can control if you want to include deleted, invisible or 
     * time-invalid resources in the result.<p>
     * 
     * @param resourcename the full path of the resource to return the child resources for
     * @param filter the resource filter to use
     * 
     * @return a list of all child <code>{@link CmsResource}</code>s
     * 
     * @throws CmsException if something goes wrong
     */
    public List getResourcesInFolder(String resourcename, CmsResourceFilter filter) throws CmsException {

        List list = new ArrayList();

        // read children existing in the VFS
        try {
            list.addAll(m_cms.getResourcesInFolder(resourcename, filter));
        } catch (CmsException ex) {
            //noop
        }

        // iterate through all wrappers and call "getResourcesInFolder" and add the results to the list
        List wrappers = getWrappers();
        Iterator iter = wrappers.iterator();
        while (iter.hasNext()) {
            I_CmsResourceWrapper wrapper = (I_CmsResourceWrapper)iter.next();
            List added = wrapper.getResourcesInFolder(m_cms, resourcename, filter);
            if (added != null) {
                list.addAll(added);
            }
        }

        // create a new list to add all resources
        ArrayList wrapped = new ArrayList();

        // eventually wrap the found resources
        iter = list.iterator();
        while (iter.hasNext()) {
            CmsResource res = (CmsResource)iter.next();

            // get resource type wrapper for the resource
            I_CmsResourceWrapper resWrapper = getResourceTypeWrapper(res);

            if (resWrapper != null) {

                // adds the wrapped resources
                wrapped.add(resWrapper.wrapResource(m_cms, res));
            } else {

                // add the resource unwrapped
                wrapped.add(res);
            }
        }

        // sort the wrapped list correctly
        Collections.sort(wrapped, CmsResource.COMPARE_ROOT_PATH_IGNORE_CASE_FOLDERS_FIRST);

        return wrapped;
    }

    /**
     * Delegate method for the CmsObject.<p>
     * 
     * @see org.opencms.file.CmsObject#getSitePath(org.opencms.file.CmsResource)
     * 
     * @param resource the resource to get the adjusted site root path for
     * @return the absolute resource path adjusted for the current site
     */
    public String getSitePath(CmsResource resource) {

        return m_cms.getSitePath(resource);
    }

    /**
     * Returns the wrappers.<p>
     *
     * @return the wrappers
     */
    public List getWrappers() {

        return m_wrappers;
    }

    /**
     * Locks a resource.<p>
     *
     * This will be an exclusive, persistant lock that is removed only if the user unlocks it.<p>
     *
     * @param resourcename the name of the resource to lock (full path)
     * 
     * @throws CmsException if something goes wrong
     */
    public void lockResource(String resourcename) throws CmsException {

        boolean exec = false;

        // iterate through all wrappers and call "lockResource" till one does not return false
        List wrappers = getWrappers();
        Iterator iter = wrappers.iterator();
        while (iter.hasNext()) {
            I_CmsResourceWrapper wrapper = (I_CmsResourceWrapper)iter.next();
            exec = wrapper.lockResource(m_cms, resourcename);
            if (exec) {
                break;
            }
        }

        // delegate the call to the CmsObject
        if (!exec) {
            m_cms.lockResource(resourcename);
        }
    }

    /**
     * Moves a resource to the given destination.<p>
     * 
     * A move operation in OpenCms is always a copy (as sibling) followed by a delete,
     * this is a result of the online/offline structure of the 
     * OpenCms VFS. This way you can see the deleted files/folders in the offline
     * project, and you will be unable to undelete them.<p>
     * 
     * @param source the name of the resource to move (full path)
     * @param destination the destination resource name (full path)
     *
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.file.CmsObject#renameResource(String, String)
     */
    public void moveResource(String source, String destination) throws CmsException {

        boolean exec = false;

        // iterate through all wrappers and call "moveResource" till one does not return false
        List wrappers = getWrappers();
        Iterator iter = wrappers.iterator();
        while (iter.hasNext()) {
            I_CmsResourceWrapper wrapper = (I_CmsResourceWrapper)iter.next();
            exec = wrapper.moveResource(m_cms, source, destination);
            if (exec) {
                break;
            }
        }

        // delegate the call to the CmsObject
        if (!exec) {
            m_cms.moveResource(source, destination);
        }
    }

    /**
     * Reads a file resource (including it's binary content) from the VFS,
     * using the specified resource filter.<p>
     * 
     * If the resource with the resource name can't be found by the CmsObject,
     * all configured resource type wrappers are asked to handle this action.
     * 
     * @see org.opencms.file.CmsObject#readFile(java.lang.String, org.opencms.file.CmsResourceFilter)
     * 
     * @param resourcename the name of the resource to read (full path)
     * @param filter the resource filter to use while reading
     * @return the file resource that was read
     *
     * @throws CmsException if the file resource could not be read for any reason
     */
    public CmsFile readFile(String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsFile res = null;

        // iterate through all wrappers and call "readFile" till one does not return null
        List wrappers = getWrappers();
        Iterator iter = wrappers.iterator();
        while (iter.hasNext()) {
            I_CmsResourceWrapper wrapper = (I_CmsResourceWrapper)iter.next();
            res = wrapper.readFile(m_cms, resourcename, filter);
            if (res != null) {
                break;
            }
        }

        // delegate the call to the CmsObject
        if (res == null) {
            res = m_cms.readFile(resourcename, filter);
        }

        // for text based resources which are encoded in UTF-8 add the UTF-marker at the start
        // of the content
        String encoding = CmsLocaleManager.getResourceEncoding(m_cms, res);
        if (CmsEncoder.ENCODING_UTF_8.equals(encoding)) {
            String contentType = OpenCms.getResourceManager().getMimeType(
                res.getRootPath(),
                encoding,
                CmsResourceManager.MIMETYPE_TEXT);
            
            if ((contentType != null) && (contentType.startsWith("text"))) {
                
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_ADD_UTF8_MARKER_1, res.getRootPath()));
                }
                
                res.setContents(CmsWrappedResource.addUtf8Marker(res.getContents()));
            }
        }

        return res;
    }

    /**
     * Delegate method for the CmsObject.<p>
     * 
     * @param resource the resource where the property is attached to
     * @param property the property name
     * @param search if true, the property is searched on all parent folders of the resource, 
     *      if it's not found attached directly to the resource
     * 
     * @return the required property, or <code>{@link CmsProperty#getNullProperty()}</code> if the property was not found
     * 
     * @throws CmsException if something goes wrong
     * 
     * @see org.opencms.file.CmsObject#readPropertyObject(org.opencms.file.CmsResource, java.lang.String, boolean)
     */
    public CmsProperty readPropertyObject(CmsResource resource, String property, boolean search) throws CmsException {

        return m_cms.readPropertyObject(resource, property, search);
    }

    /**
     * Reads a resource from the VFS,
     * using the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p> 
     * 
     * If the resource with the resource name can't be found by the CmsObject,
     * all configured resource type wrappers are asked to handle this action.
     * 
     * @param resourcename The name of the resource to read (full path)
     * @param filter the resource filter to use while reading
     * 
     * @return the resource that was read
     * 
     * @throws CmsException if the resource could not be read for any reason
     * 
     * @see CmsObject#readResource(String)
     */
    public CmsResource readResource(String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource res = null;

        // iterate through all wrappers and call "readResource" till one does not return null
        List wrappers = getWrappers();
        Iterator iter = wrappers.iterator();
        while (iter.hasNext()) {
            I_CmsResourceWrapper wrapper = (I_CmsResourceWrapper)iter.next();
            res = wrapper.readResource(m_cms, resourcename, filter);
            if (res != null) {
                break;
            }
        }

        // delegate the call to the CmsObject
        if (res == null) {
            res = m_cms.readResource(resourcename, filter);
        }

        return res;
    }

    /**
     * Delegate method for the CmsObject.<p>
     * 
     * @see org.opencms.file.CmsObject#readUser(org.opencms.util.CmsUUID)
     * 
     * @param userId the id of the user to be read
     * 
     * @return the user with the given id
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsUser readUser(CmsUUID userId) throws CmsException {

        return m_cms.readUser(userId);
    }

    /**
     * Restores the uri for the resource at the rewritten path.<p>
     * 
     * @param path the path where to find the resource
     * 
     * @return the restored path for the resource
     */
    public String restoreLink(String path) {

        String ret = null;

        // iterate through all wrappers and call "restoreLink" till one does not return null
        List wrappers = getWrappers();
        Iterator iter = wrappers.iterator();
        while (iter.hasNext()) {
            I_CmsResourceWrapper wrapper = (I_CmsResourceWrapper)iter.next();
            ret = wrapper.restoreLink(m_cms, m_cms.getRequestContext().removeSiteRoot(path));
            if (ret != null) {
                return ret;
            }
        }

        return path;
    }

    /**
     * Rewrite the link for the resource at the path.<p>
     * 
     * Used for the link processing ({@link org.opencms.staticexport.CmsLinkProcessor}).<p>
     * 
     * @param path the full path where to find the resource
     * 
     * @return the rewritten link for the resource
     */
    public String rewriteLink(String path) {

        CmsResource res = null;

        try {
            res = readResource(m_cms.getRequestContext().removeSiteRoot(path), CmsResourceFilter.ALL);
            if (res != null) {
                String ret = null;

                // iterate through all wrappers and call "rewriteLink" till one does not return null
                List wrappers = getWrappers();
                Iterator iter = wrappers.iterator();
                while (iter.hasNext()) {
                    I_CmsResourceWrapper wrapper = (I_CmsResourceWrapper)iter.next();
                    ret = wrapper.rewriteLink(m_cms, res);
                    if (ret != null) {
                        return ret;
                    }
                }
            }
        } catch (CmsException ex) {
            // noop
        }

        return path;
    }

    /**
     * Unlocks a resource.<p>
     * 
     * @param resourcename the name of the resource to unlock (full path)
     * 
     * @throws CmsException if something goes wrong
     */
    public void unlockResource(String resourcename) throws CmsException {

        boolean exec = false;

        // iterate through all wrappers and call "lockResource" till one does not return false
        List wrappers = getWrappers();
        Iterator iter = wrappers.iterator();
        while (iter.hasNext()) {
            I_CmsResourceWrapper wrapper = (I_CmsResourceWrapper)iter.next();
            exec = wrapper.unlockResource(m_cms, resourcename);
            if (exec) {
                break;
            }
        }

        // delegate the call to the CmsObject
        if (!exec) {
            m_cms.unlockResource(resourcename);
        }
    }

    /**
     * Writes a resource to the OpenCms VFS, including it's content.<p>
     * 
     * If the file does not exist in the VFS, the responsible wrapper is
     * called to save the resource.
     * 
     * @see CmsObject#writeFile(CmsFile)
     * 
     * @param resource the resource to write
     * 
     * @return the written resource (may have been modified)
     *
     * @throws CmsException if something goes wrong
     */
    public CmsFile writeFile(CmsFile resource) throws CmsException {

        CmsFile res = null;

        String resourcename = resource.getRootPath();
        if (!m_cms.existsResource(resourcename)) {

            // iterate through all wrappers and call "readResource" till one does not return null
            List wrappers = getWrappers();
            Iterator iter = wrappers.iterator();
            while (iter.hasNext()) {
                I_CmsResourceWrapper wrapper = (I_CmsResourceWrapper)iter.next();
                res = wrapper.writeFile(m_cms, resource);
                if (res != null) {
                    break;
                }
            }

            // delegate the call to the CmsObject
            if (res == null) {
                res = m_cms.writeFile(resource);
            }
        } else {
            res = m_cms.writeFile(resource);
        }

        return res;
    }

    /**
     * Try to find a resource type wrapper for the resource.<p>
     * 
     * Takes all configured resource type wrappers and ask if one of them is responsible 
     * for that resource. The first in the list which feels responsible is returned. 
     * If no wrapper could be found null will be returned.<p>
     * 
     * @param res the resource to find a resource type wrapper for
     * 
     * @return the found resource type wrapper for the resource or null if not found
     */
    private I_CmsResourceWrapper getResourceTypeWrapper(CmsResource res) {

        Iterator iter = getWrappers().iterator();
        while (iter.hasNext()) {
            I_CmsResourceWrapper wrapper = (I_CmsResourceWrapper)iter.next();

            if (wrapper.isWrappedResource(m_cms, res)) {
                return wrapper;
            }
        }

        return null;
    }

}

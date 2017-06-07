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

package org.opencms.file.wrapper;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResource.CmsResourceCopyMode;
import org.opencms.file.CmsResource.CmsResourceDeleteMode;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.I_CmsResource;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.loader.CmsLoaderException;
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
 * This class contains a subset of the methods of {@link CmsObject} and uses the
 * configured resource wrappers ({@link I_CmsResourceWrapper}) to change the view
 * to the existing resources in the VFS.<p>
 *
 * Almost every method in this class iterates through the configured list of
 * {@link I_CmsResourceWrapper} and calls the same method there. The first resource
 * wrapper in the list which feels responsible for that action handles it and the
 * iteration ends. So the resource wrappers should check in every method if it is
 * responsible or not. Be careful if there are more than one resource wrapper for
 * the same resource in the VFS, because the first in the list wins. If the iteration is
 * finished and no resource wrapper felt responsible the default action is to call the
 * method in the {@link CmsObject}.<p>
 *
 * It is possible to create an unchanged access to the resource in the VFS by creating
 * a new instance of the CmsObjectWrapper with an empty list of resource wrappers.<p>
 *
 * @since 6.2.4
 */
public class CmsObjectWrapper {

    /** The name of the attribute in the {@link CmsRequestContext} where the current CmsObjectWrapper can be found. */
    public static final String ATTRIBUTE_NAME = "org.opencms.file.wrapper.CmsObjectWrapper";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsObjectWrapper.class);

    /** Flag to contro whether byte order marks should be added to plaintext files. */
    private boolean m_addByteOrderMark = true;

    /** The initialized CmsObject. */
    private CmsObject m_cms;

    /** The list with the configured wrappers (entries of type {@link I_CmsResourceWrapper}). */
    private List<I_CmsResourceWrapper> m_wrappers;

    /**
     * Constructor with the CmsObject to wrap and the resource wrappers to use.<p>
     *
     * @param cms the initialized CmsObject
     * @param wrappers the configured wrappers to use (entries of type {@link I_CmsResourceWrapper})
     */
    public CmsObjectWrapper(CmsObject cms, List<I_CmsResourceWrapper> wrappers) {

        m_cms = cms;
        m_wrappers = wrappers;
    }

    /**
     * Copies a resource.<p>
     *
     * Iterates through all configured resource wrappers till the first returns <code>true</code>.<p>
     *
     * @see I_CmsResourceWrapper#copyResource(CmsObject, String, String, CmsResource.CmsResourceCopyMode)
     * @see CmsObject#copyResource(String, String, CmsResource.CmsResourceCopyMode)
     *
     * @param source the name of the resource to copy (full path)
     * @param destination the name of the copy destination (full path)
     * @param siblingMode indicates how to handle siblings during copy
     *
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the <code>destination</code> argument is null or of length 0
     */
    public void copyResource(String source, String destination, CmsResourceCopyMode siblingMode)
    throws CmsException, CmsIllegalArgumentException {

        boolean exec = false;

        // iterate through all wrappers and call "copyResource" till one does not return null
        List<I_CmsResourceWrapper> wrappers = getWrappers();
        Iterator<I_CmsResourceWrapper> iter = wrappers.iterator();
        while (iter.hasNext()) {
            I_CmsResourceWrapper wrapper = iter.next();
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
     * Creates a new resource of the given resource type with empty content and no properties.<p>
     *
     * @see #createResource(String, int, byte[], List)
     *
     * @param resourcename the name of the resource to create (full path)
     * @param type the type of the resource to create
     *
     * @return the created resource
     *
     * @throws CmsException if something goes wrong
     * @throws CmsIllegalArgumentException if the given <code>resourcename</code> is null or of length 0
     */
    public CmsResource createResource(String resourcename, int type) throws CmsException, CmsIllegalArgumentException {

        return createResource(resourcename, type, new byte[0], new ArrayList<CmsProperty>(0));
    }

    /**
     * Creates a new resource of the given resource type with the provided content and properties.<p>
     *
     * Iterates through all configured resource wrappers till the first returns not <code>null</code>.<p>
     *
     * @see I_CmsResourceWrapper#createResource(CmsObject, String, int, byte[], List)
     * @see CmsObject#createResource(String, int, byte[], List)
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
    public CmsResource createResource(String resourcename, int type, byte[] content, List<CmsProperty> properties)
    throws CmsException, CmsIllegalArgumentException {

        CmsResource res = null;

        // iterate through all wrappers and call "createResource" till one does not return null
        List<I_CmsResourceWrapper> wrappers = getWrappers();
        Iterator<I_CmsResourceWrapper> iter = wrappers.iterator();
        while (iter.hasNext()) {
            I_CmsResourceWrapper wrapper = iter.next();
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
     * Iterates through all configured resource wrappers till the first returns <code>true</code>.<p>
     *
     * @see I_CmsResourceWrapper#deleteResource(CmsObject, String, CmsResource.CmsResourceDeleteMode)
     * @see CmsObject#deleteResource(String, CmsResource.CmsResourceDeleteMode)
     *
     * @param resourcename the name of the resource to delete (full path)
     * @param siblingMode indicates how to handle siblings of the deleted resource
     *
     * @throws CmsException if something goes wrong
     */
    public void deleteResource(String resourcename, CmsResourceDeleteMode siblingMode) throws CmsException {

        boolean exec = false;

        // iterate through all wrappers and call "deleteResource" till one does not return false
        List<I_CmsResourceWrapper> wrappers = getWrappers();
        Iterator<I_CmsResourceWrapper> iter = wrappers.iterator();
        while (iter.hasNext()) {
            I_CmsResourceWrapper wrapper = iter.next();
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
     * using the {@link CmsResourceFilter#DEFAULT} filter.<p>
     *
     * Here it will be first checked if the resource exists in the VFS by calling
     * {@link org.opencms.file.CmsObject#existsResource(String)}. Only if it doesn't exist
     * in the VFS the method {@link I_CmsResourceWrapper#readResource(CmsObject, String, CmsResourceFilter)}
     * in the configured resource wrappers are called till the first does not throw an exception or returns
     * <code>null</code>.<p>
     *
     * @param resourcename the name of the resource to check (full path)
     *
     * @return <code>true</code> if the resource is available
     */
    public boolean existsResource(String resourcename) {

        // first try to find the resource
        boolean ret = m_cms.existsResource(resourcename);

        // if not exists, ask the resource type wrappers
        if (!ret) {

            List<I_CmsResourceWrapper> wrappers = getWrappers();
            Iterator<I_CmsResourceWrapper> iter = wrappers.iterator();
            while (iter.hasNext()) {
                I_CmsResourceWrapper wrapper = iter.next();
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
     * Returns the lock state for a specified resource.<p>
     *
     * Iterates through all configured resource wrappers till the first returns not <code>null</code>.<p>
     *
     * @see I_CmsResourceWrapper#getLock(CmsObject, CmsResource)
     * @see CmsObject#getLock(CmsResource)
     *
     * @param resource the resource to return the lock state for
     *
     * @return the lock state for the specified resource
     *
     * @throws CmsException if something goes wrong
     */
    public CmsLock getLock(CmsResource resource) throws CmsException {

        CmsLock lock = null;

        // iterate through all wrappers and call "getLock" till one does not return null
        List<I_CmsResourceWrapper> wrappers = getWrappers();
        Iterator<I_CmsResourceWrapper> iter = wrappers.iterator();
        while (iter.hasNext()) {
            I_CmsResourceWrapper wrapper = iter.next();
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
     * Delegate method for {@link CmsObject#getRequestContext()}.<p>
     *
     * @see CmsObject#getRequestContext()

     * @return the current users request context
     */
    public CmsRequestContext getRequestContext() {

        return m_cms.getRequestContext();
    }

    /**
     * Returns all child resources of a resource, that is the resources
     * contained in a folder.<p>
     *
     * First fetch all child resources from VFS by calling {@link CmsObject#getResourcesInFolder(String, CmsResourceFilter)}.
     * After that all resource wrapper are called {@link I_CmsResourceWrapper#addResourcesToFolder(CmsObject, String, CmsResourceFilter)}
     * to have the chance to add additional resources to those already existing. In that list every resource is given to
     * the appropriate resource wrapper ({@link I_CmsResourceWrapper#wrapResource(CmsObject, CmsResource)}) to have the
     * possibility to change the existing resources. The matching resource wrapper for a resource is found by a call to
     * {@link I_CmsResourceWrapper#isWrappedResource(CmsObject, CmsResource)}.<p>
     *
     * @see I_CmsResourceWrapper#addResourcesToFolder(CmsObject, String, CmsResourceFilter)
     * @see CmsObject#getResourcesInFolder(String, CmsResourceFilter)
     *
     * @param resourcename the full path of the resource to return the child resources for
     * @param filter the resource filter to use
     *
     * @return a list of all child <code>{@link CmsResource}</code>s
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> getResourcesInFolder(String resourcename, CmsResourceFilter filter) throws CmsException {

        List<CmsResource> list = new ArrayList<CmsResource>();

        // read children existing in the VFS
        try {
            list.addAll(m_cms.getResourcesInFolder(resourcename, filter));
        } catch (CmsException ex) {
            //noop
        }

        // iterate through all wrappers and call "addResourcesToFolder" and add the results to the list
        List<I_CmsResourceWrapper> wrappers = getWrappers();
        Iterator<I_CmsResourceWrapper> iter1 = wrappers.iterator();
        while (iter1.hasNext()) {
            I_CmsResourceWrapper wrapper = iter1.next();
            List<CmsResource> added = wrapper.addResourcesToFolder(m_cms, resourcename, filter);
            if (added != null) {
                list.addAll(added);
            }
        }

        // create a new list to add all resources
        ArrayList<CmsResource> wrapped = new ArrayList<CmsResource>();

        // eventually wrap the found resources
        Iterator<CmsResource> iter2 = list.iterator();
        while (iter2.hasNext()) {
            CmsResource res = iter2.next();

            // correct the length of the content if an UTF-8 marker would be added later
            if (needUtf8Marker(res) && !startsWithUtf8Marker(res)) {
                CmsWrappedResource wrap = new CmsWrappedResource(res);
                wrap.setLength(res.getLength() + CmsResourceWrapperUtils.UTF8_MARKER.length);

                res = wrap.getResource();
            }

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
        Collections.sort(wrapped, I_CmsResource.COMPARE_ROOT_PATH_IGNORE_CASE_FOLDERS_FIRST);

        return wrapped;
    }

    /**
     * Delegate method for {@link CmsObject#getSitePath(CmsResource)}.<p>
     *
     * @see CmsObject#getSitePath(org.opencms.file.CmsResource)
     *
     * @param resource the resource to get the adjusted site root path for
     *
     * @return the absolute resource path adjusted for the current site
     */
    public String getSitePath(CmsResource resource) {

        return m_cms.getSitePath(resource);
    }

    /**
     * Returns the configured resource wrappers used by this instance.<p>
     *
     * Entries in list are from type {@link I_CmsResourceWrapper}.<p>
     *
     * @return the configured resource wrappers for this instance
     */
    public List<I_CmsResourceWrapper> getWrappers() {

        return m_wrappers;
    }

    /**
     * Locks a resource.<p>
     *
     * Iterates through all configured resource wrappers till the first returns <code>true</code>.<p>
     *
     * @see CmsObject#lockResource(String)
     *
     * @param resourcename the name of the resource to lock (full path)
     *
     * @throws CmsException if something goes wrong
     */
    public void lockResource(String resourcename) throws CmsException {

        boolean exec = false;

        // iterate through all wrappers and call "lockResource" till one does not return false
        List<I_CmsResourceWrapper> wrappers = getWrappers();
        Iterator<I_CmsResourceWrapper> iter = wrappers.iterator();
        while (iter.hasNext()) {
            I_CmsResourceWrapper wrapper = iter.next();
            exec = wrapper.lockResource(m_cms, resourcename, false);
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
     * Locks a resource temporarily.<p>
     *
     * @param resourceName the name of the resource to lock
     *
     * @throws CmsException if something goes wrong
     */
    public void lockResourceTemporary(String resourceName) throws CmsException {

        boolean exec = false;
        // iterate through all wrappers and call "lockResource" till one does not return false
        List<I_CmsResourceWrapper> wrappers = getWrappers();
        for (I_CmsResourceWrapper wrapper : wrappers) {
            exec = wrapper.lockResource(m_cms, resourceName, true);
            if (exec) {
                break;
            }
        }

        // delegate the call to the CmsObject
        if (!exec) {
            m_cms.lockResourceTemporary(resourceName);
        }
    }

    /**
     * Moves a resource to the given destination.<p>
     *
     * Iterates through all configured resource wrappers till the first returns <code>true</code>.<p>
     *
     * @see I_CmsResourceWrapper#moveResource(CmsObject, String, String)
     * @see CmsObject#moveResource(String, String)
     *
     * @param source the name of the resource to move (full path)
     * @param destination the destination resource name (full path)
     *
     * @throws CmsException if something goes wrong
     */
    public void moveResource(String source, String destination) throws CmsException {

        boolean exec = false;

        // iterate through all wrappers and call "moveResource" till one does not return false
        List<I_CmsResourceWrapper> wrappers = getWrappers();
        Iterator<I_CmsResourceWrapper> iter = wrappers.iterator();
        while (iter.hasNext()) {
            I_CmsResourceWrapper wrapper = iter.next();
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
     * Iterates through all configured resource wrappers till the first returns not <code>null</code>.<p>
     *
     * If the resource contains textual content and the encoding is UTF-8, then the byte order mask
     * for UTF-8 is added at the start of the content to make sure that a client using this content
     * displays it correctly.<p>
     *
     * @see I_CmsResourceWrapper#readFile(CmsObject, String, CmsResourceFilter)
     * @see CmsObject#readFile(String, CmsResourceFilter)
     *
     * @param resourcename the name of the resource to read (full path)
     * @param filter the resource filter to use while reading
     *
     * @return the file resource that was read
     *
     * @throws CmsException if the file resource could not be read for any reason
     */
    public CmsFile readFile(String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsFile res = null;

        // iterate through all wrappers and call "readFile" till one does not return null
        List<I_CmsResourceWrapper> wrappers = getWrappers();
        Iterator<I_CmsResourceWrapper> iter = wrappers.iterator();
        while (iter.hasNext()) {
            I_CmsResourceWrapper wrapper = iter.next();
            res = wrapper.readFile(m_cms, resourcename, filter);
            if (res != null) {
                break;
            }
        }

        // delegate the call to the CmsObject
        if (res == null) {
            res = m_cms.readFile(resourcename, filter);
        }

        // for text based resources which are encoded in UTF-8 add the UTF marker at the start
        // of the content
        if (needUtf8Marker(res)) {

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_ADD_UTF8_MARKER_1, res.getRootPath()));
            }

            res.setContents(CmsResourceWrapperUtils.addUtf8Marker(res.getContents()));
        }

        return res;
    }

    /**
     * Delegate method for {@link CmsObject#readPropertyObject(CmsResource, String, boolean)}.<p>
     *
     * @see CmsObject#readPropertyObject(CmsResource, String, boolean)
     *
     * @param resource the resource where the property is attached to
     * @param property the property name
     * @param search if true, the property is searched on all parent folders of the resource,
     *      if it's not found attached directly to the resource
     *
     * @return the required property, or <code>{@link CmsProperty#getNullProperty()}</code> if the property was not found
     *
     * @throws CmsException if something goes wrong
     */
    public CmsProperty readPropertyObject(CmsResource resource, String property, boolean search) throws CmsException {

        return m_cms.readPropertyObject(resource, property, search);
    }

    /**
     * Delegate method for {@link CmsObject#readResource(CmsUUID, CmsResourceFilter)}.<p>
     *
     * @see CmsObject#readResource(CmsUUID, CmsResourceFilter)
     *
     * @param structureID the ID of the structure to read
     * @param filter the resource filter to use while reading
     *
     * @return the resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     */
    public CmsResource readResource(CmsUUID structureID, CmsResourceFilter filter) throws CmsException {

        return m_cms.readResource(structureID, filter);
    }

    /**
     * Reads a resource from the VFS,
     * using the <code>{@link CmsResourceFilter#DEFAULT}</code> filter.<p>
     *
     * Iterates through all configured resource wrappers till the first returns not <code>null</code>.<p>
     *
     * @see I_CmsResourceWrapper#readResource(CmsObject, String, CmsResourceFilter)
     * @see CmsObject#readResource(String, CmsResourceFilter)
     *
     * @param resourcename The name of the resource to read (full path)
     * @param filter the resource filter to use while reading
     *
     * @return the resource that was read
     *
     * @throws CmsException if the resource could not be read for any reason
     */
    public CmsResource readResource(String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource res = null;

        // iterate through all wrappers and call "readResource" till one does not return null
        List<I_CmsResourceWrapper> wrappers = getWrappers();
        Iterator<I_CmsResourceWrapper> iter = wrappers.iterator();
        while (iter.hasNext()) {
            I_CmsResourceWrapper wrapper = iter.next();
            res = wrapper.readResource(m_cms, resourcename, filter);
            if (res != null) {
                break;
            }
        }

        // delegate the call to the CmsObject
        if (res == null) {
            res = m_cms.readResource(resourcename, filter);
        }

        // correct the length of the content if an UTF-8 marker would be added later
        if (needUtf8Marker(res) && !startsWithUtf8Marker(res)) {
            CmsWrappedResource wrap = new CmsWrappedResource(res);
            wrap.setLength(res.getLength() + CmsResourceWrapperUtils.UTF8_MARKER.length);

            return wrap.getResource();
        }

        return res;
    }

    /**
     * Delegate method for {@link CmsObject#readUser(CmsUUID)}.<p>
     *
     * @see CmsObject#readUser(CmsUUID)
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
     * Returns a link to an existing resource in the VFS.<p>
     *
     * Because it is possible through the <code>CmsObjectWrapper</code> to create "virtual" resources,
     * which can not be found in the VFS, it is necessary to change the links in pages
     * as well, so that they point to resources which really exists in the VFS.<p>
     *
     * Iterates through all configured resource wrappers till the first returns not <code>null</code>.<p>
     *
     * @see #rewriteLink(String)
     * @see I_CmsResourceWrapper#restoreLink(CmsObject, String)
     *
     * @param path the path to the resource
     *
     * @return the path for the resource which exists in the VFS
     */
    public String restoreLink(String path) {

        if ((path != null) && (path.startsWith("#"))) {
            return path;
        }

        String ret = null;

        // iterate through all wrappers and call "restoreLink" till one does not return null
        List<I_CmsResourceWrapper> wrappers = getWrappers();
        Iterator<I_CmsResourceWrapper> iter = wrappers.iterator();
        while (iter.hasNext()) {
            I_CmsResourceWrapper wrapper = iter.next();
            ret = wrapper.restoreLink(m_cms, m_cms.getRequestContext().removeSiteRoot(path));
            if (ret != null) {
                return ret;
            }
        }

        return path;
    }

    /**
     * Returns a link to a resource after it was wrapped by the CmsObjectWrapper.<p>
     *
     * Because it is possible to change the names of resources inside the VFS by this
     * <code>CmsObjectWrapper</code>, it is necessary to change the links used in pages
     * as well, so that they point to the changed name of the resource.<p>
     *
     * For example: <code>/sites/default/index.html</code> becomes to
     * <code>/sites/default/index.html.jsp</code>, because it is a jsp page, the links
     * in pages where corrected so that they point to the new name (with extension "jsp").<p>
     *
     * Used for the link processing in the class {@link org.opencms.relations.CmsLink}.<p>
     *
     * Iterates through all configured resource wrappers till the first returns not <code>null</code>.<p>
     *
     * @see #restoreLink(String)
     * @see I_CmsResourceWrapper#rewriteLink(CmsObject, CmsResource)
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
                List<I_CmsResourceWrapper> wrappers = getWrappers();
                Iterator<I_CmsResourceWrapper> iter = wrappers.iterator();
                while (iter.hasNext()) {
                    I_CmsResourceWrapper wrapper = iter.next();
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
     * Enables or disables the automatic adding of byte order marks to plaintext files.<p>
     *
     * @param addByteOrderMark true if byte order marks should be added to plaintext files automatically
     */
    public void setAddByteOrderMark(boolean addByteOrderMark) {

        m_addByteOrderMark = addByteOrderMark;
    }

    /**
     * Unlocks a resource.<p>
     *
     * Iterates through all configured resource wrappers till the first returns <code>true</code>.<p>
     *
     * @see I_CmsResourceWrapper#unlockResource(CmsObject, String)
     * @see CmsObject#unlockResource(String)
     *
     * @param resourcename the name of the resource to unlock (full path)
     *
     * @throws CmsException if something goes wrong
     */
    public void unlockResource(String resourcename) throws CmsException {

        boolean exec = false;

        // iterate through all wrappers and call "lockResource" till one does not return false
        List<I_CmsResourceWrapper> wrappers = getWrappers();
        Iterator<I_CmsResourceWrapper> iter = wrappers.iterator();
        while (iter.hasNext()) {
            I_CmsResourceWrapper wrapper = iter.next();
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
     * Iterates through all configured resource wrappers till the first returns not <code>null</code>.<p>
     *
     * @see I_CmsResourceWrapper#writeFile(CmsObject, CmsFile)
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

        // remove the added UTF-8 marker
        if (needUtf8Marker(resource)) {
            resource.setContents(CmsResourceWrapperUtils.removeUtf8Marker(resource.getContents()));
        }

        String resourcename = m_cms.getSitePath(resource);
        if (!m_cms.existsResource(resourcename)) {

            // iterate through all wrappers and call "writeFile" till one does not return null
            List<I_CmsResourceWrapper> wrappers = getWrappers();
            Iterator<I_CmsResourceWrapper> iter = wrappers.iterator();
            while (iter.hasNext()) {
                I_CmsResourceWrapper wrapper = iter.next();
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
     * @see I_CmsResourceWrapper#isWrappedResource(CmsObject, CmsResource)
     *
     * @param res the resource to find a resource type wrapper for
     *
     * @return the found resource type wrapper for the resource or null if not found
     */
    private I_CmsResourceWrapper getResourceTypeWrapper(CmsResource res) {

        Iterator<I_CmsResourceWrapper> iter = getWrappers().iterator();
        while (iter.hasNext()) {
            I_CmsResourceWrapper wrapper = iter.next();

            if (wrapper.isWrappedResource(m_cms, res)) {
                return wrapper;
            }
        }

        return null;
    }

    /**
     * Checks if the resource type needs an UTF-8 marker.<p>
     *
     * If the encoding of the resource is "UTF-8" and the resource
     * type is one of the following:<br/>
     * <ul>
     * <li>{@link CmsResourceTypeJsp}</li>
     * <li>{@link CmsResourceTypePlain}</li>
     * <li>{@link CmsResourceTypeXmlContent}</li>
     * <li>{@link CmsResourceTypeXmlPage}</li>
     * </ul>
     *
     * it needs an UTF-8 marker.<p>
     *
     * @param res the resource to check if the content needs a UTF-8 marker
     *
     * @return <code>true</code> if the resource needs an UTF-8 maker otherwise <code>false</code>
     */
    private boolean needUtf8Marker(CmsResource res) {

        if (!m_addByteOrderMark) {
            return false;
        }
        // if the encoding of the resource is not UTF-8 return false
        String encoding = CmsLocaleManager.getResourceEncoding(m_cms, res);
        boolean result = false;
        if (CmsEncoder.ENCODING_UTF_8.equals(encoding)) {
            try {
                I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(res.getTypeId());
                if (resType instanceof CmsResourceTypeJsp) {
                    result = true;
                } else if (resType instanceof CmsResourceTypePlain) {
                    result = true;
                } else if (resType instanceof CmsResourceTypeXmlContent) {
                    result = true;
                } else if (resType instanceof CmsResourceTypeXmlPage) {
                    result = true;
                }
            } catch (CmsLoaderException e) {
                LOG.debug(e);
            }
        }
        return result;
    }

    /**
     * Checks if the file content already contains the UTF8 marker.<p>
     *
     * @param res the resource to check
     *
     * @return <code>true</code> if the file content already contains the UTF8 marker
     */
    private boolean startsWithUtf8Marker(CmsResource res) {

        boolean result = false;
        try {
            if (res.isFile()) {
                CmsFile file = m_cms.readFile(res);
                if ((file.getContents().length >= 3)
                    && (file.getContents()[0] == CmsResourceWrapperUtils.UTF8_MARKER[0])
                    && (file.getContents()[1] == CmsResourceWrapperUtils.UTF8_MARKER[1])
                    && (file.getContents()[2] == CmsResourceWrapperUtils.UTF8_MARKER[2])) {
                    result = true;
                }
            }
        } catch (CmsException e) {
            LOG.debug(e);
        }
        return result;
    }
}

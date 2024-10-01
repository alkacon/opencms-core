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
 * For further information about Alkacon Software, please see the
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

import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.repository.CmsPropertyName;
import org.opencms.repository.I_CmsRepositoryItem;
import org.opencms.repository.I_CmsRepositorySession;
import org.opencms.security.CmsPermissionViolationException;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.jackrabbit.webdav.DavCompliance;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavMethods;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.lock.ActiveLock;
import org.apache.jackrabbit.webdav.lock.LockInfo;
import org.apache.jackrabbit.webdav.lock.LockManager;
import org.apache.jackrabbit.webdav.lock.Scope;
import org.apache.jackrabbit.webdav.lock.Type;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.property.PropEntry;
import org.apache.jackrabbit.webdav.property.ResourceType;
import org.apache.jackrabbit.webdav.xml.Namespace;

/**
 * Represents a resource in the WebDav repository (may not actually correspond to an actual OpenCms resource, since
 * DavResource are also created for the target locations for move/copy operations, before any of the moving / copying happens.
 */
public class CmsDavResource implements DavResource {

    /** Logger instance for this class. **/
    private static final Log LOG = CmsLog.getLog(CmsDavResource.class);

    /** The resource factory that produced this resource. */
    private CmsDavResourceFactory m_factory;

    /** The resource locator for this resource. */
    private DavResourceLocator m_locator;

    /** The Webdav session object. */
    private CmsDavSession m_session;

    /** Lazily initialized repository item - null means not initialized, Optional.empty means tried to load resource, but it was not found. */
    private Optional<I_CmsRepositoryItem> m_item;

    /** The lock manager. */
    private LockManager m_lockManager;

    /**
     * Creates a new instance.
     *
     * @param loc the locator for this resource
     * @param factory the factory that produced this resource
     * @param session the Webdav session
     * @param lockManager the lock manager
     */
    public CmsDavResource(
        DavResourceLocator loc,
        CmsDavResourceFactory factory,
        CmsDavSession session,
        LockManager lockManager) {

        m_factory = factory;
        m_locator = loc;
        m_session = session;
        m_lockManager = lockManager;
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#addLockManager(org.apache.jackrabbit.webdav.lock.LockManager)
     */
    public void addLockManager(LockManager lockmgr) {

        m_lockManager = lockmgr;
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#addMember(org.apache.jackrabbit.webdav.DavResource, org.apache.jackrabbit.webdav.io.InputContext)
     */
    public void addMember(DavResource dres, InputContext inputContext) throws DavException {

        I_CmsRepositorySession session = getRepositorySession();
        String childPath = ((CmsDavResource)dres).getCmsPath();
        String method = ((CmsDavInputContext)inputContext).getMethod();
        InputStream stream = inputContext.getInputStream();
        if (method.equals(DavMethods.METHOD_MKCOL) && (stream != null)) {
            throw new DavException(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        }
        if (dres.exists() && isLocked(dres)) {
            throw new DavException(DavServletResponse.SC_LOCKED);
        }
        try {
            if (stream != null) {
                session.save(childPath, stream, true);
            } else {
                session.create(childPath);
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new DavException(CmsDavUtil.getStatusForException(e), e);
        } catch (Exception e) {
            throw new DavException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#alterProperties(java.util.List)
     */
    public MultiStatusResponse alterProperties(List<? extends PropEntry> changeList) throws DavException {

        if (exists() && isLocked(this)) {
            throw new DavException(DavServletResponse.SC_LOCKED);
        }

        MultiStatusResponse res = new MultiStatusResponse(getHref(), null);
        Map<CmsPropertyName, String> propMap = new HashMap<>();
        for (PropEntry entry : changeList) {
            if (entry instanceof DefaultDavProperty<?>) {
                DefaultDavProperty<String> prop = (DefaultDavProperty<String>)entry;
                CmsPropertyName cmsPropName = new CmsPropertyName(
                    prop.getName().getNamespace().getURI(),
                    prop.getName().getName());
                propMap.put(cmsPropName, prop.getValue());
            } else if (entry instanceof DavPropertyName) {
                CmsPropertyName cmsPropName = new CmsPropertyName(
                    ((DavPropertyName)entry).getNamespace().getURI(),
                    ((DavPropertyName)entry).getName());
                propMap.put(cmsPropName, "");

            }
        }
        int status = HttpServletResponse.SC_OK;
        try {
            getRepositorySession().updateProperties(getCmsPath(), propMap);
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
            if (e instanceof CmsPermissionViolationException) {
                status = HttpServletResponse.SC_FORBIDDEN;
            }
        }
        for (PropEntry entry : changeList) {
            if (entry instanceof DavPropertyName) {
                res.add((DavPropertyName)entry, status);
            } else if (entry instanceof DefaultDavProperty<?>) {
                res.add((DavProperty)entry, status);
            } else {
                res.add((DavPropertyName)entry, HttpServletResponse.SC_FORBIDDEN);
            }
        }
        return res;
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#copy(org.apache.jackrabbit.webdav.DavResource, boolean)
     */
    public void copy(DavResource dres, boolean shallow) throws DavException {

        CmsDavResource other = (CmsDavResource)dres;
        boolean targetParentExists = false;
        try {
            targetParentExists = dres.getCollection().exists();
        } catch (Exception e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        if (!targetParentExists) {
            throw new DavException(HttpServletResponse.SC_CONFLICT);
        }

        try {
            getRepositorySession().copy(getCmsPath(), other.getCmsPath(), true, shallow);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new DavException(CmsDavUtil.getStatusForException(e));
        }
    }

    /**
     * Deletes the resource.
     *
     * @throws DavException if an error occurs
     */
    public void delete() throws DavException {

        if (!exists()) {
            throw new DavException(HttpServletResponse.SC_NOT_FOUND);
        }
        try {
            getRepositorySession().delete(getCmsPath());
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new DavException(CmsDavUtil.getStatusForException(e), e);
        }

    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#exists()
     */
    public boolean exists() {

        return getItem() != null;

    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#getCollection()
     */
    public DavResource getCollection() {

        DavResourceLocator locator = m_locator.getFactory().createResourceLocator(
            m_locator.getPrefix(),
            m_locator.getWorkspacePath(),
            CmsResource.getParentFolder(m_locator.getResourcePath()));
        try {
            return m_factory.createResource(locator, m_session);
        } catch (DavException e) {
            return null;

        }
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#getComplianceClass()
     */
    public String getComplianceClass() {

        return DavCompliance.concatComplianceClasses(new String[] {DavCompliance._1_, DavCompliance._2_});
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#getDisplayName()
     */
    public String getDisplayName() {

        String result = CmsResource.getName(getCmsPath());
        result = result.replace("/", "");
        return result;
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#getFactory()
     */
    public DavResourceFactory getFactory() {

        return m_factory;
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#getHref()
     */
    public String getHref() {

        String href = m_locator.getHref(true);
        String result = CmsFileUtil.removeTrailingSeparator(href);
        return result;
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#getLocator()
     */
    public DavResourceLocator getLocator() {

        return m_locator;
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#getLock(org.apache.jackrabbit.webdav.lock.Type, org.apache.jackrabbit.webdav.lock.Scope)
     */
    public ActiveLock getLock(Type type, Scope scope) {

        return m_lockManager.getLock(type, scope, this);
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#getLocks()
     */
    public ActiveLock[] getLocks() {

        ActiveLock writeLock = getLock(Type.WRITE, Scope.EXCLUSIVE);
        return (writeLock != null) ? new ActiveLock[] {writeLock} : new ActiveLock[0];
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#getMembers()
     */
    public DavResourceIterator getMembers() {

        I_CmsRepositorySession session = getRepositorySession();
        try {
            List<I_CmsRepositoryItem> children = session.list(getCmsPath());
            List<DavResource> childDavRes = children.stream().map(child -> {
                String childPath = CmsStringUtil.joinPaths(m_locator.getWorkspacePath(), child.getName());
                DavResourceLocator childLocator = m_locator.getFactory().createResourceLocator(
                    m_locator.getPrefix(),
                    m_locator.getWorkspacePath(),
                    childPath);
                return new CmsDavResource(childLocator, m_factory, m_session, m_lockManager);
            }).filter(child -> {
                boolean exists = child.exists();
                if (!exists) {
                    // one case where this happens is when the child resource has a name that would be
                    // modified by the configured file translation rules.
                    LOG.warn(
                        "Invalid child resource: "
                            + child.getLocator().getPrefix()
                            + ":"
                            + child.getLocator().getResourcePath());
                }
                return exists;
            }).collect(Collectors.toList());
            return new DavResourceIteratorImpl(childDavRes);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return null;
        }

    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#getModificationTime()
     */
    public long getModificationTime() {

        I_CmsRepositoryItem item = getItem();
        return item.getLastModifiedDate();
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#getProperties()
     */
    public DavPropertySet getProperties() {

        DavPropertySet result = new DavPropertySet();
        ResourceType typeProp = new ResourceType(
            isCollection() ? ResourceType.COLLECTION : ResourceType.DEFAULT_RESOURCE);
        result.add(typeProp);
        result.add(
            new DefaultDavProperty<String>(
                DavPropertyName.GETLASTMODIFIED,
                CmsDavUtil.DATE_FORMAT.format(new Date(getItem().getLastModifiedDate()))));
        result.add(new DefaultDavProperty<String>(DavPropertyName.DISPLAYNAME, "" + getItem().getName()));
        if (!isCollection()) {
            result.add(
                new DefaultDavProperty<String>(DavPropertyName.GETCONTENTLENGTH, "" + getItem().getContentLength()));

            result.add(new DefaultDavProperty<String>(DavPropertyName.GETETAG, getETag()));

        }
        try {
            Map<CmsPropertyName, String> cmsProps = getRepositorySession().getProperties(getCmsPath());
            for (Map.Entry<CmsPropertyName, String> entry : cmsProps.entrySet()) {
                CmsPropertyName propName = entry.getKey();
                DavPropertyName name = DavPropertyName.create(
                    propName.getName(),
                    Namespace.getNamespace(propName.getNamespace()));
                result.add(new DefaultDavProperty<String>(name, entry.getValue()));
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return result;
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#getProperty(org.apache.jackrabbit.webdav.property.DavPropertyName)
     */
    public DavProperty<?> getProperty(DavPropertyName name) {

        return getProperties().get(name);
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#getPropertyNames()
     */
    public DavPropertyName[] getPropertyNames() {

        return getProperties().getPropertyNames();
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#getResourcePath()
     */
    public String getResourcePath() {

        return m_locator.getResourcePath();
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#getSession()
     */
    public DavSession getSession() {

        return m_session;
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#getSupportedMethods()
     */
    public String getSupportedMethods() {

        TreeSet<String> methods = new TreeSet<>();
        I_CmsRepositoryItem item = getItem();
        if (item == null) {
            methods.addAll(Arrays.asList(DavMethods.METHOD_OPTIONS, DavMethods.METHOD_PUT, DavMethods.METHOD_MKCOL));
            methods.add(DavMethods.METHOD_LOCK);
        } else {
            methods.addAll(
                Arrays.asList(
                    DavMethods.METHOD_OPTIONS,
                    DavMethods.METHOD_HEAD,
                    DavMethods.METHOD_POST,
                    DavMethods.METHOD_DELETE));
            methods.add(DavMethods.METHOD_PROPFIND);
            methods.add(DavMethods.METHOD_PROPPATCH);

            Arrays.asList(DavMethods.METHOD_COPY, DavMethods.METHOD_MOVE);
            if (!item.isCollection()) {
                methods.add(DavMethods.METHOD_PUT);
            }

        }
        return CmsStringUtil.listAsString(new ArrayList<>(methods), ", ");
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#hasLock(org.apache.jackrabbit.webdav.lock.Type, org.apache.jackrabbit.webdav.lock.Scope)
     */
    public boolean hasLock(Type type, Scope scope) {

        return m_lockManager.getLock(type, scope, this) != null;
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#isCollection()
     */
    public boolean isCollection() {

        I_CmsRepositoryItem item = getItem();
        return (item != null) && item.isCollection();
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#isLockable(org.apache.jackrabbit.webdav.lock.Type, org.apache.jackrabbit.webdav.lock.Scope)
     */
    public boolean isLockable(Type type, Scope scope) {

        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#lock(org.apache.jackrabbit.webdav.lock.LockInfo)
     */
    public ActiveLock lock(LockInfo reqLockInfo) throws DavException {

        return m_lockManager.createLock(reqLockInfo, this);
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#move(org.apache.jackrabbit.webdav.DavResource)
     */
    public void move(DavResource destination) throws DavException {

        CmsDavResource other = (CmsDavResource)destination;
        if (isLocked(this)) {
            throw new DavException(DavServletResponse.SC_LOCKED);
        }

        try {
            getRepositorySession().move(getCmsPath(), other.getCmsPath(), true);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new DavException(CmsDavUtil.getStatusForException(e));
        }

    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#refreshLock(org.apache.jackrabbit.webdav.lock.LockInfo, java.lang.String)
     */
    public ActiveLock refreshLock(LockInfo reqLockInfo, String lockToken) throws DavException {

        return m_lockManager.refreshLock(reqLockInfo, lockToken, this);
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#removeMember(org.apache.jackrabbit.webdav.DavResource)
     */
    public void removeMember(DavResource member) throws DavException {

        if (isLocked(this) || isLocked(member)) {
            throw new DavException(DavServletResponse.SC_LOCKED);
        }
        ((CmsDavResource)member).delete();
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#removeProperty(org.apache.jackrabbit.webdav.property.DavPropertyName)
     */
    public void removeProperty(DavPropertyName propertyName) throws DavException {

        if (exists() && isLocked(this)) {
            throw new DavException(DavServletResponse.SC_LOCKED);
        }

        I_CmsRepositorySession session = getRepositorySession();
        Map<CmsPropertyName, String> props = new HashMap<>();
        CmsPropertyName key = new CmsPropertyName(propertyName.getNamespace().getURI(), propertyName.getName());
        props.put(key, "");
        try {
            session.updateProperties(getCmsPath(), props);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new DavException(500);
        }
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#setProperty(org.apache.jackrabbit.webdav.property.DavProperty)
     */
    public void setProperty(DavProperty<?> property) throws DavException {

        if (exists() && isLocked(this)) {
            throw new DavException(DavServletResponse.SC_LOCKED);
        }

        if (!(property instanceof DefaultDavProperty)) {
            throw new DavException(HttpServletResponse.SC_FORBIDDEN);
        }
        I_CmsRepositorySession session = getRepositorySession();
        Map<CmsPropertyName, String> props = new HashMap<>();
        DavPropertyName propertyName = property.getName();
        String newValue = ((DefaultDavProperty<String>)property).getValue();
        if (newValue == null) {
            newValue = "";
        }
        CmsPropertyName key = new CmsPropertyName(propertyName.getNamespace().getURI(), propertyName.getName());
        props.put(key, newValue);
        try {
            session.updateProperties(getCmsPath(), props);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new DavException(500);
        }

    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#spool(org.apache.jackrabbit.webdav.io.OutputContext)
     */
    public void spool(OutputContext outputContext) throws IOException {

        I_CmsRepositoryItem item = getItem();
        outputContext.setContentType(item.getMimeType());
        outputContext.setContentLength(item.getContentLength());
        outputContext.setModificationTime(item.getLastModifiedDate());
        outputContext.setETag(getETag());
        OutputStream out = outputContext.getOutputStream();
        if (out != null) {
            out.write(item.getContent());
        }

    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#unlock(java.lang.String)
     */
    public void unlock(String lockToken) throws DavException {

        m_lockManager.releaseLock(lockToken, this);
    }

    /**
     * Gets the OpenCms path corresponding to this resource's locator.
     *
     * @return the OpenCms path
     */
    private String getCmsPath() {

        String path = m_locator.getResourcePath();
        String workspace = m_locator.getWorkspacePath();
        Optional<String> remainingPath = CmsStringUtil.removePrefixPath(workspace, path);
        return remainingPath.orElse(null);

    }

    /**
     * Computes the ETag for the item (the item must be not null).
     *
     * @return the ETag for the repository item
     */
    private String getETag() {

        return "\"" + getItem().getContentLength() + "-" + getItem().getLastModifiedDate() + "\"";
    }

    /**
     * Tries to load the appropriate repository item for this resource.
     *
     * @return the repository item, or null if none was found
     */
    private I_CmsRepositoryItem getItem() {

        if (m_item == null) {
            try {
                I_CmsRepositoryItem item = getRepositorySession().getItem(getCmsPath());
                m_item = Optional.of(item);

            } catch (Exception e) {
                boolean isFiltered = false;
                if (e instanceof CmsException) {
                    CmsMessageContainer messageContainer = ((CmsException)e).getMessageContainer();
                    if (messageContainer != null) {
                        String messageKey = messageContainer.getKey();
                        if (org.opencms.repository.Messages.ERR_ITEM_FILTERED_1.equals(messageKey)) {
                            isFiltered = true;
                        }
                    }
                }
                if (e instanceof CmsVfsResourceNotFoundException) {
                    LOG.info(e.getLocalizedMessage(), e);
                } else if (isFiltered) {
                    LOG.warn(e.getLocalizedMessage(), e);
                } else {
                    LOG.error(e.getLocalizedMessage(), e);
                }
                m_item = Optional.empty();

            }
        }
        return m_item.orElse(null);

    }

    /**
     * Gets the OpenCms repository session for which this resource was created.
     *
     * @return the OpenCms repository session
     */
    private I_CmsRepositorySession getRepositorySession() {

        return m_session.getRepositorySession();
    }

    /**
     * Return true if this resource cannot be modified due to a write lock
     * that is not owned by the current session.
     *
     * @param res the resource to check
     *
     * @return true if this resource cannot be modified due to a write lock
     */
    private boolean isLocked(DavResource res) {

        ActiveLock lock = res.getLock(Type.WRITE, Scope.EXCLUSIVE);
        if (lock == null) {
            return false;
        } else {
            for (String sLockToken : m_session.getLockTokens()) {
                if (sLockToken.equals(lock.getToken())) {
                    return false;
                }
            }
            return true;
        }
    }

}

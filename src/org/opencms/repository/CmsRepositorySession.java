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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.wrapper.CmsObjectWrapper;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsFileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * This is the session class to work with the {@link CmsRepository}.<p>
 *
 * You can get an instance of this class by calling
 * {@link CmsRepository#login(String, String)}.<p>
 *
 * This class provides basic file and folder operations on the resources
 * in the VFS of OpenCms.<p>
 *
 * @see A_CmsRepositorySession
 * @see I_CmsRepositorySession
 *
 * @since 6.5.6
 */
public class CmsRepositorySession extends A_CmsRepositorySession {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRepositorySession.class);

    /** The initialized {@link CmsObjectWrapper}. */
    private final CmsObjectWrapper m_cms;

    /**
     * Constructor with an initialized {@link CmsObjectWrapper} and a
     * {@link CmsRepositoryFilter} to use.<p>
     *
     * @param cms the initialized CmsObject
     * @param filter the repository filter to use
     */
    public CmsRepositorySession(CmsObjectWrapper cms, CmsRepositoryFilter filter) {

        m_cms = cms;
        setFilter(filter);
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#copy(java.lang.String, java.lang.String, boolean)
     */
    public void copy(String src, String dest, boolean overwrite) throws CmsException {

        src = validatePath(src);
        dest = validatePath(dest);

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_COPY_ITEM_2, src, dest));
        }

        // It is only possible in OpenCms to overwrite files.
        // Folder are not possible to overwrite.
        if (exists(dest)) {

            if (overwrite) {
                CmsResource srcRes = m_cms.readResource(src, CmsResourceFilter.DEFAULT);
                CmsResource destRes = m_cms.readResource(dest, CmsResourceFilter.DEFAULT);

                if ((srcRes.isFile()) && (destRes.isFile())) {

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.LOG_DELETE_DEST_0));
                    }

                    // delete existing resource
                    delete(dest);
                } else {

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.ERR_OVERWRITE_0));
                    }

                    // internal error (not possible)
                    throw new CmsException(Messages.get().container(Messages.ERR_OVERWRITE_0));
                }
            } else {

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.ERR_DEST_EXISTS_0));
                }

                throw new CmsVfsResourceAlreadyExistsException(Messages.get().container(Messages.ERR_DEST_EXISTS_0));
            }
        }

        // copy resource
        m_cms.copyResource(src, dest, CmsResource.COPY_PRESERVE_SIBLING);

        // unlock destination resource
        m_cms.unlockResource(dest);
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#create(java.lang.String)
     */
    public void create(String path) throws CmsException {

        path = validatePath(path);

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_CREATE_ITEM_1, path));
        }

        // create the folder
        CmsResource res = m_cms.createResource(path, CmsResourceTypeFolder.RESOURCE_TYPE_ID);

        // unlock new created folders if lock is not inherited
        if (!m_cms.getLock(res).isInherited()) {
            m_cms.unlockResource(path);
        }
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#delete(java.lang.String)
     */
    public void delete(String path) throws CmsException {

        path = validatePath(path);

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_DELETE_ITEM_1, path));
        }

        CmsRepositoryLockInfo lock = getLock(path);

        // lock resource
        m_cms.lockResource(path);

        // delete resource
        m_cms.deleteResource(path, CmsResource.DELETE_PRESERVE_SIBLINGS);

        // if deleting items out of a xml page restore lock state after deleting
        try {
            if (lock == null) {
                m_cms.unlockResource(path);
            }
        } catch (CmsException ex) {
            // noop
        }
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#exists(java.lang.String)
     */
    public boolean exists(String path) {

        try {
            path = validatePath(path);
            return m_cms.existsResource(path);
        } catch (CmsException ex) {
            return false;
        }
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#getItem(java.lang.String)
     */
    public I_CmsRepositoryItem getItem(String path) throws CmsException {

        path = validatePath(path);

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_READ_ITEM_1, path));
        }

        CmsResource res = m_cms.readResource(path, CmsResourceFilter.DEFAULT);

        CmsRepositoryItem item = new CmsRepositoryItem(res, m_cms);
        return item;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#getLock(java.lang.String)
     */
    public CmsRepositoryLockInfo getLock(String path) {

        try {
            CmsRepositoryLockInfo lockInfo = new CmsRepositoryLockInfo();

            path = validatePath(path);

            CmsResource res = m_cms.readResource(path, CmsResourceFilter.DEFAULT);

            // check user locks
            CmsLock cmsLock = m_cms.getLock(res);
            if (!cmsLock.isUnlocked()) {
                lockInfo.setPath(path);

                CmsUser owner = m_cms.readUser(cmsLock.getUserId());
                if (owner != null) {
                    lockInfo.setUsername(owner.getName());
                    lockInfo.setOwner(owner.getName() + "||" + owner.getEmail());
                }
                return lockInfo;
            }

            return null;
        } catch (CmsException ex) {

            // error occurred while finding locks
            // return null (no lock found)
            return null;
        }
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#list(java.lang.String)
     */
    public List<I_CmsRepositoryItem> list(String path) throws CmsException {

        List<I_CmsRepositoryItem> ret = new ArrayList<I_CmsRepositoryItem>();

        path = validatePath(path);

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_LIST_ITEMS_1, path));
        }

        List<CmsResource> resources = m_cms.getResourcesInFolder(path, CmsResourceFilter.DEFAULT);
        Iterator<CmsResource> iter = resources.iterator();
        while (iter.hasNext()) {
            CmsResource res = iter.next();

            if (!isFiltered(m_cms.getRequestContext().removeSiteRoot(res.getRootPath()))) {

                // open the original resource (for virtual files this is the resource in the VFS
                // which the virtual resource is based on)
                // this filters e.g. property files for resources that are filtered out and thus
                // should not be displayed
                CmsResource org = m_cms.readResource(res.getStructureId(), CmsResourceFilter.DEFAULT);
                if (!isFiltered(m_cms.getRequestContext().removeSiteRoot(org.getRootPath()))) {
                    ret.add(new CmsRepositoryItem(res, m_cms));
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_LIST_ITEMS_SUCESS_1, new Integer(ret.size())));
        }

        return ret;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#lock(java.lang.String, org.opencms.repository.CmsRepositoryLockInfo)
     */
    public boolean lock(String path, CmsRepositoryLockInfo lock) throws CmsException {

        path = validatePath(path);

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_LOCK_ITEM_1, path));
        }

        m_cms.lockResource(path);
        return true;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#move(java.lang.String, java.lang.String, boolean)
     */
    public void move(String src, String dest, boolean overwrite) throws CmsException {

        src = validatePath(src);
        dest = validatePath(dest);

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_MOVE_ITEM_2, src, dest));
        }

        // It is only possible in OpenCms to overwrite files.
        // Folder are not possible to overwrite.
        if (exists(dest)) {

            if (overwrite) {
                CmsResource srcRes = m_cms.readResource(src, CmsResourceFilter.DEFAULT);
                CmsResource destRes = m_cms.readResource(dest, CmsResourceFilter.DEFAULT);

                if ((srcRes.isFile()) && (destRes.isFile())) {

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.LOG_DELETE_DEST_0));
                    }

                    // delete existing resource
                    delete(dest);
                } else {

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.ERR_OVERWRITE_0));
                    }

                    throw new CmsException(Messages.get().container(Messages.ERR_OVERWRITE_0));
                }
            } else {

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.ERR_DEST_EXISTS_0));
                }

                throw new CmsVfsResourceAlreadyExistsException(Messages.get().container(Messages.ERR_DEST_EXISTS_0));
            }
        }

        // lock source resource
        m_cms.lockResource(src);

        // moving
        m_cms.moveResource(src, dest);

        // unlock destination resource
        m_cms.unlockResource(dest);
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#save(java.lang.String, java.io.InputStream, boolean)
     */
    public void save(String path, InputStream inputStream, boolean overwrite) throws CmsException, IOException {

        path = validatePath(path);
        byte[] content = CmsFileUtil.readFully(inputStream);

        try {
            CmsFile file = m_cms.readFile(path, CmsResourceFilter.DEFAULT);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_UPDATE_ITEM_1, path));
            }

            if (overwrite) {

                file.setContents(content);

                CmsLock lock = m_cms.getLock(file);

                // lock resource
                if (!lock.isInherited()) {
                    m_cms.lockResource(path);
                }

                // write file
                m_cms.writeFile(file);

                if (lock.isNullLock()) {
                    m_cms.unlockResource(path);
                }
            } else {

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.ERR_DEST_EXISTS_0));
                }

                throw new CmsVfsResourceAlreadyExistsException(Messages.get().container(Messages.ERR_DEST_EXISTS_0));
            }
        } catch (CmsVfsResourceNotFoundException ex) {

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_CREATE_ITEM_1, path));
            }

            int type = OpenCms.getResourceManager().getDefaultTypeForName(path).getTypeId();

            // create the file
            CmsResource res = m_cms.createResource(path, type, content, null);

            // unlock file after creation if lock is not inherited
            if (!m_cms.getLock(res).isInherited()) {
                m_cms.unlockResource(path);
            }
        }

    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#unlock(java.lang.String)
     */
    public void unlock(String path) {

        try {
            path = validatePath(path);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_UNLOCK_ITEM_1, path));
            }

            m_cms.unlockResource(path);
        } catch (CmsException ex) {

            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_UNLOCK_FAILED_0), ex);
            }
        }
    }

    /**
     * Adds the site root to the path name and checks then if the path
     * is filtered.<p>
     *
     * @see org.opencms.repository.A_CmsRepositorySession#isFiltered(java.lang.String)
     */
    @Override
    protected boolean isFiltered(String name) {

        boolean ret = super.isFiltered(m_cms.getRequestContext().addSiteRoot(name));
        if (ret) {

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.ERR_ITEM_FILTERED_1, name));
            }

        }

        return ret;
    }

    /**
     * Validates (translates) the given path and checks if it is filtered out.<p>
     *
     * @param path the path to validate
     *
     * @return the validated path
     *
     * @throws CmsSecurityException if the path is filtered out
     */
    private String validatePath(String path) throws CmsSecurityException {

        // Problems with spaces in new folders (default: "Neuer Ordner")
        // Solution: translate this to a correct name.
        String ret = m_cms.getRequestContext().getFileTranslator().translateResource(path);

        // add site root only works correct if system folder ends with a slash
        if (CmsResource.VFS_FOLDER_SYSTEM.equals(ret)) {
            ret = ret.concat("/");
        }

        // filter path
        if (isFiltered(ret)) {
            throw new CmsSecurityException(Messages.get().container(Messages.ERR_ITEM_FILTERED_1, path));
        }

        return ret;
    }
}
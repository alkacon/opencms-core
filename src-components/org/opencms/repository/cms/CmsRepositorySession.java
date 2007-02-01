/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/repository/cms/Attic/CmsRepositorySession.java,v $
 * Date   : $Date: 2007/02/01 10:08:18 $
 * Version: $Revision: 1.1.2.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2006 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.repository.cms;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.repository.CmsRepositoryException;
import org.opencms.repository.CmsRepositoryItemAlreadyExistsException;
import org.opencms.repository.CmsRepositoryItemNotFoundException;
import org.opencms.repository.CmsRepositoryLockInfo;
import org.opencms.repository.CmsRepositoryPermissionException;
import org.opencms.repository.I_CmsRepositoryItem;
import org.opencms.repository.I_CmsRepositorySession;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsFileUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * This is the session class to work with OpenCms. You should get an instance
 * of this class by calling {@link CmsRepository#login(String, String)}.<p>
 *
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.1.2.5 $
 * 
 * @since 6.5.6
 */
public class CmsRepositorySession implements I_CmsRepositorySession {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRepositorySession.class);

    /** The initialized CmsObject. */
    private final CmsObject m_cms;

    /**
     * Constructor with an initialized CmsObject to use.<p>
     * 
     * @param cms The initialized CmsObject
     */
    public CmsRepositorySession(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#copy(java.lang.String, java.lang.String, boolean)
     */
    public void copy(String src, String dest, boolean overwrite) throws CmsRepositoryException {

        // Problems with spaces in new folders (default: "Neuer Ordner")
        // Solution: translate this to a correct name.
        src = m_cms.getRequestContext().getFileTranslator().translateResource(src);
        dest = m_cms.getRequestContext().getFileTranslator().translateResource(dest);

        // It is only possible in OpenCms to overwrite files.
        // Folder are not possible to overwrite.
        try {
            if (exists(dest)) {

                if (overwrite) {
                    CmsResource srcRes = m_cms.readResource(src);
                    CmsResource destRes = m_cms.readResource(dest);

                    if ((srcRes.isFile()) && (destRes.isFile())) {

                        // delete existing resource
                        delete(dest);
                    } else {

                        // internal error (not possible)
                        throw new CmsRepositoryException();
                    }
                } else {
                    throw new CmsRepositoryItemAlreadyExistsException();
                }
            }

            // copy resource
            m_cms.copyResource(src, dest);

            // unlock destination resource
            m_cms.unlockResource(dest);
        } catch (CmsVfsResourceNotFoundException rnfex) {

            // Resource not found
            if (LOG.isDebugEnabled()) {
                LOG.debug(rnfex.getMessage());
            }

            throw new CmsRepositoryItemNotFoundException();
        } catch (CmsVfsResourceAlreadyExistsException raeex) {

            // Resource already exists
            if (LOG.isDebugEnabled()) {
                LOG.debug(raeex.getMessage());
            }

            throw new CmsRepositoryItemAlreadyExistsException();
        } catch (CmsSecurityException sex) {

            // Security issues
            if (LOG.isDebugEnabled()) {
                LOG.debug(sex.getMessage());
            }

            throw new CmsRepositoryPermissionException();
        } catch (CmsException ex) {

            // internal error
            if (LOG.isErrorEnabled()) {
                LOG.error(ex.getMessage());
            }

            throw new CmsRepositoryException();
        }
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#create(java.lang.String)
     */
    public void create(String path) throws CmsRepositoryException {

        try {

            // Problems with spaces in new folders (default: "Neuer Ordner")
            // Solution: translate this to a correct name.
            path = m_cms.getRequestContext().getFileTranslator().translateResource(path);
            
            // create the folder
            m_cms.createResource(path, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        } catch (CmsVfsResourceAlreadyExistsException raeex) {

            // Resource already exists
            if (LOG.isDebugEnabled()) {
                LOG.debug(raeex.getMessage());
            }

            throw new CmsRepositoryItemAlreadyExistsException();
        } catch (CmsSecurityException sex) {

            // Security issues
            if (LOG.isDebugEnabled()) {
                LOG.debug(sex.getMessage());
            }

            throw new CmsRepositoryPermissionException();
        } catch (CmsException ex) {

            // internal error
            if (LOG.isErrorEnabled()) {
                LOG.error(ex.getMessage());
            }

            throw new CmsRepositoryException();
        }

    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#create(java.lang.String, java.io.InputStream, boolean)
     */
    public void create(String path, InputStream inputStream, boolean overwrite) throws CmsRepositoryException {

        // check if file already exists and eventually delete it
        if (exists(path)) {
            if (overwrite) {
                delete(path);
            } else {
                throw new CmsRepositoryItemAlreadyExistsException();
            }
        }

        try {
            // Problems with spaces in new folders (default: "Neuer Ordner")
            // Solution: translate this to a correct name.
            path = m_cms.getRequestContext().getFileTranslator().translateResource(path);

            int type = OpenCms.getResourceManager().getDefaultTypeForName(path).getTypeId();
            byte[] content = CmsFileUtil.readFully(inputStream);

            // create the file
            m_cms.createResource(path, type, content, null);
            
            // unlock file after creation
            m_cms.unlockResource(path);
            
        } catch (CmsVfsResourceAlreadyExistsException raeex) {

            // Resource already exists
            if (LOG.isDebugEnabled()) {
                LOG.debug(raeex.getMessage());
            }

            throw new CmsRepositoryItemAlreadyExistsException();
        } catch (CmsSecurityException sex) {

            // Security issues
            if (LOG.isDebugEnabled()) {
                LOG.debug(sex.getMessage());
            }

            throw new CmsRepositoryPermissionException();
        } catch (Exception ex) {

            // internal error
            if (LOG.isErrorEnabled()) {
                LOG.error(ex.getMessage());
            }

            throw new CmsRepositoryException();
        }
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#delete(java.lang.String)
     */
    public void delete(String path) throws CmsRepositoryException {

        try {

            // Problems with spaces in new folders (default: "Neuer Ordner")
            // Solution: translate this to a correct name.
            path = m_cms.getRequestContext().getFileTranslator().translateResource(path);

            // lock resource
            m_cms.lockResource(path);

            // delete finally
            m_cms.deleteResource(path, CmsResource.DELETE_PRESERVE_SIBLINGS);
        } catch (CmsVfsResourceNotFoundException rnfex) {

            // Resource not found
            if (LOG.isDebugEnabled()) {
                LOG.debug(rnfex.getMessage());
            }

            throw new CmsRepositoryItemNotFoundException();
        } catch (CmsSecurityException sex) {

            // Security issues
            if (LOG.isDebugEnabled()) {
                LOG.debug(sex.getMessage());
            }

            throw new CmsRepositoryPermissionException();
        } catch (CmsException ex) {

            // internal error
            if (LOG.isErrorEnabled()) {
                LOG.error(ex.getMessage());
            }

            throw new CmsRepositoryException();
        }
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#exists(java.lang.String)
     */
    public boolean exists(String path) {

        // Problems with spaces in new folders (default: "Neuer Ordner")
        // Solution: translate this to a correct name.
        path = m_cms.getRequestContext().getFileTranslator().translateResource(path);
        return m_cms.existsResource(path);
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#getItem(java.lang.String)
     */
    public I_CmsRepositoryItem getItem(String path) throws CmsRepositoryException {

        try {
            // Problems with spaces in new folders (default: "Neuer Ordner")
            // Solution: translate this to a correct name.
            path = m_cms.getRequestContext().getFileTranslator().translateResource(path);

            CmsResource res = m_cms.readResource(path);

            CmsRepositoryItem item = new CmsRepositoryItem(res, m_cms);
            return item;
        } catch (CmsVfsResourceNotFoundException rnfex) {

            // Resource not found
            if (LOG.isDebugEnabled()) {
                LOG.debug(rnfex.getMessage());
            }

            throw new CmsRepositoryItemNotFoundException();
        } catch (CmsSecurityException sex) {

            // Security issues
            if (LOG.isDebugEnabled()) {
                LOG.debug(sex.getMessage());
            }

            throw new CmsRepositoryPermissionException();
        } catch (CmsException ex) {

            // internal error
            if (LOG.isErrorEnabled()) {
                LOG.error(ex.getMessage());
            }

            throw new CmsRepositoryException();
        }
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#getLock(java.lang.String)
     */
    public CmsRepositoryLockInfo getLock(String path) {

        try {
            CmsRepositoryLockInfo lockInfo = new CmsRepositoryLockInfo();

            // Problems with spaces in new folders (default: "Neuer Ordner")
            // Solution: translate this to a correct name.
            path = m_cms.getRequestContext().getFileTranslator().translateResource(path);

            CmsResource res = m_cms.readResource(path);

            // check system lock
            CmsLock sysLock = m_cms.getSystemLock(res);
            if (!sysLock.isUnlocked()) {
                lockInfo.setPath(path);

                CmsUser owner = m_cms.readUser(sysLock.getUserId());
                if (owner != null) {
                    lockInfo.setUsername(owner.getName());
                    lockInfo.setOwner(owner.getName() + "||" + owner.getEmail());
                }
                return lockInfo;
            }

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

            // error occured while finding locks
            // return null (no lock found)
            return null;
        }
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#list(java.lang.String)
     */
    public List list(String path) throws CmsRepositoryException {

        List ret = new ArrayList();

        try {

            // Problems with spaces in new folders (default: "Neuer Ordner")
            // Solution: translate this to a correct name.
            path = m_cms.getRequestContext().getFileTranslator().translateResource(path);

            // return empty list if resource is not a folder
            CmsResource folder = m_cms.readResource(path);
            if ((folder == null) || (!folder.isFolder())) {
                return ret;
            }

            List resources = m_cms.readResources(path, CmsResourceFilter.DEFAULT, false);
            Iterator iter = resources.iterator();
            while (iter.hasNext()) {
                CmsResource res = (CmsResource)iter.next();
                ret.add(res.getName());
            }

        } catch (CmsVfsResourceNotFoundException rnfex) {

            // Resource not found
            if (LOG.isDebugEnabled()) {
                LOG.debug(rnfex.getMessage());
            }

            throw new CmsRepositoryItemNotFoundException();
        } catch (CmsSecurityException sex) {

            // Security issues
            if (LOG.isDebugEnabled()) {
                LOG.debug(sex.getMessage());
            }

            throw new CmsRepositoryPermissionException();
        } catch (CmsException ex) {

            // internal error
            if (LOG.isErrorEnabled()) {
                LOG.error(ex.getMessage());
            }

            throw new CmsRepositoryException();
        }

        return ret;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#lock(java.lang.String, org.opencms.repository.CmsRepositoryLockInfo)
     */
    public boolean lock(String path, CmsRepositoryLockInfo lock) throws CmsRepositoryException {

        try {
            
            // Problems with spaces in new folders (default: "Neuer Ordner")
            // Solution: translate this to a correct name.
            path = m_cms.getRequestContext().getFileTranslator().translateResource(path);

            m_cms.lockResource(path);
            return true;
        } catch (CmsVfsResourceNotFoundException rnfex) {

            // Resource not found
            if (LOG.isDebugEnabled()) {
                LOG.debug(rnfex.getMessage());
            }

            throw new CmsRepositoryItemNotFoundException();
        } catch (CmsSecurityException sex) {

            // Security issues
            if (LOG.isDebugEnabled()) {
                LOG.debug(sex.getMessage());
            }

            throw new CmsRepositoryPermissionException();
        } catch (CmsException ex) {

            // internal error
            if (LOG.isErrorEnabled()) {
                LOG.error(ex.getMessage());
            }

            throw new CmsRepositoryException();
        }
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#move(java.lang.String, java.lang.String, boolean)
     */
    public void move(String src, String dest, boolean overwrite) throws CmsRepositoryException {

        // Problems with spaces in new folders (default: "Neuer Ordner")
        // Solution: translate this to a correct name.
        src = m_cms.getRequestContext().getFileTranslator().translateResource(src);
        dest = m_cms.getRequestContext().getFileTranslator().translateResource(dest);
        
        // It is only possible in OpenCms to overwrite files.
        // Folder are not possible to overwrite.
        try {
            if (exists(dest)) {

                if (overwrite) {
                    CmsResource srcRes = m_cms.readResource(src);
                    CmsResource destRes = m_cms.readResource(dest);

                    if ((srcRes.isFile()) && (destRes.isFile())) {

                        // delete existing resource
                        delete(dest);
                    } else {

                        throw new CmsRepositoryException();
                    }
                } else {
                    throw new CmsRepositoryItemAlreadyExistsException();
                }
            }

            // lock source resource
            m_cms.lockResource(src);

            // moving
            m_cms.moveResource(src, dest);

            // unlock destination resource
            m_cms.unlockResource(dest);
        } catch (CmsVfsResourceNotFoundException rnfex) {

            // Resource not found
            if (LOG.isDebugEnabled()) {
                LOG.debug(rnfex.getMessage());
            }

            throw new CmsRepositoryItemNotFoundException();
        } catch (CmsVfsResourceAlreadyExistsException raeex) {

            // Resource already exists
            if (LOG.isDebugEnabled()) {
                LOG.debug(raeex.getMessage());
            }

            throw new CmsRepositoryItemAlreadyExistsException();
        } catch (CmsException ex) {

            // internal error
            if (LOG.isErrorEnabled()) {
                LOG.error(ex.getMessage());
            }

            throw new CmsRepositoryException();
        }
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#unlock(java.lang.String)
     */
    public void unlock(String path) {

        try {
            // Problems with spaces in new folders (default: "Neuer Ordner")
            // Solution: translate this to a correct name.
            path = m_cms.getRequestContext().getFileTranslator().translateResource(path);

            m_cms.unlockResource(path);
        } catch (CmsException ex) {

            if (LOG.isErrorEnabled()) {
                LOG.error(ex.getMessage());
            }
        }
    }

}

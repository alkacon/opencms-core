/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/repository/cms/Attic/CmsRepositorySession.java,v $
 * Date   : $Date: 2007/01/25 09:09:27 $
 * Version: $Revision: 1.1.2.1 $
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
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.CmsException;
import org.opencms.repository.CmsRepositoryItemAlreadyExistsException;
import org.opencms.repository.CmsRepositoryItemNotFoundException;
import org.opencms.repository.CmsRepositoryLockInfo;
import org.opencms.repository.CmsRepositoryPermissionException;
import org.opencms.repository.I_CmsRepositoryItem;
import org.opencms.repository.I_CmsRepositorySession;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class CmsRepositorySession implements I_CmsRepositorySession {

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
    public void copy(String src, String dest, boolean overwrite)
    throws CmsRepositoryItemNotFoundException, CmsRepositoryPermissionException,
    CmsRepositoryItemAlreadyExistsException {

        try {

            if (exists(dest)) {
                if (overwrite) {
                    delete(dest);
                } else {
                    throw new CmsRepositoryItemAlreadyExistsException();
                }
            }
            m_cms.copyResource(src, dest);
        } catch (CmsException ex) {
            throw new CmsRepositoryItemNotFoundException();
        }
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#create(java.lang.String)
     */
    public void create(String path) throws CmsRepositoryItemAlreadyExistsException, CmsRepositoryPermissionException {

        try {
            path = m_cms.getRequestContext().getFileTranslator().translateResource(path);
            m_cms.createResource(path, CmsResourceTypeFolder.RESOURCE_TYPE_ID);
        } catch (CmsException ex) {
            throw new CmsRepositoryItemAlreadyExistsException();
        }

    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#create(java.lang.String, java.io.InputStream, boolean)
     */
    public void create(String path, InputStream inputStream, boolean overwrite)
    throws CmsRepositoryItemAlreadyExistsException, CmsRepositoryPermissionException {

        // TODO Auto-generated method stub

    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#delete(java.lang.String)
     */
    public void delete(String path) throws CmsRepositoryItemNotFoundException, CmsRepositoryPermissionException {

        try {
            m_cms.deleteResource(path, CmsResource.DELETE_PRESERVE_SIBLINGS);
        } catch (CmsException ex) {
            throw new CmsRepositoryItemNotFoundException();
        }

    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#exists(java.lang.String)
     */
    public boolean exists(String path) {

        return m_cms.existsResource(path);
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#getItem(java.lang.String)
     */
    public I_CmsRepositoryItem getItem(String path)
    throws CmsRepositoryItemNotFoundException, CmsRepositoryPermissionException {

        CmsRepositoryItem item = new CmsRepositoryItem();
        try {
            CmsResource res = m_cms.readResource(path);

            item.setCollection(res.isFolder());
            item.setContentLength(res.getLength());
            item.setCreationDate(res.getDateCreated());
            item.setLastModifiedDate(res.getDateLastModified());
            item.setName(m_cms.getRequestContext().removeSiteRoot(res.getRootPath()));

            return item;
        } catch (CmsException ex) {
            throw new CmsRepositoryItemNotFoundException();
        }
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#getLock(java.lang.String)
     */
    public CmsRepositoryLockInfo getLock(String path) {

        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#list(java.lang.String)
     */
    public List list(String path) throws CmsRepositoryItemNotFoundException, CmsRepositoryPermissionException {

        List ret = new ArrayList();

        try {

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

        } catch (CmsException ex) {
            throw new CmsRepositoryItemNotFoundException();
        }

        return ret;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#lock(java.lang.String, org.opencms.repository.CmsRepositoryLockInfo)
     */
    public boolean lock(String path, CmsRepositoryLockInfo lock)
    throws CmsRepositoryItemNotFoundException, CmsRepositoryPermissionException {

        try {
            m_cms.lockResource(path);
        } catch (CmsException ex) {
            throw new CmsRepositoryItemNotFoundException();
        }

        return false;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#move(java.lang.String, java.lang.String, boolean)
     */
    public void move(String src, String dest, boolean overwrite)
    throws CmsRepositoryItemNotFoundException, CmsRepositoryPermissionException,
    CmsRepositoryItemAlreadyExistsException {

        try {
            if (exists(dest)) {
                if (overwrite) {
                    delete(dest);
                } else {
                    throw new CmsRepositoryItemAlreadyExistsException();
                }
            }
            m_cms.moveResource(src, dest);
        } catch (CmsException ex) {
            throw new CmsRepositoryItemNotFoundException();
        }
    }

    /**
     * @see org.opencms.repository.I_CmsRepositorySession#unlock(java.lang.String)
     */
    public void unlock(String path) {

        // TODO Auto-generated method stub

    }

}

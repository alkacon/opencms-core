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
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResource.CmsResourceCopyMode;
import org.opencms.file.CmsResource.CmsResourceDeleteMode;
import org.opencms.file.CmsResourceFilter;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;

import java.util.List;

/**
 * Default abstract implementation of the interface {@link I_CmsResourceWrapper}.<p>
 *
 * This class returns for all methods that the action is not handled by the
 * resource wrapper.<p>
 *
 * Subclasses can only implement those methods where they want to change the default
 * behaviour.<p>
 *
 * @since 6.5.6
 */
public abstract class A_CmsResourceWrapper implements I_CmsResourceWrapper {

    /** Is handled by this resource wrapper. */
    protected boolean m_isWrappedResource;

    /**
     * @see org.opencms.file.wrapper.I_CmsResourceWrapper#addResourcesToFolder(org.opencms.file.CmsObject, java.lang.String, org.opencms.file.CmsResourceFilter)
     */
    public List<CmsResource> addResourcesToFolder(CmsObject cms, String resourcename, CmsResourceFilter filter)
    throws CmsException {

        if (m_isWrappedResource) {
            return cms.getResourcesInFolder(resourcename, filter);
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.I_CmsResourceWrapper#configure(java.lang.String)
     */
    public void configure(String configString) {

        // ignore

    }

    /**
     * @see org.opencms.file.wrapper.I_CmsResourceWrapper#copyResource(org.opencms.file.CmsObject, java.lang.String, java.lang.String, org.opencms.file.CmsResource.CmsResourceCopyMode)
     */
    public boolean copyResource(CmsObject cms, String source, String destination, CmsResourceCopyMode siblingMode)
    throws CmsException, CmsIllegalArgumentException {

        if (m_isWrappedResource) {
            cms.copyResource(source, destination, siblingMode);
            return true;
        }
        return false;
    }

    /**
     * @see org.opencms.file.wrapper.I_CmsResourceWrapper#createResource(org.opencms.file.CmsObject, java.lang.String, int, byte[], java.util.List)
     */
    public CmsResource createResource(
        CmsObject cms,
        String resourcename,
        int type,
        byte[] content,
        List<CmsProperty> properties) throws CmsException, CmsIllegalArgumentException {

        if (m_isWrappedResource) {
            return cms.createResource(resourcename, type, content, properties);
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.I_CmsResourceWrapper#deleteResource(org.opencms.file.CmsObject, java.lang.String, org.opencms.file.CmsResource.CmsResourceDeleteMode)
     */
    public boolean deleteResource(CmsObject cms, String resourcename, CmsResourceDeleteMode siblingMode)
    throws CmsException {

        if (m_isWrappedResource) {
            cms.deleteResource(resourcename, siblingMode);
            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.I_CmsResourceWrapper#getLock(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public CmsLock getLock(CmsObject cms, CmsResource resource) throws CmsException {

        if (m_isWrappedResource) {
            return cms.getLock(resource);
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.I_CmsResourceWrapper#lockResource(org.opencms.file.CmsObject, java.lang.String, boolean)
     */
    public boolean lockResource(CmsObject cms, String resourcename, boolean temporary) throws CmsException {

        if (m_isWrappedResource) {
            if (temporary) {
                cms.lockResourceTemporary(resourcename);
            } else {
                cms.lockResource(resourcename);
            }
            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.I_CmsResourceWrapper#moveResource(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public boolean moveResource(CmsObject cms, String source, String destination)
    throws CmsException, CmsIllegalArgumentException {

        if (m_isWrappedResource) {
            cms.moveResource(source, destination);
            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.I_CmsResourceWrapper#readFile(org.opencms.file.CmsObject, java.lang.String, org.opencms.file.CmsResourceFilter)
     */
    public CmsFile readFile(CmsObject cms, String resourcename, CmsResourceFilter filter) throws CmsException {

        if (m_isWrappedResource) {
            return cms.readFile(resourcename, filter);
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.I_CmsResourceWrapper#readResource(org.opencms.file.CmsObject, java.lang.String, org.opencms.file.CmsResourceFilter)
     */
    public CmsResource readResource(CmsObject cms, String resourcename, CmsResourceFilter filter) throws CmsException {

        if (m_isWrappedResource) {
            return cms.readResource(resourcename, filter);
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.I_CmsResourceWrapper#restoreLink(org.opencms.file.CmsObject, java.lang.String)
     */
    public String restoreLink(CmsObject cms, String uri) {

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.I_CmsResourceWrapper#rewriteLink(CmsObject, CmsResource)
     */
    public String rewriteLink(CmsObject cms, CmsResource res) {

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.I_CmsResourceWrapper#unlockResource(CmsObject, String)
     */
    public boolean unlockResource(CmsObject cms, String resourcename) throws CmsException {

        if (m_isWrappedResource) {
            cms.unlockResource(resourcename);
            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.I_CmsResourceWrapper#wrapResource(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public CmsResource wrapResource(CmsObject cms, CmsResource resource) {

        return resource;
    }

    /**
     * @see org.opencms.file.wrapper.I_CmsResourceWrapper#writeFile(org.opencms.file.CmsObject, org.opencms.file.CmsFile)
     */
    public CmsFile writeFile(CmsObject cms, CmsFile resource) throws CmsException {

        if (m_isWrappedResource) {
            return cms.writeFile(resource);
        }

        return null;
    }

}

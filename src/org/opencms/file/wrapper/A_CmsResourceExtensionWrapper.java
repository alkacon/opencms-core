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
 * Abstract base class which implements {@link I_CmsResourceWrapper} and
 * makes it possible to add and remove file extensions to resources.<p>
 *
 * @since 6.5.6
 */
public abstract class A_CmsResourceExtensionWrapper extends A_CmsResourceWrapper {

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#copyResource(org.opencms.file.CmsObject, java.lang.String, java.lang.String, org.opencms.file.CmsResource.CmsResourceCopyMode)
     */
    @Override
    public boolean copyResource(CmsObject cms, String source, String destination, CmsResourceCopyMode siblingMode)
    throws CmsException, CmsIllegalArgumentException {

        CmsResource res = getResource(cms, source);
        if (res != null) {

            cms.copyResource(
                CmsResourceWrapperUtils.removeFileExtension(cms, source, getExtension()),
                CmsResourceWrapperUtils.removeFileExtension(cms, destination, getExtension()),
                siblingMode);
            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#createResource(org.opencms.file.CmsObject, java.lang.String, int, byte[], java.util.List)
     */
    @Override
    public CmsResource createResource(
        CmsObject cms,
        String resourcename,
        int type,
        byte[] content,
        List<CmsProperty> properties) throws CmsException, CmsIllegalArgumentException {

        if (checkTypeId(type)) {

            return cms.createResource(
                CmsResourceWrapperUtils.removeFileExtension(cms, resourcename, getExtension()),
                type,
                content,
                properties);
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#deleteResource(CmsObject, String, org.opencms.file.CmsResource.CmsResourceDeleteMode)
     */
    @Override
    public boolean deleteResource(CmsObject cms, String resourcename, CmsResourceDeleteMode siblingMode)
    throws CmsException {

        CmsResource res = getResource(cms, resourcename);
        if (res != null) {

            cms.deleteResource(
                CmsResourceWrapperUtils.removeFileExtension(cms, resourcename, getExtension()),
                siblingMode);
            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#getLock(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    @Override
    public CmsLock getLock(CmsObject cms, CmsResource resource) throws CmsException {

        if (checkTypeId(resource.getTypeId())) {

            CmsWrappedResource wrap = new CmsWrappedResource(resource);
            wrap.setRootPath(CmsResourceWrapperUtils.removeFileExtension(cms, resource.getRootPath(), getExtension()));

            return cms.getLock(wrap.getResource());
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.I_CmsResourceWrapper#isWrappedResource(CmsObject, CmsResource)
     */
    public boolean isWrappedResource(CmsObject cms, CmsResource res) {

        return checkTypeId(res.getTypeId());
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#lockResource(org.opencms.file.CmsObject, java.lang.String, boolean)
     */
    @Override
    public boolean lockResource(CmsObject cms, String resourcename, boolean temporary) throws CmsException {

        CmsResource res = getResource(cms, resourcename);
        if (res != null) {
            String path = cms.getRequestContext().removeSiteRoot(res.getRootPath());
            if (temporary) {
                cms.lockResourceTemporary(path);
            } else {
                cms.lockResource(path);
            }
            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#moveResource(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    @Override
    public boolean moveResource(CmsObject cms, String source, String destination)
    throws CmsException, CmsIllegalArgumentException {

        CmsResource res = getResource(cms, source);
        if (res != null) {

            // check if destination name is valid
            if (!destination.endsWith("." + getExtension())) {
                throw new CmsIllegalArgumentException(
                    Messages.get().container(Messages.ERR_BAD_RESOURCE_EXTENSION_1, destination));
            }

            cms.moveResource(
                CmsResourceWrapperUtils.removeFileExtension(cms, source, getExtension()),
                CmsResourceWrapperUtils.removeFileExtension(cms, destination, getExtension()));
            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#readFile(CmsObject, String, CmsResourceFilter)
     */
    @Override
    public CmsFile readFile(CmsObject cms, String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource res = getResource(cms, resourcename, filter);
        if (res != null) {

            CmsFile file = cms.readFile(res);
            CmsWrappedResource wrap = new CmsWrappedResource(file);
            wrap.setRootPath(CmsResourceWrapperUtils.addFileExtension(cms, res.getRootPath(), getExtension()));

            return wrap.getFile();
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#readResource(CmsObject, String, CmsResourceFilter)
     */
    @Override
    public CmsResource readResource(CmsObject cms, String resourcename, CmsResourceFilter filter) {

        CmsResource res = getResource(cms, resourcename, filter);
        if (res != null) {

            CmsWrappedResource wrap = new CmsWrappedResource(res);
            wrap.setRootPath(CmsResourceWrapperUtils.addFileExtension(cms, res.getRootPath(), getExtension()));

            return wrap.getResource();
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#restoreLink(org.opencms.file.CmsObject, java.lang.String)
     */
    @Override
    public String restoreLink(CmsObject cms, String uri) {

        CmsResource res = getResource(cms, uri);
        if (res != null) {
            return res.getRootPath();
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#rewriteLink(CmsObject, CmsResource)
     */
    @Override
    public String rewriteLink(CmsObject cms, CmsResource res) {

        if (checkTypeId(res.getTypeId())) {
            return CmsResourceWrapperUtils.addFileExtension(cms, res.getRootPath(), getExtension());
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#unlockResource(org.opencms.file.CmsObject, java.lang.String)
     */
    @Override
    public boolean unlockResource(CmsObject cms, String resourcename) throws CmsException {

        CmsResource res = getResource(cms, resourcename);
        if (res != null) {
            cms.unlockResource(cms.getRequestContext().removeSiteRoot(res.getRootPath()));
            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#wrapResource(CmsObject, CmsResource)
     */
    @Override
    public CmsResource wrapResource(CmsObject cms, CmsResource res) {

        if (checkTypeId(res.getTypeId())) {

            CmsWrappedResource wrap = new CmsWrappedResource(res);
            wrap.setRootPath(CmsResourceWrapperUtils.addFileExtension(cms, res.getRootPath(), getExtension()));

            return wrap.getResource();
        }

        return res;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#writeFile(org.opencms.file.CmsObject, org.opencms.file.CmsFile)
     */
    @Override
    public CmsFile writeFile(CmsObject cms, CmsFile resource) throws CmsException {

        if (checkTypeId(resource.getTypeId())) {

            CmsWrappedResource wrap = new CmsWrappedResource(resource);
            wrap.setRootPath(CmsResourceWrapperUtils.removeFileExtension(cms, resource.getRootPath(), getExtension()));

            return cms.writeFile(wrap.getFile());
        }

        return null;
    }

    /**
     * Checks if the type id belongs to this resource wrapper.<p>
     *
     * @param typeId the type id to check
     * @return true if the type id belongs to this wrapper otherwise false
     */
    protected abstract boolean checkTypeId(int typeId);

    /**
     * Returns the extension to add and/or remove to/from the resource.<p>
     *
     * @return the extension to use
     */
    protected abstract String getExtension();

    /**
     * Trys to read the resourcename after removing the file extension and return the
     * resource if the type id is correct.<p>
     *
     * @param cms the initialized CmsObject
     * @param resourcename the name of the resource to read
     *
     * @return the resource or null if not found
     */
    private CmsResource getResource(CmsObject cms, String resourcename) {

        return getResource(cms, resourcename, CmsResourceFilter.DEFAULT);
    }

    /**
     * Trys to read the resourcename after removing the file extension and return the
     * resource if the type id is correct.<p>
     *
     * @param cms the initialized CmsObject
     * @param resourcename the name of the resource to read
     * @param filter the resource filter to use while reading
     *
     * @return the resource or null if not found
     */
    private CmsResource getResource(CmsObject cms, String resourcename, CmsResourceFilter filter) {

        CmsResource res = null;

        try {
            res = cms.readResource(
                CmsResourceWrapperUtils.removeFileExtension(cms, resourcename, getExtension()),
                filter);
        } catch (CmsException ex) {
            return null;
        }

        if (checkTypeId(res.getTypeId())) {
            return res;
        }

        return null;
    }

}

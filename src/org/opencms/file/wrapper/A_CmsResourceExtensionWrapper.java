/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/wrapper/A_CmsResourceExtensionWrapper.java,v $
 * Date   : $Date: 2007/02/15 15:54:20 $
 * Version: $Revision: 1.1.4.2 $
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
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsResource.CmsResourceCopyMode;
import org.opencms.file.CmsResource.CmsResourceDeleteMode;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;

import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Abstract class which makes it possible to add or remove a file extension to a resource.<p>
 * 
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.1.4.2 $
 * 
 * @since 6.5.6
 */
public abstract class A_CmsResourceExtensionWrapper extends A_CmsResourceWrapper {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsResourceExtensionWrapper.class);

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#copyResource(org.opencms.file.CmsObject, java.lang.String, java.lang.String, org.opencms.file.CmsResource.CmsResourceCopyMode)
     */
    public boolean copyResource(CmsObject cms, String source, String destination, CmsResourceCopyMode siblingMode)
    throws CmsException, CmsIllegalArgumentException {

        CmsResource res = getResource(cms, source);
        if (res != null) {

            cms.copyResource(removeFileExtension(cms, source), removeFileExtension(cms, destination), siblingMode);
            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#createResource(org.opencms.file.CmsObject, java.lang.String, int, byte[], java.util.List)
     */
    public CmsResource createResource(CmsObject cms, String resourcename, int type, byte[] content, List properties)
    throws CmsException, CmsIllegalArgumentException {

        if (checkTypeId(type)) {

            // TODO: addFileExtension(cms, res);
            return cms.createResource(removeFileExtension(cms, resourcename), type, content, properties);
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#deleteResource(CmsObject, String, org.opencms.file.CmsResource.CmsResourceDeleteMode)
     */
    public boolean deleteResource(CmsObject cms, String resourcename, CmsResourceDeleteMode siblingMode)
    throws CmsException {

        CmsResource res = getResource(cms, resourcename);
        if (res != null) {

            cms.deleteResource(removeFileExtension(cms, resourcename), siblingMode);
            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#getLock(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public CmsLock getLock(CmsObject cms, CmsResource resource) throws CmsException {

        if (checkTypeId(resource.getTypeId())) {

            return cms.getLock(removeFileExtension(cms, resource));
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#getSystemLock(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public CmsLock getSystemLock(CmsObject cms, CmsResource resource) throws CmsException {

        if (checkTypeId(resource.getTypeId())) {

            return cms.getSystemLock(removeFileExtension(cms, resource));
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
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#lockResource(org.opencms.file.CmsObject, java.lang.String)
     */
    public boolean lockResource(CmsObject cms, String resourcename) throws CmsException {

        CmsResource res = getResource(cms, resourcename);
        if (res != null) {

            cms.lockResource(removeFileExtension(cms, resourcename));
            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#moveResource(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public boolean moveResource(CmsObject cms, String source, String destination)
    throws CmsException, CmsIllegalArgumentException {

        CmsResource res = getResource(cms, source);
        if (res != null) {

            // check if destination name is valid
            if (!destination.endsWith("." + getExtension())) {
                throw new CmsIllegalArgumentException(Messages.get().container(
                    Messages.ERR_BAD_RESOURCE_EXTENSION_1,
                    destination));
            }

            cms.moveResource(removeFileExtension(cms, source), removeFileExtension(cms, destination));
            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#readFile(CmsObject, String, CmsResourceFilter)
     */
    public CmsFile readFile(CmsObject cms, String resourcename, CmsResourceFilter filter) throws CmsException {

        CmsResource res = getResource(cms, resourcename, filter);
        if (res != null) {
            CmsFile file = CmsFile.upgrade(res, cms);
            return addFileExtension(cms, file);
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#readResource(CmsObject, String, CmsResourceFilter)
     */
    public CmsResource readResource(CmsObject cms, String resourcename, CmsResourceFilter filter) {

        CmsResource res = getResource(cms, resourcename, filter);
        if (res != null) {
            return addFileExtension(cms, res);
        }

        return null;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#unlockResource(org.opencms.file.CmsObject, java.lang.String)
     */
    public boolean unlockResource(CmsObject cms, String resourcename) throws CmsException {

        CmsResource res = getResource(cms, resourcename);
        if (res != null) {
            cms.unlockResource(resourcename);
            return true;
        }

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#wrapResource(CmsObject, CmsResource)
     */
    public CmsResource wrapResource(CmsObject cms, CmsResource res) {

        if (checkTypeId(res.getTypeId())) {

            return addFileExtension(cms, res);
        }

        return res;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#writeFile(org.opencms.file.CmsObject, org.opencms.file.CmsFile)
     */
    public CmsFile writeFile(CmsObject cms, CmsFile resource) throws CmsException {

        if (checkTypeId(resource.getTypeId())) {

            // TODO: addFileExtension(cms, file);
            return cms.writeFile(removeFileExtension(cms, resource));
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
     * Returns the extension to add or remove to the resource.<p>
     * 
     * @return the extension to use
     */
    protected abstract String getExtension();

    /**
     * Adds the new file extension (.jsp) to the path of the given file and returns it.<p>
     * 
     * @param cms the initialized CmsObject
     * @param file the file where to change the path
     * 
     * @return the resource with the changed path
     * 
     * @see #addFileExtension(CmsObject, CmsResource)
     */
    private CmsFile addFileExtension(CmsObject cms, CmsFile file) {

        return new CmsFile(
            file.getStructureId(),
            file.getResourceId(),
            file.getContentId(),
            addFileExtension(cms, file.getRootPath()),
            file.getTypeId(),
            file.getFlags(),
            file.getProjectLastModified(),
            file.getState(),
            file.getDateCreated(),
            file.getUserCreated(),
            file.getDateLastModified(),
            file.getUserLastModified(),
            file.getDateReleased(),
            file.getDateExpired(),
            file.getSiblingCount(),
            file.getLength(),
            file.getContents());
    }

    /**
     * Adds the new file extension (.jsp) to the path of the given resource and returns it.<p>
     * 
     * @param cms the initialized CmsObject
     * @param res the resource where to change the path
     * 
     * @return the resource with the changed path
     */
    private CmsResource addFileExtension(CmsObject cms, CmsResource res) {

        return new CmsResource(
            res.getStructureId(),
            res.getResourceId(),
            addFileExtension(cms, res.getRootPath()),
            res.getTypeId(),
            res.isFolder(),
            res.getFlags(),
            res.getProjectLastModified(),
            res.getState(),
            res.getDateCreated(),
            res.getUserCreated(),
            res.getDateLastModified(),
            res.getUserLastModified(),
            res.getDateReleased(),
            res.getDateExpired(),
            res.getSiblingCount(),
            res.getLength());
    }

    /**
     * Adds the file extension ".jsp" to the resource name.<p>
     * 
     * @param cms the actual CmsObject
     * @param resourcename the name of the resource where to add the file extension
     * 
     * @return the resource name with the added file extension
     */
    private String addFileExtension(CmsObject cms, String resourcename) {

        if (!resourcename.endsWith("." + getExtension())) {
            String name = resourcename + "." + getExtension();
            int count = 0;
            while (cms.existsResource(name)) {
                count++;
                name = resourcename + "." + count + "." + getExtension();
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_CHANGED_FILE_EXTENSION_2, resourcename, name));
            }

            return name;
        }

        return resourcename;
    }

    /**
     * Trys to read the resourcename after removing the file extension and return the
     * resource of the type id is correct.<p>
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
     * resource of the type id is correct.<p>
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
            res = cms.readResource(removeFileExtension(cms, resourcename), filter);
        } catch (CmsException ex) {
            return null;
        }

        if (checkTypeId(res.getTypeId())) {
            return res;
        }

        return null;
    }

    /**
     * Removes the added file extension (.jsp) from the path of the given file and returns it.<p>
     * 
     * @param cms the initialized CmsObject
     * @param file the file where to remove the file extension from the path
     * 
     * @return the file with the changed path
     * 
     * @see #removeFileExtension(CmsObject, CmsResource)
     */
    private CmsFile removeFileExtension(CmsObject cms, CmsFile file) {

        return new CmsFile(
            file.getStructureId(),
            file.getResourceId(),
            file.getContentId(),
            removeFileExtension(cms, file.getRootPath()),
            file.getTypeId(),
            file.getFlags(),
            file.getProjectLastModified(),
            file.getState(),
            file.getDateCreated(),
            file.getUserCreated(),
            file.getDateLastModified(),
            file.getUserLastModified(),
            file.getDateReleased(),
            file.getDateExpired(),
            file.getSiblingCount(),
            file.getLength(),
            file.getContents());
    }

    /**
     * Removes the added file extension (.jsp) from the path of the given resource and returns it.<p>
     * 
     * @param cms the initialized CmsObject
     * @param res the resource where to remove the file extension from the path
     * 
     * @return the resource with the changed path
     */
    private CmsResource removeFileExtension(CmsObject cms, CmsResource res) {

        return new CmsResource(
            res.getStructureId(),
            res.getResourceId(),
            removeFileExtension(cms, res.getRootPath()),
            res.getTypeId(),
            res.isFolder(),
            res.getFlags(),
            res.getProjectLastModified(),
            res.getState(),
            res.getDateCreated(),
            res.getUserCreated(),
            res.getDateLastModified(),
            res.getUserLastModified(),
            res.getDateReleased(),
            res.getDateExpired(),
            res.getSiblingCount(),
            res.getLength());
    }

    /**
     * Removes the added file extension from the resource name.<p>
     * 
     * <ul>
     * <li>If there is only one extension, nothing will be removed.</li>
     * <li>If there are two extensions, the last one will be removed.</li>
     * <li>If there are more than two extensions the last one will be removed and
     * if then the last extension is a number, the extension with the number
     * will be removed too.</li>
     * </ul>
     * 
     * @param cms the initialized CmsObject
     * @param resourcename the resource name to remove the file extension from
     * 
     * @return the resource name without the removed file extension
     */
    private String removeFileExtension(CmsObject cms, String resourcename) {

        if (resourcename.equals("")) {
            resourcename = "/";
        }

        // get the filename without the path
        String name = CmsResource.getName(resourcename);

        String[] tokens = name.split("\\.");
        String suffix = null;

        // check if there is more than one extension
        if (tokens.length > 2) {

            // check if last extension is "jsp"
            if (getExtension().equalsIgnoreCase(tokens[tokens.length - 1])) {

                suffix = "." + getExtension();

                // check if there is another extension with a numeric index 
                if (tokens.length > 3) {

                    try {
                        int index = Integer.valueOf(tokens[tokens.length - 2]).intValue();

                        suffix = "." + index + suffix;
                    } catch (NumberFormatException ex) {
                        // noop
                    }
                }
            }
        } else if (tokens.length == 2) {

            // there is only one extension!! 
            // only remove the last extension, if the resource without the extension exists
            // and the extension is ".jsp"
            if ((cms.existsResource(CmsResource.getFolderPath(resourcename) + tokens[0]))
                && (getExtension().equals(tokens[1]))) {
                suffix = "." + tokens[1];
            }
        }

        if (suffix != null) {

            String path = resourcename.substring(0, resourcename.length() - suffix.length());
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_CHANGED_FILE_EXTENSION_2, resourcename, path));
            }

            return path;
        }

        return resourcename;
    }

}

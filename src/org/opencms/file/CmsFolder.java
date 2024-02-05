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

package org.opencms.file;

import org.opencms.db.CmsResourceState;
import org.opencms.db.CmsSecurityManager;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import org.apache.commons.logging.Log;

/**
 * A folder resource in the OpenCms VFS.<p>
 *
 * A folder resource is a CmsResource object that can contain sub-resources.<p>
 *
 * @since 6.0.0
 */
public class CmsFolder extends CmsResource {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSecurityManager.class);
    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 5527163725725725452L;

    /**
     * Constructor, creates a new CmsFolder Object from the given CmsResource.<p>
     *
     * @param resource the base resource object to create a folder from
     */
    public CmsFolder(CmsResource resource) {

        this(
            resource.getStructureId(),
            resource.getResourceId(),
            resource.getRootPath(),
            resource.getTypeId(),
            resource.getFlags(),
            resource.getProjectLastModified(),
            resource.getState(),
            resource.getDateCreated(),
            resource.getUserCreated(),
            resource.getDateLastModified(),
            resource.getUserLastModified(),
            resource.getDateReleased(),
            resource.getDateExpired(),
            resource.getVersion());
    }

    /**
     * Constructor, creates a new CmsFolder object.<p>
     *
     * @param structureId the id of this resources structure record
     * @param resourceId the id of this resources resource record
     * @param path the filename of this resouce
     * @param type the type of this resource
     * @param flags the flags of this resource
     * @param projectId the project id this resource was last modified in
     * @param state the state of this resource
     * @param dateCreated the creation date of this resource
     * @param userCreated the id of the user who created this resource
     * @param dateLastModified the date of the last modification of this resource
     * @param userLastModified the id of the user who did the last modification of this resource    * @param size the size of the file content of this resource
     * @param dateReleased the release date of this resource
     * @param dateExpired the expiration date of this resource
     * @param version the version number of this resource
     */
    public CmsFolder(
        CmsUUID structureId,
        CmsUUID resourceId,
        String path,
        int type,
        int flags,
        CmsUUID projectId,
        CmsResourceState state,
        long dateCreated,
        CmsUUID userCreated,
        long dateLastModified,
        CmsUUID userLastModified,
        long dateReleased,
        long dateExpired,
        int version) {

        super(
            structureId,
            resourceId,
            path,
            type,
            true,
            flags,
            projectId,
            state,
            dateCreated,
            userCreated,
            dateLastModified,
            userLastModified,
            dateReleased,
            dateExpired,
            1,
            -1,
            -1,
            version);
    }

    /**
     * Returns <code>true</code> if the given resource size describes a folder type.<p>
     *
     * This is <code>true</code> in case <code>size &lt; 0</code>.<p>
     *
     * @param size the resource size to check
     *
     * @return true if the given resource size describes a folder type or false if it is no folder
     */
    public static final boolean isFolderSize(long size) {

        return (size < 0);
    }

    /**
     * Returns <code>true</code> if the given resource type id describes a folder type.<p>
     *
     * @param typeId the resource type id to check
     *
     * @return true if the given resource type id describes a folder type or false if it is no folder or an unknown type.
     */
    public static final boolean isFolderType(int typeId) {

        try {
            return OpenCms.getResourceManager().getResourceType(typeId).isFolder();
        } catch (CmsLoaderException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(Messages.ERR_UNKNOWN_RESOURCE_TYPE_1, Integer.valueOf(typeId)), e);
            }
        }
        return false;
    }

    /**
     * Returns <code>true</code> if the given resource type name describes a folder type.<p>
     *
     * @param typeName the resource type name to check
     *
     * @return true if the given resource type name describes a folder type
     */
    public static final boolean isFolderType(String typeName) {

        try {
            return OpenCms.getResourceManager().getResourceType(typeName).isFolder();
        } catch (CmsLoaderException e) {
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_UNKNOWN_RESOURCE_TYPE_1, typeName),
                e);
        }
    }

    /**
     * Returns a clone of this Objects instance.<p>
     *
     * @return a clone of this instance
     */
    @Override
    public Object clone() {

        CmsResource clone = new CmsFolder(
            getStructureId(),
            getResourceId(),
            getRootPath(),
            getTypeId(),
            getFlags(),
            getProjectLastModified(),
            getState(),
            getDateCreated(),
            getUserCreated(),
            getDateLastModified(),
            getUserLastModified(),
            getDateReleased(),
            getDateExpired(),
            getVersion());

        if (isTouched()) {
            clone.setDateLastModified(getDateLastModified());
        }

        return clone;
    }

    /**
     * A folder does always have the content date <code>-1</code>.<p>
     *
     * @see org.opencms.file.CmsResource#getDateContent()
     */
    @Override
    public long getDateContent() {

        return -1;
    }

    /**
     * A folder does always have length <code>-1</code>.<p>
     *
     * @see org.opencms.file.CmsResource#getLength()
     */
    @Override
    public int getLength() {

        return -1;
    }

    /**
     * Since this is a folder, not a file, <code>false</code> is always returned.<p>
     *
     * @see org.opencms.file.CmsResource#isFile()
     */
    @Override
    public boolean isFile() {

        return false;
    }

    /**
     * Since this is a folder, <code>true</code> is always returned.<p>
     *
     * @see org.opencms.file.CmsResource#isFolder()
     */
    @Override
    public boolean isFolder() {

        return true;
    }

    /**
     * @see org.opencms.file.CmsResource#isTemporaryFile()
     */
    @Override
    public boolean isTemporaryFile() {

        return false;
    }
}
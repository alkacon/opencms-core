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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
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

package org.opencms.file.history;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.security.CmsPrincipal;
import org.opencms.util.CmsUUID;

/**
 * A historical version of a file in the OpenCms VFS resource history.<p>
 *
 * @since 6.9.1
 */
public class CmsHistoryFile extends CmsFile implements I_CmsHistoryResource {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 4073076414399668662L;

    /** The structure id of the parent of this historical resource. */
    private CmsUUID m_parentId;

    /** The publish tag of this historical resource. */
    private int m_publishTag;

    /** The version number of the structure part for this historical resource. */
    private int m_structureVersion;

    /** The version number of the resource part for this historical resource. */
    private int m_resourceVersion;

    /**
     * Creates a dummy historical file from the given resource.<p>
     *
     * @param resource the resource to wrap
     */
    public CmsHistoryFile(CmsResource resource) {

        super(resource);
        m_publishTag = 0;
        m_parentId = null;
    }

    /**
     * Constructor from a history resource.<p>
     *
     * @param resource the base history resource
     */
    public CmsHistoryFile(I_CmsHistoryResource resource) {

        this(
            resource.getPublishTag(),
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
            resource.getLength(),
            resource.getDateContent(),
            resource.getVersion(),
            resource.getParentId(),
            resource.isFile() ? ((CmsFile)resource).getContents() : null,
            resource.getResourceVersion(),
            resource.getStructureVersion());
    }

    /**
     * Default Constructor.<p>
     *
     * @param publishTag the publish tag of this historical resource
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
     * @param userLastModified the id of the user who did the last modification of this resource
     * @param dateReleased the release date of this resource
     * @param dateExpired the expiration date of this resource
     * @param size the size of the file content of this resource
     * @param dateContent the date of the last modification of the content of this resource
     * @param version the version number of this resource
     * @param parentId structure id of the parent of this historical resource
     * @param content the content of this version
     * @param resourceVersion the version number of the resource part for this historical resource
     * @param structureVersion the version number of the structure part for this historical resource
     */
    public CmsHistoryFile(
        int publishTag,
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
        int size,
        long dateContent,
        int version,
        CmsUUID parentId,
        byte[] content,
        int resourceVersion,
        int structureVersion) {

        super(
            structureId,
            resourceId,
            path,
            type,
            flags,
            projectId,
            state,
            dateCreated,
            userCreated,
            dateLastModified,
            userLastModified,
            dateReleased,
            dateExpired,
            0,
            size,
            dateContent,
            version,
            content);

        m_publishTag = publishTag;
        m_parentId = parentId;
        m_resourceVersion = resourceVersion;
        m_structureVersion = structureVersion;
    }

    /**
     * Returns a clone of this Objects instance.<p>
     *
     * @return a clone of this instance
     */
    @Override
    public Object clone() {

        return new CmsHistoryFile(
            getPublishTag(),
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
            getLength(),
            getDateContent(),
            getVersion(),
            getParentId(),
            getContents(),
            getResourceVersion(),
            getStructureVersion());
    }

    /**
     * @see org.opencms.file.history.I_CmsHistoryResource#getParentId()
     */
    public CmsUUID getParentId() {

        return m_parentId;
    }

    /**
     * @see org.opencms.file.history.I_CmsHistoryResource#getPublishTag()
     */
    public int getPublishTag() {

        return m_publishTag;
    }

    /**
     * Returns the name of the user that created this resource.<p>
     *
     * @param cms the current cms context
     *
     * @return the name of the user that created this resource
     */
    public String getUserCreatedName(CmsObject cms) {

        try {
            return CmsPrincipal.readPrincipalIncludingHistory(cms, getUserCreated()).getName();
        } catch (CmsException e) {
            return getUserCreated().toString();
        }
    }

    /**
     * Returns the name of the user that last modified this resource.<p>
     *
     * @param cms the current cms context
     *
     * @return the name of the user that last modified this resource
     */
    public String getUserLastModifiedName(CmsObject cms) {

        try {
            return CmsPrincipal.readPrincipalIncludingHistory(cms, getUserLastModified()).getName();
        } catch (CmsException e) {
            return getUserLastModified().toString();
        }
    }

    /**
     * @see org.opencms.file.history.I_CmsHistoryResource#getResourceVersion()
     */
    public int getResourceVersion() {

        return m_resourceVersion;
    }

    /**
     * @see org.opencms.file.history.I_CmsHistoryResource#getStructureVersion()
     */
    public int getStructureVersion() {

        return m_structureVersion;
    }

    /**
     * @see org.opencms.file.CmsResource#toString()
     */
    @Override
    public String toString() {

        StringBuffer result = new StringBuffer();

        result.append("[");
        result.append(super.toString());
        result.append(", resource version: ");
        result.append(m_resourceVersion);
        result.append(", structure version ");
        result.append(m_structureVersion);
        result.append(", parent id: ");
        result.append(m_parentId);
        result.append(", publish tag: ");
        result.append(m_publishTag);
        result.append("]");

        return result.toString();
    }
}

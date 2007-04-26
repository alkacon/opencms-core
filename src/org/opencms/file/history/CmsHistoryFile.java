/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/history/CmsHistoryFile.java,v $
 * Date   : $Date: 2007/04/26 14:31:12 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.file.history;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsResource;
import org.opencms.util.CmsUUID;

import java.io.Serializable;

/**
 * A historical version of a file in the OpenCms VFS resource history.<p>
 *
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.1 $
 * 
 * @since 6.9.1
 */
public class CmsHistoryFile extends CmsFile implements I_CmsHistoryResource, Cloneable, Serializable, Comparable {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 4073076414399668662L;

    /** The publish tag of this historical resource. */
    private int m_publishTag;

    /** The name of the user that created this resource, useful if user deleted. */
    private String m_userCreatedName;

    /** The name of the user that last modified this resource, useful if user deleted. */
    private String m_userLastModifiedName;

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
     * @param userCreatedName the name of the user who created this resource 
     * @param dateLastModified the date of the last modification of this resource
     * @param userLastModified the id of the user who did the last modification of this resource
     * @param userLastModifiedName the name of the user who did the last modification of this resource
     * @param dateReleased the release date of this resource
     * @param dateExpired the expiration date of this resource
     * @param size the size of the file content of this resource
     * @param dateContent the date of the last modification of the content of this resource 
     * @param version the version number of this resource
     * @param content the content of this version
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
        String userCreatedName,
        long dateLastModified,
        CmsUUID userLastModified,
        String userLastModifiedName,
        long dateReleased,
        long dateExpired,
        int size,
        long dateContent,
        int version,
        byte[] content) {

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
        m_userCreatedName = userCreatedName;
        m_userLastModifiedName = userLastModifiedName;
    }

    /**
     * Constructor from a history resource.<p>
     * 
     * @param resource the base history resource
     */
    public CmsHistoryFile(I_CmsHistoryResource resource) {

        this(
            resource.getPublishTag(),
            resource.getResource().getStructureId(),
            resource.getResource().getResourceId(),
            resource.getResource().getRootPath(),
            resource.getResource().getTypeId(),
            resource.getResource().getFlags(),
            resource.getResource().getProjectLastModified(),
            resource.getResource().getState(),
            resource.getResource().getDateCreated(),
            resource.getResource().getUserCreated(),
            resource.getCreatedByName(),
            resource.getResource().getDateLastModified(),
            resource.getResource().getUserLastModified(),
            resource.getLastModifiedByName(),
            resource.getResource().getDateReleased(),
            resource.getResource().getDateExpired(),
            resource.getResource().getLength(),
            resource.getResource().getDateContent(),
            resource.getVersion(),
            resource.getResource().isFile() ? ((CmsFile)resource.getResource()).getContents() : null);
    }

    /**
     * Returns a clone of this Objects instance.<p>
     * 
     * @return a clone of this instance
     */
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
            getCreatedByName(),
            getDateLastModified(),
            getUserLastModified(),
            getLastModifiedByName(),
            getDateReleased(),
            getDateExpired(),
            getLength(),
            getDateContent(),
            getVersion(),
            getContents());
    }

    /**
     * @see org.opencms.file.history.I_CmsHistoryResource#getBackupId()
     * 
     * @deprecated this field has been removed
     */
    public CmsUUID getBackupId() {

        return new CmsUUID();
    }

    /**
     * @see org.opencms.file.history.I_CmsHistoryResource#getCreatedByName()
     */
    public String getCreatedByName() {

        return m_userCreatedName;
    }

    /**
     * @see org.opencms.file.history.I_CmsHistoryResource#getLastModifiedByName()
     */
    public String getLastModifiedByName() {

        return m_userLastModifiedName;
    }

    /**
     * @see org.opencms.file.history.I_CmsHistoryResource#getPublishTag()
     */
    public int getPublishTag() {

        return m_publishTag;
    }

    /**
     * @see org.opencms.file.history.I_CmsHistoryResource#getPublishTagId()
     * 
     * @deprecated use {@link #getPublishTag()} instead
     */
    public int getPublishTagId() {

        return getPublishTag();
    }

    /**
     * @see org.opencms.file.history.I_CmsHistoryResource#getResource()
     */
    public CmsResource getResource() {

        return this;
    }
}

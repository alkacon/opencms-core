/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsBackupResource.java,v $
 * Date   : $Date: 2011/03/23 14:51:10 $
 * Version: $Revision: 1.25 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.file;

import org.opencms.db.CmsResourceState;
import org.opencms.file.history.CmsHistoryFile;
import org.opencms.util.CmsUUID;

/**
 * A backup resource for the OpenCms VFS resource history.<p>
 *
 * Backup resources are basic resources that contain additional information 
 * used to describe the backup state.
 * Backup resource extend CmsFile since the might contain binary content,
 * but they can also in fact be backup resources for a folder.<p>
 * 
 * Backup resources contain the names of the users that 
 * created or last modified the resource as a String because 
 * a user id might have been deleted.<p>
 *
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.25 $
 * 
 * @since 6.0.0 
 * 
 * @deprecated use {@link org.opencms.file.history.CmsHistoryFile} instead
 */
public class CmsBackupResource extends CmsHistoryFile {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -6659773406054276891L;

    private CmsUUID m_backupId;

    private String m_userCreatedName;
    private String m_userLastModifiedName;

    /**
     * Constructor, creates a new CmsBackupResource object.<p>
     * 
     * @param backupId the backup id of this backup resource     
     * @param tagId the tag id of this backup resource    
     * @param version the version of this backup resource   
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
     * @param parentId structure id of the parent of this historical resource
     * @param content the binary content data of this file
     */
    public CmsBackupResource(
        CmsUUID backupId,
        int tagId,
        int version,
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
        CmsUUID parentId,
        byte[] content) {

        super(
            tagId,
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
            size,
            dateContent,
            version,
            parentId,
            content,
            version,
            0);

        m_backupId = backupId;
        m_userCreatedName = userCreatedName;
        m_userLastModifiedName = userLastModifiedName;
    }

    /**
     * @see org.opencms.file.history.CmsHistoryFile#getBackupId()
     */
    @Override
    public CmsUUID getBackupId() {

        return m_backupId;
    }

    /**
     * @see org.opencms.file.history.CmsHistoryFile#getCreatedByName()
     */
    @Override
    public String getCreatedByName() {

        return m_userCreatedName;
    }

    /**
     * @see org.opencms.file.history.CmsHistoryFile#getLastModifiedByName()
     */
    @Override
    public String getLastModifiedByName() {

        return m_userLastModifiedName;
    }
}

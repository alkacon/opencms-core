/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsFolder.java,v $
 * Date   : $Date: 2004/05/21 15:14:28 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
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

import org.opencms.util.CmsUUID;

import java.io.Serializable;

/**
 * A folder object in OpenCms.<p>
 * 
 * A folder object is a CmsResource object that can contain sub-resources.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * 
 * @version $Revision: 1.26 
 */
public class CmsFolder extends CmsResource implements Cloneable, Serializable, Comparable {

    /**
     * Constructor, creates a new CmsFolder Object from the given CmsResource.<p> 
     * 
     * @param resource the base resource object to create a folder from
     */
    public CmsFolder(CmsResource resource) {

        this(
            resource.getStructureId(),
            resource.getResourceId(),
            resource.getParentStructureId(),
            resource.getFileId(),
            resource.getName(),
            resource.getType(),
            resource.getFlags(),
            resource.getProjectLastModified(),
            resource.getState(),
            resource.getDateCreated(),
            resource.getUserCreated(),
            resource.getDateLastModified(),
            resource.getUserLastModified(),
            resource.getLinkCount(),
            resource.getDateReleased(),
            resource.getDateExpired());
        if (resource.hasFullResourceName()) {
            setFullResourceName(resource.getRootPath());
        }
    }

    /**
     * Constructor, creates a new CmsFolder object.<p>
     *
     * @param structureId the id of this resources structure record
     * @param resourceId the id of this resources resource record
     * @param parentId the id of this resources parent folder
     * @param fileId the id of this resources content record
     * @param name the filename of this resouce
     * @param type the type of this resource
     * @param flags the flags of this resource
     * @param projectId the project id this resource was last modified in
     * @param state the state of this resource
     * @param dateCreated the creation date of this resource
     * @param userCreated the id of the user who created this resource
     * @param dateLastModified the date of the last modification of this resource
     * @param userLastModified the id of the user who did the last modification of this resource    * @param size the size of the file content of this resource
     * @param linkCount the count of all siblings of this resource 
     * @param dateReleased the release date of this resource
     * @param dateExpired the expiration date of this resource
     */
    public CmsFolder(
        CmsUUID structureId,
        CmsUUID resourceId,
        CmsUUID parentId,
        CmsUUID fileId,
        String name,
        int type,
        int flags,
        int projectId,
        int state,
        long dateCreated,
        CmsUUID userCreated,
        long dateLastModified,
        CmsUUID userLastModified,
        int linkCount,
        long dateReleased,
        long dateExpired) {

        super(
            structureId,
            resourceId,
            parentId,
            fileId,
            name,
            type,
            flags,
            projectId,
            state,
            -1,
            dateCreated,
            userCreated,
            dateLastModified,
            userLastModified,
            dateReleased,
            dateExpired,
            linkCount,
            -1);
    }

    /**
     * Returns a clone of this Objects instance.<p>
     * 
     * @return a clone of this instance
     */
    public Object clone() {

        return new CmsFolder(
            getStructureId(),
            getResourceId(),
            getParentStructureId(),
            getFileId(),
            getName(),
            getType(),
            getFlags(),
            getProjectLastModified(),
            getState(),
            getDateCreated(),
            getUserCreated(),
            getDateLastModified(),
            getUserLastModified(),
            getLinkCount(),
            getDateReleased(),
            getDateExpired());
    }

    /**
     * Sets the resource name including the path, ensuring that the name ends with a "/".<p>
     * 
     * @param fullResourceName the resource name including the path
     */
    public void setFullResourceName(String fullResourceName) {

        if (!CmsResource.isFolder(fullResourceName)) {
            super.setFullResourceName(fullResourceName + "/");
        } else {
            super.setFullResourceName(fullResourceName);
        }
    }
}
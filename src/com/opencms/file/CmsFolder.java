/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsFolder.java,v $
* Date   : $Date: 2003/05/15 12:39:34 $
* Version: $Revision: 1.16 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
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
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.file;

import com.opencms.core.*;
import com.opencms.flex.util.CmsUUID;

import java.io.*;
/**
 * Describes a folder in the Cms.
 *
 * @author Michael Emmerich
 * @version $Revision: 1.16 $ $Date: 2003/05/15 12:39:34 $
 */
public class CmsFolder extends CmsResource implements I_CmsConstants,
                                                      Cloneable,
                                                      Serializable {

     /**
      * Constructor, creates a new CmsFolder object.
      *
      * @param resourceId The database Id.
      * @param parentId The database Id of the parent folder.
      * @param fileId The id of the content.
      * @param resourceName The name (including complete path) of the resouce.
      * @param resourceType The type of this resource.
      * @param rescourceFlags The flags of thei resource.
      * @param userId The id of the user of this resource.
      * @param groupId The id of the group of this resource.
      * @param projectId The project id this resource belongs to.
      * @param accessFlags The access flags of this resource.
      * @param state The state of this resource.
      * @param lockedBy The user id of the user who has locked this resource.
      * @param dateCreated The creation date of this resource.
      * @param dateLastModified The date of the last modification of the resource.
      * @param resourceLastModifiedBy The user who changed the file.
      */
     public CmsFolder(CmsUUID resourceId, CmsUUID parentId,CmsUUID fileId,
                        String resourceName, int resourceType, int resourceFlags,
                        CmsUUID userId, CmsUUID groupId, int projectId,
                        int accessFlags, int state, CmsUUID lockedByUserId,
                        long dateCreated, long dateLastModified
                        ,CmsUUID resourceLastModifiedByUserId, int lockedInProject){

        // create the CmsResource.
        super(resourceId, parentId,fileId,resourceName,
              resourceType,resourceFlags,
              userId,groupId,projectId,
              accessFlags,state,lockedByUserId,
              C_UNKNOWN_LAUNCHER_ID,C_UNKNOWN_LAUNCHER,
              dateCreated,dateLastModified,
              resourceLastModifiedByUserId,-1, lockedInProject);
   }
    /**
    * Clones the CmsFolder by creating a new CmsFolder.
    * @return Cloned CmsFolder.
    */
    public Object clone() {
        return new CmsFolder(this.getResourceId(), this.getParentId(), this.getFileId(),
                             new String(this.getResourceName()),this.getType(),
                             this.getFlags(), this.getOwnerId(), this.getGroupId(),
                             this.getProjectId(),this.getAccessFlags(),
                             this.getState(),this.isLockedBy(),this.getDateCreated(),
                             this.getDateLastModified(), this.getResourceLastModifiedBy(),
                             this.getLockedInProject());
    }
}

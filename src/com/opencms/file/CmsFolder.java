/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsFolder.java,v $
 * Date   : $Date: 2000/02/20 11:42:09 $
 * Version: $Revision: 1.7 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.file;

import com.opencms.core.*;
/**
 * This class describes a folder in the Cms.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.7 $ $Date: 2000/02/20 11:42:09 $
 */
public class CmsFolder extends CmsResource implements I_CmsConstants,
                                                      Cloneable {
     
     /**
      * Constructor, creates a new CmsFolder object.
      * 
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
      */
     public CmsFolder(String resourceName, int resourceType, int resourceFlags,
                        int userId, int groupId, int projectId,
                        int accessFlags, int state, int lockedBy,
                        long dateCreated, long dateLastModified){
        
        // create the CmsResource.
        super(resourceName,resourceType,resourceFlags,
              userId,groupId,projectId,
              accessFlags,state,lockedBy,
              C_UNKNOWN_LAUNCHER_ID,C_UNKNOWN_LAUNCHER,
              dateCreated,dateLastModified,-1);         
   }
    
    /** 
    * Clones the CmsFolder by creating a new CmsFolder.
    * @return Cloned CmsFolder.
    */
    public Object clone() {
        return new CmsFolder(new String(this.getAbsolutePath()),this.getType(),
                             this.getFlags(), this.getOwnerId(), this.getGroupId(),
                             this.getProjectId(),this.getAccessFlags(), 
                             this.getState(),this.isLockedBy(),this.getDateCreated(),
                             this.getDateLastModified());                             
    }
}

package com.opencms.file;

import com.opencms.core.*;
/**
 * This class describes a folder in the Cms.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.4 $ $Date: 2000/01/10 18:15:04 $
 */
public class CmsFolder extends CmsResource implements I_CmsConstants
{
     
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
    
}

package com.opencms.file;

/**
 * This class describes a folder in the Cms.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.2 $ $Date: 1999/12/20 17:19:47 $
 */
public class CmsFolder extends CmsResource
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
      * @param launcherType The launcher that is require to process this recource.
      * @param launcherClassname The name of the Java class invoked by the launcher.
      * @param dateCreated The creation date of this resource.
      * @param dateLastModified The date of the last modification of the resource.
      */
     public CmsFolder(String resourceName, int resourceType, int resourceFlags,
                        int userId, int groupId, int projectId,
                        int accessFlags, int state, int lockedBy,
                        int launcherType, String launcherClassname,
                        long dateCreated, long dateLastModified){
        
        // create the CmsResource.
        super(resourceName,resourceType,resourceFlags,
              userId,groupId,projectId,
              accessFlags,state,lockedBy,
              launcherType,launcherClassname,
              dateCreated,dateLastModified);         
   }
    
}

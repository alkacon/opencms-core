package com.opencms.file;

import java.util.*;
import com.opencms.core.*;

/**
 * This class describes a resource in the Cms.
 * This resource can be a A_CmsFile or a A_CmsFolder.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.2 $ $Date: 1999/12/20 17:19:47 $
 */
 class CmsResource extends A_CmsResource implements I_CmsConstants {
     
     /**
      * Definition of the root resource.
      */
     private static String C_ROOT="/";
     
     /**
      * The name of this resource.
      */
     private String m_resourceName;
  
     /**
      * The type of this resource.
      */
     private int m_resourceType;
     
     /**
      * The flags of this resource.
      */
     private int m_resourceFlags;
     
     /**
      * The project id this recouce belongs to.
      */
     private int m_projectId;

      /**
      * The owner id of this resource.
      */
     private int m_userId;
     
     /**
      * The group id of this resource.
      */
     private int m_groupId;
      
     /**
      * The access flags of this resource.
      */
     private int m_accessFlags;
      
     /**
      * The creation date of this resource.
      */
     private long m_dateCreated;
      
     /**
      * The date of the last modification of this resource.
      */
     private long m_dateLastModified;
      
      /**
      * The size of the file content.
      */
      protected int m_size;
      
      /**
      * The state of this resource. <br>
      * A resource can have the following states:
      * <ul>
	  * <li> unchanged </li>
	  * <li> changed </li>
	  * <li> new </li>
	  * <li> deleted </li>
	  * </ul>
      */
     private int m_state;
      
     /**
      * The user id of the usrer who locked this resource.
      */
     private int m_lockedBy;   
     
     /**
      * The type of the launcher which is used to process this resource.
      */
     private int launcherType;
     
     /**
      * The Java class thas is invoked by the launcher to process this resource.
      */
     private String launcherClassname;
     
     
     
     /**
      * Constructor, creates a new CmsRecource object.
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
     public CmsResource(String resourceName, int resourceType, int resourceFlags,
                        int userId, int groupId, int projectId,
                        int accessFlags, int state, int lockedBy,
                        int launcherType, String launcherClassname,
                        long dateCreated, long dateLastModified){
         
        m_resourceName=resourceName;
        m_resourceType=resourceType;
        m_resourceFlags=resourceFlags;
        m_userId=userId;
        m_groupId=groupId;
        m_projectId=projectId;
        m_accessFlags=accessFlags;
        m_state=state;
        m_lockedBy=lockedBy;
        m_dateCreated=dateCreated;
        m_dateLastModified=dateLastModified;
        m_size=0;
            
     }

         
	/**
	 * Returns the absolute path for this resource.<BR/>
	 * Example: retuns /system/def/language.cms
	 * 
	 * @return the absolute path for this resource.
	 */
     public String getAbsolutePath(){
         return m_resourceName;
     }
	
	/**
	 * Returns the absolute path of the parent.<BR/>
	 * Example: /system/def has the parent /system/<BR/>
	 * / has no parent
	 * 
	 * @return the parent absolute path, or null if this is the root-resource.
	 */
     public String getParent(){
         String parent=null;
         // check if this is the root resource
         if (!m_resourceName.equals(C_ROOT)) {
                parent=m_resourceName.substring(0,m_resourceName.length()-1);
                parent=parent.substring(0,parent.lastIndexOf("/")+1);         
         }
         return parent;
     }
	
	/**
	 * Returns the path for this resource.<BR/>
	 * Example: retuns /system/def/ for the
	 * resource /system/def/language.cms
	 * 
	 * @return the path for this resource.
	 */
     public String getPath() {
         return m_resourceName.substring(0,m_resourceName.lastIndexOf("/")+1);
     }
	
	/**
	 * Returns the name of this resource.<BR/>
	 * Example: retuns language.cms for the
	 * resource /system/def/language.cms
	 * 
	 * @return the name of this resource.
	 */
     public String getName() {
         String name= null;
         // check if this is a file
         if (!m_resourceName.endsWith("/")) {
             name=m_resourceName.substring(m_resourceName.lastIndexOf("/")+1,
                                           m_resourceName.length());
         }else{
              name=m_resourceName.substring(0,m_resourceName.length()-1);
              name=name.substring(name.lastIndexOf("/")+1,
                                  name.length());
         }
             
         return name;
     }
    	
	/**
	 * Gets the type id for this resource.
	 * 
	 * @return the type id of this resource.
	 */
     public int getType() {
       return m_resourceType;
     }

	/**
	 * Returns the date of the creation for this resource.
	 * 
	 * @return the date of the creation for this resource.
	 */
     public long getDateCreated() {
         return m_dateCreated;
     }
	
	/**
	 * Returns the date of the last modification for this resource.
	 * 
	 * @return the date of the last modification for this resource.
	 */
     public long getDateLastModified() {
         return m_dateLastModified;
     }

	/**
	 * Returns a string-representation for this object.
	 * This can be used for debugging.
	 * 
	 * @return string-representation for this object.
	 */
      public String toString() {
        StringBuffer output=new StringBuffer();
        output.append("[Resource]:");
        output.append(m_resourceName);
        output.append(" , User=");
        output.append(m_userId);
        output.append(" , Group=");
        output.append(m_userId);
        output.append(" : Flags=");
        output.append(m_accessFlags);
        return output.toString();
      }
                                 
	
	/**
	 * Compares the overgiven object with this object.
	 * 
	 * @return true, if the object is identically else it returns false.
	 */
      public boolean equals(Object obj) {
        boolean equal=false;
        // check if the object is a CmsResource object
        if (obj instanceof CmsResource) {
            // same ID than the current user?
            if (((CmsResource)obj).getAbsolutePath().equals(m_resourceName)){
                equal = true;
            }
        }
        return equal;
      }
   
	/**
	 * Returns the userid of the resource owner.
	 * 
	 * @return the userid of the resource owner.
	 */
      int getOwnerId() {
         return m_userId;
      }
	
	/**
	 * Returns the groupid of this resource.
	 * 
	 * @return the groupid of this resource.
	 */
      int getGroupId() {
         return  m_groupId;
      }
	
	/**
	 * Returns the accessflags of this resource.
	 * 
	 * @return the accessflags of this resource.
	 */
      public int getFlags() {
         return m_accessFlags;
             }
	  	
	/**
	 * Determines, if this resource is a folder.
	 * 
	 * @return true, if this resource is a folder, else it returns false.
	 */
      public boolean isFolder(){
         boolean isFolder=false;
         if (m_resourceName.endsWith("/")){
             isFolder=true;
         }
         return isFolder;
          
      }
         

	/**
	 * Determines, if this resource is a file.
	 * 
	 * @return true, if this resource is a file, else it returns false.
	 */
      public boolean isFile() {
         boolean isFile=true;
         if (m_resourceName.endsWith("/")){
             isFile=false;
         }
         return isFile;
      }
	
	/**
	 * Returns the state of this resource.<BR/>
	 * This may be C_STATE_UNCHANGED, C_STATE_CHANGED, C_STATE_NEW or C_STATE_DELETED.
	 * 
	 * @return the state of this resource.
	 */
      public int getState() {
      return m_state;
      }
	
	/**
	 * Determines, if this resource is locked by a user.
	 * 
	 * @return true, if this resource is locked by a user, else it returns false.
	 */
      public boolean isLocked() {
          boolean isLocked=true;
          //check if the user id in the locked by field is the unknown user id.
          if (m_lockedBy == C_UNKNOWN_ID) {
              isLocked=false;
          }
          return isLocked;
      }

	/**
	 * Returns the user idthat locked this resource.
	 * 
	 * @return the user id that locked this resource.
	 * If this resource is free it returns the unknown user id.
	 */
      public int isLockedBy() {
        return m_lockedBy;
      }

	/**
	 * Returns the project id for this resource.
	 * 
	 * @return the project id for this resource.
	 */
      int getProjectId() {
          return m_projectId;
      }
      
     /**
	 * Gets the length of the content (filesize).
	 * 
	 * @return the length of the content.
	 */
     public int getLength() {
        return m_size;
     }
  
	
}

/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsResource.java,v $
* Date   : $Date: 2003/06/13 10:04:20 $
* Version: $Revision: 1.49 $
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

import com.opencms.core.I_CmsConstants;
import com.opencms.flex.util.CmsUUID;

import java.io.Serializable;

/**
 * This is the base class for all resources, files, and folders in the Cms.
 *
 * @author Michael Emmerich
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.49 $ $Date: 2003/06/13 10:04:20 $
 */
public class CmsResource implements I_CmsConstants, Cloneable, Serializable {
    
     /**
      * The ID of the file header database entry.
      */
     private CmsUUID m_resourceId;

     /**
      * The ID of the parent's file tree/hierarchy database entry.
      */
     private CmsUUID m_parentId;

     /**
      * The ID of the file content database entry.
      */
     private CmsUUID m_fileId;
     
     /**
      * The ID of the file tree/hierarchy database entry.
      */
     private CmsUUID m_structureId;

     /**
      * The name of this resource.
      */
     private String m_resourceName;

     /**
      * The type of this resource.
      */
     private int m_resourceType;

     /**
      * The flags of this resource ( not used yet; the Accessflags are stored in m_accessFlags).
      */
     private int m_resourceFlags;

     /**
      * The project id this recouce belongs to.
      */
     private int m_projectId;

      /**
      * The owner  of this resource.
      */
     private CmsUUID m_userId;

     /**
      * The group  of this resource.
      */
     private CmsUUID m_groupId;

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
     
      /** Boolean flag whether the timestamp of this resource was modified by a touch command. */
      private boolean m_isTouched;

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
     private CmsUUID m_lockedByUserId;

     /**
      * The type of the launcher which is used to process this resource.
      */
     private int m_launcherType;

     /**
      * The Java class thas is invoked by the launcher to process this resource.
      */
     private String m_launcherClassname;

     /**
      * The UserId of the user who modified this resource last.
      */
     private CmsUUID m_resourceLastModifiedByUserId;

     /**
      * The projectId of the project where the resource was locked or modified in
      */
    private int m_lockedInProject;
     /**
      * Constructor, creates a new CmsRecource object.
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
      * @param launcherType The launcher that is require to process this recource.
      * @param launcherClassname The name of the Java class invoked by the launcher.
      * @param dateCreated The creation date of this resource.
      * @param dateLastModified The date of the last modification of the resource.
      * @param resourceLastModifiedBy The user who changed the file.
      */
     public CmsResource(CmsUUID structureId, CmsUUID resourceId,
                        CmsUUID parentId,CmsUUID fileId,
                        String resourceName, int resourceType,
                        int resourceFlags, CmsUUID userId, CmsUUID groupId,
                        int projectId, int accessFlags, int state,
                        CmsUUID lockedByUserId, int launcherType,
                        String launcherClassname, long dateCreated,
                        long dateLastModified,CmsUUID resourceLastModifiedByUserId, int size, int lockedInProject){
        m_structureId = structureId;
        m_resourceId = resourceId;
        m_parentId = parentId;
        m_fileId = fileId;
        m_resourceName=resourceName;
        m_resourceType=resourceType;
        m_resourceFlags=resourceFlags;
        m_userId=userId;
        m_groupId=groupId;
        m_projectId=projectId;
        m_accessFlags=accessFlags;
        m_launcherType=launcherType;
        m_launcherClassname=launcherClassname;
        m_state=state;
        m_lockedByUserId=lockedByUserId;
        m_dateCreated=dateCreated;
        m_dateLastModified=dateLastModified;
        m_resourceLastModifiedByUserId = resourceLastModifiedByUserId;
        m_size=size;
        m_lockedInProject=lockedInProject;
        m_isTouched = false;
     }
    /**
     * Clones the CmsResource by creating a new CmsObject.
     * @return Cloned CmsObject.
     */
    public Object clone() {
        return new CmsResource(m_structureId, m_resourceId,m_parentId,
                               m_fileId, m_resourceName, m_resourceType,
                               m_resourceFlags, m_userId, m_groupId,
                               m_projectId, m_accessFlags, m_state,
                               m_lockedByUserId, m_launcherType,
                               m_launcherClassname, m_dateCreated,
                               m_dateLastModified,m_resourceLastModifiedByUserId, m_size, m_lockedInProject);
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
            if (((CmsResource)obj).getResourceName().equals(m_resourceName)){
                equal = true;
            }
        }
        return equal;
      }
      
    /**
     * Returns the absolute path of this resource,
     * e.g. <code>/system/workplace/action/index.html</code><p>
     *
     * @return the absolute path for this resource
     */
    public String getAbsolutePath() {
        return getAbsolutePath(m_resourceName);
    }

    /**
     * Returns the absolute path of the provided resource,
     * e.g. <code>/system/workplace/action/index.html</code><p>
     *
     * @return the absolute path of the provided resource
     */
    public static String getAbsolutePath(String resourceName) {
        if (resourceName == null) return null;
        return resourceName.substring(resourceName.indexOf("/", resourceName.indexOf("/", 1) + 1));
    }

    /**
     * Returns the root name of this resource,
     * e.g. <code>/default/vfs</code><p>
     *
     * @return the root name for this resource
     */
    public String getRootName() {
        int rootIndex = m_resourceName.indexOf("/", m_resourceName.indexOf("/", 1) + 1);
        return m_resourceName.substring(0, rootIndex);
    }
    
    /**
     * Returns the resource name of this resource,
     * e.g. <code>/default/vfs/system/workplace/action/index.html</code><p>
     *
     * @return the resource name for this resource.
     */
    public String getResourceName() {
        return m_resourceName;
    }
     
    /**
     * Returns the accessflags of this resource.
     *
     * @return the accessflags of this resource.
     */
      public int getAccessFlags() {
         return m_accessFlags;
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
      * Sets the date of the last modification for this resource.
      */
     public void setDateLastModified( long time ) {
        m_isTouched = true;
        m_dateLastModified = time;
     }
          
    /**
     * Gets the ID of the file content database entry.
     *
     * @return the ID of the file content database entry
     */
     public CmsUUID getFileId(){
        return m_fileId;
     }
     
    /**
     * Returns the flags of this resource ( not used yet; the Accessflags are served in getAccessFlags).
     *
     * @return the flags of this resource (this are not the AccessFlags!!).
     */
      public int getFlags() {
         return m_resourceFlags;
      }
    /**
     * Creates a Unix-Style string of access rights from the access right flag of a
     * CmsResource
     *
     * @return String of access rights
     */
    public String getFlagString()
    {
        String str = "NOT YET";
// 		TODO: check if neccessary and reimplement using acl
/*
        str += ((m_accessFlags & C_PERMISSION_READ)>0?"r":"-");
        str += ((m_accessFlags & C_PERMISSION_WRITE)>0?"w":"-");
        str += ((m_accessFlags & C_PERMISSION_VIEW)>0?"v":"-");
        str += ((m_accessFlags & C_ACCESS_GROUP_READ)>0?"r":"-");
        str += ((m_accessFlags & C_ACCESS_GROUP_WRITE)>0?"w":"-");
        str += ((m_accessFlags & C_ACCESS_GROUP_VISIBLE)>0?"v":"-");
        str += ((m_accessFlags & C_ACCESS_PUBLIC_READ)>0?"r":"-");
        str += ((m_accessFlags & C_ACCESS_PUBLIC_WRITE)>0?"w":"-");
        str += ((m_accessFlags & C_ACCESS_PUBLIC_VISIBLE)>0?"v":"-");
        str += ((m_accessFlags & C_ACCESS_INTERNAL_READ)>0?"i":"-");
*/
        return str;
    }
    /**
     * Returns the groupid of this resource.
     *
     * @return the groupid of this resource.
     */
     public CmsUUID getGroupId() {
         return  m_groupId;
      }
    /**
     * Gets the launcher classname for this resource.
     *
     * @return the launcher classname for this resource.
     */
     public String getLauncherClassname() {
         return m_launcherClassname;
     }
     /**
     * Gets the launcher type id for this resource.
     *
     * @return the launcher type id of this resource.
     */
     public int getLauncherType() {
         return m_launcherType;
     }
     /**
     * Gets the length of the content (filesize).
     *
     * @return the length of the content.
     */
     public int getLength() {
        return m_size;
     }
    /**
     * Returns the name of this resource.<BR/>
     * Example: retuns language.cms for the
     * resource /system/def/language.cms
     *
     * @return the name of this resource.
     */
    public String getName() {
        String name = null;
        String absoluteName = getAbsolutePath();
        
        // check if this is a file
        if (!absoluteName.endsWith("/")) {
            name = absoluteName.substring(absoluteName.lastIndexOf("/") + 1, absoluteName.length());
        } else {
            name = absoluteName.substring(0, absoluteName.length() - 1);
            name = name.substring(name.lastIndexOf("/") + 1, name.length());
        }

        return name;
    }
    
    /**
     * Returns the userid of the resource owner.
     *
     * @return the userid of the resource owner.
     */
    public CmsUUID getOwnerId() {
         return m_userId;
      }
      
    /**
     * Returns the absolute parent folder name of this resource.<p>
     * 
     * The parent resource of a file is the folder of the file.
     * The parent resource of a folder is the parent folder.
     * The parent resource of the root folder is <code>null</code>.<p>
     * 
     * Example: <code>/system/workplace/</code> has the parent <code>/system/</code>.
     * 
     * @return the calculated parent absolute folder path, or <code>null</code> for the root folder 
     */
     public String getParent() {
        return getParent(getAbsolutePath());
     }
     
    /**
     * Returns the absolute parent folder name of a resource.<p>
     * 
     * The parent resource of a file is the folder of the file.
     * The parent resource of a folder is the parent folder.
     * The parent resource of the root folder is <code>null</code>.<p>
     * 
     * Example: <code>/system/workplace/</code> has the parent <code>/system/</code>.
     * 
     * @param resource the resource to find the parent folder for
     * @return the calculated parent absolute folder path, or <code>null</code> for the root folder 
     */
    public static String getParent(String resource) {
        if (C_ROOT.equals(resource)) return null;
        // remove the last char, for a folder this will be "/", for a file it does not matter
        String parent = (resource.substring(0, resource.length() - 1));
        // now as the name does not end with "/", check for the last "/" which is the parent folder name
        return parent.substring(0, parent.lastIndexOf("/") + 1);
    }
    
    /**
     * Returns the folder path of this resource,
     * if the resource is a folder, the complete path of the folder is returned 
     * (not the parent folder path).<p>
     * 
     * Example: Returns <code>/system/def/</code> for the
     * resource <code>/system/def/file.html</code> and 
     * <code>/system/def/</code> for the (folder) resource <code>/system/def/</code>.
     * 
     * Does not append the repository information to the result, 
     * i.e. <code>/system/def/</code> will be returned, not <code>/default/vfs/system/def/</code>.
     *
     * @return the folder of this resource
     */
    public String getPath() {
        return getPath(getAbsolutePath());
    }
    
    /**
     * Returns the folder path of the resource with the given name,
     * if the resource is a folder (i.e. ends with a "/"), the complete path of the folder 
     * is returned (not the parent folder path).<p>
     * 
     * This is achived by just cutting of everthing behind the last occurence of a "/" character
     * in the String, no check if performed if the resource exists or not in the VFS, 
     * only resources that end with a "/" are considered to be folders.
     * 
     * Example: Returns <code>/system/def/</code> for the
     * resource <code>/system/def/file.html</code> and 
     * <code>/system/def/</code> for the (folder) resource <code>/system/def/</code>..
     *
     * @param resource the name of a resource
     * @return the folder of the given resource
     */
    public static String getPath(String resource) {
        return resource.substring(0, resource.lastIndexOf("/") + 1);
    }
        
    /**
     * Returns the name of a parent folder of the given resource, 
     * that is either minus levels up 
     * from the current folder, or that is plus levels down from the 
     * root folder.<p>
     * 
     * @param resource the name of a resource
     * @param number of levels to walk up or down
     * @return the name of a parent folder of the given resource, 
     * that is either minus levels up 
     * from the current folder, or that is plus levels down from the 
     * root folder
     */
    public static String getPathPart(String resource, int level) {
        resource = getPath(resource); 
        String result = null;
        int pos = 0, count = 0;
        if (level >= 0) {
            // Walk down from the root folder /
            while ((count < level) && (pos > -1)) {
                count ++;
                pos = resource.indexOf('/', pos+1);
            }
        } else {
            // Walk up from the current folder
            pos = resource.length();
            while ((count > level) && (pos > -1)) {
                count--;
                pos = resource.lastIndexOf('/', pos-1);
            }      
        }
        if (pos > -1) {
            // To many levels walked
            result = resource.substring(0, pos+1);
        } else {
            // Add trailing slash
            result = (level < 0)?"/":resource;
        }        
        return result;
    }
    
    /**
     * Returns the directory level of a resource.<p>
     * 
     * The root folder "/" has level 0,
     * a folder "/foo/" would have level 1,
     * a folfer "/foo/bar/" level 2 etc.<p> 
     * 
     * @return the directory level of a resource
     */
    public static int getPathLevel(String resource) {
        int level = -1;
        int pos = 0;
        while (resource.indexOf('/', pos) >= 0) {
            pos = resource.indexOf('/', pos) + 1;
            level++;
        }
        return level;
    }    
        
    /**
     * Gets the ID of the parent's file tree/hierarchy database entry.
     *
     * @return the ID of the parent's file tree/hierarchy database entry.
     */
    public CmsUUID getParentId() {
        return m_parentId;
    }
          
    /**
     * Returns the project id for this resource.
     *
     * @return the project id for this resource.
     */
    public int getProjectId() {
        return m_projectId;
    }
      
    /**
     * Gets the ID of the file header database entry.
     *
     * @return the ID of the file header database entry
     */
     public CmsUUID getResourceId(){
        return m_resourceId;
     }
     
     /**
      * Gets the ID of the file tree/hierarchy database entry to identify a resource.
      * 
      * @return the ID of the file tree/hierarchy database entry
      */
     public CmsUUID getId() {
         return m_structureId;
     }
     
    /**
     * Gets the userId from the user who made the last change.
     *
     * @return the userId from the user who made the last change.
     */
     public CmsUUID getResourceLastModifiedBy(){
        return m_resourceLastModifiedByUserId;
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
     * Gets the type id for this resource.
     *
     * @return the type id of this resource.
     */
     public int getType() {
       return m_resourceType;
     }
    /**
     * Gets the project id of the project that has locked this resource.
     *
     * @return the project id.
     */
     public int getLockedInProject() {
       return m_lockedInProject;
     }
     /**
      * Checks if a resource belongs to a project.
      * @param project The project which the resources is checked about.
      * @return true if the resource is in the project, false otherwise.
      */
     public boolean inProject(CmsProject project){
         boolean inProject=false;
         if (project.getId() == m_projectId) {
             inProject=true;
         }
         return inProject;
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
     * Determines, if this resource is locked by a user.
     *
     * @return true, if this resource is locked by a user, else it returns false.
     */
    public boolean isLocked() {
        boolean isLocked = true;
        //check if the user id in the locked by field is the unknown user id.
        if (m_lockedByUserId.isNullUUID()) {
            isLocked = false;
        }
        return isLocked;
    }
    /**
     * Returns the user idthat locked this resource.
     *
     * @return the user id that locked this resource.
     * If this resource is free it returns the unknown user id.
     */
      public CmsUUID isLockedBy() {
        return m_lockedByUserId;
      }
     /**
     * Sets the accessflags of this resource.
     *
     * @param The new accessflags of this resource.
     */
      public void setAccessFlags(int flags){
          m_accessFlags=flags;
      }
    /**
     * Sets the File id for this resource.
     *
     * @param The File id of this resource.
     */
    public void setFileId(CmsUUID fileId){
        m_fileId = fileId;
    }
     /**
     * Sets the flags of this resource.
     *
     * @param The new flags of this resource.
     */
      void setFlags(int flags){
          m_resourceFlags=flags;
      }
    /**
     * Sets the groupId of this resource.
     *
     * @param The new groupId of this resource.
     */
      public void setGroupId(CmsUUID groupId) {
          m_groupId= groupId;
      }
     /**
     * Sets launcher classname for this resource.
     *
     * @param The new launcher classname for this resource.
     */
     void setLauncherClassname(String name) {
      m_launcherClassname=name;
         }
     /**
     * Sets launcher the type id for this resource.
     *
     * @param The new launcher type id of this resource.
     */
     public void setLauncherType(int type){
         m_launcherType=type;
     }
     /**
     * Sets the the user id that locked this resource.
     *
     * @param The new the user id that locked this resource.
     */
     public void setLocked(CmsUUID userId) {
          m_lockedByUserId=userId;
      }
     /**
     * Sets the parent database id for this resource.
     *
     * @param The new database id of this resource.
     */
    public void setParentId(CmsUUID parentId){
        m_parentId = parentId;
    }
    /**
     * Sets the user id from the user who changes the resource.
     *
     * @param The userId from the user who changes the resource.
     */
    void setResourceLastModifiedBy(CmsUUID resourceLastModifiedByUserId){
        m_resourceLastModifiedByUserId = resourceLastModifiedByUserId;
    }
      /**
     * Sets the state of this resource.
     *
     * @param The new state of this resource.
     */
      public void setState(int state) {
          m_state=state;
      }
     /**
     * Sets the type id for this resource.
     *
     * @param The new type id of this resource.
     */
     public void setType(int type) {
         m_resourceType=type;
     }
    /**
     * Sets the userId of this resource.
     *
     * @param The new userId of this resource.
     */
    public  void setUserId(CmsUUID userId) {
          m_userId = userId;
      }

    /**
     * Sets the projectId of this resource.
     *
     * @param The new projectId of this resource.
     */
    public  void setProjectId(int project) {
          m_projectId = project;
      }


    /**
     * Sets the projectId in which this resource is locked.
     *
     * @param The new projectId of this resource.
     */
    public  void setLockedInProject(int project) {
          m_lockedInProject = project;
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
        output.append(" ID: ");
        output.append(m_resourceId);
        output.append(" ParentID: ");
        output.append(m_parentId);
        output.append(" , Project=");
        output.append(m_projectId);
        output.append(" , User=");
        output.append(m_userId);
        output.append(" , Group=");
        output.append(m_groupId);
        output.append(" : Access=");
        output.append(getFlagString());
        output.append(" : Resource-type=");
        output.append(getType());
        output.append(" : Locked=");
        output.append(isLockedBy());
        output.append(" : length=");
        output.append(getLength());
        output.append(" : state=");
        output.append(getState());
        return output.toString();
      }
	/**
	 * Returns the isTouched.
	 * @return boolean
	 */
	public boolean isTouched() {
		return m_isTouched;
	}
    
    /**
     * Helper to encapsulate which ID of a resource is used to handle ACE's for resources/files/folders.
     * 
     * @return the "resource-ID" of a resource
     */
    public CmsUUID getResourceAceId() {
        return this.getResourceId();
    }
    
}

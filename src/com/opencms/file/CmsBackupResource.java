/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsBackupResource.java,v $
* Date   : $Date: 2003/07/02 11:03:12 $
* Version: $Revision: 1.7 $
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
 * Describes a backup resource in the Cms.
 *
 * @author Edna Falkenhan
 * @version $Revision: 1.7 $ $Date: 2003/07/02 11:03:12 $
 */
public class CmsBackupResource extends CmsResource implements Cloneable, Serializable, Comparable {

    /**
     * The id of the version.
     */
    private int m_versionId = I_CmsConstants.C_UNKNOWN_ID;

    /**
     * The name of the owner.
     */
    private String m_ownerName = "";

    /**
     * The name of the group.
     */
    private String m_groupName = "";

    /**
     * The name of the last user who had modified the resource.
     */
    private String m_lastModifiedByName = "";

    /**
     * The content of the file
     */
    private byte[] m_fileContent;

     /**
      * Constructor, creates a new CmsBackupResource object.
      *
      * @param versionId The versionId of the resource
      * @param resourceId The database Id.
      * @param parentId The database Id of the parent folder.
      * @param fileId The id of the content.
      * @param resourceName The name (including complete path) of the resouce.
      * @param resourceType The type of this resource.
      * @param rescourceFlags The flags of thei resource.
      * @param userId The id of the user of this resource.
      * @param userName The name of the user of this resource.
      * @param groupId The id of the group of this resource.
      * @param groupName The name of the group of this resource.
      * @param projectId The project id this resource belongs to.
      * @param accessFlags The access flags of this resource.
      * @param state The state of this resource.
      * @param lockedBy The user id of the user who has locked this resource.
      * @param launcherType The launcher that is require to process this recource.
      * @param launcherClassname The name of the Java class invoked by the launcher.
      * @param dateCreated The creation date of this resource.
      * @param dateLastModified The date of the last modification of the resource.
      * @param fileContent Then content of the file.
      * @param resourceLastModifiedBy The user who changed the file.
      * @param lastModifiedByName The name of user who changed the file.
      * @param size The size of the file content.
      */
     public CmsBackupResource(int versionId, CmsUUID structureId, CmsUUID resourceId, CmsUUID parentId,
                              CmsUUID fileId, String resourceName, int resourceType,
                              int resourceFlags, CmsUUID userId, String userName, CmsUUID groupId,
                              String groupName, int projectId, int accessFlags,
                              int state, int launcherType,
                              String launcherClassname, long dateCreated,
                              long dateLastModified, CmsUUID resourceLastModifiedByUserId,
                              String lastModifiedByName,byte[] fileContent, int size, int lockedInProject){

        // create the CmsResource.
        super(structureId, resourceId, parentId,
              fileId,resourceName,resourceType,
              resourceFlags,userId,groupId,
              projectId,accessFlags,state,
              CmsUUID.getNullUUID(),launcherType,
              launcherClassname,dateCreated,
              dateLastModified,resourceLastModifiedByUserId, size, lockedInProject);

        // set content and size.
        m_fileContent=fileContent;

        // set version id
        m_versionId = versionId;

        // set owner name
        m_ownerName = userName;

        // set group name
        m_groupName = groupName;

        // set lastModifiedByName
        m_lastModifiedByName = lastModifiedByName;

   }
    /**
    * Clones the CmsFile by creating a new CmsFolder.
    * @return Cloned CmsFile.
    */
    public Object clone() {
        byte[] newContent = new byte[ this.getContents().length ];
        System.arraycopy(getContents(), 0, newContent, 0, getContents().length);

        return new CmsBackupResource(this.getVersionId(), this.getId(), this.getResourceId(),
                                     this.getParentId(), this.getFileId(),
                                     new String(this.getResourceName()), this.getType(), this.getFlags(),
                                     this.getOwnerId(), this.getOwnerName(), this.getGroupId(),
                                     this.getGroupName(), this.getProjectId(), this.getAccessFlags(),
                                     this.getState(),
                                     this.getLauncherType(), new String(this.getLauncherClassname()),
                                     this.getDateCreated(),this.getDateLastModified(),
                                     this.getResourceLastModifiedBy(),
                                     this.getLastModifiedByName(), newContent, this.getLength(), this.getLockedInProject());
    }
    /**
     * Gets the content of this file.
     *
     * @return the content of this file.
     */
    public byte[] getContents() {
      return m_fileContent;
    }

    /**
     * Gets the version id of this resource.
     *
     * @return the version id of this resource.
     */
    public int getVersionId() {
      return m_versionId;
    }

    /**
     * Gets the name of the owner of this resource.
     *
     * @return the name of the owner of this resource.
     */
    public String getOwnerName() {
      return m_ownerName;
    }

    /**
     * Gets the name of the group of this resource.
     *
     * @return the name of the group of this resource.
     */
    public String getGroupName() {
      return m_groupName;
    }

    /**
     * Gets the name of the user who changed this resource.
     *
     * @return the name of the user who changed this resource.
     */
    public String getLastModifiedByName() {
      return m_lastModifiedByName;
    }

    /**
     * Gets the file-extension.
     *
     * @return the file extension. If this file has no extension, it returns
     * a empty string ("").
     */
    public String getExtension(){
        String name=null;
        String extension="";
        int dot;

        name=this.getName();
        // check if this file has an extension.
        dot=name.lastIndexOf(".");
        if (dot> 0) {
            extension=name.substring(dot,name.length());
        }
        return extension;
    }
}

/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsFile.java,v $
* Date   : $Date: 2003/07/19 01:51:37 $
* Version: $Revision: 1.29 $
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

import com.opencms.flex.util.CmsUUID;

import java.io.Serializable;

/**
 * Describes a file in the Cms.
 *
 * @author Michael Emmerich
 * @version $Revision: 1.29 $ $Date: 2003/07/19 01:51:37 $
 */
public class CmsFile extends CmsResource implements Cloneable, Serializable, Comparable {

    /*
     * The content of the file.
     */
    private byte[] m_fileContent;
    
   /**
    * Constructor, creates a new CmsFile object.<p>
    *
    * @param structureId the id of the structure record
    * @param resourceId the id of the resource record
    * @param parentId the id of the parent folder
    * @param fileId the id of the content
    * @param resourceName the name (including complete path) of the resouce
    * @param resourceType the type of this resource
    * @param resourceFlags the flags of this resource
    * @param projectId the project id this resource belongs to.
    * @param accessFlags the access flags of this resource
    * @param state the state of this resource
    * @param lockedByUser the user id of the user who has locked this resource
    * @param launcherType the launcher that is used to process this recource
    * @param dateCreated the creation date of this resource
    * @param createdByUser the id of the user who created this resource
    * @param dateLastModified the date of the last modification of the resource
    * @param lastModifiedByUser the id of the user who did the last modification
    * @param fileContent the content of the file
    * @param size the size of the file content
    * @param lockedInProject the id of the project the resource is locked in
    * @param vfsLinkType the link type
    */
    public CmsFile(
        CmsUUID structureId,
        CmsUUID resourceId,
        CmsUUID parentId,
        CmsUUID fileId,
        String resourceName,
        int resourceType,
        int resourceFlags,
        int projectId,
        int accessFlags,
        int state,
        CmsUUID lockedByUser,
        int launcherType,
        long dateCreated,
        CmsUUID createdByUser,
        long dateLastModified,
        CmsUUID lastModifiedByUser,
        byte[] fileContent,
        int size,
        int lockedInProject,
        int vfsLinkType
    ) {
        // create the CmsResource.
        super(
            structureId, 
            resourceId, 
            parentId, 
            fileId, 
            resourceName, 
            resourceType, 
            resourceFlags, 
            projectId, 
            accessFlags, 
            state, 
            lockedByUser, 
            launcherType, 
            dateCreated, 
            createdByUser, 
            dateLastModified, 
            lastModifiedByUser, 
            size, 
            lockedInProject, 
            vfsLinkType);
    
        // set content and size.
        m_fileContent = fileContent;
    }
 
    /**
    * Clones the CmsFile by creating a new CmsFolder.
    * @return Cloned CmsFile.
    */
    public Object clone() {
        
        byte[] newContent = new byte[this.getContents().length];
        System.arraycopy(getContents(), 0, newContent, 0, getContents().length);

        CmsFile clone = new CmsFile(
            this.getId(),
            this.getResourceId(),
            this.getParentId(),
            this.getFileId(),
            new String(this.getResourceName()),
            this.getType(),
            this.getFlags(),
            this.getProjectId(),
            this.getAccessFlags(),
            this.getState(),
            this.isLockedBy(),
            this.getLoaderId(),
            this.getDateCreated(),
            this.getUserCreated(),
            this.getDateLastModified(),
            this.getUserLastModified(),
            newContent,
            this.getLength(),
            this.getLockedInProject(),
            this.getVfsLinkType());
            
        return clone;
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
     * Gets the file-extension.
     *
     * @return the file extension. If this file has no extension, it returns
     * a empty string ("")
     */
    public String getExtension() {
        String name=null;
        String extension="";
        int dot;

        name=this.getResourceName();
        // check if this file has an extension.
        dot=name.lastIndexOf(".");
        if (dot> 0) {
            extension=name.substring(dot, name.length());
        }
        return extension;
    }
    
    /**
     * Sets the content of this file.
     *
     * @param value the content of this file.
     */
    public void setContents(byte[] value) {
        m_fileContent=value;
        if (m_fileContent.length >0) {
            m_size=m_fileContent.length;
        }
    }  
}

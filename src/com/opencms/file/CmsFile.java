package com.opencms.file;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsFile.java,v $
 * Date   : $Date: 2001/07/09 08:10:22 $
 * Version: $Revision: 1.11 $
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

import java.io.*;

/**
 * This class describes a file in the Cms.
 *
 * @author Michael Emmerich
 * @version $Revision: 1.11 $ $Date: 2001/07/09 08:10:22 $
 */
public class CmsFile extends CmsResource implements Cloneable,Serializable {

	/**
	 * The content of the file.
	 */
	private byte[] m_fileContent;

	 /**
	  * Constructor, creates a new CmsFile object.
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
	  * @param fileContent Then content of the file.
	  * @param resourceLastModifiedBy The user who changed the file.
	  * @param size The size of the file content.
	  */
	 public CmsFile(int resourceId, int parentId, int fileId,
						String resourceName, int resourceType, int resourceFlags,
						int user, int group, int projectId,
						int accessFlags, int state, int lockedBy,
						int launcherType, String launcherClassname,
						long dateCreated, long dateLastModified,
						int resourceLastModifiedBy,
						byte[] fileContent,int size){

		// create the CmsResource.
		super(resourceId, parentId, fileId,
			  resourceName,resourceType,resourceFlags,
			  user,group,projectId,
			  accessFlags,state,lockedBy,
			  launcherType,launcherClassname,
			  dateCreated,dateLastModified,
			  resourceLastModifiedBy,size);

		// set content and size.
		m_fileContent=fileContent;

   }
	/**
	* Clones the CmsFile by creating a new CmsFolder.
	* @return Cloned CmsFile.
	*/
	public Object clone() {
		byte[] newContent = new byte[ this.getContents().length ];
		System.arraycopy(getContents(), 0, newContent, 0, getContents().length);

		return new CmsFile(this.getResourceId(), this.getParentId(), this.getFileId(),
							 new String(this.getAbsolutePath()),this.getType(),
							 this.getFlags(), this.getOwnerId(), this.getGroupId(),
							 this.getProjectId(),this.getAccessFlags(),
							 this.getState(),this.isLockedBy(), this.getLauncherType(),
							 new String(this.getLauncherClassname()), this.getDateCreated(),
							 this.getDateLastModified(),this.getResourceLastModifiedBy(),
							 newContent, this.getLength());
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
	/**
	 * Sets the content of this file.
	 *
	 * @param value the content of this file.
	 */
	public void setContents(byte[] value) {
		m_fileContent=value;
		if (m_fileContent.length >0 ) {
			m_size=m_fileContent.length;
		}
	}
}

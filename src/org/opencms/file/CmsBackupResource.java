/*
* File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/CmsBackupResource.java,v $
 * Date   : $Date: 2004/06/25 16:33:32 $
 * Version: $Revision: 1.6 $
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
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
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
 * A backup resource object.<p>
 *
 * Backup resources are basic resources that contain additional information 
 * used to describe the backup state.
 * Backup resource extend CmsFile since the might contain binary content,
 * but they can also in fact be backup resources for a folder.<p>
 * Backup resources contain the names of the users that 
 * created or last modified the resource as a String because 
 * a user id might have been deleted.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.6 $
 */
public class CmsBackupResource extends CmsFile implements Cloneable, Serializable, Comparable {

    /** The id of the backup version. */
    private int m_versionId;

    /**
     * The tag id of the version.
     */
    private int m_tagId;
    
    /** The backup id of the resource. */
    private CmsUUID m_backupId;


    /** The name of the last user who modified the resource. */
    private String m_lastModifiedByName;

    /**
     * The name of the user who created the resource.
     */
    private String m_createdByName;


    /**
    * Constructor, creates a new CmsBackupResource object.
    *
    * @param backupId the backup id of this backup resource     
    * @param tagId the tag id of this backup resource    
    * @param versionId the version id of this backup resource   
    * @param structureId the id of this resources structure record
    * @param resourceId the id of this resources resource record
    * @param parentId the id of this resources parent folder
    * @param fileId the id of this resources content record
    * @param name the filename of this resouce
    * @param type the type of this resource
    * @param flags the flags of this resource
    * @param projectId the project id this resource was last modified in
    * @param state the state of this resource
    * @param loaderId the id for the that is used to load this recource
    * @param dateCreated the creation date of this resource
    * @param userCreated the id of the user who created this resource
    * @param userCreatedName the name of the user who created this resource 
    * @param dateLastModified the date of the last modification of this resource
    * @param userLastModified the id of the user who did the last modification of this resource
    * @param userLastModifiedName the name of the user who did the last modification of this resource
    * @param dateReleased the release date of this resource
    * @param dateExpired the expiration date of this resource
    * @param size the size of the file content of this resource
    * @param content the binary content data of this file
    */
    public CmsBackupResource(
        CmsUUID backupId,
        int tagId,
        int versionId, 
        CmsUUID structureId, 
        CmsUUID resourceId, 
        CmsUUID parentId,
        CmsUUID fileId, 
        String name, 
        int type,
        int flags, 
        int projectId, 
        int state,
        int loaderId, 
        long dateCreated,
        CmsUUID userCreated, 
        String userCreatedName, 
        long dateLastModified,
        CmsUUID userLastModified, 
        String userLastModifiedName, 
        long dateReleased,
        long dateExpired,
        int size,
        byte[] content
    ) {
        // create the backup CmsResource.
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
            loaderId,
            dateCreated,
            userCreated,
            dateLastModified, 
            userLastModified, 
            dateReleased,
            dateExpired,
            0, 
            size, 
            content
        );
        
        m_backupId=backupId;

        // set tag id
        m_tagId = tagId;

        // set version id
        m_versionId = versionId;
      
        // set createdByName
        m_createdByName = userCreatedName;

        // set lastModifiedByName
        m_lastModifiedByName = userLastModifiedName;
    }

    /**
     * Returns a clone of this Objects instance.<p>
     * 
     * @return a clone of this instance
     */
   public Object clone() {
       byte[] newContent = new byte[ this.getContents().length ];
       System.arraycopy(getContents(), 0, newContent, 0, getContents().length);

       return new CmsBackupResource(
           getBackupId(),
           getTagId(),
           getVersionId(), 
           getStructureId(), 
           getResourceId(),
           getParentStructureId(), 
           getContentId(),
           getName(), 
           getTypeId(), 
           getFlags(),
           getProjectLastModified(), 
           getState(),
           getLoaderId(),
           getDateCreated(),
           getUserCreated(), 
           getCreatedByName(), 
           getDateLastModified(), 
           getUserLastModified(), 
           getLastModifiedByName(),
           getDateReleased(),
           getDateExpired(),
           getLength(), 
           newContent);
   }


   /**
    * Returns the backup id of this resource.
    *
    * @return the backup id of this resource.
    */
   public CmsUUID getBackupId() {
       return m_backupId;
   }

    

    /**
     * Returns the tag id of this resource.
     *
     * @return the tag id of this resource.
     */
    public int getTagId() {
        return m_tagId;
    }


    /**
     *  Returns the version id of this backup resource.
     *
     * @return the version id of this resource
     */
    public int getVersionId() {
        return m_versionId;
    }

    /**
     * Returns the user name of the creator of this backup resource.<p>
     *
     * @return the user name of the creator of this backup resource
     */
    public String getCreatedByName() {
        return m_createdByName;
    }

    /**
     * Returns the name of the user who last changed this backup resource.<p>
     *
     * @return the name of the user who last changed this backup resource
     */
    public String getLastModifiedByName() {
        return m_lastModifiedByName;
    }
}

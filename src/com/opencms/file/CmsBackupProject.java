/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsBackupProject.java,v $
* Date   : $Date: 2003/07/31 13:19:37 $
* Version: $Revision: 1.6 $
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

import java.sql.Timestamp;
import java.util.Vector;

/**
 * Describes a backup project. A project is used to handle versions of
 * one resource.
 *
 * @author Edna Falkenhan
 * @version $Revision: 1.6 $ $Date: 2003/07/31 13:19:37 $
 */
public class CmsBackupProject extends CmsProject implements Cloneable{

    /**
     * The publishing date of this project.
     */
    private long m_publishingdate = I_CmsConstants.C_UNKNOWN_LONG;

    /**
     * The user-id of the publisher
     */
    private CmsUUID m_publishedByUserId;

    /**
     * The version of the published project
     */
    private int m_versionId = I_CmsConstants.C_UNKNOWN_ID;

    /**
     * The resources of the project
     */
    private Vector m_projectresources = null;

    /**
     * The name, firstname and lastname of the owner
     */
    private String m_ownerName = new String();

    /**
     * The name, firstname and lastname of the user who has published the project
     */
    private String m_publishedByName = new String();

    /**
     * The name of the group
     */
    private String m_groupName = new String();

    /**
     * The name of the managergroup
     */
    private String m_managerGroupName = new String();

    /**
     * Construct a new CmsProject including publishing data.
     */
    public CmsBackupProject(int versionId, int projectId, String name, Timestamp publishingdate, CmsUUID publishedByUserId,
                      String publishedByName, String description, int taskId,
                      CmsUUID ownerId, String ownerName, CmsUUID groupId, String groupName,
                      CmsUUID managerGroupId, String managerGroupName, Timestamp createdate, int type,
                      Vector projectresources) {

        super(projectId, name, description, taskId,
              ownerId, groupId, managerGroupId, 0,
              createdate, type);

        m_versionId = versionId;
        m_ownerName = ownerName;
        m_groupName = groupName;
        m_managerGroupName=managerGroupName;
        m_publishedByUserId = publishedByUserId;
        m_publishedByName = publishedByName;

        if( publishingdate != null) {
            m_publishingdate = publishingdate.getTime();
        } else {
            m_publishingdate = I_CmsConstants.C_UNKNOWN_LONG;
        }
        m_projectresources = projectresources;
    }

    /**
    * Clones the CmsProject by creating a new CmsProject Object.
    * @return Cloned CmsProject.
    */
    public Object clone() {
        CmsBackupProject project=new CmsBackupProject(this.m_versionId, this.getId(),
                                       new String (this.getName()), new Timestamp(this.m_publishingdate),
                                       this.m_publishedByUserId, new String(this.m_publishedByName),
                                       new String(this.getDescription()),this.getTaskId(),
                                       this.getOwnerId(), new String(this.m_ownerName),this.getGroupId(),
                                       new String(this.m_groupName), this.getManagerGroupId(),
                                       new String(this.m_managerGroupName),new Timestamp(this.getCreateDate()),
                                       this.getType(), this.m_projectresources);
        return project;
    }

    /**
     * Compares the overgiven object with this object.
     *
     * @return true, if the object is identically else it returns false.
     */
    public boolean equals(Object obj) {
        boolean equal=false;
        // check if the object is a CmsProject object
        if (obj instanceof CmsBackupProject) {
            // same ID than the current project?
            if (((CmsBackupProject)obj).getId() == this.getId()){
                equal = true;
            }
        }
        return equal;
    }

    /**
     * Gets the published-by value.
     *
     * @return the published-by value.
     */
    public CmsUUID getPublishedBy() {
        return m_publishedByUserId;
    }
    /**
     * Returns the publishing date of this project.
     *
     * @return the publishing date of this project.
     */
    public long getPublishingDate() {
        return(m_publishingdate);
    }

    /**
     * Gets the versionId.
     *
     * @return the versionId.
     */
    public int getVersionId() {
        return m_versionId;
    }

    /**
     * Gets the ownername.
     *
     * @return the ownername.
     */
    public String getOwnerName() {
        return m_ownerName;
    }

    /**
     * Gets the publishers name.
     *
     * @return the publishers name.
     */
    public String getPublishedByName() {
        return m_publishedByName;
    }

    /**
     * Gets the groupname.
     *
     * @return the groupname.
     */
    public String getGroupName() {
        return m_groupName;
    }

    /**
     * Gets the managergroupname.
     *
     * @return the managergroupname.
     */
    public String getManagerGroupName() {
        return m_managerGroupName;
    }

    /**
     * Gets the projectresources
     */
    public Vector getProjectResources(){
        return m_projectresources;
    }
}

package com.opencms.file;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsBackupProject.java,v $
 * Date   : $Date: 2001/07/09 08:34:54 $
 * Version: $Revision: 1.1 $
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

import com.opencms.core.*;
import java.sql.*;
import java.util.Vector;
import com.opencms.util.SqlHelper;

/**
 * This class describes a backup project. A project is used to handle versions of
 * one resource.
 *
 * @author Edna Falkenhan
 * @version $Revision: 1.1 $ $Date: 2001/07/09 08:34:54 $
 */
public class CmsBackupProject extends CmsProject implements Cloneable{

	/**
	 * The publishing date of this project.
	 */
	private long m_publishingdate = C_UNKNOWN_LONG;

	/**
	 * The user-id of the publisher
	 */
	private int m_publishedBy = C_UNKNOWN_ID;

	/**
	 * The version of the published project
	 */
	private int m_versionId = C_UNKNOWN_ID;

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
	public CmsBackupProject(int versionId, int projectId, String name, Timestamp publishingdate, int publishedBy,
                      String publishedByName, String description, int taskId,
                      int ownerId, String ownerName, int group, String groupName,
                      int managerGroup, String managerGroupName, Timestamp createdate, int type,
                      Vector projectresources) {

		super(projectId, name, description, taskId,
              ownerId, group, managerGroup, 0,
              createdate, type);

        m_versionId = versionId;
        m_ownerName = ownerName;
        m_groupName = groupName;
        m_managerGroupName=managerGroupName;
		m_publishedBy = publishedBy;
        m_publishedByName = publishedByName;

		if( publishingdate != null) {
			m_publishingdate = publishingdate.getTime();
		} else {
			m_publishingdate = C_UNKNOWN_LONG;
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
									   this.m_publishedBy, new String(this.m_publishedByName),
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
	public int getPublishedBy() {
		return m_publishedBy;
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

/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/Attic/I_CmsExtendedContentDefinition.java,v $
* Date   : $Date: 2003/05/15 12:39:35 $
* Version: $Revision: 1.10 $
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
package com.opencms.defaults;

import com.opencms.file.CmsObject;
import com.opencms.flex.util.CmsUUID;

import java.util.Vector;

/**
 * Content Definitions that uses the projectmanagement,
 * that means the cd can be published and the history
 * can be enabled, should implement this interface
 */
public interface I_CmsExtendedContentDefinition {

    /**
     * Returns the unique id of the content definition
     *
     * @return int The unique id of the cd
     */
    public CmsUUID getId();

    /**
     * Returns the projectId of the content definition.
     * If the cd belongs to the current project the value
     * is the id of the current project otherwise its
     * the id of the online project
     *
     * @return int The project id
     */
    public int getProjectId();

    /**
     * Returns the state of the content definition:
     * unchanged, new, changed or deleted
     *
     * @return int The state of the cd
     */
    public int getState();

    /**
     * Returns the projectId of the content definition
     * that is stored in the cd table after the cd
     * was locked
     *
     * @return int The id of the project
     */
    public int getLockedInProject();

    /**
     * Returns the date of the last modification of the content definition
     *
     * @return long The date of the last modification
     */
    public long getDateLastModified();

    /**
     * Returns the date of the creation of the content definition
     *
     * @return long The date of the creation
     */
    public long getDateCreated();

    /**
     * Returns the id of the user who has modified the content definition
     *
     * @return int The id of the user who has modified the cd
     */
    public CmsUUID getLastModifiedBy();

    /**
     * Returns the name of the user who has modified the content definition
     *
     * @return String The name of the user who has modified the cd
     */
    public String getLastModifiedByName();

    /**
     * Returns the id of the version in the history of the content definition
     *
     * @return int The id of the version
     */
    public int getVersionId();

    /**
     * Returns the title of the content definition
     *
     * @return String The title of the cd
     */
    public String getTitle();

    /**
     * Returns the ownerId of the content definition
     *
     * @return int The ownerId of the cd
     */
    public CmsUUID getOwner();

    /**
     * Returns the groupId of the content definition
     *
     * @return int The groupId of the cd
     */
    public CmsUUID getGroupId();

    /**
     * Returns the access flags of the content definition
     *
     * @return int The access flags of the cd
     */
    public int getAccessFlags();

    /**
     * Publishes the content definition directly
     *
     * @param cms The CmsObject
     */
    public void publishResource(CmsObject cms) throws Exception;

    /**
     * Undelete method
     * for undelete instance of content definition
     *
     * @param cms The CmsObject
     */
    public void undelete(CmsObject cms) throws Exception;


    /**
     * Restore method
     * for restore instance of content definition from the history
     *
     * @param cms The CmsObject
     * @param versionId The id of the version to restore
     */
    public void restore(CmsObject cms, int versionId) throws Exception;

    /**
     * Change owner method
     * for changing permissions of content definition
     *
     * @param cms The CmsObject
     * @param owner The id of the new owner
     */
    public void chown(CmsObject cms, CmsUUID ownerId) throws Exception;

    /**
     * Change group method
     * for changing permissions of content definition
     *
     * @param cms The CmsObject
     * @param group The id of the new group
     */
    public void chgrp(CmsObject cms, CmsUUID groupId) throws Exception;

    /**
     * Change access flags method
     * for changing permissions of content definition
     *
     * @param cms The CmsObject
     * @param accessflags The new access flags
     */
    public void chmod(CmsObject cms, int accessflags) throws Exception;

    /**
     * Copy method
     * for copying content definition
     *
     * @param cms The CmsObject
     * @return int The id of the new content definition
     */
    public CmsUUID copy(CmsObject cms) throws Exception;

    /**
     * History method
     * returns the vector of the versions of content definition in the history
     *
     * @param cms The CmsObject
     * @return Vector The versions of the cd in the history
     */
    public Vector getHistory(CmsObject cms) throws Exception;

    /**
     * History method
     * returns the cd of the version with the given versionId
     *
     * @param cms The CmsObject
     * @param versionId The version id
     * @return Object The object with the version of the cd
     */
    public Object getVersionFromHistory(CmsObject cms, int versionId) throws Exception;
}
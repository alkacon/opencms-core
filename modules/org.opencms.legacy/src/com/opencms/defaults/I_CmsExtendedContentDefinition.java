/*
* File   : $Source: /alkacon/cvs/opencms/modules/org.opencms.legacy/src/com/opencms/defaults/Attic/I_CmsExtendedContentDefinition.java,v $
* Date   : $Date: 2005/05/16 17:45:08 $
* Version: $Revision: 1.1 $
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

import org.opencms.util.CmsUUID;

import org.opencms.file.CmsObject;

import java.util.Vector;

/**
 * Content Definitions that uses the projectmanagement,
 * that means the cd can be published and the history
 * can be enabled, should implement this interface
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public interface I_CmsExtendedContentDefinition {
    
    /**
     * The permission to read a resource
     */
    int C_PERMISSION_READ = 1;

    /**
     * The permission to write a resource
     */
    int C_PERMISSION_WRITE = 2;

    /**
     * The permission to view a resource
     */
    int C_PERMISSION_VIEW = 4;    

    /**
     * Returns the unique id of the content definition
     *
     * @return int The unique id of the cd
     */
    CmsUUID getId();

    /**
     * Returns the projectId of the content definition.
     * If the cd belongs to the current project the value
     * is the id of the current project otherwise its
     * the id of the online project
     *
     * @return int The project id
     */
    int getProjectId();

    /**
     * Returns the state of the content definition:
     * unchanged, new, changed or deleted
     *
     * @return int The state of the cd
     */
    int getState();

    /**
     * Returns the projectId of the content definition
     * that is stored in the cd table after the cd
     * was locked
     *
     * @return int The id of the project
     */
    int getLockedInProject();

    /**
     * Returns the date of the last modification of the content definition
     *
     * @return long The date of the last modification
     */
    long getDateLastModified();

    /**
     * Returns the date of the creation of the content definition
     *
     * @return long The date of the creation
     */
    long getDateCreated();

    /**
     * Returns the id of the user who has modified the content definition
     *
     * @return int The id of the user who has modified the cd
     */
    CmsUUID getLastModifiedBy();

    /**
     * Returns the name of the user who has modified the content definition
     *
     * @return String The name of the user who has modified the cd
     */
    String getLastModifiedByName();

    /**
     * Returns the id of the version in the history of the content definition
     *
     * @return int The id of the version
     */
    int getVersionId();

    /**
     * Returns the title of the content definition
     *
     * @return String The title of the cd
     */
    String getTitle();

    /**
     * Returns the ownerId of the content definition
     *
     * @return int The ownerId of the cd
     */
    CmsUUID getOwner();

    /**
     * Returns the groupId of the content definition
     *
     * @return int The groupId of the cd
     */
    CmsUUID getGroupId();

    /**
     * Returns the access flags of the content definition
     *
     * @return int The access flags of the cd
     */
    int getAccessFlags();

    /**
     * Publishes the content definition directly
     *
     * @param cms The CmsObject
     * @throws Exception if something goes wrong
     */
    void publishResource(CmsObject cms) throws Exception;

    /**
     * Undelete method
     * for undelete instance of content definition
     *
     * @param cms The CmsObject
     * @throws Exception if something goes wrong
     */
    void undelete(CmsObject cms) throws Exception;


    /**
     * Restore method
     * for restore instance of content definition from the history
     *
     * @param cms The CmsObject
     * @param versionId The id of the version to restore
     * @throws Exception if something goes wrong
     */
    void restore(CmsObject cms, int versionId) throws Exception;

    /**
     * Change owner method
     * for changing permissions of content definition
     *
     * @param cms The CmsObject
     * @param ownerId The id of the new owner
     * @throws Exception if something goes wrong
     */
    void chown(CmsObject cms, CmsUUID ownerId) throws Exception;

    /**
     * Change group method
     * for changing permissions of content definition
     *
     * @param cms The CmsObject
     * @param groupId The id of the new group
     * @throws Exception if something goes wrong
     */
    void chgrp(CmsObject cms, CmsUUID groupId) throws Exception;

    /**
     * Change access flags method
     * for changing permissions of content definition
     *
     * @param cms The CmsObject
     * @param accessflags The new access flags
     * @throws Exception if something goes wrong
     */
    void chmod(CmsObject cms, int accessflags) throws Exception;

    /**
     * Copy method
     * for copying content definition
     *
     * @param cms The CmsObject
     * @return int The id of the new content definition
     * @throws Exception if something goes wrong
     */
    CmsUUID copy(CmsObject cms) throws Exception;

    /**
     * History method
     * returns the vector of the versions of content definition in the history
     *
     * @param cms The CmsObject
     * @return Vector The versions of the cd in the history
     * @throws Exception if something goes wrong
     */
    Vector getHistory(CmsObject cms) throws Exception;

    /**
     * History method
     * returns the cd of the version with the given versionId
     *
     * @param cms The CmsObject
     * @param versionId The version id
     * @return Object The object with the version of the cd
     * @throws Exception if something goes wrong
     */
    Object getVersionFromHistory(CmsObject cms, int versionId) throws Exception;
}
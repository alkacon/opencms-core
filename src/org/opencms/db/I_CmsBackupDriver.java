/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/I_CmsBackupDriver.java,v $
 * Date   : $Date: 2003/09/23 07:50:24 $
 * Version: $Revision: 1.28 $
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
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

package org.opencms.db;

import org.opencms.util.*;

import com.opencms.core.CmsException;
import com.opencms.file.CmsBackupProject;
import com.opencms.file.CmsBackupResource;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsUser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Definitions of all required backup driver methods.<p>
 * 
 * A backup driver is a driver to write projects, resources and properties of
 * resources optionally to a second set of database tables while resources or
 * projects are published. A unique backup version ID is used to identify a set
 * of resource that were saved during one backup process.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com) 
 * @version $Revision: 1.28 $ $Date: 2003/09/23 07:50:24 $
 * @since 5.1
 */
public interface I_CmsBackupDriver {

    /**
     * Creates a valid CmsBackupResource instance from a JDBC ResultSet.<p>
     * 
     * @param res the JDBC result set
     * @param hasContent true if the file content is part of the result set
     * @return CmsBackupResource the new resource/file instance
     * @throws SQLException if a requested attribute was not found in the result set
     * @throws CmsException if something goes wrong
     */
    CmsBackupResource createBackupResource(ResultSet res, boolean hasContent) throws SQLException, CmsException;


    
    /**
     * Deletes all backup versions of a backup resource that are older than a given project tag and
     * where the version id is lower than a given value.<p>
     * 
     * @param res the backup resource
     * @param tag the project tag date
     * @param versions the deletion version
     * @throws CmsException if something goes wrong
     */
    void deleteBackup(CmsBackupResource res, int tag, int versions) throws CmsException;

    /**
     * Deletes backup versions of a resource.<p>
     * 
     * Deletes the m-n oldest backup versions, if m is the number of backup versions, and n
     * the number of max. allowed backup versions.
     * 
     * @param existingBackups a list of backup resources ordered by their ascending creation date
     * @param maxVersions maximum number of versions per resource
     * @throws CmsException if something goes wrong
     */
    void deleteBackups(List existingBackups, int maxVersions) throws CmsException;


    /**
     * Destroys this driver.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    void destroy() throws Throwable;

    /**
     * Initializes the SQL manager for this driver.<p>
     * 
     * To obtain JDBC connections from different pools, further 
     * {online|offline|backup} pool Urls have to be specified.
     * 
     * @return the SQL manager for this driver
     * @see org.opencms.db.generic.CmsSqlManager#setOfflinePoolUrl(String)
     * @see org.opencms.db.generic.CmsSqlManager#setOnlinePoolUrl(String)
     * @see org.opencms.db.generic.CmsSqlManager#setBackupPoolUrl(String)
     */
    org.opencms.db.generic.CmsSqlManager initQueries();

    /**
     * Reads a backup file including the file content.<p>
     *
     * @param tagId the tagId of the file
     * @param resourceId the id of the file to read
     * @return CmsBackupResource the backup file
     * @throws CmsException is something goes wrong
     */
    CmsBackupResource readBackupFile(int tagId, CmsUUID resourceId) throws CmsException;

    /**
     * Reads a backup file header excluding the file content.<p>
     *
     * @param tagId the tagId of the file
     * @param resourceId the id of the file to read
     * @return CmsBackupResource the backup file
     * @throws CmsException is something goes wrong
     */
    CmsBackupResource readBackupFileHeader(int tagId, CmsUUID resourceId) throws CmsException;

    /**
     * Reads all backup file headers  excluding the file content.<p>.
     *
     * @return List with all backup file headers
     * @throws CmsException if something goes wrong
     */
    List readBackupFileHeaders() throws CmsException;

    /**
     * Reads all backup file headers of a file excluding the file content.<p>.
     *
     * @param resourceId the id of the file to read
     * @return List with all backup file headers
     * @throws CmsException if something goes wrong
     */
    List readBackupFileHeaders(CmsUUID resourceId) throws CmsException;

    /**
     * Returns the max. current backup version of a resource.<p>
     * 
     * @param resourceId the resource ID of the resource
     * @return Returns the max. current backup version of a resource.
     * @throws CmsException if something goes wrong
     */
    int readBackupMaxVersion(CmsUUID resourceId) throws CmsException;

    /**
     * Reads a backup project.<p>
     *
     * @param tagId the versionId of the project
     * @return CmsBackupProject the backup project 
     * @throws CmsException is something goes wrong
     */
    CmsBackupProject readBackupProject(int tagId) throws CmsException;

    /**
     * Reads all resources that belong to a given backup version ID.<p>
     * 
     * @param tagId the version ID of the backup
     * @return Vector all resources that belong to the given backup version ID.
     * @throws CmsException if something goes wrong
     */
    Vector readBackupProjectResources(int tagId) throws CmsException;

    /**
     * Returns all projects from the history.<p>
     *
     * @return a Vector of projects
     * @throws CmsException if an error occurs
     */
    Vector readBackupProjects() throws CmsException;

    /**
     * Gets the TagId of the first backup project after a given date.<p>
     * 
     * This method is used during the deletion process of older backup data. 
     * @param maxdate the date to compare the backup projects with
     * @return tag id of the first backup project after maxdate 
     * @throws CmsException if something goes wrong
     */
    int readBackupProjectTag(long maxdate) throws CmsException;

    /**
     * Returns a list of all properties of a backup file or folder.<p>
     *
     * @param resource the resource to read the properties from
     * @return a Map of Strings representing the properties of the resource
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    HashMap readBackupProperties(CmsBackupResource resource) throws CmsException;

    /**
     * Gets the next available backup version ID for a resource.<p>
     * 
     * @return int the next available backup version ID
     */
    int readNextBackupTagId();

    /**
     * Writes a project to the backup.<p>
     * 
     * @param currentProject the current project
     * @param tagId the version ID of the backup
     * @param publishDate long timestamp when the current project was published 
     * @param currentUser the current user
     * @throws CmsException if something goes wrong
     */
    void writeBackupProject(CmsProject currentProject, int tagId, long publishDate, CmsUser currentUser) throws CmsException;

    /**
     * Writes the properties of a resource to the backup.<p>
     * 
     * @param publishProject the current project
     * @param resource the resource of the properties
     * @param properties the properties to write
     * @param backupId the id backup
     * @param tagId the tag ID of the backup
     * @param versionId the version ID of the backup
     * @throws CmsException if something goes wrong
     */
    void writeBackupProperties(CmsProject publishProject, CmsResource resource, Map properties, CmsUUID backupId, int tagId, int versionId) throws CmsException;

    /**
     * Writes a resource to the backup.<p>
     * 
     * @param currentUser the current user
     * @param publishProject the current project
     * @param resource the resource that is written to the backup
     * @param properties the properties of the resource
     * @param tagId the version ID of the backup
     * @param publishDate long timestamp when the resource was published
     * @param maxVersions maximum number of backup versions
     * @throws CmsException if something goes wrong
     */
    void writeBackupResource(CmsUser currentUser, CmsProject publishProject, CmsResource resource, Map properties, int tagId, long publishDate, int maxVersions) throws CmsException;


    
    /**
     * Writes a resource content to the backup.<p>
     * This method is for later use and should not be used now
     * 
     * @param projectId the project to read from
     * @param resource the resource (file header) to read the content from
     * @param backupResource the backup resource to write the backup content to
     * @throws CmsException if something goes wrong
     */
    void writeBackupResourceContent(int projectId, CmsResource resource, CmsBackupResource backupResource) throws CmsException;
}
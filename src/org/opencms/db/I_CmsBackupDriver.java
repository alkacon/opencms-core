/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/I_CmsBackupDriver.java,v $
 * Date   : $Date: 2004/12/15 12:29:45 $
 * Version: $Revision: 1.40 $
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

import org.opencms.db.generic.CmsSqlManager;
import org.opencms.file.CmsBackupProject;
import org.opencms.file.CmsBackupResource;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsPropertydefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Definitions of all required backup driver methods.<p>
 * 
 * A backup driver is a driver to write projects, resources and properties of
 * resources optionally to a second set of database tables while resources or
 * projects are published. A unique backup version ID is used to identify a set
 * of resource that were saved during one backup process.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com) 
 * @version $Revision: 1.40 $ $Date: 2004/12/15 12:29:45 $
 * @since 5.1
 */
public interface I_CmsBackupDriver {

    /** The type ID to identify backup driver implementations. */
    int C_DRIVER_TYPE_ID = 0;

    /**
     * Creates a new property defintion in the database.<p>
     * 
     * @param dbc the current database context
     * @param name the name of the propertydefinitions to overwrite
     * 
     * @return the new propertydefinition
     * @throws CmsException if something goes wrong
     */
    CmsPropertydefinition createBackupPropertyDefinition(CmsDbContext dbc, String name) throws CmsException;

    /**
     * Creates a valid CmsBackupResource instance from a JDBC ResultSet.<p>
     * 
     * @param res the JDBC result set
     * @param hasContent true if the file content is part of the result set
     * @return CmsBackupResource the new resource/file instance
     * 
     * @throws SQLException if a requested attribute was not found in the result set
     */
    CmsBackupResource createBackupResource(ResultSet res, boolean hasContent) throws SQLException;

    /**
     * Deletes all backup versions of a backup resource that are older than a given project tag and
     * where the version id is lower than a given value.<p>
     * 
     * @param dbc the current database context
     * @param res the backup resource
     * @param tag the project tag date
     * @param versions the deletion version
     * 
     * @throws CmsException if something goes wrong
     */
    void deleteBackup(CmsDbContext dbc, CmsBackupResource res, int tag, int versions) throws CmsException;

    /**
     * Deletes a property defintion.<p>
     * 
     * @param dbc the current database context
     * @param metadef the propertydefinitions to be deleted.
     *
     * @throws CmsException if something goes wrong
     */
    void deleteBackupPropertyDefinition(CmsDbContext dbc, CmsPropertydefinition metadef) throws CmsException;

    /**
     * Deletes backup versions of a resource.<p>
     * 
     * Deletes the m-n oldest backup versions, if m is the number of backup versions, and n
     * the number of max. allowed backup versions.<p>
     * 
     * @param context the current database context
     * @param existingBackups a list of backup resources ordered by their ascending creation date
     * @param maxVersions maximum number of versions per resource
     * 
     * @throws CmsException if something goes wrong
     */
    void deleteBackups(CmsDbContext context, List existingBackups, int maxVersions) throws CmsException;

    /**
     * Destroys this driver.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    void destroy() throws Throwable;

    /**
     * Returns the SqlManager of this driver.<p>
     * 
     * @return the SqlManager of this driver
     */
    CmsSqlManager getSqlManager();

    /**
     * Initializes the SQL manager for this driver.<p>
     * 
     * @param classname the classname of the SQL manager
     * 
     * @return the SQL manager for this driver
     */
    org.opencms.db.generic.CmsSqlManager initSqlManager(String classname);

    /**
     * Reads a backup file including the file content.<p>
     *
     * @param dbc the current database context
     * @param tagId the tagId of the file
     * @param resourcePath the path of the file to read
     * 
     * @return CmsBackupResource the backup file
     * @throws CmsException is something goes wrong
     */
    CmsBackupResource readBackupFile(CmsDbContext dbc, int tagId, String resourcePath) throws CmsException;

    /**
     * Reads a backup file header excluding the file content.<p>
     *
     * @param dbc the current database context
     * @param tagId the tagId of the file
     * @param resourcePath the path of the file to read
     * 
     * @return CmsBackupResource the backup file
     * @throws CmsException is something goes wrong
     */
    CmsBackupResource readBackupFileHeader(CmsDbContext dbc, int tagId, String resourcePath) throws CmsException;

    /**
     * Reads all backup file headers  excluding the file content.<p>.
     *
     * @param dbc the current database context
     * 
     * @return List with all backup file headers
     * @throws CmsException if something goes wrong
     */
    List readBackupFileHeaders(CmsDbContext dbc) throws CmsException;

    /**
     * Reads all backup file headers of a file excluding the file content.<p>
     * 
     * @param dbc the current database context
     * @param resourcePath the path of the file to read
     *
     * @return List with all backup file headers
     * @throws CmsException if something goes wrong
     */
    List readBackupFileHeaders(CmsDbContext dbc, String resourcePath) throws CmsException;

    /**
     * Returns the max. current backup version of a resource.<p>
     * 
     * @param dbc the current database context
     * @param resourceId the resource ID of the resource
     * 
     * @return Returns the max. current backup version of a resource.
     * @throws CmsException if something goes wrong
     */
    int readBackupMaxVersion(CmsDbContext dbc, CmsUUID resourceId) throws CmsException;

    /**
     * Reads a backup project.<p>
     *
     * @param dbc the current database context
     * @param tagId the versionId of the project
     * 
     * @return CmsBackupProject the backup project 
     * @throws CmsException is something goes wrong
     */
    CmsBackupProject readBackupProject(CmsDbContext dbc, int tagId) throws CmsException;

    /**
     * Reads all resources that belong to a given backup version ID.<p>
     * 
     * @param dbc the current database context
     * @param tagId the version ID of the backup
     * 
     * @return all resources that belong to the given backup version ID.
     * @throws CmsException if something goes wrong
     */
    List readBackupProjectResources(CmsDbContext dbc, int tagId) throws CmsException;

    /**
     * Returns all projects from the history.<p>
     *
     * @param dbc the current database context
     * 
     * @return a list of projects
     * @throws CmsException if an error occurs
     */
    List readBackupProjects(CmsDbContext dbc) throws CmsException;

    /**
     * Gets the TagId of the first backup project after a given date.<p>
     * 
     * This method is used during the deletion process of older backup data.<p>
     * 
     * @param dbc the current database context
     * @param maxdate the date to compare the backup projects with
     * 
     * @return tag id of the first backup project after maxdate 
     * @throws CmsException if something goes wrong
     */
    int readBackupProjectTag(CmsDbContext dbc, long maxdate) throws CmsException;

    /**
     * Returns a list of all properties of a backup file or folder.<p>
     *
     * @param dbc the current database context
     * @param resource the resource to read the properties from
     * 
     * @return a Map of Strings representing the properties of the resource
     * @throws CmsException if something goes wrong
     */
    List readBackupProperties(CmsDbContext dbc, CmsBackupResource resource) throws CmsException;

    /**
     * Reads a property definition for the specified mapping type.<p>
     * 
     * @param dbc the current database context
     * @param name the name of the propertydefinition to read
     * 
     * @return the propertydefinition that corresponds to the overgiven arguments - or null if there is no valid propertydefinition.
     * @throws CmsException if something goes wrong
     */
    CmsPropertydefinition readBackupPropertyDefinition(CmsDbContext dbc, String name) throws CmsException;

    /**
     * Reads the max. backup tag ID for a specified resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the Cms resource
     * @return the max. backup tag ID
     * 
     * @throws CmsException if something goes wrong
     */
    int readMaxTagId(CmsDbContext dbc, CmsResource resource) throws CmsException;

    /**
     * Gets the next available backup version ID for a resource.<p>
     * 
     * @param dbc the current database context
     * 
     * @return int the next available backup version ID
     */
    int readNextBackupTagId(CmsDbContext dbc);

    /**
     * Writes a project to the backup.<p>
     * 
     * @param dbc the current database context
     * @param currentProject the current project
     * @param tagId the version ID of the backup
     * @param publishDate long timestamp when the current project was published 
     * @param currentUser the current user
     * 
     * @throws CmsException if something goes wrong
     */
    void writeBackupProject(
        CmsDbContext dbc,
        CmsProject currentProject,
        int tagId,
        long publishDate,
        CmsUser currentUser) throws CmsException;

    /**
     * Writes the properties of a resource to the backup.<p>
     * 
     * @param dbc the current database context
     * @param publishProject the current project
     * @param resource the resource of the properties
     * @param properties the properties to write
     * @param backupId the id backup
     * @param tagId the tag ID of the backup
     * @param versionId the version ID of the backup
     * 
     * @throws CmsException if something goes wrong
     */
    void writeBackupProperties(
        CmsDbContext dbc,
        CmsProject publishProject,
        CmsResource resource,
        List properties,
        CmsUUID backupId,
        int tagId,
        int versionId) throws CmsException;

    /**
     * Writes a resource to the backup.<p>
     * 
     * @param dbc the current database context
     * @param currentUser the current user
     * @param publishProject the current project
     * @param resource the resource that is written to the backup
     * @param properties the properties of the resource
     * @param tagId the version ID of the backup
     * @param publishDate long timestamp when the resource was published
     * @param maxVersions maximum number of backup versions
     * 
     * @throws CmsException if something goes wrong
     */
    void writeBackupResource(
        CmsDbContext dbc,
        CmsUser currentUser,
        CmsProject publishProject,
        CmsResource resource,
        List properties,
        int tagId,
        long publishDate,
        int maxVersions) throws CmsException;

    /**
     * Writes a resource content to the backup.<p>
     * 
     * @param context the current database context
     * @param projectId the project to read from
     * @param resource the resource (file header) to read the content from
     * @param backupResource the backup resource to write the backup content to
     * 
     * @throws CmsException if something goes wrong
     */
    void writeBackupResourceContent(
        CmsDbContext context,
        int projectId,
        CmsResource resource,
        CmsBackupResource backupResource) throws CmsException;

}
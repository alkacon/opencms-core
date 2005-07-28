/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/I_CmsBackupDriver.java,v $
 * Date   : $Date: 2005/07/28 10:53:54 $
 * Version: $Revision: 1.52 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.util.CmsUUID;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Definitions of all required backup driver methods.<p>
 * 
 * A backup driver is a driver to write projects, resources and properties of
 * resources optionally to a second set of backup database tables while resources
 * get published. A unique backup tag ID is used to identify a set of resource that 
 * were saved during one backup process.<p>
 * 
 * @author Michael Emmerich 
 * @author Thomas Weckert 
 * 
 * @version $Revision: 1.52 $
 * 
 * @since 6.0.0
 */
public interface I_CmsBackupDriver {

    /** The type ID to identify backup driver implementations. */
    int DRIVER_TYPE_ID = 0;

    /**
     * Creates a new property defintion in the database.<p>
     * 
     * @param dbc the current database context
     * @param name the name of the propertydefinitions to overwrite
     * 
     * @return the new propertydefinition
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsPropertyDefinition createBackupPropertyDefinition(CmsDbContext dbc, String name) throws CmsDataAccessException;

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
     * @throws CmsDataAccessException if something goes wrong
     */
    void deleteBackup(CmsDbContext dbc, CmsBackupResource res, int tag, int versions) throws CmsDataAccessException;

    /**
     * Deletes a property defintion.<p>
     * 
     * @param dbc the current database context
     * @param metadef the propertydefinitions to be deleted
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void deleteBackupPropertyDefinition(CmsDbContext dbc, CmsPropertyDefinition metadef) throws CmsDataAccessException;

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
     * @throws CmsDataAccessException if something goes wrong
     */
    void deleteBackups(CmsDbContext context, List existingBackups, int maxVersions) throws CmsDataAccessException;

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
     * @param tagId the desired tag ID of the file
     * @param resourcePath the path of the file to read
     * @return the backup file
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsBackupResource readBackupFile(CmsDbContext dbc, int tagId, String resourcePath) throws CmsDataAccessException;

    /**
     * Reads all backup file headers  excluding the file content.<p>.
     *
     * @param dbc the current database context
     * 
     * @return List with all backup file headers
     * @throws CmsDataAccessException if something goes wrong
     */
    List readBackupFileHeaders(CmsDbContext dbc) throws CmsDataAccessException;

    /**
     * Reads all file headers of a file.<br>
     * 
     * This method returns a list with the history of all file headers, i.e.
     * the file headers of a file, independent of the project they were attached to.<br>
     *
     * The reading excludes the file content.<p>
     * 
     * The filter is (path OR id).<p>
     *  
     * @param dbc the current database context
     * @param resourcePath the path of the file to read
     * @param id the resource id (usefull for siblings)
     *
     * @return a list of file headers, as <code>{@link CmsBackupResource}</code> objects, read from the Cms
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    List readBackupFileHeaders(CmsDbContext dbc, String resourcePath, CmsUUID id) throws CmsDataAccessException;

    /**
     * Returns the max. current backup version of a resource.<p>
     * 
     * @param dbc the current database context
     * @param resourceId the resource ID of the resource
     * 
     * @return Returns the max. current backup version of a resource
     * @throws CmsDataAccessException if something goes wrong
     */
    int readBackupMaxVersion(CmsDbContext dbc, CmsUUID resourceId) throws CmsDataAccessException;

    /**
     * Reads a backup project.<p>
     *
     * @param dbc the current database context
     * @param tagId the versionId of the project
     * 
     * @return the requested backup project
     * 
     * @throws CmsDataAccessException is something goes wrong
     */
    CmsBackupProject readBackupProject(CmsDbContext dbc, int tagId) throws CmsDataAccessException;

    /**
     * Reads all resources that belong to a given backup version ID.<p>
     * 
     * @param dbc the current database context
     * @param tagId the version ID of the backup
     * 
     * @return all resources that belong to the given backup version ID
     * @throws CmsDataAccessException if something goes wrong
     */
    List readBackupProjectResources(CmsDbContext dbc, int tagId) throws CmsDataAccessException;

    /**
     * Returns all projects from the history.<p>
     *
     * @param dbc the current database context
     * 
     * @return list of <code>{@link CmsBackupProject}</code> objects 
     *           with all projects from history.
     * 
     * @throws CmsDataAccessException if an error occurs
     */
    List readBackupProjects(CmsDbContext dbc) throws CmsDataAccessException;

    /**
     * Gets the TagId of the first backup project after a given date.<p>
     * 
     * This method is used during the deletion process of older backup data.<p>
     * 
     * @param dbc the current database context
     * @param maxdate the date to compare the backup projects with
     * 
     * @return tag id of the first backup project after maxdate 
     * @throws CmsDataAccessException if something goes wrong
     */
    int readBackupProjectTag(CmsDbContext dbc, long maxdate) throws CmsDataAccessException;

    /**
     * Returns a list of all properties of a backup file or folder.<p>
     *
     * @param dbc the current database context
     * @param resource the resource to read the properties from
     * 
     * @return a Map of Strings representing the properties of the resource
     * @throws CmsDataAccessException if something goes wrong
     */
    List readBackupProperties(CmsDbContext dbc, CmsBackupResource resource) throws CmsDataAccessException;

    /**
     * Reads a property definition for the specified mapping type.<p>
     * 
     * @param dbc the current database context
     * @param name the name of the propertydefinition to read
     * 
     * @return the propertydefinition that corresponds to the overgiven arguments - or null if there is no valid propertydefinition
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsPropertyDefinition readBackupPropertyDefinition(CmsDbContext dbc, String name) throws CmsDataAccessException;

    /**
     * Reads the max. backup tag ID for a specified resource.<p>
     * 
     * @param dbc the current database context
     * @param resource the Cms resource
     * @return the max. backup tag ID
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    int readMaxTagId(CmsDbContext dbc, CmsResource resource) throws CmsDataAccessException;

    /**
     * Returns the next available backup version ID for a resource.<p>
     * 
     * @param dbc the current database context
     * 
     * @return the next available backup version ID
     */
    int readNextBackupTagId(CmsDbContext dbc);

    /**
     * Creates a backup of the current project.<p>
     * 
     * @param dbc the current database context
     * @param tagId the version ID of the backup
     * @param publishDate long timestamp when the current project was published. 
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void writeBackupProject(
        CmsDbContext dbc,
        int tagId,
        long publishDate) throws CmsDataAccessException;

    /**
     * Writes the properties of a resource to the backup.<p>
     * @param dbc the current database context
     * @param resource the resource of the properties
     * @param properties the properties to write
     * @param backupId the id backup
     * @param tagId the tag ID of the backup
     * @param versionId the version ID of the backup
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void writeBackupProperties(
        CmsDbContext dbc,
        CmsResource resource,
        List properties,
        CmsUUID backupId,
        int tagId,
        int versionId) throws CmsDataAccessException;

    /**
     * Writes a resource to the backup.<p>
     * @param dbc the current database context
     * @param resource the resource that is written to the backup
     * @param properties the properties of the resource
     * @param tagId the version ID of the backup
     * @param publishDate long timestamp when the resource was published
     * @param maxVersions maximum number of backup versions
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void writeBackupResource(
        CmsDbContext dbc,
        CmsResource resource,
        List properties,
        int tagId,
        long publishDate,
        int maxVersions) throws CmsDataAccessException;

}
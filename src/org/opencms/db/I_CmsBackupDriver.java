/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/I_CmsBackupDriver.java,v $
 * Date   : $Date: 2003/08/20 16:51:16 $
 * Version: $Revision: 1.11 $
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

import com.opencms.core.CmsException;
import com.opencms.file.CmsBackupProject;
import com.opencms.file.CmsBackupResource;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsUser;
import com.opencms.flex.util.CmsUUID;

import java.sql.ResultSet;
import java.sql.SQLException;
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
 * @version $Revision: 1.11 $ $Date: 2003/08/20 16:51:16 $
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
     */
    CmsBackupResource createCmsBackupResourceFromResultSet(ResultSet res, boolean hasContent) throws SQLException, CmsException;

    /**
     * Deletes all backups that are older then the given date.<p>
     *
     * @param maxdate long timestamp of the last version that should remain
     * @return int the oldest remaining version
     * @throws CmsException if something goes wrong
     */
    int deleteBackups(long maxdate) throws CmsException;

    /**
     * Destroys this driver.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    void destroy() throws Throwable;

    /**
     * Returns all projects from the history.
     *
     * @return a Vector of projects
     * @throws CmsException if an error occurs
     */
    Vector getAllBackupProjects() throws CmsException;

    /**
     * Initializes this driver.<p>
     * 
     * @param config the configurations object (opencms.properties)
     * @param dbPoolUrl the URL of the JDBC connection pool
     * @param driverManager the Cms driver manager
     */
    //void init(Configurations config, String dbPoolUrl, CmsDriverManager driverManager);

    /**
     * Initializes the SQL manager for this driver.<p>
     * 
     * To obtain JDBC connections from different pools, further 
     * {online|offline|backup} pool Urls have to be specified.
     * 
     * @param poolUrl the default connection pool URL
     * @return the SQL manager for this driver
     * @see org.opencms.db.generic.CmsSqlManager#setOfflinePoolUrl(String)
     * @see org.opencms.db.generic.CmsSqlManager#setOnlinePoolUrl(String)
     * @see org.opencms.db.generic.CmsSqlManager#setBackupPoolUrl(String)
     */
    org.opencms.db.generic.CmsSqlManager initQueries();

    /**
     * Gets the next available backup version ID for a resource.<p>
     * 
     * @return int the next available backup version ID
     */
    int nextBackupVersionId();

    /**
     * Reads all backup file headers of a file excluding the file content.<p>.
     *
     * @param resourceName the name of the file to read
     * @return Vector with all backup file headers
     * @throws CmsException if something goes wrong
     */
    List readAllBackupFileHeaders(CmsUUID resourceId) throws CmsException;

    /**
     * Reads a backup file including the file content.<p>
     *
     * @param versionId the versionId of the file
     * @param filename the path/name of the file
     * @return CmsBackupResource the backup file
     * @throws CmsException is something goes wrong
     */
    CmsBackupResource readBackupFile(int versionId, CmsUUID resourceId) throws CmsException;

    /**
     * Reads a backup file header excluding the file content.<p>
     *
     * @param versionId the versionId of the file
     * @param filename the path/name of the file
     * @return CmsBackupResource the backup file
     * @throws CmsException is something goes wrong
     */
    CmsBackupResource readBackupFileHeader(int versionId, CmsUUID resourceId) throws CmsException;

    /**
     * Reads a backup project.<p>
     *
     * @param versionId the versionId of the project
     * @return CmsBackupProject the backup project 
     * @throws CmsException is something goes wrong
     */
    CmsBackupProject readBackupProject(int versionId) throws CmsException;

    /**
     * Reads all resources that belong to a given backup version ID.<p>
     * 
     * @param versionId the version ID of the backup
     * @return Vector all resources that belong to the given backup version ID.
     * @throws CmsException if something goes wrong
     */
    Vector readBackupProjectResources(int versionId) throws CmsException;

    /**
     * Writes a project to the backup.<p>
     * 
     * @param currentProject the current project
     * @param versionId the version ID of the backup
     * @param publishDate long timestamp when the current project was published 
     * @param currentUser the current user
     * @throws CmsException if something goes wrong
     */
    void writeBackupProject(CmsProject currentProject, int versionId, long publishDate, CmsUser currentUser) throws CmsException;

    /**
     * Writes a resource to the backup.<p>
     * 
     * @param projectId the ID of the current project
     * @param resource the resource that is written to the backup
     * @param content the file content of the resource
     * @param properties the properties of the resource
     * @param versionId the version ID of the backup
     * @param publishDate long timestamp when the resource was published.
     * @throws CmsException if something goes wrong
     */
    void writeBackupResource(CmsUser currentUser, CmsProject publishProject, CmsResource resource, Map properties, int versionId, long publishDate) throws CmsException;
    
    /**
     * Returns the max. number of backup version of a resource.<p>
     * 
     * @return the max. number of backup version of a resource
     */
    int getMaxResourceVersionCount();
    
    /**
     * Returns the max. current backup version of a resource.<p>
     * 
     * @param resourceId the structure ID of the resource
     * @return Returns the max. current backup version of a resource.
     * @throws CmsException if something goes wrong
     */
    int readMaxBackupVersion(CmsUUID resourceId) throws CmsException;
    
    /**
     * Deletes backup versions of a resource.<p>
     * 
     * Deletes the m-n oldest backup versions, if m is the number of backup versions, and n
     * the number of max. allowed backup versions.
     * 
     * @param existingBackups a list of backup resources ordered by their ascending creation date
     * @throws CmsException if something goes wrong
     * @see org.opencms.db.I_CmsBackupDriver#getMaxResourceVersionCount()
     */
    void deleteBackups(List existingBackups) throws CmsException;

	/**
	 * Internal method to write the backup content.<p>
	 *  
	 * @param backupId			the backup id
	 * @param fileId			the id of the file
	 * @param fileContent		the content of the file
	 * @param versionId			the revision
	 * @throws CmsException		if something goes wrong
	 */
	void writeBackupFileContent(CmsUUID backupId, CmsUUID fileId, byte[] fileContent, int versionId) throws CmsException;
}
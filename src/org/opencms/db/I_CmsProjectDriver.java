/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/I_CmsProjectDriver.java,v $
 * Date   : $Date: 2005/06/22 09:13:15 $
 * Version: $Revision: 1.71 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsUUID;
import org.opencms.workflow.CmsTask;

import java.util.List;
import java.util.Set;

/**
 * Definitions of all required project driver methods. <p>
 * 
 * @author Thomas Weckert 
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.71 $
 * 
 * @since 6.0.0 
 */
public interface I_CmsProjectDriver {

    /** The type ID to identify project driver implementations. */
    int C_DRIVER_TYPE_ID = 1;

    /** The name of the temp file project. */
    String C_TEMP_FILE_PROJECT_NAME = "tempFileProject";

    /**
     * Creates a new project.<p>
     * 
     * @param dbc the current database context
     * @param owner the owner of the project
     * @param group the group for the project
     * @param managergroup the managergroup for the project
     * @param task the base workflow task for the project
     * @param name the name of the project to create
     * @param description the description for the project
     * @param flags the flags for the project
     * @param type the type for the project
     * @param reservedParam reserved optional parameter, should be <code>null</code> on standard OpenCms installations
     * 
     * @return the created <code>{@link CmsProject}</code> instance
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsProject createProject(
        CmsDbContext dbc,
        CmsUser owner,
        CmsGroup group,
        CmsGroup managergroup,
        CmsTask task,
        String name,
        String description,
        int flags,
        int type,
        Object reservedParam) throws CmsDataAccessException;

    /**
     * Creates a new projectResource from a given CmsResource object.<p>
     *
     * @param dbc the current database context
     * @param projectId The project in which the resource will be used
     * @param resourceName The resource to be written to the Cms
     * @param reservedParam reserved optional parameter, should be null on standard OpenCms installations
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void createProjectResource(CmsDbContext dbc, int projectId, String resourceName, Object reservedParam)
    throws CmsDataAccessException;

    /**
     * Deletes all entries in the published resource table.<p>
     * 
     * @param dbc the current database context
     * @param currentProject the current project
     * @param linkType the type of resource deleted (0= non-paramter, 1=parameter)
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void deleteAllStaticExportPublishedResources(CmsDbContext dbc, CmsProject currentProject, int linkType)
    throws CmsDataAccessException;

    /**
     * Deletes a project from the cms.<p>
     * 
     * Therefore it deletes all files, resources and properties.
     * 
     * @param dbc the current database context
     * @param project the project to delete
     * @throws CmsDataAccessException if something goes wrong
     */
    void deleteProject(CmsDbContext dbc, CmsProject project) throws CmsDataAccessException;

    /**
     * Delete a projectResource from an given CmsResource object.<p>
     *
     * @param dbc the current database context
     * @param projectId id of the project in which the resource is used
     * @param resourceName name of the resource to be deleted from the Cms
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void deleteProjectResource(CmsDbContext dbc, int projectId, String resourceName) throws CmsDataAccessException;

    /**
     * Deletes a specified project.<p>
     * 
     * @param dbc the current database context
     * @param project the project to be deleted
     *
     * @throws CmsDataAccessException if operation was not succesful
     */
    void deleteProjectResources(CmsDbContext dbc, CmsProject project) throws CmsDataAccessException;

    /**
     * Deletes all publish history entries with backup tag IDs >=0 and < the specified max. backup tag ID.<p>
     * 
     * @param dbc the current database context
     * @param projectId the ID of the current project
     * @param maxBackupTagId entries with backup tag IDs >=0 and < this max. backup tag ID get deleted
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void deletePublishHistory(CmsDbContext dbc, int projectId, int maxBackupTagId) throws CmsDataAccessException;

    /**
     * Deletes an entry in the published resource table.<p>
     * 
     * @param dbc the current database context
     * @param currentProject the current project
     * @param resourceName The name of the resource to be deleted in the static export
     * @param linkType the type of resource deleted (0= non-paramter, 1=parameter)
     * @param linkParameter the parameters of the resource
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void deleteStaticExportPublishedResource(
        CmsDbContext dbc,
        CmsProject currentProject,
        String resourceName,
        int linkType,
        String linkParameter) throws CmsDataAccessException;

    /**
     * Destroys this driver.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    void destroy() throws Throwable;

    /**
     * Fills the OpenCms database tables with default values.<p>
     * 
     * @param dbc the current database context
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void fillDefaults(CmsDbContext dbc) throws CmsDataAccessException;

    /**
     * Returns the SqlManager of this driver.<p>
     * 
     * @return the SqlManager of this driver
     */
    CmsSqlManager getSqlManager();

    /**
     * Initializes the SQL manager for this driver.<p>
     * 
     * To obtain JDBC connections from different pools, further 
     * {online|offline|backup} pool Urls have to be specified.<p>
     * 
     * @param classname the classname of the SQL manager
     * 
     * @return the SQL manager for this driver
     */
    org.opencms.db.generic.CmsSqlManager initSqlManager(String classname);

    /**
     * Publishes a deleted folder.<p>
     * 
     * @param dbc the current database context
     * @param report the report to log the output to
     * @param m the number of the folder to publish
     * @param n the number of all folders to publish
     * @param onlineProject the online project
     * @param offlineFolder the offline folder to publish
     * @param backupEnabled flag if backup is enabled
     * @param publishDate the publishing date
     * @param publishHistoryId the publish history id
     * @param backupTagId the backup tag id
     * @param maxVersions the maxmum number of backup versions for each resource
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void publishDeletedFolder(
        CmsDbContext dbc,
        I_CmsReport report,
        int m,
        int n,
        CmsProject onlineProject,
        CmsFolder offlineFolder,
        boolean backupEnabled,
        long publishDate,
        CmsUUID publishHistoryId,
        int backupTagId,
        int maxVersions) throws CmsDataAccessException;

    /**
     * Publishes a new, changed or deleted file.<p>
     * 
     * @param dbc the current database context
     * @param report the report to log the output to
     * @param m the number of the file to publish
     * @param n the number of all files to publish
     * @param onlineProject the online project
     * @param offlineResource the offline file to publish
     * @param publishedContentIds contains the UUIDs of already published content records
     * @param backupEnabled flag if backup is enabled
     * @param publishDate the publishing date
     * @param publishHistoryId the publish history id
     * @param backupTagId the backup tag id
     * @param maxVersions the maxmum number of backup versions for each resource
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void publishFile(
        CmsDbContext dbc,
        I_CmsReport report,
        int m,
        int n,
        CmsProject onlineProject,
        CmsResource offlineResource,
        Set publishedContentIds,
        boolean backupEnabled,
        long publishDate,
        CmsUUID publishHistoryId,
        int backupTagId,
        int maxVersions) throws CmsDataAccessException;

    /**
     * Publishes the content record of a file.<p>
     * 
     * The content record is only published unless it's UUID is not contained in publishedContentIds.
     * The calling method has to take care about whether an existing content record has to be deleted 
     * before or not.<p>  
     * 
     * The intention of this method is to get overloaded in a project driver
     * for a specific DB server to shift the binary content from the offline into the online table
     * in a more sophisticated way than in the generic ANSI-SQL implementation of this interface.
     * 
     * @param dbc the current database context
     * @param offlineProject the offline project to read data
     * @param onlineProject the online project to write data
     * @param offlineFileHeader the offline header of the file of which the content gets published
     * @param publishedResourceIds a Set with the UUIDs of the already published content records
     * 
     * @return the published file (online)
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsFile publishFileContent(
        CmsDbContext dbc,
        CmsProject offlineProject,
        CmsProject onlineProject,
        CmsResource offlineFileHeader,
        Set publishedResourceIds) throws CmsDataAccessException;

    /**
     * Publishes a new or changed folder.<p>
     * 
     * @param dbc the current database context
     * @param report the report to log the output to
     * @param m the number of the folder to publish
     * @param n the number of all folders to publish
     * @param onlineProject the online project
     * @param currentFolder the offline folder to publish
     * @param backupEnabled flag if backup is enabled
     * @param publishDate the publishing date
     * @param publishHistoryId the publish history id
     * @param backupTagId the backup tag id
     * @param maxVersions the maxmum number of backup versions for each resource
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void publishFolder(
        CmsDbContext dbc,
        I_CmsReport report,
        int m,
        int n,
        CmsProject onlineProject,
        CmsFolder currentFolder,
        boolean backupEnabled,
        long publishDate,
        CmsUUID publishHistoryId,
        int backupTagId,
        int maxVersions) throws CmsDataAccessException;

    /**
     * Publishes a specified project to the online project.<p>
     * 
     * @param dbc the current database context
     * @param report an I_CmsReport instance to print output messages
     * @param onlineProject the online project
     * @param publishList a Cms publish list
     * @param backupEnabled true if published resources should be written to the Cms backup
     * @param backupTagId the backup tag ID
     * @param maxVersions maximum number of backup versions
     * 
     * @throws CmsException if something goes wrong
     * @see org.opencms.db.CmsDriverManager#getPublishList(CmsDbContext, CmsResource, boolean)
     */
    void publishProject(
        CmsDbContext dbc,
        I_CmsReport report,
        CmsProject onlineProject,
        CmsPublishList publishList,
        boolean backupEnabled,
        int backupTagId,
        int maxVersions) throws CmsException;

    /**
     * Reads a project given the projects id.<p>
     *
     * @param dbc the current database context
     * @param id the id of the project
     * 
     * @return the project read
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsProject readProject(CmsDbContext dbc, int id) throws CmsDataAccessException;

    /**
     * Reads a project.<p>
     *
     * @param dbc the current database context
     * @param name the name of the project
     * 
     * @return the project with the given name
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsProject readProject(CmsDbContext dbc, String name) throws CmsDataAccessException;

    /**
     * Reads the project resource path for a given project and resource path,
     * to validate if a resource path for a given project already exists.<p>
     * 
     * @param dbc the current database context
     * @param projectId the ID of the project for which the resource path is read
     * @param resourcename the project's resource path
     * @param reservedParam reserved optional parameter, should be null on standard OpenCms installations
     * 
     * @return String the project's resource path
     * @throws CmsDataAccessException if something goes wrong
     */
    String readProjectResource(CmsDbContext dbc, int projectId, String resourcename, Object reservedParam)
    throws CmsDataAccessException;

    /**
     * Reads the project resources for a specified project.<p>
     * 
     * @param dbc the current database context
     * @param project the project for which the resource path is read
     * 
     * @return a list of all project resource paths
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    List readProjectResources(CmsDbContext dbc, CmsProject project) throws CmsDataAccessException;

    /**
     * Returns all projects with the given state.<p>
     *
     * @param dbc the current database context
     * @param state the requested project state
     * 
     * @return a list of objects of type <code>{@link CmsProject}</code>
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    List readProjects(CmsDbContext dbc, int state) throws CmsDataAccessException;

    /**
     * Returns all projects, which are accessible by a group.<p>
     *
     * @param dbc the current database context
     * @param group the requesting group
     * 
     * @return a Vector of projects
     * @throws CmsDataAccessException if something goes wrong
     */
    List readProjectsForGroup(CmsDbContext dbc, CmsGroup group) throws CmsDataAccessException;

    /**
     * Returns all projects, which are manageable by a group.<p>
     *
     * @param dbc the current database context
     * @param group The requesting group
     * @return a Vector of projects
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    List readProjectsForManagerGroup(CmsDbContext dbc, CmsGroup group) throws CmsDataAccessException;

    /**
     * Reads all projects which are owned by a specified user.<p>
     *
     * @param dbc the current database context
     * @param user the user
     * 
     * @return a list of objects of type <code>{@link CmsProject}</code>
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    List readProjectsForUser(CmsDbContext dbc, CmsUser user) throws CmsDataAccessException;

    /**
     * Reads all resources that build the "view" of a project.<p>
     *
     * @param dbc the current database context
     * @param project the id of the project in which the resource will be used
     * @param filter the filter for the resources to read
     * 
     * @return a List of resources
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    List readProjectView(CmsDbContext dbc, int project, String filter) throws CmsDataAccessException;

    /**
     * Reads the resources that were published during a publish process for a given publish history ID.<p>
     * 
     * @param dbc the current database context
     * @param projectId the ID of the current project
     * @param publishHistoryId unique int ID to identify the publish process in the publish history
     * 
     * @return a list of <code>{@link org.opencms.db.CmsPublishedResource}</code> objects
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    List readPublishedResources(CmsDbContext dbc, int projectId, CmsUUID publishHistoryId)
    throws CmsDataAccessException;

    /**
     * Returns the parameters of a resource in the table of all published template resources.<p>
     *
     * @param dbc the current database context
     * @param currentProject the current project
     * @param rfsName the rfs name of the resource
     * 
     * @return the paramter string of the requested resource
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    String readStaticExportPublishedResourceParameters(CmsDbContext dbc, CmsProject currentProject, String rfsName)
    throws CmsDataAccessException;

    /**
     * Returns a list of all template resources which must be processed during a static export.<p>
     * 
     * @param dbc the current database context
     * @param currentProject the current project
     * @param parameterResources flag for reading resources with parameters (1) or without (0)
     * @param timestamp the timestamp information
     * 
     * @return a list of template resources as <code>{@link String}</code> objects
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    List readStaticExportResources(CmsDbContext dbc, CmsProject currentProject, int parameterResources, long timestamp)
    throws CmsDataAccessException;

    /**
     * Removes the project id from all resources within a project.<p>
     * 
     * This must be done when a project will deleted
     * 
     * @param dbc the current database context
     * @param project the project to delete
     * @throws CmsDataAccessException if something goes wrong
     */
    void unmarkProjectResources(CmsDbContext dbc, CmsProject project) throws CmsDataAccessException;

    /**
     * Writes an already existing project.<p>
     *
     * The project id has to be a valid OpenCms project id.<br>
     * 
     * The project with the given id will be completely overriden
     * by the given data.<p>
     *
     * @param dbc the current database context
     * @param project the project that should be written
     * 
     * @throws CmsDataAccessException if operation was not successful
     */
    void writeProject(CmsDbContext dbc, CmsProject project) throws CmsDataAccessException;

    /**
     * Inserts an entry in the publish history for a published VFS resource.<p>
     * 
     * @param dbc the current database context
     * @param currentProject the current project
     * @param publishId the ID of the current publishing process
     * @param backupTagId the current backup ID
     * @param resource the state of the resource *before* it was published
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void writePublishHistory(
        CmsDbContext dbc,
        CmsProject currentProject,
        CmsUUID publishId,
        int backupTagId,
        CmsResource resource) throws CmsDataAccessException;

    /**
     * Inserts an entry in the published resource table.<p>
     * 
     * This is done during static export.<p>
     * 
     * @param dbc the current database context
     * @param currentProject the current project
     * @param resourceName The name of the resource to be added to the static export
     * @param linkType the type of resource exported (0= non-paramter, 1=parameter)
     * @param linkParameter the parameters added to the resource
     * @param timestamp a timestamp for writing the data into the db
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void writeStaticExportPublishedResource(
        CmsDbContext dbc,
        CmsProject currentProject,
        String resourceName,
        int linkType,
        String linkParameter,
        long timestamp) throws CmsDataAccessException;

}
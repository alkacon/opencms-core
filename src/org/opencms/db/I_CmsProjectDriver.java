/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/I_CmsProjectDriver.java,v $
 * Date   : $Date: 2004/01/14 12:55:58 $
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
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsUUID;
import org.opencms.workflow.CmsTask;

import com.opencms.core.CmsException;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsRequestContext;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsUser;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * Definitions of all required project driver methods.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com) 
 * @version $Revision: 1.40 $ $Date: 2004/01/14 12:55:58 $
 * @since 5.1
 */
public interface I_CmsProjectDriver {

    /**
     * Creates a link entry for each of the link targets in the linktable.<p>
     *
     * @param pageId The resourceId (offline) of the page whose liks should be traced
     * @param linkTargets A vector of strings (the linkdestinations)
     * @throws CmsException if something goes wrong  
     */
    void createLinkEntries(CmsUUID pageId, Vector linkTargets) throws CmsException;

    /**
     * Creates a link entry for each of the link targets in the online linktable.<p>
     *
     * @param pageId The resourceId (online) of the page whose liks should be traced
     * @param linkTargets A vector of strings (the linkdestinations)
     * @throws CmsException if something goes wrong
     */
    void createLinkEntriesOnline(CmsUUID pageId, Vector linkTargets) throws CmsException;

    /**
    * Creates a project.<p>
    *
    * @param owner The owner of this project
    * @param group The group for this project
    * @param managergroup The managergroup for this project
    * @param task The task
    * @param name The name of the project to create
    * @param description The description for the new project
    * @param flags The flags for the project (e.g. archive)
    * @param type the type for the project (e.g. normal)
    * @param reservedParam reservedParam reserved optional parameter, should be null on standard OpenCms installations
    * @return the new CmsProject instance
    * @throws CmsException Throws CmsException if something goes wrong
    */
    CmsProject createProject(CmsUser owner, CmsGroup group, CmsGroup managergroup, CmsTask task, String name, String description, int flags, int type, Object reservedParam) throws CmsException;

    /**
     * Creates a new projectResource from a given CmsResource object.<p>
     *
     * @param projectId The project in which the resource will be used
     * @param resourceName The resource to be written to the Cms
     * @param reservedParam reserved optional parameter, should be null on standard OpenCms installations
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    void createProjectResource(int projectId, String resourceName, Object reservedParam) throws CmsException;

    /**
     * Creates a serializable object in the systempropertys.<p>
     *
     * @param name the name of the property
     * @param object the property-object
     * @return object the property-object
     * @throws CmsException if something goes wrong
     */
    Serializable createSystemProperty(String name, Serializable object) throws CmsException;

    /**
     * Deletes all entrys in the link table that belong to the pageId.<p>
     *
     * @param pageId the resourceId (offline) of the page whose links should be deleted
     * @throws CmsException if something goes wrong
     */
    void deleteLinkEntries(CmsUUID pageId) throws CmsException;

    /**
     * Deletes all entrys in the online link table that belong to the pageId.<p>
     *
     * @param pageId the resourceId (online) of the page whose links should be deleted
     * @throws CmsException if something goes wrong
     */
    void deleteLinkEntriesOnline(CmsUUID pageId) throws CmsException;

    /**
     * Deletes a project from the cms.<p>
     * 
     * Therefore it deletes all files, resources and properties.
     * @param project the project to delete.
     * @throws CmsException Throws CmsException if something goes wrong
     */
    void deleteProject(CmsProject project) throws CmsException;

    /**
     * Removes the project id from all resources within a project.<p>
     * 
     * This must be done when a project will deleted
     * @param project the project to delete.
     * @throws CmsException Throws CmsException if something goes wrong
     */
    void unmarkProjectResources(CmsProject project) throws CmsException;
    
        
    /**
     * Delete a projectResource from an given CmsResource object.<p>
     *
     * @param projectId id of the project in which the resource is used
     * @param resourceName name of the resource to be deleted from the Cms
     * @throws CmsException if something goes wrong
     */
    void deleteProjectResource(int projectId, String resourceName) throws CmsException;

    /**
     * Deletes a specified project.<p>
     *
     * @param project the project to be deleted
     * @throws CmsException if operation was not succesful
     */
    void deleteProjectResources(CmsProject project) throws CmsException;

    /**
     * Deletes a serializable object from the systempropertys.<p>
     *
     * @param name the name of the property.
     * @throws CmsException if something goes wrong
     */
    void deleteSystemProperty(String name) throws CmsException;

    /**
     * Destroys this driver.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    void destroy() throws Throwable;

    /**
     * Fills the OpenCms database tables with default values.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    void fillDefaults() throws CmsException;

    /**
     * Initializes the SQL manager for this driver.<p>
     * 
     * To obtain JDBC connections from different pools, further 
     * {online|offline|backup} pool Urls have to be specified.
     * 
     * @return the SQL manager for this driver
     * @see org.opencms.db.generic.CmsSqlManager#setPoolUrlOffline(String)
     * @see org.opencms.db.generic.CmsSqlManager#setPoolUrlOnline(String)
     * @see org.opencms.db.generic.CmsSqlManager#setPoolUrlBackup(String)
     */
    org.opencms.db.generic.CmsSqlManager initQueries();

    /**
     * Publishes a deleted folder.<p>
     * 
     * @param context the current request context
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
     * @throws Exception if something goes wrong
     */
    void publishDeletedFolder(CmsRequestContext context, I_CmsReport report, int m, int n, CmsProject onlineProject, CmsFolder offlineFolder, boolean backupEnabled, long publishDate, CmsUUID publishHistoryId, int backupTagId, int maxVersions) throws Exception;

    /**
     * Publishes a new, changed or deleted file.<p>
     *
     * @param context the current request context
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
     * @throws Exception if something goes wrong
     */
    void publishFile(CmsRequestContext context, I_CmsReport report, int m, int n, CmsProject onlineProject, CmsResource offlineResource, Set publishedContentIds, boolean backupEnabled, long publishDate, CmsUUID publishHistoryId, int backupTagId, int maxVersions) throws Exception;
    
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
     * @param offlineProject the offline project to read data
     * @param onlineProject the online project to write data
     * @param offlineFileHeader the offline header of the file of which the content gets published
     * @param publishedContentIds a Set with the UUIDs of the already published content records
     * @return the published file (online)
     * @throws Exception if something goes wrong
     */
    CmsFile publishFileContent(CmsProject offlineProject, CmsProject onlineProject, CmsResource offlineFileHeader, Set publishedContentIds) throws Exception;

    /**
     * Publishes a new or changed folder.<p>
     * 
     * @param context the current request context
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
     * @throws Exception if something goes wrong
     */
    void publishFolder(CmsRequestContext context, I_CmsReport report, int m, int n, CmsProject onlineProject, CmsFolder currentFolder, boolean backupEnabled, long publishDate, CmsUUID publishHistoryId, int backupTagId, int maxVersions) throws Exception;

    /**
     * Publishes a specified project to the online project.<p>
     * 
     * @param context the current request context
     * @param report an I_CmsReport instance to print output messages
     * @param onlineProject the online project
     * @param publishHistoryId unique int ID to identify each publish task in the publish history
     * @param directPublishResource a CmsResource that gets directly published; or null if an entire project gets published
     * @param directPublishSiblings if a CmsResource that should get published directly is provided as an argument, all eventual siblings of this resource get published too, if this flag is true
     * @param backupEnabled true if published resources should be written to the Cms backup
     * @param backupTagId the backup tag ID
     * @param maxVersions maximum number of backup versions
     * @throws Exception if something goes wrong
     */
    void publishProject(CmsRequestContext context, I_CmsReport report, CmsProject onlineProject, CmsUUID publishHistoryId, CmsResource directPublishResource, boolean directPublishSiblings, boolean backupEnabled, int backupTagId, int maxVersions) throws Exception;

    /**
     * Searches for broken links in the online project.<p>
     *
     * @return a Vector with a CmsPageLinks object for each page containing broken links
     *          this CmsPageLinks object contains all links on the page withouth a valid target
     * @throws CmsException if something goes wrong
     */
    Vector readBrokenLinksOnline() throws CmsException;

    /**
     * Reads all export links.<p>
     *
     * @return a Vector(of Strings) with the links
     * @throws CmsException if something goes wrong
     */
    Vector readExportLinks() throws CmsException;

    /**
     * Reads all export links that depend on the resource.<p>
     * 
     * @param resources vector of resources 
     * @return a Vector(of Strings) with the linkrequest names
     * @throws CmsException if something goes wrong
     */
    Vector readExportLinks(Vector resources) throws CmsException;

    /**
     * Returns a Vector (Strings) with the link destinations of all links on the page with
     * the pageId.<p>
     *
     * @param pageId The resourceId (offline) of the page whose liks should be read
     * @return the vector of link destinations
     * @throws CmsException if something goes wrong
     */
    Vector readLinkEntries(CmsUUID pageId) throws CmsException;

    /**
     * Returns a Vector (Strings) with the link destinations of all links on the page with
     * the pageId.<p>
     *
     * @param pageId The resourceId (online) of the page whose liks should be read
     * @return the vector of link destinations
     * @throws CmsException if something goes wrong
     */
    Vector readLinkEntriesOnline(CmsUUID pageId) throws CmsException;

    /**
     * Reads a project by task-id.<p>
     *
     * @param task the task to read the project for
     * @return the project the tasks belongs to
     * @throws CmsException if something goes wrong
     */
    CmsProject readProject(CmsTask task) throws CmsException;

    /**
     * Reads a project.<p>
     *
     * @param id the id of the project
     * @return the project with the given id
     * @throws CmsException if something goes wrong
     */
    CmsProject readProject(int id) throws CmsException;
    
    /**
     * Reads a project.<p>
     *
     * @param name the name of the project
     * @return the project with the given name
     * @throws CmsException if something goes wrong
     */
    CmsProject readProject(String name) throws CmsException;    

    /**
     * Reads log entries for a project.<p>
     *
     * @param projectid the ID of the current project
     * @return a Vector of new TaskLog objects
     * @throws CmsException if something goes wrong
     */
    Vector readProjectLogs(int projectid) throws CmsException;

    /**
     * Reads the project resource path for a given project and resource path,
     * to validate if a resource path for a given project already exists.<p>
     * 
     * @param projectId the ID of the project for which the resource path is read
     * @param resourcename the project's resource path
     * @param reservedParam reserved optional parameter, should be null on standard OpenCms installations
     * @return String the project's resource path
     * @throws CmsException if something goes wrong
     */
    String readProjectResource(int projectId, String resourcename, Object reservedParam) throws CmsException;

    /**
     * Reads the project resources for a specified project.<p>
     * 
     * @param project the project for which the resource path is read
     * @return the project's resource path
     * @throws CmsException if something goes wrong
     */
    List readProjectResources(CmsProject project) throws CmsException;

    /**
     * Returns all projects, with the overgiven state.<p>
     *
     * @param state The state of the projects to read
     * @return a Vector of projects
     * @throws CmsException if something goes wrong
     */
    Vector readProjects(int state) throws CmsException;

    /**
     * Returns all projects, which are accessible by a group.<p>
     *
     * @param group the requesting group
     * @return a Vector of projects
     * @throws CmsException if something goes wrong
     */
    Vector readProjectsForGroup(CmsGroup group) throws CmsException;

    /**
     * Returns all projects, which are manageable by a group.<p>
     *
     * @param group The requesting group
     * @return a Vector of projects
     * @throws CmsException if something goes wrong
     */
    Vector readProjectsForManagerGroup(CmsGroup group) throws CmsException;

    /**
     * Returns all projects, which are owned by a user.<p>
     *
     * @param user The requesting user
     * @return a Vector of projects
     * @throws CmsException if something goes wrong
     */
    Vector readProjectsForUser(CmsUser user) throws CmsException;

    /**
     * Reads all resource from the Cms, that are in one project.<BR/>
     * A resource is either a file header or a folder.<p>
     *
     * @param project The id of the project in which the resource will be used.
     * @param filter The filter for the resources to be read
     * @return A Vecor of resources.
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    List readProjectView(int project, String filter) throws CmsException;
    
    /**
     * Reads the resources that were published in a publish task for a given publish history ID.<p>
     * 
     * @param projectId the ID of the current project
     * @param publishHistoryId unique int ID to identify each publish task in the publish history
     * @return a List of CmsPublishedResource objects
     * @throws CmsException if something goes wrong
     */
    List readPublishedResources(int projectId, CmsUUID publishHistoryId) throws CmsException;

    /**
     * Reads a serializable object from the systempropertys.<p>
     *
     * @param name The name of the property.
     * @return object The property-object.
     * @throws CmsException Throws if something goes wrong
     */
    Serializable readSystemProperty(String name) throws CmsException;

    /**
     * Update the online link table (after a project is published).<p>
     *
     * @param deleted vector (of CmsResources) with the deleted resources of the project
     * @param changed vector (of CmsResources) with the changed resources of the project
     * @param newRes vector (of CmsResources) with the newRes resources of the project
     * @param pageType the page type
     * @throws CmsException if something goes wrong
     */
    void writeProjectLinksOnline(Vector deleted, Vector changed, Vector newRes, int pageType) throws CmsException;

    /**
     * Inserts an entry in the publish history for a published VFS resource.<p>
     * 
     * @param currentProject the current project
     * @param publishId the ID of the current publishing process
     * @param tagId the current backup ID
     * @param resource the state of the resource *before* it was published
     * @throws CmsException if something goes wrong
     */
    void writePublishHistory(CmsProject currentProject, CmsUUID publishId, int tagId, CmsResource resource) throws CmsException;
    
    /**
     * Inserts an entry in the publish history for a published COS resource.<p>
     * 
     * @param currentProject the current project
     * @param publishId the ID of the current publishing process
     * @param tagId the current backup ID
     * @param contentDefinitionName the package/class name of the content definition 
     * @param masterId the content ID of the published module data
     * @param subId the module ID of the published module data
     * @param state the state of the resource *before* it was published
     * @throws CmsException if something goes wrong
     */
    void writePublishHistory(CmsProject currentProject, CmsUUID publishId, int tagId, String contentDefinitionName, CmsUUID masterId, int subId, int state) throws CmsException;

    /**
     * Writes a serializable object to the systemproperties.<p>
     *
     * @param name The name of the property.
     * @param object The property-object.
     * @return object The property-object.
     * @throws CmsException if something goes wrong
     */
    Serializable writeSystemProperty(String name, Serializable object) throws CmsException;
    
    /**
     * Returns the SqlManager of this driver.<p>
     * 
     * @return the SqlManager of this driver
     */
    CmsSqlManager getSqlManager();    
    
    /**
     * Deletes all publish history entries with backup tag IDs >=0 and < the specified max. backup tag ID.<p>
     * 
     * @param projectId the ID of the current project
     * @param maxBackupTagId entries with backup tag IDs >=0 and < this max. backup tag ID get deleted
     * @throws CmsException if something goes wrong
     */
    void deletePublishHistory(int projectId, int maxBackupTagId) throws CmsException;
}
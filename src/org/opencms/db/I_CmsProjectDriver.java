/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/I_CmsProjectDriver.java,v $
 * Date   : $Date: 2003/09/12 14:46:21 $
 * Version: $Revision: 1.19 $
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

import org.opencms.report.I_CmsReport;
import org.opencms.workflow.CmsTask;

import com.opencms.core.CmsException;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsRequestContext;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsUser;
import com.opencms.flex.util.CmsUUID;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * Definitions of all required project driver methods.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.19 $ $Date: 2003/09/12 14:46:21 $
 * @since 5.1
 */
public interface I_CmsProjectDriver {
    
    /**
     * Creates a serializable object in the systempropertys.
     *
     * @param name The name of the property.
     * @param object The property-object.
     * @return object The property-object.
     * @throws CmsException Throws CmsException if something goes wrong.
     */    
    Serializable addSystemProperty(String name, Serializable object) throws CmsException;

    /**
     * creates a link entry for each of the link targets in the linktable.<p>
     *
     * @param pageId The resourceId (offline) of the page whose liks should be traced
     * @param linkTargets A vector of strings (the linkdestinations)
     * @throws CmsException if something goes wrong  
     */    
    void createLinkEntrys(CmsUUID pageId, Vector linkTargets) throws CmsException;

    /**
     * creates a link entry for each of the link targets in the online linktable.<p>
     *
     * @param pageId The resourceId (online) of the page whose liks should be traced
     * @param linkTargets A vector of strings (the linkdestinations)
     * @throws CmsException if something goes wrong
     */
    void createOnlineLinkEntrys(CmsUUID pageId, Vector linkTargets) throws CmsException;

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
    * @return the new CmsProject instance
    * @throws CmsException Throws CmsException if something goes wrong
    */
    CmsProject createProject(CmsUser owner, CmsGroup group, CmsGroup managergroup, CmsTask task, String name, String description, int flags, int type) throws CmsException;
    
    /**
     * This method creates a new session in the database. It is used
     * for sessionfailover.
     *
     * @param sessionId the id of the session
     * @param data the sessionData
     * @throws CmsException if something goes wrong
     */    
    void createSession(String sessionId, Hashtable data) throws CmsException;

    /**
     * Deletes all projectResource from an given CmsProject.<p>
     *
     * @param projectId The project in which the resource is used
     * @throws CmsException Throws CmsException if operation was not succesful
     */    
    void deleteAllProjectResources(int projectId) throws CmsException;

    /**
     * Deletes all entrys in the link table that belong to the pageId.<p>
     *
     * @param pageId The resourceId (offline) of the page whose links should be deleted
     * @throws CmsException if something goes wrong
     */
    void deleteLinkEntrys(CmsUUID pageId) throws CmsException;

    /**
     * Deletes all entrys in the online link table that belong to the pageId.<p>
     *
     * @param pageId The resourceId (online) of the page whose links should be deleted
     * @throws CmsException if something goes wrong
     */
    void deleteOnlineLinkEntrys(CmsUUID pageId) throws CmsException;

    /**
     * Deletes a project from the cms.
     * Therefore it deletes all files, resources and properties.
     *
     * @param project the project to delete.
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    void deleteProject(CmsProject project) throws CmsException;
    
    /**
     * Deletes all properties for a project.<p>
     *
     * @param project the project where all properties should be deleted
     * @throws CmsException if operation was not successful
     */    
    void deleteProjectProperties(CmsProject project) throws CmsException;

    /**
     * delete a projectResource from an given CmsResource object.<p>
     *
     * @param projectId id of the project in which the resource is used
     * @param resourceName name of the resource to be deleted from the Cms
     * @throws CmsException if something goes wrong
     */    
    void deleteProjectResource(int projectId, String resourceName) throws CmsException;

    /**
     * Deletes a specified project
     *
     * @param project The project to be deleted.
     * @throws CmsException  Throws CmsException if operation was not succesful.
     */
    void deleteProjectResources(CmsProject project) throws CmsException;

    /**
     * Deletes old sessions.
     */
    void deleteSessions();

    /**
     * Deletes a serializable object from the systempropertys.
     *
     * @param name The name of the property.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    void deleteSystemProperty(String name) throws CmsException;
    
    /**
     * Destroys this driver.<p>
     * 
     * @throws Throwable if something goes wrong.
     */      
    void destroy() throws Throwable;

    /**
     * Ends a task from the Cms.<p>
     *
     * @param taskId Id of the task to end
     * @throws CmsException if something goes wrong
     */    
    void endTask(int taskId) throws CmsException;

    /**
     * Method to init all default-resources.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    void fillDefaults() throws CmsException;

    /**
     * Forwards a task to another user.
     *
     * @param taskId The id of the task that will be fowarded.
     * @param newRoleId The new Group the task belongs to
     * @param newUserId User who gets the task.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    void forwardTask(int taskId, CmsUUID newRoleId, CmsUUID newUserId) throws CmsException;

    /**
     * Returns all projects, which are accessible by a group.<p>
     *
     * @param group the requesting group
     * @return a Vector of projects
     * @throws CmsException if something goes wrong
     */
    Vector getAllAccessibleProjectsByGroup(CmsGroup group) throws CmsException;

    /**
     * Returns all projects, which are manageable by a group.<p>
     *
     * @param group The requesting group
     * @return a Vector of projects
     * @throws CmsException if something goes wrong
     */
    Vector getAllAccessibleProjectsByManagerGroup(CmsGroup group) throws CmsException;

    /**
     * Returns all projects, which are owned by a user.<p>
     *
     * @param user The requesting user
     * @return a Vector of projects
     * @throws CmsException if something goes wrong
     */
    Vector getAllAccessibleProjectsByUser(CmsUser user) throws CmsException;

    /**
     * Reads all export links.<p>
     *
     * @return a Vector(of Strings) with the links
     * @throws CmsException if something goes wrong
     */
    Vector getAllExportLinks() throws CmsException;

    /**
     * Returns all projects, with the overgiven state.<p>
     *
     * @param state The state of the projects to read
     * @return a Vector of projects
     * @throws CmsException if something goes wrong
     */
    Vector getAllProjects(int state) throws CmsException;

    /**
    * Reads all export links that depend on the resource.<p>
    * 
    * @param resources vector of resources 
    * @return a Vector(of Strings) with the linkrequest names
    * @throws CmsException if something goes wrong
    */
    Vector getDependingExportLinks(Vector resources) throws CmsException;

    /**
     * Searches for broken links in the online project.<p>
     *
     * @return A Vector with a CmsPageLinks object for each page containing broken links
     *          this CmsPageLinks object contains all links on the page withouth a valid target
     * @throws CmsException if something goes wrong
     */
    Vector getOnlineBrokenLinks() throws CmsException;

    /**
     * Retrieves the online project from the database.
     *
     * @return com.opencms.file.CmsProject the  onlineproject for the given project.
     * @throws CmsException Throws CmsException if the resource is not found, or the database communication went wrong.
     */
    CmsProject getOnlineProject() throws CmsException;
    
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
     * Publishes a specified project to the online project.<p>
     *
     * @param context the context
     * @param onlineProject the online project of the OpenCms
     * @param backupEnabled flag if the backup is enabled
     * @param backupTagId the backup tag id
     * @param report a report object to provide the loggin messages
     * @param exportpoints the exportpoints
     * @param directPublishResource the resource of a direct publish
     * @param maxVersions maximum number of backup versions
     * @return a vector of changed or deleted resources
     * @throws CmsException if something goes wrong
     */    
    Vector publishProject(CmsRequestContext context, CmsProject onlineProject, boolean backupEnabled, int backupTagId, I_CmsReport report, Hashtable exportpoints, CmsResource directPublishResource, int maxVersions) throws Exception;

    /**
     * Select all projectResources from an given project.<p>
     *
     * @param projectId the project in which the resource is used
     * @return Vector of resources belongig to the project
     * @throws CmsException if something goes wrong
     */
    Vector readAllProjectResources(int projectId) throws CmsException;

    /**
     * Returns a Vector (Strings) with the link destinations of all links on the page with
     * the pageId.<p>
     *
     * @param pageId The resourceId (offline) of the page whose liks should be read
     * @return the vector of link destinations
     * @throws CmsException if something goes wrong
     */
    Vector readLinkEntrys(CmsUUID pageId) throws CmsException;

    /**
     * Returns a Vector (Strings) with the link destinations of all links on the page with
     * the pageId.<p>
     *
     * @param pageId The resourceId (online) of the page whose liks should be read
     * @return the vector of link destinations
     * @throws CmsException if something goes wrong
     */
    Vector readOnlineLinkEntrys(CmsUUID pageId) throws CmsException;

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
     * Reads log entries for a project.<p>
     *
     * @param projectid the ID of the current project
     * @return A Vector of new TaskLog objects
     * @throws CmsException if something goes wrong
     */    
    Vector readProjectLogs(int projectid) throws CmsException;

    /**
     * Reads all resource from the Cms, that are in one project.<BR/>
     * A resource is either a file header or a folder.
     *
     * @param project The id of the project in which the resource will be used.
     * @param filter The filter for the resources to be read
     * @return A Vecor of resources.
     * @throws CmsException Throws CmsException if operation was not succesful
     */    
    List readProjectView(int project, String filter) throws CmsException;

    /**
     * Reads a session from the database.<p>
     *
     * @param sessionId the id og the session to read
     * @return the session data as Hashtable
     * @throws CmsException if something goes wrong
     */
    Hashtable readSession(String sessionId) throws CmsException;

    /**
     * Reads a serializable object from the systempropertys.
     *
     * @param name The name of the property.
     * @return object The property-object.
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    Serializable readSystemProperty(String name) throws CmsException;

    /**
     * Unlocks all resources in this project.
     *
     * @param project The project to be unlocked.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    void unlockProject(CmsProject project) throws CmsException;

    /**
     * Update the online link table (after a project is published).<p>
     *
     * @param deleted vector (of CmsResources) with the deleted resources of the project
     * @param changed vector (of CmsResources) with the changed resources of the project
     * @param newRes vector (of CmsResources) with the newRes resources of the project
     * @param pageType the page type
     * @throws CmsException if something goes wrong
     */
    void updateOnlineProjectLinks(Vector deleted, Vector changed, Vector newRes, int pageType) throws CmsException;
    
    /**
     * This method updates a session in the database. It is used
     * for sessionfailover.
     *
     * @param sessionId the id of the session
     * @param data the sessionData
     * @return the number of affected sessions (should be 1 for an existing session)
     * @throws CmsException if something goes wrong
     */    
    int updateSession(String sessionId, Hashtable data) throws CmsException;

    /**
     * Deletes a project from the cms.
     * Therefore it deletes all files, resources and properties.
     *
     * @param project the project to delete.
     * @throws CmsException Throws CmsException if something goes wrong.
     */   
    void writeProject(CmsProject project) throws CmsException;
    
    /**
     * Writes a serializable object to the systemproperties.
     *
     * @param name The name of the property.
     * @param object The property-object.
     * @return object The property-object.
     * @throws CmsException Throws CmsException if something goes wrong.
     */    
    Serializable writeSystemProperty(String name, Serializable object) throws CmsException;
    
    /**
     * Inserts an entry in the publish history for a published resource.<p>
     * 
     * @param currentProject the current project
     * @param publishId the ID of the current publishing process
     * @param tagId the current backup ID
     * @param resourcename the name of the resource
     * @param resource the resource that was published
     * @throws CmsException if something goes wrong
     */
    void writePublishHistory(CmsProject currentProject, int publishId, int tagId, String resourcename, CmsResource resource) throws CmsException;
    
    /**
     * Returns the next version number of the publish history.<p>
     * @return a new version number greater than the last used version number 
     * @throws CmsException if something goes wrong
     */
    int nextPublishVersionId() throws CmsException;
    
    /**
     * Publishes a new or changed folder.<p>
     * 
     * @throws CmsException
     */
    void publishFolder(CmsRequestContext context, I_CmsReport report, int m, int n, CmsProject onlineProject, CmsFolder currentFolder, boolean backupEnabled, long publishDate, int publishHistoryId, int backupTagId, int maxVersions) throws Exception;

    /**
     * Publishes a deleted folder.<p>
     * 
     * @throws CmsException
     */
    void publishDeletedFolder(CmsRequestContext context, I_CmsReport report, int m, int n, CmsProject onlineProject, CmsFolder offlineFolder, boolean backupEnabled, long publishDate, int publishHistoryId, int backupTagId, int maxVersions) throws Exception;

    /**
     * Publishes a new, changed or deleted file.<p>
     * 
     * @throws CmsException
     */
    public void publishFile(CmsRequestContext context, I_CmsReport report, int m, int n, CmsProject onlineProject, CmsResource offlineResource, boolean backupEnabled, long publishDate, int publishHistoryId, int backupTagId, int maxVersions) throws Exception;
        
}
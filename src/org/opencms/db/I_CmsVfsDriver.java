/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/I_CmsVfsDriver.java,v $
 * Date   : $Date: 2003/09/08 11:37:51 $
 * Version: $Revision: 1.49 $
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
import com.opencms.file.CmsFile;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsPropertydefinition;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsUser;
import com.opencms.file.I_CmsResourceType;
import com.opencms.flex.util.CmsUUID;
import org.opencms.report.I_CmsReport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Definitions of all required VFS driver methods.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.49 $ $Date: 2003/09/08 11:37:51 $
 * @since 5.1
 */
public interface I_CmsVfsDriver {

    /**
     * Changes the project-id of a resource to the new project
     * for publishing the resource directly
     *
     * @param newProjectId The new project-id
     * @param resourceId The id of the resource to change
     * @throws CmsException if an error occurs
     */    
    //void changeLockedInProject(int newProjectId, CmsUUID resourceId) throws CmsException;

    /**
     * Counts the locked resources in this project.
     *
     * @param project The project to be unlocked.
     * @return the amount of locked resources in this project.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    int countLockedResources(CmsProject project) throws CmsException;    
    
    /**
     * Semi-constructor to create a CmsResource instance from a JDBC result set.<p>
     * 
     * @param res the JDBC ResultSet
     * @param projectId the ID of the current project to adjust the modification date in case the resource is a VFS link
     * @return CmsResource the new CmsResource object
     * @throws SQLException in case the result set does not include a requested table attribute
     * @throws CmsException if the CmsFile object cannot be created by its constructor
     */    
    CmsResource createCmsResourceFromResultSet(ResultSet res, int projectId) throws SQLException, CmsException;
    
    /**
     * Semi-constructor to create a CmsResource instance from a JDBC result set.<p>
     * 
     * @param res the JDBC ResultSet
     * @param projectId the ID of the current project
     * @param hasProjectIdInResultSet true if the SQL select query includes the PROJECT_ID table attribute
     * @return CmsFolder the new CmsFolder
     * @throws SQLException in case the result set does not include a requested table attribute
     */
    CmsFolder createCmsFolderFromResultSet(ResultSet res, int projectId, boolean hasProjectIdInResultSet) throws SQLException;    

    /**
     * Creates a new file from an given CmsFile object and a new filename.
     *
     * @param project The project in which the resource will be used.
     * @param file The file to be written to the Cms.
     * @param userId The Id of the user who changed the resourse.
     * @param parentId The parentId of the resource.
     * @param filename The complete new name of the file (including pathinformation).
     *
     * @return file The created file.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    CmsFile createFile(CmsProject project, CmsFile file, CmsUUID userId, CmsUUID parentId, String filename) throws CmsException;
    
    /**
     * Creates a new file with the given content and resourcetype.<p>
     *
     * @param user The user who wants to create the file
     * @param project The project in which the resource will be used
     * @param filename The complete name of the new file (including pathinformation)
     * @param flags The flags of this resource
     * @param parentId The parentId of the resource
     * @param contents The contents of the new file
     * @param resourceType The resourceType of the new file
     * @return file The created file.
     * @throws CmsException if operation was not successful
     */    
    CmsFile createFile(CmsUser user, CmsProject project, String filename, int flags, CmsUUID parentId, byte[] contents, I_CmsResourceType resourceType) throws CmsException;
    
    /**
     * Writes the content of a file.
     * 
     * @param fileId The ID of the new file
     * @param fileContent The content of the new file
     * @param versionId For the content of a backup file you need to insert the versionId of the backup
     * @param projectId the ID of the current project
     * @param writeBackup true if the content should be written to the backup table
     * @throws CmsException if somethong goes wrong
     */    
    void createFileContent(CmsUUID fileId, byte[] fileContent, int versionId, int projectId, boolean writeBackup) throws CmsException;

    /**
     * Creates a new link from an given CmsResource object and a new filename.<p>
     * 
     * @param project the project where to create the link
     * @param resource the link prototype
     * @param userId the id of the user creating the link
     * @param parentId the id of the folder where the link is created
     * @param filename the name of the link
     * @return a valid link resource
     * @throws CmsException if something goes wrong
     */
    CmsResource createVfsLink(CmsProject project, CmsResource resource, CmsUUID userId, CmsUUID parentId, String filename) throws CmsException;

    /**
     * Creates a new folder from an existing folder object.
     *
     * @param user The user who wants to create the folder.
     * @param project The project in which the resource will be used.
     * @param folder The folder to be written to the Cms.
     * @param parentId The parentId of the resource.
     *
     * @param foldername The complete path of the new name of this folder.
     *
     * @return The created folder.
     * @throws CmsException Throws CmsException if operation was not succesful.
     */    
    CmsFolder createFolder(CmsUser user, CmsProject project, CmsFolder folder, CmsUUID parentId, String foldername) throws CmsException;

    /**
      * Creates a new folder
      *
      * @param user The user who wants to create the folder.
      * @param project The project in which the resource will be used.
      * @param parentId The parentId of the folder.
      * @param fileId The fileId of the folder.
      * @param folderName The complete path to the folder in which the new folder will be created.
      * @param flags The flags of this resource.
      * @param dateLastModified the overwrite modification timestamp
      * @param userLastModified the overwrite modification user
      * @param dateCreated the overwrite creation timestamp  
      * @param userCreated the overwrite creation user         
      *
      * @return The created folder.
      * @throws CmsException Throws CmsException if operation was not succesful.
      */
    CmsFolder createFolder(CmsUser user, CmsProject project, CmsUUID parentId, CmsUUID fileId, String folderName, int flags, long dateLastModified, CmsUUID userLastModified, long dateCreated, CmsUUID userCreated) throws CmsException;

    /**
     * Creates a new projectResource from a given CmsResource object.
     *
     * @param projectId The project in which the resource will be used.
     * @param resourceName The resource to be written to the Cms.
     *
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    void createProjectResource(int projectId, String resourceName) throws CmsException;

    /**
     * Creates the propertydefinitions for the resource type.<BR/>
     *
     * Only the admin can do this.
     *
     * @param name The name of the propertydefinitions to overwrite.
     * @param projectId the project in which the propertydefinition is created
     * @param resourcetype The resource-type for the propertydefinitions.
     * @return the new propertydefinition
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    CmsPropertydefinition createPropertydefinition(String name, int projectId, int resourcetype) throws CmsException;

    /**
     * Creates a new resource from an given CmsResource object.
     *
     * @param project The project in which the resource will be used.
     * @param newResource The resource to be written to the Cms.
     * @param filecontent The filecontent if the resource is a file
     * @param userId The ID of the current user.
     * @param parentId The parentId of the resource.
     * @param isFolder true to create a new folder
     *
     * @return resource The created resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    CmsResource importResource(CmsProject project, CmsUUID parentId, CmsResource newResource, byte[] filecontent, CmsUUID userId, boolean isFolder) throws CmsException;

    /**
     * delete all projectResource from an given CmsProject object.
     *
     * @param projectId The project in which the resource is used.
     *
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    void deleteAllProjectResources(int projectId) throws CmsException;

    /**
     * Deletes all properties for a file or folder.
     *
     * @param projectId the id of the project
     * @param resource the resource
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    void deleteAllProperties(int projectId, CmsResource resource) throws CmsException;
 
    /**
     * Tags a resource as deleted without removing it physically in the database.<p>
     * 
     * @param currentProject the current project
     * @param resource the resource
     * @throws CmsException if something goes wrong
     */
    void deleteFile(CmsProject currentProject, CmsResource resource) throws CmsException;

    /**
     * Deletes the folder.<p>
     *
     * Only empty folders can be deleted yet.
     *
     * @param currentProject The project in which the resource will be used.
     * @param orgFolder The folder that will be deleted.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */    
    void deleteFolder(CmsProject currentProject, CmsFolder orgFolder) throws CmsException;

    /**
     * delete a projectResource from an given CmsResource object.
     *
     * @param projectId The project in which the resource is used.
     * @param resourceName The resource to be deleted from the Cms.
     *
     *
     * @throws CmsException Throws CmsException if operation was not succesful
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
     * Deletes a property for a file or folder.
     *
     * @param meta The property-name of which the property has to be read.
     * @param projectId the id of the project
     * @param resource The resource.
     * @param resourceType The Type of the resource.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    void deleteProperty(String meta, int projectId, CmsResource resource, int resourceType) throws CmsException;

    /**
     * Delete the propertydefinitions for the resource type.<BR/>
     *
     * Only the admin can do this.
     *
     * @param metadef The propertydefinitions to be deleted.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    void deletePropertydefinition(CmsPropertydefinition metadef) throws CmsException;
    //void deleteResource(CmsResource resource) throws CmsException;
    
    /**
     * Destroys this driver.<p>
     * 
     * @throws Throwable if something goes wrong
     */      
    void destroy() throws Throwable;   
    
    /**
     * Tests if a resource with the given resourceId does already exist in the database.<p>
     * 
     * @param projectId the project id
     * @param resourceId the resource id to test for
     * @return true if a resource with the given id was found, false otherweise
     * @throws CmsException if something goes wrong
     */
    boolean existsResourceId(int projectId, CmsUUID resourceId) throws CmsException;  
    
    /**
     * Proves if the specified structure ID in the tables of the specified project {offline|online} exists.<p>
     * 
     * @param projectId the ID of current project
     * @param structureId
     * @return true, if the specified structure ID in the tables of the specified project {offline|online} exists
     * @throws CmsException if something goes wrong
     */   
    boolean existsStructureId(int projectId, CmsUUID structureId) throws CmsException;      
    
    /**
     * Gets a list of all hard and soft links pointing to the content of a resource.<p>
     * 
     * @param currentProject the current project
     * @param resource the specified resource
     * @return a List with the fileheaders
     * @throws CmsException if something goes wrong
     */
    List getAllVfsLinks(CmsProject currentProject, CmsResource resource) throws CmsException;
    
    /**
     * Gets a list of all soft links pointing to the content of a resource, excluding it's
     * hard link, and excluding the resource itself in case it is a soft link.<p>
     * 
     * @param currentProject the current project
     * @param resource the resource
     * @return the list of siblings
     * @throws CmsException if something goes wrong
     */    
    List getAllVfsSoftLinks(CmsProject currentProject, CmsResource resource) throws CmsException;

    /**
     * checks a project for broken links that would appear if the project is published.
     *
     * @param report cmsReport object for logging while the method is still running.
     * @param changed vector (of CmsResources) with the changed resources in the project.
     * @param deleted vector (of CmsResources) with the deleted resources in the project.
     * @param newRes vector (of CmsResources) with the new resources in the project.
     * @throws CmsException if something goes wrong
     */    
    void getBrokenLinks(I_CmsReport report, Vector changed, Vector deleted, Vector newRes) throws CmsException;

    /**
     * Returns a Vector with all resource-names that have set the given property to the given value.
     *
     * @param projectId the id of the project to test.
     * @param propertyDefinition the name of the propertydefinition to check.
     * @param propertyValue the value of the property for the resource.
     * @return Vector with all names of resources.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    Vector getFilesWithProperty(int projectId, String propertyDefinition, String propertyValue) throws CmsException;
    
    /**
     * Builds a DFS list view of all folders in the VFS.
     * 
     * @param currentProject the current project
     * @param parentResource the parent resource from where the tree is built (should be / usually)
     * @return List a DFS list view of all folders in the VFS
     * @throws CmsException if something goes wrong
     */
    List getFolderTree(CmsProject currentProject, CmsResource parentResource) throws CmsException;

    /**
     * This method reads all resource names from the table CmsOnlineResources
     *
     * @return A Vector (of Strings) with the resource names (like from getAbsolutePath())
     * @throws CmsException if something goes wrong
     */    
    Vector getOnlineResourceNames() throws CmsException;

    /**
     * Reads all resources (including the folders) residing in a folder<BR>
     *
     * @param projectId the id of the project
     * @param offlineResource the parent resource id of the offline resoure.
     *
     * @return A Vecor of resources.
     *
     * @throws CmsException if operation was not succesful
     */
    Vector getResourcesInFolder(int projectId, CmsFolder offlineResource) throws CmsException;

    /**
     * Returns a Vector with all resources of the given type
     * that have set the given property. For the start it is
     * only used by the static export so it reads the online project only.
     *
     * @param projectId the id of the project to test.
     * @param propertyDefinition the name of the propertydefinition to check.
     *
     * @return Vector with all resources.
     *
     * @throws CmsException if operation was not succesful.
     */
    Vector getResourcesWithProperty(int projectId, String propertyDefinition) throws CmsException;

    /**
     * Returns a Vector with all resources of the given type
     * that have set the given property to the given value.
     *
     * @param projectId the id of the project to test.
     * @param propertyDefinition the name of the propertydefinition to check.
     * @param propertyValue the value of the property for the resource.
     * @param resourceType the value of the resourcetype.
     *
     * @return Vector with all resources.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    Vector getResourcesWithProperty(int projectId, String propertyDefinition, String propertyValue, int resourceType) throws CmsException;

    /**
     * Gets all resources that are marked as undeleted.
     * @param resources Vector of resources
     * @return Returns all resources that are markes as deleted
     */
    List getUndeletedResources(List resources);
    
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
     * Reads all propertydefinitions for the given resource type.
     *
     * @param projectId the id of the project
     * @param resourcetype The resource type to read the propertydefinitions for.
     *
     * @return propertydefinitions A Vector with propertydefefinitions for the resource type.
     * The Vector is maybe empty.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */    
    Vector readAllPropertydefinitions(int projectId, I_CmsResourceType resourcetype) throws CmsException;

    /**
     * Reads all propertydefinitions for the given resource type.
     *
     * @param projectId the id of the project
     * @param resourcetype The resource type to read the propertydefinitions for.
     *
     * @return propertydefinitions A Vector with propertydefefinitions for the resource type.
     * The Vector is maybe empty.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    Vector readAllPropertydefinitions(int projectId, int resourcetype) throws CmsException;

    /**
     * Reads resources with a given version id.<p>
     *
     * @param versionId the version id to lookup
     * @return the list of resources with the given version id
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    Vector readBackupProjectResources(int versionId) throws CmsException;
    
    /**
     * Reads a file.<p>
     * 
     * @param projectId the ID of the current project
     * @param includeDeleted true if should be read even if it's state is deleted
     * @param resourceId the id of the file
     * @return CmsFile the file
     * @throws CmsException if something goes wrong
     */        
    CmsFile readFile(int projectId, boolean includeDeleted, CmsUUID resourceId) throws CmsException;
    
    /**
     * Reads a file header from the Cms.<BR/>
     * The reading excludes the filecontent.
     *
     * @param projectId The Id of the project
     * @param resource The resource.
     *
     * @return file The read file.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */    
    CmsFile readFileHeader(int projectId, CmsResource resource) throws CmsException;

    /**
     * Reads a file header from the Cms.<BR/>
     * The reading excludes the filecontent.
     *
     * @param projectId The Id of the project
     * @param resourceId The Id of the resource.
     * @param includeDeleted true if already deleted files are included
     *
     * @return file The read file.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    CmsFile readFileHeader(int projectId, CmsUUID resourceId, boolean includeDeleted) throws CmsException;

    /**
     * Reads a file header from the Cms.<BR/>
     * The reading excludes the filecontent.
     *
     * @param projectId The Id of the project in which the resource will be used.
     * @param parentId the id of the parent folder
     * @param filename The name of the file
     * @param includeDeleted true if already deleted files are included
     *
     * @return file The read file.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    CmsFile readFileHeader(int projectId, CmsUUID parentId, String filename, boolean includeDeleted) throws CmsException;

    /**
     * Reads all files from the Cms, that are in one project.<BR/>
     *
     * @param projectId The project in which the files are.
     * @param includeUnchanged true if unchanged files are included
     * @param onlyProject true if only resources aree included that are changed in the project
     *
     * @return A Vecor of files.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    // List readFiles(int projectId, boolean includeUnchanged, boolean onlyProject) throws CmsException;

    /**
     * Reads all files from the Cms, that are of the given type.<BR/>
     *
     * @param projectId A project id for reading online or offline resources
     * @param resourcetype The type of the files.
     *
     * @return A Vector of files.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    List readFiles(int projectId) throws CmsException;
    Vector readFilesByType(int projectId, int resourcetype) throws CmsException;    

    /**
     * Reads a folder from the Cms.<BR/>
     *
     * @param projectId The project in which the resource will be used.
     * @param folderId The id of the folder to be read.
     *
     * @return The read folder.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */
    CmsFolder readFolder(int projectId, CmsUUID folderId) throws CmsException;
    
    /**
     * Reads a folder from the Cms.<BR/>
     *
     * @param projectId The project in which the resource will be used.
     * @param parentId the id of the parent folder
     * @param foldername The name of the folder to be read.
     *
     * @return The read folder.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
     */    
    CmsFolder readFolder(int projectId, CmsUUID parentId, String filename) throws CmsException;
    
    /**
     * Reads all folders from the Cms, that are in one project.<BR/>
     *
     * @param currentProject The project in which the folders are.
     * @return A Vecor of folders.
     * @throws CmsException Throws CmsException if operation was not succesful
     */    
    List readFolders(int projectId) throws CmsException;
    
    /**
     * Reads the project resource path for a given project and resource path,
     * to validate if a resource path for a given project already exists.<p>
     * 
     * @param projectId the ID of the project for which the resource path is read
     * @param resourcename the project's resource path
     * @return String the project's resource path
     * @throws CmsException if something goes wrong
     */
    String readProjectResource(int projectId, String resourcename) throws CmsException;
    
    /**
     * Reads the project resources for a specified project.<p>
     * 
     * @param project the project for which the resource path is read
     * @return the project's resource path
     * @throws CmsException if something goes wrong
     */
    List readProjectResources(CmsProject project) throws CmsException;
    
    /**
     * Reads all properties of a resource.<p>
     * 
     * @param projectId the ID of the current project
     * @param resource the resource where the properties are read
     * @param resourceType the type of the resource
     * @return HashMap all properties key/value encoded
     * @throws CmsException if something goes wrong
     */
    HashMap readProperties(int projectId, CmsResource resource, int resourceType) throws CmsException;
    
    /**
     * Reads a property of a resource.<p>
     * 
     * @param meta the name of the property
     * @param projectId the ID of the current project
     * @param resource the resource where the property is read
     * @param resourceType the type of the resource
     * @return String the value of the property
     * @throws CmsException if something goes wrong
     */    
    String readProperty(String meta, int projectId, CmsResource resource, int resourceType) throws CmsException;
    
    /**
     * Reads a propertydefinition for the given resource type.
     *
     * @param name The name of the propertydefinition to read.
     * @param projectId the id of the project
     * @param type The resource type for which the propertydefinition is valid.
     *
     * @return propertydefinition The propertydefinition that corresponds to the overgiven
     * arguments - or null if there is no valid propertydefinition.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */    
    CmsPropertydefinition readPropertydefinition(String name, int projectId, I_CmsResourceType type) throws CmsException;

    /**
     * Reads a propertydefinition for the given resource type.
     *
     * @param name The name of the propertydefinition to read.
     * @param projectId the id of the project
     * @param type The resource type for which the propertydefinition is valid.
     *
     * @return propertydefinition The propertydefinition that corresponds to the overgiven
     * arguments - or null if there is no valid propertydefinition.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    CmsPropertydefinition readPropertydefinition(String name, int projectId, int type) throws CmsException;
    CmsResource readResource(CmsProject project, CmsUUID parentId, String filename) throws CmsException;
    Vector readResources(CmsProject project) throws CmsException;
    Vector readResourcesLikeName(CmsProject project, String resourcename) throws CmsException;
    
    /**
     * Removes a resource physically in the database.<p>
     * 
     * @param currentProject the current project
     * @param resource the resource
     * @throws CmsException if something goes wrong
     */
    void removeFile(CmsProject currentProject, CmsResource resource) throws CmsException;
    
    /**
     * Removes a resource physically in the database.<p>
     * 
     * @param currentProject currentProject the current project
     * @param parentId the ID of the parent resource
     * @param filename the filename of the resource
     * @throws CmsException if something goes wrong
     */
    void removeFile(CmsProject currentProject, CmsUUID parentId, String filename) throws CmsException;

	/**
	 * Removes a folder and its subfolders physically in the database.<p>
	 * The contents of the folders must have been already deleted
	 *
	 * @param currentProject the current project
	 * @param folder the folder
	 * @throws CmsException if something goes wrong
	 */    
    void removeFolder(CmsProject currentProject, CmsFolder folder) throws CmsException;
    
    /**
     * Removes a single folder physically in the database.<p>
     * The folder is removed without deleting its subresources.
     * 
     * @param currentProject the current project
     * @param structureId the structure id of the folder
     * @throws CmsException if something goes wrong
     */
    void removeResource(CmsProject currentProject, CmsResource resource) throws CmsException;
    
    void removeTemporaryFile(CmsResource file) throws CmsException;
    int renameResource(CmsUser currentUser, CmsProject currentProject, CmsResource resource, String newResourceName) throws CmsException;
    void updateLockstate(CmsResource res, int projectId) throws CmsException;
    
      
    void updateResourceState(CmsProject project, CmsResource resource, int changed) throws CmsException;
    void writeFile(CmsProject project, CmsFile file, int changed) throws CmsException;
    void writeFile(CmsProject project, CmsFile file, int changed, CmsUUID userId) throws CmsException;
    
    /**
     * Writes the file content of an existing file
     * 
     * @param projectId the ID of the current project
     * @param writeBackup true if the file content should be written to the backup table
     * @param fileId The ID of the file to update
     * @param fileContent The new content of the file
     * @throws CmsException is something goes wrong
     */    
    void writeFileContent(CmsUUID fileId, byte[] fileContent, int projectId, boolean writeBackup) throws CmsException;
    
    void writeFileHeader(CmsProject project, CmsFile file, int changed) throws CmsException;
    void writeFileHeader(CmsProject project, CmsFile file, int changed, CmsUUID userId) throws CmsException;
    void writeFolder(CmsProject project, CmsFolder folder, int changed) throws CmsException;
    void writeFolder(CmsProject project, CmsFolder folder, int changed, CmsUUID userId) throws CmsException;
    void writeProperties(Map propertyinfos, int projectId, CmsResource resource, int resourceType) throws CmsException;
    void writeProperties(Map propertyinfos, int projectId, CmsResource resource, int resourceType, boolean addDefinition) throws CmsException;
    
    /**
     * Writes a property for a file or folder with
     * added escaping of property values as MySQL doesn't support Unicode strings
     *
     * @param meta The property-name of which the property has to be read
     * @param projectId the ID of the current project
     * @param value The value for the property to be set
     * @param resource The resource
     * @param resourceType The Type of the resource
     * @param addDefinition true if a new property definition should be added automatically
     * @throws CmsException Throws CmsException if operation was not succesful
     */    
    void writeProperty(String meta, int projectId, String value, CmsResource resource, int resourceType, boolean addDefinition) throws CmsException;
    
    CmsPropertydefinition writePropertydefinition(CmsPropertydefinition metadef) throws CmsException;
    void writeResource(CmsProject project, CmsResource resource, byte[] filecontent, int changed, CmsUUID userId) throws CmsException;
    
    /**
     * Publishes the content of an existing offline resource into it's existing online counterpart.<p>
     * 
     * @param onlineResource the online resource
     * @param offlineResource the offline resource
     * @throws CmsException if somethong goes wrong
     */
    void publishResource(CmsResource onlineResource, CmsResource offlineResource) throws CmsException;
        
    /**
     * Moves a resource to a new destination folder.<p>
     * 
     * @param currentUser the current user
     * @param currentProject the current project
     * @param resource the resource that is moved
     * @param destinationFolder the destination folder where the resource is moved
     * @throws CmsException if something goes wrong
     * @return the number of affected resources (should be 1 in a consistent database)
     */
    void moveResource(CmsUser currentUser, CmsProject currentProject, CmsResource resource, CmsResource destinationFolder, String resourceName) throws CmsException;
    
    /**
     * Gets all sub folders or sub files in a given parent folder.<p>
     * 
     * @param currentUser the current user
     * @param currentProject the current project
     * @param parentFolder the parent folder
     * @param getSubFolders true if the sub folders of the parent folder are requested, false if the sub files are requested
     * @return a list of all sub folders or sub files
     * @throws CmsException if something goes wrong
     */
    List getSubResources(CmsProject currentProject, CmsFolder parentFolder, boolean getSubFolders) throws CmsException;
    
    /**
     * Gets all resources with a modification date within a given time frame.<p>
     * 
     * @param currentProject the current project
     * @param starttime the begin of the time range
     * @param endtime the end of the time range
     * @return List with all resources
     * @throws CmsException if operation was not succesful 
     */
    List getResourcesInTimeRange(int currentProject, long starttime, long endtime) throws CmsException;
    
    /**
     * Reads the file headers of all locked resources.<p>
     * 
     * @return
     * @throws CmsException if something goes wrong
     */
    List readLockedFileHeaders() throws CmsException;
        
    /**
     * Updates the project ID attrib. of a resource record.<p>
     * 
     * @param project the resource record is updated with the ID of this project
     * @param resource the resource that gets updated
     * @throws CmsException if something goes wrong
     */
    void updateProjectId(CmsProject project, CmsResource resource) throws CmsException;
    
    /**
     * Sets the project ID for a list of resources back to 0 after a project was published.<p>
     * 
     * @param currentProject the current project
     * @param resources the project ID of these resources get set back
     * @throws CmsException if somethong goes wrong
     */
    void resetProjectId(CmsProject currentProject, CmsResource resource) throws CmsException;
    
    /**
     * Replaces the content and properties of an existing resource.<p>
     * 
     * @param currentUser the current user
     * @param currentProject the current project
     * @param res the new resource
     * @param newResContent the new content
     * @param newResType the resource type
     * @param loaderId the new loader id
     * @throws CmsException if something goes wrong
     */
    void replaceResource(CmsUser currentUser, CmsProject currentProject, CmsResource res, byte[] newResContent, int newResType, int loaderId) throws CmsException;
    
    /**
     * Proves if the specified content ID in the tables of the specified project {offline|online} exists.<p>
     * 
     * @param projectId the ID of current project
     * @param contentId
     * @return true, if the specified content ID in the tables of the specified project {offline|online} exists
     * @throws CmsException if something goes wrong
     */
    boolean existsContentId(int projectId, CmsUUID contentId) throws CmsException;    
     
}
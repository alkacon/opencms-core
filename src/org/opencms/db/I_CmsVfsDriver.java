/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/I_CmsVfsDriver.java,v $
 * Date   : $Date: 2003/11/07 12:36:10 $
 * Version: $Revision: 1.65 $
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
import org.opencms.util.CmsUUID;

import com.opencms.core.CmsException;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsPropertydefinition;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsUser;
import com.opencms.file.I_CmsResourceType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Definitions of all required VFS driver methods.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.65 $ $Date: 2003/11/07 12:36:10 $
 * @since 5.1
 */
public interface I_CmsVfsDriver {
    
    /**
     * Creates a CmsFile instance from a JDBC ResultSet.<p>
     * 
     * @param res the JDBC ResultSet
     * @param projectId the project id
     * @return the new CmsFile
     * @throws SQLException in case the result set does not include a requested table attribute
     */
    CmsFile createFile(ResultSet res, int projectId) throws SQLException;
    
    /**
     * Creates a CmsFile instance from a JDBC ResultSet.<p>
     * 
     * @param res the JDBC ResultSet
     * @param projectId the project id
     * @param hasFileContentInResultSet flag to include the file content
     * @return the new CmsFile
     * @throws SQLException in case the result set does not include a requested table attribute
     */
    CmsFile createFile(ResultSet res, int projectId, boolean hasFileContentInResultSet) throws SQLException;

    /**
     * Creates a CmsFolder instance from a JDBC ResultSet.<p>
     * 
     * @param res the JDBC ResultSet
     * @param projectId the ID of the current project
     * @param hasProjectIdInResultSet true if the SQL select query includes the PROJECT_ID table attribute
     * @return CmsFolder the new CmsFolder
     * @throws SQLException in case the result set does not include a requested table attribute
     */
    CmsFolder createFolder(ResultSet res, int projectId, boolean hasProjectIdInResultSet) throws SQLException;

    /**
     * Creates a CmsResource instance from a JDBC ResultSet.<p>
     * 
     * @param res the JDBC ResultSet
     * @param projectId the ID of the current project to adjust the modification date in case the resource is a VFS link
     * @return CmsResource the new CmsResource object
     * @throws SQLException in case the result set does not include a requested table attribute
     */
    CmsResource createResource(ResultSet res, int projectId) throws SQLException;

    /**
     * Creates a new file in the database from a specified CmsFile instance.<p>
     *
     * @param project the project in which the resource will be used.
     * @param file the file to be written to the Cms.
     * @param userId the Id of the user who changed the resourse.
     * @param parentId the parentId of the resource.
     * @param filename the complete new name of the file (including pathinformation).
     * @return file the created file.
     * @throws CmsException f operation was not succesful
     */
    CmsFile createFile(CmsProject project, CmsFile file, CmsUUID userId, CmsUUID parentId, String filename) throws CmsException;

    /**
     * Creates a new file in the database from a list of arguments.<p>
     *
     * @param user the user who wants to create the file
     * @param project the project in which the resource will be used
     * @param filename the complete name of the new file (including pathinformation)
     * @param flags the flags of this resource
     * @param parentFolder the parent folder of the resource
     * @param contents the contents of the new file
     * @param resourceType the resourceType of the new file
     * @return file the created file.
     * @throws CmsException if operation was not successful
     */
    CmsFile createFile(CmsUser user, CmsProject project, String filename, int flags, CmsFolder parentFolder, byte[] contents, I_CmsResourceType resourceType) throws CmsException;

    /**
     * Creates a BLOB in the database for the content of a file.<p>
     * 
     * @param fileId the ID of the new file
     * @param fileContent the content of the new file
     * @param versionId for the content of a backup file you need to insert the versionId of the backup
     * @param projectId the ID of the current project
     * @param writeBackup true if the content should be written to the backup table
     * @throws CmsException if somethong goes wrong
     */
    void createFileContent(CmsUUID fileId, byte[] fileContent, int versionId, int projectId, boolean writeBackup) throws CmsException;

    /**
     * Creates a new folder in the database from a specified CmsFolder instance.<p>
     *
     * @param project the project in which the resource will be used
     * @param folder the folder to be written to the Cms
     * @param parentId the parentId of the resource
     * @return the created folder
     * @throws CmsException if operation was not succesful
     */
    CmsFolder createFolder(CmsProject project, CmsFolder folder, CmsUUID parentId) throws CmsException;

    /**
      * Creates a new folder in the database from a list of arguments.<p>
      *
      * @param project the project in which the resource will be used
      * @param parentId the parentId of the folder
      * @param fileId the fileId of the folder
      * @param folderName the complete path to the folder in which the new folder will be created
      * @param flags the flags of this resource
      * @param dateLastModified the overwrite modification timestamp
      * @param userLastModified the overwrite modification user
      * @param dateCreated the overwrite creation timestamp  
      * @param userCreated the overwrite creation user         
      * @return the created folder.
      * @throws CmsException if operation was not succesful
      */
    CmsFolder createFolder(CmsProject project, CmsUUID parentId, CmsUUID fileId, String folderName, int flags, long dateLastModified, CmsUUID userLastModified, long dateCreated, CmsUUID userCreated) throws CmsException;

    /**
     * Creates a new property defintion in the databse.<p>
     *
     * Only the admin can do this.
     *
     * @param name the name of the propertydefinitions to overwrite
     * @param projectId the project in which the propertydefinition is created
     * @param resourcetype the resource-type for the propertydefinitions
     * @return the new propertydefinition
     * @throws CmsException if something goes wrong
     */
    CmsPropertydefinition createPropertyDefinition(String name, int projectId, int resourcetype) throws CmsException;

    /**
     * Creates a new sibling for a specified resource.<p>
     * 
     * @param project the project where to create the link
     * @param resource the link prototype
     * @param userId the id of the user creating the link
     * @param parentId the id of the folder where the link is created
     * @param filename the name of the link
     * @return a valid link resource
     * @throws CmsException if something goes wrong
     */
    CmsResource createSibling(CmsProject project, CmsResource resource, CmsUUID userId, CmsUUID parentId, String filename) throws CmsException;

    /**
     * Deletes all properties of a specified resources.<p>
     *
     * @param projectId the id of the project
     * @param resource the resource
     * @throws CmsException if operation was not succesful
     */
    void deleteProperties(int projectId, CmsResource resource) throws CmsException;

    /**
     * Deletes a single property of a specified resource.<p>
     *
     * @param meta the property-name of which the property has to be read
     * @param projectId the id of the project
     * @param resource the resource
     * @param resourceType the Type of the resource
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    void deleteProperty(String meta, int projectId, CmsResource resource, int resourceType) throws CmsException;

    /**
     * Deletes a property defintion.<p>
     *
     * Only the admin can do this.
     *
     * @param metadef the propertydefinitions to be deleted.
     * @throws CmsException if something goes wrong
     */
    void deletePropertyDefinition(CmsPropertydefinition metadef) throws CmsException;

    /**
     * Destroys this driver.<p>
     * 
     * @throws Throwable if something goes wrong
     */
    void destroy() throws Throwable;

    /**
     * Validates if the specified content ID in the tables of the specified project {offline|online} exists.<p>
     * 
     * @param projectId the ID of current project
     * @param contentId the content id
     * @return true, if the specified content ID in the tables of the specified project {offline|online} exists
     * @throws CmsException if something goes wrong
     */
    boolean validateContentIdExists(int projectId, CmsUUID contentId) throws CmsException;

    /**
     * Validates if the specified resource ID in the tables of the specified project {offline|online} exists.<p>
     * 
     * @param projectId the project id
     * @param resourceId the resource id to test for
     * @return true if a resource with the given id was found, false otherweise
     * @throws CmsException if something goes wrong
     */
    boolean validateResourceIdExists(int projectId, CmsUUID resourceId) throws CmsException;

    /**
     * Validates if the specified structure ID in the tables of the specified project {offline|online} exists.<p>
     * 
     * @param projectId the ID of current project
     * @param structureId the structure id
     * @return true, if the specified structure ID in the tables of the specified project {offline|online} exists
     * @throws CmsException if something goes wrong
     */
    boolean validateStructureIdExists(int projectId, CmsUUID structureId) throws CmsException;

    /**
     * Reads all siblings that point to the resource record of a specified resource.<p>
     * 
     * @param currentProject the current project
     * @param resource the specified resource
     * @param includeDeleted true if deleted siblings should be included in the result List
     * @return a List with the fileheaders
     * @throws CmsException if something goes wrong
     */
    List readSiblings(CmsProject currentProject, CmsResource resource, boolean includeDeleted) throws CmsException;

    /**
     * Reads the resource names (including the site-root and path) of all resources having set
     * the given property with the specified value.<p> 
     *
     * @param projectId the id of the project to test
     * @param propertyDefinition the name of the propertydefinition to check
     * @param propertyValue the value of the property for the resource
     * @return vector with all names of resources
     *
     * @throws CmsException if operation was not succesful
     */
    Vector readResourceNames(int projectId, String propertyDefinition, String propertyValue) throws CmsException;

    /**
     * Reads all folders in a DFS list view.<p>
     * 
     * @param currentProject the current project
     * @param parentResource the parent resource from where the tree is built (should be / usually)
     * @return List a DFS list view of all folders in the VFS
     * @throws CmsException if something goes wrong
     */
    List readFolderTree(CmsProject currentProject, CmsResource parentResource) throws CmsException;

    /**
     * Reads all resources with a modification date within a given time range.<p>
     * 
     * @param currentProject the current project
     * @param starttime the begin of the time range
     * @param endtime the end of the time range
     * @return List with all resources
     * @throws CmsException if operation was not succesful 
     */
    List readResources(int currentProject, long starttime, long endtime) throws CmsException;

    /**
     * Reads all resources inside a given project and with a given state.<p>
     * 
     * @param currentProject the current project
     * @param state the state to match
     * @param mode flag signaling the read mode. Valid values are C_READMODE_IGNORESTATE,
     * C_READMODE_MATCHSTATE, C_READMODE_UNMATCHSTATE. 
     * @return List with all resources
     * @throws CmsException if operation was not succesful 
     */
    List readResources(int currentProject, int state, int mode) throws CmsException;

    
    
    /**
     * Reads all resources that have set the specified property.<p>
     *
     * @param projectId the id of the project to test
     * @param propertyDefinition the name of the propertydefinition to check
     * @return Vector with all resources
     * @throws CmsException if operation was not succesful
     */
    Vector readResources(int projectId, String propertyDefinition) throws CmsException;

    /**
     * Reads all resources that have set the given property to the specified value.<p>
     *
     * @param projectId the id of the project to test
     * @param propertyDefinition the name of the propertydefinition to check
     * @param propertyValue the value of the property for the resource
     * @param resourceType the value of the resourcetype
     * @return vector with all resources
     * @throws CmsException if operation was not succesful
     */
    Vector readResources(int projectId, String propertyDefinition, String propertyValue, int resourceType) throws CmsException;

    /**
     * Reads either all child-files or child-folders of a specified parent folder.<p>
     * 
     * @param currentProject the current project
     * @param parentFolder the parent folder
     * @param getSubFolders true if the sub folders of the parent folder are requested, false if the sub files are requested
     * @return a list of all sub folders or sub files
     * @throws CmsException if something goes wrong
     */
    List readChildResources(CmsProject currentProject, CmsFolder parentFolder, boolean getSubFolders) throws CmsException;

    /**
     * Creates a new resource from an given CmsResource instance while it is imported.<p>
     *
     * @param project the project in which the resource will be used
     * @param newResource the resource to be written to the Cms
     * @param filecontent the filecontent if the resource is a file
     * @param userId the ID of the current user
     * @param parentId the parentId of the resource
     * @param isFolder true to create a new folder
     * @return resource the created resource
     * @throws CmsException if operation was not succesful
     */
    CmsResource importResource(CmsProject project, CmsUUID parentId, CmsResource newResource, byte[] filecontent, CmsUUID userId, boolean isFolder) throws CmsException;

    /**
     * Initializes the SQL manager for this driver.<p>
     * 
     * To obtain JDBC connections from different pools, further 
     * {online|offline|backup} pool Urls have to be specified
     * 
     * @return the SQL manager for this driver
     * @see org.opencms.db.generic.CmsSqlManager#setOfflinePoolUrl(String)
     * @see org.opencms.db.generic.CmsSqlManager#setOnlinePoolUrl(String)
     * @see org.opencms.db.generic.CmsSqlManager#setBackupPoolUrl(String)
     */
    org.opencms.db.generic.CmsSqlManager initQueries();

    /**
     * Reads all property definitions for the specified resource type.<p>
     *
     * @param projectId the id of the project
     * @param resourcetype the resource type to read the propertydefinitions for
     * @return propertydefinitions a Vector with propertydefefinitions for the resource type (The Vector can be empty)
     * @throws CmsException if something goes wrong
     */
    Vector readPropertyDefinitions(int projectId, I_CmsResourceType resourcetype) throws CmsException;

    /**
     * Reads a file specified by it's structure ID.<p>
     * 
     * @param projectId the ID of the current project
     * @param includeDeleted true if should be read even if it's state is deleted
     * @param resourceId the id of the file
     * @return CmsFile the file
     * @throws CmsException if something goes wrong
     */
    CmsFile readFile(int projectId, boolean includeDeleted, CmsUUID resourceId) throws CmsException;

    /**
     * Reads a file header specified by it's structure ID.<p>
     *
     * @param projectId the Id of the project
     * @param resourceId the Id of the resource.
     * @param includeDeleted true if already deleted files are included
     * @return file the read file.
     * @throws CmsException if operation was not succesful
     */
    CmsFile readFileHeader(int projectId, CmsUUID resourceId, boolean includeDeleted) throws CmsException;

    /**
     * Reads a file header specified by it's resource name and parent ID.<p>
     *
     * @param projectId the Id of the project in which the resource will be used
     * @param parentId the id of the parent folder
     * @param filename the name of the file
     * @param includeDeleted true if already deleted files are included
     * @return the read file.
     * @throws CmsException if operation was not succesful
     */
    CmsFile readFileHeader(int projectId, CmsUUID parentId, String filename, boolean includeDeleted) throws CmsException;

    /**
     * Reads all files that are either new, changed or deleted.<p>
     *
     * @param projectId a project id for reading online or offline resources
     * @return a list of files
     * @throws CmsException if operation was not succesful
     */
    List readFiles(int projectId) throws CmsException;

    /**
     * Reads all files that are either new, changed or deleted.<p>
     * 
     * @param projectId a project id for reading online or offline resources
     * @param resourcetype the resourcetype of the files
     * @return a vector of files
     * @throws CmsException if operation was not succesful
     */
    Vector readFiles(int projectId, int resourcetype) throws CmsException;

    /**
     * Reads a folder specified by it's structure ID.<p>
     *
     * @param projectId the project in which the resource will be used
     * @param folderId the id of the folder to be read
     * @return the read folder
     * @throws CmsException if operation was not succesful
     */
    CmsFolder readFolder(int projectId, CmsUUID folderId) throws CmsException;

    /**
     * Reads a folder specified by it's resource name and parent ID.<p>
     *
     * @param projectId the project in which the resource will be used
     * @param parentId the id of the parent folder
     * @param foldername the name of the folder to be read
     * @return The read folder.
     * @throws CmsException if operation was not succesful
     */
    CmsFolder readFolder(int projectId, CmsUUID parentId, String foldername) throws CmsException;

    /**
     * Reads all folders that are new, changed or deleted.<p>
     *
     * @param projectId the project in which the folders are
     * @return a Vecor of folders
     * @throws CmsException  if operation was not succesful
     */
    List readFolders(int projectId) throws CmsException;

    /**
     * Reads all properties of a specified resource.<p>
     * 
     * @param projectId the ID of the current project
     * @param resource the resource where the properties are read
     * @param resourceType the type of the resource
     * @return HashMap all properties key/value encoded
     * @throws CmsException if something goes wrong
     */
    Map readProperties(int projectId, CmsResource resource, int resourceType) throws CmsException;

    /**
     * Reads a single property of a specified resource.<p>
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
     * Reads a property definition for the soecified resource type.<p>
     *
     * @param name the name of the propertydefinition to read
     * @param projectId the id of the project
     * @param type the resource type for which the propertydefinition is valid
     * @return the propertydefinition that corresponds to the overgiven arguments - or null if there is no valid propertydefinition.
     * @throws CmsException if something goes wrong
     */
    CmsPropertydefinition readPropertyDefinition(String name, int projectId, int type) throws CmsException;
    
    /**
     * Removes a file physically in the database.<p>
     * 
     * @param currentProject the current project
     * @param resource the resource
     * @param removeFileContent if true, the content record is also removed; if false, only the structure/resource records are removed
     * @throws CmsException if something goes wrong
     */
    void removeFile(CmsProject currentProject, CmsResource resource, boolean removeFileContent) throws CmsException;

    /**
     * Removes a folder physically in the database.<p>
     *
     * @param currentProject the current project
     * @param folder the folder
     * @throws CmsException if something goes wrong
     */
    void removeFolder(CmsProject currentProject, CmsFolder folder) throws CmsException;

    /**
     * Removes a temporary file physically in the database.<p>
     * 
     * @param file the resource from which to remove the temp files
     * @throws CmsException if something goes wrong
     */
    void removeTempFile(CmsResource file) throws CmsException;
    
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
     * Writes the "last-modified-in-project" ID of a resource.<p>
     * 
     * @param project the resource record is updated with the ID of this project
     * @param projectId the project id to write into the reource
     * @param resource the resource that gets updated
     * @throws CmsException if something goes wrong
     */
    void writeLastModifiedProjectId(CmsProject project, int projectId, CmsResource resource) throws CmsException;

    /**
     * Writes the structure and resource records of an existing offline resource into it's online counterpart while it is published.<p>
     * 
     * @param onlineProject the online project
     * @param onlineResource the online resource
     * @param offlineResource the offline resource
     * @param writeFileContent true, if also the content record of the specified offline resource should be written to the online table; false otherwise
     * @throws CmsException if somethong goes wrong
     */
    void writeResource(CmsProject onlineProject, CmsResource onlineResource, CmsResource offlineResource, boolean writeFileContent) throws CmsException;
    
    /**
     * Writes either the structure or resource state.<p>
     * 
     * @param project the current project
     * @param resource the resource to be modified
     * @param changed defines which state must be modified
     * @throws CmsException if somethong goes wrong
     */
    void writeResourceState(CmsProject project, CmsResource resource, int changed) throws CmsException;
    
    /**
     * Writes the file content of a specified file ID.<p>
     * 
     * @param projectId the ID of the current project
     * @param writeBackup true if the file content should be written to the backup table
     * @param fileId The ID of the file to update
     * @param fileContent The new content of the file
     * @throws CmsException if something goes wrong
     */
    void writeFileContent(CmsUUID fileId, byte[] fileContent, int projectId, boolean writeBackup) throws CmsException;

    /**
     * Writes the complete structure and resource records of an existing file.<p>
     * 
     * @param project the current project
     * @param file the file to update
     * @param changed defines which state must be modified
     * @param userId the user who writes the file
     * @throws CmsException if something goes wrong
     */
    void writeFileHeader(CmsProject project, CmsFile file, int changed, CmsUUID userId) throws CmsException;
    
    /**
     * Writes the complete structure and resource records of an existing folder.<p>
     * 
     * @param project the current project
     * @param folder the folder to update
     * @param changed defines which state must be modified
     * @param userId the user who writes the folder
     * @throws CmsException if something goes wrong
     */
    void writeFolder(CmsProject project, CmsFolder folder, int changed, CmsUUID userId) throws CmsException;
    
    /**
     * Writes the properties of an existing resource.<p>
     * 
     * @param propertyinfos mayp of proeprties
     * @param projectId the ID of the current project
     * @param resource the resurce  to update
     * @param resourceType the type of the resource 
     * @param addDefinition flag for creating unknown property definitions
     * @throws CmsException if something goes wrong
     */
    void writeProperties(Map propertyinfos, int projectId, CmsResource resource, int resourceType, boolean addDefinition) throws CmsException;

    /**
     * Writes a single property of an existing resource.<p>
     *
     * @param meta The property-name of which the property has to be read
     * @param projectId the ID of the current project
     * @param value The value for the property to be set
     * @param resource The resource
     * @param resourceType The Type of the resource
     * @param addDefinition true if a new property definition should be added automatically
     * @throws CmsException if operation was not succesful
     */
    void writeProperty(String meta, int projectId, String value, CmsResource resource, int resourceType, boolean addDefinition) throws CmsException;

    /**
     * Writes the complete structure and resource records of a file.<p>
     *
     * @param project the current project
     * @param resource the resource to write
     * @param filecontent the content of the resource
     * @param changed defines which state must be modified
     * @param userId the user who writes the file
     * @throws CmsException if something goes wrong
     */
    void writeResource(CmsProject project, CmsResource resource, byte[] filecontent, int changed, CmsUUID userId) throws CmsException;
    
    /**
     * Returns the SqlManager of this driver.<p>
     * 
     * @return the SqlManager of this driver
     */
    CmsSqlManager getSqlManager();

}
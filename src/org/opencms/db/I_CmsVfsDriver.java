/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/I_CmsVfsDriver.java,v $
 * Date   : $Date: 2003/09/17 09:30:16 $
 * Version: $Revision: 1.58 $
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
 * @version $Revision: 1.58 $ $Date: 2003/09/17 09:30:16 $
 * @since 5.1
 */
public interface I_CmsVfsDriver {
    
    /**
     * Creates a CmsFile instance from a JDBC ResultSet.<p>
     * 
     * @param res
     * @param projectId
     * @return
     * @throws SQLException
     * @throws CmsException
     */
    CmsFile createFile(ResultSet res, int projectId) throws SQLException, CmsException;
    
    /**
     * Creates a CmsFile instance from a JDBC ResultSet.<p>
     * 
     * @param res
     * @param projectId
     * @param hasFileContentInResultSet
     * @return
     * @throws SQLException
     * @throws CmsException
     */
    CmsFile createFile(ResultSet res, int projectId, boolean hasFileContentInResultSet) throws SQLException, CmsException;

    /**
     * Creates a CmsFolder instance from a JDBC ResultSet.<p><p>
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
     * @throws CmsException if the CmsFile object cannot be created by its constructor
     */
    CmsResource createResource(ResultSet res, int projectId) throws SQLException, CmsException;

    /**
     * Creates a new file in the database from a specified CmsFile instance.<p>
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
     * Creates a new file in the database from a list of arguments.<p>
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
    CmsFile createFile(CmsUser user, CmsProject project, String filename, int flags, CmsFolder parentFolder, byte[] contents, I_CmsResourceType resourceType) throws CmsException;

    /**
     * Creates a BLOB in the database for the content of a file.<p>
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
     * Creates a new folder in the database from a specified CmsFolder instance.<p>
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
    CmsFolder createFolder(CmsProject project, CmsFolder folder, CmsUUID parentId) throws CmsException;

    /**
      * Creates a new folder in the database from a list of arguments.<p>
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
    CmsFolder createFolder(CmsProject project, CmsUUID parentId, CmsUUID fileId, String folderName, int flags, long dateLastModified, CmsUUID userLastModified, long dateCreated, CmsUUID userCreated) throws CmsException;

    /**
     * Creates a new property defintion in the databse.<p>
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
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    void deleteProperties(int projectId, CmsResource resource) throws CmsException;

    /**
     * Deletes a single property of a specified resource.<p>
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
     * Deletes a property defintion.<p>
     *
     * Only the admin can do this.
     *
     * @param metadef The propertydefinitions to be deleted.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
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
     * @param contentId
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
     * @param structureId
     * @return true, if the specified structure ID in the tables of the specified project {offline|online} exists
     * @throws CmsException if something goes wrong
     */
    boolean validateStructureIdExists(int projectId, CmsUUID structureId) throws CmsException;

    /**
     * Reads all siblings that point to the resource record of a specified resource.<p>
     * 
     * @param currentProject the current project
     * @param resource the specified resource
     * @return a List with the fileheaders
     * @throws CmsException if something goes wrong
     */
    List readSiblings(CmsProject currentProject, CmsResource resource) throws CmsException;

    /**
     * Reads the resource names (including the site-root and path) of all resources having set
     * the given property with the specified value.<p> 
     *
     * @param projectId the id of the project to test.
     * @param propertyDefinition the name of the propertydefinition to check.
     * @param propertyValue the value of the property for the resource.
     * @return Vector with all names of resources.
     *
     * @throws CmsException Throws CmsException if operation was not succesful.
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
     * Reads all resources that have set the specified property.<p>
     *
     * @param projectId the id of the project to test.
     * @param propertyDefinition the name of the propertydefinition to check.
     *
     * @return Vector with all resources.
     *
     * @throws CmsException if operation was not succesful.
     */
    Vector readResources(int projectId, String propertyDefinition) throws CmsException;

    /**
     * Reads all resources that have set the given property to the specified value.<p>
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
    Vector readResources(int projectId, String propertyDefinition, String propertyValue, int resourceType) throws CmsException;

    /**
     * Reads either all child-files or child-folders of a specified parent folder.<p>
     * 
     * @param currentUser the current user
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
     * Reads all property definitions for the specified resource type.<p>
     *
     * @param projectId the id of the project
     * @param resourcetype The resource type to read the propertydefinitions for.
     *
     * @return propertydefinitions A Vector with propertydefefinitions for the resource type.
     * The Vector is maybe empty.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
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
     * Reads a file header specified by it's resource name and parent ID.<p>
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
     * Reads all files that are either new, changed or deleted.<p>
     *
     * @param projectId A project id for reading online or offline resources
     * @param resourcetype The type of the files.
     *
     * @return A Vector of files.
     *
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    List readFiles(int projectId) throws CmsException;

    Vector readFiles(int projectId, int resourcetype) throws CmsException;

    /**
     * Reads a folder specified by it's structure ID.<p>
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
     * Reads a folder specified by it's resource name and parent ID.<p>
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
     * Reads all folders that are new, changed or deleted.<p>
     *
     * @param currentProject The project in which the folders are.
     * @return A Vecor of folders.
     * @throws CmsException Throws CmsException if operation was not succesful
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
     * @param name The name of the propertydefinition to read.
     * @param projectId the id of the project
     * @param type The resource type for which the propertydefinition is valid.
     *
     * @return propertydefinition The propertydefinition that corresponds to the overgiven
     * arguments - or null if there is no valid propertydefinition.
     *
     * @throws CmsException Throws CmsException if something goes wrong.
     */
    CmsPropertydefinition readPropertyDefinition(String name, int projectId, int type) throws CmsException;
    
    /**
     * Removes a file physically in the database.<p>
     * 
     * @param currentProject the current project
     * @param resource the resource
     * @throws CmsException if something goes wrong
     */
    void removeFile(CmsProject currentProject, CmsResource resource) throws CmsException;

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
     * @param file
     * @throws CmsException
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
     * @param resource the resource that gets updated
     * @throws CmsException if something goes wrong
     */
    void writeLastModifiedProjectId(CmsProject project, int projectId, CmsResource resource) throws CmsException;

    /**
     * Writes the structure and resource records of an existing offline resource into it's online counterpart while it is published.<p>
     * 
     * @param onlineResource the online resource
     * @param offlineResource the offline resource
     * @throws CmsException if somethong goes wrong
     */
    void writeResource(CmsResource onlineResource, CmsResource offlineResource) throws CmsException;
    
    /**
     * Writes either the structure or resource state.<p>
     * 
     * @param project
     * @param resource
     * @param changed
     * @throws CmsException
     */
    void writeResourceState(CmsProject project, CmsResource resource, int changed) throws CmsException;
    
    /**
     * Writes the file content of a specified file ID.<p>
     * 
     * @param projectId the ID of the current project
     * @param writeBackup true if the file content should be written to the backup table
     * @param fileId The ID of the file to update
     * @param fileContent The new content of the file
     * @throws CmsException is something goes wrong
     */
    void writeFileContent(CmsUUID fileId, byte[] fileContent, int projectId, boolean writeBackup) throws CmsException;

    /**
     * Writes the complete structure and resource records of an existing file.<p>
     * 
     * @param project
     * @param file
     * @param changed
     * @param userId
     * @throws CmsException
     */
    void writeFileHeader(CmsProject project, CmsFile file, int changed, CmsUUID userId) throws CmsException;
    
    /**
     * Writes the complete structure and resource records of an existing folder.<p>
     * 
     * @param project
     * @param folder
     * @param changed
     * @param userId
     * @throws CmsException
     */
    void writeFolder(CmsProject project, CmsFolder folder, int changed, CmsUUID userId) throws CmsException;
    
    /**
     * Writes the properties of an existing resource.<p>
     * 
     * @param propertyinfos
     * @param projectId
     * @param resource
     * @param resourceType
     * @param addDefinition
     * @throws CmsException
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
     * @throws CmsException Throws CmsException if operation was not succesful
     */
    void writeProperty(String meta, int projectId, String value, CmsResource resource, int resourceType, boolean addDefinition) throws CmsException;

    /**
     * Writes an existing property defintion.<p>
     * 
     * @param metadef
     * @return
     * @throws CmsException
     */
    CmsPropertydefinition writePropertyDefinition(CmsPropertydefinition metadef) throws CmsException;
    
    
    void writeResource(CmsProject project, CmsResource resource, byte[] filecontent, int changed, CmsUUID userId) throws CmsException;

}
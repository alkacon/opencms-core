/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/I_CmsVfsDriver.java,v $
 * Date   : $Date: 2005/09/16 13:16:16 $
 * Version: $Revision: 1.112.2.2 $
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
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.util.CmsUUID;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Definitions of all required VFS driver methods.<p>
 * 
 * @author Thomas Weckert  
 * @author Michael Emmerich  
 * 
 * @version $Revision: 1.112.2.2 $
 * 
 * @since 6.0.0 
 */
public interface I_CmsVfsDriver {

    /** The type ID to identify user driver implementations. */
    int DRIVER_TYPE_ID = 3;

    /**
     * Creates a resource content with the specified id.<p>
     * 
     * @param dbc the current database context
     * @param project the current project
     * @param resourceId the resource id to create the content for
     * @param content the content to write
     * @param versionId for the content of a backup file you need to insert the versionId of the backup
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void createContent(CmsDbContext dbc, CmsProject project, CmsUUID resourceId, byte[] content, int versionId)
    throws CmsDataAccessException;

    /**
     * Creates a CmsFile instance from a JDBC ResultSet.<p>
     * 
     * @param res the JDBC ResultSet
     * @param projectId the project id
     * 
     * @return the created file
     * @throws SQLException in case the result set does not include a requested table attribute
     */
    CmsFile createFile(ResultSet res, int projectId) throws SQLException;

    /**
     * Creates a CmsFile instance from a JDBC ResultSet.<p>
     * 
     * @param res the JDBC ResultSet
     * @param projectId the project id
     * @param hasFileContentInResultSet flag to include the file content
     * 
     * @return the created file
     * @throws SQLException in case the result set does not include a requested table attribute
     */
    CmsFile createFile(ResultSet res, int projectId, boolean hasFileContentInResultSet) throws SQLException;

    /**
     * Creates a CmsFolder instance from a JDBC ResultSet.<p>
     * 
     * @param res the JDBC ResultSet
     * @param projectId the ID of the current project
     * @param hasProjectIdInResultSet true if the SQL select query includes the PROJECT_ID table attribute
     * 
     * @return the created folder
     * @throws SQLException in case the result set does not include a requested table attribute
     */
    CmsFolder createFolder(ResultSet res, int projectId, boolean hasProjectIdInResultSet) throws SQLException;

    /**
     * Creates a new property defintion in the database.<p>
     * 
     * @param dbc the current database context
     * @param projectId the project in which the propertydefinition is created
     * @param name the name of the propertydefinitions to overwrite
     * 
     * @return the new propertydefinition
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsPropertyDefinition createPropertyDefinition(CmsDbContext dbc, int projectId, String name)
    throws CmsDataAccessException;

    /**
     * Creates a new resource from a given CmsResource object.<p>
     * 
     * This method works for both files and folders. Existing resources get overwritten.<p>
     * 
     * @param dbc the current database context
     * @param project the current project
     * @param resource the resource to be created
     * @param content the file content, or null in case of a folder
     * @return the created Cms resource
     * 
     * @throws CmsDataAccessException if somethong goes wrong
     * 
     * @see org.opencms.file.types.I_CmsResourceType#createResource(org.opencms.file.CmsObject, CmsSecurityManager, String, byte[], List)
     * @see org.opencms.file.types.I_CmsResourceType#importResource(org.opencms.file.CmsObject, CmsSecurityManager, String, CmsResource, byte[], List)
     * @see org.opencms.file.CmsObject#createResource(String, int, byte[], List)
     * @see org.opencms.file.CmsObject#importResource(String, CmsResource, byte[], List)
     */
    CmsResource createResource(CmsDbContext dbc, CmsProject project, CmsResource resource, byte[] content)
    throws CmsDataAccessException;

    /**
     * Creates a CmsResource instance from a JDBC ResultSet.<p>
     * 
     * @param res the JDBC ResultSet
     * @param projectId the ID of the current project to adjust the modification date in case the resource is a VFS link
     * 
     * @return the created resource
     * @throws SQLException in case the result set does not include a requested table attribute
     */
    CmsResource createResource(ResultSet res, int projectId) throws SQLException;

    /**
     * Creates a new sibling for a specified resource.<p>
     * @param dbc the current database context
     * @param project the project where to create the link
     * @param resource the link prototype
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void createSibling(CmsDbContext dbc, CmsProject project, CmsResource resource) throws CmsDataAccessException;

    /**
     * Deletes a property defintion.<p>
     *
     * @param dbc the current database context
     * @param name the property definitions to be deleted
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void deletePropertyDefinition(CmsDbContext dbc, CmsPropertyDefinition name) throws CmsDataAccessException;

    /**
     * Deletes all property values of a file or folder.<p>
     * 
     * You may specify which whether just structure or resource property values should
     * be deleted, or both of them.<p>
     * 
     * @param dbc the current database context
     * @param projectId the id of the project
     * @param resource the resource
     * @param deleteOption determines which property values should be deleted
     * 
     * @throws CmsDataAccessException if something goes wrong
     * @see org.opencms.file.CmsProperty#DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES
     * @see org.opencms.file.CmsProperty#DELETE_OPTION_DELETE_STRUCTURE_VALUES
     * @see org.opencms.file.CmsProperty#DELETE_OPTION_DELETE_RESOURCE_VALUES
     */
    void deletePropertyObjects(CmsDbContext dbc, int projectId, CmsResource resource, int deleteOption)
    throws CmsDataAccessException;

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
     * To obtain JDBC connections from different pools, further 
     * {online|offline|backup} pool Urls have to be specified.<p>
     * 
     * @param classname the classname of the SQL manager
     * 
     * @return the SQL manager for this driver
     */
    org.opencms.db.generic.CmsSqlManager initSqlManager(String classname);

    /**
     * Publishes the structure and resource records of an 
     * offline resource into it's online counterpart.<p>
     * 
     * @param dbc the current database context
     * @param onlineProject the online project
     * @param onlineResource the online resource
     * @param offlineResource the offline resource
     * @param writeFileContent true, if also the content record of the specified offline resource should be written to the online table; false otherwise
     * 
     * @throws CmsDataAccessException if somethong goes wrong
     */
    void publishResource(
        CmsDbContext dbc,
        CmsProject onlineProject,
        CmsResource onlineResource,
        CmsResource offlineResource,
        boolean writeFileContent) throws CmsDataAccessException;

    /**
     * Reads all child-files and/or child-folders of a specified parent resource.<p>
     * 
     * @param dbc the current database context
     * @param currentProject the current project
     * @param resource the parent folder
     * @param getFolders if true the child folders of the parent folder are returned in the result set
     * @param getFiles if true the child files of the parent folder are returned in the result set
     * 
     * @return a list of all sub folders or sub files
     * @throws CmsDataAccessException if something goes wrong
     */
    List readChildResources(
        CmsDbContext dbc,
        CmsProject currentProject,
        CmsResource resource,
        boolean getFolders,
        boolean getFiles) throws CmsDataAccessException;

    /**
     * Reads a file specified by it's structure ID.<p>
     * 
     * @param dbc the current database context
     * @param projectId the ID of the current project
     * @param includeDeleted true if should be read even if it's state is deleted
     * @param structureId the id of the file
     * 
     * @return the file that was read
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsFile readFile(CmsDbContext dbc, int projectId, boolean includeDeleted, CmsUUID structureId)
    throws CmsDataAccessException;

    /**
     * Reads a folder specified by it's structure ID.<p>
     *
     * @param dbc the current database context
     * @param projectId the project in which the resource will be used
     * @param folderId the id of the folder to be read
     * 
     * @return the read folder
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsFolder readFolder(CmsDbContext dbc, int projectId, CmsUUID folderId) throws CmsDataAccessException;

    /**
     * Reads a folder specified by it's resource name.<p>
     * 
     * @param dbc the current database context
     * @param projectId the project in which the resource will be used
     * @param foldername the name of the folder to be read
     * 
     * @return the read folder
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsFolder readFolder(CmsDbContext dbc, int projectId, String foldername) throws CmsDataAccessException;

    /**
     * Reads a property definition for the soecified resource type.<p>
     * 
     * If no property definition with the given name is found, 
     * <code>null</code> is returned.<p>
     * 
     * @param dbc the current database context
     * @param name the name of the propertydefinition to read
     * @param projectId the id of the project
     * 
     * @return the property definition that was read, 
     *          or <code>null</code> if there is no property definition with the given name.
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsPropertyDefinition readPropertyDefinition(CmsDbContext dbc, String name, int projectId)
    throws CmsDataAccessException;

    /**
     * Reads all property definitions for the specified mapping type.<p>
     *
     * @param dbc the current database context
     * @param projectId the id of the project
     * 
     * @return a list with the <code>{@link CmsPropertyDefinition}</code> objects (may be empty)
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    List readPropertyDefinitions(CmsDbContext dbc, int projectId) throws CmsDataAccessException;

    /**
     * Reads a property object from the database specified by it's key name mapped to a resource.<p>
     * 
     * The implementation must return {@link CmsProperty#getNullProperty()} if the property is not found.<p>
     * 
     * @param dbc the current database context
     * @param key the key of the property
     * @param project the current project
     * @param resource the resource where the property is attached to
     * 
     * @return a CmsProperty object containing both the structure and resource value of the property
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsProperty readPropertyObject(CmsDbContext dbc, String key, CmsProject project, CmsResource resource)
    throws CmsDataAccessException;

    /**
     * Reads all property objects mapped to a specified resource from the database.<p>
     * 
     * The implementation must return an empty list if no properties are found at all.<p>
     * 
     * @param dbc the current database context
     * @param project the current project
     * @param resource the resource where the property is attached to
     * 
     * @return a list with CmsProperty objects containing both the structure and resource value of the property
     * @throws CmsDataAccessException if something goes wrong
     */
    List readPropertyObjects(CmsDbContext dbc, CmsProject project, CmsResource resource) throws CmsDataAccessException;

    /**
     * Reads a resource specified by it's structure ID.<p>
     * 
     * @param dbc the current database context
     * @param projectId the Id of the project
     * @param structureId the Id of the resource
     * @param includeDeleted true if already deleted files are included
     *
     * @return the resource that was read
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsResource readResource(CmsDbContext dbc, int projectId, CmsUUID structureId, boolean includeDeleted)
    throws CmsDataAccessException;

    /**
     * Reads a resource specified by it's resource name.<p>
     * 
     * @param dbc the current database context
     * @param projectId the Id of the project in which the resource will be used
     * @param filename the name of the file
     * @param includeDeleted true if already deleted files are included
     * 
     * @return the resource that was read
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsResource readResource(CmsDbContext dbc, int projectId, String filename, boolean includeDeleted)
    throws CmsDataAccessException;

    /**
     * Reads all resources inside a given project and with a given state.<p>
     * 
     * @param dbc the current database context
     * @param currentProject the current project
     * @param state the state to match
     * @param mode flag signaling the read mode
     *  
     * @return a list with all resources that where read
     * @throws CmsDataAccessException if somethong goes wrong
     */
    List readResources(CmsDbContext dbc, int currentProject, int state, int mode) throws CmsDataAccessException;

    /**
     * Returns all resources associated to a given principal via an ACE.<p> 
     * 
     * @param dbc the current database context
     * @param project the to read the entries from
     * @param principalId the id of the principal

     * @return a list of <code>{@link org.opencms.file.CmsResource}</code> objects
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    List readResourcesForPrincipalACE(CmsDbContext dbc, CmsProject project, CmsUUID principalId)
    throws CmsDataAccessException;

    /**
     * Returns all resources associated to a given principal through some of following attributes.<p> 
     * 
     * <ul>
     *    <li>User Created</li>
     *    <li>User Last Modified</li>
     * </ul><p>
     * 
     * @param dbc the current database context
     * @param project the to read the entries from
     * @param principalId the id of the principal

     * @return a list of <code>{@link org.opencms.file.CmsResource}</code> objects
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    List readResourcesForPrincipalAttr(CmsDbContext dbc, CmsProject project, CmsUUID principalId)
    throws CmsDataAccessException;

    /**
     * Reads all resources that have a value set for the specified property (definition), in the given path.<p>
     * 
     * Both individual and shared properties of a resource are checked.<p>
     *
     * @param dbc the current database context
     * @param projectId the id of the project
     * @param propertyDefinition the id of the property definition
     * @param path the folder to get the resources with the property from
     * 
     * @return a list of all <code>{@link CmsResource}</code> objects 
     *          that have a value set for the specified property.
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    List readResourcesWithProperty(CmsDbContext dbc, int projectId, CmsUUID propertyDefinition, String path)
    throws CmsDataAccessException;

    /**
     * Reads all resources that have a value (containing the specified value) 
     * set for the specified property (definition), in the given path.<p>
     * 
     * Both individual and shared properties of a resource are checked.<p>
     *
     * @param dbc the current database context
     * @param projectId the id of the project
     * @param propertyDefinition the id of the property definition
     * @param path the folder to get the resources with the property from
     * @param value the string to search in the value of the property
     * 
     * @return a list of all <code>{@link CmsResource}</code> objects 
     *          that have a value set for the specified property.
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    List readResourcesWithProperty(
        CmsDbContext dbc,
        int projectId,
        CmsUUID propertyDefinition,
        String path,
        String value) throws CmsDataAccessException;

    /**
     * Reads all resources inside a given project matching the criteria specified by parameter values.<p>
     * 
     * Important: If C_READMODE_EXCLUDE_TREE is true (or C_READMODE_INCLUDE_TREE is false), 
     * the provided parent String must be the UUID of the parent folder, NOT the parent folder path.<p>
     * 
     * @param dbc the current database context
     * @param projectId the project id for matching resources or C_READ_OFFLINE_PROJECTS
     * @param parent the path to the resource used as root of the searched subtree or READ_IGNORE_PARENT (C_READMODE_EXCLUDE_TREE means to read immidiate children only) 
     * @param type the resource type of matching resources or C_READ_IGNORE_TYPES (meaning inverted by C_READMODE_EXCLUDE_TYPE)
     * @param state the state of matching resources or READ_IGNORE_STATE (meaning inverted by C_READMODE_EXCLUDE_STATE)
     * @param startTime the start of the time range for the last modification date of matching resources or READ_IGNORE_TIME 
     * @param endTime the end of the time range for the last modification date of matching resources or READ_IGNORE_TIME
     * @param releasedAfter the start of the time range for the release date of matching resources
     * @param releasedBefore the end of the time range for the release date of matching resources
     * @param expiredAfter the start of the time range for the expire date of matching resources
     * @param expiredBefore the end of the time range for the expire date of matching resources
     * @param mode additional mode flags:
     *  C_READMODE_INCLUDE_TREE 
     *  C_READMODE_EXCLUDE_TREE
     *  C_READMODE_INCLUDE_PROJECT
     *  C_READMODE_EXCLUDE_TYPE
     *  C_READMODE_EXCLUDE_STATE
     * 
     * @return a list of CmsResource objects matching the given criteria
     * @throws CmsDataAccessException if somethong goes wrong
     */
    List readResourceTree(
        CmsDbContext dbc,
        int projectId,
        String parent,
        int type,
        int state,
        long startTime,
        long endTime,
        long releasedAfter,
        long releasedBefore,
        long expiredAfter,
        long expiredBefore,
        int mode) throws CmsDataAccessException;
    
    /**
     * Reads all siblings that point to the resource record of a specified resource.<p>
     * 
     * @param dbc the current database context
     * @param currentProject the current project
     * @param resource the specified resource
     * @param includeDeleted <code>true</code> if deleted siblings should be included in the result list
     * 
     * @return a list of <code>{@link CmsResource}</code>s that 
     *          are siblings to the specified resource, 
     *          including the specified resource itself.
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    List readSiblings(CmsDbContext dbc, CmsProject currentProject, CmsResource resource, boolean includeDeleted)
    throws CmsDataAccessException;

    /**
     * Removes a file physically in the database.<p>
     * 
     * @param dbc the current database context
     * @param currentProject the current project
     * @param resource the resource
     * @param removeFileContent if true, the content record is also removed; if false, only the structure/resource records are removed
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void removeFile(CmsDbContext dbc, CmsProject currentProject, CmsResource resource, boolean removeFileContent)
    throws CmsDataAccessException;

    /**
     * Removes a folder physically in the database.<p>
     * 
     * @param dbc the current database context
     * @param currentProject the current project
     * @param resource the folder
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void removeFolder(CmsDbContext dbc, CmsProject currentProject, CmsResource resource) throws CmsDataAccessException;

    /**
     * Replaces the content and properties of an existing resource.<p>
     * @param dbc the current database context
     * @param newResource the new resource
     * @param newResourceContent the new content
     * @param newResourceType the resource type
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void replaceResource(CmsDbContext dbc, CmsResource newResource, byte[] newResourceContent, int newResourceType)
    throws CmsDataAccessException;

    /**
     * Transfers the attributes of a resource from to the given users.<p>
     * 
     * @param dbc the current database context
     * @param project the current project
     * @param resource the resource to modify
     * @param createdUser the id of the user to be set as the creator of the resource
     * @param lastModifiedUser the id of the user to be set as the las modificator of the resource
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void transferResource(
        CmsDbContext dbc,
        CmsProject project,
        CmsResource resource,
        CmsUUID createdUser,
        CmsUUID lastModifiedUser) throws CmsDataAccessException;

    /**
     * Validates if the specified resource ID in the tables of the specified project {offline|online} exists.<p>
     * 
     * @param dbc the current database context
     * @param projectId the project id
     * @param resourceId the resource id to test for
     * 
     * @return true if a resource with the given id was found, false otherweise
     * @throws CmsDataAccessException if something goes wrong
     */
    boolean validateResourceIdExists(CmsDbContext dbc, int projectId, CmsUUID resourceId) throws CmsDataAccessException;

    /**
     * Validates if the specified structure ID in the tables of the specified project {offline|online} exists.<p>
     * 
     * @param dbc the current database context
     * @param projectId the ID of current project
     * @param structureId the structure id
     * 
     * @return true, if the specified structure ID in the tables of the specified project {offline|online} exists
     * @throws CmsDataAccessException if something goes wrong
     */
    boolean validateStructureIdExists(CmsDbContext dbc, int projectId, CmsUUID structureId)
    throws CmsDataAccessException;

    /**
     * Writes the resource content with the specified content id.<p>
     * 
     * @param dbc the current database context
     * @param project the current project
     * @param resourceId the id of the resource used to identify the content to update
     * @param content the new content of the file
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void writeContent(CmsDbContext dbc, CmsProject project, CmsUUID resourceId, byte[] content)
    throws CmsDataAccessException;

    /**
     * Writes the "last-modified-in-project" ID of a resource.<p>
     * 
     * @param dbc the current database context
     * @param project the resource record is updated with the ID of this project
     * @param projectId the project id to write into the reource
     * @param resource the resource that gets updated
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void writeLastModifiedProjectId(CmsDbContext dbc, CmsProject project, int projectId, CmsResource resource)
    throws CmsDataAccessException;

    /**
     * Writes a property object to the database mapped to a specified resource.<p>
     * 
     * @param dbc the current database context
     * @param project the current project
     * @param resource the resource where the property should be attached to
     * @param property a CmsProperty object containing both the structure and resource value of the property
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void writePropertyObject(CmsDbContext dbc, CmsProject project, CmsResource resource, CmsProperty property)
    throws CmsDataAccessException;

    /**
     * Writes a list of property objects to the database mapped to a specified resource.<p>
     * 
     * @param dbc the current database context
     * @param project the current project
     * @param resource the resource where the property should be attached to
     * @param properties a list of CmsProperty objects
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    void writePropertyObjects(CmsDbContext dbc, CmsProject project, CmsResource resource, List properties)
    throws CmsDataAccessException;

    /**
     * Writes the structure and/or resource record(s) of an existing file.<p>
     * 
     * Common usages of this method are saving the resource information
     * after creating, importing or restoring complete files
     * where all file header attribs are changed. Both the structure and resource 
     * records get written. Thus, using this method affects all siblings of
     * a resource! Use {@link #writeResourceState(CmsDbContext, CmsProject, CmsResource, int)}
     * instead if you just want to update the file state, e.g. of a single sibling.<p>
     * 
     * The file state is set to "changed", unless the current state is "new"
     * or "deleted". The "changed" argument allows to choose whether the structure 
     * or resource state, or none of them, is set to "changed".<p>
     * 
     * The rating of the file state values is as follows:<br>
     * unchanged &lt; changed &lt; new &lt; deleted<p>
     * 
     * Second, the "state" of the resource is the structure state, if the structure state
     * has a higher file state value than the resource state. Otherwise the file state is
     * the resource state.<p>
     * 
     * @param dbc the current database context
     * @param project the current project
     * @param resource the resource to be updated
     * @param changed determines whether the structure or resource state, or none of them, is set to "changed"
     * 
     * @throws CmsDataAccessException if something goes wrong
     * 
     * @see org.opencms.db.CmsDriverManager#UPDATE_RESOURCE_STATE
     * @see org.opencms.db.CmsDriverManager#UPDATE_STRUCTURE_STATE
     * @see org.opencms.db.CmsDriverManager#NOTHING_CHANGED
     * @see #writeResourceState(CmsDbContext, CmsProject, CmsResource, int)
     */
    void writeResource(CmsDbContext dbc, CmsProject project, CmsResource resource, int changed)
    throws CmsDataAccessException;

    /**
     * Writes file state in either the structure or resource record, or both of them.<p>
     * 
     * This method allows to change the resource state to any state by setting the
     * desired state value in the specified CmsResource instance.<p>
     * 
     * This method is frequently used while resources are published to set the file state
     * back to "unchanged".<p>
     * 
     * Only file state attribs. get updated here. Use {@link #writeResource(CmsDbContext, CmsProject, CmsResource, int)}
     * instead to write the complete file header.<p>
     * 
     * Please refer to the javadoc of {@link #writeResource(CmsDbContext, CmsProject, CmsResource, int)} to read
     * how setting resource state values affects the file state.<p>
     * 
     * @param dbc the current database context
     * @param project the current project
     * @param resource the resource to be updated
     * @param changed determines whether the structure or resource state, or none of them, is set to "changed"
     * 
     * @throws CmsDataAccessException if somethong goes wrong
     * 
     * @see org.opencms.db.CmsDriverManager#UPDATE_RESOURCE_STATE
     * @see org.opencms.db.CmsDriverManager#UPDATE_STRUCTURE_STATE
     * @see org.opencms.db.CmsDriverManager#UPDATE_ALL
     */
    void writeResourceState(CmsDbContext dbc, CmsProject project, CmsResource resource, int changed)
    throws CmsDataAccessException;
}
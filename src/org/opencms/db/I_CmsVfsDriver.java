/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.db.urlname.CmsUrlNameMappingEntry;
import org.opencms.db.urlname.CmsUrlNameMappingFilter;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.util.CmsUUID;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Definitions of all required VFS driver methods.<p>
 *
 * @since 6.0.0
 */
public interface I_CmsVfsDriver {

    /** The type ID to identify user driver implementations. */
    int DRIVER_TYPE_ID = 3;

    /** The internal request attribute to indicate that the permissions have to be checked. */
    String REQ_ATTR_CHECK_PERMISSIONS = "CHECK_PERMISSIONS";

    /** The internal request attribute to indicate that resource organizational units have to be retrieved. */
    String REQ_ATTR_RESOURCE_OUS = "RETRIEVE_RESOURCE_OUS";

    /**
     * Adds a new URL name mapping entry.<p>
     *
     * @param dbc the current database context
     * @param online if true, writes to the online tables, else to the offline tables
     * @param entry the entry to add
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void addUrlNameMappingEntry(CmsDbContext dbc, boolean online, CmsUrlNameMappingEntry entry)
    throws CmsDataAccessException;

    /**
     * Counts the number of siblings of a resource.<p>
     *
     * @param dbc the current database context
     * @param projectId the current project id
     * @param resourceId the resource id to count the number of siblings from
     *
     * @return number of siblings
     * @throws CmsDataAccessException if something goes wrong
     */
    int countSiblings(CmsDbContext dbc, CmsUUID projectId, CmsUUID resourceId) throws CmsDataAccessException;

    /**
     * Creates a content entry for the resource identified by the specified resource id.<p>
     *
     * @param dbc the current database context
     * @param projectId the id of the current project
     * @param resourceId the resource id of the resource to create the content for
     * @param content the content to write
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void createContent(CmsDbContext dbc, CmsUUID projectId, CmsUUID resourceId, byte[] content)
    throws CmsDataAccessException;

    /**
     * Creates a {@link CmsFile} instance from a JDBC ResultSet.<p>
     *
     * @param res the JDBC ResultSet
     * @param projectId the project id
     *
     * @return the created file
     * @throws SQLException in case the result set does not include a requested table attribute
     */
    CmsFile createFile(ResultSet res, CmsUUID projectId) throws SQLException;

    /**
     * Creates a {@link CmsFile} instance from a JDBC ResultSet.<p>
     *
     * @param res the JDBC ResultSet
     * @param projectId the project id
     * @param hasFileContentInResultSet flag to include the file content
     *
     * @return the created file
     * @throws SQLException in case the result set does not include a requested table attribute
     */
    CmsFile createFile(ResultSet res, CmsUUID projectId, boolean hasFileContentInResultSet) throws SQLException;

    /**
     * Creates a {@link CmsFolder} instance from a JDBC ResultSet.<p>
     *
     * @param res the JDBC ResultSet
     * @param projectId the ID of the current project
     * @param hasProjectIdInResultSet true if the SQL select query includes the PROJECT_ID table attribute
     *
     * @return the created folder
     * @throws SQLException in case the result set does not include a requested table attribute
     */
    CmsFolder createFolder(ResultSet res, CmsUUID projectId, boolean hasProjectIdInResultSet) throws SQLException;

    /**
     * Creates a new content in the offline project.<p>
     *
     * @param dbc the current database context
     * @param resourceId the resource id of the content to write
     * @param contents the content to publish
     * @param publishTag the publish tag
     * @param keepOnline if the content is online or has to be put in the history
     * @param needToUpdateContent if the content blob has to be updated
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void createOnlineContent(
        CmsDbContext dbc,
        CmsUUID resourceId,
        byte[] contents,
        int publishTag,
        boolean keepOnline,
        boolean needToUpdateContent) throws CmsDataAccessException;

    /**
     * Creates a new property definition in the database.<p>
     *
     * @param dbc the current database context
     * @param projectId the project in which the property definition is created
     * @param name the name of the property definition
     * @param type the type of the property definition
     *
     * @return the new property definition
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsPropertyDefinition createPropertyDefinition(
        CmsDbContext dbc,
        CmsUUID projectId,
        String name,
        CmsPropertyDefinition.CmsPropertyType type) throws CmsDataAccessException;

    /**
     * Creates a new {@link CmsRelation} object in the database.<p>
     *
     * @param dbc the current database context
     * @param projectId the id of the project to execute the query in
     * @param relation the relation to create
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void createRelation(CmsDbContext dbc, CmsUUID projectId, CmsRelation relation) throws CmsDataAccessException;

    /**
     * Creates a new resource from a given {@link CmsResource} object.<p>
     *
     * This method works for both files and folders. Existing resources get overwritten.<p>
     *
     * @param dbc the current database context
     * @param projectId the id of the current project
     * @param resource the resource to be created
     * @param content the file content, or null in case of a folder
     * @return the created Cms resource
     *
     * @throws CmsDataAccessException if something goes wrong
     *
     * @see org.opencms.file.types.I_CmsResourceType#createResource(org.opencms.file.CmsObject, CmsSecurityManager, String, byte[], List)
     * @see org.opencms.file.types.I_CmsResourceType#importResource(org.opencms.file.CmsObject, CmsSecurityManager, String, CmsResource, byte[], List)
     * @see org.opencms.file.CmsObject#createResource(String, int, byte[], List)
     * @see org.opencms.file.CmsObject#importResource(String, CmsResource, byte[], List)
     */
    CmsResource createResource(CmsDbContext dbc, CmsUUID projectId, CmsResource resource, byte[] content)
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
    CmsResource createResource(ResultSet res, CmsUUID projectId) throws SQLException;

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
     * Deletes the aliases matching a given filter.<p>
     *
     * @param dbc the database context
     * @param project the current project
     * @param filter the alias filter
     * @throws CmsDataAccessException if something goes wrong
     */
    void deleteAliases(CmsDbContext dbc, CmsProject project, CmsAliasFilter filter) throws CmsDataAccessException;

    /**
     * Deletes a property definition.<p>
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
     *
     * @see org.opencms.file.CmsProperty#DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES
     * @see org.opencms.file.CmsProperty#DELETE_OPTION_DELETE_STRUCTURE_VALUES
     * @see org.opencms.file.CmsProperty#DELETE_OPTION_DELETE_RESOURCE_VALUES
     */
    void deletePropertyObjects(CmsDbContext dbc, CmsUUID projectId, CmsResource resource, int deleteOption)
    throws CmsDataAccessException;

    /**
     * Deletes all relations with the given filter for the given resource.<p>
     *
     * @param dbc the current database context
     * @param projectId the id of the project to execute the query in
     * @param resource the base resource. May be <code>null</code> for all
     * @param filter the filter to restrict the relations to remove
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void deleteRelations(CmsDbContext dbc, CmsUUID projectId, CmsResource resource, CmsRelationFilter filter)
    throws CmsDataAccessException;

    /**
     * Deletes rewrite aliases matching a given filter.<p>
     *
     * @param dbc the current database context
     * @param filter the filter describing which rewrite aliases to delete
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void deleteRewriteAliases(CmsDbContext dbc, CmsRewriteAliasFilter filter) throws CmsDataAccessException;

    /**
     * Deletes the URL name mapping entries which match a given filter.<p>
     *
     * @param dbc the current database context
     * @param online if true, changes the online URL name mappings, else the offline URL name mappings
     * @param filter the URL name mapping entries to delete
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void deleteUrlNameMappingEntries(CmsDbContext dbc, boolean online, CmsUrlNameMappingFilter filter)
    throws CmsDataAccessException;

    /**
     * Destroys this driver.<p>
     *
     * @throws Throwable if something goes wrong
     */
    void destroy() throws Throwable;

    /**
     * Returns all organizational units for the given resource.<p>
     *
     * @param dbc the database context
     * @param projectId the id of the project
     * @param resource the resource
     *
     * @return a list of {@link org.opencms.security.CmsOrganizationalUnit} objects
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    List<CmsOrganizationalUnit> getResourceOus(CmsDbContext dbc, CmsUUID projectId, CmsResource resource)
    throws CmsDataAccessException;

    /**
     * Returns the SqlManager of this driver.<p>
     *
     * @return the SqlManager of this driver
     */
    CmsSqlManager getSqlManager();

    /**
     * Gets the current value of a counter, creates it if it doesn't already exist, and increments it.<p>
     *
     * @param dbc the database context
     * @param name the name of the counter
     *
     * @return the counter value before incrementing
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    int incrementCounter(CmsDbContext dbc, String name) throws CmsDataAccessException;

    /**
     * Initializes the SQL manager for this driver.<p>
     *
     * To obtain JDBC connections from different pools, further
     * {online|offline|history} pool Urls have to be specified.<p>
     *
     * @param classname the class name of the SQL manager
     *
     * @return the SQL manager for this driver
     */
    CmsSqlManager initSqlManager(String classname);

    /**
     * Adds an alias to the database.<p>
     *
     * @param dbc the current database context
     * @param project the current project
     * @param alias the alias to write
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void insertAlias(CmsDbContext dbc, CmsProject project, CmsAlias alias) throws CmsDataAccessException;

    /**
     * Adds a list of rewrite aliases.<p>
     *
     * When adding a rewrite alias, make sure that no alias with the same id is present in the database.<p>
     *
     * @param dbc the current database context
     * @param rewriteAliases the rewrite aliases to save
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void insertRewriteAliases(CmsDbContext dbc, Collection<CmsRewriteAlias> rewriteAliases)
    throws CmsDataAccessException;

    /**
     * Moves the given resource to the specified destination path.<p>
     *
     * @param dbc the current database context
     * @param projectId the Id of the project
     * @param source the resource to move
     * @param destinationPath the root path of the destination resource
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void moveResource(CmsDbContext dbc, CmsUUID projectId, CmsResource source, String destinationPath)
    throws CmsDataAccessException;

    /**
     * Publishes the structure and resource records of an
     * offline resource into it's online counterpart.<p>
     *
     * @param dbc the current database context
     * @param onlineProject the online project
     * @param onlineResource the online resource
     * @param offlineResource the offline resource
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void publishResource(
        CmsDbContext dbc,
        CmsProject onlineProject,
        CmsResource onlineResource,
        CmsResource offlineResource) throws CmsDataAccessException;

    /**
     * Copies the version number from the offline resource to the online resource,
     * this has to be done during publishing, direct after copying the resource itself.<p>
     *
     * @param dbc the current database context
     * @param resource the resource that has been publish
     * @param firstSibling if this is the first sibling to be publish
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void publishVersions(CmsDbContext dbc, CmsResource resource, boolean firstSibling) throws CmsDataAccessException;

    /**
     * Reads the aliases matching a given filter.<p>
     *
     * @param dbc the database context
     * @param project the current project
     * @param filter the alias filter
     * @return the list of aliases which were read
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    List<CmsAlias> readAliases(CmsDbContext dbc, CmsProject project, CmsAliasFilter filter)
    throws CmsDataAccessException;

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
    List<CmsResource> readChildResources(
        CmsDbContext dbc,
        CmsProject currentProject,
        CmsResource resource,
        boolean getFolders,
        boolean getFiles) throws CmsDataAccessException;

    /**
     * Reads the content of a file specified by it's resource ID.<p>
     *
     * @param dbc the current database context
     * @param projectId the ID of the current project
     * @param resourceId the id of the resource
     *
     * @return the file content
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    byte[] readContent(CmsDbContext dbc, CmsUUID projectId, CmsUUID resourceId) throws CmsDataAccessException;

    /**
     * Reads a folder specified by it's structure ID.<p>
     *
     * @param dbc the current database context
     * @param projectId the project in which the resource will be used
     * @param folderId the structure id of the folder to be read
     *
     * @return the read folder
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsFolder readFolder(CmsDbContext dbc, CmsUUID projectId, CmsUUID folderId) throws CmsDataAccessException;

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
    CmsFolder readFolder(CmsDbContext dbc, CmsUUID projectId, String foldername) throws CmsDataAccessException;

    /**
     * Reads the parent folder of a resource specified by it's structure ID.<p>
     *
     * The parent folder for the root '/' is defined as <code>null</code>.<p>
     *
     * @param dbc the current database context
     * @param projectId the project in which the resource will be used
     * @param structureId the id of the resource to read the parent folder for
     *
     * @return the read folder, or <code>null</code>
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsFolder readParentFolder(CmsDbContext dbc, CmsUUID projectId, CmsUUID structureId) throws CmsDataAccessException;

    /**
     * Reads a property definition for the specified resource type.<p>
     *
     * If no property definition with the given name is found,
     * <code>null</code> is returned.<p>
     *
     * @param dbc the current database context
     * @param name the name of the property definition to read
     * @param projectId the id of the project
     *
     * @return the property definition that was read
     *
     * @throws CmsDataAccessException a CmsDbEntryNotFoundException is thrown if the property definition does not exist
     */
    CmsPropertyDefinition readPropertyDefinition(CmsDbContext dbc, String name, CmsUUID projectId)
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
    List<CmsPropertyDefinition> readPropertyDefinitions(CmsDbContext dbc, CmsUUID projectId)
    throws CmsDataAccessException;

    /**
     * Reads a property object from the database specified by it's key name mapped to a resource.<p>
     *
     * The implementation must return {@link CmsProperty#getNullProperty()} if the property is not found.<p>
     *
     * TODO: change project parameter to project id
     *
     * @param dbc the current database context
     * @param key the key of the property
     * @param project the current project
     * @param resource the resource where the property is attached to
     *
     * @return a CmsProperty object containing both the structure and resource value of the property
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    CmsProperty readPropertyObject(CmsDbContext dbc, String key, CmsProject project, CmsResource resource)
    throws CmsDataAccessException;

    /**
     * Reads all property objects mapped to a specified resource from the database.<p>
     *
     * The implementation must return an empty list if no properties are found at all.<p>
     *
     * TODO: change project parameter to project id
     *
     * @param dbc the current database context
     * @param project the current project
     * @param resource the resource where the property is attached to
     *
     * @return a list with CmsProperty objects containing both the structure and resource value of the property
     * @throws CmsDataAccessException if something goes wrong
     */
    List<CmsProperty> readPropertyObjects(CmsDbContext dbc, CmsProject project, CmsResource resource)
    throws CmsDataAccessException;

    /**
     * Reads all relations with the given filter for the given resource.<p>
     *
     * @param dbc the current database context
     * @param projectId the id of the project to execute the query in
     * @param resource the resource to read the relations for, may be <code>null</code> for all
     * @param filter the filter to restrict the relations to retrieve
     *
     * @return the read relations
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    List<CmsRelation> readRelations(CmsDbContext dbc, CmsUUID projectId, CmsResource resource, CmsRelationFilter filter)
    throws CmsDataAccessException;

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
    CmsResource readResource(CmsDbContext dbc, CmsUUID projectId, CmsUUID structureId, boolean includeDeleted)
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
    CmsResource readResource(CmsDbContext dbc, CmsUUID projectId, String filename, boolean includeDeleted)
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
     * @throws CmsDataAccessException if something goes wrong
     */
    List<CmsResource> readResources(CmsDbContext dbc, CmsUUID currentProject, CmsResourceState state, int mode)
    throws CmsDataAccessException;

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
    List<CmsResource> readResourcesForPrincipalACE(CmsDbContext dbc, CmsProject project, CmsUUID principalId)
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
    List<CmsResource> readResourcesForPrincipalAttr(CmsDbContext dbc, CmsProject project, CmsUUID principalId)
    throws CmsDataAccessException;

    /**
     * Reads all resources that have a value (containing the specified value)
     * set for the specified property (definition), in the given path.<p>
     *
     * Both individual and shared properties of a resource are checked.<p>
     *
     * If the <code>value</code> parameter is <code>null</code>, all resources having the
     * given property set are returned.<p>
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
    List<CmsResource> readResourcesWithProperty(
        CmsDbContext dbc,
        CmsUUID projectId,
        CmsUUID propertyDefinition,
        String path,
        String value) throws CmsDataAccessException;

    /**
     * Reads all resources inside a given project matching the criteria specified by parameter values.<p>
     *
     * Important: If {@link CmsDriverManager#READMODE_EXCLUDE_TREE} is true (or {@link CmsDriverManager#READMODE_INCLUDE_TREE} is false),
     * the provided parent String must be the UUID of the parent folder, NOT the parent folder path.<p>
     *
     * @param dbc the current database context
     * @param projectId the project id for matching resources
     * @param parent the path to the resource used as root of the searched subtree or {@link CmsDriverManager#READ_IGNORE_PARENT},
     *               {@link CmsDriverManager#READMODE_EXCLUDE_TREE} means to read immediate children only
     * @param type the resource type of matching resources or {@link CmsDriverManager#READ_IGNORE_TYPE} (meaning inverted by {@link CmsDriverManager#READMODE_EXCLUDE_TYPE}
     * @param state the state of matching resources (meaning inverted by {@link CmsDriverManager#READMODE_EXCLUDE_STATE} or <code>null</code> to ignore
     * @param startTime the start of the time range for the last modification date of matching resources or READ_IGNORE_TIME
     * @param endTime the end of the time range for the last modification date of matching resources or READ_IGNORE_TIME
     * @param releasedAfter the start of the time range for the release date of matching resources
     * @param releasedBefore the end of the time range for the release date of matching resources
     * @param expiredAfter the start of the time range for the expire date of matching resources
     * @param expiredBefore the end of the time range for the expire date of matching resources
     * @param mode additional mode flags:
     * <ul>
     *  <li>{@link CmsDriverManager#READMODE_INCLUDE_TREE}
     *  <li>{@link CmsDriverManager#READMODE_EXCLUDE_TREE}
     *  <li>{@link CmsDriverManager#READMODE_INCLUDE_PROJECT}
     *  <li>{@link CmsDriverManager#READMODE_EXCLUDE_TYPE}
     *  <li>{@link CmsDriverManager#READMODE_EXCLUDE_STATE}
     * </ul>
     *
     * @return a list of CmsResource objects matching the given criteria
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    List<CmsResource> readResourceTree(
        CmsDbContext dbc,
        CmsUUID projectId,
        String parent,
        int type,
        CmsResourceState state,
        long startTime,
        long endTime,
        long releasedAfter,
        long releasedBefore,
        long expiredAfter,
        long expiredBefore,
        int mode) throws CmsDataAccessException;

    /**
     * Reads the rewrite aliases matching a given filter.<p>
     *
     * @param dbc the current database context
     * @param filter the filter describing which rewrite aliases should be returned
     *
     * @return the rewrite aliases which were found
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    List<CmsRewriteAlias> readRewriteAliases(CmsDbContext dbc, CmsRewriteAliasFilter filter)
    throws CmsDataAccessException;

    /**
     * Reads all siblings that point to the resource record of a specified resource.<p>
     *
     * @param dbc the current database context
     * @param projectId the id of the current project
     * @param resource the specified resource
     * @param includeDeleted <code>true</code> if deleted siblings should be included in the result list
     *
     * @return a list of <code>{@link CmsResource}</code>s that
     *          are siblings to the specified resource,
     *          including the specified resource itself.
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    List<CmsResource> readSiblings(CmsDbContext dbc, CmsUUID projectId, CmsResource resource, boolean includeDeleted)
    throws CmsDataAccessException;

    /**
     * Reads the URL name mapping entries which match a given filter.<p>
     *
     * @param dbc the database context
     * @param online if true, reads from the online mapping, else from the offline mapping
     * @param filter the filter which the entries to be read should match
     *
     * @return the mapping entries which match the given filter
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    List<CmsUrlNameMappingEntry> readUrlNameMappingEntries(
        CmsDbContext dbc,
        boolean online,
        CmsUrlNameMappingFilter filter) throws CmsDataAccessException;

    /**
     * Reads a resource version numbers.<p>
     *
     * @param dbc the current database context
     * @param projectId the project to read the versions from
     * @param resourceId the resource id of the resource to read the versions from
     * @param structureId the structure id of the resource to read the versions from
     *
     * @return a map with two entries with keys "structure" and "resource" for the
     *         structure and resource version number respectively, the values are {@link Integer}
     *         objects and may be <code>-1</code> if an entry could be found
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    Map<String, Integer> readVersions(CmsDbContext dbc, CmsUUID projectId, CmsUUID resourceId, CmsUUID structureId)
    throws CmsDataAccessException;

    /**
     * Removes a file physically in the database.<p>
     *
     * @param dbc the current database context
     * @param projectId the id of the current project
     * @param resource the resource
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void removeFile(CmsDbContext dbc, CmsUUID projectId, CmsResource resource) throws CmsDataAccessException;

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
     * Sets the driver manager for this driver if possible.<p>
     *
     * @param driverManager the new driver manager
     */
    void setDriverManager(CmsDriverManager driverManager);

    /**
     * Sets the SQL manager for this driver if possible.<p>
     *
     * @param sqlManager the new SQL manager
     */
    void setSqlManager(CmsSqlManager sqlManager);

    /**
     * Transfers the attributes of a resource from to the given users.<p>
     *
     * @param dbc the current database context
     * @param project the current project
     * @param resource the resource to modify
     * @param createdUser the id of the user to be set as the creator of the resource
     * @param lastModifiedUser the id of the user to be set as the last modificator of the resource
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
     * Updates the relations on the online project copying the relations from the offline project.<p>
     * TODO: add offlineProject parameter
     *
     * @param dbc the current database context
     * @param onlineProject the online project
     * @param offlineResource the resource to update the relations for
     *
     * @throws CmsDataAccessException is something goes wrong
     */
    void updateRelations(CmsDbContext dbc, CmsProject onlineProject, CmsResource offlineResource)
    throws CmsDataAccessException;

    /**
     * Validates if the specified resource ID in the tables of the specified project {offline|online} exists.<p>
     *
     * @param dbc the current database context
     * @param projectId the project id
     * @param resourceId the resource id to test for
     *
     * @return true if a resource with the given id was found, false otherwise
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    boolean validateResourceIdExists(CmsDbContext dbc, CmsUUID projectId, CmsUUID resourceId)
    throws CmsDataAccessException;

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
    boolean validateStructureIdExists(CmsDbContext dbc, CmsUUID projectId, CmsUUID structureId)
    throws CmsDataAccessException;

    /**
     * Writes the resource content with the specified resource id.<p>
     *
     * @param dbc the current database context
     * @param resourceId the id of the resource used to identify the content to update
     * @param content the new content of the file
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void writeContent(CmsDbContext dbc, CmsUUID resourceId, byte[] content) throws CmsDataAccessException;

    /**
     * Writes the "last-modified-in-project" ID of a resource.<p>
     *
     * @param dbc the current database context
     * @param project the resource record is updated with the ID of this project
     * @param projectId the project id to write into the resource
     * @param resource the resource that gets updated
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    void writeLastModifiedProjectId(CmsDbContext dbc, CmsProject project, CmsUUID projectId, CmsResource resource)
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
    void writePropertyObjects(CmsDbContext dbc, CmsProject project, CmsResource resource, List<CmsProperty> properties)
    throws CmsDataAccessException;

    /**
     * Writes the structure and/or resource record(s) of an existing file.<p>
     *
     * Common usages of this method are saving the resource information
     * after creating, importing or restoring complete files
     * where all file header attributes are changed. Both the structure and resource
     * records get written. Thus, using this method affects all siblings of
     * a resource! Use {@link #writeResourceState(CmsDbContext, CmsProject, CmsResource, int, boolean)}
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
     * @param projectId the id of the current project
     * @param resource the resource to be updated
     * @param changed determines whether the structure or resource state, or none of them, is set to "changed"
     *
     * @throws CmsDataAccessException if something goes wrong
     *
     * @see org.opencms.db.CmsDriverManager#UPDATE_RESOURCE_STATE
     * @see org.opencms.db.CmsDriverManager#UPDATE_STRUCTURE_STATE
     * @see org.opencms.db.CmsDriverManager#NOTHING_CHANGED
     * @see #writeResourceState(CmsDbContext, CmsProject, CmsResource, int, boolean)
     */
    void writeResource(CmsDbContext dbc, CmsUUID projectId, CmsResource resource, int changed)
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
     * Only file state attributes. get updated here. Use {@link #writeResource(CmsDbContext, CmsUUID, CmsResource, int)}
     * instead to write the complete file header.<p>
     *
     * Please refer to the javadoc of {@link #writeResource(CmsDbContext, CmsUUID, CmsResource, int)} to read
     * how setting resource state values affects the file state.<p>
     *
     * @param dbc the current database context
     * @param project the current project
     * @param resource the resource to be updated
     * @param changed determines whether the structure or resource state, or none of them, is set to "changed"
     * @param isPublishing if this method is called during publishing to version numbers are updated
     *
     * @throws CmsDataAccessException if something goes wrong
     *
     * @see org.opencms.db.CmsDriverManager#UPDATE_RESOURCE_STATE
     * @see org.opencms.db.CmsDriverManager#UPDATE_STRUCTURE_STATE
     * @see org.opencms.db.CmsDriverManager#UPDATE_ALL
     */
    void writeResourceState(
        CmsDbContext dbc,
        CmsProject project,
        CmsResource resource,
        int changed,
        boolean isPublishing) throws CmsDataAccessException;

}
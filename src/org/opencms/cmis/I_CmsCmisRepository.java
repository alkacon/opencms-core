/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.cmis;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.repository.CmsRepositoryFilter;
import org.opencms.repository.I_CmsRepository;

import java.math.BigInteger;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.spi.Holder;

/**
 * Base interface for OpenCms CMIS repositories.<p>
 */
public interface I_CmsCmisRepository extends I_CmsRepository {

    /**
     * Adds an object to a folder (multifiling). <p>
     *
     * @param context the call context
     * @param objectId the object id
     * @param folderId the folder id
     * @param allVersions flag to include all versions
     */
    void addObjectToFolder(CmsCmisCallContext context, String objectId, String folderId, boolean allVersions);

    /**
     * Applies ACL to an object.<p>
     *
     * @param context the call context
     * @param objectId the object id
     * @param addAces the ACEs to add
     * @param removeAces the ACEs to remove
     * @param aclPropagation the ACL propagation
     *
     * @return the new ACL
     */
    Acl applyAcl(
        CmsCmisCallContext context,
        String objectId,
        Acl addAces,
        Acl removeAces,
        AclPropagation aclPropagation);

    /**
     * Changes the ACL for an object.<p>
     *
     * @param context the call context
     * @param objectId the object id
     * @param aces the access control entries
     * @param aclPropagation the propagation mode
     *
     * @return the new ACL
     */
    Acl applyAcl(CmsCmisCallContext context, String objectId, Acl aces, AclPropagation aclPropagation);

    /**
     * Applies a policy to an object.<p>
     *
     * @param context the call context
     * @param policyId the policy id
     * @param objectId the object id
     */
    void applyPolicy(CmsCmisCallContext context, String policyId, String objectId);

    /**
     * Cancels a checkout.<p>
     *
     * @param context the call context
     * @param objectId the object id
     */
    void cancelCheckOut(CmsCmisCallContext context, String objectId);

    /**
     * Checks in a document.<p>
     *
     * @param context the call context
     * @param objectId the object id
     * @param major the major version flag
     * @param properties the properties
     * @param contentStream the content stream
     * @param checkinComment the check-in comment
     * @param policies the policies
     * @param addAces the ACEs to add
     * @param removeAces the ACEs to remove
     */
    void checkIn(
        CmsCmisCallContext context,
        Holder<String> objectId,
        boolean major,
        Properties properties,
        ContentStream contentStream,
        String checkinComment,
        List<String> policies,
        Acl addAces,
        Acl removeAces);

    /**
     * Checks out an object.<p>
     *
     * @param context the call context
     * @param objectId the object id
     * @param contentCopied indicator whether the content was copied
     */
    void checkOut(CmsCmisCallContext context, Holder<String> objectId, Holder<Boolean> contentCopied);

    /**
     * Creates a new document.<p>
     *
     * @param context the call context
     * @param propertiesObj the properties
     * @param folderId the parent folder id
     * @param contentStream the content stream
     * @param versioningState the versioning state
     * @param policies the policies
     * @param addAces the access control entries
     * @param removeAces the access control entries to remove
     *
     * @return the object id of the new document
     */
    String createDocument(
        CmsCmisCallContext context,
        Properties propertiesObj,
        String folderId,
        ContentStream contentStream,
        VersioningState versioningState,
        List<String> policies,
        Acl addAces,
        Acl removeAces);

    /**
     * Copies a document.<p>
     *
     * @param context the call context
     * @param sourceId the source object id
     * @param propertiesObj the properties
     * @param folderId the target folder id
     * @param versioningState the versioning state
     * @param policies the policies
     * @param addAces the ACEs to add
     * @param removeAces the ACES to remove
     *
     * @return the object id of the new document
     */
    String createDocumentFromSource(
        CmsCmisCallContext context,
        String sourceId,
        Properties propertiesObj,
        String folderId,
        VersioningState versioningState,
        List<String> policies,
        Acl addAces,
        Acl removeAces);

    /**
     * Creates a new folder.<p>
     *
     * @param context the call context
     * @param propertiesObj the properties
     * @param folderId the parent folder id
     * @param policies the policies
     * @param addAces the ACEs to add
     * @param removeAces the ACEs to remove
     *
     * @return the object id of the created folder
     */
    String createFolder(
        CmsCmisCallContext context,
        Properties propertiesObj,
        String folderId,
        List<String> policies,
        Acl addAces,
        Acl removeAces);

    /**
     * Creates a policy.<p>
     *
     * @param context the call context
     * @param properties the properties
     * @param folderId the folder id
     * @param policies the policies
     * @param addAces the ACEs to add
     * @param removeAces the ACEs to remove
     *
     * @return the new object id
     */
    String createPolicy(
        CmsCmisCallContext context,
        Properties properties,
        String folderId,
        List<String> policies,
        Acl addAces,
        Acl removeAces);

    /**
     * Creates a relationship.<p>
     *
     * @param context the call context
     * @param properties the properties
     * @param policies the policies
     * @param addAces the ACEs to add
     * @param removeAces the ACEs to remove
     *
     * @return the new relationship id
     */
    String createRelationship(
        CmsCmisCallContext context,
        Properties properties,
        List<String> policies,
        Acl addAces,
        Acl removeAces);

    /**
     * Deletes the content stream of an object.<p>
     *
     * @param context the call context
     * @param objectId the object id
     * @param changeToken the change token
     */
    void deleteContentStream(CmsCmisCallContext context, Holder<String> objectId, Holder<String> changeToken);

    /**
     * Deletes a CMIS object.<p>
     *
     * @param context the call context
     * @param objectId the id of the object to delete
     * @param allVersions flag to delete all version
     */
    void deleteObject(CmsCmisCallContext context, String objectId, boolean allVersions);

    /**
     * Deletes a whole file tree.<p>
     *
     * @param context the call context
     * @param folderId the folder id
     * @param allVersions flag to include all versions
     * @param unfileObjects flag to unfile objects
     * @param continueOnFailure flag to continue on failure
     *
     * @return data containing the objects which weren'T deleted successfully
     */
    FailedToDeleteData deleteTree(
        CmsCmisCallContext context,
        String folderId,
        boolean allVersions,
        UnfileObject unfileObjects,
        boolean continueOnFailure);

    /**
     * Gets the ACL for an object.<p>
     *
     * @param context the call context
     * @param objectId the object id
     * @param onlyBasicPermissions flag to only get basic permissions
     *
     * @return the ACL for the object
     */
    Acl getAcl(CmsCmisCallContext context, String objectId, boolean onlyBasicPermissions);

    /**
     * Gets the allowable actions for an object.<p>
     *
     * @param context the call context
     * @param objectId the object id
     * @return the allowable actions
     */
    AllowableActions getAllowableActions(CmsCmisCallContext context, String objectId);

    /**
     * Gets all versions of an object.<p>
     *
     * @param context the call context
     * @param objectId the object id
     * @param versionSeriesId the version series id
     * @param filter the property filter string
     * @param includeAllowableActions the flag to include allowable actions
     *
     * @return the list of versions
     */
    List<ObjectData> getAllVersions(
        CmsCmisCallContext context,
        String objectId,
        String versionSeriesId,
        String filter,
        boolean includeAllowableActions);

    /**
     * Gets the policies for an object.<p>
     *
     * @param context the call context
     * @param objectId the object id
     * @param filter the property filter
     *
     * @return the policies for the object
     */
    List<ObjectData> getAppliedPolicies(CmsCmisCallContext context, String objectId, String filter);

    /**
     * Corresponds to CMIS getCheckedOutDocs service method.<p>
     *
     * @param context
     * @param folderId
     * @param filter
     * @param orderBy
     * @param includeAllowableActions
     * @param includeRelationships
     * @param renditionFilter
     * @param maxItems
     * @param skipCount
     *
     * @return a list of CMIS objects
     */
    ObjectList getCheckedOutDocs(
        CmsCmisCallContext context,
        String folderId,
        String filter,
        String orderBy,
        boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        BigInteger maxItems,
        BigInteger skipCount);

    /**
     * Gets the children of a folder.<p>
     *
     * @param context the call context
     * @param folderId the parent folder id
     * @param filter the property filter
     * @param orderBy the ordering clause
     * @param includeAllowableActions flag to include allowable actions
     * @param includeRelationships flag to include relations
     * @param renditionFilter the rendition filter string
     * @param includePathSegment flag to include the path segment
     * @param maxItems the maximum number of items
     * @param skipCount the index from which to start
     *
     * @return the object information
     */
    ObjectInFolderList getChildren(
        CmsCmisCallContext context,
        String folderId,
        String filter,
        String orderBy,
        boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        boolean includePathSegment,
        BigInteger maxItems,
        BigInteger skipCount);

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    CmsParameterConfiguration getConfiguration();

    /**
     * Gets content changes from the repository.<p>
     *
     * @param context the call context
     * @param changeLogToken the change log token
     * @param includeProperties flag to include properties
     * @param filter filter string for properties
     * @param includePolicyIds flag to include policy ids
     * @param includeAcl flag to include ACLs
     * @param maxItems maximum number of items to return
     *
     * @return the list of content changes
     */
    ObjectList getContentChanges(
        CmsCmisCallContext context,
        Holder<String> changeLogToken,
        boolean includeProperties,
        String filter,
        boolean includePolicyIds,
        boolean includeAcl,
        BigInteger maxItems);

    /**
     * Gets the content stream for a CMIS object.<p>
     *
     * @param context the call context
     * @param objectId the object id
     * @param streamId the rendition stream id
     * @param offset
     * @param length
     *
     * @return the content stream
     */
    ContentStream getContentStream(
        CmsCmisCallContext context,
        String objectId,
        String streamId,
        BigInteger offset,
        BigInteger length);

    /**
     *
     * @param context the call context
     * @param folderId the folder id
     * @param depth the maximum depth
     * @param filter the property filter
     * @param includeAllowableActions flag to include allowable actions
     * @param includePathSegment flag to include path segments
     * @param foldersOnly flag to ignore documents and only return folders
     *
     * @return the list of descendants
     */
    List<ObjectInFolderContainer> getDescendants(
        CmsCmisCallContext context,
        String folderId,
        BigInteger depth,
        String filter,
        boolean includeAllowableActions,
        boolean includePathSegment,
        boolean foldersOnly);

    /**
     * Gets the description of the repository.<p>
     *
     * @return the repository description
     */
    String getDescription();

    /**
     * @see org.opencms.repository.I_CmsRepository#getFilter()
     */
    CmsRepositoryFilter getFilter();

    /**
     * Corresponds to CMIS getFolderParent service method.<p>
     *
     * @param context the call context
     * @param folderId the folder id
     * @param filter the property filter
     *
     * @return the parent object data
     */
    ObjectData getFolderParent(CmsCmisCallContext context, String folderId, String filter);

    /**
     * Gets the repository id.<p>
     *
     * @return the repository id
     */
    String getId();

    /**
     * Gets the name of the repository.<p>
     *
     * @return the name of the repository
     */
    String getName();

    /**
     * Gets the data for a CMIS object.<p>
     *
     * @param context the CMIS call context
     * @param objectId the id of the object
     * @param filter the property filter
     * @param includeAllowableActions flag to include allowable actions
     * @param includeRelationships flag to include relationships
     * @param renditionFilter the rendition filter string
     * @param includePolicyIds flag to include policy ids
     * @param includeAcl flag to include ACLs
     *
     * @return the CMIS object data
     */
    ObjectData getObject(
        CmsCmisCallContext context,
        String objectId,
        String filter,
        boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        boolean includePolicyIds,
        boolean includeAcl);

    /**
     * Reads a CMIS object by path.<p>
     *
     * @param context the call context
     * @param path the repository path
     * @param filter the property filter string
     * @param includeAllowableActions flag to include allowable actions
     * @param includeRelationships flag to include relationships
     * @param renditionFilter the rendition filter string
     * @param includePolicyIds flag to include policy ids
     * @param includeAcl flag to include ACLs
     *
     * @return the object data
     */
    ObjectData getObjectByPath(
        CmsCmisCallContext context,
        String path,
        String filter,
        boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        boolean includePolicyIds,
        boolean includeAcl);

    /**
     * Gets the object of the latest version.<p>
     *
     * @param context the call context
     * @param objectId the object id
     * @param versionSeriesId the version series id
     * @param major flag to get the latest major version
     * @param filter the property filter
     * @param includeAllowableActions flag to include allowable actions
     * @param includeRelationships flag to include relationships
     * @param renditionFilter filter string for renditions
     * @param includePolicyIds flag to include policies
     * @param includeAcl flag to include ACLs
     *
     * @return the data for the latest version
     */
    ObjectData getObjectOfLatestVersion(
        CmsCmisCallContext context,
        String objectId,
        String versionSeriesId,
        boolean major,
        String filter,
        boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        boolean includePolicyIds,
        boolean includeAcl);

    /**
     * Gets the parents of an object.<p>
     *
     * @param context the call context
     * @param objectId the object id
     * @param filter
     * @param includeAllowableActions
     * @param includeRelativePathSegment
     *
     * @return the data for the object parents
     */
    List<ObjectParentData> getObjectParents(
        CmsCmisCallContext context,
        String objectId,
        String filter,
        boolean includeAllowableActions,
        boolean includeRelativePathSegment);

    /**
     * Gets the relationships for an object.<p>
     *
     * @param context the call context
     * @param objectId the object id
     * @param includeSubRelationshipTypes flag to include relationship subtypes
     * @param relationshipDirection the direction for the relations
     * @param typeId the relation type id
     * @param filter the property filter
     * @param includeAllowableActions flag to include allowable actions
     * @param maxItems the maximum number of items to return
     * @param skipCount the number of items to skip
     *
     * @return the relationships for the object
     */
    ObjectList getObjectRelationships(
        CmsCmisCallContext context,
        String objectId,
        boolean includeSubRelationshipTypes,
        RelationshipDirection relationshipDirection,
        String typeId,
        String filter,
        boolean includeAllowableActions,
        BigInteger maxItems,
        BigInteger skipCount);

    /**
     * Gets the properties for a CMIS object.<p>
     *
     * @param context the call context
     * @param objectId the CMIS object id
     * @param filter the property filter string
     *
     * @return the set of properties
     */
    Properties getProperties(CmsCmisCallContext context, String objectId, String filter);

    /**
     * Gets the properties of the latest version.<p>
     *
     * @param context the call context
     * @param objectId the object id
     * @param versionSeriesId the version series id
     * @param major flag to access the latest major version
     * @param filter the property filter string
     *
     * @return the properties from the latest version
     */
    Properties getPropertiesOfLatestVersion(
        CmsCmisCallContext context,
        String objectId,
        String versionSeriesId,
        boolean major,
        String filter);

    /**
     * Gets the renditions for a CMIS object.<p>
     *
     * @param context the call context
     * @param objectId the  object id
     * @param renditionFilter the rendition filter
     * @param maxItems the maximum number of renditions
     * @param skipCount the number of renditions to skip
     *
     * @return the list of renditions
     */
    List<RenditionData> getRenditions(
        CmsCmisCallContext context,
        String objectId,
        String renditionFilter,
        BigInteger maxItems,
        BigInteger skipCount);

    /**
     * Gets the repository information for this repository.<p>
     *
     * @return the repository info
     */
    RepositoryInfo getRepositoryInfo();

    /**
     * Gets the children of a given type.<p>
     *
     * @param context the call context
     * @param typeId the parent type id
     * @param includePropertyDefinitions flag to include property definitions
     * @param maxItems the maximum number of items to return
     * @param skipCount the number of items to skip
     *
     * @return the list of child type definitions
     */
    TypeDefinitionList getTypeChildren(
        CmsCmisCallContext context,
        String typeId,
        boolean includePropertyDefinitions,
        BigInteger maxItems,
        BigInteger skipCount);

    /**
     * Gets a type definition by id.<p>
     *
     * @param context the call context
     * @param typeId the type id
     *
     * @return the type definition for the given id
     */
    TypeDefinition getTypeDefinition(CmsCmisCallContext context, String typeId);

    /**
     * Gets the type descendants.<p>
     *
     * @param context the call context
     * @param typeId the parent type id
     * @param depth the maximum type depth
     * @param includePropertyDefinitions flag to include the property definitions for types
     *
     * @return the list of type definitions
     */
    List<TypeDefinitionContainer> getTypeDescendants(
        CmsCmisCallContext context,
        String typeId,
        BigInteger depth,
        boolean includePropertyDefinitions);

    /**
     * Moves an object.<p>
     *
     * @param context the call context
     * @param objectId the object id
     * @param targetFolderId source source folder id
     * @param sourceFolderId the target folder id
     */
    void moveObject(CmsCmisCallContext context, Holder<String> objectId, String targetFolderId, String sourceFolderId);

    /**
     * Performs a query on the repository.<p>
     *
     * @param context the call context
     * @param statement the query
     * @param searchAllVersions flag to search all versions
     * @param includeAllowableActions flag to include allowable actions
     * @param includeRelationships flag to include relationships
     * @param renditionFilter the filter string for renditions
     * @param maxItems the maximum number of items to return
     * @param skipCount the number of items to skip
     *
     * @return the query result objects
     */
    ObjectList query(
        CmsCmisCallContext context,
        String statement,
        boolean searchAllVersions,
        boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        BigInteger maxItems,
        BigInteger skipCount);

    /**
     * Unfiles an object from a folder.<p>
     *
     * @param context the call context
     * @param objectId the id of the object to unfile
     * @param folderId the folder from which the object should be unfiled
     */
    void removeObjectFromFolder(CmsCmisCallContext context, String objectId, String folderId);

    /**
     * Removes a policy from an object.<p>
     *
     * @param context the call context
     * @param policyId the policy id
     * @param objectId the object id
     */
    void removePolicy(CmsCmisCallContext context, String policyId, String objectId);

    /**
     * Sets the content stream of an object.<p>
     *
     * @param context the call context
     * @param objectId the id of the object
     * @param overwriteFlag flag to overwrite the content stream
     * @param changeToken the change token
     * @param contentStream the new content stream
     */
    void setContentStream(
        CmsCmisCallContext context,
        Holder<String> objectId,
        boolean overwriteFlag,
        Holder<String> changeToken,
        ContentStream contentStream);

    /**
     * Updates the properties for an object.<p>
     *
     * @param context the call context
     * @param objectId the object id
     * @param changeToken the change token
     * @param properties the properties
     */
    void updateProperties(
        CmsCmisCallContext context,
        Holder<String> objectId,
        Holder<String> changeToken,
        Properties properties);

}

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

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionDefinitionDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionMappingDataImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfo;
import org.apache.chemistry.opencmis.commons.spi.Holder;

public class A_CmsCmisRepository {

    private String m_id;

    public String getId() {

        return m_id;
    }

    private static final String CMIS_READ = "cmis:read";
    private static final String CMIS_WRITE = "cmis:write";
    private static final String CMIS_ALL = "cmis:all";

    private static PermissionDefinition createPermission(String permission, String description) {

        PermissionDefinitionDataImpl pd = new PermissionDefinitionDataImpl();
        pd.setPermission(permission);
        pd.setDescription(description);

        return pd;
    }

    private static PermissionMapping createMapping(String key, String permission) {

        PermissionMappingDataImpl pm = new PermissionMappingDataImpl();
        pm.setKey(key);
        pm.setPermissions(Collections.singletonList(permission));

        return pm;
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.RepositoryService#getRepositoryInfo(java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public RepositoryInfo getRepositoryInfo(ExtensionsData extension) {

        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.RepositoryService#getTypeChildren(java.lang.String, java.lang.String, java.lang.Boolean, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public TypeDefinitionList getTypeChildren(
        CallContext context,
        String typeId,
        Boolean includePropertyDefinitions,
        BigInteger maxItems,
        BigInteger skipCount,
        ExtensionsData extension) {

        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.RepositoryService#getTypeDescendants(java.lang.String, java.lang.String, java.math.BigInteger, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public List<TypeDefinitionContainer> getTypeDescendants(
        CallContext context,
        String typeId,
        BigInteger depth,
        Boolean includePropertyDefinitions,
        ExtensionsData extension) {

        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.RepositoryService#getTypeDefinition(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public TypeDefinition getTypeDefinition(CallContext context, String typeId, ExtensionsData extension) {

        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.NavigationService#getChildren(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public ObjectInFolderList getChildren(
        CallContext context,
        String folderId,
        String filter,
        String orderBy,
        Boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        Boolean includePathSegment,
        BigInteger maxItems,
        BigInteger skipCount,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.NavigationService#getDescendants(java.lang.String, java.lang.String, java.math.BigInteger, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public List<ObjectInFolderContainer> getDescendants(
        CallContext context,
        String folderId,
        BigInteger depth,
        String filter,
        Boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        Boolean includePathSegment,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.NavigationService#getFolderTree(java.lang.String, java.lang.String, java.math.BigInteger, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public List<ObjectInFolderContainer> getFolderTree(
        CallContext context,
        String folderId,
        BigInteger depth,
        String filter,
        Boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        Boolean includePathSegment,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.NavigationService#getObjectParents(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public List<ObjectParentData> getObjectParents(
        CallContext context,
        String objectId,
        String filter,
        Boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        Boolean includeRelativePathSegment,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.NavigationService#getFolderParent(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public ObjectData getFolderParent(CallContext context, String folderId, String filter, ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.NavigationService#getCheckedOutDocs(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public ObjectList getCheckedOutDocs(
        CallContext context,
        String folderId,
        String filter,
        String orderBy,
        Boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        BigInteger maxItems,
        BigInteger skipCount,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#createDocument(java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.lang.String, org.apache.chemistry.opencmis.commons.data.ContentStream, org.apache.chemistry.opencmis.commons.enums.VersioningState, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public String createDocument(
        CallContext context,
        Properties properties,
        String folderId,
        ContentStream contentStream,
        VersioningState versioningState,
        List<String> policies,
        Acl addAces,
        Acl removeAces,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#createDocumentFromSource(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.lang.String, org.apache.chemistry.opencmis.commons.enums.VersioningState, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public String createDocumentFromSource(
        CallContext context,
        String sourceId,
        Properties properties,
        String folderId,
        VersioningState versioningState,
        List<String> policies,
        Acl addAces,
        Acl removeAces,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#createFolder(java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.lang.String, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public String createFolder(
        CallContext context,
        Properties properties,
        String folderId,
        List<String> policies,
        Acl addAces,
        Acl removeAces,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#createRelationship(java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public String createRelationship(
        CallContext context,
        Properties properties,
        List<String> policies,
        Acl addAces,
        Acl removeAces,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#createPolicy(java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.lang.String, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public String createPolicy(
        CallContext context,
        Properties properties,
        String folderId,
        List<String> policies,
        Acl addAces,
        Acl removeAces,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#getAllowableActions(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public AllowableActions getAllowableActions(CallContext context, String objectId, ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#getObject(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public ObjectData getObject(
        CallContext context,
        String objectId,
        String filter,
        Boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        Boolean includePolicyIds,
        Boolean includeAcl,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#getProperties(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public Properties getProperties(CallContext context, String objectId, String filter, ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#getRenditions(java.lang.String, java.lang.String, java.lang.String, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public List<RenditionData> getRenditions(
        CallContext context,
        String objectId,
        String renditionFilter,
        BigInteger maxItems,
        BigInteger skipCount,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#getObjectByPath(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public ObjectData getObjectByPath(
        CallContext context,
        String path,
        String filter,
        Boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        Boolean includePolicyIds,
        Boolean includeAcl,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#getContentStream(java.lang.String, java.lang.String, java.lang.String, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public ContentStream getContentStream(
        CallContext context,
        String objectId,
        String streamId,
        BigInteger offset,
        BigInteger length,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#updateProperties(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.data.Properties, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public void updateProperties(
        CallContext context,
        Holder<String> objectId,
        Holder<String> changeToken,
        Properties properties,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");

    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#moveObject(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public void moveObject(
        CallContext context,
        Holder<String> objectId,
        String targetFolderId,
        String sourceFolderId,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");

    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#deleteObject(java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public void deleteObject(CallContext context, String objectId, Boolean allVersions, ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");

    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#deleteTree(java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.UnfileObject, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public FailedToDeleteData deleteTree(
        CallContext context,
        String folderId,
        Boolean allVersions,
        UnfileObject unfileObjects,
        Boolean continueOnFailure,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#setContentStream(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, java.lang.Boolean, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.data.ContentStream, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public void setContentStream(
        CallContext context,
        Holder<String> objectId,
        Boolean overwriteFlag,
        Holder<String> changeToken,
        ContentStream contentStream,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");

    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#deleteContentStream(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public void deleteContentStream(
        CallContext context,
        Holder<String> objectId,
        Holder<String> changeToken,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");

    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.VersioningService#checkOut(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.data.ExtensionsData, org.apache.chemistry.opencmis.commons.spi.Holder)
     */
    public void checkOut(
        CallContext context,
        Holder<String> objectId,
        ExtensionsData extension,
        Holder<Boolean> contentCopied) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");

    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.VersioningService#cancelCheckOut(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public void cancelCheckOut(CallContext context, String objectId, ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");

    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.VersioningService#checkIn(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.Properties, org.apache.chemistry.opencmis.commons.data.ContentStream, java.lang.String, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public void checkIn(
        CallContext context,
        Holder<String> objectId,
        Boolean major,
        Properties properties,
        ContentStream contentStream,
        String checkinComment,
        List<String> policies,
        Acl addAces,
        Acl removeAces,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");

    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.VersioningService#getObjectOfLatestVersion(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public ObjectData getObjectOfLatestVersion(
        CallContext context,
        String objectId,
        String versionSeriesId,
        Boolean major,
        String filter,
        Boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        Boolean includePolicyIds,
        Boolean includeAcl,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.VersioningService#getPropertiesOfLatestVersion(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public Properties getPropertiesOfLatestVersion(
        CallContext context,
        String objectId,
        String versionSeriesId,
        Boolean major,
        String filter,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * 
     * @see org.apache.chemistry.opencmis.commons.spi.VersioningService#getAllVersions(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public List<ObjectData> getAllVersions(
        CallContext context,
        String objectId,
        String versionSeriesId,
        String filter,
        Boolean includeAllowableActions,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * 
     * @see org.apache.chemistry.opencmis.commons.spi.DiscoveryService#query(java.lang.String, java.lang.String, java.lang.Boolean, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public ObjectList query(
        CallContext context,
        String statement,
        Boolean searchAllVersions,
        Boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        BigInteger maxItems,
        BigInteger skipCount,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * 
     * @see org.apache.chemistry.opencmis.commons.spi.DiscoveryService#getContentChanges(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, java.lang.Boolean, java.lang.String, java.lang.Boolean, java.lang.Boolean, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public ObjectList getContentChanges(
        CallContext context,
        Holder<String> changeLogToken,
        Boolean includeProperties,
        String filter,
        Boolean includePolicyIds,
        Boolean includeAcl,
        BigInteger maxItems,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * 
     * @see org.apache.chemistry.opencmis.commons.spi.MultiFilingService#addObjectToFolder(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public void addObjectToFolder(
        CallContext context,
        String objectId,
        String folderId,
        Boolean allVersions,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");

    }

    /**
     * 
     * @see org.apache.chemistry.opencmis.commons.spi.MultiFilingService#removeObjectFromFolder(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public void removeObjectFromFolder(CallContext context, String objectId, String folderId, ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");

    }

    /**
     * 
     * @see org.apache.chemistry.opencmis.commons.spi.RelationshipService#getObjectRelationships(java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.RelationshipDirection, java.lang.String, java.lang.String, java.lang.Boolean, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public ObjectList getObjectRelationships(
        CallContext context,
        String objectId,
        Boolean includeSubRelationshipTypes,
        RelationshipDirection relationshipDirection,
        String typeId,
        String filter,
        Boolean includeAllowableActions,
        BigInteger maxItems,
        BigInteger skipCount,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * 
     * @see org.apache.chemistry.opencmis.commons.spi.AclService#getAcl(java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public Acl getAcl(CallContext context, String objectId, Boolean onlyBasicPermissions, ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * 
     * @see org.apache.chemistry.opencmis.commons.spi.AclService#applyAcl(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.enums.AclPropagation, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public Acl applyAcl(
        CallContext context,
        String objectId,
        Acl addAces,
        Acl removeAces,
        AclPropagation aclPropagation,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * 
     * @see org.apache.chemistry.opencmis.commons.spi.PolicyService#applyPolicy(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public void applyPolicy(CallContext context, String policyId, String objectId, ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");

    }

    /**
     * 
     * @see org.apache.chemistry.opencmis.commons.spi.PolicyService#removePolicy(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public void removePolicy(CallContext context, String policyId, String objectId, ExtensionsData extension) {

        throw new CmisNotSupportedException("Not supported!");
        // TODO: Auto-generated method stub

    }

    /**
     * 
     * @see org.apache.chemistry.opencmis.commons.spi.PolicyService#getAppliedPolicies(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public List<ObjectData> getAppliedPolicies(
        CallContext context,
        String objectId,
        String filter,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * 
     * @see org.apache.chemistry.opencmis.commons.server.CmisService#create(java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.lang.String, org.apache.chemistry.opencmis.commons.data.ContentStream, org.apache.chemistry.opencmis.commons.enums.VersioningState, java.util.List, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public String create(
        CallContext context,
        Properties properties,
        String folderId,
        ContentStream contentStream,
        VersioningState versioningState,
        List<String> policies,
        ExtensionsData extension) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * 
     * @see org.apache.chemistry.opencmis.commons.server.CmisService#deleteObjectOrCancelCheckOut(java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public void deleteObjectOrCancelCheckOut(
        CallContext context,
        String objectId,
        Boolean allVersions,
        ExtensionsData extension) {

        throw new CmisNotSupportedException("Not supported!");

        // TODO: Auto-generated method stub

    }

    /**
     * 
     * @see org.apache.chemistry.opencmis.commons.server.CmisService#applyAcl(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.enums.AclPropagation)
     */
    public Acl applyAcl(CallContext context, String objectId, Acl aces, AclPropagation aclPropagation) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * 
     * @see org.apache.chemistry.opencmis.commons.server.CmisService#getObjectInfo(java.lang.String, java.lang.String)
     */
    public ObjectInfo getObjectInfo(CallContext context, String objectId) {

        // TODO: Auto-generated method stub
        throw new CmisNotSupportedException("Not supported!");
    }

}

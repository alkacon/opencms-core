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

import org.opencms.main.OpenCms;

import java.math.BigInteger;
import java.util.ArrayList;
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
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfo;
import org.apache.chemistry.opencmis.commons.spi.Holder;

/**
 * The CMIS service class for OpenCms repositories.<p>
 *
 * Typically, a new instance of this class will be created for every CMIS service request. This class delegates the
 * actual work to an implementation of the {@link I_CmsCmisRepository} class.<p>
 */
public class CmsCmisService extends AbstractCmisService {

    /** The call context. */
    protected CallContext m_callContext;

    /**
     * Creates a new CMIS service instance.<p>
     *
     * @param context the CMIS call context
     */
    public CmsCmisService(CallContext context) {

        m_callContext = context;
    }

    /**
     * @param repositoryId
     * @param objectId
     * @param folderId
     * @param allVersions
     * @param extension
     * @see org.apache.chemistry.opencmis.commons.spi.MultiFilingService#addObjectToFolder(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public void addObjectToFolder(
        String repositoryId,
        String objectId,
        String folderId,
        Boolean allVersions,
        ExtensionsData extension) {

        getRepository(repositoryId).addObjectToFolder(makeContext(), objectId, folderId, allVersions.booleanValue());
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#applyPolicy(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public void applyPolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {

        getRepository(repositoryId).applyPolicy(makeContext(), policyId, objectId);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#createDocument(java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.lang.String, org.apache.chemistry.opencmis.commons.data.ContentStream, org.apache.chemistry.opencmis.commons.enums.VersioningState, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public String createDocument(
        String repositoryId,
        Properties properties,
        String folderId,
        ContentStream contentStream,
        VersioningState versioningState,
        List<String> policies,
        Acl addAces,
        Acl removeAces,
        ExtensionsData extension) {

        return getRepository(repositoryId).createDocument(
            makeContext(),
            properties,
            folderId,
            contentStream,
            versioningState,
            policies,
            addAces,
            removeAces);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#createDocumentFromSource(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.lang.String, org.apache.chemistry.opencmis.commons.enums.VersioningState, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public String createDocumentFromSource(
        String repositoryId,
        String sourceId,
        Properties properties,
        String folderId,
        VersioningState versioningState,
        List<String> policies,
        Acl addAces,
        Acl removeAces,
        ExtensionsData extension) {

        return getRepository(repositoryId).createDocumentFromSource(
            makeContext(),
            sourceId,
            properties,
            folderId,
            versioningState,
            policies,
            addAces,
            removeAces);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#createFolder(java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.lang.String, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public String createFolder(
        String repositoryId,
        Properties properties,
        String folderId,
        List<String> policies,
        Acl addAces,
        Acl removeAces,
        ExtensionsData extension) {

        return getRepository(repositoryId).createFolder(
            makeContext(),
            properties,
            folderId,
            policies,
            addAces,
            removeAces);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#createPolicy(java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.lang.String, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public String createPolicy(
        String repositoryId,
        Properties properties,
        String folderId,
        List<String> policies,
        Acl addAces,
        Acl removeAces,
        ExtensionsData extension) {

        return getRepository(repositoryId).createPolicy(
            makeContext(),
            properties,
            folderId,
            policies,
            addAces,
            removeAces);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#createRelationship(java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public String createRelationship(
        String repositoryId,
        Properties properties,
        List<String> policies,
        Acl addAces,
        Acl removeAces,
        ExtensionsData extension) {

        return getRepository(repositoryId).createRelationship(makeContext(), properties, policies, addAces, removeAces);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#deleteContentStream(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public void deleteContentStream(
        String repositoryId,
        Holder<String> objectId,
        Holder<String> changeToken,
        ExtensionsData extension) {

        getRepository(repositoryId).deleteContentStream(makeContext(), objectId, changeToken);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#deleteObject(java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public void deleteObject(String repositoryId, String objectId, Boolean allVersions, ExtensionsData extension) {

        getRepository(repositoryId).deleteObject(makeContext(), objectId, allVersions.booleanValue());
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#deleteObjectOrCancelCheckOut(java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public void deleteObjectOrCancelCheckOut(
        String repositoryId,
        String objectId,
        Boolean allVersions,
        ExtensionsData extension) {

        getRepository(repositoryId).deleteObject(makeContext(), objectId, allVersions.booleanValue());
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#deleteTree(java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.UnfileObject, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public FailedToDeleteData deleteTree(
        String repositoryId,
        String folderId,
        Boolean allVersions,
        UnfileObject unfileObjects,
        Boolean continueOnFailure,
        ExtensionsData extension) {

        return getRepository(repositoryId).deleteTree(
            makeContext(),
            folderId,
            allVersions.booleanValue(),
            unfileObjects,
            continueOnFailure.booleanValue());
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getAcl(java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public Acl getAcl(String repositoryId, String objectId, Boolean onlyBasicPermissions, ExtensionsData extension) {

        return getRepository(repositoryId).getAcl(makeContext(), objectId, onlyBasicPermissions.booleanValue());
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getAllowableActions(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public AllowableActions getAllowableActions(String repositoryId, String objectId, ExtensionsData extension) {

        return getRepository(repositoryId).getAllowableActions(makeContext(), objectId);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getAppliedPolicies(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public List<ObjectData> getAppliedPolicies(
        String repositoryId,
        String objectId,
        String filter,
        ExtensionsData extension) {

        return getRepository(repositoryId).getAppliedPolicies(makeContext(), objectId, filter);
    }

    /**
     *
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getCheckedOutDocs(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public ObjectList getCheckedOutDocs(
        String repositoryId,
        String folderId,
        String filter,
        String orderBy,
        Boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        BigInteger maxItems,
        BigInteger skipCount,
        ExtensionsData extension) {

        return getRepository(repositoryId).getCheckedOutDocs(
            makeContext(),
            folderId,
            filter,
            orderBy,
            includeAllowableActions.booleanValue(),
            includeRelationships,
            renditionFilter,
            maxItems,
            skipCount);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getChildren(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public ObjectInFolderList getChildren(
        String repositoryId,
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

        return getRepository(repositoryId).getChildren(
            makeContext(),
            folderId,
            filter,
            orderBy,
            includeAllowableActions.booleanValue(),
            includeRelationships,
            renditionFilter,
            includePathSegment.booleanValue(),
            maxItems,
            skipCount);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getContentChanges(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, java.lang.Boolean, java.lang.String, java.lang.Boolean, java.lang.Boolean, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public ObjectList getContentChanges(
        String repositoryId,
        Holder<String> changeLogToken,
        Boolean includeProperties,
        String filter,
        Boolean includePolicyIds,
        Boolean includeAcl,
        BigInteger maxItems,
        ExtensionsData extension) {

        return getRepository(repositoryId).getContentChanges(
            makeContext(),
            changeLogToken,
            includeProperties.booleanValue(),
            filter,
            includePolicyIds.booleanValue(),
            includeAcl.booleanValue(),
            maxItems);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getContentStream(java.lang.String, java.lang.String, java.lang.String, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public ContentStream getContentStream(
        String repositoryId,
        String objectId,
        String streamId,
        BigInteger offset,
        BigInteger length,
        ExtensionsData extension) {

        return getRepository(repositoryId).getContentStream(makeContext(), objectId, streamId, offset, length);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getDescendants(java.lang.String, java.lang.String, java.math.BigInteger, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public List<ObjectInFolderContainer> getDescendants(
        String repositoryId,
        String folderId,
        BigInteger depth,
        String filter,
        Boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        Boolean includePathSegment,
        ExtensionsData extension) {

        return getRepository(repositoryId).getDescendants(
            makeContext(),
            folderId,
            depth,
            filter,
            includeAllowableActions.booleanValue(),
            includePathSegment.booleanValue(),
            false);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getFolderParent(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public ObjectData getFolderParent(String repositoryId, String folderId, String filter, ExtensionsData extension) {

        return getRepository(repositoryId).getFolderParent(makeContext(), folderId, filter);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getFolderTree(java.lang.String, java.lang.String, java.math.BigInteger, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public List<ObjectInFolderContainer> getFolderTree(
        String repositoryId,
        String folderId,
        BigInteger depth,
        String filter,
        Boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        Boolean includePathSegment,
        ExtensionsData extension) {

        return getRepository(repositoryId).getDescendants(
            makeContext(),
            folderId,
            depth,
            filter,
            includeAllowableActions.booleanValue(),
            includePathSegment.booleanValue(),
            true);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getObject(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public ObjectData getObject(
        String repositoryId,
        String objectId,
        String filter,
        Boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        Boolean includePolicyIds,
        Boolean includeAcl,
        ExtensionsData extension) {

        return getRepository(repositoryId).getObject(
            makeContext(),
            objectId,
            filter,
            includeAllowableActions.booleanValue(),
            includeRelationships,
            renditionFilter,
            includePolicyIds.booleanValue(),
            includeAcl.booleanValue());
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getObjectByPath(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public ObjectData getObjectByPath(
        String repositoryId,
        String path,
        String filter,
        Boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        Boolean includePolicyIds,
        Boolean includeAcl,
        ExtensionsData extension) {

        return getRepository(repositoryId).getObjectByPath(
            makeContext(),
            path,
            filter,
            includeAllowableActions.booleanValue(),
            includeRelationships,
            renditionFilter,
            includePolicyIds.booleanValue(),
            includeAcl.booleanValue());
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getObjectInfo(java.lang.String, java.lang.String)
     */
    @Override
    public ObjectInfo getObjectInfo(String repositoryId, String objectId) {

        ObjectInfo result = super.getObjectInfo(repositoryId, objectId);
        return result;
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getObjectParents(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public List<ObjectParentData> getObjectParents(
        String repositoryId,
        String objectId,
        String filter,
        Boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        Boolean includeRelativePathSegment,
        ExtensionsData extension) {

        return getRepository(repositoryId).getObjectParents(
            makeContext(),
            objectId,
            filter,
            includeAllowableActions.booleanValue(),
            includeRelativePathSegment.booleanValue());
    }

    /**
     * Gets all or a subset of relationships associated with an independent
     * object.
     */
    @Override
    public ObjectList getObjectRelationships(
        String repositoryId,
        String objectId,
        Boolean includeSubRelationshipTypes,
        RelationshipDirection relationshipDirection,
        String typeId,
        String filter,
        Boolean includeAllowableActions,
        BigInteger maxItems,
        BigInteger skipCount,
        ExtensionsData extension) {

        return getRepository(repositoryId).getObjectRelationships(
            makeContext(),
            objectId,
            includeSubRelationshipTypes.booleanValue(),
            relationshipDirection,
            typeId,
            filter,
            includeAllowableActions.booleanValue(),
            maxItems,
            skipCount);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getProperties(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public Properties getProperties(String repositoryId, String objectId, String filter, ExtensionsData extension) {

        return getRepository(repositoryId).getProperties(makeContext(), objectId, filter);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getRenditions(java.lang.String, java.lang.String, java.lang.String, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public List<RenditionData> getRenditions(
        String repositoryId,
        String objectId,
        String renditionFilter,
        BigInteger maxItems,
        BigInteger skipCount,
        ExtensionsData extension) {

        return getRepository(repositoryId).getRenditions(makeContext(), objectId, renditionFilter, maxItems, skipCount);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getRepositoryInfo(java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension) {

        return getRepository(repositoryId).getRepositoryInfo();
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getRepositoryInfos(org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension) {

        List<RepositoryInfo> result = new ArrayList<RepositoryInfo>();
        for (I_CmsCmisRepository repository : OpenCms.getRepositoryManager().getRepositories(
            I_CmsCmisRepository.class)) {
            result.add(repository.getRepositoryInfo());
        }
        return result;
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getTypeChildren(java.lang.String, java.lang.String, java.lang.Boolean, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public TypeDefinitionList getTypeChildren(
        String repositoryId,
        String typeId,
        Boolean includePropertyDefinitions,
        BigInteger maxItems,
        BigInteger skipCount,
        ExtensionsData extension) {

        return getRepository(repositoryId).getTypeChildren(
            makeContext(),
            typeId,
            includePropertyDefinitions.booleanValue(),
            maxItems,
            skipCount);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getTypeDefinition(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension) {

        return getRepository(repositoryId).getTypeDefinition(makeContext(), typeId);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getTypeDescendants(java.lang.String, java.lang.String, java.math.BigInteger, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public List<TypeDefinitionContainer> getTypeDescendants(
        String repositoryId,
        String typeId,
        BigInteger depth,
        Boolean includePropertyDefinitions,
        ExtensionsData extension) {

        return getRepository(repositoryId).getTypeDescendants(
            makeContext(),
            typeId,
            depth,
            includePropertyDefinitions.booleanValue());
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#moveObject(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public void moveObject(
        String repositoryId,
        Holder<String> objectId,
        String targetFolderId,
        String sourceFolderId,
        ExtensionsData extension) {

        getRepository(repositoryId).moveObject(makeContext(), objectId, targetFolderId, sourceFolderId);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#query(java.lang.String, java.lang.String, java.lang.Boolean, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public ObjectList query(
        String repositoryId,
        String statement,
        Boolean searchAllVersions,
        Boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        BigInteger maxItems,
        BigInteger skipCount,
        ExtensionsData extension) {

        return getRepository(repositoryId).query(
            makeContext(),
            statement,
            searchAllVersions.booleanValue(),
            includeAllowableActions.booleanValue(),
            includeRelationships,
            renditionFilter,
            maxItems,
            skipCount);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#removeObjectFromFolder(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public void removeObjectFromFolder(
        String repositoryId,
        String objectId,
        String folderId,
        ExtensionsData extension) {

        getRepository(repositoryId).removeObjectFromFolder(makeContext(), objectId, folderId);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#removePolicy(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public void removePolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {

        getRepository(repositoryId).removePolicy(makeContext(), policyId, objectId);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#setContentStream(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, java.lang.Boolean, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.data.ContentStream, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public void setContentStream(
        String repositoryId,
        Holder<String> objectId,
        Boolean overwriteFlag,
        Holder<String> changeToken,
        ContentStream contentStream,
        ExtensionsData extension) {

        getRepository(repositoryId).setContentStream(
            makeContext(),
            objectId,
            overwriteFlag.booleanValue(),
            changeToken,
            contentStream);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#updateProperties(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.data.Properties, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public void updateProperties(
        String repositoryId,
        Holder<String> objectId,
        Holder<String> changeToken,
        Properties properties,
        ExtensionsData extension) {

        getRepository(repositoryId).updateProperties(makeContext(), objectId, changeToken, properties);
    }

    /**
     * Gets the repository for a given repository id.<p>
     *
     * @param repositoryId the repository id
     *
     * @return the repository with the given id
     */
    protected I_CmsCmisRepository getRepository(String repositoryId) {

        I_CmsCmisRepository result = OpenCms.getRepositoryManager().getRepository(
            repositoryId,
            I_CmsCmisRepository.class);
        if (result == null) {
            throw new CmisConnectionException("missing repository: " + repositoryId);
        }
        return result;
    }

    /**
     * Creates a call context wrapper.<p>
     *
     * @return the created wrapper
     */
    private CmsCmisCallContext makeContext() {

        return new CmsCmisCallContext(m_callContext, this);
    }
}

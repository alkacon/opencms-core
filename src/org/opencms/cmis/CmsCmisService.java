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
import org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.spi.Holder;

/**
 * The CMIS service class for OpenCms repositories.<p>
 * 
 * Typically, a new instance of this class will be created for every CMIS service request. This class delegates the
 * actual work to the CmsCmisRepository class.<p>
 */
public class CmsCmisService extends AbstractCmisService {

    /** The repository manager. */
    protected CmsCmisRepositoryManager m_repositoryManager;

    public CmsCmisService(CmsCmisRepositoryManager repoManager) {

        m_repositoryManager = repoManager;
    }

    protected CmsCmisRepository getRepository(String repositoryId) {

        return m_repositoryManager.getRepository(repositoryId);
    }

    private CmsCmisRepository m_repository;

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

        getRepository(repositoryId).addObjectToFolder(m_callContext, objectId, folderId, allVersions, extension);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#applyPolicy(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public void applyPolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {

        getRepository(repositoryId).applyPolicy(m_callContext, policyId, objectId, extension);
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
            m_callContext,
            properties,
            folderId,
            contentStream,
            versioningState,
            policies,
            addAces,
            removeAces,
            extension);
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
            m_callContext,
            sourceId,
            properties,
            folderId,
            versioningState,
            policies,
            addAces,
            removeAces,
            extension);
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
            m_callContext,
            properties,
            folderId,
            policies,
            addAces,
            removeAces,
            extension);
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
            m_callContext,
            properties,
            folderId,
            policies,
            addAces,
            removeAces,
            extension);
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

        return getRepository(repositoryId).createRelationship(
            m_callContext,
            properties,
            policies,
            addAces,
            removeAces,
            extension);
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

        getRepository(repositoryId).deleteContentStream(m_callContext, objectId, changeToken, extension);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#deleteObject(java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public void deleteObject(String repositoryId, String objectId, Boolean allVersions, ExtensionsData extension) {

        getRepository(repositoryId).deleteObject(m_callContext, objectId, allVersions, extension);
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

        getRepository(repositoryId).deleteObject(m_callContext, objectId, allVersions, extension);
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
            m_callContext,
            folderId,
            allVersions,
            unfileObjects,
            continueOnFailure,
            extension);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getAllowableActions(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public AllowableActions getAllowableActions(String repositoryId, String objectId, ExtensionsData extension) {

        return getRepository(repositoryId).getAllowableActions(m_callContext, objectId, extension);
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

        return getRepository(repositoryId).getAppliedPolicies(m_callContext, objectId, filter, extension);
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
            m_callContext,
            folderId,
            filter,
            orderBy,
            includeAllowableActions,
            includeRelationships,
            renditionFilter,
            maxItems,
            skipCount,
            extension);
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
            m_callContext,
            folderId,
            filter,
            orderBy,
            includeAllowableActions,
            includeRelationships,
            renditionFilter,
            includePathSegment,
            maxItems,
            skipCount,
            extension,
            this);
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
            m_callContext,
            changeLogToken,
            includeProperties,
            filter,
            includePolicyIds,
            includeAcl,
            maxItems,
            extension);
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

        return getRepository(repositoryId).getContentStream(
            m_callContext,
            objectId,
            streamId,
            offset,
            length,
            extension);
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
            getCallContext(),
            folderId,
            depth,
            filter,
            includeAllowableActions,
            includePathSegment,
            this,
            false);
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getFolderParent(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    @Override
    public ObjectData getFolderParent(String repositoryId, String folderId, String filter, ExtensionsData extension) {

        return getRepository(repositoryId).getFolderParent(m_callContext, folderId, filter, extension, this);
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
            getCallContext(),
            folderId,
            depth,
            filter,
            includeAllowableActions,
            includePathSegment,
            this,
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
            m_callContext,
            objectId,
            filter,
            includeAllowableActions,
            includeRelationships,
            renditionFilter,
            includePolicyIds,
            includeAcl,
            extension,
            this);
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
            m_callContext,
            path,
            filter,
            includeAllowableActions,
            includeRelationships,
            renditionFilter,
            includePolicyIds,
            includeAcl,
            extension,
            this);
    }

    /**
     * @param repositoryId
     * @param objectId
     * @param filter
     * @param includeAllowableActions
     * @param includeRelationships
     * @param renditionFilter
     * @param includeRelativePathSegment
     * @param extension
     * @return
     * @see org.apache.chemistry.opencmis.commons.spi.NavigationService#getObjectParents(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
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
            m_callContext,
            objectId,
            filter,
            includeAllowableActions,
            includeRelativePathSegment,
            extension,
            this);
    }

    /**
     * @param repositoryId
     * @param objectId
     * @param includeSubRelationshipTypes
     * @param relationshipDirection
     * @param typeId
     * @param filter
     * @param includeAllowableActions
     * @param maxItems
     * @param skipCount
     * @param extension
     * @return
     * @see org.apache.chemistry.opencmis.commons.spi.RelationshipService#getObjectRelationships(java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.RelationshipDirection, java.lang.String, java.lang.String, java.lang.Boolean, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
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
            m_callContext,
            objectId,
            includeSubRelationshipTypes,
            relationshipDirection,
            typeId,
            filter,
            includeAllowableActions,
            maxItems,
            skipCount,
            extension);
    }

    /**
     * @param repositoryId
     * @param objectId
     * @param filter
     * @param extension
     * @return
     * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#getProperties(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public Properties getProperties(String repositoryId, String objectId, String filter, ExtensionsData extension) {

        return getRepository(repositoryId).getProperties(m_callContext, objectId, filter, extension, this);
    }

    /**
     * @param repositoryId
     * @param objectId
     * @param renditionFilter
     * @param maxItems
     * @param skipCount
     * @param extension
     * @return
     * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#getRenditions(java.lang.String, java.lang.String, java.lang.String, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public List<RenditionData> getRenditions(
        String repositoryId,
        String objectId,
        String renditionFilter,
        BigInteger maxItems,
        BigInteger skipCount,
        ExtensionsData extension) {

        return getRepository(repositoryId).getRenditions(
            m_callContext,
            objectId,
            renditionFilter,
            maxItems,
            skipCount,
            extension);
    }

    /**
     * @param repositoryId
     * @param extension
     * @return
     * @see org.apache.chemistry.opencmis.commons.spi.RepositoryService#getRepositoryInfo(java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension) {

        return getRepository(repositoryId).getRepositoryInfo(extension);
    }

    /**
     * @param extension
     * @return
     * @see org.apache.chemistry.opencmis.commons.spi.RepositoryService#getRepositoryInfos(org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension) {

        List<RepositoryInfo> result = new ArrayList<RepositoryInfo>();
        for (CmsCmisRepository repo : m_repositoryManager.getRepositories()) {
            result.add(repo.getRepositoryInfo(extension));
        }
        return result;
    }

    /**
     * @param repositoryId
     * @param typeId
     * @param includePropertyDefinitions
     * @param maxItems
     * @param skipCount
     * @param extension
     * @return
     * @see org.apache.chemistry.opencmis.commons.spi.RepositoryService#getTypeChildren(java.lang.String, java.lang.String, java.lang.Boolean, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public TypeDefinitionList getTypeChildren(
        String repositoryId,
        String typeId,
        Boolean includePropertyDefinitions,
        BigInteger maxItems,
        BigInteger skipCount,
        ExtensionsData extension) {

        return getRepository(repositoryId).getTypeChildren(
            m_callContext,
            typeId,
            includePropertyDefinitions,
            maxItems,
            skipCount,
            extension);
    }

    /**
     * @param repositoryId
     * @param typeId
     * @param extension
     * @return
     * @see org.apache.chemistry.opencmis.commons.spi.RepositoryService#getTypeDefinition(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension) {

        return getRepository(repositoryId).getTypeDefinition(m_callContext, typeId, extension);
    }

    /**
     * @param repositoryId
     * @param typeId
     * @param depth
     * @param includePropertyDefinitions
     * @param extension
     * @return
     * @see org.apache.chemistry.opencmis.commons.spi.RepositoryService#getTypeDescendants(java.lang.String, java.lang.String, java.math.BigInteger, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public List<TypeDefinitionContainer> getTypeDescendants(
        String repositoryId,
        String typeId,
        BigInteger depth,
        Boolean includePropertyDefinitions,
        ExtensionsData extension) {

        return getRepository(repositoryId).getTypeDescendants(
            m_callContext,
            typeId,
            depth,
            includePropertyDefinitions,
            extension);
    }

    /**
     * @param repositoryId
     * @param objectId
     * @param targetFolderId
     * @param sourceFolderId
     * @param extension
     * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#moveObject(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public void moveObject(
        String repositoryId,
        Holder<String> objectId,
        String targetFolderId,
        String sourceFolderId,
        ExtensionsData extension) {

        getRepository(repositoryId).moveObject(m_callContext, objectId, targetFolderId, sourceFolderId, extension);
    }

    /**
     * @param repositoryId
     * @param statement
     * @param searchAllVersions
     * @param includeAllowableActions
     * @param includeRelationships
     * @param renditionFilter
     * @param maxItems
     * @param skipCount
     * @param extension
     * @return
     * @see org.apache.chemistry.opencmis.commons.spi.DiscoveryService#query(java.lang.String, java.lang.String, java.lang.Boolean, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
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
            m_callContext,
            statement,
            searchAllVersions,
            includeAllowableActions,
            includeRelationships,
            renditionFilter,
            maxItems,
            skipCount,
            extension);
    }

    /**
     * @param repositoryId
     * @param objectId
     * @param folderId
     * @param extension
     * @see org.apache.chemistry.opencmis.commons.spi.MultiFilingService#removeObjectFromFolder(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public void removeObjectFromFolder(String repositoryId, String objectId, String folderId, ExtensionsData extension) {

        getRepository(repositoryId).removeObjectFromFolder(m_callContext, objectId, folderId, extension);
    }

    /**
     * @param repositoryId
     * @param policyId
     * @param objectId
     * @param extension
     * @see org.apache.chemistry.opencmis.commons.spi.PolicyService#removePolicy(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public void removePolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {

        getRepository(repositoryId).removePolicy(m_callContext, policyId, objectId, extension);
    }

    /**
     * @param repositoryId
     * @param objectId
     * @param overwriteFlag
     * @param changeToken
     * @param contentStream
     * @param extension
     * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#setContentStream(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, java.lang.Boolean, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.data.ContentStream, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public void setContentStream(
        String repositoryId,
        Holder<String> objectId,
        Boolean overwriteFlag,
        Holder<String> changeToken,
        ContentStream contentStream,
        ExtensionsData extension) {

        getRepository(repositoryId).setContentStream(
            m_callContext,
            objectId,
            overwriteFlag,
            changeToken,
            contentStream,
            extension);
    }

    /**
     * @param repositoryId
     * @param objectId
     * @param changeToken
     * @param properties
     * @param extension
     * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#updateProperties(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.data.Properties, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
     */
    public void updateProperties(
        String repositoryId,
        Holder<String> objectId,
        Holder<String> changeToken,
        Properties properties,
        ExtensionsData extension) {

        getRepository(repositoryId).updateProperties(m_callContext, objectId, changeToken, properties, extension);
    }

    public void setCallContext(CallContext context) {

        m_callContext = context;
    }

    public CallContext getCallContext() {

        return m_callContext;
    }

    protected CallContext m_callContext;

}

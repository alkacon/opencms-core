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

import static org.opencms.cmis.CmsCmisUtil.checkResourceName;
import static org.opencms.cmis.CmsCmisUtil.ensureLock;
import static org.opencms.cmis.CmsCmisUtil.handleCmsException;
import static org.opencms.cmis.CmsCmisUtil.splitFilter;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.repository.CmsRepositoryFilter;
import org.opencms.search.CmsSearchException;
import org.opencms.search.solr.CmsSolrIndex;
import org.opencms.search.solr.CmsSolrQuery;
import org.opencms.search.solr.CmsSolrResultList;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsResourceTranslator;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.CapabilityRenditions;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.SupportedPermissions;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AclCapabilitiesDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FailedToDeleteDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderContainerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectParentDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionDefinitionDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionMappingDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryCapabilitiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryInfoImpl;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.commons.logging.Log;

/**
 * Repository instance for CMIS repositories.<p>
 */
public class CmsCmisRepository extends A_CmsCmisRepository {

    /**
     * Simple helper class to simplify creating a permission mapping.<p>
     */
    @SuppressWarnings("serial")
    private static class PermissionMappings extends HashMap<String, PermissionMapping> {

        /** Default constructor.<p> */
        public PermissionMappings() {

        }

        /**
         * Creates a single mapping entry.<p>
         *
         * @param key the mapping key
         * @param permission the permission
         *
         * @return the mapping entry
         */
        private static PermissionMapping createMapping(String key, String permission) {

            PermissionMappingDataImpl pm = new PermissionMappingDataImpl();
            pm.setKey(key);
            pm.setPermissions(Collections.singletonList(permission));

            return pm;
        }

        /**
         * Adds a permission mapping.<p>
         *
         * @param key the key
         * @param permission the permissions
         *
         * @return the instance itself
         */
        public PermissionMappings add(String key, String permission) {

            put(key, createMapping(key, permission));
            return this;
        }

    }

    /** The description parameter name. */
    public static final String PARAM_DESCRIPTION = "description";

    /** The project parameter name. */
    public static final String PARAM_PROJECT = "project";

    /** The property parameter name. */
    public static final String PARAM_PROPERTY = "property";

    /** The rendition parameter name. */
    public static final String PARAM_RENDITION = "rendition";

    /** The logger instance for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsCmisRepository.class);

    /** The index parameter name. */
    private static final String PARAM_INDEX = "index";

    /** The internal admin CMS context. */
    private CmsObject m_adminCms;

    /** The repository description. */
    private String m_description;

    /** The repository filter. */
    private CmsRepositoryFilter m_filter;

    /** The repository id. */
    private String m_id;

    /** The name of the SOLR index to use for querying. */
    private String m_indexName;

    /**
     * Readonly flag to prevent write operations on the repository.<p>
     */
    private boolean m_isReadOnly;

    /** The parameter configuration map. */
    private CmsParameterConfiguration m_parameterConfiguration = new CmsParameterConfiguration();

    /** The project of the repository. */
    private CmsProject m_project;

    /** List of dynamic property providers. */
    private List<I_CmsPropertyProvider> m_propertyProviders = new ArrayList<I_CmsPropertyProvider>();

    /** The relation object helper. */
    private CmsCmisRelationHelper m_relationHelper = new CmsCmisRelationHelper(this);

    /** The map of rendition providers by stream ids. */
    private Map<String, I_CmsCmisRenditionProvider> m_renditionProviders = new HashMap<String, I_CmsCmisRenditionProvider>();

    /** The resource object helper. */
    private CmsCmisResourceHelper m_resourceHelper = new CmsCmisResourceHelper(this);

    /** The root folder. */
    private CmsResource m_root;

    /**
     * Creates a permission definition.<p>
     *
     * @param permission the permission name
     * @param description the permission description
     *
     * @return the new permission definition
     */
    private static PermissionDefinition createPermission(String permission, String description) {

        PermissionDefinitionDataImpl pd = new PermissionDefinitionDataImpl();
        pd.setId(permission);
        pd.setDescription(description);

        return pd;
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_parameterConfiguration.add(paramName, paramValue);

    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#createDocument(org.opencms.cmis.CmsCmisCallContext, org.apache.chemistry.opencmis.commons.data.Properties, java.lang.String, org.apache.chemistry.opencmis.commons.data.ContentStream, org.apache.chemistry.opencmis.commons.enums.VersioningState, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl)
     */
    public synchronized String createDocument(
        CmsCmisCallContext context,
        Properties propertiesObj,
        String folderId,
        ContentStream contentStream,
        VersioningState versioningState,
        List<String> policies,
        Acl addAces,
        Acl removeAces) {

        checkWriteAccess();

        if ((addAces != null) || (removeAces != null)) {
            throw new CmisConstraintException("createDocument: ACEs not allowed");
        }

        if (contentStream == null) {
            throw new CmisConstraintException("createDocument: no content stream given");
        }

        try {
            CmsObject cms = getCmsObject(context);
            Map<String, PropertyData<?>> properties = propertiesObj.getProperties();
            String newDocName = (String)properties.get(PropertyIds.NAME).getFirstValue();
            String defaultType = OpenCms.getResourceManager().getDefaultTypeForName(newDocName).getTypeName();
            String resTypeName = getResourceTypeFromProperties(properties, defaultType);
            I_CmsResourceType cmsResourceType = OpenCms.getResourceManager().getResourceType(resTypeName);
            if (cmsResourceType.isFolder()) {
                throw new CmisConstraintException("Not a document type: " + resTypeName);
            }
            List<CmsProperty> cmsProperties = getOpenCmsProperties(properties);
            checkResourceName(newDocName);
            InputStream stream = contentStream.getStream();
            byte[] content = CmsFileUtil.readFully(stream);
            CmsUUID parentFolderId = new CmsUUID(folderId);
            CmsResource parentFolder = cms.readResource(parentFolderId);
            String newFolderPath = CmsStringUtil.joinPaths(parentFolder.getRootPath(), newDocName);
            try {
                CmsResource newDocument = cms.createResource(
                    newFolderPath,
                    cmsResourceType.getTypeId(),
                    content,
                    cmsProperties);
                cms.unlockResource(newDocument.getRootPath());
                return newDocument.getStructureId().toString();
            } catch (CmsVfsResourceAlreadyExistsException e) {
                throw new CmisNameConstraintViolationException(e.getLocalizedMessage(), e);
            }
        } catch (CmsException e) {
            handleCmsException(e);
            return null;
        } catch (IOException e) {
            throw new CmisRuntimeException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#createDocumentFromSource(org.opencms.cmis.CmsCmisCallContext, java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.lang.String, org.apache.chemistry.opencmis.commons.enums.VersioningState, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl)
     */
    public synchronized String createDocumentFromSource(
        CmsCmisCallContext context,
        String sourceId,
        Properties propertiesObj,
        String folderId,
        VersioningState versioningState,
        List<String> policies,
        Acl addAces,
        Acl removeAces) {

        checkWriteAccess();

        if ((addAces != null) || (removeAces != null)) {
            throw new CmisConstraintException("createDocument: ACEs not allowed");
        }

        try {
            CmsObject cms = getCmsObject(context);
            Map<String, PropertyData<?>> properties = new HashMap<String, PropertyData<?>>();
            if (propertiesObj != null) {
                properties = propertiesObj.getProperties();
            }
            List<CmsProperty> cmsProperties = getOpenCmsProperties(properties);
            CmsUUID parentFolderId = new CmsUUID(folderId);
            CmsResource parentFolder = cms.readResource(parentFolderId);
            CmsUUID sourceUuid = new CmsUUID(sourceId);
            CmsResource source = cms.readResource(sourceUuid);
            String sourcePath = source.getRootPath();

            PropertyData<?> nameProp = properties.get(PropertyIds.NAME);
            String newDocName;
            if (nameProp != null) {
                newDocName = (String)nameProp.getFirstValue();
                checkResourceName(newDocName);
            } else {
                newDocName = CmsResource.getName(source.getRootPath());
            }
            String targetPath = CmsStringUtil.joinPaths(parentFolder.getRootPath(), newDocName);

            try {
                cms.copyResource(sourcePath, targetPath);
            } catch (CmsVfsResourceAlreadyExistsException e) {
                throw new CmisNameConstraintViolationException(e.getLocalizedMessage(), e);
            }

            CmsResource targetResource = cms.readResource(targetPath);
            cms.setDateLastModified(targetResource.getRootPath(), targetResource.getDateCreated(), false);
            cms.unlockResource(targetResource);
            boolean wasLocked = ensureLock(cms, targetResource);
            cms.writePropertyObjects(targetResource, cmsProperties);
            for (String key : properties.keySet()) {
                if (key.startsWith(CmsCmisTypeManager.PROPERTY_PREFIX_DYNAMIC)) {
                    I_CmsPropertyProvider provider = getTypeManager().getPropertyProvider(key);
                    try {
                        String value = (String)(properties.get(key).getFirstValue());
                        provider.setPropertyValue(cms, targetResource, value);
                    } catch (CmsException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }

            if (wasLocked) {
                cms.unlockResource(targetResource);
            }
            return targetResource.getStructureId().toString();
        } catch (CmsException e) {
            handleCmsException(e);
            return null;
        }
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#createFolder(org.opencms.cmis.CmsCmisCallContext, org.apache.chemistry.opencmis.commons.data.Properties, java.lang.String, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl)
     */
    public synchronized String createFolder(
        CmsCmisCallContext context,
        Properties propertiesObj,
        String folderId,
        List<String> policies,
        Acl addAces,
        Acl removeAces) {

        checkWriteAccess();

        if ((addAces != null) || (removeAces != null)) {
            throw new CmisConstraintException("createFolder: ACEs not allowed");
        }

        try {
            CmsObject cms = getCmsObject(context);
            Map<String, PropertyData<?>> properties = propertiesObj.getProperties();
            String resTypeName = getResourceTypeFromProperties(properties, CmsResourceTypeFolder.getStaticTypeName());
            I_CmsResourceType cmsResourceType = OpenCms.getResourceManager().getResourceType(resTypeName);
            if (!cmsResourceType.isFolder()) {
                throw new CmisConstraintException("Invalid folder type: " + resTypeName);
            }
            List<CmsProperty> cmsProperties = getOpenCmsProperties(properties);
            String newFolderName = (String)properties.get(PropertyIds.NAME).getFirstValue();
            checkResourceName(newFolderName);
            CmsUUID parentFolderId = new CmsUUID(folderId);
            CmsResource parentFolder = cms.readResource(parentFolderId);
            String newFolderPath = CmsStringUtil.joinPaths(parentFolder.getRootPath(), newFolderName);
            try {
                CmsResource newFolder = cms.createResource(
                    newFolderPath,
                    cmsResourceType.getTypeId(),
                    null,
                    cmsProperties);
                cms.unlockResource(newFolder);
                return newFolder.getStructureId().toString();
            } catch (CmsVfsResourceAlreadyExistsException e) {
                throw new CmisNameConstraintViolationException(e.getLocalizedMessage(), e);
            }
        } catch (CmsException e) {
            handleCmsException(e);
            return null;
        }
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#createRelationship(org.opencms.cmis.CmsCmisCallContext, org.apache.chemistry.opencmis.commons.data.Properties, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl)
     */
    public synchronized String createRelationship(
        CmsCmisCallContext context,
        Properties properties,
        List<String> policies,
        Acl addAces,
        Acl removeAces) {

        try {
            CmsObject cms = getCmsObject(context);
            Map<String, PropertyData<?>> propertyMap = properties.getProperties();
            String sourceProp = (String)(propertyMap.get(PropertyIds.SOURCE_ID).getFirstValue());
            String targetProp = (String)(propertyMap.get(PropertyIds.TARGET_ID).getFirstValue());
            String typeId = (String)(propertyMap.get(PropertyIds.OBJECT_TYPE_ID).getFirstValue());
            if (!typeId.startsWith("opencms:")) {
                throw new CmisConstraintException("Can't create this relationship type.");
            }
            String cmsTypeName = typeId.substring("opencms:".length());
            CmsUUID sourceId = new CmsUUID(sourceProp);
            CmsUUID targetId = new CmsUUID(targetProp);
            CmsResource sourceRes = cms.readResource(sourceId);
            boolean wasLocked = ensureLock(cms, sourceRes);
            try {
                CmsResource targetRes = cms.readResource(targetId);
                cms.addRelationToResource(sourceRes.getRootPath(), targetRes.getRootPath(), cmsTypeName);
                return "REL_" + sourceRes.getStructureId() + "_" + targetRes.getStructureId() + "_" + cmsTypeName;
            } finally {
                if (wasLocked) {
                    cms.unlockResource(sourceRes);
                }
            }
        } catch (CmsException e) {
            CmsCmisUtil.handleCmsException(e);
            return null;
        }
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#deleteContentStream(org.opencms.cmis.CmsCmisCallContext, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.spi.Holder)
     */
    public synchronized void deleteContentStream(
        CmsCmisCallContext context,
        Holder<String> objectId,
        Holder<String> changeToken) {

        throw new CmisConstraintException("Content streams may not be deleted.");

    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#deleteObject(org.opencms.cmis.CmsCmisCallContext, java.lang.String, boolean)
     */
    public synchronized void deleteObject(CmsCmisCallContext context, String objectId, boolean allVersions) {

        checkWriteAccess();
        getHelper(objectId).deleteObject(context, objectId, allVersions);
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#deleteTree(org.opencms.cmis.CmsCmisCallContext, java.lang.String, boolean, org.apache.chemistry.opencmis.commons.enums.UnfileObject, boolean)
     */
    public synchronized FailedToDeleteData deleteTree(
        CmsCmisCallContext context,
        String folderId,
        boolean allVersions,
        UnfileObject unfileObjects,
        boolean continueOnFailure) {

        checkWriteAccess();

        try {

            FailedToDeleteDataImpl result = new FailedToDeleteDataImpl();
            result.setIds(new ArrayList<String>());
            CmsObject cms = getCmsObject(context);
            CmsUUID structureId = new CmsUUID(folderId);
            CmsResource folder = cms.readResource(structureId);
            if (!folder.isFolder()) {
                throw new CmisConstraintException("deleteTree can only be used on folders.");
            }
            ensureLock(cms, folder);
            cms.deleteResource(folder.getRootPath(), CmsResource.DELETE_PRESERVE_SIBLINGS);
            return result;
        } catch (CmsException e) {
            handleCmsException(e);
            return null;
        }
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getAcl(org.opencms.cmis.CmsCmisCallContext, java.lang.String, boolean)
     */
    public synchronized Acl getAcl(CmsCmisCallContext context, String objectId, boolean onlyBasicPermissions) {

        return getHelper(objectId).getAcl(context, objectId, onlyBasicPermissions);
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getAllowableActions(org.opencms.cmis.CmsCmisCallContext, java.lang.String)
     */
    public synchronized AllowableActions getAllowableActions(CmsCmisCallContext context, String objectId) {

        return getHelper(objectId).getAllowableActions(context, objectId);
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getCheckedOutDocs(org.opencms.cmis.CmsCmisCallContext, java.lang.String, java.lang.String, java.lang.String, boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.math.BigInteger, java.math.BigInteger)
     */
    public synchronized ObjectList getCheckedOutDocs(
        CmsCmisCallContext context,
        String folderId,
        String filter,
        String orderBy,
        boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        BigInteger maxItems,
        BigInteger skipCount) {

        ObjectListImpl result = new ObjectListImpl();
        result.setObjects(new ArrayList<ObjectData>());
        return result;
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getChildren(org.opencms.cmis.CmsCmisCallContext, java.lang.String, java.lang.String, java.lang.String, boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, boolean, java.math.BigInteger, java.math.BigInteger)
     */
    public synchronized ObjectInFolderList getChildren(
        CmsCmisCallContext context,
        String folderId,
        String filter,
        String orderBy,
        boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        boolean includePathSegment,
        BigInteger maxItems,
        BigInteger skipCount) {

        try {
            CmsCmisResourceHelper helper = getResourceHelper();

            // split filter
            Set<String> filterCollection = splitFilter(filter);
            // skip and max
            int skip = (skipCount == null ? 0 : skipCount.intValue());
            if (skip < 0) {
                skip = 0;
            }

            int max = (maxItems == null ? Integer.MAX_VALUE : maxItems.intValue());
            if (max < 0) {
                max = Integer.MAX_VALUE;
            }

            CmsObject cms = getCmsObject(context);
            CmsUUID structureId = new CmsUUID(folderId);
            CmsResource folder = cms.readResource(structureId);
            if (!folder.isFolder()) {
                throw new CmisObjectNotFoundException("Not a folder!");
            }

            // set object info of the the folder
            if (context.isObjectInfoRequired()) {
                helper.collectObjectData(
                    context,
                    cms,
                    folder,
                    null,
                    renditionFilter,
                    false,
                    false,
                    includeRelationships);
            }

            // prepare result
            ObjectInFolderListImpl result = new ObjectInFolderListImpl();
            String folderSitePath = cms.getRequestContext().getSitePath(folder);
            List<CmsResource> children = cms.getResourcesInFolder(folderSitePath, CmsResourceFilter.DEFAULT);
            CmsObjectListLimiter<CmsResource> limiter = new CmsObjectListLimiter<CmsResource>(
                children,
                maxItems,
                skipCount);
            List<ObjectInFolderData> resultObjects = new ArrayList<ObjectInFolderData>();
            for (CmsResource child : limiter) {
                // build and add child object
                ObjectInFolderDataImpl objectInFolder = new ObjectInFolderDataImpl();
                objectInFolder.setObject(
                    helper.collectObjectData(
                        context,
                        cms,
                        child,
                        filterCollection,
                        renditionFilter,
                        includeAllowableActions,
                        false,
                        includeRelationships));
                if (includePathSegment) {
                    objectInFolder.setPathSegment(child.getName());
                }
                resultObjects.add(objectInFolder);
            }
            result.setObjects(resultObjects);
            result.setNumItems(BigInteger.valueOf(children.size()));
            result.setHasMoreItems(Boolean.valueOf(limiter.hasMore()));
            return result;
        } catch (CmsException e) {
            handleCmsException(e);
            return null;
        }

    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getConfiguration()
     */
    public CmsParameterConfiguration getConfiguration() {

        return m_parameterConfiguration;
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getContentStream(org.opencms.cmis.CmsCmisCallContext, java.lang.String, java.lang.String, java.math.BigInteger, java.math.BigInteger)
     */
    public synchronized ContentStream getContentStream(
        CmsCmisCallContext context,
        String objectId,
        String streamId,
        BigInteger offset,
        BigInteger length) {

        try {
            CmsObject cms = getCmsObject(context);
            CmsResource resource = cms.readResource(new CmsUUID(objectId));
            byte[] contents = null;
            if (streamId != null) {
                I_CmsCmisRenditionProvider renditionProvider = m_renditionProviders.get(streamId);
                if (renditionProvider == null) {
                    throw new CmisRuntimeException("Invalid stream id " + streamId);
                }
                contents = renditionProvider.getContent(cms, resource);
            } else if (resource.isFolder()) {
                throw new CmisStreamNotSupportedException("Not a file!");
            } else {
                CmsFile file = cms.readFile(resource);
                contents = file.getContents();
            }
            contents = extractRange(contents, offset, length);
            InputStream stream = new ByteArrayInputStream(contents);
            ContentStreamImpl result = new ContentStreamImpl();
            result.setFileName(resource.getName());
            result.setLength(BigInteger.valueOf(contents.length));
            result.setMimeType(OpenCms.getResourceManager().getMimeType(resource.getRootPath(), null, "text/plain"));
            result.setStream(stream);

            return result;
        } catch (CmsException e) {
            handleCmsException(e);
            return null;
        }
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getDescendants(org.opencms.cmis.CmsCmisCallContext, java.lang.String, java.math.BigInteger, java.lang.String, boolean, boolean, boolean)
     */
    public synchronized List<ObjectInFolderContainer> getDescendants(
        CmsCmisCallContext context,
        String folderId,
        BigInteger depth,
        String filter,
        boolean includeAllowableActions,
        boolean includePathSegment,
        boolean foldersOnly) {

        try {
            CmsCmisResourceHelper helper = getResourceHelper();

            // check depth
            int d = (depth == null ? 2 : depth.intValue());
            if (d == 0) {
                throw new CmisInvalidArgumentException("Depth must not be 0!");
            }
            if (d < -1) {
                d = -1;
            }

            // split filter
            Set<String> filterCollection = splitFilter(filter);

            CmsObject cms = getCmsObject(context);
            CmsUUID folderStructureId = new CmsUUID(folderId);
            CmsResource folder = cms.readResource(folderStructureId);
            if (!folder.isFolder()) {
                throw new CmisObjectNotFoundException("Not a folder!");
            }

            // set object info of the the folder
            if (context.isObjectInfoRequired()) {
                helper.collectObjectData(
                    context,
                    cms,
                    folder,
                    null,
                    "cmis:none",
                    false,
                    false,
                    IncludeRelationships.NONE);
            }

            // get the tree
            List<ObjectInFolderContainer> result = new ArrayList<ObjectInFolderContainer>();
            gatherDescendants(
                context,
                cms,
                folder,
                result,
                foldersOnly,
                d,
                filterCollection,
                includeAllowableActions,
                includePathSegment);

            return result;
        } catch (CmsException e) {
            handleCmsException(e);
            return null;
        }
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getDescription()
     */
    public String getDescription() {

        if (m_description != null) {
            return m_description;
        }
        if (m_project != null) {
            return m_project.getDescription();
        }
        return m_id;
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getFilter()
     */
    public CmsRepositoryFilter getFilter() {

        return m_filter;
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getFolderParent(org.opencms.cmis.CmsCmisCallContext, java.lang.String, java.lang.String)
     */
    public synchronized ObjectData getFolderParent(CmsCmisCallContext context, String folderId, String filter) {

        List<ObjectParentData> parents = getObjectParents(context, folderId, filter, false, false);
        if (parents.size() == 0) {
            throw new CmisInvalidArgumentException("The root folder has no parent!");
        }
        return parents.get(0).getObject();
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getId()
     */
    public String getId() {

        return m_id;
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getName()
     */
    public String getName() {

        return m_id;
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getObject(org.opencms.cmis.CmsCmisCallContext, java.lang.String, java.lang.String, boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, boolean, boolean)
     */
    public synchronized ObjectData getObject(
        CmsCmisCallContext context,
        String objectId,
        String filter,
        boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        boolean includePolicyIds,
        boolean includeAcl) {

        return getHelper(objectId).getObject(
            context,
            objectId,
            filter,
            includeAllowableActions,
            includeRelationships,
            renditionFilter,
            includePolicyIds,
            includeAcl);
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getObjectByPath(org.opencms.cmis.CmsCmisCallContext, java.lang.String, java.lang.String, boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, boolean, boolean)
     */
    public synchronized ObjectData getObjectByPath(
        CmsCmisCallContext context,
        String path,
        String filter,
        boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        boolean includePolicyIds,
        boolean includeAcl

    ) {

        try {
            CmsCmisResourceHelper helper = getResourceHelper();

            // split filter
            Set<String> filterCollection = splitFilter(filter);

            // check path
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(path)) {
                throw new CmisInvalidArgumentException("Invalid folder path!");
            }
            CmsObject cms = getCmsObject(context);
            CmsResource file = cms.readResource(path);

            return helper.collectObjectData(
                context,
                cms,
                file,
                filterCollection,
                renditionFilter,
                includeAllowableActions,
                includeAcl,
                IncludeRelationships.NONE);

        } catch (CmsException e) {
            handleCmsException(e);
            return null;
        }
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getObjectParents(org.opencms.cmis.CmsCmisCallContext, java.lang.String, java.lang.String, boolean, boolean)
     */
    public synchronized List<ObjectParentData> getObjectParents(
        CmsCmisCallContext context,
        String objectId,
        String filter,
        boolean includeAllowableActions,
        boolean includeRelativePathSegment) {

        try {
            CmsCmisResourceHelper helper = getResourceHelper();

            // split filter
            Set<String> filterCollection = splitFilter(filter);
            CmsObject cms = getCmsObject(context);
            CmsUUID structureId = new CmsUUID(objectId);
            CmsResource file = cms.readResource(structureId);
            // don't climb above the root folder

            if (m_root.equals(file)) {
                return Collections.emptyList();
            }

            // set object info of the the object
            if (context.isObjectInfoRequired()) {
                helper.collectObjectData(
                    context,
                    cms,
                    file,
                    null,
                    "cmis:none",
                    false,
                    false,
                    IncludeRelationships.NONE);
            }

            // get parent folder
            CmsResource parent = cms.readParentFolder(file.getStructureId());
            ObjectData object = helper.collectObjectData(
                context,
                cms,
                parent,
                filterCollection,
                "cmis:none",
                includeAllowableActions,
                false,
                IncludeRelationships.NONE);

            ObjectParentDataImpl result = new ObjectParentDataImpl();
            result.setObject(object);
            if (includeRelativePathSegment) {
                result.setRelativePathSegment(file.getName());
            }

            return Collections.singletonList((ObjectParentData)result);
        } catch (CmsException e) {
            handleCmsException(e);
            return null;
        }

    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getObjectRelationships(org.opencms.cmis.CmsCmisCallContext, java.lang.String, boolean, org.apache.chemistry.opencmis.commons.enums.RelationshipDirection, java.lang.String, java.lang.String, boolean, java.math.BigInteger, java.math.BigInteger)
     */
    public synchronized ObjectList getObjectRelationships(
        CmsCmisCallContext context,
        String objectId,
        boolean includeSubRelationshipTypes,
        RelationshipDirection relationshipDirection,
        String typeId,
        String filter,
        boolean includeAllowableActions,
        BigInteger maxItems,
        BigInteger skipCount) {

        try {
            CmsObject cms = getCmsObject(context);
            ObjectListImpl result = new ObjectListImpl();
            CmsUUID structureId = new CmsUUID(objectId);
            CmsResource resource = cms.readResource(structureId);

            List<ObjectData> resultObjects = getRelationshipObjectData(
                context,
                cms,
                resource,
                relationshipDirection,
                CmsCmisUtil.splitFilter(filter),
                includeAllowableActions);
            CmsObjectListLimiter<ObjectData> limiter = new CmsObjectListLimiter<ObjectData>(
                resultObjects,
                maxItems,
                skipCount);
            List<ObjectData> limitedResults = new ArrayList<ObjectData>();
            for (ObjectData objectData : limiter) {
                limitedResults.add(objectData);
            }
            result.setNumItems(BigInteger.valueOf(resultObjects.size()));
            result.setHasMoreItems(Boolean.valueOf(limiter.hasMore()));
            result.setObjects(limitedResults);
            return result;
        } catch (CmsException e) {
            CmsCmisUtil.handleCmsException(e);
            return null;
        }
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getProperties(org.opencms.cmis.CmsCmisCallContext, java.lang.String, java.lang.String)
     */
    public synchronized Properties getProperties(CmsCmisCallContext context, String objectId, String filter) {

        ObjectData object = getObject(context, objectId, null, false, null, null, false, false);
        return object.getProperties();
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getRenditions(org.opencms.cmis.CmsCmisCallContext, java.lang.String, java.lang.String, java.math.BigInteger, java.math.BigInteger)
     */
    public synchronized List<RenditionData> getRenditions(
        CmsCmisCallContext context,
        String objectId,
        String renditionFilter,
        BigInteger maxItems,
        BigInteger skipCount) {

        try {
            CmsObject cms = getCmsObject(context);
            CmsResource resource = cms.readResource(new CmsUUID(objectId));
            return getResourceHelper().collectObjectData(
                context,
                cms,
                resource,
                null,
                renditionFilter,
                false,
                false,
                IncludeRelationships.NONE).getRenditions();
        } catch (CmsException e) {
            handleCmsException(e);
            return null;
        }
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getRepositoryInfo()
     */
    public synchronized RepositoryInfo getRepositoryInfo() {

        // compile repository info
        RepositoryInfoImpl repositoryInfo = new RepositoryInfoImpl();

        repositoryInfo.setId(m_id);
        repositoryInfo.setName(getName());
        repositoryInfo.setDescription(getDescription());

        repositoryInfo.setCmisVersionSupported("1.0");

        repositoryInfo.setProductName("OpenCms");
        repositoryInfo.setProductVersion(OpenCms.getSystemInfo().getVersion());
        repositoryInfo.setVendorName("Alkacon Software GmbH & Co. KG");
        repositoryInfo.setRootFolder(m_root.getStructureId().toString());
        repositoryInfo.setThinClientUri("");
        repositoryInfo.setPrincipalAnonymous(OpenCms.getDefaultUsers().getUserGuest());
        repositoryInfo.setChangesIncomplete(Boolean.TRUE);
        RepositoryCapabilitiesImpl capabilities = new RepositoryCapabilitiesImpl();
        capabilities.setCapabilityAcl(CapabilityAcl.DISCOVER);
        capabilities.setAllVersionsSearchable(Boolean.FALSE);
        capabilities.setCapabilityJoin(CapabilityJoin.NONE);
        capabilities.setSupportsMultifiling(Boolean.FALSE);
        capabilities.setSupportsUnfiling(Boolean.FALSE);
        capabilities.setSupportsVersionSpecificFiling(Boolean.FALSE);
        capabilities.setIsPwcSearchable(Boolean.FALSE);
        capabilities.setIsPwcUpdatable(Boolean.FALSE);
        capabilities.setCapabilityQuery(getIndex() != null ? CapabilityQuery.FULLTEXTONLY : CapabilityQuery.NONE);
        capabilities.setCapabilityChanges(CapabilityChanges.NONE);
        capabilities.setCapabilityContentStreamUpdates(CapabilityContentStreamUpdates.ANYTIME);
        capabilities.setSupportsGetDescendants(Boolean.TRUE);
        capabilities.setSupportsGetFolderTree(Boolean.TRUE);
        capabilities.setCapabilityRendition(CapabilityRenditions.READ);
        repositoryInfo.setCapabilities(capabilities);

        AclCapabilitiesDataImpl aclCapability = new AclCapabilitiesDataImpl();
        aclCapability.setSupportedPermissions(SupportedPermissions.BOTH);
        aclCapability.setAclPropagation(AclPropagation.REPOSITORYDETERMINED);

        // permissions
        List<PermissionDefinition> permissions = new ArrayList<PermissionDefinition>();
        permissions.add(createPermission(CMIS_READ, "Read"));
        permissions.add(createPermission(CMIS_WRITE, "Write"));
        permissions.add(createPermission(CMIS_ALL, "All"));
        aclCapability.setPermissionDefinitionData(permissions);

        // mappings
        PermissionMappings m = new PermissionMappings();
        m.add(PermissionMapping.CAN_CREATE_DOCUMENT_FOLDER, CMIS_WRITE);
        m.add(PermissionMapping.CAN_CREATE_FOLDER_FOLDER, CMIS_WRITE);
        m.add(PermissionMapping.CAN_DELETE_CONTENT_DOCUMENT, CMIS_WRITE);
        m.add(PermissionMapping.CAN_DELETE_OBJECT, CMIS_WRITE);
        m.add(PermissionMapping.CAN_DELETE_TREE_FOLDER, CMIS_WRITE);
        m.add(PermissionMapping.CAN_GET_ACL_OBJECT, CMIS_READ);
        m.add(PermissionMapping.CAN_GET_ALL_VERSIONS_VERSION_SERIES, CMIS_READ);
        m.add(PermissionMapping.CAN_GET_CHILDREN_FOLDER, CMIS_READ);
        m.add(PermissionMapping.CAN_GET_DESCENDENTS_FOLDER, CMIS_READ);
        m.add(PermissionMapping.CAN_GET_FOLDER_PARENT_OBJECT, CMIS_READ);
        m.add(PermissionMapping.CAN_GET_PARENTS_FOLDER, CMIS_READ);
        m.add(PermissionMapping.CAN_GET_PROPERTIES_OBJECT, CMIS_READ);
        m.add(PermissionMapping.CAN_MOVE_OBJECT, CMIS_WRITE);
        m.add(PermissionMapping.CAN_MOVE_SOURCE, CMIS_WRITE);
        m.add(PermissionMapping.CAN_MOVE_TARGET, CMIS_WRITE);
        m.add(PermissionMapping.CAN_SET_CONTENT_DOCUMENT, CMIS_WRITE);
        m.add(PermissionMapping.CAN_UPDATE_PROPERTIES_OBJECT, CMIS_WRITE);
        m.add(PermissionMapping.CAN_VIEW_CONTENT_OBJECT, CMIS_READ);
        aclCapability.setPermissionMappingData(m);
        repositoryInfo.setAclCapabilities(aclCapability);
        return repositoryInfo;
    }

    /**
     * @see org.opencms.repository.I_CmsRepository#getTranslation()
     */
    public CmsResourceTranslator getTranslation() {

        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getTypeChildren(org.opencms.cmis.CmsCmisCallContext, java.lang.String, boolean, java.math.BigInteger, java.math.BigInteger)
     */
    public synchronized TypeDefinitionList getTypeChildren(
        CmsCmisCallContext context,
        String typeId,
        boolean includePropertyDefinitions,
        BigInteger maxItems,
        BigInteger skipCount) {

        return m_typeManager.getTypeChildren(typeId, includePropertyDefinitions, maxItems, skipCount);
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getTypeDefinition(org.opencms.cmis.CmsCmisCallContext, java.lang.String)
     */
    public synchronized TypeDefinition getTypeDefinition(CmsCmisCallContext context, String typeId) {

        return m_typeManager.getTypeDefinition(typeId);
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#getTypeDescendants(org.opencms.cmis.CmsCmisCallContext, java.lang.String, java.math.BigInteger, boolean)
     */
    public synchronized List<TypeDefinitionContainer> getTypeDescendants(
        CmsCmisCallContext context,
        String typeId,
        BigInteger depth,
        boolean includePropertyDefinitions) {

        return m_typeManager.getTypeDescendants(typeId, depth, includePropertyDefinitions);
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#initConfiguration()
     */
    public void initConfiguration() throws CmsConfigurationException {

        if (m_filter != null) {
            m_filter.initConfiguration();
        }
        m_description = m_parameterConfiguration.getString(PARAM_DESCRIPTION, null);
        List<String> renditionProviderClasses = m_parameterConfiguration.getList(
            PARAM_RENDITION,
            Collections.<String> emptyList());
        for (String className : renditionProviderClasses) {
            try {
                I_CmsCmisRenditionProvider provider = (I_CmsCmisRenditionProvider)(Class.forName(
                    className).newInstance());
                String id = provider.getId();
                m_renditionProviders.put(id, provider);
            } catch (Throwable e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        List<String> propertyProviderClasses = m_parameterConfiguration.getList(
            PARAM_PROPERTY,
            Collections.<String> emptyList());
        for (String className : propertyProviderClasses) {
            try {
                I_CmsPropertyProvider provider = (I_CmsPropertyProvider)(Class.forName(className).newInstance());
                m_propertyProviders.add(provider);
            } catch (Throwable e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        m_indexName = m_parameterConfiguration.getString(PARAM_INDEX, null);
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#initializeCms(org.opencms.file.CmsObject)
     */
    public void initializeCms(CmsObject cms) throws CmsException {

        m_adminCms = cms;
        m_typeManager = new CmsCmisTypeManager(cms, m_propertyProviders);
        String projectName = m_parameterConfiguration.getString(PARAM_PROJECT, CmsProject.ONLINE_PROJECT_NAME);
        CmsResource root = m_adminCms.readResource("/");
        CmsObject offlineCms = OpenCms.initCmsObject(m_adminCms);
        CmsProject project = m_adminCms.readProject(projectName);
        m_project = project;
        offlineCms.getRequestContext().setCurrentProject(project);
        m_adminCms = offlineCms;
        m_root = root;
        m_isReadOnly = project.isOnlineProject();
    }

    /**
     * @see org.opencms.repository.I_CmsRepository#isTranslationEnabled()
     */
    public boolean isTranslationEnabled() {

        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#moveObject(org.opencms.cmis.CmsCmisCallContext, org.apache.chemistry.opencmis.commons.spi.Holder, java.lang.String, java.lang.String)
     */
    public synchronized void moveObject(
        CmsCmisCallContext context,
        Holder<String> objectId,
        String targetFolderId,
        String sourceFolderId) {

        checkWriteAccess();

        try {
            CmsObject cms = getCmsObject(context);
            CmsUUID structureId = new CmsUUID(objectId.getValue());
            CmsUUID targetStructureId = new CmsUUID(targetFolderId);
            CmsResource targetFolder = cms.readResource(targetStructureId);
            CmsResource resourceToMove = cms.readResource(structureId);
            String name = CmsResource.getName(resourceToMove.getRootPath());
            String newPath = CmsStringUtil.joinPaths(targetFolder.getRootPath(), name);
            boolean wasLocked = ensureLock(cms, resourceToMove);
            try {
                cms.moveResource(resourceToMove.getRootPath(), newPath);
            } finally {
                if (wasLocked) {
                    CmsResource movedResource = cms.readResource(resourceToMove.getStructureId());
                    cms.unlockResource(movedResource);
                }
            }
        } catch (CmsException e) {
            handleCmsException(e);
        }
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#query(org.opencms.cmis.CmsCmisCallContext, java.lang.String, boolean, boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.math.BigInteger, java.math.BigInteger)
     */
    @Override
    public synchronized ObjectList query(
        CmsCmisCallContext context,
        String statement,
        boolean searchAllVersions,
        boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        BigInteger maxItems,
        BigInteger skipCount) {

        try {
            CmsObject cms = getCmsObject(context);
            CmsSolrIndex index = getIndex();
            CmsCmisResourceHelper helper = getResourceHelper();

            // split filter
            Set<String> filterCollection = null;
            // skip and max
            int skip = (skipCount == null ? 0 : skipCount.intValue());
            if (skip < 0) {
                skip = 0;
            }

            int max = (maxItems == null ? Integer.MAX_VALUE : maxItems.intValue());
            if (max < 0) {
                max = Integer.MAX_VALUE;
            }
            CmsSolrResultList results = solrSearch(cms, index, statement, skip, max);
            ObjectListImpl resultObjectList = new ObjectListImpl();
            List<ObjectData> objectDataList = new ArrayList<ObjectData>();
            resultObjectList.setObjects(objectDataList);
            for (CmsResource resource : results) {
                // build and add child object
                objectDataList.add(
                    helper.collectObjectData(
                        context,
                        cms,
                        resource,
                        filterCollection,
                        renditionFilter,
                        includeAllowableActions,
                        false,
                        includeRelationships));
            }
            resultObjectList.setHasMoreItems(Boolean.valueOf(!results.isEmpty()));
            resultObjectList.setNumItems(BigInteger.valueOf(results.getVisibleHitCount()));
            return resultObjectList;
        } catch (CmsException e) {
            handleCmsException(e);
            return null;
        }

    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#setContentStream(org.opencms.cmis.CmsCmisCallContext, org.apache.chemistry.opencmis.commons.spi.Holder, boolean, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.data.ContentStream)
     */
    public synchronized void setContentStream(
        CmsCmisCallContext context,
        Holder<String> objectId,
        boolean overwriteFlag,
        Holder<String> changeToken,
        ContentStream contentStream) {

        checkWriteAccess();

        try {
            CmsObject cms = getCmsObject(context);
            CmsUUID structureId = new CmsUUID(objectId.getValue());
            if (!overwriteFlag) {
                throw new CmisContentAlreadyExistsException();
            }
            CmsResource resource = cms.readResource(structureId);
            if (resource.isFolder()) {
                throw new CmisStreamNotSupportedException("Folders may not have content streams.");
            }
            CmsFile file = cms.readFile(resource);
            InputStream contentInput = contentStream.getStream();
            byte[] newContent = CmsFileUtil.readFully(contentInput);
            file.setContents(newContent);
            boolean wasLocked = ensureLock(cms, resource);
            CmsFile newFile = cms.writeFile(file);
            if (wasLocked) {
                cms.unlockResource(newFile);
            }
        } catch (CmsException e) {
            handleCmsException(e);
        } catch (IOException e) {
            throw new CmisRuntimeException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#setFilter(org.opencms.repository.CmsRepositoryFilter)
     */
    public void setFilter(CmsRepositoryFilter filter) {

        m_filter = filter;
        LOG.warn("Filters not supported by CMIS repositories, ignoring configuration...");
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#setName(java.lang.String)
     */
    public void setName(String name) {

        m_id = name;
    }

    /**
     * @see org.opencms.repository.I_CmsRepository#setTranslation(org.opencms.util.CmsResourceTranslator, boolean)
     */
    public void setTranslation(CmsResourceTranslator translator, boolean enabled) {

        throw new UnsupportedOperationException("File translations not supported by CMIS repository.");
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisRepository#updateProperties(org.opencms.cmis.CmsCmisCallContext, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.data.Properties)
     */
    public synchronized void updateProperties(
        CmsCmisCallContext context,
        Holder<String> objectId,
        Holder<String> changeToken,
        Properties properties) {

        checkWriteAccess();

        try {

            CmsObject cms = getCmsObject(context);
            CmsUUID structureId = new CmsUUID(objectId.getValue());
            CmsResource resource = cms.readResource(structureId);
            Map<String, PropertyData<?>> propertyMap = properties.getProperties();
            List<CmsProperty> cmsProperties = getOpenCmsProperties(propertyMap);
            boolean wasLocked = ensureLock(cms, resource);
            try {
                cms.writePropertyObjects(resource, cmsProperties);
                @SuppressWarnings("unchecked")
                PropertyData<String> nameProperty = (PropertyData<String>)propertyMap.get(PropertyIds.NAME);
                if (nameProperty != null) {
                    String newName = nameProperty.getFirstValue();
                    checkResourceName(newName);
                    String parentFolder = CmsResource.getParentFolder(resource.getRootPath());
                    String newPath = CmsStringUtil.joinPaths(parentFolder, newName);
                    cms.moveResource(resource.getRootPath(), newPath);
                    resource = cms.readResource(resource.getStructureId());
                }

                for (String key : properties.getProperties().keySet()) {
                    if (key.startsWith(CmsCmisTypeManager.PROPERTY_PREFIX_DYNAMIC)) {
                        I_CmsPropertyProvider provider = getTypeManager().getPropertyProvider(key);
                        try {
                            String value = (String)(properties.getProperties().get(key).getFirstValue());
                            provider.setPropertyValue(cms, resource, value);
                        } catch (CmsException e) {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                    }
                }
            } finally {
                if (wasLocked) {
                    cms.unlockResource(resource);
                }
            }
        } catch (CmsException e) {
            handleCmsException(e);
        }
    }

    /**
     * Checks whether we have write access to this repository and throws an exception otherwise.<p>
     */
    protected void checkWriteAccess() {

        if (m_isReadOnly) {
            throw new CmisNotSupportedException("Readonly repository '" + m_id + "' does not allow write operations.");
        }
    }

    /**
     * Initializes a CMS context for the authentication data contained in a call context.<p>
     *
     * @param context the call context
     * @return the initialized CMS context
     */
    protected CmsObject getCmsObject(CmsCmisCallContext context) {

        try {
            if (context.getUsername() == null) {
                // user name can be null
                CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
                cms.getRequestContext().setCurrentProject(m_adminCms.getRequestContext().getCurrentProject());
                return cms;
            } else {
                CmsObject cms = OpenCms.initCmsObject(m_adminCms);
                CmsProject projectBeforeLogin = cms.getRequestContext().getCurrentProject();
                cms.loginUser(context.getUsername(), context.getPassword());
                cms.getRequestContext().setCurrentProject(projectBeforeLogin);
                return cms;
            }
        } catch (CmsException e) {
            throw new CmisPermissionDeniedException(e.getLocalizedMessage(), e);

        }
    }

    /**
     *  Gets the relationship data for a given resource.<p>
     *
     * @param context the call context
     * @param cms the CMS context
     * @param resource the resource
     * @param relationshipDirection the relationship direction
     * @param filterSet the property filter
     * @param includeAllowableActions true if allowable actions should be included
     * @return the list of relationship data
     *
     * @throws CmsException if something goes wrong
     */
    protected List<ObjectData> getRelationshipObjectData(
        CmsCmisCallContext context,
        CmsObject cms,
        CmsResource resource,
        RelationshipDirection relationshipDirection,
        Set<String> filterSet,
        boolean includeAllowableActions)
    throws CmsException {

        List<ObjectData> resultObjects = new ArrayList<ObjectData>();
        CmsRelationFilter relationFilter;
        if (relationshipDirection == RelationshipDirection.SOURCE) {
            relationFilter = CmsRelationFilter.TARGETS;
        } else if (relationshipDirection == RelationshipDirection.TARGET) {
            relationFilter = CmsRelationFilter.SOURCES;
        } else {
            relationFilter = CmsRelationFilter.ALL;
        }

        List<CmsRelation> unfilteredRelations = cms.getRelationsForResource(resource.getRootPath(), relationFilter);
        List<CmsRelation> relations = new ArrayList<CmsRelation>();
        for (CmsRelation relation : unfilteredRelations) {
            if (relation.getTargetId().isNullUUID() || relation.getSourceId().isNullUUID()) {
                continue;
            }
            relations.add(relation);
        }
        CmsCmisRelationHelper helper = getRelationHelper();
        for (CmsRelation relation : relations) {
            ObjectData objData = helper.collectObjectData(
                context,
                cms,
                resource,
                relation,
                filterSet,
                includeAllowableActions,
                false);
            resultObjects.add(objData);
        }
        return resultObjects;
    }

    /**
     * Gets the rendition providers matching the given filter.<p>
     *
     * @param filter the rendition filter
     *
     * @return the rendition providers matching the filter
     */
    protected List<I_CmsCmisRenditionProvider> getRenditionProviders(CmsCmisRenditionFilter filter) {

        List<I_CmsCmisRenditionProvider> result = new ArrayList<I_CmsCmisRenditionProvider>();
        for (I_CmsCmisRenditionProvider provider : m_renditionProviders.values()) {
            String mimetype = provider.getMimeType();
            String kind = provider.getKind();
            if (filter.accept(kind, mimetype)) {
                result.add(provider);
            }
        }
        return result;
    }

    /**
     * Extracts the resource type from a set of CMIS properties.<p>
     *
     * @param properties the CMIS properties
     * @param defaultValue the default value
     *
     * @return the resource type property, or the default value if the property was not found
     */
    protected String getResourceTypeFromProperties(Map<String, PropertyData<?>> properties, String defaultValue) {

        PropertyData<?> typeProp = properties.get(CmsCmisTypeManager.PROPERTY_RESOURCE_TYPE);
        String resTypeName = defaultValue;
        if (typeProp != null) {
            resTypeName = (String)typeProp.getFirstValue();
        }
        return resTypeName;
    }

    /**
     * Gets the type manager instance.<p>
     *
     * @return the type manager instance
     */
    protected CmsCmisTypeManager getTypeManager() {

        return m_typeManager;
    }

    /**
     * Gets the correct helper object for a given object id to perform operations on the corresponding object.<p>
     *
     * @param objectId the object id
     *
     * @return the helper object to use for the given object id
     */
    I_CmsCmisObjectHelper getHelper(String objectId) {

        if (CmsUUID.isValidUUID(objectId)) {
            return getResourceHelper();
        } else if (CmsCmisRelationHelper.RELATION_PATTERN.matcher(objectId).matches()) {
            return getRelationHelper();
        } else {
            return null;
        }
    }

    /**
     * Helper method for executing a query.<p>
     *
     * @param cms the CMS context to use
     * @param index the index to use for the query
     * @param query the query to perform
     * @param start the start offset
     * @param rows the number of results to return
     *
     * @return the list of search results
     * @throws CmsSearchException if something goes wrong
     */
    CmsSolrResultList solrSearch(CmsObject cms, CmsSolrIndex index, String query, int start, int rows)
    throws CmsSearchException {

        Map<String, String[]> params = new HashMap<>();
        CmsSolrQuery q = new CmsSolrQuery(null, params);
        q.setText(query);
        q.setStart(Integer.valueOf(start));
        q.setRows(Integer.valueOf(rows));
        CmsSolrResultList resultPage = index.search(cms, q, true);
        return resultPage;
    }

    /**
     * Helper method to collect the descendants of a given folder.<p>
     *
     * @param context the call context
     * @param cms the CMS context
     * @param folder the parent folder
     * @param list the list to which the descendants should be added
     * @param foldersOnly flag to exclude files from the result
     * @param depth the maximum depth
     * @param filter the property filter
     * @param includeAllowableActions flag to include allowable actions
     * @param includePathSegments flag to include path segments
     */
    private void gatherDescendants(
        CmsCmisCallContext context,
        CmsObject cms,
        CmsResource folder,
        List<ObjectInFolderContainer> list,
        boolean foldersOnly,
        int depth,
        Set<String> filter,
        boolean includeAllowableActions,
        boolean includePathSegments) {

        try {
            CmsCmisResourceHelper helper = getResourceHelper();
            List<CmsResource> children = cms.getResourcesInFolder(cms.getSitePath(folder), CmsResourceFilter.DEFAULT);
            Collections.sort(children, new Comparator<CmsResource>() {

                public int compare(CmsResource a, CmsResource b) {

                    return a.getName().compareTo(b.getName());
                }
            });
            // iterate through children
            for (CmsResource child : children) {

                // folders only?
                if (foldersOnly && !child.isFolder()) {
                    continue;
                }

                // add to list
                ObjectInFolderDataImpl objectInFolder = new ObjectInFolderDataImpl();
                objectInFolder.setObject(
                    helper.collectObjectData(
                        context,
                        cms,
                        child,
                        filter,
                        "cmis:none",
                        includeAllowableActions,
                        false,
                        IncludeRelationships.NONE));
                if (includePathSegments) {
                    objectInFolder.setPathSegment(child.getName());
                }

                ObjectInFolderContainerImpl container = new ObjectInFolderContainerImpl();
                container.setObject(objectInFolder);

                list.add(container);

                // move to next level
                if ((depth != 1) && child.isFolder()) {
                    container.setChildren(new ArrayList<ObjectInFolderContainer>());
                    gatherDescendants(
                        context,
                        cms,
                        child,
                        container.getChildren(),
                        foldersOnly,
                        depth - 1,
                        filter,
                        includeAllowableActions,
                        includePathSegments);
                }
            }
        } catch (CmsException e) {
            handleCmsException(e);
        }
    }

    /**
     * Gets the index to use for queries.<p>
     *
     * @return the index to use for queries
     */
    private CmsSolrIndex getIndex() {

        String indexName = m_indexName;
        if (indexName == null) {
            return null;
        }
        return OpenCms.getSearchManager().getIndexSolr(indexName);
    }

    /**
     * Gets the relation object helper.<p>
     *
     * @return the relation object helper
     */
    private CmsCmisRelationHelper getRelationHelper() {

        return m_relationHelper;
    }

    /**
     * Gets the resource object helper.<p>
     *
     * @return the resource object helper
     */
    private CmsCmisResourceHelper getResourceHelper() {

        return m_resourceHelper;
    }

}

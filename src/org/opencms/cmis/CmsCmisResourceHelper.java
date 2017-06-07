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

import static org.opencms.cmis.CmsCmisUtil.addAction;
import static org.opencms.cmis.CmsCmisUtil.addPropertyBoolean;
import static org.opencms.cmis.CmsCmisUtil.addPropertyDateTime;
import static org.opencms.cmis.CmsCmisUtil.addPropertyId;
import static org.opencms.cmis.CmsCmisUtil.addPropertyIdList;
import static org.opencms.cmis.CmsCmisUtil.addPropertyInteger;
import static org.opencms.cmis.CmsCmisUtil.addPropertyString;
import static org.opencms.cmis.CmsCmisUtil.ensureLock;
import static org.opencms.cmis.CmsCmisUtil.getAcePrincipalName;
import static org.opencms.cmis.CmsCmisUtil.getCmisPermissions;
import static org.opencms.cmis.CmsCmisUtil.getNativePermissions;
import static org.opencms.cmis.CmsCmisUtil.handleCmsException;
import static org.opencms.cmis.CmsCmisUtil.hasChildren;
import static org.opencms.cmis.CmsCmisUtil.millisToCalendar;
import static org.opencms.cmis.CmsCmisUtil.splitFilter;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsResourceManager;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AllowableActionsImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.commons.impl.server.RenditionInfoImpl;
import org.apache.chemistry.opencmis.commons.server.RenditionInfo;

/**
 * Helper class for CRUD operations on resources.<p>
 */
public class CmsCmisResourceHelper implements I_CmsCmisObjectHelper {

    /** The underlying repository. */
    private CmsCmisRepository m_repository;

    /**
     * Creates a new instance.<p>
     *
     * @param repository the underlying repository
     */
    public CmsCmisResourceHelper(CmsCmisRepository repository) {

        m_repository = repository;
    }

    /**
     * Deletes a CMIS object.<p>
     *
     * @param context the call context
     * @param objectId the id of the object to delete
     * @param allVersions flag to delete all version
     */
    public synchronized void deleteObject(CmsCmisCallContext context, String objectId, boolean allVersions) {

        try {
            CmsObject cms = m_repository.getCmsObject(context);
            CmsUUID structureId = new CmsUUID(objectId);
            CmsResource resource = cms.readResource(structureId);
            if (resource.isFolder()) {
                boolean isLeaf = !hasChildren(cms, resource);
                if (!isLeaf) {
                    throw new CmisConstraintException("Only leaf resources can be deleted.");
                }
            }
            ensureLock(cms, resource);
            cms.deleteResource(resource.getRootPath(), CmsResource.DELETE_PRESERVE_SIBLINGS);
        } catch (CmsException e) {
            handleCmsException(e);
        }
    }

    /**
     * Gets the ACL for an object.<p>
     *
     * @param context the call context
     * @param objectId the object id
     * @param onlyBasicPermissions flag to only get basic permissions
     *
     * @return the ACL for the object
     */
    public synchronized Acl getAcl(CmsCmisCallContext context, String objectId, boolean onlyBasicPermissions) {

        try {

            CmsObject cms = m_repository.getCmsObject(context);
            CmsUUID structureId = new CmsUUID(objectId);
            CmsResource resource = cms.readResource(structureId);
            return collectAcl(cms, resource, onlyBasicPermissions);
        } catch (CmsException e) {
            handleCmsException(e);
            return null;
        }

    }

    /**
     * Gets the allowable actions for an object.<p>
     *
     * @param context the call context
     * @param objectId the object id
     * @return the allowable actions
     */
    public synchronized AllowableActions getAllowableActions(CmsCmisCallContext context, String objectId) {

        try {
            CmsObject cms = m_repository.getCmsObject(context);
            CmsUUID structureId = new CmsUUID(objectId);
            CmsResource file = cms.readResource(structureId);
            return collectAllowableActions(cms, file);
        } catch (CmsException e) {
            handleCmsException(e);
            return null;
        }
    }

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
    public synchronized ObjectData getObject(
        CmsCmisCallContext context,
        String objectId,
        String filter,
        boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        boolean includePolicyIds,
        boolean includeAcl) {

        try {
            //            if (renditionFilter.equals("cmis:none") && context.isObjectInfoRequired()) {
            //                renditionFilter = "*";
            //            }
            // check id
            if (objectId == null) {
                throw new CmisInvalidArgumentException("Object Id must be set.");
            }
            CmsObject cms = m_repository.getCmsObject(context);
            // get the file or folder
            CmsResource file = cms.readResource(new CmsUUID(objectId));

            // split filter
            Set<String> filterCollection = splitFilter(filter);

            // gather properties
            return collectObjectData(
                context,
                cms,
                file,
                filterCollection,
                renditionFilter,
                includeAllowableActions,
                includeAcl,
                includeRelationships);
        } catch (CmsException e) {
            handleCmsException(e);
            return null;
        }
    }

    /**
     * Compiles the ACL for a file or folder.
     * @param cms the CMS context
     * @param resource the resource for which to collect the ACLs
     * @param onlyBasic flag to only include basic ACEs
     *
     * @return the ACL for the resource
     * @throws CmsException if something goes wrong
     */
    protected Acl collectAcl(CmsObject cms, CmsResource resource, boolean onlyBasic) throws CmsException {

        AccessControlListImpl cmisAcl = new AccessControlListImpl();
        List<Ace> cmisAces = new ArrayList<Ace>();
        List<CmsAccessControlEntry> aces = cms.getAccessControlEntries(resource.getRootPath(), true);
        for (CmsAccessControlEntry ace : aces) {
            boolean isDirect = ace.getResource().equals(resource.getResourceId());
            CmsUUID principalId = ace.getPrincipal();
            String principalName = getAcePrincipalName(cms, principalId);
            AccessControlEntryImpl cmisAce = new AccessControlEntryImpl();
            AccessControlPrincipalDataImpl cmisPrincipal = new AccessControlPrincipalDataImpl();
            cmisPrincipal.setId(principalName);
            cmisAce.setPrincipal(cmisPrincipal);
            cmisAce.setPermissions(onlyBasic ? getCmisPermissions(ace) : getNativePermissions(ace));
            cmisAce.setDirect(isDirect);
            cmisAces.add(cmisAce);
        }
        cmisAcl.setAces(cmisAces);
        cmisAcl.setExact(Boolean.FALSE);
        return cmisAcl;
    }

    /**
     * Compiles the allowable actions for a file or folder.
     *
     * @param cms the current CMS context
     * @param file the resource for which we want the allowable actions
     *
     * @return the allowable actions for the given resource
     */
    protected AllowableActions collectAllowableActions(CmsObject cms, CmsResource file) {

        try {

            if (file == null) {
                throw new IllegalArgumentException("File must not be null!");
            }
            CmsLock lock = cms.getLock(file);
            CmsUser user = cms.getRequestContext().getCurrentUser();
            boolean canWrite = !cms.getRequestContext().getCurrentProject().isOnlineProject()
                && (lock.isOwnedBy(user) || lock.isLockableBy(user))
                && cms.hasPermissions(file, CmsPermissionSet.ACCESS_WRITE, false, CmsResourceFilter.DEFAULT);
            boolean isReadOnly = !canWrite;
            boolean isFolder = file.isFolder();
            boolean isRoot = file.getRootPath().length() <= 1;

            Set<Action> aas = new LinkedHashSet<Action>();
            addAction(aas, Action.CAN_GET_OBJECT_PARENTS, !isRoot);
            addAction(aas, Action.CAN_GET_PROPERTIES, true);
            addAction(aas, Action.CAN_UPDATE_PROPERTIES, !isReadOnly);
            addAction(aas, Action.CAN_MOVE_OBJECT, !isReadOnly && !isRoot);
            addAction(aas, Action.CAN_DELETE_OBJECT, !isReadOnly && !isRoot);
            if (isFolder) {
                addAction(aas, Action.CAN_GET_DESCENDANTS, true);
                addAction(aas, Action.CAN_GET_CHILDREN, true);
                addAction(aas, Action.CAN_GET_FOLDER_PARENT, !isRoot);
                addAction(aas, Action.CAN_GET_FOLDER_TREE, true);
                addAction(aas, Action.CAN_CREATE_DOCUMENT, !isReadOnly);
                addAction(aas, Action.CAN_CREATE_FOLDER, !isReadOnly);
                addAction(aas, Action.CAN_DELETE_TREE, !isReadOnly);
            } else {
                addAction(aas, Action.CAN_GET_CONTENT_STREAM, true);
                addAction(aas, Action.CAN_SET_CONTENT_STREAM, !isReadOnly);
                addAction(aas, Action.CAN_GET_ALL_VERSIONS, true);
            }
            AllowableActionsImpl result = new AllowableActionsImpl();
            result.setAllowableActions(aas);
            return result;
        } catch (CmsException e) {
            handleCmsException(e);
            return null;
        }
    }

    /**
     * Fills in an ObjectData record.<p>
     *
     * @param context the call context
     * @param cms the CMS context
     * @param resource the resource for which we want the ObjectData
     * @param filter the property filter
     * @param renditionFilter the rendition filter string
     * @param includeAllowableActions true if the allowable actions should be included
     * @param includeAcl true if the ACL entries should be included
     * @param includeRelationships true if relationships should be included
     *
     * @return the object data
     * @throws CmsException if something goes wrong
     */
    protected ObjectData collectObjectData(
        CmsCmisCallContext context,
        CmsObject cms,
        CmsResource resource,
        Set<String> filter,
        String renditionFilter,
        boolean includeAllowableActions,
        boolean includeAcl,
        IncludeRelationships includeRelationships)
    throws CmsException {

        ObjectDataImpl result = new ObjectDataImpl();
        ObjectInfoImpl objectInfo = new ObjectInfoImpl();
        result.setProperties(collectProperties(cms, resource, filter, objectInfo));

        if (includeAllowableActions) {
            result.setAllowableActions(collectAllowableActions(cms, resource));
        }

        if (includeAcl) {
            result.setAcl(collectAcl(cms, resource, true));
            result.setIsExactAcl(Boolean.FALSE);
        }

        if ((includeRelationships != null) && (includeRelationships != IncludeRelationships.NONE)) {
            RelationshipDirection direction;
            if (includeRelationships == IncludeRelationships.SOURCE) {
                direction = RelationshipDirection.SOURCE;
            } else if (includeRelationships == IncludeRelationships.TARGET) {
                direction = RelationshipDirection.TARGET;
            } else {
                direction = RelationshipDirection.EITHER;
            }

            List<ObjectData> relationData = m_repository.getRelationshipObjectData(
                context,
                cms,
                resource,
                direction,
                CmsCmisUtil.splitFilter("*"),
                false);
            result.setRelationships(relationData);
        }

        result.setRenditions(collectRenditions(cms, resource, renditionFilter, objectInfo));

        if (context.isObjectInfoRequired()) {
            objectInfo.setObject(result);
            context.getObjectInfoHandler().addObjectInfo(objectInfo);
        }
        return result;
    }

    /**
     * Gathers all base properties of a file or folder.
     *
     * @param cms the current CMS context
     * @param resource the file for which we want the properties
     * @param orgfilter the property filter
     * @param objectInfo the object info handler
     *
     * @return the properties for the given resource
     */
    protected Properties collectProperties(
        CmsObject cms,
        CmsResource resource,
        Set<String> orgfilter,
        ObjectInfoImpl objectInfo) {

        CmsCmisTypeManager tm = m_repository.getTypeManager();

        if (resource == null) {
            throw new IllegalArgumentException("Resource may not be null.");
        }

        // copy filter
        Set<String> filter = (orgfilter == null ? null : new LinkedHashSet<String>(orgfilter));

        // find base type
        String typeId = null;

        List<String> relationSourceIds = new ArrayList<String>();
        List<String> relationTargetIds = new ArrayList<String>();
        try {
            List<CmsRelation> relations = cms.getRelationsForResource(resource, CmsRelationFilter.ALL);
            for (CmsRelation relation : relations) {
                if (resource.getStructureId().equals(relation.getSourceId())) {
                    relationTargetIds.add(relation.getTargetId().toString());
                }
                if (resource.getStructureId().equals(relation.getTargetId())) {
                    relationSourceIds.add(relation.getSourceId().toString());
                }
            }
        } catch (CmsException e) {
            throw new CmisStorageException(e.getLocalizedMessage(), e);
        }

        if (resource.isFolder()) {
            typeId = CmsCmisTypeManager.FOLDER_TYPE_ID;
            objectInfo.setBaseType(BaseTypeId.CMIS_FOLDER);
            objectInfo.setTypeId(typeId);
            objectInfo.setContentType(null);
            objectInfo.setFileName(null);
            objectInfo.setHasAcl(true);
            objectInfo.setHasContent(false);
            objectInfo.setVersionSeriesId(null);
            objectInfo.setIsCurrentVersion(true);
            objectInfo.setRelationshipSourceIds(relationSourceIds);
            objectInfo.setRelationshipTargetIds(relationTargetIds);
            objectInfo.setSupportsDescendants(true);
            objectInfo.setSupportsFolderTree(true);
            objectInfo.setSupportsPolicies(false);
            objectInfo.setSupportsRelationships(true);
            objectInfo.setWorkingCopyId(null);
            objectInfo.setWorkingCopyOriginalId(null);
        } else {
            typeId = CmsCmisTypeManager.DOCUMENT_TYPE_ID;
            objectInfo.setBaseType(BaseTypeId.CMIS_DOCUMENT);
            objectInfo.setTypeId(typeId);
            objectInfo.setHasAcl(true);
            objectInfo.setHasContent(true);
            objectInfo.setHasParent(true);
            objectInfo.setVersionSeriesId(null);
            objectInfo.setIsCurrentVersion(true);
            objectInfo.setRelationshipSourceIds(relationSourceIds);
            objectInfo.setRelationshipTargetIds(relationTargetIds);
            objectInfo.setSupportsDescendants(false);
            objectInfo.setSupportsFolderTree(false);
            objectInfo.setSupportsPolicies(false);
            objectInfo.setSupportsRelationships(true);
            objectInfo.setWorkingCopyId(null);
            objectInfo.setWorkingCopyOriginalId(null);
        }
        try {
            PropertiesImpl result = new PropertiesImpl();

            String id = resource.getStructureId().toString();
            addPropertyId(tm, result, typeId, filter, PropertyIds.OBJECT_ID, id);
            objectInfo.setId(id);

            String name = resource.getName();
            if ("".equals(name)) {
                name = "/";
            }
            addPropertyString(tm, result, typeId, filter, PropertyIds.NAME, name);
            objectInfo.setName(name);

            // created and modified by
            CmsUUID creatorId = resource.getUserCreated();
            CmsUUID modifierId = resource.getUserLastModified();
            String creatorName = creatorId.toString();
            String modifierName = modifierId.toString();
            try {
                CmsUser user = cms.readUser(creatorId);
                creatorName = user.getName();
            } catch (CmsException e) {
                // ignore, use id as name
            }
            try {
                CmsUser user = cms.readUser(modifierId);
                modifierName = user.getName();
            } catch (CmsException e) {
                // ignore, use id as name
            }

            addPropertyString(tm, result, typeId, filter, PropertyIds.CREATED_BY, creatorName);
            addPropertyString(tm, result, typeId, filter, PropertyIds.LAST_MODIFIED_BY, modifierName);
            objectInfo.setCreatedBy(creatorName);

            // creation and modification date
            GregorianCalendar lastModified = millisToCalendar(resource.getDateLastModified());
            GregorianCalendar created = millisToCalendar(resource.getDateCreated());

            addPropertyDateTime(tm, result, typeId, filter, PropertyIds.CREATION_DATE, created);
            addPropertyDateTime(tm, result, typeId, filter, PropertyIds.LAST_MODIFICATION_DATE, lastModified);
            objectInfo.setCreationDate(created);
            objectInfo.setLastModificationDate(lastModified);

            // change token - always null
            addPropertyString(tm, result, typeId, filter, PropertyIds.CHANGE_TOKEN, null);

            // directory or file
            if (resource.isFolder()) {
                // base type and type name
                addPropertyId(tm, result, typeId, filter, PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_FOLDER.value());
                addPropertyId(
                    tm,
                    result,
                    typeId,
                    filter,
                    PropertyIds.OBJECT_TYPE_ID,
                    CmsCmisTypeManager.FOLDER_TYPE_ID);
                String path = resource.getRootPath();
                addPropertyString(tm, result, typeId, filter, PropertyIds.PATH, (path.length() == 0 ? "/" : path));

                // folder properties
                if (resource.getRootPath().length() > 1) {
                    CmsResource parent = cms.readParentFolder(resource.getStructureId());
                    addPropertyId(tm, result, typeId, filter, PropertyIds.PARENT_ID, (

                    parent.getStructureId().toString()));
                    objectInfo.setHasParent(true);
                } else {
                    addPropertyId(tm, result, typeId, filter, PropertyIds.PARENT_ID, null);
                    objectInfo.setHasParent(false);
                }

                addPropertyIdList(tm, result, typeId, filter, PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, null);
            } else {
                // base type and type name
                addPropertyId(tm, result, typeId, filter, PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value());
                addPropertyId(
                    tm,
                    result,
                    typeId,
                    filter,
                    PropertyIds.OBJECT_TYPE_ID,
                    CmsCmisTypeManager.DOCUMENT_TYPE_ID);

                // file properties
                addPropertyBoolean(tm, result, typeId, filter, PropertyIds.IS_IMMUTABLE, false);
                addPropertyBoolean(tm, result, typeId, filter, PropertyIds.IS_LATEST_VERSION, true);
                addPropertyBoolean(tm, result, typeId, filter, PropertyIds.IS_MAJOR_VERSION, true);
                addPropertyBoolean(tm, result, typeId, filter, PropertyIds.IS_LATEST_MAJOR_VERSION, true);
                addPropertyString(tm, result, typeId, filter, PropertyIds.VERSION_LABEL, resource.getName());
                addPropertyId(
                    tm,
                    result,
                    typeId,
                    filter,
                    PropertyIds.VERSION_SERIES_ID,
                    resource.getStructureId().toString());
                addPropertyBoolean(tm, result, typeId, filter, PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, false);
                addPropertyString(tm, result, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, null);
                addPropertyString(tm, result, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, null);
                addPropertyString(tm, result, typeId, filter, PropertyIds.CHECKIN_COMMENT, "");
                addPropertyInteger(tm, result, typeId, filter, PropertyIds.CONTENT_STREAM_LENGTH, resource.getLength());
                addPropertyString(
                    tm,
                    result,
                    typeId,
                    filter,
                    PropertyIds.CONTENT_STREAM_MIME_TYPE,
                    OpenCms.getResourceManager().getMimeType(
                        resource.getRootPath(),
                        null,
                        CmsResourceManager.MIMETYPE_TEXT));
                addPropertyString(tm, result, typeId, filter, PropertyIds.CONTENT_STREAM_FILE_NAME, resource.getName());
                objectInfo.setHasContent(true);
                objectInfo.setContentType(
                    OpenCms.getResourceManager().getMimeType(
                        resource.getRootPath(),
                        null,
                        CmsResourceManager.MIMETYPE_TEXT));
                objectInfo.setFileName(resource.getName());
                addPropertyId(tm, result, typeId, filter, PropertyIds.CONTENT_STREAM_ID, null);
            }
            // normal OpenCms properties
            List<CmsProperty> props = cms.readPropertyObjects(resource, false);
            Set<String> propertiesToAdd = new LinkedHashSet<String>(
                m_repository.getTypeManager().getCmsPropertyNames());
            for (CmsProperty prop : props) {
                addPropertyString(
                    tm,
                    result,
                    typeId,
                    filter,
                    CmsCmisTypeManager.PROPERTY_PREFIX + prop.getName(),
                    prop.getValue());
                propertiesToAdd.remove(prop.getName());
            }
            for (String propName : propertiesToAdd) {
                addPropertyString(tm, result, typeId, filter, CmsCmisTypeManager.PROPERTY_PREFIX + propName, null);
            }

            // inherited OpenCms properties
            List<CmsProperty> inheritedProps = cms.readPropertyObjects(resource, true);
            Set<String> inheritedPropertiesToAdd = new LinkedHashSet<String>(
                m_repository.getTypeManager().getCmsPropertyNames());
            for (CmsProperty prop : inheritedProps) {
                addPropertyString(
                    tm,
                    result,
                    typeId,
                    filter,
                    CmsCmisTypeManager.INHERITED_PREFIX + prop.getName(),
                    prop.getValue());
                inheritedPropertiesToAdd.remove(prop.getName());
            }
            for (String propName : inheritedPropertiesToAdd) {
                addPropertyString(tm, result, typeId, filter, CmsCmisTypeManager.INHERITED_PREFIX + propName, null);
            }

            I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(resource);
            addPropertyString(
                tm,
                result,
                typeId,
                filter,
                CmsCmisTypeManager.PROPERTY_RESOURCE_TYPE,
                resType.getTypeName());
            CmsCmisUtil.addDynamicProperties(cms, m_repository.getTypeManager(), result, typeId, resource, filter);
            return result;

        } catch (Exception e) {
            if (e instanceof CmisBaseException) {
                throw (CmisBaseException)e;
            }
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Collects renditions for a resource.<p>
     *
     * @param cms the CMS context
     * @param resource the resource for which we want the renditions
     * @param renditionFilterString the filter string for the renditions
     * @param objectInfo the object info in which the renditions should be saved
     *
     * @return the rendition data for the given resource
     */
    protected List<RenditionData> collectRenditions(
        CmsObject cms,
        CmsResource resource,
        String renditionFilterString,
        ObjectInfoImpl objectInfo) {

        List<I_CmsCmisRenditionProvider> providers = m_repository.getRenditionProviders(
            new CmsCmisRenditionFilter(renditionFilterString));
        List<RenditionData> result = new ArrayList<RenditionData>();
        List<RenditionInfo> renditionInfos = new ArrayList<RenditionInfo>();
        for (I_CmsCmisRenditionProvider provider : providers) {
            RenditionData renditionData = provider.getRendition(cms, resource);
            if (renditionData != null) {
                RenditionInfoImpl renditionInfo = new RenditionInfoImpl();
                renditionInfo.setContentType(renditionData.getMimeType());
                renditionInfo.setKind(renditionData.getKind());
                renditionInfo.setId(renditionData.getStreamId());
                result.add(renditionData);
                renditionInfos.add(renditionInfo);
            }
        }
        if (objectInfo != null) {
            objectInfo.setRenditionInfos(renditionInfos);
        }
        return result;

    }

}

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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsResourceManager;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.CapabilityRenditions;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.SupportedPermissions;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AclCapabilitiesDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AllowableActionsImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FailedToDeleteDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderContainerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectParentDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionDefinitionDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionMappingDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyHtmlImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RenditionDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryCapabilitiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryInfoImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeDefinitionListImpl;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.fileshare.TypeManager;
import org.apache.commons.logging.Log;

/**
 * Repository instance for CMIS repositories.<p>
 */
public class CmsCmisRepository extends A_CmsCmisRepository {

    /** The repository id. */
    private String m_id;

    /** The root folder. */
    private CmsResource m_root;

    /** The type manager. */
    private CmsCmisTypeManager m_typeManager;

    /** The internal admin CMS context. */
    private CmsObject m_adminCms;

    /** The logger instance for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsCmisRepository.class);

    /**
     * Gets the repository id.<p>
     * 
     * @return the repository id 
     */
    public String getId() {

        return m_id;
    }

    /**
     * Creates a new CMIS repository instance.<p>
     * 
     * @param typeManager the type manager
     * @param adminCms the admin CMS context
     * @param root the root folder resource 
     * @param id the repository id 
     * @param readonly true if this is a readonly repository
     */
    public CmsCmisRepository(
        CmsCmisTypeManager typeManager,
        CmsObject adminCms,
        CmsResource root,
        String id,
        boolean readonly) {

        m_adminCms = adminCms;
        m_id = id;
        m_root = root;
        m_typeManager = typeManager;
        m_isReadOnly = readonly;
    }

    /**
     * Initializes a CMS context for the authentication data contained in a call context.<p>
     * 
     * @param context the call context
     * @return the initialized CMS context 
     * 
     * @throws CmsException if something goes wrong 
     */
    protected CmsObject getCmsObject(CallContext context) throws CmsException {

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
    }

    /**
     * Fills in an ObjectData record.<p>
     * 
     * @param context the call context
     * @param cms the CMS context
     * @param resource the resource for which we want the ObjectData
     * @param filter the property filter string 
     * @param includeAllowableActions true if the allowable actions should be included  
     * @param includeAcl true if the ACL entries should be included
     * @param objectInfos the object info handler
     * 
     * @return the object data 
     * @throws CmsException if something goes wrong 
     */
    private ObjectData collectObjectData(
        CallContext context,
        CmsObject cms,
        CmsResource resource,
        Set<String> filter,
        boolean includeAllowableActions,
        boolean includeAcl,
        ObjectInfoHandler objectInfos) throws CmsException {

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

        if (context.isObjectInfoRequired()) {
            objectInfo.setObject(result);
            objectInfos.addObjectInfo(objectInfo);
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
    private Properties collectProperties(
        CmsObject cms,
        CmsResource resource,
        Set<String> orgfilter,
        ObjectInfoImpl objectInfo) {

        if (resource == null) {
            throw new IllegalArgumentException("Resource may not be null.");
        }

        // copy filter
        Set<String> filter = (orgfilter == null ? null : new LinkedHashSet<String>(orgfilter));

        // find base type
        String typeId = null;

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
            objectInfo.setRelationshipSourceIds(null);
            objectInfo.setRelationshipTargetIds(null);
            objectInfo.setRenditionInfos(null);
            objectInfo.setSupportsDescendants(true);
            objectInfo.setSupportsFolderTree(true);
            objectInfo.setSupportsPolicies(false);
            objectInfo.setSupportsRelationships(false);
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
            objectInfo.setRelationshipSourceIds(null);
            objectInfo.setRelationshipTargetIds(null);
            objectInfo.setRenditionInfos(null);
            objectInfo.setSupportsDescendants(false);
            objectInfo.setSupportsFolderTree(false);
            objectInfo.setSupportsPolicies(false);
            objectInfo.setSupportsRelationships(false);
            objectInfo.setWorkingCopyId(null);
            objectInfo.setWorkingCopyOriginalId(null);
        }

        // let's do it
        try {
            PropertiesImpl result = new PropertiesImpl();

            // id
            String id = resource.getStructureId().toString();
            addPropertyId(result, typeId, filter, PropertyIds.OBJECT_ID, id);
            objectInfo.setId(id);

            // name
            String name = resource.getName();
            addPropertyString(result, typeId, filter, PropertyIds.NAME, name);
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

            addPropertyString(result, typeId, filter, PropertyIds.CREATED_BY, creatorName);
            addPropertyString(result, typeId, filter, PropertyIds.LAST_MODIFIED_BY, modifierName);
            objectInfo.setCreatedBy(creatorName);

            // creation and modification date
            GregorianCalendar lastModified = millisToCalendar(resource.getDateLastModified());
            GregorianCalendar created = millisToCalendar(resource.getDateCreated());

            addPropertyDateTime(result, typeId, filter, PropertyIds.CREATION_DATE, created);
            addPropertyDateTime(result, typeId, filter, PropertyIds.LAST_MODIFICATION_DATE, lastModified);
            objectInfo.setCreationDate(created);
            objectInfo.setLastModificationDate(lastModified);

            // change token - always null
            addPropertyString(result, typeId, filter, PropertyIds.CHANGE_TOKEN, null);

            // directory or file
            if (resource.isFolder()) {
                // base type and type name
                addPropertyId(result, typeId, filter, PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_FOLDER.value());
                addPropertyId(result, typeId, filter, PropertyIds.OBJECT_TYPE_ID, TypeManager.FOLDER_TYPE_ID);
                String path = resource.getRootPath();
                addPropertyString(result, typeId, filter, PropertyIds.PATH, (path.length() == 0 ? "/" : path));

                // folder properties
                if (!m_root.equals(resource)) {
                    CmsResource parent = cms.readParentFolder(resource.getStructureId());
                    addPropertyId(result, typeId, filter, PropertyIds.PARENT_ID, (m_root.equals(parent)
                    ? m_root.getStructureId().toString()
                    : parent.getStructureId().toString()));
                    objectInfo.setHasParent(true);
                } else {
                    addPropertyId(result, typeId, filter, PropertyIds.PARENT_ID, null);
                    objectInfo.setHasParent(false);
                }

                addPropertyIdList(result, typeId, filter, PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, null);
            } else {
                // base type and type name
                addPropertyId(result, typeId, filter, PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value());
                addPropertyId(result, typeId, filter, PropertyIds.OBJECT_TYPE_ID, TypeManager.DOCUMENT_TYPE_ID);

                // file properties
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_IMMUTABLE, false);
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_LATEST_VERSION, true);
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_MAJOR_VERSION, true);
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_LATEST_MAJOR_VERSION, true);
                addPropertyString(result, typeId, filter, PropertyIds.VERSION_LABEL, resource.getName());
                addPropertyId(
                    result,
                    typeId,
                    filter,
                    PropertyIds.VERSION_SERIES_ID,
                    resource.getStructureId().toString());
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, false);
                addPropertyString(result, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, null);
                addPropertyString(result, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, null);
                addPropertyString(result, typeId, filter, PropertyIds.CHECKIN_COMMENT, "");
                addPropertyInteger(result, typeId, filter, PropertyIds.CONTENT_STREAM_LENGTH, resource.getLength());
                addPropertyString(
                    result,
                    typeId,
                    filter,
                    PropertyIds.CONTENT_STREAM_MIME_TYPE,
                    OpenCms.getResourceManager().getMimeType(
                        resource.getRootPath(),
                        null,
                        CmsResourceManager.MIMETYPE_TEXT));
                addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_FILE_NAME, resource.getName());
                objectInfo.setHasContent(true);
                objectInfo.setContentType(OpenCms.getResourceManager().getMimeType(
                    resource.getRootPath(),
                    null,
                    CmsResourceManager.MIMETYPE_TEXT));
                objectInfo.setFileName(resource.getName());
                addPropertyId(result, typeId, filter, PropertyIds.CONTENT_STREAM_ID, null);
            }

            List<CmsProperty> props = cms.readPropertyObjects(resource, false);
            Set<String> propertiesToAdd = new LinkedHashSet<String>(m_typeManager.getCmsPropertyNames());
            for (CmsProperty prop : props) {
                addPropertyString(
                    result,
                    typeId,
                    filter,
                    CmsCmisTypeManager.PROPERTY_PREFIX + prop.getName(),
                    prop.getValue());
                propertiesToAdd.remove(prop.getName());
            }
            for (String propName : propertiesToAdd) {
                addPropertyString(result, typeId, filter, CmsCmisTypeManager.PROPERTY_PREFIX + propName, null);
            }
            I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(resource);
            addPropertyString(result, typeId, filter, CmsCmisTypeManager.PROPERTY_RESOURCE_TYPE, resType.getTypeName());
            return result;
        } catch (Exception e) {
            if (e instanceof CmisBaseException) {
                throw (CmisBaseException)e;
            }
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Converts milliseconds into a calendar object.
     * 
     * @param millis a time given in milliseconds after epoch 
     * @return the calendar object for the given time 
     */
    private static GregorianCalendar millisToCalendar(long millis) {

        GregorianCalendar result = new GregorianCalendar();
        result.setTimeZone(TimeZone.getTimeZone("GMT"));
        result.setTimeInMillis((long)(Math.ceil(millis / 1000) * 1000));
        return result;
    }

    /**
     * Compiles the allowable actions for a file or folder.
     *  
     * @param cms the current CMS context 
     * @param file the resource for which we want the allowable actions 
     * 
     * @return the allowable actions for the given resource 
     */
    private AllowableActions collectAllowableActions(CmsObject cms, CmsResource file) {

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
            boolean isRoot = m_root.equals(file);

            Set<Action> aas = new LinkedHashSet<Action>();
            addAction(aas, Action.CAN_GET_OBJECT_PARENTS, !isRoot);
            addAction(aas, Action.CAN_GET_PROPERTIES, true);
            addAction(aas, Action.CAN_UPDATE_PROPERTIES, !isReadOnly);
            addAction(aas, Action.CAN_MOVE_OBJECT, !isReadOnly);
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
     * Wrap OpenCms into OpenCMIS exceptions and rethrow them.<p>
     * 
     * @param e the exception to handle
     */
    protected void handleCmsException(CmsException e) {

        if (e instanceof CmsVfsResourceNotFoundException) {
            throw new CmisObjectNotFoundException(e.getLocalizedMessage(), e);
        } else if (e instanceof CmsSecurityException) {
            throw new CmisUnauthorizedException(e.getLocalizedMessage(), e);
        } else {
            throw new CmisRuntimeException(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Helper method for adding an id-valued property.<p>
     * 
     * @param props the properties to add to 
     * @param typeId the type id 
     * @param filter the property filter 
     * @param id the property id 
     * @param value the property value 
     */
    private void addPropertyId(PropertiesImpl props, String typeId, Set<String> filter, String id, String value) {

        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyIdImpl(id, value));
    }

    /**
     * Helper method for adding an id-list-valued property.<p>
     * 
     * @param props the properties to add to 
     * @param typeId the type id 
     * @param filter the property filter 
     * @param id the property id 
     * @param value the property value 
     */
    private void addPropertyIdList(
        PropertiesImpl props,
        String typeId,
        Set<String> filter,
        String id,
        List<String> value) {

        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyIdImpl(id, value));
    }

    /**
     * Adds a string property to a PropertiesImpl.<p>
     *  
     * @param props the properties 
     * @param typeId the type id 
     * @param filter the property filter string 
     * @param id the property id 
     * @param value the property value 
     */
    private void addPropertyString(PropertiesImpl props, String typeId, Set<String> filter, String id, String value) {

        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyStringImpl(id, value));
    }

    /**
     * Adds an integer property to a PropertiesImpl.<p>
     *  
     * @param props the properties 
     * @param typeId the type id 
     * @param filter the property filter string 
     * @param id the property id 
     * @param value the property value 
     */
    private void addPropertyInteger(PropertiesImpl props, String typeId, Set<String> filter, String id, long value) {

        addPropertyBigInteger(props, typeId, filter, id, BigInteger.valueOf(value));
    }

    /**
     * Adds bigint property to a PropertiesImpl.<p>
     *  
     * @param props the properties 
     * @param typeId the type id 
     * @param filter the property filter string 
     * @param id the property id 
     * @param value the property value 
     */
    private void addPropertyBigInteger(
        PropertiesImpl props,
        String typeId,
        Set<String> filter,
        String id,
        BigInteger value) {

        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyIntegerImpl(id, value));
    }

    /**
     * Adds a boolean property to a PropertiesImpl.<p>
     *  
     * @param props the properties 
     * @param typeId the type id 
     * @param filter the property filter string 
     * @param id the property id 
     * @param value the property value 
     */
    private void addPropertyBoolean(PropertiesImpl props, String typeId, Set<String> filter, String id, boolean value) {

        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyBooleanImpl(id, Boolean.valueOf(value)));
    }

    /**
     * Adds a date/time property to a PropertiesImpl.<p>
     *  
     * @param props the properties 
     * @param typeId the type id 
     * @param filter the property filter string 
     * @param id the property id 
     * @param value the property value 
     */
    private void addPropertyDateTime(
        PropertiesImpl props,
        String typeId,
        Set<String> filter,
        String id,
        GregorianCalendar value) {

        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyDateTimeImpl(id, value));
    }

    /**
     * Checks whether a property can be added to a Properties.
     *  
     * @param properties the properties object
     * @param typeId the type id 
     * @param filter the property filter
     * @param id the property id
     *  
     * @return true if the property should be added 
     */
    private boolean checkAddProperty(Properties properties, String typeId, Set<String> filter, String id) {

        if ((properties == null) || (properties.getProperties() == null)) {
            throw new IllegalArgumentException("Properties must not be null!");
        }

        if (id == null) {
            throw new IllegalArgumentException("Id must not be null!");
        }

        TypeDefinition type = m_typeManager.getType(typeId);
        if (type == null) {
            throw new IllegalArgumentException("Unknown type: " + typeId);
        }
        if (!type.getPropertyDefinitions().containsKey(id)) {
            throw new IllegalArgumentException("Unknown property: " + id);
        }

        String queryName = type.getPropertyDefinitions().get(id).getQueryName();

        if ((queryName != null) && (filter != null)) {
            if (!filter.contains(queryName)) {
                return false;
            } else {
                filter.remove(queryName);
            }
        }

        return true;
    }

    /**
     * Adds the default value of property if defined.
     *  
     * @param props the Properties object
     * @param propDef the property definition
     *  
     * @return true if the property could be added 
     */
    @SuppressWarnings("unchecked")
    private static boolean addPropertyDefault(PropertiesImpl props, PropertyDefinition<?> propDef) {

        if ((props == null) || (props.getProperties() == null)) {
            throw new IllegalArgumentException("Props must not be null!");
        }

        if (propDef == null) {
            return false;
        }

        List<?> defaultValue = propDef.getDefaultValue();
        if ((defaultValue != null) && (!defaultValue.isEmpty())) {
            switch (propDef.getPropertyType()) {
                case BOOLEAN:
                    props.addProperty(new PropertyBooleanImpl(propDef.getId(), (List<Boolean>)defaultValue));
                    break;
                case DATETIME:
                    props.addProperty(new PropertyDateTimeImpl(propDef.getId(), (List<GregorianCalendar>)defaultValue));
                    break;
                case DECIMAL:
                    props.addProperty(new PropertyDecimalImpl(propDef.getId(), (List<BigDecimal>)defaultValue));
                    break;
                case HTML:
                    props.addProperty(new PropertyHtmlImpl(propDef.getId(), (List<String>)defaultValue));
                    break;
                case ID:
                    props.addProperty(new PropertyIdImpl(propDef.getId(), (List<String>)defaultValue));
                    break;
                case INTEGER:
                    props.addProperty(new PropertyIntegerImpl(propDef.getId(), (List<BigInteger>)defaultValue));
                    break;
                case STRING:
                    props.addProperty(new PropertyStringImpl(propDef.getId(), (List<String>)defaultValue));
                    break;
                case URI:
                    props.addProperty(new PropertyUriImpl(propDef.getId(), (List<String>)defaultValue));
                    break;
                default:
                    throw new RuntimeException("Unknown datatype! Spec change?");
            }

            return true;
        }

        return false;
    }

    /**
     * Adds an action to a set of actions if a condition is fulfilled.<p>
     * 
     * @param aas the set of actions 
     * @param action the action to add 
     * @param condition the value of the condition for adding the action 
     */
    private static void addAction(Set<Action> aas, Action action, boolean condition) {

        if (condition) {
            aas.add(action);
        }
    }

    /**
     * Splits a filter statement into a collection of properties. If
     * <code>filter</code> is <code>null</code>, empty or one of the properties
     * is '*' , an empty collection will be returned.
     * 
     * @param filter the filter string 
     * @return the set of components of the filter 
     */
    private static Set<String> splitFilter(String filter) {

        if (filter == null) {
            return null;
        }

        if (filter.trim().length() == 0) {
            return null;
        }

        Set<String> result = new LinkedHashSet<String>();
        for (String s : filter.split(",")) {
            s = s.trim();
            if (s.equals("*")) {
                return null;
            } else if (s.length() > 0) {
                result.add(s);
            }
        }

        // set a few base properties
        // query name == id (for base type properties)
        result.add(PropertyIds.OBJECT_ID);
        result.add(PropertyIds.OBJECT_TYPE_ID);
        result.add(PropertyIds.BASE_TYPE_ID);

        return result;
    }

    /**
     * Gets a user-readable name for a principal id read from an ACE.<p>
     * 
     * @param cms the current CMS context 
     * @param principalId the principal id from the ACE  
     * @return the name of the principle 
     */
    String getAcePrincipalName(CmsObject cms, CmsUUID principalId) {

        if (CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID.equals(principalId)) {
            return CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME;
        }
        if (CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID.equals(principalId)) {
            return CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_NAME;
        }
        CmsRole role = CmsRole.valueOfId(principalId);
        if (role != null) {
            return role.getRoleName();
        }
        try {
            return CmsPrincipal.readPrincipalIncludingHistory(cms, principalId).getName();
        } catch (CmsException e) {
            return "" + principalId;
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
    private Acl collectAcl(CmsObject cms, CmsResource resource, boolean onlyBasic) throws CmsException {

        AccessControlListImpl cmisAcl = new AccessControlListImpl();
        List<Ace> cmisAces = new ArrayList<Ace>();
        List<CmsAccessControlEntry> aces = cms.getAccessControlEntries(resource.getRootPath(), true);
        for (CmsAccessControlEntry ace : aces) {
            boolean isDirect = ace.getResource().equals(resource.getResourceId());
            CmsUUID principalId = ace.getPrincipal();
            String principalName = getAcePrincipalName(cms, principalId);
            AccessControlEntryImpl cmisAce = new AccessControlEntryImpl();
            AccessControlPrincipalDataImpl cmisPrincipal = new AccessControlPrincipalDataImpl();
            cmisPrincipal.setPrincipalId(principalName);
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
     * Gets the name of the repository.<p>
     * 
     * @return the name of the repository 
     */
    public String getName() {

        return m_id;
    }

    /**
     * Gets the description of the repository.<p>
     * 
     * @return the repository description 
     */
    public String getDescription() {

        return m_id;
    }

    /**
     * Gets the repository information for this repository.<p>
     * 
     * @return the repository info
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
        repositoryInfo.setVendorName("Alkacon Software GmbH");

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
        capabilities.setCapabilityQuery(CapabilityQuery.NONE);
        capabilities.setCapabilityChanges(CapabilityChanges.NONE);
        capabilities.setCapabilityContentStreamUpdates(CapabilityContentStreamUpdates.ANYTIME);
        capabilities.setSupportsGetDescendants(Boolean.TRUE);
        capabilities.setSupportsGetFolderTree(Boolean.TRUE);
        capabilities.setCapabilityRendition(CapabilityRenditions.NONE);

        repositoryInfo.setCapabilities(capabilities);

        AclCapabilitiesDataImpl aclCapability = new AclCapabilitiesDataImpl();
        aclCapability.setSupportedPermissions(SupportedPermissions.BOTH);
        aclCapability.setAclPropagation(AclPropagation.REPOSITORYDETERMINED);

        // permissions
        List<PermissionDefinition> permissions = new ArrayList<PermissionDefinition>();
        permissions.add(createPermission("cmis:read", "cmis:read"));
        permissions.add(createPermission("cmis:write", "cmis:write"));
        permissions.add(createPermission("cmis:all", "cmis:all"));
        aclCapability.setPermissionDefinitionData(permissions);

        // mappings
        PermissionMappings m = new PermissionMappings();
        m.add(PermissionMapping.CAN_CREATE_DOCUMENT_FOLDER, "cmis:write");
        m.add(PermissionMapping.CAN_CREATE_FOLDER_FOLDER, "cmis:write");
        m.add(PermissionMapping.CAN_DELETE_CONTENT_DOCUMENT, "cmis:write");
        m.add(PermissionMapping.CAN_DELETE_OBJECT, "cmis:write");
        m.add(PermissionMapping.CAN_DELETE_TREE_FOLDER, "cmis:write");
        m.add(PermissionMapping.CAN_GET_ACL_OBJECT, "cmis:read");
        m.add(PermissionMapping.CAN_GET_ALL_VERSIONS_VERSION_SERIES, "cmis:read");
        m.add(PermissionMapping.CAN_GET_CHILDREN_FOLDER, "cmis:read");
        m.add(PermissionMapping.CAN_GET_DESCENDENTS_FOLDER, "cmis:read");
        m.add(PermissionMapping.CAN_GET_FOLDER_PARENT_OBJECT, "cmis:read");
        m.add(PermissionMapping.CAN_GET_PARENTS_FOLDER, "cmis:read");
        m.add(PermissionMapping.CAN_GET_PROPERTIES_OBJECT, "cmis:read");
        m.add(PermissionMapping.CAN_MOVE_OBJECT, "cmis:write");
        m.add(PermissionMapping.CAN_MOVE_SOURCE, "cmis:write");
        m.add(PermissionMapping.CAN_MOVE_TARGET, "cmis:write");
        m.add(PermissionMapping.CAN_SET_CONTENT_DOCUMENT, "cmis:write");
        m.add(PermissionMapping.CAN_UPDATE_PROPERTIES_OBJECT, "cmis:write");
        m.add(PermissionMapping.CAN_VIEW_CONTENT_OBJECT, "cmis:read");
        aclCapability.setPermissionMappingData(m);
        repositoryInfo.setAclCapabilities(aclCapability);
        return repositoryInfo;
    }

    /**
     * Simple helper class to simplify creating a permission mapping.<p>
     */
    @SuppressWarnings("serial")
    private static class PermissionMappings extends HashMap<String, PermissionMapping> {

        /** Default constructor.<p> */
        public PermissionMappings() {

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

    }

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
        pd.setPermission(permission);
        pd.setDescription(description);

        return pd;
    }

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
    public synchronized TypeDefinitionList getTypeChildren(
        CallContext context,
        String typeId,
        Boolean includePropertyDefinitions,
        BigInteger maxItems,
        BigInteger skipCount) {

        TypeDefinitionListImpl result = new TypeDefinitionListImpl();
        result.setList(new ArrayList<TypeDefinition>());
        return result;
    }

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
    public synchronized List<TypeDefinitionContainer> getTypeDescendants(
        CallContext context,
        String typeId,
        BigInteger depth,
        Boolean includePropertyDefinitions) {

        return m_typeManager.getTypeDescendants(context, typeId, depth, includePropertyDefinitions);
    }

    /**
     * Gets a type definition by id.<p>
     * 
     * @param context the call context 
     * @param typeId the type id 
     * 
     * @return the type definition for the given id 
     */
    public synchronized TypeDefinition getTypeDefinition(CallContext context, String typeId) {

        return m_typeManager.getTypeDefinition(context, typeId);
    }

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
     * @param objectInfos the combined object info for the children
     *  
     * @return the object information 
     */
    public synchronized ObjectInFolderList getChildren(
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
        ObjectInfoHandler objectInfos) {

        try {

            // split filter
            Set<String> filterCollection = splitFilter(filter);

            // set defaults if values not set
            boolean iaa = (includeAllowableActions == null ? false : includeAllowableActions.booleanValue());
            boolean ips = (includePathSegment == null ? false : includePathSegment.booleanValue());

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
                collectObjectData(context, cms, folder, null, false, false, objectInfos);
            }

            // prepare result
            ObjectInFolderListImpl result = new ObjectInFolderListImpl();
            result.setObjects(new ArrayList<ObjectInFolderData>());
            result.setHasMoreItems(Boolean.FALSE);
            int count = 0;

            List<CmsResource> children = new ArrayList<CmsResource>();
            String folderSitePath = cms.getRequestContext().getSitePath(folder);
            List<CmsResource> fileChildren = cms.getFilesInFolder(folderSitePath);
            List<CmsResource> folderChildren = cms.getSubFolders(folderSitePath);
            children.addAll(folderChildren);
            children.addAll(fileChildren);
            for (CmsResource child : children) {
                count++;
                if (skip > 0) {
                    skip--;
                    continue;
                }
                if (result.getObjects().size() >= max) {
                    result.setHasMoreItems(Boolean.TRUE);
                    continue;
                }

                // build and add child object
                ObjectInFolderDataImpl objectInFolder = new ObjectInFolderDataImpl();
                objectInFolder.setObject(collectObjectData(
                    context,
                    cms,
                    child,
                    filterCollection,
                    iaa,
                    false,
                    objectInfos));
                if (ips) {
                    objectInFolder.setPathSegment(child.getName());
                }

                result.getObjects().add(objectInFolder);
            }

            result.setNumItems(BigInteger.valueOf(count));

            return result;
        } catch (CmsException e) {
            handleCmsException(e);
            return null;
        }

    }

    /**
     * Helper method to get the children of a resource.<p>
     * 
     * @param cms the CMS context 
     * @param resource the resource 
     * @return the children of the resource 
     * 
     * @throws CmsException if something goes wrong 
     */
    private List<CmsResource> getChildren(CmsObject cms, CmsResource resource) throws CmsException {

        return cms.getResourcesInFolder(cms.getSitePath(resource), CmsResourceFilter.DEFAULT);
    }

    /**
     * Checks whether the given resource has any children.<p>
     * 
     * @param cms the CMS context
     * @param resource the resource to check
     *  
     * @return true if the resource has children 
     * 
     * @throws CmsException if something goes wrong 
     */
    private boolean hasChildren(CmsObject cms, CmsResource resource) throws CmsException {

        return !cms.getResourcesInFolder(cms.getSitePath(resource), CmsResourceFilter.ALL).isEmpty();
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
     * @param objectInfos the object info handler 
     */
    private void gatherDescendants(
        CallContext context,
        CmsObject cms,
        CmsResource folder,
        List<ObjectInFolderContainer> list,
        boolean foldersOnly,
        int depth,
        Set<String> filter,
        boolean includeAllowableActions,
        boolean includePathSegments,
        ObjectInfoHandler objectInfos) {

        try {
            List<CmsResource> children = getChildren(cms, folder);
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
                objectInFolder.setObject(collectObjectData(
                    context,
                    cms,
                    child,
                    filter,
                    includeAllowableActions,
                    false,
                    objectInfos));
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
                        includePathSegments,
                        objectInfos);
                }
            }
        } catch (CmsException e) {
            handleCmsException(e);
        }
    }

    /**
     * 
     * @param context the call context 
     * @param folderId the folder id 
     * @param depth the maximum depth 
     * @param filter the property filter 
     * @param includeAllowableActions flag to include allowable actions 
     * @param includePathSegment flag to include path segments 
     * @param objectInfos object info handler 
     * @param foldersOnly flag to ignore documents and only return folders
     * 
     * @return the list of descendants 
     */
    public synchronized List<ObjectInFolderContainer> getDescendants(
        CallContext context,
        String folderId,
        BigInteger depth,
        String filter,
        Boolean includeAllowableActions,
        Boolean includePathSegment,
        ObjectInfoHandler objectInfos,
        boolean foldersOnly) {

        try {

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

            // set defaults if values not set
            boolean iaa = (includeAllowableActions == null ? false : includeAllowableActions.booleanValue());
            boolean ips = (includePathSegment == null ? false : includePathSegment.booleanValue());

            CmsObject cms = getCmsObject(context);
            CmsUUID folderStructureId = new CmsUUID(folderId);
            CmsResource folder = cms.readResource(folderStructureId);
            if (!folder.isFolder()) {
                throw new CmisObjectNotFoundException("Not a folder!");
            }

            // set object info of the the folder
            if (context.isObjectInfoRequired()) {
                collectObjectData(context, cms, folder, null, false, false, objectInfos);
            }

            // get the tree
            List<ObjectInFolderContainer> result = new ArrayList<ObjectInFolderContainer>();
            gatherDescendants(context, cms, folder, result, foldersOnly, d, filterCollection, iaa, ips, objectInfos);

            return result;
        } catch (CmsException e) {
            handleCmsException(e);
            return null;
        }
    }

    /**
     * Gets the parents of an object.<p>
     * 
     * @param context the call context 
     * @param objectId the object id 
     * @param filter
     * @param includeAllowableActions
     * @param includeRelativePathSegment
     * @param objectInfos
     * 
     * @return the data for the object parents 
     */
    public synchronized List<ObjectParentData> getObjectParents(
        CallContext context,
        String objectId,
        String filter,
        Boolean includeAllowableActions,
        Boolean includeRelativePathSegment,
        ObjectInfoHandler objectInfos) {

        try {

            // split filter
            Set<String> filterCollection = splitFilter(filter);

            // set defaults if values not set
            boolean iaa = (includeAllowableActions == null ? false : includeAllowableActions.booleanValue());
            boolean irps = (includeRelativePathSegment == null ? false : includeRelativePathSegment.booleanValue());

            CmsObject cms = getCmsObject(context);
            CmsUUID structureId = new CmsUUID(objectId);
            CmsResource file = cms.readResource(structureId);
            // don't climb above the root folder

            if (m_root.equals(file)) {
                return Collections.emptyList();
            }

            // set object info of the the object
            if (context.isObjectInfoRequired()) {
                collectObjectData(context, cms, file, null, false, false, objectInfos);
            }

            // get parent folder
            CmsResource parent = cms.readParentFolder(file.getStructureId());
            ObjectData object = collectObjectData(context, cms, parent, filterCollection, iaa, false, objectInfos);

            ObjectParentDataImpl result = new ObjectParentDataImpl();
            result.setObject(object);
            if (irps) {
                result.setRelativePathSegment(file.getName());
            }

            return Collections.singletonList((ObjectParentData)result);
        } catch (CmsException e) {
            handleCmsException(e);
            return null;
        }

    }

    /**
     * Corresponds to CMIS getFolderParent service method.<p>
     * 
     * @param context the call context 
     * @param folderId the folder id 
     * @param filter the property filter 
     * @param objectInfos the object info handler 
     * 
     * @return the parent object data 
     */
    public synchronized ObjectData getFolderParent(
        CallContext context,
        String folderId,
        String filter,
        ObjectInfoHandler objectInfos) {

        List<ObjectParentData> parents = getObjectParents(
            context,
            folderId,
            filter,
            Boolean.FALSE,
            Boolean.FALSE,
            objectInfos);
        if (parents.size() == 0) {
            throw new CmisInvalidArgumentException("The root folder has no parent!");
        }
        return parents.get(0).getObject();
    }

    /**
     * Readonly flag to prevent write operations on the repository.<p>
     */
    private boolean m_isReadOnly;

    /**
     * Checks whether we have write access to this repository and throws an exception otherwise.<p>
     */
    private void checkWriteAccess() {

        if (m_isReadOnly) {
            throw new CmisNotSupportedException("Readonly repository '" + m_id + "' does not allow write operations.");
        }
    }

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
    public synchronized String createDocument(
        CallContext context,
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
     * Helper method to create OpenCms property objects from a map of CMIS properties.<p>
     * 
     * @param properties the CMIS properties 
     * 
     * @return the OpenCms properties 
     */
    protected List<CmsProperty> getOpenCmsProperties(Map<String, PropertyData<?>> properties) {

        List<CmsProperty> cmsProperties = new ArrayList<CmsProperty>();
        for (Map.Entry<String, PropertyData<?>> entry : properties.entrySet()) {
            String propId = entry.getKey();
            if (propId.startsWith(CmsCmisTypeManager.PROPERTY_PREFIX)) {
                String propName = propId.substring(CmsCmisTypeManager.PROPERTY_PREFIX.length());
                String value = (String)entry.getValue().getFirstValue();
                if (value == null) {
                    value = "";
                }
                cmsProperties.add(new CmsProperty(propName, value, null));
            }
        }
        return cmsProperties;
    }

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
    public synchronized String createDocumentFromSource(
        CallContext context,
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
     * Checks whether a name is a valid OpenCms resource name and throws an exception otherwise.<p>
     * 
     * @param name the name to check 
     */
    private void checkResourceName(String name) {

        try {
            CmsResource.checkResourceName(name);
        } catch (CmsIllegalArgumentException e) {
            throw new CmisNameConstraintViolationException(e.getLocalizedMessage(), e);
        }
    }

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
    public synchronized String createFolder(
        CallContext context,
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
     * Gets the allowable actions for an object.<p>
     * 
     * @param context the call context 
     * @param objectId the object id 
     * @return the allowable actions 
     */
    public synchronized AllowableActions getAllowableActions(CallContext context, String objectId) {

        try {
            CmsObject cms = getCmsObject(context);
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
     * @param objectInfos the object info handler 
     * 
     * @return the CMIS object data 
     */
    public synchronized ObjectData getObject(
        CallContext context,
        String objectId,
        String filter,
        Boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        Boolean includePolicyIds,
        Boolean includeAcl,
        ObjectInfoHandler objectInfos) {

        try {

            // check id
            if (objectId == null) {
                throw new CmisInvalidArgumentException("Object Id must be set.");
            }
            CmsObject cms = getCmsObject(context);
            // get the file or folder
            CmsResource file = cms.readResource(new CmsUUID(objectId));

            // set defaults if values not set
            boolean iaa = (includeAllowableActions == null ? false : includeAllowableActions.booleanValue());
            boolean iacl = (includeAcl == null ? false : includeAcl.booleanValue());

            // split filter
            Set<String> filterCollection = splitFilter(filter);

            // gather properties
            return collectObjectData(context, cms, file, filterCollection, iaa, iacl, objectInfos);
        } catch (CmsException e) {
            handleCmsException(e);
            return null;
        }
    }

    /**
     * Gets the properties for a CMIS object.<p>
     *  
     * @param context the call context 
     * @param objectId the CMIS object id 
     * @param filter the property filter string 
     * @param objectInfos the object info handler 
     * 
     * @return the set of properties 
     */
    public synchronized Properties getProperties(CallContext context, String objectId, String filter,

    ObjectInfoHandler objectInfos) {

        ObjectData object = getObject(
            context,
            objectId,
            null,
            Boolean.FALSE,
            null,
            null,
            Boolean.FALSE,
            Boolean.FALSE,
            objectInfos);
        return object.getProperties();
    }

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
    public synchronized List<RenditionData> getRenditions(
        CallContext context,
        String objectId,
        String renditionFilter,
        BigInteger maxItems,
        BigInteger skipCount) {

        try {
            CmsObject cms = getCmsObject(context);
            CmsUUID structureId = new CmsUUID(objectId);
            CmsResource resource = cms.readResource(structureId);
            RenditionDataImpl rendition = new RenditionDataImpl();
            rendition.setKind("opencms:rendered");
            rendition.setMimeType(OpenCms.getResourceManager().getMimeType(resource.getRootPath(), "UTF-8"));
            rendition.setStreamId("rendered");
            List<RenditionData> result = Collections.singletonList((RenditionData)rendition);
            return result;
        } catch (CmsException e) {
            handleCmsException(e);
            return null;
        }
    }

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
     * @param objectInfos the object info handler 
     * 
     * @return the object data 
     */
    public synchronized ObjectData getObjectByPath(
        CallContext context,
        String path,
        String filter,
        Boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        Boolean includePolicyIds,
        Boolean includeAcl,

        ObjectInfoHandler objectInfos) {

        try {

            // split filter
            Set<String> filterCollection = splitFilter(filter);

            // check path
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(path)) {
                throw new CmisInvalidArgumentException("Invalid folder path!");
            }
            CmsObject cms = getCmsObject(context);
            CmsResource file = cms.readResource(path);

            return collectObjectData(
                context,
                cms,
                file,
                filterCollection,
                includeAllowableActions.booleanValue(),
                includeAcl.booleanValue(),
                objectInfos);

        } catch (CmsException e) {
            handleCmsException(e);
            return null;
        }
    }

    /**
     * Copies a range of bytes from an array into a new array.<p>
     * 
     * @param content the content array 
     * @param offset the start offset in the array 
     * @param length the length of the range 
     * 
     * @return the bytes from the given range of the content 
     */
    private byte[] extractRange(byte[] content, BigInteger offset, BigInteger length) {

        if ((offset == null) && (length == null)) {
            return content;
        }
        if (offset == null) {
            offset = BigInteger.ZERO;
        }
        long offsetLong = offset.longValue();
        if (length == null) {
            length = BigInteger.valueOf(content.length - offsetLong);
        }
        long lengthLong = length.longValue();
        return Arrays.copyOfRange(content, (int)offsetLong, (int)(offsetLong + lengthLong));
    }

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
    public synchronized ContentStream getContentStream(
        CallContext context,
        String objectId,
        String streamId,
        BigInteger offset,
        BigInteger length) {

        try {

            if ((offset != null) || (length != null)) {
                throw new CmisInvalidArgumentException("Offset and Length are not supported!");
            }
            CmsObject cms = getCmsObject(context);
            CmsResource resource = cms.readResource(new CmsUUID(objectId));
            if (resource.isFolder()) {
                throw new CmisStreamNotSupportedException("Not a file!");
            }
            CmsFile file = cms.readFile(resource);
            byte[] contents;
            contents = file.getContents();
            contents = extractRange(contents, offset, length);
            InputStream stream = new ByteArrayInputStream(contents);

            ContentStreamImpl result = new ContentStreamImpl();
            result.setFileName(file.getName());
            result.setLength(BigInteger.valueOf(contents.length));
            result.setMimeType(OpenCms.getResourceManager().getMimeType(file.getRootPath(), null, "text/plain"));
            result.setStream(stream);

            return result;
        } catch (CmsException e) {
            handleCmsException(e);
            return null;
        }
    }

    /**
     * Updates the properties for an object.<p>
     * 
     * @param context the call context 
     * @param objectId the object id 
     * @param changeToken the change token 
     * @param properties the properties 
     */
    public synchronized void updateProperties(
        CallContext context,
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
     * Moves an object.<p>
     *  
     * @param context the call context 
     * @param objectId the object id 
     * @param targetFolderId source source folder id 
     * @param sourceFolderId the target folder id 
     */
    public synchronized void moveObject(
        CallContext context,
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
     * Deletes a CMIS object.<p>
     * 
     * @param context the call context 
     * @param objectId the id of the object to delete 
     * @param allVersions flag to delete all version 
     */
    public synchronized void deleteObject(CallContext context, String objectId, Boolean allVersions) {

        checkWriteAccess();
        try {
            CmsObject cms = getCmsObject(context);
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
    public synchronized FailedToDeleteData deleteTree(
        CallContext context,
        String folderId,
        Boolean allVersions,
        UnfileObject unfileObjects,
        Boolean continueOnFailure) {

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
     * Tries to lock a resource and throws an exception if it can't be locked.<p>
     * 
     * Returns true only if the resource wasn't already locked before.<p>
     * 
     * @param cms the CMS context 
     * @param resource the resource to lock 
     * @return true if the resource wasn't already locked 
     * 
     * @throws CmsException if something goes wrong 
     */
    private boolean ensureLock(CmsObject cms, CmsResource resource) throws CmsException {

        CmsLock lock = cms.getLock(resource);
        if (lock.isOwnedBy(cms.getRequestContext().getCurrentUser())) {
            return false;
        }
        cms.lockResourceTemporary(resource);
        return true;
    }

    /**
     * Sets the content stream of an object.<p>
     *  
     * @param context the call context 
     * @param objectId the id of the object 
     * @param overwriteFlag flag to overwrite the content stream 
     * @param changeToken the change token 
     * @param contentStream the new content stream 
     */
    public synchronized void setContentStream(
        CallContext context,
        Holder<String> objectId,
        Boolean overwriteFlag,
        Holder<String> changeToken,
        ContentStream contentStream) {

        checkWriteAccess();

        try {
            CmsObject cms = getCmsObject(context);
            CmsUUID structureId = new CmsUUID(objectId.getValue());
            boolean overwrite = (overwriteFlag == null) || overwriteFlag.booleanValue();
            if (!overwrite) {
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
     * Deletes the content stream of an object.<p>
     * 
     * @param context the call context 
     * @param objectId the object id 
     * @param changeToken the change token 
     */
    public synchronized void deleteContentStream(
        CallContext context,
        Holder<String> objectId,
        Holder<String> changeToken) {

        throw new CmisConstraintException("Content streams may not be deleted.");

    }

    /**
     * Converts an OpenCms ACE to a list of basic CMIS permissions.<p>
     * 
     * @param ace the access control entry 
     * 
     * @return the list of permissions 
     */
    protected List<String> getCmisPermissions(CmsAccessControlEntry ace) {

        int permissionBits = ace.getPermissions().getPermissions();
        List<String> result = new ArrayList<String>();
        if (0 != (permissionBits & CmsPermissionSet.PERMISSION_READ)) {
            result.add("cmis:read");
        }
        if (0 != (permissionBits & CmsPermissionSet.PERMISSION_WRITE)) {
            result.add("cmis:write");
        }
        int all = CmsPermissionSet.PERMISSION_WRITE
            | CmsPermissionSet.PERMISSION_READ
            | CmsPermissionSet.PERMISSION_CONTROL
            | CmsPermissionSet.PERMISSION_DIRECT_PUBLISH;
        if ((permissionBits & all) == all) {
            result.add("cmis:all");
        }
        return result;
    }

    /**
     * Converts an OpenCms access control bitset to a list of CMIS permissions representing native OpenCms permissions.<p>
     * 
     * @param permissionBits the permission bits 
     * @param denied if the permission bitset refers to a list of denied rather than allowed permissions
     *   
     * @return the list of native permissions 
     */
    protected List<String> getNativePermissions(int permissionBits, boolean denied) {

        List<String> result = new ArrayList<String>();
        String prefix = denied ? "opencms:deny-" : "opencms:";
        if ((permissionBits & CmsPermissionSet.PERMISSION_READ) != 0) {
            result.add(prefix + "read");
        }
        if ((permissionBits & CmsPermissionSet.PERMISSION_WRITE) != 0) {
            result.add(prefix + "write");
        }

        if ((permissionBits & CmsPermissionSet.PERMISSION_VIEW) != 0) {
            result.add(prefix + "view");
        }

        if ((permissionBits & CmsPermissionSet.PERMISSION_CONTROL) != 0) {
            result.add(prefix + "control");
        }

        if ((permissionBits & CmsPermissionSet.PERMISSION_DIRECT_PUBLISH) != 0) {
            result.add(prefix + "publish");
        }
        return result;
    }

    /**
     * Converts an OpenCms access control entry to a list of CMIS permissions which represent native OpenCms permissions.<p>
     * 
     * @param ace the access control entry 
     * @return the list of permissions for the entry 
     */
    protected List<String> getNativePermissions(CmsAccessControlEntry ace) {

        List<String> result = getNativePermissions(ace.getPermissions().getAllowedPermissions(), false);
        result.addAll(getNativePermissions(ace.getPermissions().getDeniedPermissions(), true));
        return result;
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
    public synchronized Acl getAcl(CallContext context, String objectId, Boolean onlyBasicPermissions) {

        try {

            CmsObject cms = getCmsObject(context);
            CmsUUID structureId = new CmsUUID(objectId);
            CmsResource resource = cms.readResource(structureId);
            return collectAcl(cms, resource, onlyBasicPermissions.booleanValue());
        } catch (CmsException e) {
            handleCmsException(e);
            return null;
        }

    }

}

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
import static org.opencms.cmis.CmsCmisUtil.addPropertyDateTime;
import static org.opencms.cmis.CmsCmisUtil.addPropertyId;
import static org.opencms.cmis.CmsCmisUtil.addPropertyString;
import static org.opencms.cmis.CmsCmisUtil.handleCmsException;
import static org.opencms.cmis.CmsCmisUtil.millisToCalendar;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AllowableActionsImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;

/**
 * Helper class for CMIS CRUD operations on relation objects.<p>
 *
 * Since CMIS requires any object to have an ID by which it is accessed, but OpenCms relations
 * are not addressable by ids, we invent an artificial relation id string of the form
 * REL_(SOURCE_ID)_(TARGET_ID)_(TYPE).<p>
 *
 */
public class CmsCmisRelationHelper implements I_CmsCmisObjectHelper {

    /**
     * A class which contains the necessary information to identify a relation object.<p>
     */
    public static class RelationKey {

        /** The internal OpenCms relation object (optional). */
        private CmsRelation m_relation;

        /** The relation type string. */
        private String m_relType;

        /** The internal OpenCms resource which is the relation source (optional). */
        private CmsResource m_source;

        /** The source id of the relation. */
        private CmsUUID m_sourceId;

        /** The target id of the relation. */
        private CmsUUID m_targetId;

        /**
         * Creates a new relation key.<p>
         *
         * @param sourceId the source id
         * @param targetId the target id
         * @param relType the relation type
         */
        public RelationKey(CmsUUID sourceId, CmsUUID targetId, String relType) {

            m_sourceId = sourceId;
            m_targetId = targetId;
            m_relType = relType;
        }

        /**
         * Reads the actual resource and relation data from the OpenCms VFS.<p>
         *
         * @param cms the CMS context to use for reading the data
         */
        public void fillRelation(CmsObject cms) {

            try {
                m_source = cms.readResource(m_sourceId);
                List<CmsRelation> relations = cms.getRelationsForResource(
                    m_source,
                    CmsRelationFilter.TARGETS.filterStructureId(m_targetId).filterType(getRelationType(m_relType)));
                if (relations.isEmpty()) {
                    throw new CmisObjectNotFoundException(toString());
                }
                m_relation = relations.get(0);
            } catch (CmsException e) {
                CmsCmisUtil.handleCmsException(e);
            }
        }

        /**
         * Gets the relation object.<p>
         *
         * @return the relation object
         */
        public CmsRelation getRelation() {

            return m_relation;
        }

        /**
         * Gets the relation type.<p>
         *
         * @return the relation type
         */
        public String getRelType() {

            return m_relType;
        }

        /**
         * Gets the source resource of the relation.<p>
         *
         * @return the source of the relation
         */
        public CmsResource getSource() {

            return m_source;
        }

        /**
         * Gets the source id.<p>
         *
         * @return the source id
         */
        public CmsUUID getSourceId() {

            return m_sourceId;
        }

        /**
         * Gets the target id of the relation.<p>
         *
         * @return the target id
         */
        public CmsUUID getTargetId() {

            return m_targetId;
        }

        /**
         * Sets the relation type.<p>
         *
         * @param relType the relation type
         */
        public void setRelType(String relType) {

            m_relType = relType;
        }

        /**
         * Sets the source id.<p>
         *
         * @param sourceId the source id
         */
        public void setSourceId(CmsUUID sourceId) {

            m_sourceId = sourceId;
        }

        /**
         * Sets the target id.<p>
         *
         * @param targetId the target id
         */
        public void setTargetId(CmsUUID targetId) {

            m_targetId = targetId;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return createKey(m_sourceId, m_targetId, m_relType);
        }
    }

    /** The prefix used to identify relation ids. */
    public static final String RELATION_ID_PREFIX = "REL_";

    /** The pattern which relation ids should match. */
    public static final Pattern RELATION_PATTERN = Pattern.compile(
        "^REL_(" + CmsUUID.UUID_REGEX + ")_(" + CmsUUID.UUID_REGEX + ")_(.*)$");

    /** The underlying CMIS repository. */
    private CmsCmisRepository m_repository;

    /**
     * Creates a new relation helper for the given repository.<p>
     *
     * @param repository the repository
     */
    public CmsCmisRelationHelper(CmsCmisRepository repository) {

        m_repository = repository;
    }

    /**
     * Creates a relation id string from the source and target ids and a relation type.<p>
     *
     * @param source the source id
     * @param target the target id
     * @param relType the relation type
     *
     * @return the relation id
     */
    protected static String createKey(CmsUUID source, CmsUUID target, String relType) {

        return RELATION_ID_PREFIX + source + "_" + target + "_" + relType;
    }

    /**
     * Gets a relation type by name.<p>
     *
     * @param typeName the relation type name
     *
     * @return the relation type with the matching name
     */
    protected static CmsRelationType getRelationType(String typeName) {

        for (CmsRelationType relType : CmsRelationType.getAll()) {
            if (relType.getName().equalsIgnoreCase(typeName)) {
                return relType;
            }
        }
        return null;
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisObjectHelper#deleteObject(org.opencms.cmis.CmsCmisCallContext, java.lang.String, boolean)
     */
    public void deleteObject(CmsCmisCallContext context, String objectId, boolean allVersions) {

        try {

            RelationKey rk = parseRelationKey(objectId);
            CmsUUID sourceId = rk.getSourceId();
            CmsObject cms = m_repository.getCmsObject(context);
            CmsResource sourceResource = cms.readResource(sourceId);
            boolean wasLocked = CmsCmisUtil.ensureLock(cms, sourceResource);
            try {
                CmsRelationFilter relFilter = CmsRelationFilter.ALL.filterType(
                    getRelationType(rk.getRelType())).filterStructureId(rk.getTargetId());
                cms.deleteRelationsFromResource(sourceResource.getRootPath(), relFilter);
            } finally {
                if (wasLocked) {
                    cms.unlockResource(sourceResource);
                }
            }
        } catch (CmsException e) {
            CmsCmisUtil.handleCmsException(e);
        }
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisObjectHelper#getAcl(org.opencms.cmis.CmsCmisCallContext, java.lang.String, boolean)
     */
    public Acl getAcl(CmsCmisCallContext context, String objectId, boolean onlyBasicPermissions) {

        CmsObject cms = m_repository.getCmsObject(context);
        RelationKey rk = parseRelationKey(objectId);
        rk.fillRelation(cms);
        return collectAcl(cms, rk.getSource(), onlyBasicPermissions);
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisObjectHelper#getAllowableActions(org.opencms.cmis.CmsCmisCallContext, java.lang.String)
     */
    public AllowableActions getAllowableActions(CmsCmisCallContext context, String objectId) {

        CmsObject cms = m_repository.getCmsObject(context);
        RelationKey rk = parseRelationKey(objectId);
        rk.fillRelation(cms);
        return collectAllowableActions(cms, rk.getSource(), rk.getRelation());
    }

    /**
     * @see org.opencms.cmis.I_CmsCmisObjectHelper#getObject(org.opencms.cmis.CmsCmisCallContext, java.lang.String, java.lang.String, boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, boolean, boolean)
     */
    public ObjectData getObject(
        CmsCmisCallContext context,
        String objectId,
        String filter,
        boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        boolean includePolicyIds,
        boolean includeAcl) {

        CmsObject cms = m_repository.getCmsObject(context);
        RelationKey rk = parseRelationKey(objectId);
        rk.fillRelation(cms);
        Set<String> filterSet = CmsCmisUtil.splitFilter(filter);
        ObjectData result = collectObjectData(
            context,
            cms,
            rk.getSource(),
            rk.getRelation(),
            filterSet,
            includeAllowableActions,
            includeAcl);
        return result;
    }

    /**
     * Compiles the ACL for a relation.<p>
     *
     * @param cms the CMS context
     * @param resource the resource for which to collect the ACLs
     * @param onlyBasic flag to only include basic ACEs
     *
     * @return the ACL for the resource
     */
    protected Acl collectAcl(CmsObject cms, CmsResource resource, boolean onlyBasic) {

        AccessControlListImpl cmisAcl = new AccessControlListImpl();
        List<Ace> cmisAces = new ArrayList<Ace>();
        cmisAcl.setAces(cmisAces);
        cmisAcl.setExact(Boolean.FALSE);
        return cmisAcl;
    }

    /**
     * Collects the allowable actions for a relation.<p>
     *
     * @param cms the current CMS context
     * @param file the source of the relation
     * @param relation the relation object
     *
     * @return the allowable actions for the given resource
     */
    protected AllowableActions collectAllowableActions(CmsObject cms, CmsResource file, CmsRelation relation) {

        try {
            Set<Action> aas = new LinkedHashSet<Action>();
            AllowableActionsImpl result = new AllowableActionsImpl();

            CmsLock lock = cms.getLock(file);
            CmsUser user = cms.getRequestContext().getCurrentUser();
            boolean canWrite = !cms.getRequestContext().getCurrentProject().isOnlineProject()
                && (lock.isOwnedBy(user) || lock.isLockableBy(user))
                && cms.hasPermissions(file, CmsPermissionSet.ACCESS_WRITE, false, CmsResourceFilter.DEFAULT);
            addAction(aas, Action.CAN_GET_PROPERTIES, true);
            addAction(aas, Action.CAN_DELETE_OBJECT, canWrite && !relation.getType().isDefinedInContent());
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
     * @param relation the relation object
     * @param filter the property filter string
     * @param includeAllowableActions true if the allowable actions should be included
     * @param includeAcl true if the ACL entries should be included
     *
     * @return the object data
     */
    protected ObjectData collectObjectData(
        CmsCmisCallContext context,
        CmsObject cms,
        CmsResource resource,
        CmsRelation relation,
        Set<String> filter,
        boolean includeAllowableActions,
        boolean includeAcl) {

        ObjectDataImpl result = new ObjectDataImpl();
        ObjectInfoImpl objectInfo = new ObjectInfoImpl();

        result.setProperties(collectProperties(cms, resource, relation, filter, objectInfo));

        if (includeAllowableActions) {
            result.setAllowableActions(collectAllowableActions(cms, resource, relation));
        }

        if (includeAcl) {
            result.setAcl(collectAcl(cms, resource, true));
            result.setIsExactAcl(Boolean.FALSE);
        }

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
     * @param relation the relation object
     * @param orgfilter the property filter
     * @param objectInfo the object info handler
     *
     * @return the properties for the given resource
     */
    protected Properties collectProperties(
        CmsObject cms,
        CmsResource resource,
        CmsRelation relation,
        Set<String> orgfilter,
        ObjectInfoImpl objectInfo) {

        CmsCmisTypeManager tm = m_repository.getTypeManager();

        if (resource == null) {
            throw new IllegalArgumentException("Resource may not be null.");
        }

        // copy filter
        Set<String> filter = (orgfilter == null ? null : new LinkedHashSet<String>(orgfilter));

        // find base type
        String typeId = "opencms:" + relation.getType().getName();
        objectInfo.setBaseType(BaseTypeId.CMIS_RELATIONSHIP);
        objectInfo.setTypeId(typeId);
        objectInfo.setContentType(null);
        objectInfo.setFileName(null);
        objectInfo.setHasAcl(false);
        objectInfo.setHasContent(false);
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

        // let's do it
        try {
            PropertiesImpl result = new PropertiesImpl();

            // id
            String id = createKey(relation);
            addPropertyId(tm, result, typeId, filter, PropertyIds.OBJECT_ID, id);
            objectInfo.setId(id);

            // name
            String name = createReadableName(relation);
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

            addPropertyId(tm, result, typeId, filter, PropertyIds.SOURCE_ID, relation.getSourceId().toString());
            addPropertyId(tm, result, typeId, filter, PropertyIds.TARGET_ID, relation.getTargetId().toString());

            // creation and modification date
            GregorianCalendar lastModified = millisToCalendar(resource.getDateLastModified());
            GregorianCalendar created = millisToCalendar(resource.getDateCreated());

            addPropertyDateTime(tm, result, typeId, filter, PropertyIds.CREATION_DATE, created);
            addPropertyDateTime(tm, result, typeId, filter, PropertyIds.LAST_MODIFICATION_DATE, lastModified);
            objectInfo.setCreationDate(created);
            objectInfo.setLastModificationDate(lastModified);

            // change token - always null
            addPropertyString(tm, result, typeId, filter, PropertyIds.CHANGE_TOKEN, null);

            // base type and type name
            addPropertyId(tm, result, typeId, filter, PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_RELATIONSHIP.value());
            addPropertyId(tm, result, typeId, filter, PropertyIds.OBJECT_TYPE_ID, typeId);
            objectInfo.setHasParent(false);
            return result;
        } catch (Exception e) {
            if (e instanceof CmisBaseException) {
                throw (CmisBaseException)e;
            }
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Creates a user-readable name from the given relation object.<p>
     *
     * @param relation the relation object
     *
     * @return the readable name
     */
    protected String createReadableName(CmsRelation relation) {

        return relation.getType().getName()
            + "[ "
            + relation.getSourcePath()
            + " -> "
            + relation.getTargetPath()
            + " ]";
    }

    /**
     * Extracts the source/target ids and the type from a relation id.<p>
     *
     * @param id the relation id
     *
     * @return the relation key object
     */
    protected RelationKey parseRelationKey(String id) {

        Matcher matcher = RELATION_PATTERN.matcher(id);
        matcher.find();
        CmsUUID src = new CmsUUID(matcher.group(1));
        CmsUUID tgt = new CmsUUID(matcher.group(2));
        String tp = matcher.group(3);
        return new RelationKey(src, tgt, tp);
    }

    /**
     * Creates a relation id from the given OpenCms relation object.<p>
     *
     * @param relation the OpenCms relation object
     *
     * @return the relation id
     */
    String createKey(CmsRelation relation) {

        return createKey(relation.getSourceId(), relation.getTargetId(), relation.getType().getName());
    }

}

/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.db.jpa;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.db.CmsDbConsistencyException;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsResourceState;
import org.opencms.db.CmsVfsOnlineResourceAlreadyExistsException;
import org.opencms.db.I_CmsDriver;
import org.opencms.db.I_CmsProjectDriver;
import org.opencms.db.I_CmsVfsDriver;
import org.opencms.db.jpa.persistence.CmsDAOContents;
import org.opencms.db.jpa.persistence.CmsDAOCounters;
import org.opencms.db.jpa.persistence.CmsDAOOfflineContents;
import org.opencms.db.jpa.persistence.CmsDAOOfflineProperties;
import org.opencms.db.jpa.persistence.CmsDAOOfflinePropertyDef;
import org.opencms.db.jpa.persistence.CmsDAOOfflineResourceRelations;
import org.opencms.db.jpa.persistence.CmsDAOOfflineResources;
import org.opencms.db.jpa.persistence.CmsDAOOfflineStructure;
import org.opencms.db.jpa.persistence.CmsDAOOfflineUrlNameMappings;
import org.opencms.db.jpa.persistence.CmsDAOOnlineProperties;
import org.opencms.db.jpa.persistence.CmsDAOOnlinePropertyDef;
import org.opencms.db.jpa.persistence.CmsDAOOnlineResourceRelations;
import org.opencms.db.jpa.persistence.CmsDAOOnlineResources;
import org.opencms.db.jpa.persistence.CmsDAOOnlineStructure;
import org.opencms.db.jpa.persistence.CmsDAOOnlineUrlNameMappings;
import org.opencms.db.jpa.persistence.I_CmsDAOProperties;
import org.opencms.db.jpa.persistence.I_CmsDAOPropertyDef;
import org.opencms.db.jpa.persistence.I_CmsDAOResourceRelations;
import org.opencms.db.jpa.persistence.I_CmsDAOResources;
import org.opencms.db.jpa.persistence.I_CmsDAOStructure;
import org.opencms.db.jpa.persistence.I_CmsDAOUrlNameMappings;
import org.opencms.db.jpa.utils.CmsQueryIntParameter;
import org.opencms.db.jpa.utils.CmsQueryStringParameter;
import org.opencms.db.jpa.utils.I_CmsQueryParameter;
import org.opencms.db.urlname.CmsUrlNameMappingEntry;
import org.opencms.db.urlname.CmsUrlNameMappingFilter;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsException;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.I_CmsResource;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsDataTypeUtil;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.apache.commons.logging.Log;

/**
 * JPA database server implementation of the vfs driver methods.<p>
 * 
 * @since 8.0.0 
 */
public class CmsVfsDriver implements I_CmsDriver, I_CmsVfsDriver {

    /** Internal presentation of empty binary content. */
    public static final byte[] EMPTY_BLOB = new byte[0];

    /** Query key. */
    private static final String C_DELETE_RELATIONS = "C_DELETE_RELATIONS";

    /** Query key. */
    private static final String C_DELETE_URLNAME_MAPPINGS = "C_DELETE_URLNAME_MAPPINGS";

    /** Query key. */
    private static final String C_HISTORY_CONTENTS_UPDATE = "C_HISTORY_CONTENTS_UPDATE";

    /** Query key. */
    private static final String C_MOVE_RELATIONS_SOURCE = "C_MOVE_RELATIONS_SOURCE";

    /** Query key. */
    private static final String C_MOVE_RELATIONS_TARGET = "C_MOVE_RELATIONS_TARGET";

    /** Query key. */
    private static final String C_OFFLINE_CONTENTS_UPDATE = "C_OFFLINE_CONTENTS_UPDATE";

    /** Query key. */
    private static final String C_OFFLINE_FILE_CONTENT_DELETE = "C_OFFLINE_FILE_CONTENT_DELETE";

    /** Query key. */
    private static final String C_ONLINE_CONTENTS_DELETE = "C_ONLINE_CONTENTS_DELETE";

    /** Query key. */
    private static final String C_ONLINE_CONTENTS_HISTORY = "C_ONLINE_CONTENTS_HISTORY";

    /** Query key. */
    private static final String C_ONLINE_FILES_CONTENT = "C_ONLINE_FILES_CONTENT";

    /** Query key. */
    private static final String C_PROPERTIES_DELETE = "C_PROPERTIES_DELETE";

    /** Query key. */
    private static final String C_PROPERTIES_DELETE_ALL_STRUCTURE_AND_RESOURCE_VALUES = "C_PROPERTIES_DELETE_ALL_STRUCTURE_AND_RESOURCE_VALUES";

    /** Query key. */
    private static final String C_PROPERTIES_DELETE_ALL_VALUES_FOR_MAPPING_TYPE = "C_PROPERTIES_DELETE_ALL_VALUES_FOR_MAPPING_TYPE";

    /** Query key. */
    private static final String C_PROPERTIES_READ = "C_PROPERTIES_READ";

    /** Query key. */
    private static final String C_PROPERTIES_READALL = "C_PROPERTIES_READALL";

    /** Query key. */
    private static final String C_PROPERTIES_READALL_COUNT = "C_PROPERTIES_READALL_COUNT";

    /** Query key. */
    private static final String C_PROPERTIES_UPDATE = "C_PROPERTIES_UPDATE";

    /** Query key. */
    private static final String C_PROPERTYDEF_DELETE = "C_PROPERTYDEF_DELETE";

    /** Query key. */
    private static final String C_PROPERTYDEF_READ = "C_PROPERTYDEF_READ";

    /** Query key. */
    private static final String C_PROPERTYDEF_READALL = "C_PROPERTYDEF_READALL";

    /** Query key. */
    private static final String C_READ_RELATIONS = "C_READ_RELATIONS";

    /** Query key. */
    private static final String C_READ_RESOURCE_OUS = "C_READ_RESOURCE_OUS";

    /** Query key. */
    private static final String C_READ_RESOURCE_STATE = "C_READ_RESOURCE_STATE";

    /** Query key. */
    private static final String C_READ_STRUCTURE_STATE = "C_READ_STRUCTURE_STATE";

    /** Query key. */
    private static final String C_READ_URLNAME_MAPPINGS = "C_READ_URLNAME_MAPPINGS";

    /** Query key. */
    private static final String C_RELATION_FILTER_SOURCE_ID = "C_RELATION_FILTER_SOURCE_ID";

    /** Query key. */
    private static final String C_RELATION_FILTER_SOURCE_PATH = "C_RELATION_FILTER_SOURCE_PATH";

    /** Query key. */
    private static final String C_RELATION_FILTER_TARGET_ID = "C_RELATION_FILTER_TARGET_ID";

    /** Query key. */
    private static final String C_RELATION_FILTER_TARGET_PATH = "C_RELATION_FILTER_TARGET_PATH";

    /** Query key. */
    private static final String C_RELATION_FILTER_TYPE = "C_RELATION_FILTER_TYPE";

    /** Query key. */
    private static final String C_RELATIONS_REPAIR_BROKEN = "C_RELATIONS_REPAIR_BROKEN";

    /** Query key. */
    private static final String C_RELATIONS_UPDATE_BROKEN = "C_RELATIONS_UPDATE_BROKEN";

    /** Query key. */
    private static final String C_RESOURCE_REPLACE = "C_RESOURCE_REPLACE";

    /** Query key. */
    private static final String C_RESOURCES_COUNT_SIBLINGS = "C_RESOURCES_COUNT_SIBLINGS";

    /** Query key. */
    private static final String C_RESOURCES_DELETE_BY_RESOURCEID = "C_RESOURCES_DELETE_BY_RESOURCEID";

    /** Query key. */
    private static final String C_RESOURCES_GET_RESOURCE_IN_PROJECT_IGNORE_STATE = "C_RESOURCES_GET_RESOURCE_IN_PROJECT_IGNORE_STATE";

    /** Query key. */
    private static final String C_RESOURCES_GET_RESOURCE_IN_PROJECT_WITH_STATE = "C_RESOURCES_GET_RESOURCE_IN_PROJECT_WITH_STATE";

    /** Query key. */
    private static final String C_RESOURCES_GET_RESOURCE_IN_PROJECT_WITHOUT_STATE = "C_RESOURCES_GET_RESOURCE_IN_PROJECT_WITHOUT_STATE";

    /** Query key. */
    private static final String C_RESOURCES_GET_RESOURCE_WITH_PROPERTYDEF = "C_RESOURCES_GET_RESOURCE_WITH_PROPERTYDEF";

    /** Query key. */
    private static final String C_RESOURCES_GET_RESOURCE_WITH_PROPERTYDEF_VALUE = "C_RESOURCES_GET_RESOURCE_WITH_PROPERTYDEF_VALUE";

    /** Query key. */
    private static final String C_RESOURCES_GET_SUBRESOURCES = "C_RESOURCES_GET_SUBRESOURCES";

    /** Query key. */
    private static final String C_RESOURCES_GET_SUBRESOURCES_GET_FILES = "C_RESOURCES_GET_SUBRESOURCES_GET_FILES";

    /** Query key. */
    private static final String C_RESOURCES_GET_SUBRESOURCES_GET_FOLDERS = "C_RESOURCES_GET_SUBRESOURCES_GET_FOLDERS";

    /** Query key. */
    private static final String C_RESOURCES_MOVE = "C_RESOURCES_MOVE";

    /** Query key. */
    private static final String C_RESOURCES_ORDER_BY_PATH = "C_RESOURCES_ORDER_BY_PATH";

    /** Query key. */
    private static final String C_RESOURCES_READ = "C_RESOURCES_READ";

    /** Query key. */
    private static final String C_RESOURCES_READ_PARENT_BY_ID = "C_RESOURCES_READ_PARENT_BY_ID";

    /** Query key. */
    private static final String C_RESOURCES_READ_PARENT_STRUCTURE_ID = "C_RESOURCES_READ_PARENT_STRUCTURE_ID";

    /** Query key. */
    private static final String C_RESOURCES_READ_RESOURCE_STATE = "C_RESOURCES_READ_RESOURCE_STATE";

    /** Query key. */
    private static final String C_RESOURCES_READ_TREE = "C_RESOURCES_READ_TREE";

    /** Query key. */
    private static final String C_RESOURCES_READ_VERSION_RES = "C_RESOURCES_READ_VERSION_RES";

    /** Query key. */
    private static final String C_RESOURCES_READ_VERSION_STR = "C_RESOURCES_READ_VERSION_STR";

    /** Query key. */
    private static final String C_RESOURCES_READ_WITH_ACE_1 = "C_RESOURCES_READ_WITH_ACE_1";

    /** Query key. */
    private static final String C_RESOURCES_READBYID = "C_RESOURCES_READBYID";

    /** Query key. */
    private static final String C_RESOURCES_SELECT_BY_DATE_LASTMODIFIED_AFTER = "C_RESOURCES_SELECT_BY_DATE_LASTMODIFIED_AFTER";

    /** Query key. */
    private static final String C_RESOURCES_SELECT_BY_DATE_LASTMODIFIED_BEFORE = "C_RESOURCES_SELECT_BY_DATE_LASTMODIFIED_BEFORE";

    /** Query key. */
    private static final String C_RESOURCES_SELECT_BY_PARENT_UUID = "C_RESOURCES_SELECT_BY_PARENT_UUID";

    /** Query key. */
    private static final String C_RESOURCES_SELECT_BY_PATH_PREFIX = "C_RESOURCES_SELECT_BY_PATH_PREFIX";

    /** Query key. */
    private static final String C_RESOURCES_SELECT_BY_PROJECT_LASTMODIFIED = "C_RESOURCES_SELECT_BY_PROJECT_LASTMODIFIED";

    /** Query key. */
    private static final String C_RESOURCES_SELECT_BY_RESOURCE_STATE = "C_RESOURCES_SELECT_BY_RESOURCE_STATE";

    /** Query key. */
    private static final String C_RESOURCES_SELECT_BY_RESOURCE_TYPE = "C_RESOURCES_SELECT_BY_RESOURCE_TYPE";

    /** Query key. */
    private static final String C_RESOURCES_SELECT_ONLY_FILES = "C_RESOURCES_SELECT_ONLY_FILES";

    /** Query key. */
    private static final String C_RESOURCES_SELECT_ONLY_FOLDERS = "C_RESOURCES_SELECT_ONLY_FOLDERS";

    /** Query key. */
    private static final String C_RESOURCES_SELECT_STRUCTURE_ID = "C_RESOURCES_SELECT_STRUCTURE_ID";

    /** Query key. */
    private static final String C_RESOURCES_TRANSFER_RESOURCE = "C_RESOURCES_TRANSFER_RESOURCE";

    /** Query key. */
    private static final String C_RESOURCES_UPDATE_FLAGS = "C_RESOURCES_UPDATE_FLAGS";

    /** Query key. */
    private static final String C_RESOURCES_UPDATE_PROJECT_LASTMODIFIED = "C_RESOURCES_UPDATE_PROJECT_LASTMODIFIED";

    /** Query key. */
    private static final String C_RESOURCES_UPDATE_RELEASE_EXPIRED = "C_RESOURCES_UPDATE_RELEASE_EXPIRED";

    /** Query key. */
    private static final String C_RESOURCES_UPDATE_RESOURCE_PROJECT = "C_RESOURCES_UPDATE_RESOURCE_PROJECT";

    /** Query key. */
    private static final String C_RESOURCES_UPDATE_RESOURCE_STATE = "C_RESOURCES_UPDATE_RESOURCE_STATE";

    /** Query key. */
    private static final String C_RESOURCES_UPDATE_RESOURCE_STATELASTMODIFIED = "C_RESOURCES_UPDATE_RESOURCE_STATELASTMODIFIED";

    /** Query key. */
    private static final String C_RESOURCES_UPDATE_RESOURCE_VERSION = "C_RESOURCES_UPDATE_RESOURCE_VERSION";

    /** Query key. */
    private static final String C_RESOURCES_UPDATE_RESOURCES = "C_RESOURCES_UPDATE_RESOURCES";

    /** Query key. */
    private static final String C_RESOURCES_UPDATE_RESOURCES_WITHOUT_STATE = "C_RESOURCES_UPDATE_RESOURCES_WITHOUT_STATE";

    /** Query key. */
    private static final String C_RESOURCES_UPDATE_SIBLING_COUNT = "C_RESOURCES_UPDATE_SIBLING_COUNT";

    /** Query key. */
    private static final String C_RESOURCES_UPDATE_STRUCTURE = "C_RESOURCES_UPDATE_STRUCTURE";

    /** Query key. */
    private static final String C_RESOURCES_UPDATE_STRUCTURE_STATE = "C_RESOURCES_UPDATE_STRUCTURE_STATE";

    /** Query key. */
    private static final String C_RESOURCES_UPDATE_STRUCTURE_VERSION = "C_RESOURCES_UPDATE_STRUCTURE_VERSION";

    /** Query key. */
    private static final String C_SELECT_NONDELETED_VFS_SIBLINGS = "C_SELECT_NONDELETED_VFS_SIBLINGS";

    /** Query key. */
    private static final String C_SELECT_RESOURCES_FOR_PRINCIPAL_ACE = "C_SELECT_RESOURCES_FOR_PRINCIPAL_ACE";

    /** Query key. */
    private static final String C_SELECT_RESOURCES_FOR_PRINCIPAL_ATTR1 = "C_SELECT_RESOURCES_FOR_PRINCIPAL_ATTR1";

    /** Query key. */
    private static final String C_SELECT_RESOURCES_FOR_PRINCIPAL_ATTR2 = "C_SELECT_RESOURCES_FOR_PRINCIPAL_ATTR2";

    /** Query key. */
    private static final String C_SELECT_VFS_SIBLINGS = "C_SELECT_VFS_SIBLINGS";

    /** Query key. */
    private static final String C_STRUCTURE_DELETE_BY_STRUCTUREID = "C_STRUCTURE_DELETE_BY_STRUCTUREID";

    /** Query key. */
    private static final String C_STRUCTURE_SELECT_BY_DATE_EXPIRED_AFTER = "C_STRUCTURE_SELECT_BY_DATE_EXPIRED_AFTER";

    /** Query key. */
    private static final String C_STRUCTURE_SELECT_BY_DATE_EXPIRED_BEFORE = "C_STRUCTURE_SELECT_BY_DATE_EXPIRED_BEFORE";

    /** Query key. */
    private static final String C_STRUCTURE_SELECT_BY_DATE_RELEASED_AFTER = "C_STRUCTURE_SELECT_BY_DATE_RELEASED_AFTER";

    /** Query key. */
    private static final String C_STRUCTURE_SELECT_BY_DATE_RELEASED_BEFORE = "C_STRUCTURE_SELECT_BY_DATE_RELEASED_BEFORE";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(org.opencms.db.jpa.CmsVfsDriver.class);

    /** The driver manager. */
    protected CmsDriverManager m_driverManager;

    /** 
     * This field is temporarily used to compute the versions during publishing.<p>
     * 
     * @see #publishVersions(CmsDbContext, CmsResource, boolean) 
     */
    protected List<CmsUUID> m_resOp = new ArrayList<CmsUUID>();

    /** The sql manager. */
    protected CmsSqlManager m_sqlManager;

    /**
     * Escapes the database wildcards within the resource path.<p>
     * 
     * This method is required to ensure chars in the resource path that have a special 
     * meaning in SQL (for example "_", which is the "any char" operator) are escaped.<p>
     * 
     * It will escape the following chars: 
     * <ul>
     * <li>"_" to "|_"</li>
     * </ul>
     * 
     * @param path the resource path
     * @return the escaped resource path
     */
    public static String escapeDbWildcard(String path) {

        return CmsStringUtil.substitute(path, "_", "|_");
    }

    /**
     * This method prepares the JPQL conditions for mapping entries for a given URL name mapping filter.<p>
     * 
     * @param filter the filter from which the JPQL conditions should be generated 
     * 
     * @return a pair consisting of an JPQL string and a list of the query parameters for the JPQL 
     */
    public static CmsPair<String, List<I_CmsQueryParameter>> prepareUrlNameMappingConditions(
        CmsUrlNameMappingFilter filter) {

        List<String> sqlConditions = new ArrayList<String>();
        List<I_CmsQueryParameter> parameters = new ArrayList<I_CmsQueryParameter>();
        if (filter.getName() != null) {
            sqlConditions.add("T_CmsDAO%(PROJECT)UrlNameMappings.m_name = ?");
            parameters.add(new CmsQueryStringParameter(filter.getName()));
        }

        if (filter.getStructureId() != null) {
            sqlConditions.add("T_CmsDAO%(PROJECT)UrlNameMappings.m_structureId = ?");
            parameters.add(new CmsQueryStringParameter(filter.getStructureId().toString()));
        }

        if (filter.getNamePattern() != null) {
            sqlConditions.add("T_CmsDAO%(PROJECT)UrlNameMappings.m_name LIKE ? ");
            parameters.add(new CmsQueryStringParameter(filter.getNamePattern()));
        }

        if (filter.getState() != null) {
            sqlConditions.add("T_CmsDAO%(PROJECT)UrlNameMappings.m_state = ?");
            parameters.add(new CmsQueryIntParameter(filter.getState().intValue()));
        }

        if (filter.getRejectStructureId() != null) {
            sqlConditions.add("T_CmsDAO%(PROJECT)UrlNameMappings.m_structureId <> ? ");
            parameters.add(new CmsQueryStringParameter(filter.getRejectStructureId().toString()));
        }

        if (filter.getLocale() != null) {
            sqlConditions.add("T_CmsDAO%(PROJECT)UrlNameMappings.m_locale = ? ");
            parameters.add(new CmsQueryStringParameter(filter.getLocale()));
        }

        String conditionString = CmsStringUtil.listAsString(sqlConditions, " AND ");
        return CmsPair.create(conditionString, parameters);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#addUrlNameMappingEntry(org.opencms.db.CmsDbContext, boolean, org.opencms.db.urlname.CmsUrlNameMappingEntry)
     */
    public void addUrlNameMappingEntry(CmsDbContext dbc, boolean online, CmsUrlNameMappingEntry entry) {

        I_CmsDAOUrlNameMappings m = online ? new CmsDAOOnlineUrlNameMappings() : new CmsDAOOfflineUrlNameMappings();

        m.setName(entry.getName());
        m.setStructureId(entry.getStructureId().toString());
        m.setState(entry.getState());
        m.setDateChanged(entry.getDateChanged());
        m.setLocale(entry.getLocale());
        m_sqlManager.persist(dbc, m);

    }

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
    public int countSiblings(CmsDbContext dbc, CmsUUID projectId, CmsUUID resourceId) throws CmsDataAccessException {

        int count = 0;

        try {
            Query q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_COUNT_SIBLINGS);
            q.setParameter(1, resourceId.toString());
            try {
                count = CmsDataTypeUtil.numberToInt((Number)q.getSingleResult());
            } catch (NoResultException e) {
                // do nothing
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        return count;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createContent(CmsDbContext, CmsUUID, CmsUUID, byte[])
     */
    public void createContent(CmsDbContext dbc, CmsUUID projectId, CmsUUID resourceId, byte[] content)
    throws CmsDataAccessException {

        try {
            CmsDAOOfflineContents oc = new CmsDAOOfflineContents();

            oc.setResourceId(resourceId.toString());
            oc.setFileContent(content);

            m_sqlManager.persist(dbc, oc);
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }
    }

    /**
     * Creates a {@link CmsFile} instance from a jpa ResultSet.<p>
     * 
     * @param o the jpa ResultSet
     * @param projectId the project id
     * @param hasFileContentInResultSet flag to include the file content
     * 
     * @return the created file
     */
    public CmsFile createFile(Object[] o, CmsUUID projectId, boolean hasFileContentInResultSet) {

        I_CmsDAOResources r = (I_CmsDAOResources)o[0];
        I_CmsDAOStructure s = (I_CmsDAOStructure)o[1];
        String lockedInProjectParameter = (String)o[2];

        byte[] content = null;

        CmsUUID resProjectId = null;

        CmsUUID structureId = new CmsUUID(s.getStructureId());
        CmsUUID resourceId = new CmsUUID(r.getResourceId());
        String resourcePath = s.getResourcePath();
        int resourceType = r.getResourceType();
        int resourceFlags = r.getResourceFlags();
        int resourceState = r.getResourceState();
        int structureState = s.getStructureState();
        long dateCreated = r.getDateCreated();
        long dateLastModified = r.getDateLastModified();
        long dateReleased = s.getDateReleased();
        long dateExpired = s.getDateExpired();
        int resourceSize = r.getResourceSize();
        CmsUUID userCreated = new CmsUUID(r.getUserCreated());
        CmsUUID userLastModified = new CmsUUID(r.getUserLastModified());
        CmsUUID lockedInProject = new CmsUUID(lockedInProjectParameter);
        int siblingCount = r.getSiblingCount();
        long dateContent = r.getDateContent();
        int resourceVersion = r.getResourceVersion();
        int structureVersion = s.getStructureVersion();

        // in case of folder type ensure, that the root path has a trailing slash
        if (CmsFolder.isFolderType(resourceType)) {
            resourcePath = CmsFileUtil.addTrailingSeparator(resourcePath);
        }
        if (hasFileContentInResultSet) {
            //content = m_sqlManager.getBytes(res, m_sqlManager.readQuery("C_RESOURCES_FILE_CONTENT"));
            throw new RuntimeException(
                "CCmsVfsDriver: public CmsFile createFile(Object[] o, CmsUUID projectId, boolean hasFileContentInResultSet) throws SQLException ");
        }
        resProjectId = lockedInProject;
        int newState = (structureState > resourceState) ? structureState : resourceState;

        return new CmsFile(
            structureId,
            resourceId,
            resourcePath,
            resourceType,
            resourceFlags,
            resProjectId,
            CmsResourceState.valueOf(newState),
            dateCreated,
            userCreated,
            dateLastModified,
            userLastModified,
            dateReleased,
            dateExpired,
            siblingCount,
            resourceSize,
            dateContent,
            resourceVersion + structureVersion,
            content);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createFile(java.sql.ResultSet, org.opencms.util.CmsUUID)
     */
    public CmsFile createFile(ResultSet res, CmsUUID projectId) {

        LOG.error("This method is not implemented!");
        return null;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createFile(java.sql.ResultSet, org.opencms.util.CmsUUID, boolean)
     */
    public CmsFile createFile(ResultSet res, CmsUUID projectId, boolean hasFileContentInResultSet) {

        LOG.error("This method is not implemented!");
        return null;
    }

    /**
     * Creates a {@link CmsFolder} instance from a jpa ResultSet.<p>
     * 
     * @param o the JDBC ResultSet
     * @param projectId the ID of the current project
     * @param hasProjectIdInResultSet true if the SQL select query includes the PROJECT_ID table attribute
     * 
     * @return the created folder
     */
    public CmsFolder createFolder(Object[] o, CmsUUID projectId, boolean hasProjectIdInResultSet) {

        I_CmsDAOResources r = (I_CmsDAOResources)o[0];
        I_CmsDAOStructure s = (I_CmsDAOStructure)o[1];
        String lockedInProjectParameter = (String)o[2];

        CmsUUID structureId = new CmsUUID(s.getStructureId());
        CmsUUID resourceId = new CmsUUID(r.getResourceId());
        String resourcePath = s.getResourcePath();
        int resourceType = r.getResourceType();
        int resourceFlags = r.getResourceFlags();
        int resourceState = r.getResourceState();
        int structureState = s.getStructureState();
        long dateCreated = r.getDateCreated();
        long dateLastModified = r.getDateLastModified();
        long dateReleased = s.getDateReleased();
        long dateExpired = s.getDateExpired();
        CmsUUID userCreated = new CmsUUID(r.getUserCreated());
        CmsUUID userLastModified = new CmsUUID(r.getUserLastModified());
        CmsUUID resProjectId = new CmsUUID(lockedInProjectParameter);
        int resourceVersion = r.getResourceVersion();
        int structureVersion = s.getStructureVersion();
        int resourceSize = r.getResourceSize();

        // in case of folder type ensure, that the root path has a trailing slash
        if (CmsFolder.isFolderSize(resourceSize)) {
            resourcePath = CmsFileUtil.addTrailingSeparator(resourcePath);
        }

        int newState = (structureState > resourceState) ? structureState : resourceState;

        return new CmsFolder(
            structureId,
            resourceId,
            resourcePath,
            resourceType,
            resourceFlags,
            resProjectId,
            CmsResourceState.valueOf(newState),
            dateCreated,
            userCreated,
            dateLastModified,
            userLastModified,
            dateReleased,
            dateExpired,
            resourceVersion + structureVersion);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createFolder(java.sql.ResultSet, org.opencms.util.CmsUUID, boolean)
     */
    public CmsFolder createFolder(ResultSet res, CmsUUID projectId, boolean hasProjectIdInResultSet) {

        LOG.error("This method is not implemented!");
        return null;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createOnlineContent(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, byte[], int, boolean, boolean)
     */
    public void createOnlineContent(
        CmsDbContext dbc,
        CmsUUID resourceId,
        byte[] contents,
        int publishTag,
        boolean keepOnline,
        boolean needToUpdateContent) throws CmsDataAccessException {

        try {
            boolean dbcHasProjectId = (dbc.getProjectId() != null) && !dbc.getProjectId().isNullUUID();

            if (needToUpdateContent || dbcHasProjectId) {
                if (dbcHasProjectId || !OpenCms.getSystemInfo().isHistoryEnabled()) {
                    // remove the online content for this resource id
                    Query q = m_sqlManager.createQuery(dbc, "C_ONLINE_CONTENTS_DELETE");
                    q.setParameter(1, resourceId.toString());
                    q.executeUpdate();
                } else {
                    // put the online content in the history, only if explicit requested
                    Query q = m_sqlManager.createQuery(dbc, "C_ONLINE_CONTENTS_HISTORY");
                    q.setParameter(1, resourceId.toString());
                    @SuppressWarnings("unchecked")
                    List<CmsDAOContents> res = q.getResultList();
                    for (CmsDAOContents c : res) {
                        c.setOnlineFlag(0);
                    }
                }

                // create new online content
                CmsDAOContents c = new CmsDAOContents();

                c.setResourceId(resourceId.toString());
                c.setFileContent(contents);
                c.setPublishTagFrom(publishTag);
                c.setPublishTagTo(publishTag);
                c.setOnlineFlag(keepOnline ? 1 : 0);

                m_sqlManager.persist(dbc, c);
            } else {
                // update old content entry
                Query q = m_sqlManager.createQuery(dbc, C_HISTORY_CONTENTS_UPDATE);
                q.setParameter(1, resourceId.toString());
                @SuppressWarnings("unchecked")
                List<CmsDAOContents> res = q.getResultList();
                for (CmsDAOContents c : res) {
                    c.setPublishTagTo(publishTag);
                }

                if (!keepOnline) {
                    // put the online content in the history 
                    q = m_sqlManager.createQuery(dbc, C_ONLINE_CONTENTS_HISTORY);
                    q.setParameter(1, resourceId.toString());
                    @SuppressWarnings("unchecked")
                    List<CmsDAOContents> res1 = q.getResultList();
                    for (CmsDAOContents c : res1) {
                        c.setOnlineFlag(0);
                    }
                }
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createPropertyDefinition(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, java.lang.String, org.opencms.file.CmsPropertyDefinition.CmsPropertyType)
     */
    public CmsPropertyDefinition createPropertyDefinition(
        CmsDbContext dbc,
        CmsUUID projectId,
        String name,
        CmsPropertyDefinition.CmsPropertyType type) throws CmsDataAccessException {

        try {
            I_CmsDAOPropertyDef pd = CmsProject.isOnlineProject(projectId)
            ? new CmsDAOOnlinePropertyDef()
            : new CmsDAOOfflinePropertyDef();

            pd.setPropertyDefId(new CmsUUID().toString());
            pd.setPropertyDefName(name);
            pd.setPropertyDefType(type.getMode());

            m_sqlManager.persist(dbc, pd);
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        return readPropertyDefinition(dbc, name, projectId);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createRelation(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.relations.CmsRelation)
     */
    public void createRelation(CmsDbContext dbc, CmsUUID projectId, CmsRelation relation) throws CmsDataAccessException {

        try {
            I_CmsDAOResourceRelations rr = CmsProject.isOnlineProject(projectId)
            ? new CmsDAOOnlineResourceRelations()
            : new CmsDAOOfflineResourceRelations();

            rr.setRelationSourceId(relation.getSourceId().toString());
            rr.setRelationSourcePath(relation.getSourcePath());
            rr.setRelationTargetId(relation.getTargetId().toString());
            rr.setRelationTargetPath(relation.getTargetPath());
            rr.setRelationType(relation.getType().getId());

            m_sqlManager.persist(dbc, rr);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_CREATE_RELATION_2,
                    String.valueOf(projectId),
                    relation));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createResource(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.file.CmsResource, byte[])
     */
    public CmsResource createResource(CmsDbContext dbc, CmsUUID projectId, CmsResource resource, byte[] content)
    throws CmsDataAccessException {

        CmsUUID newStructureId = null;

        // check the resource path
        String resourcePath = CmsFileUtil.removeTrailingSeparator(resource.getRootPath());
        if (resourcePath.length() > CmsDriverManager.MAX_VFS_RESOURCE_PATH_LENGTH) {
            throw new CmsDataAccessException(Messages.get().container(
                Messages.ERR_RESOURCENAME_TOO_LONG_2,
                resourcePath,
                new Integer(CmsDriverManager.MAX_VFS_RESOURCE_PATH_LENGTH)));
        }

        // check if the parent folder of the resource exists and if is not deleted
        if (!resource.getRootPath().equals("/")) {
            String parentFolderName = CmsResource.getParentFolder(resource.getRootPath());
            CmsFolder parentFolder = m_driverManager.getVfsDriver(dbc).readFolder(dbc, projectId, parentFolderName);
            if (parentFolder.getState().isDeleted()) {
                throw new CmsDbEntryNotFoundException(Messages.get().container(
                    Messages.ERR_PARENT_FOLDER_DELETED_1,
                    resource.getRootPath()));
            }
        }

        // validate the resource length
        internalValidateResourceLength(resource);

        // set the resource state and modification dates
        CmsResourceState newState;
        long dateModified;
        long dateCreated;
        long dateContent = System.currentTimeMillis();

        if (projectId.equals(CmsProject.ONLINE_PROJECT_ID)) {
            newState = CmsResource.STATE_UNCHANGED;
            dateCreated = resource.getDateCreated();
            dateModified = resource.getDateLastModified();
        } else {
            newState = CmsResource.STATE_NEW;
            if (resource.isTouched()) {
                dateCreated = resource.getDateCreated();
                dateModified = resource.getDateLastModified();
            } else {
                dateCreated = System.currentTimeMillis();
                dateModified = dateCreated;
            }
        }

        // check if the resource already exists
        newStructureId = resource.getStructureId();

        try {
            CmsResource existingResource = m_driverManager.getVfsDriver().readResource(
                dbc,
                dbc.getProjectId().isNullUUID() ? projectId : dbc.getProjectId(),
                resourcePath,
                true);
            if (existingResource.getState().isDeleted()) {
                // if an existing resource is deleted, it will be finally removed now.
                // but we have to reuse its id in order to avoid orphans in the online project
                newStructureId = existingResource.getStructureId();
                newState = CmsResource.STATE_CHANGED;

                // remove the existing file and it's properties
                List<CmsResource> modifiedResources = m_driverManager.getVfsDriver(dbc).readSiblings(
                    dbc,
                    projectId,
                    existingResource,
                    false);
                int propertyDeleteOption = (existingResource.getSiblingCount() > 1)
                ? CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_VALUES
                : CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES;
                deletePropertyObjects(dbc, projectId, existingResource, propertyDeleteOption);
                removeFile(dbc, projectId, existingResource);

                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCES_MODIFIED,
                    Collections.<String, Object> singletonMap(I_CmsEventListener.KEY_RESOURCES, modifiedResources)));
                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                    Collections.<String, Object> singletonMap(I_CmsEventListener.KEY_RESOURCE, existingResource)));
            } else {
                // we have a collision: there exists already a resource with the same path/name which cannot be removed
                throw new CmsVfsResourceAlreadyExistsException(Messages.get().container(
                    Messages.ERR_RESOURCE_WITH_NAME_ALREADY_EXISTS_1,
                    dbc.removeSiteRoot(resource.getRootPath())));
            }
        } catch (CmsVfsResourceNotFoundException e) {
            // that's what we want in the best case- anything else should be thrown
        }

        try {
            // read the parent id
            String parentId = internalReadParentId(dbc, projectId, resourcePath);

            // use consistent version numbers if the file is being restored
            int lastVersion = m_driverManager.getHistoryDriver(dbc).readLastVersion(dbc, newStructureId);
            int newStrVersion = 0;
            int newResVersion = 0;
            if (lastVersion > 0) {
                I_CmsHistoryResource histRes = m_driverManager.getHistoryDriver(dbc).readResource(
                    dbc,
                    newStructureId,
                    lastVersion);
                newStrVersion = histRes.getStructureVersion();
                newResVersion = histRes.getResourceVersion();
            }

            // write the structure
            I_CmsDAOStructure s = CmsProject.isOnlineProject(projectId)
            ? new CmsDAOOnlineStructure()
            : new CmsDAOOfflineStructure();

            s.setStructureId(newStructureId.toString());
            s.setResourceId(resource.getResourceId().toString());
            s.setResourcePath(resourcePath);
            s.setStructureState(newState.getState());
            s.setDateReleased(resource.getDateReleased());
            s.setDateExpired(resource.getDateExpired());
            s.setParentId(parentId);
            s.setStructureVersion(newStrVersion);

            m_sqlManager.persist(dbc, s);

            if (!validateResourceIdExists(dbc, projectId, resource.getResourceId())) {

                // create the resource record
                I_CmsDAOResources r = CmsProject.isOnlineProject(projectId)
                ? new CmsDAOOnlineResources()
                : new CmsDAOOfflineResources();
                r.setResourceId(resource.getResourceId().toString());
                r.setResourceType(resource.getTypeId());
                r.setResourceFlags(resource.getFlags());
                r.setDateCreated(dateCreated);
                r.setUserCreated(resource.getUserCreated().toString());
                r.setDateLastModified(dateModified);
                r.setUserLastModified(resource.getUserLastModified().toString());
                r.setResourceState(newState.getState());
                r.setResourceSize(resource.getLength());
                r.setDateContent(dateContent);
                r.setProjectLastModified(projectId.toString());
                r.setSiblingCount(1);
                r.setResourceVersion(newResVersion);

                m_sqlManager.persist(dbc, r);

                if (resource.isFile() && (content != null)) {
                    // create the file content
                    createContent(dbc, projectId, resource.getResourceId(), content);
                }
            } else {
                if ((content != null) || !resource.getState().isKeep()) {
                    CmsUUID projLastMod = projectId;
                    CmsResourceState state = CmsResource.STATE_CHANGED;
                    if (projectId.equals(CmsProject.ONLINE_PROJECT_ID)) {
                        // in case a sibling is being published
                        projLastMod = resource.getProjectLastModified();
                        state = CmsResource.STATE_UNCHANGED;
                    }
                    // update the resource record only if state has changed or new content is provided
                    Query q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_UPDATE_RESOURCES);

                    q.setParameter(1, resource.getResourceId().toString());

                    @SuppressWarnings("unchecked")
                    List<I_CmsDAOResources> res = q.getResultList();
                    for (I_CmsDAOResources r : res) {
                        r.setResourceType(resource.getTypeId());
                        r.setResourceFlags(resource.getFlags());
                        r.setDateLastModified(dateModified);
                        r.setUserLastModified(resource.getUserLastModified().toString());
                        r.setResourceState(state.getState());
                        r.setResourceSize(resource.getLength());
                        r.setDateContent(resource.getDateContent());
                        r.setProjectLastModified(projLastMod.toString());
                        r.setSiblingCount(countSiblings(dbc, projectId, resource.getResourceId()));
                    }

                }

                if (resource.isFile()) {
                    if (content != null) {
                        // update the file content
                        writeContent(dbc, resource.getResourceId(), content);
                    } else if (resource.getState().isKeep()) {
                        // special case sibling creation - update the link Count
                        Query q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_UPDATE_SIBLING_COUNT);
                        q.setParameter(1, resource.getResourceId().toString());
                        @SuppressWarnings("unchecked")
                        List<I_CmsDAOResources> res = q.getResultList();

                        for (I_CmsDAOResources r : res) {
                            r.setSiblingCount(countSiblings(dbc, projectId, resource.getResourceId()));
                        }

                        // update the resource flags
                        q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_UPDATE_FLAGS);
                        q.setParameter(1, resource.getResourceId().toString());
                        @SuppressWarnings("unchecked")
                        List<I_CmsDAOResources> resf = q.getResultList();

                        for (I_CmsDAOResources r : resf) {
                            r.setResourceFlags(resource.getFlags());
                        }
                    }
                }
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }
        repairBrokenRelations(dbc, projectId, resource.getStructureId(), resource.getRootPath());
        return readResource(dbc, projectId, newStructureId, false);
    }

    /**
     * Creates a CmsResource instance from a jpa ResultSet.<p>
     * 
     * @param o the jpa ResultSet
     * @param projectId the ID of the current project to adjust the modification date in case the resource is a VFS link
     * 
     * @return the created resource
     */
    public CmsResource createResource(Object[] o, CmsUUID projectId) {

        I_CmsDAOResources r = (I_CmsDAOResources)o[0];
        I_CmsDAOStructure s = (I_CmsDAOStructure)o[1];

        CmsUUID structureId = new CmsUUID(s.getStructureId());
        CmsUUID resourceId = new CmsUUID(r.getResourceId());
        String resourcePath = s.getResourcePath();
        int resourceType = r.getResourceType();
        int resourceFlags = r.getResourceFlags();
        CmsUUID resourceProjectLastModified = new CmsUUID(r.getProjectLastModified());
        int resourceState = r.getResourceState();
        int structureState = s.getStructureState();
        long dateCreated = r.getDateCreated();
        long dateLastModified = r.getDateLastModified();
        long dateReleased = s.getDateReleased();
        long dateExpired = s.getDateExpired();
        int resourceSize = r.getResourceSize();
        boolean isFolder = CmsFolder.isFolderSize(resourceSize);
        if (isFolder) {
            // in case of folder type ensure, that the root path has a trailing slash
            resourcePath = CmsFileUtil.addTrailingSeparator(resourcePath);
        }
        long dateContent = isFolder ? -1 : r.getDateContent();
        CmsUUID userCreated = new CmsUUID(r.getUserCreated());
        CmsUUID userLastModified = new CmsUUID(r.getUserLastModified());
        int siblingCount = r.getSiblingCount();
        int resourceVersion = r.getResourceVersion();
        int structureVersion = s.getStructureVersion();

        int newState = (structureState > resourceState) ? structureState : resourceState;
        // if there is a change increase the version number
        int newVersion = resourceVersion + structureVersion + (newState > 0 ? 1 : 0);

        CmsResource newResource = new CmsResource(
            structureId,
            resourceId,
            resourcePath,
            resourceType,
            isFolder,
            resourceFlags,
            resourceProjectLastModified,
            CmsResourceState.valueOf(newState),
            dateCreated,
            userCreated,
            dateLastModified,
            userLastModified,
            dateReleased,
            dateExpired,
            siblingCount,
            resourceSize,
            dateContent,
            newVersion);

        return newResource;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createResource(java.sql.ResultSet, org.opencms.util.CmsUUID)
     */
    public CmsResource createResource(ResultSet res, CmsUUID projectId) {

        LOG.error("This method is not implemented!");
        return null;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#createSibling(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource)
     */
    public void createSibling(CmsDbContext dbc, CmsProject project, CmsResource resource) throws CmsDataAccessException {

        if (!project.getUuid().equals(CmsProject.ONLINE_PROJECT_ID)) {
            // this method is only intended to be used during publishing
            return;
        }

        // check if the resource already exists
        CmsResource existingSibling = null;
        CmsUUID newStructureId = resource.getStructureId();

        try {
            existingSibling = readResource(dbc, project.getUuid(), resource.getRootPath(), true);

            if (existingSibling.getState().isDeleted()) {
                // if an existing resource is deleted, it will be finally removed now.
                // but we have to reuse its id in order to avoid orphans in the online project.
                newStructureId = existingSibling.getStructureId();

                // remove the existing file and it's properties
                List<CmsResource> modifiedResources = readSiblings(dbc, project.getUuid(), existingSibling, false);
                int propertyDeleteOption = (existingSibling.getSiblingCount() > 1)
                ? CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_VALUES
                : CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES;
                deletePropertyObjects(dbc, project.getUuid(), existingSibling, propertyDeleteOption);
                removeFile(dbc, project.getUuid(), existingSibling);

                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCES_MODIFIED,
                    Collections.<String, Object> singletonMap(I_CmsEventListener.KEY_RESOURCES, modifiedResources)));
                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED,
                    Collections.<String, Object> singletonMap(I_CmsEventListener.KEY_RESOURCE, existingSibling)));
            } else {
                // we have a collision: there exists already a resource with the same path/name which could not be removed
                throw new CmsVfsResourceAlreadyExistsException(Messages.get().container(
                    Messages.ERR_RESOURCE_WITH_NAME_ALREADY_EXISTS_1,
                    dbc.removeSiteRoot(resource.getRootPath())));
            }
        } catch (CmsVfsResourceNotFoundException e) {
            // that's what we want in the best case- anything else should be thrown
        }

        // check if a resource with the specified ID already exists
        if (!validateResourceIdExists(dbc, project.getUuid(), resource.getResourceId())) {
            throw new CmsVfsResourceNotFoundException(Messages.get().container(
                Messages.ERR_CREATE_SIBLING_FILE_NOT_FOUND_1,
                dbc.removeSiteRoot(resource.getRootPath())));
        }

        // write a new structure referring to the resource
        try {
            // use consistent version numbers if the file is being restored
            int lastVersion = m_driverManager.getHistoryDriver(dbc).readLastVersion(dbc, newStructureId);
            int newStrVersion = 0;
            if (lastVersion > 0) {
                I_CmsHistoryResource histRes = m_driverManager.getHistoryDriver(dbc).readResource(
                    dbc,
                    newStructureId,
                    lastVersion);
                newStrVersion = histRes.getStructureVersion();
            }

            // read the parent id
            String parentId = internalReadParentId(dbc, project.getUuid(), resource.getRootPath());

            // write the structure
            I_CmsDAOStructure s = CmsProject.isOnlineProject(project.getUuid())
            ? new CmsDAOOnlineStructure()
            : new CmsDAOOfflineStructure();

            s.setStructureId(newStructureId.toString());
            s.setResourceId(resource.getResourceId().toString());
            s.setResourcePath(resource.getRootPath());
            s.setStructureState(CmsResource.STATE_UNCHANGED.getState());
            s.setDateReleased(resource.getDateReleased());
            s.setDateExpired(resource.getDateExpired());
            s.setParentId(parentId);
            s.setStructureVersion(newStrVersion);

            m_sqlManager.persist(dbc, s);

            // update the link Count
            Query q = m_sqlManager.createQuery(dbc, project, C_RESOURCES_UPDATE_SIBLING_COUNT);
            q.setParameter(1, resource.getResourceId().toString());
            @SuppressWarnings("unchecked")
            List<I_CmsDAOResources> res = q.getResultList();

            for (I_CmsDAOResources r : res) {
                r.setSiblingCount(countSiblings(dbc, project.getUuid(), resource.getResourceId()));
            }

            // update the project last modified and flags
            q = m_sqlManager.createQuery(dbc, project, C_RESOURCES_UPDATE_RESOURCE_PROJECT);
            q.setParameter(1, resource.getResourceId().toString());
            @SuppressWarnings("unchecked")
            List<I_CmsDAOResources> resr = q.getResultList();

            for (I_CmsDAOResources r : resr) {
                r.setResourceFlags(resource.getFlags());
                r.setProjectLastModified(resource.getProjectLastModified().toString());
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        repairBrokenRelations(dbc, project.getUuid(), resource.getStructureId(), resource.getRootPath());
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#deletePropertyDefinition(org.opencms.db.CmsDbContext, org.opencms.file.CmsPropertyDefinition)
     */
    public void deletePropertyDefinition(CmsDbContext dbc, CmsPropertyDefinition metadef) throws CmsDataAccessException {

        try {
            if ((internalCountProperties(dbc, metadef, CmsProject.ONLINE_PROJECT_ID) != 0)
                || (internalCountProperties(dbc, metadef, CmsUUID.getOpenCmsUUID()) != 0)) { // HACK: to get an offline project

                throw new CmsDataAccessException(Messages.get().container(
                    Messages.ERR_DELETE_USED_PROPERTY_1,
                    metadef.getName()));
            }
            Query q;
            for (int i = 0; i < 2; i++) {
                if (i == 0) {
                    // delete the offline property definition
                    q = m_sqlManager.createQuery(dbc, CmsUUID.getOpenCmsUUID(), C_PROPERTYDEF_DELETE); // HACK: to get an offline project
                } else {
                    // delete the online property definition
                    q = m_sqlManager.createQuery(dbc, CmsProject.ONLINE_PROJECT_ID, C_PROPERTYDEF_DELETE);
                }

                q.setParameter(1, metadef.getId().toString());
                @SuppressWarnings("unchecked")
                List<I_CmsDAOPropertyDef> res = q.getResultList();
                for (I_CmsDAOPropertyDef pd : res) {
                    m_sqlManager.remove(dbc, pd);
                }
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#deletePropertyObjects(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.file.CmsResource, int)
     */
    public void deletePropertyObjects(CmsDbContext dbc, CmsUUID projectId, CmsResource resource, int deleteOption)
    throws CmsDataAccessException {

        try {
            Query q;

            if (deleteOption == CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_AND_RESOURCE_VALUES) {
                // delete both the structure and resource property values mapped to the specified resource
                q = m_sqlManager.createQuery(dbc, projectId, C_PROPERTIES_DELETE_ALL_STRUCTURE_AND_RESOURCE_VALUES);
                q.setParameter(1, resource.getResourceId().toString());
                q.setParameter(2, Integer.valueOf(CmsProperty.RESOURCE_RECORD_MAPPING));
                q.setParameter(3, String.valueOf(resource.getStructureId()));
                q.setParameter(4, Integer.valueOf(CmsProperty.STRUCTURE_RECORD_MAPPING));
            } else if (deleteOption == CmsProperty.DELETE_OPTION_DELETE_STRUCTURE_VALUES) {
                // delete the structure values mapped to the specified resource
                q = m_sqlManager.createQuery(dbc, projectId, C_PROPERTIES_DELETE_ALL_VALUES_FOR_MAPPING_TYPE);
                q.setParameter(1, resource.getStructureId().toString());
                q.setParameter(2, Integer.valueOf(CmsProperty.STRUCTURE_RECORD_MAPPING));
            } else if (deleteOption == CmsProperty.DELETE_OPTION_DELETE_RESOURCE_VALUES) {
                // delete the resource property values mapped to the specified resource
                q = m_sqlManager.createQuery(dbc, projectId, C_PROPERTIES_DELETE_ALL_VALUES_FOR_MAPPING_TYPE);
                q.setParameter(1, resource.getResourceId().toString());
                q.setParameter(2, Integer.valueOf(CmsProperty.RESOURCE_RECORD_MAPPING));
            } else {
                throw new CmsDataAccessException(Messages.get().container(Messages.ERR_INVALID_DELETE_OPTION_1));
            }

            @SuppressWarnings("unchecked")
            List<I_CmsDAOProperties> res = q.getResultList();
            for (I_CmsDAOProperties p : res) {
                m_sqlManager.remove(dbc, p);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#deleteRelations(org.opencms.db.CmsDbContext, CmsUUID, CmsResource, org.opencms.relations.CmsRelationFilter)
     */
    public void deleteRelations(CmsDbContext dbc, CmsUUID projectId, CmsResource resource, CmsRelationFilter filter)
    throws CmsDataAccessException {

        try {
            if (filter.isSource()) {
                List params = new ArrayList(7);

                StringBuffer queryBuf = new StringBuffer(256);
                queryBuf.append(m_sqlManager.readQuery(projectId, C_DELETE_RELATIONS));
                queryBuf.append(prepareRelationConditions(projectId, filter, resource, params, true));

                Query q = m_sqlManager.createQueryFromJPQL(dbc, queryBuf.toString());
                for (int i = 0; i < params.size(); i++) {
                    q.setParameter(i + 1, params.get(i));
                }
                @SuppressWarnings("unchecked")
                List<I_CmsDAOResourceRelations> res = q.getResultList();
                for (I_CmsDAOResourceRelations rr : res) {
                    m_sqlManager.remove(dbc, rr);
                }
            }
            if (filter.isTarget()) {
                List params = new ArrayList(7);

                StringBuffer queryBuf = new StringBuffer(256);
                queryBuf.append(m_sqlManager.readQuery(projectId, C_DELETE_RELATIONS));
                queryBuf.append(prepareRelationConditions(projectId, filter, resource, params, false));

                Query q = m_sqlManager.createQueryFromJPQL(dbc, queryBuf.toString());
                for (int i = 0; i < params.size(); i++) {
                    q.setParameter(i + 1, params.get(i));
                }

                @SuppressWarnings("unchecked")
                List<I_CmsDAOResourceRelations> res = q.getResultList();
                for (I_CmsDAOResourceRelations rr : res) {
                    m_sqlManager.remove(dbc, rr);
                }
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }
        // update broken remaining relations
        updateBrokenRelations(dbc, projectId, resource.getRootPath());
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#deleteUrlNameMappingEntries(org.opencms.db.CmsDbContext, boolean, org.opencms.db.urlname.CmsUrlNameMappingFilter)
     */
    public void deleteUrlNameMappingEntries(CmsDbContext dbc, boolean online, CmsUrlNameMappingFilter filter)
    throws CmsDataAccessException {

        try {
            String query = m_sqlManager.readQuery(C_DELETE_URLNAME_MAPPINGS);
            query = replaceProject(query, online);
            Query q = getQueryForFilter(dbc, query, filter, online);
            @SuppressWarnings("unchecked")
            List<I_CmsDAOUrlNameMappings> res = q.getResultList();

            for (I_CmsDAOUrlNameMappings m : res) {
                m_sqlManager.remove(dbc, m);
            }

        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#destroy()
     */
    public void destroy() throws Throwable {

        m_sqlManager = null;
        m_driverManager = null;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SHUTDOWN_DRIVER_1, getClass().getName()));
        }
    }

    /**
     * Returns all organizational units for the given resource.<p>
     * 
     * @param dbc the database context
     * @param projectId the id of the project
     * @param resource the resource
     * 
     * @return a list of {@link org.opencms.security.CmsOrganizationalUnit} objects
     * 
     * @throws CmsDataAccessException 
     */
    public List<CmsOrganizationalUnit> getResourceOus(CmsDbContext dbc, CmsUUID projectId, CmsResource resource)
    throws CmsDataAccessException {

        List<CmsOrganizationalUnit> ous = new ArrayList<CmsOrganizationalUnit>();
        String resName = resource.getRootPath();
        if (resource.isFolder() && !resName.endsWith("/")) {
            resName += "/";
        }

        try {
            Query q = m_sqlManager.createQuery(dbc, projectId, C_READ_RESOURCE_OUS);
            q.setParameter(1, Integer.valueOf(CmsRelationType.OU_RESOURCE.getId()));
            @SuppressWarnings("unchecked")
            List<I_CmsDAOResourceRelations> res = internalResourceOus(q.getResultList(), resName);
            for (I_CmsDAOResourceRelations rr : res) {
                CmsRelation rel = internalReadRelation(rr);
                try {
                    ous.add(m_driverManager.readOrganizationalUnit(
                        dbc,
                        rel.getSourcePath().substring(CmsUserDriver.ORGUNIT_BASE_FOLDER.length())));
                } catch (CmsException e) {
                    // should never happen
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }
        return ous;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#getSqlManager()
     */
    public CmsSqlManager getSqlManager() {

        return m_sqlManager;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#incrementCounter(org.opencms.db.CmsDbContext, java.lang.String)
     */
    public int incrementCounter(CmsDbContext dbc, String name) {

        CmsDAOCounters c = m_sqlManager.find(dbc, CmsDAOCounters.class, name);
        int result = 0;

        if (c != null) {
            result = c.getCounter();
            c.setCounter(c.getCounter() + 1);
        } else {
            c = new CmsDAOCounters();
            c.setName(name);
            c.setCounter(1);
            m_sqlManager.persist(dbc, c);
        }

        return result;
    }

    /**
     * @see org.opencms.db.I_CmsDriver#init(org.opencms.db.CmsDbContext, org.opencms.configuration.CmsConfigurationManager, java.util.List, org.opencms.db.CmsDriverManager)
     */
    public void init(
        CmsDbContext dbc,
        CmsConfigurationManager configurationManager,
        List<String> successiveDrivers,
        CmsDriverManager driverManager) {

        CmsParameterConfiguration config = configurationManager.getConfiguration();

        String poolUrl = config.get("db.vfs.pool");
        String classname = config.get("db.vfs.sqlmanager");

        m_sqlManager = this.initSqlManager(classname);

        m_driverManager = driverManager;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_ASSIGNED_POOL_1, poolUrl));
        }

        if ((successiveDrivers != null) && !successiveDrivers.isEmpty()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(
                    Messages.LOG_SUCCESSIVE_DRIVERS_UNSUPPORTED_1,
                    getClass().getName()));
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#initSqlManager(String)
     */
    public CmsSqlManager initSqlManager(String classname) {

        return CmsSqlManager.getInstance(classname);
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#moveResource(CmsDbContext, CmsUUID, CmsResource, String)
     */
    public void moveResource(CmsDbContext dbc, CmsUUID projectId, CmsResource source, String destinationPath)
    throws CmsDataAccessException {

        if ((dbc.getRequestContext() != null)
            && (dbc.getRequestContext().getAttribute(REQ_ATTR_CHECK_PERMISSIONS) != null)) {
            // only check write permissions
            checkWritePermissionsInFolder(dbc, source);
            return;
        }

        // determine destination folder        
        String destinationFoldername = CmsResource.getParentFolder(destinationPath);

        // read the destination folder (will also check read permissions)
        CmsFolder destinationFolder = m_driverManager.readFolder(dbc, destinationFoldername, CmsResourceFilter.ALL);

        if (!projectId.equals(CmsProject.ONLINE_PROJECT_ID)) {
            // check online resource
            try {
                CmsResource onlineResource = m_driverManager.getVfsDriver(dbc).readResource(
                    dbc,
                    CmsProject.ONLINE_PROJECT_ID,
                    destinationPath,
                    true);

                if (!onlineResource.getStructureId().equals(source.getStructureId())) {
                    // source resource has been moved and it is not the 
                    // same as the resource that is being trying to move back
                    CmsResource offlineResource = null;
                    try {
                        // read new location in offline project
                        offlineResource = readResource(
                            dbc,
                            dbc.getRequestContext().getCurrentProject().getUuid(),
                            onlineResource.getStructureId(),
                            true);
                    } catch (CmsException e) {
                        // should never happen
                        if (LOG.isErrorEnabled()) {
                            LOG.error(e.getMessage(), e);
                        }
                    }

                    throw new CmsVfsOnlineResourceAlreadyExistsException(Messages.get().container(
                        Messages.ERR_OVERWRITE_MOVED_RESOURCE_3,
                        dbc.removeSiteRoot(source.getRootPath()),
                        dbc.removeSiteRoot(destinationPath),
                        dbc.removeSiteRoot(offlineResource == null ? "__ERROR__" : offlineResource.getRootPath())));
                }
            } catch (CmsVfsResourceNotFoundException e) {
                // ok, no online resource
            }
        }

        try {
            Query q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_MOVE);
            q.setParameter(1, source.getStructureId().toString());
            @SuppressWarnings("unchecked")
            List<I_CmsDAOStructure> res = q.getResultList();

            for (I_CmsDAOStructure s : res) {
                s.setResourcePath(CmsFileUtil.removeTrailingSeparator(destinationPath)); // must remove trailing slash
                s.setParentId(destinationFolder.getStructureId().toString());
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        moveRelations(dbc, projectId, source.getStructureId(), destinationPath);
        repairBrokenRelations(dbc, projectId, source.getStructureId(), destinationPath);
        try {
            m_driverManager.repairCategories(dbc, projectId, readResource(dbc, projectId, destinationPath, true));
        } catch (CmsException e) {
            throw new CmsDataAccessException(e.getMessageContainer(), e);
        }
        // repair project resources
        if (!projectId.equals(CmsProject.ONLINE_PROJECT_ID) && (dbc.getRequestContext() != null)) {
            String deletedResourceRootPath = source.getRootPath();
            dbc.getRequestContext().setAttribute(CmsProjectDriver.DBC_ATTR_READ_PROJECT_FOR_RESOURCE, Boolean.TRUE);
            I_CmsProjectDriver projectDriver = m_driverManager.getProjectDriver(dbc);
            Iterator<CmsProject> itProjects = projectDriver.readProjects(dbc, deletedResourceRootPath).iterator();
            while (itProjects.hasNext()) {
                CmsProject project = itProjects.next();
                projectDriver.deleteProjectResource(dbc, project.getUuid(), deletedResourceRootPath);
                projectDriver.createProjectResource(dbc, project.getUuid(), destinationPath);
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#publishResource(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, org.opencms.file.CmsResource)
     */
    public void publishResource(
        CmsDbContext dbc,
        CmsProject onlineProject,
        CmsResource onlineResource,
        CmsResource offlineResource) throws CmsDataAccessException {

        // validate the resource length
        internalValidateResourceLength(offlineResource);
        int resourceSize = offlineResource.getLength();

        String resourcePath = CmsFileUtil.removeTrailingSeparator(offlineResource.getRootPath());
        Query q;

        try {
            boolean resourceExists = validateResourceIdExists(
                dbc,
                onlineProject.getUuid(),
                offlineResource.getResourceId());
            if (resourceExists) {
                // the resource record exists online already
                // update the online resource record
                q = m_sqlManager.createQuery(dbc, onlineProject, C_RESOURCES_UPDATE_RESOURCES);
                q.setParameter(1, offlineResource.getResourceId().toString());

                @SuppressWarnings("unchecked")
                List<I_CmsDAOResources> res = q.getResultList();

                for (I_CmsDAOResources r : res) {
                    r.setResourceType(offlineResource.getTypeId());
                    r.setResourceFlags(offlineResource.getFlags());
                    r.setDateLastModified(offlineResource.getDateLastModified());
                    r.setUserLastModified(offlineResource.getUserLastModified().toString());
                    r.setResourceState(CmsResource.STATE_UNCHANGED.getState());
                    r.setResourceSize(resourceSize);
                    r.setDateContent(offlineResource.getDateContent());
                    r.setProjectLastModified(offlineResource.getProjectLastModified().toString());
                    r.setSiblingCount(countSiblings(dbc, onlineProject.getUuid(), onlineResource.getResourceId()));
                }
            } else {
                // the resource record does NOT exist online yet
                // create the resource record online
                I_CmsDAOResources r = CmsProject.isOnlineProject(onlineProject.getUuid())
                ? new CmsDAOOnlineResources()
                : new CmsDAOOfflineResources();

                r.setResourceId(offlineResource.getResourceId().toString());
                r.setResourceType(offlineResource.getTypeId());
                r.setResourceFlags(offlineResource.getFlags());
                r.setDateCreated(offlineResource.getDateCreated());
                r.setUserCreated(offlineResource.getUserCreated().toString());
                r.setDateLastModified(offlineResource.getDateLastModified());
                r.setUserLastModified(offlineResource.getUserLastModified().toString());
                r.setResourceState(CmsResource.STATE_UNCHANGED.getState());
                r.setResourceSize(resourceSize);
                r.setDateContent(offlineResource.getDateContent());
                r.setProjectLastModified(offlineResource.getProjectLastModified().toString());
                r.setSiblingCount(1); // initial siblings count
                r.setResourceVersion(1); // initial resource version

                m_sqlManager.persist(dbc, r);
            }

            // read the parent id
            String parentId = internalReadParentId(dbc, onlineProject.getUuid(), resourcePath);

            if (validateStructureIdExists(dbc, onlineProject.getUuid(), offlineResource.getStructureId())) {
                // update the online structure record
                q = m_sqlManager.createQuery(dbc, onlineProject, C_RESOURCES_UPDATE_STRUCTURE);
                q.setParameter(1, offlineResource.getStructureId().toString());
                @SuppressWarnings("unchecked")
                List<I_CmsDAOStructure> res = q.getResultList();

                for (I_CmsDAOStructure s : res) {
                    s.setResourceId(offlineResource.getResourceId().toString());
                    s.setResourcePath(resourcePath);
                    s.setStructureState(CmsResource.STATE_UNCHANGED.getState());
                    s.setDateReleased(offlineResource.getDateReleased());
                    s.setDateExpired(offlineResource.getDateExpired());
                    s.setParentId(parentId);
                }
            } else {
                I_CmsDAOStructure s = CmsProject.isOnlineProject(onlineProject.getUuid())
                ? new CmsDAOOnlineStructure()
                : new CmsDAOOfflineStructure();

                s.setStructureId(offlineResource.getStructureId().toString());
                s.setResourceId(offlineResource.getResourceId().toString());
                s.setResourcePath(resourcePath);
                s.setStructureState(CmsResource.STATE_UNCHANGED.getState());
                s.setDateReleased(offlineResource.getDateReleased());
                s.setDateExpired(offlineResource.getDateExpired());
                s.setParentId(parentId);
                s.setStructureVersion(resourceExists ? 1 : 0); // new resources start with 0, new siblings with 1

                m_sqlManager.persist(dbc, s);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#publishVersions(org.opencms.db.CmsDbContext, org.opencms.file.CmsResource, boolean)
     */
    public void publishVersions(CmsDbContext dbc, CmsResource resource, boolean firstSibling)
    throws CmsDataAccessException {

        // if resource is null just flush the internal cache
        if (resource == null) {
            m_resOp.clear();
            return;
        }

        if (!dbc.getProjectId().isNullUUID() || dbc.currentProject().isOnlineProject()) {
            // this method is supposed to be used only in the offline project
            return;
        }

        if (firstSibling) {
            // reset the resource operation flag
            m_resOp.remove(resource.getResourceId());
        }

        boolean resOp = false; // assume structure operation

        CmsResourceState resState = internalReadResourceState(dbc, dbc.currentProject().getUuid(), resource);
        CmsResourceState strState = internalReadStructureState(dbc, dbc.currentProject().getUuid(), resource);

        if (!resState.isUnchanged()) {
            if (strState.isDeleted()) {
                resOp = (resState.isDeleted() || (resource.getSiblingCount() == 1) || (countSiblings(
                    dbc,
                    dbc.currentProject().getUuid(),
                    resource.getResourceId()) == 1));
            } else {
                resOp = true;
            }
        }

        if (!firstSibling) {
            if (resOp) {
                return;
            }
            if (m_resOp.contains(resource.getResourceId())) {
                return;
            }
        }

        // read the offline version numbers
        Map<String, Integer> versions = readVersions(
            dbc,
            dbc.currentProject().getUuid(),
            resource.getResourceId(),
            resource.getStructureId());
        int strVersion = versions.get("structure").intValue();
        int resVersion = versions.get("resource").intValue();

        if (resOp) {
            if (resource.getSiblingCount() > 1) {
                m_resOp.add(resource.getResourceId());
            }
            resVersion++;
        }
        if (!resOp) {
            strVersion++;
        }

        try {
            if (resOp) {
                // update the resource version
                Query q = m_sqlManager.createQuery(
                    dbc,
                    CmsProject.ONLINE_PROJECT_ID,
                    C_RESOURCES_UPDATE_RESOURCE_VERSION);
                q.setParameter(1, resource.getResourceId().toString());
                @SuppressWarnings("unchecked")
                List<I_CmsDAOResources> res = q.getResultList();

                for (I_CmsDAOResources r : res) {
                    r.setResourceVersion(resVersion);
                }
            }
            if (!resOp || strState.isNew()) {
                // update the structure version
                Query q = m_sqlManager.createQuery(
                    dbc,
                    CmsProject.ONLINE_PROJECT_ID,
                    C_RESOURCES_UPDATE_STRUCTURE_VERSION);
                q.setParameter(1, resource.getStructureId().toString());
                @SuppressWarnings("unchecked")
                List<I_CmsDAOStructure> res = q.getResultList();

                for (I_CmsDAOStructure s : res) {
                    s.setStructureVersion(strVersion);
                }
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readChildResources(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, boolean, boolean)
     */
    public List<CmsResource> readChildResources(
        CmsDbContext dbc,
        CmsProject currentProject,
        CmsResource resource,
        boolean getFolders,
        boolean getFiles) throws CmsDataAccessException {

        List<CmsResource> result = new ArrayList<CmsResource>();
        CmsUUID projectId = currentProject.getUuid();

        String resourceTypeClause;
        if (getFolders && getFiles) {
            resourceTypeClause = null;
        } else if (getFolders) {
            resourceTypeClause = m_sqlManager.readQuery(projectId, C_RESOURCES_GET_SUBRESOURCES_GET_FOLDERS);
        } else {
            resourceTypeClause = m_sqlManager.readQuery(projectId, C_RESOURCES_GET_SUBRESOURCES_GET_FILES);
        }
        StringBuffer query = new StringBuffer();
        query.append(m_sqlManager.readQuery(projectId, C_RESOURCES_GET_SUBRESOURCES));
        if (resourceTypeClause != null) {
            query.append(' ');
            query.append(resourceTypeClause);
        }

        try {
            Query q = m_sqlManager.createQueryFromJPQL(dbc, query.toString());
            q.setParameter(1, resource.getStructureId().toString());
            @SuppressWarnings("unchecked")
            List<Object[]> res = q.getResultList();
            I_CmsDAOResources r;
            for (Object[] o : res) {
                r = (I_CmsDAOResources)o[0];
                long size = r.getResourceSize();
                if (CmsFolder.isFolderSize(size)) {
                    result.add(createFolder(o, projectId, false));
                } else {
                    result.add(createFile(o, projectId, false));
                }
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        // sort result in memory, this is to avoid DB dependencies in the result order
        Collections.sort(result, I_CmsResource.COMPARE_ROOT_PATH_IGNORE_CASE_FOLDERS_FIRST);
        return result;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readContent(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.util.CmsUUID)
     */
    public byte[] readContent(CmsDbContext dbc, CmsUUID projectId, CmsUUID resourceId) throws CmsDataAccessException {

        byte[] byteRes = null;
        boolean resourceExists = false;

        try {
            if (projectId.equals(CmsProject.ONLINE_PROJECT_ID)) {
                // ONLINE PROJECT
                Query q = m_sqlManager.createQuery(dbc, projectId, C_ONLINE_FILES_CONTENT);
                q.setParameter(1, resourceId.toString());
                try {
                    byteRes = ((CmsDAOContents)q.getSingleResult()).getFileContent();
                    resourceExists = true;
                } catch (NoResultException e) {
                    // do nothing
                }
            } else {
                // OFFLINE PROJECT
                CmsDAOOfflineContents c = m_sqlManager.find(dbc, CmsDAOOfflineContents.class, resourceId.toString());
                if (c != null) {
                    byteRes = c.getFileContent();
                    resourceExists = true;
                }

            }

            if (!resourceExists) {
                throw new CmsVfsResourceNotFoundException(Messages.get().container(
                    Messages.ERR_READ_CONTENT_WITH_RESOURCE_ID_2,
                    resourceId,
                    Boolean.valueOf(projectId.equals(CmsProject.ONLINE_PROJECT_ID))));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        return byteRes == null ? EMPTY_BLOB : byteRes;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readFolder(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.util.CmsUUID)
     */
    public CmsFolder readFolder(CmsDbContext dbc, CmsUUID projectId, CmsUUID folderId) throws CmsDataAccessException {

        CmsFolder folder = null;

        try {
            Query q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_READBYID);
            q.setParameter(1, folderId.toString());
            try {
                Object[] o = (Object[])q.getSingleResult();
                folder = createFolder(o, projectId, true);
            } catch (NoResultException e) {
                throw new CmsVfsResourceNotFoundException(Messages.get().container(
                    Messages.ERR_READ_FOLDER_WITH_ID_1,
                    folderId));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        return folder;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readFolder(org.opencms.db.CmsDbContext, CmsUUID, java.lang.String)
     */
    public CmsFolder readFolder(CmsDbContext dbc, CmsUUID projectId, String folderPath) throws CmsDataAccessException {

        CmsFolder folder = null;

        folderPath = CmsFileUtil.removeTrailingSeparator(folderPath);
        try {
            Query q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_READ);

            q.setParameter(1, folderPath);

            try {
                Object[] o = (Object[])q.getSingleResult();
                folder = createFolder(o, projectId, true);
            } catch (NoResultException e) {
                throw new CmsVfsResourceNotFoundException(Messages.get().container(
                    Messages.ERR_READ_FOLDER_1,
                    dbc.removeSiteRoot(folderPath)));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        return folder;

    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readParentFolder(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.util.CmsUUID)
     */
    public CmsFolder readParentFolder(CmsDbContext dbc, CmsUUID projectId, CmsUUID structureId)
    throws CmsDataAccessException {

        CmsFolder parent = null;

        try {
            Query q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_READ_PARENT_BY_ID);
            q.setParameter(1, structureId.toString());

            try {
                Object[] o = (Object[])q.getSingleResult();
                parent = new CmsFolder(createResource(o, projectId));
            } catch (NoResultException e) {
                // do nothing
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        return parent;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyDefinition(org.opencms.db.CmsDbContext, java.lang.String, CmsUUID)
     */
    public CmsPropertyDefinition readPropertyDefinition(CmsDbContext dbc, String name, CmsUUID projectId)
    throws CmsDataAccessException {

        CmsPropertyDefinition propDef = null;

        try {
            Query q = m_sqlManager.createQuery(dbc, projectId, C_PROPERTYDEF_READ);
            q.setParameter(1, name);

            try {
                I_CmsDAOPropertyDef pd = (I_CmsDAOPropertyDef)q.getSingleResult();
                propDef = new CmsPropertyDefinition(
                    new CmsUUID(pd.getPropertyDefId()),
                    pd.getPropertyDefName(),
                    CmsPropertyDefinition.CmsPropertyType.valueOf(pd.getPropertyDefType()));
            } catch (NoResultException e) {
                throw new CmsDbEntryNotFoundException(Messages.get().container(
                    Messages.ERR_NO_PROPERTYDEF_WITH_NAME_1,
                    name));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        return propDef;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyDefinitions(org.opencms.db.CmsDbContext, CmsUUID)
     */
    public List<CmsPropertyDefinition> readPropertyDefinitions(CmsDbContext dbc, CmsUUID projectId)
    throws CmsDataAccessException {

        ArrayList<CmsPropertyDefinition> propertyDefinitions = new ArrayList<CmsPropertyDefinition>();

        try {
            Query q = m_sqlManager.createQuery(dbc, projectId, C_PROPERTYDEF_READALL);

            @SuppressWarnings("unchecked")
            List<I_CmsDAOPropertyDef> res = q.getResultList();
            for (I_CmsDAOPropertyDef pd : res) {
                propertyDefinitions.add(new CmsPropertyDefinition(
                    new CmsUUID(pd.getPropertyDefId()),
                    pd.getPropertyDefName(),
                    CmsPropertyDefinition.CmsPropertyType.valueOf(pd.getPropertyDefType())));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        return propertyDefinitions;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyObject(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.file.CmsProject, org.opencms.file.CmsResource)
     */
    public CmsProperty readPropertyObject(CmsDbContext dbc, String key, CmsProject project, CmsResource resource)
    throws CmsDataAccessException {

        CmsUUID projectId = ((dbc.getProjectId() == null) || dbc.getProjectId().isNullUUID())
        ? project.getUuid()
        : dbc.getProjectId();

        String propertyValue = null;
        int mappingType = -1;
        CmsProperty property = null;
        int resultSize = 0;

        try {
            Query q = m_sqlManager.createQuery(dbc, projectId, C_PROPERTIES_READ);

            q.setParameter(1, key);
            q.setParameter(2, resource.getStructureId().toString());
            q.setParameter(3, resource.getResourceId().toString());
            @SuppressWarnings("unchecked")
            List<I_CmsDAOProperties> res = q.getResultList();

            for (I_CmsDAOProperties o : res) {
                if (resultSize >= 2) {
                    throw new CmsDbConsistencyException(Messages.get().container(
                        Messages.ERR_TOO_MANY_PROPERTIES_3,
                        key,
                        resource.getRootPath(),
                        new Integer(resultSize)));
                }

                if (property == null) {
                    property = new CmsProperty();
                    property.setName(key);
                }

                propertyValue = o.getPropertyValue();
                mappingType = o.getPropertyMappingType();

                if (mappingType == CmsProperty.STRUCTURE_RECORD_MAPPING) {
                    property.setStructureValue(propertyValue);
                } else if (mappingType == CmsProperty.RESOURCE_RECORD_MAPPING) {
                    property.setResourceValue(propertyValue);
                } else {
                    throw new CmsDbConsistencyException(Messages.get().container(
                        Messages.ERR_UNKNOWN_PROPERTY_VALUE_MAPPING_3,
                        resource.getRootPath(),
                        new Integer(mappingType),
                        key));
                }

                resultSize++;
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        return (property != null) ? property : CmsProperty.getNullProperty();
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readPropertyObjects(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource)
     */
    public List<CmsProperty> readPropertyObjects(CmsDbContext dbc, CmsProject project, CmsResource resource)
    throws CmsDataAccessException {

        CmsUUID projectId = ((dbc.getProjectId() == null) || dbc.getProjectId().isNullUUID())
        ? project.getUuid()
        : dbc.getProjectId();

        int mappingType = -1;
        Map<String, CmsProperty> propertyMap = new HashMap<String, CmsProperty>();

        String propertyKey;
        String propertyValue;
        CmsProperty property;

        try {
            Query q = m_sqlManager.createQuery(dbc, projectId, C_PROPERTIES_READALL);
            q.setParameter(1, resource.getStructureId().toString());
            q.setParameter(2, resource.getResourceId().toString());
            @SuppressWarnings("unchecked")
            List<Object[]> res = q.getResultList();

            for (Object[] o : res) {
                propertyKey = null;
                propertyValue = null;
                mappingType = -1;

                propertyKey = ((I_CmsDAOPropertyDef)o[0]).getPropertyDefName();
                propertyValue = ((I_CmsDAOProperties)o[1]).getPropertyValue();
                mappingType = ((I_CmsDAOProperties)o[1]).getPropertyMappingType();

                property = propertyMap.get(propertyKey);
                if (property == null) {
                    // there doesn't exist a property object for this key yet
                    property = new CmsProperty();
                    property.setName(propertyKey);
                    propertyMap.put(propertyKey, property);
                }

                if (mappingType == CmsProperty.STRUCTURE_RECORD_MAPPING) {
                    // this property value is mapped to a structure record
                    property.setStructureValue(propertyValue);
                } else if (mappingType == CmsProperty.RESOURCE_RECORD_MAPPING) {
                    // this property value is mapped to a resource record
                    property.setResourceValue(propertyValue);
                } else {
                    throw new CmsDbConsistencyException(Messages.get().container(
                        Messages.ERR_UNKNOWN_PROPERTY_VALUE_MAPPING_3,
                        resource.getRootPath(),
                        new Integer(mappingType),
                        propertyKey));
                }
                property.setOrigin(resource.getRootPath());
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        return new ArrayList<CmsProperty>(propertyMap.values());
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readRelations(org.opencms.db.CmsDbContext, CmsUUID, CmsResource, org.opencms.relations.CmsRelationFilter)
     */
    public List<CmsRelation> readRelations(
        CmsDbContext dbc,
        CmsUUID projectId,
        CmsResource resource,
        CmsRelationFilter filter) throws CmsDataAccessException {

        Set<CmsRelation> relations = new HashSet<CmsRelation>();

        try {
            if (filter.isSource()) {
                List<String> params = new ArrayList<String>(7);

                StringBuffer queryBuf = new StringBuffer(256);
                queryBuf.append(m_sqlManager.readQuery(projectId, C_READ_RELATIONS));
                queryBuf.append(prepareRelationConditions(projectId, filter, resource, params, true));
                if (LOG.isDebugEnabled()) {
                    LOG.debug(queryBuf.toString());
                }

                Query q = m_sqlManager.createQueryFromJPQL(dbc, queryBuf.toString());
                for (int i = 0; i < params.size(); i++) {
                    q.setParameter(i + 1, params.get(i));
                }
                @SuppressWarnings("unchecked")
                List<I_CmsDAOResourceRelations> res = q.getResultList();
                for (I_CmsDAOResourceRelations rr : res) {
                    relations.add(internalReadRelation(rr));
                }
            }

            if (filter.isTarget()) {
                List<String> params = new ArrayList<String>(7);

                StringBuffer queryBuf = new StringBuffer(256);
                queryBuf.append(m_sqlManager.readQuery(projectId, C_READ_RELATIONS));
                queryBuf.append(prepareRelationConditions(projectId, filter, resource, params, false));
                if (LOG.isDebugEnabled()) {
                    LOG.debug(queryBuf.toString());
                }

                Query q = m_sqlManager.createQueryFromJPQL(dbc, queryBuf.toString());
                for (int i = 0; i < params.size(); i++) {
                    q.setParameter(i + 1, params.get(i));
                }
                @SuppressWarnings("unchecked")
                List<I_CmsDAOResourceRelations> res = q.getResultList();
                for (I_CmsDAOResourceRelations rr : res) {
                    relations.add(internalReadRelation(rr));
                }
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        List<CmsRelation> result = new ArrayList<CmsRelation>(relations);
        Collections.sort(result, CmsRelation.COMPARATOR);
        return result;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResource(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.util.CmsUUID, boolean)
     */
    public CmsResource readResource(CmsDbContext dbc, CmsUUID projectId, CmsUUID structureId, boolean includeDeleted)
    throws CmsDataAccessException {

        CmsResource resource = null;

        try {
            Query q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_READBYID);

            q.setParameter(1, structureId.toString());

            try {
                Object[] o = (Object[])q.getSingleResult();
                resource = createResource(o, projectId);
            } catch (NoResultException e) {
                throw new CmsVfsResourceNotFoundException(Messages.get().container(
                    Messages.ERR_READ_RESOURCE_WITH_ID_1,
                    structureId));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        // check if this resource is marked as deleted and if we are allowed to return a deleted resource
        if ((resource != null) && resource.getState().isDeleted() && !includeDeleted) {
            throw new CmsVfsResourceNotFoundException(Messages.get().container(
                Messages.ERR_READ_DELETED_RESOURCE_1,
                dbc.removeSiteRoot(resource.getRootPath())));
        }

        return resource;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResource(org.opencms.db.CmsDbContext, CmsUUID, java.lang.String, boolean)
     */
    public CmsResource readResource(CmsDbContext dbc, CmsUUID projectId, String path, boolean includeDeleted)
    throws CmsDataAccessException {

        CmsResource resource = null;

        // must remove trailing slash
        path = CmsFileUtil.removeTrailingSeparator(path);
        boolean endsWithSlash = path.endsWith("/");

        try {
            Query q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_READ);

            q.setParameter(1, path);

            try {
                Object[] o = (Object[])q.getSingleResult();
                resource = createResource(o, projectId);

                // check if the resource is a file, it is not allowed to end with a "/" then
                if (endsWithSlash && resource.isFile()) {
                    throw new CmsVfsResourceNotFoundException(Messages.get().container(
                        Messages.ERR_READ_RESOURCE_1,
                        dbc.removeSiteRoot(path + "/")));
                }
            } catch (NoResultException e) {
                throw new CmsVfsResourceNotFoundException(Messages.get().container(
                    Messages.ERR_READ_RESOURCE_1,
                    dbc.removeSiteRoot(path)));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        // check if this resource is marked as deleted and if we are allowed to return a deleted resource
        if ((resource != null) && resource.getState().isDeleted() && !includeDeleted) {
            throw new CmsVfsResourceNotFoundException(Messages.get().container(
                Messages.ERR_READ_DELETED_RESOURCE_1,
                dbc.removeSiteRoot(resource.getRootPath())));
        }

        return resource;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResources(org.opencms.db.CmsDbContext, CmsUUID, CmsResourceState, int)
     */
    public List<CmsResource> readResources(CmsDbContext dbc, CmsUUID projectId, CmsResourceState state, int mode)
    throws CmsDataAccessException {

        List<CmsResource> result = new ArrayList<CmsResource>();
        Query q;

        try {
            if (mode == CmsDriverManager.READMODE_MATCHSTATE) {
                q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_GET_RESOURCE_IN_PROJECT_WITH_STATE);

                q.setParameter(1, projectId.toString());
                q.setParameter(2, Integer.valueOf(state.getState()));
                q.setParameter(3, Integer.valueOf(state.getState()));
                q.setParameter(4, Integer.valueOf(state.getState()));
                q.setParameter(5, Integer.valueOf(state.getState()));
            } else if (mode == CmsDriverManager.READMODE_UNMATCHSTATE) {
                q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_GET_RESOURCE_IN_PROJECT_WITHOUT_STATE);
                q.setParameter(1, projectId.toString());
                q.setParameter(2, Integer.valueOf(state.getState()));
                q.setParameter(3, Integer.valueOf(state.getState()));
            } else {
                q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_GET_RESOURCE_IN_PROJECT_IGNORE_STATE);
                q.setParameter(1, projectId.toString());
            }

            @SuppressWarnings("unchecked")
            List<Object[]> res = q.getResultList();
            for (Object[] o : res) {
                CmsResource resource = createResource(o, projectId);
                result.add(resource);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        return result;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResourcesForPrincipalACE(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.util.CmsUUID)
     */
    public List<CmsResource> readResourcesForPrincipalACE(CmsDbContext dbc, CmsProject project, CmsUUID principalId)
    throws CmsDataAccessException {

        CmsResource currentResource = null;
        List<CmsResource> resources = new ArrayList<CmsResource>();

        try {
            Query q = m_sqlManager.createQuery(dbc, project, C_SELECT_RESOURCES_FOR_PRINCIPAL_ACE);

            q.setParameter(1, principalId.toString());
            @SuppressWarnings("unchecked")
            List<Object[]> res = q.getResultList();

            for (Object[] o : res) {
                currentResource = createFile(o, project.getUuid(), false);
                resources.add(currentResource);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        return resources;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResourcesForPrincipalAttr(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.util.CmsUUID)
     */
    public List<CmsResource> readResourcesForPrincipalAttr(CmsDbContext dbc, CmsProject project, CmsUUID principalId)
    throws CmsDataAccessException {

        CmsResource currentResource = null;
        List<CmsResource> resources = new ArrayList<CmsResource>();

        try {
            // JPQL does not support UNION clause
            String[] queries = {C_SELECT_RESOURCES_FOR_PRINCIPAL_ATTR1, C_SELECT_RESOURCES_FOR_PRINCIPAL_ATTR2};
            String[] parameters = {principalId.toString(), principalId.toString()};
            Query q;

            for (int i = 0; i < queries.length; i++) {
                q = m_sqlManager.createQuery(dbc, project, queries[i]);
                q.setParameter(1, parameters[i]);
                @SuppressWarnings("unchecked")
                List<Object[]> res = q.getResultList();
                for (Object[] o : res) {
                    currentResource = createFile(o, project.getUuid(), false);
                    resources.add(currentResource);
                }
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        return resources;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResourcesWithProperty(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.util.CmsUUID, String, String)
     */
    public List<CmsResource> readResourcesWithProperty(
        CmsDbContext dbc,
        CmsUUID projectId,
        CmsUUID propertyDef,
        String path,
        String value) throws CmsDataAccessException {

        List<CmsResource> resources = new ArrayList<CmsResource>();
        Query q;
        List<Object[]> res = new ArrayList<Object[]>();
        try {
            if (value == null) {
                q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_GET_RESOURCE_WITH_PROPERTYDEF);
                q.setParameter(1, propertyDef.toString());
                q.setParameter(2, escapeDbWildcard(path + "%"));
                res.addAll(q.getResultList());
            } else {
                q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_GET_RESOURCE_WITH_PROPERTYDEF_VALUE);
                q.setParameter(1, propertyDef.toString());
                q.setParameter(2, escapeDbWildcard(path + "%"));
                q.setParameter(3, "%" + value + "%");
                q.setParameter(4, escapeDbWildcard(path + "%"));
                q.setParameter(5, "%" + value + "%");
                res.addAll(q.getResultList());
            }

            for (Object[] o : res) {
                CmsResource resource = createResource(o, projectId);
                resources.add(resource);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        return resources;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readResourceTree(org.opencms.db.CmsDbContext, CmsUUID, java.lang.String, int, CmsResourceState, long, long, long, long, long, long, int)
     */
    public List<CmsResource> readResourceTree(
        CmsDbContext dbc,
        CmsUUID projectId,
        String parentPath,
        int type,
        CmsResourceState state,
        long lastModifiedAfter,
        long lastModifiedBefore,
        long releasedAfter,
        long releasedBefore,
        long expiredAfter,
        long expiredBefore,
        int mode) throws CmsDataAccessException {

        List<CmsResource> result = new ArrayList<CmsResource>();

        StringBuffer conditions = new StringBuffer();
        List params = new ArrayList(5);

        // prepare the selection criteria
        prepareProjectCondition(projectId, mode, conditions, params);
        prepareResourceCondition(projectId, mode, conditions);
        prepareTypeCondition(projectId, type, mode, conditions, params);
        prepareTimeRangeCondition(projectId, lastModifiedAfter, lastModifiedBefore, conditions, params);
        prepareReleasedTimeRangeCondition(projectId, releasedAfter, releasedBefore, conditions, params);
        prepareExpiredTimeRangeCondition(projectId, expiredAfter, expiredBefore, conditions, params);
        preparePathCondition(projectId, parentPath, mode, conditions, params);
        prepareStateCondition(projectId, state, mode, conditions, params);

        // now read matching resources within the subtree 

        try {
            StringBuffer queryBuf = new StringBuffer(256);
            queryBuf.append(m_sqlManager.readQuery(projectId, C_RESOURCES_READ_TREE));
            queryBuf.append(conditions);
            queryBuf.append(" ");
            queryBuf.append(m_sqlManager.readQuery(projectId, C_RESOURCES_ORDER_BY_PATH));
            Query q = m_sqlManager.createQueryFromJPQL(dbc, queryBuf.toString());
            for (int i = 0; i < params.size(); i++) {
                q.setParameter(i + 1, params.get(i));
            }

            @SuppressWarnings("unchecked")
            List<Object[]> res = q.getResultList();
            for (Object[] o : res) {
                CmsResource resource = createResource(o, projectId);
                result.add(resource);
            }

        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        return result;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readSiblings(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.file.CmsResource, boolean)
     */
    public List<CmsResource> readSiblings(
        CmsDbContext dbc,
        CmsUUID projectId,
        CmsResource resource,
        boolean includeDeleted) throws CmsDataAccessException {

        CmsResource currentResource = null;
        List<CmsResource> vfsLinks = new ArrayList<CmsResource>();
        Query q;

        try {
            if (includeDeleted) {
                q = m_sqlManager.createQuery(dbc, projectId, C_SELECT_VFS_SIBLINGS);
            } else {
                q = m_sqlManager.createQuery(dbc, projectId, C_SELECT_NONDELETED_VFS_SIBLINGS);
            }

            q.setParameter(1, resource.getResourceId().toString());
            @SuppressWarnings("unchecked")
            List<Object[]> res = q.getResultList();

            for (Object[] o : res) {
                currentResource = createFile(o, projectId, false);
                vfsLinks.add(currentResource);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        return vfsLinks;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readUrlNameMappingEntries(org.opencms.db.CmsDbContext, boolean, org.opencms.db.urlname.CmsUrlNameMappingFilter)
     */
    public List<CmsUrlNameMappingEntry> readUrlNameMappingEntries(
        CmsDbContext dbc,
        boolean online,
        CmsUrlNameMappingFilter filter) throws CmsDataAccessException {

        List<CmsUrlNameMappingEntry> result = new ArrayList<CmsUrlNameMappingEntry>();
        try {
            String query = m_sqlManager.readQuery(C_READ_URLNAME_MAPPINGS);
            Query q = getQueryForFilter(dbc, query, filter, online);
            @SuppressWarnings("unchecked")
            List<I_CmsDAOUrlNameMappings> res = q.getResultList();
            for (I_CmsDAOUrlNameMappings m : res) {
                CmsUrlNameMappingEntry entry = internalCreateUrlNameMappingEntry(m);
                result.add(entry);
            }
            return result;
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#readVersions(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID)
     */
    public Map<String, Integer> readVersions(
        CmsDbContext dbc,
        CmsUUID projectId,
        CmsUUID resourceId,
        CmsUUID structureId) throws CmsDataAccessException {

        int structureVersion = -1;
        int resourceVersion = -1;
        Query q;

        try {
            // read the offline version numbers, first for the resource entry
            q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_READ_VERSION_RES);
            q.setParameter(1, resourceId.toString());
            try {
                resourceVersion = CmsDataTypeUtil.numberToInt((Number)q.getSingleResult());
            } catch (NoResultException e) {
                // do nothing
            }

            // then for the structure entry
            q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_READ_VERSION_STR);
            q.setParameter(1, structureId.toString());

            try {
                structureVersion = CmsDataTypeUtil.numberToInt((Number)q.getSingleResult());
            } catch (NoResultException e) {
                // do nothing
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        Map<String, Integer> result = new HashMap<String, Integer>();
        result.put("structure", new Integer(structureVersion));
        result.put(I_CmsEventListener.KEY_RESOURCE, new Integer(resourceVersion));
        return result;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#removeFile(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.file.CmsResource)
     */
    public void removeFile(CmsDbContext dbc, CmsUUID projectId, CmsResource resource) throws CmsDataAccessException {

        int siblingCount = 0;

        try {
            // delete the structure record
            Query q = m_sqlManager.createQuery(dbc, projectId, C_STRUCTURE_DELETE_BY_STRUCTUREID);
            q.setParameter(1, resource.getStructureId().toString());
            @SuppressWarnings("unchecked")
            List<I_CmsDAOStructure> res = q.getResultList();
            for (I_CmsDAOStructure ps : res) {
                m_sqlManager.remove(dbc, ps);
            }

            // count the references to the resource
            siblingCount = countSiblings(dbc, projectId, resource.getResourceId());

            if (siblingCount > 0) {
                // update the link Count
                q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_UPDATE_SIBLING_COUNT);
                q.setParameter(1, resource.getResourceId().toString());
                @SuppressWarnings("unchecked")
                List<I_CmsDAOResources> ress = q.getResultList();

                for (I_CmsDAOResources r : ress) {
                    r.setSiblingCount(siblingCount);
                }

                // update the resource flags
                q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_UPDATE_FLAGS);
                q.setParameter(1, resource.getResourceId().toString());
                @SuppressWarnings("unchecked")
                List<I_CmsDAOResources> resf = q.getResultList();

                for (I_CmsDAOResources r : resf) {
                    r.setResourceFlags(resource.getFlags());
                }

            } else {
                // if not referenced any longer, also delete the resource and the content record
                q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_DELETE_BY_RESOURCEID);
                q.setParameter(1, resource.getResourceId().toString());
                @SuppressWarnings("unchecked")
                List<I_CmsDAOResources> res1 = q.getResultList();
                for (I_CmsDAOResources pr : res1) {
                    m_sqlManager.remove(dbc, pr);
                }

                boolean dbcHasProjectId = (dbc.getProjectId() != null) && !dbc.getProjectId().isNullUUID();

                // if online we have to keep historical content
                if (projectId.equals(CmsProject.ONLINE_PROJECT_ID)) {
                    // put the online content in the history 
                    q = m_sqlManager.createQuery(dbc, C_ONLINE_CONTENTS_HISTORY);
                    q.setParameter(1, resource.getResourceId().toString());
                    @SuppressWarnings("unchecked")
                    List<CmsDAOContents> res2 = q.getResultList();
                    for (CmsDAOContents c : res2) {
                        c.setOnlineFlag(0);
                    }
                } else if (dbcHasProjectId) {
                    // remove current online version
                    q = m_sqlManager.createQuery(dbc, C_ONLINE_CONTENTS_DELETE);
                    q.setParameter(1, resource.getResourceId().toString());
                    q.executeUpdate();
                } else {
                    // delete content records with this resource id
                    q = m_sqlManager.createQuery(dbc, C_OFFLINE_FILE_CONTENT_DELETE);
                    q.setParameter(1, resource.getResourceId().toString());
                    q.executeUpdate();
                }
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#removeFolder(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource)
     */
    public void removeFolder(CmsDbContext dbc, CmsProject currentProject, CmsResource resource)
    throws CmsDataAccessException {

        if ((dbc.getRequestContext() != null)
            && (dbc.getRequestContext().getAttribute(REQ_ATTR_CHECK_PERMISSIONS) != null)) {
            // only check write permissions
            checkWritePermissionsInFolder(dbc, resource);
            return;
        }

        // check if the folder has any resources in it
        Iterator<CmsResource> childResources = readChildResources(dbc, currentProject, resource, true, true).iterator();

        CmsUUID projectId = CmsProject.ONLINE_PROJECT_ID;
        if (currentProject.isOnlineProject()) {
            projectId = CmsUUID.getOpenCmsUUID(); // HACK: to get an offline project id
        }

        // collect the names of the resources inside the folder, excluding the moved resources
        StringBuffer errorResNames = new StringBuffer(128);
        while (childResources.hasNext()) {
            CmsResource errorRes = childResources.next();
            // if deleting offline, or not moved, or just renamed inside the deleted folder
            // so, it may remain some orphan online entries for moved resources
            // which will be fixed during the publishing of the moved resources
            boolean error = !currentProject.isOnlineProject();
            if (!error) {
                try {
                    String originalPath = m_driverManager.getVfsDriver().readResource(
                        dbc,
                        projectId,
                        errorRes.getRootPath(),
                        true).getRootPath();
                    error = originalPath.equals(errorRes.getRootPath())
                        || originalPath.startsWith(resource.getRootPath());
                } catch (CmsVfsResourceNotFoundException e) {
                    // ignore
                }
            }
            if (error) {
                if (errorResNames.length() != 0) {
                    errorResNames.append(", ");
                }
                errorResNames.append("[" + dbc.removeSiteRoot(errorRes.getRootPath()) + "]");
            }
        }

        // the current implementation only deletes empty folders
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(errorResNames.toString())) {

            throw new CmsVfsException(Messages.get().container(
                Messages.ERR_DELETE_NONEMTY_FOLDER_2,
                dbc.removeSiteRoot(resource.getRootPath()),
                errorResNames.toString()));
        }
        internalRemoveFolder(dbc, currentProject, resource);

        // remove project resources
        String deletedResourceRootPath = resource.getRootPath();
        if (dbc.getRequestContext() != null) {
            dbc.getRequestContext().setAttribute(CmsProjectDriver.DBC_ATTR_READ_PROJECT_FOR_RESOURCE, Boolean.TRUE);
            I_CmsProjectDriver projectDriver = m_driverManager.getProjectDriver(dbc);
            Iterator<CmsProject> itProjects = projectDriver.readProjects(dbc, deletedResourceRootPath).iterator();
            while (itProjects.hasNext()) {
                CmsProject project = itProjects.next();
                projectDriver.deleteProjectResource(dbc, project.getUuid(), deletedResourceRootPath);
            }
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#replaceResource(org.opencms.db.CmsDbContext, org.opencms.file.CmsResource, byte[], int)
     */
    public void replaceResource(CmsDbContext dbc, CmsResource newResource, byte[] resContent, int newResourceType)
    throws CmsDataAccessException {

        if (resContent == null) {
            // nothing to do
            return;
        }

        try {
            // write the file content
            writeContent(dbc, newResource.getResourceId(), resContent);

            // update the resource record
            Query q = m_sqlManager.createQuery(dbc, dbc.currentProject(), C_RESOURCE_REPLACE);
            q.setParameter(1, newResource.getResourceId().toString());
            @SuppressWarnings("unchecked")
            List<I_CmsDAOResources> res = q.getResultList();

            for (I_CmsDAOResources r : res) {
                r.setResourceType(newResourceType);
                r.setResourceSize(resContent.length);
                r.setDateContent(System.currentTimeMillis());
            }

        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#setDriverManager(org.opencms.db.CmsDriverManager)
     */
    public void setDriverManager(CmsDriverManager driverManager) {

        m_driverManager = driverManager;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#setSqlManager(org.opencms.db.CmsSqlManager)
     */
    public void setSqlManager(org.opencms.db.CmsSqlManager sqlManager) {

        m_sqlManager = (CmsSqlManager)sqlManager;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#transferResource(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, org.opencms.util.CmsUUID, org.opencms.util.CmsUUID)
     */
    public void transferResource(
        CmsDbContext dbc,
        CmsProject project,
        CmsResource resource,
        CmsUUID createdUser,
        CmsUUID lastModifiedUser) throws CmsDataAccessException {

        if (createdUser == null) {
            createdUser = resource.getUserCreated();
        }
        if (lastModifiedUser == null) {
            lastModifiedUser = resource.getUserLastModified();
        }

        try {
            Query q = m_sqlManager.createQuery(dbc, project, C_RESOURCES_TRANSFER_RESOURCE);
            q.setParameter(1, resource.getResourceId().toString());
            @SuppressWarnings("unchecked")
            List<I_CmsDAOResources> res = q.getResultList();

            for (I_CmsDAOResources r : res) {
                r.setUserCreated(createdUser.toString());
                r.setUserLastModified(lastModifiedUser.toString());
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#updateRelations(CmsDbContext, CmsProject, CmsResource)
     */
    public void updateRelations(CmsDbContext dbc, CmsProject onlineProject, CmsResource offlineResource)
    throws CmsDataAccessException {

        // delete online relations
        I_CmsVfsDriver vfsDriver = m_driverManager.getVfsDriver(dbc);
        vfsDriver.deleteRelations(dbc, onlineProject.getUuid(), offlineResource, CmsRelationFilter.TARGETS);

        CmsUUID projectId;
        if (!dbc.getProjectId().isNullUUID()) {
            projectId = CmsProject.ONLINE_PROJECT_ID;
        } else {
            projectId = dbc.currentProject().getUuid();
        }

        // copy offline to online relations
        CmsUUID dbcProjectId = dbc.getProjectId();
        dbc.setProjectId(CmsUUID.getNullUUID());
        Iterator<CmsRelation> itRelations = m_driverManager.getVfsDriver(dbc).readRelations(
            dbc,
            projectId,
            offlineResource,
            CmsRelationFilter.TARGETS).iterator();
        dbc.setProjectId(dbcProjectId);
        while (itRelations.hasNext()) {
            vfsDriver.createRelation(dbc, onlineProject.getUuid(), itRelations.next());
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#validateResourceIdExists(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.util.CmsUUID)
     */
    public boolean validateResourceIdExists(CmsDbContext dbc, CmsUUID projectId, CmsUUID resourceId)
    throws CmsDataAccessException {

        boolean exists = false;

        try {
            Query q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_READ_RESOURCE_STATE);
            q.setParameter(1, resourceId.toString());

            try {
                @SuppressWarnings("unused")
                int state = CmsDataTypeUtil.numberToInt((Number)q.getSingleResult());
                exists = true;
            } catch (NoResultException e) {
                // do nothing
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        return exists;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#validateStructureIdExists(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.util.CmsUUID)
     */
    public boolean validateStructureIdExists(CmsDbContext dbc, CmsUUID projectId, CmsUUID structureId)
    throws CmsDataAccessException {

        boolean found = false;
        int count = 0;

        try {
            Query q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_SELECT_STRUCTURE_ID);
            q.setParameter(1, structureId.toString());

            try {
                count = CmsDataTypeUtil.numberToInt((Number)q.getSingleResult());
                found = (count == 1);
            } catch (NoResultException e) {
                // do nothing
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        return found;
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeContent(org.opencms.db.CmsDbContext, org.opencms.util.CmsUUID, byte[])
     */
    public void writeContent(CmsDbContext dbc, CmsUUID resourceId, byte[] content) throws CmsDataAccessException {

        try {
            Query q = m_sqlManager.createQuery(dbc, dbc.currentProject(), C_OFFLINE_CONTENTS_UPDATE);
            // update the file content in the database.
            q.setParameter(1, resourceId.toString());
            @SuppressWarnings("unchecked")
            List<CmsDAOOfflineContents> res = q.getResultList();

            for (CmsDAOOfflineContents oc : res) {
                oc.setFileContent(content);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeLastModifiedProjectId(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, CmsUUID, org.opencms.file.CmsResource)
     */
    public void writeLastModifiedProjectId(CmsDbContext dbc, CmsProject project, CmsUUID projectId, CmsResource resource)
    throws CmsDataAccessException {

        try {
            Query q = m_sqlManager.createQuery(dbc, project, C_RESOURCES_UPDATE_PROJECT_LASTMODIFIED);
            q.setParameter(1, resource.getResourceId().toString());
            @SuppressWarnings("unchecked")
            List<I_CmsDAOResources> res = q.getResultList();

            for (I_CmsDAOResources r : res) {
                r.setProjectLastModified(projectId.toString());
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writePropertyObject(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, org.opencms.file.CmsProperty)
     */
    public void writePropertyObject(CmsDbContext dbc, CmsProject project, CmsResource resource, CmsProperty property)
    throws CmsDataAccessException {

        CmsUUID projectId = ((dbc.getProjectId() == null) || dbc.getProjectId().isNullUUID())
        ? project.getUuid()
        : dbc.getProjectId();

        // TODO: check if we need autocreation for link property definition types too
        CmsPropertyDefinition propertyDefinition = null;
        try {
            // read the property definition
            propertyDefinition = readPropertyDefinition(dbc, property.getName(), projectId);
        } catch (CmsDbEntryNotFoundException e) {
            if (property.autoCreatePropertyDefinition()) {
                propertyDefinition = createPropertyDefinition(
                    dbc,
                    projectId,
                    property.getName(),
                    CmsPropertyDefinition.TYPE_NORMAL);
                try {
                    readPropertyDefinition(dbc, property.getName(), CmsProject.ONLINE_PROJECT_ID);
                } catch (CmsDataAccessException e1) {
                    createPropertyDefinition(
                        dbc,
                        CmsProject.ONLINE_PROJECT_ID,
                        property.getName(),
                        CmsPropertyDefinition.TYPE_NORMAL);
                }
                try {
                    m_driverManager.getHistoryDriver(dbc).readPropertyDefinition(dbc, property.getName());
                } catch (CmsDataAccessException e1) {
                    m_driverManager.getHistoryDriver(dbc).createPropertyDefinition(
                        dbc,
                        property.getName(),
                        CmsPropertyDefinition.TYPE_NORMAL);
                }
                OpenCms.fireCmsEvent(new CmsEvent(
                    I_CmsEventListener.EVENT_PROPERTY_DEFINITION_CREATED,
                    Collections.<String, Object> singletonMap("propertyDefinition", propertyDefinition)));
            } else {
                throw new CmsDbEntryNotFoundException(Messages.get().container(
                    Messages.ERR_NO_PROPERTYDEF_WITH_NAME_1,
                    property.getName()));
            }
        }

        try {
            // read the existing property to test if we need the 
            // insert or update query to write a property value
            CmsProperty existingProperty = readPropertyObject(dbc, propertyDefinition.getName(), project, resource);

            if (existingProperty.isIdentical(property)) {
                // property already has the identical values set, no write required
                return;
            }

            for (int i = 0; i < 2; i++) {
                int mappingType = -1;
                String value = null;
                CmsUUID id = null;
                boolean existsPropertyValue = false;
                boolean deletePropertyValue = false;

                // 1) take any required decisions to choose and fill the correct SQL query

                if (i == 0) {
                    // write/delete the *structure value* on the first cycle
                    if ((existingProperty.getStructureValue() != null) && property.isDeleteStructureValue()) {
                        // this property value is marked to be deleted
                        deletePropertyValue = true;
                    } else {
                        value = property.getStructureValue();
                        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
                            // no structure value set or the structure value is an empty string, 
                            // continue with the resource value
                            continue;
                        }
                    }

                    // set the vars to be written to the database
                    mappingType = CmsProperty.STRUCTURE_RECORD_MAPPING;
                    id = resource.getStructureId();
                    existsPropertyValue = existingProperty.getStructureValue() != null;
                } else {
                    // write/delete the *resource value* on the second cycle
                    if ((existingProperty.getResourceValue() != null) && property.isDeleteResourceValue()) {
                        // this property value is marked to be deleted
                        deletePropertyValue = true;
                    } else {
                        value = property.getResourceValue();
                        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
                            // no resource value set or the resource value is an empty string,
                            // break out of the loop
                            break;
                        }
                    }

                    // set the vars to be written to the database
                    mappingType = CmsProperty.RESOURCE_RECORD_MAPPING;
                    id = resource.getResourceId();
                    existsPropertyValue = existingProperty.getResourceValue() != null;
                }

                // 2) execute the SQL query
                Query q;
                if (!deletePropertyValue) {
                    // insert/update the property value                    
                    if (existsPropertyValue) {
                        // {structure|resource} property value already exists- use update statement
                        q = m_sqlManager.createQuery(dbc, projectId, C_PROPERTIES_UPDATE);
                        q.setParameter(1, id.toString());
                        q.setParameter(2, Integer.valueOf(mappingType));
                        q.setParameter(3, propertyDefinition.getId().toString());
                        @SuppressWarnings("unchecked")
                        List<I_CmsDAOProperties> res = q.getResultList();

                        for (I_CmsDAOProperties p : res) {
                            p.setPropertyValue(m_sqlManager.validateEmpty(value));
                        }
                    } else {
                        // {structure|resource} property value doesn't exist- use create statement
                        I_CmsDAOProperties p = CmsProject.isOnlineProject(project.getUuid())
                        ? new CmsDAOOnlineProperties()
                        : new CmsDAOOfflineProperties();

                        p.setPropertyId(new CmsUUID().toString());
                        p.setPropertyDefId(propertyDefinition.getId().toString());
                        p.setPropertyMappingId(id.toString());
                        p.setPropertyMappingType(mappingType);
                        p.setPropertyValue(m_sqlManager.validateEmpty(value));

                        m_sqlManager.persist(dbc, p);
                    }
                } else {
                    // {structure|resource} property value marked as deleted- use delete statement
                    q = m_sqlManager.createQuery(dbc, projectId, C_PROPERTIES_DELETE);
                    q.setParameter(1, propertyDefinition.getId().toString());
                    q.setParameter(2, id.toString());
                    q.setParameter(3, Integer.valueOf(mappingType));
                    @SuppressWarnings("unchecked")
                    List<I_CmsDAOProperties> res = q.getResultList();
                    for (I_CmsDAOProperties pr : res) {
                        m_sqlManager.remove(dbc, pr);
                    }
                }
            }

        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writePropertyObjects(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, java.util.List)
     */
    public void writePropertyObjects(
        CmsDbContext dbc,
        CmsProject project,
        CmsResource resource,
        List<CmsProperty> properties) throws CmsDataAccessException {

        for (CmsProperty property : properties) {
            writePropertyObject(dbc, project, resource, property);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeResource(org.opencms.db.CmsDbContext, CmsUUID, org.opencms.file.CmsResource, int)
     */
    public void writeResource(CmsDbContext dbc, CmsUUID projectId, CmsResource resource, int changed)
    throws CmsDataAccessException {

        // validate the resource length
        internalValidateResourceLength(resource);

        String resourcePath = CmsFileUtil.removeTrailingSeparator(resource.getRootPath());

        // this task is split into two statements because some DBs (e.g. Oracle) doesn't support multi-table updates

        long resourceDateModified;

        if (resource.isTouched()) {
            resourceDateModified = resource.getDateLastModified();
        } else {
            resourceDateModified = System.currentTimeMillis();
        }

        CmsResourceState structureState = resource.getState();
        CmsResourceState resourceState = resource.getState();
        CmsResourceState structureStateOld = internalReadStructureState(dbc, projectId, resource);
        CmsResourceState resourceStateOld = internalReadResourceState(dbc, projectId, resource);
        CmsUUID projectLastModified = projectId;

        if (changed == CmsDriverManager.UPDATE_RESOURCE_STATE) {
            resourceState = resourceStateOld;
            resourceState = (resourceState.isNew() ? CmsResource.STATE_NEW : CmsResource.STATE_CHANGED);
            structureState = structureStateOld;
        } else if (changed == CmsDriverManager.UPDATE_STRUCTURE_STATE) {
            structureState = structureStateOld;
            structureState = (structureState.isNew() ? CmsResource.STATE_NEW : CmsResource.STATE_CHANGED);
        } else if (changed == CmsDriverManager.NOTHING_CHANGED) {
            projectLastModified = resource.getProjectLastModified();
        } else {
            resourceState = resourceStateOld;
            resourceState = (resourceState.isNew() ? CmsResource.STATE_NEW : CmsResource.STATE_CHANGED);
            structureState = structureStateOld;
            structureState = (structureState.isNew() ? CmsResource.STATE_NEW : CmsResource.STATE_CHANGED);
        }

        try {
            Query q;
            if (changed != CmsDriverManager.UPDATE_STRUCTURE_STATE) {
                // if the resource was unchanged
                q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_UPDATE_RESOURCES);
                q.setParameter(1, resource.getResourceId().toString());

                @SuppressWarnings("unchecked")
                List<I_CmsDAOResources> res = q.getResultList();

                for (I_CmsDAOResources r : res) {
                    r.setResourceType(resource.getTypeId());
                    r.setResourceFlags(resource.getFlags());
                    r.setDateLastModified(resourceDateModified);
                    r.setUserLastModified(resource.getUserLastModified().toString());
                    r.setResourceState(resourceState.getState());
                    r.setResourceSize(resource.getLength());
                    r.setDateContent(resource.getDateContent());
                    r.setProjectLastModified(projectLastModified.toString());
                    r.setSiblingCount(countSiblings(dbc, projectId, resource.getResourceId()));
                }
            } else {
                q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_UPDATE_RESOURCES_WITHOUT_STATE);
                q.setParameter(1, resource.getResourceId().toString());
                @SuppressWarnings("unchecked")
                List<I_CmsDAOResources> res = q.getResultList();

                for (I_CmsDAOResources r : res) {
                    r.setResourceType(resource.getTypeId());
                    r.setResourceFlags(resource.getFlags());
                    r.setDateLastModified(resourceDateModified);
                    r.setUserLastModified(resource.getUserLastModified().toString());
                    r.setResourceSize(resource.getLength());
                    r.setDateContent(resource.getDateContent());
                    r.setProjectLastModified(projectLastModified.toString());
                    r.setSiblingCount(countSiblings(dbc, projectId, resource.getResourceId()));
                }
            }

            // read the parent id
            String parentId = internalReadParentId(dbc, projectId, resourcePath);

            // update the structure
            q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_UPDATE_STRUCTURE);
            q.setParameter(1, resource.getStructureId().toString());
            @SuppressWarnings("unchecked")
            List<I_CmsDAOStructure> res = q.getResultList();

            for (I_CmsDAOStructure s : res) {
                s.setResourceId(resource.getResourceId().toString());
                s.setResourcePath(resourcePath);
                s.setStructureState(structureState.getState());
                s.setDateReleased(resource.getDateReleased());
                s.setDateExpired(resource.getDateExpired());
                s.setParentId(parentId);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }
    }

    /**
     * @see org.opencms.db.I_CmsVfsDriver#writeResourceState(org.opencms.db.CmsDbContext, org.opencms.file.CmsProject, org.opencms.file.CmsResource, int, boolean)
     */
    public void writeResourceState(
        CmsDbContext dbc,
        CmsProject project,
        CmsResource resource,
        int changed,
        boolean isPublishing) throws CmsDataAccessException {

        if (project.getUuid().equals(CmsProject.ONLINE_PROJECT_ID)) {
            return;
        }

        try {
            Query q;

            if (changed == CmsDriverManager.UPDATE_RESOURCE_PROJECT) {
                q = m_sqlManager.createQuery(dbc, project, C_RESOURCES_UPDATE_RESOURCE_PROJECT);
                q.setParameter(1, resource.getResourceId().toString());
                @SuppressWarnings("unchecked")
                List<I_CmsDAOResources> resr = q.getResultList();

                for (I_CmsDAOResources r : resr) {
                    r.setResourceFlags(resource.getFlags());
                    r.setProjectLastModified(project.getUuid().toString());
                }
            }

            if (changed == CmsDriverManager.UPDATE_RESOURCE) {
                q = m_sqlManager.createQuery(dbc, project, C_RESOURCES_UPDATE_RESOURCE_STATELASTMODIFIED);
                q.setParameter(1, resource.getResourceId().toString());
                @SuppressWarnings("unchecked")
                List<I_CmsDAOResources> res = q.getResultList();

                for (I_CmsDAOResources r : res) {
                    r.setResourceState(resource.getState().getState());
                    r.setDateLastModified(resource.getDateLastModified());
                    r.setUserLastModified(resource.getUserLastModified().toString());
                    r.setProjectLastModified(project.getUuid().toString());
                }
            }

            if ((changed == CmsDriverManager.UPDATE_RESOURCE_STATE) || (changed == CmsDriverManager.UPDATE_ALL)) {
                q = m_sqlManager.createQuery(dbc, project, C_RESOURCES_UPDATE_RESOURCE_STATE);
                q.setParameter(1, resource.getResourceId().toString());
                @SuppressWarnings("unchecked")
                List<I_CmsDAOResources> res = q.getResultList();

                for (I_CmsDAOResources r : res) {
                    r.setResourceState(resource.getState().getState());
                    r.setProjectLastModified(project.getUuid().toString());
                }
            }

            if ((changed == CmsDriverManager.UPDATE_STRUCTURE)
                || (changed == CmsDriverManager.UPDATE_ALL)
                || (changed == CmsDriverManager.UPDATE_STRUCTURE_STATE)) {
                q = m_sqlManager.createQuery(dbc, project, C_RESOURCES_UPDATE_STRUCTURE_STATE);
                q.setParameter(1, resource.getStructureId().toString());
                @SuppressWarnings("unchecked")
                List<I_CmsDAOStructure> res = q.getResultList();

                for (I_CmsDAOStructure s : res) {
                    s.setStructureState(resource.getState().getState());
                }
            }

            if ((changed == CmsDriverManager.UPDATE_STRUCTURE) || (changed == CmsDriverManager.UPDATE_ALL)) {
                q = m_sqlManager.createQuery(dbc, project, C_RESOURCES_UPDATE_RELEASE_EXPIRED);
                q.setParameter(1, resource.getStructureId().toString());
                @SuppressWarnings("unchecked")
                List<I_CmsDAOStructure> res = q.getResultList();

                for (I_CmsDAOStructure s : res) {
                    s.setDateReleased(resource.getDateReleased());
                    s.setDateExpired(resource.getDateExpired());
                }
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        if (isPublishing) {
            internalUpdateVersions(dbc, resource);
        }
    }

    /**
     * Checks that the current user has write permissions for all subresources of the given folder.<p>
     * 
     * @param dbc the current database context
     * @param folder the folder to check
     * 
     * @throws CmsDataAccessException if something goes wrong 
     */
    protected void checkWritePermissionsInFolder(CmsDbContext dbc, CmsResource folder) throws CmsDataAccessException {

        CmsUUID projectId = dbc.getRequestContext().getCurrentProject().getUuid();

        // first read all subresources with ACEs
        List<CmsResource> resources = new ArrayList<CmsResource>();
        try {
            Query q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_READ_WITH_ACE_1);
            q.setParameter(1, escapeDbWildcard(folder.getRootPath() + "%"));

            @SuppressWarnings("unchecked")
            List<Object[]> res = q.getResultList();

            for (Object[] o : res) {
                resources.add(createResource(o, projectId));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        // check current user write permission for each of these resources
        Iterator<CmsResource> itResources = resources.iterator();
        while (itResources.hasNext()) {
            CmsResource resource = itResources.next();
            try {
                m_driverManager.getSecurityManager().checkPermissions(
                    dbc.getRequestContext(),
                    resource,
                    CmsPermissionSet.ACCESS_WRITE,
                    false,
                    CmsResourceFilter.ALL);
            } catch (CmsException e) {
                throw new CmsDataAccessException(e.getMessageContainer(), e);
            }
        }

        // then check for possible jsp pages without permissions
        CmsResourceFilter filter = CmsResourceFilter.ALL;
        itResources = readTypesInResourceTree(
            dbc,
            projectId,
            folder.getRootPath(),
            CmsResourceTypeJsp.getJspResourceTypeIds(),
            filter.getState(),
            filter.getModifiedAfter(),
            filter.getModifiedBefore(),
            filter.getReleaseAfter(),
            filter.getReleaseBefore(),
            filter.getExpireAfter(),
            filter.getExpireBefore(),
            CmsDriverManager.READMODE_INCLUDE_TREE).iterator();
        while (itResources.hasNext()) {
            CmsResource resource = itResources.next();
            try {
                m_driverManager.getSecurityManager().checkPermissions(
                    dbc.getRequestContext(),
                    resource,
                    CmsPermissionSet.ACCESS_WRITE,
                    false,
                    CmsResourceFilter.ALL);
            } catch (CmsException e) {
                throw new CmsDataAccessException(e.getMessageContainer(), e);
            }
        }
    }

    /**
     * Returns the count of properties for a property definition.<p>
     * 
     * @param dbc the current database context
     * @param propertyDefinition the property definition to test
     * @param projectId the ID of the current project
     * 
     * @return the amount of properties for a property definition
     * @throws CmsDataAccessException if something goes wrong
     */
    protected int internalCountProperties(CmsDbContext dbc, CmsPropertyDefinition propertyDefinition, CmsUUID projectId)
    throws CmsDataAccessException {

        int count = 0;

        try {
            // create statement
            Query q = m_sqlManager.createQuery(dbc, projectId, C_PROPERTIES_READALL_COUNT);
            q.setParameter(1, propertyDefinition.getId().toString());

            try {
                count = CmsDataTypeUtil.numberToInt((Number)q.getSingleResult());
            } catch (NoResultException e) {
                throw new CmsDbConsistencyException(Messages.get().container(
                    Messages.ERR_COUNTING_PROPERTIES_1,
                    propertyDefinition.getName()));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        return count;
    }

    /**
     * Creates an URL name mapping entry from a result set.<p>
     * 
     * @param m a I_CmsDAOUrlNameMappings 
     * @return the URL name mapping entry created from the result set 
     * 
     */
    protected CmsUrlNameMappingEntry internalCreateUrlNameMappingEntry(I_CmsDAOUrlNameMappings m) {

        String name = m.getName();
        CmsUUID structureId = new CmsUUID(m.getStructureId());
        int state = m.getState();
        long dateChanged = m.getDateChanged();
        String locale = m.getLocale();
        return new CmsUrlNameMappingEntry(name, structureId, state, dateChanged, locale);
    }

    /**
     * Returns the parent id of the given resource.<p>
     * 
     * @param dbc the current database context
     * @param projectId the current project id 
     * @param resourcename the resource name to read the parent id for
     * 
     * @return  the parent id of the given resource
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    protected String internalReadParentId(CmsDbContext dbc, CmsUUID projectId, String resourcename)
    throws CmsDataAccessException {

        if ("/".equalsIgnoreCase(resourcename)) {
            return CmsUUID.getNullUUID().toString();
        }

        String parent = CmsResource.getParentFolder(resourcename);
        parent = CmsFileUtil.removeTrailingSeparator(parent);

        String parentId = null;

        try {
            Query q = m_sqlManager.createQuery(dbc, projectId, C_RESOURCES_READ_PARENT_STRUCTURE_ID);
            q.setParameter(1, parent);

            try {
                parentId = (String)q.getSingleResult();
            } catch (NoResultException e) {
                throw new CmsVfsResourceNotFoundException(Messages.get().container(
                    Messages.ERR_READ_PARENT_ID_1,
                    dbc.removeSiteRoot(resourcename)));
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        return parentId;
    }

    /**
     * Creates a new {@link CmsRelation} object from the given result set entry.<p>
     * 
     * @param rr the resource relation 
     *  
     * @return the new {@link CmsRelation} object
     */
    protected CmsRelation internalReadRelation(I_CmsDAOResourceRelations rr) {

        CmsUUID sourceId = new CmsUUID(rr.getRelationSourceId());
        String sourcePath = rr.getRelationSourcePath();
        CmsUUID targetId = new CmsUUID(rr.getRelationTargetId());
        String targetPath = rr.getRelationTargetPath();
        int type = rr.getRelationType();
        return new CmsRelation(sourceId, sourcePath, targetId, targetPath, CmsRelationType.valueOf(type));
    }

    /**
     * Returns the resource state of the given resource.<p>
     * 
     * @param dbc the database context
     * @param projectId the id of the project
     * @param resource the resource to read the resource state for
     * 
     * @return the resource state of the given resource
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    protected CmsResourceState internalReadResourceState(CmsDbContext dbc, CmsUUID projectId, CmsResource resource)
    throws CmsDataAccessException {

        CmsResourceState state = CmsResource.STATE_KEEP;

        try {
            Query q = m_sqlManager.createQuery(dbc, projectId, C_READ_RESOURCE_STATE);
            q.setParameter(1, resource.getResourceId().toString());

            try {
                state = CmsResourceState.valueOf(CmsDataTypeUtil.numberToInt((Number)q.getSingleResult()));
            } catch (NoResultException e) {
                // do nothing
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        return state;
    }

    /**
     * Returns the structure state of the given resource.<p>
     * 
     * @param dbc the database context
     * @param projectId the id of the project
     * @param resource the resource to read the structure state for
     * 
     * @return the structure state of the given resource
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    protected CmsResourceState internalReadStructureState(CmsDbContext dbc, CmsUUID projectId, CmsResource resource)
    throws CmsDataAccessException {

        CmsResourceState state = CmsResource.STATE_KEEP;

        try {
            Query q = m_sqlManager.createQuery(dbc, projectId, C_READ_STRUCTURE_STATE);
            q.setParameter(1, resource.getStructureId().toString());

            try {
                state = CmsResourceState.valueOf(CmsDataTypeUtil.numberToInt((Number)q.getSingleResult()));
            } catch (NoResultException e) {
                // do nothing
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }

        return state;
    }

    /**
     * Removes a resource physically in the database.<p>
     * 
     * @param dbc the current database context
     * @param currentProject the current project
     * @param resource the folder to remove
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    protected void internalRemoveFolder(CmsDbContext dbc, CmsProject currentProject, CmsResource resource)
    throws CmsDataAccessException {

        try {
            // delete the structure record            
            Query q = m_sqlManager.createQuery(dbc, currentProject, C_STRUCTURE_DELETE_BY_STRUCTUREID);
            q.setParameter(1, resource.getStructureId().toString());
            I_CmsDAOStructure s = (I_CmsDAOStructure)q.getSingleResult();
            m_sqlManager.remove(dbc, s);

            // delete the resource record
            q = m_sqlManager.createQuery(dbc, currentProject, C_RESOURCES_DELETE_BY_RESOURCEID);
            q.setParameter(1, resource.getResourceId().toString());
            I_CmsDAOResources r = (I_CmsDAOResources)q.getSingleResult();
            m_sqlManager.remove(dbc, r);
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }
    }

    /**
     * Postprocess C_READ_RESOURCE_OUS query, because some databases 
     * do not support indexOf function.
     * 
     * @param set - result of C_READ_RESOURCE_OUS query
     * @param resName - string for comparison
     * 
     * @return - the result of original C_READ_RESOURCE_OUS query
     */
    protected List<I_CmsDAOResourceRelations> internalResourceOus(List<I_CmsDAOResourceRelations> set, String resName) {

        List<I_CmsDAOResourceRelations> result = new ArrayList<I_CmsDAOResourceRelations>();

        if (resName == null) {
            return result;
        }

        for (I_CmsDAOResourceRelations rr : set) {
            if ((rr.getRelationTargetPath() != null) && (resName.indexOf(rr.getRelationTargetPath()) != -1)) {
                result.add(rr);
            }
        }

        return result;
    }

    /**
     * Updates the offline version numbers.<p>
     *  
     * @param dbc the current database context
     * @param resource the resource to update the version number for
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    protected void internalUpdateVersions(CmsDbContext dbc, CmsResource resource) throws CmsDataAccessException {

        if (dbc.getRequestContext() == null) {
            // no needed during initialization 
            return;
        }
        if (dbc.currentProject().isOnlineProject()) {
            // this method is supposed to be used only in the offline project
            return;
        }

        // read the online version numbers
        Map<String, Integer> onlineVersions = readVersions(
            dbc,
            CmsProject.ONLINE_PROJECT_ID,
            resource.getResourceId(),
            resource.getStructureId());
        int onlineStructureVersion = onlineVersions.get("structure").intValue();
        int onlineResourceVersion = onlineVersions.get("resource").intValue();

        try {
            // update the resource version
            Query q = m_sqlManager.createQuery(dbc, dbc.currentProject(), C_RESOURCES_UPDATE_RESOURCE_VERSION);
            q.setParameter(1, resource.getResourceId().toString());
            @SuppressWarnings("unchecked")
            List<I_CmsDAOResources> res = q.getResultList();

            for (I_CmsDAOResources r : res) {
                r.setResourceVersion(onlineResourceVersion);
            }

            // update the structure version
            q = m_sqlManager.createQuery(dbc, dbc.currentProject(), C_RESOURCES_UPDATE_STRUCTURE_VERSION);
            q.setParameter(1, resource.getStructureId().toString());
            @SuppressWarnings("unchecked")
            List<I_CmsDAOStructure> ress = q.getResultList();

            for (I_CmsDAOStructure s : ress) {
                s.setStructureVersion(onlineStructureVersion);
            }

        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }
    }

    /**
     * Validates that the length setting of a resource is always correct.<p>
     * 
     * Files need to have a resource length of >= 0, while folders require
     * a resource length of -1.<p>
     * 
     * @param resource the resource to check the length for
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    protected void internalValidateResourceLength(CmsResource resource) throws CmsDataAccessException {

        if (resource.isFolder() && (resource.getLength() == -1)) {
            return;
        }

        if (resource.isFile() && (resource.getLength() >= 0)) {
            return;
        }

        throw new CmsDataAccessException(Messages.get().container(
            Messages.ERR_INVALID_RESOURCE_LENGTH_2,
            new Integer(resource.getLength()),
            resource.getRootPath()));
    }

    /**
     * Moves all relations of a resource to the new path.<p>
     * 
     * @param dbc the current database context
     * @param projectId the id of the project to apply the changes 
     * @param structureId the structure id of the resource to apply the changes to
     * @param rootPath the new root path
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    protected void moveRelations(CmsDbContext dbc, CmsUUID projectId, CmsUUID structureId, String rootPath)
    throws CmsDataAccessException {

        try {
            Query q = m_sqlManager.createQuery(dbc, projectId, C_MOVE_RELATIONS_SOURCE);
            q.setParameter(1, structureId.toString());
            @SuppressWarnings("unchecked")
            List<I_CmsDAOResourceRelations> res = q.getResultList();
            I_CmsDAOResourceRelations newR;
            for (I_CmsDAOResourceRelations rr : res) {
                newR = CmsProject.isOnlineProject(projectId)
                ? new CmsDAOOnlineResourceRelations()
                : new CmsDAOOfflineResourceRelations();

                newR.setRelationSourceId(rr.getRelationSourceId());
                newR.setRelationSourcePath(rootPath);
                newR.setRelationTargetId(rr.getRelationTargetId());
                newR.setRelationTargetPath(rr.getRelationTargetPath());
                newR.setRelationType(rr.getRelationType());

                m_sqlManager.remove(dbc, rr);
                m_sqlManager.persist(dbc, newR);
            }

            q = m_sqlManager.createQuery(dbc, projectId, C_MOVE_RELATIONS_TARGET);
            q.setParameter(1, structureId.toString());
            @SuppressWarnings("unchecked")
            List<I_CmsDAOResourceRelations> res1 = q.getResultList();
            for (I_CmsDAOResourceRelations rr : res1) {
                newR = CmsProject.isOnlineProject(projectId)
                ? new CmsDAOOnlineResourceRelations()
                : new CmsDAOOfflineResourceRelations();

                newR.setRelationSourceId(rr.getRelationSourceId());
                newR.setRelationSourcePath(rr.getRelationSourcePath());
                newR.setRelationTargetId(rr.getRelationTargetId());
                newR.setRelationTargetPath(rootPath);
                newR.setRelationType(rr.getRelationType());

                m_sqlManager.remove(dbc, rr);
                m_sqlManager.persist(dbc, newR);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }
    }

    /**
     * Appends the appropriate selection criteria related with the expiration date.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param startTime the start time
     * @param endTime the end time
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection parameters
     */
    protected void prepareExpiredTimeRangeCondition(
        CmsUUID projectId,
        long startTime,
        long endTime,
        StringBuffer conditions,
        List params) {

        if (startTime > 0L) {
            // READ_IGNORE_TIME: if NOT set, add condition to match expired date against startTime
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, C_STRUCTURE_SELECT_BY_DATE_EXPIRED_AFTER));
            conditions.append(END_CONDITION);
            params.add(Long.valueOf(startTime));
        }

        if (endTime > 0L) {
            // READ_IGNORE_TIME: if NOT set, add condition to match expired date against endTime
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, C_STRUCTURE_SELECT_BY_DATE_EXPIRED_BEFORE));
            conditions.append(END_CONDITION);
            params.add(Long.valueOf(endTime));
        }
    }

    /**
     * Appends the appropriate selection criteria related with the parentPath.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param parent the parent path or UUID (if mode is C_READMODE_EXCLUDE_TREE)
     * @param mode the selection mode
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection parameters
     */
    protected void preparePathCondition(CmsUUID projectId, String parent, int mode, StringBuffer conditions, List params) {

        if (parent == CmsDriverManager.READ_IGNORE_PARENT) {
            // parent can be ignored
            return;
        }

        if ((mode & CmsDriverManager.READMODE_EXCLUDE_TREE) > 0) {
            // only return immediate children - use UUID optimization            
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, C_RESOURCES_SELECT_BY_PARENT_UUID));
            conditions.append(END_CONDITION);
            params.add(parent);
            return;
        }

        if ("/".equalsIgnoreCase(parent)) {
            // if root folder is parent, no additional condition is needed since all resources match anyway
            return;
        }

        // add condition to read path subtree        
        conditions.append(BEGIN_INCLUDE_CONDITION);
        conditions.append(m_sqlManager.readQuery(projectId, C_RESOURCES_SELECT_BY_PATH_PREFIX));
        conditions.append(END_CONDITION);
        params.add(CmsFileUtil.addTrailingSeparator(escapeDbWildcard(parent)) + "%");
    }

    /**
     * Appends the appropriate selection criteria related with the projectId.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param mode the selection mode
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection parameters
     */
    protected void prepareProjectCondition(CmsUUID projectId, int mode, StringBuffer conditions, List params) {

        if ((mode & CmsDriverManager.READMODE_INCLUDE_PROJECT) > 0) {
            // C_READMODE_INCLUDE_PROJECT: add condition to match the PROJECT_ID
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, C_RESOURCES_SELECT_BY_PROJECT_LASTMODIFIED));
            conditions.append(END_CONDITION);
            params.add(String.valueOf(projectId));
        }
    }

    /**
     * Build the whole WHERE sql statement part for the given relation filter.<p>
     * 
     * @param projectId the current project id
     * @param filter the filter
     * @param resource the resource (may be null, if you want to delete all relations for the resource in the filter)
     * @param params the parameter values (return parameter)
     * @param checkSource if the query is for the source relations 
     * 
     * @return the WHERE sql statement part string
     */
    protected String prepareRelationConditions(
        CmsUUID projectId,
        CmsRelationFilter filter,
        CmsResource resource,
        List params,
        boolean checkSource) {

        StringBuffer conditions = new StringBuffer(128);
        params.clear(); // be sure the parameters list is clear

        // source or target filter
        if (filter.isSource() || filter.isTarget()) {
            // source or target id filter from resource
            if (resource != null) {
                conditions.append(BEGIN_CONDITION);
                if (filter.isSource() && checkSource) {
                    if (!filter.isIncludeSubresources()) {
                        conditions.append(m_sqlManager.readQuery(projectId, C_RELATION_FILTER_TARGET_ID));
                        params.add(resource.getStructureId().toString());
                    } else {
                        conditions.append(m_sqlManager.readQuery(projectId, C_RELATION_FILTER_TARGET_PATH));
                        params.add(resource.getRootPath() + '%');
                    }
                } else if (filter.isTarget() && !checkSource) {
                    if (!filter.isIncludeSubresources()) {
                        conditions.append(m_sqlManager.readQuery(projectId, C_RELATION_FILTER_SOURCE_ID));
                        params.add(resource.getStructureId().toString());
                    } else {
                        conditions.append(m_sqlManager.readQuery(projectId, C_RELATION_FILTER_SOURCE_PATH));
                        params.add(resource.getRootPath() + '%');
                    }
                }
                conditions.append(END_CONDITION);
            }

            // target or source id filter from filter parameter
            if (filter.getStructureId() != null) {
                if (conditions.length() == 0) {
                    conditions.append(BEGIN_CONDITION);
                } else {
                    conditions.append(BEGIN_INCLUDE_CONDITION);
                }

                if (filter.isSource() && checkSource) {
                    conditions.append(m_sqlManager.readQuery(projectId, C_RELATION_FILTER_SOURCE_ID));
                    params.add(filter.getStructureId().toString());
                } else if (filter.isTarget() && !checkSource) {
                    conditions.append(m_sqlManager.readQuery(projectId, C_RELATION_FILTER_TARGET_ID));
                    params.add(filter.getStructureId().toString());
                }
                conditions.append(END_CONDITION);
            }

            // target or source path filter from filter parameter
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(filter.getPath())) {
                if (conditions.length() == 0) {
                    conditions.append(BEGIN_CONDITION);
                } else {
                    conditions.append(BEGIN_INCLUDE_CONDITION);
                }

                String queryPath = filter.getPath();
                if (filter.isIncludeSubresources()) {
                    queryPath += '%';
                }
                if (filter.isSource() && checkSource) {
                    conditions.append(m_sqlManager.readQuery(projectId, C_RELATION_FILTER_SOURCE_PATH));
                    params.add(queryPath);
                } else if (filter.isTarget() && !checkSource) {
                    conditions.append(m_sqlManager.readQuery(projectId, C_RELATION_FILTER_TARGET_PATH));
                    params.add(queryPath);
                }
                conditions.append(END_CONDITION);
            }
        }

        // relation type filter
        Set types = filter.getTypes();
        if (!types.isEmpty()) {
            if (conditions.length() == 0) {
                conditions.append(BEGIN_CONDITION);
            } else {
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            conditions.append(m_sqlManager.readQuery(projectId, C_RELATION_FILTER_TYPE));
            conditions.append(BEGIN_CONDITION);
            Iterator it = types.iterator();
            while (it.hasNext()) {
                CmsRelationType type = (CmsRelationType)it.next();
                conditions.append("?");
                params.add(Integer.valueOf(type.getId()));
                if (it.hasNext()) {
                    conditions.append(", ");
                }
            }
            conditions.append(END_CONDITION);
            conditions.append(END_CONDITION);
        }
        return conditions.toString();
    }

    /**
     * Appends the appropriate selection criteria related with the released date.<p>
     * 
     * @param projectId the id of the project
     * @param startTime the start time
     * @param endTime the stop time
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection parameters
     */
    protected void prepareReleasedTimeRangeCondition(
        CmsUUID projectId,
        long startTime,
        long endTime,
        StringBuffer conditions,
        List params) {

        if (startTime > 0L) {
            // READ_IGNORE_TIME: if NOT set, add condition to match released date against startTime
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, C_STRUCTURE_SELECT_BY_DATE_RELEASED_AFTER));
            conditions.append(END_CONDITION);
            params.add(Long.valueOf(startTime));
        }

        if (endTime > 0L) {
            // READ_IGNORE_TIME: if NOT set, add condition to match released date against endTime
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, C_STRUCTURE_SELECT_BY_DATE_RELEASED_BEFORE));
            conditions.append(END_CONDITION);
            params.add(Long.valueOf(endTime));
        }
    }

    /**
     * Appends the appropriate selection criteria related with the read mode.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param mode the selection mode
     * @param conditions buffer to append the selection criteria
     */
    protected void prepareResourceCondition(CmsUUID projectId, int mode, StringBuffer conditions) {

        if ((mode & CmsDriverManager.READMODE_ONLY_FOLDERS) > 0) {
            // C_READMODE_ONLY_FOLDERS: add condition to match only folders
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, C_RESOURCES_SELECT_ONLY_FOLDERS));
            conditions.append(END_CONDITION);
        } else if ((mode & CmsDriverManager.READMODE_ONLY_FILES) > 0) {
            // C_READMODE_ONLY_FILES: add condition to match only files
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, C_RESOURCES_SELECT_ONLY_FILES));
            conditions.append(END_CONDITION);
        }
    }

    /**
     * Appends the appropriate selection criteria related with the resource state.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param state the resource state
     * @param mode the selection mode
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection parameters
     */
    protected void prepareStateCondition(
        CmsUUID projectId,
        CmsResourceState state,
        int mode,
        StringBuffer conditions,
        List params) {

        if (state != null) {
            if ((mode & CmsDriverManager.READMODE_EXCLUDE_STATE) > 0) {
                // C_READ_MODIFIED_STATES: add condition to match against any state but not given state
                conditions.append(BEGIN_EXCLUDE_CONDITION);
            } else {
                // otherwise add condition to match against given state if necessary
                conditions.append(BEGIN_INCLUDE_CONDITION);
            }
            conditions.append(m_sqlManager.readQuery(projectId, C_RESOURCES_SELECT_BY_RESOURCE_STATE));
            conditions.append(END_CONDITION);
            params.add(Integer.valueOf(state.getState()));
            params.add(Integer.valueOf(state.getState()));
        }
    }

    /**
     * Appends the appropriate selection criteria related with the date of the last modification.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param startTime start of the time range
     * @param endTime end of the time range
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection parameters
     */
    protected void prepareTimeRangeCondition(
        CmsUUID projectId,
        long startTime,
        long endTime,
        StringBuffer conditions,
        List params) {

        if (startTime > 0L) {
            // READ_IGNORE_TIME: if NOT set, add condition to match last modified date against startTime
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, C_RESOURCES_SELECT_BY_DATE_LASTMODIFIED_AFTER));
            conditions.append(END_CONDITION);
            params.add(Long.valueOf(startTime));
        }

        if (endTime > 0L) {
            // READ_IGNORE_TIME: if NOT set, add condition to match last modified date against endTime
            conditions.append(BEGIN_INCLUDE_CONDITION);
            conditions.append(m_sqlManager.readQuery(projectId, C_RESOURCES_SELECT_BY_DATE_LASTMODIFIED_BEFORE));
            conditions.append(END_CONDITION);
            params.add(Long.valueOf(endTime));
        }
    }

    /**
     * Appends the appropriate selection criteria related with the resource type.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param type the resource type
     * @param mode the selection mode
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection parameters
     */
    protected void prepareTypeCondition(CmsUUID projectId, int type, int mode, StringBuffer conditions, List params) {

        if (type != CmsDriverManager.READ_IGNORE_TYPE) {
            if ((mode & CmsDriverManager.READMODE_EXCLUDE_TYPE) > 0) {
                // C_READ_FILE_TYPES: add condition to match against any type, but not given type
                conditions.append(BEGIN_EXCLUDE_CONDITION);
                conditions.append(m_sqlManager.readQuery(projectId, C_RESOURCES_SELECT_BY_RESOURCE_TYPE));
                conditions.append(END_CONDITION);
                params.add(Integer.valueOf(type));
            } else {
                //otherwise add condition to match against given type if necessary
                conditions.append(BEGIN_INCLUDE_CONDITION);
                conditions.append(m_sqlManager.readQuery(projectId, C_RESOURCES_SELECT_BY_RESOURCE_TYPE));
                conditions.append(END_CONDITION);
                params.add(Integer.valueOf(type));
            }
        }
    }

    /**
     * Appends the appropriate selection criteria related with the resource type.<p>
     * 
     * @param projectId the id of the project of the resources
     * @param types the resource type id's
     * @param mode the selection mode
     * @param conditions buffer to append the selection criteria
     * @param params list to append the selection parameters
     */
    protected void prepareTypesCondition(
        CmsUUID projectId,
        List<Integer> types,
        int mode,
        StringBuffer conditions,
        List params) {

        if ((types == null) || types.isEmpty()) {
            if ((mode & CmsDriverManager.READMODE_EXCLUDE_TYPE) > 0) {
                // C_READ_FILE_TYPES: add condition to match against any type, but not given type
                conditions.append(BEGIN_EXCLUDE_CONDITION);
                conditions.append(m_sqlManager.readQuery(projectId, C_RESOURCES_SELECT_BY_RESOURCE_TYPE));
                conditions.append(END_CONDITION);
                params.add(Integer.valueOf(CmsDriverManager.READ_IGNORE_TYPE));
            } else {
                //otherwise add condition to match against given type if necessary
                conditions.append(BEGIN_INCLUDE_CONDITION);
                Iterator<Integer> typeIt = types.iterator();
                while (typeIt.hasNext()) {
                    conditions.append(m_sqlManager.readQuery(projectId, C_RESOURCES_SELECT_BY_RESOURCE_TYPE));
                    params.add(typeIt.next());
                    if (typeIt.hasNext()) {
                        conditions.append(OR_CONDITION);
                    }
                }
                conditions.append(END_CONDITION);
            }
        }
    }

    /**
     * Reads all resources inside a given project matching the criteria specified by parameter values.<p>
     * 
     * Important: If {@link CmsDriverManager#READMODE_EXCLUDE_TREE} is true (or {@link CmsDriverManager#READMODE_INCLUDE_TREE} is false), 
     * the provided parent String must be the UUID of the parent folder, NOT the parent folder path.<p>
     * 
     * @param dbc the current database context
     * @param projectId the project id for matching resources
     * @param parentPath the path to the resource used as root of the searched subtree or {@link CmsDriverManager#READ_IGNORE_PARENT}, 
     *               {@link CmsDriverManager#READMODE_EXCLUDE_TREE} means to read immediate children only 
     * @param types the resource types of matching resources or <code>null</code> (meaning inverted by {@link CmsDriverManager#READMODE_EXCLUDE_TYPE}
     * @param state the state of matching resources (meaning inverted by {@link CmsDriverManager#READMODE_EXCLUDE_STATE} or <code>null</code> to ignore
     * @param lastModifiedAfter the start of the time range for the last modification date of matching resources or READ_IGNORE_TIME 
     * @param lastModifiedBefore the end of the time range for the last modification date of matching resources or READ_IGNORE_TIME
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
    protected List<CmsResource> readTypesInResourceTree(
        CmsDbContext dbc,
        CmsUUID projectId,
        String parentPath,
        List<Integer> types,
        CmsResourceState state,
        long lastModifiedAfter,
        long lastModifiedBefore,
        long releasedAfter,
        long releasedBefore,
        long expiredAfter,
        long expiredBefore,
        int mode) throws CmsDataAccessException {

        List<CmsResource> result = new ArrayList<CmsResource>();

        StringBuffer conditions = new StringBuffer();
        List params = new ArrayList(5);

        // prepare the selection criteria
        prepareProjectCondition(projectId, mode, conditions, params);
        prepareResourceCondition(projectId, mode, conditions);
        prepareTypesCondition(projectId, types, mode, conditions, params);
        prepareTimeRangeCondition(projectId, lastModifiedAfter, lastModifiedBefore, conditions, params);
        prepareReleasedTimeRangeCondition(projectId, releasedAfter, releasedBefore, conditions, params);
        prepareExpiredTimeRangeCondition(projectId, expiredAfter, expiredBefore, conditions, params);
        preparePathCondition(projectId, parentPath, mode, conditions, params);
        prepareStateCondition(projectId, state, mode, conditions, params);

        // now read matching resources within the subtree 
        List<Object[]> res = null;

        try {
            StringBuffer queryBuf = new StringBuffer(256);
            queryBuf.append(m_sqlManager.readQuery(projectId, C_RESOURCES_READ_TREE));
            queryBuf.append(conditions);
            queryBuf.append(" ");
            queryBuf.append(m_sqlManager.readQuery(projectId, C_RESOURCES_ORDER_BY_PATH));
            Query q = m_sqlManager.createQueryFromJPQL(dbc, queryBuf.toString());

            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof Integer) {
                    q.setParameter(i + 1, ((Integer)params.get(i)).intValue());
                } else if (params.get(i) instanceof Long) {
                    q.setParameter(i + 1, ((Long)params.get(i)).longValue());
                } else {
                    q.setParameter(i + 1, params.get(i));
                }
            }

            res = q.getResultList();
            for (Object[] obj : res) {
                CmsResource resource = createResource(obj, projectId);
                result.add(resource);
            }

        } catch (PersistenceException e) {
            throw new CmsDbSqlException(Messages.get().container(Messages.ERR_GENERIC_SQL_1, C_RESOURCES_READ_TREE), e);
        }

        return result;
    }

    /**
     * Repairs broken links.<p>
     * 
     * When a resource is created any relation pointing to it is updated to use the right id.<p>
     * 
     * @param dbc the current database context
     * @param projectId the project id
     * @param structureId the structure id of the resource that may help to repair broken links
     * @param rootPath the path of the resource that may help to repair broken links
     * 
     * @throws CmsDataAccessException if something goes wrong
     */
    protected void repairBrokenRelations(CmsDbContext dbc, CmsUUID projectId, CmsUUID structureId, String rootPath)
    throws CmsDataAccessException {

        try {
            Query q = m_sqlManager.createQuery(dbc, projectId, C_RELATIONS_REPAIR_BROKEN);
            q.setParameter(1, rootPath);
            @SuppressWarnings("unchecked")
            List<I_CmsDAOResourceRelations> res = q.getResultList();
            I_CmsDAOResourceRelations newR;
            for (I_CmsDAOResourceRelations rr : res) {
                newR = CmsProject.isOnlineProject(projectId)
                ? new CmsDAOOnlineResourceRelations()
                : new CmsDAOOfflineResourceRelations();

                newR.setRelationSourceId(rr.getRelationSourceId());
                newR.setRelationSourcePath(rr.getRelationSourcePath());
                newR.setRelationTargetId(structureId.toString());
                newR.setRelationTargetPath(rr.getRelationTargetPath());
                newR.setRelationType(rr.getRelationType());

                m_sqlManager.remove(dbc, rr);
                m_sqlManager.persist(dbc, newR);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }
    }

    /**
     * Updates broken links.<p>
     * 
     * When a resource is deleted, then the relations pointing to 
     * the deleted resource are set to the null uuid.<p>
     * 
     * @param dbc the current database context
     * @param projectId the project id
     * @param rootPath the root path of the resource that has been deleted 
     * 
     * @throws CmsDataAccessException if something goes wrong 
     */
    protected void updateBrokenRelations(CmsDbContext dbc, CmsUUID projectId, String rootPath)
    throws CmsDataAccessException {

        try {
            Query q = m_sqlManager.createQuery(dbc, projectId, C_RELATIONS_UPDATE_BROKEN);
            q.setParameter(1, rootPath);
            @SuppressWarnings("unchecked")
            List<I_CmsDAOResourceRelations> res = q.getResultList();
            I_CmsDAOResourceRelations newR;
            for (I_CmsDAOResourceRelations rr : res) {
                newR = CmsProject.isOnlineProject(projectId)
                ? new CmsDAOOnlineResourceRelations()
                : new CmsDAOOfflineResourceRelations();

                newR.setRelationSourceId(rr.getRelationSourceId());
                newR.setRelationSourcePath(rr.getRelationSourcePath());
                newR.setRelationTargetId(CmsUUID.getNullUUID().toString());
                newR.setRelationTargetPath(rr.getRelationTargetPath());
                newR.setRelationType(rr.getRelationType());

                m_sqlManager.remove(dbc, rr);
                m_sqlManager.persist(dbc, newR);
            }
        } catch (PersistenceException e) {
            throw new CmsDataAccessException(Messages.get().container(Messages.ERR_JPA_PERSITENCE, e), e);
        }
    }

    /**
     * Creates a query by combining a base query with the generated JPQL conditions for a given
     * URL name mapping filter.<p>
     *  
     * @param dbc the db context 
     * @param baseQuery the base query to which the conditions should be appended 
     * @param filter the filter from which to generate the conditions 
     * @param online what project to use - ONLINE or OFFLINE project
     * 
     * @return the created prepared statement 
     * 
     * @throws PersistenceException if something goes wrong
     */
    private Query getQueryForFilter(CmsDbContext dbc, String baseQuery, CmsUrlNameMappingFilter filter, boolean online)
    throws PersistenceException {

        CmsPair<String, List<I_CmsQueryParameter>> conditionData = prepareUrlNameMappingConditions(filter);
        String whereClause = "";
        if (!conditionData.getFirst().equals("")) {
            whereClause = " WHERE " + conditionData.getFirst();
        }
        String query = baseQuery + whereClause;
        query = replaceProject(query, online);
        Query q = m_sqlManager.createQueryFromJPQL(dbc, query);
        int counter = 1;
        for (I_CmsQueryParameter param : conditionData.getSecond()) {
            param.insertIntoQuery(q, counter);
            counter += 1;
        }
        return q;
    }

    /**
     * Replaces the %(PROJECT) macro inside a query with either Online or Offline, depending on the value 
     * of a flag.<p>
     * 
     * We use this instead of the ${PROJECT} replacement mechanism when we need explicit control over the 
     * project, and don't want to implicitly use the project of the DB context.<p>
     * 
     * @param query the query in which the macro should be replaced 
     * @param online if true, the macro will be replaced with "ONLINE", else "OFFLINE"
     * 
     * @return the query with the replaced macro 
     */
    private String replaceProject(String query, boolean online) {

        return query.replace("%(PROJECT)", online ? CmsSqlManager.ONLINE_PROJECT : CmsSqlManager.OFFLINE_PROJECT);
    }
}

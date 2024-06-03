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

package org.opencms.importexport;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.db.CmsDbEntryNotFoundException;
import org.opencms.db.log.CmsLogEntry;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceBuilder;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeXmlAdeConfiguration;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.importexport.CmsImportExportManager.TimestampMode;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelationType;
import org.opencms.relations.I_CmsLinkParseable;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPasswordHandler;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.ui.apps.lists.CmsListManager;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsDataTypeUtil;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlErrorHandler;
import org.opencms.xml.content.CmsXmlContent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipFile;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.Rule;
import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.google.common.base.Objects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Adds the XML handler rules for import and export of resources and accounts.<p>
 *
 * @since 7.0.4
 */
public class CmsImportVersion10 implements I_CmsImport {

    /**
     * Categories of resources that need to be handled differently in the 'rewrite parseables' import step.
     */
    public static enum LinkParsableCategory {
        /** ADE config (formatters, sitemap configs) */
        config,

        /** Container pages. */
        page,

        /** Other contents. */
        other;
    }

    /**
     * Data class to temporarily keep track of relation data for a resource to be imported.<p>
     */
    public static class RelationData {

        /** The relation target. */
        private String m_target;

        /** The target structure id. */
        private CmsUUID m_targetId;

        /** The relation type. */
        private CmsRelationType m_type;

        /**
         * Creates a new instance.<p>
         *
         * @param target the relation target path
         * @param targetId the relation target structure id
         * @param type the relation type
         */
        public RelationData(String target, CmsUUID targetId, CmsRelationType type) {

            super();
            m_target = target;
            m_type = type;
            m_targetId = targetId;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object otherObj) {

            if (!(otherObj instanceof RelationData)) {
                return false;
            }
            RelationData other = (RelationData)otherObj;
            return Objects.equal(m_target, other.m_target)
                && Objects.equal(m_type, other.m_type)
                && Objects.equal(m_targetId, other.m_targetId);
        }

        /**
         * Gets the relation target path.<p>
         *
         * @return the relation target path
         */
        public String getTarget() {

            return m_target;
        }

        /**
         * Gets the relation target structure id.<p>
         *
         * @return the relation target structure id
         */
        public CmsUUID getTargetId() {

            return m_targetId;
        }

        /**
         * Gets the relation type.<p>
         *
         * @return the relation type
         */
        public CmsRelationType getType() {

            return m_type;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            return Objects.hashCode(m_target, m_type.getName(), m_targetId);

        }

    }

    /** Tag for the "userinfo / entry name" attribute, contains the additional user info entry name. */
    public static final String A_NAME = "name";

    /** Tag for the "type" attribute, contains the property type. */
    public static final String A_TYPE = "type";

    /** The name of the DTD for this import version. */
    public static final String DTD_FILENAME = "opencms-import10.dtd";

    /** The location of the OpenCms configuration DTD if the default prefix is the system ID. */
    public static final String DTD_LOCATION = "org/opencms/importexport/";

    /** The version number of this import implementation.<p> */
    public static final int IMPORT_VERSION10 = 10;

    /** Tag for the "allowed" node, to identify allowed user permissions. */
    public static final String N_ACCESSCONTROL_ALLOWEDPERMISSIONS = "allowed";

    /** Tag for the "denied" node, to identify denied user permissions. */
    public static final String N_ACCESSCONTROL_DENIEDPERMISSIONS = "denied";

    /** Tag for the "accesscontrol" node, to identify access control entries. */
    public static final String N_ACCESSCONTROL_ENTRIES = "accesscontrol";

    /** Tag for the "accessentry" node, to identify a single access control entry. */
    public static final String N_ACCESSCONTROL_ENTRY = "accessentry";

    /** Tag for the "permissionset" node, to identify a permission set. */
    public static final String N_ACCESSCONTROL_PERMISSIONSET = "permissionset";

    /** Tag for the "uuidprincipal" node, to identify a principal UUID. */
    public static final String N_ACCESSCONTROL_PRINCIPAL = "uuidprincipal";

    /** Tag for the "accounts" node. */
    public static final String N_ACCOUNTS = "accounts";

    /** Tag for the "datecreated" node, contains the date created VFS file attribute. */
    public static final String N_DATECREATED = "datecreated";

    /** Tag for the "dateexpired" node, contains the expiration date VFS file attribute. */
    public static final String N_DATEEXPIRED = "dateexpired";

    /** Tag for the "datelastmodified" node, contains the date last modified VFS file attribute. */
    public static final String N_DATELASTMODIFIED = "datelastmodified";

    /** Tag for the "datereleased" node, contains the release date VFS file attribute. */
    public static final String N_DATERELEASED = "datereleased";

    /** Tag for the "description" node, contains a users description test. */
    public static final String N_DESCRIPTION = "description";

    /** Tag for the "destination" node, contains target VFS file name. */
    public static final String N_DESTINATION = "destination";

    /** Tag for the "email" node, contains a users email. */
    public static final String N_EMAIL = "email";

    /** Tag for the "file" node, container node for all VFS resources. */
    public static final String N_FILE = "file";

    /** Tag for the "files" node, container node for all VFS resources. */
    public static final String N_FILES = "files";

    /** Tag for the "firstname" node, contains a users first name. */
    public static final String N_FIRSTNAME = "firstname";

    /** Tag for the "flags" node, contains the flags of a VFS resource. */
    public static final String N_FLAGS = "flags";

    /** Tag for the "group" node, contains a group name. */
    public static final String N_GROUP = "group";

    /** Tag for the "groups" node, contains a users group data. */
    public static final String N_GROUPS = "groups";

    /** Tag for the "id" relation attribute, contains the structure id of the target resource of the relation. */
    public static final String N_ID = "id";

    /** Tag for the "lastname" node, contains a users last name. */
    public static final String N_LASTNAME = "lastname";

    /** Tag for the "managersgroup" node, contains name of the managers group of the project. */
    public static final String N_MANAGERSGROUP = "managersgroup";

    /** Tag for the "name" node, contains the name of a property. */
    public static final String N_NAME = "name";

    /** Tag for the "orgunit" node, starts the organizational unit data. */
    public static final String N_ORGUNIT = "orgunit";

    /** Tag for the "orgunits" node, starts the organizational unit data. */
    public static final String N_ORGUNITS = "orgunits";

    /** Tag for the "parentgroup" node, contains a groups parent group fqn. */
    public static final String N_PARENTGROUP = "parentgroup";

    /** Tag for the "password" node, contains a users encrypted password. */
    public static final String N_PASSWORD = "password";

    /** Tag for the "path" relation attribute, contains the path to the target resource of the relation. */
    public static final String N_PATH = "path";

    /** Tag for the "project" node, starts the project data. */
    public static final String N_PROJECT = "project";

    /** Tag for the "projects" node, starts the project data. */
    public static final String N_PROJECTS = "projects";

    /** Tag for the "properties" node, starts the list of properties of a VFS resource. */
    public static final String N_PROPERTIES = "properties";

    /** Tag for the "property" node, starts a property for a VFS resource. */
    public static final String N_PROPERTY = "property";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "relation" node, starts a relation for a VFS resource. */
    public static final String N_RELATION = "relation";

    /** Tag for the "relations" node, starts the list of relations of a VFS resources. */
    public static final String N_RELATIONS = "relations";

    /** Tag for the "resource" node, contains the a organizational unit resource name. */
    public static final String N_RESOURCE = "resource";

    /** Tag for the "resources" node, contains the list of organizational unit resources. */
    public static final String N_RESOURCES = "resources";

    /** Tag for the "source" node, contains the source path of a VFS resource in the import zip (or folder). */
    public static final String N_SOURCE = "source";

    /** Tag for the "type" node, the resource type name of a VFS resource. */
    public static final String N_TYPE = "type";

    /** Tag for the "user" node, starts the user data. */
    public static final String N_USER = "user";

    /** Tag for the "usercreated" node, contains the name of the user who created the VFS resource. */
    public static final String N_USERCREATED = "usercreated";

    /** Tag for the "usergroup" node, the name of a users group. */
    public static final String N_USERGROUP = "usergroup";

    /** Tag for the "usergroups" node, starts the users group data. */
    public static final String N_USERGROUPS = "usergroups";

    /** Tag for the "userinfo" node, contains the additional user info. */
    public static final String N_USERINFO = "userinfo";

    /** Tag for the "userinfo/entry" node, contains the additional user info entry value. */
    public static final String N_USERINFO_ENTRY = "entry";

    /** Tag for the "userlastmodified" node, contains the name of the user who last modified the VFS resource. */
    public static final String N_USERLASTMODIFIED = "userlastmodified";

    /** Tag for the "userrole" node, contains an users role name. */
    public static final String N_USERROLE = "userrole";

    /** Tag for the "userroles" node, starts the users role data. */
    public static final String N_USERROLES = "userroles";

    /** Tag for the "users" node, starts the list of users. */
    public static final String N_USERS = "users";

    /** Tag for the "usersgroup" node, contains name of the users group of the project. */
    public static final String N_USERSGROUP = "usersgroup";

    /** Tag for the "uuidresource" node, contains a the resource UUID of a VFS resource. */
    public static final String N_UUIDRESOURCE = "uuidresource";

    /** Tag for the "uuidstructure" node, only required for backward compatibility with import version 2. */
    public static final String N_UUIDSTRUCTURE = "uuidstructure";

    /** Tag for the "value" node, contains the value of a property. */
    public static final String N_VALUE = "value";

    /** Value for the "shared" property type attribute value. */
    public static final String PROPERTY_ATTRIB_TYPE_SHARED = "shared";

    /** Constant for the unspecified creation date. */
    protected static final long DATE_CREATED_UNSPECIFIED = -1;

    /** Constant for using file time as last modification date on file import. */
    protected static final long DATE_LAST_MODIFICATION_FILETIME = -1;

    /** Constant for an unspecified last modification date. */
    protected static final long DATE_LAST_MODIFICATION_UNSPECIFIED = -2;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsImportVersion10.class);

    /** Cumulative time spent waiting for config updates. */
    private static final AtomicLong cumulativeConfigWaitTime = new AtomicLong(0l);

    /** The ACE flags value. */
    protected int m_aceFlags;

    /** The ACE allowed permissions value. */
    protected int m_acePermissionsAllowed;

    /** The ACE denied permissions value. */
    protected int m_acePermissionsDenied;

    /** The ACE principal id value. */
    protected CmsUUID m_acePrincipalId;

    /** The list of ACEs for the current imported resource. */
    protected List<CmsAccessControlEntry> m_aces;

    /** The cms object. */
    protected CmsObject m_cms;

    /** The set of resource ids of files which actually are contained in the zip file. */
    protected Set<CmsUUID> m_contentFiles = new HashSet<CmsUUID>();

    /** The destination value. */
    protected String m_destination;

    /** The current file counter. */
    protected int m_fileCounter;

    /** The description of the current group to import. */
    protected String m_groupDescription;

    /** The flags of the current group to import. */
    protected int m_groupFlags;

    /** The name of the current group to import. */
    protected String m_groupName;

    /** The parent of the current group to import. */
    protected String m_groupParent;

    /** Map of all parent groups that could not be set immediately, because of the import order. */
    protected Map<String, List<String>> m_groupParents;

    /** True if a modification date has been set. */
    protected boolean m_hasDateLastModified;

    /** True if a structure id has been set. */
    protected boolean m_hasStructureId;

    /** The import helper. */
    protected CmsImportHelper m_helper;

    /** List of ignored properties. */
    protected List<String> m_ignoredProperties;

    /** List of immutable resources. */
    protected List<String> m_immutables;

    /** The flag to import ACEs. */
    protected boolean m_importACEs;

    /** The membership structure. */
    protected Map<String, Map<String, Map<String, String>>> m_membership;

    /** The current imported organizational unit. */
    protected CmsOrganizationalUnit m_orgUnit;

    /** The organizational unit description. */
    protected String m_orgUnitDescription;

    /** The organizational unit flags. */
    protected int m_orgUnitFlags;

    /** The organizational unit fqn. */
    protected String m_orgUnitName;

    /** The map of organizational unit resources, this is a global field that will be use at the end of the import. */
    protected Map<String, List<String>> m_orgUnitResources;

    /** The import parameters to use. */
    protected CmsImportParameters m_parameters;

    /** The list of resource to be parsed, this is a global list, which will be handled at the end of the import. */
    protected List<CmsResource> m_parseables;

    /** The project description. */
    protected String m_projectDescription;

    /** The project managers group name. */
    protected String m_projectManagers;

    /** The project fqn. */
    protected String m_projectName;

    /** The current read project resources. */
    protected List<String> m_projectResources;

    /** The project users group name. */
    protected String m_projectUsers;

    /** The map of properties for current imported resource. */
    protected Map<String, CmsProperty> m_properties;

    /** The property name value. */
    protected String m_propertyName;

    /** The property value value. */
    protected String m_propertyValue;

    /** The relation id value. */
    protected CmsUUID m_relationId;

    /** The relation path value. */
    protected String m_relationPath;

    /** Holds the relation data for the resource to be imported. */
    protected List<RelationData> m_relationsForResource;

    /** The relation type value. */
    protected CmsRelationType m_relationType;

    /** The report. */
    protected I_CmsReport m_report;

    /** The current imported resource. */
    protected CmsResource m_resource;

    /** Holds the field values for the CmsResource object to be created. */
    protected CmsResourceBuilder m_resourceBuilder;

    /** The source value. */
    protected String m_source;

    /** Possible exception during xml parsing. */
    protected Throwable m_throwable;

    /** The total number of files to import. */
    protected int m_totalFiles;

    /** The type name. */
    protected String m_typeName;

    /** The current imported user. */
    protected CmsUser m_user;

    /** The current user date created. */
    protected long m_userDateCreated;

    /** The current user email. */
    protected String m_userEmail;

    /** The current user first name. */
    protected String m_userFirstname;

    /** The current user flags. */
    protected int m_userFlags;

    /** The additional information for the current imported user. */
    protected Map<String, Object> m_userInfos;

    /** The current user last name. */
    protected String m_userLastname;

    /** The current user name. */
    protected String m_userName;

    /** The current user password. */
    protected String m_userPassword;

    /** The export version. */
    protected int m_version;

    /**
     * Maps index of files in import to structure ids of imported resources.
     * Necessary because not all entries in the manifest may have a structure id, and even for entries
     * with a structure id, there may be a pre-existing resource at the same path with a different structure id.
     */
    private Map<Integer, CmsUUID> m_indexToStructureId;

    /** Map to keep track of relation data for resources to be imported. */
    private Multimap<Integer, RelationData> m_relationData;

    /** True if the resource id has not been set. */
    private boolean m_resourceIdWasNull;

    /** Flag which indicates whether the resource type name from the manifest was not found. */
    private boolean m_unknownType;

    /**
     * Public constructor.<p>
     */
    public CmsImportVersion10() {

        // empty
    }

    /**
     * Parses the links.<p>
     *
     * @param cms the CMS context to use
     * @param parseables the list of resources for which to parse the links
     * @param report the report
     */
    public static void parseLinks(CmsObject cms, List<CmsResource> parseables, I_CmsReport report) {

        int i = 0;

        // group parseables into categories that need to be handled differently.
        //
        //  - container pages need to be rewritten before contents (because the contents' gallery titles
        //    may reference the container page they're in)
        //
        // - configurations need to be rewritten *and* loaded before container pages
        //   (because how the container page is written may depend on a sitemap configuration
        //   in the same import).

        ArrayListMultimap<LinkParsableCategory, CmsResource> resourcesByCategory = ArrayListMultimap.create();
        for (CmsResource resource : parseables) {
            LinkParsableCategory category = LinkParsableCategory.other;
            if (CmsResourceTypeXmlContainerPage.isContainerPage(resource)) {
                category = LinkParsableCategory.page;
            } else {
                I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(resource);
                if ((resType instanceof CmsResourceTypeXmlAdeConfiguration)
                    && !OpenCms.getResourceManager().matchResourceType(
                        CmsListManager.RES_TYPE_LIST_CONFIG,
                        resource.getTypeId())) {
                    category = LinkParsableCategory.config;
                }
            }
            resourcesByCategory.put(category, resource);
        }

        for (LinkParsableCategory category : Arrays.asList(
            LinkParsableCategory.config,
            LinkParsableCategory.page,
            LinkParsableCategory.other)) {

            List<CmsResource> resourcesInCurrentCategory = resourcesByCategory.get(category);
            resourcesInCurrentCategory.sort((a, b) -> a.getRootPath().compareTo(b.getRootPath()));
            for (CmsResource parsableRes : resourcesInCurrentCategory) {
                String resName = cms.getSitePath(parsableRes);

                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_SUCCESSION_2,
                        String.valueOf(i + 1),
                        String.valueOf(parseables.size())),
                    I_CmsReport.FORMAT_NOTE);

                LOG.info("Rewriting parsable resource: " + resName);
                report.print(
                    Messages.get().container(Messages.RPT_PARSE_LINKS_FOR_1, resName),
                    I_CmsReport.FORMAT_NOTE);
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                try {
                    CmsFile file = cms.readFile(resName);
                    // make sure the date last modified is kept...
                    file.setDateLastModified(file.getDateLastModified());
                    // make sure the file is locked
                    CmsLock lock = cms.getLock(file);
                    if (lock.isUnlocked()) {
                        cms.lockResource(resName);
                    } else if (!lock.isDirectlyOwnedInProjectBy(cms)) {
                        cms.changeLock(resName);
                    }
                    // rewrite the file
                    cms.getRequestContext().setAttribute(CmsXmlContent.AUTO_CORRECTION_ATTRIBUTE, Boolean.TRUE);
                    try {
                        cms.writeFile(file);
                    } finally {
                        cms.getRequestContext().removeAttribute(CmsXmlContent.AUTO_CORRECTION_ATTRIBUTE);
                    }

                    report.println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                        I_CmsReport.FORMAT_OK);
                } catch (Throwable e) {
                    report.addWarning(e);
                    report.println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0),
                        I_CmsReport.FORMAT_ERROR);
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(Messages.get().getBundle().key(Messages.LOG_IMPORTEXPORT_REWRITING_1, resName));
                        LOG.warn(e.getMessage(), e);
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e.getLocalizedMessage(), e);
                    }
                }
                i++;
            }
            if ((category == LinkParsableCategory.config) && (resourcesInCurrentCategory.size() > 0)) {
                long start = System.currentTimeMillis();
                OpenCms.getADEManager().waitForFormatterCache(false);
                OpenCms.getADEManager().waitForCacheUpdate(false);
                long end = System.currentTimeMillis();
                long waitTime = end - start;
                long cumulative = cumulativeConfigWaitTime.addAndGet(waitTime);
                LOG.debug("Waited " + (waitTime / 1000.0) + "s for configuration in parseLinks, total = " + cumulative);

            }
        }
        cms.getRequestContext().removeAttribute(CmsLogEntry.ATTR_LOG_ENTRY);
    }

    /**
     * Adds an ACE from the current xml data.<p>
     *
     * @see #addResourceAceRules(Digester, String)
     */
    public void addAccessControlEntry() {

        try {
            if (m_throwable != null) {
                // user or group of ACE might not exist in target system, ignore ACE
                if (LOG.isWarnEnabled()) {
                    LOG.warn(
                        Messages.get().getBundle().key(
                            Messages.LOG_IMPORTEXPORT_ERROR_IMPORTING_ACE_1,
                            getRequestContext().removeSiteRoot(m_resource.getRootPath())),
                        m_throwable);
                }
                getReport().println(m_throwable);
                getReport().addError(m_throwable);
                m_throwable = null;
                return;
            }
            if (m_aces == null) {
                // this list will be used and clean up in the importResource and importAccessControlEntries methods
                m_aces = new ArrayList<CmsAccessControlEntry>();
            }
            m_aces.add(
                new CmsAccessControlEntry(
                    null,
                    m_acePrincipalId,
                    m_acePermissionsAllowed,
                    m_acePermissionsDenied,
                    m_aceFlags));
        } finally {
            m_acePrincipalId = null;
            m_acePermissionsAllowed = 0;
            m_acePermissionsDenied = 0;
            m_aceFlags = 0;
        }
    }

    /**
     * Registers a file whose contents are contained in the zip file.<p>
     *
     * @param source the path in the zip file
     *
     * @param resourceId the resource id
     */
    public void addContentFile(String source, String resourceId) {

        if ((source != null) && (resourceId != null)) {
            try {
                m_helper.getFileBytes(source);
                m_contentFiles.add(new CmsUUID(resourceId));
            } catch (@SuppressWarnings("unused") CmsImportExportException e) {
                LOG.info("File not found in import: " + source);
            }
        }
    }

    /**
     * Adds a new resource to be associated to the current organizational unit.<p>
     *
     * @param resourceName the resource name to add
     */
    public void addOrgUnitResource(String resourceName) {

        if ((m_throwable != null) || (m_orgUnitName == null)) {
            return;
        }
        if (m_orgUnitResources == null) {
            m_orgUnitResources = new HashMap<String, List<String>>();
        }
        List<String> resources = m_orgUnitResources.get(m_orgUnitName);
        if (resources == null) {
            resources = new ArrayList<String>();
            m_orgUnitResources.put(m_orgUnitName, resources);
        }
        resources.add(resourceName);
    }

    /**
     * Adds a new resource to be associated to the current project.<p>
     *
     * @param resourceName the resource name to add
     */
    public void addProjectResource(String resourceName) {

        if ((m_throwable != null) || (m_projectName == null)) {
            return;
        }
        if (m_projectResources == null) {
            m_projectResources = new ArrayList<String>();
        }
        m_projectResources.add(resourceName);
    }

    /**
     * Adds a property from the current xml data, in case the type is implicit given.<p>
     *
     * @see #addResourcePropertyRules(Digester, String)
     */
    public void addProperty() {

        addProperty("individual");
    }

    /**
     * Adds a property from the current xml data, in case the type is explicit given.<p>
     *
     * @param propertyType the type of the property to be added
     *
     * @see #addResourcePropertyRules(Digester, String)
     */
    public void addProperty(String propertyType) {

        if (m_properties == null) {
            // this list will be used and clean up in the importResource method
            m_properties = new HashMap<String, CmsProperty>();
        }
        try {
            if ((m_propertyName == null) || getIgnoredProperties().contains(m_propertyName)) {
                // continue if the current property (name) should be ignored or is null
                return;
            }
            CmsProperty property = m_properties.get(m_propertyName);
            if (property == null) {
                property = new CmsProperty();
                property.setName(m_propertyName);
                property.setAutoCreatePropertyDefinition(true);
                m_properties.put(m_propertyName, property);
            }

            if (m_propertyValue == null) {
                m_propertyValue = "";
            }

            if ((propertyType != null) && propertyType.equals(PROPERTY_ATTRIB_TYPE_SHARED)) {
                // it is a shared/resource property value
                property.setResourceValue(m_propertyValue);
            } else {
                // it is an individual/structure value
                property.setStructureValue(m_propertyValue);
            }
        } finally {
            m_propertyName = null;
            m_propertyValue = null;
        }
    }

    /**
     * Adds a relation to be imported from the current xml data.<p>
     *
     * @see #addResourceRelationRules(Digester, String)
     */
    public void addRelation() {

        try {
            if (m_throwable != null) {
                // relation data is corrupt, ignore relation
                if (LOG.isWarnEnabled()) {
                    LOG.warn(
                        Messages.get().getBundle().key(
                            Messages.LOG_IMPORTEXPORT_ERROR_IMPORTING_RELATION_1,
                            m_destination),
                        m_throwable);
                }
                getReport().println(m_throwable);
                getReport().addError(m_throwable);
                m_throwable = null;
                return;
            }
            RelationData relData = new RelationData(m_relationPath, m_relationId, m_relationType);
            m_relationsForResource.add(relData);
            m_relationData.put(Integer.valueOf(m_fileCounter), relData);
        } finally {
            m_relationId = null;
            m_relationPath = null;
            m_relationType = null;
        }
    }

    /**
     * Adds the XML digester rules for a single import file.<p>
     *
     * @param digester the digester to add the rules to
     */
    public void addXmlDigesterRules(Digester digester) {

        // first accounts
        String xpath = CmsImportExportManager.N_EXPORT + "/" + N_ACCOUNTS + "/" + N_ORGUNITS + "/" + N_ORGUNIT + "/";
        addAccountsOrgunitRules(digester, xpath);
        addAccountsGroupRules(digester, xpath);
        addAccountsUserRules(digester, xpath);
        digester.addCallMethod(
            CmsImportExportManager.N_EXPORT + "/" + N_ACCOUNTS + "/" + N_ORGUNITS + "/" + N_ORGUNIT,
            "setMembership");

        // then resources

        // When encountering opening files tag, initialize some fields which keep track of multiple resources'  data
        digester.addRule(CmsImportExportManager.N_EXPORT + "/" + N_FILES, new Rule() {

            @SuppressWarnings("synthetic-access")
            @Override
            public void begin(String namespace, String name, Attributes attributes) throws Exception {

                m_indexToStructureId = new HashMap<>();
                m_relationData = ArrayListMultimap.create();

            }
        });

        digester.addRule(CmsImportExportManager.N_EXPORT + "/" + N_FILES + "/" + N_FILE, new Rule() {

            @Override
            public void begin(String namespace, String name, Attributes attributes) throws Exception {

                m_importACEs = true;
                m_resourceBuilder = new CmsResourceBuilder();
                m_resourceBuilder.setDateLastModified(DATE_LAST_MODIFICATION_UNSPECIFIED);
                m_resourceBuilder.setDateReleased(CmsResource.DATE_RELEASED_DEFAULT);
                m_resourceBuilder.setDateExpired(CmsResource.DATE_EXPIRED_DEFAULT);
                m_resourceBuilder.setDateCreated(DATE_CREATED_UNSPECIFIED);
                m_resourceBuilder.setUserCreated(CmsUUID.getNullUUID());
                m_relationsForResource = new ArrayList<>();
                m_hasStructureId = false;
                m_hasDateLastModified = false;
                m_unknownType = false;
            }
        });

        xpath = CmsImportExportManager.N_EXPORT + "/" + N_FILES + "/" + N_FILE + "/";
        addResourceAttributesRules(digester, xpath);
        digester.addCallMethod(xpath, "importResourceAll");
        addResourcePropertyRules(digester, xpath);
        addResourceRelationRules(digester, xpath);
        addResourceAceRules(digester, xpath);

        digester.addCallMethod(CmsImportExportManager.N_EXPORT + "/" + N_FILES, "importRelations");
        digester.addCallMethod(CmsImportExportManager.N_EXPORT + "/" + N_FILES, "rewriteParseables");

        // and now the organizational unit resources
        digester.addCallMethod(CmsImportExportManager.N_EXPORT + "/" + N_FILES, "associateOrgUnitResources");

        // then projects
        xpath = CmsImportExportManager.N_EXPORT + "/" + N_PROJECTS + "/" + N_PROJECT + "/";
        addProjectRules(digester, xpath);
    }

    /**
     * Adds the XML digester rules for pre-processing a single import file.<p>
     *
     * @param digester the digester to add the rules to
     */
    public void addXmlPreprocessingDigesterRules(Digester digester) {

        digester.addCallMethod(CmsImportExportManager.N_EXPORT + "/" + N_FILES + "/" + N_FILE, "increaseTotalFiles");
        digester.addCallMethod(
            CmsImportExportManager.N_EXPORT
                + "/"
                + CmsImportExportManager.N_INFO
                + "/"
                + CmsImportExportManager.N_VERSION,
            "setVersion",
            0);
    }

    /**
     * Associates the stored resources to the created organizational units.<p>
     *
     * This is a global process that occurs only once at the end of the import,
     * after all resources have been imported, to make sure that the resources
     * of the organizational units are available.<p>
     *
     * @see #addAccountsOrgunitRules(Digester, String)
     * @see #addXmlDigesterRules(Digester)
     */
    public void associateOrgUnitResources() {

        if ((m_orgUnitResources == null) || m_orgUnitResources.isEmpty()) {
            // no organizational resources to associate
            return;
        }

        String site = getRequestContext().getSiteRoot();
        try {
            getRequestContext().setSiteRoot("");
            List<String> orgUnits = new ArrayList<String>(m_orgUnitResources.keySet());
            Collections.sort(orgUnits);
            Iterator<String> it = orgUnits.iterator();
            while (it.hasNext()) {
                String orgUnitName = it.next();
                List<String> resources = m_orgUnitResources.get(orgUnitName);

                if (orgUnitName.equals("")) {
                    continue;
                }

                Iterator<String> itResources = resources.iterator();
                while (itResources.hasNext()) {
                    String resourceName = itResources.next();
                    try {
                        // Add the resource to the organizational unit
                        OpenCms.getOrgUnitManager().addResourceToOrgUnit(getCms(), orgUnitName, resourceName);
                    } catch (CmsException e) {
                        getReport().addWarning(e);
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(e.getLocalizedMessage());
                        }
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(e.getLocalizedMessage(), e);
                        }
                    }
                }

                // remove the meanwhile used first resource of the parent organizational unit
                try {
                    String resName = (OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
                        getCms(),
                        CmsOrganizationalUnit.getParentFqn(orgUnitName)).get(0)).getRootPath();
                    if (!resources.contains(resName)) {
                        OpenCms.getOrgUnitManager().removeResourceFromOrgUnit(getCms(), orgUnitName, resName);
                    }
                } catch (CmsException e) {
                    getReport().addWarning(e);
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(e.getLocalizedMessage());
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e.getLocalizedMessage(), e);
                    }
                }

            }
        } finally {
            getRequestContext().setSiteRoot(site);
        }

        m_orgUnitResources = null;
    }

    /**
     * Returns the ace Flags.<p>
     *
     * @return the ace Flags
     *
     * @see #N_FLAGS
     * @see #addResourceAceRules(Digester, String)
     */
    public int getAceFlags() {

        return m_aceFlags;
    }

    /**
     * Returns the ace Permissions Allowed.<p>
     *
     * @return the ace Permissions Allowed
     *
     * @see #N_ACCESSCONTROL_ALLOWEDPERMISSIONS
     * @see #addResourceAceRules(Digester, String)
     */
    public int getAcePermissionsAllowed() {

        return m_acePermissionsAllowed;
    }

    /**
     * Returns the acePermissionsDenied.<p>
     *
     * @return the acePermissionsDenied
     *
     * @see #N_ACCESSCONTROL_DENIEDPERMISSIONS
     * @see #addResourceAceRules(Digester, String)
     */
    public int getAcePermissionsDenied() {

        return m_acePermissionsDenied;
    }

    /**
     * Returns the acePrincipalId.<p>
     *
     * @return the acePrincipalId
     *
     * @see #N_ACCESSCONTROL_PRINCIPAL
     * @see #addResourceAceRules(Digester, String)
     */
    public CmsUUID getAcePrincipalId() {

        return m_acePrincipalId;
    }

    /**
     * Returns the cms object.<p>
     *
     * @return the cms object
     */
    public CmsObject getCms() {

        return m_cms;
    }

    /**
     * Returns the dateCreated.<p>
     *
     * @return the dateCreated
     *
     * @see #N_DATECREATED
     * @see #addResourceAttributesRules(Digester, String)
     */
    public long getDateCreated() {

        return m_resourceBuilder.getDateCreated();
    }

    /**
     * Returns the dateExpired.<p>
     *
     * @return the dateExpired
     *
     * @see #N_DATEEXPIRED
     * @see #addResourceAttributesRules(Digester, String)
     */
    public long getDateExpired() {

        return m_resourceBuilder.getDateExpired();
    }

    /**
     * Returns the dateLastModified.<p>
     *
     * @return the dateLastModified
     *
     * @see #N_DATELASTMODIFIED
     * @see #addResourceAttributesRules(Digester, String)
     */
    public long getDateLastModified() {

        return m_resourceBuilder.getDateLastModified();
    }

    /**
     * Returns the dateReleased.<p>
     *
     * @return the dateReleased
     *
     * @see #N_DATERELEASED
     * @see #addResourceAttributesRules(Digester, String)
     */
    public long getDateReleased() {

        return m_resourceBuilder.getDateReleased();
    }

    /**
     * Returns the destination.<p>
     *
     * @return the destination
     *
     * @see #N_DESTINATION
     * @see #addResourceAttributesRules(Digester, String)
     */
    public String getDestination() {

        return m_destination;
    }

    /**
     * Returns the flags.<p>
     *
     * @return the flags
     *
     * @see #N_FLAGS
     * @see #addResourceAttributesRules(Digester, String)
     */
    public int getFlags() {

        return m_resourceBuilder.getFlags();
    }

    /**
     * Returns the group Description.<p>
     *
     * @return the group Description
     */
    public String getGroupDescription() {

        return m_groupDescription;
    }

    /**
     * Returns the group Flags.<p>
     *
     * @return the group Flags
     */
    public int getGroupFlags() {

        return m_groupFlags;
    }

    /**
     * Returns the group Name.<p>
     *
     * @return the group Name
     */
    public String getGroupName() {

        return m_groupName;
    }

    /**
     * Returns the group Parent.<p>
     *
     * @return the group Parent
     */
    public String getGroupParent() {

        return m_groupParent;
    }

    /**
     * Returns the organizational unit description.<p>
     *
     * @return the organizational unit description
     */
    public String getOrgUnitDescription() {

        return m_orgUnitDescription;
    }

    /**
     * Returns the organizational unit flags.<p>
     *
     * @return the organizational unit flags
     */
    public int getOrgUnitFlags() {

        return m_orgUnitFlags;
    }

    /**
     * Returns the organizational unit name.<p>
     *
     * @return the organizational unit name
     */
    public String getOrgUnitName() {

        return m_orgUnitName;
    }

    /**
     * Returns the project Description.<p>
     *
     * @return the project Description
     */
    public String getProjectDescription() {

        return m_projectDescription;
    }

    /**
     * Returns the project Managers group name.<p>
     *
     * @return the project Managers group name
     */
    public String getProjectManagers() {

        return m_projectManagers;
    }

    /**
     * Returns the project Name.<p>
     *
     * @return the project Name
     */
    public String getProjectName() {

        return m_projectName;
    }

    /**
     * Returns the project Users group name.<p>
     *
     * @return the project Users group name
     */
    public String getProjectUsers() {

        return m_projectUsers;
    }

    /**
     * Returns the propertyName.<p>
     *
     * @return the propertyName
     *
     * @see #N_NAME
     * @see #addResourcePropertyRules(Digester, String)
     */
    public String getPropertyName() {

        return m_propertyName;
    }

    /**
     * Returns the propertyValue.<p>
     *
     * @return the propertyValue
     *
     * @see #N_VALUE
     * @see #addResourcePropertyRules(Digester, String)
     */
    public String getPropertyValue() {

        return m_propertyValue;
    }

    /**
     * Returns the relationId.<p>
     *
     * @return the relationId
     *
     * @see #N_ID
     * @see #addResourceRelationRules(Digester, String)
     */
    public CmsUUID getRelationId() {

        return m_relationId;
    }

    /**
     * Returns the relationPath.<p>
     *
     * @return the relationPath
     *
     * @see #N_PATH
     * @see #addResourceRelationRules(Digester, String)
     */
    public String getRelationPath() {

        return m_relationPath;
    }

    /**
     * Returns the relationType.<p>
     *
     * @return the relationType
     *
     * @see #N_TYPE
     * @see #addResourceRelationRules(Digester, String)
     */
    public CmsRelationType getRelationType() {

        return m_relationType;
    }

    /**
     * Returns the report.<p>
     *
     * @return the report
     */
    public I_CmsReport getReport() {

        return m_report;
    }

    /**
     * Gets the request context of the currently used CmsObject.<p>
     *
     * @return the current request context
     */
    public CmsRequestContext getRequestContext() {

        return getCms().getRequestContext();
    }

    /**
     * Returns the resourceId.<p>
     *
     * @return the resourceId
     *
     * @see #N_UUIDRESOURCE
     * @see #addResourceAttributesRules(Digester, String)
     */
    public CmsUUID getResourceId() {

        return m_resourceBuilder.getResourceId();
    }

    /**
     * Returns the source.<p>
     *
     * @return the source
     *
     * @see #N_SOURCE
     * @see #addResourceAttributesRules(Digester, String)
     */
    public String getSource() {

        return m_source;
    }

    /**
     * Returns the structureId.<p>
     *
     * @return the structureId
     *
     * @see #N_UUIDSTRUCTURE
     * @see #addResourceAttributesRules(Digester, String)
     */
    public CmsUUID getStructureId() {

        return m_resourceBuilder.getStructureId();
    }

    /**
     * Returns the throwable.<p>
     *
     * @return the throwable
     */
    public Throwable getThrowable() {

        return m_throwable;
    }

    /**
     * Returns the type.<p>
     *
     * @return the type
     *
     * @see #N_TYPE
     * @see #addResourceAttributesRules(Digester, String)
     */
    public I_CmsResourceType getType() {

        return null;
    }

    /**
     * Returns the userCreated.<p>
     *
     * @return the userCreated
     *
     * @see #N_USERCREATED
     * @see #addResourceAttributesRules(Digester, String)
     */
    public CmsUUID getUserCreated() {

        return m_resourceBuilder.getUserCreated();
    }

    /**
     * Returns the user Date Created.<p>
     *
     * @return the user Date Created
     */
    public long getUserDateCreated() {

        return m_userDateCreated;
    }

    /**
     * Returns the user Email address.<p>
     *
     * @return the user Email address
     */
    public String getUserEmail() {

        return m_userEmail;
    }

    /**
     * Returns the user First name.<p>
     *
     * @return the user First name
     */
    public String getUserFirstname() {

        return m_userFirstname;
    }

    /**
     * Returns the user Flags.<p>
     *
     * @return the user Flags
     */
    public int getUserFlags() {

        return m_userFlags;
    }

    /**
     * Returns the userLastModified.<p>
     *
     * @return the userLastModified
     *
     * @see #N_USERLASTMODIFIED
     * @see #addResourceAttributesRules(Digester, String)
     */
    public CmsUUID getUserLastModified() {

        return m_resourceBuilder.getUserLastModified();
    }

    /**
     * Returns the user Last name.<p>
     *
     * @return the user Last name
     */
    public String getUserLastname() {

        return m_userLastname;
    }

    /**
     * Returns the user Name.<p>
     *
     * @return the user Name
     */
    public String getUserName() {

        return m_userName;
    }

    /**
     * Returns the user Password.<p>
     *
     * @return the user Password
     */
    public String getUserPassword() {

        return m_userPassword;
    }

    /**
     * @see org.opencms.importexport.I_CmsImport#getVersion()
     */
    public int getVersion() {

        return IMPORT_VERSION10;
    }

    /**
     * Imports an ACE from the current xml data.<p>
     *
     * @see #addResourceAceRules(Digester, String)
     */
    public void importAccessControlEntries() {

        // only set permissions if the resource did not exists or if the keep permissions flag is not set
        if ((m_resource == null) || !m_importACEs) {
            return;
        }
        if ((m_aces == null) || (m_aces.size() == 0)) {
            // no ACE in the list
            return;
        }
        // if the resource was imported add the access control entries if available
        try {
            getCms().importAccessControlEntries(m_resource, m_aces);
        } catch (@SuppressWarnings("unused") CmsException exc) {
            getReport().println(
                Messages.get().container(Messages.RPT_IMPORT_ACL_DATA_FAILED_0),
                I_CmsReport.FORMAT_WARNING);
        } finally {
            m_aces = null;
        }
    }

    /**
     * @see org.opencms.importexport.I_CmsImport#importData(CmsObject, I_CmsReport, CmsImportParameters)
     */
    public void importData(CmsObject cms, I_CmsReport report, CmsImportParameters parameters) {

        m_cms = cms;
        m_report = report;
        m_parameters = parameters;

        // instantiate Digester and enable XML validation
        Digester digester = new Digester();
        digester.setUseContextClassLoader(true);
        digester.setValidating(m_parameters.isXmlValidation());
        digester.setEntityResolver(new CmsXmlEntityResolver(null));
        digester.setRuleNamespaceURI(null);
        digester.setErrorHandler(new CmsXmlErrorHandler(CmsImportExportManager.EXPORT_MANIFEST));

        // add this object to the Digester
        digester.push(this);

        addXmlDigesterRules(digester);

        InputStream stream = null;
        m_helper = new CmsImportHelper(m_parameters);
        try {
            m_helper.openFile();
            m_helper.cacheDtdSystemId(DTD_LOCATION, DTD_FILENAME, CmsConfigurationManager.DEFAULT_DTD_PREFIX);
            findContentFiles();
            // start the parsing process
            stream = m_helper.getFileStream(CmsImportExportManager.EXPORT_MANIFEST);
            digester.parse(stream);
        } catch (Exception ioe) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.ERR_IMPORTEXPORT_ERROR_READING_FILE_1,
                        CmsImportExportManager.EXPORT_MANIFEST),
                    ioe);
            }
            getReport().println(ioe);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (@SuppressWarnings("unused") Exception e) {
                // noop
            }
            m_helper.closeFile();
        }
    }

    /**
     * Import the current group from xml data.<p>
     */
    public void importGroup() {

        if (m_orgUnit == null) {
            return;
        }
        if (m_groupDescription == null) {
            m_groupDescription = "";
        }
        if (m_groupParents == null) {
            m_groupParents = new HashMap<String, List<String>>();
        }

        String groupName = m_orgUnit.getName() + m_groupName;
        try {
            if (m_throwable != null) {
                getReport().println(m_throwable);

                CmsMessageContainer message = Messages.get().container(
                    Messages.ERR_IMPORTEXPORT_ERROR_IMPORTING_GROUP_1,
                    groupName);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(message.key(), m_throwable);
                }
                m_throwable = null;
                return;
            }

            getReport().print(Messages.get().container(Messages.RPT_IMPORT_GROUP_0), I_CmsReport.FORMAT_NOTE);
            getReport().print(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, groupName));
            getReport().print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            try {
                getCms().readGroup(groupName);
                // the group already exists and will not be created
                getReport().println(Messages.get().container(Messages.RPT_NOT_CREATED_0), I_CmsReport.FORMAT_OK);
            } catch (@SuppressWarnings("unused") CmsDbEntryNotFoundException e) {
                // ok, let's create it
                // first check the parent group
                CmsUUID parentGroupId = null;
                if (CmsStringUtil.isNotEmpty(m_groupParent)) {
                    try {
                        // parent group exists
                        parentGroupId = getCms().readGroup(m_groupParent).getId();
                    } catch (@SuppressWarnings("unused") CmsDbEntryNotFoundException exc) {
                        // parent group does not exist, remember to set the parent group later
                        List<String> childs = m_groupParents.get(m_groupParent);
                        if (childs == null) {
                            childs = new ArrayList<String>();
                            m_groupParents.put(m_groupParent, childs);
                        }
                        childs.add(groupName);
                    }
                }

                getCms().createGroup(
                    groupName,
                    m_groupDescription,
                    m_groupFlags,
                    parentGroupId == null ? null : m_groupParent);
                getReport().println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);

                // set parents that could not be set before
                List<String> childs = m_groupParents.remove(groupName);
                if (childs != null) {
                    Iterator<String> it = childs.iterator();
                    while (it.hasNext()) {
                        String childGroup = it.next();
                        getCms().setParentGroup(childGroup, groupName);
                    }
                }
            }
        } catch (Exception e) {
            getReport().println(e);

            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_IMPORTING_GROUP_1,
                groupName);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }
        } finally {
            m_groupDescription = null;
            m_groupFlags = 0;
            m_groupName = null;
            m_groupParent = null;
            m_throwable = null;
        }
    }

    /**
     * Imports the current organizational unit.<p>
     */
    public void importOrgUnit() {

        try {
            if (m_throwable != null) {
                getReport().println(m_throwable);
                getReport().addError(m_throwable);

                CmsMessageContainer message = Messages.get().container(
                    Messages.ERR_IMPORTEXPORT_ERROR_IMPORTING_ORGUNITS_0);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(message.key(), m_throwable);
                }
                m_throwable = null;
                m_orgUnit = null;

                return;
            }

            getReport().print(Messages.get().container(Messages.RPT_IMPORT_ORGUNIT_0), I_CmsReport.FORMAT_NOTE);
            getReport().print(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, m_orgUnitName));
            getReport().print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            try {
                m_orgUnit = OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), m_orgUnitName);
                // the organizational unit already exists and will not be created
                getReport().println(Messages.get().container(Messages.RPT_NOT_CREATED_0), I_CmsReport.FORMAT_OK);
                m_orgUnitResources.remove(m_orgUnitName);
                return;
            } catch (@SuppressWarnings("unused") CmsDataAccessException e) {
                // ok, continue creating the ou
            }

            // get the resources that already exist for the organizational unit
            // if there are resources that does not exist jet, there will be a second try after importing resources
            List<CmsResource> resources = new ArrayList<CmsResource>();
            String site = getRequestContext().getSiteRoot();
            try {
                getRequestContext().setSiteRoot("");

                boolean remove = true;
                List<String> ouResources = CmsCollectionsGenericWrapper.list(m_orgUnitResources.get(m_orgUnitName));
                if (ouResources != null) {
                    Iterator<String> itResNames = ouResources.iterator();
                    while (itResNames.hasNext()) {
                        String resName = itResNames.next();
                        try {
                            resources.add(getCms().readResource(resName, CmsResourceFilter.ALL));
                            itResNames.remove();
                        } catch (@SuppressWarnings("unused") CmsVfsResourceNotFoundException e) {
                            // resource does not exist yet, skip it for now
                            remove = false;
                        }
                    }
                }

                if (remove) {
                    m_orgUnitResources.remove(m_orgUnitName);
                }
            } finally {
                getRequestContext().setSiteRoot(site);
            }

            // if no resource available
            if (resources.isEmpty()) {
                // meanwhile use the first one of the parent organizational unit
                resources.add(
                    OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
                        getCms(),
                        CmsOrganizationalUnit.getParentFqn(m_orgUnitName)).get(0));
            }

            // create the organizational unit with a dummy resource, which will be corrected later
            m_orgUnit = OpenCms.getOrgUnitManager().createOrganizationalUnit(
                getCms(),
                m_orgUnitName,
                m_orgUnitDescription,
                m_orgUnitFlags,
                resources.get(0).getRootPath());
            for (int i = 1; i < resources.size(); i++) {
                OpenCms.getOrgUnitManager().addResourceToOrgUnit(
                    getCms(),
                    m_orgUnitName,
                    resources.get(i).getRootPath());
            }

            getReport().println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                I_CmsReport.FORMAT_OK);
        } catch (CmsException e) {
            getReport().println(e);
            getReport().addError(e);

            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_IMPORTING_ORGUNITS_0);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }
            m_throwable = null;
            m_orgUnit = null;
        } finally {
            m_orgUnitName = null;
            m_orgUnitDescription = null;
            m_orgUnitFlags = 0;
        }

    }

    /**
     * Imports the current project.<p>
     */
    public void importProject() {

        try {
            if (m_throwable != null) {
                getReport().println(m_throwable);
                getReport().addError(m_throwable);

                CmsMessageContainer message = Messages.get().container(
                    Messages.ERR_IMPORTEXPORT_ERROR_IMPORTING_PROJECTS_0);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(message.key(), m_throwable);
                }
                m_throwable = null;

                return;
            }

            getReport().print(Messages.get().container(Messages.RPT_IMPORT_PROJECT_0), I_CmsReport.FORMAT_NOTE);
            getReport().print(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, m_projectName));
            getReport().print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            try {
                getCms().readProject(m_projectName);
                // the project already exists and will not be created
                getReport().println(Messages.get().container(Messages.RPT_NOT_CREATED_0), I_CmsReport.FORMAT_OK);
                return;
            } catch (@SuppressWarnings("unused") CmsDataAccessException e) {
                // ok, continue creating the project
            }

            // create the project
            CmsProject project = getCms().createProject(
                m_projectName,
                m_projectDescription,
                m_projectUsers,
                m_projectManagers,
                CmsProject.PROJECT_TYPE_NORMAL);
            // set the resources
            if (m_projectResources != null) {
                String site = getRequestContext().getSiteRoot();
                CmsProject currentProject = getRequestContext().getCurrentProject();
                try {
                    getRequestContext().setSiteRoot("");
                    getRequestContext().setCurrentProject(project);

                    Iterator<String> itResNames = m_projectResources.iterator();
                    while (itResNames.hasNext()) {
                        String resName = itResNames.next();
                        try {
                            getCms().copyResourceToProject(resName);
                        } catch (@SuppressWarnings("unused") CmsVfsResourceNotFoundException e) {
                            // resource does not exist, skip
                        }
                    }
                } finally {
                    getRequestContext().setSiteRoot(site);
                    getRequestContext().setCurrentProject(currentProject);
                }
            }
            getReport().println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                I_CmsReport.FORMAT_OK);
        } catch (CmsException e) {
            getReport().println(e);
            getReport().addError(e);

            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_IMPORTING_PROJECTS_0);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }
            m_throwable = null;
        } finally {
            m_projectName = null;
            m_projectDescription = null;
            m_projectManagers = null;
            m_projectUsers = null;
            m_projectResources = null;
        }

    }

    /**
     * Imports all relations from the current xml data.<p>
     *
     * This is a global process that occurs only once at the end of the import,
     * after all resources have been imported, to make sure that both resources
     * of the relations are available.<p>
     *
     * @see #addResourceRelationRules(Digester, String)
     * @see #addXmlDigesterRules(Digester)
     */
    public void importRelations() {

        if ((m_relationData == null) || m_relationData.isEmpty()) {
            // no relations to add
            return;
        }

        getReport().println(
            Messages.get().container(Messages.RPT_START_IMPORT_RELATIONS_0),
            I_CmsReport.FORMAT_HEADLINE);

        int i = 0;
        CmsResourceFilter filter = CmsResourceFilter.ALL;
        for (Integer importIndex : m_relationData.keySet()) {
            CmsUUID structureId = m_indexToStructureId.get(importIndex);
            if (structureId == null) {
                continue;
            }
            Collection<RelationData> relationDataList = m_relationData.get(importIndex);
            try {
                CmsResource src = m_cms.readResource(structureId, filter);
                if (checkImmutable(src.getRootPath())) {
                    continue;
                }
                getReport().print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_SUCCESSION_2,
                        String.valueOf(i + 1),
                        String.valueOf(m_relationData.keySet().size())),
                    I_CmsReport.FORMAT_NOTE);

                getReport().print(
                    Messages.get().container(
                        Messages.RPT_IMPORTING_RELATIONS_FOR_2,
                        src.getRootPath(),
                        Integer.valueOf(m_relationData.keySet().size())),
                    I_CmsReport.FORMAT_NOTE);
                getReport().print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                boolean withErrors = false;
                for (RelationData relationData : relationDataList) {
                    CmsResource target = null;
                    if (relationData.getTargetId() != null) {
                        try {
                            target = m_cms.readResource(relationData.getTargetId(), filter);
                        } catch (CmsVfsResourceNotFoundException e) {
                            // ignore
                        }
                    }
                    if (target == null) {
                        try {
                            target = m_cms.readResource(relationData.getTarget(), filter);
                        } catch (CmsVfsResourceNotFoundException e) {
                            // ignore
                        }
                    }

                    if (target != null) {
                        try {
                            getCms().importRelation(
                                m_cms.getSitePath(src),
                                m_cms.getSitePath(target),
                                relationData.getType().toString());
                        } catch (CmsException e) {
                            getReport().addWarning(e);
                            withErrors = true;
                            if (LOG.isWarnEnabled()) {
                                LOG.warn(e.getLocalizedMessage());
                            }
                            if (LOG.isDebugEnabled()) {
                                LOG.debug(e.getLocalizedMessage(), e);
                            }
                        }
                    }
                }
                if (!withErrors) {
                    getReport().println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                        I_CmsReport.FORMAT_OK);
                } else {
                    getReport().println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0),
                        I_CmsReport.FORMAT_ERROR);
                }
                i += 1;

            } catch (CmsException e) {
                getReport().addError(e);
                LOG.error(e.getLocalizedMessage(), e);
                continue;
            }
        }
    }

    /**
     * Imports a resource from the current xml data.<p>
     *
     * @see #addResourceAttributesRules(Digester, String)
     * @see #addResourcePropertyRules(Digester, String)
     */
    public void importResource() {

        m_resourceIdWasNull = false;

        try {
            if (m_throwable != null) {
                getReport().println(m_throwable);
                getReport().addError(m_throwable);

                CmsMessageContainer message = Messages.get().container(
                    Messages.ERR_IMPORTEXPORT_ERROR_IMPORTING_RESOURCES_0);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(message.key(), m_throwable);
                }
                m_throwable = null;
                m_importACEs = false;
                m_resource = null;

                return;
            }

            // apply name translation and import path
            String translatedName = getRequestContext().addSiteRoot(
                CmsStringUtil.joinPaths(m_parameters.getDestinationPath(), m_destination));

            boolean skipResource = checkImmutable(translatedName);
            translatedName = getRequestContext().removeSiteRoot(translatedName);
            // if the resource is not immutable and not on the exclude list, import it
            if (!skipResource) {
                if (!m_hasStructureId && isFolderType(m_typeName) && getCms().existsResource(translatedName, CmsResourceFilter.ALL)) {
                    skipResource = true;
                }
            }
            if (!skipResource) {
                // print out the information to the report
                getReport().print(Messages.get().container(Messages.RPT_IMPORTING_0), I_CmsReport.FORMAT_NOTE);
                getReport().print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        translatedName));
                getReport().print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                boolean exists = getCms().existsResource(translatedName, CmsResourceFilter.ALL);

                byte[] content = null;
                // get the file content

                if (m_source != null) {
                    content = m_helper.getFileBytes(m_source);
                }
                int size = 0;
                if (content != null) {
                    size = content.length;
                }
                setDefaultsForEmptyResourceFields();
                // create a new CmsResource
                CmsResource resource = createResourceObjectFromFields(translatedName, size);
                if (!OpenCms.getResourceManager().hasResourceType(m_typeName)) {
                    m_properties.put(
                        CmsPropertyDefinition.PROPERTY_EXPORT_TYPE,
                        new CmsProperty(CmsPropertyDefinition.PROPERTY_EXPORT_TYPE, null, m_typeName, true));
                }
                if (m_resourceBuilder.getType().isFolder()
                    || m_resourceIdWasNull
                    || hasContentInVfsOrImport(resource)) {
                    // import this resource in the VFS
                    m_resource = getCms().importResource(
                        translatedName,
                        getReport(),
                        resource,
                        content,
                        new ArrayList<CmsProperty>(m_properties.values()));
                }

                if (m_resource != null) {
                    m_indexToStructureId.put(Integer.valueOf(m_fileCounter), m_resource.getStructureId());
                }

                // only set permissions if the resource did not exists or if the keep permissions flag is not set
                m_importACEs = (m_resource != null) && (!exists || !m_parameters.isKeepPermissions());

                if (m_resource != null) {
                    getReport().println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                        I_CmsReport.FORMAT_OK);

                    if (OpenCms.getResourceManager().getResourceType(
                        m_resource.getTypeId()) instanceof I_CmsLinkParseable) {
                        // store for later use
                        m_parseables.add(m_resource);
                    }
                    if (LOG.isInfoEnabled()) {
                        LOG.info(
                            Messages.get().getBundle().key(
                                Messages.LOG_IMPORTING_4,
                                new Object[] {
                                    String.valueOf(m_fileCounter),
                                    String.valueOf(m_totalFiles),
                                    translatedName,
                                    m_destination}));
                    }
                } else {
                    // resource import failed, since no CmsResource was created
                    getReport().print(Messages.get().container(Messages.RPT_SKIPPING_0), I_CmsReport.FORMAT_NOTE);
                    getReport().println(
                        org.opencms.report.Messages.get().container(
                            org.opencms.report.Messages.RPT_ARGUMENT_1,
                            translatedName));

                    if (LOG.isInfoEnabled()) {
                        LOG.info(
                            Messages.get().getBundle().key(
                                Messages.LOG_SKIPPING_3,
                                String.valueOf(m_fileCounter),
                                String.valueOf(m_totalFiles),
                                translatedName));
                    }
                }
            } else {
                m_resource = null;
                // skip the file import, just print out the information to the report
                getReport().print(Messages.get().container(Messages.RPT_SKIPPING_0), I_CmsReport.FORMAT_NOTE);
                getReport().println(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        translatedName));

                if (LOG.isInfoEnabled()) {
                    LOG.info(
                        Messages.get().getBundle().key(
                            Messages.LOG_SKIPPING_3,
                            String.valueOf(m_fileCounter),
                            String.valueOf(m_totalFiles),
                            translatedName));
                }
                // do not import ACEs
                m_importACEs = false;
            }
        } catch (Exception e) {
            m_resource = null;
            m_importACEs = false;

            getReport().println(e);
            getReport().addError(e);

            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_IMPORTING_RESOURCES_0);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }

            return;
        }
    }

    /**
     * Imports the resource and access control entries.<p>
     */
    public void importResourceAll() {

        try {
            importResource();
            importAccessControlEntries();
            increaseCounter();
        } finally {
            m_destination = null;
            m_source = null;
            m_throwable = null;
            m_properties = null;
            m_aces = null;
        }
    }

    /**
     * @see org.opencms.importexport.I_CmsImport#importResources(org.opencms.file.CmsObject, java.lang.String, org.opencms.report.I_CmsReport, java.io.File, java.util.zip.ZipFile, org.dom4j.Document)
     *
     * @deprecated use {@link #importData(CmsObject, I_CmsReport, CmsImportParameters)} instead
     */
    @Deprecated
    public void importResources(
        CmsObject cms,
        String importPath,
        I_CmsReport report,
        File importResource,
        ZipFile importZip,
        Document docXml) {

        CmsImportParameters params = new CmsImportParameters(importResource.getAbsolutePath(), importPath, true);

        importData(cms, report, params);
    }

    /**
     * Imports a new user from the current xml data.<p>
     */
    public void importUser() {

        // create a new user id
        String userName = m_orgUnit.getName() + m_userName;
        try {
            if (m_throwable != null) {
                m_user = null;
                getReport().println(m_throwable);

                CmsMessageContainer message = Messages.get().container(
                    Messages.ERR_IMPORTEXPORT_ERROR_IMPORTING_USER_1,
                    userName);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(message.key(), m_throwable);
                }
                m_throwable = null;
                return;
            }

            getReport().print(Messages.get().container(Messages.RPT_IMPORT_USER_0), I_CmsReport.FORMAT_NOTE);
            getReport().print(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, userName));
            getReport().print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            try {
                getCms().readUser(userName);
                // user exists already
                getReport().println(Messages.get().container(Messages.RPT_NOT_CREATED_0), I_CmsReport.FORMAT_OK);
                m_user = null;
                return;
            } catch (@SuppressWarnings("unused") CmsDbEntryNotFoundException e) {
                // user does not exist
            }

            CmsParameterConfiguration config = OpenCms.getPasswordHandler().getConfiguration();
            if ((config != null) && config.containsKey(I_CmsPasswordHandler.CONVERT_DIGEST_ENCODING)) {
                if (config.getBoolean(I_CmsPasswordHandler.CONVERT_DIGEST_ENCODING, false)) {
                    m_userPassword = convertDigestEncoding(m_userPassword);
                }
            }

            m_user = getCms().importUser(
                new CmsUUID().toString(),
                userName,
                m_userPassword,
                m_userFirstname,
                m_userLastname,
                m_userEmail,
                m_userFlags,
                m_userDateCreated,
                m_userInfos);

            getReport().println(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                I_CmsReport.FORMAT_OK);
        } catch (Throwable e) {
            m_user = null;
            getReport().println(e);

            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_IMPORTING_USER_1,
                userName);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }
        } finally {
            m_userName = null;
            m_userPassword = null;
            m_userFirstname = null;
            m_userLastname = null;
            m_userEmail = null;
            m_userFlags = 0;
            m_userDateCreated = 0;
            m_userInfos = null;
        }
    }

    /**
     * Sets the current user as member of the given group.<p>
     *
     * It can happen that the organizational unit has not been imported jet,
     * in this case, the data is kept for later.<p>
     *
     * @param groupName the name of the group to set
     *
     * @see #setMembership()
     */
    public void importUserGroup(String groupName) {

        if ((m_throwable != null) || (m_user == null)) {
            return;
        }
        groupName = OpenCms.getImportExportManager().translateGroup(groupName);
        try {
            String ouName = CmsOrganizationalUnit.getParentFqn(groupName);
            try {
                // check if the organizational unit exists
                OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), ouName);
                // set the user group
                getCms().addUserToGroup(m_user.getName(), groupName);
                return;
            } catch (@SuppressWarnings("unused") CmsDbEntryNotFoundException e) {
                // organizational unit does not exist
            }
            // remember the user and group for later
            Map<String, Map<String, String>> membership = m_membership.get(ouName);
            if (membership == null) {
                membership = new HashMap<String, Map<String, String>>();
                m_membership.put(ouName, membership);
            }
            Map<String, String> groups = membership.get(I_CmsPrincipal.PRINCIPAL_GROUP);
            if (groups == null) {
                groups = new HashMap<String, String>();
                membership.put(I_CmsPrincipal.PRINCIPAL_GROUP, groups);
            }
            groups.put(m_user.getName(), groupName);
        } catch (Throwable e) {
            getReport().println(
                Messages.get().container(Messages.RPT_USER_COULDNT_BE_ADDED_TO_GROUP_2, m_user.getName(), groupName),
                I_CmsReport.FORMAT_WARNING);
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Creates a new additional information entry for the current user.<p>
     *
     * @param infoName the name of the additional information entry
     * @param infoType the type of the additional information entry
     * @param infoValue the value of the additional information entry
     */
    public void importUserInfo(String infoName, String infoType, String infoValue) {

        if (m_userInfos == null) {
            m_userInfos = new HashMap<String, Object>();
        }
        try {
            m_userInfos.put(infoName, CmsDataTypeUtil.dataImport(infoValue, infoType));
        } catch (Throwable e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Sets the current user as member of the given role.<p>
     *
     * It can happen that the organizational unit has not been imported jet,
     * in this case, the data is kept for later.<p>
     *
     * @param roleName the name of the role to set
     *
     * @see #setMembership()
     */
    public void importUserRole(String roleName) {

        if ((m_throwable != null) || (m_user == null)) {
            return;
        }
        try {
            CmsRole role = CmsRole.valueOfRoleName(roleName);
            try {
                // check if the organizational unit exists
                OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), role.getOuFqn());
                // set the user role
                OpenCms.getRoleManager().addUserToRole(getCms(), role, m_user.getName());
                return;
            } catch (@SuppressWarnings("unused") CmsDbEntryNotFoundException e) {
                // organizational unit does not exist
            }
            // remember the user and role for later
            Map<String, Map<String, String>> membership = m_membership.get(role.getOuFqn());
            if (membership == null) {
                membership = new HashMap<String, Map<String, String>>();
                m_membership.put(role.getOuFqn(), membership);
            }
            Map<String, String> roles = membership.get(I_CmsPrincipal.PRINCIPAL_USER);
            if (roles == null) {
                roles = new HashMap<String, String>();
                membership.put(I_CmsPrincipal.PRINCIPAL_USER, roles);
            }
            roles.put(m_user.getName(), role.getFqn());
        } catch (Throwable e) {
            getReport().println(
                Messages.get().container(Messages.RPT_USER_COULDNT_BE_ADDED_TO_ROLE_2, m_user.getName(), roleName),
                I_CmsReport.FORMAT_WARNING);
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Increases the file counter.<p>
     */
    public void increaseCounter() {

        m_fileCounter++;
    }

    /**
     * Increases the total number of files.<p>
     */
    public void increaseTotalFiles() {

        m_totalFiles++;
    }

    /**
     * @see org.opencms.importexport.I_CmsImport#matches(org.opencms.importexport.CmsImportParameters)
     */
    @SuppressWarnings("resource") //stream is closed always in finally block - don't know why the compiler complains
    public boolean matches(CmsImportParameters parameters) throws CmsImportExportException {

        m_fileCounter = 1;
        m_totalFiles = 0;
        m_parseables = new ArrayList<CmsResource>();

        m_parameters = parameters;

        // instantiate Digester and enable XML validation
        Digester digester = new Digester();
        digester.setUseContextClassLoader(true);
        digester.setValidating(m_parameters.isXmlValidation());
        digester.setEntityResolver(new CmsXmlEntityResolver(null));
        digester.setRuleNamespaceURI(null);
        digester.setErrorHandler(new CmsXmlErrorHandler(CmsImportExportManager.EXPORT_MANIFEST));

        // add this object to the Digester
        digester.push(this);

        addXmlPreprocessingDigesterRules(digester);

        InputStream stream = null;
        m_helper = new CmsImportHelper(m_parameters);
        m_helper.cacheDtdSystemId(DTD_LOCATION, DTD_FILENAME, CmsConfigurationManager.DEFAULT_DTD_PREFIX);
        try {
            m_helper.openFile();
            // start the parsing process
            // this will set the version attribute
            stream = m_helper.getFileStream(CmsImportExportManager.EXPORT_MANIFEST);
            digester.parse(stream);
        } catch (Exception ioe) {
            CmsMessageContainer msg = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_READING_FILE_1,
                CmsImportExportManager.EXPORT_MANIFEST);
            if (LOG.isErrorEnabled()) {
                LOG.error(msg.key(), ioe);
            }
            throw new CmsImportExportException(msg, ioe);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (@SuppressWarnings("unused") Exception e) {
                // noop
            }
            m_helper.closeFile();
        }
        return (m_version == getVersion());
    }

    /**
     * Rewrites all parseable files, to assure link check.<p>
     *
     * This is a global process, that is executed only once at the
     * end of the import to be sure that all link targets are
     * available.<p>
     *
     * @see #addXmlDigesterRules(Digester)
     */
    public void rewriteParseables() {

        if (m_parseables.isEmpty()) {
            return;
        }

        I_CmsReport report = getReport();
        CmsObject cms = getCms();
        cms.getRequestContext().setAttribute(CmsLogEntry.ATTR_LOG_ENTRY, Boolean.FALSE);
        report.println(Messages.get().container(Messages.RPT_START_PARSE_LINKS_0), I_CmsReport.FORMAT_HEADLINE);
        parseLinks(cms, m_parseables, report);
        report.println(Messages.get().container(Messages.RPT_END_PARSE_LINKS_0), I_CmsReport.FORMAT_HEADLINE);
        m_parseables = null;
    }

    /**
     * Sets the aceFlags.<p>
     *
     * @param aceFlags the aceFlags to set
     *
     * @see #N_FLAGS
     * @see #addResourceAceRules(Digester, String)
     */
    public void setAceFlags(String aceFlags) {

        try {
            m_aceFlags = Integer.parseInt(aceFlags);
        } catch (Throwable e) {
            m_throwable = e;
        }
    }

    /**
     * Sets the acePermissionsAllowed.<p>
     *
     * @param acePermissionsAllowed the acePermissionsAllowed to set
     *
     * @see #N_ACCESSCONTROL_ALLOWEDPERMISSIONS
     * @see #addResourceAceRules(Digester, String)
     */
    public void setAcePermissionsAllowed(String acePermissionsAllowed) {

        try {
            m_acePermissionsAllowed = Integer.parseInt(acePermissionsAllowed);
        } catch (Throwable e) {
            m_throwable = e;
        }
    }

    /**
     * Sets the acePermissionsDenied.<p>
     *
     * @param acePermissionsDenied the acePermissionsDenied to set
     *
     * @see #N_ACCESSCONTROL_DENIEDPERMISSIONS
     * @see #addResourceAceRules(Digester, String)
     */
    public void setAcePermissionsDenied(String acePermissionsDenied) {

        try {
            m_acePermissionsDenied = Integer.parseInt(acePermissionsDenied);
        } catch (Throwable e) {
            m_throwable = e;
        }
    }

    /**
     * Sets the acePrincipalId.<p>
     *
     * @param acePrincipalId the acePrincipalId to set
     *
     * @see #N_ACCESSCONTROL_PRINCIPAL
     * @see #addResourceAceRules(Digester, String)
     */
    public void setAcePrincipalId(String acePrincipalId) {

        try {
            CmsUUID principalId = null;
            String principal = acePrincipalId.substring(acePrincipalId.indexOf('.') + 1, acePrincipalId.length());
            if (acePrincipalId.startsWith(I_CmsPrincipal.PRINCIPAL_GROUP)) {
                principal = OpenCms.getImportExportManager().translateGroup(principal);
                principalId = getCms().readGroup(principal).getId();
            } else if (acePrincipalId.startsWith(I_CmsPrincipal.PRINCIPAL_USER)) {
                principal = OpenCms.getImportExportManager().translateUser(principal);
                principalId = getCms().readUser(principal).getId();
            } else if (acePrincipalId.startsWith(CmsRole.PRINCIPAL_ROLE)) {
                principalId = CmsRole.valueOfRoleName(principal).getId();
            } else if (acePrincipalId.equalsIgnoreCase(CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME)) {
                principalId = CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID;
            } else if (acePrincipalId.equalsIgnoreCase(CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_NAME)) {
                principalId = CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID;
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(
                        Messages.get().getBundle().key(
                            Messages.LOG_IMPORTEXPORT_ERROR_IMPORTING_ACE_1,
                            acePrincipalId));
                }
                throw new CmsIllegalStateException(
                    Messages.get().container(Messages.LOG_IMPORTEXPORT_ERROR_IMPORTING_ACE_1, acePrincipalId));
            }
            m_acePrincipalId = principalId;
        } catch (Throwable e) {
            setThrowable(e);
        }
    }

    /**
     * Sets the dateCreated.<p>
     *
     * @param dateCreated the dateCreated to set
     *
     * @see #N_DATECREATED
     * @see #addResourceAttributesRules(Digester, String)
     */
    public void setDateCreated(String dateCreated) {

        try {
            if (dateCreated != null) {
                m_resourceBuilder.setDateCreated(convertTimestamp(dateCreated));
            } else {
                m_resourceBuilder.setDateCreated(System.currentTimeMillis());
            }
        } catch (Throwable e) {
            setThrowable(e);
        }
    }

    /**
     * Sets the dateExpired.<p>
     *
     * @param dateExpired the dateExpired to set
     *
     * @see #N_DATEEXPIRED
     * @see #addResourceAttributesRules(Digester, String)
     */
    public void setDateExpired(String dateExpired) {

        try {
            if (dateExpired != null) {
                m_resourceBuilder.setDateExpired(convertTimestamp(dateExpired));
            } else {
                m_resourceBuilder.setDateExpired(CmsResource.DATE_EXPIRED_DEFAULT);
            }
        } catch (Throwable e) {
            setThrowable(e);
        }
    }

    /**
     * Sets the dateLastModified.<p>
     *
     * @param dateLastModified the dateLastModified to set
     *
     * @see #N_DATELASTMODIFIED
     * @see #addResourceAttributesRules(Digester, String)
     */
    public void setDateLastModified(String dateLastModified) {

        m_hasDateLastModified = true;
        try {
            if (dateLastModified != null) {
                TimestampMode timeMode = TimestampMode.getEnum(CmsMacroResolver.stripMacro(dateLastModified));
                switch (timeMode) {
                    case FILETIME:
                        m_resourceBuilder.setDateLastModified(-1); //Can't get the information right now, must set it afterward.
                        break;
                    case IMPORTTIME:
                        m_resourceBuilder.setDateLastModified(System.currentTimeMillis());
                        break;
                    case VFSTIME:
                    default:
                        m_resourceBuilder.setDateLastModified(convertTimestamp(dateLastModified));
                        break;
                }
            } else {
                m_resourceBuilder.setDateLastModified(System.currentTimeMillis());
            }
        } catch (Throwable e) {
            setThrowable(e);
        }
    }

    /**
     * Sets the dateReleased.<p>
     *
     * @param dateReleased the dateReleased to set
     *
     * @see #N_DATERELEASED
     * @see #addResourceAttributesRules(Digester, String)
     */
    public void setDateReleased(String dateReleased) {

        try {
            if (dateReleased != null) {
                m_resourceBuilder.setDateReleased(convertTimestamp(dateReleased));
            } else {
                m_resourceBuilder.setDateReleased(CmsResource.DATE_RELEASED_DEFAULT);
            }
        } catch (Throwable e) {
            setThrowable(e);
        }
    }

    /**
     * Sets the destination.<p>
     *
     * @param destination the destination to set
     *
     * @see #N_DESTINATION
     * @see #addResourceAttributesRules(Digester, String)
     */
    public void setDestination(String destination) {

        m_destination = destination;
    }

    /**
     * Sets the flags.<p>
     *
     * @param flags the flags to set
     *
     * @see #N_FLAGS
     * @see #addResourceAttributesRules(Digester, String)
     */
    public void setFlags(String flags) {

        try {
            m_resourceBuilder.setFlags(Integer.parseInt(flags));
        } catch (Throwable e) {
            setThrowable(e);
        }
    }

    /**
     * Sets the group Description.<p>
     *
     * @param groupDescription the description to set
     */
    public void setGroupDescription(String groupDescription) {

        m_groupDescription = groupDescription;
    }

    /**
     * Sets the group Flags.<p>
     *
     * @param groupFlags the flags to set
     */
    public void setGroupFlags(String groupFlags) {

        try {
            m_groupFlags = Integer.parseInt(groupFlags);
        } catch (Throwable e) {
            setThrowable(e);
        }
    }

    /**
     * Sets the group Name.<p>
     *
     * @param groupName the name to set
     */
    public void setGroupName(String groupName) {

        m_groupName = OpenCms.getImportExportManager().translateGroup(groupName);
    }

    /**
     * Sets the group Parent.<p>
     *
     * @param groupParent the group Parent to set
     */
    public void setGroupParent(String groupParent) {

        m_groupParent = OpenCms.getImportExportManager().translateGroup(groupParent);
    }

    /**
     * Sets the membership information that could not been set immediately,
     * because of import order issues.<p>
     */
    public void setMembership() {

        if ((m_orgUnit == null) || (m_membership == null)) {
            return;
        }

        // get the membership data to set
        Map<String, Map<String, String>> membership = m_membership.get(m_orgUnit.getName());
        if (membership == null) {
            return;
        }

        // set group membership
        Map<String, String> groups = membership.get(I_CmsPrincipal.PRINCIPAL_GROUP);
        if (groups != null) {
            Iterator<Entry<String, String>> it = groups.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, String> entry = it.next();
                String userName = entry.getKey();
                String groupName = entry.getValue();

                // set the users group
                try {
                    getCms().addUserToGroup(userName, groupName);
                } catch (Throwable e) {
                    getReport().println(
                        Messages.get().container(Messages.RPT_USER_COULDNT_BE_ADDED_TO_GROUP_2, userName, groupName),
                        I_CmsReport.FORMAT_WARNING);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e.getLocalizedMessage(), e);
                    }
                }
            }
        }

        // set role membership
        Map<String, String> roles = membership.get(I_CmsPrincipal.PRINCIPAL_USER);
        if (roles != null) {
            Iterator<Entry<String, String>> it = roles.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, String> entry = it.next();
                String userName = entry.getKey();
                String roleName = entry.getValue();

                // set the users roles
                CmsRole role = CmsRole.valueOfRoleName(roleName);
                try {
                    // set the user role
                    OpenCms.getRoleManager().addUserToRole(getCms(), role, userName);
                    return;
                } catch (Throwable e) {
                    getReport().println(
                        Messages.get().container(Messages.RPT_USER_COULDNT_BE_ADDED_TO_ROLE_2, userName, roleName),
                        I_CmsReport.FORMAT_WARNING);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e.getLocalizedMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * Sets the organizational unit description.<p>
     *
     * @param orgUnitDescription the description to set
     */
    public void setOrgUnitDescription(String orgUnitDescription) {

        m_orgUnitDescription = orgUnitDescription;
    }

    /**
     * Sets the organizational unit flags.<p>
     *
     * @param orgUnitFlags the flags to set
     */
    public void setOrgUnitFlags(String orgUnitFlags) {

        try {
            m_orgUnitFlags = Integer.parseInt(orgUnitFlags);
        } catch (Throwable e) {
            setThrowable(e);
        }
    }

    /**
     * Sets the organizational unit name.<p>
     *
     * @param orgUnitName the name to set
     */
    public void setOrgUnitName(String orgUnitName) {

        m_orgUnitName = orgUnitName;
    }

    /**
     * Sets the project Description.<p>
     *
     * @param projectDescription the description to set
     */
    public void setProjectDescription(String projectDescription) {

        m_projectDescription = projectDescription;
    }

    /**
     * Sets the project Managers group name.<p>
     *
     * @param projectManagers the managers group to set
     */
    public void setProjectManagers(String projectManagers) {

        m_projectManagers = projectManagers;
    }

    /**
     * Sets the project Name.<p>
     *
     * @param projectName the name to set
     */
    public void setProjectName(String projectName) {

        m_projectName = projectName;
    }

    /**
     * Sets the project Users group name.<p>
     *
     * @param projectUsers the Users group to set
     */
    public void setProjectUsers(String projectUsers) {

        m_projectUsers = projectUsers;
    }

    /**
     * Sets the propertyName.<p>
     *
     * @param propertyName the propertyName to set
     *
     * @see #N_NAME
     * @see #addResourcePropertyRules(Digester, String)
     */
    public void setPropertyName(String propertyName) {

        m_propertyName = propertyName;
    }

    /**
     * Sets the propertyValue.<p>
     *
     * @param propertyValue the propertyValue to set
     *
     * @see #N_VALUE
     * @see #addResourcePropertyRules(Digester, String)
     */
    public void setPropertyValue(String propertyValue) {

        m_propertyValue = propertyValue;
    }

    /**
     * Sets the relationId.<p>
     *
     * @param relationId the relationId to set
     *
     * @see #N_ID
     * @see #addResourceRelationRules(Digester, String)
     */
    public void setRelationId(String relationId) {

        try {
            m_relationId = new CmsUUID(relationId);
        } catch (Throwable e) {
            setThrowable(e);
        }
    }

    /**
     * Sets the relationPath.<p>
     *
     * @param relationPath the relationPath to set
     *
     * @see #N_PATH
     * @see #addResourceRelationRules(Digester, String)
     */
    public void setRelationPath(String relationPath) {

        m_relationPath = relationPath;
    }

    /**
     * Sets the relationType.<p>
     *
     * @param relationType the relationType to set
     *
     * @see #N_TYPE
     * @see #addResourceRelationRules(Digester, String)
     */
    public void setRelationType(String relationType) {

        try {
            m_relationType = CmsRelationType.valueOf(relationType);
        } catch (Throwable e) {
            setThrowable(e);
        }
    }

    /**
     * Sets the resourceId.<p>
     *
     * @param resourceId the resourceId to set
     *
     * @see #N_UUIDRESOURCE
     * @see #addResourceAttributesRules(Digester, String)
     */
    public void setResourceId(String resourceId) {

        try {
            if (!m_resourceBuilder.isFolder()) {
                m_resourceBuilder.setResourceId(new CmsUUID(resourceId));
            } else {
                m_resourceBuilder.setResourceId(new CmsUUID());
            }
        } catch (Throwable e) {
            setThrowable(e);
        }
    }

    /**
     * Sets the source.<p>
     *
     * @param source the source to set
     *
     * @see #N_SOURCE
     * @see #addResourceAttributesRules(Digester, String)
     */
    public void setSource(String source) {

        m_source = source;
    }

    /**
     * Sets the structureId.<p>
     *
     * @param structureId the structureId to set
     *
     * @see #N_UUIDSTRUCTURE
     * @see #addResourceAttributesRules(Digester, String)
     */
    public void setStructureId(String structureId) {

        try {
            m_resourceBuilder.setStructureId(new CmsUUID(structureId));
            m_hasStructureId = true;

        } catch (Throwable e) {
            setThrowable(e);
        }

    }

    /**
     * Sets the throwable.<p>
     *
     * @param throwable the throwable to set
     */
    public void setThrowable(Throwable throwable) {

        m_throwable = throwable;
    }

    /**
     * Sets the type.<p>
     *
     * @param typeName the type to set
     *
     * @see #N_TYPE
     * @see #addResourceAttributesRules(Digester, String)
     */
    public void setType(String typeName) {

        m_typeName = typeName;
        try {
            try {
                m_resourceBuilder.setType(OpenCms.getResourceManager().getResourceType(typeName));
            } catch (@SuppressWarnings("unused") CmsLoaderException e) {
                m_unknownType = true;
                // TODO: what happens if the resource type is a specialized folder and is not configured??
                try {
                    m_resourceBuilder.setType(
                        OpenCms.getResourceManager().getResourceType(CmsResourceTypePlain.getStaticTypeName()));
                } catch (@SuppressWarnings("unused") CmsLoaderException e1) {
                    // this should really never happen
                    m_resourceBuilder.setType(
                        OpenCms.getResourceManager().getResourceType(CmsResourceTypePlain.getStaticTypeName()));
                }
            }
            if (m_resourceBuilder.getType().isFolder()) {
                // ensure folders end with a "/"
                if (!CmsResource.isFolder(m_destination)) {
                    m_destination += "/";
                }
            }
        } catch (Throwable e) {
            setThrowable(e);
        }
    }

    /**
     * Sets the user Created.<p>
     *
     * @param userCreated the user Created to set
     */
    public void setUserCreated(CmsUUID userCreated) {

        m_resourceBuilder.setUserCreated(userCreated);
    }

    /**
     * Sets the userCreated.<p>
     *
     * @param userCreated the userCreated to set
     *
     * @see #N_USERCREATED
     * @see #addResourceAttributesRules(Digester, String)
     */
    public void setUserCreated(String userCreated) {

        try {
            String userCreatedName = OpenCms.getImportExportManager().translateUser(userCreated);
            try {
                m_resourceBuilder.setUserCreated(getCms().readUser(userCreatedName).getId());
            } catch (@SuppressWarnings("unused") CmsDbEntryNotFoundException e) {
                m_resourceBuilder.setUserCreated(getRequestContext().getCurrentUser().getId());
            }
        } catch (Throwable e) {
            setThrowable(e);
        }
    }

    /**
     * Sets the user Date Created.<p>
     *
     * @param userDateCreated the date to set
     */
    public void setUserDateCreated(String userDateCreated) {

        try {
            m_userDateCreated = convertTimestamp(userDateCreated);
        } catch (Throwable e) {
            setThrowable(e);
        }
    }

    /**
     * Sets the user email address.<p>
     *
     * @param userEmail the email address to set
     */
    public void setUserEmail(String userEmail) {

        m_userEmail = userEmail;
    }

    /**
     * Sets the user First name.<p>
     *
     * @param userFirstname the first name to set
     */
    public void setUserFirstname(String userFirstname) {

        m_userFirstname = userFirstname;
    }

    /**
     * Sets the user Flags.<p>
     *
     * @param userFlags the flags to set
     */
    public void setUserFlags(String userFlags) {

        try {
            m_userFlags = Integer.parseInt(userFlags);
        } catch (Throwable e) {
            setThrowable(e);
        }
    }

    /**
     * Sets the user Last Modified.<p>
     *
     * @param userLastModified the user Last Modified to set
     */
    public void setUserLastModified(CmsUUID userLastModified) {

        m_resourceBuilder.setUserLastModified(userLastModified);
    }

    /**
     * Sets the userLastModified.<p>
     *
     * @param userLastModified the userLastModified to set
     *
     * @see #N_USERLASTMODIFIED
     * @see #addResourceAttributesRules(Digester, String)
     */
    public void setUserLastModified(String userLastModified) {

        if (null == userLastModified) { // The optional user last modified information is not provided
            m_resourceBuilder.setUserLastModified(getRequestContext().getCurrentUser().getId());
        } else { // use the user last modified information from the manifest
            try {
                String userLastModifiedName = OpenCms.getImportExportManager().translateUser(userLastModified);
                try {
                    m_resourceBuilder.setUserLastModified(getCms().readUser(userLastModifiedName).getId());
                } catch (@SuppressWarnings("unused") CmsDbEntryNotFoundException e) {
                    m_resourceBuilder.setUserLastModified(getRequestContext().getCurrentUser().getId());
                }
            } catch (Throwable e) {
                setThrowable(e);
            }
        }

    }

    /**
     * Sets the user Last name.<p>
     *
     * @param userLastname the last name to set
     */
    public void setUserLastname(String userLastname) {

        m_userLastname = userLastname;
    }

    /**
     * Sets the user Name.<p>
     *
     * @param userName the name to set
     */
    public void setUserName(String userName) {

        m_userName = OpenCms.getImportExportManager().translateUser(userName);
    }

    /**
     * Sets the user Password.<p>
     *
     * @param userPassword the password to set
     */
    public void setUserPassword(String userPassword) {

        m_userPassword = new String(Base64.decodeBase64(userPassword.trim().getBytes()));
    }

    /**
     * Sets the export version from the manifest file.<p>
     *
     * @param version the export version to set
     */
    public void setVersion(String version) {

        m_version = Integer.parseInt(version);
    }

    /**
     * Adds the XML digester rules for groups.<p>
     *
     * @param digester the digester to add the rules to
     * @param xpath the base xpath for the rules
     */
    protected void addAccountsGroupRules(Digester digester, String xpath) {

        String xp_group = xpath + N_GROUPS + "/" + N_GROUP;
        digester.addCallMethod(xp_group, "importGroup");
        xp_group += "/";
        digester.addCallMethod(xp_group + N_NAME, "setGroupName", 0);
        digester.addCallMethod(xp_group + N_DESCRIPTION, "setGroupDescription", 0);
        digester.addCallMethod(xp_group + N_FLAGS, "setGroupFlags", 0);
        digester.addCallMethod(xp_group + N_PARENTGROUP, "setGroupParent", 0);
    }

    /**
     * Adds the XML digester rules for organizational units.<p>
     *
     * @param digester the digester to add the rules to
     * @param xpath the base xpath for the rules
     */
    protected void addAccountsOrgunitRules(Digester digester, String xpath) {

        digester.addCallMethod(xpath + N_NAME, "setOrgUnitName", 0);
        digester.addCallMethod(xpath + N_DESCRIPTION, "setOrgUnitDescription", 0);
        digester.addCallMethod(xpath + N_FLAGS, "setOrgUnitFlags", 0);
        digester.addCallMethod(xpath + N_RESOURCES + "/" + N_RESOURCE, "addOrgUnitResource", 0);
        digester.addCallMethod(xpath + N_RESOURCES, "importOrgUnit");
    }

    /**
     * Adds the XML digester rules for users.<p>
     *
     * @param digester the digester to add the rules to
     * @param xpath the base xpath for the rules
     */
    protected void addAccountsUserRules(Digester digester, String xpath) {

        String xp_user = xpath + N_USERS + "/" + N_USER + "/";
        digester.addCallMethod(xp_user + N_NAME, "setUserName", 0);
        digester.addCallMethod(xp_user + N_PASSWORD, "setUserPassword", 0);
        digester.addCallMethod(xp_user + N_FIRSTNAME, "setUserFirstname", 0);
        digester.addCallMethod(xp_user + N_LASTNAME, "setUserLastname", 0);
        digester.addCallMethod(xp_user + N_EMAIL, "setUserEmail", 0);
        digester.addCallMethod(xp_user + N_FLAGS, "setUserFlags", 0);
        digester.addCallMethod(xp_user + N_DATECREATED, "setUserDateCreated", 0);
        digester.addCallMethod(xp_user + N_USERINFO, "importUser");

        String xp_info = xp_user + N_USERINFO + "/" + N_USERINFO_ENTRY;
        digester.addCallMethod(xp_info, "importUserInfo", 3);
        digester.addCallParam(xp_info, 0, A_NAME);
        digester.addCallParam(xp_info, 1, A_TYPE);
        digester.addCallParam(xp_info, 2);

        digester.addCallMethod(xp_user + N_USERROLES + "/" + N_USERROLE, "importUserRole", 0);
        digester.addCallMethod(xp_user + N_USERGROUPS + "/" + N_USERGROUP, "importUserGroup", 0);
    }

    /**
     * Adds the XML digester rules for projects.<p>
     *
     * @param digester the digester to add the rules to
     * @param xpath the base xpath for the rules
     */
    protected void addProjectRules(Digester digester, String xpath) {

        digester.addCallMethod(xpath + N_NAME, "setProjectName", 0);
        digester.addCallMethod(xpath + N_DESCRIPTION, "setProjectDescription", 0);
        digester.addCallMethod(xpath + N_MANAGERSGROUP, "setProjectManagers", 0);
        digester.addCallMethod(xpath + N_USERSGROUP, "setProjectUsers", 0);
        digester.addCallMethod(xpath + N_RESOURCES + "/" + N_RESOURCE, "addProjectResource", 0);
        digester.addCallMethod(xpath + N_RESOURCES, "importProject");
    }

    /**
     * Adds the XML digester rules for resource access control entries.<p>
     *
     * @param digester the digester to add the rules to
     * @param xpath the base xpath for the rules
     */
    protected void addResourceAceRules(Digester digester, String xpath) {

        String xp_ace = xpath + N_ACCESSCONTROL_ENTRIES + "/" + N_ACCESSCONTROL_ENTRY;
        digester.addCallMethod(xp_ace, "addAccessControlEntry");
        digester.addCallMethod(xp_ace + "/" + N_ACCESSCONTROL_PRINCIPAL, "setAcePrincipalId", 0);
        digester.addCallMethod(xp_ace + "/" + N_FLAGS, "setAceFlags", 0);
        String xp_perms = xp_ace + "/" + N_ACCESSCONTROL_PERMISSIONSET + "/";
        digester.addCallMethod(xp_perms + N_ACCESSCONTROL_ALLOWEDPERMISSIONS, "setAcePermissionsAllowed", 0);
        digester.addCallMethod(xp_perms + N_ACCESSCONTROL_DENIEDPERMISSIONS, "setAcePermissionsDenied", 0);
    }

    /**
     * Adds the XML digester rules for resource attributes.<p>
     *
     * @param digester the digester to add the rules to
     * @param xpath the base xpath for the rules
     */
    protected void addResourceAttributesRules(Digester digester, String xpath) {

        digester.addCallMethod(xpath + N_SOURCE, "setSource", 0);
        digester.addCallMethod(xpath + N_DESTINATION, "setDestination", 0);
        digester.addCallMethod(xpath + N_TYPE, "setType", 0);
        digester.addCallMethod(xpath + N_UUIDSTRUCTURE, "setStructureId", 0);
        digester.addCallMethod(xpath + N_UUIDRESOURCE, "setResourceId", 0);
        digester.addCallMethod(xpath + N_DATELASTMODIFIED, "setDateLastModified", 0);
        digester.addCallMethod(xpath + N_USERLASTMODIFIED, "setUserLastModified", 0);
        digester.addCallMethod(xpath + N_DATECREATED, "setDateCreated", 0);
        digester.addCallMethod(xpath + N_USERCREATED, "setUserCreated", 0);
        digester.addCallMethod(xpath + N_DATERELEASED, "setDateReleased", 0);
        digester.addCallMethod(xpath + N_DATEEXPIRED, "setDateExpired", 0);
        digester.addCallMethod(xpath + N_FLAGS, "setFlags", 0);
    }

    /**
     * Adds the XML digester rules for resource properties.<p>
     *
     * @param digester the digester to add the rules to
     * @param xpath the base xpath for the rules
     */
    protected void addResourcePropertyRules(Digester digester, String xpath) {

        String xp_props = xpath + N_PROPERTIES + "/" + N_PROPERTY;
        // first rule in case the type is implicit
        digester.addCallMethod(xp_props, "addProperty");
        // second rule in case the type is given
        digester.addCallMethod(xp_props, "addProperty", 1);
        digester.addCallParam(xp_props, 0, A_TYPE);

        digester.addCallMethod(xp_props + "/" + N_NAME, "setPropertyName", 0);
        digester.addCallMethod(xp_props + "/" + N_VALUE, "setPropertyValue", 0);

    }

    /**
     * Adds the XML digester rules for resource relations.<p>
     *
     * @param digester the digester to add the rules to
     * @param xpath the base xpath for the rules
     */
    protected void addResourceRelationRules(Digester digester, String xpath) {

        String xp_rels = xpath + N_RELATIONS + "/" + N_RELATION;
        digester.addCallMethod(xp_rels, "addRelation");
        digester.addCallMethod(xp_rels + "/" + N_ID, "setRelationId", 0);
        digester.addCallMethod(xp_rels + "/" + N_PATH, "setRelationPath", 0);
        digester.addCallMethod(xp_rels + "/" + N_TYPE, "setRelationType", 0);
    }

    /**
     * Checks if the resources is in the list of immutable resources.<p>
     *
     * @param resourceName the name of the resource
     *
     * @return <code>true</code> or <code>false</code>
     */
    protected boolean checkImmutable(String resourceName) {

        boolean resourceImmutable = false;
        if (getImmutableResources().contains(resourceName)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(Messages.LOG_IMPORTEXPORT_RESOURCENAME_IMMUTABLE_1, resourceName));
            }
            // this resource must not be modified by an import if it already exists
            String storedSiteRoot = getRequestContext().getSiteRoot();
            try {
                getRequestContext().setSiteRoot("/");
                getCms().readResource(resourceName);
                resourceImmutable = true;
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(Messages.LOG_IMPORTEXPORT_IMMUTABLE_FLAG_SET_1, resourceName));
                }
            } catch (CmsException e) {
                // resourceNotImmutable will be true
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_IMPORTEXPORT_ERROR_ON_TEST_IMMUTABLE_1,
                            resourceName),
                        e);
                }
            } finally {
                getRequestContext().setSiteRoot(storedSiteRoot);
            }
        }
        return resourceImmutable;
    }

    /**
     * Converts a given digest to base64 encoding.<p>
     *
     * @param value the digest value in the legacy encoding
     *
     * @return the digest in the new encoding
     */
    protected String convertDigestEncoding(String value) {

        byte[] data = new byte[value.length() / 2];

        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(Integer.parseInt(value.substring(i * 2, (i * 2) + 2), 16) - 128);
        }
        return new String(Base64.encodeBase64(data));
    }

    /**
     * Convert a given time stamp from a String format to a long value.<p>
     *
     * The time stamp is either the string representation of a long value (old export format)
     * or a user-readable string format.<p>
     *
     * @param timestamp time stamp to convert
     *
     * @return long value of the time stamp
     */
    protected long convertTimestamp(String timestamp) {

        long value = 0;
        // try to parse the time stamp string
        // if it successes, its an old style long value
        try {
            value = Long.parseLong(timestamp);
        } catch (@SuppressWarnings("unused") NumberFormatException e) {
            // the time stamp was in in a user-readable string format, create the long value form it
            try {
                value = CmsDateUtil.parseHeaderDate(timestamp);
            } catch (@SuppressWarnings("unused") ParseException pe) {
                value = System.currentTimeMillis();
            }
        }
        return value;
    }

    /**
     * Create a CmsResource object from the currently set field values.<p>
     *
     * @param translatedName the resource name
     * @param size the size
     * @return the new CmsResource object
     */
    protected CmsResource createResourceObjectFromFields(String translatedName, int size) {

        m_resourceBuilder.setRootPath(translatedName);
        m_resourceBuilder.setState(CmsResource.STATE_NEW);
        m_resourceBuilder.setProjectLastModified(getRequestContext().getCurrentProject().getUuid());
        m_resourceBuilder.setLength(size);
        m_resourceBuilder.setSiblingCount(1);
        m_resourceBuilder.setVersion(0);
        m_resourceBuilder.setDateContent(System.currentTimeMillis());
        return m_resourceBuilder.buildResource();
    }

    /**
     * This method goes through the manifest, records all files from the manifest for which the content also
     * exists in the zip file, and stores their resource ids in m_contentFiles.<p>
     *
     * @throws CmsImportExportException thrown when the manifest.xml can't be opened as stream.
     * @throws IOException thrown if the manifest.xml stream causes problems during parsing and/or closing.
     * @throws SAXException thrown if parsing the manifest.xml fails
     */
    protected void findContentFiles() throws CmsImportExportException, IOException, SAXException {

        Digester digester = new Digester();
        digester.setUseContextClassLoader(true);
        digester.setValidating(false);
        digester.setEntityResolver(new CmsXmlEntityResolver(null));
        digester.setRuleNamespaceURI(null);
        digester.setErrorHandler(new CmsXmlErrorHandler(CmsImportExportManager.EXPORT_MANIFEST));

        digester.addCallMethod("export/files/file", "addContentFile", 2);
        digester.addCallParam("export/files/file/source", 0);
        digester.addCallParam("export/files/file/uuidresource", 1);
        m_contentFiles.clear();
        digester.push(this);
        InputStream stream = null;
        try {
            stream = m_helper.getFileStream(CmsImportExportManager.EXPORT_MANIFEST);
            digester.parse(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    /**
     * Gets the import helper instance.<p>
     *
     * @return the import helper
     */
    protected CmsImportHelper getHelper() {

        return m_helper;
    }

    /**
     * Returns the list of properties to ignore during import.<p>
     *
     * @return the list of properties to ignore during import
     */
    protected List<String> getIgnoredProperties() {

        if (m_ignoredProperties == null) {
            // get list of ignored properties
            m_ignoredProperties = OpenCms.getImportExportManager().getIgnoredProperties();
            if (m_ignoredProperties == null) {
                m_ignoredProperties = Collections.emptyList();
            }
        }
        return m_ignoredProperties;
    }

    /**
     * Returns the list of immutable resources.<p>
     *
     * @return the list of immutable resources
     */
    protected List<String> getImmutableResources() {

        if (m_immutables == null) {
            // get list of immutable resources
            m_immutables = OpenCms.getImportExportManager().getImmutableResources();
            if (m_immutables == null) {
                m_immutables = Collections.emptyList();
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(
                        Messages.LOG_IMPORTEXPORT_IMMUTABLE_RESOURCES_SIZE_1,
                        Integer.toString(m_immutables.size())));
            }
        }
        return m_immutables;
    }

    /**
     * Checks if the given type name refers to a folder type.
     *
     * @param typeName the type name
     * @return the type name
     */
    protected boolean isFolderType(String typeName) {
        if (OpenCms.getResourceManager().hasResourceType(typeName)) {
            try {
                return OpenCms.getResourceManager().getResourceType(typeName).isFolder();
            } catch (CmsLoaderException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return false;
    }

    /**
     * Fills the unset fields for an imported resource with default values.<p>
     *
     * @throws CmsImportExportException if something goes wrong
     */
    protected void setDefaultsForEmptyResourceFields() throws CmsImportExportException {

        // get UUID for the structure
        if (m_resourceBuilder.getStructureId() == null) {
            // if null generate a new structure id
            m_resourceBuilder.setStructureId(new CmsUUID());
        }

        if ((m_resourceBuilder.getResourceId() == null) && m_unknownType) {
            try {
                m_resourceBuilder.setType(OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolder.getStaticTypeName()));
            } catch (CmsLoaderException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        if (m_resourceBuilder.getType().isFolder()) {
            // ensure folders end with a "/"
            if (!CmsResource.isFolder(m_destination)) {
                m_destination += "/";
            }
        }

        // get UUIDs for the resource
        if ((m_resourceBuilder.getResourceId() == null) || (m_resourceBuilder.getType().isFolder())) {
            // folders get always a new resource UUID
            m_resourceBuilder.setResourceId(new CmsUUID());
            m_resourceIdWasNull = true;
        }

        // read date last modified from the resource, default to currentTime for folders
        if (m_resourceBuilder.getDateLastModified() == DATE_LAST_MODIFICATION_FILETIME) {
            if (null != m_source) {
                m_resourceBuilder.setDateLastModified(m_helper.getFileModification(m_source));
            } else {
                m_resourceBuilder.setDateLastModified(System.currentTimeMillis());
            }
        }

        if (m_resourceBuilder.getDateLastModified() == DATE_LAST_MODIFICATION_UNSPECIFIED) {
            m_resourceBuilder.setDateLastModified(System.currentTimeMillis());
        }

        if (null == m_resourceBuilder.getUserLastModified()) {
            m_resourceBuilder.setUserLastModified(m_cms.getRequestContext().getCurrentUser().getId());
        }

        if (m_resourceBuilder.getDateCreated() == DATE_CREATED_UNSPECIFIED) {
            m_resourceBuilder.setDateCreated(System.currentTimeMillis());
        }

        if (m_resourceBuilder.getUserCreated().isNullUUID()) {
            m_resourceBuilder.setUserCreated(getRequestContext().getCurrentUser().getId());
        }

        if (m_properties == null) {
            m_properties = new HashMap<String, CmsProperty>();
        }
    }

    /**
     * Checks whether the content for the resource being imported exists either in the VFS or in the import file.<p>
     *
     * @param resource the resource which should be checked
     *
     * @return true if the content exists in the VFS or import file
     */
    private boolean hasContentInVfsOrImport(CmsResource resource) {

        if (m_contentFiles.contains(resource.getResourceId())) {
            return true;
        }
        try {
            List<CmsResource> resources = getCms().readSiblings(resource, CmsResourceFilter.ALL);
            if (!resources.isEmpty()) {
                return true;
            }
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
        return false;

    }
}

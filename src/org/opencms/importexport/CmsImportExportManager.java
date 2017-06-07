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

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.db.CmsUserExportSettings;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.xml.CmsXmlException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

/**
 * Provides information about how to handle imported resources.<p>
 *
 * @since 6.0.0
 *
 * @see OpenCms#getImportExportManager()
 */
public class CmsImportExportManager {

    /** Time modes to specify how time stamps should be handled. */
    public static enum TimestampMode {
        /** Use the time of import for the timestamp. */
        IMPORTTIME, /** Use the timestamp of the imported file. */
        FILETIME, /** The timestamp is explicitly given. */
        VFSTIME;

        /** Returns the default timestamp mode.
         * @return the default timestamp mode
         */
        public static TimestampMode getDefaultTimeStampMode() {

            return VFSTIME;
        }

        /** More robust version of {@link java.lang.Enum#valueOf(java.lang.Class, String)} that is case insensitive
         * and defaults for all "unreadable" arguments to the default timestamp mode.
         * @param value the TimeMode value as String
         * @return <code>value</code> as TimeMode object, or the default time mode, if <code>value</code> can't be converted to a TimeMode object.
         */
        public static TimestampMode getEnum(String value) {

            if (null == value) {
                return getDefaultTimeStampMode();
            } else {
                try {
                    return TimestampMode.valueOf(value.toUpperCase());
                } catch (@SuppressWarnings("unused") IllegalArgumentException e) {
                    return getDefaultTimeStampMode();
                }
            }
        }
    }

    /** Tag in the {@link #EXPORT_MANIFEST} for the "userinfo/entry@name" attribute, contains the additional user info entry name.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String A_NAME = A_CmsImport.A_NAME;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "userinfo/entry@type" attribute, contains the additional user info entry data type name.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String A_TYPE = A_CmsImport.A_TYPE;

    /** The name of the XML manifest file used for the description of exported OpenCms VFS properties and attributes. */
    public static final String EXPORT_MANIFEST = "manifest.xml";

    /** The current version of the OpenCms export (appears in the {@link #EXPORT_MANIFEST} header). */
    public static final String EXPORT_VERSION = "" + CmsImportVersion10.IMPORT_VERSION10;

    /**
     * The name of the XML manifest file used for the description of exported OpenCms VFS properties and attributes.<p>
     *
     * @deprecated use {@link #EXPORT_MANIFEST} instead
     */
    @Deprecated
    public static final String EXPORT_XMLFILENAME = EXPORT_MANIFEST;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "access" node.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_ACCESS = A_CmsImport.N_ACCESS;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "allowed" node, to identify allowed user permissions.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_ACCESSCONTROL_ALLOWEDPERMISSIONS = A_CmsImport.N_ACCESSCONTROL_ALLOWEDPERMISSIONS;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "denied" node, to identify denied user permissions.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_ACCESSCONTROL_DENIEDPERMISSIONS = A_CmsImport.N_ACCESSCONTROL_DENIEDPERMISSIONS;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "accesscontrol" node, to identify access control entries.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_ACCESSCONTROL_ENTRIES = A_CmsImport.N_ACCESSCONTROL_ENTRIES;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "accessentry" node, to identify a single access control entry.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_ACCESSCONTROL_ENTRY = A_CmsImport.N_ACCESSCONTROL_ENTRY;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "permissionset" node, to identify a permission set.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_ACCESSCONTROL_PERMISSIONSET = A_CmsImport.N_ACCESSCONTROL_PERMISSIONSET;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "uuidprincipal" node, to identify a principal UUID.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_ACCESSCONTROL_PRINCIPAL = A_CmsImport.N_ACCESSCONTROL_PRINCIPAL;

    /** Tag for the "creator" node (appears in the {@link #EXPORT_MANIFEST} header). */
    public static final String N_CREATOR = "creator";

    /** Tag for the "createdate" node (appears in the {@link #EXPORT_MANIFEST} header). */
    public static final String N_DATE = "createdate";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "datecreated" node, contains the date created VFS file attribute.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_DATECREATED = A_CmsImport.N_DATECREATED;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "dateexpired" node, contains the expiration date VFS file attribute.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_DATEEXPIRED = A_CmsImport.N_DATEEXPIRED;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "datelastmodified" node, contains the date last modified VFS file attribute.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_DATELASTMODIFIED = A_CmsImport.N_DATELASTMODIFIED;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "datereleased" node, contains the release date VFS file attribute.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_DATERELEASED = A_CmsImport.N_DATERELEASED;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "defaultgroup" node, for backward compatibility with OpenCms 5.x.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_DEFAULTGROUP = A_CmsImport.N_DEFAULTGROUP;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "description" node, contains a users description test.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_DESCRIPTION = A_CmsImport.N_DESCRIPTION;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "destination" node, contains target VFS file name.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_DESTINATION = A_CmsImport.N_DESTINATION;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "email" node, contains a users email.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_EMAIL = A_CmsImport.N_EMAIL;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "export" node. */
    public static final String N_EXPORT = "export";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "file" node, container node for all VFS resources.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_FILE = A_CmsImport.N_FILE;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "firstname" node, contains a users first name.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_FIRSTNAME = A_CmsImport.N_FIRSTNAME;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "flags" node, contains the flags of a VFS resource.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_FLAGS = A_CmsImport.N_FLAGS;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "groupdata" node, contains a users group data.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_GROUPDATA = A_CmsImport.N_GROUPDATA;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "groupname" node, contains a groups name.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_GROUPNAME = A_CmsImport.N_GROUPNAME;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "id" node, only required for backward compatibility with import version 2.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_ID = A_CmsImport.N_ID;

    /** Tag in the {@link #EXPORT_MANIFEST}, starts the manifest info header. */
    public static final String N_INFO = "info";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "lastmodified" node, only required for backward compatibility with import version 2.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_LASTMODIFIED = A_CmsImport.N_LASTMODIFIED;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "lastname" node, contains a users last name.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_LASTNAME = A_CmsImport.N_LASTNAME;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "name" node, contains a users login name.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_NAME = A_CmsImport.N_NAME;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "opencms_version" node, appears in the manifest info header. */
    public static final String N_OC_VERSION = "opencms_version";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "parentgroup" node, contains a groups parent group name.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_PARENTGROUP = A_CmsImport.N_PARENTGROUP;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "password" node, contains a users encrypted password.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_PASSWORD = A_CmsImport.N_PASSWORD;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "infoproject" node, appears in the manifest info header. */
    public static final String N_INFO_PROJECT = "infoproject";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "properties" node, starts the list of properties of a VFS resource.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_PROPERTIES = A_CmsImport.N_PROPERTIES;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "property" node, starts a property for a VFS resource.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_PROPERTY = A_CmsImport.N_PROPERTY;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "type" property attribute, contains a property type.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_PROPERTY_ATTRIB_TYPE = A_CmsImport.N_PROPERTY_ATTRIB_TYPE;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "shared" property type attribute value.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_PROPERTY_ATTRIB_TYPE_SHARED = A_CmsImport.N_PROPERTY_ATTRIB_TYPE_SHARED;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "relation" node, starts a relation for a VFS resource.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_RELATION = A_CmsImport.N_RELATION;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "id" relation attribute, contains the structure id of the target resource of the relation.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_RELATION_ATTRIBUTE_ID = A_CmsImport.N_RELATION_ATTRIBUTE_ID;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "path" relation attribute, contains the path to the target resource of the relation.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_RELATION_ATTRIBUTE_PATH = A_CmsImport.N_RELATION_ATTRIBUTE_PATH;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "type" relation attribute, contains the type of relation.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_RELATION_ATTRIBUTE_TYPE = A_CmsImport.N_RELATION_ATTRIBUTE_TYPE;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "relations" node, starts the list of relations of a VFS resources.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_RELATIONS = A_CmsImport.N_RELATIONS;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "source" node, contains the source path of a VFS resource in the import zip (or folder).
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_SOURCE = A_CmsImport.N_SOURCE;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "address" node, contains a users address.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_TAG_ADDRESS = A_CmsImport.N_TAG_ADDRESS;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "type" node, the resource type name of a VFS resource.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_TYPE = A_CmsImport.N_TYPE;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "user" node, starts the user data.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_USER = A_CmsImport.N_USER;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "usercreated" node, contains the name of the user who created the VFS resource.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_USERCREATED = A_CmsImport.N_USERCREATED;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "userdata" node, starts the list of users.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_USERDATA = A_CmsImport.N_USERDATA;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "usergroupdatas" node, starts the users group data.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_USERGROUPDATA = A_CmsImport.N_USERGROUPDATA;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "orgunitdatas" node, starts the organizational unit data.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_ORGUNITDATA = A_CmsImport.N_ORGUNITDATA;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "usergroups" node, starts the users group data.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_USERGROUPS = A_CmsImport.N_USERGROUPS;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "userinfo" node, contains the additional user info.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_USERINFO = A_CmsImport.N_USERINFO;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "userinfo/entry" node, contains the additional user info entry value.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_USERINFO_ENTRY = A_CmsImport.N_USERINFO_ENTRY;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "userlastmodified" node, contains the name of the user who last modified the VFS resource.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_USERLASTMODIFIED = A_CmsImport.N_USERLASTMODIFIED;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "uuidresource" node, contains a the resource UUID of a VFS resource.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_UUIDRESOURCE = A_CmsImport.N_UUIDRESOURCE;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "uuidstructure" node, only required for backward compatibility with import version 2.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_UUIDSTRUCTURE = A_CmsImport.N_UUIDSTRUCTURE;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "value" node, contains the value of a property.
     * @deprecated Use the appropriate tag from latest import class instead*/
    @Deprecated
    public static final String N_VALUE = A_CmsImport.N_VALUE;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "export_version" node, appears in the manifest info header. */
    public static final String N_VERSION = "export_version";

    /** Property to specify the export time written for a resource's date last modified. */
    public static final String PROP_EXPORT_TIMESTAMP = "export.timestamp";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsImportExportManager.class);

    /** Boolean flag whether imported pages should be converted into XML pages. */
    private boolean m_convertToXmlPage;

    /** The default values of the HTML->OpenCms Template converter. */
    private CmsExtendedHtmlImportDefault m_extendedHtmlImportDefault;

    /** List of property keys that should be removed from imported resources. */
    private List<String> m_ignoredProperties;

    /** Map from resource types to default timestamp modes. */
    private Map<String, TimestampMode> m_defaultTimestampModes;

    /** List of immutable resources that should remain unchanged when resources are imported. */
    private List<String> m_immutableResources;

    /** List of resourcetypes. Only used as helper for initializing the default timestamp modes. */
    private List<String> m_resourcetypes;

    /** The initialized import/export handlers. */
    private List<I_CmsImportExportHandler> m_importExportHandlers;

    /** Import principal group translations. */
    private Map<String, String> m_importGroupTranslations;

    /** Import principal user translations. */
    private Map<String, String> m_importUserTranslations;

    /** The configured import versions class names. */
    private List<I_CmsImport> m_importVersionClasses;

    /** Boolean flag whether colliding resources should be overwritten during the import. */
    private boolean m_overwriteCollidingResources;

    /** The user export settings. */
    private CmsUserExportSettings m_userExportSettings;

    /** The URL of a 4.x OpenCms application to import content correct into 5.x OpenCms application. */
    private String m_webAppUrl;

    /**
     * Creates a new instance for the import/export manager, will be called by the import/export configuration manager.
     */
    public CmsImportExportManager() {

        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().getBundle().key(Messages.INIT_IMPORTEXPORT_INITIALIZING_0));
        }

        m_importExportHandlers = new ArrayList<I_CmsImportExportHandler>();
        m_immutableResources = new ArrayList<String>();
        m_ignoredProperties = new ArrayList<String>();
        m_convertToXmlPage = true;
        m_importGroupTranslations = new HashMap<String, String>();
        m_importUserTranslations = new HashMap<String, String>();
        m_overwriteCollidingResources = true;
        m_importVersionClasses = new ArrayList<I_CmsImport>();
        m_defaultTimestampModes = new HashMap<String, TimestampMode>();
        m_resourcetypes = new ArrayList<String>();
    }

    /** Adds the provided default timestamp mode for the resourcetypes in list {@link #m_resourcetypes}.
     * The method is called by the digester.
     * @param timestampMode the timestamp mode to add as default.
     */
    public void addDefaultTimestampMode(String timestampMode) {

        if (null != timestampMode) {
            try {
                TimestampMode mode = TimestampMode.valueOf(timestampMode.toUpperCase());
                for (String resourcetype : m_resourcetypes) {
                    m_defaultTimestampModes.put(resourcetype, mode);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                            Messages.get().getBundle().key(
                                Messages.LOG_IMPORTEXPORT_EXPORT_SETTIMESTAMPMODE_2,
                                timestampMode,
                                resourcetype));
                    }
                }
            } catch (IllegalArgumentException e) {
                LOG.error(
                    Messages.get().getBundle().key(
                        Messages.ERR_IMPORTEXPORT_EXPORT_INVALID_TIMESTAMPMODE_2,
                        timestampMode,
                        m_resourcetypes.toString()),
                    e);
            }
        } else {
            LOG.error(
                Messages.get().getBundle().key(
                    Messages.ERR_IMPORTEXPORT_EXPORT_MISSING_TIMESTAMPMODE_1,
                    m_resourcetypes.toString()));
        }
        m_resourcetypes.clear();
    }

    /**
     * Adds a property name to the list of properties that should be removed from imported resources.<p>
     *
     * @param propertyName a property name
     */
    public void addIgnoredProperty(String propertyName) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_IMPORTEXPORT_IGNORING_PROPERTY_1, propertyName));
        }
        m_ignoredProperties.add(propertyName);
    }

    /**
     * Adds a resource to the list of immutable resources that should remain
     * unchanged when resources are imported.<p>
     *
     * @param immutableResource a resources uri in the OpenCms VFS
     */
    public void addImmutableResource(String immutableResource) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get().getBundle().key(
                    Messages.LOG_IMPORTEXPORT_ADDED_IMMUTABLE_RESOURCE_1,
                    immutableResource));
        }
        m_immutableResources.add(immutableResource);
    }

    /**
     * Adds an import/export handler to the list of configured handlers.<p>
     *
     * @param handler the import/export handler to add
     */
    public void addImportExportHandler(I_CmsImportExportHandler handler) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_IMPORTEXPORT_ADDED_IMPORTEXPORT_HANDLER_1, handler));
        }
        m_importExportHandlers.add(handler);
    }

    /**
     * Adds an import princial translation to the configuration.<p>
     *
     * @param type the princial type ("USER" or "GROUP")
     * @param from the "from" translation source
     * @param to the "to" translation target
     */
    public void addImportPrincipalTranslation(String type, String from, String to) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get().getBundle().key(
                    Messages.LOG_IMPORTEXPORT_ADDED_PRINCIPAL_TRANSLATION_3,
                    type,
                    from,
                    to));
        }
        if (I_CmsPrincipal.PRINCIPAL_GROUP.equalsIgnoreCase(type)) {
            m_importGroupTranslations.put(from, to);
            if (LOG.isInfoEnabled()) {
                LOG.info(
                    Messages.get().getBundle().key(Messages.INIT_IMPORTEXPORT_ADDED_GROUP_TRANSLATION_2, from, to));
            }
        } else if (I_CmsPrincipal.PRINCIPAL_USER.equalsIgnoreCase(type)) {
            m_importUserTranslations.put(from, to);
            if (LOG.isInfoEnabled()) {
                LOG.info(Messages.get().getBundle().key(Messages.INIT_IMPORTEXPORT_ADDED_USER_TRANSLATION_2, from, to));
            }
        }
    }

    /**
     * Adds a import version class name to the configuration.<p>
     *
     * @param importVersionClass the import version class name to add
     */
    public void addImportVersionClass(I_CmsImport importVersionClass) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get().getBundle().key(Messages.LOG_IMPORTEXPORT_ADDED_IMPORT_VERSION_1, importVersionClass));
        }
        m_importVersionClasses.add(importVersionClass);
    }

    /** Adds a resourcetype name to the list of resourcetypes that obtain a default timestamp mode in {@link #addDefaultTimestampMode(String)}.
     * The method is called only by the digester.
     *
     * @param resourcetypeName name of the resourcetype
     */
    public void addResourceTypeForDefaultTimestampMode(String resourcetypeName) {

        m_resourcetypes.add(resourcetypeName);

    }

    /**
     * Checks if imported pages should be converted into XML pages.<p>
     *
     * @return true, if imported pages should be converted into XML pages
     */
    public boolean convertToXmlPage() {

        return m_convertToXmlPage;
    }

    /**
     * Checks if the current user has permissions to export Cms data of a specified export handler,
     * and if so, triggers the handler to write the export.<p>
     *
     * @param cms the cms context
     * @param handler handler containing the export data
     * @param report the output report
     *
     * @throws CmsRoleViolationException if the current user is not a allowed to export the OpenCms database
     * @throws CmsImportExportException if operation was not successful
     * @throws CmsConfigurationException if something goes wrong
     *
     * @see I_CmsImportExportHandler
     */
    public void exportData(CmsObject cms, I_CmsImportExportHandler handler, I_CmsReport report)
    throws CmsConfigurationException, CmsImportExportException, CmsRoleViolationException {

        OpenCms.getRoleManager().checkRole(cms, CmsRole.DATABASE_MANAGER);
        handler.exportData(cms, report);
    }

    /** Returns the default timestamp mode for the resourcetype - or <code>null</code> if no default mode is set.
     * @param resourcetypeName name of the resourcetype to get the timestamp mode for
     * @return if set, the default timestamp mode for the resourcetype, otherwise <code>null</code>
     */
    public TimestampMode getDefaultTimestampMode(String resourcetypeName) {

        return m_defaultTimestampModes.get(resourcetypeName);
    }

    /** Returns the map from resourcetype names to default timestamp modes.
     * @return the map from resourcetype names to default timestamp modes.
     */
    public Map<TimestampMode, List<String>> getDefaultTimestampModes() {

        Map<TimestampMode, List<String>> result = new HashMap<TimestampMode, List<String>>();
        for (String resourcetype : m_defaultTimestampModes.keySet()) {
            TimestampMode mode = m_defaultTimestampModes.get(resourcetype);
            if (result.containsKey(mode)) {
                result.get(mode).add(resourcetype);
            } else {
                List<String> list = new ArrayList<String>();
                list.add(resourcetype);
                result.put(mode, list);
            }
        }
        return result;
    }

    /**
     * Returns the extendedHtmlImportDefault.<p>
     *
     * @return the extendedHtmlImportDefault
     */
    public CmsExtendedHtmlImportDefault getExtendedHtmlImportDefault() {

        return getExtendedHtmlImportDefault(false);
    }

    /**
     * Returns the extendedHtmlImportDefault.<p>
     *
     *@param withNull returns the extendenHtmlImport as null if its null,
     *                otherwise a new CmsExtendedHtmlImportDefault Object is generated
     *
     * @return the extendedHtmlImportDefault
     */
    public CmsExtendedHtmlImportDefault getExtendedHtmlImportDefault(boolean withNull) {

        return (withNull || (m_extendedHtmlImportDefault != null)
        ? m_extendedHtmlImportDefault
        : new CmsExtendedHtmlImportDefault());
    }

    /**
     * Returns the list of property keys that should be removed from imported resources.<p>
     *
     * @return the list of property keys that should be removed from imported resources, or Collections.EMPTY_LIST
     */
    public List<String> getIgnoredProperties() {

        return m_ignoredProperties;
    }

    /**
     * Returns the list of immutable resources that should remain unchanged when resources are
     * imported.<p>
     *
     * Certain system resources should not be changed during import. This is the case for the main
     * folders in the /system/ folder. Changes to these folders usually should not be imported to
     * another system.<p>
     *
     * @return the list of immutable resources, or {@link Collections#EMPTY_LIST}
     */
    public List<String> getImmutableResources() {

        return m_immutableResources;
    }

    /**
     * Returns an instance of an import/export handler implementation that is able to import
     * a specified resource.<p>
     *
     * @param parameters the import parameters
     *
     * @return an instance of an import/export handler implementation
     *
     * @throws CmsImportExportException if something goes wrong
     */
    public I_CmsImportExportHandler getImportExportHandler(CmsImportParameters parameters)
    throws CmsImportExportException {

        Document manifest;
        InputStream stream = null;
        CmsImportHelper helper = new CmsImportHelper(parameters);
        try {
            helper.openFile();
            stream = helper.getFileStream(CmsImportExportManager.EXPORT_MANIFEST);
            SAXReader reader = new SAXReader(false);
            reader.setValidation(false);
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            manifest = reader.read(stream);
        } catch (Throwable e) {
            throw new CmsImportExportException(
                Messages.get().container(Messages.ERR_IMPORTEXPORT_FILE_NOT_FOUND_1, EXPORT_MANIFEST),
                e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (@SuppressWarnings("unused") Exception e) {
                // noop
            }
            helper.closeFile();
        }
        for (int i = 0; i < m_importExportHandlers.size(); i++) {
            I_CmsImportExportHandler handler = m_importExportHandlers.get(i);
            if (handler.matches(manifest)) {
                return handler;
            }
        }

        CmsMessageContainer message = Messages.get().container(
            Messages.ERR_IMPORTEXPORT_ERROR_NO_HANDLER_FOUND_1,
            EXPORT_MANIFEST);
        if (LOG.isDebugEnabled()) {
            LOG.debug(message.key());
        }

        throw new CmsImportExportException(message);
    }

    /**
     * Returns the list of configured import/export handlers.<p>
     *
     * @return the list of configured import/export handlers
     */
    public List<I_CmsImportExportHandler> getImportExportHandlers() {

        return m_importExportHandlers;
    }

    /**
     * Returns the configured principal group translations.<p>
     *
     * @return the configured principal group translations
     */
    public Map<String, String> getImportGroupTranslations() {

        return m_importGroupTranslations;
    }

    /**
     * Returns the configured principal user translations.<p>
     *
     * @return the configured principal user translations
     */
    public Map<String, String> getImportUserTranslations() {

        return m_importUserTranslations;
    }

    /**
     * Returns the configured import version class names.<p>
     *
     * @return the configured import version class names
     */
    public List<I_CmsImport> getImportVersionClasses() {

        return m_importVersionClasses;
    }

    /**
     * Returns the URL of a 4.x OpenCms app. (e.g. http://localhost:8080/opencms/opencms/)
     * from which content was exported.<p>
     *
     * This setting is required to import content of 4.x OpenCms apps. correct into 5.x OpenCms apps.<p>
     *
     * @return the webAppUrl.
     */
    public String getOldWebAppUrl() {

        return m_webAppUrl;
    }

    /**
     * Returns the user settings for export.<p>
     *
     * @return the user settings for export
     */
    public CmsUserExportSettings getUserExportSettings() {

        return m_userExportSettings;
    }

    /**
     * Checks if the current user has permissions to import data into the Cms,
     * and if so, creates a new import handler instance that imports the data.<p>
     *
     * @param cms the current OpenCms context object
     * @param report a Cms report to print log messages
     * @param parameters the import parameters
     *
     * @throws CmsRoleViolationException if the current user is not allowed to import the OpenCms database
     * @throws CmsImportExportException if operation was not successful
     * @throws CmsXmlException if the manifest of the import could not be unmarshalled
     * @throws CmsException in case of errors accessing the VFS
     *
     * @see I_CmsImportExportHandler
     * @see #importData(CmsObject, String, String, I_CmsReport)
     */
    public void importData(CmsObject cms, I_CmsReport report, CmsImportParameters parameters)
    throws CmsImportExportException, CmsXmlException, CmsRoleViolationException, CmsException {

        // check the required role permissions
        OpenCms.getRoleManager().checkRole(cms, CmsRole.DATABASE_MANAGER);

        try {
            OpenCms.fireCmsEvent(
                new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, Collections.<String, Object> emptyMap()));
            I_CmsImportExportHandler handler = getImportExportHandler(parameters);
            synchronized (handler) {
                handler.setImportParameters(parameters);
                handler.importData(cms, report);
            }
        } finally {
            OpenCms.fireCmsEvent(
                new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, Collections.<String, Object> emptyMap()));
        }
    }

    /**
     * Checks if the current user has permissions to import data into the Cms,
     * and if so, creates a new import handler instance that imports the data.<p>
     *
     * @param cms the current OpenCms context object
     * @param importFile the name (absolute path) of the resource (zipfile or folder) to be imported
     * @param importPath the name (absolute path) of the destination folder in the Cms if required, or null
     * @param report a Cms report to print log messages
     *
     * @throws CmsRoleViolationException if the current user is not allowed to import the OpenCms database
     * @throws CmsImportExportException if operation was not successful
     * @throws CmsXmlException if the manifest of the import could not be unmarshalled
     * @throws CmsException in case of errors accessing the VFS
     *
     * @see I_CmsImportExportHandler
     * @see #importData(CmsObject, I_CmsReport, CmsImportParameters)
     *
     * @deprecated use {@link #importData(CmsObject, I_CmsReport, CmsImportParameters)} instead
     */
    @Deprecated
    public void importData(CmsObject cms, String importFile, String importPath, I_CmsReport report)
    throws CmsImportExportException, CmsXmlException, CmsRoleViolationException, CmsException {

        CmsImportParameters parameters = new CmsImportParameters(importFile, importPath, false);
        importData(cms, report, parameters);
    }

    /**
     * Checks if colliding resources should be overwritten during the import.<p>
     *
     * @return true, if colliding resources should be overwritten during the import
     * @see #setOverwriteCollidingResources(boolean)
     */
    public boolean overwriteCollidingResources() {

        return m_overwriteCollidingResources;
    }

    /**
     * Sets if imported pages should be converted into XML pages.<p>
     *
     * @param convertToXmlPage true, if imported pages should be converted into XML pages.
     */
    public void setConvertToXmlPage(boolean convertToXmlPage) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get().getBundle().key(
                    Messages.LOG_IMPORTEXPORT_SET_CONVERT_PARAMETER_1,
                    Boolean.toString(convertToXmlPage)));
        }
        m_convertToXmlPage = convertToXmlPage;
    }

    /**
     * Sets if imported pages should be converted into XML pages.<p>
     *
     * @param convertToXmlPage <code>"true"</code>, if imported pages should be converted into XML pages.
     */
    public void setConvertToXmlPage(String convertToXmlPage) {

        setConvertToXmlPage(Boolean.valueOf(convertToXmlPage).booleanValue());
    }

    /**
     * Sets the extendedHtmlImportDefault.<p>
     *
     * @param extendedHtmlImportDefault the extendedHtmlImportDefault to set
     */
    public void setExtendedHtmlImportDefault(CmsExtendedHtmlImportDefault extendedHtmlImportDefault) {

        m_extendedHtmlImportDefault = extendedHtmlImportDefault;
    }

    /**
     * Sets the URL of a 4.x OpenCms app. (e.g. http://localhost:8080/opencms/opencms/)
     * from which content was exported.<p>
     *
     * This setting is required to import content of 4.x OpenCms apps. correct into 5.x OpenCms apps.<p>
     *
     * @param webAppUrl a URL of the a OpenCms app. (e.g. http://localhost:8080/opencms/opencms/)
     */
    public void setOldWebAppUrl(String webAppUrl) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_IMPORTEXPORT_SET_OLD_WEBAPP_URL_1, webAppUrl));
        }
        m_webAppUrl = webAppUrl;
    }

    /**
     * Sets whether colliding resources should be overwritten during the import for a
     * specified import implementation.<p>
     *
     * v1 and v2 imports (without resource UUIDs in the manifest) *MUST* overwrite colliding
     * resources. Don't forget to set this flag back to it's original value in v1 and v2
     * import implementations!<p>
     *
     * This flag must be set to false to force imports > v2 to move colliding resources to
     * /system/lost-found/.<p>
     *
     * The import implementation has to take care to set this flag correct!<p>
     *
     * @param overwriteCollidingResources true if colliding resources should be overwritten during the import
     */
    public void setOverwriteCollidingResources(boolean overwriteCollidingResources) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get().getBundle().key(
                    Messages.LOG_IMPORTEXPORT_SET_OVERWRITE_PARAMETER_1,
                    Boolean.toString(overwriteCollidingResources)));
        }
        m_overwriteCollidingResources = overwriteCollidingResources;
    }

    /**
     * @see CmsImportExportManager#setOverwriteCollidingResources(boolean)
     *
     * @param overwriteCollidingResources <code>"true"</code> if colliding resources should be overwritten during the import
     */
    public void setOverwriteCollidingResources(String overwriteCollidingResources) {

        setOverwriteCollidingResources(Boolean.valueOf(overwriteCollidingResources).booleanValue());
    }

    /**
     * Sets the user export settings.<p>
     *
     * @param userExportSettings the user export settings to set
     */
    public void setUserExportSettings(CmsUserExportSettings userExportSettings) {

        m_userExportSettings = userExportSettings;
    }

    /**
     * Returns the translated name for the given group name.<p>
     *
     * If no matching name is found, the given group name is returned.<p>
     *
     * @param name the group name to translate
     * @return the translated name for the given group name
     */
    public String translateGroup(String name) {

        if (m_importGroupTranslations == null) {
            return name;
        }
        String match = m_importGroupTranslations.get(name);
        if (match != null) {
            return match;
        } else {
            return name;
        }
    }

    /**
     * Returns the translated name for the given user name.<p>
     *
     * If no matching name is found, the given user name is returned.<p>
     *
     * @param name the user name to translate
     * @return the translated name for the given user name
     */
    public String translateUser(String name) {

        if (m_importUserTranslations == null) {
            return name;
        }
        String match = m_importUserTranslations.get(name);
        if (match != null) {
            return match;
        } else {
            return name;
        }
    }
}
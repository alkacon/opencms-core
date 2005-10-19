/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/CmsImportExportManager.java,v $
 * Date   : $Date: 2005/10/19 09:41:10 $
 * Version: $Revision: 1.28.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

/**
 * Provides information about how to handle imported resources.<p>
 * 
 * @author Thomas Weckert  
 * 
 * @version $Revision: 1.28.2.2 $ 
 * 
 * @since 6.0.0 
 * 
 * @see OpenCms#getImportExportManager()
 */
public class CmsImportExportManager {

    /** The name of the XML manifest file used for the description of exported OpenCms VFS properties and atributes. */
    public static final String EXPORT_MANIFEST = "manifest.xml";

    /** The current version of the OpenCms export (appears in the {@link #EXPORT_MANIFEST} header). */
    public static final String EXPORT_VERSION = "4";

    /** 
     * The name of the XML manifest file used for the description of exported OpenCms VFS properties and atributes.<p>
     * 
     * @deprecated use {@link #EXPORT_MANIFEST} instead
     */
    public static final String EXPORT_XMLFILENAME = EXPORT_MANIFEST;

    /** Tag in the {@link #EXPORT_MANIFEST} for the "access" node. */
    public static final String N_ACCESS = "access";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "allowed" node, to identify allowed user permissions. */
    public static final String N_ACCESSCONTROL_ALLOWEDPERMISSIONS = "allowed";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "denied" node, to identify denied user permissions. */
    public static final String N_ACCESSCONTROL_DENIEDPERMISSIONS = "denied";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "accesscontrol" node, to identify access control entries. */
    public static final String N_ACCESSCONTROL_ENTRIES = "accesscontrol";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "accessentry" node, to identify a single access control entry. */
    public static final String N_ACCESSCONTROL_ENTRY = "accessentry";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "permissionset" node, to identify a permission set. */
    public static final String N_ACCESSCONTROL_PERMISSIONSET = "permissionset";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "uuidprincipal" node, to identify a principal UUID. */
    public static final String N_ACCESSCONTROL_PRINCIPAL = "uuidprincipal";

    /** Tag for the "creator" node (appears in the {@link #EXPORT_MANIFEST} header). */
    public static final String N_CREATOR = "creator";

    /** Tag for the "createdate" node (appears in the {@link #EXPORT_MANIFEST} header). */
    public static final String N_DATE = "createdate";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "datecreated" node, contains the date created VFS file attribute. */
    public static final String N_DATECREATED = "datecreated";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "dateexpired" node, contains the expiration date VFS file attribute. */
    public static final String N_DATEEXPIRED = "dateexpired";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "datelastmodified" node, contains the date last modified VFS file attribute. */
    public static final String N_DATELASTMODIFIED = "datelastmodified";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "datereleased" node, contains the release date VFS file attribute. */
    public static final String N_DATERELEASED = "datereleased";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "defaultgroup" node, for backward compatibility with OpenCms 5.x. */
    public static final String N_DEFAULTGROUP = "defaultgroup";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "description" node, contains a users description test. */
    public static final String N_DESCRIPTION = "description";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "destination" node, contains target VFS file name. */
    public static final String N_DESTINATION = "destination";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "email" node, contains a users email. */
    public static final String N_EMAIL = "email";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "export" node. */
    public static final String N_EXPORT = "export";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "file" node, container node for all VFS resources. */
    public static final String N_FILE = "file";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "firstname" node, contains a users first name. */
    public static final String N_FIRSTNAME = "firstname";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "flags" node, contains the flags of a VFS resource. */
    public static final String N_FLAGS = "flags";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "groupdata" node, contains a users group data. */
    public static final String N_GROUPDATA = "groupdata";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "groupname" node, contains a groups name. */
    public static final String N_GROUPNAME = "groupname";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "id" node, only required for backward compatibility with import version 2. */
    public static final String N_ID = "id";

    /** Tag in the {@link #EXPORT_MANIFEST}, starts the manifest info header. */
    public static final String N_INFO = "info";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "lastmodified" node, only required for backward compatibility with import version 2. */
    public static final String N_LASTMODIFIED = "lastmodified";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "lastname" node, contains a users last name. */
    public static final String N_LASTNAME = "lastname";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "name" node, contains a users login name. */
    public static final String N_NAME = "name";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "opencms_version" node, appears in the manifest info header. */
    public static final String N_OC_VERSION = "opencms_version";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "parentgroup" node, contains a groups parent group name. */
    public static final String N_PARENTGROUP = "parentgroup";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "password" node, contains a users encrypted password. */
    public static final String N_PASSWORD = "password";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "project" node, appears in the manifest info header. */
    public static final String N_PROJECT = "project";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "properties" node, starts the list of properties of a VFS resource. */
    public static final String N_PROPERTIES = "properties";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "property" node, starts a property for a VFS resource. */
    public static final String N_PROPERTY = "property";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "type" property attribute, contains a property type. */
    public static final String N_PROPERTY_ATTRIB_TYPE = "type";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "shared" property type attribute value. */
    public static final String N_PROPERTY_ATTRIB_TYPE_SHARED = "shared";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "source" node, contains the source path of a VFS resource in the import zip (or folder). */
    public static final String N_SOURCE = "source";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "address" node, contains a users address. */
    public static final String N_TAG_ADDRESS = "address";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "type" node, the resource type name of a VFS resource. */
    public static final String N_TYPE = "type";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "user" node, starts the user data. */
    public static final String N_USER = "user";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "usercreated" node, contains the name of the user who created the VFS resource. */
    public static final String N_USERCREATED = "usercreated";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "userdata" node, starts the list of users. */
    public static final String N_USERDATA = "userdata";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "usergroupdatas" node, starts the users group data. */
    public static final String N_USERGROUPDATA = "usergroupdata";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "usergroups" node, starts the users group data. */
    public static final String N_USERGROUPS = "usergroups";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "userinfo" node, contains the additional user info. */
    public static final String N_USERINFO = "userinfo";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "userlastmodified" node, contains the name of the user who last modified the VFS resource. */
    public static final String N_USERLASTMODIFIED = "userlastmodified";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "uuidresource" node, contains a the resource UUID of a VFS resource. */
    public static final String N_UUIDRESOURCE = "uuidresource";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "uuidstructure" node, only required for backward compatibility with import version 2. */
    public static final String N_UUIDSTRUCTURE = "uuidstructure";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "value" node, contains the value of a property. */
    public static final String N_VALUE = "value";

    /** Tag in the {@link #EXPORT_MANIFEST} for the "export_version" node, appears in the manifest info header. */
    public static final String N_VERSION = "export_version";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsImportExportManager.class);

    /** Boolean flag whether imported pages should be converted into XML pages. */
    private boolean m_convertToXmlPage;

    /** List of property keys that should be removed from imported resources. */
    private List m_ignoredProperties;

    /** List of immutable resources that should remain unchanged when resources are imported. */
    private List m_immutableResources;

    /** The initialized import/export handlers. */
    private List m_importExportHandlers;

    /** Import princial group translations. */
    private Map m_importGroupTranslations;

    /** Import princial user translations. */
    private Map m_importUserTranslations;

    /** The configured import versions class names. */
    private List m_importVersionClasses;

    /** Boolean flag whether colliding resources should be overwritten during the import. */
    private boolean m_overwriteCollidingResources;

    /** The URL of a 4.x OpenCms app. to import content correct into 5.x OpenCms apps. */
    private String m_webAppUrl;

    /**
     * Creates a new instance for the import/export manager, will be called by the import/export configuration manager.
     */
    public CmsImportExportManager() {

        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().key(Messages.INIT_IMPORTEXPORT_INITIALIZING_0));
        }

        m_importExportHandlers = new ArrayList();
        m_immutableResources = new ArrayList();
        m_ignoredProperties = new ArrayList();
        m_convertToXmlPage = true;
        m_importGroupTranslations = new HashMap();
        m_importUserTranslations = new HashMap();
        m_overwriteCollidingResources = true;
        m_importVersionClasses = new ArrayList();
    }

    /**
     * Returns the "manifest.xml" of an available import resource as a dom4j document.<p>
     * 
     * The manifest is either read as a ZIP entry, or from a subfolder of the specified
     * file resource.<p>
     * 
     * @param resource a File resource
     * @return the "manifest.xml" as a dom4j document
     */
    public static Document getManifest(File resource) {

        Document manifest = null;
        ZipFile zipFile = null;
        ZipEntry zipFileEntry = null;
        InputStream input = null;
        Reader reader = null;
        SAXReader saxReader = null;
        File manifestFile = null;

        try {
            if (resource.isFile()) {
                if (!resource.getName().toLowerCase().endsWith(".zip")) {
                    // skip non-ZIP files
                    return null;
                }
                // create a Reader for a ZIP file
                zipFile = new ZipFile(resource);
                zipFileEntry = zipFile.getEntry(EXPORT_MANIFEST);
                input = zipFile.getInputStream(zipFileEntry);
                // transform the manifest.xml file into a dom4j Document
                saxReader = new SAXReader();
                manifest = saxReader.read(input);
            } else if (resource.isDirectory()) {
                // create a Reader for a file in the file system
                manifestFile = new File(resource, EXPORT_MANIFEST);
                reader = new BufferedReader(new FileReader(manifestFile));
                // transform the manifest.xml file into a dom4j Document
                saxReader = new SAXReader();
                manifest = saxReader.read(reader);
            }
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().key(Messages.LOG_IMPORTEXPORT_ERROR_READING_MANIFEST_1, resource), e);
            }
            manifest = null;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                // noop
            }
        }

        return manifest;
    }

    /**
     * Adds a property name to the list of properties that should be removed from imported resources.<p>
     * 
     * @param propertyName a property name
     */
    public void addIgnoredProperty(String propertyName) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_IMPORTEXPORT_IGNORING_PROPERTY_1, propertyName));
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
            LOG.debug(Messages.get().key(Messages.LOG_IMPORTEXPORT_ADDED_IMMUTABLE_RESOURCE_1, immutableResource));
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
            LOG.debug(Messages.get().key(Messages.LOG_IMPORTEXPORT_ADDED_IMPORTEXPORT_HANDLER_1, handler));
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
            LOG.debug(Messages.get().key(Messages.LOG_IMPORTEXPORT_ADDED_PRINCIPAL_TRANSLATION_3, type, from, to));
        }
        if (type.equalsIgnoreCase(I_CmsPrincipal.PRINCIPAL_GROUP)) {
            m_importGroupTranslations.put(from, to);
            if (LOG.isInfoEnabled()) {
                LOG.info(Messages.get().key(Messages.INIT_IMPORTEXPORT_ADDED_GROUP_TRANSLATION_2, from, to));
            }
        } else if (type.equalsIgnoreCase(I_CmsPrincipal.PRINCIPAL_USER)) {
            m_importUserTranslations.put(from, to);
            if (LOG.isInfoEnabled()) {
                LOG.info(Messages.get().key(Messages.INIT_IMPORTEXPORT_ADDED_USER_TRANSLATION_2, from, to));
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
            LOG.debug(Messages.get().key(Messages.LOG_IMPORTEXPORT_ADDED_IMPORT_VERSION_1, importVersionClass));
        }
        m_importVersionClasses.add(importVersionClass);
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
     * @param cms the current OpenCms context object
     * @param handler handler containing the export data
     * @param report a Cms report to print log messages
     * @throws CmsRoleViolationException if the current user is not a allowed to export the OpenCms database
     * @throws CmsImportExportException if operation was not successful
     * @throws CmsConfigurationException if something goes wrong
     * @see I_CmsImportExportHandler
     */
    public void exportData(CmsObject cms, I_CmsImportExportHandler handler, I_CmsReport report)
    throws CmsConfigurationException, CmsImportExportException, CmsRoleViolationException {

        cms.checkRole(CmsRole.EXPORT_DATABASE);
        handler.exportData(cms, report);
    }

    /**
     * Returns the list of property keys that should be removed from imported resources.<p>
     * 
     * @return the list of property keys that should be removed from imported resources, or Collections.EMPTY_LIST
     */
    public List getIgnoredProperties() {

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
     * @return the list of immutable resources, or Collections.EMPTY_LIST
     */
    public List getImmutableResources() {

        return m_immutableResources;
    }

    /**
     * Returns an instance of an import/export handler implementation that is able to import
     * a specified resource.<p>
     * 
     * @param importFile the name (absolute path) of the resource (zipfile or folder) to be imported
     * @return an instance of an import/export handler implementation
     * @throws CmsImportExportException if somethong goes wrong
     */
    public I_CmsImportExportHandler getImportExportHandler(String importFile) throws CmsImportExportException {

        Document manifest = null;
        I_CmsImportExportHandler handler = null;

        File file = new File(importFile);
        if (!file.exists()) {
            // file does not exist
            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_IMPORT_FILE_DOES_NOT_EXIST_1,
                importFile);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key());
            }

            throw new CmsImportExportException(message);
        }

        manifest = getManifest(file);
        for (int i = 0; i < m_importExportHandlers.size(); i++) {
            handler = (I_CmsImportExportHandler)m_importExportHandlers.get(i);
            if (handler.matches(manifest)) {
                return handler;
            }

            handler = null;
        }

        if (handler == null) {

            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_NO_HANDLER_FOUND_1,
                importFile);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key());
            }

            throw new CmsImportExportException(message);
        }

        return null;
    }

    /**
     * Returns the list of configured import/export handlers.<p>
     * 
     * @return the list of configured import/export handlers
     */
    public List getImportExportHandlers() {

        return m_importExportHandlers;
    }

    /**
     * Returns the configured principal group translations.<p>
     * 
     * @return the configured principal group translations
     */
    public Map getImportGroupTranslations() {

        return m_importGroupTranslations;
    }

    /**
     * Returns the configured principal user translations.<p>
     * 
     * @return the configured principal user translations
     */
    public Map getImportUserTranslations() {

        return m_importUserTranslations;
    }

    /**
     * Returns the configured import version class names.<p>
     * 
     * @return the configured import version class names
     */
    public List getImportVersionClasses() {

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
     */
    public void importData(CmsObject cms, String importFile, String importPath, I_CmsReport report)
    throws CmsImportExportException, CmsXmlException, CmsRoleViolationException, CmsException {

        // check the required role permissions
        cms.checkRole(CmsRole.IMPORT_DATABASE);

        try {
            OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, Collections.EMPTY_MAP));
            I_CmsImportExportHandler handler = getImportExportHandler(importFile);
            handler.importData(cms, importFile, importPath, report);
        } finally {
            OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, Collections.EMPTY_MAP));
        }
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
            LOG.debug(Messages.get().key(
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
     * Sets the URL of a 4.x OpenCms app. (e.g. http://localhost:8080/opencms/opencms/)
     * from which content was exported.<p>
     * 
     * This setting is required to import content of 4.x OpenCms apps. correct into 5.x OpenCms apps.<p>
     * 
     * @param webAppUrl a URL of the a OpenCms app. (e.g. http://localhost:8080/opencms/opencms/)
     */
    public void setOldWebAppUrl(String webAppUrl) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_IMPORTEXPORT_SET_OLD_WEBAPP_URL_1, webAppUrl));
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
            LOG.debug(Messages.get().key(
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
        String match = (String)m_importGroupTranslations.get(name);
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
        String match = (String)m_importUserTranslations.get(name);
        if (match != null) {
            return match;
        } else {
            return name;
        }
    }

    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {

        try {
            if (m_immutableResources != null) {
                m_immutableResources.clear();
            }
            m_immutableResources = null;

            if (m_ignoredProperties != null) {
                m_ignoredProperties.clear();
            }
            m_ignoredProperties = null;
        } catch (Throwable t) {
            // noop
        } finally {
            super.finalize();
        }
    }
}
/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/CmsImportExportManager.java,v $
 * Date   : $Date: 2005/06/21 15:49:58 $
 * Version: $Revision: 1.21 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
import java.io.InputStreamReader;
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
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.21 $ $Date: 2005/06/21 15:49:58 $
 * @since 5.3
 * @see OpenCms#getImportExportManager()
 */
public class CmsImportExportManager {

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
     * A tag in the manifest-file.
     */
    public static final String N_ACCESS = "access";

    /**
     * Tag to identify allowed permissions.
     */
    public static final String N_ACCESSCONTROL_ALLOWEDPERMISSIONS = "allowed";

    /**
     * Tag to identify denied permissions.
     */
    public static final String N_ACCESSCONTROL_DENIEDPERMISSIONS = "denied";

    /**
     * Tag to identify access control entries .
     */
    public static final String N_ACCESSCONTROL_ENTRIES = "accesscontrol";

    /**
     * Tag to identify a single access control entry.
     */
    public static final String N_ACCESSCONTROL_ENTRY = "accessentry";

    /**
     * Tag to identify a permission set.
     */
    public static final String N_ACCESSCONTROL_PERMISSIONSET = "permissionset";

    /**
     * Tag to identify a principal set.
     */
    public static final String N_ACCESSCONTROL_PRINCIPAL = "uuidprincipal";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_TAG_ADDRESS = "address";

    /**
     * A tag in the export manifest-file, used as subtag of C_EXPORT_TAG_INFO.
     */
    public static final String N_CREATOR = "creator";

    /**
     * A tag in the export manifest-file, used as subtag of C_EXPORT_TAG_INFO.
     */
    public static final String N_DATE = "createdate";

    /**
     * The "datecreated" tag in the manifest-file.
     */
    public static final String N_DATECREATED = "datecreated";

    /**
     * The "expire" tag in the manifest-file.
     */
    public static final String N_DATEEXPIRED = "dateexpired";

    /**
     * The "datelastmodified" tag in the manifest-file.
     */
    public static final String N_DATELASTMODIFIED = "datelastmodified";

    /**
     * The "release" tag in the manifest-file.
     */
    public static final String N_DATERELEASED = "datereleased";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_DEFAULTGROUP = "defaultgroup";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_DESCRIPTION = "description";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_DESTINATION = "destination";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_EMAIL = "email";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_EXPORT = "export";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_FILE = "file";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_FIRSTNAME = "firstname";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_FLAGS = "flags";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_GROUPNAME = "groupname";

    /**
     * Tag to identify a generic id.
     */
    public static final String N_ID = "id";

    /**
     * A tag in the export manifest-file.
     */
    public static final String N_INFO = "info";

    /**
     * The "lastmodified" tag in the manifest-file.
     */
    public static final String N_LASTMODIFIED = "lastmodified";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_LASTNAME = "lastname";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_NAME = "name";

    /**
     * A tag in the export manifest-file, used as subtag of C_EXPORT_TAG_INFO.
     */
    public static final String N_OC_VERSION = "opencms_version";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_PARENTGROUP = "parentgroup";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_PASSWORD = "password";

    /**
     * A tag in the manifest-file, used as subtag of C_EXPORT_TAG_INFO.
     */
    public static final String N_PROJECT = "project";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_PROPERTIES = "properties";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_PROPERTY = "property";

    /**
     * Key for the type attrib. of a property element.<p>
     */
    public static final String N_PROPERTY_ATTRIB_TYPE = "type";

    /**
     * Value for the "shared" type attrib. of a property element.<p>
     */
    public static final String N_PROPERTY_ATTRIB_TYPE_SHARED = "shared";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_SOURCE = "source";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_TYPE = "type";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_USER = "user";

    /**
     * The "usercreated" tag in the manifest-file.
     */
    public static final String N_USERCREATED = "usercreated";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_USERDATA = "userdata";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_USERGROUPDATA = "usergroupdata";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_USERGROUPS = "usergroups";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_USERINFO = "userinfo";

    /**
     * The "userlastmodified" tag in the manifest-file.
     */
    public static final String N_USERLASTMODIFIED = "userlastmodified";

    /**
     * The "uuidresource" tag in the manifest-file.
     */
    public static final String N_UUIDRESOURCE = "uuidresource";

    /**
     * The "uuid" tag in the manifest-file.
     */
    public static final String N_UUIDSTRUCTURE = "uuidstructure";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_VALUE = "value";

    /**
     * A tag in the export manifest-file, used as subtag of C_EXPORT_TAG_INFO.
     */
    public static final String N_VERSION = "export_version";

    /**
     * The version of the opencms export (appears in the export manifest-file).
     */
    public static final String EXPORT_VERSION = "4";

    /**
     * A tag in the manifest-file.
     */
    public static final String N_GROUPDATA = "groupdata";

    /** 
     * The filename of the xml manifest.
     */
    // used 2 times in org.opencms.importexport
    public static final String EXPORT_XMLFILENAME = "manifest.xml";

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

                // create a Reader either from a ZIP file's manifest.xml entry...
                zipFile = new ZipFile(resource);
                zipFileEntry = zipFile.getEntry("manifest.xml");
                input = zipFile.getInputStream(zipFileEntry);
                reader = new BufferedReader(new InputStreamReader(input));
            } else if (resource.isDirectory()) {
                // ...or from a subresource inside a folder
                manifestFile = new File(resource, "manifest.xml");
                reader = new BufferedReader(new FileReader(manifestFile));
            }

            // transform the manifest.xml file into a dom4j Document
            saxReader = new SAXReader();
            manifest = saxReader.read(reader);
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
        if (type.equalsIgnoreCase(I_CmsPrincipal.C_PRINCIPAL_GROUP)) {
            m_importGroupTranslations.put(from, to);
            if (LOG.isInfoEnabled()) {
                LOG.info(Messages.get().key(Messages.INIT_IMPORTEXPORT_ADDED_GROUP_TRANSLATION_2, from, to));
            }
        } else if (type.equalsIgnoreCase(I_CmsPrincipal.C_PRINCIPAL_USER)) {
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
     * @param convertToXmlPage "true", if imported pages should be converted into XML pages.
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
     * @param overwriteCollidingResources "true" if colliding resources should be overwritten during the import
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
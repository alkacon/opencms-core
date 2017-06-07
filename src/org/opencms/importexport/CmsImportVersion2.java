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

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.lock.CmsLockException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPasswordHandler;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.page.CmsXmlPage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * Implementation of the OpenCms Import Interface ({@link org.opencms.importexport.I_CmsImport}) for
 * the import version 2.<p>
 *
 * This import format was used in OpenCms 5.0.0 - 5.1.2.<p>
 *
 * @since 6.0.0
 *
 * @see org.opencms.importexport.A_CmsImport
 *
 * @deprecated this import class is no longer in use and should only be used to import old export files
 */
@Deprecated
public class CmsImportVersion2 extends A_CmsImport {

    /** Parameter for content body folder. */
    public static final String VFS_PATH_BODIES = "/system/bodies/";

    /** The runtime property name for old webapp names. */
    private static final String COMPATIBILITY_WEBAPPNAMES = "compatibility.support.webAppNames";

    /** The version number of this import implementation. */
    private static final int IMPORT_VERSION = 2;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsImportVersion2.class);

    /** Web application names for conversion support. */
    protected List<String> m_webAppNames;

    /** Old webapp URL for import conversion. */
    protected String m_webappUrl;

    /** folder storage for page file and body conversion. */
    private List<String> m_folderStorage;

    /** page file storage for page file and body co.version. */
    private List<String> m_pageStorage;

    /**
     * Translates directory Strings from OpenCms 4.x structure to new 5.0 structure.<p>
     *
     * @param content the filecontent
     * @param rules the translation rules
     * @return String the manipulated file content
     */
    public static String setDirectories(String content, String[] rules) {

        // get translation rules
        for (int i = 0; i < rules.length; i++) {
            String actRule = rules[i];
            // cut String "/default/vfs/" from rule
            actRule = CmsStringUtil.substitute(actRule, "/default/vfs", "");
            // divide rule into search and replace parts and delete regular expressions
            StringTokenizer ruleT = new StringTokenizer(actRule, "#");
            ruleT.nextToken();
            String search = ruleT.nextToken();
            int pos = search.lastIndexOf("(.*)");
            if (pos >= 0) {
                search = search.substring(0, pos);
            }
            String replace = ruleT.nextToken();
            if (pos >= 0) {
                replace = replace.substring(0, replace.lastIndexOf("$1"));
            }
            // scan content for paths if the replace String is not present
            if ((content.indexOf(replace) == -1) && (content.indexOf(search) != -1)) {
                // ensure subdirectories of the same name are not replaced
                search = "([}>\"'\\[]\\s*)" + search;
                replace = "$1" + replace;
                content = CmsStringUtil.substitutePerl(content, search, replace, "g");
            }
        }
        return content;
    }

    /**
     * @see org.opencms.importexport.I_CmsImport#getVersion()
     * @return the version number of this import implementation
     */
    public int getVersion() {

        return CmsImportVersion2.IMPORT_VERSION;
    }

    /**
     * @see org.opencms.importexport.I_CmsImport#importData(CmsObject, I_CmsReport, CmsImportParameters)
     */
    public void importData(CmsObject cms, I_CmsReport report, CmsImportParameters params)
    throws CmsImportExportException, CmsXmlException {

        // initialize the import
        initialize();
        m_cms = cms;
        m_importPath = params.getDestinationPath();
        m_report = report;

        m_folderStorage = new ArrayList<String>();
        m_pageStorage = new ArrayList<String>();
        m_linkStorage = new HashMap<String, String>();
        m_linkPropertyStorage = new HashMap<String, List<CmsProperty>>();

        if (OpenCms.getRunLevel() >= OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + ".m_folderStorage", m_folderStorage);
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + ".m_pageStorage", m_pageStorage);
            OpenCms.getMemoryMonitor().register(this.getClass().getName() + ".m_linkStorage", m_linkStorage);
            OpenCms.getMemoryMonitor().register(
                this.getClass().getName() + ".m_linkPropertyStorage",
                m_linkPropertyStorage);
        }

        CmsImportHelper helper = new CmsImportHelper(params);
        try {
            helper.openFile();
            m_importResource = helper.getFolder();
            m_importZip = helper.getZipFile();
            m_docXml = CmsXmlUtils.unmarshalHelper(helper.getFileBytes(CmsImportExportManager.EXPORT_MANIFEST), null);
            // first import the user information
            if (OpenCms.getRoleManager().hasRole(m_cms, CmsRole.ACCOUNT_MANAGER)) {
                importGroups();
                importUsers();
            }
            // now import the VFS resources
            importAllResources();
            convertPointerToSiblings();
        } catch (IOException e) {
            CmsMessageContainer msg = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_READING_FILE_1,
                CmsImportExportManager.EXPORT_MANIFEST);
            if (LOG.isErrorEnabled()) {
                LOG.error(msg.key(), e);
            }
            throw new CmsImportExportException(msg, e);
        } finally {
            helper.closeFile();
            cleanUp();
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
        Document docXml) throws CmsImportExportException {

        CmsImportParameters params = new CmsImportParameters(
            importResource != null ? importResource.getAbsolutePath() : importZip.getName(),
            importPath,
            true);

        try {
            importData(cms, report, params);
        } catch (CmsXmlException e) {
            throw new CmsImportExportException(e.getMessageContainer(), e);
        }
    }

    /**
     * Cleans up member variables after the import is finished.<p>
     *
     * This is required since there is only one instance for
     * each import version that is kept in memory and reused.<p>
     */
    @Override
    protected void cleanUp() {

        m_pageStorage = null;
        m_folderStorage = null;
        m_webAppNames = null;
        m_webappUrl = null;
        super.cleanUp();
    }

    /**
     * Performs all required pre-import steps.<p>
     *
     * The content is *NOT* changed in the implementation of this class.<p>
     *
     * @param source the source path of the resource
     * @param destination the destination path of the resource
     * @param content the content of the resource
     * @param resType the type of the resource
     * @return the (prepared) content of the resource
     */
    protected byte[] convertContent(String source, String destination, byte[] content, String resType) {

        // if the import is older than version 3, some additional conversions must be made
        if (getVersion() < 3) {
            if ("page".equals(resType)) {
                // if the imported resource is a page, store its path inside the VFS for later
                // integration with its body
                m_pageStorage.add(destination);
            } else if ("folder".equals(resType)) {
                // check if the imported resource is a folder. Folders created in the /system/bodies/ folder
                if (destination.startsWith(VFS_PATH_BODIES.substring(1))) {
                    // must be removed since we do not use body files anymore.
                    m_folderStorage.add(destination);
                }
            }
        }

        return content;
    }

    /**
     * Gets the encoding from the &lt;?XML ...&gt; tag if present.<p>
     *
     * @param content the file content
     * @return String the found encoding
     */
    protected String getEncoding(String content) {

        String encoding = content;
        int index = encoding.toLowerCase().indexOf("encoding=\"");
        // encoding attribute found, get the value
        if (index != -1) {
            encoding = encoding.substring(index + 10);
            index = encoding.indexOf("\"");
            if (index != -1) {
                encoding = encoding.substring(0, index);
                return encoding.toUpperCase();
            }
        }
        // no encoding attribute found
        return "";
    }

    /**
     * @see org.opencms.importexport.A_CmsImport#importUser(String, String, String, String, String, String, long, Map, List)
     */
    @Override
    protected void importUser(
        String name,
        String flags,
        String password,
        String firstname,
        String lastname,
        String email,
        long dateCreated,
        Map<String, Object> userInfo,
        List<String> userGroups) throws CmsImportExportException {

        boolean convert = false;

        CmsParameterConfiguration config = OpenCms.getPasswordHandler().getConfiguration();
        if ((config != null) && config.containsKey(I_CmsPasswordHandler.CONVERT_DIGEST_ENCODING)) {
            convert = config.getBoolean(I_CmsPasswordHandler.CONVERT_DIGEST_ENCODING, false);
        }

        if (convert) {
            password = convertDigestEncoding(password);
        }

        super.importUser(name, flags, password, firstname, lastname, email, dateCreated, userInfo, userGroups);
    }

    /**
     * Initializes all member variables before the import is started.<p>
     *
     * This is required since there is only one instance for
     * each import version that is kept in memory and reused.<p>
     */
    @Override
    protected void initialize() {

        m_convertToXmlPage = true;
        m_webAppNames = new ArrayList<String>();
        super.initialize();
    }

    /**
     * Sets the right encoding and returns the result.<p>
     *
     * @param content the filecontent
     * @param encoding the encoding to use
     * @return modified content
     */
    protected String setEncoding(String content, String encoding) {

        if (content.toLowerCase().indexOf("<?xml") == -1) {
            return content;
        } else {
            // XML information present, replace encoding
            // set the encoding only if it does not exist
            String xmlTag = content.substring(0, content.indexOf(">") + 1);
            if (xmlTag.toLowerCase().indexOf("encoding") == -1) {
                content = content.substring(content.indexOf(">") + 1);
                content = "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>" + content;
            }
        }
        return content;
    }

    /**
     * Returns the compatibility web app names.<p>
     *
     * @return the compatibility web app names
     */
    private List<String> getCompatibilityWebAppNames() {

        List<String> webAppNamesOri = new ArrayList<String>();

        String configuredWebAppNames = (String)OpenCms.getRuntimeProperty(COMPATIBILITY_WEBAPPNAMES);
        if ((configuredWebAppNames != null) && (configuredWebAppNames.length() != 0)) {
            // split the comma separated list of web app names
            StringTokenizer tokenizer = new StringTokenizer(configuredWebAppNames, ",;");
            while (tokenizer.hasMoreTokens()) {
                webAppNamesOri.add(tokenizer.nextToken());
            }
        }

        List<String> webAppNames = new ArrayList<String>();
        for (int i = 0; i < webAppNamesOri.size(); i++) {
            // remove possible white space
            String name = webAppNamesOri.get(i).trim();
            if (CmsStringUtil.isNotEmpty(name)) {
                webAppNames.add(name);
                if (LOG.isInfoEnabled()) {
                    LOG.info(
                        Messages.get().getBundle().key(
                            Messages.INIT_IMPORTEXPORT_OLD_CONTEXT_PATH_2,
                            Integer.toString((i + 1)),
                            name));
                }
            }
        }

        String key = (webAppNames.size() > 0)
        ? Messages.INIT_IMPORTEXPORT_OLD_CONTEXT_SUPPORT_ENABLED_0
        : Messages.INIT_IMPORTEXPORT_OLD_CONTEXT_SUPPORT_DISABLED_0;
        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().getBundle().key(key));
        }

        // add current context to webapp names list
        if (!webAppNames.contains(OpenCms.getSystemInfo().getOpenCmsContext())) {
            webAppNames.add(OpenCms.getSystemInfo().getOpenCmsContext());
        }

        return webAppNames;
    }

    /**
     * Imports the resources and writes them to the cms.<p>
     *
     * @throws CmsImportExportException if something goes wrong
     */
    @SuppressWarnings("unchecked")
    private void importAllResources() throws CmsImportExportException {

        List<Element> fileNodes = null, acentryNodes = null;
        Element currentElement = null, currentEntry = null;
        String source = null, destination = null, resourceTypeName = null, timestamp = null, uuid = null,
        uuidresource = null;
        long lastmodified = 0;
        int resourceTypeId = CmsResourceTypePlain.getStaticTypeId();
        List<CmsProperty> properties = null;
        boolean old_overwriteCollidingResources = false;
        try {
            m_webAppNames = getCompatibilityWebAppNames();
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(
                        Messages.LOG_IMPORTEXPORT_ERROR_GETTING_WEBAPP_COMPATIBILITY_NAMES_0),
                    e);
            }
            m_report.println(e);
        }
        if (m_webAppNames == null) {
            m_webAppNames = Collections.EMPTY_LIST;
        }

        // get the old webapp url from the OpenCms properties
        m_webappUrl = OpenCms.getImportExportManager().getOldWebAppUrl();
        if (m_webappUrl == null) {
            // use a default value
            m_webappUrl = "http://localhost:8080/opencms/opencms";
        }
        // cut last "/" from webappUrl if present
        if (m_webappUrl.endsWith("/")) {
            m_webappUrl = m_webappUrl.substring(0, m_webappUrl.lastIndexOf("/"));
        }

        // get list of unwanted properties
        List<String> deleteProperties = OpenCms.getImportExportManager().getIgnoredProperties();

        // get list of immutable resources
        List<String> immutableResources = OpenCms.getImportExportManager().getImmutableResources();
        if (immutableResources == null) {
            immutableResources = Collections.EMPTY_LIST;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get().getBundle().key(
                    Messages.LOG_IMPORTEXPORT_IMMUTABLE_RESOURCES_SIZE_1,
                    Integer.toString(immutableResources.size())));
        }

        // save the value of the boolean flag whether colliding resources should be overwritten
        old_overwriteCollidingResources = OpenCms.getImportExportManager().overwriteCollidingResources();

        // force v1 and v2 imports to overwrite colliding resources, because they dont have resource
        // UUIDs in their manifest anyway
        OpenCms.getImportExportManager().setOverwriteCollidingResources(true);

        try {
            // get all file-nodes
            fileNodes = m_docXml.selectNodes("//" + A_CmsImport.N_FILE);
            int importSize = fileNodes.size();

            // walk through all files in manifest
            for (int i = 0; i < importSize; i++) {

                m_report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_SUCCESSION_2,
                        String.valueOf(i + 1),
                        String.valueOf(importSize)),
                    I_CmsReport.FORMAT_NOTE);
                currentElement = fileNodes.get(i);

                // get all information for a file-import
                source = getChildElementTextValue(currentElement, A_CmsImport.N_SOURCE);
                destination = getChildElementTextValue(currentElement, A_CmsImport.N_DESTINATION);

                resourceTypeName = getChildElementTextValue(currentElement, A_CmsImport.N_TYPE);
                if (RESOURCE_TYPE_NEWPAGE_NAME.equals(resourceTypeName)) {
                    resourceTypeId = RESOURCE_TYPE_NEWPAGE_ID;
                } else if (RESOURCE_TYPE_LEGACY_PAGE_NAME.equals(resourceTypeName)) {
                    // resource with a "legacy" resource type are imported using the "plain" resource
                    // type because you cannot import a resource without having the resource type object
                    resourceTypeId = CmsResourceTypePlain.getStaticTypeId();
                } else if (RESOURCE_TYPE_LINK_NAME.equals(resourceTypeName)) {
                    // set resource type of legacy "link" which is converted later
                    resourceTypeId = RESOURCE_TYPE_LINK_ID;
                } else {
                    I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resourceTypeName);
                    resourceTypeId = type.getTypeId();
                }

                uuid = getChildElementTextValue(currentElement, A_CmsImport.N_UUIDSTRUCTURE);
                uuidresource = getChildElementTextValue(currentElement, A_CmsImport.N_UUIDRESOURCE);

                timestamp = getChildElementTextValue(currentElement, A_CmsImport.N_LASTMODIFIED);
                if (timestamp != null) {
                    lastmodified = Long.parseLong(timestamp);
                } else {
                    lastmodified = System.currentTimeMillis();
                }

                // if the type is "script" set it to plain
                if ("script".equals(resourceTypeName)) {
                    resourceTypeName = CmsResourceTypePlain.getStaticTypeName();
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_IMPORTEXPORT_ORIGINAL_RESOURCE_NAME_1,
                            destination));
                }

                String translatedName = m_cms.getRequestContext().addSiteRoot(m_importPath + destination);
                if (CmsResourceTypeFolder.RESOURCE_TYPE_NAME.equals(resourceTypeName)) {
                    // ensure folders end with a "/"
                    if (!CmsResource.isFolder(translatedName)) {
                        translatedName += "/";
                    }
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_IMPORTEXPORT_TRANSLATED_RESOURCE_NAME_1,
                            translatedName));
                }

                boolean resourceNotImmutable = checkImmutable(translatedName, immutableResources);
                translatedName = m_cms.getRequestContext().removeSiteRoot(translatedName);
                if (resourceNotImmutable) {

                    // print out the information to the report
                    m_report.print(Messages.get().container(Messages.RPT_IMPORTING_0), I_CmsReport.FORMAT_NOTE);
                    m_report.print(
                        org.opencms.report.Messages.get().container(
                            org.opencms.report.Messages.RPT_ARGUMENT_1,
                            translatedName));
                    m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                    // get all properties
                    properties = readPropertiesFromManifest(currentElement, deleteProperties);

                    // import the specified file
                    CmsResource res = importResource(
                        source,
                        destination,
                        uuid,
                        uuidresource,
                        resourceTypeId,
                        resourceTypeName,
                        lastmodified,
                        properties);

                    if (res != null) {

                        List<CmsAccessControlEntry> aceList = new ArrayList<CmsAccessControlEntry>();
                        // write all imported access control entries for this file
                        acentryNodes = currentElement.selectNodes("*/" + A_CmsImport.N_ACCESSCONTROL_ENTRY);
                        // collect all access control entries
                        for (int j = 0; j < acentryNodes.size(); j++) {
                            currentEntry = acentryNodes.get(j);
                            // get the data of the access control entry
                            String id = getChildElementTextValue(currentEntry, A_CmsImport.N_ID);
                            String acflags = getChildElementTextValue(currentEntry, A_CmsImport.N_FLAGS);
                            String allowed = getChildElementTextValue(
                                currentEntry,
                                A_CmsImport.N_ACCESSCONTROL_ALLOWEDPERMISSIONS);
                            String denied = getChildElementTextValue(
                                currentEntry,
                                A_CmsImport.N_ACCESSCONTROL_DENIEDPERMISSIONS);

                            // add the entry to the list
                            aceList.add(getImportAccessControlEntry(res, id, allowed, denied, acflags));
                        }
                        importAccessControlEntries(res, aceList);
                        if (LOG.isInfoEnabled()) {
                            LOG.info(
                                Messages.get().getBundle().key(
                                    Messages.LOG_IMPORTING_4,
                                    new Object[] {
                                        String.valueOf(i + 1),
                                        String.valueOf(importSize),
                                        translatedName,
                                        destination}));
                        }

                    } else {
                        // resource import failed, since no CmsResource was created
                        m_report.print(Messages.get().container(Messages.RPT_SKIPPING_0), I_CmsReport.FORMAT_OK);
                        m_report.println(
                            org.opencms.report.Messages.get().container(
                                org.opencms.report.Messages.RPT_ARGUMENT_1,
                                translatedName));
                        if (LOG.isInfoEnabled()) {
                            LOG.info(
                                Messages.get().getBundle().key(
                                    Messages.LOG_SKIPPING_3,
                                    String.valueOf(i + 1),
                                    String.valueOf(importSize),
                                    translatedName));
                        }
                    }
                } else {
                    // skip the file import, just print out the information to the report
                    m_report.print(Messages.get().container(Messages.RPT_SKIPPING_0), I_CmsReport.FORMAT_NOTE);
                    m_report.println(
                        org.opencms.report.Messages.get().container(
                            org.opencms.report.Messages.RPT_ARGUMENT_1,
                            translatedName));

                    if (LOG.isInfoEnabled()) {
                        LOG.info(
                            Messages.get().getBundle().key(
                                Messages.LOG_SKIPPING_3,
                                String.valueOf(i + 1),
                                String.valueOf(importSize),
                                translatedName));
                    }
                }
            }

            // now merge the body and page control files. this only has to be done if the import
            // version is below version 3
            if ((getVersion() < 3) && m_convertToXmlPage) {
                mergePageFiles();
                removeFolders();
            }
        } catch (Exception e) {
            m_report.println(e);
            m_report.addError(e);

            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_IMPORTING_RESOURCES_0);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }

            throw new CmsImportExportException(message, e);
        } finally {
            // set the flag to overwrite colliding resources back to its original value
            OpenCms.getImportExportManager().setOverwriteCollidingResources(old_overwriteCollidingResources);
        }
    }

    /**
     * Imports a resource (file or folder) into the cms.<p>
     *
     * @param source the path to the source-file
     * @param destination the path to the destination-file in the cms
     * @param uuid  the structure uuid of the resource
     * @param uuidresource  the resource uuid of the resource
     * @param resourceTypeId the ID of the file's resource type
     * @param resourceTypeName the name of the file's resource type
     * @param lastmodified the timestamp of the file
     * @param properties a list with properties for this resource
     *
     * @return imported resource
     */
    private CmsResource importResource(
        String source,
        String destination,
        String uuid,
        String uuidresource,
        int resourceTypeId,
        String resourceTypeName,
        long lastmodified,
        List<CmsProperty> properties) {

        byte[] content = null;
        CmsResource res = null;
        String targetName = null;

        try {
            // get the file content
            if (source != null) {
                content = getFileBytes(source);
            }

            content = convertContent(source, destination, content, resourceTypeName);

            // get all required information to create a CmsResource
            int size = 0;
            if (content != null) {
                size = content.length;
            }
            // get the required UUIDs
            CmsUUID curUser = m_cms.getRequestContext().getCurrentUser().getId();
            CmsUUID newUuidstructure = new CmsUUID();
            CmsUUID newUuidresource = new CmsUUID();
            if (uuid != null) {
                newUuidstructure = new CmsUUID(uuid);
            }
            if (uuidresource != null) {
                newUuidresource = new CmsUUID(uuidresource);
            }

            // extract the name of the resource form the destination
            targetName = destination;
            if (targetName.endsWith("/")) {
                targetName = targetName.substring(0, targetName.length() - 1);
            }

            boolean isFolder = false;
            try {
                isFolder = CmsFolder.isFolderType(resourceTypeId);
            } catch (Throwable t) {
                // the specified resource type ID might be of an unknown resource type.
                // as another option, check the content length and resource type name
                // to determine if the resource is a folder or not.
                isFolder = ((size == 0) && CmsResourceTypeFolder.RESOURCE_TYPE_NAME.equalsIgnoreCase(resourceTypeName));
            }

            // create a new CmsResource
            CmsResource resource = new CmsResource(
                newUuidstructure,
                newUuidresource,
                targetName,
                resourceTypeId,
                isFolder,
                0,
                m_cms.getRequestContext().getCurrentProject().getUuid(),
                CmsResource.STATE_NEW,
                lastmodified,
                curUser,
                lastmodified,
                curUser,
                CmsResource.DATE_RELEASED_DEFAULT,
                CmsResource.DATE_EXPIRED_DEFAULT,
                1,
                size,
                System.currentTimeMillis(),
                0);

            if (RESOURCE_TYPE_LINK_ID == resourceTypeId) {
                // store links for later conversion
                m_report.print(Messages.get().container(Messages.RPT_STORING_LINK_0), I_CmsReport.FORMAT_NOTE);
                m_linkStorage.put(m_importPath + destination, new String(content));
                m_linkPropertyStorage.put(m_importPath + destination, properties);
                res = resource;
            } else {
                //  import this resource in the VFS
                String resName = m_importPath + destination;
                res = m_cms.importResource(resName, resource, content, properties);
                try {
                    m_cms.unlockResource(resName);
                } catch (CmsLockException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(
                            Messages.get().getBundle().key(
                                Messages.LOG_IMPORTEXPORT_UNABLE_TO_UNLOCK_RESOURCE_1,
                                resName),
                            e);
                    }
                }
            }

            m_report.println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

        } catch (CmsException exc) {
            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_IMPORTING_RESOURCE_1,
                targetName);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), exc);
            }
            // an error while importing the file
            m_report.println(exc);
            try {
                // Sleep some time after an error so that the report output has a chance to keep up
                Thread.sleep(1000);
            } catch (Exception e) {
                // ignore
            }
        }
        return res;
    }

    /**
     * Merges a single page.<p>
     *
     * @param resourcename the resource name of the page
     * @throws CmsImportExportException if something goes wrong
     * @throws CmsXmlException if the page file could not be unmarshalled
     */
    private void mergePageFile(String resourcename) throws CmsXmlException, CmsImportExportException {

        try {

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_IMPORTEXPORT_START_MERGING_1, resourcename));
            }

            // in OpenCms versions <5 node names have not been case sensitive. thus, nodes are read both in upper
            // and lower case letters, or have to be tested for equality ignoring upper/lower case...

            // get the header file
            CmsFile pagefile = m_cms.readFile(resourcename, CmsResourceFilter.ALL);
            Document contentXml = CmsXmlUtils.unmarshalHelper(pagefile.getContents(), null);

            // get the <masterTemplate> node to check the content. this node contains the name of the template file.
            String masterTemplateNodeName = "//masterTemplate";
            Node masterTemplateNode = contentXml.selectSingleNode(masterTemplateNodeName);
            if (masterTemplateNode == null) {
                masterTemplateNode = contentXml.selectSingleNode(masterTemplateNodeName.toLowerCase());
            }
            if (masterTemplateNode == null) {
                masterTemplateNode = contentXml.selectSingleNode(masterTemplateNodeName.toUpperCase());
            }

            // there is only one <masterTemplate> allowed
            String mastertemplate = null;
            if (masterTemplateNode != null) {
                // get the name of the mastertemplate
                mastertemplate = masterTemplateNode.getText().trim();
            }

            // get the <ELEMENTDEF> nodes to check the content.
            // this node contains the information for the body element.
            String elementDefNodeName = "//ELEMENTDEF";
            Node bodyNode = contentXml.selectSingleNode(elementDefNodeName);
            if (bodyNode == null) {
                bodyNode = contentXml.selectSingleNode(elementDefNodeName.toLowerCase());
            }

            // there is only one <ELEMENTDEF> allowed
            if (bodyNode != null) {

                String bodyclass = null;
                String bodyname = null;
                Map<String, String> bodyparams = null;

                @SuppressWarnings("unchecked")
                List<Node> nodes = ((Element)bodyNode).elements();
                for (int i = 0, n = nodes.size(); i < n; i++) {

                    Node node = nodes.get(i);

                    if ("CLASS".equalsIgnoreCase(node.getName())) {
                        bodyclass = node.getText().trim();
                    } else if ("TEMPLATE".equalsIgnoreCase(node.getName())) {
                        bodyname = node.getText().trim();
                        if (!bodyname.startsWith("/")) {
                            bodyname = CmsResource.getFolderPath(resourcename) + bodyname;
                        }
                    } else if ("PARAMETER".equalsIgnoreCase(node.getName())) {
                        Element paramElement = (Element)node;
                        if (bodyparams == null) {
                            bodyparams = new HashMap<String, String>();
                        }
                        bodyparams.put((paramElement.attribute("name")).getText(), paramElement.getTextTrim());
                    }
                }

                if ((mastertemplate == null) || (bodyname == null)) {

                    CmsMessageContainer message = Messages.get().container(
                        Messages.ERR_IMPORTEXPORT_ERROR_CANNOT_MERGE_PAGE_FILE_3,
                        resourcename,
                        mastertemplate,
                        bodyname);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(message.key());
                    }

                    throw new CmsImportExportException(message);
                }

                // lock the resource, so that it can be manipulated
                m_cms.lockResource(resourcename);

                // get all properties
                List<CmsProperty> properties = m_cms.readPropertyObjects(resourcename, false);

                // now get the content of the bodyfile and insert it into the control file
                CmsFile bodyfile = m_cms.readFile(bodyname, CmsResourceFilter.IGNORE_EXPIRATION);

                //get the encoding
                String encoding = CmsProperty.get(
                    CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING,
                    properties).getValue();
                if (encoding == null) {
                    encoding = OpenCms.getSystemInfo().getDefaultEncoding();
                }

                if (m_convertToXmlPage) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.LOG_IMPORTEXPORT_START_CONVERTING_TO_XML_0));
                    }

                    CmsXmlPage xmlPage = CmsXmlPageConverter.convertToXmlPage(
                        m_cms,
                        bodyfile.getContents(),
                        getLocale(resourcename, properties),
                        encoding);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key(Messages.LOG_IMPORTEXPORT_END_CONVERTING_TO_XML_0));
                    }

                    if (xmlPage != null) {
                        pagefile.setContents(xmlPage.marshal());

                        // set the type to xml page
                        pagefile.setType(CmsResourceTypeXmlPage.getStaticTypeId());
                    }
                }

                // add the template and other required properties
                CmsProperty newProperty = new CmsProperty(
                    CmsPropertyDefinition.PROPERTY_TEMPLATE,
                    mastertemplate,
                    null);
                // property lists must not contain equal properties
                properties.remove(newProperty);
                properties.add(newProperty);

                // if set, add the bodyclass as property
                if (CmsStringUtil.isNotEmpty(bodyclass)) {
                    newProperty = new CmsProperty(CmsPropertyDefinition.PROPERTY_TEMPLATE, mastertemplate, null);
                    newProperty.setAutoCreatePropertyDefinition(true);
                    properties.remove(newProperty);
                    properties.add(newProperty);
                }
                // if set, add bodyparams as properties
                if (bodyparams != null) {
                    for (Iterator<Entry<String, String>> p = bodyparams.entrySet().iterator(); p.hasNext();) {
                        Entry<String, String> entry = p.next();
                        String key = entry.getKey();
                        String value = entry.getValue();
                        newProperty = new CmsProperty(key, value, null);
                        newProperty.setAutoCreatePropertyDefinition(true);
                        properties.remove(newProperty);
                        properties.add(newProperty);
                    }
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_IMPORTEXPORT_START_IMPORTING_XML_PAGE_0));
                }

                // now import the resource
                m_cms.importResource(resourcename, pagefile, pagefile.getContents(), properties);

                // finally delete the old body file, it is not needed anymore
                m_cms.lockResource(bodyname);
                m_cms.deleteResource(bodyname, CmsResource.DELETE_PRESERVE_SIBLINGS);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_IMPORTEXPORT_END_IMPORTING_XML_PAGE_0));
                }

                m_report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);

            } else {

                // there are more than one template nodes in this control file
                // convert the resource into a plain text file
                // lock the resource, so that it can be manipulated
                m_cms.lockResource(resourcename);
                // set the type to plain
                pagefile.setType(CmsResourceTypePlain.getStaticTypeId());
                // write all changes
                m_cms.writeFile(pagefile);
                // done, unlock the resource
                m_cms.unlockResource(resourcename);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_IMPORTEXPORT_CANNOT_CONVERT_XML_STRUCTURE_1,
                            resourcename));
                }

                m_report.println(Messages.get().container(Messages.RPT_NOT_CONVERTED_0), I_CmsReport.FORMAT_OK);

            }

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_IMPORTEXPORT_END_MERGING_1, resourcename));
            }
        } catch (CmsXmlException e) {

            throw e;
        } catch (CmsException e) {

            m_report.println(e);

            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_MERGING_PAGE_FILE_1,
                resourcename);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }

            throw new CmsImportExportException(message, e);
        }

    }

    /**
     * Merges the page control files and their corresponding bodies into a single files.<p>
     *
     * @throws CmsImportExportException if something goes wrong
     * @throws CmsXmlException if the page file could not be unmarshalled
     */
    private void mergePageFiles() throws CmsXmlException, CmsImportExportException {

        try {
            // check if the template property exists. If not, create it.
            try {
                m_cms.readPropertyDefinition(CmsPropertyDefinition.PROPERTY_TEMPLATE);
            } catch (CmsException e) {
                // the template propertydefintion does not exist. So create it.
                m_cms.createPropertyDefinition(CmsPropertyDefinition.PROPERTY_TEMPLATE);
            }

            // copy all propertydefinitions of the old page to the new page
            List<CmsPropertyDefinition> definitions = m_cms.readAllPropertyDefinitions();

            Iterator<CmsPropertyDefinition> j = definitions.iterator();
            while (j.hasNext()) {
                CmsPropertyDefinition definition = j.next();
                // check if this propertydef already exits
                try {
                    m_cms.readPropertyDefinition(definition.getName());
                } catch (Exception e) {
                    m_cms.createPropertyDefinition(definition.getName());
                }
            }
        } catch (CmsException e) {

            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_COPYING_PROPERTY_DEFINITIONS_0);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }

            throw new CmsImportExportException(message);
        }

        // iterate through the list of all page controlfiles found during the import process
        int size = m_pageStorage.size();
        m_report.println(Messages.get().container(Messages.RPT_MERGE_START_0), I_CmsReport.FORMAT_HEADLINE);
        Iterator<String> i = m_pageStorage.iterator();
        int counter = 1;
        while (i.hasNext()) {
            String resname = i.next();
            // adjust the resourcename if nescessary
            if (!resname.startsWith("/")) {
                resname = "/" + resname;
            }

            m_report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_2,
                    String.valueOf(counter),
                    String.valueOf(size)),
                I_CmsReport.FORMAT_NOTE);
            m_report.print(Messages.get().container(Messages.RPT_MERGE_0), I_CmsReport.FORMAT_NOTE);
            m_report.print(
                org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, resname));

            mergePageFile(resname);
            if (LOG.isInfoEnabled()) {
                LOG.info(
                    Messages.get().getBundle().key(
                        Messages.LOG_MERGING_3,
                        String.valueOf(counter),
                        String.valueOf(size),
                        resname));
            }

            counter++;

        }
        // free mem
        m_pageStorage.clear();

    }

    /**
     * Deletes the folder structure which has been creating while importing the body files..<p>
     *
     * @throws CmsImportExportException if something goes wrong
     */
    private void removeFolders() throws CmsImportExportException {

        try {

            int size = m_folderStorage.size();

            m_report.println(Messages.get().container(Messages.RPT_DELFOLDER_START_0), I_CmsReport.FORMAT_HEADLINE);
            // iterate though all collected folders. Iteration must start at the end of the list,
            // as folders habe to be deleted in the reverse order.
            int counter = 1;
            for (int j = (size - 1); j >= 0; j--) {
                String resname = m_folderStorage.get(j);
                resname = (resname.startsWith("/") ? "" : "/") + resname + (resname.endsWith("/") ? "" : "/");
                // now check if the folder is really empty. Only delete empty folders
                List<CmsResource> files = m_cms.getFilesInFolder(resname, CmsResourceFilter.IGNORE_EXPIRATION);

                if (files.size() == 0) {
                    List<CmsResource> folders = m_cms.getSubFolders(resname, CmsResourceFilter.IGNORE_EXPIRATION);
                    if (folders.size() == 0) {
                        m_report.print(
                            org.opencms.report.Messages.get().container(
                                org.opencms.report.Messages.RPT_SUCCESSION_2,
                                String.valueOf(counter),
                                String.valueOf(size)),
                            I_CmsReport.FORMAT_NOTE);
                        m_report.print(Messages.get().container(Messages.RPT_DELFOLDER_0), I_CmsReport.FORMAT_NOTE);
                        m_report.print(
                            org.opencms.report.Messages.get().container(
                                org.opencms.report.Messages.RPT_ARGUMENT_1,
                                resname),
                            I_CmsReport.FORMAT_DEFAULT);
                        m_cms.lockResource(resname);
                        m_cms.deleteResource(resname, CmsResource.DELETE_PRESERVE_SIBLINGS);
                        m_report.println(
                            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                            I_CmsReport.FORMAT_OK);
                        counter++;
                    }
                }
            }
        } catch (CmsException e) {

            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_REMOVING_FOLDERS_OF_IMPORTED_BODY_FILES_0);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }

            throw new CmsImportExportException(message, e);
        }
    }
}
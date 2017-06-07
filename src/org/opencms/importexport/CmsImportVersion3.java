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
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPasswordHandler;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.page.CmsXmlPage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Implementation of the OpenCms Import Interface ({@link org.opencms.importexport.I_CmsImport}) for
 * the import version 3.<p>
 *
 * This import format was used in OpenCms 5.1.2 - 5.1.6.<p>
 *
 * @since 6.0.0
 *
 * @see org.opencms.importexport.A_CmsImport
 *
 * @deprecated this import class is no longer in use and should only be used to import old export files
 */
@Deprecated
public class CmsImportVersion3 extends A_CmsImport {

    /** The version number of this import implementation.<p> */
    private static final int IMPORT_VERSION = 3;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsImportVersion3.class);

    /**
     * Creates a new CmsImportVerion3 object.<p>
     */
    public CmsImportVersion3() {

        m_convertToXmlPage = true;
    }

    /**
     * @see org.opencms.importexport.I_CmsImport#getVersion()
     * @return the version number of this import implementation
     */
    public int getVersion() {

        return CmsImportVersion3.IMPORT_VERSION;
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
     * @see org.opencms.importexport.I_CmsImport#importData(CmsObject, I_CmsReport, CmsImportParameters)
     */
    public void importData(CmsObject cms, I_CmsReport report, CmsImportParameters params)
    throws CmsImportExportException, CmsXmlException {

        // initialize the import
        initialize();
        m_cms = cms;
        m_importPath = params.getDestinationPath();
        m_report = report;

        m_linkStorage = new HashMap<String, String>();
        m_linkPropertyStorage = new HashMap<String, List<CmsProperty>>();
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
        } catch (IOException ioe) {
            CmsMessageContainer msg = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_READING_FILE_1,
                CmsImportExportManager.EXPORT_MANIFEST);
            if (LOG.isErrorEnabled()) {
                LOG.error(msg.key(), ioe);
            }
            throw new CmsImportExportException(msg, ioe);
        } finally {
            helper.closeFile();
            cleanUp();
        }
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
     * Imports the resources and writes them to the cms.<p>
     *
     * @throws CmsImportExportException if something goes wrong
     */
    @SuppressWarnings("unchecked")
    private void importAllResources() throws CmsImportExportException {

        String source, destination, type, uuidresource, userlastmodified, usercreated, flags, timestamp;
        long datelastmodified, datecreated;

        List<Element> fileNodes, acentryNodes;
        Element currentElement, currentEntry;
        List<CmsProperty> properties = null;

        // get list of unwanted properties
        List<String> deleteProperties = OpenCms.getImportExportManager().getIgnoredProperties();
        if (deleteProperties == null) {
            deleteProperties = new ArrayList<String>();
        }
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
        // get the wanted page type for imported pages
        m_convertToXmlPage = OpenCms.getImportExportManager().convertToXmlPage();

        try {
            // get all file-nodes
            fileNodes = m_docXml.selectNodes("//" + A_CmsImport.N_FILE);

            int importSize = fileNodes.size();
            // walk through all files in manifest
            for (int i = 0; i < fileNodes.size(); i++) {
                m_report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_SUCCESSION_2,
                        String.valueOf(i + 1),
                        String.valueOf(importSize)));
                currentElement = fileNodes.get(i);
                // get all information for a file-import
                // <source>
                source = getChildElementTextValue(currentElement, A_CmsImport.N_SOURCE);
                // <destintion>
                destination = getChildElementTextValue(currentElement, A_CmsImport.N_DESTINATION);
                // <type>
                type = getChildElementTextValue(currentElement, A_CmsImport.N_TYPE);
                // <uuidstructure>
                //uuidstructure = CmsImport.getChildElementTextValue(
                //    currentElement,
                //    CmsImportExportManager.N_UUIDSTRUCTURE);
                // <uuidresource>
                uuidresource = getChildElementTextValue(currentElement, A_CmsImport.N_UUIDRESOURCE);
                // <datelastmodified>
                timestamp = getChildElementTextValue(currentElement, A_CmsImport.N_DATELASTMODIFIED);
                if (timestamp != null) {
                    datelastmodified = Long.parseLong(timestamp);
                } else {
                    datelastmodified = System.currentTimeMillis();
                }
                // <userlastmodified>
                userlastmodified = getChildElementTextValue(currentElement, A_CmsImport.N_USERLASTMODIFIED);
                // <datecreated>
                timestamp = getChildElementTextValue(currentElement, A_CmsImport.N_DATECREATED);
                if (timestamp != null) {
                    datecreated = Long.parseLong(timestamp);
                } else {
                    datecreated = System.currentTimeMillis();
                }
                // <usercreated>
                usercreated = getChildElementTextValue(currentElement, A_CmsImport.N_USERCREATED);
                // <flags>
                flags = getChildElementTextValue(currentElement, A_CmsImport.N_FLAGS);

                String translatedName = m_cms.getRequestContext().addSiteRoot(m_importPath + destination);
                if (CmsResourceTypeFolder.RESOURCE_TYPE_NAME.equals(type)) {
                    translatedName += "/";
                }
                // translate the name during import
                translatedName = m_cms.getRequestContext().getDirectoryTranslator().translateResource(translatedName);
                // check if this resource is immutable
                boolean resourceNotImmutable = checkImmutable(translatedName, immutableResources);
                translatedName = m_cms.getRequestContext().removeSiteRoot(translatedName);
                // if the resource is not immutable and not on the exclude list, import it
                if (resourceNotImmutable) {
                    // print out the information to the report
                    m_report.print(Messages.get().container(Messages.RPT_IMPORTING_0), I_CmsReport.FORMAT_NOTE);
                    m_report.print(
                        org.opencms.report.Messages.get().container(
                            org.opencms.report.Messages.RPT_ARGUMENT_1,
                            translatedName));
                    //m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0));
                    // get all properties
                    properties = readPropertiesFromManifest(currentElement, deleteProperties);

                    // import the resource
                    CmsResource res = importResource(
                        source,
                        destination,
                        type,
                        uuidresource,
                        datelastmodified,
                        userlastmodified,
                        datecreated,
                        usercreated,
                        flags,
                        properties);

                    List<CmsAccessControlEntry> aceList = new ArrayList<CmsAccessControlEntry>();
                    if (res != null) {
                        // write all imported access control entries for this file
                        acentryNodes = currentElement.selectNodes("*/" + A_CmsImport.N_ACCESSCONTROL_ENTRY);
                        // collect all access control entries
                        for (int j = 0; j < acentryNodes.size(); j++) {
                            currentEntry = acentryNodes.get(j);
                            // get the data of the access control entry
                            String id = getChildElementTextValue(currentEntry, A_CmsImport.N_ACCESSCONTROL_PRINCIPAL);
                            String acflags = getChildElementTextValue(currentEntry, A_CmsImport.N_FLAGS);
                            String allowed = getChildElementTextValue(
                                currentEntry,
                                A_CmsImport.N_ACCESSCONTROL_PERMISSIONSET
                                    + "/"
                                    + A_CmsImport.N_ACCESSCONTROL_ALLOWEDPERMISSIONS);
                            String denied = getChildElementTextValue(
                                currentEntry,
                                A_CmsImport.N_ACCESSCONTROL_PERMISSIONSET
                                    + "/"
                                    + A_CmsImport.N_ACCESSCONTROL_DENIEDPERMISSIONS);

                            // get the correct principal
                            try {
                                String principalId = new CmsUUID().toString();
                                String principal = id.substring(id.indexOf('.') + 1, id.length());

                                if (id.startsWith(I_CmsPrincipal.PRINCIPAL_GROUP)) {
                                    principal = OpenCms.getImportExportManager().translateGroup(principal);
                                    principalId = m_cms.readGroup(principal).getId().toString();
                                } else {
                                    principal = OpenCms.getImportExportManager().translateUser(principal);
                                    principalId = m_cms.readUser(principal).getId().toString();
                                }

                                // add the entry to the list
                                aceList.add(getImportAccessControlEntry(res, principalId, allowed, denied, acflags));
                            } catch (CmsDataAccessException e) {
                                // user or group not found, so do not import the ace
                            }
                        }
                        importAccessControlEntries(res, aceList);

                    } else {
                        // resource import failed, since no CmsResource was created
                        m_report.print(Messages.get().container(Messages.RPT_SKIPPING_0), I_CmsReport.FORMAT_NOTE);
                        m_report.println(
                            org.opencms.report.Messages.get().container(
                                org.opencms.report.Messages.RPT_ARGUMENT_1,
                                translatedName));
                    }
                } else {
                    // skip the file import, just print out the information to the report
                    m_report.print(Messages.get().container(Messages.RPT_SKIPPING_0), I_CmsReport.FORMAT_NOTE);
                    m_report.println(
                        org.opencms.report.Messages.get().container(
                            org.opencms.report.Messages.RPT_ARGUMENT_1,
                            translatedName));
                }
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
        }

    }

    /**
     * Imports a resource (file or folder) into the cms.<p>
     *
     * @param source the path to the source-file
     * @param destination the path to the destination-file in the cms
     * @param type the resource-type of the file
     * @param uuidresource  the resource uuid of the resource
     * @param datelastmodified the last modification date of the resource
     * @param userlastmodified the user who made the last modifications to the resource
     * @param datecreated the creation date of the resource
     * @param usercreated the user who created
     * @param flags the flags of the resource
     * @param properties a list with properties for this resource
     *
     * @return imported resource
     */
    private CmsResource importResource(
        String source,
        String destination,
        String type,
        String uuidresource,
        long datelastmodified,
        String userlastmodified,
        long datecreated,
        String usercreated,
        String flags,
        List<CmsProperty> properties) {

        byte[] content = null;
        CmsResource result = null;

        try {

            // get the file content
            if (source != null) {
                content = getFileBytes(source);
            }
            int size = 0;
            if (content != null) {
                size = content.length;
            }

            // get all required information to create a CmsResource
            I_CmsResourceType resType;

            // get UUIDs for the user
            CmsUUID newUserlastmodified;
            CmsUUID newUsercreated;
            // check if user created and user lastmodified are valid users in this system.
            // if not, use the current user
            try {
                newUserlastmodified = m_cms.readUser(userlastmodified).getId();
            } catch (CmsException e) {
                newUserlastmodified = m_cms.getRequestContext().getCurrentUser().getId();
                // datelastmodified = System.currentTimeMillis();
            }

            try {
                newUsercreated = m_cms.readUser(usercreated).getId();
            } catch (CmsException e) {
                newUsercreated = m_cms.getRequestContext().getCurrentUser().getId();
                // datecreated = System.currentTimeMillis();
            }

            // convert to xml page if wanted
            if (m_convertToXmlPage && (type.equals(RESOURCE_TYPE_NEWPAGE_NAME))) {

                if (content != null) {

                    //get the encoding
                    String encoding = null;
                    encoding = CmsProperty.get(CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, properties).getValue();
                    if (encoding == null) {
                        encoding = OpenCms.getSystemInfo().getDefaultEncoding();
                    }

                    CmsXmlPage xmlPage = CmsXmlPageConverter.convertToXmlPage(
                        m_cms,
                        content,
                        getLocale(destination, properties),
                        encoding);

                    content = xmlPage.marshal();
                }
                resType = OpenCms.getResourceManager().getResourceType(CmsResourceTypeXmlPage.getStaticTypeId());
            } else if (type.equals(RESOURCE_TYPE_LINK_NAME)) {
                resType = OpenCms.getResourceManager().getResourceType(CmsResourceTypePointer.getStaticTypeId());
            } else if (type.equals(RESOURCE_TYPE_LEGACY_PAGE_NAME)) {
                resType = OpenCms.getResourceManager().getResourceType(CmsResourceTypePlain.getStaticTypeId());
            } else {
                resType = OpenCms.getResourceManager().getResourceType(type);
            }

            // get UUIDs for the resource and content
            CmsUUID newUuidresource = null;
            if ((uuidresource != null) && (!resType.isFolder())) {
                // create a UUID from the provided string
                newUuidresource = new CmsUUID(uuidresource);
            } else {
                // folders get always a new resource record UUID
                newUuidresource = new CmsUUID();
            }

            // create a new CmsResource
            CmsResource resource = new CmsResource(
                new CmsUUID(), // structure ID is always a new UUID
                newUuidresource,
                destination,
                resType.getTypeId(),
                resType.isFolder(),
                new Integer(flags).intValue(),
                m_cms.getRequestContext().getCurrentProject().getUuid(),
                CmsResource.STATE_NEW,
                datecreated,
                newUsercreated,
                datelastmodified,
                newUserlastmodified,
                CmsResource.DATE_RELEASED_DEFAULT,
                CmsResource.DATE_EXPIRED_DEFAULT,
                1,
                size,
                System.currentTimeMillis(),
                0);

            if (type.equals(RESOURCE_TYPE_LINK_NAME)) {
                // store links for later conversion
                m_report.print(Messages.get().container(Messages.RPT_STORING_LINK_0), I_CmsReport.FORMAT_NOTE);
                m_linkStorage.put(m_importPath + destination, new String(content));
                m_linkPropertyStorage.put(m_importPath + destination, properties);
                result = resource;
            } else {
                // import this resource in the VFS
                result = m_cms.importResource(destination, resource, content, properties);
            }

            if (result != null) {
                m_report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);
            }
        } catch (Exception exc) {
            // an error while importing the file
            m_report.println(exc);
            m_report.addError(exc);

            try {
                // Sleep some time after an error so that the report output has a chance to keep up
                Thread.sleep(1000);
            } catch (Exception e) {
                //
            }
        }

        return result;
    }
}
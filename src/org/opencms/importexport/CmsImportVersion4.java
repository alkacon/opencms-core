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
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.I_CmsLinkParseable;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPasswordHandler;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * Implementation of the OpenCms Import Interface ({@link org.opencms.importexport.I_CmsImport}) for
 * the import version 4.<p>
 *
 * This import format is used in OpenCms since 5.1.6.<p>
 *
 * @since 6.0.0
 *
 * @see org.opencms.importexport.A_CmsImport
 *
 * @deprecated this import class is no longer in use and should only be used to import old export files
 */
@Deprecated
public class CmsImportVersion4 extends A_CmsImport {

    /** The version number of this import implementation.<p> */
    private static final int IMPORT_VERSION = 4;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsImportVersion4.class);

    /** Stores all resource of type that implements the {@link I_CmsLinkParseable} interface. */
    private List<CmsResource> m_parseables;

    /**
     * Creates a new CmsImportVerion4 object.<p>
     */
    public CmsImportVersion4() {

        m_convertToXmlPage = true;
    }

    /**
     * @see org.opencms.importexport.I_CmsImport#getVersion()
     * @return the version number of this import implementation
     */
    public int getVersion() {

        return CmsImportVersion4.IMPORT_VERSION;
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
        m_parseables = new ArrayList<CmsResource>();

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
            readResourcesFromManifest();
            convertPointerToSiblings();
            rewriteParseables();
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
        Document docXml)
    throws CmsImportExportException {

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
        List<String> userGroups)
    throws CmsImportExportException {

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
     * Rewrites all parseable files, to assure link check.<p>
     */
    protected void rewriteParseables() {

        if (m_parseables.isEmpty()) {
            return;
        }

        m_report.println(Messages.get().container(Messages.RPT_START_PARSE_LINKS_0), I_CmsReport.FORMAT_HEADLINE);

        int i = 0;
        Iterator<CmsResource> it = m_parseables.iterator();
        while (it.hasNext()) {
            CmsResource res = it.next();

            m_report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_2,
                    String.valueOf(i + 1),
                    String.valueOf(m_parseables.size())),
                I_CmsReport.FORMAT_NOTE);

            m_report.print(
                Messages.get().container(Messages.RPT_PARSE_LINKS_FOR_1, m_cms.getSitePath(res)),
                I_CmsReport.FORMAT_NOTE);
            m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

            try {
                // make sure the date last modified is kept...
                CmsFile file = m_cms.readFile(res);
                file.setDateLastModified(res.getDateLastModified());
                m_cms.writeFile(file);

                m_report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);
            } catch (Throwable e) {
                m_report.addWarning(e);
                m_report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_FAILED_0),
                    I_CmsReport.FORMAT_ERROR);
                if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(Messages.LOG_IMPORTEXPORT_REWRITING_1, res.getRootPath()));
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug(e.getLocalizedMessage(), e);
                }
            }
            i++;
        }

        m_report.println(Messages.get().container(Messages.RPT_END_PARSE_LINKS_0), I_CmsReport.FORMAT_HEADLINE);
    }

    /**
     * Convert a given timestamp from a String format to a long value.<p>
     *
     * The timestamp is either the string representation of a long value (old export format)
     * or a user-readable string format.
     *
     * @param timestamp timestamp to convert
     * @return long value of the timestamp
     */
    private long convertTimestamp(String timestamp) {

        long value = 0;
        // try to parse the timestamp string
        // if it successes, its an old style long value
        try {
            value = Long.parseLong(timestamp);

        } catch (NumberFormatException e) {
            // the timestamp was in in a user-readable string format, create the long value form it
            try {
                value = CmsDateUtil.parseHeaderDate(timestamp);
            } catch (ParseException pe) {
                value = System.currentTimeMillis();
            }
        }
        return value;
    }

    /**
     * Imports a resource (file or folder) into the cms.<p>
     *
     * @param source the path to the source-file
     * @param destination the path to the destination-file in the cms
     * @param type the resource type name of the file
     * @param uuidresource  the resource uuid of the resource
     * @param datelastmodified the last modification date of the resource
     * @param userlastmodified the user who made the last modifications to the resource
     * @param datecreated the creation date of the resource
     * @param usercreated the user who created
     * @param datereleased the release date of the resource
     * @param dateexpired the expire date of the resource
     * @param flags the flags of the resource
     * @param properties a list with properties for this resource
     *
     * @return imported resource
     */
    private CmsResource importResource(
        String source,
        String destination,
        I_CmsResourceType type,
        String uuidresource,
        long datelastmodified,
        String userlastmodified,
        long datecreated,
        String usercreated,
        long datereleased,
        long dateexpired,
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

            // get UUIDs for the resource and content
            CmsUUID newUuidresource = null;
            if ((uuidresource != null) && (!type.isFolder())) {
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
                type.getTypeId(),
                type.isFolder(),
                Integer.valueOf(flags).intValue(),
                m_cms.getRequestContext().getCurrentProject().getUuid(),
                CmsResource.STATE_NEW,
                datecreated,
                newUsercreated,
                datelastmodified,
                newUserlastmodified,
                datereleased,
                dateexpired,
                1,
                size,
                System.currentTimeMillis(),
                0);

            // import this resource in the VFS
            result = m_cms.importResource(destination, resource, content, properties);

            if (result != null) {
                m_report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);
            }
        } catch (Exception exc) {
            // an error while importing the file
            m_report.println(exc);
            try {
                // Sleep some time after an error so that the report output has a chance to keep up
                Thread.sleep(1000);
            } catch (Exception e) {
                //
            }
        }
        return result;
    }

    /**
     * Reads all file nodes plus their meta-information (properties, ACL)
     * from the <code>manifest.xml</code> and imports them as Cms resources to the VFS.<p>
     *
     * @throws CmsImportExportException if something goes wrong
     */
    private void readResourcesFromManifest() throws CmsImportExportException {

        String source = null, destination = null, uuidresource = null, userlastmodified = null, usercreated = null,
        flags = null, timestamp = null;
        long datelastmodified = 0, datecreated = 0, datereleased = 0, dateexpired = 0;

        List<Node> fileNodes = null;
        List<Node> acentryNodes = null;
        Element currentElement = null, currentEntry = null;
        List<CmsProperty> properties = null;

        // get list of immutable resources
        List<String> immutableResources = OpenCms.getImportExportManager().getImmutableResources();
        if (immutableResources == null) {
            immutableResources = Collections.emptyList();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get().getBundle().key(
                    Messages.LOG_IMPORTEXPORT_IMMUTABLE_RESOURCES_SIZE_1,
                    Integer.toString(immutableResources.size())));
        }
        // get list of ignored properties
        List<String> ignoredProperties = OpenCms.getImportExportManager().getIgnoredProperties();
        if (ignoredProperties == null) {
            ignoredProperties = Collections.emptyList();
        }

        // get the desired page type for imported pages
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
                        String.valueOf(importSize)),
                    I_CmsReport.FORMAT_NOTE);
                currentElement = (Element)fileNodes.get(i);

                // <source>
                source = getChildElementTextValue(currentElement, A_CmsImport.N_SOURCE);
                // <destination>

                destination = getChildElementTextValue(currentElement, A_CmsImport.N_DESTINATION);

                // <type>
                String typeName = getChildElementTextValue(currentElement, A_CmsImport.N_TYPE);
                I_CmsResourceType type;
                try {
                    type = OpenCms.getResourceManager().getResourceType(typeName);
                } catch (CmsLoaderException e) {
                    // unknown resource type, import resource as type "plain"
                    type = OpenCms.getResourceManager().getResourceType(CmsResourceTypePlain.getStaticTypeName());
                }

                if (!type.isFolder()) {
                    // <uuidresource>
                    uuidresource = getChildElementTextValue(currentElement, A_CmsImport.N_UUIDRESOURCE);
                } else {
                    uuidresource = null;
                }

                // <datelastmodified>
                timestamp = getChildElementTextValue(currentElement, A_CmsImport.N_DATELASTMODIFIED);
                if (timestamp != null) {
                    datelastmodified = convertTimestamp(timestamp);
                } else {
                    datelastmodified = System.currentTimeMillis();
                }

                // <userlastmodified>
                userlastmodified = getChildElementTextValue(currentElement, A_CmsImport.N_USERLASTMODIFIED);
                userlastmodified = OpenCms.getImportExportManager().translateUser(userlastmodified);

                // <datecreated>
                timestamp = getChildElementTextValue(currentElement, A_CmsImport.N_DATECREATED);
                if (timestamp != null) {
                    datecreated = convertTimestamp(timestamp);
                } else {
                    datecreated = System.currentTimeMillis();
                }

                // <usercreated>
                usercreated = getChildElementTextValue(currentElement, A_CmsImport.N_USERCREATED);
                usercreated = OpenCms.getImportExportManager().translateUser(usercreated);

                // <datereleased>
                timestamp = getChildElementTextValue(currentElement, A_CmsImport.N_DATERELEASED);
                if (timestamp != null) {
                    datereleased = convertTimestamp(timestamp);
                } else {
                    datereleased = CmsResource.DATE_RELEASED_DEFAULT;
                }

                // <dateexpired>
                timestamp = getChildElementTextValue(currentElement, A_CmsImport.N_DATEEXPIRED);
                if (timestamp != null) {
                    dateexpired = convertTimestamp(timestamp);
                } else {
                    dateexpired = CmsResource.DATE_EXPIRED_DEFAULT;
                }

                // <flags>
                flags = getChildElementTextValue(currentElement, A_CmsImport.N_FLAGS);

                // apply name translation and import path
                String translatedName = m_cms.getRequestContext().addSiteRoot(m_importPath + destination);
                if (type.isFolder()) {
                    // ensure folders end with a "/"
                    if (!CmsResource.isFolder(translatedName)) {
                        translatedName += "/";
                    }
                }

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
                    m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                    // get all properties
                    properties = readPropertiesFromManifest(currentElement, ignoredProperties);

                    // import the resource
                    CmsResource res = importResource(
                        source,
                        translatedName,
                        type,
                        uuidresource,
                        datelastmodified,
                        userlastmodified,
                        datecreated,
                        usercreated,
                        datereleased,
                        dateexpired,
                        flags,
                        properties);

                    // if the resource was imported add the access control entrys if available
                    if (res != null) {

                        List<CmsAccessControlEntry> aceList = new ArrayList<CmsAccessControlEntry>();

                        // write all imported access control entries for this file
                        acentryNodes = currentElement.selectNodes("*/" + A_CmsImport.N_ACCESSCONTROL_ENTRY);

                        // collect all access control entries
                        for (int j = 0; j < acentryNodes.size(); j++) {
                            currentEntry = (Element)acentryNodes.get(j);

                            // get the data of the access control entry
                            String id = getChildElementTextValue(currentEntry, A_CmsImport.N_ACCESSCONTROL_PRINCIPAL);
                            String principalId = new CmsUUID().toString();
                            String principal = id.substring(id.indexOf('.') + 1, id.length());

                            try {
                                if (id.startsWith(I_CmsPrincipal.PRINCIPAL_GROUP)) {
                                    principal = OpenCms.getImportExportManager().translateGroup(principal);
                                    principalId = m_cms.readGroup(principal).getId().toString();
                                } else {
                                    principal = OpenCms.getImportExportManager().translateUser(principal);
                                    principalId = m_cms.readUser(principal).getId().toString();
                                }

                                String acflags = getChildElementTextValue(currentEntry, A_CmsImport.N_FLAGS);

                                String allowed = ((Element)currentEntry.selectNodes("./"
                                    + A_CmsImport.N_ACCESSCONTROL_PERMISSIONSET
                                    + "/"
                                    + A_CmsImport.N_ACCESSCONTROL_ALLOWEDPERMISSIONS).get(0)).getTextTrim();

                                String denied = ((Element)currentEntry.selectNodes("./"
                                    + A_CmsImport.N_ACCESSCONTROL_PERMISSIONSET
                                    + "/"
                                    + A_CmsImport.N_ACCESSCONTROL_DENIEDPERMISSIONS).get(0)).getTextTrim();

                                // add the entry to the list
                                aceList.add(getImportAccessControlEntry(res, principalId, allowed, denied, acflags));
                            } catch (CmsException e) {
                                // user or group of ACE might not exist in target system, ignore ACE
                                if (LOG.isWarnEnabled()) {
                                    LOG.warn(
                                        Messages.get().getBundle().key(
                                            Messages.LOG_IMPORTEXPORT_ERROR_IMPORTING_ACE_1,
                                            translatedName),
                                        e);
                                }
                                m_report.println(e);
                            }
                        }

                        importAccessControlEntries(res, aceList);
                        if (OpenCms.getResourceManager().getResourceType(
                            res.getTypeId()) instanceof I_CmsLinkParseable) {
                            // store for later use
                            m_parseables.add(res);
                        }
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
}
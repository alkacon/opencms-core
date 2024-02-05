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

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.i18n.I_CmsMessageBundle;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * Collection of common used methods for implementing OpenCms Import classes.<p>
 *
 * This class does not implement a real OpenCms import, real import implementation should be
 * inherited form this class.<p>
 *
 * @since 6.0.0
 *
 * @see org.opencms.importexport.I_CmsImport
 *
 * @deprecated the import is done starting with {@link CmsImportVersion7} with the digester
 */
@Deprecated
public abstract class A_CmsImport implements I_CmsImport {

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "userinfo/entry@name" attribute, contains the additional user info entry name. */
    public static final String A_NAME = "name";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "userinfo/entry@type" attribute, contains the additional user info entry data type name. */
    public static final String A_TYPE = "type";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "access" node. */
    public static final String N_ACCESS = "access";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "allowed" node, to identify allowed user permissions. */
    public static final String N_ACCESSCONTROL_ALLOWEDPERMISSIONS = "allowed";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "denied" node, to identify denied user permissions. */
    public static final String N_ACCESSCONTROL_DENIEDPERMISSIONS = "denied";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "accesscontrol" node, to identify access control entries. */
    public static final String N_ACCESSCONTROL_ENTRIES = "accesscontrol";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "accessentry" node, to identify a single access control entry. */
    public static final String N_ACCESSCONTROL_ENTRY = "accessentry";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "permissionset" node, to identify a permission set. */
    public static final String N_ACCESSCONTROL_PERMISSIONSET = "permissionset";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "uuidprincipal" node, to identify a principal UUID. */
    public static final String N_ACCESSCONTROL_PRINCIPAL = "uuidprincipal";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "datecreated" node, contains the date created VFS file attribute. */
    public static final String N_DATECREATED = "datecreated";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "dateexpired" node, contains the expiration date VFS file attribute. */
    public static final String N_DATEEXPIRED = "dateexpired";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "datelastmodified" node, contains the date last modified VFS file attribute. */
    public static final String N_DATELASTMODIFIED = "datelastmodified";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "datereleased" node, contains the release date VFS file attribute. */
    public static final String N_DATERELEASED = "datereleased";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "defaultgroup" node, for backward compatibility with OpenCms 5.x. */
    public static final String N_DEFAULTGROUP = "defaultgroup";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "description" node, contains a users description test. */
    public static final String N_DESCRIPTION = "description";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "destination" node, contains target VFS file name. */
    public static final String N_DESTINATION = "destination";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "email" node, contains a users email. */
    public static final String N_EMAIL = "email";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "file" node, container node for all VFS resources. */
    public static final String N_FILE = "file";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "firstname" node, contains a users first name. */
    public static final String N_FIRSTNAME = "firstname";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "flags" node, contains the flags of a VFS resource. */
    public static final String N_FLAGS = "flags";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "groupdata" node, contains a users group data. */
    public static final String N_GROUPDATA = "groupdata";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "groupname" node, contains a groups name. */
    public static final String N_GROUPNAME = "groupname";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "id" node, only required for backward compatibility with import version 2. */
    public static final String N_ID = "id";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "lastmodified" node, only required for backward compatibility with import version 2. */
    public static final String N_LASTMODIFIED = "lastmodified";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "lastname" node, contains a users last name. */
    public static final String N_LASTNAME = "lastname";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "name" node, contains a users login name. */
    public static final String N_NAME = "name";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "orgunitdatas" node, starts the organizational unit data. */
    public static final String N_ORGUNITDATA = "orgunitdata";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "parentgroup" node, contains a groups parent group name. */
    public static final String N_PARENTGROUP = "parentgroup";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "password" node, contains a users encrypted password. */
    public static final String N_PASSWORD = "password";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "properties" node, starts the list of properties of a VFS resource. */
    public static final String N_PROPERTIES = "properties";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "property" node, starts a property for a VFS resource. */
    public static final String N_PROPERTY = "property";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "type" property attribute, contains a property type. */
    public static final String N_PROPERTY_ATTRIB_TYPE = "type";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "shared" property type attribute value. */
    public static final String N_PROPERTY_ATTRIB_TYPE_SHARED = "shared";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "relation" node, starts a relation for a VFS resource. */
    public static final String N_RELATION = "relation";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "id" relation attribute, contains the structure id of the target resource of the relation. */
    public static final String N_RELATION_ATTRIBUTE_ID = "id";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "path" relation attribute, contains the path to the target resource of the relation. */
    public static final String N_RELATION_ATTRIBUTE_PATH = "path";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "type" relation attribute, contains the type of relation. */
    public static final String N_RELATION_ATTRIBUTE_TYPE = "type";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "relations" node, starts the list of relations of a VFS resources. */
    public static final String N_RELATIONS = "relations";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "source" node, contains the source path of a VFS resource in the import zip (or folder). */
    public static final String N_SOURCE = "source";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "address" node, contains a users address. */
    public static final String N_TAG_ADDRESS = "address";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "type" node, the resource type name of a VFS resource. */
    public static final String N_TYPE = "type";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "user" node, starts the user data. */
    public static final String N_USER = "user";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "usercreated" node, contains the name of the user who created the VFS resource. */
    public static final String N_USERCREATED = "usercreated";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "userdata" node, starts the list of users. */
    public static final String N_USERDATA = "userdata";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "usergroupdatas" node, starts the users group data. */
    public static final String N_USERGROUPDATA = "usergroupdata";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "usergroups" node, starts the users group data. */
    public static final String N_USERGROUPS = "usergroups";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "userinfo" node, contains the additional user info. */
    public static final String N_USERINFO = "userinfo";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "userinfo/entry" node, contains the additional user info entry value. */
    public static final String N_USERINFO_ENTRY = "entry";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "userlastmodified" node, contains the name of the user who last modified the VFS resource. */
    public static final String N_USERLASTMODIFIED = "userlastmodified";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "uuidresource" node, contains a the resource UUID of a VFS resource. */
    public static final String N_UUIDRESOURCE = "uuidresource";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "uuidstructure" node, only required for backward compatibility with import version 2. */
    public static final String N_UUIDSTRUCTURE = "uuidstructure";

    /** Tag in the {@link CmsImportExportManager#EXPORT_MANIFEST} for the "value" node, contains the value of a property. */
    public static final String N_VALUE = "value";

    /** The name of the legacy resource type "page". */
    public static final String RESOURCE_TYPE_LEGACY_PAGE_NAME = "page";

    /** The id of the legacy resource type "link". */
    protected static final int RESOURCE_TYPE_LINK_ID = 1024;

    /** The name of the legacy resource type "link". */
    protected static final String RESOURCE_TYPE_LINK_NAME = "link";

    /** The id of the legacy resource type "newpage". */
    protected static final int RESOURCE_TYPE_NEWPAGE_ID = 9;

    /** The name of the legacy resource type "newpage". */
    protected static final String RESOURCE_TYPE_NEWPAGE_NAME = "newpage";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsImport.class);

    /** The cms context to do the import operations with. */
    protected CmsObject m_cms;

    /** Flag for conversion to xml pages. */
    protected boolean m_convertToXmlPage;

    /** The xml manifest-file. */
    protected Document m_docXml;

    /** Groups to create during import are stored here. */
    protected Stack<Map<String, String>> m_groupsToCreate;

    /** The import-path to write resources into the cms. */
    protected String m_importPath;

    /** The import-resource (folder) to load resources from. */
    protected File m_importResource;

    /** The import-resource (zip) to load resources from. */
    protected ZipFile m_importZip;

    /** Storage for all pointer properties which must be converted into links. */
    protected Map<String, List<CmsProperty>> m_linkPropertyStorage;

    /** Storage for all pointers which must be converted into links. */
    protected Map<String, String> m_linkStorage;

    /** The object to report the log messages. */
    protected I_CmsReport m_report;

    /** Messages object with the locale of the current user. */
    protected I_CmsMessageBundle m_userMessages;

    /**
     * Converts a given digest to base64 encoding.<p>
     *
     * @param value the digest value in the legacy encoding
     * @return the digest in the new encoding
     */
    public String convertDigestEncoding(String value) {

        byte[] data = new byte[value.length() / 2];

        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(Integer.parseInt(value.substring(i * 2, (i * 2) + 2), 16) - 128);
        }

        return new String(Base64.encodeBase64(data));
    }

    /**
     * Returns the value of a child element with a specified name for a given parent element.<p>
     *
     * @param parentElement the parent element
     * @param elementName the child element name
     *
     * @return the value of the child node, or null if something went wrong
     */
    public String getChildElementTextValue(Element parentElement, String elementName) {

        try {
            // get the first child element matching the specified name
            Element childElement = (Element)parentElement.selectNodes("./" + elementName).get(0);
            // return the value of the child element
            return childElement.getTextTrim();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @see org.opencms.importexport.I_CmsImport#matches(org.opencms.importexport.CmsImportParameters)
     */
    public boolean matches(CmsImportParameters parameters) throws CmsImportExportException {

        // try to read the export version number
        CmsImportHelper helper = new CmsImportHelper(parameters);
        try {
            helper.openFile();
            // read the xml-config file
            Document docXml = CmsXmlUtils.unmarshalHelper(
                helper.getFileBytes(CmsImportExportManager.EXPORT_MANIFEST),
                null,
                false);

            return getVersion() == Integer.parseInt(
                ((Element)docXml.selectNodes("//" + CmsImportExportManager.N_VERSION).get(0)).getTextTrim());
        } catch (IOException e) {
            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_OPENING_ZIP_ARCHIVE_1,
                parameters.getPath());
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }

            throw new CmsImportExportException(message, e);
        } catch (Exception e) {
            // ignore the exception, the export file has no version number (version 0)
            // should never happen
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        } finally {
            helper.closeFile();
        }
        return false;
    }

    /**
     * Checks if the resources is in the list of immutalbe resources. <p>
     *
     * @param translatedName the name of the resource
     * @param immutableResources the list of the immutable resources
     * @return true or false
     */
    protected boolean checkImmutable(String translatedName, List<String> immutableResources) {

        boolean resourceNotImmutable = true;
        if (immutableResources.contains(translatedName)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    Messages.get().getBundle().key(Messages.LOG_IMPORTEXPORT_RESOURCENAME_IMMUTABLE_1, translatedName));
            }
            // this resource must not be modified by an import if it already exists
            String storedSiteRoot = m_cms.getRequestContext().getSiteRoot();
            try {
                m_cms.getRequestContext().setSiteRoot("/");
                m_cms.readResource(translatedName);
                resourceNotImmutable = false;
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(Messages.LOG_IMPORTEXPORT_IMMUTABLE_FLAG_SET_1, translatedName));
                }
            } catch (CmsException e) {
                // resourceNotImmutable will be true
                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_IMPORTEXPORT_ERROR_ON_TEST_IMMUTABLE_1,
                            translatedName),
                        e);
                }
            } finally {
                m_cms.getRequestContext().setSiteRoot(storedSiteRoot);
            }
        }
        return resourceNotImmutable;
    }

    /**
     * Cleans up member variables after the import is finished.<p>
     *
     * This is required since there is only one instance for
     * each import version that is kept in memory and reused.<p>
     */
    protected void cleanUp() {

        m_importResource = null;
        m_importZip = null;
        m_report = null;
        m_linkStorage = null;
        m_linkPropertyStorage = null;
        m_groupsToCreate = null;
        m_cms = null;
    }

    /**
     * Converts old style pointers to siblings if possible.<p>
     */
    protected void convertPointerToSiblings() {

        try {
            int linksSize = m_linkStorage.size();
            int i = 0;
            Iterator<Entry<String, String>> itEntries = m_linkStorage.entrySet().iterator();
            // loop through all links to convert
            while (itEntries.hasNext()) {
                Entry<String, String> entry = itEntries.next();

                String key = entry.getKey();
                String link = entry.getValue();
                List<CmsProperty> properties = m_linkPropertyStorage.get(key);
                CmsProperty.setAutoCreatePropertyDefinitions(properties, true);

                i++;
                m_report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_SUCCESSION_2,
                        String.valueOf(i),
                        String.valueOf(linksSize)),
                    I_CmsReport.FORMAT_NOTE);
                m_report.print(Messages.get().container(Messages.RPT_CONVERT_LINK_0), I_CmsReport.FORMAT_NOTE);
                m_report.print(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, key + " "));
                m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                try {
                    // check if this is an internal pointer
                    if (link.startsWith("/")) {
                        // check if the pointer target is existing
                        CmsResource target = m_cms.readResource(link);

                        // create a new sibling as CmsResource
                        CmsResource resource = new CmsResource(
                            new CmsUUID(), // structure ID is always a new UUID
                            target.getResourceId(),
                            key,
                            target.getTypeId(),
                            target.isFolder(),
                            0,
                            m_cms.getRequestContext().getCurrentProject().getUuid(), // TODO: pass flags from import
                            CmsResource.STATE_NEW,
                            target.getDateCreated(),
                            target.getUserCreated(),
                            target.getDateLastModified(),
                            target.getUserLastModified(),
                            CmsResource.DATE_RELEASED_DEFAULT,
                            CmsResource.DATE_EXPIRED_DEFAULT,
                            1,
                            0,
                            target.getDateContent(),
                            0);

                        m_cms.importResource(key, resource, null, properties);
                        m_report.println(
                            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                            I_CmsReport.FORMAT_OK);

                        if (LOG.isInfoEnabled()) {
                            LOG.info(
                                Messages.get().getBundle().key(
                                    Messages.LOG_CONVERT_LINK_DOTS_OK_3,
                                    String.valueOf(i),
                                    String.valueOf(linksSize),
                                    key));
                        }
                    } else {
                        int pointerId = OpenCms.getResourceManager().getResourceType(
                            CmsResourceTypePointer.getStaticTypeName()).getTypeId();
                        m_cms.createResource(key, pointerId, link.getBytes(), properties);
                        m_report.println(
                            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                            I_CmsReport.FORMAT_OK);

                        if (LOG.isInfoEnabled()) {
                            LOG.info(
                                Messages.get().getBundle().key(
                                    Messages.LOG_CONVERT_LINK_OK_3,
                                    String.valueOf(i),
                                    String.valueOf(linksSize),
                                    key));
                        }
                    }
                } catch (CmsException e) {
                    m_report.println();
                    m_report.print(
                        Messages.get().container(Messages.RPT_CONVERT_LINK_NOTFOUND_1, link),
                        I_CmsReport.FORMAT_WARNING);

                    if (LOG.isErrorEnabled()) {
                        LOG.error(
                            Messages.get().getBundle().key(
                                Messages.ERR_IMPORTEXPORT_LINK_CONVERSION_FAILED_2,
                                key,
                                link),
                            e);
                    }
                }
            }
        } finally {
            if (m_linkStorage != null) {
                m_linkStorage.clear();
            }
            m_linkStorage = null;

            if (m_linkPropertyStorage != null) {
                m_linkPropertyStorage.clear();
            }
            m_linkPropertyStorage = null;
        }
    }

    /**
     * Returns a byte array containing the content of the file.<p>
     *
     * @param filename the name of the file to read
     * @return a byte array containing the content of the file
     */
    protected byte[] getFileBytes(String filename) {

        try {
            // is this a zip-file?
            if (m_importZip != null) {
                // yes
                ZipEntry entry = m_importZip.getEntry(filename);

                // path to file might be relative, too
                if ((entry == null) && filename.startsWith("/")) {
                    entry = m_importZip.getEntry(filename.substring(1));
                }
                if (entry == null) {
                    throw new ZipException(
                        Messages.get().getBundle().key(Messages.LOG_IMPORTEXPORT_FILE_NOT_FOUND_IN_ZIP_1, filename));
                }

                InputStream stream = m_importZip.getInputStream(entry);
                int size = Long.valueOf(entry.getSize()).intValue();
                return CmsFileUtil.readFully(stream, size);
            } else {
                // no - use directory
                File file = new File(m_importResource, filename);
                return CmsFileUtil.readFile(file);
            }
        } catch (FileNotFoundException fnfe) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_IMPORTEXPORT_FILE_NOT_FOUND_1, filename), fnfe);
            }
            m_report.println(fnfe);
        } catch (IOException ioe) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle().key(Messages.ERR_IMPORTEXPORT_ERROR_READING_FILE_1, filename),
                    ioe);
            }
            m_report.println(ioe);
        }
        // this will only be returned in case there was an exception
        return "".getBytes();
    }

    /**
     * Creates a new access control entry and stores it for later write out.
     *
     * @param res the resource
     * @param id the id of the principal
     * @param allowed the allowed permissions
     * @param denied the denied permissions
     * @param flags the flags
     *
     * @return the created ACE
     */
    protected CmsAccessControlEntry getImportAccessControlEntry(
        CmsResource res,
        String id,
        String allowed,
        String denied,
        String flags) {

        return new CmsAccessControlEntry(
            res.getResourceId(),
            new CmsUUID(id),
            Integer.parseInt(allowed),
            Integer.parseInt(denied),
            Integer.parseInt(flags));
    }

    /**
     * Returns the appropriate locale for the given destination.<p>
     *
     * @param destination the destination path (parent must exist)
     * @param properties the properties to check at first
     *
     * @return the locale
     */
    protected Locale getLocale(String destination, List<CmsProperty> properties) {

        String localeName = CmsProperty.get(CmsPropertyDefinition.PROPERTY_LOCALE, properties).getValue();

        if (localeName != null) {
            // locale was already set on the files properties
            return OpenCms.getLocaleManager().getAvailableLocales(localeName).get(0);
        }
        // locale not set in properties, read default locales
        return OpenCms.getLocaleManager().getDefaultLocales(m_cms, CmsResource.getParentFolder(destination)).get(0);
    }

    /**
     * Writes already imported access control entries for a given resource.<p>
     *
     * @param resource the resource assigned to the access control entries
     * @param aceList the access control entries to create
     */
    protected void importAccessControlEntries(CmsResource resource, List<CmsAccessControlEntry> aceList) {

        if (aceList.size() == 0) {
            // no ACE in the list
            return;
        }
        try {
            m_cms.importAccessControlEntries(resource, aceList);
        } catch (CmsException exc) {
            m_report.println(
                Messages.get().container(Messages.RPT_IMPORT_ACL_DATA_FAILED_0),
                I_CmsReport.FORMAT_WARNING);
        }
    }

    /**
     * Imports a single group.<p>
     *
     * @param name the name of the group
     * @param description group description
     * @param flags group flags
     * @param parentgroupName name of the parent group
     *
     * @throws CmsImportExportException if something goes wrong
     */
    protected void importGroup(String name, String description, String flags, String parentgroupName)
    throws CmsImportExportException {

        if (description == null) {
            description = "";
        }

        CmsGroup parentGroup = null;
        try {
            if (CmsStringUtil.isNotEmpty(parentgroupName)) {
                try {
                    parentGroup = m_cms.readGroup(parentgroupName);
                } catch (CmsException exc) {
                    // parentGroup will be null
                }
            }

            if (CmsStringUtil.isNotEmpty(parentgroupName) && (parentGroup == null)) {
                // cannot create group, put on stack and try to create later
                Map<String, String> groupData = new HashMap<String, String>();
                groupData.put(A_CmsImport.N_NAME, name);
                groupData.put(A_CmsImport.N_DESCRIPTION, description);
                groupData.put(A_CmsImport.N_FLAGS, flags);
                groupData.put(A_CmsImport.N_PARENTGROUP, parentgroupName);
                m_groupsToCreate.push(groupData);
            } else {
                try {
                    m_report.print(Messages.get().container(Messages.RPT_IMPORT_GROUP_0), I_CmsReport.FORMAT_NOTE);
                    m_report.print(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, name));
                    m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                    m_cms.createGroup(name, description, Integer.parseInt(flags), parentgroupName);
                    m_report.println(
                        org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                        I_CmsReport.FORMAT_OK);
                } catch (CmsException exc) {

                    m_report.println(Messages.get().container(Messages.RPT_NOT_CREATED_0), I_CmsReport.FORMAT_OK);
                }
            }

        } catch (Exception e) {

            m_report.println(e);

            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_IMPORTING_GROUP_1,
                name);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }
            throw new CmsImportExportException(message, e);
        }
    }

    /**
     * Imports the OpenCms groups.<p>
     *
     * @throws CmsImportExportException if something goes wrong
     */
    protected void importGroups() throws CmsImportExportException {

        List<Node> groupNodes;
        Element currentElement;
        String name, description, flags, parentgroup;
        try {
            // getAll group nodes
            groupNodes = m_docXml.selectNodes("//" + A_CmsImport.N_GROUPDATA);
            // walk through all groups in manifest
            for (int i = 0; i < groupNodes.size(); i++) {
                currentElement = (Element)groupNodes.get(i);
                name = getChildElementTextValue(currentElement, A_CmsImport.N_NAME);
                name = OpenCms.getImportExportManager().translateGroup(name);
                description = getChildElementTextValue(currentElement, A_CmsImport.N_DESCRIPTION);
                flags = getChildElementTextValue(currentElement, A_CmsImport.N_FLAGS);
                parentgroup = getChildElementTextValue(currentElement, A_CmsImport.N_PARENTGROUP);
                if ((parentgroup != null) && (parentgroup.length() > 0)) {
                    parentgroup = OpenCms.getImportExportManager().translateGroup(parentgroup);
                }
                // import this group

                importGroup(name, description, flags, parentgroup);
            }

            // now try to import the groups in the stack
            while (!m_groupsToCreate.empty()) {
                Stack<Map<String, String>> tempStack = m_groupsToCreate;
                m_groupsToCreate = new Stack<Map<String, String>>();
                while (tempStack.size() > 0) {
                    Map<String, String> groupdata = tempStack.pop();
                    name = groupdata.get(A_CmsImport.N_NAME);
                    description = groupdata.get(A_CmsImport.N_DESCRIPTION);
                    flags = groupdata.get(A_CmsImport.N_FLAGS);
                    parentgroup = groupdata.get(A_CmsImport.N_PARENTGROUP);
                    // try to import the group
                    importGroup(name, description, flags, parentgroup);
                }
            }
        } catch (CmsImportExportException e) {

            throw e;
        } catch (Exception e) {

            m_report.println(e);

            CmsMessageContainer message = Messages.get().container(Messages.ERR_IMPORTEXPORT_ERROR_IMPORTING_GROUPS_0);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }

            throw new CmsImportExportException(message, e);
        }
    }

    /**
     * Imports a single user.<p>
     *
     * @param name user name
     * @param flags user flags
     * @param password user password
     * @param firstname firstname of the user
     * @param lastname lastname of the user
     * @param email user email
     * @param dateCreated creation date
     * @param userInfo user info
     * @param userGroups user groups
     *
     * @throws CmsImportExportException in case something goes wrong
     */
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

        // create a new user id
        String id = new CmsUUID().toString();
        try {
            try {
                m_report.print(Messages.get().container(Messages.RPT_IMPORT_USER_0), I_CmsReport.FORMAT_NOTE);
                m_report.print(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, name));
                m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                m_cms.importUser(
                    id,
                    name,
                    password,
                    firstname,
                    lastname,
                    email,
                    Integer.parseInt(flags),
                    dateCreated,
                    userInfo);
                // add user to all groups list
                for (int i = 0; i < userGroups.size(); i++) {
                    String groupName = userGroups.get(i);
                    try {
                        CmsGroup group = m_cms.readGroup(groupName);
                        if (group.isVirtual() || group.isRole()) {
                            CmsRole role = CmsRole.valueOf(group);
                            OpenCms.getRoleManager().addUserToRole(m_cms, role, name);
                        } else {
                            m_cms.addUserToGroup(name, groupName);
                        }
                    } catch (CmsException exc) {
                        m_report.println(
                            Messages.get().container(Messages.RPT_USER_COULDNT_BE_ADDED_TO_GROUP_2, name, groupName),
                            I_CmsReport.FORMAT_WARNING);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(exc.getLocalizedMessage(), exc);
                        }
                    }
                }
                m_report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);
            } catch (CmsException exc) {
                m_report.println(Messages.get().container(Messages.RPT_NOT_CREATED_0), I_CmsReport.FORMAT_OK);
            }
        } catch (Exception e) {

            m_report.println(e);

            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_IMPORTING_USER_1,
                name);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }
            throw new CmsImportExportException(message, e);
        }
    }

    /**
     * Imports the OpenCms users.<p>
     *
     * @throws CmsImportExportException if something goes wrong
     */
    @SuppressWarnings("unchecked")
    protected void importUsers() throws CmsImportExportException {

        List<Node> userNodes;
        List<Node> groupNodes;
        List<String> userGroups;
        Element currentElement, currentGroup;
        Map<String, Object> userInfo = null;
        String name, description, flags, password, firstname, lastname, email, address, pwd, infoNode, defaultGroup;
        // try to get the import resource
        //getImportResource();
        try {
            // getAll user nodes
            userNodes = m_docXml.selectNodes("//" + A_CmsImport.N_USERDATA);
            // walk threw all groups in manifest
            for (int i = 0; i < userNodes.size(); i++) {
                currentElement = (Element)userNodes.get(i);
                name = getChildElementTextValue(currentElement, A_CmsImport.N_NAME);
                name = OpenCms.getImportExportManager().translateUser(name);
                // decode passwords using base 64 decoder
                pwd = getChildElementTextValue(currentElement, A_CmsImport.N_PASSWORD);
                password = new String(Base64.decodeBase64(pwd.trim().getBytes()));
                description = getChildElementTextValue(currentElement, A_CmsImport.N_DESCRIPTION);
                flags = getChildElementTextValue(currentElement, A_CmsImport.N_FLAGS);
                firstname = getChildElementTextValue(currentElement, A_CmsImport.N_FIRSTNAME);
                lastname = getChildElementTextValue(currentElement, A_CmsImport.N_LASTNAME);
                email = getChildElementTextValue(currentElement, A_CmsImport.N_EMAIL);
                address = getChildElementTextValue(currentElement, A_CmsImport.N_TAG_ADDRESS);
                defaultGroup = getChildElementTextValue(currentElement, A_CmsImport.N_DEFAULTGROUP);
                // get the userinfo and put it into the additional info map
                infoNode = getChildElementTextValue(currentElement, A_CmsImport.N_USERINFO);
                try {
                    // read the userinfo from the dat-file
                    byte[] value = getFileBytes(infoNode);
                    // deserialize the object
                    ByteArrayInputStream bin = new ByteArrayInputStream(value);
                    ObjectInputStream oin = new ObjectInputStream(bin);
                    userInfo = (Map<String, Object>)oin.readObject();
                } catch (IOException ioex) {
                    m_report.println(ioex);
                } catch (ClassCastException ccex) {
                    m_report.println(ccex);
                } catch (ClassNotFoundException cnfex) {
                    m_report.println(cnfex);
                }
                // in case the user info could not be parsed create a new map
                if (userInfo == null) {
                    userInfo = new HashMap<String, Object>();
                }
                // get the groups of the user and put them into the list
                groupNodes = currentElement.selectNodes("*/" + A_CmsImport.N_GROUPNAME);
                userGroups = new ArrayList<String>();
                for (int j = 0; j < groupNodes.size(); j++) {
                    currentGroup = (Element)groupNodes.get(j);
                    String userInGroup = getChildElementTextValue(currentGroup, A_CmsImport.N_NAME);
                    userInGroup = OpenCms.getImportExportManager().translateGroup(userInGroup);
                    userGroups.add(userInGroup);
                }

                if (CmsStringUtil.isNotEmpty(defaultGroup)) {
                    userInfo.put(CmsUserSettings.ADDITIONAL_INFO_DEFAULTGROUP, defaultGroup);
                }

                if (description != null) {
                    userInfo.put(CmsUserSettings.ADDITIONAL_INFO_DESCRIPTION, description);
                }
                if (address != null) {
                    userInfo.put(CmsUserSettings.ADDITIONAL_INFO_ADDRESS, address);
                }
                // import this user
                importUser(name, flags, password, firstname, lastname, email, 0, userInfo, userGroups);
            }
        } catch (CmsImportExportException e) {
            throw e;
        } catch (Exception e) {
            m_report.println(e);
            CmsMessageContainer message = Messages.get().container(Messages.ERR_IMPORTEXPORT_ERROR_IMPORTING_USERS_0);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }
            throw new CmsImportExportException(message, e);
        }
    }

    /**
     * Initializes all member variables before the import is started.<p>
     *
     * This is required since there is only one instance for
     * each import version that is kept in memory and reused.<p>
     */
    protected void initialize() {

        m_groupsToCreate = new Stack<Map<String, String>>();
    }

    /**
     * Reads all properties below a specified parent element from the <code>manifest.xml</code>.<p>
     *
     * @param parentElement the current file node
     * @param ignoredPropertyKeys a list of properties to be ignored
     *
     * @return a list with all properties
     */
    protected List<CmsProperty> readPropertiesFromManifest(Element parentElement, List<String> ignoredPropertyKeys) {

        // all imported Cms property objects are collected in map first forfaster access
        Map<String, CmsProperty> properties = new HashMap<String, CmsProperty>();
        CmsProperty property = null;
        List<Node> propertyElements = parentElement.selectNodes(
            "./" + A_CmsImport.N_PROPERTIES + "/" + A_CmsImport.N_PROPERTY);
        Element propertyElement = null;
        String key = null, value = null;
        Attribute attrib = null;

        // iterate over all property elements
        for (int i = 0, n = propertyElements.size(); i < n; i++) {
            propertyElement = (Element)propertyElements.get(i);
            key = getChildElementTextValue(propertyElement, A_CmsImport.N_NAME);

            if ((key == null) || ignoredPropertyKeys.contains(key)) {
                // continue if the current property (key) should be ignored or is null
                continue;
            }

            // all Cms properties are collected in a map keyed by their property keys
            property = properties.get(key);
            if (property == null) {
                property = new CmsProperty();
                property.setName(key);
                property.setAutoCreatePropertyDefinition(true);
                properties.put(key, property);
            }

            value = getChildElementTextValue(propertyElement, A_CmsImport.N_VALUE);
            if (value == null) {
                value = "";
            }

            attrib = propertyElement.attribute(A_CmsImport.N_PROPERTY_ATTRIB_TYPE);
            if ((attrib != null) && attrib.getValue().equals(A_CmsImport.N_PROPERTY_ATTRIB_TYPE_SHARED)) {
                // it is a shared/resource property value
                property.setResourceValue(value);
            } else {
                // it is an individual/structure value
                property.setStructureValue(value);
            }
        }

        return new ArrayList<CmsProperty>(properties.values());
    }
}

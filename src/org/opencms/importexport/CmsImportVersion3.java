/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/CmsImportVersion3.java,v $
 * Date   : $Date: 2004/02/13 13:41:44 $
 * Version: $Revision: 1.23 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.page.CmsXmlPage;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsUUID;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceTypeFolder;
import org.opencms.file.CmsResourceTypePage;
import org.opencms.file.CmsResourceTypeXmlPage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import java.util.zip.ZipFile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Implementation of the OpenCms Import Interface ({@link org.opencms.importexport.I_CmsImport}) for 
 * the import version 3. <p>
 * 
 * This import format was used in OpenCms 5.1.2 - 5.1.6.
 * 
 * @see org.opencms.importexport.A_CmsImport
 *
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 */
public class CmsImportVersion3 extends A_CmsImport {
    
    /** The version number of this import implementation.<p> */
    private static final int C_IMPORT_VERSION = 3;    

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
        return CmsImportVersion3.C_IMPORT_VERSION;
    }
    

    /**
     * Imports the resources for a module.<p>
     * @param cms the current cms object
     * @param importPath the path in the cms VFS to import into
     * @param report a report object to output the progress information to
     * @param digest digest for taking a fingerprint of the files
     * @param importResource  the import-resource (folder) to load resources from
     * @param importZip the import-resource (zip) to load resources from
     * @param docXml the xml manifest-file 
     * @param excludeList filenames of files and folders which should not 
     *      be (over)written in the virtual file system (not used when null)
     * @param writtenFilenames filenames of the files and folder which have actually been 
     *      successfully written (not used when null)
     * @param fileCodes code of the written files (for the registry)
     *      (not used when null)
     * @param propertyName name of a property to be added to all resources
     * @param propertyValue value of that property
     * @throws CmsException if something goes wrong
     */
    public synchronized void importResources(CmsObject cms, String importPath, I_CmsReport report, MessageDigest digest, File importResource, ZipFile importZip, Document docXml, Vector excludeList, Vector writtenFilenames, Vector fileCodes, String propertyName, String propertyValue) throws CmsException {
        // initialize the import        
        m_cms = cms;
        m_importPath = importPath;
        m_report = report;
        m_digest = digest;
        m_importResource = importResource;
        m_importZip = importZip;
        m_docXml = docXml;
        m_importingChannelData = false;
        try {
            // first import the user information
            if (m_cms.isAdmin()) {
                importGroups();
                importUsers();
            }
            // now import the VFS resources
            importAllResources(excludeList, writtenFilenames, fileCodes, propertyName, propertyValue);
        } catch (CmsException e) {
            throw e;
        }
    }

    /**
     * Imports the groups and writes them to the cms.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    private void importGroups() throws CmsException {
        NodeList groupNodes;
        Element currentElement;
        String id, name, description, flags, parentgroup;
        try {
            // getAll group nodes
            groupNodes = m_docXml.getElementsByTagName(I_CmsConstants.C_EXPORT_TAG_GROUPDATA);
            // walk through all groups in manifest
            for (int i = 0; i < groupNodes.getLength(); i++) {
                currentElement = (Element)groupNodes.item(i);
                id = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_ID);
                name = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_NAME);
                description = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_DESCRIPTION);
                flags = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_FLAGS);
                parentgroup = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_PARENTGROUP);
                // import this group
                importGroup(id, name, description, flags, parentgroup);
            }

            // now try to import the groups in the stack
            while (!m_groupsToCreate.empty()) {
                Stack tempStack = m_groupsToCreate;
                m_groupsToCreate = new Stack();
                while (tempStack.size() > 0) {
                    Hashtable groupdata = (Hashtable)tempStack.pop();
                    id = (String)groupdata.get(I_CmsConstants.C_EXPORT_TAG_ID);
                    name = (String)groupdata.get(I_CmsConstants.C_EXPORT_TAG_NAME);
                    description = (String)groupdata.get(I_CmsConstants.C_EXPORT_TAG_DESCRIPTION);
                    flags = (String)groupdata.get(I_CmsConstants.C_EXPORT_TAG_FLAGS);
                    parentgroup = (String)groupdata.get(I_CmsConstants.C_EXPORT_TAG_PARENTGROUP);
                    // try to import the group
                    importGroup(id, name, description, flags, parentgroup);
                }
            }
        } catch (Exception exc) {
            m_report.println(exc);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
      * Imports the users and writes them to the cms.<p>
      * 
      * @throws CmsException if something goes wrong
      */
    private void importUsers() throws CmsException {
        NodeList userNodes;
        NodeList groupNodes;
        Element currentElement, currentGroup;
        Vector userGroups;
        Hashtable userInfo = new Hashtable();
        sun.misc.BASE64Decoder dec;
        String id, name, description, flags, password, recoveryPassword, firstname, lastname, email, address, section, defaultGroup, type, pwd, infoNode;
        // try to get the import resource
        //getImportResource();
        try {
            // getAll user nodes
            userNodes = m_docXml.getElementsByTagName(I_CmsConstants.C_EXPORT_TAG_USERDATA);
            // walk threw all groups in manifest
            for (int i = 0; i < userNodes.getLength(); i++) {
                currentElement = (Element)userNodes.item(i);
                id = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_ID);
                name = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_NAME);
                // decode passwords using base 64 decoder
                dec = new sun.misc.BASE64Decoder();
                pwd = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_PASSWORD);
                password = new String(dec.decodeBuffer(pwd.trim()));
                dec = new sun.misc.BASE64Decoder();
                pwd = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_RECOVERYPASSWORD);
                recoveryPassword = new String(dec.decodeBuffer(pwd.trim()));

                description = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_DESCRIPTION);
                flags = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_FLAGS);
                firstname = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_FIRSTNAME);
                lastname = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_LASTNAME);
                email = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_EMAIL);
                address = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_ADDRESS);
                section = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_SECTION);
                defaultGroup = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_DEFAULTGROUP);
                type = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_TYPE);
                // get the userinfo and put it into the hashtable
                infoNode = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_USERINFO);
                try {
                    // read the userinfo from the dat-file
                    byte[] value = getFileBytes(infoNode);
                    // deserialize the object
                    ByteArrayInputStream bin = new ByteArrayInputStream(value);
                    ObjectInputStream oin = new ObjectInputStream(bin);
                    userInfo = (Hashtable)oin.readObject();
                } catch (IOException ioex) {
                    m_report.println(ioex);
                }

                // get the groups of the user and put them into the vector
                groupNodes = currentElement.getElementsByTagName(I_CmsConstants.C_EXPORT_TAG_GROUPNAME);
                userGroups = new Vector();
                for (int j = 0; j < groupNodes.getLength(); j++) {
                    currentGroup = (Element)groupNodes.item(j);
                    userGroups.addElement(getTextNodeValue(currentGroup, I_CmsConstants.C_EXPORT_TAG_NAME));
                }
                // import this user
                importUser(id, name, description, flags, password, recoveryPassword, firstname, lastname, email, address, section, defaultGroup, type, userInfo, userGroups);
            }
        } catch (Exception exc) {
            m_report.println(exc);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Imports the resources and writes them to the cms.<p>
     * 
     * 
     * @param excludeList filenames of files and folders which should not 
     *      be (over)written in the virtual file system (not used when null)
     * @param writtenFilenames filenames of the files and folder which have actually been 
     *      successfully written (not used when null)
     * @param fileCodes code of the written files (for the registry)
     *      (not used when null)
     * @param propertyName name of a property to be added to all resources
     * @param propertyValue value of that property
     * @throws CmsException if something goes wrong
     */
    private void importAllResources(Vector excludeList, Vector writtenFilenames, Vector fileCodes, String propertyName, String propertyValue) throws CmsException {

        String source, destination, type, uuidstructure, uuidresource, uuidcontent, userlastmodified, usercreated, flags, timestamp;
        long datelastmodified, datecreated;
        int resType;
        
        NodeList fileNodes, acentryNodes;
        Element currentElement, currentEntry;
        Map properties = null;

        if (m_importingChannelData) {
            m_cms.getRequestContext().saveSiteRoot();
            m_cms.setContextToCos();
        }

        // clear some required structures at the init phase of the import      
        if (excludeList == null) {
            excludeList = new Vector();
        }
        // get list of unwanted properties
        List deleteProperties = OpenCms.getImportExportManager().getIgnoredProperties();
        if (deleteProperties == null) {
            deleteProperties = new ArrayList();
        }
        // get list of immutable resources
        List immutableResources = OpenCms.getImportExportManager().getImmutableResources();
        if (immutableResources == null) {
            immutableResources = new ArrayList();
        }
        // get the wanted page type for imported pages
        m_convertToXmlPage = OpenCms.getImportExportManager().convertToXmlPage();       
        
        try {
            // get all file-nodes
            fileNodes = m_docXml.getElementsByTagName(I_CmsConstants.C_EXPORT_TAG_FILE);
            int importSize = fileNodes.getLength();
            // walk through all files in manifest
            for (int i = 0; i < fileNodes.getLength(); i++) {
                m_report.print(" ( " + (i + 1) + " / " + importSize + " ) ");
                currentElement = (Element)fileNodes.item(i);
                // get all information for a file-import
                // <source>
                source = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_SOURCE);
                // <destintion>
                destination = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_DESTINATION);
                // <type>
                type = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_TYPE);
                if (C_RESOURCE_TYPE_NEWPAGE_NAME.equals(type)) {
                    resType = C_RESOURCE_TYPE_NEWPAGE_ID;
                } else {
                    resType = m_cms.getResourceTypeId(type);
                }
                // <uuidstructure>
                uuidstructure = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_UUIDSTRUCTURE);
                // <uuidresource>
                uuidresource = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_UUIDRESOURCE);
                // <uuidcontent>
                uuidcontent = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_UUIDCONTENT);
                // <datelastmodified>
                if ((timestamp = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_DATELASTMODIFIED)) != null) {
                    datelastmodified = Long.parseLong(timestamp);
                } else {
                    datelastmodified = System.currentTimeMillis();
                }
                // <userlastmodified>
                userlastmodified = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_USERLASTMODIFIED);
                // <datecreated>
                if ((timestamp = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_DATECREATED)) != null) {
                    datecreated = Long.parseLong(timestamp);
                } else {
                    datecreated = System.currentTimeMillis();
                }
                // <usercreated>
                usercreated = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_USERCREATED);
                // <flags>              
                flags = getTextNodeValue(currentElement, I_CmsConstants.C_EXPORT_TAG_FLAGS);

                String translatedName = m_cms.getRequestContext().addSiteRoot(m_importPath + destination);
                if (CmsResourceTypeFolder.C_RESOURCE_TYPE_NAME.equals(type)) {
                    translatedName += I_CmsConstants.C_FOLDER_SEPARATOR;
                }
                // translate the name during import
                translatedName = m_cms.getRequestContext().getDirectoryTranslator().translateResource(translatedName);
                // check if this resource is immutable
                boolean resourceNotImmutable = checkImmutable(translatedName, immutableResources);
                translatedName = m_cms.getRequestContext().removeSiteRoot(translatedName);
                // if the resource is not immutable and not on the exclude list, import it
                if (resourceNotImmutable && (!excludeList.contains(translatedName))) {
                    // print out the information to the report
                    m_report.print(m_report.key("report.importing"), I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(translatedName);
                    m_report.print(m_report.key("report.dots"));
                    // get all properties
                    properties = getPropertiesFromXml(currentElement, resType, propertyName, propertyValue, deleteProperties);
                    // import the resource               

                    CmsResource res = importResource(source, destination, type, uuidstructure, uuidresource, uuidcontent, datelastmodified, userlastmodified, datecreated, usercreated, flags, properties, writtenFilenames, fileCodes);

                    // if the resource was imported add the access control entrys if available
                    if (res != null) {
                        // write all imported access control entries for this file
                        acentryNodes = currentElement.getElementsByTagName(I_CmsConstants.C_EXPORT_TAG_ACCESSCONTROL_ENTRY);
                        // collect all access control entries
                        for (int j = 0; j < acentryNodes.getLength(); j++) {
                            currentEntry = (Element)acentryNodes.item(j);
                            // get the data of the access control entry
                            String id = getTextNodeValue(currentEntry, I_CmsConstants.C_EXPORT_TAG_ACCESSCONTROL_PRINCIPAL);
                            String acflags = getTextNodeValue(currentEntry, I_CmsConstants.C_EXPORT_TAG_FLAGS);
                            String allowed = getTextNodeValue(currentEntry, I_CmsConstants.C_EXPORT_TAG_ACCESSCONTROL_ALLOWEDPERMISSIONS);
                            String denied = getTextNodeValue(currentEntry, I_CmsConstants.C_EXPORT_TAG_ACCESSCONTROL_DENIEDPERMISSIONS);
                            // add the entry to the list
                            addImportAccessControlEntry(res, id, allowed, denied, acflags);
                        }
                        importAccessControlEntries(res);
                    } else {
                        // resource import failed, since no CmsResource was created
                        m_report.print(m_report.key("report.skipping"), I_CmsReport.C_FORMAT_NOTE);
                        m_report.println(translatedName);
                    }
                } else {
                    // skip the file import, just print out the information to the report
                    m_report.print(m_report.key("report.skipping"), I_CmsReport.C_FORMAT_NOTE);
                    m_report.println(translatedName);
                }
            }
//            if (!m_importingChannelData) {
//                // at last we have to get the links from all new imported pages for the  linkmanagement
//                m_report.println(m_report.key("report.check_links_begin"), I_CmsReport.C_FORMAT_HEADLINE);
//                updatePageLinks();
//                m_report.println(m_report.key("report.check_links_end"), I_CmsReport.C_FORMAT_HEADLINE);
//            }

        } catch (Exception exc) {
            m_report.println(exc);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            if (m_importingChannelData) {
                m_cms.getRequestContext().restoreSiteRoot();
            }
        }

    }

    /**
      * Imports a resource (file or folder) into the cms.<p>
      * 
      * @param source the path to the source-file
      * @param destination the path to the destination-file in the cms
      * @param type the resource-type of the file
      * @param uuidstructure  the structure uuid of the resource
      * @param uuidresource  the resource uuid of the resource
      * @param uuidcontent the file uuid of the resource
      * @param datelastmodified the last modification date of the resource
      * @param userlastmodified the user who made the last modifications to the resource
      * @param datecreated the creation date of the resource
      * @param usercreated the user who created 
      * @param flags the flags of the resource     
      * @param properties a hashtable with properties for this resource
      * @param writtenFilenames filenames of the files and folder which have actually been successfully written
      *       not used when null
      * @param fileCodes code of the written files (for the registry)
      *       not used when null
      * @return imported resource
      */
    private CmsResource importResource(String source, String destination, String type, String uuidstructure, String uuidresource, String uuidcontent, long datelastmodified, String userlastmodified, long datecreated, String usercreated, String flags, Map properties, Vector writtenFilenames, Vector fileCodes) {

        boolean success = true;
        byte[] content = null;
        String fullname = null;
        CmsResource res = null;

        try {
            if (m_importingChannelData) {
                // try to read an existing channel to get the channel id
                String channelId = null;
                try {
                    if ((type.equalsIgnoreCase(CmsResourceTypeFolder.C_RESOURCE_TYPE_NAME)) && (!destination.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR))) {
                        destination += I_CmsConstants.C_FOLDER_SEPARATOR;
                    }
                    CmsResource channel = m_cms.readFileHeader(I_CmsConstants.C_ROOT + destination);
                    channelId = m_cms.readProperty(m_cms.readAbsolutePath(channel), I_CmsConstants.C_PROPERTY_CHANNELID);
                } catch (Exception e) {
                    // ignore the exception, a new channel id will be generated
                }
                if (channelId == null) {
                    // the channel id does not exist, so generate a new one
                    int newChannelId = org.opencms.db.CmsDbUtil.nextId(I_CmsConstants.C_TABLE_CHANNELID);
                    channelId = "" + newChannelId;
                }
                properties.put(I_CmsConstants.C_PROPERTY_CHANNELID, channelId);
            }
            // get the file content
            if (source != null) {
                content = getFileBytes(source);
            }
            // get all required information to create a CmsResource
            int resType = m_cms.getResourceTypeId(type);
            int size = 0;
            if (content != null) {
                size = content.length;
            }
            // get the required UUIDs         
            CmsUUID curUser = m_cms.getRequestContext().currentUser().getId();
            CmsUUID newUserlastmodified = new CmsUUID(userlastmodified);
            CmsUUID newUsercreated = new CmsUUID(usercreated);
            // check if user created and user lastmodified are valid users in this system.
            // if not, set them to the current.
            if (m_cms.readUser(newUserlastmodified).getId().equals(CmsUUID.getNullUUID())) {
                newUserlastmodified = curUser;
            }
            if (m_cms.readUser(newUsercreated).getId().equals(CmsUUID.getNullUUID())) {
                newUsercreated = curUser;
            }
            // get all UUIDs for the structure, resource and content        
            CmsUUID newUuidstructure = new CmsUUID();
            CmsUUID newUuidcontent = new CmsUUID();
            CmsUUID newUuidresource = new CmsUUID();
            if (uuidstructure != null) {
                newUuidstructure = new CmsUUID(uuidstructure);
            }
            if (uuidcontent != null) {
                newUuidcontent = new CmsUUID(uuidcontent);
            }
            if (uuidresource != null) {
                newUuidresource = new CmsUUID(uuidresource);
            }
            // extract the name of the resource form the destination
            String resname = destination;
            if (resname.endsWith("/")) {
                resname = resname.substring(0, resname.length() - 1);
            }
            if (resname.lastIndexOf("/") > 0) {
                resname = resname.substring(resname.lastIndexOf("/") + 1, resname.length());
            }

            // convert to xml page if wanted
            if (m_convertToXmlPage 
                && (resType == CmsResourceTypePage.C_RESOURCE_TYPE_ID || resType == C_RESOURCE_TYPE_NEWPAGE_ID)) {
                
                if (content != null) {
                    CmsXmlPage xmlPage = CmsXmlPageConverter.convertToXmlPage(m_cms, new String(content), "body", getLocale(destination, properties));
                    ByteArrayOutputStream pageContent = new ByteArrayOutputStream();
                    xmlPage.write(pageContent, OpenCms.getSystemInfo().getDefaultEncoding());    
                    content = pageContent.toByteArray();
                }
                resType = CmsResourceTypeXmlPage.C_RESOURCE_TYPE_ID;
            }
            
            // create a new CmsResource                         
            CmsResource resource = new CmsResource(newUuidstructure, newUuidresource, CmsUUID.getNullUUID(), newUuidcontent, resname, resType, new Integer(flags).intValue(), m_cms.getRequestContext().currentProject().getId(), I_CmsConstants.C_STATE_NEW, m_cms.getResourceType(resType).getLoaderId(), datelastmodified, newUserlastmodified, datecreated, newUsercreated, size, 1);
            // import this resource in the VFS   

            res = m_cms.importResource(resource, content, properties, m_importPath + destination);

            if (res != null) {
                if (CmsResourceTypePage.C_RESOURCE_TYPE_NAME.equals(type)) {
                    m_importedPages.add(I_CmsConstants.C_FOLDER_SEPARATOR + destination);
                }
            }
            m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
        } catch (Exception exc) {
            // an error while importing the file
            success = false;
            m_report.println(exc);
            try {
                // Sleep some time after an error so that the report output has a chance to keep up
                Thread.sleep(1000);
            } catch (Exception e) {
                // 
            }
        }

        byte[] digestContent = {0 };
        if (content != null) {
            digestContent = m_digest.digest(content);
        }
        if (success && (fullname != null)) {
            if (writtenFilenames != null) {
                writtenFilenames.addElement(fullname);
            }
            if (fileCodes != null) {
                fileCodes.addElement(new String(digestContent));
            }
        }

        return res;
    }

    }

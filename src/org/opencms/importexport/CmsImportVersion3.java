/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/CmsImportVersion3.java,v $
 * Date   : $Date: 2005/03/15 18:05:54 $
 * Version: $Revision: 1.54 $
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

import org.opencms.file.CmsFolder;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.security.I_CmsPasswordHandler;
import org.opencms.util.CmsUUID;
import org.opencms.xml.page.CmsXmlPage;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.zip.ZipFile;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Implementation of the OpenCms Import Interface ({@link org.opencms.importexport.I_CmsImport}) for 
 * the import version 3.<p>
 * 
 * This import format was used in OpenCms 5.1.2 - 5.1.6.<p>
 *
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * 
 * @see org.opencms.importexport.A_CmsImport
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
        initialize();
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
        } finally {
            cleanUp();
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

        String source, destination, type, uuidstructure, uuidresource, userlastmodified, usercreated, flags, timestamp;
        long datelastmodified, datecreated;

        List fileNodes, acentryNodes;
        Element currentElement, currentEntry;
        List properties = null;

        if (m_importingChannelData) {
            m_cms.getRequestContext().saveSiteRoot();
            m_cms.getRequestContext().setSiteRoot(I_CmsConstants.VFS_FOLDER_CHANNELS);
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
            fileNodes = m_docXml.selectNodes("//" + I_CmsConstants.C_EXPORT_TAG_FILE);
            
            int importSize = fileNodes.size();
            // walk through all files in manifest
            for (int i = 0; i < fileNodes.size(); i++) {
                m_report.print(" ( " + (i + 1) + " / " + importSize + " ) ");
                currentElement = (Element)fileNodes.get(i);
                // get all information for a file-import
                // <source>
                source = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_SOURCE);
                // <destintion>
                destination = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_DESTINATION);
                // <type>
                type = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_TYPE);
                // <uuidstructure>
                uuidstructure = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_UUIDSTRUCTURE);
                // <uuidresource>
                uuidresource = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_UUIDRESOURCE);
                // <datelastmodified>
                if ((timestamp = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_DATELASTMODIFIED)) != null) {
                    datelastmodified = Long.parseLong(timestamp);
                } else {
                    datelastmodified = System.currentTimeMillis();
                }
                // <userlastmodified>
                userlastmodified = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_USERLASTMODIFIED);
                // <datecreated>
                if ((timestamp = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_DATECREATED)) != null) {
                    datecreated = Long.parseLong(timestamp);
                } else {
                    datecreated = System.currentTimeMillis();
                }
                // <usercreated>
                usercreated = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_USERCREATED);
                // <flags>              
                flags = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_FLAGS);

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
                    properties = readPropertiesFromManifest(currentElement, propertyName, propertyValue, deleteProperties);

                    // import the resource               
                    CmsResource res = importResource(source, destination, type, uuidstructure, uuidresource, datelastmodified, userlastmodified, datecreated, usercreated, flags, properties, writtenFilenames, fileCodes);

                    List aceList = new ArrayList();
                    if (res != null) {                    
                    
                        // write all imported access control entries for this file
                        acentryNodes = currentElement.selectNodes("*/" + I_CmsConstants.C_EXPORT_TAG_ACCESSCONTROL_ENTRY);
                        // collect all access control entries
                        for (int j = 0; j < acentryNodes.size(); j++) {
                            currentEntry = (Element)acentryNodes.get(j);
                            // get the data of the access control entry
                            String id = CmsImport.getChildElementTextValue(currentEntry, I_CmsConstants.C_EXPORT_TAG_ACCESSCONTROL_PRINCIPAL);
                            String acflags = CmsImport.getChildElementTextValue(currentEntry, I_CmsConstants.C_EXPORT_TAG_FLAGS);
                            String allowed = CmsImport.getChildElementTextValue(currentEntry, I_CmsConstants.C_EXPORT_TAG_ACCESSCONTROL_ALLOWEDPERMISSIONS);
                            String denied = CmsImport.getChildElementTextValue(currentEntry, I_CmsConstants.C_EXPORT_TAG_ACCESSCONTROL_DENIEDPERMISSIONS);

                            // add the entry to the list
                            aceList.add(getImportAccessControlEntry(res, id, allowed, denied, acflags));
                        }
                        importAccessControlEntries(res, aceList);

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

        } catch (Exception exc) {
            m_report.println(exc);
            throw new CmsException(CmsException.C_IMPORT_ERROR, exc);
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
     * 
      * @return imported resource
      */
    private CmsResource importResource(String source, String destination, String type, String uuidstructure, String uuidresource, long datelastmodified, String userlastmodified, long datecreated, String usercreated, String flags, List properties, Vector writtenFilenames, Vector fileCodes) {

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
                    CmsResource channel = m_cms.readResource(I_CmsConstants.C_ROOT + destination);
                    channelId = m_cms.readPropertyObject(m_cms.getSitePath(channel), I_CmsConstants.C_PROPERTY_CHANNELID, false).getValue();
                } catch (Exception e) {
                    // ignore the exception, a new channel id will be generated
                }
                if (channelId != null) {
                    properties.add(new CmsProperty(I_CmsConstants.C_PROPERTY_CHANNELID, channelId, null));
                }
            }
            // get the file content
            if (source != null) {
                content = getFileBytes(source);
            }
            // get all required information to create a CmsResource
            int resType = OpenCms.getResourceManager().getResourceType(type).getTypeId();
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
            CmsUUID newUuidresource = new CmsUUID();
            if (uuidstructure != null) {
                newUuidstructure = new CmsUUID(uuidstructure);
            }
            if (uuidresource != null) {
                newUuidresource = new CmsUUID(uuidresource);
            }

            // convert to xml page if wanted
            if (m_convertToXmlPage 
                && (resType == A_CmsImport.C_RESOURCE_TYPE_PAGE_ID || resType == C_RESOURCE_TYPE_NEWPAGE_ID)) {
                
                if (content != null) {

                    //get the encoding
                    String encoding = null;                    
                    encoding = CmsProperty.get(I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, properties).getValue();
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
                resType = CmsResourceTypeXmlPage.C_RESOURCE_TYPE_ID;
            }
            
            // create a new CmsResource                         
            CmsResource resource = new CmsResource(
                newUuidstructure, 
                newUuidresource, 
                destination,
                resType,
                CmsFolder.isFolderType(resType),
                new Integer(flags).intValue(), 
                m_cms.getRequestContext().currentProject().getId(), 
                I_CmsConstants.C_STATE_NEW, 
                datelastmodified, 
                newUserlastmodified, 
                datecreated, 
                newUsercreated, 
                CmsResource.DATE_RELEASED_DEFAULT, 
                CmsResource.DATE_EXPIRED_DEFAULT, 
                1, size
            );
            
            // import this resource in the VFS   
            res = m_cms.importResource(m_importPath + destination, resource, content, properties);

            if (res != null) {
                if (A_CmsImport.C_RESOURCE_TYPE_PAGE_NAME.equals(type)) {
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
                // noop
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

    /**
     * Imports a single user.<p>
     * @param name user name
     * @param description user description
     * @param flags user flags
     * @param password user password 
     * @param firstname firstname of the user
     * @param lastname lastname of the user
     * @param email user email
     * @param address user address 
     * @param type user type
     * @param userInfo user info
     * @param userGroups user groups
     * 
     * @throws CmsException in case something goes wrong
     */
    protected void importUser(String name, String description, String flags, String password, String firstname, String lastname, String email, String address, String type, Hashtable userInfo, Vector userGroups) throws CmsException {
     
        boolean convert = false;
        
        Map config = OpenCms.getPasswordHandler().getConfiguration();
        if (config != null && config.containsKey(I_CmsPasswordHandler.C_CONVERT_DIGEST_ENCODING)) {
            convert = Boolean.valueOf((String)config.get(I_CmsPasswordHandler.C_CONVERT_DIGEST_ENCODING)).booleanValue();
        } 
            
        if (convert) {
            password = convertDigestEncoding(password);
        }

        super.importUser(name, description, flags, password, firstname, lastname, email, address, type, userInfo, userGroups);
    }
}

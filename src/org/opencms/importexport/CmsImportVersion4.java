/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/CmsImportVersion4.java,v $
 * Date   : $Date: 2004/05/13 11:08:40 $
 * Version: $Revision: 1.36 $
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

import org.opencms.configuration.CmsImportExportConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceTypeFolder;
import org.opencms.file.CmsResourceTypeXmlPage;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsUUID;
import org.opencms.xml.page.CmsXmlPage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import java.util.zip.ZipFile;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Implementation of the OpenCms Import Interface ({@link org.opencms.importexport.I_CmsImport}) for 
 * the import version 4.<p>
 * 
 * This import format is used in OpenCms since 5.1.6.
 * @see org.opencms.importexport.A_CmsImport
 *
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 */
public class CmsImportVersion4 extends A_CmsImport {
    
    /** The version number of this import implementation.<p> */
    private static final int C_IMPORT_VERSION = 4;
    
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
        return CmsImportVersion4.C_IMPORT_VERSION;
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
        m_linkStorage = new HashMap();
        m_linkPropertyStorage = new HashMap();
        
        // these lines make Eclipse happy...
        if (writtenFilenames != null) {
            writtenFilenames.size();
        }
        if (fileCodes != null) {
            fileCodes.size();
        }
        
        try {
            // first import the user information
            if (m_cms.isAdmin()) {
                importGroups();
                importUsers();
            }
            // now import the VFS resources
            importAllResources(excludeList, propertyName, propertyValue);
            convertPointerToLinks();
        } catch (CmsException e) {
            throw e;
        } finally {
            cleanUp();
        }
    }

    /**
     * Imports the groups and writes them to the cms.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    private void importGroups() throws CmsException {
        List groupNodes;
        Element currentElement;
        String name, description, flags, parentgroup;
        try {
            // getAll group nodes
            groupNodes = m_docXml.selectNodes("//" + I_CmsConstants.C_EXPORT_TAG_GROUPDATA);
            // walk through all groups in manifest
            for (int i = 0; i < groupNodes.size(); i++) {
                currentElement = (Element)groupNodes.get(i);
                name = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_NAME);
                name = OpenCms.getImportExportManager().translateGroup(name);  
                description = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_DESCRIPTION);
                flags = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_FLAGS);
                parentgroup = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_PARENTGROUP);
                if ((parentgroup!=null) && (parentgroup.length()>0)) {
                    parentgroup = OpenCms.getImportExportManager().translateGroup(parentgroup);
                }
                // import this group
             
                importGroup(null, name, description, flags, parentgroup);
            }

            // now try to import the groups in the stack
            while (!m_groupsToCreate.empty()) {
                Stack tempStack = m_groupsToCreate;
                m_groupsToCreate = new Stack();
                while (tempStack.size() > 0) {
                    Hashtable groupdata = (Hashtable)tempStack.pop();
                    name = (String)groupdata.get(I_CmsConstants.C_EXPORT_TAG_NAME);
                    description = (String)groupdata.get(I_CmsConstants.C_EXPORT_TAG_DESCRIPTION);
                    flags = (String)groupdata.get(I_CmsConstants.C_EXPORT_TAG_FLAGS);
                    parentgroup = (String)groupdata.get(I_CmsConstants.C_EXPORT_TAG_PARENTGROUP);
                    // try to import the group
                    importGroup(null, name, description, flags, parentgroup);
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
      List userNodes;
      List groupNodes;
      Element currentElement, currentGroup;
      Vector userGroups;
      Hashtable userInfo = new Hashtable();
      sun.misc.BASE64Decoder dec;
      String  name, description, flags, password, recoveryPassword, firstname, lastname, email, address, section, defaultGroup, type, pwd, infoNode;
      // try to get the import resource
      //getImportResource();
      try {
          // getAll user nodes
          userNodes = m_docXml.selectNodes("//" + I_CmsConstants.C_EXPORT_TAG_USERDATA);
          // walk threw all groups in manifest
          for (int i = 0; i < userNodes.size(); i++) {
              currentElement = (Element)userNodes.get(i);
              name = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_NAME);
              name = OpenCms.getImportExportManager().translateUser(name);              
              // decode passwords using base 64 decoder
              dec = new sun.misc.BASE64Decoder();
              pwd = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_PASSWORD);
              password = new String(dec.decodeBuffer(pwd.trim()));
              dec = new sun.misc.BASE64Decoder();
              pwd = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_RECOVERYPASSWORD);
              recoveryPassword = new String(dec.decodeBuffer(pwd.trim()));

              description = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_DESCRIPTION);
              flags = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_FLAGS);
              firstname = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_FIRSTNAME);
              lastname = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_LASTNAME);
              email = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_EMAIL);
              address = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_ADDRESS);
              section = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_SECTION);
              defaultGroup = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_DEFAULTGROUP);
              type = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_TYPE);
              // get the userinfo and put it into the hashtable
              infoNode = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_USERINFO);
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
              groupNodes = currentElement.selectNodes("*/" + I_CmsConstants.C_EXPORT_TAG_GROUPNAME);
              userGroups = new Vector();
              for (int j = 0; j < groupNodes.size(); j++) {
                  currentGroup = (Element)groupNodes.get(j);
                  String userInGroup=CmsImport.getChildElementTextValue(currentGroup, I_CmsConstants.C_EXPORT_TAG_NAME);
                  userInGroup = OpenCms.getImportExportManager().translateGroup(userInGroup);  
                  userGroups.addElement(userInGroup);
              }
              // import this user
              importUser(null, name, description, flags, password, recoveryPassword, firstname, lastname, email, address, section, defaultGroup, type, userInfo, userGroups);
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
    * @param propertyName name of a property to be added to all resources
    * @param propertyValue value of that property
    * @throws CmsException if something goes wrong
    */
   private void importAllResources(Vector excludeList, String propertyName, String propertyValue) throws CmsException {

       String source, destination, type, uuidresource, uuidcontent, userlastmodified, usercreated, flags, timestamp;
       long datelastmodified, datecreated;
       int resType;
       int loaderId = I_CmsConstants.C_UNKNOWN_ID;

       List fileNodes, acentryNodes;
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
           fileNodes = m_docXml.selectNodes("//" + I_CmsConstants.C_EXPORT_TAG_FILE);
           int importSize = fileNodes.size();
           // walk through all files in manifest
           for (int i = 0; i < fileNodes.size(); i++) {
               m_report.print(" ( " + (i + 1) + " / " + importSize + " ) ", I_CmsReport.C_FORMAT_NOTE);
               currentElement = (Element)fileNodes.get(i);
               // get all information for a file-import
               // <source>
               source = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_SOURCE);
               // <destintion>
               destination = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_DESTINATION);
               // <type>
               type = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_TYPE);
               if (C_RESOURCE_TYPE_NEWPAGE_NAME.equals(type)) {
                   resType = C_RESOURCE_TYPE_NEWPAGE_ID;
               } else if (C_RESOURCE_TYPE_PAGE_NAME.equals(type)) {
                   resType = C_RESOURCE_TYPE_PAGE_ID;
               } else if (C_RESOURCE_TYPE_LINK_NAME.equals(type)) {
                   resType = C_RESOURCE_TYPE_LINK_ID;
               } else {
                   resType = m_cms.getResourceTypeId(type);
                   loaderId = m_cms.getResourceType(resType).getLoaderId();
               }
               // <uuidresource>
               uuidresource = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_UUIDRESOURCE);
               // <uuidcontent>
               uuidcontent = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_UUIDCONTENT);
               // <datelastmodified>
               if ((timestamp = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_DATELASTMODIFIED)) != null) {
                   datelastmodified = Long.parseLong(timestamp);
               } else {
                   datelastmodified = System.currentTimeMillis();
               }
               // <userlastmodified>
               userlastmodified = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_USERLASTMODIFIED);
               userlastmodified = OpenCms.getImportExportManager().translateUser(userlastmodified);
               // <datecreated>
               if ((timestamp = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_DATECREATED)) != null) {
                   datecreated = Long.parseLong(timestamp);
               } else {
                   datecreated = System.currentTimeMillis();
               }
               // <usercreated>
               usercreated = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_USERCREATED);
               usercreated = OpenCms.getImportExportManager().translateUser(usercreated);
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
                   properties = getPropertiesFromXml(currentElement, resType, propertyName, propertyValue, deleteProperties);
                   // import the resource               
                   CmsResource res = importResource(source, destination, resType,  loaderId, uuidresource, uuidcontent, datelastmodified, userlastmodified, datecreated, usercreated, flags, properties);
                   // if the resource was imported add the access control entrys if available
                   if (res != null) {
                       // write all imported access control entries for this file
                       acentryNodes = currentElement.selectNodes("*/" + I_CmsConstants.C_EXPORT_TAG_ACCESSCONTROL_ENTRY);
                       // collect all access control entries
                       for (int j = 0; j < acentryNodes.size(); j++) {
                           currentEntry = (Element)acentryNodes.get(j);
                           // get the data of the access control entry
                           String id = CmsImport.getChildElementTextValue(currentEntry, I_CmsConstants.C_EXPORT_TAG_ACCESSCONTROL_PRINCIPAL);
                           String principalId=new CmsUUID().toString();

                           String principal=id.substring(id.indexOf(".")+1, id.length());

                           try {
                               if (id.startsWith(CmsImportExportConfiguration.C_PRINCIPAL_GROUP)) {
                                   principal = OpenCms.getImportExportManager().translateGroup(principal);  
                                   principalId=m_cms.readGroup(principal).getId().toString();
                               } else {
                                   principal = OpenCms.getImportExportManager().translateUser(principal);  
                                   principalId=m_cms.readUser(principal).getId().toString();
                               }                                                    
                               String acflags = CmsImport.getChildElementTextValue(currentEntry, I_CmsConstants.C_EXPORT_TAG_FLAGS);                                                      
                               String allowed = ((Element)currentEntry.selectNodes("./" + I_CmsConstants.C_EXPORT_TAG_ACCESSCONTROL_PERMISSIONSET + "/" + I_CmsConstants.C_EXPORT_TAG_ACCESSCONTROL_ALLOWEDPERMISSIONS).get(0)).getTextTrim();
                               String denied = ((Element)currentEntry.selectNodes("./" + I_CmsConstants.C_EXPORT_TAG_ACCESSCONTROL_PERMISSIONSET + "/" + I_CmsConstants.C_EXPORT_TAG_ACCESSCONTROL_DENIEDPERMISSIONS).get(0)).getTextTrim();
                               // add the entry to the list
                               addImportAccessControlEntry(res, principalId, allowed, denied, acflags);
                           } catch (CmsException e) {
                               // user or group of ACE might not exist in target system, ignore ACE
                               OpenCms.getLog(this).warn("Could not import ACE for resource " + translatedName, e);
                               m_report.println(e);
                           }
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
    * @param resType the resource-type of the file
    * @param loaderId the loader id of the resource
    * @param uuidresource  the resource uuid of the resource
    * @param uuidcontent the file uuid of the resource
    * @param datelastmodified the last modification date of the resource
    * @param userlastmodified the user who made the last modifications to the resource
    * @param datecreated the creation date of the resource
    * @param usercreated the user who created 
    * @param flags the flags of the resource     
    * @param properties a hashtable with properties for this resource
    * @return imported resource
    */
    private CmsResource importResource(
        String source, 
        String destination,         
        int resType, 
        int loaderId, 
        String uuidresource, 
        String uuidcontent, 
        long datelastmodified, 
        String userlastmodified, 
        long datecreated, 
        String usercreated, 
        String flags, Map properties
    ) {

        byte[] content = null;
        CmsResource res = null;

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
                newUserlastmodified = m_cms.getRequestContext().currentUser().getId();
                // datelastmodified = System.currentTimeMillis();
            }

            try {
                newUsercreated = m_cms.readUser(usercreated).getId();
            } catch (CmsException e) {
                newUsercreated = m_cms.getRequestContext().currentUser().getId();
                // datecreated = System.currentTimeMillis();
            }

            // get UUIDs for the resource and content        
            CmsUUID newUuidresource;
            if ((uuidresource != null) && (resType != CmsResourceTypeFolder.C_RESOURCE_TYPE_ID)) {
                newUuidresource = new CmsUUID(uuidresource);
            } else {
                newUuidresource = new CmsUUID();
            }
            CmsUUID newUuidcontent;
            if (uuidcontent != null) {
                newUuidcontent = new CmsUUID(uuidcontent);
            } else {
                newUuidcontent = new CmsUUID();
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
                    && (resType == A_CmsImport.C_RESOURCE_TYPE_PAGE_ID || resType == C_RESOURCE_TYPE_NEWPAGE_ID)) {
                
                if (content != null) {
                    //get the encoding
                    String encoding;
                    encoding = (String)properties.get(I_CmsConstants.C_PROPERTY_CONTENT_ENCODING);
                    if (encoding == null) {
                        encoding = OpenCms.getSystemInfo().getDefaultEncoding();
                    }  
                    
                    CmsXmlPage xmlPage = CmsXmlPageConverter.convertToXmlPage(m_cms, new String(content, encoding), "body", getLocale(destination, properties), encoding);
                    ByteArrayOutputStream pageContent = new ByteArrayOutputStream();
                    xmlPage.write(pageContent, OpenCms.getSystemInfo().getDefaultEncoding());    
                    content = pageContent.toByteArray();
                }
                resType = CmsResourceTypeXmlPage.C_RESOURCE_TYPE_ID;
                loaderId = m_cms.getResourceType(resType).getLoaderId();
            }
            
            // create a new CmsResource                         
            CmsResource resource = new CmsResource(
                new CmsUUID(), // structure ID is always a new UUID
                newUuidresource, 
                CmsUUID.getNullUUID(),
                newUuidcontent, 
                resname, 
                resType, 
                new Integer(flags).intValue(), 
                m_cms.getRequestContext().currentProject().getId(), 
                I_CmsConstants.C_STATE_NEW, 
                loaderId, 
                datecreated, 
                newUsercreated, 
                datelastmodified, 
                newUserlastmodified, 
                size, 
                1
            );
             
            if (C_RESOURCE_TYPE_LINK_ID == resType) {
                // store links for later conversion
                m_report.print(m_report.key("report.storing_link"), I_CmsReport.C_FORMAT_NOTE);
                m_linkStorage.put(m_importPath + destination, new String(content));
                m_linkPropertyStorage.put(m_importPath + destination, properties);
                res = resource;
            } else {             
                // import this resource in the VFS   
                res = m_cms.importResource(resource, content, properties, m_importPath + destination);
            }

            if (res != null) {
                if (C_RESOURCE_TYPE_PAGE_ID == resType) {
                    m_importedPages.add(I_CmsConstants.C_FOLDER_SEPARATOR + destination);
                }
                m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
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
        return res;
    }


}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/A_CmsImport.java,v $
 * Date   : $Date: 2005/02/17 12:43:47 $
 * Version: $Revision: 1.61 $
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

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.util.CmsUUID;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.codec.binary.Base64;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Collection of common used methods for implementing OpenCms Import classes.<p>
 * 
 * This class does not implement a real OpenCms import, real import implmentation should be 
 * inherited form this class.<p>
 *
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * 
 * @see org.opencms.importexport.I_CmsImport
 */

public abstract class A_CmsImport implements I_CmsImport {

    /** The algorithm for the message digest. */
    public static final String C_IMPORT_DIGEST = "MD5";
    
    /** The id of the legacy resource type "page". */
    public static final int C_RESOURCE_TYPE_PAGE_ID = 1;
    
    /** The name of the legacy resource type "page". */
    public static final String C_RESOURCE_TYPE_PAGE_NAME = "page";
    
    /** The id of the legacy resource type "link". */
    public static final int C_RESOURCE_TYPE_LINK_ID = 2;
    
    /** The name of the legacy resource type "link". */
    public static final String C_RESOURCE_TYPE_LINK_NAME = "link";
    
    /** The id of the legacy resource type "newpage". */
    public static final int C_RESOURCE_TYPE_NEWPAGE_ID = 9;
    
    /** The name of the legacy resource type "newpage". */
    public static final String C_RESOURCE_TYPE_NEWPAGE_NAME = "newpage";
    
    /** Debug flag to show debug output. */
    protected static final int DEBUG = 0;

    /** The cms context to do the import operations with. */
    protected CmsObject m_cms;

    /** Digest for taking a fingerprint of the files. */
    protected MessageDigest m_digest;

    /** The xml manifest-file. */
    protected Document m_docXml;

    /** Groups to create during import are stored here. */
    protected Stack m_groupsToCreate;

    /**
     * In this vector we store the imported pages (as Strings from getAbsolutePath()),
     * after the import we check them all to update the link tables for the linkmanagement.
     */
    protected List m_importedPages;

    /** Indicates if module data is being imported. */
    protected boolean m_importingChannelData;

    /** The import-path to write resources into the cms. */
    protected String m_importPath;

    /** The import-resource (folder) to load resources from. */
    protected File m_importResource;

    /** Flag for conversion to xml pages. */
    protected boolean m_convertToXmlPage;

    /** The import-resource (zip) to load resources from. */
    protected ZipFile m_importZip;

    /** Storage for all pointer properties which must be converted into links. */
    protected Map m_linkPropertyStorage;

    /** Storage for all pointers which must be converted into links. */
    protected Map m_linkStorage;

    /** The object to report the log messages. */
    protected I_CmsReport m_report;

    /**
     * Initializes all member variables before the import is started.<p>
     * 
     * This is required since there is only one instance for
     * each import version that is kept in memory and reused.<p>
     */
    protected void initialize() {
        m_groupsToCreate = new Stack();
        m_importedPages = new ArrayList();
        
        if (OpenCms.getRunLevel() > 1) {
            if ((OpenCms.getMemoryMonitor() != null) && OpenCms.getMemoryMonitor().enabled()) {
                OpenCms.getMemoryMonitor().register(this.getClass().getName() + "." + "m_importedPages", m_importedPages);
    }
        }            
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
        m_importedPages = null;
        m_groupsToCreate = null;
        m_cms = null;
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
    protected CmsAccessControlEntry getImportAccessControlEntry(CmsResource res, String id, String allowed, String denied, String flags) {

        return new CmsAccessControlEntry(res.getResourceId(), new CmsUUID(id), Integer.parseInt(allowed), Integer.parseInt(denied), Integer.parseInt(flags));
    }

    /**
     * Checks if the resources is in the list of immutalbe resources. <p>
     * 
     * @param translatedName the name of the resource
     * @param immutableResources the list of the immutable resources
     * @return true or false
     */
    protected boolean checkImmutable(String translatedName, List immutableResources) {

        boolean resourceNotImmutable = true;
        if (immutableResources.contains(translatedName)) {
            if (DEBUG > 1) {
                System.err.println("Import: Translated resource name is immutable");
            }
            // this resource must not be modified by an import if it already exists
            m_cms.getRequestContext().saveSiteRoot();
            try {
                m_cms.getRequestContext().setSiteRoot("/");
                m_cms.readResource(translatedName);
                resourceNotImmutable = false;
                if (DEBUG > 0) {
                    System.err.println("Import: Immutable flag set for resource");
                }
            } catch (CmsException e) {
                // resourceNotImmutable will be true 
                if (DEBUG > 0) {
                    System.err.println("Import: Immutable test caused exception " + e);
                }                              
            } finally {
                m_cms.getRequestContext().restoreSiteRoot();
            }
        }
        return resourceNotImmutable;
    }

    /**
     * Converts old style pointers to siblings if possible.<p>
     */
    protected void convertPointerToSiblings() {
        
        Set checkedProperties = new HashSet();
        Iterator keys = m_linkStorage.keySet().iterator();
        int linksSize = m_linkStorage.size();
        int i = 0;
        CmsResource resource = null;
        String link = null;
        String key = null;
        
        try {
            // loop through all links to convert
            while (keys.hasNext()) {
                
                try {
                    key = (String)keys.next();
                    link = (String)m_linkStorage.get(key);
                    List properties = (List)m_linkPropertyStorage.get(key);
                    
                    m_report.print(" ( " + (++i) + " / " + linksSize + " ) ", I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(m_report.key("report.convert_link"), I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(key + " ");
                    m_report.print(m_report.key("report.dots"));
        
                    // check if this is an internal pointer
                    if (link.startsWith("/")) {
                        // check if the pointer target is existing
                        CmsResource target = m_cms.readResource(link);
    
                        // create a new sibling as CmsResource                         
                        resource = new CmsResource(
                            new CmsUUID(), // structure ID is always a new UUID
                            target.getResourceId(), 
                            key,
                            target.getTypeId(),
                            target.isFolder(), 
                            0, 
                            m_cms.getRequestContext().currentProject().getId(), // TODO: pass flags from import 
                            I_CmsConstants.C_STATE_NEW, 
                            target.getDateCreated(),
                            target.getUserCreated(), 
                            target.getDateLastModified(), 
                            target.getUserLastModified(), 
                            CmsResource.DATE_RELEASED_DEFAULT, 
                            CmsResource.DATE_EXPIRED_DEFAULT, 
                            1, 
                            0
                        );
                        
                        m_cms.importResource(key, resource, null, properties);
                        m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
                        
                        if (OpenCms.getLog(this).isInfoEnabled()) {
                            OpenCms.getLog(this).info(
                                
                                "( " + (i) + " / " + linksSize + " ) "
                                + m_report.key("report.convert_link")
                                + key
                                + " "
                                + m_report.key("report.dots")
                                + m_report.key("report.ok"));
                        }                              
        
                    } else {
                        
                        // make sure all found properties are already defined
                        for (int j = 0, n = properties.size(); j < n; j++) {
                            CmsProperty property = (CmsProperty)properties.get(j);
                            
                            if (!checkedProperties.contains(property)) {
                                // check the current property and create it, if necessary
                                checkPropertyDefinition(property.getKey());
                                checkedProperties.add(property);                        
                            }
                        }
                        
                        m_cms.createResource(key, CmsResourceTypePointer.C_RESOURCE_TYPE_ID, link.getBytes(), properties);
                        m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
                        
                        if (OpenCms.getLog(this).isInfoEnabled()) {
                            OpenCms.getLog(this).info(
                                
                                "( " + (i) + " / " + linksSize + " ) "
                                + m_report.key("report.convert_link")
                                + key
                                + " "
                                + m_report.key("report.ok"));
                        }                          
                        
                    }
                } catch (CmsException e) {
                    m_report.println();
                    m_report.print(m_report.key("report.convert_link_notfound") + " " + link, I_CmsReport.C_FORMAT_WARNING);
                    
                    if (OpenCms.getLog(this).isErrorEnabled()) {
                        OpenCms.getLog(this).error("Link conversion of " + key + " -> " + link + " failed", e);
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
     * Converts a given digest to base64 encoding.<p>
     * 
     * @param value the digest value in the legacy encoding
     * @return the digest in the new encoding
     */
    public String convertDigestEncoding(String value) {
        
        byte data[] = new byte[value.length() / 2];
                
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(Integer.parseInt(value.substring(i*2, i*2+2), 16) - 128); 
        }
        
        return new String(Base64.encodeBase64(data));
    }
    
    /**
     * Tries to read the property definition for a specified key.<p>
     * The property defintion gets created if it is missing.<p>
     *
     * @param key the key of the property
     * @throws CmsException if something goes wrong
     */
    private void checkPropertyDefinition(String key) throws CmsException {
        try {
            // try to read the property definition
            m_cms.readPropertyDefinition(key);
        } catch (CmsException exc) {
            // create missing property definitions
            m_cms.createPropertyDefinition(key);
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
                if (entry == null && filename.startsWith("/")) {
                    entry = m_importZip.getEntry(filename.substring(1));
                } else if (entry == null) {
                    throw new ZipException("File not found in zipfile: " + filename); 
                }    
                
                InputStream stream = m_importZip.getInputStream(entry);

                int charsRead = 0;
                int size = new Long(entry.getSize()).intValue();
                byte[] buffer = new byte[size];
                while (charsRead < size) {
                    charsRead += stream.read(buffer, charsRead, size - charsRead);
                }
                stream.close();
                return buffer;
            } else {
                // no - use directory
                File file = new File(m_importResource, filename);
                FileInputStream fileStream = new FileInputStream(file);

                int charsRead = 0;
                int size = new Long(file.length()).intValue();
                byte[] buffer = new byte[size];
                while (charsRead < size) {
                    charsRead += fileStream.read(buffer, charsRead, size - charsRead);
                }
                fileStream.close();
                return buffer;
            }
        } catch (FileNotFoundException fnfe) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("File not found: " + filename, fnfe);
            }             
            m_report.println(fnfe);
        } catch (IOException ioe) {
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error reading file " + filename, ioe);
            }             
            m_report.println(ioe);
        }
        // this will only be returned in case there was an exception
        return "".getBytes();
    }

    /**
     * Returns the appropriate locale for the given destination.<p>
     * 
     * @param destination the destination path (parent must exist)
     * @param properties the properties to check at first
     * 
     * @return the locale
     */
    protected Locale getLocale(String destination, List properties) {        
        String localeName = CmsProperty.get(I_CmsConstants.C_PROPERTY_LOCALE, properties).getValue();
                
        if (localeName != null) {
            // locale was already set on the files properties
            return (Locale)OpenCms.getLocaleManager().getAvailableLocales(localeName).get(0);
        } 
        // locale not set in properties, read default locales
        return (Locale)OpenCms.getLocaleManager().getDefaultLocales(m_cms, CmsResource.getParentFolder(destination)).get(0);        
    }
    
    /**
     * Reads all properties below a specified parent element from manifest.xml.<p>
     * 
     * @param parentElement the current file node
     * @param propertyKey key of a property to be added to all resources, or null
     * @param propertyValue value of the property to be added to all resources, or null
     * @param ignoredPropertyKeys a list of properies to be ignored
     * 
     * @return a list with all properties
     * @throws CmsException if something goes wrong
     */
    protected List readPropertiesFromManifest(
        Element parentElement,
        String propertyKey,
        String propertyValue,
        List ignoredPropertyKeys) throws CmsException {

        // all imported Cms property objects are collected in map first forfaster access
        Map properties = new HashMap();
        CmsProperty property = null;
        List propertyElements = parentElement.selectNodes("./"
            + I_CmsConstants.C_EXPORT_TAG_PROPERTIES
            + "/"
            + I_CmsConstants.C_EXPORT_TAG_PROPERTY);
        Element propertyElement = null;
        String key = null, value = null;
        Attribute attrib = null;

        if (propertyKey != null && propertyValue != null && !"".equals(propertyKey)) {
            checkPropertyDefinition(propertyKey);
            properties.put(propertyKey, propertyValue);
        }

        // iterate over all property elements
        for (int i = 0, n = propertyElements.size(); i < n; i++) {
            propertyElement = (Element)propertyElements.get(i);
            key = CmsImport.getChildElementTextValue(propertyElement, I_CmsConstants.C_EXPORT_TAG_NAME);

            if (key == null || ignoredPropertyKeys.contains(key)) {
                // continue if the current property (key) should be ignored or is null
                continue;
            }

            // all Cms properties are collected in a map keyed by their property keys
            if ((property = (CmsProperty)properties.get(key)) == null) {
                property = new CmsProperty();
                property.setKey(key);
                property.setAutoCreatePropertyDefinition(true);
                properties.put(key, property);
            }

            if ((value = CmsImport.getChildElementTextValue(propertyElement, I_CmsConstants.C_EXPORT_TAG_VALUE)) == null) {
                value = "";
            }

            if ((attrib = propertyElement.attribute(I_CmsConstants.C_EXPORT_TAG_PROPERTY_ATTRIB_TYPE)) != null
                && attrib.getValue().equals(I_CmsConstants.C_EXPORT_TAG_PROPERTY_ATTRIB_TYPE_SHARED)) {
                // it is a shared/resource property value
                property.setResourceValue(value);
            } else {
                // it is an individual/structure value
                property.setStructureValue(value);
            }

            checkPropertyDefinition(key);
        }

        return new ArrayList(properties.values());
    }

    /**
     * Writes alread imported access control entries for a given resource.
     * 
     * @param resource the resource assigned to the access control entries
     * @param aceList the access control entries to create
     */
    protected void importAccessControlEntries(CmsResource resource, List aceList) {
        if (aceList.size() == 0) {
            // no ACE in the list
            return;
        }        
        try {
            m_cms.importAccessControlEntries(resource, aceList);
        } catch (CmsException exc) {
            m_report.println(m_report.key("report.import_accesscontroldata_failed"), I_CmsReport.C_FORMAT_WARNING);
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
     * @throws CmsException if something goes wrong
     */
    protected void importGroup(String name, String description, String flags, String parentgroupName) throws CmsException {

        if (description == null) {
            description = "";
        }

        CmsGroup parentGroup = null;
        try {
            if ((parentgroupName != null) && (!"".equals(parentgroupName))) {
                try {
                    parentGroup = m_cms.readGroup(parentgroupName);
                } catch (CmsException exc) {
                    // parentGroup will be null
                }
            }

            if (((parentgroupName != null) && (!"".equals(parentgroupName))) && (parentGroup == null)) {
                // cannot create group, put on stack and try to create later
                Hashtable groupData = new Hashtable();
                groupData.put(I_CmsConstants.C_EXPORT_TAG_NAME, name);
                groupData.put(I_CmsConstants.C_EXPORT_TAG_DESCRIPTION, description);
                groupData.put(I_CmsConstants.C_EXPORT_TAG_FLAGS, flags);
                groupData.put(I_CmsConstants.C_EXPORT_TAG_PARENTGROUP, parentgroupName);
                m_groupsToCreate.push(groupData);
            } else {
                try {
                    m_report.print(m_report.key("report.importing_group"), I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(name);
                    m_report.print(m_report.key("report.dots"));
                    m_cms.createGroup(name, description, Integer.parseInt(flags), parentgroupName);
                    m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
                } catch (CmsException exc) {                     
                    m_report.println(m_report.key("report.not_created"), I_CmsReport.C_FORMAT_OK);
                }
            }

        } catch (Exception exc) {            
            m_report.println(exc);
            throw new CmsException(CmsException.C_IMPORT_ERROR, exc);
        }
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

        // create a new user id
        String id = new CmsUUID().toString();
        try {
            try {
                m_report.print(m_report.key("report.importing_user"), I_CmsReport.C_FORMAT_NOTE);
                m_report.print(name);
                m_report.print(m_report.key("report.dots"));
                m_cms.addImportUser(id, name, password, description, firstname, lastname, email, Integer.parseInt(flags), userInfo, address, Integer.parseInt(type));
                // add user to all groups vector
                for (int i = 0; i < userGroups.size(); i++) {
                    try {
                        m_cms.addUserToGroup(name, (String)userGroups.elementAt(i));
                    } catch (CmsException exc) {
                        // ignore
                    }
                }
                m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
            } catch (CmsException exc) {
                m_report.println(m_report.key("report.not_created"), I_CmsReport.C_FORMAT_OK);
            }
        } catch (Exception exc) {
            throw new CmsException(CmsException.C_IMPORT_ERROR, exc);
        }
    }
        
    /**
     * Imports the OpenCms groups.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    protected void importGroups() throws CmsException {
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
             
                importGroup(name, description, flags, parentgroup);
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
                    importGroup(name, description, flags, parentgroup);
                }
            }
        } catch (Exception exc) {
            m_report.println(exc);
            throw new CmsException(CmsException.C_IMPORT_ERROR, exc);
        }
    }
    
    /**
     * Imports the OpenCms users.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    protected void importUsers() throws CmsException {
        List userNodes;
        List groupNodes;
        Element currentElement, currentGroup;
        Vector userGroups;
        Hashtable userInfo = new Hashtable();
        String  name, description, flags, password, firstname, lastname, email, address, type, pwd, infoNode, defaultGroup;
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
                pwd = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_PASSWORD);
                password = new String(Base64.decodeBase64(pwd.trim().getBytes()));
                description = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_DESCRIPTION);
                flags = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_FLAGS);
                firstname = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_FIRSTNAME);
                lastname = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_LASTNAME);
                email = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_EMAIL);
                address = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_ADDRESS);
                type = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_TYPE);
                defaultGroup = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_DEFAULTGROUP);
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
                
                if (defaultGroup != null && !"".equalsIgnoreCase(defaultGroup)) {
                    userInfo.put(I_CmsConstants.C_ADDITIONAL_INFO_DEFAULTGROUP, defaultGroup);
                }
                
                // import this user
                importUser(name, description, flags, password, firstname, lastname, email, address, type, userInfo, userGroups);
            }
        } catch (Exception exc) {
            m_report.println(exc);
            throw new CmsException(CmsException.C_IMPORT_ERROR, exc);
        }
    }
}

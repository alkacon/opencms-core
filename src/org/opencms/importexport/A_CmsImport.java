/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/A_CmsImport.java,v $
 * Date   : $Date: 2004/02/09 10:27:12 $
 * Version: $Revision: 1.24 $
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

import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.util.CmsUUID;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.file.CmsResourceTypePointer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Collection of common used methods for implementing OpenCms Import classes.<p>
 * 
 * This class does not implement a real OpenCms import, real import implmentation should be 
 * inherited form this class.
 * 
 * @see org.opencms.importexport.I_CmsImport
 *
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 */

public abstract class A_CmsImport implements I_CmsImport {

    /** The algorithm for the message digest */
    public static final String C_IMPORT_DIGEST = "MD5";

    /** Debug flag to show debug output */
    protected static final int DEBUG = 0;

    /** Access control entries for a single resource */
    private Vector m_acEntriesToCreate = new Vector();

    /** The cms contect to do the operations on the VFS/COS with */
    protected CmsObject m_cms;

    /** Digest for taking a fingerprint of the files */
    protected MessageDigest m_digest = null;

    /** The xml manifest-file */
    protected Document m_docXml;

    /** Groups to create during import are stored here */
    protected Stack m_groupsToCreate = new Stack();
    
    /** The id of the legacy resource type "newpage" */
    protected static final int C_RESOURCE_TYPE_NEWPAGE_ID = 9;
    
    /** The name of the legacy resource type "newpage" */
    protected static final String C_RESOURCE_TYPE_NEWPAGE_NAME = "newpage";

    /**
     * In this vector we store the imported pages (as Strings from getAbsolutePath()),
     * after the import we check them all to update the link tables for the linkmanagement.
     */
    protected Vector m_importedPages = new Vector();

    /** Indicates if module data is being imported */
    protected boolean m_importingChannelData;

    /** The import-path to write resources into the cms */
    protected String m_importPath;

    /** The import-resource (folder) to load resources from */
    protected File m_importResource = null;

    /** flag for conversion to xml pages */
    protected boolean m_convertToXmlPage;
    
    /**
     * The version of this import, noted in the info tag of the manifest.xml.<p>
     * 
     * 0 indicates an export file without a version number, that is before version 4.3.23 of OpenCms.<br>
     * 1 indicates an export file of OpenCms with a version before 5.0.0
     * 2 indicates an export file of OpenCms with a version before 5.1.2
     * 3 indicates an export file of OpenCms with a version before 5.1.6
     * 4 indicates an export file of OpenCms with a version after 5.1.6
     */
    protected int m_importVersion = 0;

    /**  The import-resource (zip) to load resources from */
    protected ZipFile m_importZip = null;

    /** Storage for all pointer properties which must be converted into links */
    protected HashMap m_linkPropertyStorage;

    /** Storage for all pointers which must be converted into links */
    protected HashMap m_linkStorage;

    /** The object to report the log messages */
    protected I_CmsReport m_report = null;
    
    /**
      * Constructs a new uninitialized import, required for the module data import.<p>
      * 
      * @see CmsImportModuledata
      */
    public A_CmsImport() {
        // empty constructor
    }

    /**
     * Constructs a new import object which imports the resources from an OpenCms 
     * export zip file or a folder in the "real" file system.<p>
     *
     * @param cms the current cms object
     * @param importPath the path in the cms VFS to import into
     * @param report a report object to output the progress information to
     */
    public A_CmsImport(CmsObject cms, String importPath, I_CmsReport report) {
        // set member variables
        m_cms = cms;
        m_importPath = importPath;
        m_report = report;
        m_importingChannelData = false;
    }

    /**
     * Creates a new access control entry and stores it for later write out.
     * 
     * @param res the resource
     * @param id the id of the principal
     * @param allowed the allowed permissions
     * @param denied the denied permissions
     * @param flags the flags
     */
    protected void addImportAccessControlEntry(CmsResource res, String id, String allowed, String denied, String flags) {

        CmsAccessControlEntry ace = new CmsAccessControlEntry(res.getResourceId(), new CmsUUID(id), Integer.parseInt(allowed), Integer.parseInt(denied), Integer.parseInt(flags));
        m_acEntriesToCreate.add(ace);
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
            try {
                m_cms.readFileHeader("//" + translatedName);
                resourceNotImmutable = false;
                if (DEBUG > 0) {
                    System.err.println("Import: Immutable flag set for resource");
                }
            } catch (CmsException e) {
                // resourceNotImmutable will be true
                if (DEBUG > 0) {
                    System.err.println("Import: Immutable test caused exception " + e);
                }
            }
        }
        return resourceNotImmutable;
    }

    /**
     * Converts old style links to new internal links if possible.<p>
     * @throws CmsException if something goes wrong
     */
    protected void convertPointerToLinks() throws CmsException {
        HashSet checkedProperties = new HashSet();
        Iterator keys = m_linkStorage.keySet().iterator();
        int linksSize = m_linkStorage.size();
        int i = 0;
        // loop through all links to convert
        while (keys.hasNext()) {
            String key = (String)keys.next();
            String link = (String)m_linkStorage.get(key);
            HashMap properties = (HashMap)m_linkPropertyStorage.get(key);
            m_report.print(" ( " + (++i) + " / " + linksSize + " ) ", I_CmsReport.C_FORMAT_NOTE);
            m_report.print(m_report.key("report.convert_link"), I_CmsReport.C_FORMAT_NOTE);
            m_report.print(key + " ");
            m_report.print(m_report.key("report.dots"));

            // now check if this is an internal link
            if (link.startsWith("/")) {
                // now check if the link target is existing
                try {
                    CmsResource target = m_cms.readFileHeader(link);

                    // create a new sibling as CmsResource                         
                    CmsResource resource = new CmsResource(
                        new CmsUUID(), // structure ID is always a new UUID
                        target.getResourceId(), 
                        CmsUUID.getNullUUID(),
                        target.getFileId(), 
                        CmsResource.getName(key), 
                        target.getType(), 
                        0, // TODO: pass flags from import 
                        m_cms.getRequestContext().currentProject().getId(), 
                        I_CmsConstants.C_STATE_NEW, 
                        m_cms.getResourceType(target.getType()).getLoaderId(), 
                        target.getDateCreated(), 
                        target.getUserCreated(), 
                        target.getDateLastModified(), 
                        target.getUserLastModified(), 
                        0, 
                        1
                    );
                    
                    m_cms.importResource(resource, null, properties, key);
                    // m_cms.createVfsLink(key, link, properties);
                    m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
                } catch (CmsException ex) {
                    m_report.println();
                    m_report.print(m_report.key("report.convert_link_notfound") + " " + link, I_CmsReport.C_FORMAT_WARNING);
                    
                    if (OpenCms.getLog(this).isDebugEnabled()) {
                        OpenCms.getLog(this).debug("Link conversion failed: " + key + " -> " + link, ex);
                    }
                }

            } else {
                    // make sure all found properties are already defined
                    Iterator propKeys = properties.keySet().iterator();
                    while (propKeys.hasNext()) {
                        String property = (String)propKeys.next();
                        if (!checkedProperties.contains(property)) {
                            // check the current property and create it, if necessary
                            createPropertydefinition(property, CmsResourceTypePointer.C_RESOURCE_TYPE_ID);
                            checkedProperties.add(property);
                        }
                    }
                    m_cms.createResource(key, CmsResourceTypePointer.C_RESOURCE_TYPE_ID, properties, link.getBytes(), null);
                    m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);  
            }

        }
        m_linkStorage = null;
        m_linkPropertyStorage = null;
    }

    /**
     * Creates missing property definitions if needed.<p>
     *
     * @param name the name of the property
     * @param resourceType the type of the resource
     *
     * @throws CmsException if something goes wrong
     */
    private void createPropertydefinition(String name, int resourceType) throws CmsException {
        // does the propertydefinition exists already?
        try {
            m_cms.readPropertydefinition(name, resourceType);
        } catch (CmsException exc) {
            // no: create it
            m_cms.createPropertydefinition(name, resourceType);
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
            m_report.println(fnfe);
        } catch (IOException ioe) {
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
     * @return the locale
     * @throws CmsException if something goes wrong
     */
    protected Locale getLocale(String destination, Map properties) throws CmsException {
        String localeName = (String)properties.get(I_CmsConstants.C_PROPERTY_LOCALE);
        if (localeName == null) {
            localeName = m_cms.readProperty(CmsResource.getParentFolder(destination), I_CmsConstants.C_PROPERTY_LOCALE, true);
        }
        if (localeName != null) {
            if (localeName.indexOf(",") >= 0) {
                localeName = localeName.substring(0, localeName.indexOf(","));
            }
            return CmsLocaleManager.getLocale(localeName);
        } else {            
            return (Locale)OpenCms.getLocaleManager().getDefaultLocales(m_cms, CmsResource.getParentFolder(destination)).get(0);
        }
    }
    
    /**
     * Gets all properties from one file node in the manifest.xml.<p>
     * 
     * @param currentElement the current file node
     * @param resType the resource type of this node
     * @param propertyName name of a property to be added to all resources
     * @param propertyValue value of that property
     * @param deleteProperties the list of properies to be deleted
     * @return HashMap with all properties blonging to the resource
     * @throws CmsException if something goes wrong
     */
    protected HashMap getPropertiesFromXml(Element currentElement, int resType, String propertyName, String propertyValue, List deleteProperties) throws CmsException {
        // get all properties for this file
        NodeList propertyNodes = currentElement.getElementsByTagName(I_CmsConstants.C_EXPORT_TAG_PROPERTY);
        // clear all stores for property information
        HashMap properties = new HashMap();
        // add the module property to properties
        if (propertyName != null && propertyValue != null && !"".equals(propertyName)) {
            createPropertydefinition(propertyName, resType);
            properties.put(propertyName, propertyValue);
        }
        // walk through all properties
        for (int j = 0; j < propertyNodes.getLength(); j++) {
            Element currentProperty = (Element)propertyNodes.item(j);
            // get name information for this property
            String name = getTextNodeValue(currentProperty, I_CmsConstants.C_EXPORT_TAG_NAME);
            // check if this is an unwanted property
            if ((name != null) && (!deleteProperties.contains(name))) {
                // get value information for this property
                String value = getTextNodeValue(currentProperty, I_CmsConstants.C_EXPORT_TAG_VALUE);
                if (value == null) {
                    // create an empty property
                    value = "";
                }
                // add property
                properties.put(name, value);
                createPropertydefinition(name, resType);
            }
        }
        return properties;
    }

    /**
     * Returns the text for a node.
     *
     * @param elem the parent element
     * @param tag the tagname to get the value from
     * @return the value of the tag
     */
    protected String getTextNodeValue(Element elem, String tag) {
        try {
            return elem.getElementsByTagName(tag).item(0).getFirstChild().getNodeValue();
        } catch (Exception exc) {
            // ignore the exception and return null
            return null;
        }
    }

    /**
     * Returns the import version of the import implementation.<p>
     * 
     * @return import version
     */
    public int getVersion() {
        return 0;
    }

    /**
     * Writes alread imported access control entries for a given resource.
     * 
     * @param resource the resource assigned to the access control entries
     * @throws CmsException if something goes wrong
     */
    protected void importAccessControlEntries(CmsResource resource) throws CmsException {
        try {
            try {
                m_cms.importAccessControlEntries(resource, m_acEntriesToCreate);
            } catch (CmsException exc) {
                m_report.println(m_report.key("report.import_accesscontroldata_failed"), I_CmsReport.C_FORMAT_WARNING);
            }
        } catch (Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            m_acEntriesToCreate = new Vector();
        }
    }

    /**
     * Creates an imported group in the cms.<p>
     * 
     * @param id the uuid of this group
     * @param name the name of the group
     * @param description group description
     * @param flags group flags
     * @param parentgroupName name of the parent group
     * @throws CmsException if something goes wrong
     */
    protected void importGroup(String id, String name, String description, String flags, String parentgroupName) throws CmsException {
        if (id == null) {
            id = new CmsUUID().toString();
        }
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
                    m_cms.createGroup(id, name, description, Integer.parseInt(flags), parentgroupName);
                    m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
                } catch (CmsException exc) {
                    m_report.println(m_report.key("report.not_created"), I_CmsReport.C_FORMAT_OK);
                }
            }

        } catch (Exception exc) {
            m_report.println(exc);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Creates an imported user in the cms.<p>
     * 
     * @param id user id or null
     * @param name user name
     * @param description user description
     * @param flags user flags
     * @param password user password 
     * @param recoveryPassword user recovery password
     * @param firstname firstname of the user
     * @param lastname lastname of the user
     * @param email user email
     * @param address user address 
     * @param section user section
     * @param defaultGroup user default group
     * @param type user type
     * @param userInfo user info
     * @param userGroups user groups
     * @throws CmsException in case something goes wrong
     */
    protected void importUser(String id, String name, String description, String flags, String password, String recoveryPassword, String firstname, String lastname, String email, String address, String section, String defaultGroup, String type, Hashtable userInfo, Vector userGroups) throws CmsException {

        // create a new user id if not available
        if (id == null) {
            id = new CmsUUID().toString();
        }

        try {
            try {
                m_report.print(m_report.key("report.importing_user"), I_CmsReport.C_FORMAT_NOTE);
                m_report.print(name);
                m_report.print(m_report.key("report.dots"));
                m_cms.addImportUser(id, name, password, recoveryPassword, description, firstname, lastname, email, Integer.parseInt(flags), userInfo, defaultGroup, address, section, Integer.parseInt(type));
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
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

}

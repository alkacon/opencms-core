/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsImport.java,v $
* Date   : $Date: 2003/03/25 00:14:35 $
* Version: $Revision: 1.87 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.file;

import com.opencms.boot.CmsBase;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.OpenCms;
import com.opencms.flex.util.CmsStringSubstitution;
import com.opencms.linkmanagement.CmsPageLinks;
import com.opencms.report.I_CmsReport;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.template.CmsXmlXercesParser;
import com.opencms.util.LinkSubstitution;
import com.opencms.workplace.I_CmsWpConstants;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;


/**
 * Holds the functionaility to import resources from the filesystem
 * into the OpenCms VFS.
 *
 * @author Andreas Schouten
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * 
 * @version $Revision: 1.87 $ $Date: 2003/03/25 00:14:35 $
 */
public class CmsImport implements I_CmsConstants, I_CmsWpConstants, Serializable {
    
    /**
     * Debug flag to show debug output
     */
    public static final int C_DEBUG = 0;
    
    /**
     * The algorithm for the message digest
     */
    public static final String C_IMPORT_DIGEST = "MD5";
    
    /**
     * The path to the bodies in OpenCms 4.x
     */
    public static final String C_VFS_PATH_OLD_BODIES = "/content/bodys/";
    

    /**
     * The import-file to load resources from
     */
    private String m_importFile;

    /**
     * The import-resource (folder) to load resources from
     */
    private File m_importResource = null;

    /**
     * The version of this import, noted in the info tag of the manifest.xml.
     * 0 if the import file dosent have a version nummber (that is befor version
     * 4.3.23 of OpenCms).
     */
    private int m_importVersion = 0;

    /**
     * Web application names for conversion support
     */
    private List m_webAppNames = new ArrayList();
    
    /**
     * Old webapp URL for import conversion
     */
    private String m_webappUrl = null;

    /**
     * The import-resource (zip) to load resources from
     */
    private ZipFile m_importZip = null;

    /**
     * The import-path to write resources into the cms.
     */
    private String m_importPath;

    /**
     * The cms-object to do the operations.
     */
    private CmsObject m_cms;

    /**
     * The xml manifest-file.
     */
    private Document m_docXml;

    /**
     * Digest for taking a fingerprint of the files
     */
    private MessageDigest m_digest = null;

    /**
     * Groups to create during import are stored here
     */
    private Stack m_groupsToCreate = new Stack();

    /**
     * In this vector we store the imported pages (as Strings from getAbsolutePath()).
     * After the import we check them all to update the link tables for the linkmanagement.
     */
    private Vector m_importedPages = new Vector();

    /**
     * The object to report the log-messages.
     */
    private I_CmsReport m_report = null;
    
    /** Indicates if channel data is imported */
    private boolean m_importingChannelData;
    
   
    /**
     * This constructs a new CmsImport-object which imports the resources.
     *
     * @param importFile the file or folder to import from.
     * @param importPath the path to the cms to import into.
     * @param report A report object to provide the loggin messages.
     * @throws CmsException the CmsException is thrown if something goes wrong.
     */
    public CmsImport(String importFile, String importPath, CmsObject cms, I_CmsReport report)
        throws CmsException {

        m_importFile = importFile;
        m_importPath = importPath;
        m_cms = cms;
        m_report=report;     
        m_importingChannelData = false;

        // create the digest
        createDigest();

        // open the import resource
        getImportResource();

        // read the xml-config file
        getXmlConfigFile();

        // try to read the export version nummber
        try{
            m_importVersion = Integer.parseInt(
                getTextNodeValue((Element)m_docXml.getElementsByTagName(
                    C_EXPORT_TAG_INFO).item(0) , C_EXPORT_TAG_VERSION));
        }catch(Exception e){
            //ignore the exception, the export file has no version nummber (version 0).
        }
    }
    
    /**
     * Imports resources, used for importing module data.<p>
     * 
     * The XML Document end the import environment will be set up in the module
     * data import.<p>
     * 
     * @param cms the current CmsObject
     * @param importPath the import path
     * @param docXml the XML Document for the import 
     * @param report the report to print out the progess information to
     * @throws CmsException if something goes wrong
     * 
     * @see CmsImportModuledata
     */
    public CmsImport(
        CmsObject cms, 
        String importPath,
        Document docXml,
        I_CmsReport report
    ) throws CmsException {
        m_cms = cms;
        m_importPath = importPath;
        m_docXml = docXml;
        m_report = report;
        m_importingChannelData = true;
    }
    
    /**
     * Imports the resources and writes them to the cms even if there already exist conflicting files.<p>
     */
    public void importResources() throws CmsException {
        if (!m_importingChannelData && m_cms.isAdmin()){
            importGroups();
            importUsers();
        }
        importResources(null, null, null, null, null);
        if (m_importZip != null){
            try{
                m_importZip.close();
            } catch (IOException exc) {
                throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
            }
        }
    }
        
    /**
     * Read infos from the properties and create a MessageDigest
     */
    private void createDigest() throws CmsException {
        // Configurations config = m_cms.getConfigurations();
    
        String digest = C_IMPORT_DIGEST;
        // create the digest
        try {
            m_digest = MessageDigest.getInstance(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new CmsException("Could'nt create MessageDigest with algorithm " + digest);
        }
    }

    /**
     * Creates missing property definitions if needed.<p>
     *
     * @param name the name of the property.
     * @param propertyType the type of the property.
     * @param resourceType the type of the resource.
     *
     * @throws throws CmsException if something goes wrong.
     */
    private void createPropertydefinition(String name, String resourceType)
        throws CmsException {
        // does the propertydefinition exists already?
        try {
            m_cms.readPropertydefinition(name, resourceType);
        } catch(CmsException exc) {
            // no: create it
            m_cms.createPropertydefinition(name, resourceType);
        }
    }

    /**
     * Returns a list of files which are both in the import and in the virtual file system.<p>
     * 
     * @return Vector of Strings, complete path of the files
     */
    public Vector getConflictingFilenames() throws CmsException {
        NodeList fileNodes;
        Element currentElement;
        String source, destination;
        Vector conflictNames = new Vector();
        try {
            // get all file-nodes
            fileNodes = m_docXml.getElementsByTagName(C_EXPORT_TAG_FILE);
    
            // walk through all files in manifest
            for (int i = 0; i < fileNodes.getLength(); i++) {
                currentElement = (Element)fileNodes.item(i);
                source = getTextNodeValue(currentElement, C_EXPORT_TAG_SOURCE);
                destination = getTextNodeValue(currentElement, C_EXPORT_TAG_DESTINATION);
                if (source != null) {
                    // only consider files
                    boolean exists = true;
                    try {
                        CmsResource res = m_cms.readFileHeader(m_importPath + destination);
                        if (res.getState() == C_STATE_DELETED) {
                            exists = false;
                        }
                    } catch (CmsException e) {
                        exists = false;
                    }
                    if (exists) {
                        conflictNames.addElement(m_importPath + destination);
                    }
                }
            }
        } catch (Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
        if (m_importZip != null) {
            try {
                m_importZip.close();
            } catch (IOException exc) {
                throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
            }
        }
        return conflictNames;
    }
    
    /**
     * Returns a byte array containing the content of the file.<p>
     *
     * @param filename the name of the file to read
     * @return a byte array containing the content of the file
     */
    private byte[] getFileBytes(String filename)
        throws Exception{
        // is this a zip-file?
        if(m_importZip != null) {
            // yes
            ZipEntry entry = m_importZip.getEntry(filename);
            InputStream stream = m_importZip.getInputStream(entry);

            int charsRead = 0;
            int size = new Long(entry.getSize()).intValue();
            byte[] buffer = new byte[size];
            while(charsRead < size) {
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
            while(charsRead < size) {
                charsRead += fileStream.read(buffer, charsRead, size - charsRead);
            }
            fileStream.close();
            return buffer;
        }
    }
    
    /**
     * Gets the import resource and stores it in object-member.<p>
     */
    private void getImportResource()
        throws CmsException {
        try {
            // get the import resource
            m_importResource = new File(CmsBase.getAbsolutePath(m_importFile));

            // if it is a file it must be a zip-file
            if(m_importResource.isFile()) {
                m_importZip = new ZipFile(m_importResource);
            }
        } catch(Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }
    
    /**
     * Returns a Vector of resource names that are needed to create a project for this import.<p>
     * 
     * It calls the method getConflictingFileNames if needed, to calculate these resources.
     * 
     * @return a Vector of resource names that are needed to create a project for this import
     */
    public Vector getResourcesForProject() throws CmsException {
        NodeList fileNodes;
        Element currentElement;
        String destination;
        Vector resources = new Vector();
        try {
            if (m_importingChannelData) m_cms.setContextToCos();
            
            // get all file-nodes
            fileNodes = m_docXml.getElementsByTagName(C_EXPORT_TAG_FILE);
            // walk through all files in manifest
            for (int i = 0; i < fileNodes.getLength(); i++) {
                currentElement = (Element)fileNodes.item(i);
                destination = getTextNodeValue(currentElement, C_EXPORT_TAG_DESTINATION);
    
                // get the resources for a project
                try {
                    String resource = destination.substring(0, destination.indexOf("/", 1) + 1);
                    resource = m_importPath + resource;
                    // add the resource, if it dosen't already exist
                    if ((!resources.contains(resource)) && (!resource.equals(m_importPath))) {
                        try {
                            m_cms.readFolder(resource);
                            // this resource exists in the current project -> add it
                            resources.addElement(resource);
                        } catch (CmsException exc) {
                            // this resource is missing - we need the root-folder
                            resources.addElement(C_ROOT);
                        }
                    }
                } catch (StringIndexOutOfBoundsException exc) {
                    // this is a resource in root-folder: ignore the excpetion
                }
            }
        } catch (Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            if (m_importingChannelData) m_cms.setContextToVfs();

        }
        if (m_importZip != null) {
            try {
                m_importZip.close();
            } catch (IOException exc) {
                throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
            }
        }
        if (resources.contains(C_ROOT)) {
            // we have to import root - forget the rest!
            resources.removeAllElements();
            resources.addElement(C_ROOT);
        }
        return resources;
    }

    /**
     * Returns the text of a named node
     *
     * @param elem the parent-element
     * @param tag the tagname to get the value from
     * @return the text of a named node
     */
    private String getTextNodeValue(Element elem, String tag) {
        try {
            return elem.getElementsByTagName(tag).item(0).getFirstChild().getNodeValue();
        } catch(Exception exc) {
            // ignore the exception and return null
            return null;
        }
    }
    
    /**
     * Gets the xml-config file from the import resource and stores it in object-member.
     * Checks whether the import is from a module file
     */
    private void getXmlConfigFile() throws CmsException {
        try {
            InputStream in = new ByteArrayInputStream(getFileBytes(C_EXPORT_XMLFILENAME));
            try {
                m_docXml = A_CmsXmlContent.getXmlParser().parse(in);
            } finally {
                try {
                    in.close();
                } catch (Exception e) {}
            }
        } catch (Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Imports a resource (file or folder) into the cms.<p>
     * 
     * @param source the path to the source-file
     * @param destination the path to the destination-file in the cms
     * @param type the resource-type of the file
     * @param user the owner of the file
     * @param group the group of the file
     * @param access the access-flags of the file
     * @param properties a hashtable with properties for this resource
     * @param writtenFilenames filenames of the files and folder which have actually been successfully written
     *       not used when null
     * @param fileCodes code of the written files (for the registry)
     *       not used when null
     */
    private void importResource(String source, String destination, String type, String user, 
    String group, String access, long lastmodified, Map properties, String launcherStartClass, Vector writtenFilenames, Vector fileCodes) {

        boolean success = true;
        byte[] content = null;
        String fullname = null;
                
        try {
            if (m_importingChannelData) m_cms.setContextToCos();
            // get the file content
            if (source != null) {
                content = getFileBytes(source);
            }   
            // check and convert old import files    
            if (m_importVersion < 2) {
             
                // convert content from pre 5.x must be activated
                if ("page".equals(type) || ("plain".equals(type)) || ("XMLTemplate".equals(type))) {
                    if (C_DEBUG > 0){
                        System.err.println("#########################");
                        System.err.println("["+this.getClass().getName()+".importResource()]: starting conversion of \""+type+"\" resource "+source+".");
                    }
                    // change the filecontent for encoding if necessary
                    content = convertFile(source, content);
                }     
                // only check the file type if the version of the export is 0
                if(m_importVersion == 0){
                    // ok, a (very) old system exported this, check if the file is ok
                    if(!(new CmsCompatibleCheck()).isTemplateCompatible(m_importPath + destination, content, type)){
                        type = C_TYPE_COMPATIBLEPLAIN_NAME;
                        m_report.print(m_report.key("report.must_set_to") + C_TYPE_COMPATIBLEPLAIN_NAME + " ", I_CmsReport.C_FORMAT_WARNING);
                    }
                }   
            } 
            // version 2.0 import (since OpenCms 5.0), no content conversion required                        
            
   
            CmsResource res = m_cms.importResource(source, destination, type, user, group, access, lastmodified,
                                        properties, launcherStartClass, content, m_importPath);

            if(res != null){
                fullname = res.getAbsolutePath();
                if(C_TYPE_PAGE_NAME.equals(type)){
                    m_importedPages.add(fullname);
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
            } catch (Exception e) {};
        } finally {
            if (m_importingChannelData) m_cms.setContextToVfs();            
        }
        byte[] digestContent = {0};
        if (content != null) {
            digestContent = m_digest.digest(content);
        }
        if (success && (fullname != null)){
            if (writtenFilenames != null){
                writtenFilenames.addElement(fullname);
            }
            if (fileCodes != null){
                fileCodes.addElement(new String(digestContent));
            }
        }
    }

    /**
     * Imports the resources and writes them to the cms.<p>
     * 
     * @param excludeList filenames of files and folders which should not be (over) written in the virtual file system
     * @param writtenFilenames filenames of the files and folder which have actually been successfully written
     *         not used when null
     * @param fileCodes code of the written files (for the registry)
     *         not used when null
     * @param propertyName name of a property to be added to all resources
     * @param propertyValue value of that property
     */
    public void importResources(Vector excludeList, Vector writtenFilenames, Vector fileCodes, 
    String propertyName, String propertyValue) throws CmsException {
        NodeList fileNodes, propertyNodes;
        Element currentElement, currentProperty;
        String source, destination, type, user, group, access, launcherStartClass, dummy;
        long lastmodified = 0;
        Map properties;
        
        Vector types = new Vector(); // stores the file types for which the property already exists
        if (excludeList == null) {
            excludeList = new Vector();
        }
        
        m_webAppNames = (List)A_OpenCms.getRuntimeProperty("compatibility.support.webAppNames");
        if (m_webAppNames == null)
            m_webAppNames = new ArrayList();

        // get the old webapp url from the OpenCms properties
        m_webappUrl = (String)A_OpenCms.getRuntimeProperty("compatibility.support.import.old.webappurl");
        if (m_webappUrl == null) {
            // use a default value
            m_webappUrl = "http://localhost:8080/opencms/opencms";
        }
        // cut last "/" from webappUrl if present
        if (m_webappUrl.endsWith("/")) {
            m_webappUrl = m_webappUrl.substring(0, m_webappUrl.lastIndexOf("/"));
        }

        // get list of unwanted properties
        List deleteProperties = (List) A_OpenCms.getRuntimeProperty("compatibility.support.import.remove.propertytags");
        if (deleteProperties == null) deleteProperties = new ArrayList();
            
        try {
            // get all file-nodes
            fileNodes = m_docXml.getElementsByTagName(C_EXPORT_TAG_FILE);
            int importSize = fileNodes.getLength();
    
            String root = I_CmsConstants.C_DEFAULT_SITE + I_CmsConstants.C_ROOTNAME_VFS;
    
            // walk through all files in manifest
            for (int i = 0; i < fileNodes.getLength(); i++) {
    
                m_report.print(" ( " + (i+1) + " / " + importSize + " ) ");
                currentElement = (Element) fileNodes.item(i);
    
                // get all information for a file-import
                source = getTextNodeValue(currentElement, C_EXPORT_TAG_SOURCE);
                destination = getTextNodeValue(currentElement, C_EXPORT_TAG_DESTINATION);
                type = getTextNodeValue(currentElement, C_EXPORT_TAG_TYPE);
                user = getTextNodeValue(currentElement, C_EXPORT_TAG_USER);
                group = getTextNodeValue(currentElement, C_EXPORT_TAG_GROUP);
                access = getTextNodeValue(currentElement, C_EXPORT_TAG_ACCESS);
                launcherStartClass = getTextNodeValue(currentElement, C_EXPORT_TAG_LAUNCHER_START_CLASS);
                
                if ((dummy=getTextNodeValue(currentElement,C_EXPORT_TAG_LASTMODIFIED))!=null) {
                    lastmodified = Long.parseLong(dummy);
                }
                else {
                    lastmodified = System.currentTimeMillis();
                }                
                
                // if the type is "script" set it to plain
                if("script".equals(type)){
                    type = C_TYPE_PLAIN_NAME;
                }
                
                String translatedName = root + m_importPath + destination;
                if (C_TYPE_FOLDER_NAME.equals(type)) {
                    translatedName += C_FOLDER_SEPARATOR;                    
                }                    
                translatedName = m_cms.getRequestContext().getDirectoryTranslator().translateResource(translatedName);
                translatedName = translatedName.substring(root.length());
                
                if (! excludeList.contains(translatedName)) {                    
                    // print out the information to the report
                    m_report.print(m_report.key("report.importing"), I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(translatedName + " ");                    
                        
                    // get all properties for this file
                    propertyNodes = currentElement.getElementsByTagName(C_EXPORT_TAG_PROPERTY);
                    // clear all stores for property information
                    properties = new HashMap();
                    // add the module property to properties
                    if (propertyName != null && propertyValue != null && !"".equals(propertyName)) {
                        if (!types.contains(type)) {
                            types.addElement(type);
                            createPropertydefinition(propertyName, type);
                        }
                        properties.put(propertyName, propertyValue);
                    }
                    // walk through all properties
                    for (int j = 0; j < propertyNodes.getLength(); j++) {
                        currentProperty = (Element) propertyNodes.item(j);
                        // get name information for this property
                        String name = getTextNodeValue(currentProperty, C_EXPORT_TAG_NAME);
						// check if this is an unwanted property
						if ((name != null) && (!deleteProperties.contains(name))) {
                            // get value information for this property
							String value = getTextNodeValue(currentProperty, C_EXPORT_TAG_VALUE);
							if (value == null) {
								// create an empty property
								value = "";
							}
							// add property
							properties.put(name, value);
							createPropertydefinition(name, type);
						}
                    }
    
                    // import the specified file 
                    importResource(source, destination, type, user, group, access, lastmodified, properties, launcherStartClass, writtenFilenames, fileCodes);
                } else {
                    // skip the file import, just print out the information to the report
                    m_report.print(m_report.key("report.skipping"), I_CmsReport.C_FORMAT_NOTE);
                    m_report.println(translatedName);
                }
            }
            if (!m_importingChannelData) {
                // at last we have to get the links from all new imported pages for the  linkmanagement
                m_report.println(m_report.key("report.check_links_begin"), I_CmsReport.C_FORMAT_HEADLINE);
                updatePageLinks();
                m_report.println(m_report.key("report.check_links_end"), I_CmsReport.C_FORMAT_HEADLINE);
                m_cms.joinLinksToTargets(m_report);
            }  
        } catch (Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
        if (m_importZip != null) {
            try {
                m_importZip.close();
            } catch (IOException exc) {
                throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
            }
        }
    }

    /**
     * Imports the groups and writes them to the cms.<p>
     */
    private void importGroups() throws CmsException{
        NodeList groupNodes;
        Element currentElement;
        String name, description, flags, parentgroup;

        try{
            // getAll group nodes
            groupNodes = m_docXml.getElementsByTagName(C_EXPORT_TAG_GROUPDATA);
            // walk threw all groups in manifest
            for (int i = 0; i < groupNodes.getLength(); i++) {
                currentElement = (Element)groupNodes.item(i);
                name = getTextNodeValue(currentElement, C_EXPORT_TAG_NAME);
                description = getTextNodeValue(currentElement, C_EXPORT_TAG_DESCRIPTION);
                flags = getTextNodeValue(currentElement, C_EXPORT_TAG_FLAGS);
                parentgroup = getTextNodeValue(currentElement, C_EXPORT_TAG_PARENTGROUP);
                // import this group
                importGroup(name, description, flags, parentgroup);
            }

            // now try to import the groups in the stack
            while (!m_groupsToCreate.empty()){
                Stack tempStack = m_groupsToCreate;
                m_groupsToCreate = new Stack();
                while (tempStack.size() > 0){
                    Hashtable groupdata = (Hashtable)tempStack.pop();
                    name = (String)groupdata.get(C_EXPORT_TAG_NAME);
                    description = (String)groupdata.get(C_EXPORT_TAG_DESCRIPTION);
                    flags = (String)groupdata.get(C_EXPORT_TAG_FLAGS);
                    parentgroup = (String)groupdata.get(C_EXPORT_TAG_PARENTGROUP);
                    // try to import the group
                    importGroup(name, description, flags, parentgroup);
                }
            }
        } catch (Exception exc){
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
     }

    /**
     * Imports the users and writes them to the cms.<p>
     */
    private void importUsers() throws CmsException{
        NodeList userNodes;
        NodeList groupNodes;
        Element currentElement, currentGroup;
        Vector userGroups;
        Hashtable userInfo = new Hashtable();
        sun.misc.BASE64Decoder dec;
        String name, description, flags, password, recoveryPassword, firstname,
                lastname, email, address, section, defaultGroup, type, pwd, infoNode;
        // try to get the import resource
        getImportResource();
        try{
            // getAll user nodes
            userNodes = m_docXml.getElementsByTagName(C_EXPORT_TAG_USERDATA);
            // walk threw all groups in manifest
            for (int i = 0; i < userNodes.getLength(); i++) {
                currentElement = (Element)userNodes.item(i);
                name = getTextNodeValue(currentElement, C_EXPORT_TAG_NAME);
                // decode passwords using base 64 decoder
                dec = new sun.misc.BASE64Decoder();
                pwd = getTextNodeValue(currentElement, C_EXPORT_TAG_PASSWORD);
                password = new String(dec.decodeBuffer(pwd.trim()));
                dec = new sun.misc.BASE64Decoder();
                pwd = getTextNodeValue(currentElement, C_EXPORT_TAG_RECOVERYPASSWORD);
                recoveryPassword = new String(dec.decodeBuffer(pwd.trim()));

                description = getTextNodeValue(currentElement, C_EXPORT_TAG_DESCRIPTION);
                flags = getTextNodeValue(currentElement, C_EXPORT_TAG_FLAGS);
                firstname = getTextNodeValue(currentElement, C_EXPORT_TAG_FIRSTNAME);
                lastname = getTextNodeValue(currentElement, C_EXPORT_TAG_LASTNAME);
                email = getTextNodeValue(currentElement, C_EXPORT_TAG_EMAIL);
                address = getTextNodeValue(currentElement, C_EXPORT_TAG_ADDRESS);
                section = getTextNodeValue(currentElement, C_EXPORT_TAG_SECTION);
                defaultGroup = getTextNodeValue(currentElement, C_EXPORT_TAG_DEFAULTGROUP);
                type = getTextNodeValue(currentElement, C_EXPORT_TAG_TYPE);
                // get the userinfo and put it into the hashtable
                infoNode = getTextNodeValue(currentElement,C_EXPORT_TAG_USERINFO);
                try{
                    // read the userinfo from the dat-file
                    byte[] value = getFileBytes(infoNode);
                    // deserialize the object
                    ByteArrayInputStream bin= new ByteArrayInputStream(value);
                    ObjectInputStream oin = new ObjectInputStream(bin);
                    userInfo = (Hashtable)oin.readObject();
                } catch (IOException ioex){
                    m_report.println(ioex);
                }

                // get the groups of the user and put them into the vector
                groupNodes = currentElement.getElementsByTagName(C_EXPORT_TAG_GROUPNAME);
                userGroups = new Vector();
                for (int j=0; j < groupNodes.getLength(); j++){
                    currentGroup = (Element) groupNodes.item(j);
                    userGroups.addElement(getTextNodeValue(currentGroup, C_EXPORT_TAG_NAME));
                }
                // import this group
                importUser(name, description, flags, password, recoveryPassword, firstname,
                lastname, email, address, section, defaultGroup, type, userInfo, userGroups);
            }
        } catch (Exception exc){
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Creates an imported group in the cms.<p>
     * 
     * @param name the name of the group
     * @param description group description
     * @param flags group flags
     * @param parentgroupName name of the parent group
     */
    private void importGroup(String name, String description, String flags, String parentgroupName)
        throws CmsException{
        if(description == null) {
            description = "";
        }
        CmsGroup parentGroup = null;
        try{
            if ((parentgroupName != null) && (!"".equals(parentgroupName))) {
                try{
                    parentGroup = m_cms.readGroup(parentgroupName);
                } catch(CmsException exc){
                }
            }
            if (((parentgroupName != null) && (!"".equals(parentgroupName))) && (parentGroup == null)){
                // cannot create group, put on stack and try to create later
                Hashtable groupData = new Hashtable();
                groupData.put(C_EXPORT_TAG_NAME, name);
                    groupData.put(C_EXPORT_TAG_DESCRIPTION, description);
                groupData.put(C_EXPORT_TAG_FLAGS, flags);
                groupData.put(C_EXPORT_TAG_PARENTGROUP, parentgroupName);
                m_groupsToCreate.push(groupData);
            } else {
                try{
                    m_report.print(m_report.key("report.importing_group"), I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(name);     
                    m_report.print(m_report.key("report.dots"), I_CmsReport.C_FORMAT_NOTE);                                   
                    m_cms.addGroup(name, description, Integer.parseInt(flags), parentgroupName);
                    m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
                } catch (CmsException exc){
                    m_report.println(m_report.key("report.not_created"), I_CmsReport.C_FORMAT_OK);
                }
            }
        } catch (Exception exc){
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }
    
    /**
     * Creates an imported user in the cms.<p>
     * 
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
    private void importUser(String name, String description, String flags, String password,
                            String recoveryPassword, String firstname, String lastname,
                            String email, String address, String section, String defaultGroup,
                            String type, Hashtable userInfo, Vector userGroups)
        throws CmsException{
        try{
            try{
                m_report.print(m_report.key("report.importing_user"), I_CmsReport.C_FORMAT_NOTE);
                m_report.print(name);
                m_report.print(m_report.key("report.dots"), I_CmsReport.C_FORMAT_NOTE);                
                m_cms.addImportUser(name, password, recoveryPassword, description, firstname,
                                    lastname, email, Integer.parseInt(flags), userInfo, defaultGroup, address,
                                    section, Integer.parseInt(type));
                // add user to all groups vector
                for (int i = 0; i < userGroups.size(); i++) {
                    try {
                        m_cms.addUserToGroup(name, (String) userGroups.elementAt(i));
                    } catch (CmsException exc) {}
                }
                m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
            } catch (CmsException exc){
                m_report.println(m_report.key("report.not_created"), I_CmsReport.C_FORMAT_OK);
            }
        } catch (Exception exc){
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Checks all new imported pages and create or updates the entrys in the
     * database for the linkmanagement.<p>
     */
    private void updatePageLinks(){
        int importPagesSize = m_importedPages.size();
        for(int i=0; i<importPagesSize; i++){
            m_report.print(" ( " + (i+1) + " / " + importPagesSize + " ) ");
            try{
                // first parse the page
                CmsPageLinks links = m_cms.getPageLinks((String)m_importedPages.elementAt(i));
                m_report.print(m_report.key("report.checking_page"), I_CmsReport.C_FORMAT_NOTE);
                m_report.println((String)m_importedPages.elementAt(i));
                // now save the result in the database
                m_cms.createLinkEntrys(links.getResourceId(), links.getLinkTargets());
            } catch(CmsException e){
                m_report.println(e);
                // m_report.println(m_report.key("report.problems_with") + m_importedPages.elementAt(i) + ": " + e.getMessage());
            }
        }
    }
    
	/**
	 * Converts the content of a file from OpenCms 4.x versions.<p>
	 * 
	 * @param filename the name of the file to convert
	 * @param byteContent the content of the file
     * @param type the type of the file
	 * @return the converted filecontent
	 */
    private byte[] convertFile(String filename, byte[] byteContent) {
        byte[] returnValue = byteContent;
        if (!filename.startsWith("/")) {
            filename = "/" + filename;
        }

        String fileContent = new String(byteContent);
        String encoding = getEncoding(fileContent);
        if (!"".equals(encoding)) {
            // encoding found, ensure that the String is correct
            try {
                // get content of the file and store it in String with the correct encoding
                fileContent = new String(byteContent, encoding);
            } catch (UnsupportedEncodingException e) {
                // encoding not supported, we use the default and hope we are lucky
                if (C_DEBUG > 0){
                    System.err.println("["+this.getClass().getName()+".convertFile()]: Encoding not supported, using default encoding.");
                }
            }
        } else {
            // encoding not found, set encoding of xml files to default
            if (C_DEBUG > 0){
                System.err.println("["+this.getClass().getName()+".convertFile()]: Encoding not set, using default encoding and setting it in <?xml...?>.");
            }
            encoding = OpenCms.getDefaultEncoding();
            fileContent = setEncoding(fileContent, encoding);
        }
        // check the frametemplates
        if (filename.indexOf("frametemplates") != -1) {
            fileContent = scanFrameTemplate(fileContent);
        }    
        // scan content/bodys
        if (filename.indexOf(C_VFS_PATH_OLD_BODIES) != -1
            || filename.indexOf(I_CmsWpConstants.C_VFS_PATH_BODIES) != -1) {
            if (C_DEBUG > 0){
                System.err.println("["+this.getClass().getName()+".convertFile()]: Starting scan of body page.");
            }
            fileContent = convertPageBody(fileContent, filename);
        }
        // translate OpenCms 4.x paths to the new directory structure 
        fileContent = setDirectories(fileContent, m_cms.getRequestContext().getDirectoryTranslator().getTranslations());
        // create output ByteArray
        try {
            returnValue = fileContent.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            // encoding not supported, we use the default and hope we are lucky
            returnValue = fileContent.getBytes();
        }
        return returnValue;
    }
    
    /**
     * Gets the encoding from the &lt;?XML ...&gt; tag if present.<p>
     * 
     * @param content the file content
     * @return String the found encoding
     */
	private String getEncoding(String content) {
		String encoding = content;
		int index = encoding.toLowerCase().indexOf("encoding=\"");
		// encoding attribute found, get the value
		if (index != -1) {
			encoding = encoding.substring(index + 10);
			if ((index = encoding.indexOf("\"")) != -1) {
				encoding = encoding.substring(0, index);
				return encoding.toUpperCase();
			}
		}
		// no encoding attribute found
		return "";
	}	
    
    /** 
     * Scans the given content of a frametemplate and returns the result.<p>
     *
     * @param content the filecontent
     * @return modified content
     */
    private String scanFrameTemplate(String content) {
        // no Meta-Tag present, insert it!
        if (content.toLowerCase().indexOf("http-equiv=\"content-type\"") == -1) {
            content = replaceString(content,"</head>","<meta http-equiv=\"content-type\" content=\"text/html; charset=]]><method name=\"getEncoding\"/><![CDATA[\">\n</head>");
        }
        // Meta-Tag present
        else {
        	if(content.toLowerCase().indexOf("charset=]]><method name=\"getencoding\"/>") == -1){
            	String fileStart = content.substring(0,content.toLowerCase().indexOf("charset=")+8);
            	String editContent = content.substring(content.toLowerCase().indexOf("charset="));
            	editContent = editContent.substring(editContent.indexOf("\""));
            	String newEncoding = "]]><method name=\"getEncoding\"/><![CDATA[";
            	content = fileStart + newEncoding + editContent;
        	}
        }
        return content;
    }
    
    /** 
     * Sets the right encoding and returns the result.<p>
     * 
     * @param content the filecontent
     * @param encoding the encoding to use
     * @return modified content
     */
    private String setEncoding(String content, String encoding) {
        if (content.toLowerCase().indexOf("<?xml") == -1) {
            return content;
        }
        // XML information present, replace encoding
        else {
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
     * Translates directory Strings from OpenCms 4.x structure to new 5.0 structure.<p>
     * 
     * @param content the filecontent
     * @param type the file type
     * @return String the manipulated file content
     */
    public static String setDirectories(String content, String[] rules) {
        // get translation rules
        String root = I_CmsConstants.C_DEFAULT_SITE + I_CmsConstants.C_ROOTNAME_VFS;   
        for (int i=0; i<rules.length; i++) {
            String actRule = rules[i];
            // cut String "/default/vfs/" from rule
            actRule = CmsStringSubstitution.substitute(actRule, root, "");
            // divide rule into search and replace parts and delete regular expressions
            StringTokenizer ruleT = new StringTokenizer(actRule, "#");
            ruleT.nextToken();
            String search = ruleT.nextToken();
            search = search.substring(0, search.lastIndexOf("(.*)"));
            String replace = ruleT.nextToken();
            replace = replace.substring(0, replace.lastIndexOf("$1"));
            // scan content for paths if the replace String is not present
            if (content.indexOf(replace) == -1 && content.indexOf(search) != -1) {
                // ensure subdirectories of the same name are not replaced
                search = "([}>\"'\\[]\\s*)" + search;
                replace = "$1" + replace;
                content = CmsStringSubstitution.substitutePerl(content, search, replace, "g");
            }
        }
        return content;
    }
    
    /**
     * Searches for the webapps String and replaces it with a macro which is needed for the WYSIWYG editor,
     * also creates missing &lt;edittemplate&gt; tags for exports of older OpenCms 4.x versions.<p>
     * 
     * @param content the filecontent 
     * @return String the modified filecontent
     */
    private String convertPageBody(String content, String fileName) {
        // variables needed for the creation of <template> elements
        boolean createTemplateTags = false;
        Hashtable templateElements = new Hashtable();
        // first check if any contextpaths are in the content String
        boolean found = false;
        for (int i = 0; i < m_webAppNames.size(); i++) {
            if (content.indexOf((String)m_webAppNames.get(i)) != -1) {
                found = true;
            }
        }
        // check if edittemplates are in the content string
        if (content.indexOf("<edittemplate>") != -1) {
            found = true;
        }
        // only build document when some paths were found or <edittemplate> is missing!
        if (found == true) {
            InputStream in = new ByteArrayInputStream(content.getBytes());
            String editString, templateString;
            try {
                // create DOM document
                Document contentXml = A_CmsXmlContent.getXmlParser().parse(in);
                // get all <edittemplate> nodes to check their content
                NodeList editNodes = contentXml.getElementsByTagName("edittemplate");
                // no <edittemplate> tags present, create them!
                if (editNodes.getLength() < 1) {
                    if (C_DEBUG > 0){
                        System.err.println("["+this.getClass().getName()+".convertPageBody()]: No <edittemplate> found, creating it.");
                    }
                    createTemplateTags = true;
                    NodeList templateNodes = contentXml.getElementsByTagName("TEMPLATE");
                    // create an <edittemplate> tag for each <template> tag
                    for (int i = 0; i < templateNodes.getLength(); i++) {
                        // get the CDATA content of the <template> tags
                        editString = templateNodes.item(i).getFirstChild().getNodeValue();
                        templateString = editString;
                        // substitute the links in the <template> tag String
                        try {
                            LinkSubstitution sub = new LinkSubstitution();
                            templateString = sub.substituteContentBody(templateString, m_webappUrl, fileName);
                        } catch (CmsException e) {
                            throw new CmsException("[" + this.getClass().getName() + ".convertPageBody()] can't parse the content: ", e);
                        }
                        // look for the "name" attribute of the <template> tag
                        NamedNodeMap attrs = templateNodes.item(i).getAttributes();
                        String templateName = "";
                        if (attrs.getLength() > 0) {
                            templateName = attrs.item(0).getNodeValue();
                        }
                        // create the new <edittemplate> node                       
                        Element newNode = contentXml.createElement("edittemplate");
                        CDATASection newText = contentXml.createCDATASection(editString);
                        newNode.appendChild(newText);
                        // set the "name" attribute, if necessary
                        attrs = newNode.getAttributes();
                        if (!templateName.equals("")) {
                            newNode.setAttribute("name", templateName);
                        }
                        // append the new edittemplate node to the document
                        contentXml.getElementsByTagName("XMLTEMPLATE").item(0).appendChild(newNode);
                        // store modified <template> node Strings in Hashtable
                        if (templateName.equals("")) {
                            templateName = "noNameKey";
                        }
                        templateElements.put(templateName, templateString);
                    }
                    // finally, delete old <TEMPLATE> tags from document
                    while (templateNodes.getLength() > 0) {
                        contentXml.getElementsByTagName("XMLTEMPLATE").item(0).removeChild(templateNodes.item(0));
                    }
                }
                // check the content of the <edittemplate> nodes
                for (int i = 0; i < editNodes.getLength(); i++) {
                    editString = editNodes.item(i).getFirstChild().getNodeValue();
                    for (int k = 0; k < m_webAppNames.size(); k++) {
                        editString =
                            CmsStringSubstitution.substitute(editString, (String)m_webAppNames.get(k), C_MACRO_OPENCMS_CONTEXT + "/");
                    }
                    editNodes.item(i).getFirstChild().setNodeValue(editString);
                }
                // convert XML document back to String
                CmsXmlXercesParser parser = new CmsXmlXercesParser();
                Writer out = new StringWriter();
                parser.getXmlText(contentXml, out);
                content = out.toString();
                // rebuild the template tags in the document!
                if (createTemplateTags) {
                    content = content.substring(0, content.lastIndexOf("</XMLTEMPLATE>"));
                    // get the keys
                    Enumeration enum = templateElements.keys();
                    while (enum.hasMoreElements()) {
                        String key = (String)enum.nextElement();
                        String value = (String)templateElements.get(key);
                        // create the default template
                        if (key.equals("noNameKey")) {
                            content += "\n<TEMPLATE><![CDATA[" + value;
                        }
                        // create template with "name" attribute
                        else {
                            content += "\n<TEMPLATE name=\"" + key + "\"><![CDATA[" + value;
                        }
                        content += "]]></TEMPLATE>\n";
                    }
                    content += "\n</XMLTEMPLATE>";
                }

            } catch (Exception exc) {}
        }
        return content;
    }  
       
    /**
     * Method to replace a subString with replaceItem.<p>
     * 
     * @param testString the original String
     * @param searchString the subString that has to be replaced
     * @param replaceItem the String that replaces searchString
     * @return String with replaced subStrings
     */
    protected static String replaceString(String testString, String searchString, String replaceItem) {
    	// if searchString isn't in testString, return (better performance) 
        if (testString.toLowerCase().indexOf(searchString.toLowerCase()) == -1) {
        	return testString;
        }
        int tempIndex = 0;
        int searchLen = searchString.length();
        int searchIndex = testString.toLowerCase().indexOf(searchString.toLowerCase());
        StringBuffer returnString = new StringBuffer(testString.length());
        while (searchIndex != -1) {
            returnString.append(testString.substring(0,searchIndex));
            returnString.append(replaceItem);
            tempIndex = searchIndex+searchLen;
            testString = testString.substring(tempIndex);
            searchIndex = testString.toLowerCase().indexOf(searchString.toLowerCase());
        }
        returnString.append(testString);
        return returnString.toString();
    }
}

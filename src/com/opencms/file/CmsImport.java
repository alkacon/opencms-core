/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsImport.java,v $
* Date   : $Date: 2002/10/18 16:54:59 $
* Version: $Revision: 1.56 $
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

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.lang.reflect.*;
import java.security.*;
import com.opencms.boot.*;
import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.util.Utils;
import com.opencms.linkmanagement.*;
import com.opencms.report.*;
import org.w3c.dom.*;
import source.org.apache.java.util.*;

/**
 * This class holds the functionaility to import resources from the filesystem
 * into the cms.
 *
 * @author Andreas Schouten
 * @version $Revision: 1.56 $ $Date: 2002/10/18 16:54:59 $
 */
public class CmsImport implements I_CmsConstants, Serializable {

    /**
     * The algorithm for the message digest
     */
    public static final String C_IMPORT_DIGEST="MD5";

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
     *
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

    /**
     * This constructs a new CmsImport-object which imports the resources.
     *
     * @param importFile the file or folder to import from.
     * @param importPath the path to the cms to import into.
     * @param report A report object to provide the loggin messages.
     * @exception CmsException the CmsException is thrown if something goes wrong.
     */
    public CmsImport(String importFile, String importPath, CmsObject cms, I_CmsReport report)
        throws CmsException {

        m_importFile = importFile;
        m_importPath = importPath;
        m_cms = cms;
        m_report=report;

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
 * Read infos from the properties and create a MessageDigest
 * Creation date: (29.08.00 15:45:35)
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
     * Creates missing property definitions if needed.
     *
     * @param name the name of the property.
     * @param propertyType the type of the property.
     * @param resourceType the type of the resource.
     *
     * @exception throws CmsException if something goes wrong.
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
     * Checks if the file sticks to the rules for files in the content path.
     * If not, it sets the type of the file to compatible_plain.
     * This is for exports of older versions of OpenCms. The imported files
     * will work as befor, but they cant be edited.
     *
     * @param name The name of the resource including path that is imported
     * @param content the content of the resource.
     * @param type the type of the resourse, is set to compatible_plain if nessesary.
     * @param properties the properties, not yet used here.
     * @return the new type of the resouce
     */
    private String fitFileType(String name, byte[] content, String type, Hashtable properties){

        // only check the file if the version of the export is 0
        if(m_importVersion == 0){
            // ok, an old system exported this, check if the file is ok
            if(!(new CmsCompatibleCheck()).isTemplateCompatible(name, content, type)){
                type = C_TYPE_COMPATIBLEPLAIN_NAME;
                m_report.addString(" must set to "+C_TYPE_COMPATIBLEPLAIN_NAME+" ");
            }
        }
        return type;
    }

/**
 * Returns a list of files which are both in the import and in the virtual file system
 * Creation date: (24.08.00 16:18:23)
 * @return java.util.Vector of Strings, complete path of the files
 */
public Vector getConflictingFilenames() throws CmsException {
    NodeList fileNodes;
    Element currentElement, currentProperty;
    String source, destination, path;
    Vector conflictNames = new Vector();
    try {
        // get all file-nodes
        fileNodes = m_docXml.getElementsByTagName(C_EXPORT_TAG_FILE);

        // walk through all files in manifest
        for (int i = 0; i < fileNodes.getLength(); i++) {
            currentElement = (Element) fileNodes.item(i);
            source = getTextNodeValue(currentElement, C_EXPORT_TAG_SOURCE);
            destination = getTextNodeValue(currentElement, C_EXPORT_TAG_DESTINATION);
            path = m_importPath + destination;
            if (source != null) {
                // only consider files
                boolean exists = true;
                try {
                    CmsResource res=m_cms.readFileHeader(m_importPath + destination);
                    if (res.getState()==C_STATE_DELETED) {
                        exists=false;
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
    if (m_importZip != null)
    {
      try
      {
          m_importZip.close();
      } catch (IOException exc) {
          throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
      }
    }
    return conflictNames;
}
    /**
     * Returns a byte-array containing the content of the file.
     *
     * @param filename The name of the file to read.
     * @return bytes[] The content of the file.
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
     * Returns a buffered reader for this resource using the importFile as root.
     *
     * @param filename The name of the file to read.
     * @return BufferedReader The filereader for this file.
     */
    private BufferedReader getFileReader(String filename)
        throws Exception{
        // is this a zip-file?
        if(m_importZip != null) {
            // yes
            ZipEntry entry = m_importZip.getEntry(filename);
            InputStream stream = m_importZip.getInputStream(entry);
            return new BufferedReader( new InputStreamReader(stream));
        } else {
            // no - use directory
            File xmlFile = new File(m_importResource, filename);
            return new BufferedReader(new FileReader(xmlFile));
        }
    }
    /**
     * Gets the import resource and stores it in object-member.
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
 * This method returns the resource-names that are needed to create a project for this import.
 * It calls the method getConflictingFileNames if needed, to calculate these resources.
 */
public Vector getResourcesForProject() throws CmsException {
    NodeList fileNodes;
    Element currentElement, currentProperty;
    String source, destination, path;
    Vector resources = new Vector();
    try {
        // get all file-nodes
        fileNodes = m_docXml.getElementsByTagName(C_EXPORT_TAG_FILE);
        // walk through all files in manifest
        for (int i = 0; i < fileNodes.getLength(); i++) {
            currentElement = (Element) fileNodes.item(i);
            source = getTextNodeValue(currentElement, C_EXPORT_TAG_SOURCE);
            destination = getTextNodeValue(currentElement, C_EXPORT_TAG_DESTINATION);
            path = m_importPath + destination;

            // get the resources for a project
            try {
                String resource = destination.substring(0, destination.indexOf("/",1) + 1);
                resource = m_importPath + resource;
                // add the resource, if it dosen't already exist
                if((!resources.contains(resource)) && (!resource.equals(m_importPath))) {
                    try {
                        m_cms.readFolder(resource);
                        // this resource exists in the current project -> add it
                        resources.addElement(resource);
                    } catch(CmsException exc) {
                        // this resource is missing - we need the root-folder
                        resources.addElement(C_ROOT);
                    }
                }
            } catch(StringIndexOutOfBoundsException exc) {
                // this is a resource in root-folder: ignore the excpetion
            }
        }
    } catch (Exception exc) {
        throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
    }
    if (m_importZip != null)
    {
      try
      {
          m_importZip.close();
      } catch (IOException exc) {
          throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
      }
    }
    if(resources.contains(C_ROOT)) {
        // we have to import root - forget the rest!
        resources.removeAllElements();
        resources.addElement(C_ROOT);
    }
    return resources;
}
    /**
     * Returns the text for this node.
     *
     * @param elem the parent-element.
     * @param tag the tagname to get the value from.
     * @return the value of the tag.
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
    /**private void getXmlConfigFile()
        throws CmsException {

        try {
            BufferedReader xmlReader = getFileReader(C_EXPORT_XMLFILENAME);
            m_docXml = A_CmsXmlContent.getXmlParser().parse(xmlReader);
            xmlReader.close();
         } catch(Exception exc) {

            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }*/


      private void getXmlConfigFile() throws CmsException {
         try {
             //Gridnine AB Sep 3, 2002
             /*
             BufferedReader xmlReader =getFileReader(C_EXPORT_XMLFILENAME);
             m_docXml = A_CmsXmlContent.getXmlParser().parse(xmlReader);
             xmlReader.close();
             */
             InputStream in = new ByteArrayInputStream(getFileBytes(C_EXPORT_XMLFILENAME));
             try {
                 m_docXml = A_CmsXmlContent.getXmlParser().parse(in);
             } finally {
                 try {
                     in.close();
                 } catch (Exception e) {}
             }
          } catch(Exception exc) {
             throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
         }
     }



/**
 * Imports a resource (file or folder) into the cms.
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
private void importResource(String source, String destination, String type, String user, String group, String access, Hashtable properties, String launcherStartClass, Vector writtenFilenames, Vector fileCodes) {
    // print out the information for shell-users
    m_report.addString("Importing " + destination + " ");
    boolean success = true;
    byte[] content = null;
    String fullname = null;
    try {
        if (source != null){
            content = getFileBytes(source);
        }
        // set invalid files to type compatible_plain
        type = fitFileType(m_importPath + destination, content, type, properties);

        CmsResource res = m_cms.importResource(source, destination, type, user, group, access,
                                    properties, launcherStartClass, content, m_importPath);
        if(res != null){
            fullname = res.getAbsolutePath();
            if(C_TYPE_PAGE_NAME.equals(type)){
                m_importedPages.add(fullname);
            }
        }
        m_report.addString("OK");
        m_report.addSeperator(0);
    } catch (Exception exc) {
        // an error while importing the file
        success = false;
        m_report.addString("Error: " + exc.toString() + Utils.getStackTrace(exc));
        m_report.addSeperator(0); 
        try {
            // Sleep some time after an error so that the report output has a chance to keep up
            Thread.sleep(1000);   
        } catch (Exception e) {};
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
 * Imports the resources and writes them to the cms even if there already exist conflicting files
 */
public void importResources() throws CmsException {
    if (m_cms.isAdmin()){
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
 * Imports the resources and writes them to the cms.
 * param excludeList filenames of files and folders which should not be (over) written in the virtual file system
 * param writtenFilenames filenames of the files and folder which have actually been successfully written
 *       not used when null
 * param fileCodes code of the written files (for the registry)
 *       not used when null
 * param propertyName name of a property to be added to all resources
 * param propertyValue value of that property
 */
public void importResources(Vector excludeList, Vector writtenFilenames, Vector fileCodes, String propertyName, String propertyValue) throws CmsException {
    NodeList fileNodes, propertyNodes;
    Element currentElement, currentProperty;
    String source, destination, type, user, group, access, launcherStartClass;
    Hashtable properties;
    Vector types = new Vector(); // stores the file types for which the property already exists

    // first lock the resource to import
    // m_cms.lockResource(m_importPath);
    try {
        // get all file-nodes
        fileNodes = m_docXml.getElementsByTagName(C_EXPORT_TAG_FILE);
        int importSize = fileNodes.getLength();

        // walk through all files in manifest
        for (int i = 0; i < fileNodes.getLength(); i++) {

            m_report.addString(" ( "+(i+1)+" / "+importSize+" )  ");
            currentElement = (Element) fileNodes.item(i);

            // get all information for a file-import
            source = getTextNodeValue(currentElement, C_EXPORT_TAG_SOURCE);
            destination = getTextNodeValue(currentElement, C_EXPORT_TAG_DESTINATION);
            type = getTextNodeValue(currentElement, C_EXPORT_TAG_TYPE);
            user = getTextNodeValue(currentElement, C_EXPORT_TAG_USER);
            group = getTextNodeValue(currentElement, C_EXPORT_TAG_GROUP);
            access = getTextNodeValue(currentElement, C_EXPORT_TAG_ACCESS);
            launcherStartClass = getTextNodeValue(currentElement, C_EXPORT_TAG_LAUNCHER_START_CLASS);
            // if the type is javascript set it to plain
            if("script".equals(type)){
                type = C_TYPE_PLAIN_NAME;
            }
            if (!inExcludeList(excludeList, m_importPath + destination)) {

                // get all properties for this file
                propertyNodes = currentElement.getElementsByTagName(C_EXPORT_TAG_PROPERTY);
                // clear all stores for property information
                properties = new Hashtable();
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
                    // get all information for this property
                    String name = getTextNodeValue(currentProperty, C_EXPORT_TAG_NAME);
                    String value = getTextNodeValue(currentProperty, C_EXPORT_TAG_VALUE);
                    if(value == null) {
                        // create an empty property
                        value = "";
                    }
                    // store these informations
                    if ((name != null) && (value != null)) {
                        properties.put(name, value);
                        createPropertydefinition(name, type);
                    }
                }

                // import the specified file and write maybe put it on the lists writtenFilenames,fileCodes
                importResource(source, destination, type, user, group, access, properties, launcherStartClass, writtenFilenames, fileCodes);
            } else {
                m_report.addString("skipping " + destination);
            }
        }
        // at last we have to get the links from all new imported pages for the  linkmanagement
        m_report.addSeperator(1);
        updatePageLinks();
    } catch (Exception exc) {
        throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
    }
    if (m_importZip != null)
    {
      try
      {
          m_importZip.close();
      } catch (IOException exc) {
          throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
      }
    }
}
/**
 * Checks whether the path is on the list of files which are excluded from the import
 *
 * @return boolean true if path is on the excludeList
 * @param excludeList list of pathnames which should not be (over) written
 * @param path a complete path of a resource
 */
private boolean inExcludeList(Vector excludeList, String path) {
    boolean onList = false;
    if (excludeList == null) {
        return onList;
    }
    int i=0;
    while (!onList && i<excludeList.size()) {
        onList = (path.equals(excludeList.elementAt(i)));
        i++;
    }
    return onList;
}

    /**
     * Imports the groups and writes them to the cms.
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
     * Imports the users and writes them to the cms.
     */
    private void importUsers() throws CmsException{
        NodeList userNodes;
        NodeList groupNodes;
        Element currentElement, currentGroup, currentInfo;
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
                    m_report.addString(ioex.getMessage());
                    m_report.addSeperator(0);
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
     * Writes the group to the cms.
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
                    m_report.addString("Importing Group: "+name+" ...");
                    m_cms.addGroup(name, description, Integer.parseInt(flags), parentgroupName);
                    m_report.addString("OK");
                    m_report.addSeperator(0);
                } catch (CmsException exc){
                    m_report.addString("not created");
                    m_report.addSeperator(0);
                }
            }
        } catch (Exception exc){
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }
    /**
     * Writes the group to the cms.
     */
    private void importUser(String name, String description, String flags, String password,
                            String recoveryPassword, String firstname, String lastname,
                            String email, String address, String section, String defaultGroup,
                            String type, Hashtable userInfo, Vector userGroups)
        throws CmsException{
        CmsUser newUser = null;
        int defGroupId = C_UNKNOWN_ID;
        try{
            try{
                m_report.addString("Importing User: "+name+" ...");
                newUser = m_cms.addImportUser(name, password, recoveryPassword, description, firstname,
                                    lastname, email, Integer.parseInt(flags), userInfo, defaultGroup, address,
                                    section, Integer.parseInt(type));
                // add user to all groups vector
                for (int i=0; i < userGroups.size(); i++){
                    try{
                        m_cms.addUserToGroup(name, (String)userGroups.elementAt(i));
                    } catch (CmsException exc){
                    }
                }
                m_report.addString("OK");
                m_report.addSeperator(0);
            } catch (CmsException exc){
                m_report.addString("not created");
                m_report.addSeperator(0);
            }
        } catch (Exception exc){
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * checks all new imported pages and create/updates the entrys in the
     * database for the linkmanagement.
     */
    private void updatePageLinks(){
        LinkChecker checker = new LinkChecker();
        int importPagesSize = m_importedPages.size();
        for(int i=0; i<importPagesSize; i++){
            m_report.addString(" ( "+(i+1)+" / "+importPagesSize+" )  ");
            try{
                // first parse the page
                CmsPageLinks links = m_cms.getPageLinks((String)m_importedPages.elementAt(i));
                m_report.addString(" checking page "+(String)m_importedPages.elementAt(i));
                m_report.addSeperator(0);
                // now save the result in the database
                m_cms.createLinkEntrys(links.getResourceId(), links.getLinkTargets());
            }catch(CmsException e){
                m_report.addString("problems with "+(String)m_importedPages.elementAt(i)+":"+e.getMessage());
                m_report.addSeperator(0);
            }
        }
    }
}

/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsImport.java,v $
* Date   : $Date: 2003/07/18 19:03:49 $
* Version: $Revision: 1.114 $
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

import org.opencms.security.CmsAccessControlEntry;

import com.opencms.boot.CmsBase;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.OpenCms;
import com.opencms.flex.util.CmsStringSubstitution;
import com.opencms.flex.util.CmsUUID;
import com.opencms.linkmanagement.CmsPageLinks;
import com.opencms.report.I_CmsReport;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.template.CmsXmlXercesParser;
import com.opencms.util.LinkSubstitution;
import com.opencms.workplace.I_CmsWpConstants;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
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
 * or a zip file into the OpenCms VFS.
 *
 * @author Andreas Schouten
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * 
 * @version $Revision: 1.114 $ $Date: 2003/07/18 19:03:49 $
 */
public class CmsImport implements I_CmsConstants, I_CmsWpConstants, Serializable {

    /** The algorithm for the message digest */
    public static final String C_IMPORT_DIGEST = "MD5";

    /** The cms contect to do the operations on the VFS/COS with */
    protected CmsObject m_cms;

    /** The xml manifest-file */
    protected Document m_docXml;

    /** The object to report the log messages */
    protected I_CmsReport m_report = null;

    /** The import-resource (folder) to load resources from */
    protected File m_importResource = null;

    /**  The import-resource (zip) to load resources from */
    protected ZipFile m_importZip = null;

    /** Indicates if module data is being imported */
    protected boolean m_importingChannelData;

    /** The import-file to load resources from */
    protected String m_importFile;

    /** The import-path to write resources into the cms */
    protected String m_importPath;

    /**
     * The version of this import, noted in the info tag of the manifest.xml.<p>
     * 
     * 0 indicates an export file without a version number, that is before version 4.3.23 of OpenCms.<br>
     * 1 indicates an export file of OpenCms with a version before 5.0.0
     * 2 is the current (since 5.0.0) version of the export file    
     */
    private int m_importVersion = 0;

    /** The path to the bodies in OpenCms 4.x */
    private static final String C_VFS_PATH_OLD_BODIES = "/content/bodys/";

    /** The path to the bodies in OpenCms 5.x */
    private static final String C_VFS_PATH_BODIES = "system/bodies/";

    /** Web application names for conversion support */
    private List m_webAppNames = new ArrayList();

    /** Old webapp URL for import conversion */
    private String m_webappUrl = null;

    /** Digest for taking a fingerprint of the files */
    private MessageDigest m_digest = null;

    /** Groups to create during import are stored here */
    private Stack m_groupsToCreate = new Stack();

    /** Access control entries for a single resource */
    private Vector m_acEntriesToCreate = new Vector();

    /**
     * In this vector we store the imported pages (as Strings from getAbsolutePath()),
     * after the import we check them all to update the link tables for the linkmanagement.
     */
    private Vector m_importedPages = new Vector();

    /** page file storage for page file and body coversion */
    private List m_pageStorage = new ArrayList();

    /** folder storage for page file and body coversion */
    private List m_folderStorage = new LinkedList();

    /** Debug flag to show debug output */
    private static final int DEBUG = 0;

    /**
     * Constructs a new uninitialized import, required for the module data import.<p>
     * 
     * @see CmsImportModuledata
     */
    public CmsImport() {
        // empty constructor
    }

    /**
     * Constructs a new import object which imports the resources from an OpenCms 
     * export zip file or a folder in the "real" file system.<p>
     *
     * @param cms the current cms object
     * @param importFile the file or folder to import from
     * @param importPath the path in the cms VFS to import into
     * @param report a report object to output the progress information to
     * @throws CmsException if something goes wrong
     */
    public CmsImport(CmsObject cms, String importFile, String importPath, I_CmsReport report) throws CmsException {
        // set member variables
        m_cms = cms;
        m_importFile = importFile;
        m_importPath = importPath;
        m_report = report;
        m_importingChannelData = false;
    }

    /**
     * Imports the resources and writes them to the cms VFS, even if there 
     * already exist files with the same name.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    public void importResources() throws CmsException {
        // initialize the import
        openImportFile();
        try {
            // first import the user information
            if (m_cms.isAdmin()) {
                importGroups();
                importUsers();
            }

            // now import the VFS resources
            importAllResources(null, null, null, null, null);
        } catch (CmsException e) {
            throw e;
        } finally {
            // close the import file
            closeImportFile();
        }
    }

    /**
     * Imports the resources for a module.<p>
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
    public void importResources(Vector excludeList, Vector writtenFilenames, Vector fileCodes, String propertyName, String propertyValue) throws CmsException {
        // initialize the import
        openImportFile();
        try {
            // now import the VFS resources
            importAllResources(excludeList, writtenFilenames, fileCodes, propertyName, propertyValue);
        } catch (CmsException e) {
            throw e;
        } finally {
            // close the import file
            closeImportFile();
        }
    }

    /**
     * Initilizes the import.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    protected void openImportFile() throws CmsException {
        // create the digest
        createDigest();

        // open the import resource
        getImportResource();

        // read the xml-config file
        getXmlConfigFile();

        // try to read the export version nummber
        try {
            m_importVersion = Integer.parseInt(getTextNodeValue((Element)m_docXml.getElementsByTagName(C_EXPORT_TAG_INFO).item(0), C_EXPORT_TAG_VERSION));
        } catch (Exception e) {
            //ignore the exception, the export file has no version nummber (version 0).
        }
    }

    /**
     * Closes the import file.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    protected void closeImportFile() throws CmsException {
        if (m_importZip != null) {
            try {
                m_importZip.close();
            } catch (IOException exc) {
                m_report.println(exc);
                throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
            }
        }
    }

    /**
     * Read infos from the properties and create a MessageDigest.<p>
     * 
     * @throws CmsException if something goes wrong
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
     * Returns a list of files which are both in the import and in the virtual file system.<p>
     * 
     * @return Vector of Strings, complete path of the files
     * @throws CmsException if something goes wrong
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
     * Gets the import resource and stores it in object-member.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    protected void getImportResource() throws CmsException {
        try {
            // get the import resource
            m_importResource = new File(CmsBase.getAbsolutePath(m_importFile));

            // if it is a file it must be a zip-file
            if (m_importResource.isFile()) {
                m_importZip = new ZipFile(m_importResource);
            }
        } catch (Exception exc) {
            m_report.println(exc);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Returns a Vector of resource names that are needed to create a project for this import.<p>
     * 
     * It calls the method getConflictingFileNames if needed, to calculate these resources.
     * 
     * @return a Vector of resource names that are needed to create a project for this import
     * @throws CmsException if something goes wrong
     */
    public Vector getResourcesForProject() throws CmsException {
        NodeList fileNodes;
        Element currentElement;
        String destination;
        Vector resources = new Vector();
        try {
            if (m_importingChannelData)
                m_cms.setContextToCos();

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
            m_report.println(exc);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            if (m_importingChannelData)
                m_cms.setContextToVfs();

        }
        if (m_importZip != null) {
            try {
                m_importZip.close();
            } catch (IOException exc) {
                m_report.println(exc);
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
     * Gets the xml-config file from the import resource and stores it in object-member.
     * Checks whether the import is from a module file
     * 
     * @throws CmsException if something goes wrong
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
            m_report.println(exc);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Imports a resource (file or folder) into the cms.<p>
     * This method is used for import versions 2 or lower.
     * 
     * @param source the path to the source-file
     * @param destination the path to the destination-file in the cms
     * @param uuid  the structure uuid of the resource
     * @param uuidfile  the file uuid of the resource
     * @param uuidresource  the resource uuid of the resource
     * @param type the resource-type of the file
     * @param access the access-flags of the file
     * @param lastmodified the timestamp of the file
     * @param properties a hashtable with properties for this resource
     * @param writtenFilenames filenames of the files and folder which have actually been successfully written
     *       not used when null
     * @param fileCodes code of the written files (for the registry)
     *       not used when null
     * @return imported resource
     */
    private CmsResource importResourceVersion2(String source, String destination, String uuid, String uuidfile, String uuidresource, String type, String access, long lastmodified, Map properties, Vector writtenFilenames, Vector fileCodes) {

        boolean success = true;
        byte[] content = null;
        String fullname = null;
        CmsResource res = null;

        try {
            if (m_importingChannelData) {
                // try to read an existing channel to get the channel id
                String channelId = null;
                try {
                    if ((type.equalsIgnoreCase(CmsResourceTypeFolder.C_RESOURCE_TYPE_NAME)) && (!destination.endsWith(C_FOLDER_SEPARATOR))) {
                        destination += C_FOLDER_SEPARATOR;
                    }
                    CmsResource channel = m_cms.readFileHeader(C_ROOT + destination);
                    channelId = m_cms.readProperty(m_cms.readAbsolutePath(channel), I_CmsConstants.C_PROPERTY_CHANNELID);
                } catch (Exception e) {
                    // ignore the exception, a new channel id will be generated
                }
                if (channelId == null) {
                    // the channel id does not exist, so generate a new one
                    int newChannelId = org.opencms.db.CmsIdGenerator.nextId(C_TABLE_CHANNELID);
                    channelId = "" + newChannelId;
                }
                properties.put(I_CmsConstants.C_PROPERTY_CHANNELID, channelId);
            }

            // get the file content
            if (source != null) {
                content = getFileBytes(source);
            }
            // check and convert old import files    
            if (m_importVersion < 2) {

                // convert content from pre 5.x must be activated
                if ("page".equals(type) || ("plain".equals(type)) || ("XMLTemplate".equals(type))) {
                    if (DEBUG > 0) {
                        System.err.println("#########################");
                        System.err.println("[" + this.getClass().getName() + ".importResource()]: starting conversion of \"" + type + "\" resource " + source + ".");
                    }
                    // change the filecontent for encoding if necessary
                    content = convertFile(source, content);
                }
                // only check the file type if the version of the export is 0
                if (m_importVersion == 0) {
                    // ok, a (very) old system exported this, check if the file is ok
                    if (!(new CmsCompatibleCheck()).isTemplateCompatible(m_importPath + destination, content, type)) {
                        type = CmsResourceTypeCompatiblePlain.C_RESOURCE_TYPE_NAME;
                        m_report.print(m_report.key("report.must_set_to") + type + " ", I_CmsReport.C_FORMAT_WARNING);
                    }
                }
            }

            // if the import is older than version 3, some additional conversions must be made
            if (m_importVersion < 3) {
                if ("page".equals(type)) {

                    // if the imported resource is a page, store its path inside the VFS for later
                    // integration with its body
                    m_pageStorage.add(destination);
                } else if ("folder".equals(type)) {
                    // check if the imported resource is a folder. Folders created in the /system/bodies/ folder
                    // must be remove since we do not use body files anymore.
                    if (destination.startsWith(C_VFS_PATH_BODIES)) {
                        m_folderStorage.add(destination);

                    }
                }

            }                  
            // get all required information to create a CmsResource
            int resType=m_cms.getResourceTypeId(type);
            int size=0;
            if (content!=null) size=content.length;  
            // get the required UUIDs         
            CmsUUID curUser=m_cms.getRequestContext().currentUser().getId();            
            CmsUUID newUuidstructure= new CmsUUID();
            CmsUUID newUuidcontent = new CmsUUID();
            CmsUUID newUuidresource = new CmsUUID();             
            if (uuid!=null) {
                newUuidstructure = new CmsUUID(uuid);       
            }
            if (uuidfile!=null) {
                newUuidcontent = new CmsUUID(uuidfile);
            }
            if (uuidresource!=null) { 
                newUuidresource = new CmsUUID(uuidresource);               
            }             
                    
            // extract the name of the resource form the destination
            String resname=destination;
            if (resname.endsWith("/")) {
                resname=resname.substring(0,resname.length()-1);            
            } 
            if (resname.lastIndexOf("/")>0) {
                resname=resname.substring(resname.lastIndexOf("/")+1,resname.length());
            }
                         
            // create a new CmsResource                         
            CmsResource resource=new CmsResource(newUuidstructure,newUuidresource,
                                                 CmsUUID.getNullUUID(),
                                                 newUuidcontent,resname,resType,
                                                 new Integer(0).intValue(),m_cms.getRequestContext().currentProject().getId(),
                                                 0,I_CmsConstants.C_STATE_NEW,curUser,
                                                 m_cms.getResourceType(resType).getLoaderId(),
                                                 lastmodified,curUser,lastmodified,
                                                 curUser,size,m_cms.getRequestContext().currentProject().getId(),
                                                 I_CmsConstants.C_VFS_LINK_TYPE_MASTER);
            // import this resource in the VFS              
            res = m_cms.importResource(resource, content, properties, m_importPath+destination);   
         
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

        byte[] digestContent = { 0 };
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
     * Imports a resource (file or folder) into the cms.<p>
     * This method id used for imports version 3.
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
    private CmsResource importResourceVersion3(String source, String destination, String type, String uuidstructure, String uuidresource, String uuidcontent, long datelastmodified, String userlastmodified, long datecreated, String usercreated, String flags, Map properties, Vector writtenFilenames, Vector fileCodes) {

        boolean success = true;
        byte[] content = null;
        String fullname = null;
        CmsResource res = null;

        try {
            if (m_importingChannelData) {
                // try to read an existing channel to get the channel id
                String channelId = null;
                try {
                    if ((type.equalsIgnoreCase(CmsResourceTypeFolder.C_RESOURCE_TYPE_NAME)) && (!destination.endsWith(C_FOLDER_SEPARATOR))) {
                        destination += C_FOLDER_SEPARATOR;
                    }
                    CmsResource channel = m_cms.readFileHeader(C_ROOT + destination);
                    channelId = m_cms.readProperty(m_cms.readAbsolutePath(channel), I_CmsConstants.C_PROPERTY_CHANNELID);
                } catch (Exception e) {
                    // ignore the exception, a new channel id will be generated
                }
                if (channelId == null) {
                    // the channel id does not exist, so generate a new one
                    int newChannelId = org.opencms.db.CmsIdGenerator.nextId(C_TABLE_CHANNELID);
                    channelId = "" + newChannelId;
                }
                properties.put(I_CmsConstants.C_PROPERTY_CHANNELID, channelId);
            }
            // get the file content
            if (source != null) {
                content = getFileBytes(source);
            }
            // get all required information to create a CmsResource
            int resType=m_cms.getResourceTypeId(type);
            int size=0;
            if (content!=null) size=content.length;  
            // get the required UUIDs         
            CmsUUID curUser=m_cms.getRequestContext().currentUser().getId();    
            CmsUUID newUserlastmodified= new CmsUUID(userlastmodified);
            CmsUUID newUsercreated= new CmsUUID(usercreated);         
            CmsUUID newUuidstructure= new CmsUUID();
            CmsUUID newUuidcontent = new CmsUUID();
            CmsUUID newUuidresource = new CmsUUID();             
            if (uuidstructure!=null) {
                newUuidstructure = new CmsUUID(uuidstructure);       
            }
            if (uuidcontent!=null) {
                newUuidcontent = new CmsUUID(uuidcontent);
            }
            if (uuidresource!=null) { 
                newUuidresource = new CmsUUID(uuidresource);               
            }             
            // extract the name of the resource form the destination
            String resname=destination;
            if (resname.endsWith("/")) {
                resname=resname.substring(0,resname.length()-1);            
            } 
            if (resname.lastIndexOf("/")>0) {
                resname=resname.substring(resname.lastIndexOf("/")+1,resname.length());
            }
                         
            // create a new CmsResource                         
            CmsResource resource=new CmsResource(newUuidstructure,newUuidresource,
                                                 CmsUUID.getNullUUID(),
                                                 newUuidcontent,resname,resType,
                                                 new Integer(flags).intValue(),m_cms.getRequestContext().currentProject().getId(),
                                                 0,I_CmsConstants.C_STATE_NEW,curUser,
                                                 m_cms.getResourceType(resType).getLoaderId(),
                                                 datelastmodified,newUserlastmodified,datecreated,
                                                 newUsercreated,size,m_cms.getRequestContext().currentProject().getId(),
                                                 I_CmsConstants.C_VFS_LINK_TYPE_MASTER);
            // import this resource in the VFS              
            res = m_cms.importResource(resource, content, properties, m_importPath+destination);             
            
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

        byte[] digestContent = { 0 };
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
    * Imports the resources and writes them to the cms.<p>
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
    protected void importAllResources(Vector excludeList, Vector writtenFilenames, Vector fileCodes, String propertyName, String propertyValue) throws CmsException {
        // use the correct import verions.
        // this is nescessary since the manifest.xml has changed 
        if (m_importVersion <= 2) {
            importAllResourcesVersion2(excludeList, writtenFilenames, fileCodes, propertyName, propertyValue);
        } else if (m_importVersion == 3) {
            importAllResourcesVersion3(excludeList, writtenFilenames, fileCodes, propertyName, propertyValue);

        }
    }

    /**
     * Imports the resources and writes them to the cms.<p>
     * 
     * This method is used to import import files with the version 3.
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
    private void importAllResourcesVersion3(Vector excludeList, Vector writtenFilenames, Vector fileCodes, String propertyName, String propertyValue) throws CmsException {

        String source, destination, type, uuidstructure, uuidresource, uuidcontent, userlastmodified, usercreated, flags, timestamp;
        long datelastmodified, datecreated;

        NodeList fileNodes, acentryNodes;
        Element currentElement, currentEntry;
        Map properties = null;

        if (m_importingChannelData)
            m_cms.setContextToCos();

        // clear some required structures at the init phase of the import      
        if (excludeList == null) {
            excludeList = new Vector();
        }
        // get list of unwanted properties
        List deleteProperties = (List)A_OpenCms.getRuntimeProperty("compatibility.support.import.remove.propertytags");
        if (deleteProperties == null)
            deleteProperties = new ArrayList();
        // get list of immutable resources
        List immutableResources = (List)A_OpenCms.getRuntimeProperty("import.immutable.resources");
        if (immutableResources == null)
            immutableResources = new ArrayList();
        try {
            // get all file-nodes
            fileNodes = m_docXml.getElementsByTagName(C_EXPORT_TAG_FILE);
            int importSize = fileNodes.getLength();
            // walk through all files in manifest
            for (int i = 0; i < fileNodes.getLength(); i++) {
                m_report.print(" ( " + (i + 1) + " / " + importSize + " ) ");
                currentElement = (Element)fileNodes.item(i);
                // get all information for a file-import
                // <source>
                source = getTextNodeValue(currentElement, C_EXPORT_TAG_SOURCE);
                // <destintion>
                destination = getTextNodeValue(currentElement, C_EXPORT_TAG_DESTINATION);
                // <type>
                type = getTextNodeValue(currentElement, C_EXPORT_TAG_TYPE);
                // <uuidstructure>
                uuidstructure = getTextNodeValue(currentElement, C_EXPORT_TAG_UUIDSTRUCTURE);
                // <uuidresource>
                uuidresource = getTextNodeValue(currentElement, C_EXPORT_TAG_UUIDRESOURCE);
                // <uuidcontent>
                uuidcontent = getTextNodeValue(currentElement, C_EXPORT_TAG_UUIDCONTENT);
                // <datelastmodified>
                if ((timestamp = getTextNodeValue(currentElement, C_EXPORT_TAG_DATELASTMODIFIED)) != null) {
                    datelastmodified = Long.parseLong(timestamp);
                } else {
                    datelastmodified = System.currentTimeMillis();
                }
                // <userlastmodified>
                userlastmodified = getTextNodeValue(currentElement, C_EXPORT_TAG_USERLASTMODIFIED);
                // <datecreated>
                if ((timestamp = getTextNodeValue(currentElement, C_EXPORT_TAG_DATECREATED)) != null) {
                    datecreated = Long.parseLong(timestamp);
                } else {
                    datecreated = System.currentTimeMillis();
                }
                // <usercreated>
                usercreated = getTextNodeValue(currentElement, C_EXPORT_TAG_USERCREATED);
                // <flags>              
                flags = getTextNodeValue(currentElement, C_EXPORT_TAG_FLAGS);

                //TODO: will this work with multiple sites?
                String translatedName = C_VFS_DEFAULT + m_importPath + destination;
                if (CmsResourceTypeFolder.C_RESOURCE_TYPE_NAME.equals(type)) {
                    translatedName += C_FOLDER_SEPARATOR;
                }
                // translate the name during import
                translatedName = m_cms.getRequestContext().getDirectoryTranslator().translateResource(translatedName);
                // check if this resource is immutable
                boolean resourceNotImmutable = checkImmutable(translatedName, immutableResources);
                translatedName = translatedName.substring(C_VFS_DEFAULT.length());
                // if the resource is not immutable and not on the exclude list, import it
                if (resourceNotImmutable && (!excludeList.contains(translatedName))) {
                    // print out the information to the report
                    m_report.print(m_report.key("report.importing"), I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(translatedName + " ");
                    // get all properties
                    properties = getPropertiesFromXml(currentElement, type, propertyName, propertyValue, deleteProperties);
                    // import the resource               

                    CmsResource res = importResourceVersion3(source, destination, type, uuidstructure, uuidresource,uuidcontent, datelastmodified, userlastmodified, datecreated, usercreated, flags, properties, writtenFilenames, fileCodes);

                    // if the resource was imported add the access control entrys if available
                    if (res != null) {
                        // write all imported access control entries for this file
                        acentryNodes = currentElement.getElementsByTagName(C_EXPORT_TAG_ACCESSCONTROL_ENTRY);
                        // collect all access control entries
                        for (int j = 0; j < acentryNodes.getLength(); j++) {
                            currentEntry = (Element)acentryNodes.item(j);
                            // get the data of the access control entry
                            String id = getTextNodeValue(currentEntry, C_EXPORT_TAG_ACCESSCONTROL_PRINCIPAL);
                            String acflags = getTextNodeValue(currentEntry, C_EXPORT_TAG_FLAGS);
                            String allowed = getTextNodeValue(currentEntry, C_EXPORT_TAG_ACCESSCONTROL_ALLOWEDPERMISSIONS);
                            String denied = getTextNodeValue(currentEntry, C_EXPORT_TAG_ACCESSCONTROL_DENIEDPERMISSIONS);
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
            if (!m_importingChannelData) {
                // at last we have to get the links from all new imported pages for the  linkmanagement
                m_report.println(m_report.key("report.check_links_begin"), I_CmsReport.C_FORMAT_HEADLINE);
                updatePageLinks();
                m_report.println(m_report.key("report.check_links_end"), I_CmsReport.C_FORMAT_HEADLINE);
                m_cms.joinLinksToTargets(m_report);
            }

        } catch (Exception exc) {
            m_report.println(exc);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            if (m_importingChannelData)
                m_cms.setContextToVfs();
        }

    }

    /**
     * Imports the resources and writes them to the cms.<p>
     * 
     * This method is used to import import files with the version 2 or lower.
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
    private void importAllResourcesVersion2(Vector excludeList, Vector writtenFilenames, Vector fileCodes, String propertyName, String propertyValue) throws CmsException {
        NodeList fileNodes, acentryNodes;
        Element currentElement, currentEntry;
        String source, destination, type, access, timestamp, uuid, uuidfile, uuidresource;
        long lastmodified = 0;
        Map properties = null;

        if (m_importingChannelData)
            m_cms.setContextToCos();

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
        List deleteProperties = (List)A_OpenCms.getRuntimeProperty("compatibility.support.import.remove.propertytags");
        if (deleteProperties == null)
            deleteProperties = new ArrayList();

        // get list of immutable resources
        List immutableResources = (List)A_OpenCms.getRuntimeProperty("import.immutable.resources");
        if (immutableResources == null)
            immutableResources = new ArrayList();
        if (DEBUG > 0)
            System.err.println("Import: Immutable resources size is " + immutableResources.size());

        try {
            // get all file-nodes
            fileNodes = m_docXml.getElementsByTagName(C_EXPORT_TAG_FILE);
            int importSize = fileNodes.getLength();

            // walk through all files in manifest
            for (int i = 0; i < fileNodes.getLength(); i++) {

                m_report.print(" ( " + (i + 1) + " / " + importSize + " ) ");
                currentElement = (Element)fileNodes.item(i);

                // get all information for a file-import
                source = getTextNodeValue(currentElement, C_EXPORT_TAG_SOURCE);
                destination = getTextNodeValue(currentElement, C_EXPORT_TAG_DESTINATION);
                type = getTextNodeValue(currentElement, C_EXPORT_TAG_TYPE);
                access = getTextNodeValue(currentElement, C_EXPORT_TAG_ACCESS);
                uuid = getTextNodeValue(currentElement, C_EXPORT_TAG_UUIDSTRUCTURE);
                uuidfile = getTextNodeValue(currentElement, C_EXPORT_TAG_UUIDCONTENT);
                uuidresource = getTextNodeValue(currentElement, C_EXPORT_TAG_UUIDRESOURCE);

                if ((timestamp = getTextNodeValue(currentElement, C_EXPORT_TAG_LASTMODIFIED)) != null) {
                    lastmodified = Long.parseLong(timestamp);
                } else {
                    lastmodified = System.currentTimeMillis();
                }

                // if the type is "script" set it to plain
                if ("script".equals(type)) {
                    type = CmsResourceTypePlain.C_RESOURCE_TYPE_NAME;
                }

                String translatedName = C_VFS_DEFAULT + m_importPath + destination;
                if (CmsResourceTypeFolder.C_RESOURCE_TYPE_NAME.equals(type)) {
                    translatedName += C_FOLDER_SEPARATOR;
                }
                translatedName = m_cms.getRequestContext().getDirectoryTranslator().translateResource(translatedName);
                if (DEBUG > 3)
                    System.err.println("Import: Translated resource name is " + translatedName);

                boolean resourceNotImmutable = checkImmutable(translatedName, immutableResources);

                translatedName = translatedName.substring(C_VFS_DEFAULT.length());
                if (resourceNotImmutable && (!excludeList.contains(translatedName))) {

                    // print out the information to the report
                    m_report.print(m_report.key("report.importing"), I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(translatedName + " ");

                    // get all properties
                    properties = getPropertiesFromXml(currentElement, type, propertyName, propertyValue, deleteProperties);

                    // import the specified file 
                    CmsResource res = importResourceVersion2(source, destination, uuid, uuidfile, uuidresource, type, access, lastmodified, properties, writtenFilenames, fileCodes);

                    if (res != null) {

                        // write all imported access control entries for this file
                        acentryNodes = currentElement.getElementsByTagName(C_EXPORT_TAG_ACCESSCONTROL_ENTRY);
                        // collect all access control entries
                        //String resid = getTextNodeValue(currentElement, C_EXPORT_TAG_ID);
                        for (int j = 0; j < acentryNodes.getLength(); j++) {
                            currentEntry = (Element)acentryNodes.item(j);
                            // get the data of the access control entry
                            String id = getTextNodeValue(currentEntry, C_EXPORT_TAG_ID);
                            String flags = getTextNodeValue(currentEntry, C_EXPORT_TAG_FLAGS);
                            String allowed = getTextNodeValue(currentEntry, C_EXPORT_TAG_ACCESSCONTROL_ALLOWEDPERMISSIONS);
                            String denied = getTextNodeValue(currentEntry, C_EXPORT_TAG_ACCESSCONTROL_DENIEDPERMISSIONS);

                            // add the entry to the list
                            addImportAccessControlEntry(res, id, allowed, denied, flags);
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
            if (!m_importingChannelData) {
                // at last we have to get the links from all new imported pages for the  linkmanagement
                m_report.println(m_report.key("report.check_links_begin"), I_CmsReport.C_FORMAT_HEADLINE);
                updatePageLinks();
                m_report.println(m_report.key("report.check_links_end"), I_CmsReport.C_FORMAT_HEADLINE);
                m_cms.joinLinksToTargets(m_report);
            }

            // now merge the body and page control files. this only has to be done if the import
            // version is below version 3
            if (m_importVersion < 3) {
                // only do the conversions if the new resourcetype (CmsResourceTypeNewPage.) is available 
                CmsResource newpage = null;
                try {
                    newpage = m_cms.readFileHeader("/system/workplace/restypes/" + CmsResourceTypeNewPage.C_RESOURCE_TYPE_NAME);
                } catch (CmsException e1) {
                    // do nothing, 
                }
                if (newpage != null) {
                    mergePageFiles();
                    removeFolders();
                }
            }

        } catch (Exception exc) {
            m_report.println(exc);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            if (m_importingChannelData)
                m_cms.setContextToVfs();
        }
    }

    /**
     * Gets all properties from one file node in the manifest.xml.<p>
     * 
     * @param currentElement the current file node
     * @param type the resource type of this node
     * @param propertyName name of a property to be added to all resources
     * @param propertyValue value of that property
     * @param deleteProperties the list of properies to be deleted
     * @return HashMap with all properties blonging to the resource
     * @throws CmsException if something goes wrong
     */
    private HashMap getPropertiesFromXml(Element currentElement, String type, String propertyName, String propertyValue, List deleteProperties) throws CmsException {

        Vector types = new Vector(); // stores the file types for which the property already exists
        // get all properties for this file
        NodeList propertyNodes = currentElement.getElementsByTagName(C_EXPORT_TAG_PROPERTY);
        // clear all stores for property information
        HashMap properties = new HashMap();
        // add the module property to properties
        int resType = m_cms.getResourceTypeId(type);
        if (propertyName != null && propertyValue != null && !"".equals(propertyName)) {
            if (!types.contains(type)) {
                types.addElement(type);
                createPropertydefinition(propertyName, resType);
            }
            properties.put(propertyName, propertyValue);
        }
        // walk through all properties
        for (int j = 0; j < propertyNodes.getLength(); j++) {
            Element currentProperty = (Element)propertyNodes.item(j);
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
                createPropertydefinition(name, resType);
            }
        }
        return properties;
    }

    /**
     * Checks if the resources is in the list of immutalbe resources. <p>
     * 
     * @param translatedName the name of the resource
     * @param immutableResources the list of the immutable resources
     * @return true or false
     */
    private boolean checkImmutable(String translatedName, List immutableResources) {

        boolean resourceNotImmutable = true;
        if (immutableResources.contains(translatedName)) {
            if (DEBUG > 1)
                System.err.println("Import: Translated resource name is immutable");

            // this resource must not be modified by an import if it already exists
            try {
                m_cms.readFileHeader("//" + translatedName);
                resourceNotImmutable = false;
                if (DEBUG > 0)
                    System.err.println("Import: Immutable flag set for resource");
            } catch (CmsException e) {
                // resourceNotImmutable will be true
                if (DEBUG > 0)
                    System.err.println("Import: Immutable test caused exception " + e);
            }
        }
        return resourceNotImmutable;
    }

    /**
     * Merges the page control files and their corresponding bodies into a single files.<p>
     * 
     * This conversion is only made for imports earlier than verison 3.
     * 
     * @throws CmsException if something goes wrong
     */
    private void mergePageFiles() throws CmsException {

        // check if the template property exists. If not, create it.
        try {
            m_cms.readPropertydefinition(C_XML_CONTROL_TEMPLATE_PROPERTY, CmsResourceTypeNewPage.C_RESOURCE_TYPE_ID);
        } catch (CmsException e) {
            // the template propertydefintion does not exist. So create it.
            m_cms.createPropertydefinition(C_XML_CONTROL_TEMPLATE_PROPERTY, CmsResourceTypeNewPage.C_RESOURCE_TYPE_ID);
        }
        // copy all propertydefinitions of the old page to the new page
        Vector definitions = m_cms.readAllPropertydefinitions(CmsResourceTypePage.C_RESOURCE_TYPE_ID);

        Iterator j = definitions.iterator();
        while (j.hasNext()) {
            CmsPropertydefinition definition = (CmsPropertydefinition)j.next();
            // check if this propertydef already exits
            try {
                m_cms.readPropertydefinition(definition.getName(), CmsResourceTypeNewPage.C_RESOURCE_TYPE_ID);
            } catch (Exception e) {
                m_cms.createPropertydefinition(definition.getName(), CmsResourceTypeNewPage.C_RESOURCE_TYPE_ID);
            }
        }

        // iterate through the list of all page controlfiles found during the import process
        int size = m_pageStorage.size();

        m_report.println(m_report.key("report.merge_start"), I_CmsReport.C_FORMAT_HEADLINE);

        Iterator i = m_pageStorage.iterator();
        int counter = 1;
        while (i.hasNext()) {
            String resname = (String)i.next();
            // adjust the resourcename if nescessary
            if (!resname.startsWith("/")) {
                resname = "/" + resname;
            }
            m_report.print("( " + counter + " / " + size + " ) ", I_CmsReport.C_FORMAT_DEFAULT);
            m_report.print(m_report.key("report.merge") + " " + resname, I_CmsReport.C_FORMAT_NOTE);

            // get the header file
            CmsFile pagefile = m_cms.readFile(resname);
            // now parse the content of the headerfile to identify the master template used by this
            // page
            InputStream in = new ByteArrayInputStream(pagefile.getContents());
            Document contentXml;
            CmsFile bodyfile;

            try {
                String mastertemplate = "";
                String bodyname = "";
                // create DOM document
                contentXml = A_CmsXmlContent.getXmlParser().parse(in);
                // get the <masterTemplate> node to check the content.
                // this node contains the name of the template file
                NodeList masterTemplateNode = contentXml.getElementsByTagName("masterTemplate");
                // there is only one <masterTemplate> allowed
                if (masterTemplateNode.getLength() == 1) {
                    // get the name of the mastertemplate
                    mastertemplate = masterTemplateNode.item(0).getFirstChild().getNodeValue();
                }
                // get the <TEMPLATE> node to check the content  .
                // this node contains the name of the body file.
                NodeList bodyNode = contentXml.getElementsByTagName("TEMPLATE");
                // there is only one <masterTemplate> allowed
                if (bodyNode.getLength() == 1) {
                    // get the name of the mastertemplate
                    bodyname = bodyNode.item(0).getFirstChild().getNodeValue();
                    // lock the resource, so that it can be manipulated
                    m_cms.lockResource(resname);
                    // get all properties                   
                    Map properties = m_cms.readProperties(resname);
                    // now get the content of the bodyfile and insert it into the control file                   
                    bodyfile = m_cms.readFile(bodyname);
                    pagefile.setContents(bodyfile.getContents());
                    //new set the type to new page                               
                    pagefile.setType(CmsResourceTypeNewPage.C_RESOURCE_TYPE_ID);
                    // write all changes                     
                    m_cms.writeFile(pagefile);
                    // add the template property to the controlfile
                    m_cms.writeProperty(resname, C_XML_CONTROL_TEMPLATE_PROPERTY, mastertemplate);
                    m_cms.writeProperties(resname, properties);
                    // don, ulock the resource                   
                    m_cms.unlockResource(resname, false);
                    // finally delete the old body file, it is not needed anymore
                    m_cms.lockResource(bodyname);
                    m_cms.deleteResource(bodyname);
                    m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
                }

            } catch (Exception e) {
                throw new CmsException(e.toString());
            } finally {
                // free mem
                pagefile = null;
                in = null;
                contentXml = null;
                bodyfile = null;
            }

            counter++;

        }
        // free mem
        m_pageStorage = null;

    }

    /**
     * Deletes the folder structure which has been creating while importing the body files..<p>
     * 
     * This conversion is only made for imports earlier than verison 3.
     * 
     * @throws CmsException if something goes wrong
     */
    private void removeFolders() throws CmsException {
        int size = m_folderStorage.size();

        m_report.println(m_report.key("report.delfolder_start"), I_CmsReport.C_FORMAT_HEADLINE);
        // iterate though all collected folders. Iteration must start at the end of the list,
        // as folders habe to be deleted in the reverse order.
        ListIterator i = m_folderStorage.listIterator(size);
        int counter = 1;
        while (i.hasPrevious()) {
            String resname = (String)i.previous();
            resname = "/" + resname + "/";
            // now check if the folder is really empty. Only delete empty folders
            List files = m_cms.getFilesInFolder(resname, false);

            if (files.size() == 0) {
                List folders = m_cms.getSubFolders(resname, false);
                if (folders.size() == 0) {
                    m_report.print("( " + counter + " / " + size + " ) ", I_CmsReport.C_FORMAT_DEFAULT);
                    m_report.print(m_report.key("report.delfolder") + " " + resname, I_CmsReport.C_FORMAT_NOTE);
                    m_cms.lockResource(resname);
                    m_cms.deleteResource(resname);
                    m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
                    counter++;
                }
            }
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
            groupNodes = m_docXml.getElementsByTagName(C_EXPORT_TAG_GROUPDATA);
            // walk through all groups in manifest
            for (int i = 0; i < groupNodes.getLength(); i++) {
                currentElement = (Element)groupNodes.item(i);
                id = getTextNodeValue(currentElement, C_EXPORT_TAG_ID);
                name = getTextNodeValue(currentElement, C_EXPORT_TAG_NAME);
                description = getTextNodeValue(currentElement, C_EXPORT_TAG_DESCRIPTION);
                flags = getTextNodeValue(currentElement, C_EXPORT_TAG_FLAGS);
                parentgroup = getTextNodeValue(currentElement, C_EXPORT_TAG_PARENTGROUP);
                // import this group
                importGroup(id, name, description, flags, parentgroup);
            }

            // now try to import the groups in the stack
            while (!m_groupsToCreate.empty()) {
                Stack tempStack = m_groupsToCreate;
                m_groupsToCreate = new Stack();
                while (tempStack.size() > 0) {
                    Hashtable groupdata = (Hashtable)tempStack.pop();
                    id = (String)groupdata.get(C_EXPORT_TAG_ID);
                    name = (String)groupdata.get(C_EXPORT_TAG_NAME);
                    description = (String)groupdata.get(C_EXPORT_TAG_DESCRIPTION);
                    flags = (String)groupdata.get(C_EXPORT_TAG_FLAGS);
                    parentgroup = (String)groupdata.get(C_EXPORT_TAG_PARENTGROUP);
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
        getImportResource();
        try {
            // getAll user nodes
            userNodes = m_docXml.getElementsByTagName(C_EXPORT_TAG_USERDATA);
            // walk threw all groups in manifest
            for (int i = 0; i < userNodes.getLength(); i++) {
                currentElement = (Element)userNodes.item(i);
                id = getTextNodeValue(currentElement, C_EXPORT_TAG_ID);
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
                infoNode = getTextNodeValue(currentElement, C_EXPORT_TAG_USERINFO);
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
                groupNodes = currentElement.getElementsByTagName(C_EXPORT_TAG_GROUPNAME);
                userGroups = new Vector();
                for (int j = 0; j < groupNodes.getLength(); j++) {
                    currentGroup = (Element)groupNodes.item(j);
                    userGroups.addElement(getTextNodeValue(currentGroup, C_EXPORT_TAG_NAME));
                }
                // import this group
                importUser(id, name, description, flags, password, recoveryPassword, firstname, lastname, email, address, section, defaultGroup, type, userInfo, userGroups);
            }
        } catch (Exception exc) {
            m_report.println(exc);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
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
    private void importGroup(String id, String name, String description, String flags, String parentgroupName) throws CmsException {
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
                } catch (CmsException exc) {}
            }
            if (((parentgroupName != null) && (!"".equals(parentgroupName))) && (parentGroup == null)) {
                // cannot create group, put on stack and try to create later
                Hashtable groupData = new Hashtable();
                groupData.put(C_EXPORT_TAG_NAME, name);
                groupData.put(C_EXPORT_TAG_DESCRIPTION, description);
                groupData.put(C_EXPORT_TAG_FLAGS, flags);
                groupData.put(C_EXPORT_TAG_PARENTGROUP, parentgroupName);
                m_groupsToCreate.push(groupData);
            } else {
                try {
                    m_report.print(m_report.key("report.importing_group"), I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(name);
                    m_report.print(m_report.key("report.dots"), I_CmsReport.C_FORMAT_NOTE);
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
    private void importUser(String id, String name, String description, String flags, String password, String recoveryPassword, String firstname, String lastname, String email, String address, String section, String defaultGroup, String type, Hashtable userInfo, Vector userGroups) throws CmsException {

        // create a new user id if not available
        if (id == null) {
            id = new CmsUUID().toString();
        }

        try {
            try {
                m_report.print(m_report.key("report.importing_user"), I_CmsReport.C_FORMAT_NOTE);
                m_report.print(name);
                m_report.print(m_report.key("report.dots"), I_CmsReport.C_FORMAT_NOTE);
                m_cms.addImportUser(id, name, password, recoveryPassword, description, firstname, lastname, email, Integer.parseInt(flags), userInfo, defaultGroup, address, section, Integer.parseInt(type));
                // add user to all groups vector
                for (int i = 0; i < userGroups.size(); i++) {
                    try {
                        m_cms.addUserToGroup(name, (String)userGroups.elementAt(i));
                    } catch (CmsException exc) {}
                }
                m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
            } catch (CmsException exc) {
                m_report.println(m_report.key("report.not_created"), I_CmsReport.C_FORMAT_OK);
            }
        } catch (Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Writes alread imported access control entries for a given resource.
     * 
     * @param resource the resource assigned to the access control entries
     * @throws CmsException if something goes wrong
     */
    private void importAccessControlEntries(CmsResource resource) throws CmsException {
        try {
            try {
                m_cms.writeAccessControlEntries(resource, m_acEntriesToCreate);
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
     * Creates a new access control entry and stores it for later write out.
     * 
     * @param res the resource
     * @param id the id of the principal
     * @param allowed the allowed permissions
     * @param denied the denied permissions
     * @param flags the flags
     */
    private void addImportAccessControlEntry(CmsResource res, String id, String allowed, String denied, String flags) {

        CmsAccessControlEntry ace = new CmsAccessControlEntry(res.getResourceAceId(), new CmsUUID(id), Integer.parseInt(allowed), Integer.parseInt(denied), Integer.parseInt(flags));
        m_acEntriesToCreate.add(ace);
    }

    /**
     * Checks all new imported pages and create or updates the entrys in the
     * database for the linkmanagement.<p>
     */
    private void updatePageLinks() {
        int importPagesSize = m_importedPages.size();
        for (int i = 0; i < importPagesSize; i++) {
            m_report.print(" ( " + (i + 1) + " / " + importPagesSize + " ) ");
            try {
                // first parse the page
                CmsPageLinks links = m_cms.getPageLinks((String)m_importedPages.elementAt(i));
                m_report.print(m_report.key("report.checking_page"), I_CmsReport.C_FORMAT_NOTE);
                m_report.println((String)m_importedPages.elementAt(i));
                // now save the result in the database
                m_cms.createLinkEntrys(links.getResourceId(), links.getLinkTargets());
            } catch (CmsException e) {
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
                if (DEBUG > 0) {
                    System.err.println("[" + this.getClass().getName() + ".convertFile()]: Encoding not supported, using default encoding.");
                }
            }
        } else {
            // encoding not found, set encoding of xml files to default
            if (DEBUG > 0) {
                System.err.println("[" + this.getClass().getName() + ".convertFile()]: Encoding not set, using default encoding and setting it in <?xml...?>.");
            }
            encoding = OpenCms.getDefaultEncoding();
            fileContent = setEncoding(fileContent, encoding);
        }
        // check the frametemplates
        if (filename.indexOf("frametemplates") != -1) {
            fileContent = scanFrameTemplate(fileContent);
        }
        // scan content/bodys
        if (filename.indexOf(C_VFS_PATH_OLD_BODIES) != -1 || filename.indexOf(I_CmsWpConstants.C_VFS_PATH_BODIES) != -1) {
            if (DEBUG > 0) {
                System.err.println("[" + this.getClass().getName() + ".convertFile()]: Starting scan of body page.");
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
            content = CmsStringSubstitution.substitute(content, "</head>", "<meta http-equiv=\"content-type\" content=\"text/html; charset=]]><method name=\"getEncoding\"/><![CDATA[\">\n</head>");
        } else {
            // Meta-Tag present
            if (content.toLowerCase().indexOf("charset=]]><method name=\"getencoding\"/>") == -1) {
                String fileStart = content.substring(0, content.toLowerCase().indexOf("charset=") + 8);
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
        } else {
            // XML information present, replace encoding
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
     * @param rules the translation rules
     * @return String the manipulated file content
     */
    public static String setDirectories(String content, String[] rules) {
        // get translation rules
        for (int i = 0; i < rules.length; i++) {
            String actRule = rules[i];
            // cut String "/default/vfs/" from rule
            actRule = CmsStringSubstitution.substitute(actRule, "/default/vfs", "");
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
     * @param fileName the name of the file 
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
        if (found) {
            InputStream in = new ByteArrayInputStream(content.getBytes());
            String editString, templateString;
            try {
                // create DOM document
                Document contentXml = A_CmsXmlContent.getXmlParser().parse(in);
                // get all <edittemplate> nodes to check their content
                NodeList editNodes = contentXml.getElementsByTagName("edittemplate");
                // no <edittemplate> tags present, create them!
                if (editNodes.getLength() < 1) {
                    if (DEBUG > 0) {
                        System.err.println("[" + this.getClass().getName() + ".convertPageBody()]: No <edittemplate> found, creating it.");
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
                            templateString = LinkSubstitution.substituteContentBody(templateString, m_webappUrl, fileName);
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
                        editString = CmsStringSubstitution.substitute(editString, (String)m_webAppNames.get(k), C_MACRO_OPENCMS_CONTEXT + "/");
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
                        } else {
                            // create template with "name" attribute
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
}
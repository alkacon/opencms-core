/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/CmsImportVersion2.java,v $
 * Date   : $Date: 2004/10/31 21:30:18 $
 * Version: $Revision: 1.76 $
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertydefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.I_CmsWpConstants;
import org.opencms.xml.page.CmsXmlPage;

import java.io.File;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.ZipFile;

import org.apache.commons.collections.ExtendedProperties;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * Implementation of the OpenCms Import Interface ({@link org.opencms.importexport.I_CmsImport}) for 
 * the import version 2.<p>
 * 
 * This import format was used in OpenCms 5.0.0 - 5.1.2.<p>
 *
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * 
 * @see org.opencms.importexport.A_CmsImport
 */
public class CmsImportVersion2 extends A_CmsImport {
    
    /** The version number of this import implementation. */
    private static final int C_IMPORT_VERSION = 2;
    
    /** The runtime property name for old webapp names. */
    private static final String C_COMPATIBILITY_WEBAPPNAMES = "compatibility.support.webAppNames";

    /** Web application names for conversion support. */
    protected List m_webAppNames;

    /** Old webapp URL for import conversion. */
    protected String m_webappUrl;
    
    /** folder storage for page file and body conversion. */
    private List m_folderStorage;

    /** page file storage for page file and body co.version. */
    private List m_pageStorage;
    
    /**
     * @see org.opencms.importexport.I_CmsImport#getVersion()
     * @return the version number of this import implementation
     */
    public int getVersion() {
        return CmsImportVersion2.C_IMPORT_VERSION;
    }
    
    /**
     * Initializes all member variables before the import is started.<p>
     * 
     * This is required since there is only one instance for
     * each import version that is kept in memory and reused.<p>
     */
    protected void initialize() {
        m_convertToXmlPage = true;
        m_webAppNames = new ArrayList(); 
        super.initialize();
    }
    
    /**
     * Cleans up member variables after the import is finished.<p>
     * 
     * This is required since there is only one instance for
     * each import version that is kept in memory and reused.<p>
     */
    protected void cleanUp() {
        m_pageStorage = null;
        m_folderStorage = null;
        m_webAppNames = null;
        m_webappUrl = null;   
        super.cleanUp();
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
        m_folderStorage = new LinkedList();
        m_pageStorage = new ArrayList();
        m_importedPages = new Vector();   
        m_linkStorage = new HashMap();
        m_linkPropertyStorage = new HashMap();
        
        try {
            // first import the user information
            if (m_cms.isAdmin()) {
                importGroups();
                importUsers();
            }
            // now import the VFS resources
            importAllResources(excludeList, writtenFilenames, fileCodes, propertyName, propertyValue);
            convertPointerToSiblings();
        } catch (CmsException e) {
            throw e;
        } finally {
            cleanUp();
        }
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
    private void importAllResources(Vector excludeList, Vector writtenFilenames, Vector fileCodes, String propertyName, String propertyValue) throws CmsException {
        List fileNodes = null, acentryNodes = null;
        Element currentElement = null, currentEntry = null;
        String source = null, destination = null, resourceTypeName = null, timestamp = null, uuid = null, uuidresource = null;
        long lastmodified = 0;
        int resourceTypeId = I_CmsConstants.C_UNKNOWN_ID;
        List properties = null;
        boolean old_overwriteCollidingResources = false;

        if (m_importingChannelData) {
            m_cms.getRequestContext().saveSiteRoot();
            m_cms.getRequestContext().setSiteRoot(I_CmsConstants.VFS_FOLDER_CHANNELS);
        }

        if (excludeList == null) {
            excludeList = new Vector();
        }

        try {
            m_webAppNames = getCompatibilityWebAppNames();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            m_report.println(e);
        }
        if (m_webAppNames == null) {
            m_webAppNames = Collections.EMPTY_LIST;
        }

        // get the old webapp url from the OpenCms properties
        m_webappUrl = OpenCms.getImportExportManager().getOldWebAppUrl();
        if (m_webappUrl == null) {
            // use a default value
            m_webappUrl = "http://localhost:8080/opencms/opencms";
        }
        // cut last "/" from webappUrl if present
        if (m_webappUrl.endsWith("/")) {
            m_webappUrl = m_webappUrl.substring(0, m_webappUrl.lastIndexOf("/"));
        }

        // get list of unwanted properties
        List deleteProperties = OpenCms.getImportExportManager().getIgnoredProperties();

        // get list of immutable resources
        List immutableResources = OpenCms.getImportExportManager().getImmutableResources();
        if (DEBUG > 0) {
            System.err.println("Import: Immutable resources size is " + immutableResources.size());
        }
        
        // save the value of the boolean flag whether colliding resources should be overwritten
        old_overwriteCollidingResources = OpenCms.getImportExportManager().overwriteCollidingResources();
        
        // force v1 and v2 imports to overwrite colliding resources, because they dont have resource 
        // UUIDs in their manifest anyway
        OpenCms.getImportExportManager().setOverwriteCollidingResources(true);
        
        try {
            // get all file-nodes
            fileNodes = m_docXml.selectNodes("//" + I_CmsConstants.C_EXPORT_TAG_FILE);
            int importSize = fileNodes.size();

            // walk through all files in manifest
            for (int i = 0; i < importSize; i++) {

                m_report.print(" ( " + (i + 1) + " / " + importSize + " ) ", I_CmsReport.C_FORMAT_NOTE);
                currentElement = (Element) fileNodes.get(i);

                // get all information for a file-import
                source = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_SOURCE);
                destination = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_DESTINATION);
                
                resourceTypeName = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_TYPE);
                if (C_RESOURCE_TYPE_NEWPAGE_NAME.equals(resourceTypeName)) {
                    resourceTypeId = C_RESOURCE_TYPE_NEWPAGE_ID;
                } else if (C_RESOURCE_TYPE_PAGE_NAME.equals(resourceTypeName)) {
                    // resource with a "legacy" resource type are imported using the "plain" resource
                    // type because you cannot import a resource without having the resource type object
                    resourceTypeId = CmsResourceTypePlain.C_RESOURCE_TYPE_ID;
                } else if (C_RESOURCE_TYPE_LINK_NAME.equals(resourceTypeName)) {
                    // set resource type of legacy "link" which is converted later
                    resourceTypeId = C_RESOURCE_TYPE_LINK_ID;
                } else {
                    I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resourceTypeName);
                    resourceTypeId = type.getTypeId();
                }
                
                uuid = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_UUIDSTRUCTURE);
                uuidresource = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_UUIDRESOURCE);

                if ((timestamp = CmsImport.getChildElementTextValue(currentElement, I_CmsConstants.C_EXPORT_TAG_LASTMODIFIED)) != null) {
                    lastmodified = Long.parseLong(timestamp);
                } else {
                    lastmodified = System.currentTimeMillis();
                }

                // if the type is "script" set it to plain
                if ("script".equals(resourceTypeName)) {
                    resourceTypeName = CmsResourceTypePlain.C_RESOURCE_TYPE_NAME;
                }

                String translatedName = m_cms.getRequestContext().addSiteRoot(m_importPath + destination);
                if (CmsResourceTypeFolder.C_RESOURCE_TYPE_NAME.equals(resourceTypeName)) {
                    translatedName += I_CmsConstants.C_FOLDER_SEPARATOR;
                }
                translatedName = m_cms.getRequestContext().getDirectoryTranslator().translateResource(translatedName);
                if (DEBUG > 3) {
                    System.err.println("Import: Translated resource name is " + translatedName);
                }

                boolean resourceNotImmutable = checkImmutable(translatedName, immutableResources);

                translatedName = m_cms.getRequestContext().removeSiteRoot(translatedName);
                if (resourceNotImmutable && (!excludeList.contains(translatedName))) {

                    // print out the information to the report
                    m_report.print(m_report.key("report.importing"), I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(translatedName);
                    m_report.print(m_report.key("report.dots"));

                    // get all properties
                    properties = readPropertiesFromManifest(currentElement, propertyName, propertyValue, deleteProperties);

                    // import the specified file 
                    CmsResource res = importResource(source, destination, uuid, uuidresource, resourceTypeId, resourceTypeName, lastmodified, properties, writtenFilenames, fileCodes);

                    if (res != null) {

                        // write all imported access control entries for this file
                        acentryNodes = currentElement.selectNodes("*/" + I_CmsConstants.C_EXPORT_TAG_ACCESSCONTROL_ENTRY);
                        // collect all access control entries
                        //String resid = getTextNodeValue(currentElement, C_EXPORT_TAG_ID);
                        for (int j = 0; j < acentryNodes.size(); j++) {
                            currentEntry = (Element)acentryNodes.get(j);
                            // get the data of the access control entry
                            String id = CmsImport.getChildElementTextValue(currentEntry, I_CmsConstants.C_EXPORT_TAG_ID);
                            String flags = CmsImport.getChildElementTextValue(currentEntry, I_CmsConstants.C_EXPORT_TAG_FLAGS);
                            String allowed = CmsImport.getChildElementTextValue(currentEntry, I_CmsConstants.C_EXPORT_TAG_ACCESSCONTROL_ALLOWEDPERMISSIONS);
                            String denied = CmsImport.getChildElementTextValue(currentEntry, I_CmsConstants.C_EXPORT_TAG_ACCESSCONTROL_DENIEDPERMISSIONS);

                            // add the entry to the list
                            addImportAccessControlEntry(res, id, allowed, denied, flags);
                        }
                        importAccessControlEntries(res);

                    } else {
                        // resource import failed, since no CmsResource was created
                        m_report.print(m_report.key("report.skipping"), I_CmsReport.C_FORMAT_OK);
                    }
                } else {
                    // skip the file import, just print out the information to the report
                    m_report.print(m_report.key("report.skipping"), I_CmsReport.C_FORMAT_NOTE);
                    m_report.println(translatedName);
                }
            }

            // now merge the body and page control files. this only has to be done if the import
            // version is below version 3
            if (getVersion() < 3 && m_convertToXmlPage) {
                mergePageFiles();
                removeFolders();
            }
        } catch (Exception exc) {
            exc.printStackTrace(System.err);
            m_report.println(exc);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        } finally {
            if (m_importingChannelData) {
                m_cms.getRequestContext().restoreSiteRoot();
            }
            
            // set the flag to overwrite colliding resources back to its original value
            OpenCms.getImportExportManager().setOverwriteCollidingResources(old_overwriteCollidingResources);
        }
    }

    /**
     * Imports a resource (file or folder) into the cms.<p>
     * 
     * @param source the path to the source-file
     * @param destination the path to the destination-file in the cms
     * @param uuid  the structure uuid of the resource
     * @param uuidresource  the resource uuid of the resource
     * @param resourceTypeId the ID of the file's resource type
     * @param resourceTypeName the name of the file's resource type
     * @param lastmodified the timestamp of the file
     * @param properties a hashtable with properties for this resource
     * @param writtenFilenames filenames of the files and folder which have actually been successfully written
     *       not used when null
     * @param fileCodes code of the written files (for the registry)
     *       not used when null
     * 
     * @return imported resource
     */
    private CmsResource importResource(String source, String destination, String uuid, String uuidresource, int resourceTypeId, String resourceTypeName, long lastmodified, List properties, Vector writtenFilenames, Vector fileCodes) {

        boolean success = true;
        byte[] content = null;
        String fullname = null;
        CmsResource res = null;

        try {
            
            if (m_importingChannelData) {
                // try to read an existing channel to get the channel id
                String channelId = null;
                try {
                    if ((resourceTypeName.equalsIgnoreCase(CmsResourceTypeFolder.C_RESOURCE_TYPE_NAME)) && (!destination.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR))) {
                        destination += I_CmsConstants.C_FOLDER_SEPARATOR;
                    }
                    CmsResource channel = m_cms.readResource(I_CmsConstants.C_ROOT + destination);
                    
                    
                    channelId = m_cms.readPropertyObject(m_cms.getSitePath(channel), I_CmsConstants.C_PROPERTY_CHANNELID, false).getValue();
                                        
                } catch (Exception e) {
                    // ignore the exception, a new channel id will be generated
                }
                // TODO: CW: remove
                /*
                if (channelId == null) {
                    // the channel id does not exist, so generate a new one
                    int newChannelId = CmsDbUtil.nextId(I_CmsConstants.C_TABLE_CHANNELID);
                    channelId = "" + newChannelId;
                }
                properties.add(new CmsProperty(I_CmsConstants.C_PROPERTY_CHANNELID, channelId, null));
                */
                if (channelId != null) {
                    properties.add(new CmsProperty(I_CmsConstants.C_PROPERTY_CHANNELID, channelId, null));
                }
            }

            // get the file content
            if (source != null) {
                content = getFileBytes(source);
            }
            
            content = convertContent(source, destination, content, resourceTypeName);
            
            // get all required information to create a CmsResource
            int size=0;
            if (content!=null) {
                size=content.length;
            }
            // get the required UUIDs         
            CmsUUID curUser=m_cms.getRequestContext().currentUser().getId();            
            CmsUUID newUuidstructure= new CmsUUID();
            CmsUUID newUuidresource = new CmsUUID();             
            if (uuid!=null) {
                newUuidstructure = new CmsUUID(uuid);       
            }
            if (uuidresource!=null) { 
                newUuidresource = new CmsUUID(uuidresource);               
            }             
            
            // extract the name of the resource form the destination
            String targetName=destination;
            if (targetName.endsWith("/")) {
                targetName=targetName.substring(0, targetName.length()-1);            
            }
            
            // create a new CmsResource                         
            CmsResource resource=new CmsResource(
                newUuidstructure, 
                newUuidresource,
                targetName,
                resourceTypeId,
                CmsFolder.isFolderType(resourceTypeId), 
                0,
                m_cms.getRequestContext().currentProject().getId(), 
                I_CmsConstants.C_STATE_NEW,
                lastmodified,
                curUser,
                lastmodified,
                curUser, CmsResource.DATE_RELEASED_DEFAULT, 
                CmsResource.DATE_EXPIRED_DEFAULT,
                1, 
                size
            );
                        
            if (C_RESOURCE_TYPE_LINK_ID == resourceTypeId) {
                // store links for later conversion
                m_report.print(m_report.key("report.storing_link"), I_CmsReport.C_FORMAT_NOTE);
                m_linkStorage.put(m_importPath + destination, new String(content));
                m_linkPropertyStorage.put(m_importPath + destination, properties);
                res = resource;
            } else {                                                                                                       
                //  import this resource in the VFS                         
                res = m_cms.importResource(m_importPath+destination, resource, content, properties);
            }   
            
            if (res != null) {
                if (C_RESOURCE_TYPE_PAGE_NAME.equals(resourceTypeName)) {
                    m_importedPages.add(I_CmsConstants.C_FOLDER_SEPARATOR + destination);
                }
                m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
            }
            
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

        byte[] digestContent = {0};
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
     * Performs all required pre-import steps.<p>
     * 
     * The content is *NOT* changed in the implementation of this class.<p>
     * 
     * @param source the source path of the resource
     * @param destination the destination path of the resource
     * @param content the content of the resource
     * @param resType the type of the resource
     * @return the (prepared) content of the resource
     */
    protected byte[] convertContent(String source, String destination, byte[] content, String resType) {
        // if the import is older than version 3, some additional conversions must be made
        if (getVersion() < 3) {
            if ("page".equals(resType)) {
                if (DEBUG > 0) {
                    System.err.println("#########################");
                    System.err.println("[" + this.getClass().getName() + ".convertContent()]: storing resource " + source + ".");
                }                
                // if the imported resource is a page, store its path inside the VFS for later
                // integration with its body
                m_pageStorage.add(destination);
            } else if ("folder".equals(resType)) {
                // check if the imported resource is a folder. Folders created in the /system/bodies/ folder
                if (destination.startsWith(I_CmsWpConstants.C_VFS_PATH_BODIES.substring(1))) {
                    // must be remove since we do not use body files anymore.
                    m_folderStorage.add(destination);
                }
            }
        }
        
        return content;
    }

    /**
     * Merges the page control files and their corresponding bodies into a single files.<p>
     * 
     * 
     * @throws CmsException if something goes wrong
     */
    private void mergePageFiles() throws CmsException {

        // check if the template property exists. If not, create it.
        try {
            m_cms.readPropertydefinition(I_CmsConstants.C_PROPERTY_TEMPLATE);
        } catch (CmsException e) {
            // the template propertydefintion does not exist. So create it.
            m_cms.createPropertydefinition(I_CmsConstants.C_PROPERTY_TEMPLATE);
        }
        // copy all propertydefinitions of the old page to the new page
        List definitions = m_cms.readAllPropertydefinitions();

        Iterator j = definitions.iterator();
        while (j.hasNext()) {
            CmsPropertydefinition definition = (CmsPropertydefinition)j.next();
            // check if this propertydef already exits
            try {
                m_cms.readPropertydefinition(definition.getName());
            } catch (Exception e) {
                m_cms.createPropertydefinition(definition.getName());
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

            m_report.print("( " + counter + " / " + size + " ) ", I_CmsReport.C_FORMAT_NOTE);
            m_report.print(m_report.key("report.merge") + " " , I_CmsReport.C_FORMAT_NOTE);
            m_report.print(resname, I_CmsReport.C_FORMAT_DEFAULT);

            try {
                
                mergePageFile(resname);
                
            } catch (Exception e) {
                m_report.println(e);
                // throw new CmsException(e.toString());
            }

            counter++;

        }
        // free mem
        m_pageStorage.clear();

    }

    /**
     * Merges a single page.<p>
     * 
     * @param resourcename the resource name of the page
     * @throws Exception if something goes wrong
     */
    private void mergePageFile(String resourcename) throws Exception {
        
        // get the header file
        CmsFile pagefile = m_cms.readFile(resourcename, CmsResourceFilter.IGNORE_EXPIRATION);
        Document contentXml = CmsImport.getXmlDocument(pagefile.getContents());
        
        // get the <masterTemplate> node to check the content.
        // this node contains the name of the template file
        List masterTemplateNode = contentXml.selectNodes("//masterTemplate");
        
        // there is only one <masterTemplate> allowed
        String mastertemplate = null;    
        if (masterTemplateNode.size() == 1) {
            // get the name of the mastertemplate
            mastertemplate = ((Element) masterTemplateNode.get(0)).getTextTrim();
        }
        
        // get the <ELEMENTDEF> nodes to check the content.
        // this node contains the information for the body element.
        List bodyNode = contentXml.selectNodes("//ELEMENTDEF");
        
        // there is only one <ELEMENTDEF> allowed
        if (bodyNode.size() == 1) {
            
            // get the elementdef
            Element bodyElement = (Element) bodyNode.get(0);
            List nodes = bodyElement.elements();
            int i;
            Node node = null;
            
            // get the class of the body template
            String bodyclass = null;
            for (i = 0; i < nodes.size(); i++) {
                node = (Node) nodes.get(i);
                if ("CLASS".equals(node.getName())) {
                    bodyclass = ((Element)node).getTextTrim();
                    break;
                }
            }
            
            // get the name of the body template
            String bodyname = null;          
            for (i = 0; i < nodes.size(); i++) {
                node = (Node) nodes.get(i);
                if ("TEMPLATE".equals(node.getName())) {
                    bodyname = ((Element)node).getTextTrim();
                    if (!bodyname.startsWith("/")) {
                        bodyname = CmsResource.getFolderPath(resourcename) + bodyname;
                    }
                    break;
                }
            }
            
            // get body template parameters if defined
            Map bodyparams = null;
            for (i = 0; i < nodes.size(); i++) {
                node = (Node) nodes.get(i);
                if ("PARAMETER".equals(node.getName())) {
                    Element paramElement = (Element) node;
                    if (bodyparams == null) {
                        bodyparams = new HashMap();
                    }
                    bodyparams.put((paramElement.attribute("name")).getText(), paramElement.getTextTrim());
                }
            }
            
            if ((mastertemplate == null) || (bodyname == null)) {
                // required XML nodes not found
                throw new CmsException("Could not merge page file '" + resourcename + "', bad XML structure!");
            }
            
            // lock the resource, so that it can be manipulated
            m_cms.lockResource(resourcename);
            
            // get all properties                               
            List properties = m_cms.readPropertyObjects(resourcename, false);
            
            // now get the content of the bodyfile and insert it into the control file                   
            CmsFile bodyfile = m_cms.readFile(bodyname, CmsResourceFilter.IGNORE_EXPIRATION);
            
            //get the encoding
            String encoding = null;                    
            encoding = CmsProperty.get(I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, properties).getValue();
            if (encoding == null) {
                encoding = OpenCms.getSystemInfo().getDefaultEncoding();
            }
                     
            if (m_convertToXmlPage) {
                // TODO: Check encoding
                CmsXmlPage xmlPage = CmsXmlPageConverter.convertToXmlPage(m_cms, new String(bodyfile.getContents(), encoding), "body", getLocale(resourcename, properties), encoding); 
                
                if (xmlPage != null) {
                    pagefile.setContents(xmlPage.marshal());
                    
                    // set the type to xml page
                    pagefile.setType(CmsResourceTypeXmlPage.C_RESOURCE_TYPE_ID);
                }
            }

            // add the template and other required properties
            CmsProperty newProperty;
            
            newProperty = new CmsProperty(I_CmsConstants.C_PROPERTY_TEMPLATE, mastertemplate, null);
            // property lists must not contain equal properties
            properties.remove(newProperty);            
            properties.add(newProperty);
            
            // if set, add the bodyclass as property
            if (bodyclass != null && !"".equals(bodyclass)) {
                newProperty = new CmsProperty(I_CmsConstants.C_PROPERTY_TEMPLATE, mastertemplate, null);
                newProperty.setAutoCreatePropertyDefinition(true);
                properties.remove(newProperty);            
                properties.add(newProperty);
            }
            // if set, add bodyparams as properties
            if (bodyparams != null) {
                for (Iterator p = bodyparams.keySet().iterator(); p.hasNext();) {
                    String key = (String)p.next();
                    newProperty = new CmsProperty(key, (String)bodyparams.get(key), null);
                    newProperty.setAutoCreatePropertyDefinition(true);
                    properties.remove(newProperty);            
                    properties.add(newProperty);
                }
            }
            
            // now import the resource
            m_cms.importResource(resourcename, pagefile, pagefile.getContents(), properties);

            // done, ulock the resource                   
            m_cms.unlockResource(resourcename);
            
            // finally delete the old body file, it is not needed anymore
            m_cms.lockResource(bodyname);
            m_cms.deleteResource(bodyname, I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
            m_report.println(" " + m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK); 
            
        } else {
            
            // there are more than one template nodes in this control file
            // convert the resource into a plain text file
            // lock the resource, so that it can be manipulated
            m_cms.lockResource(resourcename);
            // set the type to plain
            pagefile.setType(CmsResourceTypePlain.C_RESOURCE_TYPE_ID);
            // write all changes                     
            m_cms.writeFile(pagefile);
            // don, ulock the resource                   
            m_cms.unlockResource(resourcename);
            m_report.println(" " + m_report.key("report.notconverted"), I_CmsReport.C_FORMAT_OK);
        }
    }
    
    /**
     * Deletes the folder structure which has been creating while importing the body files..<p>
     *
     *      * @throws CmsException if something goes wrong
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
            resname = (resname.startsWith("/") ? "" : "/") + resname + (resname.endsWith("/") ? "" : "/");
            // now check if the folder is really empty. Only delete empty folders
            List files = m_cms.getFilesInFolder(resname, CmsResourceFilter.IGNORE_EXPIRATION);

            if (files.size() == 0) {
                List folders = m_cms.getSubFolders(resname, CmsResourceFilter.IGNORE_EXPIRATION);
                if (folders.size() == 0) {
                    m_report.print("( " + counter + " / " + size + " ) ",  I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(m_report.key("report.delfolder") + " " , I_CmsReport.C_FORMAT_NOTE);
                    m_report.print(resname, I_CmsReport.C_FORMAT_DEFAULT);
                    m_cms.lockResource(resname);
                    m_cms.deleteResource(resname, I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS);
                    m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
                    counter++;
                }
            }
        }
    }

    /**
     * Gets the encoding from the &lt;?XML ...&gt; tag if present.<p>
     * 
     * @param content the file content
     * @return String the found encoding
     */
    protected String getEncoding(String content) {
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
     * Sets the right encoding and returns the result.<p>
     * 
     * @param content the filecontent
     * @param encoding the encoding to use
     * @return modified content
     */
    protected String setEncoding(String content, String encoding) {
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
            actRule = CmsStringUtil.substitute(actRule, "/default/vfs", "");
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
                content = CmsStringUtil.substitutePerl(content, search, replace, "g");
            }
        }
        return content;
    }
    
    /**
     * Returns the compatibility web app names.<p>
     * 
     * @return the compatibility web app names
     */
    private List getCompatibilityWebAppNames() throws Exception {        
        
        String[] appNames = new ExtendedProperties((String)OpenCms.getRuntimeProperty(C_COMPATIBILITY_WEBAPPNAMES)).getStringArray(C_COMPATIBILITY_WEBAPPNAMES);
        // old web application names (for editor macro replacement) 
        if (appNames == null) {
            appNames = new String[0];
        }
        List webAppNamesOri = Arrays.asList(appNames);
        List webAppNames = new ArrayList();
        for (int i = 0; i < webAppNamesOri.size(); i++) {
            // remove possible white space
            String name = ((String)webAppNamesOri.get(i)).trim();
            if (name != null && !"".equals(name)) {
                webAppNames.add(name);
                if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                    OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Old context path     : " + (i + 1) + " - " + name);
                }
            }
        }
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Old context support  : " + ((webAppNames.size() > 0) ? "enabled" : "disabled"));
        }
        // check if list is null
        if (webAppNames == null) {
            webAppNames = new ArrayList();
        }
        // add current context to webapp names list
        if (!webAppNames.contains(OpenCms.getSystemInfo().getOpenCmsContext())) {
            webAppNames.add(OpenCms.getSystemInfo().getOpenCmsContext());
        }   
        
        return webAppNames;
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
     
        if (!"com.opencms.legacy.CmsLegacyPasswordHandler".equals(OpenCms.getPasswordHandler().getClass().getName())) {
            password = convertDigestEncoding(password);
        }
        super.importUser(name, description, flags, password, firstname, lastname, email, address, type, userInfo, userGroups);
    }
}
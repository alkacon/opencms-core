/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsExport.java,v $
 * Date   : $Date: 2003/07/09 10:58:09 $
 * Version: $Revision: 1.60 $
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

import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.flex.util.CmsUUID;
import com.opencms.report.CmsShellReport;
import com.opencms.report.I_CmsReport;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.util.Utils;
import com.opencms.workplace.I_CmsWpConstants;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Provides the functionality to export files from the OpenCms VFS to a ZIP file.<p>
 * 
 * The ZIP file written will contain a copy of all exported files with their contents.
 * It will also contain a <code>manifest.xml</code> file in wich all meta-information 
 * about this files are stored, like permissions etc.
 *
 * @author Andreas Schouten
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.60 $ $Date: 2003/07/09 10:58:09 $
 */
public class CmsExport implements I_CmsConstants, Serializable {

    // the tags for the manifest
    public static String C_EXPORT_TAG_FILES = "files";    
    public static String C_EXPORT_TAG_CHANNELS = "channels";
    public static String C_EXPORT_TAG_MASTERS = "masters";
    
    /** The xml elemtent to store user data and further information in */
    private Element m_userdataElement;

    /** Indicates if the system should be included to the export */
    private boolean m_excludeSystem;

    /** Indicated if the unchanged resources should be included to the export */
    private boolean m_excludeUnchanged;

    /** Indicates if the user data and group data should be included to the export */
    private boolean m_exportUserdata;
    
    /** Max file age of contents to export */
    private long m_contentAge = 0; 

    /** Indicates if the current project it the online project */
    private boolean m_isOnlineProject;

    /** Cache for previously added super folders */
    private Vector m_superFolders;
    
    /** Set to store the names of page files in, required for later page body file export */
    private Set m_exportedPageFiles = null;
    
    /** Set of all exported files, required for later page body file export */
    private Set m_exportedResources = null;
    
    /** Stores the id of the "page" resource type to save lookup time */
    private int m_pageType;
        
    /** Indicates if module data is exported */
    protected boolean m_exportingModuleData = false;   

    /** The export ZIP file to store resources in */
    protected String m_exportFile;

    /** The export ZIP stream to write resources to */
    protected ZipOutputStream m_exportZipStream = null;

    /** The CmsObject to do the operations */
    protected CmsObject m_cms;

    /** The xml manifest-file */
    protected Document m_docXml;

    /** The xml element to store file information in */
    protected Element m_filesElement;

    /** The xml-element to store masters information in */
    protected Element m_mastersElement;
    
    /** The report for the log messages */
    protected I_CmsReport m_report = null;
     
    /** The channelid and the resourceobject of the exported channels */
    protected Set m_exportedChannelIds = new HashSet();
    
    /**
     * Constructs a new uninitialized export, required for the module data export.<p>
     * 
     * @see CmsExportModuledata
     */
    public CmsExport() {
        // empty constructor
    }    
    
    /**
     * Constructs a new export.<p>
     *
     * @param cms the cmsObject to work with
     * @param exportFile the file or folder to export to
     * @param resourcesToExport the paths of folders and files to export
     * @param excludeSystem if true, the system folder is excluded, if false all the resources in
     *        resourcesToExport are included
     * @param excludeUnchanged <code>true</code>, if unchanged files should be excluded
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsExport(
        CmsObject cms, 
        String exportFile, 
        String[] resourcesToExport, 
        boolean excludeSystem, 
        boolean excludeUnchanged
    ) throws CmsException {
        this(cms, exportFile, resourcesToExport, excludeSystem, excludeUnchanged, null, false, 0, new CmsShellReport());
    }

    /**
     * Constructs a new export.<p>
     *
     * @param cms the cmsObject to work with
     * @param exportFile the file or folder to export to
     * @param resourcesToExport the paths of folders and files to export
     * @param excludeSystem if true, the system folder is excluded, if false all the resources in
     *        resourcesToExport are included
     * @param excludeUnchanged <code>true</code>, if unchanged files should be excluded
     * @param moduleNode module informations in a Node for module export
     * @param exportUserdata if true, the user and grou pdata will also be exported
     * @param contentAge export contents changed after this date/time
     * @param report to handle the log messages
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsExport(
        CmsObject cms, 
        String exportFile, 
        String[] resourcesToExport, 
        boolean excludeSystem, 
        boolean excludeUnchanged, 
        Node moduleNode, 
        boolean exportUserdata, 
        long contentAge, 
        I_CmsReport report
    ) throws CmsException {                       

        m_exportFile = exportFile;
        m_cms = cms;
        m_excludeSystem = excludeSystem;
        m_excludeUnchanged = excludeUnchanged;
        m_exportUserdata = exportUserdata;
        m_isOnlineProject = cms.getRequestContext().currentProject().isOnlineProject();
        m_contentAge = contentAge;
        m_report = report;
        m_exportingModuleData = false;

        openExportFile(moduleNode);

        // export all the resources
        exportAllResources(resourcesToExport);

        // export userdata and groupdata if desired
        if(m_exportUserdata){
            exportGroups();
            exportUsers();
        }
                
        closeExportFile();
    }
        
    /**
     * Opens the export ZIP file and initializes the internal XML document for the manifest.<p>
     * 
     * @param moduleNode optional modul node if a module is to be exported
     * @throws CmsException if something goes wrong
     */
    protected void openExportFile(Node moduleNode) throws CmsException {
        // open the export resource
        getExportResource();

        // create the xml-config file
        getXmlConfigFile(moduleNode);        
    }
    
    /**
     * Closes the export ZIP file and saves the internal XML document for the manifest.<p>
     * 
     * @throws CmsException if something goes wrong
     */    
    protected void closeExportFile() throws CmsException {
        // write the document to the zip-file
        writeXmlConfigFile();

        try {
            m_exportZipStream.close();
        } catch(IOException exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }        
    }
        
    /**
     * Exports all resources and possible sub-folders form the provided list of resources.
     * 
     * @param resourcesToExport the list of resources to export
     * @throws CmsException if something goes wrong
     */
    protected void exportAllResources(String[] resourcesToExport) throws CmsException {
                 
        // distinguish folder and file names   
        Vector folderNames = new Vector();
        Vector fileNames = new Vector();
        for (int i=0; i<resourcesToExport.length; i++) {
            if (resourcesToExport[i].endsWith(C_FOLDER_SEPARATOR)) {
                folderNames.addElement(resourcesToExport[i]);
            } else {
                fileNames.addElement(resourcesToExport[i]);
            }
        }
                
        // remove the possible redundancies in the list of resources
        checkRedundancies(folderNames, fileNames);
        
        // init sets required for the body file exports 
        m_exportedPageFiles = new HashSet();
        m_exportedResources = new HashSet();
        
        // set the "page" file resource type id
        m_pageType = m_cms.getResourceType("page").getResourceType();
                
        // export the folders
        for (int i=0; i<folderNames.size(); i++) {
            String path = (String) folderNames.elementAt(i);
            // first add superfolders to the xml-config file
            addSuperFolders(path);
            exportResources(path);
            m_exportedResources.add(path);
        }
        
        // export the single files
        addSingleFiles(fileNames);

        // export all body files that have not already been exported
        addPageBodyFiles();        
    }        
        
    /**
     * Adds a CDATA element to the XML document.<p>
     * 
     * @param docXml Document to create the new element in
     * @param element the element to add the subelement to
     * @param name the name of the new subelement
     * @param value the value of the element
     */
    protected void addCdataElement(Document docXml, Element element, String name, String value) {
        Element newElement = docXml.createElement(name);
        element.appendChild(newElement);
        CDATASection text = docXml.createCDATASection(value);
        newElement.appendChild(text);
    }
    
    /**
     * Adds a text element to the XML document.<p>
     * 
     * @param docXml Document to create the new element in
     * @param element the element to add the subelement to
     * @param name the name of the new subelement
     * @param value the value of the element
     */
    protected void addElement(Document docXml, Element element, String name, String value) {
        Element newElement = docXml.createElement(name);
        element.appendChild(newElement);
        Text text = docXml.createTextNode(value);
        newElement.appendChild(text);
    }
    
    /**
     * Creates the export XML file and appends the initial tags to it.<p>
     * 
     * @param moduleNode a node with module informations
     * @throws CmsException if something goes wrong
     */
    private void getXmlConfigFile(Node moduleNode) throws CmsException {

        try {
            // creates the document
            if (m_exportingModuleData) {
                // module (COS) data is exported
                m_docXml = A_CmsXmlContent.getXmlParser().createEmptyDocument(C_EXPORT_TAG_MODULEXPORT);                
            } else {
                // standard export, only VFS resources are exported
                m_docXml = A_CmsXmlContent.getXmlParser().createEmptyDocument(C_EXPORT_TAG_EXPORT);
            }
            // abbends the initital tags
            Node exportNode = m_docXml.getFirstChild();

            // add the info element. it contains all infos for this export
            Element info = m_docXml.createElement(C_EXPORT_TAG_INFO);
            m_docXml.getDocumentElement().appendChild(info);
            addElement(m_docXml, info, C_EXPORT_TAG_CREATOR, m_cms.getRequestContext().currentUser().getName());
            addElement(m_docXml, info, C_EXPORT_TAG_OC_VERSION, A_OpenCms.getVersionName());
            addElement(m_docXml, info, C_EXPORT_TAG_DATE, Utils.getNiceDate(new Date().getTime()));
            addElement(m_docXml, info, C_EXPORT_TAG_PROJECT, m_cms.getRequestContext().currentProject().getName());
            addElement(m_docXml, info, C_EXPORT_TAG_VERSION, C_EXPORT_VERSION);

            if (moduleNode != null) {
                // this is a module export - import module informations here
                exportNode.appendChild(A_CmsXmlContent.getXmlParser().importNode(m_docXml, moduleNode));
                // now remove the unused file informations
                NodeList files = ((Element)exportNode).getElementsByTagName("files");
                if (files.getLength() == 1) {
                    files.item(0).getParentNode().removeChild(files.item(0));
                }
            }
            if (m_exportingModuleData) {
                // add the root element for the channels
                m_filesElement = m_docXml.createElement(C_EXPORT_TAG_CHANNELS);
                m_docXml.getDocumentElement().appendChild(m_filesElement);
                // add the root element for the masters
                m_mastersElement = m_docXml.createElement(C_EXPORT_TAG_MASTERS);
                m_docXml.getDocumentElement().appendChild(m_mastersElement);
            } else {
                // standard export, add not for the files
                m_filesElement = m_docXml.createElement(C_EXPORT_TAG_FILES);
                m_docXml.getDocumentElement().appendChild(m_filesElement);
                
            }
            if (m_exportUserdata) {
                // add userdata
                m_userdataElement = m_docXml.createElement(C_EXPORT_TAG_USERGROUPDATA);
                m_docXml.getDocumentElement().appendChild(m_userdataElement);
            }
        } catch (Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }
    
    /**
     * Writes the <code>manifex.xml</code> configuration file to the esport zip file.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    private void writeXmlConfigFile() throws CmsException {
        try {
            ZipEntry entry = new ZipEntry(C_EXPORT_XMLFILENAME);
            m_exportZipStream.putNextEntry(entry);
            A_CmsXmlContent.getXmlParser().getXmlText(m_docXml, m_exportZipStream, A_OpenCms.getDefaultEncoding());
            m_exportZipStream.closeEntry();
        } catch (Exception exc) {
            m_report.println(exc);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Gets the export resource and stores it in a member variable.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    private void getExportResource() throws CmsException {
        try {
            // add zip-extension, if needed
            if (!m_exportFile.toLowerCase().endsWith(".zip")) {
                m_exportFile += ".zip";
            }
            // create the export-zipstream
            m_exportZipStream = new ZipOutputStream(new FileOutputStream(m_exportFile));
        } catch (Exception exc) {
            m_report.println(exc);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Exports all page body files that have not explicityl been added by the user.<p>
     * 
     * @throws CmsException if something goes wrong
     */
    private void addPageBodyFiles() throws CmsException {
        Iterator i;   
        
        Vector bodyFileNames = new Vector();
        String bodyPath = I_CmsWpConstants.C_VFS_PATH_BODIES.substring(0, I_CmsWpConstants.C_VFS_PATH_BODIES.lastIndexOf("/"));
        
        // check all exported page files if their body has already been exported
        i = m_exportedPageFiles.iterator();
        while (i.hasNext()) {
            String body = bodyPath + (String)i.next();
            if (! isRedundant(body)) {
                bodyFileNames.add(body);
            }
        }
        
        // now export the body files that have not already been exported
        addSingleFiles(bodyFileNames);
    }

    /**
     * Adds all files with names in fileNames to the xml-config file.<p>
     * 
     * @param fileNames Vector of path Strings, e.g. <code>/folder/index.html</code>
     * @throws CmsException if something goes wrong
     */
    private void addSingleFiles(Vector fileNames) throws CmsException {
        if (fileNames != null) {
            for (int i = 0; i < fileNames.size(); i++) {
                String fileName = (String) fileNames.elementAt(i);
                try {
                    CmsFile file = m_cms.readFile(fileName);
                    if((file.getState() != C_STATE_DELETED) && (!file.getName().startsWith("~"))) {
                        addSuperFolders(fileName);
                        exportResource(file);
                        m_exportedResources.add(fileName);
                    }
                } catch(CmsException exc) {
                    if(exc.getType() != CmsException.C_RESOURCE_DELETED) {
                        throw exc;
                    }
                }
            }
        }
    }

    /**
     * Adds the superfolders of path to the config file, starting at the top, 
     * excluding the root folder.<p>
     * 
     * @param path the path of the folder in the virtual files system (VFS) 
     * @throws CmsException if something goes wrong
     */
    private void addSuperFolders(String path) throws CmsException {
        // Initialize the "previously added folder cache"
        if (m_superFolders == null) {
            m_superFolders = new Vector();
        }
        Vector superFolders = new Vector();

        // Check, if the path is really a folder
        if (path.lastIndexOf(C_ROOT) != (path.length() - 1)) {
            path = path.substring(0, path.lastIndexOf(C_ROOT) + 1);
        }
        while (path.length() > C_ROOT.length()) {
            superFolders.addElement(path);
            path = path.substring(0, path.length() - 1);
            path = path.substring(0, path.lastIndexOf(C_ROOT) + 1);
        }
        for (int i = superFolders.size() - 1; i >= 0; i--) {
            String addFolder = (String)superFolders.elementAt(i);
            if (!m_superFolders.contains(addFolder)) {
                // This super folder was NOT added previously. Add it now!
                CmsFolder folder = m_cms.readFolder(addFolder);
                writeXmlEntrys(folder);
                // Remember that this folder was added
                m_superFolders.addElement(addFolder);
            }
        }
    }

    /**
     * Checks if a given resource is already included in the export
     * or not.<p>
     * 
     * @param resourcename the VFS resource name to check
     * @return <code>true</code> if the resource must not be exported again, <code>false</code> otherwise
     */
    private boolean isRedundant(String resourcename) {
        if (m_exportedResources == null)
            return false;
        Iterator i = m_exportedResources.iterator();
        while (i.hasNext()) {
            String s = (String)i.next();
            if (resourcename.startsWith(s))
                return true;
        }
        return false;
    }

    /** 
     * Checks whether some of the resources are redundant because a superfolder has also
     * been selected or a file is included in a folder.<p>
     * 
     * @param folderNames contains the full pathnames of all folders
     * @param fileNames contains the full pathnames of all files
     */
    private void checkRedundancies(Vector folderNames, Vector fileNames) {
        int i, j;
        if (folderNames == null) {
            return;
        }
        Vector redundant = new Vector();
        int n = folderNames.size();
        if (n > 1) {
            // otherwise no check needed, because there is only one resource

            for (i = 0; i < n; i++) {
                redundant.addElement(new Boolean(false));
            }
            for (i = 0; i < n - 1; i++) {
                for (j = i + 1; j < n; j++) {
                    if (((String)folderNames.elementAt(i)).length() < ((String)folderNames.elementAt(j)).length()) {
                        if (((String)folderNames.elementAt(j)).startsWith((String)folderNames.elementAt(i))) {
                            redundant.setElementAt(new Boolean(true), j);
                        }
                    } else {
                        if (((String)folderNames.elementAt(i)).startsWith((String)folderNames.elementAt(j))) {
                            redundant.setElementAt(new Boolean(true), i);
                        }
                    }
                }
            }
            for (i = n - 1; i >= 0; i--) {
                if (((Boolean)redundant.elementAt(i)).booleanValue()) {
                    folderNames.removeElementAt(i);
                }
            }
        }
        // now remove the files who are included automatically in a folder
        // otherwise there would be a zip exception

        for (i = fileNames.size() - 1; i >= 0; i--) {
            for (j = 0; j < folderNames.size(); j++) {
                if (((String)fileNames.elementAt(i)).startsWith((String)folderNames.elementAt(j))) {
                    fileNames.removeElementAt(i);
                }
            }
        }
    }
    
    /**
     * Exports one single file with all its data and content.<p>
     *
     * @param file the file to be exported
     * @throws CmsException if something goes wrong
     */
    private void exportResource(CmsFile file) throws CmsException {
        String source = getSourceFilename(m_cms.readAbsolutePath(file));

        m_report.print(m_report.key("report.exporting"), I_CmsReport.C_FORMAT_NOTE);
        m_report.print(m_cms.readAbsolutePath(file));
        m_report.print(m_report.key("report.dots"), I_CmsReport.C_FORMAT_NOTE);
        try {
            // create the manifest-entrys
            writeXmlEntrys((CmsResource)file);
            // store content in zip-file
            ZipEntry entry = new ZipEntry(source);
            m_exportZipStream.putNextEntry(entry);
            m_exportZipStream.write(file.getContents());
            m_exportZipStream.closeEntry();
        } catch (Exception exc) {
            m_report.println(exc);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }

        if (file.getType() == m_pageType) {
            m_exportedPageFiles.add(m_cms.readAbsolutePath(file));
        }

        m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
    }
    
    /**
     * Exports all needed sub-resources to the zip-file.<p>
     *
     * @param path to complete path to the resource to export
     * @throws CmsException if something goes wrong
     */
    private void exportResources(String path) throws CmsException {
        
        if (m_exportingModuleData) {
            // collect channel id information if required
            String channelId = m_cms.readProperty(path, I_CmsConstants.C_PROPERTY_CHANNELID);
            if (channelId != null) {
                if(! m_exportedChannelIds.contains(channelId)) {
                    m_exportedChannelIds.add(channelId);
                }
            }
        }
                
        // get all subFolders
        List subFolders = m_cms.getSubFolders(path);
        // get all files in folder
        List subFiles = m_cms.getFilesInFolder(path);

        // walk through all files and export them
        for (int i = 0; i < subFiles.size(); i++) {
            CmsResource file = (CmsResource)subFiles.get(i);
            int state = file.getState();
            long age = file.getDateLastModified();

            if (m_isOnlineProject || (!m_excludeUnchanged) || state == C_STATE_NEW || state == C_STATE_CHANGED) {
                if ((state != C_STATE_DELETED) && (!file.getName().startsWith("~")) && (age >= m_contentAge)) {
                    exportResource(m_cms.readFile(m_cms.readAbsolutePath(file)));
                }
            }
            // release file header memory
            subFiles.set(i, null);
        }
        // all files are exported, release memory
        subFiles = null;

        // walk through all subfolders and export them
        for (int i = 0; i < subFolders.size(); i++) {
            CmsResource folder = (CmsResource)subFolders.get(i);
            if (folder.getState() != C_STATE_DELETED) {
                // check if this is a system-folder and if it should be included.
                String export = m_cms.readAbsolutePath(folder);
                if (// new VFS, always export "/system/" OR
                 (I_CmsWpConstants.C_VFS_NEW_STRUCTURE
                    && export.equalsIgnoreCase(I_CmsWpConstants.C_VFS_PATH_SYSTEM))
                    || // new VFS, always export "/system/bodies/" OR
                 (
                        I_CmsWpConstants.C_VFS_NEW_STRUCTURE && export.startsWith(I_CmsWpConstants.C_VFS_PATH_BODIES))
                    || // new VFS, always export "/system/galleries/" OR
                 (
                        I_CmsWpConstants.C_VFS_NEW_STRUCTURE
                            && export.startsWith(I_CmsWpConstants.C_VFS_PATH_GALLERIES))
                    || // option "exclude system folder" selected AND
                !(
                        m_excludeSystem
                    && // export folder is a system folder 
                 (
                        export.startsWith(I_CmsWpConstants.C_VFS_PATH_SYSTEM)
                    || // if new VFS, ignore old system folders (are below "/system" in new VFS)
                 (
                        (!I_CmsWpConstants.C_VFS_NEW_STRUCTURE) && export.startsWith("/pics/system/"))
                            || ((!I_CmsWpConstants.C_VFS_NEW_STRUCTURE) && export.startsWith("/moduledemos/"))))) {
                    // export this folder
                    if (folder.getDateLastModified() >= m_contentAge) {
                        // only export folder data to manifest.xml if it has changed
                        writeXmlEntrys(folder);
                    }
                    // export all resources in this folder
                    exportResources(m_cms.readAbsolutePath(folder));
                }
            }
            // release folder memory
            subFolders.set(i, null);
        }
    }
        
    /**
     * Substrings the source filename, so it is shrinked to the needed part for export.<p>
     * 
     * @param absoluteName the absolute path of the resource
     * @return the shrinked path
     */
    private String getSourceFilename(String absoluteName) {
        String path = absoluteName; // keep absolute name to distinguish resources
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }
    
    /**
     * Writes the data for a resource (like access-rights) to the <code>manifest.xml</code> file.<p>
     * 
     * @param resource the resource to get the data from
     * @throws CmsException if something goes wrong
     */
    private void writeXmlEntrys(CmsResource resource) throws CmsException {
        String source, type, /*user, group, access,*/ launcherStartClass, lastModified;

        // get all needed informations from the resource
        source = getSourceFilename(m_cms.readAbsolutePath(resource));
        type = m_cms.getResourceType(resource.getType()).getResourceTypeName();
        // TODO: fix this later
        // user = m_cms.readOwner(resource).getName();
        // group = m_cms.readGroup(resource).getName();
        // access = resource.getAccessFlags() + "";
        launcherStartClass = resource.getLauncherClassname();
        lastModified = String.valueOf(resource.getDateLastModified()); 

        // write these informations to the xml-manifest
        Element file = m_docXml.createElement(C_EXPORT_TAG_FILE);
        m_filesElement.appendChild(file);

        // only write source if resource is a file
        if (resource.isFile()) {
            addElement(m_docXml, file, C_EXPORT_TAG_SOURCE, source);
        } else {
            // output something to the report for the folder
            m_report.print(m_report.key("report.exporting"), I_CmsReport.C_FORMAT_NOTE);
            m_report.print(m_cms.readAbsolutePath(resource));
            m_report.print(m_report.key("report.dots"), I_CmsReport.C_FORMAT_NOTE);
            m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
        }
        addElement(m_docXml, file, C_EXPORT_TAG_DESTINATION, source);
        addElement(m_docXml, file, C_EXPORT_TAG_TYPE, type);
        // addElement(m_docXml, file, C_EXPORT_TAG_USER, user);
        // addElement(m_docXml, file, C_EXPORT_TAG_GROUP, group);
        // addElement(m_docXml, file, C_EXPORT_TAG_ACCESS, access);
        addElement(m_docXml, file, C_EXPORT_TAG_LASTMODIFIED, lastModified);
        if (launcherStartClass != null
            && !"".equals(launcherStartClass)
            && !C_UNKNOWN_LAUNCHER.equals(launcherStartClass)) {
            addElement(m_docXml, file, C_EXPORT_TAG_LAUNCHER_START_CLASS, launcherStartClass);
        }

        // append the node for properties
        Element properties = m_docXml.createElement(C_EXPORT_TAG_PROPERTIES);
        file.appendChild(properties);

        // read the properties
        Map fileProperties = m_cms.readProperties(m_cms.readAbsolutePath(resource));
        Iterator i = fileProperties.keySet().iterator();

        // create xml-elements for the properties
        while (i.hasNext()) {
            String key = (String)i.next();
            // make sure channel id property is not exported with module data
            if ((! m_exportingModuleData) || (! I_CmsConstants.C_PROPERTY_CHANNELID.equals(key))) {            
                // append the node for a property
                Element property = m_docXml.createElement(C_EXPORT_TAG_PROPERTY);
                properties.appendChild(property);
    
                String value = (String)fileProperties.get(key);
                String propertyType = m_cms.readPropertydefinition(key, type).getType() + "";
    
                addElement(m_docXml, property, C_EXPORT_TAG_NAME, key);
                addElement(m_docXml, property, C_EXPORT_TAG_TYPE, propertyType);
                addCdataElement(m_docXml, property, C_EXPORT_TAG_VALUE, value);
            }
        }
        
        // append the nodes for access control entries
        Element acentries = m_docXml.createElement(C_EXPORT_TAG_ACCESSCONTROL_ENTRIES);
        file.appendChild(acentries);
        // TODO: this should be already available in the resource
        addElement(m_docXml, acentries, C_EXPORT_TAG_ID, resource.getResourceAceId().toString());
        
        // read the access control entries
        Vector fileAcEntries = m_cms.getAccessControlEntries(m_cms.readAbsolutePath(resource), false);
        i = fileAcEntries.iterator();
        
        // create xml elements for each access control entry
        while (i.hasNext()) {
        	CmsAccessControlEntry ace = (CmsAccessControlEntry)i.next();
        	Element acentry = m_docXml.createElement(C_EXPORT_TAG_ACCESSCONTROL_ENTRY);
        	acentries.appendChild(acentry);
        	
        	addElement(m_docXml, acentry, C_EXPORT_TAG_ID, ace.getPrincipal().toString());
        	addElement(m_docXml, acentry, C_EXPORT_TAG_FLAGS, new Integer(ace.getFlags()).toString());
        	
        	Element acpermissionset = m_docXml.createElement(C_EXPORT_TAG_ACCESSCONTROL_PERMISSIONSET); 
        	acentry.appendChild(acpermissionset);
        	addElement(m_docXml, acpermissionset, C_EXPORT_TAG_ACCESSCONTROL_ALLOWEDPERMISSIONS, new Integer(ace.getAllowedPermissions()).toString());
        	addElement(m_docXml, acpermissionset, C_EXPORT_TAG_ACCESSCONTROL_DENIEDPERMISSIONS, new Integer(ace.getDeniedPermissions()).toString());
        }
    }

    /**
     * Exports all groups with all data.<p>
     *
     * @throws CmsException if something goes wrong
     */
    private void exportGroups() throws CmsException {
        Vector allGroups = m_cms.getGroups();
        for (int i = 0; i < allGroups.size(); i++){
            exportGroup((CmsGroup)allGroups.elementAt(i));
        }
    }

    /**
     * Exports all users with all data.<p>
     *
     * @throws CmsException if something goes wrong
     */
    private void exportUsers() throws CmsException {
        Vector allUsers = m_cms.getUsers();
        for (int i = 0; i < allUsers.size(); i++){
            exportUser((CmsUser)allUsers.elementAt(i));
        }
    }

    /**
     * Exports one single group with all it's data.<p>
     *
     * @param group the group to be exported
     * @throws CmsException if something goes wrong
     */
    private void exportGroup(CmsGroup group) throws CmsException {
        m_report.print(m_report.key("report.exporting_group"), I_CmsReport.C_FORMAT_NOTE);
        m_report.print(group.getName());
        m_report.print(m_report.key("report.dots"), I_CmsReport.C_FORMAT_NOTE);
        try {
            // create the manifest entries
            writeXmlGroupEntrys(group);
        } catch (Exception e) {
            m_report.println(e);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, e);
        }
        m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
    }

    /**
     * Exports one single user with all its data.<p>
     *
     * @param user the user to be exported
     * @throws CmsException if something goes wrong
     */
    private void exportUser(CmsUser user) throws CmsException {
        m_report.print(m_report.key("report.exporting_user"), I_CmsReport.C_FORMAT_NOTE);
        m_report.print(user.getName());
        m_report.print(m_report.key("report.dots"), I_CmsReport.C_FORMAT_NOTE);
        try {
            // create the manifest entries
            writeXmlUserEntrys(user);
        } catch (Exception e) {
            m_report.println(e);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, e);
        }
        m_report.println(m_report.key("report.ok"), I_CmsReport.C_FORMAT_OK);
    }

    /**
     * Writes the data for a group to the <code>manifest.xml</code> file.<p>
     * 
     * @param group the group to get the data from
     * @throws CmsException if something goes wrong
     */
    private void writeXmlGroupEntrys(CmsGroup group) throws CmsException {
        String id, name, description, flags, parentgroup;

        // get all needed information from the group
        id = group.getId().toString();
        name = group.getName();
        description = group.getDescription();
        flags = Integer.toString(group.getFlags());
        CmsUUID parentId = group.getParentId();
        if (!parentId.isNullUUID()) {
            parentgroup = m_cms.getParent(name).getName();
        } else {
            parentgroup = "";
        }

        // write these informations to the xml-manifest
        Element groupdata = m_docXml.createElement(C_EXPORT_TAG_GROUPDATA);
        m_userdataElement.appendChild(groupdata);

		addElement(m_docXml, groupdata, C_EXPORT_TAG_ID, id);
        addElement(m_docXml, groupdata, C_EXPORT_TAG_NAME, name);
        addCdataElement(m_docXml, groupdata, C_EXPORT_TAG_DESCRIPTION, description);
        addElement(m_docXml, groupdata, C_EXPORT_TAG_FLAGS, flags);
        addElement(m_docXml, groupdata, C_EXPORT_TAG_PARENTGROUP, parentgroup);
    }

    /**
     * Writes the data for a user to the <code>manifest.xml</code> file.<p>
     * 
     * @param user The user to write into the manifest.
     * @throws CmsException if something goes wrong
     */
    private void writeXmlUserEntrys(CmsUser user) throws CmsException {
        String id, name, password, recoveryPassword, description, firstname;
        String lastname, email, flags, defaultGroup, address, section, type;
        String datfileName = new String();
        Hashtable info = new Hashtable();
        Vector userGroups = new Vector();
        sun.misc.BASE64Encoder enc;
        ObjectOutputStream oout;

        // get all needed information from the group
        id = user.getId().toString();
        name = user.getName();
        password = user.getPassword();
        recoveryPassword = user.getRecoveryPassword();
        description = user.getDescription();
        firstname = user.getFirstname();
        lastname = user.getLastname();
        email = user.getEmail();
        flags = Integer.toString(user.getFlags());
        info = user.getAdditionalInfo();
        defaultGroup = user.getDefaultGroup().getName();
        address = user.getAddress();
        section = user.getSection();
        type = Integer.toString(user.getType());
        userGroups = m_cms.getDirectGroupsOfUser(user.getName());

        // write these informations to the xml-manifest
        Element userdata = m_docXml.createElement(C_EXPORT_TAG_USERDATA);
        m_userdataElement.appendChild(userdata);

		addElement(m_docXml, userdata, C_EXPORT_TAG_ID, id);
        addElement(m_docXml, userdata, C_EXPORT_TAG_NAME, name);
        //Encode the info value, using any base 64 decoder
        enc = new sun.misc.BASE64Encoder();
        String passwd = new String(enc.encodeBuffer(password.getBytes()));
        addCdataElement(m_docXml, userdata, C_EXPORT_TAG_PASSWORD, passwd);
        enc = new sun.misc.BASE64Encoder();
        String recPasswd = new String(enc.encodeBuffer(recoveryPassword.getBytes()));
        addCdataElement(m_docXml, userdata, C_EXPORT_TAG_RECOVERYPASSWORD, recPasswd);

        addCdataElement(m_docXml, userdata, C_EXPORT_TAG_DESCRIPTION, description);
        addElement(m_docXml, userdata, C_EXPORT_TAG_FIRSTNAME, firstname);
        addElement(m_docXml, userdata, C_EXPORT_TAG_LASTNAME, lastname);
        addElement(m_docXml, userdata, C_EXPORT_TAG_EMAIL, email);
        addElement(m_docXml, userdata, C_EXPORT_TAG_FLAGS, flags);
        addElement(m_docXml, userdata, C_EXPORT_TAG_DEFAULTGROUP, defaultGroup);
        addCdataElement(m_docXml, userdata, C_EXPORT_TAG_ADDRESS, address);
        addElement(m_docXml, userdata, C_EXPORT_TAG_SECTION, section);
        addElement(m_docXml, userdata, C_EXPORT_TAG_TYPE, type);
        // serialize the hashtable and write the info into a file
        try {
            datfileName = "/~" + C_EXPORT_TAG_USERINFO + "/" + name + ".dat";
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            oout = new ObjectOutputStream(bout);
            oout.writeObject(info);
            oout.close();
            byte[] serializedInfo = bout.toByteArray();
            // store the userinfo in zip-file
            ZipEntry entry = new ZipEntry(datfileName);
            m_exportZipStream.putNextEntry(entry);
            m_exportZipStream.write(serializedInfo);
            m_exportZipStream.closeEntry();
        } catch (IOException ioex) {
            m_report.println(ioex);
        }
        // create tag for userinfo
        addCdataElement(m_docXml, userdata, C_EXPORT_TAG_USERINFO, datfileName);

        // append the node for groups of user
        Element usergroup = m_docXml.createElement(C_EXPORT_TAG_USERGROUPS);
        userdata.appendChild(usergroup);
        for (int i = 0; i < userGroups.size(); i++) {
            String groupName = ((CmsGroup)userGroups.elementAt(i)).getName();
            Element group = m_docXml.createElement(C_EXPORT_TAG_GROUPNAME);
            usergroup.appendChild(group);
            addElement(m_docXml, group, C_EXPORT_TAG_NAME, groupName);
        }
    }
}

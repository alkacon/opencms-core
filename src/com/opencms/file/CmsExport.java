/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsExport.java,v $
* Date   : $Date: 2002/12/06 23:16:45 $
* Version: $Revision: 1.38 $
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
import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.report.*;
import org.w3c.dom.*;

import com.opencms.util.*;
import com.opencms.workplace.I_CmsWpConstants;

/**
 * This class holds the functionaility to export resources from the cms
 * to the filesystem.
 *
 * @author Andreas Schouten
 * @version $Revision: 1.38 $ $Date: 2002/12/06 23:16:45 $
 */
public class CmsExport implements I_CmsConstants, Serializable {

    /**
     * The export-zipfile to store resources to
     */
    private String m_exportFile;

    /**
     * The export-stream (zip) to store resources to
     */
    private ZipOutputStream m_exportZipStream = null;

    /**
     * The export-path to read resources from the cms.
     */
    private String m_exportPath;

    /**
     * The cms-object to do the operations.
     */
    private CmsObject m_cms;

    /**
     * The xml manifest-file.
     */
    private Document m_docXml;

    /**
     * The xml-elemtent to store fileinformations to.
     */
    private Element m_filesElement;

    /**
     * The xml-elemtent to store userdatainformations to.
     */
    private Element m_userdataElement;

    /**
     * Decides, if the system should be included to the export.
     */
    private boolean m_excludeSystem;

    /**
     * Decides, if the unchanged resources should be included to the export.
     */
    private boolean m_excludeUnchanged;

    /**
     * Decides, if the userdata and groupdata should be included to the export.
     */
    private boolean m_exportUserdata;
    
    /** Max file age of contents to export */
    private long m_contentAge = 0; 

    /**
     * Is the current project the online project?
     */
    private boolean m_isOnlineProject;

    /**
     * Cache for previously added super folders
     */
    private Vector m_superFolders;

    /**
     * The object to report the log-messages.
     */
    private I_CmsReport m_report = null;

    /** Minimum file change date for exporting */
    private long m_minFileChangeDate = Long.MIN_VALUE;
    
    /** Set to store the names of page files in, required for later page body file export */
    private Set m_exportedPageFiles = null;
    
    /** Set of all exported files, required for later page body file export */
    private Set m_exportedResources = null;
    
    /** Stores the id of the "page" resource type to save lookup time */
    private int m_pageType;

    /**
     * This constructs a new CmsImport-object which imports the resources.
     *
     * @param importFile the file or folder to import from.
     * @param importPath the path to the cms to import into.
     * @param cms the cms-object to work with.
     * @exception CmsException the CmsException is thrown if something goes wrong.
     */
    public CmsExport(String exportFile, String[] exportPaths, CmsObject cms)
        throws CmsException {
        this(exportFile, exportPaths, cms, false, false);
    }
    /**
     * This constructs a new CmsImport-object which imports the resources.
     *
     * @param importFile the file or folder to import from.
     * @param importPath the path to the cms to import into.
     * @param cms the cms-object to work with.
     * @param Node moduleNode module informations in a Node for module-export.
     * @exception CmsException the CmsException is thrown if something goes wrong.
     */
    public CmsExport(String exportFile, String[] exportPaths, CmsObject cms, Node moduleNode)
        throws CmsException {
        this(exportFile, exportPaths, cms, false, false, moduleNode);
    }
/**
 * This constructs a new CmsImport-object which imports the resources.
 *
 * @param importFile the file or folder to import from.
 * @param exportPaths the paths of folders and files to write into the exportFile
 * @param cms the cms-object to work with.
 * @param excludeSystem if true, the system folder is excluded, if false exactly the resources in
 *        exportPaths are included
 * @param excludeUnchanged <code>true</code>, if unchanged files should be excluded.
 * @exception CmsException the CmsException is thrown if something goes wrong.
 */
public CmsExport(String exportFile, String[] exportPaths, CmsObject cms, boolean excludeSystem, boolean excludeUnchanged) throws CmsException {
    this(exportFile, exportPaths, cms, excludeSystem, excludeUnchanged, null);
}
    /**
     * This constructs a new CmsImport-object which imports the resources.
     *
     * @param importFile the file or folder to import from.
     * @param exportPaths the paths of folders and files to write into the exportFile
     * @param cms the cms-object to work with.
     * @param excludeSystem if true, the system folder is excluded, if false exactly the resources in
     *        exportPaths are included
     * @param excludeUnchanged <code>true</code>, if unchanged files should be excluded.
     * @param Node moduleNode module informations in a Node for module-export.
     * @exception CmsException the CmsException is thrown if something goes wrong.
     */
    public CmsExport(String exportFile, String[] exportPaths, CmsObject cms, boolean excludeSystem, boolean excludeUnchanged, Node moduleNode)
        throws CmsException {
        this(exportFile, exportPaths, cms, excludeSystem, excludeUnchanged, moduleNode, false);
    }
    /**
     * This constructs a new CmsImport-object which imports the resources.
     *
     * @param importFile the file or folder to import from.
     * @param exportPaths the paths of folders and files to write into the exportFile
     * @param cms the cms-object to work with.
     * @param excludeSystem if true, the system folder is excluded, if false exactly the resources in
     *        exportPaths are included
     * @param excludeUnchanged <code>true</code>, if unchanged files should be excluded.
     * @param Node moduleNode module informations in a Node for module-export.
     * @param exportUserdata if true, the userdata and groupdata are exported
     * @exception CmsException the CmsException is thrown if something goes wrong.
     */
    public CmsExport(String exportFile, String[] exportPaths, CmsObject cms, boolean excludeSystem
                            , boolean excludeUnchanged, Node moduleNode, boolean exportUserdata)
                        throws CmsException {
        this(exportFile, exportPaths, cms, excludeSystem, excludeUnchanged, moduleNode, exportUserdata, 0,
                new CmsShellReport());
    }

    /**
     * This constructs a new CmsImport-object which imports the resources.
     *
     * @param importFile the file or folder to import from.
     * @param exportPaths the paths of folders and files to write into the exportFile
     * @param cms the cms-object to work with.
     * @param excludeSystem if true, the system folder is excluded, if false exactly the resources in
     *        exportPaths are included
     * @param excludeUnchanged <code>true</code>, if unchanged files should be excluded.
     * @param Node moduleNode module informations in a Node for module-export.
     * @param exportUserdata if true, the userdata and groupdata are exported
     * @param report the cmsReport to handle the log messages.
     * @exception CmsException the CmsException is thrown if something goes wrong.
     */
    public CmsExport(String exportFile, String[] exportPaths, CmsObject cms, boolean excludeSystem, boolean excludeUnchanged, Node moduleNode, boolean exportUserdata, long contentAge, I_CmsReport report)
        throws CmsException {

        m_exportFile = exportFile;
        m_cms = cms;
        m_excludeSystem = excludeSystem;
        m_excludeUnchanged = excludeUnchanged;
        m_exportUserdata = exportUserdata;
        m_isOnlineProject = cms.getRequestContext().currentProject().equals(cms.onlineProject());
        m_contentAge = contentAge;
        m_report = report;

        Vector folderNames = new Vector();
        Vector fileNames = new Vector();
        for (int i=0; i<exportPaths.length; i++) {
            if (exportPaths[i].endsWith(C_ROOT)) {
                folderNames.addElement(exportPaths[i]);
            } else {
                fileNames.addElement(exportPaths[i]);
            }
        }

        // open the import resource
        getExportResource();

        // create the xml-config file
        getXmlConfigFile(moduleNode);

        // remove the possible redundancies in the list of paths
        checkRedundancies(folderNames, fileNames);
        
        // init sets required for the body file exports 
        m_exportedPageFiles = new HashSet();
        m_exportedResources = new HashSet();
        // set the "page" file resource type id
        m_pageType = cms.getResourceType("page").getResourceType();

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

        // export userdata and groupdata if desired
        if(m_exportUserdata){
            exportGroups();
            exportUsers();
        }
        // write the document to the zip-file
        writeXmlConfigFile();

        try {
            m_exportZipStream.close();
        } catch(IOException exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }
    /**
     * Adds a element to the xml-document.
     * @param element The element to add the subelement to.
     * @param name The name of the new subelement.
     * @param value The value of the element.
     */
    private void addCdataElement(Element element, String name, String value) {
        Element newElement = m_docXml.createElement(name);
        element.appendChild(newElement);
        CDATASection text = m_docXml.createCDATASection(value);
        newElement.appendChild(text);
    }
    /**
     * Adds a element to the xml-document.
     * @param element The element to add the subelement to.
     * @param name The name of the new subelement.
     * @param value The value of the element.
     */
    private void addElement(Element element, String name, String value) {
        Element newElement = m_docXml.createElement(name);
        element.appendChild(newElement);
        Text text = m_docXml.createTextNode(value);
        newElement.appendChild(text);
    }

    /**
     * Exports all page body files that have not explicityl been
     * added by the user.
     */
    public void addPageBodyFiles() throws CmsException {
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
 * adds all files with names in fileNames to the xml-config file
 * Creation date: (10.08.00 17:12:05)
 * @param fileNames java.util.Vector of Strings, e.g. /folder/index.html
 */
public void addSingleFiles(Vector fileNames) throws CmsException {
    if (fileNames != null) {
        for (int i = 0; i < fileNames.size(); i++) {
            String fileName = (String) fileNames.elementAt(i);
            try {
                CmsFile file = m_cms.readFile(fileName);
                if((file.getState() != C_STATE_DELETED) && (!file.getName().startsWith("~"))) {
                    addSuperFolders(fileName);
                    exportFile(file);
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
 * Adds the superfolders of path to the config file, starting at the top, excluding the root folder
 * Creation date: (10.08.00 11:07:58)
 * @param path java.lang.String the path of the folder in the filesystem
 */
public void addSuperFolders(String path) throws CmsException {
    // Initialize the "previously added folder cache"
    if(m_superFolders == null) {
        m_superFolders = new Vector();
    }
    Vector superFolders = new Vector();

    // Check, if the path is really a folder
    if(path.lastIndexOf(C_ROOT) != (path.length()-1)) {
        path = path.substring(0, path.lastIndexOf(C_ROOT)+1);
    }
    while (path.length() > C_ROOT.length()) {
        superFolders.addElement(path);
        path = path.substring(0, path.length() - 1);
        path = path.substring(0, path.lastIndexOf(C_ROOT)+1);
    }
    for (int i = superFolders.size()-1; i >= 0; i--) {
        String addFolder = (String)superFolders.elementAt(i);
        if(!m_superFolders.contains(addFolder)) {
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
     * or not.
     * 
     * @param resourcename the VFS resource name to check
     * @return <code>true</code> if the resource must not be exported again, <code>false</code> otherwise
     */
    private boolean isRedundant(String resourcename) {
        if (m_exportedResources == null) return false;
        Iterator i = m_exportedResources.iterator();
        while (i.hasNext()) {
            String s = (String)i.next();
            if (resourcename.startsWith(s)) return true;
        }
        return false;
    }

/** Check whether some of the resources are redundant because a superfolder has also
  *  been selected or a file is included in a folder and change the parameter Vectors
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
                if (((String) folderNames.elementAt(i)).length() < ((String) folderNames.elementAt(j)).length()) {
                    if (((String) folderNames.elementAt(j)).startsWith((String) folderNames.elementAt(i))) {
                        redundant.setElementAt(new Boolean(true), j);
                    }
                } else {
                    if (((String) folderNames.elementAt(i)).startsWith((String) folderNames.elementAt(j))) {
                        redundant.setElementAt(new Boolean(true), i);
                    }
                }
            }
        }
        for (i = n - 1; i >= 0; i--) {
            if (((Boolean) redundant.elementAt(i)).booleanValue()) {
                folderNames.removeElementAt(i);
            }
        }
    }
    // now remove the files who are included automatically in a folder
    // otherwise there would be a zip exception

    for (i = fileNames.size() - 1; i >= 0; i--) {
        for (j = 0; j < folderNames.size(); j++) {
            if (((String) fileNames.elementAt(i)).startsWith((String) folderNames.elementAt(j))) {
                fileNames.removeElementAt(i);
            }
        }
    }
}
    /**
     * Exports one single file with all its data and content.
     *
     * @param file the file to be exported,
     * @exception throws a CmsException if something goes wrong.
     */
    private void exportFile(CmsFile file)
        throws CmsException {
        String source = getSourceFilename(file.getAbsolutePath());

        m_report.addString("Exporting " + source + " ...");

        try {
            // create the manifest-entrys
            writeXmlEntrys((CmsResource) file);
            // store content in zip-file
            ZipEntry entry = new ZipEntry(source);
            m_exportZipStream.putNextEntry(entry);
            m_exportZipStream.write(file.getContents());
            m_exportZipStream.closeEntry();
        } catch(Exception exc) {
            m_report.addString("Error:"+exc.getMessage());
            m_report.addSeperator(0);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }

        if (file.getType() == m_pageType) {
            m_exportedPageFiles.add(file.getAbsolutePath());
        }

        m_report.addString("OK");
        m_report.addSeperator(0);
    }
    /**
     * Exports all needed sub-resources to the zip-file.
     *
     * @param path to complete path to the resource to export
     * @exception throws CmsException if something goes wrong.
     */
    private void exportResources(String path)
        throws CmsException {
        // get all subFolders
        Vector subFolders = m_cms.getSubFolders(path);
        // get all files in folder
        Vector subFiles = m_cms.getFilesInFolder(path);

        // walk through all files and export them
        for(int i = 0; i < subFiles.size(); i++) {
            CmsResource file = (CmsResource) subFiles.elementAt(i);
            int state = file.getState();
            long age = file.getDateLastModified();

            if(m_isOnlineProject || (!m_excludeUnchanged) || state == C_STATE_NEW || state == C_STATE_CHANGED) {
                if((state != C_STATE_DELETED) && (!file.getName().startsWith("~")) && (age >= m_contentAge)) {
                    exportFile(m_cms.readFile(file.getAbsolutePath()));
                }
            }            
            // release file header memory
            subFiles.set(i, null);
        }        
        // all files are exported, release memory
        subFiles = null;

        // walk through all subfolders and export them
        for(int i = 0; i < subFolders.size(); i++) {
            CmsResource folder = (CmsResource) subFolders.elementAt(i);
            if(folder.getState() != C_STATE_DELETED){
                // check if this is a system-folder and if it should be included.
                String export = folder.getAbsolutePath();
                if( // new VFS, always export "/system/bodies/" OR
                    (I_CmsWpConstants.C_VFS_NEW_STRUCTURE && export.startsWith("/system/bodies/")) ||
                    // option "exclude system folder" selected AND
                    !(m_excludeSystem && 
                        // export folder is a system folder 
                        (export.startsWith("/system/") ||            
                        // if new VFS, ignore old system folders (are below "/system" in new VFS)
                        ((! I_CmsWpConstants.C_VFS_NEW_STRUCTURE) && export.startsWith("/pics/system/")) ||
                        ((! I_CmsWpConstants.C_VFS_NEW_STRUCTURE) && export.startsWith("/moduledemos/")))                                           
                    )) {
                    // export this folder
                    if (folder.getDateLastModified() >= m_contentAge) {
                        // only export folder data to manifest.xml if it has changed
                        writeXmlEntrys(folder);
                    }
                    // export all resources in this folder
                    exportResources(folder.getAbsolutePath());
                }
            }            
            // release folder memory
            subFolders.set(i, null);
        }
    }
    /**
     * Gets the import resource and stores it in object-member.
     */
    private void getExportResource()
        throws CmsException {
        try {
            // add zip-extension, if needed
            if( !m_exportFile.toLowerCase().endsWith(".zip") ) {
                m_exportFile += ".zip";
            }

            // create the export-zipstream
            m_exportZipStream = new ZipOutputStream(new FileOutputStream(m_exportFile));

        } catch(Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }
    /**
     * Substrings the source-filename, so it is shrinked to the needed part for
     * import/export.
     * @param absoluteName The absolute path of the resource.
     * @return The shrinked path.
     */
    private String getSourceFilename(String absoluteName) {
        // String path = absoluteName.substring(m_exportPath.length());
        String path = absoluteName; // keep absolute name to distinguish resources
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if(path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }
    /**
     * Creates the xml-file and appends the initial tags to it.
     * @param Node moduleNode a node with module informations.
     */
    private void getXmlConfigFile(Node moduleNode)
        throws CmsException {

        try {
            // creates the document
            m_docXml = A_CmsXmlContent.getXmlParser().createEmptyDocument(C_EXPORT_TAG_EXPORT);
            // abbends the initital tags
            Node exportNode = m_docXml.getFirstChild();

            // add the info element. it contains all infos for this export
            Element info = m_docXml.createElement(C_EXPORT_TAG_INFO);
            m_docXml.getDocumentElement().appendChild(info);
            addElement(info, C_EXPORT_TAG_CREATOR, m_cms.getRequestContext().currentUser().getName());
            addElement(info, C_EXPORT_TAG_OC_VERSION, m_cms.version());
            addElement(info, C_EXPORT_TAG_DATE, Utils.getNiceDate(new Date().getTime()));
            addElement(info, C_EXPORT_TAG_PROJECT, m_cms.getRequestContext().currentProject().getName());
            addElement(info, C_EXPORT_TAG_VERSION, C_EXPORT_VERSION);

            if(moduleNode != null) {
                // this is a module export - import module informations here
                exportNode.appendChild(A_CmsXmlContent.getXmlParser().importNode(m_docXml, moduleNode));
                // now remove the unused file informations
                NodeList files = ((Element)exportNode).getElementsByTagName("files");
                if(files.getLength() == 1) {
                    files.item(0).getParentNode().removeChild(files.item(0));
                }
            }
            m_filesElement = m_docXml.createElement(C_EXPORT_TAG_FILES);
            m_docXml.getDocumentElement().appendChild(m_filesElement);
            // add userdata
            if (m_exportUserdata){
                m_userdataElement = m_docXml.createElement(C_EXPORT_TAG_USERGROUPDATA);
                m_docXml.getDocumentElement().appendChild(m_userdataElement);
            }
        } catch(Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }
    /**
     * Writes the xml-config file (manifest) to the zip-file.
     */
    private void writeXmlConfigFile()
        throws CmsException {
        try {
            ZipEntry entry = new ZipEntry(C_EXPORT_XMLFILENAME);
            m_exportZipStream.putNextEntry(entry);
            //A_CmsXmlContent.getXmlParser().getXmlText(m_docXml,m_exportZipStream, null);
            //Gridnine AB Sep 2, 2002
            A_CmsXmlContent.getXmlParser().getXmlText(m_docXml,m_exportZipStream, I_CmsXmlParser.C_XML_ENCODING);
            m_exportZipStream.closeEntry();
        } catch(Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }
    /**
     * Writes the data for a resources (like acces-rights) to the manifest-xml-file.
     * @param resource The resource to get the data from.
     * @exception throws a CmsException if something goes wrong.
     */
    private void writeXmlEntrys(CmsResource resource)
        throws CmsException {
        String source, type, user, group, access, launcherStartClass;

        // get all needed informations from the resource
        source = getSourceFilename(resource.getAbsolutePath());
        type = m_cms.getResourceType(resource.getType()).getResourceTypeName();
        user = m_cms.readOwner(resource).getName();
        group = m_cms.readGroup(resource).getName();
        access = resource.getAccessFlags() + "";
        launcherStartClass = resource.getLauncherClassname();

        // write these informations to the xml-manifest
        Element file = m_docXml.createElement(C_EXPORT_TAG_FILE);
        m_filesElement.appendChild(file);

        // only write source if resource is a file
        if(resource.isFile()) {
            addElement(file, C_EXPORT_TAG_SOURCE, source);
        }
        addElement(file, C_EXPORT_TAG_DESTINATION, source);
        addElement(file, C_EXPORT_TAG_TYPE, type);
        addElement(file, C_EXPORT_TAG_USER, user);
        addElement(file, C_EXPORT_TAG_GROUP, group);
        addElement(file, C_EXPORT_TAG_ACCESS, access);
        if(launcherStartClass != null && !"".equals(launcherStartClass) && !C_UNKNOWN_LAUNCHER.equals(launcherStartClass)) {
            addElement(file, C_EXPORT_TAG_LAUNCHER_START_CLASS, launcherStartClass);
        }

        // append the node for properties
        Element properties = m_docXml.createElement(C_EXPORT_TAG_PROPERTIES);
        file.appendChild(properties);

        // read the properties
        Hashtable fileProperties = m_cms.readAllProperties(resource.getAbsolutePath());
        Enumeration keys = fileProperties.keys();

        // create xml-elements for the properties
        while(keys.hasMoreElements()) {
            // append the node for a property
            Element property = m_docXml.createElement(C_EXPORT_TAG_PROPERTY);
            properties.appendChild(property);

            String key = (String) keys.nextElement();
            String value = (String) fileProperties.get(key);
            String propertyType = m_cms.readPropertydefinition(key, type).getType() + "";

            addElement(property, C_EXPORT_TAG_NAME, key);
            addElement(property, C_EXPORT_TAG_TYPE, propertyType);
            addCdataElement(property, C_EXPORT_TAG_VALUE, value);
        }

    }

    /**
     * Exports groups with all data.
     *
     * @exception throws a CmsException if something goes wrong.
     */
    private void exportGroups()
        throws CmsException {
        Vector allGroups = m_cms.getGroups();
        for (int i = 0; i < allGroups.size(); i++){
            exportGroup((CmsGroup)allGroups.elementAt(i));
        }
    }

    /**
     * Exports users with all data.
     *
     * @exception throws a CmsException if something goes wrong.
     */
    private void exportUsers()
        throws CmsException {
        Vector allUsers = m_cms.getUsers();
        for (int i = 0; i < allUsers.size(); i++){
            exportUser((CmsUser)allUsers.elementAt(i));
        }
    }

    /**
     * Exports one single group with all its data.
     *
     * @param group the group to be exported,
     * @exception throws a CmsException if something goes wrong.
     */
    private void exportGroup(CmsGroup group)
        throws CmsException {
        m_report.addString("Exporting group "+group.getName()+" ...");
        try {
            // create the manifest entries
            writeXmlGroupEntrys(group);
        } catch(Exception e) {
            m_report.addString("Error:"+e.getMessage());
            m_report.addSeperator(0);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, e);
        }
        m_report.addString("OK");
        m_report.addSeperator(0);
    }

    /**
     * Exports one single user with all its data.
     *
     * @param user the user to be exported,
     * @exception throws a CmsException if something goes wrong.
     */
    private void exportUser(CmsUser user)
        throws CmsException {
        m_report.addString("Exporting user "+user.getName()+" ...");
        try {
            // create the manifest entries
            writeXmlUserEntrys(user);
        } catch(Exception e) {
            m_report.addString("Error:"+e.getMessage());
            m_report.addSeperator(0);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, e);
        }
            m_report.addString("OK");
            m_report.addSeperator(0);
    }

    /**
     * Writes the data for a group to the manifest-xml-file.
     * @param group The group to get the data from.
     * @exception throws a CmsException if something goes wrong.
     */
    private void writeXmlGroupEntrys(CmsGroup group)
        throws CmsException {
        String name, description, flags, parentgroup;

        // get all needed information from the group
        name = group.getName();
        description = group.getDescription();
        flags = Integer.toString(group.getFlags());
        int parentId = group.getParentId();
        if (parentId != C_UNKNOWN_ID) {
            parentgroup = m_cms.getParent(name).getName();
        } else {
            parentgroup = "";
        }

        // write these informations to the xml-manifest
        Element groupdata = m_docXml.createElement(C_EXPORT_TAG_GROUPDATA);
        m_userdataElement.appendChild(groupdata);

        addElement(groupdata, C_EXPORT_TAG_NAME, name);
        addCdataElement(groupdata, C_EXPORT_TAG_DESCRIPTION, description);
        addElement(groupdata, C_EXPORT_TAG_FLAGS, flags);
        addElement(groupdata, C_EXPORT_TAG_PARENTGROUP, parentgroup);
    }

    /**
     * Writes the data for a user to the manifest-xml-file.
     * @param group The group to get the data from.
     * @exception throws a CmsException if something goes wrong.
     */
    private void writeXmlUserEntrys(CmsUser user)
        throws CmsException {
        String name, password, recoveryPassword, description, firstname;
        String lastname, email, flags, defaultGroup, address, section, type;
        String datfileName = new String();
        String infostr = new String();
        Hashtable info = new Hashtable();
        Vector userGroups = new Vector();
        sun.misc.BASE64Encoder enc;
        ObjectOutputStream oout;

        // get all needed information from the group
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

        addElement(userdata, C_EXPORT_TAG_NAME, name);
        //Encode the info value, using any base 64 decoder
        enc = new sun.misc.BASE64Encoder();
        String passwd = new String(enc.encodeBuffer(password.getBytes()));
        addCdataElement(userdata, C_EXPORT_TAG_PASSWORD, passwd);
        enc = new sun.misc.BASE64Encoder();
        String recPasswd = new String(enc.encodeBuffer(recoveryPassword.getBytes()));
        addCdataElement(userdata, C_EXPORT_TAG_RECOVERYPASSWORD, recPasswd);

        addCdataElement(userdata, C_EXPORT_TAG_DESCRIPTION, description);
        addElement(userdata, C_EXPORT_TAG_FIRSTNAME, firstname);
        addElement(userdata, C_EXPORT_TAG_LASTNAME, lastname);
        addElement(userdata, C_EXPORT_TAG_EMAIL, email);
        addElement(userdata, C_EXPORT_TAG_FLAGS, flags);
        addElement(userdata, C_EXPORT_TAG_DEFAULTGROUP, defaultGroup);
        addCdataElement(userdata, C_EXPORT_TAG_ADDRESS, address);
        addElement(userdata, C_EXPORT_TAG_SECTION, section);
        addElement(userdata, C_EXPORT_TAG_TYPE, type);
        // serialize the hashtable and write the info into a file
        try{
            datfileName = "/~"+C_EXPORT_TAG_USERINFO+"/"+name+".dat";
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
        } catch (IOException ioex){
            m_report.addString("IOException: "+ioex.getMessage());
            m_report.addSeperator(0);
        }
        // create tag for userinfo
        addCdataElement(userdata, C_EXPORT_TAG_USERINFO, datfileName);

        // append the node for groups of user
        Element usergroup = m_docXml.createElement(C_EXPORT_TAG_USERGROUPS);
        userdata.appendChild(usergroup);
        for (int i = 0; i < userGroups.size(); i++){
            String groupName = ((CmsGroup)userGroups.elementAt(i)).getName();
            Element group = m_docXml.createElement(C_EXPORT_TAG_GROUPNAME);
            usergroup.appendChild(group);
            addElement(group, C_EXPORT_TAG_NAME, groupName);
        }
    }
}

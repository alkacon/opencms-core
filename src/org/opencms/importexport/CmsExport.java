/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/CmsExport.java,v $
 * Date   : $Date: 2004/02/22 19:14:26 $
 * Version: $Revision: 1.30 $
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
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.util.CmsUUID;
import org.opencms.util.CmsXmlSaxWriter;
import org.opencms.workplace.I_CmsWpConstants;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXWriter;
import org.xml.sax.SAXException;

/**
 * Provides the functionality to export files from the OpenCms VFS to a ZIP file.<p>
 * 
 * The ZIP file written will contain a copy of all exported files with their contents.
 * It will also contain a <code>manifest.xml</code> file in wich all meta-information 
 * about this files are stored, like permissions etc.
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * 
 * @version $Revision: 1.30 $ $Date: 2004/02/22 19:14:26 $
 */
public class CmsExport implements Serializable {

    /** Manifest tag: channels */
    public static String C_EXPORT_TAG_CHANNELS = "channels";
    
    /** Manifest tag: files */
    public static String C_EXPORT_TAG_FILES = "files";

    /** The CmsObject to do the operations */
    private CmsObject m_cms;

    /** Max file age of contents to export */
    private long m_contentAge;

    /** Indicates if the system should be included to the export */
    private boolean m_excludeSystem;

    /** Indicated if the unchanged resources should be included to the export */
    private boolean m_excludeUnchanged;
    
    /** Counter for the export */
    private int m_exportCount;

    /** The channelid and the resourceobject of the exported channels */
    private Set m_exportedChannelIds;

    /** Set of all exported pages, required for later page body file export */
    private Set m_exportedPageFiles;

    /** Set of all exported files, required for later page body file export */
    private Set m_exportedResources;

    /** The export ZIP file to store resources in */
    private String m_exportFileName;

    /** Indicates if module data is exported */
    private boolean m_exportingCosData;

    /** Indicates if the user data and group data should be included to the export */
    private boolean m_exportUserdata;

    /** The export ZIP stream to write resources to */
    private ZipOutputStream m_exportZipStream;

    /** The top level file node where all resources are appended to */
    private Element m_fileNode;

    /** The report for the log messages */
    private I_CmsReport m_report;

    /** The SAX writer to write the output to */
    private SAXWriter m_saxWriter;

    /** Cache for previously added super folders */
    private Vector m_superFolders;

    /**
     * Constructs a new uninitialized export, required for special subclass data export.<p>
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
    public CmsExport(CmsObject cms, String exportFile, String[] resourcesToExport, boolean excludeSystem, boolean excludeUnchanged) throws CmsException {
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
     * @param moduleElement module informations in a Node for module export
     * @param exportUserdata if true, the user and grou pdata will also be exported
     * @param contentAge export contents changed after this date/time
     * @param report to handle the log messages
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsExport(CmsObject cms, String exportFile, String[] resourcesToExport, boolean excludeSystem, boolean excludeUnchanged, Element moduleElement, boolean exportUserdata, long contentAge, I_CmsReport report) throws CmsException {
        setCms(cms);
        setReport(report);
        setExportFileName(exportFile);
        setExportingCosData(false);

        m_excludeSystem = excludeSystem;
        m_excludeUnchanged = excludeUnchanged;
        m_exportUserdata = exportUserdata;
        m_contentAge = contentAge;
        m_exportCount = 0;

        // clear all caches
        report.println(report.key("report.clearcache"), I_CmsReport.C_FORMAT_NOTE);
        OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_CLEAR_CACHES, Collections.EMPTY_MAP, false));
        
        try {
            Element exportNode = openExportFile();

            if (moduleElement != null) {
                // add the module element
                exportNode.add(moduleElement);                       
                // write the XML
                digestElement(exportNode, moduleElement);
            }

            exportAllResources(exportNode, resourcesToExport);

            // export userdata and groupdata if selected
            if (m_exportUserdata) {
                Element userGroupData = exportNode.addElement(I_CmsConstants.C_EXPORT_TAG_USERGROUPDATA);
                getSaxWriter().writeOpen(userGroupData);

                exportGroups(userGroupData);
                exportUsers(userGroupData);

                getSaxWriter().writeClose(userGroupData);
                exportNode.remove(userGroupData);
            }

            closeExportFile(exportNode);
        } catch (SAXException se) {
            getReport().println(se);
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error exporting to file " + getExportFileName(), se);
            }
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, se);
        } catch (IOException ioe) {
            getReport().println(ioe);
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error exporting to file " + getExportFileName(), ioe);
            }
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, ioe);
        }
    }

    /** 
     * Checks whether some of the resources are redundant because a superfolder has also
     * been selected or a file is included in a folder.<p>
     * 
     * @param folderNames contains the full pathnames of all folders
     * @param fileNames contains the full pathnames of all files
     */
    public static void checkRedundancies(Vector folderNames, Vector fileNames) {
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
     * Exports the given folder and all child resources.<p>
     *
     * @param folderName to complete path to the resource to export
     * @throws CmsException if something goes wrong
     * @throws SAXException if something goes wrong procesing the manifest.xml
     */
    private void addChildResources(String folderName) throws CmsException, SAXException {

        if (isExportingCosData()) {
            // collect channel id information if required
            String channelId = getCms().readFolder(folderName).getResourceId().toString();
            if (channelId != null) {
                getExportedChannelIds().add(channelId);
            }
        }

        // get all subFolders
        List subFolders = getCms().getSubFolders(folderName);
        // get all files in folder
        List subFiles = getCms().getFilesInFolder(folderName);

        // walk through all files and export them
        for (int i = 0; i < subFiles.size(); i++) {
            CmsResource file = (CmsResource)subFiles.get(i);
            int state = file.getState();
            long age = file.getDateLastModified();

            if (getCms().getRequestContext().currentProject().isOnlineProject() 
                || (!m_excludeUnchanged) 
                || state == I_CmsConstants.C_STATE_NEW 
                || state == I_CmsConstants.C_STATE_CHANGED) {
                if ((state != I_CmsConstants.C_STATE_DELETED) && (!file.getName().startsWith("~")) && (age >= m_contentAge)) {
                    exportFile(getCms().readFile(getCms().readAbsolutePath(file)));
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
            if (folder.getState() != I_CmsConstants.C_STATE_DELETED) {
                // check if this is a system-folder and if it should be included.
                String export = getCms().readAbsolutePath(folder);
                if (// always export "/system/"
                export.equalsIgnoreCase(I_CmsWpConstants.C_VFS_PATH_SYSTEM) // OR always export "/system/bodies/"                                  
                    || export.startsWith(I_CmsWpConstants.C_VFS_PATH_BODIES) // OR always export "/system/galleries/"
                    || export.startsWith(I_CmsWpConstants.C_VFS_PATH_GALLERIES) // OR option "exclude system folder" selected
                    || !(m_excludeSystem // AND export folder is a system folder
                && export.startsWith(I_CmsWpConstants.C_VFS_PATH_SYSTEM))) {

                    // export this folder only if age is above selected age
                    // default for selected age (if not set by user) is <code>long 0</code> (i.e. 1970)
                    if (folder.getDateLastModified() >= m_contentAge) {
                        // only export folder data to manifest.xml if it has changed
                        exportResource(folder, false);
                    }

                    // export all sub-resources in this folder
                    addChildResources(getCms().readAbsolutePath(folder));
                }
            }
            // release folder memory
            subFolders.set(i, null);
        }
    }

    /**
     * Adds all files in fileNames to the manifest.xml file.<p>
     * 
     * @param fileNames Vector of path Strings, e.g. <code>/folder/index.html</code>
     * @throws CmsException if something goes wrong
     * @throws SAXException if something goes wrong procesing the manifest.xml
     */
    private void addFiles(Vector fileNames) throws CmsException, SAXException {
        if (fileNames != null) {
            for (int i = 0; i < fileNames.size(); i++) {
                String fileName = (String)fileNames.elementAt(i);
                try {
                    CmsFile file = getCms().readFile(fileName);
                    if ((file.getState() != I_CmsConstants.C_STATE_DELETED) && (!file.getName().startsWith("~"))) {
                        addParentFolders(fileName);
                        exportFile(file);
                    }
                } catch (CmsException exc) {
                    if (exc.getType() != CmsException.C_RESOURCE_DELETED) {
                        throw exc;
                    }
                }
            }
        }
    }

    /**
     * Exports all page body files that have not explicityl been added by the user.<p>
     * 
     * @throws CmsException if something goes wrong
     * @throws SAXException if something goes wrong procesing the manifest.xml
     */
    private void addPageBodyFiles() throws CmsException, SAXException {
        Iterator i;

        Vector bodyFileNames = new Vector();
        String bodyPath = I_CmsWpConstants.C_VFS_PATH_BODIES.substring(0, I_CmsWpConstants.C_VFS_PATH_BODIES.lastIndexOf("/"));

        // check all exported page files if their body has already been exported
        i = m_exportedPageFiles.iterator();
        while (i.hasNext()) {
            String filename = (String)i.next();
            // check if the site path is within the filename. If so,this export is
            // started from the root site and the path to the bodies must be modifed
            // this is not nice, but it works.
            if (filename.startsWith(I_CmsConstants.VFS_FOLDER_SITES)) {
                filename = filename.substring(I_CmsConstants.VFS_FOLDER_SITES.length() + 1, filename.length());
                filename = filename.substring(filename.indexOf("/"), filename.length());
            }
            String body = bodyPath + filename;
            bodyFileNames.add(body);
        }

        // now export the body files that have not already been exported
        addFiles(bodyFileNames);
    }

    /**
     * Adds the parent folders of the given resource to the config file, 
     * starting at the top, excluding the root folder.<p>
     * 
     * @param resourceName the name of a resource in the VFS
     * @throws CmsException if something goes wrong
     * @throws SAXException if something goes wrong procesing the manifest.xml
     */
    private void addParentFolders(String resourceName) throws CmsException, SAXException {
        // Initialize the "previously added folder cache"
        if (m_superFolders == null) {
            m_superFolders = new Vector();
        }
        Vector superFolders = new Vector();

        // Check, if the path is really a folder
        if (resourceName.lastIndexOf(I_CmsConstants.C_ROOT) != (resourceName.length() - 1)) {
            resourceName = resourceName.substring(0, resourceName.lastIndexOf(I_CmsConstants.C_ROOT) + 1);
        }
        while (resourceName.length() > I_CmsConstants.C_ROOT.length()) {
            superFolders.addElement(resourceName);
            resourceName = resourceName.substring(0, resourceName.length() - 1);
            resourceName = resourceName.substring(0, resourceName.lastIndexOf(I_CmsConstants.C_ROOT) + 1);
        }
        for (int i = superFolders.size() - 1; i >= 0; i--) {
            String addFolder = (String)superFolders.elementAt(i);
            if (!m_superFolders.contains(addFolder)) {
                // This super folder was NOT added previously. Add it now!
                CmsFolder folder = getCms().readFolder(addFolder);
                exportResource(folder, false);
                // Remember that this folder was added
                m_superFolders.addElement(addFolder);
            }
        }
    }

    /**
     * Closes the export ZIP file and saves the XML document for the manifest.<p>
     * 
     * @param exportNode the export root node
     * @throws SAXException if something goes wrong procesing the manifest.xml
     * @throws IOException if something goes wrong while closing the export file
     */
    protected void closeExportFile(Element exportNode) throws IOException, SAXException {
        // close the <export> Tag
        getSaxWriter().writeClose(exportNode);

        // close the XML document 
        CmsXmlSaxWriter xmlSaxWriter = (CmsXmlSaxWriter)getSaxWriter().getContentHandler();
        xmlSaxWriter.endDocument();

        // create zip entry for the manifest XML document
        ZipEntry entry = new ZipEntry(I_CmsConstants.C_EXPORT_XMLFILENAME);
        getExportZipStream().putNextEntry(entry);

        // write the XML to the zip stream
        getExportZipStream().write(xmlSaxWriter.getWriter().toString().getBytes(OpenCms.getSystemInfo().getDefaultEncoding()));

        // close the zip entry for the manifest XML document
        getExportZipStream().closeEntry();

        // finally close the zip stream
        getExportZipStream().close();
    }
    
    /**
     * Writes the output element to the XML output writer and detaches it 
     * from it's parent element.<p> 
     * 
     * @param parent the parent element
     * @param output the output element 
     * @throws SAXException if something goes wrong procesing the manifest.xml
     */
    protected void digestElement(Element parent, Element output) throws SAXException {
        m_saxWriter.write(output);
        parent.remove(output);
    }

    /**
     * Exports all resources and possible sub-folders form the provided list of resources.
     * 
     * @param parent the parent node to add the resources to
     * @param resourcesToExport the list of resources to export
     * @throws CmsException if something goes wrong
     * @throws SAXException if something goes wrong procesing the manifest.xml
     */
    protected void exportAllResources(Element parent, String[] resourcesToExport) throws CmsException, SAXException {
        // export all the resources
        String fileNodeName;
        if (isExportingCosData()) {
            // module (COS) data is exported
            fileNodeName = C_EXPORT_TAG_CHANNELS;
        } else {
            // standard export, only VFS resources are exported
            fileNodeName = C_EXPORT_TAG_FILES;
        }
        m_fileNode = parent.addElement(fileNodeName);
        getSaxWriter().writeOpen(m_fileNode);

        // distinguish folder and file names   
        Vector folderNames = new Vector();
        Vector fileNames = new Vector();
        for (int i = 0; i < resourcesToExport.length; i++) {
            if (resourcesToExport[i].endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
                folderNames.addElement(resourcesToExport[i]);
            } else {
                fileNames.addElement(resourcesToExport[i]);
            }
        }

        // remove the possible redundancies in the list of resources
        checkRedundancies(folderNames, fileNames);

        // init sets required for the body file exports 
        m_exportedResources = new HashSet();
        m_exportedPageFiles = new HashSet();

        // export the folders
        for (int i = 0; i < folderNames.size(); i++) {
            String path = (String)folderNames.elementAt(i);
            // first add superfolders to the xml-config file
            addParentFolders(path);
            addChildResources(path);
            m_exportedResources.add(path);
        }
        // export the files
        addFiles(fileNames);
        // export all body files that have not already been exported
        addPageBodyFiles();
        
        // write the XML
        getSaxWriter().writeClose(m_fileNode);
        parent.remove(m_fileNode);
        m_fileNode = null;
    }

    /**
     * Exports one single file with all its data and content.<p>
     *
     * @param file the file to be exported
     * @throws CmsException if something goes wrong
     */
    private void exportFile(CmsFile file) throws CmsException {
        String source = trimResourceName(getCms().readAbsolutePath(file));
        getReport().print(" ( " + ++m_exportCount + " ) ", I_CmsReport.C_FORMAT_NOTE);
        getReport().print(getReport().key("report.exporting"), I_CmsReport.C_FORMAT_NOTE);
        getReport().print(getCms().readAbsolutePath(file));
        getReport().print(getReport().key("report.dots"));
        try {
            // store content in zip-file
            // check if the content of this resource was not already exported
            if (!m_exportedResources.contains(file.getResourceId())) {
                ZipEntry entry = new ZipEntry(source);
                getExportZipStream().putNextEntry(entry);
                getExportZipStream().write(file.getContents());
                getExportZipStream().closeEntry();
                // add the resource id to the storage to mark that this resource was already exported
                m_exportedResources.add(file.getResourceId());
                // create the manifest-entrys
                exportResource(file, true);
            } else {
                // only create the manifest-entrys
                exportResource(file, false);
            }
            // check if the resource is a page of the old style. if so, export the body as well       
            if (getCms().getResourceType(file.getType()).getResourceTypeName().equals("page")) {
                m_exportedPageFiles.add("/" + source);
            }
        } catch (Exception exc) {
            getReport().println(exc);
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
        getReport().println(" " + getReport().key("report.ok"), I_CmsReport.C_FORMAT_OK);
    }

    /**
     * Exports one single group with all it's data.<p>
     *
     * @param parent the parent node to add the groups to
     * @param group the group to be exported
     * @throws CmsException if something goes wrong
     * @throws SAXException if something goes wrong procesing the manifest.xml
     */
    private void exportGroup(Element parent, CmsGroup group) throws CmsException, SAXException {
        String parentgroup;
        if (group.getParentId().isNullUUID()) {
            parentgroup = "";
        } else {
            parentgroup = getCms().getParent(group.getName()).getName();
        }

        Element e = parent.addElement(I_CmsConstants.C_EXPORT_TAG_GROUPDATA);
        e.addElement(I_CmsConstants.C_EXPORT_TAG_NAME).addText(group.getName());
        e.addElement(I_CmsConstants.C_EXPORT_TAG_DESCRIPTION).addCDATA(group.getDescription());
        e.addElement(I_CmsConstants.C_EXPORT_TAG_FLAGS).addText(Integer.toString(group.getFlags()));
        e.addElement(I_CmsConstants.C_EXPORT_TAG_PARENTGROUP).addText(parentgroup);

        // write the XML
        digestElement(parent, e);
    }

    /**
     * Exports all groups with all data.<p>
     *
     * @param parent the parent node to add the groups to
     * @throws CmsException if something goes wrong
     * @throws SAXException if something goes wrong procesing the manifest.xml
     */
    private void exportGroups(Element parent) throws CmsException, SAXException {
        Vector allGroups = getCms().getGroups();
        for (int i = 0, l = allGroups.size(); i < l; i++) {
            CmsGroup group = (CmsGroup)allGroups.elementAt(i);
            getReport().print(" ( " + (i+1) + " / " + l + " ) ", I_CmsReport.C_FORMAT_NOTE);
            getReport().print(getReport().key("report.exporting_group"), I_CmsReport.C_FORMAT_NOTE);
            getReport().print(group.getName());
            getReport().print(getReport().key("report.dots"));
            exportGroup(parent, group);
            getReport().println(getReport().key("report.ok"), I_CmsReport.C_FORMAT_OK);
        }
    }

    /**
     * Writes the data for a resource (like access-rights) to the <code>manifest.xml</code> file.<p>
     * 
     * @param resource the resource to get the data from
     * @param source flag to show if the source information in the xml file must be written
     * @throws CmsException if something goes wrong
     * @throws SAXException if something goes wrong procesing the manifest.xml
     */
    private void exportResource(CmsResource resource, boolean source) throws CmsException, SAXException {
        // define the file node
        Element e = m_fileNode.addElement(I_CmsConstants.C_EXPORT_TAG_FILE);

        // only write <source> if resource is a file
        String fileName = trimResourceName(getCms().readAbsolutePath(resource));
        if (resource.isFile()) {
            if (source) {
                e.addElement(I_CmsConstants.C_EXPORT_TAG_SOURCE).addText(fileName);
            }
        } else {
            // output something to the report for the folder
            getReport().print(" ( " + ++m_exportCount + " ) ", I_CmsReport.C_FORMAT_NOTE);
            getReport().print(getReport().key("report.exporting"), I_CmsReport.C_FORMAT_NOTE);
            getReport().print(getCms().readAbsolutePath(resource));
            getReport().print(getReport().key("report.dots"));
            getReport().println(getReport().key("report.ok"), I_CmsReport.C_FORMAT_OK);
        }

        // <destination>
        e.addElement(I_CmsConstants.C_EXPORT_TAG_DESTINATION).addText(fileName);
        // <type>
        e.addElement(I_CmsConstants.C_EXPORT_TAG_TYPE).addText(getCms().getResourceType(resource.getType()).getResourceTypeName());
        //  <uuidresource>
        e.addElement(I_CmsConstants.C_EXPORT_TAG_UUIDRESOURCE).addText(resource.getResourceId().toString());
        //  <uuidcontent>
        e.addElement(I_CmsConstants.C_EXPORT_TAG_UUIDCONTENT).addText(resource.getFileId().toString());
        // <datelastmodified>
        e.addElement(I_CmsConstants.C_EXPORT_TAG_DATELASTMODIFIED).addText(String.valueOf(resource.getDateLastModified()));
        // <userlastmodified>
        e.addElement(I_CmsConstants.C_EXPORT_TAG_USERLASTMODIFIED).addText(getCms().readUser(resource.getUserLastModified()).getName());
        // <datecreated>
        e.addElement(I_CmsConstants.C_EXPORT_TAG_DATECREATED).addText(String.valueOf(resource.getDateCreated()));
        // <usercreated>
        e.addElement(I_CmsConstants.C_EXPORT_TAG_USERCREATED).addText(getCms().readUser(resource.getUserCreated()).getName());
        // <flags>
        int resFlags = resource.getFlags();
        resFlags &= ~I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
        e.addElement(I_CmsConstants.C_EXPORT_TAG_FLAGS).addText(Integer.toString(resFlags));

        // append the node for properties
        Element p = e.addElement(I_CmsConstants.C_EXPORT_TAG_PROPERTIES);

        // read the properties
        Map fileProperties = getCms().readProperties(getCms().readAbsolutePath(resource));
        Iterator i = fileProperties.keySet().iterator();
        // create xml-elements for the properties
        while (i.hasNext()) {
            String key = (String)i.next();
            // make sure channel id property is not exported with module data
            if ((!isExportingCosData()) || (!I_CmsConstants.C_PROPERTY_CHANNELID.equals(key))) {
                // append the node for a property
                Element q = p.addElement(I_CmsConstants.C_EXPORT_TAG_PROPERTY);
                q.addElement(I_CmsConstants.C_EXPORT_TAG_NAME).addText(key);
                q.addElement(I_CmsConstants.C_EXPORT_TAG_VALUE).addCDATA((String)fileProperties.get(key));
            }
        }
        // append the nodes for access control entries
        Element acl = e.addElement(I_CmsConstants.C_EXPORT_TAG_ACCESSCONTROL_ENTRIES);

        // read the access control entries
        Vector fileAcEntries = getCms().getAccessControlEntries(getCms().readAbsolutePath(resource), false);
        i = fileAcEntries.iterator();

        // create xml elements for each access control entry
        while (i.hasNext()) {
            CmsAccessControlEntry ace = (CmsAccessControlEntry)i.next();
            Element a = acl.addElement(I_CmsConstants.C_EXPORT_TAG_ACCESSCONTROL_ENTRY);

            // now check if the principal is a group or a user
            int flags = ace.getFlags();
            String acePrincipalName = "";
            CmsUUID acePrincipal = ace.getPrincipal();
            if ((flags & I_CmsConstants.C_ACCESSFLAGS_GROUP) > 0) {
                // the principal is a group
                acePrincipalName = I_CmsConstants.C_EXPORT_ACEPRINCIPAL_GROUP 
                    + getCms().readGroup(acePrincipal).getName();
            } else {
                // the principal is a user
                acePrincipalName = I_CmsConstants.C_EXPORT_ACEPRINCIPAL_USER 
                    + getCms().readUser(acePrincipal).getName();
            }

            a.addElement(I_CmsConstants.C_EXPORT_TAG_ACCESSCONTROL_PRINCIPAL).addText(acePrincipalName);
            a.addElement(I_CmsConstants.C_EXPORT_TAG_FLAGS).addText(Integer.toString(flags));

            Element b = a.addElement(I_CmsConstants.C_EXPORT_TAG_ACCESSCONTROL_PERMISSIONSET);
            b.addElement(I_CmsConstants.C_EXPORT_TAG_ACCESSCONTROL_ALLOWEDPERMISSIONS).addText(Integer.toString(ace.getAllowedPermissions()));
            b.addElement(I_CmsConstants.C_EXPORT_TAG_ACCESSCONTROL_DENIEDPERMISSIONS).addText(Integer.toString(ace.getDeniedPermissions()));
        }
        
        // write the XML
        digestElement(m_fileNode, e);
    }

    /**
     * Exports one single user with all its data.<p>
     * 
     * @param parent the parent node to add the users to
     * @param user the user to be exported
     * @throws CmsException if something goes wrong
     * @throws SAXException if something goes wrong procesing the manifest.xml
     */
    private void exportUser(Element parent, CmsUser user) throws CmsException, SAXException {
        // add user node to the manifest.xml
        Element e = parent.addElement(I_CmsConstants.C_EXPORT_TAG_USERDATA);
        e.addElement(I_CmsConstants.C_EXPORT_TAG_NAME).addText(user.getName());
        // encode the password, using a base 64 decoder
        sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();
        String passwd = new String(enc.encodeBuffer(user.getPassword().getBytes()));
        e.addElement(I_CmsConstants.C_EXPORT_TAG_PASSWORD).addCDATA(passwd);
        e.addElement(I_CmsConstants.C_EXPORT_TAG_RECOVERYPASSWORD).addCDATA(user.getRecoveryPassword());
        e.addElement(I_CmsConstants.C_EXPORT_TAG_DESCRIPTION).addCDATA(user.getDescription());
        e.addElement(I_CmsConstants.C_EXPORT_TAG_FIRSTNAME).addText(user.getFirstname());
        e.addElement(I_CmsConstants.C_EXPORT_TAG_LASTNAME).addText(user.getLastname());
        e.addElement(I_CmsConstants.C_EXPORT_TAG_EMAIL).addText(user.getEmail());
        e.addElement(I_CmsConstants.C_EXPORT_TAG_FLAGS).addText(Integer.toString(user.getFlags()));
        e.addElement(I_CmsConstants.C_EXPORT_TAG_DEFAULTGROUP).addText(user.getDefaultGroup().getName());
        e.addElement(I_CmsConstants.C_EXPORT_TAG_ADDRESS).addCDATA(user.getAddress());
        e.addElement(I_CmsConstants.C_EXPORT_TAG_SECTION).addText(user.getSection());
        e.addElement(I_CmsConstants.C_EXPORT_TAG_TYPE).addText(Integer.toString(user.getType()));
        // serialize the hashtable and write the info into a file
        try {
            String datfileName = "/~" + I_CmsConstants.C_EXPORT_TAG_USERINFO + "/" + user.getName() + ".dat";
            // create tag for userinfo
            e.addElement(I_CmsConstants.C_EXPORT_TAG_USERINFO).addText(datfileName);            
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(user.getAdditionalInfo());
            oout.close();
            byte[] serializedInfo = bout.toByteArray();
            // store the serialized  user info hashtable in the zip-file
            ZipEntry entry = new ZipEntry(datfileName);
            getExportZipStream().putNextEntry(entry);
            getExportZipStream().write(serializedInfo);
            getExportZipStream().closeEntry();
        } catch (IOException ioe) {
            getReport().println(ioe);
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error writing user data to zip for user " + user.getName(), ioe);
            }
        }
        // append the node for groups of user
        Vector userGroups = getCms().getDirectGroupsOfUser(user.getName());        
        Element g = e.addElement(I_CmsConstants.C_EXPORT_TAG_USERGROUPS);
        for (int i = 0; i < userGroups.size(); i++) {
            String groupName = ((CmsGroup)userGroups.elementAt(i)).getName();
            g.addElement(I_CmsConstants.C_EXPORT_TAG_GROUPNAME).addElement(I_CmsConstants.C_EXPORT_TAG_NAME).addText(groupName);
        }
        // write the XML
        digestElement(parent, e);
    }

    /**
     * Exports all users with all data.<p>
     *
     * @param parent the parent node to add the users to
     * @throws CmsException if something goes wrong
     * @throws SAXException if something goes wrong procesing the manifest.xml
     */
    private void exportUsers(Element parent) throws CmsException, SAXException {
        Vector allUsers = getCms().getUsers();
        for (int i = 0, l = allUsers.size(); i < l; i++) {
            CmsUser user = (CmsUser)allUsers.elementAt(i);
            getReport().print(" ( " + (i+1) + " / " + l + " ) ", I_CmsReport.C_FORMAT_NOTE);
            getReport().print(getReport().key("report.exporting_user"), I_CmsReport.C_FORMAT_NOTE);
            getReport().print(user.getName());
            getReport().print(getReport().key("report.dots"));
            exportUser(parent, user);
            getReport().println(getReport().key("report.ok"), I_CmsReport.C_FORMAT_OK);
        }
    }

    /**
     * Returns the OpenCms context object this export was initialized with.<p>
     * 
     * @return the OpenCms context object this export was initialized with
     */
    protected CmsObject getCms() {
        return m_cms;
    }

    /**
     * Returns the set of already exported channel Ids.<p>
     * 
     * @return the set of already exported channel Ids
     */
    protected Set getExportedChannelIds() {
        return m_exportedChannelIds;
    }

    /**
     * Returns the name of the export file.<p>
     * 
     * @return the name of the export file
     */
    protected String getExportFileName() {
        return m_exportFileName;
    }

    /**
     * Returns the zip output stream to write to.<p>
     * 
     * @return the zip output stream to write to
     */
    protected ZipOutputStream getExportZipStream() {
        return m_exportZipStream;
    }

    /**
     * Returns the report to write progess messages to.<p>
     * 
     * @return the report to write progess messages to
     */
    protected I_CmsReport getReport() {
        return m_report;
    }

    /**
     * Returns the SAX baesed xml writer to write the XML output to.<p>
     * 
     * @return the SAX baesed xml writer to write the XML output to
     */
    protected SAXWriter getSaxWriter() {
        return m_saxWriter;
    }

    /**
     * Returns true if this is a COS data export, false if this is a VFS or module export.<p>
     * 
     * @return true if this is a module data export, false if this is a VFS or module export
     */
    protected boolean isExportingCosData() {
        return m_exportingCosData;
    }

    /**
     * Opens the export ZIP file and initializes the internal XML document for the manifest.<p>
     * 
     * @return the node in the XML document where all files are appended to
     * @throws SAXException if something goes wrong procesing the manifest.xml
     * @throws IOException if something goes wrong while closing the export file
     */
    protected Element openExportFile() throws IOException, SAXException {
        // create the export-zipstream
        setExportZipStream(new ZipOutputStream(new FileOutputStream(getExportFileName())));
        // generate the SAX XML writer 
        CmsXmlSaxWriter saxHandler = new CmsXmlSaxWriter(new StringWriter(4096), OpenCms.getSystemInfo().getDefaultEncoding());
        // initialize the dom4j writer object as member variable
        setSaxWriter(new SAXWriter(saxHandler, saxHandler));
        // the XML document to write the XMl to
        Document doc = DocumentHelper.createDocument();
        // start the document
        saxHandler.startDocument();

        // the node in the XML document where the file entries are appended to        
        String exportNodeName;
        if (isExportingCosData()) {
            // module (COS) data is exported
            exportNodeName = I_CmsConstants.C_EXPORT_TAG_MODULEXPORT;
        } else {
            // standard export, only VFS resources are exported
            exportNodeName = I_CmsConstants.C_EXPORT_TAG_EXPORT;
        }
        // add main export node to XML document
        Element exportNode = doc.addElement(exportNodeName);
        getSaxWriter().writeOpen(exportNode);

        // add the info element. it contains all infos for this export
        Element info = exportNode.addElement(I_CmsConstants.C_EXPORT_TAG_INFO);
        info.addElement(I_CmsConstants.C_EXPORT_TAG_CREATOR).addText(getCms().getRequestContext().currentUser().getName());
        info.addElement(I_CmsConstants.C_EXPORT_TAG_OC_VERSION).addText(OpenCms.getSystemInfo().getVersionName());
        info.addElement(I_CmsConstants.C_EXPORT_TAG_DATE).addText(CmsMessages.getDateTimeShort(System.currentTimeMillis()));
        info.addElement(I_CmsConstants.C_EXPORT_TAG_PROJECT).addText(getCms().getRequestContext().currentProject().getName());
        info.addElement(I_CmsConstants.C_EXPORT_TAG_VERSION).addText(I_CmsConstants.C_EXPORT_VERSION);
        
        // write the XML
        digestElement(exportNode, info);

        return exportNode;
    }

    /**
     * Sets the OpenCms context object this export was initialized with.<p>
     * 
     * @param cms the OpenCms context object this export was initialized with
     */
    protected void setCms(CmsObject cms) {
        m_cms = cms;
    }

    /**
     * Sets the set of already exported channel Ids.<p>
     * 
     * @param exportedChannelIds the set of already exported channel Ids
     */
    protected void setExportedChannelIds(Set exportedChannelIds) {
        m_exportedChannelIds = exportedChannelIds;
    }

    /**
     * Sets the name of the export file.<p>
     * 
     * @param exportFileName the name of the export file
     */
    protected void setExportFileName(String exportFileName) {
        // ensure the export file name ends with ".zip"
        if (!exportFileName.toLowerCase().endsWith(".zip")) {
            m_exportFileName = exportFileName + ".zip";
        } else {                
            m_exportFileName = exportFileName;
        }
    }

    /**
     * Sets the flag for indicating if this is a COS data export.<p>
     * 
     * @param exportingModuleData the flag for indicating if this is a COS data export
     */
    protected void setExportingCosData(boolean exportingModuleData) {
        m_exportingCosData = exportingModuleData;
    }

    /**
     * Sets the zip output stream to write to.<p>
     * 
     * @param exportZipStream the zip output stream to write to
     */
    protected void setExportZipStream(ZipOutputStream exportZipStream) {
        m_exportZipStream = exportZipStream;
    }

    /**
     * Sets the report to write progess messages to.<p>
     * 
     * @param report the report to write progess messages to
     */
    protected void setReport(I_CmsReport report) {
        m_report = report;
    }

    /**
     * Sets the SAX baesed xml writer to write the XML output to.<p>
     * 
     * @param saxWriter the SAX baesed xml writer to write the XML output to
     */
    protected void setSaxWriter(SAXWriter saxWriter) {
        m_saxWriter = saxWriter;
    }

    /**
     * Cuts leading and trailing '/' from the given resource name.<p>
     * 
     * @param resourceName the absolute path of a resource
     * @return the trimmed resource name
     */
    private String trimResourceName(String resourceName) {
        if (resourceName.startsWith("/")) {
            resourceName = resourceName.substring(1);
        }
        if (resourceName.endsWith("/")) {
            resourceName = resourceName.substring(0, resourceName.length() - 1);
        }
        return resourceName;
    }
}

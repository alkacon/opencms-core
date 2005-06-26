/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/CmsExport.java,v $
 * Date   : $Date: 2005/06/26 12:23:30 $
 * Version: $Revision: 1.75 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsException;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.util.CmsXmlSaxWriter;
import org.opencms.workplace.I_CmsWpConstants;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;

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
 * about this files are stored, like permissions etc.<p>
 *
 * @author Alexander Kandzior 
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.75 $ 
 * 
 * @since 6.0.0 
 */
public class CmsExport implements Serializable {

    private static final int C_SUB_LENGTH = 4096;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExport.class);

    /** The CmsObject to do the operations. */
    private CmsObject m_cms;

    /** Max file age of contents to export. */
    private long m_contentAge;

    /** Counter for the export. */
    private int m_exportCount;

    /** Set of all exported pages, required for later page body file export. */
    private Set m_exportedPageFiles;

    /** Set of all exported files, required for later page body file export. */
    private Set m_exportedResources;

    /** The export ZIP file to store resources in. */
    private String m_exportFileName;

    /** Indicates if the user data and group data should be included to the export. */
    private boolean m_exportUserdata;

    /** The export ZIP stream to write resources to. */
    private ZipOutputStream m_exportZipStream;

    /** Indicates if the system should be included to the export. */
    private boolean m_includeSystem;

    /** Indicates if the unchanged resources should be included to the export .*/
    private boolean m_includeUnchanged;

    /** The report for the log messages. */
    private I_CmsReport m_report;

    /** The top level file node where all resources are appended to. */
    private Element m_resourceNode;

    /** The SAX writer to write the output to. */
    private SAXWriter m_saxWriter;

    /** Cache for previously added super folders. */
    private List m_superFolders;

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
     * @param includeSystem if true, the system folder is included
     * @param includeUnchanged <code>true</code>, if unchanged files should be included
     * @throws CmsImportExportException if something goes wrong
     * @throws CmsRoleViolationException if the current user has not the required role
     */
    public CmsExport(
        CmsObject cms,
        String exportFile,
        String[] resourcesToExport,
        boolean includeSystem,
        boolean includeUnchanged)
    throws CmsImportExportException, CmsRoleViolationException {

        this(cms, exportFile, resourcesToExport, includeSystem, includeUnchanged, null, false, 0, new CmsShellReport());
    }

    /**
     * Constructs a new export.<p>
     *
     * @param cms the cmsObject to work with
     * @param exportFile the file or folder to export to
     * @param resourcesToExport the paths of folders and files to export
     * @param includeSystem if true, the system folder is included
     * @param includeUnchanged <code>true</code>, if unchanged files should be included
     * @param moduleElement module informations in a Node for module export
     * @param exportUserdata if true, the user and grou pdata will also be exported
     * @param contentAge export contents changed after this date/time
     * @param report to handle the log messages
     * 
     * @throws CmsImportExportException if something goes wrong
     * @throws CmsRoleViolationException if the current user has not the required role
     */
    public CmsExport(
        CmsObject cms,
        String exportFile,
        String[] resourcesToExport,
        boolean includeSystem,
        boolean includeUnchanged,
        Element moduleElement,
        boolean exportUserdata,
        long contentAge,
        I_CmsReport report)
    throws CmsImportExportException, CmsRoleViolationException {

        setCms(cms);
        setReport(report);
        setExportFileName(exportFile);

        // check if the user has the required permissions
        cms.checkRole(CmsRole.EXPORT_DATABASE);

        m_includeSystem = includeSystem;
        m_includeUnchanged = includeUnchanged;
        m_exportUserdata = exportUserdata;
        m_contentAge = contentAge;
        m_exportCount = 0;

        // clear all caches
        report.println(Messages.get().container(Messages.RPT_CLEARCACHE_0), I_CmsReport.C_FORMAT_NOTE);
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, Collections.EMPTY_MAP));

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
                Element userGroupData = exportNode.addElement(CmsImportExportManager.N_USERGROUPDATA);
                getSaxWriter().writeOpen(userGroupData);

                exportGroups(userGroupData);
                exportUsers(userGroupData);

                getSaxWriter().writeClose(userGroupData);
                exportNode.remove(userGroupData);
            }

            closeExportFile(exportNode);
        } catch (SAXException se) {
            getReport().println(se);

            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_EXPORTING_TO_FILE_1,
                getExportFileName());
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), se);
            }

            throw new CmsImportExportException(message, se);
        } catch (IOException ioe) {
            getReport().println(ioe);

            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_EXPORTING_TO_FILE_1,
                getExportFileName());
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), ioe);
            }

            throw new CmsImportExportException(message, ioe);
        }
    }

    /** 
     * Checks whether some of the resources are redundant because a superfolder has also
     * been selected or a file is included in a folder.<p>
     * 
     * @param folderNames contains the full pathnames of all folders
     * @param fileNames contains the full pathnames of all files
     */
    public static void checkRedundancies(List folderNames, List fileNames) {

        if (folderNames == null) {
            return;
        }
        if (!folderNames.isEmpty()) {
            Collections.sort(folderNames);
            List result = new ArrayList();
            Iterator i = folderNames.iterator();
            while (i.hasNext()) {
                // check all folders in the list
                String folder = (String)i.next();
                if (CmsStringUtil.isEmpty(folder)) {
                    // skip empty strings
                    continue;
                }
                boolean valid = true;
                for (int j = (result.size() - 1); j >= 0; j--) {
                    // check if this folder is indirectly contained because a parent folder is contained
                    String check = (String)result.get(j);
                    if (folder.startsWith(check)) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    // the folder is not already contained in the result
                    result.add(folder);
                }
            }
            folderNames.clear();
            folderNames.addAll(result);
        }
        if (!fileNames.isEmpty()) {
            Collections.sort(fileNames);
            List result = new ArrayList();
            Iterator i = fileNames.iterator();
            while (i.hasNext()) {
                // check all folders in the list
                String file = (String)i.next();
                if (CmsStringUtil.isEmpty(file)) {
                    // skip empty strings
                    continue;
                }
                boolean valid = true;
                for (int j = (folderNames.size() - 1); j >= 0; j--) {
                    // check if this folder is indirectly contained because a parent folder is contained
                    String check = (String)folderNames.get(j);
                    if (file.startsWith(check)) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    // the folder is not already contained in the result
                    result.add(file);
                }
            }
            fileNames.clear();
            fileNames.addAll(result);
        }
    }

    /**
     * Exports the given folder and all child resources.<p>
     *
     * @param folderName to complete path to the resource to export
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong procesing the manifest.xml
     * @throws IOException if not all resources could be appended to the ZIP archive
     */
    protected void addChildResources(String folderName) throws CmsImportExportException, IOException, SAXException {

        try {
            // get all subFolders
            List subFolders = getCms().getSubFolders(folderName, CmsResourceFilter.IGNORE_EXPIRATION);
            // get all files in folder
            List subFiles = getCms().getFilesInFolder(folderName, CmsResourceFilter.IGNORE_EXPIRATION);

            // walk through all files and export them
            for (int i = 0; i < subFiles.size(); i++) {
                CmsResource file = (CmsResource)subFiles.get(i);
                int state = file.getState();
                long age = file.getDateLastModified();

                if (getCms().getRequestContext().currentProject().isOnlineProject()
                    || (m_includeUnchanged)
                    || state == I_CmsConstants.C_STATE_NEW
                    || state == I_CmsConstants.C_STATE_CHANGED) {
                    if ((state != I_CmsConstants.C_STATE_DELETED)
                        && (!file.getName().startsWith("~"))
                        && (age >= m_contentAge)) {
                        String export = getCms().getSitePath(file);
                        if (checkExportResource(export)) {
                            exportFile(getCms().readFile(export, CmsResourceFilter.IGNORE_EXPIRATION));
                        }
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
                    String export = getCms().getSitePath(folder);
                    if (checkExportResource(export)) {

                        // export this folder only if age is above selected age
                        // default for selected age (if not set by user) is <code>long 0</code> (i.e. 1970)
                        if (folder.getDateLastModified() >= m_contentAge) {
                            // only export folder data to manifest.xml if it has changed
                            appendResourceToManifest(folder, false);
                        }

                        // export all sub-resources in this folder
                        addChildResources(getCms().getSitePath(folder));
                    }
                }
                // release folder memory
                subFolders.set(i, null);
            }
        } catch (CmsImportExportException e) {

            throw e;
        } catch (CmsException e) {

            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_ADDING_CHILD_RESOURCES_1,
                folderName);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }

            throw new CmsImportExportException(message, e);
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
        ZipEntry entry = new ZipEntry(CmsImportExportManager.EXPORT_XMLFILENAME);
        getExportZipStream().putNextEntry(entry);

        // complex substring operation is required to ensure handling for very large export manifest files
        StringBuffer result = ((StringWriter)xmlSaxWriter.getWriter()).getBuffer();
        int steps = result.length() / C_SUB_LENGTH;
        int rest = result.length() % C_SUB_LENGTH;
        int pos = 0;
        for (int i = 0; i < steps; i++) {
            String sub = result.substring(pos, pos + C_SUB_LENGTH);
            getExportZipStream().write(sub.getBytes(OpenCms.getSystemInfo().getDefaultEncoding()));
            pos += C_SUB_LENGTH;
        }
        if (rest > 0) {
            String sub = result.substring(pos, pos + rest);
            getExportZipStream().write(sub.getBytes(OpenCms.getSystemInfo().getDefaultEncoding()));
        }

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
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong procesing the manifest.xml
     * @throws IOException if not all resources could be appended to the ZIP archive
     */
    protected void exportAllResources(Element parent, String[] resourcesToExport)
    throws CmsImportExportException, IOException, SAXException {

        // export all the resources
        String resourceNodeName = getResourceNodeName();
        m_resourceNode = parent.addElement(resourceNodeName);
        getSaxWriter().writeOpen(m_resourceNode);

        // distinguish folder and file names   
        List folderNames = new ArrayList();
        List fileNames = new ArrayList();
        for (int i = 0; i < resourcesToExport.length; i++) {
            if (CmsResource.isFolder(resourcesToExport[i])) {
                folderNames.add(resourcesToExport[i]);
            } else {
                fileNames.add(resourcesToExport[i]);
            }
        }

        // remove the possible redundancies in the list of resources
        checkRedundancies(folderNames, fileNames);

        // init sets required for the body file exports 
        m_exportedResources = new HashSet();
        m_exportedPageFiles = new HashSet();

        // export the folders
        for (int i = 0; i < folderNames.size(); i++) {
            String path = (String)folderNames.get(i);
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
        getSaxWriter().writeClose(m_resourceNode);
        parent.remove(m_resourceNode);
        m_resourceNode = null;
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
     * Returns the name of the export file.<p>
     * 
     * @return the name of the export file
     */
    protected String getExportFileName() {

        return m_exportFileName;
    }

    /**
     * Returns the name of the main export node.<p>
     * 
     * @return the name of the main export node
     */
    protected String getExportNodeName() {

        return CmsImportExportManager.N_EXPORT;
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
     * Returns the name for the main resource node.<p>
     * 
     * @return the name for the main resource node
     */
    protected String getResourceNodeName() {

        return "files";
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
     * Checks if a property should be written to the export or not.<p>
     * 
     * @param property the property to check
     * @return if true, the property is to be ignored, otherwise it should be exported
     */
    protected boolean isIgnoredProperty(CmsProperty property) {

        if (property == null) {
            return true;
        }
        // default implementation is to export all properties not null
        return false;
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
        CmsXmlSaxWriter saxHandler = new CmsXmlSaxWriter(
            new StringWriter(4096),
            OpenCms.getSystemInfo().getDefaultEncoding());
        // initialize the dom4j writer object as member variable
        setSaxWriter(new SAXWriter(saxHandler, saxHandler));
        // the XML document to write the XMl to
        Document doc = DocumentHelper.createDocument();
        // start the document
        saxHandler.startDocument();

        // the node in the XML document where the file entries are appended to        
        String exportNodeName = getExportNodeName();
        // add main export node to XML document
        Element exportNode = doc.addElement(exportNodeName);
        getSaxWriter().writeOpen(exportNode);

        // add the info element. it contains all infos for this export
        Element info = exportNode.addElement(CmsImportExportManager.N_INFO);
        info.addElement(CmsImportExportManager.N_CREATOR).addText(getCms().getRequestContext().currentUser().getName());
        info.addElement(CmsImportExportManager.N_OC_VERSION).addText(OpenCms.getSystemInfo().getVersionName());
        info.addElement(CmsImportExportManager.N_DATE).addText(CmsDateUtil.getDateTimeShort(System.currentTimeMillis()));
        info.addElement(CmsImportExportManager.N_PROJECT).addText(
            getCms().getRequestContext().currentProject().getName());
        info.addElement(CmsImportExportManager.N_VERSION).addText(CmsImportExportManager.EXPORT_VERSION);

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
     * Adds all files in fileNames to the manifest.xml file.<p>
     * 
     * @param fileNames list of path Strings, e.g. <code>/folder/index.html</code>
     * @throws CmsImportExportException2 if something goes wrong
     * @throws SAXException if something goes wrong procesing the manifest.xml
     */
    private void addFiles(List fileNames) throws CmsImportExportException, IOException, SAXException {

        if (fileNames != null) {
            for (int i = 0; i < fileNames.size(); i++) {
                String fileName = (String)fileNames.get(i);

                try {
                    CmsFile file = getCms().readFile(fileName, CmsResourceFilter.IGNORE_EXPIRATION);
                    if ((file.getState() != I_CmsConstants.C_STATE_DELETED) && (!file.getName().startsWith("~"))) {
                        if (checkExportResource(fileName)) {
                            addParentFolders(fileName);
                            exportFile(file);
                        }
                    }
                } catch (CmsImportExportException e) {

                    throw e;
                } catch (CmsException e) {
                    if (e instanceof CmsVfsException) { // file not found
                        CmsMessageContainer message = Messages.get().container(
                            Messages.ERR_IMPORTEXPORT_ERROR_ADDING_FILE_1,
                            fileName);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(message.key(), e);
                        }

                        throw new CmsImportExportException(message, e);
                    }
                }
            }
        }
    }

    /**
     * Exports all page body files that have not explicityl been added by the user.<p>
     * 
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong procesing the manifest.xml
     */
    private void addPageBodyFiles() throws CmsImportExportException, IOException, SAXException {

        Iterator i;

        List bodyFileNames = new ArrayList();
        String bodyPath = I_CmsWpConstants.C_VFS_PATH_BODIES.substring(
            0,
            I_CmsWpConstants.C_VFS_PATH_BODIES.lastIndexOf("/"));

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
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong procesing the manifest.xml
     */
    private void addParentFolders(String resourceName) throws CmsImportExportException, SAXException {

        try {
            // this is a resource in /system/ folder and option includeSystem is not true
            if (!checkExportResource(resourceName)) {
                return;
            }

            // Initialize the "previously added folder cache"
            if (m_superFolders == null) {
                m_superFolders = new ArrayList();
            }
            ArrayList superFolders = new ArrayList();

            // Check, if the path is really a folder
            if (resourceName.lastIndexOf(I_CmsConstants.C_ROOT) != (resourceName.length() - 1)) {
                resourceName = resourceName.substring(0, resourceName.lastIndexOf(I_CmsConstants.C_ROOT) + 1);
            }
            while (resourceName.length() > I_CmsConstants.C_ROOT.length()) {
                superFolders.add(resourceName);
                resourceName = resourceName.substring(0, resourceName.length() - 1);
                resourceName = resourceName.substring(0, resourceName.lastIndexOf(I_CmsConstants.C_ROOT) + 1);
            }
            for (int i = superFolders.size() - 1; i >= 0; i--) {
                String addFolder = (String)superFolders.get(i);
                if (!m_superFolders.contains(addFolder)) {
                    // This super folder was NOT added previously. Add it now!
                    CmsFolder folder = getCms().readFolder(addFolder, CmsResourceFilter.IGNORE_EXPIRATION);
                    appendResourceToManifest(folder, false);
                    // Remember that this folder was added
                    m_superFolders.add(addFolder);
                }
            }
        } catch (CmsImportExportException e) {

            throw e;
        } catch (CmsException e) {

            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_ADDING_PARENT_FOLDERS_1,
                resourceName);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }

            throw new CmsImportExportException(message, e);
        }
    }

    /**
     * Writes the data for a resource (like access-rights) to the <code>manifest.xml</code> file.<p>
     * 
     * @param resource the resource to get the data from
     * @param source flag to show if the source information in the xml file must be written
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong procesing the manifest.xml
     */
    private void appendResourceToManifest(CmsResource resource, boolean source)
    throws CmsImportExportException, SAXException {

        try {
            CmsProperty property = null;
            String key = null, value = null;
            Element propertyElement = null;

            // define the file node
            Element fileElement = m_resourceNode.addElement(CmsImportExportManager.N_FILE);

            // only write <source> if resource is a file
            String fileName = trimResourceName(getCms().getSitePath(resource));
            if (resource.isFile()) {
                if (source) {
                    fileElement.addElement(CmsImportExportManager.N_SOURCE).addText(fileName);
                }
            } else {
                m_exportCount++;
                I_CmsReport report = getReport();
                // output something to the report for the folder
                report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_1,
                    String.valueOf(m_exportCount)), I_CmsReport.C_FORMAT_NOTE);
                report.print(Messages.get().container(Messages.RPT_EXPORT_0), I_CmsReport.C_FORMAT_NOTE);
                report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    getCms().getSitePath(resource)));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.C_FORMAT_OK);

                if (LOG.isInfoEnabled()) {
                    LOG.info(Messages.get().key(
                        Messages.LOG_EXPORTING_OK_2,
                        String.valueOf(m_exportCount),
                        getCms().getSitePath(resource)));
                }
            }

            // <destination>
            fileElement.addElement(CmsImportExportManager.N_DESTINATION).addText(fileName);
            // <type>
            fileElement.addElement(CmsImportExportManager.N_TYPE).addText(
                OpenCms.getResourceManager().getResourceType(resource.getTypeId()).getTypeName());

            if (resource.isFile()) {
                //  <uuidresource>
                fileElement.addElement(CmsImportExportManager.N_UUIDRESOURCE).addText(
                    resource.getResourceId().toString());
            }

            // <datelastmodified>
            fileElement.addElement(CmsImportExportManager.N_DATELASTMODIFIED).addText(
                CmsDateUtil.getHeaderDate(resource.getDateLastModified()));
            // <userlastmodified>
            String userNameLastModified = null;
            try {
                userNameLastModified = getCms().readUser(resource.getUserLastModified()).getName();
            } catch (CmsException e) {
                userNameLastModified = OpenCms.getDefaultUsers().getUserAdmin();
            }
            fileElement.addElement(CmsImportExportManager.N_USERLASTMODIFIED).addText(userNameLastModified);
            // <datecreated>
            fileElement.addElement(CmsImportExportManager.N_DATECREATED).addText(
                CmsDateUtil.getHeaderDate(resource.getDateCreated()));
            // <usercreated>
            String userNameCreated = null;
            try {
                userNameCreated = getCms().readUser(resource.getUserCreated()).getName();
            } catch (CmsException e) {
                userNameCreated = OpenCms.getDefaultUsers().getUserAdmin();
            }
            fileElement.addElement(CmsImportExportManager.N_USERCREATED).addText(userNameCreated);
            // <release>
            if (resource.getDateReleased() != CmsResource.DATE_RELEASED_DEFAULT) {
                fileElement.addElement(CmsImportExportManager.N_DATERELEASED).addText(
                    CmsDateUtil.getHeaderDate(resource.getDateReleased()));
            }
            // <expire>
            if (resource.getDateExpired() != CmsResource.DATE_EXPIRED_DEFAULT) {
                fileElement.addElement(CmsImportExportManager.N_DATEEXPIRED).addText(
                    CmsDateUtil.getHeaderDate(resource.getDateExpired()));
            }
            // <flags>
            int resFlags = resource.getFlags();
            resFlags &= ~I_CmsConstants.C_RESOURCEFLAG_LABELLINK;
            fileElement.addElement(CmsImportExportManager.N_FLAGS).addText(Integer.toString(resFlags));

            // write the properties to the manifest
            Element propertiesElement = fileElement.addElement(CmsImportExportManager.N_PROPERTIES);
            List properties = getCms().readPropertyObjects(getCms().getSitePath(resource), false);
            for (int i = 0, n = properties.size(); i < n; i++) {
                property = (CmsProperty)properties.get(i);

                if (isIgnoredProperty(property)) {
                    continue;
                }

                key = property.getName();

                for (int j = 0; j < 2; j++) {
                    // iterations made here:
                    // 0) append individual/structure property value
                    // 1) append shared/resource property value
                    if ((j == 0 && (value = property.getStructureValue()) != null)
                        || (j == 1 && (value = property.getResourceValue()) != null)) {
                        propertyElement = propertiesElement.addElement(CmsImportExportManager.N_PROPERTY);

                        if (j == 1) {
                            // add a type attrib. to the property node in case of a shared/resource property value
                            propertyElement.addAttribute(
                                CmsImportExportManager.N_PROPERTY_ATTRIB_TYPE,
                                CmsImportExportManager.N_PROPERTY_ATTRIB_TYPE_SHARED);
                        }

                        propertyElement.addElement(CmsImportExportManager.N_NAME).addText(key);
                        propertyElement.addElement(CmsImportExportManager.N_VALUE).addCDATA(value);
                    }
                }
            }

            // append the nodes for access control entries
            Element acl = fileElement.addElement(CmsImportExportManager.N_ACCESSCONTROL_ENTRIES);

            // read the access control entries
            List fileAcEntries = getCms().getAccessControlEntries(getCms().getSitePath(resource), false);
            Iterator i = fileAcEntries.iterator();

            // create xml elements for each access control entry
            while (i.hasNext()) {
                CmsAccessControlEntry ace = (CmsAccessControlEntry)i.next();
                Element a = acl.addElement(CmsImportExportManager.N_ACCESSCONTROL_ENTRY);

                // now check if the principal is a group or a user
                int flags = ace.getFlags();
                String acePrincipalName = "";
                CmsUUID acePrincipal = ace.getPrincipal();
                if ((flags & I_CmsConstants.C_ACCESSFLAGS_GROUP) > 0) {
                    // the principal is a group
                    acePrincipalName = I_CmsPrincipal.C_PRINCIPAL_GROUP
                        + '.'
                        + getCms().readGroup(acePrincipal).getName();
                } else {
                    // the principal is a user
                    acePrincipalName = I_CmsPrincipal.C_PRINCIPAL_USER
                        + '.'
                        + getCms().readUser(acePrincipal).getName();
                }

                a.addElement(CmsImportExportManager.N_ACCESSCONTROL_PRINCIPAL).addText(acePrincipalName);
                a.addElement(CmsImportExportManager.N_FLAGS).addText(Integer.toString(flags));

                Element b = a.addElement(CmsImportExportManager.N_ACCESSCONTROL_PERMISSIONSET);
                b.addElement(CmsImportExportManager.N_ACCESSCONTROL_ALLOWEDPERMISSIONS).addText(
                    Integer.toString(ace.getAllowedPermissions()));
                b.addElement(CmsImportExportManager.N_ACCESSCONTROL_DENIEDPERMISSIONS).addText(
                    Integer.toString(ace.getDeniedPermissions()));
            }

            // write the XML
            digestElement(m_resourceNode, fileElement);
        } catch (CmsImportExportException e) {

            throw e;
        } catch (CmsException e) {

            CmsMessageContainer message = Messages.get().container(
                Messages.ERR_IMPORTEXPORT_ERROR_APPENDING_RESOURCE_TO_MANIFEST_1,
                resource.getRootPath());
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }

            throw new CmsImportExportException(message, e);
        }
    }

    /**
     * Returns true if the checked resource name can be exported depending on the include settings.<p>
     * 
     * @param resourcename the absolute path of the resource
     * @return true if the checked resource name can be exported depending on the include settings
     */
    private boolean checkExportResource(String resourcename) {

        return (// other folder than "/system/" will be exported
        !resourcename.startsWith(I_CmsWpConstants.C_VFS_PATH_SYSTEM) // OR always export "/system/"
            || resourcename.equalsIgnoreCase(I_CmsWpConstants.C_VFS_PATH_SYSTEM) // OR always export "/system/bodies/"                                  
            || resourcename.startsWith(I_CmsWpConstants.C_VFS_PATH_BODIES) // OR always export "/system/galleries/"
            || resourcename.startsWith(I_CmsWpConstants.C_VFS_PATH_GALLERIES) // OR option "include system folder" selected
        || (m_includeSystem // AND export folder is a system folder
        && resourcename.startsWith(I_CmsWpConstants.C_VFS_PATH_SYSTEM)));
    }

    /**
     * Exports one single file with all its data and content.<p>
     *
     * @param file the file to be exported
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong procesing the manifest.xml
     * @throws CmsLoaderException if an "old style" XML page could be exported
     * @throws IOException if the ZIP entry for the file could be appended to the ZIP archive
     */
    private void exportFile(CmsFile file)
    throws CmsImportExportException, SAXException, CmsLoaderException, IOException {

        String source = trimResourceName(getCms().getSitePath(file));
        I_CmsReport report = getReport();
        m_exportCount++;
        report.print(org.opencms.report.Messages.get().container(
            org.opencms.report.Messages.RPT_SUCCESSION_1,
            String.valueOf(m_exportCount)), I_CmsReport.C_FORMAT_NOTE);
        report.print(Messages.get().container(Messages.RPT_EXPORT_0), I_CmsReport.C_FORMAT_NOTE);
        report.print(org.opencms.report.Messages.get().container(
            org.opencms.report.Messages.RPT_ARGUMENT_1,
            getCms().getSitePath(file)));
        report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

        // store content in zip-file
        // check if the content of this resource was not already exported
        if (!m_exportedResources.contains(file.getResourceId())) {
            ZipEntry entry = new ZipEntry(source);
            // save the time of the last modification in the zip
            entry.setTime(file.getDateLastModified());
            getExportZipStream().putNextEntry(entry);
            getExportZipStream().write(file.getContents());
            getExportZipStream().closeEntry();
            // add the resource id to the storage to mark that this resource was already exported
            m_exportedResources.add(file.getResourceId());
            // create the manifest-entrys
            appendResourceToManifest(file, true);
        } else {
            // only create the manifest-entrys
            appendResourceToManifest(file, false);
        }
        // check if the resource is a page of the old style. if so, export the body as well       
        if (OpenCms.getResourceManager().getResourceType(file.getTypeId()).getTypeName().equals("page")) {
            m_exportedPageFiles.add("/" + source);
        }

        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().key(Messages.LOG_EXPORTING_OK_2, String.valueOf(m_exportCount), source));
        }
        report.println(
            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
            I_CmsReport.C_FORMAT_OK);
    }

    /**
     * Exports one single group with all it's data.<p>
     *
     * @param parent the parent node to add the groups to
     * @param group the group to be exported
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong procesing the manifest.xml
     */
    private void exportGroup(Element parent, CmsGroup group) throws CmsImportExportException, SAXException {

        try {
            String parentgroup;
            if (group.getParentId().isNullUUID()) {
                parentgroup = "";
            } else {
                parentgroup = getCms().getParent(group.getName()).getName();
            }

            Element e = parent.addElement(CmsImportExportManager.N_GROUPDATA);
            e.addElement(CmsImportExportManager.N_NAME).addText(group.getName());
            e.addElement(CmsImportExportManager.N_DESCRIPTION).addCDATA(group.getDescription());
            e.addElement(CmsImportExportManager.N_FLAGS).addText(Integer.toString(group.getFlags()));
            e.addElement(CmsImportExportManager.N_PARENTGROUP).addText(parentgroup);

            // write the XML
            digestElement(parent, e);
        } catch (CmsException e) {

            CmsMessageContainer message = org.opencms.db.Messages.get().container(
                org.opencms.db.Messages.ERR_GET_PARENT_GROUP_1,
                group.getName());
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }

            throw new CmsImportExportException(message, e);
        }
    }

    /**
     * Exports all groups with all data.<p>
     *
     * @param parent the parent node to add the groups to
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong procesing the manifest.xml
     */
    private void exportGroups(Element parent) throws CmsImportExportException, SAXException {

        try {
            I_CmsReport report = getReport();
            List allGroups = getCms().getGroups();
            for (int i = 0, l = allGroups.size(); i < l; i++) {
                CmsGroup group = (CmsGroup)allGroups.get(i);
                report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_2,
                    String.valueOf(i + 1),
                    String.valueOf(l)), I_CmsReport.C_FORMAT_NOTE);
                report.print(Messages.get().container(Messages.RPT_EXPORT_GROUP_0), I_CmsReport.C_FORMAT_NOTE);
                report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    group.getName()));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                exportGroup(parent, group);
                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.C_FORMAT_OK);
            }
        } catch (CmsImportExportException e) {

            throw e;
        } catch (CmsException e) {

            CmsMessageContainer message = org.opencms.db.Messages.get().container(
                org.opencms.db.Messages.ERR_GET_GROUPS_0);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }

            throw new CmsImportExportException(message, e);
        }
    }

    /**
     * Exports one single user with all its data.<p>
     * 
     * @param parent the parent node to add the users to
     * @param user the user to be exported
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong procesing the manifest.xml
     */
    private void exportUser(Element parent, CmsUser user) throws CmsImportExportException, SAXException {

        try {
            // add user node to the manifest.xml
            Element e = parent.addElement(CmsImportExportManager.N_USERDATA);
            e.addElement(CmsImportExportManager.N_NAME).addText(user.getName());
            // encode the password, using a base 64 decoder
            String passwd = new String(Base64.encodeBase64(user.getPassword().getBytes()));
            e.addElement(CmsImportExportManager.N_PASSWORD).addCDATA(passwd);
            e.addElement(CmsImportExportManager.N_DESCRIPTION).addCDATA(user.getDescription());
            e.addElement(CmsImportExportManager.N_FIRSTNAME).addText(user.getFirstname());
            e.addElement(CmsImportExportManager.N_LASTNAME).addText(user.getLastname());
            e.addElement(CmsImportExportManager.N_EMAIL).addText(user.getEmail());
            e.addElement(CmsImportExportManager.N_FLAGS).addText(Integer.toString(user.getFlags()));
            e.addElement(CmsImportExportManager.N_TAG_ADDRESS).addCDATA(user.getAddress());
            e.addElement(CmsImportExportManager.N_TYPE).addText(Integer.toString(user.getType()));
            // serialize the user info and write it into a file
            try {
                String datfileName = "/~" + CmsImportExportManager.N_USERINFO + "/" + user.getName() + ".dat";
                // create tag for userinfo
                e.addElement(CmsImportExportManager.N_USERINFO).addText(datfileName);
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(bout);
                oout.writeObject(user.getAdditionalInfo());
                oout.close();
                byte[] serializedInfo = bout.toByteArray();
                // store the serialized user info in the zip-file
                ZipEntry entry = new ZipEntry(datfileName);
                getExportZipStream().putNextEntry(entry);
                getExportZipStream().write(serializedInfo);
                getExportZipStream().closeEntry();
            } catch (IOException ioe) {
                getReport().println(ioe);

                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().key(Messages.ERR_IMPORTEXPORT_ERROR_EXPORTING_USER_1, user.getName()), ioe);
                }
            }
            // append the node for groups of user
            List userGroups = getCms().getDirectGroupsOfUser(user.getName());
            Element g = e.addElement(CmsImportExportManager.N_USERGROUPS);
            for (int i = 0; i < userGroups.size(); i++) {
                String groupName = ((CmsGroup)userGroups.get(i)).getName();
                g.addElement(CmsImportExportManager.N_GROUPNAME).addElement(CmsImportExportManager.N_NAME).addText(
                    groupName);
            }
            // write the XML
            digestElement(parent, e);
        } catch (CmsException e) {

            CmsMessageContainer message = org.opencms.db.Messages.get().container(
                org.opencms.db.Messages.ERR_GET_GROUPS_OF_USER_1,
                user.getName());
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }

            throw new CmsImportExportException(message, e);
        }
    }

    /**
     * Exports all users with all data.<p>
     *
     * @param parent the parent node to add the users to
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong procesing the manifest.xml
     */
    private void exportUsers(Element parent) throws CmsImportExportException, SAXException {

        try {
            I_CmsReport report = getReport();
            List allUsers = getCms().getUsers();
            for (int i = 0, l = allUsers.size(); i < l; i++) {
                CmsUser user = (CmsUser)allUsers.get(i);
                report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_SUCCESSION_2,
                    String.valueOf(i + 1),
                    String.valueOf(l)), I_CmsReport.C_FORMAT_NOTE);
                report.print(Messages.get().container(Messages.RPT_EXPORT_USER_0), I_CmsReport.C_FORMAT_NOTE);
                report.print(org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    user.getName()));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                exportUser(parent, user);
                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.C_FORMAT_OK);
            }
        } catch (CmsImportExportException e) {

            throw e;
        } catch (CmsException e) {

            CmsMessageContainer message = org.opencms.db.Messages.get().container(
                org.opencms.db.Messages.ERR_GET_USERS_0);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }

            throw new CmsImportExportException(message, e);
        }
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

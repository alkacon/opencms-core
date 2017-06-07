/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.db.CmsDefaultUsers;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.importexport.CmsImportExportManager.TimestampMode;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule.ExportMode;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsDataTypeUtil;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.util.CmsXmlSaxWriter;
import org.opencms.workplace.CmsWorkplace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
 * It will also contain a <code>manifest.xml</code> file in which all meta-information
 * about this files are stored, like permissions etc.<p>
 *
 * @since 6.0.0
 */
public class CmsExport {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExport.class);

    /** The cms context. */
    private CmsObject m_cms;

    /** Counter for the export. */
    private int m_exportCount;

    /** Set of all exported files, required for preventing redundant sibling export. */
    private Set<CmsUUID> m_exportedResources;

    /** The export writer. */
    private CmsExportHelper m_exportWriter;

    /** The export parameters. */
    private CmsExportParameters m_parameters;

    /** The report. */
    private I_CmsReport m_report;

    /** The top level file node where all resources are appended to. */
    private Element m_resourceNode;

    /** The SAX writer to write the output to. */
    private SAXWriter m_saxWriter;

    /** Cache for previously added super folders. */
    private List<String> m_superFolders;

    /**
     * Constructs a new uninitialized export, required for special subclass data export.<p>
     */
    public CmsExport() {

        // empty constructor
    }

    /**
     * Constructs a new export.<p>
     *
     * @param cms the cms context
     * @param report the report
     *
     * @throws CmsRoleViolationException if the current user has not the required role
     */
    public CmsExport(CmsObject cms, I_CmsReport report)
    throws CmsRoleViolationException {

        m_cms = cms;
        m_report = report;

        // check if the user has the required permissions
        OpenCms.getRoleManager().checkRole(getCms(), CmsRole.DATABASE_MANAGER);

    }

    /**
     * Export the data.<p>
     *
     * @param parameters the export parameters
     *
     * @throws CmsImportExportException if something goes wrong
     */
    public void exportData(CmsExportParameters parameters) throws CmsImportExportException {

        m_parameters = parameters;
        m_exportCount = 0;

        // clear all caches
        getReport().println(Messages.get().container(Messages.RPT_CLEARCACHE_0), I_CmsReport.FORMAT_NOTE);
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, new HashMap<String, Object>(0)));

        try {
            Element exportNode = openExportFile(parameters.getExportMode());

            if (m_parameters.getModuleInfo() != null) {
                // add the module element
                exportNode.add(m_parameters.getModuleInfo());
                // write the XML
                digestElement(exportNode, m_parameters.getModuleInfo());
            }

            // export account data only if selected
            if (m_parameters.isExportAccountData()) {
                Element accountsElement = exportNode.addElement(CmsImportVersion10.N_ACCOUNTS);
                getSaxWriter().writeOpen(accountsElement);

                exportOrgUnits(accountsElement);

                getSaxWriter().writeClose(accountsElement);
                exportNode.remove(accountsElement);
            }

            // export resource data only if selected
            if (m_parameters.isExportResourceData()) {
                exportAllResources(exportNode, m_parameters.getResources());
            }

            // export project data only if selected
            if (m_parameters.isExportProjectData()) {
                Element projectsElement = exportNode.addElement(CmsImportVersion10.N_PROJECTS);
                getSaxWriter().writeOpen(projectsElement);

                exportProjects(projectsElement);

                getSaxWriter().writeClose(projectsElement);
                exportNode.remove(projectsElement);
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
     * Exports the given folder and all child resources.<p>
     *
     * @param folderName to complete path to the resource to export
     *
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     * @throws IOException if not all resources could be appended to the ZIP archive
     */
    protected void addChildResources(String folderName) throws CmsImportExportException, IOException, SAXException {

        try {
            // get all subFolders
            List<CmsResource> subFolders = getCms().getSubFolders(folderName, CmsResourceFilter.IGNORE_EXPIRATION);
            // get all files in folder
            List<CmsResource> subFiles = getCms().getFilesInFolder(folderName, CmsResourceFilter.IGNORE_EXPIRATION);

            // walk through all files and export them
            for (int i = 0; i < subFiles.size(); i++) {
                CmsResource file = subFiles.get(i);
                CmsResourceState state = file.getState();
                long age = file.getDateLastModified() < file.getDateCreated()
                ? file.getDateCreated()
                : file.getDateLastModified();

                if (getCms().getRequestContext().getCurrentProject().isOnlineProject()
                    || (m_parameters.isIncludeUnchangedResources())
                    || state.isNew()
                    || state.isChanged()) {
                    if (!state.isDeleted()
                        && !CmsWorkplace.isTemporaryFile(file)
                        && (age >= m_parameters.getContentAge())) {
                        String export = getCms().getSitePath(file);
                        if (checkExportResource(export)) {
                            if (isInExportableProject(file)) {
                                exportFile(getCms().readFile(export, CmsResourceFilter.IGNORE_EXPIRATION));
                            }
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
                CmsResource folder = subFolders.get(i);
                if (folder.getState() != CmsResource.STATE_DELETED) {
                    // check if this is a system-folder and if it should be included.
                    String export = getCms().getSitePath(folder);
                    if (checkExportResource(export)) {

                        long age = folder.getDateLastModified() < folder.getDateCreated()
                        ? folder.getDateCreated()
                        : folder.getDateLastModified();
                        // export this folder only if age is above selected age
                        // default for selected age (if not set by user) is <code>long 0</code> (i.e. 1970)
                        if (age >= m_parameters.getContentAge()) {
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
     * Adds all files in fileNames to the manifest.xml file.<p>
     *
     * @param fileNames list of path Strings, e.g. <code>/folder/index.html</code>
     *
     * @throws CmsImportExportException if something goes wrong
     * @throws IOException if a file could not be exported
     * @throws SAXException if something goes wrong processing the manifest.xml
     */
    protected void addFiles(List<String> fileNames) throws CmsImportExportException, IOException, SAXException {

        if (fileNames != null) {
            for (int i = 0; i < fileNames.size(); i++) {
                String fileName = fileNames.get(i);

                try {
                    CmsFile file = getCms().readFile(fileName, CmsResourceFilter.IGNORE_EXPIRATION);
                    if (!file.getState().isDeleted() && !CmsWorkplace.isTemporaryFile(file)) {
                        if (checkExportResource(fileName)) {
                            if (m_parameters.isRecursive()) {
                                addParentFolders(fileName);
                            }
                            if (isInExportableProject(file)) {
                                exportFile(file);
                            }
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
     * Adds the parent folders of the given resource to the config file,
     * starting at the top, excluding the root folder.<p>
     *
     * @param resourceName the name of a resource in the VFS
     *
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     */
    protected void addParentFolders(String resourceName) throws CmsImportExportException, SAXException {

        try {
            // this is a resource in /system/ folder and option includeSystem is not true
            if (!checkExportResource(resourceName)) {
                return;
            }

            // Initialize the "previously added folder cache"
            if (m_superFolders == null) {
                m_superFolders = new ArrayList<String>();
            }
            List<String> superFolders = new ArrayList<String>();
            String currentSubFolder = resourceName;

            // Check, if the path is really a folder
            boolean isFolderResource = currentSubFolder.endsWith("/");

            while (currentSubFolder.length() > "/".length()) {
                currentSubFolder = currentSubFolder.substring(0, currentSubFolder.length() - 1);
                currentSubFolder = currentSubFolder.substring(0, currentSubFolder.lastIndexOf("/") + 1);
                if (currentSubFolder.length() <= "/".length()) {
                    break;
                }
                superFolders.add(currentSubFolder);
            }
            for (int i = superFolders.size() - 1; i >= 0; i--) {
                String addFolder = superFolders.get(i);
                if (!m_superFolders.contains(addFolder)) {
                    // This super folder was NOT added previously. Add it now!
                    CmsFolder folder = getCms().readFolder(addFolder, CmsResourceFilter.IGNORE_EXPIRATION);
                    appendResourceToManifest(folder, false, true);
                    // Remember that this folder was added
                    m_superFolders.add(addFolder);
                }
            }
            if (isFolderResource) { // add the folder itself
                if (!m_superFolders.contains(resourceName)) {
                    // This super folder was NOT added previously. Add it now!
                    CmsFolder folder = getCms().readFolder(resourceName, CmsResourceFilter.IGNORE_EXPIRATION);
                    appendResourceToManifest(folder, false);
                    // Remember that this folder was added
                    m_superFolders.add(resourceName);
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
     * Adds a property node to the manifest.xml.<p>
     *
     * @param propertiesElement the parent element to append the node to
     * @param propertyName the name of the property
     * @param propertyValue the value of the property
     * @param shared if <code>true</code>, add a shared property attribute to the generated property node
     */
    protected void addPropertyNode(
        Element propertiesElement,
        String propertyName,
        String propertyValue,
        boolean shared) {

        if (propertyValue != null) {
            Element propertyElement = propertiesElement.addElement(CmsImportVersion10.N_PROPERTY);
            if (shared) {
                // add "type" attribute to the property node in case of a shared/resource property value
                propertyElement.addAttribute(CmsImportVersion10.A_TYPE, CmsImportVersion10.PROPERTY_ATTRIB_TYPE_SHARED);
            }
            propertyElement.addElement(CmsImportVersion10.N_NAME).addText(propertyName);
            propertyElement.addElement(CmsImportVersion10.N_VALUE).addCDATA(propertyValue);
        }
    }

    /**
     * Adds a relation node to the <code>manifest.xml</code>.<p>
     *
     * @param relationsElement the parent element to append the node to
     * @param structureId the structure id of the target relation
     * @param sitePath the site path of the target relation
     * @param relationType the type of the relation
     */
    protected void addRelationNode(Element relationsElement, String structureId, String sitePath, String relationType) {

        if ((structureId != null) && (sitePath != null) && (relationType != null)) {
            Element relationElement = relationsElement.addElement(CmsImportVersion10.N_RELATION);

            relationElement.addElement(CmsImportVersion10.N_ID).addText(structureId);
            relationElement.addElement(CmsImportVersion10.N_PATH).addText(sitePath);
            relationElement.addElement(CmsImportVersion10.N_TYPE).addText(relationType);
        }
    }

    /** @see #appendResourceToManifest(CmsResource, boolean, boolean)
     * @param resource @see #appendResourceToManifest(CmsResource, boolean, boolean)
     * @param source @see #appendResourceToManifest(CmsResource, boolean, boolean)
     * @throws CmsImportExportException @see #appendResourceToManifest(CmsResource, boolean, boolean)
     * @throws SAXException @see #appendResourceToManifest(CmsResource, boolean, boolean)
     */
    protected void appendResourceToManifest(CmsResource resource, boolean source)
    throws CmsImportExportException, SAXException {

        appendResourceToManifest(resource, source, false);
    }

    /**
     * Writes the data for a resource (like access-rights) to the <code>manifest.xml</code> file.<p>
     *
     * @param resource the resource to get the data from
     * @param source flag to show if the source information in the xml file must be written
     * @param isSuperFolder flag to indicate that the resource is only a super folder of a module resource.
     *  This will prevent exporting uuid and creation date in the reduced export mode.
     *
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     */
    protected void appendResourceToManifest(CmsResource resource, boolean source, boolean isSuperFolder)
    throws CmsImportExportException, SAXException {

        try {
            // only write <source> if resource is a file
            String fileName = trimResourceName(getCms().getSitePath(resource));
            if (fileName.startsWith("system/orgunits")) {
                // it is not allowed to export organizational unit resources
                // export the organizational units instead
                return;
            }

            // define the file node
            Element fileElement = m_resourceNode.addElement(CmsImportVersion10.N_FILE);

            if (resource.isFile()) {
                if (source) {
                    fileElement.addElement(CmsImportVersion10.N_SOURCE).addText(fileName);
                }
            } else {
                m_exportCount++;
                I_CmsReport report = getReport();
                // output something to the report for the folder
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_SUCCESSION_1,
                        String.valueOf(m_exportCount)),
                    I_CmsReport.FORMAT_NOTE);
                report.print(Messages.get().container(Messages.RPT_EXPORT_0), I_CmsReport.FORMAT_NOTE);
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        getCms().getSitePath(resource)));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);

                if (LOG.isInfoEnabled()) {
                    LOG.info(
                        Messages.get().getBundle().key(
                            Messages.LOG_EXPORTING_OK_2,
                            String.valueOf(m_exportCount),
                            getCms().getSitePath(resource)));
                }
            }

            boolean isReducedExportMode = m_parameters.getExportMode().equals(ExportMode.REDUCED);
            // <destination>
            fileElement.addElement(CmsImportVersion10.N_DESTINATION).addText(fileName);
            // <type>
            fileElement.addElement(CmsImportVersion10.N_TYPE).addText(
                OpenCms.getResourceManager().getResourceType(resource.getTypeId()).getTypeName());

            if (!(isReducedExportMode && isSuperFolder)) {
                //  <uuidstructure>
                fileElement.addElement(CmsImportVersion10.N_UUIDSTRUCTURE).addText(
                    resource.getStructureId().toString());
                if (resource.isFile()) {
                    //  <uuidresource>
                    fileElement.addElement(CmsImportVersion10.N_UUIDRESOURCE).addText(
                        resource.getResourceId().toString());
                }
            }

            if (!isReducedExportMode) {
                // <datelastmodified>
                fileElement.addElement(CmsImportVersion10.N_DATELASTMODIFIED).addText(
                    getDateLastModifiedForExport(resource));
                // <userlastmodified>
                String userNameLastModified = null;
                try {
                    userNameLastModified = getCms().readUser(resource.getUserLastModified()).getName();
                } catch (@SuppressWarnings("unused") CmsException e) {
                    userNameLastModified = OpenCms.getDefaultUsers().getUserAdmin();
                }
                fileElement.addElement(CmsImportVersion10.N_USERLASTMODIFIED).addText(userNameLastModified);
            }
            if (!(isReducedExportMode && isSuperFolder)) {
                // <datecreated>
                fileElement.addElement(CmsImportVersion10.N_DATECREATED).addText(
                    CmsDateUtil.getHeaderDate(resource.getDateCreated()));
            }
            if (!isReducedExportMode) {
                // <usercreated>
                String userNameCreated = null;
                try {
                    userNameCreated = getCms().readUser(resource.getUserCreated()).getName();
                } catch (@SuppressWarnings("unused") CmsException e) {
                    userNameCreated = OpenCms.getDefaultUsers().getUserAdmin();
                }
                fileElement.addElement(CmsImportVersion10.N_USERCREATED).addText(userNameCreated);
            }
            if (!(isReducedExportMode && isSuperFolder)) {
                // <release>
                if (resource.getDateReleased() != CmsResource.DATE_RELEASED_DEFAULT) {
                    fileElement.addElement(CmsImportVersion10.N_DATERELEASED).addText(
                        CmsDateUtil.getHeaderDate(resource.getDateReleased()));
                }
                // <expire>
                if (resource.getDateExpired() != CmsResource.DATE_EXPIRED_DEFAULT) {
                    fileElement.addElement(CmsImportVersion10.N_DATEEXPIRED).addText(
                        CmsDateUtil.getHeaderDate(resource.getDateExpired()));
                }
                // <flags>
                int resFlags = resource.getFlags();
                resFlags &= ~CmsResource.FLAG_LABELED;
                fileElement.addElement(CmsImportVersion10.N_FLAGS).addText(Integer.toString(resFlags));

                // write the properties to the manifest
                Element propertiesElement = fileElement.addElement(CmsImportVersion10.N_PROPERTIES);
                List<CmsProperty> properties = getCms().readPropertyObjects(getCms().getSitePath(resource), false);
                // sort the properties for a well defined output order
                Collections.sort(properties);
                for (int i = 0, n = properties.size(); i < n; i++) {
                    CmsProperty property = properties.get(i);
                    if (isIgnoredProperty(property)) {
                        continue;
                    }
                    addPropertyNode(propertiesElement, property.getName(), property.getStructureValue(), false);
                    addPropertyNode(propertiesElement, property.getName(), property.getResourceValue(), true);
                }

                // Write the relations to the manifest
                List<CmsRelation> relations = getCms().getRelationsForResource(
                    resource,
                    CmsRelationFilter.TARGETS.filterNotDefinedInContent());
                Element relationsElement = fileElement.addElement(CmsImportVersion10.N_RELATIONS);
                // iterate over the relations
                for (CmsRelation relation : relations) {
                    // relation may be broken already:
                    try {
                        CmsResource target = relation.getTarget(getCms(), CmsResourceFilter.ALL);
                        String structureId = target.getStructureId().toString();
                        String sitePath = getCms().getSitePath(target);
                        String relationType = relation.getType().getName();
                        addRelationNode(relationsElement, structureId, sitePath, relationType);
                    } catch (CmsVfsResourceNotFoundException crnfe) {
                        // skip this relation:
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(
                                Messages.get().getBundle().key(
                                    Messages.LOG_IMPORTEXPORT_WARN_DELETED_RELATIONS_2,
                                    new String[] {relation.getTargetPath(), resource.getRootPath()}),
                                crnfe);
                        }
                    }
                }

                // append the nodes for access control entries
                Element acl = fileElement.addElement(CmsImportVersion10.N_ACCESSCONTROL_ENTRIES);

                // read the access control entries
                List<CmsAccessControlEntry> fileAcEntries = getCms().getAccessControlEntries(
                    getCms().getSitePath(resource),
                    false);
                Iterator<CmsAccessControlEntry> i = fileAcEntries.iterator();

                // create xml elements for each access control entry
                while (i.hasNext()) {
                    CmsAccessControlEntry ace = i.next();
                    Element a = acl.addElement(CmsImportVersion10.N_ACCESSCONTROL_ENTRY);

                    // now check if the principal is a group or a user
                    int flags = ace.getFlags();
                    String acePrincipalName = "";
                    CmsUUID acePrincipal = ace.getPrincipal();
                    if ((flags & CmsAccessControlEntry.ACCESS_FLAGS_ALLOTHERS) > 0) {
                        acePrincipalName = CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME;
                    } else if ((flags & CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE_ALL) > 0) {
                        acePrincipalName = CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_NAME;
                    } else if ((flags & CmsAccessControlEntry.ACCESS_FLAGS_GROUP) > 0) {
                        // the principal is a group
                        try {
                            acePrincipalName = getCms().readGroup(acePrincipal).getPrefixedName();
                        } catch (@SuppressWarnings("unused") CmsException e) {
                            // the group for this permissions does not exist anymore, so simply skip it
                        }
                    } else if ((flags & CmsAccessControlEntry.ACCESS_FLAGS_USER) > 0) {
                        // the principal is a user
                        try {
                            acePrincipalName = getCms().readUser(acePrincipal).getPrefixedName();
                        } catch (@SuppressWarnings("unused") CmsException e) {
                            // the user for this permissions does not exist anymore, so simply skip it
                        }
                    } else {
                        // the principal is a role
                        acePrincipalName = CmsRole.PRINCIPAL_ROLE + "." + CmsRole.valueOfId(acePrincipal).getRoleName();
                    }

                    // only add the permission if a principal was set
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(acePrincipalName)) {
                        a.addElement(CmsImportVersion10.N_ACCESSCONTROL_PRINCIPAL).addText(acePrincipalName);
                        a.addElement(CmsImportVersion10.N_FLAGS).addText(Integer.toString(flags));

                        Element b = a.addElement(CmsImportVersion10.N_ACCESSCONTROL_PERMISSIONSET);
                        b.addElement(CmsImportVersion10.N_ACCESSCONTROL_ALLOWEDPERMISSIONS).addText(
                            Integer.toString(ace.getAllowedPermissions()));
                        b.addElement(CmsImportVersion10.N_ACCESSCONTROL_DENIEDPERMISSIONS).addText(
                            Integer.toString(ace.getDeniedPermissions()));
                    }
                }
            } else {
                fileElement.addElement(CmsImportVersion10.N_PROPERTIES);
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
    protected boolean checkExportResource(String resourcename) {

        return (// other folder than "/system/" will be exported
        !resourcename.startsWith(CmsWorkplace.VFS_PATH_SYSTEM) // OR always export "/system/"
            || resourcename.equalsIgnoreCase(CmsWorkplace.VFS_PATH_SYSTEM) // OR always export "/system/galleries/"
            || resourcename.startsWith(CmsWorkplace.VFS_PATH_GALLERIES) // OR option "include system folder" selected
            || (m_parameters.isIncludeSystemFolder() // AND export folder is a system folder
                && resourcename.startsWith(CmsWorkplace.VFS_PATH_SYSTEM)));
    }

    /**
     * Closes the export ZIP file and saves the XML document for the manifest.<p>
     *
     * @param exportNode the export root node
     *
     * @throws SAXException if something goes wrong processing the manifest.xml
     * @throws IOException if something goes wrong while closing the export file
     */
    protected void closeExportFile(Element exportNode) throws IOException, SAXException {

        // close the <export> Tag
        getSaxWriter().writeClose(exportNode);

        // close the XML document
        CmsXmlSaxWriter xmlSaxWriter = (CmsXmlSaxWriter)getSaxWriter().getContentHandler();

        // write the manifest file
        m_exportWriter.writeManifest(xmlSaxWriter);
    }

    /**
     * Writes the output element to the XML output writer and detaches it
     * from it's parent element.<p>
     *
     * @param parent the parent element
     * @param output the output element
     *
     * @throws SAXException if something goes wrong processing the manifest.xml
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
     *
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     * @throws IOException if not all resources could be appended to the ZIP archive
     */
    protected void exportAllResources(Element parent, List<String> resourcesToExport)
    throws CmsImportExportException, IOException, SAXException {

        // export all the resources
        String resourceNodeName = getResourceNodeName();
        m_resourceNode = parent.addElement(resourceNodeName);
        getSaxWriter().writeOpen(m_resourceNode);

        if (m_parameters.isRecursive()) {
            // remove the possible redundancies in the list of resources
            resourcesToExport = CmsFileUtil.removeRedundancies(resourcesToExport);
        }

        // distinguish folder and file names
        List<String> folderNames = new ArrayList<String>();
        List<String> fileNames = new ArrayList<String>();
        Iterator<String> it = resourcesToExport.iterator();
        while (it.hasNext()) {
            String resource = it.next();
            if (CmsResource.isFolder(resource)) {
                folderNames.add(resource);
            } else {
                fileNames.add(resource);
            }
        }

        m_exportedResources = new HashSet<CmsUUID>();

        // export the folders
        for (int i = 0; i < folderNames.size(); i++) {
            String path = folderNames.get(i);
            if (m_parameters.isRecursive()) {
                // first add super folders to the xml-config file
                addParentFolders(path);
                addChildResources(path);
            } else {
                CmsFolder folder;
                try {
                    folder = getCms().readFolder(path, CmsResourceFilter.IGNORE_EXPIRATION);
                } catch (CmsException e) {
                    CmsMessageContainer message = Messages.get().container(
                        Messages.ERR_IMPORTEXPORT_ERROR_ADDING_PARENT_FOLDERS_1,
                        path);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(message.key(), e);
                    }
                    throw new CmsImportExportException(message, e);
                }
                CmsResourceState state = folder.getState();
                long age = folder.getDateLastModified() < folder.getDateCreated()
                ? folder.getDateCreated()
                : folder.getDateLastModified();

                if (getCms().getRequestContext().getCurrentProject().isOnlineProject()
                    || (m_parameters.isIncludeUnchangedResources())
                    || state.isNew()
                    || state.isChanged()) {
                    if (!state.isDeleted() && (age >= m_parameters.getContentAge())) {
                        // check if this is a system-folder and if it should be included.
                        String export = getCms().getSitePath(folder);
                        if (checkExportResource(export)) {
                            appendResourceToManifest(folder, true);
                        }
                    }
                }
            }
        }
        // export the files
        addFiles(fileNames);

        // write the XML
        getSaxWriter().writeClose(m_resourceNode);
        parent.remove(m_resourceNode);
        m_resourceNode = null;
    }

    /**
     * Exports one single file with all its data and content.<p>
     *
     * @param file the file to be exported
     *
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     * @throws IOException if the ZIP entry for the file could be appended to the ZIP archive
     */
    protected void exportFile(CmsFile file) throws CmsImportExportException, SAXException, IOException {

        String source = trimResourceName(getCms().getSitePath(file));
        I_CmsReport report = getReport();
        m_exportCount++;
        report.print(
            org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_SUCCESSION_1,
                String.valueOf(m_exportCount)),
            I_CmsReport.FORMAT_NOTE);
        report.print(Messages.get().container(Messages.RPT_EXPORT_0), I_CmsReport.FORMAT_NOTE);
        report.print(
            org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_ARGUMENT_1,
                getCms().getSitePath(file)));
        report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

        // store content in zip-file
        // check if the content of this resource was not already exported
        if (!m_exportedResources.contains(file.getResourceId())) {
            // write the file using the export writer
            m_exportWriter.writeFile(file, source);
            // add the resource id to the storage to mark that this resource was already exported
            m_exportedResources.add(file.getResourceId());
            // create the manifest-entries
            appendResourceToManifest(file, true);
        } else {
            // only create the manifest-entries
            appendResourceToManifest(file, false);
        }

        if (LOG.isInfoEnabled()) {
            LOG.info(
                Messages.get().getBundle().key(Messages.LOG_EXPORTING_OK_2, String.valueOf(m_exportCount), source));
        }
        report.println(
            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
            I_CmsReport.FORMAT_OK);
    }

    /**
     * Exports one single group with all it's data.<p>
     *
     * @param parent the parent node to add the groups to
     * @param group the group to be exported
     *
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     */
    protected void exportGroup(Element parent, CmsGroup group) throws CmsImportExportException, SAXException {

        try {
            String parentgroup;
            if ((group.getParentId() == null) || group.getParentId().isNullUUID()) {
                parentgroup = "";
            } else {
                parentgroup = getCms().getParent(group.getName()).getName();
            }

            Element e = parent.addElement(CmsImportVersion10.N_GROUP);
            e.addElement(CmsImportVersion10.N_NAME).addText(group.getSimpleName());
            e.addElement(CmsImportVersion10.N_DESCRIPTION).addCDATA(group.getDescription());
            e.addElement(CmsImportVersion10.N_FLAGS).addText(Integer.toString(group.getFlags()));
            e.addElement(CmsImportVersion10.N_PARENTGROUP).addText(parentgroup);

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
     * Exports all groups of the given organizational unit.<p>
     *
     * @param parent the parent node to add the groups to
     * @param orgunit the organizational unit to write the groups for
     *
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     */
    protected void exportGroups(Element parent, CmsOrganizationalUnit orgunit)
    throws CmsImportExportException, SAXException {

        try {
            I_CmsReport report = getReport();
            List<CmsGroup> allGroups = OpenCms.getOrgUnitManager().getGroups(getCms(), orgunit.getName(), false);
            for (int i = 0, l = allGroups.size(); i < l; i++) {
                CmsGroup group = allGroups.get(i);
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_SUCCESSION_2,
                        String.valueOf(i + 1),
                        String.valueOf(l)),
                    I_CmsReport.FORMAT_NOTE);
                report.print(Messages.get().container(Messages.RPT_EXPORT_GROUP_0), I_CmsReport.FORMAT_NOTE);
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        group.getName()));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                exportGroup(parent, group);
                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);
            }
        } catch (CmsImportExportException e) {
            throw e;
        } catch (CmsException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
            throw new CmsImportExportException(e.getMessageContainer(), e);
        }
    }

    /**
     * Exports one single organizational unit with all it's data.<p>
     *
     * @param parent the parent node to add the groups to
     * @param orgunit the group to be exported
     *
     * @throws SAXException if something goes wrong processing the manifest.xml
     * @throws CmsException if something goes wrong reading the data to export
     */
    protected void exportOrgUnit(Element parent, CmsOrganizationalUnit orgunit) throws SAXException, CmsException {

        Element orgunitElement = parent.addElement(CmsImportVersion10.N_ORGUNIT);
        getSaxWriter().writeOpen(orgunitElement);

        Element name = orgunitElement.addElement(CmsImportVersion10.N_NAME).addText(orgunit.getName());
        digestElement(orgunitElement, name);

        Element description = orgunitElement.addElement(CmsImportVersion10.N_DESCRIPTION).addCDATA(
            orgunit.getDescription());
        digestElement(orgunitElement, description);

        Element flags = orgunitElement.addElement(CmsImportVersion10.N_FLAGS).addText(
            Integer.toString(orgunit.getFlags()));
        digestElement(orgunitElement, flags);

        Element resources = orgunitElement.addElement(CmsImportVersion10.N_RESOURCES);
        Iterator<CmsResource> it = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
            getCms(),
            orgunit.getName()).iterator();
        while (it.hasNext()) {
            CmsResource resource = it.next();
            resources.addElement(CmsImportVersion10.N_RESOURCE).addText(resource.getRootPath());
        }
        digestElement(orgunitElement, resources);
        getReport().println(
            org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
            I_CmsReport.FORMAT_OK);

        Element groupsElement = parent.addElement(CmsImportVersion10.N_GROUPS);
        getSaxWriter().writeOpen(groupsElement);
        exportGroups(groupsElement, orgunit);
        getSaxWriter().writeClose(groupsElement);

        Element usersElement = parent.addElement(CmsImportVersion10.N_USERS);
        getSaxWriter().writeOpen(usersElement);
        exportUsers(usersElement, orgunit);
        getSaxWriter().writeClose(usersElement);

        getSaxWriter().writeClose(orgunitElement);
    }

    /**
     * Exports all organizational units with all data.<p>
     *
     * @param parent the parent node to add the organizational units to
     *
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     */
    protected void exportOrgUnits(Element parent) throws CmsImportExportException, SAXException {

        try {
            Element orgunitsElement = parent.addElement(CmsImportVersion10.N_ORGUNITS);
            getSaxWriter().writeOpen(orgunitsElement);

            I_CmsReport report = getReport();
            List<CmsOrganizationalUnit> allOUs = new ArrayList<CmsOrganizationalUnit>();
            allOUs.add(OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), ""));
            allOUs.addAll(OpenCms.getOrgUnitManager().getOrganizationalUnits(getCms(), "", true));
            for (int i = 0; i < allOUs.size(); i++) {
                CmsOrganizationalUnit ou = allOUs.get(i);
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_SUCCESSION_2,
                        String.valueOf(i + 1),
                        String.valueOf(allOUs.size())),
                    I_CmsReport.FORMAT_NOTE);
                report.print(Messages.get().container(Messages.RPT_EXPORT_ORGUNIT_0), I_CmsReport.FORMAT_NOTE);
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        ou.getName()));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

                exportOrgUnit(orgunitsElement, ou);
            }
            getSaxWriter().writeClose(orgunitsElement);
        } catch (CmsImportExportException e) {
            throw e;
        } catch (CmsException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
            throw new CmsImportExportException(e.getMessageContainer(), e);
        }
    }

    /**
     * Exports one single project with all it's data.<p>
     *
     * @param parent the parent node to add the project to
     * @param project the project to be exported
     *
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     */
    protected void exportProject(Element parent, CmsProject project) throws CmsImportExportException, SAXException {

        I_CmsReport report = getReport();
        CmsDefaultUsers defaultUsers = OpenCms.getDefaultUsers();

        String users;
        try {
            users = getCms().readGroup(project.getGroupId()).getName();
        } catch (CmsException e) {
            CmsMessageContainer message = org.opencms.db.Messages.get().container(
                org.opencms.db.Messages.ERR_READ_GROUP_FOR_ID_1,
                project.getGroupId());
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }

            users = defaultUsers.getGroupUsers();
            report.println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
            report.print(message, I_CmsReport.FORMAT_ERROR);

        }
        String managers;
        try {
            managers = getCms().readGroup(project.getManagerGroupId()).getName();
        } catch (CmsException e) {
            CmsMessageContainer message = org.opencms.db.Messages.get().container(
                org.opencms.db.Messages.ERR_READ_GROUP_FOR_ID_1,
                project.getManagerGroupId());
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }

            managers = defaultUsers.getGroupAdministrators();
            report.println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
            report.print(message, I_CmsReport.FORMAT_ERROR);
        }

        Element e = parent.addElement(CmsImportVersion10.N_PROJECT);
        e.addElement(CmsImportVersion10.N_NAME).addText(project.getSimpleName());
        e.addElement(CmsImportVersion10.N_DESCRIPTION).addCDATA(project.getDescription());
        e.addElement(CmsImportVersion10.N_USERSGROUP).addText(users);
        e.addElement(CmsImportVersion10.N_MANAGERSGROUP).addText(managers);

        Element resources = e.addElement(CmsImportVersion10.N_RESOURCES);
        try {
            Iterator<String> it = getCms().readProjectResources(project).iterator();
            while (it.hasNext()) {
                String resName = it.next();
                resources.addElement(CmsImportVersion10.N_RESOURCE).addText(resName);
            }
        } catch (CmsException exc) {
            CmsMessageContainer message = org.opencms.db.Messages.get().container(
                org.opencms.db.Messages.ERR_READ_PROJECT_RESOURCES_2,
                project.getName(),
                project.getUuid());
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), exc);
            }

            throw new CmsImportExportException(message, exc);
        }
        // write the XML
        digestElement(parent, e);
    }

    /**
     * Exports all projects with all data.<p>
     *
     * @param parent the parent node to add the projects to
     *
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     */
    protected void exportProjects(Element parent) throws CmsImportExportException, SAXException {

        try {
            I_CmsReport report = getReport();
            List<CmsProject> allProjects = OpenCms.getOrgUnitManager().getAllManageableProjects(getCms(), "", true);
            for (int i = 0; i < allProjects.size(); i++) {
                CmsProject project = allProjects.get(i);
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_SUCCESSION_2,
                        String.valueOf(i + 1),
                        String.valueOf(allProjects.size())),
                    I_CmsReport.FORMAT_NOTE);
                report.print(Messages.get().container(Messages.RPT_EXPORT_PROJECT_0), I_CmsReport.FORMAT_NOTE);
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        project.getName()));

                exportProject(parent, project);

                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);
            }
        } catch (CmsImportExportException e) {
            throw e;
        } catch (CmsException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
            throw new CmsImportExportException(e.getMessageContainer(), e);
        }
    }

    /**
     * Exports one single user with all its data.<p>
     *
     * @param parent the parent node to add the users to
     * @param user the user to be exported
     *
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     */
    protected void exportUser(Element parent, CmsUser user) throws CmsImportExportException, SAXException {

        try {
            // add user node to the manifest.xml
            Element e = parent.addElement(CmsImportVersion10.N_USER);
            e.addElement(CmsImportVersion10.N_NAME).addText(user.getSimpleName());
            // encode the password, using a base 64 decoder
            String passwd = new String(Base64.encodeBase64(user.getPassword().getBytes()));
            e.addElement(CmsImportVersion10.N_PASSWORD).addCDATA(passwd);
            e.addElement(CmsImportVersion10.N_FIRSTNAME).addText(user.getFirstname());
            e.addElement(CmsImportVersion10.N_LASTNAME).addText(user.getLastname());
            e.addElement(CmsImportVersion10.N_EMAIL).addText(user.getEmail());
            e.addElement(CmsImportVersion10.N_FLAGS).addText(Integer.toString(user.getFlags()));
            e.addElement(CmsImportVersion10.N_DATECREATED).addText(Long.toString(user.getDateCreated()));

            Element userInfoNode = e.addElement(CmsImportVersion10.N_USERINFO);
            List<String> keys = new ArrayList<String>(user.getAdditionalInfo().keySet());
            Collections.sort(keys);
            Iterator<String> itInfoKeys = keys.iterator();
            while (itInfoKeys.hasNext()) {
                String key = itInfoKeys.next();
                if (key == null) {
                    continue;
                }
                Object value = user.getAdditionalInfo(key);
                if (value == null) {
                    continue;
                }
                Element entryNode = userInfoNode.addElement(CmsImportVersion10.N_USERINFO_ENTRY);
                entryNode.addAttribute(CmsImportVersion10.A_NAME, key);
                entryNode.addAttribute(CmsImportVersion10.A_TYPE, value.getClass().getName());
                try {
                    // serialize the user info and write it into a file
                    entryNode.addCDATA(CmsDataTypeUtil.dataExport(value));
                } catch (IOException ioe) {
                    getReport().println(ioe);
                    if (LOG.isErrorEnabled()) {
                        LOG.error(
                            Messages.get().getBundle().key(
                                Messages.ERR_IMPORTEXPORT_ERROR_EXPORTING_USER_1,
                                user.getName()),
                            ioe);
                    }
                }
            }

            // append node for roles of user
            Element userRoles = e.addElement(CmsImportVersion10.N_USERROLES);
            List<CmsRole> roles = OpenCms.getRoleManager().getRolesOfUser(
                getCms(),
                user.getName(),
                "",
                true,
                true,
                true);
            for (int i = 0; i < roles.size(); i++) {
                String roleName = roles.get(i).getFqn();
                userRoles.addElement(CmsImportVersion10.N_USERROLE).addText(roleName);
            }
            // append the node for groups of user
            Element userGroups = e.addElement(CmsImportVersion10.N_USERGROUPS);
            List<CmsGroup> groups = getCms().getGroupsOfUser(user.getName(), true, true);
            for (int i = 0; i < groups.size(); i++) {
                String groupName = groups.get(i).getName();
                userGroups.addElement(CmsImportVersion10.N_USERGROUP).addText(groupName);
            }
            // write the XML
            digestElement(parent, e);
        } catch (CmsException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
            throw new CmsImportExportException(e.getMessageContainer(), e);
        }
    }

    /**
     * Exports all users of the given organizational unit.<p>
     *
     * @param parent the parent node to add the users to
     * @param orgunit the organizational unit to write the groups for
     *
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     */
    protected void exportUsers(Element parent, CmsOrganizationalUnit orgunit)
    throws CmsImportExportException, SAXException {

        try {
            I_CmsReport report = getReport();
            List<CmsUser> allUsers = OpenCms.getOrgUnitManager().getUsers(getCms(), orgunit.getName(), false);
            for (int i = 0, l = allUsers.size(); i < l; i++) {
                CmsUser user = allUsers.get(i);
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_SUCCESSION_2,
                        String.valueOf(i + 1),
                        String.valueOf(l)),
                    I_CmsReport.FORMAT_NOTE);
                report.print(Messages.get().container(Messages.RPT_EXPORT_USER_0), I_CmsReport.FORMAT_NOTE);
                report.print(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_ARGUMENT_1,
                        user.getName()));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                exportUser(parent, user);
                report.println(
                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0),
                    I_CmsReport.FORMAT_OK);
            }
        } catch (CmsImportExportException e) {
            throw e;
        } catch (CmsException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
            throw new CmsImportExportException(e.getMessageContainer(), e);
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
     * Returns the name of the export file.<p>
     *
     * @return the name of the export file
     */
    protected String getExportFileName() {

        return m_parameters.getPath();
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
     * Returns the report to write progress messages to.<p>
     *
     * @return the report to write progress messages to
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
     * Returns the SAX based xml writer to write the XML output to.<p>
     *
     * @return the SAX based xml writer to write the XML output to
     */
    protected SAXWriter getSaxWriter() {

        return m_saxWriter;
    }

    /**
     * Checks if a property should be written to the export or not.<p>
     *
     * @param property the property to check
     *
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
     * Checks if a resource is belongs to the correct project for exporting.<p>
     *
     * @param res the resource to check
     *
     * @return <code>true</code>, if the resource can be exported, false otherwise
     */
    protected boolean isInExportableProject(CmsResource res) {

        boolean retValue = true;
        // the "only modified in current project flag" is checked
        if (m_parameters.isInProject()) {
            // resource state is new or changed
            if ((res.getState() == CmsResource.STATE_CHANGED) || (res.getState() == CmsResource.STATE_NEW)) {
                // the resource belongs not to the current project, so it must not be exported
                if (!res.getProjectLastModified().equals(getCms().getRequestContext().getCurrentProject().getUuid())) {
                    retValue = false;
                }
            } else {
                // state is unchanged, so do not export it
                retValue = false;
            }
        }
        return retValue;
    }

    /**
     * Opens the export ZIP file and initializes the internal XML document for the manifest.<p>
     * @param exportMode the export mode to use.
     *
     * @return the node in the XML document where all files are appended to
     *
     * @throws SAXException if something goes wrong processing the manifest.xml
     * @throws IOException if something goes wrong while closing the export file
     */
    protected Element openExportFile(ExportMode exportMode) throws IOException, SAXException {

        // create the export writer
        m_exportWriter = new CmsExportHelper(
            getExportFileName(),
            m_parameters.isExportAsFiles(),
            m_parameters.isXmlValidation());
        // initialize the dom4j writer object as member variable
        setSaxWriter(m_exportWriter.getSaxWriter());

        // the node in the XML document where the file entries are appended to
        String exportNodeName = getExportNodeName();
        // the XML document to write the XMl to
        Document doc = DocumentHelper.createDocument();
        // add main export node to XML document
        Element exportNode = doc.addElement(exportNodeName);
        getSaxWriter().writeOpen(exportNode);

        // add the info element. it contains all infos for this export
        Element info = exportNode.addElement(CmsImportExportManager.N_INFO);
        if (!exportMode.equals(ExportMode.REDUCED)) {
            info.addElement(CmsImportExportManager.N_CREATOR).addText(
                getCms().getRequestContext().getCurrentUser().getName());
            info.addElement(CmsImportExportManager.N_OC_VERSION).addText(OpenCms.getSystemInfo().getVersionNumber());
            info.addElement(CmsImportExportManager.N_DATE).addText(
                CmsDateUtil.getHeaderDate(System.currentTimeMillis()));
        }
        info.addElement(CmsImportExportManager.N_INFO_PROJECT).addText(
            getCms().getRequestContext().getCurrentProject().getName());
        info.addElement(CmsImportExportManager.N_VERSION).addText(CmsImportExportManager.EXPORT_VERSION);

        // write the XML
        digestElement(exportNode, info);

        return exportNode;
    }

    /**
     * Sets the SAX based XML writer to write the XML output to.<p>
     *
     * @param saxWriter the SAX based XML writer to write the XML output to
     */
    protected void setSaxWriter(SAXWriter saxWriter) {

        m_saxWriter = saxWriter;
    }

    /**
     * Cuts leading and trailing '/' from the given resource name.<p>
     *
     * @param resourceName the absolute path of a resource
     *
     * @return the trimmed resource name
     */
    protected String trimResourceName(String resourceName) {

        if (resourceName.startsWith("/")) {
            resourceName = resourceName.substring(1);
        }
        if (resourceName.endsWith("/")) {
            resourceName = resourceName.substring(0, resourceName.length() - 1);
        }
        return resourceName;
    }

    /** Returns the manifest entry for the <code>&lt;datelastmodified&gt;</code> node of the resource.
     * Depending on the export.timestamp property, the time stamp from the VFS (default) or
     * special macros are used.
     *
     * @param resource the resource for which the manifest entry is generated
     * @return the time stamp or macro to write as value for <code>&lt;datelastmodified&gt;</code>
     */
    private String getDateLastModifiedForExport(final CmsResource resource) {

        TimestampMode timeMode = TimestampMode.VFSTIME;
        String typeName = OpenCms.getResourceManager().getResourceType(resource).getTypeName();
        TimestampMode defaultModeForResourceType = OpenCms.getImportExportManager().getDefaultTimestampMode(typeName);
        if (null == defaultModeForResourceType) {
            try {
                CmsProperty exporttimeProp = m_cms.readPropertyObject(
                    resource,
                    CmsImportExportManager.PROP_EXPORT_TIMESTAMP,
                    true);
                if (TimestampMode.FILETIME.equals(TimestampMode.getEnum(exporttimeProp.getValue()))) {
                    timeMode = TimestampMode.FILETIME;
                } else if (TimestampMode.IMPORTTIME.equals(TimestampMode.getEnum(exporttimeProp.getValue()))) {
                    timeMode = TimestampMode.IMPORTTIME;
                }
            } catch (@SuppressWarnings("unused") CmsException e) {
                // Do nothing, use default mode
            }
        } else {
            timeMode = defaultModeForResourceType;
        }
        switch (timeMode) {
            case FILETIME:
                return CmsMacroResolver.formatMacro(CmsImportExportManager.TimestampMode.FILETIME.toString());
            case IMPORTTIME:
                return CmsMacroResolver.formatMacro(CmsImportExportManager.TimestampMode.IMPORTTIME.toString());
            case VFSTIME:
            default:
                return CmsDateUtil.getHeaderDate(resource.getDateLastModified());
        }

    }
}

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

import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.module.CmsModule.ExportMode;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.Element;

/**
 * Export parameters.<p>
 *
 * @since 7.0.4
 */
public class CmsExportParameters {

    /** The additional resource list. */
    private String m_additionalResourceList = null;

    /** Only resources modified after this time stamp will be exported. */
    private long m_contentAge;

    /** If the account data should be exported. */
    private boolean m_exportAccountData;

    /** Indicates if the resources are exported in one export .ZIP file (the default) or as individual files. */
    private boolean m_exportAsFiles;

    /** The export mode that should be used for the export. */
    private ExportMode m_exportMode = ExportMode.DEFAULT;

    /** If the project data should be exported. */
    private boolean m_exportProjectData;

    /** If the resource data should be exported. */
    private boolean m_exportResourceData = true;

    /** If the system folder should be included in the export.*/
    private boolean m_includeSystemFolder = true;

    /** If unchanged files should be included in the export.*/
    private boolean m_includeUnchangedResources = true;
    /** If set, only resources belonging to the current project will be exported. */
    private boolean m_inProject;

    /** The module informations if to export a module. */
    private Element m_moduleInfo;

    /** The site root that should override the site root from the request context. */
    private String m_overrideSiteRoot;

    /** The file path, should be a zip file. */
    private String m_path;

    /** If the resources should be recursively exported. */
    private boolean m_recursive = true;

    /** The resources to export.*/
    private List<String> m_resources;

    /** Don't write parent folders to manifest. */
    private boolean m_skipParentFolders;

    /** If set, the manifest.xml file will be generated with dtd info. */
    private boolean m_xmlValidation;

    /** Resources for with meta data should be exported, even if not in the resources to export.
     * That are super-folders of exported resources, where meta data should be kept in the export.
     */
    private List<String> m_additionalResourcesToExportWithMetaData;

    /**
     * Constructor.<p>
     */
    public CmsExportParameters() {

        // empty constructor for the database export dialog
    }

    /**
     * Constructor.<p>
     *
     * @param exportFile the zip file to export to
     * @param moduleElement module informations in a Node for module export
     * @param exportResourceData if the resource data has also to be exported
     * @param exportUserdata if the account data has also to be exported
     * @param exportProjectData if the project data has also to be exported
     * @param resourcesToExport the paths of folders and files to export
     * @param includeSystem if <code>true</code>, the system folder is included
     * @param includeUnchanged <code>true</code>, if unchanged files should be included
     * @param contentAge export contents changed after this date/time
     * @param recursive recursive flag
     * @param inProject if only resources in the current project are exported
     * @param exportMode the export mode to use
     */
    public CmsExportParameters(
        String exportFile,
        Element moduleElement,
        boolean exportResourceData,
        boolean exportUserdata,
        boolean exportProjectData,
        List<String> resourcesToExport,
        boolean includeSystem,
        boolean includeUnchanged,
        long contentAge,
        boolean recursive,
        boolean inProject,
        ExportMode exportMode) {

        setPath(exportFile);
        setResources(resourcesToExport);
        setIncludeSystemFolder(includeSystem);
        setIncludeUnchangedResources(includeUnchanged);
        setModuleInfo(moduleElement);
        setExportAccountData(exportUserdata);
        setContentAge(contentAge);
        setRecursive(recursive);
        setExportResourceData(exportResourceData);
        setExportProjectData(exportProjectData);
        setInProject(inProject);
        setExportAsFiles(false);
        setExportMode(exportMode);
        setAdditionalResourcesToExportWithMetaData(null);
    }

    /**
     * Constructor.<p>
     *
     * @param exportFile the zip file to export to
     * @param moduleElement module informations in a Node for module export
     * @param exportResourceData if the resource data has also to be exported
     * @param exportUserdata if the account data has also to be exported
     * @param exportProjectData if the project data has also to be exported
     * @param resourcesToExport the paths of folders and files to export
     * @param includeSystem if <code>true</code>, the system folder is included
     * @param includeUnchanged <code>true</code>, if unchanged files should be included
     * @param contentAge export contents changed after this date/time
     * @param recursive recursive flag
     * @param inProject if only resources in the current project are exported
     * @param exportMode the export mode to use
     * @param additionalResourcesToExportWithMetaData the list of export-site relative paths of folders/files for
     *      which meta data should be exported, even if they do not belong the the resourcesToExport.
     */
    public CmsExportParameters(
        String exportFile,
        Element moduleElement,
        boolean exportResourceData,
        boolean exportUserdata,
        boolean exportProjectData,
        List<String> resourcesToExport,
        boolean includeSystem,
        boolean includeUnchanged,
        long contentAge,
        boolean recursive,
        boolean inProject,
        ExportMode exportMode,
        List<String> additionalResourcesToExportWithMetaData) {

        setPath(exportFile);
        setResources(resourcesToExport);
        setIncludeSystemFolder(includeSystem);
        setIncludeUnchangedResources(includeUnchanged);
        setModuleInfo(moduleElement);
        setExportAccountData(exportUserdata);
        setContentAge(contentAge);
        setRecursive(recursive);
        setExportResourceData(exportResourceData);
        setExportProjectData(exportProjectData);
        setInProject(inProject);
        setExportAsFiles(false);
        setExportMode(exportMode);
        setAdditionalResourcesToExportWithMetaData(additionalResourcesToExportWithMetaData);
    }

    /**
     * Adds the resources from the additional resource list to the actual export resources.
     */
    public void addAdditionalResources() {

        if (m_additionalResourceList != null) {
            Set<String> resources = new HashSet<>();
            resources.addAll(getResources());
            for (String line : m_additionalResourceList.split("\n")) {
                line = line.trim();
                if (line.length() > 0) {
                    resources.add(line);
                }
            }
            setResources(new ArrayList<>(resources));
            m_additionalResourceList = null;
        }
    }

    /**
     * Gets the additional resource list.
     *
     * @return the additional resource list
     */
    public String getAdditionalResourceList() {

        return m_additionalResourceList;
    }

    /**
     * Returns the content Age.<p>
     *
     * @return the content Age
     */
    public long getContentAge() {

        return m_contentAge;
    }

    /**
     * Returns the export mode that should be used.
     *
     * @return the export mode that should be used.
     */
    public ExportMode getExportMode() {

        return m_exportMode;
    }

    /**
     * Returns the module informations if to export a module.<p>
     *
     * @return the module informations if to export a module
     */
    public Element getModuleInfo() {

        return m_moduleInfo;
    }

    /**
     * Returns the file path, should be a zip file.<p>
     *
     * @return the file path
     */
    public String getPath() {

        // ensure the export file name ends with ".zip" in case of ZIP file export
        if ((m_path != null) && !isExportAsFiles() && !m_path.toLowerCase().endsWith(".zip")) {
            m_path += ".zip";
        }
        return m_path;
    }

    /**
     * Returns the resources.<p>
     *
     * @return the resources
     */
    public List<String> getResources() {

        if (m_resources == null) {
            return Collections.emptyList();
        }
        return m_resources;
    }

    /**
     * Returns the resources to export with (some additional) meta-data, even for reduced meta-data export.
     * @return the resources to export with (some additional) meta-data, even for reduced meta-data export.
     */
    public List<String> getResourcesToExportWithMetaData() {

        return null != m_additionalResourcesToExportWithMetaData
        ? m_additionalResourcesToExportWithMetaData
        : Collections.emptyList();

    }

    /**
     * Checks if to export account data.<p>
     *
     * @return <code>true</code>, if to export account data
     */
    public boolean isExportAccountData() {

        return m_exportAccountData;
    }

    /**
     * Indicates if the resources are exported in one export .ZIP file (the default) or as individual files.<p>
     *
     * @return <code>false</code> if the resources will be exported in a .ZIP file,
     *      <code>true</code> if the resources will be exported as individual files
     */
    public boolean isExportAsFiles() {

        return m_exportAsFiles;
    }

    /**
     * Checks if to export project data.<p>
     *
     * @return <code>true</code>, if to export project data
     */
    public boolean isExportProjectData() {

        return m_exportProjectData;
    }

    /**
     * Checks if to export resource data.<p>
     *
     * @return <code>true</code>, if to export resource data
     */
    public boolean isExportResourceData() {

        return m_exportResourceData;
    }

    /**
     * Checks if to include the /system/ Folder.<p>
     *
     * @return <code>true</code>, if to include the /system/ Folder
     */
    public boolean isIncludeSystemFolder() {

        return m_includeSystemFolder;
    }

    /**
     * Checks if to include unchanged resources.<p>
     *
     * @return <code>true</code>, if to include unchanged resources
     */
    public boolean isIncludeUnchangedResources() {

        return m_includeUnchangedResources;
    }

    /**
     * Checks if to include only resources in the current project.<p>
     *
     * @return <code>true</code>, if to include only resources in the current project
     */
    public boolean isInProject() {

        return m_inProject;
    }

    /**
     * Checks if to recurse the resources to export.<p>
     *
     * @return <code>true</code>, if to recurse the resources to export
     */
    public boolean isRecursive() {

        return m_recursive;
    }

    /**
     * If true, parent folders are not written to the manifest.
     *
     * @return true if parent folders should be skipped
     */
    public boolean isSkipParentFolders() {

        return m_skipParentFolders;
    }

    /**
     * Checks if the manifest.xml file will be generated with dtd info.<p>
     *
     * @return the xml validation flag
     */
    public boolean isXmlValidation() {

        return m_xmlValidation;
    }

    /**
     * Sets the additional resources for the export.
     *
     * @param additionalResourceList the additional resource paths to export, separated by newlines
     */
    public void setAdditionalResourceList(String additionalResourceList) {

        m_additionalResourceList = additionalResourceList;

    }

    /**
     * Checks if the manifest.xml file will be generated with dtd info.<p>
     *
     * @param resourcesToExportWithMetaData the vfs paths of the resources.
     */
    public void setAdditionalResourcesToExportWithMetaData(List<String> resourcesToExportWithMetaData) {

        m_additionalResourcesToExportWithMetaData = resourcesToExportWithMetaData;

    }

    /**
     * Sets the content Age.<p>
     *
     * @param contentAge the content Age to set
     */
    public void setContentAge(long contentAge) {

        if (contentAge < 0) {
            String ageString = Long.toString(contentAge);
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_BAD_CONTENT_AGE_1, ageString));
        }
        m_contentAge = contentAge;
    }

    /**
     * Sets if to export account data.<p>
     *
     * @param exportAccountData the flag to set
     */
    public void setExportAccountData(boolean exportAccountData) {

        m_exportAccountData = exportAccountData;
    }

    /**
     * Controls if the resources are exported in one export .ZIP file (the default) or as individual files.<p>
     *
     * @param exportAsFiles if <code>false</code>, then the resources will be exported in a .ZIP file,
     *      otherwise as individual files
     */
    public void setExportAsFiles(boolean exportAsFiles) {

        m_exportAsFiles = exportAsFiles;
    }

    /**
     * Sets the export mode.
     *
     * @param exportMode the export mode to set
     */
    public void setExportMode(ExportMode exportMode) {

        m_exportMode = null != exportMode ? exportMode : ExportMode.DEFAULT;
    }

    /**
     * Sets if to export project data.<p>
     *
     * @param exportProjectData the flag to set
     */
    public void setExportProjectData(boolean exportProjectData) {

        m_exportProjectData = exportProjectData;
    }

    /**
     * Sets if to export resource data.<p>
     *
     * @param exportResourceData the flag to set
     */
    public void setExportResourceData(boolean exportResourceData) {

        m_exportResourceData = exportResourceData;
    }

    /**
     * Sets if to include the /system/ Folder.<p>
     *
     * @param includeSystemFolder the flag to set
     */
    public void setIncludeSystemFolder(boolean includeSystemFolder) {

        m_includeSystemFolder = includeSystemFolder;
    }

    /**
     * Sets if to include unchanged resources.<p>
     *
     * @param includeUnchangedResources the flag to set
     */
    public void setIncludeUnchangedResources(boolean includeUnchangedResources) {

        m_includeUnchangedResources = includeUnchangedResources;
    }

    /**
     * Sets if to only include files in the current project.<p>
     *
     * @param inProject the flag to set
     */
    public void setInProject(boolean inProject) {

        m_inProject = inProject;
    }

    /**
     * Sets the module informations if to export a module.<p>
     *
     * @param moduleInfo the module info node to set
     */
    public void setModuleInfo(Element moduleInfo) {

        m_moduleInfo = moduleInfo;
    }

    /**
     * Sets the file path, should be a zip file.<p>
     *
     * @param path the file path
     */
    public void setPath(String path) {

        if (CmsStringUtil.isEmpty(path) || !path.trim().equals(path)) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_BAD_FILE_NAME_1, path));
        }
        m_path = path;
    }

    /**
     * Sets the recursive flag.<p>
     *
     * @param recursive the flag to set
     */
    public void setRecursive(boolean recursive) {

        m_recursive = recursive;
    }

    /**
     * Sets the resources.<p>
     *
     * @param resources the resources to set
     */
    public void setResources(List<String> resources) {

        m_resources = resources;
    }

    /**
     * Enables / disables skipping of parent folders in the manifest.
     *
     * @param skipSuperFolders true if parent folders should not be written to the manifest
     */
    public void setSkipParentFolders(boolean skipSuperFolders) {

        m_skipParentFolders = skipSuperFolders;
    }

    /**
     * Sets the xml validation flag. If set, the manifest.xml file will be generated with dtd info.<p>
     *
     * @param xmlValidation the xml validation flag to set
     */
    public void setXmlValidation(boolean xmlValidation) {

        m_xmlValidation = xmlValidation;
    }
}
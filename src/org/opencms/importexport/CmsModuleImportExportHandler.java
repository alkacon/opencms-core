/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/Attic/CmsModuleImportExportHandler.java,v $
 * Date   : $Date: 2004/02/25 14:12:43 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRegistry;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Import/export handler implementation for Cms modules.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.2 $ $Date: 2004/02/25 14:12:43 $
 * @since 5.3
 */
public class CmsModuleImportExportHandler extends Object implements I_CmsImportExportHandler {

    /** The description of this import/export handler.<p> */
    private String m_description;

    /** The type of this import/export handler.<p> */
    private String m_type;

    /** The name of the export file in the real file system.<p> */
    private String m_fileName;

    /** The VFS resources to be exported additionally with the module.<p> */
    private List m_additionalResources;

    /** The (package) name of the module to be exported.<p> */
    private String m_moduleName;

    /** Module import/export handler type.<p> */
    public static final String C_TYPE_MODDATA = "moddata";

    /**
     * Creates a new Cms module import/export handler.<p>
     */
    public CmsModuleImportExportHandler() {
        super();
        m_type = CmsModuleImportExportHandler.C_TYPE_MODDATA;
        m_description = C_DEFAULT_DESCRIPTION;
    }

    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        try {
            if (m_additionalResources != null) {
                m_additionalResources.clear();
            }
            m_additionalResources = null;
        } catch (Exception e) {
            // noop
        } finally {
            super.finalize();
        }
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#getType()
     */
    public String getType() {
        return m_type;
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#exportData(org.opencms.file.CmsObject, org.opencms.report.I_CmsReport)
     */
    public void exportData(CmsObject cms, I_CmsReport report) throws CmsException {
        report.print(report.key("report.export_module_begin"), I_CmsReport.C_FORMAT_HEADLINE);
        report.println(" <i>" + getModuleName() + "</i>", I_CmsReport.C_FORMAT_HEADLINE);
        
        CmsRegistry registry = cms.getRegistry();
        registry.exportModule(getModuleName(), getAdditionalResources(), getFileName(), report);
        
        report.println(report.key("report.export_module_end"), I_CmsReport.C_FORMAT_HEADLINE);
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#importData(org.opencms.file.CmsObject, java.lang.String, java.lang.String, org.opencms.report.I_CmsReport)
     */
    public void importData(CmsObject cms, String importFile, String importPath, I_CmsReport report) throws CmsException {
        CmsProject importProject = null;
        CmsProject previousProject = null;
        String moduleZipName = null;
        String modulePackageName = null;
        int underscore = -1;
        Map moduleInfo = null;
        CmsRegistry registry = null;
        String moduleType = null;
        Vector conflictFiles = null;

        try {
            previousProject = cms.getRequestContext().currentProject();

            try {
                cms.getRequestContext().saveSiteRoot();
                cms.getRequestContext().setSiteRoot("/");

                // create a Project to import the module.
                importProject = cms.createProject("ImportModule", "A System generated project to import the module " + getModuleName(), OpenCms.getDefaultUsers().getGroupAdministrators(), OpenCms.getDefaultUsers().getGroupAdministrators(), I_CmsConstants.C_PROJECT_TYPE_TEMPORARY);
                cms.getRequestContext().setCurrentProject(importProject);

                // copy the root folder to the project
                cms.copyResourceToProject("/");
            } finally {
                cms.getRequestContext().restoreSiteRoot();
            }

            registry = cms.getRegistry();

            importFile = importFile.replace('\\', '/');
            moduleZipName = importFile.substring(importFile.lastIndexOf('/') + 1);
            
            if (moduleZipName.toLowerCase().endsWith(".zip")) {
                modulePackageName = moduleZipName.substring(0, moduleZipName.lastIndexOf('.'));
                if ((underscore = modulePackageName.indexOf('_')) > 0) {
                    modulePackageName = modulePackageName.substring(0, underscore);
                }
            } else {
                modulePackageName = moduleZipName;
            }

            moduleInfo = registry.importGetModuleInfo(importFile);
            moduleType = (String) moduleInfo.get("type");

            if (!CmsRegistry.C_MODULE_TYPE_SIMPLE.equals(moduleType)) {
                conflictFiles = registry.importGetConflictingFileNames(importFile);
                if (!conflictFiles.isEmpty()) {
                    throw new CmsException("Import of " + modulePackageName + " has conflicts with existing resources: " + conflictFiles.toString());
                }
            } else {
                // simple module, no "confict file" check performed
                conflictFiles = new Vector();
            }

            report.print(report.key("report.import_module_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            report.println(" <i>" + modulePackageName + "</i>", I_CmsReport.C_FORMAT_HEADLINE);

            registry.importModule(importFile, conflictFiles, report);

            report.println(report.key("report.publish_project_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            // now unlock and publish the project
            cms.unlockProject(importProject.getId());
            cms.publishProject(report);

            report.println(report.key("report.publish_project_end"), I_CmsReport.C_FORMAT_HEADLINE);
            report.println(report.key("report.import_module_end"), I_CmsReport.C_FORMAT_HEADLINE);
        } finally {
            cms.getRequestContext().setCurrentProject(previousProject);
        }
    }

    /**
     * Returns the name of the export file in the real file system.<p>
     * 
     * @return the name of the export file in the real file system
     */
    public String getFileName() {
        return m_fileName;
    }

    /**
     * Sets the name of the export file in the real file system.<p>
     * 
     * @param fileName the name of the export file in the real file system
     */
    public void setFileName(String fileName) {
        m_fileName = fileName;
    }

    /**
     * Returns the (package) name of the module to be exported.<p>
     * 
     * @return the (package) name of the module to be exported
     */
    public String getModuleName() {
        return m_moduleName;
    }

    /**
     * Returns the VFS resources to be exported additionally with the module.<p>
     * 
     * @return the VFS resources to be exported additionally with the module
     */
    public String[] getAdditionalResources() {
        return (String[]) m_additionalResources.toArray();
    }

    /**
     * Returns the VFS resources to exported additionally with the module as a list.<p>
     * 
     * @return the VFS resources to exported additionally with the module as a list
     */
    public List getResourcesAsList() {
        return m_additionalResources;
    }

    /**
     * Sets the (package) name of the module to be exported.<p>
     * 
     * @param moduleName the (package) name of the module to be exported
     */
    public void setModuleName(String moduleName) {
        m_moduleName = moduleName;
    }

    /**
     * Sets the VFS resources to be exported additionally with the module.<p>
     * 
     * @param resources the VFS resources to be exported additionally with the module
     */
    public void setAdditionalResources(String[] resources) {
        m_additionalResources = Arrays.asList(resources);
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#getDescription()
     */
    public String getDescription() {
        return m_description;
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#setDescription(java.lang.String)
     */
    public void setDescription(String description) {
        m_description = description;
    }

}

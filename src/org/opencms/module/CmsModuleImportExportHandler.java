/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/module/CmsModuleImportExportHandler.java,v $
 * Date   : $Date: 2005/02/17 12:44:35 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.module;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.importexport.CmsExport;
import org.opencms.importexport.CmsImport;
import org.opencms.importexport.I_CmsImportExportHandler;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsHtmlReport;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsSecurityException;
import org.opencms.workplace.I_CmsWpConstants;
import org.opencms.xml.CmsXmlErrorHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.digester.Digester;

import org.dom4j.Document;
import org.dom4j.Element;
import org.xml.sax.SAXException;

/**
 * Import/export handler implementation for Cms modules.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.7 $ $Date: 2005/02/17 12:44:35 $
 * @since 5.3
 */
public class CmsModuleImportExportHandler extends Object implements I_CmsImportExportHandler {

    /** The name of the module import project. */
    public static final String C_IMPORT_MODULE_PROJECT_NAME = "ImportModule";

    /** The VFS resources to be exported additionally with the module.<p> */
    private List m_additionalResources;

    /** The description of this import/export handler.<p> */
    private String m_description;

    /** The name of the export file in the real file system.<p> */
    private String m_fileName;
    
    /** The module imported with the digester. */
    private CmsModule m_importedModule;
    
    /** The (package) name of the module to be exported.<p> */
    private String m_moduleName;

    /**
     * Creates a new Cms module import/export handler.<p>
     */
    public CmsModuleImportExportHandler() {
        super();
        m_description = C_DEFAULT_DESCRIPTION;
    }

    /**
     * Reads a module object from an external file source.<p>
     * 
     * @param importResource the name of the input source
     * @return the imported module 
     * @throws CmsConfigurationException if the module could not be imported
     */
    public static CmsModule readModuleFromImport(String importResource) throws CmsConfigurationException {
        
        // instantiate Digester and enable XML validation
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.setRuleNamespaceURI(null);   
        digester.setErrorHandler(new CmsXmlErrorHandler());

        // add this class to the Digester
        CmsModuleImportExportHandler handler = new CmsModuleImportExportHandler();
        digester.push(handler);

        CmsModuleXmlHandler.addXmlDigesterRules(digester);       
        
        InputStream stream = null;
        ZipFile importZip = null;
        
        try {
            
            File file = new File(importResource);
            if (file.isFile()) {
                importZip = new ZipFile(importResource);
                ZipEntry entry = importZip.getEntry("manifest.xml");
                stream = importZip.getInputStream(entry);               
            } else if (file.isDirectory()) {
                file = new File(file, "manifest.xml");
                stream = new FileInputStream(file);
            }
            
            // start the parsing process        
            digester.parse(stream);    
            
        } catch (IOException e) {
            OpenCms.getLog(CmsModuleImportExportHandler.class).error("IO error reading module import", e);
            throw new CmsConfigurationException(CmsConfigurationException.C_CONFIGURATION_ERROR, e);
        } catch (SAXException e) {
            OpenCms.getLog(CmsModuleImportExportHandler.class).error("SAX error reading module import", e);
            throw new CmsConfigurationException(CmsConfigurationException.C_CONFIGURATION_ERROR, e);
        } finally {
            try {
                if (importZip != null) {
                    importZip.close();
                }                
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
                // noop
            }
        }     
        
        CmsModule importedModule = handler.getModule();
        
        // the digester must have set the module now
        if (importedModule == null) {
            throw new CmsConfigurationException("Could not import module from source '" + importResource + "' is already installed", CmsConfigurationException.C_CONFIGURATION_ERROR);                    
        }       

        return importedModule;
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#exportData(org.opencms.file.CmsObject, org.opencms.report.I_CmsReport)
     */
    public void exportData(CmsObject cms, I_CmsReport report) throws CmsException {
        
        report.print(report.key("report.export_module_begin"), I_CmsReport.C_FORMAT_HEADLINE);
        if (report instanceof CmsHtmlReport) {
            report.println(" <i>" + getModuleName() + "</i>", I_CmsReport.C_FORMAT_HEADLINE);
        } else {
            report.println(" " + getModuleName(), I_CmsReport.C_FORMAT_HEADLINE);
        }
                
        // check if the user has the required permissions
        if ((cms == null) || !cms.isAdmin()) {
            throw new CmsSecurityException(CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
        
        if (! OpenCms.getModuleManager().hasModule(getModuleName())) {
            // module not available
            throw new CmsConfigurationException("No module '" + getModuleName() + "' available for export", CmsConfigurationException.C_CONFIGURATION_ERROR);
        }
        
        // generate module XML
        CmsModule module = OpenCms.getModuleManager().getModule(getModuleName());
        if (! module.getVersion().isUpdated()) {
            // increment version number if not recently updated
            module.getVersion().increment();
        }
        // reset update status so that all following exports auto-increment the number
        module.getVersion().setUpdated(false);
        Element moduleElement = CmsModuleXmlHandler.generateXml(module);

        // export the module using the standard export        
        new CmsExport(cms, getFileName(), getAdditionalResources(), false, false, moduleElement, false, 0, report);
        
        report.println(report.key("report.export_module_end"), I_CmsReport.C_FORMAT_HEADLINE);
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
     * @see org.opencms.importexport.I_CmsImportExportHandler#getDescription()
     */
    public String getDescription() {
        return m_description;
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
     * Returns the (package) name of the module to be exported.<p>
     * 
     * @return the (package) name of the module to be exported
     */
    public String getModuleName() {
        return m_moduleName;
    }

    /**
     * Returns the VFS resources to be exported additionally with the module as a list.<p>
     * 
     * @return the VFS resources to be exported additionally with the module as a list
     */
    public List getResourcesAsList() {
        return m_additionalResources;
    }
    
    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#importData(org.opencms.file.CmsObject, java.lang.String, java.lang.String, org.opencms.report.I_CmsReport)
     */
    public synchronized void importData(CmsObject cms, String importFile, String importPath, I_CmsReport report) 
    throws CmsException {
        
        CmsProject previousProject = cms.getRequestContext().currentProject();
        try {
            
            CmsProject importProject = null;
            
            try {
                cms.getRequestContext().saveSiteRoot();
                cms.getRequestContext().setSiteRoot("/");

                try {
                    // try to read a (leftover) module import project
                    importProject = cms.readProject(C_IMPORT_MODULE_PROJECT_NAME);
                } catch (CmsException e) {
                    // create a Project to import the module
                    importProject = cms.createProject(C_IMPORT_MODULE_PROJECT_NAME, "A System generated project to import the module " + getModuleName(), OpenCms.getDefaultUsers().getGroupAdministrators(), OpenCms.getDefaultUsers().getGroupAdministrators(), I_CmsConstants.C_PROJECT_TYPE_TEMPORARY);
                }
                
                cms.getRequestContext().setCurrentProject(importProject);

                // copy the root folder to the project
                cms.copyResourceToProject("/");
            } finally {
                cms.getRequestContext().restoreSiteRoot();
            }

            importFile = importFile.replace('\\', '/');
            String moduleZipName = importFile.substring(importFile.lastIndexOf('/') + 1);
            String modulePackageName;
            
            if (moduleZipName.toLowerCase().endsWith(".zip")) {
                modulePackageName = moduleZipName.substring(0, moduleZipName.lastIndexOf('.'));
                int pos = modulePackageName.lastIndexOf('_');
                if (pos > 0) {
                    modulePackageName = modulePackageName.substring(0, pos);
                }
            } else {
                modulePackageName = moduleZipName;
            }
            
            report.print(report.key("report.import_module_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            if (report instanceof CmsHtmlReport) {
                report.println(" <i>" + modulePackageName + "</i>", I_CmsReport.C_FORMAT_HEADLINE);
            } else {
                report.println(" " + modulePackageName, I_CmsReport.C_FORMAT_HEADLINE);
            }

            importModule(cms, importFile, report);

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
     * @see org.opencms.importexport.I_CmsImportExportHandler#matches(org.dom4j.Document)
     */
    public boolean matches(Document manifest) {
        Element rootElement = manifest.getRootElement();
        
        boolean hasModuleNode = (rootElement.selectNodes("./module/name").size() > 0);        
        return (hasModuleNode);
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
     * @see org.opencms.importexport.I_CmsImportExportHandler#setDescription(java.lang.String)
     */
    public void setDescription(String description) {
        m_description = description;
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
     * Will be called by the digester if a module was imported.<p>
     * 
     * @param moduleHandler contains the imported module
     */
    public void setModule(CmsModuleXmlHandler moduleHandler) {

        m_importedModule = moduleHandler.getModule();
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
     * Returns the module imported with the digester.<p>
     * 
     * @return the module imported with the digester
     */
    private CmsModule getModule() {
        
        return m_importedModule;
    }
    
    /**
     * Imports a module from a external file source.<p>
     * 
     * @param cms must have been initialized with "Admin" permissions 
     * @param importResource the name of the input source
     * @param report the report to print the progess information to
     * @throws CmsSecurityException if no "Admin" permissions are available
     * @throws CmsConfigurationException if the module is already installed or the 
     *      dependencies are not fulfilled
     * @throws CmsException if errors occur reading the module data
     */
    private synchronized void importModule(CmsObject cms, String importResource, I_CmsReport report) 
    throws CmsSecurityException, CmsConfigurationException, CmsException {
        
        // check if the user has the required permissions
        if ((cms == null) || !cms.isAdmin()) {
            throw new CmsSecurityException(CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
        
        // read the module from the import file
        CmsModule importedModule = readModuleFromImport(importResource);
   
        
        // check if the module is already istalled
        if (OpenCms.getModuleManager().hasModule(importedModule.getName())) {
            throw new CmsConfigurationException("The module '" + importedModule.getName() + "' is already installed", CmsConfigurationException.C_CONFIGURATION_ERROR);        
        }

        // check the module dependencies
        List dependencies = OpenCms.getModuleManager().checkDependencies(importedModule, CmsModuleManager.C_DEPENDENCY_MODE_IMPORT);
        if (dependencies.size() > 0) {
            // some dependencies not fulfilled
            String missingModules = "";
            Iterator it = dependencies.iterator();
            while (it.hasNext()) {
                CmsModuleDependency dependency = (CmsModuleDependency)it.next();
                missingModules += "Module: " + dependency.getName() + " Version: " + dependency.getVersion() + "\n";                
            }
            throw new CmsConfigurationException("The following dependencies for the module are not fulfilled:\n" + missingModules, CmsConfigurationException.C_CONFIGURATION_MODULE_DEPENDENCIES);
        }

        //  add the imported module to the module manager
        OpenCms.getModuleManager().addModule(cms, importedModule);
        
        // reinitialize the resource manager with additional module resourcetypes if nescessary
        if (importedModule.getResourceTypes() != Collections.EMPTY_LIST) {
            OpenCms.getResourceManager().initialize(cms);
        }    
        // reinitialize the workplace manager with addititonal module explorertypes if nescessary
        if (importedModule.getExplorerTypes() != Collections.EMPTY_LIST) {
            OpenCms.getWorkplaceManager().addExplorerTypeSettings(cms, importedModule.getExplorerTypes());
        }   
        
        Vector exclusion = new Vector();
        // add all default directories to the exclusion list
        // this prevents modification of these directories
        exclusion.add(I_CmsWpConstants.C_VFS_PATH_SYSTEM);
        exclusion.add(I_CmsWpConstants.C_VFS_PATH_MODULES);
        exclusion.add(I_CmsWpConstants.C_VFS_PATH_WORKPLACE);
        exclusion.add(I_CmsWpConstants.C_VFS_PATH_GALLERIES);
        exclusion.add(I_CmsWpConstants.C_VFS_PATH_HELP);
        exclusion.add(I_CmsWpConstants.C_VFS_PATH_LOCALES);
        exclusion.add(I_CmsWpConstants.C_VFS_PATH_SCRIPTS);
        exclusion.add(I_CmsWpConstants.C_VFS_PATH_SYSTEMPICS);
        exclusion.add(I_CmsWpConstants.C_VFS_GALLERY_PICS);
        exclusion.add(I_CmsWpConstants.C_VFS_GALLERY_HTML);
        exclusion.add(I_CmsWpConstants.C_VFS_GALLERY_DOWNLOAD);
        exclusion.add(I_CmsWpConstants.C_VFS_GALLERY_EXTERNALLINKS);

        // import the module resources
        CmsImport cmsImport = new CmsImport(cms, importResource, "/", report);
        cmsImport.importResources(exclusion, new Vector(), new Vector(), null, null);               
    }
    
}

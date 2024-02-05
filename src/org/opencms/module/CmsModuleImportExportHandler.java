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

package org.opencms.module;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.importexport.CmsExport;
import org.opencms.importexport.CmsExportParameters;
import org.opencms.importexport.CmsImport;
import org.opencms.importexport.CmsImportExportException;
import org.opencms.importexport.CmsImportExportManager;
import org.opencms.importexport.CmsImportHelper;
import org.opencms.importexport.CmsImportParameters;
import org.opencms.importexport.I_CmsImportExportHandler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsShell;
import org.opencms.main.CmsSystemInfo;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModuleXmlHandler.XmlWriteMode;
import org.opencms.report.CmsHtmlReport;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlErrorHandler;
import org.opencms.xml.CmsXmlException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.Rule;
import org.apache.commons.logging.Log;

import org.dom4j.Document;
import org.dom4j.Element;
import org.xml.sax.SAXException;

/**
 * Import/export handler implementation for Cms modules.<p>
 *
 * @since 6.0.0
 */
public class CmsModuleImportExportHandler implements I_CmsImportExportHandler {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsModuleImportExportHandler.class);

    /** The VFS resources to be exported additionally with the module.<p> */
    private List<String> m_additionalResources;

    /** The description of this import/export handler.<p> */
    private String m_description;

    /** The name of the export file in the real file system.<p> */
    private String m_fileName;

    /** The module imported with the digester. */
    private CmsModule m_importedModule;

    /** The import parameters. */
    private CmsImportParameters m_importParams;

    /** The (package) name of the module to be exported.<p> */
    private String m_moduleName;

    /**
     * Creates a new Cms module import/export handler.<p>
     */
    public CmsModuleImportExportHandler() {

        super();
        m_description = org.opencms.importexport.Messages.get().getBundle().key(
            org.opencms.importexport.Messages.GUI_CMSIMPORTHANDLER_DEFAULT_DESC_0);
    }

    /**
     * Gets the module export handler containing all resources used in the module export.<p>
     * @param cms the {@link CmsObject} used by to set up the handler. The object's site root might be adjusted to the import site of the module.
     * @param module The module to export
     * @param handlerDescription A description of the export handler, shown when the export thread using the handler runs.
     * @return CmsModuleImportExportHandler with all module resources
     */
    public static CmsModuleImportExportHandler getExportHandler(
        CmsObject cms,
        final CmsModule module,
        final String handlerDescription) {

        // check if all resources are valid
        List<String> resListCopy = new ArrayList<String>();

        String moduleName = module.getName();

        try {
            cms = OpenCms.initCmsObject(cms);
            String importSite = module.getSite();
            if (!CmsStringUtil.isEmptyOrWhitespaceOnly(importSite)) {
                cms.getRequestContext().setSiteRoot(importSite);
            }
        } catch (CmsException e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
        }
        try {
            resListCopy = CmsModule.calculateModuleResourceNames(cms, module);
        } catch (CmsException e) {
            // some resource did not exist / could not be read
            if (LOG.isInfoEnabled()) {
                LOG.warn(Messages.get().getBundle().key(Messages.ERR_READ_MODULE_RESOURCES_1, module.getName()), e);
            }
        }
        resListCopy = CmsFileUtil.removeRedundancies(resListCopy);
        String[] resources = new String[resListCopy.size()];

        for (int i = 0; i < resListCopy.size(); i++) {
            resources[i] = resListCopy.get(i);
        }

        String filename = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            OpenCms.getSystemInfo().getPackagesRfsPath()
                + CmsSystemInfo.FOLDER_MODULES
                + moduleName
                + "_"
                + "%(version)");

        CmsModuleImportExportHandler moduleExportHandler = new CmsModuleImportExportHandler();
        moduleExportHandler.setFileName(filename);
        moduleExportHandler.setModuleName(moduleName.replace('\\', '/'));
        moduleExportHandler.setAdditionalResources(resources);
        moduleExportHandler.setDescription(handlerDescription);

        return moduleExportHandler;
    }

    /**
     * Reads a module object from an external file source.<p>
     *
     * @param importResource the name of the input source
     *
     * @return the imported module
     *
     * @throws CmsConfigurationException if the module could not be imported
     */
    public static CmsModule readModuleFromImport(String importResource) throws CmsConfigurationException {

        // instantiate Digester and enable XML validation
        Digester digester = new Digester();
        digester.setUseContextClassLoader(true);
        digester.setValidating(false);
        digester.setRuleNamespaceURI(null);
        digester.setErrorHandler(new CmsXmlErrorHandler(importResource));

        // add this class to the Digester
        CmsModuleImportExportHandler handler = new CmsModuleImportExportHandler();
        final String[] version = new String[] {null};
        digester.push(handler);

        digester.addRule("*/export_version", new Rule() {

            @Override
            public void body(String namespace, String name, String text) throws Exception {

                version[0] = text.trim();
            }

        });
        CmsModuleXmlHandler.addXmlDigesterRules(digester);

        InputStream stream = null;
        ZipFile importZip = null;

        try {
            File file = new File(importResource);
            if (!file.exists()) {
                throw new IOException("readModuleFromImport: Path '" + importResource + "' does not exist.");
            }
            if (file.isFile()) {
                importZip = new ZipFile(importResource);
                ZipEntry entry = importZip.getEntry(CmsImportExportManager.EXPORT_MANIFEST);
                if (entry != null) {
                    stream = importZip.getInputStream(entry);
                } else {
                    CmsMessageContainer message = Messages.get().container(
                        Messages.ERR_NO_MANIFEST_MODULE_IMPORT_1,
                        importResource);
                    LOG.error(message.key());
                    throw new CmsConfigurationException(message);
                }
            } else if (file.isDirectory()) {
                file = new File(file, CmsImportExportManager.EXPORT_MANIFEST);
                stream = new FileInputStream(file);
            }

            // start the parsing process
            digester.parse(stream);
        } catch (IOException e) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_IO_MODULE_IMPORT_1, importResource);
            LOG.error(message.key(), e);
            throw new CmsConfigurationException(message, e);
        } catch (SAXException e) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_SAX_MODULE_IMPORT_1, importResource);
            LOG.error(message.key(), e);
            throw new CmsConfigurationException(message, e);
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
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_IMPORT_MOD_ALREADY_INSTALLED_1, importResource));
        } else {
            importedModule.setExportVersion(version[0]);
        }

        return importedModule;
    }

    /**
     * Reads a module object from an external file source.<p>
     *
     * @param manifest the manifest data
     *
     * @return the imported module
     *
     * @throws CmsConfigurationException if the module could not be imported
     */
    public static CmsModule readModuleFromManifest(byte[] manifest) throws CmsConfigurationException {

        // instantiate Digester and enable XML validation
        Digester digester = new Digester();
        digester.setUseContextClassLoader(true);
        digester.setValidating(false);
        digester.setRuleNamespaceURI(null);
        digester.setErrorHandler(new CmsXmlErrorHandler("manifest data"));

        // add this class to the Digester
        CmsModuleImportExportHandler handler = new CmsModuleImportExportHandler();
        final String[] version = new String[] {null};
        digester.push(handler);

        digester.addRule("*/export_version", new Rule() {

            @Override
            public void body(String namespace, String name, String text) throws Exception {

                version[0] = text.trim();
            }

        });
        CmsModuleXmlHandler.addXmlDigesterRules(digester);

        InputStream stream = new ByteArrayInputStream(manifest);

        try {
            digester.parse(stream);
        } catch (IOException e) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_IO_MODULE_IMPORT_1, "manifest data");
            LOG.error(message.key(), e);
            throw new CmsConfigurationException(message, e);
        } catch (SAXException e) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_SAX_MODULE_IMPORT_1, "manifest data");
            LOG.error(message.key(), e);
            throw new CmsConfigurationException(message, e);
        }
        CmsModule importedModule = handler.getModule();
        // the digester must have set the module now
        if (importedModule == null) {
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_IMPORT_MOD_ALREADY_INSTALLED_1, "manifest data"));
        } else {
            importedModule.setExportVersion(version[0]);
        }

        return importedModule;
    }

    /**
     * Writes the messages for starting an import to the given report.<p>
     *
     * @param report the report to write to
     * @param modulePackageName the module name
     */
    public static void reportBeginImport(I_CmsReport report, String modulePackageName) {

        report.print(Messages.get().container(Messages.RPT_IMPORT_MODULE_BEGIN_0), I_CmsReport.FORMAT_HEADLINE);
        if (report instanceof CmsHtmlReport) {
            report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    "<i>" + modulePackageName + "</i>"));
        } else {
            report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    modulePackageName));
        }
        report.println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
    }

    /**
     * Writes the messages for finishing an import to the given report.<p>
     *
     * @param report the report to write to
     */
    public static void reportEndImport(I_CmsReport report) {

        report.println(Messages.get().container(Messages.RPT_IMPORT_MODULE_END_0), I_CmsReport.FORMAT_HEADLINE);
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#exportData(org.opencms.file.CmsObject, org.opencms.report.I_CmsReport)
     */
    public void exportData(CmsObject cms, I_CmsReport report)
    throws CmsConfigurationException, CmsImportExportException, CmsRoleViolationException {

        // check if the user has the required permissions
        OpenCms.getRoleManager().checkRole(cms, CmsRole.DATABASE_MANAGER);

        report.print(Messages.get().container(Messages.RPT_EXPORT_MODULE_BEGIN_0), I_CmsReport.FORMAT_HEADLINE);
        if (report instanceof CmsHtmlReport) {
            report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    "<i>" + getModuleName() + "</i>"));

        } else {
            report.print(
                org.opencms.report.Messages.get().container(
                    org.opencms.report.Messages.RPT_ARGUMENT_1,
                    getModuleName()));
        }
        report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));

        if (!OpenCms.getModuleManager().hasModule(getModuleName())) {
            // module not available
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_NO_MOD_FOR_EXPORT_1, getModuleName()));
        }

        // generate module XML
        CmsModule module = OpenCms.getModuleManager().getModule(getModuleName());
        boolean shouldIncrementVersion;
        try {
            shouldIncrementVersion = module.isAutoIncrement()
                && (module.getVersion().isUpdated() || module.shouldIncrementVersionBasedOnResources(cms));
        } catch (CmsException e) {
            shouldIncrementVersion = false;
            LOG.error(e.getLocalizedMessage(), e);
        }
        module.getVersion().setUpdated(false);
        if (shouldIncrementVersion) {
            module.getVersion().increment();
            module.setCheckpointTime(System.currentTimeMillis());
            OpenCms.getModuleManager().updateModuleConfiguration();
        }

        Element moduleElement = CmsModuleXmlHandler.generateXml(module, XmlWriteMode.manifest);

        CmsExportParameters params = new CmsExportParameters(
            getFileName(),
            moduleElement,
            true,
            false,
            false,
            getAdditionalResources(),
            true,
            true,
            0,
            true,
            false,
            module.getExportMode(),
            // provide the extra resources only in case of excluded resources, otherwise not needed
            ((null == module.getExcludeResources()) || module.getExcludeResources().isEmpty())
            ? null
            : module.getResources());

        // export the module using the standard export
        CmsObject exportCms = cms;
        String importSite = module.getSite();
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(importSite)) {
            try {
                exportCms = OpenCms.initCmsObject(exportCms);
                exportCms.getRequestContext().setSiteRoot(importSite);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        new CmsExport(exportCms, report).exportData(params);
        report.println(Messages.get().container(Messages.RPT_EXPORT_MODULE_END_0), I_CmsReport.FORMAT_HEADLINE);
    }

    /**
     * Returns the VFS resources to be exported additionally with the module.<p>
     *
     * @return the VFS resources to be exported additionally with the module
     */
    public List<String> getAdditionalResources() {

        return m_additionalResources;
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

        CmsMacroResolver resolver = new CmsMacroResolver();
        resolver.addMacro("version", OpenCms.getModuleManager().getModule(m_moduleName).getVersionStr());
        return resolver.resolveMacros(m_fileName);
    }

    /**
     * Returns the import parameters.<p>
     *
     * @return the import parameters
     */
    public CmsImportParameters getImportParameters() {

        return m_importParams;
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
    public List<String> getResourcesAsList() {

        return m_additionalResources;
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#importData(CmsObject, I_CmsReport)
     */
    public synchronized void importData(CmsObject cms, I_CmsReport report)
    throws CmsXmlException, CmsImportExportException, CmsRoleViolationException, CmsException {

        CmsImportParameters parameters = getImportParameters();
        CmsProject previousProject = cms.getRequestContext().getCurrentProject();
        try {
            CmsProject importProject = null;
            String modulePackageName = null;
            String storedSiteRoot = cms.getRequestContext().getSiteRoot();
            CmsImportHelper helper = new CmsImportHelper(parameters);
            try {
                cms.getRequestContext().setSiteRoot("/");
                helper.openFile();
                modulePackageName = helper.getFileName();

                try {
                    // try to read a (leftover) module import project
                    importProject = cms.readProject(
                        Messages.get().getBundle(cms.getRequestContext().getLocale()).key(
                            Messages.GUI_IMPORT_MODULE_PROJECT_NAME_1,
                            new Object[] {modulePackageName}));
                } catch (CmsException e) {
                    // create a Project to import the module
                    importProject = cms.createProject(
                        Messages.get().getBundle(cms.getRequestContext().getLocale()).key(
                            Messages.GUI_IMPORT_MODULE_PROJECT_NAME_1,
                            new Object[] {modulePackageName}),
                        Messages.get().getBundle(cms.getRequestContext().getLocale()).key(
                            Messages.GUI_IMPORT_MODULE_PROJECT_DESC_1,
                            new Object[] {modulePackageName}),
                        OpenCms.getDefaultUsers().getGroupAdministrators(),
                        OpenCms.getDefaultUsers().getGroupAdministrators(),
                        CmsProject.PROJECT_TYPE_TEMPORARY);
                }

                cms.getRequestContext().setCurrentProject(importProject);

                // copy the root folder to the project
                cms.copyResourceToProject("/");
            } catch (Exception e) {
                throw new CmsImportExportException(
                    Messages.get().container(Messages.ERR_IO_MODULE_IMPORT_1, parameters.getPath()),
                    e);
            } finally {
                helper.closeFile();
                cms.getRequestContext().setSiteRoot(storedSiteRoot);
            }

            reportBeginImport(report, modulePackageName);
            importModule(cms, report, parameters);
            report.println(Messages.get().container(Messages.RPT_PUBLISH_PROJECT_BEGIN_0), I_CmsReport.FORMAT_HEADLINE);
            // now unlock and publish the project
            cms.unlockProject(importProject.getUuid());
            OpenCms.getPublishManager().publishProject(cms, report);
            OpenCms.getPublishManager().waitWhileRunning();

            report.println(Messages.get().container(Messages.RPT_PUBLISH_PROJECT_END_0), I_CmsReport.FORMAT_HEADLINE);
            reportEndImport(report);
        } finally {
            cms.getRequestContext().setCurrentProject(previousProject);
        }
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#importData(org.opencms.file.CmsObject, java.lang.String, java.lang.String, org.opencms.report.I_CmsReport)
     *
     * @deprecated use {@link #importData(CmsObject, I_CmsReport)} instead
     */
    @Deprecated
    public void importData(CmsObject cms, String importFile, String importPath, I_CmsReport report)
    throws CmsXmlException, CmsImportExportException, CmsRoleViolationException, CmsException {

        CmsImportParameters parameters = new CmsImportParameters(importFile, importPath, true);
        setImportParameters(parameters);

        importData(cms, report);
    }

    /**
     * @see org.opencms.importexport.I_CmsImportExportHandler#matches(org.dom4j.Document)
     */
    public boolean matches(Document manifest) {

        Element rootElement = manifest.getRootElement();

        return (rootElement.selectNodes("./module/name").size() > 0);
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
     * Sets the import parameters.<p>
     *
     * @param importParams the parameters to set
     */
    public void setImportParameters(CmsImportParameters importParams) {

        m_importParams = importParams;
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
     * Returns the module imported with the digester.<p>
     *
     * @return the module imported with the digester
     */
    private CmsModule getModule() {

        return m_importedModule;
    }

    /**
     * Imports a module from an external file source.<p>
     *
     * @param cms must have been initialized with {@link CmsRole#DATABASE_MANAGER} permissions
     * @param report the report to print the progress information to
     * @param parameters the import parameters
     *
     * @return the imported module
     *
     * @throws CmsSecurityException if no {@link CmsRole#DATABASE_MANAGER} permissions are available
     * @throws CmsConfigurationException if the module is already installed or the
     *      dependencies are not fulfilled
     * @throws CmsException if errors occur reading the module data
     */
    private synchronized CmsModule importModule(CmsObject cms, I_CmsReport report, CmsImportParameters parameters)
    throws CmsSecurityException, CmsConfigurationException, CmsException {

        // check if the user has the required permissions
        OpenCms.getRoleManager().checkRole(cms, CmsRole.DATABASE_MANAGER);

        // read the module from the import file
        CmsModule importedModule = readModuleFromImport(parameters.getPath());

        // check if the module is already installed
        if (OpenCms.getModuleManager().hasModule(importedModule.getName())) {
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_MOD_ALREADY_INSTALLED_1, importedModule.getName()));
        }

        // check the module dependencies
        List<CmsModuleDependency> dependencies = OpenCms.getModuleManager().checkDependencies(
            importedModule,
            CmsModuleManager.DEPENDENCY_MODE_IMPORT);
        if (dependencies.size() > 0) {
            // some dependencies not fulfilled
            StringBuffer missingModules = new StringBuffer();
            for (CmsModuleDependency dependency : dependencies) {
                missingModules.append("  ").append(dependency.getName()).append(", Version ").append(
                    dependency.getVersion()).append("\r\n");
            }
            throw new CmsConfigurationException(
                Messages.get().container(
                    Messages.ERR_MOD_DEPENDENCY_INFO_2,
                    importedModule.getName() + ", Version " + importedModule.getVersion(),
                    missingModules));
        }

        // check the imported resource types for name / id conflicts
        List<I_CmsResourceType> checkedTypes = new ArrayList<I_CmsResourceType>();
        for (I_CmsResourceType type : importedModule.getResourceTypes()) {
            // first check against the already configured resource types
            int externalConflictIndex = OpenCms.getResourceManager().getResourceTypes().indexOf(type);
            if (externalConflictIndex >= 0) {
                I_CmsResourceType conflictingType = OpenCms.getResourceManager().getResourceTypes().get(
                    externalConflictIndex);
                if (!type.isIdentical(conflictingType)) {
                    // if name and id are identical, we assume this is a module replace operation
                    throw new CmsConfigurationException(
                        org.opencms.loader.Messages.get().container(
                            org.opencms.loader.Messages.ERR_CONFLICTING_MODULE_RESOURCE_TYPES_5,
                            new Object[] {
                                type.getTypeName(),
                                Integer.valueOf(type.getTypeId()),
                                importedModule.getName(),
                                conflictingType.getTypeName(),
                                Integer.valueOf(conflictingType.getTypeId())}));
                }
            }
            // now check against the other resource types of the imported module
            int internalConflictIndex = checkedTypes.indexOf(type);
            if (internalConflictIndex >= 0) {
                I_CmsResourceType conflictingType = checkedTypes.get(internalConflictIndex);
                throw new CmsConfigurationException(
                    org.opencms.loader.Messages.get().container(
                        org.opencms.loader.Messages.ERR_CONFLICTING_RESTYPES_IN_MODULE_5,
                        new Object[] {
                            importedModule.getName(),
                            type.getTypeName(),
                            Integer.valueOf(type.getTypeId()),
                            conflictingType.getTypeName(),
                            Integer.valueOf(conflictingType.getTypeId())}));
            }
            // add the resource type for the next check
            checkedTypes.add(type);
        }

        // import the module resources
        CmsObject importCms = OpenCms.initCmsObject(cms);
        String importSite = importedModule.getImportSite();
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(importSite)) {
            importCms.getRequestContext().setSiteRoot(importSite);
        } else {
            String siteToSet = importCms.getRequestContext().getSiteRoot();
            if ("".equals(siteToSet)) {
                siteToSet = "/";
            }
            importedModule.setSite(siteToSet);
        }

        //  add the imported module to the module manager
        OpenCms.getModuleManager().addModule(cms, importedModule);

        // reinitialize the resource manager with additional module resource types if necessary
        if (importedModule.getResourceTypes() != Collections.EMPTY_LIST) {
            OpenCms.getResourceManager().initialize(cms);
        }
        // reinitialize the workplace manager with additional module explorer types if necessary
        if (importedModule.getExplorerTypes() != Collections.EMPTY_LIST) {
            OpenCms.getWorkplaceManager().addExplorerTypeSettings(importedModule);
        }

        CmsImport cmsImport = new CmsImport(importCms, report);
        cmsImport.importData(parameters);
        String importScript = importedModule.getImportScript();
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(importScript)) {
            LOG.info("Executing import script for module " + importedModule.getName());
            report.println(Messages.get().container(Messages.RPT_IMPORT_SCRIPT_HEADER_0), I_CmsReport.FORMAT_HEADLINE);
            importScript = "echo on\n" + importScript;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(buffer);
            CmsShell shell = new CmsShell(cms, "${user}@${project}:${siteroot}|${uri}>", null, out, out);
            shell.execute(importScript);
            String outputString = buffer.toString();
            LOG.info("Shell output for import script was: \n" + outputString);
            report.println(Messages.get().container(Messages.RPT_IMPORT_SCRIPT_OUTPUT_1, outputString));
        }
        importedModule.setCheckpointTime(System.currentTimeMillis());
        OpenCms.getModuleManager().updateModuleConfiguration();
        return importedModule;
    }
}
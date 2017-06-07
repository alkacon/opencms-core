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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.modules;

import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.ade.configuration.formatters.CmsFormatterConfigurationCache;
import org.opencms.configuration.CmsConfigurationCopyResource;
import org.opencms.db.CmsExportPoint;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.A_CmsResourceType;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeUnknown;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsVfsBundleManager;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.Messages;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import com.google.common.base.Function;
import com.google.common.base.Functions;

/**
 * The report thread to clone a module.<p>
 */
public class CmsCloneModuleThread extends A_CmsReportThread {

    /**
     * String replacement function.<p>
     */
    class ReplaceAll implements Function<String, String> {

        /** Regex to match. */
        private String m_from;

        /** Replacement for the regex. */
        private String m_to;

        /**
         * Creates a new instance.<p>
         *
         * @param from the regex to match
         * @param to the replacement
         */
        public ReplaceAll(String from, String to) {

            m_from = from;
            m_to = to;
        }

        /**
         * @see com.google.common.base.Function#apply(java.lang.Object)
         */
        public String apply(String input) {

            return input.replaceAll(m_from, m_to);
        }

    }

    /** The icon path. */
    public static final String ICON_PATH = CmsWorkplace.VFS_PATH_RESOURCES + CmsWorkplace.RES_PATH_FILETYPES;

    /** Classes folder within the module. */
    public static final String PATH_CLASSES = "classes/";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCloneModuleThread.class);

    /** The clone module information. */
    private CmsCloneModuleInfo m_cloneInfo;

    /**
     * Constructor.<p>
     *
     * @param cms the cms context
     * @param cloneInfo the clone module information
     */
    protected CmsCloneModuleThread(CmsObject cms, CmsCloneModuleInfo cloneInfo) {

        super(cms, cloneInfo.getName());
        m_cloneInfo = cloneInfo;
        initHtmlReport(cms.getRequestContext().getLocale());
    }

    /**
     * Returns a list of all module names.<p>
     *
     * @return a list of all module names
     */
    public List<String> getAllModuleNames() {

        List<String> sortedModuleNames = new ArrayList<String>(OpenCms.getModuleManager().getModuleNames());
        java.util.Collections.sort(sortedModuleNames);
        return sortedModuleNames;
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    @Override
    public String getReportUpdate() {

        return getReport().getReportUpdate();
    }

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        CmsModule sourceModule = OpenCms.getModuleManager().getModule(m_cloneInfo.getSourceModuleName());

        // clone the module object
        CmsModule targetModule = (CmsModule)sourceModule.clone();
        targetModule.setName(m_cloneInfo.getName());
        targetModule.setNiceName(m_cloneInfo.getNiceName());
        targetModule.setDescription(m_cloneInfo.getDescription());
        targetModule.setAuthorEmail(m_cloneInfo.getAuthorEmail());
        targetModule.setAuthorName(m_cloneInfo.getAuthorName());
        targetModule.setGroup(m_cloneInfo.getGroup());
        targetModule.setActionClass(m_cloneInfo.getActionClass());

        CmsObject cms = getCms();
        CmsProject currentProject = cms.getRequestContext().getCurrentProject();
        try {
            CmsProject workProject = cms.createProject(
                "Clone_module_work_project",
                "Clone modulee work project",
                OpenCms.getDefaultUsers().getGroupAdministrators(),
                OpenCms.getDefaultUsers().getGroupAdministrators(),
                CmsProject.PROJECT_TYPE_TEMPORARY);
            cms.getRequestContext().setCurrentProject(workProject);

            // store the module paths
            String sourceModulePath = CmsWorkplace.VFS_PATH_MODULES + sourceModule.getName() + "/";
            String targetModulePath = CmsWorkplace.VFS_PATH_MODULES + targetModule.getName() + "/";

            // store the package name as path part
            String sourcePathPart = sourceModule.getName().replaceAll("\\.", "/");
            String targetPathPart = targetModule.getName().replaceAll("\\.", "/");

            // store the classes folder paths
            String sourceClassesPath = targetModulePath + PATH_CLASSES + sourcePathPart + "/";
            String targetClassesPath = targetModulePath + PATH_CLASSES + targetPathPart + "/";

            // copy the resources
            cms.copyResource(sourceModulePath, targetModulePath);

            // check if we have to create the classes folder
            if (cms.existsResource(sourceClassesPath)) {
                // in the source module a classes folder was defined,
                // now create all sub-folders for the package structure in the new module folder
                createTargetClassesFolder(targetModule, sourceClassesPath, targetModulePath + PATH_CLASSES);
                // delete the origin classes folder
                deleteSourceClassesFolder(targetModulePath, sourcePathPart, targetPathPart);
            }

            // TODO: clone module dependencies

            // adjust the export points
            cloneExportPoints(sourceModule, targetModule, sourcePathPart, targetPathPart);

            // adjust the resource type names and IDs
            Map<String, String> descKeys = new HashMap<String, String>();
            Map<I_CmsResourceType, I_CmsResourceType> resTypeMap = cloneResourceTypes(
                sourceModule,
                targetModule,
                sourcePathPart,
                targetPathPart,
                descKeys);

            // adjust the explorer type names and store referred icons and message keys
            Map<String, String> iconPaths = new HashMap<String, String>();
            cloneExplorerTypes(targetModule, iconPaths, descKeys);

            // rename the icon file names
            cloneExplorerTypeIcons(iconPaths);

            // adjust the module resources
            adjustModuleResources(sourceModule, targetModule, sourcePathPart, targetPathPart, iconPaths);

            // search and replace the localization keys
            if (getCms().existsResource(targetClassesPath)) {
                List<CmsResource> props = cms.readResources(targetClassesPath, CmsResourceFilter.DEFAULT_FILES);
                replacesMessages(descKeys, props);
            }

            int type = OpenCms.getResourceManager().getResourceType(CmsVfsBundleManager.TYPE_XML_BUNDLE).getTypeId();
            CmsResourceFilter filter = CmsResourceFilter.requireType(type);
            List<CmsResource> resources = cms.readResources(targetModulePath, filter);
            replacesMessages(descKeys, resources);
            renameXmlVfsBundles(resources, targetModule, sourceModule.getName());

            List<CmsResource> allModuleResources = cms.readResources(targetModulePath, CmsResourceFilter.ALL);
            replacePath(sourceModulePath, targetModulePath, allModuleResources);

            // search and replace paths
            replaceModuleName();

            // replace formatter paths
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_cloneInfo.getFormatterTargetModule())
                && !targetModule.getResourceTypes().isEmpty()) {
                replaceFormatterPaths(targetModule);
            }

            adjustConfigs(targetModule, resTypeMap);

            // now unlock and publish the project
            getReport().println(
                Messages.get().container(Messages.RPT_PUBLISH_PROJECT_BEGIN_0),
                I_CmsReport.FORMAT_HEADLINE);
            cms.unlockProject(workProject.getUuid());
            OpenCms.getPublishManager().publishProject(cms, getReport());
            OpenCms.getPublishManager().waitWhileRunning();

            getReport().println(
                Messages.get().container(Messages.RPT_PUBLISH_PROJECT_END_0),
                I_CmsReport.FORMAT_HEADLINE);

            //  add the imported module to the module manager
            OpenCms.getModuleManager().addModule(cms, targetModule);

            // reinitialize the resource manager with additional module resource types if necessary
            if (targetModule.getResourceTypes() != Collections.EMPTY_LIST) {
                OpenCms.getResourceManager().initialize(cms);
            }
            // reinitialize the workplace manager with additional module explorer types if necessary
            if (targetModule.getExplorerTypes() != Collections.EMPTY_LIST) {
                OpenCms.getWorkplaceManager().addExplorerTypeSettings(targetModule);
            }

            // re-initialize the workplace
            OpenCms.getWorkplaceManager().initialize(cms);
            // fire "clear caches" event to reload all cached resource bundles
            OpenCms.fireCmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, new HashMap<String, Object>());

            // following changes will not be published right now, switch back to previous project
            cms.getRequestContext().setCurrentProject(currentProject);

            // change resource types and schema locations
            if (isTrue(m_cloneInfo.getChangeResourceTypes())) {
                changeResourceTypes(resTypeMap);
            }
            // adjust container pages
            CmsObject cloneCms = OpenCms.initCmsObject(cms);
            if (isTrue(m_cloneInfo.getApplyChangesEverywhere())) {
                cloneCms.getRequestContext().setSiteRoot("/");
            }

            if (m_cloneInfo.isRewriteContainerPages()) {
                CmsResourceFilter f = CmsResourceFilter.requireType(
                    CmsResourceTypeXmlContainerPage.getContainerPageTypeId());
                List<CmsResource> allContainerPages = cloneCms.readResources("/", f);
                replacePath(sourceModulePath, targetModulePath, allContainerPages);
            }
        } catch (Throwable e) {
            LOG.error(e.getLocalizedMessage(), e);
            getReport().addError(e);
        } finally {
            cms.getRequestContext().setCurrentProject(currentProject);
        }
    }

    /**
     * Returns <code>true</code> if the module has been created successful.<p>
     *
     * @return <code>true</code> if the module has been created successful
     */
    public boolean success() {

        return OpenCms.getModuleManager().getModule(m_cloneInfo.getName()) != null;
    }

    /**
     * Adjusts the module configuration file and the formatter configurations.<p>
     *
     * @param targetModule the target module
     * @param resTypeMap the resource type mapping
     *
     * @throws CmsException if something goes wrong
     * @throws UnsupportedEncodingException if the file content could not be read with the determined encoding
     */
    private void adjustConfigs(CmsModule targetModule, Map<I_CmsResourceType, I_CmsResourceType> resTypeMap)
    throws CmsException, UnsupportedEncodingException {

        String modPath = CmsWorkplace.VFS_PATH_MODULES + targetModule.getName() + "/";
        CmsObject cms = getCms();
        if (((m_cloneInfo.getSourceNamePrefix() != null) && (m_cloneInfo.getTargetNamePrefix() != null))
            || !m_cloneInfo.getSourceNamePrefix().equals(m_cloneInfo.getTargetNamePrefix())) {
            // replace resource type names in formatter configurations
            List<CmsResource> resources = cms.readResources(
                modPath,
                CmsResourceFilter.requireType(
                    OpenCms.getResourceManager().getResourceType(
                        CmsFormatterConfigurationCache.TYPE_FORMATTER_CONFIG)));
            String source = "<Type><!\\[CDATA\\[" + m_cloneInfo.getSourceNamePrefix();
            String target = "<Type><!\\[CDATA\\[" + m_cloneInfo.getTargetNamePrefix();
            Function<String, String> replaceType = new ReplaceAll(source, target);

            for (CmsResource resource : resources) {
                transformResource(resource, replaceType);
            }
            resources.clear();
        }

        // replace resource type names in module configuration
        try {
            CmsResource config = cms.readResource(
                modPath + CmsADEManager.CONFIG_FILE_NAME,
                CmsResourceFilter.requireType(
                    OpenCms.getResourceManager().getResourceType(CmsADEManager.MODULE_CONFIG_TYPE)));
            Function<String, String> substitution = Functions.identity();
            // compose the substitution functions from simple substitution functions for each type

            for (Map.Entry<I_CmsResourceType, I_CmsResourceType> mapping : resTypeMap.entrySet()) {
                substitution = Functions.compose(
                    new ReplaceAll(mapping.getKey().getTypeName(), mapping.getValue().getTypeName()),
                    substitution);
            }

            // Either replace prefix in or prepend it to the folder name value

            Function<String, String> replaceFolderName = new ReplaceAll(
                "(<Folder>[ \n]*<Name><!\\[CDATA\\[)(" + m_cloneInfo.getSourceNamePrefix() + ")?",
                "$1" + m_cloneInfo.getTargetNamePrefix());
            substitution = Functions.compose(replaceFolderName, substitution);
            transformResource(config, substitution);
        } catch (CmsVfsResourceNotFoundException e) {
            LOG.info(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Adjusts the paths of the module resources from the source path to the target path.<p>
     *
     * @param targetResources the paths to adjust
     * @param sourceModuleName the source module name
     * @param targetModuleName the target module name
     * @param sourcePathPart the path part of the source module
     * @param targetPathPart the path part of the target module
     * @param iconPaths the path where resource type icons are located
     * @return the adjusted paths
     */
    private List<String> adjustModuleResourcePaths(
        List<String> targetResources,
        String sourceModuleName,
        String targetModuleName,
        String sourcePathPart,
        String targetPathPart,
        Map<String, String> iconPaths) {

        List<String> newTargetResources = new ArrayList<String>();
        for (String modRes : targetResources) {
            String nIcon = iconPaths.get(modRes.substring(modRes.lastIndexOf('/') + 1));
            if (nIcon != null) {
                // the referenced resource is an resource type icon, add the new icon path
                newTargetResources.add(ICON_PATH + nIcon);
            } else if (modRes.contains(sourceModuleName)) {
                // there is the name in it
                newTargetResources.add(modRes.replaceAll(sourceModuleName, targetModuleName));
            } else if (modRes.contains(sourcePathPart)) {
                // there is a path in it
                newTargetResources.add(modRes.replaceAll(sourcePathPart, targetPathPart));
            } else {
                // there is whether the path nor the name in it
                newTargetResources.add(modRes);
            }
        }
        return newTargetResources;
    }

    /**
     * Adjusts the paths of the module resources from the source path to the target path.<p>
     *
     * @param sourceModule the source module
     * @param targetModule the target module
     * @param sourcePathPart the path part of the source module
     * @param targetPathPart the path part of the target module
     * @param iconPaths the path where resource type icons are located
     */
    private void adjustModuleResources(
        CmsModule sourceModule,
        CmsModule targetModule,
        String sourcePathPart,
        String targetPathPart,
        Map<String, String> iconPaths) {

        List<String> newTargetResources = adjustModuleResourcePaths(
            targetModule.getResources(),
            sourceModule.getName(),
            targetModule.getName(),
            sourcePathPart,
            targetPathPart,
            iconPaths);
        targetModule.setResources(newTargetResources);

        List<String> newTargetExcludeResources = adjustModuleResourcePaths(
            targetModule.getExcludeResources(),
            sourceModule.getName(),
            targetModule.getName(),
            sourcePathPart,
            targetPathPart,
            iconPaths);
        targetModule.setExcludeResources(newTargetExcludeResources);
    }

    /**
     * Manipulates a string by cutting of a prefix, if present, and adding a new prefix.
     *
     * @param word the string to be manipulated
     * @param oldPrefix the old prefix that should be replaced
     * @param newPrefix the new prefix that is added
     * @return the manipulated string
     */
    private String alterPrefix(String word, String oldPrefix, String newPrefix) {

        if (word.startsWith(oldPrefix)) {
            return word.replaceFirst(oldPrefix, newPrefix);
        }
        return (newPrefix + word);
    }

    /**
     * Changes the resource types and the schema locations of existing content.<p>
     *
     * @param resTypeMap a map containing the source types as keys and the target types as values
     *
     * @throws CmsException if something goes wrong
     * @throws UnsupportedEncodingException if the file content could not be read with the determined encoding
     */
    private void changeResourceTypes(Map<I_CmsResourceType, I_CmsResourceType> resTypeMap)
    throws CmsException, UnsupportedEncodingException {

        CmsObject cms = getCms();
        CmsObject cloneCms = OpenCms.initCmsObject(cms);

        if (isTrue(m_cloneInfo.getApplyChangesEverywhere())) {
            cloneCms.getRequestContext().setSiteRoot("/");
        }

        for (Map.Entry<I_CmsResourceType, I_CmsResourceType> mapping : resTypeMap.entrySet()) {
            CmsResourceFilter filter = CmsResourceFilter.requireType(mapping.getKey());
            List<CmsResource> resources = cloneCms.readResources("/", filter);
            String sourceSchemaPath = mapping.getKey().getConfiguration().get("schema");
            String targetSchemaPath = mapping.getValue().getConfiguration().get("schema");
            for (CmsResource res : resources) {
                if (lockResource(cms, res)) {
                    CmsFile file = cms.readFile(res);
                    if (CmsResourceTypeXmlContent.isXmlContent(file)) {
                        CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(getCms(), file);
                        xmlContent.setAutoCorrectionEnabled(true);
                        file = xmlContent.correctXmlStructure(getCms());
                    }
                    String encoding = CmsLocaleManager.getResourceEncoding(cms, file);
                    String content = new String(file.getContents(), encoding);
                    content = content.replaceAll(sourceSchemaPath, targetSchemaPath);
                    file.setContents(content.getBytes(encoding));
                    try {
                        cms.writeFile(file);
                    } catch (CmsXmlException e) {
                        LOG.error(e.getMessage(), e);
                    }
                    res.setType(mapping.getValue().getTypeId());
                    cms.writeResource(res);
                }
            }
        }
    }

    /**
     * Copies the explorer type icons.<p>
     *
     * @param iconPaths the path to the location where the icons are located
     *
     * @throws CmsException if something goes wrong
     */
    private void cloneExplorerTypeIcons(Map<String, String> iconPaths) throws CmsException {

        for (Map.Entry<String, String> entry : iconPaths.entrySet()) {
            String source = ICON_PATH + entry.getKey();
            String target = ICON_PATH + entry.getValue();
            if (getCms().existsResource(source) && !getCms().existsResource(target)) {
                getCms().copyResource(source, target);
            }
        }
    }

    /**
     * Copies the explorer type definitions.<p>
     *
     * @param targetModule the target module
     * @param iconPaths the path to the location where the icons are located
     * @param descKeys a map that contains a mapping of the explorer type definitions messages
     */
    private void cloneExplorerTypes(
        CmsModule targetModule,
        Map<String, String> iconPaths,
        Map<String, String> descKeys) {

        List<CmsExplorerTypeSettings> targetExplorerTypes = targetModule.getExplorerTypes();
        for (CmsExplorerTypeSettings expSetting : targetExplorerTypes) {
            descKeys.put(
                expSetting.getKey(),
                alterPrefix(expSetting.getKey(), m_cloneInfo.getSourceNamePrefix(), m_cloneInfo.getTargetNamePrefix()));
            String newIcon = alterPrefix(
                expSetting.getIcon(),
                m_cloneInfo.getSourceNamePrefix(),
                m_cloneInfo.getTargetNamePrefix());
            String newBigIcon = alterPrefix(
                expSetting.getBigIconIfAvailable(),
                m_cloneInfo.getSourceNamePrefix(),
                m_cloneInfo.getTargetNamePrefix());
            iconPaths.put(expSetting.getIcon(), newIcon);
            iconPaths.put(expSetting.getBigIconIfAvailable(), newBigIcon);
            String oldExpTypeName = expSetting.getName();
            String newExpTypeName = alterPrefix(
                oldExpTypeName,
                m_cloneInfo.getSourceNamePrefix(),
                m_cloneInfo.getTargetNamePrefix());
            expSetting.setName(newExpTypeName);
            String newResourcePage = expSetting.getNewResourcePage();
            if (newResourcePage != null) {
                expSetting.setNewResourcePage(
                    alterPrefix(newResourcePage, m_cloneInfo.getSourceNamePrefix(), m_cloneInfo.getTargetNamePrefix()));
            }
            expSetting.setKey(expSetting.getKey().replaceFirst(oldExpTypeName, newExpTypeName));
            expSetting.setIcon(
                alterPrefix(
                    expSetting.getIcon(),
                    m_cloneInfo.getSourceNamePrefix(),
                    m_cloneInfo.getTargetNamePrefix()));
            expSetting.setBigIcon(
                alterPrefix(
                    expSetting.getBigIconIfAvailable(),
                    m_cloneInfo.getSourceNamePrefix(),
                    m_cloneInfo.getTargetNamePrefix()));
            expSetting.setNewResourceUri(expSetting.getNewResourceUri().replaceFirst(oldExpTypeName, newExpTypeName));
            expSetting.setInfo(expSetting.getInfo().replaceFirst(oldExpTypeName, newExpTypeName));
        }
    }

    /**
     * Clones the export points of the module and adjusts its paths.<p>
     *
     * @param sourceModule the source module
     * @param targetModule the target module
     * @param sourcePathPart the source path part
     * @param targetPathPart the target path part
     */
    private void cloneExportPoints(
        CmsModule sourceModule,
        CmsModule targetModule,
        String sourcePathPart,
        String targetPathPart) {

        for (CmsExportPoint exp : targetModule.getExportPoints()) {
            if (exp.getUri().contains(sourceModule.getName())) {
                exp.setUri(exp.getUri().replaceAll(sourceModule.getName(), targetModule.getName()));
            }
            if (exp.getUri().contains(sourcePathPart)) {
                exp.setUri(exp.getUri().replaceAll(sourcePathPart, targetPathPart));
            }
        }
    }

    /**
     * Clones/copies the resource types.<p>
    
     * @param sourceModule the source module
     * @param targetModule the target module
     * @param sourcePathPart the source path part
     * @param targetPathPart the target path part
     * @param keys the map where to put in the messages of the resource type
     *
     * @return a map with source resource types as key and the taregt resource types as value
     */
    private Map<I_CmsResourceType, I_CmsResourceType> cloneResourceTypes(
        CmsModule sourceModule,
        CmsModule targetModule,
        String sourcePathPart,
        String targetPathPart,
        Map<String, String> keys) {

        Map<I_CmsResourceType, I_CmsResourceType> resourceTypeMapping = new HashMap<I_CmsResourceType, I_CmsResourceType>();

        List<I_CmsResourceType> targetResourceTypes = new ArrayList<I_CmsResourceType>();
        for (I_CmsResourceType sourceResType : targetModule.getResourceTypes()) {

            // get the class name attribute
            String className = sourceResType.getClassName();
            // create the class instance
            I_CmsResourceType targetResType;
            try {
                if (className != null) {
                    className = className.trim();
                }

                int newId = -1;
                boolean exists = true;
                do {
                    newId = new Random().nextInt((99999)) + 10000;
                    try {
                        OpenCms.getResourceManager().getResourceType(newId);
                    } catch (CmsLoaderException e) {
                        exists = false;
                    }
                } while (exists);

                targetResType = (I_CmsResourceType)Class.forName(className).newInstance();

                for (String mapping : sourceResType.getConfiguredMappings()) {
                    targetResType.addMappingType(mapping);
                }

                targetResType.setAdjustLinksFolder(sourceResType.getAdjustLinksFolder());

                if (targetResType instanceof A_CmsResourceType) {
                    A_CmsResourceType concreteTargetResType = (A_CmsResourceType)targetResType;
                    for (CmsProperty prop : sourceResType.getConfiguredDefaultProperties()) {
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(prop.getValue())) {
                            prop.setStructureValue(
                                prop.getStructureValue().replaceAll(
                                    sourceModule.getName(),
                                    targetModule.getName()).replaceAll(sourcePathPart, targetPathPart));
                            prop.setResourceValue(
                                prop.getResourceValue().replaceAll(
                                    sourceModule.getName(),
                                    targetModule.getName()).replaceAll(sourcePathPart, targetPathPart));
                        }
                        concreteTargetResType.addDefaultProperty(prop);
                    }
                    for (CmsConfigurationCopyResource conres : sourceResType.getConfiguredCopyResources()) {
                        concreteTargetResType.addCopyResource(
                            conres.getSource(),
                            conres.getTarget(),
                            conres.getTypeString());
                    }
                }

                for (Map.Entry<String, String> entry : sourceResType.getConfiguration().entrySet()) {
                    targetResType.addConfigurationParameter(
                        entry.getKey(),
                        entry.getValue().replaceAll(sourceModule.getName(), targetModule.getName()));
                }

                targetResType.setAdditionalModuleResourceType(true);
                targetResType.initConfiguration(
                    alterPrefix(
                        sourceResType.getTypeName(),
                        m_cloneInfo.getSourceNamePrefix(),
                        m_cloneInfo.getTargetNamePrefix()),
                    newId + "",
                    sourceResType.getClassName());

                keys.put(sourceResType.getTypeName(), targetResType.getTypeName());
                targetResourceTypes.add(targetResType);

                resourceTypeMapping.put(sourceResType, targetResType);

            } catch (Exception e) {
                // resource type is unknown, use dummy class to import the module resources
                targetResType = new CmsResourceTypeUnknown();
                // write an error to the log
                LOG.error(
                    org.opencms.configuration.Messages.get().getBundle().key(
                        org.opencms.configuration.Messages.ERR_UNKNOWN_RESTYPE_CLASS_2,
                        className,
                        targetResType.getClass().getName()),
                    e);
            }
        }
        targetModule.setResourceTypes(targetResourceTypes);
        return resourceTypeMapping;
    }

    /**
     * Creates the target folder for the module clone.<p>
     *
     * @param targetModule the target module
     * @param sourceClassesPath the source module class path
     * @param targetBaseClassesPath the 'classes' folder of the target module
     *
     * @throws CmsException if something goes wrong
     */
    private void createTargetClassesFolder(
        CmsModule targetModule,
        String sourceClassesPath,
        String targetBaseClassesPath) throws CmsException {

        StringTokenizer tok = new StringTokenizer(targetModule.getName(), ".");
        int folderId = CmsResourceTypeFolder.getStaticTypeId();
        String targetClassesPath = targetBaseClassesPath;

        while (tok.hasMoreTokens()) {
            String folder = tok.nextToken();
            targetClassesPath += folder + "/";
            if (!getCms().existsResource(targetClassesPath)) {
                getCms().createResource(targetClassesPath, folderId);
            }
        }
        // move exiting content into new classes sub-folder
        List<CmsResource> propertyFiles = getCms().readResources(sourceClassesPath, CmsResourceFilter.ALL);
        for (CmsResource res : propertyFiles) {
            if (!getCms().existsResource(targetClassesPath + res.getName())) {
                getCms().copyResource(res.getRootPath(), targetClassesPath + res.getName());
            }
        }
    }

    /**
     * Deletes the temporarily copied classes files.<p>
     *
     * @param targetModulePath the target module path
     * @param sourcePathPart the path part of the source module
     * @param targetPathPart the target path part
     *
     * @throws CmsException if something goes wrong
     */
    private void deleteSourceClassesFolder(String targetModulePath, String sourcePathPart, String targetPathPart)
    throws CmsException {

        String sourceFirstFolder = sourcePathPart.substring(0, sourcePathPart.indexOf('/'));
        String targetFirstFolder = sourcePathPart.substring(0, sourcePathPart.indexOf('/'));
        if (!sourceFirstFolder.equals(targetFirstFolder)) {
            getCms().deleteResource(
                targetModulePath + PATH_CLASSES + sourceFirstFolder,
                CmsResource.DELETE_PRESERVE_SIBLINGS);
            return;
        }
        String[] targetPathParts = CmsStringUtil.splitAsArray(targetPathPart, '/');
        String[] sourcePathParts = CmsStringUtil.splitAsArray(sourcePathPart, '/');
        int sourceLength = sourcePathParts.length;
        int diff = 0;
        for (int i = 0; i < targetPathParts.length; i++) {
            if (sourceLength >= i) {
                if (!targetPathParts[i].equals(sourcePathParts[i])) {
                    diff = i + 1;
                }
            }
        }
        String topSourceClassesPath = targetModulePath
            + PATH_CLASSES
            + sourcePathPart.substring(0, sourcePathPart.indexOf('/'))
            + "/";

        if (diff != 0) {
            topSourceClassesPath = targetModulePath + PATH_CLASSES;
            for (int i = 0; i < diff; i++) {
                topSourceClassesPath += sourcePathParts[i] + "/";
            }
        }
        getCms().deleteResource(topSourceClassesPath, CmsResource.DELETE_PRESERVE_SIBLINGS);
    }

    /**
     * Returns <code>true</code> if form input is selected, checked, on or yes.<p>
     *
     * @param value the value to check
     *
     * @return <code>true</code> if form input is selected, checked, on or yes
     */
    private boolean isTrue(String value) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
            if (Boolean.valueOf(value.toLowerCase()).booleanValue()
                || value.toLowerCase().equals("on")
                || value.toLowerCase().equals("yes")
                || value.toLowerCase().equals("checked")
                || value.toLowerCase().equals("selected")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Locks the current resource.<p>
     *
     * @param cms the current CmsObject
     * @param cmsResource the resource to lock
     *
     * @return <code>true</code> if the given resource was locked was successfully
     *
     * @throws CmsException if some goes wrong
     */
    private boolean lockResource(CmsObject cms, CmsResource cmsResource) throws CmsException {

        CmsLock lock = cms.getLock(cms.getSitePath(cmsResource));
        // check the lock
        if ((lock != null)
            && lock.isOwnedBy(cms.getRequestContext().getCurrentUser())
            && lock.isOwnedInProjectBy(
                cms.getRequestContext().getCurrentUser(),
                cms.getRequestContext().getCurrentProject())) {
            // prove is current lock from current user in current project
            return true;
        } else if ((lock != null) && !lock.isUnlocked() && !lock.isOwnedBy(cms.getRequestContext().getCurrentUser())) {
            // the resource is not locked by the current user, so can not lock it
            return false;
        } else if ((lock != null)
            && !lock.isUnlocked()
            && lock.isOwnedBy(cms.getRequestContext().getCurrentUser())
            && !lock.isOwnedInProjectBy(
                cms.getRequestContext().getCurrentUser(),
                cms.getRequestContext().getCurrentProject())) {
            // prove is current lock from current user but not in current project
            // file is locked by current user but not in current project

            cms.changeLock(cms.getSitePath(cmsResource));
        } else if ((lock != null) && lock.isUnlocked()) {
            // lock resource from current user in current project
            cms.lockResource(cms.getSitePath(cmsResource));
        }
        lock = cms.getLock(cms.getSitePath(cmsResource));
        if ((lock != null)
            && lock.isOwnedBy(cms.getRequestContext().getCurrentUser())
            && !lock.isOwnedInProjectBy(
                cms.getRequestContext().getCurrentUser(),
                cms.getRequestContext().getCurrentProject())) {
            // resource could not be locked
            return false;
        }
        // resource is locked successfully
        return true;
    }

    /**
     * Renames the vfs resource bundle files within the target module according to the new module's name.<p>
     *
     * @param resources the vfs resource bundle files
     * @param targetModule the target module
     * @param name the package name of the source module
     *
     * @return a list of all xml vfs bundles within the given module
     * @throws CmsException if something gows wrong
     */
    private List<CmsResource> renameXmlVfsBundles(List<CmsResource> resources, CmsModule targetModule, String name)
    throws CmsException {

        for (CmsResource res : resources) {
            String newName = res.getName().replaceAll(name, targetModule.getName());
            String targetRootPath = CmsResource.getFolderPath(res.getRootPath()) + newName;
            if (!getCms().existsResource(targetRootPath)) {
                getCms().moveResource(res.getRootPath(), targetRootPath);
            }
        }
        return resources;

    }

    /**
     * Replaces the referenced formatters within the new XSD files with the new formatter paths.<p>
     *
     * @param targetModule the target module
     *
     * @throws CmsException if something goes wrong
     * @throws UnsupportedEncodingException if the file content could not be read with the determined encoding
     */
    private void replaceFormatterPaths(CmsModule targetModule) throws CmsException, UnsupportedEncodingException {

        CmsResource formatterSourceFolder = getCms().readResource(
            "/system/modules/" + m_cloneInfo.getFormatterSourceModule() + "/");
        CmsResource formatterTargetFolder = getCms().readResource(
            "/system/modules/" + m_cloneInfo.getFormatterTargetModule() + "/");
        for (I_CmsResourceType type : targetModule.getResourceTypes()) {
            String schemaPath = type.getConfiguration().get("schema");
            CmsResource res = getCms().readResource(schemaPath);
            CmsFile file = getCms().readFile(res);
            if (CmsResourceTypeXmlContent.isXmlContent(file)) {
                CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(getCms(), file);
                xmlContent.setAutoCorrectionEnabled(true);
                file = xmlContent.correctXmlStructure(getCms());
            }
            String encoding = CmsLocaleManager.getResourceEncoding(getCms(), file);
            String content = new String(file.getContents(), encoding);
            content = content.replaceAll(formatterSourceFolder.getRootPath(), formatterTargetFolder.getRootPath());
            file.setContents(content.getBytes(encoding));
            getCms().writeFile(file);
        }
    }

    /**
     * Initializes a thread to find and replace all occurrence of the module's path.<p>
     *
     * @throws CmsException in case writing the file fails
     * @throws UnsupportedEncodingException in case of the wrong encoding
    
     */
    private void replaceModuleName() throws CmsException, UnsupportedEncodingException {

        CmsResourceFilter filter = CmsResourceFilter.ALL.addRequireFile().addExcludeState(
            CmsResource.STATE_DELETED).addRequireTimerange().addRequireVisible();
        List<CmsResource> resources = getCms().readResources(
            CmsWorkplace.VFS_PATH_MODULES + m_cloneInfo.getName() + "/",
            filter);
        for (CmsResource resource : resources) {
            CmsFile file = getCms().readFile(resource);
            if (CmsResourceTypeXmlContent.isXmlContent(file)) {
                CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(getCms(), file);
                xmlContent.setAutoCorrectionEnabled(true);
                file = xmlContent.correctXmlStructure(getCms());
            }
            byte[] contents = file.getContents();
            String encoding = CmsLocaleManager.getResourceEncoding(getCms(), file);
            String content = new String(contents, encoding);
            Matcher matcher = Pattern.compile(m_cloneInfo.getSourceModuleName()).matcher(content);
            if (matcher.find()) {
                contents = matcher.replaceAll(m_cloneInfo.getName()).getBytes(encoding);
                if (lockResource(getCms(), file)) {
                    file.setContents(contents);
                    getCms().writeFile(file);
                }
            }
        }
    }

    /**
     * Replaces the paths within all the given resources and removes all UUIDs by an regex.<p>
     *
     * @param sourceModulePath the search path
     * @param targetModulePath the replace path
     * @param resources the resources
     *
     * @throws CmsException if something goes wrong
     * @throws UnsupportedEncodingException if the file content could not be read with the determined encoding
     */
    private void replacePath(String sourceModulePath, String targetModulePath, List<CmsResource> resources)
    throws CmsException, UnsupportedEncodingException {

        for (CmsResource resource : resources) {
            if (resource.isFile()) {
                CmsFile file = getCms().readFile(resource);
                if (CmsResourceTypeXmlContent.isXmlContent(file)) {
                    CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(getCms(), file);
                    xmlContent.setAutoCorrectionEnabled(true);
                    file = xmlContent.correctXmlStructure(getCms());
                }
                String encoding = CmsLocaleManager.getResourceEncoding(getCms(), file);
                String oldContent = new String(file.getContents(), encoding);
                String newContent = oldContent.replaceAll(sourceModulePath, targetModulePath);
                Matcher matcher = Pattern.compile(CmsUUID.UUID_REGEX).matcher(newContent);
                newContent = matcher.replaceAll("");
                newContent = newContent.replaceAll("<uuid></uuid>", "");
                if (!oldContent.equals(newContent)) {
                    file.setContents(newContent.getBytes(encoding));
                    if (!resource.getRootPath().startsWith(CmsWorkplace.VFS_PATH_SYSTEM)) {
                        if (lockResource(getCms(), resource)) {
                            getCms().writeFile(file);
                        }
                    } else {
                        getCms().writeFile(file);
                    }
                }
            }
        }
    }

    /**
     * Replaces the messages for the given resources.<p>
     *
     * @param descKeys the replacement mapping
     * @param resources the resources to consult
     *
     * @throws CmsException if something goes wrong
     * @throws UnsupportedEncodingException if the file content could not be read with the determined encoding
    
     */
    private void replacesMessages(Map<String, String> descKeys, List<CmsResource> resources)
    throws CmsException, UnsupportedEncodingException {

        for (CmsResource resource : resources) {
            CmsFile file = getCms().readFile(resource);
            String encoding = CmsLocaleManager.getResourceEncoding(getCms(), file);
            String content = new String(file.getContents(), encoding);
            for (Map.Entry<String, String> entry : descKeys.entrySet()) {
                content = content.replaceAll(entry.getKey(), entry.getValue());
            }
            file.setContents(content.getBytes(encoding));
            getCms().writeFile(file);
        }
    }

    /**
     * Reads a file into a string, applies a transformation to the string, and writes the string back to the file.<p>
     *
     * @param resource the resource to transform
     * @param transformation the transformation to apply
     * @throws CmsException if something goes wrong
     * @throws UnsupportedEncodingException in case the encoding is not supported
     */
    private void transformResource(CmsResource resource, Function<String, String> transformation)
    throws CmsException, UnsupportedEncodingException {

        CmsFile file = getCms().readFile(resource);
        if (CmsResourceTypeXmlContent.isXmlContent(file)) {
            CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(getCms(), file);
            xmlContent.setAutoCorrectionEnabled(true);
            file = xmlContent.correctXmlStructure(getCms());
        }
        String encoding = CmsLocaleManager.getResourceEncoding(getCms(), file);
        String content = new String(file.getContents(), encoding);
        content = transformation.apply(content);
        file.setContents(content.getBytes(encoding));
        lockResource(getCms(), file);
        getCms().writeFile(file);

    }
}

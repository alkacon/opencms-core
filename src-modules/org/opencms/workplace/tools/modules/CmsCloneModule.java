/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools.modules;

import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.ade.configuration.formatters.CmsFormatterConfigurationCache;
import org.opencms.configuration.CmsConfigurationCopyResource;
import org.opencms.configuration.Messages;
import org.opencms.db.CmsExportPoint;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.A_CmsResourceType;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeUnknown;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsVfsBundleManager;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.loader.CmsLoaderException;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.report.CmsLogReport;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.xml.CmsXmlException;

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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Clones a module.<p>
 */
public class CmsCloneModule extends CmsJspActionElement {

    /** The icon path. */
    public static final String ICON_PATH = CmsWorkplace.VFS_PATH_RESOURCES + CmsWorkplace.RES_PATH_FILETYPES;

    /** Classes folder within the module. */
    public static final String PATH_CLASSES = "classes/";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCloneModule.class);

    /** The action class used for the clone. */
    private String m_actionClass;

    /** If 'true' resource types in all sites will be adjusted. */
    private String m_applyChangesEverywhere;

    /** The author's email used for the clone. */
    private String m_authorEmail = "sales@alkacon.com";

    /** The author's name used for the clone. */
    private String m_authorName = "Alkacon Software GmbH";

    /** Option to change the resource types (optional flag). */
    private String m_changeResourceTypes;

    /** Option to delete the source module after cloning (optional flag). */
    private String m_deleteModule;

    /** The description used for the clone. */
    private String m_description = "This module provides the template layout.";

    /** A module name where the formatters are located that are referenced within the XSDs of the module to clone. */
    private String m_formatterSourceModule = "com.alkacon.bootstrap.formatters";

    /** A module name where the formatters are located that should be referenced by the XSDs of the clone. */
    private String m_formatterTargetModule;

    /** The module group used for the clone. */
    private String m_group;

    /** The nice name used for the clone. */
    private String m_niceName = "My new template module.";

    /** The new module name used for the clone. */
    private String m_packageName = "my.company.template";

    /** Flag that controls whether container pages should be rewritten. */
    private boolean m_rewriteContainerPages;

    /** The name of the source module to be cloned. */
    private String m_sourceModuleName = "com.alkacon.bootstrap.formatters";

    /** The prefix that is used by the source module. */
    private String m_sourceNamePrefix = "bs";

    /** The prefix that is used by the target module. */
    private String m_targetNamePrefix = "my";

    /**
     * Public constructor.<p>
     */
    public CmsCloneModule() {

        // NOOP
    }

    /**
     * Constructor, with parameters.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsCloneModule(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * Bean constructor.<p>
     *  
     * @param actionClass action class
     * @param authorEmail author email
     * @param authorName author name
     * @param changeResourceTypes change resource type flags
     * @param description module description
     * @param formatterSourceModule formatter source module
     * @param formatterTargetModule formatter target module
     * @param group module group
     * @param niceName nice name
     * @param packageName package/module name
     * @param sourceModuleName source module package/name
     * @param sourceNamePrefix source name prefix
     * @param targetNamePrefix source name prefix
     */
    public CmsCloneModule(
        String actionClass,
        String authorEmail,
        String authorName,
        String changeResourceTypes,
        String description,
        String formatterSourceModule,
        String formatterTargetModule,
        String group,
        String niceName,
        String packageName,
        String sourceModuleName,
        String sourceNamePrefix,
        String targetNamePrefix) {

        super();
        m_actionClass = actionClass;
        m_authorEmail = authorEmail;
        m_authorName = authorName;
        m_changeResourceTypes = changeResourceTypes;
        m_description = description;
        m_formatterSourceModule = formatterSourceModule;
        m_formatterTargetModule = formatterTargetModule;
        m_group = group;
        m_niceName = niceName;
        m_packageName = packageName;
        m_sourceModuleName = sourceModuleName;
        m_sourceNamePrefix = sourceNamePrefix;
        m_targetNamePrefix = targetNamePrefix;
    }

    /**
     * Executes the module clone and returns the new module.<p>
     * 
     * @throws Throwable if anything goes wrong
     */
    public void executeModuleClone() throws Throwable {

        CmsModule sourceModule = OpenCms.getModuleManager().getModule(m_sourceModuleName);

        // clone the module object
        CmsModule targetModule = (CmsModule)sourceModule.clone();
        targetModule.setName(m_packageName);
        targetModule.setNiceName(m_niceName);
        targetModule.setDescription(m_description);
        targetModule.setAuthorEmail(m_authorEmail);
        targetModule.setAuthorName(m_authorName);
        targetModule.setGroup(m_group);
        targetModule.setActionClass(m_actionClass);

        try {

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
            getCmsObject().copyResource(sourceModulePath, targetModulePath);

            // check if we have to create the classes folder
            if (getCmsObject().existsResource(sourceClassesPath)) {
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
            if (getCmsObject().existsResource(targetClassesPath)) {
                List<CmsResource> props = getCmsObject().readResources(
                    targetClassesPath,
                    CmsResourceFilter.DEFAULT_FILES);
                replacesMessages(descKeys, props);
            }

            int type = OpenCms.getResourceManager().getResourceType(CmsVfsBundleManager.TYPE_XML_BUNDLE).getTypeId();
            CmsResourceFilter filter = CmsResourceFilter.requireType(type);
            List<CmsResource> resources = getCmsObject().readResources(targetModulePath, filter);
            replacesMessages(descKeys, resources);
            renameXmlVfsBundles(resources, targetModule, sourceModule.getName());

            List<CmsResource> allModuleResources = getCmsObject().readResources(targetModulePath, CmsResourceFilter.ALL);
            replacePath(sourceModulePath, targetModulePath, allModuleResources);

            // search and replace paths
            replaceModuleName();

            // replace formatter paths
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_formatterTargetModule)
                && !targetModule.getResourceTypes().isEmpty()) {
                replaceFormatterPaths(targetModule);
            }

            adjustConfigs(targetModule, resTypeMap);

            // publish the new module
            for (String res : targetModule.getResources()) {
                OpenCms.getPublishManager().publishResource(getCmsObject(), res);
                OpenCms.getPublishManager().waitWhileRunning();
            }

            //  add the imported module to the module manager
            OpenCms.getModuleManager().addModule(getCmsObject(), targetModule);

            // reinitialize the resource manager with additional module resource types if necessary
            if (targetModule.getResourceTypes() != Collections.EMPTY_LIST) {
                OpenCms.getResourceManager().initialize(getCmsObject());
            }
            // reinitialize the workplace manager with additional module explorer types if necessary
            if (targetModule.getExplorerTypes() != Collections.EMPTY_LIST) {
                OpenCms.getWorkplaceManager().addExplorerTypeSettings(targetModule);
            }

            // re-initialize the workplace
            OpenCms.getWorkplaceManager().initialize(getCmsObject());
            // fire "clear caches" event to reload all cached resource bundles
            OpenCms.fireCmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, new HashMap<String, Object>());

            // change resource types and schema locations
            if (isTrue(m_changeResourceTypes)) {
                changeResourceTypes(resTypeMap);
            }
            // adjust container pages
            CmsObject cms = OpenCms.initCmsObject(getCmsObject());
            if (isTrue(m_applyChangesEverywhere)) {
                cms.getRequestContext().setSiteRoot("/");
            }

            if (isRewriteContainerPages()) {
                CmsResourceFilter f = CmsResourceFilter.requireType(CmsResourceTypeXmlContainerPage.getContainerPageTypeId());
                List<CmsResource> allContainerPages = cms.readResources("/", f);
                replacePath(sourceModulePath, targetModulePath, allContainerPages);
            }
            // delete the old module
            if (isTrue(m_deleteModule)) {
                OpenCms.getModuleManager().deleteModule(
                    getCmsObject(),
                    sourceModule.getName(),
                    false,
                    new CmsLogReport(getCmsObject().getRequestContext().getLocale(), CmsCloneModule.class));
            }

        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
            throw t;
        }
    }

    /**
     * Returns the action class.<p>
     *
     * @return the action class
     */
    public String getActionClass() {

        return m_actionClass;
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
     * Returns the author email.<p>
     *
     * @return the author email
     */
    public String getAuthorEmail() {

        return m_authorEmail;
    }

    /**
     * Returns the author name.<p>
     *
     * @return the author name
     */
    public String getAuthorName() {

        return m_authorName;
    }

    /**
     * Returns the change resource types flag as String.<p>
     *
     * @return the change resource types flag as String
     */
    public String getChangeResourceTypes() {

        return m_changeResourceTypes;
    }

    /**
     * Returns the changeResourceTypesEverywhere.<p>
     *
     * @return the changeResourceTypesEverywhere
     */
    public String getChangeResourceTypesEverywhere() {

        return m_applyChangesEverywhere;
    }

    /**
     * Returns the delete module flag String.<p>
     *
     * @return the delete module flag as String
     */
    public String getDeleteModule() {

        return m_deleteModule;
    }

    /**
     * Returns the description.<p>
     *
     * @return the description
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the formatter source module package/name.<p>
     *
     * @return the formatter source module package/name
     */
    public String getFormatterSourceModule() {

        return m_formatterSourceModule;
    }

    /**
     * Returns the formatter target module package/name.<p>
     *
     * @return the formatter target module package/name
     */
    public String getFormatterTargetModule() {

        return m_formatterTargetModule;
    }

    /**
     * Returns the group.<p>
     *
     * @return the group
     */
    public String getGroup() {

        return m_group;
    }

    /**
     * Returns the nice name.<p>
     *
     * @return the nice name
     */
    public String getNiceName() {

        return m_niceName;
    }

    /**
     * Returns the package/module name for the clone/target.<p>
     *
     * @return the package/module name for the clone/target
     */
    public String getPackageName() {

        return m_packageName;
    }

    /**
     * Returns the source module package/name (the module to clone).<p>
     *
     * @return the source module package/name (the module to clone)
     */
    public String getSourceModuleName() {

        return m_sourceModuleName;
    }

    /**
     * Returns the source name prefix.<p>
     *
     * @return the source name prefix
     */
    public String getSourceNamePrefix() {

        return m_sourceNamePrefix;
    }

    /**
     * Returns the target name prefix.<p>
     *
     * @return the target name prefix
     */
    public String getTargetNamePrefix() {

        return m_targetNamePrefix;
    }

    /**
     * Returns the rewriteContainerPages.<p>
     *
     * @return the rewriteContainerPages
     */
    public boolean isRewriteContainerPages() {

        return m_rewriteContainerPages;
    }

    /**
     * Sets the action class.<p>
     *
     * @param actionClass the action class
     */
    public void setActionClass(String actionClass) {

        m_actionClass = actionClass;
    }

    /**
     * Sets the changeResourceTypesEverywhere.<p>
     *
     * @param applyChangesEverywhere the changeResourceTypesEverywhere to set
     */
    public void setApplyChangesEverywhere(String applyChangesEverywhere) {

        m_applyChangesEverywhere = applyChangesEverywhere;
    }

    /**
     * Sets the author email.<p>
     *
     * @param authorEmail the author email to set
     */
    public void setAuthorEmail(String authorEmail) {

        m_authorEmail = authorEmail;
    }

    /**
     * Sets the author name.<p>
     *
     * @param authorName the author name to set
     */
    public void setAuthorName(String authorName) {

        m_authorName = authorName;
    }

    /**
     * Sets the change resource types flag.<p>
     *
     * @param changeResourceTypes the change resource types falg to set
     */
    public void setChangeResourceTypes(String changeResourceTypes) {

        m_changeResourceTypes = changeResourceTypes;
    }

    /**
     * Sets the delete module flag.<p>
     *
     * @param deleteModule the delete module flag to set
     */
    public void setDeleteModule(String deleteModule) {

        m_deleteModule = deleteModule;
    }

    /**
     * Sets the description.<p>
     *
     * @param description the description to set
     */
    public void setDescription(String description) {

        m_description = description;
    }

    /**
     * Sets the formatter source module name.<p>
     *
     * @param formatterSourceModule the formatter source module name to set
     */
    public void setFormatterSourceModule(String formatterSourceModule) {

        m_formatterSourceModule = formatterSourceModule;
    }

    /**
     * Sets the formatter target module name.<p>
     *
     * @param formatterTargetModule the formatter target module name to set
     */
    public void setFormatterTargetModule(String formatterTargetModule) {

        m_formatterTargetModule = formatterTargetModule;
    }

    /**
     * Sets the group.<p>
     *
     * @param group the group to set
     */
    public void setGroup(String group) {

        m_group = group;
    }

    /**
     * Sets the nice name.<p>
     *
     * @param niceName the nice name to set
     */
    public void setNiceName(String niceName) {

        m_niceName = niceName;
    }

    /**
     * Sets the package name.<p>
     *
     * @param packageName the package name to set
     */
    public void setPackageName(String packageName) {

        m_packageName = packageName;
    }

    /**
     * Sets the rewriteContainerPages.<p>
     *
     * @param rewriteContainerPages the rewriteContainerPages to set
     */
    public void setRewriteContainerPages(boolean rewriteContainerPages) {

        m_rewriteContainerPages = rewriteContainerPages;
    }

    /**
     * Sets the source module name.<p>
     *
     * @param sourceModuleName the source module name to set
     */
    public void setSourceModuleName(String sourceModuleName) {

        m_sourceModuleName = sourceModuleName;
    }

    /**
     * Sets the source name prefix.<p>
     *
     * @param sourceNamePrefix the source name prefix to set
     */
    public void setSourceNamePrefix(String sourceNamePrefix) {

        m_sourceNamePrefix = sourceNamePrefix;
    }

    /**
     * Sets the target name prefix.<p>
     *
     * @param targetNamePrefix the target name prefix to set
     */
    public void setTargetNamePrefix(String targetNamePrefix) {

        m_targetNamePrefix = targetNamePrefix;
    }

    /**
     * Returns <code>true</code> if the module has been created successful.<p>
     * 
     * @return <code>true</code> if the module has been created successful
     */
    public boolean success() {

        return OpenCms.getModuleManager().getModule(m_packageName) != null;
    }

    /**
     * Adjusts the module configuration file and the fromatter configurations.<p>
     * 
     * @param targetModule the target moodule
     * @param resTypeMap the resource type mapping
     * 
     * @throws CmsException if something goes wrong
     * @throws UnsupportedEncodingException if the file content could not be read with the determined encoding
     */
    private void adjustConfigs(CmsModule targetModule, Map<I_CmsResourceType, I_CmsResourceType> resTypeMap)
    throws CmsException, UnsupportedEncodingException {

        String modPath = CmsWorkplace.VFS_PATH_MODULES + targetModule.getName() + "/";

        if (((m_sourceNamePrefix != null) && (m_targetNamePrefix != null))
            || !m_sourceNamePrefix.equals(m_targetNamePrefix)) {
            // replace resource type names in formatter configurations
            List<CmsResource> resources = getCmsObject().readResources(
                modPath,
                CmsResourceFilter.requireType(OpenCms.getResourceManager().getResourceType(
                    CmsFormatterConfigurationCache.TYPE_FORMATTER_CONFIG).getTypeId()));
            String source = "<Type><!\\[CDATA\\[" + m_sourceNamePrefix;
            String target = "<Type><!\\[CDATA\\[" + m_targetNamePrefix;
            replaceResourceTypeNames(resources, source, target);
            resources.clear();
        }

        // replace resource type names in module configuration
        try {
            CmsResource config = getCmsObject().readResource(
                modPath + CmsADEManager.CONFIG_FILE_NAME,
                CmsResourceFilter.requireType(OpenCms.getResourceManager().getResourceType(
                    CmsADEManager.MODULE_CONFIG_TYPE).getTypeId()));
            for (Map.Entry<I_CmsResourceType, I_CmsResourceType> mapping : resTypeMap.entrySet()) {
                replaceResourceTypeNames(
                    Collections.singletonList(config),
                    mapping.getKey().getTypeName(),
                    mapping.getValue().getTypeName());
            }
        } catch (CmsVfsResourceNotFoundException e) {
            LOG.info(e.getLocalizedMessage(), e);
        }
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

        List<String> newTargetResources = new ArrayList<String>();
        List<String> targetResources = targetModule.getResources();
        for (String modRes : targetResources) {
            String nIcon = iconPaths.get(modRes.substring(modRes.lastIndexOf('/') + 1));
            if (nIcon != null) {
                // the referenced resource is an resource type icon, add the new icon path
                newTargetResources.add(ICON_PATH + nIcon);
            } else if (modRes.contains(sourceModule.getName())) {
                // there is the name in it
                newTargetResources.add(modRes.replaceAll(sourceModule.getName(), targetModule.getName()));
            } else if (modRes.contains(sourcePathPart)) {
                // there is a path in it
                newTargetResources.add(modRes.replaceAll(sourcePathPart, targetPathPart));
            } else {
                // there is whether the path nor the name in it
                newTargetResources.add(modRes);
            }
        }
        targetModule.setResources(newTargetResources);
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

        CmsObject clone = OpenCms.initCmsObject(getCmsObject());
        if (isTrue(m_applyChangesEverywhere)) {
            clone.getRequestContext().setSiteRoot("/");
        }

        for (Map.Entry<I_CmsResourceType, I_CmsResourceType> mapping : resTypeMap.entrySet()) {
            CmsResourceFilter filter = CmsResourceFilter.requireType(mapping.getKey().getTypeId());
            List<CmsResource> resources = clone.readResources("/", filter);
            String sourceSchemaPath = mapping.getKey().getConfiguration().get("schema");
            String targetSchemaPath = mapping.getValue().getConfiguration().get("schema");
            for (CmsResource res : resources) {
                if (lockResource(getCmsObject(), res)) {
                    CmsFile file = getCmsObject().readFile(res);
                    String encoding = CmsLocaleManager.getResourceEncoding(getCmsObject(), file);
                    String content = new String(file.getContents(), encoding);
                    content = content.replaceAll(sourceSchemaPath, targetSchemaPath);
                    file.setContents(content.getBytes(encoding));
                    try {
                        getCmsObject().writeFile(file);
                    } catch (CmsXmlException e) {
                        LOG.error(e.getMessage(), e);
                    }
                    res.setType(mapping.getValue().getTypeId());
                    getCmsObject().writeResource(res);
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
            if (!getCmsObject().existsResource(target)) {
                getCmsObject().copyResource(source, target);
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
    private void cloneExplorerTypes(CmsModule targetModule, Map<String, String> iconPaths, Map<String, String> descKeys) {

        List<CmsExplorerTypeSettings> targetExplorerTypes = targetModule.getExplorerTypes();
        for (CmsExplorerTypeSettings expSetting : targetExplorerTypes) {
            descKeys.put(expSetting.getKey(), expSetting.getKey().replaceFirst(m_sourceNamePrefix, m_targetNamePrefix));
            String newIcon = expSetting.getIcon().replaceFirst(m_sourceNamePrefix, m_targetNamePrefix);
            String newBigIcon = expSetting.getBigIconIfAvailable().replaceFirst(m_sourceNamePrefix, m_targetNamePrefix);
            iconPaths.put(expSetting.getIcon(), newIcon);
            iconPaths.put(expSetting.getBigIconIfAvailable(), newBigIcon);
            expSetting.setName(expSetting.getName().replaceFirst(m_sourceNamePrefix, m_targetNamePrefix));
            String newResourcePage = expSetting.getNewResourcePage();
            if (newResourcePage != null) {
                expSetting.setNewResourcePage(newResourcePage.replaceFirst(m_sourceNamePrefix, m_targetNamePrefix));
            }
            expSetting.setKey(expSetting.getKey().replaceFirst(m_sourceNamePrefix, m_targetNamePrefix));
            expSetting.setIcon(expSetting.getIcon().replaceFirst(m_sourceNamePrefix, m_targetNamePrefix));
            expSetting.setBigIcon(expSetting.getBigIconIfAvailable().replaceFirst(
                m_sourceNamePrefix,
                m_targetNamePrefix));
            expSetting.setNewResourceUri(expSetting.getNewResourceUri().replaceFirst(
                m_sourceNamePrefix,
                m_targetNamePrefix));
            expSetting.setInfo(expSetting.getInfo().replaceFirst(m_sourceNamePrefix, m_targetNamePrefix));
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
     * 
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
                            prop.setStructureValue(prop.getStructureValue().replaceAll(
                                sourceModule.getName(),
                                targetModule.getName()).replaceAll(sourcePathPart, targetPathPart));
                            prop.setResourceValue(prop.getResourceValue().replaceAll(
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
                    sourceResType.getTypeName().replaceFirst(m_sourceNamePrefix, m_targetNamePrefix),
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
                    Messages.get().getBundle().key(
                        Messages.ERR_UNKNOWN_RESTYPE_CLASS_2,
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
            if (!getCmsObject().existsResource(targetClassesPath)) {
                getCmsObject().createResource(targetClassesPath, folderId);
            }
        }
        // move exiting content into new classes sub-folder
        List<CmsResource> propertyFiles = getCmsObject().readResources(sourceClassesPath, CmsResourceFilter.ALL);
        for (CmsResource res : propertyFiles) {
            if (!getCmsObject().existsResource(targetClassesPath + res.getName())) {
                getCmsObject().copyResource(res.getRootPath(), targetClassesPath + res.getName());
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
            getCmsObject().deleteResource(
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
        getCmsObject().deleteResource(topSourceClassesPath, CmsResource.DELETE_PRESERVE_SIBLINGS);
    }

    /**
     * Returns <code>true</code> if form imput is selected, checked, on or yes.<p>
     * 
     * @param value the value to check
     * 
     * @return <code>true</code> if form imput is selected, checked, on or yes
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
            // change the lock 
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
     * 
     * @throws CmsException if something gows wrong
     */
    private List<CmsResource> renameXmlVfsBundles(List<CmsResource> resources, CmsModule targetModule, String name)
    throws CmsException {

        for (CmsResource res : resources) {
            String newName = res.getName().replaceAll(name, targetModule.getName());
            String targetRootPath = CmsResource.getFolderPath(res.getRootPath()) + newName;
            if (!getCmsObject().existsResource(targetRootPath)) {
                getCmsObject().moveResource(res.getRootPath(), targetRootPath);
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

        CmsResource formatterSourceFolder = getCmsObject().readResource(
            "/system/modules/" + m_formatterSourceModule + "/");
        CmsResource formatterTargetFolder = getCmsObject().readResource(
            "/system/modules/" + m_formatterTargetModule + "/");
        for (I_CmsResourceType type : targetModule.getResourceTypes()) {
            String schemaPath = type.getConfiguration().get("schema");
            CmsResource res = getCmsObject().readResource(schemaPath);
            CmsFile file = getCmsObject().readFile(res);
            String encoding = CmsLocaleManager.getResourceEncoding(getCmsObject(), file);
            String content = new String(file.getContents(), encoding);
            content = content.replaceAll(formatterSourceFolder.getRootPath(), formatterTargetFolder.getRootPath());
            file.setContents(content.getBytes(encoding));
            getCmsObject().writeFile(file);
        }
    }

    /**
     * Initializes a thread to find and replace all occurrence of the module's path.<p>
     * 
     * @throws CmsException 
     * @throws UnsupportedEncodingException 
     */
    private void replaceModuleName() throws CmsException, UnsupportedEncodingException {

        CmsResourceFilter filter = CmsResourceFilter.ALL.addRequireFile().addExcludeState(CmsResource.STATE_DELETED).addRequireTimerange().addRequireVisible();
        List<CmsResource> resources = getCmsObject().readResources(
            CmsWorkplace.VFS_PATH_MODULES + m_packageName + "/",
            filter);
        for (CmsResource resource : resources) {
            CmsFile file = getCmsObject().readFile(resource);
            byte[] contents = file.getContents();
            String encoding = CmsLocaleManager.getResourceEncoding(getCmsObject(), file);
            String content = new String(contents, encoding);
            Matcher matcher = Pattern.compile(m_sourceModuleName).matcher(content);
            if (matcher.find()) {
                contents = matcher.replaceAll(m_packageName).getBytes(encoding);
                if (lockResource(getCmsObject(), file)) {
                    file.setContents(contents);
                    getCmsObject().writeFile(file);
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
                CmsFile file = getCmsObject().readFile(resource);
                String encoding = CmsLocaleManager.getResourceEncoding(getCmsObject(), file);
                String oldContent = new String(file.getContents(), encoding);
                String newContent = oldContent.replaceAll(sourceModulePath, targetModulePath);
                Matcher matcher = Pattern.compile(CmsUUID.UUID_REGEX).matcher(newContent);
                newContent = matcher.replaceAll("");
                newContent = newContent.replaceAll("<uuid></uuid>", "");
                if (!oldContent.equals(newContent)) {
                    file.setContents(newContent.getBytes(encoding));
                    if (!resource.getRootPath().startsWith(CmsWorkplace.VFS_PATH_SYSTEM)) {
                        if (lockResource(getCmsObject(), resource)) {
                            getCmsObject().writeFile(file);
                        }
                    } else {
                        getCmsObject().writeFile(file);
                    }
                }
            }
        }
    }

    /**
     * Replaces the source name with the target name within all the given resources.<p>
     * 
     * @param resources the resource to consider
     * @param sourceName the source value
     * @param targetName the target value
     *  
     * @throws CmsException if sth. goes wrong
     * @throws UnsupportedEncodingException
     */
    private void replaceResourceTypeNames(List<CmsResource> resources, String sourceName, String targetName)
    throws CmsException, UnsupportedEncodingException {

        for (CmsResource resource : resources) {
            CmsFile file = getCmsObject().readFile(resource);
            String encoding = CmsLocaleManager.getResourceEncoding(getCmsObject(), file);
            String content = new String(file.getContents(), encoding);
            content = content.replaceAll(sourceName, targetName);
            file.setContents(content.getBytes(encoding));
            lockResource(getCmsObject(), file);
            getCmsObject().writeFile(file);
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
            CmsFile file = getCmsObject().readFile(resource);
            String encoding = CmsLocaleManager.getResourceEncoding(getCmsObject(), file);
            String content = new String(file.getContents(), encoding);
            for (Map.Entry<String, String> entry : descKeys.entrySet()) {
                content = content.replaceAll(entry.getKey(), entry.getValue());
            }
            file.setContents(content.getBytes(encoding));
            getCmsObject().writeFile(file);
        }
    }

}

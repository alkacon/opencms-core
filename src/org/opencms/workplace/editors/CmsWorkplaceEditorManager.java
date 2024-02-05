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

package org.opencms.workplace.editors;

import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;

/**
 * The editor manager stores information about all available configured editors in OpenCms.<p>
 *
 * This class provides methods and constants to select the right editor according to:
 * <ul>
 * <li>the user preferences</li>
 * <li>the users current browser</li>
 * <li>the resource type</li>
 * <li>the editor rankings</li>
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsWorkplaceEditorManager {

    /** The filename of the editor configuration XML file. */
    public static final String EDITOR_CONFIGURATION_FILENAME = "editor_configuration.xml";

    /** The filename of the editor JSP. */
    public static final String EDITOR_FILENAME = "editor.jsp";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsWorkplaceEditorManager.class);

    /** The editor configurations. */
    private List<CmsWorkplaceEditorConfiguration> m_editorConfigurations;

    /** The preferred editor configurations. */
    private Map<String, CmsWorkplaceEditorConfiguration> m_preferredEditors;

    /**
     * Creates a new editor manager.<p>
     *
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     */
    public CmsWorkplaceEditorManager(CmsObject cms) {

        // get all subfolders of the workplace editor folder
        List<CmsResource> editorFolders;
        try {
            editorFolders = cms.getSubFolders(CmsEditor.PATH_EDITORS);
        } catch (CmsException e) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_READ_EDITIR_FOLDER_FAILED_1, CmsEditor.PATH_EDITORS));
            // can not throw exception here since then OpenCms would not even start in shell mode (runlevel 2)
            editorFolders = new ArrayList<CmsResource>();
        }

        m_editorConfigurations = new ArrayList<CmsWorkplaceEditorConfiguration>(editorFolders.size());

        // try to read the configuration files and create configuration objects for valid configurations
        Iterator<CmsResource> i = editorFolders.iterator();
        while (i.hasNext()) {
            CmsResource currentFolder = i.next();
            String folderName = CmsEditor.PATH_EDITORS + currentFolder.getName();
            if (!folderName.endsWith("/")) {
                folderName += "/";
            }
            CmsFile configFile = null;
            try {
                configFile = cms.readFile(
                    folderName + EDITOR_CONFIGURATION_FILENAME,
                    CmsResourceFilter.IGNORE_EXPIRATION);
            } catch (CmsException e) {
                // no configuration file present, ignore this folder
                if (LOG.isInfoEnabled()) {
                    LOG.info(e.getLocalizedMessage(), e);
                }
                continue;
            }
            // get the file contents
            byte[] xmlData = configFile.getContents();
            CmsWorkplaceEditorConfiguration editorConfig = new CmsWorkplaceEditorConfiguration(
                xmlData,
                folderName + EDITOR_FILENAME,
                currentFolder.getName());
            if (editorConfig.isValidConfiguration()) {
                m_editorConfigurations.add(editorConfig);
            }
        }
        m_preferredEditors = new HashMap<String, CmsWorkplaceEditorConfiguration>(m_editorConfigurations.size());
    }

    /**
     * Checks whether GWT widgets are available for all fields of a content.<p>
     *
     * @param cms the current CMS context
     * @param resource the resource to check
     *
     * @return false if for some fields the new Acacia widgets are not available
     */
    public static boolean checkAcaciaEditorAvailable(CmsObject cms, CmsResource resource) {

        boolean result = false;
        if (resource == null) {
            try {
                // we want a stack trace
                throw new Exception();
            } catch (Exception e) {
                LOG.error("Can't check widget availability because resource is null!", e);
            }
        } else {
            try {
                CmsFile file = (resource instanceof CmsFile) ? (CmsFile)resource : cms.readFile(resource);
                if (file.getContents().length > 0) {
                    CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
                    if (!content.getContentDefinition().getContentHandler().isAcaciaEditorDisabled()) {
                        result = true;
                    }
                }
            } catch (CmsException e) {
                LOG.info(
                    "error thrown in checkAcaciaEditorAvailable for " + resource + " : " + e.getLocalizedMessage(),
                    e);
                result = true;
            }
        }
        return result;
    }

    /**
     * Returns a map of configurable editors for the workplace preferences dialog.<p>
     *
     * This map has the resource type name as key, the value is a sorted map with
     * the ranking as key and a CmsWorkplaceEditorConfiguration object as value.<p>
     *
     * @return configurable editors for the workplace preferences dialog
     */
    public Map<String, SortedMap<Float, CmsWorkplaceEditorConfiguration>> getConfigurableEditors() {

        Map<String, SortedMap<Float, CmsWorkplaceEditorConfiguration>> configurableEditors = new HashMap<String, SortedMap<Float, CmsWorkplaceEditorConfiguration>>();
        Iterator<CmsWorkplaceEditorConfiguration> i = m_editorConfigurations.iterator();
        while (i.hasNext()) {
            CmsWorkplaceEditorConfiguration currentConfig = i.next();
            // get all resource types specified for the current editor configuration
            Iterator<String> k = currentConfig.getResourceTypes().keySet().iterator();
            while (k.hasNext()) {
                // key is the current resource type of the configuration
                String key = k.next();

                // check if the current resource type is only a reference to another resource type
                CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(key);
                if ((settings == null) || CmsStringUtil.isNotEmpty(settings.getReference())) {
                    // skip this resource type
                    continue;
                }

                if ((currentConfig.getMappingForResourceType(key) == null)
                    || currentConfig.getMappingForResourceType(key).equals(key)) {
                    // editor is configurable for specified resource type
                    SortedMap<Float, CmsWorkplaceEditorConfiguration> editorConfigs = configurableEditors.get(key);
                    if (editorConfigs == null) {
                        // no configuration map present for resource type, create one
                        editorConfigs = new TreeMap<Float, CmsWorkplaceEditorConfiguration>();
                    }
                    // put the current editor configuration to the resource map with ranking value as key
                    editorConfigs.put(Float.valueOf(currentConfig.getRankingForResourceType(key)), currentConfig);
                    // put the resource map to the result map with resource type as key
                    configurableEditors.put(key, editorConfigs);
                }
            }
        }
        return configurableEditors;
    }

    /**
     * Gets the editor configuration with the given name.<p>
     *
     * @param name the name of the editor configuration
     *
     * @return the editor configuration
     */
    public CmsWorkplaceEditorConfiguration getEditorConfiguration(String name) {

        for (CmsWorkplaceEditorConfiguration config : m_editorConfigurations) {
            if (name.equals(config.getName())) {
                return config;
            }
        }
        return null;
    }

    /**
     * Gets the value of a global editor configuration parameter.
     *
     * @param cms the CMS context
     * @param editor the editor name
     * @param param the name of the parameter
     *
     * @return the editor parameter value
     */
    public String getEditorParameter(CmsObject cms, String editor, String param) {

        String path = OpenCms.getSystemInfo().getConfigFilePath(cms, "editors/" + editor + ".properties");
        CmsVfsMemoryObjectCache cache = CmsVfsMemoryObjectCache.getVfsMemoryObjectCache();
        CmsParameterConfiguration config = (CmsParameterConfiguration)cache.getCachedObject(cms, path);
        if (config == null) {
            try {
                CmsFile file = cms.readFile(path);
                try (ByteArrayInputStream input = new ByteArrayInputStream(file.getContents())) {
                    config = new CmsParameterConfiguration(input); // Uses ISO-8859-1, should be OK for config parameters
                    cache.putCachedObject(cms, path, config);
                }
            } catch (CmsVfsResourceNotFoundException e) {
                return null;
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                return null;
            }
        }
        return config.getString(param, null);
    }

    /**
     * Returns the editor URI for the current resource type.<p>
     *
     * @param context the request context
     * @param userAgent the user agent String that identifies the browser
     * @return a valid editor URI for the resource type or null, if no editor matches
     */
    public String getWidgetEditor(CmsRequestContext context, String userAgent) {

        // step 1: check if the user specified a preferred editor for the resource type xmlpage
        CmsUserSettings settings = new CmsUserSettings(context.getCurrentUser());
        String resourceType = CmsResourceTypeXmlPage.getStaticTypeName();
        String preferredEditorSetting = settings.getPreferredEditor(resourceType);
        if (preferredEditorSetting == null) {
            // no preferred editor setting found for this resource type, look for mapped resource type preferred editor
            Iterator<CmsWorkplaceEditorConfiguration> i = m_editorConfigurations.iterator();
            while (i.hasNext()) {
                CmsWorkplaceEditorConfiguration currentConfig = i.next();
                String mapping = currentConfig.getMappingForResourceType(resourceType);
                if (mapping != null) {
                    preferredEditorSetting = settings.getPreferredEditor(mapping);
                }
                if (preferredEditorSetting != null) {
                    break;
                }
            }
        }
        if (preferredEditorSetting != null) {
            CmsWorkplaceEditorConfiguration preferredConf = filterPreferredEditor(preferredEditorSetting);
            if ((preferredConf != null) && preferredConf.isWidgetEditor() && preferredConf.matchesBrowser(userAgent)) {
                // return preferred editor only if it matches the current users browser
                return preferredConf.getWidgetEditor();
            }
        }

        // step 2: filter editors for the given resoure type
        SortedMap<Float, CmsWorkplaceEditorConfiguration> filteredEditors = filterEditorsForResourceType(resourceType);

        // step 3: check if one of the editors matches the current users browser
        while (filteredEditors.size() > 0) {
            // check editor configuration with highest ranking
            Float key = filteredEditors.lastKey();
            CmsWorkplaceEditorConfiguration conf = filteredEditors.get(key);
            if (conf.isWidgetEditor() && conf.matchesBrowser(userAgent)) {
                return conf.getWidgetEditor();
            }
            filteredEditors.remove(key);
        }

        // no valid editor found
        return null;
    }

    /**
     * Checks if there is an editor which can process the given resource.<p>
     *
     * @param res the resource
     *
     * @return true if the given resource can be edited with one of the configured editors
     */
    public boolean isEditorAvailableForResource(CmsResource res) {

        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(res);
        String typeName = type.getTypeName();
        for (CmsWorkplaceEditorConfiguration editorConfig : m_editorConfigurations) {
            if (editorConfig.matchesResourceType(typeName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the default editor URI for the current resource type.<p>
     *
     * @param context the request context
     * @param resourceType the current resource type
     * @param userAgent the user agent String that identifies the browser
     * @return a valid default editor URI for the resource type or null, if no editor matches
     */
    protected String getDefaultEditorUri(CmsRequestContext context, String resourceType, String userAgent) {

        SortedMap<Float, CmsWorkplaceEditorConfiguration> filteredEditors = filterEditorsForResourceType(resourceType);
        while (filteredEditors.size() > 0) {
            // get the configuration with the lowest key value from the map
            Float key = filteredEditors.firstKey();
            CmsWorkplaceEditorConfiguration conf = filteredEditors.get(key);
            // match the found configuration with the current users browser
            if (conf.matchesBrowser(userAgent)) {
                return conf.getEditorUri();
            }
            filteredEditors.remove(key);
        }
        if (context == null) {
            // this is just so that all parameters are used, signature should be identical to getEditorUri(...)
            return null;
        }
        // no valid default editor found
        return null;
    }

    /**
     * Returns the editor configuration objects.<p>
     *
     * @return the editor configuration objects
     */
    protected List<CmsWorkplaceEditorConfiguration> getEditorConfigurations() {

        return m_editorConfigurations;
    }

    /**
     * Returns the editor URI for the current resource type.<p>
     *
     * @param context the request context
     * @param resourceType the current resource type
     * @param userAgent the user agent String that identifies the browser
     * @return a valid editor URI for the resource type or null, if no editor matches
     */
    protected String getEditorUri(CmsRequestContext context, String resourceType, String userAgent) {

        // step 1: check if the user specified a preferred editor for the given resource type
        CmsUserSettings settings = new CmsUserSettings(context.getCurrentUser());
        String preferredEditorSetting = settings.getPreferredEditor(resourceType);
        if (preferredEditorSetting == null) {
            // no preferred editor setting found for this resource type, look for mapped resource type preferred editor
            Iterator<CmsWorkplaceEditorConfiguration> i = m_editorConfigurations.iterator();
            while (i.hasNext()) {
                CmsWorkplaceEditorConfiguration currentConfig = i.next();
                String mapping = currentConfig.getMappingForResourceType(resourceType);
                if (mapping != null) {
                    preferredEditorSetting = settings.getPreferredEditor(mapping);
                }
                if (preferredEditorSetting != null) {
                    break;
                }
            }
        }
        if (preferredEditorSetting != null) {
            CmsWorkplaceEditorConfiguration preferredConf = filterPreferredEditor(preferredEditorSetting);
            if ((preferredConf != null) && preferredConf.matchesBrowser(userAgent)) {
                // return preferred editor only if it matches the current users browser
                return preferredConf.getEditorUri();
            }
        }

        // step 2: filter editors for the given resoure type
        SortedMap<Float, CmsWorkplaceEditorConfiguration> filteredEditors = filterEditorsForResourceType(resourceType);

        // step 3: check if one of the editors matches the current users browser
        while (filteredEditors.size() > 0) {
            // check editor configuration with highest ranking
            Float key = filteredEditors.lastKey();
            CmsWorkplaceEditorConfiguration conf = filteredEditors.get(key);
            if (conf.matchesBrowser(userAgent)) {
                return conf.getEditorUri();
            }
            filteredEditors.remove(key);
        }

        // no valid editor found
        return null;
    }

    /**
     * Filters the matching editors for the given resource type from the list of all available editors.<p>
     *
     * @param resourceType the resource type to filter
     * @return a map of filtered editor configurations sorted asceding by the ranking for the current resource type, with the (Float) ranking as key
     */
    private SortedMap<Float, CmsWorkplaceEditorConfiguration> filterEditorsForResourceType(String resourceType) {

        SortedMap<Float, CmsWorkplaceEditorConfiguration> filteredEditors = new TreeMap<Float, CmsWorkplaceEditorConfiguration>();
        Iterator<CmsWorkplaceEditorConfiguration> i = m_editorConfigurations.iterator();
        while (i.hasNext()) {
            CmsWorkplaceEditorConfiguration currentConfig = i.next();
            if (currentConfig.matchesResourceType(resourceType)) {
                float key = currentConfig.getRankingForResourceType(resourceType);
                if (key >= 0) {
                    filteredEditors.put(Float.valueOf(key), currentConfig);
                }
            }
        }
        return filteredEditors;
    }

    /**
     * Filters the preferred editor from the list of all available editors.<p>
     *
     * @param preferredEditor the preferred editor identification String
     * @return the preferred editor configuration object or null, if none is found
     */
    private CmsWorkplaceEditorConfiguration filterPreferredEditor(String preferredEditor) {

        if (m_preferredEditors.size() == 0) {
            Iterator<CmsWorkplaceEditorConfiguration> i = m_editorConfigurations.iterator();
            while (i.hasNext()) {
                CmsWorkplaceEditorConfiguration currentConfig = i.next();
                m_preferredEditors.put(currentConfig.getEditorUri(), currentConfig);
            }
        }
        return m_preferredEditors.get(preferredEditor);
    }

}

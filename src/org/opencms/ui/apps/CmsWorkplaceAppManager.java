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
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.apps;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleManager;
import org.opencms.ui.CmsUserIconHelper;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.actions.CmsContextMenuActionItem;
import org.opencms.ui.actions.I_CmsDefaultAction;
import org.opencms.ui.apps.cacheadmin.CmsCacheAdminConfiguration;
import org.opencms.ui.apps.cacheadmin.CmsCacheFolder;
import org.opencms.ui.apps.cacheadmin.CmsCacheViewFlexConfiguration;
import org.opencms.ui.apps.cacheadmin.CmsCacheViewImageConfiguration;
import org.opencms.ui.apps.datesearch.CmsDateSearchConfiguration;
import org.opencms.ui.apps.dbmanager.CmsDbExportConfiguration;
import org.opencms.ui.apps.dbmanager.CmsDbImportHTTPConfiguration;
import org.opencms.ui.apps.dbmanager.CmsDbImportServerConfiguration;
import org.opencms.ui.apps.dbmanager.CmsDbManagerConfiguration;
import org.opencms.ui.apps.dbmanager.CmsDbManagerFolder;
import org.opencms.ui.apps.dbmanager.CmsDbPropertiesAppConfiguration;
import org.opencms.ui.apps.dbmanager.CmsDbRemovePubLocksConfiguration;
import org.opencms.ui.apps.dbmanager.CmsDbStaticExportConfiguration;
import org.opencms.ui.apps.dbmanager.CmsDbSynchronizationConfiguration;
import org.opencms.ui.apps.dbmanager.sqlconsole.CmsSqlConsoleAppConfiguration;
import org.opencms.ui.apps.filehistory.CmsFileHistoryConfiguration;
import org.opencms.ui.apps.git.CmsGitAppConfiguration;
import org.opencms.ui.apps.linkvalidation.CmsLinkInFolderValidationConfiguration;
import org.opencms.ui.apps.linkvalidation.CmsLinkValidationConfiguration;
import org.opencms.ui.apps.linkvalidation.CmsLinkValidationExternalConfiguration;
import org.opencms.ui.apps.linkvalidation.CmsLinkValidationFolder;
import org.opencms.ui.apps.lists.CmsListManagerConfiguration;
import org.opencms.ui.apps.logfile.CmsLogFileConfiguration;
import org.opencms.ui.apps.modules.CmsModuleAppConfiguration;
import org.opencms.ui.apps.projects.CmsProjectManagerConfiguration;
import org.opencms.ui.apps.projects.CmsProjectOverviewConfiguration;
import org.opencms.ui.apps.publishqueue.CmsPublishQueueConfiguration;
import org.opencms.ui.apps.resourcetypes.CmsResourceTypeAppConfiguration;
import org.opencms.ui.apps.scheduler.CmsScheduledJobsAppConfig;
import org.opencms.ui.apps.search.CmsSourceSearchAppConfiguration;
import org.opencms.ui.apps.searchindex.CmsSearchindexAppConfiguration;
import org.opencms.ui.apps.sessions.CmsBroadCastConfigurtion;
import org.opencms.ui.apps.shell.CmsShellAppConfiguration;
import org.opencms.ui.apps.sitemanager.CmsSiteManagerConfiguration;
import org.opencms.ui.apps.unusedcontentfinder.CmsUnusedContentFinderConfiguration;
import org.opencms.ui.apps.user.CmsAccountsAppConfiguration;
import org.opencms.ui.apps.userdata.CmsUserDataAppConfiguration;
import org.opencms.ui.contextmenu.CmsContextMenuItemProviderGroup;
import org.opencms.ui.contextmenu.I_CmsContextMenuItem;
import org.opencms.ui.contextmenu.I_CmsContextMenuItemProvider;
import org.opencms.ui.editors.CmsAcaciaEditor;
import org.opencms.ui.editors.CmsSourceEditor;
import org.opencms.ui.editors.CmsXmlContentEditor;
import org.opencms.ui.editors.CmsXmlPageEditor;
import org.opencms.ui.editors.I_CmsEditor;
import org.opencms.ui.editors.messagebundle.CmsMessageBundleEditor;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.tools.CmsTool;
import org.opencms.workplace.tools.CmsToolManager;
import org.opencms.workplace.tools.I_CmsToolHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * The workplace app manager.<p>
 */
public class CmsWorkplaceAppManager {

    /**
     * Comparator for configuration objects implementing I_CmsHasOrder.<p>
     *
     * @param <T> the type to compare
     */
    public static class ConfigurationComparator<T extends I_CmsHasOrder> implements Comparator<T> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(I_CmsHasOrder o1, I_CmsHasOrder o2) {

            return ComparisonChain.start().compare(o1.getOrder(), o2.getOrder()).result();
        }
    }

    /**
     * Wrapper for the navigation state.<p>
     */
    public static class NavigationState {

        /** The parameter separator. */
        public static final String PARAM_SEPARATOR = "/";

        /** The state parameters. */
        private String m_params = "";

        /** The view/app name. */
        private String m_viewName = "";

        /**
         * Constructor.<p>
         *
         * @param stateString the state string to parse
         */
        public NavigationState(String stateString) {

            if (stateString.startsWith("!")) {
                stateString = stateString.substring(1);
            }
            int separatorPos = stateString.indexOf(PARAM_SEPARATOR);
            if (separatorPos > 0) {
                m_viewName = stateString.substring(0, separatorPos);
                m_params = stateString.substring(separatorPos + 1);
            } else {
                m_viewName = stateString;
            }
            if (m_viewName.endsWith("/")) {
                m_viewName = m_viewName.substring(0, m_viewName.length() - 1);
            }
        }

        /**
         * Returns the parameter part of the state.<p>
         *
         * @return the parameters
         */
        String getParams() {

            return m_params;
        }

        /**
         * Returns the view name.<p>
         *
         * @return the view name
         */
        String getViewName() {

            return m_viewName;

        }
    }

    /** The administration category id. */
    public static final String ADMINISTRATION_CATEGORY_ID = "Administration";

    /** The legacy category id. */
    public static final String LEGACY_CATEGORY_ID = "Legacy";

    /** The main category id. */
    public static final String MAIN_CATEGORY_ID = "Main";

    /** The toolbar.css resource name. */
    public static final String TOOLBAR_CSS = "css/toolbar.css";

    /** The workplace app settings additional info key. */
    public static String WORKPLACE_APP_SETTINGS_KEY = "WORKPLACE_APP_SETTINGS";

    /** The workplace CSS module parameter name. */
    public static String WORKPLACE_CSS_PARAM = "workplace-css";

    /** The logger for this class. */
    protected static Log LOG = CmsLog.getLog(CmsWorkplaceAppManager.class.getName());

    /** The default quick launch apps, these can be overridden by the user. */
    private static final String[] DEFAULT_USER_APPS = new String[] {
        CmsAccountsAppConfiguration.APP_ID,
        CmsModuleAppConfiguration.APP_ID};

    /** The available editors. */
    private static final I_CmsEditor[] EDITORS = new I_CmsEditor[] {
        new CmsAcaciaEditor(),
        new CmsSourceEditor(),
        new CmsXmlContentEditor(),
        new CmsXmlPageEditor(),
        new CmsMessageBundleEditor()};

    /** Legacy apps explicitly hidden from new workplace. */
    private static final Set<String> LEGACY_BLACKLIST = Sets.newConcurrentHashSet(
        Arrays.asList(
            "/accounts",
            "/contenttools",
            "/git",
            "/scheduler",
            "/galleryoverview",
            "/projects",
            "/project_overview",
            "/history",
            "/sites",
            "/cache",
            "/publishqueue",
            "/database",
            "/linkvalidation",
            "/workplace",
            "/modules",
            "/searchindex"));

    /** The additional info key for the user quick launch apps. */
    private static final String QUICK_LAUCH_APPS_KEY = "quick_launch_apps";

    /** The standard quick launch apps. */
    private static final String[] STANDARD_APPS = new String[] {
        CmsPageEditorConfiguration.APP_ID,
        CmsSitemapEditorConfiguration.APP_ID,
        CmsFileExplorerConfiguration.APP_ID,
        CmsAppHierarchyConfiguration.APP_ID};

    /** The additional style sheets. */
    private Collection<String> m_additionalStyleSheets;

    /** The admin cms context. */
    private CmsObject m_adminCms;

    /** The app categories. */
    private Map<String, I_CmsAppCategory> m_appCategories;

    /** The configured apps. */
    private Map<String, I_CmsWorkplaceAppConfiguration> m_appsById = Maps.newHashMap();

    /** The user icon helper. */
    private CmsUserIconHelper m_iconHelper;

    /** The standard quick launch apps. */
    private List<I_CmsWorkplaceAppConfiguration> m_standardQuickLaunchApps;

    /** The additional workplace CSS URIs. */
    private Set<String> m_workplaceCssUris;

    /** Menu item manager. */
    private CmsContextMenuItemProviderGroup m_workplaceMenuItemProvider;

    /**
     * Constructor.<p>
     *
     * @param adminCms the admin cms context
     *
     * @throws CmsException in case initializing the cms object fails
     */
    public CmsWorkplaceAppManager(CmsObject adminCms)
    throws CmsException {

        m_adminCms = adminCms;
        m_iconHelper = new CmsUserIconHelper(OpenCms.initCmsObject(m_adminCms));
        m_workplaceMenuItemProvider = new CmsContextMenuItemProviderGroup();
        m_workplaceMenuItemProvider.addProvider(CmsDefaultMenuItemProvider.class);
        m_workplaceMenuItemProvider.initialize();
    }

    /**
     * Constructor for testing only.<p>
     */
    protected CmsWorkplaceAppManager() {

        // nothing to do
    }

    /**
     * Returns the additional style sheets provided by I_CmsWorkplaceStylesheetProvider services.<p>
     *
     * @return the additional style sheets
     */
    public Collection<String> getAdditionalStyleSheets() {

        if (m_additionalStyleSheets == null) {
            Set<String> stylesheets = new LinkedHashSet<>();
            for (I_CmsWorkplaceStylesheetProvider provider : ServiceLoader.load(
                I_CmsWorkplaceStylesheetProvider.class)) {
                stylesheets.addAll(provider.getStylesheets());
            }
            m_additionalStyleSheets = Collections.unmodifiableSet(stylesheets);
        }
        return m_additionalStyleSheets;
    }

    /**
     * Returns the app configuration with the given id.<p>
     *
     * @param appId the app id
     *
     * @return the app configuration
     */
    public I_CmsWorkplaceAppConfiguration getAppConfiguration(String appId) {

        return m_appsById.get(appId);
    }

    /**
     * Returns the app configuration instances for the given ids.<p>
     *
     * @param appIds the app ids
     *
     * @return the app configurations
     */
    public List<I_CmsWorkplaceAppConfiguration> getAppConfigurations(String... appIds) {

        List<I_CmsWorkplaceAppConfiguration> result = new ArrayList<I_CmsWorkplaceAppConfiguration>();
        for (int i = 0; i < appIds.length; i++) {
            I_CmsWorkplaceAppConfiguration config = getAppConfiguration(appIds[i]);
            if (config != null) {
                result.add(config);
            }
        }
        return result;
    }

    /**
     * Returns the user app setting of the given type.<p>
     *
     * @param cms the cms context
     * @param type the app setting type
     *
     * @return the app setting
     *
     * @throws InstantiationException in case instantiating the settings type fails
     * @throws IllegalAccessException in case the settings default constructor is not accessible
     */
    public <T extends I_CmsAppSettings> T getAppSettings(CmsObject cms, Class<T> type)
    throws InstantiationException, IllegalAccessException {

        CmsUser user = cms.getRequestContext().getCurrentUser();
        CmsUserSettings settings = new CmsUserSettings(user);
        String settingsString = settings.getAdditionalPreference(type.getName(), true);
        T result = type.newInstance();

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(settingsString)) {

            result.restoreSettings(settingsString);

        }

        return result;
    }

    /**
     * Returns the configured categories.<p>
     *
     * @return the app categories
     */
    public Collection<I_CmsAppCategory> getCategories() {

        return Collections.unmodifiableCollection(m_appCategories.values());
    }

    /**
     * Returns the default action for the given context if available.<p>
     *
     * @param context the dialog context
     *
     * @return the default action
     */
    public I_CmsDefaultAction getDefaultAction(I_CmsDialogContext context) {

        return getDefaultAction(context, getMenuItemProvider());
    }

    /**
     * Returns the default action for the given context if available.<p>
     *
     * @param context the dialog context
     * @param menuItemProvider the menu item provider
     *
     * @return the default action
     */
    public I_CmsDefaultAction getDefaultAction(
        I_CmsDialogContext context,
        I_CmsContextMenuItemProvider menuItemProvider) {

        I_CmsDefaultAction result = null;
        int resultRank = -1;
        if (context.getResources().size() == 1) {
            for (I_CmsContextMenuItem menuItem : menuItemProvider.getMenuItems()) {
                if ((menuItem instanceof CmsContextMenuActionItem)
                    && (((CmsContextMenuActionItem)menuItem).getWorkplaceAction() instanceof I_CmsDefaultAction)) {
                    I_CmsDefaultAction action = (I_CmsDefaultAction)((CmsContextMenuActionItem)menuItem).getWorkplaceAction();
                    if (action.getVisibility(context).isActive()) {
                        if (result == null) {
                            result = action;
                            resultRank = action.getDefaultActionRank(context);
                        } else {
                            int rank = action.getDefaultActionRank(context);
                            if (rank > resultRank) {
                                result = action;
                                resultRank = rank;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Gets all configured quick launch apps, independent of the current user.<p>
     *
     * @return the quick launch apps
     */
    public List<I_CmsWorkplaceAppConfiguration> getDefaultQuickLaunchConfigurations() {

        if (m_standardQuickLaunchApps == null) {

            m_standardQuickLaunchApps = Collections.unmodifiableList(getAppConfigurations(STANDARD_APPS));
        }
        return m_standardQuickLaunchApps;
    }

    /**
     * Returns the editor for the given resource.<p>
     *
     * @param cms the CMS context
     * @param resource the resource to edit
     * @param plainText if plain text editing is required
     *
     * @return the editor
     */
    public I_CmsEditor getEditorForResource(CmsObject cms, CmsResource resource, boolean plainText) {

        List<I_CmsEditor> editors = new ArrayList<I_CmsEditor>();
        for (int i = 0; i < EDITORS.length; i++) {
            try {
                if (EDITORS[i].matchesResource(cms, resource, plainText)) {
                    editors.add(EDITORS[i]);
                }
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        I_CmsEditor result = null;
        if (editors.size() == 1) {
            result = editors.get(0);
        } else if (editors.size() > 1) {
            Collections.sort(editors, new Comparator<I_CmsEditor>() {

                public int compare(I_CmsEditor o1, I_CmsEditor o2) {

                    return o1.getPriority() > o2.getPriority() ? -1 : 1;
                }
            });
            result = editors.get(0);
        }
        return result;
    }

    /**
     * Returns the editor for the given resource type.<p>
     *
     * @param type the resource type to edit
     * @param plainText if plain text editing is required
     *
     * @return the editor
     */
    public I_CmsEditor getEditorForType(I_CmsResourceType type, boolean plainText) {

        List<I_CmsEditor> editors = new ArrayList<I_CmsEditor>();
        for (int i = 0; i < EDITORS.length; i++) {
            if (EDITORS[i].matchesType(type, plainText)) {
                editors.add(EDITORS[i]);
            }
        }
        I_CmsEditor result = null;
        if (editors.size() == 1) {
            result = editors.get(0);
        } else if (editors.size() > 1) {
            Collections.sort(editors, new Comparator<I_CmsEditor>() {

                public int compare(I_CmsEditor o1, I_CmsEditor o2) {

                    return o1.getPriority() > o2.getPriority() ? -1 : 1;
                }
            });
            result = editors.get(0);
        }
        return result;
    }

    /**
     * Gets the menu item provider for the workplace.<p>
     *
     * @return the menu item provider
     */
    public I_CmsContextMenuItemProvider getMenuItemProvider() {

        return m_workplaceMenuItemProvider;
    }

    /**
     * Gets the configured quick launch apps which are visible for the current user.<p>
     *
     * @param cms the current CMS context
     * @return the list of available quick launch apps
     */
    public List<I_CmsWorkplaceAppConfiguration> getQuickLaunchConfigurations(CmsObject cms) {

        List<I_CmsWorkplaceAppConfiguration> result = new ArrayList<I_CmsWorkplaceAppConfiguration>();
        result.addAll(getDefaultQuickLaunchConfigurations());
        result.addAll(getUserQuickLauchConfigurations(cms));
        Iterator<I_CmsWorkplaceAppConfiguration> it = result.iterator();
        while (it.hasNext()) {
            I_CmsWorkplaceAppConfiguration appConfig = it.next();
            CmsAppVisibilityStatus visibility = appConfig.getVisibility(cms);
            if (!visibility.isVisible()) {
                it.remove();
            }
        }
        return result;
    }

    /**
     * Returns the user icon helper.<p>
     *
     * @return the user icon helper
     */
    public CmsUserIconHelper getUserIconHelper() {

        return m_iconHelper;
    }

    /**
     * Returns all available workplace apps.<p>
     *
     * @return the available workpllace apps
     */
    public Collection<I_CmsWorkplaceAppConfiguration> getWorkplaceApps() {

        return m_appsById.values();
    }

    /**
     * Returns the additional workplace CSS URIs.<p>
     *
     * @return the additional workplace CSS URIs
     */
    public Collection<String> getWorkplaceCssUris() {

        return m_workplaceCssUris;
    }

    /**
     * Initializes the additional workplace CSS URIs.<p>
     * They will be taken from the module parameter 'workplace-css' if present in any module.<p>
     *
     * @param moduleManager the module manager instance
     */
    public void initWorkplaceCssUris(CmsModuleManager moduleManager) {

        Set<String> cssUris = new HashSet<String>();
        for (CmsModule module : moduleManager.getAllInstalledModules()) {
            String param = module.getParameter(WORKPLACE_CSS_PARAM);
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(param)) {
                cssUris.add(param);
            }
        }
        File cssFile = new File(
            OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebApplication(
                CmsStringUtil.joinPaths("resources", TOOLBAR_CSS)));
        if (cssFile.exists()) {
            cssUris.add(TOOLBAR_CSS);
        }
        m_workplaceCssUris = Collections.unmodifiableSet(cssUris);
    }

    /**
     * Loads the workplace apps.<p>
     */
    public void loadApps() {

        m_appsById.clear();
        m_appCategories = loadCategories();
        addAppConfigurations(loadDefaultApps());
        addAppConfigurations(loadAppsUsingServiceLoader());
        addAppConfigurations(loadLegacyApps());
    }

    /**
     * Stores the given app setting within the users additional info.<p>
     *
     * @param cms the cms context
     * @param type the app setting type, used as the settings key
     * @param appSettings the settings to store
     */
    public void storeAppSettings(CmsObject cms, Class<? extends I_CmsAppSettings> type, I_CmsAppSettings appSettings) {

        CmsUser user = cms.getRequestContext().getCurrentUser();
        CmsUserSettings settings = new CmsUserSettings(user);

        String currentSetting = settings.getAdditionalPreference(type.getName(), true);
        String state = appSettings.getSettingsString();
        if (((state == null) && (currentSetting == null)) || ((state != null) && state.equals(currentSetting))) {
            // nothing changed
            return;
        }

        settings.setAdditionalPreference(type.getName(), state);
        try {
            settings.save(cms);
        } catch (CmsException e) {
            LOG.error("Failed to store workplace app settings for type " + type.getName(), e);
        }
    }

    /**
     * Returns the quick launch apps set for the current user.<p>
     *
     * @param cms the cms context
     *
     * @return the quick launch app configurations
     */
    protected List<I_CmsWorkplaceAppConfiguration> getUserQuickLauchConfigurations(CmsObject cms) {

        String apps_info = (String)cms.getRequestContext().getCurrentUser().getAdditionalInfo(QUICK_LAUCH_APPS_KEY);
        String[] appIds = null;
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(apps_info)) {
            try {
                JSONArray ids = new JSONArray(apps_info);
                appIds = new String[ids.length()];
                for (int i = 0; i < appIds.length; i++) {
                    appIds[i] = ids.getString(i);
                }
            } catch (JSONException e) {
                LOG.error("Error parsing user quick launch apps setting.", e);
                appIds = null;
            }
        }
        return getAppConfigurations(appIds != null ? appIds : DEFAULT_USER_APPS);
    }

    /**
     * Writes the user quick launch apps setting to the user additional info.<p>
     *
     * @param cms the cms context
     * @param apps the app ids
     *
     * @throws Exception in case writing the user fails
     */
    protected void setUserQuickLaunchApps(CmsObject cms, List<String> apps) throws Exception {

        JSONArray appIds = new JSONArray(apps);
        CmsUser user = cms.getRequestContext().getCurrentUser();
        String infoValue = appIds.toString();
        String previousApps = (String)user.getAdditionalInfo(QUICK_LAUCH_APPS_KEY);
        // remove the additional info value to use default setting, in case the selected apps match the default apps
        if (new JSONArray(DEFAULT_USER_APPS).toString().equals(infoValue)) {
            infoValue = null;
        }
        // check if the additional info value needs to be changed
        if ((infoValue == previousApps) || ((infoValue != null) && infoValue.equals(previousApps))) {

            return;
        }
        if (infoValue == null) {
            user.deleteAdditionalInfo(QUICK_LAUCH_APPS_KEY);
        } else {
            user.setAdditionalInfo(QUICK_LAUCH_APPS_KEY, infoValue);
        }
        cms.writeUser(user);
    }

    /**
     * Adds the given app configuration.<p>
     *
     * @param appConfigs the app configuration
     */
    private void addAppConfigurations(Collection<I_CmsWorkplaceAppConfiguration> appConfigs) {

        for (I_CmsWorkplaceAppConfiguration appConfig : appConfigs) {
            I_CmsWorkplaceAppConfiguration old = m_appsById.get(appConfig.getId());
            if ((old == null) || (old.getPriority() < appConfig.getPriority())) {
                m_appsById.put(appConfig.getId(), appConfig);
            }
        }
    }

    /**
     * Loads the App Folder.<p>
     *
     * @return list of all app folder
     */
    private List<I_CmsFolderAppCategory> loadAppFolder() {

        List<I_CmsFolderAppCategory> result = new ArrayList<I_CmsFolderAppCategory>();
        result.addAll(
            Arrays.<I_CmsFolderAppCategory> asList(
                new CmsLinkValidationFolder(),
                new CmsDbManagerFolder(),
                new CmsCacheFolder()));
        return result;
    }

    /**
     * Returns the configured apps using the service loader.<p>
     *
     * @return tthe configured apps
     */
    private List<I_CmsWorkplaceAppConfiguration> loadAppsUsingServiceLoader() {

        List<I_CmsWorkplaceAppConfiguration> appConfigurations = new ArrayList<I_CmsWorkplaceAppConfiguration>();
        Iterator<I_CmsWorkplaceAppConfiguration> configs = ServiceLoader.load(
            I_CmsWorkplaceAppConfiguration.class).iterator();
        while (configs.hasNext()) {
            try {
                I_CmsWorkplaceAppConfiguration config = configs.next();
                appConfigurations.add(config);
            } catch (Throwable t) {
                LOG.error("Error loading workplace app configuration from classpath.", t);
            }
        }
        return appConfigurations;
    }

    /**
     * Loads the app categories.<p>
     *
     * @return the app categories
     */
    private Map<String, I_CmsAppCategory> loadCategories() {

        Map<String, I_CmsAppCategory> appCategories = new HashMap<String, I_CmsAppCategory>();
        CmsAppCategory main = new CmsAppCategory(MAIN_CATEGORY_ID, null, 0, 0);
        appCategories.put(main.getId(), main);
        CmsAppCategory admin = new CmsAppCategory(ADMINISTRATION_CATEGORY_ID, null, 5, 0);
        appCategories.put(admin.getId(), admin);
        CmsAppCategory legacy = new CmsAppCategory(LEGACY_CATEGORY_ID, null, 10, 0);
        appCategories.put(legacy.getId(), legacy);
        List<I_CmsFolderAppCategory> folder = loadAppFolder();
        for (I_CmsFolderAppCategory appFolder : folder) {
            appCategories.put(appFolder.getId(), appFolder);
        }
        Iterator<I_CmsAppCategory> categoryIt = ServiceLoader.load(I_CmsAppCategory.class).iterator();
        while (categoryIt.hasNext()) {
            try {
                I_CmsAppCategory cat = categoryIt.next();
                if (!appCategories.containsKey(cat.getId())
                    || (appCategories.get(cat.getId()).getPriority() < cat.getPriority())) {
                    appCategories.put(cat.getId(), cat);
                }
            } catch (Throwable t) {
                LOG.error("Error loading workplace app category from classpath.", t);
            }
        }
        return appCategories;
    }

    /**
     * Loads the default apps.<p>
     *
     * @return the default apps
     */
    private Collection<I_CmsWorkplaceAppConfiguration> loadDefaultApps() {

        List<I_CmsWorkplaceAppConfiguration> result = Lists.newArrayList();
        result.addAll(
            Arrays.<I_CmsWorkplaceAppConfiguration> asList(
                new CmsSitemapEditorConfiguration(),
                new CmsPageEditorConfiguration(),
                new CmsFileExplorerConfiguration(),
                new CmsScheduledJobsAppConfig(),
                new CmsAppHierarchyConfiguration(),
                new CmsEditorConfiguration(),
                new CmsQuickLaunchEditorConfiguration(),
                new CmsProjectManagerConfiguration(),
                new CmsProjectOverviewConfiguration(),
                new CmsCacheAdminConfiguration(),
                new CmsCacheViewFlexConfiguration(),
                new CmsCacheViewImageConfiguration(),
                new CmsFileHistoryConfiguration(),
                new CmsLinkValidationConfiguration(),
                new CmsLinkValidationExternalConfiguration(),
                new CmsLinkInFolderValidationConfiguration(),
                new CmsDbManagerConfiguration(),
                new CmsDbImportHTTPConfiguration(),
                new CmsDbImportServerConfiguration(),
                new CmsDbExportConfiguration(),
                new CmsDbStaticExportConfiguration(),
                new CmsSqlConsoleAppConfiguration(),
                new CmsDbRemovePubLocksConfiguration(),
                new CmsDbSynchronizationConfiguration(),
                new CmsDbPropertiesAppConfiguration(),
                new CmsSearchindexAppConfiguration(),
                new CmsLogFileConfiguration(),
                new CmsSourceSearchAppConfiguration(),
                new CmsListManagerConfiguration(),
                new CmsSiteManagerConfiguration(),
                new CmsPublishQueueConfiguration(),
                new CmsGitAppConfiguration(),
                new CmsBroadCastConfigurtion(),
                new CmsModuleAppConfiguration(),
                new CmsAccountsAppConfiguration(),
                new CmsShellAppConfiguration(),
                new CmsResourceTypeAppConfiguration(),
                new CmsUserDataAppConfiguration(),
                new CmsUnusedContentFinderConfiguration(),
                new CmsDateSearchConfiguration()));

        return result;
    }

    /**
     * Loads the legacy apps.<p>
     *
     * @return the legacy apps
     */
    private Collection<I_CmsWorkplaceAppConfiguration> loadLegacyApps() {

        List<I_CmsWorkplaceAppConfiguration> configs = new ArrayList<I_CmsWorkplaceAppConfiguration>();
        // avoid accessing the workplace manager during test case
        if (OpenCms.getRunLevel() >= OpenCms.RUNLEVEL_2_INITIALIZING) {
            List<CmsTool> tools = OpenCms.getWorkplaceManager().getToolManager().getToolHandlers();
            for (CmsTool tool : tools) {

                I_CmsToolHandler handler = tool.getHandler();
                String path = handler.getPath();

                // only collecting first path level tools
                if ((path.length() > 1) && (path.indexOf(CmsToolManager.TOOLPATH_SEPARATOR, 1) < 0)) {
                    if (!LEGACY_BLACKLIST.contains(path)) {
                        configs.add(new CmsLegacyAppConfiguration(handler));
                    }
                }

            }
        }
        return configs;
    }

}

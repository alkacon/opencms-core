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

package org.opencms.workplace;

import org.opencms.ade.configuration.CmsElementView;
import org.opencms.ade.containerpage.shared.CmsCntPageData.ElementDeleteMode;
import org.opencms.ade.galleries.shared.CmsGallerySearchScope;
import org.opencms.ade.upload.CmsDefaultUploadRestriction;
import org.opencms.ade.upload.I_CmsUploadRestriction;
import org.opencms.ade.upload.I_CmsVirusScanner;
import org.opencms.configuration.CmsAdditionalLogFolderConfig;
import org.opencms.configuration.CmsDefaultUserSettings;
import org.opencms.db.CmsExportPoint;
import org.opencms.db.CmsUserSettings;
import org.opencms.db.I_CmsProjectDriver;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeFolderExtended;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsAcceptLanguageHeaderParser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsI18nInfo;
import org.opencms.i18n.CmsLocaleComparator;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.I_CmsLocaleHandler;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsBroadcast.ContentMode;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleManager;
import org.opencms.relations.CmsCategoryService;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsPermissionViolationException;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsRfsFileViewer;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsAccountInfo.Field;
import org.opencms.workplace.editors.CmsEditorDisplayOptions;
import org.opencms.workplace.editors.CmsEditorHandler;
import org.opencms.workplace.editors.CmsWorkplaceEditorManager;
import org.opencms.workplace.editors.I_CmsEditorActionHandler;
import org.opencms.workplace.editors.I_CmsEditorCssHandler;
import org.opencms.workplace.editors.I_CmsEditorHandler;
import org.opencms.workplace.editors.I_CmsPreEditorActionDefinition;
import org.opencms.workplace.editors.directedit.CmsDirectEditDefaultProvider;
import org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider;
import org.opencms.workplace.explorer.CmsExplorerTypeAccess;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.galleries.A_CmsAjaxGallery;
import org.opencms.workplace.tools.CmsToolManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Manages the global OpenCms workplace settings for all users.<p>
 *
 * This class reads the settings from the "opencms.properties" and stores them in member variables.
 * For each setting one or more get methods are provided.<p>
 *
 * @since 6.0.0
 */
public final class CmsWorkplaceManager implements I_CmsLocaleHandler, I_CmsEventListener {

    /**
     * Helper class used to easily define default view mappings for standard resource types.<p>
     */
    static class ViewRules {

        /**
         * Internal view map.<p>
         */
        private Map<String, String> m_viewMap = Maps.newHashMap();

        /**
         * Creates a new instance.<p>
         *
         * @param rules an array of strings of the form 'foo,bar,baz:view1', where foo, ... are type names and view1 is a view name (explorertype)
         */
        public ViewRules(String... rules) {

            for (String rule : rules) {
                int colIndex = rule.indexOf(':');
                if (colIndex != -1) {
                    String before = rule.substring(0, colIndex);
                    String after = rule.substring(colIndex + 1);
                    for (String token : CmsStringUtil.splitAsList(before, ",")) {
                        m_viewMap.put(token.trim(), after);
                    }
                }

            }
        }

        /**
         * Gets the view configured for the given type.<p>
         *
         * @param type a resource type name
         * @return the view explorer type for the given resource type
         */
        public String getViewForType(String type) {

            return m_viewMap.get(type);

        }
    }

    /** The default encoding for the workplace (UTF-8). */
    public static final String DEFAULT_WORKPLACE_ENCODING = CmsEncoder.ENCODING_UTF_8;

    /** The workplace localization manifest attribute name. */
    public static final String LOCALIZATION_ATTRIBUTE_NAME = "OpenCms-Localization";

    /** The manifest file resource name. */
    public static final String MANIFEST_RESOURCE_NAME = "META-INF/MANIFEST.MF";

    /** The id of the "requestedResource" parameter for the OpenCms login form. */
    public static final String PARAM_LOGIN_REQUESTED_RESOURCE = "requestedResource";

    /** Key name for the session workplace settings. */
    public static final String SESSION_WORKPLACE_SETTINGS = "__CmsWorkplace.WORKPLACE_SETTINGS";

    /** Default view configuration. */
    static ViewRules m_defaultViewRules = new ViewRules(
        "folder,plain,jsp,htmlredirect,containerpage:view_basic",
        "imagegallery,downloadgallery,linkgallery,subsitemap,content_folder:view_folders",
        "formatter_config,xmlvfsbundle,propertyvfsbundle,bundledescriptor,sitemap_config,sitemap_master_config,site_plugin,attr_editor_config,module_config,elementview,seo_file,containerpage_template,inheritance_config,macro_formatter,flex_formatter,settings_config:view_configs",
        "xmlcontent,pointer:view_other");

    /** The default account infos. */
    private static final CmsAccountInfo[] DEFAULT_ACCOUNT_INFOS = new CmsAccountInfo[] {
        new CmsAccountInfo(Field.firstname, null, false),
        new CmsAccountInfo(Field.lastname, null, false),
        new CmsAccountInfo(Field.email, null, false),
        new CmsAccountInfo(Field.institution, null, false)};

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsWorkplaceManager.class);

    /** Value of the acacia-unlock configuration option (may be null if not set). */
    private String m_acaciaUnlock;

    /** The configured account infos. */
    private List<CmsAccountInfo> m_accountInfos;

    /** The admin cms context. */
    private CmsObject m_adminCms;

    /** If enabled, gives element authors more gallery-related permissions (mostly upload/replace). */
    private boolean m_allowElementAuthorToWorkInGalleries;

    /** Indicates if auto-locking of resources is enabled or disabled. */
    private boolean m_autoLockResources;

    /** The name of the local category folder(s). */
    private String m_categoryFolder;

    /** The default access for explorer types. */
    private CmsExplorerTypeAccess m_defaultAccess;

    /** The configured default locale of the workplace. */
    private Locale m_defaultLocale;

    /** The default property setting for setting new property values. */
    private boolean m_defaultPropertiesOnStructure;

    /** The default user settings. */
    private CmsDefaultUserSettings m_defaultUserSettings;

    /** The configured dialog handlers. */
    private Map<String, I_CmsDialogHandler> m_dialogHandler;

    /** The configured direct edit provider. */
    private I_CmsDirectEditProvider m_directEditProvider;

    /** A flag, indicating if the categories should be displayed separated by repository in the category selection dialog. */
    private boolean m_displayCategoriesByRepository;

    /** A flag, indicating if the categories should be displayed separated by repository in the category selection dialog. */
    private boolean m_displayCategorySelectionCollapsed;

    /** The edit action handler. */
    private I_CmsEditorActionHandler m_editorAction;

    /** The editor CSS handlers. */
    private List<I_CmsEditorCssHandler> m_editorCssHandlers;

    /** The workplace editor display options. */
    private CmsEditorDisplayOptions m_editorDisplayOptions;

    /** The editor handler. */
    private I_CmsEditorHandler m_editorHandler;

    /** The editor manager. */
    private CmsWorkplaceEditorManager m_editorManager;

    /** The element delete mode. */
    private ElementDeleteMode m_elementDeleteMode;

    /** The flag if switching tabs in the advanced property dialog is enabled. */
    private boolean m_enableAdvancedPropertyTabs;

    /** The configured encoding of the workplace. */
    private String m_encoding;

    /** The explorer type settings. */
    private List<CmsExplorerTypeSettings> m_explorerTypeSettings;

    /** The explorer type settings from the configured modules. */
    private List<CmsExplorerTypeSettings> m_explorerTypeSettingsFromModules;

    /** The explorer type settings from the XML configuration. */
    private List<CmsExplorerTypeSettings> m_explorerTypeSettingsFromXml;

    /** The explorer type settings as Map with resource type name as key. */
    private Map<String, CmsExplorerTypeSettings> m_explorerTypeSettingsMap;

    /** The element views generated from explorer types. */
    private Map<CmsUUID, CmsElementView> m_explorerTypeViews = Maps.newHashMap();

    /** The workplace export points. */
    private Set<CmsExportPoint> m_exportPoints;

    /** Maximum size of an upload file. */
    private int m_fileMaxUploadSize;

    /** The instance used for reading portions of lines of a file to choose. */
    private CmsRfsFileViewer m_fileViewSettings;

    /** The configured workplace galleries. */
    private Map<String, A_CmsAjaxGallery> m_galleries;

    /** The configured gallery default scope. */
    private String m_galleryDefaultScope;

    /** The group translation. */
    private I_CmsGroupNameTranslation m_groupNameTranslation;

    /** The configured group translation class name. */
    private String m_groupTranslationClass;

    /** Keep-alive flag. */
    private Boolean m_keepAlive;

    /** Contains all folders that should be labeled if siblings exist. */
    private List<String> m_labelSiteFolders;

    /** List of installed workplace locales, sorted ascending. */
    private List<Locale> m_locales;

    /** The configured list of localized workplace folders. */
    private List<String> m_localizedFolders;

    /** The additional log folder configuration. */
    private CmsAdditionalLogFolderConfig m_logFolderConfig = new CmsAdditionalLogFolderConfig();

    /** The workplace localized messages (mapped to the locales). */
    private Map<Locale, CmsWorkplaceMessages> m_messages;

    /** The post upload handler. */
    private I_CmsPostUploadDialogHandler m_postUploadHandler;

    /** The condition definitions for the resource types  which are triggered before opening the editor. */
    private List<I_CmsPreEditorActionDefinition> m_preEditorConditionDefinitions;

    /** The repository folder handler. */
    private I_CmsRepositoryFolderHandler m_repositoryFolderHandler;

    /** Indicates if the user management icon should be displayed in the workplace. */
    private boolean m_showUserGroupIcon;

    /** The role required for editing the sitemap configuration. */
    private String m_sitemapConfigEditRole;

    /** Exclude patterns for synchronization. */
    private ArrayList<Pattern> m_synchronizeExcludePatterns;

    /** The temporary file project used by the editors. */
    private CmsProject m_tempFileProject;

    /** The tool manager. */
    private CmsToolManager m_toolManager;

    /** The upload restriction. */
    private I_CmsUploadRestriction m_uploadRestriction = CmsDefaultUploadRestriction.unrestricted();

    /** Keeps track of whether the upload restriction has been set. */
    private boolean m_uploadRestrictionSet;

    /** The user additional information configuration. */
    private CmsWorkplaceUserInfoManager m_userInfoManager;

    /** The user list mode. */
    private String m_userListMode;

    /** The configured workplace views. */
    private List<CmsWorkplaceView> m_views;

    /** The configured virus scanner. */
    private I_CmsVirusScanner m_virusScanner;

    /** True if the virus scanner is enabled. */
    private boolean m_virusScannerEnabled;

    /** Expiring cache used to limit the number of notifications sent because of invalid workplace server names. */
    private Cache<String, String> m_workplaceServerUserChecks;

    /** The XML content auto correction flag. */
    private boolean m_xmlContentAutoCorrect;

    /**
     * Creates a new instance for the workplace manager, will be called by the workplace configuration manager.<p>
     */
    public CmsWorkplaceManager() {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_WORKPLACE_INITIALIZE_START_0));
        }

        m_locales = new ArrayList<Locale>();
        m_labelSiteFolders = new ArrayList<String>();
        m_localizedFolders = new ArrayList<String>();
        m_autoLockResources = true;
        m_categoryFolder = CmsCategoryService.REPOSITORY_BASE_FOLDER;
        m_xmlContentAutoCorrect = true;
        m_showUserGroupIcon = true;
        m_dialogHandler = new HashMap<String, I_CmsDialogHandler>();
        m_views = new ArrayList<CmsWorkplaceView>();
        m_exportPoints = new HashSet<CmsExportPoint>();
        m_editorHandler = new CmsEditorHandler();
        m_fileMaxUploadSize = -1;
        m_fileViewSettings = new CmsRfsFileViewer();
        m_explorerTypeSettingsFromXml = new ArrayList<CmsExplorerTypeSettings>();
        m_explorerTypeSettingsFromModules = new ArrayList<CmsExplorerTypeSettings>();
        m_defaultPropertiesOnStructure = true;
        m_enableAdvancedPropertyTabs = true;
        m_defaultUserSettings = new CmsDefaultUserSettings();
        m_defaultAccess = new CmsExplorerTypeAccess();
        m_galleries = new HashMap<String, A_CmsAjaxGallery>();
        flushMessageCache();
        m_preEditorConditionDefinitions = new ArrayList<I_CmsPreEditorActionDefinition>();
        m_editorCssHandlers = new ArrayList<I_CmsEditorCssHandler>();
        m_synchronizeExcludePatterns = new ArrayList<Pattern>();

        // important to set this to null to avoid unnecessary overhead during configuration phase
        m_explorerTypeSettings = null;
        CacheBuilder<Object, Object> cb = CacheBuilder.newBuilder().expireAfterWrite(
            2,
            TimeUnit.MINUTES).concurrencyLevel(3);
        m_workplaceServerUserChecks = cb.build();
    }

    /**
     * Returns true if the provided request was done by a Workplace user.<p>
     *
     * @param req the request to check
     *
     * @return true if the provided request was done by a Workplace user
     */
    public static boolean isWorkplaceUser(HttpServletRequest req) {

        HttpSession session = req.getSession(false);
        if (session != null) {
            // if a session is available, check for a workplace configuration
            CmsWorkplaceSettings workplaceSettings = (CmsWorkplaceSettings)session.getAttribute(
                CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
            return ((null != workplaceSettings) && !workplaceSettings.getUser().isGuestUser());
        }
        // no session means no workplace use
        return false;
    }

    /**
     * Adds an account info.<p>
     *
     * @param info the account info to add
     */
    public void addAccountInfo(CmsAccountInfo info) {

        if (m_accountInfos == null) {
            m_accountInfos = new ArrayList<CmsAccountInfo>();
        }
        m_accountInfos.add(info);
    }

    /**
     * Adds an account info.<p>
     *
     * @param field the field
     * @param addInfoKey the additional info key
     * @param editable the editable flag
     */
    public void addAccountInfo(String field, String addInfoKey, String editable) {

        addAccountInfo(new CmsAccountInfo(field, addInfoKey, editable));
    }

    /**
     * Adds a dialog handler instance to the list of configured dialog handlers.<p>
     *
     * @param clazz the instantiated dialog handler to add
     */
    public void addDialogHandler(I_CmsDialogHandler clazz) {

        m_dialogHandler.put(clazz.getDialogHandler(), clazz);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.INIT_ADD_DIALOG_HANDLER_2,
                    clazz.getDialogHandler(),
                    clazz.getClass().getName()));
        }
    }

    /**
     * Adds an editor CSS handler class to the list of handlers.<p>
     *
     * @param editorCssHandlerClassName full class name of the css handler class
     */
    public void addEditorCssHandler(String editorCssHandlerClassName) {

        try {
            I_CmsEditorCssHandler editorCssHandler = (I_CmsEditorCssHandler)Class.forName(
                editorCssHandlerClassName).newInstance();
            m_editorCssHandlers.add(editorCssHandler);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(
                    Messages.get().getBundle().key(Messages.INIT_EDITOR_CSSHANDLER_CLASS_1, editorCssHandlerClassName));
            }
        } catch (Exception e) {
            LOG.error(
                Messages.get().getBundle().key(Messages.LOG_INVALID_EDITOR_CSSHANDLER_1, editorCssHandlerClassName),
                e);
        }
    }

    /**
     * Adds an editor CSS handler class at the first position of the list of handlers.<p>
     *
     * @param editorCssHandlerClassName full class name of the css handler class
     */
    public void addEditorCssHandlerToHead(String editorCssHandlerClassName) {

        try {
            I_CmsEditorCssHandler editorCssHandler = (I_CmsEditorCssHandler)Class.forName(
                editorCssHandlerClassName).newInstance();

            List<I_CmsEditorCssHandler> editorCssHandlers = new ArrayList<I_CmsEditorCssHandler>();
            editorCssHandlers.add(editorCssHandler);
            editorCssHandlers.addAll(m_editorCssHandlers);

            m_editorCssHandlers = editorCssHandlers;

            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(
                    Messages.get().getBundle().key(Messages.INIT_EDITOR_CSSHANDLER_CLASS_1, editorCssHandlerClassName));
            }
        } catch (Exception e) {
            LOG.error(
                Messages.get().getBundle().key(Messages.LOG_INVALID_EDITOR_CSSHANDLER_1, editorCssHandlerClassName),
                e);
        }
    }

    /**
     * Adds an explorer type setting object to the list of type settings.<p>
     *
     * @param settings the explorer type settings
     */
    public void addExplorerTypeSetting(CmsExplorerTypeSettings settings) {

        m_explorerTypeSettingsFromXml.add(settings);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_ADD_TYPE_SETTING_1, settings.getName()));
        }
        if (m_explorerTypeSettings != null) {
            // reset the list of all explorer type settings, but not during startup
            initExplorerTypeSettings();
        }
    }

    /**
     * Adds the list of explorer type settings from the given module.<p>
     *
     * @param module the module witch contains the explorer type settings to add
     */
    public void addExplorerTypeSettings(CmsModule module) {

        List<CmsExplorerTypeSettings> explorerTypes = module.getExplorerTypes();
        if ((explorerTypes != null) && (explorerTypes.size() > 0)) {
            Iterator<CmsExplorerTypeSettings> i = explorerTypes.iterator();
            while (i.hasNext()) {
                CmsExplorerTypeSettings settings = i.next();
                if (m_explorerTypeSettingsFromModules.contains(settings)) {
                    m_explorerTypeSettingsFromModules.remove(settings);
                }
                m_explorerTypeSettingsFromModules.add(settings);
                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(
                        Messages.get().getBundle().key(Messages.INIT_ADD_TYPE_SETTING_1, settings.getName()));
                }
            }
            // reset the list of all explorer type settings
            initExplorerTypeSettings();
        }
    }

    /**
     * Adds newly created export point to the workplace configuration.<p>
     *
     * @param uri the export point uri
     * @param destination the export point destination
     */
    public void addExportPoint(String uri, String destination) {

        CmsExportPoint point = new CmsExportPoint(uri, destination);
        m_exportPoints.add(point);
        if (CmsLog.INIT.isInfoEnabled() && (point.getDestinationPath() != null)) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.INIT_ADD_EXPORT_POINT_2,
                    point.getUri(),
                    point.getDestinationPath()));
        }
    }

    /**
     * Adds a folder to the list of labeled folders.<p>
     *
     * @param uri the folder uri to add
     */
    public void addLabeledFolder(String uri) {

        m_labelSiteFolders.add(uri);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_LABEL_LINKS_IN_FOLDER_1, uri));
        }
    }

    /**
     * Adds a new folder to the list of localized workplace folders.<p>
     *
     * @param uri a new folder to add to the list of localized workplace folders
     */
    public void addLocalizedFolder(String uri) {

        m_localizedFolders.add(uri);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_WORKPLACE_LOCALIZED_1, uri));
        }
    }

    /**
     * Adds an initialized condition definition class that is triggered before opening the editor.<p>
     *
     * @param preEditorCondition the initialized condition definition class
     */
    public void addPreEditorConditionDefinition(I_CmsPreEditorActionDefinition preEditorCondition) {

        m_preEditorConditionDefinitions.add(preEditorCondition);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.INIT_EDITOR_PRE_ACTION_2,
                    preEditorCondition.getClass().getName(),
                    preEditorCondition.getResourceTypeName()));
        }
    }

    /**
     * Adds a condition definition class for a given resource type name that is triggered before opening the editor.<p>
     *
     * @param resourceTypeName the name of the resource type
     * @param preEditorConditionDefinitionClassName full class name of the condition definition class
     */
    public void addPreEditorConditionDefinition(String resourceTypeName, String preEditorConditionDefinitionClassName) {

        try {
            I_CmsPreEditorActionDefinition preEditorCondition = (I_CmsPreEditorActionDefinition)Class.forName(
                preEditorConditionDefinitionClassName).newInstance();
            preEditorCondition.setResourceTypeName(resourceTypeName);
            m_preEditorConditionDefinitions.add(preEditorCondition);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(
                    Messages.get().getBundle().key(
                        Messages.INIT_EDITOR_PRE_ACTION_2,
                        preEditorConditionDefinitionClassName,
                        resourceTypeName));
            }
        } catch (Exception e) {
            LOG.error(
                Messages.get().getBundle().key(
                    Messages.LOG_INVALID_EDITOR_PRE_ACTION_1,
                    preEditorConditionDefinitionClassName),
                e);
        }
    }

    /**
     * Adds a pattern to be excluded in VFS synchronization.<p>
     *
     * @param pattern a java regex to applied on the file name
     */
    public void addSynchronizeExcludePattern(String pattern) {

        try {
            m_synchronizeExcludePatterns.add(Pattern.compile(pattern));
        } catch (PatternSyntaxException e) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_INVALID_SYNCHRONIZE_EXCLUDE_PATTERN_1, pattern), e);
        }
    }

    /**
     * Returns if the autolock resources feature is enabled.<p>
     *
     * @return true if the autolock resources feature is enabled, otherwise false
     */
    public boolean autoLockResources() {

        return m_autoLockResources;
    }

    /**
     * Checks if the user in the given context has permissions for uploading.
     *
     * @param cms a CMS context
     * @throws CmsSecurityException if the user doesn't have permission
     */
    public void checkAdeGalleryUpload(CmsObject cms) throws CmsSecurityException {

        OpenCms.getRoleManager().checkRole(cms, getUploadRole());
    }

    /**
     * Checks whether the workplace is accessed through the workplace server, and sends an error message otherwise.<p>
     *
     * @param request the request to check
     * @param cms the CmsObject to use
     */
    public void checkWorkplaceRequest(HttpServletRequest request, CmsObject cms) {

        try {
            if ((OpenCms.getSiteManager().getSites().size() > 1)
                && !OpenCms.getSiteManager().isWorkplaceRequest(request)) {
                // this is a multi site-configuration, but not a request to the configured Workplace site

                CmsUser user = cms.getRequestContext().getCurrentUser();
                // to limit the number of times broadcast is called for a user, we use an expiring cache
                // with the user name as  key
                if (null == m_workplaceServerUserChecks.getIfPresent(user.getName())) {
                    m_workplaceServerUserChecks.put(user.getName(), "");
                    OpenCms.getSessionManager().sendBroadcast(
                        null,
                        Messages.get().getBundle(getWorkplaceLocale(cms)).key(
                            Messages.ERR_WORKPLACE_SERVER_CHECK_FAILED_0),
                        user,
                        ContentMode.plain);

                }

            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Implements the event listener of this class.<p>
     *
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        switch (event.getType()) {
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                flushMessageCache();
                m_editorDisplayOptions.clearCache();
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_EVENT_CLEAR_CACHES_0));
                }
                break;
            default: // no operation
        }
    }

    /**
     * Creates a temporary file which is needed while working in an editor with preview option.<p>
     *
     * <i>Note</i>: This method is synchronized to avoid rare issues that might be caused by
     * double requests fired by some browser/OS combinations.<p>
     *
     * @param cms the cms context
     * @param resourceName the name of the resource to copy
     * @param currentProjectId the id of the project to work with
     *
     * @return the file name of the temporary file
     *
     * @throws CmsException if something goes wrong
     */
    public synchronized String createTempFile(CmsObject cms, String resourceName, CmsUUID currentProjectId)
    throws CmsException {

        // check that the current user has write permissions
        if (!cms.hasPermissions(cms.readResource(resourceName, CmsResourceFilter.ALL), CmsPermissionSet.ACCESS_WRITE)) {
            throw new CmsPermissionViolationException(
                org.opencms.db.Messages.get().container(org.opencms.db.Messages.ERR_PERM_DENIED_2, resourceName, "w"));
        }

        // initialize admin cms context
        CmsObject adminCms = getAdminCms(cms);

        // generate the filename of the temporary file
        String temporaryFilename = CmsWorkplace.getTemporaryFileName(resourceName);

        // check if the temporary file is already present
        if (adminCms.existsResource(temporaryFilename, CmsResourceFilter.ALL)) {
            // delete old temporary file
            if (!cms.getLock(temporaryFilename).isUnlocked()) {
                // steal lock
                cms.changeLock(temporaryFilename);
            } else {
                // lock resource to current user
                cms.lockResource(temporaryFilename);
            }
            cms.deleteResource(temporaryFilename, CmsResource.DELETE_PRESERVE_SIBLINGS);
        }

        try {
            // switch to the temporary file project
            adminCms.getRequestContext().setCurrentProject(cms.readProject(getTempFileProjectId()));
            // copy the file to edit to a temporary file
            adminCms.copyResource(resourceName, temporaryFilename, CmsResource.COPY_AS_NEW);
        } finally {
            // switch back to current project
            adminCms.getRequestContext().setCurrentProject(cms.readProject(currentProjectId));
        }

        try {
            // switch to the temporary file project
            cms.getRequestContext().setCurrentProject(
                cms.readProject(OpenCms.getWorkplaceManager().getTempFileProjectId()));
            // lock the temporary file
            cms.changeLock(temporaryFilename);
            // touch the temporary file
            cms.setDateLastModified(temporaryFilename, System.currentTimeMillis(), false);
            // set the temporary file flag
            CmsResource tempFile = cms.readResource(temporaryFilename, CmsResourceFilter.ALL);
            int flags = tempFile.getFlags();
            if ((flags & CmsResource.FLAG_TEMPFILE) == 0) {
                flags += CmsResource.FLAG_TEMPFILE;
            }
            cms.chflags(temporaryFilename, flags);
            // remove eventual release & expiration date from temporary file to make preview in editor work
            cms.setDateReleased(temporaryFilename, CmsResource.DATE_RELEASED_DEFAULT, false);
            cms.setDateExpired(temporaryFilename, CmsResource.DATE_EXPIRED_DEFAULT, false);
            // remove visibility permissions for everybody on temporary file if possible
            if (cms.hasPermissions(tempFile, CmsPermissionSet.ACCESS_CONTROL)) {
                cms.chacc(
                    temporaryFilename,
                    I_CmsPrincipal.PRINCIPAL_GROUP,
                    OpenCms.getDefaultUsers().getGroupUsers(),
                    "-v");
            }
        } finally {
            // switch back to current project
            cms.getRequestContext().setCurrentProject(cms.readProject(currentProjectId));
        }

        return temporaryFilename;
    }

    /**
     * Flushes the cached workplace messages.<p>
     */
    public void flushMessageCache() {

        // clear the cached message objects
        m_messages = new HashMap<Locale, CmsWorkplaceMessages>();
        if (LOG.isDebugEnabled()) {
            try {
                throw new RuntimeException("Tracing exception");
            } catch (Exception e) {
                LOG.info("Tracing call to CmsWorkplaceManager.flushMessageCache method.", e);
            }
        }
    }

    /**
     * Gets the value of the acacia-unlock configuration option (null if not set explicitly).<p>
     *
     * @return the value of the acacia-unlock configuration option
     */
    public String getAcaciaUnlock() {

        return m_acaciaUnlock;
    }

    /**
     * Returns the account infos.<p>
     *
     * @return the account infos
     */
    public List<CmsAccountInfo> getAccountInfos() {

        if (m_accountInfos == null) {
            return Collections.unmodifiableList(Arrays.asList(DEFAULT_ACCOUNT_INFOS));
        } else {
            return Collections.unmodifiableList(m_accountInfos);
        }
    }

    /**
     * Gets the additional log folder configuration.<p>
     *
     * @return the additional log folder configuration
     */
    public CmsAdditionalLogFolderConfig getAdditionalLogFolderConfiguration() {

        return m_logFolderConfig;
    }

    /**
     * Returns the name of the local category folder(s).<p>
     *
     * @return the name of the local category folder(s)
     */
    public String getCategoryFolder() {

        return m_categoryFolder;
    }

    /**
     * Returns the configured account infos.<p>
     *
     * @return the configured account infos
     */
    public List<CmsAccountInfo> getConfiguredAccountInfos() {

        return m_accountInfos;
    }

    /**
     * Gets the access object of the type settings.<p>
     *
     * @return access object of the type settings
     */
    public CmsExplorerTypeAccess getDefaultAccess() {

        return m_defaultAccess;
    }

    /**
     * Returns the Workplace default locale.<p>
     *
     * @return  the Workplace default locale
     */
    public Locale getDefaultLocale() {

        return m_defaultLocale;
    }

    /**
     * Gets the default name pattern for the given type.<p>
     *
     * @param type the type name
     * @return the default name pattern for the type
     */
    public String getDefaultNamePattern(String type) {

        CmsExplorerTypeSettings settings = getExplorerTypeSetting(type);
        if ((settings != null) && (settings.getNamePattern() != null)) {
            return settings.getNamePattern();
        }
        if (type.equals("sitemap_config") || type.equals("module_config")) {
            return ".config%(number)";
        }
        if (type.equals("content_folder")) {
            return ".content%(number)";
        }
        return "new_" + type + "%(number)";
    }

    /**
     * Returns the Workplace default user settings.<p>
     *
     * @return  the Workplace default user settings
     */
    public CmsDefaultUserSettings getDefaultUserSettings() {

        return m_defaultUserSettings;
    }

    /**
     * Returns all instantiated dialog handlers for the workplace.<p>
     *
     * @return all instantiated dialog handlers for the workplace
     */
    public Map<String, I_CmsDialogHandler> getDialogHandler() {

        return m_dialogHandler;
    }

    /**
     * Returns the instantiated dialog handler class for the key or null, if there is no mapping for the key.<p>
     *
     * @param key the key whose associated value is to be returned
     *
     * @return the instantiated dialog handler class for the key
     */
    public I_CmsDialogHandler getDialogHandler(String key) {

        return m_dialogHandler.get(key);
    }

    /**
     * Returns a new instance of the configured direct edit provider.<p>
     *
     * @return a new instance of the configured direct edit provider
     */
    public I_CmsDirectEditProvider getDirectEditProvider() {

        return m_directEditProvider.newInstance();
    }

    /**
     * Returns the instantiated editor action handler class.<p>
     *
     * @return the instantiated editor action handler class
     */
    public I_CmsEditorActionHandler getEditorActionHandler() {

        return m_editorAction;
    }

    /**
     * Returns the instantiated editor CSS handler classes.<p>
     *
     * @return the instantiated editor CSS handler classes
     */
    public List<I_CmsEditorCssHandler> getEditorCssHandlers() {

        return m_editorCssHandlers;
    }

    /**
     * Returns the instantiated editor display option class.<p>
     *
     * @return the instantiated editor display option class
     */
    public CmsEditorDisplayOptions getEditorDisplayOptions() {

        return m_editorDisplayOptions;
    }

    /**
     * Returns the instantiated editor handler class.<p>
     *
     * @return the instantiated editor handler class
     */
    public I_CmsEditorHandler getEditorHandler() {

        return m_editorHandler;
    }

    /**
     * Returns the element delete mode.<p>
     *
     * @return the element delete mode
     */
    public ElementDeleteMode getElementDeleteMode() {

        return m_elementDeleteMode;
    }

    /**
     * Returns the configured workplace encoding.<p>
     *
     * @return the configured workplace encoding
     */
    public String getEncoding() {

        return m_encoding;
    }

    /**
     * Returns the explorer type settings for the specified resource type.<p>
     *
     * @param type the resource type for which the settings are required
     *
     * @return the explorer type settings for the specified resource type
     */
    public CmsExplorerTypeSettings getExplorerTypeSetting(String type) {

        return m_explorerTypeSettingsMap.get(type);
    }

    /**
     * Returns the list of explorer type settings.<p>
     *
     * These settings provide information for the new resource dialog and the context menu appearance.<p>
     *
     * @return the list of explorer type settings
     */
    public List<CmsExplorerTypeSettings> getExplorerTypeSettings() {

        if (m_explorerTypeSettings == null) {
            // initialize all explorer type settings if not already done
            initExplorerTypeSettings();
        }

        return m_explorerTypeSettings;
    }

    /**
     * Gets the explorer types for the given view name.<p>
     *
     * @param viewName the view name
     *
     * @return the explorer names for the given view names
     */
    public List<CmsExplorerTypeSettings> getExplorerTypesForView(String viewName) {

        List<CmsExplorerTypeSettings> result = Lists.newArrayList();
        for (CmsExplorerTypeSettings explorerType : getExplorerTypeSettings()) {

            String currentViewName = explorerType.getElementView();
            if (currentViewName == null) {
                currentViewName = getDefaultView(explorerType.getName());
            }
            if ((currentViewName != null) && currentViewName.equals(viewName)) {
                if (OpenCms.getResourceManager().hasResourceType(explorerType.getName())) {
                    result.add(explorerType);
                }
            } else if (CmsResourceTypeFolder.getStaticTypeName().equals(explorerType.getName())
                && "view_folders|view_basic".contains(viewName)) {
                result.add(explorerType);
            }

        }
        return result;
    }

    /**
     * Gets the element views generated from explorer types.<p>
     *
     * @return the map of element views from the explorer types
     */
    public Map<CmsUUID, CmsElementView> getExplorerTypeViews() {

        return Collections.unmodifiableMap(m_explorerTypeViews);

    }

    /**
     * Returns the set of configured export points for the workplace.<p>
     *
     * @return the set of configured export points for the workplace
     */
    public Set<CmsExportPoint> getExportPoints() {

        return m_exportPoints;
    }

    /**
     * Returns the value (in bytes) for the maximum file upload size of the current user.<p>
     *
     * @param cms the initialized CmsObject
     *
     * @return the value (in bytes) for the maximum file upload size
     */
    public long getFileBytesMaxUploadSize(CmsObject cms) {

        int maxFileSize = getFileMaxUploadSize();
        long maxFileSizeBytes = maxFileSize * 1024;
        // check if current user belongs to Admin group, if so no file upload limit
        if ((maxFileSize <= 0) || OpenCms.getRoleManager().hasRole(cms, CmsRole.VFS_MANAGER)) {
            maxFileSizeBytes = -1;
        }
        return maxFileSizeBytes;
    }

    /**
     * Returns the value (in kb) for the maximum file upload size.<p>
     *
     * @return the value (in kb) for the maximum file upload size
     */
    public int getFileMaxUploadSize() {

        return m_fileMaxUploadSize;
    }

    /**
     * Returns the system-wide file view settings for the workplace.<p>
     *
     * Note that this instance may not modified (invocation of setters) directly or a
     * <code>{@link org.opencms.main.CmsRuntimeException}</code> will be thrown.<p>
     *
     * It has to be cloned first and then may be written back to the workplace settings using
     * method {@link #setFileViewSettings(CmsObject, org.opencms.util.CmsRfsFileViewer)}.<p>
     *
     * @return the system-wide file view settings for the workplace
     */
    public CmsRfsFileViewer getFileViewSettings() {

        return m_fileViewSettings;
    }

    /**
     * Returns a collection of all available galleries.<p>
     *
     * The Map has the gallery type name as key and an instance of the
     * gallery class (not completely initialized) as value.<p>
     *
     * @return a collection of all available galleries
     */
    public Map<String, A_CmsAjaxGallery> getGalleries() {

        return m_galleries;
    }

    /**
     * Returns the gallery default scope.<p>
     *
     * @return the gallery default scope
     */
    public CmsGallerySearchScope getGalleryDefaultScope() {

        CmsGallerySearchScope result = CmsGallerySearchScope.siteShared;
        if (m_galleryDefaultScope != null) {
            try {
                result = CmsGallerySearchScope.valueOf(m_galleryDefaultScope);
            } catch (Throwable t) {
                // ignore
            }
        }
        return result;
    }

    /**
     * Gets the configured gallery default scope as a string.<p>
     *
     * @return the gallery default scope as a string
     */
    public String getGalleryDefaultScopeString() {

        return m_galleryDefaultScope;
    }

    /**
     * Returns the object used for translating group names.<p>
     *
     * @return the group name translator
     */
    public I_CmsGroupNameTranslation getGroupNameTranslation() {

        if (m_groupNameTranslation != null) {
            return m_groupNameTranslation;
        }
        if (m_groupTranslationClass != null) {
            try {
                m_groupNameTranslation = (I_CmsGroupNameTranslation)Class.forName(
                    m_groupTranslationClass).newInstance();
                return m_groupNameTranslation;
            } catch (ClassNotFoundException e) {
                LOG.error(e.getLocalizedMessage(), e);
            } catch (IllegalAccessException e) {
                LOG.error(e.getLocalizedMessage(), e);
            } catch (InstantiationException e) {
                LOG.error(e.getLocalizedMessage(), e);
            } catch (ClassCastException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            m_groupNameTranslation = getDefaultGroupNameTranslation();
            return m_groupNameTranslation;
        } else {
            m_groupNameTranslation = getDefaultGroupNameTranslation();
            return m_groupNameTranslation;
        }
    }

    /**
     * Returns the configured class name for translating group names.<p>
     *
     * @return the group translation class name
     */
    public String getGroupTranslationClass() {

        return m_groupTranslationClass;
    }

    /**
     * @see org.opencms.i18n.I_CmsLocaleHandler#getI18nInfo(javax.servlet.http.HttpServletRequest, org.opencms.file.CmsUser, org.opencms.file.CmsProject, java.lang.String)
     */
    public CmsI18nInfo getI18nInfo(HttpServletRequest req, CmsUser user, CmsProject project, String resource) {

        Locale locale = null;
        // try to read locale from session
        if (req != null) {
            // set the request character encoding
            try {
                req.setCharacterEncoding(m_encoding);
            } catch (UnsupportedEncodingException e) {
                // should not ever really happen
                LOG.error(Messages.get().getBundle().key(Messages.LOG_UNSUPPORTED_ENCODING_SET_1, m_encoding), e);
            }
            // read workplace settings
            HttpSession session = req.getSession(false);
            if (session != null) {
                CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute(
                    CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
                if (settings != null) {
                    locale = settings.getUserSettings().getLocale();
                }
            }
        }

        if (locale == null) {
            // no session available, try to read the locale form the user additional info
            if (!user.isGuestUser()) {
                // check user settings only for "real" users
                CmsUserSettings settings = new CmsUserSettings(user);
                locale = settings.getLocale();

            }
            if (req != null) {
                List<Locale> acceptedLocales = (new CmsAcceptLanguageHeaderParser(
                    req,
                    getDefaultLocale())).getAcceptedLocales();
                if ((locale != null) && (!acceptedLocales.contains(locale))) {
                    acceptedLocales.add(0, locale);
                }
                locale = OpenCms.getLocaleManager().getFirstMatchingLocale(acceptedLocales, m_locales);
            }

            // if no locale was found, use the default
            if (locale == null) {
                locale = getDefaultLocale();
            }
        }

        return new CmsI18nInfo(locale, m_encoding);
    }

    /**
     * Returns a list of site folders which generate labeled links.<p>
     *
     * @return a list of site folders which generate labeled links
     */
    public List<String> getLabelSiteFolders() {

        return m_labelSiteFolders;
    }

    /**
     * Returns the list of available workplace locales, sorted ascending.<p>
     *
     * Please note: Be careful not to modify the returned Set as it is not a clone.<p>
     *
     * @return the set of available workplace locales
     */
    public List<Locale> getLocales() {

        return m_locales;
    }

    /**
     * Returns the configured list of localized workplace folders.<p>
     *
     * @return the configured list of localized workplace folders
     */
    public List<String> getLocalizedFolders() {

        return m_localizedFolders;
    }

    /**
     * Returns the {@link CmsWorkplaceMessages} for the given locale.<p>
     *
     * The workplace messages are a collection of resource bundles, containing the messages
     * for all OpenCms core bundles and of all initialized modules.<p>
     *
     * Please note that the message objects are cached internally.
     * The returned message object should therefore never be modified directly in any way.<p>
     *
     * @param locale the locale to get the messages for
     *
     * @return the {@link CmsWorkplaceMessages} for the given locale
     */
    public CmsWorkplaceMessages getMessages(Locale locale) {

        CmsWorkplaceMessages result = m_messages.get(locale);
        if (result != null) {
            // messages have already been read
            return result;
        }

        // messages have not been read so far
        synchronized (this) {
            // check again
            result = m_messages.get(locale);
            if (result == null) {
                result = new CmsWorkplaceMessages(locale);
                m_messages.put(locale, result);
            }
        }
        return result;
    }

    /**
     * Returns the post upload handler.<p>
     *
     * @return the post upload handler
     */
    public I_CmsPostUploadDialogHandler getPostUploadHandler() {

        return m_postUploadHandler;
    }

    /**
     * Returns the condition definition for the given resource type that is triggered before opening the editor.<p>
     *
     * @param resourceType the resource type
     *
     * @return the condition definition for the given resource type class name or null if none is found
     */
    public I_CmsPreEditorActionDefinition getPreEditorConditionDefinition(I_CmsResourceType resourceType) {

        Iterator<I_CmsPreEditorActionDefinition> i = m_preEditorConditionDefinitions.iterator();
        I_CmsPreEditorActionDefinition result = null;
        int matchResult = -1;
        while (i.hasNext()) {
            I_CmsPreEditorActionDefinition currentDefinition = i.next();
            if (resourceType.getClass().isInstance(currentDefinition.getResourceType())) {
                // now determine the match count...
                int matchDistance = 0;
                Class<?> superClass = resourceType.getClass();
                while (true) {
                    // check if a super class is present
                    if (superClass == null) {
                        break;
                    }
                    if (superClass.getName().equals(currentDefinition.getResourceType().getClass().getName())) {
                        break;
                    }
                    matchDistance += 1;
                    superClass = superClass.getSuperclass();
                }
                if (matchResult != -1) {
                    if (matchDistance < matchResult) {
                        matchResult = matchDistance;
                        result = currentDefinition;
                    }
                } else {
                    matchResult = matchDistance;
                    result = currentDefinition;
                }
            }
        }
        return result;
    }

    /**
     * Returns the condition definitions for the different resource
     * types which are triggered before opening the editor.<p>
     *
     * @return the condition definitions
     */
    public List<I_CmsPreEditorActionDefinition> getPreEditorConditionDefinitions() {

        return m_preEditorConditionDefinitions;
    }

    /**
     * Returns the repository folder handler.<p>
     *
     * @return the repository folder handler
     */
    public I_CmsRepositoryFolderHandler getRepositoryFolderHandler() {

        if (m_repositoryFolderHandler == null) {
            // handler has not been configured, use the default one
            m_repositoryFolderHandler = new CmsRepositoryFolderHandler();
        }
        return m_repositoryFolderHandler;
    }

    /**
     * Gets the name of the role necessary for editing the sitemap configuration.
     *
     * @return the name of the role necessary for editing the sitemap configuration
     */
    public String getSitemapConfigEditRole() {

        return m_sitemapConfigEditRole;
    }

    /**
     * Returns Regex patterns that should be excluded from synchronization.<p>
     *
     * @return the exclude patterns
     */
    public ArrayList<Pattern> getSynchronizeExcludePatterns() {

        return m_synchronizeExcludePatterns;
    }

    /**
     * Returns the id of the temporary file project required by the editors.<p>
     *
     * @return the id of the temporary file project required by the editors
     */
    public CmsUUID getTempFileProjectId() {

        if (m_tempFileProject != null) {
            return m_tempFileProject.getUuid();
        } else {
            return null;
        }
    }

    /**
     * Returns the tool manager.<p>
     *
     * @return the tool manager
     */
    public CmsToolManager getToolManager() {

        if (m_toolManager == null) {
            m_toolManager = new CmsToolManager();
        }
        return m_toolManager;
    }

    /**
     * Gets the upload hook URI which should be opened for an upload to a given folder.<p>
     * This method will return null if no upload hook should be used for the given upload folder.<p>
     *
     * The API for this upload hook is as follows:
     *
     * The upload hook will be called with the following parameters:
     *
     * resources (required): a comma separated list of the structure ids of the uploaded resources
     *                       if this is omitted
     * closelink (optional): a link which should be opened once the upload hook has finished whatever
     *                       it is doing
     *
     * @param cms the current CMS context
     * @param uploadFolder the folder for which the upload hook should be found
     *
     * @return the URI of the upload hook or null
     */
    public String getUploadHook(CmsObject cms, String uploadFolder) {

        if (m_postUploadHandler != null) {
            return m_postUploadHandler.getUploadHook(cms, uploadFolder);
        }
        I_CmsDialogHandler handler = getDialogHandler(CmsDialogSelector.DIALOG_PROPERTY);
        if ((handler != null) && (handler instanceof I_CmsPostUploadDialogHandler)) {
            return ((I_CmsPostUploadDialogHandler)handler).getUploadHook(cms, uploadFolder);
        } else {
            return null;
        }
    }

    /**
     * Gets the upload restriction.
     *
     * @return the upload restriction
     */
    public I_CmsUploadRestriction getUploadRestriction() {

        return m_uploadRestriction;
    }

    /**
     * Returns the user additional information configuration Manager.<p>
     *
     * @return the user additional information configuration manager
     */
    public CmsWorkplaceUserInfoManager getUserInfoManager() {

        return m_userInfoManager;
    }

    /**
     * Returns the user list mode as a string.<p>
     *
     * @return the user list mode as a string
     */
    public String getUserListModeString() {

        return m_userListMode;
    }

    /**
     * Returns the map of configured workplace views.<p>
     *
     * @return the map of configured workplace views
     */
    public List<CmsWorkplaceView> getViews() {

        return m_views;
    }

    /**
     * Gets the configured virus scanner (may be null).
     *
     * @return the configured virus scanner
     */
    public I_CmsVirusScanner getVirusScanner() {

        return m_virusScanner;
    }

    /**
     * Returns the instantiated workplace editor manager class.<p>
     *
     * @return the instantiated workplace editor manager class
     */
    public CmsWorkplaceEditorManager getWorkplaceEditorManager() {

        return m_editorManager;
    }

    /**
     * Returns the list of explorer type settings configured in the opencms-workplace.xml file.<p>
     *
     * @return the list of explorer type settings
     */
    public List<CmsExplorerTypeSettings> getWorkplaceExplorerTypeSettings() {

        return Collections.unmodifiableList(m_explorerTypeSettingsFromXml);
    }

    /**
     * Returns the workplace locale from the current user's settings.<p>
     *
     * @param cms the current cms object
     *
     * @return the workplace locale
     */
    public Locale getWorkplaceLocale(CmsObject cms) {

        return getWorkplaceLocale(cms.getRequestContext());
    }

    /**
     * Gets the workplace locale for the given request context.<p>
     *
     * @param requestContext the request context
     *
     * @return the workplace locale for the request context
     */
    public Locale getWorkplaceLocale(CmsRequestContext requestContext) {

        Locale wpLocale = new CmsUserSettings(requestContext.getCurrentUser()).getLocale();
        if (wpLocale == null) {
            // fall back
            wpLocale = getDefaultLocale();
            if (wpLocale == null) {
                // fall back
                wpLocale = requestContext.getLocale();
            }
        }
        return wpLocale;

    }

    /**
     * Returns the workplace locale for the user.
     * @param user the user to get the workplace locale for.
     * @return the workplace locale for the user.
     */
    public Locale getWorkplaceLocale(CmsUser user) {

        Locale wpLocale = new CmsUserSettings(user).getLocale();
        if (wpLocale == null) {
            // fall back
            wpLocale = OpenCms.getWorkplaceManager().getDefaultLocale();
            if (wpLocale == null) {
                // fall back
                wpLocale = CmsLocaleManager.MASTER_LOCALE;
            }
        }
        return wpLocale;

    }

    /**
     * @see org.opencms.i18n.I_CmsLocaleHandler#initHandler(org.opencms.file.CmsObject)
     */
    public void initHandler(CmsObject cms) {

        // initialize the workplace locale set
        m_locales = initWorkplaceLocales(cms);
    }

    /**
     * Initializes the workplace manager with the OpenCms system configuration.<p>
     *
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     *
     * @throws CmsRoleViolationException if the provided OpenCms user context does
     *      not have <code>{@link CmsRole#WORKPLACE_MANAGER}</code> role permissions
     * @throws CmsException if something goes wrong
     */
    public synchronized void initialize(CmsObject cms) throws CmsException, CmsRoleViolationException {

        try {
            // ensure that the current user has permissions to initialize the workplace
            OpenCms.getRoleManager().checkRole(cms, CmsRole.WORKPLACE_MANAGER);

            // set the workplace encoding
            try {
                // workplace encoding is set on the workplace parent folder /system/workplace/
                CmsResource wpFolderRes = cms.readResource(CmsWorkplace.VFS_PATH_WORKPLACE);
                m_encoding = CmsLocaleManager.getResourceEncoding(cms, wpFolderRes);
            } catch (CmsVfsResourceNotFoundException e) {
                // workplace parent folder could not be read - use configured default encoding
                m_encoding = OpenCms.getSystemInfo().getDefaultEncoding();
            }

            // configure direct edit provider with default if not available
            if (m_directEditProvider == null) {
                m_directEditProvider = new CmsDirectEditDefaultProvider();
            }

            // throw away all currently configured module explorer types
            m_explorerTypeSettingsFromModules.clear();
            // now add the additional explorer types found in the modules
            CmsModuleManager moduleManager = OpenCms.getModuleManager();
            Iterator<String> moduleNameIterator = moduleManager.getModuleNames().iterator();
            while (moduleNameIterator.hasNext()) {
                CmsModule module = moduleManager.getModule(moduleNameIterator.next());
                if (module != null) {
                    addExplorerTypeSettings(module);
                }
            }
            // initialize the explorer type settings
            initExplorerTypeSettings();
            // initialize the workplace views
            initWorkplaceViews(cms);
            // initialize the workplace editor manager
            m_editorManager = new CmsWorkplaceEditorManager(cms);
            // initialize the locale handler
            initHandler(cms);

            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_VFS_ACCESS_INITIALIZED_0));
            }
            try {
                // read the temporary file project
                m_tempFileProject = cms.readProject(I_CmsProjectDriver.TEMP_FILE_PROJECT_NAME);
            } catch (CmsException e) {
                // during initial setup of OpenCms the temp file project does not yet exist...
                LOG.error(Messages.get().getBundle().key(Messages.LOG_NO_TEMP_FILE_PROJECT_0));
            }
            // create an instance of editor display options
            m_editorDisplayOptions = new CmsEditorDisplayOptions();

            // throw away all current gallery settings
            m_galleries.clear();
            // read out the configured gallery classes
            Iterator<I_CmsResourceType> typesIterator = OpenCms.getResourceManager().getResourceTypes().iterator();
            while (typesIterator.hasNext()) {
                I_CmsResourceType resourceType = typesIterator.next();
                if (resourceType instanceof CmsResourceTypeFolderExtended) {
                    // found a configured extended folder resource type
                    CmsResourceTypeFolderExtended galleryType = (CmsResourceTypeFolderExtended)resourceType;
                    String folderClassName = galleryType.getFolderClassName();
                    if (CmsStringUtil.isNotEmpty(folderClassName)) {
                        // only process this as a gallery if the folder name is not empty
                        try {
                            // check, if the folder class is a subclass of A_CmsGallery
                            if (A_CmsAjaxGallery.class.isAssignableFrom(Class.forName(folderClassName))) {
                                // create gallery class instance
                                A_CmsAjaxGallery galleryInstance = (A_CmsAjaxGallery)Class.forName(
                                    folderClassName).newInstance();
                                // set gallery folder resource type
                                galleryInstance.setResourceType(galleryType);
                                // store the gallery class instance with the type name as lookup key
                                m_galleries.put(galleryType.getTypeName(), galleryInstance);
                            }
                        } catch (ClassNotFoundException e) {
                            LOG.error(e.getLocalizedMessage());
                        } catch (InstantiationException e) {
                            LOG.error(e.getLocalizedMessage());
                        } catch (IllegalAccessException e) {
                            LOG.error(e.getLocalizedMessage());
                        }
                    }
                }
            }

            getDefaultUserSettings().initPreferences(this);

            // configures the tool manager
            getToolManager().configure(cms);

            flushMessageCache();
            getUploadRestriction().setAdminCmsObject(cms);

            // register this object as event listener
            OpenCms.addCmsEventListener(this, new int[] {I_CmsEventListener.EVENT_CLEAR_CACHES});
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            throw new CmsException(Messages.get().container(Messages.ERR_INITIALIZE_WORKPLACE_0));
        }
        m_adminCms = cms;
    }

    /**
     * Returns true if gallery upload is disabled for the user in the given context.
     *
     * @param cms a CMS context
     * @return true if the upload is disabled
     */
    public boolean isAdeGalleryUploadDisabled(CmsObject cms) {

        return !OpenCms.getRoleManager().hasRole(cms, getUploadRole());

    }

    /**
     * Checks if element authors have special permission to work in galleries (upload/replace).
     * @return true in the case above
     */

    public boolean isAllowElementAuthorToWorkInGalleries() {

        return m_allowElementAuthorToWorkInGalleries;
    }

    /**
     * Returns the default property editing mode on resources.<p>
     *
     * @return the default property editing mode on resources
     */
    public boolean isDefaultPropertiesOnStructure() {

        return m_defaultPropertiesOnStructure;
    }

    /**
     * Returns a flag, indicating if the categories should be displayed separated by repository in the category selection dialog.
     *
     * @return a flag, indicating if the categories should be displayed separated by repository in the category selection dialog.
     */
    public boolean isDisplayCategoriesByRepository() {

        return m_displayCategoriesByRepository;
    }

    /**
     * Returns a flag, indicating if the category selection dialog should have all entries completely collapsed when opened.
     *
     * @return a flag, indicating if the category selection dialog should have all entries completely collapsed when opened.
     */
    public boolean isDisplayCategorySelectionCollapsed() {

        return m_displayCategorySelectionCollapsed;
    }

    /**
     * Returns if tabs in the advanced property dialog are enabled.<p>
     *
     * @return <code>true</code> if tabs should be enabled, otherwise <code>false</code>
     */
    public boolean isEnableAdvancedPropertyTabs() {

        return m_enableAdvancedPropertyTabs;
    }

    /**
     * Returns true if "keep alive" mode is active.
     *
     * @return true if the session should be kept alive
     */
    public boolean isKeepAlive() {

        return isKeepAlive(true).booleanValue();
    }

    /**
     * Returns true if the session should be kept alive.<p>
     *
     * @param useDefault if true, the default value will be returned if the "keep alive" setting is not explicitly configured
     *
     * @return True if the "keep alive" mode is active
     */
    public Boolean isKeepAlive(boolean useDefault) {

        if (m_keepAlive != null) {
            return m_keepAlive;
        }
        if (useDefault) {
            return Boolean.TRUE;
        } else {
            return null;
        }

    }

    /**
     * Checks if the virus scanner is enabled.
     *
     * @return true if the virus scanner is enabled
     */
    public boolean isVirusScannerEnabled() {

        return m_virusScannerEnabled;
    }

    /**
     * Returns if XML content is automatically corrected when opened with the editor.<p>
     *
     * @return <code>true</code> if XML content is automatically corrected when opened with the editor, otherwise <code>false</code>
     */
    public boolean isXmlContentAutoCorrect() {

        return m_xmlContentAutoCorrect;
    }

    /**
     * Returns if lazy user lists are enabled.<p>
     *
     * @return <code>true</code> if lazy user lists are enabled
     */
    public boolean lazyUserListsEnabled() {

        return true;
    }

    /**
     * Removes the list of explorer type settings from the given module.<p>
     *
     * @param module the module witch contains the explorer type settings to remove
     */
    public void removeExplorerTypeSettings(CmsModule module) {

        List<CmsExplorerTypeSettings> explorerTypes = module.getExplorerTypes();
        if ((explorerTypes != null) && (explorerTypes.size() > 0)) {
            Iterator<CmsExplorerTypeSettings> i = explorerTypes.iterator();
            while (i.hasNext()) {
                CmsExplorerTypeSettings settings = i.next();
                if (m_explorerTypeSettingsFromModules.contains(settings)) {
                    m_explorerTypeSettingsFromModules.remove(settings);
                    if (CmsLog.INIT.isInfoEnabled()) {
                        CmsLog.INIT.info(
                            Messages.get().getBundle().key(
                                Messages.INIT_REMOVE_EXPLORER_TYPE_SETTING_1,
                                settings.getName()));
                    }
                }
            }
            // reset the list of all explorer type settings
            initExplorerTypeSettings();
        }
    }

    /**
     * Sets the value of the acacia-unlock configuration option.<p>
     *
     * @param value the value of the acacia-unlock configuration option
     */
    public void setAcaciaUnlock(String value) {

        m_acaciaUnlock = value;

    }

    /**
     * Sets the additional log folder configuration.<p>
     *
     * @param logConfig the additional log folder configuration
     */
    public void setAdditionalLogFolderConfiguration(CmsAdditionalLogFolderConfig logConfig) {

        m_logFolderConfig = logConfig;
    }

    /**
     * Enables/disables special permissions for element authors to work with galleries (upload/replace).
     * @param allowElementAuthorToWorkInGalleries true if the special permissions should be enabled for element authors
      */
    public void setAllowElementAuthorToWorkInGalleries(boolean allowElementAuthorToWorkInGalleries) {

        m_allowElementAuthorToWorkInGalleries = allowElementAuthorToWorkInGalleries;
    }

    /**
     * Sets if the autolock resources feature is enabled.<p>
     *
     * @param value <code>"true"</code> if the autolock resources feature is enabled, otherwise false
     */
    public void setAutoLock(String value) {

        m_autoLockResources = Boolean.valueOf(value).booleanValue();
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    m_autoLockResources ? Messages.INIT_AUTO_LOCK_ENABLED_0 : Messages.INIT_AUTO_LOCK_DISABLED_0));
        }
    }

    /**
     * Sets the category display options that affect how the category selection dialog is shown.
     *
     * @param displayCategoriesByRepository if true, the categories are shown separated by repository.
     * @param displayCategorySelectionCollapsed if true, the selection dialog opens showing only the top-level categories
     *              (or the various repositories) in collapsed state.
     */
    public void setCategoryDisplayOptions(
        String displayCategoriesByRepository,
        String displayCategorySelectionCollapsed) {

        m_displayCategoriesByRepository = Boolean.parseBoolean(displayCategoriesByRepository);
        m_displayCategorySelectionCollapsed = Boolean.parseBoolean(displayCategorySelectionCollapsed);
    }

    /**
     * Sets the name of the local category folder(s).<p>
     *
     * @param categoryFolder the name of the local category folder(s)
     */
    public void setCategoryFolder(String categoryFolder) {

        m_categoryFolder = categoryFolder;
    }

    /**
     * Sets the access object of the type settings.<p>
     *
     * @param access access object
     */
    public void setDefaultAccess(CmsExplorerTypeAccess access) {

        m_defaultAccess = access;
    }

    /**
     * Sets the Workplace default locale.<p>
     *
     * @param locale the locale to set
     */
    public void setDefaultLocale(String locale) {

        try {
            m_defaultLocale = CmsLocaleManager.getLocale(locale);
        } catch (Exception e) {
            if (CmsLog.INIT.isWarnEnabled()) {
                CmsLog.INIT.warn(Messages.get().getBundle().key(Messages.INIT_NONCRIT_ERROR_0), e);
            }
        }
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_DEFAULT_LOCALE_1, m_defaultLocale));
        }
    }

    /**
     * Sets the default property editing mode on resources.<p>
     *
     * @param defaultPropertiesOnStructure the default property editing mode on resources
     */
    public void setDefaultPropertiesOnStructure(String defaultPropertiesOnStructure) {

        m_defaultPropertiesOnStructure = Boolean.valueOf(defaultPropertiesOnStructure).booleanValue();
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    m_defaultPropertiesOnStructure
                    ? Messages.INIT_PROP_ON_STRUCT_TRUE_0
                    : Messages.INIT_PROP_ON_STRUCT_FALSE_0));
        }
    }

    /**
     * Sets the Workplace default user settings.<p>
     *
     * @param defaultUserSettings the user settings to set
     */
    public void setDefaultUserSettings(CmsDefaultUserSettings defaultUserSettings) {

        m_defaultUserSettings = defaultUserSettings;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.INIT_DEFAULT_USER_SETTINGS_1,
                    m_defaultUserSettings.getClass().getName()));
        }
    }

    /**
     * Sets the direct edit provider.<p>
     *
     * @param clazz the direct edit provider to set
     */
    public void setDirectEditProvider(I_CmsDirectEditProvider clazz) {

        m_directEditProvider = clazz;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.INIT_DIRECT_EDIT_PROVIDER_1,
                    m_directEditProvider.getClass().getName()));
        }
    }

    /**
     * Sets the editor action class.<p>
     *
     * @param clazz the editor action class to set
     */
    public void setEditorAction(I_CmsEditorActionHandler clazz) {

        m_editorAction = clazz;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.INIT_EDITOR_ACTION_CLASS_1,
                    m_editorAction.getClass().getName()));
        }
    }

    /**
     * Sets the editor display option class.<p>
     *
     * @param clazz the editor display option class to set
     */
    public void setEditorDisplayOptions(CmsEditorDisplayOptions clazz) {

        m_editorDisplayOptions = clazz;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.INIT_EDITOR_DISPLAY_OPTS_1,
                    m_editorAction.getClass().getName()));
        }
    }

    /**
     * Sets the editor handler class.<p>
     *
     * @param clazz the editor handler class to set
     */
    public void setEditorHandler(I_CmsEditorHandler clazz) {

        m_editorHandler = clazz;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.INIT_EDITOR_HANDLER_CLASS_1,
                    m_editorHandler.getClass().getName()));
        }
    }

    /**
     * Sets the element delete mode.<p>
     *
     * @param deleteMode the element delete mode
     */
    public void setElementDeleteMode(String deleteMode) {

        try {
            m_elementDeleteMode = ElementDeleteMode.valueOf(deleteMode);
        } catch (Throwable t) {
            m_elementDeleteMode = ElementDeleteMode.askDelete;
        }
    }

    /**
     * Sets if tabs in the advanced property dialog are enabled.<p>
     *
     * @param enableAdvancedPropertyTabs true if tabs should be enabled, otherwise false
     */
    public void setEnableAdvancedPropertyTabs(String enableAdvancedPropertyTabs) {

        m_enableAdvancedPropertyTabs = Boolean.valueOf(enableAdvancedPropertyTabs).booleanValue();
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    m_enableAdvancedPropertyTabs
                    ? Messages.INIT_ADV_PROP_DIALOG_SHOW_TABS_0
                    : Messages.INIT_ADV_PROP_DIALOG_HIDE_TABS_0));
        }
    }

    /**
     * Sets the value (in kb) for the maximum file upload size.<p>
     *
     * @param value the value (in kb) for the maximum file upload size
     */
    public void setFileMaxUploadSize(String value) {

        try {
            m_fileMaxUploadSize = Integer.valueOf(value).intValue();
        } catch (NumberFormatException e) {
            // can usually be ignored
            if (LOG.isInfoEnabled()) {
                LOG.info(e.getLocalizedMessage());
            }
            m_fileMaxUploadSize = -1;
        }
        if (CmsLog.INIT.isInfoEnabled()) {
            if (m_fileMaxUploadSize > 0) {
                CmsLog.INIT.info(
                    Messages.get().getBundle().key(
                        Messages.INIT_MAX_FILE_UPLOAD_SIZE_1,
                        Integer.valueOf(m_fileMaxUploadSize)));
            } else {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_MAX_FILE_UPLOAD_SIZE_UNLIMITED_0));
            }

        }
    }

    /**
     * Sets the system-wide file view settings for the workplace.<p>
     *
     * @param cms the CmsObject for ensuring security constraints.
     *
     * @param fileViewSettings the system-wide file view settings for the workplace to set
     *
     * @throws CmsRoleViolationException if the current user does not own the administrator role ({@link CmsRole#ROOT_ADMIN})
     * */
    public void setFileViewSettings(CmsObject cms, CmsRfsFileViewer fileViewSettings) throws CmsRoleViolationException {

        if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) {
            OpenCms.getRoleManager().checkRole(cms, CmsRole.ROOT_ADMIN);
        }
        m_fileViewSettings = fileViewSettings;
        // disallow modifications of this "new original"
        m_fileViewSettings.setFrozen(true);
    }

    /**
     * Sets the gallery default scope.<p>
     *
     * @param galleryDefaultScope the gallery default scope
     */
    public void setGalleryDefaultScope(String galleryDefaultScope) {

        m_galleryDefaultScope = galleryDefaultScope;
        try {
            CmsGallerySearchScope.valueOf(galleryDefaultScope);
        } catch (Throwable t) {
            LOG.warn(t.getLocalizedMessage(), t);
        }
    }

    /**
     * Sets the group translation class name.<p>
     *
     * @param translationClassName the group translation class name
     */
    public void setGroupTranslationClass(String translationClassName) {

        m_groupTranslationClass = translationClassName;
    }

    /**
     * Sets the "keep alive" mode.<p>
     *
     * @param keepAlive the keep-alive mode
     */
    public void setKeepAlive(String keepAlive) {

        m_keepAlive = Boolean.valueOf(keepAlive);
    }

    /**
     * Sets the post upload dialog handler.<p>
     *
     * @param uploadHandler the post upload handler
     */
    public void setPostUploadHandler(I_CmsPostUploadDialogHandler uploadHandler) {

        m_postUploadHandler = uploadHandler;
    }

    /**
     * Sets the repository folder handler.<p>
     *
     * @param clazz the repository folder handler
     */
    public void setRepositoryFolderHandler(I_CmsRepositoryFolderHandler clazz) {

        m_repositoryFolderHandler = clazz;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                org.opencms.configuration.Messages.get().getBundle().key(
                    org.opencms.configuration.Messages.INIT_REPOSITORY_FOLDER_1,
                    m_repositoryFolderHandler.getClass().getName()));
        }
    }

    /**
     * Sets the name of the role necessary for editing the sitemap configuration.
     *
     * @param roleName the name of the role necessary for editing the sitemap configuration
     */
    public void setSitemapConfigEditRole(String roleName) {

        m_sitemapConfigEditRole = roleName;
    }

    /**
     * Sets the tool Manager object.<p>
     *
     * @param toolManager the tool Manager object to set
     */
    public void setToolManager(CmsToolManager toolManager) {

        m_toolManager = toolManager;
    }

    /**
     * Sets the upload restriciton.
     *
     * @param uploadRestriction the upload restriction
     */
    public void setUploadRestriction(I_CmsUploadRestriction uploadRestriction) {

        if (m_uploadRestrictionSet) {
            throw new IllegalStateException("Upload restriction has already been set.");
        }
        m_uploadRestriction = uploadRestriction;
        m_uploadRestrictionSet = true;
    }

    /**
     * Sets the user additional information configuration manager.<p>
     *
     * @param userInfoManager the manager to set
     */
    public void setUserInfoManager(CmsWorkplaceUserInfoManager userInfoManager) {

        m_userInfoManager = userInfoManager;
    }

    /**
     * Sets the user list mode.<p>
     *
     * @param mode the user list mode
     */
    public void setUserListMode(String mode) {

        m_userListMode = mode;
    }

    /**
     * Controls if the user/group icon in the administration view should be shown.<p>
     *
     * @param value <code>"true"</code> if the user/group icon in the administration view should be shown, otherwise false
     */
    public void setUserManagementEnabled(String value) {

        m_showUserGroupIcon = Boolean.valueOf(value).booleanValue();
        if (CmsLog.INIT.isInfoEnabled()) {
            if (m_showUserGroupIcon) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_USER_MANAGEMENT_ICON_ENABLED_0));
            } else {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_USER_MANAGEMENT_ICON_DISABLED_0));
            }
        }
    }

    /**
     * Sets the virus scanner.
     *
     * @param virusScanner the virus scanner to set
     */
    public void setVirusScanner(I_CmsVirusScanner virusScanner) {

        m_virusScanner = virusScanner;
    }

    /**
     * Sets the virus scanner to enabled/disabled.
     *
     * @param enabled true if the virus scanner should be enabled
     */
    public void setVirusScannerEnabled(boolean enabled) {

        m_virusScannerEnabled = enabled;
    }

    /**
     * Sets the auto correction of XML contents when they are opened with the editor.<p>
     *
     * @param xmlContentAutoCorrect if "true", the content will be corrected without notification, otherwise a confirmation is needed
     */
    public void setXmlContentAutoCorrect(String xmlContentAutoCorrect) {

        m_xmlContentAutoCorrect = Boolean.valueOf(xmlContentAutoCorrect).booleanValue();
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    m_xmlContentAutoCorrect
                    ? Messages.INIT_XMLCONTENT_AUTOCORRECT_ENABLED_0
                    : Messages.INIT_XMLCONTENT_AUTOCORRECT_DISABLED_0));
        }
    }

    /**
     * Returns true if the Acacia editor in standalone mode should automatically unlock resources.<p>
     *
     * @return true if resources should be automatically unlocked in standalone mode
     */
    public boolean shouldAcaciaUnlock() {

        if (m_acaciaUnlock == null) {
            return true;
        } else {
            return Boolean.parseBoolean(m_acaciaUnlock);
        }
    }

    /**
     * Returns if the user/group icon in the administration view should be shown.<p>
     *
     * @return true if the user/group icon in the administration view should be shown, otherwise false
     */
    public boolean showUserGroupIcon() {

        return m_showUserGroupIcon;
    }

    /**
     * Returns true if lazy user lists should be used.<p>
     *
     * @return true if lazy user lists should be used
     */
    public boolean supportsLazyUserLists() {

        boolean result = "lazy".equalsIgnoreCase(m_userListMode);
        if (org.opencms.db.mssql.CmsUserDriver.isInstantiated()) {
            LOG.warn("Lazy user lists currently not supported on MSSQL, using classic user list mode as a fallback.");
            result = false;
        }
        return result;

    }

    /**
     * Translates a group name using the configured {@link I_CmsGroupNameTranslation}.<p>
     *
     * @param groupName the group name
     * @param keepOu if true, the OU will be appended to the translated name
     *
     * @return the translated group name
     */
    public String translateGroupName(String groupName, boolean keepOu) {

        I_CmsGroupNameTranslation translation = getGroupNameTranslation();
        return translation.translateGroupName(groupName, keepOu);
    }

    /**
     * Gets the default view name ( = explorer type) for the given type.<p>
     *
     * @param typeName a resource type name
     * @return the default view for the given type
     */
    String getDefaultView(String typeName) {

        String result = m_defaultViewRules.getViewForType(typeName);
        if (result == null) {
            result = "view_other";
            try {
                if (OpenCms.getResourceManager().hasResourceType(typeName)
                    && OpenCms.getResourceManager().getResourceType(typeName).isFolder()) {
                    result = "view_folders";
                }
            } catch (CmsLoaderException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Creates a copy of the admin cms object which is initialize with the data of the current cms object.<p>
     *
     * @param cms the current cms object
     * @return the new admin cms object
     *
     * @throws CmsException if something goes wrong
     */
    private CmsObject getAdminCms(CmsObject cms) throws CmsException {

        CmsObject adminCms = OpenCms.initCmsObject(m_adminCms);
        adminCms.getRequestContext().setSiteRoot(cms.getRequestContext().getSiteRoot());
        adminCms.getRequestContext().setRequestTime(cms.getRequestContext().getRequestTime());
        adminCms.getRequestContext().setCurrentProject(cms.getRequestContext().getCurrentProject());
        adminCms.getRequestContext().setEncoding(cms.getRequestContext().getEncoding());
        adminCms.getRequestContext().setUri(cms.getRequestContext().getUri());
        return adminCms;
    }

    /**
     * Returns a dummy group name translation which leaves the group names unchanged.<p>
     *
     * @return a dummy group name translation
     */
    private I_CmsGroupNameTranslation getDefaultGroupNameTranslation() {

        return new I_CmsGroupNameTranslation() {

            public String translateGroupName(String group, boolean keepOu) {

                return keepOu ? group : CmsOrganizationalUnit.getSimpleName(group);
            }
        };
    }

    /**
     * Returns the role required for enabling the upload functionality.
     *
     * @return the upload role
     */
    private CmsRole getUploadRole() {

        return isAllowElementAuthorToWorkInGalleries() ? CmsRole.ELEMENT_AUTHOR : CmsRole.EDITOR;
    }

    /**
     * Initializes the configured explorer type settings.<p>
     */
    private synchronized void initExplorerTypeSettings() {

        Map<String, CmsExplorerTypeSettings> explorerTypeSettingsMap = new HashMap<String, CmsExplorerTypeSettings>();
        List<CmsExplorerTypeSettings> explorerTypeSettings = new ArrayList<CmsExplorerTypeSettings>();

        if (m_defaultAccess.getAccessControlList() == null) {
            try {
                // initialize the default access control configuration
                m_defaultAccess.createAccessControlList(CmsExplorerTypeAccess.PRINCIPAL_DEFAULT);
            } catch (CmsException e) {
                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(
                        Messages.get().getBundle().key(
                            Messages.INIT_ADD_TYPE_SETTING_FAILED_1,
                            CmsExplorerTypeAccess.PRINCIPAL_DEFAULT),
                        e);
                }
            }
        }

        explorerTypeSettings.addAll(m_explorerTypeSettingsFromXml);
        explorerTypeSettings.addAll(m_explorerTypeSettingsFromModules);

        for (int i = 0; i < explorerTypeSettings.size(); i++) {
            CmsExplorerTypeSettings settings = explorerTypeSettings.get(i);
            // put the settings in the lookup map
            explorerTypeSettingsMap.put(settings.getName(), settings);
            if (getDefaultAccess() == settings.getAccess()) {
                continue;
            }
            try {
                // initialize the access control configuration of the explorer type
                settings.getAccess().createAccessControlList(settings.getName());
            } catch (CmsException e) {
                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(
                        Messages.get().getBundle().key(Messages.INIT_ADD_TYPE_SETTING_FAILED_1, settings.getName()),
                        e);
                }
            }
        }
        // sort the explorer type settings
        Collections.sort(explorerTypeSettings);
        // make the settings unmodifiable and store them in the global variables
        m_explorerTypeSettings = Collections.unmodifiableList(explorerTypeSettings);
        m_explorerTypeSettingsMap = Collections.unmodifiableMap(explorerTypeSettingsMap);

        m_explorerTypeViews = Maps.newHashMap();
        Set<String> explorerTypeViews = Sets.newHashSet();
        for (CmsExplorerTypeSettings explorerType : getExplorerTypeSettings()) {
            if (explorerType.isView()) {
                explorerTypeViews.add(explorerType.getName());
            }
        }

        for (String typeName : explorerTypeViews) {
            CmsExplorerTypeSettings explorerType = OpenCms.getWorkplaceManager().getExplorerTypeSetting(typeName);
            CmsElementView elemView = new CmsElementView(explorerType);
            m_explorerTypeViews.put(elemView.getId(), elemView);
        }

    }

    /**
     * Initializes the workplace locale set.<p>
     *
     * Currently, this is defined by the existence of a special folder
     * <code>/system/workplace/locales/{locale-name}/</code>.
     * This is likely to change in future implementations.<p>
     *
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     *
     * @return the workplace locale set
     */
    private List<Locale> initWorkplaceLocales(CmsObject cms) {

        Set<Locale> locales = new HashSet<Locale>();

        // collect locales from the VFS
        if (cms.existsResource(CmsWorkplace.VFS_PATH_LOCALES)) {
            List<CmsResource> localeFolders;
            try {
                localeFolders = cms.getSubFolders(CmsWorkplace.VFS_PATH_LOCALES);
            } catch (CmsException e) {
                LOG.warn(
                    Messages.get().getBundle().key(
                        Messages.LOG_WORKPLACE_INIT_NO_LOCALES_1,
                        CmsWorkplace.VFS_PATH_LOCALES),
                    e);
                // can not throw exception here since then OpenCms would not even start in shell mode (runlevel 2)
                localeFolders = new ArrayList<CmsResource>();
            }
            Iterator<CmsResource> i = localeFolders.iterator();
            while (i.hasNext()) {
                CmsFolder folder = (CmsFolder)i.next();
                Locale locale = CmsLocaleManager.getLocale(folder.getName());
                // add locale
                locales.add(locale);
                // add less specialized locale
                locales.add(new Locale(locale.getLanguage(), locale.getCountry()));
                // add even less specialized locale
                locales.add(new Locale(locale.getLanguage()));
            }
        }
        // collect locales from JAR manifests
        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources(MANIFEST_RESOURCE_NAME);

            while (resources.hasMoreElements()) {
                URL resUrl = resources.nextElement();
                try {
                    Manifest manifest = new Manifest(resUrl.openStream());
                    String localeString = manifest.getMainAttributes().getValue(LOCALIZATION_ATTRIBUTE_NAME);
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(localeString)) {
                        Locale locale = CmsLocaleManager.getLocale(localeString);
                        // add locale
                        locales.add(locale);
                        // add less specialized locale
                        locales.add(new Locale(locale.getLanguage(), locale.getCountry()));
                        // add even less specialized locale
                        locales.add(new Locale(locale.getLanguage()));
                    }
                } catch (IOException e) {
                    LOG.warn(
                        "Error reading manifest from " + resUrl + " while evaluating available workplace localization.",
                        e);
                }
            }
        } catch (IOException e) {
            LOG.error("Error evaluating available workplace localization from JAR manifests.", e);
        }

        // sort the result
        ArrayList<Locale> result = new ArrayList<Locale>();
        result.addAll(locales);
        Collections.sort(result, CmsLocaleComparator.getComparator());
        return result;
    }

    /**
     * Initializes the available workplace views.<p>
     *
     * Currently, this is defined by iterating the subfolder of the folder
     * <code>/system/workplace/views/</code>.
     * These subfolders must have the properties NavPos, NavText and default-file set.<p>
     *
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     * @return the available workplace views
     */
    private List<CmsWorkplaceView> initWorkplaceViews(CmsObject cms) {

        List<CmsResource> viewFolders;
        try {
            // get the subfolders of the "views" folder
            viewFolders = cms.getSubFolders(CmsWorkplace.VFS_PATH_VIEWS);
        } catch (CmsException e) {
            if ((OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) && LOG.isInfoEnabled()) {
                LOG.info(
                    Messages.get().getBundle().key(Messages.LOG_WORKPLACE_INIT_NO_VIEWS_1, CmsWorkplace.VFS_PATH_VIEWS),
                    e);
            }
            // can not throw exception here since then OpenCms would not even start in shell mode (runlevel 2)
            viewFolders = new ArrayList<CmsResource>();
        }
        m_views = new ArrayList<CmsWorkplaceView>(viewFolders.size());
        for (int i = 0; i < viewFolders.size(); i++) {
            // loop through all view folders
            CmsFolder folder = (CmsFolder)viewFolders.get(i);
            String folderPath = cms.getSitePath(folder);
            try {
                // get view information from folder properties
                String order = cms.readPropertyObject(
                    folderPath,
                    CmsPropertyDefinition.PROPERTY_NAVPOS,
                    false).getValue();
                String key = cms.readPropertyObject(
                    folderPath,
                    CmsPropertyDefinition.PROPERTY_NAVTEXT,
                    false).getValue();
                String viewUri = cms.readPropertyObject(
                    folderPath,
                    CmsPropertyDefinition.PROPERTY_DEFAULT_FILE,
                    false).getValue();
                if (viewUri == null) {
                    // no view URI found
                    viewUri = folderPath;
                } else if (!viewUri.startsWith("/")) {
                    // default file is in current view folder, create absolute path to view URI
                    viewUri = folderPath + viewUri;
                }
                if (order == null) {
                    // no valid NavPos property value found, use loop count as order value
                    order = "" + i;
                }
                Float orderValue;
                try {
                    // create Float order object
                    orderValue = Float.valueOf(order);
                } catch (NumberFormatException e) {
                    // String was not formatted correctly, use loop counter
                    orderValue = Float.valueOf(i);
                }
                if (key == null) {
                    // no language key found, use default String to avoid NullPointerException
                    key = "View " + i;
                    // if no navtext is given do not display the view
                    continue;
                }
                // create new view object
                CmsWorkplaceView view = new CmsWorkplaceView(key, viewUri, orderValue);
                m_views.add(view);
                // log the view
                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_WORKPLACE_VIEW_1, view.getUri()));
                }
            } catch (CmsException e) {
                // should usually never happen
                LOG.error(Messages.get().getBundle().key(Messages.LOG_READING_VIEW_FOLDER_FAILED_1, folderPath), e);
            }
        }
        // sort the views by their order number
        Collections.sort(m_views);
        return m_views;
    }

}

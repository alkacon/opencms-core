/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.ade.galleries.shared.CmsGallerySearchScope;
import org.opencms.configuration.CmsDefaultUserSettings;
import org.opencms.db.CmsExportPoint;
import org.opencms.db.CmsUserSettings;
import org.opencms.db.I_CmsProjectDriver;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeFolderExtended;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsAcceptLanguageHeaderParser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsI18nInfo;
import org.opencms.i18n.CmsLocaleComparator;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.I_CmsLocaleHandler;
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
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsRfsFileViewer;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.editors.CmsEditorDisplayOptions;
import org.opencms.workplace.editors.CmsEditorHandler;
import org.opencms.workplace.editors.CmsWorkplaceEditorManager;
import org.opencms.workplace.editors.I_CmsEditorActionHandler;
import org.opencms.workplace.editors.I_CmsEditorCssHandler;
import org.opencms.workplace.editors.I_CmsEditorHandler;
import org.opencms.workplace.editors.I_CmsPreEditorActionDefinition;
import org.opencms.workplace.editors.directedit.CmsDirectEditDefaultProvider;
import org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider;
import org.opencms.workplace.explorer.CmsExplorerContextMenu;
import org.opencms.workplace.explorer.CmsExplorerTypeAccess;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.menu.CmsMenuRule;
import org.opencms.workplace.galleries.A_CmsAjaxGallery;
import org.opencms.workplace.tools.CmsToolManager;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;

/**
 * Manages the global OpenCms workplace settings for all users.<p>
 * 
 * This class reads the settings from the "opencms.properties" and stores them in member variables.
 * For each setting one or more get methods are provided.<p>
 * 
 * @since 6.0.0 
 */
public final class CmsWorkplaceManager implements I_CmsLocaleHandler, I_CmsEventListener {

    /** The default encoding for the workplace (UTF-8). */
    public static final String DEFAULT_WORKPLACE_ENCODING = CmsEncoder.ENCODING_UTF_8;

    /** The id of the "requestedResource" parameter for the OpenCms login form. */
    public static final String PARAM_LOGIN_REQUESTED_RESOURCE = "requestedResource";

    /** Key name for the session workplace settings. */
    public static final String SESSION_WORKPLACE_SETTINGS = "__CmsWorkplace.WORKPLACE_SETTINGS";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsWorkplaceManager.class);

    /** Value of the acacia-unlock configuration option (may be null if not set). */
    private String m_acaciaUnlock;

    /** The admin cms context. */
    private CmsObject m_adminCms;

    /** Indicates if auto-locking of resources is enabled or disabled. */
    private boolean m_autoLockResources;

    /** The name of the local category folder(s). */
    private String m_categoryFolder;

    /** The customized workplace foot. */
    private CmsWorkplaceCustomFoot m_customFoot;

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

    /** The configured list of menu rule sets. */
    private List<CmsMenuRule> m_menuRules;

    /** The configured menu rule sets as Map with the rule name as key. */
    private Map<String, CmsMenuRule> m_menuRulesMap;

    /** The workplace localized messages (mapped to the locales). */
    private Map<Locale, CmsWorkplaceMessages> m_messages;

    /** The configured multi context menu. */
    private CmsExplorerContextMenu m_multiContextMenu;

    /** The condition definitions for the resource types  which are triggered before opening the editor. */
    private List<I_CmsPreEditorActionDefinition> m_preEditorConditionDefinitions;

    /** The repository folder handler. */
    private I_CmsRepositoryFolderHandler m_repositoryFolderHandler;

    /** Indicates if the user management icon should be displayed in the workplace. */
    private boolean m_showUserGroupIcon;

    /** Exclude patterns for synchronization. */
    private ArrayList<Pattern> m_synchronizeExcludePatterns;

    /** The temporary file project used by the editors. */
    private CmsProject m_tempFileProject;

    /** The tool manager. */
    private CmsToolManager m_toolManager;

    /** The user additional information configuration. */
    private CmsWorkplaceUserInfoManager m_userInfoManager;

    /** The user list mode. */
    private String m_userListMode;

    /** The configured workplace views. */
    private List<CmsWorkplaceView> m_views;

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
        m_menuRules = new ArrayList<CmsMenuRule>();
        m_menuRulesMap = new HashMap<String, CmsMenuRule>();
        flushMessageCache();
        m_multiContextMenu = new CmsExplorerContextMenu();
        m_multiContextMenu.setMultiMenu(true);
        m_preEditorConditionDefinitions = new ArrayList<I_CmsPreEditorActionDefinition>();
        m_editorCssHandlers = new ArrayList<I_CmsEditorCssHandler>();
        m_customFoot = new CmsWorkplaceCustomFoot();
        m_synchronizeExcludePatterns = new ArrayList<Pattern>();

        // important to set this to null to avoid unnecessary overhead during configuration phase
        m_explorerTypeSettings = null;
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
            return (null != session.getAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS));
        }
        // no session means no workplace use
        return false;
    }

    /**
     * Adds a dialog handler instance to the list of configured dialog handlers.<p>
     * 
     * @param clazz the instantiated dialog handler to add
     */
    public void addDialogHandler(I_CmsDialogHandler clazz) {

        m_dialogHandler.put(clazz.getDialogHandler(), clazz);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
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
            I_CmsEditorCssHandler editorCssHandler = (I_CmsEditorCssHandler)Class.forName(editorCssHandlerClassName).newInstance();
            m_editorCssHandlers.add(editorCssHandler);
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(
                    Messages.INIT_EDITOR_CSSHANDLER_CLASS_1,
                    editorCssHandlerClassName));
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
            I_CmsEditorCssHandler editorCssHandler = (I_CmsEditorCssHandler)Class.forName(editorCssHandlerClassName).newInstance();

            List<I_CmsEditorCssHandler> editorCssHandlers = new ArrayList<I_CmsEditorCssHandler>();
            editorCssHandlers.add(editorCssHandler);
            editorCssHandlers.addAll(m_editorCssHandlers);

            m_editorCssHandlers = editorCssHandlers;

            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(
                    Messages.INIT_EDITOR_CSSHANDLER_CLASS_1,
                    editorCssHandlerClassName));
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
                    CmsLog.INIT.info(Messages.get().getBundle().key(
                        Messages.INIT_ADD_TYPE_SETTING_1,
                        settings.getName()));
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
            CmsLog.INIT.info(Messages.get().getBundle().key(
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
     * Adds a menu rule set from the workplace configuration to the configured menu rules.<p>
     * 
     * @param menuRule the menu rule to add
     */
    public void addMenuRule(CmsMenuRule menuRule) {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_ADD_MENURULE_1, menuRule.getName()));
        }
        m_menuRules.add(menuRule);
    }

    /**
     * Adds an initialized condition definition class that is triggered before opening the editor.<p>
     * 
     * @param preEditorCondition the initialized condition definition class
     */
    public void addPreEditorConditionDefinition(I_CmsPreEditorActionDefinition preEditorCondition) {

        m_preEditorConditionDefinitions.add(preEditorCondition);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
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
                CmsLog.INIT.info(Messages.get().getBundle().key(
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
            throw new CmsPermissionViolationException(org.opencms.db.Messages.get().container(
                org.opencms.db.Messages.ERR_PERM_DENIED_2,
                resourceName,
                "w"));
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
     * Returns the name of the local category folder(s).<p>
     * 
     * @return the name of the local category folder(s)
     */
    public String getCategoryFolder() {

        return m_categoryFolder;
    }

    /**
     * Returns the customized workplace foot.<p>
     * 
     * @return the customized workplace foot
     */
    public CmsWorkplaceCustomFoot getCustomFoot() {

        return m_customFoot;
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
                m_groupNameTranslation = (I_CmsGroupNameTranslation)Class.forName(m_groupTranslationClass).newInstance();
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
                CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
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
                List<Locale> acceptedLocales = (new CmsAcceptLanguageHeaderParser(req, getDefaultLocale())).getAcceptedLocales();
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
     * Returns the menu rule set with the given name.<p>
     * 
     * If no rule set with the specified name is found, <code>null</code> is returned.<p>
     * 
     * @param ruleName the name of the rule set to get
     * 
     * @return the menu rule set with the given name
     */
    public CmsMenuRule getMenuRule(String ruleName) {

        return m_menuRulesMap.get(ruleName);
    }

    /**
     * Returns the configured menu rule sets.<p>
     * 
     * @return the configured menu rule sets
     */
    public List<CmsMenuRule> getMenuRules() {

        return m_menuRules;
    }

    /**
     * Returns the configured menu rule sets as Map.<p>
     * 
     * @return the configured menu rule sets as Map
     */
    public Map<String, CmsMenuRule> getMenuRulesMap() {

        return m_menuRulesMap;
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
            result = new CmsWorkplaceMessages(locale);
            m_messages.put(locale, result);
        }
        return result;
    }

    /**
     * Returns the configured multi context menu to use in the Explorer view.<p>
     * 
     * @return the configured multi context menu to use in the Explorer view
     */
    public CmsExplorerContextMenu getMultiContextMenu() {

        return m_multiContextMenu;
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

        I_CmsDialogHandler handler = getDialogHandler(CmsDialogSelector.DIALOG_PROPERTY);
        if ((handler != null) && (handler instanceof I_CmsPostUploadDialogHandler)) {
            return ((I_CmsPostUploadDialogHandler)handler).getUploadHook(cms, uploadFolder);
        } else {
            return null;
        }
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
     * Returns the instantiated workplace editor manager class.<p>
     * 
     * @return the instantiated workplace editor manager class
     */
    public CmsWorkplaceEditorManager getWorkplaceEditorManager() {

        return m_editorManager;
    }

    /**
     * Returns the workplace locale from the current user's settings.<p>
     * 
     * @param cms the current cms object 
     * 
     * @return the workplace locale
     */
    public Locale getWorkplaceLocale(CmsObject cms) {

        Locale wpLocale = new CmsUserSettings(cms.getRequestContext().getCurrentUser()).getLocale();
        if (wpLocale == null) {
            // fall back
            wpLocale = getDefaultLocale();
            if (wpLocale == null) {
                // fall back
                wpLocale = cms.getRequestContext().getLocale();
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
            // initialize the menu rules
            initMenuRules();
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
                                A_CmsAjaxGallery galleryInstance = (A_CmsAjaxGallery)Class.forName(folderClassName).newInstance();
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
     * Returns the default property editing mode on resources.<p>
     *
     * @return the default property editing mode on resources
     */
    public boolean isDefaultPropertiesOnStructure() {

        return m_defaultPropertiesOnStructure;
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
                        CmsLog.INIT.info(Messages.get().getBundle().key(
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
     * Sets if the autolock resources feature is enabled.<p>
     * 
     * @param value <code>"true"</code> if the autolock resources feature is enabled, otherwise false
     */
    public void setAutoLock(String value) {

        m_autoLockResources = Boolean.valueOf(value).booleanValue();
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
                m_autoLockResources ? Messages.INIT_AUTO_LOCK_ENABLED_0 : Messages.INIT_AUTO_LOCK_DISABLED_0));
        }
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
     * Sets the customized workplace foot.<p>
     * 
     * @param footCustom the customized workplace foot
     */
    public void setCustomFoot(CmsWorkplaceCustomFoot footCustom) {

        m_customFoot = footCustom;
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
            CmsLog.INIT.info(Messages.get().getBundle().key(
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
            CmsLog.INIT.info(Messages.get().getBundle().key(
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
            CmsLog.INIT.info(Messages.get().getBundle().key(
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
            CmsLog.INIT.info(Messages.get().getBundle().key(
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
            CmsLog.INIT.info(Messages.get().getBundle().key(
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
            CmsLog.INIT.info(Messages.get().getBundle().key(
                Messages.INIT_EDITOR_HANDLER_CLASS_1,
                m_editorHandler.getClass().getName()));
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
            CmsLog.INIT.info(Messages.get().getBundle().key(
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
                CmsLog.INIT.info(Messages.get().getBundle().key(
                    Messages.INIT_MAX_FILE_UPLOAD_SIZE_1,
                    new Integer(m_fileMaxUploadSize)));
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
     * Sets the configured multi context menu to use in the Explorer view.<p>
     * 
     * @param multiContextMenu the configured multi context menu to use in the Explorer view
     */
    public void setMultiContextMenu(CmsExplorerContextMenu multiContextMenu) {

        multiContextMenu.setMultiMenu(true);
        m_multiContextMenu = multiContextMenu;
    }

    /**
     * Sets the repository folder handler.<p>
     * 
     * @param clazz the repository folder handler
     */
    public void setRepositoryFolderHandler(I_CmsRepositoryFolderHandler clazz) {

        m_repositoryFolderHandler = clazz;
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(org.opencms.configuration.Messages.get().getBundle().key(
                org.opencms.configuration.Messages.INIT_REPOSITORY_FOLDER_1,
                m_repositoryFolderHandler.getClass().getName()));
        }
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
     * Sets the auto correction of XML contents when they are opened with the editor.<p>
     * 
     * @param xmlContentAutoCorrect if "true", the content will be corrected without notification, otherwise a confirmation is needed
     */
    public void setXmlContentAutoCorrect(String xmlContentAutoCorrect) {

        m_xmlContentAutoCorrect = Boolean.valueOf(xmlContentAutoCorrect).booleanValue();
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(
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

        return "lazy".equalsIgnoreCase(m_userListMode);
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
    }

    /**
     * Initializes the configured menu rule sets.<p>
     */
    private void initMenuRules() {

        Iterator<CmsMenuRule> i = m_menuRules.iterator();
        while (i.hasNext()) {
            CmsMenuRule currentRule = i.next();
            // freeze the current rule set
            currentRule.freeze();
            // put the rule set to the Map with the name as key
            m_menuRulesMap.put(currentRule.getName(), currentRule);
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
        List<CmsResource> localeFolders;
        try {
            localeFolders = cms.getSubFolders(CmsWorkplace.VFS_PATH_LOCALES);
        } catch (CmsException e) {
            LOG.error(Messages.get().getBundle().key(
                Messages.LOG_WORKPLACE_INIT_NO_LOCALES_1,
                CmsWorkplace.VFS_PATH_LOCALES));
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
            if ((OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) && LOG.isErrorEnabled()) {
                LOG.error(
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
                String order = cms.readPropertyObject(folderPath, CmsPropertyDefinition.PROPERTY_NAVPOS, false).getValue();
                String key = cms.readPropertyObject(folderPath, CmsPropertyDefinition.PROPERTY_NAVTEXT, false).getValue();
                String viewUri = cms.readPropertyObject(folderPath, CmsPropertyDefinition.PROPERTY_DEFAULT_FILE, false).getValue();
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
                    orderValue = Float.valueOf("" + i);
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

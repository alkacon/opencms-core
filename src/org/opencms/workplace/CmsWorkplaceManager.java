/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsWorkplaceManager.java,v $
 * Date   : $Date: 2005/06/17 09:24:10 $
 * Version: $Revision: 1.64 $
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

package org.opencms.workplace;

import org.opencms.configuration.CmsDefaultUserSettings;
import org.opencms.db.CmsExportPoint;
import org.opencms.db.CmsUserSettings;
import org.opencms.db.I_CmsProjectDriver;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeFolderExtended;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsAcceptLanguageHeaderParser;
import org.opencms.i18n.CmsI18nInfo;
import org.opencms.i18n.CmsLocaleComparator;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.I_CmsLocaleHandler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleManager;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsRfsFileViewer;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.editors.CmsEditorDisplayOptions;
import org.opencms.workplace.editors.CmsEditorHandler;
import org.opencms.workplace.editors.CmsWorkplaceEditorManager;
import org.opencms.workplace.editors.I_CmsEditorActionHandler;
import org.opencms.workplace.editors.I_CmsEditorHandler;
import org.opencms.workplace.explorer.CmsExplorerTypeAccess;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.galleries.A_CmsGallery;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;

/**
 * Manages the global OpenCms workplace settings for all users.<p>
 * 
 * This class reads the settings from the "opencms.properties" and stores them in member variables.
 * For each setting one or more get methods are provided.<p>
 * 
 * @author Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.64 $
 * 
 * @since 5.3.1
 */
public final class CmsWorkplaceManager implements I_CmsLocaleHandler {

    /** The default encoding for the workplace (UTF-8). */
    // TODO: Encoding feature of the workplace is not active 
    public static final String C_DEFAULT_WORKPLACE_ENCODING = "UTF-8";

    /** Key name for the session workplace settings. */
    public static final String C_SESSION_WORKPLACE_SETTINGS = "__CmsWorkplace.WORKPLACE_SETTINGS";

    /** The id of the "requestedResource" parameter for the OpenCms login form. */
    public static final String PARAM_LOGIN_REQUESTED_RESOURCE = "requestedResource";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsWorkplaceManager.class);

    /** Indicates if auto-locking of resources is enabled or disabled. */
    private boolean m_autoLockResources;

    /** The default acces for explorer types. */
    private CmsExplorerTypeAccess m_defaultAccess;

    /** The configured default locale of the workplace. */
    private Locale m_defaultLocale;

    /** The default property setting for setting new property values. */
    private boolean m_defaultPropertiesOnStructure;

    /** The default user seetings. */
    private CmsDefaultUserSettings m_defaultUserSettings;

    /** The configured dialog handlers. */
    private Map m_dialogHandler;

    /** The edit action handler. */
    private I_CmsEditorActionHandler m_editorAction;

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
    private List m_explorerTypeSettings;

    /** The explorer type settings from the configured modules. */
    private List m_explorerTypeSettingsFromModules;

    /** The explorer type settings from the XML configuration. */
    private List m_explorerTypeSettingsFromXml;

    /** The explorer type settings as Map with resource type name as key. */
    private Map m_explorerTypeSettingsMap;

    /** The workplace export points. */
    private Set m_exportPoints;

    /** Maximum size of an upload file. */
    private int m_fileMaxUploadSize;

    /** The instance used for reading portions of lines of a file to choose. */
    private CmsRfsFileViewer m_fileViewSettings;

    /** The configured workplace galleries. */
    private Map m_galleries;

    /** Contains all folders that should be labled if siblings exist. */
    private List m_labelSiteFolders;

    /** List of installed workplace locales, soreted ascending. */
    private List m_locales;

    /** The configured list of localized workplace folders. */
    private List m_localizedFolders;

    /** The workplace localized messages (mapped to the locales). */
    private Map m_messages;

    /** Indicates if the user managemet icon should be displayed in the workplace. */
    private boolean m_showUserGroupIcon;

    /** The temporary file project used by the editors. */
    private CmsProject m_tempFileProject;

    /** The tool manager. */
    private CmsToolManager m_toolManager;

    /** The configured workplace views. */
    private List m_views;

    /**
     * Creates a new instance for the workplace manager, will be called by the workplace configuration manager.<p>
     */
    public CmsWorkplaceManager() {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_WORKPLACE_INITIALIZE_START_0));
        }
        m_locales = new ArrayList();
        m_labelSiteFolders = new ArrayList();
        m_localizedFolders = new ArrayList();
        m_autoLockResources = true;
        m_showUserGroupIcon = true;
        m_dialogHandler = new HashMap();
        m_views = new ArrayList();
        m_exportPoints = new HashSet();
        m_editorHandler = new CmsEditorHandler();
        m_fileMaxUploadSize = -1;
        m_fileViewSettings = new CmsRfsFileViewer();
        m_explorerTypeSettingsFromXml = new ArrayList();
        m_explorerTypeSettingsFromModules = new ArrayList();
        m_defaultPropertiesOnStructure = true;
        m_enableAdvancedPropertyTabs = true;
        m_defaultUserSettings = new CmsDefaultUserSettings();
        m_defaultAccess = new CmsExplorerTypeAccess();
        m_galleries = new HashMap();
        m_messages = new HashMap();

        // important to set this to null to avoid unneccessary overhead during configuration phase
        m_explorerTypeSettings = null;
    }

    /**
     * Returns true if the provided request was done by a Workplace user.<p>
     * 
     * @param req the request to check
     * @return true if the provided request was done by a Workplace user
     */
    public static boolean isWorkplaceUser(HttpServletRequest req) {

        HttpSession session = req.getSession(false);
        if (session != null) {
            // if a session is available, check for a workplace configuration
            return null != session.getAttribute(CmsWorkplaceManager.C_SESSION_WORKPLACE_SETTINGS);
        }
        // no session means no workplace use
        return false;
    }

    /**
     * Adds a dialog handler instance to the list of configured dialog handlers.<p>
     * 
     * @param clazz the instanciated dialog handler to add
     */
    public void addDialogHandler(I_CmsDialogHandler clazz) {

        m_dialogHandler.put(clazz.getDialogHandler(), clazz);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(
                Messages.INIT_ADD_DIALOG_HANDLER_2,
                clazz.getDialogHandler(),
                clazz.getClass().getName()));
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
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_ADD_TYPE_SETTING_1, settings.getName()));
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

        List explorerTypes = module.getExplorerTypes();
        if ((explorerTypes != null) && (explorerTypes.size() > 0)) {
            Iterator i = explorerTypes.iterator();
            while (i.hasNext()) {
                CmsExplorerTypeSettings settings = (CmsExplorerTypeSettings)i.next();
                m_explorerTypeSettingsFromModules.add(settings);
                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(Messages.get().key(Messages.INIT_ADD_TYPE_SETTING_1, settings.getName()));
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
            CmsLog.INIT.info(Messages.get().key(
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
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_LABEL_LINKS_IN_FOLDER_1, uri));
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
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_WORKPLACE_LOCALIZED_1, uri));
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
     * Returns all instanciated dialog handlers for the workplace.<p>
     * 
     * @return all instanciated dialog handlers for the workplace
     */
    public Map getDialogHandler() {

        return m_dialogHandler;
    }

    /**
     * Returns the instanciated dialog handler class for the key or null, if there is no mapping for the key.<p>
     *  
     * @param key the key whose associated value is to be returned
     * @return the instanciated dialog handler class for the key
     */
    public Object getDialogHandler(String key) {

        return m_dialogHandler.get(key);
    }

    /**
     * Returns the instanciated editor action handler class.<p>
     * 
     * @return the instanciated editor action handler class
     */
    public I_CmsEditorActionHandler getEditorActionHandler() {

        return m_editorAction;
    }

    /**
     * Returns the instanciated editor display option class.<p>
     * 
     * @return the instanciated editor display option class
     */
    public CmsEditorDisplayOptions getEditorDisplayOptions() {

        return m_editorDisplayOptions;
    }

    /**
     * Returns the instanciated editor handler class.<p>
     * 
     * @return the instanciated editor handler class
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
     * @return the explorer type settings for the specified resource type
     */
    public CmsExplorerTypeSettings getExplorerTypeSetting(String type) {

        return (CmsExplorerTypeSettings)m_explorerTypeSettingsMap.get(type);
    }

    /**
     * Returns the list of explorer type settings.<p>
     * 
     * These settings provide information for the new resource dialog and the context menu appearance.<p>
     * 
     * @return the list of explorer type settings
     */
    public List getExplorerTypeSettings() {

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
    public Set getExportPoints() {

        return m_exportPoints;
    }

    /**
     * Returns the value (in bytes) for the maximum file upload size of the current user.<p>
     * 
     * @param cms the initialized CmsObject
     * @return the value (in bytes) for the maximum file upload size
     */
    public long getFileBytesMaxUploadSize(CmsObject cms) {

        int maxFileSize = getFileMaxUploadSize();
        long maxFileSizeBytes = maxFileSize * 1024;
        // check if current user belongs to Admin group, if so no file upload limit
        if ((maxFileSize <= 0) || cms.hasRole(CmsRole.VFS_MANAGER)) {
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
     * <code>{@link org.opencms.main.CmsRuntimeException}</code> will be thrown. 
     * It has to be cloned first and then may be written back to the workplace settings using 
     * method {@link #setFileViewSettings(CmsObject, org.opencms.util.CmsRfsFileViewer)}.
     * 
     * @return the system-wide file view settings for the workplace
     */
    public CmsRfsFileViewer getFileViewSettings() {

        return m_fileViewSettings;
    }

    /**
     * Returns a collection of all gallery class names.<p>
     * 
     * @return a collection of all gallery class names
     */
    public Map getGalleries() {

        return m_galleries;
    }

    /**
     * Returns the configured class name for the given gallery type name.<p>
     * 
     * If no gallery type of the given name is configured, <code>null</code> is returned.<p>
     * 
     * @param galleryTypeName the gallery type name to look up
     * 
     * @return the configured class name for the given gallery type name
     */
    public String getGalleryClassName(String galleryTypeName) {

        return ((CmsResourceTypeFolderExtended)m_galleries.get(galleryTypeName)).getFolderClassName();
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
                LOG.error(Messages.get().key(Messages.LOG_UNSUPPORTED_ENCODING_SET_1, m_encoding), e);
            }
            // read workplace settings
            HttpSession session = req.getSession(false);
            if (session != null) {
                CmsWorkplaceSettings settings = (CmsWorkplaceSettings)session.getAttribute(CmsWorkplaceManager.C_SESSION_WORKPLACE_SETTINGS);
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
                List acceptedLocales = (new CmsAcceptLanguageHeaderParser(req, getDefaultLocale())).getAcceptedLocales();
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
    public List getLabelSiteFolders() {

        return m_labelSiteFolders;
    }

    /**
     * Returns the list of available workplace locales, sorted ascending.<p>
     * 
     * Please note: Be careful not to modify the returned Set as it is not a clone.<p>
     * 
     * @return the set of available workplace locales
     */
    public List getLocales() {

        return m_locales;
    }

    /**
     * Returns the configured list of localized workplace folders.<p>
     * 
     * @return the configured list of localized workplace folders
     */
    public List getLocalizedFolders() {

        return m_localizedFolders;
    }

    /**
     * Returns the workplace messages for the given locale.<p>
     * 
     * The workplace messages are a collection of resource bundles, one for the basic
     * workplace, and (optionally) one for each initialized module.<p>
     * 
     * @param locale the locale to get the messages for
     * @return the workplace messages for the given locale
     */
    public CmsMessages getMessages(Locale locale) {

        CmsMessages result = (CmsMessages)m_messages.get(locale);
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
     * Returns the id of the temporary file project required by the editors.<p>
     * 
     * @return the id of the temporary file project required by the editors
     */
    public int getTempFileProjectId() {

        if (m_tempFileProject != null) {
            return m_tempFileProject.getId();
        } else {
            return -1;
        }
    }

    /**
     * Returns the tool manager.<p>
     * 
     * @return the tool manager
     */
    public CmsToolManager getToolManager() {

        return m_toolManager;
    }

    /**
     * Returns the map of configured workplace views.<p>
     * 
     * @return the map of configured workplace views
     */
    public List getViews() {

        return m_views;
    }

    /**
     * Returns the instanciated workplace editor manager class.<p>
     * 
     * @return the instanciated workplace editor manager class
     */
    public CmsWorkplaceEditorManager getWorkplaceEditorManager() {

        return m_editorManager;
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
            cms.checkRole(CmsRole.WORKPLACE_MANAGER);

            // set the workplace encoding
            m_encoding = OpenCms.getSystemInfo().getDefaultEncoding();

            // throw away all currently configured module explorer types
            m_explorerTypeSettingsFromModules.clear();
            // now add the additional explorer types found in the modules
            CmsModuleManager moduleManager = OpenCms.getModuleManager();
            Iterator j = moduleManager.getModuleNames().iterator();
            while (j.hasNext()) {
                CmsModule module = moduleManager.getModule((String)j.next());
                addExplorerTypeSettings(module);
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
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_VFS_ACCESS_INITIALIZED_0));
            }
            try {
                // read the temporary file project
                m_tempFileProject = cms.readProject(I_CmsProjectDriver.C_TEMP_FILE_PROJECT_NAME);
            } catch (CmsException e) {
                // during initial setup of OpenCms the temp file project does not yet exist...
                LOG.error(Messages.get().key(Messages.LOG_NO_TEMP_FILE_PROJECT_0));
            }
            // create an instance of editor display options
            m_editorDisplayOptions = new CmsEditorDisplayOptions();

            // throw away all current gallery settings
            m_galleries.clear();
            // read out the configured gallery classes
            j = OpenCms.getResourceManager().getResourceTypes().iterator();
            while (j.hasNext()) {
                I_CmsResourceType resourceType = (I_CmsResourceType)j.next();
                if (resourceType instanceof CmsResourceTypeFolderExtended) {
                    // found a configured extended folder resource type
                    CmsResourceTypeFolderExtended galleryType = (CmsResourceTypeFolderExtended)resourceType;
                    String folderClassName = galleryType.getFolderClassName();
                    if (CmsStringUtil.isNotEmpty(folderClassName)) {
                        // only process this as a gallery if the folder name is not empty
                        try {
                            // check, if the folder class is a subclass of A_CmsGallery
                            if (A_CmsGallery.class.isAssignableFrom(Class.forName(folderClassName))) {
                                // store the gallery class name with the type name as lookup key                        
                                m_galleries.put(galleryType.getTypeName(), galleryType);
                            }
                        } catch (ClassNotFoundException e) {
                            LOG.error(e.getLocalizedMessage());
                        }
                    }
                }
            }

            // create a new tool manager
            m_toolManager = new CmsToolManager(cms);

            // throw away all cached message objects
            m_messages.clear();
        } catch (CmsException e) {
            throw new CmsException(Messages.get().container(Messages.ERR_INITIALIZE_WORKPLACE_0));
        }
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
     * @return true if tabs should be enabled, otherwise false
     */
    public boolean isEnableAdvancedPropertyTabs() {

        return m_enableAdvancedPropertyTabs;
    }

    /** 
     * Removes the list of explorer type settings from the given module.<p>
     * 
     * @param module the module witch contains the explorer type settings to remove
     */
    public void removeExplorerTypeSettings(CmsModule module) {

        List explorerTypes = module.getExplorerTypes();
        if ((explorerTypes != null) && (explorerTypes.size() > 0)) {
            Iterator i = explorerTypes.iterator();
            while (i.hasNext()) {
                CmsExplorerTypeSettings settings = (CmsExplorerTypeSettings)i.next();
                if (m_explorerTypeSettingsFromModules.contains(settings)) {
                    m_explorerTypeSettingsFromModules.remove(settings);
                    if (CmsLog.INIT.isInfoEnabled()) {
                        CmsLog.INIT.info(Messages.get().key(
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
     * Sets if the autolock resources feature is enabled.<p>
     * 
     * @param value "true" if the autolock resources feature is enabled, otherwise false
     */
    public void setAutoLock(String value) {

        m_autoLockResources = Boolean.valueOf(value).booleanValue();
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(
                m_autoLockResources ? Messages.INIT_AUTO_LOCK_ENABLED_0 : Messages.INIT_AUTO_LOCK_DISABLED_0));
        }
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
                CmsLog.INIT.warn(Messages.get().key(Messages.INIT_NONCRIT_ERROR_0), e);
            }
        }
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_DEFAULT_LOCALE_1, m_defaultLocale));
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
            CmsLog.INIT.info(Messages.get().key(
                m_defaultPropertiesOnStructure ? Messages.INIT_PROP_ON_STRUCT_TRUE_0
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
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_DEFAULT_USER_SETTINGS_1, m_defaultUserSettings));
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
            CmsLog.INIT.info(Messages.get().key(
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
            CmsLog.INIT.info(Messages.get().key(
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
            CmsLog.INIT.info(Messages.get().key(
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
            CmsLog.INIT.info(Messages.get().key(
                m_enableAdvancedPropertyTabs ? Messages.INIT_ADV_PROP_DIALOG_SHOW_TABS_0
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
                CmsLog.INIT.info(Messages.get().key(
                    Messages.INIT_MAX_FILE_UPLOAD_SIZE_1,
                    new Integer(m_fileMaxUploadSize)));
            } else {
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_MAX_FILE_UPLOAD_SIZE_UNLIMITED_0));
            }

        }
    }

    /**
     * Sets the system-wide file view settings for the workplace.<p>
     * 
     * @param cms the CmsObject for ensuring security constraints. 
     * 
     * @param fileViewSettings the system-wide file view settings for the workplace to set 
     * @throws CmsRoleViolationException if the current user does not own the administrator role  ({@link CmsRole#ADMINISTRATOR})  
     * */
    public void setFileViewSettings(CmsObject cms, CmsRfsFileViewer fileViewSettings) throws CmsRoleViolationException {

        if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) {
            cms.checkRole(CmsRole.ADMINISTRATOR);
        }
        m_fileViewSettings = fileViewSettings;
        // disallow modifications of this "new original"
        m_fileViewSettings.setFrozen(true);
    }

    /**
     * Controls if the user/group icon in the administration view should be shown.<p>
     * 
     * @param value "true" if the user/group icon in the administration view should be shown, otherwise false
     */
    public void setUserManagementEnabled(String value) {

        m_showUserGroupIcon = Boolean.valueOf(value).booleanValue();
        if (CmsLog.INIT.isInfoEnabled()) {
            if (m_showUserGroupIcon) {
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_USER_MANAGEMENT_ICON_ENABLED_0));
            } else {
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_USER_MANAGEMENT_ICON_DISABLED_0));
            }
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
     * Inintializes the configured explorer type settings.<p>
     */
    private synchronized void initExplorerTypeSettings() {

        Map explorerTypeSettingsMap = new HashMap();
        List explorerTypeSettings = new ArrayList();

        explorerTypeSettings.addAll(m_explorerTypeSettingsFromXml);
        explorerTypeSettings.addAll(m_explorerTypeSettingsFromModules);

        for (int i = 0; i < explorerTypeSettings.size(); i++) {
            CmsExplorerTypeSettings settings = (CmsExplorerTypeSettings)explorerTypeSettings.get(i);
            // put the settings in the lookup map
            explorerTypeSettingsMap.put(settings.getName(), settings);
            try {
                // initialize the access control configuration of the explorer type
                settings.getAccess().createAccessControlList();
            } catch (CmsException e) {
                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(Messages.get().key(Messages.INIT_ADD_TYPE_SETTING_FAILED_1, settings.getName()), e);
                }
            }
        }
        // sort the explorer type settings
        Collections.sort(explorerTypeSettings);
        // make the settings unmodifiable and store them in the golbal variables
        m_explorerTypeSettings = Collections.unmodifiableList(explorerTypeSettings);
        m_explorerTypeSettingsMap = Collections.unmodifiableMap(explorerTypeSettingsMap);
    }

    /**
     * Initializes the workplace locale set.<p>
     * 
     * Currently, this is defined by the existence of a special folder 
     * <code>/system/workplace/locales/{locale-name}/</code>.
     * This is likely to change in future implementations.<p>
     * 
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     * @return the workplace locale set
     */
    private List initWorkplaceLocales(CmsObject cms) {

        Set locales = new HashSet();
        List localeFolders;
        try {
            localeFolders = cms.getSubFolders(I_CmsWpConstants.C_VFS_PATH_LOCALES);
        } catch (CmsException e) {
            LOG.error(Messages.get().key(Messages.LOG_WORKPLACE_INIT_NO_LOCALES_1, I_CmsWpConstants.C_VFS_PATH_LOCALES));
            // can not throw exception here since then OpenCms would not even start in shell mode (runlevel 2)
            localeFolders = new ArrayList();
        }
        Iterator i = localeFolders.iterator();
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
        ArrayList result = new ArrayList();
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
    private List initWorkplaceViews(CmsObject cms) {

        List viewFolders = new ArrayList();
        try {
            // get the subfolders of the "views" folder
            viewFolders = cms.getSubFolders(I_CmsWpConstants.C_VFS_PATH_VIEWS);
        } catch (CmsException e) {
            if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING && LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().key(Messages.LOG_WORKPLACE_INIT_NO_VIEWS_1, I_CmsWpConstants.C_VFS_PATH_VIEWS),
                    e);
            }
            // can not throw exception here since then OpenCms would not even start in shell mode (runlevel 2)
            viewFolders = new ArrayList();
        }
        m_views = new ArrayList(viewFolders.size());
        for (int i = 0; i < viewFolders.size(); i++) {
            // loop through all view folders
            CmsFolder folder = (CmsFolder)viewFolders.get(i);
            String folderPath = cms.getSitePath(folder);
            try {
                // get view information from folder properties
                String order = cms.readPropertyObject(folderPath, I_CmsConstants.C_PROPERTY_NAVPOS, false).getValue();
                String key = cms.readPropertyObject(folderPath, I_CmsConstants.C_PROPERTY_NAVTEXT, false).getValue();
                String viewUri = cms.readPropertyObject(folderPath, I_CmsConstants.C_PROPERTY_DEFAULT_FILE, false).getValue();
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
                    CmsLog.INIT.info(Messages.get().key(Messages.INIT_WORKPLACE_VIEW_1, view.getUri()));
                }
            } catch (CmsException e) {
                // should usually never happen
                LOG.error(Messages.get().key(Messages.LOG_READING_VIEW_FOLDER_FAILED_1, folderPath), e);
            }
        }
        // sort the views by their order number
        Collections.sort(m_views);
        return m_views;
    }
}
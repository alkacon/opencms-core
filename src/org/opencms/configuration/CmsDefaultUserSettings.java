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

package org.opencms.configuration;

import org.opencms.configuration.preferences.CmsBuiltinPreference;
import org.opencms.configuration.preferences.CmsEditorPreference;
import org.opencms.configuration.preferences.CmsPreferenceData;
import org.opencms.configuration.preferences.CmsStartGallleryPreference;
import org.opencms.configuration.preferences.CmsUserDefinedPreference;
import org.opencms.configuration.preferences.CmsUserSettingsStringPropertyWrapper;
import org.opencms.configuration.preferences.CmsWrapperPreference;
import org.opencms.configuration.preferences.I_CmsPreference;
import org.opencms.configuration.preferences.PrefMetadata;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResource.CmsResourceCopyMode;
import org.opencms.file.CmsResource.CmsResourceDeleteMode;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsLog;
import org.opencms.util.A_CmsModeStringEnumeration;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;

/**
 * Default user workplace settings, used as default values for worklace settings in the
 * user preferences.<p>
 *
 * @since 6.0.0
 */
public class CmsDefaultUserSettings extends CmsUserSettings {

    /**
     * Enumeration class for defining the publish related resources mode.<p>
     */
    public static final class CmsPublishRelatedResourcesMode extends A_CmsModeStringEnumeration {

        /** Constant for the publish related resources mode, checkbox disabled by default. */
        protected static final CmsPublishRelatedResourcesMode MODE_FALSE = new CmsPublishRelatedResourcesMode(
            CmsStringUtil.FALSE);

        /**
         * Constant for the publish related resources mode, only {@link org.opencms.security.CmsRole#VFS_MANAGER}s
         * may publish resources without publishing the related resources.
         */
        protected static final CmsPublishRelatedResourcesMode MODE_FORCE = new CmsPublishRelatedResourcesMode("FORCE");

        /** Constant for the publish related resources mode, checkbox enabled by default. */
        protected static final CmsPublishRelatedResourcesMode MODE_TRUE = new CmsPublishRelatedResourcesMode(
            CmsStringUtil.TRUE);

        /** The serial version id. */
        private static final long serialVersionUID = -2665888243460791770L;

        /**
         * Default constructor.<p>
         *
         * @param mode string representation
         */
        private CmsPublishRelatedResourcesMode(String mode) {

            super(mode);
        }

        /**
         * Returns the parsed mode object if the string representation matches, or <code>null</code> if not.<p>
         *
         * @param publishRelatedResourcesMode the string representation to parse
         *
         * @return the parsed mode object
         */
        public static CmsPublishRelatedResourcesMode valueOf(String publishRelatedResourcesMode) {

            if (publishRelatedResourcesMode == null) {
                return null;
            }
            if (publishRelatedResourcesMode.equalsIgnoreCase(MODE_FALSE.getMode())) {
                return MODE_FALSE;
            }
            if (publishRelatedResourcesMode.equalsIgnoreCase(MODE_TRUE.getMode())) {
                return MODE_TRUE;
            }
            if (publishRelatedResourcesMode.equalsIgnoreCase(MODE_FORCE.getMode())) {
                return MODE_FORCE;
            }
            return null;
        }
    }

    /**
     * Enum for the subsitemap creation mode.<p>
     */
    public enum SubsitemapCreationMode {
        /** In  this mode, existing folders are converted into subsitemaps. */
        convert,

        /** In this mode, new subsitemap folders are created, giving the user a choice of which folder type they want to use. */
        createfolder
    }

    /** The current default user settings. */
    public static CmsDefaultUserSettings CURRENT_DEFAULT_SETTINGS;

    /** Constant for the publish related resources mode, checkbox disabled by default. */
    public static final CmsPublishRelatedResourcesMode PUBLISH_RELATED_RESOURCES_MODE_FALSE = CmsPublishRelatedResourcesMode.MODE_FALSE;

    /**
     * Constant for the publish related resources mode, only {@link org.opencms.security.CmsRole#VFS_MANAGER}s
     * may publish resources without publishing the related resources.
     */
    public static final CmsPublishRelatedResourcesMode PUBLISH_RELATED_RESOURCES_MODE_FORCE = CmsPublishRelatedResourcesMode.MODE_FORCE;

    /** Constant for the publish related resources mode, checkbox enabled by default. */
    public static final CmsPublishRelatedResourcesMode PUBLISH_RELATED_RESOURCES_MODE_TRUE = CmsPublishRelatedResourcesMode.MODE_TRUE;

    /** Publish button appearance: show always. */
    public static final String PUBLISHBUTTON_SHOW_ALWAYS = "always";

    /** Publish button appearance: show auto (only if user has publish permissions). */
    public static final String PUBLISHBUTTON_SHOW_AUTO = "auto";

    /** Publish button appearance: show never. */
    public static final String PUBLISHBUTTON_SHOW_NEVER = "never";

    /** Array list for fast lookup of "button styles". */
    public static final List<String> BUTTON_STYLES_LIST = Collections.unmodifiableList(
        Arrays.asList(new String[] {"image", "textimage", "text"}));

    /** Parameter for buttonstyle text & image. */
    private static final int BUTTONSTYLE_TEXTIMAGE = 1;

    /** Value for preserving siblings in copy dialog settings. */
    private static final String COPYMODE_PRESERVE = "preservesiblings";

    /** Value for creating a resource in copy dialog settings. */
    private static final String COPYMODE_RESOURCE = "createresource";

    /** Value for creating a sibling in copy dialog settings. */
    private static final String COPYMODE_SIBLING = "createsibling";

    /** Value for deleting siblings in delete dialog settings. */
    private static final String DELETEMODE_DELETE = "deletesiblings";

    /** Value for preserving siblings in delete dialog settings. */
    private static final String DELETEMODE_PRESERVE = "preservesiblings";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDefaultUserSettings.class);

    /** Value for publishing only resources in publish dialog settings. */
    private static final String PUBLISHMODE_ONLYRESOURCE = "onlyresource";

    /** Value for publishing siblings in publish dialog settings. */
    private static final String PUBLISHMODE_SIBLINGS = "allsiblings";

    /** The enable relation deletion flag. */
    private boolean m_allowBrokenRelations = true;

    /** The configured preference data. */
    private List<CmsPreferenceData> m_preferenceData = new ArrayList<CmsPreferenceData>();

    /** Stores the preference objects, with the preference names as keys, in order. */
    private LinkedHashMap<String, I_CmsPreference> m_preferences = new LinkedHashMap<String, I_CmsPreference>();

    /** The publish related resources mode. */
    private CmsPublishRelatedResourcesMode m_publishRelatedResourcesMode;

    /** The subsitemap creation mode. */
    private SubsitemapCreationMode m_subsitemapCreationMode;

    /**
     * Adds a preference.<p>
     *
     * @param name the name of the preference
     * @param value the default value
     * @param widget the widget to use for the preference
     * @param widgetConfig the widget configuration
     * @param niceName the nice name of the preference
     * @param description the description of the preference
     * @param ruleRegex the regex used for validation
     * @param error the validation error message
     * @param tab the tab to display the preference on
     */
    public void addPreference(
        String name,
        String value,
        String widget,
        String widgetConfig,
        String niceName,
        String description,
        String ruleRegex,
        String error,
        String tab) {

        CmsXmlContentProperty prop = new CmsXmlContentProperty(
            name,
            "string",
            widget,
            widgetConfig,
            ruleRegex,
            null,
            null,
            niceName,
            description,
            error,
            null);
        CmsPreferenceData pref = new CmsPreferenceData(name, value, prop, tab);
        m_preferenceData.add(pref);
    }

    /**
     * Gets the default copy mode when copying a file of the user.<p>
     *
     * @return the default copy mode when copying a file of the user
     */
    public String getDialogCopyFileModeString() {

        if (getDialogCopyFileMode() == CmsResource.COPY_AS_NEW) {
            return COPYMODE_RESOURCE;
        } else {
            return COPYMODE_SIBLING;
        }
    }

    /**
     * Gets the default copy mode when copying a folder of the user.<p>
     *
     * @return the default copy mode when copying a folder of the user
     */
    public String getDialogCopyFolderModeString() {

        if (getDialogCopyFolderMode() == CmsResource.COPY_AS_NEW) {
            return COPYMODE_RESOURCE;
        } else if (getDialogCopyFolderMode() == CmsResource.COPY_AS_SIBLING) {
            return COPYMODE_SIBLING;
        } else {
            return COPYMODE_PRESERVE;
        }
    }

    /**
     * Returns the default setting for file deletion.<p>
     *
     * @return the default setting for file deletion
     */
    public String getDialogDeleteFileModeString() {

        if (getDialogDeleteFileMode() == CmsResource.DELETE_REMOVE_SIBLINGS) {
            return DELETEMODE_DELETE;
        } else {
            return DELETEMODE_PRESERVE;
        }
    }

    /**
     * Returns the default setting for expanding inherited permissions in the dialog.<p>
     *
     * @return true if inherited permissions should be expanded, otherwise false
     *
     * @see #getDialogExpandInheritedPermissions()
     */
    public String getDialogExpandInheritedPermissionsString() {

        return String.valueOf(getDialogExpandInheritedPermissions());
    }

    /**
     * Returns the default setting for expanding the users permissions in the dialog.<p>
     *
     * @return true if the users permissions should be expanded, otherwise false
     *
     * @see #getDialogExpandUserPermissions()
     */
    public String getDialogExpandUserPermissionsString() {

        return String.valueOf(getDialogExpandUserPermissions());
    }

    /**
     * Returns the default setting for inheriting permissions on folders.<p>
     *
     * @return true if permissions should be inherited on folders, otherwise false
     */
    public String getDialogPermissionsInheritOnFolderString() {

        return String.valueOf(getDialogPermissionsInheritOnFolder());
    }

    /**
     * Returns the default setting for direct publishing.<p>
     *
     * @return the default setting for direct publishing
     */
    public String getDialogPublishSiblingsString() {

        if (getDialogPublishSiblings()) {
            return PUBLISHMODE_SIBLINGS;
        } else {
            return PUBLISHMODE_ONLYRESOURCE;
        }
    }

    /**
     * Determines if the export settings part of the secure/export dialog should be shown.<p>
     *
     * @return true if the export dialog is shown, otherwise false
     */
    public String getDialogShowExportSettingsString() {

        return String.valueOf(getDialogShowExportSettings());
    }

    /**
     * Determines if the lock dialog should be shown.<p>
     *
     * @return true if the lock dialog is shown, otherwise false
     */
    public String getDialogShowLockString() {

        return String.valueOf(getDialogShowLock());
    }

    /**
     * Returns a string representation of the direct edit button style.<p>
     *
     * @return string representation of the direct edit button style
     */
    public String getDirectEditButtonStyleString() {

        return BUTTON_STYLES_LIST.get(getDirectEditButtonStyle());
    }

    /**
     * Returns a string representation of the editor button style.<p>
     *
     * @return string representation of the editor button style
     */
    public String getEditorButtonStyleString() {

        return BUTTON_STYLES_LIST.get(getEditorButtonStyle());
    }

    /**
     * Returns a string representation of the explorer button style.<p>
     *
     * @return string representation of the explorer button style
     */
    public String getExplorerButtonStyleString() {

        return BUTTON_STYLES_LIST.get(getExplorerButtonStyle());
    }

    /**
     * Returns a string representation of the list all projects flag.<p>
     *
     * @return string representation of the list all projects flag
     *
     * @see #getListAllProjects()
     */
    public String getListAllProjectsString() {

        return String.valueOf(getShowPublishNotification());
    }

    /**
     * Gets the map of preferences.<p>
     *
     * @return the map of preferences
     */
    public Map<String, I_CmsPreference> getPreferences() {

        return Collections.unmodifiableMap(m_preferences);
    }

    /**
     * Returns the publish related resources mode.<p>
     *
     * @return the publish related resources mode
     */
    public CmsPublishRelatedResourcesMode getPublishRelatedResources() {

        return m_publishRelatedResourcesMode;
    }

    /**
     * Returns if the explorer view is restricted to the defined site and folder.<p>
     *
     * @return true if the explorer view is restricted, otherwise false
     */
    public String getRestrictExplorerViewString() {

        return String.valueOf(getRestrictExplorerView());
    }

    /**
     * Gets if the file creation date should be shown in explorer view.<p>
     *
     * @return <code>"true"</code> if the file creation date should be shown, otherwise <code>"false"</code>
     */
    public String getShowExplorerFileDateCreated() {

        return getExplorerSetting(CmsUserSettings.FILELIST_DATE_CREATED);
    }

    /**
     * Gets if the file expired by should be shown in explorer view.<p>
     *
     * @return <code>"true"</code> if the file date expired by should be shown, otherwise <code>"false"</code>
     */
    public String getShowExplorerFileDateExpired() {

        return getExplorerSetting(CmsUserSettings.FILELIST_DATE_EXPIRED);
    }

    /**
     * Gets if the file last modified date should be shown in explorer view.<p>
     *
     * @return <code>"true"</code> if the file last modified date should be shown, otherwise <code>"false"</code>
     */
    public String getShowExplorerFileDateLastModified() {

        return getExplorerSetting(CmsUserSettings.FILELIST_DATE_LASTMODIFIED);
    }

    /**
     * Gets if the file released by should be shown in explorer view.<p>
     *
     * @return <code>"true"</code> if the file date released by should be shown, otherwise <code>"false"</code>
     */
    public String getShowExplorerFileDateReleased() {

        return getExplorerSetting(CmsUserSettings.FILELIST_DATE_RELEASED);
    }

    /**
     * Gets if the file locked by should be shown in explorer view.<p>
     *
     * @return <code>"true"</code> if the file locked by should be shown, otherwise <code>"false"</code>
     */
    public String getShowExplorerFileLockedBy() {

        return getExplorerSetting(CmsUserSettings.FILELIST_LOCKEDBY);
    }

    /**
     * Gets if the file navtext should be shown in explorer view.<p>
     *
     * @return <code>"true"</code> if the file navtext should be shown, otherwise <code>"false"</code>
     */
    public String getShowExplorerFileNavText() {

        return getExplorerSetting(CmsUserSettings.FILELIST_NAVTEXT);
    }

    /**
     * Gets if the file permissions should be shown in explorer view.<p>
     *
     * @return <code>"true"</code> if the file permissions should be shown, otherwise <code>"false"</code>
     */
    public String getShowExplorerFilePermissions() {

        return getExplorerSetting(CmsUserSettings.FILELIST_PERMISSIONS);
    }

    /**
     * Gets if the file size should be shown in explorer view.<p>
     *
     * @return <code>"true"</code> if the file size should be shown, otherwise <code>"false"</code>
     */
    public String getShowExplorerFileSize() {

        return getExplorerSetting(CmsUserSettings.FILELIST_SIZE);
    }

    /**
     * Gets  if the file state should be shown in explorer view.<p>
     *
     * @return <code>"true"</code> if the file state should be shown, otherwise <code>"false"</code>
     */
    public String getShowExplorerFileState() {

        return getExplorerSetting(CmsUserSettings.FILELIST_STATE);
    }

    /**
     * Gets if the file title should be shown in explorer view.<p>
     *
     * @return  <code>"true"</code> if the file title should be shown, otherwise <code>"false"</code>
     */
    public String getShowExplorerFileTitle() {

        return getExplorerSetting(CmsUserSettings.FILELIST_TITLE);
    }

    /**
     * Gets if the file type should be shown in explorer view.<p>
     *
     * @return  <code>"true"</code> if the file type should be shown, otherwise <code>"false"</code>
     */
    public String getShowExplorerFileType() {

        return getExplorerSetting(CmsUserSettings.FILELIST_TYPE);
    }

    /**
     * Gets if the file creator should be shown in explorer view.<p>
     *
     * @return <code>"true"</code> if the file creator should be shown, otherwise <code>"false"</code>
     */
    public String getShowExplorerFileUserCreated() {

        return getExplorerSetting(CmsUserSettings.FILELIST_USER_CREATED);
    }

    /**
     * Gets if the file last modified by should be shown in explorer view.<p>
     *
     * @return <code>"true"</code> if the file last modified by should be shown, otherwise <code>"false"</code>
     */
    public String getShowExplorerFileUserLastModified() {

        return getExplorerSetting(CmsUserSettings.FILELIST_USER_LASTMODIFIED);
    }

    /**
     * Returns a string representation of the show file upload button flag.<p>
     *
     * @return string representation of the show file upload button flag
     *
     * @see #getShowFileUploadButton()
     */
    public String getShowFileUploadButtonString() {

        return String.valueOf(getShowFileUploadButton());
    }

    /**
     * Returns a string representation of the publish notification flag.<p>
     *
     * @return string representation of the publish notification flag
     *
     * @see #getShowPublishNotification()
     */
    public String getShowPublishNotificationString() {

        return String.valueOf(getShowPublishNotification());
    }

    /**
     * Gets the subsitemap creation mode to use for the sitemap editor.<p>
     *
     * @return the subsitemap creation mode to use for the sitemap editor
     */
    public SubsitemapCreationMode getSubsitemapCreationMode() {

        return getSubsitemapCreationMode(SubsitemapCreationMode.convert);
    }

    /**
     * Gets the subsitemap creation mode, or returns a default value given as a parameter if the mode is not set.<p>
     *
     * @param defaultValue the value to return when the subsitemap creation mode is not set
     *
     * @return the subsitemap creation mode
     */
    public SubsitemapCreationMode getSubsitemapCreationMode(SubsitemapCreationMode defaultValue) {

        if (m_subsitemapCreationMode != null) {
            return m_subsitemapCreationMode;
        }
        return defaultValue;
    }

    /**
     * Returns a string representation of the workplace button style.<p>
     *
     * @return string representation of the workplace button style
     *
     * @see #getWorkplaceButtonStyle()
     */
    public String getWorkplaceButtonStyleString() {

        return BUTTON_STYLES_LIST.get(getWorkplaceButtonStyle());
    }

    /**
     * Initializes the preference configuration.<p>
     *
     * Note that this method should only be called once the resource types have been initialized, but after addPreference has been called for all configured preferences.
     *
     * @param wpManager the active workplace manager
     */
    public void initPreferences(CmsWorkplaceManager wpManager) {

        CURRENT_DEFAULT_SETTINGS = this;
        Class<?> accessorClass = CmsUserSettingsStringPropertyWrapper.class;

        // first initialize all built-in preferences. these are:
        // a) Bean properties of CmsUserSettingsStringPropertyWrapper
        // b) Editor setting preferences
        // c) Gallery setting preferences
        PropertyDescriptor[] propDescs = PropertyUtils.getPropertyDescriptors(accessorClass);
        for (PropertyDescriptor descriptor : propDescs) {
            String name = descriptor.getName();
            Method getter = descriptor.getReadMethod();
            Method setter = descriptor.getWriteMethod();
            if ((getter == null) || (setter == null)) {
                continue;
            }

            PrefMetadata metadata = getter.getAnnotation(PrefMetadata.class);
            if (metadata == null) {
                CmsBuiltinPreference preference = new CmsBuiltinPreference(name);
                m_preferences.put(preference.getName(), preference);
            } else {
                try {
                    Constructor<?> constructor = metadata.type().getConstructor(String.class);
                    I_CmsPreference pref = (I_CmsPreference)constructor.newInstance(name);
                    m_preferences.put(pref.getName(), pref);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        Map<String, String> editorValues = getEditorSettings();
        if (wpManager.getWorkplaceEditorManager() != null) {
            for (String resType : wpManager.getWorkplaceEditorManager().getConfigurableEditors().keySet()) {
                if (!editorValues.containsKey(resType)) {
                    editorValues.put(resType, null);
                }
            }
        }
        for (Map.Entry<String, String> editorSettingEntry : editorValues.entrySet()) {
            CmsEditorPreference pref = new CmsEditorPreference(
                editorSettingEntry.getKey(),
                editorSettingEntry.getValue());
            m_preferences.put(pref.getName(), pref);
        }

        Map<String, String> galleryValues = new HashMap<String, String>(getStartGalleriesSettings());
        for (String key : wpManager.getGalleries().keySet()) {
            if (!galleryValues.containsKey(key)) {
                galleryValues.put(key, null);
            }
        }
        for (Map.Entry<String, String> galleryEntry : galleryValues.entrySet()) {
            CmsStartGallleryPreference pref = new CmsStartGallleryPreference(
                galleryEntry.getKey(),
                galleryEntry.getValue());
            m_preferences.put(pref.getName(), pref);
        }

        // Now process configured preferences. Each configuration entry is either
        // for a built-in preference, in which case we create a wrapper around the existing preference,
        // or for a custom user-defined preference.
        for (CmsPreferenceData prefData : m_preferenceData) {
            String name = prefData.getName();
            I_CmsPreference pref = null;
            if (m_preferences.containsKey(name)) {
                // we first remove the existing preference, because in a LinkedHashMap, put(key, value) will not
                // update the position of the entry if the key already exists
                pref = new CmsWrapperPreference(prefData, m_preferences.remove(name));
            } else {
                pref = new CmsUserDefinedPreference(
                    prefData.getName(),
                    prefData.getDefaultValue(),
                    prefData.getPropertyDefinition(),
                    prefData.getTab());
            }
            m_preferences.put(pref.getName(), pref);
            pref.setValue(this, prefData.getDefaultValue());
        }
    }

    /**
     * Returns if the deletion of relation targets is enabled.<p>
     *
     * @return <code>true</code> if the deletion of relation targets is enabled, otherwise <code>false</code>
     */
    public boolean isAllowBrokenRelations() {

        return m_allowBrokenRelations;
    }

    /**
     * Sets if the deletion of relation targets is enabled.<p>
     *
     * @param allowBrokenRelations <code>true</code> if relation deletion should be enabled, otherwise <code>false</code>
     */
    public void setAllowBrokenRelations(String allowBrokenRelations) {

        m_allowBrokenRelations = Boolean.valueOf(allowBrokenRelations).booleanValue();
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    m_allowBrokenRelations
                    ? Messages.INIT_RELATION_DELETION_ENABLED_0
                    : Messages.INIT_RELATION_DELETION_DISABLED_0));
        }
    }

    /**
     * Sets the default copy mode when copying a file of the user.<p>
     *
     * @param mode the default copy mode when copying a file of the user
     */
    public void setDialogCopyFileMode(String mode) {

        CmsResourceCopyMode copyMode = CmsResource.COPY_AS_NEW;
        if (mode.equalsIgnoreCase(COPYMODE_SIBLING)) {
            copyMode = CmsResource.COPY_AS_SIBLING;
        }
        setDialogCopyFileMode(copyMode);
    }

    /**
     * Sets the default copy mode when copying a folder of the user.<p>
     *
     * @param mode the default copy mode when copying a folder of the user
     */
    public void setDialogCopyFolderMode(String mode) {

        CmsResourceCopyMode copyMode = CmsResource.COPY_AS_NEW;
        if (mode.equalsIgnoreCase(COPYMODE_SIBLING)) {
            copyMode = CmsResource.COPY_AS_SIBLING;
        } else if (mode.equalsIgnoreCase(COPYMODE_PRESERVE)) {
            copyMode = CmsResource.COPY_PRESERVE_SIBLING;
        }
        setDialogCopyFolderMode(copyMode);
    }

    /**
     * Sets the default setting for file deletion.<p>
     *
     * @param mode the default setting for file deletion
     */
    public void setDialogDeleteFileMode(String mode) {

        CmsResourceDeleteMode deleteMode = CmsResource.DELETE_PRESERVE_SIBLINGS;
        if (mode.equalsIgnoreCase(DELETEMODE_DELETE)) {
            deleteMode = CmsResource.DELETE_REMOVE_SIBLINGS;
        }
        setDialogDeleteFileMode(deleteMode);
    }

    /**
     * Sets the default setting for expanding inherited permissions in the dialog.<p>
     *
     * @param dialogExpandInheritedPermissions the default setting for expanding inherited permissions in the dialog
     */
    public void setDialogExpandInheritedPermissions(String dialogExpandInheritedPermissions) {

        setDialogExpandInheritedPermissions(Boolean.valueOf(dialogExpandInheritedPermissions).booleanValue());
    }

    /**
     * Sets the default setting for expanding the users permissions in the dialog.<p>
     *
     * @param dialogExpandUserPermissions the default setting for expanding the users permissions in the dialog
     */
    public void setDialogExpandUserPermissions(String dialogExpandUserPermissions) {

        setDialogExpandUserPermissions(Boolean.valueOf(dialogExpandUserPermissions).booleanValue());
    }

    /**
     * Sets the default setting for inheriting permissions on folders.<p>
     *
     * @param dialogPermissionsInheritOnFolder the default setting for inheriting permissions on folders
     */
    public void setDialogPermissionsInheritOnFolder(String dialogPermissionsInheritOnFolder) {

        setDialogPermissionsInheritOnFolder(Boolean.valueOf(dialogPermissionsInheritOnFolder).booleanValue());
    }

    /**
     * Sets the default setting for direct publishing.<p>
     *
     * @param mode the default setting for direct publishing
     */
    public void setDialogPublishSiblings(String mode) {

        boolean publishSiblings = false;
        if (mode.equalsIgnoreCase(PUBLISHMODE_SIBLINGS)) {
            publishSiblings = true;
        }
        setDialogPublishSiblings(publishSiblings);
    }

    /**
     * Sets the style of the direct edit buttons of the user.<p>
     *
     * @param buttonstyle the style of the direct edit buttons of the user
     */
    public void setDirectEditButtonStyle(String buttonstyle) {

        int buttonstyleValue = BUTTONSTYLE_TEXTIMAGE;
        try {
            if (buttonstyle != null) {
                buttonstyleValue = BUTTON_STYLES_LIST.indexOf(buttonstyle);
            }
        } catch (Exception e) {
            // do nothing, use the default value
        }
        setDirectEditButtonStyle(buttonstyleValue);
    }

    /**
     * Sets the style of the editor buttons of the user.<p>
     *
     * @param buttonstyle the style of the editor buttons of the user
     */
    public void setEditorButtonStyle(String buttonstyle) {

        int buttonstyleValue = BUTTONSTYLE_TEXTIMAGE;
        try {
            if (buttonstyle != null) {
                buttonstyleValue = BUTTON_STYLES_LIST.indexOf(buttonstyle);
            }
        } catch (Exception e) {
            // do nothing, use the default value
        }
        setEditorButtonStyle(buttonstyleValue);
    }

    /**
     * Sets the style of the explorer workplace buttons of the user.<p>
     *
     * @param buttonstyle the style of the explorer workplace buttons of the user
     */
    public void setExplorerButtonStyle(String buttonstyle) {

        int buttonstyleValue = BUTTONSTYLE_TEXTIMAGE;
        try {
            if (buttonstyle != null) {
                buttonstyleValue = BUTTON_STYLES_LIST.indexOf(buttonstyle);
            }
        } catch (Exception e) {
            // do nothing, use the default value
        }
        setExplorerButtonStyle(buttonstyleValue);
    }

    /**
     * Sets the number of displayed files per page of the user.<p>
     *
     * @param entries the number of displayed files per page of the user
     */
    public void setExplorerFileEntries(String entries) {

        try {
            setExplorerFileEntries(Integer.parseInt(entries));
        } catch (Throwable t) {
            // ignore this exception
        }
    }

    /**
     * Sets if all projects should be shown for the user.<p>
     *
     * @param listAllProjects <code>"true"</code> or <code>"false"</code>
     */
    public void setListAllProjects(String listAllProjects) {

        setListAllProjects(Boolean.valueOf(listAllProjects).booleanValue());
    }

    /**
     * Sets the workplace locale.<p>
     *
     * @param locale the workplace language default
     */
    public void setLocale(String locale) {

        // set the language
        setLocale(CmsLocaleManager.getLocale(locale));
    }

    /**
     * Digester support method for configuration if the "create index page" checkbox in the new folder
     * dialog should be initially be checked or not. <p>
     *
     * The given <code>String</code> value is interpreted as a {@link Boolean} by the means
     * of <code>{@link Boolean#valueOf(String)}</code>. <p>
     *
     * @param booleanValue a <code>String</code> that is interpred as a {@link Boolean} by the means
     *      of <code>{@link Boolean#valueOf(String)}</code>
     */
    public void setNewFolderCreateIndexPage(String booleanValue) {

        setNewFolderCreateIndexPage(Boolean.valueOf(booleanValue));
    }

    /**
     * Digester support method for configuration if the "edit properties" checkbox in the new folder
     * dialog should be initially be checked or not. <p>
     *
     * The given <code>String</code> value is interpreted as a {@link Boolean} by the means
     * of <code>{@link Boolean#valueOf(String)}</code>. <p>
     *
     * @param booleanValue a <code>String</code> that is interpreted as a <code> {@link Boolean}</code>
     *      by the means of <code>{@link Boolean#valueOf(String)}</code>
     */
    public void setNewFolderEditProperties(String booleanValue) {

        setNewFolderEditPropertes(Boolean.valueOf(booleanValue));
    }

    /**
     * Sets the publish related resources mode.<p>
     *
     * @param publishRelatedResourcesMode the publish related resources mode to set
     */
    public void setPublishRelatedResourcesMode(String publishRelatedResourcesMode) {

        m_publishRelatedResourcesMode = CmsPublishRelatedResourcesMode.valueOf(publishRelatedResourcesMode);
        if ((m_publishRelatedResourcesMode != null) && CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.INIT_PUBLISH_RELATED_RESOURCES_MODE_1,
                    m_publishRelatedResourcesMode.toString()));
        }
    }

    /**
     * Sets if the explorer view is restricted to the defined site and folder.<p>
     *
     * @param restrict true if the explorer view is restricted, otherwise false
     */
    public void setRestrictExplorerView(String restrict) {

        setRestrictExplorerView(Boolean.valueOf(restrict).booleanValue());
    }

    /**
     * Sets if the file creation date should be shown in explorer view.<p>
     *
     * @param show true if the file creation date should be shown, otherwise false
     */
    public void setShowExplorerFileDateCreated(String show) {

        setShowExplorerFileDateCreated(Boolean.valueOf(show).booleanValue());
    }

    /**
     * Sets if the file expire date should be shown in explorer view.<p>
     *
     * @param show true if the file expire date should be shown, otherwise false
     */
    public void setShowExplorerFileDateExpired(String show) {

        setShowExplorerFileDateExpired(Boolean.valueOf(show).booleanValue());
    }

    /**
     * Sets if the file last modified date should be shown in explorer view.<p>
     *
     * @param show true if the file last modified date should be shown, otherwise false
     */
    public void setShowExplorerFileDateLastModified(String show) {

        setShowExplorerFileDateLastModified(Boolean.valueOf(show).booleanValue());
    }

    /**
     * Sets if the file release date should be shown in explorer view.<p>
     *
     * @param show true if the file release date should be shown, otherwise false
     */
    public void setShowExplorerFileDateReleased(String show) {

        setShowExplorerFileDateReleased(Boolean.valueOf(show).booleanValue());
    }

    /**
     * Sets if the file locked by should be shown in explorer view.<p>
     *
     * @param show true if the file locked by should be shown, otherwise false
     */
    public void setShowExplorerFileLockedBy(String show) {

        setShowExplorerFileLockedBy(Boolean.valueOf(show).booleanValue());
    }

    /**
     * Sets if the file navtext should be shown in explorer view.<p>
     *
     * @param show true if the file locked by should be shown, otherwise false
     */
    public void setShowExplorerFileNavText(String show) {

        setShowExplorerFileNavText(Boolean.valueOf(show).booleanValue());
    }

    /**
     * Sets if the file permissions should be shown in explorer view.<p>
     *
     * @param show true if the file permissions should be shown, otherwise false
     */
    public void setShowExplorerFilePermissions(String show) {

        setShowExplorerFilePermissions(Boolean.valueOf(show).booleanValue());
    }

    /**
     * Sets if the file size should be shown in explorer view.<p>
     *
     * @param show true if the file size should be shown, otherwise false
     */
    public void setShowExplorerFileSize(String show) {

        setShowExplorerFileSize(Boolean.valueOf(show).booleanValue());
    }

    /**
     * Sets if the file state should be shown in explorer view.<p>
     *
     * @param show true if the state size should be shown, otherwise false
     */
    public void setShowExplorerFileState(String show) {

        setShowExplorerFileState(Boolean.valueOf(show).booleanValue());
    }

    /**
     * Sets if the file title should be shown in explorer view.<p>
     *
     * @param show true if the file title should be shown, otherwise false
     */
    public void setShowExplorerFileTitle(String show) {

        setShowExplorerFileTitle(Boolean.valueOf(show).booleanValue());
    }

    /**
     * Sets if the file type should be shown in explorer view.<p>
     *
     * @param show true if the file type should be shown, otherwise false
     */
    public void setShowExplorerFileType(String show) {

        setShowExplorerFileType(Boolean.valueOf(show).booleanValue());
    }

    /**
     * Sets if the file creator should be shown in explorer view.<p>
     *
     * @param show true if the file creator should be shown, otherwise false
     */
    public void setShowExplorerFileUserCreated(String show) {

        setShowExplorerFileUserCreated(Boolean.valueOf(show).booleanValue());
    }

    /**
     * Sets if the file last modified by should be shown in explorer view.<p>
     *
     * @param show true if the file last modified by should be shown, otherwise false
     */
    public void setShowExplorerFileUserLastModified(String show) {

        setShowExplorerFileUserLastModified(Boolean.valueOf(show).booleanValue());
    }

    /**
     *  Sets if the export part of the secure/export dialog should be shown.<p>
     *
     * @param mode true if the export dialog should be shown, otherwise false
     */
    public void setShowExportSettingsDialog(String mode) {

        setDialogShowExportSettings(Boolean.valueOf(mode).booleanValue());
    }

    /**
     * Controls whether to display a file upload icon or not.<p>
     *
     * @param flag <code>"true"</code> or <code>"false"</code> to flag the use of the file upload button
     */
    public void setShowFileUploadButton(String flag) {

        setShowFileUploadButton(Boolean.valueOf(flag).booleanValue());
    }

    /**
     *  Sets if the lock dialog should be shown.<p>
     *
     * @param mode true if the lock dialog should be shown, otherwise false
     */
    public void setShowLockDialog(String mode) {

        setDialogShowLock(Boolean.valueOf(mode).booleanValue());
    }

    /**
     * Sets if the publish notification should be shown for the user.<p>
     *
     * @param notification <code>"true"</code> or <code>"false"</code> to flag the notification
     */
    public void setShowPublishNotification(String notification) {

        // set if the publish notification should be shown
        setShowPublishNotification(Boolean.valueOf(notification).booleanValue());
    }

    /**
     * Digester support method for configuration if the resource type selection checkbox should
     * show up when uploading a new file in non-applet mode.<p>
     *
     * The given <code>String</code> value is interpreted as a {@link Boolean} by the means
     * of <code>{@link Boolean#valueOf(String)}</code>. <p>
     *
     * @param booleanValue a <code>String</code> that is interpreted as a {@link Boolean} by the means
     *      of <code>{@link Boolean#valueOf(String)}</code>
     */
    public void setShowUploadTypeDialog(String booleanValue) {

        setShowUploadTypeDialog(Boolean.valueOf(booleanValue));
    }

    /**
     * Sets the subsitemap creation mode.<p>
     *
     * @param mode the string value of the subsitemap creation mode
     */
    public void setSubsitemapCreationMode(String mode) {

        try {
            m_subsitemapCreationMode = SubsitemapCreationMode.valueOf(mode);
        } catch (Exception e) {
            LOG.warn("Invalid value for subsitemap creation mode was ignored: " + mode);
        }
    }

    /**
     * Sets the style of the workplace buttons of the user.<p>
     *
     * @param buttonstyle the style of the workplace buttons of the user
     */
    public void setWorkplaceButtonStyle(String buttonstyle) {

        int buttonstyleValue = BUTTONSTYLE_TEXTIMAGE;
        try {
            if (buttonstyle != null) {
                buttonstyleValue = BUTTON_STYLES_LIST.indexOf(buttonstyle);
            }
        } catch (Exception e) {
            // do nothing, use the default value
        }
        setWorkplaceButtonStyle(buttonstyleValue);
    }

    /**
     * Sets the style of the workplace search default view.<p>
     *
     * @param viewStyle the style of the workplace search default view
     */
    public void setWorkplaceSearchViewStyle(String viewStyle) {

        setWorkplaceSearchViewStyle(CmsSearchResultStyle.valueOf(viewStyle));
    }

    /**
     * Checks if  a specific explorer setting depending is set.<p>
     *
     * @param setting the settings constant value for the explorer settings
     * @return <code>"true"</code> if the explorer setting is set, otherwise <code>"false"</code>
     */
    private String getExplorerSetting(int setting) {

        return String.valueOf((getExplorerSettings() & setting) > 0);
    }

}
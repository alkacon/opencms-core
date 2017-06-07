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

package org.opencms.configuration.preferences;

import org.opencms.ade.galleries.CmsGalleryService;
import org.opencms.configuration.CmsDefaultUserSettings;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsContextInfo;

/**
 * Bean used to access the built-in preferences via reflection.<p>
 *
 * All getter/setter pairs in this class are assumed to correspond to user settings, and the corresponding
 * property names can be used as keys in the new preference configuration format, so the method names should not
 * be changed.
 */
public class CmsUserSettingsStringPropertyWrapper {

    /** The m_settings. */
    private CmsDefaultUserSettings m_settings;

    /**
     * Instantiates a new cms user settings string property wrapper.
     *
     * @param settings the settings
     */
    public CmsUserSettingsStringPropertyWrapper(CmsDefaultUserSettings settings) {

        m_settings = settings;
    }

    /**
     * Gets the allow broken relations.
     *
     * @return the allow broken relations
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getAllowBrokenRelations() {

        return "" + m_settings.isAllowBrokenRelations();
    }

    /**
     * Gets the dialog copy file mode.
     *
     * @return the dialog copy file mode
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getDialogCopyFileMode() {

        return m_settings.getDialogCopyFileModeString();
    }

    /**
     * Gets the dialog copy folder mode.
     *
     * @return the dialog copy folder mode
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getDialogCopyFolderMode() {

        return m_settings.getDialogCopyFolderModeString();
    }

    /**
     * Gets the dialog delete file mode.
     *
     * @return the dialog delete file mode
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getDialogDeleteFileMode() {

        return m_settings.getDialogDeleteFileModeString();
    }

    /**
     * Gets the dialog expand inherited permissions.
     *
     * @return the dialog expand inherited permissions
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getDialogExpandInheritedPermissions() {

        return m_settings.getDialogExpandInheritedPermissionsString();
    }

    /**
     * Gets the dialog expand user permissions.
     *
     * @return the dialog expand user permissions
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getDialogExpandUserPermissions() {

        return m_settings.getDialogExpandUserPermissionsString();
    }

    /**
     * Gets the dialog permissions inherit on folder.
     *
     * @return the dialog permissions inherit on folder
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getDialogPermissionsInheritOnFolder() {

        return m_settings.getDialogPermissionsInheritOnFolderString();
    }

    /**
     * Gets the dialog publish siblings.
     *
     * @return the dialog publish siblings
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getDialogPublishSiblings() {

        return m_settings.getDialogPublishSiblingsString();
    }

    /**
     * Gets the dialog show export settings.
     *
     * @return the dialog show export settings
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getDialogShowExportSettings() {

        return m_settings.getDialogShowExportSettingsString();
    }

    /**
     * Gets the dialog show lock.
     *
     * @return the dialog show lock
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getDialogShowLock() {

        return m_settings.getDialogShowLockString();
    }

    /**
     * Gets the direct edit button style.
     *
     * @return the direct edit button style
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getDirectEditButtonStyle() {

        return m_settings.getDirectEditButtonStyleString();
    }

    /**
     * Gets the editor button style.
     *
     * @return the editor button style
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getEditorButtonStyle() {

        return m_settings.getEditorButtonStyleString();
    }

    /**
     * Gets the element view.
     *
     * @return the element view
     */
    @PrefMetadata(type = CmsElementViewPreference.class)
    public String getElementView() {

        return m_settings.getAdditionalPreference(CmsElementViewPreference.PREFERENCE_NAME, false);
    }

    /**
     * Gets the explorer button style.
     *
     * @return the explorer button style
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getExplorerButtonStyle() {

        return m_settings.getExplorerButtonStyleString();
    }

    /**
     * Gets the default element view for the explorer.<p>
     *
     * @return the default element view for the explorer
     */
    @PrefMetadata(type = CmsExplorerElementViewPreference.class)
    public String getExplorerElementView() {

        return m_settings.getAdditionalPreference(CmsElementViewPreference.EXPLORER_PREFERENCE_NAME, false);
    }

    /**
     * Gets the explorer file entries.
     *
     * @return the explorer file entries
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getExplorerFileEntries() {

        return "" + m_settings.getExplorerFileEntries();
    }

    /**
     * Gets the explorer file entry options.
     *
     * @return the explorer file entry options
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getExplorerFileEntryOptions() {

        if (m_settings.getExplorerFileEntryOptions() == null) {
            return "";
        } else {
            return "" + m_settings.getExplorerFileEntryOptions();
        }
    }

    /**
     * Gets the value of the 'show invalid elements in galleries'.<p>
     *
     * @return the 'show invalid elements in galleries' setting
     */
    @PrefMetadata(type = CmsGalleryShowInvalidDefaultPreference.class)
    public String getGalleryShowInvalidDefault() {

        return m_settings.getAdditionalPreference(CmsGalleryService.PREF_GALLERY_SHOW_INVALID_DEFAULT, false);
    }

    /**
     * Gets the list all projects.
     *
     * @return the list all projects
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getListAllProjects() {

        return m_settings.getListAllProjectsString();
    }

    /**
     * Gets the locale.
     *
     * @return the locale
     */
    @PrefMetadata(type = CmsLanguagePreference.class)
    public String getLocale() {

        return m_settings.getLocale().toString();
    }

    /**
     * Gets the new folder create index page.
     *
     * @return the new folder create index page
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getNewFolderCreateIndexPage() {

        return m_settings.getNewFolderCreateIndexPage().toString();
    }

    /**
     * Gets the new folder edit properties.
     *
     * @return the new folder edit properties
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getNewFolderEditProperties() {

        return m_settings.getNewFolderEditProperties().toString();
    }

    /**
     * Gets the publish button appearance.
     *
     * @return the publish button appearance
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getPublishButtonAppearance() {

        return m_settings.getPublishButtonAppearance();
    }

    /**
     * Gets the publish related resources mode.
     *
     * @return the publish related resources mode
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getPublishRelatedResourcesMode() {

        return "" + m_settings.getPublishRelatedResources();
    }

    /**
     * Gets the restrict explorer view.
     *
     * @return the restrict explorer view
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getRestrictExplorerView() {

        return m_settings.getRestrictExplorerViewString();
    }

    /**
     * Gets the show explorer file date created.
     *
     * @return the show explorer file date created
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getShowExplorerFileDateCreated() {

        return m_settings.getShowExplorerFileDateCreated();
    }

    /**
     * Gets the show explorer file date expired.
     *
     * @return the show explorer file date expired
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getShowExplorerFileDateExpired() {

        return m_settings.getShowExplorerFileDateExpired();
    }

    /**
     * Gets the show explorer file date last modified.
     *
     * @return the show explorer file date last modified
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getShowExplorerFileDateLastModified() {

        return m_settings.getShowExplorerFileDateLastModified();
    }

    /**
     * Gets the show explorer file date released.
     *
     * @return the show explorer file date released
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getShowExplorerFileDateReleased() {

        return m_settings.getShowExplorerFileDateReleased();
    }

    /**
     * Gets the show explorer file locked by.
     *
     * @return the show explorer file locked by
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getShowExplorerFileLockedBy() {

        return m_settings.getShowExplorerFileLockedBy();
    }

    /**
     * Gets the show explorer file nav text.
     *
     * @return the show explorer file nav text
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getShowExplorerFileNavText() {

        return m_settings.getShowExplorerFileNavText();
    }

    /**
     * Gets the show explorer file permissions.
     *
     * @return the show explorer file permissions
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getShowExplorerFilePermissions() {

        return m_settings.getShowExplorerFilePermissions();
    }

    /**
     * Gets the show explorer file size.
     *
     * @return the show explorer file size
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getShowExplorerFileSize() {

        return m_settings.getShowExplorerFileSize();
    }

    /**
     * Gets the show explorer file state.
     *
     * @return the show explorer file state
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getShowExplorerFileState() {

        return m_settings.getShowExplorerFileState();
    }

    /**
     * Gets the show explorer file title.
     *
     * @return the show explorer file title
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getShowExplorerFileTitle() {

        return m_settings.getShowExplorerFileTitle();
    }

    /**
     * Gets the show explorer file type.
     *
     * @return the show explorer file type
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getShowExplorerFileType() {

        return m_settings.getShowExplorerFileType();
    }

    /**
     * Gets the show explorer file user created.
     *
     * @return the show explorer file user created
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getShowExplorerFileUserCreated() {

        return m_settings.getShowExplorerFileUserCreated();
    }

    /**
     * Gets the show explorer file user last modified.
     *
     * @return the show explorer file user last modified
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getShowExplorerFileUserLastModified() {

        return m_settings.getShowExplorerFileUserLastModified();
    }

    /**
     * Gets the show file upload button.
     *
     * @return the show file upload button
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getShowFileUploadButton() {

        return m_settings.getShowFileUploadButtonString();
    }

    /**
     * Gets the show publish notification.
     *
     * @return the show publish notification
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getShowPublishNotification() {

        return m_settings.getShowPublishNotificationString();
    }

    /**
     * Gets the show upload type dialog.
     *
     * @return the show upload type dialog
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getShowUploadTypeDialog() {

        return "" + m_settings.getShowUploadTypeDialog();
    }

    /**
     * Gets the start folder.
     *
     * @return the start folder
     */
    @PrefMetadata(type = CmsStartFolderPreference.class)
    public String getStartFolder() {

        return m_settings.getStartFolder();
    }

    /**
     * Gets the start project.
     *
     * @return the start project
     */
    @PrefMetadata(type = CmsProjectPreference.class)
    public String getStartProject() {

        return m_settings.getStartProject();
    }

    /**
     * Gets the start site.
     *
     * @return the start site
     */
    @PrefMetadata(type = CmsSitePreference.class)
    public String getStartSite() {

        return m_settings.getStartSite();
    }

    /**
     * Gets the start view.
     *
     * @return the start view
     */
    @PrefMetadata(type = CmsStartViewPreference.class)
    public String getStartView() {

        return m_settings.getStartView();
    }

    /**
     * Gets the subsitemap creation mode.
     *
     * @return the subsitemap creation mode
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getSubsitemapCreationMode() {

        return "" + m_settings.getSubsitemapCreationMode();
    }

    /**
     * Gets the time warp.
     *
     * @return the time warp
     */
    @PrefMetadata(type = CmsTimeWarpPreference.class)
    public String getTimeWarp() {

        long warp = m_settings.getTimeWarp();
        return warp < 0 ? "" : "" + warp; // if timewarp < 0 (i.e. time warp is not set), use the empty string because we don't want the date selector widget to interpret the negative value
    }

    /**
     * Gets the upload applet client folder.
     *
     * @return the upload applet client folder
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getUploadAppletClientFolder() {

        return m_settings.getUploadAppletClientFolder();
    }

    /**
     * Gets the upload variant.
     *
     * @return the upload variant
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getUploadVariant() {

        return m_settings.getUploadVariant().toString();
    }

    /**
     * Gets the workplace button style.
     *
     * @return the workplace button style
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getWorkplaceButtonStyle() {

        return m_settings.getWorkplaceButtonStyleString();
    }

    /**
     * Gets the workplace mode.<p>
     *
     * @return the workplace mode
     */
    @PrefMetadata(type = CmsWorkplaceModePreference.class)
    public String getWorkplaceMode() {

        return m_settings.getAdditionalPreference(CmsWorkplaceModePreference.PREFERENCE_NAME, false);
    }

    /**
     * Gets the workplace report type.
     *
     * @return the workplace report type
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getWorkplaceReportType() {

        return m_settings.getWorkplaceReportType();

    }

    /**
     * Gets the workplace search index name.
     *
     * @return the workplace search index name
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getWorkplaceSearchIndexName() {

        return m_settings.getWorkplaceSearchIndexName();
    }

    /**
     * Gets the workplace search view style.
     *
     * @return the workplace search view style
     */
    @PrefMetadata(type = CmsHiddenBuiltinPreference.class)
    public String getWorkplaceSearchViewStyle() {

        return m_settings.getWorkplaceSearchViewStyle().toString();
    }

    /**
     * Sets the allow broken relations.
     *
     * @param s the new allow broken relations
     */
    public void setAllowBrokenRelations(String s) {

        m_settings.setAllowBrokenRelations(s);
    }

    /**
     * Sets the dialog copy file mode.
     *
     * @param s the new dialog copy file mode
     */
    public void setDialogCopyFileMode(String s) {

        m_settings.setDialogCopyFileMode(s);

    }

    /**
     * Sets the dialog copy folder mode.
     *
     * @param s the new dialog copy folder mode
     */
    public void setDialogCopyFolderMode(String s) {

        m_settings.setDialogCopyFolderMode(s);
    }

    /**
     * Sets the dialog delete file mode.
     *
     * @param s the new dialog delete file mode
     */
    public void setDialogDeleteFileMode(String s) {

        m_settings.setDialogDeleteFileMode(s);
    }

    /**
     * Sets the dialog expand inherited permissions.
     *
     * @param s the new dialog expand inherited permissions
     */
    public void setDialogExpandInheritedPermissions(String s) {

        m_settings.setDialogExpandInheritedPermissions(s);
    }

    /**
     * Sets the dialog expand user permissions.
     *
     * @param s the new dialog expand user permissions
     */
    public void setDialogExpandUserPermissions(String s) {

        m_settings.setDialogExpandUserPermissions(s);
    }

    /**
     * Sets the dialog permissions inherit on folder.
     *
     * @param s the new dialog permissions inherit on folder
     */
    public void setDialogPermissionsInheritOnFolder(String s) {

        m_settings.setDialogPermissionsInheritOnFolder(s);
    }

    /**
     * Sets the dialog publish siblings.
     *
     * @param s the new dialog publish siblings
     */
    public void setDialogPublishSiblings(String s) {

        m_settings.setDialogPublishSiblings(s);
    }

    /**
     * Sets the dialog show export settings.
     *
     * @param s the new dialog show export settings
     */
    public void setDialogShowExportSettings(String s) {

        m_settings.setShowExportSettingsDialog(s);
    }

    /**
     * Sets the dialog show lock.
     *
     * @param s the new dialog show lock
     */
    public void setDialogShowLock(String s) {

        m_settings.setShowLockDialog(s);
    }

    /**
     * Sets the direct edit button style.
     *
     * @param s the new direct edit button style
     */
    public void setDirectEditButtonStyle(String s) {

        m_settings.setDirectEditButtonStyle(s);

    }

    /**
     * Sets the editor button style.
     *
     * @param s the new editor button style
     */
    public void setEditorButtonStyle(String s) {

        m_settings.setEditorButtonStyle(s);
    }

    /**
     * Sets the element view.<p>
     *
     * @param elementView the element view
     */
    public void setElementView(String elementView) {

        m_settings.setAdditionalPreference(CmsElementViewPreference.PREFERENCE_NAME, elementView);
    }

    /**
     * Sets the explorer button style.
     *
     * @param s the new explorer button style
     */
    public void setExplorerButtonStyle(String s) {

        m_settings.setExplorerButtonStyle(s);
    }

    /**
     * Sets the explorer start element view.<p>
     *
     * @param view the start element view.
     */
    public void setExplorerElementView(String view) {

        m_settings.setAdditionalPreference(CmsElementViewPreference.EXPLORER_PREFERENCE_NAME, view);
    }

    /**
     * Sets the explorer file entries.
     *
     * @param s the new explorer file entries
     */
    public void setExplorerFileEntries(String s) {

        m_settings.setExplorerFileEntries(s);
    }

    /**
     * Sets the explorer file entry options.
     *
     * @param s the new explorer file entry options
     */
    public void setExplorerFileEntryOptions(String s) {

        m_settings.setExplorerFileEntryOptions(s);

    }

    /**
     * Sets the default value for the checkbox which enables/disables showing invalid results in the gallery result tab.<p>
     *
     * @param value the value to set
     */
    public void setGalleryShowInvalidDefault(String value) {

        m_settings.setAdditionalPreference(CmsGalleryService.PREF_GALLERY_SHOW_INVALID_DEFAULT, value);
    }

    /**
     * Sets the list all projects.
     *
     * @param s the new list all projects
     */
    public void setListAllProjects(String s) {

        m_settings.setListAllProjects(s);
    }

    /**
     * Sets the locale.
     *
     * @param s the new locale
     */
    public void setLocale(String s) {

        m_settings.setLocale(CmsLocaleManager.getLocale(s));
    }

    /**
     * Sets the new folder create index page.
     *
     * @param s the new new folder create index page
     */
    public void setNewFolderCreateIndexPage(String s) {

        m_settings.setNewFolderCreateIndexPage(s);
    }

    /**
     * Sets the new folder edit properties.
     *
     * @param s the new new folder edit properties
     */
    public void setNewFolderEditProperties(String s) {

        m_settings.setNewFolderEditProperties(s);
    }

    /**
     * Sets the publish button appearance.
     *
     * @param s the new publish button appearance
     */
    public void setPublishButtonAppearance(String s) {

        m_settings.setPublishButtonAppearance(s);
    }

    /**
     * Sets the publish related resources mode.
     *
     * @param mode the new publish related resources mode
     */
    public void setPublishRelatedResourcesMode(String mode) {

        m_settings.setPublishRelatedResourcesMode(mode);
    }

    /**
     * Sets the restrict explorer view.
     *
     * @param s the new restrict explorer view
     */
    public void setRestrictExplorerView(String s) {

        m_settings.setRestrictExplorerView(s);
    }

    /**
     * Sets the show explorer file date created.
     *
     * @param s the new show explorer file date created
     */
    public void setShowExplorerFileDateCreated(String s) {

        m_settings.setShowExplorerFileDateCreated(s);

    }

    /**
     * Sets the show explorer file date expired.
     *
     * @param s the new show explorer file date expired
     */
    public void setShowExplorerFileDateExpired(String s) {

        m_settings.setShowExplorerFileDateExpired(s);
    }

    /**
     * Sets the show explorer file date last modified.
     *
     * @param s the new show explorer file date last modified
     */
    public void setShowExplorerFileDateLastModified(String s) {

        m_settings.setShowExplorerFileDateLastModified(s);
    }

    /**
     * Sets the show explorer file date released.
     *
     * @param s the new show explorer file date released
     */
    public void setShowExplorerFileDateReleased(String s) {

        m_settings.setShowExplorerFileDateReleased(s);
    }

    /**
     * Sets the show explorer file locked by.
     *
     * @param s the new show explorer file locked by
     */
    public void setShowExplorerFileLockedBy(String s) {

        m_settings.setShowExplorerFileLockedBy(s);
    }

    /**
     * Sets the show explorer file nav text.
     *
     * @param s the new show explorer file nav text
     */
    public void setShowExplorerFileNavText(String s) {

        m_settings.setShowExplorerFileNavText(s);
    }

    /**
     * Sets the show explorer file permissions.
     *
     * @param s the new show explorer file permissions
     */
    public void setShowExplorerFilePermissions(String s) {

        m_settings.setShowExplorerFilePermissions(s);
    }

    /**
     * Sets the show explorer file size.
     *
     * @param s the new show explorer file size
     */
    public void setShowExplorerFileSize(String s) {

        m_settings.setShowExplorerFileSize(s);
    }

    /**
     * Sets the show explorer file state.
     *
     * @param s the new show explorer file state
     */
    public void setShowExplorerFileState(String s) {

        m_settings.setShowExplorerFileState(s);
    }

    /**
     * Sets the show explorer file title.
     *
     * @param s the new show explorer file title
     */
    public void setShowExplorerFileTitle(String s) {

        m_settings.setShowExplorerFileTitle(s);
    }

    /**
     * Sets the show explorer file type.
     *
     * @param s the new show explorer file type
     */
    public void setShowExplorerFileType(String s) {

        m_settings.setShowExplorerFileType(s);

    }

    /**
     * Sets the show explorer file user created.
     *
     * @param s the new show explorer file user created
     */
    public void setShowExplorerFileUserCreated(String s) {

        m_settings.setShowExplorerFileUserCreated(s);
    }

    /**
     * Sets the show explorer file user last modified.
     *
     * @param s the new show explorer file user last modified
     */
    public void setShowExplorerFileUserLastModified(String s) {

        m_settings.setShowExplorerFileUserLastModified(s);
    }

    /**
     * Sets the show file upload button.
     *
     * @param s the new show file upload button
     */
    public void setShowFileUploadButton(String s) {

        m_settings.setShowFileUploadButton(s);
    }

    /**
     * Sets the show publish notification.
     *
     * @param s the new show publish notification
     */
    public void setShowPublishNotification(String s) {

        m_settings.setShowPublishNotification(s);
    }

    /**
     * Sets the show upload type dialog.
     *
     * @param s the new show upload type dialog
     */
    public void setShowUploadTypeDialog(String s) {

        m_settings.setShowUploadTypeDialog(s);
    }

    /**
     * Sets the start folder.
     *
     * @param s the new start folder
     */
    public void setStartFolder(String s) {

        m_settings.setStartFolder(s);
    }

    /**
     * Sets the start project.
     *
     * @param s the new start project
     */
    public void setStartProject(String s) {

        m_settings.setStartProject(s);
    }

    /**
     * Sets the start site.
     *
     * @param s the new start site
     */
    public void setStartSite(String s) {

        m_settings.setStartSite(s);
    }

    /**
     * Sets the start view.
     *
     * @param s the new start view
     */
    public void setStartView(String s) {

        m_settings.setStartView(s);

    }

    /**
     * Sets the subsitemap creation mode.
     *
     * @param mode the new subsitemap creation mode
     */
    public void setSubsitemapCreationMode(String mode) {

        m_settings.setSubsitemapCreationMode(mode);
    }

    /**
     * Sets the time warp.
     *
     * @param l the new time warp
     */
    public void setTimeWarp(String l) {

        long warp = CmsContextInfo.CURRENT_TIME;
        try {
            warp = Long.parseLong(l);
        } catch (NumberFormatException e) {
            // if parsing the time warp fails, it will be set to -1 (i.e. disabled)
        }
        m_settings.setTimeWarp(warp);
    }

    /**
     * Sets the upload applet client folder.
     *
     * @param s the new upload applet client folder
     */
    public void setUploadAppletClientFolder(String s) {

        m_settings.setUploadAppletClientFolder(s);

    }

    /**
     * Sets the upload variant.
     *
     * @param s the new upload variant
     */
    public void setUploadVariant(String s) {

        m_settings.setUploadVariant(s);
    }

    /**
     * Sets the workplace button style.
     *
     * @param s the new workplace button style
     */
    public void setWorkplaceButtonStyle(String s) {

        m_settings.setWorkplaceButtonStyle(s);
    }

    /**
     * Sets the workplace mode.<p>
     *
     * @param workplaceMode the workplace mode
     */
    public void setWorkplaceMode(String workplaceMode) {

        m_settings.setAdditionalPreference(CmsWorkplaceModePreference.PREFERENCE_NAME, workplaceMode);
    }

    /**
     * Sets the workplace report type.
     *
     * @param s the new workplace report type
     */
    public void setWorkplaceReportType(String s) {

        m_settings.setWorkplaceReportType(s);
    }

    /**
     * Sets the workplace search index name.
     *
     * @param s the new workplace search index name
     */
    public void setWorkplaceSearchIndexName(String s) {

        m_settings.setWorkplaceSearchIndexName(s);
    }

    /**
     * Sets the workplace search view style.
     *
     * @param s the new workplace search view style
     */
    public void setWorkplaceSearchViewStyle(String s) {

        m_settings.setWorkplaceSearchViewStyle(s);

    }
}

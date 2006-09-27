/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/CmsDefaultUserSettings.java,v $
 * Date   : $Date: 2006/09/27 09:53:52 $
 * Version: $Revision: 1.17.4.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.configuration;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsLog;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Default user workplace settings, used as default values for worklace settings in the
 * user preferences.<p>
 *  
 * @author Michael Emmerich 
 * @author Andreas Zahner 
 * 
 * @version $Revision: 1.17.4.4 $
 * 
 * @since 6.0.0 
 */
public class CmsDefaultUserSettings extends CmsUserSettings {

    /** 
     * Array of the possible "button styles".
     * Must be private because of Findbugs rule "MS".
     */
    private static final String[] BUTTON_STYLES = {"image", "textimage", "text"};

    /** Array list for fast lookup of "button styles". */
    public static final List BUTTON_STYLES_LIST = Collections.unmodifiableList(Arrays.asList(BUTTON_STYLES));

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

    /** Publish button appearance: show always. */
    public static final String PUBLISHBUTTON_SHOW_ALWAYS = "always";

    /** Publish button appearance: show auto (only if user has publish permissions). */
    public static final String PUBLISHBUTTON_SHOW_AUTO = "auto";

    /** Publish button appearance: show never. */
    public static final String PUBLISHBUTTON_SHOW_NEVER = "never";

    /** Value for publishing only resources in publish dialog settings. */
    private static final String PUBLISHMODE_ONLYRESOURCE = "onlyresource";

    /** Value for publishing siblings in publish dialog settings. */
    private static final String PUBLISHMODE_SIBLINGS = "allsiblings";

    /** The enable relation deletion flag. */
    private boolean m_allowBrokenRelations = true;

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

        return BUTTON_STYLES[getDirectEditButtonStyle()];
    }

    /**
     * Returns a string representation of the editor button style.<p>
     * 
     * @return string representation of the editor button style
     */
    public String getEditorButtonStyleString() {

        return BUTTON_STYLES[getEditorButtonStyle()];
    }

    /**
     * Returns a string representation of the explorer button style.<p>
     * 
     * @return string representation of the explorer button style
     */
    public String getExplorerButtonStyleString() {

        return BUTTON_STYLES[getExplorerButtonStyle()];
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
     * Returns a string representation of the upload Applet flag.<p>
     * 
     * @return string representation of the uploadApplet flag
     * 
     * @see #useUploadApplet()
     */
    public String getUploadAppletString() {

        return String.valueOf(useUploadApplet());
    }

    /**
     * Returns a string representation of the workplace button style.<p>
     * 
     * @return string representation of the workplace button style
     * 
     * @see #getWorkplaceButtonStyle()
     */
    public String getWorkplaceButtonStyleString() {

        return BUTTON_STYLES[getWorkplaceButtonStyle()];
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
            CmsLog.INIT.info(Messages.get().getBundle().key(
                m_allowBrokenRelations ? Messages.INIT_RELATION_DELETION_ENABLED_0
                : Messages.INIT_RELATION_DELETION_DISABLED_0));
        }
    }

    /**
     * Sets the default copy mode when copying a file of the user.<p>
     * 
     * @param mode the default copy mode when copying a file of the user
     */
    public void setDialogCopyFileMode(String mode) {

        int copyMode = CmsResource.COPY_AS_NEW;
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

        int copyMode = CmsResource.COPY_AS_NEW;
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

        int deleteMode = CmsResource.DELETE_PRESERVE_SIBLINGS;
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
     * @param booleanValue a <code>String</code> that is interpred as a {@link Boolean} by the means 
     *      of <code>{@link Boolean#valueOf(String)}</code> 
     */
    public void setNewFolderEditProperties(String booleanValue) {
        setNewFolderEditPropertes(Boolean.valueOf(booleanValue));
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
     * @param show true if the file relese date should be shown, otherwise false
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
     * Sets the usage of the upload applet for the user user.<p>
     * 
     * @param applet <code>"true"</code> or <code>"false"</code> to flag the use of the applet
     */
    public void setUploadApplet(String applet) {

        // set the usage of the upload applet
        setUseUploadApplet(Boolean.valueOf(applet).booleanValue());
    }

    /**
     * Sets the style of the  workplace buttons of the user.<p>
     * 
     * @param buttonstyle the style of the  workplace buttons of the user
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
}
/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsUserSettings.java,v $
 * Date   : $Date: 2004/02/26 11:35:34 $
 * Version: $Revision: 1.10 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.db;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.workplace.CmsReport;
import org.opencms.workplace.I_CmsWpConstants;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;

/**
 * Object to conveniently access and modify the users workplace settings.<p>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.10 $
 * 
 * @since 5.1.12
 */
public class CmsUserSettings {
    
    /** Identifier for the users additional editor settings information */
    public static final String C_ADDITIONAL_INFO_EDITORSETTINGS = "USER_EDITORSETTINGS";
    private static final int C_BUTTONSTYLE_DEFAULT = 1;
    
    /** Identifier for the workplace file siblings copy setting key */
    public static final String C_DIALOG_COPY_FILE_MODE = "DIALOG_COPY_FILE_MODE";
    
    /** Identifier for the workplace folder siblings copy setting key */
    public static final String C_DIALOG_COPY_FOLDER_MODE = "DIALOG_COPY_FOLDER_MODE";
    
    /** Identifier for the workplace sibling deletion setting key */
    public static final String C_DIALOG_DELETE_MODE = "DIALOG_DELETE_MODE";
    
    /** Identifier for the workplace publish siblings setting key */
    public static final String C_DIALOG_PUBLISH_SIBLINGS = "DIALOG_PUBLISH_SIBLINGS";
    
    /** Identifier for the direct editing button style setting key */
    public static final String C_DIRECT_EDIT_BUTTONSTYLE = "DIRECT_EDIT_BUTTONSTYLE";
    
    /** Identifier for the editor button style setting key */
    public static final String C_EDITOR_BUTTONSTYLE = "EDITOR_BUTTONSTYLE";
    
    /** Identifier for the preferred editor setting key (prefix) */
    public static final String C_EDITOR_PREFERRED_PREFIX = "EDITOR_PREFERRED_";
    
    private static final int C_ENTRYS_PER_PAGE_DEFAULT = 50;
    
    /** Identifier for the explorer button style setting key */
    public static final String C_EXPLORER_BUTTONSTYLE = "EXPLORER_BUTTONSTYLE";
    
    /** Identifier for the explorer number of file entries per page setting key */
    public static final String C_EXPLORER_FILEENTRIES = "USER_EXPLORER_FILEENTRIES";
    private static final String C_REPORTTYPE_DEFAULT = CmsReport.REPORT_TYPE_SIMPLE;
    
    /** Identifier for the workplace button style setting key */
    public static final String C_WORKPLACE_BUTTONSTYLE = "WORKPLACE_BUTTONSTYLE";
    
    /** Identifier for the workplace report type setting key */
    public static final String C_WORKPLACE_REPORTTYPE = "WORKPLACE_REPORTTYPE";
    
    private HashMap m_editorSettings;
    
    private int m_explorerButtonStyle;
    private int m_explorerFileEntries;
    private int m_explorerSettings;
    
    /** Locale to avoid multiple locale constructs out of Strings */
    private Locale m_locale;
    private int m_taskMessages;
    private Hashtable m_taskSettings;
    private String m_town;
    private CmsUser m_user;
    private Hashtable m_workplaceSettings;
    private String m_zipCode;

    /**
     * Creates an empty new user settings object.<p>
     */
    public CmsUserSettings() {
        m_explorerButtonStyle = C_BUTTONSTYLE_DEFAULT;
        m_explorerFileEntries = C_ENTRYS_PER_PAGE_DEFAULT;
        m_explorerSettings = I_CmsWpConstants.C_FILELIST_NAME;
        m_editorSettings = new HashMap();
        m_taskSettings = new Hashtable();
        m_taskMessages = 0;
        m_workplaceSettings = new Hashtable();
        m_user = null;
        m_town = "";
        m_zipCode = "";
        m_taskSettings.put(I_CmsConstants.C_TASK_FILTER, new String("a1"));
    }
    
    /**
     * Creates a user settings object with initialized settings of the user.<p>
     * 
     * @param user the OpenCms user
     */
    public CmsUserSettings(CmsUser user) {
        init(user);
    }
    
    /**
     * Returns the town of the user.<p>
     * 
     * @return the town of the user
     */
    public String getAddressTown() {
        return m_town;
    }
    
    /**
     * Returns the zipcode of the user.<p>
     * 
     * @return the zipcode of the user
     */
    public String getAddressZip() {
        return m_zipCode;
    }
    
    /**
     * Gets the default copy mode when copying a file of the user.<p>
     * 
     * @return the default copy mode when copying a file of the user
     */
    public int getDialogCopyFileMode() {
        int mode = I_CmsConstants.C_COPY_AS_SIBLING;
        try {
            mode = Integer.parseInt((String)m_workplaceSettings.get(C_DIALOG_COPY_FILE_MODE));
        } catch (Throwable t) {
            // ignore this exception
        }
        return mode;
    }
    
    /**
     * Gets the default copy mode when copying a folder of the user.<p>
     * 
     * @return the default copy mode when copying a folder of the user
     */
    public int getDialogCopyFolderMode() {
        int mode = I_CmsConstants.C_COPY_AS_SIBLING;
        try {
            mode = Integer.parseInt((String)m_workplaceSettings.get(C_DIALOG_COPY_FOLDER_MODE));
        } catch (Throwable t) {
            // ignore this exception
        }
        return mode;
    }
    
    /**
     * Returns the default setting for file deletion.<p>
     * 
     * @return the default setting for file deletion
     */
    public int getDialogDeleteFileMode() {
        int mode = I_CmsConstants.C_DELETE_OPTION_PRESERVE_SIBLINGS;
        try {
            mode = Integer.parseInt((String)m_workplaceSettings.get(C_DIALOG_DELETE_MODE));
        } catch (Throwable t) {
            // ignore this exception
        }
        return mode;
    }
    
    /**
     * Returns the default setting for direct publishing.<p>
     * 
     * @return the default setting for direct publishing: true if siblings should be published, otherwise false
     */
    public boolean getDialogPublishSiblings() {
        Boolean publishSiblings = (Boolean)m_workplaceSettings.get(C_DIALOG_PUBLISH_SIBLINGS);
        if (publishSiblings == null) {
            return false;
        }
        return publishSiblings.booleanValue();
    }
    
    /**
     * Returns the style of the direct edit buttons of the user.<p>
     * 
     * @return the style of the direct edit buttons of the user
     */
    public int getDirectEditButtonStyle() {
        int style = C_BUTTONSTYLE_DEFAULT;
        try {
            style = Integer.parseInt((String)m_editorSettings.get(C_DIRECT_EDIT_BUTTONSTYLE));
        } catch (Throwable t) {
            // ignore this exception
        }
        return style;
    }
    
    /**
     * Returns the style of the editor buttons of the user.<p>
     * 
     * @return the style of the editor buttons of the user
     */
    public int getEditorButtonStyle() {
        int style = 0;
        try {
            style = Integer.parseInt((String)m_editorSettings.get(C_EDITOR_BUTTONSTYLE));
        } catch (Throwable t) {
            // ignore this exception
        }
        return style;
    }
   
   /**
    * Returns the style of the explorer buttons of the user.<p>
    * 
    * @return the style of the explorer buttons of the user
    */
   public int getExplorerButtonStyle() {
       return m_explorerButtonStyle;
   }
   
   /**
    * Returns the number of displayed files per page of the user.<p>
    * 
    * @return the number of displayed files per page of the user
    */
   public int getExplorerFileEntries() {
       return m_explorerFileEntries;
   }
    
    /** 
     * Returns the locale of the user.<p>
     * 
     * @return the loclae of the user
     */
    public Locale getLocale() {
        if (m_locale == null) {
            m_locale = CmsLocaleManager.getLocale((String)m_workplaceSettings.get(I_CmsConstants.C_START_LOCALE)); 
        }
        return m_locale; 
    }
    
    /**
     * Returns the preferred editor for the given resource type of the user.<p>
     * 
     * @param resourceType the resource type
     * @return the preferred editor for the resource type or null, if not specified
     */
    public String getPreferredEditor(String resourceType) {
        return (String)m_editorSettings.get(C_EDITOR_PREFERRED_PREFIX + resourceType.toUpperCase());
    }
    
    /** 
     * Returns the start project of the user.<p>
     * 
     * @return the start project of the user
     */
    public String getStartProject() {
        Integer projectId = (Integer)m_workplaceSettings.get(I_CmsConstants.C_START_PROJECT);       
        return projectId.toString();
    }
    
    /**
     * Returns the current start view of the user.<p>
     * 
     * @return the current start view of the user
     */
    public String getStartView() {
        return (String)m_workplaceSettings.get(I_CmsConstants.C_START_VIEW);
    }
    
    /**
     * Returns the startup filter for the tasks view.<p>
     * 
     * @return the startup filter for the tasks view
     */
    public String getTaskStartupFilter() {
        return (String)m_taskSettings.get(I_CmsConstants.C_TASK_FILTER);
    }
    
    /**
     * Returns the current user for the settings.<p>
     * 
     * @return the CmsUser
     */
    public CmsUser getUser() {
        return m_user;
    }
    
    /**
     * Returns the style of the workplace buttons of the user.<p>
     * 
     * @return the style of the workplace buttons of the user
     */
    public int getWorkplaceButtonStyle() {
        int style = C_BUTTONSTYLE_DEFAULT;
        try {
            style = Integer.parseInt((String)m_workplaceSettings.get(C_WORKPLACE_BUTTONSTYLE));
        } catch (Throwable t) {
            // ignore this exception
        }
        return style;
    }
    
    /**
     * Returns the type of the report (simple or extended) of the user.<p>
     * 
     * @return the type of the report (simple or extended) of the user
     */
    public String getWorkplaceReportType() {
        String type = C_REPORTTYPE_DEFAULT;
        try {
            type = (String)m_workplaceSettings.get(C_WORKPLACE_REPORTTYPE);
        } catch (Throwable t) {
            // ignore this exception
        }
        return type;
    }
    
    /**
     * Initializes the user settings with the given users setting parameters.<p>
     * 
     * @param user the current CmsUser
     */
    public void init(CmsUser user) {
        m_user = user; 
        try {
            m_explorerFileEntries = Integer.parseInt((String)m_user.getAdditionalInfo(C_EXPLORER_FILEENTRIES));
        } catch (Throwable t) {
            m_explorerFileEntries = C_ENTRYS_PER_PAGE_DEFAULT;
        }
        try {
            m_explorerButtonStyle = Integer.parseInt((String)m_user.getAdditionalInfo(C_EXPLORER_BUTTONSTYLE));
        } catch (Throwable t) {
            m_explorerButtonStyle = C_BUTTONSTYLE_DEFAULT;
        }
        try {
            String explorerSettingsString = (String)m_user.getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_EXPLORERSETTINGS);
            m_explorerSettings = Integer.parseInt(explorerSettingsString);
        } catch (NumberFormatException e) {
            // explorer start settings are not present, set them to default
            m_explorerSettings = I_CmsWpConstants.C_FILELIST_NAME;
            setShowExplorerFileTitle(true);
            setShowExplorerFileType(true);
            setShowExplorerFileDateLastModified(true);
        }
        m_editorSettings = (HashMap)m_user.getAdditionalInfo(C_ADDITIONAL_INFO_EDITORSETTINGS);
        if (m_editorSettings == null) {
            m_editorSettings = new HashMap();
        }
        m_workplaceSettings = (Hashtable)m_user.getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_STARTSETTINGS);
        m_taskSettings = (Hashtable)m_user.getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_TASKSETTINGS);
        m_zipCode = (String)m_user.getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_ZIPCODE);
        m_town = (String)m_user.getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_TOWN);
        try {
            m_taskMessages = ((Integer)m_taskSettings.get(I_CmsConstants.C_TASK_MESSAGES)).intValue();
        } catch (Exception e) {
            m_taskMessages = 0;
        }
    }
    
    /**
     * Saves the changed settings of the user to the user object.<p>
     * 
     * @param cms the CmsObject needed to write the user to the db
     * @throws CmsException if user cannot be written to the db
     */
    public void save(CmsObject cms) throws CmsException {
        m_user.setAdditionalInfo(C_EXPLORER_BUTTONSTYLE, "" + m_explorerButtonStyle);
        m_user.setAdditionalInfo(C_EXPLORER_FILEENTRIES, "" + m_explorerFileEntries);
        m_user.setAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_EXPLORERSETTINGS, "" + m_explorerSettings);
        m_user.setAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_STARTSETTINGS, m_workplaceSettings);
        m_user.setAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_TOWN, m_town);
        m_user.setAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_ZIPCODE, m_zipCode);
        m_taskSettings.put(I_CmsConstants.C_TASK_MESSAGES, new Integer(m_taskMessages));
        m_user.setAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_TASKSETTINGS, m_taskSettings);
        if (m_editorSettings.size() > 0) {
            m_user.setAdditionalInfo(C_ADDITIONAL_INFO_EDITORSETTINGS, m_editorSettings);
        }

        cms.writeUser(m_user);
    }
    
    /**
     * Sets the town of the user.<p>
     * 
     * @param value the town of the user 
     */
    public void setAddressTown(String value) {
        m_town = value;
    }

    /**
     * Sets the zipcode of the user.<p>
     * 
     * @param value the zipcode of the user 
     */
    public void setAddressZip(String value) {
        m_zipCode = value;
    }
    
    /**
     * Sets the default copy mode when copying a file of the user.<p>
     * 
     * @param mode the default copy mode when copying a file of the user
     */
    public void setDialogCopyFileMode(int mode) {
        m_workplaceSettings.put(C_DIALOG_COPY_FILE_MODE, "" + mode);
    }
    
    /**
     * Sets the default copy mode when copying a folder of the user.<p>
     * 
     * @param mode the default copy mode when copying a folder of the user
     */
    public void setDialogCopyFolderMode(int mode) {
        m_workplaceSettings.put(C_DIALOG_COPY_FOLDER_MODE, "" + mode);
    }
    
    /**
     * Sets the default setting for file deletion.<p>
     * 
     * @param mode the default setting for file deletion
     */
    public void setDialogDeleteFileMode(int mode) {
        m_workplaceSettings.put(C_DIALOG_DELETE_MODE, "" + mode);
    }
    
    /**
     * Sets the default setting for direct publishing.<p>
     * 
     * @param publishSiblings the default setting for direct publishing: true if siblings should be published, otherwise false
     */
    public void setDialogPublishSiblings(boolean publishSiblings) {
        m_workplaceSettings.put(C_DIALOG_PUBLISH_SIBLINGS, new Boolean(publishSiblings));
    }
    
    /**
     * Sets the style of the direct edit buttons of the user.<p>
     * 
     * @param style the style of the direct edit buttons of the user
     */
    public void setDirectEditButtonStyle(int style) {
        m_editorSettings.put(C_DIRECT_EDIT_BUTTONSTYLE, "" + style);
    }
    
    /**
     * Sets the style of the editor buttons of the user.<p>
     * 
     * @param style the style of the editor buttons of the user
     */
    public void setEditorButtonStyle(int style) {
        m_editorSettings.put(C_EDITOR_BUTTONSTYLE, "" + style);
    }
   
   /**
    * Sets the style of the explorer buttons of the user.<p>
    * 
    * @param style the style of the explorer buttons of the user
    */
   public void setExplorerButtonStyle(int style) {
       m_explorerButtonStyle = style;
   }
   
   /**
    * Sets the number of displayed files per page of the user.<p>
    * 
    * @param entries the number of displayed files per page of the user
    */
   public void setExplorerFileEntries(int entries) {
       m_explorerFileEntries = entries;
   }
    
    /**
     * Sets a specific explorer setting depending on the set parameter.<p>
     * 
     * @param set true if the setting should be set, otherwise false
     * @param setting the settings constant value for the explorer settings
     */
    private void setExplorerSetting(boolean set, int setting) {
        if (set) {
            m_explorerSettings |= setting;
        } else {
            m_explorerSettings &= ~setting;
        }
    }
    
    /**
     * Sets the locale of the user.<p>
     * 
     * @param locale the locale of the user
     */
    public void setLocale(Locale locale) {
        m_locale = locale;
        m_workplaceSettings.put(I_CmsConstants.C_START_LOCALE, locale.toString());
    }
    
    /**
     * Sets the preferred editor for the given resource type of the user.<p>
     * 
     * @param resourceType the resource type
     * @param editorUri the editor URI
     */
    public void setPreferredEditor(String resourceType, String editorUri) {
        if (editorUri == null) {
            m_editorSettings.remove(C_EDITOR_PREFERRED_PREFIX + resourceType.toUpperCase());
        }
        m_editorSettings.put(C_EDITOR_PREFERRED_PREFIX + resourceType.toUpperCase(), editorUri);
    }

    /**
     * Sets if the file creation date should be shown in explorer view.<p>
     * 
     * @param show true if the file creation date should be shown, otherwise false
     */
    public void setShowExplorerFileDateCreated(boolean show) {
        setExplorerSetting(show, I_CmsWpConstants.C_FILELIST_DATE_CREATED);
    }

    /**
     * Sets if the file last modified date should be shown in explorer view.<p>
     * 
     * @param show true if the file last modified date should be shown, otherwise false
     */
    public void setShowExplorerFileDateLastModified(boolean show) {
        setExplorerSetting(show, I_CmsWpConstants.C_FILELIST_DATE_LASTMODIFIED);
    }

    /**
     * Sets if the file locked by should be shown in explorer view.<p>
     * 
     * @param show true if the file locked by should be shown, otherwise false
     */
    public void setShowExplorerFileLockedBy(boolean show) {
        setExplorerSetting(show, I_CmsWpConstants.C_FILELIST_LOCKEDBY);
    }

    /**
     * Sets if the file permissions should be shown in explorer view.<p>
     * 
     * @param show true if the file permissions should be shown, otherwise false
     */
    public void setShowExplorerFilePermissions(boolean show) {
        setExplorerSetting(show, I_CmsWpConstants.C_FILELIST_PERMISSIONS);
    }
    
    /**
     * Sets if the file size should be shown in explorer view.<p>
     * 
     * @param show true if the file size should be shown, otherwise false
     */
    public void setShowExplorerFileSize(boolean show) {
        setExplorerSetting(show, I_CmsWpConstants.C_FILELIST_SIZE);
    }

    /**
     * Sets if the file state should be shown in explorer view.<p>
     * 
     * @param show true if the state size should be shown, otherwise false
     */
    public void setShowExplorerFileState(boolean show) {
        setExplorerSetting(show, I_CmsWpConstants.C_FILELIST_STATE);
    }

    /**
     * Sets if the file title should be shown in explorer view.<p>
     * 
     * @param show true if the file title should be shown, otherwise false
     */
    public void setShowExplorerFileTitle(boolean show) {
        setExplorerSetting(show, I_CmsWpConstants.C_FILELIST_TITLE);
    }

    /**
     * Sets if the file type should be shown in explorer view.<p>
     * 
     * @param show true if the file type should be shown, otherwise false
     */
    public void setShowExplorerFileType(boolean show) {
        setExplorerSetting(show, I_CmsWpConstants.C_FILELIST_TYPE);
    }

    /**
     * Sets if the file creator should be shown in explorer view.<p>
     * 
     * @param show true if the file creator should be shown, otherwise false
     */
    public void setShowExplorerFileUserCreated(boolean show) {
        setExplorerSetting(show, I_CmsWpConstants.C_FILELIST_USER_CREATED);
    }

   /**
    * Sets if the file last modified by should be shown in explorer view.<p>
    * 
    * @param show true if the file last modified by should be shown, otherwise false
    */
   public void setShowExplorerFileUserLastModified(boolean show) {
       setExplorerSetting(show, I_CmsWpConstants.C_FILELIST_USER_LASTMODIFIED);
   }
    
    /**
     *  Sets if the lock dialog should be shown.<p>
     * 
     * @param show true if the lock dialog should be shown, otherwise false
     */
    public void setShowLockDialog(boolean show) {
        if (show) {
            m_workplaceSettings.put(I_CmsConstants.C_START_LOCKDIALOG, "on");
        } else {
            m_workplaceSettings.put(I_CmsConstants.C_START_LOCKDIALOG, "");
        }
    }

    /**
     * Sets the start project of the user.<p>
     * 
     * @param projectId the start project id of the user
     */
    public void setStartProject(String projectId) {
        m_workplaceSettings.put(I_CmsConstants.C_START_PROJECT, new Integer(projectId));
    }
    
    /**
     * Sets the current start view of the user.<p>
     * 
     * @param view the current start view of the user
     */
    public void setStartView(String view) {
        m_workplaceSettings.put(I_CmsConstants.C_START_VIEW, view);
    }

    /**
     * Sets if a message should be sent if the task is accepted.<p>
     * 
     * @param message true if a message should be sent if the task is accepted, otherwise false
     */
    public void setTaskMessageAccepted(boolean message) {
        setTaskMessageSetting(message, I_CmsConstants.C_TASK_MESSAGES_ACCEPTED);
    }

    /**
     * Sets if a message should be sent if the task is completed.<p>
     * 
     * @param message true if a message should be sent if the task is completed, otherwise false
     */
    public void setTaskMessageCompleted(boolean message) {
        setTaskMessageSetting(message, I_CmsConstants.C_TASK_MESSAGES_COMPLETED);
    }

    /**
     * Sets if a message should be sent if the task is forwarded.<p>
     * 
     * @param message true if a message should be sent if the task is forwarded, otherwise false
     */
    public void setTaskMessageForwarded(boolean message) {
        setTaskMessageSetting(message, I_CmsConstants.C_TASK_MESSAGES_FORWARDED);
    }

    /**
     * Sets if all role members should be informed about the task.<p>
     * 
     * @param message true if all role members should be informed about the task, otherwise false
     */
    public void setTaskMessageMembers(boolean message) {
        setTaskMessageSetting(message, I_CmsConstants.C_TASK_MESSAGES_MEMBERS);
    }
    
    /**
     * Sets a specific task message setting depending on the set parameter.<p>
     * 
     * @param set true if the setting should be set, otherwise false
     * @param setting the settings constant value for the task message settings
     */
    private void setTaskMessageSetting(boolean set, int setting) {
        if (set) {
            m_taskMessages |= setting;
        } else {
            m_taskMessages &= ~setting;
        }
    }
    
    /**
     * Sets if all projects should be shown in tasks view.<p>
     * 
     * @param show true if all projects should be shown in tasks view, otherwise false
     */
    public void setTaskShowAllProjects(boolean show) {
        m_taskSettings.put(I_CmsConstants.C_TASK_VIEW_ALL, new Boolean(show));
    }
    
    /**
     * Sets the startup filter for the tasks view.<p>
     * 
     * @param filter the startup filter for the tasks view
     */
    public void setTaskStartupFilter(String filter) {
        m_taskSettings.put(I_CmsConstants.C_TASK_FILTER, filter);
    }
    
    /**
     * Sets the current user for the settings.<p>
     * 
     * @param user the CmsUser
     */
    public void setUser(CmsUser user) {
        m_user = user;
    }

    /**
     *  Sets if the upload applet should be used.<p>
     * 
     * @param use true if the upload applet should be used, otherwise false
     */
    public void setUseUploadApplet(boolean use) {
        if (use) {
            m_workplaceSettings.put(I_CmsConstants.C_START_UPLOADAPPLET, "on");
        } else {
            m_workplaceSettings.put(I_CmsConstants.C_START_UPLOADAPPLET, "");
        }
    }
    
    /**
     * Sets the style of the workplace buttons of the user.<p>
     * 
     * @param style the style of the workplace buttons of the user
     */
    public void setWorkplaceButtonStyle(int style) {
        m_workplaceSettings.put(C_WORKPLACE_BUTTONSTYLE, "" + style);
    }
    
    /**
     * Sets the type of the report (simple or extended) of the user.<p>
     * 
     * @param type the type of the report (simple or extended) of the user
     */
    public void setWorkplaceReportType(String type) {
        m_workplaceSettings.put(C_WORKPLACE_REPORTTYPE, type);
    }
    
    /**
     * Determines if the file creation date should be shown in explorer view.<p>
     * 
     * @return true if the file creation date should be shown, otherwise false
     */
    public boolean showExplorerFileDateCreated() {
        return ((m_explorerSettings & I_CmsWpConstants.C_FILELIST_DATE_CREATED) > 0); 
    }
    
    /**
     * Determines if the file last modified date should be shown in explorer view.<p>
     * 
     * @return true if the file last modified date should be shown, otherwise false
     */
    public boolean showExplorerFileDateLastModified() {
        return ((m_explorerSettings & I_CmsWpConstants.C_FILELIST_DATE_LASTMODIFIED) > 0); 
    }
    
    /**
     * Determines if the file locked by should be shown in explorer view.<p>
     * 
     * @return true if the file locked by should be shown, otherwise false
     */
    public boolean showExplorerFileLockedBy() {
        return ((m_explorerSettings & I_CmsWpConstants.C_FILELIST_LOCKEDBY) > 0); 
    }
    
    /**
     * Determines if the file permissions should be shown in explorer view.<p>
     * 
     * @return true if the file permissions should be shown, otherwise false
     */
    public boolean showExplorerFilePermissions() {
        return ((m_explorerSettings & I_CmsWpConstants.C_FILELIST_PERMISSIONS) > 0); 
    }

    /**
     * Determines if the file size should be shown in explorer view.<p>
     * 
     * @return true if the file size should be shown, otherwise false
     */
    public boolean showExplorerFileSize() {
        return ((m_explorerSettings & I_CmsWpConstants.C_FILELIST_SIZE) > 0); 
    }
    
    /**
     * Determines if the file state should be shown in explorer view.<p>
     * 
     * @return true if the file state should be shown, otherwise false
     */
    public boolean showExplorerFileState() {
        return ((m_explorerSettings & I_CmsWpConstants.C_FILELIST_STATE) > 0); 
    }
    
    /**
     * Determines if the file title should be shown in explorer view.<p>
     * 
     * @return true if the file title should be shown, otherwise false
     */
    public boolean showExplorerFileTitle() {
        return ((m_explorerSettings & I_CmsWpConstants.C_FILELIST_TITLE) > 0); 
    }
    
    /**
     * Determines if the file type should be shown in explorer view.<p>
     * 
     * @return true if the file type should be shown, otherwise false
     */
    public boolean showExplorerFileType() {
        return ((m_explorerSettings & I_CmsWpConstants.C_FILELIST_TYPE) > 0); 
    }
    
    /**
     * Determines if the file creator should be shown in explorer view.<p>
     * 
     * @return true if the file creator should be shown, otherwise false
     */
    public boolean showExplorerFileUserCreated() {
        return ((m_explorerSettings & I_CmsWpConstants.C_FILELIST_USER_CREATED) > 0); 
    }
    
   /**
    * Determines if the file last modified by should be shown in explorer view.<p>
    * 
    * @return true if the file last modified by should be shown, otherwise false
    */
   public boolean showExplorerFileUserLastModified() {
       return ((m_explorerSettings & I_CmsWpConstants.C_FILELIST_USER_LASTMODIFIED) > 0); 
   }
    
    /**
     * Determines if the lock dialog should be shown.<p>
     * 
     * @return true if the lock dialog is shown, otherwise false
     */
    public boolean showLockDialog() {
        String show = null;
        try {
            show = (String)m_workplaceSettings.get(I_CmsConstants.C_START_LOCKDIALOG);
        } catch (NullPointerException e) {
            // do nothing
        }
        return ("on".equals(show));
    }
    
    /**
     * Determines if a message should be sent if the task is accepted.<p>
     * 
     * @return true if a message should be sent if the task is accepted, otherwise false
     */
    public boolean taskMessageAccepted() {
        return ((m_taskMessages & I_CmsConstants.C_TASK_MESSAGES_ACCEPTED) > 0); 
    }
    
    /**
     * Determines if a message should be sent if the task is completed.<p>
     * 
     * @return true if a message should be sent if the task is completed, otherwise false
     */
    public boolean taskMessageCompleted() {
        return ((m_taskMessages & I_CmsConstants.C_TASK_MESSAGES_COMPLETED) > 0); 
    }
    
    /**
     * Determines if a message should be sent if the task is forwarded.<p>
     * 
     * @return true if a message should be sent if the task is forwarded, otherwise false
     */
    public boolean taskMessageForwarded() {
        return ((m_taskMessages & I_CmsConstants.C_TASK_MESSAGES_FORWARDED) > 0); 
    }
    
    /**
     * Determines if all role members should be informed about the task.<p>
     * 
     * @return true if all role members should be informed about the task, otherwise false
     */
    public boolean taskMessageMembers() {
        return ((m_taskMessages & I_CmsConstants.C_TASK_MESSAGES_MEMBERS) > 0); 
    }
    
    /**
     * Determines if all projects should be shown in tasks view.<p>
     * @return true if all projects should be shown in tasks view, otherwise false
     */
    public boolean taskShowAllProjects() {
        Boolean show = (Boolean)m_taskSettings.get(I_CmsConstants.C_TASK_VIEW_ALL);
        return show.booleanValue();
    }
    
    /**
     * Determines if the upload applet should be used.<p>
     * 
     * @return true if the if the upload applet should be used, otherwise false
     */
    public boolean useUploadApplet() {
        String use = null;
        try {
            use = (String)m_workplaceSettings.get(I_CmsConstants.C_START_UPLOADAPPLET);
        } catch (NullPointerException e) {
            // do nothing
        }
        return ("on".equals(use));
    }
    
}

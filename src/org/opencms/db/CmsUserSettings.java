/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsUserSettings.java,v $
 * Date   : $Date: 2004/02/03 17:06:44 $
 * Version: $Revision: 1.2 $
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

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsUser;
import com.opencms.workplace.I_CmsWpConstants;

import org.opencms.workplace.CmsReport;

import java.util.Hashtable;

/**
 * Object to conveniently access and modify the users workplace settings.<p>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.2 $
 * 
 * @since 5.1.12
 */
public class CmsUserSettings {
    
    /** Identifier for the explorer button style setting key */
    public static final String C_EXPLORER_BUTTONSTYLE = "EXPLORER_BUTTONSTYLE";
    /** Identifier for the explorer number of file entries per page setting key */
    public static final String C_EXPLORER_FILEENTRIES = "USER_EXPLORER_FILEENTRIES";
    /** Identifier for the workplace button style setting key */
    public static final String C_WORKPLACE_BUTTONSTYLE = "WORKPLACE_BUTTONSTYLE";
    /** Identifier for the workplace report type setting key */
    public static final String C_WORKPLACE_REPORTTYPE = "WORKPLACE_REPORTTYPE";
    
    private static final int C_ENTRYS_PER_PAGE_DEFAULT = 50;
    private static final int C_BUTTONSTYLE_DEFAULT = 1;
    private static final String C_REPORTTYPE_DEFAULT = CmsReport.REPORT_TYPE_SIMPLE;
    
    private int m_explorerButtonStyle;
    private int m_explorerFileEntries;
    private int m_explorerSettings;
    private Hashtable m_taskSettings;
    private int m_taskMessages;
    private Hashtable m_workplaceSettings;
    private String m_town;
    private String m_zipCode;
    private CmsUser m_user;

    /**
     * Creates an empty new user settings object.<p>
     */
    public CmsUserSettings() {
        m_explorerButtonStyle = C_BUTTONSTYLE_DEFAULT;
        m_explorerFileEntries = C_ENTRYS_PER_PAGE_DEFAULT;
        m_explorerSettings = I_CmsWpConstants.C_FILELIST_NAME;
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

        cms.writeUser(m_user);
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
     * Sets the town of the user.<p>
     * 
     * @param value the town of the user 
     */
    public void setAddressTown(String value) {
        m_town = value;
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
     * Sets the zipcode of the user.<p>
     * 
     * @param value the zipcode of the user 
     */
    public void setAddressZip(String value) {
        m_zipCode = value;
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
     * Sets the current user for the settings.<p>
     * 
     * @param user the CmsUser
     */
    public void setUser(CmsUser user) {
        m_user = user;
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
     * Determines if the file creation date should be shown in explorer view.<p>
     * 
     * @return true if the file creation date should be shown, otherwise false
     */
    public boolean showExplorerFileDateCreated() {
        return ((m_explorerSettings & I_CmsWpConstants.C_FILELIST_DATE_CREATED) > 0); 
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
     * Determines if the file last modified date should be shown in explorer view.<p>
     * 
     * @return true if the file last modified date should be shown, otherwise false
     */
    public boolean showExplorerFileDateLastModified() {
        return ((m_explorerSettings & I_CmsWpConstants.C_FILELIST_DATE_LASTMODIFIED) > 0); 
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
     * Determines if the file locked by should be shown in explorer view.<p>
     * 
     * @return true if the file locked by should be shown, otherwise false
     */
    public boolean showExplorerFileLockedBy() {
        return ((m_explorerSettings & I_CmsWpConstants.C_FILELIST_LOCKEDBY) > 0); 
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
     * Determines if the file permissions should be shown in explorer view.<p>
     * 
     * @return true if the file permissions should be shown, otherwise false
     */
    public boolean showExplorerFilePermissions() {
        return ((m_explorerSettings & I_CmsWpConstants.C_FILELIST_PERMISSIONS) > 0); 
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
     * Determines if the file size should be shown in explorer view.<p>
     * 
     * @return true if the file size should be shown, otherwise false
     */
    public boolean showExplorerFileSize() {
        return ((m_explorerSettings & I_CmsWpConstants.C_FILELIST_SIZE) > 0); 
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
     * Determines if the file state should be shown in explorer view.<p>
     * 
     * @return true if the file state should be shown, otherwise false
     */
    public boolean showExplorerFileState() {
        return ((m_explorerSettings & I_CmsWpConstants.C_FILELIST_STATE) > 0); 
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
     * Determines if the file title should be shown in explorer view.<p>
     * 
     * @return true if the file title should be shown, otherwise false
     */
    public boolean showExplorerFileTitle() {
        return ((m_explorerSettings & I_CmsWpConstants.C_FILELIST_TITLE) > 0); 
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
     * Determines if the file type should be shown in explorer view.<p>
     * 
     * @return true if the file type should be shown, otherwise false
     */
    public boolean showExplorerFileType() {
        return ((m_explorerSettings & I_CmsWpConstants.C_FILELIST_TYPE) > 0); 
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
     * Determines if the file creator should be shown in explorer view.<p>
     * 
     * @return true if the file creator should be shown, otherwise false
     */
    public boolean showExplorerFileUserCreated() {
        return ((m_explorerSettings & I_CmsWpConstants.C_FILELIST_USER_CREATED) > 0); 
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
    * Determines if the file last modified by should be shown in explorer view.<p>
    * 
    * @return true if the file last modified by should be shown, otherwise false
    */
   public boolean showExplorerFileUserLastModified() {
       return ((m_explorerSettings & I_CmsWpConstants.C_FILELIST_USER_LASTMODIFIED) > 0); 
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
    * Returns the style of the explorer buttons of the user.<p>
    * 
    * @return the style of the explorer buttons of the user
    */
   public int getExplorerButtonStyle() {
       return m_explorerButtonStyle;
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
    * Returns the number of displayed files per page of the user.<p>
    * 
    * @return the number of displayed files per page of the user
    */
   public int getExplorerFileEntries() {
       return m_explorerFileEntries;
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
     * Returns the start language of the user.<p>
     * 
     * @return the start language of the user
     */
    public String getStartLanguage() {
        return (String)m_workplaceSettings.get(I_CmsConstants.C_START_LANGUAGE);
    }
    
    /**
     * Sets the start language of the user.<p>
     * 
     * @param language the start language of the user
     */
    public void setStartLanguage(String language) {
        m_workplaceSettings.put(I_CmsConstants.C_START_LANGUAGE, language);
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
     * Sets the start project of the user.<p>
     * 
     * @param projectId the start project id of the user
     */
    public void setStartProject(String projectId) {
        m_workplaceSettings.put(I_CmsConstants.C_START_PROJECT, new Integer(projectId));
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
     * Sets the current start view of the user.<p>
     * 
     * @param view the current start view of the user
     */
    public void setStartView(String view) {
        m_workplaceSettings.put(I_CmsConstants.C_START_VIEW, view);
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
     * Sets the style of the workplace buttons of the user.<p>
     * 
     * @param style the style of the workplace buttons of the user
     */
    public void setWorkplaceButtonStyle(int style) {
        m_workplaceSettings.put(C_WORKPLACE_BUTTONSTYLE, "" + style);
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
     * Sets the type of the report (simple or extended) of the user.<p>
     * 
     * @param type the type of the report (simple or extended) of the user
     */
    public void setWorkplaceReportType(String type) {
        m_workplaceSettings.put(C_WORKPLACE_REPORTTYPE, type);
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
     * Returns the startup filter for the tasks view.<p>
     * 
     * @return the startup filter for the tasks view
     */
    public String getTaskStartupFilter() {
        return (String)m_taskSettings.get(I_CmsConstants.C_TASK_FILTER);
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
     * Determines if all projects should be shown in tasks view.<p>
     * @return true if all projects should be shown in tasks view, otherwise false
     */
    public boolean taskShowAllProjects() {
        Boolean show = (Boolean)m_taskSettings.get(I_CmsConstants.C_TASK_VIEW_ALL);
        return show.booleanValue();
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
     * Determines if a message should be sent if the task is accepted.<p>
     * 
     * @return true if a message should be sent if the task is accepted, otherwise false
     */
    public boolean taskMessageAccepted() {
        return ((m_taskMessages & I_CmsConstants.C_TASK_MESSAGES_ACCEPTED) > 0); 
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
     * Determines if a message should be sent if the task is forwarded.<p>
     * 
     * @return true if a message should be sent if the task is forwarded, otherwise false
     */
    public boolean taskMessageForwarded() {
        return ((m_taskMessages & I_CmsConstants.C_TASK_MESSAGES_FORWARDED) > 0); 
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
     * Determines if a message should be sent if the task is completed.<p>
     * 
     * @return true if a message should be sent if the task is completed, otherwise false
     */
    public boolean taskMessageCompleted() {
        return ((m_taskMessages & I_CmsConstants.C_TASK_MESSAGES_COMPLETED) > 0); 
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
     * Determines if all role members should be informed about the task.<p>
     * 
     * @return true if all role members should be informed about the task, otherwise false
     */
    public boolean taskMessageMembers() {
        return ((m_taskMessages & I_CmsConstants.C_TASK_MESSAGES_MEMBERS) > 0); 
    }

    /**
     * Sets if all role members should be informed about the task.<p>
     * 
     * @param message true if all role members should be informed about the task, otherwise false
     */
    public void setTaskMessageMembers(boolean message) {
        setTaskMessageSetting(message, I_CmsConstants.C_TASK_MESSAGES_MEMBERS);
    }
    
}

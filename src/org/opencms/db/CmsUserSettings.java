/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsUserSettings.java,v $
 * Date   : $Date: 2003/12/02 16:24:42 $
 * Version: $Revision: 1.1 $
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

import java.util.Hashtable;

/**
 * Object to conveniently access and modify the users workplace settings.<p>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.1.12
 */
public class CmsUserSettings {
    
    private int m_explorerSettings;
    private Hashtable m_taskSettings;
    private int m_taskMessages;
    private Hashtable m_startSettings;
    private String m_town;
    private String m_zipCode;
    private CmsUser m_user;

    /**
     * Creates an empty new user settings object.<p>
     */
    public CmsUserSettings() {
        m_explorerSettings = I_CmsWpConstants.C_FILELIST_NAME;
        m_taskSettings = new Hashtable();
        m_taskMessages = 0;
        m_startSettings = new Hashtable();
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
        String explorerSettingsString = (String)m_user.getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_EXPLORERSETTINGS); 
        m_explorerSettings = I_CmsWpConstants.C_FILELIST_NAME;
        try {
            m_explorerSettings = Integer.parseInt(explorerSettingsString);
        } catch (NumberFormatException e) {
            // explorer start settings are not present, set them to default
            setShowExplorerFileTitle(true);
            setShowExplorerFileType(true);
            setShowExplorerFileDateLastModified(true);
        }
        m_startSettings = (Hashtable)m_user.getAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_STARTSETTINGS);
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
        m_user.setAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_EXPLORERSETTINGS, "" + m_explorerSettings);
        m_user.setAdditionalInfo(I_CmsConstants.C_ADDITIONAL_INFO_STARTSETTINGS, m_startSettings);
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
     * Determines if the lock dialog should be shown.<p>
     * 
     * @return true if the lock dialog is shown, otherwise false
     */
    public boolean showLockDialog() {
        String show = null;
        try {
            show = (String)m_startSettings.get(I_CmsConstants.C_START_LOCKDIALOG);
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
            m_startSettings.put(I_CmsConstants.C_START_LOCKDIALOG, "on");
        } else {
            m_startSettings.put(I_CmsConstants.C_START_LOCKDIALOG, "");
        }
    }
    
    /** 
     * Returns the start language of the user.<p>
     * 
     * @return the start language of the user
     */
    public String getStartLanguage() {
        return (String)m_startSettings.get(I_CmsConstants.C_START_LANGUAGE);
    }
    
    /**
     * Sets the start language of the user.<p>
     * 
     * @param language the start language of the user
     */
    public void setStartLanguage(String language) {
        m_startSettings.put(I_CmsConstants.C_START_LANGUAGE, language);
    }
    
    /** 
     * Returns the start project of the user.<p>
     * 
     * @return the start project of the user
     */
    public String getStartProject() {
        Integer projectId = (Integer)m_startSettings.get(I_CmsConstants.C_START_PROJECT);       
        return projectId.toString();
    }

    /**
     * Sets the start project of the user.<p>
     * 
     * @param projectId the start project id of the user
     */
    public void setStartProject(String projectId) {
        m_startSettings.put(I_CmsConstants.C_START_PROJECT, new Integer(projectId));
    }
    
    /**
     * Returns the current start view of the user.<p>
     * 
     * @return the current start view of the user
     */
    public String getStartView() {
        return (String)m_startSettings.get(I_CmsConstants.C_START_VIEW);
    }
    
    /**
     * Sets the current start view of the user.<p>
     * 
     * @param view the current start view of the user
     */
    public void setStartView(String view) {
        m_startSettings.put(I_CmsConstants.C_START_VIEW, view);
    }
    
    /**
     * Determines if the upload applet should be used.<p>
     * 
     * @return true if the if the upload applet should be used, otherwise false
     */
    public boolean useUploadApplet() {
        String use = null;
        try {
            use = (String)m_startSettings.get(I_CmsConstants.C_START_UPLOADAPPLET);
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
            m_startSettings.put(I_CmsConstants.C_START_UPLOADAPPLET, "on");
        } else {
            m_startSettings.put(I_CmsConstants.C_START_UPLOADAPPLET, "");
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

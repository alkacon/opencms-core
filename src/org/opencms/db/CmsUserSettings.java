/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsUserSettings.java,v $
 * Date   : $Date: 2004/05/13 13:58:10 $
 * Version: $Revision: 1.11 $
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

import org.opencms.configuration.CmsWorkplaceConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsReport;
import org.opencms.workplace.I_CmsWpConstants;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Object to conveniently access and modify the users workplace settings.<p>
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @author  Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.11 $
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
    
    /** Identifierprefix for all keys in the user additional info table */
    public static final String C_PREFERENCES = "USERPREFERENCES_";
    
    /** Identifier for the workplace button style setting key */
    public static final String C_WORKPLACE_BUTTONSTYLE = "WORKPLACE_BUTTONSTYLE";
    
    /** Identifier for the workplace report type setting key */
    public static final String C_WORKPLACE_REPORTTYPE = "WORKPLACE_REPORTTYPE";
    private boolean m_dialogDirectpublish;
    private int m_dialogFileCopy;
    private int m_dialogFileDelete;
    private int m_dialogFolderCopy;
    private int m_directeditButtonStyle;
    private int m_editorButtonStyle;
    private HashMap m_editorSettings; 
    private int m_explorerButtonStyle;
    private int m_explorerFileEntries;
    private int m_explorerSettings;
    private Locale m_locale;
    private String m_project;
    private boolean m_showLock;
    private int m_taskMessages;
    private boolean m_taskShowProjects;
    private String m_taskStartupfilter;
    private boolean m_uploadApplet;
  
    private CmsUser m_user;
    private String m_view;
    
    /** member variables to store all the data of the user settings */
    private int m_workplaceButtonStyle;
    private String m_workplaceReportType;

    /**
     * Creates an empty new user settings object.<p>
     */
    public CmsUserSettings() {   
        m_workplaceButtonStyle = C_BUTTONSTYLE_DEFAULT;
        m_workplaceReportType = CmsReport.REPORT_TYPE_SIMPLE;        
        m_explorerButtonStyle = C_BUTTONSTYLE_DEFAULT;
        m_explorerFileEntries = C_ENTRYS_PER_PAGE_DEFAULT;
        m_explorerSettings = I_CmsWpConstants.C_FILELIST_NAME;
        m_editorSettings = new HashMap();
        m_taskMessages = 0;
        m_user = null;
      
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
     * Gets the default copy mode when copying a file of the user.<p>
     * 
     * @return the default copy mode when copying a file of the user
     */
    public int getDialogCopyFileMode() {
        return m_dialogFileCopy;
    }
    
    /**
     * Gets the default copy mode when copying a folder of the user.<p>
     * 
     * @return the default copy mode when copying a folder of the user
     */
    public int getDialogCopyFolderMode() {
         return m_dialogFolderCopy;
    }
    
    /**
     * Returns the default setting for file deletion.<p>
     * 
     * @return the default setting for file deletion
     */
    public int getDialogDeleteFileMode() {
        return m_dialogFileDelete;
    }
    
    /**
     * Returns the default setting for direct publishing.<p>
     * 
     * @return the default setting for direct publishing: true if siblings should be published, otherwise false
     */
    public boolean getDialogPublishSiblings() {
        return  m_dialogDirectpublish; 
    }
    
    /**
     * Determines if the lock dialog should be shown.<p>
     * 
     * @return true if the lock dialog is shown, otherwise false
     */
    public boolean getDialogShowLock() {
        return m_showLock;
    }
    
    /**
     * Returns the style of the direct edit buttons of the user.<p>
     * 
     * @return the style of the direct edit buttons of the user
     */
    public int getDirectEditButtonStyle() {
        return m_directeditButtonStyle;       
    }
    
    /**
     * Returns the style of the editor buttons of the user.<p>
     * 
     * @return the style of the editor buttons of the user
     */
    public int getEditorButtonStyle() {
        return m_editorButtonStyle;
    }
    
    /**
     * Returns the editor settings of the user.<p>
     * 
     * @return the editor settings of the user
     */
    public Map getEditorSettings() {
        return m_editorSettings;
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
    * Returns the explorer start settings.<p>
    * 
    * @return the explorer start settings
    */
   public int getExplorerSettings() {
       return m_explorerSettings;
   }
   
    /** 
     * Returns the locale of the user.<p>
     * 
     * @return the loclae of the user
     */
    public Locale getLocale() {
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
        return m_project;
    }
    
    /**
     * Returns the current start view of the user.<p>
     * 
     * @return the current start view of the user
     */
    public String getStartView() {
        return m_view;
    }
    
    /**
     * Determines if a message should be sent if the task is accepted.<p>
     * 
     * @return true if a message should be sent if the task is accepted, otherwise false
     */
    public boolean getTaskMessageAccepted() {
        return ((m_taskMessages & I_CmsConstants.C_TASK_MESSAGES_ACCEPTED) > 0); 
    }
    
    /**
     * Determines if a message should be sent if the task is completed.<p>
     * 
     * @return true if a message should be sent if the task is completed, otherwise false
     */
    public boolean getTaskMessageCompleted() {
        return ((m_taskMessages & I_CmsConstants.C_TASK_MESSAGES_COMPLETED) > 0); 
    }
    
    /**
     * Determines if a message should be sent if the task is forwarded.<p>
     * 
     * @return true if a message should be sent if the task is forwarded, otherwise false
     */
    public boolean getTaskMessageForwarded() {
        return ((m_taskMessages & I_CmsConstants.C_TASK_MESSAGES_FORWARDED) > 0); 
    }
    
    /**
     * Determines if all role members should be informed about the task.<p>
     * 
     * @return true if all role members should be informed about the task, otherwise false
     */
    public boolean getTaskMessageMembers() {
        return ((m_taskMessages & I_CmsConstants.C_TASK_MESSAGES_MEMBERS) > 0); 
    }
    
    /**
     * Returns the task messages value for the tasks view.<p>
     * 
     * @return task messages value for the tasks view
     */
    public int getTaskMessageValue() {
        return m_taskMessages;
    }
    
    /**
     * Determines if all projects should be shown in tasks view.<p>
     * @return true if all projects should be shown in tasks view, otherwise false
     */
    public boolean getTaskShowAllProjects() {
        return m_taskShowProjects;
    }
    
    /**
     * Returns the startup filter for the tasks view.<p>
     * 
     * @return the startup filter for the tasks view
     */
    public String getTaskStartupFilter() {
        return m_taskStartupfilter;
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
        return m_workplaceButtonStyle;
    }
    
    /**
     * Returns the type of the report (simple or extended) of the user.<p>
     * 
     * @return the type of the report (simple or extended) of the user
     */
    public String getWorkplaceReportType() {
        return m_workplaceReportType;
    }
    
    /**
     * Initializes the user settings with the given users setting parameters.<p>
     * 
     * @param user the current CmsUser
     */
    public void init(CmsUser user) {
        m_user = user; 

        // try to initaliaze the User Settings with the values stored in the user object.
        // if no values are found, the default user settings will be used.
        
        // workplace button style
        try {
            m_workplaceButtonStyle = ((Integer)m_user.getAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS + CmsWorkplaceConfiguration.N_BUTTONSTYLE)).intValue();
        } catch (Throwable t) {
            m_workplaceButtonStyle = OpenCms.getWorkplaceManager().getDefaultUserSettings().getWorkplaceButtonStyle();
        }
        // workplace report type
        m_workplaceReportType = ((String)m_user.getAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS + CmsWorkplaceConfiguration.N_REPORTTYPE));
        if (m_workplaceReportType == null) {
            m_workplaceReportType = OpenCms.getWorkplaceManager().getDefaultUserSettings().getWorkplaceReportType();
        }
        // workplace uploadapplet mode
        try {
            m_uploadApplet = ((Boolean)m_user.getAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS + CmsWorkplaceConfiguration.N_UPLOADAPPLET)).booleanValue();
        } catch (Throwable t) {
            m_uploadApplet  = OpenCms.getWorkplaceManager().getDefaultUserSettings().useUploadApplet();           
        }
        // locale
        m_locale = (Locale)m_user.getAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS + CmsWorkplaceConfiguration.N_LOCALE); 
        if (m_locale == null) {            
            m_locale =  OpenCms.getWorkplaceManager().getDefaultUserSettings().getLocale();   
        }
        // start project
        try {
            m_project = ((String)m_user.getAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS + CmsWorkplaceConfiguration.N_PROJECT));
        } catch (Throwable t) {
            m_project = null;
        }
        if (m_project == null) {
             m_project = OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartProject();
        }
        // start view
        m_view = ((String)m_user.getAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS + CmsWorkplaceConfiguration.N_WORKPLACEVIEW)); 
        if (m_view == null) {
            m_view = OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartView();
        }
        // explorer button style
        try {
            m_explorerButtonStyle = ((Integer)m_user.getAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_EXPLORERGENERALOPTIONS + CmsWorkplaceConfiguration.N_BUTTONSTYLE)).intValue();
        } catch (Throwable t) {
            m_explorerButtonStyle = OpenCms.getWorkplaceManager().getDefaultUserSettings().getExplorerButtonStyle();
        }
        // explorer file entires        
        try {
            m_explorerFileEntries = ((Integer)m_user.getAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_EXPLORERGENERALOPTIONS + CmsWorkplaceConfiguration.N_ENTRIES)).intValue();
        } catch (Throwable t) {
            m_explorerFileEntries = OpenCms.getWorkplaceManager().getDefaultUserSettings().getExplorerFileEntries();
        }
        // explorer settings
        try {
            m_explorerSettings = ((Integer)m_user.getAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_EXPLORERGENERALOPTIONS + CmsWorkplaceConfiguration.N_EXPLORERDISPLAYOPTIONS)).intValue();
        } catch (Throwable t) {
            m_explorerSettings = OpenCms.getWorkplaceManager().getDefaultUserSettings().getExplorerSettings();           
        }
        // dialog file copy mode
        try {
            m_dialogFileCopy = ((Integer)m_user.getAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS + CmsWorkplaceConfiguration.N_FILECOPY)).intValue();
        } catch (Throwable t) {
            m_dialogFileCopy  = OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogCopyFileMode();           
        }
        // dialog folder copy mode
        try {
            m_dialogFolderCopy = ((Integer)m_user.getAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS + CmsWorkplaceConfiguration.N_FOLDERCOPY)).intValue();
        } catch (Throwable t) {
            m_dialogFolderCopy  = OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogCopyFolderMode();           
        }
        // dialog file delete mode
        try {
            m_dialogFileDelete = ((Integer)m_user.getAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS + CmsWorkplaceConfiguration.N_FILEDELETION)).intValue();
        } catch (Throwable t) {
            m_dialogFileDelete  = OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogDeleteFileMode();           
        }
        // dialog directpublish mode
        try {
            m_dialogDirectpublish = ((Boolean)m_user.getAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS + CmsWorkplaceConfiguration.N_DIRECTPUBLISH)).booleanValue();
        } catch (Throwable t) {
            m_dialogDirectpublish  = OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogPublishSiblings();           
        }
        // dialog show lock mode
        try {
            m_showLock = ((Boolean)m_user.getAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS + CmsWorkplaceConfiguration.N_SHOWLOCK)).booleanValue();
        } catch (Throwable t) {
            m_showLock  = OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogShowLock();           
        }
        // editor button style
        try {
            m_editorButtonStyle = ((Integer)m_user.getAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_EDITORGENERALOPTIONS + CmsWorkplaceConfiguration.N_BUTTONSTYLE)).intValue();
        } catch (Throwable t) {
            m_editorButtonStyle = OpenCms.getWorkplaceManager().getDefaultUserSettings().getEditorButtonStyle();
        }
        // directedit button style
        try {
            m_directeditButtonStyle = ((Integer)m_user.getAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_EDITORGENERALOPTIONS + CmsWorkplaceConfiguration.N_DIRECTEDITSTYLE)).intValue();
        } catch (Throwable t) {
            m_directeditButtonStyle = OpenCms.getWorkplaceManager().getDefaultUserSettings().getDirectEditButtonStyle();
        }
        // editor settings
        m_editorSettings = (HashMap)m_user.getAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_EDITORPREFERREDEDITORS);
        if (m_editorSettings == null) {
            m_editorSettings = new HashMap(OpenCms.getWorkplaceManager().getDefaultUserSettings().getEditorSettings());
        }
        // task startupfilter
        m_taskStartupfilter = (String)m_user.getAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKFLOWGENERALOPTIONS + CmsWorkplaceConfiguration.N_STARTUPFILTER); 
        if (m_taskStartupfilter == null) {            
            m_taskStartupfilter =  OpenCms.getWorkplaceManager().getDefaultUserSettings().getTaskStartupFilter();
        } 
        // task show all projects
        try {
            m_taskShowProjects = ((Boolean)m_user.getAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKFLOWGENERALOPTIONS + CmsWorkplaceConfiguration.N_SHOWPROJECTS)).booleanValue();
        } catch (Throwable t) {
            m_taskShowProjects  = OpenCms.getWorkplaceManager().getDefaultUserSettings().getTaskShowAllProjects();           
        }
        // task messages
        try {
            m_taskMessages = ((Integer)m_user.getAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKFLOWDEFAULTSETTINGS)).intValue();
        } catch (Throwable t) {
            m_taskMessages = OpenCms.getWorkplaceManager().getDefaultUserSettings().getTaskMessageValue();
        }
 
        try {
            save(null);
        } catch (CmsException e) {
            // to nothing here            
        }
    }
    
    /**
     * Saves the changed settings of the user to the user object.<p>
     * 
     * If the given CmsObject is null, the additional user infos are only updated in memory
     * and not saved into the database.<p>
     * 
     * @param cms the CmsObject needed to write the user to the db
     * @throws CmsException if user cannot be written to the db
     */
    public void save(CmsObject cms) throws CmsException {
            
        // only set those values that are different than the default values
        // if the user info should be updated in the databas (i.e. the CmsObject != null)
        // all values that are equal to the defaul values must be deleted form the additinal
        // user settings.
        
        // workplace button style
        if (getWorkplaceButtonStyle() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getWorkplaceButtonStyle()) { 
            m_user.setAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS + CmsWorkplaceConfiguration.N_BUTTONSTYLE, new Integer(getWorkplaceButtonStyle()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS + CmsWorkplaceConfiguration.N_BUTTONSTYLE);
        }
        // workplace report type
        if (!getWorkplaceReportType().equals(OpenCms.getWorkplaceManager().getDefaultUserSettings().getWorkplaceReportType())) { 
            m_user.setAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS + CmsWorkplaceConfiguration.N_REPORTTYPE, getWorkplaceReportType());
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS + CmsWorkplaceConfiguration.N_REPORTTYPE);
        }
        // workplace uploadapplet
        if (useUploadApplet() != OpenCms.getWorkplaceManager().getDefaultUserSettings().useUploadApplet()) { 
            m_user.setAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS + CmsWorkplaceConfiguration.N_UPLOADAPPLET, new Boolean(useUploadApplet()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS + CmsWorkplaceConfiguration.N_UPLOADAPPLET);
        }
        // locale
        if (!getLocale().equals(OpenCms.getWorkplaceManager().getDefaultUserSettings().getLocale())) { 
            m_user.setAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS + CmsWorkplaceConfiguration.N_LOCALE, getLocale());
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS + CmsWorkplaceConfiguration.N_LOCALE);
        }
        // startproject       
        if (getStartProject() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartProject()) { 
            m_user.setAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS + CmsWorkplaceConfiguration.N_PROJECT, getStartProject());
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS + CmsWorkplaceConfiguration.N_PROJECT);
        }
        // view
        if (!getStartView().equals(OpenCms.getWorkplaceManager().getDefaultUserSettings().getStartView())) { 
            m_user.setAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS + CmsWorkplaceConfiguration.N_WORKPLACEVIEW, getStartView());
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKPLACESTARTUPSETTINGS + CmsWorkplaceConfiguration.N_WORKPLACEVIEW);
        }
        // explorer button style    
        if (getExplorerButtonStyle() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getExplorerButtonStyle()) {          
            m_user.setAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_EXPLORERGENERALOPTIONS + CmsWorkplaceConfiguration.N_BUTTONSTYLE, new Integer(getExplorerButtonStyle()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_EXPLORERGENERALOPTIONS + CmsWorkplaceConfiguration.N_BUTTONSTYLE);
        }
        // explorer file entires
        if (getExplorerFileEntries() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getExplorerFileEntries()) {
            m_user.setAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_EXPLORERGENERALOPTIONS + CmsWorkplaceConfiguration.N_ENTRIES, new Integer(getExplorerFileEntries())); 
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_EXPLORERGENERALOPTIONS + CmsWorkplaceConfiguration.N_ENTRIES);
        }
        // explorer settings
        if (getExplorerSettings() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getExplorerSettings()) {       
            m_user.setAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_EXPLORERGENERALOPTIONS + CmsWorkplaceConfiguration.N_EXPLORERDISPLAYOPTIONS, new Integer(getExplorerSettings()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_EXPLORERGENERALOPTIONS + CmsWorkplaceConfiguration.N_EXPLORERDISPLAYOPTIONS);
        }
        // dialog file copy mode
        if (getDialogCopyFileMode() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogCopyFileMode()) {       
            m_user.setAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS + CmsWorkplaceConfiguration.N_FILECOPY, new Integer(getDialogCopyFileMode()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS + CmsWorkplaceConfiguration.N_FILECOPY);
        }
        // dialog folder copy mode
        if (getDialogCopyFolderMode() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogCopyFolderMode()) {       
            m_user.setAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS + CmsWorkplaceConfiguration.N_FOLDERCOPY, new Integer(getDialogCopyFolderMode()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS + CmsWorkplaceConfiguration.N_FOLDERCOPY);
        }
        // dialog file delete mode
        if (getDialogDeleteFileMode() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogDeleteFileMode()) {       
            m_user.setAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS + CmsWorkplaceConfiguration.N_FILEDELETION, new Integer(getDialogDeleteFileMode()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS + CmsWorkplaceConfiguration.N_FILEDELETION);
        }
        // dialog directpublish mode
        if (getDialogPublishSiblings() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogPublishSiblings()) {       
            m_user.setAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS + CmsWorkplaceConfiguration.N_DIRECTPUBLISH, new Boolean(getDialogPublishSiblings()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS + CmsWorkplaceConfiguration.N_DIRECTPUBLISH);
        }
        // dialog directpublish mode
        if (getDialogShowLock() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getDialogShowLock()) {       
            m_user.setAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS + CmsWorkplaceConfiguration.N_SHOWLOCK, new Boolean(getDialogShowLock()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_DIALOGSDEFAULTSETTINGS + CmsWorkplaceConfiguration.N_SHOWLOCK);
        }
        // editor button style    
        if (getEditorButtonStyle() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getEditorButtonStyle()) {          
            m_user.setAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_EDITORGENERALOPTIONS + CmsWorkplaceConfiguration.N_BUTTONSTYLE, new Integer(getEditorButtonStyle()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_EDITORGENERALOPTIONS + CmsWorkplaceConfiguration.N_BUTTONSTYLE);
        }
        // directedit button style    
        if (getDirectEditButtonStyle() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getDirectEditButtonStyle()) {          
            m_user.setAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_EDITORGENERALOPTIONS + CmsWorkplaceConfiguration.N_DIRECTEDITSTYLE, new Integer(getDirectEditButtonStyle()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_EDITORGENERALOPTIONS + CmsWorkplaceConfiguration.N_DIRECTEDITSTYLE);
        }
        // editorsettings
        if (m_editorSettings.size() > 0) {
            m_user.setAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_EDITORPREFERREDEDITORS, m_editorSettings);
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_EDITORPREFERREDEDITORS);
        }
        // task startup filter
        if (!getTaskStartupFilter().equals(OpenCms.getWorkplaceManager().getDefaultUserSettings().getTaskStartupFilter())) { 
            m_user.setAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKFLOWGENERALOPTIONS + CmsWorkplaceConfiguration.N_STARTUPFILTER, getTaskStartupFilter());
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKFLOWGENERALOPTIONS + CmsWorkplaceConfiguration.N_STARTUPFILTER);
        }
        // task show all projects
        if (getTaskShowAllProjects() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getTaskShowAllProjects()) {          
            m_user.setAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKFLOWGENERALOPTIONS + CmsWorkplaceConfiguration.N_SHOWPROJECTS, new Boolean(getTaskShowAllProjects()));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKFLOWGENERALOPTIONS + CmsWorkplaceConfiguration.N_SHOWPROJECTS);
        }
        // task messages        
        if (getTaskMessageValue() != OpenCms.getWorkplaceManager().getDefaultUserSettings().getTaskMessageValue()) {
              m_user.setAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKFLOWDEFAULTSETTINGS, new Integer(m_taskMessages));
        } else if (cms != null) {
            m_user.deleteAdditionalInfo(C_PREFERENCES + CmsWorkplaceConfiguration.N_WORKFLOWDEFAULTSETTINGS);
        }     
      
        // only write the updated date to the DB if we have the cms object
        if (cms != null) {           
            cms.writeUser(m_user);
        }
    }

    /**
     * Sets the default copy mode when copying a file of the user.<p>
     * 
     * @param mode the default copy mode when copying a file of the user
     */
    public void setDialogCopyFileMode(int mode) {
        m_dialogFileCopy = mode;
    }
    
    /**
     * Sets the default copy mode when copying a folder of the user.<p>
     * 
     * @param mode the default copy mode when copying a folder of the user
     */
    public void setDialogCopyFolderMode(int mode) {
        m_dialogFolderCopy = mode;
    }
    
    /**
     * Sets the default setting for file deletion.<p>
     * 
     * @param mode the default setting for file deletion
     */
    public void setDialogDeleteFileMode(int mode) {
        m_dialogFileDelete = mode;
    }
    
    /**
     * Sets the default setting for direct publishing.<p>
     * 
     * @param publishSiblings the default setting for direct publishing: true if siblings should be published, otherwise false
     */
    public void setDialogPublishSiblings(boolean publishSiblings) {
        m_dialogDirectpublish = publishSiblings;
    }
    
    /**
     *  Sets if the lock dialog should be shown.<p>
     * 
     * @param show true if the lock dialog should be shown, otherwise false
     */
    public void setDialogShowLock(boolean show) {
        m_showLock = show;
    }
    
    /**
     * Sets the style of the direct edit buttons of the user.<p>
     * 
     * @param style the style of the direct edit buttons of the user
     */
    public void setDirectEditButtonStyle(int style) {
        m_directeditButtonStyle = style;
    }
    
    /**
     * Sets the style of the editor buttons of the user.<p>
     * 
     * @param style the style of the editor buttons of the user
     */
    public void setEditorButtonStyle(int style) {
        m_editorButtonStyle = style;
    }
   
    /**
     * Sets the editor settings of the user.<p>
     * 
     * @param settings the editor settings of the user
     */
    public void setEditorSettings(Map settings) {
        m_editorSettings = new HashMap(settings);
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
    * Sets the explorer start settings.<p>
    * 
    * @param settings explorer start settings tu use
    */
    public void setExplorerSettings(int settings) {
        m_explorerSettings = settings;
    }
    
    /**
     * Sets the locale of the user.<p>
     * 
     * @param locale the locale of the user
     */
    public void setLocale(Locale locale) {
        m_locale = locale;
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
     * Sets the start project of the user.<p>
     * 
     * @param project the start project id of the user
     */
    public void setStartProject(String project) {
        m_project = project;
    }
    
    /**
     * Sets the current start view of the user.<p>
     * 
     * @param view the current start view of the user
     */
    public void setStartView(String view) {
       m_view = view;
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
     * Sets the task message values.<p>
     * 
     * @param value the value of the task messages
     */
    public void setTaskMessageValue(int value) {
        m_taskMessages = value;    
    }
    
    /**
     * Sets if all projects should be shown in tasks view.<p>
     * 
     * @param show true if all projects should be shown in tasks view, otherwise false
     */
    public void setTaskShowAllProjects(boolean show) {
        m_taskShowProjects = show;
    }
    
    /**
     * Sets the startup filter for the tasks view.<p>
     * 
     * @param filter the startup filter for the tasks view
     */
    public void setTaskStartupFilter(String filter) {
       m_taskStartupfilter = filter;
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
       m_uploadApplet = use;
    }
    
    /**
     * Sets the style of the workplace buttons of the user.<p>
     * 
     * @param style the style of the workplace buttons of the user
     */
    public void setWorkplaceButtonStyle(int style) {
        m_workplaceButtonStyle = style;
    }
    
    /**
     * Sets the type of the report (simple or extended) of the user.<p>
     * 
     * @param type the type of the report (simple or extended) of the user
     */
    public void setWorkplaceReportType(String type) {
        m_workplaceReportType = type;
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
     * Determines if the upload applet should be used.<p>
     * 
     * @return true if the if the upload applet should be used, otherwise false
     */
    public boolean useUploadApplet() {
        return m_uploadApplet;
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
    
}

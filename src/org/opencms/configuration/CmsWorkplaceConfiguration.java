/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/CmsWorkplaceConfiguration.java,v $
 * Date   : $Date: 2004/05/13 13:58:10 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.configuration;

import org.opencms.db.CmsExportPoint;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsExplorerContextMenuItem;
import org.opencms.workplace.CmsExplorerTypeSettings;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceView;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.digester.Digester;

import org.dom4j.Element;

/**
 * Import/Export master configuration class.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3
 */
public class CmsWorkplaceConfiguration extends A_CmsXmlConfiguration implements I_CmsXmlConfiguration {
    
    /** The "isxml" attribute */
    protected static final String A_ISXML = "isxml";
    
    /** The "permissions" attribute */
    protected static final String A_PERMISSIONS = "permissions";
   
    /** The "principal" attribute */
    protected static final String A_PRINCIPAL = "principal";
    
    /** The "rules" attribute */
    protected static final String A_RULES = "rules";
    
    /** The "shownavigation" attribute */
    protected static final String A_SHOWNAVIGATION = "shownavigation";
    
    /** The "target" attribute */
    protected static final String A_TARGET = "target";

    
    /** The "value" attribute */
    protected static final String A_VALUE = "value";    
    
    /** The name of the DTD for this configuration */
    private static final String C_CONFIGURATION_DTD_NAME = "opencms-workplace.dtd";
    
    /** The name of the default XML file for this configuration */
    private static final String C_DEFAULT_XML_FILE_NAME = "opencms-workplace.xml";          
    
    /** The name of the access control node */
    protected static final String N_ACCESSCONTROL = "accesscontrol";
    
    /** The name of the access entry node */
    protected static final String N_ACCESSENTRY = "accessentry";
    
    /** The name of the autolock node */
    protected static final String N_AUTOLOCK = "autolock";
    
    /** The name of the context menu node */
    protected static final String N_CONTEXTMENU = "contextmenu";
    
    /** The name of the node for the default locale */
    protected static final String N_DEFAULTLOCALE = "defaultlocale";
    
    /** The name of the default properties node */
    protected static final String N_DEFAULTPROPERTIES = "defaultproperties";
    
    /** The name of the default properties on structure node */
    protected static final String N_DEFAULTPROPERTIESONSTRUCTURE = "defaultpropertiesonstructure";
    
    /** Indivividual workplace handler node name */
    protected static final String N_DIALOGHANDLER = "dialoghandler";      
    
    /** The main workplace handler node name */
    protected static final String N_DIALOGHANDLERS = "dialoghandlers";
    
    /** The name of the edit options node */
    protected static final String N_EDITOPTIONS = "editoptions";
    
    /** The name of the editor action node */
    protected static final String N_EDITORACTION = "editoraction";
    
    /** The name of the editor handler node */
    protected static final String N_EDITORHANDLER = "editorhandler";
    
    /** The name of the "enable advanced property tabs" node */
    protected static final String N_ENABLEADVANCEDPROPERTYTABS = "enableadvancedpropertytabs";
    
    /** The name of the "user management enabled" node */
    protected static final String N_ENABLEUSERMGMT = "enableusermanagement";
    
    /** The name of the entry node */
    protected static final String N_ENTRY = "entry";
    
    /** The name of the explorer type node */
    protected static final String N_EXPLORERTYPE = "explorertype";
    
    /** The name of the explorer types node */
    protected static final String N_EXPLORERTYPES = "explorertypes";
    
    /** The name of the "labeled folders" node */
    protected static final String N_LABELEDFOLDERS = "labeledfolders";    
    
    /** The name of the "max file upload size" node */
    protected static final String N_MAXUPLOADSIZE = "maxfileuploadsize";
    
    /** The name of the new resource node */
    protected static final String N_NEWRESOURCE = "newresource";
    
    /** The name of the separator node */
    protected static final String N_SEPARATOR = "separator";
    
    /** The node name of the master workplace node */       
    protected static final String N_WORKPLACE = "workplace";
    
    /** The node name of the default preferences node */
    protected static final String N_USER = "default-preferences";

    /** The node name of the workplace preferences node */
    protected static final String N_WORKPLACEPREFERENCES = "workplace-preferences";
    
    /** The node name of the workplace general options node */
    public static final String N_WORKPLACEGENERALOPTIONS = "workplace-generaloptions";

    /** The node name of the workplace startupsettings node */
    public static final String N_WORKPLACESTARTUPSETTINGS = "workplace-startupsettings";
   
    /** The node name of the explorer preferences node */
    protected static final String N_EXPLORERPREFERENCES = "explorer-preferences";
    
    /** The node name of the explorer generaloptions node */
    public static final String N_EXPLORERGENERALOPTIONS = "explorer-generaloptions";

    /** The node name of the explorer displayoptions node */
    public static final String N_EXPLORERDISPLAYOPTIONS = "explorer-displayoptions";
    
    /** The node name of the dialogs preferences node */
    protected static final String N_DIALOGSPREFERENCES = "dialogs-preferences";
    
    /** The node name of the dialogs defaultsettings node */
    public static final String N_DIALOGSDEFAULTSETTINGS = "dialogs-defaultsettings";
        
    /** The node name of the editor preferences node */
    protected static final String N_EDITORPREFERENCES = "editors-preferences";
    
    /** The node name of the editor generaloptions node */
    public static final String N_EDITORGENERALOPTIONS = "editors-generaloptions";
    
    /** The node name of the editor preferrededitors node */
    public static final String N_EDITORPREFERREDEDITORS = "editors-preferrededitors";
    
    /** The node name of the workflow preferences node */
    protected static final String N_WORKFLOWPREFERENCES = "workflow-preferences";
    
    /** The node name of the workflow generaloptions node */
    public static final String N_WORKFLOWGENERALOPTIONS = "workflow-generaloptions";

    /** The node name of the workflow defaultsettings node */
    public static final String N_WORKFLOWDEFAULTSETTINGS = "workflow-defaultsettings";
    
    /** The node name of the buttonstyle node */
    public static final String N_BUTTONSTYLE = "buttonstyle";

    /** The node name of the reporttype node */
    public static final String N_REPORTTYPE = "reporttype";
    
    /** The node name of the uploadapplet node */
    public static final String N_UPLOADAPPLET = "uploadapplet";
    
    /** The node name of the locale node */
    public static final String N_LOCALE = "locale";
    
    /** The node name of the project node */
    public static final String N_PROJECT = "project";
    
    /** The node name of the view node */
    public static final String N_WORKPLACEVIEW = "workplaceview";
    
    /** The node name of the fileentries node */
    public static final String N_ENTRIES = "entries";

    /** The node name of the title column node */
    protected static final String N_TITLE = "show-title";

    /** The node name of the type column node */
    protected static final String N_TYPE = "show-type";
    
    /** The node name of the datelastmodified column node */
    protected static final String N_DATELASTMODIFIED = "show-datelastmodified";

    /** The node name of the datecreated column node */
    protected static final String N_DATECREATED = "show-datecreated";
    
    /** The node name of the lockedby column node */
    protected static final String N_LOCKEDBY = "show-lockedby";
    
    /** The node name of the permissions column node */
    protected static final String N_PERMISSIONS = "show-permissions";
    
    /** The node name of the size column node */
    protected static final String N_SIZE = "show-size";
    
    /** The node name of the state column node */
    protected static final String N_STATE = "show-state";
    
    /** The node name of the userlastmodified node */
    protected static final String N_USERLASTMODIFIED = "show-userlastmodified";

    /** The node name of the usercreated node */
    protected static final String N_USERCREATED = "show-usercreated";

    /** The node name of the filecopy node */
    public static final String N_FILECOPY = "filecopy";
    
    /** The node name of the foldercopy node */
    public static final String N_FOLDERCOPY = "foldercopy";
    
    /** The node name of the filedeletion node */
    public static final String N_FILEDELETION = "filedeletion";
    
    /** The node name of the directpublish node */
    public static final String N_DIRECTPUBLISH = "directpublish";
    
    /** The node name of the filecopy node */
    public static final String N_SHOWLOCK = "showlock";
    
    /** The node name of the directeditstyle node */
    public static final String N_DIRECTEDITSTYLE = "directeditstyle";
    
    /** The node name of the editor node */
    public static final String N_EDITOR = "editor";    
   
    /** The node name of the startupfilter node */
    public static final String N_STARTUPFILTER = "startupfilter";    
    
    /** The node name of the showprojects node */
    public static final String N_SHOWPROJECTS = "showprojects";    
    
    /** The node name of the message-accepted node */
    public static final String N_MESSAGEACCEPTED = "message-accepted";    
    
    /** The node name of the message-forwarded node */
    public static final String N_MESSAGEFORWARDED = "message-forwarded";
    
    /** The node name of the message-completed node */
    public static final String N_MESSAGECOMPLETED = "message-completed";    
    
    /** The node name of the informrolemembers node */
    public static final String N_INFORMROLEMEMBERS = "informrolemembers";    
    

    /** The configured workplace manager */
    private CmsWorkplaceManager m_workplaceManager;
    
   
    /**
     * Public constructor, will be called by configuration manager.<p> 
     */
    public CmsWorkplaceConfiguration() {
        setXmlFileName(C_DEFAULT_XML_FILE_NAME);        
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Workplace config     : initialized");
        }               
    } 

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester.Digester)
     */
    public void addXmlDigesterRules(Digester digester) {                                                  
        // add finish rule
        digester.addCallMethod("*/" + N_WORKPLACE, "initializeFinished");          

              
        // creation of the import/export manager        
        digester.addObjectCreate("*/" + N_WORKPLACE, CmsWorkplaceManager.class);                         
        // import/export manager finished
        digester.addSetNext("*/" + N_WORKPLACE, "setWorkplaceManager");
        
        // add default locale rule
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_DEFAULTLOCALE, "setDefaultLocale", 0);
        
        // add default properties on structure setting
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_DEFAULTPROPERTIESONSTRUCTURE, "setDefaultPropertiesOnStructure", 0);
        
        // add default properties on structure setting
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_ENABLEADVANCEDPROPERTYTABS, "setEnableAdvancedPropertyTabs", 0);
        
        // add rules for dialog handlers
        digester.addObjectCreate("*/" + N_WORKPLACE + "/" + N_DIALOGHANDLERS + "/" + N_DIALOGHANDLER, A_CLASS, CmsConfigurationException.class);
        digester.addSetNext("*/" + N_WORKPLACE + "/" + N_DIALOGHANDLERS + "/" + N_DIALOGHANDLER, "addDialogHandler");

        // add rules for editor handler
        digester.addObjectCreate("*/" + N_WORKPLACE + "/" + N_EDITORHANDLER, A_CLASS, CmsConfigurationException.class);
        digester.addSetNext("*/" + N_WORKPLACE + "/" + N_EDITORHANDLER, "setEditorHandler");        

        // add rules for editor action handler
        digester.addObjectCreate("*/" + N_WORKPLACE + "/" + N_EDITORACTION, A_CLASS, CmsConfigurationException.class);
        digester.addSetNext("*/" + N_WORKPLACE + "/" + N_EDITORACTION, "setEditorAction");        
        
        // add rules for the workplace views  
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_VIEWS + "/" + N_VIEW, "addView", 3);        
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_VIEWS + "/" + N_VIEW, 0, A_KEY);
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_VIEWS + "/" + N_VIEW, 1, A_URI);        
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_VIEWS + "/" + N_VIEW, 2, A_ORDER);
        
        // add rules for the workplace export points 
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_EXPORTPOINTS + "/" + N_EXPORTPOINT, "addExportPoint", 2);        
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_EXPORTPOINTS + "/" + N_EXPORTPOINT, 0, A_URI);
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_EXPORTPOINTS + "/" + N_EXPORTPOINT, 1, A_DESTINATION);
        
        // add autolock rule
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_AUTOLOCK, "setAutoLock", 0);
        
        // add user management enabled rule
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_ENABLEUSERMGMT, "setUserManagementEnabled", 0);
        
        // add max file upload size rule
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_MAXUPLOADSIZE, "setFileMaxUploadSize", 0);
        
        // add labeled folders rule
        digester.addCallMethod("*/" + N_WORKPLACE + "/" + N_LABELEDFOLDERS + "/" + N_RESOURCE, "addLabeledFolder", 1);        
        digester.addCallParam("*/" + N_WORKPLACE + "/" + N_LABELEDFOLDERS + "/" + N_RESOURCE, 0, A_URI);
        
        // add explorer type settings
        digester.addObjectCreate("*/" + N_EXPLORERTYPE, CmsExplorerTypeSettings.class);
        digester.addSetNext("*/" + N_EXPLORERTYPE, "addExplorerTypeSetting");
        
        digester.addCallMethod("*/" + N_EXPLORERTYPE, "setTypeAttributes", 3);
        digester.addCallParam("*/" + N_EXPLORERTYPE, 0, A_NAME);
        digester.addCallParam("*/" + N_EXPLORERTYPE, 1, A_KEY);
        digester.addCallParam("*/" + N_EXPLORERTYPE, 2, A_ICON);
        
        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, "setNewResourceUri", 1);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, 0, A_URI);
        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, "setNewResourceOrder", 1);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_NEWRESOURCE, 0, A_ORDER);
        
        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_ACCESSCONTROL + "/" + N_ACCESSENTRY, "addAccessEntry", 2);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_ACCESSCONTROL + "/" + N_ACCESSENTRY, 0, A_PRINCIPAL);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_ACCESSCONTROL + "/" + N_ACCESSENTRY, 1, A_PERMISSIONS);
        
        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_DEFAULTPROPERTIES, "setPropertyDefaults", 2);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_DEFAULTPROPERTIES, 0, A_ENABLED);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_DEFAULTPROPERTIES, 1, A_SHOWNAVIGATION);
        
        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_DEFAULTPROPERTIES + "/" + N_PROPERTY, "addProperty", 1);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_DEFAULTPROPERTIES + "/" + N_PROPERTY, 0, A_NAME);
        
        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_CONTEXTMENU + "/" + N_ENTRY, "addContextMenuEntry", 6);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_CONTEXTMENU + "/" + N_ENTRY, 0, A_KEY);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_CONTEXTMENU + "/" + N_ENTRY, 1, A_URI);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_CONTEXTMENU + "/" + N_ENTRY, 2, A_RULES);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_CONTEXTMENU + "/" + N_ENTRY, 3, A_TARGET);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_CONTEXTMENU + "/" + N_ENTRY, 4, A_ORDER);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_CONTEXTMENU + "/" + N_ENTRY, 5, A_ISXML);
        
        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_CONTEXTMENU + "/" + N_SEPARATOR, "addContextMenuSeparator", 1);
        digester.addCallParam("*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_CONTEXTMENU + "/" + N_SEPARATOR, 0, A_ORDER);
        
        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS + "/" + N_CONTEXTMENU, "createContextMenu");
        digester.addCallMethod("*/" + N_EXPLORERTYPE + "/" + N_EDITOPTIONS, "setIsResourceType");
        
        // creation of the default user settings              
        digester.addObjectCreate("*/" + N_WORKPLACE + "/" +N_USER, CmsDefaultUserSettings.class);  
        digester.addSetNext("*/" + N_WORKPLACE + "/" +N_USER, "setDefaultUserSettings");       

        // add workplace preferences generaloptions rules 
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_WORKPLACEPREFERENCES + "/"+ N_WORKPLACEGENERALOPTIONS + "/" + N_BUTTONSTYLE,
                                "setWorkplaceButtonStyle", 0);
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_WORKPLACEPREFERENCES + "/"+ N_WORKPLACEGENERALOPTIONS + "/" + N_REPORTTYPE,
                                "setWorkplaceReportType", 0);
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_WORKPLACEPREFERENCES + "/"+ N_WORKPLACEGENERALOPTIONS + "/" + N_UPLOADAPPLET,
                                "setUploadApplet", 0); 
        
        // add workplace preferences startupsettings rules 
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_WORKPLACEPREFERENCES + "/"+ N_WORKPLACESTARTUPSETTINGS + "/" + N_LOCALE,
                                "setLocale", 0);
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_WORKPLACEPREFERENCES + "/"+ N_WORKPLACESTARTUPSETTINGS  + "/" + N_PROJECT,
                                "setStartProject", 0);
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_WORKPLACEPREFERENCES + "/"+ N_WORKPLACESTARTUPSETTINGS  + "/" + N_WORKPLACEVIEW,
                                "setStartView", 0); 

        // add explorer preferences generaloptions rules 
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_EXPLORERPREFERENCES + "/"+ N_EXPLORERGENERALOPTIONS + "/" + N_BUTTONSTYLE,
                                "setExplorerButtonStyle", 0);
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_EXPLORERPREFERENCES + "/"+ N_EXPLORERGENERALOPTIONS + "/" + N_ENTRIES,
                "setExplorerFileEntries", 0);
        
        // add explorer display options rules 
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_EXPLORERPREFERENCES + "/"+ N_EXPLORERDISPLAYOPTIONS + "/"  + N_TITLE,
                "setShowExplorerFileTitle", 0);
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_EXPLORERPREFERENCES + "/"+ N_EXPLORERDISPLAYOPTIONS + "/" + N_TYPE, 
                "setShowExplorerFileType", 0);
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_EXPLORERPREFERENCES + "/"+ N_EXPLORERDISPLAYOPTIONS + "/" + N_DATELASTMODIFIED, 
                "setShowExplorerFileDateLastModified", 0);
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_EXPLORERPREFERENCES + "/"+ N_EXPLORERDISPLAYOPTIONS + "/" + N_DATECREATED, 
                "setShowExplorerFileDateCreated", 0);
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_EXPLORERPREFERENCES + "/"+ N_EXPLORERDISPLAYOPTIONS + "/" + N_LOCKEDBY, 
                "setShowExplorerFileLockedBy", 0);
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_EXPLORERPREFERENCES + "/"+ N_EXPLORERDISPLAYOPTIONS + "/" + N_PERMISSIONS, 
                "setShowExplorerFilePermissions", 0);
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_EXPLORERPREFERENCES + "/"+ N_EXPLORERDISPLAYOPTIONS + "/" + N_SIZE, 
                "setShowExplorerFileSize", 0);        
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_EXPLORERPREFERENCES + "/"+ N_EXPLORERDISPLAYOPTIONS + "/" + N_STATE, 
                "setShowExplorerFileState", 0); 
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_EXPLORERPREFERENCES + "/"+ N_EXPLORERDISPLAYOPTIONS + "/" + N_USERLASTMODIFIED, 
                "setShowExplorerFileUserLastModified", 0);
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_EXPLORERPREFERENCES + "/"+ N_EXPLORERDISPLAYOPTIONS + "/" + N_USERCREATED, 
                "setShowExplorerFileUserCreated", 0);

        // add dialog preferences rules
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_DIALOGSPREFERENCES + "/"+ N_DIALOGSDEFAULTSETTINGS + "/"  + N_FILECOPY,
                                "setDialogCopyFileMode", 0);
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_DIALOGSPREFERENCES + "/"+ N_DIALOGSDEFAULTSETTINGS + "/"  + N_FOLDERCOPY,
                "setDialogCopyFolderMode", 0);
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_DIALOGSPREFERENCES + "/"+ N_DIALOGSDEFAULTSETTINGS + "/"  + N_FILEDELETION,
                "setDialogDeleteFileMode", 0);   
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_DIALOGSPREFERENCES + "/"+ N_DIALOGSDEFAULTSETTINGS + "/"  + N_DIRECTPUBLISH,
                "setDialogPublishSiblings", 0);
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_DIALOGSPREFERENCES + "/"+ N_DIALOGSDEFAULTSETTINGS + "/"  + N_SHOWLOCK,
                "setShowLockDialog", 0); 
        
        // add editor generaloptions rules
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_EDITORPREFERENCES + "/"+ N_EDITORGENERALOPTIONS + "/" + N_BUTTONSTYLE,
                                "setEditorButtonStyle", 0);
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_EDITORPREFERENCES + "/"+ N_EDITORGENERALOPTIONS + "/" + N_DIRECTEDITSTYLE,
                "setDirectEditButtonStyle", 0);
        
        // add editor preferrededitor rules
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_EDITORPREFERENCES + "/"+ N_EDITORPREFERREDEDITORS + "/" + N_EDITOR,
                                "setPreferredEditor", 2);
        digester.addCallParam("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_EDITORPREFERENCES + "/"+ N_EDITORPREFERREDEDITORS + "/" + N_EDITOR, 0, A_TYPE);
        digester.addCallParam("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_EDITORPREFERENCES + "/"+ N_EDITORPREFERREDEDITORS + "/" + N_EDITOR, 1, A_VALUE);
        
        // add workflow generaloptions rules
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_WORKFLOWPREFERENCES + "/"+ N_WORKFLOWGENERALOPTIONS + "/" + N_STARTUPFILTER,
                "setTaskStartupFilterDefault", 0);
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_WORKFLOWPREFERENCES + "/"+ N_WORKFLOWGENERALOPTIONS + "/" + N_SHOWPROJECTS,
                "setTaskShowAllProjects", 0);
        
        // add workflow defaultsettings rules
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_WORKFLOWPREFERENCES + "/"+ N_WORKFLOWDEFAULTSETTINGS + "/" + N_MESSAGEACCEPTED,
                "setTaskMessageAccepted", 0);
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_WORKFLOWPREFERENCES + "/"+ N_WORKFLOWDEFAULTSETTINGS + "/" + N_MESSAGEFORWARDED,
                "setTaskMessageForwarded", 0);
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_WORKFLOWPREFERENCES + "/"+ N_WORKFLOWDEFAULTSETTINGS + "/" + N_MESSAGECOMPLETED,
                "setTaskMessageCompleted", 0);
        digester.addCallMethod("*/" + N_WORKPLACE + "/" +N_USER + "/" + N_WORKFLOWPREFERENCES + "/"+ N_WORKFLOWDEFAULTSETTINGS + "/" + N_INFORMROLEMEMBERS,
                "setTaskMessageMembers", 0);        
    }
    
    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#generateXml(org.dom4j.Element)
     */
    public Element generateXml(Element parent) {
        // generate workplace node and subnodes
        Element workplaceElement = parent.addElement(N_WORKPLACE);
        Iterator i;
        
        // add default locale
        workplaceElement.addElement(N_DEFAULTLOCALE)
            .setText(m_workplaceManager.getDefaultLocale().toString());   
        
        // add <dialoghandlers> subnode
        Element dialogElement = workplaceElement.addElement(N_DIALOGHANDLERS);
        Map dialogs = m_workplaceManager.getDialogHandler();
        i = dialogs.keySet().iterator();
        while (i.hasNext()) {          
            String name = (String)i.next();
            dialogElement.addElement(N_DIALOGHANDLER)
                .addAttribute(A_CLASS, dialogs.get(name).getClass().getName());
        }             
        
        // add miscellaneous editor subnodes
        workplaceElement.addElement(N_EDITORHANDLER)
            .addAttribute(A_CLASS, m_workplaceManager.getEditorHandler().getClass().getName());
        workplaceElement.addElement(N_EDITORACTION)
            .addAttribute(A_CLASS, m_workplaceManager.getEditorActionHandler().getClass().getName());
        
        // add <views> subnode
        Element viewsElement = workplaceElement.addElement(N_VIEWS);
        i = m_workplaceManager.getViews().iterator();
        while (i.hasNext()) {
            CmsWorkplaceView view = (CmsWorkplaceView)i.next();
            viewsElement.addElement(N_VIEW)
            .addAttribute(A_KEY, view.getKey())
            .addAttribute(A_URI, view.getUri())
            .addAttribute(A_ORDER, view.getOrder().toString());                
        }            
                
        // add <exportpoints> subnide
        Element resourceloadersElement = workplaceElement.addElement(N_EXPORTPOINTS);
        Set points = m_workplaceManager.getExportPoints();
        i = points.iterator();
        while (i.hasNext()) {          
            CmsExportPoint point = (CmsExportPoint)i.next();
            resourceloadersElement.addElement(N_EXPORTPOINT)
                .addAttribute(A_URI, point.getUri())
                .addAttribute(A_DESTINATION, point.getDestination());
        }     

        // add miscellaneous configuration nodes
        workplaceElement.addElement(N_AUTOLOCK)
            .setText(new Boolean(m_workplaceManager.autoLockResources()).toString());        
        workplaceElement.addElement(N_ENABLEUSERMGMT)
            .setText(new Boolean(m_workplaceManager.showUserGroupIcon()).toString());   
        workplaceElement.addElement(N_MAXUPLOADSIZE)
            .setText(new Integer(m_workplaceManager.getFileMaxUploadSize()).toString());         
        
        // add <labeledfolders> resource list
        Element labeledElement = workplaceElement.addElement(N_LABELEDFOLDERS);        
        i =  m_workplaceManager.getLabelSiteFolders().iterator();
        while (i.hasNext()) {
            labeledElement.addElement(N_RESOURCE).addAttribute(A_URI, (String)i.next());        
        }
        
        // add <explorertypes> node
        Element explorerTypesElement = workplaceElement.addElement(N_EXPLORERTYPES);
        i = m_workplaceManager.getExplorerTypeSettings().iterator();
        while (i.hasNext()) {
            // create an explorer type node
            CmsExplorerTypeSettings settings = (CmsExplorerTypeSettings)i.next();
            Element explorerTypeElement = explorerTypesElement.addElement(N_EXPLORERTYPE);
            explorerTypeElement.addAttribute(A_NAME, settings.getName());
            explorerTypeElement.addAttribute(A_KEY, settings.getKey());
            explorerTypeElement.addAttribute(A_ICON, settings.getIcon());
            // create subnode <newresource>
            Element newResElement = explorerTypeElement.addElement(N_NEWRESOURCE);
            newResElement.addAttribute(A_URI, settings.getNewResourceUri());
            newResElement.addAttribute(A_ORDER, settings.getNewResourceOrder());
            // create subnode <accesscontrol>
            Element accessControlElement = explorerTypeElement.addElement(N_ACCESSCONTROL);
            Iterator k = settings.getAccessEntries().keySet().iterator();
            while (k.hasNext()) {
                String key = (String)k.next();
                String value = (String)settings.getAccessEntries().get(key);
                Element accessEntryElement = accessControlElement.addElement(N_ACCESSENTRY);
                accessEntryElement.addAttribute(A_PRINCIPAL, key);
                accessEntryElement.addAttribute(A_PERMISSIONS, value);
            }
            // create subnode <editoptions>
            if (settings.isResourceType()) {
                Element editOptionsElement = explorerTypeElement.addElement(N_EDITOPTIONS);
                Element defaultPropertiesElement = editOptionsElement.addElement(N_DEFAULTPROPERTIES);
                defaultPropertiesElement.addAttribute(A_ENABLED, "" + settings.isPropertiesEnabled());
                defaultPropertiesElement.addAttribute(A_SHOWNAVIGATION, "" + settings.isShowNavigation());
                Iterator m = settings.getProperties().iterator();
                while (m.hasNext()) {
                    defaultPropertiesElement.addElement(N_PROPERTY).addAttribute(A_NAME, (String)m.next());
                }
                Element contextMenuElement = editOptionsElement.addElement(N_CONTEXTMENU);
                m = settings.getContextMenuEntries().iterator();
                while (m.hasNext()) {
                    CmsExplorerContextMenuItem item = (CmsExplorerContextMenuItem)m.next();
                    Element itemElement;
                    if (CmsExplorerContextMenuItem.C_TYPE_ENTRY.equals(item.getType())) {
                        // create an <entry> node
                        itemElement = contextMenuElement.addElement(N_ENTRY);
                        itemElement.addAttribute(A_KEY, item.getKey());
                        itemElement.addAttribute(A_URI, item.getUri());
                        if (item.isXml()) {
                            itemElement.addAttribute(A_ISXML, "" + item.isXml());
                        }
                        if (item.getTarget() != null) {
                            itemElement.addAttribute(A_TARGET, item.getTarget());
                        }
                        itemElement.addAttribute(A_RULES, item.getRules());
                    } else {
                        // create a <separator> node
                        itemElement = contextMenuElement.addElement(N_SEPARATOR);
                    }
                    itemElement.addAttribute(A_ORDER, "" + item.getOrder());
                }
            }            
        }
        
        // return the configured node
        return workplaceElement;
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdFilename()
     */
    public String getDtdFilename() {
        return C_CONFIGURATION_DTD_NAME;
    }
    
    /**
     * Returns the initialized workplace manager.<p>
     * 
     * @return the initialized workplace manager
     */
    public CmsWorkplaceManager getWorkplaceManager() {
        return m_workplaceManager;
    }
    
    /**
     * Will be called when configuration of this object is finished.<p> 
     */
    public void initializeFinished() {
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Workplace config     : finished");
        }            
    }   
    
    /**
     * Sets the generated workplace manager.<p>
     * 
     * @param manager the workplace manager to set
     */
    public void setWorkplaceManager(CmsWorkplaceManager manager) {
        m_workplaceManager = manager;
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Workplace init       : finished");
        }
    }
    
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/Attic/I_CmsWpConstants.java,v $
 * Date   : $Date: 2004/06/14 11:22:25 $
 * Version: $Revision: 1.6 $
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

package org.opencms.workplace;

import java.util.Locale;

/**
 * Interface defining all constants used in OpenCms
 * workplace classes and elements.
 *
 * @author Alexander Lucas
 * @author Michael Emmerich
 * @version $Revision: 1.6 $ $Date: 2004/06/14 11:22:25 $
 */

public interface I_CmsWpConstants {

    /** Parameter for the default locale */
    Locale C_DEFAULT_LOCALE = Locale.ENGLISH;    
    
    /** Parameter for the default language */
    String C_DEFAULT_LANGUAGE = C_DEFAULT_LOCALE.getLanguage();
    
    /** Parameter for content body folder */
    String C_VFS_PATH_BODIES = "/system/bodies/";
     
    /** Path to system folder */ 
    String C_VFS_PATH_SYSTEM = "/system/"; 
     
    /** Path to modules folder */
    String C_VFS_PATH_MODULES = C_VFS_PATH_SYSTEM + "modules/";
    
    /** Parameter for default module */
    String C_VFS_PATH_DEFAULTMODULE = C_VFS_PATH_MODULES + "default/";
    
    /** Path to module demo folder (deprecated since version 5.0 beta 2) */
    String C_VFS_PATH_MODULEDEMOS = C_VFS_PATH_MODULES + "moduledemos/";

    /** Path to the galleries */
    String C_VFS_PATH_GALLERIES = C_VFS_PATH_SYSTEM + "galleries/";

    /** Path to pics gallery folder */
    String C_VFS_GALLERY_PICS = C_VFS_PATH_GALLERIES + "pics/";

    /** Path to download gallery folder */
    String C_VFS_GALLERY_DOWNLOAD = C_VFS_PATH_GALLERIES + "download/";
 
    /** Path to html gallery folder */
    String C_VFS_GALLERY_HTML = C_VFS_PATH_GALLERIES + "htmlgalleries/";

    /** Path to externallink gallery folder */
    String C_VFS_GALLERY_EXTERNALLINKS = C_VFS_PATH_GALLERIES + "externallinks/";

    /** 
     * Path to the login folder.<p>
     * It's important to note that this does NOT end with a / 
     * as most other path constants do!<p>
     */
    String C_VFS_PATH_LOGIN = C_VFS_PATH_SYSTEM + "login";
    
    /** Path to the workplace */
    String C_VFS_PATH_WORKPLACE = C_VFS_PATH_SYSTEM + "workplace/";

    /** Path to system image folder */
    String C_VFS_PATH_SYSTEMPICS = C_VFS_PATH_WORKPLACE + "resources/";

    /** Path to exported system image folder */
    String C_SYSTEM_PICS_EXPORT_PATH = "/resources/";

    /** Path to locales */
    String C_VFS_PATH_LOCALES = C_VFS_PATH_WORKPLACE + "locales/";

    /** Path to scripts */
    String C_VFS_PATH_SCRIPTS = C_VFS_PATH_WORKPLACE + "scripts/";

    /** Path to online help pages */
    String C_VFS_PATH_HELP = C_VFS_PATH_WORKPLACE + "help/";

    /** Directory name of content templates folder */
    String C_VFS_DIR_TEMPLATES = "templates/";

    /** Directory name of content internal folder */
    String C_VFS_DIR_INTERNAL = "internal/";

    /** Directory name of content default_bodies folder */
    String C_VFS_DIR_DEFAULTBODIES = "default_bodies/";

    /** Path to content templates folder */
    String C_VFS_PATH_DEFAULT_TEMPLATES = C_VFS_PATH_DEFAULTMODULE + C_VFS_DIR_TEMPLATES;

    /** Path to content internal folder */
    String C_VFS_PATH_DEFAULT_INTERNAL = C_VFS_PATH_WORKPLACE + "templates/";

    /** Path to content default_bodies folder */
    String C_VFS_PATH_DEFAULT_BODIES = C_VFS_PATH_DEFAULTMODULE + C_VFS_DIR_DEFAULTBODIES;

    /** Directory name of module locales path */
    String C_VFS_DIR_LOCALES = "locales/";

    /** Parameter for layout  */
    String C_PARA_LAYOUT = "default_body";
    
    // Parameters that are used in html requests

    /** Parameter for foldername  */
    String C_PARA_FOLDER = "folder";


    /** Parameter for foldertree  */
    String C_PARA_FOLDERTREE = "foldertree";


    /** Parameter for filelist  */
    String C_PARA_FILELIST = "filelist";


    /** Parameter for url */
    String C_PARA_URL = "URL";


    /** Parameter for previous filelist  */
    String C_PARA_PREVIOUSLIST = "previous";


    /** Parameter for viewfile  */
    String C_PARA_VIEWFILE = "viewfile";


    /** Parameter for starttaskid  */
    String C_PARA_STARTTASKID = "startTaskId";


    /** Parameter for startprojectid  */
    String C_PARA_STARTPROJECTID = "startProjectId";


    /** Parameter for view name */
    String C_PARA_VIEW = "view";


    /** Parameter for page number */
    String C_PARA_PAGE = "page";


    /** Parameter for filter */
    String C_PARA_FILTER = "filter";


    /** Parameter for maximum pages */
    String C_PARA_MAXPAGE = "maxpage";


    /** Parameter for locking pages */
    String C_PARA_LOCK = "lock";


    /** Parameter for unlocking pages */
    String C_PARA_UNLOCK = "unlock";


    /** Parameter for a filename */
    String C_PARA_RESOURCE = "resource";


    /** Parameter for a link */
    String C_PARA_LINK = "link";


    /** Parameter for a filecontent */
    String C_PARA_FILECONTENT = "filecontent";


    /** Parameter for a deleting a file */
    String C_PARA_DELETE = "delete";


    /** Parameter for a name*/
    String C_PARA_NAME = "name";


    /** Parameter for a new file*/
    String C_PARA_NEWFILE = "newfile";


    /** Parameter for a new folder*/
    String C_PARA_NEWFOLDER = "newfolder";


    /** Parameter for a flag */
    String C_PARA_FLAGS = "flags";


    /** Parameter for a title */
    String C_PARA_TITLE = "title";


    /** Parameter for a template */
    String C_PARA_TEMPLATE = "template";


    /** Parameter for a navigation title */
    String C_PARA_NAVTEXT = "navtitle";


    /** Parameter for a navigation position */
    String C_PARA_NAVPOS = "navpos";


    /** Parameter for a new owner*/
    String C_PARA_NEWOWNER = "newowner";


    /** Parameter for a new group*/
    String C_PARA_NEWGROUP = "newgroup";


    /** Parameter for a new accessflags*/
    String C_PARA_NEWACCESS = "newaccess";


    /** Parameter for a new type*/
    String C_PARA_NEWTYPE = "newtype";


    /** Parameter for a formname*/
    String C_PARA_FORMNAME = "formname";


    /** Parameter for a variable*/
    String C_PARA_VARIABLE = "variable";


    /** Parameter for a project*/
    String C_PARA_PROJECT = "project";


    /** Parameter for a panel*/
    String C_PARA_PANEL = "panel";


    /** Parameter for the previous panel*/
    String C_PARA_OLDPANEL = "oldpanel";


    /** Parameter for the old password */
    String C_PARA_OLDPWD = "oldpwd";


    /** Parameter for the new password */
    String C_PARA_NEWPWD = "newpwd";


    /** Parameter for the password repead */
    String C_PARA_NEWPWDREPEAT = "newpwdrepeat";


    /** Parameter for submitting data */
    String C_PARA_SUBMIT = "SUBMIT";


    /** Parameter for the ok value */
    String C_PARA_OK = "OK";
    
    /** Parameter name of "cancel" value */
    String C_PARA_CANCEL = "CANCEL";  

    /** Parameter for the default value */
    String C_PARA_DEFAULT = "DEFAULT";


    /** Parameter for the explorersettings value */
    String C_PARA_EXPLORERSETTINGS = "EXPLORERSETTINGS";


    /** Parameter for the tasksettings value */
    String C_PARA_TASKSETTINGS = "TASKSETTINGS";


    /** Parameter for the startsettings value */
    String C_PARA_STARTSETTINGS = "STARTSETTINGS";


    /** Parameter for the usersettings value */
    String C_PARA_USERSETTINGS = "USERSETTINGS";


    /** Parameter for text/html editor content */
    String C_PARA_CONTENT = "content";


    /** Parameter for action commands */
    String C_PARA_ACTION = "action";


    /** Parameter for java script filenames */
    String C_PARA_JSFILE = "jsfile";


    /** Parameter for properties */
    String C_PARA_PROPERTYDEF = "property";


    /** Parameter for initial load */
    String C_PARA_INITIAL = "initial";


     /** Parameter for keywords */
    String C_PARA_KEYWORDS = "keywords";


     /** Parameter for description */
    String C_PARA_DESCRIPTION = "description";

    // Filenames of workplace files

    /** The filename to the icontemplate */
    String C_ICON_TEMPLATEFILE = "icontemplate";


    /** The filename to the backbuttontemplate */
    String C_ADMIN_BACK_BUTTON = "adminbackbuttontemplate";


    /** The filename to the projectlisttemplate */
    String C_PROJECTLIST_TEMPLATEFILE = "projecttemplate";


    /** The filename to the modulelisttemplate */
    String C_MODULELIST_TEMPLATEFILE = "moduletemplate";
    
    /** The property files of the modulelisttemplate */
    String C_SESSION_MODULE_PROPFILES = "modulepropfiles";

    /** The missing files of the modulelisttemplate */
    String C_SESSION_MODULE_MISSFILES = "modulemissfiles";
    
    /** The modules checksum */
    String C_SESSION_MODULE_CHECKSUM = "modulechecksum";
    
    /** The "module in use" flag */
    String C_SESSION_MODULE_INUSE = "moduleinuse";

    /** The exclusions of the modulelisttemplate */
    String C_SESSION_MODULE_EXCLUSION = "moduleexclusion";
    
    /** The modules project files */
    String C_SESSION_MODULE_PROJECTFILES = "moduleprojectfiles";
    
    /** The step of module deletion */
    String C_SESSION_MODULE_DELETE_STEP = "moduledeletestep";
    
    /** The modules administration data */
    String C_SESSION_MODULE_ADMIN_DATA = "module_admin_data";

    /** The modules administration property names */
    String C_SESSION_MODULE_ADMIN_PROP_NAMES = "module_admin_props_names";
    
    /** The modules administration property description */
    String C_SESSION_MODULE_ADMIN_PROP_DESCR = "module_admin_props_desr";

    /** The modules administration property type */
    String C_SESSION_MODULE_ADMIN_PROP_TYP = "module_admin_props_typ";
    
    /** the modules administration property value */
    String C_SESSION_MODULE_ADMIN_PROP_VAL = "module_admin_props_value";

    /** the modules packetname key */
    String C_MODULE_PACKETNAME = "packetname";

    /** The filename to the filetypelist template */
    String C_FILETYPELIST_TEMPLATEFILE = "filetypelisttemplate";


    /** The filename to the taskdocu template */
    String C_TASKDOCU_TEMPLATEFILE = "tasklistdoctemplate";


    /** The filename to the projectlisttemplate */
    String C_TASKLIST_TEMPLATEFILE = "tasklisttemplate";


    /** The filename to the projectlisttemplate */
    String C_CONTEXTMENUE_TEMPLATEFILE = "contexttemplate";


    /** The filename to the prefs scroller template file */
    String C_PREFSSCROLLER_TEMPLATEFILE = "prefsscrollerTemplate";


    /** The explorer tree. */
    String C_WP_EXPLORER_TREE = "../action/explorer_tree.html";


    /** The explorer file list. */
    String C_WP_EXPLORER_FILELIST = "../action/explorer_files.html";


    /** The folder tree. */
    String C_WP_FOLDER_TREE = "../action/folder_tree.html";

    /** The channel tree. */
    String C_WP_CHANNEL_TREE = "../action/channel_tree.html";

    /** The workplace */
    String C_WP_RELOAD = "../action/workplace_reload.html";


    /** The preferences update */
    String C_PREFERENCES_UPDATE = "../action/preferences_update.html";


    /** The preferences user panel */
    String C_WP_EXPLORER_PREFERENCES = "../action/preferences.html?panel=user";


    // Filenames of special templates

    /** Name of the template containing button definitions */
    String C_BUTTONTEMPLATE = "ButtonTemplate";


    /** Name of the template containing label definitions */
    String C_LABELTEMPLATE = "labelTemplate";


    /** Name of the template containing panel bar definitions */
    String C_PANELTEMPLATE = "panelTemplate";


    /**
     *  Name of the template containing input field definitions
     */
    String C_INPUTTEMPLATE = "inputTemplate";


    /**
     *  Name of the template containing error field definitions
     */
    String C_ERRORTEMPLATE = "errorTemplate";


    /**
     *  Name of the template containing messagebox definitions
     */
    String C_BOXTEMPLATE = "messageboxTemplate";


    /**
     *  Name of the template containing radiobutton definitions
     */
    String C_RADIOTEMPLATE = "radioTemplate";


    // Constants for editors

    /** Save action */
    String C_EDIT_ACTION_SAVE = "save";


    /** Save &amp; Exit action */
    String C_EDIT_ACTION_SAVEEXIT = "saveexit";


    /** Exit action */
    String C_EDIT_ACTION_EXIT = "exit";


    // tag defnitions

    /** Name of the label tag in the label definition template */
    String C_TAG_PROJECTLIST_DEFAULT = "defaultprojectlist";


    /** Name of the label tag in the label definition template */
    String C_TAG_MODULELIST_DEFAULT = "defaultmodulelist";


    /** Name of the label tag in the label definition template */
    String C_TAG_PROJECTLIST_SNAPLOCK = "snaplock";


    /** Name of the label tag in the label definition template */
    String C_TAG_LABEL = "label";


    /** Name of the label tag in the input definiton template */
    String C_TAG_INPUTFIELD = "inputfield";


    /** Name of the password tag in the input definiton template */
    String C_TAG_PASSWORD = "password";


    /** Name of the startup tag in the input definiton template */
    String C_TAG_STARTUP = "STARTUP";


    /** Name of the submitbutton tag in the button definiton template */
    String C_TAG_SUBMITBUTTON = "submitbutton";


    /** Name of the errorbox tag in the error definiton template */
    String C_TAG_ERRORBOX = "errorbox";


    /** Name of the errorbox tag in the error definiton template */
    String C_TAG_MESSAGEBOX = "messagepage";


    /** Name of the select start tag in the input definiton template */
    String C_TAG_SELECTBOX_START = "selectbox.start";


    /** Name of the select div start tag in the input definiton template */
    String C_TAG_SELECTBOX_START_DIV = "selectbox.startdiv";


    /** Name of the select end tag in the input definiton template */
    String C_TAG_SELECTBOX_END = "selectbox.end";


    /** Name of the selectbox "class" option tag in the input definiton template */
    String C_TAG_SELECTBOX_CLASS = "selectbox.class";


    /** Name of the (select) option tag in the input definiton template */
    String C_TAG_SELECTBOX_OPTION = "selectbox.option";


    /** Name of the (select) selected option tag in the input definiton template */
    String C_TAG_SELECTBOX_SELOPTION = "selectbox.seloption";

    
    /** Name of the selectbox "width" option tag in the input definiton template */
    String C_TAG_SELECTBOX_WIDTH = "selectbox.width";
    
    
    /** Name of the error?page tag in the error definiton template */
    String C_TAG_ERRORPAGE = "errorpagedefinition";


    /** Panel bar starting sequence tag in the panel bar definiton template */
    String C_TAG_PANEL_STARTSEQ = "paneldef.startseq";


    /** Panel bar ending sequence tag in the panel bar definiton template */
    String C_TAG_PANEL_ENDSEQ = "paneldef.endseq";


    /** Panel bar sequence for separating background and text area in the panel bar definiton template */
    String C_TAG_PANEL_SEPBGTEXT = "paneldef.sepbgtext";


    /** Panel bar sequence for active background in the panel bar definiton template */
    String C_TAG_PANEL_BGACTIVE = "paneldef.bgactive";


    /** Panel bar sequence for inactive background in the panel bar definiton template */
    String C_TAG_PANEL_BGINACTIVE = "paneldef.bginactive";


    /** Panel bar sequence for active text in the panel bar definiton template */
    String C_TAG_PANEL_TEXTACTIVE = "paneldef.textactive";


    /** Panel bar sequence for inactive text in the panel bar definiton template */
    String C_TAG_PANEL_TEXTINACTIVE = "paneldef.textinactive";


    // Parameters for buttons

    /** Name of the button */
    String C_BUTTON_NAME = "name";


    /** Action for the button */
    String C_BUTTON_ACTION = "action";


    /** Alt text of the button */
    String C_BUTTON_ALT = "alt";


    /** href text of the button */
    String C_BUTTON_HREF = "href";


    /** Value of the button */
    String C_BUTTON_VALUE = "value";


    /** Style of the button */
    String C_BUTTON_STYLE = "class";


    /** width of the button */
    String C_BUTTON_WIDTH = "width";


    /** method that should be used for deciding to (de)activate the button */
    String C_BUTTON_METHOD = "method";


    // Parameters for icons

    /** Name of the icon */
    String C_ICON_NAME = "name";


    /** Action for the icon */
    String C_ICON_ACTION = "action";


    /** Label of the icon */
    String C_ICON_LABEL = "label";


    /** href text of the icon */
    String C_ICON_HREF = "href";


    /** href target of the icon */
    String C_ICON_TARGET = "target";


    /** method that should be used for deciding to (de)activate the icon */
    String C_ICON_ACTIVE_METHOD = "activemethod";


    /** method that should be used for deciding whether the icon is visible */
    String C_ICON_VISIBLE_METHOD = "visiblemethod";


    // Parameters for labels

    /** Name of the value */
    String C_LABEL_VALUE = "value";


    // Parameters for radiobuttons

    /** Name of the radio buttons */
    String C_RADIO_RADIONAME = "radioname";


    /** Name of the radio button value */
    String C_RADIO_NAME = "name";


    /** Name of the radio button link */
    String C_RADIO_LINK = "link";


    /** Name of the radio button image name */
    String C_RADIO_IMAGENAME = "image";


    /** Stylesheet class string of the radio button */
    String C_RADIO_CLASS = "class";

    
    /** Stylesheet class name of the radio button */
    String C_RADIO_CLASSNAME = "classname";
    

    /** Datablock conatining the image option*/
    String C_RADIO_IMAGEOPTION = "optionalimage";


    /** Datablock conatining the optional entry for the image*/
    String C_RADIO_IMAGEENTRY = "imageentry";


    /** Datablock conatining the "checked" option*/
    String C_RADIO_SELECTEDOPTION = "optionalselected";


    /** Datablock conatining the optional entry for the "checked" option */
    String C_RADIO_SELECTEDENTRY = "selectedentry";


    /** Method of the radio buttons */
    String C_RADIO_METHOD = "method";


    /** Name of the radio ordering information */
    String C_RADIO_ORDER = "order";


    /** Name of the radion column entry tag in the input definiton template */
    String C_TAG_RADIO_COLENTRY = "radiobuttons.colentry";


    /** Name of the radion row entry tag in the input definiton template */
    String C_TAG_RADIO_ROWENTRY = "radiobuttons.rowentry";


    /** Name of the radio "class" option tag in the input definiton template */
    String C_TAG_RADIO_CLASS = "radiobuttons.class";


    // Parameters for input fields

    /** Name of the input field */
    String C_INPUT_NAME = "name";


    /** Style class of the input field  */
    String C_INPUT_CLASS = "class";


    /**  Size of the input field  */
    String C_INPUT_SIZE = "size";


    /**  Length of the input field  */
    String C_INPUT_LENGTH = "length";


    /**  Value of the input field  */
    String C_INPUT_VALUE = "value";


    /**  Method of the input field  */
    String C_INPUT_METHOD = "method";


    /**  Action of the input field  */
    String C_INPUT_ACTION = "action";


    /**  Method of the projectlist field  */
    String C_PROJECTLIST_METHOD = "method";


    /**  Method of the projectlist field  */
    String C_PROJECTLIST_IDX = "idx";


    /**  Method of the projectlist field  */
    String C_PROJECTLIST_MENU = "menu";


    /**  Method of the projectlist field  */
    String C_PROJECTLIST_LOCKSTATE = "lockstate";


    /**  Method of the projectlist field  */
    String C_PROJECTLIST_NAME = "name";


    /**  Method of the projectlist field  */
    String C_PROJECTLIST_NAME_ESCAPED = "name_escaped";


    /**  Method of the projectlist field  */
    String C_PROJECTLIST_PROJECTID = "id";


    /**  Method of the projectlist field  */
    String C_PROJECTLIST_DESCRIPTION = "description";


    /**  Method of the projectlist field  */
    String C_PROJECTLIST_STATE = "STATE";


    /**  Method of the projectlist field  */
    String C_PROJECTLIST_STATE_LOCKED = "project.state.FILESLOCKED";


    /**  Method of the projectlist field  */
    String C_PROJECTLIST_STATE_UNLOCKED = "project.state.FILESUNLOCKED";


    /**  Method of the projectlist field  */
    String C_PROJECTLIST_PROJECTMANAGER = "projectmanager";


    /**  Method of the projectlist field  */
    String C_PROJECTLIST_PROJECTWORKER = "projectworker";


    /**  Method of the projectlist field  */
    String C_PROJECTLIST_DATECREATED = "datecreated";


    /**  Method of the projectlist field  */
    String C_PROJECTLIST_OWNER = "owner";


    /**  Method of the modulelist field  */
    String C_MODULELIST_METHOD = "method";


    /**  Method of the modulelist field  */
    String C_MODULELIST_IDX = "idx";


    /**  Method of the modulelist field  */
    String C_MODULELIST_NAME = "name";


    /**  Method of the modulelist field  */
    String C_MODULELIST_NICE_NAME = "nicename";


    /**  Method of the modulelist field  */
    String C_MODULELIST_VERSION = "version";


    /**  Method of the modulelist field  */
    String C_MODULELIST_AUTHOR = "author";


    /**  Method of the modulelist field  */
    String C_MODULELIST_DATECREATED = "datecreated";


    /**  Method of the modulelist field  */
    String C_MODULELIST_DATEUPLOADED = "dateuploaded";


    // Parameters for error boxes and error pages

    /** Title of the error box */
    String C_ERROR_TITLE = "title";


    /** Message of the error box */
    String C_ERROR_MESSAGE = "message";


    /** Reason of the error box */
    String C_ERROR_REASON = "reason";


    /** Suggestion of the error box */
    String C_ERROR_SUGGESTION = "suggestion";


    /** Link of the error box */
    String C_ERROR_LINK = "ref";


    /** Static text in the error box */
    String C_ERROR_MSG_REASON = "msgreason";


    /** Button label of the error box */
    String C_ERROR_MSG_BUTTON = "msgbutton";


    /** Button label of the error box */
    String C_ERROR_MSG_DETAILS = "details";


    // Parameters for error boxes and error pages

    /** Title of the messagebox */
    String C_MESSAGE_TITLE = "title";


    /** First message of the messagebox */
    String C_MESSAGE_MESSAGE1 = "message1";


    /** Second message of the messagebox */
    String C_MESSAGE_MESSAGE2 = "message2";


    /** First button of the messagebox */
    String C_MESSAGE_BUTTON1 = "button1";


    /** Second button of the messagebox */
    String C_MESSAGE_BUTTON2 = "button2";


    /** Link on button1 of the messagebox */
    String C_MESSAGE_LINK1 = "link1";


    /** Link on button2 of the messagebox */
    String C_MESSAGE_LINK2 = "link2";


    /** Link on button2 of the messagebox */
    String C_MESSAGE_FILENAME = "filename";


    // Parameters for select boxes

    /** Name of the select box */
    String C_SELECTBOX_NAME = "name";


    /** Size of the select box */
    String C_SELECTBOX_SIZE = "size";


    /** Div flag of the select box */
    String C_SELECTBOX_DIV = "div";


    /** Stylesheet class string of the select box */
    String C_SELECTBOX_CLASS = "class";


    /** Stylesheet class name of the select box */
    String C_SELECTBOX_CLASSNAME = "classname";


    /** Width of the select box */
    String C_SELECTBOX_WIDTH = "width";

    
    /** Stylesheet class name of the select box */
    String C_SELECTBOX_WIDTHNAME = "widthname";
    

    /** Onchange of the select box */
    String C_SELECTBOX_ONCHANGE = "onchange";


    /** Method of the select box */
    String C_SELECTBOX_METHOD = "method";


    /** option name of the select box */
    String C_SELECTBOX_OPTIONNAME = "name";


    /** option value of the select box */
    String C_SELECTBOX_OPTIONVALUE = "value";


    /** option values for font select boxes */
    String[] C_SELECTBOX_FONTS =  {
        "Arial", "Arial Narrow", "System", "Times New Roman", "Verdana", "Monospace", "SansSerif"
    };

    /** option values for editor view select boxes */
    String[] C_SELECTBOX_EDITORVIEWS =  {
        "edithtml", "edit"
    };
    
    /** values for editor view select boxes */
    int[] C_SELECTBOX_EDITORVIEWS_ALLOWED =  {
        3, 2
    };


    /** classes of the different option values for editor view select boxes */
    String[] C_SELECTBOX_EDITORVIEWS_CLASSES =  {
        "com.opencms.workplace.CmsEditor", "com.opencms.workplace.CmsEditor"
    };


    /** templates of the different option values for editor view select boxes */
    String[] C_SELECTBOX_EDITORVIEWS_TEMPLATES =  {
        "edit_html_main", "edit_text_main"
    };


    /** default selected option value for editor view select boxes */
    int[] C_SELECTBOX_EDITORVIEWS_DEFAULT =  {
        0, 1
    };


    // Parameters for panel bars

    /** Link URL for each panel */
    String C_PANEL_LINK = "link";


    /** Text for each panel */
    String C_PANEL_NAME = "panelname";


    /** Panel defintion for explorer settings */
    String C_PANEL_EXPLORER = "explorer";


    /** Panel defintion for task settings */
    String C_PANEL_TASK = "task";


    /** Panel defintion for start settings */
    String C_PANEL_START = "start";


    /** Panel defintion for user settings */
    String C_PANEL_USER = "user";


    // Parameters for file list

    /** method value of the file list */
    String C_FILELIST_METHOD = "method";


    /** method value of the file list */
    String C_FILELIST_DISPLAYMETHOD = "namedisplaymethod";


    /** template value for the file list */
    String C_FILELIST_TEMPLATE = "template";


    /** customized template value for the file list */
    String C_FILELIST_CUSTOMTEMPLATE = "customtemplate";


    /** The name column*/
    String C_FILELIST_COLUMN_NAME = "COLUMN_NAME";

    /** The title column*/
    String C_FILELIST_COLUMN_TITLE = "COLUMN_TITLE";

    /** The type column*/
    String C_FILELIST_COLUMN_TYPE = "COLUMN_TYPE";

    /** The changed column*/
    String C_FILELIST_COLUMN_CHANGED = "COLUMN_CHANGED";

    /** The size column*/
    String C_FILELIST_COLUMN_SIZE = "COLUMN_SIZE";

    /** The state column*/
    String C_FILELIST_COLUMN_STATE = "COLUMN_STATE";

    /** The owner column*/
    String C_FILELIST_COLUMN_OWNER = "COLUMN_OWNER";

    /** The group column*/
    String C_FILELIST_COLUMN_GROUP = "COLUMN_GROUP";

    /** The access column*/
    String C_FILELIST_COLUMN_ACCESS = "COLUMN_ACCESS";

    /** The locked column*/
    String C_FILELIST_COLUMN_LOCKED = "COLUMN_LOCKED";

    /** The customizable column*/
    String C_FILELIST_COLUMN_CUSTOMIZED = "COLUMN_CUSTOMIZED";


    /** The stylesheet class to be used for a file or folder entry */
    String C_FILELIST_CLASS_VALUE = "OUTPUT_CLASS";


    /** The customizable value column*/
    String C_FILELIST_COLUMN_CUSTOMIZED_VALUE = "COLUMN_CUSTOMIZED_VALUE";


    /** The link for a file or folder entry */
    String C_FILELIST_LINK_VALUE = "LINK_VALUE";


    /** The suffix for file list values */
    String C_FILELIST_SUFFIX_VALUE = "_VALUE";


    /** The lock value column */
    String C_FILELIST_LOCK_VALUE = "LOCK_VALUE";


    /** The name value column */
    String C_FILELIST_NAME_VALUE = "NAME_VALUE";


    /** The title value column */
    String C_FILELIST_TITLE_VALUE = "TITLE_VALUE";


    /** The icon value column */
    String C_FILELIST_ICON_VALUE = "ICON_VALUE";


    /** The type value column */
    String C_FILELIST_TYPE_VALUE = "TYPE_VALUE";


    /** The changed value column */
    String C_FILELIST_CHANGED_VALUE = "CHANGED_VALUE";


    /** The size value column */
    String C_FILELIST_SIZE_VALUE = "SIZE_VALUE";


    /** The state value column */
    String C_FILELIST_STATE_VALUE = "STATE_VALUE";


    /** The owner value column */
    String C_FILELIST_OWNER_VALUE = "OWNER_VALUE";


    /** The group value column */
    String C_FILELIST_GROUP_VALUE = "GROUP_VALUE";


    /** The access value column */
    String C_FILELIST_ACCESS_VALUE = "ACCESS_VALUE";


    /** The lockedby value column */
    String C_FILELIST_LOCKED_VALUE = "LOCKED_VALUE";


    // Constants for language file control

    /** Prefix for button texts in the language file */
    String C_LANG_BUTTON = "button";


    /** Prefix for button texts in the language file */
    String C_LANG_ICON = "icon";


    /** Prefix for label texts in the language file */
    String C_LANG_LABEL = "label";


    /** Prefix for label texts in the language file */
    String C_LANG_TITLE = "title";


    // Constants for user default preferences

    /** Number of images to be shown per page in the picture browser */
    int C_PICBROWSER_MAXIMAGES = 15;


    /** Number of images to be shown per page in the download browser */
    int C_DOWNBROWSER_MAXENTRIES = 30;


    /** Name of the filelist preferences */
    String C_USERPREF_FILELIST = "filelist";

    // The order of the filelist columns has changed in the JSP workplace,
    // but the old variable values are kept so that the XML workplace still works 
    /** Flag for displaying the name column */
    int C_FILELIST_NAME = 512;
    
    /** Flag for displaying the title column */
    int C_FILELIST_TITLE = 1;

    /** Flag for displaying the filetype column */
    int C_FILELIST_TYPE = 2;

    /** Flag for displaying the size column */
    int C_FILELIST_SIZE = 8;
    
    /** Flag for displaying the access column */
    int C_FILELIST_PERMISSIONS = 128;
        
    /** Flag for displaying the changed column */
    int C_FILELIST_DATE_LASTMODIFIED = 4;
    
    /** Flag for displaying the user who last modified column */
    int C_FILELIST_USER_LASTMODIFIED = 2048;

    /** Flag for displaying the date created column */
    int C_FILELIST_DATE_CREATED = 1024;   
    
    /** Flag for displaying the date released column */
    int C_FILELIST_DATE_RELEASED = 4096;   
    
    /** Flag for displaying the date expired column */
    int C_FILELIST_DATE_EXPIRED = 8192; 
    
    /** Flag for displaying the owner column */
    int C_FILELIST_USER_CREATED = 32; 

    /** Flag for displaying the state column */
    int C_FILELIST_STATE = 16;

    /** Flag for displaying the locked column */
    int C_FILELIST_LOCKEDBY = 256;
    
    // not longer used in the JSP workplace 
    /** Flag for displaying the group column */
    int C_FILELIST_GROUP = 64;


    /** Parameter of user management */
    String C_USERS_NAME = "USERNAME";


    /** Parameter of user management */
    String C_GROUPS_NAME = "GROUPNAME";


    /** Parameter of projectnew */
    String C_PROJECTNEW_NAME = "NAME";


    /** Parameter of projectnew */
    String C_PROJECTNEW_GROUP = "GROUP";


    /** Parameter of projectnew */
    String C_PROJECTNEW_DESCRIPTION = "DESCRIPTION";


    /** Parameter of projectnew */
    String C_PROJECTNEW_MANAGERGROUP = "MANAGERGROUP";


    /** Parameter of projectnew */
    String C_PROJECTNEW_TYPE = "TYPE";


    /** Parameter of projectnew */
    String C_PROJECTNEW_FOLDER = "selectallfolders";


    /** Templateselector of projectnew */
    String C_PROJECTNEW_ERROR = "error";


    /** Templateselector of projectnew */
    String C_PROJECTNEW_DONE = "done";


    /** Taskparameter */
    String C_TASKPARA_ACCEPTATION = "acceptation";


    /** Taskparameter */
    String C_TASKPARA_ALL = "all";


    /** Taskparameter */
    String C_TASKPARA_COMPLETION = "completion";


    /** Taskparameter */
    String C_TASKPARA_DELIVERY = "delivery";


    /** Constant for session-key */
    String C_SESSION_TASK_ALLPROJECTS = "task_allprojects";


    /** Constant for session-key */
    String C_SESSION_TASK_FILTER = "task_filter";


    /** Constant for task-log */
    int C_TASKLOGTYPE_CREATED = 100;


    /** Constant for task-log */
    int C_TASKLOGTYPE_ACCEPTED = 101;


    /** Constant for task-log */
    int C_TASKLOGTYPE_COMMENT = 102;


    /** Constant for task-log */
    int C_TASKLOGTYPE_TAKE = 103;


    /** Constant for task-log */
    int C_TASKLOGTYPE_OK = 104;


    /** Constant for task-log */
    int C_TASKLOGTYPE_REACTIVATED = 105;


    /** Constant for task-log */
    int C_TASKLOGTYPE_FORWARDED = 106;


    /** Constant for task-log */
    int C_TASKLOGTYPE_CALL = 107;


    /** Constant for task-log */
    int C_TASKLOGTYPE_DUECHANGED = 108;


    /** Constant for task-log */
    int C_TASKLOGTYPE_PRIORITYCHANGED = 109;


    /** The prefix for the icon images */
    String C_ICON_PREFIX = "ic_file_";


    /** The extension for the icon images */
    String C_ICON_EXTENSION = ".gif";


    /** The default icon */
    String C_ICON_DEFAULT = "ic_file_othertype.gif";
    
    
    // Constants for macros
    
    /** Context macro */    
    String C_MACRO_OPENCMS_CONTEXT = "${OpenCmsContext}";

    // Contants for explorer filelist
    
    /** Layoutstyle for resources after release date and before expire date */
    int C_LAYOUTSTYLE_INRANGE = 0;
    
    /** Layoutstyle for resources before release date  */
    int C_LAYOUTSTYLE_BEFORERELEASE = 1;
    
    /** Layoutstyle for resources after expire date  */
    int C_LAYOUTSTYLE_AFTEREXPIRE = 2;

}

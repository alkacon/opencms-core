/*
* File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/workplace/Attic/CmsWorkplaceDefault.java,v $
* Date   : $Date: 2005/06/27 23:22:07 $
* Version: $Revision: 1.5 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


package com.opencms.workplace;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.importexport.CmsCompatibleCheck;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.editors.CmsDefaultPageEditor;
import org.opencms.workplace.explorer.CmsTree;

import com.opencms.core.I_CmsConstants;
import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsLegacyException;
import com.opencms.legacy.CmsXmlTemplateLoader;
import com.opencms.template.A_CmsXmlContent;
import com.opencms.template.CmsCacheDirectives;
import com.opencms.template.CmsXmlTemplate;
import com.opencms.template.CmsXmlTemplateFile;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Common template class for displaying OpenCms workplace screens.
 * <P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * <P>
 * Most special workplace classes may extend this class.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.5 $ $Date: 2005/06/27 23:22:07 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public class CmsWorkplaceDefault extends CmsXmlTemplate implements I_CmsConstants {

    /** URL of the pics folder in the webserver's docroot */
    private static String m_resourcesUri = null;

    /** URL of the pics folder in the webserver's docroot */
    private static String m_scriptsUri = null;

    /** Reference to the config file */
    private CmsXmlWpConfigFile m_configFile = null;


    /** classes of the different option values for editor view select boxes. */
    protected String[] C_SELECTBOX_EDITORVIEWS_CLASSES =  {
        "com.opencms.workplace.CmsEditor", "com.opencms.workplace.CmsEditor"
    };

    /** The filename to the backbuttontemplate. */
    public static final String C_ADMIN_BACK_BUTTON = "adminbackbuttontemplate";

    /** Name of the template containing messagebox definitions. */
    public static final String C_BOXTEMPLATE = "messageboxTemplate";

    /** Action for the button. */
    public static final String C_BUTTON_ACTION = "action";

    /** Alt text of the button. */
    public static final String C_BUTTON_ALT = "alt";

    /** href text of the button. */
    public static final String C_BUTTON_HREF = "href";

    /** method that should be used for deciding to (de)activate the button. */
    public static final String C_BUTTON_METHOD = "method";

    /** Name of the button. */
    public static final String C_BUTTON_NAME = "name";

    /** Style of the button. */
    public static final String C_BUTTON_STYLE = "class";

    /** Value of the button. */
    public static final String C_BUTTON_VALUE = "value";

    /** width of the button. */
    public static final String C_BUTTON_WIDTH = "width";

    /** Name of the template containing button definitions. */
    public static final String C_BUTTONTEMPLATE = "ButtonTemplate";

    /** The filename to the projectlisttemplate. */
    public static final String C_CONTEXTMENUE_TEMPLATEFILE = "contexttemplate";

    /** Number of images to be shown per page in the download browser. */
    public static final int C_DOWNBROWSER_MAXENTRIES = 30;

    /** Exit action. */
    public static final String C_EDIT_ACTION_EXIT = "exit";

    /** Save action. */
    public static final String C_EDIT_ACTION_SAVE = "save";

    /** Save &amp; Exit action. */
    public static final String C_EDIT_ACTION_SAVEEXIT = "saveexit";

    /** Link of the error box. */
    public static final String C_ERROR_LINK = "ref";

    /** Message of the error box. */
    public static final String C_ERROR_MESSAGE = "message";

    /** Button label of the error box. */
    public static final String C_ERROR_MSG_BUTTON = "msgbutton";

    /** Button label of the error box. */
    public static final String C_ERROR_MSG_DETAILS = "details";

    /** Static text in the error box. */
    public static final String C_ERROR_MSG_REASON = "msgreason";

    /** Reason of the error box. */
    public static final String C_ERROR_REASON = "reason";

    /** Suggestion of the error box. */
    public static final String C_ERROR_SUGGESTION = "suggestion";

    /** Title of the error box. */
    public static final String C_ERROR_TITLE = "title";

    /** Name of the template containing error field definitions. */
    public static final String C_ERRORTEMPLATE = "errorTemplate";

    /** The access value column. */
    public static final String C_FILELIST_ACCESS_VALUE = "ACCESS_VALUE";

    /** The changed value column. */
    public static final String C_FILELIST_CHANGED_VALUE = "CHANGED_VALUE";

    /** The stylesheet class to be used for a file or folder entry. */
    public static final String C_FILELIST_CLASS_VALUE = "OUTPUT_CLASS";

    /** The access column. */
    public static final String C_FILELIST_COLUMN_ACCESS = "COLUMN_ACCESS";

    /** The changed column. */
    public static final String C_FILELIST_COLUMN_CHANGED = "COLUMN_CHANGED";

    /** The customizable column. */
    public static final String C_FILELIST_COLUMN_CUSTOMIZED = "COLUMN_CUSTOMIZED";

    /** The customizable value column. */
    public static final String C_FILELIST_COLUMN_CUSTOMIZED_VALUE = "COLUMN_CUSTOMIZED_VALUE";

    /** The group column. */
    public static final String C_FILELIST_COLUMN_GROUP = "COLUMN_GROUP";

    /** The locked column. */
    public static final String C_FILELIST_COLUMN_LOCKED = "COLUMN_LOCKED";

    /** The name column. */
    public static final String C_FILELIST_COLUMN_NAME = "COLUMN_NAME";

    /** The owner column. */
    public static final String C_FILELIST_COLUMN_OWNER = "COLUMN_OWNER";

    /** The size column. */
    public static final String C_FILELIST_COLUMN_SIZE = "COLUMN_SIZE";

    /** The state column. */
    public static final String C_FILELIST_COLUMN_STATE = "COLUMN_STATE";

    /** The title column. */
    public static final String C_FILELIST_COLUMN_TITLE = "COLUMN_TITLE";

    /** The type column. */
    public static final String C_FILELIST_COLUMN_TYPE = "COLUMN_TYPE";

    /** customized template value for the file list. */
    public static final String C_FILELIST_CUSTOMTEMPLATE = "customtemplate";

    /** Flag for displaying the group column. */
    public static final int C_FILELIST_GROUP = 64;

    /** The group value column. */
    public static final String C_FILELIST_GROUP_VALUE = "GROUP_VALUE";

    /** The icon value column. */
    public static final String C_FILELIST_ICON_VALUE = "ICON_VALUE";

    /** The link for a file or folder entry. */
    public static final String C_FILELIST_LINK_VALUE = "LINK_VALUE";

    /** The lock value column. */
    public static final String C_FILELIST_LOCK_VALUE = "LOCK_VALUE";

    /** The lockedby value column. */
    public static final String C_FILELIST_LOCKED_VALUE = "LOCKED_VALUE";

    /** The name value column. */
    public static final String C_FILELIST_NAME_VALUE = "NAME_VALUE";

    /** The owner value column. */
    public static final String C_FILELIST_OWNER_VALUE = "OWNER_VALUE";

    /** The size value column. */
    public static final String C_FILELIST_SIZE_VALUE = "SIZE_VALUE";

    /** The state value column. */
    public static final String C_FILELIST_STATE_VALUE = "STATE_VALUE";

    /** The suffix for file list values. */
    public static final String C_FILELIST_SUFFIX_VALUE = "_VALUE";

    /** The title value column. */
    public static final String C_FILELIST_TITLE_VALUE = "TITLE_VALUE";

    /** The type value column. */
    public static final String C_FILELIST_TYPE_VALUE = "TYPE_VALUE";

    /** template value for the file list. */
    public static final String C_FILELIST_TEMPLATE = "template";

    /** Action for the icon. */
    public static final String C_ICON_ACTION = "action";

    /** method that should be used for deciding to (de)activate the icon. */
    public static final String C_ICON_ACTIVE_METHOD = "activemethod";

    /** The default icon. */
    public static final String C_ICON_DEFAULT = "ic_file_othertype.gif";

    /** The extension for the icon images. */
    public static final String C_ICON_EXTENSION = ".gif";

    /** href text of the icon. */
    public static final String C_ICON_HREF = "href";

    /** Label of the icon. */
    public static final String C_ICON_LABEL = "label";

    /** Name of the icon. */
    public static final String C_ICON_NAME = "name";

    /** The prefix for the icon images. */
    public static final String C_ICON_PREFIX = "ic_file_";

    /** href target of the icon. */
    public static final String C_ICON_TARGET = "target";

    /** The filename to the icontemplate. */
    public static final String C_ICON_TEMPLATEFILE = "icontemplate";

    /** method that should be used for deciding whether the icon is visible. */
    public static final String C_ICON_VISIBLE_METHOD = "visiblemethod";

    /**  Action of the input field. */
    public static final String C_INPUT_ACTION = "action";

    /** Style class of the input field. */
    public static final String C_INPUT_CLASS = "class";

    /**  Length of the input field. */
    public static final String C_INPUT_LENGTH = "length";

    /**  Method of the input field. */
    public static final String C_INPUT_METHOD = "method";

    /** Name of the input field. */
    public static final String C_INPUT_NAME = "name";

    /**  Size of the input field. */
    public static final String C_INPUT_SIZE = "size";

    /**  Value of the input field. */
    public static final String C_INPUT_VALUE = "value";

    /** Name of the template containing input field definitions. */
    public static final String C_INPUTTEMPLATE = "inputTemplate";

    /** Name of the value. */
    public static final String C_LABEL_VALUE = "value";

    /** Name of the template containing label definitions. */
    public static final String C_LABELTEMPLATE = "labelTemplate";

    /** Prefix for button texts in the language file. */
    public static final String C_LANG_BUTTON = "button";

    /** Prefix for button texts in the language file. */
    public static final String C_LANG_ICON = "icon";

    /** Prefix for label texts in the language file. */
    public static final String C_LANG_LABEL = "label";

    /** Prefix for label texts in the language file. */
    public static final String C_LANG_TITLE = "title";

    /** First button of the messagebox. */
    public static final String C_MESSAGE_BUTTON1 = "button1";

    /** Second button of the messagebox. */
    public static final String C_MESSAGE_BUTTON2 = "button2";

    /** Link on button2 of the messagebox. */
    public static final String C_MESSAGE_FILENAME = "filename";

    /** Link on button1 of the messagebox. */
    public static final String C_MESSAGE_LINK1 = "link1";

    /** Link on button2 of the messagebox. */
    public static final String C_MESSAGE_LINK2 = "link2";

    /** First message of the messagebox. */
    public static final String C_MESSAGE_MESSAGE1 = "message1";

    /** Second message of the messagebox. */
    public static final String C_MESSAGE_MESSAGE2 = "message2";

    /** Title of the messagebox. */
    public static final String C_MESSAGE_TITLE = "title";

    /** the modules packetname key. */
    public static final String C_MODULE_PACKETNAME = "packetname";

    /**  Method of the modulelist field. */
    public static final String C_MODULELIST_AUTHOR = "author";

    /**  Method of the modulelist field. */
    public static final String C_MODULELIST_DATECREATED = "datecreated";

    /**  Method of the modulelist field. */
    public static final String C_MODULELIST_DATEUPLOADED = "dateuploaded";

    /**  Method of the modulelist field. */
    public static final String C_MODULELIST_IDX = "idx";

    /**  Method of the modulelist field. */
    public static final String C_MODULELIST_METHOD = "method";

    /**  Method of the modulelist field. */
    public static final String C_MODULELIST_NAME = "name";

    /**  Method of the modulelist field. */
    public static final String C_MODULELIST_NICE_NAME = "nicename";

    /** The filename to the modulelisttemplate. */
    public static final String C_MODULELIST_TEMPLATEFILE = "moduletemplate";

    /**  Method of the modulelist field. */
    public static final String C_MODULELIST_VERSION = "version";

    /** Panel defintion for explorer settings. */
    public static final String C_PANEL_EXPLORER = "explorer";

    /** Link URL for each panel. */
    public static final String C_PANEL_LINK = "link";

    /** Text for each panel. */
    public static final String C_PANEL_NAME = "panelname";

    /** Panel defintion for start settings. */
    public static final String C_PANEL_START = "start";

    /** Panel defintion for task settings. */
    public static final String C_PANEL_TASK = "task";

    /** Panel defintion for user settings. */
    public static final String C_PANEL_USER = "user";

    /** Name of the template containing panel bar definitions. */
    public static final String C_PANELTEMPLATE = "panelTemplate";

    /** Parameter for action commands. */
    public static final String C_PARA_ACTION = "action";

    /** Parameter name of "cancel" value. */
    public static final String C_PARA_CANCEL = "CANCEL";

    /** Parameter for text/html editor content. */
    public static final String C_PARA_CONTENT = "content";

    /** Parameter for the default value. */
    public static final String C_PARA_DEFAULT = "DEFAULT";

    /** Parameter for a deleting a file. */
    public static final String C_PARA_DELETE = "delete";

    /** Parameter for description. */
    public static final String C_PARA_DESCRIPTION = "description";

    /** Parameter for the explorersettings value. */
    public static final String C_PARA_EXPLORERSETTINGS = "EXPLORERSETTINGS";

    /** Parameter for a filecontent. */
    public static final String C_PARA_FILECONTENT = "filecontent";

    /** Parameter for filelist. */
    public static final String C_PARA_FILELIST = "filelist";

    /** Parameter for filter. */
    public static final String C_PARA_FILTER = "filter";

    /** Parameter for a flag. */
    public static final String C_PARA_FLAGS = "flags";

    /** Parameter for foldername. */
    public static final String C_PARA_FOLDER = "folder";

    /** Parameter for foldertree. */
    public static final String C_PARA_FOLDERTREE = "foldertree";

    /** Parameter for a formname. */
    public static final String C_PARA_FORMNAME = "formname";

    /** Parameter for initial load. */
    public static final String C_PARA_INITIAL = "initial";

    /** Parameter for java script filenames. */
    public static final String C_PARA_JSFILE = "jsfile";

    /** Parameter for keywords. */
    public static final String C_PARA_KEYWORDS = "keywords";

    /** Parameter for layout. */
    public static final String C_PARA_LAYOUT = "default_body";

    /** Parameter for a link. */
    public static final String C_PARA_LINK = "link";

    /** Parameter for locking pages. */
    public static final String C_PARA_LOCK = "lock";

    /** Parameter for maximum pages. */
    public static final String C_PARA_MAXPAGE = "maxpage";

    /** Parameter for a name. */
    public static final String C_PARA_NAME = "name";

    /** Parameter for a navigation position. */
    public static final String C_PARA_NAVPOS = "navpos";

    /** Parameter for a navigation title. */
    public static final String C_PARA_NAVTEXT = "navtitle";

    /** Parameter for a new accessflags. */
    public static final String C_PARA_NEWACCESS = "newaccess";

    /** Parameter for a new file. */
    public static final String C_PARA_NEWFILE = "newfile";

    /** Parameter for a new folder. */
    public static final String C_PARA_NEWFOLDER = "newfolder";

    /** Parameter for a new group. */
    public static final String C_PARA_NEWGROUP = "newgroup";

    /** Parameter for a new owner. */
    public static final String C_PARA_NEWOWNER = "newowner";

    /** Parameter for the new password. */
    public static final String C_PARA_NEWPWD = "newpwd";

    /** Parameter for the password repeat. */
    public static final String C_PARA_NEWPWDREPEAT = "newpwdrepeat";

    /** Parameter for a new type. */
    public static final String C_PARA_NEWTYPE = "newtype";

    /** Parameter for the ok value. */
    public static final String C_PARA_OK = "OK";

    /** Parameter for the previous panel. */
    public static final String C_PARA_OLDPANEL = "oldpanel";

    /** Parameter for the old password. */
    public static final String C_PARA_OLDPWD = "oldpwd";

    /** Parameter for page number. */
    public static final String C_PARA_PAGE = "page";

    /** Parameter for a panel. */
    public static final String C_PARA_PANEL = "panel";

    /** Parameter for previous filelist. */
    public static final String C_PARA_PREVIOUSLIST = "previous";

    /** Parameter for a project. */
    public static final String C_PARA_PROJECT = "project";

    /** Parameter for properties. */
    public static final String C_PARA_PROPERTYDEF = "property";

    /** Parameter for startprojectid. */
    public static final String C_PARA_STARTPROJECTID = "startProjectId";

    /** Parameter for the startsettings value. */
    public static final String C_PARA_STARTSETTINGS = "STARTSETTINGS";

    /** Parameter for starttaskid. */
    public static final String C_PARA_STARTTASKID = "startTaskId";

    /** Parameter for submitting data. */
    public static final String C_PARA_SUBMIT = "SUBMIT";

    /** Parameter for the tasksettings value. */
    public static final String C_PARA_TASKSETTINGS = "TASKSETTINGS";

    /** Parameter for a template. */
    public static final String C_PARA_TEMPLATE = "template";

    /** Parameter for a title. */
    public static final String C_PARA_TITLE = "title";

    /** Parameter for unlocking pages. */
    public static final String C_PARA_UNLOCK = "unlock";

    /** Parameter for url. */
    public static final String C_PARA_URL = "URL";

    /** Parameter for the usersettings value. */
    public static final String C_PARA_USERSETTINGS = "USERSETTINGS";

    /** Parameter for a variable. */
    public static final String C_PARA_VARIABLE = "variable";

    /** Parameter for view name. */
    public static final String C_PARA_VIEW = "view";

    /** Parameter for viewfile. */
    public static final String C_PARA_VIEWFILE = "viewfile";

    /** Parameter for a filename. */
    public static final String C_PARA_RESOURCE = "resource";

    /** Number of images to be shown per page in the picture browser. */
    public static final int C_PICBROWSER_MAXIMAGES = 15;

    /** The preferences update. */
    public static final String C_PREFERENCES_UPDATE = "../action/preferences_update.html";

    /** The filename to the prefs scroller template file. */
    public static final String C_PREFSSCROLLER_TEMPLATEFILE = "prefsscrollerTemplate";

    /**  Method of the projectlist field. */
    public static final String C_PROJECTLIST_DATECREATED = "datecreated";

    /**  Method of the projectlist field. */
    public static final String C_PROJECTLIST_DESCRIPTION = "description";

    /**  Method of the projectlist field. */
    public static final String C_PROJECTLIST_IDX = "idx";

    /**  Method of the projectlist field. */
    public static final String C_PROJECTLIST_LOCKSTATE = "lockstate";

    /**  Method of the projectlist field. */
    public static final String C_PROJECTLIST_MENU = "menu";

    /**  Method of the projectlist field. */
    public static final String C_PROJECTLIST_METHOD = "method";

    /**  Method of the projectlist field. */
    public static final String C_PROJECTLIST_NAME = "name";

    /**  Method of the projectlist field. */
    public static final String C_PROJECTLIST_NAME_ESCAPED = "name_escaped";

    /**  Method of the projectlist field. */
    public static final String C_PROJECTLIST_OWNER = "owner";

    /**  Method of the projectlist field. */
    public static final String C_PROJECTLIST_PROJECTID = "id";

    /**  Method of the projectlist field. */
    public static final String C_PROJECTLIST_PROJECTMANAGER = "projectmanager";

    /**  Method of the projectlist field. */
    public static final String C_PROJECTLIST_PROJECTWORKER = "projectworker";

    /**  Method of the projectlist field. */
    public static final String C_PROJECTLIST_STATE = "STATE";

    /**  Method of the projectlist field. */
    public static final String C_PROJECTLIST_STATE_LOCKED = "project.state.FILESLOCKED";

    /**  Method of the projectlist field. */
    public static final String C_PROJECTLIST_STATE_UNLOCKED = "project.state.FILESUNLOCKED";

    /** The filename to the projectlisttemplate. */
    public static final String C_PROJECTLIST_TEMPLATEFILE = "projecttemplate";

    /** Parameter of projectnew. */
    public static final String C_PROJECTNEW_DESCRIPTION = "DESCRIPTION";

    /** Templateselector of projectnew. */
    public static final String C_PROJECTNEW_DONE = "done";

    /** Templateselector of projectnew. */
    public static final String C_PROJECTNEW_ERROR = "error";

    /** Parameter of projectnew. */
    public static final String C_PROJECTNEW_FOLDER = "selectallfolders";

    /** Parameter of projectnew. */
    public static final String C_PROJECTNEW_GROUP = "GROUP";

    /** Parameter of projectnew. */
    public static final String C_PROJECTNEW_MANAGERGROUP = "MANAGERGROUP";

    /** Parameter of projectnew. */
    public static final String C_PROJECTNEW_NAME = "NAME";

    /** Parameter of projectnew. */
    public static final String C_PROJECTNEW_TYPE = "TYPE";

    /** Stylesheet class string of the radio button. */
    public static final String C_RADIO_CLASS = "class";

    /** Stylesheet class name of the radio button. */
    public static final String C_RADIO_CLASSNAME = "classname";

    /** Datablock conatining the optional entry for the image. */
    public static final String C_RADIO_IMAGEENTRY = "imageentry";

    /** Name of the radio button image name. */
    public static final String C_RADIO_IMAGENAME = "image";

    /** Datablock conatining the image option. */
    public static final String C_RADIO_IMAGEOPTION = "optionalimage";

    /** Name of the radio button link. */
    public static final String C_RADIO_LINK = "link";

    /** Method of the radio buttons. */
    public static final String C_RADIO_METHOD = "method";

    /** Name of the radio button value. */
    public static final String C_RADIO_NAME = "name";

    /** Name of the radio ordering information. */
    public static final String C_RADIO_ORDER = "order";

    /** Name of the radio buttons. */
    public static final String C_RADIO_RADIONAME = "radioname";

    /** Datablock conatining the optional entry for the "checked" option. */
    public static final String C_RADIO_SELECTEDENTRY = "selectedentry";

    /** Datablock conatining the "checked" option. */
    public static final String C_RADIO_SELECTEDOPTION = "optionalselected";

    /** Name of the template containing radiobutton definitions. */
    public static final String C_RADIOTEMPLATE = "radioTemplate";

    /** Stylesheet class string of the select box. */
    public static final String C_SELECTBOX_CLASS = "class";

    /** Stylesheet class name of the select box. */
    public static final String C_SELECTBOX_CLASSNAME = "classname";

    /** Div flag of the select box. */
    public static final String C_SELECTBOX_DIV = "div";

    /** default selected option value for editor view select boxes. */
    public static final int[] C_SELECTBOX_EDITORVIEWS_DEFAULT = {0, 1};

    /** templates of the different option values for editor view select boxes. */
    public static final String[] C_SELECTBOX_EDITORVIEWS_TEMPLATES = {"edit_html_main", "edit_text_main"};

    /** Method of the select box. */
    public static final String C_SELECTBOX_METHOD = "method";

    /** Name of the select box. */
    public static final String C_SELECTBOX_NAME = "name";

    /** Onchange of the select box. */
    public static final String C_SELECTBOX_ONCHANGE = "onchange";

    /** option name of the select box. */
    public static final String C_SELECTBOX_OPTIONNAME = "name";

    /** option value of the select box. */
    public static final String C_SELECTBOX_OPTIONVALUE = "value";

    /** Size of the select box. */
    public static final String C_SELECTBOX_SIZE = "size";

    /** Width of the select box. */
    public static final String C_SELECTBOX_WIDTH = "width";

    /** Stylesheet class name of the select box. */
    public static final String C_SELECTBOX_WIDTHNAME = "widthname";

    /** The modules administration data. */
    public static final String C_SESSION_MODULE_ADMIN_DATA = "module_admin_data";

    /** The modules administration property description. */
    public static final String C_SESSION_MODULE_ADMIN_PROP_DESCR = "module_admin_props_desr";

    /** The modules administration property names. */
    public static final String C_SESSION_MODULE_ADMIN_PROP_NAMES = "module_admin_props_names";

    /** The modules administration property type. */
    public static final String C_SESSION_MODULE_ADMIN_PROP_TYP = "module_admin_props_typ";

    /** the modules administration property value. */
    public static final String C_SESSION_MODULE_ADMIN_PROP_VAL = "module_admin_props_value";

    /** The modules checksum. */
    public static final String C_SESSION_MODULE_CHECKSUM = "modulechecksum";

    /** The step of module deletion. */
    public static final String C_SESSION_MODULE_DELETE_STEP = "moduledeletestep";

    /** The exclusions of the modulelisttemplate. */
    public static final String C_SESSION_MODULE_EXCLUSION = "moduleexclusion";

    /** The "module in use" flag. */
    public static final String C_SESSION_MODULE_INUSE = "moduleinuse";

    /** The missing files of the modulelisttemplate. */
    public static final String C_SESSION_MODULE_MISSFILES = "modulemissfiles";

    /** The modules project files. */
    public static final String C_SESSION_MODULE_PROJECTFILES = "moduleprojectfiles";

    /** The property files of the modulelisttemplate. */
    public static final String C_SESSION_MODULE_PROPFILES = "modulepropfiles";

    /** Constant for session-key. */
    public static final String C_SESSION_TASK_ALLPROJECTS = "task_allprojects";

    /** Constant for session-key. */
    public static final String C_SESSION_TASK_FILTER = "task_filter";

    /** Name of the errorbox tag in the error definiton template. */
    public static final String C_TAG_ERRORBOX = "errorbox";

    /** Name of the error?page tag in the error definiton template. */
    public static final String C_TAG_ERRORPAGE = "errorpagedefinition";

    /** Name of the label tag in the input definiton template. */
    public static final String C_TAG_INPUTFIELD = "inputfield";

    /** Name of the label tag in the label definition template. */
    public static final String C_TAG_LABEL = "label";

    /** Name of the errorbox tag in the error definiton template. */
    public static final String C_TAG_MESSAGEBOX = "messagepage";

    /** Name of the label tag in the label definition template. */
    public static final String C_TAG_MODULELIST_DEFAULT = "defaultmodulelist";

    /** Panel bar sequence for active background in the panel bar definiton template. */
    public static final String C_TAG_PANEL_BGACTIVE = "paneldef.bgactive";

    /** Panel bar sequence for inactive background in the panel bar definiton template. */
    public static final String C_TAG_PANEL_BGINACTIVE = "paneldef.bginactive";

    /** Panel bar ending sequence tag in the panel bar definiton template. */
    public static final String C_TAG_PANEL_ENDSEQ = "paneldef.endseq";

    /** Panel bar sequence for separating background and text area in the panel bar definiton template. */
    public static final String C_TAG_PANEL_SEPBGTEXT = "paneldef.sepbgtext";

    /** Panel bar starting sequence tag in the panel bar definiton template. */
    public static final String C_TAG_PANEL_STARTSEQ = "paneldef.startseq";

    /** Panel bar sequence for active text in the panel bar definiton template. */
    public static final String C_TAG_PANEL_TEXTACTIVE = "paneldef.textactive";

    /** Panel bar sequence for inactive text in the panel bar definiton template. */
    public static final String C_TAG_PANEL_TEXTINACTIVE = "paneldef.textinactive";

    /** Name of the password tag in the input definiton template. */
    public static final String C_TAG_PASSWORD = "password";

    /** Name of the label tag in the label definition template. */
    public static final String C_TAG_PROJECTLIST_DEFAULT = "defaultprojectlist";

    /** Name of the label tag in the label definition template. */
    public static final String C_TAG_PROJECTLIST_SNAPLOCK = "snaplock";

    /** Name of the radio "class" option tag in the input definiton template. */
    public static final String C_TAG_RADIO_CLASS = "radiobuttons.class";

    /** Name of the radion column entry tag in the input definiton template. */
    public static final String C_TAG_RADIO_COLENTRY = "radiobuttons.colentry";

    /** Name of the radion row entry tag in the input definiton template. */
    public static final String C_TAG_RADIO_ROWENTRY = "radiobuttons.rowentry";

    /** Name of the selectbox "class" option tag in the input definiton template. */
    public static final String C_TAG_SELECTBOX_CLASS = "selectbox.class";

    /** Name of the select end tag in the input definiton template. */
    public static final String C_TAG_SELECTBOX_END = "selectbox.end";

    /** Name of the (select) option tag in the input definiton template. */
    public static final String C_TAG_SELECTBOX_OPTION = "selectbox.option";

    /** Name of the (select) selected option tag in the input definiton template. */
    public static final String C_TAG_SELECTBOX_SELOPTION = "selectbox.seloption";

    /** Name of the select start tag in the input definiton template. */
    public static final String C_TAG_SELECTBOX_START = "selectbox.start";

    /** Name of the select div start tag in the input definiton template. */
    public static final String C_TAG_SELECTBOX_START_DIV = "selectbox.startdiv";

    /** Name of the selectbox "width" option tag in the input definiton template. */
    public static final String C_TAG_SELECTBOX_WIDTH = "selectbox.width";

    /** Name of the startup tag in the input definiton template. */
    public static final String C_TAG_STARTUP = "STARTUP";

    /** Name of the submitbutton tag in the button definiton template. */
    public static final String C_TAG_SUBMITBUTTON = "submitbutton";

    /** The filename to the taskdocu template. */
    public static final String C_TASKDOCU_TEMPLATEFILE = "tasklistdoctemplate";

    /** The filename to the projectlisttemplate. */
    public static final String C_TASKLIST_TEMPLATEFILE = "tasklisttemplate";

    /** Constant for task-log. */
    public static final int C_TASKLOGTYPE_ACCEPTED = 101;

    /** Constant for task-log. */
    public static final int C_TASKLOGTYPE_CALL = 107;

    /** Constant for task-log. */
    public static final int C_TASKLOGTYPE_COMMENT = 102;

    /** Constant for task-log. */
    public static final int C_TASKLOGTYPE_CREATED = 100;

    /** Constant for task-log. */
    public static final int C_TASKLOGTYPE_DUECHANGED = 108;

    /** Constant for task-log. */
    public static final int C_TASKLOGTYPE_FORWARDED = 106;

    /** Constant for task-log. */
    public static final int C_TASKLOGTYPE_OK = 104;

    /** Constant for task-log. */
    public static final int C_TASKLOGTYPE_PRIORITYCHANGED = 109;

    /** Constant for task-log. */
    public static final int C_TASKLOGTYPE_REACTIVATED = 105;

    /** Constant for task-log. */
    public static final int C_TASKLOGTYPE_TAKE = 103;

    /** Taskparameter. */
    public static final String C_TASKPARA_ACCEPTATION = "acceptation";

    /** Taskparameter. */
    public static final String C_TASKPARA_ALL = "all";

    /** Taskparameter. */
    public static final String C_TASKPARA_COMPLETION = "completion";

    /** Taskparameter. */
    public static final String C_TASKPARA_DELIVERY = "delivery";

    /** Name of the filelist preferences. */
    public static final String C_USERPREF_FILELIST = "filelist";

    /** Parameter of user management. */
    public static final String C_USERS_NAME = "USERNAME";

    /** Directory name of content internal folder. */
    public static final String C_VFS_DIR_INTERNAL = "internal/";

    /** Directory name of module locales path. */
    public static final String C_VFS_DIR_LOCALES = "locales/";

    /** 
     * Path to the login folder.<p>
     * 
     * It's important to note that this does NOT end with a / 
     * as most other path constants do!<p>
     */
    public static final String VFS_PATH_LOGIN = CmsWorkplace.VFS_PATH_SYSTEM + "login";

    /** Path to content default_bodies folder. */
    public static final String C_VFS_PATH_DEFAULT_BODIES = CmsCompatibleCheck.VFS_PATH_DEFAULTMODULE + CmsWorkplace.VFS_DIR_DEFAULTBODIES;

    /** Path to module demo folder (deprecated since version 5.0 beta 2). */
    public static final String C_VFS_PATH_MODULEDEMOS = CmsWorkplace.VFS_PATH_MODULES + "moduledemos/";

    /** Path to online help pages. */
    public static final String C_VFS_PATH_HELP = CmsWorkplace.VFS_PATH_WORKPLACE + "help/";

    /** Path to scripts. */
    public static final String C_VFS_PATH_SCRIPTS = CmsWorkplace.VFS_PATH_WORKPLACE + "scripts/";

    /** Path to download gallery folder. */
    public static final String C_VFS_GALLERY_DOWNLOAD = CmsWorkplace.VFS_PATH_GALLERIES + "download/";

    /** Path to externallink gallery folder. */
    public static final String C_VFS_GALLERY_EXTERNALLINKS = CmsWorkplace.VFS_PATH_GALLERIES + "externallinks/";

    /** Path to html gallery folder. */
    public static final String C_VFS_GALLERY_HTML = CmsWorkplace.VFS_PATH_GALLERIES + "htmlgalleries/";

    /** Path to pics gallery folder. */
    public static final String C_VFS_GALLERY_PICS = CmsWorkplace.VFS_PATH_GALLERIES + "pics/";

    /** Path to content internal folder. */
    public static final String C_VFS_PATH_DEFAULT_INTERNAL = CmsWorkplace.VFS_PATH_WORKPLACE + "templates/";

    /** The channel tree. */
    public static final String C_WP_CHANNEL_TREE = "../action/channel_tree.html";

    /** The explorer file list. */
    public static final String C_WP_EXPLORER_FILELIST = "../action/explorer_files.html";

    /** The preferences user panel. */
    public static final String C_WP_EXPLORER_PREFERENCES = "../action/preferences.html?panel=user";

    /** The explorer tree. */
    public static final String C_WP_EXPLORER_TREE = "../action/explorer_tree.html";

    /** The folder tree. */
    public static final String C_WP_FOLDER_TREE = "../action/folder_tree.html";

    /** The workplace. */
    public static final String C_WP_RELOAD = "../action/workplace_reload.html";

    /**
     * Checks a Java System property for containing the given value
     * @param propertyName Name of the property
     * @param value Value that should be checked
     * @return <code>true</code> if the property contains the value, <code>false</code> otherwise
     */
    protected boolean checkJavaProperty(String propertyName, String value) {
        boolean result = false;
        String prop = null;
        try {
            prop = System.getProperty(propertyName);
            if(prop != null && prop.equals(value)) {
                result = true;
            }
            return result;
        }
        catch(Exception e) {
            return false;
        }
    }

    /**
     * Used by workplace icons to decide whether the icon should
     * be activated or not. Icons will use this method if the attribute <code>method="doNotShow"</code>
     * is defined in the <code>&lt;ICON&gt;</code> tag.
     * <P>
     * This method always returns <code>false</code> thus icons controlled by
     * this method will never be activated.
     *
     * @param cms CmsObject Object for accessing system resources <em>(not used here)</em>.
     * @param lang reference to the currently valid language file <em>(not used here)</em>.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return <code>false</code>.
     */
    public Boolean doNotShow(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) {
        return new Boolean(false);
    }

    /**
     * Gets a reference to the default config file.
     * The path to this file ist stored in <code>C_WORKPLACE_INI</code>
     *
     * @param cms CmsObject Object for accessing system resources.
     * @return Reference to the config file.
     * @throws CmsException
     */
    public CmsXmlWpConfigFile getConfigFile(CmsObject cms) throws CmsException {

        //if(m_configFile == null) {
        m_configFile = new CmsXmlWpConfigFile(cms);

        //}
        return m_configFile;
    }

    /**
     * Help method used to fill the vectors returned to
     * <code>CmsSelectBox</code> with constant values.
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param content String array containing the elements to be set.
     * @param lang reference to the currently valid language file
     */
    protected void getConstantSelectEntries(Vector names, Vector values, String[] contents,
            CmsXmlLanguageFile lang) throws CmsException {
        for(int i = 0;i < contents.length;i++) {
            String value = contents[i];
            values.addElement(value);
            String s = lang.getLanguageValue("select." + value);
            if ((s == null) || s.startsWith("???")) s = value;
            names.addElement(s);
        }
    }

    /**
     * Gets all fonts available in the WYSIWYG editor.
     * 
     * The given vectors <code>names</code> and <code>values</code> will
     * be filled with the appropriate information to be used for building
     * a select box.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param lang reference to the currently valid language file
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the user's current workplace view in the vectors.
     * @throws CmsException
     */
    public Integer getFonts(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values,
            Hashtable parameters) throws CmsException {
        for(int i = 0;i < CmsDefaultPageEditor.SELECTBOX_FONTS.length; i++) {
            String value = CmsDefaultPageEditor.SELECTBOX_FONTS[i];
            values.addElement(value);
            names.addElement(value);
        }        
        return new Integer(0);
    }

    /**
     * Gets the key that should be used to cache the results of
     * this template class.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return key that can be used for caching
     */
    public Object getKey(CmsObject cms, String templateFile, Hashtable parameters, String templateSelector) {

        CmsRequestContext reqContext = cms.getRequestContext();

        String result = "" 
            + reqContext.currentProject().getId() 
            + ":" 
            + reqContext.currentUser().getName() 
            + reqContext.addSiteRoot(templateFile);
        Enumeration keys = parameters.keys();

        // select the right language to use
        String currentLanguage = null;

        CmsUserSettings settings = new CmsUserSettings(cms);
        currentLanguage = settings.getLocale().toString();

        while(keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            result = result + key + parameters.get(key);
        }
        result = result + templateSelector + currentLanguage;
        return result;
    }

    /**
     * Get the currently valid <code>lasturl</code> parameter that can be
     * used for redirecting to the previous workplace screen.
     * @param cms Cms object for accessing system resources.
     * @param parameters User parameters.
     * @return <code>lasturl</code> parameter.
     */
    protected String getLastUrl(CmsObject cms, Hashtable parameters) {
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        String lasturl = (String)parameters.get("lasturl");

        // Lasturl parameter will be taken either from the parameter hashtable
        // (if exists) or from the session storage.
        // If neccessary, session storage will be updated.
        StringBuffer encLasturl = new StringBuffer();
        boolean notfirst = false;
        if(lasturl != null) {

            // Fine. A lasturl parameter was found in session or parameter hashtable.
            // Check, if the URL parameters of the last url have to be encoded.
            int asteriskIdx = lasturl.indexOf("?");
            if(asteriskIdx > -1 && (asteriskIdx < (lasturl.length() - 1))) {

                // In fact, there are URL parameters
                encLasturl.append(lasturl.substring(0, asteriskIdx + 1));
                String queryString = lasturl.substring(asteriskIdx + 1);
                StringTokenizer st = new StringTokenizer(queryString, "&");
                while(st.hasMoreTokens()) {

                    // Loop through all URL parameters
                    String currToken = st.nextToken();
                    if(currToken != null && !"".equals(currToken)) {

                        // Look for the "=" character to divide parameter name and value
                        int idx = currToken.indexOf("=");
                        if(notfirst) {
                            encLasturl.append("&");
                        }
                        else {
                            notfirst = true;
                        }
                        if(idx > -1) {

                            // A parameter name/value pair was found.
                            // Encode the parameter value and write back!
                            String key = currToken.substring(0, idx);
                            String value = (idx < (currToken.length() - 1)) ? currToken.substring(idx + 1) : "";
                            encLasturl.append(key);
                            encLasturl.append("=");
                            encLasturl.append(CmsEncoder.escape(value,
                                cms.getRequestContext().getEncoding()));
                        }
                        else {

                            // Something strange happened.
                            // Maybe a parameter without "=" ?
                            // Write back without encoding!
                            encLasturl.append(currToken);
                        }
                    }
                }
                lasturl = encLasturl.toString();
            }
            session.putValue("lasturl", lasturl);
        }
        else {
            lasturl = (String)session.getValue("lasturl");
        }
        return lasturl;
    }

    /**
     * Reads in the template file and starts the XML parser for the expected
     * content type <class>CmsXmlWpTemplateFile</code>
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public CmsXmlTemplateFile getOwnTemplateFile(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);
        return xmlTemplateDocument;
    }
    
    /**
     * Checks if the current user is <strong>administrator</strong>.
     * <P>
     * This method is used by workplace icons to decide whether the icon should
     * be activated or not. Icons will use this method if the attribute <code>method="isAdministrator"</code>
     * is defined in the <code>&lt;ICON&gt;</code> tag.
     *
     * @param cms CmsObject Object for accessing system resources <em>(not used here)</em>.
     * @param lang reference to the currently valid language file <em>(not used here)</em>.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return <code>true</code> if the current user is in the Administrators Group, <code>false</code> otherwise.
     * @throws CmsException if there were errors while accessing project data.
     */
    public Boolean isAdmin(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) throws CmsException {
        return new Boolean(cms.isAdmin());
    }

    /**
     * Same as above, only that this returns always 'false'.
     * Can be used to quickly deactivate unwanted icons on the workplace even
     * for then admin by just adding "False" to isAdmin call in file property.
     */
    public Boolean isAdminFalse(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) throws CmsException {
        return new Boolean(false);
    }
    
    /**
     * Checks if the current user is the administrator <strong>Admin</strong>.
     * <P>
     * This method is used by workplace icons to decide whether the icon should
     * be activated or not. 
     * 
     * @param cms CmsObject Object for accessing system resources <em>(not used here)</em>.
     * @param lang reference to the currently valid language file <em>(not used here)</em>.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return <code>true</code> if the current user is the user Admin, <code>false</code> otherwise.
     * @throws CmsException if there were errors while accessing project data.
     */
    public Boolean isTheAdminUser(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) throws CmsException {
        CmsRequestContext reqCont = cms.getRequestContext();
        if(cms.isAdmin()){
            return new Boolean(reqCont.currentUser().getName().equals(OpenCms.getDefaultUsers().getUserAdmin()));
        }else{
            return new Boolean(false);
        }
    }

    /**
     * Indicates if the results of this class are cacheable.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }

    /**
     * gets the caching information from the current template class.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if this class may stream it's results, <EM>false</EM> otherwise.
     */
    public CmsCacheDirectives getCacheDirectives(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return new CmsCacheDirectives(false);
    }

    /**
     * Indicates if the current template class is able to stream it's results
     * directly to the response oputput stream.
     * <P>
     * Classes must not set this feature, if they might throw special
     * exception that cause HTTP errors (e.g. 404/Not Found), or if they
     * might send HTTP redirects.
     * <p>
     * If a class sets this feature, it has to check the
     * isStreaming() property of the RequestContext. If this is set
     * to <code>true</code> the results must be streamed directly
     * to the output stream. If it is <code>false</code> the results
     * must not be streamed.
     * <P>
     * Complex classes that are able top include other subtemplates
     * have to check the streaming ability of their subclasses here!
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if this class may stream it's results, <EM>false</EM> otherwise.
     */
    public boolean isStreamable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }

    /**
     * Checks if the current project is <STRONG>not</STRONG> the "Online" project.
     * <P>
     * This method is used by workplace icons to decide whether the icon should
     * be activated or not. Icons will use this method if the attribute <code>method="isNotOnlineProject"</code>
     * is defined in the <code>&lt;ICON&gt;</code> tag.
     *
     * @param cms CmsObject Object for accessing system resources <em>(not used here)</em>.
     * @param lang reference to the currently valid language file <em>(not used here)</em>.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return <code>true</code> if the current project is the online project, <code>false</code> otherwise.
     * @throws CmsException if there were errors while accessing project data.
     */
    public Boolean isNotOnlineProject(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) throws CmsException {
        CmsRequestContext reqCont = cms.getRequestContext();
        return new Boolean(!reqCont.currentProject().isOnlineProject());
    }

    /**
     * Checks if the current project is the "Online" project.
     * <P>
     * This method is used by workplace icons to decide whether the icon should
     * be activated or not. Icons will use this method if the attribute <code>method="isOnlineProject"</code>
     * is defined in the <code>&lt;ICON&gt;</code> tag.
     *
     * @param cms CmsObject Object for accessing system resources <em>(not used here)</em>.
     * @param lang reference to the currently valid language file <em>(not used here)</em>.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return <code>true</code> if the current project is the online project, <code>false</code> otherwise.
     * @throws CmsException if there were errors while accessing project data.
     */
    public Boolean isOnlineProject(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) throws CmsException {
        CmsRequestContext reqCont = cms.getRequestContext();
        return new Boolean(reqCont.currentProject().isOnlineProject());
    }

    /**
     * Checks if the current user is <STRONG>Project manager</STRONG>.
     * <P>
     * This method is used by workplace icons to decide whether the icon should
     * be activated or not. Icons will use this method if the attribute <code>method="isProjectManager"</code>
     * is defined in the <code>&lt;ICON&gt;</code> tag.
     *
     * @param cms CmsObject Object for accessing system resources <em>(not used here)</em>.
     * @param lang reference to the currently valid language file <em>(not used here)</em>.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return <code>true</code> if the current project is the online project, <code>false</code> otherwise.
     * @throws CmsException if there were errors while accessing project data.
     */
    public Boolean isProjectManager(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) throws CmsException {
        return new Boolean(cms.isManagerOfProject());
    }

    /**
     * User method to generate an URI for the system resources folder.<p>
     * 
     * All pictures and style sheets should reside in the docroot of the webserver for
     * performance reasons. This folder can be mounted into the OpenCms system to
     * make it accessible for the OpenCms explorer.<p>
     * 
     * The path to the docroot can be set in the workplace ini.<p>
     * 
     * In any workplace template file, this method can be invoked by
     * <code>&lt;METHOD name="resourcesUri"&gt;<em>PictureName</em>&lt;/METHOD&gt;</code>.<p>
     * 
     * <b>Warning:</b> Using this method, only workplace resources, usually residing
     * in the <code>/system/workplace/resources</code> folder, can be accessed. 
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XML document <em>(not used here)</em>.
     * @param userObj Hashtable with parameters <em>(not used here)</em>.
     * @return String with the resources URI
     * @throws CmsException
     */
    public Object resourcesUri(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) throws CmsException {
        if(m_resourcesUri == null) {
            CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);
            m_resourcesUri = configFile.getWpPicturePath();
        }
        if (tagcontent == null) return m_resourcesUri;
        return m_resourcesUri + tagcontent;
    }
    
    /**
     * User method to generate an URI for the system scripts folder.<p>
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XML document <em>(not used here)</em>.
     * @param userObj Hashtable with parameters <em>(not used here)</em>.
     * @return String with the scripts URI
     * @throws CmsException
     */
    public Object scriptsUri(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) throws CmsException {
        if(m_scriptsUri == null) {
            m_scriptsUri = CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getServletUrl() + CmsWorkplaceDefault.C_VFS_PATH_SCRIPTS;
        }
        if (tagcontent == null) return m_scriptsUri;
        return m_scriptsUri + tagcontent;
    }

    /**
     * @deprecated use {@link #resourcesUri(CmsObject, String, A_CmsXmlContent, Object)} instead
     */
    public Object picsUrl(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) throws CmsException {
        return resourcesUri(cms, tagcontent, doc, userObj);
    }
    
    /**
     * User method to generate the JavaScript for the tree initialization.<p>
     * 
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XML document <em>(not used here)</em>.
     * @param userObj Hashtable with parameters <em>(not used here)</em>.
     * @return String with the JavaScript for the tree initialization
     * @throws CmsException if something goes wrong
     */
    public Object initTree(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) throws CmsException {    
        String skinUri = CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getWebAppUrl() + "/resources/";
        return CmsTree.initTree(cms, cms.getRequestContext().getEncoding(), skinUri);
    }
    
    /**
     * Starts the processing of the given template file by calling the
     * <code>getProcessedTemplateContent()</code> method of the content defintition
     * of the corresponding content type.
     * <P>
     * Any exceptions thrown while processing the template will be caught,
     * printed and and thrown again.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param xmlTemplateDocument XML parsed document of the content type "XML template file" or
     * any derived content type.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return Content of the template and all subtemplates.
     * @throws CmsException
     */
    protected byte[] startProcessing(CmsObject cms, CmsXmlTemplateFile xmlTemplateDocument, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {

        // checks if the access was with the correct port. If not it sends a not found error
        if( checkPort(cms) ) {
            String lasturl = getLastUrl(cms, parameters);
            // Since we are in the workplace, no browser caching is allowed here.
            // Set all caching information to "no-cache".
            // Don't bother about the internal caching here! Workplace users should be forced
            // to reload the workplace pages at every request.
            //HTTP 1.1
            CmsXmlTemplateLoader.getResponse(cms.getRequestContext()).setHeader("Cache-Control", "no-cache");
            //HTTP 1.0
            CmsXmlTemplateLoader.getResponse(cms.getRequestContext()).setHeader("Pragma", "no-cache");
            ((CmsXmlWpTemplateFile)xmlTemplateDocument).setData("lasturl", lasturl);
            return super.startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
        } else {
            throw new CmsLegacyException("No access to the workplace with this port", CmsLegacyException.C_NOT_FOUND);
        }
    }

    /**
     * Checks, if the request is running on the correct port. With the opencms.properties
     * the access to the workplace can be limitted to a user defined port. With this
     * feature a firewall can block all outside requests to this port with the result
     * the workplace is only available in the local net segment.
     * @param cms the CmsObject to check the port with.
     */
    protected boolean checkPort(CmsObject cms) {
        return true;
    }

    /**
     * User method to get the name of the user.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document <em>(not used here)</em>.
     * @param userObj Hashtable with parameters <em>(not used here)</em>.
     * @return String with the pics URL.
     * @throws CmsException
     */
    public Object userName(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) throws CmsException {
        return CmsUser.getFullName(cms.getRequestContext().currentUser());
    }
    

    /**
     * Returns the explorer file URI (required for compatibility to the JSP workplace).<p>
     *
     * @param cms for accessing system resources.
     * @param tagcontent additional parameter passed to the method <em>(not used here)</em>.
     * @param doc reference to the A_CmsXmlContent object of the initiating XML document <em>(not used here)</em>.
     * @param userObj Hashtable with parameters <em>(not used here)</em>.
     * @return String the explorer file URI
     * @throws CmsException if something goes wring
     */
    public Object explorerFileUri(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) throws CmsException {
        return CmsWorkplaceAction.getExplorerFileUri(CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getOriginalRequest());
    }    
    
    /**
     * Returns the explorer file URI with the full path, including context 
     * (required for compatibility to the JSP workplace).<p>
     *
     * @param cms for accessing system resources.
     * @param tagcontent additional parameter passed to the method <em>(not used here)</em>.
     * @param doc reference to the A_CmsXmlContent object of the initiating XML document <em>(not used here)</em>.
     * @param userObj Hashtable with parameters <em>(not used here)</em>.
     * @return String the explorer file URI
     * @throws CmsException if something goes wring
     */
    public Object explorerFileFullUri(CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) throws CmsException {
        return CmsWorkplaceAction.getExplorerFileFullUri(cms, CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getOriginalRequest());
    }        
}

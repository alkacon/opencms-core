/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/I_CmsWpConstants.java,v $
 * Date   : $Date: 2000/02/20 10:14:00 $
 * Version: $Revision: 1.47 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.workplace;

/**
 * Interface defining all constants used in OpenCms
 * workplace classes and elements.
 * 
 * @author Alexander Lucas
 * @author Michael Emmerich
 * @version $Revision: 1.47 $ $Date: 2000/02/20 10:14:00 $
 */
public interface I_CmsWpConstants {

	/** Parameter for creating projects: copy content to project */
	public static final String C_CONTENTPATH = "/content/";
    
    /** Parameter for content templates folder */
	public static final String C_CONTENTTEMPLATEPATH = C_CONTENTPATH+"templates/";
    
    /** Parameter for content body folder */
	public static final String C_CONTENTBODYPATH = C_CONTENTPATH+"bodys/";
	
	// Parameters that are used in html requests
	
    /** Parameter for foldername  */
    public static final String C_PARA_FOLDER="folder";
   
    /** Parameter for filelist  */
    public static final String C_PARA_FILELIST="filelist";

    /** Parameter for url */
    public static final String C_PARA_URL="URL";
    
    /** Parameter for previous filelist  */
    public static final String C_PARA_PREVIOUSLIST="previous";

    /** Parameter for viewfile  */
    public static final String C_PARA_VIEWFILE="viewfile";
    
    /** Parameter for view name */
    public static final String C_PARA_VIEW = "view";
    
    /** Parameter for page number */
    public static final String C_PARA_PAGE = "page";
    
    /** Parameter for filter */
    public static final String C_PARA_FILTER = "filter";    
    
    /** Parameter for maximum pages */
    public static final String C_PARA_MAXPAGE = "maxpage";    

    /** Parameter for locking pages */
    public static final String C_PARA_LOCK = "lock";  

    /** Parameter for unlocking pages */
    public static final String C_PARA_UNLOCK = "unlock"; 
    
    /** Parameter for a filename */
    public static final String C_PARA_FILE = "file";  

    /** Parameter for a deleting a file */
    public static final String C_PARA_DELETE = "delete";
    
    /** Parameter for a name*/
    public static final String C_PARA_NAME = "name";  
    
    /** Parameter for a new file*/
    public static final String C_PARA_NEWFILE = "newfile";  
 
    /** Parameter for a new folder*/
    public static final String C_PARA_NEWFOLDER = "newfolder";  
    
    /** Parameter for a flag */
    public static final String C_PARA_FLAGS = "flags";  

    /** Parameter for a title */
    public static final String C_PARA_TITLE = "title";  

    /** Parameter for a template */
    public static final String C_PARA_TEMPLATE = "template";
    
    /** Parameter for a navigation title */
    public static final String C_PARA_NAVTITLE = "navtitle";
    
    /** Parameter for a navigation position */
    public static final String C_PARA_NAVPOS = "navpos";  
    
    // Filenames of workplace files
    
    /** prefix for temporary files */
    public static final String C_WP_TEMP_PREFIX = "~";
    
	/** The filename to the icontemplate */
	public static final String C_ICON_TEMPLATEFILE = "icontemplate";
	
	/** The filename to the projectlisttemplate */
	public static final String C_PROJECTLIST_TEMPLATEFILE = "projecttemplate";
	
	/** The filename to the projectlisttemplate */
	public static final String C_TASKLIST_TEMPLATEFILE = "tasklisttemplate";
	
	/** The filename to the projectlisttemplate */
	public static final String C_CONTEXTMENUE_TEMPLATEFILE = "contexttemplate";
	
	/** The explorer tree. */
    public static final String C_WP_EXPLORER_TREE="explorer_tree.html";

    /** The explorer file list. */
    public static final String C_WP_EXPLORER_FILELIST="explorer_files.html";    
    
    // Filenames of special templates
    
    /** Name of the template containing button definitions */
    public static final String C_BUTTONTEMPLATE = "ButtonTemplate";
 
    /** Name of the template containing label definitions */
    public static final String C_LABELTEMPLATE = "labelTemplate";
    
     /**
     *  Name of the template containing input field definitions
     */
    public static final String C_INPUTTEMPLATE = "inputTemplate";
    
     /**
     *  Name of the template containing error field definitions
     */
    public static final String C_ERRORTEMPLATE = "errorTemplate";    

     /**
     *  Name of the template containing messagebox definitions
     */
    public static final String C_BOXTEMPLATE = "messageboxTemplate";    

     /**
     *  Name of the template containing radiobutton definitions
     */
    public static final String C_RADIOTEMPLATE = "radioTemplate";    
    
    // tag defnitions
	
   /** Name of the label tag in the label definition template */
   public static final String C_TAG_PROJECTLIST_DEFAULT = "defaultprojectlist";   
    
   /** Name of the label tag in the label definition template */
   public static final String C_TAG_PROJECTLIST_SNAPLOCK = "snaplock";   
   
   /** Name of the label tag in the label definition template */
   public static final String C_TAG_LABEL="label";
   
   /** Name of the label tag in the input definiton template */
   public static final String C_TAG_INPUTFIELD="inputfield";
    
   /** Name of the password tag in the input definiton template */
   public static final String C_TAG_PASSWORD="password";
   
   /** Name of the startup tag in the input definiton template */
   public static final String C_TAG_STARTUP="STARTUP";
      
    /** Name of the submitbutton tag in the button definiton template */
   public static final String C_TAG_SUBMITBUTTON="submitbutton";

    /** Name of the errorbox tag in the error definiton template */
   public static final String C_TAG_ERRORBOX="errorbox";

    /** Name of the errorbox tag in the error definiton template */
   public static final String C_TAG_MESSAGEBOX="messagepage";
   
   /**
    * Name of the select start tag in the input definiton template
    */
   public static final String C_TAG_SELECTBOX_START="selectbox.start";

   /**
    * Name of the select end tag in the input definiton template
    */
   public static final String C_TAG_SELECTBOX_END="selectbox.end";
   
   /**
    * Name of the selectbox "class" option tag in the input definiton template
    */
   public static final String C_TAG_SELECTBOX_CLASS="selectbox.class";
   
   /**
    * Name of the (select) option tag in the input definiton template
    */
   public static final String C_TAG_SELECTBOX_OPTION="selectbox.option";

   /**
    * Name of the (select) selected option tag in the input definiton template
    */
   public static final String C_TAG_SELECTBOX_SELOPTION="selectbox.seloption";
      
    /**
    * Name if the error´page tag in the error definiton template
    */
   public static final String C_TAG_ERRORPAGE="errorpage";
   
   
    // Parameters for buttons
    
    /** Name of the button */
    public static final String C_BUTTON_NAME = "name";
    
    /** Action for the button */
    public static final String C_BUTTON_ACTION = "action";
    
    /** Alt text of the button */
    public static final String C_BUTTON_ALT = "alt";
    
    /** href text of the button */
    public static final String C_BUTTON_HREF = "href";

    /** Value of the button */
    public static final String C_BUTTON_VALUE = "value";
    
    /** Style of the button */
    public static final String C_BUTTON_STYLE = "class";
    
    /** width of the button */
    public static final String C_BUTTON_WIDTH = "width";
    
    // Parameters for icons
    
    /** Name of the icon */
    public static final String C_ICON_NAME = "name";
    
    /** Action for the icon */
    public static final String C_ICON_ACTION = "action";

   /** Label of the icon */
    public static final String C_ICON_LABEL = "label";
    
    /** href text of the icon */
    public static final String C_ICON_HREF = "href";

    /** href target of the icon */
    public static final String C_ICON_TARGET = "target";
    
    // Parameters for labels

    /** Name of the value */
    public static final String C_LABEL_VALUE = "value";
    
    // Parameters for radiobuttons
  
    /** Name of the radio buttons */
    public static final String C_RADIO_RADIONAME = "radioname";
    
    /** Name of the radio button value */
    public static final String C_RADIO_NAME = "name";

    /** Name of the radio button link */
    public static final String C_RADIO_LINK = "link";
  
    /** Name of the radio button image */
    public static final String C_RADIO_IMAGE = "image";
    
    /** Method of the radio buttons */
    public static final String C_RADIO_METHOD = "method";

    
    
    // Parameters for input fields
    
    /** Name of the input field */
    public static final String C_INPUT_NAME = "name";
    
    /** Style class of the input field  */
    public static final String C_INPUT_CLASS = "class";

    /**  Size of the input field  */
    public static final String C_INPUT_SIZE = "size";
    
    /**  Length of the input field  */
    public static final String C_INPUT_LENGTH = "length";    

    /**  Value of the input field  */
    public static final String C_INPUT_VALUE = "value";    
    
    /**  Method of the input field  */
    public static final String C_INPUT_METHOD = "method";    
    
    /**  Action of the input field  */
    public static final String C_INPUT_ACTION = "action";    
    
    /**  Method of the projectlist field  */
    public static final String C_PROJECTLIST_METHOD = "method";    
	
    /**  Method of the projectlist field  */
    public static final String C_PROJECTLIST_IDX = "idx";    
	
    /**  Method of the projectlist field  */
    public static final String C_PROJECTLIST_MENU = "menu";    
	
    /**  Method of the projectlist field  */
    public static final String C_PROJECTLIST_LOCKSTATE = "lockstate";    
	
    /**  Method of the projectlist field  */
    public static final String C_PROJECTLIST_NAME = "name";    
	
    /**  Method of the projectlist field  */
    public static final String C_PROJECTLIST_DESCRIPTION = "description";
	
    /**  Method of the projectlist field  */
    public static final String C_PROJECTLIST_STATE = "STATE";    
	
    /**  Method of the projectlist field  */
    public static final String C_PROJECTLIST_STATE_LOCKED = "project.state.FILESLOCKED";    

    /**  Method of the projectlist field  */
    public static final String C_PROJECTLIST_STATE_UNLOCKED = "project.state.FILESUNLOCKED";    
	
    /**  Method of the projectlist field  */
    public static final String C_PROJECTLIST_PROJECTMANAGER = "projectmanager";    
	
    /**  Method of the projectlist field  */
    public static final String C_PROJECTLIST_PROJECTWORKER = "projectworker";
	
    /**  Method of the projectlist field  */
    public static final String C_PROJECTLIST_DATECREATED = "datecreated";    
	
    /**  Method of the projectlist field  */
    public static final String C_PROJECTLIST_OWNER = "owner";    
	
    // Parameters for error boxes and error pages
    
    /** Title of the error box */
    public static final String C_ERROR_TITLE = "title";
    
    /** Message of the error box */
    public static final String C_ERROR_MESSAGE = "message";

     /** Reason of the error box */
    public static final String C_ERROR_REASON = "reason";
    
     /** Suggestion of the error box */
    public static final String C_ERROR_SUGGESTION = "suggestion";    

    /** Link of the error box */
    public static final String C_ERROR_LINK = "ref";    

    /** Static text in the error box */
    public static final String C_ERROR_MSG_REASON = "msgreason";    
   
    /** Button label of the error box */
    public static final String C_ERROR_MSG_BUTTON = "msgbutton";    

    // Parameters for error boxes and error pages
    
    /** Title of the messagebox */
    public static final String C_MESSAGE_TITLE = "title";
 
    /** First message of the messagebox */
    public static final String C_MESSAGE_MESSAGE1 = "message1";

    /** Second message of the messagebox */
    public static final String C_MESSAGE_MESSAGE2 = "message2";
    
    /** First button of the messagebox */
    public static final String C_MESSAGE_BUTTON1 = "button1";
    
    /** Second button of the messagebox */
    public static final String C_MESSAGE_BUTTON2 = "button2";
    
    /** Link on button1 of the messagebox */
    public static final String C_MESSAGE_LINK1 = "link1";
  
    /** Link on button2 of the messagebox */
    public static final String C_MESSAGE_LINK2 = "link2";
	
	/** Link on button2 of the messagebox */
    public static final String C_MESSAGE_FILENAME = "filename";
    
    // Parameters for select boxes
    
    /** Name of the select box */
    public static final String C_SELECTBOX_NAME = "name";
    
    /** Size of the select box */
    public static final String C_SELECTBOX_SIZE = "size";
	
    /** Stylesheet class string of the select box */
    public static final String C_SELECTBOX_CLASS = "class";

    /** Stylesheet class name of the select box */
    public static final String C_SELECTBOX_CLASSNAME = "classname";
    
    /** Width of the select box */
    public static final String C_SELECTBOX_WIDTH = "width";

    /** Onchange of the select box */
    public static final String C_SELECTBOX_ONCHANGE = "onchange";
    
    /** Method of the select box */
    public static final String C_SELECTBOX_METHOD = "method";
    
    /** option name of the select box */
    public static final String C_SELECTBOX_OPTIONNAME = "name";
    
    /** option value of the select box */
    public static final String C_SELECTBOX_OPTIONVALUE = "value";
        
    /** option values for font select boxes */
    public static final String[] C_SELECTBOX_FONTS = 
            { "Arial", "Arial Narrow", "System", "Times New Roman", "Verdana" };

    /** option values for font select boxes */
    public static final String[] C_SELECTBOX_FONTSTYLES = 
            { "Normal", "Überschrift 1", "Überschrift 2", "Überschrift 3", "Überschrift 4", 
              "Überschrift 5", "Überschrift 6" };

    /** option values for font select boxes */
    public static final String[] C_SELECTBOX_FONTSIZES = 
            { "1", "2", "3", "4", "5", "6", "7"};

    /** option values for editor view select boxes */
    public static final String[] C_SELECTBOX_EDITORVIEWS = 
            { "edithtml", "edit"};

    /** classes of the different option values for editor view select boxes */
    public static final String[] C_SELECTBOX_EDITORVIEWS_CLASSES = 
            { "com.opencms.workplace.CmsEditor", "com.opencms.workplace.CmsEditor"};

    /** templates of the different option values for editor view select boxes */
    public static final String[] C_SELECTBOX_EDITORVIEWS_TEMPLATES = 
            { "htmledit", "textedit"};
        
    /** default selected option value for editor view select boxes */
    public static final int C_SELECTBOX_EDITORVIEWS_DEFAULT = 0;
    
    // Parameters for file list
    
    /** method value of the file list */
    public static final String C_FILELIST_METHOD ="method";
    
    /** template value for the file list */
    public static final String C_FILELIST_TEMPLATE="template";
    
    

    // Constants for language file control
            
    /** Prefix for button texts in the language file */
    public static final String C_LANG_BUTTON = "button";
    
    /** Prefix for button texts in the language file */
    public static final String C_LANG_ICON = "icon";
	
    // Constants for user default preferences
    
    /** Number of images to be shown per page in the picture browser */
    public static final int C_PICBROWSER_MAXIMAGES = 20;
    
    /** Name of the filelist preferences */
    public static final String C_USERPREF_FILELIST ="filelist";
    
    /** Flag for displaying the title column */
    public static final int C_FILELIST_TITLE = 1;
    
    /** Flag for displaying the filetype column */
    public static final int C_FILELIST_TYPE = 2;
    
    /** Flag for displaying the changed column */
    public static final int C_FILELIST_CHANGED = 4;
    
    /** Flag for displaying the size column */
    public static final int C_FILELIST_SIZE = 8;
    
    /** Flag for displaying the state column */
    public static final int C_FILELIST_STATE = 16;
    
    /** Flag for displaying the owner column */
    public static final int C_FILELIST_OWNER = 32;
    
    /** Flag for displaying the group column */
    public static final int C_FILELIST_GROUP = 64;
    
    /** Flag for displaying the access column */
    public static final int C_FILELIST_ACCESS = 128;
    
    /** Flag for displaying the locked column */
    public static final int C_FILELIST_LOCKED = 256;
    
	/** Parameter of projectnew */    
	public static final String C_PROJECTNEW_NAME = "NAME";

	/** Parameter of projectnew */    
	public static final String C_PROJECTNEW_GROUP = "GROUP";

	/** Parameter of projectnew */    
	public static final String C_PROJECTNEW_DESCRIPTION = "DESCRIPTION";

	/** Parameter of projectnew */    
	public static final String C_PROJECTNEW_MANAGERGROUP = "MANAGERGROUP";

	/** Parameter of projectnew */    
	public static final String C_PROJECTNEW_FOLDER = "selectallfolders";

	/** Templateselector of projectnew */    
	public static final String C_PROJECTNEW_ERROR = "error";

	/** Templateselector of projectnew */    
	public static final String C_PROJECTNEW_DONE = "done";
	
	/**	Taskparameter */
	public static final String C_TASKPARA_ACCEPTATION = "acceptation";

	/**	Taskparameter */
	public static final String C_TASKPARA_ALL = "all";

	/**	Taskparameter */
	public static final String C_TASKPARA_COMPLETION = "completion";

	/**	Taskparameter */
	public static final String C_TASKPARA_DELIVERY = "delivery";

	/** Constant for session-key */
	public static final String C_SESSION_TASK_PROJECTNAME = "task_projectname";

	/** Constant for session-key */
	public static final String C_SESSION_TASK_FILTER = "task_filter";
}

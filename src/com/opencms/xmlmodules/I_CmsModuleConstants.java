/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/xmlmodules/Attic/I_CmsModuleConstants.java,v $
 * Date   : $Date: 2000/07/11 15:01:18 $
 * Version: $Revision: 1.2 $
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

package com.opencms.xmlmodules;

/**
 * This interface is a pool for constants used for all XML-based modules. 
 * All module classes have to implement this class to get access to this constants.
 * 
 * @author Matthias Schreiber
 * @version $Revision: 1.2 $ $Date: 2000/07/11 15:01:18 $
 */
public interface I_CmsModuleConstants {

	/** Path to the root of all XML modules */
    public static final String C_XMLMODULE_PATH = "/system/xmlmodules/"; 
	
	/** Path to the news configuration file */
    public static final String C_XMLMODULE_INIFILE = "module.ini";
	
    /** Text of the "active" state */
    public static final String C_STATE_ACTIVE = "active";     

    /** Text of the "inactive" state */
    public static final String C_STATE_INACTIVE = "inactive"; 
	
	/** Name of XML-Tag containing the date */
    public static final String C_XML_DATE = "date";   
	
	/** Name of XML-Tag containing text or description */
    public static final String C_XML_TEXT = "text";   
	
	/** Name of XML-Tag containing the headline */
    public static final String C_XML_HEADLINE = "headline";   
	
	/** Name of XML-Tag containing the author */
    public static final String C_XML_AUTHOR = "author";   
    
	/** Default protocol for the URL of the external link */   
	public final static String C_DEF_PROTOCOL="http://"; 
    
    // Names for parameters used in session storage and
    // in HTTP requests.
    
    /** Name of the file parameter in the URL and the session strage. */
    public static final String C_PARAM_FILE = "file";

    /** Name of the state parameter in the HTTP get request and the session strage. */
    public static final String C_PARAM_STATE = "state";    

    /** Name of the action parameter in URL and the session strage.*/
    public static final String C_PARAM_ACTION = "action";    

    /** Name of the read parameter in the page file. */
    public static final String C_PARAM_READ = "read"; 
	
	/** Name of the folder parameter in the page file. */
    public static final String C_PARAM_FOLDER = "folder";  
	
	/** Maximum number of links to articles displayed on one WML deck. */
    public static final String C_WAP_LINKSPERDECK = "4";   
	
	/** Default navigation deck index */
    public final static String C_WAP_FIRSTDECK = "0";
	
	/** Default title for WML decks */
    public final static String C_WAP_DECKTITLE = "OpenCms created";
	
	/** XML tag used for the paragraph separator definition */
    public final static String C_TAG_PARAGRAPHSEP = "paragraphsep";
    
	/** XML tag used for the wml navigation definition */
    public final static String C_TAG_WMLNAV = "wmlnav";
	
	/** XML tag used for the wml article definition */
    public final static String C_TAG_WMLARTICLE = "wmlarticle";
	
	/** XML tag used for user-defined linktext to next WML deck */
    public final static String C_TAG_WMLLINKLABEL = "linklabel";
	
	/** Default text for a link to the next WML deck */
    public final static String C_DEF_WMLLINKLABEL = "mehr";
	
	 /** Filelist datablock for news state value */
    public final static String C_STATE_VALUE = "MODULE_STATE_VALUE";

    /** Filelist datablock for news author value */
    public final static String C_AUTHOR_VALUE = "MODULE_AUTHOR_VALUE";

    /** Definition of the Datablock NEW */   
    public final static String C_NEW="NEW";
	
	 /** Template selector of the "done" page */
    public static final String C_DONE = "done";
     
    /** Definition of the Datablock NEW_ENABLED */   
    public final static String C_NEW_ENABLED="NEW_ENABLED";    

    /** Definition of the Datablock NEW_DISABLED */   
    public final static String C_NEW_DISABLED="NEW_DISABLED"; 
	
}

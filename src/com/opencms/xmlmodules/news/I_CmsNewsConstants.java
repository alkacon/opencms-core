
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/xmlmodules/news/Attic/I_CmsNewsConstants.java,v $
* Date   : $Date: 2001/01/24 09:43:48 $
* Version: $Revision: 1.4 $
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

package com.opencms.xmlmodules.news;

import com.opencms.xmlmodules.I_CmsModuleConstants;

/**
 * This interface is a pool for constants used in the news module. 
 * All news classes may implement this class to get access to this constants.
 * <P>
 * Some of these constants may be displaced here and should later be put
 * into a <code>news.ini</code> file.
 * 
 * @author Matthias Schreiber
 * @version $Revision: 1.4 $ $Date: 2001/01/24 09:43:48 $
 */
public interface I_CmsNewsConstants extends I_CmsModuleConstants {
    
    /** Name of the current XML module */
    public static final String C_XMLMODULE_NAME = "news";
    
    /** Path to the news configuration file */
    public static final String C_INI_FILE = C_XMLMODULE_PATH + "news/module.ini";
    
    /** Folder for news content XML files */
    public static final String C_FOLDER_CONTENT = "/content/news/";
    
    /** Folder for correspondig pages to display news */
    public static final String C_FOLDER_PAGE = "/news/";
    
    /** XML tag used for the news list entry definition */
    public final static String C_TAG_LISTENTRY = "newslistentry";
    
    /** Name of the default bodytemplate file used for news */
    public static final String C_BODYTEMPLATE_FILE = "newsModuleTemplate";
    
    /** Class for processing the bodytemplate */
    public static final String C_BODYTEMPLATE_CLASS = "com.opencms.xmlmodules.news.CmsNewsContent";
    
    /** Special tags for the news-module application */
    
    /** Name of the shorttext XML tag */
    public static final String C_XML_SHORTTEXT = "shorttext";
    
    /** Name of the external link XML tag */
    public static final String C_XML_EXTLINK = "extlink";
}

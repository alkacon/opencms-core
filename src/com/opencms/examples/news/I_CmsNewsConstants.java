/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/examples/news/Attic/I_CmsNewsConstants.java,v $
 * Date   : $Date: 2000/05/02 15:58:37 $
 * Version: $Revision: 1.8 $
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

package com.opencms.examples.news;

/**
 * This interface is a pool for constants used in the news examples. 
 * All news classes may implement this class to get access to this constants.
 * <P>
 * Some of these constants may be displaced here and should later be put
 * into a <code>news.ini</code> file.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.8 $ $Date: 2000/05/02 15:58:37 $
 */
public interface I_CmsNewsConstants {
    
    /** Path to the news configuration file */
    public static final String C_NEWS_INI = "/news/news.ini";
    
    /** Folder for news content XML files */
    public static final String C_NEWS_FOLDER_CONTENT = "/content/news/";
    
    /** Folder for correspondig pages to display news */
    public static final String C_NEWS_FOLDER_PAGE = "/news/";                            

    /** Name of the news list definition file */
    public static final String C_NEWS_NEWSLISTDEF = "newsListDefinition";     

    /** Text of the "active" state */
    public static final String C_NEWS_STATE_ACTIVE = "active";     

    /** Text of the "inactive" state */
    public static final String C_NEWS_STATE_INACTIVE = "inactive";     
    
    
    // Names for XML elements in news file of the content type
    // CmsNewsTemplateFile
    
    /** Name of the author XML tag */
    public static final String C_NEWS_XML_AUTHOR = "author";     
    
    /** Name of the date XML tag */
    public static final String C_NEWS_XML_DATE = "date";     

    /** Name of the headline XML tag */
    public static final String C_NEWS_XML_HEADLINE = "headline";     

    /** Name of the shorttext XML tag */
    public static final String C_NEWS_XML_SHORTTEXT = "shorttext";     

    /** Name of the text XML tag */
    public static final String C_NEWS_XML_TEXT = "text";     

    /** Name of the external link XML tag */
    public static final String C_NEWS_XML_EXTLINK = "extlink";         

    /** Name of the XML tag for "news active"*/
    public static final String C_NEWS_XML_ACTIVE = "active";         

    /** Name of the XML tag for "news inactive"*/
    public static final String C_NEWS_XML_INACTIVE = "inactive";
    
    
    // Names for news parameters used in session storage and
    // in HTTP requests.
    
    /** Name of the file parameter in the URL and the session strage. */
    public static final String C_NEWS_PARAM_FILE = "file";

    /** Name of the date parameter in the HTTP get request and the session strage. */
    public static final String C_NEWS_PARAM_DATE = "date";

    /** Name of the headline parameter in the HTTP get request and the session strage. */
    public static final String C_NEWS_PARAM_HEADLINE = "headline";
    
    /** Name of the shorttext parameter in the HTTP get request and the session strage. */
    public static final String C_NEWS_PARAM_SHORTTEXT = "shorttext";

    /** Name of the text parameter in the HTTP get request and the session strage. */
    public static final String C_NEWS_PARAM_TEXT = "text";
    
    /** Name of the external link parameter in the HTTP get request and the session strage. */
    public static final String C_NEWS_PARAM_EXTLINK = "extlink";

    /** Name of the state parameter in the HTTP get request and the session strage. */
    public static final String C_NEWS_PARAM_STATE = "state";    

    /** Name of the action parameter in URL and the session strage.*/
    public static final String C_NEWS_PARAM_ACTION = "action";    

    /** Name of the author parameter in session storage */
    public static final String C_NEWS_PARAM_AUTHOR = "author";        

    /** Name of the newsfolder parameter in the newspage file. */
    public static final String C_NEWS_PARAM_NEWSFOLDER = "newsfolder";    

    /** Name of the read parameter in the newspage file. */
    public static final String C_NEWS_PARAM_READ = "read";        
}

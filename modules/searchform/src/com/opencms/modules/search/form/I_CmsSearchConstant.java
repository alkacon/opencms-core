/*
* File   : $Source: /alkacon/cvs/opencms/modules/searchform/src/com/opencms/modules/search/form/Attic/I_CmsSearchConstant.java,v $
* Date   : $Date: 2002/02/19 10:19:55 $
* Version: $Revision: 1.1 $
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
package com.opencms.modules.search.form;

/**
 * Common interface only for the CmsSearchForm class.
 * Define all Constant for the CmsSearchForm class.
 *
 * @author    Markus Fabritius
 * @version $Revision: 1.1 $ $Date: 2002/02/19 10:19:55 $
 */
public interface I_CmsSearchConstant {

public static final String C_PARAM_SESSION_SORT = "searchengineSort";
public static final String C_PARAM_SESSION_METHOD = "searchengineMethod";
public static final String C_PARAM_SESSION_FORMAT = "searchengineFormat";
public static final String C_PARAM_SESSION_WORD = "searchengineName";
public static final String C_PARAM_SESSION_RESTRICT = "searchengineRestrict";
public static final String C_PARAM_SESSION_MATCH_PER_PAGE = "searchengineMatchPerPage";

public static final String C_PARAM_SEARCHFORM_SORT = "sort";
public static final String C_PARAM_SEARCHFORM_METHOD = "method";
public static final String C_PARAM_SEARCHFORM_FORMAT = "format";
public static final String C_PARAM_SEARCHFORM_WORD = "words";
public static final String C_PARAM_SEARCHFORM_RESTRICT = "restrict";
public static final String C_PARAM_SEARCHFORM_MATCH_PER_PAGE = "matchesperpage";
public static final String C_PARAM_SEARCHFORM_PAGE = "page";

public static final String C_PARAM_MODULE_NAME = "com.opencms.modules.search.form";
public static final String C_PARAM_SEARCH_MODUL = "SearchModul";
public static final String C_PARAM_SERVERPATH = "Serverpath";
public static final String C_PARAM_CONFIGURATION_FILE = "ConfigurationFile";
public static final String C_PARAM_AREA_FIELD = "AreaField";
public static final String C_PARAM_AREA_SECTION = "AreaSection";
public static final String C_PARAM_MATCH_PER_PAGE = "MatchPerPage";
public static final String C_PARAM_NAVIGATION_RANGE = "NavigationRange";
public static final String C_PARAM_PARSING_EXCERPT_URL = "ParsingExcerptUrl";

public static final String C_PARAM_ERROR_NOWORD = "WordMissing";
public static final String C_PARAM_ERROR_SERVERPATH = "ServerNotFound";
public static final String C_PARAM_ERROR_FILE_TOPIC = "TopicFileError";
public static final String C_PARAM_ERROR_REFLECTION = "CallingError";

public static final String C_PARAM_SEARCHENGINE_TOKEN = ",";

}
/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/genericSql/Attic/I_CmsQuerys.java,v $
 * Date   : $Date: 2000/06/06 14:52:15 $
 * Version: $Revision: 1.3 $
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

package com.opencms.file.genericSql;

/**
 * This interface is defines all queries used in the DB-Access class.  
 * @author Michael Emmerich
 * 
 * @version $Revision: 1.3 $ $Date: 2000/06/06 14:52:15 $
 */
public interface I_CmsQuerys {
    
    // Common constants   
	static String C_DATABASE_PREFIX = "CMS_";

    // Constants for Groups table
    public static final String C_GROUPS_GROUP_ID="GROUP_ID";
    public static final String C_GROUPS_PARENT_GROUP_ID="PARENT_GROUP_ID";
    public static final String C_GROUPS_GROUP_NAME="GROUP_NAME"; 
    public static final String C_GROUPS_GROUP_DESCRIPTION="GROUP_DESCRIPTION";      
    public static final String C_GROUPS_GROUP_FLAGS="GROUP_FLAGS";  
                         
    // Constants for Groups
    public static final Integer C_GROUPS_READGROUP_KEY = new Integer(200);
    public static final String C_GROUPS_READGROUP = "SELECT * FROM " + C_DATABASE_PREFIX 
                                                  + "GROUPS WHERE GROUP_NAME = ?";
	
	// Constants for Projects
	
    public static final Integer C_PROJECTS_MAXID_KEY = new Integer(400);
	public static final String C_PROJECTS_MAXID = "SELECT MAX(PROJECT_ID) FROM " + C_DATABASE_PREFIX + "PROJECTS";	
	
}


/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/genericSql/Attic/I_CmsQuerys.java,v $
 * Date   : $Date: 2000/06/06 14:16:24 $
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

package com.opencms.file.genericSql;

/**
 * This interface is defines all queries used in the DB-Access class.  
 * @author Michael Emmerich
 * 
 * @version $Revision: 1.1 $ $Date: 2000/06/06 14:16:24 $
 */
public interface I_CmsQuerys {
    
    // Common constants
       
	static String C_DATABASE_PREFIX = "CMS_";
        
    // Constants for Groups
 
    public static final Integer C_GROUPS_READGROUP_KEY = new Integer(200);
    
    public static final String C_GROUPS_READGROUP = "SELECT * FROM " + C_DATABASE_PREFIX 
                                                  + "GROUPS WHERE GROUP_NAME = ?";
	
}


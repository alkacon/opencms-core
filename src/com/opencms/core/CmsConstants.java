package com.opencms.core;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/core/Attic/CmsConstants.java,v $
 * Date   : $Date: 2000/09/18 08:25:02 $
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

/**
* Abstract class holding constants for the OpenCms system.
* <p>
* This class should substitute the I_CmsConstants, since there is no need to implement an interface holding only constants.
* The Class is abstract since there is no need to instantiate it. 
*   
* @author Anders Fugmann
* @version $Revision: 1.4 $ $Date: 2000/09/18 08:25:02 $  
* 
*/
public abstract class CmsConstants {
	/**
  * USE_MULTISITE defines weather or not to use the new multisite functionality.
  * This variable will be removed when multisite is fully functional.
  */
	public static boolean USE_MULTISITE = true;
}

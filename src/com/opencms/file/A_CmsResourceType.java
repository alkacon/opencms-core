/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/A_CmsResourceType.java,v $
 * Date   : $Date: 2000/02/15 17:43:59 $
 * Version: $Revision: 1.5 $
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

package com.opencms.file;

import java.util.*;
import java.io.*;

/**
 * This abstract class describes a resource-type. To determine the special launcher 
 * for a resource this resource-type is needed.
 * 
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @version $Revision: 1.5 $ $Date: 2000/02/15 17:43:59 $
 */
abstract public class A_CmsResourceType implements Serializable {	
	
	/**
	 * Returns the type of this resource-type.
	 * 
	 * @return the type of this resource-type.
	 */
	abstract int getResourceType();
    
     /**
	 * Returns the launcher type needed for this resource-type.
	 * 
	 * @return the launcher type for this resource-type.
	 */
	abstract int getLauncherType();
	
	/**
	 * Returns the name for this resource-type.
	 * 
	 * @return the name for this resource-type.
	 */
	abstract public String getResourceName();
    
     /**
	 * Returns the name of the Java class loaded by the launcher.
	 * This method returns <b>null</b> if the default class for this type is used.
	 * 
	 * @return the name of the Java class.
	 */
	abstract public String getLauncherClass();
	
	/**
	 * Returns a string-representation for this object.
	 * This can be used for debugging.
	 * 
	 * @return string-representation for this object.
	 */
	abstract public String toString();
}

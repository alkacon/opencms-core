/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/A_CmsPropertydefinition.java,v $
 * Date   : $Date: 2000/04/03 10:48:29 $
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

package com.opencms.file;

/**
 * This abstract class describes a Propertydefinition in the Cms.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 2000/04/03 10:48:29 $
 */
abstract public class A_CmsPropertydefinition {
	/**
	 * Returns the name of this Propertydefinition.
	 * 
	 * @return name The name of the Propertydefinition.
	 */
	abstract public String getName();
	
	/**
	 * Returns the id of a Propertydefinition. This method has the package-visibility.
	 * 
	 * @return id The id of this Propertydefinition.
	 */
	abstract int getId();
	
	/**
	 * Gets the resourcetype for this Propertydefinition.
	 * 
	 * @return the resourcetype of this Propertydefinition.
	 */
	abstract public int getType();

	/**
	 * Gets the type for this Propertydefinition.
	 * The type may be C_PROPERTYDEF_TYPE_NORMAL, C_PROPERTYDEF_TYPE_OPTIONAL or
	 * C_PROPERTYDEF_TYPE_MANDATORY.
	 * 
	 * @return the type of this Propertydefinition.
	 */
	abstract public int getPropertydefType();
	
	/**
	 * Sets the type for this Propertydefinition.
	 * The type may be C_PROPERTYDEF_TYPE_NORMAL, C_PROPERTYDEF_TYPE_OPTIONAL or
	 * C_PROPERTYDEF_TYPE_MANDATORY.
	 * 
	 * @param type The new type fot this Propertydefinition.
	 */
	abstract public void setPropertydefType(int type);
	
	/**
	 * Returns a string-representation for this object.
	 * This can be used for debugging.
	 * 
	 * @return string-representation for this object.
	 */
	abstract public String toString();
	
	/**
	 * Compares the overgiven object with this object.
	 * 
	 * @return true, if the object is identically else it returns false.
	 */
	abstract public boolean equals(Object obj);

}

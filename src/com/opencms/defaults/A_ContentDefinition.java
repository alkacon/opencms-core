package com.opencms.defaults;


/*
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
import com.opencms.template.*;
import com.opencms.file.*;
 
import java.util.*;
import java.lang.reflect.*;

/**
 * Abstract class for the content definition
 * Creation date: (27.10.00 10:04:42)
 * author: Michael Knoll
 * version 1.0
 */
public abstract class A_ContentDefinition implements I_CmsContent{

/**
 * applies the filter method
 * @returns an Vector containing the method 
 */
public static Vector applyFilter(CmsObject cms, FilterMethod filterMethod) {
	return applyFilter(cms, filterMethod, null);
}
/**
 * applies the filter through the method object and the user parameters
 * @returns a vector with the filtered content 
 */
public static Vector applyFilter(CmsObject cms, FilterMethod filterMethod, String userParameter) {
	
	Vector retValue = new Vector();
	try {
		Method method = filterMethod.getFilterMethod();
		Object[] params;
		if( userParameter != null ) {
			int defaultParameterLength = filterMethod.getDefaultParameter().length;
			Object[] allParams = new Object[defaultParameterLength + 1];
			System.arraycopy(filterMethod.getDefaultParameter(), 0, allParams, 0, defaultParameterLength);
			allParams[defaultParameterLength] = userParameter;
			params = allParams;
		} else {
			params = filterMethod.getDefaultParameter();
		}
		return (Vector) method.invoke(null,params);
	} catch (InvocationTargetException ite) {
		System.err.println("A_ContentDefinition applyFilter: InvocationTargetException!");
		ite.getTargetException().printStackTrace();
	} catch (Exception e) {
		System.err.println("A_ContentDefinition applyFilter: Other Exception!");
		e.printStackTrace();
	}	
	return retValue;
}
/**
 * abstract delete method
 * for delete instance of content definition
 * must be overwritten in your content definition
 */
public abstract void delete(CmsObject cms) throws Exception;
/**
 * Gets the getXXX methods
 * You have to override this method in your content definition.
 * @returns a Vector with the filed methods.
 */
public  static Vector getFieldMethods(CmsObject cms) {
	return new Vector();
}
/**
 * Gets the headlines of the table
 * You have to override this method in your content definition.
 * @returns a Vector with the colum names.
 */
public static Vector getFieldNames(CmsObject cms) {
	return new Vector();
}
/**
 * Gets the filter methods.
 * You have to override this method in your content definition.
 * @returns a Vector of FilterMethod objects containing the methods, names and default parameters
 */
public static Vector getFilterMethods(CmsObject cms) {
	return new Vector();
}
/**
 * Gets the lockstates 
 * You have to override this method in your content definition, if you have overwritten
 * the isLackable method with true.
 * @returns a String with the lockstate
 */
public String getLockstate() {
	return null;
}
/**
 * gets the unique Id of a content definition instance
 * @returns a string with the Id 
 */
public abstract String getUniqueId(CmsObject cms) ;
/**
 * if the content definition objects should be lockable
 * this method has to be overwritten with value true
 * @returns a boolean 
 */
public static boolean isLockable() {
	return false;
}
/**
 *Sets the lockstates 
 * You have to override this method in your content definition, 
 * if you have overwritten the isLockable method with true.
 * @sets the lockstate for the actual entry
 */
public void setLockstate(String lockstate) {

}
/**
 * abstract write method
 * must be overwritten in content definition
 */
public abstract void write(CmsObject cms) throws Exception;
}

package com.opencms.defaults;

/*
 *
 * Copyright (C) 2000  The OpenCms Group
 *
 * This File is part of OpenCms -
 * the Open Source Content Mananagement
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
public abstract class A_CmsContentDefinition implements I_CmsContent{

/**
 * applies the filter method
 * @returns an Vector containing the method
 */
public static Vector applyFilter(CmsObject cms, CmsFilterMethod filterMethod) throws Exception {
	return applyFilter(cms, filterMethod, null);
}

/**
 * applies the filter through the method object and the user parameters
 * @returns a vector with the filtered content
 */
public static Vector applyFilter(CmsObject cms, CmsFilterMethod filterMethod, String userParameter) throws Exception {
	Method method = filterMethod.getFilterMethod();
	Object[] defaultParams = filterMethod.getDefaultParameter();
	Vector allParameters = new Vector();
	Object[] allParametersArray;
	Class[] paramTypes = method.getParameterTypes();

	if( (paramTypes.length > 0) && (paramTypes[0] == CmsObject.class) ) {
		allParameters.addElement(cms);
	}

	for(int i = 0; i < defaultParams.length; i++) {
		allParameters.addElement(defaultParams[i]);
	}

	if (filterMethod.hasUserParameter()) {
		allParameters.addElement(userParameter);
	}

	allParametersArray = new Object[allParameters.size()];
	allParameters.copyInto(allParametersArray);

	return (Vector) method.invoke(null, allParametersArray);
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
 * the isLockable method with true.
 * @returns a String with the lockstate
 */
public int getLockstate() {
	return -1;
}
/**
 * gets the unique Id of a content definition instance
 * @returns a string with the Id
 */
public abstract String getUniqueId(CmsObject cms) ;
/**
 * Gets the url of the field entry
 * You have to override this method in your content definition,
 * if you wish to link url´s to the field entries
 * @returns a String with the url
 */
public String getUrl() {
	return null;
}
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
public void setLockstate(int lockstate) {

}
/**
 * abstract write method
 * must be overwritten in content definition
 */
public abstract void write(CmsObject cms) throws Exception;
}

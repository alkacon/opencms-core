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

import java.lang.reflect.*;
import com.opencms.file.*;

/**
 * Insert the type's description here.
 * Creation date: (08.11.00 12:02:57)
 * @author: Michael Knoll
 */
public class FilterMethod {
	private String m_filterName;
	private Method m_filterMethod;
	private Object [] m_defaultParameter;
  private String m_defaultFilterParam = "";

/**
 * FilterMethod constructor
 */
public FilterMethod(String filterName, Method filterMethod, Object [] filterParameters) {
  this(filterName, filterMethod, filterParameters, "");
}

/**
 * FilterMethod constructor with a default value in the Selectbox
 */
public FilterMethod(String filterName, Method filterMethod, Object [] filterParameters, String defaultFilterParam) {

	m_filterName = filterName;
	m_filterMethod = filterMethod;
	m_defaultParameter = filterParameters;
  m_defaultFilterParam = defaultFilterParam;
}


/**
 * gets the default parameter
 */
public Object [] getDefaultParameter() {

	return m_defaultParameter;
	}
/**
 * gets the filter method
 */
public Method getFilterMethod() {

	return m_filterMethod;
	}
/**
 * gets the filtername
 */
public String getFilterName() {

	return m_filterName;
	}
/**
 * Returns, if this filter needs additional user parameter.
 * @return true if this filter needs additional user paramet. Otherwise return false.
 */
public boolean hasUserParameter() {

	// check if this filter needs a user-parameter

	Class[] paramTypes = m_filterMethod.getParameterTypes();

	if( (paramTypes.length > 0) && (paramTypes[0] == CmsObject.class) ) {
		return (paramTypes.length > (m_defaultParameter.length + 1) );
	} else {
		return (paramTypes.length > m_defaultParameter.length );
	}
}
/**
 * sets the filter parameter
 * @param parameter the filter parameter
 */
public void setDefaultParameter(Object [] parameter) {

	m_defaultParameter = parameter;
}
/**
 * sets the filter method
 * @param method the filter method
 */
public void setFilterMethod(Method method) {

	m_filterMethod = method;
}
/**
 * sets the filter name
 * @param name the filter name
 */
public void setFilterName(String name) {

	m_filterName = name;
}

public String getDefaultFilterParam() {
  return m_defaultFilterParam;
}
}

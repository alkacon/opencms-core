/*
* File   : $Source: /alkacon/cvs/opencms/modules/org.opencms.legacy/src/com/opencms/defaults/Attic/CmsFilterMethod.java,v $
* Date   : $Date: 2005/05/16 17:45:08 $
* Version: $Revision: 1.1 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.defaults;

import java.lang.reflect.*;
import org.opencms.file.*;

/**
 * Insert the type's description here.
 * Creation date: (08.11.00 12:02:57)
 * @author Michael Knoll
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsFilterMethod {
    private String m_filterName;
    private Method m_filterMethod;
    private Object [] m_defaultParameter;
  private String m_defaultFilterParam = "";

/**
 * FilterMethod constructor.<p>
 * 
 * @param filterName name of the filter
 * @param filterMethod the filter method
 * @param filterParameters additional filter parameters
 */
public CmsFilterMethod(String filterName, Method filterMethod, Object [] filterParameters) {
  this(filterName, filterMethod, filterParameters, "");
}

/**
 * FilterMethod constructor with a default value in the Selectbox.<p>
 * 
 * @param filterName name of the filter
 * @param filterMethod the filter method
 * @param filterParameters additional filter parameters
 * @param defaultFilterParam the default value of the filter
 */
public CmsFilterMethod(String filterName, Method filterMethod, Object [] filterParameters, String defaultFilterParam) {

    m_filterName = filterName;
    m_filterMethod = filterMethod;
    m_defaultParameter = filterParameters;
  m_defaultFilterParam = defaultFilterParam;
}


/**
 * Gets the default parameter.<p>
 * 
 * @return the default parameters
 */
public Object [] getDefaultParameter() {

    return m_defaultParameter;
    }
/**
 * Gets the filter method.<p>
 * 
 * @return the filter method
 */
public Method getFilterMethod() {

    return m_filterMethod;
    }
/**
 * Gets the filtername.
 * 
 * @return the name of the filter
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

    if ((paramTypes.length > 0) && (paramTypes[0] == CmsObject.class)) {
        return paramTypes.length > (m_defaultParameter.length + 1);
    } else {
        return paramTypes.length > m_defaultParameter.length;
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


/**
 * Gets the value of the default filter parameter.<p>
 * 
 * @return the default value
 */
public String getDefaultFilterParam() {
  return m_defaultFilterParam;
}
}

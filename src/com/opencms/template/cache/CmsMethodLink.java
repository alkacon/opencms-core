/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsMethodLink.java,v $
* Date   : $Date: 2005/02/18 15:18:52 $
* Version: $Revision: 1.7 $
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

package com.opencms.template.cache;


/**
 * An instance of CmsMethodLink is a link to a method. The link contains
 * the method name and the parameter for the method.
 *
 * @author Hanjo Riege
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsMethodLink {

    /**
     * The name of the method to link to.
     */
    private String m_methodName;

    /**
     * The parameter for the methode.
     */
    private String m_parameter;

    /**
     * Creates a new method-Link.
     * @param methodName - the name of the method to link to.
     */
    public CmsMethodLink(String methodName, String methodParameter) {
        m_methodName = methodName;
        m_parameter = methodParameter;
    }

    /**
     * Returns the name of the method and the parameter.
     * @return the name of the method with the parameter.
     */
    public String toString() {
        return m_methodName +"("+m_parameter+")";
    }

    /**
     * Returns the name of the method to link to.
     * @return the name of the method to link to.
     */
    public String getMethodeName() {
        return m_methodName;
    }

    /**
     * Returns the parameter for the method.
     * @return the parameter for the method.
     */
    public String getMethodParameter(){
        return m_parameter;
    }

}
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsMethodLink.java,v $
* Date   : $Date: 2001/07/03 11:53:57 $
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
package com.opencms.template.cache;

import java.util.*;
import java.io.*;
import com.opencms.file.*;

/**
 * An instance of CmsMethodLink is a link to a method. The link contains
 * the method name and the parameter for the method.
 *
 * @author: Hanjo Riege
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
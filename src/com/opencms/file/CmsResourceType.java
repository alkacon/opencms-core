/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsResourceType.java,v $
* Date   : $Date: 2003/04/01 15:20:18 $
* Version: $Revision: 1.15 $
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

package com.opencms.file;

import com.opencms.core.I_CmsConstants;

import java.io.Serializable;

/**
 * Describes a resource-type. To determine the special launcher
 * for a resource this resource-type is needed.
 *
 * @author Michael Emmerich
 * @version $Revision: 1.15 $ $Date: 2003/04/01 15:20:18 $
 */
public class CmsResourceType implements I_CmsConstants, Serializable {

     /**
      * The id of resource type.
      */
    private int m_resourceType;

    /**
     * The id of the launcher used by this resource.
     */
    private int m_launcherType;

    /**
     * The resource type name.
     */
    private String m_resourceTypeName;

    /**
     * The class name of the Java class launched by the launcher.
     */
    private String m_launcherClass;


    /**
     * Constructor, creates a new CmsResourceType object.
     *
     * @param resourceType The id of the resource type.
     * @param launcherType The id of the required launcher.
     * @param resourceTypeName The printable name of the resource type.
     * @param launcherClass The Java class that should be invoked by the launcher.
     * This value is <b> null </b> if the default invokation class should be used.
     */
    public CmsResourceType(int resourceType, int launcherType,
                           String resourceTypeName, String launcherClass){

        m_resourceType=resourceType;
        m_launcherType=launcherType;
        m_resourceTypeName=resourceTypeName;
        m_launcherClass=launcherClass;
    }
     /**
     * Returns the name of the Java class loaded by the launcher.
     * This method returns <b>null</b> if the default class for this type is used.
     *
     * @return the name of the Java class.
     */
     public String getLauncherClass() {
         if ((m_launcherClass == null) || (m_launcherClass.length()<1)) {
            return C_UNKNOWN_LAUNCHER;
         } else {
            return m_launcherClass;
         }
     }
     /**
     * Returns the launcher type needed for this resource-type.
     *
     * @return the launcher type for this resource-type.
     */
     public int getLauncherType() {
         return m_launcherType;
     }
    /**
     * Returns the name for this resource-type.
     *
     * @return the name for this resource-type.
     */
     public String getResourceName() {
         return m_resourceTypeName;
     }
    /**
     * Returns the type of this resource-type.
     *
     * @return the type of this resource-type.
     */
    public int getResourceType() {
         return m_resourceType;
     }
    /**
     * Returns a string-representation for this object.
     * This can be used for debugging.
     *
     * @return string-representation for this object.
     */
     public String toString() {
        StringBuffer output=new StringBuffer();
        output.append("[ResourceType]:");
        output.append(m_resourceTypeName);
        output.append(" , Id=");
        output.append(m_resourceType);
        output.append(" , launcherType=");
        output.append(m_launcherType);
        output.append(" , launcherClass=");
        output.append(m_launcherClass);
        return output.toString();
      }
}

/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsElementDescriptor.java,v $
* Date   : $Date: 2005/02/18 15:18:52 $
* Version: $Revision: 1.10 $
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
 * This descriptor is used to locate CmsElement-Objects with the
 * CmsElementLocator. It is the key for a CmsElement.
 *
 * @author Andreas Schouten
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsElementDescriptor {

    /**
     * The name of the class for this descriptor.
     */
    private String m_className;

    /**
     * The name of the template-file for this descriptor.
     */
    private String m_templateName;

    /**
     * The constructor to create a new CmsElementDescriptor.
     *
     * @param className the name of the class for this descriptor.
     * @param templateName the name of the template for this descriptor.
     */
    public CmsElementDescriptor(String className, String templateName) {
        m_className = className;
        m_templateName = templateName;
    }

    /**
     * Returns the key of this descriptor.
     *
     * @return the key of this descriptor.
     */
    public String getKey() {
        return m_className + "|" + m_templateName;
    }

    /**
     * Get the class name for the element defined.
     * @return Class name for the element.
     */
    public String getClassName() {
        return m_className;
    }

    /**
     * Get the template name for the element defined.
     * @return Template name for the element.
     */
    public String getTemplateName() {
        return m_templateName;
    }

    /**
     * We have to return a hashcode for the hashtable. We can use the hashcode
     * from the Strings m_className and m_templatename.
     * @return The hashCode.
     */
    public int hashCode(){
        return (m_className + m_templateName).hashCode();
    }

    /**
     * Compares the overgiven object with this object.
     *
     * @return true, if the object is identically else it returns false.
     */
    public boolean equals(Object obj) {
        // check if the object is a CmsElementDescriptor object
        if (obj instanceof CmsElementDescriptor) {
            // same key ?
            if (((CmsElementDescriptor)obj).getKey().equals(getKey()) ){
                return true;
            }
        }
        return false;
    }

    /**
     * toString methode
     */
    public String toString(){
        return m_className + " | " + m_templateName;
    }
}
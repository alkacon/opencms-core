/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsPropertydefinition.java,v $
* Date   : $Date: 2003/04/01 15:20:18 $
* Version: $Revision: 1.9 $
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

/**
 * Describes a Propertydefinition in the Cms.
 *
 * @author Andreas Schouten
 * @version $Revision: 1.9 $ $Date: 2003/04/01 15:20:18 $
 */
// TESTFIX (a.kandzior@alkacon.com) Old code: public class CmsPropertydefinition implements Cloneable {
public class CmsPropertydefinition implements Cloneable, Comparable {
    /**
     * The name of this Propertydefinition.
     */
    private String m_name = null;

    /**
     * The resource-type for this Propertydefinition.
     */
    private int m_resourceType;

    /**
     * The id of this Propertydefinition.
     */
    private int m_id;

    /**
     * Creates a new CmsPropertydefinition.
     *
     * @param id The id of the Propertydefinition.
     * @param name The name of the Propertydefinition.
     * @param resourcetype The type of the resource for this Propertydefinition.
     */
    public CmsPropertydefinition(int id, String name, int resourcetype) {
        m_id = id;
        m_name = name;
        m_resourceType = resourcetype;

    }
    /**
    * Clones the CmsPropertydefinition by creating a new CmsPropertydefinition.
    * @return Cloned CmsPropertydefinition.
    */
    public Object clone() {
        return new CmsPropertydefinition(m_id, m_name, m_resourceType);
    }
    /**
     * Compares the overgiven object with this object.
     *
     * @return true, if the object is identically else it returns false.
     */
    public boolean equals(Object obj) {
        boolean equal=false;
        // check if the object is a CmsPropertydefinition object
        if (obj instanceof CmsPropertydefinition) {
            // same ID than the current project?
            if (((CmsPropertydefinition)obj).getId() == m_id){
                equal = true;
            }
        }
        return equal;
    }
    /**
     * Returns the id of a Propertydefinition. This method has the package-visibility.
     *
     * @return id The id of this Propertydefinition.
     */
    public int getId() {
        return m_id;
    }
    /**
     * Returns the name of this Propertydefinition.
     *
     * @return name The name of the Propertydefinition.
     */
    public String getName() {
        return m_name;
    }

    /**
     * Gets the resourcetype for this Propertydefinition.
     *
     * @return the resourcetype of this Propertydefinition.
     */
    public int getType() {
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
        output.append("[Propertydefinition]:");
        output.append(m_name);
        output.append(" , Id=");
        output.append(m_id);
        output.append(" , ResourceType=");
        output.append(getType());
        return output.toString();
    }
    
    /**
     * TESTFIX (a.kandzior@alkacon.com) New code:
     * Implements the comparable interface.
     */
    public int compareTo(Object obj) {
        if ((obj == null) || ! (obj instanceof CmsPropertydefinition)) return 0;
        CmsPropertydefinition def = (CmsPropertydefinition)obj;
        return (getName().compareTo(def.getName()));
    }    
    // End TESTFIX
}

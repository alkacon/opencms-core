/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/Attic/CmsPropertydefinition.java,v $
 * Date   : $Date: 2004/11/11 16:29:39 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file;

import org.opencms.util.CmsUUID;

/**
 * Describes a Propertydefinition in the Cms.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.7 $
 */
public class CmsPropertydefinition implements Cloneable, Comparable {

    /** The null property definition object. */
    private static final CmsPropertydefinition C_NULL_PROPERTY_DEFINITION = new CmsPropertydefinition(CmsUUID.getNullUUID(), "", -1);

    /** The id of this property definition. */
    private CmsUUID m_id;
    
    /** The name of this property definition. */
    private String m_name;

    /** The mapping type for this property definition. */
    private int m_mappingType;

    /**
     * Creates a new CmsPropertydefinition.<p>
     *
     * @param id the id of the property definition
     * @param name the name of the property definition
     * @param mappingtype the type of the resource for this property definition
     */
    public CmsPropertydefinition(CmsUUID id, String name, int mappingtype) {
        m_id = id;
        m_name = name;
        m_mappingType = mappingtype;

    }
    
    /**
     * Returns a clone of this Objects instance.<p>
     * 
     * @return a clone of this instance
     */   
    public Object clone() {
        return new CmsPropertydefinition(m_id, m_name, m_mappingType);
    }
    
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {
        if ((obj == null) || ! (obj instanceof CmsPropertydefinition)) {
            return 0;
        }
        CmsPropertydefinition def = (CmsPropertydefinition)obj;
        return (getName().compareTo(def.getName()));
    }    
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof CmsPropertydefinition) {
            return ((CmsPropertydefinition)obj).getId().equals(m_id);
        }
        return false;    
    }
    
    /**
     * Returns the id of this property definition.<p>
     *
     * @return id the id of this Propertydefinition
     */
    public CmsUUID getId() {
        return m_id;
    }
    
    /**
     * Returns the name of this property definition.<p>
     *
     * @return name The name of this property definition
     */
    public String getName() {
        return m_name;
    }

    /**
     * Gets the resource type of this property definition.<p>
     *
     * @return the resource type of this property definition
     */
    public int getType() {
        return m_mappingType;
    }
    
    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        if (m_name != null) {
            return m_name.hashCode();
        }
        return 0;
    }

    /**
     * Returns the null property definition.<p>
     * 
     * @return the null property definition
     */
    public static CmsPropertydefinition getNullPropertyDefinition() {
        return CmsPropertydefinition.C_NULL_PROPERTY_DEFINITION;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer result=new StringBuffer();
        result.append("[Propertydefinition]");
        result.append(" name:");
        result.append(m_name);
        result.append(" id:");
        result.append(m_id);
        result.append(" type:");
        result.append(getType());
        return result.toString();
    }
}

package com.opencms.file;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsPropertydefinition.java,v $
 * Date   : $Date: 2000/08/08 14:08:23 $
 * Version: $Revision: 1.5 $
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

/**
 * This class describes a Propertydefinition in the Cms.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.5 $ $Date: 2000/08/08 14:08:23 $
 */
public class CmsPropertydefinition implements Cloneable {
	/**
	 * The name of this Propertydefinition.
	 */
	private String m_name = null;
	
	/**
	 * The resource-type for this Propertydefinition.
	 */
	private int m_resourceType;
	
	/**
	 * The type of this Propertydefinition.
	 */
	private int m_propertydefinitionType;
		
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
	 * @param type The type of the Propertydefinition (e.g. mandatory)
	 */
	public CmsPropertydefinition(int id, String name, int resourcetype, int type) {
		m_id = id;
		m_name = name;
		m_resourceType = resourcetype;
		m_propertydefinitionType = type;
	}
	/** 
	* Clones the CmsPropertydefinition by creating a new CmsPropertydefinition.
	* @return Cloned CmsPropertydefinition.
	*/
	public Object clone() {
		return new CmsPropertydefinition(m_id, m_name, m_resourceType,m_propertydefinitionType);
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
	 * Gets the type for this Propertydefinition.
	 * The type may be C_PROPERTYDEF_TYPE_NORMAL, C_PROPERTYDEF_TYPE_OPTIONAL or
	 * C_PROPERTYDEF_TYPE_MANDATORY.
	 * 
	 * @return the type of this Propertydefinition.
	 */
	public int getPropertydefType() {
		return m_propertydefinitionType;
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
	 * Sets the type for this Propertydefinition.
	 * The type may be C_PROPERTYDEF_TYPE_NORMAL, C_PROPERTYDEF_TYPE_OPTIONAL or
	 * C_PROPERTYDEF_TYPE_MANDATORY.
	 * 
	 * @param type The new type fot this Propertydefinition.
	 */
	public void setPropertydefType(int type) {
		m_propertydefinitionType = type;
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
		output.append(" , PropertydefType=");
		output.append(getPropertydefType());
		return output.toString();
	}
}

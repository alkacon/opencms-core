/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsMetadefinition.java,v $
 * Date   : $Date: 2000/02/15 17:43:59 $
 * Version: $Revision: 1.3 $
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

package com.opencms.file;

/**
 * This class describes a metadefinition in the Cms.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.3 $ $Date: 2000/02/15 17:43:59 $
 */
public class CmsMetadefinition extends A_CmsMetadefinition {
	/**
	 * The name of this metadefinition.
	 */
	private String m_name = null;
	
	/**
	 * The resource-type for this metadefinition.
	 */
	private int m_resourceType;
	
	/**
	 * The type of this metadefinition.
	 */
	private int m_metadefinitionType;
		
	/**
	 * The id of this metadefinition.
	 */
	private int m_id;
	
	/**
	 * Creates a new CmsMetadefinition.
	 * 
	 * @param id The id of the metadefinition.
	 * @param name The name of the metadefinition.
	 * @param resourcetype The type of the resource for this metadefinition.
	 * @param type The type of the metadefinition (e.g. mandatory)
	 */
	CmsMetadefinition(int id, String name, int resourcetype, int type) {
		m_id = id;
		m_name = name;
		m_resourceType = resourcetype;
		m_metadefinitionType = type;
	}

	/**
	 * Returns the name of this metadefinition.
	 * 
	 * @return name The name of the metadefinition.
	 */
	public String getName() {
		return m_name;
	}
	
	/**
	 * Returns the id of a metadefinition. This method has the package-visibility.
	 * 
	 * @return id The id of this metadefinition.
	 */
	int getId() {
		return m_id;
	}
	
	/**
	 * Gets the resourcetype for this metadefinition.
	 * 
	 * @return the resourcetype of this metadefinition.
	 */
	public int getType() {
		return m_resourceType;
	}


	/**
	 * Gets the type for this metadefinition.
	 * The type may be C_METADEF_TYPE_NORMAL, C_METADEF_TYPE_OPTIONAL or
	 * C_METADEF_TYPE_MANDATORY.
	 * 
	 * @return the type of this metadefinition.
	 */
	public int getMetadefType() {
		return m_metadefinitionType;
	}
	
	/**
	 * Sets the type for this metadefinition.
	 * The type may be C_METADEF_TYPE_NORMAL, C_METADEF_TYPE_OPTIONAL or
	 * C_METADEF_TYPE_MANDATORY.
	 * 
	 * @param type The new type fot this metadefinition.
	 */
	public void setMetadefType(int type) {
		m_metadefinitionType = type;
	}
	
	/**
	 * Returns a string-representation for this object.
	 * This can be used for debugging.
	 * 
	 * @return string-representation for this object.
	 */
	public String toString() {
        StringBuffer output=new StringBuffer();
        output.append("[Metadefinition]:");
        output.append(m_name);
        output.append(" , Id=");
        output.append(m_id);
        output.append(" , ResourceType=");
        output.append(getType());
        output.append(" , MetadefType=");
        output.append(getMetadefType());
        return output.toString();
	}
	
	/**
	 * Compares the overgiven object with this object.
	 * 
	 * @return true, if the object is identically else it returns false.
	 */
	public boolean equals(Object obj) {
        boolean equal=false;
        // check if the object is a CmsMetadefinition object
        if (obj instanceof CmsMetadefinition) {
            // same ID than the current project?
            if (((CmsMetadefinition)obj).getId() == m_id){
                equal = true;
            }
        }
        return equal;
	}

}

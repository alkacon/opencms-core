/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/A_CmsProject.java,v $
 * Date   : $Date: 2000/02/15 17:43:59 $
 * Version: $Revision: 1.10 $
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
 * This abstract class describes a project. A project is used to handle versions of 
 * one resource.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.10 $ $Date: 2000/02/15 17:43:59 $
 */
abstract public class A_CmsProject
{
	/**
	 * Returns the name of this project.
	 * 
	 * @return the name of this project.
	 */
    abstract public String getName();

	/**
	 * Returns the description of this project.
	 * 
	 * @return description The description of this project.
	 */
	abstract public String getDescription();

	/**
	 * Sets the description of this project.
	 * 
	 * @param description The description of this project.
	 */
	abstract public void setDescription(String description);
	
	/**
	 * Returns the state of this project.<BR/>
	 * This may be C_PROJECT_STATE_UNLOCKED, C_PROJECT_STATE_LOCKED, 
	 * C_PROJECT_STATE_ARCHIVE.
	 * 
	 * @return the state of this project.
	 */
	abstract public int getFlags();

	/**
	 * Sets the state of this project.<BR/>
	 * This may be C_PROJECT_STATE_UNLOCKED, C_PROJECT_STATE_LOCKED, 
	 * C_PROJECT_STATE_ARCHIVE.
	 * 
	 * @param flags The flag to bes set.
	 */
	abstract void setFlags(int flags);
	
	/**
	 * Returns the id of this project.
	 * 
	 * @return the id of this project.
	 */	
    abstract int getId();
	
	/**
	 * Returns the userid of the project owner.
	 * 
	 * @return the userid of the project owner.
	 */
	abstract int getOwnerId();
	
	/**
	 * Returns the groupid of this project.
	 * 
	 * @return the groupid of this project.
	 */
    abstract int getGroupId();
	
	/**
	 * Returns the manager groupid of this project.
	 * 
	 * @return the manager groupid of this project.
	 */
    abstract int getManagerGroupId();
	
	/**
	 * Returns the taskid of this project.
	 * 
	 * @return the taskid of this project.
	 */
    abstract int getTaskId();
	
	/**
	 * Returns the publishing date of this project.
	 * 
	 * @return the publishing date of this project.
	 */
    abstract public long getCreateDate();
	
	/**
	 * Returns the publishing date of this project.
	 * 
	 * @return the publishing date of this project.
	 */
    abstract public long getPublishingDate();
	
	/**
	 * Sets the publishing date of this project.
	 * 
	 * @param the publishing date of this project.
	 */
    abstract void setPublishingDate(long publishingDate);
	
	/**
	 * Returns a string-representation for this object.
	 * This can be used for debugging.
	 * 
	 * @return string-representation for this object.
	 */
	abstract public String toString();
	
	/**
	 * Compares the overgiven object with this object.
	 * 
	 * @return true, if the object is identically else it returns false.
	 */
    abstract public boolean equals(Object obj);

}

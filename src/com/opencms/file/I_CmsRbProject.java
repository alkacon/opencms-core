/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/I_CmsRbProject.java,v $
 * Date   : $Date: 2000/04/19 10:15:59 $
 * Version: $Revision: 1.11 $
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

import java.util.*;

import com.opencms.core.*;

/**
 * This public class describes a resource broker for projects in the Cms.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.11 $ $Date: 2000/04/19 10:15:59 $
 */
interface I_CmsRbProject {
	
	/**
	 * Reads a project from the Cms.
	 * 
	 * @param id The id of the project to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public A_CmsProject readProject(int id)
		 throws CmsException ;
	
	/**
	 * Reads a project from the Cms.
	 * 
	 * @param task The task of the project to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public A_CmsProject readProject(A_CmsTask task)
		 throws CmsException ;
	 
	/**
	 * Creates a project.
	 * 
	 * @param id The new id of the project. (normaly = C_UNKNOWN_ID)
	 * @param name The name of the project to read.
	 * @param description The description for the new project.
	 * @param task The task.
	 * @param owner The owner to be set.
	 * @param group the group to be set.
	 * @param managergroup the managergroup to be set.
	 * @param flags The flags to be set.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public A_CmsProject createProject(int id, String name, String description, A_CmsTask task, 
										 A_CmsUser owner, A_CmsGroup group, 
										 A_CmsGroup managergroup, int flags)
		 throws CmsException;
	
	/**
	 * Creates a project.
	 * 
	 * @param name The name of the project to read.
	 * @param description The description for the new project.
	 * @param task The task.
	 * @param owner The owner to be set.
	 * @param group the group to be set.
	 * @param managergroup the managergroup to be set.
	 * @param flags The flags to be set.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public A_CmsProject createProject(String name, String description, A_CmsTask task, 
										 A_CmsUser owner, A_CmsGroup group, 
										 A_CmsGroup managergroup, int flags)
		 throws CmsException;
	 
	/**
	 * Updates a project.
	 * 
	 * @param project The project that will be written.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public A_CmsProject writeProject(A_CmsProject project)
		 throws CmsException;

	/**
	 * Deletes a project.
	 * 
	 * @param project The project that will be deleted.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public void deleteProject(A_CmsProject project)
		 throws CmsException;
	 
	/**
	 * Returns all projects, which are owned by a user.
	 * 
	 * @param user The user to test.
	 * 
	 * @return a Vector of projects.
	 */
	 public Vector getAllAccessibleProjectsByUser(A_CmsUser user)
		 throws CmsException;

	/**
	 * Returns all projects, which the group may access.
	 * 
	 * @param group The group to test.
	 * 
	 * @return a Vector of projects.
	 */
	 public Vector getAllAccessibleProjectsByGroup(A_CmsGroup group)
		 throws CmsException;

	/**
	 * Returns all projects, which the managergroup may manage.
	 * 
	 * @param managergroup The group to test.
	 * 
	 * @return a Vector of projects.
	 */
	 public Vector getAllAccessibleProjectsByManagerGroup(A_CmsGroup managergroup)
		 throws CmsException;
	 
	/**
	 * Returns all projects with the overgiven flag.
	 * 
	 * @param flag The flag for the project.
	 * 
	 * @return a Vector of projects.
	 */
	 public Vector getAllProjects(int flag)
		 throws CmsException;
}

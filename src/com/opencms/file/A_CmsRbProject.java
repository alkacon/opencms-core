package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This abstract class describes a resource broker for projects in the Cms.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.5 $ $Date: 1999/12/17 17:25:36 $
 */
abstract class A_CmsRbProject {
	
	/**
	 * Reads a project from the Cms.
	 * 
	 * @param name The name of the project to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 abstract A_CmsProject readProject(String name)
		 throws CmsException ;
	
	/**
	 * Creates a project.
	 * 
	 * @param name The name of the project to read.
	 * @param description The description for the new project.
	 * @param task The globe task.
	 * @param owner The owner to be set.
	 * @param group the group to be set.
	 * @param flags The flags to be set.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 abstract A_CmsProject createProject(String name, String description, A_CmsTask task, 
										 A_CmsUser owner, A_CmsGroup group, int flags)
		 throws CmsException;
	
	/**
	 * Updates a project.
	 * 
	 * @param project The project that will be written.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 abstract A_CmsProject writeProject(A_CmsProject project)
		 throws CmsException;

	/**
	 * Returns all projects, which are owned by a user.
	 * 
	 * @param user The user to test.
	 * 
	 * @return a Vector of projects.
	 */
	 abstract Vector getAllAccessibleProjectsByUser(A_CmsUser user)
		 throws CmsException;

	/**
	 * Returns all projects, which the group may access.
	 * 
	 * @param group The group to test.
	 * 
	 * @return a Vector of projects.
	 */
	 abstract Vector getAllAccessibleProjectsByGroup(A_CmsGroup group)
		 throws CmsException;
}

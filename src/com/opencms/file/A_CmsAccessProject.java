package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This abstract class describes the access to projects in the Cms.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.3 $ $Date: 1999/12/16 18:55:53 $
 */
abstract class A_CmsAccessProject {

	/**
	 * Reads a project from the Cms.
	 * 
	 * @param name The name of the project to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 abstract A_CmsProject readProject(String name)
		throws CmsException;
	
	/**
	 * Creates a project.
	 * 
	 * @param name The name of the project to read.
	 * @param description The description for the new project.
	 * @param globetaskId The id of the globe task.
	 * @param ownerId The id of the owner to be set.
	 * @param groupId the id of the group to be set.
	 * @param flags The flags for the project (e.g. archive).
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if
	 * a project with the same name for this resource-type exists already.
	 */
	 abstract A_CmsProject createProject(String name, String description, int globetaskId, 
								int ownerId, int groupId, int flags)
		throws CmsException, CmsDuplicateKeyException;
	
	/**
	 * Updates a project.
	 * 
	 * @param name The name of the project to update.
	 * @param description The description for the project.
	 * @param globetaskId The id of the globe task.
	 * @param ownerId The id of the owner to be set.
	 * @param groupId the id of the group to be set.
	 * @param flags The flags for the project (e.g. archive).
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 * @exception CmsDuplicateKeyException Throws CmsDuplicateKeyException if
	 * a project with the same name for this resource-type exists already.
	 */
	 abstract A_CmsProject updateProject(String name, String description, int globetaskId, 
								int ownerId, int groupId, int flags)
		throws CmsException, CmsDuplicateKeyException;

	/**
	 * Returns all projects, which are owned by a user.
	 * 
	 * @param userId the id of the user to test.
	 * 
	 * @return a Vector of projects.
	 */
	 abstract Vector getAllAccessibleProjectsByUser(int userId)
		 throws CmsException;

	/**
	 * Returns all projects, which the group may access.
	 * 
	 * @param groupId the id of the group to test.
	 * 
	 * @return a Vector of projects.
	 */
	 abstract Vector getAllAccessibleProjectsByGroup(int groupId)
		 throws CmsException;
}
